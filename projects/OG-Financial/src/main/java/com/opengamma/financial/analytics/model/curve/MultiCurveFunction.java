/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.curve;

import static com.opengamma.engine.value.ValuePropertyNames.CURVE;
import static com.opengamma.engine.value.ValuePropertyNames.CURVE_CALCULATION_METHOD;
import static com.opengamma.engine.value.ValuePropertyNames.CURVE_CONSTRUCTION_CONFIG;
import static com.opengamma.engine.value.ValuePropertyNames.CURVE_SENSITIVITY_CURRENCY;
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

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.Clock;
import org.threeten.bp.Instant;
import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalTime;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;

import com.google.common.collect.Iterables;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.provider.calculator.generic.LastTimeCalculator;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlockBundle;
import com.opengamma.analytics.financial.provider.description.interestrate.ParameterProviderInterface;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.core.convention.ConventionSource;
import com.opengamma.core.marketdatasnapshot.SnapshotDataBundle;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.CompiledFunctionDefinition;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.target.ComputationTargetReference;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.OpenGammaCompilationContext;
import com.opengamma.financial.analytics.conversion.CurveNodeConverter;
import com.opengamma.financial.analytics.curve.ConfigDBCurveConstructionConfigurationSource;
import com.opengamma.financial.analytics.curve.CurveConstructionConfiguration;
import com.opengamma.financial.analytics.curve.CurveConstructionConfigurationSource;
import com.opengamma.financial.analytics.curve.CurveDefinition;
import com.opengamma.financial.analytics.curve.CurveNodeCurrencyVisitor;
import com.opengamma.financial.analytics.curve.CurveUtils;
import com.opengamma.financial.analytics.curve.credit.ConfigDBCurveDefinitionSource;
import com.opengamma.financial.analytics.curve.credit.CurveDefinitionSource;
import com.opengamma.financial.analytics.ircurve.strips.CurveNodeVisitor;
import com.opengamma.financial.analytics.timeseries.HistoricalTimeSeriesBundle;
import com.opengamma.id.ExternalId;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.async.AsynchronousExecution;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.Pair;

/**
 * Top-level class for multi-curve functions. This is a work in progress
 *
 * @param <T> The type of the provider produced
 * @param <U> The type of the builder
 * @param <V> The type of the curve generator
 * @param <W> The type of the sensitivity results
 */
public abstract class MultiCurveFunction<T extends ParameterProviderInterface, U, V, W> extends AbstractFunction {
  /** The logger */
  private static final Logger s_logger = LoggerFactory.getLogger(MultiCurveFunction.class);
  /** The maturity calculator */
  private static final LastTimeCalculator MATURITY_CALCULATOR = LastTimeCalculator.getInstance();
  /** The curve configuration name */
  private final String _configurationName;
  /** A curve construction configuration source */
  private CurveConstructionConfigurationSource _curveConstructionConfigurationSource;
  /** A curve definition source */
  private CurveDefinitionSource _curveDefinitionSource;

  /**
   * @param configurationName The configuration name, not null
   */
  public MultiCurveFunction(final String configurationName) {
    ArgumentChecker.notNull(configurationName, "configuration name");
    _configurationName = configurationName;
  }

  @Override
  public void init(final FunctionCompilationContext context) {
    _curveConstructionConfigurationSource = ConfigDBCurveConstructionConfigurationSource.init(context, this);
    _curveDefinitionSource = ConfigDBCurveDefinitionSource.init(context, this);
  }

