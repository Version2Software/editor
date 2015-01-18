/**
 * Copyright (C) 2006 Version 2 Software, LLC. All rights reserved.
 * 
 * This software is published under the terms of the GNU Lesser Public License (LGPL),
 * a copy of which is included in this distribution.
 */

package com.version2software.sparkplug.editor.command;

import com.version2software.sparkplug.editor.extensions.Remove;

import junit.framework.TestCase;

public class TestRemove extends TestCase {

   public void testRemove() throws Exception {
      String expected ="<remove pageId=\"untitled\" offset=\"0\" length=\"1\"/>";

      Remove remove = new Remove("untitled", 0, 1);

      assertEquals(expected, remove.toXML());
      assertEquals("untitled", remove.getPageId());
      assertEquals("0", remove.getOffset());
      assertEquals("1", remove.getLength());
   }
}
