/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.payments;

import static org.testng.AssertJUnit.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.time.calendar.LocalDate;
import javax.time.calendar.Period;
import javax.time.calendar.ZonedDateTime;

import org.testng.annotations.Test;

import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.instrument.index.IborIndex;
import com.opengamma.financial.instrument.payment.CapFloorIborDefinition;
import com.opengamma.financial.instrument.payment.CouponFixedDefinition;
import com.opengamma.financial.instrument.payment.CouponIborDefinition;
import com.opengamma.financial.interestrate.ParRateCalculator;
import com.opengamma.financial.interestrate.PresentValueCalculator;
import com.opengamma.financial.interestrate.PresentValueSensitivity;
import com.opengamma.financial.interestrate.TestsDataSets;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.financial.model.option.definition.SABRInterestRateDataBundle;
import com.opengamma.financial.model.option.definition.SABRInterestRateParameter;
import com.opengamma.financial.model.option.pricing.analytic.formula.BlackFunctionData;
import com.opengamma.financial.model.option.pricing.analytic.formula.BlackPriceFunction;
import com.opengamma.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.math.curve.InterpolatedDoublesCurve;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.interpolation.LinearInterpolator1D;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtil;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Test related to the pricing and sensitivity of the Ibor cap/floor with the SABR model.
 */
public class CapFloorIborSABRMethodTest {
  // Details
  private static final Period TENOR = Period.ofMonths(3);
  private static final int SETTLEMENT_DAYS = 2;
  private static final Calendar CALENDAR = new MondayToFridayCalendar("A");
  private static final DayCount DAY_COUNT_INDEX = DayCountFactory.INSTANCE.getDayCount("Actual/360");
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Modified Following");
  private static final boolean IS_EOM = true;
  private static final Currency CUR = Currency.USD;
  private static final IborIndex INDEX = new IborIndex(CUR, TENOR, SETTLEMENT_DAYS, CALENDAR, DAY_COUNT_INDEX, BUSINESS_DAY, IS_EOM);
  private static final ZonedDateTime FIXING_DATE = DateUtil.getUTCDate(2011, 1, 3);
  private static final double NOTIONAL = 1000000; //1m
  private static final double STRIKE = 0.04;
  private static final boolean IS_CAP = true;
  // Definition description
  private static final CapFloorIborDefinition CAP_LONG_DEFINITION = CapFloorIborDefinition.from(NOTIONAL, FIXING_DATE, INDEX, STRIKE, IS_CAP);
  private static final CouponIborDefinition COUPON_IBOR_DEFINITION = CouponIborDefinition.from(NOTIONAL, FIXING_DATE, INDEX);
  private static final CouponFixedDefinition COUPON_STRIKE_DEFINITION = new CouponFixedDefinition(COUPON_IBOR_DEFINITION, STRIKE);
  private static final CapFloorIborDefinition CAP_SHORT_DEFINITION = CapFloorIborDefinition.from(-NOTIONAL, FIXING_DATE, INDEX, STRIKE, IS_CAP);
  private static final CapFloorIborDefinition FLOOR_SHORT_DEFINITION = CapFloorIborDefinition.from(-NOTIONAL, FIXING_DATE, INDEX, STRIKE, !IS_CAP);
  // Methods and calculator
  private static final CapFloorIborSABRMethod METHOD = new CapFloorIborSABRMethod();
  private static final ParRateCalculator PRC = ParRateCalculator.getInstance();
  private static final PresentValueCalculator PVC = PresentValueCalculator.getInstance();
  private static final BlackPriceFunction BLACK_FUNCTION = new BlackPriceFunction();
  // To derivative
  private static final LocalDate REFERENCE_DATE = LocalDate.of(2008, 8, 18);
  private static final String FUNDING_CURVE_NAME = "Funding";
  private static final String FORWARD_CURVE_NAME = "Forward";
  private static final String[] CURVES_NAME = {FUNDING_CURVE_NAME, FORWARD_CURVE_NAME};
  private static final CapFloorIbor CAP_LONG = (CapFloorIbor) CAP_LONG_DEFINITION.toDerivative(REFERENCE_DATE, CURVES_NAME);
  private static final CouponIbor COUPON_IBOR = (CouponIbor) COUPON_IBOR_DEFINITION.toDerivative(REFERENCE_DATE, CURVES_NAME);
  private static final CouponFixed COUPON_STRIKE = COUPON_STRIKE_DEFINITION.toDerivative(REFERENCE_DATE, CURVES_NAME);
  private static final CapFloorIbor CAP_SHORT = (CapFloorIbor) CAP_SHORT_DEFINITION.toDerivative(REFERENCE_DATE, CURVES_NAME);
  private static final CapFloorIbor FLOOR_SHORT = (CapFloorIbor) FLOOR_SHORT_DEFINITION.toDerivative(REFERENCE_DATE, CURVES_NAME);