  @Override
  public CompiledFunctionDefinition compile(final FunctionCompilationContext context, final Instant atInstant) {
    final ZonedDateTime atZDT = ZonedDateTime.ofInstant(atInstant, ZoneOffset.UTC);
    //TODO work out a way to use dependency graph to get curve information for this config
    final CurveConstructionConfiguration curveConstructionConfiguration = _curveConstructionConfigurationSource.getCurveConstructionConfiguration(_configurationName);
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
            .with(CURVE_CALCULATION_METHOD, ROOT_FINDING)
            .get();
        exogenousRequirements.add(new ValueRequirement(CURVE_BUNDLE, ComputationTargetSpecification.NULL, properties));
        exogenousRequirements.add(new ValueRequirement(JACOBIAN_BUNDLE, ComputationTargetSpecification.NULL, properties));
      }
    }
    final String[] curveNames = CurveUtils.getCurveNamesForConstructionConfiguration(curveConstructionConfiguration);
    final ConventionSource conventionSource = OpenGammaCompilationContext.getConventionSource(context);
    final SecuritySource securitySource = OpenGammaCompilationContext.getSecuritySource(context);
    final ConfigSource configSource = OpenGammaCompilationContext.getConfigSource(context);
    try {
      final CurveNodeVisitor<Set<Currency>> visitor = new CurveNodeCurrencyVisitor(conventionSource, securitySource, configSource);
      final Set<Currency> currencies = CurveUtils.getCurrencies(curveConstructionConfiguration, _curveDefinitionSource, _curveConstructionConfigurationSource,
          visitor);
      final String[] currencyStrings = new String[currencies.size()];
      int i = 0;
      for (final Currency currency : currencies) {
        currencyStrings[i++] = currency.getCode();
      }
      return getCompiledFunction(atZDT.with(LocalTime.MIDNIGHT), atZDT.plusDays(1).with(LocalTime.MIDNIGHT).minusNanos(1000000), curveNames, exogenousRequirements,
          curveConstructionConfiguration, currencyStrings);
    } catch (final Throwable e) {
      s_logger.error("{}: problem in CurveConstructionConfiguration called {}", e.getMessage(), _configurationName);
      s_logger.error("Full stack trace", e);
      throw new OpenGammaRuntimeException(e.getMessage() + ": problem in CurveConstructionConfiguration called " + _configurationName);
    }
  }

  /**
   * Gets the calculator.
   *
   * @return The calculator
   */
  protected abstract InstrumentDerivativeVisitor<T, Double> getCalculator();

  /**
   * Gets the sensitivity calculator.
   *
   * @return The sensitivity calculator
   */
  protected abstract InstrumentDerivativeVisitor<T, W> getSensitivityCalculator();

  /**
   * Gets the curve type property.
   *
   * @return The curve type property
   */
  protected abstract String getCurveTypeProperty();

  /**
   * Gets the compiled function for this curve construction method.
   *
   * @param earliestInvocation The earliest time this metadata and invoker are valid, null to indicate no lower validity bound
   * @param latestInvocation The latest time this metadata and invoker are valid, null to indicate no upper validity bound
   * @param curveNames The curve names
   * @param exogenousRequirements The exogenous requirements
   * @param curveConstructionConfiguration The curve construction configuration
   * @return A compiled function that produces curves.
   * @deprecated Use the method that sets all currencies used in curve construction
   */
  @Deprecated
  public abstract CompiledFunctionDefinition getCompiledFunction(ZonedDateTime earliestInvocation, ZonedDateTime latestInvocation, String[] curveNames,
      Set<ValueRequirement> exogenousRequirements, CurveConstructionConfiguration curveConstructionConfiguration);

  /**
   * Gets the compiled function for this curve construction method. This method is not abstract to maintain
   * backwards-compatibility for the version of the curve functions that do not set sensitivity currencies.
   *
   * @param earliestInvocation The earliest time this metadata and invoker are valid, null to indicate no lower validity bound
   * @param latestInvocation The latest time this metadata and invoker are valid, null to indicate no upper validity bound
   * @param curveNames The curve names
   * @param exogenousRequirements The exogenous requirements
   * @param curveConstructionConfiguration The curve construction configuration
   * @param currencies The set of currencies to which the curves produce sensitivities
   * @return A compiled function that produces curves.
   */
  public CompiledFunctionDefinition getCompiledFunction(final ZonedDateTime earliestInvocation, final ZonedDateTime latestInvocation, final String[] curveNames,
      final Set<ValueRequirement> exogenousRequirements, final CurveConstructionConfiguration curveConstructionConfiguration, final String[] currencies) {
    return getCompiledFunction(earliestInvocation, latestInvocation, curveNames, exogenousRequirements, curveConstructionConfiguration);
  }

  /**
   * Base function for the compiled functions.
   */
  protected abstract class CurveCompiledFunctionDefinition extends AbstractInvokingCompiledFunction {
    /** The curve names */
    private final String[] _curveNames;
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
     * @deprecated Use the constructor that sets all currencies used in curve construction
     */
    @Deprecated
    protected CurveCompiledFunctionDefinition(final ZonedDateTime earliestInvocation, final ZonedDateTime latestInvocation, final String[] curveNames, final String curveRequirement,
        final Set<ValueRequirement> exogenousRequirements) {
      this(earliestInvocation, latestInvocation, curveNames, curveRequirement, exogenousRequirements, null);
    }

    /**
     * @param earliestInvocation The earliest time this metadata and invoker are valid, null to indicate no lower validity bound
     * @param latestInvocation The latest time this metadata and invoker are valid, null to indicate no upper validity bound
     * @param curveNames The curve names, not null
     * @param curveRequirement The curve value requirement produced by this function, not null
     * @param exogenousRequirements The exogenous requirements, not null
     * @param currencies The set of currencies to which the curves produce sensitivities, can be null
     */
    protected CurveCompiledFunctionDefinition(final ZonedDateTime earliestInvocation, final ZonedDateTime latestInvocation, final String[] curveNames, final String curveRequirement,
        final Set<ValueRequirement> exogenousRequirements, final String[] currencies) {
      super(earliestInvocation, latestInvocation);
      ArgumentChecker.notNull(curveNames, "curve names");
      ArgumentChecker.notNull(curveRequirement, "curve requirement");
      ArgumentChecker.notNull(exogenousRequirements, "exogenous requirements");
      _curveNames = curveNames;
      _exogenousRequirements = exogenousRequirements;
      _results = new HashSet<>();
      final ValueProperties properties = getBundleProperties(_curveNames, currencies);
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
      final FXMatrix fxMatrix = (FXMatrix) inputs.getValue(ValueRequirementNames.FX_MATRIX);
      final T knownData = getKnownData(inputs);
      final Clock snapshotClock = executionContext.getValuationClock();
      final ZonedDateTime now = ZonedDateTime.now(snapshotClock);
      final ValueProperties bundleProperties = desiredValues.iterator().next().getConstraints().copy()
          .withoutAny(CURVE)
          .with(CURVE, Arrays.asList(_curveNames))
          .get();
      final double absoluteTolerance = Double.parseDouble(Iterables.getOnlyElement(bundleProperties.getValues(PROPERTY_ROOT_FINDER_ABSOLUTE_TOLERANCE)));
      final double relativeTolerance = Double.parseDouble(Iterables.getOnlyElement(bundleProperties.getValues(PROPERTY_ROOT_FINDER_RELATIVE_TOLERANCE)));
      final int maxIterations = Integer.parseInt(Iterables.getOnlyElement(bundleProperties.getValues(PROPERTY_ROOT_FINDER_MAX_ITERATIONS)));
      final U builder = getBuilder(absoluteTolerance, relativeTolerance, maxIterations);
      final Pair<T, CurveBuildingBlockBundle> pair = getCurves(inputs, now, builder, knownData, executionContext, fxMatrix);
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
        final ValueProperties properties = ValueProperties.builder().with(CURVE, curveName).get();
        requirements.add(new ValueRequirement(CURVE_DEFINITION, ComputationTargetSpecification.NULL, properties));
        requirements.add(new ValueRequirement(CURVE_MARKET_DATA, ComputationTargetSpecification.NULL, properties));
        requirements.add(new ValueRequirement(CURVE_SPECIFICATION, ComputationTargetSpecification.NULL, properties));
      }
      @SuppressWarnings("synthetic-access")
      final ValueProperties properties = ValueProperties.builder().with(CURVE_CONSTRUCTION_CONFIG, _configurationName).get();
      requirements.add(new ValueRequirement(CURVE_INSTRUMENT_CONVERSION_HISTORICAL_TIME_SERIES, ComputationTargetSpecification.NULL, properties));
      requirements.add(new ValueRequirement(FX_MATRIX, ComputationTargetSpecification.NULL, properties));
      addExogenousRequirements(constraints, requirements);
      return requirements;
    }

    /**
     * Gets the exogenous requirements.
     *
     * @return The exogenous requirements
     */
    protected Set<ValueRequirement> getExogenousRequirements() {
      return _exogenousRequirements;
    }

    /**
     * Adds the exogenous requirements, composed with any input constraints, to a collection.
     *
     * @param desiredValue the input constraints, not null
     * @param requirements the collection to add the requirements to, not null
     */
    protected void addExogenousRequirements(final ValueProperties desiredValue, final Collection<ValueRequirement> requirements) {
      if (!_exogenousRequirements.isEmpty()) {
        for (final ValueRequirement exogenousRequirement : _exogenousRequirements) {
          final String valueName = exogenousRequirement.getValueName();
          final ComputationTargetReference targetReq = exogenousRequirement.getTargetReference();
          final ValueProperties exogenousConstraints = exogenousRequirement.getConstraints();
          final ValueProperties.Builder composedConstraints = exogenousConstraints.copy();
          // add desired contraints as optional constraints on the exogenous requirment
          for (final String desiredConstraintProperty : desiredValue.getProperties()) {
            if (!exogenousConstraints.isDefined(desiredConstraintProperty)) {
              final Set<String> constrainedValue = desiredValue.getValues(desiredConstraintProperty);
              if (!constrainedValue.isEmpty()) {
                composedConstraints.with(desiredConstraintProperty, constrainedValue);
              }
              composedConstraints.withOptional(desiredConstraintProperty);
            }
          }
          requirements.add(new ValueRequirement(valueName, targetReq, composedConstraints.get()));
        }
      }
    }

    /**
     * Gets the curve names.
     *
     * @return The curve names
     */
    protected String[] getCurveNames() {
      return _curveNames;
    }

    /**
     * Gets the curve construction configuration name.
     *
     * @return The curve construction configuration name
     */
    protected String getCurveConstructionConfigurationName() {
      return _configurationName;
    }

    /**
     * Gets the known data from the FX matrix.
     *
     * @param inputs The inputs
     * @return The known data
     */
    protected abstract T getKnownData(FunctionInputs inputs);

    /**
     * Gets the curve builder.
     *
     * @param absoluteTolerance The absolute tolerance for the root-finder
     * @param relativeTolerance The relative tolerance for the root-finder
     * @param maxIterations The maximum number of iterations
     * @return The builder
     */
    protected abstract U getBuilder(double absoluteTolerance, double relativeTolerance, int maxIterations);

    /**
     * Gets the generator for a curve definition
     *
     * @param definition The curve definition
     * @param valuationDate The valuation date
     * @return The generator
     */
    protected abstract V getGenerator(CurveDefinition definition, LocalDate valuationDate);

    /**
     * @param context The execution context
     * @param marketData The market data snapshot
     * @param dataId The market data id for a node
     * @param historicalData The historical data
     * @param valuationTime The valuation time
     * @param fxMatrix The FX matrix
     * @return A visitor that converts curve nodes to instrument definitions
     */
    protected abstract CurveNodeVisitor<InstrumentDefinition<?>> getCurveNodeConverter(FunctionExecutionContext context, SnapshotDataBundle marketData, ExternalId dataId,
        HistoricalTimeSeriesBundle historicalData, ZonedDateTime valuationTime, FXMatrix fxMatrix);

    /**
     * @param inputs The inputs
     * @param now The valuation time
     * @param builder The builder
     * @param knownData The known data
     * @param context The function execution context
     * @param fx The FX matrix.
     * @return The curve provider and associated results
     */
    protected abstract Pair<T, CurveBuildingBlockBundle> getCurves(FunctionInputs inputs, ZonedDateTime now, U builder, T knownData, FunctionExecutionContext context, FXMatrix fx);

    /**
     * @param bundleSpec The value specification for the curve bundle
     * @param jacobianSpec The value specification for the block of Jacobian matrices
     * @param bundleProperties The properties for the curve bundle
     * @param pair The results
     * @return A set of results
     */
    protected abstract Set<ComputedValue> getResults(ValueSpecification bundleSpec, ValueSpecification jacobianSpec, ValueProperties bundleProperties, Pair<T, CurveBuildingBlockBundle> pair);

    /**
     * Gets the curve node converter used to convert a node into an InstrumentDerivative.
     *
     * @param conventionSource the convention source, not null
     * @return The curve node converter used to convert a node into an InstrumentDerivative.
     */
    protected CurveNodeConverter getCurveNodeConverter(final ConventionSource conventionSource) {
      ArgumentChecker.notNull(conventionSource, "convention source");
      return new CurveNodeConverter(conventionSource);
    }

    /**
     * Gets the maturity calculator.
     *
     * @return The maturity calculator
     */
    @SuppressWarnings("synthetic-access")
    protected InstrumentDerivativeVisitor<Object, Double> getMaturityCalculator() {
      return MATURITY_CALCULATOR;
    }

    /**
     * Gets the result properties for a curve bundle. This method does not
     * set the {@link ValuePropertyNames#CURVE_SENSITIVITY_CURRENCY} property.
     *
     * @param curveNames All of the curves produced by this function
     * @return The result properties
     */
    protected ValueProperties getBundleProperties(final String[] curveNames) {
      return getBundleProperties(curveNames, null);
    }

    /**
     * Gets the result properties for a curve.
     *
     * @param curveName The curve name
     * @param sensitivityCurrencies The set of currencies to which the curves produce sensitivities
     * @return The result properties
     */
    @SuppressWarnings("synthetic-access")
    protected ValueProperties getCurveProperties(final String curveName) {
      final ValueProperties.Builder builder = createValueProperties()
          .with(CURVE, curveName)
          .with(CURVE_CALCULATION_METHOD, ROOT_FINDING)
          .with(PROPERTY_CURVE_TYPE, getCurveTypeProperty())
          .with(CURVE_CONSTRUCTION_CONFIG, _configurationName)
          .withAny(PROPERTY_ROOT_FINDER_ABSOLUTE_TOLERANCE)
          .withAny(PROPERTY_ROOT_FINDER_RELATIVE_TOLERANCE)
          .withAny(PROPERTY_ROOT_FINDER_MAX_ITERATIONS);
      return builder.get();
    }

    /**
     * Gets the result properties for a curve bundle.
     *
     * @param curveNames All of the curves produced by this function
     * @param sensitivityCurrencies The set of currencies to which the curves produce sensitivities
     * @return The result properties
     */
    @SuppressWarnings("synthetic-access")
    protected ValueProperties getBundleProperties(final String[] curveNames, final String[] sensitivityCurrencies) {
      final ValueProperties.Builder builder = createValueProperties()
          .with(CURVE_CALCULATION_METHOD, ROOT_FINDING)
          .with(PROPERTY_CURVE_TYPE, getCurveTypeProperty())
          .with(CURVE_CONSTRUCTION_CONFIG, _configurationName)
          .withAny(PROPERTY_ROOT_FINDER_ABSOLUTE_TOLERANCE)
          .withAny(PROPERTY_ROOT_FINDER_RELATIVE_TOLERANCE)
          .withAny(PROPERTY_ROOT_FINDER_MAX_ITERATIONS)
          .with(CURVE, curveNames);
      if (sensitivityCurrencies != null) {
        builder.with(CURVE_SENSITIVITY_CURRENCY, sensitivityCurrencies);
      }
      return builder.get();
    }
  }
}
