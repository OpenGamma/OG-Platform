/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.volatility;

import java.util.Collections;
import java.util.Set;

import org.threeten.bp.Instant;
import org.threeten.bp.LocalTime;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.CompiledFunctionDefinition;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.SurfaceAndCubePropertyNames;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.model.InstrumentTypeProperties;
import com.opengamma.financial.analytics.model.equity.EquitySecurityUtils;
import com.opengamma.financial.analytics.volatility.surface.ConfigDBVolatilitySurfaceSpecificationSource;
import com.opengamma.financial.analytics.volatility.surface.VolatilitySurfaceSpecification;
import com.opengamma.id.UniqueId;
import com.opengamma.util.money.UnorderedCurrencyPair;

/**
 *
 */
public class VolatilitySurfaceSpecificationFunction extends AbstractFunction {

  private ConfigDBVolatilitySurfaceSpecificationSource _volatilitySurfaceSpecificationSource;

  @Override
  public void init(final FunctionCompilationContext context) {
    _volatilitySurfaceSpecificationSource = ConfigDBVolatilitySurfaceSpecificationSource.init(context, this);
  }

  @Override
  public CompiledFunctionDefinition compile(final FunctionCompilationContext outerContext, final Instant atInstant) {
    final ZonedDateTime atZDT = ZonedDateTime.ofInstant(atInstant, ZoneOffset.UTC);
    return new AbstractInvokingCompiledFunction(atZDT.with(LocalTime.MIDNIGHT).toInstant(), atZDT.plusDays(1).with(LocalTime.MIDNIGHT).minusNanos(1000000).toInstant()) {

      @Override
      public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target,
          final Set<ValueRequirement> desiredValues) {
        final ValueRequirement desiredValue = desiredValues.iterator().next();
        final String surfaceName = desiredValue.getConstraint(ValuePropertyNames.SURFACE);
        final String instrumentType = desiredValue.getConstraint(InstrumentTypeProperties.PROPERTY_SURFACE_INSTRUMENT_TYPE);
        VolatilitySurfaceSpecification specification = null;
        if (instrumentType.equals(InstrumentTypeProperties.FOREX)) {
          final UnorderedCurrencyPair pair = UnorderedCurrencyPair.of(target.getUniqueId());
          String name = pair.getFirstCurrency().getCode() + pair.getSecondCurrency().getCode();
          String fullSpecificationName = surfaceName + "_" + name;
          specification = _volatilitySurfaceSpecificationSource.getSpecification(fullSpecificationName, instrumentType);
          if (specification == null) {
            name = pair.getSecondCurrency().getCode() + pair.getFirstCurrency().getCode();
            fullSpecificationName = surfaceName + "_" + name;
            specification = _volatilitySurfaceSpecificationSource.getSpecification(fullSpecificationName, instrumentType);
            if (specification == null) {
              throw new OpenGammaRuntimeException("Could not get volatility surface specification named " + fullSpecificationName);
            }
          }
        } else if (instrumentType.equals(InstrumentTypeProperties.EQUITY_OPTION) || instrumentType.equals(InstrumentTypeProperties.EQUITY_FUTURE_OPTION)) {
          final String fullSpecificationName = surfaceName + "_" + EquitySecurityUtils.getTrimmedTarget(UniqueId.parse(target.getValue().toString()));
          specification = _volatilitySurfaceSpecificationSource.getSpecification(fullSpecificationName, instrumentType);
          if (specification == null) {
            throw new OpenGammaRuntimeException("Could not get volatility surface specification named " + fullSpecificationName + " for instrument type " + instrumentType);
          }
        } else {
          final String fullSpecificationName = surfaceName + "_" + target.getUniqueId().getValue();
          specification = _volatilitySurfaceSpecificationSource.getSpecification(fullSpecificationName, instrumentType);
          if (specification == null) {
            throw new OpenGammaRuntimeException("Could not get volatility surface specification named " + fullSpecificationName + " for instrument type " + instrumentType);
          }
        }
        @SuppressWarnings("synthetic-access")
        final ValueSpecification spec = new ValueSpecification(ValueRequirementNames.VOLATILITY_SURFACE_SPEC, target.toSpecification(), createValueProperties()
            .with(ValuePropertyNames.SURFACE, surfaceName).with(InstrumentTypeProperties.PROPERTY_SURFACE_INSTRUMENT_TYPE, instrumentType)
            .with(SurfaceAndCubePropertyNames.PROPERTY_SURFACE_QUOTE_TYPE, specification.getSurfaceQuoteType())
            .with(SurfaceAndCubePropertyNames.PROPERTY_SURFACE_UNITS, specification.getQuoteUnits()).get());
        return Collections.singleton(new ComputedValue(spec, specification));
      }

      @Override
      public ComputationTargetType getTargetType() {
        return ComputationTargetType.ANYTHING;
      }

      @SuppressWarnings("synthetic-access")
      @Override
      public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
        return Collections.singleton(new ValueSpecification(ValueRequirementNames.VOLATILITY_SURFACE_SPEC, target.toSpecification(), createValueProperties()
            .withAny(ValuePropertyNames.SURFACE).withAny(InstrumentTypeProperties.PROPERTY_SURFACE_INSTRUMENT_TYPE).withAny(SurfaceAndCubePropertyNames.PROPERTY_SURFACE_QUOTE_TYPE)
            .withAny(SurfaceAndCubePropertyNames.PROPERTY_SURFACE_UNITS).get()));
      }

      @Override
      public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
        final Set<String> surfaceNames = desiredValue.getConstraints().getValues(ValuePropertyNames.SURFACE);
        if (surfaceNames == null || surfaceNames.size() != 1) {
          return null;
        }
        final Set<String> instrumentTypes = desiredValue.getConstraints().getValues(InstrumentTypeProperties.PROPERTY_SURFACE_INSTRUMENT_TYPE);
        if (instrumentTypes == null || instrumentTypes.size() != 1) {
          return null;
        }
        return Collections.emptySet();
      }
    };

  }

}
