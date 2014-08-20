/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.discrete;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang.ArrayUtils;

import com.opengamma.analytics.financial.model.interestrate.curve.ForwardCurve;
import com.opengamma.analytics.financial.model.volatility.smile.function.SmileModelData;
import com.opengamma.analytics.financial.model.volatility.smile.function.VolatilityFunctionProvider;
import com.opengamma.analytics.math.function.ConcatenatedVectorFunction;
import com.opengamma.analytics.math.function.DoublesVectorFunctionProvider;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.function.ParameterizedCurve;
import com.opengamma.analytics.math.function.ParameterizedCurveVectorFunctionProvider;
import com.opengamma.analytics.math.function.VectorFunction;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.analytics.math.matrix.MatrixAlgebra;
import com.opengamma.analytics.math.matrix.OGMatrixAlgebra;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.DoublesPair;

/**
 * The parameters of the smile model (e.g. SABR, Heston, SVI) are themselves represented as parameterised term structures
 * (one for each smile model parameter), and this collection of parameters are the <i>model parameters</i>. For a particular
 * expiry, we can find the smile model parameters, then (using the smile model) find the (Black) volatility across strikes
 * (i.e. the smile); hence the model parameters describe a volatility surface. <p>
 * Given a set of expiry-strike points, this provides a mapping (a {@link DiscreteVolatilityFunction}) between the model
 * parameters and the (Black) volatilities at the required points. 
 * @param <T> The type of smile model data 
 */
public abstract class ParameterizedSmileModelDiscreateVolatilityFunctionProvider<T extends SmileModelData> extends DiscreteVolatilityFunctionProvider {

  private static final MatrixAlgebra MA = new OGMatrixAlgebra();

  private final VolatilityFunctionProvider<T> _volFuncPro;
  private final ForwardCurve _fwdCurve;;
  private final DoublesVectorFunctionProvider[] _smileModelParameterProviders;

  // private final int _nParms;

  /**
   * Set up the {@link DiscreteVolatilityFunctionProvider} 
   * @param volFuncPro The smile model 
   * @param fwdCurve The forward curve 
   * @param smileModelParameterProviders each of these providers represents a different smile parameter - <b>there 
   * must be one for each smile model parameter</b>. Given a (common) set of expiries, each one provides a 
   * {@link VectorFunction} that gives the corresponding smile model parameter at each expiry for a set of model 
   * parameters. This gives a lot of flexibility as to how the (smile model) parameter term structures are represented. 
   */
  public ParameterizedSmileModelDiscreateVolatilityFunctionProvider(final VolatilityFunctionProvider<T> volFuncPro, final ForwardCurve fwdCurve,
      final DoublesVectorFunctionProvider[] smileModelParameterProviders) {

    //TODO change this to take a ParameterizedCurveVectorFunctionProvider so the sample expiry points do not need to 
    //be known in advance 
    ArgumentChecker.notNull(volFuncPro, "volFuncPro");
    ArgumentChecker.notNull(fwdCurve, "fwdCurve");
    ArgumentChecker.noNulls(smileModelParameterProviders, "modelToSmileModelParms");
    ArgumentChecker.isTrue(getNumSmileModelParamters() == smileModelParameterProviders.length, "Incorrect number of smileModelParameterProviders");
    _volFuncPro = volFuncPro;
    _fwdCurve = fwdCurve;
    _smileModelParameterProviders = smileModelParameterProviders;
  }

