/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument;

import com.opengamma.analytics.financial.commodity.definition.AgricultureForwardDefinition;
import com.opengamma.analytics.financial.commodity.definition.AgricultureFutureDefinition;
import com.opengamma.analytics.financial.commodity.definition.AgricultureFutureOptionDefinition;
import com.opengamma.analytics.financial.commodity.definition.EnergyForwardDefinition;
import com.opengamma.analytics.financial.commodity.definition.EnergyFutureDefinition;
import com.opengamma.analytics.financial.commodity.definition.EnergyFutureOptionDefinition;
import com.opengamma.analytics.financial.commodity.definition.MetalForwardDefinition;
import com.opengamma.analytics.financial.commodity.definition.MetalFutureDefinition;
import com.opengamma.analytics.financial.commodity.definition.MetalFutureOptionDefinition;
import com.opengamma.analytics.financial.equity.future.definition.EquityFutureDefinition;
import com.opengamma.analytics.financial.equity.future.definition.EquityIndexDividendFutureDefinition;
import com.opengamma.analytics.financial.equity.future.definition.EquityIndexFutureDefinition;
import com.opengamma.analytics.financial.equity.future.definition.IndexFutureDefinition;
import com.opengamma.analytics.financial.equity.future.definition.VolatilityIndexFutureDefinition;
import com.opengamma.analytics.financial.equity.option.EquityIndexFutureOptionDefinition;
import com.opengamma.analytics.financial.equity.option.EquityIndexOptionDefinition;
import com.opengamma.analytics.financial.equity.option.EquityOptionDefinition;
import com.opengamma.analytics.financial.equity.variance.EquityVarianceSwapDefinition;
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
import com.opengamma.analytics.financial.instrument.bond.BondInterestIndexedSecurityDefinition;
import com.opengamma.analytics.financial.instrument.bond.BondInterestIndexedTransactionDefinition;
import com.opengamma.analytics.financial.instrument.cash.CashDefinition;
import com.opengamma.analytics.financial.instrument.cash.DepositCounterpartDefinition;
import com.opengamma.analytics.financial.instrument.cash.DepositIborDefinition;
import com.opengamma.analytics.financial.instrument.cash.DepositZeroDefinition;
import com.opengamma.analytics.financial.instrument.cds.ISDACDSDefinition;
import com.opengamma.analytics.financial.instrument.fra.ForwardRateAgreementDefinition;
import com.opengamma.analytics.financial.instrument.future.BondFutureDefinition;
import com.opengamma.analytics.financial.instrument.future.BondFutureOptionPremiumSecurityDefinition;
import com.opengamma.analytics.financial.instrument.future.BondFutureOptionPremiumTransactionDefinition;
import com.opengamma.analytics.financial.instrument.future.BondFuturesSecurityDefinition;
import com.opengamma.analytics.financial.instrument.future.BondFuturesTransactionDefinition;
import com.opengamma.analytics.financial.instrument.future.FederalFundsFutureSecurityDefinition;
import com.opengamma.analytics.financial.instrument.future.FederalFundsFutureTransactionDefinition;
import com.opengamma.analytics.financial.instrument.future.InterestRateFutureOptionMarginSecurityDefinition;
import com.opengamma.analytics.financial.instrument.future.InterestRateFutureOptionMarginTransactionDefinition;
import com.opengamma.analytics.financial.instrument.future.InterestRateFutureOptionPremiumSecurityDefinition;
import com.opengamma.analytics.financial.instrument.future.InterestRateFutureOptionPremiumTransactionDefinition;
import com.opengamma.analytics.financial.instrument.future.InterestRateFutureSecurityDefinition;
import com.opengamma.analytics.financial.instrument.future.InterestRateFutureTransactionDefinition;
import com.opengamma.analytics.financial.instrument.future.SwapFuturesPriceDeliverableSecurityDefinition;
import com.opengamma.analytics.financial.instrument.future.SwapFuturesPriceDeliverableTransactionDefinition;
import com.opengamma.analytics.financial.instrument.inflation.CapFloorInflationYearOnYearInterpolationDefinition;
import com.opengamma.analytics.financial.instrument.inflation.CapFloorInflationYearOnYearMonthlyDefinition;
import com.opengamma.analytics.financial.instrument.inflation.CapFloorInflationZeroCouponInterpolationDefinition;
import com.opengamma.analytics.financial.instrument.inflation.CapFloorInflationZeroCouponMonthlyDefinition;
import com.opengamma.analytics.financial.instrument.inflation.CouponInflationYearOnYearInterpolationDefinition;
import com.opengamma.analytics.financial.instrument.inflation.CouponInflationYearOnYearInterpolationWithMarginDefinition;
import com.opengamma.analytics.financial.instrument.inflation.CouponInflationYearOnYearMonthlyDefinition;
import com.opengamma.analytics.financial.instrument.inflation.CouponInflationYearOnYearMonthlyWithMarginDefinition;
import com.opengamma.analytics.financial.instrument.inflation.CouponInflationZeroCouponInterpolationDefinition;
import com.opengamma.analytics.financial.instrument.inflation.CouponInflationZeroCouponInterpolationGearingDefinition;
import com.opengamma.analytics.financial.instrument.inflation.CouponInflationZeroCouponMonthlyDefinition;
import com.opengamma.analytics.financial.instrument.inflation.CouponInflationZeroCouponMonthlyGearingDefinition;
import com.opengamma.analytics.financial.instrument.payment.CapFloorCMSDefinition;
import com.opengamma.analytics.financial.instrument.payment.CapFloorCMSSpreadDefinition;
import com.opengamma.analytics.financial.instrument.payment.CapFloorIborDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponArithmeticAverageONDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponArithmeticAverageONSpreadDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponArithmeticAverageONSpreadSimplifiedDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponCMSDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponFixedAccruedCompoundingDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponFixedCompoundingDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponFixedDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborAverageDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborCompoundingDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborCompoundingFlatSpreadDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborCompoundingSpreadDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborGearingDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborRatchetDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborSpreadDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponONCompoundedDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponONDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponONSimplifiedDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponONSpreadSimplifiedDefinition;
import com.opengamma.analytics.financial.instrument.payment.PaymentDefinition;
import com.opengamma.analytics.financial.instrument.payment.PaymentFixedDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedIborDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedIborSpreadDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapIborIborDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapXCcyIborIborDefinition;
import com.opengamma.analytics.financial.instrument.swaption.SwaptionBermudaFixedIborDefinition;
import com.opengamma.analytics.financial.instrument.swaption.SwaptionCashFixedCompoundedONCompoundingDefinition;
import com.opengamma.analytics.financial.instrument.swaption.SwaptionCashFixedIborDefinition;
import com.opengamma.analytics.financial.instrument.swaption.SwaptionPhysicalFixedIborDefinition;
import com.opengamma.analytics.financial.instrument.swaption.SwaptionPhysicalFixedIborSpreadDefinition;
import com.opengamma.analytics.financial.instrument.varianceswap.VarianceSwapDefinition;
import com.opengamma.util.ArgumentChecker;

