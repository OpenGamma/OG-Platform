/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.regression;


import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.opengamma.core.position.PortfolioNode;
import com.opengamma.core.position.PositionSource;
import com.opengamma.core.position.Trade;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.target.ComputationTargetReference;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ComputedValueResult;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.ViewComputationResultModel;
import com.opengamma.engine.view.ViewResultEntry;
import com.opengamma.engine.view.compilation.CompiledViewDefinition;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.util.ArgumentChecker;

/**
 *
 */
public final class CalculationResults {

  private static final Logger s_logger = LoggerFactory.getLogger(CalculationResults.class);

  private final Map<CalculationResultKey, Object> _values;

  private CalculationResults(Map<CalculationResultKey, Object> values) {
    _values = Collections.unmodifiableMap(values);
  }

  // TODO test case
  public static CalculationResults create(CompiledViewDefinition viewDef,
                                          ViewComputationResultModel results,
                                          PositionSource positionSource) {
    ArgumentChecker.notNull(viewDef, "viewDef");
    ArgumentChecker.notNull(results, "results");
    List<ViewResultEntry> allResults = results.getAllResults();
    Map<CalculationResultKey, Object> valueMap = Maps.newHashMapWithExpectedSize(allResults.size());
    Map<UniqueId, List<String>> nodesToPaths = nodesToPaths(viewDef.getPortfolio().getRootNode(),
                                                            Collections.<String>emptyList());
    for (ViewResultEntry entry : allResults) {
      ComputedValueResult computedValue = entry.getComputedValue();
      ValueSpecification valueSpec = computedValue.getSpecification();
      ComputationTargetSpecification targetSpec = valueSpec.getTargetSpecification();
      ComputationTargetType targetType = targetSpec.getType();
      UniqueId nodeId;
      ObjectId targetId;
      if (targetType.equals(ComputationTargetType.POSITION)) {
        ComputationTargetReference nodeRef = targetSpec.getParent();
        // position targets can have a parent node but it's not guaranteed
        if (nodeRef != null) {
          nodeId = nodeRef.getSpecification().getUniqueId();
        } else {
          nodeId = null;
        }
        UniqueId positionId = targetSpec.getUniqueId();
        String idAttr = positionSource.getPosition(positionId).getAttributes().get(DatabaseRestore.REGRESSION_ID);
        if (idAttr != null) {
          targetId = ObjectId.parse(idAttr);
        } else {
          targetId = null;
          s_logger.warn("No ID attribute found for " + positionId);
        }
      } else if (targetType.equals(ComputationTargetType.PORTFOLIO_NODE)) {
        nodeId = targetSpec.getUniqueId();
        targetId = null;
      } else if (targetType.equals(ComputationTargetType.TRADE)) {
        // TODO this assumes a trade target spec will never have a parent
        // this is true at the moment but subject to change. see PLAT-2286
        // and PortfolioCompilerTraversalCallback.preOrderOperation
        nodeId = null;
        UniqueId tradeId = targetSpec.getUniqueId();
        Trade trade = positionSource.getTrade(tradeId);
        String idAttr = trade.getAttributes().get(DatabaseRestore.REGRESSION_ID);
        targetId = ObjectId.parse(idAttr);
      } else {
        // TODO handle specific requirements
        s_logger.warn("Ignoring value for target type {}", targetType);
        continue;
      }
      List<String> path = nodesToPaths.get(nodeId);
      CalculationResultKey key = new CalculationResultKey(entry.getCalculationConfiguration(),
                                                          valueSpec.getValueName(),
                                                          valueSpec.getProperties(),
                                                          path,
                                                          targetId);
      valueMap.put(key, computedValue.getValue());
    }
    return new CalculationResults(valueMap);
  }

  // TODO test case
  private static Map<UniqueId, List<String>> nodesToPaths(PortfolioNode node, List<String> parentPath) {
    String name = node.getName();
    List<String> path = ImmutableList.<String>builder().addAll(parentPath).add(name).build();
    Map<UniqueId, List<String>> map = Maps.newHashMap();
    map.put(node.getUniqueId(), path);
    for (PortfolioNode childNode : node.getChildNodes()) {
      map.putAll(nodesToPaths(childNode, path));
    }
    return map;
  }

  public Map<CalculationResultKey, Object> getValues() {
    return _values;
  }
}
