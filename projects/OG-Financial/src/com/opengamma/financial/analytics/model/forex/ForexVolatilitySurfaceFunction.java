/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.forex;

import static com.opengamma.financial.analytics.volatility.surface.RawVolatilitySurfaceDataFunction.PROPERTY_SURFACE_INSTRUMENT_TYPE;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

import javax.time.calendar.Period;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.config.ConfigSource;
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
import com.opengamma.financial.analytics.volatility.surface.BloombergFXOptionVolatilitySurfaceInstrumentProvider.FXVolQuoteType;
import com.opengamma.financial.analytics.volatility.surface.ConfigDBVolatilitySurfaceDefinitionSource;
import com.opengamma.financial.analytics.volatility.surface.ConfigDBVolatilitySurfaceSpecificationSource;
import com.opengamma.financial.analytics.volatility.surface.VolatilitySurfaceDefinition;
import com.opengamma.financial.analytics.volatility.surface.VolatilitySurfaceSpecification;
import com.opengamma.financial.model.option.definition.SmileDeltaParameter;
import com.opengamma.financial.model.option.definition.SmileDeltaTermStructureParameter;
import com.opengamma.util.time.Tenor;
import com.opengamma.util.tuple.ObjectsPair;
import com.opengamma.util.tuple.Pair;

/**
 * 
 */
public class ForexVolatilitySurfaceFunction extends AbstractFunction.NonCompiledInvoker {
  private static final Logger s_logger = LoggerFactory.getLogger(ForexVolatilitySurfaceFunction.class);
  private static final String INSTRUMENT_TYPE = "FX_VANILLA_OPTION";
  private ValueSpecification _result;
  private final String _definitionName;
  private final String _specificationName;
  private VolatilitySurfaceSpecification _specification;
  private VolatilitySurfaceDefinition<?, ?> _definition;
  private ValueRequirement _requirement;

  public ForexVolatilitySurfaceFunction(final String definitionName, final String specificationName) {
    Validate.notNull(definitionName, "definition name");
    Validate.notNull(specificationName, "specification name");
    _definitionName = definitionName;
    _specificationName = specificationName;
  }

