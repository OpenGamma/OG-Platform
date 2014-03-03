/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.examples.simulated.loader;

import java.math.BigDecimal;

import org.apache.commons.lang.math.RandomUtils;
import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.component.tool.AbstractTool;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.core.security.Security;
import com.opengamma.financial.convention.StubType;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.financial.convention.frequency.SimpleFrequency;
import com.opengamma.financial.security.cds.CDSSecurity;
import com.opengamma.financial.tool.ToolContext;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
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
import com.opengamma.util.money.Currency;

/**
 * Load example CDS security and store for testing
 * @author Martin Traverse
 * @see CDSSecurity
 * @see CDSSimplePresentValueFunction
 */
public class ExampleCDSLoader extends AbstractTool<ToolContext> {

  private int _counter;

  //-------------------------------------------------------------------------
  /**
   * Main method to run the tool.
   * 
   * @param args  the standard tool arguments, not null
   */
  public static void main(String[] args) {  // CSIGNORE
    new ExampleCDSLoader().invokeAndTerminate(args);
  }

  //-------------------------------------------------------------------------
  @Override
  protected void doRun() throws Exception {
    
    final SecurityMaster secMaster = getToolContext().getSecurityMaster();
    
    final ManageableSecurity cds = makeOneCDS();
    final SecurityDocument cdsDoc = new SecurityDocument(cds);
    
    secMaster.add(cdsDoc);
    
    portfolioWithSecurity(cds, "Test CDS Port 1");
  }
  
  private void portfolioWithSecurity(Security security, String portfolioName) {

    
    final PositionMaster posMaster = getToolContext().getPositionMaster();
    final PortfolioMaster portMaster = getToolContext().getPortfolioMaster();
    
    final ManageablePosition position = makePositionAndTrade(security);
    final PositionDocument positionDoc = new PositionDocument(position);
    posMaster.add(positionDoc);
    
    ManageablePortfolio portfolio = new ManageablePortfolio(portfolioName);
    ManageablePortfolioNode rootNode = portfolio.getRootNode();
    rootNode.setName("Root");
    rootNode.addPosition(position.getUniqueId());
    
    PortfolioDocument portfolioDoc = new PortfolioDocument(portfolio);
    portMaster.add(portfolioDoc);

  }
  
  private CDSSecurity makeOneCDS() {
    
    ZonedDateTime maturity = LocalDateTime.of(2020, 12, 20, 0, 0, 0, 0).atZone(ZoneOffset.UTC);
    ZonedDateTime startDate = LocalDateTime.of(2010, 12, 20, 0, 0, 0, 0).atZone(ZoneOffset.UTC);
    SimpleFrequency frequency = SimpleFrequency.ANNUAL;
    DayCount dayCount = DayCounts.ACT_360;
    BusinessDayConvention businessDayConvention = BusinessDayConventions.FOLLOWING;
    final CDSSecurity cds1 = new CDSSecurity(1.0, 0.6, 0.4, Currency.USD, maturity, startDate,
                                             frequency, 
                                             dayCount, 
                                             businessDayConvention,  
                                             StubType.SHORT_START, 3,
                                             "US Treasury", Currency.USD, "Senior", "No Restructuring");
    cds1.addExternalId(ExternalId.of(ExternalSchemes.OG_SYNTHETIC_TICKER, "TEST_CDS_00001--US912828KY53-A"));
    cds1.setName("TEST CDS" + _counter++);
    
    return cds1;
  }
  
  protected ManageablePosition makePositionAndTrade(Security security) {

    int shares = (RandomUtils.nextInt(490) + 10) * 10;
    ExternalIdBundle bundle = security.getExternalIdBundle();
    
    ManageablePosition position = new ManageablePosition(BigDecimal.valueOf(shares), bundle);
    ManageableTrade trade = new ManageableTrade(BigDecimal.valueOf(shares), bundle, LocalDate.of(2010, 12, 3), null, ExternalId.of("CPARTY", "BACS"));
    position.addTrade(trade);
   
    return position;
  }

}
