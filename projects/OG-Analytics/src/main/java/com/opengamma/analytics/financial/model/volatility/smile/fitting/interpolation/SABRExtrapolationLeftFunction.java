/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.smile.fitting.interpolation;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.BlackFunctionData;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.BlackPriceFunction;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.SABRExtrapolationRightFunction;
import com.opengamma.analytics.financial.model.volatility.smile.function.SABRFormulaData;
import com.opengamma.analytics.financial.model.volatility.smile.function.SABRHaganVolatilityFunction;
import com.opengamma.analytics.financial.model.volatility.smile.function.VolatilityFunctionProvider;
import com.opengamma.analytics.math.MathException;
import com.opengamma.analytics.math.differentiation.FiniteDifferenceType;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.rootfinding.BracketRoot;
import com.opengamma.analytics.math.rootfinding.RidderSingleRootFinder;

/**
 * Counterpart of {@link SABRExtrapolationRightFunction}. 
 * Note that several functionalities are absent in this class. 
 */
public class SABRExtrapolationLeftFunction {

  private final double _forward;
  /**
   * The SABR parameter for the pricing below the cut-off strike.
   */
  private final SABRFormulaData _sabrData;
  /**
   * The cut-off strike. The smile is extrapolated above that level.
   */
  private final double _cutOffStrike;
  /**
   * The tail thickness parameter.
   */
  private final double _mu;
  /**
   * An array containing the three fitting parameters.
   */
  private final double[] _parameter;
  /**
   * The Black implied volatility at the cut-off strike.
   */
  private double _volatilityK;
  /**
   * The price and its derivatives of order 1 and 2 at the cut-off strike.
   */
  private final double[] _priceK = new double[3];
  /**
   * The time to option expiry.
   */
  private final double _timeToExpiry;
  /**
   * Volatility provider function. Currently implemented only for SABRHaganVolatilityFunction.
   */
  private final VolatilityFunctionProvider<SABRFormulaData> _sabrFunction;
  /**
   * Black function used.
   */
  private static final BlackPriceFunction BLACK_FUNCTION = new BlackPriceFunction();
  /**
   * Value below which the time-to-expiry is considered to be 0 and the price of the fitting parameters fit a price of 0 (OTM).
   */
  private static final double SMALL_EXPIRY = 1.0E-6;
  private static final double SMALL_PARAMETER = -1.0E4;

  /**
   * Constructor.
   * @param forward The forward (rate or price).
   * @param sabrData The SABR formula data.
   * @param cutOffStrike The cut-off-strike.
   * @param timeToExpiry The time to expiration.
   * @param mu The mu parameter.
   * @param volatilityFunction The SABR volatility function
   */
  public SABRExtrapolationLeftFunction(final double forward, final SABRFormulaData sabrData, final double cutOffStrike, final double timeToExpiry, final double mu,
      final VolatilityFunctionProvider<SABRFormulaData> volatilityFunction) {
    Validate.notNull(sabrData, "SABR data");
    Validate.notNull(volatilityFunction, "volatilityFunction");
    _forward = forward;
    _sabrData = sabrData;
    _cutOffStrike = cutOffStrike;
    _timeToExpiry = timeToExpiry;
    _mu = mu;
    _sabrFunction = volatilityFunction;
    if (timeToExpiry > SMALL_EXPIRY) {
      _parameter = computesFittingParameters();
    } else { // Implementation note: when time to expiry is very small, the price above the cut-off strike and its derivatives should be 0 (or at least very small).
      _parameter = new double[] {SMALL_PARAMETER, 0.0, 0.0 };
    }
  }

  /**
   * Computes the option price with numeraire=1. The price is SABR below the cut-off strike and extrapolated beyond.
   * @param option The option.
   * @return The option price.
   */
  public double price(final EuropeanVanillaOption option) {
    double p = 0.0;
    final double k = option.getStrike();

    if (k >= _cutOffStrike) { // Uses Hagan et al SABR function.
      final Function1D<SABRFormulaData, Double> funcSabr = _sabrFunction.getVolatilityFunction(option, _forward);
      final double volatility = funcSabr.evaluate(_sabrData);
      final BlackFunctionData dataBlack = new BlackFunctionData(_forward, 1.0, volatility);
      final Function1D<BlackFunctionData, Double> funcBlack = BLACK_FUNCTION.getPriceFunction(option);
      p = funcBlack.evaluate(dataBlack);
    } else { // Uses extrapolation for put.
      p = extrapolation(k);
      if (option.isCall()) { // Call by call/put parity
        p = p + (_forward - option.getStrike());
      }
    }

    return p;
  }

