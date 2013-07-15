/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.swaption.provider;

import com.opengamma.analytics.financial.interestrate.annuity.derivative.AnnuityPaymentFixed;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionCashFixedIbor;
import com.opengamma.analytics.financial.model.interestrate.HullWhiteOneFactorPiecewiseConstantInterestRateModel;
import com.opengamma.analytics.financial.model.interestrate.definition.HullWhiteOneFactorPiecewiseConstantParameters;
import com.opengamma.analytics.financial.provider.calculator.discounting.CashFlowEquivalentCalculator;
import com.opengamma.analytics.financial.provider.description.interestrate.HullWhiteOneFactorProviderInterface;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.integration.RungeKuttaIntegrator1D;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 * Method to compute the present value of cash-settled European swaptions with the Hull-White one factor model by numerical integration.
 */
public final class SwaptionCashFixedIborHullWhiteNumericalIntegrationMethod {

  /**
   * The method unique instance.
   */
  private static final SwaptionCashFixedIborHullWhiteNumericalIntegrationMethod INSTANCE = new SwaptionCashFixedIborHullWhiteNumericalIntegrationMethod();

  /**
   * Return the unique instance of the class.
   * @return The instance.
   */
  public static SwaptionCashFixedIborHullWhiteNumericalIntegrationMethod getInstance() {
    return INSTANCE;
  }

  /**
   * Private constructor.
   */
  private SwaptionCashFixedIborHullWhiteNumericalIntegrationMethod() {
  }

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
   * @param hullWhite The Hull-White parameters and the curves.
   * @return The present value.
   */
  public MultipleCurrencyAmount presentValue(final SwaptionCashFixedIbor swaption, final HullWhiteOneFactorProviderInterface hullWhite) {
    ArgumentChecker.notNull(swaption, "Swaption");
    ArgumentChecker.notNull(hullWhite, "Hull-White provider");
    final Currency ccy = swaption.getCurrency();
    final HullWhiteOneFactorPiecewiseConstantParameters parameters = hullWhite.getHullWhiteParameters();
    final MulticurveProviderInterface multicurves = hullWhite.getMulticurveProvider();
    final double expiryTime = swaption.getTimeToExpiry();
    final int nbFixed = swaption.getUnderlyingSwap().getFixedLeg().getNumberOfPayments();
    final double[] alphaFixed = new double[nbFixed];
    final double[] dfFixed = new double[nbFixed];
    final double[] discountedCashFlowFixed = new double[nbFixed];
    for (int loopcf = 0; loopcf < nbFixed; loopcf++) {
      alphaFixed[loopcf] = MODEL.alpha(parameters, 0.0, expiryTime, expiryTime, swaption.getUnderlyingSwap().getFixedLeg().getNthPayment(loopcf).getPaymentTime());
      dfFixed[loopcf] = multicurves.getDiscountFactor(ccy, swaption.getUnderlyingSwap().getFixedLeg().getNthPayment(loopcf).getPaymentTime());
      discountedCashFlowFixed[loopcf] = dfFixed[loopcf] * swaption.getUnderlyingSwap().getFixedLeg().getNthPayment(loopcf).getPaymentYearFraction()
          * swaption.getUnderlyingSwap().getFixedLeg().getNthPayment(loopcf).getNotional();
    }

    final AnnuityPaymentFixed cfeIbor = swaption.getUnderlyingSwap().getSecondLeg().accept(CFEC, multicurves);
    final double[] alphaIbor = new double[cfeIbor.getNumberOfPayments()];
    final double[] dfIbor = new double[cfeIbor.getNumberOfPayments()];
    final double[] discountedCashFlowIbor = new double[cfeIbor.getNumberOfPayments()];
    for (int loopcf = 0; loopcf < cfeIbor.getNumberOfPayments(); loopcf++) {
      alphaIbor[loopcf] = MODEL.alpha(parameters, 0.0, expiryTime, expiryTime, cfeIbor.getNthPayment(loopcf).getPaymentTime());
      dfIbor[loopcf] = multicurves.getDiscountFactor(ccy, cfeIbor.getNthPayment(loopcf).getPaymentTime());
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
    return MultipleCurrencyAmount.of(swaption.getCurrency(), pv);
  }

  /**
   * Inner class to implement the integration used in price replication.
   */
  private static final class SwaptionIntegrant extends Function1D<Double, Double> {

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
    public SwaptionIntegrant(final double[] discountedCashFlowFixed, final double[] alphaFixed, final double[] discountedCashFlowIbor, final double[] alphaIbor, final int nbFixedPaymentYear,
        final double strike, final boolean isPayer) {
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
      @SuppressWarnings("synthetic-access")
      final double swapRate = MODEL.swapRate(x, _discountedCashFlowFixed, _alphaFixed, _discountedCashFlowIbor, _alphaIbor);
      final double annuityCash = 1.0 / swapRate * (1.0 - 1.0 / Math.pow(1 + swapRate / _nbFixedPaymentYear, _nbFixedPeriod));
      final double dfDensity = Math.exp(-(x + _alphaIbor[0]) * (x + _alphaIbor[0]) / 2.0);
      double result;
      result = dfDensity * annuityCash * Math.max((_isPayer ? 1.0 : -1.0) * (swapRate - _strike), 0.0);
      return result;
    }
  }

}
