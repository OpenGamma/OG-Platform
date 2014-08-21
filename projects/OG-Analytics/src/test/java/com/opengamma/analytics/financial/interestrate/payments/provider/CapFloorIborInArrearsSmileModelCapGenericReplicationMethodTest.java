/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.payments.provider;

import static org.testng.AssertJUnit.assertEquals;

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
import com.opengamma.analytics.financial.model.volatility.smile.fitting.interpolation.SmileInterpolatorSABR;
import com.opengamma.analytics.financial.model.volatility.smile.fitting.interpolation.SmileInterpolatorSABRWithExtrapolation;
import com.opengamma.analytics.financial.provider.description.MulticurveProviderDiscountDataSets;
import com.opengamma.analytics.financial.provider.description.SABRDataSets;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.SABRCapProviderDiscount;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Test class for {@link CapFloorIborInArrearsSmileModelCapGenericReplicationMethod} and {@link CouponIborInArrearsSmileModelReplicationMethod}
 */
@Test(groups = TestGroup.UNIT)
public class CapFloorIborInArrearsSmileModelCapGenericReplicationMethodTest {

  private static final MulticurveProviderDiscount MULTICURVES = MulticurveProviderDiscountDataSets.createMulticurveEurUsd();
  private static final IborIndex EURIBOR6M = MulticurveProviderDiscountDataSets.getIndexesIborMulticurveEurUsd()[1];
  private static final Currency EUR = EURIBOR6M.getCurrency();
  private static final Calendar CALENDAR = MulticurveProviderDiscountDataSets.getEURCalendar();
  private static final SABRInterestRateParameters SABR_PARAMETER = SABRDataSets.createSABR1();
  private static final SABRCapProviderDiscount SABR_MULTICURVES = new SABRCapProviderDiscount(MULTICURVES, SABR_PARAMETER, EURIBOR6M);

