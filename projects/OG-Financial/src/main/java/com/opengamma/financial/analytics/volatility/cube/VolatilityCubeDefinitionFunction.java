/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.volatility.cube;

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
import com.opengamma.id.UniqueId;
import com.opengamma.util.money.UnorderedCurrencyPair;

/**
 *
 */
public class VolatilityCubeDefinitionFunction extends AbstractFunction {

  private ConfigDBVolatilityCubeDefinitionSource _volatilityCubeDefinitionSource;

  @Override
  public CompiledFunctionDefinition compile(final FunctionCompilationContext outerContext, final Instant atInstant) {
    _volatilityCubeDefinitionSource = ConfigDBVolatilityCubeDefinitionSource.init(outerContext, this);

    final ZonedDateTime atZDT = ZonedDateTime.ofInstant(atInstant, ZoneOffset.UTC);
    return new AbstractInvokingCompiledFunction(atZDT.with(LocalTime.MIDNIGHT), atZDT.plusDays(1).with(LocalTime.MIDNIGHT).minusNanos(1000000)) {

      @Override
      public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target,
          final Set<ValueRequirement> desiredValues) {
        final ValueRequirement desiredValue = desiredValues.iterator().next();
        final String cubeName = desiredValue.getConstraint(ValuePropertyNames.CUBE);
        final String instrumentType = desiredValue.getConstraint(InstrumentTypeProperties.PROPERTY_CUBE_INSTRUMENT_TYPE);
        VolatilityCubeDefinition<?, ?, ?> definition = null;
        if (instrumentType.equals(InstrumentTypeProperties.FOREX)) {
          final UnorderedCurrencyPair pair = UnorderedCurrencyPair.of(target.getUniqueId());
          String name = pair.getFirstCurrency().getCode() + pair.getSecondCurrency().getCode();
          String fullDefinitionName = cubeName + "_" + name;
          definition = _volatilityCubeDefinitionSource.getDefinition(fullDefinitionName, instrumentType);
          if (definition == null) {
            name = pair.getSecondCurrency().getCode() + pair.getFirstCurrency().getCode();
            fullDefinitionName = cubeName + "_" + name;
            definition = _volatilityCubeDefinitionSource.getDefinition(fullDefinitionName, instrumentType);
            if (definition == null) {
              throw new OpenGammaRuntimeException("Could not get volatility cube definition named " + fullDefinitionName);
            }
          }
        } else if (instrumentType.equals(InstrumentTypeProperties.EQUITY_OPTION) || instrumentType.equals(InstrumentTypeProperties.EQUITY_FUTURE_OPTION)) {
          //FIXME: Modify to take ExternalId to avoid incorrect cast to UniqueId
          final String fullDefinitionName = cubeName + "_" + EquitySecurityUtils.getTrimmedTarget(UniqueId.parse(target.getValue().toString()));
          definition = _volatilityCubeDefinitionSource.getDefinition(fullDefinitionName, instrumentType);
          if (definition == null) {
            throw new OpenGammaRuntimeException("Could not get volatility cube definition named " + fullDefinitionName + " for instrument type " + instrumentType);
          }
        } else {
          final String fullDefinitionName = cubeName + "_" + target.getUniqueId().getValue();
          definition = _volatilityCubeDefinitionSource.getDefinition(fullDefinitionName, instrumentType);
          if (definition == null) {
            throw new OpenGammaRuntimeException("Could not get volatility cube definition named " + fullDefinitionName + " for instrument type " + instrumentType);
          }
        }
        @SuppressWarnings("synthetic-access")
        final ValueSpecification spec = new ValueSpecification(ValueRequirementNames.VOLATILITY_CUBE_DEFN, target.toSpecification(), createValueProperties()
            .with(ValuePropertyNames.CUBE, cubeName).with(InstrumentTypeProperties.PROPERTY_CUBE_INSTRUMENT_TYPE, instrumentType).get());
        return Collections.singleton(new ComputedValue(spec, definition));
      }

      @Override
      public ComputationTargetType getTargetType() {
        return ComputationTargetType.ANYTHING;
      }

      @SuppressWarnings("synthetic-access")
      @Override
      public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
        return Collections.singleton(new ValueSpecification(ValueRequirementNames.VOLATILITY_CUBE_DEFN, target.toSpecification(), createValueProperties()
            .withAny(ValuePropertyNames.CUBE).withAny(InstrumentTypeProperties.PROPERTY_CUBE_INSTRUMENT_TYPE).get()));
      }

      @Override
      public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
        final Set<String> cubeNames = desiredValue.getConstraints().getValues(ValuePropertyNames.CUBE);
        if (cubeNames == null || cubeNames.size() != 1) {
          return null;
        }
        final Set<String> instrumentTypes = desiredValue.getConstraints().getValues(InstrumentTypeProperties.PROPERTY_CUBE_INSTRUMENT_TYPE);
        if (instrumentTypes == null || instrumentTypes.size() != 1) {
          return null;
        }
        return Collections.emptySet();
      }

    };
  }

}
