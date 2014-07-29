/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.curve.interestrate;

import static com.opengamma.financial.analytics.model.curve.interestrate.MultiYieldCurvePropertiesAndDefaults.PROPERTY_DECOMPOSITION;
import static com.opengamma.financial.analytics.model.curve.interestrate.MultiYieldCurvePropertiesAndDefaults.PROPERTY_ROOT_FINDER_ABSOLUTE_TOLERANCE;
import static com.opengamma.financial.analytics.model.curve.interestrate.MultiYieldCurvePropertiesAndDefaults.PROPERTY_ROOT_FINDER_MAX_ITERATIONS;
import static com.opengamma.financial.analytics.model.curve.interestrate.MultiYieldCurvePropertiesAndDefaults.PROPERTY_ROOT_FINDER_RELATIVE_TOLERANCE;
import static com.opengamma.financial.analytics.model.curve.interestrate.MultiYieldCurvePropertiesAndDefaults.PROPERTY_USE_FINITE_DIFFERENCE;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.ircurve.InterpolatedYieldCurveSpecificationWithSecurities;
import com.opengamma.financial.analytics.ircurve.YieldCurveData;
import com.opengamma.financial.analytics.ircurve.YieldCurveDefinition;
import com.opengamma.financial.analytics.ircurve.calcconfig.ConfigDBCurveCalculationConfigSource;
import com.opengamma.financial.analytics.ircurve.calcconfig.MultiCurveCalculationConfig;
import com.opengamma.financial.analytics.model.curve.MultiCurveFunction;
import com.opengamma.financial.analytics.timeseries.HistoricalTimeSeriesBundle;

/**
 * Base class for functions that construct yield curves and the Jacobian from {@link YieldCurveDefinition} and {@link MultiCurveCalculationConfig}s using root-finding.
 * 
 * @deprecated This function uses configuration objects that have been superseded. Use functions that descend from {@link MultiCurveFunction}
 */
@Deprecated
public abstract class MultiYieldCurveFunction extends AbstractFunction.NonCompiledInvoker {
  /** The logger */
  private static final Logger s_logger = LoggerFactory.getLogger(MultiYieldCurveFunction.class);

  private ConfigDBCurveCalculationConfigSource _curveCalculationConfigSource;

