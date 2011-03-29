/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.payments;

import static org.testng.AssertJUnit.assertEquals;

import javax.time.calendar.LocalDate;
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
import com.opengamma.financial.instrument.index.CMSIndex;
import com.opengamma.financial.instrument.index.IborIndex;
import com.opengamma.financial.instrument.payment.CouponCMSDefinition;
import com.opengamma.financial.instrument.swap.ZZZSwapFixedIborDefinition;
import com.opengamma.financial.interestrate.ParRateCalculator;
import com.opengamma.financial.interestrate.PresentValueCalculator;
import com.opengamma.financial.interestrate.TestsDataSets;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.interestrate.annuity.definition.AnnuityCouponFixed;
import com.opengamma.financial.interestrate.swap.definition.FixedCouponSwap;
import com.opengamma.financial.model.option.definition.SABRInterestRateDataBundle;
import com.opengamma.financial.model.option.definition.SABRInterestRateParameter;
import com.opengamma.financial.model.option.pricing.analytic.formula.BlackFunctionData;
import com.opengamma.financial.model.option.pricing.analytic.formula.BlackPriceFunction;
import com.opengamma.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.integration.RungeKuttaIntegrator1D;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtil;
import com.opengamma.util.tuple.DoublesPair;

public class CouponCMSTest {
  //Swap 2Y
  private static final Currency CUR = Currency.USD;
  private static final Calendar CALENDAR = new MondayToFridayCalendar("A");
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Modified Following");
  private static final boolean IS_EOM = true;
  private static final Period ANNUITY_TENOR = Period.ofYears(2);
  private static final ZonedDateTime SETTLEMENT_DATE = DateUtil.getUTCDate(2011, 3, 17);
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
  private static final CMSIndex CMS_INDEX = new CMSIndex(FIXED_PAYMENT_PERIOD, FIXED_DAY_COUNT, IBOR_INDEX, ANNUITY_TENOR);
  private static final ZZZSwapFixedIborDefinition SWAP_DEFINITION = new ZZZSwapFixedIborDefinition(FIXED_ANNUITY, IBOR_ANNUITY);
  private static final ZonedDateTime PAYMENT_DATE = DateUtil.getUTCDate(2011, 4, 6);
  private static final ZonedDateTime FIXING_DATE = DateUtil.getUTCDate(2010, 12, 30);
  private static final ZonedDateTime ACCRUAL_START_DATE = DateUtil.getUTCDate(2011, 1, 5);
  private static final ZonedDateTime ACCRUAL_END_DATE = DateUtil.getUTCDate(2011, 4, 5);
  private static final DayCount PAYMENT_DAY_COUNT = DayCountFactory.INSTANCE.getDayCount("Actual/360");
  private static final double ACCRUAL_FACTOR = PAYMENT_DAY_COUNT.getDayCountFraction(ACCRUAL_START_DATE, ACCRUAL_END_DATE);
  private static final double NOTIONAL = 1000000; //1m
  private static final CouponCMSDefinition CMS_COUPON_DEFINITION = CouponCMSDefinition.from(PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR, NOTIONAL, FIXING_DATE, SWAP_DEFINITION,
      CMS_INDEX);
  // to derivatives
  private static final LocalDate REFERENCE_DATE = LocalDate.of(2010, 8, 18);
  private static final String FUNDING_CURVE_NAME = "Funding";
  private static final String FORWARD_CURVE_NAME = "Forward";
  private static final String[] CURVES_NAME = {FUNDING_CURVE_NAME, FORWARD_CURVE_NAME};
  private static final FixedCouponSwap<Payment> SWAP = SWAP_DEFINITION.toDerivative(REFERENCE_DATE, CURVES_NAME);

  private static final CouponCMS CMS_COUPON = (CouponCMS) CMS_COUPON_DEFINITION.toDerivative(REFERENCE_DATE, CURVES_NAME);

  // Calculators
  private static final ParRateCalculator PRC = ParRateCalculator.getInstance();
  private static final PresentValueCalculator PVC = PresentValueCalculator.getInstance();

