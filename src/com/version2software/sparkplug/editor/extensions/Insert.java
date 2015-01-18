/**
 * Copyright (C) 2006 Version 2 Software, LLC. All rights reserved.
 * 
 * This software is published under the terms of the GNU Lesser Public License (LGPL),
 * a copy of which is included in this distribution.
 */

package com.version2software.sparkplug.editor.extensions;

import org.jivesoftware.smack.packet.PacketExtension;

import com.version2software.sparkplug.editor.EditorConstants;

public class Insert implements PacketExtension  {
   public static final String ELEMENT_NAME = "editor-insert";
   
   private String pageId;
   private int offset;
   private String text;
   private String style;
   
   public Insert() {
      super();
   }

   public Insert(String pageId, int offset, String text, String style) {
      this.pageId = pageId;
      this.offset = offset;
      this.text = text;
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

   public String getText() {
      return text;
   }
   
   public void setText(String text) {
      this.text = text;
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
      sb.append("<text>").append(text).append("</text>");
      sb.append("<style>").append(style).append("</style>");
      sb.append("</" + ELEMENT_NAME + ">");
      return sb.toString();
   }
}