  @Override
  public void init(final FunctionCompilationContext context) {
    _curveCalculationConfigSource = ConfigDBCurveCalculationConfigSource.init(context, this);
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.CURRENCY;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final ValueProperties curveProperties = getCurveProperties();
    final ValueProperties properties = getJacobianProperties();
    final ValueSpecification curve = new ValueSpecification(ValueRequirementNames.YIELD_CURVE, target.toSpecification(), curveProperties);
    final ValueSpecification jacobian = new ValueSpecification(ValueRequirementNames.YIELD_CURVE_JACOBIAN, target.toSpecification(), properties);
    return Sets.newHashSet(curve, jacobian);
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final ValueProperties constraints = desiredValue.getConstraints();
    final String curveCalculationConfigName = constraints.getStrictValue(ValuePropertyNames.CURVE_CALCULATION_CONFIG);
    if (curveCalculationConfigName == null) {
      return null;
    }
    final MultiCurveCalculationConfig curveCalculationConfig = _curveCalculationConfigSource.getConfig(curveCalculationConfigName);
    if (curveCalculationConfig == null) {
      s_logger.error("Could not find curve calculation configuration named {}", curveCalculationConfigName);
      return null;
    }
    if (!curveCalculationConfig.getCalculationMethod().equals(getCalculationMethod())) {
      return null;
    }
    if (!curveCalculationConfig.getTarget().equals(target.toSpecification())) {
      s_logger.warn("Invalid target for {}, was {} - expected {}", curveCalculationConfigName, target, curveCalculationConfig.getTarget());
      return null;
    }
    final String absoluteTolerance = constraints.getStrictValue(PROPERTY_ROOT_FINDER_ABSOLUTE_TOLERANCE);
    if (absoluteTolerance == null) {
      return null;
    }
    final String relativeTolerance = constraints.getStrictValue(PROPERTY_ROOT_FINDER_RELATIVE_TOLERANCE);
    if (relativeTolerance == null) {
      return null;
    }
    final String maxIteration = constraints.getStrictValue(PROPERTY_ROOT_FINDER_MAX_ITERATIONS);
    if (maxIteration == null) {
      return null;
    }
    final String decomposition = constraints.getStrictValue(PROPERTY_DECOMPOSITION);
    if (decomposition == null) {
      return null;
    }
    final String finiteDifference = constraints.getStrictValue(PROPERTY_USE_FINITE_DIFFERENCE);
    if (finiteDifference == null) {
      return null;
    }
    final String[] curveNames = curveCalculationConfig.getYieldCurveNames();
    final Set<ValueRequirement> requirements = new HashSet<>();
    final ComputationTargetSpecification targetSpec = target.toSpecification();
    for (final String curveName : curveNames) {
      final ValueProperties properties = ValueProperties.builder().with(ValuePropertyNames.CURVE, curveName).with(ValuePropertyNames.CURVE_CALCULATION_CONFIG, curveCalculationConfigName)
          .withOptional(ValuePropertyNames.CURVE_CALCULATION_CONFIG).get();
      final ValueProperties curveTSProperties = ValueProperties.builder().with(ValuePropertyNames.CURVE_CALCULATION_CONFIG, curveCalculationConfigName).get();
      requirements.add(new ValueRequirement(ValueRequirementNames.YIELD_CURVE_DATA, targetSpec, properties));
      requirements.add(new ValueRequirement(ValueRequirementNames.YIELD_CURVE_SPEC, targetSpec, properties));
      requirements.add(new ValueRequirement(ValueRequirementNames.YIELD_CURVE_INSTRUMENT_CONVERSION_HISTORICAL_TIME_SERIES, targetSpec, curveTSProperties));
    }
    if (curveCalculationConfig.getExogenousConfigData() != null) {
      final LinkedHashMap<String, String[]> exogenousCurveConfigs = curveCalculationConfig.getExogenousConfigData();
      for (final Map.Entry<String, String[]> entry : exogenousCurveConfigs.entrySet()) {
        for (final String exogenousCurveName : entry.getValue()) {
          final ValueProperties properties = ValueProperties.builder().with(ValuePropertyNames.CURVE_CALCULATION_CONFIG, entry.getKey()).with(ValuePropertyNames.CURVE, exogenousCurveName)
              .with(PROPERTY_DECOMPOSITION, decomposition).with(PROPERTY_ROOT_FINDER_ABSOLUTE_TOLERANCE, absoluteTolerance).with(PROPERTY_ROOT_FINDER_MAX_ITERATIONS, maxIteration)
              .with(PROPERTY_ROOT_FINDER_RELATIVE_TOLERANCE, relativeTolerance).with(PROPERTY_USE_FINITE_DIFFERENCE, finiteDifference).get();
          requirements.add(new ValueRequirement(ValueRequirementNames.YIELD_CURVE, targetSpec, properties));
        }
      }
    }
    return requirements;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target, final Map<ValueSpecification, ValueRequirement> inputs) {
    final Set<ValueSpecification> results = new HashSet<>();
    final ComputationTargetSpecification targetSpec = target.toSpecification();
    String curveCalculationConfigName = null;
    for (final Map.Entry<ValueSpecification, ValueRequirement> entry : inputs.entrySet()) {
      final ValueRequirement value = entry.getValue();
      if (value.getValueName().equals(ValueRequirementNames.YIELD_CURVE_SPEC)) {
        if (curveCalculationConfigName == null) {
          curveCalculationConfigName = value.getConstraint(ValuePropertyNames.CURVE_CALCULATION_CONFIG);
        } else {
          if (!value.getConstraint(ValuePropertyNames.CURVE_CALCULATION_CONFIG).equals(curveCalculationConfigName)) {
            throw new OpenGammaRuntimeException("Had different curve calculation configuration names; should never happen");
          }
        }
        final String curveName = value.getConstraint(ValuePropertyNames.CURVE);
        final ValueProperties curveProperties = getCurveProperties(curveCalculationConfigName, curveName);
        final ValueSpecification spec = new ValueSpecification(ValueRequirementNames.YIELD_CURVE, targetSpec, curveProperties);
        results.add(spec);
      }
    }
    if (curveCalculationConfigName == null) {
      return null;
    }
    final ValueProperties properties = getJacobianProperties(curveCalculationConfigName);
    final ValueSpecification jacobian = new ValueSpecification(ValueRequirementNames.YIELD_CURVE_JACOBIAN, targetSpec, properties);
    results.add(jacobian);
    return results;
  }

