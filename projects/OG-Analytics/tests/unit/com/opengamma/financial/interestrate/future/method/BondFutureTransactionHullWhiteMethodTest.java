/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.future.method;

import static com.opengamma.financial.interestrate.FDCurveSensitivityCalculator.curveSensitvityFDCalculator;
import static org.testng.AssertJUnit.assertEquals;
import it.unimi.dsi.fastutil.doubles.DoubleAVLTreeSet;

import java.util.List;

import javax.time.calendar.Period;
import javax.time.calendar.ZonedDateTime;

import org.testng.annotations.Test;

import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.convention.yield.YieldConvention;
import com.opengamma.financial.convention.yield.YieldConventionFactory;
import com.opengamma.financial.instrument.bond.BondFixedSecurityDefinition;
import com.opengamma.financial.instrument.future.BondFutureSecurityDefinition;
import com.opengamma.financial.instrument.future.BondFutureTransactionDefinition;
import com.opengamma.financial.interestrate.InterestRateCurveSensitivity;
import com.opengamma.financial.interestrate.InterestRateDerivative;
import com.opengamma.financial.interestrate.TestsDataSets;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.interestrate.bond.definition.BondFixedSecurity;
import com.opengamma.financial.interestrate.future.calculator.PresentValueFromFuturePriceCalculator;
import com.opengamma.financial.interestrate.future.definition.BondFutureSecurity;
import com.opengamma.financial.interestrate.future.definition.BondFutureTransaction;
import com.opengamma.financial.model.interestrate.HullWhiteTestsDataSet;
import com.opengamma.financial.model.interestrate.definition.HullWhiteOneFactorPiecewiseConstantDataBundle;
import com.opengamma.financial.model.interestrate.definition.HullWhiteOneFactorPiecewiseConstantParameters;
import com.opengamma.financial.schedule.ScheduleCalculator;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Tests related to the bond future transaction figures computed with Hull-White one factor model.
 */
