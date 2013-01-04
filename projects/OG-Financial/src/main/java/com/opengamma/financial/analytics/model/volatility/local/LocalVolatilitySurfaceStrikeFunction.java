/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.volatility.local;

import java.util.Collections;
import java.util.Set;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.model.volatility.local.LocalVolatilitySurfaceConverter;
import com.opengamma.analytics.financial.model.volatility.local.LocalVolatilitySurfaceMoneyness;
import com.opengamma.analytics.financial.model.volatility.local.LocalVolatilitySurfaceStrike;
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

/**
 * Takes a LocalVolatilitySurface with Surface Parameterization of MONEYNESS and
 * returns one of STRIKE
 */
public abstract class LocalVolatilitySurfaceStrikeFunction extends AbstractFunction.NonCompiledInvoker {

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final ValueRequirement desiredValue = desiredValues.iterator().next();
    final ValueRequirement volatilitySurfaceRequirement = getVolatilitySurfaceRequirement(target, desiredValue);
    final Object localVolatilityObject = inputs.getValue(volatilitySurfaceRequirement);
    if (localVolatilityObject == null) {
      throw new OpenGammaRuntimeException("Could not get local volatility surface");
    }
    final LocalVolatilitySurfaceMoneyness localVolatilityMoneyness = (LocalVolatilitySurfaceMoneyness) localVolatilityObject;
    final LocalVolatilitySurfaceStrike localVolatilityStrike = LocalVolatilitySurfaceConverter.toStrikeSurface(localVolatilityMoneyness);
    final ValueProperties properties = getResultProperties(desiredValue, LocalVolatilitySurfacePropertyNamesAndValues.STRIKE);
    final ValueSpecification spec = new ValueSpecification(ValueRequirementNames.LOCAL_VOLATILITY_SURFACE, target.toSpecification(), properties);
    return Collections.singleton(new ComputedValue(spec, localVolatilityStrike));
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final ValueProperties properties = getResultProperties(LocalVolatilitySurfacePropertyNamesAndValues.STRIKE);
    return Collections.singleton(new ValueSpecification(ValueRequirementNames.LOCAL_VOLATILITY_SURFACE, target.toSpecification(), properties));
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final ValueProperties constraints = desiredValue.getConstraints();
    final Set<ValueRequirement> requirements = LocalVolatilitySurfaceUtils.ensureDupireLocalVolatilitySurfaceProperties(constraints);
    if (requirements == null) {
      return null;
    }
    return Collections.singleton(getVolatilitySurfaceRequirement(target, desiredValue));
  }

  protected abstract String getInstrumentType();

  protected abstract String getBlackSmileInterpolatorName();

  private ValueProperties getResultProperties(final String parameterizationType) {
    return LocalVolatilitySurfaceUtils.addAllDupireLocalVolatilitySurfaceProperties(createValueProperties().get(), getInstrumentType(), getBlackSmileInterpolatorName(), parameterizationType).get();
  }

  private ValueProperties getResultProperties(final ValueRequirement desiredValue, final String parameterizationType) {
    return LocalVolatilitySurfaceUtils.addAllDupireLocalVolatilitySurfaceProperties(createValueProperties().get(), getInstrumentType(), getBlackSmileInterpolatorName(), parameterizationType,
        desiredValue).get();
  }

  private ValueRequirement getVolatilitySurfaceRequirement(final ComputationTarget target, final ValueRequirement desiredValue) {
    final ValueProperties properties = LocalVolatilitySurfaceUtils.addAllDupireLocalVolatilitySurfaceProperties(ValueProperties.builder().get(), getInstrumentType(),
        getBlackSmileInterpolatorName(), LocalVolatilitySurfacePropertyNamesAndValues.MONEYNESS, desiredValue).get();
    return new ValueRequirement(ValueRequirementNames.LOCAL_VOLATILITY_SURFACE, target.toSpecification(), properties);
  }
}
