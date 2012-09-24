/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument;

import java.util.Set;

import javax.time.calendar.LocalDate;

import com.opengamma.analytics.financial.forex.definition.ForexDefinition;
import com.opengamma.analytics.financial.forex.definition.ForexNonDeliverableForwardDefinition;
import com.opengamma.analytics.financial.forex.definition.ForexNonDeliverableOptionDefinition;
import com.opengamma.analytics.financial.forex.definition.ForexOptionDigitalDefinition;
import com.opengamma.analytics.financial.forex.definition.ForexOptionSingleBarrierDefinition;
import com.opengamma.analytics.financial.forex.definition.ForexOptionVanillaDefinition;
import com.opengamma.analytics.financial.forex.definition.ForexSwapDefinition;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityDefinition;
import com.opengamma.analytics.financial.instrument.bond.BillSecurityDefinition;
import com.opengamma.analytics.financial.instrument.bond.BillTransactionDefinition;
import com.opengamma.analytics.financial.instrument.bond.BondCapitalIndexedSecurityDefinition;
import com.opengamma.analytics.financial.instrument.bond.BondCapitalIndexedTransactionDefinition;
import com.opengamma.analytics.financial.instrument.bond.BondFixedSecurityDefinition;
import com.opengamma.analytics.financial.instrument.bond.BondFixedTransactionDefinition;
import com.opengamma.analytics.financial.instrument.bond.BondIborSecurityDefinition;
import com.opengamma.analytics.financial.instrument.bond.BondIborTransactionDefinition;
import com.opengamma.analytics.financial.instrument.cash.CashDefinition;
import com.opengamma.analytics.financial.instrument.cash.DepositCounterpartDefinition;
import com.opengamma.analytics.financial.instrument.cash.DepositIborDefinition;
import com.opengamma.analytics.financial.instrument.cash.DepositZeroDefinition;
import com.opengamma.analytics.financial.instrument.fra.ForwardRateAgreementDefinition;
import com.opengamma.analytics.financial.instrument.future.BondFutureDefinition;
import com.opengamma.analytics.financial.instrument.future.BondFutureOptionPremiumSecurityDefinition;
import com.opengamma.analytics.financial.instrument.future.BondFutureOptionPremiumTransactionDefinition;
import com.opengamma.analytics.financial.instrument.future.DeliverableSwapFuturesSecurityDefinition;
import com.opengamma.analytics.financial.instrument.future.FederalFundsFutureSecurityDefinition;
import com.opengamma.analytics.financial.instrument.future.FederalFundsFutureTransactionDefinition;
import com.opengamma.analytics.financial.instrument.future.InterestRateFutureDefinition;
import com.opengamma.analytics.financial.instrument.future.InterestRateFutureOptionMarginSecurityDefinition;
import com.opengamma.analytics.financial.instrument.future.InterestRateFutureOptionMarginTransactionDefinition;
import com.opengamma.analytics.financial.instrument.future.InterestRateFutureOptionPremiumSecurityDefinition;
import com.opengamma.analytics.financial.instrument.future.InterestRateFutureOptionPremiumTransactionDefinition;
import com.opengamma.analytics.financial.instrument.inflation.CouponInflationZeroCouponInterpolationDefinition;
import com.opengamma.analytics.financial.instrument.inflation.CouponInflationZeroCouponInterpolationGearingDefinition;
import com.opengamma.analytics.financial.instrument.inflation.CouponInflationZeroCouponMonthlyDefinition;
import com.opengamma.analytics.financial.instrument.inflation.CouponInflationZeroCouponMonthlyGearingDefinition;
import com.opengamma.analytics.financial.instrument.payment.CapFloorCMSDefinition;
import com.opengamma.analytics.financial.instrument.payment.CapFloorCMSSpreadDefinition;
import com.opengamma.analytics.financial.instrument.payment.CapFloorIborDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponCMSDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponFixedDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborCompoundedDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborGearingDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborRatchetDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborSpreadDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponOISDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponOISSimplifiedDefinition;
import com.opengamma.analytics.financial.instrument.payment.PaymentDefinition;
import com.opengamma.analytics.financial.instrument.payment.PaymentFixedDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedIborDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedIborSpreadDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapIborIborDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapXCcyIborIborDefinition;
import com.opengamma.analytics.financial.instrument.swaption.SwaptionBermudaFixedIborDefinition;
import com.opengamma.analytics.financial.instrument.swaption.SwaptionCashFixedIborDefinition;
import com.opengamma.analytics.financial.instrument.swaption.SwaptionPhysicalFixedIborDefinition;
import com.opengamma.analytics.financial.instrument.swaption.SwaptionPhysicalFixedIborSpreadDefinition;

