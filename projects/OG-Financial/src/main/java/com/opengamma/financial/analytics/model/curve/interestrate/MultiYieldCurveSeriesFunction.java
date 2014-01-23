/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.curve.interestrate;

import static com.opengamma.engine.value.ValuePropertyNames.CURVE;
import static com.opengamma.engine.value.ValuePropertyNames.CURVE_CALCULATION_CONFIG;
import static com.opengamma.engine.value.ValueRequirementNames.YIELD_CURVE;
import static com.opengamma.engine.value.ValueRequirementNames.YIELD_CURVE_CONVERSION_HISTORICAL_TIME_SERIES;
import static com.opengamma.engine.value.ValueRequirementNames.YIELD_CURVE_HISTORICAL_TIME_SERIES;
import static com.opengamma.engine.value.ValueRequirementNames.YIELD_CURVE_SERIES;
import static com.opengamma.engine.value.ValueRequirementNames.YIELD_CURVE_SPEC;
import static com.opengamma.financial.analytics.model.curve.interestrate.MultiYieldCurvePropertiesAndDefaults.PROPERTY_DECOMPOSITION;
import static com.opengamma.financial.analytics.model.curve.interestrate.MultiYieldCurvePropertiesAndDefaults.PROPERTY_ROOT_FINDER_ABSOLUTE_TOLERANCE;
import static com.opengamma.financial.analytics.model.curve.interestrate.MultiYieldCurvePropertiesAndDefaults.PROPERTY_ROOT_FINDER_MAX_ITERATIONS;
import static com.opengamma.financial.analytics.model.curve.interestrate.MultiYieldCurvePropertiesAndDefaults.PROPERTY_ROOT_FINDER_RELATIVE_TOLERANCE;
import static com.opengamma.financial.analytics.model.curve.interestrate.MultiYieldCurvePropertiesAndDefaults.PROPERTY_USE_FINITE_DIFFERENCE;
import static com.opengamma.financial.analytics.timeseries.HistoricalTimeSeriesFunctionUtils.DATA_FIELD_PROPERTY;
import static com.opengamma.financial.analytics.timeseries.HistoricalTimeSeriesFunctionUtils.END_DATE_PROPERTY;
import static com.opengamma.financial.analytics.timeseries.HistoricalTimeSeriesFunctionUtils.INCLUDE_END_PROPERTY;
import static com.opengamma.financial.analytics.timeseries.HistoricalTimeSeriesFunctionUtils.INCLUDE_START_PROPERTY;
import static com.opengamma.financial.analytics.timeseries.HistoricalTimeSeriesFunctionUtils.RESOLUTION_KEY_PROPERTY;
import static com.opengamma.financial.analytics.timeseries.HistoricalTimeSeriesFunctionUtils.START_DATE_PROPERTY;
import static com.opengamma.financial.analytics.timeseries.HistoricalTimeSeriesFunctionUtils.YES_VALUE;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.OpenGammaCompilationContext;
import com.opengamma.financial.analytics.ircurve.InterpolatedYieldCurveSpecificationWithSecurities;
import com.opengamma.financial.analytics.ircurve.YieldCurveDefinition;
import com.opengamma.financial.analytics.ircurve.calcconfig.ConfigDBCurveCalculationConfigSource;
import com.opengamma.financial.analytics.ircurve.calcconfig.MultiCurveCalculationConfig;
import com.opengamma.financial.analytics.model.curve.MultiCurveFunction;
import com.opengamma.financial.analytics.timeseries.HistoricalTimeSeriesBundle;

/**
 * Base class for functions that construct yield curves and the Jacobian from {@link YieldCurveDefinition}
 * and {@link MultiCurveCalculationConfig}s using root-finding.
 * @deprecated This function uses configuration objects that have been superseded. Use functions that
 * descend from {@link MultiCurveFunction}
 */
@Deprecated
public abstract class MultiYieldCurveSeriesFunction extends AbstractFunction.NonCompiledInvoker {
  /** The logger */
  private static final Logger s_logger = LoggerFactory.getLogger(MultiYieldCurveSeriesFunction.class);

