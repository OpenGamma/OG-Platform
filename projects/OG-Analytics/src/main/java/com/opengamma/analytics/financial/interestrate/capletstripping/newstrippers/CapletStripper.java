/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.capletstripping.newstrippers;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import com.opengamma.analytics.financial.interestrate.capletstripping.CapFloor;
import com.opengamma.analytics.financial.model.volatility.SimpleOptionData;
import com.opengamma.analytics.financial.model.volatility.surface.VolatilitySurface;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.analytics.math.matrix.OGMatrixAlgebra;
import com.opengamma.analytics.math.rootfinding.newton.NewtonDefaultVectorRootFinder;
import com.opengamma.analytics.math.rootfinding.newton.NewtonVectorRootFinder;
import com.opengamma.analytics.math.statistics.leastsquare.LeastSquareResults;
import com.opengamma.analytics.math.statistics.leastsquare.NonLinearLeastSquare;
import com.opengamma.analytics.math.surface.Surface;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class CapletStripper {

  private static final OGMatrixAlgebra MA = new OGMatrixAlgebra();

  private static final NonLinearLeastSquare NLLS = new NonLinearLeastSquare();
  private static final NewtonVectorRootFinder ROOTFINDER = new NewtonDefaultVectorRootFinder();

  private final MultiCapFloorPricer2 _pricer;
  private final int _nCaps;
  private final int _nCaplets;
  private final double[] _capStartTimes;
  private final double[] _capEndTimes;
  // private final DoublesPair[] _strikeExpiry;

  private final VolatilitySurfaceProvider _volSurfProv;

  public CapletStripper(final List<CapFloor> caps, final MulticurveProviderInterface curves, final VolatilitySurfaceProvider volSurfProv) {
    ArgumentChecker.noNulls(caps, "caps null");
    ArgumentChecker.notNull(curves, "null curves");
    ArgumentChecker.notNull(volSurfProv, "volSurfProv");
    _volSurfProv = volSurfProv;
    _nCaps = caps.size();

    _capStartTimes = new double[_nCaps];
    _capEndTimes = new double[_nCaps];
    final Iterator<CapFloor> iter = caps.iterator();
    CapFloor cap = iter.next();

    _capStartTimes[0] = cap.getStartTime();
    _capEndTimes[0] = cap.getEndTime();
    int ii = 1;
    while (iter.hasNext()) {
      cap = iter.next();
      _capStartTimes[ii] = cap.getStartTime();
      _capEndTimes[ii] = cap.getEndTime();
      ii++;
    }

    _pricer = new MultiCapFloorPricer2(caps, curves);
    _nCaplets = _pricer.getTotalNumberOfCaplets();

  }

  //************************************************************************************************************
  //Least Square Methods 
  //************************************************************************************************************

  public LeastSquareResults leastSqrSolveForCapPrices(final double[] capPrices, final DoubleMatrix1D start) {

    final double[] errors = new double[_nCaps];
    Arrays.fill(errors, 1.0);
    return leastSqrSolveForCapPrices(capPrices, errors, start);
  }

  public LeastSquareResults leastSqrSolveForCapPrices(final double[] capPrices, final double[] errors, final DoubleMatrix1D start) {
    ArgumentChecker.notNull(start, "start");
    ArgumentChecker.isTrue(start.getNumberOfElements() == _volSurfProv.getNumModelParameters(), "length of start ({}) not equal to expected number of model parameters ({})",
        start.getNumberOfElements(), _volSurfProv.getNumModelParameters());
    ArgumentChecker.isTrue(capPrices.length >= _volSurfProv.getNumModelParameters(), "Number of cap prices ({}) is less than number of model parameters ({}). "
        + "It is not possible to solve this system. To use these prices and model, must use a penalty matrix method", capPrices.length, _volSurfProv.getNumModelParameters());
    checkErrors(errors);
    checkPrices(capPrices);
    final DoubleMatrix1D sigma = new DoubleMatrix1D(errors);

    final LeastSquareResults res = NLLS.solve(new DoubleMatrix1D(capPrices), sigma, getCapPriceFunction(), getCapPriceJacobianFunction(), start);
    return res;
  }

  public LeastSquareResults leastSqrSolveForCapVols(final double[] capVols, final DoubleMatrix1D start) {
    final double[] errors = new double[_nCaps];
    Arrays.fill(errors, 1.0);
    return leastSqrSolveForCapVols(capVols, errors, start);
  }

  public LeastSquareResults leastSqrSolveForCapVols(final double[] capVols, final double[] errors, final DoubleMatrix1D start) {
    ArgumentChecker.notNull(start, "start");
    ArgumentChecker.isTrue(start.getNumberOfElements() == _volSurfProv.getNumModelParameters(), "length of start ({}) not equal to expected number of model parameters ({})",
        start.getNumberOfElements(), _volSurfProv.getNumModelParameters());
    ArgumentChecker.isTrue(capVols.length >= _volSurfProv.getNumModelParameters(), "Number of cap prices ({}) is less than number of model parameters ({}). "
        + "It is not possible to solve this system. To use these prices and model, must use a penalty matrix method", capVols.length, _volSurfProv.getNumModelParameters());
    checkErrors(errors);
    checkVols(capVols);
    final DoubleMatrix1D sigma = new DoubleMatrix1D(errors);

    final LeastSquareResults res = NLLS.solve(new DoubleMatrix1D(capVols), sigma, getCapVolFunction(), getCapVolJacobianFunction(), start);
    return res;
  }

  //************************************************************************************************************
  //Root Finding Methods 
  //************************************************************************************************************
  public DoubleMatrix1D rootFindForCapPrices(final double[] capPrices, final DoubleMatrix1D start) {
    ArgumentChecker.notNull(start, "start");
    ArgumentChecker.isTrue(start.getNumberOfElements() == _volSurfProv.getNumModelParameters(), "length of start ({}) not equal to expected number of model parameters ({})",
        start.getNumberOfElements(), _volSurfProv.getNumModelParameters());
    ArgumentChecker.isTrue(capPrices.length == _volSurfProv.getNumModelParameters(), "Number of cap prices ({}) is not equal to number of model parameters ({}). " + "To root find they must equal",
        capPrices.length, _volSurfProv.getNumModelParameters());

    checkPrices(capPrices);

    final DoubleMatrix1D res = ROOTFINDER.getRoot(getCapPriceFunction(), /*getCapPriceJacobianFunction(),*/start);
    return res;
  }

  protected void checkPrices(final double[] capPrices) {
    ArgumentChecker.notEmpty(capPrices, "null cap prices");
    ArgumentChecker.isTrue(_nCaps == capPrices.length, "wrong number of capPrices, should have {}, but {} given", _nCaps, capPrices.length);
    final double[] base = _pricer.getIntrinsicCapValues();
    for (int i = 0; i < _nCaps; i++) {
      ArgumentChecker.isTrue(capPrices[i] >= base[i], "Cap price {} lower that intrinisic value {}", capPrices[i], base[i]);
    }
  }

  protected void checkVols(final double[] capVols) {
    ArgumentChecker.notEmpty(capVols, "null cap vols");
    ArgumentChecker.isTrue(_nCaps == capVols.length, "wrong number of capVols, should have {}, but {} given", _nCaps, capVols.length);
    for (int i = 0; i < _nCaps; i++) {
      ArgumentChecker.isTrue(capVols[i] >= 0.0, "Cap vol {} less than zero", capVols[i]);
    }
  }

  protected void checkErrors(final double[] errors) {
    ArgumentChecker.notEmpty(errors, "null errors");
    ArgumentChecker.isTrue(_nCaps == errors.length, "wrong number of errors, should have {}, but {} given", _nCaps, errors.length);
    for (int i = 0; i < _nCaps; i++) {
      ArgumentChecker.isTrue(errors[i] > 0.0, "erros {} less than zero or equal to zero", errors[i]);
    }
  }

  private Function1D<DoubleMatrix1D, DoubleMatrix1D> getCapPriceFunction() {

    return new Function1D<DoubleMatrix1D, DoubleMatrix1D>() {
      @Override
      public DoubleMatrix1D evaluate(final DoubleMatrix1D x) {
        final VolatilitySurface volSurface = _volSurfProv.getVolSurface(x);
        final double[] modPrices = _pricer.price(volSurface);
        return new DoubleMatrix1D(modPrices);
      }
    };
  }

  protected Function1D<DoubleMatrix1D, DoubleMatrix1D> getCapVolFunction() {

    return new Function1D<DoubleMatrix1D, DoubleMatrix1D>() {
      @Override
      public DoubleMatrix1D evaluate(final DoubleMatrix1D x) {
        final VolatilitySurface volSurface = _volSurfProv.getVolSurface(x);
        final double[] modVols = _pricer.impliedVols(volSurface);
        return new DoubleMatrix1D(modVols);
      }
    };
  }

  private Function1D<DoubleMatrix1D, DoubleMatrix2D> getCapPriceJacobianFunction() {

    return new Function1D<DoubleMatrix1D, DoubleMatrix2D>() {
      @Override
      public DoubleMatrix2D evaluate(final DoubleMatrix1D x) {
        final VolatilitySurface volSurface = _volSurfProv.getVolSurface(x);
        final Surface<Double, Double, DoubleMatrix1D> volSurfaceAdjoint = _volSurfProv.getVolSurfaceAdjoint(x);
        final double[] vols = _pricer.getCapletVols(volSurface);
        final double[] capPrices = _pricer.priceFromCapletVols(vols);

        //cap vega matrix - sensitivity of cap prices to the volatilities of the caplets 
        final DoubleMatrix2D vega = _pricer.vegaFromCapletVols(vols);

        final SimpleOptionData[] capletsArray = _pricer.getCapletArray();

        final int nCaplets = capletsArray.length;
        final double[][] data = new double[nCaplets][];
        for (int i = 0; i < nCaplets; i++) {
          final SimpleOptionData caplet = capletsArray[i];
          data[i] = volSurfaceAdjoint.getZValue(caplet.getTimeToExpiry(), caplet.getStrike()).getData();
        }

        //sensitivities of the caplet volatilities to model parameters 
        final DoubleMatrix2D volJac = new DoubleMatrix2D(data);

        //sensitivity of the cap prices to the model parameters 
        return (DoubleMatrix2D) MA.multiply(vega, volJac);
      }
    };
  }

  protected Function1D<DoubleMatrix1D, DoubleMatrix2D> getCapVolJacobianFunction() {

    return new Function1D<DoubleMatrix1D, DoubleMatrix2D>() {
      @Override
      public DoubleMatrix2D evaluate(final DoubleMatrix1D x) {
        final VolatilitySurface volSurface = _volSurfProv.getVolSurface(x);
        final Surface<Double, Double, DoubleMatrix1D> volSurfaceAdjoint = _volSurfProv.getVolSurfaceAdjoint(x);
        final double[] vols = _pricer.getCapletVols(volSurface);
        final double[] capPrices = _pricer.priceFromCapletVols(vols);

        //cap vega matrix - sensitivity of cap prices to the volatilities of the caplets 
        final DoubleMatrix2D vega = _pricer.vegaFromCapletVols(vols);

        //sensitivity of the cap prices to their volatilities 
        final double[] capVega = _pricer.vega(_pricer.impliedVols(capPrices));

        final double[][] temp = new double[_nCaps][_nCaplets];
        for (int i = 0; i < _nCaps; i++) {
          for (int j = 0; j < _nCaplets; j++) {
            temp[i][j] = vega.getEntry(i, j) / capVega[i];
          }
        }

        //TODO this calculation should be handled by the pricer 
        //sensitivity of the cap (implied) volatilities to the caplet volatilities
        final DoubleMatrix2D capVolVega = new DoubleMatrix2D(temp);

        final SimpleOptionData[] capletsArray = _pricer.getCapletArray();

        final int nCaplets = capletsArray.length;
        final double[][] data = new double[nCaplets][];
        for (int i = 0; i < nCaplets; i++) {
          final SimpleOptionData caplet = capletsArray[i];
          data[i] = volSurfaceAdjoint.getZValue(caplet.getTimeToExpiry(), caplet.getStrike()).getData();
        }

        //sensitivities of the caplet volatilities to model parameters 
        final DoubleMatrix2D volJac = new DoubleMatrix2D(data);

        //sensitivity of the cap prices to the model parameters 
        return (DoubleMatrix2D) MA.multiply(capVolVega, volJac);
      }
    };
  }
}
