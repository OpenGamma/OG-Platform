/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.payments;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.opengamma.financial.interestrate.ParRateCalculator;
import com.opengamma.financial.interestrate.ParRateCurveSensitivityCalculator;
import com.opengamma.financial.interestrate.PresentValueSABRSensitivityDataBundle;
import com.opengamma.financial.interestrate.PresentValueSensitivity;
import com.opengamma.financial.interestrate.annuity.definition.AnnuityCouponFixed;
import com.opengamma.financial.interestrate.swap.definition.FixedCouponSwap;
import com.opengamma.financial.model.option.definition.SABRInterestRateDataBundle;
import com.opengamma.financial.model.option.definition.SABRInterestRateParameter;
import com.opengamma.financial.model.option.pricing.analytic.formula.BlackFunctionData;
import com.opengamma.financial.model.option.pricing.analytic.formula.BlackPriceFunction;
import com.opengamma.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.financial.model.volatility.smile.function.SABRFormulaData;
import com.opengamma.financial.model.volatility.smile.function.SABRHaganVolatilityFunction;
import com.opengamma.financial.model.volatility.smile.function.VolatilityFunctionProvider;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.integration.RungeKuttaIntegrator1D;
import com.opengamma.util.tuple.DoublesPair;

/**
 *  Class used to compute the price of a CMS cap/floor by swaption replication on a SABR formula.
 *  Reference: Hagan, P. S. (2003). Convexity conundrums: Pricing CMS swaps, caps, and floors. Wilmott Magazine, March, pages 38--44.
 *  OpenGamma implementation note: Replication pricing for linear and TEC format CMS, Version 1.2, March 2011.
 */
public class CapFloorCMSSABRReplicationMethod {

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
  private double _integrationInterval;
  /**
   * Minimal number of integration steps in the replication.
   */
  private final int _nbIteration = 6;

  /** 
   * Default constructor of the CMS cap/floor replication method. The default integration interval is 1.00 (100%).
   */
  public CapFloorCMSSABRReplicationMethod() {
    _integrationInterval = 1.00;
  }

  /**
   * Constructor of the CMS cap/floor replication method with the integration range. 
   * @param integrationInterval Integration range.
   */
  public CapFloorCMSSABRReplicationMethod(final double integrationInterval) {
    _integrationInterval = integrationInterval;
  }

  /**
   * Gets the integration interval.
   * @return The integration interval.
   */
  public double getIntegrationInterval() {
    return _integrationInterval;
  }

  /**
   * Sets the integration interval.
   * @param integrationInterval The integration interval
   */
  public void setIntegrationInterval(final double integrationInterval) {
    this._integrationInterval = integrationInterval;
  }

  /**
   * Compute the present value of a CMS cap/floor by replication in SABR framework. 
   * @param cmsCapFloor The CMS cap/floor.
   * @param sabrData The SABR data bundle.
   * @return The present value.
   */
  public double presentValue(final CapFloorCMS cmsCapFloor, final SABRInterestRateDataBundle sabrData) {
    final SABRInterestRateParameter sabrParameter = sabrData.getSABRParameter();
    final FixedCouponSwap<? extends Payment> underlyingSwap = cmsCapFloor.getUnderlyingSwap();
    final double forward = PRC.visit(underlyingSwap, sabrData);
    final double discountFactor = sabrData.getCurve(underlyingSwap.getFixedLeg().getNthPayment(0).getFundingCurveName()).getDiscountFactor(cmsCapFloor.getPaymentTime());
    final CMSIntegrant integrant = new CMSIntegrant(cmsCapFloor, sabrParameter, forward);
    final double strike = cmsCapFloor.getStrike();
    @SuppressWarnings("synthetic-access")
    final double strikePart = discountFactor * integrant.k(strike) * integrant.g(forward) / integrant.h(forward) * integrant.bs(strike);
    final double absoluteTolerance = 1.0 / (discountFactor * Math.abs(cmsCapFloor.getNotional()) * cmsCapFloor.getPaymentYearFraction());
    final double relativeTolerance = 1E-10;
    final RungeKuttaIntegrator1D integrator = new RungeKuttaIntegrator1D(absoluteTolerance, relativeTolerance, _nbIteration);
    double integralPart;
    try {
      if (cmsCapFloor.isCap()) {
        integralPart = discountFactor * integrator.integrate(integrant, strike, strike + _integrationInterval);
      } else {
        integralPart = discountFactor * integrator.integrate(integrant, 0.0, strike);
      }
    } catch (final Exception e) {
      throw new RuntimeException(e);
    }
    final double priceCMS = (strikePart + integralPart) * cmsCapFloor.getNotional() * cmsCapFloor.getPaymentYearFraction();
    return priceCMS;
  }

