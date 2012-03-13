/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.production.tool.portfolio;

import java.math.BigDecimal;

import org.apache.commons.lang.math.RandomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.core.security.SecurityUtils;
import com.opengamma.financial.security.bond.BondSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.master.portfolio.ManageablePortfolio;
import com.opengamma.master.portfolio.ManageablePortfolioNode;
import com.opengamma.master.portfolio.PortfolioDocument;
import com.opengamma.master.position.ManageablePosition;
import com.opengamma.master.position.PositionDocument;
import com.opengamma.master.security.SecurityDocument;
import com.opengamma.master.security.SecuritySearchRequest;
import com.opengamma.master.security.SecuritySearchResult;
import com.opengamma.production.tool.AbstractProductionTool;

/**
 * Loads a demo bond portfolio.
 */
public class DemoBondPortfolioLoader extends AbstractProductionTool {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(DemoBondPortfolioLoader.class);

  /**
   * The name of the portfolio.
   */
  private static final String PORTFOLIO_NAME = "Test Bond Portfolio";

  //-------------------------------------------------------------------------
  /**
   * Main method to run the tool.
   * No arguments are needed.
   * 
   * @param args  the arguments, unused
   */
  public static void main(String[] args) {  // CSIGNORE
    new DemoBondPortfolioLoader().initAndRun(args);
    System.exit(0);
  }

  //-------------------------------------------------------------------------
  /**
   * Loads the portfolio.
   */
  @Override 
  protected void doRun() {
    // load all bond securities
    SecuritySearchRequest secSearch = new SecuritySearchRequest();
    secSearch.setFullDetail(false);
    secSearch.setSecurityType(BondSecurity.SECURITY_TYPE);
    SecuritySearchResult securities = getToolContext().getSecurityMaster().search(secSearch);
    s_logger.info("Found {} securities", securities.getDocuments().size());
    
    // create shell portfolio
    ManageablePortfolio portfolio = new ManageablePortfolio(PORTFOLIO_NAME);
    ManageablePortfolioNode rootNode = portfolio.getRootNode();
    rootNode.setName("Root");
    for (SecurityDocument shellDoc : securities.getDocuments()) {
      s_logger.warn("Loading security {} {}", shellDoc.getUniqueId(), shellDoc.getSecurity().getName());
      SecurityDocument doc = getToolContext().getSecurityMaster().get(shellDoc.getUniqueId());
      BondSecurity sec = (BondSecurity) doc.getSecurity();
      
      String domicile = sec.getIssuerDomicile();
      String issuerType = sec.getIssuerType();
      String issuer = sec.getIssuerName();
      
      // create portfolio structure
      ManageablePortfolioNode domicileNode = rootNode.findNodeByName(domicile);
      if (domicileNode == null) {
        s_logger.warn("Creating node for domicile {}", domicile);
        domicileNode = new ManageablePortfolioNode(domicile);
        rootNode.addChildNode(domicileNode);
      }
      ManageablePortfolioNode issuerTypeNode = domicileNode.findNodeByName(issuerType);
      if (issuerTypeNode == null) {
        s_logger.warn("Creating node for issuer type {}", issuerType);
        issuerTypeNode = new ManageablePortfolioNode(issuerType);
        domicileNode.addChildNode(issuerTypeNode);
      }
      ManageablePortfolioNode issuerNode = issuerTypeNode.findNodeByName(issuer);
      if (issuerNode == null) {
        s_logger.warn("Creating node for isssuer {}", issuer);
        issuerNode = new ManageablePortfolioNode(issuer);
        issuerTypeNode.addChildNode(issuerNode);
      }
      
      // add position
      s_logger.warn("Creating position {}", sec);
      int shares = (RandomUtils.nextInt(490) + 10) * 10;
      ExternalId buid = sec.getExternalIdBundle().getExternalId(SecurityUtils.BLOOMBERG_BUID);
      ExternalIdBundle bundle;
      if (buid != null) {
        bundle = ExternalIdBundle.of(buid);
      } else {
        bundle = sec.getExternalIdBundle();
      }
      ManageablePosition position = new ManageablePosition(BigDecimal.valueOf(shares), bundle);
      PositionDocument addedPosition = getToolContext().getPositionMaster().add(new PositionDocument(position));
      
      // add position reference to portfolio
      issuerNode.addPosition(addedPosition.getUniqueId());
    }
    getToolContext().getPortfolioMaster().add(new PortfolioDocument(portfolio));
  }

}