/**
 * Delegates functionality to the visitor provided in the constructor
 * @param <DATA_TYPE> The type of the data
 * @param <RESULT_TYPE> The type of the results
 */
public class InstrumentDefinitionVisitorDelegate<DATA_TYPE, RESULT_TYPE> implements InstrumentDefinitionVisitor<DATA_TYPE, RESULT_TYPE> {
  /** The delegate visitor */
  private final InstrumentDefinitionVisitor<DATA_TYPE, RESULT_TYPE> _delegate;

  /**
   * @param delegate The delegate, not null
   */
  public InstrumentDefinitionVisitorDelegate(final InstrumentDefinitionVisitor<DATA_TYPE, RESULT_TYPE> delegate) {
    ArgumentChecker.notNull(delegate, "delegate");
    _delegate = delegate;
  }

  @Override
  public RESULT_TYPE visitBondFixedSecurityDefinition(final BondFixedSecurityDefinition bond, final DATA_TYPE data) {
    return _delegate.visitBondFixedSecurityDefinition(bond, data);
  }

  @Override
  public RESULT_TYPE visitBondFixedSecurityDefinition(final BondFixedSecurityDefinition bond) {
    return _delegate.visitBondFixedSecurityDefinition(bond);
  }

  @Override
  public RESULT_TYPE visitBondFixedTransactionDefinition(final BondFixedTransactionDefinition bond, final DATA_TYPE data) {
    return _delegate.visitBondFixedTransactionDefinition(bond, data);
  }

  @Override
  public RESULT_TYPE visitBondFixedTransactionDefinition(final BondFixedTransactionDefinition bond) {
    return _delegate.visitBondFixedTransactionDefinition(bond);
  }

  @Override
  public RESULT_TYPE visitBondFutureDefinition(final BondFutureDefinition bond, final DATA_TYPE data) {
    return _delegate.visitBondFutureDefinition(bond, data);
  }

  @Override
  public RESULT_TYPE visitBondFutureDefinition(final BondFutureDefinition bond) {
    return _delegate.visitBondFutureDefinition(bond);
  }

  @Override
  public RESULT_TYPE visitBondFuturesSecurityDefinition(final BondFuturesSecurityDefinition bond, final DATA_TYPE data) {
    return _delegate.visitBondFuturesSecurityDefinition(bond, data);
  }

  @Override
  public RESULT_TYPE visitBondFuturesSecurityDefinition(final BondFuturesSecurityDefinition bond) {
    return _delegate.visitBondFuturesSecurityDefinition(bond);
  }

  @Override
  public RESULT_TYPE visitBondFuturesTransactionDefinition(final BondFuturesTransactionDefinition bond, final DATA_TYPE data) {
    return _delegate.visitBondFuturesTransactionDefinition(bond, data);
  }

  @Override
  public RESULT_TYPE visitBondFuturesTransactionDefinition(final BondFuturesTransactionDefinition bond) {
    return _delegate.visitBondFuturesTransactionDefinition(bond);
  }

  @Override
  public RESULT_TYPE visitBondFutureOptionPremiumSecurityDefinition(final BondFutureOptionPremiumSecurityDefinition bond, final DATA_TYPE data) {
    return _delegate.visitBondFutureOptionPremiumSecurityDefinition(bond, data);
  }

  @Override
  public RESULT_TYPE visitBondFutureOptionPremiumTransactionDefinition(final BondFutureOptionPremiumTransactionDefinition bond, final DATA_TYPE data) {
    return _delegate.visitBondFutureOptionPremiumTransactionDefinition(bond, data);
  }

  @Override
  public RESULT_TYPE visitBondIborTransactionDefinition(final BondIborTransactionDefinition bond, final DATA_TYPE data) {
    return _delegate.visitBondIborTransactionDefinition(bond, data);
  }

  @Override
  public RESULT_TYPE visitBondIborTransactionDefinition(final BondIborTransactionDefinition bond) {
    return _delegate.visitBondIborTransactionDefinition(bond);
  }

  @Override
  public RESULT_TYPE visitBondIborSecurityDefinition(final BondIborSecurityDefinition bond, final DATA_TYPE data) {
    return _delegate.visitBondIborSecurityDefinition(bond, data);
  }

  @Override
  public RESULT_TYPE visitBondIborSecurityDefinition(final BondIborSecurityDefinition bond) {
    return _delegate.visitBondIborSecurityDefinition(bond);
  }

  @Override
  public RESULT_TYPE visitBillSecurityDefinition(final BillSecurityDefinition bill, final DATA_TYPE data) {
    return _delegate.visitBillSecurityDefinition(bill, data);
  }

  @Override
  public RESULT_TYPE visitBillSecurityDefinition(final BillSecurityDefinition bill) {
    return _delegate.visitBillSecurityDefinition(bill);
  }

  @Override
  public RESULT_TYPE visitBillTransactionDefinition(final BillTransactionDefinition bill, final DATA_TYPE data) {
    return _delegate.visitBillTransactionDefinition(bill, data);
  }

  @Override
  public RESULT_TYPE visitBillTransactionDefinition(final BillTransactionDefinition bill) {
    return _delegate.visitBillTransactionDefinition(bill);
  }

  @Override
  public RESULT_TYPE visitCashDefinition(final CashDefinition cash, final DATA_TYPE data) {
    return _delegate.visitCashDefinition(cash, data);
  }

  @Override
  public RESULT_TYPE visitCashDefinition(final CashDefinition cash) {
    return _delegate.visitCashDefinition(cash);
  }

  @Override
  public RESULT_TYPE visitDepositIborDefinition(final DepositIborDefinition deposit, final DATA_TYPE data) {
    return _delegate.visitDepositIborDefinition(deposit, data);
  }

  @Override
  public RESULT_TYPE visitDepositIborDefinition(final DepositIborDefinition deposit) {
    return _delegate.visitDepositIborDefinition(deposit);
  }

  @Override
  public RESULT_TYPE visitDepositCounterpartDefinition(final DepositCounterpartDefinition deposit, final DATA_TYPE data) {
    return _delegate.visitDepositCounterpartDefinition(deposit, data);
  }

  @Override
  public RESULT_TYPE visitDepositCounterpartDefinition(final DepositCounterpartDefinition deposit) {
    return _delegate.visitDepositCounterpartDefinition(deposit);
  }

  @Override
  public RESULT_TYPE visitDepositZeroDefinition(final DepositZeroDefinition deposit, final DATA_TYPE data) {
    return _delegate.visitDepositZeroDefinition(deposit, data);
  }

  @Override
  public RESULT_TYPE visitDepositZeroDefinition(final DepositZeroDefinition deposit) {
    return _delegate.visitDepositZeroDefinition(deposit);
  }

  @Override
  public RESULT_TYPE visitForwardRateAgreementDefinition(final ForwardRateAgreementDefinition fra, final DATA_TYPE data) {
    return _delegate.visitForwardRateAgreementDefinition(fra, data);
  }

