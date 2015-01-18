/**
 * Copyright (C) 2006 Version 2 Software, LLC. All rights reserved.
 * 
 * This software is published under the terms of the GNU Lesser Public License (LGPL),
 * a copy of which is included in this distribution.
 */

package com.version2software.sparkplug.editor.view;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledEditorKit;

import org.jivesoftware.spark.util.log.Log;

import com.version2software.sparkplug.editor.extensions.Cursor;
import com.version2software.sparkplug.editor.extensions.Insert;
import com.version2software.sparkplug.editor.extensions.Remove;
import com.version2software.sparkplug.editor.extensions.Update;

public class EditorTextPane extends JTextPane {
   private JPopupMenu popupMenu = new JPopupMenu();
   private JMenuItem cutPopupMenuItem = new JMenuItem("Cut");
   private JMenuItem copyPopupMenuItem = new JMenuItem("Copy");
   private JMenuItem pastePopupMenuItem = new JMenuItem("Paste");
   private JMenuItem selectPopupAllMenuItem = new JMenuItem("Select All");
   
   private StyledEditorKit styledEditorKit = new StyledEditorKit();
   
   private Editor editor;
   private String name;
   private File file;
   private boolean changed = false;
   private boolean altering = false;
   
   public EditorTextPane(Editor editor, String name) {
      this.editor = editor;
      this.name = name;
      
      setContentType("text/rtf");
      styledEditorKit.install(this);
      initUI();
      initListeners();
   }
   
   private void initUI() {
      popupMenu.add(cutPopupMenuItem);
      popupMenu.add(copyPopupMenuItem);
      popupMenu.add(pastePopupMenuItem);
      popupMenu.addSeparator();
      popupMenu.add(selectPopupAllMenuItem);
   }
   
