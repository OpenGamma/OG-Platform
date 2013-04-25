/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.aggregation;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.opengamma.core.position.Portfolio;
import com.opengamma.core.position.PortfolioNode;
import com.opengamma.core.position.Position;
import com.opengamma.core.position.PositionSource;
import com.opengamma.core.position.impl.SimplePortfolio;
import com.opengamma.core.position.impl.SimplePortfolioNode;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.engine.view.compilation.PortfolioCompiler;
import com.opengamma.financial.portfolio.save.SavePortfolio;
import com.opengamma.id.UniqueId;
import com.opengamma.id.UniqueIdSupplier;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.portfolio.ManageablePortfolio;
import com.opengamma.master.portfolio.PortfolioMaster;
import com.opengamma.master.portfolio.PortfolioSearchRequest;
import com.opengamma.master.portfolio.PortfolioSearchResult;
import com.opengamma.master.position.PositionMaster;

/**
 * An aggregator of portfolios.
 */
public final class PortfolioAggregator {
  
  private static final Logger s_logger = LoggerFactory.getLogger(PortfolioAggregator.class);

  private static final UniqueIdSupplier s_syntheticIdentifiers = new UniqueIdSupplier("PortfolioAggregator");

  private final List<AggregationFunction<?>> _aggregationFunctions;

  public PortfolioAggregator(AggregationFunction<?>... aggregationFunctions) {
    _aggregationFunctions = Arrays.asList(aggregationFunctions);
  }
  
  public PortfolioAggregator(Collection<AggregationFunction<?>> aggregationFunctions) {
    _aggregationFunctions = new ArrayList<AggregationFunction<?>>(aggregationFunctions);
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
    List<Position> flattenedPortfolio = flatten(inputPortfolio);
    final SimplePortfolioNode root = new SimplePortfolioNode(createSyntheticIdentifier(), buildPortfolioName("Portfolio"));
    SimplePortfolio aggPortfolio = new SimplePortfolio(aggId, aggPortfolioName, root);
    aggregate(root, flattenedPortfolio, new ArrayDeque<AggregationFunction<?>>(_aggregationFunctions));
    aggPortfolio.setAttributes(inputPortfolio.getAttributes());
    return aggPortfolio;
  }
  
  public static List<Position> flatten(Portfolio inputPortfolio) {
    List<Position> positions = Lists.newArrayList();
    flatten(inputPortfolio.getRootNode(), positions);
    return positions;
  }
  
  private static void flatten(PortfolioNode portfolioNode, List<Position> flattenedPortfolio) {
    flattenedPortfolio.addAll(portfolioNode.getPositions());    
    for (PortfolioNode subNode : portfolioNode.getChildNodes()) {
      flatten(subNode, flattenedPortfolio);
    }
  }
  
  private void aggregate(SimplePortfolioNode inputNode, List<Position> flattenedPortfolio, Queue<AggregationFunction<?>> functionList) {
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

  private String buildPortfolioName(String existingName) {
    StringBuilder aggregatedPortfolioName = new StringBuilder();
    aggregatedPortfolioName.append(existingName);
    aggregatedPortfolioName.append(" aggregated by ");
    for (AggregationFunction<?> aggFunction : _aggregationFunctions) {
      aggregatedPortfolioName.append(aggFunction.getName());
      aggregatedPortfolioName.append(", ");
    }
    if (_aggregationFunctions.size() > 0) {
      aggregatedPortfolioName.delete(aggregatedPortfolioName.length() - 2, aggregatedPortfolioName.length()); // remember it's end index _exclusive_
    }
    return aggregatedPortfolioName.toString();
  }

  public static void aggregate(String portfolioName, String aggregationName,
                               PortfolioMaster portfolioMaster, PositionMaster positionMaster,
                               PositionSource positionSource, SecuritySource secSource,
                               AggregationFunction<?>[] aggregationFunctions, boolean split) {
    PortfolioSearchRequest searchReq = new PortfolioSearchRequest();
    searchReq.setName(portfolioName);
    s_logger.info("Searching for portfolio " + portfolioName + "...");
    PortfolioSearchResult searchResult = portfolioMaster.search(searchReq);
    s_logger.info("Done. Got " + searchResult.getDocuments().size() + " results");
    ManageablePortfolio manageablePortfolio = searchResult.getFirstPortfolio();
    if (manageablePortfolio == null) {
      s_logger.error("Portfolio " + portfolioName + " was not found");
      System.exit(1);
    }
    s_logger.info("Reloading portfolio from position source...");
    Portfolio portfolio = positionSource.getPortfolio(manageablePortfolio.getUniqueId(), VersionCorrection.LATEST);
    if (portfolio == null) {
      s_logger.error("Portfolio " + portfolioName + " was not found from PositionSource");
      System.exit(1);
    }
    s_logger.info("Done.");
    ExecutorService newFixedThreadPool = Executors.newFixedThreadPool(20);
    s_logger.info("Resolving portfolio positions and securities...");
    Portfolio resolvedPortfolio = PortfolioCompiler.resolvePortfolio(portfolio, newFixedThreadPool, secSource);
    if (resolvedPortfolio == null) {
      s_logger.error("Portfolio " + portfolioName + " was not correctly resolved by PortfolioCompiler");
      System.exit(1);
    }
    s_logger.info("Resolution Complete.");
    PortfolioAggregator aggregator = new PortfolioAggregator(aggregationFunctions);
    s_logger.info("Beginning aggregation");
    Portfolio aggregatedPortfolio = aggregator.aggregate(resolvedPortfolio);
    s_logger.info("Aggregation complete, about to persist...");
    if (aggregatedPortfolio == null) {
      s_logger.error("Portfolio " + portfolioName + " was not correctly aggregated by the Portfolio Aggregator");
      System.exit(1);
    }
    SavePortfolio savePortfolio = new SavePortfolio(newFixedThreadPool, portfolioMaster, positionMaster);
    if (split) {
      for (PortfolioNode portfolioNode : aggregatedPortfolio.getRootNode().getChildNodes()) {
        String splitPortfolioName = portfolioName + " (" + aggregationName + " " + portfolioNode.getName() + ")";
        SimplePortfolioNode root = new SimplePortfolioNode("root");
        root.addChildNode(portfolioNode);
        Portfolio splitPortfolio = new SimplePortfolio(splitPortfolioName, root);
        splitPortfolio.setAttributes(aggregatedPortfolio.getAttributes());
        s_logger.info("Saving split portfolio " + portfolioName + "...");
        savePortfolio.savePortfolio(splitPortfolio, true);
      }

    } else {
      savePortfolio.savePortfolio(aggregatedPortfolio, true); // update matching named portfolio.
    }
    s_logger.info("Saved.");

    // Shut down thread pool before returning
    newFixedThreadPool.shutdown();
  }
}
