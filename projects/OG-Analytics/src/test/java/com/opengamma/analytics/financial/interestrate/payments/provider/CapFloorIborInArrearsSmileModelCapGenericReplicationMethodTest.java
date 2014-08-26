/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.payments.provider;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.payment.CapFloorIborDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborDefinition;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CapFloorIbor;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIbor;
import com.opengamma.analytics.financial.model.option.definition.SABRInterestRateParameters;
import com.opengamma.analytics.financial.model.volatility.BlackFormulaRepository;
import com.opengamma.analytics.financial.model.volatility.smile.fitting.interpolation.BenaimDodgsonKainthExtrapolationFunctionProvider;
import com.opengamma.analytics.financial.model.volatility.smile.fitting.interpolation.InterpolatedSmileFunction;
import com.opengamma.analytics.financial.model.volatility.smile.fitting.interpolation.ShiftedLogNormalExtrapolationFunctionProvider;
import com.opengamma.analytics.financial.model.volatility.smile.fitting.interpolation.SmileExtrapolationFunctionSABRProvider;
import com.opengamma.analytics.financial.model.volatility.smile.fitting.interpolation.SmileInterpolatorSABR;
import com.opengamma.analytics.financial.model.volatility.smile.fitting.interpolation.SmileInterpolatorSABRWithExtrapolation;
import com.opengamma.analytics.financial.model.volatility.smile.fitting.interpolation.SmileInterpolatorSpline;
import com.opengamma.analytics.financial.provider.description.MulticurveProviderDiscountDataSets;
import com.opengamma.analytics.financial.provider.description.SABRDataSets;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.SABRCapProviderDiscount;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Test class for {@link CapFloorIborInArrearsSmileModelCapGenericReplicationMethod} and 
 * {@link CouponIborInArrearsSmileModelReplicationMethod}
 */
@Test(groups = TestGroup.UNIT)
public class CapFloorIborInArrearsSmileModelCapGenericReplicationMethodTest {

  private static final MulticurveProviderDiscount MULTICURVES = MulticurveProviderDiscountDataSets
      .createMulticurveEurUsd();
  private static final IborIndex EURIBOR6M = MulticurveProviderDiscountDataSets.getIndexesIborMulticurveEurUsd()[1];
  private static final Currency EUR = EURIBOR6M.getCurrency();
  private static final Calendar CALENDAR = MulticurveProviderDiscountDataSets.getEURCalendar();
  private static final SABRInterestRateParameters SABR_PARAMETER = SABRDataSets.createSABR1();
  private static final SABRCapProviderDiscount SABR_MULTICURVES = new SABRCapProviderDiscount(MULTICURVES,
      SABR_PARAMETER, EURIBOR6M);