  @Override
  public void init(final FunctionCompilationContext context) {
    ConfigDBCurveCalculationConfigSource.reinitOnChanges(context, this);
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.CURRENCY;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final ValueProperties curveProperties = getCurveSeriesProperties();
    final ValueSpecification curve = new ValueSpecification(YIELD_CURVE_SERIES, target.toSpecification(), curveProperties);
    return Sets.newHashSet(curve);
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    ValueProperties.Builder seriesConstraints = null;
    final ValueProperties constraints = desiredValue.getConstraints();
    Set<String> values = desiredValue.getConstraints().getValues(DATA_FIELD_PROPERTY);
    if ((values == null) || values.isEmpty()) {
      seriesConstraints = desiredValue.getConstraints().copy().with(DATA_FIELD_PROPERTY, MarketDataRequirementNames.MARKET_VALUE);
    } else if (values.size() > 1) {
      seriesConstraints = desiredValue.getConstraints().copy().withoutAny(DATA_FIELD_PROPERTY)
          .with(DATA_FIELD_PROPERTY, values.iterator().next());
    }
    values = desiredValue.getConstraints().getValues(RESOLUTION_KEY_PROPERTY);
    if ((values == null) || values.isEmpty()) {
      if (seriesConstraints == null) {
        seriesConstraints = desiredValue.getConstraints().copy();
      }
      seriesConstraints.with(RESOLUTION_KEY_PROPERTY, "");
    } else if (values.size() > 1) {
      if (seriesConstraints == null) {
        seriesConstraints = desiredValue.getConstraints().copy();
      }
      seriesConstraints.withoutAny(RESOLUTION_KEY_PROPERTY).with(RESOLUTION_KEY_PROPERTY, values.iterator().next());
    }
    values = desiredValue.getConstraints().getValues(START_DATE_PROPERTY);
    if ((values == null) || values.isEmpty()) {
      if (seriesConstraints == null) {
        seriesConstraints = desiredValue.getConstraints().copy();
      }
      seriesConstraints.with(START_DATE_PROPERTY, "Null");
    }
    values = desiredValue.getConstraints().getValues(INCLUDE_START_PROPERTY);
    if ((values == null) || (values.size() != 1)) {
      if (seriesConstraints == null) {
        seriesConstraints = desiredValue.getConstraints().copy();
      }
      seriesConstraints.with(INCLUDE_START_PROPERTY, YES_VALUE);
    }
    values = desiredValue.getConstraints().getValues(END_DATE_PROPERTY);
    if ((values == null) || values.isEmpty()) {
      if (seriesConstraints == null) {
        seriesConstraints = desiredValue.getConstraints().copy();
      }
      seriesConstraints.with(END_DATE_PROPERTY, "Now");
    }
    values = desiredValue.getConstraints().getValues(INCLUDE_END_PROPERTY);
    if ((values == null) || (values.size() != 1)) {
      if (seriesConstraints == null) {
        seriesConstraints = desiredValue.getConstraints().copy();
      }
      seriesConstraints.with(INCLUDE_END_PROPERTY, YES_VALUE);
    }
    if (seriesConstraints != null) {
      Set<String> propertyValue = constraints.getValues(PROPERTY_ROOT_FINDER_ABSOLUTE_TOLERANCE);
      if (propertyValue == null) {
        seriesConstraints.withAny(PROPERTY_ROOT_FINDER_ABSOLUTE_TOLERANCE);
      } else {
        seriesConstraints.with(PROPERTY_ROOT_FINDER_ABSOLUTE_TOLERANCE, propertyValue);
      }
      propertyValue = constraints.getValues(PROPERTY_ROOT_FINDER_RELATIVE_TOLERANCE);
      if (propertyValue == null) {
        seriesConstraints.withAny(PROPERTY_ROOT_FINDER_RELATIVE_TOLERANCE);
      } else {
        seriesConstraints.with(PROPERTY_ROOT_FINDER_RELATIVE_TOLERANCE, propertyValue);
      }
      propertyValue = constraints.getValues(PROPERTY_ROOT_FINDER_MAX_ITERATIONS);
      if (propertyValue == null) {
        seriesConstraints.withAny(PROPERTY_ROOT_FINDER_MAX_ITERATIONS);
      } else {
        seriesConstraints.with(PROPERTY_ROOT_FINDER_MAX_ITERATIONS, propertyValue);
      }
      propertyValue = constraints.getValues(PROPERTY_DECOMPOSITION);
      if (propertyValue == null) {
        seriesConstraints.withAny(PROPERTY_DECOMPOSITION);
      } else {
        seriesConstraints.with(PROPERTY_DECOMPOSITION, propertyValue);
      }
      propertyValue = constraints.getValues(PROPERTY_USE_FINITE_DIFFERENCE);
      if (propertyValue == null) {
        seriesConstraints.withAny(PROPERTY_USE_FINITE_DIFFERENCE);
      } else {
        seriesConstraints.with(PROPERTY_USE_FINITE_DIFFERENCE, propertyValue);
      }
      return Collections.singleton(new ValueRequirement(YIELD_CURVE_SERIES, target.toSpecification(), seriesConstraints.get()));
    }
    final Set<String> curveCalculationConfigNames = constraints.getValues(ValuePropertyNames.CURVE_CALCULATION_CONFIG);
    if (curveCalculationConfigNames == null || curveCalculationConfigNames.size() != 1) {
      return null;
    }
    final String curveCalculationConfigName = Iterables.getOnlyElement(curveCalculationConfigNames);
    final ConfigSource configSource = OpenGammaCompilationContext.getConfigSource(context);
    final ConfigDBCurveCalculationConfigSource curveCalculationConfigSource = new ConfigDBCurveCalculationConfigSource(configSource);
    final MultiCurveCalculationConfig curveCalculationConfig = curveCalculationConfigSource.getConfig(curveCalculationConfigName);
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
    final Set<String> rootFinderAbsoluteTolerance = constraints.getValues(PROPERTY_ROOT_FINDER_ABSOLUTE_TOLERANCE);
    if (rootFinderAbsoluteTolerance == null || rootFinderAbsoluteTolerance.size() != 1) {
      return null;
    }
    final Set<String> rootFinderRelativeTolerance = constraints.getValues(PROPERTY_ROOT_FINDER_RELATIVE_TOLERANCE);
    if (rootFinderRelativeTolerance == null || rootFinderRelativeTolerance.size() != 1) {
      return null;
    }
    final Set<String> maxIterations = constraints.getValues(PROPERTY_ROOT_FINDER_MAX_ITERATIONS);
    if (maxIterations == null || maxIterations.size() != 1) {
      return null;
    }
    final Set<String> decomposition = constraints.getValues(PROPERTY_DECOMPOSITION);
    if (decomposition == null || decomposition.size() != 1) {
      return null;
    }
    final Set<String> useFiniteDifference = constraints.getValues(PROPERTY_USE_FINITE_DIFFERENCE);
    if (useFiniteDifference == null || useFiniteDifference.size() != 1) {
      return null;
    }
    final String[] curveNames = curveCalculationConfig.getYieldCurveNames();
    final String absoluteTolerance = Iterables.getOnlyElement(rootFinderAbsoluteTolerance);
    final String relativeTolerance = Iterables.getOnlyElement(rootFinderRelativeTolerance);
    final String maxIteration = Iterables.getOnlyElement(maxIterations);
    final String finiteDifference = Iterables.getOnlyElement(useFiniteDifference);
    final Set<ValueRequirement> requirements = new HashSet<>();
    final ComputationTargetSpecification targetSpec = target.toSpecification();
    for (final String curveName : curveNames) {
      final ValueProperties properties = ValueProperties.builder()
          .with(CURVE, curveName)
          .with(CURVE_CALCULATION_CONFIG, curveCalculationConfigName).withOptional(CURVE_CALCULATION_CONFIG).get();
      final ValueProperties curveTSProperties = ValueProperties.builder()
          .with(CURVE, curveName)
          .with(DATA_FIELD_PROPERTY, constraints.getValues(DATA_FIELD_PROPERTY))
          .with(RESOLUTION_KEY_PROPERTY, constraints.getValues(RESOLUTION_KEY_PROPERTY))
          .with(START_DATE_PROPERTY, constraints.getValues(START_DATE_PROPERTY))
          .with(INCLUDE_START_PROPERTY, constraints.getValues(INCLUDE_START_PROPERTY))
          .with(END_DATE_PROPERTY, constraints.getValues(END_DATE_PROPERTY))
          .with(INCLUDE_END_PROPERTY, constraints.getValues(INCLUDE_END_PROPERTY))
          .get();
      requirements.add(new ValueRequirement(YIELD_CURVE_HISTORICAL_TIME_SERIES, targetSpec, properties));
      requirements.add(new ValueRequirement(YIELD_CURVE_SPEC, targetSpec, properties));
      requirements.add(new ValueRequirement(YIELD_CURVE_CONVERSION_HISTORICAL_TIME_SERIES, targetSpec, curveTSProperties));
      requirements.add(new ValueRequirement(YIELD_CURVE_HISTORICAL_TIME_SERIES, targetSpec, curveTSProperties));
    }
    if (curveCalculationConfig.getExogenousConfigData() != null) {
      final LinkedHashMap<String, String[]> exogenousCurveConfigs = curveCalculationConfig.getExogenousConfigData();
      for (final Map.Entry<String, String[]> entry : exogenousCurveConfigs.entrySet()) {
        for (final String exogenousCurveName : entry.getValue()) {
          final ValueProperties properties = constraints.copy()
              .withoutAny(CURVE_CALCULATION_CONFIG)
              .with(CURVE_CALCULATION_CONFIG, entry.getKey())
              .withoutAny(CURVE)
              .with(CURVE, exogenousCurveName)
              .get();
          requirements.add(new ValueRequirement(YIELD_CURVE_SERIES, targetSpec, properties));
        }
      }
    }
    return requirements;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target, final Map<ValueSpecification, ValueRequirement> inputs) {
    if (inputs.size() == 1) {
      final ValueSpecification input = Iterables.getOnlyElement(inputs.entrySet()).getKey();
      if (YIELD_CURVE_SERIES.equals(input.getValueName())) {
        // Use the substituted result
        return Collections.singleton(input);
      }
    }
    final Set<ValueSpecification> results = new HashSet<>();
    final ComputationTargetSpecification targetSpec = target.toSpecification();
    String curveCalculationConfigName = null;
    for (final Map.Entry<ValueSpecification, ValueRequirement> entry : inputs.entrySet()) {
      final ValueRequirement value = entry.getValue();
      if (value.getValueName().equals(YIELD_CURVE_SPEC)) {
        if (curveCalculationConfigName == null) {
          curveCalculationConfigName = value.getConstraint(CURVE_CALCULATION_CONFIG);
        } else {
          if (!value.getConstraint(CURVE_CALCULATION_CONFIG).equals(curveCalculationConfigName)) {
            throw new OpenGammaRuntimeException("Had different curve calculation configuration names; should never happen");
          }
        }
        final String curveName = value.getConstraint(CURVE);
        final ValueProperties curveProperties = getCurveSeriesProperties(curveCalculationConfigName, curveName);
        final ValueSpecification spec = new ValueSpecification(YIELD_CURVE_SERIES, targetSpec, curveProperties);
        results.add(spec);
      }
    }
    if (curveCalculationConfigName == null) {
      return null;
    }
    return results;
  }