  /**
   * Computes the present value sensitivity to the yield curves of a CMS cap/floor by replication in SABR framework. 
   * @param cmsCapFloor The CMS cap/floor.
   * @param sabrData The SABR data bundle. The SABR function need to be the Hagan function.
   * @return The present value sensitivity to curves.
   */
  @SuppressWarnings("synthetic-access")
  public PresentValueSensitivity presentValueSensitivity(final CapFloorCMS cmsCapFloor, final SABRInterestRateDataBundle sabrData) {
    final SABRInterestRateParameter sabrParameter = sabrData.getSABRParameter();
    final FixedCouponSwap<? extends Payment> underlyingSwap = cmsCapFloor.getUnderlyingSwap();
    final double forward = PRC.visit(underlyingSwap, sabrData);
    final double discountFactor = sabrData.getCurve(underlyingSwap.getFixedLeg().getNthPayment(0).getFundingCurveName()).getDiscountFactor(cmsCapFloor.getPaymentTime());
    final double strike = cmsCapFloor.getStrike();
    // Common
    final CMSIntegrant integrantPrice = new CMSIntegrant(cmsCapFloor, sabrParameter, forward);
    final CMSDeltaIntegrant integrantDelta = new CMSDeltaIntegrant(cmsCapFloor, sabrParameter, forward);
    final double factor = discountFactor / integrantDelta.h(forward) * integrantDelta.g(forward);
    final double absoluteTolerance = 1.0 / (factor * Math.abs(cmsCapFloor.getNotional()) * cmsCapFloor.getPaymentYearFraction());
    final double relativeTolerance = 1E-10;
    final RungeKuttaIntegrator1D integrator = new RungeKuttaIntegrator1D(absoluteTolerance, relativeTolerance, _nbIteration);
    // Price
    final double[] bs = integrantDelta.bsbsp(strike);
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
    PresentValueSensitivity result = new PresentValueSensitivity(resultMap);
    final PresentValueSensitivity forwardDr = new PresentValueSensitivity(PRSC.visit(cmsCapFloor.getUnderlyingSwap(), sabrData));
    result = result.add(forwardDr.multiply(deltaS0));
    return result;
  }

