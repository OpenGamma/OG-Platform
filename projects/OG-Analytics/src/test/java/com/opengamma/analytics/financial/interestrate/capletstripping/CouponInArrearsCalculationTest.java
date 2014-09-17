/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.capletstripping;

import java.util.Arrays;
import java.util.List;

import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexIborMaster;
import com.opengamma.analytics.financial.instrument.payment.CapFloorIborDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborDefinition;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CapFloorIbor;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIbor;
import com.opengamma.analytics.financial.model.volatility.smile.fitting.interpolation.BenaimDodgsonKainthExtrapolationFunctionProvider;
import com.opengamma.analytics.financial.model.volatility.smile.fitting.interpolation.ShiftedLogNormalExtrapolationFunctionProvider;
import com.opengamma.analytics.financial.model.volatility.smile.fitting.interpolation.SmileExtrapolationFunctionSABRProvider;
import com.opengamma.analytics.financial.model.volatility.smile.fitting.interpolation.SmileInterpolatorSABRWithExtrapolation;
import com.opengamma.analytics.financial.provider.description.MulticurveProviderDiscountDataSets;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.math.function.DoublesVectorFunctionProvider;
import com.opengamma.analytics.math.function.InterpolatedVectorFunctionProvider;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.analytics.math.interpolation.TransformedInterpolator1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.minimization.DoubleRangeLimitTransform;
import com.opengamma.analytics.math.minimization.ParameterLimitsTransform;
import com.opengamma.analytics.math.minimization.ParameterLimitsTransform.LimitType;
import com.opengamma.analytics.math.minimization.SingleRangeLimitTransform;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtils;

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
@Test(description = "demo test")
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

  /**
   * Use CapletStripperDirect as the caplet stripper, then model the smile with a SABR interpolator and
   * Benaim-Dodgson-Kainth extrapolation
   */
  @Test
  public void capletStrippingDirectBDKTest() {
    System.out.println("***Global Direct stripper with local SABR + Benaim-Dodgson-Kainth***");
    System.out.println();

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
  @Test
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
  @Test
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
  @Test
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
   * All methodologies for COUPON_IBOR
   */
  public void couponIborAllTest() {
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

    System.out.println("***Global Direct stripper with local SABR + BDK***");
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

    SmileExtrapolationFunctionSABRProvider provider;
    for (int i = 0; i < 11; i++) {
      double mu = 1.0 + 0.25 * i;
      provider = new BenaimDodgsonKainthExtrapolationFunctionProvider(mu, mu);
      SmileInterpolatorSABRWithExtrapolation sabrExtrap = new SmileInterpolatorSABRWithExtrapolation(provider);
      double pv = cal.presentValue(couponIbor, sabrExtrap).getAmount(USD);
      System.out.println("mu: " + mu + ", pv: " + pv);
    }

    /*
     * lambda = 1.0
     */
    System.out.println("<<lambda = 1.0>>");
    lambda = 1.0; // this is chosen to give a chi2/DoF of around 1
    stripper = new CapletStripperDirect(pricer, lambda);
    cal = new CouponInArrearsCalculation(stripper, caps, capVols, MarketDataType.VOL,
        errors, guess, CURVES);
    System.out.println("Chi2: " + cal.getChiSq());
    System.out.println("Time for stripping :" + cal.getTime() + "s");
    System.out.println();

    for (int i = 0; i < 11; i++) {
      double mu = 1.0 + 0.25 * i;
      provider = new BenaimDodgsonKainthExtrapolationFunctionProvider(mu, mu);
      SmileInterpolatorSABRWithExtrapolation sabrExtrap = new SmileInterpolatorSABRWithExtrapolation(provider);
      double pv = cal.presentValue(couponIbor, sabrExtrap).getAmount(USD);
      System.out.println("mu: " + mu + ", pv: " + pv);
    }

    System.out.println("***Interpolated SABR parameter stripper with local SABR + BDK***");
    System.out.println("***Global Direct stripper with local SABR + shifted lognormal***");
    System.out.println("***Interpolated SABR parameter stripper with local SABR + shifted lognormal***");
  }

  //  @Test
  //  public void test() {
  //    double eps = 1.e-6;
  //
  //    double forward = 0.5;
  //    double strike = 0.6;
  //    double strikeUp = strike + eps;
  //    double strikeDw = strike - eps;
  //    double expiry = 1.5;
  //    double vol = 0.4;
  //    double alpha = 0.01;
  //    boolean isCall = strike >= forward;
  //
  //    double impVol = BlackFormulaRepository.impliedVolatility(
  //        BlackFormulaRepository.price(forward * Math.exp(alpha), strike, expiry, vol, strike >= forward), forward,
  //        strike, expiry, strike >= forward);
  //    double impVolUp = BlackFormulaRepository.impliedVolatility(
  //        BlackFormulaRepository.price(forward * Math.exp(alpha), strikeUp, expiry, vol, strikeUp >= forward), forward,
  //        strikeUp, expiry, strikeUp >= forward);
  //    double impVolDw = BlackFormulaRepository.impliedVolatility(
  //        BlackFormulaRepository.price(forward * Math.exp(alpha), strikeDw, expiry, vol, strikeDw >= forward), forward,
  //        strikeDw, expiry, strikeDw >= forward);
  //    double fd = 0.5 * (impVolUp - impVolDw) / eps;
  //    
  //    double an = (-BlackFormulaRepository.dualDelta(forward, strike, expiry, impVol, isCall) + BlackFormulaRepository
  //        .dualDelta(forward * Math.exp(alpha), strike, expiry, vol, isCall)) /
  //        BlackFormulaRepository.vega(forward, strikeDw, expiry, impVol);
  //
  //    System.out.println(fd + "\t" + an);
  //  }
}
