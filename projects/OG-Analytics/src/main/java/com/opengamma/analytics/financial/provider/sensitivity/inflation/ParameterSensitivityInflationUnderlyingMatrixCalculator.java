/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.sensitivity.inflation;

import it.unimi.dsi.fastutil.ints.IntArrayList;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.ArrayUtils;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.provider.description.inflation.InflationProviderInterface;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.util.tuple.DoublesPair;

/**
 * For an instrument, computes the sensitivity of a value (often the present value or a par spread) to the parameters used in the curve.
 * The meaning of "parameters" will depend of the way the curve is stored (interpolated yield, function parameters, etc.).
 * The return format is ParameterSensitivity object.
 */
public class ParameterSensitivityInflationUnderlyingMatrixCalculator extends ParameterSensitivityInflationMatrixProviderAbstractCalculator {

  /**
   * Constructor
   * @param curveSensitivityCalculator The curve sensitivity calculator.
   */
  public ParameterSensitivityInflationUnderlyingMatrixCalculator(final InstrumentDerivativeVisitor<InflationProviderInterface, InflationSensitivity> curveSensitivityCalculator) {
    super(curveSensitivityCalculator);
  }

  /**
   * Computes the sensitivity with respect to the parameters from the point sensitivities to the continuously compounded rate.
   * @param sensitivity The point sensitivity.
   * @param inflation The inflation provider. Not null.
   * @param curvesSet The set of curves for which the sensitivity will be computed. Not null.
   * @return The sensitivity (as a ParameterSensitivity). ??The order of the sensitivity is by curve as provided by the curvesSet??
   */
  @Override
  public DoubleMatrix1D pointToParameterSensitivity(final InflationSensitivity sensitivity, final InflationProviderInterface inflation, final Set<String> curvesSet) {
    // TODO: The first part depends only of the multicurves and curvesSet, not the sensitivity. Should it be refactored and done only once?
    final Set<String> curveNamesSet = inflation.getAllNames(); // curvesSet; //
    final int nbCurve = curveNamesSet.size();
    final String[] curveNamesArray = new String[nbCurve];
    int loopname = 0;
    final LinkedHashMap<String, Integer> curveNum = new LinkedHashMap<>();
    for (final String name : curveNamesSet) { // loop over all curves (by name)
      curveNamesArray[loopname] = name;
      curveNum.put(name, loopname++);
    }
    final int[] nbNewParameters = new int[nbCurve];
    // Implementation note: nbNewParameters - number of new parameters in the curve, parameters not from an underlying curve which is another curve of the bundle.
    final int[][] indexOther = new int[nbCurve][];
    // Implementation note: indexOther - the index of the underlying curves, if any.
    loopname = 0;
    for (final String name : curveNamesSet) { // loop over all curves (by name)
      nbNewParameters[loopname] = inflation.getNumberOfParameters(name);
      loopname++;
    }
    loopname = 0;
    for (final String name : curveNamesSet) { // loop over all curves (by name)
      final List<String> underlyingCurveNames = inflation.getUnderlyingCurvesNames(name);
      final IntArrayList indexOtherList = new IntArrayList();
      for (final String u : underlyingCurveNames) {
        final Integer i = curveNum.get(u);
        if (i != null) {
          indexOtherList.add(i);
          nbNewParameters[loopname] -= nbNewParameters[i]; // Only one level: a curve used as an underlying can not have an underlying itself.
        }
      }
      indexOther[loopname] = indexOtherList.toIntArray();
      loopname++;
    }
    final int nbSensiCurve = curvesSet.size();
    //    for (final String name : curveNamesSet) { // loop over all curves (by name)
    //      if (curvesSet.contains(name)) {
    //        nbSensiCurve++;
    //      }
    //    }
    final int[] nbNewParamSensiCurve = new int[nbSensiCurve];
    // Implementation note: nbNewParamSensiCurve
    final int[][] indexOtherSensiCurve = new int[nbSensiCurve][];
    // Implementation note: indexOtherSensiCurve -
    final int[] startCleanParameter = new int[nbSensiCurve];
    // Implementation note: startCleanParameter - for each curve for which the sensitivity should be computed, the index in the total sensitivity vector at which that curve start.
    final int[][] startDirtyParameter = new int[nbSensiCurve][];
    // Implementation note: startDirtyParameter - for each curve for which the sensitivity should be computed, the indexes of the underlying curves.
    int nbSensitivityCurve = 0;
    int nbCleanParameters = 0;
    int currentDirtyStart = 0;
    for (final String name : curvesSet) { // loop over all curves (by name)
      //      if (curvesSet.contains(name)) {
      final int num = curveNum.get(name);
      final IntArrayList startDirtyParameterList = new IntArrayList();
      final List<String> underlyingCurveNames = inflation.getUnderlyingCurvesNames(name);
      for (final String u : underlyingCurveNames) {
        final Integer i = curveNum.get(u);
        if (i != null) {
          startDirtyParameterList.add(currentDirtyStart);
          currentDirtyStart += nbNewParameters[i];
        }
      }
      startDirtyParameterList.add(currentDirtyStart);
      currentDirtyStart += nbNewParameters[num];
      startDirtyParameter[nbSensitivityCurve] = startDirtyParameterList.toIntArray();
      nbNewParamSensiCurve[nbSensitivityCurve] = nbNewParameters[num];
      indexOtherSensiCurve[nbSensitivityCurve] = indexOther[num];
      startCleanParameter[nbSensitivityCurve] = nbCleanParameters;
      nbCleanParameters += nbNewParamSensiCurve[nbSensitivityCurve];
      nbSensitivityCurve++;
      //      }
    }
    // Implementation note: Compute the "dirty" sensitivity, i.e. the sensitivity where the underlying curves are not taken into account.
    double[] sensiDirty = new double[0];
    final Map<String, List<DoublesPair>> sensitivityPriceIndex = sensitivity.getPriceCurveSensitivities();
    for (final String name : curvesSet) { // loop over all curves (by name)
      //      if (curvesSet.contains(name)) {
      final int nbParam = inflation.getNumberOfParameters(name);
      final double[] s1Name = new double[nbParam];
      final double[] sDsc1Name = inflation.parameterInflationSensitivity(name, sensitivityPriceIndex.get(name));

      //        if ((sDsc1Name != null) && (sFwd1Name == null)) {
      //          s1Name = sDsc1Name;
      //        }
      //        if ((sDsc1Name == null) && (sFwd1Name != null)) {
      //          s1Name = sFwd1Name;
      //        }
      //        if ((sDsc1Name != null) && (sFwd1Name != null)) {
      for (int loopp = 0; loopp < nbParam; loopp++) {
        s1Name[loopp] = sDsc1Name[loopp];
      }
      //        }
      sensiDirty = ArrayUtils.addAll(sensiDirty, s1Name);
      //      }
    }
    // Implementation note: "clean" the sensitivity, i.e. add the underlying curve parts.
    final double[] sensiClean = new double[nbCleanParameters];
    for (int loopcurve = 0; loopcurve < nbSensiCurve; loopcurve++) {
      for (int loopo = 0; loopo < indexOtherSensiCurve[loopcurve].length; loopo++) {
        if (curvesSet.contains(curveNamesArray[indexOtherSensiCurve[loopcurve][loopo]])) {
          for (int loops = 0; loops < nbNewParamSensiCurve[indexOtherSensiCurve[loopcurve][loopo]]; loops++) {
            sensiClean[startCleanParameter[indexOtherSensiCurve[loopcurve][loopo]] + loops] += sensiDirty[startDirtyParameter[loopcurve][loopo] + loops];
          }
        }
      }
      for (int loops = 0; loops < nbNewParamSensiCurve[loopcurve]; loops++) {
        sensiClean[startCleanParameter[loopcurve] + loops] += sensiDirty[startDirtyParameter[loopcurve][indexOtherSensiCurve[loopcurve].length] + loops];
      }
    }
    return new DoubleMatrix1D(sensiClean);
  }

}
