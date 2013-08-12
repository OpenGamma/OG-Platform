/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.viewdefinitioneditor;

import java.awt.EventQueue;

import javax.swing.Action;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.SpringLayout;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagLayout;

import javax.swing.JScrollPane;

import java.awt.GridBagConstraints;

import javax.swing.JTree;

import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowStateListener;
import java.beans.PropertyChangeListener;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.SynchronousQueue;

import javax.swing.JTextPane;

import com.opengamma.component.tool.AbstractTool;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.core.config.impl.ConfigItem;
import com.opengamma.core.position.Portfolio;
import com.opengamma.core.position.PortfolioNode;
import com.opengamma.core.position.Position;
import com.opengamma.core.position.PositionSource;
import com.opengamma.core.position.Trade;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.financial.depgraph.rest.DependencyGraphBuildTrace;
import com.opengamma.financial.depgraph.rest.DependencyGraphTraceBuilderProperties;
import com.opengamma.financial.tool.ToolContext;
import com.opengamma.id.UniqueId;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.integration.swing.PortfolioTreeModel;
import com.opengamma.integration.swing.ViewEntry;
import com.opengamma.integration.swing.ViewListCellRenderer;
import com.opengamma.integration.swing.ViewListModel;
import com.opengamma.integration.tool.IntegrationToolContext;
import com.opengamma.scripts.Scriptable;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import org.jdesktop.swingx.JXTreeTable;
import org.jdesktop.swingx.treetable.DefaultTreeTableModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.JSplitPane;

/**
 * Debugging tool for engine functions.
 */
@Scriptable
public class ViewDefinitionEditor extends AbstractTool<IntegrationToolContext> {
  
  private static final Logger s_logger = LoggerFactory.getLogger(ViewDefinitionEditor.class);

  private JFrame _frame;

  private JTextField _viewNameTextField;

  private ViewListModel _viewListModel;

  /**
   * Launch the application.
   */
  public static void main(String[] args) {
    //new EngineDebugger().initialize();
    new ViewDefinitionEditor().initAndRun(args, IntegrationToolContext.class);
  }

  /**
   * Initialize the contents of the frame.
   * @wbp.parser.entryPoint
   */
  private void initialize() {
    _frame = new JFrame();
    _frame.setTitle("View Definition Editor");
    _frame.setPreferredSize(new Dimension(1000, 700));
    _frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    
    JPanel mainPanel = new JPanel();
    _frame.getContentPane().add(mainPanel, BorderLayout.CENTER);
    mainPanel.setLayout(new BorderLayout());
    
    _viewList = new JList<ViewEntry>();
    _viewListModel = getViewListModel();
    _viewList.setModel(_viewListModel);
    _viewList.setCellRenderer(getViewListCellRenderer());
    _viewList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

    final ConfigSource configSource = getToolContext().getConfigSource();
    final PositionSource positionSource = getToolContext().getPositionSource();
    final SecuritySource securitySource = getToolContext().getSecuritySource();
    
    
    JPanel panel = new JPanel();
    mainPanel.add(panel, BorderLayout.CENTER);
    panel.setLayout(new BorderLayout(0, 0));
    
    _splitPane = new JSplitPane();
    panel.add(_splitPane);
    
    //JScrollPane failuresScrollPane = new JScrollPane(_failuresTreeTable);
    //_splitPane.setRightComponent(failuresScrollPane);
   
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
              ConfigItem<ViewDefinition> configItem = (ConfigItem<ViewDefinition>) configSource.get(viewEntry.getUniqueId());
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
    JPanel viewSelectionPanel = new JPanel(new BorderLayout());
    JScrollPane scrollPane = new JScrollPane(_viewList);
    viewSelectionPanel.add(_viewNameTextField, BorderLayout.PAGE_START);    
    viewSelectionPanel.add(scrollPane, BorderLayout.CENTER);
    _splitPane.setLeftComponent(viewSelectionPanel);
  }
  
  private ListCellRenderer<? super ViewEntry> getViewListCellRenderer() {
    return new ViewListCellRenderer();
  }

  private ViewListModel getViewListModel() {
    return new ViewListModel(getToolContext().getConfigMaster());
  }
  
  private final SynchronousQueue<Void> _endQueue = new SynchronousQueue<>();

  private JSplitPane _splitPane;

  private JList<ViewEntry> _viewList;

  private JButton _goButton;

  private JTree _portfolioTree;
  @Override
  protected void doRun() throws Exception {
    initialize();
    EventQueue.invokeLater(new Runnable() {
      @Override
      public void run() {
        try {
          _frame.pack();
          _frame.setVisible(true);
          _frame.addWindowStateListener(new WindowStateListener() {

            @Override
            public void windowStateChanged(WindowEvent e) {
              if (e.getNewState() == WindowEvent.WINDOW_CLOSED) {
                _endQueue.add(null);
              }
            }
          });
          _splitPane.setDividerLocation(0.3d);
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    });
    _endQueue.take();
  }

}
