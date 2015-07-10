/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.aggregation;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.opengamma.core.position.Portfolio;
import com.opengamma.core.position.PortfolioNode;
import com.opengamma.core.position.Position;
import com.opengamma.core.position.impl.SimplePortfolio;
import com.opengamma.core.position.impl.SimplePortfolioNode;
import com.opengamma.financial.comparison.PortfolioComparator;
import com.opengamma.util.ArgumentChecker;

/**
 * An aggregator of portfolios.
 */
public class PortfolioMultiAggregatorWithOverrideOrdering {
  
  private static final Logger s_logger = LoggerFactory.getLogger(PortfolioMultiAggregatorWithOverrideOrdering.class);

  private final List<List<AggregationFunction<?>>> _aggregationFunctionsList;

  public PortfolioMultiAggregatorWithOverrideOrdering(List<List<AggregationFunction<?>>> aggregationFunctions) {
    _aggregationFunctionsList = aggregationFunctions;
  }
  
  public Portfolio aggregate(Portfolio inputPortfolio, Comparator<Position> comparator) {
    return aggregate(inputPortfolio, inputPortfolio.getName() + " multi-aggregated", comparator);
  }

  public Portfolio aggregate(Portfolio inputPortfolio, String portfolioName, Comparator<Position> comparator) {
    ArgumentChecker.notEmpty(portfolioName, "portfolioName");
    List<Position> flattenedPortfolio = Lists.newArrayList();
    flatten(inputPortfolio.getRootNode(), flattenedPortfolio, comparator);
    final SimplePortfolioNode root = new SimplePortfolioNode(portfolioName);
    for (List<AggregationFunction<?>> aggregationFunctions : _aggregationFunctionsList) {
      final SimplePortfolioNode aggregateRoot = new SimplePortfolioNode(buildPortfolioNodeName(aggregationFunctions));
      aggregate(aggregateRoot, flattenedPortfolio, new ArrayDeque<>(aggregationFunctions), comparator);
      root.addChildNode(aggregateRoot);
    }
    return new SimplePortfolio(portfolioName, root);
  }
  
  protected void flatten(PortfolioNode portfolioNode, List<Position> flattenedPortfolio, Comparator<Position> comparator) {
    flattenedPortfolio.addAll(portfolioNode.getPositions());    
    for (PortfolioNode subNode : portfolioNode.getChildNodes()) {
      flatten(subNode, flattenedPortfolio, comparator);
    }
  }
  
  protected void aggregate(SimplePortfolioNode inputNode, List<Position> flattenedPortfolio, Queue<AggregationFunction<?>> functionList, Comparator<Position> comparator) {
    AggregationFunction<?> nextFunction = functionList.remove();
    s_logger.debug("Aggregating {} positions by {}", flattenedPortfolio, nextFunction);
    @SuppressWarnings("unchecked")
    Map<String, List<Position>> buckets = new TreeMap<>((Comparator<? super String>) nextFunction);
    for (Object entry : nextFunction.getRequiredEntries()) {
      buckets.put(entry.toString(), new ArrayList<Position>());
    }
    // drop into buckets - could drop straight into tree but this is easier because we can use faster lookups as we're going.
    for (Position position : flattenedPortfolio) {
      Object obj = nextFunction.classifyPosition(position);
      if (obj != null) {
        String name = obj.toString();
        if (buckets.containsKey(name)) {
          buckets.get(name).add(position);
        } else {
          ArrayList<Position> list = Lists.newArrayList();
          list.add(position);
          buckets.put(name, list);
        }
      }
    }
    for (String bucketName : buckets.keySet()) {
      SimplePortfolioNode newNode = new SimplePortfolioNode();
      newNode.setParentNodeId(inputNode.getUniqueId());
      newNode.setName(bucketName);
      inputNode.addChildNode(newNode);
      List<Position> bucket = buckets.get(bucketName);
      Collections.sort(bucket, comparator);
      if (functionList.isEmpty() || bucket.isEmpty()) { //IGN-138 - don't build huge empty portfolios
        for (Position position : bucket) {
          newNode.addPosition(position);
        }
      } else {
        aggregate(newNode, bucket, new ArrayDeque<>(functionList), comparator); // make a copy for each bucket.
      }
    }
  }
  
  protected String buildPortfolioNodeName(List<AggregationFunction<?>> aggregationFunctions) {
    StringBuilder aggregatedPortfolioName = new StringBuilder();
    for (AggregationFunction<?> aggFunction : aggregationFunctions) {
      aggregatedPortfolioName.append(aggFunction.getName());
      aggregatedPortfolioName.append(", ");
    }
    if (aggregationFunctions.size() > 0) {
      aggregatedPortfolioName.delete(aggregatedPortfolioName.length() - 2, aggregatedPortfolioName.length()); // remember it's end index _exclusive_
    }
    return aggregatedPortfolioName.toString();
  }
}
