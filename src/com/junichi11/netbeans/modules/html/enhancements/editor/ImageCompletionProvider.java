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

import com.junichi11.netbeans.modules.html.enhancements.utils.DocUtils;
import java.awt.Image;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.AbstractDocument;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.editor.mimelookup.MimeRegistrations;
import org.netbeans.api.html.lexer.HTMLTokenId;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.lexer.TokenUtilities;
import org.netbeans.modules.php.editor.lexer.PHPTokenId;
import org.netbeans.spi.editor.completion.CompletionProvider;
import org.netbeans.spi.editor.completion.CompletionResultSet;
import org.netbeans.spi.editor.completion.CompletionTask;
import org.netbeans.spi.editor.completion.support.AsyncCompletionQuery;
import org.netbeans.spi.editor.completion.support.AsyncCompletionTask;

/**
 *
 * @author junichi11
 */
@MimeRegistrations({
    @MimeRegistration(mimeType = DocUtils.HTML_MIME_TYPE, service = CompletionProvider.class),
    @MimeRegistration(mimeType = DocUtils.PHP_MIME_TYPE, service = CompletionProvider.class)
})
public class ImageCompletionProvider implements CompletionProvider {

    private static final Logger LOGGER = Logger.getLogger(ImageCompletionProvider.class.getName());

    @Override
    public CompletionTask createTask(int queryType, JTextComponent component) {
        if (queryType != CompletionProvider.COMPLETION_QUERY_TYPE) {
            return null;
        }
        return new AsyncCompletionTask(new AsyncCompletionQueryImpl(), component);
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
    private static TokenSequence<HTMLTokenId> getTokenSequence(Document doc, int caretOffset) {
        TokenHierarchy<Document> hierarchy = TokenHierarchy.get(doc);
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
     * Check whether attribute is width.
     *
     * @param text token text
     * @return true if attribute is width, otherwise false.
     */
    private static boolean isWidth(CharSequence text) {
        return TokenUtilities.equals(text, "width"); // NOI18N
    }

    /**
     * Check whether attribute is height.
     *
     * @param text token text
     * @return true if attribute is height, otherwise false.
     */
    private static boolean isHeight(CharSequence text) {
        return TokenUtilities.equals(text, "height"); // NOI18N
    }

    //~Inner classes
    private static class AsyncCompletionQueryImpl extends AsyncCompletionQuery {

        public AsyncCompletionQueryImpl() {
        }

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

                // check whether attribute is width or height
                ImgAttribute imgAttr = createImgAttribute(ts, caretOffset);
                if (imgAttr == ImgAttribute.NONE) {
                    return;
                }

                String imgPath = getImgSrcPath(ts);
                if (imgPath == null) {
                    return;
                }
                Image image = DocUtils.getImage(imgPath, doc);
                if (image == null) {
                    return;
                }
                resultSet.addItem(new ImageSizeCompletionItem(getImageSize(image, imgAttr), caretOffset, 0));
            } finally {
                ad.readUnlock();
                resultSet.finish();
            }
        }

        @NonNull
        private ImgAttribute createImgAttribute(TokenSequence<HTMLTokenId> ts, int caretOffset) {
            ts.move(caretOffset);
            ts.moveNext(); // current
            if (ts.offset() == caretOffset) { // ^""
                return ImgAttribute.NONE;
            }
            ts.movePrevious(); // =?
            ts.movePrevious(); // attribute?
            Token<HTMLTokenId> token = ts.token();
            if (token == null
                    || token.id() != HTMLTokenId.ARGUMENT) {
                return ImgAttribute.NONE;
            }
            CharSequence tokenText = token.text();
            return ImgAttribute.create(isHeight(tokenText), isWidth(tokenText));
        }

        @CheckForNull
        private String getImgSrcPath(TokenSequence<HTMLTokenId> ts) {
            while (ts.movePrevious()) {
                Token<HTMLTokenId> token = ts.token();
                if (TokenUtilities.equals(token.text(), "src")) { // NOI18N
                    break;
                }
                if (token.id() == HTMLTokenId.TAG_OPEN) {
                    return null;
                }
            }
            ts.moveNext();
            ts.moveNext();
            Token<HTMLTokenId> token = ts.token();
            CharSequence srcText = token.text();
            if (srcText == null || TokenUtilities.equals(srcText, "\"\"")) { // NOI18N
                return null;
            }
            String imgPath = srcText.toString();
            return imgPath.substring(1, imgPath.length() - 1);
        }

        private String getImageSize(Image image, ImgAttribute imgAttribute) {
            // get value of width or height
            int value = 0;
            if (imgAttribute.isHeight()) {
                value = image.getHeight(null);
            } else if (imgAttribute.isWidth()) {
                value = image.getWidth(null);
            } else {
                LOGGER.log(Level.WARNING, "Not hight and width"); // NOI18N
            }
            return String.valueOf(value);
        }

    }

    private static final class ImgAttribute {

        private final boolean isHeight;
        private final boolean isWidth;
        private static final ImgAttribute NONE = new ImgAttribute(false, false);

        private ImgAttribute(boolean isHeight, boolean isWidth) {
            this.isHeight = isHeight;
            this.isWidth = isWidth;
        }

        public static ImgAttribute create(boolean isHeight, boolean isWidth) {
            if (!isHeight && !isWidth) {
                return NONE;
            }
            return new ImgAttribute(isHeight, isWidth);
        }

        public boolean isHeight() {
            return isHeight;
        }

        public boolean isWidth() {
            return isWidth;
        }
    }
}
