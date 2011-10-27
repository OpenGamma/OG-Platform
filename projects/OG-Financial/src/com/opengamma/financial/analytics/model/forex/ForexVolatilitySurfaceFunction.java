/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.forex;

import static com.opengamma.financial.analytics.volatility.surface.RawVolatilitySurfaceDataFunction.PROPERTY_SURFACE_INSTRUMENT_TYPE;

import java.util.Collections;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.time.calendar.Period;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

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
    final Object[] objectTenors = fxVolatilitySurface.getXs();
    final Tenor[] tenors = convertTenors(objectTenors); 
    final Pair<Number, FXVolQuoteType>[] quotes = sortQuotes(fxVolatilitySurface.getYs());
    final int nPoints = tenors.length;
    final SmileDeltaParameter[] smile = new SmileDeltaParameter[nPoints];
    final int nSmiles = (quotes.length - 1) / 2;
    for (int i = 0; i < tenors.length; i++) {
      final Tenor tenor = tenors[i];
      final double t = getTime(tenor);
      final Double atm = fxVolatilitySurface.getVolatility(tenor, quotes[0]);
      if (atm == null) {
        throw new OpenGammaRuntimeException("Could not get ATM volatility data for surface");
      }
      final double[] deltas = new double[nSmiles];
      final double[] riskReversals = new double[nSmiles];
      final double[] butterflies = new double[nSmiles];
      for (int j = 1, k = 0; j < quotes.length; j += 2, k++) {
        deltas[k] = quotes[j].getFirst().doubleValue() / 100;
        riskReversals[k] = fxVolatilitySurface.getVolatility(tenors[i], quotes[j]);
        butterflies[k] = fxVolatilitySurface.getVolatility(tenors[i], quotes[j + 1]);
      }
      smile[i] = new SmileDeltaParameter(t, atm, deltas, riskReversals, butterflies);
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

  private Tenor[] convertTenors(final Object[] objectTenors) {
    final int n = objectTenors.length;
    final Tenor[] result = new Tenor[n];
    for (int i = 0; i < n; i++) {
      result[i] = (Tenor) objectTenors[i];
    }
    return result;
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

  @SuppressWarnings("unchecked")
  private Pair<Number, FXVolQuoteType>[] sortQuotes(final Object[] quotes) {
    final int n = quotes.length;
    @SuppressWarnings("rawtypes")
    final Pair[] sorted = new Pair[n];
    final SortedSet<Number> deltas = new TreeSet<Number>();
    for (final Object quote : quotes) {
      final Pair<Number, FXVolQuoteType> pair = (Pair<Number, FXVolQuoteType>) quote;
      deltas.add(pair.getFirst());
    }
    int i = 0;
    for (final Number delta : deltas) {
      if (delta.intValue() != 0) {
        sorted[i++] = ObjectsPair.of(delta, FXVolQuoteType.RISK_REVERSAL);
        sorted[i++] = ObjectsPair.of(delta, FXVolQuoteType.BUTTERFLY);
      } else {
        sorted[i++] = ObjectsPair.of(delta, FXVolQuoteType.ATM);
      }
    }
    return sorted;
  }
}
