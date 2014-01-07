/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.viewer.status.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Random;

import org.threeten.bp.LocalDate;
import org.threeten.bp.OffsetTime;
import org.threeten.bp.ZonedDateTime;

import com.google.common.collect.Lists;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.credit.DebtSeniority;
import com.opengamma.analytics.financial.credit.RestructuringClause;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.financial.convention.StubType;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.financial.convention.frequency.Frequency;
import com.opengamma.financial.convention.frequency.SimpleFrequency;
import com.opengamma.financial.convention.yield.SimpleYieldConvention;
import com.opengamma.financial.convention.yield.YieldConvention;
import com.opengamma.financial.security.bond.CorporateBondSecurity;
import com.opengamma.financial.security.bond.GovernmentBondSecurity;
import com.opengamma.financial.security.bond.MunicipalBondSecurity;
import com.opengamma.financial.security.capfloor.CapFloorCMSSpreadSecurity;
import com.opengamma.financial.security.capfloor.CapFloorSecurity;
import com.opengamma.financial.security.cash.CashSecurity;
import com.opengamma.financial.security.cds.LegacyFixedRecoveryCDSSecurity;
import com.opengamma.financial.security.cds.LegacyRecoveryLockCDSSecurity;
import com.opengamma.financial.security.cds.LegacyVanillaCDSSecurity;
import com.opengamma.financial.security.cds.StandardFixedRecoveryCDSSecurity;
import com.opengamma.financial.security.cds.StandardRecoveryLockCDSSecurity;
import com.opengamma.financial.security.cds.StandardVanillaCDSSecurity;
import com.opengamma.financial.security.deposit.PeriodicZeroDepositSecurity;
import com.opengamma.financial.security.equity.EquitySecurity;
import com.opengamma.financial.security.equity.EquityVarianceSwapSecurity;
import com.opengamma.financial.security.forward.AgricultureForwardSecurity;
import com.opengamma.financial.security.forward.EnergyForwardSecurity;
import com.opengamma.financial.security.forward.MetalForwardSecurity;
import com.opengamma.financial.security.fra.FRASecurity;
import com.opengamma.financial.security.future.AgricultureFutureSecurity;
import com.opengamma.financial.security.future.BondFutureDeliverable;
import com.opengamma.financial.security.future.BondFutureSecurity;
import com.opengamma.financial.security.future.EnergyFutureSecurity;
import com.opengamma.financial.security.future.EquityIndexDividendFutureSecurity;
import com.opengamma.financial.security.future.FXFutureSecurity;
import com.opengamma.financial.security.future.IndexFutureSecurity;
import com.opengamma.financial.security.future.InterestRateFutureSecurity;
import com.opengamma.financial.security.future.MetalFutureSecurity;
import com.opengamma.financial.security.future.StockFutureSecurity;
import com.opengamma.financial.security.fx.FXForwardSecurity;
import com.opengamma.financial.security.fx.NonDeliverableFXForwardSecurity;
import com.opengamma.financial.security.option.AmericanExerciseType;
import com.opengamma.financial.security.option.AsianExerciseType;
import com.opengamma.financial.security.option.BarrierDirection;
import com.opengamma.financial.security.option.BarrierType;
import com.opengamma.financial.security.option.BermudanExerciseType;
import com.opengamma.financial.security.option.BondFutureOptionSecurity;
import com.opengamma.financial.security.option.CommodityFutureOptionSecurity;
import com.opengamma.financial.security.option.EquityBarrierOptionSecurity;
import com.opengamma.financial.security.option.EquityIndexDividendFutureOptionSecurity;
import com.opengamma.financial.security.option.EquityIndexOptionSecurity;
import com.opengamma.financial.security.option.EquityOptionSecurity;
import com.opengamma.financial.security.option.EuropeanExerciseType;
import com.opengamma.financial.security.option.ExerciseType;
import com.opengamma.financial.security.option.FXBarrierOptionSecurity;
import com.opengamma.financial.security.option.FXDigitalOptionSecurity;
import com.opengamma.financial.security.option.FXOptionSecurity;
import com.opengamma.financial.security.option.IRFutureOptionSecurity;
import com.opengamma.financial.security.option.MonitoringType;
import com.opengamma.financial.security.option.NonDeliverableFXDigitalOptionSecurity;
import com.opengamma.financial.security.option.NonDeliverableFXOptionSecurity;
import com.opengamma.financial.security.option.OptionType;
import com.opengamma.financial.security.option.SamplingFrequency;
import com.opengamma.financial.security.option.SwaptionSecurity;
import com.opengamma.financial.security.swap.FixedInterestRateLeg;
import com.opengamma.financial.security.swap.FloatingInterestRateLeg;
import com.opengamma.financial.security.swap.FloatingRateType;
import com.opengamma.financial.security.swap.ForwardSwapSecurity;
import com.opengamma.financial.security.swap.InterestRateNotional;
import com.opengamma.financial.security.swap.Notional;
import com.opengamma.financial.security.swap.SwapLeg;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ExternalScheme;
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
 * Creates reference portfolios from which View calculation status report can be generated.
 */
public class BloombergReferencePortfolioMaker implements Runnable {

  /**
   * Reference portfolio name.
   */
  public static final String PORTFOLIO_NAME = "BBG_VS_REF_PRT";

  private static final int NUM_SECURITIES = 1;

  private final ExternalScheme _security = ExternalScheme.of("RefSec");
  private final PortfolioMaster _portfolios;
  private final PositionMaster _positions;
  private final SecurityMaster _securities;
  private int _identifiers;
  private Random _generation;

  public BloombergReferencePortfolioMaker(final PortfolioMaster portfolios, final PositionMaster positions, final SecurityMaster securities) {
    _portfolios = portfolios;
    _positions = positions;
    _securities = securities;
  }

  private ExternalIdBundle getBundle(final ManageableSecurity security) {
    final ExternalIdBundle bundle = security.getExternalIdBundle();
    final ExternalId identifier = bundle.getExternalId(_security);
    if (identifier != null) {
      return identifier.toBundle();
    } else {
      return bundle;
    }
  }

  private ManageableTrade createTrade(final ManageableSecurity security) {
    final ManageableTrade trade = new ManageableTrade(BigDecimal.ONE, getBundle(security), LocalDate.now(), OffsetTime.now(), ExternalId.of(
        "counterparty", "Foo"));
    if (BondFutureOptionSecurity.SECURITY_TYPE.equals(security.getSecurityType())) {
      trade.setPremium(-100d);
    } else if (IRFutureOptionSecurity.SECURITY_TYPE.equals(security.getSecurityType())) {
      trade.setPremium(100d);
    }
    return trade;
  }

  private ManageablePosition createPosition(final ManageableSecurity security) {
    final ManageablePosition position = new ManageablePosition(BigDecimal.ONE, getBundle(security));
    position.addTrade(createTrade(security));
    return position;
  }
  
  private ManageablePortfolioNode createNode(final String nodeName, final Collection<? extends ManageableSecurity> securities) {
    final ManageablePortfolioNode node = new ManageablePortfolioNode(nodeName);
    for (ManageableSecurity security : securities) {
      final ManageablePosition position = createPosition(security);
      node.addPosition(_positions.add(new PositionDocument(position)).getObjectId());
    }
    return node;
  }
  
  private void createPortfolio(final String portfolioName, final Iterable<ManageablePortfolioNode> nodes) {
    final ManageablePortfolioNode rootNode = new ManageablePortfolioNode("ROOT");
    final ManageablePortfolio portfolio = new ManageablePortfolio(portfolioName, rootNode);
    for (ManageablePortfolioNode node : nodes) {
      rootNode.addChildNode(node);
    }
    _portfolios.add(new PortfolioDocument(portfolio));
  }

  private void store(final ManageableSecurity security) {
    security.addExternalId(ExternalId.of(_security, Integer.toString(_identifiers++)));
    if (security.getExternalIdBundle().getValue(ExternalSchemes.BLOOMBERG_TICKER) == null) {
      security.addExternalId(ExternalId.of(ExternalSchemes.BLOOMBERG_TICKER, security.getSecurityType()));
    }
    _securities.add(new SecurityDocument(security));
  }

  private void resetGenerationState(final int i) {
    _generation = new Random(i);
  }

  private int select(final int n) {
    return _generation.nextInt(n);
  }

  private <T> T select(final T... data) {
    return data[_generation.nextInt(data.length)];
  }

  private BarrierDirection barrierDirection() {
    return select(BarrierDirection.values());
  }

  private BarrierType barrierType() {
    return select(BarrierType.values());
  }

  private BondFutureDeliverable bondFutureDeliverable() {
    final ExternalIdBundle identifiers;
    switch (select(2)) {
      case 0:
        identifiers = createCorporateBondSecurity().getExternalIdBundle();
        break;
      case 1:
        identifiers = createGovernmentBondSecurity().getExternalIdBundle();
        break;
      // Note: MunicipalBondSecurity is not implemented
      default:
        throw new IllegalStateException();
    }
    return new BondFutureDeliverable(identifiers, 1.0);
  }

  private String issuerDomicile() {
    return select("GB", "US");
  }

  private boolean bool() {
    return select(2) == 0;
  }

  private BusinessDayConvention businessDayConvention() {
    return select(BusinessDayConventions.FOLLOWING, BusinessDayConventions.MODIFIED_FOLLOWING, BusinessDayConventions.NONE);
  }

  private Currency currency() {
    return select(Currency.USD, Currency.GBP, Currency.EUR, Currency.JPY, Currency.CHF);
  }

  private Currency differentCurrency(final Currency to) {
    Currency c;
    do {
      c = currency();
    } while (to.equals(c));
    return c;
  }

  private DayCount dayCount() {
    return select(DayCounts.ACT_ACT_ISDA, DayCounts.THIRTY_U_360);
  }

