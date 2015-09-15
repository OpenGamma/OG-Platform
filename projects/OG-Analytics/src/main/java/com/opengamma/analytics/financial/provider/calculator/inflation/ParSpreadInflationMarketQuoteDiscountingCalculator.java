/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.analytics.financial.provider.calculator.inflation;

import com.opengamma.analytics.financial.forex.derivative.ForexSwap;
import com.opengamma.analytics.financial.forex.provider.ForexSwapDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.interestrate.cash.derivative.Cash;
import com.opengamma.analytics.financial.interestrate.cash.derivative.DepositIbor;
import com.opengamma.analytics.financial.interestrate.cash.provider.CashDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.cash.provider.DepositIborDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.fra.derivative.ForwardRateAgreement;
import com.opengamma.analytics.financial.interestrate.fra.provider.ForwardRateAgreementDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureTransaction;
import com.opengamma.analytics.financial.interestrate.future.provider.InterestRateFutureSecurityDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.inflation.derivative.CouponInflation;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixedCompounding;
import com.opengamma.analytics.financial.interestrate.swap.derivative.Swap;
import com.opengamma.analytics.financial.interestrate.swap.derivative.SwapFixedCoupon;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueMarketQuoteSensitivityDiscountingCalculator;
import com.opengamma.analytics.financial.provider.description.inflation.ParameterInflationProviderInterface;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.util.ArgumentChecker;

/**
 * Compute the spread to be added to the market standard quote of the instrument for which the present value of the instrument is zero.
 * The notion of "market quote" will depend of each instrument.
 */
public final class ParSpreadInflationMarketQuoteDiscountingCalculator 
  extends InstrumentDerivativeVisitorAdapter<ParameterInflationProviderInterface, Double> {

  /**
   * The unique instance of the calculator.
   */
  private static final ParSpreadInflationMarketQuoteDiscountingCalculator INSTANCE = new ParSpreadInflationMarketQuoteDiscountingCalculator();

  /**
   * Gets the calculator instance.
   * @return The calculator.
   */
  public static ParSpreadInflationMarketQuoteDiscountingCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Constructor.
   */
  private ParSpreadInflationMarketQuoteDiscountingCalculator() {
  }

  /**
   * The methods and calculators.
   */
  private static final PresentValueDiscountingInflationCalculator PVIC = PresentValueDiscountingInflationCalculator.getInstance();
  private static final PresentValueDiscountingCalculator PVMC = PresentValueDiscountingCalculator.getInstance();
  private static final PresentValueMarketQuoteSensitivityDiscountingCalculator PVMQSC = PresentValueMarketQuoteSensitivityDiscountingCalculator.getInstance();
  private static final CashDiscountingMethod METHOD_DEPOSIT = CashDiscountingMethod.getInstance();
  private static final DepositIborDiscountingMethod METHOD_DEPOSIT_IBOR = DepositIborDiscountingMethod.getInstance();
  private static final ForwardRateAgreementDiscountingMethod METHOD_FRA = ForwardRateAgreementDiscountingMethod.getInstance();
  private static final InterestRateFutureSecurityDiscountingMethod METHOD_IR_FUT = InterestRateFutureSecurityDiscountingMethod.getInstance();
  private static final ForexSwapDiscountingMethod METHOD_FOREX_SWAP = ForexSwapDiscountingMethod.getInstance();

  //-----     Deposit     -----

  @Override
  public Double visitCash(final Cash deposit, final ParameterInflationProviderInterface inflation) {
    return METHOD_DEPOSIT.parSpread(deposit, inflation.getMulticurveProvider());
  }

  @Override
  public Double visitDepositIbor(final DepositIbor deposit, final ParameterInflationProviderInterface inflation) {
    return METHOD_DEPOSIT_IBOR.parSpread(deposit, inflation.getMulticurveProvider());
  }

  // -----     Payment/Coupon     ------

  @Override
  public Double visitForwardRateAgreement(final ForwardRateAgreement fra, final ParameterInflationProviderInterface inflation) {
    return METHOD_FRA.parSpread(fra, inflation.getMulticurveProvider());
  }

  //-----      Swaps     -----

  /**
  * For swaps the ParSpread is the spread to be added on each coupon of the first leg to obtain a present value of zero.
  * It is computed as the opposite of the present value of the swap in currency of the first leg divided by the present value of a basis point
  * of the first leg (as computed by the PresentValueBasisPointCalculator).
  * @param swap The swap.
  * @param inflation The inflation curves and multi-curves provider.
  * @return The par spread.
  */
  @Override
  public Double visitSwap(final Swap<?, ?> swap, final ParameterInflationProviderInterface inflation) {
    ArgumentChecker.notNull(inflation, "Market");
    ArgumentChecker.notNull(swap, "Swap");
    if (swap.getFirstLeg().getNumberOfPayments() == 1 && swap.getFirstLeg().getNthPayment(0) instanceof CouponFixedCompounding) {
      final CouponFixedCompounding cpn = (CouponFixedCompounding) swap.getFirstLeg().getNthPayment(0);
      final double pvInflationLeg = swap.getSecondLeg().accept(PVIC, inflation).getAmount(swap.getSecondLeg().getCurrency());
      final double discountFactor = inflation.getInflationProvider().getDiscountFactor(swap.getFirstLeg().getCurrency(), cpn.getPaymentTime());
      final double tenor = cpn.getPaymentAccrualFactors().length;
      final double notional = ((CouponInflation) swap.getSecondLeg().getNthPayment(0)).getNotional();
      return Math.pow(pvInflationLeg / discountFactor / notional + 1, 1 / tenor) - 1 - cpn.getFixedRate();
    }
    final MulticurveProviderInterface multicurves = inflation.getMulticurveProvider();
    return -multicurves.getFxRates().convert(swap.accept(PVMC, multicurves), swap.getFirstLeg().getCurrency()).getAmount() 
        / swap.getFirstLeg().accept(PVMQSC, multicurves);
  }

  @Override
  public Double visitFixedCouponSwap(final SwapFixedCoupon<?> swap, final ParameterInflationProviderInterface inflation) {
    return visitSwap(swap, inflation);
  }

  //-----     Futures     -----

  @Override
  public Double visitInterestRateFutureTransaction(final InterestRateFutureTransaction futures, final ParameterInflationProviderInterface inflation) {
    return METHOD_IR_FUT.price(futures.getUnderlyingSecurity(), inflation.getMulticurveProvider()) - futures.getReferencePrice();
  }

  //     -----     Forex     -----

  /**
  * The par spread is the spread that should be added to the forex forward points to have a zero value.
  * @param fx The forex swap.
  * @param inflation The inflation provider.
  * @return The spread.
  */
  @Override
  public Double visitForexSwap(final ForexSwap fx, final ParameterInflationProviderInterface inflation) {
    return METHOD_FOREX_SWAP.parSpread(fx, inflation.getMulticurveProvider());
  }

}
