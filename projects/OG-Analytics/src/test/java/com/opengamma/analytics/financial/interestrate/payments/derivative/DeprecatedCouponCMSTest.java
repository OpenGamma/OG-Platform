/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.payments.derivative;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.LocalTime;
import org.threeten.bp.Period;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.annuity.AnnuityCouponFixedDefinition;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityCouponIborDefinition;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexSwap;
import com.opengamma.analytics.financial.instrument.payment.CouponCMSDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedIborDefinition;
import com.opengamma.analytics.financial.interestrate.ParRateCalculator;
import com.opengamma.analytics.financial.interestrate.PresentValueCalculator;
import com.opengamma.analytics.financial.interestrate.PresentValueSABRCalculator;
import com.opengamma.analytics.financial.interestrate.TestsDataSetsSABR;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.AnnuityCouponFixed;
import com.opengamma.analytics.financial.interestrate.payments.method.CouponCMSSABRReplicationMethod;
import com.opengamma.analytics.financial.interestrate.swap.derivative.SwapFixedCoupon;
import com.opengamma.analytics.financial.model.option.definition.SABRInterestRateDataBundle;
import com.opengamma.analytics.financial.model.option.definition.SABRInterestRateParameters;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.BlackFunctionData;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.BlackPriceFunction;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.analytics.financial.model.volatility.smile.function.SABRBerestyckiVolatilityFunction;
import com.opengamma.analytics.financial.model.volatility.smile.function.SABRHaganAlternativeVolatilityFunction;
import com.opengamma.analytics.financial.model.volatility.smile.function.SABRHaganVolatilityFunction;
import com.opengamma.analytics.financial.model.volatility.smile.function.SABRPaulotVolatilityFunction;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.math.MathException;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.integration.RungeKuttaIntegrator1D;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Tests of the CMS coupons.
 * @deprecated This class tests deprecated functionality.
 */