  @SuppressWarnings("unchecked")
  private ExerciseType exerciseType() {
    try {
      return select(AmericanExerciseType.class, EuropeanExerciseType.class, AsianExerciseType.class, BermudanExerciseType.class).newInstance();
    } catch (final Exception e) {
      throw new OpenGammaRuntimeException("Caught", e);
    }
  }

  private String exchange() {
    return select("XLON", "XNYS");
  }

  private Expiry expiry() {
    return new Expiry(ZonedDateTime.now().plusMonths(14));
  }

  private FixedInterestRateLeg fixedInterestRateLeg(final Notional notional) {
    final DayCount dayCount = dayCount();
    final Frequency frequency = frequency();
    final ExternalId regionIdentifier = region();
    final BusinessDayConvention businessDayConvention = businessDayConvention();
    final boolean eom = bool();
    final double rate = 0;
    return new FixedInterestRateLeg(dayCount, frequency, regionIdentifier, businessDayConvention, notional, eom, rate);
  }

  private FloatingInterestRateLeg floatingInterestRateLeg(final Notional notional) {
    final DayCount dayCount = dayCount();
    final Frequency frequency = frequency();
    final ExternalId regionIdentifier = region();
    final BusinessDayConvention businessDayConvention = businessDayConvention();
    final boolean eom = bool();
    final ExternalId floatingReferenceRateId = ExternalId.of(ExternalSchemes.BLOOMBERG_TICKER, "US0003M Index");
    final FloatingRateType floatingRateType = floatingRateType();
    return new FloatingInterestRateLeg(dayCount, frequency, regionIdentifier, businessDayConvention, notional, eom, floatingReferenceRateId, floatingRateType);
  }

  private FloatingRateType floatingRateType() {
    return select(FloatingRateType.values());
  }

  private Frequency frequency() {
    return select(SimpleFrequency.QUARTERLY, SimpleFrequency.ANNUAL, SimpleFrequency.MONTHLY);
  }

  private InterestRateNotional interestRateNotional() {
    final Currency currency = currency();
    final double amount = 5e6;
    return new InterestRateNotional(currency, amount);
  }

  private MonitoringType monitoringType() {
    return select(MonitoringType.values());
  }

  private OptionType optionType() {
    return select(OptionType.values());
  }

  private ExternalId region() {
    return ExternalId.of(ExternalSchemes.ISO_COUNTRY_ALPHA2, select("GB", "US"));
  }

  private SamplingFrequency samplingFrequency() {
    return select(SamplingFrequency.values());
  }

  private StubType stubType() {
    return select(StubType.values());
  }

  private YieldConvention yieldConvention() {
    return select(SimpleYieldConvention.MONEY_MARKET, SimpleYieldConvention.TRUE, SimpleYieldConvention.US_STREET);
  }

  private BondFutureOptionSecurity createBondFutureOptionSecurity() {
    final String tradingExchange = exchange();
    final String settlementExchange = exchange();
    final Expiry expiry = expiry();
    final ExerciseType exerciseType = exerciseType();
    final ExternalId underlyingIdentifier = createBondFutureSecurity().getExternalIdBundle().getExternalId(_security);
    final double pointValue = 1000;
    final Currency currency = currency();
    final double strike = 1.25;
    final OptionType optionType = optionType();
    final BondFutureOptionSecurity security = new BondFutureOptionSecurity(tradingExchange, settlementExchange, expiry, exerciseType, underlyingIdentifier, pointValue, currency, strike, optionType);
    store(security);
    return security;
  }

  public ManageablePortfolioNode createBondFutureOptionNode() {
    final Collection<ManageableSecurity> securities = new ArrayList<ManageableSecurity>(NUM_SECURITIES);
    for (int i = 0; i < NUM_SECURITIES; i++) {
      resetGenerationState(i);
      securities.add(createBondFutureOptionSecurity());
    }
    return createNode(BondFutureOptionSecurity.SECURITY_TYPE, securities);
  }

  private CorporateBondSecurity createCorporateBondSecurity() {
    final String issuerName = "issuerName";
    final String issuerType = "issuerType";
    final String issuerDomicile = issuerDomicile();
    final String market = "market";
    final Currency currency = currency();
    final YieldConvention yieldConvention = yieldConvention();
    final Expiry lastTradeDate = expiry();
    final String couponType = "couponType";
    final double couponRate = 1.5;
    final Frequency couponFrequency = frequency();
    final DayCount dayCountConvention = dayCount();
    final ZonedDateTime interestAccrualDate = ZonedDateTime.now().minusMonths(24);
    final ZonedDateTime settlementDate = ZonedDateTime.now().minusMonths(12);
    final ZonedDateTime firstCouponDate = interestAccrualDate;
    final Double issuancePrice = 100d;
    final double totalAmountIssued = 1e9;
    final double minimumAmount = 50000;
    final double minimumIncrement = 50000;
    final double parAmount = 50000;
    final double redemptionValue = 100d;
    final CorporateBondSecurity security = new CorporateBondSecurity(issuerName, issuerType, issuerDomicile, market, currency, yieldConvention, lastTradeDate, couponType, couponRate,
          couponFrequency, dayCountConvention, interestAccrualDate, settlementDate, firstCouponDate, issuancePrice, totalAmountIssued, minimumAmount, minimumIncrement, parAmount, redemptionValue);
    store(security);
    return security;
  }

  public ManageablePortfolioNode createCorporateBondNode() {
    final Collection<ManageableSecurity> securities = new ArrayList<ManageableSecurity>(NUM_SECURITIES);
    for (int i = 0; i < NUM_SECURITIES; i++) {
      resetGenerationState(i);
      securities.add(createCorporateBondSecurity());
    }
    return createNode(CorporateBondSecurity.SECURITY_TYPE, securities);
  }

  private GovernmentBondSecurity createGovernmentBondSecurity() {
    final String issuerName = "issuerName";
    final String issuerType = "issuerType";
    final String issuerDomicile = issuerDomicile();
    final String market = "market";
    final Currency currency = currency();
    final YieldConvention yieldConvention = yieldConvention();
    final Expiry lastTradeDate = expiry();
    final String couponType = "couponType";
    final double couponRate = 1.5;
    final Frequency couponFrequency = frequency();
    final DayCount dayCountConvention = dayCount();
    final ZonedDateTime interestAccrualDate = ZonedDateTime.now().minusMonths(24);
    final ZonedDateTime settlementDate = ZonedDateTime.now().minusMonths(12);
    final ZonedDateTime firstCouponDate = interestAccrualDate;
    final Double issuancePrice = 100d;
    final double totalAmountIssued = 1e9;
    final double minimumAmount = 50000;
    final double minimumIncrement = 50000;
    final double parAmount = 50000;
    final double redemptionValue = 100;
    final GovernmentBondSecurity security = new GovernmentBondSecurity(issuerName, issuerType, issuerDomicile, market, currency, yieldConvention, lastTradeDate, couponType, couponRate,
          couponFrequency, dayCountConvention, interestAccrualDate, settlementDate, firstCouponDate, issuancePrice, totalAmountIssued, minimumAmount, minimumIncrement, parAmount, redemptionValue);
    store(security);
    return security;
  }

  public ManageablePortfolioNode createGovernmentBondNode() {
    final Collection<ManageableSecurity> securities = new ArrayList<ManageableSecurity>(NUM_SECURITIES);
    for (int i = 0; i < NUM_SECURITIES; i++) {
      resetGenerationState(i);
      securities.add(createGovernmentBondSecurity());
    }
    return createNode(GovernmentBondSecurity.SECURITY_TYPE, securities);
  }

  private MunicipalBondSecurity createMunicipalBondSecurity() {
    final String issuerName = "issuerName";
    final String issuerType = "issuerType";
    final String issuerDomicile = issuerDomicile();
    final String market = "market";
    final Currency currency = currency();
    final YieldConvention yieldConvention = yieldConvention();
    final Expiry lastTradeDate = expiry();
    final String couponType = "couponType";
    final double couponRate = 1.5;
    final Frequency couponFrequency = frequency();
    final DayCount dayCountConvention = dayCount();
    final ZonedDateTime interestAccrualDate = ZonedDateTime.now().minusMonths(24);
    final ZonedDateTime settlementDate = ZonedDateTime.now().minusMonths(12);
    final ZonedDateTime firstCouponDate = interestAccrualDate;
    final Double issuancePrice = 100d;
    final double totalAmountIssued = 1e9;
    final double minimumAmount = 50000;
    final double minimumIncrement = 50000;
    final double parAmount = 50000;
    final double redemptionValue = 100d;
    final MunicipalBondSecurity security = new MunicipalBondSecurity(issuerName, issuerType, issuerDomicile, market, currency, yieldConvention, lastTradeDate, couponType, couponRate,
          couponFrequency, dayCountConvention, interestAccrualDate, settlementDate, firstCouponDate, issuancePrice, totalAmountIssued, minimumAmount, minimumIncrement, parAmount, redemptionValue);
    store(security);
    return security;
  }

  public ManageablePortfolioNode createMunicipalBondNode() {
    final Collection<ManageableSecurity> securities = new ArrayList<ManageableSecurity>(NUM_SECURITIES);
    for (int i = 0; i < NUM_SECURITIES; i++) {
      resetGenerationState(i);
      securities.add(createMunicipalBondSecurity());
    }
    return createNode(MunicipalBondSecurity.SECURITY_TYPE, securities);
  }

  public void createBondPortfolioNodes(final List<ManageablePortfolioNode> nodes) {
    nodes.add(createCorporateBondNode());
    nodes.add(createGovernmentBondNode());
    nodes.add(createMunicipalBondNode());
  }

