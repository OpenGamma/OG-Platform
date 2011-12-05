/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.swaption.method;

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
import com.opengamma.financial.instrument.index.IborIndex;
import com.opengamma.financial.instrument.index.IndexSwap;
import com.opengamma.financial.instrument.swap.SwapFixedIborDefinition;
import com.opengamma.financial.instrument.swaption.SwaptionPhysicalFixedIborDefinition;
import com.opengamma.financial.interestrate.InstrumentDerivative;
import com.opengamma.financial.interestrate.InterestRateCurveSensitivity;
import com.opengamma.financial.interestrate.ParRateCalculator;
import com.opengamma.financial.interestrate.PresentValueCalculator;
import com.opengamma.financial.interestrate.PresentValueSABRSensitivityDataBundle;
import com.opengamma.financial.interestrate.TestsDataSets;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.interestrate.annuity.definition.AnnuityCouponFixed;
import com.opengamma.financial.interestrate.annuity.definition.AnnuityCouponIbor;
import com.opengamma.financial.interestrate.annuity.definition.GenericAnnuity;
import com.opengamma.financial.interestrate.method.SensitivityFiniteDifference;
import com.opengamma.financial.interestrate.method.SuccessiveRootFinderCalibrationEngine;
import com.opengamma.financial.interestrate.payments.Coupon;
import com.opengamma.financial.interestrate.payments.CouponFixed;
import com.opengamma.financial.interestrate.payments.CouponIbor;
import com.opengamma.financial.interestrate.payments.Payment;
import com.opengamma.financial.interestrate.swap.SwapFixedIborMethod;
import com.opengamma.financial.interestrate.swap.definition.FixedCouponSwap;
import com.opengamma.financial.interestrate.swap.definition.FixedFloatSwap;
import com.opengamma.financial.interestrate.swaption.derivative.SwaptionPhysicalFixedIbor;
import com.opengamma.financial.interestrate.swaption.derivative.SwaptionPhysicalFixedIbor.SwaptionPhysicalFixedIborCalibrationType;
import com.opengamma.financial.model.interestrate.LiborMarketModelDisplacedDiffusionTestsDataSet;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.financial.model.interestrate.definition.LiborMarketModelDisplacedDiffusionDataBundle;
import com.opengamma.financial.model.interestrate.definition.LiborMarketModelDisplacedDiffusionParameters;
import com.opengamma.financial.model.option.definition.SABRInterestRateDataBundle;
import com.opengamma.financial.model.option.definition.SABRInterestRateParameters;
import com.opengamma.financial.model.option.pricing.analytic.formula.BlackFunctionData;
import com.opengamma.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.financial.model.volatility.BlackImpliedVolatilityFormula;
import com.opengamma.financial.montecarlo.LiborMarketModelMonteCarloMethod;
import com.opengamma.financial.schedule.ScheduleCalculator;
import com.opengamma.math.curve.ConstantDoublesCurve;
import com.opengamma.math.random.NormalRandomNumberGenerator;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Tests related to the pricing of physical delivery swaption in LMM displaced diffusion.
 */
