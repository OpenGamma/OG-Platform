/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.definition;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.financial.model.volatility.VolatilityModel;
import com.opengamma.financial.model.volatility.smile.function.SABRFormulaData;
import com.opengamma.financial.model.volatility.smile.function.SABRHaganVolatilityFunction;
import com.opengamma.financial.model.volatility.smile.function.VolatilityFunctionProvider;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.surface.InterpolatedDoublesSurface;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Class describing the SABR parameter surfaces used in interest rate modeling.
 */
public class SABRInterestRateParameters implements VolatilityModel<double[]> {

  /**
   * The alpha (volatility level) surface. The first dimension is the expiration and the second the tenor.
   */
  private final InterpolatedDoublesSurface _alphaSurface;
  /**
   * The beta (elasticity) surface. The first dimension is the expiration and the second the tenor.
   */
  private final InterpolatedDoublesSurface _betaSurface;
  /**
   * The rho (correlation) surface. The first dimension is the expiration and the second the tenor.
   */
  private final InterpolatedDoublesSurface _rhoSurface;
  /**
   * The nu (volatility of volatility) surface. The first dimension is the expiration and the second the tenor.
   */
  private final InterpolatedDoublesSurface _nuSurface;
  /**
   * The function containing the SABR volatility formula. Default is HaganVolatilityFunction.
   */
  private final VolatilityFunctionProvider<SABRFormulaData> _sabrFunction;
  /**
   * The standard day count for which the parameter surfaces are valid.
   */
  private final DayCount _dayCount;

  // TODO: add SwapGenerator?

  /**
   * Constructor from the parameter surfaces. The default SABR volatility formula is HaganVolatilityFunction.
   * @param alpha The alpha parameters.  The first dimension is the expiration and the second the tenor.
   * @param beta The beta parameters.
   * @param rho The rho parameters.
   * @param nu The nu parameters.
   * @param dayCount The standard day count for which the parameter surfaces are valid.
   */
  public SABRInterestRateParameters(final InterpolatedDoublesSurface alpha, final InterpolatedDoublesSurface beta, final InterpolatedDoublesSurface rho, final InterpolatedDoublesSurface nu,
      final DayCount dayCount) {
    this(alpha, beta, rho, nu, dayCount, new SABRHaganVolatilityFunction());
  }

  /**
   * Constructor from the parameter surfaces. The default SABR volatility formula is HaganVolatilityFunction.
   * @param alpha The alpha parameters. The first dimension is the expiration and the second the tenor.
   * @param beta The beta parameters.
   * @param rho The rho parameters.
   * @param nu The nu parameters.
   * @param dayCount The standard day count for which the parameter surfaces are valid.
   * @param sabrFormula The SABR formula provider.
   */
  public SABRInterestRateParameters(final InterpolatedDoublesSurface alpha, final InterpolatedDoublesSurface beta, final InterpolatedDoublesSurface rho, final InterpolatedDoublesSurface nu,
      final DayCount dayCount, final VolatilityFunctionProvider<SABRFormulaData> sabrFormula) {
    Validate.notNull(alpha, "alpha surface");
    Validate.notNull(beta, "beta surface");
    Validate.notNull(rho, "rho surface");
    Validate.notNull(nu, "nu surface");
    Validate.notNull(dayCount, "dayCount");
    Validate.notNull(sabrFormula, "SABR formula");
    _alphaSurface = alpha;
    _betaSurface = beta;
    _rhoSurface = rho;
    _nuSurface = nu;
    _dayCount = dayCount;
    _sabrFunction = sabrFormula;
  }

  /**
   * Return the alpha parameter for a pair of time to expiry and instrument maturity.
   * @param expiryMaturity The expiry/maturity pair.
   * @return The alpha parameter.
   */
  public double getAlpha(final DoublesPair expiryMaturity) {
    return _alphaSurface.getZValue(expiryMaturity);
  }

  /**
   * Return the beta parameter for a pair of time to expiry and instrument maturity.
   * @param expiryMaturity The expiry/maturity pair.
   * @return The beta parameter.
   */
  public double getBeta(final DoublesPair expiryMaturity) {
    return _betaSurface.getZValue(expiryMaturity);
  }

  /**
   * Return the rho parameter for a pair of time to expiry and instrument maturity.
   * @param expiryMaturity The expiry/maturity pair.
   * @return The rho parameter.
   */
  public double getRho(final DoublesPair expiryMaturity) {
    return _rhoSurface.getZValue(expiryMaturity);
  }

