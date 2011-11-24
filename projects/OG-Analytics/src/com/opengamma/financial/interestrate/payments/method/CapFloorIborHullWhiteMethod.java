/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.payments.method;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.interestrate.InstrumentDerivative;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.interestrate.method.PricingMethod;
import com.opengamma.financial.interestrate.payments.CapFloorIbor;
import com.opengamma.financial.model.interestrate.HullWhiteOneFactorPiecewiseConstantInterestRateModel;
import com.opengamma.financial.model.interestrate.definition.HullWhiteOneFactorPiecewiseConstantDataBundle;
import com.opengamma.math.statistics.distribution.NormalDistribution;
import com.opengamma.math.statistics.distribution.ProbabilityDistribution;
import com.opengamma.util.money.CurrencyAmount;

/**
 *  Class used to compute the price and sensitivity of a Ibor cap/floor with Hull-White one factor model.
 *  The general pricing formula is given by
 * {@latex.ilb %preamble{\\usepackage{amsmath}}
 * \\begin{equation*}
 * \\frac{\\delta_p}{\\delta_F}P^D(0,t_p)\\left( \\frac{P^j(0,t_0)}{P^j(0,t_1)} N(-\\kappa-\\alpha_0) - (1+\\delta_F K) N(-\\kappa-\\alpha_1) \\right)
 * \\end{equation*}
 * where
 * \\begin{equation*}
 * \\kappa = \\frac{1}{\\alpha_1-\\alpha_0} \\left( \\ln\\left(\\frac{(1+\\delta_F K)P^j(0,t_1)}{P^j(0,t_0)}\\right) - \\frac12 (\\alpha_1^2 - \\alpha_0^2) \\right).
 * \\end{equation*}
 * }
 */
public class CapFloorIborHullWhiteMethod implements PricingMethod {

  /**
   * The normal distribution.
   */
  private static final ProbabilityDistribution<Double> NORMAL = new NormalDistribution(0, 1);

  /**
   * The Hull-White model.
   */
  private final HullWhiteOneFactorPiecewiseConstantInterestRateModel _model = new HullWhiteOneFactorPiecewiseConstantInterestRateModel();

  /**
   * Constructor from the model.
   */
  public CapFloorIborHullWhiteMethod() {
  }

  /**
   * Computes the present value of a cap/floor in the Hull-White one factor model.
   * @param cap The cap/floor.
   * @param hwData The Hull-White parameters and the curves.
   * @return The present value.
   */
  public CurrencyAmount presentValue(final CapFloorIbor cap, final HullWhiteOneFactorPiecewiseConstantDataBundle hwData) {
    double tp = cap.getPaymentTime();
    double t0 = cap.getFixingPeriodStartTime();
    double t1 = cap.getFixingPeriodEndTime();
    double deltaF = cap.getFixingYearFraction();
    double deltaP = cap.getPaymentYearFraction();
    double k = cap.getStrike();
    double dfPay = hwData.getCurve(cap.getFundingCurveName()).getDiscountFactor(tp);
    double dfForwardT0 = hwData.getCurve(cap.getForwardCurveName()).getDiscountFactor(t0);
    double dfForwardT1 = hwData.getCurve(cap.getForwardCurveName()).getDiscountFactor(t1);
    double alpha0 = _model.alpha(0.0, cap.getFixingTime(), tp, t0, hwData.getHullWhiteParameter());
    double alpha1 = _model.alpha(0.0, cap.getFixingTime(), tp, t1, hwData.getHullWhiteParameter());
    double kappa = (Math.log((1 + deltaF * k) * dfForwardT1 / dfForwardT0) - (alpha1 * alpha1 - alpha0 * alpha0) / 2.0) / (alpha1 - alpha0);
    double omega = (cap.isCap() ? 1.0 : -1.0);
    double price = deltaP / deltaF * dfPay * omega * (dfForwardT0 / dfForwardT1 * NORMAL.getCDF(omega * (-kappa - alpha0)) - (1.0 + deltaF * k) * NORMAL.getCDF(omega * (-kappa - alpha1)));
    price *= cap.getNotional();
    return CurrencyAmount.of(cap.getCurrency(), price);
  }

  @Override
  public CurrencyAmount presentValue(InstrumentDerivative instrument, YieldCurveBundle curves) {
    Validate.isTrue(instrument instanceof CapFloorIbor, "Ibor Cap/floor");
    Validate.isTrue(curves instanceof HullWhiteOneFactorPiecewiseConstantDataBundle, "Bundle should contain Hull-White data");
    return presentValue((CapFloorIbor) instrument, (HullWhiteOneFactorPiecewiseConstantDataBundle) curves);
  }

}
