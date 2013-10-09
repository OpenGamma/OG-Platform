/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.volatility.cube;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.Instant;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.core.marketdatasnapshot.VolatilityCubeData;
import com.opengamma.core.marketdatasnapshot.VolatilityPoint;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.CompiledFunctionDefinition;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.target.PrimitiveComputationTargetType;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.SurfaceAndCubePropertyNames;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.OpenGammaCompilationContext;
import com.opengamma.financial.analytics.volatility.SwaptionVolatilityCubeSpecificationSource;
import com.opengamma.financial.analytics.volatility.cube.ConfigDBSwaptionVolatilityCubeSpecificationSource;
import com.opengamma.financial.analytics.volatility.cube.CubeInstrumentProvider;
import com.opengamma.financial.analytics.volatility.cube.SwaptionVolatilityCubeSpecification;
import com.opengamma.financial.analytics.volatility.cube.SyntheticSwaptionVolatilityCubeDefinitionSource;
import com.opengamma.financial.analytics.volatility.cube.VolatilityCubeDefinition;
import com.opengamma.financial.analytics.volatility.cube.VolatilityCubeDefinitionSource;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Tenor;

/**
 * 
 */
public class RawSwaptionVolatilityCubeDataFunction extends AbstractFunction {
  private static final Logger s_logger = LoggerFactory.getLogger(RawSwaptionVolatilityCubeDataFunction.class);

  public static Set<ValueRequirement> buildDataRequirements(final SwaptionVolatilityCubeSpecificationSource specificationSource,
      final VolatilityCubeDefinitionSource definitionSource, final ZonedDateTime atInstant, final ComputationTarget target,
      final String specificationName, final String definitionName) {
    final Currency currency = target.getValue(PrimitiveComputationTargetType.CURRENCY);
    final String fullSpecificationName = specificationName + "_" + currency.getCode();
    final String fullDefinitionName = definitionName + "_" + currency.getCode();
    final SwaptionVolatilityCubeSpecification specification = specificationSource.getSpecification(fullSpecificationName);
    if (specification == null) {
      throw new OpenGammaRuntimeException("Could not get swaption volatility cube specification named " + fullSpecificationName);
    }
    final VolatilityCubeDefinition definition = definitionSource.getDefinition(currency, fullDefinitionName);
    if (definition == null) {
      throw new OpenGammaRuntimeException("Could not get swaption volatility cube definition named " + fullDefinitionName);
    }
    final CubeInstrumentProvider<Tenor, Tenor, Double> provider = (CubeInstrumentProvider<Tenor, Tenor, Double>) specification.getCubeInstrumentProvider();
    final Set<ValueRequirement> result = new HashSet<ValueRequirement>();
    for (final Tenor swapTenor : definition.getSwapTenors()) {
      for (final Tenor swaptionExpiry : definition.getOptionExpiries()) {
        for (final Double relativeStrike : definition.getRelativeStrikes()) {
          final ExternalId identifier = provider.getInstrument(swapTenor, swaptionExpiry, relativeStrike);
          result.add(new ValueRequirement(provider.getDataFieldName(), ComputationTargetType.PRIMITIVE, identifier));
        }
      }
    }
    return result;
  }

