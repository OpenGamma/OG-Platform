/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.rootfinding;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.interestrate.MultipleYieldCurveFinderDataBundle;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.lambdava.tuple.DoublesPair;

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
  public void setTestType(final TestType testType) {
    _testType = testType;
  }

  public YieldCurveFittingTestDataBundle(final List<InstrumentDerivative> derivatives, final YieldCurveBundle knownCurves, final LinkedHashMap<String, double[]> unknownCurveNodePoints,
      final LinkedHashMap<String, Interpolator1D> unknownCurveInterpolators,
      final InstrumentDerivativeVisitor<YieldCurveBundle, Double> marketValueCalculator, final InstrumentDerivativeVisitor<YieldCurveBundle, Map<String, List<DoublesPair>>> marketValueSensitivityCalculator,
      final double[] marketRates, final DoubleMatrix1D startPosition, final boolean useFiniteDifferenceByDefault, final FXMatrix fxMatrix) {
    super(derivatives, marketRates, knownCurves, unknownCurveNodePoints, unknownCurveInterpolators, useFiniteDifferenceByDefault, fxMatrix);
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
  public void setCurveYields(final HashMap<String, double[]> curveYields) {
    _curveYields = curveYields;
  }

  public YieldCurveFittingTestDataBundle(final List<InstrumentDerivative> derivatives, final YieldCurveBundle knownCurves, final LinkedHashMap<String, double[]> unknownCurveNodePoints,
      final LinkedHashMap<String, Interpolator1D> unknownCurveInterpolators,
      final InstrumentDerivativeVisitor<YieldCurveBundle, Double> marketValueCalculator, final InstrumentDerivativeVisitor<YieldCurveBundle, Map<String, List<DoublesPair>>> marketValueSensitivityCalculator,
      final double[] marketRates, final DoubleMatrix1D startPosition, final HashMap<String, double[]> curveYields, final boolean useFiniteDifferenceByDefault, final FXMatrix fxMatrix) {
    super(derivatives, marketRates, knownCurves, unknownCurveNodePoints, unknownCurveInterpolators, useFiniteDifferenceByDefault, fxMatrix);
    Validate.notNull(marketValueCalculator);
    Validate.notNull(marketValueSensitivityCalculator);
    Validate.notNull(marketRates);
    Validate.notNull(startPosition);
    Validate.notNull(curveYields);
    Validate.isTrue(getTotalNodes() == startPosition.getNumberOfElements());

    for (final String name : getCurveNames()) {
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
  public void setMarketValueSensitivityCalculator(final InstrumentDerivativeVisitor<YieldCurveBundle, Map<String, List<DoublesPair>>> marketValueSensitivityCalculator) {
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
  public void setMarketRates(final double[] marketRates) {
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
  public void setStartPosition(final DoubleMatrix1D startPosition) {
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
  public void setMarketValueCalculator(final InstrumentDerivativeVisitor<YieldCurveBundle, Double> marketValueCalculator) {
    _marketValueCalculator = marketValueCalculator;
  }

}
