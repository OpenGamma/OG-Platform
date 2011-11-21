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
import com.opengamma.financial.interestrate.swaption.derivative.SwaptionCashFixedIbor;
import com.opengamma.financial.interestrate.swaption.derivative.SwaptionPhysicalFixedIbor;
import com.opengamma.financial.model.interestrate.HullWhiteOneFactorPiecewiseConstantInterestRateModel;
import com.opengamma.financial.model.interestrate.definition.HullWhiteOneFactorPiecewiseConstantDataBundle;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.integration.RungeKuttaIntegrator1D;
import com.opengamma.util.money.CurrencyAmount;

/**
 * Method to compute the present value of cash-settled European swaptions with the Hull-White one factor model by numerical integration.
 */
public class SwaptionCashFixedIborHullWhiteNumericalIntegrationMethod implements PricingMethod {

  /**
   * The model used in computations.
   */
  private static final HullWhiteOneFactorPiecewiseConstantInterestRateModel MODEL = new HullWhiteOneFactorPiecewiseConstantInterestRateModel();
  /**
   * The cash flow equivalent calculator used in computations.
   */
  private static final CashFlowEquivalentCalculator CFEC = CashFlowEquivalentCalculator.getInstance();
  /**
   * Minimal number of integration steps in the replication.
   */
  private static final int NB_INTEGRATION = 6;

  /**
   * Computes the present value of the Physical delivery swaption.
   * @param swaption The swaption.
   * @param hwData The Hull-White parameters and the curves.
   * @return The present value.
   */
  public CurrencyAmount presentValue(final SwaptionCashFixedIbor swaption, final HullWhiteOneFactorPiecewiseConstantDataBundle hwData) {
    Validate.notNull(swaption);
    Validate.notNull(hwData);
    double expiryTime = swaption.getTimeToExpiry();
    int nbFixed = swaption.getUnderlyingSwap().getFixedLeg().getNumberOfPayments();
    double[] alphaFixed = new double[nbFixed];
    double[] dfFixed = new double[nbFixed];
    double[] discountedCashFlowFixed = new double[nbFixed];
    for (int loopcf = 0; loopcf < nbFixed; loopcf++) {
      alphaFixed[loopcf] = MODEL.alpha(0.0, expiryTime, expiryTime, swaption.getUnderlyingSwap().getFixedLeg().getNthPayment(loopcf).getPaymentTime(), hwData.getHullWhiteParameter());
      dfFixed[loopcf] = hwData.getCurve(swaption.getUnderlyingSwap().getFixedLeg().getNthPayment(loopcf).getFundingCurveName()).getDiscountFactor(
          swaption.getUnderlyingSwap().getFixedLeg().getNthPayment(loopcf).getPaymentTime());
      discountedCashFlowFixed[loopcf] = dfFixed[loopcf] * swaption.getUnderlyingSwap().getFixedLeg().getNthPayment(loopcf).getPaymentYearFraction()
          * swaption.getUnderlyingSwap().getFixedLeg().getNthPayment(loopcf).getNotional();
    }

    AnnuityPaymentFixed cfeIbor = CFEC.visit(swaption.getUnderlyingSwap().getSecondLeg(), hwData);
    double[] alphaIbor = new double[cfeIbor.getNumberOfPayments()];
    double[] dfIbor = new double[cfeIbor.getNumberOfPayments()];
    double[] discountedCashFlowIbor = new double[cfeIbor.getNumberOfPayments()];
    for (int loopcf = 0; loopcf < cfeIbor.getNumberOfPayments(); loopcf++) {
      alphaIbor[loopcf] = MODEL.alpha(0.0, expiryTime, expiryTime, cfeIbor.getNthPayment(loopcf).getPaymentTime(), hwData.getHullWhiteParameter());
      dfIbor[loopcf] = hwData.getCurve(cfeIbor.getDiscountCurve()).getDiscountFactor(cfeIbor.getNthPayment(loopcf).getPaymentTime());
      discountedCashFlowIbor[loopcf] = dfIbor[loopcf] * cfeIbor.getNthPayment(loopcf).getAmount();
    }
    final int nbFixedPaymentYear = (int) Math.round(1.0 / swaption.getUnderlyingSwap().getFixedLeg().getNthPayment(0).getPaymentYearFraction());
    final double notional = Math.abs(swaption.getUnderlyingSwap().getFixedLeg().getNthPayment(0).getNotional());
    // Integration
    final SwaptionIntegrant integrant = new SwaptionIntegrant(discountedCashFlowFixed, alphaFixed, discountedCashFlowIbor, alphaIbor, nbFixedPaymentYear, swaption.getStrike(), swaption.isCall());
    final double limit = 10.0;
    final double absoluteTolerance = 1.0E-8;
    final double relativeTolerance = 1.0E-9;
    final RungeKuttaIntegrator1D integrator = new RungeKuttaIntegrator1D(absoluteTolerance, relativeTolerance, NB_INTEGRATION);
    double pv = 0.0;
    try {
      pv = 1.0 / Math.sqrt(2.0 * Math.PI) * integrator.integrate(integrant, -limit, limit) * (swaption.isLong() ? 1.0 : -1.0) * notional * dfIbor[0];
    } catch (final Exception e) {
      throw new RuntimeException(e);
    }
    return CurrencyAmount.of(swaption.getCurrency(), pv);
  }

