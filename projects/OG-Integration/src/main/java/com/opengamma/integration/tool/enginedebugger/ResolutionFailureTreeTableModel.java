/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.enginedebugger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.jdesktop.swingx.treetable.AbstractTreeTableModel;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.id.ExternalIdDisplayComparator;
import com.opengamma.core.id.ExternalIdDisplayComparatorUtils;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.depgraph.ResolutionFailure;
import com.opengamma.engine.depgraph.ResolutionFailureImpl;
import com.opengamma.engine.target.ComputationTargetReference;
import com.opengamma.engine.target.ComputationTargetReferenceVisitor;
import com.opengamma.engine.target.ComputationTargetRequirement;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.integration.tool.enginedebugger.node.FunctionEntryNode;
import com.opengamma.integration.tool.enginedebugger.node.TreeTableNode;
import com.opengamma.integration.tool.enginedebugger.node.UnsatisfiedResolutionFailuresNode;
import com.opengamma.integration.tool.enginedebugger.node.ValuePropertyNode;
import com.opengamma.integration.tool.enginedebugger.node.ValueSpecificationToRequirementMapNode;

/**
 * Tree-table model for browsing ResolutionFailure structures
 */
public class ResolutionFailureTreeTableModel extends AbstractTreeTableModel {

  private static final Object LIST_NAME = "Events";
  
  public ResolutionFailureTreeTableModel(List<ResolutionFailure> rootFailures) {
    super(topLevelNodes(rootFailures));
  }
  
  private static List<ResolutionFailureTreeTableNode> topLevelNodes(List<ResolutionFailure> failures) {
    List<ResolutionFailureTreeTableNode> results = new ArrayList<>();
    for (ResolutionFailure failure : failures) {
      results.add(new ResolutionFailureTreeTableNode((ResolutionFailureImpl) failure));
    }
    return results;
  }
  
  @Override
  public int getColumnCount() {
    return 4;
  }

  @Override
  public Object getValueAt(Object node, final int column) {
    if (node instanceof TreeTableNode) {
      TreeTableNode failureNode = (TreeTableNode) node;
      return failureNode.getColumn(column);
    } else if (node instanceof List) {
      if (column == 0) {
        return LIST_NAME;
      } else {
        return null;
      }
    } else if (node instanceof String) {
      switch (column) {
        case 0:
          return (String)node;
        default:
          return null;
      }
    }
    return node.getClass().toString() + "(" + node.hashCode() + ")";//*/
  }

  @Override
  public Object getChild(Object parent, final int index) {
    if (parent instanceof TreeTableNode) {
      TreeTableNode failureNode = (TreeTableNode) parent;
      return failureNode.getChildAt(index);
    } else if (parent instanceof List) {
      List<?> failures = (List<?>) parent;
      return failures.get(index);
    }
    return null;
  }

  @Override
  public int getChildCount(Object parent) {
    if (parent instanceof TreeTableNode) {
      TreeTableNode failureNode = (TreeTableNode) parent;
      return failureNode.getChildCount();
    } else if (parent instanceof List) {
      List<?> failures = (List<?>) parent;
      return failures.size();
    }
    return 0;
  }

  @Override
  public int getIndexOfChild(Object parent, Object child) {
    if (parent instanceof TreeTableNode) {
      TreeTableNode failureNode = (TreeTableNode) parent;
      return failureNode.getIndexOfChild(child);
    } else if (parent instanceof List) {
      List<?> failures = (List<?>) parent;
      return failures.indexOf(child);
    }
    return -1;    
  }

}