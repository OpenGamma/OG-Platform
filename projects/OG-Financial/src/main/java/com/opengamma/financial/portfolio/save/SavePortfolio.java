/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.portfolio.save;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.apache.commons.lang.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.core.position.Portfolio;
import com.opengamma.core.position.PortfolioNode;
import com.opengamma.core.position.Position;
import com.opengamma.core.position.Trade;
import com.opengamma.core.position.impl.AbstractPortfolioNodeTraversalCallback;
import com.opengamma.core.position.impl.PortfolioNodeTraverser;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.master.DocumentVisibility;
import com.opengamma.master.portfolio.ManageablePortfolio;
import com.opengamma.master.portfolio.ManageablePortfolioNode;
import com.opengamma.master.portfolio.PortfolioDocument;
import com.opengamma.master.portfolio.PortfolioMaster;
import com.opengamma.master.portfolio.PortfolioSearchRequest;
import com.opengamma.master.portfolio.PortfolioSearchResult;
import com.opengamma.master.position.ManageablePosition;
import com.opengamma.master.position.ManageableTrade;
import com.opengamma.master.position.PositionDocument;
import com.opengamma.master.position.PositionMaster;
import com.opengamma.master.position.PositionSearchRequest;
import com.opengamma.master.position.PositionSearchResult;
import com.opengamma.master.security.ManageableSecurityLink;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 *  Utility to save a portfolio.
 */
public class SavePortfolio {

  private static final Logger s_logger = LoggerFactory.getLogger(SavePortfolio.class);

  private final ExecutorService _executor;
  private final PortfolioMaster _portfolios;
  private final PositionMaster _positions;
  private final Map<UniqueId, ObjectId> _positionMap = new HashMap<UniqueId, ObjectId>();
  private final boolean _rewriteExistingPositions;

  private static final ConcurrentMap<ExternalId, ObjectId> s_cache = new ConcurrentHashMap<ExternalId, ObjectId>();
  private static final ObjectId MISSING = ObjectId.of("SavePortfolio", "MISSING_VALUE");

  // TODO: cache this properly with EHCache or something or there may be a memory leak

  public SavePortfolio(final ExecutorService executor, final PortfolioMaster portfolios, final PositionMaster positions) {
    this(executor, portfolios, positions, false);
  }
  
  protected SavePortfolio(final ExecutorService executor, final PortfolioMaster portfolios, final PositionMaster positions, final boolean rewriteExistingPositions) {
    _executor = executor;
    _portfolios = portfolios;
    _positions = positions;
    _rewriteExistingPositions = rewriteExistingPositions;
  }

  protected ExternalIdBundle mapSecurityKey(final ExternalIdBundle securityKey) {
    return null;
  }

  protected ManageablePosition createManageablePosition(final Position position) {
    final ManageablePosition manageablePosition = new ManageablePosition();
    manageablePosition.setQuantity(position.getQuantity());
    manageablePosition.setSecurityLink(new ManageableSecurityLink(position.getSecurityLink()));
    manageablePosition.setAttributes(position.getAttributes());
    final Collection<Trade> trades = position.getTrades();
    final List<ManageableTrade> manageableTrades = new ArrayList<ManageableTrade>(trades.size());
    for (Trade trade : trades) {
      final ManageableTrade mtrade = new ManageableTrade(trade);
      final ExternalIdBundle replacementKey = mapSecurityKey(mtrade.getSecurityLink().getExternalId());
      if (replacementKey != null) {
        mtrade.getSecurityLink().setExternalId(replacementKey);
      }
      mtrade.setAttributes(trade.getAttributes());
      manageableTrades.add(mtrade);
    }
    manageablePosition.setTrades(manageableTrades);
    final String providerIdFieldName = manageablePosition.providerId().name();
    if (position.getAttributes().containsKey(providerIdFieldName)) {
      // this is here to preserve the provider id when round-tripping to and from the resolved vs managed positions.
      manageablePosition.setProviderId(ExternalId.parse(position.getAttributes().get(providerIdFieldName)));
    } else {
      manageablePosition.setProviderId(position.getUniqueId().toExternalId());
    }
    return manageablePosition;
  }

