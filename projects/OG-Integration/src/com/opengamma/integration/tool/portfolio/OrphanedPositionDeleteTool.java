/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.portfolio;

import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;
import com.opengamma.component.factory.tool.RemoteComponentFactoryToolContextAdapter;
import com.opengamma.component.tool.AbstractComponentTool;
import com.opengamma.financial.tool.ToolContext;
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

/**
 * Deletes positions that are not currently in a portfolio
 */
public class OrphanedPositionDeleteTool extends AbstractComponentTool {
  
  private static final Logger s_logger = LoggerFactory.getLogger(OrphanedPositionDeleteTool.class);
  /**
   * Main method to run the tool.
   */
  public static void main(String[] args) { // CSIGNORE
    new OrphanedPositionDeleteTool().initAndRun(args);
    System.exit(0);
  }

  @Override
  protected void doRun() throws Exception {
    ToolContext toolContext = new RemoteComponentFactoryToolContextAdapter(getRemoteComponentFactory());
    PortfolioMaster portfolioMaster = toolContext.getPortfolioMaster();
    
    PortfolioSearchRequest request = new PortfolioSearchRequest();  
    Set<ObjectId> validPositions = Sets.newHashSet();
    
    PortfolioSearchResult portfolioSearchResult = portfolioMaster.search(request);
    
    for (PortfolioDocument portfolioDocument : portfolioSearchResult.getDocuments()) {
      validPositions.addAll(PositionAccumulator.getAccumulatedPositions(portfolioDocument.getPortfolio().getRootNode()));
    }
        
    PositionMaster positionMaster = toolContext.getPositionMaster();
    
    s_logger.info("removing orphaned positions");
    
    PositionSearchResult positionSearchResult = positionMaster.search(new PositionSearchRequest());
    for (PositionDocument positionDocument : positionSearchResult.getDocuments()) {
      UniqueId positionId = positionDocument.getPosition().getUniqueId();
      if (!validPositions.contains(positionId.getObjectId())) {
        positionMaster.remove(positionId);
        s_logger.info("removed position {}", positionId);
      }
    }
  }
  
  private static final class PositionAccumulator {
    
    /**
     * The set of positions.
     */
    private final Set<ObjectId> _positions = new HashSet<ObjectId>();
    
    private PositionAccumulator(final ManageablePortfolioNode node) {
      _positions.addAll(node.getPositionIds());
      for (ManageablePortfolioNode childNode : node.getChildNodes()) {
        _positions.addAll(PositionAccumulator.getAccumulatedPositions(childNode));
      }
    }

    public static Set<ObjectId> getAccumulatedPositions(final ManageablePortfolioNode node) {
      return new PositionAccumulator(node).getPositions();
    }

    /**
     * Gets the positions.
     * @return the positions
     */
    public Set<ObjectId> getPositions() {
      return _positions;
    } 
  }
    
}
