/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.swap;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZoneId;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.annuity.AnnuityDefinition;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityDefinitionBuilder;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIbor;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIborMaster;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexIborMaster;
import com.opengamma.analytics.financial.instrument.payment.CouponIborDefinition;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixed;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIbor;
import com.opengamma.analytics.financial.interestrate.swap.derivative.SwapMultileg;
import com.opengamma.financial.convention.StubType;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.timeseries.precise.zdt.ImmutableZonedDateTimeDoubleTimeSeries;
import com.opengamma.timeseries.precise.zdt.ZonedDateTimeDoubleTimeSeries;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Test the swaps with multiple legs constructor and to derivative.
 */
@Test(groups = TestGroup.UNIT)
public class SwapMultilegDefinitionTest {

  private static final Calendar TARGET = new MondayToFridayCalendar("TRAGET");
  private static final IndexIborMaster INDEX_MASTER = IndexIborMaster.getInstance();
  private static final IborIndex EURIBOR3M = INDEX_MASTER.getIndex("EURIBOR3M");
  private static final IborIndex EURIBOR6M = INDEX_MASTER.getIndex("EURIBOR6M");
  private static final GeneratorSwapFixedIborMaster SWAP_MASTER = GeneratorSwapFixedIborMaster.getInstance();
  private static final GeneratorSwapFixedIbor EUR1YEURIBOR6M = SWAP_MASTER.getGenerator("EUR1YEURIBOR6M", TARGET);
  private static final Period ANNUITY_TENOR = Period.ofYears(2);
  private static final Currency EUR = EURIBOR3M.getCurrency();

  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2013, 3, 20);
  private static final ZonedDateTime SETTLEMENT_DATE = DateUtils.getUTCDate(2013, 10, 16);
  private static final double NOTIONAL = 1000000; // 1m
  private static final double SPREAD = 0.0010; // 10 bps
  private static final StubType STUB = StubType.SHORT_START;

  // Swap represeting a EUR basis swap: 1 spread leg and 2 Euribor leg.
  private static final boolean IS_PAYER_SPREAD = true;
  private static final ZonedDateTime MATURITY_DATE = SETTLEMENT_DATE.plus(ANNUITY_TENOR);
  private static final int NB_LEGS = 3;
  @SuppressWarnings("rawtypes")
  private static final AnnuityDefinition[] LEGS_DEFINITION = new AnnuityDefinition[NB_LEGS];
  static {
    LEGS_DEFINITION[0] = AnnuityDefinitionBuilder.couponFixed(EUR, SETTLEMENT_DATE, MATURITY_DATE, EUR1YEURIBOR6M.getFixedLegPeriod(), TARGET,
        EUR1YEURIBOR6M.getFixedLegDayCount(), EUR1YEURIBOR6M.getBusinessDayConvention(), EUR1YEURIBOR6M.isEndOfMonth(), NOTIONAL, SPREAD, IS_PAYER_SPREAD, STUB, 0);
    LEGS_DEFINITION[1] = AnnuityDefinitionBuilder.couponIbor(SETTLEMENT_DATE, MATURITY_DATE, EURIBOR3M.getTenor(), NOTIONAL, EURIBOR3M,
        IS_PAYER_SPREAD, EURIBOR3M.getDayCount(), EURIBOR3M.getBusinessDayConvention(), EURIBOR3M.isEndOfMonth(), TARGET, STUB, 0);
    LEGS_DEFINITION[2] = AnnuityDefinitionBuilder.couponIbor(SETTLEMENT_DATE, MATURITY_DATE, EURIBOR6M.getTenor(), NOTIONAL, EURIBOR6M,
        !IS_PAYER_SPREAD, EURIBOR6M.getDayCount(), EURIBOR6M.getBusinessDayConvention(), EURIBOR6M.isEndOfMonth(), TARGET, STUB, 0);
  }
  @SuppressWarnings("unchecked")
  private static final SwapMultilegDefinition SWAP_MULTI_LEG_DEFINITION = new SwapMultilegDefinition(LEGS_DEFINITION);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullLegs() {
    new SwapMultilegDefinition(null);
  }

  @SuppressWarnings("unchecked")
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullZeroLeg() {
    new SwapMultilegDefinition(new AnnuityDefinition[0]);
  }

  @SuppressWarnings("unchecked")
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullLeg2() {
    @SuppressWarnings("rawtypes")
    final AnnuityDefinition[] legs2 = LEGS_DEFINITION.clone();
    legs2[1] = null;
    new SwapMultilegDefinition(legs2);
  }

  @Test
  public void getter() {
    assertEquals("SwapMultilegDefinition: getter", LEGS_DEFINITION, SWAP_MULTI_LEG_DEFINITION.getLegs());
  }

  /**
   * Tests toDerivative with different reference dates.
   */
  @Test
  public void toDerivative() {
    final ZonedDateTimeDoubleTimeSeries fixingTsF = ImmutableZonedDateTimeDoubleTimeSeries.ofEmpty(ZoneId.of("UTC"));
    final ZonedDateTimeDoubleTimeSeries fixingTs3 = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(new ZonedDateTime[] {((CouponIborDefinition) LEGS_DEFINITION[1].getNthPayment(0)).getFixingDate() },
        new double[] {0.0123 });
    final ZonedDateTimeDoubleTimeSeries fixingTs6 = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(new ZonedDateTime[] {((CouponIborDefinition) LEGS_DEFINITION[2].getNthPayment(0)).getFixingDate() },
        new double[] {0.0135 });
    final ZonedDateTimeDoubleTimeSeries[] fixingTs = new ZonedDateTimeDoubleTimeSeries[] {fixingTsF, fixingTs3, fixingTs6 };
    final ZonedDateTime referenceDateBeforeFirstFixing = REFERENCE_DATE;
    final SwapMultileg swapConvertedBeforeFirstFixing = SWAP_MULTI_LEG_DEFINITION.toDerivative(referenceDateBeforeFirstFixing);
    for (int loopcpn = 0; loopcpn < swapConvertedBeforeFirstFixing.getLegs()[0].getNumberOfPayments(); loopcpn++) {
      assertTrue("SwapMultilegDefinition: toDerivative", swapConvertedBeforeFirstFixing.getLegs()[0].getNthPayment(loopcpn) instanceof CouponFixed);
    }
    for (int loopcpn = 0; loopcpn < swapConvertedBeforeFirstFixing.getLegs()[1].getNumberOfPayments(); loopcpn++) {
      assertTrue("SwapMultilegDefinition: toDerivative", swapConvertedBeforeFirstFixing.getLegs()[1].getNthPayment(loopcpn) instanceof CouponIbor);
    }
    for (int loopcpn = 0; loopcpn < swapConvertedBeforeFirstFixing.getLegs()[2].getNumberOfPayments(); loopcpn++) {
      assertTrue("SwapMultilegDefinition: toDerivative", swapConvertedBeforeFirstFixing.getLegs()[2].getNthPayment(loopcpn) instanceof CouponIbor);
    }
    for (int loopleg = 0; loopleg < NB_LEGS; loopleg++) {
      assertEquals("SwapMultilegDefinition: toDerivative", swapConvertedBeforeFirstFixing.getLegs()[loopleg], LEGS_DEFINITION[loopleg].toDerivative(referenceDateBeforeFirstFixing));
    }
    final ZonedDateTime referenceDateOnFirstFixingDate = ((CouponIborDefinition) LEGS_DEFINITION[1].getNthPayment(0)).getFixingDate();
    final SwapMultileg swapConvertedOnFirstFixingDateNoFixing = SWAP_MULTI_LEG_DEFINITION.toDerivative(referenceDateOnFirstFixingDate);
    for (int loopcpn = 0; loopcpn < swapConvertedBeforeFirstFixing.getLegs()[0].getNumberOfPayments(); loopcpn++) {
      assertTrue("SwapMultilegDefinition: toDerivative", swapConvertedOnFirstFixingDateNoFixing.getLegs()[0].getNthPayment(loopcpn) instanceof CouponFixed);
    }
    for (int loopcpn = 0; loopcpn < swapConvertedBeforeFirstFixing.getLegs()[1].getNumberOfPayments(); loopcpn++) {
      assertTrue("SwapMultilegDefinition: toDerivative", swapConvertedOnFirstFixingDateNoFixing.getLegs()[1].getNthPayment(loopcpn) instanceof CouponIbor);
    }
    for (int loopcpn = 0; loopcpn < swapConvertedBeforeFirstFixing.getLegs()[2].getNumberOfPayments(); loopcpn++) {
      assertTrue("SwapMultilegDefinition: toDerivative", swapConvertedOnFirstFixingDateNoFixing.getLegs()[2].getNthPayment(loopcpn) instanceof CouponIbor);
    }
    final SwapMultileg swapConvertedOnFirstFixingDateFixing = SWAP_MULTI_LEG_DEFINITION.toDerivative(referenceDateOnFirstFixingDate, fixingTs);
    for (int loopcpn = 0; loopcpn < swapConvertedBeforeFirstFixing.getLegs()[0].getNumberOfPayments(); loopcpn++) {
      assertTrue("SwapMultilegDefinition: toDerivative", swapConvertedOnFirstFixingDateFixing.getLegs()[0].getNthPayment(loopcpn) instanceof CouponFixed);
    }
    assertTrue("SwapMultilegDefinition: toDerivative", swapConvertedOnFirstFixingDateFixing.getLegs()[1].getNthPayment(0) instanceof CouponFixed);
    for (int loopcpn = 1; loopcpn < swapConvertedBeforeFirstFixing.getLegs()[1].getNumberOfPayments(); loopcpn++) {
      assertTrue("SwapMultilegDefinition: toDerivative", swapConvertedOnFirstFixingDateFixing.getLegs()[1].getNthPayment(loopcpn) instanceof CouponIbor);
    }
    assertTrue("SwapMultilegDefinition: toDerivative", swapConvertedOnFirstFixingDateFixing.getLegs()[2].getNthPayment(0) instanceof CouponFixed);
    for (int loopcpn = 1; loopcpn < swapConvertedBeforeFirstFixing.getLegs()[2].getNumberOfPayments(); loopcpn++) {
      assertTrue("SwapMultilegDefinition: toDerivative", swapConvertedOnFirstFixingDateFixing.getLegs()[2].getNthPayment(loopcpn) instanceof CouponIbor);
    }

  }

}
