/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.volatility.surface;

import javax.time.InstantProvider;
import javax.time.calendar.TimeZone;
import javax.time.calendar.ZonedDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.core.config.ConfigSource;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.CompiledFunctionDefinition;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.financial.OpenGammaCompilationContext;
import com.opengamma.financial.analytics.model.InstrumentTypeProperties;
import com.opengamma.util.money.UnorderedCurrencyPair;

/**
 *
 */
public class RawFXVolatilitySurfaceDataFunction extends RawVolatilitySurfaceDataFunction {
  private static final Logger s_logger = LoggerFactory.getLogger(RawFXVolatilitySurfaceDataFunction.class);

  public RawFXVolatilitySurfaceDataFunction() {
    super(InstrumentTypeProperties.FOREX);
  }

  @Override
  protected ComputationTargetType getTargetType() {
    return ComputationTargetType.UNORDERED_CURRENCY_PAIR;
  }

  @Override
  public CompiledFunctionDefinition compile(final FunctionCompilationContext myContext, final InstantProvider atInstantProvider) {
    final ConfigSource configSource = OpenGammaCompilationContext.getConfigSource(myContext);
    final ConfigDBVolatilitySurfaceDefinitionSource definitionSource = new ConfigDBVolatilitySurfaceDefinitionSource(configSource);
    final ConfigDBVolatilitySurfaceSpecificationSource specificationSource = new ConfigDBVolatilitySurfaceSpecificationSource(configSource);
    final ZonedDateTime atInstant = ZonedDateTime.ofInstant(atInstantProvider, TimeZone.UTC);
    return new FXVolatilitySurfaceCompiledFunction(atInstant.withTime(0, 0), atInstant.plusDays(1).withTime(0, 0).minusNanos(1000000), atInstant, definitionSource, specificationSource);
  }

  /**
   * Implementation of the compiled function
   */
  protected class FXVolatilitySurfaceCompiledFunction extends CompiledFunction {

    public FXVolatilitySurfaceCompiledFunction(final ZonedDateTime from, final ZonedDateTime to, final ZonedDateTime now,
        final ConfigDBVolatilitySurfaceDefinitionSource definitionSource, final ConfigDBVolatilitySurfaceSpecificationSource specificationSource) {
      super(from, to, now, definitionSource, specificationSource);
    }

    @Override
    protected VolatilitySurfaceDefinition<Object, Object> getSurfaceDefinition(final ComputationTarget target, final String definitionName, final String instrumentType) {
      final UnorderedCurrencyPair pair = UnorderedCurrencyPair.of(target.getUniqueId());
      String name = pair.getFirstCurrency().getCode() + pair.getSecondCurrency().getCode();
      String fullDefinitionName = definitionName + "_" + name;
      VolatilitySurfaceDefinition<Object, Object> definition = (VolatilitySurfaceDefinition<Object, Object>) getDefinitionSource().getDefinition(fullDefinitionName, instrumentType);
      if (definition == null) {
        name = pair.getSecondCurrency().getCode() + pair.getFirstCurrency().getCode();
        fullDefinitionName = definitionName + "_" + name;
        definition = (VolatilitySurfaceDefinition<Object, Object>) getDefinitionSource().getDefinition(fullDefinitionName, instrumentType);
        if (definition == null) {
          s_logger.error("Could not get volatility surface definition named " + fullDefinitionName + " for instrument type " + instrumentType);
          return null;
        }
      }
      return definition;
    }

    @Override
    protected VolatilitySurfaceSpecification getSurfaceSpecification(final ComputationTarget target, final String specificationName, final String instrumentType) {
      final UnorderedCurrencyPair pair = UnorderedCurrencyPair.of(target.getUniqueId());
      String name = pair.getFirstCurrency().getCode() + pair.getSecondCurrency().getCode();
      String fullSpecificationName = specificationName + "_" + name;
      VolatilitySurfaceSpecification specification = getSpecificationSource().getSpecification(fullSpecificationName, instrumentType);
      if (specification == null) {
        name = pair.getSecondCurrency().getCode() + pair.getFirstCurrency().getCode();
        fullSpecificationName = specificationName + "_" + name;
        specification = getSpecificationSource().getSpecification(fullSpecificationName, instrumentType);
        if (specification == null) {
          s_logger.error("Could not get volatility surface specification named " + fullSpecificationName);
          return null;
        }
      }
      return specification;
    }
  }
}