  /**
   * Gets the yield curve properties with no values set.
   * @return The properties for the curve
   */
  protected abstract ValueProperties getCurveSeriesProperties();

  /**
   * Gets properties for a single yield curve with the curve calculation configuration name and curve.
   * name set.
   * @param curveCalculationConfigName The curve calculation configuration name
   * @param curveName The curve name
   * @return The properties for the curve
   */
  protected abstract ValueProperties getCurveSeriesProperties(final String curveCalculationConfigName, final String curveName);

  /**
   * Gets the curve calculation method.
   * @return The curve calculation method
   */
  protected abstract String getCalculationMethod();

  /**
   * Gets the snapshot containing the market data from the function inputs.
   * @param inputs The inputs
   * @param targetSpec The specification of the market data
   * @param curveName The curve name
   * @return The market data snapshot
   * @throws OpenGammaRuntimeException if the snapshot is not present in the inputs
   */
  protected HistoricalTimeSeriesBundle getHistoricalMarketData(final FunctionInputs inputs, final ComputationTargetSpecification targetSpec, final String curveName) {
    final ValueRequirement marketDataRequirement = new ValueRequirement(YIELD_CURVE_HISTORICAL_TIME_SERIES, targetSpec, ValueProperties.with(CURVE, curveName).get());
    final Object marketDataObject = inputs.getValue(marketDataRequirement);
    if (marketDataObject == null) {
      throw new OpenGammaRuntimeException("Could not get a value for requirement " + marketDataRequirement);
    }
    return (HistoricalTimeSeriesBundle) marketDataObject;
  }