public class SwaptionPhysicalFixedIborLMMDDMethodTest {
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
  private static final ZonedDateTime SETTLEMENT_DATE = ScheduleCalculator.getAdjustedDate(EXPIRY_DATE, CALENDAR, SETTLEMENT_DAYS);
  private static final double NOTIONAL = 100000000; //100m
  private static final double RATE = 0.0325;
  private static final boolean FIXED_IS_PAYER = true;
  private static final SwapFixedIborDefinition SWAP_PAYER_DEFINITION = SwapFixedIborDefinition.from(SETTLEMENT_DATE, CMS_INDEX, NOTIONAL, RATE, FIXED_IS_PAYER);
  private static final SwapFixedIborDefinition SWAP_RECEIVER_DEFINITION = SwapFixedIborDefinition.from(SETTLEMENT_DATE, CMS_INDEX, NOTIONAL, RATE, !FIXED_IS_PAYER);
  private static final boolean IS_LONG = true;
  private static final SwaptionPhysicalFixedIborDefinition SWAPTION_PAYER_LONG_DEFINITION = SwaptionPhysicalFixedIborDefinition.from(EXPIRY_DATE, SWAP_PAYER_DEFINITION, IS_LONG);
  private static final SwaptionPhysicalFixedIborDefinition SWAPTION_RECEIVER_LONG_DEFINITION = SwaptionPhysicalFixedIborDefinition.from(EXPIRY_DATE, SWAP_RECEIVER_DEFINITION, IS_LONG);
  private static final SwaptionPhysicalFixedIborDefinition SWAPTION_PAYER_SHORT_DEFINITION = SwaptionPhysicalFixedIborDefinition.from(EXPIRY_DATE, SWAP_PAYER_DEFINITION, !IS_LONG);
  //to derivatives
  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2011, 7, 7);
  private static final String FUNDING_CURVE_NAME = "Funding";
  private static final String FORWARD_CURVE_NAME = "Forward";
  private static final String[] CURVES_NAME = {FUNDING_CURVE_NAME, FORWARD_CURVE_NAME};
  private static final YieldCurveBundle CURVES = TestsDataSets.createCurves1();

  private static final FixedCouponSwap<Coupon> SWAP_RECEIVER = SWAP_RECEIVER_DEFINITION.toDerivative(REFERENCE_DATE, CURVES_NAME);
  private static final SwaptionPhysicalFixedIbor SWAPTION_PAYER_LONG = SWAPTION_PAYER_LONG_DEFINITION.toDerivative(REFERENCE_DATE, CURVES_NAME);
  private static final SwaptionPhysicalFixedIbor SWAPTION_RECEIVER_LONG = SWAPTION_RECEIVER_LONG_DEFINITION.toDerivative(REFERENCE_DATE, CURVES_NAME);
  private static final SwaptionPhysicalFixedIbor SWAPTION_PAYER_SHORT = SWAPTION_PAYER_SHORT_DEFINITION.toDerivative(REFERENCE_DATE, CURVES_NAME);
  // Parameters and methods
  private static final PresentValueCalculator PVC = PresentValueCalculator.getInstance();
  private static final ParRateCalculator PRC = ParRateCalculator.getInstance();
  private static final SwaptionPhysicalFixedIborLMMDDMethod METHOD_LMM = new SwaptionPhysicalFixedIborLMMDDMethod();
  private static final SwaptionPhysicalFixedIborSABRMethod METHOD_SABR = SwaptionPhysicalFixedIborSABRMethod.getInstance();
  private static final LiborMarketModelDisplacedDiffusionParameters PARAMETERS_LMM = LiborMarketModelDisplacedDiffusionTestsDataSet.createLMMParameters(REFERENCE_DATE, SWAP_PAYER_DEFINITION);
  private static final LiborMarketModelDisplacedDiffusionDataBundle BUNDLE_LMM = new LiborMarketModelDisplacedDiffusionDataBundle(PARAMETERS_LMM, CURVES);

  private static final int NB_PATH = 12500;
  private static final LiborMarketModelMonteCarloMethod METHOD_LMM_MC = new LiborMarketModelMonteCarloMethod(new NormalRandomNumberGenerator(0.0, 1.0), NB_PATH);

  @Test
  /**
   * Test the present value.
   */
  public void presentValue() {
    double pvPreviousRun = 4367793.468; // Mean reversion 0.001: 4367793.468;
    CurrencyAmount pv = METHOD_LMM.presentValue(SWAPTION_PAYER_LONG, BUNDLE_LMM);
    assertEquals("Swaption physical - LMM - present value", pvPreviousRun, pv.getAmount(), 1E-2);
  }

  @Test
  /**
   * Test the present value: approximated formula vs Monte Carlo.
   */
  public void presentValueMC() {
    YieldAndDiscountCurve dsc = CURVES.getCurve(CURVES_NAME[0]);
    LiborMarketModelMonteCarloMethod methodLmmMc;
    methodLmmMc = new LiborMarketModelMonteCarloMethod(new NormalRandomNumberGenerator(0.0, 1.0, new MersenneTwister()), NB_PATH);
    CurrencyAmount pvMC = methodLmmMc.presentValue(SWAPTION_PAYER_LONG, CUR, dsc, BUNDLE_LMM);
    double pvMCPreviousRun = 4371960.422; // One jump: xxx // Jump 2Y: xxx // Jump 1Y: 4371960.422 - 4385240.574
    assertEquals("Swaption physical - LMM - present value Monte Carlo", pvMCPreviousRun, pvMC.getAmount(), 1.0E-2);
    CurrencyAmount pvApprox = METHOD_LMM.presentValue(SWAPTION_PAYER_LONG, BUNDLE_LMM);
    double pvbp = SwapFixedIborMethod.presentValueBasisPoint(SWAP_RECEIVER, CURVES);
    double forward = PRC.visit(SWAP_RECEIVER, CURVES);
    BlackFunctionData data = new BlackFunctionData(forward, pvbp, 0.20);
    EuropeanVanillaOption option = new EuropeanVanillaOption(RATE, SWAPTION_PAYER_LONG.getTimeToExpiry(), FIXED_IS_PAYER);
    BlackImpliedVolatilityFormula implied = new BlackImpliedVolatilityFormula();
    double impliedVolMC = implied.getImpliedVolatility(data, option, pvMC.getAmount());
    double impliedVolApprox = implied.getImpliedVolatility(data, option, pvApprox.getAmount());
    assertEquals("Swaption physical - LMM - present value Approximation/Monte Carlo", impliedVolMC, impliedVolApprox, 2.0E-3);
  }

  @Test
  /**
   * Tests long/short parity.
   */
  public void longShortParity() {
    CurrencyAmount pvLong = METHOD_LMM.presentValue(SWAPTION_PAYER_LONG, BUNDLE_LMM);
    CurrencyAmount pvShort = METHOD_LMM.presentValue(SWAPTION_PAYER_SHORT, BUNDLE_LMM);
    assertEquals("Swaption physical - LMM - present value - long/short parity", pvLong.getAmount(), -pvShort.getAmount(), 1E-2);
  }

  @Test
  /**
   * Tests payer/receiver/swap parity.
   */
  public void payerReceiverParity() {
    CurrencyAmount pvReceiverLong = METHOD_LMM.presentValue(SWAPTION_RECEIVER_LONG, BUNDLE_LMM);
    CurrencyAmount pvPayerShort = METHOD_LMM.presentValue(SWAPTION_PAYER_SHORT, BUNDLE_LMM);
    double pvSwap = PVC.visit(SWAP_RECEIVER, CURVES);
    assertEquals("Swaption physical - LMM - present value - payer/receiver/swap parity", pvReceiverLong.getAmount() + pvPayerShort.getAmount(), pvSwap, 1E-2);
  }

  @Test
  /**
   * Test the present value LMM volatility parameters sensitivity.
   */
  public void presentValueLMMSensitivity() {
    double[][] pvLmmSensi = METHOD_LMM.presentValueLMMSensitivity(SWAPTION_PAYER_LONG, BUNDLE_LMM);

    double shift = 1.0E-6;
    LiborMarketModelDisplacedDiffusionParameters parameterShiftPlus = LiborMarketModelDisplacedDiffusionTestsDataSet.createLMMParameters(REFERENCE_DATE, SWAP_PAYER_DEFINITION, shift);
    LiborMarketModelDisplacedDiffusionDataBundle bundleShiftPlus = new LiborMarketModelDisplacedDiffusionDataBundle(parameterShiftPlus, CURVES);
    CurrencyAmount pvShiftPlus = METHOD_LMM.presentValue(SWAPTION_PAYER_LONG, bundleShiftPlus);
    LiborMarketModelDisplacedDiffusionParameters parameterShiftMinus = LiborMarketModelDisplacedDiffusionTestsDataSet.createLMMParameters(REFERENCE_DATE, SWAP_PAYER_DEFINITION, -shift);
    LiborMarketModelDisplacedDiffusionDataBundle bundleShiftMinus = new LiborMarketModelDisplacedDiffusionDataBundle(parameterShiftMinus, CURVES);
    CurrencyAmount pvShiftMinus = METHOD_LMM.presentValue(SWAPTION_PAYER_LONG, bundleShiftMinus);
    double pvLmmSensiTotExpected = (pvShiftPlus.getAmount() - pvShiftMinus.getAmount()) / (2 * shift);
    double pvLmmSensiTot = 0.0;
    for (int loopperiod = 0; loopperiod < pvLmmSensi.length; loopperiod++) {
      for (int loopfact = 0; loopfact < pvLmmSensi[0].length; loopfact++) {
        pvLmmSensiTot += pvLmmSensi[loopperiod][loopfact];
      }
    }
    assertEquals("Swaption physical - LMM - present value Lmm parameters sensitivity", pvLmmSensiTotExpected, pvLmmSensiTot, 1.0E-2);
  }

  @Test
  /**
   * Test the present value curvesensitivity.
   */
  public void presentValueCurveSensitivity() {
    InterestRateCurveSensitivity pvsSwaption = METHOD_LMM.presentValueCurveSensitivity(SWAPTION_PAYER_LONG, BUNDLE_LMM);
    pvsSwaption = pvsSwaption.clean();
    final double deltaTolerancePrice = 1.0E+2;
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
    final double[] sensiForwardMethod = SensitivityFiniteDifference.curveSensitivity(swptBumpedForward, BUNDLE_LMM, CURVES_NAME[1], bumpedCurveName, nodeTimesForward, deltaShift, METHOD_LMM);
    final List<DoublesPair> sensiPvForward = pvsSwaption.getSensitivities().get(CURVES_NAME[1]);
    for (int loopnode = 0; loopnode < sensiForwardMethod.length; loopnode++) {
      final DoublesPair pairPv = sensiPvForward.get(loopnode);
      assertEquals("Sensitivity swaption pv to forward curve: Node " + loopnode, nodeTimesForward[loopnode], pairPv.getFirst(), 1E-8);
      assertEquals("Sensitivity finite difference method: node sensitivity " + loopnode, sensiForwardMethod[loopnode], pairPv.second, deltaTolerancePrice);
    }
    // 2. Discounting curve sensitivity
    final SwaptionPhysicalFixedIbor swptBumpedDisc = SWAPTION_PAYER_LONG_DEFINITION.toDerivative(REFERENCE_DATE, new String[] {bumpedCurveName, CURVES_NAME[1]});
    DoubleAVLTreeSet discTime = new DoubleAVLTreeSet();
    discTime.add(SWAPTION_PAYER_LONG.getSettlementTime());
    for (int loopcpn = 0; loopcpn < SWAPTION_PAYER_LONG.getUnderlyingSwap().getSecondLeg().getNumberOfPayments(); loopcpn++) {
      CouponIbor cpn = (CouponIbor) SWAPTION_PAYER_LONG.getUnderlyingSwap().getSecondLeg().getNthPayment(loopcpn);
      discTime.add(cpn.getPaymentTime());
    }
    double[] nodeTimesDisc = discTime.toDoubleArray();
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

    double startRate = 0.03;
    double[] rate = new double[nbTest];
    SwapFixedIborDefinition[] swapDefinition = new SwapFixedIborDefinition[nbTest];
    SwaptionPhysicalFixedIborDefinition[] swaptionDefinition = new SwaptionPhysicalFixedIborDefinition[nbTest];
    SwaptionPhysicalFixedIbor[] swaption = new SwaptionPhysicalFixedIbor[nbTest];
    for (int looptest = 0; looptest < nbTest; looptest++) {
      rate[looptest] = startRate + 0.00001 * looptest;
      swapDefinition[looptest] = SwapFixedIborDefinition.from(SETTLEMENT_DATE, CMS_INDEX, NOTIONAL, rate[looptest], FIXED_IS_PAYER);
      swaptionDefinition[looptest] = SwaptionPhysicalFixedIborDefinition.from(EXPIRY_DATE, swapDefinition[looptest], IS_LONG);
      swaption[looptest] = swaptionDefinition[looptest].toDerivative(REFERENCE_DATE, CURVES_NAME);
    }

    CurrencyAmount[] pvPayerLongApproximation = new CurrencyAmount[nbTest];
    double[][][] pvLmmSensi = new double[nbTest][][];
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

    System.out.println("Approximation: " + pvPayerLongApproximation.toString());

    final int nbTest2 = 10;
    YieldAndDiscountCurve dsc = CURVES.getCurve(CURVES_NAME[0]);
    CurrencyAmount[] pvMC = new CurrencyAmount[nbTest];
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
  public void calibratePriceAmortized() {
    Period fixedPaymentPeriod = Period.ofMonths(12);
    Currency ccy = Currency.EUR;
    Period iborTenor = Period.ofMonths(6);
    IborIndex iborIndex = new IborIndex(ccy, iborTenor, SETTLEMENT_DAYS, CALENDAR, IBOR_DAY_COUNT, BUSINESS_DAY, IS_EOM);
    SABRInterestRateParameters sabrParameter = TestsDataSets.createSABR1();
    SABRInterestRateDataBundle sabrBundle = new SABRInterestRateDataBundle(sabrParameter, CURVES);
    int[] swapTenorYear = {1, 2, 3, 4, 5};
    IndexSwap[] cmsIndex = new IndexSwap[swapTenorYear.length];
    for (int loopexp = 0; loopexp < swapTenorYear.length; loopexp++) {
      cmsIndex[loopexp] = new IndexSwap(fixedPaymentPeriod, FIXED_DAY_COUNT, iborIndex, Period.ofYears(swapTenorYear[loopexp]));
    }
    double[] amortization = new double[] {1.00, 0.80, 0.60, 0.40, 0.20}; // For 5Y amortization
    //    double[] amortization = new double[] {1.00, 0.90, 0.80, 0.70, 0.60, 0.50, 0.40, 0.30, 0.20, 0.10}; // For 10Y amortization
    SwapFixedIborDefinition[] swapCalibrationDefinition = new SwapFixedIborDefinition[swapTenorYear.length];
    SwaptionPhysicalFixedIborDefinition[] swaptionCalibrationDefinition = new SwaptionPhysicalFixedIborDefinition[swapTenorYear.length];
    SwaptionPhysicalFixedIbor[] swaptionCalibration = new SwaptionPhysicalFixedIbor[swapTenorYear.length];
    for (int loopexp = 0; loopexp < swapTenorYear.length; loopexp++) {
      swapCalibrationDefinition[loopexp] = SwapFixedIborDefinition.from(SETTLEMENT_DATE, cmsIndex[loopexp], NOTIONAL, RATE, FIXED_IS_PAYER);
      swaptionCalibrationDefinition[loopexp] = SwaptionPhysicalFixedIborDefinition.from(EXPIRY_DATE, swapCalibrationDefinition[loopexp], IS_LONG);
      swaptionCalibration[loopexp] = swaptionCalibrationDefinition[loopexp].toDerivative(REFERENCE_DATE, CURVES_NAME);
    }
    CouponFixed[] cpnFixed = new CouponFixed[swapTenorYear.length];
    AnnuityCouponFixed legFixed = swaptionCalibration[swapTenorYear.length - 1].getUnderlyingSwap().getFixedLeg();
    CouponIbor[] cpnIbor = new CouponIbor[2 * swapTenorYear.length];
    @SuppressWarnings("unchecked")
    GenericAnnuity<Payment> legIbor = (GenericAnnuity<Payment>) swaptionCalibration[swapTenorYear.length - 1].getUnderlyingSwap().getSecondLeg();
    for (int loopexp = 0; loopexp < swapTenorYear.length; loopexp++) {
      cpnFixed[loopexp] = legFixed.getNthPayment(loopexp).withNotional(legFixed.getNthPayment(loopexp).getNotional() * amortization[loopexp]);
      cpnIbor[2 * loopexp] = ((CouponIbor) legIbor.getNthPayment(2 * loopexp)).withNotional(((CouponIbor) legIbor.getNthPayment(2 * loopexp)).getNotional() * amortization[loopexp]);
      cpnIbor[2 * loopexp + 1] = ((CouponIbor) legIbor.getNthPayment(2 * loopexp + 1)).withNotional(((CouponIbor) legIbor.getNthPayment(2 * loopexp + 1)).getNotional() * amortization[loopexp]);
    }

    FixedFloatSwap swapAmortized = new FixedFloatSwap(new AnnuityCouponFixed(cpnFixed), new AnnuityCouponIbor(cpnIbor));
    SwaptionPhysicalFixedIbor swaptionAmortized = SwaptionPhysicalFixedIbor.from(swaptionCalibration[0].getTimeToExpiry(), swapAmortized, swaptionCalibration[0].getSettlementTime(), IS_LONG);

    InstrumentDerivative[] swaptionCalibration2 = swaptionAmortized.calibrationBasket(SwaptionPhysicalFixedIborCalibrationType.FIXEDLEG_STRIKE);

    assertEquals("Calibration basket", swaptionCalibration.length, swaptionCalibration2.length);
    for (int loopcal = 0; loopcal < swaptionCalibration.length; loopcal++) {
      assertEquals("Calibration basket: " + loopcal, METHOD_SABR.presentValue(swaptionCalibration[loopcal], sabrBundle).getAmount(), METHOD_SABR
          .presentValue(swaptionCalibration2[loopcal], sabrBundle).getAmount(), 1.0E-2);
    }
    // Calibration and price
    LiborMarketModelDisplacedDiffusionParameters lmmParameters = LiborMarketModelDisplacedDiffusionTestsDataSet.createLMMParametersDisplacementAngle(REFERENCE_DATE,
        swapCalibrationDefinition[swapTenorYear.length - 1], 0.10, Math.PI / 2);
    SwaptionPhysicalLMMDDCalibrationObjective objective = new SwaptionPhysicalLMMDDCalibrationObjective(lmmParameters);
    SuccessiveRootFinderCalibrationEngine calibrationEngine = new SwaptionPhysicalLMMDDSuccessiveRootFinderCalibrationEngine(objective);
    calibrationEngine.addInstrument(swaptionCalibration2, METHOD_SABR);
    calibrationEngine.calibrate(sabrBundle);
    LiborMarketModelDisplacedDiffusionDataBundle lmmBundle = new LiborMarketModelDisplacedDiffusionDataBundle(lmmParameters, CURVES);
    CurrencyAmount pvAmortized = METHOD_LMM.presentValue(swaptionAmortized, lmmBundle);
    double pvAmortizedPrevious = 3058997.117;
    assertEquals("LMM Amortized pricing", pvAmortizedPrevious, pvAmortized.getAmount(), 1.0E-2);
    // Method
    SwaptionPhysicalFixedIborSABRLMMMethod method = new SwaptionPhysicalFixedIborSABRLMMMethod();
    CurrencyAmount pvAmortizedMethod = method.presentValue(swaptionAmortized, sabrBundle);
    assertEquals("LMM Amortized pricing", pvAmortized.getAmount(), pvAmortizedMethod.getAmount(), 1.0E-2);

    // SABR parameters sensitivity in all-in-one method.
    List<Object> results = method.presentValueCurveSABRSensitivity(swaptionAmortized, sabrBundle);
    InterestRateCurveSensitivity pvcs1 = (InterestRateCurveSensitivity) results.get(1);
    PresentValueSABRSensitivityDataBundle pvss1 = (PresentValueSABRSensitivityDataBundle) results.get(2);

    // SABR parameters sensitivity
    PresentValueSABRSensitivityDataBundle pvss = method.presentValueSABRSensitivity(swaptionAmortized, sabrBundle);

    // SABR parameters sensitivity (all-in-one)
    for (int loopcal = 0; loopcal < swaptionCalibration.length; loopcal++) {
      DoublesPair expiryMaturity = new DoublesPair(swaptionCalibration[loopcal].getTimeToExpiry(), swaptionCalibration[loopcal].getMaturityTime());
      assertEquals("Sensitivity swaption pv to alpha", pvss1.getAlpha().getMap().get(expiryMaturity), pvss.getAlpha().getMap().get(expiryMaturity), 1E-2);
      assertEquals("Sensitivity swaption pv to rho", pvss1.getRho().getMap().get(expiryMaturity), pvss.getRho().getMap().get(expiryMaturity), 1E-2);
      assertEquals("Sensitivity swaption pv to nu", pvss1.getNu().getMap().get(expiryMaturity), pvss.getNu().getMap().get(expiryMaturity), 1E-2);
    }
    // SABR parameters sensitivity (parallel shift check)
    SABRInterestRateParameters sabrParameterShift;
    SABRInterestRateDataBundle sabrBundleShift;
    LiborMarketModelDisplacedDiffusionParameters lmmParametersShift = LiborMarketModelDisplacedDiffusionTestsDataSet.createLMMParametersDisplacementAngle(REFERENCE_DATE,
        swapCalibrationDefinition[swapTenorYear.length - 1], 0.10, Math.PI / 2);
    SwaptionPhysicalLMMDDCalibrationObjective objectiveShift = new SwaptionPhysicalLMMDDCalibrationObjective(lmmParametersShift);
    SuccessiveRootFinderCalibrationEngine calibrationEngineShift = new SwaptionPhysicalLMMDDSuccessiveRootFinderCalibrationEngine(objectiveShift);
    calibrationEngineShift.addInstrument(swaptionCalibration2, METHOD_SABR);
    LiborMarketModelDisplacedDiffusionDataBundle lmmBundleShift = new LiborMarketModelDisplacedDiffusionDataBundle(lmmParametersShift, CURVES);

    double alphaVegaTotalComputed = 0.0;
    assertEquals("Number of alpha sensitivity", pvss.getAlpha().getMap().keySet().size(), swaptionCalibration.length);
    for (int loopcal = 0; loopcal < swaptionCalibration.length; loopcal++) {
      DoublesPair expiryMaturity = new DoublesPair(swaptionCalibration[loopcal].getTimeToExpiry(), swaptionCalibration[loopcal].getMaturityTime());
      alphaVegaTotalComputed += pvss.getAlpha().getMap().get(expiryMaturity);
    }
    double shiftAlpha = 0.00001;
    sabrParameterShift = TestsDataSets.createSABR1AlphaBumped(shiftAlpha);
    sabrBundleShift = new SABRInterestRateDataBundle(sabrParameterShift, CURVES);
    calibrationEngineShift.calibrate(sabrBundleShift);
    CurrencyAmount pvAmortizedShiftAlpha = METHOD_LMM.presentValue(swaptionAmortized, lmmBundleShift);
    double alphaVegaTotalExpected = (pvAmortizedShiftAlpha.getAmount() - pvAmortized.getAmount()) / shiftAlpha;
    assertEquals("Alpha sensitivity value", alphaVegaTotalExpected, alphaVegaTotalComputed, 1.0E+2);

    double rhoVegaTotalComputed = 0.0;
    assertEquals("Number of alpha sensitivity", pvss.getRho().getMap().keySet().size(), swaptionCalibration.length);
    for (int loopcal = 0; loopcal < swaptionCalibration.length; loopcal++) {
      DoublesPair expiryMaturity = new DoublesPair(swaptionCalibration[loopcal].getTimeToExpiry(), swaptionCalibration[loopcal].getMaturityTime());
      rhoVegaTotalComputed += pvss.getRho().getMap().get(expiryMaturity);
    }
    double shiftRho = 0.00001;
    sabrParameterShift = TestsDataSets.createSABR1RhoBumped(shiftRho);
    sabrBundleShift = new SABRInterestRateDataBundle(sabrParameterShift, CURVES);
    calibrationEngineShift.calibrate(sabrBundleShift);
    CurrencyAmount pvAmortizedShiftRho = METHOD_LMM.presentValue(swaptionAmortized, lmmBundleShift);
    double rhoVegaTotalExpected = (pvAmortizedShiftRho.getAmount() - pvAmortized.getAmount()) / shiftRho;
    assertEquals("Rho sensitivity value", rhoVegaTotalExpected, rhoVegaTotalComputed, 1.0E+1);

    double nuVegaTotalComputed = 0.0;
    assertEquals("Number of alpha sensitivity", pvss.getNu().getMap().keySet().size(), swaptionCalibration.length);
    for (int loopcal = 0; loopcal < swaptionCalibration.length; loopcal++) {
      DoublesPair expiryMaturity = new DoublesPair(swaptionCalibration[loopcal].getTimeToExpiry(), swaptionCalibration[loopcal].getMaturityTime());
      nuVegaTotalComputed += pvss.getNu().getMap().get(expiryMaturity);
    }
    double shiftNu = 0.00001;
    sabrParameterShift = TestsDataSets.createSABR1NuBumped(shiftNu);
    sabrBundleShift = new SABRInterestRateDataBundle(sabrParameterShift, CURVES);
    calibrationEngineShift.calibrate(sabrBundleShift);
    CurrencyAmount pvAmortizedShiftNu = METHOD_LMM.presentValue(swaptionAmortized, lmmBundleShift);
    double nuVegaTotalExpected = (pvAmortizedShiftNu.getAmount() - pvAmortized.getAmount()) / shiftNu;
    assertEquals("Nu sensitivity value", nuVegaTotalExpected, nuVegaTotalComputed, 1.0E+1);

    // Curve sensitivity
    InterestRateCurveSensitivity pvcs = method.presentValueCurveSensitivity(swaptionAmortized, sabrBundle);
    pvcs = pvcs.clean();
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
    double shiftCurve = 0.0000001;
    YieldAndDiscountCurve curve5Shift = new YieldCurve(ConstantDoublesCurve.from(0.05 + shiftCurve));
    YieldAndDiscountCurve curve4Shift = new YieldCurve(ConstantDoublesCurve.from(0.04 + shiftCurve));

    YieldCurveBundle curvesDscShift = new YieldCurveBundle();
    curvesDscShift.setCurve(FUNDING_CURVE_NAME, curve5Shift);
    curvesDscShift.setCurve(FORWARD_CURVE_NAME, CURVES.getCurve(FORWARD_CURVE_NAME));
    SABRInterestRateDataBundle sabrBundleCurveDscShift = new SABRInterestRateDataBundle(sabrParameter, curvesDscShift);
    CurrencyAmount pvAmortizedShiftCurveDsc = method.presentValue(swaptionAmortized, sabrBundleCurveDscShift);
    double dscDeltaTotalExpected = (pvAmortizedShiftCurveDsc.getAmount() - pvAmortized.getAmount()) / shiftCurve;
    double dscDeltaTotal = 0.0;
    final List<DoublesPair> dscSensi = pvcs.getSensitivities().get(FUNDING_CURVE_NAME);
    for (int looppay = 0; looppay < dscSensi.size(); looppay++) {
      dscDeltaTotal += dscSensi.get(looppay).second;
    }
    assertEquals("Curve DSC sensitivity value", dscDeltaTotalExpected, dscDeltaTotal, 5.0E+1);

    YieldCurveBundle curvesFwdShift = new YieldCurveBundle();
    curvesFwdShift.setCurve(FUNDING_CURVE_NAME, CURVES.getCurve(FUNDING_CURVE_NAME));
    curvesFwdShift.setCurve(FORWARD_CURVE_NAME, curve4Shift);
    SABRInterestRateDataBundle sabrBundleCurveFwdShift = new SABRInterestRateDataBundle(sabrParameter, curvesFwdShift);
    CurrencyAmount pvAmortizedShiftCurveFwd = method.presentValue(swaptionAmortized, sabrBundleCurveFwdShift);
    double fwdDeltaTotalExpected = (pvAmortizedShiftCurveFwd.getAmount() - pvAmortized.getAmount()) / shiftCurve;
    double fwdDeltaTotal = 0.0;
    final List<DoublesPair> fwdSensi = pvcs.getSensitivities().get(FORWARD_CURVE_NAME);
    for (int looppay = 0; looppay < fwdSensi.size(); looppay++) {
      fwdDeltaTotal += fwdSensi.get(looppay).second;
    }
    assertEquals("Curve FWD sensitivity value", fwdDeltaTotalExpected, fwdDeltaTotal, 2.0E+2);
  }

}
