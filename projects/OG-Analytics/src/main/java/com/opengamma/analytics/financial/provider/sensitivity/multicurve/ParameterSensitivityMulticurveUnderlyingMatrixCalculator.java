/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.sensitivity.multicurve;

import it.unimi.dsi.fastutil.ints.IntArrayList;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.ArrayUtils;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.DoublesPair;

/**
 * For an instrument, computes the sensitivity of a value (often the present value or a par spread) to the parameters used in the curve.
 * The meaning of "parameters" will depend of the way the curve is stored (interpolated yield, function parameters, etc.).
 * The return format is ParameterSensitivity object.
 */
public class ParameterSensitivityMulticurveUnderlyingMatrixCalculator extends ParameterSensitivityMulticurveMatrixAbstractCalculator {

  /**
   * Constructor
   * @param curveSensitivityCalculator The curve sensitivity calculator.
   */
  public ParameterSensitivityMulticurveUnderlyingMatrixCalculator(final InstrumentDerivativeVisitor<MulticurveProviderInterface, MulticurveSensitivity> curveSensitivityCalculator) {
    super(curveSensitivityCalculator);
  }

  /**
   * Computes the sensitivity with respect to the parameters from the point sensitivities to the continuously compounded rate.
   * @param sensitivity The point sensitivity.
   * @param multicurves The multi-curve provider. Not null.
   * @param sensicurveNamesSet The set of curves for which the sensitivity will be computed. Not null.
   * @return The sensitivity (as a ParameterSensitivity). The order of the sensitivity is by curve as provided by the sensicurveNamesSet.
   */
  @Override
  public DoubleMatrix1D pointToParameterSensitivity(final MulticurveSensitivity sensitivity, final MulticurveProviderInterface multicurves, final Set<String> sensicurveNamesSet) {
    // TODO: The first part depends only of the multicurves and curvesSet, not the sensitivity. Should it be refactored and done only once?
    final Set<String> multicurveNamesSet = multicurves.getAllNames();
    // Implementation note: Check sensicurve are in multicurve
    ArgumentChecker.isTrue(multicurveNamesSet.containsAll(sensicurveNamesSet), "curve in the names set not in the multi-curve provider");
    final int nbMultiCurve = multicurveNamesSet.size();
    // Populate the name names and numbers for the curves in the multicurve
    int loopname = 0;
    final LinkedHashMap<String, Integer> multicurveNum = new LinkedHashMap<>();
    for (final String name : multicurveNamesSet) { // loop over all curves in multicurves (by name)
      multicurveNum.put(name, loopname++);
    }
    final int[] nbNewParameters = new int[nbMultiCurve];
    final int[] nbParameters = new int[nbMultiCurve];
    // Implementation note: nbNewParameters - number of new parameters in the curve, parameters not from an underlying curve which is another curve of the bundle.
    loopname = 0;
    for (final String name : multicurveNamesSet) { // loop over all curves in multicurves (by name)
      nbParameters[loopname] = multicurves.getNumberOfParameters(name);
      nbNewParameters[loopname] = nbParameters[loopname];
      loopname++;
    }
    loopname = 0;
    for (final String name : multicurveNamesSet) { // loop over all curves in multicurves (by name)
      final List<String> underlyingCurveNames = multicurves.getUnderlyingCurvesNames(name);
      for (final String u : underlyingCurveNames) {
        final Integer i = multicurveNum.get(u);
        if (i != null) {
          nbNewParameters[loopname] -= nbNewParameters[i]; // Only one level: a curve used as an underlying can not have an underlying itself.
        }
      }
      loopname++;
    }
    // Implementation note: nbNewParamSensiCurve
    final int[][] indexOtherMulticurve = new int[nbMultiCurve][];
    // Implementation note: indexOtherMultiCurve - for each curve in the multi-curve, the index of the underlying curves in the same set
    final int[] startOwnParameter = new int[nbMultiCurve];
    // Implementation note: The start index of the parameters of the own (new) parameters.
    final int[][] startUnderlyingParameter = new int[nbMultiCurve][];
    // Implementation note: The start index of the parameters of the underlying curves
    loopname = 0;
    for (final String name : multicurveNamesSet) { // loop over all curves in multi-curve (by name)
      final List<String> underlyingCurveNames = multicurves.getUnderlyingCurvesNames(name);
      final IntArrayList indexOtherMulticurveList = new IntArrayList();
      for (final String u : underlyingCurveNames) {
        final Integer i = multicurveNum.get(u);
        if (i != null) {
          indexOtherMulticurveList.add(i);
        }
      }
      indexOtherMulticurve[loopname] = indexOtherMulticurveList.toIntArray();
      loopname++;
    }
    for (final String name : multicurveNamesSet) { // loop over all curves (by name)
      int loopstart = 0;
      final int num = multicurveNum.get(name);
      final IntArrayList startUnderlyingParamList = new IntArrayList();
      final List<String> underlyingCurveNames = multicurves.getUnderlyingCurvesNames(name);
      for (final String u : underlyingCurveNames) {
        final Integer i = multicurveNum.get(u);
        if (i != null) {
          startUnderlyingParamList.add(loopstart);
          loopstart += nbNewParameters[i]; // Implementation note: Rely on underlying curves being first and then the new parameters
        }
      }
      startOwnParameter[num] = loopstart;
      startUnderlyingParameter[num] = startUnderlyingParamList.toIntArray();
    }
    // Implementation note: Compute the "dirty" sensitivity, i.e. the sensitivity to all the parameters in each curve. The underlying are taken into account in the "clean" step.
    double[][] sensiDirty = new double[nbMultiCurve][];
    final Map<String, List<DoublesPair>> sensitivityDsc = sensitivity.getYieldDiscountingSensitivities();
    final Map<String, List<ForwardSensitivity>> sensitivityFwd = sensitivity.getForwardSensitivities();
    for (final String name : multicurveNamesSet) { // loop over all curves (by name)
      final int num = multicurveNum.get(name);
      sensiDirty[num] = new double[nbParameters[num]];
      final double[] sDsc1Name = multicurves.parameterSensitivity(name, sensitivityDsc.get(name));
      final double[] sFwd1Name = multicurves.parameterForwardSensitivity(name, sensitivityFwd.get(name));
      for (int loopp = 0; loopp < nbParameters[num]; loopp++) {
        sensiDirty[num][loopp] = sDsc1Name[loopp] + sFwd1Name[loopp];
      }
    }
    // Implementation note: "clean" the sensitivity, i.e. add the parts on the same curves together.
    double[][] sensiClean = new double[nbMultiCurve][];
    for (int loopc = 0; loopc < nbMultiCurve; loopc++) {
      sensiClean[loopc] = new double[nbNewParameters[loopc]];
    }
    for (final String name : multicurveNamesSet) { // loop over all curves (by name)
      final int num = multicurveNum.get(name);
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
    for (String name : sensicurveNamesSet) {
      final int num = multicurveNum.get(name);
      result = ArrayUtils.addAll(result, sensiClean[num]);
    }
    return new DoubleMatrix1D(result);
  }

}
