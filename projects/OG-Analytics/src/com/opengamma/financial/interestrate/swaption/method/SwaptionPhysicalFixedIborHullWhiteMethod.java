/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.swaption.method;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.interestrate.CashFlowEquivalentCalculator;
import com.opengamma.financial.interestrate.InterestRateDerivative;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.interestrate.annuity.definition.AnnuityPaymentFixed;
import com.opengamma.financial.interestrate.method.PricingMethod;
import com.opengamma.financial.interestrate.swaption.SwaptionPhysicalFixedIbor;
import com.opengamma.financial.model.interestrate.HullWhiteOneFactorPiecewiseConstantInterestRateModel;
import com.opengamma.financial.model.interestrate.definition.HullWhiteOneFactorPiecewiseConstantDataBundle;
import com.opengamma.math.statistics.distribution.NormalDistribution;
import com.opengamma.math.statistics.distribution.ProbabilityDistribution;
import com.opengamma.util.money.CurrencyAmount;

/**
 * Method to computes the present value and sensitivities of physical delivery European swaptions with the Hull-White one factor model.
 * Reference: Henrard, M. (2003). Explicit bond option and swaption formula in Heath-Jarrow-Morton one-factor model. International Journal of Theoretical and Applied Finance, 6(1):57--72.
 */
public class SwaptionPhysicalFixedIborHullWhiteMethod implements PricingMethod {

  /**
   * The model used in computations.
   */
  private static final HullWhiteOneFactorPiecewiseConstantInterestRateModel MODEL = new HullWhiteOneFactorPiecewiseConstantInterestRateModel();
  /**
   * The cash flow equivalent calculator used in computations.
   */
  private static final CashFlowEquivalentCalculator CFEC = CashFlowEquivalentCalculator.getInstance();
  /**
   * The normal distribution implementation.
   */
  private static final ProbabilityDistribution<Double> NORMAL = new NormalDistribution(0, 1);

  /**
   * Computes the present value of the Physical delivery swaption.
   * @param swaption The swaption.
   * @param hwData The Hull-White parameters and the curves.
   * @return The present value.
   */
  public CurrencyAmount presentValue(final SwaptionPhysicalFixedIbor swaption, final HullWhiteOneFactorPiecewiseConstantDataBundle hwData) {
    Validate.notNull(swaption);
    AnnuityPaymentFixed cfe = CFEC.visit(swaption.getUnderlyingSwap(), hwData);
    return presentValue(swaption, cfe, hwData);
  }

  /**
   * Computes the present value of the Physical delivery swaption.
   * @param swaption The swaption.
   * @param cfe The swaption cash flow equivalent.
   * @param hwData The Hull-White parameters and the curves.
   * @return The present value.
   */
  public CurrencyAmount presentValue(final SwaptionPhysicalFixedIbor swaption, final AnnuityPaymentFixed cfe, final HullWhiteOneFactorPiecewiseConstantDataBundle hwData) {
    Validate.notNull(swaption);
    Validate.notNull(hwData);
    double expiryTime = swaption.getTimeToExpiry();
    double[] alpha = new double[cfe.getNumberOfPayments()];
    double[] df = new double[cfe.getNumberOfPayments()];
    double[] discountedCashFlow = new double[cfe.getNumberOfPayments()];
    for (int loopcf = 0; loopcf < cfe.getNumberOfPayments(); loopcf++) {
      alpha[loopcf] = MODEL.alpha(0.0, expiryTime, expiryTime, cfe.getNthPayment(loopcf).getPaymentTime(), hwData.getHullWhiteParameter());
      df[loopcf] = hwData.getCurve(cfe.getDiscountCurve()).getDiscountFactor(cfe.getNthPayment(loopcf).getPaymentTime());
      discountedCashFlow[loopcf] = df[loopcf] * cfe.getNthPayment(loopcf).getAmount();
    }
    double kappa = MODEL.kappa(discountedCashFlow, alpha);
    double omega = (swaption.getUnderlyingSwap().getFixedLeg().isPayer() ? -1.0 : 1.0);
    double pv = 0.0;
    for (int loopcf = 0; loopcf < cfe.getNumberOfPayments(); loopcf++) {
      pv += discountedCashFlow[loopcf] * NORMAL.getCDF(omega * (kappa + alpha[loopcf]));
    }
    return CurrencyAmount.of(swaption.getUnderlyingSwap().getFirstLeg().getCurrency(), pv * (swaption.isLong() ? 1.0 : -1.0));
  }

