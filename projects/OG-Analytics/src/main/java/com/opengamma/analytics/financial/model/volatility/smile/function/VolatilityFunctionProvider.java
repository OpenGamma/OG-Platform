/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.smile.function;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.analytics.math.matrix.MatrixAlgebra;
import com.opengamma.analytics.math.matrix.OGMatrixAlgebra;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 * @param <T> Type of the data needed for the volatility function
 */
public abstract class VolatilityFunctionProvider<T extends SmileModelData> {

  private static final MatrixAlgebra MA = new OGMatrixAlgebra();
  private static final double EPS = 1e-6;

  /**
   * Returns a function that, given data of type T, calculates the volatility.
   * @param option The option, not null
   * @param forward The forward value of the underlying
   * @return Returns a function that, given data of type T, calculates the volatility
   */
  public abstract Function1D<T, Double> getVolatilityFunction(final EuropeanVanillaOption option, final double forward);

  /**
   * Returns a function that, given data of type T, calculates the volatilities for the given strikes
   * @param forward the forward
   * @param strikes set of strikes
   * @param timeToExpiry time-to-expiry
   * @return A set of volatilities for the given strikes
   */
  public Function1D<T, double[]> getVolatilityFunction(final double forward, final double[] strikes, final double timeToExpiry) {

    final int n = strikes.length;
    final List<Function1D<T, Double>> funcs = new ArrayList<>(n);
    for (int i = 0; i < n; i++) {
      final boolean isCall = strikes[i] >= forward;
      funcs.add(getVolatilityFunction(new EuropeanVanillaOption(strikes[i], timeToExpiry, isCall), forward));
    }

    return new Function1D<T, double[]>() {
      @Override
      public double[] evaluate(final T data) {
        final double[] res = new double[n];
        for (int i = 0; i < n; i++) {
          res[i] = funcs.get(i).evaluate(data);
        }
        return res;
      }

    };
  }

  /**
   * Returns a function  that calculates volatility and the ajoint (volatility sensitivity to forward, strike and model parameters)
   * by means of central finite difference - this should be overridden where possible
   * @param option The option, not null
   * @param forward The forward value of the underlying
   * @return Returns a function that, given data of type T, calculates the volatility adjoint
   */
  public Function1D<T, double[]> getVolatilityAdjointFunction(final EuropeanVanillaOption option, final double forward) {
    Validate.notNull(option, "option");
    Validate.isTrue(forward >= 0.0, "forward must be greater than zero");
    final Function1D<T, Double> func = getVolatilityFunction(option, forward);

    return new Function1D<T, double[]>() {
      @SuppressWarnings("synthetic-access")
      @Override
      public final double[] evaluate(final T data) {
        Validate.notNull(data, "data");
        final double[] x = new double[3 + data.getNumberOfParameters()]; //vol, fwd, strike, the model parameters
        x[0] = func.evaluate(data);
        x[1] = forwardBar(option, forward, data);
        x[2] = strikeBar(option, forward, data);
        System.arraycopy(paramBar(func, data), 0, x, 3, data.getNumberOfParameters());
        return x;
      }
    };
  }

  /**
   *Returns a function  that calculates the volatility sensitivity to model parameters
   * by means of central finite difference - this should be overridden where possible
   * @param option The option, not null
   * @param forward The forward value of the underlying
   * @return Returns a function that, given data of type T, calculates the volatility model sensitivity
   */
  public Function1D<T, double[]> getModelAdjointFunction(final EuropeanVanillaOption option, final double forward) {
    Validate.notNull(option, "option");
    Validate.isTrue(forward >= 0.0, "forward must be greater than zero");
    final Function1D<T, Double> func = getVolatilityFunction(option, forward);

    return new Function1D<T, double[]>() {
      @SuppressWarnings("synthetic-access")
      @Override
      public final double[] evaluate(final T data) {
        Validate.notNull(data, "data");
        return paramBar(func, data);
      }
    };
  }

