/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.payments.method;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import javax.time.calendar.Period;
import javax.time.calendar.ZonedDateTime;

import org.testng.annotations.Test;

import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.instrument.index.IborIndex;
import com.opengamma.financial.instrument.payment.CapFloorIborDefinition;
import com.opengamma.financial.instrument.payment.CouponFixedDefinition;
import com.opengamma.financial.instrument.payment.CouponIborDefinition;
import com.opengamma.financial.interestrate.ParRateCalculator;
import com.opengamma.financial.interestrate.PresentValueSABRCalculator;
import com.opengamma.financial.interestrate.TestsDataSetsSABR;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.interestrate.method.PricingMethod;
import com.opengamma.financial.interestrate.payments.CapFloorIbor;
import com.opengamma.financial.interestrate.payments.CouponFixed;
import com.opengamma.financial.interestrate.payments.CouponIbor;
import com.opengamma.financial.model.option.definition.SABRInterestRateDataBundle;
import com.opengamma.financial.model.option.definition.SABRInterestRateParameters;
import com.opengamma.financial.schedule.ScheduleCalculator;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.integration.RungeKuttaIntegrator1D;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.time.DateUtils;

/**
 * Tests regarding the pricing of in-arrears Ibor products by replication.
 */
public class CapFloorIborInArrearsReplicationMethodTest {