  /**
   * Set up the {@link DiscreteVolatilityFunctionProvider} 
   * @param volFuncPro The smile model 
   * @param fwdCurve The forward curve 
   * @param smileParameterTS each of these represents a different smile parameter term structure- <b>there 
   * must be one for each smile model parameter</b>. 
   */
  public ParameterizedSmileModelDiscreateVolatilityFunctionProvider(final VolatilityFunctionProvider<T> volFuncPro, final ForwardCurve fwdCurve, final ParameterizedCurve[] smileParameterTS) {
    ArgumentChecker.notNull(volFuncPro, "volFuncPro");
    ArgumentChecker.notNull(fwdCurve, "fwdCurve");
    ArgumentChecker.noNulls(smileParameterTS, "smileParameterTS");
    ArgumentChecker.isTrue(getNumSmileModelParamters() == smileParameterTS.length, "Incorrect number of smileParameterTS");

    DoublesVectorFunctionProvider[] vfp = new DoublesVectorFunctionProvider[getNumSmileModelParamters()];
    for (int i = 0; i < getNumSmileModelParamters(); i++) {
      vfp[i] = new ParameterizedCurveVectorFunctionProvider(smileParameterTS[i]);
    }

    _volFuncPro = volFuncPro;
    _fwdCurve = fwdCurve;
    _smileModelParameterProviders = vfp;
  }

