/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.examples.loader;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.time.calendar.LocalDateTime;
import javax.time.calendar.TimeZone;
import javax.time.calendar.ZonedDateTime;

import com.opengamma.core.region.RegionUtils;
import com.opengamma.core.security.SecurityUtils;
import com.opengamma.examples.tool.AbstractTool;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.convention.frequency.SimpleFrequency;
import com.opengamma.financial.convention.yield.SimpleYieldConvention;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.bond.BondSecurity;
import com.opengamma.financial.security.bond.GovernmentBondSecurity;
import com.opengamma.financial.security.capfloor.CapFloorSecurity;
import com.opengamma.financial.security.fra.FRASecurity;
import com.opengamma.financial.security.future.BondFutureDeliverable;
import com.opengamma.financial.security.future.BondFutureSecurity;
import com.opengamma.financial.security.future.InterestRateFutureSecurity;
import com.opengamma.financial.security.fx.FXForwardSecurity;
import com.opengamma.financial.security.option.BarrierDirection;
import com.opengamma.financial.security.option.BarrierType;
import com.opengamma.financial.security.option.EuropeanExerciseType;
import com.opengamma.financial.security.option.FXBarrierOptionSecurity;
import com.opengamma.financial.security.option.FXOptionSecurity;
import com.opengamma.financial.security.option.MonitoringType;
import com.opengamma.financial.security.option.SamplingFrequency;
import com.opengamma.financial.security.option.SwaptionSecurity;
import com.opengamma.financial.security.swap.FixedInterestRateLeg;
import com.opengamma.financial.security.swap.FloatingInterestRateLeg;
import com.opengamma.financial.security.swap.FloatingRateType;
import com.opengamma.financial.security.swap.InterestRateNotional;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ExternalScheme;
import com.opengamma.master.portfolio.ManageablePortfolio;
import com.opengamma.master.portfolio.ManageablePortfolioNode;
import com.opengamma.master.portfolio.PortfolioDocument;
import com.opengamma.master.portfolio.PortfolioMaster;
import com.opengamma.master.position.ManageablePosition;
import com.opengamma.master.position.PositionDocument;
import com.opengamma.master.position.PositionMaster;
import com.opengamma.master.security.SecurityDocument;
import com.opengamma.master.security.SecurityMaster;
import com.opengamma.util.GUIDGenerator;
import com.opengamma.util.i18n.Country;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Expiry;

/**
 * 
 */
public class ExampleMixedPortfolioLoader extends AbstractTool {
  /**
   * Example mixed portfolio name
   */
  public static final String PORTFOLIO_NAME = "Example Mixed Portfolio";
  private static final String ID_SCHEME = "MIXED_PORFOLIO_LOADER";
  private static final DayCount DAY_COUNT = DayCountFactory.INSTANCE.getDayCount("Actual/360");
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Following");
  
  public static void main(String[] args) { //CSIGNORE
    if (init()) {
      new ExampleMixedPortfolioLoader().run();
    }
    System.exit(0);
  }
  
  private void persistToPortfolio() {
    PortfolioMaster portfolioMaster = getToolContext().getPortfolioMaster();
    PositionMaster positionMaster = getToolContext().getPositionMaster();
    SecurityMaster securityMaster = getToolContext().getSecurityMaster();
    ManageablePortfolioNode rootNode = new ManageablePortfolioNode(PORTFOLIO_NAME);
    ManageablePortfolio portfolio = new ManageablePortfolio(PORTFOLIO_NAME, rootNode);
    PortfolioDocument portfolioDoc = new PortfolioDocument();
    portfolioDoc.setPortfolio(portfolio);
    
    final Collection<SwapSecurity> iborSwaps = getIborSwaps();
    final ManageablePortfolioNode childPortfolio1 = new ManageablePortfolioNode("Ibor swaps");
    for (final SwapSecurity swap : iborSwaps) {
      SecurityDocument toAddDoc = new SecurityDocument();
      toAddDoc.setSecurity(swap);
      securityMaster.add(toAddDoc);
      ManageablePosition position = new ManageablePosition(BigDecimal.ONE, swap.getExternalIdBundle());
      PositionDocument addedDoc = positionMaster.add(new PositionDocument(position));
      childPortfolio1.addPosition(addedDoc.getUniqueId());
    }
    rootNode.addChildNode(childPortfolio1);
    
    final Collection<SwapSecurity> cmSwaps = getCMSwaps();
    final ManageablePortfolioNode childPortfolio2 = new ManageablePortfolioNode("CM swaps");
    for (final SwapSecurity swap : cmSwaps) {
      SecurityDocument toAddDoc = new SecurityDocument();
      toAddDoc.setSecurity(swap);
      securityMaster.add(toAddDoc);
      ManageablePosition position = new ManageablePosition(BigDecimal.ONE, swap.getExternalIdBundle());
      PositionDocument addedDoc = positionMaster.add(new PositionDocument(position));
      childPortfolio2.addPosition(addedDoc.getUniqueId());
    }
    rootNode.addChildNode(childPortfolio2);
    
    final Collection<FinancialSecurity> simpleFI = getSimpleFixedIncome();
    final ManageablePortfolioNode childPortfolio3 = new ManageablePortfolioNode("Fixed income");
    for (final FinancialSecurity fi : simpleFI) {
      SecurityDocument toAddDoc = new SecurityDocument();
      toAddDoc.setSecurity(fi);
      securityMaster.add(toAddDoc);
      ManageablePosition position = new ManageablePosition(BigDecimal.ONE, fi.getExternalIdBundle());
      PositionDocument addedDoc = positionMaster.add(new PositionDocument(position));
      childPortfolio3.addPosition(addedDoc.getUniqueId());
    }
    rootNode.addChildNode(childPortfolio3);
    
    final Collection<FXForwardSecurity> simpleFX = getSimpleFX();
    final ManageablePortfolioNode childPortfolio4 = new ManageablePortfolioNode("FX forward");
    for (final FXForwardSecurity fxForward : simpleFX) {
      SecurityDocument toAddDoc = new SecurityDocument();
      toAddDoc.setSecurity(fxForward);
      securityMaster.add(toAddDoc);
      ManageablePosition position = new ManageablePosition(BigDecimal.ONE, fxForward.getExternalIdBundle());
      PositionDocument addedDoc = positionMaster.add(new PositionDocument(position));
      childPortfolio4.addPosition(addedDoc.getUniqueId());
    }
    rootNode.addChildNode(childPortfolio4);
    
    final Collection<FinancialSecurity> optionFX = getFXOptions();
    final ManageablePortfolioNode childPortfolio5 = new ManageablePortfolioNode("FX options");
    for (final FinancialSecurity f : optionFX) {
      SecurityDocument toAddDoc = new SecurityDocument();
      toAddDoc.setSecurity(f);
      securityMaster.add(toAddDoc);
      ManageablePosition position = new ManageablePosition(BigDecimal.ONE, f.getExternalIdBundle());
      PositionDocument addedDoc = positionMaster.add(new PositionDocument(position));
      childPortfolio5.addPosition(addedDoc.getUniqueId());
    }
    rootNode.addChildNode(childPortfolio5);
    
    final Collection<FinancialSecurity> bonds = getBonds();
    final ManageablePortfolioNode bondPortfolio = new ManageablePortfolioNode("Bonds");
    for (final FinancialSecurity security : bonds) {
      SecurityDocument toAddDoc = new SecurityDocument();
      toAddDoc.setSecurity(security);
      securityMaster.add(toAddDoc);
      ManageablePosition position = new ManageablePosition(BigDecimal.ONE, security.getExternalIdBundle());
      PositionDocument addedDoc = positionMaster.add(new PositionDocument(position));
      bondPortfolio.addPosition(addedDoc.getUniqueId());
    }
    rootNode.addChildNode(bondPortfolio);
    
    final Collection<SwaptionSecurity> swaptions = getSwaptions();
    final ManageablePortfolioNode swaptionPortfolio = new ManageablePortfolioNode("Swaptions");
    for (final FinancialSecurity security : swaptions) {
      SecurityDocument toAddDoc = new SecurityDocument();
      toAddDoc.setSecurity(security);
      securityMaster.add(toAddDoc);
      ManageablePosition position = new ManageablePosition(BigDecimal.ONE, security.getExternalIdBundle());
      PositionDocument addedDoc = positionMaster.add(new PositionDocument(position));
      swaptionPortfolio.addPosition(addedDoc.getUniqueId());
    }
    rootNode.addChildNode(swaptionPortfolio);
    
    addPortfolioNode(rootNode, getIborCapFloor(), "Ibor cap/floor");
    
    portfolioMaster.add(portfolioDoc);
  }

