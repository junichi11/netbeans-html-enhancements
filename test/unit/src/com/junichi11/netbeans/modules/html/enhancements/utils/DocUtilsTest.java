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

import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Document;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.csl.api.OffsetRange;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author junichi11
 */
public class DocUtilsTest extends NbTestCase {

    public DocUtilsTest(String name) {
        super(name);
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    @Override
    public void setUp() {
    }

    @After
    @Override
    public void tearDown() {
    }

    public void testGetImgRange() throws Exception {
        Document doc = getDocument("testfiles/index.html");
        OffsetRange result = DocUtils.getImgRange(doc, 670);
        assertEquals(OffsetRange.NONE, result);

        // ^<
        result = DocUtils.getImgRange(doc, 764);
        assertEquals(OffsetRange.NONE, result);

        result = DocUtils.getImgRange(doc, 765);
        assertEquals(764, result.getStart());
        assertEquals(850, result.getEnd());

        result = DocUtils.getImgRange(doc, 772);
        assertEquals(764, result.getStart());
        assertEquals(850, result.getEnd());

        //              width="^642"
        result = DocUtils.getImgRange(doc, 817);
        assertEquals(764, result.getStart());
        assertEquals(850, result.getEnd());

        result = DocUtils.getImgRange(doc, 848);
        assertEquals(764, result.getStart());
        assertEquals(850, result.getEnd());

        result = DocUtils.getImgRange(doc, 849);
        assertEquals(764, result.getStart());
        assertEquals(850, result.getEnd());

        // >^
        result = DocUtils.getImgRange(doc, 850);
        assertEquals(OffsetRange.NONE, result);

        // ^<
        result = DocUtils.getImgRange(doc, 859);
        assertEquals(OffsetRange.NONE, result);

        result = DocUtils.getImgRange(doc, 860);
        assertEquals(859, result.getStart());
        assertEquals(923, result.getEnd());

        result = DocUtils.getImgRange(doc, 876);
        assertEquals(859, result.getStart());
        assertEquals(923, result.getEnd());

        result = DocUtils.getImgRange(doc, 922);
        assertEquals(859, result.getStart());
        assertEquals(923, result.getEnd());

        // >^
        result = DocUtils.getImgRange(doc, 923);
        assertEquals(OffsetRange.NONE, result);
    }

    public void testGetImgTag() throws Exception {
        Document doc = getDocument("testfiles/index.html");

        String result = DocUtils.getImgTag(doc, 670);
        assertNull(result);

        result = DocUtils.getImgTag(doc, 764);
        assertNull(result);

        String expResult = ""
                + "<img src=\"imgs/test1.png\" alt=\"\"\n"
                + "             width=\"642\"\n"
                + "             height=\"493\" />";
        result = DocUtils.getImgTag(doc, 765);
        assertEquals(expResult, result);

        result = DocUtils.getImgTag(doc, 817);
        assertEquals(expResult, result);

        result = DocUtils.getImgTag(doc, 849);
        assertEquals(expResult, result);

        result = DocUtils.getImgTag(doc, 850);
        assertNull(result);

        result = DocUtils.getImgTag(doc, 959);
        assertNull(result);

        expResult = "<img src=\"imgs/test2.png\" alt=\"test\" width=\"642\" height=\"493\" />";
        result = DocUtils.getImgTag(doc, 860);
        assertEquals(expResult, result);

        result = DocUtils.getImgTag(doc, 922);
        assertEquals(expResult, result);

        result = DocUtils.getImgTag(doc, 923);
        assertNull(result);
    }

    private FileObject getTestFile(String relativePath) {
        return FileUtil.toFileObject(getDataDir()).getFileObject(relativePath);
    }

    private Document getDocument(String relativePath) throws Exception {
        FileObject testFile = getTestFile(relativePath);
        Document doc = new DefaultStyledDocument();
        doc.insertString(0, testFile.asText(), null);
        return doc;
    }

}
