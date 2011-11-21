/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.instrument;

import com.opengamma.financial.forex.definition.ForexDefinition;
import com.opengamma.financial.forex.definition.ForexNonDeliverableForwardDefinition;
import com.opengamma.financial.forex.definition.ForexNonDeliverableOptionDefinition;
import com.opengamma.financial.forex.definition.ForexOptionSingleBarrierDefinition;
import com.opengamma.financial.forex.definition.ForexOptionVanillaDefinition;
import com.opengamma.financial.forex.definition.ForexSwapDefinition;
import com.opengamma.financial.instrument.annuity.AnnuityDefinition;
import com.opengamma.financial.instrument.bond.BondCapitalIndexedSecurityDefinition;
import com.opengamma.financial.instrument.bond.BondCapitalIndexedTransactionDefinition;
import com.opengamma.financial.instrument.bond.BondFixedSecurityDefinition;
import com.opengamma.financial.instrument.bond.BondFixedTransactionDefinition;
import com.opengamma.financial.instrument.bond.BondIborSecurityDefinition;
import com.opengamma.financial.instrument.bond.BondIborTransactionDefinition;
import com.opengamma.financial.instrument.cash.CashDefinition;
import com.opengamma.financial.instrument.fra.ForwardRateAgreementDefinition;
import com.opengamma.financial.instrument.future.BondFutureDefinition;
import com.opengamma.financial.instrument.future.InterestRateFutureDefinition;
import com.opengamma.financial.instrument.future.InterestRateFutureOptionMarginSecurityDefinition;
import com.opengamma.financial.instrument.future.InterestRateFutureOptionMarginTransactionDefinition;
import com.opengamma.financial.instrument.future.InterestRateFutureOptionPremiumSecurityDefinition;
import com.opengamma.financial.instrument.future.InterestRateFutureOptionPremiumTransactionDefinition;
import com.opengamma.financial.instrument.inflation.CouponInflationZeroCouponInterpolationDefinition;
import com.opengamma.financial.instrument.inflation.CouponInflationZeroCouponInterpolationGearingDefinition;
import com.opengamma.financial.instrument.inflation.CouponInflationZeroCouponMonthlyDefinition;
import com.opengamma.financial.instrument.inflation.CouponInflationZeroCouponMonthlyGearingDefinition;
import com.opengamma.financial.instrument.payment.CapFloorCMSDefinition;
import com.opengamma.financial.instrument.payment.CouponCMSDefinition;
import com.opengamma.financial.instrument.payment.CouponFixedDefinition;
import com.opengamma.financial.instrument.payment.CouponFloatingDefinition;
import com.opengamma.financial.instrument.payment.CouponIborDefinition;
import com.opengamma.financial.instrument.payment.CouponOISDefinition;
import com.opengamma.financial.instrument.payment.CouponOISSimplifiedDefinition;
import com.opengamma.financial.instrument.payment.PaymentDefinition;
import com.opengamma.financial.instrument.payment.PaymentFixedDefinition;
import com.opengamma.financial.instrument.swap.SwapDefinition;
import com.opengamma.financial.instrument.swap.SwapFixedIborDefinition;
import com.opengamma.financial.instrument.swap.SwapFixedIborSpreadDefinition;
import com.opengamma.financial.instrument.swap.SwapIborIborDefinition;
import com.opengamma.financial.instrument.swaption.SwaptionBermudaFixedIborDefinition;
import com.opengamma.financial.instrument.swaption.SwaptionCashFixedIborDefinition;
import com.opengamma.financial.instrument.swaption.SwaptionPhysicalFixedIborDefinition;

/**
 * 
 * @param <T> Type of the data 
 * @param <U> Type of the result
 */
public interface InstrumentDefinitionVisitor<T, U> {

  U visit(InstrumentDefinition<?> definition, T data);

  U visit(InstrumentDefinition<?> definition);

  U visitBondFixedSecurityDefinition(BondFixedSecurityDefinition bond, T data);

