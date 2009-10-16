/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.viewer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.swing.ListSelectionModel;
import javax.swing.RowSorter;
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

import com.opengamma.engine.analytics.AnalyticValue;
import com.opengamma.engine.analytics.AnalyticValueDefinition;
import com.opengamma.engine.analytics.yc.DiscountCurveAnalyticFunction;
import com.opengamma.engine.depgraph.DependencyGraphModel;
import com.opengamma.engine.depgraph.DependencyNode;
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
  private JXTable _parent;
  private PortfolioTableModel _parentModel;
  private Set<DependencyNode> _roots = new HashSet<DependencyNode>();
  private ViewComputationCache _viewComputationCache;
  private String[] _columns = new String[] {"Dependency Graph", "Inputs", "Outputs"};
  
  public PortfolioSelectionListenerAndDepGraphTreeTableModel(JXTable parent) {
    super("Root");
    _parent = parent;
    // add listener for selections.
    _parent.getSelectionModel().addListSelectionListener(this);
    // add listener for new data.
    _parentModel = (PortfolioTableModel) _parent.getModel();
    _parentModel.addTableModelListener(this);
  }
  
  public String getColumnName(int column) {
    return _columns[column];
  }
  
  private void readChanges(ListSelectionModel lsm) {
    boolean allDataChanged = false; // flag to refresh whole table or just all rows
    if (lsm.isSelectionEmpty()) {
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
        if (_roots.size() == 0) {
          allDataChanged = true;
        }
        _roots = new HashSet<DependencyNode>(dependencyGraphModel.getDependencyGraph(security).getTopLevelNodes());
        Iterator<DependencyNode> iter = _roots.iterator();
        while (iter.hasNext()) {
          DependencyNode next = iter.next();
          if (next.getFunction().getClass().equals(DiscountCurveAnalyticFunction.class)) {
            iter.remove();
          }
        }
      }
      if (allDataChanged) {
        fireTreeStructureChanged();
      } else {
        fireTreeNodesChanged();
      }
    }
  }
  
  private void fireTreeNodesChanged() {
    TreeModelEvent treeModelEvent = new TreeModelEvent(this, new TreePath(getRoot()), null, null);
    for (TreeModelListener listener : getTreeModelListeners()) {
      listener.treeNodesChanged(treeModelEvent);
    }    
  }
  
  private void fireTreeStructureChanged() {
    TreeModelEvent treeModelEvent = new TreeModelEvent(this, new TreePath(getRoot()), null, null);
    for (TreeModelListener listener : getTreeModelListeners()) {
      listener.treeStructureChanged(treeModelEvent);
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
    if (parent instanceof DependencyNode) {
      DependencyNode parentNode = (DependencyNode)parent;
      return parentNode.getInputNodes().size();
    } else {
      return _roots.size();
    }
  }

  @Override
  public int getIndexOfChild(Object parent, Object child) {
    if (parent instanceof DependencyNode) {
      DependencyNode node = (DependencyNode) parent;
      return new ArrayList<DependencyNode>(node.getInputNodes()).indexOf(child);
    } else {
      return new ArrayList<DependencyNode>(_roots).indexOf(child);
    }
  }
  
  @Override
  public Object getChild(Object parent, int index) {
    if (parent instanceof DependencyNode) {
      DependencyNode node = (DependencyNode) parent;
      return new ArrayList<DependencyNode>(node.getInputNodes()).get(index);
    } else {
      return new ArrayList<DependencyNode>(_roots).get(index);
    }
  }

  @Override
  public Object getValueAt(Object node, int column) {
    ValueDefinitionRenderingVisitor visitor = new ValueDefinitionRenderingVisitor();
    if (node instanceof DependencyNode) {
      DependencyNode depNode = (DependencyNode) node;
      switch (column) {
      case 0:
        return depNode.getFunction().getShortName();
      case 1:
        StringBuilder sb = new StringBuilder();
        Collection<AnalyticValueDefinition<?>> values = depNode.getInputValues();
        for (AnalyticValueDefinition<?> valueDefinition : values) {
          if (valueDefinition instanceof VisitableValueDefinition) {
            sb.append(((VisitableValueDefinition) valueDefinition).accept(visitor));
          }
          sb.append(", ");
        }
        if (sb.length() >= 2) {
          sb.delete(sb.length()-2, sb.length());
        }
        return sb.toString();
      case 2:
        StringBuilder sb2 = new StringBuilder();
        Collection<AnalyticValueDefinition<?>> values2 = depNode.getOutputValues();
        for (AnalyticValueDefinition<?> valueDefinition : values2) {
          AnalyticValue<?> value = _viewComputationCache.getValue(valueDefinition);
          if (value instanceof Renderable) {
            sb2.append(value.getValue());
          } else {
            sb2.append(value.getValue());
          }
          sb2.append(", ");
        }
        if (sb2.length() >= 2) {
          sb2.delete(sb2.length()-2, sb2.length());
        }        
        return sb2.toString();
      default:
        return "Default";
      }
    } else {
      return "Root";
    }
  }

  @Override
  public int getColumnCount() {
    return 3;
  }
}
