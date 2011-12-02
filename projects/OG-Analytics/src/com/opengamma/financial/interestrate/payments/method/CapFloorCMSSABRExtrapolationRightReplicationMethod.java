/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.payments.method;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.interestrate.InstrumentDerivative;
import com.opengamma.financial.interestrate.ParRateCalculator;
import com.opengamma.financial.interestrate.ParRateCurveSensitivityCalculator;
import com.opengamma.financial.interestrate.InterestRateCurveSensitivity;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.interestrate.annuity.definition.AnnuityCouponFixed;
import com.opengamma.financial.interestrate.method.PricingMethod;
import com.opengamma.financial.interestrate.payments.CapFloorCMS;
import com.opengamma.financial.interestrate.payments.Payment;
import com.opengamma.financial.interestrate.swap.definition.FixedCouponSwap;
import com.opengamma.financial.model.option.definition.SABRInterestRateDataBundle;
import com.opengamma.financial.model.option.definition.SABRInterestRateParameters;
import com.opengamma.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.financial.model.option.pricing.analytic.formula.SABRExtrapolationRightFunction;
import com.opengamma.financial.model.volatility.smile.function.SABRFormulaData;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.integration.RungeKuttaIntegrator1D;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.tuple.DoublesPair;

/**
 *  Class used to compute the price of a CMS cap/floor by swaption replication on a SABR formula with extrapolation.
 *  Reference: Hagan, P. S. (2003). Convexity conundrums: Pricing CMS swaps, caps, and floors. Wilmott Magazine, March, pages 38--44.
 *  OpenGamma implementation note: Replication pricing for linear and TEC format CMS, Version 1.2, March 2011.
 *  OpenGamma implementation note for the extrapolation: Smile extrapolation, version 1.2, May 2011.
 */
public class CapFloorCMSSABRExtrapolationRightReplicationMethod implements PricingMethod {

