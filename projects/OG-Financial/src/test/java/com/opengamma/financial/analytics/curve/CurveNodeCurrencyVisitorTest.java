/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.curve;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.testng.annotations.Test;
import org.threeten.bp.LocalTime;

import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.interestrate.CompoundingType;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.financial.analytics.ircurve.strips.CashNode;
import com.opengamma.financial.analytics.ircurve.strips.ContinuouslyCompoundedRateNode;
import com.opengamma.financial.analytics.ircurve.strips.CreditSpreadNode;
import com.opengamma.financial.analytics.ircurve.strips.DiscountFactorNode;
import com.opengamma.financial.analytics.ircurve.strips.FRANode;
import com.opengamma.financial.analytics.ircurve.strips.FXForwardNode;
import com.opengamma.financial.analytics.ircurve.strips.InflationNodeType;
import com.opengamma.financial.analytics.ircurve.strips.RateFutureNode;
import com.opengamma.financial.analytics.ircurve.strips.SwapNode;
import com.opengamma.financial.analytics.ircurve.strips.ZeroCouponInflationNode;
import com.opengamma.financial.convention.CMSLegConvention;
import com.opengamma.financial.convention.CompoundingIborLegConvention;
import com.opengamma.financial.convention.Convention;
import com.opengamma.financial.convention.ConventionSource;
import com.opengamma.financial.convention.DepositConvention;
import com.opengamma.financial.convention.FXSpotConvention;
import com.opengamma.financial.convention.IMMFutureAndFutureOptionQuarterlyExpiryCalculator;
import com.opengamma.financial.convention.IborIndexConvention;
import com.opengamma.financial.convention.InflationLegConvention;
import com.opengamma.financial.convention.InterestRateFutureConvention;
import com.opengamma.financial.convention.OISLegConvention;
import com.opengamma.financial.convention.OvernightIndexConvention;
import com.opengamma.financial.convention.PriceIndexConvention;
import com.opengamma.financial.convention.StubType;
import com.opengamma.financial.convention.SwapFixedLegConvention;
import com.opengamma.financial.convention.SwapIndexConvention;
import com.opengamma.financial.convention.VanillaIborLegConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.Tenor;

/**
 *
 */
