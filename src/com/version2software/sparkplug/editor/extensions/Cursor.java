/**
 * Copyright (C) 2006 Version 2 Software, LLC. All rights reserved.
 * 
 * This software is published under the terms of the GNU Lesser Public License (LGPL),
 * a copy of which is included in this distribution.
 */

package com.version2software.sparkplug.editor.extensions;

import org.jivesoftware.smack.packet.PacketExtension;

import com.version2software.sparkplug.editor.EditorConstants;

public class Cursor implements PacketExtension   {
   public static final String ELEMENT_NAME = "editor-cursor";
   
   private String pageId;
   private int dot;
   private int mark;
   
   public Cursor() {
      super();
   }
   
   public Cursor(String pageId, int dot, int mark) {
      this.pageId = pageId;
      this.dot = dot;
      this.mark = mark;
   }
   
   public String getPageId() {
      return pageId;
   }
   
   public void setPageId(String pageId) {
      this.pageId = pageId;
   }

   /**
    * Fetches the location of the caret. 
    * @return
    */
   public int getDot() {
      return dot;
   }
   
   public void setDot(int dot) {
      this.dot = dot;
   }

   /**
    * Fetches the location of other end of a logical selection. If there is no selection, this will be the same as dot. 
    * @return
    */
   public int getMark() {
      return mark;
   }
   
   public void setMark(int mark) {
      this.mark = mark;
   }

   public String getElementName() {
      return ELEMENT_NAME;
   }

   public String getNamespace() {
      return EditorConstants.NAMESPACE;
   }

   public String toXML() {
      StringBuffer sb = new StringBuffer();
      sb.append("<" + ELEMENT_NAME + " xmlns=\"" + EditorConstants.NAMESPACE + "\">");
      sb.append("<pageId>").append(pageId).append("</pageId>");
      sb.append("<dot>").append(dot).append("</dot>");
      sb.append("<mark>").append(mark).append("</mark>");
      sb.append("</" + ELEMENT_NAME + ">");
      return sb.toString();
  }
}