  @Override
  public RESULT_TYPE visitForwardRateAgreementDefinition(final ForwardRateAgreementDefinition fra) {
    return _delegate.visitForwardRateAgreementDefinition(fra);
  }

  @Override
  public RESULT_TYPE visitInterestRateFutureSecurityDefinition(final InterestRateFutureSecurityDefinition future, final DATA_TYPE data) {
    return _delegate.visitInterestRateFutureSecurityDefinition(future, data);
  }

  @Override
  public RESULT_TYPE visitInterestRateFutureSecurityDefinition(final InterestRateFutureSecurityDefinition future) {
    return _delegate.visitInterestRateFutureSecurityDefinition(future);
  }

  @Override
  public RESULT_TYPE visitInterestRateFutureTransactionDefinition(final InterestRateFutureTransactionDefinition future, final DATA_TYPE data) {
    return _delegate.visitInterestRateFutureTransactionDefinition(future, data);
  }

  @Override
  public RESULT_TYPE visitInterestRateFutureTransactionDefinition(final InterestRateFutureTransactionDefinition future) {
    return _delegate.visitInterestRateFutureTransactionDefinition(future);
  }

  @Override
  public RESULT_TYPE visitFederalFundsFutureSecurityDefinition(final FederalFundsFutureSecurityDefinition future, final DATA_TYPE data) {
    return _delegate.visitFederalFundsFutureSecurityDefinition(future, data);
  }

  @Override
  public RESULT_TYPE visitFederalFundsFutureSecurityDefinition(final FederalFundsFutureSecurityDefinition future) {
    return _delegate.visitFederalFundsFutureSecurityDefinition(future);
  }

  @Override
  public RESULT_TYPE visitFederalFundsFutureTransactionDefinition(final FederalFundsFutureTransactionDefinition future, final DATA_TYPE data) {
    return _delegate.visitFederalFundsFutureTransactionDefinition(future, data);
  }

  @Override
  public RESULT_TYPE visitFederalFundsFutureTransactionDefinition(final FederalFundsFutureTransactionDefinition future) {
    return _delegate.visitFederalFundsFutureTransactionDefinition(future);
  }

  @Override
  public RESULT_TYPE visitDeliverableSwapFuturesSecurityDefinition(final SwapFuturesPriceDeliverableSecurityDefinition futures, final DATA_TYPE data) {
    return _delegate.visitDeliverableSwapFuturesSecurityDefinition(futures, data);
  }

  @Override
  public RESULT_TYPE visitDeliverableSwapFuturesSecurityDefinition(final SwapFuturesPriceDeliverableSecurityDefinition futures) {
    return _delegate.visitDeliverableSwapFuturesSecurityDefinition(futures);
  }

  @Override
  public RESULT_TYPE visitDeliverableSwapFuturesTransactionDefinition(final SwapFuturesPriceDeliverableTransactionDefinition futures, final DATA_TYPE data) {
    return _delegate.visitDeliverableSwapFuturesTransactionDefinition(futures, data);
  }

  @Override
  public RESULT_TYPE visitDeliverableSwapFuturesTransactionDefinition(final SwapFuturesPriceDeliverableTransactionDefinition futures) {
    return _delegate.visitDeliverableSwapFuturesTransactionDefinition(futures);
  }

  @Override
  public RESULT_TYPE visitInterestRateFutureOptionPremiumSecurityDefinition(final InterestRateFutureOptionPremiumSecurityDefinition future, final DATA_TYPE data) {
    return _delegate.visitInterestRateFutureOptionPremiumSecurityDefinition(future, data);
  }

  @Override
  public RESULT_TYPE visitInterestRateFutureOptionPremiumSecurityDefinition(final InterestRateFutureOptionPremiumSecurityDefinition future) {
    return _delegate.visitInterestRateFutureOptionPremiumSecurityDefinition(future);
  }

  @Override
  public RESULT_TYPE visitInterestRateFutureOptionPremiumTransactionDefinition(final InterestRateFutureOptionPremiumTransactionDefinition future, final DATA_TYPE data) {
    return _delegate.visitInterestRateFutureOptionPremiumTransactionDefinition(future, data);
  }

  @Override
  public RESULT_TYPE visitInterestRateFutureOptionPremiumTransactionDefinition(final InterestRateFutureOptionPremiumTransactionDefinition future) {
    return _delegate.visitInterestRateFutureOptionPremiumTransactionDefinition(future);
  }

  @Override
  public RESULT_TYPE visitInterestRateFutureOptionMarginSecurityDefinition(final InterestRateFutureOptionMarginSecurityDefinition future, final DATA_TYPE data) {
    return _delegate.visitInterestRateFutureOptionMarginSecurityDefinition(future, data);
  }

  @Override
  public RESULT_TYPE visitInterestRateFutureOptionMarginSecurityDefinition(final InterestRateFutureOptionMarginSecurityDefinition future) {
    return _delegate.visitInterestRateFutureOptionMarginSecurityDefinition(future);
  }

  @Override
  public RESULT_TYPE visitInterestRateFutureOptionMarginTransactionDefinition(final InterestRateFutureOptionMarginTransactionDefinition future, final DATA_TYPE data) {
    return _delegate.visitInterestRateFutureOptionMarginTransactionDefinition(future, data);
  }

  @Override
  public RESULT_TYPE visitInterestRateFutureOptionMarginTransactionDefinition(final InterestRateFutureOptionMarginTransactionDefinition future) {
    return _delegate.visitInterestRateFutureOptionMarginTransactionDefinition(future);
  }

  @Override
  public RESULT_TYPE visitBondFutureOptionPremiumSecurityDefinition(final BondFutureOptionPremiumSecurityDefinition bond) {
    return _delegate.visitBondFutureOptionPremiumSecurityDefinition(bond);
  }

  @Override
  public RESULT_TYPE visitBondFutureOptionPremiumTransactionDefinition(final BondFutureOptionPremiumTransactionDefinition bond) {
    return _delegate.visitBondFutureOptionPremiumTransactionDefinition(bond);
  }

  // -----     Payment and coupon     -----

  @Override
  public RESULT_TYPE visitPaymentFixedDefinition(final PaymentFixedDefinition payment, final DATA_TYPE data) {
    return _delegate.visitPaymentFixedDefinition(payment, data);
  }

  @Override
  public RESULT_TYPE visitPaymentFixedDefinition(final PaymentFixedDefinition payment) {
    return _delegate.visitPaymentFixedDefinition(payment);
  }

  @Override
  public RESULT_TYPE visitCouponFixedDefinition(final CouponFixedDefinition payment, final DATA_TYPE data) {
    return _delegate.visitCouponFixedDefinition(payment, data);
  }

  @Override
  public RESULT_TYPE visitCouponFixedDefinition(final CouponFixedDefinition payment) {
    return _delegate.visitCouponFixedDefinition(payment);
  }

  @Override
  public RESULT_TYPE visitCouponFixedCompoundingDefinition(final CouponFixedCompoundingDefinition payment, final DATA_TYPE data) {
    return _delegate.visitCouponFixedCompoundingDefinition(payment, data);
  }

