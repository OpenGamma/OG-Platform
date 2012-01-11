/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.swaption.method;

import static com.opengamma.financial.interestrate.TestUtils.assertSensitivityEquals;
import static org.testng.AssertJUnit.assertEquals;
import it.unimi.dsi.fastutil.doubles.DoubleAVLTreeSet;

import java.util.List;

import javax.time.calendar.Period;
import javax.time.calendar.ZonedDateTime;

import org.testng.annotations.Test;

import cern.jet.random.engine.MersenneTwister;

import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.instrument.index.IndexSwap;
import com.opengamma.financial.instrument.index.IborIndex;
import com.opengamma.financial.instrument.swap.SwapFixedIborDefinition;
import com.opengamma.financial.instrument.swaption.SwaptionPhysicalFixedIborDefinition;
import com.opengamma.financial.interestrate.CashFlowEquivalentCalculator;
import com.opengamma.financial.interestrate.FDCurveSensitivityCalculator;
import com.opengamma.financial.interestrate.InterestRateCurveSensitivity;
import com.opengamma.financial.interestrate.ParRateCalculator;
import com.opengamma.financial.interestrate.PresentValueCalculator;
import com.opengamma.financial.interestrate.TestsDataSetsSABR;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.interestrate.annuity.definition.AnnuityPaymentFixed;
import com.opengamma.financial.interestrate.method.SensitivityFiniteDifference;
import com.opengamma.financial.interestrate.payments.Coupon;
import com.opengamma.financial.interestrate.payments.CouponIbor;
import com.opengamma.financial.interestrate.swap.SwapFixedDiscountingMethod;
import com.opengamma.financial.interestrate.swap.definition.FixedCouponSwap;
import com.opengamma.financial.interestrate.swaption.derivative.SwaptionPhysicalFixedIbor;
import com.opengamma.financial.model.interestrate.HullWhiteOneFactorPiecewiseConstantInterestRateModel;
import com.opengamma.financial.model.interestrate.HullWhiteTestsDataSet;
import com.opengamma.financial.model.interestrate.definition.HullWhiteOneFactorPiecewiseConstantDataBundle;
import com.opengamma.financial.model.interestrate.definition.HullWhiteOneFactorPiecewiseConstantParameters;
import com.opengamma.financial.model.option.pricing.analytic.formula.BlackFunctionData;
import com.opengamma.financial.model.volatility.BlackImpliedVolatilityFormula;
import com.opengamma.financial.montecarlo.HullWhiteMonteCarloMethod;
import com.opengamma.financial.schedule.ScheduleCalculator;
import com.opengamma.math.random.NormalRandomNumberGenerator;
import com.opengamma.math.statistics.distribution.NormalDistribution;
import com.opengamma.math.statistics.distribution.ProbabilityDistribution;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Tests related to the pricing of physical delivery swaption in Hull-White one factor model.
 */
