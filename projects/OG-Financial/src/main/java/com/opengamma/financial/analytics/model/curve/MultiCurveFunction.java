/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.curve;

import static com.opengamma.engine.value.ValuePropertyNames.CURVE;
import static com.opengamma.engine.value.ValuePropertyNames.CURVE_CALCULATION_METHOD;
import static com.opengamma.engine.value.ValuePropertyNames.CURVE_CONSTRUCTION_CONFIG;
import static com.opengamma.engine.value.ValueRequirementNames.CURVE_BUNDLE;
import static com.opengamma.engine.value.ValueRequirementNames.CURVE_DEFINITION;
import static com.opengamma.engine.value.ValueRequirementNames.CURVE_INSTRUMENT_CONVERSION_HISTORICAL_TIME_SERIES;
import static com.opengamma.engine.value.ValueRequirementNames.CURVE_MARKET_DATA;
import static com.opengamma.engine.value.ValueRequirementNames.CURVE_SPECIFICATION;
import static com.opengamma.engine.value.ValueRequirementNames.FX_MATRIX;
import static com.opengamma.engine.value.ValueRequirementNames.JACOBIAN_BUNDLE;
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
import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalTime;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;
import org.threeten.bp.temporal.ChronoUnit;

import com.google.common.collect.Iterables;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.provider.calculator.generic.LastTimeCalculator;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlockBundle;
import com.opengamma.analytics.financial.provider.description.interestrate.ParameterProviderInterface;
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
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.OpenGammaCompilationContext;
import com.opengamma.financial.OpenGammaExecutionContext;
import com.opengamma.financial.analytics.conversion.CurveNodeConverter;
import com.opengamma.financial.analytics.curve.ConfigDBCurveConstructionConfigurationSource;
import com.opengamma.financial.analytics.curve.CurveConstructionConfiguration;
import com.opengamma.financial.analytics.curve.CurveConstructionConfigurationSource;
import com.opengamma.financial.analytics.curve.CurveDefinition;
import com.opengamma.financial.analytics.curve.CurveUtils;
import com.opengamma.financial.analytics.curve.InterpolatedCurveDefinition;
import com.opengamma.financial.analytics.ircurve.strips.CurveNodeVisitor;
import com.opengamma.financial.analytics.timeseries.HistoricalTimeSeriesBundle;
import com.opengamma.financial.convention.ConventionSource;
import com.opengamma.financial.view.ConfigDocumentWatchSetProvider;
import com.opengamma.id.ExternalId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.async.AsynchronousExecution;
import com.opengamma.util.tuple.Pair;

/**
 * Top-level class for multi-curve functions.
 *
 * This is a work in progress
 * @param <T> The type of the provider produced
 * @param <U> The type of the builder
 * @param <V> The type of the curve generator
 * @param <W> The type of the sensitivity results
 */
public abstract class MultiCurveFunction<T extends ParameterProviderInterface, U, V, W> extends AbstractFunction {
  /** The curve configuration name */
  private final String _configurationName;
  /** The maturity calculator */
  private static final LastTimeCalculator MATURITY_CALCULATOR = LastTimeCalculator.getInstance();

  /**
   * @param configurationName The configuration name, not null
   */
  public MultiCurveFunction(final String configurationName) {
    ArgumentChecker.notNull(configurationName, "configuration name");
    _configurationName = configurationName;
  }

