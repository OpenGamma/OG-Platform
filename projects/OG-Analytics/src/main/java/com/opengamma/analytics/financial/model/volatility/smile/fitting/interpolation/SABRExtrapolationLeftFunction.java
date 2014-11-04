/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.smile.fitting.interpolation;

import java.util.Arrays;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.BlackFunctionData;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.BlackPriceFunction;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.SABRExtrapolationRightFunction;
import com.opengamma.analytics.financial.model.volatility.BlackFormulaRepository;
import com.opengamma.analytics.financial.model.volatility.smile.function.SABRFormulaData;
import com.opengamma.analytics.financial.model.volatility.smile.function.VolatilityFunctionProvider;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.rootfinding.BracketRoot;
import com.opengamma.analytics.math.rootfinding.RidderSingleRootFinder;
import com.opengamma.util.ArgumentChecker;

/**
 * Counterpart of {@link SABRExtrapolationRightFunction}. Note that several functionalities are absent in this class.
 */
public class SABRExtrapolationLeftFunction extends SABRExtrapolationLeftRightFunction {

  private final double _forward;
  private final SABRFormulaData _sabrData;
  private final double _cutOffStrike;
  private final double _mu;
  private final double[] _parameter;
  private double _volatilityK;
  private final double[] _priceK = new double[3];
  private final double _timeToExpiry;

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
    super(volatilityFunction);
    Validate.notNull(sabrData, "SABR data");
    Validate.notNull(volatilityFunction, "volatilityFunction");
    _forward = forward;
    _sabrData = sabrData;
    _cutOffStrike = cutOffStrike;
    _timeToExpiry = timeToExpiry;
    _mu = mu;
    if (timeToExpiry > SMALL_EXPIRY) {
      _parameter = computesFittingParameters();
    } else {
      _parameter = new double[] {SMALL_PARAMETER, 0.0, 0.0 };
    }
  }

  /**
   * Computes the option price with numeraire=1. The price is SABR below the cut-off strike and extrapolated beyond.
   * @param option The option.
   * @return The option price.
   */
  public double price(final EuropeanVanillaOption option) {
    ArgumentChecker.notNull(option, "option");

    double p = 0.0;
    double k = option.getStrike();

    if (k >= _cutOffStrike) { // Uses Hagan SABR function.
      Function1D<SABRFormulaData, Double> funcSabr = getVolatilityFunction().getVolatilityFunction(option, _forward);
      double volatility = funcSabr.evaluate(_sabrData);
      if (volatility < 0.0) {
        volatility = 0.0;
      }
      BlackFunctionData dataBlack = new BlackFunctionData(_forward, 1.0, volatility);
      Function1D<BlackFunctionData, Double> funcBlack = BLACK_FUNCTION.getPriceFunction(option);
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
    final double[] bsD = new double[3];
    final double[][] bsD2 = new double[3][3];
    if (_volatilityK < 0.0) { // all the sensitivities are set to be zero
      _volatilityK = 0.0;
    } else {
      final BlackFunctionData dataBlack = new BlackFunctionData(_forward, 1.0, _volatilityK);
      _priceK[0] = BLACK_FUNCTION.getPriceAdjoint2(option, dataBlack, bsD, bsD2);
    }
    double volga = BlackFormulaRepository.volga(_forward, _cutOffStrike, _timeToExpiry, _volatilityK);
    _priceK[1] = bsD[2] + bsD[1] * vD[1];
    _priceK[2] = bsD2[2][2] + bsD2[1][2] * vD[1] + (bsD2[2][1] + /* bsD2[1][1] * */volga * vD[1]) * vD[1] + bsD[1] * vD2[1][1];
    double eps = 1.0E-15;
    if (Math.abs(_priceK[0]) < eps && Math.abs(_priceK[1]) < eps && Math.abs(_priceK[2]) < eps) {
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
      double res = -_cPriceK[2] / _cPriceK[0] * k2 + _cMu * (_cMu - 1) + 2 * b * _cMu * _cCutOffStrike + (2 * c * (2 * _cMu + 1) + b * b) * k2 + 4 * b * c * (k2 * _cCutOffStrike) + 4 * c * c *
          (k2 * k2);
      return res;
    }
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    long temp;
    temp = Double.doubleToLongBits(_cutOffStrike);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_forward);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_mu);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result + Arrays.hashCode(_parameter);
    result = prime * result + Arrays.hashCode(_priceK);
    result = prime * result + ((_sabrData == null) ? 0 : _sabrData.hashCode());
    temp = Double.doubleToLongBits(_timeToExpiry);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_volatilityK);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof SABRExtrapolationLeftFunction)) {
      return false;
    }
    SABRExtrapolationLeftFunction other = (SABRExtrapolationLeftFunction) obj;
    if (Double.doubleToLongBits(_cutOffStrike) != Double.doubleToLongBits(other._cutOffStrike)) {
      return false;
    }
    if (Double.doubleToLongBits(_forward) != Double.doubleToLongBits(other._forward)) {
      return false;
    }
    if (Double.doubleToLongBits(_mu) != Double.doubleToLongBits(other._mu)) {
      return false;
    }
    if (Double.doubleToLongBits(_timeToExpiry) != Double.doubleToLongBits(other._timeToExpiry)) {
      return false;
    }
    if (_sabrData == null) {
      if (other._sabrData != null) {
        return false;
      }
    } else if (!_sabrData.equals(other._sabrData)) {
      return false;
    }
    if (Double.doubleToLongBits(_volatilityK) != Double.doubleToLongBits(other._volatilityK)) {
      return false;
    }
    if (!Arrays.equals(_priceK, other._priceK)) {
      return false;
    }
    if (!Arrays.equals(_parameter, other._parameter)) {
      return false;
    }
    return true;
  }

}
