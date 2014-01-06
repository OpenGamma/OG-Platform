/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.future.provider;

import org.apache.commons.math.stat.descriptive.rank.Min;

import com.opengamma.analytics.financial.interestrate.annuity.derivative.AnnuityPaymentFixed;
import com.opengamma.analytics.financial.interestrate.future.derivative.BondFuturesSecurity;
import com.opengamma.analytics.financial.interestrate.payments.derivative.PaymentFixed;
import com.opengamma.analytics.financial.legalentity.LegalEntity;
import com.opengamma.analytics.financial.model.interestrate.HullWhiteOneFactorPiecewiseConstantInterestRateModel;
import com.opengamma.analytics.financial.provider.calculator.discounting.CashFlowEquivalentCalculator;
import com.opengamma.analytics.financial.provider.description.interestrate.HullWhiteIssuerProviderInterface;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.integration.RungeKuttaIntegrator1D;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * Method to compute the bond futures security results with the price computed as the cheapest forward.
 */
public final class BondFuturesSecurityHullWhiteNumericalIntegrationMethod {

  /**
   * Creates the method unique instance.
   */
  private static final BondFuturesSecurityHullWhiteNumericalIntegrationMethod INSTANCE = new BondFuturesSecurityHullWhiteNumericalIntegrationMethod();

  /**
   * Return the method unique instance.
   * @return The instance.
   */
  public static BondFuturesSecurityHullWhiteNumericalIntegrationMethod getInstance() {
    return INSTANCE;
  }

  /**
   * Constructor.
   */
  private BondFuturesSecurityHullWhiteNumericalIntegrationMethod() {
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
  private static final int NB_INTEGRATION = 15;
  /**
   * Function to compute the minimum of an array.
   */
  private static final Min MIN_FUNCTION = new Min();

  /**
   * Computes the future price from the curves used to price the underlying bonds and a Hull-White one factor model. Computation by numerical integration.
   * @param futures The future security.
   * @param data The curve and Hull-White parameters.
   * @return The future price.
   */
  public double price(final BondFuturesSecurity futures, final HullWhiteIssuerProviderInterface data) {
    ArgumentChecker.notNull(futures, "Futures");
    ArgumentChecker.notNull(data, "Hull-White/Issuer provider");
    final Currency ccy = futures.getCurrency();
    final LegalEntity issuer = futures.getDeliveryBasketAtDeliveryDate()[0].getIssuerEntity();
    final double expiryTime = futures.getNoticeLastTime();
    final double deliveryTime = futures.getDeliveryLastTime();
    final int nbBonds = futures.getDeliveryBasketAtDeliveryDate().length;
    final int[] nbPayments = new int[nbBonds];
    final AnnuityPaymentFixed[] cfe = new AnnuityPaymentFixed[nbBonds];
    for (int loopb = 0; loopb < nbBonds; loopb++) {
      cfe[loopb] = futures.getDeliveryBasketAtDeliveryDate()[loopb].accept(CFEC, data.getMulticurveProvider());
      nbPayments[loopb] = cfe[loopb].getNumberOfPayments();
      final PaymentFixed[] payments = new PaymentFixed[nbPayments[loopb] + 1];
      payments[0] = new PaymentFixed(ccy, deliveryTime, -futures.getDeliveryBasketAtDeliveryDate()[loopb].getAccruedInterest());
      System.arraycopy(cfe[loopb].getPayments(), 0, payments, 1, nbPayments[loopb]);
      cfe[loopb] = new AnnuityPaymentFixed(payments);
    }
    final double[][] alpha = new double[nbBonds][];
    final double[][] beta = new double[nbBonds][];
    final double[][] df = new double[nbBonds][];
    final double[][] discountedCashFlow = new double[nbBonds][];
    for (int loopb = 0; loopb < nbBonds; loopb++) {
      alpha[loopb] = new double[nbPayments[loopb] + 1];
      beta[loopb] = new double[nbPayments[loopb] + 1];
      df[loopb] = new double[nbPayments[loopb] + 1];
      discountedCashFlow[loopb] = new double[nbPayments[loopb] + 1];
      for (int loopcf = 0; loopcf < cfe[loopb].getNumberOfPayments(); loopcf++) {
        alpha[loopb][loopcf] = MODEL.alpha(data.getHullWhiteParameters(), 0.0, expiryTime, deliveryTime, cfe[loopb].getNthPayment(loopcf).getPaymentTime());
        beta[loopb][loopcf] = MODEL.futuresConvexityFactor(data.getHullWhiteParameters(), expiryTime, cfe[loopb].getNthPayment(loopcf).getPaymentTime(), deliveryTime);
        df[loopb][loopcf] = data.getIssuerProvider().getDiscountFactor(issuer, cfe[loopb].getNthPayment(loopcf).getPaymentTime());
        discountedCashFlow[loopb][loopcf] = df[loopb][loopcf] / df[loopb][0] * cfe[loopb].getNthPayment(loopcf).getAmount() * beta[loopb][loopcf]
            / futures.getConversionFactor()[loopb];
      }
    }
    // Integration
    final FuturesIntegrant integrant = new FuturesIntegrant(discountedCashFlow, alpha);
    final double limit = 10.0;
    final double absoluteTolerance = 1.0E-2;
    final double relativeTolerance = 1.0E-6;
    final RungeKuttaIntegrator1D integrator = new RungeKuttaIntegrator1D(absoluteTolerance, relativeTolerance, NB_INTEGRATION);
    double price = 0.0;
    try {
      price = 1.0 / Math.sqrt(2.0 * Math.PI) * integrator.integrate(integrant, -limit, limit);
    } catch (final Exception e) {
      throw new RuntimeException(e);
    }
    return price;
  }

  /**
   * Inner class to implement the integration used in price replication.
   */
  private static final class FuturesIntegrant extends Function1D<Double, Double> {

    private final double[][] _discountedCashFlow;
    private final double[][] _alpha;
    private final int _nbBonds;

    /**
     * Constructor to the integrant function.
     * @param discountedCashFlow The discounted cash flows.
     * @param alpha The bond volatilities.
     */
    public FuturesIntegrant(final double[][] discountedCashFlow, final double[][] alpha) {
      _discountedCashFlow = discountedCashFlow;
      _alpha = alpha;
      _nbBonds = discountedCashFlow.length;
    }

    @Override
    public Double evaluate(final Double x) {
      double[] bond = new double[_nbBonds];
      for (int loopb = 0; loopb < _nbBonds; loopb++) {
        for (int loopcf = 0; loopcf < _discountedCashFlow[loopb].length; loopcf++) {
          bond[loopb] += _discountedCashFlow[loopb][loopcf] * Math.exp(-(x + _alpha[loopb][loopcf]) * (x + _alpha[loopb][loopcf]) / 2.0);
        }
      }
      return MIN_FUNCTION.evaluate(bond);
    }
  }

}
