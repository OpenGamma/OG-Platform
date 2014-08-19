/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.pricing.analytic.formula;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.model.volatility.smile.function.SABRFormulaData;
import com.opengamma.analytics.financial.model.volatility.smile.function.SABRHaganVolatilityFunction;
import com.opengamma.analytics.financial.model.volatility.smile.function.VolatilityFunctionProvider;
import com.opengamma.analytics.math.MathException;
import com.opengamma.analytics.math.differentiation.FiniteDifferenceType;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.matrix.ColtMatrixAlgebra;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.analytics.math.matrix.OGMatrixAlgebra;
import com.opengamma.analytics.math.rootfinding.BracketRoot;
import com.opengamma.analytics.math.rootfinding.RidderSingleRootFinder;

/**
 * Pricing function in the SABR model with Hagan et al. volatility function and controlled extrapolation for large strikes by extrapolation on call prices.
 * The form of the extrapolation as a function of the strike is
 * \begin{equation*}
 * f(K) = K^{-\mu} \exp\left( a + \frac{b}{K} + \frac{c}{K^2} \right).
 * \end{equation*}
 * <P>Reference: Benaim, S., Dodgson, M., and Kainth, D. (2008). An arbitrage-free method for smile extrapolation. Technical report, Royal Bank of Scotland.
 * <P>OpenGamma implementation note: Smile extrapolation, version 1.2, May 2011.
 */
public class SABRExtrapolationRightFunction {

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
   * An array containing the derivative of the three fitting parameters with respect to the forward.
   * Those parameters are computed only when and if required.
   */
  private double[] _parameterDerivativeForward = new double[3];
  /**
   * Flag indicating if the parameter derivatives to forward have been computed.
   */
  private boolean _parameterDerivativeForwardComputed;
  /**
   * An array containing the derivative of the three fitting parameters with respect to the alpha parameter.
   * Those parameters are computed only when and if required.
   */
  private double[][] _parameterDerivativeSABR = new double[4][3];
  /**
   * Flag indicating if the parameter derivatives to SABR parameters have been computed.
   */
  private boolean _parameterDerivativeSABRComputed;
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
   */
  public SABRExtrapolationRightFunction(final double forward, final SABRFormulaData sabrData, final double cutOffStrike, final double timeToExpiry, final double mu) {
    Validate.notNull(sabrData, "SABR data");
    _forward = forward;
    _sabrData = sabrData;
    _cutOffStrike = cutOffStrike;
    _timeToExpiry = timeToExpiry;
    _mu = mu;
    _sabrFunction = new SABRHaganVolatilityFunction();
    if (timeToExpiry > SMALL_EXPIRY) {
      _parameter = computesFittingParameters();
    } else { // Implementation note: when time to expiry is very small, the price above the cut-off strike and its derivatives should be 0 (or at least very small).
      _parameter = new double[] {SMALL_PARAMETER, 0.0, 0.0 };
      _parameterDerivativeForward = new double[3];
      _parameterDerivativeForwardComputed = true;
      _parameterDerivativeSABR = new double[4][3];
      _parameterDerivativeSABRComputed = true;
    }
  }

  /**
   * Constructor.
   * @param forward The forward (rate or price).
   * @param sabrData The SABR formula data.
   * @param cutOffStrike The cut-off-strike.
   * @param timeToExpiry The time to expiration.
   * @param mu The mu parameter.
   * @param volatilityFunction The SABR volatility function
   */
  public SABRExtrapolationRightFunction(final double forward, final SABRFormulaData sabrData, final double cutOffStrike, final double timeToExpiry, final double mu,
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
      _parameterDerivativeForward = new double[3];
      _parameterDerivativeForwardComputed = true;
      _parameterDerivativeSABR = new double[4][3];
      _parameterDerivativeSABRComputed = true;
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

    if (k <= _cutOffStrike) { // Uses Hagan et al SABR function.
      final Function1D<SABRFormulaData, Double> funcSabr = _sabrFunction.getVolatilityFunction(option, _forward);
      final double volatility = funcSabr.evaluate(_sabrData);
      final BlackFunctionData dataBlack = new BlackFunctionData(_forward, 1.0, volatility);
      final Function1D<BlackFunctionData, Double> funcBlack = BLACK_FUNCTION.getPriceFunction(option);
      p = funcBlack.evaluate(dataBlack);
    } else { // Uses extrapolation for call.
      p = extrapolation(k);
      if (!option.isCall()) { // Put by call/put parity
        p = p - (_forward - option.getStrike());
      }
    }

    return p;
  }

