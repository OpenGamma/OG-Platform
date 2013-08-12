/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.enginedebugger;

import java.awt.EventQueue;

import javax.swing.Action;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ComboBoxModel;
import javax.swing.JButton;
import javax.swing.JFrame;
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
import com.opengamma.integration.swing.JPortfolioTree;
import com.opengamma.integration.swing.PortfolioTreeModel;
import com.opengamma.integration.swing.ViewEntry;
import com.opengamma.integration.swing.ViewListCellRenderer;
import com.opengamma.integration.swing.ViewListModel;
import com.opengamma.integration.tool.IntegrationToolContext;
import com.opengamma.scripts.Scriptable;

import javax.swing.JComboBox;
import javax.swing.JLabel;
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
public class EngineDebugger extends AbstractTool<IntegrationToolContext> {
  
  private static final Logger s_logger = LoggerFactory.getLogger(EngineDebugger.class);

  private static final String DEFAULT_VALUE_REQUIREMENT = "Present Value";

  private JFrame _frame;

  /**
   * Launch the application.
   */
  public static void main(String[] args) {
    //new EngineDebugger().initialize();
    new EngineDebugger().initAndRun(args, IntegrationToolContext.class);
  }

  /**
   * Initialize the contents of the frame.
   * @wbp.parser.entryPoint
   */
  private void initialize() {
    _frame = new JFrame();
    _frame.setTitle("Engine Function Debugger");
    _frame.setPreferredSize(new Dimension(1000, 700));
    _frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    
    JPanel mainPanel = new JPanel();
    _frame.getContentPane().add(mainPanel, BorderLayout.CENTER);
    mainPanel.setLayout(new BorderLayout());
    
    JPanel viewSelectionPanel = new JPanel();
    JPanel parametersPanel = new JPanel();
    BoxLayout boxLayout = new BoxLayout(parametersPanel, BoxLayout.LINE_AXIS);
    parametersPanel.setLayout(boxLayout);
    parametersPanel.add(viewSelectionPanel, Box.createHorizontalGlue());

    _valueRequirementField = new JTextField(DEFAULT_VALUE_REQUIREMENT);
    //valueRequirementField.setMinimumSize(new Dimension(200, 24));
    //valueRequirementField.setPreferredSize(new Dimension(700, 24));
    parametersPanel.add(_valueRequirementField, Box.createHorizontalGlue());
    _goButton = new JButton("Go");
    parametersPanel.add(_goButton, Box.createHorizontalGlue());
    mainPanel.add(parametersPanel, BorderLayout.PAGE_START);
    
    JLabel viewDefinitionsLabel = new JLabel("View Definitions");
    viewSelectionPanel.add(viewDefinitionsLabel);
    
    _comboBox = new JComboBox<ViewEntry>();
    _comboBox.setModel(getViewComboBoxModel());
    _comboBox.setRenderer(getViewListCellRenderer());
    viewSelectionPanel.add(_comboBox);

    final ConfigSource configSource = getToolContext().getConfigSource();
    final PositionSource positionSource = getToolContext().getPositionSource();
    final SecuritySource securitySource = getToolContext().getSecuritySource();
    
    _failuresTreeTable = new JXTreeTable(new DefaultTreeTableModel());
    _failuresTreeTable.setShowsRootHandles(true);
    _failuresTreeTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    
    JPanel panel = new JPanel();
    mainPanel.add(panel, BorderLayout.CENTER);
    panel.setLayout(new BorderLayout(0, 0));
    
    _splitPane = new JSplitPane();
    panel.add(_splitPane);
    
    JScrollPane failuresScrollPane = new JScrollPane(_failuresTreeTable);
    _splitPane.setRightComponent(failuresScrollPane);
    
    _portfolioTree = new JPortfolioTree(new DefaultTreeModel(null), getToolContext().getConfigSource());
    JScrollPane scrollPane = new JScrollPane(_portfolioTree);
    _splitPane.setLeftComponent(scrollPane);
    
    _portfolioTree.addTreeSelectionListener(new TreeSelectionListener() {
      @Override
      public void valueChanged(TreeSelectionEvent e) {
        JTree portfolioTree = (JTree) e.getSource();
        updateTreeTableModel(portfolioTree);
      }
    });
    
    _comboBox.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        JComboBox<ViewEntry> cb = (JComboBox<ViewEntry>) e.getSource();
        final ViewEntry viewEntry = (ViewEntry) cb.getSelectedItem();
        if (viewEntry != null) {
          SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
              ConfigItem<ViewDefinition> configItem = (ConfigItem<ViewDefinition>) configSource.get(viewEntry.getUniqueId());
              if (configItem.getValue() != null) {
                _portfolioTree.setModel(getPortfolioTreeModel(configItem.getValue().getPortfolioId(), getToolContext()));
              } else {
                JOptionPane.showMessageDialog(null, "There is no portfolio set in the selected view", "No portfolio", JOptionPane.ERROR_MESSAGE);
              }
            }
          });
        }
      }
    });
    
    _goButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        updateTreeTableModel(_portfolioTree);
      }
    });
  }
  
  private ListCellRenderer<? super ViewEntry> getViewListCellRenderer() {
    return new ViewListCellRenderer();
  }

  private ComboBoxModel<ViewEntry> getViewComboBoxModel() {
    return new ViewListModel(getToolContext().getConfigMaster());
  }
  
  private TreeModel getPortfolioTreeModel(UniqueId portfolioId, IntegrationToolContext toolContext) {
    return new PortfolioTreeModel(portfolioId, toolContext);
  }
  
  private ResolutionFailureTreeTableModel createResolutionFailureTreeTableModel(final Object leafNode, final String valueReq) {
    DependencyGraphTraceBuilderProperties properties = new DependencyGraphTraceBuilderProperties();
    ComputationTargetType targetType = null;
    if (leafNode instanceof Position) {
      targetType = ComputationTargetType.POSITION;
    } else if (leafNode instanceof Trade) {
      targetType = ComputationTargetType.TRADE;
    } else if (leafNode instanceof PortfolioNode) {
      targetType = ComputationTargetType.PORTFOLIO_NODE;
    } else if (leafNode instanceof Portfolio) {
      targetType = ComputationTargetType.PORTFOLIO;
    } else if (leafNode instanceof Security) {
      targetType = ComputationTargetType.SECURITY;
    }
    final String name;
    final ValueProperties constraints;
    final int i = valueReq.indexOf('[');
    if ((i > 0) && (valueReq.charAt(valueReq.length() - 1) == ']')) {
      name = valueReq.substring(0, i);
      constraints = ValueProperties.parse(valueReq.substring(i + 1, valueReq.length() - 1));
    } else {
      name = valueReq;
      constraints = ValueProperties.none();
    }
    properties = properties.addRequirement(new ValueRequirement(name, targetType, ((UniqueIdentifiable) leafNode).getUniqueId(), constraints));
    DependencyGraphBuildTrace trace = getToolContext().getDependencyGraphTraceProvider().getTrace(properties);
    ResolutionFailureTreeTableModel failuresTreeTableModel = new ResolutionFailureTreeTableModel(trace.getFailures());
    return failuresTreeTableModel;
  }
  
  private void updateTreeTableModel(JTree portfolioTree) {
    TreePath selectionPath = portfolioTree.getSelectionPath();
    if (selectionPath != null) {
      final Object leafNode = selectionPath.getLastPathComponent();
      if (leafNode instanceof UniqueIdentifiable) {
        SwingWorker<ResolutionFailureTreeTableModel, Object> worker = new SwingWorker<ResolutionFailureTreeTableModel, Object>() {
          @Override
          protected ResolutionFailureTreeTableModel doInBackground() throws Exception {
            ResolutionFailureTreeTableModel failuresTreeTableModel = createResolutionFailureTreeTableModel(leafNode, _valueRequirementField.getText().trim());
            return failuresTreeTableModel;
          }

          @Override
          protected void done() {
            try {
              _failuresTreeTable.setTreeTableModel(new DebugTraceTreeTableModel(get()));
              System.err.println("set tree table model");
            } catch (InterruptedException ex) {
              JOptionPane.showMessageDialog(null, "Thread interrupted while getting graph trace", "Thread Interrupted", JOptionPane.ERROR_MESSAGE);
            } catch (ExecutionException ex) {
              JOptionPane.showMessageDialog(null, "Execution execption while getting graph trace", "Execution Exception", JOptionPane.ERROR_MESSAGE);
            }
          }
          
        };
        worker.execute();
      } else {
        JOptionPane.showMessageDialog(null, "Target " + leafNode.toString() + " has no unique identifier", "No UniqueId", JOptionPane.ERROR_MESSAGE);
      }
    }
  }

  private final SynchronousQueue<Void> _endQueue = new SynchronousQueue<>();

  private JSplitPane _splitPane;

  private JComboBox<ViewEntry> _comboBox;

  private JButton _goButton;

  private JXTreeTable _failuresTreeTable;

  private JTextField _valueRequirementField;

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
