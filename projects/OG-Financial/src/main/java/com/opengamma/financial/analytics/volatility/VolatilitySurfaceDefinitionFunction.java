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
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.model.InstrumentTypeProperties;
import com.opengamma.financial.analytics.model.equity.EquitySecurityUtils;
import com.opengamma.financial.analytics.volatility.surface.ConfigDBVolatilitySurfaceDefinitionSource;
import com.opengamma.financial.analytics.volatility.surface.VolatilitySurfaceDefinition;
import com.opengamma.id.UniqueId;
import com.opengamma.util.money.UnorderedCurrencyPair;

/**
 *
 */
public class VolatilitySurfaceDefinitionFunction extends AbstractFunction {

  private ConfigDBVolatilitySurfaceDefinitionSource _volatilitySurfaceDefinitionSource;

  @Override
  public void init(final FunctionCompilationContext context) {
    _volatilitySurfaceDefinitionSource = ConfigDBVolatilitySurfaceDefinitionSource.init(context, this);
  }

  @Override
  public CompiledFunctionDefinition compile(final FunctionCompilationContext outerContext, final Instant atInstant) {
    final ZonedDateTime atZDT = ZonedDateTime.ofInstant(atInstant, ZoneOffset.UTC);
    return new AbstractInvokingCompiledFunction(atZDT.with(LocalTime.MIDNIGHT), atZDT.plusDays(1).with(LocalTime.MIDNIGHT).minusNanos(1000000)) {

      @Override
      public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target,
          final Set<ValueRequirement> desiredValues) {
        final ValueRequirement desiredValue = desiredValues.iterator().next();
        final String surfaceName = desiredValue.getConstraint(ValuePropertyNames.SURFACE);
        final String instrumentType = desiredValue.getConstraint(InstrumentTypeProperties.PROPERTY_SURFACE_INSTRUMENT_TYPE);
        VolatilitySurfaceDefinition<?, ?> definition = null;
        if (instrumentType.equals(InstrumentTypeProperties.FOREX)) {
          final UnorderedCurrencyPair pair = UnorderedCurrencyPair.of(target.getUniqueId());
          String name = pair.getFirstCurrency().getCode() + pair.getSecondCurrency().getCode();
          String fullDefinitionName = surfaceName + "_" + name;
          definition = _volatilitySurfaceDefinitionSource.getDefinition(fullDefinitionName, instrumentType);
          if (definition == null) {
            name = pair.getSecondCurrency().getCode() + pair.getFirstCurrency().getCode();
            fullDefinitionName = surfaceName + "_" + name;
            definition = _volatilitySurfaceDefinitionSource.getDefinition(fullDefinitionName, instrumentType);
            if (definition == null) {
              throw new OpenGammaRuntimeException("Could not get volatility surface definition named " + fullDefinitionName);
            }
          }
        } else if (instrumentType.equals(InstrumentTypeProperties.EQUITY_OPTION) || instrumentType.equals(InstrumentTypeProperties.EQUITY_FUTURE_OPTION)) {
          //FIXME: Modify to take ExternalId to avoid incorrect cast to UniqueId
          final String fullDefinitionName = surfaceName + "_" + EquitySecurityUtils.getTrimmedTarget(UniqueId.parse(target.getValue().toString()));
          definition = _volatilitySurfaceDefinitionSource.getDefinition(fullDefinitionName, instrumentType);
          if (definition == null) {
            throw new OpenGammaRuntimeException("Could not get volatility surface definition named " + fullDefinitionName + " for instrument type " + instrumentType);
          }
        } else {
          final String fullDefinitionName = surfaceName + "_" + target.getUniqueId().getValue();
          definition = _volatilitySurfaceDefinitionSource.getDefinition(fullDefinitionName, instrumentType);
          if (definition == null) {
            throw new OpenGammaRuntimeException("Could not get volatility surface definition named " + fullDefinitionName + " for instrument type " + instrumentType);
          }
        }
        @SuppressWarnings("synthetic-access")
        final ValueSpecification spec = new ValueSpecification(ValueRequirementNames.VOLATILITY_SURFACE_DEFINITION, target.toSpecification(), createValueProperties()
            .with(ValuePropertyNames.SURFACE, surfaceName).with(InstrumentTypeProperties.PROPERTY_SURFACE_INSTRUMENT_TYPE, instrumentType).get());
        return Collections.singleton(new ComputedValue(spec, definition));
      }

      @Override
      public ComputationTargetType getTargetType() {
        return ComputationTargetType.ANYTHING;
      }

      @SuppressWarnings("synthetic-access")
      @Override
      public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
        return Collections.singleton(new ValueSpecification(ValueRequirementNames.VOLATILITY_SURFACE_DEFINITION, target.toSpecification(), createValueProperties()
            .withAny(ValuePropertyNames.SURFACE).withAny(InstrumentTypeProperties.PROPERTY_SURFACE_INSTRUMENT_TYPE).get()));
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
