/*
 * Copyright 2020 junichi11.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.junichi11.netbeans.modules.html.enhancements.editor;

import java.awt.Image;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.text.AbstractDocument;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.editor.mimelookup.MimeRegistrations;
import org.netbeans.api.html.lexer.HTMLTokenId;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.lexer.TokenUtilities;
import org.netbeans.modules.csl.spi.GsfUtilities;
import org.netbeans.modules.php.editor.lexer.PHPTokenId;
import org.netbeans.spi.editor.completion.CompletionProvider;
import org.netbeans.spi.editor.completion.CompletionResultSet;
import org.netbeans.spi.editor.completion.CompletionTask;
import org.netbeans.spi.editor.completion.support.AsyncCompletionQuery;
import org.netbeans.spi.editor.completion.support.AsyncCompletionTask;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 *
 * @author junichi11
 */
@MimeRegistrations({
    @MimeRegistration(mimeType = "text/html", service = CompletionProvider.class),
    @MimeRegistration(mimeType = "text/x-php5", service = CompletionProvider.class)
})
public class ImageCompletionProvider implements CompletionProvider {

    private static final Logger LOGGER = Logger.getLogger(ImageCompletionProvider.class.getName());

    @Override
    public CompletionTask createTask(int queryType, JTextComponent component) {
        if (queryType != CompletionProvider.COMPLETION_QUERY_TYPE) {
            return null;
        }
        return new AsyncCompletionTask(new AsyncCompletionQuery() {
            @Override
            protected void query(CompletionResultSet resultSet, Document doc, int caretOffset) {
                AbstractDocument ad = (AbstractDocument) doc;
                ad.readLock();
                try {
                    // get token sequence
                    TokenSequence<HTMLTokenId> ts = getTokenSequence(doc, caretOffset);
                    if (ts == null) {
                        return;
                    }
                    ts.move(caretOffset);
                    ts.moveNext(); // current
                    ts.movePrevious(); // =?
                    ts.movePrevious(); // attribute?

                    // check whether attribute is width or height
                    Token token = ts.token();
                    CharSequence tokenText = token.text();
                    boolean isWidth = isWidth(tokenText);
                    boolean isHeight = isHeight(tokenText);
                    if (token.id() != HTMLTokenId.ARGUMENT) {
                        return;
                    }
                    if (!isWidth && !isHeight) {
                        return;
                    }

                    // search "src"
                    while (ts.movePrevious()) {
                        Token t = ts.token();
                        if (TokenUtilities.equals(t.text(), "src")) { // NOI18N
                            break;
                        }
                        if (t.id() == HTMLTokenId.TAG_OPEN) {
                            return;
                        }
                    }
                    ts.moveNext();
                    ts.moveNext();
                    token = ts.token();
                    CharSequence text = token.text();
                    if (TokenUtilities.equals(text, "\"\"")) { // NOI18N
                        return;
                    }

                    // img src
                    String imagePath = normalizeImagePath(text.toString());
                    Image image = null;

                    // URL
                    if (imagePath.startsWith("http://") || imagePath.startsWith("https://")) { // NOI18N
                        try {
                            URL imageUrl = new URL(imagePath);
                            image = ImageIO.read(imageUrl);
                        } catch (MalformedURLException ex) {
                            LOGGER.log(Level.WARNING, null, ex);
                            return;
                        } catch (IOException ex) {
                            LOGGER.log(Level.WARNING, null, ex);
                            return;
                        }
                    }

                    // Path
                    FileObject currentFile = getFileObject(doc);
                    if (currentFile != null && image == null && !imagePath.isEmpty()) {
                        FileObject imageFile = currentFile.getFileObject(imagePath);
                        if (imageFile == null) {
                            return;
                        }
                        try {
                            image = ImageIO.read(imageFile.getInputStream());
                        } catch (IOException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                    if (image == null) {
                        return;
                    }

                    // get value of width or height
                    int value = 0;
                    if (isHeight) {
                        value = image.getHeight(null);
                    } else if (isWidth) {
                        value = image.getWidth(null);
                    } else {
                        // bug
                    }
                    resultSet.addItem(new HtmlEnhancementCompletionItem(String.valueOf(value), caretOffset, 0));
                } finally {
                    ad.readUnlock();
                    resultSet.finish();
                }
            }
        }, component);
    }

    @Override
    public int getAutoQueryTypes(JTextComponent component, String typedText) {
        return 0;
    }

    /**
     * Get TokenSequence
     *
     * @param doc
     * @param caretOffset
     * @return
     */
    @SuppressWarnings("unchecked")
    protected TokenSequence<HTMLTokenId> getTokenSequence(Document doc, int caretOffset) {
        TokenHierarchy hierarchy = TokenHierarchy.get(doc);
        TokenSequence<HTMLTokenId> ts = hierarchy.tokenSequence(HTMLTokenId.language());
        if (ts == null) {
            TokenSequence<PHPTokenId> phpTs = hierarchy.tokenSequence(PHPTokenId.language());
            if (phpTs != null) {
                phpTs.move(caretOffset);
                phpTs.moveNext();
                ts = phpTs.embedded(HTMLTokenId.language());
            }
        }
        return ts;
    }

    /**
     * Create path For FileObject
     *
     * @param imagePath
     * @return normalized path
     */
    protected String normalizeImagePath(String imagePath) {
        imagePath = imagePath.substring(1, imagePath.length() - 1);
        if (imagePath.startsWith("./")) { // NOI18N
            imagePath = "." + imagePath; // NOI18N
        } else if (imagePath.startsWith("https://")) { // NOI18N
            // do nothing
        } else if (imagePath.startsWith("http://")) { // NOI18N
            // do nothing
        } else if (imagePath.isEmpty()) {
            // do nothing
        } else {
            imagePath = "../" + imagePath; // NOI18N
        }
        return imagePath;
    }

    /**
     * Check whether attribute is width.
     *
     * @param text token text
     * @return true if attribute is width, otherwise false.
     */
    protected boolean isWidth(CharSequence text) {
        return TokenUtilities.equals(text, "width"); // NOI18N
    }

    /**
     * Check whether attribute is height.
     *
     * @param text token text
     * @return true if attribute is height, otherwise false.
     */
    protected boolean isHeight(CharSequence text) {
        return TokenUtilities.equals(text, "height"); // NOI18N
    }

    /**
     * Get FileObject with Document.
     *
     * @param doc Document
     * @return FileObject
     */
    @CheckForNull
    private FileObject getFileObject(Document doc) {
        return GsfUtilities.findFileObject(doc);
    }
}
