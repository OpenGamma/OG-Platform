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
import com.opengamma.analytics.financial.instrument.payment.CouponFixedDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborDefinition;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CapFloorIbor;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixed;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIbor;
import com.opengamma.analytics.financial.model.option.definition.SABRInterestRateParameters;
import com.opengamma.analytics.financial.model.volatility.smile.fitting.interpolation.SmileInterpolatorSABR;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.sabrcap.PresentValueSABRCapCalculator;
import com.opengamma.analytics.financial.provider.description.MulticurveProviderDiscountDataSets;
import com.opengamma.analytics.financial.provider.description.SABRDataSets;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.SABRCapProviderDiscount;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * 
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
  private static final CouponFixedDefinition COUPON_STRIKE_DEFINITION = new CouponFixedDefinition(COUPON_IBOR_IA_DEFINITION, STRIKE);
  private static final CapFloorIborDefinition CAP_IA_SHORT_DEFINITION = new CapFloorIborDefinition(EUR, END_ACCRUAL_DATE, START_ACCRUAL_DATE, END_ACCRUAL_DATE, ACCRUAL_FACTOR, -NOTIONAL, FIXING_DATE,
      EURIBOR6M, STRIKE, IS_CAP, CALENDAR);
  private static final CapFloorIborDefinition FLOOR_IA_SHORT_DEFINITION = new CapFloorIborDefinition(EUR, END_ACCRUAL_DATE, START_ACCRUAL_DATE, END_ACCRUAL_DATE, ACCRUAL_FACTOR, -NOTIONAL,
      FIXING_DATE, EURIBOR6M, STRIKE, !IS_CAP, CALENDAR);
  // To derivative
  private static final CapFloorIbor CAP_LONG = (CapFloorIbor) CAP_IA_LONG_DEFINITION.toDerivative(REFERENCE_DATE);
  private static final CouponIbor COUPON_IBOR = (CouponIbor) COUPON_IBOR_IA_DEFINITION.toDerivative(REFERENCE_DATE);
  private static final CouponFixed COUPON_STRIKE = COUPON_STRIKE_DEFINITION.toDerivative(REFERENCE_DATE);
  private static final CapFloorIbor CAP_SHORT = (CapFloorIbor) CAP_IA_SHORT_DEFINITION.toDerivative(REFERENCE_DATE);
  private static final CapFloorIbor FLOOR_SHORT = (CapFloorIbor) FLOOR_IA_SHORT_DEFINITION.toDerivative(REFERENCE_DATE);
  // Methods
  private static final PresentValueSABRCapCalculator PVSCC = PresentValueSABRCapCalculator.getInstance();
  private static final PresentValueDiscountingCalculator PVDC = PresentValueDiscountingCalculator.getInstance();
  private static final double CUT_OFF_STRIKE = 0.08;
  private static final double MU = 8.00;
  private static final CapFloorIborSABRCapExtrapolationRightMethod METHOD_SABREXTRA_STD = new CapFloorIborSABRCapExtrapolationRightMethod(CUT_OFF_STRIKE, MU);
  private static final CapFloorIborSABRCapMethod METHOD_SABR_STD = CapFloorIborSABRCapMethod.getInstance();
  private static final CouponIborInArrearsReplicationMethod METHOD_SABREXTRA_COUPON_IA = new CouponIborInArrearsReplicationMethod(METHOD_SABR_STD);

  @Test
  public void test() {
    final CapFloorIborInArrearsSABRCapGenericReplicationMethod methodSabr = new CapFloorIborInArrearsSABRCapGenericReplicationMethod(METHOD_SABR_STD);
    MultipleCurrencyAmount res1 = methodSabr.presentValue(CAP_LONG, SABR_MULTICURVES);

    double forward = SABR_MULTICURVES.getMulticurveProvider().getSimplyCompoundForwardRate(CAP_LONG.getIndex(), CAP_LONG.getFixingPeriodStartTime(), CAP_LONG.getFixingPeriodEndTime(),
        CAP_LONG.getFixingAccrualFactor());
    double maturity = CAP_LONG.getFixingPeriodEndTime() - CAP_LONG.getFixingPeriodStartTime();

    int nSample = 2000;
    double[] sampleStrikes = new double[nSample];
    double[] sampleVolatilities = new double[nSample];
    for (int i = 0; i < nSample; ++i) {
      sampleStrikes[i] = forward * (i * 0.001 + 0.002);
      sampleVolatilities[i] = SABR_PARAMETER.getVolatility(CAP_LONG.getFixingTime(), maturity, sampleStrikes[i], forward);
    }

    SmileInterpolatorSABR sabrInterp = new SmileInterpolatorSABR();
    Function1D<Double, Double> volatilityFunction = sabrInterp.getVolatilityFunction(forward, sampleStrikes, CAP_LONG.getFixingTime(), sampleVolatilities);
    final CapFloorIborInArrearsSmileModelCapGenericReplicationMethod methodSabrGeneral = new CapFloorIborInArrearsSmileModelCapGenericReplicationMethod(volatilityFunction);
    MultipleCurrencyAmount res2 = methodSabrGeneral.presentValue(CAP_LONG, MULTICURVES);

    double ref = res1.getAmount(CAP_LONG.getCurrency());
    assertEquals(ref, res2.getAmount(CAP_LONG.getCurrency()), ref * 1.e-8);
  }
}
