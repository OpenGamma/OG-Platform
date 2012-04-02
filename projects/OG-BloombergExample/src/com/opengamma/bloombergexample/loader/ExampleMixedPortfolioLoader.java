/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.bloombergexample.loader;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.time.calendar.LocalDateTime;
import javax.time.calendar.TimeZone;
import javax.time.calendar.ZonedDateTime;

import com.opengamma.core.region.RegionUtils;
import com.opengamma.core.security.SecurityUtils;
import com.opengamma.bloombergexample.tool.AbstractExampleTool;
import com.opengamma.financial.convention.InMemoryConventionBundleMaster;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.convention.frequency.SimpleFrequency;
import com.opengamma.financial.convention.yield.SimpleYieldConvention;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.bond.GovernmentBondSecurity;
import com.opengamma.financial.security.capfloor.CapFloorSecurity;
import com.opengamma.financial.security.equity.EquityVarianceSwapSecurity;
import com.opengamma.financial.security.fra.FRASecurity;
import com.opengamma.financial.security.future.BondFutureDeliverable;
import com.opengamma.financial.security.future.BondFutureSecurity;
import com.opengamma.financial.security.future.EquityFutureSecurity;
import com.opengamma.financial.security.future.EquityIndexDividendFutureSecurity;
import com.opengamma.financial.security.future.InterestRateFutureSecurity;
import com.opengamma.financial.security.fx.FXForwardSecurity;
import com.opengamma.financial.security.option.AmericanExerciseType;
import com.opengamma.financial.security.option.BarrierDirection;
import com.opengamma.financial.security.option.BarrierType;
import com.opengamma.financial.security.option.EuropeanExerciseType;
import com.opengamma.financial.security.option.FXBarrierOptionSecurity;
import com.opengamma.financial.security.option.FXOptionSecurity;
import com.opengamma.financial.security.option.IRFutureOptionSecurity;
import com.opengamma.financial.security.option.MonitoringType;
import com.opengamma.financial.security.option.OptionType;
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
 * Example code to load a mixed portfolio.
 */
public class ExampleMixedPortfolioLoader extends AbstractExampleTool {
  
  /**
   * Example mixed portfolio name
   */
  public static final String PORTFOLIO_NAME = "Example Mixed Portfolio";
  private static final String ID_SCHEME = "MIXED_PORFOLIO_LOADER";
  private static final DayCount DAY_COUNT = DayCountFactory.INSTANCE.getDayCount("Actual/360");
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Following");
  private static final ExternalId USDLIBOR3M = SecurityUtils.bloombergTickerSecurityId("US0003M Index");
  
  public static void main(String[] args) { //CSIGNORE
    new ExampleMixedPortfolioLoader().initAndRun(args);
    System.exit(0);
  }

  private void persistToPortfolio() {
    PortfolioMaster portfolioMaster = getToolContext().getPortfolioMaster();
    ManageablePortfolioNode rootNode = new ManageablePortfolioNode(PORTFOLIO_NAME);
    ManageablePortfolio portfolio = new ManageablePortfolio(PORTFOLIO_NAME, rootNode);
    PortfolioDocument portfolioDoc = new PortfolioDocument();
    portfolioDoc.setPortfolio(portfolio);
    
    addPortfolioNode(rootNode, getIborSwaps(), "Ibor swaps", BigDecimal.ONE);
    addPortfolioNode(rootNode, getCMSwaps(), "CM swaps", BigDecimal.ONE);
    addPortfolioNode(rootNode, getSimpleFixedIncome(), "Fixed income", BigDecimal.ONE);
    addPortfolioNode(rootNode, getSimpleFX(), "FX forward", BigDecimal.ONE);
    addPortfolioNode(rootNode, getFXOptions(), "FX options", BigDecimal.ONE);
    addBondNode(rootNode);
    addPortfolioNode(rootNode, getSwaptions(), "Swaptions", BigDecimal.ONE);
    addPortfolioNode(rootNode, getIborCapFloor(), "Ibor cap/floor", BigDecimal.ONE);
    addPortfolioNode(rootNode, getCMCapFloor(), "CM cap/floor", BigDecimal.ONE);
    addPortfolioNode(rootNode, getIRFutureOptions(), "IR future options", BigDecimal.valueOf(100));
    addEquityNode(rootNode);
    
    portfolioMaster.add(portfolioDoc);
  }