  // Dates
  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2011, 6, 7);
  private static final ZonedDateTime START_ACCRUAL_DATE = ScheduleCalculator.getAdjustedDate(REFERENCE_DATE, Period.ofYears(9), EURIBOR6M, CALENDAR);
  private static final ZonedDateTime END_ACCRUAL_DATE = ScheduleCalculator.getAdjustedDate(START_ACCRUAL_DATE, EURIBOR6M, CALENDAR);
  private static final double ACCRUAL_FACTOR = EURIBOR6M.getDayCount().getDayCountFraction(START_ACCRUAL_DATE, END_ACCRUAL_DATE, CALENDAR);
  private static final ZonedDateTime FIXING_DATE = ScheduleCalculator.getAdjustedDate(END_ACCRUAL_DATE, -EURIBOR6M.getSpotLag(), CALENDAR);
  private static final double NOTIONAL = 100000000; //100m
  private static final double STRIKE = 0.03;
  private static final boolean IS_CAP = true;
  // Definition description: In arrears
  private static final CapFloorIborDefinition CAP_IA_LONG_DEFINITION = new CapFloorIborDefinition(EUR, END_ACCRUAL_DATE, START_ACCRUAL_DATE, END_ACCRUAL_DATE, ACCRUAL_FACTOR, NOTIONAL, FIXING_DATE,
      EURIBOR6M, STRIKE, IS_CAP, CALENDAR);
  private static final CouponIborDefinition COUPON_IBOR_IA_DEFINITION = new CouponIborDefinition(EUR, END_ACCRUAL_DATE, START_ACCRUAL_DATE, END_ACCRUAL_DATE, ACCRUAL_FACTOR, NOTIONAL, FIXING_DATE,
      EURIBOR6M, CALENDAR);
  private static final CapFloorIborDefinition CAP_IA_SHORT_DEFINITION = new CapFloorIborDefinition(EUR, END_ACCRUAL_DATE, START_ACCRUAL_DATE, END_ACCRUAL_DATE, ACCRUAL_FACTOR, -NOTIONAL, FIXING_DATE,
      EURIBOR6M, STRIKE, IS_CAP, CALENDAR);
  private static final CapFloorIborDefinition FLOOR_IA_SHORT_DEFINITION = new CapFloorIborDefinition(EUR, END_ACCRUAL_DATE, START_ACCRUAL_DATE, END_ACCRUAL_DATE, ACCRUAL_FACTOR, -NOTIONAL,
      FIXING_DATE, EURIBOR6M, STRIKE, !IS_CAP, CALENDAR);
  // To derivative
  private static final CapFloorIbor CAP_LONG = (CapFloorIbor) CAP_IA_LONG_DEFINITION.toDerivative(REFERENCE_DATE);
  private static final CouponIbor COUPON_IBOR = (CouponIbor) COUPON_IBOR_IA_DEFINITION.toDerivative(REFERENCE_DATE);
  private static final CapFloorIbor CAP_SHORT = (CapFloorIbor) CAP_IA_SHORT_DEFINITION.toDerivative(REFERENCE_DATE);
  private static final CapFloorIbor FLOOR_SHORT = (CapFloorIbor) FLOOR_IA_SHORT_DEFINITION.toDerivative(REFERENCE_DATE);
  // Methods
  private static final double CUT_OFF_STRIKE = 0.08;
  private static final double MU = 8.00;
  private static final CapFloorIborSABRCapExtrapolationRightMethod METHOD_SABREXTRA_STD = new CapFloorIborSABRCapExtrapolationRightMethod(CUT_OFF_STRIKE, MU);
  private static final CapFloorIborSABRCapMethod METHOD_SABR_STD = CapFloorIborSABRCapMethod.getInstance();

  /**
   * Consistency with {@link CapFloorIborInArrearsSABRCapGenericReplicationMethod} using SABR interpolation and extrapolation
   * Note that SmileInterpolatorSABR and SmileInterpolatorSABRWithRightExtrapolation uses weighted sum of SABR's as interpolation. 
   * Thus they do not exactly match (especially for the no extrapolation case).
   * 
   * Due to an update of the integration range, the resulting numbers are not close in some cases. 
   * Still one can confirm the agreement if we use the same upper cutoff in {@link CapFloorIborInArrearsSmileModelCapGenericReplicationMethod} 
   */
  @Test
  public void consistencyTest() {
    /**
     * Cap/Floor Ibor
     */
    CapFloorIbor[] derivatives = new CapFloorIbor[] {CAP_LONG, CAP_SHORT, FLOOR_SHORT };
    for (final CapFloorIbor derivative : derivatives) {
      CapFloorIborInArrearsSABRCapGenericReplicationMethod methodSabr = new CapFloorIborInArrearsSABRCapGenericReplicationMethod(METHOD_SABR_STD);
      MultipleCurrencyAmount res1 = methodSabr.presentValue(derivative, SABR_MULTICURVES);

      double forward = SABR_MULTICURVES.getMulticurveProvider().getSimplyCompoundForwardRate(derivative.getIndex(), derivative.getFixingPeriodStartTime(), derivative.getFixingPeriodEndTime(),
          derivative.getFixingAccrualFactor());
      double maturity = derivative.getFixingPeriodEndTime() - derivative.getFixingPeriodStartTime();
      int nSample = 1000;
      double[] sampleStrikes = new double[nSample];
      double[] sampleVolatilities = new double[nSample];
      for (int i = 0; i < nSample; ++i) {
        sampleStrikes[i] = forward * (i * 0.001 + 0.002);
        sampleVolatilities[i] = SABR_PARAMETER.getVolatility(derivative.getFixingTime(), maturity, sampleStrikes[i], forward);
      }

      /*
       * Without extrapolation
       */
      SmileInterpolatorSABR sabrInterp = new SmileInterpolatorSABR();
      InterpolatedSmileFunction smileFunction = new InterpolatedSmileFunction(sabrInterp, forward, sampleStrikes, derivative.getFixingTime(), sampleVolatilities);
      CapFloorIborInArrearsSmileModelCapGenericReplicationMethod methodSabrGeneral = new CapFloorIborInArrearsSmileModelCapGenericReplicationMethod(smileFunction);
      MultipleCurrencyAmount res2 = methodSabrGeneral.presentValue(derivative, MULTICURVES);
      double ref = res1.getAmount(derivative.getCurrency());
      assertEquals(ref, res2.getAmount(derivative.getCurrency()), Math.abs(ref) * 1.e-1);

      /*
       * With extrapolation
       */
      CapFloorIborSABRCapExtrapolationRightMethod oldMethod = new CapFloorIborSABRCapExtrapolationRightMethod(sampleStrikes[nSample - 1], MU);
      CapFloorIborInArrearsSABRCapGenericReplicationMethod methodSabrExtrap = new CapFloorIborInArrearsSABRCapGenericReplicationMethod(oldMethod);
      MultipleCurrencyAmount res1Extrap = methodSabrExtrap.presentValue(derivative, SABR_MULTICURVES);

      BenaimDodgsonKainthExtrapolationFunctionProvider provider = new BenaimDodgsonKainthExtrapolationFunctionProvider(MU, MU);
      SmileInterpolatorSABRWithExtrapolation sabrExtrap = new SmileInterpolatorSABRWithExtrapolation(provider);
      InterpolatedSmileFunction smileFunctionExtrap = new InterpolatedSmileFunction(sabrExtrap, forward, sampleStrikes, derivative.getFixingTime(), sampleVolatilities);
      CapFloorIborInArrearsSmileModelCapGenericReplicationMethod methodSabrGeneralExtrap = new CapFloorIborInArrearsSmileModelCapGenericReplicationMethod(smileFunctionExtrap);
      MultipleCurrencyAmount res2Extrap = methodSabrGeneralExtrap.presentValue(derivative, MULTICURVES);

      double refExtrap = res1Extrap.getAmount(derivative.getCurrency());
      assertEquals(refExtrap, res2Extrap.getAmount(derivative.getCurrency()), Math.abs(refExtrap) * 1.e-6);
    }

    /**
     * Coupon Ibor
     */
    CouponIborInArrearsReplicationMethod methodSabr = new CouponIborInArrearsReplicationMethod(METHOD_SABR_STD);
    MultipleCurrencyAmount res1 = methodSabr.presentValue(COUPON_IBOR, SABR_MULTICURVES);

    double forward = SABR_MULTICURVES.getMulticurveProvider().getSimplyCompoundForwardRate(COUPON_IBOR.getIndex(), COUPON_IBOR.getFixingPeriodStartTime(), COUPON_IBOR.getFixingPeriodEndTime(),
        COUPON_IBOR.getFixingAccrualFactor());
    double maturity = COUPON_IBOR.getFixingPeriodEndTime() - COUPON_IBOR.getFixingPeriodStartTime();
    int nSample = 1000;
    double[] sampleStrikes = new double[nSample];
    double[] sampleVolatilities = new double[nSample];
    for (int i = 0; i < nSample; ++i) {
      sampleStrikes[i] = forward * (i * 0.001 + 0.002);
      sampleVolatilities[i] = SABR_PARAMETER.getVolatility(COUPON_IBOR.getFixingTime(), maturity, sampleStrikes[i], forward);
    }

    /*
     * Without extrapolation
     */
    SmileInterpolatorSABR sabrInterp = new SmileInterpolatorSABR();
    InterpolatedSmileFunction smileFunction = new InterpolatedSmileFunction(sabrInterp, forward, sampleStrikes, COUPON_IBOR.getFixingTime(), sampleVolatilities);
    CouponIborInArrearsSmileModelReplicationMethod methodSabrGeneral = new CouponIborInArrearsSmileModelReplicationMethod(smileFunction);
    MultipleCurrencyAmount res2 = methodSabrGeneral.presentValue(COUPON_IBOR, MULTICURVES);
    double ref = res1.getAmount(COUPON_IBOR.getCurrency());
    assertEquals(ref, res2.getAmount(COUPON_IBOR.getCurrency()), Math.abs(ref) * 1.e-1);

    /*
     * With extrapolation
     */
    CapFloorIborSABRCapExtrapolationRightMethod oldMethod = new CapFloorIborSABRCapExtrapolationRightMethod(sampleStrikes[nSample - 1], MU);
    CouponIborInArrearsReplicationMethod methodSabrExtrap = new CouponIborInArrearsReplicationMethod(oldMethod);
    MultipleCurrencyAmount res1Extrap = methodSabrExtrap.presentValue(COUPON_IBOR, SABR_MULTICURVES);

    BenaimDodgsonKainthExtrapolationFunctionProvider provider = new BenaimDodgsonKainthExtrapolationFunctionProvider(MU, MU);
    SmileInterpolatorSABRWithExtrapolation sabrExtrap = new SmileInterpolatorSABRWithExtrapolation(provider);
    InterpolatedSmileFunction smileFunctionExtrap = new InterpolatedSmileFunction(sabrExtrap, forward, sampleStrikes, COUPON_IBOR.getFixingTime(), sampleVolatilities);
    CouponIborInArrearsSmileModelReplicationMethod methodSabrGeneralExtrap = new CouponIborInArrearsSmileModelReplicationMethod(smileFunctionExtrap);
    MultipleCurrencyAmount res2Extrap = methodSabrGeneralExtrap.presentValue(COUPON_IBOR, MULTICURVES);

    double refExtrap = res1Extrap.getAmount(COUPON_IBOR.getCurrency());
    assertEquals(refExtrap, res2Extrap.getAmount(COUPON_IBOR.getCurrency()), Math.abs(refExtrap) * 1.e-6);

  }

  /**
   * Comparing two ways of extrapolation, {@link SmileExtrapolationFunctionSABRProvider}
   */
  @Test(enabled = false)
  public void ComparisonTest() {
    CapFloorIbor[] derivatives = new CapFloorIbor[] {CAP_LONG, CAP_SHORT, FLOOR_SHORT };
    for (final CapFloorIbor derivative : derivatives) {
      double forward = SABR_MULTICURVES.getMulticurveProvider().getSimplyCompoundForwardRate(derivative.getIndex(), derivative.getFixingPeriodStartTime(), derivative.getFixingPeriodEndTime(),
          derivative.getFixingAccrualFactor());
      double maturity = derivative.getFixingPeriodEndTime() - derivative.getFixingPeriodStartTime();
      int nSample = 11;
      double[] sampleStrikes = new double[nSample];
      double[] sampleVolatilities = new double[nSample];
      for (int i = 0; i < nSample; ++i) {
        sampleStrikes[i] = forward * (i * 0.05 + 0.95);
        sampleVolatilities[i] = SABR_PARAMETER.getVolatility(derivative.getFixingTime(), maturity, sampleStrikes[i], forward);
      }

      double muLow = sampleStrikes[0] * BlackFormulaRepository.dualDelta(forward, sampleStrikes[0], derivative.getFixingTime(), sampleVolatilities[0], false) /
          BlackFormulaRepository.price(forward, sampleStrikes[0], derivative.getFixingTime(), sampleVolatilities[0], false);
      double muHigh = -sampleStrikes[nSample - 1] * BlackFormulaRepository.dualDelta(forward, sampleStrikes[nSample - 1], derivative.getFixingTime(), sampleVolatilities[nSample - 1], true) /
          BlackFormulaRepository.price(forward, sampleStrikes[nSample - 1], derivative.getFixingTime(), sampleVolatilities[nSample - 1], true);

      BenaimDodgsonKainthExtrapolationFunctionProvider provider = new BenaimDodgsonKainthExtrapolationFunctionProvider(muLow, muHigh);
      SmileInterpolatorSABRWithExtrapolation sabrExtrap2 = new SmileInterpolatorSABRWithExtrapolation(provider);
      InterpolatedSmileFunction smileFunctionExtrap2 = new InterpolatedSmileFunction(sabrExtrap2, forward, sampleStrikes, derivative.getFixingTime(), sampleVolatilities);
      CapFloorIborInArrearsSmileModelCapGenericReplicationMethod methodSabrGeneralExtrap2 = new CapFloorIborInArrearsSmileModelCapGenericReplicationMethod(smileFunctionExtrap2);
      MultipleCurrencyAmount res2Extrap2 = methodSabrGeneralExtrap2.presentValue(derivative, MULTICURVES);

      ShiftedLogNormalExtrapolationFunctionProvider providerLog = new ShiftedLogNormalExtrapolationFunctionProvider("Quiet");
      SmileInterpolatorSABRWithExtrapolation sabrExtrap2Log = new SmileInterpolatorSABRWithExtrapolation(providerLog);
      InterpolatedSmileFunction smileFunctionExtrap2Log = new InterpolatedSmileFunction(sabrExtrap2Log, forward, sampleStrikes, derivative.getFixingTime(), sampleVolatilities);
      CapFloorIborInArrearsSmileModelCapGenericReplicationMethod methodSabrGeneralExtrap2Log = new CapFloorIborInArrearsSmileModelCapGenericReplicationMethod(smileFunctionExtrap2Log);
      MultipleCurrencyAmount res2Extrap2Log = methodSabrGeneralExtrap2Log.presentValue(derivative, MULTICURVES);

      //      for (int i = 0; i < 300; ++i) {
      //        Double strike = forward * (0.01 + 0.5 * i);
      //        System.out.println(strike + "\t" + smileFunctionExtrap2.getVolatility(strike) + "\t" + smileFunctionExtrap2Log.getVolatility(strike));
      //      }
      System.out.println("\n");
      System.out.println(res2Extrap2 + "\t" + res2Extrap2Log);
    }

    /**
     * Coupon Ibor
     */
    double forward = SABR_MULTICURVES.getMulticurveProvider().getSimplyCompoundForwardRate(COUPON_IBOR.getIndex(), COUPON_IBOR.getFixingPeriodStartTime(), COUPON_IBOR.getFixingPeriodEndTime(),
        COUPON_IBOR.getFixingAccrualFactor());
    double maturity = COUPON_IBOR.getFixingPeriodEndTime() - COUPON_IBOR.getFixingPeriodStartTime();
    int nSample = 1000;
    double[] sampleStrikes = new double[nSample];
    double[] sampleVolatilities = new double[nSample];
    for (int i = 0; i < nSample; ++i) {
      sampleStrikes[i] = forward * (i * 0.001 + 0.2);
      sampleVolatilities[i] = SABR_PARAMETER.getVolatility(COUPON_IBOR.getFixingTime(), maturity, sampleStrikes[i], forward);
    }

    BenaimDodgsonKainthExtrapolationFunctionProvider provider = new BenaimDodgsonKainthExtrapolationFunctionProvider(MU, MU);
    SmileInterpolatorSABRWithExtrapolation sabrExtrap2 = new SmileInterpolatorSABRWithExtrapolation(provider);
    InterpolatedSmileFunction smileFunctionExtrap2 = new InterpolatedSmileFunction(sabrExtrap2, forward, sampleStrikes, COUPON_IBOR.getFixingTime(), sampleVolatilities);
    CouponIborInArrearsSmileModelReplicationMethod methodSabrGeneralExtrap2 = new CouponIborInArrearsSmileModelReplicationMethod(smileFunctionExtrap2);
    MultipleCurrencyAmount res2Extrap2 = methodSabrGeneralExtrap2.presentValue(COUPON_IBOR, MULTICURVES);

    ShiftedLogNormalExtrapolationFunctionProvider providerLog = new ShiftedLogNormalExtrapolationFunctionProvider("Quiet");
    SmileInterpolatorSABRWithExtrapolation sabrExtrap2Log = new SmileInterpolatorSABRWithExtrapolation(providerLog);
    InterpolatedSmileFunction smileFunctionExtrap2Log = new InterpolatedSmileFunction(sabrExtrap2Log, forward, sampleStrikes, COUPON_IBOR.getFixingTime(), sampleVolatilities);
    CouponIborInArrearsSmileModelReplicationMethod methodSabrGeneralExtrap2Log = new CouponIborInArrearsSmileModelReplicationMethod(smileFunctionExtrap2Log);
    MultipleCurrencyAmount res2Extrap2Log = methodSabrGeneralExtrap2Log.presentValue(COUPON_IBOR, MULTICURVES);

    //    for (int i = 0; i < 300; ++i) {
    //      Double strike = forward * (0.01 + 0.5 * i);
    //      System.out.println(strike + "\t" + smileFunctionExtrap2.getVolatility(strike) + "\t" + smileFunctionExtrap2Log.getVolatility(strike));
    //    }
    System.out.println("\n");
    System.out.println(res2Extrap2 + "\t" + res2Extrap2Log);

  }

}
