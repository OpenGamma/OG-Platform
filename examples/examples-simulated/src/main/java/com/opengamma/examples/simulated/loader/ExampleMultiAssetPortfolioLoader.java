/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.examples.simulated.loader;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.component.tool.AbstractTool;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.financial.convention.frequency.SimpleFrequency;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.capfloor.CapFloorSecurity;
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
import com.opengamma.financial.security.swap.FloatingSpreadIRLeg;
import com.opengamma.financial.security.swap.InterestRateNotional;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.financial.tool.ToolContext;
import com.opengamma.id.ExternalId;
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
import com.opengamma.scripts.Scriptable;
import com.opengamma.util.GUIDGenerator;
import com.opengamma.util.i18n.Country;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Expiry;

/**
 * Example code to load a multi asset portfolio.
 */
@Scriptable
public class ExampleMultiAssetPortfolioLoader extends AbstractTool<ToolContext> {

  /**
   * Example mixed portfolio name
   */
  public static final String PORTFOLIO_NAME = "Multi Asset Portfolio";
  /**
   * Portfolio currencies
   */
  public static final Currency[] s_currencies = new Currency[] {Currency.USD, Currency.GBP, Currency.EUR, Currency.JPY, Currency.CHF, Currency.NZD, Currency.DKK };

  private static final String ID_SCHEME = "MULTI_ASSET_PORFOLIO_LOADER";
  private static final DayCount DAY_COUNT = DayCounts.ACT_360;
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventions.FOLLOWING;
  private static final ExternalId USDLIBOR3M = ExternalId.of(ExternalSchemes.OG_SYNTHETIC_TICKER, "USDLIBORP3M");
  private static final LocalDate TODAY = LocalDate.now();

  //-------------------------------------------------------------------------
  /**
   * Main method to run the tool.
   * 
   * @param args  the standard tool arguments, not null
   */
  public static void main(String[] args) { //CSIGNORE
    new ExampleMultiAssetPortfolioLoader().invokeAndTerminate(args);
  }

  //-------------------------------------------------------------------------
  private void persistToPortfolio() {
    PortfolioMaster portfolioMaster = getToolContext().getPortfolioMaster();
    ManageablePortfolioNode rootNode = new ManageablePortfolioNode(PORTFOLIO_NAME);
    ManageablePortfolio portfolio = new ManageablePortfolio(PORTFOLIO_NAME, rootNode);
    PortfolioDocument portfolioDoc = new PortfolioDocument();
    portfolioDoc.setPortfolio(portfolio);
    addPortfolioNode(rootNode, getIborSwaps(), "Ibor swaps", BigDecimal.ONE);
    addPortfolioNode(rootNode, getCMSwaps(), "CM swaps", BigDecimal.ONE);
    addPortfolioNode(rootNode, getSimpleFX(), "FX forward", BigDecimal.ONE);
    addPortfolioNode(rootNode, getFXOptions(), "FX options", BigDecimal.ONE);
    addPortfolioNode(rootNode, getSwaptions(), "Swaptions", BigDecimal.ONE);
    addPortfolioNode(rootNode, getIborCapFloor(), "Ibor cap/floor", BigDecimal.ONE);
    addPortfolioNode(rootNode, getCMCapFloor(), "CM cap/floor", BigDecimal.ONE);
    portfolioMaster.add(portfolioDoc);
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

    final CapFloorSecurity cmsCap = new CapFloorSecurity(ZonedDateTime.of(LocalDateTime.of(2011, 4, 1, 1, 0), ZoneOffset.UTC),
        ZonedDateTime.of(LocalDateTime.of(2016, 4, 1, 1, 0), ZoneOffset.UTC), 1.5E7,
        ExternalId.of(ExternalSchemes.OG_SYNTHETIC_TICKER, "USDISDA10P10Y"), 0.03, SimpleFrequency.ANNUAL, Currency.USD,
        DayCounts.ACT_360, false, true, false);
    cmsCap.addExternalId(ExternalId.of(ID_SCHEME, GUIDGenerator.generate().toString()));
    cmsCap.setName(getCapFloorName(cmsCap));
    securities.add(cmsCap);

    final CapFloorSecurity cmsFloor = new CapFloorSecurity(ZonedDateTime.of(LocalDateTime.of(2011, 9, 9, 1, 0), ZoneOffset.UTC),
        ZonedDateTime.of(LocalDateTime.of(2016, 9, 9, 1, 0), ZoneOffset.UTC), 1.5E7,
        ExternalId.of(ExternalSchemes.OG_SYNTHETIC_TICKER, "USDISDA10P10Y"), 0.01, SimpleFrequency.SEMI_ANNUAL, Currency.USD,
        DayCounts.ACT_360, false, false, false);
    cmsFloor.setName(getCapFloorName(cmsFloor));
    cmsFloor.addExternalId(ExternalId.of(ID_SCHEME, GUIDGenerator.generate().toString()));
    securities.add(cmsFloor);
    return securities;
  }

