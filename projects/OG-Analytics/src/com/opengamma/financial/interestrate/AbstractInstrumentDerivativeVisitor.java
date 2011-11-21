/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.forex.derivative.Forex;
import com.opengamma.financial.forex.derivative.ForexNonDeliverableForward;
import com.opengamma.financial.forex.derivative.ForexNonDeliverableOption;
import com.opengamma.financial.forex.derivative.ForexOptionSingleBarrier;
import com.opengamma.financial.forex.derivative.ForexOptionVanilla;
import com.opengamma.financial.forex.derivative.ForexSwap;
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
import com.opengamma.financial.interestrate.cash.definition.Cash;
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
import com.opengamma.financial.interestrate.payments.ZZZCouponOIS;
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

/**
 * 
 * @param <S> The type of the data
 * @param <T> The return type of the calculation
 */
public abstract class AbstractInstrumentDerivativeVisitor<S, T> implements InstrumentDerivativeVisitor<S, T> {

  @Override
  public T visit(final InstrumentDerivative derivative, final S data) {
    Validate.notNull(derivative, "derivative");
    Validate.notNull(data, "data");
    return derivative.accept(this, data);
  }

  @Override
  public T[] visit(final InstrumentDerivative[] derivative, final S data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visit(derivative[], data)");
  }

  @Override
  public T visit(final InstrumentDerivative derivative) {
    Validate.notNull(derivative, "derivative");
    return derivative.accept(this);
  }

