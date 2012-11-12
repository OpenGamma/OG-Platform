/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics;

import java.util.Collections;
import java.util.List;

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
import com.opengamma.engine.view.compilation.CompiledViewDefinition;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.Pair;

/**
 *
 */
public class PortfolioGridStructure extends MainGridStructure {

  private final AnalyticsNode _root;

  /* package */PortfolioGridStructure(final CompiledViewDefinition compiledViewDef) {
    super(compiledViewDef, rows(compiledViewDef));
    ArgumentChecker.notNull(compiledViewDef, "compiledViewDef");
    _root = AnalyticsNode.portoflioRoot(compiledViewDef);
  }

  private PortfolioGridStructure() {
    _root = AnalyticsNode.emptyRoot();
  }

  /* package */static PortfolioGridStructure empty() {
    return new PortfolioGridStructure();
  }

  @Override
  protected List<ColumnKey> buildColumns(final ViewCalculationConfiguration calcConfig) {
    final List<ColumnKey> columnKeys = Lists.newArrayList();
    for (final Pair<String, ValueProperties> portfolioOutput : calcConfig.getAllPortfolioRequirements()) {
      final String valueName = portfolioOutput.getFirst();
      final ValueProperties constraints = portfolioOutput.getSecond();
      final ColumnKey columnKey = new ColumnKey(calcConfig.getName(), valueName, constraints);
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
    final PortfolioMapperFunction<Row> targetFn = new PortfolioMapperFunction<Row>() {

      @Override
      public Row apply(final PortfolioNode node) {
        final ComputationTargetSpecification target = ComputationTargetSpecification.of(node);
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
      public Row apply(final PortfolioNode parentNode, final Position position) {
        final ComputationTargetSpecification target = ComputationTargetSpecification.of(parentNode).containing(ComputationTargetType.POSITION, position.getUniqueId().toLatest());
        return new Row(target, position.getSecurity().getName(), position.getQuantity());
      }

    };
    return PortfolioMapper.map(portfolio.getRootNode(), targetFn);
  }
}
