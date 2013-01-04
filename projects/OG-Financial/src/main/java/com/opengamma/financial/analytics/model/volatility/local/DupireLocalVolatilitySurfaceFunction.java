/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.volatility.local;

import static com.opengamma.financial.analytics.model.volatility.local.LocalVolatilitySurfacePropertyNamesAndValues.PROPERTY_DERIVATIVE_EPS;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.model.volatility.local.DupireLocalVolatilityCalculator;
import com.opengamma.analytics.financial.model.volatility.local.LocalVolatilitySurfaceMoneyness;
import com.opengamma.analytics.financial.model.volatility.surface.BlackVolatilitySurfaceMoneyness;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.model.volatility.surface.black.BlackVolatilitySurfacePropertyUtils;

/**
 *
 */
public abstract class DupireLocalVolatilitySurfaceFunction extends AbstractFunction.NonCompiledInvoker {

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final ValueRequirement desiredValue = desiredValues.iterator().next();
    final double eps = Double.parseDouble(desiredValue.getConstraint(PROPERTY_DERIVATIVE_EPS));
    final Object impliedVolatilitySurfaceObject = inputs.getValue(getVolatilitySurfaceRequirement(target, desiredValue));
    if (impliedVolatilitySurfaceObject == null) {
      throw new OpenGammaRuntimeException("Volatility surface was null");
    }
    final BlackVolatilitySurfaceMoneyness impliedVolatilitySurface = (BlackVolatilitySurfaceMoneyness) impliedVolatilitySurfaceObject;
    final DupireLocalVolatilityCalculator calculator = new DupireLocalVolatilityCalculator(eps);
    final LocalVolatilitySurfaceMoneyness localVolatilitySurface = calculator.getLocalVolatility(impliedVolatilitySurface);
    final ValueProperties properties = getResultProperties(desiredValue, LocalVolatilitySurfacePropertyNamesAndValues.MONEYNESS);
    final ValueSpecification spec = new ValueSpecification(ValueRequirementNames.LOCAL_VOLATILITY_SURFACE, target.toSpecification(), properties);
    return Collections.singleton(new ComputedValue(spec, localVolatilitySurface));
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final ValueProperties properties = getResultProperties(LocalVolatilitySurfacePropertyNamesAndValues.MONEYNESS);
    return Collections.singleton(new ValueSpecification(ValueRequirementNames.LOCAL_VOLATILITY_SURFACE, target.toSpecification(), properties));
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final ValueProperties constraints = desiredValue.getConstraints();
    final Set<ValueRequirement> requirements = LocalVolatilitySurfaceUtils.ensureDupireLocalVolatilitySurfaceProperties(constraints);
    if (requirements == null) {
      return null;
    }
    final ValueRequirement volatilitySurfaceRequirement = getVolatilitySurfaceRequirement(target, desiredValue);
    return Sets.newHashSet(volatilitySurfaceRequirement);
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target, final Map<ValueSpecification, ValueRequirement> inputs) {
    ValueProperties.Builder properties = LocalVolatilitySurfaceUtils.addDupireLocalVolatilitySurfaceProperties(createValueProperties().get(), LocalVolatilitySurfacePropertyNamesAndValues.MONEYNESS);
    for (final Map.Entry<ValueSpecification, ValueRequirement> entry : inputs.entrySet()) {
      final ValueProperties inputProperties = entry.getValue().getConstraints();
      final Set<String> propertyNames = inputProperties.getProperties();
      for (final String propertyName : propertyNames) {
        properties = properties.with(propertyName, inputProperties.getValues(propertyName));
      }
    }
    return Collections.singleton(new ValueSpecification(ValueRequirementNames.LOCAL_VOLATILITY_SURFACE, target.toSpecification(), properties.get()));
  }

  protected abstract String getInstrumentType();

  protected abstract String getBlackSmileInterpolatorName();

  protected ValueProperties getResultProperties(final String parameterizationType) {
    return LocalVolatilitySurfaceUtils.addAllDupireLocalVolatilitySurfaceProperties(createValueProperties().get(), getInstrumentType(), getBlackSmileInterpolatorName(), parameterizationType).get();
  }

  protected ValueProperties getResultProperties(final ValueRequirement desiredValue, final String parameterizationType) {
    return LocalVolatilitySurfaceUtils.addAllDupireLocalVolatilitySurfaceProperties(createValueProperties().get(), getInstrumentType(), getBlackSmileInterpolatorName(), parameterizationType,
        desiredValue).get();
  }

  protected ValueRequirement getVolatilitySurfaceRequirement(final ComputationTarget target, final ValueRequirement desiredValue) {
    final ValueProperties properties = BlackVolatilitySurfacePropertyUtils.addAllBlackSurfaceProperties(ValueProperties.builder().get(), getInstrumentType(), desiredValue).get();
    return new ValueRequirement(ValueRequirementNames.BLACK_VOLATILITY_SURFACE, target.toSpecification(), properties);
  }

}
