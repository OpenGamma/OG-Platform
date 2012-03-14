/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.ArrayUtils;

import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.DoubleLabelledMatrix1D;
import com.opengamma.financial.analytics.LabelledMatrix1D;
import com.opengamma.financial.analytics.ircurve.InterpolatedYieldCurveSpecificationWithSecurities;
import com.opengamma.financial.analytics.model.fixedincome.YieldCurveLabelGenerator;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.util.tuple.DoublesPair;

/**
 * 
 */
public class YieldCurveNodeSensitivitiesHelper {
  private static final DecimalFormat s_formatter = new DecimalFormat("##.######");

  @Deprecated
  public static Set<ComputedValue> getSensitivitiesForCurve(final YieldAndDiscountCurve curve,
      final DoubleMatrix1D sensitivitiesForCurve, final InterpolatedYieldCurveSpecificationWithSecurities curveSpec,
      final ValueSpecification resultSpec) {
    final int n = sensitivitiesForCurve.getNumberOfElements();
    final Double[] keys = curve.getCurve().getXData();
    final double[] values = new double[n];
    final Object[] labels = YieldCurveLabelGenerator.getLabels(curveSpec);
    DoubleLabelledMatrix1D labelledMatrix = new DoubleLabelledMatrix1D(keys, labels, values);
    for (int i = 0; i < n; i++) {
      labelledMatrix = (DoubleLabelledMatrix1D) labelledMatrix.add(keys[i], labels[i], sensitivitiesForCurve.getEntry(i));
    }
    return Collections.singleton(new ComputedValue(resultSpec, labelledMatrix));
  }

  public static Set<ComputedValue> getInstrumentLabelledSensitivitiesForCurve(final String curveName, final YieldCurveBundle bundle,
      final DoubleMatrix1D sensitivitiesForCurve, final InterpolatedYieldCurveSpecificationWithSecurities curveSpec,
      final ValueSpecification resultSpec) {
    final int nSensitivites = curveSpec.getStrips().size();
    int startIndex = 0;
    for (final String name : bundle.getAllNames()) {
      if (curveName.equals(name)) {
        break;
      }
      startIndex += bundle.getCurve(name).getCurve().size(); //TODO won't work for functional curves
    }
    final YieldAndDiscountCurve curve = bundle.getCurve(curveName);
    final Double[] keys = curve.getCurve().getXData();
    final double[] values = new double[nSensitivites];
    final Object[] labels = YieldCurveLabelGenerator.getLabels(curveSpec);
    for (int i = 0; i < nSensitivites; i++) {
      values[i] = sensitivitiesForCurve.getEntry(i + startIndex);
    }
    final DoubleLabelledMatrix1D labelledMatrix = new DoubleLabelledMatrix1D(keys, labels, values);
    return Collections.singleton(new ComputedValue(resultSpec, labelledMatrix));
  }

  public static Set<ComputedValue> getTimeLabelledSensitivitiesForCurve(final List<DoublesPair> resultList, final ValueSpecification resultSpec) {
    final int n = resultList.size();
    final Double[] keys = new Double[n];
    final double[] values = new double[n];
    final Object[] labels = new Object[n];
    LabelledMatrix1D<Double, Double> labelledMatrix = new DoubleLabelledMatrix1D(ArrayUtils.EMPTY_DOUBLE_OBJECT_ARRAY, ArrayUtils.EMPTY_OBJECT_ARRAY, ArrayUtils.EMPTY_DOUBLE_ARRAY);
    for (int i = 0; i < n; i++) {
      final DoublesPair pair = resultList.get(i);
      keys[i] = pair.first;
      values[i] = pair.second;
      labels[i] = s_formatter.format(pair.first);
      labelledMatrix = labelledMatrix.add(pair.first, s_formatter.format(pair.first), pair.second, 1e-16);
    }
    return Collections.singleton(new ComputedValue(resultSpec, labelledMatrix));
  }

  @Deprecated
  public static Set<ComputedValue> getSensitivitiesForMultipleCurves(final String forwardCurveName, final String fundingCurveName,
      final ValueSpecification forwardResultSpecification, final ValueSpecification fundingResultSpecification, final YieldCurveBundle bundle,
      final DoubleMatrix1D sensitivitiesForCurves, final Map<String, InterpolatedYieldCurveSpecificationWithSecurities> curveSpecs) {
    final int nForward = bundle.getCurve(forwardCurveName).getCurve().size();
    final int nFunding = bundle.getCurve(fundingCurveName).getCurve().size();
    final Map<String, DoubleMatrix1D> sensitivities = new HashMap<String, DoubleMatrix1D>();
    sensitivities.put(fundingCurveName, new DoubleMatrix1D(Arrays.copyOfRange(sensitivitiesForCurves.toArray(), 0, nFunding)));
    sensitivities.put(forwardCurveName, new DoubleMatrix1D(Arrays.copyOfRange(sensitivitiesForCurves.toArray(), nFunding, nForward + nFunding)));
    final Set<ComputedValue> results = new HashSet<ComputedValue>();
    results.addAll(getInstrumentLabelledSensitivitiesForCurve(fundingCurveName, bundle, sensitivities.get(fundingCurveName),
        curveSpecs.get(fundingCurveName), fundingResultSpecification));
    results.addAll(getInstrumentLabelledSensitivitiesForCurve(forwardCurveName, bundle, sensitivities.get(forwardCurveName),
        curveSpecs.get(forwardCurveName), forwardResultSpecification));
    return results;
  }
}
