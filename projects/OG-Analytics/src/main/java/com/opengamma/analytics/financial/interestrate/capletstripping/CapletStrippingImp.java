/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.capletstripping;

import java.util.Arrays;

import com.opengamma.analytics.financial.model.volatility.discrete.DiscreteVolatilityFunction;
import com.opengamma.analytics.financial.model.volatility.discrete.DiscreteVolatilityFunctionProvider;
import com.opengamma.analytics.financial.model.volatility.discrete.DiscreteVolatilityFunctionProviderFromVolSurface;
import com.opengamma.analytics.financial.model.volatility.surface.VolatilitySurfaceProvider;
import com.opengamma.analytics.math.MathException;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.linearalgebra.CholeskyDecompositionCommons;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.analytics.math.matrix.OGMatrixAlgebra;
import com.opengamma.analytics.math.rootfinding.newton.NewtonDefaultVectorRootFinder;
import com.opengamma.analytics.math.rootfinding.newton.NewtonVectorRootFinder;
import com.opengamma.analytics.math.statistics.leastsquare.LeastSquareResults;
import com.opengamma.analytics.math.statistics.leastsquare.NonLinearLeastSquare;
import com.opengamma.analytics.math.statistics.leastsquare.NonLinearLeastSquareWithPenalty;
import com.opengamma.util.ArgumentChecker;

/**
 * Implementation for caplet stripping. This can be used directly by supplying either a {@link VolatilitySurfaceProvider} 
 * or a {@link DiscreteVolatilityFunctionProvider} and (in the case of penalty based methods) a penalty matrix.
 * Alternatively one can use a concrete implementation of {@link CapletStripper} which wraps this.
 */
public class CapletStrippingImp {

  private static final OGMatrixAlgebra MA = new OGMatrixAlgebra();

  private static final NonLinearLeastSquare NLLS = new NonLinearLeastSquare();
  private static final NewtonVectorRootFinder ROOTFINDER = new NewtonDefaultVectorRootFinder();
  private static final NonLinearLeastSquareWithPenalty NLLSWP = new NonLinearLeastSquareWithPenalty(new CholeskyDecompositionCommons());

  private final MultiCapFloorPricer _pricer;
  private final int _nModelParms;
  private final DiscreteVolatilityFunction _volFunc;

  /**
   * set up the caplet stripper 
   * @param pricer The pricer (which contained the details of the market values of the caps/floors)
   * @param volSurfProvider A parameterised description of a (caplet) volatility surface
   */
  public CapletStrippingImp(final MultiCapFloorPricer pricer, final VolatilitySurfaceProvider volSurfProvider) {
    this(pricer, new DiscreteVolatilityFunctionProviderFromVolSurface(volSurfProvider));
  }

  /**
   * set up the caplet stripper 
   * @param pricer The pricer (which contained the details of the market values of the caps/floors)
   * @param volFuncProv A mapping between model parameters and caplet volatility (strictly in the order expected by 
   * the pricer)
   */
  public CapletStrippingImp(final MultiCapFloorPricer pricer, final DiscreteVolatilityFunctionProvider volFuncProv) {
    ArgumentChecker.notNull(pricer, "pricer");
    ArgumentChecker.notNull(volFuncProv, "volFuncProv");

    _pricer = pricer;
    // _nModelParms = volFuncProv.getNumModelParameters();
    _volFunc = volFuncProv.from(pricer.getExpiryStrikeArray());
    _nModelParms = _volFunc.getLengthOfDomain();
  }

