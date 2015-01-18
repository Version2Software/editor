/**
 * Copyright (C) 2006 Version 2 Software, LLC. All rights reserved.
 * 
 * This software is published under the terms of the GNU Lesser Public License (LGPL),
 * a copy of which is included in this distribution.
 */

package com.version2software.sparkplug.editor;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.io.File;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.filter.OrFilter;
import org.jivesoftware.smack.filter.PacketExtensionFilter;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smack.util.StringUtils;
import org.jivesoftware.spark.ChatManager;
import org.jivesoftware.spark.PluginManager;
import org.jivesoftware.spark.SparkManager;
import org.jivesoftware.spark.Workspace;
import org.jivesoftware.spark.plugin.ContextMenuListener;
import org.jivesoftware.spark.plugin.Plugin;
import org.jivesoftware.spark.ui.ChatRoom;
import org.jivesoftware.spark.ui.ChatRoomButton;
import org.jivesoftware.spark.ui.ChatRoomListenerAdapter;
import org.jivesoftware.spark.ui.ContactItem;
import org.jivesoftware.spark.ui.ContactList;
import org.jivesoftware.spark.ui.rooms.ChatRoomImpl;
import org.jivesoftware.spark.util.SwingWorker;
import org.jivesoftware.spark.util.log.Log;

import com.version2software.sparkplug.editor.extensions.Cursor;
import com.version2software.sparkplug.editor.extensions.Insert;
import com.version2software.sparkplug.editor.extensions.Remove;
import com.version2software.sparkplug.editor.extensions.Update;
import com.version2software.sparkplug.editor.view.Editor;

public class EditorPlugin implements Plugin {
   private Map<String, Editor> editorMap = new ConcurrentHashMap<String, Editor>();
   private EditorPacketListener listener = new EditorPacketListener();
   private static Workspace workspace;

   public void initialize() {
      workspace = SparkManager.getWorkspace();
      
      ProviderManager providerManager = ProviderManager.getInstance();
      providerManager.addExtensionProvider(Cursor.ELEMENT_NAME, EditorConstants.NAMESPACE, Cursor.class);
      providerManager.addExtensionProvider(Insert.ELEMENT_NAME, EditorConstants.NAMESPACE, Insert.class);
      providerManager.addExtensionProvider(Remove.ELEMENT_NAME, EditorConstants.NAMESPACE, Remove.class);
      providerManager.addExtensionProvider(Update.ELEMENT_NAME, EditorConstants.NAMESPACE, Update.class);
            
      OrFilter filter = new OrFilter();
      filter.addFilter(new PacketExtensionFilter(Cursor.ELEMENT_NAME, EditorConstants.NAMESPACE));
      filter.addFilter(new PacketExtensionFilter(Insert.ELEMENT_NAME, EditorConstants.NAMESPACE));
      filter.addFilter(new PacketExtensionFilter(Remove.ELEMENT_NAME, EditorConstants.NAMESPACE));
      filter.addFilter(new PacketExtensionFilter(Update.ELEMENT_NAME, EditorConstants.NAMESPACE));
      
      SparkManager.getConnection().addPacketListener(listener, filter);
      
      addChatRoomEditorButton();
      addContactListListener();
      
      SwingUtilities.invokeLater(new Runnable() {
         public void run() {
            new CheckForNewVersion().start();
         }
      });
   }

   public void shutdown() {
      ProviderManager providerManager = ProviderManager.getInstance();
      providerManager.removeExtensionProvider(Cursor.ELEMENT_NAME, EditorConstants.NAMESPACE);
      providerManager.removeExtensionProvider(Insert.ELEMENT_NAME, EditorConstants.NAMESPACE);
      providerManager.removeExtensionProvider(Remove.ELEMENT_NAME, EditorConstants.NAMESPACE);
      providerManager.removeExtensionProvider(Update.ELEMENT_NAME, EditorConstants.NAMESPACE);
      
      XMPPConnection connection = SparkManager.getConnection();
      if (connection.isConnected()) {
         connection.removePacketListener(listener);
      }
      
      workspace = null;
      listener = null;
      editorMap = null;
   }

   public boolean canShutDown() {
      return true;
   }

   public void uninstall() {
   }
   