  private void addPortfolioNode(final ManageablePortfolioNode rootNode, final Collection<FinancialSecurity> finSecurities, final String portfolioNodeName) {
    PositionMaster positionMaster = getToolContext().getPositionMaster();
    SecurityMaster securityMaster = getToolContext().getSecurityMaster();
    final ManageablePortfolioNode portfolioNode = new ManageablePortfolioNode(portfolioNodeName);
    for (final FinancialSecurity security : finSecurities) {
      SecurityDocument toAddDoc = new SecurityDocument();
      toAddDoc.setSecurity(security);
      securityMaster.add(toAddDoc);
      ManageablePosition position = new ManageablePosition(BigDecimal.ONE, security.getExternalIdBundle());
      PositionDocument addedDoc = positionMaster.add(new PositionDocument(position));
      portfolioNode.addPosition(addedDoc.getUniqueId());
    }
    rootNode.addChildNode(portfolioNode);
  }
  
  private Collection<FinancialSecurity> getBonds() {
    final List<FinancialSecurity> securities = new ArrayList<FinancialSecurity>();
    final GovernmentBondSecurity bond1 = new GovernmentBondSecurity("US TREASURY N/B", "Sovereign", "US", "US GOVERNMENT", 
        Currency.USD, SimpleYieldConvention.US_STREET, 
        new Expiry(ZonedDateTime.of(LocalDateTime.of(2013, 12, 15, 16, 0), TimeZone.UTC)), "FIXED", 2.625, 
        SimpleFrequency.SEMI_ANNUAL, DayCountFactory.INSTANCE.getDayCount("Actual/Actual ICMA"), 
        ZonedDateTime.of(LocalDateTime.of(2009, 5, 30, 18, 0), TimeZone.UTC), 
        ZonedDateTime.of(LocalDateTime.of(2011, 5, 28, 11, 0), TimeZone.UTC), 
        ZonedDateTime.of(LocalDateTime.of(2009, 12, 31, 11, 0), TimeZone.UTC), 
        99.651404, 3.8075E10, 100.0, 100.0, 100.0, 100.0);
    bond1.addExternalId(ExternalId.of(SecurityUtils.OG_SYNTHETIC_TICKER, "US912828KY53"));
    bond1.setName("T 2 5/8 06/30/14");
    
    final GovernmentBondSecurity bond2 = new GovernmentBondSecurity("US TREASURY N/B", "Sovereign", "US", "US GOVERNMENT", 
        Currency.USD, SimpleYieldConvention.US_STREET, 
        new Expiry(ZonedDateTime.of(LocalDateTime.of(2015, 8, 31, 18, 0), TimeZone.UTC)), "FIXED", 1.25, 
        SimpleFrequency.SEMI_ANNUAL, DayCountFactory.INSTANCE.getDayCount("Actual/Actual ICMA"), 
        ZonedDateTime.of(LocalDateTime.of(2010, 8, 31, 18, 0), TimeZone.UTC), 
        ZonedDateTime.of(LocalDateTime.of(2011, 2, 14, 11, 0), TimeZone.UTC), 
        ZonedDateTime.of(LocalDateTime.of(2011, 2, 28, 11, 0), TimeZone.UTC), 
        99.402797, 3.6881E10, 100.0, 100.0, 100.0, 100.0);
    bond2.addExternalId(ExternalId.of(SecurityUtils.OG_SYNTHETIC_TICKER, "US912828NV87"));
    bond2.setName("T 1 1/4 08/31/15");
    
    final GovernmentBondSecurity bond3 = new GovernmentBondSecurity("TSY 8% 2021", "Sovereign", "GB", "UK GILT STOCK", 
        Currency.GBP, SimpleYieldConvention.UK_BUMP_DMO_METHOD, 
        new Expiry(ZonedDateTime.of(LocalDateTime.of(2021, 6, 7, 18, 0), TimeZone.UTC)), "FIXED", 8.0, 
        SimpleFrequency.SEMI_ANNUAL, DayCountFactory.INSTANCE.getDayCount("Actual/Actual ISDA"), 
        ZonedDateTime.of(LocalDateTime.of(1996, 2, 29, 18, 0), TimeZone.UTC), 
        ZonedDateTime.of(LocalDateTime.of(2011, 1, 28, 11, 0), TimeZone.UTC), 
        ZonedDateTime.of(LocalDateTime.of(1996, 6, 7, 12, 0), TimeZone.UTC), 
        99.0625, 2.2686E10, 0.01, 0.01, 100.0, 100.0);
    bond3.addExternalId(ExternalId.of(SecurityUtils.OG_SYNTHETIC_TICKER, "GB0009997999"));
    bond3.setName("UKT 8 06/07/21");
    bond3.setAnnouncementDate(ZonedDateTime.of(LocalDateTime.of(1996, 2, 20, 11, 0), TimeZone.UTC));
    
    final List<BondFutureDeliverable> bondFutureDelivarables = new ArrayList<BondFutureDeliverable>();
    bondFutureDelivarables.add(new BondFutureDeliverable(ExternalIdBundle.of(ExternalId.of(SecurityUtils.OG_SYNTHETIC_TICKER, "GV912810FF0")), 0.9221));
    bondFutureDelivarables.add(new BondFutureDeliverable(ExternalIdBundle.of(ExternalId.of(SecurityUtils.OG_SYNTHETIC_TICKER, "GV912810FJ2")), 1.0132));
    bondFutureDelivarables.add(new BondFutureDeliverable(ExternalIdBundle.of(ExternalId.of(SecurityUtils.OG_SYNTHETIC_TICKER, "GV912810FA1")), 1.037));
    bondFutureDelivarables.add(new BondFutureDeliverable(ExternalIdBundle.of(ExternalId.of(SecurityUtils.OG_SYNTHETIC_TICKER, "GV912810FE3")), 0.9485));
    bondFutureDelivarables.add(new BondFutureDeliverable(ExternalIdBundle.of(ExternalId.of(SecurityUtils.OG_SYNTHETIC_TICKER, "GV912810FB9")), 1.0125));
    bondFutureDelivarables.add(new BondFutureDeliverable(ExternalIdBundle.of(ExternalId.of(SecurityUtils.OG_SYNTHETIC_TICKER, "GV912810FM5")), 1.0273));
    bondFutureDelivarables.add(new BondFutureDeliverable(ExternalIdBundle.of(ExternalId.of(SecurityUtils.OG_SYNTHETIC_TICKER, "GV912810FG8")), 0.9213));
    bondFutureDelivarables.add(new BondFutureDeliverable(ExternalIdBundle.of(ExternalId.of(SecurityUtils.OG_SYNTHETIC_TICKER, "GV912810PT9")), 0.8398));
    
    bondFutureDelivarables.add(new BondFutureDeliverable(ExternalIdBundle.of(ExternalId.of(SecurityUtils.OG_SYNTHETIC_TICKER, "GV912810FP8")), 0.9301));
    bondFutureDelivarables.add(new BondFutureDeliverable(ExternalIdBundle.of(ExternalId.of(SecurityUtils.OG_SYNTHETIC_TICKER, "GV912810FT0")), 0.8113));
    
    final BondFutureSecurity bond4 = new BondFutureSecurity(new Expiry(ZonedDateTime.of(LocalDateTime.of(2012, 3, 21, 20, 0), TimeZone.UTC)), 
        "XCBT", "XCBT", Currency.USD, 1000.0, bondFutureDelivarables, "Bond", ZonedDateTime.of(LocalDateTime.of(2012, 3, 1, 0, 0), TimeZone.UTC), 
        ZonedDateTime.of(LocalDateTime.of(2012, 3, 1, 0, 0), TimeZone.UTC));

    securities.add(bond1);
    securities.add(bond2);
    securities.add(bond3);
    securities.add(bond4);
    return securities;
  }