  @Test
  /**
   * Test the present value using the method with the direct formula (Black with implied volatility).
   */
  public void testPresentValue() {
    YieldCurveBundle curves = TestsDataSets.createCurves1();
    SABRInterestRateParameter sabrParameter = TestsDataSets.createSABR1();
    SABRInterestRateDataBundle sabrBundle = new SABRInterestRateDataBundle(sabrParameter, curves);
    double methodPrice = METHOD.presentValue(CAP_LONG, sabrBundle);
    double df = curves.getCurve(FUNDING_CURVE_NAME).getDiscountFactor(CAP_LONG.getPaymentTime());
    double forward = PRC.visit(CAP_LONG, curves);
    double maturity = CAP_LONG.getFixingPeriodEndTime() - CAP_LONG.getFixingPeriodStartTime();
    double volatility = sabrParameter.getVolatility(CAP_LONG.getFixingTime(), maturity, STRIKE, forward);
    BlackFunctionData dataBlack = new BlackFunctionData(forward, df, volatility);
    EuropeanVanillaOption option = new EuropeanVanillaOption(STRIKE, CAP_LONG.getFixingTime(), IS_CAP);
    Function1D<BlackFunctionData, Double> funcBlack = BLACK_FUNCTION.getPriceFunction(option);
    double expectedPrice = funcBlack.evaluate(dataBlack) * CAP_LONG.getNotional() * CAP_LONG.getPaymentYearFraction();
    assertEquals("Cap/floor: SABR pricing", expectedPrice, methodPrice, 1E-2);
  }

  @Test
  /**
   * Test several present value parities: long/short, cap/floor/forward
   */
  public void testPresentValueParity() {
    YieldCurveBundle curves = TestsDataSets.createCurves1();
    SABRInterestRateParameter sabrParameter = TestsDataSets.createSABR1();
    SABRInterestRateDataBundle sabrBundle = new SABRInterestRateDataBundle(sabrParameter, curves);
    double priceCapLong = METHOD.presentValue(CAP_LONG, sabrBundle);
    double priceCapShort = METHOD.presentValue(CAP_SHORT, sabrBundle);
    assertEquals("Cap/floor - SABR pricing: long/short parity", -priceCapLong, priceCapShort, 1E-2);
    double priceFloorShort = METHOD.presentValue(FLOOR_SHORT, sabrBundle);
    double priceIbor = PVC.visit(COUPON_IBOR, curves);
    double priceStrike = PVC.visit(COUPON_STRIKE, curves);
    assertEquals("Cap/floor - SABR pricing: cap/floor parity", priceIbor - priceStrike, priceCapLong + priceFloorShort, 1E-2);
  }

