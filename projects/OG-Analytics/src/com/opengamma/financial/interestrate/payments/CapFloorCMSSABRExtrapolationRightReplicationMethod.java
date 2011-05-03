/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.payments;

import com.opengamma.financial.interestrate.ParRateCalculator;
import com.opengamma.financial.interestrate.annuity.definition.AnnuityCouponFixed;
import com.opengamma.financial.interestrate.swap.definition.FixedCouponSwap;
import com.opengamma.financial.model.option.definition.SABRInterestRateDataBundle;
import com.opengamma.financial.model.option.definition.SABRInterestRateParameter;
import com.opengamma.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.financial.model.option.pricing.analytic.formula.SABRExtrapolationRightFunction;
import com.opengamma.financial.model.volatility.smile.function.SABRFormulaData;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.integration.RungeKuttaIntegrator1D;
import com.opengamma.util.tuple.DoublesPair;

/**
 *  Class used to compute the price of a CMS cap/floor by swaption replication on a SABR formula with extrapolation.
 *  Reference: Hagan, P. S. (2003). Convexity conundrums: Pricing CMS swaps, caps, and floors. Wilmott Magazine, March, pages 38--44.
 *  OpenGamma implementation note: Replication pricing for linear and TEC format CMS, Version 1.2, March 2011.
 */
public class CapFloorCMSSABRExtrapolationRightReplicationMethod {

  /**
   * The par rate calculator.
   */
  private static final ParRateCalculator PRC = ParRateCalculator.getInstance();
  /**
   * Range of the integral. Used only for caps. Represent the approximation of infinity in the strike dimension.
   * The range is [strike, strike+integrationInterval].
   */
  private final double _integrationInterval;
  /**
   * Minimal number of integration steps in the replication.
   */
  private final int _nbIteration = 6;
  /**
   * The cut-off strike. The smile is extrapolated above that level.
   */
  private final double _cutOffStrike;
  /**
   * The tail thickness parameter.
   */
  private final double _mu;

  /** 
   * Default constructor of the CMS cap/floor replication method. The default integration interval is 1.00 (100%).
   * @param cutOffStrike The cut-off strike.
   * @param mu The tail thickness parameter.
   */
  public CapFloorCMSSABRExtrapolationRightReplicationMethod(double cutOffStrike, double mu) {
    _integrationInterval = 1.00;
    _mu = mu;
    _cutOffStrike = cutOffStrike;
  }