   private void initListeners() {
      getDocument().addDocumentListener(new EditorDocumentListener());
      addCaretListener(new EditorCaretListener());
      addCaretListener(new CurrentLineHighlighter());

      cutPopupMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, ActionEvent.CTRL_MASK));
      copyPopupMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.CTRL_MASK));
      pastePopupMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, ActionEvent.CTRL_MASK));
      selectPopupAllMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, ActionEvent.CTRL_MASK));

      cutPopupMenuItem.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            cut();
         }
      });

      copyPopupMenuItem.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            copy();
         }
      });

      pastePopupMenuItem.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            paste();
         }
      });

      selectPopupAllMenuItem.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            selectAll();
         }
      });

      addMouseListener(new MouseAdapter() {
         public void mousePressed(MouseEvent e) {
            checkForTriggerEvent(e);
         }

         public void mouseReleased(MouseEvent e) {
            checkForTriggerEvent(e);
         }

         private void checkForTriggerEvent(MouseEvent e) {
            if (e.isPopupTrigger()) {
               popupMenu.show(e.getComponent(), e.getX(), e.getY());
            }
         }
      });
   }
   
   /**
    * Sets the file that is to be associated with the EditorTextPane.
    * 
    * @param file
    */
   public void setFile(File file) {
      this.file = file;
   }
   
   /**
    * @return the file that is associated with the EditorTextPane.
    */
   public File getFile() {
      return file;
   }
   
   /**
    * @return true if the underling document has been updated.
    */
   public boolean getChanged() {
      return changed;
   }
   
   /**
    * Sets the changed value of the underling document.
    * 
    * @param changed
    */
   public void setChanged(boolean changed) {
      this.changed = changed;
   }
   
   /**
    * Updates the placement of the cursorin an existing document or creates a new one using 
    * the elements pageIdas a key.
    * 
    * @param element
    */
   public void processCursor(Cursor element) {
      altering = true;
      try {
         int mark = element.getMark();
         int dot = element.getDot();

         setCaretPosition(dot);

         if (dot < mark) {
            setSelectionStart(dot);
            setSelectionEnd(mark);
         } else if (dot > mark) {
            setSelectionStart(mark);
            setSelectionEnd(dot);
         } else {
            setSelectionStart(dot);
            setSelectionEnd(dot);
         }

      } catch (Exception e) {
         Log.error("processCursor : " + element.toString(), e);
      }
      altering = false;
   }
   
   /**
    * Inserts a piece of text in an existing document  or creates a new one using the elements
    * pageIdas a key.
    * 
    * @param element
    */
   public void processInsert(Insert element) {
      altering = true;
      try {
         SimpleAttributeSet set = new SimpleAttributeSet();
         String style = element.getStyle();
         if (style.contains("italic")) {
            set.addAttribute(StyleConstants.Italic, Boolean.TRUE);
         }
         
         if (style.contains("bold")) {
            set.addAttribute(StyleConstants.Bold, Boolean.TRUE);
         }
         
         if (style.contains("underline")) {
            set.addAttribute(StyleConstants.Underline, Boolean.TRUE);
         }
         
         getDocument().insertString(element.getOffset(), element.getText(), set);
         
         changed = true;
      } catch (Exception e) {
         Log.error("processInsert : " + element.toString(), e);
      }
      altering = false;
   }
   
   /**
    * Removes a piece of text in an existing document.
    * 
    * @param element
    */
   public void processRemove(Remove element) {
      altering = true;
      try {
         getDocument().remove(element.getOffset(), element.getLength());
         changed = true;
      } catch (Exception e) {
         Log.error("processRemove : " + element.toString(), e);
      }
      altering = false;
   }
   
   /**
    * Updates the style (italic, bold, underline) of an piece of text in an existing document 
    * or creates a new one using the elements pageIdas a key.
    * 
    * There's no "replace" or "update" method for Document so during an update the text to changed
    * is copied, removed and then replaced.
    * 
    * @param element
    */
   public void processUpdate(Update element) {
      altering = true;
      try {
         //there's no "replace" or "update" method for Document so we need to 
         //copy the existing text, remove it and then replace it.
         Document doc = getDocument();
         
         String text = doc.getText(element.getOffset(), element.getLength());
         doc.remove(element.getOffset(), element.getLength());
         
         SimpleAttributeSet set = new SimpleAttributeSet();
         String style = element.getStyle();
         if (style.contains("italic")) {
            set.addAttribute(StyleConstants.Italic, Boolean.TRUE);
         }
         
         if (style.contains("bold")) {
            set.addAttribute(StyleConstants.Bold, Boolean.TRUE);
         }
         
         if (style.contains("underline")) {
            set.addAttribute(StyleConstants.Underline, Boolean.TRUE);
         }
         
         doc.insertString(element.getOffset(), text, set);
         
         changed = true;
      } catch (Exception e) {
         Log.error("processUpdate : " + element.toString(), e);
      }
      altering = false;
   }
   
   private String getID() {
      return file == null ? name : file.getName();
   }
   
   private class EditorDocumentListener implements DocumentListener {
      public void insertUpdate(DocumentEvent evt) {
         try {
            if (!altering) {
               int offset = evt.getOffset();
               int length = evt.getLength();
               
               String str = evt.getDocument().getText(offset, length);
               
               DefaultStyledDocument doc = (DefaultStyledDocument) evt.getDocument();
               Element element = doc.getCharacterElement(offset);

               AttributeSet set = element.getAttributes();
               
               StringBuilder styles = new StringBuilder();
               if (StyleConstants.isItalic(set)) {
                  styles.append("italic");
               }
               
               if (StyleConstants.isBold(set)) {
                  if (styles.length() > 0) {
                     styles.append(",");
                  }
                  
                  styles.append("bold");
               }
               
               if (StyleConstants.isUnderline(set)) {
                  if (styles.length() > 0) {
                     styles.append(",");
                  }
                  
                  styles.append("underline");
               }
               
               Insert insert = new Insert(getID(), offset, str, styles.toString());
               editor.sendPacketExtension(insert);
               changed = true;
            }
         } catch (BadLocationException e) {
            Log.error(e);
         }
      }
  
      public void removeUpdate(DocumentEvent evt) {
         if (!altering) {
            int offset = evt.getOffset();
            int length = evt.getLength();
 
            Remove remove = new Remove(getID(), offset, length);
            editor.sendPacketExtension(remove);
            changed = true;
         }
      }

      public void changedUpdate(DocumentEvent evt) {
         changed = true;
         
         int offset = evt.getOffset();
         int length = evt.getLength();

         DefaultStyledDocument doc = (DefaultStyledDocument) evt.getDocument();
         Element element = doc.getCharacterElement(offset);

         AttributeSet set = element.getAttributes();
         
         StringBuilder styles = new StringBuilder();
         if (StyleConstants.isItalic(set)) {
            styles.append(StyleConstants.Italic);
         }
         
         if (StyleConstants.isBold(set)) {
            if (styles.length() > 0) {
               styles.append(",");
            }
            
            styles.append(StyleConstants.Bold);
         }
         
         if (StyleConstants.isUnderline(set)) {
            if (styles.length() > 0) {
               styles.append(",");
            }
            
            styles.append(StyleConstants.Underline);
         }
         
         Update insert = new Update(getID(), offset, length, styles.toString());
         editor.sendPacketExtension(insert);
         changed = true;
      }
   }
   
   private class EditorCaretListener implements CaretListener {
      public void caretUpdate(CaretEvent e) {
         if (!altering) {
            int dot = e.getDot();
            int mark = e.getMark();
            
            MutableAttributeSet set = styledEditorKit.getInputAttributes();
            editor.menuItemItalic.setSelected(StyleConstants.isItalic(set));
            editor.menuItemBold.setSelected(StyleConstants.isBold(set));
            editor.menuItemUnderline.setSelected(StyleConstants.isUnderline(set));
            
            Cursor cursor = new Cursor(getID(), dot, mark);
            editor.sendPacketExtension(cursor);
         }
      }
   }
}