  private CapFloorSecurity createCapFloorSecurity() {
    final ZonedDateTime startDate = ZonedDateTime.now().minusMonths(24);
    final ZonedDateTime maturityDate = ZonedDateTime.now().plusMonths(60);
    final double notional = 1e6;
    final ExternalId underlyingIdentifier = ExternalId.of(ExternalSchemes.RIC, "USDSFIX10Y=");
    final double strike = 0.01;
    final Frequency frequency = frequency();
    final Currency currency = currency();
    final DayCount dayCount = dayCount();
    final boolean payer = bool();
    final boolean cap = bool();
    final boolean ibor = bool();
    final CapFloorSecurity security = new CapFloorSecurity(startDate, maturityDate, notional, underlyingIdentifier, strike, frequency, currency, dayCount, payer, cap, ibor);
    store(security);
    return security;
  }

  public ManageablePortfolioNode createCapFloorNode() {
    final Collection<ManageableSecurity> securities = new ArrayList<ManageableSecurity>(NUM_SECURITIES);
    for (int i = 0; i < NUM_SECURITIES; i++) {
      resetGenerationState(i);
      securities.add(createCapFloorSecurity());
    }
    return createNode(CapFloorSecurity.SECURITY_TYPE, securities);
  }

  private CapFloorCMSSpreadSecurity createCapFloorCMSSpreadSecurity() {
    final ZonedDateTime startDate = ZonedDateTime.now().minusMonths(24);
    final ZonedDateTime maturityDate = ZonedDateTime.now().plusMonths(60);
    final double notional = 1e6;
    final Currency currency = currency();
    final ExternalId longIdentifier = ExternalId.of(ExternalSchemes.RIC, "USDSFIX10Y=");
    final ExternalId shortIdentifier = ExternalId.of(ExternalSchemes.RIC, "USDSFIX1Y=");
    final double strike = 0;
    final Frequency frequency = frequency();
    final DayCount dayCount = dayCount();
    final boolean payer = bool();
    final boolean cap = bool();
    final CapFloorCMSSpreadSecurity security = new CapFloorCMSSpreadSecurity(startDate, maturityDate, notional, longIdentifier, shortIdentifier, strike, frequency, currency, dayCount, payer, cap);
    store(security);
    return security;
  }

  public ManageablePortfolioNode createCapFloorCMSSpreadNode() {
    final Collection<ManageableSecurity> securities = new ArrayList<ManageableSecurity>(NUM_SECURITIES);
    for (int i = 0; i < NUM_SECURITIES; i++) {
      resetGenerationState(i);
      securities.add(createCapFloorCMSSpreadSecurity());
    }
    return createNode(CapFloorCMSSpreadSecurity.SECURITY_TYPE, securities);
  }

  private CashSecurity createCashSecurity() {
    final Currency currency = currency();
    final ExternalId region = region();
    final ZonedDateTime start = ZonedDateTime.now().minusMonths(6);
    final ZonedDateTime maturity = ZonedDateTime.now().plusMonths(6);
    final DayCount dayCount = dayCount();
    final double rate = 0.01;
    final double amount = 100000;
    final CashSecurity security = new CashSecurity(currency, region, start, maturity, dayCount, rate, amount);
    store(security);
    return security;
  }

  public ManageablePortfolioNode createCashNode() {
    final Collection<ManageableSecurity> securities = new ArrayList<ManageableSecurity>(NUM_SECURITIES);
    for (int i = 0; i < NUM_SECURITIES; i++) {
      resetGenerationState(i);
      securities.add(createCashSecurity());
    }
    return createNode(CashSecurity.SECURITY_TYPE, securities);
  }

  private AgricultureForwardSecurity createAgricultureForwardSecurity() {
    final String unitName = "unitName";
    final Double unitNumber = null;
    final Expiry expiry = expiry();
    final Currency currency = currency();
    final double unitAmount = 0;
    final String category = "category";
    final AgricultureForwardSecurity security = new AgricultureForwardSecurity(unitName, unitNumber, expiry, currency, unitAmount, category);
    store(security);
    return security;
  }

  public ManageablePortfolioNode loadAgricultureForwardPortfolio() {
    final Collection<ManageableSecurity> securities = new ArrayList<ManageableSecurity>(NUM_SECURITIES);
    for (int i = 0; i < NUM_SECURITIES; i++) {
      resetGenerationState(i);
      securities.add(createAgricultureForwardSecurity());
    }
    return createNode(AgricultureForwardSecurity.SECURITY_TYPE, securities);
  }

  private EnergyForwardSecurity createEnergyForwardSecurity() {
    final String unitName = "unitName";
    final Double unitNumber = null;
    final Expiry expiry = expiry();
    final Currency currency = currency();
    final double unitAmount = 0;
    final String category = "category";
    final EnergyForwardSecurity security = new EnergyForwardSecurity(unitName, unitNumber, expiry, currency, unitAmount, category);
    store(security);
    return security;
  }

  public ManageablePortfolioNode loadEnergyForwardPortfolio() {
    final Collection<ManageableSecurity> securities = new ArrayList<ManageableSecurity>(NUM_SECURITIES);
    for (int i = 0; i < NUM_SECURITIES; i++) {
      resetGenerationState(i);
      securities.add(createEnergyForwardSecurity());
    }
    return createNode(EnergyForwardSecurity.SECURITY_TYPE, securities);
  }

  private MetalForwardSecurity createMetalForwardSecurity() {
    final String unitName = "unitName";
    final Double unitNumber = null;
    final Expiry expiry = expiry();
    final Currency currency = currency();
    final double unitAmount = 0;
    final String category = "category";
    final MetalForwardSecurity security = new MetalForwardSecurity(unitName, unitNumber, expiry, currency, unitAmount, category);
    store(security);
    return security;
  }

  public ManageablePortfolioNode loadMetalForwardPortfolio() {
    final Collection<ManageableSecurity> securities = new ArrayList<ManageableSecurity>(NUM_SECURITIES);
    for (int i = 0; i < NUM_SECURITIES; i++) {
      resetGenerationState(i);
      securities.add(createMetalForwardSecurity());
    }
    return createNode(MetalForwardSecurity.SECURITY_TYPE, securities);
  }

  public void loadCommodityForwardPortfolio() {
    loadAgricultureForwardPortfolio();
    loadEnergyForwardPortfolio();
    loadMetalForwardPortfolio();
  }

  private CommodityFutureOptionSecurity createCommodityFutureOptionSecurity() {
    final String tradingExchange = exchange();
    final String settlementExchange = exchange();
    final Expiry expiry = expiry();
    final ExerciseType exerciseType = exerciseType();
    final ExternalId underlyingIdentifier = ExternalId.of(ExternalSchemes.BLOOMBERG_TICKER, "US0003M Index");
    final double pointValue = 0;
    final Currency currency = currency();
    final double strike = 0;
    final OptionType optionType = optionType();
    final CommodityFutureOptionSecurity security = new CommodityFutureOptionSecurity(tradingExchange, settlementExchange, expiry, exerciseType, underlyingIdentifier, pointValue, currency, strike,
          optionType);
    store(security);
    return security;
  }

  public ManageablePortfolioNode createCommodityFutureOptionNode() {
    final Collection<ManageableSecurity> securities = new ArrayList<ManageableSecurity>(NUM_SECURITIES);
    for (int i = 0; i < NUM_SECURITIES; i++) {
      resetGenerationState(i);
      securities.add(createCommodityFutureOptionSecurity());
    }
    return createNode(CommodityFutureOptionSecurity.SECURITY_TYPE, securities);
  }

  /*
  private ContinuousZeroDepositSecurity createContinuousZeroDepositSecurity() {
    final Currency currency = null;
    final ZonedDateTime startDate = null;
    final ZonedDateTime maturityDate = null;
    final double rate = 0;
    final ExternalId region = region();
    // ContinuousZeroDepositSecurity doesn't have a public constructure ?
    final ContinuousZeroDepositSecurity security = new ContinuousZeroDepositSecurity(currency, startDate, maturityDate, rate, region);
    store(security);
    return security;
  }
  */

  /*
  public void loadContinuousZeroDepositPortfolio() {
    final Collection<ManageableSecurity> securities = new ArrayList<ManageableSecurity>(NUM_SECURITIES);
    for (int i = 0; i < NUM_SECURITIES; i++) {
      resetGenerationState(i);
      securities.add(createContinuousZeroDepositSecurity());
    }
    load(securities);
  }
  */

  private LegacyFixedRecoveryCDSSecurity createLegacyFixedRecoveryCDSSecurity() {
    final boolean isBuy = bool();
    final ExternalId protectionSeller = ExternalId.of("protectionSeller", "Foo");
    final ExternalId protectionBuyer = ExternalId.of("protectionBuyer", "Foo");
    final ExternalId referenceEntity = ExternalId.of("referenceEntity", "Foo");
    final DebtSeniority debtSeniority = DebtSeniority.SENIOR;
    final RestructuringClause restructuringClause = RestructuringClause.NONE;
    final ExternalId regionId = region();
    final ZonedDateTime startDate = ZonedDateTime.now().minusMonths(6);
    final ZonedDateTime effectiveDate = ZonedDateTime.now().minusMonths(5);
    final ZonedDateTime maturityDate = ZonedDateTime.now().plusMonths(6);
    final StubType stubType = stubType();
    final Frequency couponFrequency = frequency();
    final DayCount dayCount = dayCount();
    final BusinessDayConvention businessDayConvention = businessDayConvention();
    final boolean immAdjustMaturityDate = bool();
    final boolean adjustEffectiveDate = bool();
    final boolean adjustMaturityDate = bool();
    final InterestRateNotional notional = interestRateNotional();
    final double recoveryRate = 0;
    final boolean includeAccruedPremium = bool();
    final boolean protectionStart = bool();
    final double parSpread = 0;
    final LegacyFixedRecoveryCDSSecurity security = new LegacyFixedRecoveryCDSSecurity(isBuy, protectionSeller, protectionBuyer, referenceEntity, debtSeniority, restructuringClause, regionId,
        startDate, effectiveDate, maturityDate, stubType, couponFrequency, dayCount, businessDayConvention, immAdjustMaturityDate, adjustEffectiveDate, adjustMaturityDate, notional, recoveryRate,
        includeAccruedPremium, protectionStart, parSpread);
    store(security);
    return security;
  }

