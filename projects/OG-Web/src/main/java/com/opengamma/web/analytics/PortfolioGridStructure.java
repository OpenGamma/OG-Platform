/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.opengamma.core.position.Portfolio;
import com.opengamma.core.position.PortfolioNode;
import com.opengamma.core.position.Position;
import com.opengamma.core.position.impl.PortfolioMapper;
import com.opengamma.core.position.impl.PortfolioMapperFunction;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.view.ViewCalculationConfiguration;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.engine.view.compilation.CompiledViewDefinition;
import com.opengamma.util.tuple.Pair;

/**
 *
 */
public class PortfolioGridStructure extends MainGridStructure {

  private final AnalyticsNode _root;

  private PortfolioGridStructure(GridColumnGroups columnGroups,
                                 CompiledViewDefinition compiledViewDef,
                                 TargetLookup targetLookup) {
    super(columnGroups, targetLookup);
    _root = AnalyticsNode.portoflioRoot(compiledViewDef);
  }

  private PortfolioGridStructure() {
    _root = AnalyticsNode.emptyRoot();
  }

  /* package */ static PortfolioGridStructure forAnalytics(CompiledViewDefinition compiledViewDef,
                                                           ValueMappings valueMappings) {
    List<MainGridStructure.Row> rows = rows(compiledViewDef);
    GridColumn labelColumn = new GridColumn("Label", "", null, new PortfolioLabelRenderer(rows));
    GridColumn quantityColumn = new GridColumn("Quantity", "", BigDecimal.class, new QuantityRenderer(rows), null);
    GridColumnGroup fixedColumns = new GridColumnGroup("fixed", ImmutableList.of(labelColumn, quantityColumn));
    TargetLookup targetLookup = new TargetLookup(valueMappings, rows);
    List<GridColumnGroup> analyticsColumns = buildAnalyticsColumns(compiledViewDef.getViewDefinition(), targetLookup);
    List<GridColumnGroup> groups = Lists.newArrayList(fixedColumns);
    groups.addAll(analyticsColumns);
    GridColumnGroups columnGroups = new GridColumnGroups(groups);
    return new PortfolioGridStructure(columnGroups, compiledViewDef, targetLookup);
  }

  /* package */
  @SuppressWarnings("UnusedDeclaration")
  static PortfolioGridStructure forBlotter() {
    return new PortfolioGridStructure();
  }

  /* package */ static PortfolioGridStructure empty() {
    return new PortfolioGridStructure();
  }

  public AnalyticsNode getRoot() {
    return _root;
  }

  /**
   * @param viewDef The view definition
   * @return Columns for displaying calculated analytics data, one group per calculation configuration
   */
  private static List<GridColumnGroup> buildAnalyticsColumns(ViewDefinition viewDef, TargetLookup targetLookup) {
    List<GridColumnGroup> columnGroups = Lists.newArrayList();
    for (ViewCalculationConfiguration calcConfig : viewDef.getAllCalculationConfigurations()) {
      List<GridColumn> columns = Lists.newArrayList();
      for (Pair<String, ValueProperties> portfolioOutput : calcConfig.getAllPortfolioRequirements()) {
        String valueName = portfolioOutput.getFirst();
        Class<?> columnType = ValueTypes.getTypeForValueName(valueName);
        ValueProperties constraints = portfolioOutput.getSecond();
        ColumnSpecification columnSpec = new ColumnSpecification(calcConfig.getName(), valueName, constraints);
        // TODO ensure columnSpec isn't a duplicate
        columns.add(GridColumn.forKey(columnSpec, columnType, targetLookup));
      }
      columnGroups.add(new GridColumnGroup(calcConfig.getName(), columns));
    }
    return columnGroups;
  }

  private static List<Row> rows(final CompiledViewDefinition viewDef) {
    final Portfolio portfolio = viewDef.getPortfolio();
    if (portfolio == null) {
      return Collections.emptyList();
    }
    PortfolioMapperFunction<Row> targetFn = new PortfolioMapperFunction<Row>() {

      @Override
      public Row apply(PortfolioNode node) {
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
        return new Row(target, nodeName);
      }

      @Override
      public Row apply(PortfolioNode parentNode, Position position) {
        // TODO I don't think toLatest() will do long term. resolution time available on the result model
        ComputationTargetSpecification target = ComputationTargetSpecification.of(parentNode).containing(
            ComputationTargetType.POSITION, position.getUniqueId().toLatest());
        return new Row(target, position.getSecurity().getName(), position.getQuantity());
      }
    };
    return PortfolioMapper.map(portfolio.getRootNode(), targetFn);
  }
}
