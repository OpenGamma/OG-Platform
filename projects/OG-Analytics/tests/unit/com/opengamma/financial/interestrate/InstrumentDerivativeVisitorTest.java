/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate;

import static org.testng.AssertJUnit.assertEquals;

import javax.time.calendar.Period;

import org.testng.annotations.Test;

import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.forex.derivative.Forex;
import com.opengamma.financial.forex.derivative.ForexNonDeliverableForward;
import com.opengamma.financial.forex.derivative.ForexNonDeliverableOption;
import com.opengamma.financial.forex.derivative.ForexOptionDigital;
import com.opengamma.financial.forex.derivative.ForexOptionSingleBarrier;
import com.opengamma.financial.forex.derivative.ForexOptionVanilla;
import com.opengamma.financial.forex.derivative.ForexSwap;
import com.opengamma.financial.instrument.future.FutureInstrumentsDescriptionDataSet;
import com.opengamma.financial.instrument.index.IborIndex;
import com.opengamma.financial.instrument.swaption.SwaptionInstrumentsDescriptionDataSet;
import com.opengamma.financial.interestrate.annuity.definition.AnnuityCouponFixed;
import com.opengamma.financial.interestrate.annuity.definition.AnnuityCouponIbor;
import com.opengamma.financial.interestrate.annuity.definition.AnnuityCouponIborRatchet;
import com.opengamma.financial.interestrate.annuity.definition.GenericAnnuity;
import com.opengamma.financial.interestrate.bond.definition.BondCapitalIndexedSecurity;
import com.opengamma.financial.interestrate.bond.definition.BondCapitalIndexedTransaction;
import com.opengamma.financial.interestrate.bond.definition.BondFixedSecurity;
import com.opengamma.financial.interestrate.bond.definition.BondFixedTransaction;
import com.opengamma.financial.interestrate.bond.definition.BondIborSecurity;
import com.opengamma.financial.interestrate.bond.definition.BondIborTransaction;
import com.opengamma.financial.interestrate.cash.derivative.Cash;
import com.opengamma.financial.interestrate.cash.derivative.DepositCounterpart;
import com.opengamma.financial.interestrate.cash.derivative.DepositIbor;
import com.opengamma.financial.interestrate.fra.ForwardRateAgreement;
import com.opengamma.financial.interestrate.future.definition.BondFuture;
import com.opengamma.financial.interestrate.future.definition.InterestRateFuture;
import com.opengamma.financial.interestrate.future.definition.InterestRateFutureOptionMarginSecurity;
import com.opengamma.financial.interestrate.future.definition.InterestRateFutureOptionMarginTransaction;
import com.opengamma.financial.interestrate.future.definition.InterestRateFutureOptionPremiumSecurity;
import com.opengamma.financial.interestrate.future.definition.InterestRateFutureOptionPremiumTransaction;
import com.opengamma.financial.interestrate.inflation.derivatives.CouponInflationZeroCouponInterpolation;
import com.opengamma.financial.interestrate.inflation.derivatives.CouponInflationZeroCouponInterpolationGearing;
import com.opengamma.financial.interestrate.inflation.derivatives.CouponInflationZeroCouponMonthly;
import com.opengamma.financial.interestrate.inflation.derivatives.CouponInflationZeroCouponMonthlyGearing;
import com.opengamma.financial.interestrate.payments.CapFloorCMS;
import com.opengamma.financial.interestrate.payments.CapFloorCMSSpread;
import com.opengamma.financial.interestrate.payments.CapFloorIbor;
import com.opengamma.financial.interestrate.payments.CouponCMS;
import com.opengamma.financial.interestrate.payments.CouponFixed;
import com.opengamma.financial.interestrate.payments.CouponIbor;
import com.opengamma.financial.interestrate.payments.CouponIborFixed;
import com.opengamma.financial.interestrate.payments.CouponIborGearing;
import com.opengamma.financial.interestrate.payments.Payment;
import com.opengamma.financial.interestrate.payments.PaymentFixed;
import com.opengamma.financial.interestrate.payments.derivative.CouponOIS;
import com.opengamma.financial.interestrate.swap.definition.CrossCurrencySwap;
import com.opengamma.financial.interestrate.swap.definition.FixedCouponSwap;
import com.opengamma.financial.interestrate.swap.definition.FixedFloatSwap;
import com.opengamma.financial.interestrate.swap.definition.FloatingRateNote;
import com.opengamma.financial.interestrate.swap.definition.ForexForward;
import com.opengamma.financial.interestrate.swap.definition.OISSwap;
import com.opengamma.financial.interestrate.swap.definition.Swap;
import com.opengamma.financial.interestrate.swap.definition.TenorSwap;
import com.opengamma.financial.interestrate.swaption.derivative.SwaptionBermudaFixedIbor;
import com.opengamma.financial.interestrate.swaption.derivative.SwaptionCashFixedIbor;
import com.opengamma.financial.interestrate.swaption.derivative.SwaptionPhysicalFixedIbor;
import com.opengamma.util.money.Currency;

