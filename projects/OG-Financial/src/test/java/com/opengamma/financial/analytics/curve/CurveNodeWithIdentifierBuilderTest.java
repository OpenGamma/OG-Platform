/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.curve;

import static org.testng.AssertJUnit.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;

import com.opengamma.financial.analytics.ircurve.CurveInstrumentProvider;
import com.opengamma.financial.analytics.ircurve.IndexType;
import com.opengamma.financial.analytics.ircurve.StaticCurveInstrumentProvider;
import com.opengamma.financial.analytics.ircurve.strips.CashNode;
import com.opengamma.financial.analytics.ircurve.strips.ContinuouslyCompoundedRateNode;
import com.opengamma.financial.analytics.ircurve.strips.CreditSpreadNode;
import com.opengamma.financial.analytics.ircurve.strips.CurveNodeWithIdentifier;
import com.opengamma.financial.analytics.ircurve.strips.DataFieldType;
import com.opengamma.financial.analytics.ircurve.strips.DiscountFactorNode;
import com.opengamma.financial.analytics.ircurve.strips.FRANode;
import com.opengamma.financial.analytics.ircurve.strips.FXForwardNode;
import com.opengamma.financial.analytics.ircurve.strips.InflationNodeType;
import com.opengamma.financial.analytics.ircurve.strips.RateFutureNode;
import com.opengamma.financial.analytics.ircurve.strips.RollDateFRANode;
import com.opengamma.financial.analytics.ircurve.strips.RollDateSwapNode;
import com.opengamma.financial.analytics.ircurve.strips.SwapNode;
import com.opengamma.financial.analytics.ircurve.strips.ZeroCouponInflationNode;
import com.opengamma.id.ExternalId;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.Tenor;

/**
 *
 */
@Test(groups = TestGroup.UNIT)
public class CurveNodeWithIdentifierBuilderTest {
  private static final CurveNodeIdMapper MAPPER;
  private static final CurveNodeWithIdentifierBuilder BUILDER;
  private static final CurveNodeWithIdentifierBuilder BUILDER_NO_MAPPER;