  /**
   * Gets the bundle containing historical fixing from the function inputs.
   * @param inputs The inputs
   * @param targetSpec The specification of the historical data
   * @param curveName The curve name
   * @return The bundle
   * @throws OpenGammaRuntimeException if the bundle is not present in the inputs
   */
  protected HistoricalTimeSeriesBundle getTimeSeriesBundle(final FunctionInputs inputs, final ComputationTargetSpecification targetSpec, final String curveName) {
    final ValueRequirement timeSeriesRequirement = new ValueRequirement(YIELD_CURVE_CONVERSION_HISTORICAL_TIME_SERIES, targetSpec, ValueProperties.with(
        CURVE, curveName).get());
    final Object timeSeriesObject = inputs.getValue(timeSeriesRequirement);
    if (timeSeriesObject == null) {
      throw new OpenGammaRuntimeException("Could not get conversion time series for requirement " + timeSeriesRequirement);
    }
    return (HistoricalTimeSeriesBundle) timeSeriesObject;
  }

  /**
   * Gets the interpolated yield curve specifications from the function inputs
   * @param inputs The inputs
   * @param targetSpec The specification of the interpolated yield curve
   * @param curveName The curve name
   * @return The specification
   * @throws OpenGammaRuntimeException if the specification is not present in the inputs
   */
  protected InterpolatedYieldCurveSpecificationWithSecurities getYieldCurveSpecification(final FunctionInputs inputs,
      final ComputationTargetSpecification targetSpec, final String curveName) {
    final ValueRequirement specRequirement = new ValueRequirement(YIELD_CURVE_SPEC, targetSpec, ValueProperties.with(CURVE, curveName).get());
    final Object specObject = inputs.getValue(specRequirement);
    if (specObject == null) {
      return null;
    }
    return (InterpolatedYieldCurveSpecificationWithSecurities) specObject;
  }

