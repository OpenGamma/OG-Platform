/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.swaption.provider;

import com.opengamma.analytics.financial.interestrate.annuity.derivative.AnnuityPaymentFixed;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.analytics.financial.interestrate.swap.derivative.SwapFixedCoupon;
import com.opengamma.analytics.financial.interestrate.swap.provider.SwapFixedCouponDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionCashFixedIbor;
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
 * Method to compute the present value of cash-settled European swaptions with the G2++ model by numerical integration.
 */
public class SwaptionCashFixedIborG2ppNumericalIntegrationMethod {

  /**
   * Minimal number of integration steps in the integration.
   */
  private static final int NB_INTEGRATION = 5;

  /**
   * The model used in computations.
   */
  private static final G2ppPiecewiseConstantModel MODEL_G2PP = new G2ppPiecewiseConstantModel();
  /**
   * The cash flow equivalent calculator used in computations.
   */
  private static final CashFlowEquivalentCalculator CFEC = CashFlowEquivalentCalculator.getInstance();
  /**
   * The swap method.
   */
  private static final SwapFixedCouponDiscountingMethod METHOD_SWAP = SwapFixedCouponDiscountingMethod.getInstance();

  public MultipleCurrencyAmount presentValue(final SwaptionCashFixedIbor swaption, final G2ppProviderInterface g2Data) {
    final Currency ccy = swaption.getCurrency();
    final MulticurveProviderInterface multicurves = g2Data.getMulticurveProvider();
    final double notional = swaption.getUnderlyingSwap().getFixedLeg().getNthPayment(0).getNotional();
    final double strike = swaption.getUnderlyingSwap().getFixedLeg().getNthPayment(0).getFixedRate();
    final AnnuityPaymentFixed cfeIbor = swaption.getUnderlyingSwap().getSecondLeg().accept(CFEC, g2Data.getMulticurveProvider());
    final double theta = swaption.getTimeToExpiry();
    final double dft0 = multicurves.getDiscountFactor(ccy, swaption.getSettlementTime());
    final int nbCfFixed = swaption.getUnderlyingSwap().getFixedLeg().getNumberOfPayments();
    final int nbCfIbor = cfeIbor.getNumberOfPayments();
    final double[] tFixed = new double[nbCfFixed];
    final double[] dfFixed = new double[nbCfFixed];
    final double[] discountedCashFlowFixed = new double[nbCfFixed];
    for (int loopcf = 0; loopcf < nbCfFixed; loopcf++) {
      tFixed[loopcf] = swaption.getUnderlyingSwap().getFixedLeg().getNthPayment(loopcf).getPaymentTime();
      dfFixed[loopcf] = multicurves.getDiscountFactor(ccy, tFixed[loopcf]);
      discountedCashFlowFixed[loopcf] = dfFixed[loopcf] * swaption.getUnderlyingSwap().getFixedLeg().getNthPayment(loopcf).getPaymentYearFraction();
    }
    final double[] tIbor = new double[nbCfIbor];
    final double[] dfIbor = new double[nbCfIbor];
    final double[] discountedCashFlowIbor = new double[nbCfIbor];
    for (int loopcf = 0; loopcf < nbCfIbor; loopcf++) {
      tIbor[loopcf] = cfeIbor.getNthPayment(loopcf).getPaymentTime();
      dfIbor[loopcf] = multicurves.getDiscountFactor(ccy, tIbor[loopcf]);
      discountedCashFlowIbor[loopcf] = dfIbor[loopcf] * cfeIbor.getNthPayment(loopcf).getAmount() / notional;
    }

    final double rhog2pp = g2Data.getG2ppParameters().getCorrelation();
    final double[][] gamma = MODEL_G2PP.gamma(g2Data.getG2ppParameters(), 0, theta);
    final double rhobar = rhog2pp * gamma[0][1] / Math.sqrt(gamma[0][0] * gamma[1][1]);

    final double[][] hthetaFixed = MODEL_G2PP.volatilityMaturityPart(g2Data.getG2ppParameters(), theta, tFixed);
    final double[][] alphaFixed = new double[2][nbCfFixed];
    final double[] tau2Fixed = new double[nbCfFixed];
    for (int loopcf = 0; loopcf < nbCfFixed; loopcf++) {
      alphaFixed[0][loopcf] = Math.sqrt(gamma[0][0]) * hthetaFixed[0][loopcf];
      alphaFixed[1][loopcf] = Math.sqrt(gamma[1][1]) * hthetaFixed[1][loopcf];
      tau2Fixed[loopcf] = alphaFixed[0][loopcf] * alphaFixed[0][loopcf] + alphaFixed[1][loopcf] * alphaFixed[1][loopcf] + 2 * rhog2pp * gamma[0][1] * hthetaFixed[0][loopcf] * hthetaFixed[1][loopcf];
    }

    final double[][] hthetaIbor = MODEL_G2PP.volatilityMaturityPart(g2Data.getG2ppParameters(), theta, tIbor);
    final double[][] alphaIbor = new double[2][nbCfIbor];
    final double[] tau2Ibor = new double[nbCfIbor];
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
    final IntegratorRepeated2D integrator2D = new IntegratorRepeated2D(integrator1D);
    double pv = 0.0;
    try {
      pv = 1.0 / (2.0 * Math.PI * Math.sqrt(1 - rhobar * rhobar)) * integrator2D.integrate(integrant, new Double[] {-limit, -limit}, new Double[] {limit, limit});
    } catch (final Exception e) {
      throw new RuntimeException(e);
    }

    return MultipleCurrencyAmount.of(swaption.getCurrency(), dft0 * pv * (swaption.isLong() ? 1.0 : -1.0));
  }

  /**
   * Inner class to implement the integration used in price replication.
   */
  private static final class SwaptionIntegrant extends Function2D<Double, Double> {

    private final double[] _discountedCashFlowFixed;
    private final double[][] _alphaFixed;
    private final double[] _tau2Fixed;
    private final double[] _discountedCashFlowIbor;
    private final double[][] _alphaIbor;
    private final double[] _tau2Ibor;
    private final double _rhobar;
    private final SwapFixedCoupon<? extends Payment> _swap;
    private final double _strike;
    //    private final double _notional;
    private final double _omega;

    public SwaptionIntegrant(final double[] discountedCashFlowFixed, final double[][] alphaFixed, final double[] tau2Fixed, final double[] discountedCashFlowIbor, final double[][] alphaIbor,
        final double[] tau2Ibor, final double rhobar, final SwapFixedCoupon<? extends Payment> swap, final double strike) {
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
      final double rate = -resultIbor / resultFixed;
      @SuppressWarnings("synthetic-access")
      final double annuity = METHOD_SWAP.getAnnuityCash(_swap, rate);
      final double densityPart = -(x0 * x0 + x1 * x1 - 2 * _rhobar * x0 * x1) / (2.0 * (1 - _rhobar * _rhobar));
      final double discounting = Math.exp(-_alphaIbor[0][0] * x0 - _alphaIbor[1][0] * x1 - _tau2Ibor[0] / 2.0 + densityPart);
      return discounting * annuity * Math.max(_omega * (rate - _strike), 0.0);
    }

  }

}