  private String getCapFloorName(final CapFloorSecurity capFloorSec) {
    return String.format("%s %s @ %.2f [%s-%s] %s, %s %s %s", capFloorSec.isIbor() ? "IBOR" : "CMS", capFloorSec.isCap() ? "cap " : "floor ",
        capFloorSec.getStrike(), capFloorSec.getStartDate().toLocalDate(), capFloorSec.getMaturityDate().toLocalDate(),
        capFloorSec.getFrequency().getName(), capFloorSec.getCurrency().getCode(),
        PortfolioLoaderHelper.NOTIONAL_FORMATTER.format(capFloorSec.getNotional()), capFloorSec.isPayer() ? " Short" : " Long");
  }

  private List<FinancialSecurity> getIborSwaps() {
    final List<FinancialSecurity> securities = new ArrayList<FinancialSecurity>();
    final SwapSecurity swap1 = new SwapSecurity(
        ZonedDateTime.of(LocalDateTime.of(TODAY.getYear() + 1, 5, 1, 11, 0), ZoneOffset.UTC),
        ZonedDateTime.of(LocalDateTime.of(TODAY.getYear() + 1, 5, 1, 11, 0), ZoneOffset.UTC),
        ZonedDateTime.of(LocalDateTime.of(TODAY.getYear() + 40, 5, 1, 11, 0), ZoneOffset.UTC),
        "Cpty",
        new FixedInterestRateLeg(DAY_COUNT,
            SimpleFrequency.SEMI_ANNUAL,
            ExternalSchemes.countryRegionId(Country.of("US")),
            BUSINESS_DAY,
            new InterestRateNotional(Currency.USD, 15000000),
            true,
            0.05),
        new FloatingInterestRateLeg(DAY_COUNT,
            SimpleFrequency.QUARTERLY,
            ExternalSchemes.countryRegionId(Country.of("US")),
            BUSINESS_DAY,
            new InterestRateNotional(Currency.USD, 15000000),
            true,
            USDLIBOR3M,
            FloatingRateType.IBOR));
    swap1.addExternalId(ExternalId.of(ID_SCHEME, GUIDGenerator.generate().toString()));
    swap1.setName("Swap: pay 5% fixed vs 3m Libor, start=" + swap1.getEffectiveDate().toLocalDate() + ", maturity=" + swap1.getMaturityDate().toLocalDate() + ", notional=USD 15MM");
    final SwapSecurity swap2 = new SwapSecurity(
        ZonedDateTime.of(LocalDateTime.of(TODAY.getYear() + 1, 5, 1, 11, 0), ZoneOffset.UTC),
        ZonedDateTime.of(LocalDateTime.of(TODAY.getYear() + 1, 5, 1, 11, 0), ZoneOffset.UTC),
        ZonedDateTime.of(LocalDateTime.of(TODAY.getYear() + 30, 5, 1, 11, 0), ZoneOffset.UTC),
        "Cpty",
        new FixedInterestRateLeg(DAY_COUNT,
            SimpleFrequency.SEMI_ANNUAL,
            ExternalSchemes.countryRegionId(Country.of("DE")),
            BUSINESS_DAY,
            new InterestRateNotional(Currency.EUR, 20000000),
            true,
            0.04),
        new FloatingInterestRateLeg(DAY_COUNT,
            SimpleFrequency.QUARTERLY,
            ExternalSchemes.countryRegionId(Country.of("DE")),
            BUSINESS_DAY,
            new InterestRateNotional(Currency.EUR, 20000000),
            true,
            ExternalId.of(ExternalSchemes.OG_SYNTHETIC_TICKER, "USDLIBORP6M"),
            FloatingRateType.IBOR));
    swap2.addExternalId(ExternalId.of(ID_SCHEME, GUIDGenerator.generate().toString()));
    swap2.setName("Swap: pay 4% fixed vs 6m Euribor, start=" + swap2.getEffectiveDate().toLocalDate() + ", maturity=" + swap2.getMaturityDate().toLocalDate() + ", notional=EUR 20MM");
    final SwapSecurity swap3 = new SwapSecurity(
        ZonedDateTime.of(LocalDateTime.of(TODAY.getYear() + 1, 5, 1, 11, 0), ZoneOffset.UTC),
        ZonedDateTime.of(LocalDateTime.of(TODAY.getYear() + 1, 5, 1, 11, 0), ZoneOffset.UTC),
        ZonedDateTime.of(LocalDateTime.of(TODAY.getYear() + 13, 5, 1, 11, 0), ZoneOffset.UTC),
        "Cpty",
        new FixedInterestRateLeg(DAY_COUNT,
            SimpleFrequency.SEMI_ANNUAL,
            ExternalSchemes.countryRegionId(Country.of("GB")),
            BUSINESS_DAY,
            new InterestRateNotional(Currency.GBP, 15000000),
            true,
            0.03),
        new FloatingInterestRateLeg(DAY_COUNT,
            SimpleFrequency.QUARTERLY,
            ExternalSchemes.countryRegionId(Country.of("GB")),
            BUSINESS_DAY,
            new InterestRateNotional(Currency.GBP, 15000000),
            true,
            ExternalId.of(ExternalSchemes.OG_SYNTHETIC_TICKER, "GBPLIBORP6M"),
            FloatingRateType.IBOR));
    swap3.addExternalId(ExternalId.of(ID_SCHEME, GUIDGenerator.generate().toString()));
    swap3.setName("Swap: pay 3% fixed vs 6m Libor, start=" + swap3.getEffectiveDate().toLocalDate() + ", maturity=" + swap3.getMaturityDate().toLocalDate() + ", notional=GBP 15MM");
    final SwapSecurity swap4 = new SwapSecurity(
        ZonedDateTime.of(LocalDateTime.of(TODAY.getYear() + 1, 5, 1, 11, 0), ZoneOffset.UTC),
        ZonedDateTime.of(LocalDateTime.of(TODAY.getYear() + 1, 5, 1, 11, 0), ZoneOffset.UTC),
        ZonedDateTime.of(LocalDateTime.of(TODAY.getYear() + 25, 5, 1, 11, 0), ZoneOffset.UTC),
        "Cpty",
        new FixedInterestRateLeg(DAY_COUNT,
            SimpleFrequency.SEMI_ANNUAL,
            ExternalSchemes.countryRegionId(Country.of("JP")),
            BUSINESS_DAY,
            new InterestRateNotional(Currency.JPY, 100000000),
            true,
            0.02),
        new FloatingInterestRateLeg(DAY_COUNT,
            SimpleFrequency.QUARTERLY,
            ExternalSchemes.countryRegionId(Country.of("JP")),
            BUSINESS_DAY,
            new InterestRateNotional(Currency.JPY, 100000000),
            true,
            ExternalId.of(ExternalSchemes.OG_SYNTHETIC_TICKER, "JPYLIBORP6M"),
            FloatingRateType.IBOR));
    swap4.addExternalId(ExternalId.of(ID_SCHEME, GUIDGenerator.generate().toString()));
    swap4.setName("Swap: pay 2% fixed vs 6m Libor, start=" + swap4.getEffectiveDate().toLocalDate() + ", maturity=" + swap4.getMaturityDate().toLocalDate() + ", notional=JPY 100MM");
    final SwapSecurity swap5 = new SwapSecurity(
        ZonedDateTime.of(LocalDateTime.of(TODAY.getYear() + 1, 5, 1, 11, 0), ZoneOffset.UTC),
        ZonedDateTime.of(LocalDateTime.of(TODAY.getYear() + 1, 5, 1, 11, 0), ZoneOffset.UTC),
        ZonedDateTime.of(LocalDateTime.of(TODAY.getYear() + 40, 5, 1, 11, 0), ZoneOffset.UTC),
        "Cpty",
        new FixedInterestRateLeg(DAY_COUNT,
            SimpleFrequency.SEMI_ANNUAL,
            ExternalSchemes.countryRegionId(Country.of("CH")),
            BUSINESS_DAY,
            new InterestRateNotional(Currency.CHF, 5000000),
            true,
            0.07),
        new FloatingInterestRateLeg(DAY_COUNT,
            SimpleFrequency.QUARTERLY,
            ExternalSchemes.countryRegionId(Country.of("CH")),
            BUSINESS_DAY,
            new InterestRateNotional(Currency.CHF, 5000000),
            true,
            ExternalId.of(ExternalSchemes.OG_SYNTHETIC_TICKER, "CHFLIBORP6M"),
            FloatingRateType.IBOR));
    swap5.addExternalId(ExternalId.of(ID_SCHEME, GUIDGenerator.generate().toString()));
    swap5.setName("Swap: pay 7% fixed vs 6m Libor, start=" + swap5.getEffectiveDate().toLocalDate() + ", maturity=" + swap5.getMaturityDate().toLocalDate() + ", notional=CHF 50MM");

    final SwapSecurity swap6 = new SwapSecurity(
        ZonedDateTime.of(LocalDateTime.of(TODAY.getYear() + 1, 5, 1, 11, 0), ZoneOffset.UTC),
        ZonedDateTime.of(LocalDateTime.of(TODAY.getYear() + 1, 5, 1, 11, 0), ZoneOffset.UTC),
        ZonedDateTime.of(LocalDateTime.of(TODAY.getYear() + 20, 5, 1, 11, 0), ZoneOffset.UTC),
        "Cpty",
        new FixedInterestRateLeg(DAY_COUNT,
            SimpleFrequency.SEMI_ANNUAL,
            ExternalSchemes.countryRegionId(Country.of("DK")),
            BUSINESS_DAY,
            new InterestRateNotional(Currency.DKK, 90000000),
            true,
            0.05),
        new FloatingInterestRateLeg(DAY_COUNT,
            SimpleFrequency.QUARTERLY,
            ExternalSchemes.countryRegionId(Country.of("DK")),
            BUSINESS_DAY,
            new InterestRateNotional(Currency.DKK, 90000000),
            true,
            ExternalId.of(ExternalSchemes.OG_SYNTHETIC_TICKER, "DKKLIBORP6M"),
            FloatingRateType.IBOR));
    swap6.addExternalId(ExternalId.of(ID_SCHEME, GUIDGenerator.generate().toString()));
    swap6.setName("Swap: pay 5% fixed vs 6m Cibor, start=" + swap6.getEffectiveDate().toLocalDate() + ", maturity=" + swap6.getMaturityDate().toLocalDate() + ", notional=DKK 90MM");
    securities.add(swap1);
    securities.add(swap2);
    securities.add(swap3);
    securities.add(swap4);
    securities.add(swap5);
    securities.add(swap6);
    return securities;
  }