  /**
   * If the number of model parameters equals the number of cap prices, this first tries to root-find for the cap prices;
   * if there are more cap prices than model parameters (or the root-find failed), this solves for cap price in an (unweighed)
   * least-square sense  
   * @param capPrices The market cap prices. This <b>must</b> be in the same order (and the same number) and the caps
   * in the pricer passed to the constructor. 
   * @param start An initial guess for the model parameters 
   * @return the results of the stripping 
   */
  public CapletStrippingResult solveForCapPrices(final double[] capPrices, final DoubleMatrix1D start) {
    ArgumentChecker.notNull(capPrices, "capPrices");
    //try to root-find first
    if (_nModelParms == capPrices.length) {
      try {
        return rootFindForCapPrices(capPrices, start);
      } catch (final MathException e) {
      }
    }
    //if the root-finder failed or there are more market prices than model parameters, try to solve in a least-square sence 
    return leastSqrSolveForCapPrices(capPrices, start);
  }

  /**
   * If the number of model parameters equals the number of cap prices, this first tries to root-find for the cap prices;
   * if there are more cap prices than model parameters (or the root-find failed), this solves for cap price in a
   * least-square sense (weighted by the errors)  
   * @param capPrices The market cap prices. This <b>must</b> be in the same order (and the same number) and the caps
   * in the pricer passed to the constructor. 
   * @param errors The 'error' in the cap price (all must be positive). 
   * @param start An initial guess for the model parameters 
   * @return the results of the stripping 
   */
  public CapletStrippingResult solveForCapPrices(final double[] capPrices, final double[] errors, final DoubleMatrix1D start) {
    ArgumentChecker.notNull(capPrices, "capPrices");
    //try to root-find first
    if (_nModelParms == capPrices.length) {
      try {
        return rootFindForCapPrices(capPrices, errors, start);
      } catch (final MathException e) {
      }
    }
    //if the root-finder failed or there are more market prices than model parameters, try to solve in a least-square sence 
    return leastSqrSolveForCapPrices(capPrices, errors, start);
  }

  /**
   * If the number of model parameters equals the number of cap vols, this first tries to root-find for the cap vols;
   * if there are more cap vols than model parameters (or the root-find failed), this solves for cap vol in an (unweighed)
   * least-square sense  
   * @param capVols The market cap volatilities. This <b>must</b> be in the same order (and the same number) and the caps
   * in the pricer passed to the constructor. 
   * @param start An initial guess for the model parameters 
   * @return the results of the stripping 
   */
  public CapletStrippingResult solveForCapVols(final double[] capVols, final DoubleMatrix1D start) {
    ArgumentChecker.notNull(capVols, "capVols");
    //try to root-find first
    if (_nModelParms == capVols.length) {
      try {
        return rootFindForCapVols(capVols, start);
      } catch (final MathException e) {
      }
    }
    //if the root-finder failed or there are more market prices than model parameters, try to solve in a least-square sence 
    return leastSqrSolveForCapVols(capVols, start);
  }

  /**
   * If the number of model parameters equals the number of cap vols, this first tries to root-find for the cap vols;
   * if there are more cap vols than model parameters (or the root-find failed), this solves for cap vols in a
   * least-square sense (weighted by the errors)  
   * @param capVols The market cap volatilities. This <b>must</b> be in the same order (and the same number) and the caps
   * in the pricer passed to the constructor. 
   * @param errors The 'error' in the cap volatilities (all must be positive). 
   * @param start An initial guess for the model parameters 
   * @return the results of the stripping 
   */
  public CapletStrippingResult solveForCapVols(final double[] capVols, final double[] errors, final DoubleMatrix1D start) {
    ArgumentChecker.notNull(capVols, "capVols");
    //try to root-find first
    if (_nModelParms == capVols.length) {
      try {
        return rootFindForCapVols(capVols, errors, start);
      } catch (final MathException e) {
      }
    }
    //if the root-finder failed or there are more market prices than model parameters, try to solve in a least-square sence 
    return leastSqrSolveForCapVols(capVols, errors, start);
  }

  //************************************************************************************************************
  //Least Square Methods 
  //************************************************************************************************************