  U visitBondFixedSecurityDefinition(BondFixedSecurityDefinition bond);

  U visitBondFixedTransactionDefinition(BondFixedTransactionDefinition bond, T data);

  U visitBondFixedTransactionDefinition(BondFixedTransactionDefinition bond);

  U visitBondFutureSecurityDefinition(BondFutureDefinition bond, T data);

  U visitBondFutureSecurityDefinition(BondFutureDefinition bond);

  U visitBondIborTransactionDefinition(BondIborTransactionDefinition bond, T data);

  U visitBondIborTransactionDefinition(BondIborTransactionDefinition bond);

  U visitBondIborSecurityDefinition(BondIborSecurityDefinition bond, T data);

  U visitBondIborSecurityDefinition(BondIborSecurityDefinition bond);

  U visitCashDefinition(CashDefinition cash, T data);

  U visitCashDefinition(CashDefinition cash);

  U visitForwardRateAgreementDefinition(ForwardRateAgreementDefinition fra, T data);

  U visitForwardRateAgreementDefinition(ForwardRateAgreementDefinition fra);

  U visitInterestRateFutureSecurityDefinition(InterestRateFutureDefinition future, T data);

  U visitInterestRateFutureSecurityDefinition(InterestRateFutureDefinition future);

  U visitInterestRateFutureOptionPremiumSecurityDefinition(InterestRateFutureOptionPremiumSecurityDefinition future, T data);

  U visitInterestRateFutureOptionPremiumSecurityDefinition(InterestRateFutureOptionPremiumSecurityDefinition future);

  U visitInterestRateFutureOptionPremiumTransactionDefinition(InterestRateFutureOptionPremiumTransactionDefinition future, T data);

  U visitInterestRateFutureOptionPremiumTransactionDefinition(InterestRateFutureOptionPremiumTransactionDefinition future);

  U visitInterestRateFutureOptionMarginSecurityDefinition(InterestRateFutureOptionMarginSecurityDefinition future, T data);

  U visitInterestRateFutureOptionMarginSecurityDefinition(InterestRateFutureOptionMarginSecurityDefinition future);

  U visitInterestRateFutureOptionMarginTransactionDefinition(InterestRateFutureOptionMarginTransactionDefinition future, T data);

  U visitInterestRateFutureOptionMarginTransactionDefinition(InterestRateFutureOptionMarginTransactionDefinition future);

  U visitPaymentFixed(PaymentFixedDefinition payment, T data);

  U visitPaymentFixed(PaymentFixedDefinition payment);

  U visitCouponFixed(CouponFixedDefinition payment, T data);

  U visitCouponFixed(CouponFixedDefinition payment);

  U visitCouponFloating(CouponFloatingDefinition payment, T data);

  U visitCouponFloating(CouponFloatingDefinition payment);

  U visitCouponIbor(CouponIborDefinition payment, T data);

  U visitCouponIbor(CouponIborDefinition payment);

  U visitCouponIborSpread(CouponIborDefinition payment, T data);

  U visitCouponIborSpread(CouponIborDefinition payment);

  U visitCouponOISSimplified(CouponOISSimplifiedDefinition payment, T data);

  U visitCouponOISSimplified(CouponOISSimplifiedDefinition payment);

  U visitCouponOIS(CouponOISDefinition payment, T data);

  U visitCouponOIS(CouponOISDefinition payment);

  U visitCouponCMS(CouponCMSDefinition payment, T data);

  U visitCouponCMS(CouponCMSDefinition payment);

  U visitCapFloorCMS(CapFloorCMSDefinition payment, T data);

  U visitCapFloorCMS(CapFloorCMSDefinition payment);

  U visitAnnuityDefinition(AnnuityDefinition<? extends PaymentDefinition> annuity, T data);

  U visitAnnuityDefinition(AnnuityDefinition<? extends PaymentDefinition> annuity);

  U visitSwapDefinition(SwapDefinition swap, T data);

  U visitSwapDefinition(SwapDefinition swap);