  private static List<SwapSecurity> getIborSwaps() {
    final List<SwapSecurity> securities = new ArrayList<SwapSecurity>();
    final SwapSecurity swap1 = new SwapSecurity(
        ZonedDateTime.of(LocalDateTime.of(2000, 5, 1, 11, 0), TimeZone.UTC), 
        ZonedDateTime.of(LocalDateTime.of(2000, 5, 1, 11, 0), TimeZone.UTC), 
        ZonedDateTime.of(LocalDateTime.of(2040, 5, 1, 11, 0), TimeZone.UTC), 
        "Cpty", 
        new FixedInterestRateLeg(DAY_COUNT, 
                                 SimpleFrequency.SEMI_ANNUAL, 
                                 RegionUtils.countryRegionId(Country.of("US")), 
                                 BUSINESS_DAY, 
                                 new InterestRateNotional(Currency.USD, 15000000), 
                                 true, 
                                 0.05), 
        new FloatingInterestRateLeg(DAY_COUNT, 
                                    SimpleFrequency.QUARTERLY, 
                                    RegionUtils.countryRegionId(Country.of("US")), 
                                    BUSINESS_DAY, 
                                    new InterestRateNotional(Currency.USD, 15000000), 
                                    true, 
                                    ExternalId.of(SecurityUtils.OG_SYNTHETIC_TICKER, "USDLIBORP3M"), 
                                    FloatingRateType.IBOR));
    swap1.addExternalId(ExternalId.of(ID_SCHEME, GUIDGenerator.generate().toString()));
    swap1.setName("Swap: pay 5% fixed vs 3m Libor, start=1/5/2000, maturity=1/5/2040, notional=USD 15MM");
    final SwapSecurity swap2 = new SwapSecurity(
        ZonedDateTime.of(LocalDateTime.of(2005, 5, 1, 11, 0), TimeZone.UTC), 
        ZonedDateTime.of(LocalDateTime.of(2005, 5, 1, 11, 0), TimeZone.UTC), 
        ZonedDateTime.of(LocalDateTime.of(2030, 5, 1, 11, 0), TimeZone.UTC), 
        "Cpty", 
        new FixedInterestRateLeg(DAY_COUNT, 
                                 SimpleFrequency.SEMI_ANNUAL, 
                                 RegionUtils.countryRegionId(Country.of("DE")), 
                                 BUSINESS_DAY, 
                                 new InterestRateNotional(Currency.EUR, 20000000), 
                                 true, 
                                 0.04), 
        new FloatingInterestRateLeg(DAY_COUNT, 
                                    SimpleFrequency.QUARTERLY, 
                                    RegionUtils.countryRegionId(Country.of("DE")), 
                                    BUSINESS_DAY, 
                                    new InterestRateNotional(Currency.EUR, 20000000), 
                                    true, 
                                    ExternalId.of(SecurityUtils.BLOOMBERG_TICKER, "EU0006M Index"), 
                                    FloatingRateType.IBOR));
    swap2.addExternalId(ExternalId.of(ID_SCHEME, GUIDGenerator.generate().toString()));
    swap2.setName("Swap: pay 4% fixed vs 6m Euribor, start=1/5/2005, maturity=1/5/2030, notional=EUR 20MM");
    final SwapSecurity swap3 = new SwapSecurity(
        ZonedDateTime.of(LocalDateTime.of(2007, 5, 1, 11, 0), TimeZone.UTC), 
        ZonedDateTime.of(LocalDateTime.of(2007, 5, 1, 11, 0), TimeZone.UTC), 
        ZonedDateTime.of(LocalDateTime.of(2020, 5, 1, 11, 0), TimeZone.UTC), 
        "Cpty", 
        new FixedInterestRateLeg(DAY_COUNT, 
                                 SimpleFrequency.SEMI_ANNUAL, 
                                 RegionUtils.countryRegionId(Country.of("GB")), 
                                 BUSINESS_DAY, 
                                 new InterestRateNotional(Currency.GBP, 15000000), 
                                 true, 
                                 0.03), 
        new FloatingInterestRateLeg(DAY_COUNT, 
                                    SimpleFrequency.QUARTERLY, 
                                    RegionUtils.countryRegionId(Country.of("GB")), 
                                    BUSINESS_DAY, 
                                    new InterestRateNotional(Currency.GBP, 15000000), 
                                    true, 
                                    ExternalId.of(SecurityUtils.BLOOMBERG_TICKER, "BP0006M Index"), 
                                    FloatingRateType.IBOR));
    swap3.addExternalId(ExternalId.of(ID_SCHEME, GUIDGenerator.generate().toString()));
    swap3.setName("Swap: pay 3% fixed vs 6m Libor, start=1/5/2007, maturity=1/5/2020, notional=GBP 15MM");
    final SwapSecurity swap4 = new SwapSecurity(
        ZonedDateTime.of(LocalDateTime.of(2003, 5, 1, 11, 0), TimeZone.UTC), 
        ZonedDateTime.of(LocalDateTime.of(2003, 5, 1, 11, 0), TimeZone.UTC), 
        ZonedDateTime.of(LocalDateTime.of(2028, 5, 1, 11, 0), TimeZone.UTC), 
        "Cpty", 
        new FixedInterestRateLeg(DAY_COUNT, 
                                 SimpleFrequency.SEMI_ANNUAL, 
                                 RegionUtils.countryRegionId(Country.of("JP")), 
                                 BUSINESS_DAY, 
                                 new InterestRateNotional(Currency.JPY, 100000000), 
                                 true, 
                                 0.02), 
        new FloatingInterestRateLeg(DAY_COUNT, 
                                    SimpleFrequency.QUARTERLY, 
                                    RegionUtils.countryRegionId(Country.of("JP")), 
                                    BUSINESS_DAY, 
                                    new InterestRateNotional(Currency.JPY, 100000000), 
                                    true, 
                                    ExternalId.of(SecurityUtils.BLOOMBERG_TICKER, "JY0006M Index"), 
                                    FloatingRateType.IBOR));
    swap4.addExternalId(ExternalId.of(ID_SCHEME, GUIDGenerator.generate().toString()));
    swap4.setName("Swap: pay 2% fixed vs 6m Libor, start=1/5/2003, maturity=1/5/2028, notional=JPY 100MM");
    final SwapSecurity swap5 = new SwapSecurity(
        ZonedDateTime.of(LocalDateTime.of(2004, 5, 1, 11, 0), TimeZone.UTC), 
        ZonedDateTime.of(LocalDateTime.of(2004, 5, 1, 11, 0), TimeZone.UTC), 
        ZonedDateTime.of(LocalDateTime.of(2044, 5, 1, 11, 0), TimeZone.UTC), 
        "Cpty", 
        new FixedInterestRateLeg(DAY_COUNT, 
                                 SimpleFrequency.SEMI_ANNUAL, 
                                 RegionUtils.countryRegionId(Country.of("CH")), 
                                 BUSINESS_DAY, 
                                 new InterestRateNotional(Currency.CHF, 5000000), 
                                 true, 
                                 0.07), 
        new FloatingInterestRateLeg(DAY_COUNT, 
                                    SimpleFrequency.QUARTERLY, 
                                    RegionUtils.countryRegionId(Country.of("CH")), 
                                    BUSINESS_DAY, 
                                    new InterestRateNotional(Currency.CHF, 5000000), 
                                    true, 
                                    ExternalId.of(SecurityUtils.BLOOMBERG_TICKER, "SF0006M Index"), 
                                    FloatingRateType.IBOR));
    swap5.addExternalId(ExternalId.of(ID_SCHEME, GUIDGenerator.generate().toString()));
    swap5.setName("Swap: pay 7% fixed vs 6m Libor, start=1/5/2004, maturity=1/5/2044, notional=CHF 50MM");
//    final SwapSecurity swap6 = new SwapSecurity(
//        ZonedDateTime.of(LocalDateTime.of(2010, 5, 1, 11, 0), TimeZone.UTC), 
//        ZonedDateTime.of(LocalDateTime.of(2010, 5, 1, 11, 0), TimeZone.UTC), 
//        ZonedDateTime.of(LocalDateTime.of(2040, 5, 1, 11, 0), TimeZone.UTC), 
//        "Cpty", 
//        new FixedInterestRateLeg(DAY_COUNT, 
//                                 SimpleFrequency.SEMI_ANNUAL, 
//                                 RegionUtils.countryRegionId(Country.of("CA")), 
//                                 BUSINESS_DAY, 
//                                 new InterestRateNotional(Currency.CAD, 20000000), 
//                                 true, 
//                                 0.05), 
//        new FloatingInterestRateLeg(DAY_COUNT, 
//                                    SimpleFrequency.QUARTERLY, 
//                                    RegionUtils.countryRegionId(Country.of("CA")), 
//                                    BUSINESS_DAY, 
//                                    new InterestRateNotional(Currency.CAD, 20000000), 
//                                    true, 
//                                    ExternalId.of(SecurityUtils.BLOOMBERG_TICKER, "CDOR06 RBC Index"), 
//                                    FloatingRateType.IBOR));
//    swap6.addExternalId(ExternalId.of(ID_SCHEME, GUIDGenerator.generate().toString()));
//    swap6.setName("Swap: pay 5% fixed vs 6m CDOR, start=1/5/2010, maturity=1/5/2040, notional=CAD 20MM");
//    final SwapSecurity swap7 = new SwapSecurity(
//        ZonedDateTime.of(LocalDateTime.of(2005, 5, 1, 11, 0), TimeZone.UTC), 
//        ZonedDateTime.of(LocalDateTime.of(2005, 5, 1, 11, 0), TimeZone.UTC), 
//        ZonedDateTime.of(LocalDateTime.of(2025, 5, 1, 11, 0), TimeZone.UTC), 
//        "Cpty", 
//        new FixedInterestRateLeg(DAY_COUNT, 
//                                 SimpleFrequency.SEMI_ANNUAL, 
//                                 RegionUtils.countryRegionId(Country.of("AU")), 
//                                 BUSINESS_DAY, 
//                                 new InterestRateNotional(Currency.AUD, 25000000), 
//                                 true, 
//                                 0.05), 
//        new FloatingInterestRateLeg(DAY_COUNT, 
//                                    SimpleFrequency.QUARTERLY, 
//                                    RegionUtils.countryRegionId(Country.of("AU")), 
//                                    BUSINESS_DAY, 
//                                    new InterestRateNotional(Currency.AUD, 25000000), 
//                                    true, 
//                                    ExternalId.of(SecurityUtils.BLOOMBERG_TICKER, "AU0006M Index"), 
//                                    FloatingRateType.IBOR));
//    swap7.addExternalId(ExternalId.of(ID_SCHEME, GUIDGenerator.generate().toString()));
//    swap7.setName("Swap: pay 5% fixed vs 6m Libor, start=1/5/2005, maturity=1/5/2025, notional=AUD 25MM");
    final SwapSecurity swap8 = new SwapSecurity(
        ZonedDateTime.of(LocalDateTime.of(2010, 5, 1, 11, 0), TimeZone.UTC), 
        ZonedDateTime.of(LocalDateTime.of(2010, 5, 1, 11, 0), TimeZone.UTC), 
        ZonedDateTime.of(LocalDateTime.of(2030, 5, 1, 11, 0), TimeZone.UTC), 
        "Cpty", 
        new FixedInterestRateLeg(DAY_COUNT, 
                                 SimpleFrequency.SEMI_ANNUAL, 
                                 RegionUtils.countryRegionId(Country.of("NZ")), 
                                 BUSINESS_DAY, 
                                 new InterestRateNotional(Currency.NZD, 55000000), 
                                 true, 
                                 0.05), 
        new FloatingInterestRateLeg(DAY_COUNT, 
                                    SimpleFrequency.QUARTERLY, 
                                    RegionUtils.countryRegionId(Country.of("NZ")), 
                                    BUSINESS_DAY, 
                                    new InterestRateNotional(Currency.NZD, 55000000), 
                                    true, 
                                    ExternalId.of(SecurityUtils.BLOOMBERG_TICKER, "NZ0006M Index"), 
                                    FloatingRateType.IBOR));
    swap8.addExternalId(ExternalId.of(ID_SCHEME, GUIDGenerator.generate().toString()));
    swap8.setName("Swap: pay 5% fixed vs 6m Libor, start=1/5/2010, maturity=1/5/2030, notional=NZD 55MM");
    final SwapSecurity swap9 = new SwapSecurity(
        ZonedDateTime.of(LocalDateTime.of(2010, 5, 1, 11, 0), TimeZone.UTC), 
        ZonedDateTime.of(LocalDateTime.of(2010, 5, 1, 11, 0), TimeZone.UTC), 
        ZonedDateTime.of(LocalDateTime.of(2030, 5, 1, 11, 0), TimeZone.UTC), 
        "Cpty", 
        new FixedInterestRateLeg(DAY_COUNT, 
                                 SimpleFrequency.SEMI_ANNUAL, 
                                 RegionUtils.countryRegionId(Country.of("DK")), 
                                 BUSINESS_DAY, 
                                 new InterestRateNotional(Currency.DKK, 90000000), 
                                 true, 
                                 0.05), 
        new FloatingInterestRateLeg(DAY_COUNT, 
                                    SimpleFrequency.QUARTERLY, 
                                    RegionUtils.countryRegionId(Country.of("DK")), 
                                    BUSINESS_DAY, 
                                    new InterestRateNotional(Currency.DKK, 90000000), 
                                    true, 
                                    ExternalId.of(SecurityUtils.BLOOMBERG_TICKER, "CIBO06M Index"), 
                                    FloatingRateType.IBOR));
    swap9.addExternalId(ExternalId.of(ID_SCHEME, GUIDGenerator.generate().toString()));
    swap9.setName("Swap: pay 5% fixed vs 6m Cibor, start=1/5/2010, maturity=1/5/2030, notional=DKK 90MM");
    securities.add(swap1);
    securities.add(swap2);
    securities.add(swap3);
    securities.add(swap4);
    securities.add(swap5);
//    securities.add(swap6);
//    securities.add(swap7);
    securities.add(swap8);
    securities.add(swap9);
    return securities;
  }
  
