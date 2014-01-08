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
import org.threeten.bp.LocalDate;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.model.interestrate.curve.DiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.DoubleLabelledMatrix1D;
import com.opengamma.financial.analytics.LabelledMatrix1D;
import com.opengamma.financial.analytics.curve.CurveSpecification;
import com.opengamma.financial.analytics.fxforwardcurve.ConfigDBFXForwardCurveDefinitionSource;
import com.opengamma.financial.analytics.fxforwardcurve.ConfigDBFXForwardCurveSpecificationSource;
import com.opengamma.financial.analytics.fxforwardcurve.FXForwardCurveDefinition;
import com.opengamma.financial.analytics.fxforwardcurve.FXForwardCurveInstrumentProvider;
import com.opengamma.financial.analytics.fxforwardcurve.FXForwardCurveSpecification;
import com.opengamma.financial.analytics.ircurve.InterpolatedYieldCurveSpecificationWithSecurities;
import com.opengamma.financial.analytics.model.fixedincome.YieldCurveLabelGenerator;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.UnorderedCurrencyPair;
import com.opengamma.util.time.Tenor;
import com.opengamma.util.tuple.DoublesPair;

/**
 *
 */
public class YieldCurveNodeSensitivitiesHelper {
  private static final DecimalFormat s_formatter = new DecimalFormat("##.######");

  /**
   * @deprecated Use {@link #getInstrumentLabelledSensitivitiesForCurve(String, YieldCurveBundle, DoubleMatrix1D, InterpolatedYieldCurveSpecificationWithSecurities, ValueSpecification)} instead
   * @param curve The curve
   * @param sensitivitiesForCurve The sensitivities for the curve
   * @param curveSpec The curve specification
   * @param resultSpec The resultSpecification
   * @return The computed value
   */
  @Deprecated
  public static Set<ComputedValue> getSensitivitiesForCurve(final YieldAndDiscountCurve curve, final DoubleMatrix1D sensitivitiesForCurve,
      final InterpolatedYieldCurveSpecificationWithSecurities curveSpec, final ValueSpecification resultSpec) {
    final int n = sensitivitiesForCurve.getNumberOfElements();
    if (!(curve instanceof YieldCurve)) {
      throw new IllegalArgumentException("Can only handle YieldCurve");
    }
    final Double[] keys = ((YieldCurve) curve).getCurve().getXData();
    final double[] values = new double[n];
    final Object[] labels = YieldCurveLabelGenerator.getLabels(curveSpec);
    DoubleLabelledMatrix1D labelledMatrix = new DoubleLabelledMatrix1D(keys, labels, values);
    for (int i = 0; i < n; i++) {
      labelledMatrix = (DoubleLabelledMatrix1D) labelledMatrix.add(keys[i], labels[i], sensitivitiesForCurve.getEntry(i));
    }
    return Collections.singleton(new ComputedValue(resultSpec, labelledMatrix));
  }

  public static Set<ComputedValue> getInstrumentLabelledSensitivitiesForCurve(final String curveName, final YieldCurveBundle bundle, final DoubleMatrix1D sensitivitiesForCurve,
      final InterpolatedYieldCurveSpecificationWithSecurities curveSpec, final ValueSpecification resultSpec) {
    return getHybridLabelledSensitivitiesForCurve(curveName, bundle, sensitivitiesForCurve, curveSpec, resultSpec);
  }

  public static Set<ComputedValue> getInstrumentLabelledSensitivitiesForCurve(final String curveName, final YieldCurveBundle bundle, final DoubleMatrix1D sensitivitiesForCurve,
      final CurveSpecification curveSpec, final ValueSpecification resultSpec) {
    return getHybridLabelledSensitivitiesForCurve(curveName, bundle, sensitivitiesForCurve, curveSpec, resultSpec);
  }

  public static Set<ComputedValue> getHybridLabelledSensitivitiesForCurve(final String curveName, final YieldCurveBundle bundle, final DoubleMatrix1D sensitivitiesForCurve,
      final InterpolatedYieldCurveSpecificationWithSecurities curveSpec, final ValueSpecification resultSpec) {
    final int nSensitivities = curveSpec.getStrips().size();
    int startIndex = 0;
    for (final String name : bundle.getAllNames()) {
      if (curveName.equals(name)) {
        break;
      }
      if (!(bundle.getCurve(name) instanceof YieldCurve)) { //TODO: make it more generic
        throw new IllegalArgumentException("Can only handle YieldCurve");
      }
      startIndex += ((YieldCurve) bundle.getCurve(name)).getCurve().size();
    }
    final YieldAndDiscountCurve curve = bundle.getCurve(curveName);
    final Double[] keys = ((YieldCurve) curve).getCurve().getXData();
    final double[] values = new double[nSensitivities];
    final Object[] labels = YieldCurveLabelGenerator.getHybridLabels(curveSpec);
    for (int i = 0; i < nSensitivities; i++) {
      values[i] = sensitivitiesForCurve.getEntry(i + startIndex);
    }
    final DoubleLabelledMatrix1D labelledMatrix = new DoubleLabelledMatrix1D(keys, labels, values);
    return Collections.singleton(new ComputedValue(resultSpec, labelledMatrix));
  }

