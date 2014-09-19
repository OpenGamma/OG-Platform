/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.capletstripping;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.index.GeneratorAttributeIR;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIbor;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIborMaster;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexIborMaster;
import com.opengamma.analytics.financial.instrument.payment.CapFloorIborDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapDefinition;
import com.opengamma.analytics.financial.interestrate.datasets.StandardDataSetsMulticurveUSD;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CapFloorIbor;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIbor;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.analytics.financial.interestrate.payments.provider.CouponIborInArrearsSmileModelReplicationMethod;
import com.opengamma.analytics.financial.interestrate.swap.derivative.Swap;
import com.opengamma.analytics.financial.model.volatility.smile.fitting.interpolation.BenaimDodgsonKainthExtrapolationFunctionProvider;
import com.opengamma.analytics.financial.model.volatility.smile.fitting.interpolation.InterpolatedSmileFunction;
import com.opengamma.analytics.financial.model.volatility.smile.fitting.interpolation.ShiftedLogNormalExtrapolationFunctionProvider;
import com.opengamma.analytics.financial.model.volatility.smile.fitting.interpolation.SmileExtrapolationFunctionSABRProvider;
import com.opengamma.analytics.financial.model.volatility.smile.fitting.interpolation.SmileInterpolatorSABRWithExtrapolation;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueDiscountingCalculator;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlockBundle;
import com.opengamma.analytics.financial.provider.description.MulticurveProviderDiscountDataSets;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyParameterSensitivity;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.financial.util.AssertSensitivityObjects;
import com.opengamma.analytics.math.function.DoublesVectorFunctionProvider;
import com.opengamma.analytics.math.function.InterpolatedVectorFunctionProvider;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolator;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.analytics.math.interpolation.GridInterpolator2D;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.analytics.math.interpolation.TransformedInterpolator1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.minimization.DoubleRangeLimitTransform;
import com.opengamma.analytics.math.minimization.ParameterLimitsTransform;
import com.opengamma.analytics.math.minimization.ParameterLimitsTransform.LimitType;
import com.opengamma.analytics.math.minimization.SingleRangeLimitTransform;
import com.opengamma.analytics.math.surface.InterpolatedDoublesSurface;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.tuple.DoublesPair;
import com.opengamma.util.tuple.Pair;

/**
 * Demo for in arrears end-to-end, where we ultimately wish to price an in-arrears caplet/floorlets. This proceeds as follows:
 * <ul>
 * <li>We take a set of market quoted cap/floor values (on the relevant index), and infer the (Black) volatilities of the
 *  underlying caplet/floorlets (aka caplet stripping).</li>
 * <li>This (possible via some interpolation) gives use caplet volatilities (smile) at the expiry of the in-arrears
 *  caplet/floorlet.</li>
 * <li>This smile is fitted with a smile interpolator, and extrapolated beyond the range of (market) strikes, using a
 *  smile extrapolator.</li>
 * <li>Using a static replication augment, the in-arrears price is computed by integration of the price of standard
 *  caplet/floorlets out to a strike of infinity (in practice some large cut-off), where
 * the price is determined from the smile extrapolation.</li>
 * </ul>
 */
@Test(groups = TestGroup.UNIT)
public class CouponInArrearsCalculationTest {

  private static final MulticurveProviderDiscount CURVES = CapletStrippingSetup.getYieldCurves();

  private static final IndexIborMaster MASTER_IBOR_INDEX = IndexIborMaster.getInstance();

  private static final IborIndex USDLIBOR3M = MASTER_IBOR_INDEX.getIndex(IndexIborMaster.USDLIBOR3M);
  private static final Currency USD = USDLIBOR3M.getCurrency();
  private static final Calendar CALENDAR = MulticurveProviderDiscountDataSets.getUSDCalendar();

