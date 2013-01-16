/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
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
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.Pair;

/**
 *
 */
public class PortfolioGridStructure extends MainGridStructure {

  private final AnalyticsNode _root;

  private PortfolioGridStructure(AnalyticsColumnGroup fixedColumns,
                                 Map<String, List<ColumnKey>> analyticsColumns,
                                 CompiledViewDefinition compiledViewDef,
                                 ValueMappings valueMappings,
                                 List<Row> rows) {
    super(ImmutableList.of(fixedColumns), analyticsColumns, compiledViewDef, valueMappings, rows);
    ArgumentChecker.notNull(compiledViewDef, "compiledViewDef");
    _root = AnalyticsNode.portoflioRoot(compiledViewDef);
  }

  private PortfolioGridStructure() {
    _root = AnalyticsNode.emptyRoot();
  }

  /* package */ static PortfolioGridStructure create(CompiledViewDefinition compiledViewDef, ValueMappings valueMappings) {
    List<MainGridStructure.Row> rows = rows(compiledViewDef);
    AnalyticsColumn labelColumn = new AnalyticsColumn("Label", "", null, new LabelRenderer(0, rows));
    AnalyticsColumn quantityColumn = new AnalyticsColumn("Quantity", "", BigDecimal.class, new QuantityRenderer(1, rows));
    AnalyticsColumnGroup fixedColumns = new AnalyticsColumnGroup("fixed", ImmutableList.of(labelColumn, quantityColumn));
    Map<String, List<ColumnKey>> analyticsColumns = buildColumns(compiledViewDef.getViewDefinition());
    return new PortfolioGridStructure(fixedColumns,
                                      analyticsColumns,
                                      compiledViewDef,
                                      valueMappings,
                                      rows);
  }

  /* package */ static PortfolioGridStructure empty() {
    return new PortfolioGridStructure();
  }

  public AnalyticsNode getRoot() {
    return _root;
  }

  private static Map<String, List<ColumnKey>> buildColumns(ViewDefinition viewDef) {
    Map<String, List<ColumnKey>> columnsByCalcConfig = Maps.newHashMap();
    for (ViewCalculationConfiguration calcConfig : viewDef.getAllCalculationConfigurations()) {
      List<ColumnKey> columnKeys = Lists.newArrayList();
      for (Pair<String, ValueProperties> portfolioOutput : calcConfig.getAllPortfolioRequirements()) {
        String valueName = portfolioOutput.getFirst();
        ValueProperties constraints = portfolioOutput.getSecond();
        ColumnKey columnKey = new ColumnKey(calcConfig.getName(), valueName, constraints);
        columnKeys.add(columnKey);
      }
      columnsByCalcConfig.put(calcConfig.getName(), columnKeys);
    }
    return columnsByCalcConfig;
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
