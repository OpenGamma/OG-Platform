/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate;

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
import com.opengamma.analytics.financial.interestrate.annuity.derivative.AnnuityCouponIborRatchet;
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
import com.opengamma.analytics.financial.interestrate.fra.derivative.ForwardRateAgreement;
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
import com.opengamma.analytics.financial.interestrate.swap.derivative.Swap;
import com.opengamma.analytics.financial.interestrate.swap.derivative.SwapFixedCoupon;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionBermudaFixedIbor;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionCashFixedIbor;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionPhysicalFixedIbor;

/**
 * 
 * @param <S> The type of the data
 * @param <T> The return type of the calculation
 */
public interface InstrumentDerivativeVisitor<S, T> {

  // Two arguments

  T visit(InstrumentDerivative derivative, S data);

  T[] visit(InstrumentDerivative[] derivative, S data);

  T visitBondFixedSecurity(BondFixedSecurity bond, S data);

  T visitBondFixedTransaction(BondFixedTransaction bond, S data);

  T visitBondIborSecurity(BondIborSecurity bond, S data);

  T visitBondIborTransaction(BondIborTransaction bond, S data);

  T visitBillSecurity(BillSecurity bill, S data);

  T visitBillTransaction(BillTransaction bill, S data);

  T visitGenericAnnuity(Annuity<? extends Payment> genericAnnuity, S data);

  T visitFixedCouponAnnuity(AnnuityCouponFixed fixedCouponAnnuity, S data);

  T visitAnnuityCouponIborRatchet(AnnuityCouponIborRatchet annuity, S data);

  T visitFixedCouponSwap(SwapFixedCoupon<?> swap, S data);

  T visitSwaptionCashFixedIbor(SwaptionCashFixedIbor swaption, S data);

  T visitSwaptionPhysicalFixedIbor(SwaptionPhysicalFixedIbor swaption, S data);

  T visitSwaptionBermudaFixedIbor(SwaptionBermudaFixedIbor swaption, S data);

  T visitCash(Cash cash, S data);

  T visitFixedPayment(PaymentFixed payment, S data);

  T visitCouponCMS(CouponCMS payment, S data);

  T visitCapFloorIbor(CapFloorIbor payment, S data);

  T visitCapFloorCMS(CapFloorCMS payment, S data);

  T visitCapFloorCMSSpread(CapFloorCMSSpread payment, S data);

  T visitForwardRateAgreement(ForwardRateAgreement fra, S data);

  T visitBondCapitalIndexedSecurity(BondCapitalIndexedSecurity<?> bond, S data);

  T visitBondCapitalIndexedTransaction(BondCapitalIndexedTransaction<?> bond, S data);

  T visitCDSDerivative(ISDACDSDerivative cds, S data);

  // One argument

  T visit(InstrumentDerivative derivative);

  T[] visit(InstrumentDerivative[] derivative);

  T visitBondFixedSecurity(BondFixedSecurity bond);

  T visitBondFixedTransaction(BondFixedTransaction bond);

  T visitBondIborSecurity(BondIborSecurity bond);

  T visitBondIborTransaction(BondIborTransaction bond);

  T visitBillSecurity(BillSecurity bill);

  T visitBillTransaction(BillTransaction bill);

  T visitGenericAnnuity(Annuity<? extends Payment> genericAnnuity);

  T visitFixedCouponAnnuity(AnnuityCouponFixed fixedCouponAnnuity);

  T visitAnnuityCouponIborRatchet(AnnuityCouponIborRatchet annuity);

  T visitFixedCouponSwap(SwapFixedCoupon<?> swap);

  T visitSwaptionCashFixedIbor(SwaptionCashFixedIbor swaption);

  T visitSwaptionPhysicalFixedIbor(SwaptionPhysicalFixedIbor swaption);

  T visitSwaptionBermudaFixedIbor(SwaptionBermudaFixedIbor swaption);

  T visitCash(Cash cash);

  T visitFixedPayment(PaymentFixed payment);

  T visitCouponCMS(CouponCMS payment);

  T visitCapFloorIbor(CapFloorIbor payment);

  T visitCapFloorCMS(CapFloorCMS payment);

  T visitCapFloorCMSSpread(CapFloorCMSSpread payment);

  T visitForwardRateAgreement(ForwardRateAgreement fra);

  T visitBondCapitalIndexedSecurity(BondCapitalIndexedSecurity<?> bond);

  T visitBondCapitalIndexedTransaction(BondCapitalIndexedTransaction<?> bond);

  T visitCDSDerivative(ISDACDSDerivative cds);

  // -----     Coupons     -----

  T visitCouponFixed(CouponFixed payment, S data);

  T visitCouponFixed(CouponFixed payment);

  T visitCouponIbor(CouponIbor payment, S data);

  T visitCouponIbor(CouponIbor payment);

  T visitCouponIborSpread(CouponIborSpread payment, S data);

  T visitCouponIborSpread(CouponIborSpread payment);

  T visitCouponIborGearing(CouponIborGearing payment);

  T visitCouponIborGearing(CouponIborGearing payment, S data);