  private Collection<FinancialSecurity> getCMSwaps() {
    final List<FinancialSecurity> securities = new ArrayList<FinancialSecurity>();
    final int year = TODAY.getYear();
    final SwapSecurity swap1 = new SwapSecurity(
        ZonedDateTime.of(LocalDateTime.of(year + 1, 12, 20, 11, 0), ZoneOffset.UTC),
        ZonedDateTime.of(LocalDateTime.of(year + 1, 12, 20, 11, 0), ZoneOffset.UTC),
        ZonedDateTime.of(LocalDateTime.of(year + 5, 12, 20, 11, 0), ZoneOffset.UTC),
        "Cpty",
        new FixedInterestRateLeg(DAY_COUNT,
            SimpleFrequency.QUARTERLY,
            ExternalSchemes.countryRegionId(Country.of("US")),
            BUSINESS_DAY,
            new InterestRateNotional(Currency.USD, 21000000),
            true,
            0.035),
        new FloatingInterestRateLeg(DAY_COUNT,
            SimpleFrequency.QUARTERLY,
            ExternalSchemes.countryRegionId(Country.of("US")),
            BUSINESS_DAY,
            new InterestRateNotional(Currency.USD, 21000000),
            true,
            ExternalId.of(ExternalSchemes.OG_SYNTHETIC_TICKER, "USDISDA10P10Y"),
            FloatingRateType.CMS));
    swap1.addExternalId(ExternalId.of(ID_SCHEME, GUIDGenerator.generate().toString()));
    swap1.setName("CMSwap: pay 5Y fixed @ 3.5% vs USDISDA10P10Y, start=" + swap1.getEffectiveDate().toLocalDate() + ", maturity=" + swap1.getMaturityDate().toLocalDate() + ", notional=USD 21MM");
    final SwapSecurity swap2 = new SwapSecurity(
        ZonedDateTime.of(LocalDateTime.of(year + 1, 4, 1, 11, 0), ZoneOffset.UTC),
        ZonedDateTime.of(LocalDateTime.of(year + 1, 4, 1, 11, 0), ZoneOffset.UTC),
        ZonedDateTime.of(LocalDateTime.of(year + 7, 4, 1, 11, 0), ZoneOffset.UTC),
        "Cpty",
        new FloatingInterestRateLeg(DAY_COUNT,
            SimpleFrequency.QUARTERLY,
            ExternalSchemes.countryRegionId(Country.of("US")),
            BUSINESS_DAY,
            new InterestRateNotional(Currency.USD, 123000000),
            true,
            ExternalId.of(ExternalSchemes.OG_SYNTHETIC_TICKER, "USDISDA10P1Y"),
            FloatingRateType.CMS),
        new FloatingSpreadIRLeg(DAY_COUNT,
            SimpleFrequency.QUARTERLY,
            ExternalSchemes.countryRegionId(Country.of("US")),
            BUSINESS_DAY,
            new InterestRateNotional(Currency.USD, 123000000),
            true,
            USDLIBOR3M,
            FloatingRateType.IBOR, 0.005));
    swap2.addExternalId(ExternalId.of(ID_SCHEME, GUIDGenerator.generate().toString()));
    swap2.setName("CMSwap: pay USDISDA10P1Y vs 3m Libor, start=" + swap2.getEffectiveDate().toLocalDate() + ", maturity=" + swap2.getMaturityDate().toLocalDate() + ", notional=USD 123MM");
    securities.add(swap1);
    securities.add(swap2);
    return securities;
  }