  public ManageablePortfolioNode loadLegacyFixedRecoveryCDSPortfolio() {
    final Collection<ManageableSecurity> securities = new ArrayList<ManageableSecurity>(NUM_SECURITIES);
    for (int i = 0; i < NUM_SECURITIES; i++) {
      resetGenerationState(i);
      securities.add(createLegacyFixedRecoveryCDSSecurity());
    }
    return createNode(LegacyFixedRecoveryCDSSecurity.SECURITY_TYPE, securities);
    
  }

  private LegacyRecoveryLockCDSSecurity createLegacyRecoveryLockCDSSecurity() {
    final boolean isBuy = bool();
    final ExternalId protectionSeller = ExternalId.of("protectionSeller", "Foo");
    final ExternalId protectionBuyer = ExternalId.of("protectionBuyer", "Foo");
    final ExternalId referenceEntity = ExternalId.of("referenceEntity", "Foo");
    final DebtSeniority debtSeniority = DebtSeniority.SENIOR;
    final RestructuringClause restructuringClause = RestructuringClause.NONE;
    final ExternalId regionId = region();
    final ZonedDateTime startDate = ZonedDateTime.now().minusMonths(6);
    final ZonedDateTime effectiveDate = ZonedDateTime.now().minusMonths(5);
    final ZonedDateTime maturityDate = ZonedDateTime.now().plusMonths(6);
    final StubType stubType = stubType();
    final Frequency couponFrequency = frequency();
    final DayCount dayCount = dayCount();
    final BusinessDayConvention businessDayConvention = businessDayConvention();
    final boolean immAdjustMaturityDate = bool();
    final boolean adjustEffectiveDate = bool();
    final boolean adjustMaturityDate = bool();
    final InterestRateNotional notional = interestRateNotional();
    final double recoveryRate = 0;
    final boolean includeAccruedPremium = bool();
    final boolean protectionStart = bool();
    final double parSpread = 0;
    final LegacyRecoveryLockCDSSecurity security = new LegacyRecoveryLockCDSSecurity(isBuy, protectionSeller, protectionBuyer, referenceEntity, debtSeniority, restructuringClause, regionId,
        startDate, effectiveDate, maturityDate, stubType, couponFrequency, dayCount, businessDayConvention, immAdjustMaturityDate, adjustEffectiveDate, adjustMaturityDate, notional, recoveryRate,
        includeAccruedPremium, protectionStart, parSpread);
    store(security);
    return security;
  }

  public ManageablePortfolioNode loadLegacyRecoveryLockCDSPortfolio() {
    final Collection<ManageableSecurity> securities = new ArrayList<ManageableSecurity>(NUM_SECURITIES);
    for (int i = 0; i < NUM_SECURITIES; i++) {
      resetGenerationState(i);
      securities.add(createLegacyRecoveryLockCDSSecurity());
    }
    return createNode(LegacyRecoveryLockCDSSecurity.SECURITY_TYPE, securities);
  }

  private LegacyVanillaCDSSecurity createLegacyVanillaCDSSecurity() {
    final boolean isBuy = bool();
    final ExternalId protectionSeller = ExternalId.of("protectionSeller", "Foo");
    final ExternalId protectionBuyer = ExternalId.of("protectionBuyer", "Foo");
    final ExternalId referenceEntity = ExternalId.of("referenceEntity", "Foo");
    final DebtSeniority debtSeniority = DebtSeniority.SENIOR;
    final RestructuringClause restructuringClause = RestructuringClause.NONE;
    final ExternalId regionId = region();
    final ZonedDateTime startDate = ZonedDateTime.now().minusMonths(6);
    final ZonedDateTime effectiveDate = ZonedDateTime.now().minusMonths(5);
    final ZonedDateTime maturityDate = ZonedDateTime.now().plusMonths(6);
    final StubType stubType = stubType();
    final Frequency couponFrequency = frequency();
    final DayCount dayCount = dayCount();
    final BusinessDayConvention businessDayConvention = businessDayConvention();
    final boolean immAdjustMaturityDate = bool();
    final boolean adjustEffectiveDate = bool();
    final boolean adjustMaturityDate = bool();
    final InterestRateNotional notional = interestRateNotional();
    final boolean includeAccruedPremium = bool();
    final boolean protectionStart = bool();
    final double parSpread = 0;
    final LegacyVanillaCDSSecurity security = new LegacyVanillaCDSSecurity(isBuy, protectionSeller, protectionBuyer, referenceEntity, debtSeniority, restructuringClause, regionId,
        startDate, effectiveDate, maturityDate, stubType, couponFrequency, dayCount, businessDayConvention, immAdjustMaturityDate, adjustEffectiveDate, adjustMaturityDate, notional,
        includeAccruedPremium, protectionStart, parSpread);
    store(security);
    return security;
  }

  public ManageablePortfolioNode loadLegacyVanillaCDSPortfolio() {
    final Collection<ManageableSecurity> securities = new ArrayList<ManageableSecurity>(NUM_SECURITIES);
    for (int i = 0; i < NUM_SECURITIES; i++) {
      resetGenerationState(i);
      securities.add(createLegacyVanillaCDSSecurity());
    }
    return createNode(LegacyVanillaCDSSecurity.SECURITY_TYPE, securities);
  }

  public void loadLegacyCDSPortfolio() {
    loadLegacyFixedRecoveryCDSPortfolio();
    loadLegacyRecoveryLockCDSPortfolio();
    loadLegacyVanillaCDSPortfolio();
  }

  private StandardFixedRecoveryCDSSecurity createStandardFixedRecoveryCDSSecurity() {
    final boolean isBuy = bool();
    final ExternalId protectionSeller = ExternalId.of("protectionSeller", "Foo");
    final ExternalId protectionBuyer = ExternalId.of("protectionBuyer", "Foo");
    final ExternalId referenceEntity = ExternalId.of("referenceEntity", "Foo");
    final DebtSeniority debtSeniority = DebtSeniority.SENIOR;
    final RestructuringClause restructuringClause = RestructuringClause.NONE;
    final ExternalId regionId = region();
    final ZonedDateTime startDate = ZonedDateTime.now().minusMonths(6);
    final ZonedDateTime effectiveDate = ZonedDateTime.now().minusMonths(5);
    final ZonedDateTime maturityDate = ZonedDateTime.now().plusMonths(6);
    final StubType stubType = stubType();
    final Frequency couponFrequency = frequency();
    final DayCount dayCount = dayCount();
    final BusinessDayConvention businessDayConvention = businessDayConvention();
    final boolean immAdjustMaturityDate = bool();
    final boolean adjustEffectiveDate = bool();
    final boolean adjustMaturityDate = bool();
    final InterestRateNotional notional = interestRateNotional();
    final double recoveryRate = 0;
    final boolean includeAccruedPremium = bool();
    final boolean protectionStart = bool();
    final double quotedSpread = 0;
    final InterestRateNotional upfrontAmount = interestRateNotional();
    final double coupon = 0;
    final ZonedDateTime settlementDate = ZonedDateTime.now().plusMonths(7);
    final StandardFixedRecoveryCDSSecurity security = new StandardFixedRecoveryCDSSecurity(isBuy, protectionSeller, protectionBuyer, referenceEntity, debtSeniority, restructuringClause, regionId,
        startDate, effectiveDate, maturityDate, stubType, couponFrequency, dayCount, businessDayConvention, immAdjustMaturityDate, adjustEffectiveDate, adjustMaturityDate, notional, recoveryRate,
        includeAccruedPremium, protectionStart, quotedSpread, upfrontAmount);
    store(security);
    return security;
  }

  public ManageablePortfolioNode loadStandardFixedRecoveryCDSPortfolio() {
    final Collection<ManageableSecurity> securities = new ArrayList<ManageableSecurity>(NUM_SECURITIES);
    for (int i = 0; i < NUM_SECURITIES; i++) {
      resetGenerationState(i);
      securities.add(createStandardFixedRecoveryCDSSecurity());
    }
    return createNode(StandardFixedRecoveryCDSSecurity.SECURITY_TYPE, securities);
  }

  private StandardRecoveryLockCDSSecurity createStandardRecoveryLockCDSSecurity() {
    final boolean isBuy = bool();
    final ExternalId protectionSeller = ExternalId.of("protectionSeller", "Foo");
    final ExternalId protectionBuyer = ExternalId.of("protectionBuyer", "Foo");
    final ExternalId referenceEntity = ExternalId.of("referenceEntity", "Foo");
    final DebtSeniority debtSeniority = DebtSeniority.SENIOR;
    final RestructuringClause restructuringClause = RestructuringClause.NONE;
    final ExternalId regionId = region();
    final ZonedDateTime startDate = ZonedDateTime.now().minusMonths(6);
    final ZonedDateTime effectiveDate = ZonedDateTime.now().minusMonths(5);
    final ZonedDateTime maturityDate = ZonedDateTime.now().plusMonths(6);
    final StubType stubType = stubType();
    final Frequency couponFrequency = frequency();
    final DayCount dayCount = dayCount();
    final BusinessDayConvention businessDayConvention = businessDayConvention();
    final boolean immAdjustMaturityDate = bool();
    final boolean adjustEffectiveDate = bool();
    final boolean adjustMaturityDate = bool();
    final InterestRateNotional notional = interestRateNotional();
    final double recoveryRate = 0;
    final boolean includeAccruedPremium = bool();
    final boolean protectionStart = bool();
    final double quotedSpread = 0;
    final InterestRateNotional upfrontAmount = interestRateNotional();
    final double coupon = 0;
    final ZonedDateTime settlementDate = ZonedDateTime.now().plusMonths(7);
    final StandardRecoveryLockCDSSecurity security = new StandardRecoveryLockCDSSecurity(isBuy, protectionSeller, protectionBuyer, referenceEntity, debtSeniority, restructuringClause, regionId,
        startDate, effectiveDate, maturityDate, stubType, couponFrequency, dayCount, businessDayConvention, immAdjustMaturityDate, adjustEffectiveDate, adjustMaturityDate, notional, recoveryRate,
        includeAccruedPremium, protectionStart, quotedSpread, upfrontAmount);
    store(security);
    return security;
  }

