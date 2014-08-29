/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.discrete;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
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
import com.opengamma.util.tuple.FirstThenSecondDoublesPairComparator;

/**
 * The parameters of the smile model (e.g. SABR, Heston, SVI) are themselves represented as parameterised term structures
 * (one for each smile model parameter), and this collection of parameters are the <i>model parameters</i>. For a particular
 * expiry, we can find the smile model parameters, then (using the smile model) find the (Black) volatility across strikes
 * (i.e. the smile); hence the model parameters describe a volatility surface. <p>
 * Given a set of expiry-strike points, this provides a mapping (a {@link DiscreteVolatilityFunction}) between the model
 * parameters and the (Black) volatilities at the required points. 
 * @param <T> The type of smile model data 
 */
public abstract class ParameterizedSmileModelDiscreteVolatilityFunctionProvider<T extends SmileModelData> extends DiscreteVolatilityFunctionProvider {

  private static final MatrixAlgebra MA = new OGMatrixAlgebra();

  private final VolatilityFunctionProvider<T> _volFuncPro;
  private final ForwardCurve _fwdCurve;;
  private final DoublesVectorFunctionProvider[] _smileModelParameterProviders;

  /**
   * Set up the {@link DiscreteVolatilityFunctionProvider} 
   * @param volFuncPro The smile model 
   * @param fwdCurve The forward curve 
   * @param smileModelParameterProviders each of these providers represents a different smile parameter - <b>there 
   * must be one for each smile model parameter</b>. Given a (common) set of expiries, each one provides a 
   * {@link VectorFunction} that gives the corresponding smile model parameter at each expiry for a set of model 
   * parameters. This gives a lot of flexibility as to how the (smile model) parameter term structures are represented. 
   */
  public ParameterizedSmileModelDiscreteVolatilityFunctionProvider(final VolatilityFunctionProvider<T> volFuncPro, final ForwardCurve fwdCurve,
      final DoublesVectorFunctionProvider[] smileModelParameterProviders) {
    ArgumentChecker.notNull(volFuncPro, "volFuncPro");
    ArgumentChecker.notNull(fwdCurve, "fwdCurve");
    ArgumentChecker.noNulls(smileModelParameterProviders, "modelToSmileModelParms");
    ArgumentChecker.isTrue(getNumSmileModelParameters() == smileModelParameterProviders.length, "Incorrect number of smileModelParameterProviders");
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
  public ParameterizedSmileModelDiscreteVolatilityFunctionProvider(final VolatilityFunctionProvider<T> volFuncPro, final ForwardCurve fwdCurve, final ParameterizedCurve[] smileParameterTS) {
    ArgumentChecker.notNull(volFuncPro, "volFuncPro");
    ArgumentChecker.notNull(fwdCurve, "fwdCurve");
    ArgumentChecker.noNulls(smileParameterTS, "smileParameterTS");
    ArgumentChecker.isTrue(getNumSmileModelParameters() == smileParameterTS.length, "Incorrect number of smileParameterTS");

    DoublesVectorFunctionProvider[] vfp = new DoublesVectorFunctionProvider[getNumSmileModelParameters()];
    for (int i = 0; i < getNumSmileModelParameters(); i++) {
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
    final int nOptions = expiryStrikePoints.length;
    ArgumentChecker.isTrue(nOptions > 0, "Require at least one expiryStrikePoints");
    final int nSmileModelParms = getNumSmileModelParameters();

    //order expiryStrikePoints first by expiry then strike and ensure they are unique  
    //Also get the (sorted) array of unique expiries 
    Set<DoublesPair> expStrikeSet = new TreeSet<>(new FirstThenSecondDoublesPairComparator());
    final Set<Double> expSet = new TreeSet<>();
    for (DoublesPair point : expiryStrikePoints) {
      double t = point.first;
      double k = point.second;
      if (!expStrikeSet.add(point)) {
        throw new IllegalArgumentException("expiryStrikePoints are not unique. Point with expiry of " + t + " and a strike of " + k + " already present");
      }
      expSet.add(t);
    }
    final double[] expiries = ArrayUtils.toPrimitive(expSet.toArray(new Double[0]));
    final int nExpiries = expiries.length;

    //for each expiry, get the (sorted) array of unique strikes by walking the expStrikeSet (which is sorted first by expiry then strike)
    final double[][] strikes = new double[nExpiries][];
    Iterator<DoublesPair> iter = expStrikeSet.iterator();
    int expIndex = 0;
    DoublesPair p = iter.next(); //set must have at least one entry 
    double t0 = p.first;
    List<Double> strikeList = new ArrayList<>();
    strikeList.add(p.second);
    while (iter.hasNext()) {
      p = iter.next();
      if (p.first > t0) {
        strikes[expIndex++] = ArrayUtils.toPrimitive(strikeList.toArray(new Double[0]));
        t0 = p.first;
        strikeList = new ArrayList<>();
      }
      strikeList.add(p.second);
    }
    strikes[expIndex++] = ArrayUtils.toPrimitive(strikeList.toArray(new Double[0]));

    //find the reverse map to go from the (sorted) expiry and strike arrays to the positions in the input 
    //expiryStrikePoints array - this allows the output vols from evaluate (and the vol sensitivities from calculateJacobian)
    //to be in the same order as the input expiryStrikePoints
    final int[][] revMap = new int[nExpiries][];
    for (int i = 0; i < nExpiries; i++) {
      revMap[i] = new int[strikes[i].length];
    }
    int index = 0;
    for (DoublesPair point : expiryStrikePoints) {
      double t = point.first;
      double k = point.second;
      int tIndex = Arrays.binarySearch(expiries, t);
      int kIndex = Arrays.binarySearch(strikes[tIndex], k);
      revMap[tIndex][kIndex] = index++;
    }

    //create vectorFunctions that give the smile model parameters at the expiries and concatenate these to one function
    VectorFunction[] funcs = new VectorFunction[nSmileModelParms];
    for (int i = 0; i < nSmileModelParms; i++) {
      funcs[i] = _smileModelParameterProviders[i].from(expiries);
    }
    final VectorFunction modelToSmileModelParms = new ConcatenatedVectorFunction(funcs);

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
        // the vector theta are the smile model parameters, order as 1st parameter for all expiries, 2nd parameter for
        //all expiries, etc
        //x is checked in this call
        final DoubleMatrix1D theta = modelToSmileModelParms.evaluate(x);

        final DoubleMatrix1D res = new DoubleMatrix1D(new double[nOptions]);
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
  public abstract int getNumSmileModelParameters();

  /**
   * Convert the modelParameter array to SmileModelData
   * @param modelParameters model parameters array
   * @return SmileModelData
   */
  protected abstract T toSmileModelData(final double[] modelParameters);

}
