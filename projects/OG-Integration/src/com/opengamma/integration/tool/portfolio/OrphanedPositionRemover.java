/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.portfolio;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.master.portfolio.ManageablePortfolioNode;
import com.opengamma.master.portfolio.PortfolioDocument;
import com.opengamma.master.portfolio.PortfolioMaster;
import com.opengamma.master.portfolio.PortfolioSearchRequest;
import com.opengamma.master.portfolio.PortfolioSearchResult;
import com.opengamma.master.position.PositionDocument;
import com.opengamma.master.position.PositionMaster;
import com.opengamma.master.position.PositionSearchRequest;
import com.opengamma.master.position.PositionSearchResult;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.paging.Paging;
import com.opengamma.util.paging.PagingRequest;

/**
 * Deletes positions that are not currently in a portfolio
 */
public class OrphanedPositionRemover {
  
  private static final Logger s_logger = LoggerFactory.getLogger(OrphanedPositionRemover.class);
  
  private static final int PAGE_SIZE = 100;
  
  private final PortfolioMaster _portfolioMaster;
  private final PositionMaster _positionMaster;

  public OrphanedPositionRemover(PortfolioMaster portfolioMaster, PositionMaster positionMaster) {
    ArgumentChecker.notNull(positionMaster, "position master");
    ArgumentChecker.notNull(portfolioMaster, "portfolio master");
    _portfolioMaster = portfolioMaster;
    _positionMaster = positionMaster;
  }

  public void run() {
    final Set<ObjectId> validPositions = getValidPositions();
    final Set<UniqueId> orphanedPositions = getOrphanedPositions(validPositions);
    removePositions(orphanedPositions);
  }
  
  private void removePositions(Set<UniqueId> orphanedPositions) {
    s_logger.info("removing {} orphaned positions", orphanedPositions.size());
    for (UniqueId orphanId : orphanedPositions) {
      _positionMaster.remove(orphanId);
      s_logger.info("removed position {}", orphanId);
    }
  }

  private Set<UniqueId> getOrphanedPositions(Set<ObjectId> validPositions) {
    
    final Set<UniqueId> result = Sets.newHashSet();
    final int totalPositions = getTotalPositionSize();
    
    Paging paging = Paging.of(PagingRequest.ofPage(1, PAGE_SIZE), totalPositions);
    int totalPages = paging.getTotalPages();
    for (int i = 1; i <= totalPages; i++) {
      PositionSearchRequest searchRequest = new PositionSearchRequest();
      searchRequest.setPagingRequest(PagingRequest.ofPage(i, PAGE_SIZE));
      for (PositionDocument positionDocument : _positionMaster.search(searchRequest).getDocuments()) {
        UniqueId positionId = positionDocument.getPosition().getUniqueId();
        if (!validPositions.contains(positionId.getObjectId())) {
          result.add(positionId);
        }
      }
    }
    return result;
  }

  private int getTotalPositionSize() {
    PositionSearchRequest searchRequest = new PositionSearchRequest();
    searchRequest.setPagingRequest(PagingRequest.ONE);
    PositionSearchResult portfolioSearchResult = _positionMaster.search(searchRequest);
    return portfolioSearchResult.getPaging().getTotalItems();
  }

  private Set<ObjectId> getValidPositions() {
    
    final Set<ObjectId> result = Sets.newHashSet();
    final int totalPortfolios = getPortfolioTotalSize();
    Paging paging = Paging.of(PagingRequest.ofPage(1, PAGE_SIZE), totalPortfolios);
    int totalPages = paging.getTotalPages();
    for (int i = 1; i <= totalPages; i++) {
      PortfolioSearchRequest searchRequest = new PortfolioSearchRequest();
      searchRequest.setPagingRequest(PagingRequest.ofPage(i, PAGE_SIZE));
      for (PortfolioDocument portfolioDocument : _portfolioMaster.search(searchRequest).getDocuments()) {
        accumulatePositionIdentifiers(portfolioDocument.getPortfolio().getRootNode(), result);
      }
    }
    return result;
    
  }
  
  private int getPortfolioTotalSize() {
    PortfolioSearchRequest searchRequest = new PortfolioSearchRequest();
    searchRequest.setPagingRequest(PagingRequest.ONE);
    
    PortfolioSearchResult portfolioSearchResult = _portfolioMaster.search(searchRequest);
    return portfolioSearchResult.getPaging().getTotalItems();
  }

  private void accumulatePositionIdentifiers(final ManageablePortfolioNode node, final Set<ObjectId> positions) {
    positions.addAll(node.getPositionIds());
    
    for (ManageablePortfolioNode childNode : node.getChildNodes()) {
      accumulatePositionIdentifiers(childNode, positions);
    }
  }
  
}