  /**
   * This solves for cap price in an (unweighed) least-square sense  
   * @param capPrices The market cap prices. This <b>must</b> be in the same order (and the same number) and the caps
   * in the pricer passed to the constructor. 
   * @param start An initial guess for the model parameters 
   * @return the results of the stripping 
   */
  public CapletStrippingResult leastSqrSolveForCapPrices(final double[] capPrices, final DoubleMatrix1D start) {
    final double[] errors = new double[capPrices.length];
    Arrays.fill(errors, 1.0);
    return leastSqrSolveForCapPrices(capPrices, errors, start);
  }

  /**
   * This solves for cap price in a least-square sense (weighted by the errors)  
   * @param capPrices The market cap prices. This <b>must</b> be in the same order (and the same number) and the caps
   * in the pricer passed to the constructor. 
   * @param errors The 'error' in the cap price (all must be positive). 
   * @param start An initial guess for the model parameters 
   * @return the results of the stripping 
   */
  public CapletStrippingResult leastSqrSolveForCapPrices(final double[] capPrices, final double[] errors, final DoubleMatrix1D start) {
    ArgumentChecker.notNull(start, "start");
    ArgumentChecker.isTrue(start.getNumberOfElements() == _nModelParms, "length of start ({}) not equal to expected number of model parameters ({})", start.getNumberOfElements(), _nModelParms);
    ArgumentChecker.isTrue(capPrices.length >= _nModelParms, "Number of cap prices ({}) is less than number of model parameters ({}). "
        + "It is not possible to solve this system. To use these prices and model, must use a penalty matrix method", capPrices.length, _nModelParms);
    checkErrors(errors);
    checkPrices(capPrices);
    final DoubleMatrix1D sigma = new DoubleMatrix1D(errors);

    final LeastSquareResults res = NLLS.solve(new DoubleMatrix1D(capPrices), sigma, getCapPriceFunction(), getCapPriceJacobianFunction(), start);
    return new CapletStrippingResultLeastSquare(res, _volFunc, _pricer);
  }

  /**
   * This solves for vol price in an (unweighed) least-square sense  
   * @param capVols The market cap volatilities. This <b>must</b> be in the same order (and the same number) and the caps
   * in the pricer passed to the constructor. 
   * @param start An initial guess for the model parameters 
   * @return the results of the stripping 
   */
  public CapletStrippingResult leastSqrSolveForCapVols(final double[] capVols, final DoubleMatrix1D start) {
    final double[] errors = new double[capVols.length];
    Arrays.fill(errors, 1.0);
    return leastSqrSolveForCapVols(capVols, errors, start);
  }

  /**
   * This solves for cap vols in a least-square sense (weighted by the errors)  
   * @param capVols The market cap volatilities. This <b>must</b> be in the same order (and the same number) and the caps
   * in the pricer passed to the constructor. 
   * @param errors The 'error' in the cap price (all must be positive). 
   * @param start An initial guess for the model parameters 
   * @return the results of the stripping 
   */
  public CapletStrippingResult leastSqrSolveForCapVols(final double[] capVols, final double[] errors, final DoubleMatrix1D start) {
    ArgumentChecker.notNull(start, "start");
    ArgumentChecker.isTrue(start.getNumberOfElements() == _nModelParms, "length of start ({}) not equal to expected number of model parameters ({})", start.getNumberOfElements(), _nModelParms);
    ArgumentChecker.isTrue(capVols.length >= _nModelParms, "Number of cap prices ({}) is less than number of model parameters ({}). "
        + "It is not possible to solve this system. To use these prices and model, must use a penalty matrix method", capVols.length, _nModelParms);
    checkErrors(errors);
    checkVols(capVols);
    final DoubleMatrix1D sigma = new DoubleMatrix1D(errors);

    final LeastSquareResults res = NLLS.solve(new DoubleMatrix1D(capVols), sigma, getCapVolFunction(), getCapVolJacobianFunction(), start);
    return new CapletStrippingResultLeastSquare(res, _volFunc, _pricer);
  }

  //************************************************************************************************************
  //Root Finding Methods 
  //************************************************************************************************************

