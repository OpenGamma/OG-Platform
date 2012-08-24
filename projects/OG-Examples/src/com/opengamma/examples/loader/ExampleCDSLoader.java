/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.examples.loader;

import java.math.BigDecimal;

import javax.time.calendar.LocalDate;
import javax.time.calendar.LocalDateTime;
import javax.time.calendar.TimeZone;
import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.math.RandomUtils;

import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.core.security.Security;
import com.opengamma.examples.tool.AbstractExampleTool;

import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.convention.frequency.SimpleFrequency;
import com.opengamma.financial.convention.yield.SimpleYieldConvention;
import com.opengamma.financial.security.bond.BondSecurity;
import com.opengamma.financial.security.bond.GovernmentBondSecurity;
import com.opengamma.financial.security.cds.CDSSecurity;
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
import com.opengamma.util.time.Expiry;

/**
 * Load example CDS security and store for testing
 * @author Martin Traverse
 * @see CDSSecurity
 * @see CDSSimplePresentValueFunction
 */
public class ExampleCDSLoader extends AbstractExampleTool {

  public static void main(String[] args) {  // CSIGNORE
    
    new ExampleCDSLoader().initAndRun(args);
    System.exit(0);
  }

  @Override
  protected void doRun() throws Exception {
    
    final SecurityMaster secMaster = getToolContext().getSecurityMaster();
    
    final ManageableSecurity und = makeOneBond();
    final SecurityDocument undDoc = new SecurityDocument(und);
    secMaster.add(undDoc);
    
    final ManageableSecurity cds = makeOneCDS(und.externalIdBundle().get().getExternalId(ExternalSchemes.OG_SYNTHETIC_TICKER));
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
  
  private CDSSecurity makeOneCDS(final ExternalId underlying) {
    
    ZonedDateTime maturity = ZonedDateTime.of(2020, 12, 20, 0, 0, 0, 0, TimeZone.UTC);
    SimpleFrequency frequency = SimpleFrequency.ANNUAL;
    DayCount dayCount = DayCountFactory.INSTANCE.getDayCount("Actual/360");
    BusinessDayConvention businessDayConvention = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Following");
    final CDSSecurity cds1 = new CDSSecurity(1.0, 0.6, 0.0025, Currency.GBP, maturity, ZonedDateTime.now(), 
                                             frequency, 
                                             dayCount, 
                                             businessDayConvention,  
                                             underlying);
    cds1.addExternalId(ExternalId.of(ExternalSchemes.OG_SYNTHETIC_TICKER, "TEST_CDS_00001--US912828KY53-A"));
    cds1.setName("TEST CDS 00001");
    
    return cds1;
  }
  
  private BondSecurity makeOneBond() {
    
    final GovernmentBondSecurity bond1 = new GovernmentBondSecurity("US TREASURY N/B", "Sovereign", "US", "US GOVERNMENT", 
        Currency.USD, SimpleYieldConvention.US_STREET, 
        new Expiry(ZonedDateTime.of(LocalDateTime.of(2013, 12, 15, 16, 0), TimeZone.UTC)), "FIXED", 2.625, 
        SimpleFrequency.SEMI_ANNUAL, DayCountFactory.INSTANCE.getDayCount("Actual/Actual ICMA"), 
        ZonedDateTime.of(LocalDateTime.of(2009, 5, 30, 18, 0), TimeZone.UTC), 
        ZonedDateTime.of(LocalDateTime.of(2011, 5, 28, 11, 0), TimeZone.UTC), 
        ZonedDateTime.of(LocalDateTime.of(2009, 12, 31, 11, 0), TimeZone.UTC), 
        99.651404, 3.8075E10, 100.0, 100.0, 100.0, 100.0);
    bond1.addExternalId(ExternalId.of(ExternalSchemes.OG_SYNTHETIC_TICKER, "US912828KY53-A"));
    bond1.setName("T 2 5/8 06/30/14 A");
    
    return bond1;
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