/**
 * 
 */
public class CashFlowCalculator implements InstrumentDefinitionVisitor<Object, Set<LocalDate>> {

  @Override
  public Set<LocalDate> visit(final InstrumentDefinition<?> definition, final Object data) {
    return definition.accept(this, data);
  }

  @Override
  public Set<LocalDate> visit(final InstrumentDefinition<?> definition) {
    return null;
  }

  @Override
  public Set<LocalDate> visitBondFixedSecurityDefinition(final BondFixedSecurityDefinition bond, final Object data) {
    return null;
  }

  @Override
  public Set<LocalDate> visitBondFixedSecurityDefinition(final BondFixedSecurityDefinition bond) {
    return null;
  }

  @Override
  public Set<LocalDate> visitBondFixedTransactionDefinition(final BondFixedTransactionDefinition bond, final Object data) {
    return null;
  }

  @Override
  public Set<LocalDate> visitBondFixedTransactionDefinition(final BondFixedTransactionDefinition bond) {
    return null;
  }

  @Override
  public Set<LocalDate> visitBondFutureSecurityDefinition(final BondFutureDefinition bond, final Object data) {
    return null;
  }

  @Override
  public Set<LocalDate> visitBondFutureSecurityDefinition(final BondFutureDefinition bond) {
    return null;
  }

  @Override
  public Set<LocalDate> visitBondFutureOptionPremiumSecurityDefinition(final BondFutureOptionPremiumSecurityDefinition bond, final Object data) {
    return null;
  }

  @Override
  public Set<LocalDate> visitBondFutureOptionPremiumSecurityDefinition(final BondFutureOptionPremiumSecurityDefinition bond) {
    return null;
  }

  @Override
  public Set<LocalDate> visitBondFutureOptionPremiumTransactionDefinition(final BondFutureOptionPremiumTransactionDefinition bond, final Object data) {
    return null;
  }

  @Override
  public Set<LocalDate> visitBondFutureOptionPremiumTransactionDefinition(final BondFutureOptionPremiumTransactionDefinition bond) {
    return null;
  }

  @Override
  public Set<LocalDate> visitBondIborTransactionDefinition(final BondIborTransactionDefinition bond, final Object data) {
    return null;
  }

  @Override
  public Set<LocalDate> visitBondIborTransactionDefinition(final BondIborTransactionDefinition bond) {
    return null;
  }

  @Override
  public Set<LocalDate> visitBondIborSecurityDefinition(final BondIborSecurityDefinition bond, final Object data) {
    return null;
  }

  @Override
  public Set<LocalDate> visitBondIborSecurityDefinition(final BondIborSecurityDefinition bond) {
    return null;
  }

  @Override
  public Set<LocalDate> visitBillSecurityDefinition(final BillSecurityDefinition bill, final Object data) {
    return null;
  }

  @Override
  public Set<LocalDate> visitBillSecurityDefinition(final BillSecurityDefinition bill) {
    return null;
  }

  @Override
  public Set<LocalDate> visitBillTransactionDefinition(final BillTransactionDefinition bill, final Object data) {
    return null;
  }

  @Override
  public Set<LocalDate> visitBillTransactionDefinition(final BillTransactionDefinition bill) {
    return null;
  }