  /**
   * Return the nu parameter for a pair of time to expiry and instrument maturity.
   * @param expiryMaturity The expiry/maturity pair.
   * @return The nu parameter.
   */
  public double getNu(final DoublesPair expiryMaturity) {
    return _nuSurface.getZValue(expiryMaturity);
  }

  /**
   * Gets the standard day count for which the parameter surfaces are valid.
   * @return The day count.
   */
  public DayCount getDayCount() {
    return _dayCount;
  }

  /**
   * Gets the SABR function.
   * @return The SABR function
   */
  public VolatilityFunctionProvider<SABRFormulaData> getSabrFunction() {
    return _sabrFunction;
  }

  /**
   * Gets the alpha surface.
   * @return The alpha surface.
   */
  public InterpolatedDoublesSurface getAlphaSurface() {
    return _alphaSurface;
  }

  /**
   * Gets the beta surface.
   * @return The beta surface.
   */
  public InterpolatedDoublesSurface getBetaSurface() {
    return _betaSurface;
  }

  /**
   * Gets the rho surface.
   * @return The rho surface.
   */
  public InterpolatedDoublesSurface getRhoSurface() {
    return _rhoSurface;
  }

  /**
   * Gets the nu surface.
   * @return The nu surface.
   */
  public InterpolatedDoublesSurface getNuSurface() {
    return _nuSurface;
  }

  /**
   * Return the volatility for a expiry, a maturity, a strike and a forward rate.
   * @param expiryTime Time to expiry.
   * @param maturity Tenor.
   * @param strike The strike.
   * @param forward The forward.
   * @return The volatility.
   */
  public double getVolatility(final double expiryTime, final double maturity, final double strike, final double forward) {
    final DoublesPair expiryMaturity = new DoublesPair(expiryTime, maturity);
    final SABRFormulaData data = new SABRFormulaData(getAlpha(expiryMaturity), getBeta(expiryMaturity), getRho(expiryMaturity), getNu(expiryMaturity));
    final EuropeanVanillaOption option = new EuropeanVanillaOption(strike, expiryTime, true);
    final Function1D<SABRFormulaData, Double> funcSabrLongPayer = _sabrFunction.getVolatilityFunction(option, forward);
    return funcSabrLongPayer.evaluate(data);
  }

  @Override
  /**
   * Return the volatility for a expiry/maturity/strike/forward array.
   * @param data An array of four doubles with [0] the expiry, [1] the maturity, [2] the strike, [3] the forward.
   * @return The volatility.
   */
  public Double getVolatility(final double[] data) {
    Validate.notNull(data, "data");
    Validate.isTrue(data.length == 4, "data should have four components (expiry time, maturity, strike and forward");
    return getVolatility(data[0], data[1], data[2], data[3]);
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
  public double[] getVolatilityAdjoint(final double expiryTime, final double maturity, final double strike, final double forward) {
    Validate.isTrue(_sabrFunction instanceof SABRHaganVolatilityFunction, "Adjoint volatility available only for Hagan formula");
    final SABRHaganVolatilityFunction sabrHaganFunction = (SABRHaganVolatilityFunction) _sabrFunction;
    final DoublesPair expiryMaturity = new DoublesPair(expiryTime, maturity);
    final SABRFormulaData data = new SABRFormulaData(getAlpha(expiryMaturity), getBeta(expiryMaturity), getRho(expiryMaturity), getNu(expiryMaturity));
    final EuropeanVanillaOption option = new EuropeanVanillaOption(strike, expiryTime, true);
    return sabrHaganFunction.getVolatilityAdjointOld(option, forward, data);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _alphaSurface.hashCode();
    result = prime * result + _betaSurface.hashCode();
    result = prime * result + _dayCount.hashCode();
    result = prime * result + _nuSurface.hashCode();
    result = prime * result + _rhoSurface.hashCode();
    result = prime * result + _sabrFunction.hashCode();
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final SABRInterestRateParameters other = (SABRInterestRateParameters) obj;
    if (!ObjectUtils.equals(_alphaSurface, other._alphaSurface)) {
      return false;
    }
    if (!ObjectUtils.equals(_betaSurface, other._betaSurface)) {
      return false;
    }
    if (!ObjectUtils.equals(_rhoSurface, other._rhoSurface)) {
      return false;
    }
    if (!ObjectUtils.equals(_nuSurface, other._nuSurface)) {
      return false;
    }
    if (!ObjectUtils.equals(_dayCount, other._dayCount)) {
      return false;
    }
    return ObjectUtils.equals(_sabrFunction, other._sabrFunction);
  }

}
