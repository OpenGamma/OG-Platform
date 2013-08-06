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
import com.opengamma.core.security.SecuritySource;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.OpenGammaCompilationContext;
import com.opengamma.financial.analytics.curve.ConfigDBCurveConstructionConfigurationSource;
import com.opengamma.financial.analytics.curve.CurveConstructionConfiguration;
import com.opengamma.financial.analytics.curve.CurveUtils;
import com.opengamma.financial.analytics.curve.exposure.ConfigDBInstrumentExposuresProvider;
import com.opengamma.financial.analytics.timeseries.HistoricalTimeSeriesBundle;
import com.opengamma.financial.analytics.timeseries.HistoricalTimeSeriesFunctionUtils;
import com.opengamma.financial.security.FinancialSecurity;
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
   * Base compiled function for all multi-curve pricing and risk functions.
   */
  public abstract class MultiCurveCompiledFunction extends AbstractInvokingCompiledFunction {

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
      final FinancialSecurity security = getSecurityFromTarget(target);
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
     * Gets the value requirement names that this function can produce
     * @return The value requirement names
     */
    protected String[] getValueRequirementNames() {
      return _valueRequirements;
    }

    /**
     * Gets the security given a target.
     * @param target The target, not null
     * @return A security
     */
    protected abstract FinancialSecurity getSecurityFromTarget(ComputationTarget target);

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
     * Gets an {@link InstrumentDefinition} given a target.
     * @param target The target, not null
     * @return An instrument definition
     */
    protected abstract InstrumentDefinition<?> getDefinitionFromTarget(ComputationTarget target);

    /**
     * Gets a conversion time-series for an instrument definition. If no time-series are required,
     * returns an empty set.
     * @param compilationContext The compilation context, not null
     * @param target The target, not null
     * @param definition The definition, not null
     * @return A set of time-series requirements
     */
    protected abstract Set<ValueRequirement> getConversionTimeSeriesRequirements(FunctionCompilationContext compilationContext, ComputationTarget target,
        InstrumentDefinition<?> definition);

    /**
     * Gets an {@link InstrumentDerivative}.
     * @param target The target, not null
     * @param now The valuation time, not null
     * @param timeSeries The conversion time series bundle, not null but may be empty
     * @param definition The definition, not null
     * @return The instrument derivative
     */
    protected abstract InstrumentDerivative getDerivative(ComputationTarget target, ZonedDateTime now, HistoricalTimeSeriesBundle timeSeries,
        InstrumentDefinition<?> definition);

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