  // Dates
  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2013, 6, 7);
  private static final ZonedDateTime START_ACCRUAL_DATE = ScheduleCalculator.getAdjustedDate(REFERENCE_DATE, Period.ofYears(9), USDLIBOR3M, CALENDAR);
  private static final ZonedDateTime END_ACCRUAL_DATE = ScheduleCalculator.getAdjustedDate(START_ACCRUAL_DATE, USDLIBOR3M, CALENDAR);
  private static final double ACCRUAL_FACTOR = USDLIBOR3M.getDayCount().getDayCountFraction(START_ACCRUAL_DATE, END_ACCRUAL_DATE, CALENDAR);
  private static final ZonedDateTime FIXING_DATE = ScheduleCalculator.getAdjustedDate(END_ACCRUAL_DATE, -USDLIBOR3M.getSpotLag(), CALENDAR);
  private static final double NOTIONAL = 100000000; // 100m
  private static final double STRIKE = 0.01;
  private static final boolean IS_CAP = true;

  // Definition description: In arrears
  private static final CapFloorIborDefinition CAP_IA_LONG_DEFINITION = new CapFloorIborDefinition(USD, FIXING_DATE, START_ACCRUAL_DATE, END_ACCRUAL_DATE, ACCRUAL_FACTOR, NOTIONAL, FIXING_DATE,
      USDLIBOR3M, STRIKE, IS_CAP, CALENDAR);
  private static final CapFloorIborDefinition FLOOR_IA_SHORT_DEFINITION = new CapFloorIborDefinition(USD, FIXING_DATE,
      START_ACCRUAL_DATE, END_ACCRUAL_DATE, ACCRUAL_FACTOR, -NOTIONAL, FIXING_DATE, USDLIBOR3M, STRIKE, !IS_CAP,
      CALENDAR);
  private static final CouponIborDefinition COUPON_IBOR_IA_DEFINITION = new CouponIborDefinition(USD, FIXING_DATE,
      START_ACCRUAL_DATE, END_ACCRUAL_DATE, ACCRUAL_FACTOR, NOTIONAL, FIXING_DATE, USDLIBOR3M, CALENDAR);

  // To derivative
  private static final CapFloorIbor CAPLET_LONG = (CapFloorIbor) CAP_IA_LONG_DEFINITION.toDerivative(REFERENCE_DATE);
  private static final CapFloorIbor FLOORLET_SHORT = (CapFloorIbor) FLOOR_IA_SHORT_DEFINITION
      .toDerivative(REFERENCE_DATE);
  private static final CouponIbor COUPON_IBOR = (CouponIbor) COUPON_IBOR_IA_DEFINITION.toDerivative(REFERENCE_DATE);

  // Knots chosen to realize "smooth" term structure
  private static final double[] ALPHA_KNOTS = new double[] {1, 2, 3, 5, 7, 10 };
  private static final double[] BETA_KNOTS = new double[] {1 };
  private static final double[] RHO_KNOTS = new double[] {1, 3, 7 };
  private static final double[] NU_KNOTS = new double[] {1, 2, 3, 5, 7, 10 };
  private static final Interpolator1D BASE_INTERPOLATOR;
  private static final ParameterLimitsTransform ALPHA_TRANSFORM;
  private static final ParameterLimitsTransform BETA_TRANSFORM;
  private static final ParameterLimitsTransform RHO_TRANSFORM;
  private static final ParameterLimitsTransform NU_TRANSFORM;

  private static final DoubleMatrix1D SABR_START;
  private static final DoublesVectorFunctionProvider[] s_providers;

  static {
    BASE_INTERPOLATOR = CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.DOUBLE_QUADRATIC, Interpolator1DFactory.LINEAR_EXTRAPOLATOR);
    ALPHA_TRANSFORM = new SingleRangeLimitTransform(0.0, LimitType.GREATER_THAN);
    BETA_TRANSFORM = new DoubleRangeLimitTransform(0.1, 1);
    RHO_TRANSFORM = new DoubleRangeLimitTransform(-1, 1);
    NU_TRANSFORM = new SingleRangeLimitTransform(0.0, LimitType.GREATER_THAN);
    int nAlphaKnots = ALPHA_KNOTS.length;
    int nBetaKnots = BETA_KNOTS.length;
    int nRhoKnots = RHO_KNOTS.length;
    int nNuKnots = NU_KNOTS.length;
    SABR_START = new DoubleMatrix1D(nAlphaKnots + +nBetaKnots + nRhoKnots + nNuKnots);
    double[] temp = new double[nAlphaKnots];
    Arrays.fill(temp, ALPHA_TRANSFORM.transform(0.2));
    System.arraycopy(temp, 0, SABR_START.getData(), 0, nAlphaKnots);
    temp = new double[nBetaKnots];
    Arrays.fill(temp, BETA_TRANSFORM.transform(0.7));
    System.arraycopy(temp, 0, SABR_START.getData(), nAlphaKnots, nBetaKnots);
    temp = new double[nRhoKnots];
    Arrays.fill(temp, RHO_TRANSFORM.transform(-0.2));
    System.arraycopy(temp, 0, SABR_START.getData(), nAlphaKnots + nBetaKnots, nRhoKnots);
    temp = new double[nNuKnots];
    Arrays.fill(temp, NU_TRANSFORM.transform(0.5));
    System.arraycopy(temp, 0, SABR_START.getData(), nAlphaKnots + nBetaKnots + nRhoKnots, nNuKnots);

    InterpolatedVectorFunctionProvider alphaPro = new InterpolatedVectorFunctionProvider(new TransformedInterpolator1D(BASE_INTERPOLATOR, ALPHA_TRANSFORM), ALPHA_KNOTS);
    InterpolatedVectorFunctionProvider betaPro = new InterpolatedVectorFunctionProvider(new TransformedInterpolator1D(CombinedInterpolatorExtrapolatorFactory.getInterpolator(
        Interpolator1DFactory.LINEAR, Interpolator1DFactory.FLAT_EXTRAPOLATOR), BETA_TRANSFORM), BETA_KNOTS);
    InterpolatedVectorFunctionProvider rhoPro = new InterpolatedVectorFunctionProvider(new TransformedInterpolator1D(BASE_INTERPOLATOR, RHO_TRANSFORM), RHO_KNOTS);
    InterpolatedVectorFunctionProvider nuPro = new InterpolatedVectorFunctionProvider(new TransformedInterpolator1D(BASE_INTERPOLATOR, NU_TRANSFORM), NU_KNOTS);

    s_providers = new DoublesVectorFunctionProvider[] {alphaPro, betaPro, rhoPro, nuPro };
  }

  /*
   * Swap example
   */
  private static final Calendar NYC = StandardDataSetsMulticurveUSD.calendarArray()[0];
  private static final GeneratorSwapFixedIborMaster GENERATOR_SWAP_FIXED_IBOR_MASTER = GeneratorSwapFixedIborMaster
      .getInstance();
  private static final GeneratorSwapFixedIbor USD6MLIBOR1M = GENERATOR_SWAP_FIXED_IBOR_MASTER.getGenerator(
      "USD6MLIBOR1M", NYC);
  private static final ZonedDateTime TRADE_DATE_1M = DateUtils.getUTCDate(2014, 9, 10);
  private static final double FIXED_RATE_1M = 0.0125;
  private static final Period TENOR_SWAP_1M = Period.ofYears(2);
  private static final GeneratorAttributeIR ATTRIBUTE_1M = new GeneratorAttributeIR(TENOR_SWAP_1M);
  private static final ZonedDateTime VALUATION_DATE = DateUtils.getUTCDate(2014, 1, 22);
  private static final SwapDefinition SWAP_FIXED_1M_DEFINITION =
      USD6MLIBOR1M.generateInstrument(TRADE_DATE_1M, FIXED_RATE_1M, NOTIONAL, ATTRIBUTE_1M);
  private static final Swap<? extends Payment, ? extends Payment> SWAP_FIXED_1M = SWAP_FIXED_1M_DEFINITION
      .toDerivative(VALUATION_DATE);
  private static final Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> MULTICURVE_OIS_PAIR =
      StandardDataSetsMulticurveUSD.getCurvesUSDOisL1L3L6();
  private static final MulticurveProviderDiscount MULTICURVE_OIS = MULTICURVE_OIS_PAIR.getFirst();
  private static final CurveBuildingBlockBundle BLOCK_OIS = MULTICURVE_OIS_PAIR.getSecond();

  private static final PresentValueDiscountingCalculator PVDC = PresentValueDiscountingCalculator.getInstance();

  /**
   * Minimal consistency test for subsequent demos
   */
  @Test
  public void consistencyCapCouponTest() {
    List<CapFloor> caps = CapletStrippingSetup.getAllCapsExATM();
    double[] capVols = CapletStrippingSetup.getAllCapVolsExATM();

    MultiCapFloorPricerGrid pricer = new MultiCapFloorPricerGrid(caps, CURVES);
    double lambda = 1.0;
    double oneBP = 1e-4;
    int nCaps = caps.size();
    double[] errors = new double[nCaps];
    Arrays.fill(errors, oneBP);
    double avVol = 0.0;
    for (int i = 0; i < nCaps; i++) {
      avVol += capVols[i];
    }
    avVol /= nCaps;
    DoubleMatrix1D guess = new DoubleMatrix1D(pricer.getGridSize(), avVol); // set the initial guess equal to the average cap vol

    CapletStripperDirect stripper = new CapletStripperDirect(pricer, lambda);
    CouponInArrearsCalculation cal = new CouponInArrearsCalculation(stripper, caps, capVols, MarketDataType.VOL,
        errors, guess, CURVES);

    double stdPv = cal.simpleCapletPrice(CAPLET_LONG) * CAPLET_LONG.getNotional();
    ShiftedLogNormalExtrapolationFunctionProvider provider = new ShiftedLogNormalExtrapolationFunctionProvider(
        "Quiet");
    SmileInterpolatorSABRWithExtrapolation sabrExtrap = new SmileInterpolatorSABRWithExtrapolation(provider);
    double pv = cal.presentValue(CAPLET_LONG, sabrExtrap).getAmount(USD);
    assertTrue(stdPv < pv);

    //IBOR
    SmileInterpolatorSABRWithExtrapolation sabrExtrap1 = new SmileInterpolatorSABRWithExtrapolation(provider);
    double pvIbor = cal.presentValue(COUPON_IBOR, sabrExtrap1).getAmount(USD);

    // underlying computation
    CapletStrippingResult result = stripper.solve(capVols, MarketDataType.VOL, errors, guess);
    DoublesPair[] expStrikes = result.getPricer().getExpiryStrikeArray();
    DoubleMatrix1D vols = result.getCapletVols();
    CombinedInterpolatorExtrapolator interpolator = CombinedInterpolatorExtrapolatorFactory.getInterpolator(
        Interpolator1DFactory.LINEAR, Interpolator1DFactory.LINEAR_EXTRAPOLATOR);
    GridInterpolator2D interpolator2D = new GridInterpolator2D(interpolator, interpolator);
    InterpolatedDoublesSurface volSurface = new InterpolatedDoublesSurface(expStrikes, vols.getData(), interpolator2D);
    double expiry = COUPON_IBOR.getFixingTime();
    double[] sampleStrikes = result.getPricer().getStrikes();
    int nStrikes = sampleStrikes.length;
    double[] sampleVols = new double[nStrikes];
    for (int i = 0; i < nStrikes; i++) {
      sampleVols[i] = volSurface.getZValue(expiry, sampleStrikes[i]);
    }
    double forward = CURVES.getSimplyCompoundForwardRate(COUPON_IBOR.getIndex(),
        COUPON_IBOR.getFixingPeriodStartTime(), COUPON_IBOR.getFixingPeriodEndTime(),
        COUPON_IBOR.getFixingAccrualFactor());
    // reconstruct smile interpolation -- next random tends to result in different SBAR parameters
    SmileInterpolatorSABRWithExtrapolation interp = new SmileInterpolatorSABRWithExtrapolation(provider);
    InterpolatedSmileFunction smileFunction = new InterpolatedSmileFunction(interp, forward, sampleStrikes,
        expiry, sampleVols);
    CouponIborInArrearsSmileModelReplicationMethod inArrearsCal = new CouponIborInArrearsSmileModelReplicationMethod(
        smileFunction);
    double pvIborRe = inArrearsCal.presentValue(COUPON_IBOR, CURVES).getAmount(USD);
    assertEquals(pvIborRe, pvIbor, pvIborRe * 1.e-12);
  }

  /**
   * BDK can not be used if forward is outside strike range
   */
  @Test
  public void consistencySwapTest() {
    List<CapFloor> caps = CapletStrippingSetup.getAllCapsExATM();
    double[] capVols = CapletStrippingSetup.getAllCapVolsExATM();
    MultiCapFloorPricerGrid pricer = new MultiCapFloorPricerGrid(caps, MULTICURVE_OIS);

    // Setting up errors and guess values
    double oneBP = 1e-4;
    int nCaps = caps.size();
    double[] errors = new double[nCaps];
    Arrays.fill(errors, oneBP);
    double avVol = 0.0;
    for (int i = 0; i < nCaps; i++) {
      avVol += capVols[i];
    }
    avVol /= nCaps;
    DoubleMatrix1D guess = new DoubleMatrix1D(pricer.getGridSize(), avVol); // set the initial guess equal to the average cap vol

    /*
     * lambda = 0.01
     */
    double lambda = 0.01;
    CapletStripperDirect stripper = new CapletStripperDirect(pricer, lambda);
    CouponInArrearsCalculation cal = new CouponInArrearsCalculation(stripper, caps, capVols, MarketDataType.VOL,
        errors, guess, MULTICURVE_OIS);
    PresentValueDiscountingCalculator pvdc = PresentValueDiscountingCalculator.getInstance();

    double mu = 2.25;
    BenaimDodgsonKainthExtrapolationFunctionProvider provider = new BenaimDodgsonKainthExtrapolationFunctionProvider(
        mu, mu);
    SmileInterpolatorSABRWithExtrapolation sabrExtrapQuiet = new SmileInterpolatorSABRWithExtrapolation(provider);

    MultipleCurrencyAmount firstPV = SWAP_FIXED_1M.getFirstLeg().accept(pvdc, MULTICURVE_OIS);
    MultipleCurrencyAmount secondPV = cal.presentValue((CouponIbor) SWAP_FIXED_1M.getSecondLeg().getNthPayment(0),
        sabrExtrapQuiet);
    for (int j = 1; j < SWAP_FIXED_1M.getSecondLeg().getNumberOfPayments(); j++) {
      secondPV = secondPV.plus(cal.presentValue((CouponIbor) SWAP_FIXED_1M.getSecondLeg().getNthPayment(j),
          sabrExtrapQuiet));
    }
    MultipleCurrencyAmount ref = firstPV.plus(secondPV);

    SmileInterpolatorSABRWithExtrapolation sabrExtrapQuiet1 = new SmileInterpolatorSABRWithExtrapolation(provider);
    MultipleCurrencyAmount res1 = cal.presentValue(SWAP_FIXED_1M, sabrExtrapQuiet1);

    Swap<? extends Payment, ? extends Payment> swap = new Swap<>(
        SWAP_FIXED_1M.getSecondLeg(), SWAP_FIXED_1M.getFirstLeg());
    SmileInterpolatorSABRWithExtrapolation sabrExtrapQuiet2 = new SmileInterpolatorSABRWithExtrapolation(provider);
    MultipleCurrencyAmount res2 = cal.presentValue(swap, sabrExtrapQuiet2);

    assertEquals(ref.getAmount(USD), res1.getAmount(USD), Math.abs(ref.getAmount(USD)) * 1.0e-12);
    assertEquals(ref.getAmount(USD), res2.getAmount(USD), Math.abs(ref.getAmount(USD)) * 1.0e-12);

    SmileInterpolatorSABRWithExtrapolation sabrExtrapQuiet3 = new SmileInterpolatorSABRWithExtrapolation(provider);
    double parRate = cal.parRate(SWAP_FIXED_1M, sabrExtrapQuiet3);
    double firstPVMod = firstPV.getAmount(USD) * parRate / 0.0125;
    assertTrue(Math.abs((secondPV.getAmount(USD) + firstPVMod)) < Math.abs(ref.getAmount(USD)) * 1.e-10);

    SmileInterpolatorSABRWithExtrapolation sabrExtrapQuiet4 = new SmileInterpolatorSABRWithExtrapolation(provider);
    MultipleCurrencyParameterSensitivity sense = cal.presentValueCurveSensitivity(SWAP_FIXED_1M, BLOCK_OIS,
        sabrExtrapQuiet4).multipliedBy(oneBP);
    assertEquals(sense.getAllNamesCurrency().iterator().next().getSecond(), USD);

    SmileInterpolatorSABRWithExtrapolation sabrExtrapQuiet5 = new SmileInterpolatorSABRWithExtrapolation(provider);
    MultipleCurrencyParameterSensitivity sense1 = cal.presentValueCurveSensitivity(swap, BLOCK_OIS,
        sabrExtrapQuiet5).multipliedBy(oneBP);
    AssertSensitivityObjects.assertEquals("two identical swaps", sense, sense1, 1.0e-10);
  }

  /**
   * BDK can not be used if forward is outside strike range
   */
  @Test(description = "demo test", enabled = false)
  public void SwapExampleTest() {
    System.out.println("***Global Direct stripper***");
    List<CapFloor> caps = CapletStrippingSetup.getAllCapsExATM();
    double[] capVols = CapletStrippingSetup.getAllCapVolsExATM();
    MultiCapFloorPricerGrid pricer = new MultiCapFloorPricerGrid(caps, MULTICURVE_OIS);

    // Setting up errors and guess values
    double oneBP = 1e-4;
    int nCaps = caps.size();
    double[] errors = new double[nCaps];
    Arrays.fill(errors, oneBP);
    double avVol = 0.0;
    for (int i = 0; i < nCaps; i++) {
      avVol += capVols[i];
    }
    avVol /= nCaps;
    DoubleMatrix1D guess = new DoubleMatrix1D(pricer.getGridSize(), avVol); // set the initial guess equal to the average cap vol

    /*
     * lambda = 0.02
     */
    System.out.println("<<lambda = 0.02>>");
    double lambda = 0.02; // this is chosen to give a chi2/DoF of around 1
    CapletStripperDirect stripper = new CapletStripperDirect(pricer, lambda);
    CouponInArrearsCalculation cal = new CouponInArrearsCalculation(stripper, caps, capVols, MarketDataType.VOL,
        errors, guess, MULTICURVE_OIS);
    System.out.println("Chi2: " + cal.getChiSq());
    System.out.println("Time for stripping :" + cal.getTime() + "s");
    System.out.println();

    for (int i = 0; i < 11; i++) {
      double mu = 1.0 + 0.25 * i;
      BenaimDodgsonKainthExtrapolationFunctionProvider provider = new BenaimDodgsonKainthExtrapolationFunctionProvider(
          mu, mu);
      SmileInterpolatorSABRWithExtrapolation sabrExtrapQuiet = new SmileInterpolatorSABRWithExtrapolation(provider);
      MultipleCurrencyAmount pv = cal.presentValue(SWAP_FIXED_1M, sabrExtrapQuiet);
      SmileInterpolatorSABRWithExtrapolation sabrExtrapQuiet1 = new SmileInterpolatorSABRWithExtrapolation(provider);
      Double parRate = cal.parRate(SWAP_FIXED_1M, sabrExtrapQuiet1);
      System.out.println("mu: " + mu + ", pv: " + pv + ", parRate: " + parRate);
    }
    System.out.println();

    /*
     * lambda = 1.0
     */
    System.out.println("<<lambda = 1.0>>");
    double lambdaLarge = 1.0;
    CapletStripperDirect stripperLarge = new CapletStripperDirect(pricer, lambdaLarge);
    CouponInArrearsCalculation calLarge = new CouponInArrearsCalculation(stripperLarge, caps, capVols,
        MarketDataType.VOL, errors, guess, MULTICURVE_OIS);
    System.out.println("Chi2: " + calLarge.getChiSq());
    System.out.println("Time for stripping :" + calLarge.getTime() + "s");
    System.out.println();

    for (int i = 0; i < 11; i++) {
      double mu = 1.0 + 0.25 * i;
      BenaimDodgsonKainthExtrapolationFunctionProvider provider = new BenaimDodgsonKainthExtrapolationFunctionProvider(
          mu, mu);
      SmileInterpolatorSABRWithExtrapolation sabrExtrapQuiet = new SmileInterpolatorSABRWithExtrapolation(provider);
      MultipleCurrencyAmount pv = calLarge.presentValue(SWAP_FIXED_1M, sabrExtrapQuiet);
      SmileInterpolatorSABRWithExtrapolation sabrExtrapQuiet1 = new SmileInterpolatorSABRWithExtrapolation(provider);
      Double parRate = calLarge.parRate(SWAP_FIXED_1M, sabrExtrapQuiet1);
      System.out.println("mu: " + mu + ", pv: " + pv + ", parRate: " + parRate);
    }
    System.out.println();
  }
  
  
  
  /**
   * Use CapletStripperDirect as the caplet stripper, then model the smile with a SABR interpolator and
   * Benaim-Dodgson-Kainth extrapolation
   */
  @Test(description = "demo test", enabled = false)
  public void capletStrippingDirectBDKTest() {
    System.out.println("***Global Direct stripper with local SABR + Benaim-Dodgson-Kainth***");
    System.out.println();

    List<CapFloor> caps = CapletStrippingSetup.getAllCapsExATM();
    double[] capVols = CapletStrippingSetup.getAllCapVolsExATM();
    // ATMs are not consistent absolute strike caps, thus excluded
    //    List<CapFloor> caps = CapletStrippingSetup.getAllCaps();
    //    double[] capVols = CapletStrippingSetup.getAllCapVols();
    MultiCapFloorPricerGrid pricer = new MultiCapFloorPricerGrid(caps, CURVES);

    // Setting up errors and guess values
    double oneBP = 1e-4;
    int nCaps = caps.size();
    double[] errors = new double[nCaps];
    Arrays.fill(errors, oneBP);
    double avVol = 0.0;
    for (int i = 0; i < nCaps; i++) {
      avVol += capVols[i];
    }
    avVol /= nCaps;
    DoubleMatrix1D guess = new DoubleMatrix1D(pricer.getGridSize(), avVol); // set the initial guess equal to the average cap vol

    /*
     * lambda = 0.03
     */
    System.out.println("<<lambda = 0.03>>");
    double lambda = 0.03; // this is chosen to give a chi2/DoF of around 1
    CapletStripperDirect stripper = new CapletStripperDirect(pricer, lambda);
    CouponInArrearsCalculation cal = new CouponInArrearsCalculation(stripper, caps, capVols, MarketDataType.VOL,
        errors, guess, CURVES);
    System.out.println("Chi2: " + cal.getChiSq());
    System.out.println("Time for stripping :" + cal.getTime() + "s");
    System.out.println();

    for (CapFloorIbor caplet : new CapFloorIbor[] {CAPLET_LONG, FLOORLET_SHORT }) {
      System.out.println("isCap: " + caplet.isCap());
      System.out.println("non corrected price: " + cal.simpleCapletPrice(caplet) * caplet.getNotional());
      for (int i = 0; i < 11; i++) {
        double mu = 1.0 + 0.25 * i;
        BenaimDodgsonKainthExtrapolationFunctionProvider provider = new BenaimDodgsonKainthExtrapolationFunctionProvider(
            mu, mu);
        SmileInterpolatorSABRWithExtrapolation sabrExtrap = new SmileInterpolatorSABRWithExtrapolation(provider);
        double pv = cal.presentValue(caplet, sabrExtrap).getAmount(USD);
        System.out.println("mu: " + mu + ", pv: " + pv);
      }
      System.out.println();
    }
    System.out.println();

    // increase lambda to get a smoother (but less we fitting) caplet vol surface. Ultimately, the choice of lambda has
    // less effect than the choice of mu
    /*
     * lambda = 1.0
     */
    System.out.println("<<lambda = 1.0>>");
    lambda = 1.0;
    stripper = new CapletStripperDirect(pricer, lambda);
    cal = new CouponInArrearsCalculation(stripper, caps, capVols, MarketDataType.VOL, errors, guess, CURVES);
    System.out.println("Chi2: " + cal.getChiSq());
    System.out.println("Time for stripping :" + cal.getTime() + "s");
    System.out.println();

    for (CapFloorIbor caplet : new CapFloorIbor[] {CAPLET_LONG, FLOORLET_SHORT }) {
      System.out.println("isCap: " + caplet.isCap());
      System.out.println("non corrected price: " + cal.simpleCapletPrice(caplet) * caplet.getNotional());
      for (int i = 0; i < 11; i++) {
        double mu = 1.0 + 0.25 * i;
        BenaimDodgsonKainthExtrapolationFunctionProvider provider = new BenaimDodgsonKainthExtrapolationFunctionProvider(
            mu, mu);
        SmileInterpolatorSABRWithExtrapolation sabrExtrap = new SmileInterpolatorSABRWithExtrapolation(provider);
        double pv = cal.presentValue(caplet, sabrExtrap).getAmount(USD);
        System.out.println("mu: " + mu + ", pv: " + pv);
      }
      System.out.println();
    }
    System.out.println("\n");
  }

  /**
   * Use CapletStripperSABR as the caplet stripper, then model the smile with a SABR interpolator and
   * Benaim-Dodgson-Kainth extrapolation.
   * <p>
   * Note, even though a full SABR term structure surface is formed in the caplet stripping stage, this information is thrown away, and new SABR fits (as part of the SABR interpolation) are made.
   */
  @Test(description = "demo test", enabled = false)
  public void capletStrippingSABRBDKTest() {
    System.out.println("***Interpolated SABR parameter stripper with local SABR + Benaim-Dodgson-Kainth***");

    List<CapFloor> caps = CapletStrippingSetup.getAllCapsExATM();
    double[] capVols = CapletStrippingSetup.getAllCapVolsExATM();
    //    List<CapFloor> caps = CapletStrippingSetup.getAllCaps();
    //    double[] capVols = CapletStrippingSetup.getAllCapVols();
    double oneBP = 1e-4;
    int nCaps = caps.size();

    MultiCapFloorPricer pricer = new MultiCapFloorPricer(caps, CURVES);

    double[] errors = new double[nCaps];
    Arrays.fill(errors, oneBP); // 1bps
    CapletStripper stripper = new CapletStripperSABRModel(pricer, s_providers);

    CouponInArrearsCalculation cal = new CouponInArrearsCalculation(stripper, caps, capVols, MarketDataType.VOL,
        errors, SABR_START, CURVES);
    System.out.println("Chi2: " + cal.getChiSq());
    System.out.println("Time for stripping :" + cal.getTime() + "s");
    System.out.println();

    for (CapFloorIbor caplet : new CapFloorIbor[] {CAPLET_LONG, FLOORLET_SHORT }) {
      System.out.println("isCap: " + caplet.isCap());
      System.out.println("non corrected price: " + cal.simpleCapletPrice(caplet) * caplet.getNotional());
    for (int i = 0; i < 11; i++) {
      double mu = 1.0 + 0.25 * i;
      BenaimDodgsonKainthExtrapolationFunctionProvider provider = new BenaimDodgsonKainthExtrapolationFunctionProvider(mu, mu);
      SmileInterpolatorSABRWithExtrapolation sabrExtrap = new SmileInterpolatorSABRWithExtrapolation(provider);
      double pv = cal.presentValue(caplet, sabrExtrap).getAmount(USD);
      System.out.println("mu: " + mu + ", pv: " + pv);
    }
      System.out.println();
    }
    System.out.println("\n");
  }
  
  /**
   * Global Direct stripper with local SABR + shifted lognormal
   */
  @Test(description = "demo test", enabled = false)
  public void capletStrippingDirectSLNTest() {
    System.out.println("***Global Direct stripper with local SABR + shifted lognormal***");

    List<CapFloor> caps = CapletStrippingSetup.getAllCapsExATM();
    double[] capVols = CapletStrippingSetup.getAllCapVolsExATM();
    //    List<CapFloor> caps = CapletStrippingSetup.getAllCaps();
    //    double[] capVols = CapletStrippingSetup.getAllCapVols();

    MultiCapFloorPricerGrid pricer = new MultiCapFloorPricerGrid(caps, CURVES);

    // Setting up errors and guess values
    double oneBP = 1e-4;
    int nCaps = caps.size();
    double[] errors = new double[nCaps];
    Arrays.fill(errors, oneBP);
    double avVol = 0.0;
    for (int i = 0; i < nCaps; i++) {
      avVol += capVols[i];
    }
    avVol /= nCaps;
    DoubleMatrix1D guess = new DoubleMatrix1D(pricer.getGridSize(), avVol); // set the initial guess equal to the average cap vol

    /*
     * lambda = 0.03
     */
    System.out.println("<<lambda = 0.03>>");
    double lambda = 0.03; // this is chosen to give a chi2/DoF of around 1
    CapletStripperDirect stripper = new CapletStripperDirect(pricer, lambda);

    CouponInArrearsCalculation cal = new CouponInArrearsCalculation(stripper, caps, capVols, MarketDataType.VOL,
        errors, guess, CURVES);
    System.out.println("Chi2: " + cal.getChiSq());
    System.out.println("Time for stripping :" + cal.getTime() + "s");
    System.out.println();

    for (CapFloorIbor caplet : new CapFloorIbor[] {CAPLET_LONG, FLOORLET_SHORT }) {
      System.out.println("isCap: " + caplet.isCap());
      System.out.println("non corrected price: " + cal.simpleCapletPrice(caplet) * caplet.getNotional());

    ShiftedLogNormalExtrapolationFunctionProvider provider = new ShiftedLogNormalExtrapolationFunctionProvider(
        "Quiet");
    SmileInterpolatorSABRWithExtrapolation sabrExtrap = new SmileInterpolatorSABRWithExtrapolation(provider);
    double pv = cal.presentValue(caplet, sabrExtrap).getAmount(USD);
    System.out.println("pv(Quiet): " + pv);

    provider = new ShiftedLogNormalExtrapolationFunctionProvider("Flat");
    sabrExtrap = new SmileInterpolatorSABRWithExtrapolation(provider);
    pv = cal.presentValue(caplet, sabrExtrap).getAmount(USD);
    System.out.println("pv(Flat): " + pv);
    System.out.println();
    }
    System.out.println();

    /*
     * lambda = 1.0
     */
    System.out.println("<<lambda = 1.0>>");
    lambda = 1.0;
    stripper = new CapletStripperDirect(pricer, lambda);
    cal = new CouponInArrearsCalculation(stripper, caps, capVols, MarketDataType.VOL, errors, guess, CURVES);
    System.out.println("Chi2: " + cal.getChiSq());
    System.out.println("Time for stripping :" + cal.getTime() + "s");
    System.out.println();

    for (CapFloorIbor caplet : new CapFloorIbor[] {CAPLET_LONG, FLOORLET_SHORT }) {
      System.out.println("isCap: " + caplet.isCap());
      System.out.println("non corrected price: " + cal.simpleCapletPrice(caplet) * caplet.getNotional());

      ShiftedLogNormalExtrapolationFunctionProvider provider = new ShiftedLogNormalExtrapolationFunctionProvider(
          "Quiet");
      SmileInterpolatorSABRWithExtrapolation sabrExtrap = new SmileInterpolatorSABRWithExtrapolation(provider);
      double pv = cal.presentValue(caplet, sabrExtrap).getAmount(USD);
    System.out.println("pv(Quiet): " + pv);

    provider = new ShiftedLogNormalExtrapolationFunctionProvider("Flat");
    sabrExtrap = new SmileInterpolatorSABRWithExtrapolation(provider);
    pv = cal.presentValue(caplet, sabrExtrap).getAmount(USD);
    System.out.println("pv(Flat): " + pv);
      System.out.println();
    }
    System.out.println("\n");
  }

  /**
   * Interpolated SABR parameter stripper with local SABR + shifted lognormal
   */
  @Test(description = "demo test", enabled = false)
  public void capletStrippingSABRSLNTest() {
    System.out.println("***Interpolated SABR parameter stripper with local SABR + shifted lognormal***");

    List<CapFloor> caps = CapletStrippingSetup.getAllCapsExATM();
    double[] capVols = CapletStrippingSetup.getAllCapVolsExATM();
    //    List<CapFloor> caps = CapletStrippingSetup.getAllCaps();
    //    double[] capVols = CapletStrippingSetup.getAllCapVols();
    MultiCapFloorPricer pricer = new MultiCapFloorPricer(caps, CURVES);

    double oneBP = 1e-4;
    int nCaps = caps.size();
    double[] errors = new double[nCaps];
    Arrays.fill(errors, oneBP); // 1bps


    CapletStripper stripper = new CapletStripperSABRModel(pricer, s_providers);
    CouponInArrearsCalculation cal = new CouponInArrearsCalculation(stripper, caps, capVols, MarketDataType.VOL,
        errors, SABR_START, CURVES);
    System.out.println("Chi2: " + cal.getChiSq());
    System.out.println("Time for stripping :" + cal.getTime() + "s");
    System.out.println();

    for (CapFloorIbor caplet : new CapFloorIbor[] {CAPLET_LONG, FLOORLET_SHORT }) {
      System.out.println("isCap: " + caplet.isCap());
      System.out.println("non corrected price: " + cal.simpleCapletPrice(caplet) * caplet.getNotional());

    ShiftedLogNormalExtrapolationFunctionProvider provider = new ShiftedLogNormalExtrapolationFunctionProvider(
        "Quiet");
    SmileInterpolatorSABRWithExtrapolation sabrExtrap = new SmileInterpolatorSABRWithExtrapolation(provider);
    double pv = cal.presentValue(caplet, sabrExtrap).getAmount(USD);
    System.out.println("pv(Quiet): " + pv);

    provider = new ShiftedLogNormalExtrapolationFunctionProvider("Flat");
    sabrExtrap = new SmileInterpolatorSABRWithExtrapolation(provider);
    pv = cal.presentValue(caplet, sabrExtrap).getAmount(USD);
    System.out.println("pv(Flat): " + pv);
      System.out.println();
    }
    System.out.println("\n");
  }

  /**
   * All cases for COUPON_IBOR with direct stripping
   */
  @Test(description = "demo test", enabled = false)
  public void couponIborDirectAllTest() {
    System.out.println("***Global Direct stripper***");
    CouponIbor couponIbor = COUPON_IBOR;
    List<CapFloor> caps = CapletStrippingSetup.getAllCapsExATM();
    double[] capVols = CapletStrippingSetup.getAllCapVolsExATM();
    MultiCapFloorPricerGrid pricer = new MultiCapFloorPricerGrid(caps, CURVES);

    // Setting up errors and guess values
    double oneBP = 1e-4;
    int nCaps = caps.size();
    double[] errors = new double[nCaps];
    Arrays.fill(errors, oneBP);
    double avVol = 0.0;
    for (int i = 0; i < nCaps; i++) {
      avVol += capVols[i];
    }
    avVol /= nCaps;
    DoubleMatrix1D guess = new DoubleMatrix1D(pricer.getGridSize(), avVol); // set the initial guess equal to the average cap vol

    /*
     * lambda = 0.03
     */
    System.out.println("<<lambda = 0.03>>");
    double lambda = 0.03; // this is chosen to give a chi2/DoF of around 1
    CapletStripperDirect stripper = new CapletStripperDirect(pricer, lambda);
    CouponInArrearsCalculation cal = new CouponInArrearsCalculation(stripper, caps, capVols, MarketDataType.VOL,
        errors, guess, CURVES);
    System.out.println("Chi2: " + cal.getChiSq());
    System.out.println("Time for stripping :" + cal.getTime() + "s");
    System.out.println();

    /*
     * lambda = 1.0
     */
    System.out.println("<<lambda = 1.0>>");
    double lambdaLarge = 1.0;
    CapletStripperDirect stripperLarge = new CapletStripperDirect(pricer, lambdaLarge);
    CouponInArrearsCalculation calLarge = new CouponInArrearsCalculation(stripperLarge, caps, capVols,
        MarketDataType.VOL, errors, guess, CURVES);
    System.out.println("Chi2: " + calLarge.getChiSq());
    System.out.println("Time for stripping :" + calLarge.getTime() + "s");
    System.out.println();
    SmileExtrapolationFunctionSABRProvider provider;

    System.out.println("*With local SABR + BDK*");
    System.out.println("<<lambda = 0.03>>");
    for (int i = 0; i < 11; i++) {
      double mu = 1.0 + 0.25 * i;
      provider = new BenaimDodgsonKainthExtrapolationFunctionProvider(mu, mu);
      SmileInterpolatorSABRWithExtrapolation sabrExtrap = new SmileInterpolatorSABRWithExtrapolation(provider);
      double pv = cal.presentValue(couponIbor, sabrExtrap).getAmount(USD);
      System.out.println("mu: " + mu + ", pv: " + pv);
    }
    System.out.println();
    System.out.println("<<lambda = 1.0>>");
    for (int i = 0; i < 11; i++) {
      double mu = 1.0 + 0.25 * i;
      provider = new BenaimDodgsonKainthExtrapolationFunctionProvider(mu, mu);
      SmileInterpolatorSABRWithExtrapolation sabrExtrap = new SmileInterpolatorSABRWithExtrapolation(provider);
      double pv = calLarge.presentValue(couponIbor, sabrExtrap).getAmount(USD);
      System.out.println("mu: " + mu + ", pv: " + pv);
    }
    System.out.println();

    System.out.println("*With local SABR + shifted lognormal*");
    System.out.println("<<lambda = 0.03>>");
    ShiftedLogNormalExtrapolationFunctionProvider providerQuiet = new ShiftedLogNormalExtrapolationFunctionProvider(
        "Quiet");
    SmileInterpolatorSABRWithExtrapolation sabrExtrapQuiet = new SmileInterpolatorSABRWithExtrapolation(providerQuiet);
    double pv = cal.presentValue(couponIbor, sabrExtrapQuiet).getAmount(USD);
    System.out.println("pv(Quiet): " + pv);
    ShiftedLogNormalExtrapolationFunctionProvider providerFlat = new ShiftedLogNormalExtrapolationFunctionProvider(
        "Flat");
    SmileInterpolatorSABRWithExtrapolation sabrExtrapFlat = new SmileInterpolatorSABRWithExtrapolation(providerFlat);
    pv = cal.presentValue(couponIbor, sabrExtrapFlat).getAmount(USD);
    System.out.println("pv(Flat): " + pv);
    System.out.println();
    System.out.println("<<lambda = 1.0>>");
    pv = calLarge.presentValue(couponIbor, sabrExtrapQuiet).getAmount(USD);
    System.out.println("pv(Quiet): " + pv);
    pv = calLarge.presentValue(couponIbor, sabrExtrapFlat).getAmount(USD);
    System.out.println("pv(Flat): " + pv);
    System.out.println();

  }

  /**
   * All cases for COUPON_IBOR with interpolated SABR parameter strpping
   */
  @Test(description = "demo test", enabled = false)
  public void couponIborSABRParameterAllTest() {
    CouponIbor couponIbor = COUPON_IBOR;

    System.out.println("***Interpolated SABR parameter stripper***");
    List<CapFloor> caps = CapletStrippingSetup.getAllCapsExATM();
    double[] capVols = CapletStrippingSetup.getAllCapVolsExATM();
    //    List<CapFloor> caps = CapletStrippingSetup.getAllCaps();
    //    double[] capVols = CapletStrippingSetup.getAllCapVols();
    double oneBP = 1e-4;
    int nCaps = caps.size();

    MultiCapFloorPricer pricer = new MultiCapFloorPricer(caps, CURVES);

    double[] errors = new double[nCaps];
    Arrays.fill(errors, oneBP); // 1bps
    CapletStripper stripper = new CapletStripperSABRModel(pricer, s_providers);

    CouponInArrearsCalculation cal = new CouponInArrearsCalculation(stripper, caps, capVols, MarketDataType.VOL,
        errors, SABR_START, CURVES);
    System.out.println("Chi2: " + cal.getChiSq());
    System.out.println("Time for stripping :" + cal.getTime() + "s");
    System.out.println();

    SmileExtrapolationFunctionSABRProvider provider;
    System.out.println("*With local SABR + BDK*");
    for (int i = 0; i < 11; i++) {
      double mu = 1.0 + 0.25 * i;
      provider = new BenaimDodgsonKainthExtrapolationFunctionProvider(mu, mu);
      SmileInterpolatorSABRWithExtrapolation sabrExtrap = new SmileInterpolatorSABRWithExtrapolation(provider);
      double pv = cal.presentValue(couponIbor, sabrExtrap).getAmount(USD);
      System.out.println("mu: " + mu + ", pv: " + pv);
    }
    System.out.println();

    System.out.println("*With local SABR + shifted lognormal*");
    provider = new ShiftedLogNormalExtrapolationFunctionProvider(
        "Quiet");
    SmileInterpolatorSABRWithExtrapolation sabrExtrap = new SmileInterpolatorSABRWithExtrapolation(provider);
    double pv = cal.presentValue(couponIbor, sabrExtrap).getAmount(USD);
    System.out.println("pv(Quiet): " + pv);
    provider = new ShiftedLogNormalExtrapolationFunctionProvider("Flat");
    sabrExtrap = new SmileInterpolatorSABRWithExtrapolation(provider);
    pv = cal.presentValue(couponIbor, sabrExtrap).getAmount(USD);
    System.out.println("pv(Flat): " + pv);
    System.out.println();
  }
}