  /**
   * Gets the underlying Sabr data.
   * @return the _sabrData
   */
  public SABRFormulaData getSabrData() {
    return _sabrData;
  }

  /**
   * Gets the cut-off strike. The smile is extrapolated above that level.
   * @return the _cutOffStrike
   */
  public double getCutOffStrike() {
    return _cutOffStrike;
  }

  /**
   * Gets the tail thickness parameter.
   * @return The mu parameter.
   */
  public double getMu() {
    return _mu;
  }

  /**
   * Gets the time to expiry.
   * @return The time to expiry.
   */
  public double getTimeToExpiry() {
    return _timeToExpiry;
  }

  /**
   * Gets the three fitting parameters.
   * @return The parameters.
   */
  public double[] getParameter() {
    return _parameter;
  }

  /**
   * Computes the three fitting parameters to ensure a C^2 price curve.
   * @return The parameters.
   */
  private double[] computesFittingParameters() {
    final double[] param = new double[3];
    final EuropeanVanillaOption option = new EuropeanVanillaOption(_cutOffStrike, _timeToExpiry, false);
    final double[] vD = new double[6];
    final double[][] vD2 = new double[2][2];
    _volatilityK = getVolatilityAdjoint2(option, _forward, _sabrData, vD, vD2);
    final BlackFunctionData dataBlack = new BlackFunctionData(_forward, 1.0, _volatilityK);
    final double[] bsD = new double[3];
    final double[][] bsD2 = new double[3][3];
    _priceK[0] = BLACK_FUNCTION.getPriceAdjoint2(option, dataBlack, bsD, bsD2);
    _priceK[1] = bsD[2] + bsD[1] * vD[1];
    _priceK[2] = bsD2[2][2] + bsD2[1][2] * vD[1] + (bsD2[2][1] + bsD2[1][1] * vD[1]) * vD[1] + bsD[1] * vD2[1][1];
    double eps = 1.0E-15;
    if (Math.abs(_priceK[0]) < eps && Math.abs(_priceK[1]) < eps && Math.abs(_priceK[2]) < eps) {
      // Implementation note: If value and its derivatives is too small, then parameters are such that the extrapolated price is "very small".
      return new double[] {-100.0, 0, 0 };
    }
    final CFunction toSolveC = new CFunction(_priceK, _cutOffStrike, _mu);
    final BracketRoot bracketer = new BracketRoot();
    double accuracy = 1.0E-5;
    final RidderSingleRootFinder rootFinder = new RidderSingleRootFinder(accuracy);
    final double[] range = bracketer.getBracketedPoints(toSolveC, -1.0, 1.0);
    param[2] = rootFinder.getRoot(toSolveC, range[0], range[1]);
    param[1] = -2 * param[2] * _cutOffStrike + _priceK[1] / _priceK[0] - _mu / _cutOffStrike;
    param[0] = Math.log(_priceK[0] / Math.pow(_cutOffStrike, _mu)) - param[1] * _cutOffStrike - param[2] * (_cutOffStrike * _cutOffStrike);
    return param;
  }

  /**
   * The extrapolation function.
   * @param strike The strike.
   * @return The extrapolated price.
   */
  private double extrapolation(final double strike) {
    return Math.pow(strike, _mu) * Math.exp(_parameter[0] + _parameter[1] * strike + _parameter[2] * (strike * strike));
  }

