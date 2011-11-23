/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.swaption.method;

import com.opengamma.financial.interestrate.CashFlowEquivalentCalculator;
import com.opengamma.financial.interestrate.InstrumentDerivative;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.interestrate.annuity.definition.AnnuityPaymentFixed;
import com.opengamma.financial.interestrate.method.PricingMethod;
import com.opengamma.financial.interestrate.payments.Payment;
import com.opengamma.financial.interestrate.swap.SwapFixedIborMethod;
import com.opengamma.financial.interestrate.swap.definition.FixedCouponSwap;
import com.opengamma.financial.interestrate.swaption.derivative.SwaptionCashFixedIbor;
import com.opengamma.financial.model.interestrate.G2ppPiecewiseConstantModel;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.financial.model.interestrate.definition.G2ppPiecewiseConstantDataBundle;
import com.opengamma.math.function.Function2D;
import com.opengamma.math.integration.IntegratorRepeated2D;
import com.opengamma.math.integration.RungeKuttaIntegrator1D;
import com.opengamma.util.money.CurrencyAmount;

/**
 * Method to compute the present value of cash-settled European swaptions with the G2++ model by numerical integration.
 */
public class SwaptionCashFixedIborG2ppNumericalIntegrationMethod implements PricingMethod {

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

  public CurrencyAmount presentValue(final SwaptionCashFixedIbor swaption, final G2ppPiecewiseConstantDataBundle g2Data) {
    YieldAndDiscountCurve dsc = g2Data.getCurve(swaption.getUnderlyingSwap().getFixedLeg().getDiscountCurve());
    double notional = swaption.getUnderlyingSwap().getFixedLeg().getNthPayment(0).getNotional();
    double strike = swaption.getUnderlyingSwap().getFixedLeg().getNthPayment(0).getFixedRate();
    AnnuityPaymentFixed cfeIbor = CFEC.visit(swaption.getUnderlyingSwap().getSecondLeg(), g2Data);
    double theta = swaption.getTimeToExpiry();
    double dft0 = dsc.getDiscountFactor(swaption.getSettlementTime());
    int nbCfFixed = swaption.getUnderlyingSwap().getFixedLeg().getNumberOfPayments();
    int nbCfIbor = cfeIbor.getNumberOfPayments();
    double[] tFixed = new double[nbCfFixed];
    double[] dfFixed = new double[nbCfFixed];
    double[] discountedCashFlowFixed = new double[nbCfFixed];
    for (int loopcf = 0; loopcf < nbCfFixed; loopcf++) {
      tFixed[loopcf] = swaption.getUnderlyingSwap().getFixedLeg().getNthPayment(loopcf).getPaymentTime();
      dfFixed[loopcf] = dsc.getDiscountFactor(tFixed[loopcf]);
      discountedCashFlowFixed[loopcf] = dfFixed[loopcf] * swaption.getUnderlyingSwap().getFixedLeg().getNthPayment(loopcf).getPaymentYearFraction();
    }
    double[] tIbor = new double[nbCfIbor];
    double[] dfIbor = new double[nbCfIbor];
    double[] discountedCashFlowIbor = new double[nbCfIbor];
    for (int loopcf = 0; loopcf < nbCfIbor; loopcf++) {
      tIbor[loopcf] = cfeIbor.getNthPayment(loopcf).getPaymentTime();
      dfIbor[loopcf] = dsc.getDiscountFactor(tIbor[loopcf]);
      discountedCashFlowIbor[loopcf] = dfIbor[loopcf] * cfeIbor.getNthPayment(loopcf).getAmount() / notional;
    }

    double rhog2pp = g2Data.getG2ppParameter().getCorrelation();
    double[][] gamma = MODEL_G2PP.gamma(g2Data.getG2ppParameter(), 0, theta);
    double rhobar = rhog2pp * gamma[0][1] / Math.sqrt(gamma[0][0] * gamma[1][1]);

    double[][] hthetaFixed = MODEL_G2PP.volatilityMaturityPart(g2Data.getG2ppParameter(), theta, tFixed);
    double[][] alphaFixed = new double[2][nbCfFixed];
    double[] tau2Fixed = new double[nbCfFixed];
    for (int loopcf = 0; loopcf < nbCfFixed; loopcf++) {
      alphaFixed[0][loopcf] = Math.sqrt(gamma[0][0]) * hthetaFixed[0][loopcf];
      alphaFixed[1][loopcf] = Math.sqrt(gamma[1][1]) * hthetaFixed[1][loopcf];
      tau2Fixed[loopcf] = alphaFixed[0][loopcf] * alphaFixed[0][loopcf] + alphaFixed[1][loopcf] * alphaFixed[1][loopcf] + 2 * rhog2pp * gamma[0][1] * hthetaFixed[0][loopcf] * hthetaFixed[1][loopcf];
    }

    double[][] hthetaIbor = MODEL_G2PP.volatilityMaturityPart(g2Data.getG2ppParameter(), theta, tIbor);
    double[][] alphaIbor = new double[2][nbCfIbor];
    double[] tau2Ibor = new double[nbCfIbor];
    for (int loopcf = 0; loopcf < nbCfIbor; loopcf++) {
      alphaIbor[0][loopcf] = Math.sqrt(gamma[0][0]) * hthetaIbor[0][loopcf];
      alphaIbor[1][loopcf] = Math.sqrt(gamma[1][1]) * hthetaIbor[1][loopcf];
      tau2Ibor[loopcf] = alphaIbor[0][loopcf] * alphaIbor[0][loopcf] + alphaIbor[1][loopcf] * alphaIbor[1][loopcf] + 2 * rhog2pp * gamma[0][1] * hthetaIbor[0][loopcf] * hthetaIbor[1][loopcf];
    }

    final SwaptionIntegrant integrant = new SwaptionIntegrant(discountedCashFlowFixed, alphaFixed, tau2Fixed, discountedCashFlowIbor, alphaIbor, tau2Ibor, rhobar, 
        swaption.getUnderlyingSwap(), strike);
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

    return CurrencyAmount.of(swaption.getCurrency(), dft0 * pv * (swaption.isLong() ? 1.0 : -1.0));
  }

