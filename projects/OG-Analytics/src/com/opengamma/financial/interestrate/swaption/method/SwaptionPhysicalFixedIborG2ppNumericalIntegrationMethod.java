/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.swaption.method;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.interestrate.CashFlowEquivalentCalculator;
import com.opengamma.financial.interestrate.InstrumentDerivative;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.interestrate.annuity.definition.AnnuityPaymentFixed;
import com.opengamma.financial.interestrate.method.PricingMethod;
import com.opengamma.financial.interestrate.swaption.derivative.SwaptionPhysicalFixedIbor;
import com.opengamma.financial.model.interestrate.G2ppPiecewiseConstantModel;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.financial.model.interestrate.definition.G2ppPiecewiseConstantDataBundle;
import com.opengamma.math.function.Function2D;
import com.opengamma.math.integration.IntegratorRepeated2D;
import com.opengamma.math.integration.RungeKuttaIntegrator1D;
import com.opengamma.util.money.CurrencyAmount;

/**
 * Method to compute the present value of physical delivery European swaptions with the G2++ model by numerical integration.
 */
public class SwaptionPhysicalFixedIborG2ppNumericalIntegrationMethod implements PricingMethod {

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
  private static final int NB_INTEGRATION = 5;

  /**
   * Computes the present value of the Physical delivery swaption through approximation..
   * @param swaption The swaption.
   * @param g2Data The G2++ parameters and the curves.
   * @return The present value.
   */
  public CurrencyAmount presentValue(final SwaptionPhysicalFixedIbor swaption, final G2ppPiecewiseConstantDataBundle g2Data) {
    YieldAndDiscountCurve dsc = g2Data.getCurve(swaption.getUnderlyingSwap().getFixedLeg().getDiscountCurve());
    AnnuityPaymentFixed cfe = CFEC.visit(swaption.getUnderlyingSwap(), g2Data);
    double theta = swaption.getTimeToExpiry();
    int nbCf = cfe.getNumberOfPayments();
    double[] t = new double[nbCf];
    double[] df = new double[nbCf];
    double[] discountedCashFlow = new double[nbCf];
    for (int loopcf = 0; loopcf < nbCf; loopcf++) {
      t[loopcf] = cfe.getNthPayment(loopcf).getPaymentTime();
      df[loopcf] = dsc.getDiscountFactor(cfe.getNthPayment(loopcf).getPaymentTime());
      discountedCashFlow[loopcf] = df[loopcf] * cfe.getNthPayment(loopcf).getAmount();
    }

    double rhog2pp = g2Data.getG2ppParameter().getCorrelation();
    double[][] htheta = MODEL_G2PP.volatilityMaturityPart(g2Data.getG2ppParameter(), theta, t);
    double[][] gamma = MODEL_G2PP.gamma(g2Data.getG2ppParameter(), 0, theta);
    double[][] alpha = new double[2][nbCf];
    double[] tau2 = new double[nbCf];
    for (int loopcf = 0; loopcf < nbCf; loopcf++) {
      alpha[0][loopcf] = Math.sqrt(gamma[0][0]) * htheta[0][loopcf];
      alpha[1][loopcf] = Math.sqrt(gamma[1][1]) * htheta[1][loopcf];
      tau2[loopcf] = alpha[0][loopcf] * alpha[0][loopcf] + alpha[1][loopcf] * alpha[1][loopcf] + 2 * rhog2pp * gamma[0][1] * htheta[0][loopcf] * htheta[1][loopcf];
    }
    double rhobar = rhog2pp * gamma[0][1] / Math.sqrt(gamma[0][0] * gamma[1][1]);

    final SwaptionIntegrant integrant = new SwaptionIntegrant(discountedCashFlow, alpha, tau2, rhobar);
    final double limit = 10.0;
    final double absoluteTolerance = 1.0E-0;
    final double relativeTolerance = 1.0E-5;
    final RungeKuttaIntegrator1D integrator1D = new RungeKuttaIntegrator1D(absoluteTolerance, relativeTolerance, NB_INTEGRATION);
    IntegratorRepeated2D integrator2D = new IntegratorRepeated2D(integrator1D);
    double pv = 0.0;
    try {
      pv = 1.0 / (2.0 * Math.PI * Math.sqrt(1 - rhobar * rhobar)) * integrator2D.integrate(integrant, new Double[] {-limit, -limit}, new Double[] {limit, limit});
    } catch (final Exception e) {
      throw new RuntimeException(e);
    }
    return CurrencyAmount.of(swaption.getCurrency(), pv * (swaption.isLong() ? 1.0 : -1.0));
  }

  @Override
  public CurrencyAmount presentValue(InstrumentDerivative instrument, YieldCurveBundle curves) {
    Validate.isTrue(instrument instanceof SwaptionPhysicalFixedIbor, "Physical delivery swaption");
    Validate.isTrue(curves instanceof G2ppPiecewiseConstantDataBundle, "Bundle should contain G2++ data");
    return presentValue((SwaptionPhysicalFixedIbor) instrument, (G2ppPiecewiseConstantDataBundle) curves);
  }

  /**
   * Inner class to implement the integration used in price replication.
   */
  private class SwaptionIntegrant extends Function2D<Double, Double> {

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
      double densityPart = -(x0 * x0 + x1 * x1 - 2 * _rhobar * x0 * x1) / (2.0 * (1 - _rhobar * _rhobar));
      for (int loopcf = 0; loopcf < _discountedCashFlow.length; loopcf++) {
        result += _discountedCashFlow[loopcf] * Math.exp(-_alpha[0][loopcf] * x0 - _alpha[1][loopcf] * x1 - _tau2[loopcf] / 2.0 + densityPart);
      }
      return Math.max(result, 0.0);
    }
  }

}