  /**
   * Root-find for the cap prices
   * @param capPrices The market cap prices. This <b>must</b> be in the same order (and the same number) and the caps
   * in the pricer passed to the constructor. 
   * @param start An initial guess for the model parameters 
   * @return the results of the stripping 
   */
  public CapletStrippingResult rootFindForCapPrices(final double[] capPrices, final DoubleMatrix1D start) {
    ArgumentChecker.notNull(start, "start");
    ArgumentChecker.isTrue(start.getNumberOfElements() == _nModelParms, "length of start ({}) not equal to expected number of model parameters ({})", start.getNumberOfElements(), _nModelParms);
    ArgumentChecker.isTrue(capPrices.length == _nModelParms, "Number of cap prices ({}) is not equal to number of model parameters ({}). " + "To root find they must equal", capPrices.length,
        _nModelParms);

    checkPrices(capPrices);
    final DoubleMatrix1D res = ROOTFINDER.getRoot(getDiffFunc(getCapPriceFunction(), new DoubleMatrix1D(capPrices)), getCapPriceJacobianFunction(), start);
    return new CapletStrippingResultRootFind(res, _volFunc, _pricer);
  }

  /**
   * Root-find for the cap prices
   * @param capPrices The market cap prices. This <b>must</b> be in the same order (and the same number) and the caps
   * in the pricer passed to the constructor.   
   * @param errors This can be used to scale the problem when there is a massive difference in the magnitude of the prices. 
   * Common choices are the cap vega, or the cap market prices themselves.   
   * @param  start An initial guess for the model parameters 
   * @return the results of the stripping 
   */
  public CapletStrippingResult rootFindForCapPrices(final double[] capPrices, final double[] errors, final DoubleMatrix1D start) {
    ArgumentChecker.notNull(start, "start");
    ArgumentChecker.isTrue(start.getNumberOfElements() == _nModelParms, "length of start ({}) not equal to expected number of model parameters ({})", start.getNumberOfElements(), _nModelParms);
    ArgumentChecker.isTrue(capPrices.length == _nModelParms, "Number of cap prices ({}) is not equal to number of model parameters ({}). " + "To root find they must equal", capPrices.length,
        _nModelParms);

    checkPrices(capPrices);
    checkErrors(errors);
    final DoubleMatrix1D sigma = new DoubleMatrix1D(errors);
    final Function1D<DoubleMatrix1D, DoubleMatrix1D> func = getDiffFunc(getCapPriceFunction(), new DoubleMatrix1D(capPrices), sigma);
    final Function1D<DoubleMatrix1D, DoubleMatrix2D> jac = getWeightedJacobianFunction(getCapPriceJacobianFunction(), sigma);
    final DoubleMatrix1D res = ROOTFINDER.getRoot(func, jac, start);
    return new CapletStrippingResultRootFind(res, _volFunc, _pricer);
  }

  /**
   * Root-find for the cap vols
   * @param capVols The market cap volatilities. This <b>must</b> be in the same order (and the same number) and the caps
   * in the pricer passed to the constructor. 
   * @param start An initial guess for the model parameters 
   * @return the results of the stripping 
   */
  public CapletStrippingResult rootFindForCapVols(final double[] capVols, final DoubleMatrix1D start) {
    ArgumentChecker.notNull(start, "start");
    ArgumentChecker.isTrue(start.getNumberOfElements() == _nModelParms, "length of start ({}) not equal to expected number of model parameters ({})", start.getNumberOfElements(), _nModelParms);
    ArgumentChecker
        .isTrue(capVols.length == _nModelParms, "Number of cap prices ({}) is not equal to number of model parameters ({}). " + "To root find they must equal", capVols.length, _nModelParms);
    checkVols(capVols);
    final DoubleMatrix1D res = ROOTFINDER.getRoot(getDiffFunc(getCapVolFunction(), new DoubleMatrix1D(capVols)), getCapVolJacobianFunction(), start);
    return new CapletStrippingResultRootFind(res, _volFunc, _pricer);
  }

