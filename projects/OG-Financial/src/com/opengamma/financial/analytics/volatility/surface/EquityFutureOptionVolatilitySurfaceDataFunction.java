/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.volatility.surface;

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.time.calendar.LocalDate;
import javax.time.calendar.ZonedDateTime;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.core.id.ExternalSchemes;
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
import com.opengamma.financial.OpenGammaCompilationContext;
import com.opengamma.financial.OpenGammaExecutionContext;
import com.opengamma.financial.analytics.model.FutureOptionExpiries;
import com.opengamma.financial.analytics.model.InstrumentTypeProperties;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.Pair;


/**
 * 
 */
public class EquityFutureOptionVolatilitySurfaceDataFunction extends AbstractFunction.NonCompiledInvoker {

  @Override
  /**
   * {@inheritDoc} <p>
   * INPUT: We are taking a VolatilitySurfaceData object, which contains all number of missing data, plus strikes and vols are in percentages <p>
   * OUTPUT: and converting this into a StandardVolatilitySurfaceData object, which has no empty values, expiry is in years, and the strike and vol scale is without unit (35% -> 0.35)
   */
  public Set<ComputedValue> execute(FunctionExecutionContext executionContext, FunctionInputs inputs, ComputationTarget target, Set<ValueRequirement> desiredValues) {
    
    final ZonedDateTime valTime = executionContext.getValuationClock().zonedDateTime();
    final LocalDate valDate = valTime.toLocalDate();
    
    // 1. Build the surface name, in two parts: the given name and the target
    final ValueRequirement desiredValue = desiredValues.iterator().next();
    final String surfaceName = desiredValue.getConstraint(ValuePropertyNames.SURFACE);
    final String fullName = surfaceName + "_" + target.getUniqueId().getValue();  // e.g. FOO_DJX 
    
    // 2. We (MAY) need the definition and the specification, to loop over axes, and to get inputs respectively.
    final ConfigSource configSrc =  OpenGammaExecutionContext.getConfigSource(executionContext);
    final ConfigDBVolatilitySurfaceSpecificationSource specSrc = new ConfigDBVolatilitySurfaceSpecificationSource(configSrc);
    final VolatilitySurfaceSpecification specification = specSrc.getSpecification(fullName, InstrumentTypeProperties.EQUITY_OPTION);

    // 3. Get the RawEquityVolatilitySurfaceData object
    final ValueProperties surfaceProperties = ValueProperties.builder() // createValueProperties() 
      .with(ValuePropertyNames.SURFACE, surfaceName)
      .with(InstrumentTypeProperties.PROPERTY_SURFACE_INSTRUMENT_TYPE, InstrumentTypeProperties.EQUITY_OPTION)
      .with(SurfacePropertyNames.PROPERTY_SURFACE_QUOTE_TYPE, specification.getSurfaceQuoteType())
      .with(SurfacePropertyNames.PROPERTY_SURFACE_UNITS, specification.getQuoteUnits())      
      .get();
    final Object rawSurfaceObject = inputs.getValue(new ValueRequirement(ValueRequirementNames.VOLATILITY_SURFACE_DATA, target.toSpecification(), surfaceProperties));
    if (rawSurfaceObject == null) {
      throw new OpenGammaRuntimeException("Could not get volatility surface");
    }
    @SuppressWarnings("unchecked")
    VolatilitySurfaceData<Number, Double> rawSurface = (VolatilitySurfaceData<Number, Double>) rawSurfaceObject;
    
    // 4. Remove empties, convert expiries from number to years, and scale vols 
    final Map<Pair<Double, Double>, Double> volValues = new HashMap<Pair<Double, Double>, Double>();
    final DoubleArrayList tList = new DoubleArrayList();
    final DoubleArrayList kList = new DoubleArrayList();
    for (Number nthExpiry : rawSurface.getXs()) {
      Double t = FutureOptionExpiries.EQUITY.getFutureOptionTtm(nthExpiry.intValue(), valDate);
      for (Double strike : rawSurface.getYs()) {
        Double vol = rawSurface.getVolatility(nthExpiry, strike);
        if (vol != null) {
          tList.add(t);
          kList.add(strike / 100.);
          volValues.put(Pair.of(t, strike / 100.), vol / 100.);
        }
      }
    }
    final VolatilitySurfaceData<Double, Double> stdVolSurface = new VolatilitySurfaceData<Double, Double>(rawSurface.getDefinitionName(), rawSurface.getSpecificationName(), rawSurface.getTarget(),
        tList.toArray(new Double[0]), kList.toArray(new Double[0]), volValues);
        
    // 5. Return
    final ValueProperties stdVolProperties = createValueProperties()
      .with(ValuePropertyNames.SURFACE, surfaceName)
      .with(InstrumentTypeProperties.PROPERTY_SURFACE_INSTRUMENT_TYPE, InstrumentTypeProperties.EQUITY_OPTION)   
      .get();
    final ValueSpecification stdVolSpec = new ValueSpecification(ValueRequirementNames.STANDARD_VOLATILITY_SURFACE_DATA, 
        new ComputationTargetSpecification(specification.getTarget()), stdVolProperties);
    return Collections.singleton(new ComputedValue(stdVolSpec, stdVolSurface));
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.PRIMITIVE;
  }