  private static Collection<SwapSecurity> getCMSwaps() {
    final List<SwapSecurity> securities = new ArrayList<SwapSecurity>();
    final SwapSecurity swap1 = new SwapSecurity(
        ZonedDateTime.of(LocalDateTime.of(2000, 5, 1, 11, 0), TimeZone.UTC), 
        ZonedDateTime.of(LocalDateTime.of(2000, 5, 1, 11, 0), TimeZone.UTC), 
        ZonedDateTime.of(LocalDateTime.of(2040, 5, 1, 11, 0), TimeZone.UTC), 
        "Cpty", 
        new FixedInterestRateLeg(DAY_COUNT, 
                                 SimpleFrequency.SEMI_ANNUAL, 
                                 RegionUtils.countryRegionId(Country.of("US")), 
                                 BUSINESS_DAY, 
                                 new InterestRateNotional(Currency.USD, 15000000), 
                                 true, 
                                 0.05), 
        new FloatingInterestRateLeg(DAY_COUNT, 
                                    SimpleFrequency.QUARTERLY, 
                                    RegionUtils.countryRegionId(Country.of("US")), 
                                    BUSINESS_DAY, 
                                    new InterestRateNotional(Currency.USD, 15000000), 
                                    true, 
                                    ExternalId.of(SecurityUtils.BLOOMBERG_TICKER, "USSW6 Curncy"), 
                                    FloatingRateType.CMS));
    swap1.addExternalId(ExternalId.of(ID_SCHEME, GUIDGenerator.generate().toString()));
    swap1.setName("Swap: pay 5% fixed vs USSW6, start=1/5/2000, maturity=1/5/2040, notional=USD 15MM");
    final SwapSecurity swap2 = new SwapSecurity(
        ZonedDateTime.of(LocalDateTime.of(2000, 5, 1, 11, 0), TimeZone.UTC), 
        ZonedDateTime.of(LocalDateTime.of(2000, 5, 1, 11, 0), TimeZone.UTC), 
        ZonedDateTime.of(LocalDateTime.of(2040, 5, 1, 11, 0), TimeZone.UTC), 
        "Cpty", 
        new FloatingInterestRateLeg(DAY_COUNT, 
                                    SimpleFrequency.QUARTERLY, 
                                    RegionUtils.countryRegionId(Country.of("US")), 
                                    BUSINESS_DAY, 
                                    new InterestRateNotional(Currency.USD, 15000000), 
                                    true, 
                                    ExternalId.of(SecurityUtils.OG_SYNTHETIC_TICKER, "USDLIBORP3M"), 
                                    FloatingRateType.IBOR),
        new FloatingInterestRateLeg(DAY_COUNT, 
                                    SimpleFrequency.QUARTERLY, 
                                    RegionUtils.countryRegionId(Country.of("US")), 
                                    BUSINESS_DAY, 
                                    new InterestRateNotional(Currency.USD, 15000000), 
                                    true, 
                                    ExternalId.of(SecurityUtils.BLOOMBERG_TICKER, "USSW6 Curncy"), 
                                    FloatingRateType.CMS));
    swap2.addExternalId(ExternalId.of(ID_SCHEME, GUIDGenerator.generate().toString()));
    swap2.setName("Swap: pay 6m Libor vs USSW6, start=1/5/2000, maturity=1/5/2040, notional=USD 15MM");
    final SwapSecurity swap3 = new SwapSecurity(
        ZonedDateTime.of(LocalDateTime.of(2000, 5, 1, 11, 0), TimeZone.UTC), 
        ZonedDateTime.of(LocalDateTime.of(2000, 5, 1, 11, 0), TimeZone.UTC), 
        ZonedDateTime.of(LocalDateTime.of(2040, 5, 1, 11, 0), TimeZone.UTC), 
        "Cpty", 
        new FloatingInterestRateLeg(DAY_COUNT, 
                                    SimpleFrequency.QUARTERLY, 
                                    RegionUtils.countryRegionId(Country.of("US")), 
                                    BUSINESS_DAY, 
                                    new InterestRateNotional(Currency.USD, 15000000), 
                                    true, 
                                    ExternalId.of(SecurityUtils.BLOOMBERG_TICKER, "USSW10 Curncy"), 
                                    FloatingRateType.CMS),
        new FloatingInterestRateLeg(DAY_COUNT, 
                                    SimpleFrequency.QUARTERLY, 
                                    RegionUtils.countryRegionId(Country.of("US")), 
                                    BUSINESS_DAY, 
                                    new InterestRateNotional(Currency.USD, 15000000), 
                                    true, 
                                    ExternalId.of(SecurityUtils.BLOOMBERG_TICKER, "USSW6 Curncy"), 
                                    FloatingRateType.CMS));
    swap3.addExternalId(ExternalId.of(ID_SCHEME, GUIDGenerator.generate().toString()));
    swap3.setName("Swap: pay USSW10 vs USSW6, start=1/5/2000, maturity=1/5/2040, notional=USD 15MM");
    securities.add(swap1);
    securities.add(swap2);
    securities.add(swap3);
    return securities;
  }
  
