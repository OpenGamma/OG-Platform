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
import com.opengamma.financial.interestrate.PresentValueSABRSensitivity;
import com.opengamma.financial.interestrate.PresentValueSensitivity;
import com.opengamma.financial.interestrate.annuity.definition.AnnuityCouponFixed;
import com.opengamma.financial.interestrate.swap.definition.FixedCouponSwap;
import com.opengamma.financial.model.option.definition.SABRInterestRateDataBundle;
import com.opengamma.financial.model.option.definition.SABRInterestRateParameter;
import com.opengamma.financial.model.option.pricing.analytic.formula.BlackFunctionData;
import com.opengamma.financial.model.option.pricing.analytic.formula.BlackPriceFunction;
import com.opengamma.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.integration.RungeKuttaIntegrator1D;
import com.opengamma.util.tuple.DoublesPair;

/**
 *  Class used to compute the price of a CMS cap/floor by swaption replication with SABR Hagan formula.
 */
public class CapFloorCMSReplicationSABRMethod {

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
   */
  private double _integrationInterval;

  private final int _nbIteration = 6;

  /** 
   * Default constructor of the CMS cap/floor replication method. The default integration interval is 1.00 (100%).
   */
  public CapFloorCMSReplicationSABRMethod() {
    _integrationInterval = 1.00;
  }

