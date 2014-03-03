/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.examples.simulated.tutorial;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.opengamma.component.tool.AbstractTool;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.financial.tool.ToolContext;
import com.opengamma.id.ExternalId;
import com.opengamma.master.portfolio.ManageablePortfolio;
import com.opengamma.master.portfolio.ManageablePortfolioNode;
import com.opengamma.master.portfolio.PortfolioDocument;
import com.opengamma.master.portfolio.PortfolioMaster;
import com.opengamma.master.position.ManageablePosition;
import com.opengamma.master.position.PositionDocument;
import com.opengamma.master.position.PositionMaster;
import com.opengamma.master.security.SecurityDocument;
import com.opengamma.master.security.SecurityMaster;
import com.opengamma.util.money.Currency;

/**
 * Load a portfolio of {@link Tutorial1Security} instances for testing.
 * <p>
 * This creates a set of security instances from hard coded values, stores them in the security master and then creates a portfolio containing a position in each.
 */
public class Tutorial1SecurityLoader extends AbstractTool<ToolContext> {

  /**
   * Main method to run the tool.
   * 
   * @param args  the standard tool arguments, not null
   */
  public static void main(final String[] args) { // CSIGNORE
    new Tutorial1SecurityLoader().invokeAndTerminate(args);
  }

  //-------------------------------------------------------------------------
  /**
   * Creates five sample instances of {@link Tutorial1Security}.
   * <p>
   * Our example asset class has an underlying security. For demonstration purposes we will use the first five from the example equities created during installation of the example server.
   *
   * @return the example security instances, not null and not containing null
   */
  protected List<Tutorial1Security> loadSecurities() {
    final List<Tutorial1Security> securities = new ArrayList<Tutorial1Security>();
    securities.add(new Tutorial1Security("Example 1", ExternalId.of("Tutorial", "1").toBundle(), Currency.USD, ExternalId.of(ExternalSchemes.OG_SYNTHETIC_TICKER, "HD")));
    securities.add(new Tutorial1Security("Example 2", ExternalId.of("Tutorial", "2").toBundle(), Currency.USD, ExternalId.of(ExternalSchemes.OG_SYNTHETIC_TICKER, "ARG")));
    securities.add(new Tutorial1Security("Example 3", ExternalId.of("Tutorial", "3").toBundle(), Currency.USD, ExternalId.of(ExternalSchemes.OG_SYNTHETIC_TICKER, "IPG")));
    securities.add(new Tutorial1Security("Example 4", ExternalId.of("Tutorial", "4").toBundle(), Currency.USD, ExternalId.of(ExternalSchemes.OG_SYNTHETIC_TICKER, "RSG")));
    securities.add(new Tutorial1Security("Example 5", ExternalId.of("Tutorial", "5").toBundle(), Currency.USD, ExternalId.of(ExternalSchemes.OG_SYNTHETIC_TICKER, "M")));
    return securities;
  }

  @Override
  protected void doRun() {
    final SecurityMaster securities = getToolContext().getSecurityMaster();
    final PositionMaster positions = getToolContext().getPositionMaster();
    final PortfolioMaster portfolios = getToolContext().getPortfolioMaster();
    final ManageablePortfolio portfolio = new ManageablePortfolio("Tutorial 1");
    final ManageablePortfolioNode root = portfolio.getRootNode();
    final Random rnd = new Random();
    root.setName("Root");
    for (final Tutorial1Security security : loadSecurities()) {
      securities.add(new SecurityDocument(security.toRawSecurity()));
      final ManageablePosition position = new ManageablePosition(new BigDecimal(100 + rnd.nextInt(900)), security.getExternalIdBundle());
      root.addPosition(positions.add(new PositionDocument(position)));
    }
    portfolios.add(new PortfolioDocument(portfolio));
  }

}