  @Override
  public CurrencyAmount presentValue(InterestRateDerivative instrument, YieldCurveBundle curves) {
    Validate.isTrue(instrument instanceof SwaptionPhysicalFixedIbor, "Physical delivery swaption");
    Validate.isTrue(curves instanceof HullWhiteOneFactorPiecewiseConstantDataBundle, "Bundle should contain Hull-White data");
    return presentValue((SwaptionPhysicalFixedIbor) instrument, (HullWhiteOneFactorPiecewiseConstantDataBundle) curves);
  }

  public double[] presentValueHullWhiteSensitivity(final SwaptionPhysicalFixedIbor swaption, final HullWhiteOneFactorPiecewiseConstantDataBundle hwData) {
    Validate.notNull(swaption);
    Validate.notNull(hwData);
    int nbSigma = hwData.getHullWhiteParameter().getVolatility().length;
    double[] sigmaBar = new double[nbSigma];
    AnnuityPaymentFixed cfe = CFEC.visit(swaption.getUnderlyingSwap(), hwData);
    //Forward sweep
    double expiryTime = swaption.getTimeToExpiry();
    double[] alpha = new double[cfe.getNumberOfPayments()];
    double[][] alphaDerivatives = new double[cfe.getNumberOfPayments()][nbSigma];
    double[] df = new double[cfe.getNumberOfPayments()];
    double[] discountedCashFlow = new double[cfe.getNumberOfPayments()];
    for (int loopcf = 0; loopcf < cfe.getNumberOfPayments(); loopcf++) {
      alpha[loopcf] = MODEL.alpha(0.0, expiryTime, expiryTime, cfe.getNthPayment(loopcf).getPaymentTime(), hwData.getHullWhiteParameter(), alphaDerivatives[loopcf]);
      df[loopcf] = hwData.getCurve(cfe.getDiscountCurve()).getDiscountFactor(cfe.getNthPayment(loopcf).getPaymentTime());
      discountedCashFlow[loopcf] = df[loopcf] * cfe.getNthPayment(loopcf).getAmount();
    }
    double kappa = MODEL.kappa(discountedCashFlow, alpha);
    double omega = (swaption.getUnderlyingSwap().getFixedLeg().isPayer() ? -1.0 : 1.0);
    double pv = 0.0;
    for (int loopcf = 0; loopcf < cfe.getNumberOfPayments(); loopcf++) {
      pv += discountedCashFlow[loopcf] * NORMAL.getCDF(omega * (kappa + alpha[loopcf]));
    }
    //Backward sweep
    double pvBar = 1.0;
    //    double kappaBar = 0.0;
    double[] alphaBar = new double[cfe.getNumberOfPayments()];
    for (int loopcf = 0; loopcf < cfe.getNumberOfPayments(); loopcf++) {
      alphaBar[loopcf] = discountedCashFlow[loopcf] * NORMAL.getPDF(omega * (kappa + alpha[loopcf])) * omega * pvBar;
    }
    for (int loopsigma = 0; loopsigma < nbSigma; loopsigma++) {
      for (int loopcf = 0; loopcf < cfe.getNumberOfPayments(); loopcf++) {
        sigmaBar[loopsigma] += alphaDerivatives[loopcf][loopsigma] * alphaBar[loopcf];
      }
    }
    return sigmaBar;
  }

}
