/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.multicurve;

import static com.opengamma.engine.value.ValuePropertyNames.CURVE;
import static com.opengamma.engine.value.ValuePropertyNames.CURVE_CONSTRUCTION_CONFIG;
import static com.opengamma.engine.value.ValuePropertyNames.CURVE_EXPOSURES;
import static com.opengamma.engine.value.ValueRequirementNames.CURVE_BUNDLE;
import static com.opengamma.engine.value.ValueRequirementNames.CURVE_DEFINITION;
import static com.opengamma.engine.value.ValueRequirementNames.JACOBIAN_BUNDLE;

import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.Clock;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.region.RegionSource;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.OpenGammaCompilationContext;
import com.opengamma.financial.analytics.conversion.CashSecurityConverter;
import com.opengamma.financial.analytics.conversion.FRASecurityConverter;
import com.opengamma.financial.analytics.conversion.FixedIncomeConverterDataProvider;
import com.opengamma.financial.analytics.conversion.FutureTradeConverter;
import com.opengamma.financial.analytics.conversion.SwapSecurityConverter;
import com.opengamma.financial.analytics.conversion.TradeConverter;
import com.opengamma.financial.analytics.curve.ConfigDBCurveConstructionConfigurationSource;
import com.opengamma.financial.analytics.curve.CurveConstructionConfiguration;
import com.opengamma.financial.analytics.curve.CurveUtils;
import com.opengamma.financial.analytics.curve.exposure.ConfigDBInstrumentExposuresProvider;
import com.opengamma.financial.analytics.timeseries.HistoricalTimeSeriesBundle;
import com.opengamma.financial.analytics.timeseries.HistoricalTimeSeriesFunctionUtils;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.convention.ConventionSource;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.FinancialSecurityVisitor;
import com.opengamma.financial.security.FinancialSecurityVisitorAdapter;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesResolver;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.async.AsynchronousExecution;

/**
 * Base function for all multi-curve pricing and risk functions.
 */
public abstract class MultiCurvePricingFunction extends AbstractFunction {
  /** The logger */
  private static final Logger s_logger = LoggerFactory.getLogger(MultiCurvePricingFunction.class);
  private final String[] _valueRequirements;

  /**
   * @param valueRequirements The value requirements, not null
   */
  public MultiCurvePricingFunction(final String... valueRequirements) {
    ArgumentChecker.notNull(valueRequirements, "value requirements");
    _valueRequirements = valueRequirements;
  }

  /**
   * Constructs an object capable of converting from {@link ComputationTarget} to {@link InstrumentDefinition}.
   * @param context The compilation context, not null
   * @return The converter
   */
  protected TradeConverter getTargetToDefinitionConverter(final FunctionCompilationContext context) {
    final SecuritySource securitySource = OpenGammaCompilationContext.getSecuritySource(context);
    final HolidaySource holidaySource = OpenGammaCompilationContext.getHolidaySource(context);
    final RegionSource regionSource = OpenGammaCompilationContext.getRegionSource(context);
    final ConventionBundleSource conventionBundleSource = OpenGammaCompilationContext.getConventionBundleSource(context);
    final ConventionSource conventionSource = OpenGammaCompilationContext.getConventionSource(context);
    final CashSecurityConverter cashConverter = new CashSecurityConverter(holidaySource, regionSource);
    final FRASecurityConverter fraConverter = new FRASecurityConverter(holidaySource, regionSource, conventionSource);
    final SwapSecurityConverter swapConverter = new SwapSecurityConverter(holidaySource, conventionSource, regionSource, false);
    final FinancialSecurityVisitor<InstrumentDefinition<?>> securityConverter = FinancialSecurityVisitorAdapter.<InstrumentDefinition<?>>builder()
        .cashSecurityVisitor(cashConverter)
        .fraSecurityVisitor(fraConverter)
        .swapSecurityVisitor(swapConverter).create();
    final FutureTradeConverter futureTradeConverter = new FutureTradeConverter(securitySource, holidaySource, conventionSource, conventionBundleSource,
        regionSource);
    return new TradeConverter(futureTradeConverter, securityConverter);
  }