  @Override
  public Set<LocalDate> visitCashDefinition(final CashDefinition cash, final Object data) {
    return null;
  }

  @Override
  public Set<LocalDate> visitCashDefinition(final CashDefinition cash) {
    return null;
  }

  @Override
  public Set<LocalDate> visitDepositIborDefinition(final DepositIborDefinition deposit, final Object data) {
    return null;
  }

  @Override
  public Set<LocalDate> visitDepositIborDefinition(final DepositIborDefinition deposit) {
    return null;
  }

  @Override
  public Set<LocalDate> visitDepositCounterpartDefinition(final DepositCounterpartDefinition deposit, final Object data) {
    return null;
  }

  @Override
  public Set<LocalDate> visitDepositCounterpartDefinition(final DepositCounterpartDefinition deposit) {
    return null;
  }

  @Override
  public Set<LocalDate> visitDepositZeroDefinition(final DepositZeroDefinition deposit, final Object data) {
    return null;
  }

  @Override
  public Set<LocalDate> visitDepositZeroDefinition(final DepositZeroDefinition deposit) {
    return null;
  }

  @Override
  public Set<LocalDate> visitForwardRateAgreementDefinition(final ForwardRateAgreementDefinition fra, final Object data) {
    return null;
  }

  @Override
  public Set<LocalDate> visitForwardRateAgreementDefinition(final ForwardRateAgreementDefinition fra) {
    return null;
  }

  @Override
  public Set<LocalDate> visitInterestRateFutureSecurityDefinition(final InterestRateFutureDefinition future, final Object data) {
    return null;
  }

  @Override
  public Set<LocalDate> visitInterestRateFutureSecurityDefinition(final InterestRateFutureDefinition future) {
    return null;
  }

  @Override
  public Set<LocalDate> visitFederalFundsFutureSecurityDefinition(final FederalFundsFutureSecurityDefinition future, final Object data) {
    return null;
  }

  @Override
  public Set<LocalDate> visitFederalFundsFutureSecurityDefinition(final FederalFundsFutureSecurityDefinition future) {
    return null;
  }

  @Override
  public Set<LocalDate> visitFederalFundsFutureTransactionDefinition(final FederalFundsFutureTransactionDefinition future, final Object data) {
    return null;
  }

  @Override
  public Set<LocalDate> visitFederalFundsFutureTransactionDefinition(final FederalFundsFutureTransactionDefinition future) {
    return null;
  }

  @Override
  public Set<LocalDate> visitInterestRateFutureOptionPremiumSecurityDefinition(final InterestRateFutureOptionPremiumSecurityDefinition future, final Object data) {
    return null;
  }

  @Override
  public Set<LocalDate> visitInterestRateFutureOptionPremiumSecurityDefinition(final InterestRateFutureOptionPremiumSecurityDefinition future) {
    return null;
  }

  @Override
  public Set<LocalDate> visitInterestRateFutureOptionPremiumTransactionDefinition(final InterestRateFutureOptionPremiumTransactionDefinition future, final Object data) {
    return null;
  }

  @Override
  public Set<LocalDate> visitInterestRateFutureOptionPremiumTransactionDefinition(final InterestRateFutureOptionPremiumTransactionDefinition future) {
    return null;
  }

  @Override
  public Set<LocalDate> visitInterestRateFutureOptionMarginSecurityDefinition(final InterestRateFutureOptionMarginSecurityDefinition future, final Object data) {
    return null;
  }

  @Override
  public Set<LocalDate> visitInterestRateFutureOptionMarginSecurityDefinition(final InterestRateFutureOptionMarginSecurityDefinition future) {
    return null;
  }

  @Override
  public Set<LocalDate> visitInterestRateFutureOptionMarginTransactionDefinition(final InterestRateFutureOptionMarginTransactionDefinition future, final Object data) {
    return null;
  }

  @Override
  public Set<LocalDate> visitInterestRateFutureOptionMarginTransactionDefinition(final InterestRateFutureOptionMarginTransactionDefinition future) {
    return null;
  }