  public ManageablePortfolioNode loadStandardRecoveryLockCDSPortfolio() {
    final Collection<ManageableSecurity> securities = new ArrayList<ManageableSecurity>(NUM_SECURITIES);
    for (int i = 0; i < NUM_SECURITIES; i++) {
      resetGenerationState(i);
      securities.add(createStandardRecoveryLockCDSSecurity());
    }
    return createNode(StandardRecoveryLockCDSSecurity.SECURITY_TYPE, securities);
  }

  private StandardVanillaCDSSecurity createStandardVanillaCDSSecurity() {
    final boolean isBuy = bool();
    final ExternalId protectionSeller = ExternalId.of("protectionSeller", "Foo");
    final ExternalId protectionBuyer = ExternalId.of("protectionBuyer", "Foo");
    final ExternalId referenceEntity = ExternalId.of("referenceEntity", "Foo");
    final DebtSeniority debtSeniority = DebtSeniority.SENIOR;
    final RestructuringClause restructuringClause = RestructuringClause.NONE;
    final ExternalId regionId = region();
    final ZonedDateTime startDate = ZonedDateTime.now().minusMonths(6);
    final ZonedDateTime effectiveDate = ZonedDateTime.now().minusMonths(5);
    final ZonedDateTime maturityDate = ZonedDateTime.now().plusMonths(6);
    final StubType stubType = stubType();
    final Frequency couponFrequency = frequency();
    final DayCount dayCount = dayCount();
    final BusinessDayConvention businessDayConvention = businessDayConvention();
    final boolean immAdjustMaturityDate = bool();
    final boolean adjustEffectiveDate = bool();
    final boolean adjustMaturityDate = bool();
    final InterestRateNotional notional = interestRateNotional();
    final boolean includeAccruedPremium = bool();
    final boolean protectionStart = bool();
    final double quotedSpread = 0;
    final InterestRateNotional upfrontAmount = interestRateNotional();
    final double coupon = 0;
    final ZonedDateTime settlementDate = ZonedDateTime.now().plusMonths(7);
    final boolean adjustCashSettlementDate = true;
    final StandardVanillaCDSSecurity security = new StandardVanillaCDSSecurity(isBuy, protectionSeller, protectionBuyer, referenceEntity, debtSeniority, restructuringClause, regionId,
        startDate, effectiveDate, maturityDate, stubType, couponFrequency, dayCount, businessDayConvention, immAdjustMaturityDate, adjustEffectiveDate, adjustMaturityDate, notional,
        includeAccruedPremium, protectionStart, quotedSpread, upfrontAmount, coupon, settlementDate, adjustCashSettlementDate);
    store(security);
    return security;
  }

  public ManageablePortfolioNode loadStandardVanillaCDSPortfolio() {
    final Collection<ManageableSecurity> securities = new ArrayList<ManageableSecurity>(NUM_SECURITIES);
    for (int i = 0; i < NUM_SECURITIES; i++) {
      resetGenerationState(i);
      securities.add(createStandardVanillaCDSSecurity());
    }
    return createNode(StandardVanillaCDSSecurity.SECURITY_TYPE, securities);
  }

  public void loadStandardCDSPortfolio() {
    loadStandardFixedRecoveryCDSPortfolio();
    loadStandardRecoveryLockCDSPortfolio();
    loadStandardVanillaCDSPortfolio();
  }

  public void loadCreditDefaultSwapPortfolio() {
    loadLegacyCDSPortfolio();
    loadStandardCDSPortfolio();
  }

  private EquitySecurity createEquitySecurity() {
    final String exchange = "exchange";
    final String exchangeCode = exchange();
    final String companyName = "companyName";
    final Currency currency = currency();
    final EquitySecurity security = new EquitySecurity(exchange, exchangeCode, companyName, currency);
    security.addExternalId(ExternalId.of(ExternalSchemes.BLOOMBERG_TICKER, "AAPL US Equity"));
    store(security);
    return security;
  }

  public ManageablePortfolioNode createEquityNode() {
    final Collection<ManageableSecurity> securities = new ArrayList<ManageableSecurity>(NUM_SECURITIES);
    for (int i = 0; i < NUM_SECURITIES; i++) {
      resetGenerationState(i);
      securities.add(createEquitySecurity());
    }
    return createNode(EquitySecurity.SECURITY_TYPE, securities);
  }

  private EquityBarrierOptionSecurity createEquityBarrierOptionSecurity() {
    final OptionType optionType = optionType();
    final double strike = 0;
    final Currency currency = currency();
    final ExternalId underlyingId = createEquitySecurity().getExternalIdBundle().getExternalId(ExternalSchemes.BLOOMBERG_TICKER);
    final ExerciseType exerciseType = exerciseType();
    final Expiry expiry = expiry();
    final double pointValue = 0;
    final String exchange = exchange();
    final BarrierType barrierType = barrierType();
    final BarrierDirection barrierDirection = barrierDirection();
    final MonitoringType monitoringType = monitoringType();
    final SamplingFrequency samplingFrequency = samplingFrequency();
    final double barrierLevel = 0;
    final EquityBarrierOptionSecurity security = new EquityBarrierOptionSecurity(optionType, strike, currency, underlyingId, exerciseType, expiry, pointValue, exchange, barrierType,
          barrierDirection, monitoringType, samplingFrequency, barrierLevel);
    store(security);
    return security;
  }

  public ManageablePortfolioNode createEquityBarrierOptionNode() {
    final Collection<ManageableSecurity> securities = new ArrayList<ManageableSecurity>(NUM_SECURITIES);
    for (int i = 0; i < NUM_SECURITIES; i++) {
      resetGenerationState(i);
      securities.add(createEquityBarrierOptionSecurity());
    }
    return createNode(EquityBarrierOptionSecurity.SECURITY_TYPE, securities);
  }

  private EquityIndexDividendFutureOptionSecurity createEquityIndexDividendFutureOptionSecurity() {
    final String exchange = exchange();
    final Expiry expiry = expiry();
    final ExerciseType exerciseType = exerciseType();
    final ExternalId underlyingIdentifier = ExternalId.of(ExternalSchemes.BLOOMBERG_TICKER, "US0003M Index");
    final double pointValue = 0;
    final boolean margined = bool();
    final Currency currency = currency();
    final double strike = 0;
    final OptionType optionType = optionType();
    final EquityIndexDividendFutureOptionSecurity security = new EquityIndexDividendFutureOptionSecurity(exchange, expiry, exerciseType, underlyingIdentifier, pointValue, margined, currency, strike,
        optionType);
    store(security);
    return security;
  }

  public ManageablePortfolioNode loadEquityIndexDividendFutureOptionPortfolio() {
    final Collection<ManageableSecurity> securities = new ArrayList<ManageableSecurity>(NUM_SECURITIES);
    for (int i = 0; i < NUM_SECURITIES; i++) {
      resetGenerationState(i);
      securities.add(createEquityIndexDividendFutureOptionSecurity());
    }
    return createNode(EquityIndexDividendFutureOptionSecurity.SECURITY_TYPE, securities);
  }

  private EquityIndexOptionSecurity createEquityIndexOptionSecurity() {
    final OptionType optionType = optionType();
    final double strike = 0;
    final Currency currency = currency();
    final ExternalId underlyingId = ExternalId.of(ExternalSchemes.BLOOMBERG_TICKER, "AAPL US Equity");
    final ExerciseType exerciseType = exerciseType();
    final Expiry expiry = expiry();
    final double pointValue = 0;
    final String exchange = exchange();
    final EquityIndexOptionSecurity security = new EquityIndexOptionSecurity(optionType, strike, currency, underlyingId, exerciseType, expiry, pointValue, exchange);
    security.addExternalId(ExternalId.of(ExternalSchemes.BLOOMBERG_TICKER, "AAPL US 10/22/11 C365 Equity"));
    store(security);
    return security;
  }

  public ManageablePortfolioNode createEquityIndexOptionNode() {
    final Collection<ManageableSecurity> securities = new ArrayList<ManageableSecurity>(NUM_SECURITIES);
    for (int i = 0; i < NUM_SECURITIES; i++) {
      resetGenerationState(i);
      securities.add(createEquityIndexOptionSecurity());
    }
    return createNode(EquityIndexOptionSecurity.SECURITY_TYPE, securities);
  }

  private EquityOptionSecurity createEquityOptionSecurity() {
    final OptionType optionType = optionType();
    final double strike = 0;
    final Currency currency = currency();
    final ExternalId underlyingIdentifier = createEquitySecurity().getExternalIdBundle().getExternalId(ExternalSchemes.BLOOMBERG_TICKER);
    final ExerciseType exerciseType = exerciseType();
    final Expiry expiry = expiry();
    final double pointValue = 0;
    final String exchange = exchange();
    final EquityOptionSecurity security = new EquityOptionSecurity(optionType, strike, currency, underlyingIdentifier, exerciseType, expiry, pointValue, exchange);
    security.addExternalId(ExternalId.of(ExternalSchemes.BLOOMBERG_TICKER, "AAPL US 10/22/11 C365 Equity"));
    store(security);
    return security;
  }

