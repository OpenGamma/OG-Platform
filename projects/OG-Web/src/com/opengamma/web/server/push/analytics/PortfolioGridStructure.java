/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.analytics;

import java.util.List;

import com.google.common.collect.Lists;
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

  private static List<Row> rows(CompiledViewDefinition viewDef) {
    PortfolioMapperFunction<Row> targetFn = new PortfolioMapperFunction<Row>() {
      @Override
      public Row apply(PortfolioNode node) {
        ComputationTargetSpecification target =
            new ComputationTargetSpecification(ComputationTargetType.PORTFOLIO_NODE, node.getUniqueId());
        return new Row(target, node.getName());
      }

      @Override
      public Row apply(Position position) {
        ComputationTargetSpecification target =
            new ComputationTargetSpecification(ComputationTargetType.POSITION, position.getUniqueId());
        // TODO will the security be resolved?
        return new Row(target, position.getSecurity().getName());
      }
    };
    return PortfolioMapper.map(viewDef.getPortfolio().getRootNode(), targetFn);
  }

}