  /**
   * Constructs an object capable of converting from {@link InstrumentDefinition} to {@link InstrumentDerivative}.
   * @param context The compilation context, not null
   * @return The converter
   */
  protected FixedIncomeConverterDataProvider getDefinitionToDerivativeConverter(final FunctionCompilationContext context) {
    final ConventionBundleSource conventionBundleSource = OpenGammaCompilationContext.getConventionBundleSource(context);
    final HistoricalTimeSeriesResolver timeSeriesResolver = OpenGammaCompilationContext.getHistoricalTimeSeriesResolver(context);
    return new FixedIncomeConverterDataProvider(conventionBundleSource, timeSeriesResolver);
  }

  /**
   * Base compiled function for all multi-curve pricing and risk functions.
   */
  public abstract class MultiCurveCompiledFunction extends AbstractInvokingCompiledFunction {
    /** Converts targets to definitions */
    private final TradeConverter _tradeToDefinitionConverter;
    /** Converts definitions to derivatives */
    private final FixedIncomeConverterDataProvider _definitionToDerivativeConverter;

    /**
     * @param tradeToDefinitionConverter Converts trades to definitions, not null
     * @param definitionToDerivativeConverter Converts definitions to derivatives, not null
     */
    protected MultiCurveCompiledFunction(final TradeConverter tradeToDefinitionConverter,
        final FixedIncomeConverterDataProvider definitionToDerivativeConverter) {
      ArgumentChecker.notNull(tradeToDefinitionConverter, "target to definition converter");
      ArgumentChecker.notNull(definitionToDerivativeConverter, "definition to derivative converter");
      _tradeToDefinitionConverter = tradeToDefinitionConverter;
      _definitionToDerivativeConverter = definitionToDerivativeConverter;
    }

    @Override
    public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target,
        final Set<ValueRequirement> desiredValues) throws AsynchronousExecution {
      final Clock snapshotClock = executionContext.getValuationClock();
      final ZonedDateTime now = ZonedDateTime.now(snapshotClock);
      final HistoricalTimeSeriesBundle timeSeries = HistoricalTimeSeriesFunctionUtils.getHistoricalTimeSeriesInputs(executionContext, inputs);
      final InstrumentDefinition<?> definition = getDefinitionFromTarget(target);
      final InstrumentDerivative derivative = getDerivative(target, now, timeSeries, definition);
      return getValues(inputs, target, desiredValues, derivative);
    }

    @Override
    public ComputationTargetType getTargetType() {
      return ComputationTargetType.TRADE;
    }