  private void addEquityNode(ManageablePortfolioNode rootNode) {
    final ManageablePortfolioNode portfolioNode = new ManageablePortfolioNode("Equity");
    
    EquityVarianceSwapSecurity equityVarianceSwap = new EquityVarianceSwapSecurity(SecurityUtils.bloombergTickerSecurityId("DJX Index"), 
        Currency.USD, 0.5, 1000000.0, true, 250.0, ZonedDateTime.of(LocalDateTime.of(2010, 11, 1, 16, 0), TimeZone.UTC), 
        ZonedDateTime.of(LocalDateTime.of(2012, 11, 1, 16, 0), TimeZone.UTC), 
        ZonedDateTime.of(LocalDateTime.of(2010, 11, 1, 16, 0), TimeZone.UTC), RegionUtils.currencyRegionId(Currency.USD), SimpleFrequency.DAILY);
    equityVarianceSwap.setName("Equity Variance Swap, USD 1MM, strike=0.5, maturing 2012-11-01");
    equityVarianceSwap.addExternalId(ExternalId.of(ID_SCHEME, GUIDGenerator.generate().toString()));
    storeFinancialSecurity(equityVarianceSwap);
    addPosition(portfolioNode, equityVarianceSwap, BigDecimal.ONE);
    
    EquityIndexDividendFutureSecurity dividendFuture = new EquityIndexDividendFutureSecurity(new Expiry(ZonedDateTime.of(LocalDateTime.of(2011, 12, 16, 17, 30), TimeZone.UTC)), 
        "XEUR", "XEUR", Currency.USD, 1000.0, ZonedDateTime.of(LocalDateTime.of(2011, 12, 16, 17, 30), TimeZone.UTC), ExternalId.of(SecurityUtils.OG_SYNTHETIC_TICKER, "HSBA"));
    dividendFuture.setName("HSBC Holdings SSDF Dec11");
    dividendFuture.addExternalId(ExternalId.of(SecurityUtils.OG_SYNTHETIC_TICKER, "H2SBZ1GR"));
    storeFinancialSecurity(dividendFuture);
    addPosition(portfolioNode, dividendFuture, BigDecimal.valueOf(100));
    
    EquityFutureSecurity equityFuture = new EquityFutureSecurity(new Expiry(ZonedDateTime.of(LocalDateTime.of(2011, 12, 16, 17, 30), TimeZone.UTC)), 
        "XCME", "XCME", Currency.USD, 250.0, ZonedDateTime.of(LocalDateTime.of(2012, 12, 20, 21, 15), TimeZone.UTC), SecurityUtils.bloombergTickerSecurityId("SPX Index"));
    equityFuture.setName("S&P 500 FUTURE Dec12");
    equityFuture.addExternalId(ExternalId.of(SecurityUtils.OG_SYNTHETIC_TICKER, "SPZ2"));
    storeFinancialSecurity(equityFuture);
    addPosition(portfolioNode, equityFuture, BigDecimal.ONE);
    
    rootNode.addChildNode(portfolioNode);
  }

