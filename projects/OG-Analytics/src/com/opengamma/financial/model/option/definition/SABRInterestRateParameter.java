/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.definition;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.financial.model.volatility.smile.function.SABRFormulaData;
import com.opengamma.financial.model.volatility.smile.function.SABRHaganVolatilityFunction;
import com.opengamma.financial.model.volatility.smile.function.VolatilityFunctionProvider;
import com.opengamma.financial.model.volatility.surface.VolatilitySurface;
import com.opengamma.math.function.Function1D;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Class describing the SABR parameter surfaces used in interest rate modeling.
 */
public class SABRInterestRateParameter {

  /**
   * The alpha (volatility level) surface.
   */
  private final VolatilitySurface _alphaSurface;
  //TODO: Should it be a surface of <double, Period>?
  /**
   * The beta (elasticity) surface.
   */
  private final VolatilitySurface _betaSurface;
  /**
   * The rho (correlation) surface.
   */
  private final VolatilitySurface _rhoSurface;
  /**
   * The nu (volatility of volatility) surface.
   */
  private final VolatilitySurface _nuSurface;
  /**
   * The function containing the SABR volatility formula. Default is HaganVolatilityFunction.
   */
  private final VolatilityFunctionProvider<SABRFormulaData> _sabrFunction;

  /**
   * Constructor from the parameter surfaces. The default SABR volatility formula is HaganVolatilityFunction.
   * @param alpha The alpha parameters.
   * @param beta The beta parameters.
   * @param rho The rho parameters.
   * @param nu The nu parameters.
   */
  public SABRInterestRateParameter(final VolatilitySurface alpha, final VolatilitySurface beta, final VolatilitySurface rho, final VolatilitySurface nu) {
    Validate.notNull(alpha, "alpha surface");
    Validate.notNull(beta, "beta surface");
    Validate.notNull(rho, "rho surface");
    Validate.notNull(nu, "nu surface");
    _alphaSurface = alpha;
    _betaSurface = beta;
    _rhoSurface = rho;
    _nuSurface = nu;
    _sabrFunction = new SABRHaganVolatilityFunction();
  }

  /**
   * Constructor from the parameter surfaces. The default SABR volatility formula is HaganVolatilityFunction.
   * @param alpha The alpha parameters.
   * @param beta The beta parameters.
   * @param rho The rho parameters.
   * @param nu The nu parameters.
   * @param sabrFormula The SABR formula provider.
   */
  public SABRInterestRateParameter(final VolatilitySurface alpha, final VolatilitySurface beta, final VolatilitySurface rho, final VolatilitySurface nu,
      VolatilityFunctionProvider<SABRFormulaData> sabrFormula) {
    Validate.notNull(alpha, "alpha surface");
    Validate.notNull(beta, "beta surface");
    Validate.notNull(rho, "rho surface");
    Validate.notNull(nu, "nu surface");
    Validate.notNull(sabrFormula, "SABR formula");
    _alphaSurface = alpha;
    _betaSurface = beta;
    _rhoSurface = rho;
    _nuSurface = nu;
    _sabrFunction = sabrFormula;
  }

  /**
   * Return the alpha parameter for a pair of time to expiry and instrument maturity.
   * @param expiryMaturity The expiry/maturity pair.
   * @return The alpha parameter.
   */
  public double getAlpha(DoublesPair expiryMaturity) {
    return _alphaSurface.getVolatility(expiryMaturity);
  }

  /**
   * Return the beta parameter for a pair of time to expiry and instrument maturity.
   * @param expiryMaturity The expiry/maturity pair.
   * @return The beta parameter.
   */
  public double getBeta(DoublesPair expiryMaturity) {
    return _betaSurface.getVolatility(expiryMaturity);
  }

  /**
   * Return the rho parameter for a pair of time to expiry and instrument maturity.
   * @param expiryMaturity The expiry/maturity pair.
   * @return The rho parameter.
   */
  public double getRho(DoublesPair expiryMaturity) {
    return _rhoSurface.getVolatility(expiryMaturity);
  }

  /**
   * Return the nu parameter for a pair of time to expiry and instrument maturity.
   * @param expiryMaturity The expiry/maturity pair.
   * @return The nu parameter.
   */
  public double getNu(DoublesPair expiryMaturity) {
    return _nuSurface.getVolatility(expiryMaturity);
  }

  /**
   * Gets the SABR function.
   * @return The SABR function
   */
  public VolatilityFunctionProvider<SABRFormulaData> getSabrFunction() {
    return _sabrFunction;
  }

  /**
   * Return the volatility for a expiry/maturity pair, a strike and a forward rate.
   * @param expiryTime Time to expiry.
   * @param maturity Tenor.
   * @param strike The strike.
   * @param forward The forward.
   * @return The volatility.
   */
  public double getVolatility(double expiryTime, double maturity, double strike, double forward) {
    DoublesPair expiryMaturity = new DoublesPair(expiryTime, maturity);
    SABRFormulaData data = new SABRFormulaData(forward, getAlpha(expiryMaturity), getBeta(expiryMaturity), getNu(expiryMaturity), getRho(expiryMaturity));
    EuropeanVanillaOption option = new EuropeanVanillaOption(strike, expiryTime, true);
    Function1D<SABRFormulaData, Double> funcSabrLongPayer = _sabrFunction.getVolatilityFunction(option);
    return funcSabrLongPayer.evaluate(data);
  }

  /**
   * Return the Black implied volatility in the SABR model and its derivatives when the SABR function is Hagan function.
   * @param expiryTime Time to expiry.
   * @param maturity Tenor.
   * @param strike The strike.
   * @param forward The forward.
   * @return The volatility and its derivative. An array with [0] the volatility, [1] Derivative w.r.t the forward, [2] the derivative w.r.t the strike, 
   * [3] the derivative w.r.t. to alpha, [4] the derivative w.r.t. to rho, [5] the derivative w.r.t. to nu.
   */
  public double[] getVolatilityAdjoint(double expiryTime, double maturity, double strike, double forward) {
    Validate.isTrue(_sabrFunction instanceof SABRHaganVolatilityFunction, "Adjoint volatility available only for Hagan formula");
    SABRHaganVolatilityFunction sabrHaganFunction = (SABRHaganVolatilityFunction) _sabrFunction;
    DoublesPair expiryMaturity = new DoublesPair(expiryTime, maturity);
    SABRFormulaData data = new SABRFormulaData(forward, getAlpha(expiryMaturity), getBeta(expiryMaturity), getNu(expiryMaturity), getRho(expiryMaturity));
    EuropeanVanillaOption option = new EuropeanVanillaOption(strike, expiryTime, true);
    return sabrHaganFunction.getVolatilityAdjoint(option, data);
  }

}
