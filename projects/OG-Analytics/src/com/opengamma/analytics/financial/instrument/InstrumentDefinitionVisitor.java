/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument;

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
 * @param <DATA_TYPE> Type of the data 
 * @param <RESULT_TYPE> Type of the result
 */
public interface InstrumentDefinitionVisitor<DATA_TYPE, RESULT_TYPE> {

  RESULT_TYPE visit(InstrumentDefinition<?> definition, DATA_TYPE data);

  RESULT_TYPE visit(InstrumentDefinition<?> definition);

  // -----     Bond and bill     -----

  RESULT_TYPE visitBondFixedSecurityDefinition(BondFixedSecurityDefinition bond, DATA_TYPE data);

  RESULT_TYPE visitBondFixedSecurityDefinition(BondFixedSecurityDefinition bond);

  RESULT_TYPE visitBondFixedTransactionDefinition(BondFixedTransactionDefinition bond, DATA_TYPE data);

  RESULT_TYPE visitBondFixedTransactionDefinition(BondFixedTransactionDefinition bond);

  RESULT_TYPE visitBondFutureSecurityDefinition(BondFutureDefinition bond, DATA_TYPE data);

  RESULT_TYPE visitBondFutureOptionPremiumSecurityDefinition(BondFutureOptionPremiumSecurityDefinition bond, DATA_TYPE data);

  RESULT_TYPE visitBondFutureOptionPremiumTransactionDefinition(BondFutureOptionPremiumTransactionDefinition bond, DATA_TYPE data);

  RESULT_TYPE visitBondIborTransactionDefinition(BondIborTransactionDefinition bond, DATA_TYPE data);

  RESULT_TYPE visitBondIborTransactionDefinition(BondIborTransactionDefinition bond);

  RESULT_TYPE visitBondIborSecurityDefinition(BondIborSecurityDefinition bond, DATA_TYPE data);

  RESULT_TYPE visitBondIborSecurityDefinition(BondIborSecurityDefinition bond);

  RESULT_TYPE visitBillSecurityDefinition(BillSecurityDefinition bill, DATA_TYPE data);

  RESULT_TYPE visitBillSecurityDefinition(BillSecurityDefinition bill);

  RESULT_TYPE visitBillTransactionDefinition(BillTransactionDefinition bill, DATA_TYPE data);

  RESULT_TYPE visitBillTransactionDefinition(BillTransactionDefinition bill);

  // -----     Deposit     -----

  RESULT_TYPE visitCashDefinition(CashDefinition cash, DATA_TYPE data);

  RESULT_TYPE visitCashDefinition(CashDefinition cash);

  RESULT_TYPE visitDepositIborDefinition(DepositIborDefinition deposit, DATA_TYPE data);

  RESULT_TYPE visitDepositIborDefinition(DepositIborDefinition deposit);

  RESULT_TYPE visitDepositCounterpartDefinition(DepositCounterpartDefinition deposit, DATA_TYPE data);

  RESULT_TYPE visitDepositCounterpartDefinition(DepositCounterpartDefinition deposit);

  RESULT_TYPE visitDepositZeroDefinition(DepositZeroDefinition deposit, DATA_TYPE data);

  RESULT_TYPE visitDepositZeroDefinition(DepositZeroDefinition deposit);

  // -----     Futures     -----
  RESULT_TYPE visitForwardRateAgreementDefinition(ForwardRateAgreementDefinition fra, DATA_TYPE data);

  RESULT_TYPE visitForwardRateAgreementDefinition(ForwardRateAgreementDefinition fra);

  RESULT_TYPE visitInterestRateFutureSecurityDefinition(InterestRateFutureDefinition future, DATA_TYPE data);

  RESULT_TYPE visitInterestRateFutureSecurityDefinition(InterestRateFutureDefinition future);

  RESULT_TYPE visitFederalFundsFutureSecurityDefinition(FederalFundsFutureSecurityDefinition future, DATA_TYPE data);