  /**
   * Computes the option price derivative with respect to the strike. The price is SABR below the cut-off strike and extrapolated beyond.
   * @param option The option.
   * @return The option derivative.
   */
  public double priceDerivativeStrike(final EuropeanVanillaOption option) {
    double pDK = 0.0;
    final double k = option.getStrike();
    if (k <= _cutOffStrike) { // Uses Hagan et al SABR function.
      final BlackPriceFunction blackFunction = new BlackPriceFunction();
      final double[] volatilityAdjoint = getVolatilityAdjoint(option, _forward, _sabrData);
      final BlackFunctionData dataBlack = new BlackFunctionData(_forward, 1.0, volatilityAdjoint[0]);
      final double[] bsAdjoint = blackFunction.getPriceAdjoint(option, dataBlack);
      pDK = bsAdjoint[3] + bsAdjoint[2] * volatilityAdjoint[2];
    } else { // Uses extrapolation for call.
      pDK = extrapolationDerivative(k);
      if (!option.isCall()) { // Put by call/put parity
        pDK += 1.0;
      }
    }
    return pDK;
  }

  /**
   * Computes the option price derivative with respect to the forward. The price is SABR below the cut-off strike and extrapolated beyond.
   * @param option The option.
   * @return The option derivative.
   */
  public double priceDerivativeForward(final EuropeanVanillaOption option) {
    double[] pA;
    double priceDerivative;
    final double k = option.getStrike();
    if (k <= _cutOffStrike) { // Uses Hagan et al SABR function.
      final double[] volatilityA = getVolatilityAdjoint(option, _forward, _sabrData);
      final BlackFunctionData dataBlack = new BlackFunctionData(_forward, 1.0, volatilityA[0]);
      pA = BLACK_FUNCTION.getPriceAdjoint(option, dataBlack);
      priceDerivative = pA[1] + pA[2] * volatilityA[1];
    } else { // Uses extrapolation for call.
      if (!_parameterDerivativeForwardComputed) {
        _parameterDerivativeForward = computesParametersDerivativeForward();
        _parameterDerivativeForwardComputed = true;
      }
      final double f = extrapolation(k);
      final double fDa = f;
      final double fDb = f / k;
      final double fDc = fDb / k;
      priceDerivative = fDa * _parameterDerivativeForward[0] + fDb * _parameterDerivativeForward[1] + fDc * _parameterDerivativeForward[2];
      if (!option.isCall()) { // Put by call/put parity
        priceDerivative -= 1;
      }
      //TODO: check put
    }
    return priceDerivative;
  }