   /**
    * Reads the plugin.xml file and returns the value of <version> element or null if the file cannot be found/read.
    * 
    * @return the version of the currently installed Editor
    */
   public static String getVersion() {
      String version = null;
      
      try {
         File pluginXML;
         
         if (workspace == null) { //running in "standalone" mode
            pluginXML = new File("plugin.xml").getAbsoluteFile();
         } else {
            pluginXML = new File(PluginManager.PLUGINS_DIRECTORY, "editor/plugin.xml").getAbsoluteFile();
         }
         
         SAXBuilder builder = new SAXBuilder(); 
         Document doc = builder.build(pluginXML);
         Element root = doc.getRootElement(); 
         version = root.getChild("version").getText();
      } catch (Exception e) {
         Log.error("Unable to get EditorPlugin version", e);
      }
      
      return version;
   }

   private void startWorker(final String jid) {
      SwingWorker worker = new SwingWorker() {
         public Object construct() {
            return doWork(jid);
         }

         public void finished() {
         }
      };
      worker.start();
   }

   private Object doWork(final String roomname) {
      String from = StringUtils.parseBareAddress(roomname);
      
      Editor frame;
      if (editorMap.containsKey(from)) {
         frame = editorMap.get(from);
      } else {
         frame = new Editor(from);
         editorMap.put(from, frame);
      }
      
      frame.setVisible(true);

      return this;
   }
   
   private class EditorPacketListener implements PacketListener {
      public void processPacket(Packet packet) {
         String from = StringUtils.parseBareAddress(packet.getFrom());
         
         Editor editor = null;
         if (editorMap.containsKey(from)) {
            editor = editorMap.get(from);
         } else {
            editor = new Editor(from);
            editorMap.put(from, editor);
         }
         
         editor.processPacket(packet);
         
         if (!editor.isVisible()) {
            editor.setVisible(true);
         }
      }
   }

   private void addChatRoomEditorButton() {
      ChatManager chatManager = SparkManager.getChatManager();

      chatManager.addChatRoomListener(new ChatRoomListenerAdapter() {
         public void chatRoomOpened(final ChatRoom room) {
            if (room instanceof ChatRoomImpl) {
               ChatRoomButton button = new ChatRoomButton(getImageIcon("stock_copy.png"));
               button.setToolTipText("Share an editor");

               room.getToolBar().addChatRoomButton(button);
               button.addActionListener(new ActionListener() {
                  public void actionPerformed(ActionEvent e) {
                     startWorker(room.getRoomname());
                  }
               });
            }
         }

         public void chatRoomLeft(ChatRoom room) {
         }
      });
   }

   private void addContactListListener() {
      final ContactList contactList = workspace.getContactList();

      final Action resourceManagerAction = new AbstractAction() {
         public void actionPerformed(ActionEvent actionEvent) {
            Iterator contactListIter = contactList.getSelectedUsers().iterator();
            if (contactListIter.hasNext()) {
               ContactItem contactItem = (ContactItem) contactListIter.next();
               startWorker(contactItem.getJID());
            }
         }
      };

      resourceManagerAction.putValue(Action.NAME, "Shared Editor");
      resourceManagerAction.putValue(Action.SMALL_ICON, getImageIcon("stock_copy-16.png"));

      contactList.addContextMenuListener(new ContextMenuListener() {
         public void poppingUp(Object object, JPopupMenu popupMenu) {
            if (object instanceof ContactItem) {
               popupMenu.add(resourceManagerAction);
            }
         }

         public void poppingDown(JPopupMenu popup) {
            //not used
         }

         public boolean handleDefaultAction(MouseEvent e) {
            return false;
         }
      });
   }
   
   private ImageIcon getImageIcon(String icon) {
      return new ImageIcon(EditorPlugin.class.getResource(icon));
   }
   
   private class CheckForNewVersion extends Thread {
      public void run() {
         try {
            SAXBuilder builder = new SAXBuilder(); 
            Document doc = builder.build(new URL("http://www.version2software.com/downloads/editor/version.xml"));
            Element root = doc.getRootElement(); 
            String currentVersion = root.getChild("currentVersion").getText();
            String pluginVersion = getVersion();
            
            if (pluginVersion != null && pluginVersion.compareTo(currentVersion) < 0) {
               String msg = "<html>A new version of Shared Editor is available. You can download it from<br>" +
                     "<a href=\"http://www.version2software.com/software.html\">http://www.version2software.com/software.html</a></html>";
               JOptionPane.showMessageDialog(SparkManager.getFocusedComponent(), msg, "New Version", JOptionPane.INFORMATION_MESSAGE);
            }
         }
         catch (Exception e) {
            Log.error("Unable to get latest SharedEditorPlugin version", e);
         }
      }
   }
}