@Deprecated
@Test(groups = TestGroup.UNIT)
public class DeprecatedCouponCMSTest {
  //Swap 5Y
  private static final Currency CUR = Currency.EUR;
  private static final Calendar CALENDAR = new MondayToFridayCalendar("A");
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventions.MODIFIED_FOLLOWING;
  private static final boolean IS_EOM = true;
  private static final Period ANNUITY_TENOR = Period.ofYears(5);
  private static final ZonedDateTime SETTLEMENT_DATE = DateUtils.getUTCDate(2014, 3, 17);
  //Fixed leg: Semi-annual bond
  private static final Period FIXED_PAYMENT_PERIOD = Period.ofMonths(6);
  private static final DayCount FIXED_DAY_COUNT = DayCounts.THIRTY_U_360;
  private static final double RATE = 0.0325;
  private static final boolean FIXED_IS_PAYER = true;
  private static final AnnuityCouponFixedDefinition FIXED_ANNUITY = AnnuityCouponFixedDefinition.from(CUR, SETTLEMENT_DATE, ANNUITY_TENOR, FIXED_PAYMENT_PERIOD, CALENDAR, FIXED_DAY_COUNT,
      BUSINESS_DAY, IS_EOM, 1.0, RATE, FIXED_IS_PAYER);
  //Ibor leg: quarterly money
  private static final Period INDEX_TENOR = Period.ofMonths(3);
  private static final int SETTLEMENT_DAYS = 2;
  private static final DayCount DAY_COUNT = DayCounts.ACT_360;
  private static final IborIndex IBOR_INDEX = new IborIndex(CUR, INDEX_TENOR, SETTLEMENT_DAYS, DAY_COUNT, BUSINESS_DAY, IS_EOM, "Ibor");
  private static final AnnuityCouponIborDefinition IBOR_ANNUITY = AnnuityCouponIborDefinition.from(SETTLEMENT_DATE, ANNUITY_TENOR, 1.0, IBOR_INDEX, !FIXED_IS_PAYER, CALENDAR);
  // CMS coupon construction
  private static final IndexSwap CMS_INDEX = new IndexSwap(FIXED_PAYMENT_PERIOD, FIXED_DAY_COUNT, IBOR_INDEX, ANNUITY_TENOR, CALENDAR);
  private static final SwapFixedIborDefinition SWAP_DEFINITION = new SwapFixedIborDefinition(FIXED_ANNUITY, IBOR_ANNUITY);
  private static final ZonedDateTime PAYMENT_DATE = DateUtils.getUTCDate(2014, 6, 17); // Prefixed
  private static final ZonedDateTime FIXING_DATE = ScheduleCalculator.getAdjustedDate(SETTLEMENT_DATE, -SETTLEMENT_DAYS, CALENDAR);
  private static final ZonedDateTime ACCRUAL_START_DATE = SETTLEMENT_DATE;
  private static final ZonedDateTime ACCRUAL_END_DATE = PAYMENT_DATE;
  private static final DayCount PAYMENT_DAY_COUNT = DayCounts.ACT_360;
  private static final double ACCRUAL_FACTOR = PAYMENT_DAY_COUNT.getDayCountFraction(ACCRUAL_START_DATE, ACCRUAL_END_DATE);
  private static final double NOTIONAL = 1000000; //1m
  private static final CouponCMSDefinition CMS_COUPON_RECEIVER_DEFINITION = CouponCMSDefinition.from(PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR, NOTIONAL, FIXING_DATE,
      SWAP_DEFINITION, CMS_INDEX);
  // to derivatives
  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2010, 8, 18);
  private static final String FUNDING_CURVE_NAME = "Funding";
  private static final String FORWARD_CURVE_NAME = "Forward";
  private static final String[] CURVES_NAME = {FUNDING_CURVE_NAME, FORWARD_CURVE_NAME };
  private static final SwapFixedCoupon<Coupon> SWAP = SWAP_DEFINITION.toDerivative(REFERENCE_DATE, CURVES_NAME);

  private static final CouponCMS CMS_COUPON_RECEIVER = (CouponCMS) CMS_COUPON_RECEIVER_DEFINITION.toDerivative(REFERENCE_DATE, CURVES_NAME);

  // Calculators
  private static final ParRateCalculator PRC = ParRateCalculator.getInstance();
  private static final PresentValueSABRCalculator PVC = PresentValueSABRCalculator.getInstance();

  @Test
  public void testGetter() {
    final DayCount actAct = DayCounts.ACT_ACT_ISDA;
    final ZonedDateTime zonedDate = ZonedDateTime.of(LocalDateTime.of(REFERENCE_DATE.toLocalDate(), LocalTime.MIDNIGHT), ZoneOffset.UTC);
    final double fixingTime = actAct.getDayCountFraction(zonedDate, FIXING_DATE);
    assertEquals(fixingTime, CMS_COUPON_RECEIVER.getFixingTime(), 1E-10);
    assertEquals(SWAP, CMS_COUPON_RECEIVER.getUnderlyingSwap());
    assertEquals(NOTIONAL, CMS_COUPON_RECEIVER.getNotional(), 1E-10);
  }

  @Test
  public void testWithNotional() {
    final double notional = NOTIONAL + 10000;
    final CouponCMS coupon = new CouponCMS(CUR, 0.25, FUNDING_CURVE_NAME, 0.25, NOTIONAL, 0.25, SWAP, 0.25);
    final CouponCMS expected = new CouponCMS(CUR, 0.25, FUNDING_CURVE_NAME, 0.25, notional, 0.25, SWAP, 0.25);
    assertEquals(expected, coupon.withNotional(notional));
  }

  @Test
  public void testHashCodeEquals() {
    final double paymentTime = 1.5;
    final double paymentYearFraction = 0.25;
    final double fixingTime = 0.245;
    final double settlementTime = 1.51;
    final CouponCMS coupon = new CouponCMS(CUR, paymentTime, FUNDING_CURVE_NAME, paymentYearFraction, NOTIONAL, fixingTime, SWAP, settlementTime);
    CouponCMS other = new CouponCMS(CUR, paymentTime, FUNDING_CURVE_NAME, paymentYearFraction, NOTIONAL, fixingTime, SWAP, settlementTime);
    assertEquals(coupon, other);
    assertEquals(coupon.hashCode(), other.hashCode());
    other = new CouponCMS(Currency.AUD, paymentTime, FUNDING_CURVE_NAME, paymentYearFraction, NOTIONAL, fixingTime, SWAP, settlementTime);
    assertFalse(other.equals(coupon));
    other = new CouponCMS(CUR, paymentTime + 1e-8, FUNDING_CURVE_NAME, paymentYearFraction, NOTIONAL, fixingTime, SWAP, settlementTime);
    assertFalse(other.equals(coupon));
    other = new CouponCMS(CUR, paymentTime, FORWARD_CURVE_NAME, paymentYearFraction, NOTIONAL, fixingTime, SWAP, settlementTime);
    assertFalse(other.equals(coupon));
    other = new CouponCMS(CUR, paymentTime, FUNDING_CURVE_NAME, paymentYearFraction + 0.1, NOTIONAL, fixingTime, SWAP, settlementTime);
    assertFalse(other.equals(coupon));
    other = new CouponCMS(CUR, paymentTime, FUNDING_CURVE_NAME, paymentYearFraction, NOTIONAL + 10000, fixingTime, SWAP, settlementTime);
    assertFalse(other.equals(coupon));
    other = new CouponCMS(CUR, paymentTime, FUNDING_CURVE_NAME, paymentYearFraction, NOTIONAL, fixingTime + 1e-8, SWAP, settlementTime);
    assertFalse(other.equals(coupon));
    other = new CouponCMS(CUR, paymentTime, FUNDING_CURVE_NAME, paymentYearFraction, NOTIONAL, fixingTime, SWAP.withNotional(NOTIONAL + 1000), settlementTime);
    assertFalse(other.equals(coupon));
    other = new CouponCMS(CUR, paymentTime, FUNDING_CURVE_NAME, paymentYearFraction, NOTIONAL, fixingTime, SWAP, settlementTime + 1e-8);
    assertFalse(other.equals(coupon));
  }

  @Test
  public void testPriceReplication() {
    final YieldCurveBundle curves = TestsDataSetsSABR.createCurves1();
    final SABRInterestRateParameters sabrParameter = TestsDataSetsSABR.createSABR1();
    final SABRInterestRateDataBundle sabrBundle = new SABRInterestRateDataBundle(sabrParameter, curves);
    final double forward = SWAP.accept(PRC, curves);
    final double discountFactor = curves.getCurve(FUNDING_CURVE_NAME).getDiscountFactor(CMS_COUPON_RECEIVER.getPaymentTime());
    final CMSIntegrant integrant = new CMSIntegrant(CMS_COUPON_RECEIVER, sabrParameter, forward);
    final double factor = discountFactor / integrant.h(forward) * integrant.g(forward);
    final double strike = 0; //CMS swap is equivalent to CMS cap with strike 0 (when rates are always positive).
    final double strikePart = integrant.k(strike) * integrant.bs(strike, forward);
    final double absoluteTolerance = 1E+2;
    final double relativeTolerance = 1.0;
    final int nbIteration = 15;
    final RungeKuttaIntegrator1D integratorOG = new RungeKuttaIntegrator1D(absoluteTolerance, relativeTolerance, nbIteration);
    final double integrationInterval = 1.00;
    double integralPart;
    try {
      integralPart = integratorOG.integrate(integrant, strike, strike + integrationInterval);
    } catch (final Exception e) {
      throw new MathException(e);
    }
    final double priceCMS = factor * (strikePart + integralPart) * CMS_COUPON_RECEIVER.getNotional() * CMS_COUPON_RECEIVER.getPaymentYearFraction();
    assertEquals(8854.551, priceCMS, 1E-2);
    // Price not verified yet: from previous run.
    final CouponCMSSABRReplicationMethod replication = CouponCMSSABRReplicationMethod.getInstance();
    final double priceCMS_method = replication.presentValue(CMS_COUPON_RECEIVER, sabrBundle).getAmount();
    assertEquals(priceCMS, priceCMS_method, 1.5); // Different precision in integration.
    final double priceCMS_calculator = CMS_COUPON_RECEIVER.accept(PVC, sabrBundle);
    assertEquals(priceCMS_method, priceCMS_calculator, 2E-1);// Different precision in integration.
    final PresentValueCalculator pvcNoConvexity = PresentValueCalculator.getInstance();
    final double priceCMS_noConvexity = CMS_COUPON_RECEIVER.accept(pvcNoConvexity, curves);// Price without convexity adjustment.
    assertEquals(priceCMS_calculator, priceCMS_noConvexity, 400.0);
    assertEquals(priceCMS_calculator > priceCMS_noConvexity, true);
  }

  @Test
  public void testPriceChangeSABRFormula() {
    final YieldCurveBundle curves = TestsDataSetsSABR.createCurves1();
    // SABR Hagan volatility function
    final SABRInterestRateParameters sabrParameterHagan = TestsDataSetsSABR.createSABR1(new SABRHaganVolatilityFunction());
    final SABRInterestRateDataBundle sabrHaganBundle = new SABRInterestRateDataBundle(sabrParameterHagan, curves);
    final double priceHagan = CMS_COUPON_RECEIVER.accept(PVC, sabrHaganBundle);
    // From previous run
    assertEquals(8853.300, priceHagan, 1E-2);
    // No convexity adjustment
    final PresentValueCalculator pvcNoConvexity = PresentValueCalculator.getInstance();
    final double priceNoConvexity = CMS_COUPON_RECEIVER.accept(pvcNoConvexity, curves);
    assertEquals(priceHagan, priceNoConvexity, 400.0);
    // SABR Hagan alternative volatility function
    final SABRInterestRateParameters sabrParameterHaganAlt = TestsDataSetsSABR.createSABR1(new SABRHaganAlternativeVolatilityFunction());
    final SABRInterestRateDataBundle sabrHaganAltBundle = new SABRInterestRateDataBundle(sabrParameterHaganAlt, curves);
    final double priceHaganAlt = CMS_COUPON_RECEIVER.accept(PVC, sabrHaganAltBundle);
    assertEquals(priceHagan, priceHaganAlt, 40.0);
    // SABR Berestycki volatility function
    final SABRInterestRateParameters sabrParameterBerestycki = TestsDataSetsSABR.createSABR1(new SABRBerestyckiVolatilityFunction());
    final SABRInterestRateDataBundle sabrBerestyckiBundle = new SABRInterestRateDataBundle(sabrParameterBerestycki, curves);
    final double priceBerestycki = CMS_COUPON_RECEIVER.accept(PVC, sabrBerestyckiBundle);
    assertEquals(priceHagan, priceBerestycki, 5);
    final SABRInterestRateParameters sabrParameterPaulot = TestsDataSetsSABR.createSABR1(new SABRPaulotVolatilityFunction());
    final SABRInterestRateDataBundle sabrPaulotBundle = new SABRInterestRateDataBundle(sabrParameterPaulot, curves);
    final double pricePaulot = CMS_COUPON_RECEIVER.accept(PVC, sabrPaulotBundle);
    assertEquals(priceHagan, pricePaulot, 15);
  }

  private static class CMSIntegrant extends Function1D<Double, Double> {

    private final static double EPS = 1E-10;
    private final int _nbFixedPeriod;
    private final int _nbFixedPaymentYear;
    private final double _tau;
    private final double _delta;
    private final double _eta;
    private final double _timeToExpiry;
    private final double _maturity;
    private final double _strike = 0.0;
    private final double _forward;
    private final SABRInterestRateParameters _sabrParameter;
    private final BlackPriceFunction _blackFunction = new BlackPriceFunction();

    /**
     *
     */
    public CMSIntegrant(final CouponCMS cmsCoupon, final SABRInterestRateParameters sabrParameter, final double forward) {
      _nbFixedPeriod = cmsCoupon.getUnderlyingSwap().getFixedLeg().getPayments().length;
      _nbFixedPaymentYear = (int) Math.round(1.0 / cmsCoupon.getUnderlyingSwap().getFixedLeg().getNthPayment(0).getPaymentYearFraction());
      _tau = 1.0 / _nbFixedPaymentYear;
      _delta = cmsCoupon.getPaymentTime() - cmsCoupon.getSettlementTime();
      _eta = -_delta;
      _sabrParameter = sabrParameter;
      _timeToExpiry = cmsCoupon.getFixingTime();
      final AnnuityCouponFixed annuityFixed = cmsCoupon.getUnderlyingSwap().getFixedLeg();
      _maturity = annuityFixed.getNthPayment(annuityFixed.getNumberOfPayments() - 1).getPaymentTime() - cmsCoupon.getSettlementTime();
      _forward = forward;
    }

    @Override
    public Double evaluate(final Double x) {//evaluate(Double x)
      final double[] kD = kpkpp(x);
      // Implementation note: kD[0] contains the first derivative of k; kD[1] the second derivative of k.
      return (kD[1] * (x - _strike) + 2.0 * kD[0]) * bs(x, _forward);
    }

    public double h(final double x) {
      return Math.pow(1.0 + _tau * x, _eta);
    }

    public double g(final double x) {
      if (x >= EPS) {
        final double periodFactor = 1 + x / _nbFixedPaymentYear;
        final double nPeriodDiscount = Math.pow(periodFactor, -_nbFixedPeriod);
        return 1.0 / x * (1.0 - nPeriodDiscount);
      }
      return ((double) _nbFixedPeriod) / _nbFixedPaymentYear;
    }

    public double k(final double x) {
      double G;
      double h;
      if (x >= EPS) {
        final double periodFactor = 1 + x / _nbFixedPaymentYear;
        final double nPeriodDiscount = Math.pow(periodFactor, -_nbFixedPeriod);
        G = 1.0 / x * (1.0 - nPeriodDiscount);
        h = Math.pow(1.0 + _tau * x, _eta);
      } else {
        G = ((double) _nbFixedPeriod) / _nbFixedPaymentYear;
        h = 1.0;
      }
      return h / G;
    }

    public double[] kpkpp(final double x) {
      final double periodFactor = 1 + x / _nbFixedPaymentYear;
      final double nPeriodDiscount = Math.pow(periodFactor, -_nbFixedPeriod);
      double G, Gp, Gpp;
      if (x >= EPS) {
        G = 1.0 / x * (1.0 - nPeriodDiscount);
        Gp = -G / x + _nbFixedPeriod / x / _nbFixedPaymentYear * nPeriodDiscount / periodFactor;
        Gpp = 2.0 / (x * x) * G - 2.0 * _nbFixedPeriod / (x * x) / _nbFixedPaymentYear * nPeriodDiscount / periodFactor - (_nbFixedPeriod + 1.0) * _nbFixedPeriod / x
            / (_nbFixedPaymentYear * _nbFixedPaymentYear) * nPeriodDiscount / (periodFactor * periodFactor);
      } else {
        // Implementation comment: When x is (almost) 0, useful for CMS swaps which are priced as CMS cap of strike 0.
        G = ((double) _nbFixedPeriod) / _nbFixedPaymentYear;
        Gp = -_nbFixedPeriod / 2.0 * (_nbFixedPeriod + 1.0) / (_nbFixedPaymentYear * _nbFixedPaymentYear);
        Gpp = _nbFixedPeriod / 2.0 * (_nbFixedPeriod + 1.0) * (1.0 + (_nbFixedPeriod + 2.0) / 3.0) / (_nbFixedPaymentYear * _nbFixedPaymentYear * _nbFixedPaymentYear);
      }
      final double h = Math.pow(1.0 + _tau * x, _eta);
      final double hp = _eta * _tau * h / periodFactor;
      final double hpp = (_eta - 1.0) * _tau * hp / periodFactor;
      final double kp = hp / G - h * Gp / (G * G);
      final double kpp = hpp / G - 2 * hp * Gp / (G * G) - h * (Gpp / (G * G) - 2 * (Gp * Gp) / (G * G * G));
      return new double[] {kp, kpp };
    }

    public double bs(final double strike, final double forward) {
      final EuropeanVanillaOption option = new EuropeanVanillaOption(strike, _timeToExpiry, true);
      final double volatility = _sabrParameter.getVolatility(_timeToExpiry, _maturity, strike, forward);
      final BlackFunctionData dataBlack = new BlackFunctionData(forward, 1.0, volatility);
      final Function1D<BlackFunctionData, Double> func = _blackFunction.getPriceFunction(option);
      return func.evaluate(dataBlack);
    }
  }
}