  public ManageablePortfolioNode createEquityOptionNode() {
    final Collection<ManageableSecurity> securities = new ArrayList<ManageableSecurity>(NUM_SECURITIES);
    for (int i = 0; i < NUM_SECURITIES; i++) {
      resetGenerationState(i);
      securities.add(createEquityOptionSecurity());
    }
    return createNode(EquityOptionSecurity.SECURITY_TYPE, securities);
  }

  private EquityVarianceSwapSecurity createEquityVarianceSwapSecurity() {
    final ExternalId spotUnderlyingId = ExternalId.of(ExternalSchemes.BLOOMBERG_TICKER, "DJX Index");
    final Currency currency = currency();
    final double strike = 0.5;
    final double notional = 1e6;
    final boolean parameterizedAsVariance = bool();
    final double annualizationFactor = 250;
    final ZonedDateTime firstObservationDate = ZonedDateTime.now().minusMonths(6);
    final ZonedDateTime lastObservationDate = ZonedDateTime.now().plusMonths(6);
    final ZonedDateTime settlementDate = lastObservationDate;
    final ExternalId regionId = region();
    final Frequency observationFrequency = frequency();
    final EquityVarianceSwapSecurity security = new EquityVarianceSwapSecurity(spotUnderlyingId, currency, strike, notional, parameterizedAsVariance, annualizationFactor, firstObservationDate,
          lastObservationDate, settlementDate, regionId, observationFrequency);
    store(security);
    return security;
  }

  public ManageablePortfolioNode createEquityVarianceSwapNode() {
    final Collection<ManageableSecurity> securities = new ArrayList<ManageableSecurity>(NUM_SECURITIES);
    for (int i = 0; i < NUM_SECURITIES; i++) {
      resetGenerationState(i);
      securities.add(createEquityVarianceSwapSecurity());
    }
    return createNode(EquityVarianceSwapSecurity.SECURITY_TYPE, securities);
  }

  private FRASecurity createFRASecurity() {
    final Currency currency = currency();
    final ExternalId region = region();
    final ZonedDateTime startDate = ZonedDateTime.now().minusMonths(3);
    final ZonedDateTime endDate = ZonedDateTime.now().plusMonths(3);
    final double rate = 0.01;
    final double amount = 15e6;
    final ExternalId underlyingIdentifier = ExternalId.of(ExternalSchemes.BLOOMBERG_TICKER, "US0003M Index");
    final ZonedDateTime fixingDate = endDate.minusDays(7);
    final FRASecurity security = new FRASecurity(currency, region, startDate, endDate, rate, amount, underlyingIdentifier, fixingDate);
    store(security);
    return security;
  }

  public ManageablePortfolioNode createFRANode() {
    final Collection<ManageableSecurity> securities = new ArrayList<ManageableSecurity>(NUM_SECURITIES);
    for (int i = 0; i < NUM_SECURITIES; i++) {
      resetGenerationState(i);
      securities.add(createFRASecurity());
    }
    return createNode(FRASecurity.SECURITY_TYPE, securities);
  }

  private BondFutureSecurity createBondFutureSecurity() {
    final Expiry expiry = expiry();
    final String tradingExchange = exchange();
    final String settlementExchange = exchange();
    final Currency currency = currency();
    final double unitAmount = 0;
    final Collection<? extends BondFutureDeliverable> basket = Arrays.asList(bondFutureDeliverable(), bondFutureDeliverable());
    final ZonedDateTime firstDeliveryDate = expiry.getExpiry().minusDays(14);
    final ZonedDateTime lastDeliveryDate = expiry.getExpiry().plusDays(14);
    final String category = "category";
    final BondFutureSecurity security = new BondFutureSecurity(expiry, tradingExchange, settlementExchange, currency, unitAmount, basket, firstDeliveryDate, lastDeliveryDate, category);
    store(security);
    return security;
  }

  public ManageablePortfolioNode createBondFutureNode() {
    final Collection<ManageableSecurity> securities = new ArrayList<ManageableSecurity>(NUM_SECURITIES);
    for (int i = 0; i < NUM_SECURITIES; i++) {
      resetGenerationState(i);
      securities.add(createBondFutureSecurity());
    }
    return createNode(BondFutureSecurity.SECURITY_TYPE, securities);
  }

  private AgricultureFutureSecurity createAgricultureFutureSecurity() {
    final Expiry expiry = expiry();
    final String tradingExchange = exchange();
    final String settlementExchange = exchange();
    final Currency currency = currency();
    final double unitAmount = 0;
    final String category = "category";
    final AgricultureFutureSecurity security = new AgricultureFutureSecurity(expiry, tradingExchange, settlementExchange, currency, unitAmount, category);
    store(security);
    return security;
  }

  public ManageablePortfolioNode createAgricultureFutureNode() {
    final Collection<ManageableSecurity> securities = new ArrayList<ManageableSecurity>(NUM_SECURITIES);
    for (int i = 0; i < NUM_SECURITIES; i++) {
      resetGenerationState(i);
      securities.add(createAgricultureFutureSecurity());
    }
    return createNode(AgricultureForwardSecurity.SECURITY_TYPE, securities);
  }

  private EnergyFutureSecurity createEnergyFutureSecurity() {
    final Expiry expiry = expiry();
    final String tradingExchange = exchange();
    final String settlementExchange = exchange();
    final Currency currency = currency();
    final double unitAmount = 0;
    final String category = "category";
    final EnergyFutureSecurity security = new EnergyFutureSecurity(expiry, tradingExchange, settlementExchange, currency, unitAmount, category);
    store(security);
    return security;
  }

  public ManageablePortfolioNode createEnergyFutureNode() {
    final Collection<ManageableSecurity> securities = new ArrayList<ManageableSecurity>(NUM_SECURITIES);
    for (int i = 0; i < NUM_SECURITIES; i++) {
      resetGenerationState(i);
      securities.add(createEnergyFutureSecurity());
    }
    return createNode(EnergyFutureSecurity.SECURITY_TYPE, securities);
  }

  private MetalFutureSecurity createMetalFutureSecurity() {
    final Expiry expiry = expiry();
    final String tradingExchange = exchange();
    final String settlementExchange = exchange();
    final Currency currency = currency();
    final double unitAmount = 0;
    final String category = "category";
    final MetalFutureSecurity security = new MetalFutureSecurity(expiry, tradingExchange, settlementExchange, currency, unitAmount, category);
    store(security);
    return security;
  }

  public ManageablePortfolioNode createMetalFutureNode() {
    final Collection<ManageableSecurity> securities = new ArrayList<ManageableSecurity>(NUM_SECURITIES);
    for (int i = 0; i < NUM_SECURITIES; i++) {
      resetGenerationState(i);
      securities.add(createMetalFutureSecurity());
    }
    return createNode(MetalFutureSecurity.SECURITY_TYPE, securities);
  }

  private EquityIndexDividendFutureSecurity createEquityIndexDividendFutureSecurity() {
    final Expiry expiry = expiry();
    final String tradingExchange = exchange();
    final String settlementExchange = exchange();
    final Currency currency = currency();
    final double unitAmount = 0;
    final ZonedDateTime settlementDate = expiry.getExpiry();
    final ExternalId underlyingIdentifier = ExternalId.of(ExternalSchemes.BLOOMBERG_TICKER, "US0003M Index");
    final String category = "category";
    final EquityIndexDividendFutureSecurity security = new EquityIndexDividendFutureSecurity(expiry, tradingExchange, settlementExchange, currency, unitAmount, settlementDate, underlyingIdentifier,
          category);
    store(security);
    return security;
  }

  public ManageablePortfolioNode createEquityIndexDividendFutureNode() {
    final Collection<ManageableSecurity> securities = new ArrayList<ManageableSecurity>(NUM_SECURITIES);
    for (int i = 0; i < NUM_SECURITIES; i++) {
      resetGenerationState(i);
      securities.add(createEquityIndexDividendFutureSecurity());
    }
    return createNode(EquityIndexDividendFutureSecurity.SECURITY_TYPE, securities);
  }

  private FXFutureSecurity createFXFutureSecurity() {
    final Expiry expiry = expiry();
    final String tradingExchange = exchange();
    final String settlementExchange = exchange();
    final Currency currency = currency();
    final double unitAmount = 0;
    final Currency numerator = currency();
    final Currency denominator = differentCurrency(numerator);
    final String category = "category";
    final FXFutureSecurity security = new FXFutureSecurity(expiry, tradingExchange, settlementExchange, currency, unitAmount, numerator, denominator, category);
    store(security);
    return security;
  }

  public ManageablePortfolioNode createFXFutureNode() {
    final Collection<ManageableSecurity> securities = new ArrayList<ManageableSecurity>(NUM_SECURITIES);
    for (int i = 0; i < NUM_SECURITIES; i++) {
      resetGenerationState(i);
      securities.add(createFXFutureSecurity());
    }
    return createNode(FXFutureSecurity.SECURITY_TYPE, securities);
  }

  private IndexFutureSecurity createIndexFutureSecurity() {
    final Expiry expiry = expiry();
    final String tradingExchange = exchange();
    final String settlementExchange = exchange();
    final Currency currency = currency();
    final double unitAmount = 0;
    final String category = "category";
    final IndexFutureSecurity security = new IndexFutureSecurity(expiry, tradingExchange, settlementExchange, currency, unitAmount, category);
    store(security);
    return security;
  }

  public ManageablePortfolioNode createIndexFutureNode() {
    final Collection<ManageableSecurity> securities = new ArrayList<ManageableSecurity>(NUM_SECURITIES);
    for (int i = 0; i < NUM_SECURITIES; i++) {
      resetGenerationState(i);
      securities.add(createIndexFutureSecurity());
    }
    return createNode(IndexFutureSecurity.SECURITY_TYPE, securities);
  }