  @Override
  public RESULT_TYPE visitCouponFixedCompoundingDefinition(final CouponFixedCompoundingDefinition payment) {
    return _delegate.visitCouponFixedCompoundingDefinition(payment);
  }

  @Override
  public RESULT_TYPE visitCouponFixedAccruedCompoundingDefinition(final CouponFixedAccruedCompoundingDefinition payment, final DATA_TYPE data) {
    return _delegate.visitCouponFixedAccruedCompoundingDefinition(payment, data);
  }

  @Override
  public RESULT_TYPE visitCouponFixedAccruedCompoundingDefinition(final CouponFixedAccruedCompoundingDefinition payment) {
    return _delegate.visitCouponFixedAccruedCompoundingDefinition(payment);
  }

  @Override
  public RESULT_TYPE visitCouponIborDefinition(final CouponIborDefinition payment, final DATA_TYPE data) {
    return _delegate.visitCouponIborDefinition(payment, data);
  }

  @Override
  public RESULT_TYPE visitCouponIborDefinition(final CouponIborDefinition payment) {
    return _delegate.visitCouponIborDefinition(payment);
  }

  @Override
  public RESULT_TYPE visitCouponIborAverageDefinition(final CouponIborAverageDefinition payment, final DATA_TYPE data) {
    return _delegate.visitCouponIborAverageDefinition(payment, data);
  }

  @Override
  public RESULT_TYPE visitCouponIborAverageDefinition(final CouponIborAverageDefinition payment) {
    return _delegate.visitCouponIborAverageDefinition(payment);
  }

  @Override
  public RESULT_TYPE visitCouponIborSpreadDefinition(final CouponIborSpreadDefinition payment, final DATA_TYPE data) {
    return _delegate.visitCouponIborSpreadDefinition(payment, data);
  }

  @Override
  public RESULT_TYPE visitCouponIborSpreadDefinition(final CouponIborSpreadDefinition payment) {
    return _delegate.visitCouponIborSpreadDefinition(payment);
  }

  @Override
  public RESULT_TYPE visitCouponIborGearingDefinition(final CouponIborGearingDefinition payment, final DATA_TYPE data) {
    return _delegate.visitCouponIborGearingDefinition(payment, data);
  }

  @Override
  public RESULT_TYPE visitCouponIborGearingDefinition(final CouponIborGearingDefinition payment) {
    return _delegate.visitCouponIborGearingDefinition(payment);
  }

  @Override
  public RESULT_TYPE visitCouponIborCompoundingDefinition(final CouponIborCompoundingDefinition payment, final DATA_TYPE data) {
    return _delegate.visitCouponIborCompoundingDefinition(payment, data);
  }

  @Override
  public RESULT_TYPE visitCouponIborCompoundingDefinition(final CouponIborCompoundingDefinition payment) {
    return _delegate.visitCouponIborCompoundingDefinition(payment);
  }

  @Override
  public RESULT_TYPE visitCouponIborCompoundingSpreadDefinition(final CouponIborCompoundingSpreadDefinition payment, final DATA_TYPE data) {
    return _delegate.visitCouponIborCompoundingSpreadDefinition(payment, data);
  }

  @Override
  public RESULT_TYPE visitCouponIborCompoundingSpreadDefinition(final CouponIborCompoundingSpreadDefinition payment) {
    return _delegate.visitCouponIborCompoundingSpreadDefinition(payment);
  }

  @Override
  public RESULT_TYPE visitCouponIborCompoundingFlatSpreadDefinition(final CouponIborCompoundingFlatSpreadDefinition payment, final DATA_TYPE data) {
    return _delegate.visitCouponIborCompoundingFlatSpreadDefinition(payment, data);
  }

  @Override
  public RESULT_TYPE visitCouponIborCompoundingFlatSpreadDefinition(final CouponIborCompoundingFlatSpreadDefinition payment) {
    return _delegate.visitCouponIborCompoundingFlatSpreadDefinition(payment);
  }

  @Override
  public RESULT_TYPE visitCouponIborRatchetDefinition(final CouponIborRatchetDefinition payment, final DATA_TYPE data) {
    return _delegate.visitCouponIborRatchetDefinition(payment, data);
  }

  @Override
  public RESULT_TYPE visitCouponIborRatchetDefinition(final CouponIborRatchetDefinition payment) {
    return _delegate.visitCouponIborRatchetDefinition(payment);
  }

  @Override
  public RESULT_TYPE visitCapFloorIborDefinition(final CapFloorIborDefinition payment, final DATA_TYPE data) {
    return _delegate.visitCapFloorIborDefinition(payment, data);
  }

  @Override
  public RESULT_TYPE visitCapFloorIborDefinition(final CapFloorIborDefinition payment) {
    return _delegate.visitCapFloorIborDefinition(payment);
  }

  @Override
  public RESULT_TYPE visitCouponOISSimplifiedDefinition(final CouponONSimplifiedDefinition payment, final DATA_TYPE data) {
    return _delegate.visitCouponOISSimplifiedDefinition(payment, data);
  }

  @Override
  public RESULT_TYPE visitCouponOISSimplifiedDefinition(final CouponONSimplifiedDefinition payment) {
    return _delegate.visitCouponOISSimplifiedDefinition(payment);
  }

  @Override
  public RESULT_TYPE visitCouponONSpreadSimplifiedDefinition(final CouponONSpreadSimplifiedDefinition payment, final DATA_TYPE data) {
    return _delegate.visitCouponONSpreadSimplifiedDefinition(payment, data);
  }

  @Override
  public RESULT_TYPE visitCouponONSpreadSimplifiedDefinition(final CouponONSpreadSimplifiedDefinition payment) {
    return _delegate.visitCouponONSpreadSimplifiedDefinition(payment);
  }

  @Override
  public RESULT_TYPE visitCouponOISDefinition(final CouponONDefinition payment, final DATA_TYPE data) {
    return _delegate.visitCouponOISDefinition(payment, data);
  }

  @Override
  public RESULT_TYPE visitCouponOISDefinition(final CouponONDefinition payment) {
    return _delegate.visitCouponOISDefinition(payment);
  }

  @Override
  public RESULT_TYPE visitCouponONCompoundedDefinition(final CouponONCompoundedDefinition payment, final DATA_TYPE data) {
    return _delegate.visitCouponONCompoundedDefinition(payment, data);
  }

  @Override
  public RESULT_TYPE visitCouponONCompoundedDefinition(final CouponONCompoundedDefinition payment) {
    return _delegate.visitCouponONCompoundedDefinition(payment);
  }

  @Override
  public RESULT_TYPE visitCouponArithmeticAverageONDefinition(final CouponArithmeticAverageONDefinition payment, final DATA_TYPE data) {
    return _delegate.visitCouponArithmeticAverageONDefinition(payment, data);
  }

  @Override
  public RESULT_TYPE visitCouponArithmeticAverageONDefinition(final CouponArithmeticAverageONDefinition payment) {
    return _delegate.visitCouponArithmeticAverageONDefinition(payment);
  }