  @Override
  public CurrencyAmount presentValue(InstrumentDerivative instrument, YieldCurveBundle curves) {
    Validate.isTrue(instrument instanceof SwaptionPhysicalFixedIbor, "Physical delivery swaption");
    Validate.isTrue(curves instanceof HullWhiteOneFactorPiecewiseConstantDataBundle, "Bundle should contain Hull-White data");
    return presentValue(instrument, curves);
  }

  /**
   * Inner class to implement the integration used in price replication.
   */
  private class SwaptionIntegrant extends Function1D<Double, Double> {

    private final double[] _discountedCashFlowFixed;
    private final double[] _alphaFixed;
    private final double[] _discountedCashFlowIbor;
    private final double[] _alphaIbor;
    private final int _nbFixedPaymentYear;
    private final int _nbFixedPeriod;
    private final double _strike;
    private final boolean _isPayer;

    /**
     * Constructor to the integrant function.
     * @param discountedCashFlowFixed The discounted cash flows.
     * @param alphaFixed The bond volatilities.
     * @param discountedCashFlowIbor The discounted cash flows.
     * @param alphaIbor The bond volatilities.
     * @param nbFixedPaymentYear Number of Fixed payment per year.
     * @param notional The notional.
     * @param strike The strike.
     */
    public SwaptionIntegrant(final double[] discountedCashFlowFixed, final double[] alphaFixed, final double[] discountedCashFlowIbor, final double[] alphaIbor, int nbFixedPaymentYear, double strike,
        boolean isPayer) {
      _discountedCashFlowFixed = discountedCashFlowFixed;
      _alphaFixed = alphaFixed;
      _discountedCashFlowIbor = discountedCashFlowIbor;
      _alphaIbor = alphaIbor;
      _nbFixedPaymentYear = nbFixedPaymentYear;
      _nbFixedPeriod = _discountedCashFlowFixed.length;
      _strike = strike;
      _isPayer = isPayer;
    }

    @Override
    public Double evaluate(final Double x) {
      double numerator = 0.0;
      for (int loopcf = 0; loopcf < _discountedCashFlowIbor.length; loopcf++) {
        numerator += _discountedCashFlowIbor[loopcf] * Math.exp(-(x + _alphaIbor[loopcf]) * (x + _alphaIbor[loopcf]) / 2.0);
      }
      double denominator = 0.0;
      for (int loopcf = 0; loopcf < _discountedCashFlowFixed.length; loopcf++) {
        denominator += _discountedCashFlowFixed[loopcf] * Math.exp(-(x + _alphaFixed[loopcf]) * (x + _alphaFixed[loopcf]) / 2.0);
      }
      double swapRate = -numerator / denominator;
      final double annuityCash = 1.0 / swapRate * (1.0 - 1.0 / Math.pow(1 + swapRate / _nbFixedPaymentYear, _nbFixedPeriod));
      double dfDensity = Math.exp(-(x + _alphaIbor[0]) * (x + _alphaIbor[0]) / 2.0);
      double result;
      result = dfDensity * annuityCash * Math.max((_isPayer ? 1.0 : -1.0) * (swapRate - _strike), 0.0);
      return result;
    }
  }

}