  private InterestRateFutureSecurity createInterestRateFutureSecurity() {
    final Expiry expiry = expiry();
    final String tradingExchange = exchange();
    final String settlementExchange = exchange();
    final Currency currency = currency();
    final double unitAmount = 0;
    final ExternalId underlyingIdentifier = ExternalId.of(ExternalSchemes.BLOOMBERG_TICKER, "EUR003M Index");
    final String category = "category";
    final InterestRateFutureSecurity security = new InterestRateFutureSecurity(expiry, tradingExchange, settlementExchange, currency, unitAmount, underlyingIdentifier, category);
    security.addExternalId(ExternalId.of(ExternalSchemes.BLOOMBERG_TICKER, "ERM4 Comdty"));
    store(security);
    return security;
  }

  public ManageablePortfolioNode createInterestRateFutureNode() {
    final Collection<ManageableSecurity> securities = new ArrayList<ManageableSecurity>(NUM_SECURITIES);
    for (int i = 0; i < NUM_SECURITIES; i++) {
      resetGenerationState(i);
      securities.add(createInterestRateFutureSecurity());
    }
    return createNode(InterestRateFutureSecurity.SECURITY_TYPE, securities);
  }

  private StockFutureSecurity createStockFutureSecurity() {
    final Expiry expiry = expiry();
    final String tradingExchange = exchange();
    final String settlementExchange = exchange();
    final Currency currency = currency();
    final double unitAmount = 0;
    final String category = "category";
    final StockFutureSecurity security = new StockFutureSecurity(expiry, tradingExchange, settlementExchange, currency, unitAmount, category);
    store(security);
    return security;
  }

  public ManageablePortfolioNode createStockFutureNode() {
    final Collection<ManageableSecurity> securities = new ArrayList<ManageableSecurity>(NUM_SECURITIES);
    for (int i = 0; i < NUM_SECURITIES; i++) {
      resetGenerationState(i);
      securities.add(createStockFutureSecurity());
    }
    return createNode(StockFutureSecurity.SECURITY_TYPE, securities);
  }

  public void createFutureNodes(List<ManageablePortfolioNode> nodes) {
    nodes.add(createBondFutureNode());
    nodes.add(createAgricultureFutureNode());
    nodes.add(createEnergyFutureNode());
    nodes.add(createMetalFutureNode());
    nodes.add(createEquityIndexDividendFutureNode());
    nodes.add(createFXFutureNode());
    nodes.add(createIndexFutureNode());
    nodes.add(createInterestRateFutureNode());
    nodes.add(createStockFutureNode());
  }

  private FXBarrierOptionSecurity createFXBarrierOptionSecurity() {
    final Currency putCurrency = currency();
    final Currency callCurrency = differentCurrency(putCurrency);
    final double putAmount = 0;
    final double callAmount = 0;
    final Expiry expiry = expiry();
    final ZonedDateTime settlementDate = expiry.getExpiry();
    final BarrierType barrierType = barrierType();
    final BarrierDirection barrierDirection = barrierDirection();
    final MonitoringType monitoringType = monitoringType();
    final SamplingFrequency samplingFrequency = samplingFrequency();
    final double barrierLevel = 0;
    final boolean isLong = bool();
    final FXBarrierOptionSecurity security = new FXBarrierOptionSecurity(putCurrency, callCurrency, putAmount, callAmount, expiry, settlementDate, barrierType, barrierDirection, monitoringType,
          samplingFrequency, barrierLevel, isLong);
    store(security);
    return security;
  }

  public ManageablePortfolioNode createFXBarrierOptionNode() {
    final Collection<ManageableSecurity> securities = new ArrayList<ManageableSecurity>(NUM_SECURITIES);
    for (int i = 0; i < NUM_SECURITIES; i++) {
      resetGenerationState(i);
      securities.add(createFXBarrierOptionSecurity());
    }
    return createNode(FXBarrierOptionSecurity.SECURITY_TYPE, securities);
  }

  private FXDigitalOptionSecurity createFXDigitalOptionSecurity() {
    final Currency putCurrency = currency();
    final Currency callCurrency = differentCurrency(putCurrency);
    final double putAmount = 0;
    final double callAmount = 0;
    final Currency paymentCurrency = currency();
    final Expiry expiry = expiry();
    final ZonedDateTime settlementDate = expiry.getExpiry();
    final boolean isLong = bool();
    final FXDigitalOptionSecurity security = new FXDigitalOptionSecurity(putCurrency, callCurrency, putAmount, callAmount, paymentCurrency, expiry, settlementDate, isLong);
    store(security);
    return security;
  }

  public ManageablePortfolioNode createFXDigitalOptionNode() {
    final Collection<ManageableSecurity> securities = new ArrayList<ManageableSecurity>(NUM_SECURITIES);
    for (int i = 0; i < NUM_SECURITIES; i++) {
      resetGenerationState(i);
      securities.add(createFXDigitalOptionSecurity());
    }
    return createNode(FXDigitalOptionSecurity.SECURITY_TYPE, securities);
  }

  private FXForwardSecurity createFXForwardSecurity() {
    final Currency payCurrency = currency();
    final double payAmount = 0;
    final Currency receiveCurrency = differentCurrency(payCurrency);
    final double receiveAmount = 0;
    final ZonedDateTime forwardDate = ZonedDateTime.now().plusMonths(7);
    final ExternalId region = region();
    final FXForwardSecurity security = new FXForwardSecurity(payCurrency, payAmount, receiveCurrency, receiveAmount, forwardDate, region);
    store(security);
    return security;
  }

  public ManageablePortfolioNode createFXForwardNode() {
    final Collection<ManageableSecurity> securities = new ArrayList<ManageableSecurity>(NUM_SECURITIES);
    for (int i = 0; i < NUM_SECURITIES; i++) {
      resetGenerationState(i);
      securities.add(createFXForwardSecurity());
    }
    return createNode(FXFutureSecurity.SECURITY_TYPE, securities);
  }

  private FXOptionSecurity createFXOptionSecurity() {
    final Currency putCurrency = currency();
    final Currency callCurrency = differentCurrency(putCurrency);
    final double putAmount = 0;
    final double callAmount = 0;
    final Expiry expiry = expiry();
    final ZonedDateTime settlementDate = expiry.getExpiry();
    final boolean isLong = bool();
    final ExerciseType exerciseType = exerciseType();
    final FXOptionSecurity security = new FXOptionSecurity(putCurrency, callCurrency, putAmount, callAmount, expiry, settlementDate, isLong, exerciseType);
    store(security);
    return security;
  }

  public ManageablePortfolioNode createFXOptionNode() {
    final Collection<ManageableSecurity> securities = new ArrayList<ManageableSecurity>(NUM_SECURITIES);
    for (int i = 0; i < NUM_SECURITIES; i++) {
      resetGenerationState(i);
      securities.add(createFXOptionSecurity());
    }
    return createNode(FXOptionSecurity.SECURITY_TYPE, securities);
  }

  private IRFutureOptionSecurity createIRFutureOptionSecurity() {
    final String exchange = exchange();
    final Expiry expiry = expiry();
    final ExerciseType exerciseType = exerciseType();
    final ExternalId underlyingIdentifier = createInterestRateFutureSecurity().getExternalIdBundle().getExternalId(_security);
    final double pointValue = 0;
    final boolean margined = bool();
    final Currency currency = Currency.USD; // currency(); // Only got a USD surface at the moment
    final double strike = 0;
    final OptionType optionType = optionType();
    final IRFutureOptionSecurity security = new IRFutureOptionSecurity(exchange, expiry, exerciseType, underlyingIdentifier, pointValue, margined, currency, strike, optionType);
    security.addExternalId(ExternalId.of(ExternalSchemes.BLOOMBERG_TICKER, "US0003M Index"));
    store(security);
    return security;
  }

  public ManageablePortfolioNode createIRFutureOptionNode() {
    final Collection<ManageableSecurity> securities = new ArrayList<ManageableSecurity>(NUM_SECURITIES);
    for (int i = 0; i < NUM_SECURITIES; i++) {
      resetGenerationState(i);
      securities.add(createIRFutureOptionSecurity());
    }
    return createNode(IRFutureOptionSecurity.SECURITY_TYPE, securities);
  }

  private NonDeliverableFXDigitalOptionSecurity createNonDeliverableFXDigitalOptionSecurity() {
    final Currency putCurrency = currency();
    final Currency callCurrency = differentCurrency(putCurrency);
    final double putAmount = 0;
    final double callAmount = 0;
    final Currency paymentCurrency = currency();
    final Expiry expiry = expiry();
    final ZonedDateTime settlementDate = expiry.getExpiry();
    final boolean isLong = bool();
    final boolean deliverInCallCurrency = bool();
    final NonDeliverableFXDigitalOptionSecurity security = new NonDeliverableFXDigitalOptionSecurity(putCurrency, callCurrency, putAmount, callAmount, paymentCurrency, expiry, settlementDate, isLong,
        deliverInCallCurrency);
    store(security);
    return security;
  }

  public ManageablePortfolioNode createNonDeliverableFXDigitalOptionNode() {
    final Collection<ManageableSecurity> securities = new ArrayList<ManageableSecurity>(NUM_SECURITIES);
    for (int i = 0; i < NUM_SECURITIES; i++) {
      resetGenerationState(i);
      securities.add(createNonDeliverableFXDigitalOptionSecurity());
    }
    return createNode(NonDeliverableFXDigitalOptionSecurity.SECURITY_TYPE, securities);
  }