  private Collection<FinancialSecurity> getSwaptions() {
    final List<FinancialSecurity> securities = new ArrayList<FinancialSecurity>();
    final int year = TODAY.getYear();
    final EuropeanExerciseType europeanExerciseType = new EuropeanExerciseType();
    final SwapSecurity swap1 = new SwapSecurity(
        ZonedDateTime.of(LocalDateTime.of(year + 1, 6, 1, 1, 0), ZoneOffset.UTC),
        ZonedDateTime.of(LocalDateTime.of(year + 1, 6, 1, 1, 0), ZoneOffset.UTC),
        ZonedDateTime.of(LocalDateTime.of(year + 11, 6, 1, 1, 0), ZoneOffset.UTC),
        "Cpty",
        new FloatingInterestRateLeg(DAY_COUNT, SimpleFrequency.QUARTERLY,
            ExternalSchemes.financialRegionId("US+GB"),
            BusinessDayConventions.MODIFIED_FOLLOWING,
            new InterestRateNotional(Currency.USD, 1.0E7),
            false,
            USDLIBOR3M,
            FloatingRateType.IBOR),
        new FixedInterestRateLeg(DayCounts.THIRTY_U_360,
            SimpleFrequency.SEMI_ANNUAL,
            ExternalSchemes.financialRegionId("US+GB"),
            BusinessDayConventions.MODIFIED_FOLLOWING,
            new InterestRateNotional(Currency.USD, 1.0E7),
            false,
            0.04));
    swap1.addExternalId(ExternalId.of(ID_SCHEME, GUIDGenerator.generate().toString()));
    swap1.setName("Swap: pay 3m Libor vs 4% fixed, start=" + swap1.getEffectiveDate().toLocalDate() + ", maturity=" + swap1.getMaturityDate().toLocalDate() + ", notional=USD 10MM");
    storeFinancialSecurity(swap1);
    final SwaptionSecurity swaption1 = new SwaptionSecurity(false, swap1.getExternalIdBundle().getExternalId(ExternalScheme.of(ID_SCHEME)),
        true, new Expiry(ZonedDateTime.of(LocalDateTime.of(year + 1, 6, 1, 1, 0), ZoneOffset.UTC)),
        true, Currency.USD, null, europeanExerciseType, null);
    swaption1.addExternalId(ExternalId.of(ID_SCHEME, GUIDGenerator.generate().toString()));
    swaption1.setName("Vanilla swaption, 1Y x 10Y, USD 10,000,000 @ 4%");
    securities.add(swaption1);

    final SwapSecurity swap2 = new SwapSecurity(
        ZonedDateTime.of(LocalDateTime.of(year + 2, 6, 1, 1, 0), ZoneOffset.UTC),
        ZonedDateTime.of(LocalDateTime.of(year + 2, 6, 1, 1, 0), ZoneOffset.UTC),
        ZonedDateTime.of(LocalDateTime.of(year + 4, 6, 1, 1, 0), ZoneOffset.UTC),
        "Cpty",
        new FloatingInterestRateLeg(DAY_COUNT, SimpleFrequency.QUARTERLY,
            ExternalSchemes.financialRegionId("US+GB"),
            BusinessDayConventions.MODIFIED_FOLLOWING,
            new InterestRateNotional(Currency.USD, 3000000.0),
            false,
            USDLIBOR3M,
            FloatingRateType.IBOR),
        new FixedInterestRateLeg(DayCounts.THIRTY_U_360,
            SimpleFrequency.SEMI_ANNUAL,
            ExternalSchemes.financialRegionId("US+GB"),
            BusinessDayConventions.MODIFIED_FOLLOWING,
            new InterestRateNotional(Currency.USD, 3000000.0),
            false,
            0.01));
    swap2.addExternalId(ExternalId.of(ID_SCHEME, GUIDGenerator.generate().toString()));
    swap2.setName("Swap: pay 3m Libor vs 1% fixed, start=" + swap2.getEffectiveDate().toLocalDate() + ", maturity=" + swap2.getMaturityDate().toLocalDate() + ", notional=USD 3MM");
    storeFinancialSecurity(swap2);
    final SwaptionSecurity swaption2 = new SwaptionSecurity(false, swap2.getExternalIdBundle().getExternalId(ExternalScheme.of(ID_SCHEME)),
        false, new Expiry(ZonedDateTime.of(LocalDateTime.of(year + 2, 6, 1, 1, 0), ZoneOffset.UTC)),
        true, Currency.USD, null, europeanExerciseType, null);
    swaption2.addExternalId(ExternalId.of(ID_SCHEME, GUIDGenerator.generate().toString()));
    swaption2.setName("Vanilla swaption, 2Y x 2Y, USD 3,000,000 @ 1%");
    securities.add(swaption2);

    final SwapSecurity swap3 = new SwapSecurity(
        ZonedDateTime.of(LocalDateTime.of(year + 5, 6, 1, 1, 0), ZoneOffset.UTC),
        ZonedDateTime.of(LocalDateTime.of(year + 5, 6, 1, 1, 0), ZoneOffset.UTC),
        ZonedDateTime.of(LocalDateTime.of(year + 20, 6, 1, 1, 0), ZoneOffset.UTC),
        "Cpty",
        new FloatingInterestRateLeg(DAY_COUNT, SimpleFrequency.QUARTERLY,
            ExternalSchemes.financialRegionId("US+GB"),
            BusinessDayConventions.MODIFIED_FOLLOWING,
            new InterestRateNotional(Currency.USD, 6000000.0),
            false,
            USDLIBOR3M,
            FloatingRateType.IBOR),
        new FixedInterestRateLeg(DayCounts.THIRTY_U_360,
            SimpleFrequency.SEMI_ANNUAL,
            ExternalSchemes.financialRegionId("US+GB"),
            BusinessDayConventions.MODIFIED_FOLLOWING,
            new InterestRateNotional(Currency.USD, 6000000.0),
            false,
            0.035));
    swap3.addExternalId(ExternalId.of(ID_SCHEME, GUIDGenerator.generate().toString()));
    swap3.setName("Swap: pay 3m Libor vs 3.5% fixed, start=" + swap3.getEffectiveDate().toLocalDate() + ", maturity=" + swap3.getMaturityDate().toLocalDate() + ", notional=USD 6MM");
    storeFinancialSecurity(swap3);
    final SwaptionSecurity swaption3 = new SwaptionSecurity(false, swap3.getExternalIdBundle().getExternalId(ExternalScheme.of(ID_SCHEME)),
        false, new Expiry(ZonedDateTime.of(LocalDateTime.of(year + 5, 6, 1, 1, 0), ZoneOffset.UTC)),
        true, Currency.USD, null, europeanExerciseType, null);
    swaption3.addExternalId(ExternalId.of(ID_SCHEME, GUIDGenerator.generate().toString()));
    swaption3.setName("Vanilla swaption, 5Y x 15Y, USD 6,000,000 @ 3.5%");
    securities.add(swaption3);
    return securities;
  }