public class BondFutureTransactionHullWhiteMethodTest {
  // 5-Year U.S. Treasury Note Futures: FVU1
  private static final Currency CUR = Currency.USD;
  private static final Period PAYMENT_TENOR = Period.ofMonths(6);
  private static final Calendar CALENDAR = new MondayToFridayCalendar("A");
  private static final DayCount DAY_COUNT = DayCountFactory.INSTANCE.getDayCount("Actual/Actual ICMA");
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Following");
  private static final boolean IS_EOM = false;
  private static final int SETTLEMENT_DAYS = 1;
  private static final YieldConvention YIELD_CONVENTION = YieldConventionFactory.INSTANCE.getYieldConvention("STREET CONVENTION");
  private static final Period[] BOND_TENOR = new Period[] {Period.ofYears(5), Period.ofYears(5), Period.ofYears(5), Period.ofYears(8), Period.ofYears(5), Period.ofYears(5), Period.ofYears(5)};
  private static final int NB_BOND = BOND_TENOR.length;
  private static final ZonedDateTime[] START_ACCRUAL_DATE = new ZonedDateTime[] {DateUtils.getUTCDate(2010, 11, 30), DateUtils.getUTCDate(2010, 12, 31), DateUtils.getUTCDate(2011, 1, 31),
      DateUtils.getUTCDate(2008, 2, 29), DateUtils.getUTCDate(2011, 3, 31), DateUtils.getUTCDate(2011, 4, 30), DateUtils.getUTCDate(2011, 5, 31)};
  private static final double[] RATE = new double[] {0.01375, 0.02125, 0.0200, 0.02125, 0.0225, 0.0200, 0.0175};
  private static final double[] CONVERSION_FACTOR = new double[] {.8317, .8565, .8493, .8516, .8540, .8417, .8292};

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
  private static final ZonedDateTime FIRST_DELIVERY_DATE = ScheduleCalculator.getAdjustedDate(FIRST_NOTICE_DATE, CALENDAR, SETTLEMENT_DAYS);
  private static final ZonedDateTime LAST_DELIVERY_DATE = ScheduleCalculator.getAdjustedDate(LAST_NOTICE_DATE, CALENDAR, SETTLEMENT_DAYS);
  private static final double NOTIONAL = 100000;
  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2011, 6, 20);
  private static final DayCount ACT_ACT = DayCountFactory.INSTANCE.getDayCount("Actual/Actual ISDA");
  private static final double LAST_TRADING_TIME = ACT_ACT.getDayCountFraction(REFERENCE_DATE, LAST_TRADING_DATE);
  private static final double FIRST_NOTICE_TIME = ACT_ACT.getDayCountFraction(REFERENCE_DATE, FIRST_NOTICE_DATE);
  private static final double LAST_NOTICE_TIME = ACT_ACT.getDayCountFraction(REFERENCE_DATE, LAST_NOTICE_DATE);
  private static final double FIRST_DELIVERY_TIME = ACT_ACT.getDayCountFraction(REFERENCE_DATE, FIRST_DELIVERY_DATE);
  private static final double LAST_DELIVERY_TIME = ACT_ACT.getDayCountFraction(REFERENCE_DATE, LAST_DELIVERY_DATE);
  private static final String CREDIT_CURVE_NAME = "Credit";
  private static final String REPO_CURVE_NAME = "Repo";
  private static final String[] CURVES_NAME = {CREDIT_CURVE_NAME, REPO_CURVE_NAME};
  private static final YieldCurveBundle CURVES = TestsDataSets.createCurvesBond1();
  private static final HullWhiteOneFactorPiecewiseConstantParameters PARAMETERS_HW = HullWhiteTestsDataSet.createHullWhiteParameters();
  private static final HullWhiteOneFactorPiecewiseConstantDataBundle BUNDLE_HW = new HullWhiteOneFactorPiecewiseConstantDataBundle(PARAMETERS_HW, CURVES);
  private static final BondFixedSecurity[] BASKET = new BondFixedSecurity[NB_BOND];
  private static final BondFixedSecurity[] STANDARD = new BondFixedSecurity[NB_BOND];
  static {
    for (int loopbasket = 0; loopbasket < NB_BOND; loopbasket++) {
      BASKET[loopbasket] = BASKET_DEFINITION[loopbasket].toDerivative(REFERENCE_DATE, LAST_DELIVERY_DATE, CURVES_NAME);
      STANDARD[loopbasket] = BASKET_DEFINITION[loopbasket].toDerivative(REFERENCE_DATE, CURVES_NAME);
    }
  }
  private static final BondFutureSecurity BOND_FUTURE_SECURITY = new BondFutureSecurity(LAST_TRADING_TIME, FIRST_NOTICE_TIME, LAST_NOTICE_TIME, FIRST_DELIVERY_TIME, LAST_DELIVERY_TIME, NOTIONAL,
      BASKET, CONVERSION_FACTOR);
  private static final BondFutureTransactionHullWhiteMethod METHOD_FUTURE_TRANSACTION = BondFutureTransactionHullWhiteMethod.getInstance();
  private static final BondFutureSecurityHullWhiteMethod METHOD_FUTURE_SECURITY = BondFutureSecurityHullWhiteMethod.getInstance();
  // Transaction
  private static final int QUANTITY = 4321;
  private static final double REFERENCE_PRICE = 1.0987;
  private static final BondFutureTransaction FUTURE_TRANSACTION = new BondFutureTransaction(BOND_FUTURE_SECURITY, QUANTITY, REFERENCE_PRICE);

  @Test(enabled = true)
  /**
   * Tests the present value method for bond futures transactions.
   */
  public void presentValue() {
    final CurrencyAmount pvComputed = METHOD_FUTURE_TRANSACTION.presentValue(FUTURE_TRANSACTION, BUNDLE_HW);
    final double priceFuture = METHOD_FUTURE_SECURITY.price(BOND_FUTURE_SECURITY, BUNDLE_HW);
    final double pvExpected = (priceFuture - REFERENCE_PRICE) * NOTIONAL * QUANTITY;
    assertEquals("Bond future transaction Discounting Method: present value currency", CUR, pvComputed.getCurrency());
    assertEquals("Bond future transaction Discounting Method: present value amount", pvExpected, pvComputed.getAmount(), 1.0E-2);
    final InterestRateDerivative derivative = FUTURE_TRANSACTION;
    final CurrencyAmount pvComputed2 = METHOD_FUTURE_TRANSACTION.presentValue(derivative, BUNDLE_HW);
    assertEquals("Bond future transaction Discounting Method: present value", pvComputed, pvComputed2);
  }

  @Test(enabled = true)
  /**
   * Tests the present value method for bond futures transactions.
   */
  public void presentValueFromPrice() {
    final double quotedPrice = 1.05;
    final double presentValueMethod = METHOD_FUTURE_TRANSACTION.presentValueFromPrice(FUTURE_TRANSACTION, quotedPrice);
    assertEquals("Bond future transaction Method: present value from price", (quotedPrice - REFERENCE_PRICE) * QUANTITY * NOTIONAL, presentValueMethod);
    final PresentValueFromFuturePriceCalculator calculator = PresentValueFromFuturePriceCalculator.getInstance();
    final double presentValueCalculator = calculator.visit(FUTURE_TRANSACTION, quotedPrice);
    assertEquals("Bond future transaction Method: present value from price", presentValueMethod, presentValueCalculator);
  }

  @Test
  /**
   * Tests the curve sensitivity with one bond in the basket.
   */
  public void presentValueCurveSensitivityOneBasket() {
    final double toleranceSensitivity = 1.0E+2; // 1.0E+2: 0.01 USD by bp 
    BondFixedSecurityDefinition[] basket1Definition = new BondFixedSecurityDefinition[1];
    basket1Definition[0] = BASKET_DEFINITION[0];
    double[] conversionFactor = new double[1];
    conversionFactor[0] = CONVERSION_FACTOR[0];
    BondFutureSecurityDefinition futureSecurity1Definition = new BondFutureSecurityDefinition(LAST_TRADING_DATE, FIRST_NOTICE_DATE, LAST_NOTICE_DATE, NOTIONAL, basket1Definition, conversionFactor);
    BondFutureTransactionDefinition futureTransaction1Definition = new BondFutureTransactionDefinition(futureSecurity1Definition, QUANTITY, REFERENCE_DATE, REFERENCE_PRICE);
    BondFutureTransaction futureTransaction1 = futureTransaction1Definition.toDerivative(REFERENCE_DATE, REFERENCE_PRICE, CURVES_NAME);
    InterestRateCurveSensitivity pvs = METHOD_FUTURE_TRANSACTION.presentValueCurveSensitivity(futureTransaction1, BUNDLE_HW);
    pvs = pvs.clean();
    // Bond curve sensitivity
    DoubleAVLTreeSet bondTime = new DoubleAVLTreeSet();
    bondTime.add(BOND_FUTURE_SECURITY.getDeliveryLastTime());
    for (int loopcpn = 0; loopcpn < futureTransaction1.getUnderlyingFuture().getDeliveryBasket()[0].getCoupon().getNumberOfPayments(); loopcpn++) {
      bondTime.add(futureTransaction1.getUnderlyingFuture().getDeliveryBasket()[0].getCoupon().getNthPayment(loopcpn).getPaymentTime());
    }
    double[] nodeTimesBond = bondTime.toDoubleArray();
    List<DoublesPair> fdSensiList = curveSensitvityFDCalculator(futureTransaction1, METHOD_FUTURE_TRANSACTION, BUNDLE_HW, CURVES_NAME[0], nodeTimesBond, 1e-8); // Shift is 1.0E-6
    final List<DoublesPair> sensiPvCredit = pvs.getSensitivities().get(CURVES_NAME[0]);
    assertEquals(fdSensiList.size(), sensiPvCredit.size());
    for (int loopnode = 0; loopnode < fdSensiList.size(); loopnode++) {
      final DoublesPair pairPv = sensiPvCredit.get(loopnode);
      assertEquals("Bond future curve sensitivity: node " + loopnode, fdSensiList.get(loopnode).first, pairPv.getFirst(), 1E-8); //checking times
    }
    for (int loopnode = 1; loopnode < fdSensiList.size(); loopnode++) {
      assertEquals("Bond future curve sensitivity: node " + loopnode, fdSensiList.get(loopnode).second, sensiPvCredit.get(loopnode).second, toleranceSensitivity);
    }
  }

  @Test
  /**
   * Tests the curve sensitivity.
   */
  public void presentValueCurveSensitivity() {
    final double toleranceSensitivity = 1.0E+5; // 1.0E+2: 0.01 USD by bp 
    InterestRateCurveSensitivity pvs = METHOD_FUTURE_TRANSACTION.presentValueCurveSensitivity(FUTURE_TRANSACTION, BUNDLE_HW);
    pvs = pvs.clean();
    // Bond curve sensitivity
    DoubleAVLTreeSet bondTime = new DoubleAVLTreeSet();
    bondTime.add(BOND_FUTURE_SECURITY.getDeliveryLastTime());
    for (int loopbasket = 0; loopbasket < NB_BOND; loopbasket++) {
      for (int loopcpn = 0; loopcpn < BASKET[loopbasket].getCoupon().getNumberOfPayments(); loopcpn++) {
        bondTime.add(BASKET[loopbasket].getCoupon().getNthPayment(loopcpn).getPaymentTime());
      }
    }
    double[] nodeTimesBond = bondTime.toDoubleArray();
    List<DoublesPair> fdSensiList = curveSensitvityFDCalculator(FUTURE_TRANSACTION, METHOD_FUTURE_TRANSACTION, BUNDLE_HW, CURVES_NAME[0], nodeTimesBond, 1e-9);
    final List<DoublesPair> sensiPvCredit = pvs.getSensitivities().get(CURVES_NAME[0]);
    assertEquals(fdSensiList.size(), sensiPvCredit.size());
    for (int loopnode = 0; loopnode < fdSensiList.size(); loopnode++) { //checking times
      final DoublesPair pairPv = sensiPvCredit.get(loopnode);
      assertEquals(fdSensiList.get(loopnode).getFirst(), pairPv.getFirst(), 1E-8);
    }
    for (int loopnode = 1; loopnode < fdSensiList.size(); loopnode++) { // Checking sensitivity value
      assertEquals("Bond future curve sensitivity: node " + loopnode, fdSensiList.get(loopnode).second, sensiPvCredit.get(loopnode).second, toleranceSensitivity);
    }
  }

  @Test(enabled = true)
  /**
   * Tests the present value curve sensitivity method for bond futures transactions.
   */
  public void presentValueCurveSensitivityRelative() {
    final InterestRateCurveSensitivity pvcsComputed = METHOD_FUTURE_TRANSACTION.presentValueCurveSensitivity(FUTURE_TRANSACTION, BUNDLE_HW);
    final InterestRateCurveSensitivity pcsSecurity = METHOD_FUTURE_SECURITY.priceCurveSensitivity(BOND_FUTURE_SECURITY, BUNDLE_HW);
    final InterestRateCurveSensitivity pvcsExpected = pcsSecurity.multiply(QUANTITY * NOTIONAL);
    assertEquals("Bond future transaction Discounting Method: present value curve sensitivity", pvcsExpected, pvcsComputed);
  }

  @Test(enabled = false)
  /**
   * Tests of performance. "enabled = false" for the standard testing.
   */
  public void performance() {
    long startTime, endTime;
    final int nbTest = 1000;
    CurrencyAmount pv = CurrencyAmount.of(CUR, 0.0);
    InterestRateCurveSensitivity pvs = new InterestRateCurveSensitivity();
    startTime = System.currentTimeMillis();
    for (int looptest = 0; looptest < nbTest; looptest++) {
      pv = METHOD_FUTURE_TRANSACTION.presentValue(FUTURE_TRANSACTION, BUNDLE_HW);
    }
    endTime = System.currentTimeMillis();
    System.out.println(nbTest + " pv Bond Future Hull-White (Default number of points): " + (endTime - startTime) + " ms (pv=" + pv + ")");
    // Performance note: HW pv: 13-Oct-11: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 165 ms for 1000 futures.

    startTime = System.currentTimeMillis();
    for (int looptest = 0; looptest < nbTest; looptest++) {
      pvs = METHOD_FUTURE_TRANSACTION.presentValueCurveSensitivity(FUTURE_TRANSACTION, BUNDLE_HW);
    }
    endTime = System.currentTimeMillis();
    System.out.println(nbTest + " pv curve sensitivity Bond Future Hull-White (Default number of points): " + (endTime - startTime) + " ms (pvs=" + pvs + ")");
    // Performance note: HW pv sensitivity: 13-Oct-11: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 175 ms for 1000 futures.
  }

}