  static {
    final Map<Tenor, CurveInstrumentProvider> cashNodeIds = new HashMap<>();
    final Map<Tenor, CurveInstrumentProvider> continuouslyCompoundedRateIds = new HashMap<>();
    final Map<Tenor, CurveInstrumentProvider> creditSpreadNodeIds = new HashMap<>();
    final Map<Tenor, CurveInstrumentProvider> discountFactorIds = new HashMap<>();
    final Map<Tenor, CurveInstrumentProvider> fraNodeIds = new HashMap<>();
    final Map<Tenor, CurveInstrumentProvider> fxForwardNodeIds = new HashMap<>();
    final Map<Tenor, CurveInstrumentProvider> immFRANodeIds = new HashMap<>();
    final Map<Tenor, CurveInstrumentProvider> immSwapNodeIds = new HashMap<>();
    final Map<Tenor, CurveInstrumentProvider> rateFutureNodeIds = new HashMap<>();
    final Map<Tenor, CurveInstrumentProvider> swapNodeIds = new HashMap<>();
    final Map<Tenor, CurveInstrumentProvider> zeroCouponInflationNodeIds = new HashMap<>();
    cashNodeIds.put(Tenor.TWO_MONTHS, new StaticCurveInstrumentProvider(ExternalId.of("Test", "Cash"), "Cash Data", DataFieldType.OUTRIGHT));
    continuouslyCompoundedRateIds.put(Tenor.TWO_MONTHS, new StaticCurveInstrumentProvider(ExternalId.of("Test", "Rate"), "CC Rate Data", DataFieldType.POINTS));
    creditSpreadNodeIds.put(Tenor.TWO_MONTHS, new StaticCurveInstrumentProvider(ExternalId.of("Test", "Credit spread"), "Credit Data", DataFieldType.OUTRIGHT));
    discountFactorIds.put(Tenor.TWO_MONTHS, new StaticCurveInstrumentProvider(ExternalId.of("Test", "DF"), "DF Data", DataFieldType.POINTS));
    fraNodeIds.put(Tenor.TWO_MONTHS, new StaticCurveInstrumentProvider(ExternalId.of("Test", "FRA"), "FRA Data", DataFieldType.OUTRIGHT));
    fxForwardNodeIds.put(Tenor.TWO_MONTHS, new StaticCurveInstrumentProvider(ExternalId.of("Test", "FX Forward"), "FX Forward Data", DataFieldType.POINTS));
    immFRANodeIds.put(Tenor.ONE_YEAR, new TestCurveInstrumentProvider(ExternalId.of("Test", "IMM FRA"), "IMM FRA Data", DataFieldType.OUTRIGHT));
    immSwapNodeIds.put(Tenor.ONE_YEAR, new TestCurveInstrumentProvider(ExternalId.of("Test", "IMM Swap"), "IMM Swap Data", DataFieldType.OUTRIGHT));
    rateFutureNodeIds.put(Tenor.TWO_MONTHS, new TestCurveInstrumentProvider(ExternalId.of("Test", "Future"), "Market_Value", DataFieldType.OUTRIGHT));
    swapNodeIds.put(Tenor.TWO_MONTHS, new StaticCurveInstrumentProvider(ExternalId.of("Test", "Swap"), "Swap Data", DataFieldType.POINTS));
    zeroCouponInflationNodeIds.put(Tenor.TWO_MONTHS, new StaticCurveInstrumentProvider(ExternalId.of("Test", "ZCI"), "ZC Data", DataFieldType.OUTRIGHT));
    MAPPER = CurveNodeIdMapper.builder()
        .cashNodeIds(cashNodeIds)
        .continuouslyCompoundedRateNodeIds(continuouslyCompoundedRateIds)
        .creditSpreadNodeIds(creditSpreadNodeIds)
        .discountFactorNodeIds(discountFactorIds)
        .fraNodeIds(fraNodeIds)
        .fxForwardNodeIds(fxForwardNodeIds)
        .immFRANodeIds(immFRANodeIds)
        .immSwapNodeIds(immSwapNodeIds)
        .rateFutureNodeIds(rateFutureNodeIds)
        .swapNodeIds(swapNodeIds)
        .zeroCouponInflationNodeIds(zeroCouponInflationNodeIds).build();
    BUILDER = new CurveNodeWithIdentifierBuilder(LocalDate.of(2013, 1, 1), MAPPER);
    BUILDER_NO_MAPPER = new CurveNodeWithIdentifierBuilder(LocalDate.of(2013, 1, 1), null);
  }

  @Test
  public void testCash() {
    final CashNode cash = new CashNode(Tenor.ONE_DAY, Tenor.TWO_MONTHS, ExternalId.of("Test", "Test"), "Test");
    assertEquals(new CurveNodeWithIdentifier(cash, ExternalId.of("Test", "Cash"), "Cash Data", DataFieldType.OUTRIGHT), cash.accept(BUILDER));
  }

  @Test
  public void testRate() {
    final ContinuouslyCompoundedRateNode rate = new ContinuouslyCompoundedRateNode("Test", Tenor.TWO_MONTHS);
    assertEquals(new CurveNodeWithIdentifier(rate, ExternalId.of("Test", "Rate"), "CC Rate Data", DataFieldType.POINTS), rate.accept(BUILDER));
  }

  @Test
  public void testCreditSpread() {
    final CreditSpreadNode creditSpread = new CreditSpreadNode("Test", Tenor.TWO_MONTHS);
    assertEquals(new CurveNodeWithIdentifier(creditSpread, ExternalId.of("Test", "Credit spread"), "Credit Data", DataFieldType.OUTRIGHT), creditSpread.accept(BUILDER));
  }

  @Test
  public void testDiscountFactor() {
    final DiscountFactorNode df = new DiscountFactorNode("Test", Tenor.TWO_MONTHS);
    assertEquals(new CurveNodeWithIdentifier(df, ExternalId.of("Test", "DF"), "DF Data", DataFieldType.POINTS), df.accept(BUILDER));
  }

  @Test
  public void testFRA() {
    final FRANode fra = new FRANode(Tenor.ONE_DAY, Tenor.TWO_MONTHS, ExternalId.of("Test", "Test"), "Test");
    assertEquals(new CurveNodeWithIdentifier(fra, ExternalId.of("Test", "FRA"), "FRA Data", DataFieldType.OUTRIGHT), fra.accept(BUILDER));
  }