  @Test
  public void testGetter() {
    final DayCount actAct = DayCountFactory.INSTANCE.getDayCount("Actual/Actual ISDA");
    final ZonedDateTime zonedDate = ZonedDateTime.of(LocalDateTime.ofMidnight(REFERENCE_DATE), TimeZone.UTC);
    double fixingTime = actAct.getDayCountFraction(zonedDate, FIXING_DATE);
    assertEquals(fixingTime, CMS_COUPON.getFixingTime(), 1E-10);
    assertEquals(SWAP, CMS_COUPON.getUnderlyingSwap());
    assertEquals(NOTIONAL, CMS_COUPON.getNotional(), 1E-10);
  }

  @Test
  public void testPriceReplication() {
    YieldCurveBundle curves = TestsDataSets.createCurves1();
    SABRInterestRateParameter sabrParameter = TestsDataSets.createSABR1();
    SABRInterestRateDataBundle sabrBundle = new SABRInterestRateDataBundle(sabrParameter, curves);
    double forward = PRC.visit(SWAP, curves);
    double discountFactor = curves.getCurve(FUNDING_CURVE_NAME).getDiscountFactor(CMS_COUPON.getPaymentTime());
    CMSIntegrant integrant = new CMSIntegrant(CMS_COUPON, sabrParameter, forward);
    double factor = discountFactor / integrant.h(forward) * integrant.G(forward);
    double strike = 0; //CMS swap is equivalent to CMS cap with strike 0 (when rates are always positive).
    double strikePart = integrant.k(strike) * integrant.bs(strike, forward);
    double absoluteTolerance = 1E+2;
    double relativeTolerance = 1.0;
    int nbIteration = 15;
    RungeKuttaIntegrator1D integratorOG = new RungeKuttaIntegrator1D(absoluteTolerance, relativeTolerance, nbIteration);
    double integrationInterval = 0.50;
    double integralPart;
    try {
      integralPart = integratorOG.integrate(integrant, strike, strike + integrationInterval);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    double priceCMS = factor * (strikePart + integralPart) * CMS_COUPON.getNotional() * CMS_COUPON.getPaymentYearFraction();
    assertEquals(10183.770, priceCMS, 1E-2);
    // Price not verified yet: from previous run.

    CouponCMSReplicationSABRMethod replication = new CouponCMSReplicationSABRMethod(integrationInterval);
    double priceCMS_method = replication.price(CMS_COUPON, sabrBundle);
    assertEquals(priceCMS, priceCMS_method, 2E-1); // Different precision in integration.
    double priceCMS_calculator = PVC.visit(CMS_COUPON, sabrBundle);
    assertEquals(priceCMS_method, priceCMS_calculator, 2E-1);// Different precision in integration.
    double priceCMS_noConvexity = PVC.visit(CMS_COUPON, curves);
    assertEquals(priceCMS_calculator, priceCMS_noConvexity, 20.0);// Price without convexity adjustment.

    //    // Performance analysis.
    //    long startTime, endTime;
    //    int nbTest = 10;
    //    startTime = System.currentTimeMillis();
    //    for (int looptest = 0; looptest < nbTest; looptest++) {
    //      priceCMS_calculator = replication.price(CMS_COUPON, sabrBundle);
    //    }
    //    endTime = System.currentTimeMillis();
    //    System.out.println(nbTest + " CMS swap by replication (" + integrationInterval + "): " + (endTime - startTime) + " ms / price: " + priceCMS_calculator);
    //    integrationInterval = 1.00;
    //    replication.setIntegrationInterval(integrationInterval);
    //    startTime = System.currentTimeMillis();
    //    for (int looptest = 0; looptest < nbTest; looptest++) {
    //      priceCMS_calculator = replication.price(CMS_COUPON, sabrBundle);
    //    }
    //    endTime = System.currentTimeMillis();
    //    System.out.println(nbTest + " CMS swap by replication (" + integrationInterval + "): " + (endTime - startTime) + " ms / price: " + priceCMS_calculator);
    //    integrationInterval = 0.50;
    //    replication.setIntegrationInterval(integrationInterval);
    //    startTime = System.currentTimeMillis();
    //    for (int looptest = 0; looptest < nbTest; looptest++) {
    //      priceCMS_calculator = replication.price(CMS_COUPON, sabrBundle);
    //    }
    //    endTime = System.currentTimeMillis();
    //    System.out.println(nbTest + " CMS swap by replication (" + integrationInterval + "): " + (endTime - startTime) + " ms / price: " + priceCMS_calculator);
    //    integrationInterval = 1.00;
    //    replication.setIntegrationInterval(integrationInterval);
    //    startTime = System.currentTimeMillis();
    //    for (int looptest = 0; looptest < nbTest; looptest++) {
    //      priceCMS_calculator = replication.price(CMS_COUPON, sabrBundle);
    //    }
    //    endTime = System.currentTimeMillis();
    //    System.out.println(nbTest + " CMS swap by replication (" + integrationInterval + "): " + (endTime - startTime) + " ms / price: " + priceCMS_calculator);
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
    private final SABRInterestRateParameter _sabrParameter;
    private final BlackPriceFunction _blackFunction = new BlackPriceFunction();

    /**
     * 
     */
    public CMSIntegrant(CouponCMS cmsCoupon, SABRInterestRateParameter sabrParameter, double forward) {
      _nbFixedPeriod = cmsCoupon.getUnderlyingSwap().getFixedLeg().getPayments().length;
      _nbFixedPaymentYear = (int) Math.round(1.0 / cmsCoupon.getUnderlyingSwap().getFixedLeg().getNthPayment(0).getPaymentYearFraction());
      _tau = 1.0 / _nbFixedPaymentYear;
      _delta = cmsCoupon.getPaymentTime() - cmsCoupon.getFixingTime();
      //FIXME: The delta definition should be improved.
      _eta = -_delta / _tau;
      _sabrParameter = sabrParameter;
      _timeToExpiry = cmsCoupon.getFixingTime();
      // FIXME: A better notion of maturity is required
      AnnuityCouponFixed annuityFixed = cmsCoupon.getUnderlyingSwap().getFixedLeg();
      double maturity = annuityFixed.getNthPayment(0).getPaymentYearFraction();
      if (annuityFixed.getNumberOfPayments() >= 2) {
        maturity += annuityFixed.getNthPayment(annuityFixed.getNumberOfPayments() - 1).getPaymentTime() - annuityFixed.getNthPayment(0).getPaymentTime();
      }
      _maturity = maturity;
      _forward = forward;
    }

    @Override
    public Double evaluate(Double x) {//evaluate(Double x)
      double[] kD = kpkpp(x);
      // Implementation note: kD[0] contains the first derivative of k; kD[1] the second derivative of k. 
      return (kD[1] * (x - _strike) + 2.0 * kD[0]) * bs(x, _forward);
    }

    private double h(double x) {
      return Math.pow(1.0 + _tau * x, _eta);
    }

    private double G(double x) {
      if (x >= EPS) {
        double periodFactor = 1 + x / _nbFixedPaymentYear;
        double nPeriodDiscount = Math.pow(periodFactor, -_nbFixedPeriod);
        return 1.0 / x * (1.0 - nPeriodDiscount);
      }
      return ((double) _nbFixedPeriod) / _nbFixedPaymentYear;
    }

    private double k(double x) {
      double G;
      double h;
      if (x >= EPS) {
        double periodFactor = 1 + x / _nbFixedPaymentYear;
        double nPeriodDiscount = Math.pow(periodFactor, -_nbFixedPeriod);
        G = 1.0 / x * (1.0 - nPeriodDiscount);
        h = Math.pow(1.0 + _tau * x, _eta);
      } else {
        G = ((double) _nbFixedPeriod) / _nbFixedPaymentYear;
        h = 1.0;
      }
      return h / G;
    }

    private double[] kpkpp(double x) {
      double periodFactor = 1 + x / _nbFixedPaymentYear;
      double nPeriodDiscount = Math.pow(periodFactor, -_nbFixedPeriod);
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
      double h = Math.pow(1.0 + _tau * x, _eta);
      double hp = _eta * _tau * h / periodFactor;
      double hpp = (_eta - 1.0) * _tau * hp / periodFactor;
      double kp = hp / G - h * Gp / (G * G);
      double kpp = hpp / G - 2 * hp * Gp / (G * G) - h * (Gpp / (G * G) - 2 * (Gp * Gp) / (G * G * G));
      return new double[] {kp, kpp};
    }

    double bs(double strike, double forward) {
      EuropeanVanillaOption option = new EuropeanVanillaOption(strike, _timeToExpiry, true);
      double volatility = _sabrParameter.getVolatility(new DoublesPair(_timeToExpiry, _maturity), strike, forward);
      BlackFunctionData dataBlack = new BlackFunctionData(forward, 1.0, volatility);
      Function1D<BlackFunctionData, Double> func = _blackFunction.getPriceFunction(option);
      return func.evaluate(dataBlack);
    }
  }
}
