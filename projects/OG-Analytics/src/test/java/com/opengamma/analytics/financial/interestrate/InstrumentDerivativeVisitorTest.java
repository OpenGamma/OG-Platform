/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate;

import javax.time.calendar.Period;

import com.opengamma.analytics.financial.instrument.future.FutureInstrumentsDescriptionDataSet;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.swaption.SwaptionInstrumentsDescriptionDataSet;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.Annuity;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.AnnuityCouponFixed;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.AnnuityCouponIbor;
import com.opengamma.analytics.financial.interestrate.cash.derivative.Cash;
import com.opengamma.analytics.financial.interestrate.fra.ForwardRateAgreement;
import com.opengamma.analytics.financial.interestrate.future.derivative.BondFuture;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFuture;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixed;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIbor;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborSpread;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.analytics.financial.interestrate.payments.derivative.PaymentFixed;
import com.opengamma.analytics.financial.interestrate.swap.derivative.FixedFloatSwap;
import com.opengamma.analytics.financial.interestrate.swap.derivative.Swap;
import com.opengamma.analytics.financial.interestrate.swap.derivative.SwapFixedCoupon;
import com.opengamma.analytics.financial.interestrate.swap.derivative.TenorSwap;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionCashFixedIbor;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionPhysicalFixedIbor;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.util.money.Currency;

/**
 * 
 */
public class InstrumentDerivativeVisitorTest {
  private static final String CURVE_NAME = "Test";
  private static final InstrumentDerivativeVisitorAdapter<Object, Object> ABSTRACT_VISITOR = new InstrumentDerivativeVisitorAdapter<Object, Object>() {
  };
  private static final Currency CUR = Currency.USD;
  private static final Cash CASH = new Cash(CUR, 0, 1, 1, 0, 1, CURVE_NAME);
  private static final IborIndex INDEX = new IborIndex(CUR, Period.ofMonths(3), 2, new MondayToFridayCalendar("A"), DayCountFactory.INSTANCE.getDayCount("30/360"),
      BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Following"), true);
  private static final ForwardRateAgreement FRA = new ForwardRateAgreement(CUR, 1, CURVE_NAME, 1, 100000, INDEX, 1, 1, 1.25, 0.25, 0.04, CURVE_NAME);
  private static final AnnuityCouponIbor FLOAT_LEG = new AnnuityCouponIbor(CUR, new double[] {1 }, INDEX, CURVE_NAME, CURVE_NAME, false);
  private static final AnnuityCouponIbor FLOAT_LEG_2 = new AnnuityCouponIbor(CUR, new double[] {1 }, INDEX, CURVE_NAME, CURVE_NAME, true);
  private static final AnnuityCouponFixed FIXED_LEG = new AnnuityCouponFixed(CUR, new double[] {1 }, 0.0, CURVE_NAME, true);
  private static final FixedFloatSwap SWAP = new FixedFloatSwap(FIXED_LEG, FLOAT_LEG);
  private static final TenorSwap<CouponIbor> TENOR_SWAP = new TenorSwap<CouponIbor>(FLOAT_LEG, FLOAT_LEG_2);
  private static final PaymentFixed FIXED_PAYMENT = new PaymentFixed(CUR, 1, 1, CURVE_NAME);
  private static final CouponIborSpread LIBOR_PAYMENT = new CouponIborSpread(CUR, 1.0, CURVE_NAME, 0, 1, 1, INDEX, 1, 1, 0, CURVE_NAME);
  private static final PaymentFixed FIXED_PAYMENT_2 = new PaymentFixed(CUR, 1, -1, CURVE_NAME);
  private static final CouponIborSpread LIBOR_PAYMENT_2 = new CouponIborSpread(CUR, 1.0, CURVE_NAME, 0, -1, 1, INDEX, 1, 1, 0, CURVE_NAME);
  //  private static final CouponFloating FLOATING_COUPON = new CouponFloating(CUR, 1, CURVE_NAME, 1, 1, 1);
  private static final Annuity<Payment> GA = new Annuity<Payment>(new Payment[] {FIXED_PAYMENT, LIBOR_PAYMENT });
  private static final Annuity<Payment> GA_2 = new Annuity<Payment>(new Payment[] {FIXED_PAYMENT_2, LIBOR_PAYMENT_2 });
  private static final SwapFixedCoupon<CouponIbor> FCS = new SwapFixedCoupon<CouponIbor>(FIXED_LEG, FLOAT_LEG);
  private static final AnnuityCouponFixed FCA = new AnnuityCouponFixed(CUR, new double[] {1 }, 0.05, CURVE_NAME, true);
  private static final AnnuityCouponIbor FLA = new AnnuityCouponIbor(CUR, new double[] {1 }, INDEX, 0.05, CURVE_NAME, CURVE_NAME, true);
  private static final CouponFixed FCP = new CouponFixed(CUR, 1, CURVE_NAME, 1, 0.04);
  private static final Swap<Payment, Payment> FIXED_FIXED = new Swap<Payment, Payment>(GA, GA_2);
  private static final SwaptionCashFixedIbor SWAPTION_CASH = SwaptionInstrumentsDescriptionDataSet.createSwaptionCashFixedIbor();
  private static final SwaptionPhysicalFixedIbor SWAPTION_PHYS = SwaptionInstrumentsDescriptionDataSet.createSwaptionPhysicalFixedIbor();
  private static final InterestRateFuture IR_FUT_SECURITY = FutureInstrumentsDescriptionDataSet.createInterestRateFutureSecurity();
  private static final BondFuture BNDFUT_SECURITY = FutureInstrumentsDescriptionDataSet.createBondFutureSecurity();

}