  @Test
  /**
   * Test the present value rate sensitivity against a finite difference computation. Test sensitivity long/short parity.
   */
  public void testPresentValueSensitivity() {
    YieldCurveBundle curves = TestsDataSets.createCurves1();
    SABRInterestRateParameter sabrParameter = TestsDataSets.createSABR1();
    SABRInterestRateDataBundle sabrBundle = new SABRInterestRateDataBundle(sabrParameter, curves);
    double pv = METHOD.presentValue(CAP_LONG, sabrBundle);
    PresentValueSensitivity pvsCapLong = METHOD.presentValueSensitivity(CAP_LONG, sabrBundle);
    PresentValueSensitivity pvsCapShort = METHOD.presentValueSensitivity(CAP_SHORT, sabrBundle);
    // Long/short parity
    PresentValueSensitivity pvsCapShort_1 = pvsCapShort.multiply(-1);
    assertEquals(pvsCapLong.getSensitivity(), pvsCapShort_1.getSensitivity());
    // Present value sensitivity comparison with finite difference.
    double deltaTolerance = 1.0E+2;
    //Testing note: Sensitivity is for a movement of 1. 1E+2 = 1 cent for a 1 bp move. Tolerance increased to cope with numerical imprecision of finite difference.
    final double deltaShift = 1.0E-7;
    pvsCapLong = pvsCapLong.clean();
    // 1. Forward curve sensitivity
    String bumpedCurveName = "Bumped Curve";
    String[] bumpedCurvesForwardName = {FUNDING_CURVE_NAME, bumpedCurveName};
    CapFloorIbor capBumpedForward = (CapFloorIbor) CAP_LONG_DEFINITION.toDerivative(REFERENCE_DATE, bumpedCurvesForwardName);
    final YieldAndDiscountCurve curveForward = curves.getCurve(FORWARD_CURVE_NAME);
    Set<Double> timeForwardSet = new TreeSet<Double>();
    timeForwardSet.add(CAP_LONG.getFixingPeriodStartTime());
    timeForwardSet.add(CAP_LONG.getFixingPeriodEndTime());
    int nbForwardDate = timeForwardSet.size();
    List<Double> timeForwardList = new ArrayList<Double>(timeForwardSet);
    Double[] timeForwardArray = new Double[nbForwardDate];
    timeForwardArray = timeForwardList.toArray(timeForwardArray);
    final double[] yieldsForward = new double[nbForwardDate + 1];
    double[] nodeTimesForward = new double[nbForwardDate + 1];
    yieldsForward[0] = curveForward.getInterestRate(0.0);
    for (int i = 0; i < nbForwardDate; i++) {
      nodeTimesForward[i + 1] = timeForwardArray[i];
      yieldsForward[i + 1] = curveForward.getInterestRate(nodeTimesForward[i + 1]);
    }
    final YieldAndDiscountCurve tempCurveForward = new YieldCurve(InterpolatedDoublesCurve.fromSorted(nodeTimesForward, yieldsForward, new LinearInterpolator1D()));
    List<DoublesPair> tempForward = pvsCapLong.getSensitivity().get(FORWARD_CURVE_NAME);
    double[] resFwd = new double[nbForwardDate];
    for (int i = 0; i < nbForwardDate; i++) {
      final YieldAndDiscountCurve bumpedCurveForward = tempCurveForward.withSingleShift(nodeTimesForward[i + 1], deltaShift);
      final YieldCurveBundle curvesBumpedForward = new YieldCurveBundle();
      curvesBumpedForward.addAll(curves);
      curvesBumpedForward.setCurve("Bumped Curve", bumpedCurveForward);
      SABRInterestRateDataBundle sabrBundleBumped = new SABRInterestRateDataBundle(sabrParameter, curvesBumpedForward);
      final double bumpedpv = METHOD.presentValue(capBumpedForward, sabrBundleBumped);
      resFwd[i] = (bumpedpv - pv) / deltaShift;
      final DoublesPair pair = tempForward.get(i);
      assertEquals("Sensitivity to forward curve: Node " + i, nodeTimesForward[i + 1], pair.getFirst(), 1E-8);
      assertEquals("Sensitivity to forward curve: Node " + i, resFwd[i], pair.getSecond(), deltaTolerance);
    }
    // 2. Funding curve sensitivity
    String[] bumpedCurvesFundingName = {bumpedCurveName, FORWARD_CURVE_NAME};
    CapFloorIbor capBumpedFunding = (CapFloorIbor) CAP_LONG_DEFINITION.toDerivative(REFERENCE_DATE, bumpedCurvesFundingName);
    int nbPayDate = 1;
    final YieldAndDiscountCurve curveFunding = curves.getCurve(FUNDING_CURVE_NAME);
    final double[] yieldsFunding = new double[nbPayDate + 1];
    double[] nodeTimesFunding = new double[nbPayDate + 1];
    yieldsFunding[0] = curveFunding.getInterestRate(0.0);
    nodeTimesFunding[1] = CAP_LONG.getPaymentTime();
    yieldsFunding[1] = curveFunding.getInterestRate(nodeTimesFunding[1]);
    final YieldAndDiscountCurve tempCurveFunding = new YieldCurve(InterpolatedDoublesCurve.fromSorted(nodeTimesFunding, yieldsFunding, new LinearInterpolator1D()));
    List<DoublesPair> tempFunding = pvsCapLong.getSensitivity().get(FUNDING_CURVE_NAME);
    double[] resDsc = new double[nbPayDate];
    for (int i = 0; i < nbPayDate; i++) {
      final YieldAndDiscountCurve bumpedCurve = tempCurveFunding.withSingleShift(nodeTimesFunding[i + 1], deltaShift);
      final YieldCurveBundle curvesBumped = new YieldCurveBundle();
      curvesBumped.addAll(curves);
      curvesBumped.setCurve("Bumped Curve", bumpedCurve);
      SABRInterestRateDataBundle sabrBundleBumped = new SABRInterestRateDataBundle(sabrParameter, curvesBumped);
      final double bumpedpv = METHOD.presentValue(capBumpedFunding, sabrBundleBumped);
      resDsc[i] = (bumpedpv - pv) / deltaShift;
      final DoublesPair pair = tempFunding.get(i);
      assertEquals("Sensitivity to discounting curve: Node " + i, nodeTimesFunding[i + 1], pair.getFirst(), 1E-8);
      assertEquals("Sensitivity to discounting curve: Node " + i, resDsc[i], pair.getSecond(), deltaTolerance);
    }
  }
}
