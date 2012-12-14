/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.swaption.provider;

import com.opengamma.analytics.financial.interestrate.annuity.derivative.AnnuityPaymentFixed;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionPhysicalFixedIbor;
import com.opengamma.analytics.financial.model.interestrate.G2ppPiecewiseConstantModel;
import com.opengamma.analytics.financial.provider.calculator.discounting.CashFlowEquivalentCalculator;
import com.opengamma.analytics.financial.provider.description.interestrate.G2ppProviderInterface;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.analytics.math.function.Function2D;
import com.opengamma.analytics.math.integration.IntegratorRepeated2D;
import com.opengamma.analytics.math.integration.RungeKuttaIntegrator1D;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 * Method to compute the present value of physical delivery European swaptions with the G2++ model by numerical integration.
 */
public class SwaptionPhysicalFixedIborG2ppNumericalIntegrationMethod {

  /**
   * The model used in computations.
   */
  private static final G2ppPiecewiseConstantModel MODEL_G2PP = new G2ppPiecewiseConstantModel();
  /**
   * The cash flow equivalent calculator used in computations.
   */
  private static final CashFlowEquivalentCalculator CFEC = CashFlowEquivalentCalculator.getInstance();
  /**
   * Minimal number of integration steps in the integration.
   */
  private static final int NB_INTEGRATION = 50;

  /**
   * Computes the present value of the Physical delivery swaption through approximation..
   * @param swaption The swaption.
   * @param g2Data The G2++ parameters and the curves.
   * @return The present value.
   */
  public MultipleCurrencyAmount presentValue(final SwaptionPhysicalFixedIbor swaption, final G2ppProviderInterface g2Data) {
    final Currency ccy = swaption.getCurrency();
    final MulticurveProviderInterface multicurves = g2Data.getMulticurveProvider();
    final AnnuityPaymentFixed cfe = swaption.getUnderlyingSwap().accept(CFEC, g2Data.getMulticurveProvider());
    final double theta = swaption.getTimeToExpiry();
    final int nbCf = cfe.getNumberOfPayments();
    final double[] t = new double[nbCf];
    final double[] df = new double[nbCf];
    final double[] discountedCashFlow = new double[nbCf];
    for (int loopcf = 0; loopcf < nbCf; loopcf++) {
      t[loopcf] = cfe.getNthPayment(loopcf).getPaymentTime();
      df[loopcf] = multicurves.getDiscountFactor(ccy, cfe.getNthPayment(loopcf).getPaymentTime());
      discountedCashFlow[loopcf] = df[loopcf] * cfe.getNthPayment(loopcf).getAmount();
    }

    final double rhog2pp = g2Data.getG2ppParameters().getCorrelation();
    final double[][] htheta = MODEL_G2PP.volatilityMaturityPart(g2Data.getG2ppParameters(), theta, t);
    final double[][] gamma = MODEL_G2PP.gamma(g2Data.getG2ppParameters(), 0, theta);
    final double[][] alpha = new double[2][nbCf];
    final double[] tau2 = new double[nbCf];
    for (int loopcf = 0; loopcf < nbCf; loopcf++) {
      alpha[0][loopcf] = Math.sqrt(gamma[0][0]) * htheta[0][loopcf];
      alpha[1][loopcf] = Math.sqrt(gamma[1][1]) * htheta[1][loopcf];
      tau2[loopcf] = alpha[0][loopcf] * alpha[0][loopcf] + alpha[1][loopcf] * alpha[1][loopcf] + 2 * rhog2pp * gamma[0][1] * htheta[0][loopcf] * htheta[1][loopcf];
    }
    final double rhobar = rhog2pp * gamma[0][1] / Math.sqrt(gamma[0][0] * gamma[1][1]);

    final SwaptionIntegrant integrant = new SwaptionIntegrant(discountedCashFlow, alpha, tau2, rhobar);
    final double limit = 12.0;
    final double absoluteTolerance = 1.0E-1;
    final double relativeTolerance = 1.0E-6;
    final RungeKuttaIntegrator1D integrator1D = new RungeKuttaIntegrator1D(absoluteTolerance, relativeTolerance, NB_INTEGRATION);
    final IntegratorRepeated2D integrator2D = new IntegratorRepeated2D(integrator1D);
    double pv = 0.0;
    try {
      pv = 1.0 / (2.0 * Math.PI * Math.sqrt(1 - rhobar * rhobar)) * integrator2D.integrate(integrant, new Double[] {-limit, -limit}, new Double[] {limit, limit});
    } catch (final Exception e) {
      throw new RuntimeException(e);
    }
    return MultipleCurrencyAmount.of(swaption.getCurrency(), pv * (swaption.isLong() ? 1.0 : -1.0));
  }

  /**
   * Inner class to implement the integration used in price replication.
   */
  private static final class SwaptionIntegrant extends Function2D<Double, Double> {

    private final double[] _discountedCashFlow;
    private final double[][] _alpha;
    private final double[] _tau2;
    private final double _rhobar;

    /**
     * Constructor to the integrant function.
     * @param discountedCashFlow The discounted cash flows.
     * @param alpha The bond volatilities.
     */
    public SwaptionIntegrant(final double[] discountedCashFlow, final double[][] alpha, final double[] tau2, final double rhobar) {
      _discountedCashFlow = discountedCashFlow;
      _alpha = alpha;
      _tau2 = tau2;
      _rhobar = rhobar;
    }

    @Override
    public Double evaluate(final Double x0, final Double x1) {
      double result = 0.0;
      final double densityPart = -(x0 * x0 + x1 * x1 - 2 * _rhobar * x0 * x1) / (2.0 * (1 - _rhobar * _rhobar));
      for (int loopcf = 0; loopcf < _discountedCashFlow.length; loopcf++) {
        result += _discountedCashFlow[loopcf] * Math.exp(-_alpha[0][loopcf] * x0 - _alpha[1][loopcf] * x1 - _tau2[loopcf] / 2.0 + densityPart);
      }
      return Math.max(result, 0.0);
    }
  }

}