  @Override
  public Set<LocalDate> visitCouponFixed(final CouponFixedDefinition payment, final Object data) {
    return null;
  }

  @Override
  public Set<LocalDate> visitCouponFixed(final CouponFixedDefinition payment) {
    return null;
  }

  @Override
  public Set<LocalDate> visitCouponIbor(final CouponIborDefinition payment, final Object data) {
    return null;
  }

  @Override
  public Set<LocalDate> visitCouponIbor(final CouponIborDefinition payment) {
    return null;
  }

  @Override
  public Set<LocalDate> visitCouponIborSpread(final CouponIborSpreadDefinition payment, final Object data) {
    return null;
  }

  @Override
  public Set<LocalDate> visitCouponIborSpread(final CouponIborSpreadDefinition payment) {
    return null;
  }

  @Override
  public Set<LocalDate> visitCouponIborGearing(final CouponIborGearingDefinition payment, final Object data) {
    return null;
  }

  @Override
  public Set<LocalDate> visitCouponIborGearing(final CouponIborGearingDefinition payment) {
    return null;
  }

  @Override
  public Set<LocalDate> visitCouponIborCompounded(final CouponIborCompoundedDefinition payment, final Object data) {
    return null;
  }

  @Override
  public Set<LocalDate> visitCouponIborCompounded(final CouponIborCompoundedDefinition payment) {
    return null;
  }

  @Override
  public Set<LocalDate> visitCouponIborRatchet(final CouponIborRatchetDefinition payment, final Object data) {
    return null;
  }

  @Override
  public Set<LocalDate> visitCouponIborRatchet(final CouponIborRatchetDefinition payment) {
    return null;
  }

  @Override
  public Set<LocalDate> visitCapFloorIbor(final CapFloorIborDefinition payment, final Object data) {
    return null;
  }

  @Override
  public Set<LocalDate> visitCapFloorIbor(final CapFloorIborDefinition payment) {
    return null;
  }

  @Override
  public Set<LocalDate> visitCouponOISSimplified(final CouponOISSimplifiedDefinition payment, final Object data) {
    return null;
  }

  @Override
  public Set<LocalDate> visitCouponOISSimplified(final CouponOISSimplifiedDefinition payment) {
    return null;
  }

  @Override
  public Set<LocalDate> visitCouponOIS(final CouponOISDefinition payment, final Object data) {
    return null;
  }

  @Override
  public Set<LocalDate> visitCouponOIS(final CouponOISDefinition payment) {
    return null;
  }

  @Override
  public Set<LocalDate> visitCouponCMS(final CouponCMSDefinition payment, final Object data) {
    return null;
  }

  @Override
  public Set<LocalDate> visitCouponCMS(final CouponCMSDefinition payment) {
    return null;
  }

  @Override
  public Set<LocalDate> visitCapFloorCMS(final CapFloorCMSDefinition payment, final Object data) {
    return null;
  }

  @Override
  public Set<LocalDate> visitCapFloorCMS(final CapFloorCMSDefinition payment) {
    return null;
  }

  @Override
  public Set<LocalDate> visitCapFloorCMSSpread(final CapFloorCMSSpreadDefinition payment, final Object data) {
    return null;
  }

  @Override
  public Set<LocalDate> visitCapFloorCMSSpread(final CapFloorCMSSpreadDefinition payment) {
    return null;
  }

  @Override
  public Set<LocalDate> visitAnnuityDefinition(final AnnuityDefinition<? extends PaymentDefinition> annuity, final Object data) {
    return null;
  }

  @Override
  public Set<LocalDate> visitAnnuityDefinition(final AnnuityDefinition<? extends PaymentDefinition> annuity) {
    return null;
  }

  @Override
  public Set<LocalDate> visitSwapDefinition(final SwapDefinition swap, final Object data) {
    return null;
  }

  @Override
  public Set<LocalDate> visitSwapDefinition(final SwapDefinition swap) {
    return null;
  }