  /**
   * Computes the present value sensitivity to the SABR parameters of a CMS cap/floor by replication in SABR framework. 
   * @param cmsCapFloor The CMS cap/floor.
   * @param sabrData The SABR data bundle. The SABR function need to be the Hagan function.
   * @return The present value sensitivity to SABR parameters.
   */
  @SuppressWarnings("synthetic-access")
  public PresentValueSABRSensitivityDataBundle presentValueSABRSensitivity(final CapFloorCMS cmsCapFloor, final SABRInterestRateDataBundle sabrData) {
    final SABRInterestRateParameter sabrParameter = sabrData.getSABRParameter();
    final FixedCouponSwap<? extends Payment> underlyingSwap = cmsCapFloor.getUnderlyingSwap();
    final double forward = PRC.visit(underlyingSwap, sabrData);
    final double discountFactor = sabrData.getCurve(underlyingSwap.getFixedLeg().getNthPayment(0).getFundingCurveName()).getDiscountFactor(cmsCapFloor.getPaymentTime());
    double strike = cmsCapFloor.getStrike();
    // Implementation note: to avoid singularity at 0.
    strike = (strike < 1E-4 ? 1E-4 : strike);
    final double maturity = underlyingSwap.getFixedLeg().getNthPayment(underlyingSwap.getFixedLeg().getNumberOfPayments() - 1).getPaymentTime() - cmsCapFloor.getSettlementTime();
    final CMSVegaIntegrant integrantVega = new CMSVegaIntegrant(cmsCapFloor, sabrParameter, forward);
    final double factor = discountFactor / integrantVega.h(forward) * integrantVega.g(forward);
    final double absoluteTolerance = 1.0 / (factor * Math.abs(cmsCapFloor.getNotional()) * cmsCapFloor.getPaymentYearFraction());
    final double relativeTolerance = 1E-10;
    final RungeKuttaIntegrator1D integrator = new RungeKuttaIntegrator1D(absoluteTolerance, relativeTolerance, _nbIteration);
    final double factor2 = factor * integrantVega.k(strike) * integrantVega.bs(strike);
    final double[] strikePartPrice = new double[3];
    final double[] volatilityAdjoint = sabrData.getSABRParameter().getVolatilityAdjoint(cmsCapFloor.getFixingTime(), maturity, strike, forward);
    strikePartPrice[0] = factor2 * volatilityAdjoint[3];
    strikePartPrice[1] = factor2 * volatilityAdjoint[4];
    strikePartPrice[2] = factor2 * volatilityAdjoint[5];
    final double[] integralPart = new double[3];
    final double[] totalSensi = new double[3];
    for (int loopparameter = 0; loopparameter < 3; loopparameter++) {
      integrantVega.setParameterIndex(loopparameter);
      try {
        if (cmsCapFloor.isCap()) {
          integralPart[loopparameter] = discountFactor * integrator.integrate(integrantVega, strike, strike + _integrationInterval);
        } else {
          integralPart[loopparameter] = discountFactor * integrator.integrate(integrantVega, 0.0, strike);
        }
      } catch (final Exception e) {
        throw new RuntimeException(e);
      }
      totalSensi[loopparameter] = (strikePartPrice[loopparameter] + integralPart[loopparameter]) * cmsCapFloor.getNotional() * cmsCapFloor.getPaymentYearFraction();
    }
    final PresentValueSABRSensitivityDataBundle sensi = new PresentValueSABRSensitivityDataBundle();
    final DoublesPair expiryMaturity = new DoublesPair(cmsCapFloor.getFixingTime(), maturity);
    sensi.addAlpha(expiryMaturity, totalSensi[0]);
    sensi.addRho(expiryMaturity, totalSensi[1]);
    sensi.addNu(expiryMaturity, totalSensi[2]);
    return sensi;
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
    private final SABRFormulaData _sabrData;
    /**
     * The function containing the SABR volatility formula.
     */
    private final VolatilityFunctionProvider<SABRFormulaData> _sabrFunction;
    private final BlackPriceFunction _blackFunction = new BlackPriceFunction();
    private final boolean _isCall;

    /**
     * Constructor
     */
    public CMSIntegrant(final CapFloorCMS cmsCap, final SABRInterestRateParameter sabrParameter, final double forward) {
      _nbFixedPeriod = cmsCap.getUnderlyingSwap().getFixedLeg().getPayments().length;
      _nbFixedPaymentYear = (int) Math.round(1.0 / cmsCap.getUnderlyingSwap().getFixedLeg().getNthPayment(0).getPaymentYearFraction());
      _tau = 1.0 / _nbFixedPaymentYear;
      _delta = cmsCap.getPaymentTime() - cmsCap.getSettlementTime();
      _eta = -_delta;
      //      _sabrParameter = sabrParameter;
      _timeToExpiry = cmsCap.getFixingTime();
      // TODO: A better notion of maturity may be required (using period?)
      final AnnuityCouponFixed annuityFixed = cmsCap.getUnderlyingSwap().getFixedLeg();
      _maturity = annuityFixed.getNthPayment(annuityFixed.getNumberOfPayments() - 1).getPaymentTime() - cmsCap.getSettlementTime();
      _forward = forward;
      final DoublesPair expiryMaturity = new DoublesPair(_timeToExpiry, _maturity);
      final double alpha = sabrParameter.getAlpha(expiryMaturity);
      final double beta = sabrParameter.getBeta(expiryMaturity);
      final double rho = sabrParameter.getRho(expiryMaturity);
      final double nu = sabrParameter.getNu(expiryMaturity);
      _sabrData = new SABRFormulaData(_forward, alpha, beta, nu, rho);
      _sabrFunction = sabrParameter.getSabrFunction();
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
    private double h(final double x) {
      return Math.pow(1.0 + _tau * x, _eta);
    }

    /**
     * The cash annuity.
     * @param x The swap rate.
     * @return The annuity.
     */
    private double g(final double x) {
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
    private double k(final double x) {
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
    private double[] kpkpp(final double x) {
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
      return new double[] {kp, kpp};
    }

    /**
     * The Black-Scholes formula with numeraire 1 as function of the strike.
     * @param strike The strike.
     * @return The Black-Scholes formula.
     */
    double bs(final double strike) {
      final EuropeanVanillaOption option = new EuropeanVanillaOption(strike, _timeToExpiry, _isCall);
      final Function1D<SABRFormulaData, Double> funcSabr = _sabrFunction.getVolatilityFunction(option);
      final double volatility = funcSabr.evaluate(_sabrData);
      final BlackFunctionData dataBlack = new BlackFunctionData(_forward, 1.0, volatility);
      final Function1D<BlackFunctionData, Double> func = _blackFunction.getPriceFunction(option);
      return func.evaluate(dataBlack);
    }
  }

  /**
   * Inner class to implement the integration used in price replication.
   */
  //TODO Refactor CMSDeltaIntegrant and CMSVegaIntegrant as extension of CMSIntegrant
  private class CMSDeltaIntegrant extends Function1D<Double, Double> {
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
    private final SABRFormulaData _sabrData;
    /**
     * The function containing the SABR volatility formula.
     */
    private final VolatilityFunctionProvider<SABRFormulaData> _sabrFunction;
    private final BlackPriceFunction _blackFunction = new BlackPriceFunction();
    private final boolean _isCall;

    /**
     * 
     */
    public CMSDeltaIntegrant(final CapFloorCMS cmsCap, final SABRInterestRateParameter sabrParameter, final double forward) {
      _nbFixedPeriod = cmsCap.getUnderlyingSwap().getFixedLeg().getPayments().length;
      _nbFixedPaymentYear = (int) Math.round(1.0 / cmsCap.getUnderlyingSwap().getFixedLeg().getNthPayment(0).getPaymentYearFraction());
      _tau = 1.0 / _nbFixedPaymentYear;
      _delta = cmsCap.getPaymentTime() - cmsCap.getSettlementTime();
      _eta = -_delta;
      _timeToExpiry = cmsCap.getFixingTime();
      _forward = forward;
      final AnnuityCouponFixed annuityFixed = cmsCap.getUnderlyingSwap().getFixedLeg();
      _maturity = annuityFixed.getNthPayment(annuityFixed.getNumberOfPayments() - 1).getPaymentTime() - cmsCap.getSettlementTime();
      final DoublesPair expiryMaturity = new DoublesPair(_timeToExpiry, _maturity);
      final double alpha = sabrParameter.getAlpha(expiryMaturity);
      final double beta = sabrParameter.getBeta(expiryMaturity);
      final double rho = sabrParameter.getRho(expiryMaturity);
      final double nu = sabrParameter.getNu(expiryMaturity);
      _sabrData = new SABRFormulaData(_forward, alpha, beta, nu, rho);
      _sabrFunction = sabrParameter.getSabrFunction();
      _isCall = cmsCap.isCap();
      _strike = cmsCap.getStrike();
    }

    @Override
    public Double evaluate(final Double x) {
      final double[] kD = kpkpp(x);
      // Implementation note: kD[0] contains the first derivative of k; kD[1] the second derivative of k. 
      final double[] bs = bsbsp(x);
      final double[] n = nnp(_forward);
      // TODO: Store the value of n?
      return (kD[1] * (x - _strike) + 2.0 * kD[0]) * (n[1] * bs[0] + n[0] * bs[1]);
    }

    /**
     * The approximation of the discount factor as function of the swap rate.
     * @param x The swap rate.
     * @return The discount factor.
     */
    private double h(final double x) {
      return Math.pow(1.0 + _tau * x, _eta);
    }

    private double[] hhp(final double x) {
      final double[] result = new double[2];
      result[0] = Math.pow(1.0 + _tau * x, _eta);
      result[1] = _eta * _tau * result[0] / (1 + x * _tau);
      return result;
    }

    /**
     * The cash annuity.
     * @param x The swap rate.
     * @return The annuity.
     */
    private double g(final double x) {
      if (x >= EPS) {
        final double periodFactor = 1 + x / _nbFixedPaymentYear;
        final double nPeriodDiscount = Math.pow(periodFactor, -_nbFixedPeriod);
        return 1.0 / x * (1.0 - nPeriodDiscount);
      }
      return ((double) _nbFixedPeriod) / _nbFixedPaymentYear;
    }

    private double[] ggp(final double x) {
      final double[] result = new double[2];
      if (x >= EPS) {
        final double periodFactor = 1 + x / _nbFixedPaymentYear;
        final double nPeriodDiscount = Math.pow(periodFactor, -_nbFixedPeriod);
        result[0] = 1.0 / x * (1.0 - nPeriodDiscount);
        result[1] = -result[0] / x + _tau * _nbFixedPeriod / x * nPeriodDiscount / periodFactor;
      } else {
        result[0] = _nbFixedPeriod * _tau;
        result[1] = -_nbFixedPeriod * (_nbFixedPeriod + 1.0) * _tau * _tau / 2.0;
      }
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

    /**
     * The factor used in the strike part and in the integration of the replication.
     * @param x The swap rate. 
     * @return The factor.
     */
    private double k(final double x) {
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
    private double[] kpkpp(final double x) {
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
      return new double[] {kp, kpp};
    }

    /**
     * The Black-Scholes formula and its derivative with respect to the forward.
     * @param strike The strike.
     * @return The Black-Scholes formula and its derivative.
     */
    double[] bsbsp(final double strike) {
      final double[] result = new double[2];
      final EuropeanVanillaOption option = new EuropeanVanillaOption(strike, _timeToExpiry, _isCall);
      final SABRHaganVolatilityFunction sabrHaganFunction = (SABRHaganVolatilityFunction) _sabrFunction;
      final double[] volatility = sabrHaganFunction.getVolatilityAdjoint(option, _sabrData);
      final BlackFunctionData dataBlack = new BlackFunctionData(_forward, 1.0, volatility[0]);
      final double[] bsAdjoint = _blackFunction.getPriceAdjoint(option, dataBlack);
      result[0] = bsAdjoint[0];
      result[1] = bsAdjoint[1] + bsAdjoint[2] * volatility[1];
      return result;
    }
  }

  /**
   * Inner class to implement the integration used in price replication.
   */
  private class CMSVegaIntegrant extends Function1D<Double, Double> {
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
    private final SABRFormulaData _sabrData;
    /**
     * The function containing the SABR volatility formula.
     */
    private final VolatilityFunctionProvider<SABRFormulaData> _sabrFunction;
    //    private final SABRInterestRateParameter _sabrParameter;
    private final BlackPriceFunction _blackFunction = new BlackPriceFunction();
    private final boolean _isCall;
    private int _parameterIndex;

    /**
     * 
     */
    public CMSVegaIntegrant(final CapFloorCMS cmsCap, final SABRInterestRateParameter sabrParameter, final double forward) {
      _nbFixedPeriod = cmsCap.getUnderlyingSwap().getFixedLeg().getPayments().length;
      _nbFixedPaymentYear = (int) Math.round(1.0 / cmsCap.getUnderlyingSwap().getFixedLeg().getNthPayment(0).getPaymentYearFraction());
      _tau = 1.0 / _nbFixedPaymentYear;
      _delta = cmsCap.getPaymentTime() - cmsCap.getSettlementTime();
      _eta = -_delta;
      //      _sabrParameter = sabrParameter;
      _timeToExpiry = cmsCap.getFixingTime();
      _forward = forward;
      final AnnuityCouponFixed annuityFixed = cmsCap.getUnderlyingSwap().getFixedLeg();
      _maturity = annuityFixed.getNthPayment(annuityFixed.getNumberOfPayments() - 1).getPaymentTime() - cmsCap.getSettlementTime();
      final DoublesPair expiryMaturity = new DoublesPair(_timeToExpiry, _maturity);
      final double alpha = sabrParameter.getAlpha(expiryMaturity);
      final double beta = sabrParameter.getBeta(expiryMaturity);
      final double rho = sabrParameter.getRho(expiryMaturity);
      final double nu = sabrParameter.getNu(expiryMaturity);
      _sabrData = new SABRFormulaData(_forward, alpha, beta, nu, rho);
      _sabrFunction = sabrParameter.getSabrFunction();
      _isCall = cmsCap.isCap();
      // Implementation note: to avoid singularity at 0.
      _strike = (cmsCap.getStrike() < 1E-4 ? 1E-4 : cmsCap.getStrike());
      _factor = g(_forward) / h(_forward);
    }

    @Override
    public Double evaluate(final Double x) {
      final double[] kD = kpkpp(x);
      // Implementation note: kD[0] contains the first derivative of k; kD[1] the second derivative of k. 
      final EuropeanVanillaOption option = new EuropeanVanillaOption(x, _timeToExpiry, _isCall);
      final SABRHaganVolatilityFunction sabrHaganFunction = (SABRHaganVolatilityFunction) _sabrFunction;
      final double[] volatilityAdjoint = sabrHaganFunction.getVolatilityAdjoint(option, _sabrData);
      return _factor * (kD[1] * (x - _strike) + 2.0 * kD[0]) * bs(x) * volatilityAdjoint[3 + _parameterIndex];
    }

    /**
     * Sets the _parameterIndex field.
     * @param _parameterIndex  the _parameterIndex
     */
    public void setParameterIndex(final int parameterIndex) {
      this._parameterIndex = parameterIndex;
    }

    /**
     * The approximation of the discount factor as function of the swap rate.
     * @param x The swap rate.
     * @return The discount factor.
     */
    private double h(final double x) {
      return Math.pow(1.0 + _tau * x, _eta);
    }

    /**
     * The cash annuity.
     * @param x The swap rate.
     * @return The annuity.
     */
    private double g(final double x) {
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
    private double k(final double x) {
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
    private double[] kpkpp(final double x) {
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
      return new double[] {kp, kpp};
    }

    /**
     * The derivative with respect to the volatility of the Black-Scholes formula with numeraire 1.
     * @param strike The strike.
     * @return The Black-Scholes formula derivative with respect to volatility.
     */
    double bs(final double strike) {
      final EuropeanVanillaOption option = new EuropeanVanillaOption(strike, _timeToExpiry, _isCall);
      final Function1D<SABRFormulaData, Double> funcSabr = _sabrFunction.getVolatilityFunction(option);
      final double volatility = funcSabr.evaluate(_sabrData);
      //      double volatility = _sabrParameter.getVolatility(_timeToExpiry, _maturity, strike, _forward);
      final BlackFunctionData dataBlack = new BlackFunctionData(_forward, 1.0, volatility);
      final double[] bsAdjoint = _blackFunction.getPriceAdjoint(option, dataBlack);
      return bsAdjoint[2];
    }
  }

}
