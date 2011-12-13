/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate;

import javax.time.calendar.Period;

import org.apache.commons.lang.Validate;

import cern.jet.random.engine.MersenneTwister;
import cern.jet.random.engine.MersenneTwister64;
import cern.jet.random.engine.RandomEngine;

import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.convention.frequency.SimpleFrequency;
import com.opengamma.financial.instrument.index.IborIndex;
import com.opengamma.financial.instrument.index.IndexON;
import com.opengamma.financial.interestrate.annuity.definition.AnnuityCouponFixed;
import com.opengamma.financial.interestrate.annuity.definition.AnnuityCouponIbor;
import com.opengamma.financial.interestrate.annuity.definition.GenericAnnuity;
import com.opengamma.financial.interestrate.cash.definition.Cash;
import com.opengamma.financial.interestrate.fra.ForwardRateAgreement;
import com.opengamma.financial.interestrate.future.definition.InterestRateFuture;
import com.opengamma.financial.interestrate.payments.CouponFixed;
import com.opengamma.financial.interestrate.payments.PaymentFixed;
import com.opengamma.financial.interestrate.payments.derivative.CouponOIS;
import com.opengamma.financial.interestrate.swap.definition.CrossCurrencySwap;
import com.opengamma.financial.interestrate.swap.definition.FixedFloatSwap;
import com.opengamma.financial.interestrate.swap.definition.FloatingRateNote;
import com.opengamma.financial.interestrate.swap.definition.ForexForward;
import com.opengamma.financial.interestrate.swap.definition.OISSwap;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.CurrencyAmount;

/**
 * A set of methods to generate simply interest rate derivatives for testing purposes 
 */
public abstract class SimpleInstrumentFactory {

  private static final Currency CUR = Currency.USD;
  private static final Period TENOR = Period.ofMonths(6);
  private static final int SETTLEMENT_DAYS = 2;
  private static final Calendar CALENDAR = new MondayToFridayCalendar("A");
  private static final DayCount DAY_COUNT_INDEX = DayCountFactory.INSTANCE.getDayCount("Actual/360");
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Modified Following");
  private static final boolean IS_EOM = true;
  private static final IborIndex INDEX = new IborIndex(CUR, TENOR, SETTLEMENT_DAYS, CALENDAR, DAY_COUNT_INDEX, BUSINESS_DAY, IS_EOM);

