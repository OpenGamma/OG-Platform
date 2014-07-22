/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.calculator.discounting;

import com.opengamma.analytics.financial.forex.derivative.Forex;
import com.opengamma.analytics.financial.forex.derivative.ForexSwap;
import com.opengamma.analytics.financial.forex.provider.ForexDiscountingMethod;
import com.opengamma.analytics.financial.forex.provider.ForexSwapDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.interestrate.cash.derivative.Cash;
import com.opengamma.analytics.financial.interestrate.cash.derivative.DepositIbor;
import com.opengamma.analytics.financial.interestrate.cash.provider.CashDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.cash.provider.DepositIborDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.fra.derivative.ForwardRateAgreement;
import com.opengamma.analytics.financial.interestrate.fra.provider.ForwardRateAgreementDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.future.derivative.FederalFundsFutureTransaction;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureTransaction;
import com.opengamma.analytics.financial.interestrate.future.provider.FederalFundsFutureSecurityDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.future.provider.InterestRateFutureSecurityDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixedAccruedCompounding;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponONCompounded;
import com.opengamma.analytics.financial.interestrate.swap.derivative.Swap;
import com.opengamma.analytics.financial.interestrate.swap.derivative.SwapFixedCoupon;
import com.opengamma.analytics.financial.interestrate.swap.derivative.SwapMultileg;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.util.ArgumentChecker;

/**
 * Compute the spread to be added to the market standard quote of the instrument for which the present value of the instrument is zero.
 * The notion of "market quote" will depend of each instrument.
 */
public final class ParSpreadMarketQuoteDiscountingCalculator extends InstrumentDerivativeVisitorAdapter<MulticurveProviderInterface, Double> {

  /**
   * The unique instance of the calculator.
   */
  private static final ParSpreadMarketQuoteDiscountingCalculator INSTANCE = new ParSpreadMarketQuoteDiscountingCalculator();

  /**
   * Gets the calculator instance.
   * @return The calculator.
   */
  public static ParSpreadMarketQuoteDiscountingCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Constructor.
   */
  private ParSpreadMarketQuoteDiscountingCalculator() {
  }

  /**
   * The methods and calculators.
   */
  private static final PresentValueDiscountingCalculator PVDC = PresentValueDiscountingCalculator.getInstance();
  private static final PresentValueMarketQuoteSensitivityDiscountingCalculator PVMQSC = PresentValueMarketQuoteSensitivityDiscountingCalculator.getInstance();
  private static final CashDiscountingMethod METHOD_DEPOSIT = CashDiscountingMethod.getInstance();
  private static final DepositIborDiscountingMethod METHOD_DEPOSIT_IBOR = DepositIborDiscountingMethod.getInstance();
  private static final ForwardRateAgreementDiscountingMethod METHOD_FRA = ForwardRateAgreementDiscountingMethod.getInstance();
  private static final InterestRateFutureSecurityDiscountingMethod METHOD_STIR_FUT = InterestRateFutureSecurityDiscountingMethod.getInstance();
  private static final ForexSwapDiscountingMethod METHOD_FOREX_SWAP = ForexSwapDiscountingMethod.getInstance();
  private static final ForexDiscountingMethod METHOD_FOREX = ForexDiscountingMethod.getInstance();
  private static final FederalFundsFutureSecurityDiscountingMethod METHOD_FED_FUNDS = FederalFundsFutureSecurityDiscountingMethod.getInstance();

  //     -----     Deposit     -----

  @Override
  public Double visitCash(final Cash deposit, final MulticurveProviderInterface multicurve) {
    return METHOD_DEPOSIT.parSpread(deposit, multicurve);
  }

  @Override
  public Double visitDepositIbor(final DepositIbor deposit, final MulticurveProviderInterface multicurve) {
    return METHOD_DEPOSIT_IBOR.parSpread(deposit, multicurve);
  }

  // -----     Payment/Coupon     ------

  @Override
  public Double visitForwardRateAgreement(final ForwardRateAgreement fra, final MulticurveProviderInterface multicurve) {
    return METHOD_FRA.parSpread(fra, multicurve);
  }

  //     -----     Swaps     -----

