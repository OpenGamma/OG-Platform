/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.payments;

import static org.testng.AssertJUnit.assertEquals;

import javax.time.calendar.LocalDateTime;
import javax.time.calendar.Period;
import javax.time.calendar.TimeZone;
import javax.time.calendar.ZonedDateTime;

import org.testng.annotations.Test;

import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.instrument.annuity.AnnuityCouponFixedDefinition;
import com.opengamma.financial.instrument.annuity.AnnuityCouponIborDefinition;
import com.opengamma.financial.instrument.index.IborIndex;
import com.opengamma.financial.instrument.index.IndexSwap;
import com.opengamma.financial.instrument.payment.CouponCMSDefinition;
import com.opengamma.financial.instrument.swap.SwapFixedIborDefinition;
import com.opengamma.financial.interestrate.ParRateCalculator;
import com.opengamma.financial.interestrate.PresentValueCalculator;
import com.opengamma.financial.interestrate.PresentValueSABRCalculator;
import com.opengamma.financial.interestrate.TestsDataSets;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.interestrate.annuity.definition.AnnuityCouponFixed;
import com.opengamma.financial.interestrate.payments.method.CouponCMSSABRReplicationMethod;
import com.opengamma.financial.interestrate.swap.definition.FixedCouponSwap;
import com.opengamma.financial.model.option.definition.SABRInterestRateDataBundle;
import com.opengamma.financial.model.option.definition.SABRInterestRateParameters;
import com.opengamma.financial.model.option.pricing.analytic.formula.BlackFunctionData;
import com.opengamma.financial.model.option.pricing.analytic.formula.BlackPriceFunction;
import com.opengamma.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.financial.model.volatility.smile.function.SABRBerestyckiVolatilityFunction;
import com.opengamma.financial.model.volatility.smile.function.SABRHaganAlternativeVolatilityFunction;
import com.opengamma.financial.model.volatility.smile.function.SABRHaganVolatilityFunction;
import com.opengamma.financial.model.volatility.smile.function.SABRPaulotVolatilityFunction;
import com.opengamma.financial.schedule.ScheduleCalculator;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.integration.RungeKuttaIntegrator1D;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtils;

/**
 * Tests of the CMS coupons.
 */
