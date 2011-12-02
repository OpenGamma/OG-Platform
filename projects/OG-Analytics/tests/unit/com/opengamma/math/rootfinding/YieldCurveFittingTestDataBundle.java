/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.rootfinding;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.interestrate.InstrumentDerivative;
import com.opengamma.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.financial.interestrate.MultipleYieldCurveFinderDataBundle;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.math.interpolation.Interpolator1D;
import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.util.tuple.DoublesPair;

/**
 * 
 */
public class YieldCurveFittingTestDataBundle extends MultipleYieldCurveFinderDataBundle {

  public enum TestType {
    ANALYTIC_JACOBIAN, FD_JACOBIAN, FD_CURVE_SENITIVITY
  }

  private InstrumentDerivativeVisitor<YieldCurveBundle, Double> _marketValueCalculator;
  private InstrumentDerivativeVisitor<YieldCurveBundle, Map<String, List<DoublesPair>>> _marketValueSensitivityCalculator;

  private double[] _marketRates;
  private HashMap<String, double[]> _curveYields;
  private DoubleMatrix1D _startPosition;
  private TestType _testType = TestType.ANALYTIC_JACOBIAN;

  /**
   * Gets the testType field.
   * @return the testType
   */
  public TestType getTestType() {
    return _testType;
  }

  /**
   * Sets the testType field.
   * @param testType  the testType
   */
  public void setTestType(TestType testType) {
    _testType = testType;
  }

//  public YieldCurveFittingTestDataBundle(List<InterestRateDerivative> derivatives, YieldCurveBundle knownCurves, LinkedHashMap<String, double[]> unknownCurveNodePoints,
//      LinkedHashMap<String, Interpolator1D> unknownCurveInterpolators,
//      InterestRateDerivativeVisitor<YieldCurveBundle, Double> marketValueCalculator, InterestRateDerivativeVisitor<YieldCurveBundle, Map<String, List<DoublesPair>>> marketValueSensitivityCalculator,
//      double[] marketRates, DoubleMatrix1D startPosition) {
//    this(derivatives, knownCurves, unknownCurveNodePoints, unknownCurveInterpolators, marketValueCalculator, marketValueSensitivityCalculator, marketRates, startPosition, false);
//  }

  /**
   * @param derivatives
   * @param knownCurves
   * @param unknownCurveNodePoints
   * @param unknownCurveInterpolators
   */
  public YieldCurveFittingTestDataBundle(List<InstrumentDerivative> derivatives, YieldCurveBundle knownCurves, LinkedHashMap<String, double[]> unknownCurveNodePoints,
      LinkedHashMap<String, Interpolator1D> unknownCurveInterpolators,
      InstrumentDerivativeVisitor<YieldCurveBundle, Double> marketValueCalculator, InstrumentDerivativeVisitor<YieldCurveBundle, Map<String, List<DoublesPair>>> marketValueSensitivityCalculator,
      double[] marketRates, DoubleMatrix1D startPosition, boolean useFiniteDifferenceByDefault) {
    super(derivatives, marketRates, knownCurves, unknownCurveNodePoints, unknownCurveInterpolators, useFiniteDifferenceByDefault);
    Validate.notNull(marketValueCalculator);
    Validate.notNull(marketValueSensitivityCalculator);
    Validate.notNull(marketRates);
    Validate.notNull(startPosition);
    Validate.isTrue(marketRates.length == startPosition.getNumberOfElements());

    _marketValueCalculator = marketValueCalculator;
    _marketValueSensitivityCalculator = marketValueSensitivityCalculator;
    _marketRates = marketRates;
    _startPosition = startPosition;
    _curveYields = null;
  }

  /**
   * Gets the curveYields field.
   * @return the curveYields
   */
  public HashMap<String, double[]> getCurveYields() {
    return _curveYields;
  }

