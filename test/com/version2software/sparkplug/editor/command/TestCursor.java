/**
 * Copyright (C) 2006 Version 2 Software, LLC. All rights reserved.
 * 
 * This software is published under the terms of the GNU Lesser Public License (LGPL),
 * a copy of which is included in this distribution.
 */

package com.version2software.sparkplug.editor.command;

import com.version2software.sparkplug.editor.extensions.Cursor;

import junit.framework.TestCase;

public class TestCursor extends TestCase {

   public void testRemove() throws Exception {
      String expected ="<cursor pageId=\"untitled\" dot=\"1\" mark=\"0\"/>";

      Cursor cursor = new Cursor("untitled", 1, 0);

      assertEquals(expected, cursor.toXML());
      assertEquals("untitled", cursor.getPageId());
      assertEquals("1", cursor.getDot());
      assertEquals("0", cursor.getMark());
   }
}