  /**
   * Root-find for the cap vols
   * @param capVols The market cap volatilities. This <b>must</b> be in the same order (and the same number) and the caps
   * in the pricer passed to the constructor.   
   * @param errors This can be used to scale the problem when there is a massive difference in the magnitude of the vols. 
   It is unlikely that this is needed. 
   * @param  start An initial guess for the model parameters 
   * @return the results of the stripping 
   */
  public CapletStrippingResult rootFindForCapVols(final double[] capVols, final double[] errors, final DoubleMatrix1D start) {
    ArgumentChecker.notNull(start, "start");
    ArgumentChecker.isTrue(start.getNumberOfElements() == _nModelParms, "length of start ({}) not equal to expected number of model parameters ({})", start.getNumberOfElements(), _nModelParms);
    ArgumentChecker
        .isTrue(capVols.length == _nModelParms, "Number of cap prices ({}) is not equal to number of model parameters ({}). " + "To root find they must equal", capVols.length, _nModelParms);
    checkVols(capVols);
    checkErrors(errors);
    final DoubleMatrix1D sigma = new DoubleMatrix1D(errors);
    final Function1D<DoubleMatrix1D, DoubleMatrix1D> func = getDiffFunc(getCapPriceFunction(), new DoubleMatrix1D(capVols), sigma);
    final Function1D<DoubleMatrix1D, DoubleMatrix2D> jac = getWeightedJacobianFunction(getCapVolJacobianFunction(), sigma);
    final DoubleMatrix1D res = ROOTFINDER.getRoot(func, jac, start);
    return new CapletStrippingResultRootFind(res, _volFunc, _pricer);
  }

  //************************************************************************************************************
  //Penalty Methods 
  //************************************************************************************************************

  /**
   *  This solves for cap price in a penalised least-square sense (weighted by the errors).   
   * @param capPrices The market cap prices. This <b>must</b> be in the same order (and the same number) and the caps
   * in the pricer passed to the constructor.  
   * @param errors This can be used to scale the problem when there is a massive difference in the magnitude of the prices. 
   * Common choices are the cap vega, or the cap market prices themselves.   
   * @param start  An initial guess for the model parameters 
   * @param penaltyMatrix A penalty matrix $P$. For a vector of model parameters, $x$, the penalty is computed as 
   * $x^TPx$; this is added to the (weighted) sum of squares difference between market and model prices. The strength of 
   * the penalty is controlled by scaling $P$ <b>before</b> passing it in.  
   * @param allowed specifies the domain of x 
   * @return the results of the stripping 
   */
  public CapletStrippingResult solveForCapPrices(final double[] capPrices, final double[] errors, final DoubleMatrix1D start, final DoubleMatrix2D penaltyMatrix,
      final Function1D<DoubleMatrix1D, Boolean> allowed) {
    ArgumentChecker.notNull(start, "start");
    ArgumentChecker.notNull(penaltyMatrix, "penaltyMatrix");
    ArgumentChecker.isTrue(start.getNumberOfElements() == _nModelParms, "length of start ({}) not equal to expected number of model parameters ({})", start.getNumberOfElements(), _nModelParms);
    checkErrors(errors);
    checkPrices(capPrices);
    ArgumentChecker.isTrue(penaltyMatrix.getNumberOfRows() == _nModelParms && penaltyMatrix.getNumberOfColumns() == _nModelParms,
        "Penalty matrix must be square of size {}. Supplied matrix is {] by {}", _nModelParms, penaltyMatrix.getNumberOfRows(), penaltyMatrix.getNumberOfColumns());

    final LeastSquareResults res = NLLSWP.solve(new DoubleMatrix1D(capPrices), new DoubleMatrix1D(errors), getCapPriceFunction(), getCapPriceJacobianFunction(), start, penaltyMatrix, allowed);
    return new CapletStrippingResultLeastSquare(res, _volFunc, _pricer);
  }