  @Override
  public Set<LocalDate> visitSwapFixedIborDefinition(final SwapFixedIborDefinition swap, final Object data) {
    return null;
  }

  @Override
  public Set<LocalDate> visitSwapFixedIborDefinition(final SwapFixedIborDefinition swap) {
    return null;
  }

  @Override
  public Set<LocalDate> visitSwapFixedIborSpreadDefinition(final SwapFixedIborSpreadDefinition swap, final Object data) {
    return null;
  }

  @Override
  public Set<LocalDate> visitSwapFixedIborSpreadDefinition(final SwapFixedIborSpreadDefinition swap) {
    return null;
  }

  @Override
  public Set<LocalDate> visitSwapIborIborDefinition(final SwapIborIborDefinition swap, final Object data) {
    return null;
  }

  @Override
  public Set<LocalDate> visitSwapIborIborDefinition(final SwapIborIborDefinition swap) {
    return null;
  }

  @Override
  public Set<LocalDate> visitSwapXCcyIborIborDefinition(final SwapXCcyIborIborDefinition swap, final Object data) {
    return null;
  }

  @Override
  public Set<LocalDate> visitSwapXCcyIborIborDefinition(final SwapXCcyIborIborDefinition swap) {
    return null;
  }

  @Override
  public Set<LocalDate> visitSwaptionCashFixedIborDefinition(final SwaptionCashFixedIborDefinition swaption, final Object data) {
    return null;
  }

  @Override
  public Set<LocalDate> visitSwaptionCashFixedIborDefinition(final SwaptionCashFixedIborDefinition swaption) {
    return null;
  }

  @Override
  public Set<LocalDate> visitSwaptionPhysicalFixedIborDefinition(final SwaptionPhysicalFixedIborDefinition swaption, final Object data) {
    return null;
  }

  @Override
  public Set<LocalDate> visitSwaptionPhysicalFixedIborDefinition(final SwaptionPhysicalFixedIborDefinition swaption) {
    return null;
  }

  @Override
  public Set<LocalDate> visitSwaptionPhysicalFixedIborSpreadDefinition(final SwaptionPhysicalFixedIborSpreadDefinition swaption, final Object data) {
    return null;
  }

  @Override
  public Set<LocalDate> visitSwaptionPhysicalFixedIborSpreadDefinition(final SwaptionPhysicalFixedIborSpreadDefinition swaption) {
    return null;
  }

  @Override
  public Set<LocalDate> visitSwaptionBermudaFixedIborDefinition(final SwaptionBermudaFixedIborDefinition swaption, final Object data) {
    return null;
  }

  @Override
  public Set<LocalDate> visitSwaptionBermudaFixedIborDefinition(final SwaptionBermudaFixedIborDefinition swaption) {
    return null;
  }

  @Override
  public Set<LocalDate> visitCouponInflationZeroCouponFirstOfMonth(final CouponInflationZeroCouponMonthlyDefinition coupon, final Object data) {
    return null;
  }

  @Override
  public Set<LocalDate> visitCouponInflationZeroCouponFirstOfMonth(final CouponInflationZeroCouponMonthlyDefinition coupon) {
    return null;
  }

  @Override
  public Set<LocalDate> visitCouponInflationZeroCouponInterpolation(final CouponInflationZeroCouponInterpolationDefinition coupon, final Object data) {
    return null;
  }

  @Override
  public Set<LocalDate> visitCouponInflationZeroCouponInterpolation(final CouponInflationZeroCouponInterpolationDefinition coupon) {
    return null;
  }

  @Override
  public Set<LocalDate> visitCouponInflationZeroCouponMonthlyGearing(final CouponInflationZeroCouponMonthlyGearingDefinition coupon, final Object data) {
    return null;
  }

  @Override
  public Set<LocalDate> visitCouponInflationZeroCouponMonthlyGearing(final CouponInflationZeroCouponMonthlyGearingDefinition coupon) {
    return null;
  }

