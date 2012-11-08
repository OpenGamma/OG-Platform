/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate;

import com.opengamma.analytics.financial.commodity.derivative.AgricultureForward;
import com.opengamma.analytics.financial.commodity.derivative.AgricultureFuture;
import com.opengamma.analytics.financial.commodity.derivative.AgricultureFutureOption;
import com.opengamma.analytics.financial.commodity.derivative.EnergyForward;
import com.opengamma.analytics.financial.commodity.derivative.EnergyFuture;
import com.opengamma.analytics.financial.commodity.derivative.EnergyFutureOption;
import com.opengamma.analytics.financial.commodity.derivative.MetalForward;
import com.opengamma.analytics.financial.commodity.derivative.MetalFuture;
import com.opengamma.analytics.financial.commodity.derivative.MetalFutureOption;
import com.opengamma.analytics.financial.credit.cds.ISDACDSDerivative;
import com.opengamma.analytics.financial.forex.derivative.Forex;
import com.opengamma.analytics.financial.forex.derivative.ForexNonDeliverableForward;
import com.opengamma.analytics.financial.forex.derivative.ForexNonDeliverableOption;
import com.opengamma.analytics.financial.forex.derivative.ForexOptionDigital;
import com.opengamma.analytics.financial.forex.derivative.ForexOptionSingleBarrier;
import com.opengamma.analytics.financial.forex.derivative.ForexOptionVanilla;
import com.opengamma.analytics.financial.forex.derivative.ForexSwap;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.Annuity;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.AnnuityCouponFixed;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.AnnuityCouponIbor;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.AnnuityCouponIborRatchet;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.AnnuityCouponIborSpread;
import com.opengamma.analytics.financial.interestrate.bond.definition.BillSecurity;
import com.opengamma.analytics.financial.interestrate.bond.definition.BillTransaction;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondCapitalIndexedSecurity;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondCapitalIndexedTransaction;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondFixedSecurity;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondFixedTransaction;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondIborSecurity;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondIborTransaction;
import com.opengamma.analytics.financial.interestrate.cash.derivative.Cash;
import com.opengamma.analytics.financial.interestrate.cash.derivative.DepositCounterpart;
import com.opengamma.analytics.financial.interestrate.cash.derivative.DepositIbor;
import com.opengamma.analytics.financial.interestrate.cash.derivative.DepositZero;
import com.opengamma.analytics.financial.interestrate.fra.ForwardRateAgreement;
import com.opengamma.analytics.financial.interestrate.future.derivative.BondFuture;
import com.opengamma.analytics.financial.interestrate.future.derivative.BondFutureOptionPremiumSecurity;
import com.opengamma.analytics.financial.interestrate.future.derivative.BondFutureOptionPremiumTransaction;
import com.opengamma.analytics.financial.interestrate.future.derivative.DeliverableSwapFuturesSecurity;
import com.opengamma.analytics.financial.interestrate.future.derivative.FederalFundsFutureSecurity;
import com.opengamma.analytics.financial.interestrate.future.derivative.FederalFundsFutureTransaction;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFuture;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureOptionMarginSecurity;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureOptionMarginTransaction;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureOptionPremiumSecurity;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureOptionPremiumTransaction;
import com.opengamma.analytics.financial.interestrate.inflation.derivative.CouponInflationZeroCouponInterpolation;
import com.opengamma.analytics.financial.interestrate.inflation.derivative.CouponInflationZeroCouponInterpolationGearing;
import com.opengamma.analytics.financial.interestrate.inflation.derivative.CouponInflationZeroCouponMonthly;
import com.opengamma.analytics.financial.interestrate.inflation.derivative.CouponInflationZeroCouponMonthlyGearing;
import com.opengamma.analytics.financial.interestrate.payments.ForexForward;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CapFloorCMS;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CapFloorCMSSpread;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CapFloorIbor;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponCMS;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixed;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIbor;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborCompounded;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborGearing;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborSpread;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponOIS;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.analytics.financial.interestrate.payments.derivative.PaymentFixed;
import com.opengamma.analytics.financial.interestrate.swap.derivative.CrossCurrencySwap;
import com.opengamma.analytics.financial.interestrate.swap.derivative.FixedFloatSwap;
import com.opengamma.analytics.financial.interestrate.swap.derivative.FloatingRateNote;
import com.opengamma.analytics.financial.interestrate.swap.derivative.Swap;
import com.opengamma.analytics.financial.interestrate.swap.derivative.SwapFixedCoupon;
import com.opengamma.analytics.financial.interestrate.swap.derivative.TenorSwap;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionBermudaFixedIbor;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionCashFixedIbor;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionPhysicalFixedIbor;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 * @param <S> The type of the data
 * @param <T> The return type of the calculation
 */
