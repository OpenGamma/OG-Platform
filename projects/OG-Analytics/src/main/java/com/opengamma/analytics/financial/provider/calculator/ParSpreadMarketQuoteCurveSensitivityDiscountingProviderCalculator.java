/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.calculator;

import com.opengamma.analytics.financial.interestrate.AbstractInstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.interestrate.cash.derivative.Cash;
import com.opengamma.analytics.financial.interestrate.cash.derivative.DepositIbor;
import com.opengamma.analytics.financial.interestrate.cash.provider.CashDiscountingProviderMethod;
import com.opengamma.analytics.financial.interestrate.cash.provider.DepositIborDiscountingProviderMethod;
import com.opengamma.analytics.financial.interestrate.fra.derivative.ForwardRateAgreement;
import com.opengamma.analytics.financial.interestrate.fra.provider.ForwardRateAgreementDiscountingProviderMethod;
import com.opengamma.analytics.financial.interestrate.market.description.CurveSensitivityMarket;
import com.opengamma.analytics.financial.interestrate.market.description.MultipleCurrencyCurveSensitivityMarket;
import com.opengamma.analytics.financial.interestrate.swap.derivative.Swap;
import com.opengamma.analytics.financial.interestrate.swap.derivative.SwapFixedCoupon;
import com.opengamma.analytics.financial.provider.description.MulticurveProviderInterface;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * Compute the sensitivity of the spread to the curve; the spread is the number to be added to the market standard quote of the instrument for which the present value of the instrument is zero.
 * The notion of "spread" will depend of each instrument.
 * @author marc
 */
public final class ParSpreadMarketQuoteCurveSensitivityDiscountingProviderCalculator extends AbstractInstrumentDerivativeVisitor<MulticurveProviderInterface, CurveSensitivityMarket> {

  /**
   * The unique instance of the calculator.
   */
  private static final ParSpreadMarketQuoteCurveSensitivityDiscountingProviderCalculator INSTANCE = new ParSpreadMarketQuoteCurveSensitivityDiscountingProviderCalculator();

  /**
   * Gets the calculator instance.
   * @return The calculator.
   */
  public static ParSpreadMarketQuoteCurveSensitivityDiscountingProviderCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Constructor.
   */
  private ParSpreadMarketQuoteCurveSensitivityDiscountingProviderCalculator() {
  }

  /**
   * The methods and calculators.
   */
  private static final PresentValueDiscountingProviderCalculator PVMC = PresentValueDiscountingProviderCalculator.getInstance();
  private static final PresentValueCurveSensitivityDiscountingProviderCalculator PVCSMC = PresentValueCurveSensitivityDiscountingProviderCalculator.getInstance();
  private static final PresentValueMarketQuoteSensitivityMarketCalculator PVMQSMC = PresentValueMarketQuoteSensitivityMarketCalculator.getInstance();
  private static final PresentValueMarketQuoteSensitivityCurveSensitivityMarketCalculator PVMQSCSMC = PresentValueMarketQuoteSensitivityCurveSensitivityMarketCalculator.getInstance();
  private static final CashDiscountingProviderMethod METHOD_DEPOSIT = CashDiscountingProviderMethod.getInstance();
  private static final DepositIborDiscountingProviderMethod METHOD_DEPOSIT_IBOR = DepositIborDiscountingProviderMethod.getInstance();
  private static final ForwardRateAgreementDiscountingProviderMethod METHOD_FRA = ForwardRateAgreementDiscountingProviderMethod.getInstance();

  //     -----     Deposit     -----

  @Override
  public CurveSensitivityMarket visitCash(final Cash deposit, final MulticurveProviderInterface multicurve) {
    return METHOD_DEPOSIT.parSpreadCurveSensitivity(deposit, multicurve);
  }

  @Override
  public CurveSensitivityMarket visitDepositIbor(final DepositIbor deposit, final MulticurveProviderInterface multicurve) {
    return METHOD_DEPOSIT_IBOR.parSpreadCurveSensitivity(deposit, multicurve);
  }

  // -----     Payment/Coupon     ------

  @Override
  public CurveSensitivityMarket visitForwardRateAgreement(final ForwardRateAgreement fra, final MulticurveProviderInterface multicurve) {
    return METHOD_FRA.parSpreadCurveSensitivity(fra, multicurve);
  }

  //     -----     Swaps     -----

  @Override
  public CurveSensitivityMarket visitSwap(final Swap<?, ?> swap, final MulticurveProviderInterface multicurve) {
    ArgumentChecker.notNull(multicurve, "multicurve");
    ArgumentChecker.notNull(swap, "Swap");
    final Currency ccy1 = swap.getFirstLeg().getCurrency();
    final MultipleCurrencyCurveSensitivityMarket pvcs = PVCSMC.visit(swap, multicurve);
    final CurveSensitivityMarket pvcs1 = pvcs.converted(ccy1, multicurve.getFxRates()).getSensitivity(ccy1);
    final CurveSensitivityMarket pvmqscs = PVMQSCSMC.visit(swap.getFirstLeg(), multicurve);
    final double pvmqs = PVMQSMC.visit(swap.getFirstLeg(), multicurve);
    final double pv = multicurve.getFxRates().convert(PVMC.visit(swap, multicurve), ccy1).getAmount();
    // Implementation note: Total pv in currency 1.
    return pvcs1.multipliedBy(-1.0 / pvmqs).plus(pvmqscs.multipliedBy(pv / (pvmqs * pvmqs)));
  }

  @Override
  public CurveSensitivityMarket visitFixedCouponSwap(final SwapFixedCoupon<?> swap, final MulticurveProviderInterface multicurve) {
    return visitSwap(swap, multicurve);
  }
}