  private void addBondNode(ManageablePortfolioNode rootNode) {
    
    final ManageablePortfolioNode portfolioNode = new ManageablePortfolioNode("Bonds");
   
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
    storeFinancialSecurity(bond1);
    addPosition(portfolioNode, bond1, BigDecimal.valueOf(2120));
    
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
    storeFinancialSecurity(bond2);
    addPosition(portfolioNode, bond2, BigDecimal.valueOf(3940));
    
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
    storeFinancialSecurity(bond3);
    addPosition(portfolioNode, bond3, BigDecimal.valueOf(4690));
    
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
    bond4.setName("US LONG BOND(CBT) Mar12");
    bond4.addExternalId(ExternalId.of(SecurityUtils.OG_SYNTHETIC_TICKER, "USH12"));
    storeFinancialSecurity(bond4);
    addPosition(portfolioNode, bond4, BigDecimal.valueOf(10));
    
    rootNode.addChildNode(portfolioNode);
  }

  private void addPosition(final ManageablePortfolioNode portfolioNode, final FinancialSecurity security, final BigDecimal quantity) {
    PositionMaster positionMaster = getToolContext().getPositionMaster();
    ManageablePosition position = new ManageablePosition(quantity, security.getExternalIdBundle());
    PositionDocument addedDoc = positionMaster.add(new PositionDocument(position));
    portfolioNode.addPosition(addedDoc.getUniqueId());
  }

  private Collection<FinancialSecurity> getIRFutureOptions() {
    List<FinancialSecurity> securities = new ArrayList<FinancialSecurity>();
    
    InterestRateFutureSecurity edu12 = new InterestRateFutureSecurity(new Expiry(ZonedDateTime.of(LocalDateTime.of(2012, 9, 17, 20, 0), TimeZone.UTC)), 
        "XCME", "XCME", Currency.USD, 2500.0, USDLIBOR3M);
    edu12.addExternalId(ExternalId.of(SecurityUtils.OG_SYNTHETIC_TICKER, "EDU12"));
    edu12.setName("90DAY EURO$ FUTR Sep12");
    storeFinancialSecurity(edu12);
    
    AmericanExerciseType exerciseType = new AmericanExerciseType();
    IRFutureOptionSecurity optionSec1 = new IRFutureOptionSecurity("CME", new Expiry(ZonedDateTime.of(LocalDateTime.of(2012, 9, 17, 0, 0), TimeZone.UTC)), 
        exerciseType, ExternalId.of(SecurityUtils.OG_SYNTHETIC_TICKER, "EDU12"), 6.25, false, Currency.USD, 98.0, OptionType.PUT);
    optionSec1.addExternalId(ExternalId.of(SecurityUtils.OG_SYNTHETIC_TICKER, "EDU2P98"));
    optionSec1.setName("EDU2P 2012-09-17 P 98.0");
    storeFinancialSecurity(optionSec1);
    securities.add(optionSec1);
    
    InterestRateFutureSecurity edz12 = new InterestRateFutureSecurity(new Expiry(ZonedDateTime.of(LocalDateTime.of(2012, 12, 17, 20, 0), TimeZone.UTC)), 
        "XCME", "XCME", Currency.USD, 2500.0, USDLIBOR3M);
    edz12.addExternalId(ExternalId.of(SecurityUtils.OG_SYNTHETIC_TICKER, "EDZ12"));
    edz12.setName("90DAY EURO$ FUTR Dec12");
    storeFinancialSecurity(edz12);
    
    IRFutureOptionSecurity optionSec2 = new IRFutureOptionSecurity("CME", new Expiry(ZonedDateTime.of(LocalDateTime.of(2012, 12, 17, 0, 0), TimeZone.UTC)), 
        exerciseType, ExternalId.of(SecurityUtils.OG_SYNTHETIC_TICKER, "EDZ12"), 6.25, false, Currency.USD, 99.0, OptionType.CALL);
    optionSec2.addExternalId(ExternalId.of(SecurityUtils.OG_SYNTHETIC_TICKER, "EDU2P98"));
    optionSec2.setName("EDZ2C 2012-12-17 C 99.0");
    storeFinancialSecurity(optionSec2);
    securities.add(optionSec2);
    
    return securities;
  }

