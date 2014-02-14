/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.payments.provider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.opengamma.analytics.financial.interestrate.payments.derivative.CapFloorIbor;
import com.opengamma.analytics.financial.model.interestrate.HullWhiteOneFactorPiecewiseConstantInterestRateModel;
import com.opengamma.analytics.financial.model.interestrate.definition.HullWhiteOneFactorPiecewiseConstantParameters;
import com.opengamma.analytics.financial.provider.description.interestrate.HullWhiteOneFactorProviderInterface;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.ForwardSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MulticurveSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyMulticurveSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.SimplyCompoundedForwardSensitivity;
import com.opengamma.analytics.math.statistics.distribution.NormalDistribution;
import com.opengamma.analytics.math.statistics.distribution.ProbabilityDistribution;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;
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
public final class CapFloorIborHullWhiteMethod {

  /**
   * The method unique instance.
   */
  private static final CapFloorIborHullWhiteMethod INSTANCE = new CapFloorIborHullWhiteMethod();

  /**
   * Private constructor.
   */
  private CapFloorIborHullWhiteMethod() {
  }

  /**
   * Return the unique instance of the class.
   * @return The instance.
   */
  public static CapFloorIborHullWhiteMethod getInstance() {
    return INSTANCE;
  }

  /**
   * The normal distribution.
   */
  private static final ProbabilityDistribution<Double> NORMAL = new NormalDistribution(0, 1);

  /**
   * The Hull-White model.
   */
  private final HullWhiteOneFactorPiecewiseConstantInterestRateModel _model = new HullWhiteOneFactorPiecewiseConstantInterestRateModel();

  /**
   * Computes the present value of a cap/floor in the Hull-White one factor model.
   * @param cap The cap/floor.
   * @param hullWhite The Hull-White parameters and the curves.
   * @return The present value.
   */
  public MultipleCurrencyAmount presentValue(final CapFloorIbor cap, final HullWhiteOneFactorProviderInterface hullWhite) {
    ArgumentChecker.notNull(cap, "The cap/floor shoud not be null");
    ArgumentChecker.notNull(hullWhite, "The Hull-White data shoud not be null");
    final Currency ccy = cap.getCurrency();
    final HullWhiteOneFactorPiecewiseConstantParameters parameters = hullWhite.getHullWhiteParameters();
    final MulticurveProviderInterface multicurves = hullWhite.getMulticurveProvider();
    final double tp = cap.getPaymentTime();
    final double t0 = cap.getFixingPeriodStartTime();
    final double t1 = cap.getFixingPeriodEndTime();
    final double deltaF = cap.getFixingAccrualFactor();
    final double deltaP = cap.getPaymentYearFraction();
    final double k = cap.getStrike(); // Add a check on strike above -1/deltaF
    final double dfPay = multicurves.getDiscountFactor(ccy, tp);
    final double forward = multicurves.getSimplyCompoundForwardRate(cap.getIndex(), t0, t1, deltaF); // Add a check on strike above -1/deltaF
    final double alpha0 = _model.alpha(parameters, 0.0, cap.getFixingTime(), tp, t0);
    final double alpha1 = _model.alpha(parameters, 0.0, cap.getFixingTime(), tp, t1);
    final double kappa = (Math.log((1 + deltaF * k) / (1.0 + deltaF * forward)) - (alpha1 * alpha1 - alpha0 * alpha0) / 2.0) / (alpha1 - alpha0);
    final double omega = (cap.isCap() ? 1.0 : -1.0);
    double pv = deltaP / deltaF * dfPay * omega * ((1.0 + deltaF * forward) * NORMAL.getCDF(omega * (-kappa - alpha0)) - (1.0 + deltaF * k) * NORMAL.getCDF(omega * (-kappa - alpha1)));
    pv *= cap.getNotional();
    return MultipleCurrencyAmount.of(ccy, pv);
  }