  /**
   * Sets the curveYields field.
   * @param curveYields  the curveYields
   */
  public void setCurveYields(HashMap<String, double[]> curveYields) {
    _curveYields = curveYields;
  }

//  public YieldCurveFittingTestDataBundle(List<InterestRateDerivative> derivatives, YieldCurveBundle knownCurves, LinkedHashMap<String, double[]> unknownCurveNodePoints,
//      LinkedHashMap<String, Interpolator1D> unknownCurveInterpolators,
//      InterestRateDerivativeVisitor<YieldCurveBundle, Double> marketValueCalculator, InterestRateDerivativeVisitor<YieldCurveBundle, Map<String, List<DoublesPair>>> marketValueSensitivityCalculator,
//      double[] marketRates, DoubleMatrix1D startPosition, HashMap<String, double[]> curveYields) {
//    this(derivatives, knownCurves, unknownCurveNodePoints, unknownCurveInterpolators, marketValueCalculator, marketValueSensitivityCalculator, marketRates, startPosition, curveYields, false);
//  }

  public YieldCurveFittingTestDataBundle(List<InstrumentDerivative> derivatives, YieldCurveBundle knownCurves, LinkedHashMap<String, double[]> unknownCurveNodePoints,
      LinkedHashMap<String, Interpolator1D> unknownCurveInterpolators,
      InstrumentDerivativeVisitor<YieldCurveBundle, Double> marketValueCalculator, InstrumentDerivativeVisitor<YieldCurveBundle, Map<String, List<DoublesPair>>> marketValueSensitivityCalculator,
      double[] marketRates, DoubleMatrix1D startPosition, HashMap<String, double[]> curveYields, boolean useFiniteDifferenceByDefault) {
    super(derivatives, marketRates, knownCurves, unknownCurveNodePoints, unknownCurveInterpolators, useFiniteDifferenceByDefault);
    Validate.notNull(marketValueCalculator);
    Validate.notNull(marketValueSensitivityCalculator);
    Validate.notNull(marketRates);
    Validate.notNull(startPosition);
    Validate.notNull(curveYields);
    Validate.isTrue(getTotalNodes() == startPosition.getNumberOfElements());

    for (String name : getCurveNames()) {
      Validate.isTrue(getCurveNodePointsForCurve(name).length == curveYields.get(name).length);
    }

    _marketValueCalculator = marketValueCalculator;
    _marketValueSensitivityCalculator = marketValueSensitivityCalculator;
    _marketRates = marketRates;
    _startPosition = startPosition;
    _curveYields = curveYields;
  }

  /**
   * Gets the marketValueSensitivityCalculator field.
   * @return the marketValueSensitivityCalculator
   */
  public InstrumentDerivativeVisitor<YieldCurveBundle, Map<String, List<DoublesPair>>> getMarketValueSensitivityCalculator() {
    return _marketValueSensitivityCalculator;
  }

  /**
   * Sets the marketValueSensitivityCalculator field.
   * @param marketValueSensitivityCalculator  the marketValueSensitivityCalculator
   */
  public void setMarketValueSensitivityCalculator(InstrumentDerivativeVisitor<YieldCurveBundle, Map<String, List<DoublesPair>>> marketValueSensitivityCalculator) {
    _marketValueSensitivityCalculator = marketValueSensitivityCalculator;
  }

  /**
   * Gets the marketRates field.
   * @return the marketRates
   */
  public double[] getMarketRates() {
    return _marketRates;
  }

  /**
   * Sets the marketRates field.
   * @param marketRates  the marketRates
   */
  public void setMarketRates(double[] marketRates) {
    _marketRates = marketRates;
  }

  /**
   * Gets the startPosition field.
   * @return the startPosition
   */
  public DoubleMatrix1D getStartPosition() {
    return _startPosition;
  }

  /**
   * Sets the startPosition field.
   * @param startPosition  the startPosition
   */
  public void setStartPosition(DoubleMatrix1D startPosition) {
    _startPosition = startPosition;
  }

  /**
   * Gets the marketValueCalculator field.
   * @return the marketValueCalculator
   */
  public InstrumentDerivativeVisitor<YieldCurveBundle, Double> getMarketValueCalculator() {
    return _marketValueCalculator;
  }

  /**
   * Sets the marketValueCalculator field.
   * @param marketValueCalculator  the marketValueCalculator
   */
  public void setMarketValueCalculator(InstrumentDerivativeVisitor<YieldCurveBundle, Double> marketValueCalculator) {
    _marketValueCalculator = marketValueCalculator;
  }

}