  private double getVolatilityAdjoint2(final EuropeanVanillaOption option, final double forward, final SABRFormulaData data, final double[] volatilityD, final double[][] volatilityD2) {
    if (_sabrFunction instanceof SABRHaganVolatilityFunction) {
      return ((SABRHaganVolatilityFunction) _sabrFunction).getVolatilityAdjoint2(option, forward, data, volatilityD, volatilityD2);
    }

    //TODO Implement analytic formula for each volatility function
    double eps = 1.0e-6;
    volatilityD[0] = fdSensitivity(option, forward, data, 3, eps);
    volatilityD[1] = fdSensitivity(option, forward, data, 4, eps);
    volatilityD[2] = fdSensitivity(option, forward, data, 5, eps);
    volatilityD[3] = fdSensitivity(option, forward, data, 6, eps);

    double fwdUp = fdSensitivity(option, forward + eps, data, 1, eps);
    double fwdDw = fdSensitivity(option, forward - eps, data, 1, eps);
    double crUp = fdSensitivity(option, forward + eps, data, 2, eps);
    double crDw = fdSensitivity(option, forward - eps, data, 2, eps);
    double strUp = fdSensitivity(option.withStrike(option.getStrike() + eps), forward, data, 2, eps);
    double strDw = fdSensitivity(option.withStrike(option.getStrike() - eps), forward, data, 2, eps);

    volatilityD2[0][0] = 0.5 * (fwdUp - fwdDw) / eps;
    volatilityD2[1][0] = 0.5 * (crUp - crDw) / eps;
    volatilityD2[0][1] = volatilityD2[1][0];
    volatilityD2[1][1] = 0.5 * (strUp - strDw) / eps;
    return _sabrFunction.getVolatilityFunction(option, forward).evaluate(data);
  }