  @Test
  public void testFXForward() {
    final FXForwardNode fxForward = new FXForwardNode(Tenor.ONE_DAY, Tenor.TWO_MONTHS, ExternalId.of("Test1", "Test1"), Currency.USD, Currency.JPY, "Test");
    assertEquals(new CurveNodeWithIdentifier(fxForward, ExternalId.of("Test", "FX Forward"), "FX Forward Data", DataFieldType.POINTS), fxForward.accept(BUILDER));
  }

  @Test
  public void testIMMFRA() {
    final RollDateFRANode immFRA = new RollDateFRANode(Tenor.ONE_YEAR, Tenor.THREE_MONTHS, 4, 40, ExternalId.of("Test1", "Test1"), "Id mapper");
    assertEquals(new CurveNodeWithIdentifier(immFRA, ExternalId.of("Test", "IMM FRA"), "IMM FRA Data", DataFieldType.OUTRIGHT), immFRA.accept(BUILDER));
  }

  @Test
  public void testIMMSwap() {
    final RollDateSwapNode immSwap = new RollDateSwapNode(Tenor.ONE_YEAR, 4, 40, ExternalId.of("Test1", "Test1"), "Id mapper");
    assertEquals(new CurveNodeWithIdentifier(immSwap, ExternalId.of("Test", "IMM Swap"), "IMM Swap Data", DataFieldType.OUTRIGHT), immSwap.accept(BUILDER));
  }

  @Test
  public void testRateFuture() {
    final RateFutureNode future = new RateFutureNode(1, Tenor.TWO_MONTHS, Tenor.ONE_MONTH, Tenor.ONE_MONTH, ExternalId.of("Test", "Test"),
        "Test");
    assertEquals(new CurveNodeWithIdentifier(future, ExternalId.of("Test", "Future"), "Market_Value", DataFieldType.OUTRIGHT), future.accept(BUILDER));
  }

  @Test
  public void testSwap() {
    final SwapNode swap = new SwapNode(Tenor.ONE_DAY, Tenor.TWO_MONTHS, ExternalId.of("Test", "Test"), ExternalId.of("Test", "Test"), "Test");
    assertEquals(new CurveNodeWithIdentifier(swap, ExternalId.of("Test", "Swap"), "Swap Data", DataFieldType.POINTS), swap.accept(BUILDER));
  }

  @Test
  public void testZeroCouponInflation() {
    final ZeroCouponInflationNode node = new ZeroCouponInflationNode(Tenor.TWO_MONTHS, ExternalId.of("Test", "Test"), ExternalId.of("Test", "Test"), InflationNodeType.MONTHLY, "Test");
    assertEquals(new CurveNodeWithIdentifier(node, ExternalId.of("Test", "ZCI"), "ZC Data", DataFieldType.OUTRIGHT), node.accept(BUILDER));
  }
  
  @Test(expectedExceptions= {IllegalStateException.class})
  public void testCashNoIdMapper() {
    final CashNode cash = new CashNode(Tenor.ONE_DAY, Tenor.TWO_MONTHS, ExternalId.of("Test", "Test"), null);
    cash.accept(BUILDER_NO_MAPPER);
  }

  @Test(expectedExceptions= {IllegalStateException.class})
  public void testRateNoIdMapper() {
    final ContinuouslyCompoundedRateNode rate = new ContinuouslyCompoundedRateNode(null, Tenor.TWO_MONTHS);
    rate.accept(BUILDER_NO_MAPPER);
  }

  @Test(expectedExceptions= {IllegalStateException.class})
  public void testCreditSpreadNoIdMapper() {
    final CreditSpreadNode creditSpread = new CreditSpreadNode(null, Tenor.TWO_MONTHS);
    creditSpread.accept(BUILDER_NO_MAPPER);
  }

  @Test(expectedExceptions= {IllegalStateException.class})
  public void testDiscountFactorNoIdMapper() {
    final DiscountFactorNode df = new DiscountFactorNode(null, Tenor.TWO_MONTHS);
    df.accept(BUILDER_NO_MAPPER);
  }

  @Test(expectedExceptions= {IllegalStateException.class})
  public void testFRANoIdMapper() {
    final FRANode fra = new FRANode(Tenor.ONE_DAY, Tenor.TWO_MONTHS, ExternalId.of("Test", "Test"), null);
    fra.accept(BUILDER_NO_MAPPER);
  }