  @Override
  public void init(final FunctionCompilationContext context) {
    ConfigDocumentWatchSetProvider.reinitOnChanges(context, this, CurveConstructionConfiguration.class);
    ConfigDocumentWatchSetProvider.reinitOnChanges(context, null, CurveDefinition.class);
    ConfigDocumentWatchSetProvider.reinitOnChanges(context, null, InterpolatedCurveDefinition.class);
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
        exogenousRequirements.add(new ValueRequirement(CURVE_BUNDLE, ComputationTargetSpecification.NULL, properties));
      }
    }
    final String[] curveNames = CurveUtils.getCurveNamesForConstructionConfiguration(curveConstructionConfiguration);
    return getCompiledFunction(atZDT.with(LocalTime.MIDNIGHT), atZDT.plusDays(1).with(LocalTime.MIDNIGHT).minusNanos(1000000), curveNames, exogenousRequirements,
        curveConstructionConfiguration);
  }

  /**
   * Gets the compiled function for this curve construction method.
   * @param earliestInvocation The earliest time this metadata and invoker are valid, null to indicate no lower validity bound
   * @param latestInvocation The latest time this metadata and invoker are valid, null to indicate no upper validity bound
   * @param curveNames The curve names
   * @param exogenousRequirements The exogenous requirements
   * @param curveConstructionConfiguration The curve construction configuration
   * @return A compiled function that produces curves.
   */
  public abstract CompiledFunctionDefinition getCompiledFunction(ZonedDateTime earliestInvocation, ZonedDateTime latestInvocation, String[] curveNames,
      Set<ValueRequirement> exogenousRequirements, CurveConstructionConfiguration curveConstructionConfiguration);

  /**
   * Base function for the compiled functions.
   */
  protected abstract class CurveCompiledFunctionDefinition extends AbstractInvokingCompiledFunction {
    /** The curve names */
    private final String[] _curveNames;
    /** The curve value requirement */
    private final String _curveRequirement;
    /** The exogenous requirements */
    private final Set<ValueRequirement> _exogenousRequirements;
    /** The set of results */
    private final Set<ValueSpecification> _results;

    /**
     * @param earliestInvocation The earliest time this metadata and invoker are valid, null to indicate no lower validity bound
     * @param latestInvocation The latest time this metadata and invoker are valid, null to indicate no upper validity bound
     * @param curveNames The curve names, not null
     * @param curveRequirement The curve value requirement produced by this function, not null
     * @param exogenousRequirements The exogenous requirements, not null
     */
    protected CurveCompiledFunctionDefinition(final ZonedDateTime earliestInvocation, final ZonedDateTime latestInvocation, final String[] curveNames,
        final String curveRequirement, final Set<ValueRequirement> exogenousRequirements) {
      super(earliestInvocation, latestInvocation);
      ArgumentChecker.notNull(curveNames, "curve names");
      ArgumentChecker.notNull(curveRequirement, "curve requirement");
      ArgumentChecker.notNull(exogenousRequirements, "exogenous requirements");
      _curveNames = curveNames;
      _curveRequirement = curveRequirement;
      _exogenousRequirements = exogenousRequirements;
      _results = new HashSet<>();
      final ValueProperties properties = getBundleProperties(_curveNames);
      for (final String curveName : _curveNames) {
        final ValueProperties curveProperties = getCurveProperties(curveName);
        _results.add(new ValueSpecification(curveRequirement, ComputationTargetSpecification.NULL, curveProperties));
      }
      _results.add(new ValueSpecification(CURVE_BUNDLE, ComputationTargetSpecification.NULL, properties));
      _results.add(new ValueSpecification(JACOBIAN_BUNDLE, ComputationTargetSpecification.NULL, properties));
    }

    @Override
    public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target,
        final Set<ValueRequirement> desiredValues) throws AsynchronousExecution {
      final T knownData = getKnownData(inputs);
      final Clock snapshotClock = executionContext.getValuationClock();
      final ZonedDateTime now = ZonedDateTime.now(snapshotClock);
      ValueProperties bundleProperties = null;
      for (final ValueRequirement desiredValue : desiredValues) {
        if (desiredValue.getValueName().equals(CURVE_BUNDLE)) {
          bundleProperties = desiredValue.getConstraints();
          break;
        } else if (desiredValue.getValueName().equals(_curveRequirement)) {
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
      final ValueSpecification bundleSpec = new ValueSpecification(CURVE_BUNDLE, ComputationTargetSpecification.NULL, bundleProperties);
      final ValueSpecification jacobianSpec = new ValueSpecification(JACOBIAN_BUNDLE, ComputationTargetSpecification.NULL, bundleProperties);
      return getResults(bundleSpec, jacobianSpec, bundleProperties, pair);
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
        requirements.add(new ValueRequirement(CURVE_DEFINITION, ComputationTargetSpecification.NULL, properties));
        requirements.add(new ValueRequirement(CURVE_MARKET_DATA, ComputationTargetSpecification.NULL, properties));
        requirements.add(new ValueRequirement(CURVE_SPECIFICATION, ComputationTargetSpecification.NULL, properties));
      }
      final ValueProperties properties = ValueProperties.builder()
          .with(CURVE_CONSTRUCTION_CONFIG, _configurationName)
          .get();
      requirements.add(new ValueRequirement(CURVE_INSTRUMENT_CONVERSION_HISTORICAL_TIME_SERIES, ComputationTargetSpecification.NULL, properties));
      requirements.add(new ValueRequirement(FX_MATRIX, ComputationTargetSpecification.NULL, properties));
      requirements.addAll(_exogenousRequirements);
      return requirements;
    }

    /**
     * Gets the exogenous requirements.
     * @return The exogenous requirements
     */
    protected Set<ValueRequirement> getExogenousRequirements() {
      return _exogenousRequirements;
    }

    /**
     * Gets the curve names.
     * @return The curve names
     */
    protected String[] getCurveNames() {
      return _curveNames;
    }

    /**
     * Gets the calculator.
     * @return The calculator
     */
    protected abstract InstrumentDerivativeVisitor<T, Double> getCalculator();

    /**
     * Gets the sensitivity calculator.
     * @return The sensitivity calculator
     */
    protected abstract InstrumentDerivativeVisitor<T, W> getSensitivityCalculator();

    /**
     * Gets the curve type property.
     * @return The curve type property
     */
    protected abstract String getCurveTypeProperty();

    /**
     * Gets the known data from the function inputs.
     * @param inputs The inputs
     * @return The known data
     */
    protected abstract T getKnownData(FunctionInputs inputs);

    /**
     * Gets the curve builder.
     * @param absoluteTolerance The absolute tolerance for the root-finder
     * @param relativeTolerance The relative tolerance for the root-finder
     * @param maxIterations The maximum number of iterations
     * @return The builder
     */
    protected abstract U getBuilder(double absoluteTolerance, double relativeTolerance, int maxIterations);

    /**
     * Gets the generator for a curve definition
     * @param definition The curve definition
     * @param valuationDate The valuation date
     * @return The generator
     */
    protected abstract V getGenerator(CurveDefinition definition, LocalDate valuationDate);

    /**
     * @param conventionSource The convention source
     * @param holidaySource The holiday source
     * @param regionSource The region source
     * @param marketData The market data snapshot
     * @param dataId The market data id for a node
     * @param historicalData The historical data
     * @param valuationTime The valuation time
     * @return A visitor that converts curve nodes to instrument definitions
     */
    protected abstract CurveNodeVisitor<InstrumentDefinition<?>> getCurveNodeConverter(ConventionSource conventionSource, HolidaySource holidaySource,
        RegionSource regionSource, SnapshotDataBundle marketData, ExternalId dataId, HistoricalTimeSeriesBundle historicalData, ZonedDateTime valuationTime);

    /**
     * @param inputs The inputs
     * @param now The valuation time
     * @param builder The builder
     * @param knownData The known data
     * @param conventionSource The convention source
     * @param holidaySource The holiday source
     * @param regionSource The region source
     * @return The curve provider and associated results
     */
    protected abstract Pair<T, CurveBuildingBlockBundle> getCurves(FunctionInputs inputs, ZonedDateTime now, U builder, T knownData,
        ConventionSource conventionSource, HolidaySource holidaySource, RegionSource regionSource);

    /**
     * @param bundleSpec The value specification for the curve bundle
     * @param jacobianSpec The value specification for the block of Jacobian matrices
     * @param bundleProperties The properties for the curve bundle
     * @param pair The results
     * @return A set of results
     */
    protected abstract Set<ComputedValue> getResults(ValueSpecification bundleSpec, ValueSpecification jacobianSpec,
        ValueProperties bundleProperties, Pair<T, CurveBuildingBlockBundle> pair);

    /**
     * Gets the curve node converter used to convert a node into an InstrumentDerivative.
     * @param conventionSource the convention source, not null
     * @return The curve node converter used to convert a node into an InstrumentDerivative.
     */
    protected CurveNodeConverter getCurveNodeConverter(final ConventionSource conventionSource) {
      ArgumentChecker.notNull(conventionSource, "convention source");
      return new CurveNodeConverter(conventionSource);
    }

    /**
     * Gets the maturity calculator.
     * @return The maturity calculator
     */
    protected InstrumentDerivativeVisitor<Object, Double> getMaturityCalculator() {
      return MATURITY_CALCULATOR;
    }

    /**
     * Gets the result properties for a curve
     * @param curveName The curve name
     * @return The result properties
     */
    protected ValueProperties getCurveProperties(final String curveName) {
      return createValueProperties()
          .with(CURVE, curveName)
          .with(CURVE_CALCULATION_METHOD, ROOT_FINDING)
          .with(PROPERTY_CURVE_TYPE, getCurveTypeProperty())
          .with(CURVE_CONSTRUCTION_CONFIG, _configurationName)
          .withAny(PROPERTY_ROOT_FINDER_ABSOLUTE_TOLERANCE)
          .withAny(PROPERTY_ROOT_FINDER_RELATIVE_TOLERANCE)
          .withAny(PROPERTY_ROOT_FINDER_MAX_ITERATIONS)
          .get();
    }

    /**
     * Gets the result properties for a curve bundle
     * @param curveNames All of the curves produced by this function
     * @return The result properties
     */
    protected ValueProperties getBundleProperties(final String[] curveNames) {
      return createValueProperties()
          .with(CURVE_CALCULATION_METHOD, ROOT_FINDING)
          .with(PROPERTY_CURVE_TYPE, getCurveTypeProperty())
          .with(CURVE_CONSTRUCTION_CONFIG, _configurationName)
          .withAny(PROPERTY_ROOT_FINDER_ABSOLUTE_TOLERANCE)
          .withAny(PROPERTY_ROOT_FINDER_RELATIVE_TOLERANCE)
          .withAny(PROPERTY_ROOT_FINDER_MAX_ITERATIONS)
          .with(CURVE, curveNames)
          .get();
    }

  }
}
