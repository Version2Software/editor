/**
 * Copyright (C) 2006 Version 2 Software, LLC. All rights reserved.
 * 
 * This software is published under the terms of the GNU Lesser Public License (LGPL),
 * a copy of which is included in this distribution.
 */

package com.version2software.sparkplug.editor.view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.StyledEditorKit;

import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.spark.SparkManager;
import org.jivesoftware.spark.util.log.Log;

import com.version2software.sparkplug.editor.EditorConstants;
import com.version2software.sparkplug.editor.extensions.Cursor;
import com.version2software.sparkplug.editor.extensions.Insert;
import com.version2software.sparkplug.editor.extensions.Remove;
import com.version2software.sparkplug.editor.extensions.Update;

//http://www.jabber.org/jeps/inbox/sxde.html
//http://javatechniques.com/public/java/docs/gui/jtextpane-speed-part1.html
public class Editor extends JFrame {
   private static int docCount = 1;
   private static File lastDir;
   
   private JMenuItem menuItemNew = new JMenuItem("New");
   private JMenuItem menuItemOpen = new JMenuItem("Open");
   private JMenuItem menuItemClose = new JMenuItem("Close");
   private JMenuItem menuItemSave = new JMenuItem("Save");
   private JMenuItem menuItemExit = new JMenuItem("Exit");

   private JMenuItem menuItemCut = new JMenuItem("Cut");
   private JMenuItem menuItemCopy = new JMenuItem("Copy");
   private JMenuItem menuItemPaste = new JMenuItem("Paste");
   private JMenuItem menuItemSelectAll = new JMenuItem("Select All");
   
   protected JCheckBoxMenuItem menuItemBold = new JCheckBoxMenuItem("Bold");
   protected JCheckBoxMenuItem menuItemItalic = new JCheckBoxMenuItem("Italic");
   protected JCheckBoxMenuItem menuItemUnderline = new JCheckBoxMenuItem("Underline");
   
   private JMenuItem menuItemAbout = new JMenuItem("About");
   
   private JTabbedPane tabbedPane = new JTabbedPane();
   private String participant;
   
   public static void main(String args[]) {
      new Editor("Standalone");
   }

   public Editor(String participant) {
      super("Editing with " + participant);
      
      this.participant = participant;
      
      initUI();
      initListeners();
      
      createNewTab(null);
   }
   
   private void initUI() {
      JMenu menuFile = new JMenu("File");
      menuFile.add(menuItemNew);
      menuFile.add(menuItemOpen);
      menuFile.addSeparator();
      menuFile.add(menuItemClose);
      menuFile.add(menuItemSave);
      menuFile.addSeparator();
      menuFile.add(menuItemExit);
      
      JMenu menuEdit = new JMenu("Edit");
      menuEdit.add(menuItemCut);
      menuEdit.add(menuItemCopy);
      menuEdit.add(menuItemPaste);
      menuEdit.addSeparator();
      menuEdit.add(menuItemSelectAll);
      
      JMenu menuFormat = new JMenu("Format");
      menuFormat.add(menuItemBold);
      menuFormat.add(menuItemItalic);
      menuFormat.add(menuItemUnderline);
      
      JMenu menuHelp = new JMenu("Help");
      menuHelp.add(menuItemAbout);

      JMenuBar menuBar = new JMenuBar();
      menuBar.add(menuFile);
      menuBar.add(menuEdit);
      menuBar.add(menuFormat);
      menuBar.add(menuHelp);
      
      setJMenuBar(menuBar);
      
      tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
      
      getContentPane().add(tabbedPane, BorderLayout.CENTER);
      getContentPane().add(new JPanel(), BorderLayout.SOUTH);
      
      int width = 450;
      int height = 420;
      Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
      setLocation(screenSize.width / 2 - width / 2, screenSize.height / 2 - height / 2);
      setSize(width, height);
      setVisible(true);
   }

