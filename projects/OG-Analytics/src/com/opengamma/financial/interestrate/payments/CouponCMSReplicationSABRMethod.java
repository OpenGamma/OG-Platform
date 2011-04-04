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
import com.opengamma.financial.model.option.pricing.analytic.formula.BlackFunctionData;
import com.opengamma.financial.model.option.pricing.analytic.formula.BlackPriceFunction;
import com.opengamma.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.integration.RungeKuttaIntegrator1D;

/**
 *  Class used to compute the price of a CMS coupon by swaption replication with SABR Hagan formula.
 */
public class CouponCMSReplicationSABRMethod {

  /**
   * The par rate calculator.
   */
  private static final ParRateCalculator PRC = ParRateCalculator.getInstance();

  private double _integrationInterval;
  /**
   * Range of the integral. Used only for caps. Represent the approximation of infinity in the strike dimension.
   */
  private final int _nbIteration = 6;

  /** 
   * Default constructor. The default integration interval is 1.00 (100%).
   */
  public CouponCMSReplicationSABRMethod() {
    _integrationInterval = 1.00;
  }

  /**
   * Constructor of the CMS replication method with the integration range. 
   * @param integrationInterval Integration range.
   */
  public CouponCMSReplicationSABRMethod(double integrationInterval) {
    _integrationInterval = integrationInterval;
  }

  /**
   * Compute the price of a CMS coupon by replication in SABR framework. 
   * @param cmsCoupon The CMS coupon.
   * @param sabrData The SABR and curve data.
   * @return The coupon price.
   */
  public double price(CouponCMS cmsCoupon, SABRInterestRateDataBundle sabrData) {
    // TODO: implement CMS coupon as a cap with strike 0.
    SABRInterestRateParameter sabrParameter = sabrData.getSABRParameter();
    FixedCouponSwap<? extends Payment> underlyingSwap = cmsCoupon.getUnderlyingSwap();
    double forward = PRC.visit(underlyingSwap, sabrData);
    double discountFactor = sabrData.getCurve(underlyingSwap.getFixedLeg().getNthPayment(0).getFundingCurveName()).getDiscountFactor(cmsCoupon.getPaymentTime());
    CMSIntegrant integrant = new CMSIntegrant(cmsCoupon, sabrParameter, forward);
    double factor = discountFactor / integrant.h(forward) * integrant.g(forward);
    // Implementation comment: When rates are always positive, a CMS swap is equivalent to CMS a cap with strike 0.
    double strike = 0;
    double strikePart = integrant.k(strike) * integrant.bs(strike);
    double absoluteTolerance = 1.0 / (factor * cmsCoupon.getNotional() * cmsCoupon.getPaymentYearFraction());
    double relativeTolerance = 1E-10;
    RungeKuttaIntegrator1D integrator = new RungeKuttaIntegrator1D(absoluteTolerance, relativeTolerance, _nbIteration);
    double integralPart;
    try {
      integralPart = integrator.integrate(integrant, strike, strike + _integrationInterval);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    double priceCMS = factor * (strikePart + integralPart) * cmsCoupon.getNotional() * cmsCoupon.getPaymentYearFraction();
    return priceCMS;
  }

  /**
   * Gets the _integrationInterval field.
   * @return The integration interval.
   */
  public double getIntegrationInterval() {
    return _integrationInterval;
  }

  /**
   * Sets the _integrationInterval field.
   * @param integrationInterval  the _integrationInterval
   */
  public void setIntegrationInterval(double integrationInterval) {
    this._integrationInterval = integrationInterval;
  }

  /**
   * Inner class to implement the integration used in replication.
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
    private final double _strike = 0.0;
    private final double _forward;
    private final SABRInterestRateParameter _sabrParameter;
    private final BlackPriceFunction _blackFunction = new BlackPriceFunction();

    /**
     * 
     */
    public CMSIntegrant(CouponCMS cmsCoupon, SABRInterestRateParameter sabrParameter, double forward) {
      _nbFixedPeriod = cmsCoupon.getUnderlyingSwap().getFixedLeg().getPayments().length;
      _nbFixedPaymentYear = (int) Math.round(1.0 / cmsCoupon.getUnderlyingSwap().getFixedLeg().getNthPayment(0).getPaymentYearFraction());
      _tau = 1.0 / _nbFixedPaymentYear;
      _delta = cmsCoupon.getPaymentTime() - cmsCoupon.getSettlementTime();
      _eta = -_delta;
      _sabrParameter = sabrParameter;
      _timeToExpiry = cmsCoupon.getFixingTime();
      // TODO: A better notion of maturity may be required (using period?)
      AnnuityCouponFixed annuityFixed = cmsCoupon.getUnderlyingSwap().getFixedLeg();
      _maturity = annuityFixed.getNthPayment(annuityFixed.getNumberOfPayments() - 1).getPaymentTime() - cmsCoupon.getSettlementTime();
      _forward = forward;
    }

    @Override
    public Double evaluate(Double x) {
      double[] kD = kpkpp(x);
      // Implementation note: kD[0] contains the first derivative of k; kD[1] the second derivative of k. 
      return (kD[1] * (x - _strike) + 2.0 * kD[0]) * bs(x);
    }

    /**
     * The approximation of the discount factor as function of the swap rate.
     * @param x The swap rate.
     * @return The discount factor.
     */
    private double h(double x) {
      return Math.pow(1.0 + x / _nbFixedPaymentYear, _eta);
    }

    /**
     * The cash annuity.
     * @param x The swap rate.
     * @return The annuity.
     */
    private double g(double x) {
      if (x >= EPS) {
        double periodFactor = 1 + _tau * x;
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
        double periodFactor = 1 + _tau * x;
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
      double periodFactor = 1 + _tau * x;
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
      EuropeanVanillaOption option = new EuropeanVanillaOption(strike, _timeToExpiry, true);
      double volatility = _sabrParameter.getVolatility(_timeToExpiry, _maturity, strike, _forward);
      BlackFunctionData dataBlack = new BlackFunctionData(_forward, 1.0, volatility);
      Function1D<BlackFunctionData, Double> func = _blackFunction.getPriceFunction(option);
      return func.evaluate(dataBlack);
    }
  }

}
