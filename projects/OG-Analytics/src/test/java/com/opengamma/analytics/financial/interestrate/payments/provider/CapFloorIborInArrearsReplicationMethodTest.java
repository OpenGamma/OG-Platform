/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
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
import com.opengamma.analytics.financial.instrument.payment.CouponFixedDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborDefinition;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CapFloorIbor;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixed;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIbor;
import com.opengamma.analytics.financial.model.option.definition.SABRInterestRateParameters;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.sabrcap.PresentValueSABRCapCalculator;
import com.opengamma.analytics.financial.provider.description.MulticurveProviderDiscountDataSets;
import com.opengamma.analytics.financial.provider.description.SABRDataSets;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.SABRCapProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.SABRCapProviderInterface;
import com.opengamma.analytics.financial.provider.method.CapFloorIborSABRCapMethodInterface;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.math.MathException;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.integration.RungeKuttaIntegrator1D;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Tests regarding the pricing of in-arrears Ibor products by replication.
 */
@Test(groups = TestGroup.UNIT)
public class CapFloorIborInArrearsReplicationMethodTest {

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
  private static final CouponIborInArrearsReplicationMethod METHOD_SABREXTRA_COUPON_IA = new CouponIborInArrearsReplicationMethod(METHOD_SABREXTRA_STD);
  private static final CapFloorIborInArrearsSABRCapGenericReplicationMethod METHOD_SABREXTRA_CAP_IA = new CapFloorIborInArrearsSABRCapGenericReplicationMethod(METHOD_SABREXTRA_STD);

  private static final double TOLERANCE_PV = 1.0E-0;

  //  private static final double TOLERANCE_PV_DELTA = 1.0E+2;

  @Test
  /**
   * Tests the present value using the SABR with extrapolation to the right method to price standard cap/floor.
   * It is suggested not to use the standard SABR method as it can lead to exploding prices for long term contracts.
   */
  public void persentValueSABRExtrapolation() {
    final CapFloorIbor capStandard = new CapFloorIbor(EUR, CAP_LONG.getFixingPeriodEndTime(), CAP_LONG.getPaymentYearFraction(), NOTIONAL, CAP_LONG.getFixingTime(), EURIBOR6M,
        CAP_LONG.getFixingPeriodStartTime(), CAP_LONG.getFixingPeriodEndTime(), CAP_LONG.getFixingAccrualFactor(), STRIKE, IS_CAP);
    final MultipleCurrencyAmount priceStandard = capStandard.accept(PVSCC, SABR_MULTICURVES);
    final double forward = MULTICURVES.getSimplyCompoundForwardRate(CAP_LONG.getIndex(), CAP_LONG.getFixingPeriodStartTime(), CAP_LONG.getFixingPeriodEndTime(), CAP_LONG.getFixingAccrualFactor());
    final double beta = (1.0 + CAP_LONG.getFixingAccrualFactor() * forward) * MULTICURVES.getDiscountFactor(EUR, CAP_LONG.getFixingPeriodEndTime())
        / MULTICURVES.getDiscountFactor(EUR, CAP_LONG.getFixingPeriodStartTime());
    final double strikePart = (1.0 + CAP_LONG.getFixingAccrualFactor() * STRIKE) * priceStandard.getAmount(EUR);
    final RungeKuttaIntegrator1D integrator = new RungeKuttaIntegrator1D(1.0, 1E-8, 10);
    final InArrearsIntegrant integrant = new InArrearsIntegrant(METHOD_SABREXTRA_STD, capStandard, SABR_MULTICURVES);
    double integralPart;
    try {
      integralPart = integrator.integrate(integrant, STRIKE, STRIKE + 2.0);
    } catch (final Exception e) {
      throw new MathException(e);
    }
    integralPart *= 2.0 * CAP_LONG.getFixingAccrualFactor();
    final MultipleCurrencyAmount price = METHOD_SABREXTRA_CAP_IA.presentValue(CAP_LONG, SABR_MULTICURVES);
    final double priceExpected = (strikePart + integralPart) / beta;
    assertEquals("Cap/floor IA - SABR pricing", priceExpected, price.getAmount(EUR), TOLERANCE_PV);
    final double priceExpected2 = 203548.836; // From previous run
    assertEquals("Cap/floor IA - SABR pricing", priceExpected2, price.getAmount(EUR), TOLERANCE_PV);
  }

  @Test
  /**
   * Compare the present value by replication to a value without adjustment.
   */
  public void presentValueSABRNoAdjustment() {
    final double forward = MULTICURVES.getSimplyCompoundForwardRate(EURIBOR6M, CAP_LONG.getFixingPeriodStartTime(), CAP_LONG.getFixingPeriodEndTime(), CAP_LONG.getFixingAccrualFactor());
    final MultipleCurrencyAmount priceIbor = METHOD_SABREXTRA_COUPON_IA.presentValue(COUPON_IBOR, SABR_MULTICURVES);
    assertTrue("Coupon IA - SABR pricing: coupon = cap with strike eps",
        priceIbor.getAmount(EUR) > forward * NOTIONAL * CAP_LONG.getPaymentYearFraction() * MULTICURVES.getDiscountFactor(EUR, CAP_LONG.getPaymentTime()));
  }