  @Override
  public RESULT_TYPE visitCouponArithmeticAverageONSpreadDefinition(final CouponArithmeticAverageONSpreadDefinition payment, final DATA_TYPE data) {
    return _delegate.visitCouponArithmeticAverageONSpreadDefinition(payment, data);
  }

  @Override
  public RESULT_TYPE visitCouponArithmeticAverageONSpreadDefinition(final CouponArithmeticAverageONSpreadDefinition payment) {
    return _delegate.visitCouponArithmeticAverageONSpreadDefinition(payment);
  }

  @Override
  public RESULT_TYPE visitCouponArithmeticAverageONSpreadSimplifiedDefinition(final CouponArithmeticAverageONSpreadSimplifiedDefinition payment, final DATA_TYPE data) {
    return _delegate.visitCouponArithmeticAverageONSpreadSimplifiedDefinition(payment, data);
  }

  @Override
  public RESULT_TYPE visitCouponArithmeticAverageONSpreadSimplifiedDefinition(final CouponArithmeticAverageONSpreadSimplifiedDefinition payment) {
    return _delegate.visitCouponArithmeticAverageONSpreadSimplifiedDefinition(payment);
  }

  @Override
  public RESULT_TYPE visitCouponCMSDefinition(final CouponCMSDefinition payment, final DATA_TYPE data) {
    return _delegate.visitCouponCMSDefinition(payment, data);
  }

  @Override
  public RESULT_TYPE visitCouponCMSDefinition(final CouponCMSDefinition payment) {
    return _delegate.visitCouponCMSDefinition(payment);
  }

  @Override
  public RESULT_TYPE visitCapFloorCMSDefinition(final CapFloorCMSDefinition payment, final DATA_TYPE data) {
    return _delegate.visitCapFloorCMSDefinition(payment, data);
  }

  @Override
  public RESULT_TYPE visitCapFloorCMSDefinition(final CapFloorCMSDefinition payment) {
    return _delegate.visitCapFloorCMSDefinition(payment);
  }

  @Override
  public RESULT_TYPE visitCapFloorCMSSpreadDefinition(final CapFloorCMSSpreadDefinition payment, final DATA_TYPE data) {
    return _delegate.visitCapFloorCMSSpreadDefinition(payment, data);
  }

  @Override
  public RESULT_TYPE visitCapFloorCMSSpreadDefinition(final CapFloorCMSSpreadDefinition payment) {
    return _delegate.visitCapFloorCMSSpreadDefinition(payment);
  }

  // -----     Annuity     -----

  @Override
  public RESULT_TYPE visitAnnuityDefinition(final AnnuityDefinition<? extends PaymentDefinition> annuity, final DATA_TYPE data) {
    return _delegate.visitAnnuityDefinition(annuity, data);
  }

  @Override
  public RESULT_TYPE visitAnnuityDefinition(final AnnuityDefinition<? extends PaymentDefinition> annuity) {
    return _delegate.visitAnnuityDefinition(annuity);
  }

  @Override
  public RESULT_TYPE visitSwapDefinition(final SwapDefinition swap, final DATA_TYPE data) {
    return _delegate.visitSwapDefinition(swap, data);
  }

  @Override
  public RESULT_TYPE visitSwapDefinition(final SwapDefinition swap) {
    return _delegate.visitSwapDefinition(swap);
  }

  @Override
  public RESULT_TYPE visitSwapFixedIborDefinition(final SwapFixedIborDefinition swap, final DATA_TYPE data) {
    return _delegate.visitSwapFixedIborDefinition(swap, data);
  }

  @Override
  public RESULT_TYPE visitSwapFixedIborDefinition(final SwapFixedIborDefinition swap) {
    return _delegate.visitSwapFixedIborDefinition(swap);
  }

  @Override
  public RESULT_TYPE visitSwapFixedIborSpreadDefinition(final SwapFixedIborSpreadDefinition swap, final DATA_TYPE data) {
    return _delegate.visitSwapFixedIborSpreadDefinition(swap, data);
  }

  @Override
  public RESULT_TYPE visitSwapFixedIborSpreadDefinition(final SwapFixedIborSpreadDefinition swap) {
    return _delegate.visitSwapFixedIborSpreadDefinition(swap);
  }

  @Override
  public RESULT_TYPE visitSwapIborIborDefinition(final SwapIborIborDefinition swap, final DATA_TYPE data) {
    return _delegate.visitSwapIborIborDefinition(swap, data);
  }

  @Override
  public RESULT_TYPE visitSwapIborIborDefinition(final SwapIborIborDefinition swap) {
    return _delegate.visitSwapIborIborDefinition(swap);
  }

  @Override
  public RESULT_TYPE visitSwapXCcyIborIborDefinition(final SwapXCcyIborIborDefinition swap, final DATA_TYPE data) {
    return _delegate.visitSwapXCcyIborIborDefinition(swap, data);
  }

  @Override
  public RESULT_TYPE visitSwapXCcyIborIborDefinition(final SwapXCcyIborIborDefinition swap) {
    return _delegate.visitSwapXCcyIborIborDefinition(swap);
  }

  @Override
  public RESULT_TYPE visitSwaptionCashFixedIborDefinition(final SwaptionCashFixedIborDefinition swaption, final DATA_TYPE data) {
    return _delegate.visitSwaptionCashFixedIborDefinition(swaption, data);
  }

  @Override
  public RESULT_TYPE visitSwaptionCashFixedIborDefinition(final SwaptionCashFixedIborDefinition swaption) {
    return _delegate.visitSwaptionCashFixedIborDefinition(swaption);
  }

  @Override
  public RESULT_TYPE visitSwaptionPhysicalFixedIborDefinition(final SwaptionPhysicalFixedIborDefinition swaption, final DATA_TYPE data) {
    return _delegate.visitSwaptionPhysicalFixedIborDefinition(swaption, data);
  }

  @Override
  public RESULT_TYPE visitSwaptionPhysicalFixedIborDefinition(final SwaptionPhysicalFixedIborDefinition swaption) {
    return _delegate.visitSwaptionPhysicalFixedIborDefinition(swaption);
  }

  @Override
  public RESULT_TYPE visitSwaptionPhysicalFixedIborSpreadDefinition(final SwaptionPhysicalFixedIborSpreadDefinition swaption, final DATA_TYPE data) {
    return _delegate.visitSwaptionPhysicalFixedIborSpreadDefinition(swaption, data);
  }

  @Override
  public RESULT_TYPE visitSwaptionPhysicalFixedIborSpreadDefinition(final SwaptionPhysicalFixedIborSpreadDefinition swaption) {
    return _delegate.visitSwaptionPhysicalFixedIborSpreadDefinition(swaption);
  }

  @Override
  public RESULT_TYPE visitSwaptionBermudaFixedIborDefinition(final SwaptionBermudaFixedIborDefinition swaption, final DATA_TYPE data) {
    return _delegate.visitSwaptionBermudaFixedIborDefinition(swaption, data);
  }

