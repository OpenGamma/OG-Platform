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
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.view.ViewCalculationConfiguration;
import com.opengamma.engine.view.compilation.CompiledViewDefinition;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.lambdava.tuple.Pair;

/**
 *
 */
public class PortfolioGridStructure extends MainGridStructure {

  /* Label column can be NodeId or PositionId, setting the type as null means it will be set for every cell. */
  private static final AnalyticsColumnGroup s_fixedColumnGroup =
      new AnalyticsColumnGroup("fixed", ImmutableList.of(new AnalyticsColumn("Label", "", null),
                                                         new AnalyticsColumn("Quantity", "", BigDecimal.class)));
  private final AnalyticsNode _root;

  /* package */ PortfolioGridStructure(CompiledViewDefinition compiledViewDef, ValueMappings valueMappings) {
    super(s_fixedColumnGroup, compiledViewDef, rows(compiledViewDef), valueMappings);
    ArgumentChecker.notNull(compiledViewDef, "compiledViewDef");
    _root = AnalyticsNode.portoflioRoot(compiledViewDef);
  }

  private PortfolioGridStructure() {
    _root = AnalyticsNode.emptyRoot();
  }

  /* package */ static PortfolioGridStructure empty() {
    return new PortfolioGridStructure();
  }

  protected List<ColumnKey> buildColumns(ViewCalculationConfiguration calcConfig) {
    List<ColumnKey> columnKeys = Lists.newArrayList();
    for (Pair<String, ValueProperties> portfolioOutput : calcConfig.getAllPortfolioRequirements()) {
      String valueName = portfolioOutput.getFirst();
      ValueProperties constraints = portfolioOutput.getSecond();
      ColumnKey columnKey = new ColumnKey(calcConfig.getName(), valueName, constraints);
      columnKeys.add(columnKey);
    }
    return columnKeys;
  }

  public AnalyticsNode getRoot() {
    return _root;
  }

  @Override
  public String toString() {
    return "PortfolioGridStructure [_root=" + _root + "]";
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
      public Row apply(Position position) {
        ComputationTargetSpecification target =
            new ComputationTargetSpecification(ComputationTargetType.POSITION, position.getUniqueId());
        return new Row(target, position.getSecurity().getName(), position.getQuantity());
      }
    };
    return PortfolioMapper.map(portfolio.getRootNode(), targetFn);
  }
}