  /**
   *  This solves for cap volatilities in a penalised least-square sense (weighted by the errors).   
   * @param capVols The market cap volatilities. This <b>must</b> be in the same order (and the same number) and the caps
   * in the pricer passed to the constructor.  
   * @param errors This can be used to scale the problem when there is a massive difference in the magnitude of the prices. 
   * Common choices are the cap vega, or the cap market prices themselves.   
   * @param start  An initial guess for the model parameters 
   * @param penaltyMatrix A penalty matrix $P$. For a vector of model parameters, $x$, the penalty is computed as 
   * $x^TPx$; this is added to the (weighted) sum of squares difference between market and model prices. The strength of 
   * the penalty is controlled by scaling $P$ <b>before</b> passing it in.  
   * @return the results of the stripping 
   */
  public CapletStrippingResult solveForCapVols(final double[] capVols, final double[] errors, final DoubleMatrix1D start, final DoubleMatrix2D penaltyMatrix) {
    return solveForCapVols(capVols, errors, start, penaltyMatrix, NonLinearLeastSquareWithPenalty.UNCONSTAINED);
  }

  /**
   *  This solves for cap volatilities in a penalised least-square sense (weighted by the errors).   
   * @param capVols The market cap volatilities. This <b>must</b> be in the same order (and the same number) and the caps
   * in the pricer passed to the constructor.  
   * @param errors This can be used to scale the problem when there is a massive difference in the magnitude of the prices. 
   * Common choices are the cap vega, or the cap market prices themselves.   
   * @param start  An initial guess for the model parameters 
   * @param penaltyMatrix A penalty matrix $P$. For a vector of model parameters, $x$, the penalty is computed as 
   * $x^TPx$; this is added to the (weighted) sum of squares difference between market and model prices. The strength of 
   * the penalty is controlled by scaling $P$ <b>before</b> passing it in.  
   * @param allowed specifies the domain of x 
   * @return the results of the stripping 
   */
  public CapletStrippingResult solveForCapVols(final double[] capVols, final double[] errors, final DoubleMatrix1D start, final DoubleMatrix2D penaltyMatrix,
      final Function1D<DoubleMatrix1D, Boolean> allowed) {
    ArgumentChecker.notNull(start, "start");
    ArgumentChecker.notNull(penaltyMatrix, "penaltyMatrix");
    ArgumentChecker.isTrue(start.getNumberOfElements() == _nModelParms, "length of start ({}) not equal to expected number of model parameters ({})", start.getNumberOfElements(), _nModelParms);
    checkVols(capVols);
    checkErrors(errors);
    ArgumentChecker.isTrue(penaltyMatrix.getNumberOfRows() == _nModelParms && penaltyMatrix.getNumberOfColumns() == _nModelParms,
        "Penalty matrix must be square of size {}. Supplied matrix is {] by {}", _nModelParms, penaltyMatrix.getNumberOfRows(), penaltyMatrix.getNumberOfColumns());

    final LeastSquareResults res = NLLSWP.solve(new DoubleMatrix1D(capVols), new DoubleMatrix1D(errors), getCapVolFunction(), getCapVolJacobianFunction(), start, penaltyMatrix, allowed);
    return new CapletStrippingResultLeastSquare(res, _volFunc, _pricer);
  }

  protected void checkPrices(final double[] capPrices) {
    ArgumentChecker.notEmpty(capPrices, "null cap prices");
    final int nCaps = getNumCaps();
    ArgumentChecker.isTrue(nCaps == capPrices.length, "wrong number of capPrices, should have {}, but {} given", nCaps, capPrices.length);
    final double[] base = _pricer.getIntrinsicCapValues();
    for (int i = 0; i < nCaps; i++) {
      ArgumentChecker.isTrue(capPrices[i] >= base[i], "Cap price {} lower that intrinisic value {}", capPrices[i], base[i]);
    }
  }

