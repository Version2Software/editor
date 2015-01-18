/**
 * Copyright (C) 2006 Version 2 Software, LLC. All rights reserved.
 * 
 * This software is published under the terms of the GNU Lesser Public License (LGPL),
 * a copy of which is included in this distribution.
 */

package com.version2software.sparkplug.editor.extensions;

import org.jivesoftware.smack.packet.PacketExtension;

import com.version2software.sparkplug.editor.EditorConstants;

public class Update implements PacketExtension  {
   public static final String ELEMENT_NAME = "editor-update";
   
   private String pageId;
   private int offset;
   private int length;
   private String style;

   public Update() {
      super();
   }
   
   public Update(String pageId, int offset, int length, String style) {
      this.pageId = pageId;
      this.offset = offset;
      this.length = length;
      this.style = style;
   }
   
   public String getPageId() {
      return pageId;
   }
   
   public void setPageId(String pageId) {
      this.pageId = pageId;
   }

   public int getOffset() {
      return offset;
   }
   
   public void setOffset(int offset) {
      this.offset = offset;
   }
   
   public int getLength() {
      return length;
   }
   
   public void setLength(int length) {
      this.length = length;
   }
   
   public String getStyle() {
      return style;
   }
   
   public void setStyle(String style) {
      this.style = style;
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
      sb.append("<offset>").append(offset).append("</offset>");
      sb.append("<length>").append(length).append("</length>");
      sb.append("<style>").append(style).append("</style>");
      sb.append("</" + ELEMENT_NAME + ">");
      return sb.toString();
   }
}