  /**
   * The volatility adjoint set by finite difference, unless this is overloaded
   * @param forward Forward value of underlying
   * @param strikes The strikes
   * @param timeToExpiry Time-toExpiry
   * @return The first column is is vol at each strike, the second and third are the forward and strike sensitivity at each strike, and the
   * remaining columns are the model parameter sensitivities are each strike
   */
  public Function1D<T, double[][]> getVolatilityAdjointFunction(final double forward, final double[] strikes, final double timeToExpiry) {

    final int n = strikes.length;
    final Function1D<T, double[]> func = getVolatilityFunction(forward, strikes, timeToExpiry);

    return new Function1D<T, double[][]>() {
      @SuppressWarnings("synthetic-access")
      @Override
      public double[][] evaluate(final T data) {
        Validate.notNull(data, "data");
        final double[][] res = new double[3 + data.getNumberOfParameters()][n];
        res[0] = func.evaluate(data);
        res[1] = forwardBar(strikes, timeToExpiry, forward, data);
        res[2] = strikeBar(strikes, timeToExpiry, forward, data);
        final double[][] temp = paramBarSet(func, data);
        final int m = temp.length;
        for (int i = 0; i < m; i++) {
          res[3 + i] = temp[i];
        }
        //now transpose
        //TODO a transpose that works on double[][]?
        return MA.getTranspose(new DoubleMatrix2D(res)).getData();
      }
    };
  }

  protected Function1D<T, double[][]> getVolatilityAdjointFunctionByCallingSingleStrikes(final double forward, final double[] strikes, final double timeToExpiry) {

    final int n = strikes.length;
    final List<Function1D<T, double[]>> funcs = new ArrayList<>(n);
    for (int i = 0; i < n; i++) {
      final boolean isCall = strikes[i] >= forward;
      funcs.add(getVolatilityAdjointFunction(new EuropeanVanillaOption(strikes[i], timeToExpiry, isCall), forward));
    }

    return new Function1D<T, double[][]>() {
      @Override
      public double[][] evaluate(final T data) {
        final double[][] res = new double[n][];
        for (int i = 0; i < n; i++) {
          res[i] = funcs.get(i).evaluate(data);
        }
        return res;
      }

    };
  }

  /**
   * Returns a function  that calculates the volatility sensitivity to model parameters for a range of strikes by means of
   * central finite difference - this should be overridden where possible
   * @param forward Forward value of underlying
   * @param strikes The strikes
   * @param timeToExpiry Time-toExpiry
   * @return  Matrix (i.e. double[][]) of volatility sensitivities to model parameters, where the columns are the model parameter
   *  sensitivities at each strike
   */
  public Function1D<T, double[][]> getModelAdjointFunction(final double forward, final double[] strikes, final double timeToExpiry) {

    final Function1D<T, double[]> func = getVolatilityFunction(forward, strikes, timeToExpiry);

    return new Function1D<T, double[][]>() {
      @SuppressWarnings("synthetic-access")
      @Override
      public double[][] evaluate(final T data) {
        Validate.notNull(data, "data");
        final double[][] temp = paramBarSet(func, data);

        //now transpose
        //TODO a transpose that works on double[][]?
        return MA.getTranspose(new DoubleMatrix2D(temp)).getData();
      }
    };
  }

  protected Function1D<T, double[][]> getModelAdjointFunctionByCallingSingleStrikes(final double forward, final double[] strikes, final double timeToExpiry) {

    final int n = strikes.length;
    final List<Function1D<T, double[]>> funcs = new ArrayList<>(n);
    for (int i = 0; i < n; i++) {
      final boolean isCall = strikes[i] >= forward;
      funcs.add(getModelAdjointFunction(new EuropeanVanillaOption(strikes[i], timeToExpiry, isCall), forward));
    }

    return new Function1D<T, double[][]>() {
      @Override
      public double[][] evaluate(final T data) {
        final double[][] res = new double[n][];
        for (int i = 0; i < n; i++) {
          res[i] = funcs.get(i).evaluate(data);
        }
        return res;
      }

    };
  }

  private double forwardBar(final EuropeanVanillaOption option, final double forward, final T data) {
    final Function1D<T, Double> funcUp = getVolatilityFunction(option, forward + EPS);
    final Function1D<T, Double> funcDown = getVolatilityFunction(option, forward - EPS);
    return (funcUp.evaluate(data) - funcDown.evaluate(data)) / 2 / EPS;
  }

  private double[] forwardBar(final double[] strikes, final double timeToExpiry, final double forward, final T data) {
    final int n = strikes.length;
    final Function1D<T, double[]> funcUp = getVolatilityFunction(forward + EPS, strikes, timeToExpiry);
    final Function1D<T, double[]> funcDown = getVolatilityFunction(forward - EPS, strikes, timeToExpiry);
    final double[] res = new double[n];
    final double[] up = funcUp.evaluate(data);
    final double[] down = funcDown.evaluate(data);
    for (int i = 0; i < n; i++) {
      res[i] = (up[i] - down[i]) / 2 / EPS;
    }
    return res;
  }

  private double strikeBar(final EuropeanVanillaOption option, final double forward, final T data) {
    final Function1D<T, Double> funcUp = getVolatilityFunction(option.withStrike(option.getStrike() + EPS), forward);
    final Function1D<T, Double> funcDown = getVolatilityFunction(option.withStrike(option.getStrike() - EPS), forward);
    return (funcUp.evaluate(data) - funcDown.evaluate(data)) / 2 / EPS;
  }

