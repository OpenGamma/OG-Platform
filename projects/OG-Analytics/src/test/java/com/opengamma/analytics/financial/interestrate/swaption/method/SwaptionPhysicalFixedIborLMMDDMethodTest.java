/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.swaption.method;

import static org.testng.AssertJUnit.assertEquals;
import it.unimi.dsi.fastutil.doubles.DoubleAVLTreeSet;

import java.util.List;

import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import cern.colt.Arrays;
import cern.jet.random.engine.MersenneTwister;

import com.opengamma.analytics.financial.instrument.annuity.AnnuityCouponFixedDefinition;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityCouponIborDefinition;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityDefinition;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIbor;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIborMaster;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexSwap;
import com.opengamma.analytics.financial.instrument.payment.CouponFixedDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborDefinition;
import com.opengamma.analytics.financial.instrument.payment.PaymentDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedIborDefinition;
import com.opengamma.analytics.financial.instrument.swaption.SwaptionPhysicalFixedIborDefinition;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InterestRateCurveSensitivity;
import com.opengamma.analytics.financial.interestrate.ParRateCalculator;
import com.opengamma.analytics.financial.interestrate.PresentValueCalculator;
import com.opengamma.analytics.financial.interestrate.PresentValueSABRSensitivityDataBundle;
import com.opengamma.analytics.financial.interestrate.TestsDataSetsSABR;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.Annuity;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.AnnuityCouponFixed;
import com.opengamma.analytics.financial.interestrate.method.SensitivityFiniteDifference;
import com.opengamma.analytics.financial.interestrate.method.SuccessiveRootFinderCalibrationEngine;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Coupon;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixed;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIbor;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.analytics.financial.interestrate.swap.derivative.SwapFixedCoupon;
import com.opengamma.analytics.financial.interestrate.swap.method.SwapFixedCouponDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionPhysicalFixedIbor;
import com.opengamma.analytics.financial.model.interestrate.TestsDataSetLiborMarketModelDisplacedDiffusion;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.financial.model.interestrate.definition.LiborMarketModelDisplacedDiffusionDataBundle;
import com.opengamma.analytics.financial.model.interestrate.definition.LiborMarketModelDisplacedDiffusionParameters;
import com.opengamma.analytics.financial.model.option.definition.SABRInterestRateDataBundle;
import com.opengamma.analytics.financial.model.option.definition.SABRInterestRateParameters;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.BlackFunctionData;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.analytics.financial.model.volatility.BlackImpliedVolatilityFormula;
import com.opengamma.analytics.financial.montecarlo.LiborMarketModelMonteCarloMethod;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.math.curve.ConstantDoublesCurve;
import com.opengamma.analytics.math.random.NormalRandomNumberGenerator;
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
 * Tests related to the pricing of physical delivery swaption in LMM displaced diffusion.
 * @deprecated This class tests deprecated functionality.
 */
@Deprecated
@Test(groups = TestGroup.UNIT)
public class SwaptionPhysicalFixedIborLMMDDMethodTest {

  private static final Calendar TARGET = new MondayToFridayCalendar("TARGET");
  private static final GeneratorSwapFixedIbor EUR1YEURIBOR6M = GeneratorSwapFixedIborMaster.getInstance().getGenerator("EUR1YEURIBOR6M", TARGET);
  private static final IborIndex EURIBOR6M = EUR1YEURIBOR6M.getIborIndex();

