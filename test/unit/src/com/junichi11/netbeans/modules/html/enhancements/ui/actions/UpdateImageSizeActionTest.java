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

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author junichi11
 */
public class UpdateImageSizeActionTest extends NbTestCase {

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    public UpdateImageSizeActionTest(String name) {
        super(name);
    }

    @Before
    @Override
    public void setUp() {
    }

    @After
    @Override
    public void tearDown() {
    }

    public void testUpdateImgTag() {
        String target = "<img src=\"\" />";
        int width = 100;
        int height = 200;
        String expResult = "<img src=\"\" width=\"100\" height=\"200\" />";
        String result = UpdateImageSizeAction.updateImgTag(target, width, height);
        assertEquals(expResult, result);

        target = "<img src=\"test.png\"/>";
        width = 100;
        height = 200;
        expResult = "<img src=\"test.png\" width=\"100\" height=\"200\" />";
        result = UpdateImageSizeAction.updateImgTag(target, width, height);
        assertEquals(expResult, result);

        target = "<img src=\"test.png\" alt=\"test\"/>";
        width = 100;
        height = 200;
        expResult = "<img src=\"test.png\" alt=\"test\" width=\"100\" height=\"200\" />";
        result = UpdateImageSizeAction.updateImgTag(target, width, height);
        assertEquals(expResult, result);

        target = "<img src=\"test.png\" alt=\"test\" width=\"2\" />";
        width = 100;
        height = 200;
        expResult = "<img src=\"test.png\" alt=\"test\" width=\"100\" height=\"200\" />";
        result = UpdateImageSizeAction.updateImgTag(target, width, height);
        assertEquals(expResult, result);

        target = "<img src=\"test.png\" alt=\"test\" height=\"2\" />";
        width = 100;
        height = 200;
        expResult = "<img src=\"test.png\" alt=\"test\" height=\"200\" width=\"100\" />";
        result = UpdateImageSizeAction.updateImgTag(target, width, height);
        assertEquals(expResult, result);

        target = "<img src=\"test.png\" alt=\"test\" width=\"2\" height=\"3\"/>";
        width = 100;
        height = 200;
        expResult = "<img src=\"test.png\" alt=\"test\" width=\"100\" height=\"200\" />";
        result = UpdateImageSizeAction.updateImgTag(target, width, height);
        assertEquals(expResult, result);

        target = "<img src=\"test.png\" alt=\"test\" width=\"\" height=\"\"/>";
        width = 100;
        height = 200;
        expResult = "<img src=\"test.png\" alt=\"test\" width=\"100\" height=\"200\" />";
        result = UpdateImageSizeAction.updateImgTag(target, width, height);
        assertEquals(expResult, result);
    }

}
