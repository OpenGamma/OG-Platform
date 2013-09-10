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

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.core.marketdatasnapshot.SnapshotDataBundle;
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
import com.opengamma.financial.OpenGammaCompilationContext;
import com.opengamma.financial.analytics.ircurve.InterpolatedYieldCurveSpecificationWithSecurities;
import com.opengamma.financial.analytics.ircurve.calcconfig.ConfigDBCurveCalculationConfigSource;
import com.opengamma.financial.analytics.ircurve.calcconfig.MultiCurveCalculationConfig;
import com.opengamma.financial.analytics.timeseries.HistoricalTimeSeriesBundle;

/**
 *
 */
public abstract class MultiYieldCurveFunction extends AbstractFunction.NonCompiledInvoker {
  private static final Logger s_logger = LoggerFactory.getLogger(MultiYieldCurveFunction.class);

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
    final ValueProperties curveProperties = getCurveProperties();
    final ValueProperties properties = getJacobianProperties();
    final ValueSpecification curve = new ValueSpecification(ValueRequirementNames.YIELD_CURVE, target.toSpecification(), curveProperties);
    final ValueSpecification jacobian = new ValueSpecification(ValueRequirementNames.YIELD_CURVE_JACOBIAN, target.toSpecification(), properties);
    return Sets.newHashSet(curve, jacobian);
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final ValueProperties constraints = desiredValue.getConstraints();
    final Set<String> curveCalculationConfigNames = constraints.getValues(ValuePropertyNames.CURVE_CALCULATION_CONFIG);
    if (curveCalculationConfigNames == null || curveCalculationConfigNames.size() != 1) {
      return null;
    }
    final String curveCalculationConfigName = Iterables.getOnlyElement(curveCalculationConfigNames);
    final ConfigSource configSource = OpenGammaCompilationContext.getConfigSource(context);
    final ConfigDBCurveCalculationConfigSource curveCalculationConfigSource = new ConfigDBCurveCalculationConfigSource(configSource);
    final MultiCurveCalculationConfig curveCalculationConfig = curveCalculationConfigSource.getConfig(curveCalculationConfigName);
    if (curveCalculationConfig == null) {
      s_logger.error("Could not find curve calculation configuration named " + curveCalculationConfigName);
      return null;
    }
    if (!curveCalculationConfig.getCalculationMethod().equals(getCalculationMethod())) {
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
    final Set<ValueRequirement> requirements = new HashSet<ValueRequirement>();
    final ComputationTargetSpecification targetSpec = target.toSpecification();
    for (final String curveName : curveNames) {
      final ValueProperties properties = ValueProperties.builder()
          .with(ValuePropertyNames.CURVE, curveName)
          .with(ValuePropertyNames.CURVE_CALCULATION_CONFIG, curveCalculationConfigName).withOptional(ValuePropertyNames.CURVE_CALCULATION_CONFIG).get();
      final ValueProperties curveTSProperties = ValueProperties.builder()
          .with(ValuePropertyNames.CURVE_CALCULATION_CONFIG, curveCalculationConfigName).get();
      requirements.add(new ValueRequirement(ValueRequirementNames.YIELD_CURVE_MARKET_DATA, targetSpec, properties));
      requirements.add(new ValueRequirement(ValueRequirementNames.YIELD_CURVE_SPEC, targetSpec, properties));
      requirements.add(new ValueRequirement(ValueRequirementNames.YIELD_CURVE_INSTRUMENT_CONVERSION_HISTORICAL_TIME_SERIES, targetSpec, curveTSProperties));
    }
    if (curveCalculationConfig.getExogenousConfigData() != null) {
      final LinkedHashMap<String, String[]> exogenousCurveConfigs = curveCalculationConfig.getExogenousConfigData();
      for (final Map.Entry<String, String[]> entry : exogenousCurveConfigs.entrySet()) {
        for (final String exogenousCurveName : entry.getValue()) {
          final ValueProperties properties = ValueProperties.builder()
              .with(ValuePropertyNames.CURVE_CALCULATION_CONFIG, entry.getKey())
              .with(ValuePropertyNames.CURVE, exogenousCurveName)
              .with(PROPERTY_DECOMPOSITION, decomposition)
              .with(PROPERTY_ROOT_FINDER_ABSOLUTE_TOLERANCE, absoluteTolerance)
              .with(PROPERTY_ROOT_FINDER_MAX_ITERATIONS, maxIteration)
              .with(PROPERTY_ROOT_FINDER_RELATIVE_TOLERANCE, relativeTolerance)
              .with(PROPERTY_USE_FINITE_DIFFERENCE, finiteDifference)
              .get();
          requirements.add(new ValueRequirement(ValueRequirementNames.YIELD_CURVE, targetSpec, properties));
        }
      }
    }
    return requirements;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target, final Map<ValueSpecification, ValueRequirement> inputs) {
    final Set<ValueSpecification> results = new HashSet<ValueSpecification>();
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
    assert curveCalculationConfigName != null;
    final ValueProperties properties = getJacobianProperties(curveCalculationConfigName);
    final ValueSpecification jacobian = new ValueSpecification(ValueRequirementNames.YIELD_CURVE_JACOBIAN, targetSpec, properties);
    results.add(jacobian);
    return results;
  }

