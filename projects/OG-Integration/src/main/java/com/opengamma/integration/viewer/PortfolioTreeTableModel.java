/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.viewer;

import java.util.ArrayList;
import java.util.Map;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreePath;

import org.jdesktop.swingx.treetable.AbstractTreeTableModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.Instant;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.LinkUtils;
import com.opengamma.core.position.PortfolioNode;
import com.opengamma.core.position.Position;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.value.ComputedValueResult;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.view.ViewCalculationResultModel;
import com.opengamma.engine.view.ViewComputationResultModel;
import com.opengamma.engine.view.ViewDeltaResultModel;
import com.opengamma.engine.view.compilation.CompiledViewDefinition;
import com.opengamma.engine.view.cycle.ViewCycleMetadata;
import com.opengamma.engine.view.execution.ViewCycleExecutionOptions;
import com.opengamma.engine.view.listener.ViewResultListener;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.util.tuple.Pair;

class PortfolioTreeTableModel extends AbstractTreeTableModel implements ViewResultListener {

  private static final Logger s_logger = LoggerFactory.getLogger(PortfolioTreeTableModel.class);
  private static final String UNAVAILABLE_VALUE = "N/A";
  
  private ViewerColumnModel _columnModel;
  private PortfolioNode _treeRoot;
  private ViewComputationResultModel _resultModel;

  public PortfolioTreeTableModel() {
    _columnModel = new ViewerColumnModel();
  }

  @Override
  public Object getRoot() {
    return _treeRoot;
  }

  @Override
  public synchronized String getColumnName(int column) {
    if (column == 0) {
      return "Trade";
    } else {
      return _columnModel.getCalculationConfigurationName(column) + "-" + _columnModel.getRequirementName(column);
    }
  }

  @Override
  public synchronized int getColumnCount() {
    return _columnModel.getColumnCount();
  }

  /*
  private void dumpPortfolio(PortfolioNode node, int depth) {
    StringBuilder indent = new StringBuilder();
    for (int i=0; i<depth; i++) indent.append(" ");
    s_logger.info("{}{}", indent.toString(), node);
    if (node.getPositions() != null) {
      for (Position position : node.getPositions()) {
        s_logger.info("{}  {}", indent.toString(), position);
      }
    }
    if (node.getSubNodes() != null) {
      for (PortfolioNode subNode : node.getSubNodes()) {
        dumpPortfolio(subNode, depth+2);
      }
    }
  }
  */

  //-------------------------------------------------------------------------
  @Override
  public UserPrincipal getUser() {
    return UserPrincipal.getLocalUser();
  }
  
  @Override
  public void viewDefinitionCompiled(CompiledViewDefinition compiledViewDefinition, boolean hasMarketDataPermissions) {
    _columnModel.init(compiledViewDefinition.getViewDefinition());
    _treeRoot = compiledViewDefinition.getPortfolio().getRootNode();
    fireTreeStructureChanged();
  }

  @Override
  public void viewDefinitionCompilationFailed(Instant valuationTime, Exception exception) {
  }

  @Override
  public void cycleStarted(ViewCycleMetadata cycleMetadata) {
    //ignore
  }
  
  @Override
  public void cycleFragmentCompleted(ViewComputationResultModel fullFragment, ViewDeltaResultModel deltaFragment) {
    //ignore
  }

  @Override
  public void cycleCompleted(ViewComputationResultModel fullResult, ViewDeltaResultModel deltaResult) {
    _resultModel = fullResult;
    fireTreeNodesChanged();
  }

  @Override
  public void cycleExecutionFailed(ViewCycleExecutionOptions executionOptions, Exception exception) {
  }

  @Override
  public void processCompleted() {
  }

  @Override
  public void processTerminated(boolean executionInterrupted) {
  }

  @Override
  public void clientShutdown(Exception e) {
  }
  
  //-------------------------------------------------------------------------
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