  @Override
  // TODO Review choice of target as BLOOMBERG_TICKER_WEAK~DJX. Seems arbitrary. Both are contained in the specification itself.
  public boolean canApplyTo(FunctionCompilationContext context, ComputationTarget target) {
    if (target.getType() != getTargetType()) {
      return false;  
    }
    
    String targetScheme = target.getUniqueId().getScheme();
    return (targetScheme.equalsIgnoreCase(ExternalSchemes.BLOOMBERG_TICKER.getName()) ||
            targetScheme.equalsIgnoreCase(ExternalSchemes.BLOOMBERG_TICKER_WEAK.getName()) ||
            targetScheme.equalsIgnoreCase(Currency.OBJECT_SCHEME)
            );
  }

  @Override
  public Set<ValueSpecification> getResults(FunctionCompilationContext context, ComputationTarget target) {
    ValueSpecification spec = new ValueSpecification(ValueRequirementNames.STANDARD_VOLATILITY_SURFACE_DATA, target.toSpecification(),
        createValueProperties().withAny(ValuePropertyNames.SURFACE)
        .with(InstrumentTypeProperties.PROPERTY_SURFACE_INSTRUMENT_TYPE, InstrumentTypeProperties.EQUITY_OPTION)
        .get());
    return Collections.singleton(spec);
  }

  @Override
  public Set<ValueRequirement> getRequirements(FunctionCompilationContext context, ComputationTarget target, ValueRequirement desiredValue) {
    // Function requires a VolatilitySurfaceData, typically supplied by RawOptionVolatilitySurfaceDataFunction
    // 1. Build the surface name, in two parts: the given name and the target
    final Set<String> surfaceNames = desiredValue.getConstraints().getValues(ValuePropertyNames.SURFACE);
    if (surfaceNames == null || surfaceNames.size() != 1) {
      throw new OpenGammaRuntimeException("Function takes only get a single surface. One has asked for " + surfaceNames);
    }
    final String givenName = surfaceNames.iterator().next();
    final String fullName = givenName + "_" + target.getUniqueId().getValue();
    
    // 2. Look up the specification
    final ConfigSource configSource = OpenGammaCompilationContext.getConfigSource(context);
    final ConfigDBVolatilitySurfaceSpecificationSource volSpecSource = new ConfigDBVolatilitySurfaceSpecificationSource(configSource);
    final VolatilitySurfaceSpecification specification = volSpecSource.getSpecification(fullName, InstrumentTypeProperties.EQUITY_OPTION);
    // 3. Build the ValueRequirements' constraints
    final ValueProperties constraints = ValueProperties.builder()
      .with(ValuePropertyNames.SURFACE, givenName)
      .with(InstrumentTypeProperties.PROPERTY_SURFACE_INSTRUMENT_TYPE, InstrumentTypeProperties.EQUITY_OPTION)
      .with(SurfacePropertyNames.PROPERTY_SURFACE_QUOTE_TYPE, specification.getSurfaceQuoteType())
      .with(SurfacePropertyNames.PROPERTY_SURFACE_UNITS, specification.getQuoteUnits())      
      .get();
    // 4. Return  
    ValueRequirement surfaceReq = new ValueRequirement(ValueRequirementNames.VOLATILITY_SURFACE_DATA, target.toSpecification(), constraints);
    return Collections.singleton(surfaceReq);
  }

}
