/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package com.junichi11.netbeans.html.enhancement.editor;

import java.awt.Image;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import javax.imageio.ImageIO;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.html.lexer.HTMLTokenId;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.lexer.TokenUtilities;
import org.netbeans.modules.parsing.api.Source;
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
public class ImageCompletionProvider implements CompletionProvider {

    @Override
    public CompletionTask createTask(int queryType, JTextComponent component) {
        if (queryType != CompletionProvider.COMPLETION_QUERY_TYPE) {
            return null;
        }
        return new AsyncCompletionTask(new AsyncCompletionQuery() {
            @Override
            protected void query(CompletionResultSet resultSet, Document doc, int caretOffset) {
                TokenSequence<HTMLTokenId> ts = getTokenSequence(doc, caretOffset);
                if (ts == null) {
                    resultSet.finish();
                    return;
                }
                ts.move(caretOffset);
                ts.moveNext();
                ts.movePrevious();
                ts.movePrevious();
                Token token = ts.token();
                CharSequence tokenText = token.text();
                boolean isWidth = isWidth(tokenText);
                boolean isHeight = isHeight(tokenText);
                if (token.id() != HTMLTokenId.ARGUMENT) {
                    resultSet.finish();
                    return;
                }
                if (!isWidth && !isHeight) {
                    resultSet.finish();
                    return;
                }
                while (ts.movePrevious()) {
                    Token t = ts.token();
                    if (TokenUtilities.equals(t.text(), "src")) { // NOI18N
                        break;
                    }
                    if (t.id() == HTMLTokenId.TAG_OPEN) {
                        resultSet.finish();
                        return;
                    }
                }
                ts.moveNext();
                ts.moveNext();
                token = ts.token();
                CharSequence text = token.text();
                if (TokenUtilities.equals(text, "\"\"")) { // NOI18N
                    resultSet.finish();
                    return;
                }

                Source source = Source.create(doc);
                FileObject currentFile = source.getFileObject();
                String imagePath = normalizeImagePath(text.toString());
                Image image = null;

                // URL
                if (imagePath.startsWith("http://") || imagePath.startsWith("https://")) { // NOI18N
                    try {
                        URL imageUrl = new URL(imagePath);
                        image = ImageIO.read(imageUrl);
                    } catch (MalformedURLException ex) {
                        Exceptions.printStackTrace(ex);
                    } catch (IOException ex) {
                        resultSet.finish();
                        return;
                    }
                }

                // Path
                if (image == null && !imagePath.isEmpty()) {
                    FileObject imageFile = currentFile.getFileObject(imagePath);
                    if (imageFile == null) {
                        resultSet.finish();
                        return;
                    }
                    try {
                        image = ImageIO.read(imageFile.getInputStream());
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
                if (image == null) {
                    resultSet.finish();
                    return;
                }
                int value = 0;
                if (isHeight) {
                    value = image.getHeight(null);
                } else if (isWidth) {
                    value = image.getWidth(null);
                } else {
                    // bug
                }
                resultSet.addItem(new HtmlEnhancementCompletionItem(String.valueOf(value), caretOffset, 0));
                resultSet.finish();
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
     * @return
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

    protected boolean isWidth(CharSequence text) {
        if (TokenUtilities.equals(text, "width")) { // NOI18N
            return true;
        }
        return false;
    }

    protected boolean isHeight(CharSequence text) {
        if (TokenUtilities.equals(text, "height")) { // NOI18N
            return true;
        }
        return false;
    }
}
