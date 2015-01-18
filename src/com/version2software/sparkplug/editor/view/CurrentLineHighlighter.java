/**
 * Copyright (C) 2006 Version 2 Software, LLC. All rights reserved.
 * 
 * This software is published under the terms of the GNU Lesser Public License (LGPL),
 * a copy of which is included in this distribution.
 */

package com.version2software.sparkplug.editor.view;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Shape;

import javax.swing.SwingUtilities;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.Highlighter;
import javax.swing.text.JTextComponent;
import javax.swing.text.Utilities;

import org.jivesoftware.spark.util.log.Log;

public class CurrentLineHighlighter implements CaretListener, Highlighter.HighlightPainter {
   private static final Color DEFAULT_COLOR = new Color(231, 244, 244);
   private Object highlight;

   public void caretUpdate(CaretEvent evt) {
      final JTextComponent component = (JTextComponent) evt.getSource();
      
      SwingUtilities.invokeLater(new Runnable() {
         public void run() {
            if (component != null && highlight != null) {
               component.getHighlighter().removeHighlight(highlight);
               component.repaint();
               highlight = null;
            }
      
            int pos = component.getCaretPosition();
            Element elem = Utilities.getParagraphElement(component, pos);
            int start = elem.getStartOffset();
            int end = elem.getEndOffset();
            try {
               highlight = component.getHighlighter().addHighlight(start, end, CurrentLineHighlighter.this);
            } catch (BadLocationException ble) {
               Log.error(ble);
            }
         }
      });
   }

   public void paint(Graphics g, int p0, int p1, Shape bounds, JTextComponent c) {
      try {
         Rectangle r = c.modelToView(c.getCaretPosition());
         g.setColor(DEFAULT_COLOR);
         g.fillRect(0, r.y, c.getWidth(), r.height);

      } catch (BadLocationException ble) {
         Log.error(ble);
      }
   }
}