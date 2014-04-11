/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.calculator.PresentValueMCACalculator;
import com.opengamma.analytics.financial.forex.derivative.ForexSwap;
import com.opengamma.analytics.financial.forex.method.ForexSwapDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.bond.definition.BillTransaction;
import com.opengamma.analytics.financial.interestrate.bond.method.BillTransactionDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.cash.derivative.Cash;
import com.opengamma.analytics.financial.interestrate.cash.derivative.DepositCounterpart;
import com.opengamma.analytics.financial.interestrate.cash.derivative.DepositZero;
import com.opengamma.analytics.financial.interestrate.cash.method.CashDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.cash.method.DepositZeroDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.fra.derivative.ForwardRateAgreement;
import com.opengamma.analytics.financial.interestrate.fra.method.ForwardRateAgreementDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureTransaction;
import com.opengamma.analytics.financial.interestrate.future.method.InterestRateFutureSecurityDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.swap.derivative.Swap;
import com.opengamma.analytics.financial.interestrate.swap.derivative.SwapFixedCoupon;

/**
 * Compute the spread to be added to the market standard quote of the instrument for which the present value of the instrument is zero.
 * The notion of "market quote" will depend of each instrument.
 * @deprecated This class uses deprecated pricing methods.
 */
@Deprecated
public final class ParSpreadMarketQuoteCalculator extends InstrumentDerivativeVisitorAdapter<YieldCurveBundle, Double> {

  /**
   * The unique instance of the calculator.
   */
  private static final ParSpreadMarketQuoteCalculator INSTANCE = new ParSpreadMarketQuoteCalculator();

  /**
   * Gets the calculator instance.
   * @return The calculator.
   */
  public static ParSpreadMarketQuoteCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Constructor.
   */
  private ParSpreadMarketQuoteCalculator() {
  }

  /**
   * The methods and calculators.
   */
  private static final PresentValueMCACalculator PVMCC = PresentValueMCACalculator.getInstance();
  private static final PresentValueBasisPointCalculator PVBPC = PresentValueBasisPointCalculator.getInstance();
  private static final CashDiscountingMethod METHOD_DEPOSIT = CashDiscountingMethod.getInstance();
  private static final DepositZeroDiscountingMethod METHOD_DEPOSIT_ZERO = DepositZeroDiscountingMethod.getInstance();
  private static final BillTransactionDiscountingMethod METHOD_BILL_TRANSACTION = BillTransactionDiscountingMethod.getInstance();
  private static final ForwardRateAgreementDiscountingMethod METHOD_FRA = ForwardRateAgreementDiscountingMethod.getInstance();
  private static final InterestRateFutureSecurityDiscountingMethod METHOD_IR_FUTURES_SECURITY = InterestRateFutureSecurityDiscountingMethod.getInstance();
  private static final ForexSwapDiscountingMethod METHOD_FX_SWAP = ForexSwapDiscountingMethod.getInstance();

  //     -----     Deposit     -----

  @Override
  public Double visitCash(final Cash deposit, final YieldCurveBundle curves) {
    return METHOD_DEPOSIT.parSpread(deposit, curves);
  }

  @Override
  public Double visitDepositZero(final DepositZero deposit, final YieldCurveBundle curves) {
    return METHOD_DEPOSIT_ZERO.parSpread(deposit, curves);
  }

  @Override
  public Double visitDepositCounterpart(final DepositCounterpart deposit, final YieldCurveBundle curves) {
    return METHOD_DEPOSIT.parSpread(deposit, curves);
  }

  //     -----     Bill & bonds     -----

  @Override
  public Double visitBillTransaction(final BillTransaction bill, final YieldCurveBundle curves) {
    return METHOD_BILL_TRANSACTION.parSpread(bill, curves);
  }

  //-----     Swaps     -----

  /**
   * For swaps the ParSpread is the spread to be added on each coupon of the first leg to obtain a present value of zero.
   * It is computed as the opposite of the present value of the swap in currency of the first leg divided by the present value of a basis point
   * of the first leg (as computed by the PresentValueBasisPointCalculator).
   * @param swap The swap.
   * @param curves The yield curve bundle.
   * @return The par spread.
   */
  @Override
  public Double visitSwap(final Swap<?, ?> swap, final YieldCurveBundle curves) {
    Validate.notNull(curves);
    Validate.notNull(swap);
    return -curves.getFxRates().convert(swap.accept(PVMCC, curves), swap.getFirstLeg().getCurrency()).getAmount() / swap.getFirstLeg().accept(PVBPC, curves);
  }

  @Override
  public Double visitFixedCouponSwap(final SwapFixedCoupon<?> swap, final YieldCurveBundle curves) {
    return visitSwap(swap, curves);
  }

  /**
   * For ForwardRateAgreement the ParSpread is the spread to be added to the fixed rate to obtain a present value of zero.
   * @param fra The forward rate agreement.
   * @param curves The yield curve bundle.
   * @return The par spread.
   */
  @Override
  public Double visitForwardRateAgreement(final ForwardRateAgreement fra, final YieldCurveBundle curves) {
    Validate.notNull(curves);
    Validate.notNull(fra);
    return METHOD_FRA.parSpread(fra, curves);
  }

  //     -----     Futures     -----

  /**
   * For InterestRateFutures the ParSpread is the spread to be added to the reference price to obtain a present value of zero.
   * @param future The futures.
   * @param curves The yield curve bundle.
   * @return The par spread.
   */
  @Override
  public Double visitInterestRateFutureTransaction(final InterestRateFutureTransaction future, final YieldCurveBundle curves) {
    return METHOD_IR_FUTURES_SECURITY.price(future.getUnderlyingSecurity(), curves) - future.getReferencePrice();
  }

  //  @Override
  //  public Double visitInterestRateFutureSecurity(final InterestRateFutureSecurity future, final YieldCurveBundle curves) {
  //    return METHOD_IR_FUTURES_SECURITY.price(future, curves);
  //  }

  //     -----     Forex     -----

  /**
   * The par spread is the spread that should be added to the forex forward points to have a zero value.
   * @param fx The forex swap.
   * @param curves The yield curve bundle with the relevant exchange rates.
   * @return The spread.
   */
  @Override
  public Double visitForexSwap(final ForexSwap fx, final YieldCurveBundle curves) {
    return METHOD_FX_SWAP.parSpread(fx, curves);
  }

}
