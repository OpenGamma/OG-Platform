/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */

package com.opengamma.examples.portfolioloader;

import java.util.ArrayList;
import java.util.Collection;

import com.opengamma.financial.portfolio.loader.LoaderContext;
import com.opengamma.master.portfolio.ManageablePortfolio;
import com.opengamma.master.portfolio.ManageablePortfolioNode;
import com.opengamma.master.portfolio.PortfolioDocument;
import com.opengamma.master.portfolio.PortfolioMaster;
import com.opengamma.master.position.ManageablePosition;
import com.opengamma.master.position.ManageableTrade;
import com.opengamma.master.position.PositionDocument;
import com.opengamma.master.position.PositionMaster;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.master.security.SecurityDocument;
import com.opengamma.master.security.SecurityMaster;
import com.opengamma.util.tuple.Triple;

/**
 * 
 */
public class PortfolioWriter {

  private String _portfolioName;
  private PortfolioMaster _portfolioMaster;
  private PositionMaster _positionMaster;
  private SecurityMaster _securityMaster;
  
  private Collection<ManageableSecurity> _securities = new ArrayList<ManageableSecurity>();
  private Collection<ManageablePosition> _positions = new ArrayList<ManageablePosition>(); // Trades?
  private Collection<ManageableTrade> _trades = new ArrayList<ManageableTrade>();

  public PortfolioWriter(String portfolioName, LoaderContext loaderContext) {
    _portfolioName = portfolioName;
    // Obtain access to the required masters
    _portfolioMaster = loaderContext.getPortfolioMaster();
    _positionMaster = loaderContext.getPositionMaster();
    _securityMaster = loaderContext.getSecurityMaster();
  }
  
  public PortfolioWriter(String portfolioName, PortfolioMaster portfolioMaster, PositionMaster positionMaster, SecurityMaster securityMaster) {
    _portfolioName = portfolioName;
    _portfolioMaster = portfolioMaster;
    _positionMaster = positionMaster;
    _securityMaster = securityMaster;
  }
  
  public void load(PortfolioLoader portfolioLoader) {
    
    Triple<Collection<ManageableTrade>, Collection<ManageablePosition>, Collection<ManageableSecurity>> imports = portfolioLoader.loadAll();
    
    // Import the securities into this buffer
    _securities.addAll(imports.getThird());
    
    // Import the positions into this buffer
    _positions.addAll(imports.getSecond());
    
    // Import the trades into this buffer
    _trades.addAll(imports.getFirst());
  }
  
  public void loadAll(Collection<PortfolioLoader> portfolioLoaders) {
    for (PortfolioLoader portfolioLoader : portfolioLoaders) {
      load(portfolioLoader);
    }
  }
  
  public void flush() {
    // Write the securities to the OG security master
    persistSecurities(_securities);
    
    // Create a new portfolio 
    // TODO add to existing portfolio, as an option
    final ManageablePortfolio portfolio = createEmptyPortfolio(_portfolioName);
    
    // Write the positions to the OG position master
    persistPositions(portfolio, _positions);
    
    // Write the trades to the OG position master
    persistTrades(portfolio, _trades);
  }

  public void prettyPrint() {
    System.out.println("Securities:");
    for (ManageableSecurity s : _securities) {
      System.out.println(s.toString());
    }

    System.out.println("Positions:");
    for (ManageablePosition p : _positions) {
      System.out.println(p.toString());
    }
    
    System.out.println("Trades:");
    for (ManageableTrade t : _trades) {
      System.out.println(t.toString());
    }
  }

  private void persistSecurities(Collection<ManageableSecurity> securities) {
    for (ManageableSecurity security : securities) {
      SecurityDocument doc = new SecurityDocument(security);
      _securityMaster.add(doc);
    }
  }

  private void persistPositions(ManageablePortfolio portfolio, Collection<ManageablePosition> positions) {
    ManageablePortfolioNode rootNode = portfolio.getRootNode();
    for (ManageablePosition position : positions) {
      PositionDocument addedPosition = _positionMaster.add(new PositionDocument(position));
      rootNode.addPosition(addedPosition.getUniqueId());
    }
    _portfolioMaster.add(new PortfolioDocument(portfolio));
  }
  
  private void persistTrades(ManageablePortfolio portfolio, Collection<ManageableTrade> trades) {
    
  }

  private ManageablePortfolio createEmptyPortfolio(String portfolioName) {
    ManageablePortfolio portfolio = new ManageablePortfolio(portfolioName);
    ManageablePortfolioNode rootNode = portfolio.getRootNode();
    rootNode.setName("Root");
    return portfolio;
  }

}
