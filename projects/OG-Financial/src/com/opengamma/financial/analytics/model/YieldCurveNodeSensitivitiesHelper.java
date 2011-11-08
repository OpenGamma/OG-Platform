/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.DoubleLabelledMatrix1D;
import com.opengamma.financial.analytics.ircurve.InterpolatedYieldCurveSpecificationWithSecurities;
import com.opengamma.financial.analytics.model.fixedincome.YieldCurveLabelGenerator;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.math.matrix.DoubleMatrix1D;

/**
 * 
 */
public class YieldCurveNodeSensitivitiesHelper {

  public static Set<ComputedValue> getSensitivitiesForCurve(final String curveName, final YieldCurveBundle bundle,
      final DoubleMatrix1D sensitivitiesForCurve, final InterpolatedYieldCurveSpecificationWithSecurities curveSpec, 
      final ValueSpecification resultSpec) {
    final int n = sensitivitiesForCurve.getNumberOfElements();
    final YieldAndDiscountCurve curve = bundle.getCurve(curveName);
    final Double[] keys = curve.getCurve().getXData();
    final double[] values = new double[n];
    final Object[] labels = YieldCurveLabelGenerator.getLabels(curveSpec);
    DoubleLabelledMatrix1D labelledMatrix = new DoubleLabelledMatrix1D(keys, labels, values);
    for (int i = 0; i < n; i++) {
      labelledMatrix = (DoubleLabelledMatrix1D) labelledMatrix.add(keys[i], labels[i], sensitivitiesForCurve.getEntry(i));
    }
    return Collections.singleton(new ComputedValue(resultSpec, labelledMatrix));
  }

  //TODO at some point this needs to deal with more than two curves
  public static Set<ComputedValue> getSensitivitiesForMultipleCurves(final String forwardCurveName, final String fundingCurveName, 
      final ValueSpecification forwardResultSpecification, final ValueSpecification fundingResultSpecification, final YieldCurveBundle bundle,
      final DoubleMatrix1D sensitivitiesForCurves, final Map<String, InterpolatedYieldCurveSpecificationWithSecurities> curveSpecs) {
    final int nForward = bundle.getCurve(forwardCurveName).getCurve().size();
    final int nFunding = bundle.getCurve(fundingCurveName).getCurve().size();
    final Map<String, DoubleMatrix1D> sensitivities = new HashMap<String, DoubleMatrix1D>();
    sensitivities.put(fundingCurveName, new DoubleMatrix1D(Arrays.copyOfRange(sensitivitiesForCurves.toArray(), 0, nFunding)));
    sensitivities.put(forwardCurveName, new DoubleMatrix1D(Arrays.copyOfRange(sensitivitiesForCurves.toArray(), nFunding, nForward + nFunding)));
    final Set<ComputedValue> results = new HashSet<ComputedValue>();
    results.addAll(getSensitivitiesForCurve(fundingCurveName, bundle, sensitivities.get(fundingCurveName), 
        curveSpecs.get(fundingCurveName), fundingResultSpecification));
    results.addAll(getSensitivitiesForCurve(forwardCurveName, bundle, sensitivities.get(forwardCurveName), 
        curveSpecs.get(forwardCurveName), forwardResultSpecification));
    return results;
  }
}