  /** Random number generator */
  protected static final RandomEngine RANDOM = new MersenneTwister64(MersenneTwister.DEFAULT_SEED);
  /** Replaces rates */
  protected static final RateReplacingInterestRateDerivativeVisitor REPLACE_RATE = RateReplacingInterestRateDerivativeVisitor.getInstance();
  private static final Currency DUMMY_CUR = Currency.USD;
  private static final IborIndex DUMMY_INDEX = new IborIndex(DUMMY_CUR, Period.ofMonths(1), 2, new MondayToFridayCalendar("A"), DayCountFactory.INSTANCE.getDayCount("Actual/365"),
      BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Following"), true);
  private static final IndexON DUMMY_OIS_INDEX = new IndexON("OIS", DUMMY_CUR, DayCountFactory.INSTANCE.getDayCount("Actual/365"), 0, new MondayToFridayCalendar("A"));

  public static InstrumentDerivative makeCash(final double time, final String fundCurveName, final double rate, final double notional) {
    return new Cash(DUMMY_CUR, time, notional, rate, fundCurveName);
  }

  public static InstrumentDerivative makeLibor(final double time, final String indexCurveName, final double rate, final double notional) {
    return new Cash(DUMMY_CUR, time, notional, rate, indexCurveName);
  }

  /**
   * makes a very simple FRA with  payment time, fixing time and fixing period start being identical and an amount tau before fixing period end. The payment and fixing year fractions are
   * Identically equal to tau.   
   * @param time The fixing period end (the last relevant date for the FRA)
   * @param paymentFreq for a 3M FRA the payment freq is quarterly 
   * @param fundCurveName Name of funding curve
   * @param indexCurveName Name of index curve 
   * @param rate The FRA rate
   * @param notional the notional amount 
   * @return A FRA
   */
  public static InstrumentDerivative makeFRA(final double time, final SimpleFrequency paymentFreq, final String fundCurveName, final String indexCurveName, final double rate, final double notional) {
    double tau = 1. / paymentFreq.getPeriodsPerYear();
    return new ForwardRateAgreement(DUMMY_CUR, time - tau, fundCurveName, tau, notional, DUMMY_INDEX, time - tau, time - tau, time, tau, rate, indexCurveName);
  }

  public static InstrumentDerivative makeFuture(final double time, final SimpleFrequency paymentFreq, final String fundCurveName, final String indexCurveName) {
    double tau = 1. / paymentFreq.getPeriodsPerYear();
    return new InterestRateFuture(time, DUMMY_INDEX, time, time + tau, tau, 0, 1, tau, "N", fundCurveName, indexCurveName);
  }

  public static OISSwap makeOISSwap(final double time, final String fundingCurveName, final String indexCurveName, final double rate, final double notional) {

    if (time < 1.0) {
      return makeSinglePaymentOISSwap(time, fundingCurveName, indexCurveName, rate, notional);
    }

    SimpleFrequency paymentFreq = SimpleFrequency.ANNUAL;

    final int index = (int) (time * paymentFreq.getPeriodsPerYear());
    final double[] paymentTimes = new double[index];
    double tau = 1. / paymentFreq.getPeriodsPerYear();

    final CouponOIS[] oisCoupons = new CouponOIS[index];
    for (int i = 0; i < index; i++) {
      paymentTimes[i] = tau * (i + 1);
      oisCoupons[i] = new CouponOIS(DUMMY_CUR, paymentTimes[i], fundingCurveName, tau, notional, DUMMY_OIS_INDEX, paymentTimes[i] - tau, paymentTimes[i], tau, notional, indexCurveName);
    }

    final AnnuityCouponFixed fixedLeg = new AnnuityCouponFixed(DUMMY_CUR, paymentTimes, notional, rate, fundingCurveName, true);
    final GenericAnnuity<CouponOIS> payLeg = new GenericAnnuity<CouponOIS>(oisCoupons);

    return new OISSwap(fixedLeg, payLeg);
  }

  private static OISSwap makeSinglePaymentOISSwap(final double time, final String fundingCurveName, final String indexCurveName, final double rate, final double notional) {

    CouponOIS oisCoupon = new CouponOIS(DUMMY_CUR, time, fundingCurveName, time, notional, DUMMY_OIS_INDEX, 0, time, time, notional, indexCurveName);

    CouponFixed fixedCoupon = new CouponFixed(DUMMY_CUR, time, fundingCurveName, time, -notional, rate);

    AnnuityCouponFixed fixedLeg = new AnnuityCouponFixed(new CouponFixed[] {fixedCoupon});
    return new OISSwap(fixedLeg, new GenericAnnuity<CouponOIS>(new CouponOIS[] {oisCoupon}));
  }

  protected static FixedFloatSwap makeSwap(final double time, final SimpleFrequency floatLegFreq, final String fundingCurveName, final String liborCurveName, final double rate, 
      final double notional) {

    int floatPayments = (int) (time * floatLegFreq.getPeriodsPerYear());
    Validate.isTrue(floatPayments % 2 == 0, "need even number of float payments as fixed payments at half frequency");
    int fixedPayments = floatPayments / 2;

    double tauFloat = 1. / floatLegFreq.getPeriodsPerYear();
    double tauFixed = 2 * tauFloat;

    final double[] fixed = new double[fixedPayments];
    final double[] floating = new double[floatPayments];
    final double[] indexFixing = new double[floatPayments];
    final double[] indexMaturity = new double[floatPayments];
    final double[] yearFrac = new double[floatPayments];

    //turn on to randomised fixing/reset/payment dates 
    final double sigma = 0.0 / 365.0;

    for (int i = 0; i < fixedPayments; i++) {
      fixed[i] = tauFixed * (1 + i) + sigma * (RANDOM.nextDouble() - 0.5);
    }
    for (int i = 0; i < floatPayments; i++) {
      floating[i] = tauFloat * (1 + i) + sigma * (RANDOM.nextDouble() - 0.5);
      yearFrac[i] = tauFloat + sigma * (RANDOM.nextDouble() - 0.5);
      indexFixing[i] = tauFloat * i + sigma * (i == 0 ? RANDOM.nextDouble() / 2 : (RANDOM.nextDouble() - 0.5));
      indexMaturity[i] = tauFloat * (1 + i) + sigma * (RANDOM.nextDouble() - 0.5);
    }
    final AnnuityCouponFixed fixedLeg = new AnnuityCouponFixed(DUMMY_CUR, fixed, notional, rate, fundingCurveName, true);

    final AnnuityCouponIbor floatingLeg = new AnnuityCouponIbor(DUMMY_CUR, floating, indexFixing, INDEX, indexMaturity, yearFrac, notional, fundingCurveName, liborCurveName, false);
    return new FixedFloatSwap(fixedLeg, floatingLeg);
  }

  public static FixedFloatSwap makeSwap(final double time, final SimpleFrequency floatLegFreq, final SimpleFrequency fixedLegFreq, final String fundingCurveName, final String liborCurveName,
      final double rate, final double notional) {

    int floatPayments = (int) (time * floatLegFreq.getPeriodsPerYear());
    int fixedPayments = (int) (time * fixedLegFreq.getPeriodsPerYear());

    double tauFloat = 1. / floatLegFreq.getPeriodsPerYear();
    double tauFixed = 1. / fixedLegFreq.getPeriodsPerYear();

    Validate.isTrue(tauFloat * floatPayments == time, "float payments will not finish on time");
    Validate.isTrue(tauFixed * fixedPayments == time, "fixed payments will not finish on time");

    final double[] fixed = new double[fixedPayments];
    final double[] floating = new double[floatPayments];
    final double[] indexFixing = new double[floatPayments];
    final double[] indexMaturity = new double[floatPayments];
    final double[] yearFrac = new double[floatPayments];

    //turn on to randomised fixing/reset/payment dates 
    final double sigma = 0.0 / 365.0;

    for (int i = 0; i < fixedPayments; i++) {
      fixed[i] = tauFixed * (1 + i) + sigma * (RANDOM.nextDouble() - 0.5);
    }
    for (int i = 0; i < floatPayments; i++) {
      floating[i] = tauFloat * (1 + i) + sigma * (RANDOM.nextDouble() - 0.5);
      yearFrac[i] = tauFloat + sigma * (RANDOM.nextDouble() - 0.5);
      indexFixing[i] = tauFloat * i + sigma * (i == 0 ? RANDOM.nextDouble() / 2 : (RANDOM.nextDouble() - 0.5));
      indexMaturity[i] = tauFloat * (1 + i) + sigma * (RANDOM.nextDouble() - 0.5);
    }
    final AnnuityCouponFixed fixedLeg = new AnnuityCouponFixed(DUMMY_CUR, fixed, notional, rate, fundingCurveName, true);

    final AnnuityCouponIbor floatingLeg = new AnnuityCouponIbor(DUMMY_CUR, floating, indexFixing, INDEX, indexMaturity, yearFrac, notional, fundingCurveName, liborCurveName, false);
    return new FixedFloatSwap(fixedLeg, floatingLeg);
  }

  /**
   * Sets up a simple Floating rate note to test the analytics. 
   * @param notional An amount in a currency
   * @param nYears time to maturity in years
   * @param freq Frequency of payments 
   * @param discountCurve Name of discount curve
   * @param indexCurve Name of index curve
   * @param spread the spread paid
   * @return A FRN
   */
  public static FloatingRateNote makeFRN(final CurrencyAmount notional, final int nYears, SimpleFrequency freq, final String discountCurve, final String indexCurve, final double spread) {

    int payments = (int) (nYears * freq.getPeriodsPerYear());
    final double[] floatingPayments = new double[payments];
    final double[] indexFixing = new double[payments];
    final double[] indexMaturity = new double[payments];
    final double[] yearFrac = new double[payments];

    for (int i = 0; i < payments; i++) {
      indexFixing[i] = i / freq.getPeriodsPerYear();
      indexMaturity[i] = (i + 1) / freq.getPeriodsPerYear();
      floatingPayments[i] = indexMaturity[i];
      yearFrac[i] = 1 / freq.getPeriodsPerYear();
    }
    final AnnuityCouponIbor floatingLeg = new AnnuityCouponIbor(notional.getCurrency(), floatingPayments, indexFixing, INDEX, indexMaturity, yearFrac, notional.getAmount(), discountCurve, indexCurve,
        notional.getAmount() < 0.0).withSpread(spread);

    PaymentFixed initialPayment = new PaymentFixed(notional.getCurrency(), 0.0, -notional.getAmount(), discountCurve);
    PaymentFixed finalPayment = new PaymentFixed(notional.getCurrency(), nYears, notional.getAmount(), discountCurve);

    return new FloatingRateNote(floatingLeg, initialPayment, finalPayment);
  }

  /**
   * Makes a simple Cross Currency Swap for testing. 
   * Domestic and foreign amounts are exchanged at outset (t=0) with the implicit assumption that they net to zero (i.e. the spot FX rate
   * is domesticNotional/foreignNotional)
   * @param domesticNotional The notional amount in domestic currency
   * @param foreignNotional The notional amount in foreign currency
   * @param swapLength The length (in years) of the swap
   * @param domesticPaymentFreq Frequency (per year) of floating payments in domestic currency
   * @param foreignPaymentFreq Frequency (per year) of floating payments in foreign currency
   * @param domesticDiscountCurve The curve that all payments in domestic currency are discounted from 
   * @param domesticIndexCurve The curve that all domestic floating payments are calculated from 
   * @param foreignDiscountCurve The curve that all payments in foreign currency are discounted from 
   * @param foreignIndexCurve The curve that all foreign floating payments are calculated from 
   * @param spread The spread added to <b>foreign</b> floating payments
   * @return a CrossCurrencySwap
   */
  protected static CrossCurrencySwap makeCrossCurrencySwap(final CurrencyAmount domesticNotional, final CurrencyAmount foreignNotional, final int swapLength, SimpleFrequency domesticPaymentFreq,
      SimpleFrequency foreignPaymentFreq, final String domesticDiscountCurve, final String domesticIndexCurve, final String foreignDiscountCurve, final String foreignIndexCurve, final double spread) {

    FloatingRateNote domesticFRN = makeFRN(domesticNotional, swapLength, domesticPaymentFreq, domesticDiscountCurve, domesticIndexCurve, 0.0);
    FloatingRateNote foreignFRN = makeFRN(foreignNotional, swapLength, foreignPaymentFreq, foreignDiscountCurve, foreignIndexCurve, spread);

    double spotFX = domesticNotional.getAmount() / foreignNotional.getAmount(); //assume the initial exchange of notionals cancels 
    return new CrossCurrencySwap(domesticFRN, foreignFRN, spotFX);
  }

  public static ForexForward makeForexForward(final CurrencyAmount domesticNotional, final CurrencyAmount foreignNotional, final double paymentTime, final double spotFX,
      final String domesticDiscountCurve, final String foreignDiscountCurve) {
    PaymentFixed p1 = new PaymentFixed(domesticNotional.getCurrency(), paymentTime, domesticNotional.getAmount(), domesticDiscountCurve);
    PaymentFixed p2 = new PaymentFixed(foreignNotional.getCurrency(), paymentTime, foreignNotional.getAmount(), foreignDiscountCurve);
    return new ForexForward(p1, p2, spotFX);
  }

  //  public static Bond makeBond(final double maturity, final String curveName, final double coupon) {
  //
  //    final int n = (int) Math.ceil(maturity * 2.0);
  //    final double[] paymentTimes = new double[n];
  //    paymentTimes[n - 1] = maturity;
  //    for (int i = n - 2; i >= 0; i--) {
  //      paymentTimes[i] = paymentTimes[i + 1] - 0.5;
  //    }
  //    final double accuredInterest = coupon * (0.5 - paymentTimes[0]);
  //
  //    return new Bond(DUMMY_CUR, paymentTimes, coupon, 0.5, accuredInterest, curveName);
  //  }

}
