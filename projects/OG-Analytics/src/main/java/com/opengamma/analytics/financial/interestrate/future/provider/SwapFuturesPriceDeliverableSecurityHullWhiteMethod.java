/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.future.provider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.opengamma.analytics.financial.interestrate.annuity.derivative.AnnuityPaymentFixed;
import com.opengamma.analytics.financial.interestrate.future.derivative.SwapFuturesPriceDeliverableSecurity;
import com.opengamma.analytics.financial.model.interestrate.HullWhiteOneFactorPiecewiseConstantInterestRateModel;
import com.opengamma.analytics.financial.model.interestrate.definition.HullWhiteOneFactorPiecewiseConstantParameters;
import com.opengamma.analytics.financial.provider.calculator.discounting.CashFlowEquivalentCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.CashFlowEquivalentCurveSensitivityCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueDiscountingCalculator;
import com.opengamma.analytics.financial.provider.description.interestrate.HullWhiteOneFactorProviderInterface;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MulticurveSensitivity;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Method to compute the price for an deliverable swap futures with convexity adjustment from a Hull-White one factor model.
 * <p> Reference: Henrard M., Deliverable Interest Rate Swap Futures: pricing in Gaussian HJM model, September 2012.
 */
public final class SwapFuturesPriceDeliverableSecurityHullWhiteMethod {

  /**
   * The unique instance of the calculator.
   */
  private static final SwapFuturesPriceDeliverableSecurityHullWhiteMethod INSTANCE = new SwapFuturesPriceDeliverableSecurityHullWhiteMethod();

  /**
   * Constructor.
   */
  private SwapFuturesPriceDeliverableSecurityHullWhiteMethod() {
  }

  /**
   * Gets the calculator instance.
   * @return The calculator.
   */
  public static SwapFuturesPriceDeliverableSecurityHullWhiteMethod getInstance() {
    return INSTANCE;
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
  /**
   * The present value calculator by discounting.
   */
  private static final PresentValueDiscountingCalculator PVDC = PresentValueDiscountingCalculator.getInstance();

  /**
   * Computes the futures price.
   * @param futures The futures.
   * @param hwMulticurves The multi-curves provider with Hull-White one factor parameters.
   * @return The price.
   */
  public double price(final SwapFuturesPriceDeliverableSecurity futures, final HullWhiteOneFactorProviderInterface hwMulticurves) {
    ArgumentChecker.notNull(futures, "Future");
    ArgumentChecker.notNull(hwMulticurves, "Multi-curves with Hull-White");
    final Currency ccy = futures.getCurrency();
    ArgumentChecker.isTrue(hwMulticurves.getHullWhiteCurrency().equals(ccy), "Futures currency incompatible with data");
    final HullWhiteOneFactorPiecewiseConstantParameters parameters = hwMulticurves.getHullWhiteParameters();
    final MulticurveProviderInterface multicurves = hwMulticurves.getMulticurveProvider();
    final AnnuityPaymentFixed cfe = futures.getUnderlyingSwap().accept(CFEC, multicurves);
    final int nbCf = cfe.getNumberOfPayments();
    final double[] adjustments = new double[nbCf];
    final double[] df = new double[nbCf];
    for (int loopcf = 0; loopcf < nbCf; loopcf++) {
      adjustments[loopcf] = MODEL.futuresConvexityFactor(parameters, futures.getLastTradingTime(), cfe.getNthPayment(loopcf).getPaymentTime(), futures.getDeliveryTime());
      df[loopcf] = multicurves.getDiscountFactor(ccy, cfe.getNthPayment(loopcf).getPaymentTime());
    }
    double price = 1.0;
    for (int loopcf = 0; loopcf < nbCf; loopcf++) {
      price += (cfe.getNthPayment(loopcf).getAmount() * df[loopcf] * adjustments[loopcf]) / df[0];
    }
    return price;
  }

  /**
   * Returns the convexity adjustment, i.e. the difference between the adjusted price and the present value of the underlying swap.
   * @param futures The swap futures.
   * @param hwMulticurves The multi-curve and parameters provider.
   * @return The adjustment.
   */
  public double convexityAdjustment(final SwapFuturesPriceDeliverableSecurity futures, final HullWhiteOneFactorProviderInterface hwMulticurves) {
    ArgumentChecker.notNull(futures, "swap futures");
    ArgumentChecker.notNull(hwMulticurves, "parameter provider");
    MultipleCurrencyAmount pv = futures.getUnderlyingSwap().accept(PVDC, hwMulticurves.getMulticurveProvider());
    double price = price(futures, hwMulticurves);
    return price - (1.0d + pv.getAmount(futures.getCurrency()));
  }

  /**
   * Computes the futures price sensitivity to the curves.
   * @param futures The futures.
   * @param hwMulticurves The multi-curves provider with Hull-White one factor parameters.
   * @return The sensitivity.
   */
  // TODO: review Dsc sensitivity
  public MulticurveSensitivity priceCurveSensitivity(final SwapFuturesPriceDeliverableSecurity futures, final HullWhiteOneFactorProviderInterface hwMulticurves) {
    ArgumentChecker.notNull(futures, "Future");
    ArgumentChecker.notNull(hwMulticurves, "Multi-curves with Hull-White");
    final Currency ccy = futures.getCurrency();
    ArgumentChecker.isTrue(hwMulticurves.getHullWhiteCurrency().equals(ccy), "Futures currency incompatible with data");
    final HullWhiteOneFactorPiecewiseConstantParameters parameters = hwMulticurves.getHullWhiteParameters();
    final MulticurveProviderInterface multicurves = hwMulticurves.getMulticurveProvider();
    final AnnuityPaymentFixed cfe = futures.getUnderlyingSwap().accept(CFEC, multicurves);
    final int nbCf = cfe.getNumberOfPayments();
    final double[] adjustments = new double[nbCf];
    final double[] df = new double[nbCf];
    for (int loopcf = 0; loopcf < nbCf; loopcf++) {
      adjustments[loopcf] = MODEL.futuresConvexityFactor(parameters, futures.getLastTradingTime(), cfe.getNthPayment(loopcf).getPaymentTime(), futures.getDeliveryTime());
      df[loopcf] = multicurves.getDiscountFactor(ccy, cfe.getNthPayment(loopcf).getPaymentTime());
    }
    double price = 1.0;
    for (int loopcf = 0; loopcf < nbCf; loopcf++) {
      price += (cfe.getNthPayment(loopcf).getAmount() * df[loopcf] * adjustments[loopcf]) / df[0];
    }
    // Backward sweep
    final double priceBar = 1.0;
    final double[] dfBar = new double[nbCf];
    dfBar[0] = -(price - cfe.getNthPayment(0).getAmount() * adjustments[0]) / df[0] * priceBar;
    for (int loopcf = 1; loopcf < nbCf; loopcf++) {
      dfBar[loopcf] = (cfe.getNthPayment(loopcf).getAmount() * adjustments[loopcf]) / df[0] * priceBar;
    }
    final double[] cfeAmountBar = new double[nbCf];
    for (int loopcf = 0; loopcf < nbCf; loopcf++) {
      cfeAmountBar[loopcf] = (df[loopcf] * adjustments[loopcf]) / df[0] * priceBar;
    }
    final List<DoublesPair> listDfSensi = new ArrayList<>();
    for (int loopcf = 0; loopcf < cfe.getNumberOfPayments(); loopcf++) {
      final DoublesPair dfSensi = new DoublesPair(cfe.getNthPayment(loopcf).getPaymentTime(), -cfe.getNthPayment(loopcf).getPaymentTime() * df[loopcf] * dfBar[loopcf]);
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