  /**
   * Gets the Jacobian properties with no values set.
   * 
   * @return The properties for the Jacobian
   */
  protected abstract ValueProperties getJacobianProperties();

  /**
   * Gets the yield curve properties with no values set.
   * 
   * @return The properties for the curve
   */
  protected abstract ValueProperties getCurveProperties();

  /**
   * Gets the Jacobian properties with the curve calculation configuration name set.
   * 
   * @param curveCalculationConfigName The curve calculation configuration name
   * @return The properties for the Jacobian
   */
  protected abstract ValueProperties getJacobianProperties(final String curveCalculationConfigName);

  /**
   * Gets properties for a single yield curve with the curve calculation configuration name and curve. name set.
   * 
   * @param curveCalculationConfigName The curve calculation configuration name
   * @param curveName The curve name
   * @return The properties for the curve
   */
  protected abstract ValueProperties getCurveProperties(final String curveCalculationConfigName, final String curveName);

  /**
   * Gets the Jacobian properties with all values set.
   * 
   * @param curveCalculationConfigName The curve calculation configuration name
   * @param absoluteTolerance The absolute tolerance
   * @param relativeTolerance The relative tolerance
   * @param maxIterations The maximum number of iterations
   * @param decomposition The decomposition
   * @param useFiniteDifference True if finite difference was used to calculate the derivative
   * @return The properties for the Jacobian
   */
  protected abstract ValueProperties getJacobianProperties(final String curveCalculationConfigName, final String absoluteTolerance, final String relativeTolerance,
      final String maxIterations, final String decomposition, final String useFiniteDifference);

  /**
   * Gets properties for a single yield curve all values set. name set.
   * 
   * @param curveCalculationConfigName The curve calculation configuration name
   * @param curveName The curve name
   * @param absoluteTolerance The absolute tolerance
   * @param relativeTolerance The relative tolerance
   * @param maxIterations The maximum number of iterations
   * @param decomposition The decomposition
   * @param useFiniteDifference True if finite difference was used to calculate the derivative
   * @return The properties for the curve
   */
  protected abstract ValueProperties getCurveProperties(final String curveCalculationConfigName, final String curveName, final String absoluteTolerance, final String relativeTolerance,
      final String maxIterations, final String decomposition, final String useFiniteDifference);

  /**
   * Gets the curve calculation method.
   * 
   * @return The curve calculation method
   */
  protected abstract String getCalculationMethod();

  /**
   * Gets the snapshot containing the market data from the function inputs.
   * 
   * @param inputs The inputs
   * @param targetSpec The specification of the market data
   * @param curveName The curve name
   * @return The market data snapshot
   * @throws OpenGammaRuntimeException if the snapshot is not present in the inputs
   */
  protected YieldCurveData getMarketData(final FunctionInputs inputs, final ComputationTargetSpecification targetSpec, final String curveName) {
    ValueProperties constraints = ValueProperties.with(ValuePropertyNames.CURVE, curveName).get();
    final ValueRequirement marketDataRequirement =
        new ValueRequirement(ValueRequirementNames.YIELD_CURVE_DATA, targetSpec, constraints);
    final Object marketDataObject = inputs.getValue(marketDataRequirement);
    if (marketDataObject == null) {
      throw new OpenGammaRuntimeException("Could not get a value for requirement " + marketDataRequirement);
    }
    return (YieldCurveData) marketDataObject;
  }

