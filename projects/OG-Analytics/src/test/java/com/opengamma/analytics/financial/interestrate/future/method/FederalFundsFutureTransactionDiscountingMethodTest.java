/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.future.method;

import static org.testng.AssertJUnit.assertEquals;

import java.util.List;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.future.FederalFundsFutureSecurityDefinition;
import com.opengamma.analytics.financial.instrument.future.FederalFundsFutureTransactionDefinition;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.instrument.index.IndexONMaster;
import com.opengamma.analytics.financial.interestrate.InterestRateCurveSensitivity;
import com.opengamma.analytics.financial.interestrate.TestsDataSetsSABR;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.interestrate.future.derivative.FederalFundsFutureSecurity;
import com.opengamma.analytics.financial.interestrate.future.derivative.FederalFundsFutureTransaction;
import com.opengamma.analytics.financial.interestrate.method.SensitivityFiniteDifference;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.timeseries.precise.zdt.ImmutableZonedDateTimeDoubleTimeSeries;
import com.opengamma.timeseries.precise.zdt.ZonedDateTimeDoubleTimeSeries;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.tuple.DoublesPair;

/**
 * @deprecated This class tests deprecated functionality.
 */
@Deprecated
@Test(groups = TestGroup.UNIT)
public class FederalFundsFutureTransactionDiscountingMethodTest {

  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2012, 1, 30);

  private static final Calendar NYC = new MondayToFridayCalendar("NYC");
  private static final IndexON INDEX_FEDFUND = IndexONMaster.getInstance().getIndex("FED FUND");
  private static final ZonedDateTime MARCH_1 = DateUtils.getUTCDate(2012, 3, 1);
  private static final double TRADE_PRICE = 0.99900;
  private static final int QUANTITY = 123;

  private static final FederalFundsFutureSecurityDefinition FUTURE_SECURITY_DEFINITION = FederalFundsFutureSecurityDefinition.fromFedFund(MARCH_1, INDEX_FEDFUND, NYC);
  private static final FederalFundsFutureTransactionDefinition FUTURE_TRANSACTION_DEFINITION = new FederalFundsFutureTransactionDefinition(FUTURE_SECURITY_DEFINITION, QUANTITY, REFERENCE_DATE,
      TRADE_PRICE);

  private static final YieldCurveBundle CURVES = TestsDataSetsSABR.createCurves2();
  private static final String[] CURVE_NAMES = TestsDataSetsSABR.curves2Names();

  private static final ZonedDateTime[] CLOSING_DATE = new ZonedDateTime[] {REFERENCE_DATE.minusDays(2), REFERENCE_DATE.minusDays(1), REFERENCE_DATE};
  private static final double[] CLOSING_PRICE = new double[] {0.99895, 0.99905, 0.99915};
  private static final ZonedDateTimeDoubleTimeSeries CLOSING_TS = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(CLOSING_DATE, CLOSING_PRICE);
  private static final ZonedDateTime[] FIXING_DATE = new ZonedDateTime[] {REFERENCE_DATE.minusDays(2), REFERENCE_DATE.minusDays(1), REFERENCE_DATE};
  private static final double[] FIXING_RATE = new double[] {0.0010, 0.0011, 0.0009};
  private static final ZonedDateTimeDoubleTimeSeries FIXING_TS = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(FIXING_DATE, FIXING_RATE);
  private static final ZonedDateTimeDoubleTimeSeries[] DATA = new ZonedDateTimeDoubleTimeSeries[] {FIXING_TS, CLOSING_TS};

  private static final FederalFundsFutureSecurity FUTURE_SECURITY = FUTURE_SECURITY_DEFINITION.toDerivative(REFERENCE_DATE, CURVE_NAMES);
  private static final FederalFundsFutureTransaction FUTURE_TRANSACTION = FUTURE_TRANSACTION_DEFINITION.toDerivative(REFERENCE_DATE, DATA, CURVE_NAMES);

  private static final FederalFundsFutureSecurityDiscountingMethod METHOD_SECURITY = FederalFundsFutureSecurityDiscountingMethod.getInstance();
  private static final FederalFundsFutureTransactionDiscountingMethod METHOD_TRANSACTION = FederalFundsFutureTransactionDiscountingMethod.getInstance();

  private static final double TOLERANCE_PV = 1.0E-2;

  @Test
  public void presentValueFromPrice() {
    final double price = 0.99895;
    final CurrencyAmount pv = METHOD_TRANSACTION.presentValueFromPrice(FUTURE_TRANSACTION, price);
    final double pvExpected = (price - FUTURE_TRANSACTION.getReferencePrice()) * FUTURE_SECURITY_DEFINITION.getNotional() * FUTURE_SECURITY_DEFINITION.getMarginAccrualFactor() * QUANTITY;
    assertEquals("Federal Funds Future transaction: present value", pvExpected, pv.getAmount(), TOLERANCE_PV);
  }

  @Test
  public void presentValue() {
    final CurrencyAmount pv = METHOD_TRANSACTION.presentValue(FUTURE_TRANSACTION, CURVES);
    final double price = METHOD_SECURITY.price(FUTURE_SECURITY, CURVES);
    final CurrencyAmount pvExpected = METHOD_TRANSACTION.presentValueFromPrice(FUTURE_TRANSACTION, price);
    assertEquals("Federal Funds Future transaction: present value", pvExpected.getAmount(), pv.getAmount(), TOLERANCE_PV);
  }

  @Test
  public void presentValueCurveSensitivity() {
    final InterestRateCurveSensitivity pvcsComputed = METHOD_TRANSACTION.presentValueCurveSensitivity(FUTURE_TRANSACTION, CURVES);
    assertEquals("Federal Funds Future transaction: present value curve sensitivity", 1, pvcsComputed.getSensitivities().size());
    assertEquals("Federal Funds Future transaction: present value curve sensitivity", FUTURE_TRANSACTION.getUnderlyingSecurity().getFixingPeriodTime().length,
        pvcsComputed.getSensitivities().get(CURVE_NAMES[0]).size());
    final double deltaTolerancePrice = 1.0E+0;
    //Testing note: Sensitivity is for a movement of 1. 1E+2 = 1 cent for a 1 bp move. Tolerance increased to cope with numerical imprecision of finite difference.
    final double deltaShift = 1.0E-6;
    // Discounting curve sensitivity
    final String bumpedCurveName = "Bumped Curve";
    final FederalFundsFutureTransaction futureTransactionBumped = FUTURE_TRANSACTION_DEFINITION.toDerivative(REFERENCE_DATE, DATA, bumpedCurveName);
    final double[] nodeTimesDisc = futureTransactionBumped.getUnderlyingSecurity().getFixingPeriodTime();
    final double[] sensiDiscMethod = SensitivityFiniteDifference.curveSensitivity(futureTransactionBumped, CURVES, CURVE_NAMES[0], bumpedCurveName, nodeTimesDisc, deltaShift, METHOD_TRANSACTION);
    final List<DoublesPair> sensiPvDisc = pvcsComputed.getSensitivities().get(CURVE_NAMES[0]);
    for (int loopnode = 0; loopnode < sensiDiscMethod.length; loopnode++) {
      final DoublesPair pairPv = sensiPvDisc.get(loopnode);
      assertEquals("Sensitivity coupon pv to forward curve: Node " + loopnode, nodeTimesDisc[loopnode], pairPv.getFirst(), 1E-8);
      AssertJUnit.assertEquals("Sensitivity finite difference method: node sensitivity " + loopnode, pairPv.second, sensiDiscMethod[loopnode], deltaTolerancePrice);
    }
  }

}