  @Override
  public RESULT_TYPE visitSwaptionBermudaFixedIborDefinition(final SwaptionBermudaFixedIborDefinition swaption) {
    return _delegate.visitSwaptionBermudaFixedIborDefinition(swaption);
  }

  @Override
  public RESULT_TYPE visitSwaptionCashFixedONCompoundingDefinition(final SwaptionCashFixedCompoundedONCompoundingDefinition swaption, final DATA_TYPE data) {
    return _delegate.visitSwaptionCashFixedONCompoundingDefinition(swaption, data);
  }

  @Override
  public RESULT_TYPE visitSwaptionCashFixedONCompoundingDefinition(final SwaptionCashFixedCompoundedONCompoundingDefinition swaption) {
    return _delegate.visitSwaptionCashFixedONCompoundingDefinition(swaption);
  }

  @Override
  public RESULT_TYPE visitCouponInflationZeroCouponFirstOfMonth(final CouponInflationZeroCouponMonthlyDefinition coupon, final DATA_TYPE data) {
    return _delegate.visitCouponInflationZeroCouponFirstOfMonth(coupon, data);
  }

  @Override
  public RESULT_TYPE visitCouponInflationZeroCouponFirstOfMonth(final CouponInflationZeroCouponMonthlyDefinition coupon) {
    return _delegate.visitCouponInflationZeroCouponFirstOfMonth(coupon);
  }

  @Override
  public RESULT_TYPE visitCouponInflationZeroCouponInterpolation(final CouponInflationZeroCouponInterpolationDefinition coupon, final DATA_TYPE data) {
    return _delegate.visitCouponInflationZeroCouponInterpolation(coupon, data);
  }

  @Override
  public RESULT_TYPE visitCouponInflationZeroCouponInterpolation(final CouponInflationZeroCouponInterpolationDefinition coupon) {
    return _delegate.visitCouponInflationZeroCouponInterpolation(coupon);
  }

  @Override
  public RESULT_TYPE visitCouponInflationZeroCouponMonthlyGearing(final CouponInflationZeroCouponMonthlyGearingDefinition coupon, final DATA_TYPE data) {
    return _delegate.visitCouponInflationZeroCouponMonthlyGearing(coupon, data);
  }

  @Override
  public RESULT_TYPE visitCouponInflationZeroCouponMonthlyGearing(final CouponInflationZeroCouponMonthlyGearingDefinition coupon) {
    return _delegate.visitCouponInflationZeroCouponMonthlyGearing(coupon);
  }

  @Override
  public RESULT_TYPE visitCouponInflationZeroCouponInterpolationGearing(final CouponInflationZeroCouponInterpolationGearingDefinition coupon, final DATA_TYPE data) {
    return _delegate.visitCouponInflationZeroCouponInterpolationGearing(coupon, data);
  }

  @Override
  public RESULT_TYPE visitCouponInflationZeroCouponInterpolationGearing(final CouponInflationZeroCouponInterpolationGearingDefinition coupon) {
    return _delegate.visitCouponInflationZeroCouponInterpolationGearing(coupon);
  }

  @Override
  public RESULT_TYPE visitCouponInflationYearOnYearFirstOfMonth(final CouponInflationYearOnYearMonthlyDefinition coupon, final DATA_TYPE data) {
    return _delegate.visitCouponInflationYearOnYearFirstOfMonth(coupon, data);
  }

  @Override
  public RESULT_TYPE visitCouponInflationYearOnYearFirstOfMonth(final CouponInflationYearOnYearMonthlyDefinition coupon) {
    return _delegate.visitCouponInflationYearOnYearFirstOfMonth(coupon);
  }

  @Override
  public RESULT_TYPE visitCouponInflationYearOnYearInterpolationDefinition(final CouponInflationYearOnYearInterpolationDefinition coupon, final DATA_TYPE data) {
    return _delegate.visitCouponInflationYearOnYearInterpolationDefinition(coupon, data);
  }

  @Override
  public RESULT_TYPE visitCouponInflationYearOnYearInterpolationDefinition(final CouponInflationYearOnYearInterpolationDefinition coupon) {
    return _delegate.visitCouponInflationYearOnYearInterpolationDefinition(coupon);
  }

  @Override
  public RESULT_TYPE visitCouponInflationYearOnYearMonthlyWithMargin(final CouponInflationYearOnYearMonthlyWithMarginDefinition coupon, final DATA_TYPE data) {
    return _delegate.visitCouponInflationYearOnYearMonthlyWithMargin(coupon, data);
  }

  @Override
  public RESULT_TYPE visitCouponInflationYearOnYearMonthlyWithMargin(final CouponInflationYearOnYearMonthlyWithMarginDefinition coupon) {
    return _delegate.visitCouponInflationYearOnYearMonthlyWithMargin(coupon);
  }

  @Override
  public RESULT_TYPE visitCouponInflationYearOnYearInterpolationWithMargin(final CouponInflationYearOnYearInterpolationWithMarginDefinition coupon, final DATA_TYPE data) {
    return _delegate.visitCouponInflationYearOnYearInterpolationWithMargin(coupon, data);
  }

  @Override
  public RESULT_TYPE visitCouponInflationYearOnYearInterpolationWithMargin(final CouponInflationYearOnYearInterpolationWithMarginDefinition coupon) {
    return _delegate.visitCouponInflationYearOnYearInterpolationWithMargin(coupon);
  }

  @Override
  public RESULT_TYPE visitCapFloorInflationZeroCouponInterpolationDefinition(final CapFloorInflationZeroCouponInterpolationDefinition coupon, final DATA_TYPE data) {
    return _delegate.visitCapFloorInflationZeroCouponInterpolationDefinition(coupon, data);
  }

  @Override
  public RESULT_TYPE visitCapFloorInflationZeroCouponInterpolationDefinition(final CapFloorInflationZeroCouponInterpolationDefinition coupon) {
    return _delegate.visitCapFloorInflationZeroCouponInterpolationDefinition(coupon);
  }

  @Override
  public RESULT_TYPE visitCapFloorInflationZeroCouponMonthlyDefinition(final CapFloorInflationZeroCouponMonthlyDefinition coupon, final DATA_TYPE data) {
    return _delegate.visitCapFloorInflationZeroCouponMonthlyDefinition(coupon, data);
  }

  @Override
  public RESULT_TYPE visitCapFloorInflationZeroCouponMonthlyDefinition(final CapFloorInflationZeroCouponMonthlyDefinition coupon) {
    return _delegate.visitCapFloorInflationZeroCouponMonthlyDefinition(coupon);
  }

  @Override
  public RESULT_TYPE visitCapFloorInflationYearOnYearInterpolationDefinition(final CapFloorInflationYearOnYearInterpolationDefinition coupon, final DATA_TYPE data) {
    return _delegate.visitCapFloorInflationYearOnYearInterpolationDefinition(coupon, data);
  }

  @Override
  public RESULT_TYPE visitCapFloorInflationYearOnYearInterpolationDefinition(final CapFloorInflationYearOnYearInterpolationDefinition coupon) {
    return _delegate.visitCapFloorInflationYearOnYearInterpolationDefinition(coupon);
  }