  /**
   * Gets the bundle containing historical fixing from the function inputs.
   * 
   * @param inputs The inputs
   * @param targetSpec The specification of the historical data
   * @param curveCalculationConfigName The curve calculation configuration name
   * @return The bundle
   * @throws OpenGammaRuntimeException if the bundle is not present in the inputs
   */
  protected HistoricalTimeSeriesBundle getTimeSeriesBundle(final FunctionInputs inputs, final ComputationTargetSpecification targetSpec, final String curveCalculationConfigName) {
    final ValueRequirement timeSeriesRequirement = new ValueRequirement(ValueRequirementNames.YIELD_CURVE_INSTRUMENT_CONVERSION_HISTORICAL_TIME_SERIES, targetSpec, ValueProperties.with(
        ValuePropertyNames.CURVE_CALCULATION_CONFIG, curveCalculationConfigName).get());
    final Object timeSeriesObject = inputs.getValue(timeSeriesRequirement);
    if (timeSeriesObject == null) {
      throw new OpenGammaRuntimeException("Could not get conversion time series for requirement " + timeSeriesRequirement);
    }
    return (HistoricalTimeSeriesBundle) timeSeriesObject;
  }

  /**
   * Gets the interpolated yield curve specification from the function inputs
   * 
   * @param inputs The inputs
   * @param targetSpec The specification of the interpolated yield curve
   * @param curveName The curve name
   * @return The specification
   * @throws OpenGammaRuntimeException if the specification is not present in the inputs
   */
  protected InterpolatedYieldCurveSpecificationWithSecurities getYieldCurveSpecification(final FunctionInputs inputs, final ComputationTargetSpecification targetSpec, final String curveName) {
    final ValueRequirement specRequirement = new ValueRequirement(ValueRequirementNames.YIELD_CURVE_SPEC, targetSpec, ValueProperties.with(ValuePropertyNames.CURVE, curveName).get());
    final Object specObject = inputs.getValue(specRequirement);
    if (specObject == null) {
      return null;
    }
    final InterpolatedYieldCurveSpecificationWithSecurities spec = (InterpolatedYieldCurveSpecificationWithSecurities) specObject;
    return spec;
  }

  /**
   * Gets any known (i.e. exogenous) curves from the function inputs. These curves are held fixed during fitting.
   * 
   * @param curveCalculationConfig The curve calculation configuration
   * @param targetSpec The specification of the known curves
   * @param inputs The inputs
   * @return A yield curve bundle containing the curves or null if none of the curves are known before fitting
   * @throws OpenGammaRuntimeException If an exogenous curve is required but is not present in the inputs
   */
  protected YieldCurveBundle getKnownCurves(final MultiCurveCalculationConfig curveCalculationConfig, final ComputationTargetSpecification targetSpec, final FunctionInputs inputs) {
    YieldCurveBundle knownCurves = null;
    if (curveCalculationConfig.getExogenousConfigData() != null) {
      knownCurves = new YieldCurveBundle();
      final LinkedHashMap<String, String[]> exogenousCurveNames = curveCalculationConfig.getExogenousConfigData();
      for (final Map.Entry<String, String[]> entry : exogenousCurveNames.entrySet()) {
        for (final String exogenousCurveName : entry.getValue()) {
          final ValueProperties properties = ValueProperties.builder().with(ValuePropertyNames.CURVE, exogenousCurveName).get();
          final Object exogenousCurveObject = inputs.getValue(new ValueRequirement(ValueRequirementNames.YIELD_CURVE, targetSpec, properties));
          if (exogenousCurveObject == null) {
            throw new OpenGammaRuntimeException("Could not get exogenous curve named " + exogenousCurveName);
          }
          final YieldAndDiscountCurve exogenousCurve = (YieldAndDiscountCurve) exogenousCurveObject;
          knownCurves.setCurve(exogenousCurveName, exogenousCurve);
        }
      }
    }
    return knownCurves;
  }

  protected ConfigDBCurveCalculationConfigSource getCurveCalculationConfigSource() {
    return _curveCalculationConfigSource;
  }

}