  RESULT_TYPE visitFederalFundsFutureSecurityDefinition(FederalFundsFutureSecurityDefinition future);

  RESULT_TYPE visitFederalFundsFutureTransactionDefinition(FederalFundsFutureTransactionDefinition future, DATA_TYPE data);

  RESULT_TYPE visitFederalFundsFutureTransactionDefinition(FederalFundsFutureTransactionDefinition future);

  RESULT_TYPE visitBondFutureSecurityDefinition(BondFutureDefinition bond);

  RESULT_TYPE visitDeliverableSwapFuturesSecurityDefinition(DeliverableSwapFuturesSecurityDefinition futures, DATA_TYPE data);

  RESULT_TYPE visitDeliverableSwapFuturesSecurityDefinition(DeliverableSwapFuturesSecurityDefinition futures);

  // -----     Futures options    -----

  RESULT_TYPE visitInterestRateFutureOptionPremiumSecurityDefinition(InterestRateFutureOptionPremiumSecurityDefinition future, DATA_TYPE data);

  RESULT_TYPE visitInterestRateFutureOptionPremiumSecurityDefinition(InterestRateFutureOptionPremiumSecurityDefinition future);

  RESULT_TYPE visitInterestRateFutureOptionPremiumTransactionDefinition(InterestRateFutureOptionPremiumTransactionDefinition future, DATA_TYPE data);

  RESULT_TYPE visitInterestRateFutureOptionPremiumTransactionDefinition(InterestRateFutureOptionPremiumTransactionDefinition future);

  RESULT_TYPE visitInterestRateFutureOptionMarginSecurityDefinition(InterestRateFutureOptionMarginSecurityDefinition future, DATA_TYPE data);

  RESULT_TYPE visitInterestRateFutureOptionMarginSecurityDefinition(InterestRateFutureOptionMarginSecurityDefinition future);

  RESULT_TYPE visitInterestRateFutureOptionMarginTransactionDefinition(InterestRateFutureOptionMarginTransactionDefinition future, DATA_TYPE data);

  RESULT_TYPE visitInterestRateFutureOptionMarginTransactionDefinition(InterestRateFutureOptionMarginTransactionDefinition future);

  RESULT_TYPE visitBondFutureOptionPremiumSecurityDefinition(BondFutureOptionPremiumSecurityDefinition bond);

  RESULT_TYPE visitBondFutureOptionPremiumTransactionDefinition(BondFutureOptionPremiumTransactionDefinition bond);

  // -----     Payment and coupon     -----

  RESULT_TYPE visitPaymentFixedDefinition(PaymentFixedDefinition payment, DATA_TYPE data);

  RESULT_TYPE visitPaymentFixedDefinition(PaymentFixedDefinition payment);

  RESULT_TYPE visitCouponFixedDefinition(CouponFixedDefinition payment, DATA_TYPE data);

  RESULT_TYPE visitCouponFixedDefinition(CouponFixedDefinition payment);

  RESULT_TYPE visitCouponIborDefinition(CouponIborDefinition payment, DATA_TYPE data);

  RESULT_TYPE visitCouponIborDefinition(CouponIborDefinition payment);

  RESULT_TYPE visitCouponIborSpreadDefinition(CouponIborSpreadDefinition payment, DATA_TYPE data);

  RESULT_TYPE visitCouponIborSpreadDefinition(CouponIborSpreadDefinition payment);

  RESULT_TYPE visitCouponIborGearingDefinition(CouponIborGearingDefinition payment, DATA_TYPE data);

  RESULT_TYPE visitCouponIborGearingDefinition(CouponIborGearingDefinition payment);

  RESULT_TYPE visitCouponIborCompoundedDefinition(CouponIborCompoundedDefinition payment, DATA_TYPE data);

  RESULT_TYPE visitCouponIborCompoundedDefinition(CouponIborCompoundedDefinition payment);

  RESULT_TYPE visitCouponIborRatchetDefinition(CouponIborRatchetDefinition payment, DATA_TYPE data);

