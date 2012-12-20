/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.curve.sensitivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.ArrayUtils;

import com.opengamma.analytics.financial.forex.method.MultipleCurrencyInterestRateCurveSensitivity;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.interestrate.InterestRateCurveSensitivity;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.ObjectsPair;
import com.opengamma.util.tuple.Pair;

/**
 * For an instrument, computes the sensitivity of a present value to the parameters used in the curve.
 * The meaning of "parameters" will depend of the way the curve is stored (interpolated yield, function parameters, etc.).
 * The return format is a ParameterSensitivity object, i.e. a map between Curve/Currency and the sensitivity to the parameters in the curve.
 */
public class ParameterUnderlyingSensitivityBlockCalculator extends AbstractParameterSensitivityBlockCalculator {

  /**
   * Constructor
   * @param curveSensitivityCalculator The curve sensitivity calculator.
   */
  public ParameterUnderlyingSensitivityBlockCalculator(InstrumentDerivativeVisitor<YieldCurveBundle, MultipleCurrencyInterestRateCurveSensitivity> curveSensitivityCalculator) {
    super(curveSensitivityCalculator);
  }

  /**
   * Computes the sensitivity with respect to the parameters from the point sensitivities to the continuously compounded rate.
   * @param sensitivity The point sensitivity.
   * @param fixedCurves The fixed curves names (for which the parameter sensitivity are not computed even if they are necessary for the instrument pricing).
   * The curve in the list may or may not be in the bundle. Not null.
   * @param bundle The curve bundle with all the curves with respect to which the sensitivity should be computed. Not null.
   * @return The sensitivity.
   */
  @Override
  public ParameterSensitivity pointToParameterSensitivity(final MultipleCurrencyInterestRateCurveSensitivity sensitivity, final Set<String> fixedCurves, final YieldCurveBundle bundle) {
    ArgumentChecker.notNull(sensitivity, "Sensitivity");
    ArgumentChecker.notNull(fixedCurves, "Fixed Curves");
    ArgumentChecker.notNull(bundle, "Curve bundle");
    ParameterSensitivity result = new ParameterSensitivity();
    for (final Currency ccy : sensitivity.getCurrencies()) {
      result = result.plus(pointToParameterSensitivity(ccy, sensitivity.getSensitivity(ccy), fixedCurves, bundle));
    }
    return result;
  }

  public ParameterSensitivity pointToParameterSensitivity(final Currency ccy, final InterestRateCurveSensitivity sensitivity, final Set<String> fixedCurves, final YieldCurveBundle bundle) {
    Set<String> curveNamesSet = bundle.getAllNames();
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
      final YieldAndDiscountCurve curve = bundle.getCurve(name);
      List<String> underlyingCurveNames = curve.getUnderlyingCurvesNames();
      nbNewParameters[loopname] = curve.getNumberOfParameters();
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
    loopname = 0;
    for (final String name : bundle.getAllNames()) { // loop over all curves (by name)
      if (!fixedCurves.contains(name)) {
        loopname++;
      }
    }
    int nbSensitivityCurve = loopname;
    int[] nbNewParamSensiCurve = new int[nbSensitivityCurve];
    // Implementation note: nbNewParamSensiCurve
    int[][] indexOtherSensiCurve = new int[nbSensitivityCurve][];
    // Implementation note: indexOtherSensiCurve - 
    // int[] startCleanParameter = new int[nbSensitivityCurve];
    // Implementation note: startCleanParameter - for each curve for which the sensitivity should be computed, the index in the total sensitivity vector at which that curve start.
    int[][] startDirtyParameter = new int[nbSensitivityCurve][];
    // Implementation note: startDirtyParameter - for each curve for which the sensitivity should be computed, the indexes of the underlying curves.
    int nbCleanParameters = 0;
    int currentDirtyStart = 0;
    loopname = 0;
    for (final String name : curveNamesSet) { // loop over all curves (by name)
      if (!fixedCurves.contains(name)) {
        int num = curveNum.get(name);
        final YieldAndDiscountCurve curve = bundle.getCurve(name);
        List<Integer> startDirtyParameterList = new ArrayList<Integer>();
        List<String> underlyingCurveNames = curve.getUnderlyingCurvesNames();
        for (String u : underlyingCurveNames) {
          Integer i = curveNum.get(u);
          if (i != null) {
            startDirtyParameterList.add(currentDirtyStart);
            currentDirtyStart += nbNewParameters[i];
          }
        }
        startDirtyParameterList.add(currentDirtyStart);
        currentDirtyStart += nbNewParameters[loopname];
        startDirtyParameter[loopname] = ArrayUtils.toPrimitive(startDirtyParameterList.toArray(new Integer[0]));
        nbNewParamSensiCurve[loopname] = nbNewParameters[num];
        indexOtherSensiCurve[loopname] = indexOther[num];
        // startCleanParameter[loopname] = nbCleanParameters;
        nbCleanParameters += nbNewParamSensiCurve[loopname];
        loopname++;
      }
    }
    final List<Double> sensiDirtyList = new ArrayList<Double>();
    for (final String name : curveNamesSet) { // loop over all curves (by name)
      if (!fixedCurves.contains(name)) {
        final YieldAndDiscountCurve curve = bundle.getCurve(name);
        Double[] oneCurveSensitivity = pointToParameterSensitivity(sensitivity.getSensitivities().get(name), curve);
        sensiDirtyList.addAll(Arrays.asList(oneCurveSensitivity));
      }
    }
    double[] sensiDirty = ArrayUtils.toPrimitive(sensiDirtyList.toArray(new Double[0]));
    double[][] sensiClean = new double[nbSensitivityCurve][];
    for (int loopcurve = 0; loopcurve < nbSensitivityCurve; loopcurve++) {
      sensiClean[loopcurve] = new double[nbNewParamSensiCurve[loopcurve]];
    }
    for (int loopcurve = 0; loopcurve < nbSensitivityCurve; loopcurve++) {
      for (int loopo = 0; loopo < indexOtherSensiCurve[loopcurve].length; loopo++) {
        if (!fixedCurves.contains(curveNamesArray[indexOtherSensiCurve[loopcurve][loopo]])) {
          for (int loops = 0; loops < nbNewParamSensiCurve[indexOtherSensiCurve[loopcurve][loopo]]; loops++) {
            sensiClean[indexOtherSensiCurve[loopcurve][loopo]][loops] += sensiDirty[startDirtyParameter[loopcurve][loopo] + loops];
          }
        }
      }
      for (int loops = 0; loops < nbNewParamSensiCurve[loopcurve]; loops++) {
        sensiClean[loopcurve][loops] += sensiDirty[startDirtyParameter[loopcurve][indexOtherSensiCurve[loopcurve].length] + loops];
      }
    }
    final LinkedHashMap<Pair<String, Currency>, DoubleMatrix1D> result = new LinkedHashMap<Pair<String, Currency>, DoubleMatrix1D>();
    for (int loopcurve = 0; loopcurve < nbSensitivityCurve; loopcurve++) {
      result.put(new ObjectsPair<String, Currency>(curveNamesArray[loopcurve], ccy), new DoubleMatrix1D(sensiClean[loopcurve]));
    }
    return new ParameterSensitivity(result);
  }

}
