/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.curve;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.ArrayUtils;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.interestrate.InterestRateCurveSensitivity;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;

/**
 * For an instrument, computes the sensitivity of a value (often the present value or a par spread) to the parameters used in the curve.
 * The meaning of "parameters" will depend of the way the curve is stored (interpolated yield, function parameters, etc.).
 * In case a curve is the spread to another curve included in the bundle, the sensitivity is with respect to the underlying curve parameters.
 * The "underlying" curves taken into account are only to one level deep.
 * The return format is a vector (DoubleMatrix1D) with length equal to the total number of parameters in all the curves, 
 * and ordered as the parameters to the different curves themselves in increasing order. 
 */
public class ParameterUnderlyingSensitivityCalculator extends AbstractParameterSensitivityCalculator {

  /**
   * Constructor
   * @param curveSensitivityCalculator The curve sensitivity calculator.
   */
  public ParameterUnderlyingSensitivityCalculator(InstrumentDerivativeVisitor<YieldCurveBundle, InterestRateCurveSensitivity> curveSensitivityCalculator) {
    super(curveSensitivityCalculator);
  }

  /**
   * Computes the sensitivity with respect to the parameters from the point sensitivities to the continuously compounded rate.
   * @param sensitivity The point sensitivity.
   * @param fixedCurves The fixed curves names (for which the parameter sensitivity are not computed even if they are necessary for the instrument pricing).
   * The curve in the list may or may not be in the bundle. Not null.
   * @param bundle The curve bundle with all the curves with respect to which the sensitivity should be computed. Not null.
   * @return The sensitivity (as a DoubleMatrix1D).
   */
  @Override
  public DoubleMatrix1D pointToParameterSensitivity(final InterestRateCurveSensitivity sensitivity, final Set<String> fixedCurves, final YieldCurveBundle bundle) {
    Integer nbCurve = 0;
    LinkedHashMap<String, Integer> curveNum = new LinkedHashMap<String, Integer>();
    for (final String name : bundle.getAllNames()) { // loop over all curves (by name)
      if (!fixedCurves.contains(name)) {
        curveNum.put(name, nbCurve++);
      }
    }
    nbCurve = 0;
    int[] nbNewParameters = new int[curveNum.size()];
    // Implementation note: 
    int[] startCleanParameter = new int[curveNum.size()];
    // Implementation note: 
    int[][] startDirtyParameter = new int[curveNum.size()][];
    int[][] indexOther = new int[curveNum.size()][];
    // Implementation note: the start of the different blocs of parameters. First the other curves, then the new part.
    int nbCleanParameters = 0;
    int currentDirtyStart = 0;
    for (final String name : bundle.getAllNames()) { // loop over all curves (by name)
      if (!fixedCurves.contains(name)) {
        final YieldAndDiscountCurve curve = bundle.getCurve(name);
        List<String> underlyingCurveNames = curve.getUnderlyingCurvesNames();
        startCleanParameter[nbCurve] = nbCleanParameters;
        nbNewParameters[nbCurve] = curve.getNumberOfParameters();
        List<Integer> indexOtherList = new ArrayList<Integer>();
        List<Integer> startDirtyParameterList = new ArrayList<Integer>();
        for (String u : underlyingCurveNames) {
          Integer i = curveNum.get(u);
          if (i != null) {
            indexOtherList.add(i);
            nbNewParameters[nbCurve] -= nbNewParameters[i];
            startDirtyParameterList.add(currentDirtyStart);
            currentDirtyStart += nbNewParameters[i];
          }
        }
        startDirtyParameterList.add(currentDirtyStart);
        currentDirtyStart += nbNewParameters[nbCurve];
        indexOther[nbCurve] = ArrayUtils.toPrimitive(indexOtherList.toArray(new Integer[0]));
        startDirtyParameter[nbCurve] = ArrayUtils.toPrimitive(startDirtyParameterList.toArray(new Integer[0]));
        nbCleanParameters += nbNewParameters[nbCurve];
        nbCurve++;
      }
    }
    final List<Double> sensiDirtyList = new ArrayList<Double>();
    for (final String name : bundle.getAllNames()) { // loop over all curves (by name)
      if (!fixedCurves.contains(name)) {
        final YieldAndDiscountCurve curve = bundle.getCurve(name);
        List<Double> oneCurveSensitivity = pointToParameterSensitivity(sensitivity.getSensitivities().get(name), curve);
        sensiDirtyList.addAll(oneCurveSensitivity);
      }
    }
    double[] sensiDirty = ArrayUtils.toPrimitive(sensiDirtyList.toArray(new Double[0]));
    double[] sensiClean = new double[nbCleanParameters];
    for (int loopcurve = 0; loopcurve < nbCurve; loopcurve++) {
      for (int loopo = 0; loopo < indexOther[loopcurve].length; loopo++) {
        for (int loops = 0; loops < nbNewParameters[indexOther[loopcurve][loopo]]; loops++) {
          sensiClean[startCleanParameter[indexOther[loopcurve][loopo]] + loops] += sensiDirty[startDirtyParameter[loopcurve][loopo] + loops];
        }
      }
      for (int loops = 0; loops < nbNewParameters[loopcurve]; loops++) {
        sensiClean[startCleanParameter[loopcurve] + loops] += sensiDirty[startDirtyParameter[loopcurve][indexOther[loopcurve].length] + loops];
      }
    }
    return new DoubleMatrix1D(sensiClean);
  }

}
