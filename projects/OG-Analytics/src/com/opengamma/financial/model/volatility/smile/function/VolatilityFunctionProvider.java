/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.model.volatility.smile.function;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.matrix.DoubleMatrix2D;
import com.opengamma.math.matrix.MatrixAlgebra;
import com.opengamma.math.matrix.OGMatrixAlgebra;

/**
 * 
 * @param <T> Type of the data needed for the volatility function
 */
public abstract class VolatilityFunctionProvider<T extends SmileModelData> {

  private static final MatrixAlgebra MA = new OGMatrixAlgebra();

  private static final double EPS = 1e-6;

  // public abstract int getNumberOfParameters();

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
  public Function1D<T, double[]> getVolatilitySetFunction(double forward, double[] strikes, double timeToExpiry) {

    final int n = strikes.length;
    final List<Function1D<T, Double>> funcs = new ArrayList<Function1D<T, Double>>(n);
    for (int i = 0; i < n; i++) {
      funcs.add(getVolatilityFunction(new EuropeanVanillaOption(strikes[i], timeToExpiry, true), forward));
    }

    return new Function1D<T, double[]>() {
      @Override
      public double[] evaluate(T data) {
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
      @Override
      public final double[] evaluate(final T data) {
        Validate.notNull(data, "data");
        double[] x = new double[3 + data.getNumberOfparameters()]; //vol, fwd, strike, the model parameters      
        x[0] = func.evaluate(data);
        x[1] = forwardBar(option, forward, data);
        x[2] = strikeBar(option, forward, data);
        System.arraycopy(paramBar(func, data), 0, x, 3, data.getNumberOfparameters());
        return x;
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
  public Function1D<T, double[][]> getVolatilityAdjointSetFunction(final double forward, final double[] strikes, final double timeToExpiry) {

    final int n = strikes.length;
    final Function1D<T, double[]> func = getVolatilitySetFunction(forward, strikes, timeToExpiry);

    return new Function1D<T, double[][]>() {
      @Override
      public double[][] evaluate(T data) {
        Validate.notNull(data, "data");
        double[][] res = new double[3 + data.getNumberOfparameters()][n];
        res[0] = func.evaluate(data);
        res[1] = forwardBar(strikes, timeToExpiry, forward, data);
        res[2] = strikeBar(strikes, timeToExpiry, forward, data);
        double[][] temp = paramBarSet(func, data);
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

  protected Function1D<T, double[][]> getVolatilityAdjointSetFunctionAlt(double forward, double[] strikes, double timeToExpiry) {

    final int n = strikes.length;
    final List<Function1D<T, double[]>> funcs = new ArrayList<Function1D<T, double[]>>(n);
    for (int i = 0; i < n; i++) {
      funcs.add(getVolatilityAdjointFunction(new EuropeanVanillaOption(strikes[i], timeToExpiry, true), forward));
    }

    return new Function1D<T, double[][]>() {
      @Override
      public double[][] evaluate(T data) {
        final double[][] res = new double[n][];
        for (int i = 0; i < n; i++) {
          res[i] = funcs.get(i).evaluate(data);
        }
        return res;
      }

    };
  }

  private double forwardBar(final EuropeanVanillaOption option, final double forward, T data) {
    Function1D<T, Double> funcUp = getVolatilityFunction(option, forward + EPS);
    Function1D<T, Double> funcDown = getVolatilityFunction(option, forward - EPS);
    return (funcUp.evaluate(data) - funcDown.evaluate(data)) / 2 / EPS;
  }

  private double[] forwardBar(double[] strikes, double timeToExpiry, double forward, T data) {
    final int n = strikes.length;
    Function1D<T, double[]> funcUp = getVolatilitySetFunction(forward + EPS, strikes, timeToExpiry);
    Function1D<T, double[]> funcDown = getVolatilitySetFunction(forward - EPS, strikes, timeToExpiry);
    final double[] res = new double[n];
    final double[] up = funcUp.evaluate(data);
    final double[] down = funcDown.evaluate(data);
    for (int i = 0; i < n; i++) {
      res[i] = (up[i] - down[i]) / 2 / EPS;
    }
    return res;
  }

  private double strikeBar(final EuropeanVanillaOption option, final double forward, T data) {
    Function1D<T, Double> funcUp = getVolatilityFunction(option.withStrike(option.getStrike() + EPS), forward);
    Function1D<T, Double> funcDown = getVolatilityFunction(option.withStrike(option.getStrike() - EPS), forward);
    return (funcUp.evaluate(data) - funcDown.evaluate(data)) / 2 / EPS;
  }

  private double[] strikeBar(double[] strikes, double timeToExpiry, double forward, T data) {
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
    Function1D<T, double[]> funcUp = getVolatilitySetFunction(forward, strikesUp, timeToExpiry);
    Function1D<T, double[]> funcDown = getVolatilitySetFunction(forward, strikesDown, timeToExpiry);
    final double[] up = funcUp.evaluate(data);
    final double[] down = funcDown.evaluate(data);
    for (int i = 0; i < n; i++) {
      res[i] = (up[i] - down[i]) / 2 / EPS;
    }

    return res;
  }

  //TODO there is no guard against model parameter going outside allowed range 
  @SuppressWarnings("unchecked")
  private double[] paramBar(Function1D<T, Double> func, T data) {
    final int n = data.getNumberOfparameters();
    double[] res = new double[n];
    for (int i = 0; i < n; i++) {
      T dUp = (T) data.with(i, data.getParameter(i) + EPS);
      T dDown = (T) data.with(i, data.getParameter(i) - EPS);
      res[i] = (func.evaluate(dUp) - func.evaluate(dDown)) / 2 / EPS;
    }
    return res;
  }

  @SuppressWarnings("unchecked")
  private double[][] paramBarSet(Function1D<T, double[]> func, T data) {
    final int n = data.getNumberOfparameters();
    double[][] res = new double[n][];
    for (int i = 0; i < n; i++) {
      T dUp = (T) data.with(i, data.getParameter(i) + EPS);
      T dDown = (T) data.with(i, data.getParameter(i) - EPS);
      double[] up = func.evaluate(dUp);
      double[] down = func.evaluate(dDown);
      final int m = up.length;
      res[i] = new double[m];
      for (int j = 0; j < m; j++) {
        res[i][j] = (up[j] - down[j]) / 2 / EPS;
      }
    }
    return res;
  }
}
