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
package com.junichi11.netbeans.modules.html.enhancements.ui.actions;

import com.junichi11.netbeans.modules.html.enhancements.utils.DocUtils;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.JTextComponent;
import javax.swing.text.StyledDocument;
import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.modules.csl.api.OffsetRange;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.cookies.EditorCookie;
import org.openide.text.NbDocument;
import org.openide.util.NbBundle.Messages;

@ActionID(
        category = "XML",
        id = "com.junichi11.netbeans.modules.html.enhancements.ui.actions.UpdateImageSizeAction")
@ActionRegistration(
        displayName = "#CTL_UpdateImageSizeAction")
@ActionReference(path = "Shortcuts", name = "AD-U")
@Messages("CTL_UpdateImageSizeAction=Update Image Size")
public final class UpdateImageSizeAction implements ActionListener {

    private final EditorCookie context;
    private static final String SRC_GROUP = "src"; // NOI18N
    private static final Pattern IMAGE_TAG_PATTERN = Pattern.compile("<img.*?src=([\"\\'])(?<src>.+?)\\1.*?>", Pattern.DOTALL); // NOI18N
    private static final Logger LOGGER = Logger.getLogger(UpdateImageSizeAction.class.getName());

    public UpdateImageSizeAction(EditorCookie context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        StyledDocument document = context.getDocument();
        JTextComponent editor = EditorRegistry.findComponent(document);
        updateImageSize(editor, document);
    }

    void updateImageSize(JTextComponent editor, StyledDocument document) {
        Caret caret = editor.getCaret();
        int offset = caret.getDot();
        try {
            // get src path
            String imgTag = DocUtils.getImgTag(document, offset);
            if (imgTag == null || imgTag.isEmpty()) {
                return;
            }
            Matcher matcher = IMAGE_TAG_PATTERN.matcher(imgTag);
            String src = ""; // NOI18N
            if (matcher.find()) {
                src = matcher.group(SRC_GROUP);
            }
            if (src.isEmpty()) {
                return;
            }
            Image image = DocUtils.getImage(src, document);
            if (image == null) {
                return;
            }
            String update = updateImgTag(imgTag, image.getWidth(null), image.getHeight(null));
            OffsetRange imgRange = DocUtils.getImgRange(document, offset);
            if (imgRange != OffsetRange.NONE) {
                NbDocument.runAtomicAsUser(document, () -> {
                    try {
                        document.remove(imgRange.getStart(), imgTag.length());
                        document.insertString(imgRange.getStart(), update, null);
                    } catch (BadLocationException ex) {
                        LOGGER.log(Level.WARNING, "Invalid offset: {0}", ex.offsetRequested()); // NOI18N
                    }
                });
            }
        } catch (BadLocationException ex) {
            LOGGER.log(Level.WARNING, "Invalid offset: {0}", ex.offsetRequested()); // NOI18N
        }
    }

    private String updateImgTag(String target, int width, int height) {
        if (!target.endsWith(" />")) { // NOI18N
            target = target.replace("/>", " />"); // NOI18N
        }

        // width
        if (target.contains("width=")) { // NOI18N
            target = target.replaceAll("width=\"[0-9]*\"", "width=\"" + width + "\""); // NOI18N
        } else {
            target = target.replaceAll("/>", "width=\"" + width + "\" />"); // NOI18N
        }

        // height
        if (target.contains("height=")) { // NOI18N
            target = target.replaceAll("height=\"[0-9]*\"", "height=\"" + height + "\""); // NOI18N
        } else {
            target = target.replaceAll("/>", "height=\"" + height + "\" />"); // NOI18N
        }

        return target;
    }
}
