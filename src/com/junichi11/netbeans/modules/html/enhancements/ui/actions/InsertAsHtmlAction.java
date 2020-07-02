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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.editor.indent.api.Reformat;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.NbBundle.Messages;

@ActionID(
    category = "Edit",
    id = "com.junichi11.netbeans.modules.html.enhancements.ui.actions.InsertAsHtmlAction")
@ActionRegistration(displayName = "#CTL_InsertAsHtmlAction")
@ActionReferences({
    @ActionReference(path = "Menu/Edit", position = 1470),
    @ActionReference(path = "Loaders/image/png-gif-jpeg-bmp/Actions", position = 150)
})
@Messages("CTL_InsertAsHtmlAction=Insert as HTML")
public final class InsertAsHtmlAction implements ActionListener {

    private static final String SLASH = "/"; // NOI18N
    private static final String PARENT = "../"; // NOI18N
    private static final String IMG_TAG_FORMAT = "<img src=\"%s\" alt=\"\" width=\"%s\" height=\"%s\" />"; // NOI18N
    private final List<DataObject> contexts;
    private static final Set<String> IMG_MIME_TYPES = new HashSet<String>();
    private static final Logger LOGGER = Logger.getLogger(InsertAsHtmlAction.class.getName());

    static {
        IMG_MIME_TYPES.add("image/png"); // NOI18N
        IMG_MIME_TYPES.add("image/jpeg"); // NOI18N
        IMG_MIME_TYPES.add("image/gif"); // NOI18N
        IMG_MIME_TYPES.add("image/bmp"); // NOI18N
    }

    public InsertAsHtmlAction(List<DataObject> context) {
        this.contexts = context;
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        JTextComponent editor = getEditor();
        if (editor == null) {
            return;
        }

        String imgTag = buildImgTags();
        int offset = editor.getCaret().getDot();
        Document document = editor.getDocument();
        try {
            document.insertString(offset, imgTag, null);
        } catch (BadLocationException ex) {
            LOGGER.log(Level.WARNING, "Invalid offset: {0}", ex.offsetRequested());
        }

        int stringCount = imgTag.length();
        if (stringCount == 0) {
            return;
        }
        reformat(document, offset, offset + stringCount);
    }

    private String buildImgTags() {
        StringBuilder sb = new StringBuilder();
        boolean isMulti = false;
        for (DataObject context : contexts) {
            FileObject imageFile = context.getPrimaryFile();
            if (imageFile == null) {
                break;
            }
            if (!isImage(imageFile)) {
                continue;
            }
            if (isMulti) {
                sb.append("\n"); // NOI18N
            }
            sb.append(createImgTag(imageFile));
            isMulti = true;
        }
        return sb.toString();
    }

    /**
     * Get editor
     *
     * @return
     */
    private JTextComponent getEditor() {
        return EditorRegistry.lastFocusedComponent();
    }

    /**
     * Create img tag. Get the relative path, with and height, set it to the
     * format.
     *
     * @param imageFile
     * @return img tag
     */
    private String createImgTag(FileObject imageFile) {
        BufferedImage read = null;
        try {
            read = ImageIO.read(FileUtil.toFile(imageFile));
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, null, ex);
        }
        if (read == null) {
            return null;
        }
        Document document = getEditor().getDocument();
        FileObject fileObject = DocUtils.getFileObject(document);
        String relativePath = getRelativePath(fileObject, imageFile);

        return String.format(IMG_TAG_FORMAT, relativePath, read.getWidth(), read.getHeight());
    }

    /**
     * Get relative path from target file to image file
     *
     * @param from target file
     * @param to image file
     * @return realative path
     */
    private String getRelativePath(FileObject from, FileObject to) {
        String relativePath = FileUtil.getRelativePath(from.getParent(), to);
        if (relativePath != null) {
            return relativePath;
        }
        String fromPath = from.getPath();
        String toPath = to.getPath();
        String[] fromSplit = fromPath.split(SLASH);
        String[] toSplit = toPath.split(SLASH);
        int fromLength = fromSplit.length;
        int toLength = toSplit.length;
        int minLength = Math.min(fromLength, toLength);
        int diffPosition = 0;
        for (int i = 0; i < minLength; i++) {
            if (!fromSplit[i].equals(toSplit[i])) {
                diffPosition = i;
                break;
            }
        }
        int times = fromLength - diffPosition - 1;
        relativePath = getUpPath(times) + getDownPath(diffPosition, toSplit);
        return relativePath;
    }

    /**
     * Get path relative to the branch. (e.g. ../../../)
     *
     * @param times
     * @return path relative to the branch
     */
    private String getUpPath(int times) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < times; i++) {
            sb.append(PARENT);
        }
        return sb.toString();
    }

    /**
     * Get image path from branch. (e.g. path/to/image/file.png)
     *
     * @param position
     * @param target
     * @return path to image file
     */
    private String getDownPath(int position, String[] target) {
        StringBuilder sb = new StringBuilder();
        int length = target.length;
        int last = length - 1;
        for (int i = position; i < length; i++) {
            sb.append(target[i]);
            if (i != last) {
                sb.append(SLASH);
            }
        }
        return sb.toString();
    }

    /**
     * Chekck whether file is image
     *
     * @param image
     * @return
     */
    private boolean isImage(FileObject image) {
        String mimeType = image.getMIMEType();
        return IMG_MIME_TYPES.contains(mimeType);
    }

    /**
     * Reformat
     *
     * @param document
     * @param start
     * @param end
     */
    private void reformat(Document document, final int start, final int end) {
        final BaseDocument baseDoc = (BaseDocument) document;
        final Reformat reformat = Reformat.get(baseDoc);
        // reformat
        reformat.lock();
        try {
            baseDoc.runAtomic(() -> {
                try {
                    reformat.reformat(start, end);
                } catch (BadLocationException ex) {
                    LOGGER.log(Level.WARNING, null, ex);
                }
            });
        } finally {
            reformat.unlock();
        }
    }
}
