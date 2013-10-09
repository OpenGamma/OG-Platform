/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.payments.method;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.interestrate.CashFlowEquivalentCalculator;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.AnnuityPaymentFixed;
import com.opengamma.analytics.financial.interestrate.method.PricingMethod;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CapFloorCMS;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.analytics.financial.interestrate.swap.derivative.SwapFixedCoupon;
import com.opengamma.analytics.financial.model.interestrate.HullWhiteOneFactorPiecewiseConstantInterestRateModel;
import com.opengamma.analytics.financial.model.interestrate.definition.HullWhiteOneFactorPiecewiseConstantDataBundle;
import com.opengamma.analytics.math.MathException;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.integration.RungeKuttaIntegrator1D;
import com.opengamma.util.money.CurrencyAmount;

/**
 * Pricing method of a CMS cap/floor in the Hull-White (extended Vasicek) model by approximation.
 * <P> Reference: M. Henrard. CMS Swaps and Caps in One-Factor Gaussian Models, SSRN working paper 985551, February 2008.
 * Available at http://ssrn.com/abstract=985551
 * @deprecated Use {@link com.opengamma.analytics.financial.interestrate.payments.provider.CapFloorCMSHullWhiteNumericalIntegrationMethod}
 */
@Deprecated
public final class CapFloorCMSHullWhiteNumericalIntegrationMethod implements PricingMethod {

  /**
   * The method unique instance.
   */
  private static final CapFloorCMSHullWhiteNumericalIntegrationMethod INSTANCE = new CapFloorCMSHullWhiteNumericalIntegrationMethod();

  /**
   * Return the unique instance of the class.
   * @return The instance.
   */
  public static CapFloorCMSHullWhiteNumericalIntegrationMethod getInstance() {
    return INSTANCE;
  }

