/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.future.method;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.opengamma.analytics.financial.interestrate.CashFlowEquivalentCalculator;
import com.opengamma.analytics.financial.interestrate.CashFlowEquivalentCurveSensitivityCalculator;
import com.opengamma.analytics.financial.interestrate.InterestRateCurveSensitivity;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.AnnuityPaymentFixed;
import com.opengamma.analytics.financial.interestrate.future.derivative.SwapFuturesPriceDeliverableSecurity;
import com.opengamma.analytics.financial.interestrate.future.provider.SwapFuturesPriceDeliverableSecurityHullWhiteMethod;
import com.opengamma.analytics.financial.model.interestrate.HullWhiteOneFactorPiecewiseConstantInterestRateModel;
import com.opengamma.analytics.financial.model.interestrate.definition.HullWhiteOneFactorPiecewiseConstantDataBundle;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Method to compute the price for an deliverable swap futures with convexity adjustment from a Hull-White one factor model.
 * <p> Reference: Henrard M., Deliverable Interest Rate Swap Futures: pricing in Gaussian HJM model, September 2012.
 * @deprecated Use {@link SwapFuturesPriceDeliverableSecurityHullWhiteMethod}
 */
@Deprecated
public final class SwapFuturesDeliverableSecurityHullWhiteMethod {

  /**
   * The unique instance of the calculator.
   */
  private static final SwapFuturesDeliverableSecurityHullWhiteMethod INSTANCE = new SwapFuturesDeliverableSecurityHullWhiteMethod();

  /**
   * Gets the calculator instance.
   * @return The calculator.
   */
  public static SwapFuturesDeliverableSecurityHullWhiteMethod getInstance() {
    return INSTANCE;
  }

  /**
   * Constructor.
   */
  private SwapFuturesDeliverableSecurityHullWhiteMethod() {
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
   * Computes the futures price.
   * @param futures The futures.
   * @param curves The curves and the Hull-White parameters.
   * @return The price.
   */
  public double price(final SwapFuturesPriceDeliverableSecurity futures, final HullWhiteOneFactorPiecewiseConstantDataBundle curves) {
    final AnnuityPaymentFixed cfe = futures.getUnderlyingSwap().accept(CFEC, curves);
    final int nbCf = cfe.getNumberOfPayments();
    final double[] adjustments = new double[nbCf];
    final double[] df = new double[nbCf];
    for (int loopcf = 0; loopcf < nbCf; loopcf++) {
      adjustments[loopcf] = MODEL.futuresConvexityFactor(curves.getHullWhiteParameter(), futures.getTradingLastTime(), cfe.getNthPayment(loopcf).getPaymentTime(), futures.getDeliveryTime());
      df[loopcf] = curves.getCurve(cfe.getNthPayment(loopcf).getFundingCurveName()).getDiscountFactor(cfe.getNthPayment(loopcf).getPaymentTime());
    }
    double price = 1.0;
    for (int loopcf = 0; loopcf < nbCf; loopcf++) {
      price += (cfe.getNthPayment(loopcf).getAmount() * df[loopcf] * adjustments[loopcf]) / df[0];
    }
    return price;
  }

  public InterestRateCurveSensitivity pricecurveSensitivity(final SwapFuturesPriceDeliverableSecurity futures, final HullWhiteOneFactorPiecewiseConstantDataBundle curves) {
    final AnnuityPaymentFixed cfe = futures.getUnderlyingSwap().accept(CFEC, curves);
    final int nbCf = cfe.getNumberOfPayments();
    final double[] adjustments = new double[nbCf];
    final double[] df = new double[nbCf];
    for (int loopcf = 0; loopcf < nbCf; loopcf++) {
      adjustments[loopcf] = MODEL.futuresConvexityFactor(curves.getHullWhiteParameter(), futures.getTradingLastTime(), cfe.getNthPayment(loopcf).getPaymentTime(), futures.getDeliveryTime());
      df[loopcf] = curves.getCurve(cfe.getNthPayment(loopcf).getFundingCurveName()).getDiscountFactor(cfe.getNthPayment(loopcf).getPaymentTime());
    }
    double price = 1.0;
    for (int loopcf = 0; loopcf < nbCf; loopcf++) {
      price += (cfe.getNthPayment(loopcf).getAmount() * df[loopcf] * adjustments[loopcf]) / df[0];
    }
    // Backward sweep
    final double priceBar = 1.0;
    final double[] dfBar = new double[nbCf];
    dfBar[0] = -(price - cfe.getNthPayment(0).getAmount() * adjustments[0]) / df[0];
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
    pvsDF.put(cfe.getDiscountCurve(), listDfSensi);
    InterestRateCurveSensitivity sensitivity = new InterestRateCurveSensitivity(pvsDF);
    final Map<Double, InterestRateCurveSensitivity> cfeCurveSensi = futures.getUnderlyingSwap().accept(CFECSC, curves);
    for (int loopcf = 0; loopcf < cfe.getNumberOfPayments(); loopcf++) {
      final InterestRateCurveSensitivity sensiCfe = cfeCurveSensi.get(cfe.getNthPayment(loopcf).getPaymentTime());
      if (!(sensiCfe == null)) { // There is some sensitivity to that cfe.
        sensitivity = sensitivity.plus(sensiCfe.multipliedBy(cfeAmountBar[loopcf]));
      }
    }
    return sensitivity;
  }

}