  private static Collection<SwaptionSecurity> getSwaptions() {
    final List<SwaptionSecurity> securities = new ArrayList<SwaptionSecurity>();
    
    List<SwapSecurity> iborSwaps = getIborSwaps();
    
    final EuropeanExerciseType europeanExerciseType = new EuropeanExerciseType();
    final SwaptionSecurity swaption1 = new SwaptionSecurity(false, iborSwaps.get(0).getExternalIdBundle().getExternalId(ExternalScheme.of(ID_SCHEME)),
        true, new Expiry(ZonedDateTime.of(LocalDateTime.of(2012, 6, 1, 1, 0), TimeZone.UTC)), 
        true, Currency.USD, null, europeanExerciseType, null);
    securities.add(swaption1);
    
    final SwaptionSecurity swaption2 = new SwaptionSecurity(false, iborSwaps.get(1).getExternalIdBundle().getExternalId(ExternalScheme.of(ID_SCHEME)), 
        false, new Expiry(ZonedDateTime.of(LocalDateTime.of(2013, 6, 1, 1, 0), TimeZone.UTC)), 
        true, Currency.USD, null, europeanExerciseType, null);
    securities.add(swaption2);
    
    final SwaptionSecurity swaption3 = new SwaptionSecurity(false, iborSwaps.get(2).getExternalIdBundle().getExternalId(ExternalScheme.of(ID_SCHEME)), 
        false, new Expiry(ZonedDateTime.of(LocalDateTime.of(2016, 6, 1, 1, 0), TimeZone.UTC)), 
        true, Currency.USD, null, europeanExerciseType, null);
    securities.add(swaption3);
    return securities;
  }
  