  @Override
  public RESULT_TYPE visitCapFloorInflationYearOnYearMonthlyDefinition(final CapFloorInflationYearOnYearMonthlyDefinition coupon, final DATA_TYPE data) {
    return _delegate.visitCapFloorInflationYearOnYearMonthlyDefinition(coupon, data);
  }

  @Override
  public RESULT_TYPE visitCapFloorInflationYearOnYearMonthlyDefinition(final CapFloorInflationYearOnYearMonthlyDefinition coupon) {
    return _delegate.visitCapFloorInflationYearOnYearMonthlyDefinition(coupon);
  }

  @Override
  public RESULT_TYPE visitBondCapitalIndexedSecurity(final BondCapitalIndexedSecurityDefinition<?> bond, final DATA_TYPE data) {
    return _delegate.visitBondCapitalIndexedSecurity(bond, data);
  }

  @Override
  public RESULT_TYPE visitBondCapitalIndexedSecurity(final BondCapitalIndexedSecurityDefinition<?> bond) {
    return _delegate.visitBondCapitalIndexedSecurity(bond);
  }

  @Override
  public RESULT_TYPE visitBondInterestIndexedSecurity(final BondInterestIndexedSecurityDefinition<?, ?> bond, final DATA_TYPE data) {
    return _delegate.visitBondInterestIndexedSecurity(bond, data);
  }

  @Override
  public RESULT_TYPE visitBondInterestIndexedSecurity(final BondInterestIndexedSecurityDefinition<?, ?> bond) {
    return _delegate.visitBondInterestIndexedSecurity(bond);
  }

  @Override
  public RESULT_TYPE visitBondCapitalIndexedTransaction(final BondCapitalIndexedTransactionDefinition<?> bond, final DATA_TYPE data) {
    return _delegate.visitBondCapitalIndexedTransaction(bond, data);
  }

  @Override
  public RESULT_TYPE visitBondCapitalIndexedTransaction(final BondCapitalIndexedTransactionDefinition<?> bond) {
    return _delegate.visitBondCapitalIndexedTransaction(bond);
  }

  @Override
  public RESULT_TYPE visitBondInterestIndexedTransaction(final BondInterestIndexedTransactionDefinition<?, ?> bond, final DATA_TYPE data) {
    return _delegate.visitBondInterestIndexedTransaction(bond, data);
  }

  @Override
  public RESULT_TYPE visitBondInterestIndexedTransaction(final BondInterestIndexedTransactionDefinition<?, ?> bond) {
    return _delegate.visitBondInterestIndexedTransaction(bond);
  }

  @Override
  public RESULT_TYPE visitCDSDefinition(final ISDACDSDefinition cds, final DATA_TYPE data) {
    return _delegate.visitCDSDefinition(cds, data);
  }

  @Override
  public RESULT_TYPE visitCDSDefinition(final ISDACDSDefinition cds) {
    return _delegate.visitCDSDefinition(cds);
  }

  @Override
  public RESULT_TYPE visitForexDefinition(final ForexDefinition fx, final DATA_TYPE data) {
    return _delegate.visitForexDefinition(fx, data);
  }

  @Override
  public RESULT_TYPE visitForexDefinition(final ForexDefinition fx) {
    return _delegate.visitForexDefinition(fx);
  }

  @Override
  public RESULT_TYPE visitForexSwapDefinition(final ForexSwapDefinition fx, final DATA_TYPE data) {
    return _delegate.visitForexSwapDefinition(fx, data);
  }

  @Override
  public RESULT_TYPE visitForexSwapDefinition(final ForexSwapDefinition fx) {
    return _delegate.visitForexSwapDefinition(fx);
  }

  @Override
  public RESULT_TYPE visitForexOptionVanillaDefinition(final ForexOptionVanillaDefinition fx, final DATA_TYPE data) {
    return _delegate.visitForexOptionVanillaDefinition(fx, data);
  }

  @Override
  public RESULT_TYPE visitForexOptionVanillaDefinition(final ForexOptionVanillaDefinition fx) {
    return _delegate.visitForexOptionVanillaDefinition(fx);
  }

  @Override
  public RESULT_TYPE visitForexOptionSingleBarrierDefiniton(final ForexOptionSingleBarrierDefinition fx, final DATA_TYPE data) {
    return _delegate.visitForexOptionSingleBarrierDefiniton(fx, data);
  }

  @Override
  public RESULT_TYPE visitForexOptionSingleBarrierDefiniton(final ForexOptionSingleBarrierDefinition fx) {
    return _delegate.visitForexOptionSingleBarrierDefiniton(fx);
  }

  @Override
  public RESULT_TYPE visitForexNonDeliverableForwardDefinition(final ForexNonDeliverableForwardDefinition ndf, final DATA_TYPE data) {
    return _delegate.visitForexNonDeliverableForwardDefinition(ndf, data);
  }

  @Override
  public RESULT_TYPE visitForexNonDeliverableForwardDefinition(final ForexNonDeliverableForwardDefinition ndf) {
    return _delegate.visitForexNonDeliverableForwardDefinition(ndf);
  }

  @Override
  public RESULT_TYPE visitForexNonDeliverableOptionDefinition(final ForexNonDeliverableOptionDefinition ndo, final DATA_TYPE data) {
    return _delegate.visitForexNonDeliverableOptionDefinition(ndo, data);
  }

  @Override
  public RESULT_TYPE visitForexNonDeliverableOptionDefinition(final ForexNonDeliverableOptionDefinition ndo) {
    return _delegate.visitForexNonDeliverableOptionDefinition(ndo);
  }

  @Override
  public RESULT_TYPE visitForexOptionDigitalDefinition(final ForexOptionDigitalDefinition fx, final DATA_TYPE data) {
    return _delegate.visitForexOptionDigitalDefinition(fx, data);
  }

  @Override
  public RESULT_TYPE visitForexOptionDigitalDefinition(final ForexOptionDigitalDefinition fx) {
    return _delegate.visitForexOptionDigitalDefinition(fx);
  }

  @Override
  public RESULT_TYPE visitMetalForwardDefinition(final MetalForwardDefinition future, final DATA_TYPE data) {
    return _delegate.visitMetalForwardDefinition(future, data);
  }

  @Override
  public RESULT_TYPE visitMetalForwardDefinition(final MetalForwardDefinition future) {
    return _delegate.visitMetalForwardDefinition(future);
  }

  @Override
  public RESULT_TYPE visitMetalFutureDefinition(final MetalFutureDefinition future, final DATA_TYPE data) {
    return _delegate.visitMetalFutureDefinition(future, data);
  }

  @Override
  public RESULT_TYPE visitMetalFutureDefinition(final MetalFutureDefinition future) {
    return _delegate.visitMetalFutureDefinition(future);
  }

  @Override
  public RESULT_TYPE visitMetalFutureOptionDefinition(final MetalFutureOptionDefinition future, final DATA_TYPE data) {
    return _delegate.visitMetalFutureOptionDefinition(future, data);
  }

