/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.swing;

import java.awt.BorderLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.core.config.ConfigSource;
import com.opengamma.core.config.impl.ConfigItem;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.master.config.ConfigMaster;

/**
 * Swing component with a text box at the top and a filtered list of views underneath
 */
public class ViewBrowserListComponent extends JComponent {

  private static final long serialVersionUID = 1L;
  
  private static final Logger s_logger = LoggerFactory.getLogger(ViewBrowserListComponent.class);
  private ConfigSource _configSource;
  private JList<ViewEntry> _viewList;
  private ViewListModel _viewListModel;
  private ConfigMaster _configMaster;
  private JTextField _viewNameTextField;

  public ViewBrowserListComponent(ConfigSource configSource, ConfigMaster configMaster) {
    _configSource = configSource;
    _configMaster = configMaster;
    _viewList = new JList<ViewEntry>();
    _viewListModel = new ViewListModel(_configMaster);
    _viewList.setModel(_viewListModel);
    _viewList.setCellRenderer(getViewListCellRenderer());
    _viewList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    
    _viewNameTextField = new JTextField();
    _viewNameTextField.setHorizontalAlignment(JTextField.LEFT);
    _viewNameTextField.addKeyListener(new KeyListener() {
      private void actionPerformed(KeyEvent e) {
        JTextField field = _viewNameTextField;
        _viewListModel.setFilter(field.getText());;
      }

      @Override
      public void keyTyped(KeyEvent e) {
        s_logger.warn("key code = {}", e.getKeyCode());
        actionPerformed(e);
      }

      @Override
      public void keyPressed(KeyEvent e) {
        s_logger.warn("key pressed = {}", e.getKeyCode());
        if (e.getKeyCode() == KeyEvent.VK_DOWN) {
          _viewList.requestFocusInWindow();
        }
      }

      @Override
      public void keyReleased(KeyEvent e) {
      }
    });
    _viewList.addListSelectionListener(new ListSelectionListener() {
      @Override
      public void valueChanged(ListSelectionEvent e) {
        @SuppressWarnings("unchecked")
        JList<ViewEntry> cb = (JList<ViewEntry>) e.getSource();
        final ViewEntry viewEntry = (ViewEntry) cb.getSelectedValue();
        if (viewEntry != null) {
          SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
              @SuppressWarnings("unchecked")
              ConfigItem<ViewDefinition> configItem = (ConfigItem<ViewDefinition>) _configSource.get(viewEntry.getUniqueId());
              if (configItem.getValue() != null) {
                _viewNameTextField.setText(viewEntry.getName());
                //_portfolioTree.setModel(getPortfolioTreeModel(configItem.getValue().getPortfolioId(), getToolContext()));
              } else {
                JOptionPane.showMessageDialog(null, "There is no portfolio set in the selected view", "No portfolio", JOptionPane.ERROR_MESSAGE);
              }
            }
          });
        }
      }
    });
    // add key listener to skip from the top of the list, to the text box.
    _viewList.addKeyListener(new KeyListener() {
      @Override
      public void keyTyped(KeyEvent e) {
      }
      @Override
      public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_UP && _viewList.getSelectedIndex() == 0) {
          _viewNameTextField.requestFocusInWindow();
        }
      }
      @Override
      public void keyReleased(KeyEvent e) {
      }
    });
    setLayout(new BorderLayout());
    JScrollPane scrollPane = new JScrollPane(_viewList);
    add(_viewNameTextField, BorderLayout.PAGE_START);    
    add(scrollPane, BorderLayout.CENTER);
  }
  
  private ListCellRenderer<? super ViewEntry> getViewListCellRenderer() {
    return new ViewListCellRenderer();
  }

  public ViewListModel getViewListModel() {
    return _viewListModel;
  }
  
  
}
