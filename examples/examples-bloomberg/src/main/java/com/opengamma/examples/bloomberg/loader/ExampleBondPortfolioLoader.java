/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.examples.bloomberg.loader;

import java.math.BigDecimal;

import org.apache.commons.lang.math.RandomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.component.tool.AbstractTool;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.financial.security.bond.BondSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.integration.tool.IntegrationToolContext;
import com.opengamma.master.portfolio.ManageablePortfolio;
import com.opengamma.master.portfolio.ManageablePortfolioNode;
import com.opengamma.master.portfolio.PortfolioDocument;
import com.opengamma.master.position.ManageablePosition;
import com.opengamma.master.position.PositionDocument;
import com.opengamma.master.security.SecurityDocument;
import com.opengamma.master.security.SecurityMaster;
import com.opengamma.master.security.SecuritySearchRequest;
import com.opengamma.master.security.impl.SecuritySearchIterator;
import com.opengamma.scripts.Scriptable;

/**
 * Example code to load and aggregate a bond portfolio.
 * <p>
 * This loads all bond securities previously stored in the master and
 * categorizes them in a hierarchy by domicile, then issuer type, then issuer name.
 * Note this requires that you've already populated your security master with 
 * some bond securities, so you typically need some static market data lookup 
 * service.
 */
@Scriptable
public class ExampleBondPortfolioLoader extends AbstractTool<IntegrationToolContext> {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(ExampleBondPortfolioLoader.class);

  /**
   * The name of the portfolio.
   */
  private static final String PORTFOLIO_NAME = "Bond Portfolio";

  //-------------------------------------------------------------------------
  /**
   * Main method to run the tool.
   * 
   * @param args  the standard tool arguments, not null
   */
  public static void main(String[] args) {  // CSIGNORE
    new ExampleBondPortfolioLoader().invokeAndTerminate(args);
  }

  //-------------------------------------------------------------------------
  /**
   * Loads the test portfolio into the position master.
   */
  @Override 
  protected void doRun() {
    // create shell portfolio
    final ManageablePortfolio portfolio = createPortfolio();
    final ManageablePortfolioNode rootNode = portfolio.getRootNode();
    
    // add each security to the portfolio
    for (SecurityDocument shellDoc : loadAllBondSecurities()) {
      // load the full detail of the security
      final BondSecurity security = loadFullSecurity(shellDoc);
      
      // build the tree structure
      final ManageablePortfolioNode issuerNode = buildPortfolioTree(rootNode, security);
      
      // create the position and add it to the master
      final ManageablePosition position = createPosition(security);
      final PositionDocument addedPosition = addPosition(position);
      
      // add the position reference (the unique identifier) to portfolio
      issuerNode.addPosition(addedPosition.getUniqueId());
    }
    
    // adds the complete tree structure to the master
    addPortfolio(portfolio);
  }

  /**
   * Loads all securities from the master.
   * <p>
   * This obtains an iterator over the securities.
   * This avoids loading the potentially large number of bonds into memory.
   * 
   * @return iterable of all bond securities in the security master, not null
   */
  protected Iterable<SecurityDocument> loadAllBondSecurities() {
    SecuritySearchRequest secSearch = new SecuritySearchRequest();
    secSearch.setFullDetail(false);
    secSearch.setSecurityType(BondSecurity.SECURITY_TYPE);
    SecurityMaster securityMaster = getToolContext().getSecurityMaster();
    return SecuritySearchIterator.iterable(securityMaster, secSearch);
  }

  /**
   * Loads the full detail of the security.
   * <p>
   * The search used an optimization where the "full detail" of the security was not loaded.
   * It is thus necessary to load the full information about the security before processing.
   * The unique identifier is the key to loading the security.
   * 
   * @param shellDoc  the document to load, not null
   * @return the equity security, not null
   */
  protected BondSecurity loadFullSecurity(SecurityDocument shellDoc) {
    s_logger.info("Loading security {} {}", shellDoc.getUniqueId(), shellDoc.getSecurity().getName());
    SecurityDocument doc = getToolContext().getSecurityMaster().get(shellDoc.getUniqueId());
    BondSecurity sec = (BondSecurity) doc.getSecurity();
    return sec;
  }

  /**
   * Create a shell portfolio.
   * <p>
   * This creates the portfolio and the root of the tree structure that holds the positions.
   * Subsequent methods then populate the tree.
   * 
   * @return the shell portfolio, not null
   */
  protected ManageablePortfolio createPortfolio() {
    ManageablePortfolio portfolio = new ManageablePortfolio(PORTFOLIO_NAME);
    ManageablePortfolioNode rootNode = portfolio.getRootNode();
    rootNode.setName("Root");
    return portfolio;
  }

  /**
   * Create the portfolio tree structure based.
   * <p>
   * This uses the domicile, issuer type and issuer name to create a tree structure.
   * The position will be added to the lowest child node, which is returned.
   * 
   * @param rootNode  the root node of the tree, not null
   * @param security  the bond security, not null
   * @return the lowest child node, not null
   */
  protected ManageablePortfolioNode buildPortfolioTree(ManageablePortfolioNode rootNode, BondSecurity security) {
    String domicile = security.getIssuerDomicile();
    ManageablePortfolioNode domicileNode = rootNode.findNodeByName(domicile);
    if (domicileNode == null) {
      s_logger.info("Creating node for domicile {}", domicile);
      domicileNode = new ManageablePortfolioNode(domicile);
      rootNode.addChildNode(domicileNode);
    }
    
    String issuerType = security.getIssuerType();
    ManageablePortfolioNode issuerTypeNode = domicileNode.findNodeByName(issuerType);
    if (issuerTypeNode == null) {
      s_logger.info("Creating node for issuer type {}", issuerType);
      issuerTypeNode = new ManageablePortfolioNode(issuerType);
      domicileNode.addChildNode(issuerTypeNode);
    }
    
    String issuerName = security.getIssuerName();
    ManageablePortfolioNode issuerNode = issuerTypeNode.findNodeByName(issuerName);
    if (issuerNode == null) {
      s_logger.info("Creating node for isssuer {}", issuerName);
      issuerNode = new ManageablePortfolioNode(issuerName);
      issuerTypeNode.addChildNode(issuerNode);
    }
    return issuerNode;
  }

  /**
   * Create a position of a random number of shares.
   * <p>
   * This creates the position using a random number of units.
   * 
   * @param security  the security to add a position for, not null
   * @return the position, not null
   */
  protected ManageablePosition createPosition(BondSecurity security) {
    s_logger.info("Creating position {}", security);
    int shares = (RandomUtils.nextInt(490) + 10) * 10;
    ExternalId buid = security.getExternalIdBundle().getExternalId(ExternalSchemes.BLOOMBERG_BUID);
    ExternalIdBundle bundle;
    if (buid != null) {
      bundle = ExternalIdBundle.of(buid);
    } else {
      bundle = security.getExternalIdBundle();
    }
    return new ManageablePosition(BigDecimal.valueOf(shares), bundle);
  }

  /**
   * Adds the position to the master.
   * 
   * @param position  the position to add, not null
   * @return the added document, not null
   */
  protected PositionDocument addPosition(ManageablePosition position) {
    return getToolContext().getPositionMaster().add(new PositionDocument(position));
  }

  /**
   * Adds the portfolio to the master.
   * 
   * @param portfolio  the portfolio to add, not null
   * @return the added document, not null
   */
  protected PortfolioDocument addPortfolio(ManageablePortfolio portfolio) {
    return getToolContext().getPortfolioMaster().add(new PortfolioDocument(portfolio));
  }

}