  protected void checkVols(final double[] capVols) {

    ArgumentChecker.notEmpty(capVols, "null cap vols");
    final int nCaps = getNumCaps();
    ArgumentChecker.isTrue(nCaps == capVols.length, "wrong number of capVols, should have {}, but {} given", nCaps, capVols.length);
    for (int i = 0; i < nCaps; i++) {
      ArgumentChecker.isTrue(capVols[i] >= 0.0, "Cap vol {} less than zero", capVols[i]);
    }
  }

  protected void checkErrors(final double[] errors) {
    ArgumentChecker.notEmpty(errors, "null errors");
    final int nCaps = getNumCaps();
    ArgumentChecker.isTrue(nCaps == errors.length, "wrong number of errors, should have {}, but {} given", nCaps, errors.length);
    for (int i = 0; i < nCaps; i++) {
      ArgumentChecker.isTrue(errors[i] > 0.0, "erros {} less than zero or equal to zero", errors[i]);
    }
  }

  //************************************************************************************************************
  //Functions 
  //************************************************************************************************************

  /**
   * get the cap price function which takes a set of model parameters and returns cap prices. <b>Note:</b> protected 
   * access is given for testing.
   * @return The cap price function 
   */
  protected Function1D<DoubleMatrix1D, DoubleMatrix1D> getCapPriceFunction() {

    return new Function1D<DoubleMatrix1D, DoubleMatrix1D>() {
      @Override
      public DoubleMatrix1D evaluate(final DoubleMatrix1D x) {
        final DoubleMatrix1D capletVols = _volFunc.evaluate(x);
        final double[] modPrices = _pricer.priceFromCapletVols(capletVols.getData());
        return new DoubleMatrix1D(modPrices);
      }
    };
  }

  /**
   * get the cap volatility function which takes a set of model parameters and returns cap volatilities. <b>Note:</b> protected 
   * access is given for testing.
   * @return The cap volatility function 
   */
  protected Function1D<DoubleMatrix1D, DoubleMatrix1D> getCapVolFunction() {

    return new Function1D<DoubleMatrix1D, DoubleMatrix1D>() {
      @Override
      public DoubleMatrix1D evaluate(final DoubleMatrix1D x) {
        final DoubleMatrix1D capletVols = _volFunc.evaluate(x);
        final double[] modPrices = _pricer.priceFromCapletVols(capletVols.getData());
        final double[] modVols = _pricer.impliedVols(modPrices);
        return new DoubleMatrix1D(modVols);
      }
    };
  }

  private Function1D<DoubleMatrix1D, DoubleMatrix1D> getDiffFunc(final Function1D<DoubleMatrix1D, DoubleMatrix1D> func, final DoubleMatrix1D mrkVals) {
    ArgumentChecker.notNull(mrkVals, "mrkVals");
    return new Function1D<DoubleMatrix1D, DoubleMatrix1D>() {
      @Override
      public DoubleMatrix1D evaluate(final DoubleMatrix1D x) {
        final DoubleMatrix1D modelVal = func.evaluate(x);
        return (DoubleMatrix1D) MA.subtract(modelVal, mrkVals);
      }
    };
  }

  private Function1D<DoubleMatrix1D, DoubleMatrix1D> getDiffFunc(final Function1D<DoubleMatrix1D, DoubleMatrix1D> func, final DoubleMatrix1D mrkVals, final DoubleMatrix1D errors) {
    ArgumentChecker.notNull(mrkVals, "mrkVals");
    ArgumentChecker.notNull(errors, "errors");
    final int n = errors.getNumberOfElements();
    final double[] w = errors.toArray();
    for (int i = 0; i < n; i++) {
      ArgumentChecker.notNegativeOrZero(w[i], "errors must be possitive");
      w[i] = 1. / w[i];
    }
    final double[] b = mrkVals.getData();
    return new Function1D<DoubleMatrix1D, DoubleMatrix1D>() {
      @Override
      public DoubleMatrix1D evaluate(final DoubleMatrix1D x) {
        final double[] a = func.evaluate(x).getData();
        for (int i = 0; i < n; i++) {
          a[i] = w[i] * (a[i] - b[i]);
        }
        return new DoubleMatrix1D(a);
      }
    };
  }

