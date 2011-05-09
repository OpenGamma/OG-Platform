/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cometd.Client;

import com.opengamma.core.position.Portfolio;
import com.opengamma.core.position.PortfolioNode;
import com.opengamma.core.position.Position;
import com.opengamma.core.security.Security;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.view.compilation.CompiledViewDefinition;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.web.server.conversion.ResultConverterCache;

/**
 * Represents a portfolio grid
 */
public class WebViewPortfolioGrid extends WebViewGrid {
  
  private Map<Long, PortfolioRow> _rowIdToRowMap;
  
  public WebViewPortfolioGrid(CompiledViewDefinition compiledViewDefinition, ResultConverterCache resultConverterCache, Client local, Client remote) {
    this(compiledViewDefinition, flattenPortfolio(compiledViewDefinition.getPortfolio()), resultConverterCache, local, remote);
  }
  
  private WebViewPortfolioGrid(CompiledViewDefinition compiledViewDefinition, List<PortfolioRow> rows, ResultConverterCache resultConverterCache,
      Client local, Client remote) {
    super("portfolio", compiledViewDefinition, getTargets(rows),
        EnumSet.of(ComputationTargetType.PORTFOLIO_NODE, ComputationTargetType.POSITION), resultConverterCache, local,
        remote, "undefined"); 
    _rowIdToRowMap = new HashMap<Long, PortfolioRow>();
    for (PortfolioRow row : rows) {
      long rowId = getGridStructure().getRowId(row.getTarget().getUniqueId());
      _rowIdToRowMap.put(rowId, row);
    }
  }
  
  @Override
  protected void addRowDetails(UniqueIdentifier target, long rowId, Map<String, Object> details) {
    PortfolioRow row = _rowIdToRowMap.get(rowId);
    details.put("indent", row.getDepth());
    if (row.getParentRow() != null) {
      long parentRowId = getGridStructure().getRowId(row.getParentRow().getTarget().getUniqueId());
      details.put("parentRowId", parentRowId);
    }
    ComputationTargetType targetType = row.getTarget().getType();
    details.put("type", targetType.toString());
    if (targetType == ComputationTargetType.POSITION) {
      Security security = row.getPosition().getSecurity();
      String des = security.getName();
      details.put("position", des + " (" + row.getPosition().getQuantity().toPlainString() + ")");
    } else {
      details.put("position", row.getAggregateName());
    }
  }
  
  private static List<UniqueIdentifier> getTargets(List<PortfolioRow> rows) {
    List<UniqueIdentifier> targets = new ArrayList<UniqueIdentifier>();
    for (PortfolioRow row : rows) {
      targets.add(row.getTarget().getUniqueId());
    }
    return targets;
  }
  
  private static List<PortfolioRow> flattenPortfolio(final Portfolio portfolio) {
    List<PortfolioRow> rows = new ArrayList<PortfolioRow>();
    if (portfolio == null) {
      return rows;
    }
    flattenPortfolio(portfolio.getRootNode(), null, 0, portfolio.getName(), rows);
    return rows;
  }
    
  private static void flattenPortfolio(final PortfolioNode portfolio, final PortfolioRow parentRow, final int depth,
      final String nodeName, final List<PortfolioRow> rows) {
    PortfolioRow aggregateRow = new PortfolioRow(depth, parentRow,
        new ComputationTargetSpecification(ComputationTargetType.PORTFOLIO_NODE, portfolio.getUniqueId()), null, nodeName);
    rows.add(aggregateRow);
    
    for (Position position : portfolio.getPositions()) {
      PortfolioRow portfolioRow = new PortfolioRow(depth + 1, aggregateRow,
          new ComputationTargetSpecification(ComputationTargetType.POSITION, position.getUniqueId()), position, null);
      rows.add(portfolioRow);
    }
    Collection<PortfolioNode> subNodes = portfolio.getChildNodes();
    if (subNodes != null && subNodes.size() > 0) {
      for (PortfolioNode subNode : subNodes) {
        flattenPortfolio(subNode, aggregateRow, depth + 1, subNode.getName(), rows);
      }
    }
  }
  
}