  @Override
  public T[] visit(final InstrumentDerivative[] derivative) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visit(derivative[])");
  }

  @Override
  public T visitBondFixedSecurity(final BondFixedSecurity bond, final S data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitBondFixedSecurity()");
  }

  @Override
  public T visitBondFixedTransaction(final BondFixedTransaction bond, final S data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitBondFixedTransaction()");
  }

  @Override
  public T visitBondIborSecurity(final BondIborSecurity bond, final S data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitBondIborSecurity()");
  }

  @Override
  public T visitBondIborTransaction(final BondIborTransaction bond, final S data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitBondIborTransaction()");
  }

  @Override
  public T visitFixedCouponAnnuity(final AnnuityCouponFixed fixedCouponAnnuity, final S data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitFixedCouponAnnuity()");
  }

  @Override
  public T visitForwardLiborAnnuity(final AnnuityCouponIbor annuityCouponIbor, final S data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitForwardLiborAnnuity()");
  }

  @Override
  public T visitFixedFloatSwap(final FixedFloatSwap swap, final S data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitFixedFloatSwap()");
  }

  @Override
  public T visitOISSwap(final OISSwap swap, final S data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitOISSwap()");
  }

  @Override
  public T visitTenorSwap(final TenorSwap<? extends Payment> tenorSwap, final S data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitTenorSwap()");
  }

  @Override
  public T visitFloatingRateNote(FloatingRateNote frn, S data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support  visitFloatingRateNote()");
  }

  @Override
  public T visitCrossCurrencySwap(final CrossCurrencySwap ccs, final S data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitCrossCurrencySwap()");
  }

  @Override
  public T visitForexForward(final ForexForward fx, final S data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitForexForward()");
  }

  @Override
  public T visitCash(final Cash cash, final S data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitCash()");
  }

  @Override
  public T visitBondFuture(BondFuture bondFuture, S data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitBondFuture()");
  }

  @Override
  public T visitInterestRateFuture(final InterestRateFuture future, final S data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitInterestRateFutureSecurity()");
  }

  @Override
  public T visitInterestRateFutureOptionPremiumSecurity(final InterestRateFutureOptionPremiumSecurity option, final S data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitInterestRateFutureOptionPremiumSecurity()");
  }

  @Override
  public T visitInterestRateFutureOptionPremiumTransaction(final InterestRateFutureOptionPremiumTransaction option, final S data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitInterestRateFutureOptionPremiumSecurity()");
  }

  @Override
  public T visitInterestRateFutureOptionMarginSecurity(final InterestRateFutureOptionMarginSecurity option, final S data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitInterestRateFutureOptionMarginSecurity()");
  }

  @Override
  public T visitInterestRateFutureOptionMarginTransaction(final InterestRateFutureOptionMarginTransaction option, final S data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitInterestRateFutureOptionMarginTransaction()");
  }

  @Override
  public T visitZZZCouponOIS(final ZZZCouponOIS payment, final S data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitContinuouslyMonitoredAverageRatePayment()");
  }

  @Override
  public T visitFixedCouponPayment(final CouponFixed payment, final S data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitFixedCouponPayment()");
  }

  @Override
  public T visitCouponIbor(final CouponIbor payment, final S data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitCouponIbor()");
  }

  @Override
  public T visitCouponIborGearing(final CouponIborGearing payment, final S data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitCouponIborGearing()");
  }

  @Override
  public T visitCouponOIS(final CouponOIS payment, final S data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitCouponOIS()");
  }

  @Override
  public T visitCouponCMS(final CouponCMS payment, final S data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitCouponCMS()");
  }

  @Override
  public T visitCapFloorIbor(final CapFloorIbor payment, final S data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitCapFloorIbor()");
  }

  @Override
  public T visitCapFloorCMS(final CapFloorCMS payment, final S data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitCapFloorCMS()");
  }

  @Override
  public T visitCapFloorCMSSpread(final CapFloorCMSSpread payment, final S data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitCapFloorCMSSpread()");
  }

  @Override
  public T visitForwardRateAgreement(final ForwardRateAgreement fra, final S data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitForwardRateAgreement()");
  }

  @Override
  public T visitGenericAnnuity(final GenericAnnuity<? extends Payment> genericAnnuity, final S data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitGenericAnnuity()");
  }

  @Override
  public T visitAnnuityCouponIborRatchet(final AnnuityCouponIborRatchet annuity, final S data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitAnnuityCouponIborRatchet()");
  }

  @Override
  public T visitSwap(final Swap<?, ?> swap, final S data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitSwap()");
  }

  @Override
  public T visitFixedCouponSwap(final FixedCouponSwap<?> swap, final S data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitFixedCouponSwap()");
  }

  @Override
  public T visitSwaptionCashFixedIbor(final SwaptionCashFixedIbor swaption, final S data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitSwaptionCashFixedIbor()");
  }

  @Override
  public T visitSwaptionPhysicalFixedIbor(final SwaptionPhysicalFixedIbor swaption, final S data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitSwaptionPhysicalFixedIbor()");
  }

  @Override
  public T visitSwaptionBermudaFixedIbor(final SwaptionBermudaFixedIbor swaption, final S data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitSwaptionBermudaFixedIbor()");
  }

  @Override
  public T visitFixedPayment(final PaymentFixed payment, final S data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitFixedPayment()");
  }

  @Override
  public T visitBondFixedSecurity(final BondFixedSecurity bond) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitBondFixedSecurity()");
  }

  @Override
  public T visitBondFixedTransaction(final BondFixedTransaction bond) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitBondFixedTransaction()");
  }

  @Override
  public T visitBondIborSecurity(final BondIborSecurity bond) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitBondIborSecurity()");
  }

  @Override
  public T visitBondIborTransaction(final BondIborTransaction bond) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitBondIborTransaction()");
  }

  @Override
  public T visitFixedCouponAnnuity(final AnnuityCouponFixed fixedCouponAnnuity) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitFixedCouponAnnuity()");
  }

  @Override
  public T visitForwardLiborAnnuity(final AnnuityCouponIbor forwardLiborAnnuity) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitForwardLiborAnnuity()");
  }

  @Override
  public T visitFixedFloatSwap(final FixedFloatSwap swap) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitFixedFloatSwap()");
  }

  @Override
  public T visitFloatingRateNote(final FloatingRateNote frn) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitFloatingRateNote()");
  }

  @Override
  public T visitCrossCurrencySwap(final CrossCurrencySwap ccs) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitCrossCurrencySwap()");
  }

  @Override
  public T visitForexForward(final ForexForward fx) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitForexForward()");
  }

  @Override
  public T visitTenorSwap(final TenorSwap<? extends Payment> tenorSwap) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitTenorSwap()");
  }

  @Override
  public T visitCash(final Cash cash) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitCash()");
  }

  @Override
  public T visitBondFuture(BondFuture bondFuture) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitBondFuture()");
  }

  @Override
  public T visitInterestRateFutureSecurity(final InterestRateFuture future) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitInterestRateFutureSecurity()");
  }

  @Override
  public T visitInterestRateFutureOptionPremiumSecurity(final InterestRateFutureOptionPremiumSecurity option) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitInterestRateFutureOptionPremiumSecurity()");
  }

  @Override
  public T visitInterestRateFutureOptionPremiumTransaction(final InterestRateFutureOptionPremiumTransaction option) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitInterestRateFutureOptionPremiumSecurity()");
  }

  @Override
  public T visitInterestRateFutureOptionMarginSecurity(final InterestRateFutureOptionMarginSecurity option) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitInterestRateFutureOptionMarginSecurity()");
  }

  @Override
  public T visitInterestRateFutureOptionMarginTransaction(final InterestRateFutureOptionMarginTransaction option) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitInterestRateFutureOptionMarginTransaction()");
  }

  @Override
  public T visitZZZCouponOIS(final ZZZCouponOIS payment) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitContinuouslyMonitoredAverageRatePayment()");
  }

  @Override
  public T visitFixedCouponPayment(final CouponFixed payment) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitFixedCouponPayment()");
  }

  @Override
  public T visitCouponIbor(final CouponIbor payment) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitCouponIbor()");
  }

  @Override
  public T visitCouponIborGearing(final CouponIborGearing payment) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitCouponIborGearing()");
  }

  @Override
  public T visitCouponOIS(final CouponOIS payment) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitCouponOIS()");
  }

  @Override
  public T visitCouponCMS(final CouponCMS payment) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitCouponCMS()");
  }

  @Override
  public T visitCapFloorIbor(final CapFloorIbor payment) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitCapFloorIbor()");
  }

  @Override
  public T visitCapFloorCMS(final CapFloorCMS payment) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitCapFloorCMS()");
  }

  @Override
  public T visitCapFloorCMSSpread(final CapFloorCMSSpread payment) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitCapFloorCMSSpread()");
  }

  @Override
  public T visitForwardRateAgreement(final ForwardRateAgreement fra) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitForwardRateAgreement()");
  }

  @Override
  public T visitGenericAnnuity(final GenericAnnuity<? extends Payment> genericAnnuity) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitGenericAnnuity()");
  }

  @Override
  public T visitAnnuityCouponIborRatchet(final AnnuityCouponIborRatchet annuity) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitAnnuityCouponIborRatchet()");
  }

  @Override
  public T visitSwap(final Swap<?, ?> swap) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitSwap()");
  }

  @Override
  public T visitSwaptionCashFixedIbor(final SwaptionCashFixedIbor swaption) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitSwaptionCashFixedIbor()");
  }

  @Override
  public T visitSwaptionPhysicalFixedIbor(final SwaptionPhysicalFixedIbor swaption) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitSwaptionPhysicalFixedIbor()");
  }

  @Override
  public T visitSwaptionBermudaFixedIbor(final SwaptionBermudaFixedIbor swaption) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitSwaptionBermudaFixedIbor()");
  }

  @Override
  public T visitFixedCouponSwap(final FixedCouponSwap<?> swap) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitFixedCouponSwap()");
  }

  @Override
  public T visitOISSwap(final OISSwap swap) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitOISSwap()");
  }

  @Override
  public T visitFixedPayment(final PaymentFixed payment) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitFixedPayment()");
  }

  @Override
  public T visitCouponIborFixed(CouponIborFixed payment) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitCouponIborFixed()");
  }

  @Override
  public T visitCouponIborFixed(CouponIborFixed payment, S data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitCouponIborFixed()");
  }

  @Override
  public T visitCouponInflationZeroCouponMonthly(CouponInflationZeroCouponMonthly coupon) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitCouponInflationZeroCouponMonthly()");
  }

  @Override
  public T visitCouponInflationZeroCouponMonthly(CouponInflationZeroCouponMonthly coupon, S data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitCouponInflationZeroCouponMonthly()");
  }

  @Override
  public T visitCouponInflationZeroCouponMonthlyGearing(CouponInflationZeroCouponMonthlyGearing coupon) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitCouponInflationZeroCouponMonthlyGearing()");
  }

  @Override
  public T visitCouponInflationZeroCouponMonthlyGearing(CouponInflationZeroCouponMonthlyGearing coupon, S data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitCouponInflationZeroCouponMonthlyGearing()");
  }

  @Override
  public T visitCouponInflationZeroCouponInterpolation(CouponInflationZeroCouponInterpolation coupon) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitCouponInflationZeroCouponInterpolation()");
  }

  @Override
  public T visitCouponInflationZeroCouponInterpolation(CouponInflationZeroCouponInterpolation coupon, S data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitCouponInflationZeroCouponInterpolation()");
  }

  @Override
  public T visitCouponInflationZeroCouponInterpolationGearing(CouponInflationZeroCouponInterpolationGearing coupon) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitCouponInflationZeroCouponInterpolationGearing()");
  }

  @Override
  public T visitCouponInflationZeroCouponInterpolationGearing(CouponInflationZeroCouponInterpolationGearing coupon, S data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitCouponInflationZeroCouponInterpolationGearing()");
  }

  @Override
  public T visitBondCapitalIndexedSecurity(BondCapitalIndexedSecurity<?> bond) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitBondCapitalIndexedSecurity()");
  }

  @Override
  public T visitBondCapitalIndexedSecurity(BondCapitalIndexedSecurity<?> bond, S data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitBondCapitalIndexedSecurity()");
  }

  @Override
  public T visitBondCapitalIndexedTransaction(BondCapitalIndexedTransaction<?> bond) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitBondCapitalIndexedTransaction()");
  }

  @Override
  public T visitBondCapitalIndexedTransaction(BondCapitalIndexedTransaction<?> bond, S data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitBondCapitalIndexedTransaction()");
  }

  // -----     Forex     -----

  @Override
  public T visitForex(final Forex derivative, final S data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitForex()");
  }

  @Override
  public T visitForex(final Forex derivative) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitForex()");
  }

  @Override
  public T visitForexSwap(final ForexSwap derivative, final S data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitForexSwap()");
  }

  @Override
  public T visitForexSwap(final ForexSwap derivative) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitForexSwap()");
  }

  @Override
  public T visitForexOptionVanilla(final ForexOptionVanilla derivative, final S data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitForexOptionVanilla()");
  }

  @Override
  public T visitForexOptionVanilla(final ForexOptionVanilla derivative) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitForexOptionVanilla()");
  }

  @Override
  public T visitForexOptionSingleBarrier(final ForexOptionSingleBarrier derivative, final S data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitForexOptionSingleBarrier()");
  }

  @Override
  public T visitForexOptionSingleBarrier(final ForexOptionSingleBarrier derivative) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitForexOptionSingleBarrier()");
  }

  @Override
  public T visitForexNonDeliverableForward(final ForexNonDeliverableForward derivative, final S data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitForexNonDeliverableForward()");
  }

  @Override
  public T visitForexNonDeliverableForward(final ForexNonDeliverableForward derivative) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitForexNonDeliverableForward()");
  }

  @Override
  public T visitForexNonDeliverableOption(final ForexNonDeliverableOption derivative, final S data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitForexNonDeliverableOption()");
  }

  @Override
  public T visitForexNonDeliverableOption(final ForexNonDeliverableOption derivative) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitForexNonDeliverableOption()");
  }

}