  private Object renderForColumn(ComputationTargetSpecification targetSpec, int column) {
    String calcConfigName = _columnModel.getCalculationConfigurationName(column);
    String requirementName = _columnModel.getRequirementName(column);

    if (_resultModel == null || calcConfigName == null || requirementName == null) {
      s_logger.warn("Unhandled column {}", column);
      return UNAVAILABLE_VALUE;
    }

    ViewCalculationResultModel calcResultModel = _resultModel.getCalculationResult(calcConfigName);
    Map<Pair<String, ValueProperties>, ComputedValueResult> values = calcResultModel.getValues(targetSpec);
    if (values == null) {
      s_logger.debug("No values available for {}", targetSpec);
      return UNAVAILABLE_VALUE;
    }
    // TODO 2011-06-15 -- support value name AND value properties so that the required value can be looked up directly
    for (Map.Entry<Pair<String, ValueProperties>, ComputedValueResult> valueEntry : values.entrySet()) {
      if (valueEntry.getKey().getFirst().equals(requirementName)) {
        return valueEntry.getValue().toString();
      }
    }
    return UNAVAILABLE_VALUE;
  }

  protected String getNodeTitle(Object node) {
    if (node instanceof Position) {
      Position position = (Position) node;
      String key = LinkUtils.bestName(position.getSecurityLink());
      return key + " @ " + position.getQuantity();
    } else if (node instanceof PortfolioNode) {
      return ((PortfolioNode) node).getName();
    } else {
      return "Unknown Node";
    }
  }

  @Override
  public synchronized Object getValueAt(Object node, int column) {
    //s_logger.info("getValueAt({}, {})", node, column);
    if (node == null) {
      return null;
    }
    if (column == 0) {
      return getNodeTitle(node);
    }
    if (node instanceof UniqueIdentifiable) {
      ComputationTargetSpecification targetSpec = ComputationTargetSpecification.of(((UniqueIdentifiable) node).getUniqueId());
      return renderForColumn(targetSpec, column);
    }
    return "Unknown object: " + node;
  }

  @Override
  public synchronized Object getChild(Object parent, int index) {
    //s_logger.info("getChild({}, {})", parent, index);
    if (parent == null) {
      return null;
    }
    if (parent instanceof Position) {
      throw new OpenGammaRuntimeException("Unexpected call to getChild on a position");
    } else if (parent instanceof PortfolioNode) {
      PortfolioNode node = (PortfolioNode) parent;
      if (index < node.getPositions().size()) {
        return (node.getPositions().toArray())[index];
      } else {
        return (node.getChildNodes().toArray())[index - node.getPositions().size()];
      }
    } else {
      throw new OpenGammaRuntimeException("Unexpected call to getChild on a unexpected type " + parent);
    }
  }

  @Override
  public synchronized int getChildCount(Object parent) {
    //s_logger.info("getChildCount({})", parent);
    if (parent == null) {
      return 0;
    }
    if (parent instanceof Position) {
      return 0;
    } else if (parent instanceof PortfolioNode) {
      PortfolioNode node = (PortfolioNode) parent;
      return node.size();
    } else {
      throw new OpenGammaRuntimeException("Unexpected call to getChild on a unexpected type " + parent);
    }
  }

  @Override
  public synchronized int getIndexOfChild(Object parent, Object child) {
    //s_logger.info("getIndexOfChild({}, {})", parent, child);
    if (parent == null) {
      return 0;
    }
    if (parent instanceof Position) {
      return 0;
    } else if (parent instanceof PortfolioNode) {
      PortfolioNode node = (PortfolioNode) parent;
      if (child instanceof Position) {
        return new ArrayList<Position>(node.getPositions()).indexOf(child);
      } else {
        return new ArrayList<PortfolioNode>(node.getChildNodes()).indexOf(child) + node.getPositions().size();
      }
    } else {
      throw new OpenGammaRuntimeException("Unexpected call to getChild on a unexpected type " + parent);
    }
  }

  public synchronized boolean isLeaf(Object node) {
    if (node == null) {
      return true;
    }
    if (node instanceof Position) {
      return true;
    }
    if (node instanceof PortfolioNode) {
      return ((PortfolioNode) node).size() == 0;
    }
    return false;
  }

}