public class SwaptionPhysicalFixedIborHullWhiteMethodTest {
  // Swaption 5Yx5Y
  private static final Currency CUR = Currency.USD;
  private static final Calendar CALENDAR = new MondayToFridayCalendar("A");
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Modified Following");
  private static final boolean IS_EOM = true;
  private static final int SETTLEMENT_DAYS = 2;
  private static final Period IBOR_TENOR = Period.ofMonths(3);
  private static final DayCount IBOR_DAY_COUNT = DayCountFactory.INSTANCE.getDayCount("Actual/360");
  private static final IborIndex IBOR_INDEX = new IborIndex(CUR, IBOR_TENOR, SETTLEMENT_DAYS, CALENDAR, IBOR_DAY_COUNT, BUSINESS_DAY, IS_EOM);
  private static final int SWAP_TENOR_YEAR = 5;
  private static final Period SWAP_TENOR = Period.ofYears(SWAP_TENOR_YEAR);
  private static final Period FIXED_PAYMENT_PERIOD = Period.ofMonths(6);
  private static final DayCount FIXED_DAY_COUNT = DayCountFactory.INSTANCE.getDayCount("30/360");
  private static final IndexSwap CMS_INDEX = new IndexSwap(FIXED_PAYMENT_PERIOD, FIXED_DAY_COUNT, IBOR_INDEX, SWAP_TENOR);
  private static final ZonedDateTime EXPIRY_DATE = DateUtils.getUTCDate(2016, 7, 7);
  private static final ZonedDateTime SETTLEMENT_DATE = ScheduleCalculator.getAdjustedDate(EXPIRY_DATE, SETTLEMENT_DAYS, CALENDAR);
  private static final double NOTIONAL = 100000000; //100m
  private static final double RATE = 0.0325;
  private static final boolean FIXED_IS_PAYER = true;
  private static final SwapFixedIborDefinition SWAP_PAYER_DEFINITION = SwapFixedIborDefinition.from(SETTLEMENT_DATE, CMS_INDEX, NOTIONAL, RATE, FIXED_IS_PAYER);
  private static final SwapFixedIborDefinition SWAP_RECEIVER_DEFINITION = SwapFixedIborDefinition.from(SETTLEMENT_DATE, CMS_INDEX, NOTIONAL, RATE, !FIXED_IS_PAYER);
  private static final boolean IS_LONG = true;
  private static final SwaptionPhysicalFixedIborDefinition SWAPTION_PAYER_LONG_DEFINITION = SwaptionPhysicalFixedIborDefinition.from(EXPIRY_DATE, SWAP_PAYER_DEFINITION, IS_LONG);
  private static final SwaptionPhysicalFixedIborDefinition SWAPTION_RECEIVER_LONG_DEFINITION = SwaptionPhysicalFixedIborDefinition.from(EXPIRY_DATE, SWAP_RECEIVER_DEFINITION, IS_LONG);
  private static final SwaptionPhysicalFixedIborDefinition SWAPTION_PAYER_SHORT_DEFINITION = SwaptionPhysicalFixedIborDefinition.from(EXPIRY_DATE, SWAP_PAYER_DEFINITION, !IS_LONG);
  private static final SwaptionPhysicalFixedIborDefinition SWAPTION_RECEIVER_SHORT_DEFINITION = SwaptionPhysicalFixedIborDefinition.from(EXPIRY_DATE, SWAP_RECEIVER_DEFINITION, !IS_LONG);
  //to derivatives
  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2011, 7, 7);
  private static final String FUNDING_CURVE_NAME = "Funding";
  private static final String FORWARD_CURVE_NAME = "Forward";
  private static final String[] CURVES_NAME = {FUNDING_CURVE_NAME, FORWARD_CURVE_NAME};
  private static final YieldCurveBundle CURVES = TestsDataSetsSABR.createCurves1();
  private static final FixedCouponSwap<Coupon> SWAP_RECEIVER = SWAP_RECEIVER_DEFINITION.toDerivative(REFERENCE_DATE, CURVES_NAME);
  private static final SwaptionPhysicalFixedIbor SWAPTION_PAYER_LONG = SWAPTION_PAYER_LONG_DEFINITION.toDerivative(REFERENCE_DATE, CURVES_NAME);
  private static final SwaptionPhysicalFixedIbor SWAPTION_RECEIVER_LONG = SWAPTION_RECEIVER_LONG_DEFINITION.toDerivative(REFERENCE_DATE, CURVES_NAME);
  private static final SwaptionPhysicalFixedIbor SWAPTION_PAYER_SHORT = SWAPTION_PAYER_SHORT_DEFINITION.toDerivative(REFERENCE_DATE, CURVES_NAME);
  private static final SwaptionPhysicalFixedIbor SWAPTION_RECEIVER_SHORT = SWAPTION_RECEIVER_SHORT_DEFINITION.toDerivative(REFERENCE_DATE, CURVES_NAME);
  // Calculator
  private static final CashFlowEquivalentCalculator CFEC = CashFlowEquivalentCalculator.getInstance();
  private static final PresentValueCalculator PVC = PresentValueCalculator.getInstance();
  private static final SwaptionPhysicalFixedIborHullWhiteMethod METHOD_HW = new SwaptionPhysicalFixedIborHullWhiteMethod();
  private static final SwaptionPhysicalFixedIborHullWhiteNumericalIntegrationMethod METHOD_HW_INTEGRATION = new SwaptionPhysicalFixedIborHullWhiteNumericalIntegrationMethod();
  private static final SwaptionPhysicalFixedIborHullWhiteApproximationMethod METHOD_HW_APPROXIMATION = new SwaptionPhysicalFixedIborHullWhiteApproximationMethod();
  private static final int NB_PATH = 12500;
  private static final HullWhiteMonteCarloMethod METHOD_HW_MONTECARLO = new HullWhiteMonteCarloMethod(new NormalRandomNumberGenerator(0.0, 1.0), NB_PATH);
  private static final HullWhiteOneFactorPiecewiseConstantParameters PARAMETERS_HW = HullWhiteTestsDataSet.createHullWhiteParameters();
  private static final HullWhiteOneFactorPiecewiseConstantDataBundle BUNDLE_HW = new HullWhiteOneFactorPiecewiseConstantDataBundle(PARAMETERS_HW, CURVES);
  private static final HullWhiteOneFactorPiecewiseConstantInterestRateModel MODEL = new HullWhiteOneFactorPiecewiseConstantInterestRateModel();
  private static final ProbabilityDistribution<Double> NORMAL = new NormalDistribution(0, 1);

  @Test
  /**
   * Test the present value.
   */
  public void presentValueExplicit() {
    CurrencyAmount pv = METHOD_HW.presentValue(SWAPTION_PAYER_LONG, BUNDLE_HW);
    double timeToExpiry = SWAPTION_PAYER_LONG.getTimeToExpiry();
    AnnuityPaymentFixed cfe = CFEC.visitSwap(SWAPTION_PAYER_LONG.getUnderlyingSwap(), CURVES);
    int numberOfPayments = cfe.getNumberOfPayments();
    double alpha[] = new double[numberOfPayments];
    double disccf[] = new double[numberOfPayments];
    for (int loopcf = 0; loopcf < numberOfPayments; loopcf++) {
      alpha[loopcf] = MODEL.alpha(0.0, timeToExpiry, timeToExpiry, cfe.getNthPayment(loopcf).getPaymentTime(), PARAMETERS_HW);
      disccf[loopcf] = CURVES.getCurve(FUNDING_CURVE_NAME).getDiscountFactor(cfe.getNthPayment(loopcf).getPaymentTime()) * cfe.getNthPayment(loopcf).getAmount();
    }
    double kappa = MODEL.kappa(disccf, alpha);
    double pvExpected = 0.0;
    for (int loopcf = 0; loopcf < numberOfPayments; loopcf++) {
      pvExpected += disccf[loopcf] * NORMAL.getCDF(-kappa - alpha[loopcf]);
    }
    assertEquals("Swaption physical - Hull-White - present value", pvExpected, pv.getAmount(), 1E-2);
    CurrencyAmount pv2 = METHOD_HW.presentValue(SWAPTION_PAYER_LONG, cfe, BUNDLE_HW);
    assertEquals("Swaption physical - Hull-White - present value", pv, pv2);
  }

  @Test
  /**
   * Tests long/short parity.
   */
  public void longShortParityExplicit() {
    CurrencyAmount pvLong = METHOD_HW.presentValue(SWAPTION_PAYER_LONG, BUNDLE_HW);
    CurrencyAmount pvShort = METHOD_HW.presentValue(SWAPTION_PAYER_SHORT, BUNDLE_HW);
    assertEquals("Swaption physical - Hull-White - present value - long/short parity", pvLong.getAmount(), -pvShort.getAmount(), 1E-2);
  }

  @Test
  /**
   * Tests payer/receiver/swap parity.
   */
  public void payerReceiverParityExplicit() {
    CurrencyAmount pvReceiverLong = METHOD_HW.presentValue(SWAPTION_RECEIVER_LONG, BUNDLE_HW);
    CurrencyAmount pvPayerShort = METHOD_HW.presentValue(SWAPTION_PAYER_SHORT, BUNDLE_HW);
    double pvSwap = PVC.visit(SWAP_RECEIVER, CURVES);
    assertEquals("Swaption physical - Hull-White - present value - payer/receiver/swap parity", pvReceiverLong.getAmount() + pvPayerShort.getAmount(), pvSwap, 1E-2);
  }

  @Test
  /**
   * Compare explicit formula with numerical integration.
   */
  public void presentValueNumericalIntegration() {
    CurrencyAmount pvPayerLongExplicit = METHOD_HW.presentValue(SWAPTION_PAYER_LONG, BUNDLE_HW);
    CurrencyAmount pvPayerLongIntegration = METHOD_HW_INTEGRATION.presentValue(SWAPTION_PAYER_LONG, BUNDLE_HW);
    assertEquals("Swaption physical - Hull-White - present value - explicit/numerical integration", pvPayerLongExplicit.getAmount(), pvPayerLongIntegration.getAmount(), 1.0E-0);
    CurrencyAmount pvPayerShortExplicit = METHOD_HW.presentValue(SWAPTION_PAYER_SHORT, BUNDLE_HW);
    CurrencyAmount pvPayerShortIntegration = METHOD_HW_INTEGRATION.presentValue(SWAPTION_PAYER_SHORT, BUNDLE_HW);
    assertEquals("Swaption physical - Hull-White - present value - explicit/numerical integration", pvPayerShortExplicit.getAmount(), pvPayerShortIntegration.getAmount(), 1.0E-0);
    CurrencyAmount pvReceiverLongExplicit = METHOD_HW.presentValue(SWAPTION_RECEIVER_LONG, BUNDLE_HW);
    CurrencyAmount pvReceiverLongIntegration = METHOD_HW_INTEGRATION.presentValue(SWAPTION_RECEIVER_LONG, BUNDLE_HW);
    assertEquals("Swaption physical - Hull-White - present value - explicit/numerical integration", pvReceiverLongExplicit.getAmount(), pvReceiverLongIntegration.getAmount(), 1.0E-0);
    CurrencyAmount pvReceiverShortExplicit = METHOD_HW.presentValue(SWAPTION_RECEIVER_SHORT, BUNDLE_HW);
    CurrencyAmount pvReceiverShortIntegration = METHOD_HW_INTEGRATION.presentValue(SWAPTION_RECEIVER_SHORT, BUNDLE_HW);
    assertEquals("Swaption physical - Hull-White - present value - explicit/numerical integration", pvReceiverShortExplicit.getAmount(), pvReceiverShortIntegration.getAmount(), 1.0E-0);
  }

  @Test
  /**
   * Compare explicit formula with approximated formula.
   */
  public void presentValueApproximation() {
    BlackImpliedVolatilityFormula implied = new BlackImpliedVolatilityFormula();
    double forward = ParRateCalculator.getInstance().visit(SWAPTION_PAYER_LONG.getUnderlyingSwap(), CURVES);
    double pvbp = SwapFixedDiscountingMethod.presentValueBasisPoint(SWAPTION_PAYER_LONG.getUnderlyingSwap(), CURVES);
    CurrencyAmount pvPayerLongExplicit = METHOD_HW.presentValue(SWAPTION_PAYER_LONG, BUNDLE_HW);
    CurrencyAmount pvPayerLongApproximation = METHOD_HW_APPROXIMATION.presentValue(SWAPTION_PAYER_LONG, BUNDLE_HW);
    BlackFunctionData data = new BlackFunctionData(forward, pvbp, 0.20);
    double volExplicit = implied.getImpliedVolatility(data, SWAPTION_PAYER_LONG, pvPayerLongExplicit.getAmount());
    double volApprox = implied.getImpliedVolatility(data, SWAPTION_PAYER_LONG, pvPayerLongApproximation.getAmount());
    assertEquals("Swaption physical - Hull-White - present value - explicit/approximation", pvPayerLongExplicit.getAmount(), pvPayerLongApproximation.getAmount(), 5.0E+2);
    assertEquals("Swaption physical - Hull-White - present value - explicit/approximation", volExplicit, volApprox, 2.5E-4); // 0.025%
    CurrencyAmount pvReceiverLongExplicit = METHOD_HW.presentValue(SWAPTION_RECEIVER_LONG, BUNDLE_HW);
    CurrencyAmount pvReceiverLongApproximation = METHOD_HW_APPROXIMATION.presentValue(SWAPTION_RECEIVER_LONG, BUNDLE_HW);
    assertEquals("Swaption physical - Hull-White - present value - explicit/numerical integration", pvReceiverLongExplicit.getAmount(), pvReceiverLongApproximation.getAmount(), 5.0E+2);
  }

  @Test
  /**
   * Approximation analysis.
   */
  public void presentValueApproximationAnalysis() {
    BlackImpliedVolatilityFormula implied = new BlackImpliedVolatilityFormula();
    int nbStrike = 20;
    double[] pvExplicit = new double[nbStrike + 1];
    double[] pvApproximation = new double[nbStrike + 1];
    double[] strike = new double[nbStrike + 1];
    double[] volExplicit = new double[nbStrike + 1];
    double[] volApprox = new double[nbStrike + 1];
    double strikeRange = 0.035;
    FixedCouponSwap<Coupon> swap = SWAP_PAYER_DEFINITION.toDerivative(REFERENCE_DATE, CURVES_NAME);
    double forward = ParRateCalculator.getInstance().visit(swap, CURVES);
    double pvbp = SwapFixedDiscountingMethod.presentValueBasisPoint(swap, CURVES);
    for (int loopstrike = 0; loopstrike <= nbStrike; loopstrike++) {
      strike[loopstrike] = forward - strikeRange + 3 * strikeRange * loopstrike / nbStrike; // From forward-strikeRange to forward+2*strikeRange
      SwapFixedIborDefinition swapDefinition = SwapFixedIborDefinition.from(SETTLEMENT_DATE, CMS_INDEX, NOTIONAL, strike[loopstrike], FIXED_IS_PAYER);
      SwaptionPhysicalFixedIborDefinition swaptionDefinition = SwaptionPhysicalFixedIborDefinition.from(EXPIRY_DATE, swapDefinition, IS_LONG);
      SwaptionPhysicalFixedIbor swaption = swaptionDefinition.toDerivative(REFERENCE_DATE, CURVES_NAME);
      pvExplicit[loopstrike] = METHOD_HW.presentValue(swaption, BUNDLE_HW).getAmount();
      pvApproximation[loopstrike] = METHOD_HW_APPROXIMATION.presentValue(swaption, BUNDLE_HW).getAmount();
      BlackFunctionData data = new BlackFunctionData(forward, pvbp, 0.20);
      volExplicit[loopstrike] = implied.getImpliedVolatility(data, swaption, pvExplicit[loopstrike]);
      volApprox[loopstrike] = implied.getImpliedVolatility(data, swaption, pvApproximation[loopstrike]);
      assertEquals("Swaption physical - Hull-White - implied volatility - explicit/approximation", volExplicit[loopstrike], volApprox[loopstrike], 0.1E-2); // 0.10%
    }
  }

  @Test(enabled = true)
  /**
   * Compare explicit formula with Monte-Carlo and long/short and payer/receiver parities.
   */
  public void presentValueMonteCarlo() {
    int nbPath = 12500;
    HullWhiteMonteCarloMethod methodMC;
    methodMC = new HullWhiteMonteCarloMethod(new NormalRandomNumberGenerator(0.0, 1.0, new MersenneTwister()), nbPath);
    // Seed fixed to the DEFAULT_SEED for testing purposes.
    CurrencyAmount pvPayerLongExplicit = METHOD_HW.presentValue(SWAPTION_PAYER_LONG, BUNDLE_HW);
    CurrencyAmount pvPayerLongMC = methodMC.presentValue(SWAPTION_PAYER_LONG, CUR, FUNDING_CURVE_NAME, BUNDLE_HW);
    assertEquals("Swaption physical - Hull-White - Monte Carlo", pvPayerLongExplicit.getAmount(), pvPayerLongMC.getAmount(), 1.0E+4);
    double pvMCPreviousRun = 5137844.655;
    assertEquals("Swaption physical - Hull-White - Monte Carlo", pvMCPreviousRun, pvPayerLongMC.getAmount(), 1.0E-2);
    methodMC = new HullWhiteMonteCarloMethod(new NormalRandomNumberGenerator(0.0, 1.0, new MersenneTwister()), nbPath);
    CurrencyAmount pvPayerShortMC = methodMC.presentValue(SWAPTION_PAYER_SHORT, CUR, FUNDING_CURVE_NAME, BUNDLE_HW);
    assertEquals("Swaption physical - Hull-White - Monte Carlo", -pvPayerLongMC.getAmount(), pvPayerShortMC.getAmount(), 1.0E-2);
    CurrencyAmount pvReceiverLongMC = methodMC.presentValue(SWAPTION_RECEIVER_LONG, CUR, FUNDING_CURVE_NAME, BUNDLE_HW);
    double pvSwap = PVC.visit(SWAP_RECEIVER, CURVES);
    assertEquals("Swaption physical - Hull-White - Monte Carlo - payer/receiver/swap parity", pvReceiverLongMC.getAmount() + pvPayerShortMC.getAmount(), pvSwap, 1.0E+5);
  }

  @Test
  /**
   * Tests the Hull-White parameters sensitivity for the explicit formula.
   */
  public void presentValueHullWhiteSensitivityExplicit() {
    double[] hwSensitivity = METHOD_HW.presentValueHullWhiteSensitivity(SWAPTION_PAYER_LONG, BUNDLE_HW);
    int nbVolatility = PARAMETERS_HW.getVolatility().length;
    double shiftVol = 1.0E-6;
    double[] volatilityBumped = new double[nbVolatility];
    System.arraycopy(PARAMETERS_HW.getVolatility(), 0, volatilityBumped, 0, nbVolatility);
    double[] volatilityTime = new double[nbVolatility - 1];
    System.arraycopy(PARAMETERS_HW.getVolatilityTime(), 1, volatilityTime, 0, nbVolatility - 1);
    double[] pvBumpedPlus = new double[nbVolatility];
    double[] pvBumpedMinus = new double[nbVolatility];
    HullWhiteOneFactorPiecewiseConstantParameters parametersBumped = new HullWhiteOneFactorPiecewiseConstantParameters(PARAMETERS_HW.getMeanReversion(), volatilityBumped, volatilityTime);
    HullWhiteOneFactorPiecewiseConstantDataBundle bundleBumped = new HullWhiteOneFactorPiecewiseConstantDataBundle(parametersBumped, CURVES);
    for (int loopvol = 0; loopvol < nbVolatility; loopvol++) {
      volatilityBumped[loopvol] += shiftVol;
      parametersBumped.setVolatility(volatilityBumped);
      pvBumpedPlus[loopvol] = METHOD_HW.presentValue(SWAPTION_PAYER_LONG, bundleBumped).getAmount();
      volatilityBumped[loopvol] -= 2 * shiftVol;
      parametersBumped.setVolatility(volatilityBumped);
      pvBumpedMinus[loopvol] = METHOD_HW.presentValue(SWAPTION_PAYER_LONG, bundleBumped).getAmount();
      assertEquals(
          "Swaption - Hull-White sensitivity adjoint: derivative " + loopvol + " - difference:" + ((pvBumpedPlus[loopvol] - pvBumpedMinus[loopvol]) / (2 * shiftVol) - hwSensitivity[loopvol]),
          (pvBumpedPlus[loopvol] - pvBumpedMinus[loopvol]) / (2 * shiftVol), hwSensitivity[loopvol], 1.0E-0);
      volatilityBumped[loopvol] = PARAMETERS_HW.getVolatility()[loopvol];
    }
  }

  @Test
  /**
   * Tests the curve sensitivity for the explicit formula.
   */
  public void presentValueCurveSensitivity() {
    InterestRateCurveSensitivity pvsSwaption = METHOD_HW.presentValueCurveSensitivity(SWAPTION_PAYER_LONG, BUNDLE_HW);
    pvsSwaption = pvsSwaption.clean();
    final double deltaTolerancePrice = 1.0E+0;
    //Testing note: Sensitivity is for a movement of 1. 1E+2 = 1 cent for a 1 bp move. Tolerance increased to cope with numerical imprecision of finite difference.
    final double deltaShift = 1.0E-6;
    // 1. Forward curve sensitivity
    final String bumpedCurveName = "Bumped Curve";
    final SwaptionPhysicalFixedIbor swptBumpedForward = SWAPTION_PAYER_LONG_DEFINITION.toDerivative(REFERENCE_DATE, new String[] {CURVES_NAME[0], bumpedCurveName});
    DoubleAVLTreeSet forwardTime = new DoubleAVLTreeSet();
    for (int loopcpn = 0; loopcpn < SWAPTION_PAYER_LONG.getUnderlyingSwap().getSecondLeg().getNumberOfPayments(); loopcpn++) {
      CouponIbor cpn = (CouponIbor) SWAPTION_PAYER_LONG.getUnderlyingSwap().getSecondLeg().getNthPayment(loopcpn);
      forwardTime.add(cpn.getFixingPeriodStartTime());
      forwardTime.add(cpn.getFixingPeriodEndTime());
    }
    double[] nodeTimesForward = forwardTime.toDoubleArray();
    final double[] sensiForwardMethod = SensitivityFiniteDifference.curveSensitivity(swptBumpedForward, BUNDLE_HW, CURVES_NAME[1], bumpedCurveName, nodeTimesForward, deltaShift, METHOD_HW);
    //    assertEquals("Sensitivity finite difference method: number of node", 2, sensiForwardMethod.length);
    final List<DoublesPair> sensiPvForward = pvsSwaption.getSensitivities().get(CURVES_NAME[1]);
    for (int loopnode = 0; loopnode < sensiForwardMethod.length; loopnode++) {
      final DoublesPair pairPv = sensiPvForward.get(loopnode);
      assertEquals("Sensitivity swaption pv to forward curve: Node " + loopnode, nodeTimesForward[loopnode], pairPv.getFirst(), 1E-8);
      assertEquals("Sensitivity finite difference method: node sensitivity " + loopnode, pairPv.second, sensiForwardMethod[loopnode], deltaTolerancePrice);
    }
    // 2. Discounting curve sensitivity
    final SwaptionPhysicalFixedIbor swptBumpedDisc = SWAPTION_PAYER_LONG_DEFINITION.toDerivative(REFERENCE_DATE, new String[] {bumpedCurveName, CURVES_NAME[1]});
    DoubleAVLTreeSet discTime = new DoubleAVLTreeSet();
    for (int loopcpn = 0; loopcpn < SWAPTION_PAYER_LONG.getUnderlyingSwap().getSecondLeg().getNumberOfPayments(); loopcpn++) {
      CouponIbor cpn = (CouponIbor) SWAPTION_PAYER_LONG.getUnderlyingSwap().getSecondLeg().getNthPayment(loopcpn);
      discTime.add(cpn.getPaymentTime());
    }
    double[] nodeTimesDisc = discTime.toDoubleArray();
    final double[] sensiDiscMethod = SensitivityFiniteDifference.curveSensitivity(swptBumpedDisc, BUNDLE_HW, CURVES_NAME[0], bumpedCurveName, nodeTimesDisc, deltaShift, METHOD_HW);
    assertEquals("Sensitivity finite difference method: number of node", SWAP_TENOR_YEAR * 4, sensiDiscMethod.length);
    final List<DoublesPair> sensiPvDisc = pvsSwaption.getSensitivities().get(CURVES_NAME[0]);
    List<DoublesPair> fdSense = FDCurveSensitivityCalculator.curveSensitvityFDCalculator(SWAPTION_PAYER_LONG, METHOD_HW, BUNDLE_HW, CURVES_NAME[0], nodeTimesDisc, 1e-8);

    assertSensitivityEquals(sensiPvDisc, fdSense, deltaTolerancePrice);
  }

  @Test
  /**
   * Tests the curve sensitivity in Monte Carlo approach.
   */
  public void presentValueCurveSensitivityMonteCarlo() {
    double toleranceDelta = 1.0E+6; // 100 USD by bp
    InterestRateCurveSensitivity pvcsExplicit = METHOD_HW.presentValueCurveSensitivity(SWAPTION_PAYER_LONG, BUNDLE_HW);
    int nbPath = 30000; // 10000 path -> 200 USD by bp
    HullWhiteMonteCarloMethod methodMC = new HullWhiteMonteCarloMethod(new NormalRandomNumberGenerator(0.0, 1.0, new MersenneTwister()), nbPath);
    InterestRateCurveSensitivity pvcsMC = methodMC.presentValueCurveSensitivity(SWAPTION_PAYER_LONG, FUNDING_CURVE_NAME, BUNDLE_HW);
    pvcsMC = pvcsMC.clean();
    InterestRateCurveSensitivity diff = pvcsExplicit.clean().add(pvcsMC.multiply(-1)).clean();
    final List<DoublesPair> sensiDsc = diff.getSensitivities().get(FUNDING_CURVE_NAME);
    int nbDsc = sensiDsc.size();
    for (int loopdsc = 0; loopdsc < nbDsc; loopdsc++) {
      assertEquals("Sensitivity MC method: node sensitivity (node: " + loopdsc + ")", 0.0, sensiDsc.get(loopdsc).second, toleranceDelta);
    }
    final List<DoublesPair> sensiFwd = diff.getSensitivities().get(FORWARD_CURVE_NAME);
    int nbFwd = sensiFwd.size();
    for (int loopfwd = 0; loopfwd < nbFwd; loopfwd++) {
      assertEquals("Sensitivity MC method: node sensitivity (node: " + loopfwd + ")", 0.0, sensiFwd.get(loopfwd).second, toleranceDelta);
    }

    // From previous run 
    final List<DoublesPair> sensiDscMC = pvcsMC.getSensitivities().get(FUNDING_CURVE_NAME);
    final List<DoublesPair> sensiFwdMC = pvcsMC.getSensitivities().get(FORWARD_CURVE_NAME);
    double[] sensiDscExpected = new double[] {0.0000, 0.0000, -3637714.1984, 557942.4840, -3787541.0789, 613898.2842, -4080860.4679, 589073.7077, -4178299.1839, 652156.5042, -4447809.9953,
        617234.4471, -4506992.0525, 678479.4058, -4754223.0800, 591911.3273, -4821219.8085, 708193.7945, -4922426.5897, 664008.0977, -5056657.1907, 734532.5709};
    double[] sensiFwdExpected = new double[] {-248654368.3630, 4284328.0810, 4407573.3981, 4466697.9207, 4639237.7090, 4802919.1013, 4902043.1034, 4924368.2503, 5082799.3958, 5231525.2847,
        5310677.1705, 5308454.0898, 5453701.6063, 5606360.6795, 5762074.8382, 5665693.0081, 9427619.7175, -343032977.1261, 348874771.7538, -350204210.4835, 352341391.2901, 5938939.1529,
        362465824.7561};
    for (int loopdsc = 0; loopdsc < nbDsc; loopdsc++) {
      assertEquals("Sensitivity MC method: node sensitivity (node: " + loopdsc + ")", sensiDscExpected[loopdsc], sensiDscMC.get(loopdsc).second, 1.0E-2);
    }
    for (int loopfwd = 0; loopfwd < nbFwd; loopfwd++) {
      assertEquals("Sensitivity MC method: node sensitivity (node: " + loopfwd + ")", sensiFwdExpected[loopfwd], sensiFwdMC.get(loopfwd).second, 1.0E-2);
    }
  }

  @Test(enabled = false)
  /**
   * Tests of performance. "enabled = false" for the standard testing.
   */
  public void performance() {
    long startTime, endTime;
    final int nbTest = 100;
    CurrencyAmount pvPayerLongExplicit = CurrencyAmount.of(CUR, 0.0);
    CurrencyAmount pvPayerLongIntegration = CurrencyAmount.of(CUR, 0.0);
    CurrencyAmount pvPayerLongApproximation = CurrencyAmount.of(CUR, 0.0);
    CurrencyAmount pvPayerLongMC = CurrencyAmount.of(CUR, 0.0);
    double[] pvhws = METHOD_HW.presentValueHullWhiteSensitivity(SWAPTION_PAYER_LONG, BUNDLE_HW);
    InterestRateCurveSensitivity pvcs = METHOD_HW.presentValueCurveSensitivity(SWAPTION_PAYER_LONG, BUNDLE_HW);
    //    YieldAndDiscountCurve curve = CURVES.getCurve(FUNDING_CURVE_NAME);
    startTime = System.currentTimeMillis();
    for (int looptest = 0; looptest < nbTest; looptest++) {
      pvPayerLongExplicit = METHOD_HW.presentValue(SWAPTION_PAYER_LONG, BUNDLE_HW);
    }
    endTime = System.currentTimeMillis();
    System.out.println(nbTest + " pv swaption Hull-White explicit method: " + (endTime - startTime) + " ms");
    // Performance note: HW price: 19-Jul-11: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 330 ms for 10000 swaptions.
    startTime = System.currentTimeMillis();
    for (int looptest = 0; looptest < nbTest; looptest++) {
      pvhws = METHOD_HW.presentValueHullWhiteSensitivity(SWAPTION_PAYER_LONG, BUNDLE_HW);
    }
    endTime = System.currentTimeMillis();
    System.out.println(nbTest + " HW sensitivity swaption Hull-White explicit method: " + (endTime - startTime) + " ms");
    // Performance note: HW sensitivity (3): 19-Jul-11: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 415 ms for 10000 swaptions.
    startTime = System.currentTimeMillis();
    for (int looptest = 0; looptest < nbTest; looptest++) {
      pvcs = METHOD_HW.presentValueCurveSensitivity(SWAPTION_PAYER_LONG, BUNDLE_HW);
    }
    endTime = System.currentTimeMillis();
    System.out.println(nbTest + " curve sensitivity swaption Hull-White explicit method: " + (endTime - startTime) + " ms");
    // Performance note: curve sensitivity (40): 19-Jul-11: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 890 ms for 10000 swaptions.
    startTime = System.currentTimeMillis();
    for (int looptest = 0; looptest < nbTest; looptest++) {
      pvhws = METHOD_HW.presentValueHullWhiteSensitivity(SWAPTION_PAYER_LONG, BUNDLE_HW);
      pvcs = METHOD_HW.presentValueCurveSensitivity(SWAPTION_PAYER_LONG, BUNDLE_HW);
      pvhws = METHOD_HW.presentValueHullWhiteSensitivity(SWAPTION_PAYER_LONG, BUNDLE_HW);
    }
    endTime = System.currentTimeMillis();
    System.out.println(nbTest + " price/delta/vega swaption Hull-White explicit method: " + (endTime - startTime) + " ms");
    // Performance note: present value/delta/vega: 19-Jul-11: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 1300 ms for 10000 swaptions.
    startTime = System.currentTimeMillis();
    for (int looptest = 0; looptest < nbTest; looptest++) {
      pvPayerLongIntegration = METHOD_HW_INTEGRATION.presentValue(SWAPTION_PAYER_LONG, BUNDLE_HW);
    }
    endTime = System.currentTimeMillis();
    System.out.println(nbTest + " swaption Hull-White numerical integration method: " + (endTime - startTime) + " ms");
    // Performance note: HW numerical integration: 19-Jul-11: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 1600 ms for 10000 swaptions.
    startTime = System.currentTimeMillis();
    for (int looptest = 0; looptest < nbTest; looptest++) {
      pvPayerLongApproximation = METHOD_HW_APPROXIMATION.presentValue(SWAPTION_PAYER_LONG, BUNDLE_HW);
    }
    endTime = System.currentTimeMillis();
    System.out.println(nbTest + " swaption Hull-White approximation method: " + (endTime - startTime) + " ms");
    // Performance note: HW approximation: 18-Aug-11: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 160 ms for 10000 swaptions.
    startTime = System.currentTimeMillis();
    for (int looptest = 0; looptest < nbTest; looptest++) {
      pvPayerLongMC = METHOD_HW_MONTECARLO.presentValue(SWAPTION_PAYER_LONG, CUR, FUNDING_CURVE_NAME, BUNDLE_HW);
    }
    endTime = System.currentTimeMillis();
    System.out.println(nbTest + " swaption Hull-White Monte Carlo method (" + NB_PATH + " paths): " + (endTime - startTime) + " ms");
    // Performance note: HW approximation: 18-Aug-11: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 9200 ms for 1000 swaptions (12500 paths).

    double difference = pvPayerLongExplicit.getAmount() - pvPayerLongIntegration.getAmount();
    double difference2 = pvPayerLongExplicit.getAmount() - pvPayerLongApproximation.getAmount();
    double difference3 = pvPayerLongExplicit.getAmount() - pvPayerLongMC.getAmount();
    System.out.println("Difference explicit-integration: " + difference);
    System.out.println("Difference explicit-approximation: " + difference2);
    System.out.println("Difference explicit-Monte Carlo: " + difference3);
    System.out.println("Curve sensitivity: " + pvcs.toString());
    System.out.println("HW sensitivity: " + pvhws.toString());
  }

  @Test(enabled = false)
  /**
   * Tests of performance. "enabled = false" for the standard testing.
   */
  public void performanceCurveSensitivity() {
    long startTime, endTime;
    final int nbTest = 25;
    CurrencyAmount pvMC = CurrencyAmount.of(CUR, 0.0);
    InterestRateCurveSensitivity pvcsExplicit = METHOD_HW.presentValueCurveSensitivity(SWAPTION_PAYER_LONG, BUNDLE_HW);
    InterestRateCurveSensitivity pvcsMC = pvcsExplicit;
    int nbPath = 12500;
    HullWhiteMonteCarloMethod methodMC = new HullWhiteMonteCarloMethod(new NormalRandomNumberGenerator(0.0, 1.0, new MersenneTwister()), nbPath);

    startTime = System.currentTimeMillis();
    for (int looptest = 0; looptest < nbTest; looptest++) {
      pvMC = METHOD_HW_MONTECARLO.presentValue(SWAPTION_PAYER_LONG, CUR, FUNDING_CURVE_NAME, BUNDLE_HW);
    }
    endTime = System.currentTimeMillis();
    System.out.println(nbTest + " swaption Hull-White Monte Carlo method (" + NB_PATH + " paths): " + (endTime - startTime) + " ms / price:" + pvMC.toString());
    // Performance note: HW approximation: 14-Oct-11: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 240 ms for 25 swaptions (12500 paths).
    startTime = System.currentTimeMillis();
    for (int looptest = 0; looptest < nbTest; looptest++) {
      pvcsMC = methodMC.presentValueCurveSensitivity(SWAPTION_PAYER_LONG, FUNDING_CURVE_NAME, BUNDLE_HW);
    }
    endTime = System.currentTimeMillis();
    System.out.println(nbTest + " curve sensitivity swaption Hull-White MC method: (" + nbPath + " paths) " + (endTime - startTime) + " ms / risk:" + pvcsMC.toString());
    // Performance note: curve sensitivity (40): 12-Oct-11: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 765 ms for 25 swaptions (12500 paths).
  }

}
