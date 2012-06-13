/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.payments.method;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InterestRateCurveSensitivity;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.interestrate.method.PricingMethod;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CapFloorIbor;
import com.opengamma.analytics.financial.model.interestrate.HullWhiteOneFactorPiecewiseConstantInterestRateModel;
import com.opengamma.analytics.financial.model.interestrate.definition.HullWhiteOneFactorPiecewiseConstantDataBundle;
import com.opengamma.analytics.math.statistics.distribution.NormalDistribution;
import com.opengamma.analytics.math.statistics.distribution.ProbabilityDistribution;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Class used to compute the price and sensitivity of a Ibor cap/floor with
 * Hull-White one factor model.  The general pricing formula is given by:
 * $$
 * \begin{equation*}
 * \frac{\delta_p}{\delta_F}P^D(0,t_p)\left( \frac{P^j(0,t_0)}{P^j(0,t_1)} N(-\kappa-\alpha_0) - (1+\delta_F K) N(-\kappa-\alpha_1) \right)
 * \end{equation*}
 * $$
 * where:
 * \begin{equation*}
 * \kappa = \frac{1}{\alpha_1-\alpha_0} \left( \ln\left(\frac{(1+\delta_F K)P^j(0,t_1)}{P^j(0,t_0)}\right) - \frac12 (\alpha_1^2 - \alpha_0^2) \right).
 * \end{equation*}
 * $$
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
    double alpha0 = _model.alpha(hwData.getHullWhiteParameter(), 0.0, cap.getFixingTime(), tp, t0);
    double alpha1 = _model.alpha(hwData.getHullWhiteParameter(), 0.0, cap.getFixingTime(), tp, t1);
    double kappa = (Math.log((1 + deltaF * k) * dfForwardT1 / dfForwardT0) - (alpha1 * alpha1 - alpha0 * alpha0) / 2.0) / (alpha1 - alpha0);
    double omega = (cap.isCap() ? 1.0 : -1.0);
    double pv = deltaP / deltaF * dfPay * omega * (dfForwardT0 / dfForwardT1 * NORMAL.getCDF(omega * (-kappa - alpha0)) - (1.0 + deltaF * k) * NORMAL.getCDF(omega * (-kappa - alpha1)));
    pv *= cap.getNotional();
    return CurrencyAmount.of(cap.getCurrency(), pv);
  }

  @Override
  public CurrencyAmount presentValue(InstrumentDerivative instrument, YieldCurveBundle curves) {
    Validate.isTrue(instrument instanceof CapFloorIbor, "Ibor Cap/floor");
    Validate.isTrue(curves instanceof HullWhiteOneFactorPiecewiseConstantDataBundle, "Bundle should contain Hull-White data");
    return presentValue((CapFloorIbor) instrument, (HullWhiteOneFactorPiecewiseConstantDataBundle) curves);
  }

  public InterestRateCurveSensitivity presentValueCurveSensitivity(final CapFloorIbor cap, final HullWhiteOneFactorPiecewiseConstantDataBundle hwData) {
    double tp = cap.getPaymentTime();
    double t0 = cap.getFixingPeriodStartTime();
    double t1 = cap.getFixingPeriodEndTime();
    double deltaF = cap.getFixingYearFraction();
    double deltaP = cap.getPaymentYearFraction();
    double k = cap.getStrike();
    double omega = (cap.isCap() ? 1.0 : -1.0);
    // Forward sweep
    double dfPay = hwData.getCurve(cap.getFundingCurveName()).getDiscountFactor(tp);
    double dfForwardT0 = hwData.getCurve(cap.getForwardCurveName()).getDiscountFactor(t0);
    double dfForwardT1 = hwData.getCurve(cap.getForwardCurveName()).getDiscountFactor(t1);
    double alpha0 = _model.alpha(hwData.getHullWhiteParameter(), 0.0, cap.getFixingTime(), tp, t0);
    double alpha1 = _model.alpha(hwData.getHullWhiteParameter(), 0.0, cap.getFixingTime(), tp, t1);
    double kappa = (Math.log((1 + deltaF * k) * dfForwardT1 / dfForwardT0) - (alpha1 * alpha1 - alpha0 * alpha0) / 2.0) / (alpha1 - alpha0);
    double n0 = NORMAL.getCDF(omega * (-kappa - alpha0));
    double n1 = NORMAL.getCDF(omega * (-kappa - alpha1));
    //    double pv = deltaP / deltaF * dfPay * omega * (dfForwardT0 / dfForwardT1 * n0 - (1.0 + deltaF * k) * n1) * cap.getNotional();
    // Backward sweep
    double pvBar = 1.0;
    //    double kappaBar = 0.0; // kappa is the optimal exercise boundary
    double dfForwardT1Bar = -deltaP / deltaF * dfPay * omega * dfForwardT0 / (dfForwardT1 * dfForwardT1) * n0 * cap.getNotional() * pvBar;
    double dfForwardT0Bar = deltaP / deltaF * dfPay * omega / dfForwardT1 * n0 * cap.getNotional() * pvBar;
    double dfPayBar = deltaP / deltaF * omega * (dfForwardT0 / dfForwardT1 * n0 - (1.0 + deltaF * k) * n1) * cap.getNotional() * pvBar;
    InterestRateCurveSensitivity result = new InterestRateCurveSensitivity();
    final List<DoublesPair> listDiscounting = new ArrayList<DoublesPair>();
    listDiscounting.add(new DoublesPair(cap.getPaymentTime(), -cap.getPaymentTime() * dfPay * dfPayBar));
    result = result.plus(cap.getFundingCurveName(), listDiscounting);
    final List<DoublesPair> listForward = new ArrayList<DoublesPair>();
    listForward.add(new DoublesPair(cap.getFixingPeriodStartTime(), -cap.getFixingPeriodStartTime() * dfForwardT0 * dfForwardT0Bar));
    listForward.add(new DoublesPair(cap.getFixingPeriodEndTime(), -cap.getFixingPeriodEndTime() * dfForwardT1 * dfForwardT1Bar));
    result = result.plus(cap.getForwardCurveName(), listForward);
    return result;
  }

}
