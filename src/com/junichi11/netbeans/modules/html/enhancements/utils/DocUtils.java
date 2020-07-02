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
package com.junichi11.netbeans.modules.html.enhancements.utils;

import java.awt.Image;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.spi.GsfUtilities;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author junichi11
 */
public final class DocUtils {

    public static final String HTML_MIME_TYPE = "text/html"; // NOI18N
    public static final String PHP_MIME_TYPE = "text/x-php5"; // NOI18N

    private static final String IMG_TAG_START = "<img "; // NOI18N
    private static final Logger LOGGER = Logger.getLogger(DocUtils.class.getName());

    private DocUtils() {
    }

    /**
     * Get img tag range.
     *
     * @param doc Document
     * @param offset caret position
     * @return range array if exists img tag, otherwise null
     * @throws BadLocationException
     */
    public static OffsetRange getImgRange(Document doc, int offset) throws BadLocationException {
        int start = offset;
        int end = offset;
        final int last = doc.getLength();

        // start position
        while (start - 1 > 0) {
            if ((last - start >= IMG_TAG_START.length())
                    && start != offset
                    && doc.getText(start, IMG_TAG_START.length()).startsWith(IMG_TAG_START)) {
                break;
            }
            String text = doc.getText(start - 1, 1);
            if (text.equals(">")) { // NOI18N
                return OffsetRange.NONE;
            }
            start--;
        }

        // end position
        while (end <= last) {
            if (doc.getText(offset, end - offset).endsWith(">")) { // NOI18N
                break;
            }
            if (end == last) {
                return OffsetRange.NONE;
            }
            end++;
        }

        return new OffsetRange(start, end);
    }

    /**
     * Get img tag text
     *
     * @param doc Document
     * @param offset caret position
     * @return img tag text
     * @throws BadLocationException
     */
    @CheckForNull
    public static String getImgTag(Document doc, int offset) throws BadLocationException {
        OffsetRange range = getImgRange(doc, offset);
        if (range == OffsetRange.NONE) {
            return null;
        }

        return doc.getText(range.getStart(), range.getLength());
    }

    /**
     * Get FileObject from Document
     *
     * @param doc Document
     * @return FileObject
     */
    @CheckForNull
    public static FileObject getFileObject(Document doc) {
        return GsfUtilities.findFileObject(doc);
    }

    /**
     * Convert to path for FileObject
     *
     * @param path
     * @return relative path
     */
    private static String normalizePath(@NonNull String path) {
        if (path.isEmpty()) {
            return path;
        }
        if (path.startsWith("./")) { // NOI18N
            path = "." + path; // NOI18N
        } else {
            path = "../" + path; // NOI18N
        }
        return path;
    }

    /**
     * Get Image
     *
     * @param path
     * @param doc
     * @return Image
     */
    @CheckForNull
    public static Image getImage(String path, Document doc) {
        if (path == null || path.isEmpty()) {
            return null;
        }

        // URL
        if (path.startsWith("http://") || path.startsWith("https://")) { // NOI18N
            try {
                return ImageIO.read(new URL(path));
            } catch (MalformedURLException ex) {
                LOGGER.log(Level.WARNING, null, ex);
            } catch (IOException ex) {
                LOGGER.log(Level.WARNING, null, ex);
            }
            return null;
        }

        // relative path
        path = normalizePath(path);
        FileObject current = getFileObject(doc);
        if (current == null) {
            return null;
        }
        FileObject target = current.getFileObject(path);
        if (target == null) {
            return null;
        }
        try {
            return ImageIO.read(FileUtil.toFile(target));
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, null, ex);
        }
        return null;
    }
}