  private double fdSensitivity(final EuropeanVanillaOption optionData, final double forward, final SABRFormulaData sabrData, final int sense, final double delta) {

    Function1D<SABRFormulaData, Double> funcC = null;
    Function1D<SABRFormulaData, Double> funcB = null;
    Function1D<SABRFormulaData, Double> funcA = null;
    SABRFormulaData dataC = null;
    SABRFormulaData dataB = sabrData;
    SABRFormulaData dataA = null;
    final Function1D<SABRFormulaData, Double> func = _sabrFunction.getVolatilityFunction(optionData, forward);

    FiniteDifferenceType fdType = null;

    switch (sense) {
      case 1:
        if (forward > delta) {
          fdType = FiniteDifferenceType.CENTRAL;
          funcA = _sabrFunction.getVolatilityFunction(optionData, forward - delta);
          funcC = _sabrFunction.getVolatilityFunction(optionData, forward + delta);
        } else {
          fdType = FiniteDifferenceType.FORWARD;
          funcA = func;
          funcB = _sabrFunction.getVolatilityFunction(optionData, forward + delta);
          funcC = _sabrFunction.getVolatilityFunction(optionData, forward + 2 * delta);
        }
        dataC = sabrData;
        dataB = sabrData;
        dataA = sabrData;
        break;
      case 2:
        final double strike = optionData.getStrike();
        if (strike >= delta) {
          fdType = FiniteDifferenceType.CENTRAL;
          funcA = _sabrFunction.getVolatilityFunction(optionData.withStrike(strike - delta), forward);
          funcC = _sabrFunction.getVolatilityFunction(optionData.withStrike(strike + delta), forward);
        } else {
          fdType = FiniteDifferenceType.FORWARD;
          funcA = func;
          funcB = _sabrFunction.getVolatilityFunction(optionData.withStrike(strike + delta), forward);
          funcC = _sabrFunction.getVolatilityFunction(optionData.withStrike(strike + 2 * delta), forward);
        }
        dataC = sabrData;
        dataB = sabrData;
        dataA = sabrData;
        break;
      case 3:
        final double a = sabrData.getAlpha();
        if (a >= delta) {
          fdType = FiniteDifferenceType.CENTRAL;
          dataA = sabrData.withAlpha(a - delta);
          dataC = sabrData.withAlpha(a + delta);
        } else {
          fdType = FiniteDifferenceType.FORWARD;
          dataA = sabrData;
          dataB = sabrData.withAlpha(a + delta);
          dataC = sabrData.withAlpha(a + 2 * delta);
        }
        funcC = func;
        funcB = func;
        funcA = func;
        break;
      case 4:
        final double b = sabrData.getBeta();
        if (b >= delta) {
          fdType = FiniteDifferenceType.CENTRAL;
          dataA = sabrData.withBeta(b - delta);
          dataC = sabrData.withBeta(b + delta);
        } else {
          fdType = FiniteDifferenceType.FORWARD;
          dataA = sabrData;
          dataB = sabrData.withBeta(b + delta);
          dataC = sabrData.withBeta(b + 2 * delta);
        }
        funcC = func;
        funcB = func;
        funcA = func;
        break;
      case 5:
        final double r = sabrData.getRho();
        if ((r + 1) < delta) {
          fdType = FiniteDifferenceType.FORWARD;
          dataA = sabrData;
          dataB = sabrData.withRho(r + delta);
          dataC = sabrData.withRho(r + 2 * delta);
        } else if ((1 - r) < delta) {
          fdType = FiniteDifferenceType.BACKWARD;
          dataA = sabrData.withRho(r - 2 * delta);
          dataB = sabrData.withRho(r - delta);
          dataC = sabrData;
        } else {
          fdType = FiniteDifferenceType.CENTRAL;
          dataC = sabrData.withRho(r + delta);
          dataA = sabrData.withRho(r - delta);
        }
        funcC = func;
        funcB = func;
        funcA = func;
        break;
      case 6:
        final double n = sabrData.getNu();
        if (n >= delta) {
          fdType = FiniteDifferenceType.CENTRAL;
          dataA = sabrData.withNu(n - delta);
          dataC = sabrData.withNu(n + delta);
        } else {
          fdType = FiniteDifferenceType.FORWARD;
          dataA = sabrData;
          dataB = sabrData.withNu(n + delta);
          dataC = sabrData.withNu(n + 2 * delta);
        }
        funcC = func;
        funcB = func;
        funcA = func;
        break;
      default:
        throw new MathException();
    }

    if (fdType != null) {
      switch (fdType) {
        case FORWARD:
          return (-1.5 * funcA.evaluate(dataA) + 2.0 * funcB.evaluate(dataB) - 0.5 * funcC.evaluate(dataC)) / delta;
        case BACKWARD:
          return (0.5 * funcA.evaluate(dataA) - 2.0 * funcB.evaluate(dataB) + 1.5 * funcC.evaluate(dataC)) / delta;
        case CENTRAL:
          return (funcC.evaluate(dataC) - funcA.evaluate(dataA)) / 2.0 / delta;
        default:
          throw new MathException("enum not found");
      }
    }
    throw new MathException();
  }

  /**
   * Inner class to solve the one dimension equation required to obtain c parameters. 
   */
  private static final class CFunction extends Function1D<Double, Double> {
    /**
     * Array with the option price and its derivatives at the cut-off strike;
     */
    private final double[] _cPriceK;
    /**
     * The cut-off strike (in the root finding function). The smile is extrapolated above that level.
     */
    private final double _cCutOffStrike;
    /**
     * The tail thickness parameter (in the root finding function).
     */
    private final double _cMu;

    /**
     * Constructor of the two dimension function. 
     * @param price The option price and its derivatives.
     * @param cutOffStrike The cut-off strike.
     * @param mu The tail thickness parameter.
     */
    public CFunction(final double[] price, final double cutOffStrike, final double mu) {
      _cPriceK = price;
      _cCutOffStrike = cutOffStrike;
      _cMu = mu;
    }

    @Override
    public Double evaluate(Double c) {
      double b = -2 * c * _cCutOffStrike + _cPriceK[1] / _cPriceK[0] - _cMu / _cCutOffStrike;
      double k2 = _cCutOffStrike * _cCutOffStrike;
      double res = -_cPriceK[2] / _cPriceK[0] * k2 + _cMu * (_cMu - 1) + 2 * b * _cMu * _cCutOffStrike + (2 * c * (2 * _cMu + 1) + b * b) * k2 + 4 * b * c * (k2 * _cCutOffStrike) + 4 * c * c
          * (k2 * k2);
      return res;
    }
  }
}