  /**
   * Gets any known (i.e. exogenous) curves from the function inputs. These curves are held
   * fixed during fitting.
   * @param curveCalculationConfig The curve calculation configuration
   * @param targetSpec The specification of the known curves
   * @param inputs The inputs
   * @return A yield curve bundle containing the curves or null if none of the curves are known before fitting
   * @throws OpenGammaRuntimeException If an exogenous curve is required but is not present in the inputs
   */
  protected YieldCurveBundle getKnownCurves(final MultiCurveCalculationConfig curveCalculationConfig, final ComputationTargetSpecification targetSpec,
      final FunctionInputs inputs) {
    YieldCurveBundle knownCurves = null;
    if (curveCalculationConfig.getExogenousConfigData() != null) {
      knownCurves = new YieldCurveBundle();
      final LinkedHashMap<String, String[]> exogenousCurveNames = curveCalculationConfig.getExogenousConfigData();
      for (final Map.Entry<String, String[]> entry : exogenousCurveNames.entrySet()) {
        for (final String exogenousCurveName : entry.getValue()) {
          final ValueProperties properties = ValueProperties.builder()
              .with(CURVE, exogenousCurveName).get();
          final Object exogenousCurveObject = inputs.getValue(new ValueRequirement(YIELD_CURVE, targetSpec, properties));
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

}
