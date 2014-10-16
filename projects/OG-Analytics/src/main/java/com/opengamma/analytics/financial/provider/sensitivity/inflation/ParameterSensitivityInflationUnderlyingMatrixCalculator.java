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
import com.opengamma.analytics.financial.provider.description.inflation.ParameterInflationProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.ForwardSensitivity;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.util.ArgumentChecker;
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
  public ParameterSensitivityInflationUnderlyingMatrixCalculator(final InstrumentDerivativeVisitor<ParameterInflationProviderInterface, InflationSensitivity> curveSensitivityCalculator) {
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
    // TODO: The first part depends only of the inflationprovider and curvesSet, not the sensitivity. Should it be refactored and done only once?
    final Set<String> curveNamesSet = inflation.getAllCurveNames(); // curvesSet; //
    // Implementation note: Check sensicurve are in multicurve
    ArgumentChecker.isTrue(curveNamesSet.containsAll(curvesSet), "curve in the names set not in the multi-curve provider");
    final int nbCurve = curveNamesSet.size();
    // Populate the name names and numbers for the curves in the multicurve
    int loopname = 0;
    final LinkedHashMap<String, Integer> curveNum = new LinkedHashMap<>();
    for (final String name : curveNamesSet) { // loop over all curves (by name)
      curveNum.put(name, loopname++);
    }
    final int[] nbNewParameters = new int[nbCurve];
    final int[] nbParameters = new int[nbCurve];
    // Implementation note: nbNewParameters - number of new parameters in the curve, parameters not from an underlying curve which is another curve of the bundle.
    loopname = 0;
    for (final String name : curveNamesSet) { // loop over all curves (by name)
      nbParameters[loopname] = inflation.getNumberOfParameters(name);
      nbNewParameters[loopname] = nbParameters[loopname];
      loopname++;
    }
    loopname = 0;
    for (final String name : curveNamesSet) { // loop over all curves (by name)
      final List<String> underlyingCurveNames = inflation.getUnderlyingCurvesNames(name);
      for (final String u : underlyingCurveNames) {
        final Integer i = curveNum.get(u);
        if (i != null) {
          nbNewParameters[loopname] -= nbNewParameters[i]; // Only one level: a curve used as an underlying can not have an underlying itself.
        }
      }
      loopname++;
    }
    // Implementation note: nbNewParamSensiCurve
    final int[][] indexOtherMulticurve = new int[nbCurve][];
    // Implementation note: indexOtherMultiCurve - for each curve in the multi-curve, the index of the underlying curves in the same set
    final int[] startOwnParameter = new int[nbCurve];
    // Implementation note: The start index of the parameters of the own (new) parameters.
    final int[][] startUnderlyingParameter = new int[nbCurve][];
    // Implementation note: The start index of the parameters of the underlying curves
    loopname = 0;
    for (final String name : curveNamesSet) { // loop over all curves in multi-curve (by name)
      final List<String> underlyingCurveNames = inflation.getUnderlyingCurvesNames(name);
      final IntArrayList indexOtherMulticurveList = new IntArrayList();
      for (final String u : underlyingCurveNames) {
        final Integer i = curveNum.get(u);
        if (i != null) {
          indexOtherMulticurveList.add(i);
        }
      }
      indexOtherMulticurve[loopname] = indexOtherMulticurveList.toIntArray();
      loopname++;
    }
    for (final String name : curvesSet) { // loop over all curves (by name)
      int loopstart = 0;
      final int num = curveNum.get(name);
      final IntArrayList startUnderlyingParamList = new IntArrayList();
      final List<String> underlyingCurveNames = inflation.getUnderlyingCurvesNames(name);
      for (final String u : underlyingCurveNames) {
        final Integer i = curveNum.get(u);
        if (i != null) {
          startUnderlyingParamList.add(loopstart);
          loopstart += nbNewParameters[i];
        }
      }
      startOwnParameter[num] = loopstart;
      startUnderlyingParameter[num] = startUnderlyingParamList.toIntArray();
    }
    // Implementation note: Compute the "dirty" sensitivity, i.e. the sensitivity to all the parameters in each curve. The underlying are taken into account in the "clean" step.
    final double[][] sensiDirty = new double[nbCurve][];
    final Map<String, List<DoublesPair>> sensitivityDiscountAndPriceIndex = sensitivity.getDiscountAndPriceIndexSensitivities();
    final Map<String, List<ForwardSensitivity>> sensitivityFwd = sensitivity.getForwardSensitivities();
    for (final String name : curvesSet) { // loop over all curves (by name)
      final int num = curveNum.get(name);
      sensiDirty[num] = new double[nbParameters[num]];
      final double[] sDsc1Name = inflation.parameterInflationSensitivity(name, sensitivityDiscountAndPriceIndex.get(name));
      final double[] sFwd1Name = inflation.parameterForwardSensitivity(name, sensitivityFwd.get(name));
      for (int loopp = 0; loopp < nbParameters[num]; loopp++) {
        sensiDirty[num][loopp] = sDsc1Name[loopp] + sFwd1Name[loopp];
      }
    }
    // Implementation note: "clean" the sensitivity, i.e. add the underlying curve parts.
    final double[][] sensiClean = new double[nbCurve][];
    for (int loopc = 0; loopc < nbCurve; loopc++) {
      sensiClean[loopc] = new double[nbNewParameters[loopc]];
    }
    for (final String name : curveNamesSet) { // loop over all curves (by name)
      final int num = curveNum.get(name);
      // Direct sensitivity
      for (int loopi = 0; loopi < nbNewParameters[num]; loopi++) {
        sensiClean[num][loopi] += sensiDirty[num][startOwnParameter[num] + loopi];
      }
      // Underlying (indirect) sensitivity
      for (int loopu = 0; loopu < startUnderlyingParameter[num].length; loopu++) {
        for (int loopi = 0; loopi < nbNewParameters[indexOtherMulticurve[num][loopu]]; loopi++) {
          sensiClean[indexOtherMulticurve[num][loopu]][loopi] += sensiDirty[num][startUnderlyingParameter[num][loopu] + loopi];
        }
      }
    }
    double[] result = new double[0];
    for (final String name : curvesSet) {
      final int num = curveNum.get(name);
      result = ArrayUtils.addAll(result, sensiClean[num]);
    }
    return new DoubleMatrix1D(result);
  }

}