  @Test(expectedExceptions= {IllegalStateException.class})
  public void testFXForwardNoIdMapper() {
    final FXForwardNode fxForward = new FXForwardNode(Tenor.ONE_DAY, Tenor.TWO_MONTHS, ExternalId.of("Test1", "Test1"), Currency.USD, Currency.JPY, null);
    fxForward.accept(BUILDER_NO_MAPPER);
  }

  @Test(expectedExceptions= {IllegalStateException.class})
  public void testIMMFRANoIdMapper() {
    final RollDateFRANode immFRA = new RollDateFRANode(Tenor.ONE_YEAR, Tenor.THREE_MONTHS, 4, 40, ExternalId.of("Test1", "Test1"), null);
    immFRA.accept(BUILDER_NO_MAPPER);
  }

  @Test(expectedExceptions= {IllegalStateException.class})
  public void testIMMSwapNoIdMapper() {
    final RollDateSwapNode immSwap = new RollDateSwapNode(Tenor.ONE_YEAR, 4, 40, ExternalId.of("Test1", "Test1"), null);
    immSwap.accept(BUILDER_NO_MAPPER);
  }

  @Test(expectedExceptions= {IllegalStateException.class})
  public void testRateFutureNoIdMapper() {
    final RateFutureNode future = new RateFutureNode(1, Tenor.TWO_MONTHS, Tenor.ONE_MONTH, Tenor.ONE_MONTH, ExternalId.of("Test", "Test"),
        null);
    future.accept(BUILDER_NO_MAPPER);
  }

  @Test(expectedExceptions= {IllegalStateException.class})
  public void testSwapNoIdMapper() {
    final SwapNode swap = new SwapNode(Tenor.ONE_DAY, Tenor.TWO_MONTHS, ExternalId.of("Test", "Test"), ExternalId.of("Test", "Test"), null);
    swap.accept(BUILDER_NO_MAPPER);
  }

  @Test(expectedExceptions= {IllegalStateException.class})
  public void testZeroCouponInflationNoIdMapper() {
    final ZeroCouponInflationNode node = new ZeroCouponInflationNode(Tenor.TWO_MONTHS, ExternalId.of("Test", "Test"), ExternalId.of("Test", "Test"), InflationNodeType.MONTHLY, null);
    node.accept(BUILDER_NO_MAPPER);
  }

  private static class TestCurveInstrumentProvider implements CurveInstrumentProvider {
    private final ExternalId _id;
    private final String _dataField;
    private final DataFieldType _fieldType;

    public TestCurveInstrumentProvider(final ExternalId id, final String dataField, final DataFieldType fieldType) {
      _id = id;
      _dataField = dataField;
      _fieldType = fieldType;
    }

    @Override
    public ExternalId getInstrument(final LocalDate curveDate, final Tenor tenor) {
      return null;
    }

    @Override
    public ExternalId getInstrument(final LocalDate curveDate, final Tenor startTenor, final Tenor futureTenor, final int numFutureFromTenor) {
      return _id;
    }

    @Override
    public String getMarketDataField() {
      return _dataField;
    }

    @Override
    public DataFieldType getDataFieldType() {
      return _fieldType;
    }

    @Override
    @Deprecated
    public ExternalId getInstrument(final LocalDate curveDate, final Tenor tenor, final int numQuarterlyFuturesFromTenor) {
      return null;
    }

    @Override
    @Deprecated
    public ExternalId getInstrument(final LocalDate curveDate, final Tenor tenor, final int periodsPerYear, final boolean isPeriodicZeroDeposit) {
      return null;
    }

    @Override
    @Deprecated
    public ExternalId getInstrument(final LocalDate curveDate, final Tenor tenor, final Tenor payTenor, final Tenor receiveTenor, final IndexType payIndexType, final IndexType receiveIndexType) {
      return null;
    }

    @Override
    @Deprecated
    public ExternalId getInstrument(final LocalDate curveDate, final Tenor tenor, final Tenor resetTenor, final IndexType indexType) {
      return null;
    }

    @Override
    public ExternalId getInstrument(final LocalDate curveDate, final Tenor startTenor, final int startIMMPeriods, final int endIMMPeriods) {
      return _id;
    }

  }
}
