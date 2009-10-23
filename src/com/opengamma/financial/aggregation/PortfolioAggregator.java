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

import com.opengamma.engine.position.FullyPopulatedPortfolio;
import com.opengamma.engine.view.FullyPopulatedPortfolioNode;
import com.opengamma.engine.view.FullyPopulatedPosition;

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
  
  public FullyPopulatedPortfolioNode aggregate(FullyPopulatedPortfolio inputPortfolio) {
    String aggregatedPortfolioName = buildPortfolioName(inputPortfolio.getPortfolioName());
    List<FullyPopulatedPosition> flattenedPortfolio = new ArrayList<FullyPopulatedPosition>();
    flatten((FullyPopulatedPortfolioNode) inputPortfolio, flattenedPortfolio);
    FullyPopulatedPortfolioNode root = aggregate(new FullyPopulatedPortfolio(aggregatedPortfolioName), flattenedPortfolio, new ArrayDeque<AggregationFunction<?>>(_aggregationFunctions));
    return root;
  }
  
  protected void flatten(FullyPopulatedPortfolioNode portfolioNode, List<FullyPopulatedPosition> flattenedPortfolio) {
    flattenedPortfolio.addAll(portfolioNode.getPopulatedPositions());    
    for (FullyPopulatedPortfolioNode subNode : portfolioNode.getPopulatedSubNodes()) {
      flatten(subNode, flattenedPortfolio);
    }
  }
  
  /**
   * @param inputNode
   * @param flattenedPortfolio
   * @return
   */
  protected FullyPopulatedPortfolioNode aggregate(FullyPopulatedPortfolioNode inputNode, List<FullyPopulatedPosition> flattenedPortfolio, Queue<AggregationFunction<?>> functionList) {
    AggregationFunction<?> nextFunction = functionList.remove();
    Map<String, List<FullyPopulatedPosition>> buckets = new TreeMap<String, List<FullyPopulatedPosition>>();
    // drop into buckets - could drop straight into tree but this is easier because we can use faster lookups as we're going.
    for (FullyPopulatedPosition position : flattenedPortfolio) {
      Object obj = nextFunction.classifyPosition(position);
      String name = obj.toString();
      if (buckets.containsKey(name)) {
        buckets.get(name).add(position);
      } else {
        ArrayList<FullyPopulatedPosition> list = new ArrayList<FullyPopulatedPosition>();
        list.add(position);
        buckets.put(name, list);
      }
    }
    for (String bucketName : buckets.keySet()) {
      FullyPopulatedPortfolioNode newNode = new FullyPopulatedPortfolioNode(bucketName);
      inputNode.addSubNode(newNode);
      if (functionList.isEmpty()) {
        for (FullyPopulatedPosition position : buckets.get(bucketName)) {
          newNode.addPosition(position.getPosition(), position.getSecurity());
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
