/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.viewer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.swing.ListSelectionModel;
import javax.swing.RowSorter;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.table.TableModel;
import javax.swing.tree.TreePath;

import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.treetable.AbstractTreeTableModel;
import org.jdesktop.swingx.treetable.TreeTableModel;

import com.opengamma.engine.analytics.AnalyticValue;
import com.opengamma.engine.analytics.AnalyticValueDefinition;
import com.opengamma.engine.depgraph.DependencyGraphModel;
import com.opengamma.engine.depgraph.DependencyNode;
import com.opengamma.engine.depgraph.SecurityDependencyGraph;
import com.opengamma.engine.position.Position;
import com.opengamma.engine.security.Security;
import com.opengamma.engine.security.SecurityMaster;
import com.opengamma.engine.view.ViewComputationCache;

/**
 * 
 *
 * @author jim
 */
public class PortfolioSelectionListenerAndDepGraphTreeTableModel extends AbstractTreeTableModel implements ListSelectionListener, TableModelListener  {
  private Map<Class<?>, TreeModelListener> _listeners = new HashMap<Class<?>, TreeModelListener>();
  private JXTable _parent;
  private PortfolioTableModel _parentModel;
  private Position _position;
  private Set<DependencyNode> _roots = new HashSet<DependencyNode>();
  private ViewComputationCache _viewComputationCache;
  
  public PortfolioSelectionListenerAndDepGraphTreeTableModel(JXTable parent) {
    super("Root");
    _parent = parent;
    // add listener for selections.
    _parent.getSelectionModel().addListSelectionListener(this);
    // add listener for new data.
    _parentModel = (PortfolioTableModel) _parent.getModel();
    _parentModel.addTableModelListener(this);
  }
  
  private void setPosition(Position p) {
    _position = p;;
  }

  private void readChanges(ListSelectionModel lsm) {
    boolean allDataChanged = false; // flag to refresh whole table or just all rows
    if (lsm.isSelectionEmpty()) {
      synchronized (this) {
        setPosition(null);
      }
    } else {
      int selectedRow = lsm.getMinSelectionIndex();
      RowSorter<? extends TableModel> rowSorter = _parent.getRowSorter();
      int modelRow;
      if (rowSorter != null) {
        modelRow = rowSorter.convertRowIndexToModel(selectedRow);
      }  else {
        modelRow = selectedRow;
      }
      Entry<Position, Map<String, AnalyticValue<?>>> row = _parentModel.getRow(modelRow);
      _viewComputationCache = _parentModel.getViewComputationCache();
      DependencyGraphModel dependencyGraphModel = _parentModel.getDependencyGraphModel();
      SecurityMaster secMaster = _parentModel.getSecurityMaster();
      Position position = row.getKey();
      Security security = secMaster.getSecurity(position.getSecurityKey());
      synchronized (this) {
        setPosition(row.getKey());
        if (_roots.size() == 0) {
          allDataChanged = true;
        }
        _roots = dependencyGraphModel.getDependencyGraph(security).getTopLevelNodes();
        
        System.err.println("set roots to "+_roots.size());
      }
      if (allDataChanged) {
        fireTreeStructureChanged();
      } else {
        fireTreeNodesChanged();
      }
//      if (allDataChanged) {
//        SwingUtilities.invokeLater(new Runnable() {
//          @Override
//          public void run() {
//            fireTreeStructureChanged();
//          }
//        });
//      } else {
//        SwingUtilities.invokeLater(new Runnable() {
//          @Override
//          public void run() {
//            fireTreeNodesChanged();
//          }
//        });
//      }
    }
  }
  
  private void fireTreeNodesChanged() {
    //System.err.println("fireNodesChanged");
    TreeModelEvent treeModelEvent = new TreeModelEvent(this, new TreePath(getRoot()), null, null);
    for (TreeModelListener listener : getTreeModelListeners()) {
      listener.treeNodesChanged(treeModelEvent);//(Object[]) null));//new Object[] {"Root"}));
    }    
  }
  
  private void fireTreeStructureChanged() {
    //System.err.println("fireNodesChanged");
    TreeModelEvent treeModelEvent = new TreeModelEvent(this, new TreePath(getRoot()), null, null);
    for (TreeModelListener listener : getTreeModelListeners()) {
      listener.treeStructureChanged(treeModelEvent);//(Object[]) null));//new Object[] {"Root"}));
    }
  }
  
  @Override
  public void valueChanged(ListSelectionEvent e) {
    if (e.getValueIsAdjusting()) return;
    ListSelectionModel lsm = (ListSelectionModel)e.getSource();
    readChanges(lsm);
  }

  @Override
  public void tableChanged(TableModelEvent e) {
    //System.err.println("tableChanged");
    ListSelectionModel lsm = _parent.getSelectionModel();
    RowSorter<? extends TableModel> rowSorter = _parent.getRowSorter();
    int row;
    if (rowSorter != null) {
      row = rowSorter.convertRowIndexToModel(lsm.getMinSelectionIndex());
    } else {
      row = lsm.getMinSelectionIndex();
    }
    // this logic isn't actually necessary at the moment, but might be if we get more efficient table updating.
    if (e.getFirstRow() <= row && e.getLastRow() >= row) {
      // the selected row has changed under us, regrab it.
      readChanges(lsm);
    }
  }
  @Override
  public int getChildCount(Object parent) {
    //System.err.println("parent "+parent);
    if (parent instanceof DependencyNode) {
      DependencyNode parentNode = (DependencyNode)parent;
      System.err.println("getChildCount() returning "+parentNode.getInputNodes().size());
      return parentNode.getInputNodes().size();
    } else {
      System.err.println("getChildCount() returning "+_roots.size());
      return _roots.size();
    }
  }

  @Override
  public int getIndexOfChild(Object parent, Object child) {
    //System.err.println("getIndexOfChild("+parent+", "+child+")");
    if (parent instanceof DependencyNode) {
      DependencyNode node = (DependencyNode) parent;
      return new ArrayList<DependencyNode>(node.getInputNodes()).indexOf(child);
    } else {
      return new ArrayList<DependencyNode>(_roots).indexOf(child);
    }
  }
  
  @Override
  public Object getChild(Object parent, int index) {
    //System.err.println("getChild("+parent+", "+index+")");
    if (parent instanceof DependencyNode) {
      DependencyNode node = (DependencyNode) parent;
      return new ArrayList<DependencyNode>(node.getInputNodes()).get(index);
    } else {
      return new ArrayList<DependencyNode>(_roots).get(index);
    }
  }

  @Override
  public Object getValueAt(Object node, int column) {
    //System.err.println("getValueAt("+node+", "+column+")");
    if (node instanceof DependencyNode) {
      DependencyNode depNode = (DependencyNode) node;
      switch (column) {
      case 0:
        return depNode.getFunction().getShortName();
      case 1:
        StringBuilder sb = new StringBuilder();
        Collection<AnalyticValueDefinition<?>> values = depNode.getResolvedInputs().values();
        for (AnalyticValueDefinition<?> valueDefinition : values) {
          sb.append(valueDefinition.toString());
          sb.append("=");
          sb.append(_viewComputationCache.getValue(valueDefinition));
          sb.append(", ");
        }
        if (sb.length() >= 2) {
          sb.delete(sb.length()-2, sb.length());
        }
        return sb.toString();
      default:
        return "Default";
      }
    } else {
      return "Root";
    }
  }

  @Override
  public int getColumnCount() {
    return 2;
  }
}
