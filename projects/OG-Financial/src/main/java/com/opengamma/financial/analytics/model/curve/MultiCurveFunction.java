/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.curve;

import static com.opengamma.engine.value.ValuePropertyNames.CURVE;
import static com.opengamma.engine.value.ValuePropertyNames.CURVE_CALCULATION_METHOD;
import static com.opengamma.engine.value.ValuePropertyNames.CURVE_CONSTRUCTION_CONFIG;
import static com.opengamma.financial.analytics.model.curve.CurveCalculationPropertyNamesAndValues.DISCOUNTING;
import static com.opengamma.financial.analytics.model.curve.CurveCalculationPropertyNamesAndValues.PROPERTY_CURVE_TYPE;
import static com.opengamma.financial.analytics.model.curve.CurveCalculationPropertyNamesAndValues.ROOT_FINDING;
import static com.opengamma.financial.analytics.model.curve.interestrate.MultiYieldCurvePropertiesAndDefaults.PROPERTY_ROOT_FINDER_ABSOLUTE_TOLERANCE;
import static com.opengamma.financial.analytics.model.curve.interestrate.MultiYieldCurvePropertiesAndDefaults.PROPERTY_ROOT_FINDER_MAX_ITERATIONS;
import static com.opengamma.financial.analytics.model.curve.interestrate.MultiYieldCurvePropertiesAndDefaults.PROPERTY_ROOT_FINDER_RELATIVE_TOLERANCE;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.threeten.bp.Clock;
import org.threeten.bp.Instant;
import org.threeten.bp.LocalTime;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;
import org.threeten.bp.temporal.ChronoUnit;

import com.google.common.collect.Iterables;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlockBundle;
import com.opengamma.analytics.financial.provider.description.interestrate.ParameterProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MulticurveSensitivity;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.marketdatasnapshot.SnapshotDataBundle;
import com.opengamma.core.region.RegionSource;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.CompiledFunctionDefinition;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.OpenGammaCompilationContext;
import com.opengamma.financial.OpenGammaExecutionContext;
import com.opengamma.financial.analytics.curve.ConfigDBCurveConstructionConfigurationSource;
import com.opengamma.financial.analytics.curve.CurveConstructionConfiguration;
import com.opengamma.financial.analytics.curve.CurveConstructionConfigurationSource;
import com.opengamma.financial.analytics.curve.CurveDefinition;
import com.opengamma.financial.analytics.curve.CurveUtils;
import com.opengamma.financial.analytics.ircurve.strips.CurveNodeVisitor;
import com.opengamma.financial.analytics.timeseries.HistoricalTimeSeriesBundle;
import com.opengamma.financial.convention.ConventionSource;
import com.opengamma.id.ExternalId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.async.AsynchronousExecution;
import com.opengamma.util.tuple.Pair;

/**
 *
 */
public abstract class MultiCurveFunction<T extends ParameterProviderInterface, U, V> extends AbstractFunction {
  /** The curve configuration name */
  private final String _configurationName;

  public MultiCurveFunction(final String configurationName) {
    ArgumentChecker.notNull(configurationName, "configuration name");
    _configurationName = configurationName;
  }

  @Override
  public CompiledFunctionDefinition compile(final FunctionCompilationContext context, final Instant atInstant) {
    final ZonedDateTime atZDT = ZonedDateTime.ofInstant(atInstant, ZoneOffset.UTC);
    final ConfigSource configSource = OpenGammaCompilationContext.getConfigSource(context);
    final CurveConstructionConfigurationSource curveConfigurationSource = new ConfigDBCurveConstructionConfigurationSource(configSource);
    final Instant versionTime = atZDT.plus(1, ChronoUnit.HOURS).truncatedTo(ChronoUnit.HOURS).toInstant();
    //TODO work out a way to use dependency graph to get curve information for this config
    final CurveConstructionConfiguration curveConstructionConfiguration = curveConfigurationSource.getCurveConstructionConfiguration(_configurationName,
        VersionCorrection.of(versionTime, versionTime));
    if (curveConstructionConfiguration == null) {
      throw new OpenGammaRuntimeException("Could not get curve construction configuration called " + _configurationName);
    }
    final Set<ValueRequirement> exogenousRequirements = new HashSet<>();
    if (curveConstructionConfiguration.getExogenousConfigurations() != null) {
      final List<String> exogenousConfigurations = curveConstructionConfiguration.getExogenousConfigurations();
      for (final String name : exogenousConfigurations) {
        //TODO deal with arbitrary depth
        final ValueProperties properties = ValueProperties.builder()
            .with(CURVE_CONSTRUCTION_CONFIG, name)
            .get();
        exogenousRequirements.add(new ValueRequirement(ValueRequirementNames.CURVE_BUNDLE, ComputationTargetSpecification.NULL, properties));
      }
    }
    final String[] curveNames = CurveUtils.getCurveNamesForConstructionConfiguration(curveConstructionConfiguration);
    return getCompiledFunction(atZDT.with(LocalTime.MIDNIGHT), atZDT.plusDays(1).with(LocalTime.MIDNIGHT).minusNanos(1000000), curveNames, exogenousRequirements,
        curveConstructionConfiguration);
  }

