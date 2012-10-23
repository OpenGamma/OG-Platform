/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.analytics;

import java.util.Collections;
import java.util.List;

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
import com.opengamma.util.tuple.Pair;
import com.opengamma.web.server.RequirementBasedColumnKey;

/**
 *
 */
public class PortfolioGridStructure extends MainGridStructure {

  private final AnalyticsNode _root;

  /* package */ PortfolioGridStructure(CompiledViewDefinition compiledViewDef) {
    super(compiledViewDef, rows(compiledViewDef));
    ArgumentChecker.notNull(compiledViewDef, "compiledViewDef");
    _root = AnalyticsNode.portoflioRoot(compiledViewDef);
  }

  private PortfolioGridStructure() {
    _root = AnalyticsNode.emptyRoot();
  }

  /* package */ static PortfolioGridStructure empty() {
    return new PortfolioGridStructure();
  }

  protected List<RequirementBasedColumnKey> buildColumns(ViewCalculationConfiguration calcConfig) {
    List<RequirementBasedColumnKey> columnKeys = Lists.newArrayList();
    for (Pair<String, ValueProperties> portfolioOutput : calcConfig.getAllPortfolioRequirements()) {
      String valueName = portfolioOutput.getFirst();
      ValueProperties constraints = portfolioOutput.getSecond();
      RequirementBasedColumnKey columnKey = new RequirementBasedColumnKey(calcConfig.getName(), valueName, constraints);
      columnKeys.add(columnKey);
    }
    return columnKeys;
  }

  public AnalyticsNode getRoot() {
    return _root;
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