@Test(groups = TestGroup.UNIT)
public class CurveNodeCurrencyVisitorTest {
  private static final String SCHEME = "Test";
  private static final BusinessDayConvention MODIFIED_FOLLOWING = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Modified Following");
  private static final DayCount ACT_360 = DayCountFactory.INSTANCE.getDayCount("Actual/360");
  private static final DayCount THIRTY_360 = DayCountFactory.INSTANCE.getDayCount("30/360");
  private static final ExternalId US = ExternalSchemes.financialRegionId("US");
  private static final ExternalId EU = ExternalSchemes.financialRegionId("EU");
  private static final ExternalId NYLON = ExternalSchemes.financialRegionId("US+GB");
  private static final ExternalId FIXED_LEG_ID = ExternalId.of(SCHEME, "USD Swap Fixed Leg");
  private static final ExternalId DEPOSIT_1M_ID = ExternalId.of(SCHEME, "USD 1m Deposit");
  private static final ExternalId LIBOR_3M_ID = ExternalId.of(SCHEME, "USD 3m Libor");
  private static final ExternalId EURIBOR_6M_ID = ExternalId.of(SCHEME, "EUR 6m Euribor");
  private static final ExternalId RATE_FUTURE_3M_ID = ExternalId.of(SCHEME, "USD 3m Rate Future");
  private static final ExternalId SWAP_3M_IBOR_ID = ExternalId.of(SCHEME, "USD 3m Floating Leg");
  private static final ExternalId SWAP_6M_EURIBOR_ID = ExternalId.of(SCHEME, "EUR 6m Floating Leg");
  private static final ExternalId OVERNIGHT_ID = ExternalId.of(SCHEME, "USD Overnight");
  private static final ExternalId OIS_ID = ExternalId.of(SCHEME, "USD OIS Leg");
  private static final ExternalId FX_FORWARD_ID = ExternalId.of(SCHEME, "FX Forward");
  private static final ExternalId SWAP_INDEX_ID = ExternalId.of(SCHEME, "3M Swap Index");
  private static final ExternalId CMS_SWAP_ID = ExternalId.of(SCHEME, "USD CMS");
  private static final ExternalId COMPOUNDING_IBOR_ID = ExternalId.of(SCHEME, "USD Compounding Libor");
  private static final ExternalId IMM_3M_EXPIRY_CONVENTION = ExternalId.of(SCHEME, IMMFutureAndFutureOptionQuarterlyExpiryCalculator.NAME);
  private static final ExternalId PRICE_INDEX_ID = ExternalId.of(SCHEME, "USD CPI");
  private static final ExternalId ZERO_COUPON_INFLATION_ID = ExternalId.of(SCHEME, "ZCI");
  private static final SwapFixedLegConvention FIXED_LEG = new SwapFixedLegConvention("USD Swap Fixed Leg", ExternalIdBundle.of(ExternalId.of(SCHEME, "USD Swap Fixed Leg")),
      Tenor.SIX_MONTHS, ACT_360, MODIFIED_FOLLOWING, Currency.USD, NYLON, 2, false, StubType.NONE, false, 2);
  private static final VanillaIborLegConvention SWAP_3M_LIBOR = new VanillaIborLegConvention("USD 3m Floating Leg", ExternalIdBundle.of(ExternalId.of(SCHEME, "USD 3m Floating Leg")),
      LIBOR_3M_ID, false, SCHEME, Tenor.THREE_MONTHS, 2, false, StubType.NONE, false, 2);
  private static final VanillaIborLegConvention SWAP_6M_EURIBOR = new VanillaIborLegConvention("EUR 6m Floating Leg", ExternalIdBundle.of(ExternalId.of(SCHEME, "EUR 6m Floating Leg")),
      EURIBOR_6M_ID, false, SCHEME, Tenor.SIX_MONTHS, 2, false, StubType.NONE, false,2 );
  private static final OISLegConvention OIS = new OISLegConvention("USD OIS Leg", ExternalIdBundle.of(ExternalId.of(SCHEME, "USD OIS Leg")), OVERNIGHT_ID,
      Tenor.ONE_YEAR, MODIFIED_FOLLOWING, 2, false, StubType.NONE, false, 1);
  private static final DepositConvention DEPOSIT_1M = new DepositConvention("USD 1m Deposit", ExternalIdBundle.of(DEPOSIT_1M_ID),
      ACT_360, MODIFIED_FOLLOWING, 2, false, Currency.USD, US);
  private static final IborIndexConvention LIBOR_3M = new IborIndexConvention("USD 3m Libor", ExternalIdBundle.of(LIBOR_3M_ID),
      THIRTY_360, MODIFIED_FOLLOWING, 2, false, Currency.USD, LocalTime.of(11, 0), "US", US, US, "Page");
  private static final IborIndexConvention EURIBOR_6M = new IborIndexConvention("EUR 6m Libor", ExternalIdBundle.of(EURIBOR_6M_ID),
      THIRTY_360, MODIFIED_FOLLOWING, 2, false, Currency.EUR, LocalTime.of(11, 0), "EU", EU, EU, "Page");
  private static final InterestRateFutureConvention RATE_FUTURE_3M = new InterestRateFutureConvention("USD 3m Rate Future", ExternalIdBundle.of(RATE_FUTURE_3M_ID),
      IMM_3M_EXPIRY_CONVENTION, NYLON, LIBOR_3M_ID);
  private static final OvernightIndexConvention OVERNIGHT = new OvernightIndexConvention("USD Overnight", ExternalIdBundle.of(ExternalId.of(SCHEME, "USD Overnight")),
      ACT_360, 1, Currency.USD, NYLON);
  private static final SwapIndexConvention SWAP_INDEX = new SwapIndexConvention("3M Swap Index", ExternalIdBundle.of(ExternalId.of(SCHEME, "3M Swap Index")), LocalTime.of(11, 0),
      SWAP_3M_IBOR_ID);
  private static final CompoundingIborLegConvention COMPOUNDING_IBOR = new CompoundingIborLegConvention("USD Compounding Libor", ExternalIdBundle.of(ExternalId.of(SCHEME, "USD Compounding Libor")),
      LIBOR_3M_ID, Tenor.THREE_MONTHS, CompoundingType.COMPOUNDING, Tenor.ONE_MONTH, StubType.SHORT_START, 2, false, StubType.LONG_START, true, 1);
  private static final PriceIndexConvention PRICE_INDEX = new PriceIndexConvention("USD CPI", ExternalIdBundle.of(ExternalId.of(SCHEME, "USD CPI")), Currency.USD, US,
      ExternalId.of("TS", "CPI"));
  private static final InflationLegConvention INFLATION_LEG = new InflationLegConvention("ZCI", ExternalIdBundle.of(ExternalId.of(SCHEME, "ZCI")), MODIFIED_FOLLOWING, ACT_360, false,
      3, 2, PRICE_INDEX_ID);
  private static final CMSLegConvention CMS = new CMSLegConvention("USD CMS", ExternalIdBundle.of(ExternalId.of(SCHEME, "USD CMS")), SWAP_INDEX_ID, Tenor.SIX_MONTHS, false);
  private static final Map<ExternalId, Convention> CONVENTIONS = new HashMap<>();
  private static final ConventionSource CONVENTION_SOURCE;
  private static final CurveNodeCurrencyVisitor VISITOR;
  private static final CurveNodeCurrencyVisitor EMPTY_CONVENTIONS = new CurveNodeCurrencyVisitor(new TestConventionSource(Collections.<ExternalId, Convention>emptyMap()));