  /**
   * Compute the present value of a CMS cap/floor by replication in SABR framework with extrapolation on the right. 
   * @param cmsCapFloor The CMS cap/floor.
   * @param sabrData The SABR data bundle.
   * @return The present value.
   */
  public double presentValue(CapFloorCMS cmsCapFloor, SABRInterestRateDataBundle sabrData) {
    SABRInterestRateParameter sabrParameter = sabrData.getSABRParameter();
    FixedCouponSwap<? extends Payment> underlyingSwap = cmsCapFloor.getUnderlyingSwap();
    double forward = PRC.visit(underlyingSwap, sabrData);
    double discountFactorTp = sabrData.getCurve(underlyingSwap.getFixedLeg().getNthPayment(0).getFundingCurveName()).getDiscountFactor(cmsCapFloor.getPaymentTime());
    CMSIntegrant integrant = new CMSIntegrant(cmsCapFloor, sabrParameter, forward, _cutOffStrike, _mu);
    double strike = cmsCapFloor.geStrike();
    double strikePart = discountFactorTp * integrant.k(strike) * integrant.g(forward) / integrant.h(forward) * integrant.bs(strike);
    double absoluteTolerance = 1.0 / (discountFactorTp * Math.abs(cmsCapFloor.getNotional()) * cmsCapFloor.getPaymentYearFraction());
    double relativeTolerance = 1E-10;
    RungeKuttaIntegrator1D integrator = new RungeKuttaIntegrator1D(absoluteTolerance, relativeTolerance, _nbIteration);
    double integralPart;
    try {
      if (cmsCapFloor.isCap()) {
        integralPart = discountFactorTp * integrator.integrate(integrant, strike, strike + _integrationInterval);
      } else {
        integralPart = discountFactorTp * integrator.integrate(integrant, 0.0, strike);
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    double priceCMS = (strikePart + integralPart) * cmsCapFloor.getNotional() * cmsCapFloor.getPaymentYearFraction();
    return priceCMS;
  }

  /**
   * Inner class to implement the integration used in price replication.
   */
  private class CMSIntegrant extends Function1D<Double, Double> {
    private static final double EPS = 1E-10;
    private final int _nbFixedPeriod;
    private final int _nbFixedPaymentYear;
    private final double _tau;
    private final double _delta;
    private final double _eta;
    private final double _timeToExpiry;
    private final double _maturity;
    private final double _strike;
    private final double _forward;
    private final double _factor;
    private final SABRExtrapolationRightFunction _sabrExtrapolation;
    private final boolean _isCall;

    /**
     * Constructor
     */
    public CMSIntegrant(CapFloorCMS cmsCap, SABRInterestRateParameter sabrParameter, double forward, double cutOffStrike, double mu) {
      _nbFixedPeriod = cmsCap.getUnderlyingSwap().getFixedLeg().getPayments().length;
      _nbFixedPaymentYear = (int) Math.round(1.0 / cmsCap.getUnderlyingSwap().getFixedLeg().getNthPayment(0).getPaymentYearFraction());
      _tau = 1.0 / _nbFixedPaymentYear;
      _delta = cmsCap.getPaymentTime() - cmsCap.getSettlementTime();
      _eta = -_delta;
      _timeToExpiry = cmsCap.getFixingTime();
      AnnuityCouponFixed annuityFixed = cmsCap.getUnderlyingSwap().getFixedLeg();
      _maturity = annuityFixed.getNthPayment(annuityFixed.getNumberOfPayments() - 1).getPaymentTime() - cmsCap.getSettlementTime();
      _forward = forward;
      DoublesPair expiryMaturity = new DoublesPair(_timeToExpiry, _maturity);
      double alpha = sabrParameter.getAlpha(expiryMaturity);
      double beta = sabrParameter.getBeta(expiryMaturity);
      double rho = sabrParameter.getRho(expiryMaturity);
      double nu = sabrParameter.getNu(expiryMaturity);
      SABRFormulaData sabrData = new SABRFormulaData(_forward, alpha, beta, nu, rho);
      _sabrExtrapolation = new SABRExtrapolationRightFunction(sabrData, cutOffStrike, _timeToExpiry, mu);
      _isCall = cmsCap.isCap();
      _strike = cmsCap.geStrike();
      _factor = g(_forward) / h(_forward);
    }

    @Override
    public Double evaluate(Double x) {
      double[] kD = kpkpp(x);
      // Implementation note: kD[0] contains the first derivative of k; kD[1] the second derivative of k. 
      return (kD[1] * (x - _strike) + 2.0 * kD[0]) * bs(x) * _factor;
    }

    /**
     * The approximation of the discount factor as function of the swap rate.
     * @param x The swap rate.
     * @return The discount factor.
     */
    private double h(double x) {
      return Math.pow(1.0 + _tau * x, _eta);
    }

    /**
     * The cash annuity.
     * @param x The swap rate.
     * @return The annuity.
     */
    private double g(double x) {
      if (x >= EPS) {
        double periodFactor = 1 + x / _nbFixedPaymentYear;
        double nPeriodDiscount = Math.pow(periodFactor, -_nbFixedPeriod);
        return 1.0 / x * (1.0 - nPeriodDiscount);
      }
      return ((double) _nbFixedPeriod) / _nbFixedPaymentYear;
    }

    /**
     * The factor used in the strike part and in the integration of the replication.
     * @param x The swap rate. 
     * @return The factor.
     */
    private double k(double x) {
      double g;
      double h;
      if (x >= EPS) {
        double periodFactor = 1 + x / _nbFixedPaymentYear;
        double nPeriodDiscount = Math.pow(periodFactor, -_nbFixedPeriod);
        g = 1.0 / x * (1.0 - nPeriodDiscount);
        h = Math.pow(1.0 + _tau * x, _eta);
      } else {
        g = ((double) _nbFixedPeriod) / _nbFixedPaymentYear;
        h = 1.0;
      }
      return h / g;
    }

    /**
     * The first and second derivative of the function k.
     * @param x The swap rate.
     * @return The derivative (first element is the first derivative, second element is second derivative.
     */
    private double[] kpkpp(double x) {
      double periodFactor = 1 + x / _nbFixedPaymentYear;
      double nPeriodDiscount = Math.pow(periodFactor, -_nbFixedPeriod);
      /**
       * The value of the annuity and its first and second derivative.
       */
      double g, gp, gpp;
      if (x >= EPS) {
        g = 1.0 / x * (1.0 - nPeriodDiscount);
        gp = -g / x + _nbFixedPeriod / x / _nbFixedPaymentYear * nPeriodDiscount / periodFactor;
        gpp = 2.0 / (x * x) * g - 2.0 * _nbFixedPeriod / (x * x) / _nbFixedPaymentYear * nPeriodDiscount / periodFactor - (_nbFixedPeriod + 1.0) * _nbFixedPeriod / x
            / (_nbFixedPaymentYear * _nbFixedPaymentYear) * nPeriodDiscount / (periodFactor * periodFactor);
      } else {
        // Implementation comment: When x is (almost) 0, useful for CMS swaps which are priced as CMS cap of strike 0.
        g = ((double) _nbFixedPeriod) / _nbFixedPaymentYear;
        gp = -_nbFixedPeriod / 2.0 * (_nbFixedPeriod + 1.0) / (_nbFixedPaymentYear * _nbFixedPaymentYear);
        gpp = _nbFixedPeriod / 2.0 * (_nbFixedPeriod + 1.0) * (1.0 + (_nbFixedPeriod + 2.0) / 3.0) / (_nbFixedPaymentYear * _nbFixedPaymentYear * _nbFixedPaymentYear);
      }
      double h = Math.pow(1.0 + _tau * x, _eta);
      double hp = _eta * _tau * h / periodFactor;
      double hpp = (_eta - 1.0) * _tau * hp / periodFactor;
      double kp = hp / g - h * gp / (g * g);
      double kpp = hpp / g - 2 * hp * gp / (g * g) - h * (gpp / (g * g) - 2 * (gp * gp) / (g * g * g));
      return new double[] {kp, kpp};
    }

    /**
     * The Black-Scholes formula with numeraire 1 as function of the strike.
     * @param strike The strike.
     * @return The Black-Scholes formula.
     */
    double bs(double strike) {
      EuropeanVanillaOption option = new EuropeanVanillaOption(strike, _timeToExpiry, _isCall);
      return _sabrExtrapolation.price(option);
    }
  }
}