  private static Collection<FinancialSecurity> getIborCapFloor() {
    final List<FinancialSecurity> securities = new ArrayList<FinancialSecurity>();
    
    final CapFloorSecurity sec1 = new CapFloorSecurity(ZonedDateTime.of(LocalDateTime.of(2011, 1, 1, 1, 0), TimeZone.UTC), 
        ZonedDateTime.of(LocalDateTime.of(2014, 1, 1, 1, 0), TimeZone.UTC), 1.5E7, 
        ExternalId.of(SecurityUtils.OG_SYNTHETIC_TICKER, "USDLIBORP3M"), 0.01, SimpleFrequency.QUARTERLY, Currency.USD, 
        DayCountFactory.INSTANCE.getDayCount("30U/360"), false, true, true);
    sec1.setName("Ibor cap,  @ 0.01");
    securities.add(sec1);
    
    final CapFloorSecurity sec2 = new CapFloorSecurity(ZonedDateTime.of(LocalDateTime.of(2011, 1, 1, 1, 0), TimeZone.UTC), 
        ZonedDateTime.of(LocalDateTime.of(2014, 1, 1, 1, 0), TimeZone.UTC), 1.5E7, 
        ExternalId.of(SecurityUtils.OG_SYNTHETIC_TICKER, "USDLIBORP3M"), 0.01, SimpleFrequency.QUARTERLY, Currency.USD, 
        DayCountFactory.INSTANCE.getDayCount("30U/360"), false, false, true);
    sec2.setName("Ibor floor,  @ 0.01");
    securities.add(sec2);
    return securities;
  }
  