  /**
   * Computes the option price derivative with respect to the SABR parameters. The price is SABR below the cut-off strike and extrapolated beyond.
   * @param option The option.
   * @param priceDerivativeSABR An array of three doubles in which the derivative with respect to SABR parameters will be stored. 
   * The derivatives are w.r.t. [0] alpha, [1] beta, [2] rho, and [3] nu.
   * @return The option derivative.
   */
  public double priceAdjointSABR(final EuropeanVanillaOption option, final double[] priceDerivativeSABR) {
    double[] pA;
    double price;
    final double k = option.getStrike();
    if (k <= _cutOffStrike) { // Uses Hagan et al SABR function.
      final double[] volatilityA = getVolatilityAdjoint(option, _forward, _sabrData);
      final BlackFunctionData dataBlack = new BlackFunctionData(_forward, 1.0, volatilityA[0]);
      pA = BLACK_FUNCTION.getPriceAdjoint(option, dataBlack);
      price = pA[0];
      for (int loopparam = 0; loopparam < 4; loopparam++) {
        priceDerivativeSABR[loopparam] = pA[2] * volatilityA[loopparam + 3];
      }
    } else { // Uses extrapolation for call.
      if (!_parameterDerivativeSABRComputed) {
        _parameterDerivativeSABR = computesParametersDerivativeSABR();
        _parameterDerivativeSABRComputed = true;
      }
      final double f = extrapolation(k);
      final double fDa = f;
      final double fDb = f / k;
      final double fDc = fDb / k;
      price = f;
      for (int loopparam = 0; loopparam < 4; loopparam++) {
        priceDerivativeSABR[loopparam] = fDa * _parameterDerivativeSABR[loopparam][0] + fDb * _parameterDerivativeSABR[loopparam][1] + fDc * _parameterDerivativeSABR[loopparam][2];
      }
    }
    //TODO: check put
    return price;
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
   * Gets the three fitting parameters derivatives with respect to the forward.
   * @return The parameters derivative.
   */
  public double[] getParameterDerivativeForward() {
    if (!_parameterDerivativeForwardComputed) {
      _parameterDerivativeForward = computesParametersDerivativeForward();
      _parameterDerivativeForwardComputed = true;
    }
    return _parameterDerivativeForward;
  }

  /**
   * Gets the three fitting parameters derivatives with respect to the SABR parameters.
   * @return The parameters derivative.
   */
  public double[][] getParameterDerivativeSABR() {
    if (!_parameterDerivativeSABRComputed) {
      _parameterDerivativeSABR = computesParametersDerivativeSABR();
      _parameterDerivativeSABRComputed = true;
    }
    return _parameterDerivativeSABR;
  }

  /**
   * Computes the three fitting parameters to ensure a C^2 price curve.
   * @return The parameters.
   */
  private double[] computesFittingParameters() {
    final double[] param = new double[3]; // Implementation note: called a,b,c in the note.
    final EuropeanVanillaOption option = new EuropeanVanillaOption(_cutOffStrike, _timeToExpiry, true);
    // Computes derivatives at cut-off.
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
    param[1] = -2 * param[2] / _cutOffStrike - (_priceK[1] / _priceK[0] * _cutOffStrike + _mu) * _cutOffStrike;
    param[0] = Math.log(_priceK[0] / Math.pow(_cutOffStrike, -_mu)) - param[1] / _cutOffStrike - param[2] / (_cutOffStrike * _cutOffStrike);
    return param;
  }

  /**
   * Computes the derivative of the three fitting parameters with respect to the forward. 
   * The computation requires some third order derivatives; they are computed by finite difference on the second order derivatives.
   * Used to compute the derivative of the price with respect to the forward.
   * @return The derivatives.
   */
  private double[] computesParametersDerivativeForward() {
    double eps = 1.0E-15;
    if (Math.abs(_priceK[0]) < eps && Math.abs(_priceK[1]) < eps && Math.abs(_priceK[2]) < eps) {
      // Implementation note: If value and its derivatives is too small, then parameters are such that the extrapolated price is "very small".
      return new double[] {0.0, 0.0, 0.0 };
    }
    // Derivative of price with respect to forward.
    final double[] pDF = new double[3];
    final double shift = 0.00001;
    final EuropeanVanillaOption option = new EuropeanVanillaOption(_cutOffStrike, _timeToExpiry, true);
    final double[] vD = new double[6];
    final double[][] vD2 = new double[2][2];
    getVolatilityAdjoint2(option, _forward, _sabrData, vD, vD2);
    final BlackFunctionData dataBlack = new BlackFunctionData(_forward, 1.0, _volatilityK);
    final double[] bsD = new double[3];
    final double[][] bsD2 = new double[3][3];
    BLACK_FUNCTION.getPriceAdjoint2(option, dataBlack, bsD, bsD2);
    pDF[0] = bsD[0] + bsD[1] * vD[0];
    pDF[1] = bsD2[0][2] + bsD2[1][0] * vD[1] + (bsD2[2][1] + bsD2[1][1] * vD[1]) * vD[0] + bsD[1] * vD2[1][0];
    final EuropeanVanillaOption optionKP = new EuropeanVanillaOption(_cutOffStrike * (1 + shift), _timeToExpiry, true);
    final double[] bsDKP = new double[3];
    final double[][] bsD2KP = new double[3][3];
    BLACK_FUNCTION.getPriceAdjoint2(optionKP, dataBlack, bsDKP, bsD2KP);
    final double bsD3FKK = (bsD2KP[2][0] - bsD2[2][0]) / (_cutOffStrike * shift);
    final BlackFunctionData dataBlackVP = new BlackFunctionData(_forward, 1.0, _volatilityK * (1 + shift));
    final double[] bsDVP = new double[3];
    final double[][] bsD2VP = new double[3][3];
    BLACK_FUNCTION.getPriceAdjoint2(option, dataBlackVP, bsDVP, bsD2VP);
    final double bsD3sss = (bsD2VP[1][1] - bsD2[1][1]) / (_volatilityK * shift);
    final double bsD3sFK = (bsD2VP[0][2] - bsD2[0][2]) / (_volatilityK * shift);
    final double bsD3sFs = (bsD2VP[0][1] - bsD2[0][1]) / (_volatilityK * shift);
    final double bsD3sKK = (bsD2VP[2][2] - bsD2[2][2]) / (_volatilityK * shift);
    final double bsD3ssK = (bsD2VP[1][2] - bsD2[1][2]) / (_volatilityK * shift);
    final double[] vDKP = new double[6];
    final double[][] vD2KP = new double[2][2];
    getVolatilityAdjoint2(optionKP, _forward, _sabrData, vDKP, vD2KP);
    final double vD3KKF = (vD2KP[1][0] - vD2[1][0]) / (_cutOffStrike * shift);
    pDF[2] = bsD3FKK + bsD3sFK * vD[1] + (bsD3sFK + bsD3sFs * vD[1]) * vD[1] + bsD2[1][0] * vD2[1][1] + (bsD3sKK + bsD3ssK * vD[1] + (bsD3ssK + bsD3sss * vD[1]) * vD[1] + bsD2[1][1] * vD2[1][1])
        * vD[0] + 2 * (bsD2[2][1] + bsD2[1][1] * vD[1]) * vD2[1][0] + bsD[1] * vD3KKF;
    final DoubleMatrix1D pDFvector = new DoubleMatrix1D(pDF);
    // Derivative of f with respect to abc.
    final double[][] fD = new double[3][3]; // fD[i][j]: derivative with respect to jth variable of f_i
    final double f = _priceK[0];
    final double fp = _priceK[1];
    final double fpp = _priceK[2];
    fD[0][0] = f;
    fD[0][1] = f / _cutOffStrike;
    fD[0][2] = fD[0][1] / _cutOffStrike;
    fD[1][0] = fp;
    fD[1][1] = (fp - fD[0][1]) / _cutOffStrike;
    fD[1][2] = (fp - 2 * fD[0][1]) / (_cutOffStrike * _cutOffStrike);
    fD[2][0] = fpp;
    fD[2][1] = (fpp + fD[0][2] * (2 * (_mu + 1) + 2 * _parameter[1] / _cutOffStrike + 4 * _parameter[2] / (_cutOffStrike * _cutOffStrike))) / _cutOffStrike;
    fD[2][2] = (fpp + fD[0][2] * (2 * (2 * _mu + 3) + 4 * _parameter[1] / _cutOffStrike + 8 * _parameter[2] / (_cutOffStrike * _cutOffStrike))) / (_cutOffStrike * _cutOffStrike);
    final DoubleMatrix2D fDmatrix = new DoubleMatrix2D(fD);
    // Derivative of abc with respect to forward
    final ColtMatrixAlgebra algebra = new ColtMatrixAlgebra();
    final DoubleMatrix2D fDInverse = algebra.getInverse(fDmatrix);
    final OGMatrixAlgebra algebraOG = new OGMatrixAlgebra();
    final DoubleMatrix1D derivativeF = (DoubleMatrix1D) algebraOG.multiply(fDInverse, pDFvector);
    return derivativeF.getData();
  }

  /**
   * Computes the derivative of the three fitting parameters with respect to the SABR parameters. 
   * The computation requires some third order derivatives; they are computed by finite difference on the second order derivatives.
   * Used to compute the derivative of the price with respect to the SABR parameters.
   * @return The derivatives.
   */
  private double[][] computesParametersDerivativeSABR() {
    double eps = 1.0E-15;
    final double[][] result = new double[4][3];
    if (Math.abs(_priceK[0]) < eps && Math.abs(_priceK[1]) < eps && Math.abs(_priceK[2]) < eps) {
      // Implementation note: If value and its derivatives is too small, then parameters are such that the extrapolated price is "very small".
      return result;
    }
    // Derivative of price with respect to SABR parameters.
    final double[][] pDSABR = new double[4][3]; // parameter SABR - equation
    final double shift = 0.00001;
    final EuropeanVanillaOption option = new EuropeanVanillaOption(_cutOffStrike, _timeToExpiry, true);
    final double[] vD = new double[6];
    final double[][] vD2 = new double[2][2];
    getVolatilityAdjoint2(option, _forward, _sabrData, vD, vD2);
    final BlackFunctionData dataBlack = new BlackFunctionData(_forward, 1.0, _volatilityK);
    for (int loopparam = 0; loopparam < 4; loopparam++) {
      final int paramIndex = 2 + loopparam;
      final double[] bsD = new double[3];
      final double[][] bsD2 = new double[3][3];
      BLACK_FUNCTION.getPriceAdjoint2(option, dataBlack, bsD, bsD2);
      pDSABR[loopparam][0] = bsD[1] * vD[paramIndex];
      final double[] vDpP = new double[6];
      final double[][] vD2pP = new double[2][2];
      SABRFormulaData sabrDatapP;
      double param;
      double paramShift;
      switch (loopparam) {
        case 0:
          param = _sabrData.getAlpha();
          paramShift = param * shift; // Relative shift to cope with difference in order of magnitude.
          sabrDatapP = _sabrData.withAlpha(param + paramShift);
          break;
        case 1:
          param = _sabrData.getBeta();
          paramShift = shift; // Absolute shift as usually 0 <= beta <= 1; beta can be zero, so relative shift is not possible.
          sabrDatapP = _sabrData.withBeta(param + paramShift);
          break;
        case 2:
          param = _sabrData.getRho();
          paramShift = shift; // Absolute shift as -1 <= rho <= 1; rho can be zero, so relative shift is not possible.
          sabrDatapP = _sabrData.withRho(param + paramShift);
          break;
        default:
          param = _sabrData.getNu();
          paramShift = param * shift; // Relative shift to cope with difference in order of magnitude.
          sabrDatapP = _sabrData.withNu(param + paramShift);
          break;
      }
      getVolatilityAdjoint2(option, _forward, sabrDatapP, vDpP, vD2pP);
      final double vD2Kp = (vDpP[1] - vD[1]) / paramShift;
      final double vD3KKa = (vD2pP[1][1] - vD2[1][1]) / paramShift;
      pDSABR[loopparam][1] = (bsD2[2][1] + bsD2[1][1] * vD[1]) * vD[paramIndex] + bsD[1] * vD2Kp;
      final double[] bsDVP = new double[3];
      final double[][] bsD2VP = new double[3][3];
      final BlackFunctionData dataBlackVP = new BlackFunctionData(_forward, 1.0, _volatilityK * (1 + shift));
      BLACK_FUNCTION.getPriceAdjoint2(option, dataBlackVP, bsDVP, bsD2VP);
      final double bsD3sss = (bsD2VP[1][1] - bsD2[1][1]) / (_volatilityK * shift);
      final double bsD3sKK = (bsD2VP[2][2] - bsD2[2][2]) / (_volatilityK * shift);
      final double bsD3ssK = (bsD2VP[1][2] - bsD2[1][2]) / (_volatilityK * shift);
      pDSABR[loopparam][2] = (bsD3sKK + bsD3ssK * vD[1] + (bsD3ssK + bsD3sss * vD[1]) * vD[1] + bsD2[1][1] * vD2[1][1]) * vD[paramIndex] + 2 * (bsD2[1][2] + bsD2[1][1] * vD[1]) * vD2Kp + bsD[1]
          * vD3KKa;
    }
    // Derivative of f with respect to abc.
    final double[][] fD = new double[3][3]; // fD[i][j]: derivative with respect to jth variable of f_i
    final double f = _priceK[0];
    final double fp = _priceK[1];
    final double fpp = _priceK[2];
    fD[0][0] = f;
    fD[0][1] = f / _cutOffStrike;
    fD[0][2] = fD[0][1] / _cutOffStrike;
    fD[1][0] = fp;
    fD[1][1] = (fp - fD[0][1]) / _cutOffStrike;
    fD[1][2] = (fp - 2 * fD[0][1]) / (_cutOffStrike * _cutOffStrike);
    fD[2][0] = fpp;
    fD[2][1] = (fpp + fD[0][2] * (2 * (_mu + 1) + 2 * _parameter[1] / _cutOffStrike + 4 * _parameter[2] / (_cutOffStrike * _cutOffStrike))) / _cutOffStrike;
    fD[2][2] = (fpp + fD[0][2] * (2 * (2 * _mu + 3) + 4 * _parameter[1] / _cutOffStrike + 8 * _parameter[2] / (_cutOffStrike * _cutOffStrike))) / (_cutOffStrike * _cutOffStrike);
    final DoubleMatrix2D fDmatrix = new DoubleMatrix2D(fD);
    // Derivative of abc with respect to forward
    final ColtMatrixAlgebra algebra = new ColtMatrixAlgebra();
    final DoubleMatrix2D fDInverse = algebra.getInverse(fDmatrix);
    final OGMatrixAlgebra algebraOG = new OGMatrixAlgebra();
    for (int loopparam = 0; loopparam < 4; loopparam++) {
      final DoubleMatrix1D pDSABRvector = new DoubleMatrix1D(pDSABR[loopparam]);
      final DoubleMatrix1D derivativeSABR = (DoubleMatrix1D) algebraOG.multiply(fDInverse, pDSABRvector);
      result[loopparam] = derivativeSABR.getData();
    }
    return result;
  }

  /**
   * The extrapolation function.
   * @param strike The strike.
   * @return The extrapolated price.
   */
  private double extrapolation(final double strike) {
    return Math.pow(strike, -_mu) * Math.exp(_parameter[0] + _parameter[1] / strike + _parameter[2] / (strike * strike));
  }

  /**
   * The first order derivative of the extrapolation function with respect to the strike.
   * @param strike The strike.
   * @return The extrapolated price.
   */
  private double extrapolationDerivative(final double strike) {
    return -extrapolation(strike) * (_mu + (_parameter[1] + 2 * _parameter[2] / strike) / strike) / strike;
  }

  private double[] getVolatilityAdjoint(final EuropeanVanillaOption option, final double forward, final SABRFormulaData data) {
    if (_sabrFunction instanceof SABRHaganVolatilityFunction) {
      return ((SABRHaganVolatilityFunction) _sabrFunction).getVolatilityAdjoint(option, forward, data);
    }

    //TODO Implement analytic formula for each volatility function
    double eps = 1.0e-6;
    double[] res = new double[7];
    res[0] = _sabrFunction.getVolatilityFunction(option, forward).evaluate(data);
    res[1] = fdSensitivity(option, forward, data, 1, eps);
    res[2] = fdSensitivity(option, forward, data, 2, eps);
    res[3] = fdSensitivity(option, forward, data, 3, eps);
    res[4] = fdSensitivity(option, forward, data, 4, eps);
    res[5] = fdSensitivity(option, forward, data, 5, eps);
    res[6] = fdSensitivity(option, forward, data, 6, eps);
    return res;
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
      double b = -2 * c / _cCutOffStrike - (_cPriceK[1] / _cPriceK[0] * _cCutOffStrike + _cMu) * _cCutOffStrike;
      double k2 = _cCutOffStrike * _cCutOffStrike;
      double res = -_cPriceK[2] / _cPriceK[0] * k2 + _cMu * (_cMu + 1) + 2 * b * (_cMu + 1) / _cCutOffStrike + (2 * c * (2 * _cMu + 3) + b * b) / k2 + 4 * b * c / (k2 * _cCutOffStrike) + 4 * c * c
          / (k2 * k2);
      return res;
    }
  }

}