  RESULT_TYPE visitCouponIborRatchetDefinition(CouponIborRatchetDefinition payment);

  RESULT_TYPE visitCapFloorIborDefinition(CapFloorIborDefinition payment, DATA_TYPE data);

  RESULT_TYPE visitCapFloorIborDefinition(CapFloorIborDefinition payment);

  RESULT_TYPE visitCouponOISSimplifiedDefinition(CouponOISSimplifiedDefinition payment, DATA_TYPE data);

  RESULT_TYPE visitCouponOISSimplifiedDefinition(CouponOISSimplifiedDefinition payment);

  RESULT_TYPE visitCouponOISDefinition(CouponOISDefinition payment, DATA_TYPE data);

  RESULT_TYPE visitCouponOISDefinition(CouponOISDefinition payment);

  RESULT_TYPE visitCouponCMSDefinition(CouponCMSDefinition payment, DATA_TYPE data);

  RESULT_TYPE visitCouponCMSDefinition(CouponCMSDefinition payment);

  RESULT_TYPE visitCapFloorCMSDefinition(CapFloorCMSDefinition payment, DATA_TYPE data);

  RESULT_TYPE visitCapFloorCMSDefinition(CapFloorCMSDefinition payment);

  RESULT_TYPE visitCapFloorCMSSpreadDefinition(CapFloorCMSSpreadDefinition payment, DATA_TYPE data);

  RESULT_TYPE visitCapFloorCMSSpreadDefinition(CapFloorCMSSpreadDefinition payment);

  // -----     Annuity     -----

  RESULT_TYPE visitAnnuityDefinition(AnnuityDefinition<? extends PaymentDefinition> annuity, DATA_TYPE data);

  RESULT_TYPE visitAnnuityDefinition(AnnuityDefinition<? extends PaymentDefinition> annuity);

  // -----     Swap     -----

  RESULT_TYPE visitSwapDefinition(SwapDefinition swap, DATA_TYPE data);

  RESULT_TYPE visitSwapDefinition(SwapDefinition swap);

  RESULT_TYPE visitSwapFixedIborDefinition(SwapFixedIborDefinition swap, DATA_TYPE data);

  RESULT_TYPE visitSwapFixedIborDefinition(SwapFixedIborDefinition swap);

  RESULT_TYPE visitSwapFixedIborSpreadDefinition(SwapFixedIborSpreadDefinition swap, DATA_TYPE data);

  RESULT_TYPE visitSwapFixedIborSpreadDefinition(SwapFixedIborSpreadDefinition swap);

  RESULT_TYPE visitSwapIborIborDefinition(SwapIborIborDefinition swap, DATA_TYPE data);

  RESULT_TYPE visitSwapIborIborDefinition(SwapIborIborDefinition swap);

  RESULT_TYPE visitSwapXCcyIborIborDefinition(SwapXCcyIborIborDefinition swap, DATA_TYPE data);

  RESULT_TYPE visitSwapXCcyIborIborDefinition(SwapXCcyIborIborDefinition swap);

  // -----     Swaption     -----

  RESULT_TYPE visitSwaptionCashFixedIborDefinition(SwaptionCashFixedIborDefinition swaption, DATA_TYPE data);

  RESULT_TYPE visitSwaptionCashFixedIborDefinition(SwaptionCashFixedIborDefinition swaption);

  RESULT_TYPE visitSwaptionPhysicalFixedIborDefinition(SwaptionPhysicalFixedIborDefinition swaption, DATA_TYPE data);

  RESULT_TYPE visitSwaptionPhysicalFixedIborDefinition(SwaptionPhysicalFixedIborDefinition swaption);

  RESULT_TYPE visitSwaptionPhysicalFixedIborSpreadDefinition(SwaptionPhysicalFixedIborSpreadDefinition swaption, DATA_TYPE data);

  RESULT_TYPE visitSwaptionPhysicalFixedIborSpreadDefinition(SwaptionPhysicalFixedIborSpreadDefinition swaption);