  private NonDeliverableFXForwardSecurity createNonDeliverableFXForwardSecurity() {
    final Currency payCurrency = currency();
    final double payAmount = 0;
    final Currency receiveCurrency = differentCurrency(payCurrency);
    final double receiveAmount = 0;
    final ZonedDateTime forwardDate = ZonedDateTime.now().plusMonths(7);
    final ExternalId region = region();
    final boolean deliverInReceiveCurrency = bool();
    final NonDeliverableFXForwardSecurity security = new NonDeliverableFXForwardSecurity(payCurrency, payAmount, receiveCurrency, receiveAmount, forwardDate, region, deliverInReceiveCurrency);
    store(security);
    return security;
  }

  public ManageablePortfolioNode createNonDeliverableFXForwardNode() {
    final Collection<ManageableSecurity> securities = new ArrayList<ManageableSecurity>(NUM_SECURITIES);
    for (int i = 0; i < NUM_SECURITIES; i++) {
      resetGenerationState(i);
      securities.add(createNonDeliverableFXForwardSecurity());
    }
    return createNode(NonDeliverableFXForwardSecurity.SECURITY_TYPE, securities);
  }

  private NonDeliverableFXOptionSecurity createNonDeliverableFXOptionSecurity() {
    final Currency putCurrency = currency();
    final Currency callCurrency = differentCurrency(putCurrency);
    final double putAmount = 0;
    final double callAmount = 0;
    final Expiry expiry = expiry();
    final ZonedDateTime settlementDate = expiry.getExpiry();
    final boolean isLong = bool();
    final ExerciseType exerciseType = exerciseType();
    final boolean deliveryInCallCurrency = bool();
    final NonDeliverableFXOptionSecurity security = new NonDeliverableFXOptionSecurity(putCurrency, callCurrency, putAmount, callAmount, expiry, settlementDate, isLong, exerciseType,
        deliveryInCallCurrency);
    store(security);
    return security;
  }

  public ManageablePortfolioNode createNonDeliverableFXOptionNode() {
    final Collection<ManageableSecurity> securities = new ArrayList<ManageableSecurity>(NUM_SECURITIES);
    for (int i = 0; i < NUM_SECURITIES; i++) {
      resetGenerationState(i);
      securities.add(createNonDeliverableFXOptionSecurity());
    }
    return createNode(NonDeliverableFXOptionSecurity.SECURITY_TYPE, securities);
  }

  private PeriodicZeroDepositSecurity createPeriodicZeroDepositSecurity() {
    final Currency currency = currency();
    final ZonedDateTime startDate = ZonedDateTime.now().minusMonths(6);
    final ZonedDateTime maturityDate = ZonedDateTime.now().plusMonths(6);
    final double rate = 0.02;
    final double compoundingPeriodsPerYear = 12;
    final ExternalId region = region();
    final PeriodicZeroDepositSecurity security = new PeriodicZeroDepositSecurity(currency, startDate, maturityDate, rate, compoundingPeriodsPerYear, region);
    store(security);
    return security;
  }

  public ManageablePortfolioNode loadPeriodicZeroDepositPortfolio() {
    final Collection<ManageableSecurity> securities = new ArrayList<ManageableSecurity>(NUM_SECURITIES);
    for (int i = 0; i < NUM_SECURITIES; i++) {
      resetGenerationState(i);
      securities.add(createPeriodicZeroDepositSecurity());
    }
    return createNode(PeriodicZeroDepositSecurity.SECURITY_TYPE, securities);
  }

  /*
  private SimpleZeroDepositSecurity createSimpleZeroDepositSecurity(final int i) {
    final Currency currency = null;
    final ZonedDateTime startDate = null;
    final ZonedDateTime maturityDate = null;
    final double rate = 0;
    final ExternalId region = region();
    // SimpleZeroDepositSecurity doesn't have a public constructor?
    final SimpleZeroDepositSecurity security = new SimpleZeroDepositSecurity(currency, startDate, maturityDate, rate, region);
    store(security);
    return security;
  }
  */

  /*
  public void loadSimpleZeroDepositPortfolio() {
    final Collection<ManageableSecurity> securities = new ArrayList<ManageableSecurity>(NUM_SECURITIES);
    for (int i = 0; i < NUM_SECURITIES; i++) {
      securities.add(createSimpleZeroDepositSecurity(i));
    }
    load(securities);
  }
  */

  private ForwardSwapSecurity createForwardSwapSecurity() {
    final ZonedDateTime tradeDate = ZonedDateTime.now().minusMonths(6);
    final ZonedDateTime effectiveDate = ZonedDateTime.now().plusMonths(23);
    final ZonedDateTime maturityDate = ZonedDateTime.now().plusMonths(25);
    final String counterparty = "counterparty";
    final Notional notional = interestRateNotional();
    final boolean payLegFixed = bool();
    final SwapLeg payLeg = payLegFixed ? fixedInterestRateLeg(notional) : floatingInterestRateLeg(notional);
    final SwapLeg receiveLeg = payLegFixed ? floatingInterestRateLeg(notional) : fixedInterestRateLeg(notional);
    final ZonedDateTime forwardStartDate = ZonedDateTime.now().plusMonths(12);
    final ForwardSwapSecurity security = new ForwardSwapSecurity(tradeDate, effectiveDate, maturityDate, counterparty, payLeg, receiveLeg, forwardStartDate);
    store(security);
    return security;
  }

  public ManageablePortfolioNode createForwardSwapNode() {
    final Collection<ManageableSecurity> securities = new ArrayList<ManageableSecurity>(NUM_SECURITIES);
    for (int i = 0; i < NUM_SECURITIES; i++) {
      resetGenerationState(i);
      securities.add(createForwardSwapSecurity());
    }
    return createNode(ForwardSwapSecurity.SECURITY_TYPE, securities);
  }

  private SwapSecurity createSwapSecurity() {
    final ZonedDateTime tradeDate = ZonedDateTime.now().minusMonths(6);
    final ZonedDateTime effectiveDate = ZonedDateTime.now().plusMonths(23);
    final ZonedDateTime maturityDate = ZonedDateTime.now().plusMonths(25);
    final String counterparty = "counterparty";
    final Notional notional = interestRateNotional();
    final boolean payLegFixed = bool();
    final SwapLeg payLeg = payLegFixed ? fixedInterestRateLeg(notional) : floatingInterestRateLeg(notional);
    final SwapLeg receiveLeg = payLegFixed ? floatingInterestRateLeg(notional) : fixedInterestRateLeg(notional);
    final SwapSecurity security = new SwapSecurity(tradeDate, effectiveDate, maturityDate, counterparty, payLeg, receiveLeg);
    store(security);
    return security;
  }

  public ManageablePortfolioNode createSwapNode() {
    final Collection<ManageableSecurity> securities = new ArrayList<ManageableSecurity>(NUM_SECURITIES);
    for (int i = 0; i < NUM_SECURITIES; i++) {
      resetGenerationState(i);
      securities.add(createSwapSecurity());
    }
    return createNode(SwapSecurity.SECURITY_TYPE, securities);
  }

  private SwaptionSecurity createSwaptionSecurity() {
    final boolean payer = bool();
    final ExternalId underlyingIdentifier = createSwapSecurity().getExternalIdBundle().getExternalId(_security);
    final boolean isLong = bool();
    final Expiry expiry = expiry();
    final boolean cashSettled = bool();
    final Currency currency = currency();
    final SwaptionSecurity security = new SwaptionSecurity(payer, underlyingIdentifier, isLong, expiry, cashSettled, currency);
    store(security);
    return security;
  }

  public ManageablePortfolioNode createSwaptionNode() {
    final Collection<ManageableSecurity> securities = new ArrayList<ManageableSecurity>(NUM_SECURITIES);
    for (int i = 0; i < NUM_SECURITIES; i++) {
      resetGenerationState(i);
      securities.add(createSwaptionSecurity());
    }
    return createNode(SwaptionSecurity.SECURITY_TYPE, securities);
  }

  @Override
  public void run() {
    List<ManageablePortfolioNode> nodes = Lists.newArrayList();
    nodes.add(createBondFutureOptionNode());
    createBondPortfolioNodes(nodes);
    
    nodes.add(createCapFloorCMSSpreadNode());
    nodes.add(createCapFloorNode());
    nodes.add(createCashNode());
    //loadCDSPortfolio()); // Should we include the riskcare model?
    //loadCommodityForwardPortfolio()); // Doesn't produce any values (v1.2.x)
    nodes.add(createCommodityFutureOptionNode());
    //loadContinuousZeroDepositPortfolio()); // Security doesn't have public constructor
    //loadCreditDefaultSwapPortfolio()); // Doesn't produce any values (v1.2.x)
    nodes.add(createEquityBarrierOptionNode());
    //loadEquityIndexDividendFutureOptionPortfolio()); // Doesn't produce any values (v1.2.x)
    nodes.add(createEquityIndexOptionNode());
    nodes.add(createEquityOptionNode());
    nodes.add(createEquityNode());
    nodes.add(createEquityVarianceSwapNode());
    // TODO: should we include external sensitivity securities?
    nodes.add(createFRANode());
    createFutureNodes(nodes);
    nodes.add(createFXBarrierOptionNode());
    nodes.add(createFXDigitalOptionNode());
    nodes.add(createFXForwardNode());
    nodes.add(createFXOptionNode());
    nodes.add(createIRFutureOptionNode());
    nodes.add(createNonDeliverableFXDigitalOptionNode());
    nodes.add(createNonDeliverableFXForwardNode());
    nodes.add(createNonDeliverableFXOptionNode());
    //loadPeriodicZeroDepositPortfolio()); // Doesn't produce any values (v1.2.x)
    //loadSimpleZeroDepositPortfolio()); // Security doesn't have public constructor
    nodes.add(createSwapNode());
    nodes.add(createForwardSwapNode());
    nodes.add(createSwaptionNode());
    
    createPortfolio(PORTFOLIO_NAME, nodes);
  }

}
