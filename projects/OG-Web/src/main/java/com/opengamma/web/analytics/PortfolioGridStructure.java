/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.opengamma.core.position.Portfolio;
import com.opengamma.core.position.PortfolioNode;
import com.opengamma.core.position.Position;
import com.opengamma.core.position.Trade;
import com.opengamma.core.position.impl.PortfolioMapper;
import com.opengamma.core.position.impl.PortfolioMapperFunction;
import com.opengamma.core.security.Security;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.view.ViewCalculationConfiguration;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.engine.view.compilation.CompiledViewDefinition;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.id.UniqueId;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.Pair;

/**
 * The structure of the grid that displays portfolio data and analytics. Contains the column definitions and
 * the portfolio tree structure.
 * TODO subclass for the blotter?
 */
public class PortfolioGridStructure extends MainGridStructure {

  /** The root node of the portfolio structure. */
  private final AnalyticsNode _rootNode;
  /** Rows in the grid. */
  private final List<PortfolioGridRow> _rows;
  private final ValueMappings _valueMappings;

  /* package */  PortfolioGridStructure(GridColumnGroups columnGroups,
                                        AnalyticsNode rootNode,
                                        List<PortfolioGridRow> rows,
                                        TargetLookup targetLookup,
                                        ValueMappings valueMappings) {
    super(columnGroups, targetLookup);
    _rootNode = rootNode;
    _rows = rows;
    _valueMappings = valueMappings;
  }

  /* package */ static PortfolioGridStructure create(Portfolio portfolio, ValueMappings valueMappings) {
    ArgumentChecker.notNull(valueMappings, "valueMappings");
    List<PortfolioGridRow> rows = buildRows(portfolio);
    TargetLookup targetLookup = new TargetLookup(valueMappings, rows);
    AnalyticsNode rootNode = AnalyticsNode.portoflioRoot(portfolio);
    return new PortfolioGridStructure(GridColumnGroups.empty(), rootNode, rows, targetLookup, valueMappings);
  }

  /**
   * @return The root node of the portfolio structure.
   */
  public AnalyticsNode getRootNode() {
    return _rootNode;
  }

  /* package */ PortfolioGridStructure withUpdatedRows(Portfolio portfolio) {
    AnalyticsNode rootNode = AnalyticsNode.portoflioRoot(portfolio);
    List<PortfolioGridRow> rows = buildRows(portfolio);
    TargetLookup targetLookup = new TargetLookup(_valueMappings, rows);
    return new PortfolioGridStructure(getColumnStructure(), rootNode, rows, targetLookup, _valueMappings);
  }

  /* package */ PortfolioGridStructure withUpdatedStructure(CompiledViewDefinition compiledViewDef) {
    Portfolio portfolio = compiledViewDef.getPortfolio();
    AnalyticsNode rootNode = AnalyticsNode.portoflioRoot(portfolio);
    List<PortfolioGridRow> rows = buildRows(portfolio);
    GridColumnGroup fixedColumns = buildFixedColumns(rows);
    ValueMappings valueMappings = new ValueMappings(compiledViewDef);
    TargetLookup targetLookup = new TargetLookup(valueMappings, rows);
    List<GridColumnGroup> analyticsColumns = buildAnalyticsColumns(compiledViewDef.getViewDefinition(), targetLookup);
    List<GridColumnGroup> groups = Lists.newArrayList(fixedColumns);
    groups.addAll(analyticsColumns);
    GridColumnGroups columnGroups = new GridColumnGroups(groups);
    return new PortfolioGridStructure(columnGroups, rootNode, rows, targetLookup, valueMappings);
  }

  /* package */ static GridColumnGroup buildFixedColumns(List<PortfolioGridRow> rows) {
    GridColumn labelColumn = new GridColumn("Name", "", null, new PortfolioLabelRenderer(rows));
    GridColumn quantityColumn = new GridColumn("Quantity", "", BigDecimal.class, new QuantityRenderer(rows), null);
    return new GridColumnGroup("fixed", ImmutableList.of(labelColumn, quantityColumn), false);
  }