  private Collection<FinancialSecurity> getIborCapFloor() {
    final List<FinancialSecurity> securities = new ArrayList<FinancialSecurity>();
    int year = TODAY.getYear();
    final CapFloorSecurity sec1 = new CapFloorSecurity(ZonedDateTime.of(LocalDateTime.of(year + 1, 1, 1, 1, 0), ZoneOffset.UTC),
        ZonedDateTime.of(LocalDateTime.of(year + 3, 1, 1, 1, 0), ZoneOffset.UTC), 1.5E7,
        USDLIBOR3M, 0.01, SimpleFrequency.QUARTERLY, Currency.USD,
        DayCounts.THIRTY_U_360, false, true, true);
    sec1.setName(getCapFloorName(sec1));
    sec1.addExternalId(ExternalId.of(ID_SCHEME, GUIDGenerator.generate().toString()));
    securities.add(sec1);

    final CapFloorSecurity sec2 = new CapFloorSecurity(ZonedDateTime.of(LocalDateTime.of(year + 1, 1, 1, 1, 0), ZoneOffset.UTC),
        ZonedDateTime.of(LocalDateTime.of(year + 3, 1, 1, 1, 0), ZoneOffset.UTC), 1.5E7,
        USDLIBOR3M, 0.01, SimpleFrequency.QUARTERLY, Currency.USD,
        DayCounts.THIRTY_U_360, false, false, true);
    sec2.setName(getCapFloorName(sec2));
    sec2.addExternalId(ExternalId.of(ID_SCHEME, GUIDGenerator.generate().toString()));
    securities.add(sec2);
    return securities;
  }