  private static Collection<FinancialSecurity> getSimpleFixedIncome() {
    final List<FinancialSecurity> securities = new ArrayList<FinancialSecurity>();
    final FRASecurity fra = new FRASecurity(Currency.USD, 
                                            RegionUtils.countryRegionId(Country.of("US")), 
                                            ZonedDateTime.of(LocalDateTime.of(2012, 1, 14, 11, 0), TimeZone.UTC), 
                                            ZonedDateTime.of(LocalDateTime.of(2012, 4, 14, 11, 0), TimeZone.UTC), 
                                            0.01, 
                                            15000000, 
                                            ExternalId.of(SecurityUtils.OG_SYNTHETIC_TICKER, "USDLIBORP3M"), 
                                            ZonedDateTime.of(LocalDateTime.of(2011, 1, 14, 11, 0), TimeZone.UTC));
    fra.addExternalId(ExternalId.of(ID_SCHEME, GUIDGenerator.generate().toString()));
    fra.setName("FRA: pay 1% vs 3m Libor, start=1/14/2012, maturity=4/14/2012, notional=USD 15MM");
    final InterestRateFutureSecurity irFuture = new InterestRateFutureSecurity(new Expiry(ZonedDateTime.of(LocalDateTime.of(2013, 12, 15, 16, 0), TimeZone.UTC)), 
                                                                               "CME", 
                                                                               "CME", 
                                                                               Currency.USD, 
                                                                               1000, 
                                                                               ExternalId.of(SecurityUtils.OG_SYNTHETIC_TICKER, "USDLIBORP3M"));
    irFuture.addExternalId(ExternalId.of(SecurityUtils.OG_SYNTHETIC_TICKER, "EDZ13"));
    irFuture.setName("90DAY EURO$ FUTR Jun13");
    securities.add(fra);
    securities.add(irFuture);
    return securities;
  }
  