public abstract class AbstractInstrumentDerivativeVisitor<S, T> implements InstrumentDerivativeVisitor<S, T> {
  private final T _defaultReturnType;

  public AbstractInstrumentDerivativeVisitor(final T defaultReturnType) {
    _defaultReturnType = defaultReturnType;
  }

  public AbstractInstrumentDerivativeVisitor() {
    _defaultReturnType = null;
  }

  @Override
  public T visit(final InstrumentDerivative derivative, final S data) {
    ArgumentChecker.notNull(derivative, "derivative");
    ArgumentChecker.notNull(data, "data");
    if (_defaultReturnType != null) {
      return _defaultReturnType;
    }
    throw new UnsupportedOperationException(getClass().getSimpleName() + " does not support instruments of type " + derivative.getClass().getSimpleName()
        + " with data of type " + data.getClass().getSimpleName());
  }

  @Override
  public T[] visit(final InstrumentDerivative[] derivative, final S data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visit(derivative[], data)");
  }

  @Override
  public T visit(final InstrumentDerivative derivative) {
    ArgumentChecker.notNull(derivative, "derivative");
    if (_defaultReturnType != null) {
      return _defaultReturnType;
    }
    throw new UnsupportedOperationException(getClass().getSimpleName() + " does not support instruments of type " + derivative.getClass().getSimpleName());
  }