  private static Collection<FinancialSecurity> getSimpleFX() {
    final List<FinancialSecurity> securities = new ArrayList<FinancialSecurity>();
    int year = TODAY.getYear();
    final FXForwardSecurity fxForward1 = new FXForwardSecurity(Currency.USD, 1000000, Currency.EUR, 1000000,
        ZonedDateTime.of(LocalDateTime.of(year + 1, 2, 1, 11, 0), ZoneOffset.UTC),
        ExternalSchemes.countryRegionId(Country.of("US")));
    fxForward1.addExternalId(ExternalId.of(ID_SCHEME, GUIDGenerator.generate().toString()));
    fxForward1.setName("FX forward, pay USD 1000000, receive EUR 1000000, maturity=" + fxForward1.getForwardDate().toLocalDate());
    final FXForwardSecurity fxForward2 = new FXForwardSecurity(Currency.CHF, 2000000, Currency.EUR, 1000000,
        ZonedDateTime.of(LocalDateTime.of(year + 1, 2, 1, 11, 0), ZoneOffset.UTC),
        ExternalSchemes.countryRegionId(Country.of("US")));
    fxForward2.addExternalId(ExternalId.of(ID_SCHEME, GUIDGenerator.generate().toString()));
    fxForward2.setName("FX forward, pay CHF 2000000, receive EUR 1000000, maturity=" + fxForward2.getForwardDate().toLocalDate());
    securities.add(fxForward1);
    securities.add(fxForward2);
    return securities;
  }

