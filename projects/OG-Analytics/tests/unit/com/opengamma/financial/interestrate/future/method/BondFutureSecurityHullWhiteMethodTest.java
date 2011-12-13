/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.future.method;

import static org.testng.AssertJUnit.assertEquals;

import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.convention.yield.YieldConvention;
import com.opengamma.financial.convention.yield.YieldConventionFactory;
import com.opengamma.financial.instrument.bond.BondFixedSecurityDefinition;
import com.opengamma.financial.interestrate.InterestRateCurveSensitivity;
import com.opengamma.financial.interestrate.TestsDataSets;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.interestrate.bond.definition.BondFixedSecurity;
import com.opengamma.financial.interestrate.future.calculator.PresentValueFromFuturePriceCalculator;
import com.opengamma.financial.interestrate.future.definition.BondFuture;
import com.opengamma.financial.model.interestrate.HullWhiteTestsDataSet;
import com.opengamma.financial.model.interestrate.definition.HullWhiteOneFactorPiecewiseConstantDataBundle;
import com.opengamma.financial.model.interestrate.definition.HullWhiteOneFactorPiecewiseConstantParameters;
import com.opengamma.financial.schedule.ScheduleCalculator;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.time.DateUtils;

import it.unimi.dsi.fastutil.doubles.DoubleAVLTreeSet;

import javax.time.calendar.Period;
import javax.time.calendar.ZonedDateTime;

import org.testng.annotations.Test;

/**
 * Tests related to the bond future figures computed with the Hull-White one factor model for the delivery option.
 */
