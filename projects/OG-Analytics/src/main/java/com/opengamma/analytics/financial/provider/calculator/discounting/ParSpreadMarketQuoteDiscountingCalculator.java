/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.calculator.discounting;

import com.opengamma.analytics.financial.forex.derivative.ForexSwap;
import com.opengamma.analytics.financial.forex.provider.ForexSwapDiscountingProviderMethod;
import com.opengamma.analytics.financial.interestrate.AbstractInstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.cash.derivative.Cash;
import com.opengamma.analytics.financial.interestrate.cash.derivative.DepositIbor;
import com.opengamma.analytics.financial.interestrate.cash.provider.CashDiscountingProviderMethod;
import com.opengamma.analytics.financial.interestrate.cash.provider.DepositIborDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.fra.derivative.ForwardRateAgreement;
import com.opengamma.analytics.financial.interestrate.fra.provider.ForwardRateAgreementDiscountingProviderMethod;
import com.opengamma.analytics.financial.interestrate.swap.derivative.Swap;
import com.opengamma.analytics.financial.interestrate.swap.derivative.SwapFixedCoupon;
import com.opengamma.analytics.financial.provider.description.MulticurveProviderInterface;
import com.opengamma.util.ArgumentChecker;

/**
 * Compute the spread to be added to the market standard quote of the instrument for which the present value of the instrument is zero.
 * The notion of "market quote" will depend of each instrument.
 * @author marc
 */
public final class ParSpreadMarketQuoteDiscountingCalculator extends AbstractInstrumentDerivativeVisitor<MulticurveProviderInterface, Double> {

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
  private static final PresentValueDiscountingCalculator PVMC = PresentValueDiscountingCalculator.getInstance();
  private static final PresentValueMarketQuoteSensitivityDiscountingCalculator PVMQSC = PresentValueMarketQuoteSensitivityDiscountingCalculator.getInstance();
  private static final CashDiscountingProviderMethod METHOD_DEPOSIT = CashDiscountingProviderMethod.getInstance();
  private static final DepositIborDiscountingMethod METHOD_DEPOSIT_IBOR = DepositIborDiscountingMethod.getInstance();
  private static final ForwardRateAgreementDiscountingProviderMethod METHOD_FRA = ForwardRateAgreementDiscountingProviderMethod.getInstance();
  private static final ForexSwapDiscountingProviderMethod METHOD_FOREX_SWAP = ForexSwapDiscountingProviderMethod.getInstance();

  @Override
  public Double visit(final InstrumentDerivative derivative, final MulticurveProviderInterface multicurve) {
    ArgumentChecker.notNull(multicurve, "Multi-curves");
    ArgumentChecker.notNull(derivative, "Derivatives");
    return derivative.accept(this, multicurve);
  }

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
   * of the first leg (as computed by the PresentValueBasisPointCalculator).
   * @param swap The swap.
   * @param multicurves The multi-curves provider.
   * @return The par spread.
   */
  @Override
  public Double visitSwap(final Swap<?, ?> swap, final MulticurveProviderInterface multicurves) {
    ArgumentChecker.notNull(multicurves, "Market");
    ArgumentChecker.notNull(swap, "Swap");
    return -multicurves.getFxRates().convert(PVMC.visit(swap, multicurves), swap.getFirstLeg().getCurrency()).getAmount() / PVMQSC.visit(swap.getFirstLeg(), multicurves);
  }

  @Override
  public Double visitFixedCouponSwap(final SwapFixedCoupon<?> swap, final MulticurveProviderInterface multicurve) {
    return visitSwap(swap, multicurve);
  }

  //     -----     Forex     -----

  /**
   * The par spread is the spread that should be added to the forex forward points to have a zero value.
   * @param fx The forex swap.
   * @param multicurves The multi-curves provider.
   * @return The spread.
   */
  @Override
  public Double visitForexSwap(final ForexSwap fx, final MulticurveProviderInterface multicurves) {
    return METHOD_FOREX_SWAP.parSpread(fx, multicurves);
  }

}
