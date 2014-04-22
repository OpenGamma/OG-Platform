/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.future.calculator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.AnnuityPaymentFixed;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureSecurity;
import com.opengamma.analytics.financial.interestrate.future.derivative.SwapFuturesPriceDeliverableSecurity;
import com.opengamma.analytics.financial.model.interestrate.HullWhiteOneFactorPiecewiseConstantInterestRateModel;
import com.opengamma.analytics.financial.model.interestrate.definition.HullWhiteOneFactorPiecewiseConstantParameters;
import com.opengamma.analytics.financial.provider.calculator.discounting.CashFlowEquivalentCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.CashFlowEquivalentCurveSensitivityCalculator;
import com.opengamma.analytics.financial.provider.description.interestrate.HullWhiteOneFactorProviderInterface;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.ForwardSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MulticurveSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.SimplyCompoundedForwardSensitivity;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Computes the par rate for different instrument. The meaning of "par rate" is instrument dependent.
 */
public final class FuturesPriceCurveSensitivityHullWhiteCalculator extends InstrumentDerivativeVisitorAdapter<HullWhiteOneFactorProviderInterface, MulticurveSensitivity> {

  /**
   * The unique instance of the calculator.
   */
  private static final FuturesPriceCurveSensitivityHullWhiteCalculator INSTANCE = new FuturesPriceCurveSensitivityHullWhiteCalculator();

  /**
   * Gets the calculator instance.
   * @return The calculator.
   */
  public static FuturesPriceCurveSensitivityHullWhiteCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Constructor.
   */
  private FuturesPriceCurveSensitivityHullWhiteCalculator() {
  }

  /**
   * The Hull-White model.
   */
  private static final HullWhiteOneFactorPiecewiseConstantInterestRateModel MODEL = new HullWhiteOneFactorPiecewiseConstantInterestRateModel();
  /**
   * The cash flow equivalent calculator used in computations.
   */
  private static final CashFlowEquivalentCalculator CFEC = CashFlowEquivalentCalculator.getInstance();
  /**
   * The cash flow equivalent curve sensitivity calculator used in computations.
   */
  private static final CashFlowEquivalentCurveSensitivityCalculator CFECSC = CashFlowEquivalentCurveSensitivityCalculator.getInstance();

  //     -----     Futures     -----

  @Override
  public MulticurveSensitivity visitInterestRateFutureSecurity(final InterestRateFutureSecurity futures, final HullWhiteOneFactorProviderInterface multicurve) {
    ArgumentChecker.notNull(futures, "Future");
    ArgumentChecker.notNull(multicurve, "Multi-curves with Hull-White");
    final double futureConvexityFactor = MODEL.futuresConvexityFactor(multicurve.getHullWhiteParameters(), futures.getTradingLastTime(), futures.getFixingPeriodStartTime(),
        futures.getFixingPeriodEndTime());
    // Backward sweep
    final double priceBar = 1.0;
    final double forwardBar = -futureConvexityFactor * priceBar;
    final Map<String, List<ForwardSensitivity>> mapFwd = new HashMap<>();
    final List<ForwardSensitivity> listForward = new ArrayList<>();
    listForward.add(new SimplyCompoundedForwardSensitivity(futures.getFixingPeriodStartTime(), futures.getFixingPeriodEndTime(), futures.getFixingPeriodAccrualFactor(), forwardBar));
    mapFwd.put(multicurve.getMulticurveProvider().getName(futures.getIborIndex()), listForward);
    return MulticurveSensitivity.ofForward(mapFwd);
  }

  @Override
  public MulticurveSensitivity visitSwapFuturesPriceDeliverableSecurity(final SwapFuturesPriceDeliverableSecurity futures, final HullWhiteOneFactorProviderInterface multicurve) {
    ArgumentChecker.notNull(futures, "Future");
    ArgumentChecker.notNull(multicurve, "Multi-curves with Hull-White");
    final Currency ccy = futures.getCurrency();
    ArgumentChecker.isTrue(multicurve.getHullWhiteCurrency().equals(ccy), "Futures currency incompatible with data");
    final HullWhiteOneFactorPiecewiseConstantParameters parameters = multicurve.getHullWhiteParameters();
    final MulticurveProviderInterface multicurves = multicurve.getMulticurveProvider();
    final AnnuityPaymentFixed cfe = futures.getUnderlyingSwap().accept(CFEC, multicurves);
    final int nbCf = cfe.getNumberOfPayments();
    final double[] adjustments = new double[nbCf];
    final double[] df = new double[nbCf];
    for (int loopcf = 0; loopcf < nbCf; loopcf++) {
      adjustments[loopcf] = MODEL.futuresConvexityFactor(parameters, futures.getTradingLastTime(), cfe.getNthPayment(loopcf).getPaymentTime(), futures.getDeliveryTime());
      df[loopcf] = multicurves.getDiscountFactor(ccy, cfe.getNthPayment(loopcf).getPaymentTime());
    }
    double price = 1.0;
    for (int loopcf = 0; loopcf < nbCf; loopcf++) {
      price += (cfe.getNthPayment(loopcf).getAmount() * df[loopcf] * adjustments[loopcf]) / df[0];
    }
    // Backward sweep
    final double priceBar = 1.0;
    final double[] dfBar = new double[nbCf];
    dfBar[0] = -(price - 1.0d - cfe.getNthPayment(0).getAmount() * adjustments[0]) / df[0] * priceBar;
    for (int loopcf = 1; loopcf < nbCf; loopcf++) {
      dfBar[loopcf] = (cfe.getNthPayment(loopcf).getAmount() * adjustments[loopcf]) / df[0] * priceBar;
    }
    final double[] cfeAmountBar = new double[nbCf];
    for (int loopcf = 0; loopcf < nbCf; loopcf++) {
      cfeAmountBar[loopcf] = (df[loopcf] * adjustments[loopcf]) / df[0] * priceBar;
    }
    final List<DoublesPair> listDfSensi = new ArrayList<>();
    for (int loopcf = 0; loopcf < cfe.getNumberOfPayments(); loopcf++) {
      final DoublesPair dfSensi = DoublesPair.of(cfe.getNthPayment(loopcf).getPaymentTime(), -cfe.getNthPayment(loopcf).getPaymentTime() * df[loopcf] * dfBar[loopcf]);
      listDfSensi.add(dfSensi);
    }
    final Map<String, List<DoublesPair>> pvsDF = new HashMap<>();
    pvsDF.put(multicurves.getName(ccy), listDfSensi);
    MulticurveSensitivity sensitivity = MulticurveSensitivity.ofYieldDiscounting(pvsDF);
    final Map<Double, MulticurveSensitivity> cfeCurveSensi = futures.getUnderlyingSwap().accept(CFECSC, multicurves);
    for (int loopcf = 0; loopcf < cfe.getNumberOfPayments(); loopcf++) {
      final MulticurveSensitivity sensiCfe = cfeCurveSensi.get(cfe.getNthPayment(loopcf).getPaymentTime());
      if (!(sensiCfe == null)) { // There is some sensitivity to that cfe.
        sensitivity = sensitivity.plus(sensiCfe.multipliedBy(cfeAmountBar[loopcf]));
      }
    }
    return sensitivity;
  }

}
