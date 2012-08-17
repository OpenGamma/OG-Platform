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

/**
 * Deletes positions that are not currently in a portfolio
 */
public class OrphanedPositionRemover {
  
  private static final Logger s_logger = LoggerFactory.getLogger(OrphanedPositionRemover.class);
  
  private final PortfolioMaster _portfolioMaster;
  private final PositionMaster _positionMaster;

  public OrphanedPositionRemover(PortfolioMaster portfolioMaster, PositionMaster positionMaster) {
    ArgumentChecker.notNull(positionMaster, "position master");
    ArgumentChecker.notNull(portfolioMaster, "portfolio master");
    _portfolioMaster = portfolioMaster;
    _positionMaster = positionMaster;
  }

  public void run() {
    Set<ObjectId> validPositions = getValidPositions();
    Set<UniqueId> orphanedPositions = getOrphanedPositions(validPositions);
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
    Set<UniqueId> orphanedPositions = Sets.newHashSet();
    PositionSearchResult positionSearchResult = _positionMaster.search(new PositionSearchRequest());
    for (PositionDocument positionDocument : positionSearchResult.getDocuments()) {
      UniqueId positionId = positionDocument.getPosition().getUniqueId();
      if (!validPositions.contains(positionId.getObjectId())) {
        orphanedPositions.add(positionId);
      }
    }
    return orphanedPositions;
  }

  private Set<ObjectId> getValidPositions() {
    final Set<ObjectId> validPositions = Sets.newHashSet();
    PortfolioSearchResult portfolioSearchResult = _portfolioMaster.search(new PortfolioSearchRequest());
    
    for (PortfolioDocument portfolioDocument : portfolioSearchResult.getDocuments()) {
      accumulatePositions(portfolioDocument.getPortfolio().getRootNode(), validPositions);
    }
    return validPositions;
  }
  
  private void accumulatePositions(final ManageablePortfolioNode node, final Set<ObjectId> positions) {
    positions.addAll(node.getPositionIds());
    
    for (ManageablePortfolioNode childNode : node.getChildNodes()) {
      accumulatePositions(childNode, positions);
    }
  }
  
}