  /**
   * Constructor of the CMS cap/floor replication method with the integration range. 
   * @param integrationInterval Integration range.
   */
  public CapFloorCMSReplicationSABRMethod(double integrationInterval) {
    _integrationInterval = integrationInterval;
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
   * Compute the price of a CMS cap/floor by replication in SABR framework. 
   * @param cmsCapFloor The CMS cap/floor.
   * @param sabrData The SABR data bundle.
   * @return The price.
   */
  public double presentValue(CapFloorCMS cmsCapFloor, SABRInterestRateDataBundle sabrData) {
    SABRInterestRateParameter sabrParameter = sabrData.getSABRParameter();
    FixedCouponSwap<? extends Payment> underlyingSwap = cmsCapFloor.getUnderlyingSwap();
    double forward = PRC.visit(underlyingSwap, sabrData);
    double discountFactor = sabrData.getCurve(underlyingSwap.getFixedLeg().getNthPayment(0).getFundingCurveName()).getDiscountFactor(cmsCapFloor.getPaymentTime());
    CMSIntegrant integrant = new CMSIntegrant(cmsCapFloor, sabrParameter, forward);
    //    double factor = discountFactor / integrant.h(forward) * integrant.g(forward);
    double strike = cmsCapFloor.geStrike();
    double strikePart = discountFactor * integrant.k(strike) * integrant.g(forward) / integrant.h(forward) * integrant.bs(strike);
    double absoluteTolerance = 1.0 / (discountFactor * Math.abs(cmsCapFloor.getNotional()) * cmsCapFloor.getPaymentYearFraction());
    double relativeTolerance = 1E-10;
    RungeKuttaIntegrator1D integrator = new RungeKuttaIntegrator1D(absoluteTolerance, relativeTolerance, _nbIteration);
    double integralPart;
    try {
      if (cmsCapFloor.isCap()) {
        integralPart = discountFactor * integrator.integrate(integrant, strike, strike + _integrationInterval);
      } else {
        integralPart = discountFactor * integrator.integrate(integrant, 0.0, strike);
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    double priceCMS = (strikePart + integralPart) * cmsCapFloor.getNotional() * cmsCapFloor.getPaymentYearFraction();
    return priceCMS;
  }

  public PresentValueSensitivity presentValueSensitivity(CapFloorCMS cmsCapFloor, SABRInterestRateDataBundle sabrData) {
    SABRInterestRateParameter sabrParameter = sabrData.getSABRParameter();
    FixedCouponSwap<? extends Payment> underlyingSwap = cmsCapFloor.getUnderlyingSwap();
    double forward = PRC.visit(underlyingSwap, sabrData);
    double discountFactor = sabrData.getCurve(underlyingSwap.getFixedLeg().getNthPayment(0).getFundingCurveName()).getDiscountFactor(cmsCapFloor.getPaymentTime());
    double strike = cmsCapFloor.geStrike();
    // Common
    CMSIntegrant integrantPrice = new CMSIntegrant(cmsCapFloor, sabrParameter, forward);
    CMSDeltaIntegrant integrantDelta = new CMSDeltaIntegrant(cmsCapFloor, sabrParameter, forward);
    double factor = discountFactor / integrantDelta.h(forward) * integrantDelta.g(forward);
    double absoluteTolerance = 1.0 / (factor * Math.abs(cmsCapFloor.getNotional()) * cmsCapFloor.getPaymentYearFraction());
    double relativeTolerance = 1E-10;
    RungeKuttaIntegrator1D integrator = new RungeKuttaIntegrator1D(absoluteTolerance, relativeTolerance, _nbIteration);
    // Price
    double[] bs = integrantDelta.bsbsp(strike);
    double[] n = integrantDelta.nnp(forward);
    double strikePartPrice = discountFactor * integrantDelta.k(strike) * n[0] * bs[0];
    double integralPartPrice;
    try {
      if (cmsCapFloor.isCap()) {
        integralPartPrice = discountFactor * integrator.integrate(integrantPrice, strike, strike + _integrationInterval);
      } else {
        integralPartPrice = discountFactor * integrator.integrate(integrantPrice, 0.0, strike);
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    double price = discountFactor * (strikePartPrice + integralPartPrice) * cmsCapFloor.getNotional() * cmsCapFloor.getPaymentYearFraction();
    // Delta
    double strikePart = discountFactor * integrantDelta.k(strike) * (n[1] * bs[0] + n[0] * bs[1]);
    double integralPart;
    try {
      if (cmsCapFloor.isCap()) {
        integralPart = discountFactor * integrator.integrate(integrantDelta, strike, strike + _integrationInterval);
      } else {
        integralPart = discountFactor * integrator.integrate(integrantDelta, 0.0, strike);
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    double deltaS0 = (strikePart + integralPart) * cmsCapFloor.getNotional() * cmsCapFloor.getPaymentYearFraction();
    double deltaPD = price / discountFactor;

    double sensiDF = -cmsCapFloor.getSettlementTime() * discountFactor * deltaPD;
    final List<DoublesPair> list = new ArrayList<DoublesPair>();
    list.add(new DoublesPair(cmsCapFloor.getSettlementTime(), sensiDF));
    final Map<String, List<DoublesPair>> resultMap = new HashMap<String, List<DoublesPair>>();
    resultMap.put(cmsCapFloor.getUnderlyingSwap().getFixedLeg().getNthPayment(0).getFundingCurveName(), list);
    PresentValueSensitivity result = new PresentValueSensitivity(resultMap);
    PresentValueSensitivity forwardDr = new PresentValueSensitivity(PRSC.visit(cmsCapFloor.getUnderlyingSwap(), sabrData));
    result = result.add(forwardDr.multiply(deltaS0));
    return result;
  }

  public PresentValueSABRSensitivity presentValueSABRSensitivity(CapFloorCMS cmsCapFloor, SABRInterestRateDataBundle sabrData) {
    SABRInterestRateParameter sabrParameter = sabrData.getSABRParameter();
    FixedCouponSwap<? extends Payment> underlyingSwap = cmsCapFloor.getUnderlyingSwap();
    double forward = PRC.visit(underlyingSwap, sabrData);
    double discountFactor = sabrData.getCurve(underlyingSwap.getFixedLeg().getNthPayment(0).getFundingCurveName()).getDiscountFactor(cmsCapFloor.getPaymentTime());
    double strike = cmsCapFloor.geStrike();
    // Implementation note: to avoid singularity at 0.
    strike = (strike < 1E-4 ? 1E-4 : strike);
    double maturity = underlyingSwap.getFixedLeg().getNthPayment(underlyingSwap.getFixedLeg().getNumberOfPayments() - 1).getPaymentTime() - cmsCapFloor.getSettlementTime();
    CMSVegaIntegrant integrantVega = new CMSVegaIntegrant(cmsCapFloor, sabrParameter, forward);
    double factor = discountFactor / integrantVega.h(forward) * integrantVega.g(forward);
    double absoluteTolerance = 1.0 / (factor * Math.abs(cmsCapFloor.getNotional()) * cmsCapFloor.getPaymentYearFraction());
    double relativeTolerance = 1E-10;
    RungeKuttaIntegrator1D integrator = new RungeKuttaIntegrator1D(absoluteTolerance, relativeTolerance, _nbIteration);
    double factor2 = factor * integrantVega.k(strike) * integrantVega.bs(strike);
    double[] strikePartPrice = new double[3];
    double[] volatilityAdjoint = sabrData.getSABRParameter().getVolatilityAdjoint(cmsCapFloor.getFixingTime(), maturity, strike, forward);
    strikePartPrice[0] = factor2 * volatilityAdjoint[3];
    strikePartPrice[1] = factor2 * volatilityAdjoint[4];
    strikePartPrice[2] = factor2 * volatilityAdjoint[5];
    double[] integralPart = new double[3];
    double[] totalSensi = new double[3];
    for (int loopparameter = 0; loopparameter < 3; loopparameter++) {
      integrantVega.setParameterIndex(loopparameter);
      try {
        if (cmsCapFloor.isCap()) {
          integralPart[loopparameter] = discountFactor * integrator.integrate(integrantVega, strike, strike + _integrationInterval);
        } else {
          integralPart[loopparameter] = discountFactor * integrator.integrate(integrantVega, 0.0, strike);
        }
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
      totalSensi[loopparameter] = (strikePartPrice[loopparameter] + integralPart[loopparameter]) * cmsCapFloor.getNotional() * cmsCapFloor.getPaymentYearFraction();
    }
    PresentValueSABRSensitivity sensi = new PresentValueSABRSensitivity();
    DoublesPair expiryMaturity = new DoublesPair(cmsCapFloor.getFixingTime(), maturity);
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
    private final SABRInterestRateParameter _sabrParameter;
    private final BlackPriceFunction _blackFunction = new BlackPriceFunction();
    private final boolean _isCall;

    /**
     * 
     */
    public CMSIntegrant(CapFloorCMS cmsCap, SABRInterestRateParameter sabrParameter, double forward) {
      _nbFixedPeriod = cmsCap.getUnderlyingSwap().getFixedLeg().getPayments().length;
      _nbFixedPaymentYear = (int) Math.round(1.0 / cmsCap.getUnderlyingSwap().getFixedLeg().getNthPayment(0).getPaymentYearFraction());
      _tau = 1.0 / _nbFixedPaymentYear;
      _delta = cmsCap.getPaymentTime() - cmsCap.getSettlementTime();
      _eta = -_delta;
      _sabrParameter = sabrParameter;
      _timeToExpiry = cmsCap.getFixingTime();
      // TODO: A better notion of maturity may be required (using period?)
      AnnuityCouponFixed annuityFixed = cmsCap.getUnderlyingSwap().getFixedLeg();
      _maturity = annuityFixed.getNthPayment(annuityFixed.getNumberOfPayments() - 1).getPaymentTime() - cmsCap.getSettlementTime();
      _forward = forward;
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
      double volatility = _sabrParameter.getVolatility(_timeToExpiry, _maturity, strike, _forward);
      BlackFunctionData dataBlack = new BlackFunctionData(_forward, 1.0, volatility);
      Function1D<BlackFunctionData, Double> func = _blackFunction.getPriceFunction(option);
      return func.evaluate(dataBlack);
    }
  }

  /**
   * Inner class to implement the integration used in price replication.
   */
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
    private final SABRInterestRateParameter _sabrParameter;
    private final BlackPriceFunction _blackFunction = new BlackPriceFunction();
    private final boolean _isCall;

    /**
     * 
     */
    public CMSDeltaIntegrant(CapFloorCMS cmsCap, SABRInterestRateParameter sabrParameter, double forward) {
      _nbFixedPeriod = cmsCap.getUnderlyingSwap().getFixedLeg().getPayments().length;
      _nbFixedPaymentYear = (int) Math.round(1.0 / cmsCap.getUnderlyingSwap().getFixedLeg().getNthPayment(0).getPaymentYearFraction());
      _tau = 1.0 / _nbFixedPaymentYear;
      _delta = cmsCap.getPaymentTime() - cmsCap.getSettlementTime();
      _eta = -_delta;
      _sabrParameter = sabrParameter;
      _timeToExpiry = cmsCap.getFixingTime();
      // TODO: A better notion of maturity may be required (using period?)
      AnnuityCouponFixed annuityFixed = cmsCap.getUnderlyingSwap().getFixedLeg();
      _maturity = annuityFixed.getNthPayment(annuityFixed.getNumberOfPayments() - 1).getPaymentTime() - cmsCap.getSettlementTime();
      _forward = forward;
      _isCall = cmsCap.isCap();
      _strike = cmsCap.geStrike();
    }

    @Override
    public Double evaluate(Double x) {
      double[] kD = kpkpp(x);
      // Implementation note: kD[0] contains the first derivative of k; kD[1] the second derivative of k. 
      double[] bs = bsbsp(x);
      double[] n = nnp(_forward);
      return (kD[1] * (x - _strike) + 2.0 * kD[0]) * (n[1] * bs[0] + n[0] * bs[1]);
    }

    /**
     * The approximation of the discount factor as function of the swap rate.
     * @param x The swap rate.
     * @return The discount factor.
     */
    private double h(double x) {
      return Math.pow(1.0 + _tau * x, _eta);
    }

    private double[] hhp(double x) {
      double[] result = new double[2];
      result[0] = Math.pow(1.0 + _tau * x, _eta);
      result[1] = _eta * _tau * result[0] / (1 + x * _tau);
      return result;
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

    private double[] ggp(double x) {
      double[] result = new double[2];
      if (x >= EPS) {
        double periodFactor = 1 + x / _nbFixedPaymentYear;
        double nPeriodDiscount = Math.pow(periodFactor, -_nbFixedPeriod);
        result[0] = 1.0 / x * (1.0 - nPeriodDiscount);
        result[1] = -result[0] / x + _tau * nPeriodDiscount / x * nPeriodDiscount / periodFactor;
      }
      result[0] = _nbFixedPeriod * _tau;
      result[1] = -_nbFixedPeriod * (_nbFixedPeriod + 1.0) * _tau * _tau / 2.0;
      return result;
    }

    private double[] nnp(double x) {
      double[] result = new double[2];
      double[] ggp = ggp(x);
      double[] hhp = hhp(x);
      result[0] = ggp[0] / hhp[0];
      result[1] = ggp[1] / hhp[0] - ggp[0] * hhp[1] / (hhp[0] * hhp[0]);
      return result;
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
      double volatility = _sabrParameter.getVolatility(_timeToExpiry, _maturity, strike, _forward);
      BlackFunctionData dataBlack = new BlackFunctionData(_forward, 1.0, volatility);
      Function1D<BlackFunctionData, Double> func = _blackFunction.getPriceFunction(option);
      return func.evaluate(dataBlack);
    }

    double[] bsbsp(double strike) {
      double[] result = new double[2];
      EuropeanVanillaOption option = new EuropeanVanillaOption(strike, _timeToExpiry, _isCall);
      double volatility = _sabrParameter.getVolatility(_timeToExpiry, _maturity, strike, _forward);
      BlackFunctionData dataBlack = new BlackFunctionData(_forward, 1.0, volatility);
      double[] bsAdjoint = _blackFunction.getPriceAdjoint(option, dataBlack);
      result[0] = bsAdjoint[0];
      result[1] = bsAdjoint[1];
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
    private final SABRInterestRateParameter _sabrParameter;
    private final BlackPriceFunction _blackFunction = new BlackPriceFunction();
    private final boolean _isCall;
    private int _parameterIndex;

    /**
     * 
     */
    public CMSVegaIntegrant(CapFloorCMS cmsCap, SABRInterestRateParameter sabrParameter, double forward) {
      _nbFixedPeriod = cmsCap.getUnderlyingSwap().getFixedLeg().getPayments().length;
      _nbFixedPaymentYear = (int) Math.round(1.0 / cmsCap.getUnderlyingSwap().getFixedLeg().getNthPayment(0).getPaymentYearFraction());
      _tau = 1.0 / _nbFixedPaymentYear;
      _delta = cmsCap.getPaymentTime() - cmsCap.getSettlementTime();
      _eta = -_delta;
      _sabrParameter = sabrParameter;
      _timeToExpiry = cmsCap.getFixingTime();
      // TODO: A better notion of maturity may be required (using period?)
      AnnuityCouponFixed annuityFixed = cmsCap.getUnderlyingSwap().getFixedLeg();
      _maturity = annuityFixed.getNthPayment(annuityFixed.getNumberOfPayments() - 1).getPaymentTime() - cmsCap.getSettlementTime();
      _forward = forward;
      _isCall = cmsCap.isCap();
      // Implementation note: to avoid singularity at 0.
      _strike = (cmsCap.geStrike() < 1E-4 ? 1E-4 : cmsCap.geStrike());
      _factor = g(_forward) / h(_forward);
    }

    @Override
    public Double evaluate(Double x) {
      double[] kD = kpkpp(x);
      // Implementation note: kD[0] contains the first derivative of k; kD[1] the second derivative of k. 
      double[] volatilityAdjoint = _sabrParameter.getVolatilityAdjoint(_timeToExpiry, _maturity, x, _forward);
      return _factor * (kD[1] * (x - _strike) + 2.0 * kD[0]) * bs(x) * volatilityAdjoint[3 + _parameterIndex];
    }

    /**
     * Sets the _parameterIndex field.
     * @param _parameterIndex  the _parameterIndex
     */
    public void setParameterIndex(int parameterIndex) {
      this._parameterIndex = parameterIndex;
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
     * The derivative with respect to the volatility of the Black-Scholes formula with numeraire 1.
     * @param strike The strike.
     * @return The Black-Scholes formula derivative with respect to volatility.
     */
    double bs(double strike) {
      EuropeanVanillaOption option = new EuropeanVanillaOption(strike, _timeToExpiry, _isCall);
      double volatility = _sabrParameter.getVolatility(_timeToExpiry, _maturity, strike, _forward);
      BlackFunctionData dataBlack = new BlackFunctionData(_forward, 1.0, volatility);
      double[] bsAdjoint = _blackFunction.getPriceAdjoint(option, dataBlack);
      return bsAdjoint[2];
    }
  }

}