  public abstract CompiledFunctionDefinition getCompiledFunction(ZonedDateTime earliestInvokation, ZonedDateTime latestInvokation, String[] curveNames,
      Set<ValueRequirement> exogenousRequirements, CurveConstructionConfiguration curveConstructionConfiguration);

  public abstract class CurveCompiledFunctionDefinition extends AbstractInvokingCompiledFunction {
    private final String[] _curveNames;
    private final Set<ValueRequirement> _exogenousRequirements;
    private final Set<ValueSpecification> _results;

    public CurveCompiledFunctionDefinition(final ZonedDateTime earliestInvokation, final ZonedDateTime latestInvokation, final String[] curveNames,
        final Set<ValueRequirement> exogenousRequirements) {
      super(earliestInvokation, latestInvokation);
      _curveNames = curveNames;
      _exogenousRequirements = exogenousRequirements;
      _results = new HashSet<>();
      final ValueProperties properties = getBundleProperties();
      for (final String curveName : _curveNames) {
        final ValueProperties curveProperties = getCurveProperties(curveName);
        _results.add(new ValueSpecification(ValueRequirementNames.YIELD_CURVE, ComputationTargetSpecification.NULL, curveProperties));
      }
      _results.add(new ValueSpecification(ValueRequirementNames.CURVE_BUNDLE, ComputationTargetSpecification.NULL, properties));
    }

    @Override
    public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target,
        final Set<ValueRequirement> desiredValues) throws AsynchronousExecution {
      final T knownData = getKnownData(inputs);
      final Clock snapshotClock = executionContext.getValuationClock();
      final ZonedDateTime now = ZonedDateTime.now(snapshotClock);
      ValueProperties bundleProperties = null;
      for (final ValueRequirement desiredValue : desiredValues) {
        if (desiredValue.getValueName().equals(ValueRequirementNames.CURVE_BUNDLE)) {
          bundleProperties = desiredValue.getConstraints();
          break;
        } else if (desiredValue.getValueName().equals(ValueRequirementNames.YIELD_CURVE)) {
          bundleProperties = desiredValue.getConstraints()
              .withoutAny(CURVE);
          break;
        }
      }
      if (bundleProperties == null) {
        throw new OpenGammaRuntimeException("Could not get bundle properties from desired values");
      }
      final double absoluteTolerance = Double.parseDouble(Iterables.getOnlyElement(bundleProperties.getValues(PROPERTY_ROOT_FINDER_ABSOLUTE_TOLERANCE)));
      final double relativeTolerance = Double.parseDouble(Iterables.getOnlyElement(bundleProperties.getValues(PROPERTY_ROOT_FINDER_RELATIVE_TOLERANCE)));
      final int maxIterations = Integer.parseInt(Iterables.getOnlyElement(bundleProperties.getValues(PROPERTY_ROOT_FINDER_MAX_ITERATIONS)));
      final U builder = getBuilder(absoluteTolerance, relativeTolerance, maxIterations);
      final ConventionSource conventionSource = OpenGammaExecutionContext.getConventionSource(executionContext);
      final HolidaySource holidaySource = OpenGammaExecutionContext.getHolidaySource(executionContext);
      final RegionSource regionSource = OpenGammaExecutionContext.getRegionSource(executionContext);
      final Pair<T, CurveBuildingBlockBundle> pair = getCurves(inputs, now, builder, knownData, conventionSource, holidaySource, regionSource);
      final ValueSpecification bundleSpec = new ValueSpecification(ValueRequirementNames.CURVE_BUNDLE, ComputationTargetSpecification.NULL, bundleProperties);
      return getResults(bundleSpec, bundleProperties, pair);
    }

    @Override
    public ComputationTargetType getTargetType() {
      return ComputationTargetType.NULL;
    }