  private void storeFinancialSecurity(final FinancialSecurity security) {
    SecurityMaster securityMaster = getToolContext().getSecurityMaster();
    SecurityDocument toAddDoc = new SecurityDocument();
    toAddDoc.setSecurity(security);
    securityMaster.add(toAddDoc);
  }

  private void addPortfolioNode(final ManageablePortfolioNode rootNode, final Collection<FinancialSecurity> finSecurities, final String portfolioNodeName, BigDecimal quantity) {
    PositionMaster positionMaster = getToolContext().getPositionMaster();
    final ManageablePortfolioNode portfolioNode = new ManageablePortfolioNode(portfolioNodeName);
    for (final FinancialSecurity security : finSecurities) {
      storeFinancialSecurity(security);
      ManageablePosition position = new ManageablePosition(quantity, security.getExternalIdBundle());
      PositionDocument addedDoc = positionMaster.add(new PositionDocument(position));
      portfolioNode.addPosition(addedDoc.getUniqueId());
    }
    rootNode.addChildNode(portfolioNode);
  }
  
  private List<FinancialSecurity> getCMCapFloor() {
    final List<FinancialSecurity> securities = new ArrayList<FinancialSecurity>();
    
    final CapFloorSecurity sec1 = new CapFloorSecurity(ZonedDateTime.of(LocalDateTime.of(2011, 1, 1, 1, 0), TimeZone.UTC), 
        ZonedDateTime.of(LocalDateTime.of(2014, 1, 1, 1, 0), TimeZone.UTC), 1.5E7, 
        ExternalId.of(SecurityUtils.OG_SYNTHETIC_TICKER, "USDSWAPP20Y"), 0.01, SimpleFrequency.QUARTERLY, Currency.USD, 
        DayCountFactory.INSTANCE.getDayCount("30U/360"), false, true, true);
    sec1.setName("Ibor cap,  @ 0.01");
    sec1.addExternalId(ExternalId.of(ID_SCHEME, GUIDGenerator.generate().toString()));
    securities.add(sec1);
    
    final CapFloorSecurity sec2 = new CapFloorSecurity(ZonedDateTime.of(LocalDateTime.of(2011, 1, 1, 1, 0), TimeZone.UTC), 
        ZonedDateTime.of(LocalDateTime.of(2014, 1, 1, 1, 0), TimeZone.UTC), 1.5E7, 
        ExternalId.of(SecurityUtils.OG_SYNTHETIC_TICKER, "USDSWAPP20Y"), 0.01, SimpleFrequency.QUARTERLY, Currency.USD, 
        DayCountFactory.INSTANCE.getDayCount("30U/360"), false, false, true);
    sec2.setName("Ibor floor,  @ 0.01");
    sec2.addExternalId(ExternalId.of(ID_SCHEME, GUIDGenerator.generate().toString()));
    securities.add(sec2);
    return securities;
  }
  