  @Override
  public T[] visit(final InstrumentDerivative[] derivative) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visit(derivative[])");
  }

  @Override
  public T visitBondFixedSecurity(final BondFixedSecurity bond, final S data) {
    return visit(bond, data);
  }

  @Override
  public T visitBondFixedTransaction(final BondFixedTransaction bond, final S data) {
    return visit(bond, data);
  }

  @Override
  public T visitBondIborSecurity(final BondIborSecurity bond, final S data) {
    return visit(bond, data);
  }

  @Override
  public T visitBondIborTransaction(final BondIborTransaction bond, final S data) {
    return visit(bond, data);
  }

  @Override
  public T visitBillSecurity(final BillSecurity bill, final S data) {
    return visit(bill, data);
  }

  @Override
  public T visitBillTransaction(final BillTransaction bill, final S data) {
    return visit(bill, data);
  }

  @Override
  public T visitFixedCouponAnnuity(final AnnuityCouponFixed fixedCouponAnnuity, final S data) {
    return visit(fixedCouponAnnuity, data);
  }

  @Override
  public T visitCapFloorIbor(final CapFloorIbor payment, final S data) {
    return visit(payment, data);
  }

  @Override
  public T visitCapFloorCMS(final CapFloorCMS payment, final S data) {
    return visit(payment, data);
  }

  @Override
  public T visitCapFloorCMSSpread(final CapFloorCMSSpread payment, final S data) {
    return visit(payment, data);
  }

  @Override
  public T visitForwardRateAgreement(final ForwardRateAgreement fra, final S data) {
    return visit(fra, data);
  }

  @Override
  public T visitGenericAnnuity(final Annuity<? extends Payment> genericAnnuity, final S data) {
    return visit(genericAnnuity, data);
  }

  @Override
  public T visitAnnuityCouponIborRatchet(final AnnuityCouponIborRatchet annuity, final S data) {
    return visit(annuity, data);
  }

  @Override
  public T visitFixedCouponSwap(final SwapFixedCoupon<?> swap, final S data) {
    return visit(swap, data);
  }

  @Override
  public T visitSwaptionCashFixedIbor(final SwaptionCashFixedIbor swaption, final S data) {
    return visit(swaption, data);
  }

  @Override
  public T visitSwaptionPhysicalFixedIbor(final SwaptionPhysicalFixedIbor swaption, final S data) {
    return visit(swaption, data);
  }

  @Override
  public T visitSwaptionBermudaFixedIbor(final SwaptionBermudaFixedIbor swaption, final S data) {
    return visit(swaption, data);
  }

  @Override
  public T visitFixedPayment(final PaymentFixed payment, final S data) {
    return visit(payment, data);
  }

  @Override
  public T visitCDSDerivative(final ISDACDSDerivative cds, final S data) {
    return visit(cds, data);
  }

  @Override
  public T visitBondFixedSecurity(final BondFixedSecurity bond) {
    return visit(bond);
  }

  @Override
  public T visitBondFixedTransaction(final BondFixedTransaction bond) {
    return visit(bond);
  }

  @Override
  public T visitBondIborSecurity(final BondIborSecurity bond) {
    return visit(bond);
  }

  @Override
  public T visitBondIborTransaction(final BondIborTransaction bond) {
    return visit(bond);
  }

  @Override
  public T visitBillSecurity(final BillSecurity bill) {
    return visit(bill);
  }

  @Override
  public T visitBillTransaction(final BillTransaction bill) {
    return visit(bill);
  }

  @Override
  public T visitFixedCouponAnnuity(final AnnuityCouponFixed fixedCouponAnnuity) {
    return visit(fixedCouponAnnuity);
  }

  @Override
  public T visitCash(final Cash cash, final S data) {
    return visit(cash, data);
  }

  @Override
  public T visitCash(final Cash cash) {
    return visit(cash);
  }

  @Override
  public T visitCapFloorIbor(final CapFloorIbor payment) {
    return visit(payment);
  }

  @Override
  public T visitCapFloorCMS(final CapFloorCMS payment) {
    return visit(payment);
  }

  @Override
  public T visitCapFloorCMSSpread(final CapFloorCMSSpread payment) {
    return visit(payment);
  }

  @Override
  public T visitForwardRateAgreement(final ForwardRateAgreement fra) {
    return visit(fra);
  }

  @Override
  public T visitGenericAnnuity(final Annuity<? extends Payment> genericAnnuity) {
    return visit(genericAnnuity);
  }

  @Override
  public T visitAnnuityCouponIborRatchet(final AnnuityCouponIborRatchet annuity) {
    return visit(annuity);
  }

  @Override
  public T visitSwaptionCashFixedIbor(final SwaptionCashFixedIbor swaption) {
    return visit(swaption);
  }

  @Override
  public T visitSwaptionPhysicalFixedIbor(final SwaptionPhysicalFixedIbor swaption) {
    return visit(swaption);
  }

  @Override
  public T visitSwaptionBermudaFixedIbor(final SwaptionBermudaFixedIbor swaption) {
    return visit(swaption);
  }

  @Override
  public T visitFixedCouponSwap(final SwapFixedCoupon<?> swap) {
    return visit(swap);
  }

  @Override
  public T visitFixedPayment(final PaymentFixed payment) {
    return visit(payment);
  }

  @Override
  public T visitCDSDerivative(final ISDACDSDerivative cds) {
    return visit(cds);
  }

  // -----     Payment and coupon     -----

  @Override
  public T visitCouponFixed(final CouponFixed payment, final S data) {
    return visit(payment, data);
  }

  @Override
  public T visitCouponFixed(final CouponFixed payment) {
    return visit(payment);
  }

  @Override
  public T visitCouponIbor(final CouponIbor payment, final S data) {
    return visit(payment, data);
  }

  @Override
  public T visitCouponIbor(final CouponIbor payment) {
    return visit(payment);
  }

  @Override
  public T visitCouponIborSpread(final CouponIborSpread payment, final S data) {
    return visit(payment, data);
  }

  @Override
  public T visitCouponIborSpread(final CouponIborSpread payment) {
    return visit(payment);
  }

  @Override
  public T visitCouponIborGearing(final CouponIborGearing payment, final S data) {
    return visit(payment, data);
  }

  @Override
  public T visitCouponIborGearing(final CouponIborGearing payment) {
    return visit(payment);
  }

  @Override
  public T visitCouponIborCompounded(final CouponIborCompounded payment, final S data) {
    return visit(payment, data);
  }

  @Override
  public T visitCouponIborCompounded(final CouponIborCompounded payment) {
    return visit(payment);
  }

  @Override
  public T visitCouponOIS(final CouponOIS payment, final S data) {
    return visit(payment, data);
  }

  @Override
  public T visitCouponCMS(final CouponCMS payment, final S data) {
    return visitCouponCMS(payment, data);
  }

  @Override
  public T visitCouponOIS(final CouponOIS payment) {
    return visit(payment);
  }

  @Override
  public T visitCouponCMS(final CouponCMS payment) {
    return visit(payment);
  }

  // -----     Inflation     -----

  @Override
  public T visitCouponInflationZeroCouponMonthly(final CouponInflationZeroCouponMonthly coupon) {
    return visit(coupon);
  }

  @Override
  public T visitCouponInflationZeroCouponMonthly(final CouponInflationZeroCouponMonthly coupon, final S data) {
    return visit(coupon, data);
  }

  @Override
  public T visitCouponInflationZeroCouponMonthlyGearing(final CouponInflationZeroCouponMonthlyGearing coupon) {
    return visit(coupon);
  }

  @Override
  public T visitCouponInflationZeroCouponMonthlyGearing(final CouponInflationZeroCouponMonthlyGearing coupon, final S data) {
    return visit(coupon, data);
  }

  @Override
  public T visitCouponInflationZeroCouponInterpolation(final CouponInflationZeroCouponInterpolation coupon) {
    return visit(coupon);
  }

  @Override
  public T visitCouponInflationZeroCouponInterpolation(final CouponInflationZeroCouponInterpolation coupon, final S data) {
    return visit(coupon, data);
  }

  @Override
  public T visitCouponInflationZeroCouponInterpolationGearing(final CouponInflationZeroCouponInterpolationGearing coupon) {
    return visit(coupon);
  }

  @Override
  public T visitCouponInflationZeroCouponInterpolationGearing(final CouponInflationZeroCouponInterpolationGearing coupon, final S data) {
    return visit(coupon, data);
  }

  @Override
  public T visitBondCapitalIndexedSecurity(final BondCapitalIndexedSecurity<?> bond) {
    return visit(bond);
  }

  @Override
  public T visitBondCapitalIndexedSecurity(final BondCapitalIndexedSecurity<?> bond, final S data) {
    return visit(bond, data);
  }

  @Override
  public T visitBondCapitalIndexedTransaction(final BondCapitalIndexedTransaction<?> bond) {
    return visit(bond);
  }

  @Override
  public T visitBondCapitalIndexedTransaction(final BondCapitalIndexedTransaction<?> bond, final S data) {
    return visit(bond, data);
  }

  // -----     Futures     -----

  @Override
  public T visitBondFuture(final BondFuture bondFuture, final S data) {
    return visit(bondFuture, data);
  }

  @Override
  public T visitBondFuture(final BondFuture bondFuture) {
    return visit(bondFuture);
  }

  @Override
  public T visitInterestRateFuture(final InterestRateFuture future, final S data) {
    return visit(future, data);
  }

  @Override
  public T visitInterestRateFuture(final InterestRateFuture future) {
    return visit(future);
  }

  @Override
  public T visitFederalFundsFutureSecurity(final FederalFundsFutureSecurity future, final S data) {
    return visit(future, data);
  }

  @Override
  public T visitFederalFundsFutureSecurity(final FederalFundsFutureSecurity future) {
    return visit(future);
  }

  @Override
  public T visitFederalFundsFutureTransaction(final FederalFundsFutureTransaction future, final S data) {
    return visit(future, data);
  }

  @Override
  public T visitFederalFundsFutureTransaction(final FederalFundsFutureTransaction future) {
    return visit(future);
  }

  @Override
  public T visitDeliverableSwapFuturesSecurity(final DeliverableSwapFuturesSecurity futures, final S data) {
    return visit(futures, data);
  }

  @Override
  public T visitDeliverableSwapFuturesSecurity(final DeliverableSwapFuturesSecurity futures) {
    return visit(futures);
  }

  @Override
  public T visitInterestRateFutureOptionMarginSecurity(final InterestRateFutureOptionMarginSecurity option, final S data) {
    return visit(option, data);
  }

  @Override
  public T visitInterestRateFutureOptionMarginSecurity(final InterestRateFutureOptionMarginSecurity option) {
    return visit(option);
  }

  @Override
  public T visitInterestRateFutureOptionMarginTransaction(final InterestRateFutureOptionMarginTransaction option, final S data) {
    return visit(option, data);
  }

  @Override
  public T visitInterestRateFutureOptionMarginTransaction(final InterestRateFutureOptionMarginTransaction option) {
    return visit(option);
  }

  @Override
  public T visitInterestRateFutureOptionPremiumSecurity(final InterestRateFutureOptionPremiumSecurity option, final S data) {
    return visit(option, data);
  }

  @Override
  public T visitInterestRateFutureOptionPremiumSecurity(final InterestRateFutureOptionPremiumSecurity option) {
    return visit(option);
  }

  @Override
  public T visitInterestRateFutureOptionPremiumTransaction(final InterestRateFutureOptionPremiumTransaction option, final S data) {
    return visit(option, data);
  }

  @Override
  public T visitInterestRateFutureOptionPremiumTransaction(final InterestRateFutureOptionPremiumTransaction option) {
    return visit(option);
  }

  @Override
  public T visitBondFutureOptionPremiumSecurity(final BondFutureOptionPremiumSecurity option, final S data) {
    return visit(option, data);
  }

  @Override
  public T visitBondFutureOptionPremiumSecurity(final BondFutureOptionPremiumSecurity option) {
    return visit(option);
  }

  @Override
  public T visitBondFutureOptionPremiumTransaction(final BondFutureOptionPremiumTransaction option, final S data) {
    return visit(option, data);
  }

  @Override
  public T visitBondFutureOptionPremiumTransaction(final BondFutureOptionPremiumTransaction option) {
    return visit(option);
  }

  // -----     Annuity     -----

  // -----     Swap     -----

  @Override
  public T visitSwap(final Swap<?, ?> swap, final S data) {
    return visitSwap(swap, data);
  }

  @Override
  public T visitSwap(final Swap<?, ?> swap) {
    return visitSwap(swap);
  }

  // -----     Deposit     -----

  @Override
  public T visitDepositIbor(final DepositIbor deposit, final S data) {
    return visit(deposit, data);
  }

  @Override
  public T visitDepositIbor(final DepositIbor deposit) {
    return visit(deposit);
  }

  @Override
  public T visitDepositCounterpart(final DepositCounterpart deposit, final S data) {
    return visit(deposit, data);
  }

  @Override
  public T visitDepositCounterpart(final DepositCounterpart deposit) {
    return visit(deposit);
  }

  @Override
  public T visitDepositZero(final DepositZero deposit, final S data) {
    return visit(deposit, data);
  }

  @Override
  public T visitDepositZero(final DepositZero deposit) {
    return visit(deposit);
  }

  // -----     Forex     -----

  @Override
  public T visitForex(final Forex derivative, final S data) {
    return visit(derivative, data);
  }

  @Override
  public T visitForex(final Forex derivative) {
    return visit(derivative);
  }

  @Override
  public T visitForexSwap(final ForexSwap derivative, final S data) {
    return visit(derivative, data);
  }

  @Override
  public T visitForexSwap(final ForexSwap derivative) {
    return visit(derivative);
  }

  @Override
  public T visitForexOptionVanilla(final ForexOptionVanilla derivative, final S data) {
    return visit(derivative, data);
  }

  @Override
  public T visitForexOptionVanilla(final ForexOptionVanilla derivative) {
    return visit(derivative);
  }

  @Override
  public T visitForexOptionSingleBarrier(final ForexOptionSingleBarrier derivative, final S data) {
    return visit(derivative, data);
  }

  @Override
  public T visitForexOptionSingleBarrier(final ForexOptionSingleBarrier derivative) {
    return visit(derivative);
  }

  @Override
  public T visitForexNonDeliverableForward(final ForexNonDeliverableForward derivative, final S data) {
    return visit(derivative, data);
  }

  @Override
  public T visitForexNonDeliverableForward(final ForexNonDeliverableForward derivative) {
    return visit(derivative);
  }

  @Override
  public T visitForexNonDeliverableOption(final ForexNonDeliverableOption derivative, final S data) {
    return visit(derivative, data);
  }

  @Override
  public T visitForexNonDeliverableOption(final ForexNonDeliverableOption derivative) {
    return visit(derivative);
  }

  @Override
  public T visitForexOptionDigital(final ForexOptionDigital derivative, final S data) {
    return visit(derivative, data);
  }

  @Override
  public T visitForexOptionDigital(final ForexOptionDigital derivative) {
    return visit(derivative);
  }

  //-----     Commodity     -----

  @Override
  public T visitMetalForward(final MetalForward forward, final S data) {
    return visit(forward, data);
  }

  @Override
  public T visitMetalForward(final MetalForward forward) {
    return visit(forward);
  }

  @Override
  public T visitMetalFuture(final MetalFuture future, final S data) {
    return visit(future, data);
  }

  @Override
  public T visitMetalFuture(final MetalFuture future) {
    return visit(future);
  }

  @Override
  public T visitMetalFutureOption(final MetalFutureOption option, final S data) {
    return visit(option, data);
  }

  @Override
  public T visitMetalFutureOption(final MetalFutureOption option) {
    return visit(option);
  }

  @Override
  public T visitAgricultureForward(final AgricultureForward forward, final S data) {
    return visit(forward, data);
  }

  @Override
  public T visitAgricultureForward(final AgricultureForward forward) {
    return visit(forward);
  }

  @Override
  public T visitAgricultureFuture(final AgricultureFuture future, final S data) {
    return visit(future, data);
  }

  @Override
  public T visitAgricultureFuture(final AgricultureFuture future) {
    return visit(future);
  }

  @Override
  public T visitAgricultureFutureOption(final AgricultureFutureOption option, final S data) {
    return visit(option, data);
  }

  @Override
  public T visitAgricultureFutureOption(final AgricultureFutureOption option) {
    return visit(option);
  }

  @Override
  public T visitEnergyForward(final EnergyForward forward, final S data) {
    return visit(forward, data);
  }

  @Override
  public T visitEnergyForward(final EnergyForward future) {
    return visit(future);
  }

  @Override
  public T visitEnergyFuture(final EnergyFuture future, final S data) {
    return visit(future, data);
  }

  @Override
  public T visitEnergyFuture(final EnergyFuture future) {
    return visit(future);
  }

  @Override
  public T visitEnergyFutureOption(final EnergyFutureOption option, final S data) {
    return visit(option, data);
  }

  @Override
  public T visitEnergyFutureOption(final EnergyFutureOption option) {
    return visit(option);
  }

  //  -----     Deprecated     -----

  @Override
  public T visitForexForward(final ForexForward fx, final S data) {
    return visit(fx, data);
  }

  @Override
  public T visitForexForward(final ForexForward fx) {
    return visit(fx);
  }

  @Override
  public T visitAnnuityCouponIborSpread(final AnnuityCouponIborSpread annuity, final S data) {
    return visit(annuity, data);
  }

  @Override
  public T visitAnnuityCouponIborSpread(final AnnuityCouponIborSpread annuity) {
    return visit(annuity);
  }

  @Override
  public T visitFloatingRateNote(final FloatingRateNote derivative, final S data) {
    return visit(derivative, data);
  }

  @Override
  public T visitFloatingRateNote(final FloatingRateNote derivative) {
    return visit(derivative);
  }

  @Override
  public T visitTenorSwap(final TenorSwap<? extends Payment> tenorSwap) {
    return visit(tenorSwap);
  }

  @Override
  public T visitForwardLiborAnnuity(final AnnuityCouponIbor forwardLiborAnnuity) {
    return visit(forwardLiborAnnuity);
  }

  @Override
  public T visitFixedFloatSwap(final FixedFloatSwap swap) {
    return visit(swap);
  }

  @Override
  public T visitCrossCurrencySwap(final CrossCurrencySwap ccs) {
    return visit(ccs);
  }

  @Override
  public T visitForwardLiborAnnuity(final AnnuityCouponIbor annuityCouponIbor, final S data) {
    return visit(annuityCouponIbor, data);
  }

  @Override
  public T visitFixedFloatSwap(final FixedFloatSwap swap, final S data) {
    return visit(swap, data);
  }

  @Override
  public T visitTenorSwap(final TenorSwap<? extends Payment> tenorSwap, final S data) {
    return visit(tenorSwap, data);
  }

  @Override
  public T visitCrossCurrencySwap(final CrossCurrencySwap ccs, final S data) {
    return visit(ccs, data);
  }

}