  U visitSwapFixedIborDefinition(SwapFixedIborDefinition swap, T data);

  U visitSwapFixedIborDefinition(SwapFixedIborDefinition swap);

  U visitSwapFixedIborSpreadDefinition(SwapFixedIborSpreadDefinition swap, T data);

  U visitSwapFixedIborSpreadDefinition(SwapFixedIborSpreadDefinition swap);

  U visitSwapIborIborDefinition(SwapIborIborDefinition swap, T data);

  U visitSwapIborIborDefinition(SwapIborIborDefinition swap);

  U visitSwaptionCashFixedIborDefinition(SwaptionCashFixedIborDefinition swaption, T data);

  U visitSwaptionCashFixedIborDefinition(SwaptionCashFixedIborDefinition swaption);

  U visitSwaptionPhysicalFixedIborDefinition(SwaptionPhysicalFixedIborDefinition swaption, T data);

  U visitSwaptionPhysicalFixedIborDefinition(SwaptionPhysicalFixedIborDefinition swaption);

  U visitSwaptionBermudaFixedIborDefinition(SwaptionBermudaFixedIborDefinition swaption, T data);

  U visitSwaptionBermudaFixedIborDefinition(SwaptionBermudaFixedIborDefinition swaption);

  U visitCouponInflationZeroCouponFirstOfMonth(CouponInflationZeroCouponMonthlyDefinition coupon, T data);

  U visitCouponInflationZeroCouponFirstOfMonth(CouponInflationZeroCouponMonthlyDefinition coupon);

  U visitCouponInflationZeroCouponInterpolation(CouponInflationZeroCouponInterpolationDefinition coupon, T data);

  U visitCouponInflationZeroCouponInterpolation(CouponInflationZeroCouponInterpolationDefinition coupon);

  U visitCouponInflationZeroCouponMonthlyGearing(CouponInflationZeroCouponMonthlyGearingDefinition coupon, T data);

  U visitCouponInflationZeroCouponMonthlyGearing(CouponInflationZeroCouponMonthlyGearingDefinition coupon);

  U visitCouponInflationZeroCouponInterpolationGearing(CouponInflationZeroCouponInterpolationGearingDefinition coupon, T data);

  U visitCouponInflationZeroCouponInterpolationGearing(CouponInflationZeroCouponInterpolationGearingDefinition coupon);

  U visitBondCapitalIndexedSecurity(BondCapitalIndexedSecurityDefinition<?> bond, T data);

  U visitBondCapitalIndexedSecurity(BondCapitalIndexedSecurityDefinition<?> bond);

  U visitBondCapitalIndexedTransaction(BondCapitalIndexedTransactionDefinition<?> bond, T data);

  U visitBondCapitalIndexedTransaction(BondCapitalIndexedTransactionDefinition<?> bond);

  // -----     Forex     -----

  U visitForexDefinition(ForexDefinition fx, T data);

  U visitForexDefinition(ForexDefinition fx);

  U visitForexSwapDefinition(ForexSwapDefinition fx, T data);

  U visitForexSwapDefinition(ForexSwapDefinition fx);

  U visitForexOptionVanillaDefinition(ForexOptionVanillaDefinition fx, T data);

  U visitForexOptionVanillaDefinition(ForexOptionVanillaDefinition fx);

  U visitForexOptionSingleBarrierDefiniton(ForexOptionSingleBarrierDefinition fx, T data);

  U visitForexOptionSingleBarrierDefiniton(ForexOptionSingleBarrierDefinition fx);

  U visitForexNonDeliverableForwardDefinition(ForexNonDeliverableForwardDefinition ndf, T data);

  U visitForexNonDeliverableForwardDefinition(ForexNonDeliverableForwardDefinition ndf);

  U visitForexNonDeliverableOptionDefinition(ForexNonDeliverableOptionDefinition ndo, T data);

  U visitForexNonDeliverableOptionDefinition(ForexNonDeliverableOptionDefinition ndo);

}