  private void populatePositionMapCache(final PortfolioNode node) {
    final List<Future<Pair<UniqueId, ObjectId>>> futures = new LinkedList<Future<Pair<UniqueId, ObjectId>>>();
    PortfolioNodeTraverser.depthFirst(new AbstractPortfolioNodeTraversalCallback() {
      @Override
      public void preOrderOperation(final PortfolioNode parentNode, final Position position) {
        final ExternalId positionId = position.getUniqueId().toExternalId();
        ObjectId id = s_cache.get(positionId);
        if (id == null) {
          futures.add(_executor.submit(new Callable<Pair<UniqueId, ObjectId>>() {
            @Override
            public Pair<UniqueId, ObjectId> call() throws Exception {
              final PositionSearchRequest searchRequest = new PositionSearchRequest();
              searchRequest.setPositionProviderId(positionId);
              final PositionSearchResult searchResult = _positions.search(searchRequest);
              ObjectId id = null;
              if (searchResult.getFirstPosition() != null) {
                id = searchResult.getFirstPosition().getUniqueId().getObjectId();
                s_logger.debug("Found position {} in master at {}", position, id);
              }
              if (id == null) {
                s_cache.putIfAbsent(positionId, MISSING);
              } else {
                s_cache.putIfAbsent(positionId, id);
              }
              return Pairs.of(position.getUniqueId(), id);
            }
          }));
        } else if (id == MISSING) {
          _positionMap.put(position.getUniqueId(), null);
        } else {
          _positionMap.put(position.getUniqueId(), id);
        }
      }
    }).traverse(node);
    if (futures.isEmpty()) {
      return;
    }
    s_logger.info("{} operations to populate cache", futures.size());
    Iterator<Future<Pair<UniqueId, ObjectId>>> futureItr = futures.iterator();
    while (futureItr.hasNext()) {
      final Future<Pair<UniqueId, ObjectId>> future = futureItr.next();
      try {
        final Pair<UniqueId, ObjectId> value = future.get();
        futureItr.remove();
        _positionMap.put(value.getFirst(), value.getSecond());
      } catch (final InterruptedException e) {
        s_logger.warn("Interrupted", e);
        break;
      } catch (final ExecutionException e) {
        s_logger.warn("Exception", e);
        break;
      }
    }
    futureItr = futures.iterator();
    while (futureItr.hasNext()) {
      final Future<?> future = futureItr.next();
      future.cancel(false);
    }
  }

  protected ObjectId mapPositionIdentifier(final Position position) {
    ObjectId id = _positionMap.get(position.getUniqueId());
    if (id == null) {
      s_logger.debug("Adding position {} to master", position);
      id = _positions.add(new PositionDocument(createManageablePosition(position))).getUniqueId().getObjectId();
      _positionMap.put(position.getUniqueId(), id);
      s_cache.put(position.getUniqueId().toExternalId(), id);
    } else {
      s_logger.debug("Position {} already in master at {}", position, id);
    }
    return id;
  }

  private ManageablePortfolioNode createManageablePortfolioNode(final PortfolioNode node) {
    final ManageablePortfolioNode manageableNode = new ManageablePortfolioNode();
    manageableNode.setName(node.getName());
    final List<PortfolioNode> childNodes = node.getChildNodes();
    final List<ManageablePortfolioNode> manageableChildNodes = new ArrayList<ManageablePortfolioNode>(childNodes.size());
    // TODO: put a hook here so a sub-class can choose to flatten the portfolio if it wishes
    for (PortfolioNode childNode : childNodes) {
      manageableChildNodes.add(createManageablePortfolioNode(childNode));
    }
    manageableNode.setChildNodes(manageableChildNodes);
    final List<Position> positions = node.getPositions();
    final List<ObjectId> positionIdentifiers = new ArrayList<ObjectId>(positions.size());
    for (Position position : positions) {
      positionIdentifiers.add(mapPositionIdentifier(position));
    }
    manageableNode.setPositionIds(positionIdentifiers);
    return manageableNode;
  }