  /**
   * For swaps the ParSpread is the spread to be added on each coupon of the first leg to obtain a present value of zero.
   * It is computed as the opposite of the present value of the swap in currency of the first leg divided by the present value of a basis point
   * of the first leg (as computed by the PresentValueMarketQuoteSensitivityDiscountingCalculator).
   * @param swap The swap.
   * @param multicurves The multi-curves provider.
   * @return The par spread.
   */
  @Override
  public Double visitSwap(final Swap<?, ?> swap, final MulticurveProviderInterface multicurves) {
    ArgumentChecker.notNull(multicurves, "Market");
    ArgumentChecker.notNull(swap, "Swap");

    // Implementation note: if the swap is an On compounded (ie Brazilian like), the parspread formula is not the same.
    if (swap.getSecondLeg().getNthPayment(0) instanceof CouponONCompounded && swap.getFirstLeg().getNthPayment(0) instanceof CouponFixedAccruedCompounding &&
        swap.getFirstLeg().getNumberOfPayments() == 1) {
      // Implementation note: check if the swap is a Brazilian swap. 
      final CouponFixedAccruedCompounding cpnFixed = (CouponFixedAccruedCompounding) swap.getFirstLeg().getNthPayment(0);
      final double pvONLeg = swap.getSecondLeg().accept(PVDC, multicurves).getAmount(swap.getSecondLeg().getCurrency());
      final double discountFactor = multicurves.getDiscountFactor(swap.getFirstLeg().getCurrency(), cpnFixed.getPaymentTime());
      final double paymentYearFraction = cpnFixed.getPaymentYearFraction();
      final double notional = ((CouponONCompounded) swap.getSecondLeg().getNthPayment(0)).getNotional();
      return Math.pow(pvONLeg / discountFactor / notional, 1 / paymentYearFraction) - 1 - cpnFixed.getFixedRate();
    }
    return -multicurves.getFxRates().convert(swap.accept(PVDC, multicurves), swap.getFirstLeg().getCurrency()).getAmount() / swap.getFirstLeg().accept(PVMQSC, multicurves);
  }

  @Override
  public Double visitFixedCouponSwap(final SwapFixedCoupon<?> swap, final MulticurveProviderInterface multicurve) {
    return visitSwap(swap, multicurve);
  }

  /**
   * For multiple legs swaps the ParSpread is the spread to be added on each coupon of the first leg to obtain a present value of zero.
   * It is computed as the opposite of the present value of the swap in currency of the first leg divided by the present value of a basis point
   * of the first leg (as computed by the PresentValueMarketQuoteSensitivityDiscountingCalculator).
   * @param swap The swap with multiple legs.
   * @param multicurves The multi-curve provider.
   * @return The par spread.
   */
  @Override
  public Double visitSwapMultileg(final SwapMultileg swap, final MulticurveProviderInterface multicurves) {
    ArgumentChecker.notNull(multicurves, "Market");
    ArgumentChecker.notNull(swap, "Swap");
    return -multicurves.getFxRates().convert(swap.accept(PVDC, multicurves), swap.getLegs()[0].getCurrency()).getAmount() / swap.getLegs()[0].accept(PVMQSC, multicurves);
  }

  //     -----     Futures     -----

  @Override
  public Double visitInterestRateFutureTransaction(final InterestRateFutureTransaction futures, final MulticurveProviderInterface multicurves) {
    return METHOD_STIR_FUT.price(futures.getUnderlyingSecurity(), multicurves) - futures.getReferencePrice();
  }

  @Override
  public Double visitFederalFundsFutureTransaction(final FederalFundsFutureTransaction future, final MulticurveProviderInterface multicurve) {
    return METHOD_FED_FUNDS.price(future.getUnderlyingSecurity(), multicurve) - future.getReferencePrice();
  }

  //     -----     Forex     -----

  @Override
  public Double visitForexSwap(final ForexSwap fx, final MulticurveProviderInterface multicurves) {
    return METHOD_FOREX_SWAP.parSpread(fx, multicurves);
  }

  @Override
  public Double visitForex(final Forex fx, final MulticurveProviderInterface multicurves) {
    return METHOD_FOREX.parSpread(fx, multicurves);
  }

}