  /**
   * get the cap price Jacobian function which takes a set of model parameters and returns cap price Jacobian (sensitivity
   *  of cap prices to model parameters). <b>Note:</b> protected access is given for testing.
   * @return The cap price Jacobian function 
   */
  protected Function1D<DoubleMatrix1D, DoubleMatrix2D> getCapPriceJacobianFunction() {

    return new Function1D<DoubleMatrix1D, DoubleMatrix2D>() {
      @Override
      public DoubleMatrix2D evaluate(final DoubleMatrix1D x) {

        final DoubleMatrix1D capletVols = _volFunc.evaluate(x);
        final DoubleMatrix2D capletVolJac = _volFunc.calculateJacobian(x);

        //cap vega matrix - sensitivity of cap prices to the volatilities of the caplets 
        final DoubleMatrix2D vega = _pricer.vegaFromCapletVols(capletVols.getData());

        //sensitivity of the cap prices to the model parameters 
        return (DoubleMatrix2D) MA.multiply(vega, capletVolJac);
      }
    };
  }

  /**
   * get the cap volatility Jacobian function which takes a set of model parameters and returns cap volatility Jacobian (sensitivity
   *  of cap volatilities to model parameters). <b>Note:</b> protected access is given for testing.
   * @return The cap price Jacobian function 
   */
  protected Function1D<DoubleMatrix1D, DoubleMatrix2D> getCapVolJacobianFunction() {

    return new Function1D<DoubleMatrix1D, DoubleMatrix2D>() {
      @Override
      public DoubleMatrix2D evaluate(final DoubleMatrix1D x) {
        final DoubleMatrix1D capletVols = _volFunc.evaluate(x);
        final DoubleMatrix2D capletVolJac = _volFunc.calculateJacobian(x);
        final DoubleMatrix2D capVolVega = _pricer.capVolVega(capletVols.getData());
        return (DoubleMatrix2D) MA.multiply(capVolVega, capletVolJac);
      }
    };
  }

  private Function1D<DoubleMatrix1D, DoubleMatrix2D> getWeightedJacobianFunction(final Function1D<DoubleMatrix1D, DoubleMatrix2D> jacFunc, final DoubleMatrix1D errors) {
    ArgumentChecker.notNull(errors, "errors");
    final int n = errors.getNumberOfElements();
    final double[] w = errors.toArray();
    for (int i = 0; i < n; i++) {
      ArgumentChecker.notNegativeOrZero(w[i], "errors must be possitive");
      w[i] = 1. / w[i];
    }

    return new Function1D<DoubleMatrix1D, DoubleMatrix2D>() {
      @Override
      public DoubleMatrix2D evaluate(final DoubleMatrix1D x) {
        final DoubleMatrix2D jac = jacFunc.evaluate(x);
        final double[][] data = jac.getData();
        for (int i = 0; i < n; i++) {
          for (int j = 0; j < _nModelParms; j++) {
            data[i][j] *= w[i];
          }
        }
        return jac;
      }
    };
  }

  /**
   * get the number of cap passed into the constructor (via pricer)
   * @return The number of caps
   */
  public int getNumCaps() {
    return _pricer.getNumCaps();
  }

  /**
   * Gets the pricer.
   * @return the pricer
   */
  public MultiCapFloorPricer getPricer() {
    return _pricer;
  }

  /**
   * Gets the nModelParms.
   * @return the nModelParms
   */
  public int getnModelParms() {
    return _nModelParms;
  }

  /**
   * Gets the volFunc.
   * @return the volFunc
   */
  public DiscreteVolatilityFunction getVolFunc() {
    return _volFunc;
  }
}