    @Override
    public Set<ValueSpecification> getResults(final FunctionCompilationContext compilationContext, final ComputationTarget target) {
      return _results;
    }

    @Override
    public Set<ValueRequirement> getRequirements(final FunctionCompilationContext compilationContext, final ComputationTarget target, final ValueRequirement desiredValue) {
      final ValueProperties constraints = desiredValue.getConstraints();
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
      final Set<ValueRequirement> requirements = new HashSet<>();
      for (final String curveName : _curveNames) {
        final ValueProperties properties = ValueProperties.builder()
            .with(CURVE, curveName)
            .get();
        requirements.add(new ValueRequirement(ValueRequirementNames.CURVE_DEFINITION, ComputationTargetSpecification.NULL, properties));
        requirements.add(new ValueRequirement(ValueRequirementNames.CURVE_MARKET_DATA, ComputationTargetSpecification.NULL, properties));
        requirements.add(new ValueRequirement(ValueRequirementNames.CURVE_SPECIFICATION, ComputationTargetSpecification.NULL, properties));
      }
      final ValueProperties properties = ValueProperties.builder()
          .with(CURVE_CONSTRUCTION_CONFIG, _configurationName)
          .get();
      requirements.add(new ValueRequirement(ValueRequirementNames.CURVE_INSTRUMENT_CONVERSION_HISTORICAL_TIME_SERIES, ComputationTargetSpecification.NULL, properties));
      requirements.add(new ValueRequirement(ValueRequirementNames.FX_MATRIX, ComputationTargetSpecification.NULL, properties));
      requirements.addAll(_exogenousRequirements);
      return requirements;
    }

    public Set<ValueRequirement> getExogenousRequirements() {
      return _exogenousRequirements;
    }

    public String[] getCurveNames() {
      return _curveNames;
    }

    public abstract InstrumentDerivativeVisitor<T, Double> getCalculator();

    public abstract InstrumentDerivativeVisitor<T, MulticurveSensitivity> getSensitivityCalculator();

    public abstract String getCurveTypeProperty();

    public abstract T getKnownData(FunctionInputs inputs);

    public abstract U getBuilder(double absoluteTolerance, double relativeTolerance, int maxIterations);

    public abstract V getGenerator(CurveDefinition definition);

    public abstract CurveNodeVisitor<InstrumentDefinition<?>> getCurveNodeConverter(ConventionSource conventionSource, HolidaySource holidaySource,
        RegionSource regionSource, SnapshotDataBundle marketData, ExternalId dataId, HistoricalTimeSeriesBundle historicalData, ZonedDateTime valuationTime);

    public abstract Pair<T, CurveBuildingBlockBundle> getCurves(FunctionInputs inputs, ZonedDateTime now, U builder, T knownData,
        ConventionSource conventionSource, HolidaySource holidaySource, RegionSource regionSource);

    public abstract Set<ComputedValue> getResults(ValueSpecification bundleSpec, ValueProperties bundleProperties, Pair<T, CurveBuildingBlockBundle> pair);

    @SuppressWarnings("synthetic-access")
    private ValueProperties getCurveProperties(final String curveName) {
      return createValueProperties()
          .with(CURVE, curveName)
          .with(CURVE_CALCULATION_METHOD, ROOT_FINDING)
          .with(PROPERTY_CURVE_TYPE, DISCOUNTING)
          .with(CURVE_CONSTRUCTION_CONFIG, _configurationName)
          .withAny(PROPERTY_ROOT_FINDER_ABSOLUTE_TOLERANCE)
          .withAny(PROPERTY_ROOT_FINDER_RELATIVE_TOLERANCE)
          .withAny(PROPERTY_ROOT_FINDER_MAX_ITERATIONS)
          .get();
    }

    @SuppressWarnings("synthetic-access")
    private ValueProperties getBundleProperties() {
      return createValueProperties()
          .with(CURVE_CALCULATION_METHOD, ROOT_FINDING)
          .with(PROPERTY_CURVE_TYPE, DISCOUNTING)
          .with(CURVE_CONSTRUCTION_CONFIG, _configurationName)
          .withAny(PROPERTY_ROOT_FINDER_ABSOLUTE_TOLERANCE)
          .withAny(PROPERTY_ROOT_FINDER_RELATIVE_TOLERANCE)
          .withAny(PROPERTY_ROOT_FINDER_MAX_ITERATIONS)
          .get();
    }

  }
}
