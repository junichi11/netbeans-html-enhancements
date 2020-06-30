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
package com.junichi11.netbeans.modules.html.enhancements.ui.actions;

import com.junichi11.netbeans.modules.html.enhancements.editor.EditorSupport;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.JTextComponent;
import javax.swing.text.StyledDocument;
import org.netbeans.api.editor.EditorRegistry;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.cookies.EditorCookie;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle.Messages;

@ActionID(
        category = "XML",
        id = "com.junichi11.netbeans.html.enhancement.ui.actions.UpdateImageSizeAction")
@ActionRegistration(
        displayName = "#CTL_UpdateImageSizeAction")
@ActionReference(path = "Shortcuts", name = "AD-U")
@Messages("CTL_UpdateImageSizeAction=Update Image Size")
public final class UpdateImageSizeAction implements ActionListener {

    private final EditorCookie context;

    public UpdateImageSizeAction(EditorCookie context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        StyledDocument document = context.getDocument();
        JTextComponent editor = EditorRegistry.focusedComponent();
        Caret caret = editor.getCaret();
        int offset = caret.getDot();
        try {
            // get src path
            String imgTag = EditorSupport.getImgTag(document, offset);
            if (imgTag == null || imgTag.isEmpty()) {
                return;
            }
            Pattern pattern = Pattern.compile("<img.*?src=([\"\\'])(.+?)\\1.*?>"); // NOI18N
            Matcher matcher = pattern.matcher(imgTag);
            String src = "";
            if (matcher.find()) {
                src = matcher.group(2);
            }
            if (src.isEmpty()) {
                return;
            }
            Image image = EditorSupport.getImage(src, document);

            if (image == null) {
                return;
            }
            String update = updateImgTag(imgTag, image.getWidth(null), image.getHeight(null));
            int[] imgRange = EditorSupport.getImgRange(document, offset);

            // remove
            document.remove(imgRange[0], imgTag.length());

            // insert
            document.insertString(imgRange[0], update, null);
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
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
