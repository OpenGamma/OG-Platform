/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.pricing.analytic.formula;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.model.volatility.smile.function.SABRFormulaData;
import com.opengamma.financial.model.volatility.smile.function.SABRHaganVolatilityFunction;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.linearalgebra.DecompositionFactory;
import com.opengamma.math.linearalgebra.LUDecompositionCommons;
import com.opengamma.math.matrix.ColtMatrixAlgebra;
import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.math.matrix.DoubleMatrix2D;
import com.opengamma.math.matrix.OGMatrixAlgebra;
import com.opengamma.math.rootfinding.newton.NewtonDefaultVectorRootFinder;

/**
 * Pricing function in the SABR model with Hagan et al. volatility function and controlled extrapolation for large strikes by extrapolation on call prices.
 * The form of the extrapolation as a function of the strike is
 * {@latex.ilb %preamble{\\usepackage{amsmath}}
 * \\begin{equation*}
 * f(K) = K^{-\\mu} \\exp\\left( a + \\frac{b}{K} + \\frac{c}{K^2} \\right).
 * \\end{equation*}
 * }
 * Reference: Benaim, S., Dodgson, M., and Kainth, D. (2008). An arbitrage-free method for smile extrapolation. Technical report, Royal Bank of Scotland.
 * OpenGamma implementation note: Smile extrapolation, version 1.2, May 2011.
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
  private double[][] _parameterDerivativeSABR = new double[3][3];
  /**
   * Flag indicating if the parameter derivatives to SABR parameters have been computed.
   */
  private boolean _parameterDerivativeSABRComputed;
  /**
   * The Black implied volatility at the cut-off strike.
   */
  private double _volatilityK;
  /**
   * The price and its derivatives at the cut-off strike.
   */
  private final double[] _priceK = new double[3];
  /**
   * The time to option expiry.
   */
  private final double _timeToExpiry;
  /**
   * Volatility provider function. Currently implemented only for SABRHaganVolatilityFunction.
   */
  private final SABRHaganVolatilityFunction _sabrFunction;
  /**
   * Black function used.
   */
  private static final BlackPriceFunction BLACK_FUNCTION = new BlackPriceFunction();

  public SABRExtrapolationRightFunction(final double forward, final SABRFormulaData sabrData, final double cutOffStrike, final double timeToExpiry, final double mu) {
    Validate.notNull(sabrData, "SABR data");
    _forward = forward;
    _sabrData = sabrData;
    _cutOffStrike = cutOffStrike;
    _timeToExpiry = timeToExpiry;
    _mu = mu;
    _sabrFunction = new SABRHaganVolatilityFunction();
    _parameter = computesFittingParameters();
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
   * Computes the option price derivative with respect to the forward. The price is SABR below the cut-off strike and extrapolated beyond.
   * @param option The option.
   * @return The option derivative.
   */
  public double priceDerivativeForward(final EuropeanVanillaOption option) {
    double[] pA;
    double priceDerivative;
    final double k = option.getStrike();
    if (k <= _cutOffStrike) { // Uses Hagan et al SABR function.
      final double[] volatilityA = _sabrFunction.getVolatilityAdjoint(option, _forward, _sabrData);
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
    }
    return priceDerivative;
  }

  /**
   * Computes the option price derivative with respect to the SABR parameters. The price is SABR below the cut-off strike and extrapolated beyond.
   * @param option The option.
   * @param priceDerivativeSABR An array of three doubles in which the derivative with respect to SABR parameters will be stored. 
   * The derivatives are w.r.t. [0] alpha, [1] rho, and [2] nu.
   * @return The option derivative.
   */
  public double priceAdjointSABR(final EuropeanVanillaOption option, final double[] priceDerivativeSABR) {
    double[] pA;
    double price;
    final double k = option.getStrike();
    if (k <= _cutOffStrike) { // Uses Hagan et al SABR function.
      final double[] volatilityA = _sabrFunction.getVolatilityAdjointOld(option, _forward, _sabrData);
      final BlackFunctionData dataBlack = new BlackFunctionData(_forward, 1.0, volatilityA[0]);
      pA = BLACK_FUNCTION.getPriceAdjoint(option, dataBlack);
      price = pA[0];
      for (int loopparam = 0; loopparam < 3; loopparam++) {
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
      for (int loopparam = 0; loopparam < 3; loopparam++) {
        priceDerivativeSABR[loopparam] = fDa * _parameterDerivativeSABR[loopparam][0] + fDb * _parameterDerivativeSABR[loopparam][1] + fDc * _parameterDerivativeSABR[loopparam][2];
      }
    }
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
    final double[] param = new double[3];
    final EuropeanVanillaOption option = new EuropeanVanillaOption(_cutOffStrike, _timeToExpiry, true);
    // Computes derivatives at cut-off.
    final double[] vD = new double[5];
    final double[][] vD2 = new double[2][2];
    _volatilityK = _sabrFunction.getVolatilityAdjoint2(option, _forward, _sabrData, vD, vD2);
    final BlackFunctionData dataBlack = new BlackFunctionData(_forward, 1.0, _volatilityK);
    final double[] bsD = new double[3];
    final double[][] bsD2 = new double[3][3];
    _priceK[0] = BLACK_FUNCTION.getPriceAdjoint2(option, dataBlack, bsD, bsD2);
    _priceK[1] = bsD[2] + bsD[1] * vD[1];
    _priceK[2] = bsD2[2][2] + bsD2[1][2] * vD[1] + (bsD2[2][1] + bsD2[1][1] * vD[1]) * vD[1] + bsD[1] * vD2[1][1];
    final BcFunction toSolveBC = new BcFunction(_priceK, _cutOffStrike, _mu);
    final double absoluteTol = 1E-5;
    final double relativeTol = 1E-5;
    final int maxSteps = 10000;
    final NewtonDefaultVectorRootFinder finder = new NewtonDefaultVectorRootFinder(absoluteTol, relativeTol, maxSteps, DecompositionFactory.LU_COMMONS);
    final DoubleMatrix1D startPosition = new DoubleMatrix1D(new double[] {0.1, 0.1});
    final DoubleMatrix1D ab = finder.getRoot(toSolveBC, startPosition);
    param[1] = ab.getEntry(0);
    param[2] = ab.getEntry(1);
    param[0] = Math.log(_priceK[0] / Math.pow(_cutOffStrike, -_mu)) - param[1] / _cutOffStrike - param[2] / (_cutOffStrike * _cutOffStrike);
    return param;
  }

  /**
   * Computes the derivative of the three fitting parameters with respect to the forward. 
   * Used to compute the derivative of the price with respect to the forward.
   * @return The derivatives.
   */
  private double[] computesParametersDerivativeForward() {
    // Derivative of price with respect to forward.
    final double[] pDF = new double[3];
    final double shift = 0.00001;
    final EuropeanVanillaOption option = new EuropeanVanillaOption(_cutOffStrike, _timeToExpiry, true);
    final double[] vD = new double[5];
    final double[][] vD2 = new double[2][2];
    _sabrFunction.getVolatilityAdjoint2(option, _forward, _sabrData, vD, vD2);
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
    final double[] vDKP = new double[5];
    final double[][] vD2KP = new double[2][2];
    _sabrFunction.getVolatilityAdjoint2(optionKP, _forward, _sabrData, vDKP, vD2KP);
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

  private double[][] computesParametersDerivativeSABR() {
    // Derivative of price with respect to SABR parameters.
    final double[][] pDSABR = new double[3][3]; // parameter SABR - equation
    final double shift = 0.00001;
    final EuropeanVanillaOption option = new EuropeanVanillaOption(_cutOffStrike, _timeToExpiry, true);
    final double[] vD = new double[5];
    final double[][] vD2 = new double[2][2];
    _sabrFunction.getVolatilityAdjoint2(option, _forward, _sabrData, vD, vD2);
    final BlackFunctionData dataBlack = new BlackFunctionData(_forward, 1.0, _volatilityK);
    for (int loopparam = 0; loopparam < 3; loopparam++) {
      final int paramIndex = 2 + loopparam;
      final double[] bsD = new double[3];
      final double[][] bsD2 = new double[3][3];
      BLACK_FUNCTION.getPriceAdjoint2(option, dataBlack, bsD, bsD2);
      pDSABR[loopparam][0] = bsD[1] * vD[paramIndex];
      final double[] vDpP = new double[5];
      final double[][] vD2pP = new double[2][2];
      SABRFormulaData sabrDatapP;
      double param;
      if (loopparam == 0) {
        param = _sabrData.getAlpha();
        sabrDatapP = _sabrData.withAlpha(param * (1 + shift));
      } else if (loopparam == 1) {
        param = _sabrData.getRho();
        sabrDatapP = _sabrData.withRho(param * (1 + shift));
      } else {
        param = _sabrData.getNu();
        sabrDatapP = _sabrData.withNu(param * (1 + shift));
      }
      _sabrFunction.getVolatilityAdjoint2(option, _forward, sabrDatapP, vDpP, vD2pP);
      final double vD2Kp = (vDpP[1] - vD[1]) / (param * shift);
      final double vD3KKa = (vD2pP[1][1] - vD2[1][1]) / (param * shift);
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
    final double[][] result = new double[3][3];
    for (int loopparam = 0; loopparam < 3; loopparam++) {
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
   * Inner class to solve the two dimension equation required to obtain b and c parameters. 
   */
  private class BcFunction extends Function1D<DoubleMatrix1D, DoubleMatrix1D> {
    /**
     * Array with the option price and its derivatives at the cut-off strike;
     */
    private final double[] _price;
    /**
     * The cut-off strike (in the root finding function). The smile is extrapolated above that level.
     */
    private final double _myCutOffStrike;
    /**
     * The tail thickness parameter (in the root finding function).
     */
    private final double _myMu;

    /**
     * Constructor of the two dimension function. 
     * @param price The option price and its derivatives.
     * @param cutOffStrike The cut-off strike.
     * @param mu The tail thickness parameter.
     */
    public BcFunction(final double[] price, final double cutOffStrike, final double mu) {
      _price = price;
      _myCutOffStrike = cutOffStrike;
      _myMu = mu;
    }

    @Override
    public DoubleMatrix1D evaluate(final DoubleMatrix1D x) {
      final double[] data = new double[2];
      data[0] = _price[0] * -(_myMu + (x.getEntry(0) + 2 * x.getEntry(1) / _myCutOffStrike) / _myCutOffStrike) / _myCutOffStrike - _price[1];
      data[1] = _price[0]
          * (_myMu * (_myMu + 1) + 2 * x.getEntry(0) * (_myMu + 1) / _myCutOffStrike + (2 * x.getEntry(1) * (2 * _myMu + 3) + x.getEntry(0) * x.getEntry(0)) / (_myCutOffStrike * _myCutOffStrike) + 4
              * x.getEntry(0) * x.getEntry(1) / (_myCutOffStrike * _myCutOffStrike * _myCutOffStrike) + 4 * x.getEntry(1) * x.getEntry(1)
              / (_myCutOffStrike * _myCutOffStrike * _myCutOffStrike * _myCutOffStrike)) / (_myCutOffStrike * _myCutOffStrike) - _price[2];
      return new DoubleMatrix1D(data);
    }

  }

}
