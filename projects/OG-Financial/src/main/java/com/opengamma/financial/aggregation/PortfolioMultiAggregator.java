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

import com.opengamma.core.position.Portfolio;
import com.opengamma.core.position.PortfolioNode;
import com.opengamma.core.position.Position;
import com.opengamma.core.position.impl.SimplePortfolio;
import com.opengamma.core.position.impl.SimplePortfolioNode;
import com.opengamma.id.UniqueId;
import com.opengamma.id.UniqueIdSupplier;

/**
 * An aggregator of portfolios.
 */
public class PortfolioMultiAggregator {
  
  private static final Logger s_logger = LoggerFactory.getLogger(PortfolioMultiAggregator.class);

  private static final UniqueIdSupplier s_syntheticIdentifiers = new UniqueIdSupplier("PortfolioAggregator");

  private final List<List<AggregationFunction<?>>> _aggregationFunctionsList;

  public PortfolioMultiAggregator(List<List<AggregationFunction<?>>> aggregationFunctions) {
    _aggregationFunctionsList = aggregationFunctions;
  }
  
  private static UniqueId createSyntheticIdentifier() {
    return s_syntheticIdentifiers.get();
  }

  public Portfolio aggregate(Portfolio inputPortfolio) {
    UniqueId portfolioId = inputPortfolio.getUniqueId();
    UniqueId aggId;
    if (portfolioId != null) {
      String aggPortfolioId = buildPortfolioName(portfolioId.getValue());
      aggId = UniqueId.of(portfolioId.getScheme(), aggPortfolioId);
    } else {
      aggId = createSyntheticIdentifier();
    }
    String aggPortfolioName = buildPortfolioName(inputPortfolio.getName());
    List<Position> flattenedPortfolio = new ArrayList<Position>();
    flatten(inputPortfolio.getRootNode(), flattenedPortfolio);
    final SimplePortfolioNode root = new SimplePortfolioNode(createSyntheticIdentifier(), aggPortfolioName);
    for (List<AggregationFunction<?>> aggregationFunctions : _aggregationFunctionsList) {
      final SimplePortfolioNode aggregateRoot = new SimplePortfolioNode(createSyntheticIdentifier(), buildPortfolioNodeName(aggregationFunctions));
      aggregate(aggregateRoot, flattenedPortfolio, new ArrayDeque<AggregationFunction<?>>(aggregationFunctions));
      root.addChildNode(aggregateRoot);
    }
    SimplePortfolio aggPortfolio = new SimplePortfolio(aggId, aggPortfolioName, root);
    return aggPortfolio;
  }
  
  protected void flatten(PortfolioNode portfolioNode, List<Position> flattenedPortfolio) {
    flattenedPortfolio.addAll(portfolioNode.getPositions());    
    for (PortfolioNode subNode : portfolioNode.getChildNodes()) {
      flatten(subNode, flattenedPortfolio);
    }
  }
  
  protected void aggregate(SimplePortfolioNode inputNode, List<Position> flattenedPortfolio, Queue<AggregationFunction<?>> functionList) {
    AggregationFunction<?> nextFunction = functionList.remove();
    s_logger.debug("Aggregating {} positions by {}", flattenedPortfolio, nextFunction);
    @SuppressWarnings("unchecked")
    Map<String, List<Position>> buckets = new TreeMap<String, List<Position>>((Comparator<? super String>) nextFunction);
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
          ArrayList<Position> list = new ArrayList<Position>();
          list.add(position);
          buckets.put(name, list);
        }
      }
    }
    for (String bucketName : buckets.keySet()) {
      SimplePortfolioNode newNode = new SimplePortfolioNode();
      newNode.setUniqueId(createSyntheticIdentifier());
      newNode.setParentNodeId(inputNode.getUniqueId());
      newNode.setName(bucketName);
      inputNode.addChildNode(newNode);
      List<Position> bucket = buckets.get(bucketName);
      Collections.sort(bucket, nextFunction.getPositionComparator());
      if (functionList.isEmpty() || bucket.isEmpty()) { //IGN-138 - don't build huge empty portfolios
        for (Position position : bucket) {
          newNode.addPosition(position);
        }
      } else {
        aggregate(newNode, bucket, new ArrayDeque<AggregationFunction<?>>(functionList)); // make a copy for each bucket.
      }
    }
  }

  protected String buildPortfolioName(String existingName) {
    StringBuilder aggregatedPortfolioName = new StringBuilder();
    aggregatedPortfolioName.append(existingName);
    aggregatedPortfolioName.append(" multi-aggregated");

    return aggregatedPortfolioName.toString();
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
