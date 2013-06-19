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
import com.opengamma.financial.analytics.ircurve.strips.DiscountFactorNode;
import com.opengamma.financial.analytics.ircurve.strips.FRANode;
import com.opengamma.financial.analytics.ircurve.strips.RateFutureNode;
import com.opengamma.financial.analytics.ircurve.strips.SwapNode;
import com.opengamma.id.ExternalId;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.Tenor;

/**
 * 
 */
@Test(groups = TestGroup.UNIT)
public class CurveNodeWithIdentifierBuilderTest {
  private static final CurveNodeIdMapper MAPPER;
  private static final CurveNodeWithIdentifierBuilder BUILDER;

  static {
    final Map<Tenor, CurveInstrumentProvider> cashNodeIds = new HashMap<>();
    final Map<Tenor, CurveInstrumentProvider> continuouslyCompoundedRateIds = new HashMap<>();
    final Map<Tenor, CurveInstrumentProvider> creditSpreadNodeIds = new HashMap<>();
    final Map<Tenor, CurveInstrumentProvider> discountFactorIds = new HashMap<>();
    final Map<Tenor, CurveInstrumentProvider> fraNodeIds = new HashMap<>();
    final Map<Tenor, CurveInstrumentProvider> rateFutureNodeIds = new HashMap<>();
    final Map<Tenor, CurveInstrumentProvider> swapNodeIds = new HashMap<>();
    cashNodeIds.put(Tenor.TWO_MONTHS, new StaticCurveInstrumentProvider(ExternalId.of("Test", "Cash")));
    continuouslyCompoundedRateIds.put(Tenor.TWO_MONTHS, new StaticCurveInstrumentProvider(ExternalId.of("Test", "Rate")));
    creditSpreadNodeIds.put(Tenor.TWO_MONTHS, new StaticCurveInstrumentProvider(ExternalId.of("Test", "Credit spread")));
    discountFactorIds.put(Tenor.TWO_MONTHS, new StaticCurveInstrumentProvider(ExternalId.of("Test", "DF")));
    fraNodeIds.put(Tenor.TWO_MONTHS, new StaticCurveInstrumentProvider(ExternalId.of("Test", "FRA")));
    rateFutureNodeIds.put(Tenor.TWO_MONTHS, new TestCurveInstrumentProvider(ExternalId.of("Test", "Future")));
    swapNodeIds.put(Tenor.TWO_MONTHS, new StaticCurveInstrumentProvider(ExternalId.of("Test", "Swap")));
    MAPPER = new CurveNodeIdMapper(cashNodeIds, continuouslyCompoundedRateIds, creditSpreadNodeIds,
        discountFactorIds, fraNodeIds, rateFutureNodeIds, swapNodeIds);
    BUILDER = new CurveNodeWithIdentifierBuilder(LocalDate.of(2013, 1, 1), MAPPER);
  }

  @Test
  public void testCash() {
    final CashNode cash = new CashNode(Tenor.ONE_DAY, Tenor.TWO_MONTHS, ExternalId.of("Test", "Test"), "Test");
    assertEquals(new CurveNodeWithIdentifier(cash, ExternalId.of("Test", "Cash")), cash.accept(BUILDER));
  }

  @Test
  public void testRate() {
    final ContinuouslyCompoundedRateNode rate = new ContinuouslyCompoundedRateNode("Test", Tenor.TWO_MONTHS);
    assertEquals(new CurveNodeWithIdentifier(rate, ExternalId.of("Test", "Rate")), rate.accept(BUILDER));
  }

  @Test
  public void testCreditSpread() {
    final CreditSpreadNode creditSpread = new CreditSpreadNode("Test", Tenor.TWO_MONTHS);
    assertEquals(new CurveNodeWithIdentifier(creditSpread, ExternalId.of("Test", "Credit spread")), creditSpread.accept(BUILDER));
  }

  @Test
  public void testDiscountFactor() {
    final DiscountFactorNode df = new DiscountFactorNode("Test", Tenor.TWO_MONTHS);
    assertEquals(new CurveNodeWithIdentifier(df, ExternalId.of("Test", "DF")), df.accept(BUILDER));
  }

  @Test
  public void testFRA() {
    final FRANode fra = new FRANode(Tenor.ONE_DAY, Tenor.TWO_MONTHS, ExternalId.of("Test", "Test"), "Test");
    assertEquals(new CurveNodeWithIdentifier(fra, ExternalId.of("Test", "FRA")), fra.accept(BUILDER));
  }

  @Test
  public void testFuture() {
    final RateFutureNode future = new RateFutureNode(1, Tenor.TWO_MONTHS, Tenor.ONE_MONTH, Tenor.ONE_MONTH, ExternalId.of("Test", "Test"),
        ExternalId.of("Test", "Test"), "Test");
    assertEquals(new CurveNodeWithIdentifier(future, ExternalId.of("Test", "Future")), future.accept(BUILDER));
  }

  @Test
  public void testSwap() {
    final SwapNode swap = new SwapNode(Tenor.ONE_DAY, Tenor.TWO_MONTHS, ExternalId.of("Test", "Test"), ExternalId.of("Test", "Test"), "Test");
    assertEquals(new CurveNodeWithIdentifier(swap, ExternalId.of("Test", "Swap")), swap.accept(BUILDER));
  }

  private static class TestCurveInstrumentProvider implements CurveInstrumentProvider {
    private final ExternalId _id;

    public TestCurveInstrumentProvider(final ExternalId id) {
      _id = id;
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

  }
}
