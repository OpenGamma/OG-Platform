/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.bondcurves;

import static com.opengamma.engine.value.ValuePropertyNames.CALCULATION_METHOD;
import static com.opengamma.engine.value.ValuePropertyNames.CURVE_CONSTRUCTION_CONFIG;
import static com.opengamma.engine.value.ValuePropertyNames.CURVE_EXPOSURES;
import static com.opengamma.engine.value.ValueRequirementNames.CURVE_BUNDLE;
import static com.opengamma.engine.value.ValueRequirementNames.JACOBIAN_BUNDLE;
import static com.opengamma.financial.analytics.model.CalculationPropertyNamesAndValues.CURVES_METHOD;
import static com.opengamma.financial.analytics.model.curve.CurveCalculationPropertyNamesAndValues.PROPERTY_CURVE_TYPE;
import static com.opengamma.financial.analytics.model.curve.interestrate.MultiYieldCurvePropertiesAndDefaults.PROPERTY_ROOT_FINDER_ABSOLUTE_TOLERANCE;
import static com.opengamma.financial.analytics.model.curve.interestrate.MultiYieldCurvePropertiesAndDefaults.PROPERTY_ROOT_FINDER_MAX_ITERATIONS;
import static com.opengamma.financial.analytics.model.curve.interestrate.MultiYieldCurvePropertiesAndDefaults.PROPERTY_ROOT_FINDER_RELATIVE_TOLERANCE;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.ZonedDateTime;

import com.google.common.collect.Iterables;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.provider.description.interestrate.ParameterIssuerProviderInterface;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.core.security.Security;
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
import com.opengamma.financial.analytics.curve.exposure.ConfigDBInstrumentExposuresProvider;
import com.opengamma.financial.analytics.model.BondFunctionUtils;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.bond.BondSecurity;
import com.opengamma.financial.security.future.BondFutureSecurity;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.async.AsynchronousExecution;

/**
 * Base class for bond and bond future analytic calculations from yield curves.
 * @param <S> The type of the curves required by the calculator
 * @param <T> The type of the result
 */
public abstract class BondFromCurvesFunction<S extends ParameterIssuerProviderInterface, T> extends AbstractFunction.NonCompiledInvoker {
  /** The logger */
  private static final Logger s_logger = LoggerFactory.getLogger(BondFromCurvesFunction.class);
  /** The value requirement name */
  private final String _valueRequirementName;
  /** The calculator */
  private final InstrumentDerivativeVisitor<S, T> _calculator;

  /**
   * @param valueRequirementName The value requirement name, not null
   * @param calculator The calculator
   */
  public BondFromCurvesFunction(final String valueRequirementName, final InstrumentDerivativeVisitor<S, T> calculator) {
    ArgumentChecker.notNull(valueRequirementName, "value requirement");
    _valueRequirementName = valueRequirementName;
    _calculator = calculator;
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target,
      final Set<ValueRequirement> desiredValues) throws AsynchronousExecution {
    final ValueRequirement desiredValue = Iterables.getOnlyElement(desiredValues);
    final ValueProperties properties = desiredValue.getConstraints();
    final ZonedDateTime now = ZonedDateTime.now(executionContext.getValuationClock());
    final InstrumentDerivative derivative = BondFunctionUtils.getDerivative(executionContext, target, now);
    final S issuerCurves = (S) inputs.getValue(CURVE_BUNDLE);
    final ValueSpecification spec = new ValueSpecification(_valueRequirementName, target.toSpecification(), properties);
    final T result = derivative.accept(_calculator, issuerCurves);
    return Collections.singleton(new ComputedValue(spec, result));
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.TRADE;
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    final Security security = target.getTrade().getSecurity();
    return security instanceof BondSecurity ||
        security instanceof BondFutureSecurity;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final ValueProperties properties = getResultProperties(target).get();
    return Collections.singleton(new ValueSpecification(_valueRequirementName, target.toSpecification(), properties));
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final ValueProperties constraints = desiredValue.getConstraints();
    final Set<String> curveExposureConfigs = constraints.getValues(CURVE_EXPOSURES);
    if (curveExposureConfigs == null || curveExposureConfigs.size() != 1) {
      return null;
    }
    final Set<String> absoluteTolerances = constraints.getValues(PROPERTY_ROOT_FINDER_ABSOLUTE_TOLERANCE);
    if (absoluteTolerances == null || absoluteTolerances.size() != 1) {
      return null;
    }
    final Set<String> curveTypes = constraints.getValues(PROPERTY_CURVE_TYPE);
    if (curveTypes == null || curveTypes.size() != 1) {
      return null;
    }
    final FinancialSecurity security = (FinancialSecurity) target.getTrade().getSecurity();
    final Set<ValueRequirement> requirements = new HashSet<>();
    try {
      final ConfigSource configSource = OpenGammaCompilationContext.getConfigSource(context);
      final SecuritySource securitySource = OpenGammaCompilationContext.getSecuritySource(context);
      final ConfigDBInstrumentExposuresProvider exposureSource = new ConfigDBInstrumentExposuresProvider(configSource, securitySource);
      for (final String curveExposureConfig : curveExposureConfigs) {
        final Set<String> curveConstructionConfigurationNames = exposureSource.getCurveConstructionConfigurationsForConfig(curveExposureConfig, security);
        for (final String curveConstructionConfigurationName : curveConstructionConfigurationNames) {
          final ValueProperties properties = ValueProperties.builder()
              .with(CURVE_CONSTRUCTION_CONFIG, curveConstructionConfigurationName)
              .with(PROPERTY_ROOT_FINDER_ABSOLUTE_TOLERANCE, constraints.getValues(PROPERTY_ROOT_FINDER_ABSOLUTE_TOLERANCE))
              .with(PROPERTY_ROOT_FINDER_RELATIVE_TOLERANCE, constraints.getValues(PROPERTY_ROOT_FINDER_RELATIVE_TOLERANCE))
              .with(PROPERTY_ROOT_FINDER_MAX_ITERATIONS, constraints.getValues(PROPERTY_ROOT_FINDER_MAX_ITERATIONS))
              .with(PROPERTY_CURVE_TYPE, curveTypes)
              .get();
          requirements.add(new ValueRequirement(CURVE_BUNDLE, ComputationTargetSpecification.NULL, properties));
          requirements.add(new ValueRequirement(JACOBIAN_BUNDLE, ComputationTargetSpecification.NULL, properties));
        }
      }
      return requirements;
    } catch (final Exception e) {
      s_logger.error(e.getMessage(), e);
      return null;
    }
  }
  /**
   * Gets the value properties of the result
   * @param target The computation target
   * @return The properties
   */
  protected ValueProperties.Builder getResultProperties(final ComputationTarget target) {
    return createValueProperties()
        .with(CALCULATION_METHOD, CURVES_METHOD)
        .withAny(CURVE_EXPOSURES)
        .withAny(PROPERTY_CURVE_TYPE)
        .withAny(PROPERTY_ROOT_FINDER_ABSOLUTE_TOLERANCE)
        .withAny(PROPERTY_ROOT_FINDER_RELATIVE_TOLERANCE)
        .withAny(PROPERTY_ROOT_FINDER_MAX_ITERATIONS);
  }

}
