/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.volatility.surface.fitting;

import java.util.Collections;
import java.util.Set;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.marketdatasnapshot.VolatilitySurfaceData;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.volatility.surface.RawVolatilitySurfaceDataFunction;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Tenor;

/**
 * 
 */
public class NonLinearLeastSquaresSurfaceFittingFunction extends AbstractFunction.NonCompiledInvoker {
  private final Currency _currency;
  private final String _definitionName;
  private final String _instrumentType;
  private final String _resultName;
  private ValueRequirement _surfaceRequirement;
  private ValueSpecification _resultSpecification;

  public NonLinearLeastSquaresSurfaceFittingFunction(final String currency, final String definitionName, final String instrumentType, final String resultName) {
    this(Currency.of(currency), definitionName, instrumentType, resultName);
  }

  public NonLinearLeastSquaresSurfaceFittingFunction(final Currency currency, final String definitionName, final String instrumentType, final String resultName) {
    Validate.notNull(currency, "currency");
    Validate.notNull(definitionName, "definition name");
    Validate.notNull(instrumentType, "instrument type");
    Validate.notNull(resultName, "result name");
    _currency = currency;
    _definitionName = definitionName;
    _instrumentType = instrumentType;
    _resultName = resultName;
  }

  @Override
  public void init(final FunctionCompilationContext context) {
    final ComputationTargetSpecification currencyTargetSpec = new ComputationTargetSpecification(_currency);
    final ValueProperties surfaceProperties = ValueProperties.with(ValuePropertyNames.SURFACE, _definitionName)
        .with(RawVolatilitySurfaceDataFunction.PROPERTY_SURFACE_INSTRUMENT_TYPE, _instrumentType).get();
    _surfaceRequirement = new ValueRequirement(ValueRequirementNames.VOLATILITY_SURFACE_DATA, currencyTargetSpec, surfaceProperties);
    final ValueProperties resultProperties = createValueProperties()
          .with(ValuePropertyNames.CURRENCY, _currency.getCode())
          .with(ValuePropertyNames.SURFACE, _definitionName)
          .with(RawVolatilitySurfaceDataFunction.PROPERTY_SURFACE_INSTRUMENT_TYPE, _instrumentType)
          .get();
    _resultSpecification = new ValueSpecification(_resultName, currencyTargetSpec, resultProperties);
  }

  @SuppressWarnings("unused")
  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final Object objectSurfaceData = inputs.getValue(_surfaceRequirement);
    if (objectSurfaceData == null) {
      throw new OpenGammaRuntimeException("Could not get volatility surface data");
    }
    @SuppressWarnings("unchecked")
    final VolatilitySurfaceData<Tenor, Double> volatilitySurfaceData = (VolatilitySurfaceData<Tenor, Double>) objectSurfaceData;
    final Tenor[] tenors = volatilitySurfaceData.getXs();
    final Double[] strikes = volatilitySurfaceData.getYs();

    return Collections.singleton(new ComputedValue(_resultSpecification, 0));
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.PRIMITIVE;
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    if (target.getType() != ComputationTargetType.PRIMITIVE) {
      return false;
    }
    return ObjectUtils.equals(target.getUniqueId(), _currency);
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    return Collections.singleton(_surfaceRequirement);
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    return Collections.singleton(_resultSpecification);
  }

}