  private double[] strikeBar(final double[] strikes, final double timeToExpiry, final double forward, final T data) {
    final int n = strikes.length;
    final double[] res = new double[n];
    final double[] strikesUp = new double[n];
    final double[] strikesDown = new double[n];
    for (int i = 0; i < n; i++) {
      strikesUp[i] = strikes[i] + EPS;
    }
    for (int i = 0; i < n; i++) {
      strikesDown[i] = strikes[i] - EPS;
    }
    final Function1D<T, double[]> funcUp = getVolatilityFunction(forward, strikesUp, timeToExpiry);
    final Function1D<T, double[]> funcDown = getVolatilityFunction(forward, strikesDown, timeToExpiry);
    final double[] up = funcUp.evaluate(data);
    final double[] down = funcDown.evaluate(data);
    for (int i = 0; i < n; i++) {
      res[i] = (up[i] - down[i]) / 2 / EPS;
    }

    return res;
  }

  private double[] paramBar(final Function1D<T, Double> func, final T data) {
    final int n = data.getNumberOfParameters();
    final double[] res = new double[n];
    for (int i = 0; i < n; i++) {
      res[i] = paramBar(func, data, i);
    }
    return res;
  }

  /**
   * Get the model's sensitivity to its parameters by finite-difference, taking care on the boundary of allowed regions
   * @param func Function that gives the volatility for a set of model parameters
   * @param data Model parameters
   * @param index the index of the model parameter
   * @return The first derivative of the volatility WRT the parameter given by index
   */
  private double paramBar(final Function1D<T, Double> func, final T data, final int index) {
    final double mid = data.getParameter(index);
    final double up = mid + EPS;
    final double down = mid - EPS;
    if (data.isAllowed(index, down)) {
      if (data.isAllowed(index, up)) {
        final T dUp = (T) data.with(index, up);
        final T dDown = (T) data.with(index, down);
        return (func.evaluate(dUp) - func.evaluate(dDown)) / 2 / EPS;
      }
      final T dDown = (T) data.with(index, down);
      return (func.evaluate(data) - func.evaluate(dDown)) / EPS;
    }
    ArgumentChecker.isTrue(data.isAllowed(index, up), "No values and index {} = {} are allowed", index, mid);
    final T dUp = (T) data.with(index, up);
    return (func.evaluate(dUp) - func.evaluate(data)) / EPS;
  }

  private double[][] paramBarSet(final Function1D<T, double[]> func, final T data) {
    final int n = data.getNumberOfParameters();
    final double[][] res = new double[n][];
    for (int i = 0; i < n; i++) {
      res[i] = paramBarSet(func, data, i);
    }
    return res;
  }

  /**
   * Get the model's sensitivity to its parameters by finite-difference, taking care on the boundary of allowed regions
   * @param func Function that gives the volatility for a set of model parameters
   * @param data Model parameters
   * @param index the index of the model parameter
   * @return The first derivative of the volatility WRT the parameter given by index
   */
  private double[] paramBarSet(final Function1D<T, double[]> func, final T data, final int index) {
    final double mid = data.getParameter(index);
    final double up = mid + EPS;
    final double down = mid - EPS;
    if (data.isAllowed(index, down)) {
      if (data.isAllowed(index, up)) {
        final T dUp = (T) data.with(index, up);
        final T dDown = (T) data.with(index, down);
        final double[] rUp = func.evaluate(dUp);
        final double[] rDown = func.evaluate(dDown);
        final int m = rUp.length;
        final double[] res = new double[m];
        for (int i = 0; i < m; i++) {
          res[i] = (rUp[i] - rDown[i]) / 2 / EPS;
        }
        return res;
      }
      final double[] rMid = func.evaluate(data);
      final double[] rDown = func.evaluate((T) data.with(index, down));
      final int m = rMid.length;
      final double[] res = new double[m];
      for (int i = 0; i < m; i++) {
        res[i] = (rMid[i] - rDown[i]) / 2 / EPS;
      }
      return res;
    }
    ArgumentChecker.isTrue(data.isAllowed(index, up), "No values and index {} = {} are allowed", index, mid);
    final double[] rMid = func.evaluate(data);
    final double[] rUp = func.evaluate((T) data.with(index, up));
    final int m = rMid.length;
    final double[] res = new double[m];
    for (int i = 0; i < m; i++) {
      res[i] = (rUp[i] - rMid[i]) / 2 / EPS;
    }
    return res;
  }

}