  @Override
  public CompiledFunctionDefinition compile(final FunctionCompilationContext compilationContext, final Instant atInstant) {
    final ConfigSource configSource = OpenGammaCompilationContext.getConfigSource(compilationContext);
    final SyntheticSwaptionVolatilityCubeDefinitionSource definitionSource = new SyntheticSwaptionVolatilityCubeDefinitionSource(configSource);
    final ConfigDBSwaptionVolatilityCubeSpecificationSource specificationSource = new ConfigDBSwaptionVolatilityCubeSpecificationSource(configSource);
    final ZonedDateTime atZDT = ZonedDateTime.ofInstant(atInstant, ZoneOffset.UTC);
    return new AbstractInvokingCompiledFunction() {

      @SuppressWarnings("synthetic-access")
      @Override
      public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
        final ValueRequirement desiredValue = desiredValues.iterator().next();
        final String cubeName = desiredValue.getConstraint(ValuePropertyNames.CUBE);
        final String definitionName = desiredValue.getConstraint(SurfaceAndCubePropertyNames.PROPERTY_CUBE_DEFINITION);
        final String specificationName = desiredValue.getConstraint(SurfaceAndCubePropertyNames.PROPERTY_CUBE_SPECIFICATION);
        final Currency currency = target.getValue(PrimitiveComputationTargetType.CURRENCY);
        final String fullSpecificationName = specificationName + "_" + currency.getCode();
        final String fullDefinitionName = definitionName + "_" + currency.getCode();
        final SwaptionVolatilityCubeSpecification specification = specificationSource.getSpecification(fullSpecificationName);
        if (specification == null) {
          throw new OpenGammaRuntimeException("Could not get swaption volatility cube specification named " + fullSpecificationName);
        }
        final VolatilityCubeDefinition definition = definitionSource.getDefinition(currency, fullDefinitionName);
        if (definition == null) {
          throw new OpenGammaRuntimeException("Could not get swaption volatility cube definition named " + fullDefinitionName);
        }
        final CubeInstrumentProvider<Tenor, Tenor, Double> provider = (CubeInstrumentProvider<Tenor, Tenor, Double>) specification.getCubeInstrumentProvider();
        final Map<VolatilityPoint, Double> data = new HashMap<VolatilityPoint, Double>();
        final Map<VolatilityPoint, ExternalIdBundle> ids = new HashMap<VolatilityPoint, ExternalIdBundle>();
        for (final Tenor x : definition.getSwapTenors()) {
          for (final Tenor y : definition.getOptionExpiries()) {
            for (final Double z : definition.getRelativeStrikes()) {
              final ExternalId id = provider.getInstrument(x, y, z);
              final ValueRequirement requirement = new ValueRequirement(provider.getDataFieldName(), ComputationTargetType.PRIMITIVE, id);
              final Object volatilityObject = inputs.getValue(requirement);
              if (volatilityObject != null) {
                final Double volatility = (Double) volatilityObject;
                final VolatilityPoint coordinate = new VolatilityPoint(x, y, z);
                data.put(coordinate, volatility);
                ids.put(coordinate, id.toBundle());
              }
            }
          }
        }
        final VolatilityCubeData volatilityCubeData = new VolatilityCubeData();
        volatilityCubeData.setDataPoints(data);
        volatilityCubeData.setDataIds(ids);
        final ValueProperties properties = createValueProperties()
            .with(ValuePropertyNames.CUBE, cubeName)
            .with(SurfaceAndCubePropertyNames.PROPERTY_CUBE_DEFINITION, definitionName)
            .with(SurfaceAndCubePropertyNames.PROPERTY_CUBE_SPECIFICATION, specificationName)
            .with(SurfaceAndCubePropertyNames.PROPERTY_CUBE_QUOTE_TYPE, specification.getCubeQuoteType())
            .with(SurfaceAndCubePropertyNames.PROPERTY_CUBE_UNITS, specification.getQuoteUnits()).get();
        return Collections.singleton(new ComputedValue(new ValueSpecification(ValueRequirementNames.VOLATILITY_CUBE_MARKET_DATA, target.toSpecification(), properties), volatilityCubeData));
      }

      @Override
      public boolean canHandleMissingInputs() {
        return true;
      }

      @Override
      public boolean canHandleMissingRequirements() {
        return true;
      }

      @Override
      public ComputationTargetType getTargetType() {
        return ComputationTargetType.CURRENCY;
      }

      @SuppressWarnings("synthetic-access")
      @Override
      public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
        final ValueProperties properties = createValueProperties()
            .withAny(ValuePropertyNames.CUBE)
            .withAny(SurfaceAndCubePropertyNames.PROPERTY_CUBE_DEFINITION)
            .withAny(SurfaceAndCubePropertyNames.PROPERTY_CUBE_SPECIFICATION)
            .withAny(SurfaceAndCubePropertyNames.PROPERTY_CUBE_QUOTE_TYPE)
            .withAny(SurfaceAndCubePropertyNames.PROPERTY_CUBE_UNITS).get();
        return Collections.singleton(new ValueSpecification(ValueRequirementNames.VOLATILITY_CUBE_MARKET_DATA, target.toSpecification(), properties));
      }

      @SuppressWarnings("synthetic-access")
      @Override
      public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
        final ValueProperties constraints = desiredValue.getConstraints();
        final Set<String> cubeNames = constraints.getValues(ValuePropertyNames.CUBE);
        if (cubeNames == null || cubeNames.size() != 1) {
          s_logger.info("Can only get a single cube; asked for " + cubeNames);
          return null;
        }
        final Set<String> definitionNames = constraints.getValues(SurfaceAndCubePropertyNames.PROPERTY_CUBE_DEFINITION);
        if (definitionNames == null || definitionNames.size() != 1) {
          return null;
        }
        final Set<String> specificationNames = constraints.getValues(SurfaceAndCubePropertyNames.PROPERTY_CUBE_SPECIFICATION);
        if (specificationNames == null || specificationNames.size() != 1) {
          return null;
        }
        final String definitionName = definitionNames.iterator().next();
        final String specificationName = specificationNames.iterator().next();
        return buildDataRequirements(specificationSource, definitionSource, atZDT, target, specificationName, definitionName);
      }
    };
  }
}
