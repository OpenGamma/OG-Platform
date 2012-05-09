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

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.CompiledFunctionDefinition;
import com.opengamma.engine.function.FunctionCompilationContext;
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
  public boolean isCorrectIdType(final ComputationTarget target) {
    return UnorderedCurrencyPair.OBJECT_SCHEME.equals(target.getUniqueId().getScheme());
  }

  @Override
  public CompiledFunctionDefinition compile(final FunctionCompilationContext myContext, final InstantProvider atInstantProvider) {
    final ConfigSource configSource = OpenGammaCompilationContext.getConfigSource(myContext);
    final ConfigDBVolatilitySurfaceDefinitionSource definitionSource = new ConfigDBVolatilitySurfaceDefinitionSource(configSource);
    final ConfigDBVolatilitySurfaceSpecificationSource specificationSource = new ConfigDBVolatilitySurfaceSpecificationSource(configSource);
    final ZonedDateTime atInstant = ZonedDateTime.ofInstant(atInstantProvider, TimeZone.UTC);
    return new FXVolatilitySurfaceCompiledFunction(atInstant.withTime(0, 0), atInstant.plusDays(1).withTime(0, 0).minusNanos(1000000), atInstant, definitionSource, specificationSource);
  }

  protected class FXVolatilitySurfaceCompiledFunction extends CompiledFunction {

    public FXVolatilitySurfaceCompiledFunction(final ZonedDateTime from, final ZonedDateTime to, final ZonedDateTime now,
        final ConfigDBVolatilitySurfaceDefinitionSource definitionSource, final ConfigDBVolatilitySurfaceSpecificationSource specificationSource) {
      super(from, to, now, definitionSource, specificationSource);
    }

    //    @SuppressWarnings({"unchecked", "synthetic-access" })
    //    @Override
    //    public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target,
    //        final Set<ValueRequirement> desiredValues) {
    //      final ValueRequirement desiredValue = desiredValues.iterator().next();
    //      final String surfaceName = desiredValue.getConstraint(ValuePropertyNames.SURFACE);
    //      final String instrumentType = desiredValue.getConstraint(InstrumentTypeProperties.PROPERTY_SURFACE_INSTRUMENT_TYPE);
    //      final String surfaceQuoteType = desiredValue.getConstraint(SurfacePropertyNames.PROPERTY_SURFACE_QUOTE_TYPE);
    //      final String surfaceUnits = desiredValue.getConstraint(SurfacePropertyNames.PROPERTY_SURFACE_UNITS);
    //      final ValueRequirement specificationRequirement = new ValueRequirement(ValueRequirementNames.VOLATILITY_SURFACE_SPEC, target.toSpecification(),
    //          ValueProperties.builder()
    //          .with(ValuePropertyNames.SURFACE, surfaceName)
    //          .with(InstrumentTypeProperties.PROPERTY_SURFACE_INSTRUMENT_TYPE, instrumentType)
    //          .with(SurfacePropertyNames.PROPERTY_SURFACE_QUOTE_TYPE, surfaceQuoteType)
    //          .with(SurfacePropertyNames.PROPERTY_SURFACE_UNITS, surfaceUnits).get());
    //      final VolatilitySurfaceDefinition<Object, Object> definition = getSurfaceDefinition(target, surfaceName, instrumentType);
    //      final Object specificationObject = inputs.getValue(specificationRequirement);
    //      if (specificationObject == null) {
    //        throw new OpenGammaRuntimeException("Specification with requirement " + specificationRequirement + " was null");
    //      }
    //      final VolatilitySurfaceSpecification specification = (VolatilitySurfaceSpecification) specificationObject;
    //      final LocalDate valuationDate = executionContext.getValuationClock().today();
    //      final SurfaceInstrumentProvider<Object, Object> provider = (SurfaceInstrumentProvider<Object, Object>) specification.getSurfaceInstrumentProvider();
    //      final Map<Pair<Object, Object>, Double> volatilityValues = new HashMap<Pair<Object, Object>, Double>();
    //      final ObjectArrayList<Object> xList = new ObjectArrayList<Object>();
    //      final ObjectArrayList<Object> yList = new ObjectArrayList<Object>();
    //      for (final Object x : definition.getXs()) {
    //        for (final Object y : definition.getYs()) {
    //          final ExternalId identifier = provider.getInstrument(x, y, valuationDate);
    //          final ValueRequirement requirement = new ValueRequirement(provider.getDataFieldName(), identifier);
    //          final Double volatility = (Double) inputs.getValue(requirement);
    //          if (volatility != null) {
    //            xList.add(x);
    //            yList.add(y);
    //            volatilityValues.put(Pair.of(x, y), volatility);
    //          } else {
    //            s_logger.debug("Missing option price~" + identifier.toString());
    //          }
    //        }
    //      }
    //      final VolatilitySurfaceData<Object, Object> volSurfaceData = new VolatilitySurfaceData<Object, Object>(definition.getName(), specification.getName(),
    //          definition.getTarget(), definition.getXs(), definition.getYs(), volatilityValues);
    //      final ValueSpecification result = new ValueSpecification(ValueRequirementNames.VOLATILITY_SURFACE_DATA, new ComputationTargetSpecification(definition.getTarget()),
    //          createValueProperties()
    //          .with(ValuePropertyNames.SURFACE, surfaceName)
    //          .with(InstrumentTypeProperties.PROPERTY_SURFACE_INSTRUMENT_TYPE, instrumentType)
    //          .with(SurfacePropertyNames.PROPERTY_SURFACE_QUOTE_TYPE, specification.getSurfaceQuoteType())
    //          .with(SurfacePropertyNames.PROPERTY_SURFACE_UNITS, specification.getQuoteUnits()).get());
    //      return Collections.singleton(new ComputedValue(result, volSurfaceData));
    //    }

    @Override
    @SuppressWarnings({"unchecked" })
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
          throw new OpenGammaRuntimeException("Could not get volatility surface definition named " + fullDefinitionName + " for instrument type " + instrumentType);
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
          throw new OpenGammaRuntimeException("Could not get volatility surface specification named " + fullSpecificationName);
        }
      }
      return specification;
    }
  }
}