  // Swaption 5Yx5Y
  private static final Currency CUR = Currency.EUR;
  private static final Calendar CALENDAR = new MondayToFridayCalendar("A");
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventions.MODIFIED_FOLLOWING;
  private static final boolean IS_EOM = true;
  private static final int SETTLEMENT_DAYS = 2;
  private static final Period IBOR_TENOR = Period.ofMonths(3);
  private static final DayCount IBOR_DAY_COUNT = DayCounts.ACT_360;
  private static final IborIndex IBOR_INDEX = new IborIndex(CUR, IBOR_TENOR, SETTLEMENT_DAYS, IBOR_DAY_COUNT, BUSINESS_DAY, IS_EOM);
  private static final int SWAP_TENOR_YEAR = 5;
  private static final Period SWAP_TENOR = Period.ofYears(SWAP_TENOR_YEAR);
  private static final Period FIXED_PAYMENT_PERIOD = Period.ofMonths(6);
  private static final DayCount FIXED_DAY_COUNT = DayCounts.THIRTY_U_360;
  private static final IndexSwap CMS_INDEX = new IndexSwap(FIXED_PAYMENT_PERIOD, FIXED_DAY_COUNT, IBOR_INDEX, SWAP_TENOR, CALENDAR);
  private static final ZonedDateTime EXPIRY_DATE = DateUtils.getUTCDate(2016, 7, 7);
  private static final ZonedDateTime SETTLEMENT_DATE = ScheduleCalculator.getAdjustedDate(EXPIRY_DATE, SETTLEMENT_DAYS, CALENDAR);
  private static final double NOTIONAL = 100000000; //100m
  private static final double RATE = 0.0325;
  private static final boolean FIXED_IS_PAYER = true;
  private static final SwapFixedIborDefinition SWAP_PAYER_DEFINITION = SwapFixedIborDefinition.from(SETTLEMENT_DATE, CMS_INDEX, NOTIONAL, RATE, FIXED_IS_PAYER, CALENDAR);
  private static final SwapFixedIborDefinition SWAP_RECEIVER_DEFINITION = SwapFixedIborDefinition.from(SETTLEMENT_DATE, CMS_INDEX, NOTIONAL, RATE, !FIXED_IS_PAYER, CALENDAR);
  private static final boolean IS_LONG = true;
  private static final SwaptionPhysicalFixedIborDefinition SWAPTION_PAYER_LONG_DEFINITION = SwaptionPhysicalFixedIborDefinition.from(EXPIRY_DATE, SWAP_PAYER_DEFINITION, true, IS_LONG);
  private static final SwaptionPhysicalFixedIborDefinition SWAPTION_RECEIVER_LONG_DEFINITION = SwaptionPhysicalFixedIborDefinition.from(EXPIRY_DATE, SWAP_RECEIVER_DEFINITION, false, IS_LONG);
  private static final SwaptionPhysicalFixedIborDefinition SWAPTION_PAYER_SHORT_DEFINITION = SwaptionPhysicalFixedIborDefinition.from(EXPIRY_DATE, SWAP_PAYER_DEFINITION, true, !IS_LONG);
  //to derivatives
  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2011, 7, 7);
  private static final String FUNDING_CURVE_NAME = "Funding";
  private static final String FORWARD_CURVE_NAME = "Forward";
  private static final String[] CURVES_NAME = {FUNDING_CURVE_NAME, FORWARD_CURVE_NAME };
  private static final YieldCurveBundle CURVES = TestsDataSetsSABR.createCurves1();

  private static final SwapFixedCoupon<Coupon> SWAP_RECEIVER = SWAP_RECEIVER_DEFINITION.toDerivative(REFERENCE_DATE, CURVES_NAME);
  private static final SwaptionPhysicalFixedIbor SWAPTION_PAYER_LONG = SWAPTION_PAYER_LONG_DEFINITION.toDerivative(REFERENCE_DATE, CURVES_NAME);
  private static final SwaptionPhysicalFixedIbor SWAPTION_RECEIVER_LONG = SWAPTION_RECEIVER_LONG_DEFINITION.toDerivative(REFERENCE_DATE, CURVES_NAME);
  private static final SwaptionPhysicalFixedIbor SWAPTION_PAYER_SHORT = SWAPTION_PAYER_SHORT_DEFINITION.toDerivative(REFERENCE_DATE, CURVES_NAME);
  // Parameters and methods
  private static final PresentValueCalculator PVC = PresentValueCalculator.getInstance();
  private static final ParRateCalculator PRC = ParRateCalculator.getInstance();
  private static final SwaptionPhysicalFixedIborLMMDDMethod METHOD_LMM = SwaptionPhysicalFixedIborLMMDDMethod.getInstance();
  private static final SwaptionPhysicalFixedIborSABRMethod METHOD_SABR = SwaptionPhysicalFixedIborSABRMethod.getInstance();
  private static final LiborMarketModelDisplacedDiffusionParameters PARAMETERS_LMM = TestsDataSetLiborMarketModelDisplacedDiffusion.createLMMParameters(REFERENCE_DATE,
      SWAP_PAYER_DEFINITION.getIborLeg());
  private static final LiborMarketModelDisplacedDiffusionDataBundle BUNDLE_LMM = new LiborMarketModelDisplacedDiffusionDataBundle(PARAMETERS_LMM, CURVES);

  private static final int NB_PATH = 12500;
  private static final LiborMarketModelMonteCarloMethod METHOD_LMM_MC = new LiborMarketModelMonteCarloMethod(new NormalRandomNumberGenerator(0.0, 1.0), NB_PATH);
  private static final SwapFixedCouponDiscountingMethod METHOD_SWAP = SwapFixedCouponDiscountingMethod.getInstance();
  private static final double[] MONEYNESS = new double[] {-0.0100, 0, 0.0100 };
  private static final SwaptionPhysicalFixedIborSABRLMMAtBestMethod METHOD_SABR_LMM_ATBEST = new SwaptionPhysicalFixedIborSABRLMMAtBestMethod(MONEYNESS, PARAMETERS_LMM);
  private static final SwaptionPhysicalFixedIborBasketMethod METHOD_BASKET = SwaptionPhysicalFixedIborBasketMethod.getInstance();

  @Test
  /**
   * Test the present value.
   */
  public void presentValue() {
    final double pvPreviousRun = 4367793.468; // Mean reversion 0.001: 4367793.468;
    final CurrencyAmount pv = METHOD_LMM.presentValue(SWAPTION_PAYER_LONG, BUNDLE_LMM);
    assertEquals("Swaption physical - LMM - present value", pvPreviousRun, pv.getAmount(), 1E-2);
  }

  @Test
  /**
   * Test the present value: approximated formula vs Monte Carlo.
   */
  public void presentValueMC() {
    final YieldAndDiscountCurve dsc = CURVES.getCurve(CURVES_NAME[0]);
    LiborMarketModelMonteCarloMethod methodLmmMc;
    methodLmmMc = new LiborMarketModelMonteCarloMethod(new NormalRandomNumberGenerator(0.0, 1.0, new MersenneTwister()), NB_PATH);
    final CurrencyAmount pvMC = methodLmmMc.presentValue(SWAPTION_PAYER_LONG, CUR, dsc, BUNDLE_LMM);
    final double pvMCPreviousRun = 4371960.422;
    assertEquals("Swaption physical - LMM - present value Monte Carlo", pvMCPreviousRun, pvMC.getAmount(), 1.0E-2);
    final CurrencyAmount pvApprox = METHOD_LMM.presentValue(SWAPTION_PAYER_LONG, BUNDLE_LMM);
    final double pvbp = METHOD_SWAP.presentValueBasisPoint(SWAP_RECEIVER, CURVES);
    final double forward = SWAP_RECEIVER.accept(PRC, CURVES);
    final BlackFunctionData data = new BlackFunctionData(forward, pvbp, 0.20);
    final EuropeanVanillaOption option = new EuropeanVanillaOption(RATE, SWAPTION_PAYER_LONG.getTimeToExpiry(), FIXED_IS_PAYER);
    final BlackImpliedVolatilityFormula implied = new BlackImpliedVolatilityFormula();
    final double impliedVolMC = implied.getImpliedVolatility(data, option, pvMC.getAmount());
    final double impliedVolApprox = implied.getImpliedVolatility(data, option, pvApprox.getAmount());
    assertEquals("Swaption physical - LMM - present value Approximation/Monte Carlo", impliedVolMC, impliedVolApprox, 2.0E-3);
  }

  @Test
  /**
   * Tests long/short parity.
   */
  public void longShortParity() {
    final CurrencyAmount pvLong = METHOD_LMM.presentValue(SWAPTION_PAYER_LONG, BUNDLE_LMM);
    final CurrencyAmount pvShort = METHOD_LMM.presentValue(SWAPTION_PAYER_SHORT, BUNDLE_LMM);
    assertEquals("Swaption physical - LMM - present value - long/short parity", pvLong.getAmount(), -pvShort.getAmount(), 1E-2);
  }

  @Test
  /**
   * Tests payer/receiver/swap parity.
   */
  public void payerReceiverParity() {
    final CurrencyAmount pvReceiverLong = METHOD_LMM.presentValue(SWAPTION_RECEIVER_LONG, BUNDLE_LMM);
    final CurrencyAmount pvPayerShort = METHOD_LMM.presentValue(SWAPTION_PAYER_SHORT, BUNDLE_LMM);
    final double pvSwap = SWAP_RECEIVER.accept(PVC, CURVES);
    assertEquals("Swaption physical - LMM - present value - payer/receiver/swap parity", pvReceiverLong.getAmount() + pvPayerShort.getAmount(), pvSwap, 1E-2);
  }

  @Test
  /**
   * Test the present value LMM volatility parameters sensitivity.
   */
  public void presentValueLMMSensitivity() {
    final double[][] pvLmmSensi = METHOD_LMM.presentValueLMMSensitivity(SWAPTION_PAYER_LONG, BUNDLE_LMM);

    final double shift = 1.0E-6;
    final LiborMarketModelDisplacedDiffusionParameters parameterShiftPlus = TestsDataSetLiborMarketModelDisplacedDiffusion.createLMMParametersShiftVol(REFERENCE_DATE,
        SWAP_PAYER_DEFINITION.getIborLeg(), shift);
    final LiborMarketModelDisplacedDiffusionDataBundle bundleShiftPlus = new LiborMarketModelDisplacedDiffusionDataBundle(parameterShiftPlus, CURVES);
    final CurrencyAmount pvShiftPlus = METHOD_LMM.presentValue(SWAPTION_PAYER_LONG, bundleShiftPlus);
    final LiborMarketModelDisplacedDiffusionParameters parameterShiftMinus = TestsDataSetLiborMarketModelDisplacedDiffusion.createLMMParametersShiftVol(REFERENCE_DATE,
        SWAP_PAYER_DEFINITION.getIborLeg(), -shift);
    final LiborMarketModelDisplacedDiffusionDataBundle bundleShiftMinus = new LiborMarketModelDisplacedDiffusionDataBundle(parameterShiftMinus, CURVES);
    final CurrencyAmount pvShiftMinus = METHOD_LMM.presentValue(SWAPTION_PAYER_LONG, bundleShiftMinus);
    final double pvLmmSensiTotExpected = (pvShiftPlus.getAmount() - pvShiftMinus.getAmount()) / (2 * shift);
    double pvLmmSensiTot = 0.0;
    for (final double[] element : pvLmmSensi) {
      for (int loopfact = 0; loopfact < pvLmmSensi[0].length; loopfact++) {
        pvLmmSensiTot += element[loopfact];
      }
    }
    assertEquals("Swaption physical - LMM - present value Lmm parameters sensitivity", pvLmmSensiTotExpected, pvLmmSensiTot, 1.0E-2);
  }

  @Test
  /**
   * Test the present value displaced diffusion parameters sensitivity.
   */
  public void presentValueDDSensitivity() {
    final double[] pvDDSensi = METHOD_LMM.presentValueDDSensitivity(SWAPTION_PAYER_LONG, BUNDLE_LMM);
    final double shift = 1.0E-6;
    final LiborMarketModelDisplacedDiffusionParameters parameterShiftPlus = TestsDataSetLiborMarketModelDisplacedDiffusion.createLMMParametersShiftDis(REFERENCE_DATE,
        SWAP_PAYER_DEFINITION.getIborLeg(), shift);
    final LiborMarketModelDisplacedDiffusionDataBundle bundleShiftPlus = new LiborMarketModelDisplacedDiffusionDataBundle(parameterShiftPlus, CURVES);
    final CurrencyAmount pvShiftPlus = METHOD_LMM.presentValue(SWAPTION_PAYER_LONG, bundleShiftPlus);
    final LiborMarketModelDisplacedDiffusionParameters parameterShiftMinus = TestsDataSetLiborMarketModelDisplacedDiffusion.createLMMParametersShiftDis(REFERENCE_DATE,
        SWAP_PAYER_DEFINITION.getIborLeg(), -shift);
    final LiborMarketModelDisplacedDiffusionDataBundle bundleShiftMinus = new LiborMarketModelDisplacedDiffusionDataBundle(parameterShiftMinus, CURVES);
    final CurrencyAmount pvShiftMinus = METHOD_LMM.presentValue(SWAPTION_PAYER_LONG, bundleShiftMinus);
    final double pvDDSensiTotExpected = (pvShiftPlus.getAmount() - pvShiftMinus.getAmount()) / (2 * shift);
    double pvDDSensiTot = 0.0;
    for (final double element : pvDDSensi) {
      pvDDSensiTot += element;
    }
    assertEquals("Swaption physical - LMM - present value displacement parameters sensitivity", pvDDSensiTotExpected, pvDDSensiTot, 1.0E-2);
  }

  @Test
  /**
   * Test the present value curvesensitivity.
   */
  public void presentValueCurveSensitivity() {
    InterestRateCurveSensitivity pvsSwaption = METHOD_LMM.presentValueCurveSensitivity(SWAPTION_PAYER_LONG, BUNDLE_LMM);
    pvsSwaption = pvsSwaption.cleaned();
    final double deltaTolerancePrice = 1.0E+2;
    //Testing note: Sensitivity is for a movement of 1. 1E+2 = 1 cent for a 1 bp move. Tolerance increased to cope with numerical imprecision of finite difference.
    final double deltaShift = 1.0E-6;
    // 1. Forward curve sensitivity
    final String bumpedCurveName = "Bumped Curve";
    final SwaptionPhysicalFixedIbor swptBumpedForward = SWAPTION_PAYER_LONG_DEFINITION.toDerivative(REFERENCE_DATE, new String[] {CURVES_NAME[0], bumpedCurveName });
    final DoubleAVLTreeSet forwardTime = new DoubleAVLTreeSet();
    for (int loopcpn = 0; loopcpn < SWAPTION_PAYER_LONG.getUnderlyingSwap().getSecondLeg().getNumberOfPayments(); loopcpn++) {
      final CouponIbor cpn = (CouponIbor) SWAPTION_PAYER_LONG.getUnderlyingSwap().getSecondLeg().getNthPayment(loopcpn);
      forwardTime.add(cpn.getFixingPeriodStartTime());
      forwardTime.add(cpn.getFixingPeriodEndTime());
    }
    final double[] nodeTimesForward = forwardTime.toDoubleArray();
    final double[] sensiForwardMethod = SensitivityFiniteDifference.curveSensitivity(swptBumpedForward, BUNDLE_LMM, CURVES_NAME[1], bumpedCurveName, nodeTimesForward, deltaShift, METHOD_LMM);
    final List<DoublesPair> sensiPvForward = pvsSwaption.getSensitivities().get(CURVES_NAME[1]);
    for (int loopnode = 0; loopnode < sensiForwardMethod.length; loopnode++) {
      final DoublesPair pairPv = sensiPvForward.get(loopnode);
      assertEquals("Sensitivity swaption pv to forward curve: Node " + loopnode, nodeTimesForward[loopnode], pairPv.getFirst(), 1E-8);
      assertEquals("Sensitivity finite difference method: node sensitivity " + loopnode, sensiForwardMethod[loopnode], pairPv.second, deltaTolerancePrice);
    }
    // 2. Discounting curve sensitivity
    final SwaptionPhysicalFixedIbor swptBumpedDisc = SWAPTION_PAYER_LONG_DEFINITION.toDerivative(REFERENCE_DATE, new String[] {bumpedCurveName, CURVES_NAME[1] });
    final DoubleAVLTreeSet discTime = new DoubleAVLTreeSet();
    discTime.add(SWAPTION_PAYER_LONG.getSettlementTime());
    for (int loopcpn = 0; loopcpn < SWAPTION_PAYER_LONG.getUnderlyingSwap().getSecondLeg().getNumberOfPayments(); loopcpn++) {
      final CouponIbor cpn = (CouponIbor) SWAPTION_PAYER_LONG.getUnderlyingSwap().getSecondLeg().getNthPayment(loopcpn);
      discTime.add(cpn.getPaymentTime());
    }
    final double[] nodeTimesDisc = discTime.toDoubleArray();
    final double[] sensiDiscMethod = SensitivityFiniteDifference.curveSensitivity(swptBumpedDisc, BUNDLE_LMM, CURVES_NAME[0], bumpedCurveName, nodeTimesDisc, deltaShift, METHOD_LMM);
    final List<DoublesPair> sensiPvDisc = pvsSwaption.getSensitivities().get(CURVES_NAME[0]);
    assertEquals("Sensitivity finite difference method: number of node", SWAP_TENOR_YEAR * 4 + 1, sensiPvDisc.size());
    for (int loopnode = 0; loopnode < sensiDiscMethod.length; loopnode++) {
      final DoublesPair pairPv = sensiPvDisc.get(loopnode);
      assertEquals("Sensitivity swaption pv to forward curve: Node " + loopnode, nodeTimesDisc[loopnode], pairPv.getFirst(), 1E-8);
      assertEquals("Sensitivity finite difference method: node sensitivity", sensiDiscMethod[loopnode], pairPv.second, deltaTolerancePrice);
    }
  }

  @Test(enabled = false)
  /**
   * Tests of performance. "enabled = false" for the standard testing.
   */
  public void performance() {
    long startTime, endTime;
    final int nbTest = 1000;

    final double startRate = 0.03;
    final double[] rate = new double[nbTest];
    final SwapFixedIborDefinition[] swapDefinition = new SwapFixedIborDefinition[nbTest];
    final SwaptionPhysicalFixedIborDefinition[] swaptionDefinition = new SwaptionPhysicalFixedIborDefinition[nbTest];
    final SwaptionPhysicalFixedIbor[] swaption = new SwaptionPhysicalFixedIbor[nbTest];
    for (int looptest = 0; looptest < nbTest; looptest++) {
      rate[looptest] = startRate + 0.00001 * looptest;
      swapDefinition[looptest] = SwapFixedIborDefinition.from(SETTLEMENT_DATE, CMS_INDEX, NOTIONAL, rate[looptest], FIXED_IS_PAYER, CALENDAR);
      swaptionDefinition[looptest] = SwaptionPhysicalFixedIborDefinition.from(EXPIRY_DATE, swapDefinition[looptest], FIXED_IS_PAYER, IS_LONG);
      swaption[looptest] = swaptionDefinition[looptest].toDerivative(REFERENCE_DATE, CURVES_NAME);
    }

    final CurrencyAmount[] pvPayerLongApproximation = new CurrencyAmount[nbTest];
    final double[][][] pvLmmSensi = new double[nbTest][][];
    startTime = System.currentTimeMillis();
    for (int looptest = 0; looptest < nbTest; looptest++) {
      pvPayerLongApproximation[looptest] = METHOD_LMM.presentValue(swaption[looptest], BUNDLE_LMM);
    }
    endTime = System.currentTimeMillis();
    System.out.println(nbTest + " swaption LMM approximation method: " + (endTime - startTime) + " ms");
    // Performance note: LMM approximation: 1-Sep-11: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 30 ms for 1000 swaptions.

    startTime = System.currentTimeMillis();
    for (int looptest = 0; looptest < nbTest; looptest++) {
      pvLmmSensi[looptest] = METHOD_LMM.presentValueLMMSensitivity(swaption[looptest], BUNDLE_LMM);
    }
    endTime = System.currentTimeMillis();
    System.out.println(nbTest + " swaption LMM approximation method - LMM volatility parameters sensitivity (20x2): " + (endTime - startTime) + " ms");
    // Performance note: LMM approximation - LMM sensi: 1-Sep-11: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 30 ms for 1000 swaptions.

    System.out.println("Approximation: " + Arrays.toString(pvPayerLongApproximation));

    final int nbTest2 = 10;
    final YieldAndDiscountCurve dsc = CURVES.getCurve(CURVES_NAME[0]);
    final CurrencyAmount[] pvMC = new CurrencyAmount[nbTest];
    startTime = System.currentTimeMillis();
    for (int looptest = 0; looptest < nbTest2; looptest++) {
      pvMC[looptest] = METHOD_LMM_MC.presentValue(swaption[looptest], CUR, dsc, BUNDLE_LMM);
    }
    endTime = System.currentTimeMillis();
    System.out.println(nbTest2 + " swaption LMM Monte Carlo method (" + NB_PATH + " paths): " + (endTime - startTime) + " ms");
    // Performance note: LMM Monte Carlo: 20-Sep-11: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 2400 ms for 10 swaptions/12500 paths/5 jumps.
  }

  @Test
  /**
   * Calibrate and price an amortized swaption.
   */
  public void calibrateExactPriceAmortized() {
    final Period fixedPaymentPeriod = Period.ofMonths(12);
    final Currency ccy = Currency.EUR;
    final Period iborTenor = Period.ofMonths(6);
    final IborIndex iborIndex = new IborIndex(ccy, iborTenor, SETTLEMENT_DAYS, IBOR_DAY_COUNT, BUSINESS_DAY, IS_EOM);
    final SABRInterestRateParameters sabrParameter = TestsDataSetsSABR.createSABR1();
    final SABRInterestRateDataBundle sabrBundle = new SABRInterestRateDataBundle(sabrParameter, CURVES);
    final int[] swapTenorYear = {1, 2, 3, 4, 5 };
    final IndexSwap[] cmsIndex = new IndexSwap[swapTenorYear.length];
    for (int loopexp = 0; loopexp < swapTenorYear.length; loopexp++) {
      cmsIndex[loopexp] = new IndexSwap(fixedPaymentPeriod, FIXED_DAY_COUNT, iborIndex, Period.ofYears(swapTenorYear[loopexp]), CALENDAR);
    }
    final double[] amortization = new double[] {1.00, 0.80, 0.60, 0.40, 0.20 }; // For 5Y amortization
    //    double[] amortization = new double[] {1.00, 0.90, 0.80, 0.70, 0.60, 0.50, 0.40, 0.30, 0.20, 0.10}; // For 10Y amortization
    final SwapFixedIborDefinition[] swapCalibrationDefinition = new SwapFixedIborDefinition[swapTenorYear.length];
    final SwaptionPhysicalFixedIborDefinition[] swaptionCalibrationDefinition = new SwaptionPhysicalFixedIborDefinition[swapTenorYear.length];
    final SwaptionPhysicalFixedIbor[] swaptionCalibration = new SwaptionPhysicalFixedIbor[swapTenorYear.length];
    for (int loopexp = 0; loopexp < swapTenorYear.length; loopexp++) {
      swapCalibrationDefinition[loopexp] = SwapFixedIborDefinition.from(SETTLEMENT_DATE, cmsIndex[loopexp], NOTIONAL, RATE, FIXED_IS_PAYER, CALENDAR);
      swaptionCalibrationDefinition[loopexp] = SwaptionPhysicalFixedIborDefinition.from(EXPIRY_DATE, swapCalibrationDefinition[loopexp], FIXED_IS_PAYER, IS_LONG);
      swaptionCalibration[loopexp] = swaptionCalibrationDefinition[loopexp].toDerivative(REFERENCE_DATE, CURVES_NAME);
    }
    final CouponFixed[] cpnFixed = new CouponFixed[swapTenorYear.length];
    final AnnuityCouponFixed legFixed = swaptionCalibration[swapTenorYear.length - 1].getUnderlyingSwap().getFixedLeg();
    final CouponIbor[] cpnIbor = new CouponIbor[2 * swapTenorYear.length];
    final Annuity<Payment> legIbor = (Annuity<Payment>) swaptionCalibration[swapTenorYear.length - 1].getUnderlyingSwap().getSecondLeg();
    for (int loopexp = 0; loopexp < swapTenorYear.length; loopexp++) {
      cpnFixed[loopexp] = legFixed.getNthPayment(loopexp).withNotional(legFixed.getNthPayment(loopexp).getNotional() * amortization[loopexp]);
      cpnIbor[2 * loopexp] = ((CouponIbor) legIbor.getNthPayment(2 * loopexp)).withNotional(((CouponIbor) legIbor.getNthPayment(2 * loopexp)).getNotional() * amortization[loopexp]);
      cpnIbor[2 * loopexp + 1] = ((CouponIbor) legIbor.getNthPayment(2 * loopexp + 1)).withNotional(((CouponIbor) legIbor.getNthPayment(2 * loopexp + 1)).getNotional() * amortization[loopexp]);
    }

    final SwapFixedCoupon<Coupon> swapAmortized = new SwapFixedCoupon<>(new AnnuityCouponFixed(cpnFixed), new Annuity<Coupon>(cpnIbor));
    final SwaptionPhysicalFixedIbor swaptionAmortized = SwaptionPhysicalFixedIbor.from(swaptionCalibration[0].getTimeToExpiry(), swapAmortized, swaptionCalibration[0].getSettlementTime(), IS_LONG);

    final InstrumentDerivative[] swaptionCalibration2 = METHOD_BASKET.calibrationBasketFixedLegPeriod(swaptionAmortized);

    assertEquals("Calibration basket", swaptionCalibration.length, swaptionCalibration2.length);
    for (int loopcal = 0; loopcal < swaptionCalibration.length; loopcal++) {
      assertEquals("Calibration basket: " + loopcal, METHOD_SABR.presentValue(swaptionCalibration[loopcal], sabrBundle).getAmount(), METHOD_SABR
          .presentValue(swaptionCalibration2[loopcal], sabrBundle).getAmount(), 1.0E-2);
    }
    // Calibration and price
    final LiborMarketModelDisplacedDiffusionParameters lmmParameters = TestsDataSetLiborMarketModelDisplacedDiffusion.createLMMParametersDisplacementAngle(REFERENCE_DATE,
        swapCalibrationDefinition[swapTenorYear.length - 1].getIborLeg(), 0.10, Math.PI / 2);
    final SwaptionPhysicalLMMDDSuccessiveRootFinderCalibrationObjective objective = new SwaptionPhysicalLMMDDSuccessiveRootFinderCalibrationObjective(lmmParameters);
    final SuccessiveRootFinderCalibrationEngine calibrationEngine = new SwaptionPhysicalLMMDDSuccessiveRootFinderCalibrationEngine(objective);
    calibrationEngine.addInstrument(swaptionCalibration2, METHOD_SABR);
    calibrationEngine.calibrate(sabrBundle);
    final LiborMarketModelDisplacedDiffusionDataBundle lmmBundle = new LiborMarketModelDisplacedDiffusionDataBundle(lmmParameters, CURVES);
    final CurrencyAmount pvAmortized = METHOD_LMM.presentValue(swaptionAmortized, lmmBundle);
    final double pvAmortizedPrevious = 3058997.117;
    assertEquals("LMM Amortized pricing", pvAmortizedPrevious, pvAmortized.getAmount(), 1.0E-2);
    // Method
    final SwaptionPhysicalFixedIborSABRLMMExactMethod method = new SwaptionPhysicalFixedIborSABRLMMExactMethod();
    final CurrencyAmount pvAmortizedMethod = method.presentValue(swaptionAmortized, sabrBundle);
    assertEquals("LMM Amortized pricing", pvAmortized.getAmount(), pvAmortizedMethod.getAmount(), 1.0E-2);

    // SABR parameters sensitivity in all-in-one method.
    final List<Object> results = method.presentValueCurveSABRSensitivity(swaptionAmortized, sabrBundle);
    final InterestRateCurveSensitivity pvcs1 = (InterestRateCurveSensitivity) results.get(1);
    final PresentValueSABRSensitivityDataBundle pvss1 = (PresentValueSABRSensitivityDataBundle) results.get(2);

    // SABR parameters sensitivity
    final PresentValueSABRSensitivityDataBundle pvss = method.presentValueSABRSensitivity(swaptionAmortized, sabrBundle);

    // SABR parameters sensitivity (all-in-one)
    for (final SwaptionPhysicalFixedIbor element : swaptionCalibration) {
      final DoublesPair expiryMaturity = DoublesPair.of(element.getTimeToExpiry(), element.getMaturityTime());
      assertEquals("Sensitivity swaption pv to alpha", pvss1.getAlpha().getMap().get(expiryMaturity), pvss.getAlpha().getMap().get(expiryMaturity), 1E-2);
      assertEquals("Sensitivity swaption pv to rho", pvss1.getRho().getMap().get(expiryMaturity), pvss.getRho().getMap().get(expiryMaturity), 1E-2);
      assertEquals("Sensitivity swaption pv to nu", pvss1.getNu().getMap().get(expiryMaturity), pvss.getNu().getMap().get(expiryMaturity), 1E-2);
    }
    // SABR parameters sensitivity (parallel shift check)
    SABRInterestRateParameters sabrParameterShift;
    SABRInterestRateDataBundle sabrBundleShift;
    final LiborMarketModelDisplacedDiffusionParameters lmmParametersShift = TestsDataSetLiborMarketModelDisplacedDiffusion.createLMMParametersDisplacementAngle(REFERENCE_DATE,
        swapCalibrationDefinition[swapTenorYear.length - 1].getIborLeg(), 0.10, Math.PI / 2);
    final SwaptionPhysicalLMMDDSuccessiveRootFinderCalibrationObjective objectiveShift = new SwaptionPhysicalLMMDDSuccessiveRootFinderCalibrationObjective(lmmParametersShift);
    final SuccessiveRootFinderCalibrationEngine calibrationEngineShift = new SwaptionPhysicalLMMDDSuccessiveRootFinderCalibrationEngine(objectiveShift);
    calibrationEngineShift.addInstrument(swaptionCalibration2, METHOD_SABR);
    final LiborMarketModelDisplacedDiffusionDataBundle lmmBundleShift = new LiborMarketModelDisplacedDiffusionDataBundle(lmmParametersShift, CURVES);

    double alphaVegaTotalComputed = 0.0;
    assertEquals("Number of alpha sensitivity", pvss.getAlpha().getMap().keySet().size(), swaptionCalibration.length);
    for (final SwaptionPhysicalFixedIbor element : swaptionCalibration) {
      final DoublesPair expiryMaturity = DoublesPair.of(element.getTimeToExpiry(), element.getMaturityTime());
      alphaVegaTotalComputed += pvss.getAlpha().getMap().get(expiryMaturity);
    }
    final double shiftAlpha = 0.00001;
    sabrParameterShift = TestsDataSetsSABR.createSABR1AlphaBumped(shiftAlpha);
    sabrBundleShift = new SABRInterestRateDataBundle(sabrParameterShift, CURVES);
    calibrationEngineShift.calibrate(sabrBundleShift);
    final CurrencyAmount pvAmortizedShiftAlpha = METHOD_LMM.presentValue(swaptionAmortized, lmmBundleShift);
    final double alphaVegaTotalExpected = (pvAmortizedShiftAlpha.getAmount() - pvAmortized.getAmount()) / shiftAlpha;
    assertEquals("Alpha sensitivity value", alphaVegaTotalExpected, alphaVegaTotalComputed, 1.0E+2);

    double rhoVegaTotalComputed = 0.0;
    assertEquals("Number of alpha sensitivity", pvss.getRho().getMap().keySet().size(), swaptionCalibration.length);
    for (final SwaptionPhysicalFixedIbor element : swaptionCalibration) {
      final DoublesPair expiryMaturity = DoublesPair.of(element.getTimeToExpiry(), element.getMaturityTime());
      rhoVegaTotalComputed += pvss.getRho().getMap().get(expiryMaturity);
    }
    final double shiftRho = 0.00001;
    sabrParameterShift = TestsDataSetsSABR.createSABR1RhoBumped(shiftRho);
    sabrBundleShift = new SABRInterestRateDataBundle(sabrParameterShift, CURVES);
    calibrationEngineShift.calibrate(sabrBundleShift);
    final CurrencyAmount pvAmortizedShiftRho = METHOD_LMM.presentValue(swaptionAmortized, lmmBundleShift);
    final double rhoVegaTotalExpected = (pvAmortizedShiftRho.getAmount() - pvAmortized.getAmount()) / shiftRho;
    assertEquals("Rho sensitivity value", rhoVegaTotalExpected, rhoVegaTotalComputed, 1.0E+1);

    double nuVegaTotalComputed = 0.0;
    assertEquals("Number of alpha sensitivity", pvss.getNu().getMap().keySet().size(), swaptionCalibration.length);
    for (final SwaptionPhysicalFixedIbor element : swaptionCalibration) {
      final DoublesPair expiryMaturity = DoublesPair.of(element.getTimeToExpiry(), element.getMaturityTime());
      nuVegaTotalComputed += pvss.getNu().getMap().get(expiryMaturity);
    }
    final double shiftNu = 0.00001;
    sabrParameterShift = TestsDataSetsSABR.createSABR1NuBumped(shiftNu);
    sabrBundleShift = new SABRInterestRateDataBundle(sabrParameterShift, CURVES);
    calibrationEngineShift.calibrate(sabrBundleShift);
    final CurrencyAmount pvAmortizedShiftNu = METHOD_LMM.presentValue(swaptionAmortized, lmmBundleShift);
    final double nuVegaTotalExpected = (pvAmortizedShiftNu.getAmount() - pvAmortized.getAmount()) / shiftNu;
    assertEquals("Nu sensitivity value", nuVegaTotalExpected, nuVegaTotalComputed, 1.0E+1);

    // Curve sensitivity
    InterestRateCurveSensitivity pvcs = method.presentValueCurveSensitivity(swaptionAmortized, sabrBundle);
    pvcs = pvcs.cleaned();
    // Curve sensitivity (all-in-one)
    final List<DoublesPair> pvcsFwd = pvcs.getSensitivities().get(CURVES_NAME[1]);
    final List<DoublesPair> pvcsFwd1 = pvcs1.getSensitivities().get(CURVES_NAME[1]);
    for (int loopnode = 0; loopnode < pvcsFwd.size(); loopnode++) {
      final DoublesPair pairPvcsFwd = pvcsFwd.get(loopnode);
      final DoublesPair pairPvcsFwd1 = pvcsFwd1.get(loopnode);
      assertEquals("Sensitivity swaption pv to forward curve: Node " + loopnode, pairPvcsFwd.first, pairPvcsFwd1.first, 1E-8);
      assertEquals("Sensitivity finite difference method: node sensitivity " + loopnode, pairPvcsFwd.second, pairPvcsFwd1.second, 1E-2);
    }
    final List<DoublesPair> pvcsDsc = pvcs.getSensitivities().get(CURVES_NAME[0]);
    final List<DoublesPair> pvcsDsc1 = pvcs1.getSensitivities().get(CURVES_NAME[0]);
    for (int loopnode = 0; loopnode < pvcsDsc.size(); loopnode++) {
      final DoublesPair pairPvcsDsc = pvcsDsc.get(loopnode);
      final DoublesPair pairPvcsDsc1 = pvcsDsc1.get(loopnode);
      assertEquals("Sensitivity swaption pv to forward curve: Node " + loopnode, pairPvcsDsc.first, pairPvcsDsc1.first, 1E-8);
      assertEquals("Sensitivity finite difference method: node sensitivity " + loopnode, pairPvcsDsc.second, pairPvcsDsc1.second, 1E-2);
    }
    // Curve sensitivity (parallel shift check)
    final double shiftCurve = 0.0000001;
    final YieldAndDiscountCurve curve5Shift = YieldCurve.from(ConstantDoublesCurve.from(0.05 + shiftCurve));
    final YieldAndDiscountCurve curve4Shift = YieldCurve.from(ConstantDoublesCurve.from(0.04 + shiftCurve));

    final YieldCurveBundle curvesDscShift = new YieldCurveBundle();
    curvesDscShift.setCurve(FUNDING_CURVE_NAME, curve5Shift);
    curvesDscShift.setCurve(FORWARD_CURVE_NAME, CURVES.getCurve(FORWARD_CURVE_NAME));
    final SABRInterestRateDataBundle sabrBundleCurveDscShift = new SABRInterestRateDataBundle(sabrParameter, curvesDscShift);
    final CurrencyAmount pvAmortizedShiftCurveDsc = method.presentValue(swaptionAmortized, sabrBundleCurveDscShift);
    final double dscDeltaTotalExpected = (pvAmortizedShiftCurveDsc.getAmount() - pvAmortized.getAmount()) / shiftCurve;
    double dscDeltaTotal = 0.0;
    final List<DoublesPair> dscSensi = pvcs.getSensitivities().get(FUNDING_CURVE_NAME);
    for (int looppay = 0; looppay < dscSensi.size(); looppay++) {
      dscDeltaTotal += dscSensi.get(looppay).second;
    }
    assertEquals("Curve DSC sensitivity value", dscDeltaTotalExpected, dscDeltaTotal, 5.0E+1);

    final YieldCurveBundle curvesFwdShift = new YieldCurveBundle();
    curvesFwdShift.setCurve(FUNDING_CURVE_NAME, CURVES.getCurve(FUNDING_CURVE_NAME));
    curvesFwdShift.setCurve(FORWARD_CURVE_NAME, curve4Shift);
    final SABRInterestRateDataBundle sabrBundleCurveFwdShift = new SABRInterestRateDataBundle(sabrParameter, curvesFwdShift);
    final CurrencyAmount pvAmortizedShiftCurveFwd = method.presentValue(swaptionAmortized, sabrBundleCurveFwdShift);
    final double fwdDeltaTotalExpected = (pvAmortizedShiftCurveFwd.getAmount() - pvAmortized.getAmount()) / shiftCurve;
    double fwdDeltaTotal = 0.0;
    final List<DoublesPair> fwdSensi = pvcs.getSensitivities().get(FORWARD_CURVE_NAME);
    for (int looppay = 0; looppay < fwdSensi.size(); looppay++) {
      fwdDeltaTotal += fwdSensi.get(looppay).second;
    }
    assertEquals("Curve FWD sensitivity value", fwdDeltaTotalExpected, fwdDeltaTotal, 2.0E+2);
  }

  @Test
  /**
   * Calibrate and price an amortized swaption.
   */
  public void calibrateAtBestPriceAmortized() {
    final SABRInterestRateParameters sabrParameter = TestsDataSetsSABR.createSABR1();
    final SABRInterestRateDataBundle sabrBundle = new SABRInterestRateDataBundle(sabrParameter, CURVES);
    final double[] amortization = new double[] {1.00, 0.80, 0.60, 0.40, 0.20 }; // For 5Y amortization
    final int nbPeriods = amortization.length;
    final SwapFixedIborDefinition swapDefinition = SwapFixedIborDefinition.from(SETTLEMENT_DATE, Period.ofYears(nbPeriods), EUR1YEURIBOR6M, NOTIONAL, RATE, FIXED_IS_PAYER);
    //    SwapFixedCoupon<Coupon> swap = swapDefinition.toDerivative(REFERENCE_DATE, CURVES_NAME);
    final CouponFixedDefinition[] cpnFixed = new CouponFixedDefinition[nbPeriods];
    final AnnuityCouponFixedDefinition legFixed = swapDefinition.getFixedLeg();
    final CouponIborDefinition[] cpnIbor = new CouponIborDefinition[2 * nbPeriods];
    final AnnuityDefinition<? extends PaymentDefinition> legIbor = swapDefinition.getSecondLeg();
    for (int loopexp = 0; loopexp < nbPeriods; loopexp++) {
      cpnFixed[loopexp] = legFixed.getNthPayment(loopexp).withNotional(legFixed.getNthPayment(loopexp).getNotional() * amortization[loopexp]);
      cpnIbor[2 * loopexp] = ((CouponIborDefinition) legIbor.getNthPayment(2 * loopexp))
          .withNotional(((CouponIborDefinition) legIbor.getNthPayment(2 * loopexp)).getNotional() * amortization[loopexp]);
      cpnIbor[2 * loopexp + 1] = ((CouponIborDefinition) legIbor.getNthPayment(2 * loopexp + 1)).withNotional(((CouponIborDefinition) legIbor.getNthPayment(2 * loopexp + 1)).getNotional()
          * amortization[loopexp]);
    }
    final SwapFixedIborDefinition swapAmortizedDefinition = new SwapFixedIborDefinition(new AnnuityCouponFixedDefinition(cpnFixed, CALENDAR), new AnnuityCouponIborDefinition(cpnIbor, EURIBOR6M, TARGET));
    final SwaptionPhysicalFixedIborDefinition swaptionAmortizedDefinition = SwaptionPhysicalFixedIborDefinition.from(EXPIRY_DATE, swapAmortizedDefinition, true, IS_LONG);
    final SwaptionPhysicalFixedIbor swaptionAmortized = swaptionAmortizedDefinition.toDerivative(REFERENCE_DATE, CURVES_NAME);

    // SABR parameters sensitivity (parallel shift check). The sensitivities are not exact; in the approximation a small "second order" term is ignored
    final PresentValueSABRSensitivityDataBundle pvss = METHOD_SABR_LMM_ATBEST.presentValueSABRSensitivity(swaptionAmortized, sabrBundle);
    final double[] shift = new double[] {0.0001, 0.0001, 0.0001 };
    final double[] toleranceSABRSensi = new double[] {5.0E+4, 5.0E+3, 1.0E+4 };
    final double[] sensiComputed = new double[] {pvss.getAlpha().toSingleValue(), pvss.getRho().toSingleValue(), pvss.getNu().toSingleValue() };
    final double[] sensiExpected = new double[shift.length];
    SABRInterestRateParameters sabrParameterShift;
    SABRInterestRateDataBundle sabrBundleShift;
    for (int loopp = 0; loopp < shift.length; loopp++) {
      sabrParameterShift = TestsDataSetsSABR.createSABR1ParameterBumped(shift[loopp], loopp);
      sabrBundleShift = new SABRInterestRateDataBundle(sabrParameterShift, CURVES);
      final CurrencyAmount pvShiftPlus = METHOD_SABR_LMM_ATBEST.presentValue(swaptionAmortized, sabrBundleShift);
      sabrParameterShift = TestsDataSetsSABR.createSABR1ParameterBumped(-shift[loopp], loopp);
      sabrBundleShift = new SABRInterestRateDataBundle(sabrParameterShift, CURVES);
      final CurrencyAmount pvShiftMinus = METHOD_SABR_LMM_ATBEST.presentValue(swaptionAmortized, sabrBundleShift);
      sensiExpected[loopp] = (pvShiftPlus.getAmount() - pvShiftMinus.getAmount()) / (2 * shift[loopp]);
      assertEquals("SwaptionPhysicalFixedIborLMM: Calibration at best - SABR sensitivity " + loopp, sensiExpected[loopp], sensiComputed[loopp], toleranceSABRSensi[loopp]);
    }

    // TODO: Curve sensitivity test
  }

}
