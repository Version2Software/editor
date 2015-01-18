/**
 * Copyright (C) 2006 Version 2 Software, LLC. All rights reserved.
 * 
 * This software is published under the terms of the GNU Lesser Public License (LGPL),
 * a copy of which is included in this distribution.
 */

package com.version2software.sparkplug.editor.command;

import com.version2software.sparkplug.editor.extensions.Insert;

import junit.framework.TestCase;

public class TestInsert extends TestCase {

   public void testInsert() throws Exception {
      String expected ="<insert pageId=\"untitled\" offset=\"0\">a</insert>";

      Insert insert = new Insert("untitled", 0, "a", null);

      assertEquals(expected, insert.toXML());
      assertEquals("untitled", insert.getPageId());
      assertEquals("0", insert.getOffset());
      assertEquals("a", insert.getText());
   }
}