public class BondFutureSecurityHullWhiteMethodTest {
  // 5-Year U.S. Treasury Note Futures: FVU1
  private static final Currency CUR = Currency.USD;
  private static final Period PAYMENT_TENOR = Period.ofMonths(6);
  private static final Calendar CALENDAR = new MondayToFridayCalendar("A");
  private static final DayCount DAY_COUNT = DayCountFactory.INSTANCE.getDayCount("Actual/Actual ICMA");
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Following");
  private static final boolean IS_EOM = false;
  private static final int SETTLEMENT_DAYS = 1;
  private static final YieldConvention YIELD_CONVENTION = YieldConventionFactory.INSTANCE.getYieldConvention("STREET CONVENTION");
  private static final int NB_BOND = 7;
  private static final Period[] BOND_TENOR = new Period[] {Period.ofYears(5), Period.ofYears(5), Period.ofYears(5), Period.ofYears(8), Period.ofYears(5), Period.ofYears(5), Period.ofYears(5) };
  private static final ZonedDateTime[] START_ACCRUAL_DATE = new ZonedDateTime[] {DateUtils.getUTCDate(2010, 11, 30), DateUtils.getUTCDate(2010, 12, 31), DateUtils.getUTCDate(2011, 1, 31),
      DateUtils.getUTCDate(2008, 2, 29), DateUtils.getUTCDate(2011, 3, 31), DateUtils.getUTCDate(2011, 4, 30), DateUtils.getUTCDate(2011, 5, 31) };
  private static final double[] RATE = new double[] {0.01375, 0.02125, 0.0200, 0.02125, 0.0225, 0.0200, 0.0175 };
  private static final double[] CONVERSION_FACTOR = new double[] {.8317, .8565, .8493, .8516, .8540, .8417, .8292 };
  private static final ZonedDateTime[] MATURITY_DATE = new ZonedDateTime[NB_BOND];
  private static final BondFixedSecurityDefinition[] BASKET_DEFINITION = new BondFixedSecurityDefinition[NB_BOND];
  static {
    for (int loopbasket = 0; loopbasket < NB_BOND; loopbasket++) {
      MATURITY_DATE[loopbasket] = START_ACCRUAL_DATE[loopbasket].plus(BOND_TENOR[loopbasket]);
      BASKET_DEFINITION[loopbasket] = BondFixedSecurityDefinition.from(CUR, MATURITY_DATE[loopbasket], START_ACCRUAL_DATE[loopbasket], PAYMENT_TENOR, RATE[loopbasket], SETTLEMENT_DAYS, CALENDAR,
          DAY_COUNT, BUSINESS_DAY, YIELD_CONVENTION, IS_EOM);
    }
  }
  private static final ZonedDateTime LAST_TRADING_DATE = DateUtils.getUTCDate(2011, 9, 30);
  private static final ZonedDateTime FIRST_NOTICE_DATE = DateUtils.getUTCDate(2011, 8, 31);
  private static final ZonedDateTime LAST_NOTICE_DATE = DateUtils.getUTCDate(2011, 10, 4);
  private static final ZonedDateTime FIRST_DELIVERY_DATE = ScheduleCalculator.getAdjustedDate(FIRST_NOTICE_DATE, SETTLEMENT_DAYS, CALENDAR);
  private static final ZonedDateTime LAST_DELIVERY_DATE = ScheduleCalculator.getAdjustedDate(LAST_NOTICE_DATE, SETTLEMENT_DAYS, CALENDAR);
  private static final double NOTIONAL = 100000;
  private static final double REF_PRICE = 0.0;
  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2011, 6, 20);
  private static final DayCount ACT_ACT = DayCountFactory.INSTANCE.getDayCount("Actual/Actual ISDA");
  private static final double LAST_TRADING_TIME = ACT_ACT.getDayCountFraction(REFERENCE_DATE, LAST_TRADING_DATE);
  private static final double FIRST_NOTICE_TIME = ACT_ACT.getDayCountFraction(REFERENCE_DATE, FIRST_NOTICE_DATE);
  private static final double LAST_NOTICE_TIME = ACT_ACT.getDayCountFraction(REFERENCE_DATE, LAST_NOTICE_DATE);
  private static final double FIRST_DELIVERY_TIME = ACT_ACT.getDayCountFraction(REFERENCE_DATE, FIRST_DELIVERY_DATE);
  private static final double LAST_DELIVERY_TIME = ACT_ACT.getDayCountFraction(REFERENCE_DATE, LAST_DELIVERY_DATE);
  private static final String CREDIT_CURVE_NAME = "Credit";
  private static final String REPO_CURVE_NAME = "Repo";
  private static final String[] CURVES_NAME = {CREDIT_CURVE_NAME, REPO_CURVE_NAME };
  private static final YieldCurveBundle CURVES = TestsDataSets.createCurvesBond2();
  private static final BondFixedSecurity[] BASKET = new BondFixedSecurity[NB_BOND];
  private static final BondFixedSecurity[] STANDARD = new BondFixedSecurity[NB_BOND];
  static {
    for (int loopbasket = 0; loopbasket < NB_BOND; loopbasket++) {
      BASKET[loopbasket] = BASKET_DEFINITION[loopbasket].toDerivative(REFERENCE_DATE, LAST_DELIVERY_DATE, CURVES_NAME);
      STANDARD[loopbasket] = BASKET_DEFINITION[loopbasket].toDerivative(REFERENCE_DATE, CURVES_NAME);
    }
  }

  private static final BondFuture BOND_FUTURE_DERIV = new BondFuture(LAST_TRADING_TIME, FIRST_NOTICE_TIME, LAST_NOTICE_TIME, FIRST_DELIVERY_TIME, LAST_DELIVERY_TIME, NOTIONAL,
      BASKET, CONVERSION_FACTOR, REF_PRICE);
  private static final BondFutureHullWhiteMethod METHOD_HW = BondFutureHullWhiteMethod.getInstance();
  private static final HullWhiteOneFactorPiecewiseConstantParameters PARAMETERS_HW = HullWhiteTestsDataSet.createHullWhiteParameters();
  private static final HullWhiteOneFactorPiecewiseConstantDataBundle BUNDLE_HW = new HullWhiteOneFactorPiecewiseConstantDataBundle(PARAMETERS_HW, CURVES);

  @Test
  public void price() {
    final double priceComputed = METHOD_HW.price(BOND_FUTURE_DERIV, BUNDLE_HW);
    double priceExpected = 1.00; // Rates are at 6%
    assertEquals("Bond future security Discounting Method: price from curves", priceExpected, priceComputed, 5.0E-3);
  }

  @Test(enabled = false)
  /**
   * Tests of performance. "enabled = false" for the standard testing.
   */
  public void performance() {
    long startTime, endTime;
    final int nbTest = 1000;
    double priceFuture = 0.0;
    startTime = System.currentTimeMillis();
    for (int looptest = 0; looptest < nbTest; looptest++) {
      priceFuture = METHOD_HW.price(BOND_FUTURE_DERIV, BUNDLE_HW);
    }
    endTime = System.currentTimeMillis();
    System.out.println(nbTest + " price Bond Future Hull-White (Default number of points): " + (endTime - startTime) + " ms");
    // Performance note: HW price: 25-Aug-11: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 190 ms for 1000 futures.

    int[] nbPoint = new int[] {41, 61, 81, 101, 151, 201, 501 };
    int nbRange = nbPoint.length;
    double[] priceRange = new double[nbRange];
    for (int looprange = 0; looprange < nbRange; looprange++) {
      startTime = System.currentTimeMillis();
      for (int looptest = 0; looptest < nbTest; looptest++) {
        priceRange[looprange] = METHOD_HW.price(BOND_FUTURE_DERIV, BUNDLE_HW, nbPoint[looprange]);
      }
      endTime = System.currentTimeMillis();
      System.out.println(nbTest + " price Bond Future Hull-White: with " + nbPoint[looprange] + " points: " + (endTime - startTime) + " ms - price: " + priceRange[looprange]);
    }

    System.out.println("Bond futures - price - Hull-White one factor - delivery option: " + priceFuture);
  }

  @Test(enabled = true)
  /** Tests the present value method for bond futures transactions. */
  public void presentValue() {
    final CurrencyAmount pvComputed = METHOD_HW.presentValue(BOND_FUTURE_DERIV, BUNDLE_HW);
    final double priceFuture = METHOD_HW.price(BOND_FUTURE_DERIV, BUNDLE_HW);
    final double pvExpected = (priceFuture - REF_PRICE) * NOTIONAL;
    assertEquals("Bond future HW Method: present value currency", CUR, pvComputed.getCurrency());
    assertEquals("Bond future HW Method: present value amount", pvExpected, pvComputed.getAmount(), 1.0E-2);
  }

  @Test(enabled = true)
  /** Tests the present value method for bond futures transactions. */
  public void presentValueFromPrice() {
    final double quotedPrice = 1.05;
    final double presentValueMethod = METHOD_HW.presentValueFromPrice(BOND_FUTURE_DERIV, quotedPrice);
    assertEquals("Bond future transaction Method: present value from price", (quotedPrice - REF_PRICE) * NOTIONAL, presentValueMethod);
    final PresentValueFromFuturePriceCalculator calculator = PresentValueFromFuturePriceCalculator.getInstance();
    final double presentValueCalculator = calculator.visit(BOND_FUTURE_DERIV, quotedPrice);
    assertEquals("Bond future transaction Method: present value from price", presentValueMethod, presentValueCalculator);
  }

  @Test
  /**
   * Tests the curve sensitivity.
   */
  public void presentValueCurveSensitivity() {

    InterestRateCurveSensitivity pvs = METHOD_HW.presentValueCurveSensitivity(BOND_FUTURE_DERIV, BUNDLE_HW);
    pvs = pvs.clean();
    // Bond curve sensitivity
    DoubleAVLTreeSet bondTime = new DoubleAVLTreeSet();
    bondTime.add(BOND_FUTURE_DERIV.getDeliveryLastTime());
    for (int loopbasket = 0; loopbasket < NB_BOND; loopbasket++) {
      for (int loopcpn = 0; loopcpn < BASKET[loopbasket].getCoupon().getNumberOfPayments(); loopcpn++) {
        bondTime.add(BASKET[loopbasket].getCoupon().getNthPayment(loopcpn).getPaymentTime());
      }
    }
    /*
    final double toleranceSensitivity = 1.0E+5; // 1.0E+2: 0.01 USD by bp 
    double[] nodeTimesBond = bondTime.toDoubleArray();
    List<DoublesPair> fdSensiList = curveSensitvityFDCalculator(BOND_FUTURE_DERIV, BUNDLE_HW, BUNDLE_HW, CURVES_NAME[0], nodeTimesBond, 1e-9);
    final List<DoublesPair> sensiPvCredit = pvs.getSensitivities().get(CURVES_NAME[0]);
    assertEquals(fdSensiList.size(), sensiPvCredit.size());
    for (int loopnode = 0; loopnode < fdSensiList.size(); loopnode++) { //checking times
      final DoublesPair pairPv = sensiPvCredit.get(loopnode);
      assertEquals(fdSensiList.get(loopnode).getFirst(), pairPv.getFirst(), 1E-8);
    }
    for (int loopnode = 1; loopnode < fdSensiList.size(); loopnode++) { // Checking sensitivity value
      assertEquals("Bond future curve sensitivity: node " + loopnode, fdSensiList.get(loopnode).second, sensiPvCredit.get(loopnode).second, toleranceSensitivity);
    }
    */
  }

  @Test(enabled = true)
  /**
   * Tests the present value curve sensitivity method for bond futures.
   */
  public void presentValueCurveSensitivityRelative() {
    final InterestRateCurveSensitivity pvcsComputed = METHOD_HW.presentValueCurveSensitivity(BOND_FUTURE_DERIV, BUNDLE_HW);
    final InterestRateCurveSensitivity pcsSecurity = METHOD_HW.priceCurveSensitivity(BOND_FUTURE_DERIV, BUNDLE_HW);
    final InterestRateCurveSensitivity pvcsExpected = pcsSecurity.multiply(NOTIONAL);
    assertEquals("Bond future transaction Discounting Method: present value curve sensitivity", pvcsExpected, pvcsComputed);
  }

}