  @Override
  public CurrencyAmount presentValue(InstrumentDerivative instrument, YieldCurveBundle curves) {
    return null;
  }

  /**
   * Inner class to implement the integration used in price replication.
   */
  private class SwaptionIntegrant extends Function2D<Double, Double> {

    private final double[] _discountedCashFlowFixed;
    private final double[][] _alphaFixed;
    private final double[] _tau2Fixed;
    private final double[] _discountedCashFlowIbor;
    private final double[][] _alphaIbor;
    private final double[] _tau2Ibor;
    private final double _rhobar;
    private final FixedCouponSwap<? extends Payment> _swap;
    private final double _strike;
    //    private final double _notional;
    private final double _omega;

    public SwaptionIntegrant(final double[] discountedCashFlowFixed, final double[][] alphaFixed, final double[] tau2Fixed, final double[] discountedCashFlowIbor, final double[][] alphaIbor,
        final double[] tau2Ibor, final double rhobar, FixedCouponSwap<? extends Payment> swap, double strike) {
      _discountedCashFlowFixed = discountedCashFlowFixed;
      _alphaFixed = alphaFixed;
      _tau2Fixed = tau2Fixed;
      _discountedCashFlowIbor = discountedCashFlowIbor;
      _alphaIbor = alphaIbor;
      _tau2Ibor = tau2Ibor;
      _rhobar = rhobar;
      _swap = swap;
      _strike = strike;
      //      _notional = Math.abs(swap.getFixedLeg().getNthPayment(0).getNotional());
      _omega = (swap.getFixedLeg().isPayer() ? 1.0 : -1.0);
    }

    @Override
    public Double evaluate(final Double x0, final Double x1) {
      double resultFixed = 0.0;
      for (int loopcf = 0; loopcf < _discountedCashFlowFixed.length; loopcf++) {
        resultFixed += _discountedCashFlowFixed[loopcf] * Math.exp(-_alphaFixed[0][loopcf] * x0 - _alphaFixed[1][loopcf] * x1 - _tau2Fixed[loopcf] / 2.0);
      }
      double resultIbor = 0.0;
      for (int loopcf = 0; loopcf < _discountedCashFlowIbor.length; loopcf++) {
        resultIbor += _discountedCashFlowIbor[loopcf] * Math.exp(-_alphaIbor[0][loopcf] * x0 - _alphaIbor[1][loopcf] * x1 - _tau2Ibor[loopcf] / 2.0);
      }
      double rate = -resultIbor / resultFixed;
      double annuity = SwapFixedIborMethod.getAnnuityCash(_swap, rate);
      double densityPart = -(x0 * x0 + x1 * x1 - 2 * _rhobar * x0 * x1) / (2.0 * (1 - _rhobar * _rhobar));
      double discounting = Math.exp(-_alphaIbor[0][0] * x0 - _alphaIbor[1][0] * x1 - _tau2Ibor[0] / 2.0 + densityPart);
      return discounting * annuity * Math.max(_omega * (rate - _strike), 0.0);
    }

  }

}
