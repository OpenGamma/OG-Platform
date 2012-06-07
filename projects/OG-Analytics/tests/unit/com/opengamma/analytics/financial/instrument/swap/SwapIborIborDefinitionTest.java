/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.swap;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import javax.time.calendar.Period;
import javax.time.calendar.ZonedDateTime;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.instrument.annuity.AnnuityCouponIborSpreadDefinition;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.iborindex.IndexIborTestsMaster;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Coupon;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixed;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.analytics.financial.interestrate.swap.derivative.Swap;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.timeseries.zoneddatetime.ArrayZonedDateTimeDoubleTimeSeries;

/**
 * Test the swap Ibor+spread to Ibor+spread constructor and to derivative.
 */
public class SwapIborIborDefinitionTest {

  private static final Calendar CALENDAR = new MondayToFridayCalendar("A");
  private static final IndexIborTestsMaster INDEX_MASTER = IndexIborTestsMaster.getInstance();
  private static final IborIndex USDLIBOR3M = INDEX_MASTER.getIndex("USDLIBOR3M", CALENDAR);
  private static final IborIndex USDLIBOR6M = INDEX_MASTER.getIndex("USDLIBOR6M", CALENDAR);
  private static final Period ANNUITY_TENOR = Period.ofYears(2);

  private static final ZonedDateTime SETTLEMENT_DATE = DateUtils.getUTCDate(2012, 4, 18);
  private static final double NOTIONAL = 1000000;

  private static final boolean IS_PAYER_1 = true;
  private static final double SPREAD_1 = 0.0012;
  private static final AnnuityCouponIborSpreadDefinition IBOR_LEG_1 = AnnuityCouponIborSpreadDefinition.from(SETTLEMENT_DATE, ANNUITY_TENOR, NOTIONAL, USDLIBOR3M, SPREAD_1, IS_PAYER_1);
  private static final boolean IS_PAYER_2 = false;
  private static final double SPREAD_2 = 0.0;
  private static final AnnuityCouponIborSpreadDefinition IBOR_LEG_2 = AnnuityCouponIborSpreadDefinition.from(SETTLEMENT_DATE, ANNUITY_TENOR, NOTIONAL, USDLIBOR6M, SPREAD_2, IS_PAYER_2);
  private static final SwapIborIborDefinition SWAP_IBOR_IBOR = new SwapIborIborDefinition(IBOR_LEG_1, IBOR_LEG_2);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullLeg1() {
    new SwapIborIborDefinition(null, IBOR_LEG_2);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullLeg2() {
    new SwapIborIborDefinition(IBOR_LEG_1, null);
  }

  @Test
  public void testGetter() {
    assertEquals(SWAP_IBOR_IBOR.getFirstLeg(), IBOR_LEG_1);
    assertEquals(SWAP_IBOR_IBOR.getSecondLeg(), IBOR_LEG_2);
  }

  /**
   * Tests toDerivative with different reference dates.
   */
  @Test
  public void toDerivative() {
    final String[] yieldCurveNames = new String[] {"dsc", "fwd", "fwd6m" };
    final ArrayZonedDateTimeDoubleTimeSeries fixingTs3 = new ArrayZonedDateTimeDoubleTimeSeries(new ZonedDateTime[] {SWAP_IBOR_IBOR.getFirstLeg().getNthPayment(0).getFixingDate() },
        new double[] {0.0123 });
    final ArrayZonedDateTimeDoubleTimeSeries fixingTs6 = new ArrayZonedDateTimeDoubleTimeSeries(new ZonedDateTime[] {SWAP_IBOR_IBOR.getFirstLeg().getNthPayment(0).getFixingDate() },
        new double[] {0.0135 });
    final ArrayZonedDateTimeDoubleTimeSeries[] fixingTs = new ArrayZonedDateTimeDoubleTimeSeries[] {fixingTs3, fixingTs6 };
    final ZonedDateTime referenceDateBeforeFirstFixing = DateUtils.getUTCDate(2012, 4, 13);
    final Swap<? extends Payment, ? extends Payment> swapConvertedBeforeFirstFixing = SWAP_IBOR_IBOR.toDerivative(referenceDateBeforeFirstFixing, yieldCurveNames);
    for (int loopcpn = 0; loopcpn < swapConvertedBeforeFirstFixing.getFirstLeg().getNumberOfPayments(); loopcpn++) {
      assertTrue("SwapIborIborDefinition: toDerivative", swapConvertedBeforeFirstFixing.getFirstLeg().getNthPayment(loopcpn) instanceof Coupon);
    }
    for (int loopcpn = 0; loopcpn < swapConvertedBeforeFirstFixing.getSecondLeg().getNumberOfPayments(); loopcpn++) {
      assertTrue("SwapIborIborDefinition: toDerivative", swapConvertedBeforeFirstFixing.getSecondLeg().getNthPayment(loopcpn) instanceof Coupon);
    }
    final ZonedDateTime referenceDateOnFirstFixing = DateUtils.getUTCDate(2012, 4, 16);
    final Swap<? extends Payment, ? extends Payment> swapConvertedOnFirstFixing = SWAP_IBOR_IBOR.toDerivative(referenceDateOnFirstFixing, fixingTs, yieldCurveNames);
    for (int loopcpn = 0; loopcpn < swapConvertedOnFirstFixing.getFirstLeg().getNumberOfPayments(); loopcpn++) {
      assertTrue("SwapIborIborDefinition: toDerivative", swapConvertedOnFirstFixing.getFirstLeg().getNthPayment(loopcpn) instanceof Coupon);
    }
    for (int loopcpn = 0; loopcpn < swapConvertedOnFirstFixing.getSecondLeg().getNumberOfPayments(); loopcpn++) {
      assertTrue("SwapIborIborDefinition: toDerivative", swapConvertedOnFirstFixing.getSecondLeg().getNthPayment(loopcpn) instanceof Coupon);
    }
    assertTrue("SwapIborIborDefinition: toDerivative", swapConvertedOnFirstFixing.getFirstLeg().getNthPayment(0) instanceof CouponFixed);
    assertTrue("SwapIborIborDefinition: toDerivative", swapConvertedOnFirstFixing.getSecondLeg().getNthPayment(0) instanceof CouponFixed);
  }

}