  private ManageablePortfolio createManageablePortfolio(final Portfolio portfolio) {
    if (!_rewriteExistingPositions) {
      populatePositionMapCache(portfolio.getRootNode());
    }
    final ManageablePortfolio manageablePortfolio = new ManageablePortfolio();
    manageablePortfolio.setName(getPortfolioName(portfolio));
    manageablePortfolio.setRootNode(createManageablePortfolioNode(portfolio.getRootNode()));
    manageablePortfolio.setAttributes(portfolio.getAttributes());
    return manageablePortfolio;
  }

  protected String getPortfolioName(final Portfolio portfolio) {
    return portfolio.getName();
  }

  private boolean nodesEqual(final ManageablePortfolioNode node1, final ManageablePortfolioNode node2) {
    if (!ObjectUtils.equals(node1.getName(), node2.getName())) {
      return false;
    }
    final List<ManageablePortfolioNode> children1 = node1.getChildNodes(), children2 = node2.getChildNodes();
    if (children1.size() != children2.size()) {
      return false;
    }
    if (!ObjectUtils.equals(node1.getPositionIds(), node2.getPositionIds())) {
      return false;
    }
    final Iterator<ManageablePortfolioNode> itr1 = children1.iterator(), itr2 = children2.iterator();
    while (itr1.hasNext() && itr2.hasNext()) {
      if (!nodesEqual(itr1.next(), itr2.next())) {
        return false;
      }
    }
    return true;
  }
  
  public UniqueId savePortfolio(final Portfolio portfolio, final boolean updateMatchingName) {
    return savePortfolio(portfolio, updateMatchingName, DocumentVisibility.VISIBLE);
  }

  public UniqueId savePortfolio(final Portfolio portfolio, final boolean updateMatchingName, final DocumentVisibility visibility) {
    s_logger.debug("Saving portfolio '{}'", portfolio.getName());
    final PortfolioSearchRequest request = new PortfolioSearchRequest();
    request.setName(getPortfolioName(portfolio));
    request.setVisibility(visibility);  // Any existing match needs to be at least as visible 
    final PortfolioSearchResult result = _portfolios.search(request);
    final ManageablePortfolio manageablePortfolio = createManageablePortfolio(portfolio);
    PortfolioDocument document;
    if (updateMatchingName) {
      document = result.getFirstDocument();
      // TODO why did this assume document will never be null? is that valid or have I broken something?
      if (document != null) {
        final ManageablePortfolio resultPortfolio = document.getPortfolio();
        if (nodesEqual(manageablePortfolio.getRootNode(), resultPortfolio.getRootNode())) {
          s_logger.debug("Found existing match at {}", document.getUniqueId());
          return document.getUniqueId();
        }
      }
    } else {
      document = null;
      for (PortfolioDocument resultDocument : result.getDocuments()) {
        final ManageablePortfolio resultPortfolio = resultDocument.getPortfolio();
        if (manageablePortfolio.getName().equals(resultPortfolio.getName()) && nodesEqual(manageablePortfolio.getRootNode(), resultPortfolio.getRootNode())) {
          s_logger.debug("Found existing match at {}", resultDocument.getUniqueId());
          return resultDocument.getUniqueId();
        }
      }
    }
    if (document == null) {
      s_logger.debug("Adding to master");
      document = new PortfolioDocument(manageablePortfolio);
      document.setVisibility(visibility);
      document = _portfolios.add(document);
    } else {
      s_logger.debug("Updating {} within master", document.getUniqueId());
      // Retain existing visibility
      document.setPortfolio(manageablePortfolio);
      document = _portfolios.update(document);
    }
    s_logger.info("Portfolio '{}' saved as {}", manageablePortfolio.getName(), document.getUniqueId());
    return document.getUniqueId();
  }

}
