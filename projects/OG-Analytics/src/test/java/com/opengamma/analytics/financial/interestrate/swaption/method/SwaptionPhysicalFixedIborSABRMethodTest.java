/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.swaption.method;

import static org.testng.AssertJUnit.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.annuity.AnnuityCouponFixedDefinition;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityCouponIborDefinition;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityDefinition;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIbor;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIborMaster;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexIborMaster;
import com.opengamma.analytics.financial.instrument.index.IndexSwap;
import com.opengamma.analytics.financial.instrument.payment.PaymentDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedIborDefinition;
import com.opengamma.analytics.financial.instrument.swaption.SwaptionPhysicalFixedIborDefinition;
import com.opengamma.analytics.financial.interestrate.InterestRateCurveSensitivity;
import com.opengamma.analytics.financial.interestrate.PV01Calculator;
import com.opengamma.analytics.financial.interestrate.ParRateCalculator;
import com.opengamma.analytics.financial.interestrate.PresentValueCurveSensitivityCalculator;
import com.opengamma.analytics.financial.interestrate.PresentValueCurveSensitivitySABRCalculator;
import com.opengamma.analytics.financial.interestrate.PresentValueSABRCalculator;
import com.opengamma.analytics.financial.interestrate.PresentValueSABRHullWhiteMonteCarloCalculator;
import com.opengamma.analytics.financial.interestrate.PresentValueSABRSensitivityDataBundle;
import com.opengamma.analytics.financial.interestrate.PresentValueSABRSensitivitySABRCalculator;
import com.opengamma.analytics.financial.interestrate.TestsDataSetsSABR;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Coupon;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIbor;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.analytics.financial.interestrate.swap.derivative.SwapFixedCoupon;
import com.opengamma.analytics.financial.interestrate.swap.method.SwapFixedCouponDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionPhysicalFixedIbor;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.financial.model.option.definition.SABRInterestRateDataBundle;
import com.opengamma.analytics.financial.model.option.definition.SABRInterestRateParameters;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.BlackFunctionData;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.BlackPriceFunction;
import com.opengamma.analytics.financial.model.volatility.smile.function.SABRHaganAlternativeVolatilityFunction;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.interpolation.LinearInterpolator1D;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.tuple.DoublesPair;

/**
 * @deprecated This class tests deprecated functionality.
 */