  /**
   * @param viewDef The view definition
   * @return Columns for displaying calculated analytics data, one group per calculation configuration
   */
  /* package */ static List<GridColumnGroup> buildAnalyticsColumns(ViewDefinition viewDef, TargetLookup targetLookup) {
    List<GridColumnGroup> columnGroups = Lists.newArrayList();
    Set<ColumnSpecification> columnSpecs = Sets.newHashSet();
    for (ViewCalculationConfiguration calcConfig : viewDef.getAllCalculationConfigurations()) {
      List<GridColumn> columns = Lists.newArrayList();
      for (Pair<String, ValueProperties> portfolioOutput : calcConfig.getAllPortfolioRequirements()) {
        String valueName = portfolioOutput.getFirst();
        Class<?> columnType = ValueTypes.getTypeForValueName(valueName);
        ValueProperties constraints = portfolioOutput.getSecond();
        ColumnSpecification columnSpec = new ColumnSpecification(calcConfig.getName(), valueName, constraints);
        // ensure columnSpec isn't a duplicate
        if (columnSpecs.add(columnSpec)) {
          columns.add(GridColumn.forSpec(columnSpec, columnType, targetLookup));
        }
      }
      if (!columns.isEmpty()) {
        columnGroups.add(new GridColumnGroup(calcConfig.getName(), columns, true));
      }
    }
    return columnGroups;
  }

  /* package */ static List<PortfolioGridRow> buildRows(final Portfolio portfolio) {
    if (portfolio == null) {
      return Collections.emptyList();
    }
    PortfolioMapperFunction<List<PortfolioGridRow>> targetFn = new PortfolioMapperFunction<List<PortfolioGridRow>>() {

      @Override
      public List<PortfolioGridRow> apply(PortfolioNode node) {
        ComputationTargetSpecification target =
            new ComputationTargetSpecification(ComputationTargetType.PORTFOLIO_NODE, node.getUniqueId());
        String nodeName;
        // if the parent ID is null it's the root node
        if (node.getParentNodeId() == null) {
          // the root node is called "Root" which isn't any use for displaying in the UI, use the portfolio name instead
          nodeName = portfolio.getName();
        } else {
          nodeName = node.getName();
        }
        return Lists.newArrayList(new PortfolioGridRow(target, nodeName, node.getUniqueId()));
      }

      @Override
      public List<PortfolioGridRow> apply(PortfolioNode parentNode, Position position) {
        ComputationTargetSpecification nodeSpec = ComputationTargetSpecification.of(parentNode);
        // TODO I don't think toLatest() will do long term. resolution time available on the result model
        UniqueId positionId = position.getUniqueId();
        ComputationTargetSpecification target = nodeSpec.containing(ComputationTargetType.POSITION,
                                                                    positionId.toLatest());
        Security security = position.getSecurity();
        List<PortfolioGridRow> rows = Lists.newArrayList();
        UniqueId nodeId = parentNode.getUniqueId();
        if (isFungible(position.getSecurity())) {
          rows.add(new PortfolioGridRow(target, security.getName(), security.getUniqueId(), nodeId, positionId));
          for (Trade trade : position.getTrades()) {
            String tradeDate = trade.getTradeDate().toString();
            rows.add(new PortfolioGridRow(ComputationTargetSpecification.of(trade),
                                          tradeDate,
                                          security.getUniqueId(),
                                          nodeId,
                                          positionId,
                                          trade.getUniqueId()));
          }
        } else {
          Collection<Trade> trades = position.getTrades();
          if (trades.isEmpty()) {
            rows.add(new PortfolioGridRow(target, security.getName(), security.getUniqueId(), nodeId, positionId));
          } else {
            // there is never more than one trade on a position in an OTC security
            UniqueId tradeId = trades.iterator().next().getUniqueId();
            rows.add(new PortfolioGridRow(target, security.getName(), security.getUniqueId(), nodeId, positionId, tradeId));
          }
        }
        return rows;
      }
    };
    List<List<PortfolioGridRow>> rows = PortfolioMapper.map(portfolio.getRootNode(), targetFn);
    Iterable<PortfolioGridRow> flattenedRows = Iterables.concat(rows);
    return Lists.newArrayList(flattenedRows);
  }

  /**
   * @param security A security
   * @return true if the security is fungible, false if OTC
   */
  private static boolean isFungible(Security security) {
    if (security instanceof FinancialSecurity) {
      return !((FinancialSecurity) security).accept(new OtcSecurityVisitor());
    } else {
      return false;
    }
  }

  /** Rows in the grid. */
  /* package */ List<PortfolioGridRow> getRows() {
    return _rows;
  }

  /* package */ ValueMappings getValueMappings() {
    return _valueMappings;
  }
}