  public static Set<ComputedValue> getHybridLabelledSensitivitiesForCurve(final String curveName, final YieldCurveBundle bundle, final DoubleMatrix1D sensitivitiesForCurve,
      final CurveSpecification curveSpec, final ValueSpecification resultSpec) {
    final int nSensitivities = curveSpec.getNodes().size();
    int startIndex = 0;
    for (final String name : bundle.getAllNames()) {
      if (curveName.equals(name)) {
        break;
      }
      if (!(bundle.getCurve(name) instanceof YieldCurve)) { //TODO: make it more generic
        throw new IllegalArgumentException("Can only handle YieldCurve");
      }
      startIndex += ((YieldCurve) bundle.getCurve(name)).getCurve().size();
    }
    final YieldAndDiscountCurve curve = bundle.getCurve(curveName);
    final Double[] keys;
    if (curve instanceof YieldCurve) {
      keys = ((YieldCurve) curve).getCurve().getXData();
    } else if (curve instanceof DiscountCurve) {
      keys = ((DiscountCurve) curve).getCurve().getXData();
    } else {
      throw new OpenGammaRuntimeException("Cant get underlying curve from: " + curve);
    }
    final double[] values = new double[nSensitivities];
    final Object[] labels = YieldCurveLabelGenerator.getHybridLabels(curveSpec);
    for (int i = 0; i < nSensitivities; i++) {
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

  public static Set<ComputedValue> getInstrumentLabelledSensitivitiesForCurve(final DoubleMatrix1D sensitivities, final Currency domesticCurrency, final Currency foreignCurrency,
      final String[] curveNames, final YieldCurveBundle curves, final ConfigDBFXForwardCurveSpecificationSource fxForwardCurveSpecificationSource,
      final ConfigDBFXForwardCurveDefinitionSource fxForwardCurveDefinitionSource, final LocalDate localNow, final ValueSpecification resultSpec) {
    final String currencyPair = UnorderedCurrencyPair.of(domesticCurrency, foreignCurrency).toString();
    final FXForwardCurveSpecification fxForwardCurveSpecification = fxForwardCurveSpecificationSource.getSpecification(curveNames[0], currencyPair);
    final FXForwardCurveDefinition fxForwardCurveDefinition = fxForwardCurveDefinitionSource.getDefinition(curveNames[0], currencyPair);
    final FXForwardCurveInstrumentProvider curveInstrumentProvider = fxForwardCurveSpecification.getCurveInstrumentProvider();
    final Tenor[] tenors = fxForwardCurveDefinition.getTenorsArray();
    final int length = tenors.length;
    final Double[] keys = new Double[length];
    final Object[] labels = new Object[length];
    final double[] values = new double[length];
    if (!(curves.getCurve(curveNames[0]) instanceof YieldCurve)) { //TODO: make it more generic
      throw new IllegalArgumentException("Can only handle YieldCurve");
    }
    final Double[] xData = ((YieldCurve) curves.getCurve(curveNames[0])).getCurve().getXData();
    for (int i = 0; i < length; i++) {
      keys[i] = xData[i];
      labels[i] = curveInstrumentProvider.getInstrument(localNow, tenors[i]);
      values[i] = sensitivities.getEntry(i);
    }
    final DoubleLabelledMatrix1D labelledMatrix = new DoubleLabelledMatrix1D(keys, labels, values);
    return Collections.singleton(new ComputedValue(resultSpec, labelledMatrix));
  }

  /**
   * @deprecated Use {@link #getInstrumentLabelledSensitivitiesForCurve(String, YieldCurveBundle, DoubleMatrix1D, InterpolatedYieldCurveSpecificationWithSecurities, ValueSpecification)} instead
   * @param forwardCurveName The forward curve name
   * @param fundingCurveName The funding curve name
   * @param forwardResultSpecification The forward result specification
   * @param fundingResultSpecification The funding result specification
   * @param bundle The bundle containing the yield curves
   * @param sensitivitiesForCurves A matrix containing the sensitivities to each curve in the bundle
   * @param curveSpecs The specifications for the forward and funding curves
   * @return The computed value
   */
  @Deprecated
  public static Set<ComputedValue> getSensitivitiesForMultipleCurves(final String forwardCurveName, final String fundingCurveName, final ValueSpecification forwardResultSpecification,
      final ValueSpecification fundingResultSpecification, final YieldCurveBundle bundle, final DoubleMatrix1D sensitivitiesForCurves,
      final Map<String, InterpolatedYieldCurveSpecificationWithSecurities> curveSpecs) {
    if (!(bundle.getCurve(forwardCurveName) instanceof YieldCurve)) { //TODO: make it more generic
      throw new IllegalArgumentException("Can only handle YieldCurve");
    }
    final int nForward = ((YieldCurve) bundle.getCurve(forwardCurveName)).getCurve().size();
    final int nFunding = ((YieldCurve) bundle.getCurve(fundingCurveName)).getCurve().size();
    final Map<String, DoubleMatrix1D> sensitivities = new HashMap<>();
    sensitivities.put(fundingCurveName, new DoubleMatrix1D(Arrays.copyOfRange(sensitivitiesForCurves.toArray(), 0, nFunding)));
    sensitivities.put(forwardCurveName, new DoubleMatrix1D(Arrays.copyOfRange(sensitivitiesForCurves.toArray(), nFunding, nForward + nFunding)));
    final Set<ComputedValue> results = new HashSet<>();
    results.addAll(getSensitivitiesForCurve(bundle.getCurve(fundingCurveName), sensitivities.get(fundingCurveName), curveSpecs.get(fundingCurveName), fundingResultSpecification));
    results.addAll(getSensitivitiesForCurve(bundle.getCurve(forwardCurveName), sensitivities.get(forwardCurveName), curveSpecs.get(forwardCurveName), forwardResultSpecification));
    return results;
  }
}