  /**
   * {@inheritDoc}
   * <b>Note</b> The set of unique expiries in the expiryStrikePoints must match what is expected from the modelToSmileModelMap 
   * passed to the constructor
   */
  @Override
  public DiscreteVolatilityFunction from(final DoublesPair[] expiryStrikePoints) {
    ArgumentChecker.noNulls(expiryStrikePoints, "expiryStrikePoints");
    final int nSmileModelParms = getNumSmileModelParamters();
    final int nOptions = expiryStrikePoints.length;

    //get the (sorted) array of unique expiries 
    final Set<Double> expSet = new TreeSet<>();
    for (int i = 0; i < nOptions; i++) {
      final double t = expiryStrikePoints[i].first;
      expSet.add(t);
    }
    final double[] expiries = ArrayUtils.toPrimitive(expSet.toArray(new Double[0]));
    final int nExpiries = expiries.length;

    //create vectorFunctions that give the smile model parameters at the expiries and concatanate these to one function
    int n = _smileModelParameterProviders.length;
    VectorFunction[] funcs = new VectorFunction[n];
    for (int i = 0; i < n; i++) {
      funcs[i] = _smileModelParameterProviders[i].from(expiries);
    }
    final VectorFunction modelToSmileModelParms = new ConcatenatedVectorFunction(funcs);

    //for each expiry, get the (sorted) array of unique strikes 
    final double[][] strikes = new double[nExpiries][];
    final List<Set<Double>> strikeSets = new ArrayList<>(nExpiries);
    for (int i = 0; i < nExpiries; i++) {
      final Set<Double> s = new TreeSet<>();
      strikeSets.add(s);
    }

    final int[] optToExpMap = new int[nOptions];
    for (int i = 0; i < nOptions; i++) {
      final double t = expiryStrikePoints[i].first;
      final double k = expiryStrikePoints[i].second;
      final int index = Arrays.binarySearch(expiries, t);
      optToExpMap[i] = index;
      final Set<Double> s = strikeSets.get(index);
      if (!s.add(k)) {
        throw new IllegalArgumentException("This code should be unreachable");
      }
    }

    //find the reverse map to go from the (sorted) expiry and strike arrays to the positions in the input 
    //expiryStrikePoints array
    final int[][] revMap = new int[nExpiries][];
    for (int i = 0; i < nExpiries; i++) {
      final Set<Double> s = strikeSets.get(i);
      strikes[i] = ArrayUtils.toPrimitive(s.toArray(new Double[0]));
      final int nStrikes = strikes[i].length;
      revMap[i] = new int[nStrikes];
    }

    for (int i = 0; i < nOptions; i++) {
      final int tIndex = optToExpMap[i];
      final double[] strikesAtT = strikes[tIndex];
      final double k = expiryStrikePoints[i].second;
      final int kIndex = Arrays.binarySearch(strikesAtT, k);
      if (kIndex < 0) {
        throw new IllegalArgumentException("This code should be unreachable");
      }
      revMap[tIndex][kIndex] = i;
    }

    //this is a list of functions that map from model parameters (SmileModelData) to smiles (volatilities at fixed
    //strikes at particular expiry)
    final List<Function1D<T, double[]>> smiles = new ArrayList<>(nExpiries);
    final List<Function1D<T, double[][]>> smilesAdjoints = new ArrayList<>(nExpiries);
    for (int i = 0; i < nExpiries; i++) {
      final double fwd = _fwdCurve.getForward(expiries[i]);
      final Function1D<T, double[]> volFunc = _volFuncPro.getVolatilityFunction(fwd, strikes[i], expiries[i]);
      final Function1D<T, double[][]> adjointFunc = _volFuncPro.getModelAdjointFunction(fwd, strikes[i], expiries[i]);
      smiles.add(volFunc);
      smilesAdjoints.add(adjointFunc);
    }

    return new DiscreteVolatilityFunction() {

      @Override
      public DoubleMatrix2D calculateJacobian(final DoubleMatrix1D x) {

        final DoubleMatrix2D dSigmadTheta = new DoubleMatrix2D(nOptions, nExpiries * nSmileModelParms);
        final DoubleMatrix1D theta = modelToSmileModelParms.evaluate(x);
        final DoubleMatrix2D dThetadx = modelToSmileModelParms.calculateJacobian(x);

        final double[] parms = new double[nSmileModelParms];
        for (int i = 0; i < nExpiries; i++) {
          for (int j = 0; j < nSmileModelParms; j++) {
            parms[j] = theta.getEntry(i + j * nExpiries);
          }
          final T modelData = toSmileModelData(parms);
          final double[] sigmas = smiles.get(i).evaluate(modelData);
          //this is dSigma_dTheta for a single expiry 
          final double[][] temp = smilesAdjoints.get(i).evaluate(modelData);
          final int[] map = revMap[i];
          final int nStrikes = sigmas.length;
          for (int k = 0; k < nStrikes; k++) {
            final int firstIndex = map[k];
            for (int j = 0; j < nSmileModelParms; j++) {
              final int secondIndex = i + j * nExpiries;
              dSigmadTheta.getData()[firstIndex][secondIndex] = temp[k][j];
            }
          }
        }

        //these matrices are likely to be sparse, so this is wasteful without sparse matrix support 
        return (DoubleMatrix2D) MA.multiply(dSigmadTheta, dThetadx);
      }

      @Override
      public DoubleMatrix1D evaluate(final DoubleMatrix1D x) {

        final DoubleMatrix1D res = new DoubleMatrix1D(new double[nOptions]);

        // the vector theta are the smile model parameters, order as 1st parameter for all expiries, 2nd parameter for
        //all expiries, etc
        final DoubleMatrix1D theta = modelToSmileModelParms.evaluate(x);
        ArgumentChecker.isTrue(theta.getNumberOfElements() == nSmileModelParms * nExpiries, "Length of x ({}) inconsistent with number of smile model parameters ({}) and number of expiries ({})",
            theta.getNumberOfElements(), nSmileModelParms, nExpiries);

        final double[] parms = new double[nSmileModelParms];
        for (int i = 0; i < nExpiries; i++) {
          for (int j = 0; j < nSmileModelParms; j++) {
            parms[j] = theta.getEntry(i + j * nExpiries);
          }

          final double[] sigmas = smiles.get(i).evaluate(toSmileModelData(parms));
          // scatter smile in order expected by res;
          final int[] map = revMap[i];
          final int nStrikes = sigmas.length;
          for (int j = 0; j < nStrikes; j++) {
            res.getData()[map[j]] = sigmas[j];
          }
        }

        return res;
      }

      @Override
      public int getLengthOfDomain() {
        return modelToSmileModelParms.getLengthOfDomain();
      }

      @Override
      public int getLengthOfRange() {
        return nOptions;
      }
    };
  }

  /**
   * The number of smile model parameters 
   * @return number of smile model parameters 
   */
  public abstract int getNumSmileModelParamters();

  /**
   * Convert the modelParameter array to SmileModelData
   * @param modelParameters model parameters array
   * @return SmileModelData
   */
  protected abstract T toSmileModelData(final double[] modelParameters);

}