public class CouponCMSTest {
  //Swap 5Y
  private static final Currency CUR = Currency.USD;
  private static final Calendar CALENDAR = new MondayToFridayCalendar("A");
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Modified Following");
  private static final boolean IS_EOM = true;
  private static final Period ANNUITY_TENOR = Period.ofYears(5);
  private static final ZonedDateTime SETTLEMENT_DATE = DateUtils.getUTCDate(2014, 3, 17);
  //Fixed leg: Semi-annual bond
  private static final Period FIXED_PAYMENT_PERIOD = Period.ofMonths(6);
  private static final DayCount FIXED_DAY_COUNT = DayCountFactory.INSTANCE.getDayCount("30/360");
  private static final double RATE = 0.0325;
  private static final boolean FIXED_IS_PAYER = true;
  private static final AnnuityCouponFixedDefinition FIXED_ANNUITY = AnnuityCouponFixedDefinition.from(CUR, SETTLEMENT_DATE, ANNUITY_TENOR, FIXED_PAYMENT_PERIOD, CALENDAR, FIXED_DAY_COUNT,
      BUSINESS_DAY, IS_EOM, 1.0, RATE, FIXED_IS_PAYER);
  //Ibor leg: quarterly money
  private static final Period INDEX_TENOR = Period.ofMonths(3);
  private static final int SETTLEMENT_DAYS = 2;
  private static final DayCount DAY_COUNT = DayCountFactory.INSTANCE.getDayCount("Actual/360");
  private static final IborIndex IBOR_INDEX = new IborIndex(CUR, INDEX_TENOR, SETTLEMENT_DAYS, CALENDAR, DAY_COUNT, BUSINESS_DAY, IS_EOM);
  private static final AnnuityCouponIborDefinition IBOR_ANNUITY = AnnuityCouponIborDefinition.from(SETTLEMENT_DATE, ANNUITY_TENOR, 1.0, IBOR_INDEX, !FIXED_IS_PAYER);
  // CMS coupon construction
  private static final IndexSwap CMS_INDEX = new IndexSwap(FIXED_PAYMENT_PERIOD, FIXED_DAY_COUNT, IBOR_INDEX, ANNUITY_TENOR);
  private static final SwapFixedIborDefinition SWAP_DEFINITION = new SwapFixedIborDefinition(FIXED_ANNUITY, IBOR_ANNUITY);
  private static final ZonedDateTime PAYMENT_DATE = DateUtils.getUTCDate(2014, 6, 17); // Prefixed
  private static final ZonedDateTime FIXING_DATE = ScheduleCalculator.getAdjustedDate(SETTLEMENT_DATE, -SETTLEMENT_DAYS, CALENDAR);
  private static final ZonedDateTime ACCRUAL_START_DATE = SETTLEMENT_DATE;
  private static final ZonedDateTime ACCRUAL_END_DATE = PAYMENT_DATE;
  private static final DayCount PAYMENT_DAY_COUNT = DayCountFactory.INSTANCE.getDayCount("Actual/360");
  private static final double ACCRUAL_FACTOR = PAYMENT_DAY_COUNT.getDayCountFraction(ACCRUAL_START_DATE, ACCRUAL_END_DATE);
  private static final double NOTIONAL = 1000000; //1m
  private static final CouponCMSDefinition CMS_COUPON_RECEIVER_DEFINITION = CouponCMSDefinition.from(PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR, NOTIONAL, FIXING_DATE,
      SWAP_DEFINITION, CMS_INDEX);
  // to derivatives
  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2010, 8, 18);
  private static final String FUNDING_CURVE_NAME = "Funding";
  private static final String FORWARD_CURVE_NAME = "Forward";
  private static final String[] CURVES_NAME = {FUNDING_CURVE_NAME, FORWARD_CURVE_NAME};
  private static final FixedCouponSwap<Coupon> SWAP = SWAP_DEFINITION.toDerivative(REFERENCE_DATE, CURVES_NAME);

  private static final CouponCMS CMS_COUPON_RECEIVER = (CouponCMS) CMS_COUPON_RECEIVER_DEFINITION.toDerivative(REFERENCE_DATE, CURVES_NAME);

  // Calculators
  private static final ParRateCalculator PRC = ParRateCalculator.getInstance();
  private static final PresentValueSABRCalculator PVC = PresentValueSABRCalculator.getInstance();

  @Test
  public void testGetter() {
    final DayCount actAct = DayCountFactory.INSTANCE.getDayCount("Actual/Actual ISDA");
    final ZonedDateTime zonedDate = ZonedDateTime.of(LocalDateTime.ofMidnight(REFERENCE_DATE), TimeZone.UTC);
    final double fixingTime = actAct.getDayCountFraction(zonedDate, FIXING_DATE);
    assertEquals(fixingTime, CMS_COUPON_RECEIVER.getFixingTime(), 1E-10);
    assertEquals(SWAP, CMS_COUPON_RECEIVER.getUnderlyingSwap());
    assertEquals(NOTIONAL, CMS_COUPON_RECEIVER.getNotional(), 1E-10);
  }

  @Test
  public void testPriceReplication() {
    final YieldCurveBundle curves = TestsDataSets.createCurves1();
    final SABRInterestRateParameters sabrParameter = TestsDataSets.createSABR1();
    final SABRInterestRateDataBundle sabrBundle = new SABRInterestRateDataBundle(sabrParameter, curves);
    final double forward = PRC.visit(SWAP, curves);
    final double discountFactor = curves.getCurve(FUNDING_CURVE_NAME).getDiscountFactor(CMS_COUPON_RECEIVER.getPaymentTime());
    final CMSIntegrant integrant = new CMSIntegrant(CMS_COUPON_RECEIVER, sabrParameter, forward);
    final double factor = discountFactor / integrant.h(forward) * integrant.G(forward);
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
      throw new RuntimeException(e);
    }
    final double priceCMS = factor * (strikePart + integralPart) * CMS_COUPON_RECEIVER.getNotional() * CMS_COUPON_RECEIVER.getPaymentYearFraction();
    assertEquals(8854.551, priceCMS, 1E-2);
    // Price not verified yet: from previous run.
    final CouponCMSSABRReplicationMethod replication = new CouponCMSSABRReplicationMethod(integrationInterval);
    final double priceCMS_method = replication.presentValue(CMS_COUPON_RECEIVER, sabrBundle);
    assertEquals(priceCMS, priceCMS_method, 1.5); // Different precision in integration.
    final double priceCMS_calculator = PVC.visit(CMS_COUPON_RECEIVER, sabrBundle);
    assertEquals(priceCMS_method, priceCMS_calculator, 2E-1);// Different precision in integration.
    final PresentValueCalculator pvcNoConvexity = PresentValueCalculator.getInstance();
    final double priceCMS_noConvexity = pvcNoConvexity.visit(CMS_COUPON_RECEIVER, curves);// Price without convexity adjustment.
    assertEquals(priceCMS_calculator, priceCMS_noConvexity, 400.0);
    assertEquals(priceCMS_calculator > priceCMS_noConvexity, true);
  }

  @Test
  public void testPriceChangeSABRFormula() {
    final YieldCurveBundle curves = TestsDataSets.createCurves1();
    // SABR Hagan volatility function
    final SABRInterestRateParameters sabrParameterHagan = TestsDataSets.createSABR1(new SABRHaganVolatilityFunction());
    final SABRInterestRateDataBundle sabrHaganBundle = new SABRInterestRateDataBundle(sabrParameterHagan, curves);
    final double priceHagan = PVC.visit(CMS_COUPON_RECEIVER, sabrHaganBundle);
    // From previous run
    assertEquals(8853.300, priceHagan, 1E-2);
    // No convexity adjustment
    final PresentValueCalculator pvcNoConvexity = PresentValueCalculator.getInstance();
    final double priceNoConvexity = pvcNoConvexity.visit(CMS_COUPON_RECEIVER, curves);
    assertEquals(priceHagan, priceNoConvexity, 400.0);
    // SABR Hagan alternative volatility function
    final SABRInterestRateParameters sabrParameterHaganAlt = TestsDataSets.createSABR1(new SABRHaganAlternativeVolatilityFunction());
    final SABRInterestRateDataBundle sabrHaganAltBundle = new SABRInterestRateDataBundle(sabrParameterHaganAlt, curves);
    final double priceHaganAlt = PVC.visit(CMS_COUPON_RECEIVER, sabrHaganAltBundle);
    assertEquals(priceHagan, priceHaganAlt, 40.0);
    // SABR Berestycki volatility function
    final SABRInterestRateParameters sabrParameterBerestycki = TestsDataSets.createSABR1(new SABRBerestyckiVolatilityFunction());
    final SABRInterestRateDataBundle sabrBerestyckiBundle = new SABRInterestRateDataBundle(sabrParameterBerestycki, curves);
    final double priceBerestycki = PVC.visit(CMS_COUPON_RECEIVER, sabrBerestyckiBundle);
    assertEquals(priceHagan, priceBerestycki, 5);
    // SABR Johnson volatility function
    //    SABRInterestRateParameter sabrParameterJohnson = TestsDataSets.createSABR1(new SABRJohnsonVolatilityFunction());
    //    SABRInterestRateDataBundle sabrJohnsonBundle = new SABRInterestRateDataBundle(sabrParameterJohnson, curves);
    //    double priceJohnson = PVC.visit(CMS_COUPON, sabrJohnsonBundle);
    //    assertEquals(priceHagan, priceJohnson, 1);
    // SABR Paulot volatility function ! Does not work well !
    final SABRInterestRateParameters sabrParameterPaulot = TestsDataSets.createSABR1(new SABRPaulotVolatilityFunction());
    final SABRInterestRateDataBundle sabrPaulotBundle = new SABRInterestRateDataBundle(sabrParameterPaulot, curves);
    final double pricePaulot = PVC.visit(CMS_COUPON_RECEIVER, sabrPaulotBundle);
    assertEquals(priceHagan, pricePaulot, 15);
  }

  private class CMSIntegrant extends Function1D<Double, Double> {

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

    public double G(final double x) {
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
      return new double[] {kp, kpp};
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
