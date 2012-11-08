/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.sensitivity;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.ArrayUtils;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.provider.description.MulticurveProviderInterface;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.util.tuple.DoublesPair;

/**
 * For an instrument, computes the sensitivity of a value (often the present value or a par spread) to the parameters used in the curve.
 * The meaning of "parameters" will depend of the way the curve is stored (interpolated yield, function parameters, etc.).
 * The return format is ParameterSensitivity object.
 */
public class ParameterSensitivityMatrixMulticurveUnderlyingCalculator extends AbstractParameterSensitivityMatrixMulticurveProviderCalculator {

  /**
   * Constructor
   * @param curveSensitivityCalculator The curve sensitivity calculator.
   */
  public ParameterSensitivityMatrixMulticurveUnderlyingCalculator(InstrumentDerivativeVisitor<MulticurveProviderInterface, MulticurveSensitivity> curveSensitivityCalculator) {
    super(curveSensitivityCalculator);
  }

  /**
   * Computes the sensitivity with respect to the parameters from the point sensitivities to the continuously compounded rate.
   * @param sensitivity The point sensitivity.
   * @param multicurves The multi-curve provider. Not null.
   * @param curvesSet The set of curves for which the sensitivity will be computed. Not null.
   * @return The sensitivity (as a ParameterSensitivity).
   */
  @Override
  public DoubleMatrix1D pointToParameterSensitivity(final MulticurveSensitivity sensitivity, final MulticurveProviderInterface multicurves, final Set<String> curvesSet) {
    // TODO: take into account the fact that there are now Dsc and Fwd sensi.
    Set<String> curveNamesSet = multicurves.getAllNames();
    int nbCurve = curveNamesSet.size();
    String[] curveNamesArray = new String[nbCurve];
    int loopname = 0;
    LinkedHashMap<String, Integer> curveNum = new LinkedHashMap<String, Integer>();
    for (final String name : curveNamesSet) { // loop over all curves (by name)
      curveNamesArray[loopname] = name;
      curveNum.put(name, loopname++);
    }
    int[] nbNewParameters = new int[nbCurve];
    // Implementation note: nbNewParameters - number of new parameters in the curve, parameters not from an underlying curve which is another curve of the bundle.
    int[][] indexOther = new int[nbCurve][];
    // Implementation note: indexOther - the index of the underlying curves, if any.
    loopname = 0;
    for (final String name : curveNamesSet) { // loop over all curves (by name)
      List<String> underlyingCurveNames = multicurves.getUnderlyingCurvesNames(name);
      nbNewParameters[loopname] = multicurves.getNumberOfParameters(name);
      List<Integer> indexOtherList = new ArrayList<Integer>();
      for (String u : underlyingCurveNames) {
        Integer i = curveNum.get(u);
        if (i != null) {
          indexOtherList.add(i);
          nbNewParameters[loopname] -= nbNewParameters[i];
        }
      }
      indexOther[loopname] = ArrayUtils.toPrimitive(indexOtherList.toArray(new Integer[0]));
      loopname++;
    }
    int nbSensiCurve = 0;
    for (final String name : curveNamesSet) { // loop over all curves (by name)
      if (curvesSet.contains(name)) {
        nbSensiCurve++;
      }
    }
    int[] nbNewParamSensiCurve = new int[nbSensiCurve];
    // Implementation note: nbNewParamSensiCurve
    int[][] indexOtherSensiCurve = new int[nbSensiCurve][];
    // Implementation note: indexOtherSensiCurve - 
    int[] startCleanParameter = new int[nbSensiCurve];
    // Implementation note: startCleanParameter - for each curve for which the sensitivity should be computed, the index in the total sensitivity vector at which that curve start.
    int[][] startDirtyParameter = new int[nbSensiCurve][];
    // Implementation note: startDirtyParameter - for each curve for which the sensitivity should be computed, the indexes of the underlying curves.
    int nbSensitivityCurve = 0;
    int nbCleanParameters = 0;
    int currentDirtyStart = 0;
    for (final String name : curveNamesSet) { // loop over all curves (by name)
      if (curvesSet.contains(name)) {
        int num = curveNum.get(name);
        List<Integer> startDirtyParameterList = new ArrayList<Integer>();
        List<String> underlyingCurveNames = multicurves.getUnderlyingCurvesNames(name);
        for (String u : underlyingCurveNames) {
          Integer i = curveNum.get(u);
          if (i != null) {
            startDirtyParameterList.add(currentDirtyStart);
            currentDirtyStart += nbNewParameters[i];
          }
        }
        startDirtyParameterList.add(currentDirtyStart);
        currentDirtyStart += nbNewParameters[nbSensitivityCurve];
        startDirtyParameter[nbSensitivityCurve] = ArrayUtils.toPrimitive(startDirtyParameterList.toArray(new Integer[0]));
        nbNewParamSensiCurve[nbSensitivityCurve] = nbNewParameters[num];
        indexOtherSensiCurve[nbSensitivityCurve] = indexOther[num];
        startCleanParameter[nbSensitivityCurve] = nbCleanParameters;
        nbCleanParameters += nbNewParamSensiCurve[nbSensitivityCurve];
        nbSensitivityCurve++;
      }
    }

    final double[] sensiDirty = new double[0];

    // Use parameter sensi fo the first pass?: see no underlying code!
    // YieldAndDiscount
    Map<String, List<DoublesPair>> sensitivityDsc = sensitivity.getYieldDiscountingSensitivities();
    for (final String name : sensitivityDsc.keySet()) {
      if (curvesSet.contains(name)) {
        double[] s1name = multicurves.parameterSensitivity(name, sensitivityDsc.get(name));
        ArrayUtils.addAll(sensiDirty, s1name);
      }
    }
    // Forward
    Map<String, List<ForwardSensitivity>> sensitivityFwd = sensitivity.getForwardSensitivities();
    for (final String name : sensitivityFwd.keySet()) {
      if (curvesSet.contains(name)) {
        double[] s1name = multicurves.parameterForwardSensitivity(name, sensitivityFwd.get(name));
        ArrayUtils.addAll(sensiDirty, s1name);
      }
    }
    double[] sensiClean = new double[nbCleanParameters];
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
