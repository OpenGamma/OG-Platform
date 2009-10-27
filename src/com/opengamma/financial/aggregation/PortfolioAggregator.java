/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.aggregation;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.position.Portfolio;
import com.opengamma.engine.position.PortfolioImpl;
import com.opengamma.engine.position.PortfolioNode;
import com.opengamma.engine.position.PortfolioNodeImpl;
import com.opengamma.engine.position.Position;

/**
 * 
 *
 * @author jim
 */
public class PortfolioAggregator {
  @SuppressWarnings("unused")
  private static final Logger s_logger = LoggerFactory.getLogger(PortfolioAggregator.class);
  private List<AggregationFunction<?>> _aggregationFunctions;

  public PortfolioAggregator(AggregationFunction<?>... aggregationFunctions) {
    _aggregationFunctions = Arrays.asList(aggregationFunctions);
  }
  
  public PortfolioNode aggregate(Portfolio inputPortfolio) {
    String aggregatedPortfolioName = buildPortfolioName(inputPortfolio.getPortfolioName());
    List<Position> flattenedPortfolio = new ArrayList<Position>();
    flatten((PortfolioNode) inputPortfolio, flattenedPortfolio);
    PortfolioNode root = aggregate(new PortfolioImpl(aggregatedPortfolioName), flattenedPortfolio, new ArrayDeque<AggregationFunction<?>>(_aggregationFunctions));
    return root;
  }
  
  protected void flatten(PortfolioNode portfolioNode, List<Position> flattenedPortfolio) {
    flattenedPortfolio.addAll(portfolioNode.getPositions());    
    for (PortfolioNode subNode : portfolioNode.getSubNodes()) {
      flatten(subNode, flattenedPortfolio);
    }
  }
  
  /**
   * @param inputNode
   * @param flattenedPortfolio
   * @return
   */
  protected PortfolioNode aggregate(PortfolioNodeImpl inputNode, List<Position> flattenedPortfolio, Queue<AggregationFunction<?>> functionList) {
    AggregationFunction<?> nextFunction = functionList.remove();
    Map<String, List<Position>> buckets = new TreeMap<String, List<Position>>();
    // drop into buckets - could drop straight into tree but this is easier because we can use faster lookups as we're going.
    for (Position position : flattenedPortfolio) {
      Object obj = nextFunction.classifyPosition(position);
      String name = obj.toString();
      if (buckets.containsKey(name)) {
        buckets.get(name).add(position);
      } else {
        ArrayList<Position> list = new ArrayList<Position>();
        list.add(position);
        buckets.put(name, list);
      }
    }
    for (String bucketName : buckets.keySet()) {
      PortfolioNodeImpl newNode = new PortfolioNodeImpl(bucketName);
      inputNode.addSubNode(newNode);
      if (functionList.isEmpty()) {
        for (Position position : buckets.get(bucketName)) {
          newNode.addPosition(position);
        }
      } else {
        aggregate(newNode, buckets.get(bucketName), new ArrayDeque<AggregationFunction<?>>(functionList)); // make a copy for each bucket.
      }
    }
    return inputNode;
  }

  protected String buildPortfolioName(String existingName) {
    StringBuilder aggregatedPortfolioName = new StringBuilder();
    aggregatedPortfolioName.append(existingName);
    aggregatedPortfolioName.append(" aggregated by ");
    for (AggregationFunction<?> aggFunction : _aggregationFunctions) {
      aggregatedPortfolioName.append(aggFunction.getName());
      aggregatedPortfolioName.append(", ");
    }
    if (_aggregationFunctions.size() > 0) {
      aggregatedPortfolioName.delete(aggregatedPortfolioName.length()-2, aggregatedPortfolioName.length()); // remember it's end index _exclusive_
    }
    return aggregatedPortfolioName.toString();
  }
}
