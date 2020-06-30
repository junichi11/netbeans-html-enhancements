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
package com.junichi11.netbeans.modules.html.enhancements.editor;

import java.awt.Image;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.modules.parsing.api.Source;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author junichi11
 */
public class EditorSupport {

    private static final Logger LOGGER = Logger.getLogger(EditorSupport.class.getName());

    /**
     * Get img tag range.
     *
     * @param doc Document
     * @param offset caret position
     * @return range array if exists img tag, otherwise null
     * @throws BadLocationException
     */
    public static int[] getImgRange(Document doc, int offset) throws BadLocationException {
        int start = offset;
        int end = offset;

        // start position
        while (start - 1 > 0) {
            if (doc.getText(start, offset - start).startsWith("<img")) { // NOI18N
                break;
            }
            String text = doc.getText(start - 1, 1);
            if (text.equals("\n") || text.equals(">")) { // NOI18N
                return null;
            }
            start--;
        }

        // end position
        int last = doc.getLength();
        while (end <= last) {
            if (doc.getText(offset, end - offset).endsWith("/>")) { // NOI18N
                break;
            }
            String text = doc.getText(end, 1);
            if (text.equals("\n") || end == last) { // NOI18N
                return null;
            }
            end++;
        }

        return new int[]{start, end};
    }

    /**
     * Get current line range
     *
     * @param doc Document
     * @param offset caret positon
     * @return line range array
     * @throws BadLocationException
     */
    public static int[] getLineRange(Document doc, int offset) throws BadLocationException {
        int lineStart = offset;
        int lineEnd = offset;

        // start position
        while (lineStart - 1 > 0) {
            String text = doc.getText(lineStart - 1, 1);
            if (text.equals("\n")) { // NOI18N
                break;
            }
            lineStart--;
        }

        // end position
        int last = doc.getLength();
        while (lineEnd <= last) {
            String text = doc.getText(lineEnd + 1, 1);
            if (text.equals("\n") || lineEnd == last) { // NOI18N
                break;
            }
            lineEnd++;
        }

        return new int[]{lineStart, lineEnd};
    }

    /**
     * Get text as line
     *
     * @param doc Document
     * @param offset caret position
     * @return text
     * @throws BadLocationException
     */
    public static String getTextAsLine(Document doc, int offset) throws BadLocationException {
        int[] range = getLineRange(doc, offset);
        if (range == null || range.length != 2) {
            return null;
        }

        return doc.getText(range[0], range[1] - range[0]);
    }

    /**
     * Get img tag text
     *
     * @param doc Document
     * @param offset caret position
     * @return img tag text
     * @throws BadLocationException
     */
    public static String getImgTag(Document doc, int offset) throws BadLocationException {
        int[] range = getImgRange(doc, offset);
        if (range == null || range.length != 2) {
            return null;
        }

        return doc.getText(range[0], range[1] - range[0]);
    }

    /**
     * Get FileObject from Document
     *
     * @param doc Document
     * @return FileObject
     */
    public static FileObject getFileObject(Document doc) {
        Source source = Source.create(doc);
        return source.getFileObject();
    }

    /**
     * Convert to path for FileObject
     *
     * @param path
     * @return relative path
     */
    public static String normalizePath(String path) {
        if (path == null || path.isEmpty()) {
            return null;
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
                return null;
            } catch (IOException ex) {
                LOGGER.log(Level.WARNING, null, ex);
                return null;
            }
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
            return null;
        }
    }
}