@Deprecated
@Test(groups = TestGroup.UNIT)
public class SwaptionPhysicalFixedIborSABRMethodTest {
  // Swaption description
  private static final ZonedDateTime EXPIRY_DATE = DateUtils.getUTCDate(2014, 3, 18);
  private static final boolean IS_LONG = true;
  private static final int SETTLEMENT_DAYS = 2;
  // Swap 5Y description
  private static final Currency CUR = Currency.EUR;
  private static final Calendar CALENDAR = new MondayToFridayCalendar("A");
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventions.MODIFIED_FOLLOWING;
  private static final boolean IS_EOM = true;
  private static final int ANNUITY_TENOR_YEAR = 5;
  private static final Period ANNUITY_TENOR = Period.ofYears(ANNUITY_TENOR_YEAR);
  private static final ZonedDateTime SETTLEMENT_DATE = ScheduleCalculator.getAdjustedDate(EXPIRY_DATE, SETTLEMENT_DAYS, CALENDAR);
  private static final double NOTIONAL = 100000000; //100m
  //  Fixed leg: Semi-annual bond
  private static final Period FIXED_PAYMENT_PERIOD = Period.ofMonths(6);
  private static final DayCount FIXED_DAY_COUNT = DayCounts.THIRTY_U_360;
  private static final double RATE = 0.0325;
  private static final boolean FIXED_IS_PAYER = true;
  private static final AnnuityCouponFixedDefinition FIXED_ANNUITY_PAYER = AnnuityCouponFixedDefinition.from(CUR, SETTLEMENT_DATE, ANNUITY_TENOR, FIXED_PAYMENT_PERIOD, CALENDAR, FIXED_DAY_COUNT,
      BUSINESS_DAY, IS_EOM, NOTIONAL, RATE, FIXED_IS_PAYER);
  private static final AnnuityCouponFixedDefinition FIXED_ANNUITY_RECEIVER = AnnuityCouponFixedDefinition.from(CUR, SETTLEMENT_DATE, ANNUITY_TENOR, FIXED_PAYMENT_PERIOD, CALENDAR, FIXED_DAY_COUNT,
      BUSINESS_DAY, IS_EOM, NOTIONAL, RATE, !FIXED_IS_PAYER);
  //  Ibor leg: quarterly money
  private static final Period INDEX_TENOR = Period.ofMonths(3);
  private static final DayCount DAY_COUNT = DayCounts.ACT_360;
  private static final IborIndex IBOR_INDEX = new IborIndex(CUR, INDEX_TENOR, SETTLEMENT_DAYS, DAY_COUNT, BUSINESS_DAY, IS_EOM);
  private static final IndexSwap CMS_INDEX = new IndexSwap(FIXED_PAYMENT_PERIOD, FIXED_DAY_COUNT, IBOR_INDEX, ANNUITY_TENOR, CALENDAR);
  private static final AnnuityCouponIborDefinition IBOR_ANNUITY_RECEIVER = AnnuityCouponIborDefinition.from(SETTLEMENT_DATE, ANNUITY_TENOR, NOTIONAL, IBOR_INDEX, !FIXED_IS_PAYER, CALENDAR);
  private static final AnnuityCouponIborDefinition IBOR_ANNUITY_PAYER = AnnuityCouponIborDefinition.from(SETTLEMENT_DATE, ANNUITY_TENOR, NOTIONAL, IBOR_INDEX, FIXED_IS_PAYER, CALENDAR);
  // Swaption construction: All combinations
  private static final SwapFixedIborDefinition SWAP_DEFINITION_PAYER = new SwapFixedIborDefinition(FIXED_ANNUITY_PAYER, IBOR_ANNUITY_RECEIVER);
  private static final SwapFixedIborDefinition SWAP_DEFINITION_RECEIVER = new SwapFixedIborDefinition(FIXED_ANNUITY_RECEIVER, IBOR_ANNUITY_PAYER);
  private static final SwaptionPhysicalFixedIborDefinition SWAPTION_DEFINITION_LONG_PAYER = SwaptionPhysicalFixedIborDefinition.from(EXPIRY_DATE, SWAP_DEFINITION_PAYER, true, IS_LONG);
  private static final SwaptionPhysicalFixedIborDefinition SWAPTION_DEFINITION_LONG_RECEIVER = SwaptionPhysicalFixedIborDefinition.from(EXPIRY_DATE, SWAP_DEFINITION_RECEIVER, false, IS_LONG);
  private static final SwaptionPhysicalFixedIborDefinition SWAPTION_DEFINITION_SHORT_PAYER = SwaptionPhysicalFixedIborDefinition.from(EXPIRY_DATE, SWAP_DEFINITION_PAYER, true, !IS_LONG);
  private static final SwaptionPhysicalFixedIborDefinition SWAPTION_DEFINITION_SHORT_RECEIVER = SwaptionPhysicalFixedIborDefinition.from(EXPIRY_DATE, SWAP_DEFINITION_RECEIVER, false, !IS_LONG);
  // to derivatives
  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2008, 8, 18);
  private static final String FUNDING_CURVE_NAME = "Funding";
  private static final String FORWARD_CURVE_NAME = "Forward";
  private static final String[] CURVES_NAME = {FUNDING_CURVE_NAME, FORWARD_CURVE_NAME};
  private static final YieldCurveBundle CURVES = TestsDataSetsSABR.createCurves1();
  private static final SABRInterestRateParameters SABR_PARAMETER = TestsDataSetsSABR.createSABR1();
  private static final SABRInterestRateDataBundle SABR_BUNDLE = new SABRInterestRateDataBundle(SABR_PARAMETER, CURVES);
  private static final SwapFixedCoupon<Coupon> SWAP_PAYER = SWAP_DEFINITION_PAYER.toDerivative(REFERENCE_DATE, CURVES_NAME);
  private static final SwapFixedCoupon<Coupon> SWAP_RECEIVER = SWAP_DEFINITION_RECEIVER.toDerivative(REFERENCE_DATE, CURVES_NAME);
  private static final SwaptionPhysicalFixedIbor SWAPTION_LONG_PAYER = SWAPTION_DEFINITION_LONG_PAYER.toDerivative(REFERENCE_DATE, CURVES_NAME);
  private static final SwaptionPhysicalFixedIbor SWAPTION_LONG_RECEIVER = SWAPTION_DEFINITION_LONG_RECEIVER.toDerivative(REFERENCE_DATE, CURVES_NAME);
  private static final SwaptionPhysicalFixedIbor SWAPTION_SHORT_PAYER = SWAPTION_DEFINITION_SHORT_PAYER.toDerivative(REFERENCE_DATE, CURVES_NAME);
  private static final SwaptionPhysicalFixedIbor SWAPTION_SHORT_RECEIVER = SWAPTION_DEFINITION_SHORT_RECEIVER.toDerivative(REFERENCE_DATE, CURVES_NAME);
  // Calculators
  private static final PresentValueSABRCalculator PVC = PresentValueSABRCalculator.getInstance();
  private static final ParRateCalculator PRC = ParRateCalculator.getInstance();
  private static final PresentValueCurveSensitivityCalculator PVSC = PresentValueCurveSensitivityCalculator.getInstance();
  private static final PresentValueCurveSensitivitySABRCalculator PVCSC_SABR = PresentValueCurveSensitivitySABRCalculator.getInstance();
  private static final PV01Calculator PV01C = new PV01Calculator(PVCSC_SABR);
  private static final PresentValueSABRSensitivitySABRCalculator PVSSC_SABR = PresentValueSABRSensitivitySABRCalculator.getInstance();
  private static final SwaptionPhysicalFixedIborSABRMethod METHOD = SwaptionPhysicalFixedIborSABRMethod.getInstance();
  // Pricing functions
  private static final BlackPriceFunction BLACK_FUNCTION = new BlackPriceFunction();
  private static final SwapFixedCouponDiscountingMethod METHOD_SWAP = SwapFixedCouponDiscountingMethod.getInstance();

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNoSABRHaganSensi() {
    final YieldCurveBundle curves = TestsDataSetsSABR.createCurves1();
    final SABRInterestRateParameters sabrParameter = TestsDataSetsSABR.createSABR1(new SABRHaganAlternativeVolatilityFunction());
    final SABRInterestRateDataBundle sabrBundle = new SABRInterestRateDataBundle(sabrParameter, curves);
    SWAPTION_LONG_PAYER.accept(PVCSC_SABR, sabrBundle);
  }

  /**
   * Tests present value with respect to a hard-coded value. Tests against the explicit formula. Tests long/short parity and payer/receiver/swap parity.
   */
  @Test
  public void testPresentValue() {
    final YieldCurveBundle curves = TestsDataSetsSABR.createCurves1();
    final SABRInterestRateParameters sabrParameter = TestsDataSetsSABR.createSABR1();
    final SABRInterestRateDataBundle sabrBundle = new SABRInterestRateDataBundle(sabrParameter, curves);
    // Swaption pricing.
    final double priceLongPayer = SWAPTION_LONG_PAYER.accept(PVC, sabrBundle);
    final double priceShortPayer = SWAPTION_SHORT_PAYER.accept(PVC, sabrBundle);
    final double priceLongReceiver = SWAPTION_LONG_RECEIVER.accept(PVC, sabrBundle);
    final double priceShortReceiver = SWAPTION_SHORT_RECEIVER.accept(PVC, sabrBundle);
    // From previous run
    final double expectedPriceLongPayer = 4973866.250;
    assertEquals(expectedPriceLongPayer, priceLongPayer, 1E-2);
    final double pvbp = METHOD_SWAP.presentValueBasisPoint(SWAP_PAYER, curves.getCurve(FUNDING_CURVE_NAME));
    final double forward = SWAP_PAYER.accept(PRC, curves);
    final double maturity = SWAP_PAYER.getFirstLeg().getNthPayment(SWAP_PAYER.getFirstLeg().getNumberOfPayments() - 1).getPaymentTime() - SWAPTION_LONG_PAYER.getSettlementTime();
    assertEquals(maturity, ANNUITY_TENOR_YEAR, 1E-2);
    final double volatility = sabrParameter.getVolatility(SWAPTION_LONG_PAYER.getTimeToExpiry(), maturity, RATE, forward);
    final BlackFunctionData data = new BlackFunctionData(forward, pvbp, volatility);
    final Function1D<BlackFunctionData, Double> func = BLACK_FUNCTION.getPriceFunction(SWAPTION_LONG_PAYER);
    final double expectedPrice = func.evaluate(data);
    assertEquals(expectedPrice, priceLongPayer, 1E-2);
    // Long/Short parity
    assertEquals(priceLongPayer, -priceShortPayer, 1E-2);
    assertEquals(priceLongReceiver, -priceShortReceiver, 1E-2);
    // Payer/Receiver parity
    final double priceSwapPayer = SWAP_PAYER.accept(PVC, curves);
    final double priceSwapReceiver = SWAP_RECEIVER.accept(PVC, curves);
    assertEquals(priceSwapPayer, priceLongPayer + priceShortReceiver, 1E-2);
    assertEquals(priceSwapReceiver, priceLongReceiver + priceShortPayer, 1E-2);
  }

  /**
   * Test the absence of arbitrage between swaptions with same cash-flows but different conventions.
   */
  @Test
  public void testPresentValueConventionArbitrage() {
    final YieldCurveBundle curves = TestsDataSetsSABR.createCurves1();
    final SABRInterestRateParameters sabrParameter = TestsDataSetsSABR.createSABR1();
    final SABRInterestRateDataBundle sabrBundle = new SABRInterestRateDataBundle(sabrParameter, curves);
    final double rate360 = 0.0360;
    final IndexSwap index360 = new IndexSwap(FIXED_PAYMENT_PERIOD, DayCounts.ACT_360, IBOR_INDEX, ANNUITY_TENOR, CALENDAR);
    final SwapFixedIborDefinition swap360 = SwapFixedIborDefinition.from(SETTLEMENT_DATE, index360, NOTIONAL, rate360, FIXED_IS_PAYER, CALENDAR);
    final SwaptionPhysicalFixedIborDefinition swaption360Definition = SwaptionPhysicalFixedIborDefinition.from(EXPIRY_DATE, swap360, FIXED_IS_PAYER, IS_LONG);
    final SwaptionPhysicalFixedIbor swaption360 = swaption360Definition.toDerivative(REFERENCE_DATE, CURVES_NAME);
    final double rate365 = 0.0365;
    final IndexSwap index365 = new IndexSwap(FIXED_PAYMENT_PERIOD, DayCounts.ACT_365, IBOR_INDEX, ANNUITY_TENOR, CALENDAR);
    final SwapFixedIborDefinition swap365 = SwapFixedIborDefinition.from(SETTLEMENT_DATE, index365, NOTIONAL, rate365, FIXED_IS_PAYER, CALENDAR);
    final SwaptionPhysicalFixedIborDefinition swaption365Definition = SwaptionPhysicalFixedIborDefinition.from(EXPIRY_DATE, swap365, FIXED_IS_PAYER, IS_LONG);
    final SwaptionPhysicalFixedIbor swaption365 = swaption365Definition.toDerivative(REFERENCE_DATE, CURVES_NAME);
    final double price360 = swaption360.accept(PVC, sabrBundle);
    final double price365 = swaption365.accept(PVC, sabrBundle);
    assertEquals(price360, price365, 1E-2);
  }

  @Test
  /**
   * Tests the method against the present value calculator.
   */
  public void presentValueMethodVsCalculator() {
    final YieldCurveBundle curves = TestsDataSetsSABR.createCurves1();
    final SABRInterestRateParameters sabrParameter = TestsDataSetsSABR.createSABR1();
    final SABRInterestRateDataBundle sabrBundle = new SABRInterestRateDataBundle(sabrParameter, curves);
    final double pvMethod = METHOD.presentValue(SWAPTION_LONG_PAYER, sabrBundle).getAmount();
    final double pvCalculator = SWAPTION_LONG_PAYER.accept(PVC, sabrBundle);
    assertEquals("Swaption physical SABR: present value : method and calculator", pvMethod, pvCalculator);
  }

  @Test
  /**
   * Tests the method against the present value curve sensitivity calculator.
   */
  public void presentValueCurveSensitivityMethodVsCalculator() {
    final YieldCurveBundle curves = TestsDataSetsSABR.createCurves1();
    final SABRInterestRateParameters sabrParameter = TestsDataSetsSABR.createSABR1();
    final SABRInterestRateDataBundle sabrBundle = new SABRInterestRateDataBundle(sabrParameter, curves);
    final InterestRateCurveSensitivity pvsMethod = METHOD.presentValueCurveSensitivity(SWAPTION_LONG_PAYER, sabrBundle);
    final Map<String, List<DoublesPair>> pvsCalculator = SWAPTION_LONG_PAYER.accept(PVCSC_SABR, sabrBundle);
    assertEquals("Swaption physical SABR: present value curve sensitivity: method and calculator", pvsMethod.getSensitivities(), pvsCalculator);
  }

  @Test
  public void testPresentValueCurveSensitivity() {
    final YieldCurveBundle curves = TestsDataSetsSABR.createCurves1();
    final SABRInterestRateParameters sabrParameter = TestsDataSetsSABR.createSABR1();
    final SABRInterestRateDataBundle sabrBundle = new SABRInterestRateDataBundle(sabrParameter, curves);
    // Swaption sensitivity
    final InterestRateCurveSensitivity pvsLongPayer = METHOD.presentValueCurveSensitivity(SWAPTION_LONG_PAYER, sabrBundle);
    final InterestRateCurveSensitivity pvsShortPayer = METHOD.presentValueCurveSensitivity(SWAPTION_SHORT_PAYER, sabrBundle);
    // Long/short parity
    final InterestRateCurveSensitivity pvsShortPayer_1 = pvsShortPayer.multipliedBy(-1);
    assertEquals(pvsLongPayer.getSensitivities(), pvsShortPayer_1.getSensitivities());
    // PresentValueCalculator
    final Map<String, List<DoublesPair>> pvscLongPayer = SWAPTION_LONG_PAYER.accept(PVCSC_SABR, sabrBundle);
    assertEquals(pvsLongPayer.getSensitivities(), pvscLongPayer);
    // Present value sensitivity comparison with finite difference.
    final double deltaTolerance = 1E+2; //Sensitivity is for a movement of 1. 1E+2 = 1 cent for a 1 bp move.
    final double deltaShift = 1e-9;
    InterestRateCurveSensitivity pvsSwapPayer = new InterestRateCurveSensitivity(SWAP_PAYER.accept(PVSC, sabrBundle));
    pvsSwapPayer = pvsSwapPayer.cleaned();
    InterestRateCurveSensitivity sensi = new InterestRateCurveSensitivity(pvscLongPayer);
    sensi = sensi.cleaned();
    final double pv = SWAPTION_LONG_PAYER.accept(PVC, sabrBundle);
    // 1. Forward curve sensitivity
    final String bumpedCurveName = "Bumped Curve";
    final String[] bumpedCurvesForwardName = {FUNDING_CURVE_NAME, bumpedCurveName};
    final SwaptionPhysicalFixedIbor swaptionBumpedForward = SWAPTION_DEFINITION_LONG_PAYER.toDerivative(REFERENCE_DATE, bumpedCurvesForwardName);
    final YieldAndDiscountCurve curveForward = curves.getCurve(FORWARD_CURVE_NAME);
    final Set<Double> timeForwardSet = new TreeSet<>();
    for (final Payment pay : SWAPTION_LONG_PAYER.getUnderlyingSwap().getSecondLeg().getPayments()) {
      final CouponIbor coupon = (CouponIbor) pay;
      timeForwardSet.add(coupon.getFixingPeriodStartTime());
      timeForwardSet.add(coupon.getFixingPeriodEndTime());
    }
    final int nbForwardDate = timeForwardSet.size();
    final List<Double> timeForwardList = new ArrayList<>(timeForwardSet);
    Double[] timeForwardArray = new Double[nbForwardDate];
    timeForwardArray = timeForwardList.toArray(timeForwardArray);
    final double[] yieldsForward = new double[nbForwardDate + 1];
    final double[] nodeTimesForward = new double[nbForwardDate + 1];
    yieldsForward[0] = curveForward.getInterestRate(0.0);
    for (int i = 0; i < nbForwardDate; i++) {
      nodeTimesForward[i + 1] = timeForwardArray[i];
      yieldsForward[i + 1] = curveForward.getInterestRate(nodeTimesForward[i + 1]);
    }
    final YieldAndDiscountCurve tempCurveForward = YieldCurve.from(InterpolatedDoublesCurve.fromSorted(nodeTimesForward, yieldsForward, new LinearInterpolator1D()));
    final List<DoublesPair> tempForward = sensi.getSensitivities().get(FORWARD_CURVE_NAME);
    for (int i = 0; i < nbForwardDate; i++) {
      final YieldAndDiscountCurve bumpedCurveForward = tempCurveForward.withSingleShift(nodeTimesForward[i + 1], deltaShift);
      final YieldCurveBundle curvesBumpedForward = new YieldCurveBundle();
      curvesBumpedForward.addAll(curves);
      curvesBumpedForward.setCurve("Bumped Curve", bumpedCurveForward);
      final SABRInterestRateDataBundle sabrBundleBumped = new SABRInterestRateDataBundle(sabrParameter, curvesBumpedForward);
      final double bumpedpv = swaptionBumpedForward.accept(PVC, sabrBundleBumped);
      final double res = (bumpedpv - pv) / deltaShift;
      final DoublesPair pair = tempForward.get(i);
      assertEquals("Node " + i, nodeTimesForward[i + 1], pair.getFirst(), 1E-8);
      assertEquals("Node " + i, res, pair.getSecond(), deltaTolerance);
    }
    // 2. Funding curve sensitivity
    final String[] bumpedCurvesFundingName = {bumpedCurveName, FORWARD_CURVE_NAME};
    final SwaptionPhysicalFixedIbor swaptionBumpedFunding = SWAPTION_DEFINITION_LONG_PAYER.toDerivative(REFERENCE_DATE, bumpedCurvesFundingName);
    final SwapDefinition underlyingSwap = SWAPTION_DEFINITION_LONG_PAYER.getUnderlyingSwap();
    AnnuityDefinition<? extends PaymentDefinition> floatLeg;
    if (underlyingSwap.getFirstLeg() instanceof AnnuityCouponFixedDefinition) {
      floatLeg = underlyingSwap.getSecondLeg();
    } else {
      floatLeg = underlyingSwap.getFirstLeg();
    }
    final int nbPayDate = floatLeg.getPayments().length;
    final YieldAndDiscountCurve curveFunding = curves.getCurve(FUNDING_CURVE_NAME);
    final double[] yieldsFunding = new double[nbPayDate + 1];
    final double[] nodeTimesFunding = new double[nbPayDate + 1];
    yieldsFunding[0] = curveFunding.getInterestRate(0.0);
    for (int i = 0; i < nbPayDate; i++) {
      nodeTimesFunding[i + 1] = SWAPTION_LONG_PAYER.getUnderlyingSwap().getSecondLeg().getNthPayment(i).getPaymentTime();
      yieldsFunding[i + 1] = curveFunding.getInterestRate(nodeTimesFunding[i + 1]);
    }
    final YieldAndDiscountCurve tempCurveFunding = YieldCurve.from(InterpolatedDoublesCurve.fromSorted(nodeTimesFunding, yieldsFunding, new LinearInterpolator1D()));
    final List<DoublesPair> tempFunding = sensi.getSensitivities().get(FUNDING_CURVE_NAME);
    for (int i = 0; i < nbPayDate; i++) {
      final YieldAndDiscountCurve bumpedCurve = tempCurveFunding.withSingleShift(nodeTimesFunding[i + 1], deltaShift);
      final YieldCurveBundle curvesBumped = new YieldCurveBundle();
      curvesBumped.addAll(curves);
      curvesBumped.setCurve("Bumped Curve", bumpedCurve);
      final SABRInterestRateDataBundle sabrBundleBumped = new SABRInterestRateDataBundle(sabrParameter, curvesBumped);
      final double bumpedpv = swaptionBumpedFunding.accept(PVC, sabrBundleBumped);
      final double res = (bumpedpv - pv) / deltaShift;
      final DoublesPair pair = tempFunding.get(i);
      assertEquals("Node " + i, nodeTimesFunding[i + 1], pair.getFirst(), 1E-8);
      assertEquals("Node " + i, res, pair.getSecond(), deltaTolerance);
    }
  }

  @Test
  public void testPresentValueSABRSensitivity() {
    final YieldCurveBundle curves = TestsDataSetsSABR.createCurves1();
    final SABRInterestRateParameters sabrParameter = TestsDataSetsSABR.createSABR1();
    final SABRInterestRateDataBundle sabrBundle = new SABRInterestRateDataBundle(sabrParameter, curves);
    // Swaption sensitivity
    final PresentValueSABRSensitivityDataBundle pvsLongPayer = METHOD.presentValueSABRSensitivity(SWAPTION_LONG_PAYER, sabrBundle);
    PresentValueSABRSensitivityDataBundle pvsShortPayer = METHOD.presentValueSABRSensitivity(SWAPTION_SHORT_PAYER, sabrBundle);
    // Long/short parity
    pvsShortPayer = pvsShortPayer.multiplyBy(-1.0);
    assertEquals(pvsLongPayer.getAlpha(), pvsShortPayer.getAlpha());
    // SABR sensitivity vs finite difference
    final double pvLongPayer = METHOD.presentValue(SWAPTION_LONG_PAYER, sabrBundle).getAmount();
    final double shift = 0.000001;
    final DoublesPair expectedExpiryTenor = DoublesPair.of(SWAPTION_LONG_PAYER.getTimeToExpiry(), ANNUITY_TENOR_YEAR);
    // Alpha sensitivity vs finite difference computation
    final SABRInterestRateParameters sabrParameterAlphaBumped = TestsDataSetsSABR.createSABR1AlphaBumped(shift);
    final SABRInterestRateDataBundle sabrBundleAlphaBumped = new SABRInterestRateDataBundle(sabrParameterAlphaBumped, curves);
    final double pvLongPayerAlphaBumped = METHOD.presentValue(SWAPTION_LONG_PAYER, sabrBundleAlphaBumped).getAmount();
    final double expectedAlphaSensi = (pvLongPayerAlphaBumped - pvLongPayer) / shift;
    assertEquals("Number of alpha sensitivity", pvsLongPayer.getAlpha().getMap().keySet().size(), 1);
    assertEquals("Alpha sensitivity expiry/tenor", pvsLongPayer.getAlpha().getMap().keySet().contains(expectedExpiryTenor), true);
    assertEquals("Alpha sensitivity value", pvsLongPayer.getAlpha().getMap().get(expectedExpiryTenor), expectedAlphaSensi, 1.5E+1);
    // Rho sensitivity vs finite difference computation
    final SABRInterestRateParameters sabrParameterRhoBumped = TestsDataSetsSABR.createSABR1RhoBumped(shift);
    final SABRInterestRateDataBundle sabrBundleRhoBumped = new SABRInterestRateDataBundle(sabrParameterRhoBumped, curves);
    final double pvLongPayerRhoBumped = METHOD.presentValue(SWAPTION_LONG_PAYER, sabrBundleRhoBumped).getAmount();
    final double expectedRhoSensi = (pvLongPayerRhoBumped - pvLongPayer) / shift;
    assertEquals("Number of rho sensitivity", pvsLongPayer.getRho().getMap().keySet().size(), 1);
    assertEquals("Rho sensitivity expiry/tenor", pvsLongPayer.getRho().getMap().keySet().contains(expectedExpiryTenor), true);
    assertEquals("Rho sensitivity value", pvsLongPayer.getRho().getMap().get(expectedExpiryTenor), expectedRhoSensi, 1.0E+0);
    // Alpha sensitivity vs finite difference computation
    final SABRInterestRateParameters sabrParameterNuBumped = TestsDataSetsSABR.createSABR1NuBumped(shift);
    final SABRInterestRateDataBundle sabrBundleNuBumped = new SABRInterestRateDataBundle(sabrParameterNuBumped, curves);
    final double pvLongPayerNuBumped = METHOD.presentValue(SWAPTION_LONG_PAYER, sabrBundleNuBumped).getAmount();
    final double expectedNuSensi = (pvLongPayerNuBumped - pvLongPayer) / shift;
    assertEquals("Number of nu sensitivity", pvsLongPayer.getNu().getMap().keySet().size(), 1);
    assertEquals("Nu sensitivity expiry/tenor", pvsLongPayer.getNu().getMap().keySet().contains(expectedExpiryTenor), true);
    assertEquals("Nu sensitivity value", pvsLongPayer.getNu().getMap().get(expectedExpiryTenor), expectedNuSensi, 2.0E+0);
  }

  @Test
  /**
   * Tests the present value SABR parameters sensitivity: Method vs Calculator.
   */
  public void presentValueSABRSensitivityMethodVsCalculator() {
    final PresentValueSABRSensitivityDataBundle pvssMethod = METHOD.presentValueSABRSensitivity(SWAPTION_LONG_PAYER, SABR_BUNDLE);
    final PresentValueSABRSensitivityDataBundle pvssCalculator = SWAPTION_LONG_PAYER.accept(PVSSC_SABR, SABR_BUNDLE);
    assertEquals("Swaption Physical SABR: Present value SABR sensitivity: method vs calculator", pvssMethod, pvssCalculator);
  }

  @Test(enabled = true)
  /**
   * Tests the present value of the Hull-White Monte-Carlo calibrated to SABR swaption.
   */
  public void presentValueSABRHullWhiteMonteCarlo() {
    final CurrencyAmount pvSABR = METHOD.presentValue(SWAPTION_LONG_PAYER, SABR_BUNDLE);
    final PresentValueSABRHullWhiteMonteCarloCalculator pvcSABRHWMC = PresentValueSABRHullWhiteMonteCarloCalculator.getInstance();
    final double pvMC = SWAPTION_LONG_PAYER.accept(pvcSABRHWMC, SABR_BUNDLE);
    assertEquals("Swaption Physical SABR: Present value using Hull-White by Monte Carlo", pvSABR.getAmount(), pvMC, 2.5E+4);
  }

  @Test(enabled = false)
  /**
   * Analyzes the smoothness of sensitivities.
   */
  public void analysisSensitivities() {
    final IborIndex USDLIBOR3M = IndexIborMaster.getInstance().getIndex("USDLIBOR3M");
    final Period expiryTenor = Period.ofYears(5);
    final Period underlyingTenor = Period.ofYears(10);
    final ZonedDateTime expiryDate = ScheduleCalculator.getAdjustedDate(REFERENCE_DATE, expiryTenor, USDLIBOR3M, CALENDAR);
    final ZonedDateTime settleDate = ScheduleCalculator.getAdjustedDate(expiryDate, USDLIBOR3M.getSpotLag(), CALENDAR);
    final GeneratorSwapFixedIbor USD6MLIBOR3M = GeneratorSwapFixedIborMaster.getInstance().getGenerator("USD6MLIBOR3M", CALENDAR);
    final double notional = 1000000; // 1m
    final double strikeRange = 0.1150;
    final double strikeStart = 0.0050;
    final int nbStrike = 50;
    final double[] strikes = new double[nbStrike + 1];
    final SwaptionPhysicalFixedIbor[] swaptions = new SwaptionPhysicalFixedIbor[nbStrike + 1];
    final double[] pv = new double[nbStrike + 1];
    final double[] pv01Dsc = new double[nbStrike + 1];
    final double[] pv01Fwd = new double[nbStrike + 1];
    final double[] alphaSensi = new double[nbStrike + 1];
    final double[] rhoSensi = new double[nbStrike + 1];
    final double[] nuSensi = new double[nbStrike + 1];
    for (int loopstrike = 0; loopstrike <= nbStrike; loopstrike++) {
      strikes[loopstrike] = strikeStart + loopstrike * strikeRange / nbStrike;
      final SwapFixedIborDefinition swapDefinition = SwapFixedIborDefinition.from(settleDate, underlyingTenor, USD6MLIBOR3M, notional, strikes[loopstrike], true);
      final SwaptionPhysicalFixedIborDefinition swaptionDefinition = SwaptionPhysicalFixedIborDefinition.from(expiryDate, swapDefinition, true, true);
      swaptions[loopstrike] = swaptionDefinition.toDerivative(REFERENCE_DATE, CURVES_NAME);
      pv[loopstrike] = METHOD.presentValue(swaptions[loopstrike], SABR_BUNDLE).getAmount();
      final PresentValueSABRSensitivityDataBundle sabrSensi = METHOD.presentValueSABRSensitivity(swaptions[loopstrike], SABR_BUNDLE);
      final Map<String, Double> pv01 = PV01C.visit(swaptions[loopstrike], SABR_BUNDLE);
      alphaSensi[loopstrike] = sabrSensi.getAlpha().toSingleValue();
      rhoSensi[loopstrike] = sabrSensi.getRho().toSingleValue();
      nuSensi[loopstrike] = sabrSensi.getNu().toSingleValue();
      pv01Dsc[loopstrike] = pv01.get(CURVES_NAME[0]);
      pv01Fwd[loopstrike] = pv01.get(CURVES_NAME[1]);
    }
    @SuppressWarnings("unused")
    final double atm = swaptions[0].getUnderlyingSwap().accept(PRC, CURVES);
  }

  @Test(enabled = false)
  /**
   * Test of performance. In normal testing, "enabled = false".
   */
  public void performance() {
    long startTime, endTime;
    final int nbTest = 10000;
    final double[] pv = new double[nbTest];
    final PresentValueSABRSensitivityDataBundle[] pvss = new PresentValueSABRSensitivityDataBundle[nbTest];

    startTime = System.currentTimeMillis();
    for (int looptest = 0; looptest < nbTest; looptest++) {
      pv[looptest] = SWAPTION_LONG_PAYER.accept(PVC, SABR_BUNDLE) + looptest;
    }
    endTime = System.currentTimeMillis();
    System.out.println(nbTest + " physical swaptions SABR (price): " + (endTime - startTime) + " ms");
    // Performance note: price: 12-Jun-12: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 25 ms for 1000 swaptions.

    startTime = System.currentTimeMillis();
    for (int looptest = 0; looptest < nbTest; looptest++) {
      pv[looptest] = SWAPTION_LONG_PAYER.accept(PVC, SABR_BUNDLE) + looptest;
      SWAPTION_LONG_PAYER.accept(PVCSC_SABR, SABR_BUNDLE);
      pvss[looptest] = SWAPTION_LONG_PAYER.accept(PVSSC_SABR, SABR_BUNDLE);
    }
    endTime = System.currentTimeMillis();
    System.out.println(nbTest + " physical swaptions SABR (price+delta+vega): " + (endTime - startTime) + " ms");
    // Performance note: price+delta+vega: 12-Jun-12: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 110 ms for 1000 swaptions.

    final int nbTest2 = 10;
    final PresentValueSABRHullWhiteMonteCarloCalculator pvcSABRHWMC = PresentValueSABRHullWhiteMonteCarloCalculator.getInstance();
    final double[] pvMC = new double[nbTest2];

    startTime = System.currentTimeMillis();
    for (int looptest = 0; looptest < nbTest2; looptest++) {
      pvMC[looptest] = SWAPTION_LONG_PAYER.accept(pvcSABRHWMC, SABR_BUNDLE);
    }
    endTime = System.currentTimeMillis();
    System.out.println(nbTest2 + " physical swaptions SABR + Hull-White Monte Carlo: " + (endTime - startTime) + " ms");
    // Performance note: price: 12-Jun-12: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 530 ms for 10 swaptions.

  }

  @Test(enabled = false)
  /**
   * Test of relative performance of constructor, toDerivative and pricing. In normal testing, "enabled = false".
   */
  public void constructorPerformance() {
    final YieldCurveBundle curves = TestsDataSetsSABR.createCurves1();
    final SABRInterestRateParameters sabrParameter = TestsDataSetsSABR.createSABR1();
    final SABRInterestRateDataBundle sabrBundle = new SABRInterestRateDataBundle(sabrParameter, curves);
    SwapFixedIborDefinition swap = SwapFixedIborDefinition.from(SETTLEMENT_DATE, CMS_INDEX, NOTIONAL, RATE, FIXED_IS_PAYER, CALENDAR);
    SwaptionPhysicalFixedIborDefinition swaptionDefinition = SwaptionPhysicalFixedIborDefinition.from(EXPIRY_DATE, swap, FIXED_IS_PAYER, IS_LONG);
    SwaptionPhysicalFixedIbor swaption = swaptionDefinition.toDerivative(REFERENCE_DATE, CURVES_NAME);
    long startTime, endTime;
    final int nbTest = 1000;
    startTime = System.currentTimeMillis();
    for (int looptest = 0; looptest < nbTest; looptest++) {
      swap = SwapFixedIborDefinition.from(SETTLEMENT_DATE, CMS_INDEX, NOTIONAL, RATE, FIXED_IS_PAYER, CALENDAR);
      swaptionDefinition = SwaptionPhysicalFixedIborDefinition.from(EXPIRY_DATE, swap, IS_LONG);
    }
    endTime = System.currentTimeMillis();
    System.out.println(nbTest + " physical swaptions SABR (definition construction): " + (endTime - startTime) + " ms");
    startTime = System.currentTimeMillis();
    for (int looptest = 0; looptest < nbTest; looptest++) {
      swaption = swaptionDefinition.toDerivative(REFERENCE_DATE, CURVES_NAME);
    }
    endTime = System.currentTimeMillis();
    System.out.println(nbTest + " physical swaptions SABR (to derivatives): " + (endTime - startTime) + " ms");
    startTime = System.currentTimeMillis();
    for (int looptest = 0; looptest < nbTest; looptest++) {
      swaption.accept(PVC, sabrBundle);
      swaption.accept(PVCSC_SABR, sabrBundle);
      swaption.accept(PVSSC_SABR, sabrBundle);
    }
    endTime = System.currentTimeMillis();
    System.out.println(nbTest + " physical swaptions SABR (pv+delta+SABR vega): " + (endTime - startTime) + " ms");
    // Performance note: definition construction: 15-Jun-11: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 45 ms for 1000 swaptions.
    // Performance note: to derivatives: 15-Jun-11: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 40 ms for 1000 swaptions.
    // Performance note: pv: 15-Jun-11: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 45 ms for 1000 swaptions.
    // Performance note: pv+delta: 15-Jun-11: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 130 ms for 1000 swaptions.
    // Performance note: pv+delta+SABR vega: 15-Jun-11: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 175 ms for 1000 swaptions.
  }
}