  /**
   * The par rate calculator.
   */
  private static final ParRateCalculator PRC = ParRateCalculator.getInstance();
  /**
   * The par rate sensitivity calculator.
   */
  private static final ParRateCurveSensitivityCalculator PRSC = ParRateCurveSensitivityCalculator.getInstance();
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
  public CapFloorCMSSABRExtrapolationRightReplicationMethod(final double cutOffStrike, final double mu) {
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
  public CurrencyAmount presentValue(final CapFloorCMS cmsCapFloor, final SABRInterestRateDataBundle sabrData) {
    Validate.notNull(cmsCapFloor);
    Validate.notNull(sabrData);
    final SABRInterestRateParameters sabrParameter = sabrData.getSABRParameter();
    final FixedCouponSwap<? extends Payment> underlyingSwap = cmsCapFloor.getUnderlyingSwap();
    final double forward = PRC.visit(underlyingSwap, sabrData);
    final double discountFactorTp = sabrData.getCurve(underlyingSwap.getFixedLeg().getNthPayment(0).getFundingCurveName()).getDiscountFactor(cmsCapFloor.getPaymentTime());
    final CMSIntegrant integrant = new CMSIntegrant(cmsCapFloor, sabrParameter, forward, _cutOffStrike, _mu);
    final double strike = cmsCapFloor.getStrike();
    final double factor = discountFactorTp / integrant.h(forward) * integrant.g(forward);
    final double strikePart = factor * integrant.k(strike) * integrant.bs(strike);
    final double absoluteTolerance = 1.0 / (factor * Math.abs(cmsCapFloor.getNotional()) * cmsCapFloor.getPaymentYearFraction());
    final double relativeTolerance = 1E-10;
    final RungeKuttaIntegrator1D integrator = new RungeKuttaIntegrator1D(absoluteTolerance, relativeTolerance, _nbIteration);
    double integralPart;
    try {
      if (cmsCapFloor.isCap()) {
        integralPart = discountFactorTp * integrator.integrate(integrant, strike, strike + _integrationInterval);
      } else {
        integralPart = discountFactorTp * integrator.integrate(integrant, 0.0, strike);
      }
    } catch (final Exception e) {
      throw new RuntimeException(e);
    }
    final double priceCMS = (strikePart + integralPart) * cmsCapFloor.getNotional() * cmsCapFloor.getPaymentYearFraction();
    return CurrencyAmount.of(cmsCapFloor.getCurrency(), priceCMS);
  }

  @Override
  public CurrencyAmount presentValue(InstrumentDerivative instrument, YieldCurveBundle curves) {
    Validate.isTrue(instrument instanceof CapFloorCMS, "CMS cap/floor");
    Validate.isTrue(curves instanceof SABRInterestRateDataBundle, "Bundle should contain SABR data");
    return presentValue((CapFloorCMS) instrument, (SABRInterestRateDataBundle) curves);
  }

  /**
   * Computes the present value sensitivity to the yield curves of a CMS cap/floor by replication in the SABR framework with extrapolation on the right. 
   * @param cmsCapFloor The CMS cap/floor.
   * @param sabrData The SABR data bundle. The SABR function need to be the Hagan function.
   * @return The present value sensitivity to curves.
   */
  public InterestRateCurveSensitivity presentValueSensitivity(final CapFloorCMS cmsCapFloor, final SABRInterestRateDataBundle sabrData) {
    Validate.notNull(cmsCapFloor);
    Validate.notNull(sabrData);
    final SABRInterestRateParameters sabrParameter = sabrData.getSABRParameter();
    final FixedCouponSwap<? extends Payment> underlyingSwap = cmsCapFloor.getUnderlyingSwap();
    final double forward = PRC.visit(underlyingSwap, sabrData);
    final double discountFactor = sabrData.getCurve(underlyingSwap.getFixedLeg().getNthPayment(0).getFundingCurveName()).getDiscountFactor(cmsCapFloor.getPaymentTime());
    final double strike = cmsCapFloor.getStrike();
    // Common
    final CMSIntegrant integrantPrice = new CMSIntegrant(cmsCapFloor, sabrParameter, forward, _cutOffStrike, _mu);
    final CMSDeltaIntegrant integrantDelta = new CMSDeltaIntegrant(cmsCapFloor, sabrParameter, forward, _cutOffStrike, _mu);
    final double factor = discountFactor / integrantDelta.h(forward) * integrantDelta.g(forward);
    final double absoluteTolerance = 1.0 / (factor * Math.abs(cmsCapFloor.getNotional()) * cmsCapFloor.getPaymentYearFraction());
    final double relativeTolerance = 1E-10;
    final RungeKuttaIntegrator1D integrator = new RungeKuttaIntegrator1D(absoluteTolerance, relativeTolerance, _nbIteration);
    // Price
    final double[] bs = integrantDelta.bsbsp(strike);
    @SuppressWarnings("synthetic-access")
    final double[] n = integrantDelta.nnp(forward);
    final double strikePartPrice = discountFactor * integrantDelta.k(strike) * n[0] * bs[0];
    double integralPartPrice;
    try {
      if (cmsCapFloor.isCap()) {
        integralPartPrice = discountFactor * integrator.integrate(integrantPrice, strike, strike + _integrationInterval);
      } else {
        integralPartPrice = discountFactor * integrator.integrate(integrantPrice, 0.0, strike);
      }
    } catch (final Exception e) {
      throw new RuntimeException(e);
    }
    final double price = (strikePartPrice + integralPartPrice) * cmsCapFloor.getNotional() * cmsCapFloor.getPaymentYearFraction();
    // Delta
    final double strikePart = discountFactor * integrantDelta.k(strike) * (n[1] * bs[0] + n[0] * bs[1]);
    double integralPart;
    try {
      if (cmsCapFloor.isCap()) {
        integralPart = discountFactor * integrator.integrate(integrantDelta, strike, strike + _integrationInterval);
      } else {
        integralPart = discountFactor * integrator.integrate(integrantDelta, 0.0, strike);
      }
    } catch (final Exception e) {
      throw new RuntimeException(e);
    }
    final double deltaS0 = (strikePart + integralPart) * cmsCapFloor.getNotional() * cmsCapFloor.getPaymentYearFraction();
    final double deltaPD = price / discountFactor;

    final double sensiDF = -cmsCapFloor.getPaymentTime() * discountFactor * deltaPD;
    final List<DoublesPair> list = new ArrayList<DoublesPair>();
    list.add(new DoublesPair(cmsCapFloor.getPaymentTime(), sensiDF));
    final Map<String, List<DoublesPair>> resultMap = new HashMap<String, List<DoublesPair>>();
    resultMap.put(cmsCapFloor.getUnderlyingSwap().getFixedLeg().getNthPayment(0).getFundingCurveName(), list);
    InterestRateCurveSensitivity result = new InterestRateCurveSensitivity(resultMap);
    final InterestRateCurveSensitivity forwardDr = new InterestRateCurveSensitivity(PRSC.visit(cmsCapFloor.getUnderlyingSwap(), sabrData));
    result = result.add(forwardDr.multiply(deltaS0));
    return result;

  }

  /**
   * Inner class to implement the integration used in price replication.
   */
  private class CMSIntegrant extends Function1D<Double, Double> {
    protected static final double EPS = 1E-10;
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
     * Gets the _nbFixedPeriod field.
     * @return the _nbFixedPeriod
     */
    public int getNbFixedPeriod() {
      return _nbFixedPeriod;
    }

    /**
     * Gets the _nbFixedPaymentYear field.
     * @return the _nbFixedPaymentYear
     */
    public int getNbFixedPaymentYear() {
      return _nbFixedPaymentYear;
    }

    /**
     * Gets the _tau field.
     * @return the _tau
     */
    public double getTau() {
      return _tau;
    }

    /**
     * Gets the _eta field.
     * @return the _eta
     */
    public double getEta() {
      return _eta;
    }

    /**
     * Gets the _timeToExpiry field.
     * @return the _timeToExpiry
     */
    public double getTimeToExpiry() {
      return _timeToExpiry;
    }

    /**
     * Gets the _sabrExtrapolation field.
     * @return the _sabrExtrapolation
     */
    public SABRExtrapolationRightFunction getSabrExtrapolation() {
      return _sabrExtrapolation;
    }

    /**
     * Gets the _isCall field.
     * @return the _isCall
     */
    public boolean isCall() {
      return _isCall;
    }

    /**
     * Gets the _strike field.
     * @return the _strike
     */
    public double getStrike() {
      return _strike;
    }

    /**
     * Gets the _forward field.
     * @return the _forward
     */
    public double getForward() {
      return _forward;
    }

    /**
     * Constructor
     */
    public CMSIntegrant(final CapFloorCMS cmsCap, final SABRInterestRateParameters sabrParameter, final double forward, final double cutOffStrike, final double mu) {
      _nbFixedPeriod = cmsCap.getUnderlyingSwap().getFixedLeg().getPayments().length;
      _nbFixedPaymentYear = (int) Math.round(1.0 / cmsCap.getUnderlyingSwap().getFixedLeg().getNthPayment(0).getPaymentYearFraction());
      _tau = 1.0 / _nbFixedPaymentYear;
      _delta = cmsCap.getPaymentTime() - cmsCap.getSettlementTime();
      _eta = -_delta;
      _timeToExpiry = cmsCap.getFixingTime();
      final AnnuityCouponFixed annuityFixed = cmsCap.getUnderlyingSwap().getFixedLeg();
      _maturity = annuityFixed.getNthPayment(annuityFixed.getNumberOfPayments() - 1).getPaymentTime() - cmsCap.getSettlementTime();
      _forward = forward;
      final DoublesPair expiryMaturity = new DoublesPair(_timeToExpiry, _maturity);
      final double alpha = sabrParameter.getAlpha(expiryMaturity);
      final double beta = sabrParameter.getBeta(expiryMaturity);
      final double rho = sabrParameter.getRho(expiryMaturity);
      final double nu = sabrParameter.getNu(expiryMaturity);
      final SABRFormulaData sabrData = new SABRFormulaData(alpha, beta, rho, nu);
      _sabrExtrapolation = new SABRExtrapolationRightFunction(forward, sabrData, cutOffStrike, _timeToExpiry, mu);
      _isCall = cmsCap.isCap();
      _strike = cmsCap.getStrike();
      _factor = g(_forward) / h(_forward);
    }

    @Override
    public Double evaluate(final Double x) {
      final double[] kD = kpkpp(x);
      // Implementation note: kD[0] contains the first derivative of k; kD[1] the second derivative of k. 
      return (kD[1] * (x - _strike) + 2.0 * kD[0]) * bs(x) * _factor;
    }

    /**
     * The approximation of the discount factor as function of the swap rate.
     * @param x The swap rate.
     * @return The discount factor.
     */
    double h(final double x) {
      return Math.pow(1.0 + _tau * x, _eta);
    }

    /**
     * The cash annuity.
     * @param x The swap rate.
     * @return The annuity.
     */
    double g(final double x) {
      if (x >= EPS) {
        final double periodFactor = 1 + x / _nbFixedPaymentYear;
        final double nPeriodDiscount = Math.pow(periodFactor, -_nbFixedPeriod);
        return 1.0 / x * (1.0 - nPeriodDiscount);
      }
      return ((double) _nbFixedPeriod) / _nbFixedPaymentYear;
    }

    /**
     * The factor used in the strike part and in the integration of the replication.
     * @param x The swap rate. 
     * @return The factor.
     */
    double k(final double x) {
      double g;
      double h;
      if (x >= EPS) {
        final double periodFactor = 1 + x / _nbFixedPaymentYear;
        final double nPeriodDiscount = Math.pow(periodFactor, -_nbFixedPeriod);
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
    protected double[] kpkpp(final double x) {
      final double periodFactor = 1 + x / _nbFixedPaymentYear;
      final double nPeriodDiscount = Math.pow(periodFactor, -_nbFixedPeriod);
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
      final double h = Math.pow(1.0 + _tau * x, _eta);
      final double hp = _eta * _tau * h / periodFactor;
      final double hpp = (_eta - 1.0) * _tau * hp / periodFactor;
      final double kp = hp / g - h * gp / (g * g);
      final double kpp = hpp / g - 2 * hp * gp / (g * g) - h * (gpp / (g * g) - 2 * (gp * gp) / (g * g * g));
      return new double[] {kp, kpp };
    }

    /**
     * The Black-Scholes formula with numeraire 1 as function of the strike.
     * @param strike The strike.
     * @return The Black-Scholes formula.
     */
    double bs(final double strike) {
      final EuropeanVanillaOption option = new EuropeanVanillaOption(strike, _timeToExpiry, _isCall);
      return _sabrExtrapolation.price(option);
    }
  }

  private class CMSDeltaIntegrant extends CMSIntegrant {

    public CMSDeltaIntegrant(final CapFloorCMS cmsCap, final SABRInterestRateParameters sabrParameter, final double forward, final double cutOffStrike, final double mu) {
      super(cmsCap, sabrParameter, forward, cutOffStrike, mu);
    }

    @Override
    public Double evaluate(final Double x) {
      final double[] kD = kpkpp(x);
      // Implementation note: kD[0] contains the first derivative of k; kD[1] the second derivative of k. 
      final double[] bs = bsbsp(x);
      final double[] n = nnp(getForward());
      return (kD[1] * (x - getStrike()) + 2.0 * kD[0]) * (n[1] * bs[0] + n[0] * bs[1]);
    }

    /**
     * The Black-Scholes formula and its derivative with respect to the forward.
     * @param strike The strike.
     * @return The Black-Scholes formula and its derivative.
     */
    double[] bsbsp(final double strike) {
      final double[] result = new double[2];
      final EuropeanVanillaOption option = new EuropeanVanillaOption(strike, getTimeToExpiry(), isCall());
      result[0] = getSabrExtrapolation().price(option);
      result[1] = getSabrExtrapolation().priceDerivativeForward(option);
      return result;
    }

    private double[] nnp(final double x) {
      final double[] result = new double[2];
      final double[] ggp = ggp(x);
      final double[] hhp = hhp(x);
      result[0] = ggp[0] / hhp[0];
      result[1] = ggp[1] / hhp[0] - ggp[0] * hhp[1] / (hhp[0] * hhp[0]);
      return result;
    }

    private double[] ggp(final double x) {
      final double[] result = new double[2];
      if (x >= EPS) {
        final double periodFactor = 1 + x / getNbFixedPaymentYear();
        final double nPeriodDiscount = Math.pow(periodFactor, -getNbFixedPeriod());
        result[0] = 1.0 / x * (1.0 - nPeriodDiscount);
        result[1] = -result[0] / x + getTau() * getNbFixedPeriod() / x * nPeriodDiscount / periodFactor;
      } else {
        result[0] = getNbFixedPeriod() * getTau();
        result[1] = -getNbFixedPeriod() * (getNbFixedPeriod() + 1.0) * getTau() * getTau() / 2.0;
      }
      return result;
    }

    private double[] hhp(final double x) {
      final double[] result = new double[2];
      result[0] = Math.pow(1.0 + getTau() * x, getEta());
      result[1] = getEta() * getTau() * result[0] / (1 + x * getTau());
      return result;
    }

  }
}