  @Override
  public void init(final FunctionCompilationContext context) {
    final ConfigSource configSource = OpenGammaCompilationContext.getConfigSource(context);
    final ConfigDBVolatilitySurfaceDefinitionSource volSurfaceDefinitionSource = new ConfigDBVolatilitySurfaceDefinitionSource(configSource);
    _definition = volSurfaceDefinitionSource.getDefinition(_definitionName, INSTRUMENT_TYPE);
    if (_definition == null) {
      throw new OpenGammaRuntimeException("Couldn't find Volatility Surface Definition for " + INSTRUMENT_TYPE + " called " + _definitionName);
    }
    final ConfigDBVolatilitySurfaceSpecificationSource volatilitySurfaceSpecificationSource = new ConfigDBVolatilitySurfaceSpecificationSource(configSource);
    _specification = volatilitySurfaceSpecificationSource.getSpecification(_specificationName, INSTRUMENT_TYPE);
    if (_specification == null) {
      throw new OpenGammaRuntimeException("Couldn't find Volatility Surface Specification for " + INSTRUMENT_TYPE + " called " + _specificationName);
    }
    _requirement = new ValueRequirement(ValueRequirementNames.VOLATILITY_SURFACE_DATA, _definition.getTarget(),
        ValueProperties.with(ValuePropertyNames.SURFACE, _definitionName)
                       .with(PROPERTY_SURFACE_INSTRUMENT_TYPE, INSTRUMENT_TYPE).get());
    _result = new ValueSpecification(ValueRequirementNames.STANDARD_VOLATILITY_SURFACE_DATA, new ComputationTargetSpecification(_definition.getTarget()),
        createValueProperties().with(ValuePropertyNames.SURFACE, _definitionName)
                               .with(PROPERTY_SURFACE_INSTRUMENT_TYPE, INSTRUMENT_TYPE).get());
  }
  
  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final Object volatilitySurfaceObject = inputs.getValue(_requirement);
    if (volatilitySurfaceObject == null) {
      throw new OpenGammaRuntimeException("Could not get " + _requirement);
    }
    @SuppressWarnings("unchecked")
    final VolatilitySurfaceData<Tenor, Pair<Number, FXVolQuoteType>> fxVolatilitySurface = (VolatilitySurfaceData<Tenor, Pair<Number, FXVolQuoteType>>) volatilitySurfaceObject;
    final Tenor[] tenors = fxVolatilitySurface.getXs();
    Arrays.sort(tenors);
    final Pair<Number, FXVolQuoteType>[] quotes = fxVolatilitySurface.getYs(); 
    final Number[] deltaValues = getDeltaValues(quotes);
    final int nPoints = tenors.length;
    final SmileDeltaParameter[] smile = new SmileDeltaParameter[nPoints];
    final int nSmileValues = deltaValues.length - 1;
    for (int i = 0; i < tenors.length; i++) {
      final Tenor tenor = tenors[i];
      final double t = getTime(tenor);
      final Double atm = fxVolatilitySurface.getVolatility(tenor, ObjectsPair.of(deltaValues[0], FXVolQuoteType.ATM));
      if (atm == null) {
        throw new OpenGammaRuntimeException("Could not get ATM volatility data for surface");
      }
      final DoubleArrayList deltas = new DoubleArrayList();
      final DoubleArrayList riskReversals = new DoubleArrayList();
      final DoubleArrayList butterflies = new DoubleArrayList();
      for (int j = 0; j < nSmileValues; j++) {        
        Number delta = deltaValues[j + 1];
        if (delta != null) {
          Double rr = fxVolatilitySurface.getVolatility(tenor, ObjectsPair.of(delta, FXVolQuoteType.RISK_REVERSAL));
          Double butterfly = fxVolatilitySurface.getVolatility(tenor, ObjectsPair.of(delta, FXVolQuoteType.BUTTERFLY));
          if (rr != null && butterfly != null) {
            deltas.add(delta.doubleValue() / 100.);        
            riskReversals.add(rr);
            butterflies.add(butterfly);            
          } 
        } else {
          s_logger.info("Had a null value for tenor number " + j);
        }
      }
      smile[i] = new SmileDeltaParameter(t, atm, deltas.toDoubleArray(), riskReversals.toDoubleArray(), butterflies.toDoubleArray());
    }
    final SmileDeltaTermStructureParameter smiles = new SmileDeltaTermStructureParameter(smile);
    return Collections.<ComputedValue>singleton(new ComputedValue(_result, smiles));
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.PRIMITIVE;
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    return target.getType() == ComputationTargetType.PRIMITIVE && ObjectUtils.equals(target.getUniqueId(), _definition.getTarget().getUniqueId());
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    return Collections.<ValueRequirement>singleton(_requirement);
  }

  @Override
  public Set<ValueSpecification> getResults(FunctionCompilationContext context, ComputationTarget target) {
    return Collections.<ValueSpecification>singleton(_result);
  }
  
  private double getTime(final Tenor tenor) {
    final Period period = tenor.getPeriod();
    if (period.getYears() != 0) {
      return period.getYears();
    }
    if (period.getMonths() != 0) {
      return ((double) period.getMonths()) / 12;
    }
    if (period.getDays() != 0) {
      return ((double) period.getDays()) / 365;
    }
    throw new OpenGammaRuntimeException("Should never happen");
  }

  private Number[] getDeltaValues(Pair<Number, FXVolQuoteType>[] quotes) {
    final TreeSet<Number> values = new TreeSet<Number>();
    for (Pair<Number, FXVolQuoteType> pair : quotes) {
      values.add(pair.getFirst());
    }
    return values.toArray((Number[]) Array.newInstance(Number.class, values.size()));
  }
}