  RESULT_TYPE visitSwaptionBermudaFixedIborDefinition(SwaptionBermudaFixedIborDefinition swaption, DATA_TYPE data);

  RESULT_TYPE visitSwaptionBermudaFixedIborDefinition(SwaptionBermudaFixedIborDefinition swaption);

  // -----     Inflation     -----

  RESULT_TYPE visitCouponInflationZeroCouponFirstOfMonth(CouponInflationZeroCouponMonthlyDefinition coupon, DATA_TYPE data);

  RESULT_TYPE visitCouponInflationZeroCouponFirstOfMonth(CouponInflationZeroCouponMonthlyDefinition coupon);

  RESULT_TYPE visitCouponInflationZeroCouponInterpolation(CouponInflationZeroCouponInterpolationDefinition coupon, DATA_TYPE data);

  RESULT_TYPE visitCouponInflationZeroCouponInterpolation(CouponInflationZeroCouponInterpolationDefinition coupon);

  RESULT_TYPE visitCouponInflationZeroCouponMonthlyGearing(CouponInflationZeroCouponMonthlyGearingDefinition coupon, DATA_TYPE data);

  RESULT_TYPE visitCouponInflationZeroCouponMonthlyGearing(CouponInflationZeroCouponMonthlyGearingDefinition coupon);

  RESULT_TYPE visitCouponInflationZeroCouponInterpolationGearing(CouponInflationZeroCouponInterpolationGearingDefinition coupon, DATA_TYPE data);

  RESULT_TYPE visitCouponInflationZeroCouponInterpolationGearing(CouponInflationZeroCouponInterpolationGearingDefinition coupon);

  RESULT_TYPE visitBondCapitalIndexedSecurity(BondCapitalIndexedSecurityDefinition<?> bond, DATA_TYPE data);

  RESULT_TYPE visitBondCapitalIndexedSecurity(BondCapitalIndexedSecurityDefinition<?> bond);

  RESULT_TYPE visitBondCapitalIndexedTransaction(BondCapitalIndexedTransactionDefinition<?> bond, DATA_TYPE data);

  RESULT_TYPE visitBondCapitalIndexedTransaction(BondCapitalIndexedTransactionDefinition<?> bond);

  // -----     Forex     -----

  RESULT_TYPE visitForexDefinition(ForexDefinition fx, DATA_TYPE data);

  RESULT_TYPE visitForexDefinition(ForexDefinition fx);

  RESULT_TYPE visitForexSwapDefinition(ForexSwapDefinition fx, DATA_TYPE data);

  RESULT_TYPE visitForexSwapDefinition(ForexSwapDefinition fx);

  RESULT_TYPE visitForexOptionVanillaDefinition(ForexOptionVanillaDefinition fx, DATA_TYPE data);

  RESULT_TYPE visitForexOptionVanillaDefinition(ForexOptionVanillaDefinition fx);

  RESULT_TYPE visitForexOptionSingleBarrierDefiniton(ForexOptionSingleBarrierDefinition fx, DATA_TYPE data);

  RESULT_TYPE visitForexOptionSingleBarrierDefiniton(ForexOptionSingleBarrierDefinition fx);

  RESULT_TYPE visitForexNonDeliverableForwardDefinition(ForexNonDeliverableForwardDefinition ndf, DATA_TYPE data);

  RESULT_TYPE visitForexNonDeliverableForwardDefinition(ForexNonDeliverableForwardDefinition ndf);

  RESULT_TYPE visitForexNonDeliverableOptionDefinition(ForexNonDeliverableOptionDefinition ndo, DATA_TYPE data);

  RESULT_TYPE visitForexNonDeliverableOptionDefinition(ForexNonDeliverableOptionDefinition ndo);

  RESULT_TYPE visitForexOptionDigitalDefinition(ForexOptionDigitalDefinition fx, DATA_TYPE data);

  RESULT_TYPE visitForexOptionDigitalDefinition(ForexOptionDigitalDefinition fx);

}