  @Override
  public Set<LocalDate> visitCouponInflationZeroCouponInterpolationGearing(final CouponInflationZeroCouponInterpolationGearingDefinition coupon, final Object data) {
    return null;
  }

  @Override
  public Set<LocalDate> visitCouponInflationZeroCouponInterpolationGearing(final CouponInflationZeroCouponInterpolationGearingDefinition coupon) {
    return null;
  }

  @Override
  public Set<LocalDate> visitBondCapitalIndexedSecurity(final BondCapitalIndexedSecurityDefinition<?> bond, final Object data) {
    return null;
  }

  @Override
  public Set<LocalDate> visitBondCapitalIndexedSecurity(final BondCapitalIndexedSecurityDefinition<?> bond) {
    return null;
  }

  @Override
  public Set<LocalDate> visitBondCapitalIndexedTransaction(final BondCapitalIndexedTransactionDefinition<?> bond, final Object data) {
    return null;
  }

  @Override
  public Set<LocalDate> visitBondCapitalIndexedTransaction(final BondCapitalIndexedTransactionDefinition<?> bond) {
    return null;
  }

  @Override
  public Set<LocalDate> visitForexDefinition(final ForexDefinition fx, final Object data) {
    return null;
  }

  @Override
  public Set<LocalDate> visitForexDefinition(final ForexDefinition fx) {
    return null;
  }

  @Override
  public Set<LocalDate> visitForexSwapDefinition(final ForexSwapDefinition fx, final Object data) {
    return null;
  }

  @Override
  public Set<LocalDate> visitForexSwapDefinition(final ForexSwapDefinition fx) {
    return null;
  }

  @Override
  public Set<LocalDate> visitForexOptionVanillaDefinition(final ForexOptionVanillaDefinition fx, final Object data) {
    return null;
  }

  @Override
  public Set<LocalDate> visitForexOptionVanillaDefinition(final ForexOptionVanillaDefinition fx) {
    return null;
  }

  @Override
  public Set<LocalDate> visitForexOptionSingleBarrierDefiniton(final ForexOptionSingleBarrierDefinition fx, final Object data) {
    return null;
  }

  @Override
  public Set<LocalDate> visitForexOptionSingleBarrierDefiniton(final ForexOptionSingleBarrierDefinition fx) {
    return null;
  }

  @Override
  public Set<LocalDate> visitForexNonDeliverableForwardDefinition(final ForexNonDeliverableForwardDefinition ndf, final Object data) {
    return null;
  }

  @Override
  public Set<LocalDate> visitForexNonDeliverableForwardDefinition(final ForexNonDeliverableForwardDefinition ndf) {
    return null;
  }

  @Override
  public Set<LocalDate> visitForexNonDeliverableOptionDefinition(final ForexNonDeliverableOptionDefinition ndo, final Object data) {
    return null;
  }

  @Override
  public Set<LocalDate> visitForexNonDeliverableOptionDefinition(final ForexNonDeliverableOptionDefinition ndo) {
    return null;
  }

  @Override
  public Set<LocalDate> visitForexOptionDigitalDefinition(final ForexOptionDigitalDefinition fx, final Object data) {
    return null;
  }

  @Override
  public Set<LocalDate> visitForexOptionDigitalDefinition(final ForexOptionDigitalDefinition fx) {
    return null;
  }

  @Override
  public Set<LocalDate> visitDeliverableSwapFuturesSecurityDefinition(final DeliverableSwapFuturesSecurityDefinition futures, final Object data) {
    return null;
  }

  @Override
  public Set<LocalDate> visitDeliverableSwapFuturesSecurityDefinition(final DeliverableSwapFuturesSecurityDefinition futures) {
    return null;
  }

  @Override
  public Set<LocalDate> visitPaymentFixedDefinition(final PaymentFixedDefinition payment, final Object data) {
    return null;
  }

  @Override
  public Set<LocalDate> visitPaymentFixedDefinition(final PaymentFixedDefinition payment) {
    return null;
  }

}