  private static Collection<FinancialSecurity> getFXOptions() {
    final List<FinancialSecurity> securities = new ArrayList<FinancialSecurity>();
    final FXOptionSecurity vanilla1 = new FXOptionSecurity(Currency.USD, 
        Currency.EUR, 
        1000000, 
        1000000,
        new Expiry(ZonedDateTime.of(LocalDateTime.of(TODAY.getYear() + 1, 1, 6, 11, 0), ZoneOffset.UTC)),
        ZonedDateTime.of(LocalDateTime.of(TODAY.getYear() + 1, 1, 6, 11, 0), ZoneOffset.UTC),
        true, 
        new EuropeanExerciseType());
    vanilla1.addExternalId(ExternalId.of(ID_SCHEME, GUIDGenerator.generate().toString()));
    vanilla1.setName("FX vanilla option, put USD 1000000, receive EUR 1000000, maturity=" + vanilla1.getSettlementDate().toLocalDate());
    final FXOptionSecurity vanilla2 = new FXOptionSecurity(Currency.EUR, 
        Currency.USD, 
        1500000,
        1000000,
        new Expiry(ZonedDateTime.of(LocalDateTime.of(TODAY.getYear() + 2, 1, 6, 11, 0), ZoneOffset.UTC)),
        ZonedDateTime.of(LocalDateTime.of(TODAY.getYear() + 2, 1, 6, 11, 0), ZoneOffset.UTC),
        true,
        new EuropeanExerciseType());
    vanilla2.addExternalId(ExternalId.of(ID_SCHEME, GUIDGenerator.generate().toString()));
    vanilla2.setName("FX vanilla option, put EUR 1500000, receive USD 1000000, maturity=" + vanilla2.getSettlementDate().toLocalDate());
    final FXBarrierOptionSecurity barrier1 = new FXBarrierOptionSecurity(Currency.USD,
        Currency.EUR,
        1000000,
        1000000,
        new Expiry(ZonedDateTime.of(LocalDateTime.of(TODAY.getYear() + 1, 1, 6, 11, 0), ZoneOffset.UTC)),
        ZonedDateTime.of(LocalDateTime.of(TODAY.getYear() + 1, 1, 6, 11, 0), ZoneOffset.UTC),
        BarrierType.UP,
        BarrierDirection.KNOCK_OUT,
        MonitoringType.CONTINUOUS,
        SamplingFrequency.DAILY_CLOSE,
        1.5,
        true);
    barrier1.addExternalId(ExternalId.of(ID_SCHEME, GUIDGenerator.generate().toString()));
    barrier1.setName("FX single barrier up knock-out option, put USD 1000000, receive EUR 1000000, maturity=" + barrier1.getSettlementDate().toLocalDate() + ", barrier=1.5 EUR/USD");
    securities.add(vanilla1);
    securities.add(vanilla2);
    securities.add(barrier1);
    return securities;
  }

  @Override
  protected void doRun() {
    persistToPortfolio();
  }

}