  private List<FinancialSecurity> getIborSwaps() {
    final List<FinancialSecurity> securities = new ArrayList<FinancialSecurity>();
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
                                    USDLIBOR3M, 
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
                                    SecurityUtils.bloombergTickerSecurityId("US0006M Index"), 
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
                                    SecurityUtils.bloombergTickerSecurityId("BP0006M Index"), 
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
                                    SecurityUtils.bloombergTickerSecurityId("JY0006M Index"), 
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
                                    ExternalId.of(SecurityUtils.OG_SYNTHETIC_TICKER, "CHFLIBORP6M"), 
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
                                    SecurityUtils.bloombergTickerSecurityId("NZ0006M Index"), 
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
                                    SecurityUtils.bloombergTickerSecurityId("CIBO06M Index"), 
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
  
  private Collection<FinancialSecurity> getCMSwaps() {
    final List<FinancialSecurity> securities = new ArrayList<FinancialSecurity>();
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
                                    ExternalId.of(SecurityUtils.OG_SYNTHETIC_TICKER, "USDSWAPP6Y"), 
                                    FloatingRateType.CMS));
    swap1.addExternalId(ExternalId.of(ID_SCHEME, GUIDGenerator.generate().toString()));
    swap1.setName("Swap: pay 5% fixed vs USDSWAPP6Y, start=1/5/2000, maturity=1/5/2040, notional=USD 15MM");
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
                                    USDLIBOR3M, 
                                    FloatingRateType.IBOR),
        new FloatingInterestRateLeg(DAY_COUNT, 
                                    SimpleFrequency.QUARTERLY, 
                                    RegionUtils.countryRegionId(Country.of("US")), 
                                    BUSINESS_DAY, 
                                    new InterestRateNotional(Currency.USD, 15000000), 
                                    true, 
                                    ExternalId.of(SecurityUtils.OG_SYNTHETIC_TICKER, "USDSWAPP6Y"), 
                                    FloatingRateType.CMS));
    swap2.addExternalId(ExternalId.of(ID_SCHEME, GUIDGenerator.generate().toString()));
    swap2.setName("Swap: pay 6m Libor vs USDSWAPP6Y, start=1/5/2000, maturity=1/5/2040, notional=USD 15MM");
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
                                    ExternalId.of(SecurityUtils.OG_SYNTHETIC_TICKER, "USDSWAPP10Y"), 
                                    FloatingRateType.CMS),
        new FloatingInterestRateLeg(DAY_COUNT, 
                                    SimpleFrequency.QUARTERLY, 
                                    RegionUtils.countryRegionId(Country.of("US")), 
                                    BUSINESS_DAY, 
                                    new InterestRateNotional(Currency.USD, 15000000), 
                                    true, 
                                    ExternalId.of(SecurityUtils.OG_SYNTHETIC_TICKER, "USDSWAPP6Y"), 
                                    FloatingRateType.CMS));
    swap3.addExternalId(ExternalId.of(ID_SCHEME, GUIDGenerator.generate().toString()));
    swap3.setName("Swap: pay USSW10 vs USDSWAPP10Y, start=1/5/2000, maturity=1/5/2040, notional=USD 15MM");
    securities.add(swap1);
    securities.add(swap2);
    securities.add(swap3);
    return securities;
  }
  
  private Collection<FinancialSecurity> getSwaptions() {
    final List<FinancialSecurity> securities = new ArrayList<FinancialSecurity>();
    
    final EuropeanExerciseType europeanExerciseType = new EuropeanExerciseType();
    final SwapSecurity swap1 = new SwapSecurity(
        ZonedDateTime.of(LocalDateTime.of(2012, 6, 1, 1, 0), TimeZone.UTC), 
        ZonedDateTime.of(LocalDateTime.of(2012, 6, 1, 1, 0), TimeZone.UTC),
        ZonedDateTime.of(LocalDateTime.of(2022, 6, 1, 1, 0), TimeZone.UTC), 
        "Cpty",
        new FloatingInterestRateLeg(DAY_COUNT, SimpleFrequency.QUARTERLY, 
            RegionUtils.financialRegionId("US+GB"),
            BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Modified Following"), 
            new InterestRateNotional(Currency.USD, 1.0E7), 
            false, 
            USDLIBOR3M, 
            FloatingRateType.IBOR),
        new FixedInterestRateLeg(DayCountFactory.INSTANCE.getDayCount("30U/360"), 
            SimpleFrequency.SEMI_ANNUAL, 
            RegionUtils.financialRegionId("US+GB"),
            BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Modified Following"), 
            new InterestRateNotional(Currency.USD, 1.0E7), 
            false, 
            0.04));
    swap1.addExternalId(ExternalId.of(ID_SCHEME, GUIDGenerator.generate().toString()));
    swap1.setName("Swap: pay 3m Libor vs 4% fixed, start=1/6/2012, maturity=1/6/2022, notional=USD 10MM");
    storeFinancialSecurity(swap1);
    final SwaptionSecurity swaption1 = new SwaptionSecurity(false, swap1.getExternalIdBundle().getExternalId(ExternalScheme.of(ID_SCHEME)),
        true, new Expiry(ZonedDateTime.of(LocalDateTime.of(2012, 6, 1, 1, 0), TimeZone.UTC)), 
        true, Currency.USD, null, europeanExerciseType, null);
    swaption1.addExternalId(ExternalId.of(ID_SCHEME, GUIDGenerator.generate().toString()));
    swaption1.setName("Vanilla swaption, 1Y x 10Y, USD 10,000,000 @ 4%");
    securities.add(swaption1);
    
    final SwapSecurity swap2 = new SwapSecurity(
        ZonedDateTime.of(LocalDateTime.of(2013, 6, 1, 1, 0), TimeZone.UTC), 
        ZonedDateTime.of(LocalDateTime.of(2013, 6, 1, 1, 0), TimeZone.UTC),
        ZonedDateTime.of(LocalDateTime.of(2015, 6, 1, 1, 0), TimeZone.UTC), 
        "Cpty",
        new FloatingInterestRateLeg(DAY_COUNT, SimpleFrequency.QUARTERLY, 
            RegionUtils.financialRegionId("US+GB"),
            BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Modified Following"), 
            new InterestRateNotional(Currency.USD, 3000000.0), 
            false, 
            USDLIBOR3M, 
            FloatingRateType.IBOR),
        new FixedInterestRateLeg(DayCountFactory.INSTANCE.getDayCount("30U/360"), 
            SimpleFrequency.SEMI_ANNUAL, 
            RegionUtils.financialRegionId("US+GB"),
            BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Modified Following"), 
            new InterestRateNotional(Currency.USD, 3000000.0), 
            false, 
            0.01));
    swap2.addExternalId(ExternalId.of(ID_SCHEME, GUIDGenerator.generate().toString()));
    swap2.setName("Swap: pay 3m Libor vs 1% fixed, start=1/6/2013, maturity=1/6/2015, notional=USD 3MM");
    storeFinancialSecurity(swap2);
    final SwaptionSecurity swaption2 = new SwaptionSecurity(false, swap2.getExternalIdBundle().getExternalId(ExternalScheme.of(ID_SCHEME)), 
        false, new Expiry(ZonedDateTime.of(LocalDateTime.of(2013, 6, 1, 1, 0), TimeZone.UTC)), 
        true, Currency.USD, null, europeanExerciseType, null);
    swaption2.addExternalId(ExternalId.of(ID_SCHEME, GUIDGenerator.generate().toString()));
    swaption2.setName("Vanilla swaption, 2Y x 2Y, USD 3,000,000 @ 1%");
    securities.add(swaption2);

    final SwapSecurity swap3 = new SwapSecurity(
        ZonedDateTime.of(LocalDateTime.of(2016, 6, 1, 1, 0), TimeZone.UTC), 
        ZonedDateTime.of(LocalDateTime.of(2016, 6, 1, 1, 0), TimeZone.UTC),
        ZonedDateTime.of(LocalDateTime.of(2031, 6, 1, 1, 0), TimeZone.UTC), 
        "Cpty",
        new FloatingInterestRateLeg(DAY_COUNT, SimpleFrequency.QUARTERLY, 
            RegionUtils.financialRegionId("US+GB"),
            BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Modified Following"), 
            new InterestRateNotional(Currency.USD, 6000000.0), 
            false, 
            USDLIBOR3M, 
            FloatingRateType.IBOR),
        new FixedInterestRateLeg(DayCountFactory.INSTANCE.getDayCount("30U/360"), 
            SimpleFrequency.SEMI_ANNUAL, 
            RegionUtils.financialRegionId("US+GB"),
            BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Modified Following"), 
            new InterestRateNotional(Currency.USD, 6000000.0), 
            false, 
            0.035));
    swap3.addExternalId(ExternalId.of(ID_SCHEME, GUIDGenerator.generate().toString()));
    swap3.setName("Swap: pay 3m Libor vs 3.5% fixed, start=1/6/2016, maturity=1/6/2031, notional=USD 6MM");
    storeFinancialSecurity(swap3);
    final SwaptionSecurity swaption3 = new SwaptionSecurity(false, swap3.getExternalIdBundle().getExternalId(ExternalScheme.of(ID_SCHEME)), 
        false, new Expiry(ZonedDateTime.of(LocalDateTime.of(2016, 6, 1, 1, 0), TimeZone.UTC)), 
        true, Currency.USD, null, europeanExerciseType, null);
    swaption3.addExternalId(ExternalId.of(ID_SCHEME, GUIDGenerator.generate().toString()));
    swaption3.setName("Vanilla swaption, 5Y x 15Y, USD 6,000,000 @ 3.5%");
    securities.add(swaption3);
    return securities;
  }
  
  private static Collection<FinancialSecurity> getIborCapFloor() {
    final List<FinancialSecurity> securities = new ArrayList<FinancialSecurity>();
    
    final CapFloorSecurity sec1 = new CapFloorSecurity(ZonedDateTime.of(LocalDateTime.of(2011, 1, 1, 1, 0), TimeZone.UTC), 
        ZonedDateTime.of(LocalDateTime.of(2014, 1, 1, 1, 0), TimeZone.UTC), 1.5E7, 
        USDLIBOR3M, 0.01, SimpleFrequency.QUARTERLY, Currency.USD, 
        DayCountFactory.INSTANCE.getDayCount("30U/360"), false, true, true);
    sec1.setName("Ibor cap,  @ 0.01");
    sec1.addExternalId(ExternalId.of(ID_SCHEME, GUIDGenerator.generate().toString()));
    securities.add(sec1);
    
    final CapFloorSecurity sec2 = new CapFloorSecurity(ZonedDateTime.of(LocalDateTime.of(2011, 1, 1, 1, 0), TimeZone.UTC), 
        ZonedDateTime.of(LocalDateTime.of(2014, 1, 1, 1, 0), TimeZone.UTC), 1.5E7, 
        USDLIBOR3M, 0.01, SimpleFrequency.QUARTERLY, Currency.USD, 
        DayCountFactory.INSTANCE.getDayCount("30U/360"), false, false, true);
    sec2.setName("Ibor floor,  @ 0.01");
    sec2.addExternalId(ExternalId.of(ID_SCHEME, GUIDGenerator.generate().toString()));
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
                                            USDLIBOR3M, 
                                            ZonedDateTime.of(LocalDateTime.of(2011, 1, 14, 11, 0), TimeZone.UTC));
    fra.addExternalId(ExternalId.of(ID_SCHEME, GUIDGenerator.generate().toString()));
    fra.setName("FRA: pay 1% vs 3m Libor, start=1/14/2012, maturity=4/14/2012, notional=USD 15MM");
    final InterestRateFutureSecurity irFuture = new InterestRateFutureSecurity(new Expiry(ZonedDateTime.of(LocalDateTime.of(2013, 12, 15, 16, 0), TimeZone.UTC)), 
                                                                               "CME", 
                                                                               "CME", 
                                                                               Currency.USD, 
                                                                               1000, 
                                                                               USDLIBOR3M);
    irFuture.addExternalId(ExternalId.of(SecurityUtils.OG_SYNTHETIC_TICKER, "EDZ13"));
    irFuture.setName("90DAY EURO$ FUTR Jun13");
    securities.add(fra);
    securities.add(irFuture);
    return securities;
  }
  
  private static Collection<FinancialSecurity> getSimpleFX() {
    final List<FinancialSecurity> securities = new ArrayList<FinancialSecurity>();
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
