/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.sensitivity.parameter;

import it.unimi.dsi.fastutil.ints.IntArrayList;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.provider.description.interestrate.ParameterProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.ForwardSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyMulticurveSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyParameterSensitivity;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.DoublesPair;
import com.opengamma.util.tuple.Pairs;

/**
 * For an instrument, computes the sensitivity of a value (often the present value) to the parameters used in the curve.
 * The meaning of "parameters" will depend of the way the curve is stored (interpolated yield, function parameters, etc.).
 * The sensitivity is computed for the underlying curves up to one level deep, not fuether.
 * The return format is MultipleCurrencyParameterSensitivity object.
 * @param <DATA_TYPE> Data type.
 */
public class ParameterSensitivityUnderlyingParameterCalculator<DATA_TYPE extends ParameterProviderInterface> 
  extends ParameterSensitivityParameterAbstractCalculator<DATA_TYPE> {

  /**
   * Constructor
   * @param curveSensitivityCalculator The curve sensitivity calculator.
   */
  public ParameterSensitivityUnderlyingParameterCalculator(
      final InstrumentDerivativeVisitor<DATA_TYPE, MultipleCurrencyMulticurveSensitivity> curveSensitivityCalculator) {
    super(curveSensitivityCalculator);
  }

  @Override
  public MultipleCurrencyParameterSensitivity pointToParameterSensitivity(final MultipleCurrencyMulticurveSensitivity sensitivity, 
      final DATA_TYPE parameterMulticurves, final Set<String> sensicurveNamesSet) {
    ArgumentChecker.notNull(sensitivity, "sensitivity");
    ArgumentChecker.notNull(parameterMulticurves, "multicurves parameter");
    ArgumentChecker.notNull(sensicurveNamesSet, "curves set");
    // TODO: The first part depends only of the multicurves and curvesSet, not the sensitivity. Should it be refactored and done only once?
    final Set<String> multicurveNamesSet = parameterMulticurves.getMulticurveProvider().getAllNames();
    final String[] multicurveNamesArray = multicurveNamesSet.toArray(new String[0]);
    // Implementation note: Check sensicurve are in multicurve
    ArgumentChecker.isTrue(multicurveNamesSet.containsAll(sensicurveNamesSet), "curve in the names set not in the multi-curve provider");
    final int nbMultiCurve = multicurveNamesSet.size();
    // Populate the name names and numbers for the curves in the multicurve
    final LinkedHashMap<String, Integer> multicurveNum = new LinkedHashMap<>();
    for (int loopcur = 0; loopcur < nbMultiCurve; loopcur++) { // loop over all curves in multicurves
      multicurveNum.put(multicurveNamesArray[loopcur], loopcur);
    }
    final int[] nbNewParameters = new int[nbMultiCurve];
    final int[] nbParameters = new int[nbMultiCurve];
    // Implementation note: nbNewParameters - number of new parameters in the curve, parameters not 
    //   from an underlying curve which is another curve of the bundle.
    for (int loopcur = 0; loopcur < nbMultiCurve; loopcur++) { // loop over all curves in multicurves
      nbParameters[loopcur] = 
          parameterMulticurves.getMulticurveProvider().getNumberOfParameters(multicurveNamesArray[loopcur]);
      nbNewParameters[loopcur] = nbParameters[loopcur];
    }
    for (int loopcur = 0; loopcur < nbMultiCurve; loopcur++) { // loop over all curves in multicurves
      final List<String> underlyingCurveNames = 
          parameterMulticurves.getMulticurveProvider().getUnderlyingCurvesNames(multicurveNamesArray[loopcur]);
      for (final String u : underlyingCurveNames) {
        final Integer i = multicurveNum.get(u);
        if (i != null) {
          nbNewParameters[loopcur] -= nbNewParameters[i]; // Only one level: a curve used as an underlying can not have an underlying itself.
        }
      }
    }
    // Implementation note: nbNewParamSensiCurve
    final int[][] indexOtherMulticurve = new int[nbMultiCurve][];
    // Implementation note: indexOtherMultiCurve - for each curve in the multi-curve, the index of the underlying curves in the same set
    final int[] startOwnParameter = new int[nbMultiCurve];
    // Implementation note: The start index of the parameters of the own (new) parameters.
    final int[][] startUnderlyingParameter = new int[nbMultiCurve][];
    // Implementation note: The start index of the parameters of the underlying curves
    for (int loopcur = 0; loopcur < nbMultiCurve; loopcur++) { // loop over all curves in multicurves
      final List<String> underlyingCurveNames = 
          parameterMulticurves.getMulticurveProvider().getUnderlyingCurvesNames(multicurveNamesArray[loopcur]);
      final IntArrayList indexOtherMulticurveList = new IntArrayList();
      for (final String u : underlyingCurveNames) {
        final Integer i = multicurveNum.get(u);
        if (i != null) {
          indexOtherMulticurveList.add(i);
        }
      }
      indexOtherMulticurve[loopcur] = indexOtherMulticurveList.toIntArray();
    }
    for (int loopcurve = 0; loopcurve < nbMultiCurve; loopcurve++) { // loop over all curves in multicurves
      int loopstart = 0;
      final int num = multicurveNum.get(multicurveNamesArray[loopcurve]);
      final IntArrayList startUnderlyingParamList = new IntArrayList();
      final List<String> underlyingCurveNames = 
          parameterMulticurves.getMulticurveProvider().getUnderlyingCurvesNames(multicurveNamesArray[loopcurve]);
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
    // Implementation note: Compute the "dirty" sensitivity, i.e. the sensitivity to all the parameters in each curve. 
    // The underlying are taken into account in the "clean" step.
    Set<Currency> ccySet = sensitivity.getCurrencies();
    Currency[] ccyArray = ccySet.toArray(new Currency[0]);
    int nbCcy = ccySet.size();
    double[][][] sensiDirty = new double[nbCcy][nbMultiCurve][];
    for (int loopccy = 0; loopccy < nbCcy; loopccy++) {
      final Map<String, List<DoublesPair>> sensitivityDsc = 
          sensitivity.getSensitivity(ccyArray[loopccy]).getYieldDiscountingSensitivities();
      final Map<String, List<ForwardSensitivity>> sensitivityFwd = 
          sensitivity.getSensitivity(ccyArray[loopccy]).getForwardSensitivities();
      for (int loopcurve = 0; loopcurve < nbMultiCurve; loopcurve++) { // loop over all curves
        sensiDirty[loopccy][loopcurve] = new double[nbParameters[loopcurve]];
        final double[] sDsc1Name = parameterMulticurves.parameterSensitivity(multicurveNamesArray[loopcurve], 
            sensitivityDsc.get(multicurveNamesArray[loopcurve]));
        final double[] sFwd1Name = parameterMulticurves.parameterForwardSensitivity(multicurveNamesArray[loopcurve], 
            sensitivityFwd.get(multicurveNamesArray[loopcurve]));
        for (int loopp = 0; loopp < nbParameters[loopcurve]; loopp++) {
          sensiDirty[loopccy][loopcurve][loopp] = sDsc1Name[loopp] + sFwd1Name[loopp];
        }
      }
    }
    // Implementation note: "clean" the sensitivity, i.e. add the parts on the same curves together.
    double[][][] sensiClean = new double[nbCcy][nbMultiCurve][];
    for (int loopccy = 0; loopccy < nbCcy; loopccy++) {
      for (int loopcurve = 0; loopcurve < nbMultiCurve; loopcurve++) { // loop over all curves
        sensiClean[loopccy][loopcurve] = new double[nbNewParameters[loopcurve]];
      }
    }
    for (int loopccy = 0; loopccy < nbCcy; loopccy++) {
      for (int loopcurve = 0; loopcurve < nbMultiCurve; loopcurve++) { // loop over all curves
        // Direct sensitivity
        for (int loopi = 0; loopi < nbNewParameters[loopcurve]; loopi++) {
          sensiClean[loopccy][loopcurve][loopi] += sensiDirty[loopccy][loopcurve][startOwnParameter[loopcurve] + loopi];
        }
        // Underlying (indirect) sensitivity
        for (int loopu = 0; loopu < startUnderlyingParameter[loopcurve].length; loopu++) {
          for (int loopi = 0; loopi < nbNewParameters[indexOtherMulticurve[loopcurve][loopu]]; loopi++) {
            sensiClean[loopccy][indexOtherMulticurve[loopcurve][loopu]][loopi] += 
                sensiDirty[loopccy][loopcurve][startUnderlyingParameter[loopcurve][loopu] + loopi];
          }
        }
      }
    }
    MultipleCurrencyParameterSensitivity result = new MultipleCurrencyParameterSensitivity();
    for (int loopccy = 0; loopccy < nbCcy; loopccy++) { // loop on all currencies
      for (int loopcurve = 0; loopcurve < nbMultiCurve; loopcurve++) { // loop over all curves
        result = result.plus(Pairs.of(multicurveNamesArray[loopcurve], ccyArray[loopccy]), 
            new DoubleMatrix1D(sensiClean[loopccy][loopcurve]));
      }
    }
    return result;
  }

  @Override
  public MultipleCurrencyParameterSensitivity pointToParameterSensitivity(
      final MultipleCurrencyMulticurveSensitivity sensitivity, final DATA_TYPE parameterMulticurves) {
    ArgumentChecker.notNull(parameterMulticurves, "multicurves parameter");
    return pointToParameterSensitivity(sensitivity, parameterMulticurves, 
        parameterMulticurves.getMulticurveProvider().getAllCurveNames());
  }
}
