package com.version2software.sparkplug.editor.view;

/**
 * Copyright (C) 2006 Version 2 Software, LLC. All rights reserved.
 * 
 * This software is published under the terms of the GNU Lesser Public License (LGPL),
 * a copy of which is included in this distribution.
 */

import java.awt.Component;
import java.awt.Dimension;

import javax.swing.Box;
import javax.swing.JEditorPane;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLFrameHyperlinkEvent;

import org.jivesoftware.spark.util.BrowserLauncher;
import org.jivesoftware.spark.util.log.Log;

import com.version2software.sparkplug.editor.EditorPlugin;

public class AboutDialog {
   public static void showDialog(Component parentComponent) {
      Box box = Box.createVerticalBox();
      
      JEditorPane editorPane = new JEditorPane();
      editorPane.setEditable(false);
      editorPane.setContentType("text/html");
      editorPane.setPreferredSize(new Dimension(350, 200));
      editorPane.addHyperlinkListener(new HyperlinkListener() {
         public void hyperlinkUpdate(HyperlinkEvent e) {
            if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
               JEditorPane pane = (JEditorPane) e.getSource();
               if (e instanceof HTMLFrameHyperlinkEvent) {
                  HTMLFrameHyperlinkEvent evt = (HTMLFrameHyperlinkEvent) e;
                  HTMLDocument doc = (HTMLDocument) pane.getDocument();
                  doc.processHTMLFrameHyperlinkEvent(evt);
               } else {
                  try {
                     BrowserLauncher.openURL(e.getURL().toString());
                  } catch (Throwable t) {
                     Log.error(t);
                  }
               }
            }
         }
      });
      
      String msg = "<html>Shared Editor Sparkplug<br>" +
         "Version: " + EditorPlugin.getVersion() +
         "<p>This sparkplug adds a simple editor to Spark that allows two people to work on a document at the same time. " +
         "This plugin demonstrates how the XMPP protocol can be utilized to send more than just instant messages." +
         "</p><br>" +
         "(c) Copyright Version 2 Software, LLC 2006. All rights reserved.<br>" +
         "Visit <a href=\"http://www.version2software.com/\">http://www.version2software.com/</a><br></html>";
      
      editorPane.setText(msg);
      
      JScrollPane scrollPane = new JScrollPane(editorPane);
      box.add(scrollPane);
      
      JOptionPane.showMessageDialog(parentComponent, box, "About", JOptionPane.INFORMATION_MESSAGE);
   }
}
