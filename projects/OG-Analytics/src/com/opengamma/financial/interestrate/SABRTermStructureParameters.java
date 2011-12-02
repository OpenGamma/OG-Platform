/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate;

import java.util.LinkedHashMap;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.financial.model.volatility.VolatilityModel1D;
import com.opengamma.financial.model.volatility.smile.function.SABRFormulaData;
import com.opengamma.financial.model.volatility.smile.function.SABRHaganVolatilityFunction;
import com.opengamma.financial.model.volatility.smile.function.VolatilityFunctionProvider;
import com.opengamma.math.curve.Curve;
import com.opengamma.math.function.Function1D;

/**
 * 
 */
public class SABRTermStructureParameters implements VolatilityModel1D {

  private static final String ALPHA = "alpha";
  private static final String BETA = "beta";
  private static final String NU = "nu";
  private static final String RHO = "rho";
  private static final VolatilityFunctionProvider<SABRFormulaData> DEFUALT_SABR = new SABRHaganVolatilityFunction();

  private final Curve<Double, Double> _alpha;
  private final Curve<Double, Double> _beta;
  private final Curve<Double, Double> _nu;
  private final Curve<Double, Double> _rho;

  private final VolatilityFunctionProvider<SABRFormulaData> _sabrFunction;

  public SABRTermStructureParameters(final LinkedHashMap<String, Curve<Double, Double>> curveBundle) {
    Validate.notNull(curveBundle, "null curve bundle");
    Curve<Double, Double> alpha = curveBundle.get(ALPHA);
    Curve<Double, Double> beta = curveBundle.get(BETA);
    Curve<Double, Double> nu = curveBundle.get(NU);
    Curve<Double, Double> rho = curveBundle.get(RHO);
    validate(alpha, beta, rho, nu);
    _alpha = alpha;
    _beta = beta;
    _nu = nu;
    _rho = rho;
    _sabrFunction = DEFUALT_SABR;
  }

  public SABRTermStructureParameters(final Curve<Double, Double> alpha, final Curve<Double, Double> beta, final Curve<Double, Double> rho,
      final Curve<Double, Double> nu) {
    this(alpha, beta, rho, nu, DEFUALT_SABR);
  }

  public SABRTermStructureParameters(final Curve<Double, Double> alpha, final Curve<Double, Double> beta, final Curve<Double, Double> rho,
      final Curve<Double, Double> nu, VolatilityFunctionProvider<SABRFormulaData> sabrFunction) {
    validate(alpha, beta, rho, nu);
    Validate.notNull(sabrFunction, "null sabrFunction");
    _alpha = alpha;
    _beta = beta;
    _nu = nu;
    _rho = rho;
    _sabrFunction = sabrFunction;
  }

  private static void validate(final Curve<Double, Double> alpha, final Curve<Double, Double> beta, final Curve<Double, Double> rho,
      final Curve<Double, Double> nu) {
    Validate.notNull(alpha, "null aplha");
    Validate.notNull(beta, "null beta");
    Validate.notNull(nu, "null nu");
    Validate.notNull(rho, "null rho");
  }

  public double getAlpha(final double timeToExpiry) {
    return _alpha.getYValue(timeToExpiry);
  }

  public double getBeta(final double timeToExpiry) {
    return _beta.getYValue(timeToExpiry);
  }

  public double getRho(final double timeToExpiry) {
    return _rho.getYValue(timeToExpiry);
  }

  public double getNu(final double timeToExpiry) {
    return _nu.getYValue(timeToExpiry);
  }

  /**
   * get the Black volatility for a given forward/strike/time-to-expiry
   * @param fwdKT Array of values of forward, strike and time-to-expiry <b>in that order</b>
   * @return The (Black) volatility
   */
  @Override
  public Double getVolatility(double[] fwdKT) {
    Validate.notNull(fwdKT, "null fwdKT");
    Validate.isTrue(fwdKT.length == 3, "length must be 3");
    return getVolatility(fwdKT[0], fwdKT[1], fwdKT[2]);
  }

  /**
   * get the Black volatility for a given forward/strike/time-to-expiry
   * @param fwd The Forward
   * @param strike The Strike
   * @param timeToExpiry The time-to-expiry
   * @return The (Black) volatility
   */
  @Override
  public double getVolatility(final double fwd, final double strike, final double timeToExpiry) {
    final SABRFormulaData data = new SABRFormulaData(getAlpha(timeToExpiry), getBeta(timeToExpiry), getRho(timeToExpiry), getNu(timeToExpiry));
    final EuropeanVanillaOption option = new EuropeanVanillaOption(strike, timeToExpiry, true);
    final Function1D<SABRFormulaData, Double> func = _sabrFunction.getVolatilityFunction(option, fwd);
    double vol = func.evaluate(data);
    //The SABR Hagan formula can produce negative vols
    return Math.max(0, vol);
  }

}