  @Test
  /**
   * Check different parity relationship for the present value: long/short, coupon with cap with strike 0.
   */
  public void persentValueSABRExtrapolationParity() {
    final MultipleCurrencyAmount priceCapLong = METHOD_SABREXTRA_CAP_IA.presentValue(CAP_LONG, SABR_MULTICURVES);
    final MultipleCurrencyAmount priceCapShort = METHOD_SABREXTRA_CAP_IA.presentValue(CAP_SHORT, SABR_MULTICURVES);
    assertEquals("Cap/floor IA - SABR pricing: long-short parity", priceCapLong.getAmount(EUR), -priceCapShort.getAmount(EUR), TOLERANCE_PV);
    final MultipleCurrencyAmount priceIbor = METHOD_SABREXTRA_COUPON_IA.presentValue(COUPON_IBOR, SABR_MULTICURVES);
    final CapFloorIborDefinition cap0Definition = new CapFloorIborDefinition(EUR, END_ACCRUAL_DATE, START_ACCRUAL_DATE, END_ACCRUAL_DATE, ACCRUAL_FACTOR, NOTIONAL, FIXING_DATE, EURIBOR6M, 0.0, IS_CAP, CALENDAR);
    final CapFloorIbor cap0 = (CapFloorIbor) cap0Definition.toDerivative(REFERENCE_DATE);
    final MultipleCurrencyAmount priceCap0 = METHOD_SABREXTRA_CAP_IA.presentValue(cap0, SABR_MULTICURVES);
    assertEquals("Coupon IA - SABR pricing: coupon = cap with strike 0", priceCap0.getAmount(EUR), priceIbor.getAmount(EUR), TOLERANCE_PV);
    final MultipleCurrencyAmount priceFloorShort = METHOD_SABREXTRA_CAP_IA.presentValue(FLOOR_SHORT, SABR_MULTICURVES);
    final MultipleCurrencyAmount priceStrike = COUPON_STRIKE.accept(PVDC, MULTICURVES);
    assertEquals("Cap/floor IA - SABR pricing: cap/floor parity", priceIbor.getAmount(EUR) - priceStrike.getAmount(EUR), priceCapLong.getAmount(EUR) + priceFloorShort.getAmount(EUR), 5.0E+4);
    //TODO: check further the difference (numerical?)
  }

  @Test(enabled = false)
  /**
   * Performance test. "enabled = false" for the standard testing.
   */
  public void sabrExtrapolationPerformance() {
    long startTime, endTime;
    final int nbTest = 100;
    final double[] prices = new double[nbTest];
    @SuppressWarnings("unused")
    double sum = 0.0;
    startTime = System.currentTimeMillis();
    for (int looptest = 0; looptest < nbTest; looptest++) {
      final CapFloorIborSABRCapExtrapolationRightMethod methodSABRExtraStd = new CapFloorIborSABRCapExtrapolationRightMethod(CUT_OFF_STRIKE, MU); //To start with a "clean" method
      final CapFloorIborInArrearsSABRCapGenericReplicationMethod methodSABRExtraIA = new CapFloorIborInArrearsSABRCapGenericReplicationMethod(methodSABRExtraStd);
      prices[looptest] = methodSABRExtraIA.presentValue(CAP_LONG, SABR_MULTICURVES).getAmount(EUR);
      sum += prices[looptest];
    }
    endTime = System.currentTimeMillis();
    System.out.println(nbTest + " IA cap by replication (price): " + (endTime - startTime) + " ms");
    // Performance note: price: 07-Jun-11: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 385 ms for 100 cap 9Y.
    // TODO: review performance: the SABR extrapolation calibration is done at each integration step: result could be stored.
  }

  /**
   * Inner class to implement the integration used in price replication.
   */
  private static class InArrearsIntegrant extends Function1D<Double, Double> {

    /**
     * The base method for the pricing of standard cap/floors.
     */
    private final CapFloorIborSABRCapMethodInterface _baseMethod;
    /**
     * The standard cap/floor used for replication.
     */
    private final CapFloorIbor _capStandard;
    /**
     * The SABR data bundle used in the standard cap/floor pricing.
     */
    private final SABRCapProviderInterface _sabrData;

    /**
     * Constructor with the required data.
     * @param baseMethod The base method for the pricing of standard cap/floors.
     * @param capStandard The standard cap/floor used for replication.
     * @param sabrData The SABR data bundle used in the standard cap/floor pricing.
     */
    public InArrearsIntegrant(final CapFloorIborSABRCapMethodInterface baseMethod, final CapFloorIbor capStandard, final SABRCapProviderInterface sabr) {
      this._baseMethod = baseMethod;
      this._capStandard = capStandard;
      this._sabrData = sabr;
    }

    @SuppressWarnings("synthetic-access")
    @Override
    public Double evaluate(final Double x) {
      final CapFloorIbor capStrike = _capStandard.withStrike(x);
      return _baseMethod.presentValue(capStrike, _sabrData).getAmount(EUR);
    }
  }

}