  protected abstract ValueProperties getJacobianProperties();

  protected abstract ValueProperties getCurveProperties();

  protected abstract ValueProperties getJacobianProperties(final String curveCalculationConfigName);

  protected abstract ValueProperties getCurveProperties(final String curveCalculationConfigName, final String curveName);

  protected abstract ValueProperties getJacobianProperties(final String curveCalculationConfigName, final String absoluteTolerance, final String relativeTolerance, final String maxIterations,
      final String decomposition, final String useFiniteDifference);

  protected abstract ValueProperties getCurveProperties(final String curveCalculationConfigName, final String curveName, final String absoluteTolerance, final String relativeTolerance,
      final String maxIterations, final String decomposition, final String useFiniteDifference);

  protected abstract String getCalculationMethod();

  protected SnapshotDataBundle getMarketData(final FunctionInputs inputs, final ComputationTargetSpecification targetSpec, final String curveName) {
    final ValueRequirement marketDataRequirement = new ValueRequirement(ValueRequirementNames.YIELD_CURVE_MARKET_DATA, targetSpec, ValueProperties.with(ValuePropertyNames.CURVE, curveName).get());
    final Object marketDataObject = inputs.getValue(marketDataRequirement);
    if (marketDataObject == null) {
      throw new OpenGammaRuntimeException("Could not get a value for requirement " + marketDataRequirement);
    }
    return (SnapshotDataBundle) marketDataObject;
  }

  protected HistoricalTimeSeriesBundle getTimeSeriesBundle(final FunctionInputs inputs, final ComputationTargetSpecification targetSpec, final String curveCalculationConfigName) {
    final ValueRequirement timeSeriesRequirement = new ValueRequirement(ValueRequirementNames.YIELD_CURVE_INSTRUMENT_CONVERSION_HISTORICAL_TIME_SERIES, targetSpec, ValueProperties.with(
        ValuePropertyNames.CURVE_CALCULATION_CONFIG, curveCalculationConfigName).get());
    final Object timeSeriesObject = inputs.getValue(timeSeriesRequirement);
    if (timeSeriesObject == null) {
      throw new OpenGammaRuntimeException("Could not get conversion time series for requirement " + timeSeriesRequirement);
    }
    return (HistoricalTimeSeriesBundle) timeSeriesObject;
  }

  protected InterpolatedYieldCurveSpecificationWithSecurities getYieldCurveSpecification(final FunctionInputs inputs, final ComputationTargetSpecification targetSpec, final String curveName) {
    final ValueRequirement specRequirement = new ValueRequirement(ValueRequirementNames.YIELD_CURVE_SPEC, targetSpec, ValueProperties.with(ValuePropertyNames.CURVE, curveName).get());
    final Object specObject = inputs.getValue(specRequirement);
    if (specObject == null) {
      return null;
    }
    final InterpolatedYieldCurveSpecificationWithSecurities spec = (InterpolatedYieldCurveSpecificationWithSecurities) specObject;
    return spec;
  }

  protected YieldCurveBundle getKnownCurves(final MultiCurveCalculationConfig curveCalculationConfig, final ComputationTargetSpecification targetSpec,
      final FunctionInputs inputs) {
    YieldCurveBundle knownCurves = null;
    if (curveCalculationConfig.getExogenousConfigData() != null) {
      knownCurves = new YieldCurveBundle();
      final LinkedHashMap<String, String[]> exogenousCurveNames = curveCalculationConfig.getExogenousConfigData();
      for (final Map.Entry<String, String[]> entry : exogenousCurveNames.entrySet()) {
        for (final String exogenousCurveName : entry.getValue()) {
          final ValueProperties properties = ValueProperties.builder()
              .with(ValuePropertyNames.CURVE, exogenousCurveName).get();
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

}