  // Euribor 6m
  private static final Period TENOR = Period.ofMonths(6);
  private static final int SETTLEMENT_DAYS = 2;
  private static final Calendar CALENDAR = new MondayToFridayCalendar("A");
  private static final DayCount DAY_COUNT_INDEX = DayCountFactory.INSTANCE.getDayCount("Actual/360");
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Modified Following");
  private static final boolean IS_EOM = true;
  private static final Currency CUR = Currency.EUR;
  private static final IborIndex INDEX = new IborIndex(CUR, TENOR, SETTLEMENT_DAYS, CALENDAR, DAY_COUNT_INDEX, BUSINESS_DAY, IS_EOM);
  // Dates
  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2011, 6, 7);
  private static final ZonedDateTime START_ACCRUAL_DATE = ScheduleCalculator.getAdjustedDate(REFERENCE_DATE, Period.ofYears(9), BUSINESS_DAY, CALENDAR, IS_EOM);
  private static final ZonedDateTime END_ACCRUAL_DATE = ScheduleCalculator.getAdjustedDate(START_ACCRUAL_DATE, TENOR, BUSINESS_DAY, CALENDAR, IS_EOM);
  private static final double ACCRUAL_FACTOR = DAY_COUNT_INDEX.getDayCountFraction(START_ACCRUAL_DATE, END_ACCRUAL_DATE);
  private static final ZonedDateTime FIXING_DATE = ScheduleCalculator.getAdjustedDate(END_ACCRUAL_DATE, -SETTLEMENT_DAYS, CALENDAR);
  private static final double NOTIONAL = 100000000; //100m
  private static final double STRIKE = 0.04;
  private static final boolean IS_CAP = true;
  // Definition description: In arrears
  private static final CapFloorIborDefinition CAP_LONG_DEFINITION = new CapFloorIborDefinition(CUR, END_ACCRUAL_DATE, START_ACCRUAL_DATE, END_ACCRUAL_DATE, ACCRUAL_FACTOR, NOTIONAL, FIXING_DATE,
      INDEX, STRIKE, IS_CAP);
  private static final CouponIborDefinition COUPON_IBOR_DEFINITION = new CouponIborDefinition(CUR, END_ACCRUAL_DATE, START_ACCRUAL_DATE, END_ACCRUAL_DATE, ACCRUAL_FACTOR, NOTIONAL, FIXING_DATE, INDEX);
  private static final CouponFixedDefinition COUPON_STRIKE_DEFINITION = new CouponFixedDefinition(COUPON_IBOR_DEFINITION, STRIKE);
  private static final CapFloorIborDefinition CAP_SHORT_DEFINITION = new CapFloorIborDefinition(CUR, END_ACCRUAL_DATE, START_ACCRUAL_DATE, END_ACCRUAL_DATE, ACCRUAL_FACTOR, -NOTIONAL, FIXING_DATE,
      INDEX, STRIKE, IS_CAP);
  private static final CapFloorIborDefinition FLOOR_SHORT_DEFINITION = new CapFloorIborDefinition(CUR, END_ACCRUAL_DATE, START_ACCRUAL_DATE, END_ACCRUAL_DATE, ACCRUAL_FACTOR, -NOTIONAL, FIXING_DATE,
      INDEX, STRIKE, !IS_CAP);
  // To derivative
  private static final String FUNDING_CURVE_NAME = "Funding";
  private static final String FORWARD_CURVE_NAME = "Forward";
  private static final String[] CURVES_NAME = {FUNDING_CURVE_NAME, FORWARD_CURVE_NAME};
  private static final CapFloorIbor CAP_LONG = (CapFloorIbor) CAP_LONG_DEFINITION.toDerivative(REFERENCE_DATE, CURVES_NAME);
  private static final CouponIbor COUPON_IBOR = (CouponIbor) COUPON_IBOR_DEFINITION.toDerivative(REFERENCE_DATE, CURVES_NAME);
  private static final CouponFixed COUPON_STRIKE = COUPON_STRIKE_DEFINITION.toDerivative(REFERENCE_DATE, CURVES_NAME);
  private static final CapFloorIbor CAP_SHORT = (CapFloorIbor) CAP_SHORT_DEFINITION.toDerivative(REFERENCE_DATE, CURVES_NAME);
  private static final CapFloorIbor FLOOR_SHORT = (CapFloorIbor) FLOOR_SHORT_DEFINITION.toDerivative(REFERENCE_DATE, CURVES_NAME);
  // Methods
  private static final PresentValueSABRCalculator PVC = PresentValueSABRCalculator.getInstance();
  private static final double CUT_OFF_STRIKE = 0.08;
  private static final double MU = 8.00;
  private static final CapFloorIborSABRExtrapolationRightMethod METHOD_SABREXTRA_STD = new CapFloorIborSABRExtrapolationRightMethod(CUT_OFF_STRIKE, MU);
  private static final CouponIborInArrearsReplicationMethod METHOD_SABREXTRA_COUPON_IA = new CouponIborInArrearsReplicationMethod(METHOD_SABREXTRA_STD);
  private static final CapFloorIborInArrearsGenericReplicationMethod METHOD_SABREXTRA_CAP_IA = new CapFloorIborInArrearsGenericReplicationMethod(METHOD_SABREXTRA_STD);
  private static final ParRateCalculator PRC = ParRateCalculator.getInstance();

  private static final YieldCurveBundle CURVES = TestsDataSetsSABR.createCurves1();
  private static final SABRInterestRateParameters SABR_PARAMETERS = TestsDataSetsSABR.createSABR1();
  private static final SABRInterestRateDataBundle SABR_BUNDLE = new SABRInterestRateDataBundle(SABR_PARAMETERS, CURVES);

  @Test
  /**
   * Tests the present value using the SABR with extrapolation to the right method to price standard cap/floor.
   * It is suggested not to use the standard SABR method as it can lead to exploding prices for long term contracts.
   */
  public void persentValueSABRExtrapolation() {
    CapFloorIbor capStandard = new CapFloorIbor(CUR, CAP_LONG.getFixingPeriodEndTime(), FUNDING_CURVE_NAME, CAP_LONG.getPaymentYearFraction(), NOTIONAL, CAP_LONG.getFixingTime(), INDEX,
        CAP_LONG.getFixingPeriodStartTime(), CAP_LONG.getFixingPeriodEndTime(), CAP_LONG.getFixingYearFraction(), FORWARD_CURVE_NAME, STRIKE, IS_CAP);
    double priceStandard = PVC.visit(capStandard, SABR_BUNDLE);
    double beta = CURVES.getCurve(FORWARD_CURVE_NAME).getDiscountFactor(CAP_LONG.getFixingPeriodStartTime()) / CURVES.getCurve(FORWARD_CURVE_NAME).getDiscountFactor(CAP_LONG.getFixingPeriodEndTime())
        * CURVES.getCurve(FUNDING_CURVE_NAME).getDiscountFactor(CAP_LONG.getFixingPeriodEndTime()) / CURVES.getCurve(FUNDING_CURVE_NAME).getDiscountFactor(CAP_LONG.getFixingPeriodStartTime());
    double strikePart = (1.0 + CAP_LONG.getFixingYearFraction() * STRIKE) * priceStandard;
    final RungeKuttaIntegrator1D integrator = new RungeKuttaIntegrator1D(1.0, 1E-8, 10);
    final InArrearsIntegrant integrant = new InArrearsIntegrant(METHOD_SABREXTRA_STD, capStandard, SABR_BUNDLE);
    double integralPart;
    try {
      integralPart = integrator.integrate(integrant, STRIKE, STRIKE + 2.0);
    } catch (final Exception e) {
      throw new RuntimeException(e);
    }
    integralPart *= 2.0 * CAP_LONG.getFixingYearFraction();
    final CurrencyAmount price = METHOD_SABREXTRA_CAP_IA.presentValue(CAP_LONG, SABR_BUNDLE);
    double priceExpected = (strikePart + integralPart) / beta;
    assertEquals("Cap/floor IA - SABR pricing", priceExpected, price.getAmount(), 1E+0);
    double priceExpected2 = 451870.184; // From previous run
    assertEquals("Cap/floor IA - SABR pricing", priceExpected2, price.getAmount(), 1E-2);
  }

  @Test
  /**
   * Compare the present value by replication to a value without adjustment.
   */
  public void presentValueSABRNoAdjustment() {
    double forward = PRC.visit(CAP_LONG, CURVES);
    final CurrencyAmount priceIbor = METHOD_SABREXTRA_COUPON_IA.presentValue(COUPON_IBOR, SABR_BUNDLE);
    assertTrue("Coupon IA - SABR pricing: coupon = cap with strike eps", priceIbor.getAmount() > forward * NOTIONAL * CAP_LONG.getPaymentYearFraction()
        * CURVES.getCurve(FUNDING_CURVE_NAME).getDiscountFactor(CAP_LONG.getPaymentTime()));
  }

  @Test
  /**
   * Check different parity relationship for the present value: long/short, coupon with cap with strike 0.
   */
  public void persentValueSABRExtrapolationParity() {
    final CurrencyAmount priceCapLong = METHOD_SABREXTRA_CAP_IA.presentValue(CAP_LONG, SABR_BUNDLE);
    final CurrencyAmount priceCapShort = METHOD_SABREXTRA_CAP_IA.presentValue(CAP_SHORT, SABR_BUNDLE);
    assertEquals("Cap/floor - SABR pricing: long-short parity", priceCapLong.getAmount(), -priceCapShort.getAmount(), 1E-2);
    final CurrencyAmount priceIbor = METHOD_SABREXTRA_COUPON_IA.presentValue(COUPON_IBOR, SABR_BUNDLE);
    final CapFloorIborDefinition cap0Definition = new CapFloorIborDefinition(CUR, END_ACCRUAL_DATE, START_ACCRUAL_DATE, END_ACCRUAL_DATE, ACCRUAL_FACTOR, NOTIONAL, FIXING_DATE, INDEX, 0.0, IS_CAP);
    final CapFloorIbor cap0 = (CapFloorIbor) cap0Definition.toDerivative(REFERENCE_DATE, CURVES_NAME);
    CurrencyAmount priceCap0 = METHOD_SABREXTRA_CAP_IA.presentValue(cap0, SABR_BUNDLE);
    assertEquals("Coupon IA - SABR pricing: coupon = cap with strike 0", priceCap0.getAmount(), priceIbor.getAmount(), 1E-2);
    final CurrencyAmount priceFloorShort = METHOD_SABREXTRA_CAP_IA.presentValue(FLOOR_SHORT, SABR_BUNDLE);
    final double priceStrike = PVC.visit(COUPON_STRIKE, CURVES);
    assertEquals("Cap/floor IA - SABR pricing: cap/floor parity", priceIbor.getAmount() - priceStrike, priceCapLong.getAmount() + priceFloorShort.getAmount(), 2.0E+4);
    //TODO: check further the difference (numerical?)
  }

  @Test(enabled = false)
  /**
   * Performance test. "enabled = false" for the standard testing.
   */
  public void sabrExtrapolationPerformance() {
    long startTime, endTime;
    final int nbTest = 100;
    double[] prices = new double[nbTest];
    double sum = 0.0;
    startTime = System.currentTimeMillis();
    for (int looptest = 0; looptest < nbTest; looptest++) {
      CapFloorIborSABRExtrapolationRightMethod methodSABRExtraStd = new CapFloorIborSABRExtrapolationRightMethod(CUT_OFF_STRIKE, MU); //To start with a "clean" method
      CapFloorIborInArrearsGenericReplicationMethod methodSABRExtraIA = new CapFloorIborInArrearsGenericReplicationMethod(methodSABRExtraStd);
      prices[looptest] = methodSABRExtraIA.presentValue(CAP_LONG, SABR_BUNDLE).getAmount();
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
  private class InArrearsIntegrant extends Function1D<Double, Double> {

    /**
     * The base method for the pricing of standard cap/floors.
     */
    private final PricingMethod _baseMethod;
    /**
     * The standard cap/floor used for replication.
     */
    private final CapFloorIbor _capStandard;
    /**
     * The SABR data bundle used in the standard cap/floor pricing.
     */
    private final SABRInterestRateDataBundle _sabrData;

    /**
     * Constructor with the required data.
     * @param baseMethod The base method for the pricing of standard cap/floors.
     * @param capStandard The standard cap/floor used for replication.
     * @param sabrData The SABR data bundle used in the standard cap/floor pricing.
     */
    public InArrearsIntegrant(final PricingMethod baseMethod, final CapFloorIbor capStandard, final SABRInterestRateDataBundle sabrData) {
      this._baseMethod = baseMethod;
      this._capStandard = capStandard;
      this._sabrData = sabrData;
    }

    @Override
    public Double evaluate(final Double x) {
      CapFloorIbor capStrike = _capStandard.withStrike(x);
      return _baseMethod.presentValue(capStrike, _sabrData).getAmount();
    }
  }
}