  static {
    CONVENTIONS.put(DEPOSIT_1M_ID, DEPOSIT_1M);
    CONVENTIONS.put(FIXED_LEG_ID, FIXED_LEG);
    CONVENTIONS.put(LIBOR_3M_ID, LIBOR_3M);
    CONVENTIONS.put(RATE_FUTURE_3M_ID, RATE_FUTURE_3M);
    CONVENTIONS.put(SWAP_3M_IBOR_ID, SWAP_3M_LIBOR);
    CONVENTIONS.put(OIS_ID, OIS);
    CONVENTIONS.put(OVERNIGHT_ID, OVERNIGHT);
    CONVENTIONS.put(SWAP_INDEX_ID, SWAP_INDEX);
    CONVENTIONS.put(CMS_SWAP_ID, CMS);
    CONVENTIONS.put(EURIBOR_6M_ID, EURIBOR_6M);
    CONVENTIONS.put(SWAP_6M_EURIBOR_ID, SWAP_6M_EURIBOR);
    CONVENTIONS.put(COMPOUNDING_IBOR_ID, COMPOUNDING_IBOR);
    CONVENTIONS.put(PRICE_INDEX_ID, PRICE_INDEX);
    CONVENTIONS.put(ZERO_COUPON_INFLATION_ID, INFLATION_LEG);
    CONVENTION_SOURCE = new TestConventionSource(CONVENTIONS);
    VISITOR = new CurveNodeCurrencyVisitor(CONVENTION_SOURCE);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullConventionSource() {
    new CurveNodeCurrencyVisitor(null);
  }

  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void testNullCashConvention() {
    final CashNode node = new CashNode(Tenor.ONE_DAY, Tenor.ONE_WEEK, DEPOSIT_1M_ID, SCHEME);
    node.accept(EMPTY_CONVENTIONS);
  }

  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void testNullFRAConvention() {
    final FRANode node = new FRANode(Tenor.ONE_DAY, Tenor.THREE_MONTHS, LIBOR_3M_ID, SCHEME);
    node.accept(EMPTY_CONVENTIONS);
  }

  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void testNullRateFutureConvention() {
    final RateFutureNode node = new RateFutureNode(2, Tenor.ONE_DAY, Tenor.THREE_MONTHS, Tenor.THREE_MONTHS, RATE_FUTURE_3M_ID, LIBOR_3M_ID, SCHEME);
    node.accept(EMPTY_CONVENTIONS);
  }

  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void testNullRateFutureUnderlyingConvention() {
    final Map<ExternalId, Convention> map = new HashMap<>();
    map.put(RATE_FUTURE_3M_ID, RATE_FUTURE_3M);
    final CurveNodeCurrencyVisitor visitor = new CurveNodeCurrencyVisitor(new TestConventionSource(map));
    final RateFutureNode node = new RateFutureNode(2, Tenor.ONE_DAY, Tenor.THREE_MONTHS, Tenor.THREE_MONTHS, RATE_FUTURE_3M_ID, LIBOR_3M_ID, SCHEME);
    node.accept(visitor);
  }

  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void testNullSwapPayConvention() {
    final SwapNode node = new SwapNode(Tenor.ONE_DAY, Tenor.TEN_YEARS, FIXED_LEG_ID, SWAP_3M_IBOR_ID, SCHEME);
    node.accept(EMPTY_CONVENTIONS);
  }

  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void testNullSwapReceiveConvention() {
    final Map<ExternalId, Convention> map = new HashMap<>();
    map.put(FIXED_LEG_ID, FIXED_LEG);
    final CurveNodeCurrencyVisitor visitor = new CurveNodeCurrencyVisitor(new TestConventionSource(map));
    final SwapNode node = new SwapNode(Tenor.ONE_DAY, Tenor.TEN_YEARS, FIXED_LEG_ID, SWAP_3M_IBOR_ID, SCHEME);
    node.accept(visitor);
  }

  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void testNullIborUnderlyingConvention() {
    final Map<ExternalId, Convention> map = new HashMap<>();
    map.put(FIXED_LEG_ID, FIXED_LEG);
    map.put(SWAP_3M_IBOR_ID, SWAP_3M_LIBOR);
    final CurveNodeCurrencyVisitor visitor = new CurveNodeCurrencyVisitor(new TestConventionSource(map));
    final SwapNode node = new SwapNode(Tenor.ONE_DAY, Tenor.TEN_YEARS, FIXED_LEG_ID, SWAP_3M_IBOR_ID, SCHEME);
    node.accept(visitor);
  }

  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void testNullCMSIndexUnderlyingConvention() {
    final Map<ExternalId, Convention> map = new HashMap<>();
    map.put(FIXED_LEG_ID, FIXED_LEG);
    map.put(SWAP_INDEX_ID, SWAP_INDEX);
    final CurveNodeCurrencyVisitor visitor = new CurveNodeCurrencyVisitor(new TestConventionSource(map));
    final SwapNode node = new SwapNode(Tenor.ONE_DAY, Tenor.TEN_YEARS, FIXED_LEG_ID, SWAP_INDEX_ID, SCHEME);
    node.accept(visitor);
  }

  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void testNullCMSUnderlyingConvention() {
    final Map<ExternalId, Convention> map = new HashMap<>();
    map.put(FIXED_LEG_ID, FIXED_LEG);
    map.put(CMS_SWAP_ID, CMS);
    final CurveNodeCurrencyVisitor visitor = new CurveNodeCurrencyVisitor(new TestConventionSource(map));
    final SwapNode node = new SwapNode(Tenor.ONE_DAY, Tenor.TEN_YEARS, FIXED_LEG_ID, CMS_SWAP_ID, SCHEME);
    node.accept(visitor);
  }

  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void testNullOISUnderlyingConvention() {
    final Map<ExternalId, Convention> map = new HashMap<>();
    map.put(FIXED_LEG_ID, FIXED_LEG);
    map.put(OIS_ID, OIS);
    final CurveNodeCurrencyVisitor visitor = new CurveNodeCurrencyVisitor(new TestConventionSource(map));
    final SwapNode node = new SwapNode(Tenor.ONE_DAY, Tenor.TEN_YEARS, FIXED_LEG_ID, OIS_ID, SCHEME);
    node.accept(visitor);
  }

  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void testNullFixedLegConvention() {
    final Map<ExternalId, Convention> map = new HashMap<>();
    map.put(OIS_ID, OIS);
    final CurveNodeCurrencyVisitor visitor = new CurveNodeCurrencyVisitor(new TestConventionSource(map));
    final SwapNode node = new SwapNode(Tenor.ONE_DAY, Tenor.TEN_YEARS, FIXED_LEG_ID, OIS_ID, SCHEME);
    node.accept(visitor);
  }

  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void testNullCompoundingIborLegConvention() {
    final Map<ExternalId, Convention> map = new HashMap<>();
    final CompoundingIborLegConvention compoundingIbor = new CompoundingIborLegConvention("USD Compounding Libor", ExternalIdBundle.of(ExternalId.of(SCHEME, "USD Compounding Libor")),
        LIBOR_3M_ID, Tenor.THREE_MONTHS, CompoundingType.COMPOUNDING, Tenor.ONE_MONTH, StubType.SHORT_START, 2, false, StubType.LONG_START, true, 1);
    map.put(FIXED_LEG_ID, FIXED_LEG);
    map.put(COMPOUNDING_IBOR_ID, compoundingIbor);
    final CurveNodeCurrencyVisitor visitor = new CurveNodeCurrencyVisitor(new TestConventionSource(map));
    final SwapNode node = new SwapNode(Tenor.ONE_DAY, Tenor.TEN_YEARS, FIXED_LEG_ID, COMPOUNDING_IBOR_ID, SCHEME);
    node.accept(visitor);
  }

  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void testWrongUnderlyingCompoundingIborLegConvention() {
    final Map<ExternalId, Convention> map = new HashMap<>();
    map.put(FIXED_LEG_ID, FIXED_LEG);
    map.put(COMPOUNDING_IBOR_ID, COMPOUNDING_IBOR);
    final CurveNodeCurrencyVisitor visitor = new CurveNodeCurrencyVisitor(new TestConventionSource(map));
    final SwapNode node = new SwapNode(Tenor.ONE_DAY, Tenor.TEN_YEARS, FIXED_LEG_ID, COMPOUNDING_IBOR_ID, SCHEME);
    node.accept(visitor);
  }

  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void testNullZeroCouponInflationConvention() {
    final ZeroCouponInflationNode node = new ZeroCouponInflationNode(Tenor.EIGHT_MONTHS, ZERO_COUPON_INFLATION_ID, FIXED_LEG_ID, InflationNodeType.INTERPOLATED, "TEST");
    node.accept(EMPTY_CONVENTIONS);
  }

  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void testNullPriceIndexConvention() {
    final Map<ExternalId, Convention> map = new HashMap<>();
    map.put(ZERO_COUPON_INFLATION_ID, INFLATION_LEG);
    final CurveNodeCurrencyVisitor visitor = new CurveNodeCurrencyVisitor(new TestConventionSource(map));
    final ZeroCouponInflationNode node = new ZeroCouponInflationNode(Tenor.EIGHT_MONTHS, ZERO_COUPON_INFLATION_ID, FIXED_LEG_ID, InflationNodeType.MONTHLY, "TEST");
    node.accept(visitor);
  }

  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void testWrongTypeZeroCouponInflationConvention() {
    final ZeroCouponInflationNode node = new ZeroCouponInflationNode(Tenor.EIGHT_MONTHS, SWAP_3M_IBOR_ID, FIXED_LEG_ID, InflationNodeType.INTERPOLATED, "TEST");
    node.accept(VISITOR);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testUnhandledConvention() {
    final FXSpotConvention convention = new FXSpotConvention("Test", ExternalIdBundle.of(ExternalId.of("Test", "Test")), 0, US);
    final Map<ExternalId, Convention> map = new HashMap<>();
    map.put(FIXED_LEG_ID, FIXED_LEG);
    map.put(ExternalId.of("Test", "Test"), convention);
    final CurveNodeCurrencyVisitor visitor = new CurveNodeCurrencyVisitor(new TestConventionSource(map));
    final SwapNode node = new SwapNode(Tenor.ONE_DAY, Tenor.TEN_YEARS, FIXED_LEG_ID, ExternalId.of("Test", "Test"), SCHEME);
    node.accept(visitor);
  }

  @Test
  public void testCash() {
    final CashNode node = new CashNode(Tenor.ONE_DAY, Tenor.ONE_WEEK, DEPOSIT_1M_ID, SCHEME);
    final Set<Currency> currencies = node.accept(VISITOR);
    assertEquals(1, currencies.size());
    assertEquals(Currency.USD, currencies.iterator().next());
  }

  @Test
  public void testContinuouslyCompoundedRateNode() {
    final ContinuouslyCompoundedRateNode node = new ContinuouslyCompoundedRateNode(SCHEME, Tenor.TWELVE_MONTHS);
    assertNull(node.accept(VISITOR));
  }

  @Test
  public void testCreditSpreadNode() {
    final CreditSpreadNode node = new CreditSpreadNode(SCHEME, Tenor.THREE_MONTHS);
    assertNull(node.accept(VISITOR));
  }

  @Test
  public void testDiscountFactorNode() {
    final DiscountFactorNode node = new DiscountFactorNode(SCHEME, Tenor.FIVE_YEARS);
    assertNull(node.accept(VISITOR));
  }

  @Test
  public void testFRANode() {
    final FRANode node = new FRANode(Tenor.ONE_DAY, Tenor.THREE_MONTHS, LIBOR_3M_ID, SCHEME);
    final Set<Currency> currencies = node.accept(VISITOR);
    assertEquals(1, currencies.size());
    assertEquals(Currency.USD, currencies.iterator().next());
  }

  @Test
  public void testFXForwardNode() {
    final FXForwardNode node = new FXForwardNode(Tenor.ONE_DAY, Tenor.ONE_YEAR, FX_FORWARD_ID, Currency.EUR, Currency.AUD, SCHEME);
    final Set<Currency> currencies = node.accept(VISITOR);
    assertEquals(2, currencies.size());
    assertEquals(Sets.newHashSet(Currency.EUR, Currency.AUD), currencies);
  }

  @Test
  public void testRateFutureNode() {
    final RateFutureNode node = new RateFutureNode(2, Tenor.ONE_DAY, Tenor.THREE_MONTHS, Tenor.THREE_MONTHS, RATE_FUTURE_3M_ID, LIBOR_3M_ID, SCHEME);
    final Set<Currency> currencies = node.accept(VISITOR);
    assertEquals(1, currencies.size());
    assertEquals(Currency.USD, currencies.iterator().next());
  }

  @Test
  public void testSwapNode() {
    SwapNode node = new SwapNode(Tenor.ONE_DAY, Tenor.TEN_YEARS, FIXED_LEG_ID, SWAP_3M_IBOR_ID, SCHEME);
    Set<Currency> currencies = node.accept(VISITOR);
    assertEquals(1, currencies.size());
    assertEquals(Currency.USD, currencies.iterator().next());
    node = new SwapNode(Tenor.ONE_DAY, Tenor.TEN_YEARS, FIXED_LEG_ID, OIS_ID, SCHEME);
    currencies = node.accept(VISITOR);
    assertEquals(1, currencies.size());
    assertEquals(Currency.USD, currencies.iterator().next());
    node = new SwapNode(Tenor.ONE_DAY, Tenor.TEN_YEARS, FIXED_LEG_ID, CMS_SWAP_ID, SCHEME);
    currencies = node.accept(VISITOR);
    assertEquals(1, currencies.size());
    assertEquals(Currency.USD, currencies.iterator().next());
    node = new SwapNode(Tenor.ONE_DAY, Tenor.TEN_YEARS, FIXED_LEG_ID, COMPOUNDING_IBOR_ID, SCHEME);
    currencies = node.accept(VISITOR);
    assertEquals(1, currencies.size());
    assertEquals(Currency.USD, currencies.iterator().next());
    node = new SwapNode(Tenor.ONE_DAY, Tenor.TEN_YEARS, SWAP_3M_IBOR_ID, SWAP_6M_EURIBOR_ID, SCHEME);
    currencies = node.accept(VISITOR);
    assertEquals(2, currencies.size());
    assertEquals(Sets.newHashSet(Currency.EUR, Currency.USD), currencies);
  }

  @Test
  public void testZeroCouponInflationNode() {
    final ZeroCouponInflationNode node = new ZeroCouponInflationNode(Tenor.EIGHT_MONTHS, ZERO_COUPON_INFLATION_ID, FIXED_LEG_ID, InflationNodeType.INTERPOLATED, "TEST");
    final Set<Currency> currencies = node.accept(VISITOR);
    assertEquals(1, currencies.size());
    assertEquals(Currency.USD, currencies.iterator().next());
  }
}