  /**
   * Private constructor.
   */
  private CapFloorCMSHullWhiteNumericalIntegrationMethod() {
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
   * Minimal number of integration steps.
   */
  private static final int NB_INTEGRATION = 100;

  public CurrencyAmount presentValue(final CapFloorCMS cms, final HullWhiteOneFactorPiecewiseConstantDataBundle hwData) {
    Validate.notNull(cms);
    Validate.notNull(hwData);
    final double expiryTime = cms.getFixingTime();
    final SwapFixedCoupon<? extends Payment> swap = cms.getUnderlyingSwap();
    final int nbFixed = cms.getUnderlyingSwap().getFixedLeg().getNumberOfPayments();
    final double[] alphaFixed = new double[nbFixed];
    final double[] dfFixed = new double[nbFixed];
    final double[] discountedCashFlowFixed = new double[nbFixed];
    for (int loopcf = 0; loopcf < nbFixed; loopcf++) {
      alphaFixed[loopcf] = MODEL.alpha(hwData.getHullWhiteParameter(), 0.0, expiryTime, expiryTime, swap.getFixedLeg().getNthPayment(loopcf).getPaymentTime());
      dfFixed[loopcf] = hwData.getCurve(swap.getFixedLeg().getNthPayment(loopcf).getFundingCurveName()).getDiscountFactor(swap.getFixedLeg().getNthPayment(loopcf).getPaymentTime());
      discountedCashFlowFixed[loopcf] = dfFixed[loopcf] * swap.getFixedLeg().getNthPayment(loopcf).getPaymentYearFraction() * swap.getFixedLeg().getNthPayment(loopcf).getNotional();
    }

    final AnnuityPaymentFixed cfeIbor = swap.getSecondLeg().accept(CFEC, hwData);
    final double[] alphaIbor = new double[cfeIbor.getNumberOfPayments()];
    final double[] dfIbor = new double[cfeIbor.getNumberOfPayments()];
    final double[] discountedCashFlowIbor = new double[cfeIbor.getNumberOfPayments()];
    for (int loopcf = 0; loopcf < cfeIbor.getNumberOfPayments(); loopcf++) {
      alphaIbor[loopcf] = MODEL.alpha(hwData.getHullWhiteParameter(), 0.0, expiryTime, expiryTime, cfeIbor.getNthPayment(loopcf).getPaymentTime());
      dfIbor[loopcf] = hwData.getCurve(cfeIbor.getDiscountCurve()).getDiscountFactor(cfeIbor.getNthPayment(loopcf).getPaymentTime());
      discountedCashFlowIbor[loopcf] = dfIbor[loopcf] * cfeIbor.getNthPayment(loopcf).getAmount();
    }

    final double alphaPayment = MODEL.alpha(hwData.getHullWhiteParameter(), 0.0, expiryTime, expiryTime, cms.getPaymentTime());
    final double dfPayment = hwData.getCurve(cfeIbor.getDiscountCurve()).getDiscountFactor(cms.getPaymentTime());
    // Integration
    final CMSIntegrant integrant = new CMSIntegrant(discountedCashFlowFixed, alphaFixed, discountedCashFlowIbor, alphaIbor, alphaPayment, cms.getStrike(), (cms.isCap() ? 1.0 : -1.0));
    final double limit = 10.0;
    final double absoluteTolerance = 1.0E-8;
    final double relativeTolerance = 1.0E-9;
    final RungeKuttaIntegrator1D integrator = new RungeKuttaIntegrator1D(absoluteTolerance, relativeTolerance, NB_INTEGRATION);
    double pv = 0.0;
    try {
      pv = 1.0 / Math.sqrt(2.0 * Math.PI) * integrator.integrate(integrant, -limit, limit) * dfPayment * cms.getNotional() * cms.getPaymentYearFraction();
    } catch (final Exception e) {
      throw new MathException(e);
    }
    return CurrencyAmount.of(cms.getCurrency(), pv);
  }

  @Override
  public CurrencyAmount presentValue(final InstrumentDerivative instrument, final YieldCurveBundle curves) {
    Validate.isTrue(instrument instanceof CapFloorCMS, "Cap/floor CMS");
    Validate.isTrue(curves instanceof HullWhiteOneFactorPiecewiseConstantDataBundle, "Curves with HW data");
    return presentValue((CapFloorCMS) instrument, (HullWhiteOneFactorPiecewiseConstantDataBundle) curves);
  }

  /**
   * Inner class to implement the integration used in price computation.
   */
  private static final class CMSIntegrant extends Function1D<Double, Double> {

    private final double[] _discountedCashFlowFixed;
    private final double[] _alphaFixed;
    private final double[] _discountedCashFlowIbor;
    private final double[] _alphaIbor;
    private final double _alphaPayment;
    private final double _strike;
    private final double _omega;

    /**
     * Constructor to the integrant function.
     * @param discountedCashFlowFixed The discounted cash flows of the underlying swap fixed leg.
     * @param alphaFixed The bond volatilities of the underlying swap fixed leg.
     * @param discountedCashFlowIbor The discounted cash flows of the underlying swap Ibor leg.
     * @param alphaIbor The bond volatilities of the underlying swap Ibor leg.
     * @param alphaPayment The bond volatilities of the payment discount factor.
     * @param strike The strike.
     * @param omega The factor.
     */
    public CMSIntegrant(final double[] discountedCashFlowFixed, final double[] alphaFixed, final double[] discountedCashFlowIbor, final double[] alphaIbor, final double alphaPayment,
        final double strike,
        final double omega) {
      _discountedCashFlowFixed = discountedCashFlowFixed;
      _alphaFixed = alphaFixed;
      _discountedCashFlowIbor = discountedCashFlowIbor;
      _alphaIbor = alphaIbor;
      _alphaPayment = alphaPayment;
      _strike = strike;
      _omega = omega;
    }

    @Override
    public Double evaluate(final Double x) {
      @SuppressWarnings("synthetic-access")
      final double swapRate = MODEL.swapRate(x, _discountedCashFlowFixed, _alphaFixed, _discountedCashFlowIbor, _alphaIbor);
      final double dfDensity = Math.exp(-(x + _alphaPayment) * (x + _alphaPayment) / 2.0);
      final double result = dfDensity * Math.max(_omega * (swapRate - _strike), 0.0);
      return result;
    }
  }

}