  @Override
  public RESULT_TYPE visitMetalFutureOptionDefinition(final MetalFutureOptionDefinition future) {
    return _delegate.visitMetalFutureOptionDefinition(future);
  }

  @Override
  public RESULT_TYPE visitAgricultureForwardDefinition(final AgricultureForwardDefinition future, final DATA_TYPE data) {
    return _delegate.visitAgricultureForwardDefinition(future, data);
  }

  @Override
  public RESULT_TYPE visitAgricultureForwardDefinition(final AgricultureForwardDefinition future) {
    return _delegate.visitAgricultureForwardDefinition(future);
  }

  @Override
  public RESULT_TYPE visitAgricultureFutureDefinition(final AgricultureFutureDefinition future, final DATA_TYPE data) {
    return _delegate.visitAgricultureFutureDefinition(future, data);
  }

  @Override
  public RESULT_TYPE visitAgricultureFutureDefinition(final AgricultureFutureDefinition future) {
    return _delegate.visitAgricultureFutureDefinition(future);
  }

  @Override
  public RESULT_TYPE visitAgricultureFutureOptionDefinition(final AgricultureFutureOptionDefinition future, final DATA_TYPE data) {
    return _delegate.visitAgricultureFutureOptionDefinition(future, data);
  }

  @Override
  public RESULT_TYPE visitAgricultureFutureOptionDefinition(final AgricultureFutureOptionDefinition future) {
    return _delegate.visitAgricultureFutureOptionDefinition(future);
  }

  @Override
  public RESULT_TYPE visitEnergyForwardDefinition(final EnergyForwardDefinition future, final DATA_TYPE data) {
    return _delegate.visitEnergyForwardDefinition(future, data);
  }

  @Override
  public RESULT_TYPE visitEnergyForwardDefinition(final EnergyForwardDefinition future) {
    return _delegate.visitEnergyForwardDefinition(future);
  }

  @Override
  public RESULT_TYPE visitEnergyFutureDefinition(final EnergyFutureDefinition future, final DATA_TYPE data) {
    return _delegate.visitEnergyFutureDefinition(future, data);
  }

  @Override
  public RESULT_TYPE visitEnergyFutureDefinition(final EnergyFutureDefinition future) {
    return _delegate.visitEnergyFutureDefinition(future);
  }

  @Override
  public RESULT_TYPE visitEnergyFutureOptionDefinition(final EnergyFutureOptionDefinition future, final DATA_TYPE data) {
    return _delegate.visitEnergyFutureOptionDefinition(future, data);
  }

  @Override
  public RESULT_TYPE visitEnergyFutureOptionDefinition(final EnergyFutureOptionDefinition future) {
    return _delegate.visitEnergyFutureOptionDefinition(future);
  }

  @Override
  public RESULT_TYPE visitEquityFutureDefinition(final EquityFutureDefinition future, final DATA_TYPE data) {
    return _delegate.visitEquityFutureDefinition(future, data);
  }

  @Override
  public RESULT_TYPE visitEquityFutureDefinition(final EquityFutureDefinition future) {
    return _delegate.visitEquityFutureDefinition(future);
  }

  @Override
  public RESULT_TYPE visitEquityIndexDividendFutureDefinition(final EquityIndexDividendFutureDefinition future, final DATA_TYPE data) {
    return _delegate.visitEquityIndexDividendFutureDefinition(future, data);
  }

  @Override
  public RESULT_TYPE visitEquityIndexDividendFutureDefinition(final EquityIndexDividendFutureDefinition future) {
    return _delegate.visitEquityIndexDividendFutureDefinition(future);
  }

  @Override
  public RESULT_TYPE visitEquityIndexOptionDefinition(final EquityIndexOptionDefinition option, final DATA_TYPE data) {
    return _delegate.visitEquityIndexOptionDefinition(option, data);
  }

  @Override
  public RESULT_TYPE visitEquityIndexOptionDefinition(final EquityIndexOptionDefinition option) {
    return _delegate.visitEquityIndexOptionDefinition(option);
  }

  @Override
  public RESULT_TYPE visitEquityIndexFutureOptionDefinition(final EquityIndexFutureOptionDefinition option, final DATA_TYPE data) {
    return _delegate.visitEquityIndexFutureOptionDefinition(option, data);
  }

  @Override
  public RESULT_TYPE visitEquityIndexFutureOptionDefinition(final EquityIndexFutureOptionDefinition option) {
    return _delegate.visitEquityIndexFutureOptionDefinition(option);
  }

  @Override
  public RESULT_TYPE visitEquityOptionDefinition(final EquityOptionDefinition option, final DATA_TYPE data) {
    return _delegate.visitEquityOptionDefinition(option, data);
  }

  @Override
  public RESULT_TYPE visitEquityOptionDefinition(final EquityOptionDefinition option) {
    return _delegate.visitEquityOptionDefinition(option);
  }

  @Override
  public RESULT_TYPE visitVarianceSwapDefinition(final VarianceSwapDefinition varianceSwap) {
    return _delegate.visitVarianceSwapDefinition(varianceSwap);
  }

  @Override
  public RESULT_TYPE visitVarianceSwapDefinition(final VarianceSwapDefinition varianceSwap, final DATA_TYPE data) {
    return _delegate.visitVarianceSwapDefinition(varianceSwap, data);
  }

  @Override
  public RESULT_TYPE visitEquityVarianceSwapDefinition(final EquityVarianceSwapDefinition varianceSwap) {
    return _delegate.visitEquityVarianceSwapDefinition(varianceSwap);
  }

  @Override
  public RESULT_TYPE visitEquityVarianceSwapDefinition(final EquityVarianceSwapDefinition varianceSwap, final DATA_TYPE data) {
    return _delegate.visitEquityVarianceSwapDefinition(varianceSwap, data);
  }

  @Override
  public RESULT_TYPE visitIndexFutureDefinition(final IndexFutureDefinition future, final DATA_TYPE data) {
    return _delegate.visitIndexFutureDefinition(future, data);
  }

  @Override
  public RESULT_TYPE visitIndexFutureDefinition(final IndexFutureDefinition future) {
    return _delegate.visitIndexFutureDefinition(future);
  }

  @Override
  public RESULT_TYPE visitEquityIndexFutureDefinition(final EquityIndexFutureDefinition future, final DATA_TYPE data) {
    return _delegate.visitEquityIndexFutureDefinition(future, data);
  }

  @Override
  public RESULT_TYPE visitEquityIndexFutureDefinition(final EquityIndexFutureDefinition future) {
    return _delegate.visitEquityIndexFutureDefinition(future);
  }

  @Override
  public RESULT_TYPE visitVolatilityIndexFutureDefinition(final VolatilityIndexFutureDefinition future, final DATA_TYPE data) {
    return _delegate.visitVolatilityIndexFutureDefinition(future, data);
  }

  @Override
  public RESULT_TYPE visitVolatilityIndexFutureDefinition(final VolatilityIndexFutureDefinition future) {
    return _delegate.visitVolatilityIndexFutureDefinition(future);
  }

}