    @Override
    public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
      return target.getTrade().getSecurity() instanceof FinancialSecurity;
    }

    @Override
    public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
      final ValueProperties properties = getResultProperties(target).get();
      final Set<ValueSpecification> results = new HashSet<>();
      for (final String valueRequirement : _valueRequirements) {
        results.add(new ValueSpecification(valueRequirement, target.toSpecification(), properties));
      }
      return results;
    }

    @Override
    public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
      final ValueProperties constraints = desiredValue.getConstraints();
      if (!requirementsSet(constraints)) {
        return null;
      }
      final Set<String> curveExposureConfigs = constraints.getValues(CURVE_EXPOSURES);
      final FinancialSecurity security = (FinancialSecurity) target.getTrade().getSecurity();
      final ConfigSource configSource = OpenGammaCompilationContext.getConfigSource(context);
      final SecuritySource securitySource = OpenGammaCompilationContext.getSecuritySource(context);
      final ConfigDBInstrumentExposuresProvider exposureSource = new ConfigDBInstrumentExposuresProvider(configSource, securitySource);
      final ConfigDBCurveConstructionConfigurationSource constructionConfigurationSource = new ConfigDBCurveConstructionConfigurationSource(configSource);
      final Set<ValueRequirement> requirements = new HashSet<>();
      final ValueProperties.Builder commonCurveProperties = getCurveProperties(target, constraints);
      for (final String curveExposureConfig : curveExposureConfigs) {
        final Set<String> curveConstructionConfigurationNames = exposureSource.getCurveConstructionConfigurationsForConfig(curveExposureConfig, security);
        for (final String curveConstructionConfigurationName : curveConstructionConfigurationNames) {
          final ValueProperties properties = commonCurveProperties.with(CURVE_CONSTRUCTION_CONFIG, curveConstructionConfigurationNames).get();
          requirements.add(new ValueRequirement(CURVE_BUNDLE, ComputationTargetSpecification.NULL, properties));
          requirements.add(new ValueRequirement(JACOBIAN_BUNDLE, ComputationTargetSpecification.NULL, properties));
          final CurveConstructionConfiguration curveConstructionConfiguration = constructionConfigurationSource.getCurveConstructionConfiguration(curveConstructionConfigurationName);
          final String[] curveNames = CurveUtils.getCurveNamesForConstructionConfiguration(curveConstructionConfiguration);
          for (final String curveName : curveNames) {
            final ValueProperties curveProperties = ValueProperties.builder()
                .with(CURVE, curveName)
                .get();
            requirements.add(new ValueRequirement(CURVE_DEFINITION, ComputationTargetSpecification.NULL, curveProperties));
          }
        }
      }
      try {
        final InstrumentDefinition<?> definition = getDefinitionFromTarget(target);
        final Set<ValueRequirement> timeSeriesRequirements = getConversionTimeSeriesRequirements(context, target, definition);
        if (timeSeriesRequirements == null) {
          return null;
        }
        requirements.addAll(timeSeriesRequirements);
        return requirements;
      } catch (final Exception e) {
        s_logger.error(e.getMessage());
        return null;
      }
    }

    /**
     * Gets an {@link InstrumentDefinition} given a target.
     * @param target The target, not null
     * @return An instrument definition
     */
    protected InstrumentDefinition<?> getDefinitionFromTarget(final ComputationTarget target) {
      return _tradeToDefinitionConverter.convert(target.getTrade());
    }

    /**
     * Gets a conversion time-series for an instrument definition. If no time-series are required,
     * returns an empty set.
     * @param context The compilation context, not null
     * @param target The target, not null
     * @param definition The definition, not null
     * @return A set of time-series requirements
     */
    protected Set<ValueRequirement> getConversionTimeSeriesRequirements(final FunctionCompilationContext context, final ComputationTarget target,
        final InstrumentDefinition<?> definition) {
      return _definitionToDerivativeConverter.getConversionTimeSeriesRequirements(target.getTrade().getSecurity(), definition);
    }

    /**
     * Gets an {@link InstrumentDerivative}.
     * @param target The target, not null
     * @param now The valuation time, not null
     * @param timeSeries The conversion time series bundle, not null but may be empty
     * @param definition The definition, not null
     * @return The instrument derivative
     */
    protected InstrumentDerivative getDerivative(final ComputationTarget target, final ZonedDateTime now, final HistoricalTimeSeriesBundle timeSeries,
        final InstrumentDefinition<?> definition) {
      return _definitionToDerivativeConverter.convert(target.getTrade().getSecurity(), definition, now, timeSeries);
    }

    /**
     * Gets the value requirement names that this function can produce
     * @return The value requirement names
     */
    protected String[] getValueRequirementNames() {
      return _valueRequirements;
    }

    /**
     * Gets the properties for the results given a target.
     * @param target The target, not null
     * @return The result properties
     */
    protected abstract ValueProperties.Builder getResultProperties(ComputationTarget target);

    /**
     * Checks that all constraints have values.
     * @param constraints The constraints, not null
     * @return True if all of the constraints have been set
     */
    protected abstract boolean requirementsSet(ValueProperties constraints);

    /**
     * Gets the properties that are common to all curves.
     * @param target The target, not null
     * @param constraints The input constraints
     * @return The common curve properties
     */
    protected abstract ValueProperties.Builder getCurveProperties(ComputationTarget target, ValueProperties constraints);

    /**
     * Calculates the result.
     * @param inputs The inputs, not null
     * @param target The target, not null
     * @param desiredValues The desired values for this function, not null
     * @param derivative The derivative, not null
     * @return The results
     */
    protected abstract Set<ComputedValue> getValues(FunctionInputs inputs, ComputationTarget target, Set<ValueRequirement> desiredValues,
        InstrumentDerivative derivative);
  }
}