  private static Collection<FXForwardSecurity> getSimpleFX() {
    final List<FXForwardSecurity> securities = new ArrayList<FXForwardSecurity>();
    final FXForwardSecurity fxForward1 = new FXForwardSecurity(Currency.USD, 1000000, Currency.EUR, 1000000,
                                                               ZonedDateTime.of(LocalDateTime.of(2013, 2, 1, 11, 0), TimeZone.UTC), 
                                                               RegionUtils.countryRegionId(Country.of("US")));
    fxForward1.addExternalId(ExternalId.of(ID_SCHEME, GUIDGenerator.generate().toString()));
    fxForward1.setName("FX forward, pay USD 1000000, receive EUR 1000000, maturity=1/2/2013");

//    final FXForwardSecurity fxForward2 = new FXForwardSecurity(Currency.CAD, 800000, Currency.JPY, 80000000, 
//                                                               ZonedDateTime.of(LocalDateTime.of(2013, 2, 1, 11, 0), TimeZone.UTC), 
//                                                               RegionUtils.countryRegionId(Country.of("US")));
//    fxForward2.addExternalId(ExternalId.of(ID_SCHEME, GUIDGenerator.generate().toString()));
//    fxForward2.setName("FX forward, pay CAD 800000, receive JPY 80000000, maturity=1/2/2013");
    
    final FXForwardSecurity fxForward3 = new FXForwardSecurity(Currency.CHF, 2000000, Currency.EUR, 1000000,
                                                               ZonedDateTime.of(LocalDateTime.of(2013, 2, 1, 11, 0), TimeZone.UTC), 
                                                               RegionUtils.countryRegionId(Country.of("US")));
    fxForward3.addExternalId(ExternalId.of(ID_SCHEME, GUIDGenerator.generate().toString()));
    fxForward3.setName("FX forward, pay CHF 2000000, receive EUR 1000000, maturity=1/2/2013");
    securities.add(fxForward1);
//    securities.add(fxForward2);
    securities.add(fxForward3);
    return securities;
  }
  
  private static Collection<FinancialSecurity> getFXOptions() {
    final List<FinancialSecurity> securities = new ArrayList<FinancialSecurity>();
    final FXOptionSecurity vanilla1 = new FXOptionSecurity(Currency.USD, 
                                                           Currency.EUR, 
                                                           1000000, 
                                                           1000000, 
                                                           new Expiry(ZonedDateTime.of(LocalDateTime.of(2013, 1, 6, 11, 0), TimeZone.UTC)), 
                                                           ZonedDateTime.of(LocalDateTime.of(2013, 1, 6, 11, 0), TimeZone.UTC), 
                                                           true, 
                                                           new EuropeanExerciseType());
    vanilla1.addExternalId(ExternalId.of(ID_SCHEME, GUIDGenerator.generate().toString()));
    vanilla1.setName("FX vanilla option, put USD 1000000, receive EUR 1000000, maturity=1/6/2013");
    final FXOptionSecurity vanilla2 = new FXOptionSecurity(Currency.EUR, 
                                                           Currency.USD, 
                                                           1500000, 
                                                           1000000, 
                                                           new Expiry(ZonedDateTime.of(LocalDateTime.of(2014, 1, 6, 11, 0), TimeZone.UTC)), 
                                                           ZonedDateTime.of(LocalDateTime.of(2014, 1, 6, 11, 0), TimeZone.UTC), 
                                                           true, 
                                                           new EuropeanExerciseType());
    vanilla2.addExternalId(ExternalId.of(ID_SCHEME, GUIDGenerator.generate().toString()));
    vanilla2.setName("FX vanilla option, put EUR 1500000, receive USD 1000000, maturity=1/6/2014");
    final FXBarrierOptionSecurity barrier1 = new FXBarrierOptionSecurity(Currency.USD, 
                                                                         Currency.EUR, 
                                                                         1000000, 
                                                                         1000000, 
                                                                         new Expiry(ZonedDateTime.of(LocalDateTime.of(2013, 1, 6, 11, 0), TimeZone.UTC)), 
                                                                         ZonedDateTime.of(LocalDateTime.of(2013, 1, 6, 11, 0), TimeZone.UTC), 
                                                                         BarrierType.UP, 
                                                                         BarrierDirection.KNOCK_OUT, 
                                                                         MonitoringType.CONTINUOUS, 
                                                                         SamplingFrequency.DAILY_CLOSE, 
                                                                         1.5, 
                                                                         true);
    barrier1.addExternalId(ExternalId.of(ID_SCHEME, GUIDGenerator.generate().toString()));
    barrier1.setName("FX single barrier up knock-out option, put USD 1000000, receive EUR 1000000, maturity=1/6/2013, barrier=1.5 EUR/USD");
//    final FXBarrierOptionSecurity barrier2 = new FXBarrierOptionSecurity(Currency.EUR, 
//                                                                         Currency.USD, 
//                                                                         1500000, 
//                                                                         1000000, 
//                                                                         new Expiry(ZonedDateTime.of(LocalDateTime.of(2015, 1, 6, 11, 0), TimeZone.UTC)), 
//                                                                         ZonedDateTime.of(LocalDateTime.of(2015, 1, 6, 11, 0), TimeZone.UTC), 
//                                                                         BarrierType.DOWN, 
//                                                                         BarrierDirection.KNOCK_OUT, 
//                                                                         MonitoringType.CONTINUOUS, 
//                                                                         SamplingFrequency.DAILY_CLOSE, 
//                                                                         0.2, 
//                                                                         true);
//    barrier2.addExternalId(ExternalId.of(ID_SCHEME, GUIDGenerator.generate().toString()));
//    barrier2.setName("FX single barrier down knock-out option, put EUR 1500000, receive USD 1000000, maturity=1/6/2015, barrier=0.2 USD/EUR");    
    securities.add(vanilla1);
    securities.add(vanilla2);
    securities.add(barrier1);
//    securities.add(barrier2);
    return securities;
  }

  @Override
  protected void doRun() {
    persistToPortfolio();
  }

}