/**
 * 
 */
public class InstrumentDerivativeVisitorTest {
  private static final String CURVE_NAME = "Test";
  private static final AbstractInstrumentDerivativeVisitor<Object, Object> ABSTRACT_VISITOR = new AbstractInstrumentDerivativeVisitor<Object, Object>() {
  };
  private static final Currency CUR = Currency.USD;
  private static final Cash CASH = new Cash(CUR, 0, 1, 1, 0, 1, CURVE_NAME);
  private static final IborIndex INDEX = new IborIndex(CUR, Period.ofMonths(3), 2, new MondayToFridayCalendar("A"), DayCountFactory.INSTANCE.getDayCount("30/360"),
      BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Following"), true);
  private static final ForwardRateAgreement FRA = new ForwardRateAgreement(CUR, 1, CURVE_NAME, 1, 100000, INDEX, 1, 1, 1.25, 0.25, 0.04, CURVE_NAME);
  private static final AnnuityCouponIbor FLOAT_LEG = new AnnuityCouponIbor(CUR, new double[] {1}, INDEX, CURVE_NAME, CURVE_NAME, false);
  private static final AnnuityCouponIbor FLOAT_LEG_2 = new AnnuityCouponIbor(CUR, new double[] {1}, INDEX, CURVE_NAME, CURVE_NAME, true);
  private static final AnnuityCouponFixed FIXED_LEG = new AnnuityCouponFixed(CUR, new double[] {1}, 0.0, CURVE_NAME, true);
  private static final FixedFloatSwap SWAP = new FixedFloatSwap(FIXED_LEG, FLOAT_LEG);
  private static final TenorSwap<CouponIbor> TENOR_SWAP = new TenorSwap<CouponIbor>(FLOAT_LEG, FLOAT_LEG_2);
  private static final PaymentFixed FIXED_PAYMENT = new PaymentFixed(CUR, 1, 1, CURVE_NAME);
  private static final CouponIbor LIBOR_PAYMENT = new CouponIbor(CUR, 1.0, CURVE_NAME, 0, 1, 1, INDEX, 1, 1, 0, CURVE_NAME);
  private static final PaymentFixed FIXED_PAYMENT_2 = new PaymentFixed(CUR, 1, -1, CURVE_NAME);
  private static final CouponIbor LIBOR_PAYMENT_2 = new CouponIbor(CUR, 1.0, CURVE_NAME, 0, -1, 1, INDEX, 1, 1, 0, CURVE_NAME);
  //  private static final CouponFloating FLOATING_COUPON = new CouponFloating(CUR, 1, CURVE_NAME, 1, 1, 1);
  private static final GenericAnnuity<Payment> GA = new GenericAnnuity<Payment>(new Payment[] {FIXED_PAYMENT, LIBOR_PAYMENT});
  private static final GenericAnnuity<Payment> GA_2 = new GenericAnnuity<Payment>(new Payment[] {FIXED_PAYMENT_2, LIBOR_PAYMENT_2});
  private static final FixedCouponSwap<CouponIbor> FCS = new FixedCouponSwap<CouponIbor>(FIXED_LEG, FLOAT_LEG);
  private static final AnnuityCouponFixed FCA = new AnnuityCouponFixed(CUR, new double[] {1}, 0.05, CURVE_NAME, true);
  private static final AnnuityCouponIbor FLA = new AnnuityCouponIbor(CUR, new double[] {1}, INDEX, 0.05, CURVE_NAME, CURVE_NAME, true);
  private static final CouponFixed FCP = new CouponFixed(CUR, 1, CURVE_NAME, 1, 0.04);
  private static final Swap<Payment, Payment> FIXED_FIXED = new Swap<Payment, Payment>(GA, GA_2);
  private static final SwaptionCashFixedIbor SWAPTION_CASH = SwaptionInstrumentsDescriptionDataSet.createSwaptionCashFixedIbor();
  private static final SwaptionPhysicalFixedIbor SWAPTION_PHYS = SwaptionInstrumentsDescriptionDataSet.createSwaptionPhysicalFixedIbor();
  private static final InterestRateFuture IR_FUT_SECURITY = FutureInstrumentsDescriptionDataSet.createInterestRateFutureSecurity();
  private static final BondFuture BNDFUT_SECURITY = FutureInstrumentsDescriptionDataSet.createBondFutureSecurity();

  private static final InstrumentDerivativeVisitor<Object, Class<?>> VISITOR = new InstrumentDerivativeVisitor<Object, Class<?>>() {

    @Override
    public Class<?> visit(final InstrumentDerivative derivative, final Object curves) {
      return derivative.getClass();
    }

    @Override
    public Class<?> visit(final InstrumentDerivative ird) {
      return ird.accept(this, null);
    }

    @Override
    public Class<?> visitCash(final Cash cash, final Object anything) {
      return visit(cash, anything);
    }

    @Override
    public Class<?> visitForwardRateAgreement(final ForwardRateAgreement fra, final Object anything) {
      return visit(fra, anything);
    }

    @Override
    public Class<?> visitTenorSwap(final TenorSwap<? extends Payment> swap, final Object anything) {
      return visit(swap, anything);
    }

    @Override
    public Class<?> visitFixedCouponSwap(final FixedCouponSwap<?> swap, final Object anything) {
      return visit(swap, anything);
    }

    @Override
    public Class<?> visitFixedPayment(final PaymentFixed payment, final Object anything) {
      return visit(payment, anything);
    }

    @Override
    public Class<?> visitCouponIbor(final CouponIbor payment, final Object anything) {
      return visit(payment, anything);
    }

    @Override
    public Class<?> visitGenericAnnuity(final GenericAnnuity<? extends Payment> annuity, final Object anything) {
      return visit(annuity, anything);
    }

    @Override
    public Class<?> visitSwap(final Swap<?, ?> swap, final Object anything) {
      return visit(swap, anything);
    }

    @Override
    public Class<?> visitCash(final Cash cash) {
      return visit(cash);
    }

    @Override
    public Class<?> visitForwardRateAgreement(final ForwardRateAgreement fra) {
      return visit(fra);
    }

    @Override
    public Class<?> visitSwap(final Swap<?, ?> swap) {
      return visit(swap);
    }

    @Override
    public Class<?> visitFixedCouponSwap(final FixedCouponSwap<?> swap) {
      return visit(swap);
    }

    @Override
    public Class<?> visitTenorSwap(final TenorSwap<? extends Payment> swap) {
      return visit(swap);
    }

    @Override
    public Class<?> visitFloatingRateNote(final FloatingRateNote frn) {
      return visit(frn);
    }

    @Override
    public Class<?> visitFixedPayment(final PaymentFixed payment) {
      return visit(payment);
    }

    @Override
    public Class<?> visitCouponIbor(final CouponIbor payment) {
      return visit(payment);
    }

    @Override
    public Class<?> visitFixedCouponAnnuity(final AnnuityCouponFixed fixedCouponAnnuity, final Object anything) {
      return visit(fixedCouponAnnuity, anything);
    }

    @Override
    public Class<?> visitForwardLiborAnnuity(final AnnuityCouponIbor forwardLiborAnnuity, final Object anything) {
      return visit(forwardLiborAnnuity, anything);
    }

    @Override
    public Class<?> visitFixedFloatSwap(final FixedFloatSwap swap, final Object anything) {
      return visit(swap, anything);
    }

    @Override
    public Class<?> visitFixedCouponPayment(final CouponFixed payment, final Object anything) {
      return visit(payment, anything);
    }

    @Override
    public Class<?> visitFixedCouponAnnuity(final AnnuityCouponFixed fixedCouponAnnuity) {
      return visit(fixedCouponAnnuity);
    }

    @Override
    public Class<?> visitForwardLiborAnnuity(final AnnuityCouponIbor forwardLiborAnnuity) {
      return visit(forwardLiborAnnuity);
    }

    @Override
    public Class<?> visitFixedFloatSwap(final FixedFloatSwap swap) {
      return visit(swap);
    }

    @Override
    public Class<?> visitFixedCouponPayment(final CouponFixed payment) {
      return visit(payment);
    }

    @Override
    public Class<?> visitGenericAnnuity(final GenericAnnuity<? extends Payment> genericAnnuity) {
      return visit(genericAnnuity);
    }

    @Override
    public Class<?> visitCouponCMS(final CouponCMS payment, final Object data) {
      return visit(payment, data);
    }

    @Override
    public Class<?> visitCouponCMS(final CouponCMS payment) {
      return visit(payment);
    }

    @Override
    public Class<?> visitSwaptionCashFixedIbor(final SwaptionCashFixedIbor swaption, final Object data) {
      return visit(swaption, data);
    }

    @Override
    public Class<?> visitSwaptionCashFixedIbor(final SwaptionCashFixedIbor swaption) {
      return visit(swaption);
    }

    @Override
    public Class<?> visitSwaptionPhysicalFixedIbor(final SwaptionPhysicalFixedIbor swaption, final Object data) {
      return visit(swaption, data);
    }

    @Override
    public Class<?> visitSwaptionPhysicalFixedIbor(final SwaptionPhysicalFixedIbor swaption) {
      return visit(swaption);
    }

    @Override
    public Class<?> visitCapFloorCMS(final CapFloorCMS payment, final Object data) {
      return visit(payment, data);
    }

    @Override
    public Class<?> visitCapFloorCMS(final CapFloorCMS payment) {
      return visit(payment);
    }

    @Override
    public Class<?>[] visit(final InstrumentDerivative[] derivative, final Object data) {
      return visit(derivative, data);
    }

    @Override
    public Class<?>[] visit(final InstrumentDerivative[] derivative) {
      return visit(derivative);
    }

    @Override
    public Class<?> visitCapFloorIbor(final CapFloorIbor payment, final Object data) {
      return visit(payment, data);
    }

    @Override
    public Class<?> visitCapFloorIbor(final CapFloorIbor payment) {
      return visit(payment);
    }

    @Override
    public Class<?> visitInterestRateFuture(final InterestRateFuture future, final Object data) {
      return visit(future, data);
    }

    @Override
    public Class<?> visitInterestRateFutureSecurity(final InterestRateFuture future) {
      return visit(future);
    }

    @Override
    public Class<?> visitInterestRateFutureOptionPremiumSecurity(final InterestRateFutureOptionPremiumSecurity option, final Object data) {
      return visit(option, data);
    }

    @Override
    public Class<?> visitInterestRateFutureOptionPremiumTransaction(final InterestRateFutureOptionPremiumTransaction option, final Object data) {
      return visit(option, data);
    }

    @Override
    public Class<?> visitInterestRateFutureOptionPremiumSecurity(final InterestRateFutureOptionPremiumSecurity option) {
      return visit(option);
    }

    @Override
    public Class<?> visitInterestRateFutureOptionPremiumTransaction(final InterestRateFutureOptionPremiumTransaction option) {
      return visit(option);
    }

    @Override
    public Class<?> visitInterestRateFutureOptionMarginSecurity(final InterestRateFutureOptionMarginSecurity option, final Object data) {
      return visit(option, data);
    }

    @Override
    public Class<?> visitInterestRateFutureOptionMarginSecurity(final InterestRateFutureOptionMarginSecurity option) {
      return visit(option);
    }

    @Override
    public Class<?> visitInterestRateFutureOptionMarginTransaction(final InterestRateFutureOptionMarginTransaction option, final Object data) {
      return visit(option, data);
    }

    @Override
    public Class<?> visitInterestRateFutureOptionMarginTransaction(final InterestRateFutureOptionMarginTransaction option) {
      return visit(option);
    }

    @Override
    public Class<?> visitCouponIborGearing(final CouponIborGearing payment, final Object data) {
      return visit(payment, data);
    }

    @Override
    public Class<?> visitCouponIborGearing(final CouponIborGearing payment) {
      return visit(payment);
    }

    @Override
    public Class<?> visitBondFixedSecurity(final BondFixedSecurity bond, final Object data) {
      return visit(bond, data);
    }

    @Override
    public Class<?> visitBondFixedTransaction(final BondFixedTransaction bond, final Object data) {
      return visit(bond, data);
    }

    @Override
    public Class<?> visitBondIborSecurity(final BondIborSecurity bond, final Object data) {
      return visit(bond, data);
    }

    @Override
    public Class<?> visitBondIborTransaction(final BondIborTransaction bond, final Object data) {
      return visit(bond, data);
    }

    @Override
    public Class<?> visitBondFixedSecurity(final BondFixedSecurity bond) {
      return visit(bond);
    }

    @Override
    public Class<?> visitBondFixedTransaction(final BondFixedTransaction bond) {
      return visit(bond);
    }

    @Override
    public Class<?> visitBondIborSecurity(final BondIborSecurity bond) {
      return visit(bond);
    }

    @Override
    public Class<?> visitBondIborTransaction(final BondIborTransaction bond) {
      return visit(bond);
    }

    @Override
    public Class<?> visitBondFuture(final BondFuture bondFuture, final Object data) {
      return visit(bondFuture, data);
    }

    @Override
    public Class<?> visitBondFuture(final BondFuture bondFuture) {
      return visit(bondFuture);
    }

    @Override
    public Class<?> visitCapFloorCMSSpread(final CapFloorCMSSpread payment, final Object data) {
      return visit(payment, data);
    }

    @Override
    public Class<?> visitCapFloorCMSSpread(final CapFloorCMSSpread payment) {
      return visit(payment);
    }

    @Override
    public Class<?> visitCouponIborFixed(CouponIborFixed payment, Object data) {
      return null;
    }

    @Override
    public Class<?> visitCouponIborFixed(CouponIborFixed payment) {
      return null;
    }

    @Override
    public Class<?> visitSwaptionBermudaFixedIbor(SwaptionBermudaFixedIbor swaption, Object data) {
      return null;
    }

    @Override
    public Class<?> visitSwaptionBermudaFixedIbor(SwaptionBermudaFixedIbor swaption) {
      return null;
    }

    @Override
    public Class<?> visitCouponInflationZeroCouponMonthly(CouponInflationZeroCouponMonthly coupon, Object data) {
      return null;
    }

    @Override
    public Class<?> visitCouponInflationZeroCouponInterpolation(CouponInflationZeroCouponInterpolation coupon, Object data) {
      return null;
    }

    @Override
    public Class<?> visitCouponInflationZeroCouponMonthly(CouponInflationZeroCouponMonthly coupon) {
      return null;
    }

    @Override
    public Class<?> visitCouponInflationZeroCouponInterpolation(CouponInflationZeroCouponInterpolation coupon) {
      return null;
    }

    @Override
    public Class<?> visitBondCapitalIndexedSecurity(BondCapitalIndexedSecurity<?> bond, Object data) {
      return null;
    }

    @Override
    public Class<?> visitBondCapitalIndexedSecurity(BondCapitalIndexedSecurity<?> bond) {
      return null;
    }

    @Override
    public Class<?> visitBondCapitalIndexedTransaction(BondCapitalIndexedTransaction<?> bond, Object data) {
      return null;
    }

    @Override
    public Class<?> visitBondCapitalIndexedTransaction(BondCapitalIndexedTransaction<?> bond) {
      return null;
    }

    @Override
    public Class<?> visitCouponInflationZeroCouponInterpolationGearing(CouponInflationZeroCouponInterpolationGearing coupon, Object data) {
      return null;
    }

    @Override
    public Class<?> visitCouponInflationZeroCouponInterpolationGearing(CouponInflationZeroCouponInterpolationGearing coupon) {
      return null;
    }

    @Override
    public Class<?> visitCouponInflationZeroCouponMonthlyGearing(CouponInflationZeroCouponMonthlyGearing coupon, Object data) {
      return null;
    }

    @Override
    public Class<?> visitCouponInflationZeroCouponMonthlyGearing(CouponInflationZeroCouponMonthlyGearing coupon) {
      return null;
    }

    @Override
    public Class<?> visitAnnuityCouponIborRatchet(AnnuityCouponIborRatchet annuity, Object data) {
      return null;
    }

    @Override
    public Class<?> visitAnnuityCouponIborRatchet(AnnuityCouponIborRatchet annuity) {
      return null;
    }

    @Override
    public Class<?> visitCouponOIS(CouponOIS payment, Object data) {
      return null;
    }

    @Override
    public Class<?> visitCouponOIS(CouponOIS payment) {
      return null;
    }

    @Override
    public Class<?> visitOISSwap(OISSwap swap, Object data) {
      return null;
    }

    @Override
    public Class<?> visitCrossCurrencySwap(CrossCurrencySwap ccs, Object data) {
      return null;
    }

    @Override
    public Class<?> visitFloatingRateNote(FloatingRateNote frn, Object data) {
      return null;
    }

    @Override
    public Class<?> visitForexForward(ForexForward fx, Object data) {
      return null;
    }

    @Override
    public Class<?> visitCrossCurrencySwap(CrossCurrencySwap ccs) {
      return null;
    }

    @Override
    public Class<?> visitForexForward(ForexForward fx) {
      return null;
    }

    @Override
    public Class<?> visitOISSwap(OISSwap swap) {
      return null;
    }

    @Override
    public Class<?> visitForex(Forex derivative, Object data) {
      return null;
    }

    @Override
    public Class<?> visitForex(Forex derivative) {
      return null;
    }

    @Override
    public Class<?> visitForexSwap(ForexSwap derivative, Object data) {
      return null;
    }

    @Override
    public Class<?> visitForexSwap(ForexSwap derivative) {
      return null;
    }

    @Override
    public Class<?> visitForexOptionVanilla(ForexOptionVanilla derivative, Object data) {
      return null;
    }

    @Override
    public Class<?> visitForexOptionVanilla(ForexOptionVanilla derivative) {
      return null;
    }

    @Override
    public Class<?> visitForexOptionSingleBarrier(ForexOptionSingleBarrier derivative, Object data) {
      return null;
    }

    @Override
    public Class<?> visitForexOptionSingleBarrier(ForexOptionSingleBarrier derivative) {
      return null;
    }

    @Override
    public Class<?> visitForexNonDeliverableForward(ForexNonDeliverableForward derivative, Object data) {
      return null;
    }

    @Override
    public Class<?> visitForexNonDeliverableForward(ForexNonDeliverableForward derivative) {
      return null;
    }

    @Override
    public Class<?> visitForexNonDeliverableOption(ForexNonDeliverableOption derivative, Object data) {
      return null;
    }

    @Override
    public Class<?> visitForexNonDeliverableOption(ForexNonDeliverableOption derivative) {
      return null;
    }

    @Override
    public Class<?> visitDepositIbor(DepositIbor deposit, Object data) {
      return null;
    }

    @Override
    public Class<?> visitDepositIbor(DepositIbor deposit) {
      return null;
    }

    @Override
    public Class<?> visitDepositCounterpart(DepositCounterpart deposit, Object data) {
      return null;
    }

    @Override
    public Class<?> visitDepositCounterpart(DepositCounterpart deposit) {
      return null;
    }

    @Override
    public Class<?> visitForexOptionDigital(ForexOptionDigital derivative, Object data) {
      return null;
    }

    @Override
    public Class<?> visitForexOptionDigital(ForexOptionDigital derivative) {
      return null;
    }
  };

  @Test
  public void test() {
    final Object curves = null;
    assertEquals(VISITOR.visit(CASH, curves), Cash.class);
    assertEquals(FRA.accept(VISITOR, curves), ForwardRateAgreement.class);
    assertEquals(FIXED_LEG.accept(VISITOR, curves), AnnuityCouponFixed.class);
    assertEquals(FLOAT_LEG.accept(VISITOR, curves), AnnuityCouponIbor.class);
    assertEquals(SWAP.accept(VISITOR, curves), FixedFloatSwap.class);
    assertEquals(TENOR_SWAP.accept(VISITOR, curves), TenorSwap.class);
    assertEquals(FIXED_PAYMENT.accept(VISITOR, curves), PaymentFixed.class);
    assertEquals(LIBOR_PAYMENT.accept(VISITOR, curves), CouponIbor.class);
    assertEquals(FCA.accept(VISITOR, curves), AnnuityCouponFixed.class);
    assertEquals(FLA.accept(VISITOR, curves), AnnuityCouponIbor.class);
    assertEquals(FCS.accept(VISITOR, curves), FixedCouponSwap.class);
    assertEquals(FCP.accept(VISITOR, curves), CouponFixed.class);
    assertEquals(GA.accept(VISITOR, curves), GenericAnnuity.class);
    assertEquals(FIXED_FIXED.accept(VISITOR, curves), Swap.class);
    assertEquals(VISITOR.visit(CASH), Cash.class);
    assertEquals(FRA.accept(VISITOR), ForwardRateAgreement.class);
    assertEquals(FIXED_LEG.accept(VISITOR), AnnuityCouponFixed.class);
    assertEquals(FLOAT_LEG.accept(VISITOR), AnnuityCouponIbor.class);
    assertEquals(SWAP.accept(VISITOR), FixedFloatSwap.class);
    assertEquals(TENOR_SWAP.accept(VISITOR), TenorSwap.class);
    assertEquals(FIXED_PAYMENT.accept(VISITOR), PaymentFixed.class);
    assertEquals(LIBOR_PAYMENT.accept(VISITOR), CouponIbor.class);
    assertEquals(GA.accept(VISITOR), GenericAnnuity.class);
    assertEquals(FCA.accept(VISITOR), AnnuityCouponFixed.class);
    assertEquals(FLA.accept(VISITOR), AnnuityCouponIbor.class);
    assertEquals(FCS.accept(VISITOR), FixedCouponSwap.class);
    assertEquals(FCP.accept(VISITOR), CouponFixed.class);
    assertEquals(IR_FUT_SECURITY.accept(VISITOR), InterestRateFuture.class);
    assertEquals(BNDFUT_SECURITY.accept(VISITOR), BondFuture.class);
    assertEquals(FIXED_FIXED.accept(VISITOR), Swap.class);
    assertEquals(SWAPTION_CASH.accept(VISITOR), SwaptionCashFixedIbor.class);
    assertEquals(SWAPTION_PHYS.accept(VISITOR), SwaptionPhysicalFixedIbor.class);
    //    assertEquals(FLOATING_COUPON.accept(VISITOR), CouponFloating.class);
  }

  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testCash1() {
    ABSTRACT_VISITOR.visit(CASH, CURVE_NAME);
  }

  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testCash2() {
    ABSTRACT_VISITOR.visit(CASH);
  }

  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testFRA1() {
    ABSTRACT_VISITOR.visit(FRA, CURVE_NAME);
  }

  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testFRA2() {
    ABSTRACT_VISITOR.visit(FRA);
  }

  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testFloatLeg1() {
    ABSTRACT_VISITOR.visit(FLOAT_LEG, CURVE_NAME);
  }

  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testFloatLeg2() {
    ABSTRACT_VISITOR.visit(FLOAT_LEG);
  }

  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testFixedLeg1() {
    ABSTRACT_VISITOR.visit(FIXED_LEG, CURVE_NAME);
  }

  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testFixedLeg2() {
    ABSTRACT_VISITOR.visit(FIXED_LEG);
  }

  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testSwap1() {
    ABSTRACT_VISITOR.visit(SWAP, CURVE_NAME);
  }

  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testSwap2() {
    ABSTRACT_VISITOR.visit(SWAP);
  }

  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testTenorSwap1() {
    ABSTRACT_VISITOR.visit(TENOR_SWAP, CURVE_NAME);
  }

  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testTenorSwap2() {
    ABSTRACT_VISITOR.visit(TENOR_SWAP);
  }

  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testFixedPayment1() {
    ABSTRACT_VISITOR.visit(FIXED_PAYMENT, CURVE_NAME);
  }

  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testFixedPayment2() {
    ABSTRACT_VISITOR.visit(FIXED_PAYMENT);
  }

  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testLiborPayment1() {
    ABSTRACT_VISITOR.visit(LIBOR_PAYMENT, CURVE_NAME);
  }

  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testLiborPayment2() {
    ABSTRACT_VISITOR.visit(LIBOR_PAYMENT);
  }

  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testGA1() {
    ABSTRACT_VISITOR.visit(GA, CURVE_NAME);
  }

  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testGA2() {
    ABSTRACT_VISITOR.visit(GA);
  }

  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testFCS1() {
    ABSTRACT_VISITOR.visit(FCS, CURVE_NAME);
  }

  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testFCS2() {
    ABSTRACT_VISITOR.visit(FCS);
  }

  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testFCA1() {
    ABSTRACT_VISITOR.visit(FCA, CURVE_NAME);
  }

  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testFCA2() {
    ABSTRACT_VISITOR.visit(FCA);
  }

  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testFLA1() {
    ABSTRACT_VISITOR.visit(FLA, CURVE_NAME);
  }

  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testFLA2() {
    ABSTRACT_VISITOR.visit(FLA);
  }

  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testFCP1() {
    ABSTRACT_VISITOR.visit(FCP, CURVE_NAME);
  }

  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testFCP2() {
    ABSTRACT_VISITOR.visit(FCP);
  }

  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testGeneralSwap1() {
    ABSTRACT_VISITOR.visit(FIXED_FIXED, CURVE_NAME);
  }

  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testGeneralSwap2() {
    ABSTRACT_VISITOR.visit(FIXED_FIXED);
  }

  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testSwaptionCash1() {
    ABSTRACT_VISITOR.visit(SWAPTION_CASH);
  }

  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testSwaptionCash2() {
    ABSTRACT_VISITOR.visit(SWAPTION_CASH, CURVE_NAME);
  }

  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testSwaptionPhysical1() {
    ABSTRACT_VISITOR.visit(SWAPTION_PHYS);
  }

  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testSwaptionPhysical2() {
    ABSTRACT_VISITOR.visit(SWAPTION_PHYS, CURVE_NAME);
  }

  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testIRFutSec() {
    ABSTRACT_VISITOR.visit(IR_FUT_SECURITY);
  }

  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testBondFutSec() {
    ABSTRACT_VISITOR.visit(BNDFUT_SECURITY);
  }

}