  T visitCouponIborCompounded(CouponIborCompounded payment);

  T visitCouponIborCompounded(CouponIborCompounded payment, S data);

  T visitCouponOIS(CouponOIS payment, S data);

  T visitCouponOIS(CouponOIS payment);

  // -----     Annuity     -----

  // -----     Swap     -----

  T visitSwap(Swap<?, ?> swap, S data);

  T visitSwap(Swap<?, ?> swap);

  // -----     Inflation     -----

  T visitCouponInflationZeroCouponMonthly(CouponInflationZeroCouponMonthly coupon, S data);

  T visitCouponInflationZeroCouponMonthly(CouponInflationZeroCouponMonthly coupon);

  T visitCouponInflationZeroCouponMonthlyGearing(CouponInflationZeroCouponMonthlyGearing coupon, S data);

  T visitCouponInflationZeroCouponMonthlyGearing(CouponInflationZeroCouponMonthlyGearing coupon);

  T visitCouponInflationZeroCouponInterpolation(CouponInflationZeroCouponInterpolation coupon, S data);

  T visitCouponInflationZeroCouponInterpolation(CouponInflationZeroCouponInterpolation coupon);

  T visitCouponInflationZeroCouponInterpolationGearing(CouponInflationZeroCouponInterpolationGearing coupon, S data);

  T visitCouponInflationZeroCouponInterpolationGearing(CouponInflationZeroCouponInterpolationGearing coupon);

  // -----     Futures   -----

  T visitBondFuture(BondFuture bondFuture, S data);

  T visitBondFuture(BondFuture future);

  T visitInterestRateFuture(InterestRateFuture future, S data);

  T visitInterestRateFuture(InterestRateFuture future);

  T visitFederalFundsFutureSecurity(FederalFundsFutureSecurity future, S data);

  T visitFederalFundsFutureSecurity(FederalFundsFutureSecurity future);

  T visitFederalFundsFutureTransaction(FederalFundsFutureTransaction future, S data);

  T visitFederalFundsFutureTransaction(FederalFundsFutureTransaction future);

  T visitDeliverableSwapFuturesSecurity(DeliverableSwapFuturesSecurity futures, S data);

  T visitDeliverableSwapFuturesSecurity(DeliverableSwapFuturesSecurity futures);

  // -----     Futures options   -----

  T visitBondFutureOptionPremiumSecurity(BondFutureOptionPremiumSecurity option, S data);

  T visitBondFutureOptionPremiumSecurity(BondFutureOptionPremiumSecurity option);

  T visitBondFutureOptionPremiumTransaction(BondFutureOptionPremiumTransaction option, S data);

  T visitBondFutureOptionPremiumTransaction(BondFutureOptionPremiumTransaction option);

  T visitInterestRateFutureOptionMarginSecurity(InterestRateFutureOptionMarginSecurity option, S data);

  T visitInterestRateFutureOptionMarginSecurity(InterestRateFutureOptionMarginSecurity option);

  T visitInterestRateFutureOptionMarginTransaction(InterestRateFutureOptionMarginTransaction option, S data);

  T visitInterestRateFutureOptionMarginTransaction(InterestRateFutureOptionMarginTransaction option);

  T visitInterestRateFutureOptionPremiumSecurity(InterestRateFutureOptionPremiumSecurity option, S data);

  T visitInterestRateFutureOptionPremiumSecurity(InterestRateFutureOptionPremiumSecurity option);

  T visitInterestRateFutureOptionPremiumTransaction(InterestRateFutureOptionPremiumTransaction option, S data);

  T visitInterestRateFutureOptionPremiumTransaction(InterestRateFutureOptionPremiumTransaction option);

  // -----     Deposit     -----

  T visitDepositIbor(DepositIbor deposit, S data);

  T visitDepositIbor(DepositIbor deposit);

  T visitDepositCounterpart(DepositCounterpart deposit, S data);

  T visitDepositCounterpart(DepositCounterpart deposit);

  T visitDepositZero(DepositZero deposit, S data);

  T visitDepositZero(DepositZero deposit);

  // -----     Forex     -----

  T visitForex(Forex derivative, S data);

  T visitForex(Forex derivative);

  T visitForexSwap(ForexSwap derivative, S data);

  T visitForexSwap(ForexSwap derivative);

  T visitForexOptionVanilla(ForexOptionVanilla derivative, S data);

  T visitForexOptionVanilla(ForexOptionVanilla derivative);

  T visitForexOptionSingleBarrier(ForexOptionSingleBarrier derivative, S data);

  T visitForexOptionSingleBarrier(ForexOptionSingleBarrier derivative);

  T visitForexNonDeliverableForward(ForexNonDeliverableForward derivative, S data);

  T visitForexNonDeliverableForward(ForexNonDeliverableForward derivative);

  T visitForexNonDeliverableOption(ForexNonDeliverableOption derivative, S data);

  T visitForexNonDeliverableOption(ForexNonDeliverableOption derivative);

  T visitForexOptionDigital(ForexOptionDigital derivative, S data);

  T visitForexOptionDigital(ForexOptionDigital derivative);

}