  // Dates
  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2011, 6, 7);
  private static final ZonedDateTime START_ACCRUAL_DATE = ScheduleCalculator.getAdjustedDate(REFERENCE_DATE,
      Period.ofYears(9), EURIBOR6M, CALENDAR);
  private static final ZonedDateTime END_ACCRUAL_DATE = ScheduleCalculator.getAdjustedDate(START_ACCRUAL_DATE,
      EURIBOR6M, CALENDAR);
  private static final double ACCRUAL_FACTOR = EURIBOR6M.getDayCount().getDayCountFraction(START_ACCRUAL_DATE,
      END_ACCRUAL_DATE, CALENDAR);
  private static final ZonedDateTime FIXING_DATE = ScheduleCalculator.getAdjustedDate(END_ACCRUAL_DATE,
      -EURIBOR6M.getSpotLag(), CALENDAR);
  private static final double NOTIONAL = 100000000; //100m
  private static final double STRIKE = 0.03;
  private static final boolean IS_CAP = true;
  // Definition description: In arrears
  private static final CapFloorIborDefinition CAP_IA_LONG_DEFINITION = new CapFloorIborDefinition(EUR,
      END_ACCRUAL_DATE, START_ACCRUAL_DATE, END_ACCRUAL_DATE, ACCRUAL_FACTOR, NOTIONAL, FIXING_DATE,
      EURIBOR6M, STRIKE, IS_CAP, CALENDAR);
  private static final CouponIborDefinition COUPON_IBOR_IA_DEFINITION = new CouponIborDefinition(EUR, END_ACCRUAL_DATE,
      START_ACCRUAL_DATE, END_ACCRUAL_DATE, ACCRUAL_FACTOR, NOTIONAL, FIXING_DATE,
      EURIBOR6M, CALENDAR);
  private static final CapFloorIborDefinition CAP_IA_SHORT_DEFINITION = new CapFloorIborDefinition(EUR,
      END_ACCRUAL_DATE, START_ACCRUAL_DATE, END_ACCRUAL_DATE, ACCRUAL_FACTOR, -NOTIONAL, FIXING_DATE,
      EURIBOR6M, STRIKE, IS_CAP, CALENDAR);
  private static final CapFloorIborDefinition FLOOR_IA_SHORT_DEFINITION = new CapFloorIborDefinition(EUR,
      END_ACCRUAL_DATE, START_ACCRUAL_DATE, END_ACCRUAL_DATE, ACCRUAL_FACTOR, -NOTIONAL,
      FIXING_DATE, EURIBOR6M, STRIKE, !IS_CAP, CALENDAR);
  // To derivative
  private static final CapFloorIbor CAP_LONG = (CapFloorIbor) CAP_IA_LONG_DEFINITION.toDerivative(REFERENCE_DATE);
  private static final CouponIbor COUPON_IBOR = (CouponIbor) COUPON_IBOR_IA_DEFINITION.toDerivative(REFERENCE_DATE);
  private static final CapFloorIbor CAP_SHORT = (CapFloorIbor) CAP_IA_SHORT_DEFINITION.toDerivative(REFERENCE_DATE);
  private static final CapFloorIbor FLOOR_SHORT = (CapFloorIbor) FLOOR_IA_SHORT_DEFINITION.toDerivative(REFERENCE_DATE);
  // Methods
  private static final double MU = 8.00;
  private static final CapFloorIborSABRCapMethod METHOD_SABR_STD = CapFloorIborSABRCapMethod.getInstance();

  // Extracted Sample data
  private static final double[] STRIKES = new double[] {0.005, 0.01, 0.014844615790478254, 0.015, 0.02, 0.025, 0.03,
      0.035, 0.04, 0.045, 0.05, 0.055, 0.06, 0.07, 0.08, 0.09, 0.1, 0.11, 0.12 };
  private static final double[] VOLS = new double[] {0.7358548700933457, 0.5523841305358348, 0.4657877918377955,
      0.4638783367475177, 0.4224312896268929, 0.40717031545963184, 0.4045852433505948,
      0.40746426050271956, 0.41252053623278173, 0.4183168373948183, 0.42422257685856807, 0.4299622516488387,
      0.4354215920678454, 0.4453783030412607, 0.45410005682243837, 0.4617460214027164,
      0.4684858081353601, 0.4744669985423348, 0.47981106357536796 };
  private static final double FORWARD = 0.014844615790478254;

  /**
   * Consistency with {@link CapFloorIborInArrearsSABRCapGenericReplicationMethod} using SABR interpolation and extrapolation
   * Note that SmileInterpolatorSABR and SmileInterpolatorSABRWithRightExtrapolation uses weighted sum of SABR's as interpolation. 
   * Thus they do not exactly match (especially for the no extrapolation case).
   * 
   * Due to an update of the integration range, the resulting numbers are not close in some cases. 
   * Still one can confirm the agreement if we use the same upper cutoff 
   * in {@link CapFloorIborInArrearsSmileModelCapGenericReplicationMethod} 
   */
  @Test
  public void consistencyTest() {
    /**
     * Cap/Floor Ibor
     */
    CapFloorIbor[] derivatives = new CapFloorIbor[] {CAP_LONG, CAP_SHORT, FLOOR_SHORT };
    for (final CapFloorIbor derivative : derivatives) {
      CapFloorIborInArrearsSABRCapGenericReplicationMethod methodSabr = new CapFloorIborInArrearsSABRCapGenericReplicationMethod(
          METHOD_SABR_STD);
      MultipleCurrencyAmount res1 = methodSabr.presentValue(derivative, SABR_MULTICURVES);

      double forward = SABR_MULTICURVES.getMulticurveProvider().getSimplyCompoundForwardRate(derivative.getIndex(),
          derivative.getFixingPeriodStartTime(), derivative.getFixingPeriodEndTime(),
          derivative.getFixingAccrualFactor());
      double maturity = derivative.getFixingPeriodEndTime() - derivative.getFixingPeriodStartTime();
      int nSample = 1000;
      double[] sampleStrikes = new double[nSample];
      double[] sampleVolatilities = new double[nSample];
      for (int i = 0; i < nSample; ++i) {
        sampleStrikes[i] = forward * (i * 0.001 + 0.002);
        sampleVolatilities[i] = SABR_PARAMETER.getVolatility(derivative.getFixingTime(), maturity, sampleStrikes[i],
            forward);
      }

      /*
       * Without extrapolation
       */
      SmileInterpolatorSABR sabrInterp = new SmileInterpolatorSABR();
      InterpolatedSmileFunction smileFunction = new InterpolatedSmileFunction(sabrInterp, forward, sampleStrikes,
          derivative.getFixingTime(), sampleVolatilities);
      CapFloorIborInArrearsSmileModelCapGenericReplicationMethod methodSabrGeneral = new CapFloorIborInArrearsSmileModelCapGenericReplicationMethod(
          smileFunction);
      MultipleCurrencyAmount res2 = methodSabrGeneral.presentValue(derivative, MULTICURVES);
      double ref = res1.getAmount(derivative.getCurrency());
      assertEquals(ref, res2.getAmount(derivative.getCurrency()), Math.abs(ref) * 1.e-1);

      /*
       * With extrapolation
       */
      CapFloorIborSABRCapExtrapolationRightMethod oldMethod = new CapFloorIborSABRCapExtrapolationRightMethod(
          sampleStrikes[nSample - 1], MU);
      CapFloorIborInArrearsSABRCapGenericReplicationMethod methodSabrExtrap = new CapFloorIborInArrearsSABRCapGenericReplicationMethod(
          oldMethod);
      MultipleCurrencyAmount res1Extrap = methodSabrExtrap.presentValue(derivative, SABR_MULTICURVES);

      BenaimDodgsonKainthExtrapolationFunctionProvider provider = new BenaimDodgsonKainthExtrapolationFunctionProvider(
          MU, MU);
      SmileInterpolatorSABRWithExtrapolation sabrExtrap = new SmileInterpolatorSABRWithExtrapolation(provider);
      InterpolatedSmileFunction smileFunctionExtrap = new InterpolatedSmileFunction(sabrExtrap, forward, sampleStrikes,
          derivative.getFixingTime(), sampleVolatilities);
      CapFloorIborInArrearsSmileModelCapGenericReplicationMethod methodSabrGeneralExtrap = new CapFloorIborInArrearsSmileModelCapGenericReplicationMethod(
          smileFunctionExtrap);
      MultipleCurrencyAmount res2Extrap = methodSabrGeneralExtrap.presentValue(derivative, MULTICURVES);

      double refExtrap = res1Extrap.getAmount(derivative.getCurrency());
      assertEquals(refExtrap, res2Extrap.getAmount(derivative.getCurrency()), Math.abs(refExtrap) * 1.e-6);
    }

    /**
     * Coupon Ibor
     */
    CouponIborInArrearsReplicationMethod methodSabr = new CouponIborInArrearsReplicationMethod(METHOD_SABR_STD);
    MultipleCurrencyAmount res1 = methodSabr.presentValue(COUPON_IBOR, SABR_MULTICURVES);

    double forward = SABR_MULTICURVES.getMulticurveProvider().getSimplyCompoundForwardRate(COUPON_IBOR.getIndex(),
        COUPON_IBOR.getFixingPeriodStartTime(), COUPON_IBOR.getFixingPeriodEndTime(),
        COUPON_IBOR.getFixingAccrualFactor());
    double maturity = COUPON_IBOR.getFixingPeriodEndTime() - COUPON_IBOR.getFixingPeriodStartTime();
    int nSample = 1000;
    double[] sampleStrikes = new double[nSample];
    double[] sampleVolatilities = new double[nSample];
    for (int i = 0; i < nSample; ++i) {
      sampleStrikes[i] = forward * (i * 0.001 + 0.002);
      sampleVolatilities[i] = SABR_PARAMETER.getVolatility(COUPON_IBOR.getFixingTime(), maturity, sampleStrikes[i],
          forward);
    }

    /*
     * Without extrapolation
     */
    SmileInterpolatorSABR sabrInterp = new SmileInterpolatorSABR();
    InterpolatedSmileFunction smileFunction = new InterpolatedSmileFunction(sabrInterp, forward, sampleStrikes,
        COUPON_IBOR.getFixingTime(), sampleVolatilities);
    CouponIborInArrearsSmileModelReplicationMethod methodSabrGeneral = new CouponIborInArrearsSmileModelReplicationMethod(
        smileFunction);
    MultipleCurrencyAmount res2 = methodSabrGeneral.presentValue(COUPON_IBOR, MULTICURVES);
    double ref = res1.getAmount(COUPON_IBOR.getCurrency());
    assertEquals(ref, res2.getAmount(COUPON_IBOR.getCurrency()), Math.abs(ref) * 1.e-1);

    /*
     * With extrapolation
     */
    CapFloorIborSABRCapExtrapolationRightMethod oldMethod = new CapFloorIborSABRCapExtrapolationRightMethod(
        sampleStrikes[nSample - 1], MU);
    CouponIborInArrearsReplicationMethod methodSabrExtrap = new CouponIborInArrearsReplicationMethod(oldMethod);
    MultipleCurrencyAmount res1Extrap = methodSabrExtrap.presentValue(COUPON_IBOR, SABR_MULTICURVES);

    BenaimDodgsonKainthExtrapolationFunctionProvider provider = new BenaimDodgsonKainthExtrapolationFunctionProvider(
        MU, MU);
    SmileInterpolatorSABRWithExtrapolation sabrExtrap = new SmileInterpolatorSABRWithExtrapolation(provider);
    InterpolatedSmileFunction smileFunctionExtrap = new InterpolatedSmileFunction(sabrExtrap, forward, sampleStrikes,
        COUPON_IBOR.getFixingTime(), sampleVolatilities);
    CouponIborInArrearsSmileModelReplicationMethod methodSabrGeneralExtrap = new CouponIborInArrearsSmileModelReplicationMethod(
        smileFunctionExtrap);
    MultipleCurrencyAmount res2Extrap = methodSabrGeneralExtrap.presentValue(COUPON_IBOR, MULTICURVES);

    double refExtrap = res1Extrap.getAmount(COUPON_IBOR.getCurrency());
    assertEquals(refExtrap, res2Extrap.getAmount(COUPON_IBOR.getCurrency()), Math.abs(refExtrap) * 1.e-6);

  }

  /**
   * Comparing two ways of extrapolation, {@link SmileExtrapolationFunctionSABRProvider}
   * The two extrapolation methods produce similar result only for a specific choice of mu.
   */
  @Test
  public void ComparisonTest() {
    CapFloorIbor[] derivatives = new CapFloorIbor[] {CAP_LONG, CAP_SHORT, FLOOR_SHORT };
    for (final CapFloorIbor derivative : derivatives) {
      double muLow = 18.0;
      double muHigh = 1.145;

      BenaimDodgsonKainthExtrapolationFunctionProvider provider = new BenaimDodgsonKainthExtrapolationFunctionProvider(
          muLow, muHigh);
      SmileInterpolatorSABRWithExtrapolation sabrExtrap2 = new SmileInterpolatorSABRWithExtrapolation(provider);
      InterpolatedSmileFunction smileFunctionExtrap2 = new InterpolatedSmileFunction(sabrExtrap2, FORWARD, STRIKES,
          derivative.getFixingTime(), VOLS);
      CapFloorIborInArrearsSmileModelCapGenericReplicationMethod methodSabrGeneralExtrap2 = new CapFloorIborInArrearsSmileModelCapGenericReplicationMethod(
          smileFunctionExtrap2);
      MultipleCurrencyAmount res2Extrap2 = methodSabrGeneralExtrap2.presentValue(derivative, MULTICURVES);

      ShiftedLogNormalExtrapolationFunctionProvider providerLog = new ShiftedLogNormalExtrapolationFunctionProvider(
          "Quiet");
      SmileInterpolatorSABRWithExtrapolation sabrExtrap2Log = new SmileInterpolatorSABRWithExtrapolation(providerLog);
      InterpolatedSmileFunction smileFunctionExtrap2Log = new InterpolatedSmileFunction(sabrExtrap2Log, FORWARD,
          STRIKES, derivative.getFixingTime(), VOLS);
      CapFloorIborInArrearsSmileModelCapGenericReplicationMethod methodSabrGeneralExtrap2Log = new CapFloorIborInArrearsSmileModelCapGenericReplicationMethod(
          smileFunctionExtrap2Log);
      MultipleCurrencyAmount res2Extrap2Log = methodSabrGeneralExtrap2Log.presentValue(derivative, MULTICURVES);

      SmileInterpolatorSpline spline = new SmileInterpolatorSpline(Interpolator1DFactory.PCHIP_INSTANCE, "Quiet");
      InterpolatedSmileFunction smileFunctionSpline = new InterpolatedSmileFunction(spline, FORWARD, STRIKES,
          derivative.getFixingTime(), VOLS);
      CapFloorIborInArrearsSmileModelCapGenericReplicationMethod methodSpline = new CapFloorIborInArrearsSmileModelCapGenericReplicationMethod(
          smileFunctionSpline);
      MultipleCurrencyAmount resSpline = methodSpline.presentValue(derivative, MULTICURVES);

      double ref = res2Extrap2.getAmount(EUR);
      assertEquals(ref, res2Extrap2Log.getAmount(EUR), Math.abs(ref) * 1.e-3);
      assertEquals(ref, resSpline.getAmount(EUR), Math.abs(ref) * 1.e-3);
    }

    /**
     * Coupon Ibor
     */
    double muLow = 14.05;
    double muHigh = 1.145;

    BenaimDodgsonKainthExtrapolationFunctionProvider provider = new BenaimDodgsonKainthExtrapolationFunctionProvider(
        muLow, muHigh);
    SmileInterpolatorSABRWithExtrapolation sabrExtrap2 = new SmileInterpolatorSABRWithExtrapolation(provider);
    InterpolatedSmileFunction smileFunctionExtrap2 = new InterpolatedSmileFunction(sabrExtrap2, FORWARD, STRIKES,
        COUPON_IBOR.getFixingTime(), VOLS);
    CouponIborInArrearsSmileModelReplicationMethod methodSabrGeneralExtrap2 = new CouponIborInArrearsSmileModelReplicationMethod(
        smileFunctionExtrap2);
    MultipleCurrencyAmount res2Extrap2 = methodSabrGeneralExtrap2.presentValue(COUPON_IBOR, MULTICURVES);

    ShiftedLogNormalExtrapolationFunctionProvider providerLog = new ShiftedLogNormalExtrapolationFunctionProvider(
        "Quiet");
    SmileInterpolatorSABRWithExtrapolation sabrExtrap2Log = new SmileInterpolatorSABRWithExtrapolation(providerLog);
    InterpolatedSmileFunction smileFunctionExtrap2Log = new InterpolatedSmileFunction(sabrExtrap2Log, FORWARD, STRIKES,
        COUPON_IBOR.getFixingTime(), VOLS);
    CouponIborInArrearsSmileModelReplicationMethod methodSabrGeneralExtrap2Log = new CouponIborInArrearsSmileModelReplicationMethod(
        smileFunctionExtrap2Log);
    MultipleCurrencyAmount res2Extrap2Log = methodSabrGeneralExtrap2Log.presentValue(COUPON_IBOR, MULTICURVES);

    SmileInterpolatorSpline spline = new SmileInterpolatorSpline(Interpolator1DFactory.PCHIP_INSTANCE, "Quiet");
    InterpolatedSmileFunction smileFunctionSpline = new InterpolatedSmileFunction(spline, FORWARD, STRIKES,
        COUPON_IBOR.getFixingTime(), VOLS);
    CouponIborInArrearsSmileModelReplicationMethod methodSpline = new CouponIborInArrearsSmileModelReplicationMethod(
        smileFunctionSpline);
    MultipleCurrencyAmount resSpline = methodSpline.presentValue(COUPON_IBOR, MULTICURVES);

    double ref = res2Extrap2.getAmount(EUR);
    assertEquals(ref, res2Extrap2Log.getAmount(EUR), Math.abs(ref) * 1.e-2);
    assertEquals(ref, resSpline.getAmount(EUR), Math.abs(ref) * 1.e-2);
  }

  /**
   * Check parity relationship for the present value
   */
  @Test
  public void persentValueSABRExtrapolationParity() {
    double[] musCap = muCalculator(STRIKES, VOLS, FORWARD, CAP_LONG.getFixingTime());
    SmileExtrapolationFunctionSABRProvider provider1 = new BenaimDodgsonKainthExtrapolationFunctionProvider(musCap[0],
        musCap[1]);
    double[] musIbor = muCalculator(STRIKES, VOLS, FORWARD, COUPON_IBOR.getFixingTime());
    SmileExtrapolationFunctionSABRProvider provider2 = new BenaimDodgsonKainthExtrapolationFunctionProvider(musIbor[0],
        musIbor[1]);
    SmileExtrapolationFunctionSABRProvider provider = new ShiftedLogNormalExtrapolationFunctionProvider("Quiet");

    SmileExtrapolationFunctionSABRProvider[] providers1 = new SmileExtrapolationFunctionSABRProvider[] {provider1,
        provider };
    SmileExtrapolationFunctionSABRProvider[] providers2 = new SmileExtrapolationFunctionSABRProvider[] {provider2,
        provider };

    for (int i = 0; i < 2; ++i) {
      SmileInterpolatorSABRWithExtrapolation sabrExtrap = new SmileInterpolatorSABRWithExtrapolation(providers1[i]);
      InterpolatedSmileFunction smileFunctionExtrap = new InterpolatedSmileFunction(sabrExtrap, FORWARD, STRIKES,
          CAP_LONG.getFixingTime(), VOLS);
      CapFloorIborInArrearsSmileModelCapGenericReplicationMethod methodSabrGeneralExtrap = new CapFloorIborInArrearsSmileModelCapGenericReplicationMethod(
          smileFunctionExtrap);

      MultipleCurrencyAmount priceCapLong = methodSabrGeneralExtrap.presentValue(CAP_LONG, MULTICURVES);
      MultipleCurrencyAmount priceCapShort = methodSabrGeneralExtrap.presentValue(CAP_SHORT, MULTICURVES);
      assertEquals(priceCapLong.getAmount(EUR), -priceCapShort.getAmount(EUR),
          Math.abs(priceCapLong.getAmount(EUR)) * 1.0e-10);

      SmileInterpolatorSABRWithExtrapolation sabrExtrap2 = new SmileInterpolatorSABRWithExtrapolation(providers2[i]);
      InterpolatedSmileFunction smileFunctionExtrap2 = new InterpolatedSmileFunction(sabrExtrap2, FORWARD, STRIKES,
          COUPON_IBOR.getFixingTime(), VOLS);
      CouponIborInArrearsSmileModelReplicationMethod methodSabrGeneralExtrap2 = new CouponIborInArrearsSmileModelReplicationMethod(
          smileFunctionExtrap2);
      MultipleCurrencyAmount priceIbor = methodSabrGeneralExtrap2.presentValue(COUPON_IBOR, MULTICURVES);
      final CapFloorIborDefinition cap0Definition = new CapFloorIborDefinition(EUR, END_ACCRUAL_DATE,
          START_ACCRUAL_DATE, END_ACCRUAL_DATE, ACCRUAL_FACTOR, NOTIONAL, FIXING_DATE, EURIBOR6M, 0.0,
          IS_CAP, CALENDAR);
      final CapFloorIbor cap0 = (CapFloorIbor) cap0Definition.toDerivative(REFERENCE_DATE);
      final MultipleCurrencyAmount priceCap0 = methodSabrGeneralExtrap.presentValue(cap0, MULTICURVES);
      assertEquals(priceCap0.getAmount(EUR), priceIbor.getAmount(EUR), Math.abs(priceCap0.getAmount(EUR)) * 1.0e-10);

    }
  }

  /**
   * faster decay for larger mu, thus smaller pv
   */
  @Test
  public void muDependenceTest() {
    double[] mu = new double[] {25.0, 18.0, 5.0, 1.5 };
    int nMu = mu.length;

    CapFloorIbor[] derivatives = new CapFloorIbor[] {CAP_LONG, CAP_SHORT, FLOOR_SHORT };
    for (final CapFloorIbor derivative : derivatives) {

      double prev = 0.0;
      double pres = 0.0;
      for (int i = 0; i < nMu; ++i) {
        BenaimDodgsonKainthExtrapolationFunctionProvider provider = new BenaimDodgsonKainthExtrapolationFunctionProvider(
            mu[i], mu[i]);
        SmileInterpolatorSABRWithExtrapolation sabrExtrap2 = new SmileInterpolatorSABRWithExtrapolation(provider);
        InterpolatedSmileFunction smileFunctionExtrap2 = new InterpolatedSmileFunction(sabrExtrap2, FORWARD, STRIKES,
            derivative.getFixingTime(), VOLS);
        CapFloorIborInArrearsSmileModelCapGenericReplicationMethod methodSabrGeneralExtrap2 = new CapFloorIborInArrearsSmileModelCapGenericReplicationMethod(
            smileFunctionExtrap2);
        MultipleCurrencyAmount res2Extrap2 = methodSabrGeneralExtrap2.presentValue(derivative, MULTICURVES);
        pres = Math.abs(res2Extrap2.getAmount(EUR));
        assertTrue(pres > prev);
        prev = pres;
      }
    }

    double prev = 0.0;
    double pres = 0.0;
    for (int i = 0; i < nMu; ++i) {
      BenaimDodgsonKainthExtrapolationFunctionProvider provider = new BenaimDodgsonKainthExtrapolationFunctionProvider(
          mu[i], mu[i]);
      SmileInterpolatorSABRWithExtrapolation sabrExtrap2 = new SmileInterpolatorSABRWithExtrapolation(provider);
      InterpolatedSmileFunction smileFunctionExtrap2 = new InterpolatedSmileFunction(sabrExtrap2, FORWARD, STRIKES,
          COUPON_IBOR.getFixingTime(), VOLS);
      CouponIborInArrearsSmileModelReplicationMethod methodSabrGeneralExtrap2 = new CouponIborInArrearsSmileModelReplicationMethod(
          smileFunctionExtrap2);
      MultipleCurrencyAmount res2Extrap2 = methodSabrGeneralExtrap2.presentValue(COUPON_IBOR, MULTICURVES);
      pres = Math.abs(res2Extrap2.getAmount(EUR));
      assertTrue(pres > prev);
      prev = pres;
    }
  }

  private double[] muCalculator(final double[] strikes, final double[] vols, final double forward, final double expiry) {
    double[] res = new double[2];

    int nSample = strikes.length;
    res[0] = strikes[0] * BlackFormulaRepository.dualDelta(forward, strikes[0], expiry, vols[0], false) /
        BlackFormulaRepository.price(forward, strikes[0], expiry, vols[0], false);
    res[1] = -strikes[nSample - 1] *
        BlackFormulaRepository.dualDelta(forward, strikes[nSample - 1], expiry, vols[nSample - 1], true) /
        BlackFormulaRepository.price(forward, strikes[nSample - 1], expiry, vols[nSample - 1], true);
    return res;
  }
}