  /**
   * Computes the present value curve sensitivity of a cap/floor in the Hull-White one factor model.
   * @param cap The cap/floor.
   * @param hullWhite The Hull-White parameters and the curves.
   * @return The present value curve sensitivity.
   */
  public MultipleCurrencyMulticurveSensitivity presentValueCurveSensitivity(final CapFloorIbor cap, final HullWhiteOneFactorProviderInterface hullWhite) {
    ArgumentChecker.notNull(cap, "The cap/floor shoud not be null");
    ArgumentChecker.notNull(hullWhite, "The Hull-White data shoud not be null");
    final Currency ccy = cap.getCurrency();
    final HullWhiteOneFactorPiecewiseConstantParameters parameters = hullWhite.getHullWhiteParameters();
    final MulticurveProviderInterface multicurves = hullWhite.getMulticurveProvider();
    final double tp = cap.getPaymentTime();
    final double t0 = cap.getFixingPeriodStartTime();
    final double t1 = cap.getFixingPeriodEndTime();
    final double deltaF = cap.getFixingAccrualFactor();
    final double deltaP = cap.getPaymentYearFraction();
    final double k = cap.getStrike();
    final double omega = (cap.isCap() ? 1.0 : -1.0);
    // Forward sweep
    final double dfPay = multicurves.getDiscountFactor(ccy, tp);
    final double forward = multicurves.getSimplyCompoundForwardRate(cap.getIndex(), t0, t1, deltaF);
    final double alpha0 = _model.alpha(parameters, 0.0, cap.getFixingTime(), tp, t0);
    final double alpha1 = _model.alpha(parameters, 0.0, cap.getFixingTime(), tp, t1);
    final double kappa = (Math.log((1 + deltaF * k) / (1.0 + deltaF * forward)) - (alpha1 * alpha1 - alpha0 * alpha0) / 2.0) / (alpha1 - alpha0);
    final double n0 = NORMAL.getCDF(omega * (-kappa - alpha0));
    final double n1 = NORMAL.getCDF(omega * (-kappa - alpha1));
    //    double pv = deltaP / deltaF * dfPay * omega * ((1.0 + deltaF * forward) * n0 - (1.0 + deltaF * k) * n1) * cap.getNotional();
    // Backward sweep
    final double pvBar = 1.0;
    //    double kappaBar = 0.0; // kappa is the optimal exercise boundary
    final double forwardBar = deltaP / deltaF * dfPay * omega * deltaF * n0 * cap.getNotional() * pvBar;
    final double dfPayBar = deltaP / deltaF * omega * ((1.0 + deltaF * forward) * n0 - (1.0 + deltaF * k) * n1) * cap.getNotional() * pvBar;

    final Map<String, List<DoublesPair>> mapDsc = new HashMap<>();
    final List<DoublesPair> listDiscounting = new ArrayList<>();
    listDiscounting.add(DoublesPair.of(cap.getPaymentTime(), -cap.getPaymentTime() * dfPay * dfPayBar));
    mapDsc.put(multicurves.getName(ccy), listDiscounting);

    final Map<String, List<ForwardSensitivity>> mapFwd = new HashMap<>();
    final List<ForwardSensitivity> listForward = new ArrayList<>();
    listForward.add(new SimplyCompoundedForwardSensitivity(cap.getFixingPeriodStartTime(), cap.getFixingPeriodEndTime(), cap.getFixingAccrualFactor(), forwardBar));
    mapFwd.put(multicurves.getName(cap.getIndex()), listForward);

    return MultipleCurrencyMulticurveSensitivity.of(ccy, MulticurveSensitivity.of(mapDsc, mapFwd));
  }

  /**
   * Computes the present value Hull-White parameters sensitivity of a cap/floor in the Hull-White one factor model.
   * @param cap The cap/floor.
   * @param hullWhite The Hull-White parameters and the curves.
   * @return The present value parameters sensitivity.
   */
  public double[] presentValueHullWhiteSensitivity(final CapFloorIbor cap, final HullWhiteOneFactorProviderInterface hullWhite) {
    ArgumentChecker.notNull(cap, "The cap/floor shoud not be null");
    ArgumentChecker.notNull(hullWhite, "The Hull-White data shoud not be null");
    final Currency ccy = cap.getCurrency();
    final HullWhiteOneFactorPiecewiseConstantParameters parameters = hullWhite.getHullWhiteParameters();
    final MulticurveProviderInterface multicurves = hullWhite.getMulticurveProvider();
    final double tp = cap.getPaymentTime();
    final double[] t = new double[2];
    t[0] = cap.getFixingPeriodStartTime();
    t[1] = cap.getFixingPeriodEndTime();
    final double deltaF = cap.getFixingAccrualFactor();
    final double deltaP = cap.getPaymentYearFraction();
    final double k = cap.getStrike();
    final double omega = (cap.isCap() ? 1.0 : -1.0);
    // Forward sweep
    final double dfPay = multicurves.getDiscountFactor(ccy, tp);
    final double forward = multicurves.getSimplyCompoundForwardRate(cap.getIndex(), t[0], t[1], deltaF);
    final int nbSigma = parameters.getVolatility().length;
    final double[] alpha = new double[2];
    final double[][] alphaDerivatives = new double[2][nbSigma];
    for (int loopcf = 0; loopcf < 2; loopcf++) {
      alpha[loopcf] = _model.alpha(parameters, 0.0, cap.getFixingTime(), tp, t[loopcf], alphaDerivatives[loopcf]);
    }
    final double kappa = (Math.log((1 + deltaF * k) / (1.0 + deltaF * forward)) - (alpha[1] * alpha[1] - alpha[0] * alpha[0]) / 2.0) / (alpha[1] - alpha[0]);
    final double[] n = new double[2];
    for (int loopcf = 0; loopcf < 2; loopcf++) {
      n[loopcf] = NORMAL.getCDF(omega * (-kappa - alpha[loopcf]));
    }
    // Backward sweep
    final double pvBar = 1.0;
    final double[] nBar = new double[2];
    nBar[1] = deltaP / deltaF * dfPay * omega * (1.0 + deltaF * k) * cap.getNotional() * pvBar;
    nBar[0] = deltaP / deltaF * dfPay * omega * (1.0 + deltaF * forward) * cap.getNotional();
    final double[] alphaBar = new double[2];
    for (int loopcf = 0; loopcf < 2; loopcf++) {
      alphaBar[loopcf] = NORMAL.getPDF(omega * (-kappa - alpha[loopcf])) * -omega * nBar[loopcf];
    }
    final double[] sigmaBar = new double[nbSigma];
    for (int loopcf = 0; loopcf < 2; loopcf++) {
      for (int loopsigma = 0; loopsigma < nbSigma; loopsigma++) {
        sigmaBar[loopsigma] += alphaDerivatives[loopcf][loopsigma] * alphaBar[loopcf];
      }
    }
    return sigmaBar;
  }

}