   private void initListeners() {
      setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
      
      menuItemNew.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.CTRL_MASK));
      menuItemOpen.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK));
      menuItemClose.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W, ActionEvent.CTRL_MASK));
      menuItemSave.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
      menuItemExit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, ActionEvent.CTRL_MASK));
      menuItemCut.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, ActionEvent.CTRL_MASK));
      menuItemCopy.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.CTRL_MASK));
      menuItemPaste.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, ActionEvent.CTRL_MASK));
      menuItemSelectAll.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, ActionEvent.CTRL_MASK));

      menuItemNew.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent ae) {
            createNewTab(null);
         }
      });
      
      menuItemOpen.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent ae) {
            JFileChooser chooser = new JFileChooser(lastDir);
            chooser.setDialogTitle("Open");
            
            int selection = chooser.showOpenDialog(Editor.this);
            if (selection == JFileChooser.APPROVE_OPTION) {
               File file = chooser.getSelectedFile();
               lastDir = file.getParentFile();
               
               //if the file is already open, don't open it again
               for (int index = 0; index < tabbedPane.getTabCount(); index++) {
                   JScrollPane scrollPane = (JScrollPane) tabbedPane.getComponentAt(index);
                   EditorTextPane textPane = (EditorTextPane) scrollPane.getViewport().getView();
                   if (textPane.getFile() != null && textPane.getFile().getPath().equals(file.getPath())) {
                      tabbedPane.setSelectedIndex(index);
                      return;
                  }
               }
               
               EditorTextPane pane = createNewTab(file.getName());
               try {
                  FileInputStream fi = new FileInputStream(file);
                  pane.getEditorKit().read(fi, pane.getDocument(), 0);
                  pane.setFile(file);
               } catch (Exception e) {
                  Log.error(e);
                  JOptionPane.showMessageDialog(Editor.this, "Unable to open document", "Error", JOptionPane.ERROR_MESSAGE);
               }
            }
         }
      });
      
      menuItemClose.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent ae) {
            EditorTextPane tab = getSelectedTextPane();
            if (tab.getChanged()) {
               String msg = "Do you want to save this document before closing?";
               int selection = JOptionPane.showConfirmDialog(Editor.this, msg, "Replace?", JOptionPane.YES_NO_CANCEL_OPTION);
               
               switch (selection) {
               case JOptionPane.OK_OPTION:
                  saveTextPane();
                  closeSelectedTab();
                  break;
                  
               case JOptionPane.NO_OPTION:
                  closeSelectedTab();
                  break;

               default:
                  break;
               }
            } else {
               closeSelectedTab();
            }
         }
      });
      
      menuItemSave.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent ae) {
            //TODO send change of document name to remote user
            saveTextPane();
         }
      });

      menuItemExit.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent ae) {
            dispose();
         }
      });
      
      menuItemCut.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent ae) {
            getSelectedTextPane().cut();
         }
      });

      menuItemCopy.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent ae) {
            getSelectedTextPane().copy();
         }
      });

      menuItemPaste.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent ae) {
            getSelectedTextPane().paste();
         }
      });

      menuItemSelectAll.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent ae) {
            getSelectedTextPane().selectAll();
         }
      });
      
      menuItemBold.addActionListener(new StyledEditorKit.BoldAction());
      menuItemItalic.addActionListener(new StyledEditorKit.ItalicAction());
      menuItemUnderline.addActionListener(new StyledEditorKit.UnderlineAction());
      
      menuItemAbout.addActionListener(new AboutMenuActionListener());
   }
   
   private void closeSelectedTab() {
      int index = tabbedPane.getSelectedIndex();
      if (index != -1) {
         tabbedPane.remove(index);
      }
   }
   
   private void saveTextPane() {
      try {
         EditorTextPane textPane = getSelectedTextPane();
         File file = textPane.getFile();
         if (file == null) {
            JFileChooser chooser = new JFileChooser(lastDir);
            chooser.setDialogTitle("Save");
            if (chooser.showSaveDialog(Editor.this) == JFileChooser.APPROVE_OPTION) {
               
               file = chooser.getSelectedFile();
               if (file.exists()) {
                  lastDir = file.getParentFile();
                  String msg = "\"" +file.getName()+ "\" already exists. Do you want to replace it?";
                  int selection = JOptionPane.showConfirmDialog(Editor.this, msg, "Replace?", JOptionPane.OK_CANCEL_OPTION);
                  if (selection == JOptionPane.CANCEL_OPTION) {
                     return;
                  }
               }
               
               saveFile(file, textPane);
               textPane.setFile(file);
               textPane.setChanged(false);
               tabbedPane.setTitleAt(tabbedPane.getSelectedIndex(), file.getName());
            }
         } else {
            saveFile(file, textPane);
         }
      } catch (Exception e) {
         Log.error(e);
         JOptionPane.showMessageDialog(Editor.this, "Unable to save document", "Error", JOptionPane.ERROR_MESSAGE);
      }
   }
   
   private void saveFile(File file, EditorTextPane textPane) throws IOException, BadLocationException {
      OutputStream os = new BufferedOutputStream(new FileOutputStream(file));
      Document doc = textPane.getDocument();
      int length = doc.getLength();
      textPane.getEditorKit().write(os, doc, 0, length);
      os.close();
   }
   
   private EditorTextPane getSelectedTextPane() {
      JScrollPane scrollPane = (JScrollPane) tabbedPane.getSelectedComponent();
      EditorTextPane textPane = (EditorTextPane) scrollPane.getViewport().getView();
      return textPane;
   }
   
   /**
    * Returns the EditorTextPane based on the pageId from the JTabbedPane. If a EditorTextPane
    * does not already exist with the supplied pageId a new EditorTextPan will be created and 
    * added to the JTabbedPane.
    * 
    * @param pageId
    * @return the EditorTextPane based on the pagdId
    */
   private EditorTextPane getTargetedTextPane(String pageId) {
      int index = tabbedPane.indexOfTab(pageId);
      if (index == -1) {
         createNewTab(pageId);
         index = tabbedPane.indexOfTab(pageId);
      }
      
      JScrollPane scrollPane = (JScrollPane) tabbedPane.getComponentAt(index);
      EditorTextPane textPane = (EditorTextPane) scrollPane.getViewport().getView();
      return textPane;
   }

   private EditorTextPane createNewTab(String t) {
      String title = t == null ? "Untitled" + docCount++ : t;
      
      final EditorTextPane textPane = new EditorTextPane(this, title);
      tabbedPane.add(title, new JScrollPane(textPane, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED));
      tabbedPane.setSelectedIndex(tabbedPane.indexOfTab(title));
      
      SwingUtilities.invokeLater(new Runnable() {
         public void run() {
            textPane.setCaretPosition(textPane.getDocument().getLength());
         }
      });
      
      return textPane;
   }
   
   //TODO i'm not sure if I like this, maybe the packet should be passed to the Editor and it decides how to handle it?
   public void processPacket(Packet packet) {
      Cursor cursor = (Cursor) packet.getExtension(Cursor.ELEMENT_NAME, EditorConstants.NAMESPACE);
      if (cursor != null) {
         getTargetedTextPane(cursor.getPageId()).processCursor(cursor);
      }
      
      Insert insert = (Insert) packet.getExtension(Insert.ELEMENT_NAME, EditorConstants.NAMESPACE);
      if (insert != null) {
         getTargetedTextPane(insert.getPageId()).processInsert(insert);
      }
      
      Remove remove = (Remove) packet.getExtension(Remove.ELEMENT_NAME, EditorConstants.NAMESPACE);
      if (remove != null) {
         getTargetedTextPane(remove.getPageId()).processRemove(remove);
      }
      
      Update update = (Update) packet.getExtension(Update.ELEMENT_NAME, EditorConstants.NAMESPACE);
      if (update != null) {
         getTargetedTextPane(update.getPageId()).processUpdate(update);
      }
   }
   
   protected void sendPacketExtension(final PacketExtension extension) {
      if (participant.equals("Standalone")) {
         return;
      }
      SwingUtilities.invokeLater(new Runnable() {
         public void run() {
            new RunPacketExtensionThread(extension).start();
         }
      });
   }

   private class RunPacketExtensionThread extends Thread {
      private PacketExtension extension;

      public RunPacketExtensionThread(PacketExtension extension) {
         this.extension = extension;
      }

      public void run() {
         XMPPConnection con = SparkManager.getConnection();
         Message message = new Message();
         message.setTo(participant);
         message.setFrom(con.getUser());
         message.addExtension(extension);
         con.sendPacket(message);
      }
   }

   private class AboutMenuActionListener implements ActionListener {
      public void actionPerformed(ActionEvent ae) {
         AboutDialog.showDialog(Editor.this);
      }
   }
}
