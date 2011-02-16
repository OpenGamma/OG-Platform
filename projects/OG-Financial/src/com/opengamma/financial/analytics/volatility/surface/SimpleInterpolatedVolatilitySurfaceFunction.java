/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.volatility.surface;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.time.InstantProvider;
import javax.time.calendar.TimeZone;
import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.core.common.Currency;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.CompiledFunctionDefinition;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.OpenGammaCompilationContext;
import com.opengamma.id.Identifier;
import com.opengamma.livedata.normalization.MarketDataRequirementNames;
import com.opengamma.util.tuple.Pair;

/**
 * 
 */
public class SimpleInterpolatedVolatilitySurfaceFunction extends AbstractFunction {

  /**
   * Resultant value specification property for the curve result. Note these should be moved into either the ValuePropertyNames class
   * if there are generic terms, or an OpenGammaValuePropertyNames if they are more specific to our financial integration.
   */
  public static final String PROPERTY_SURFACE_DEFINITION_NAME = "NAME";

  @SuppressWarnings("unused")
  private String _interpolator;
  private VolatilitySurfaceDefinition<?, ?> _definition;
  private ValueSpecification _result;
  private Set<ValueSpecification> _results;
  private final Currency _surfaceCurrency;
  private final String _definitionName;
  private final String _specificationName;

  private VolatilitySurfaceSpecification _specification;

  public SimpleInterpolatedVolatilitySurfaceFunction(final String currency, final String definitionName, final String specificationName) {
    this(Currency.getInstance(currency), definitionName, specificationName);
  }

  public SimpleInterpolatedVolatilitySurfaceFunction(final Currency currency, final String definitionName, final String specificationName) {
    Validate.notNull(currency, "Currency");
    Validate.notNull(definitionName, "Definition Name");
    Validate.notNull(specificationName, "Specification Name");
    _definition = null;
    _surfaceCurrency = currency;
    _definitionName = definitionName;
    _specificationName = specificationName;
    _interpolator = null;
    _result = null;
    _results = null;
  }

  public Currency getCurveCurrency() {
    return _surfaceCurrency;
  }

  public String getDefinitionName() {
    return _definitionName;
  }
  
  public String getSpecificationName() {
    return _specificationName;
  }

  @Override
  public void init(final FunctionCompilationContext context) {
    final ConfigSource configSource = OpenGammaCompilationContext.getConfigSource(context);
    final ConfigDBVolatilitySurfaceDefinitionSource volSurfaceDefinitionSource = new ConfigDBVolatilitySurfaceDefinitionSource(configSource);
    _definition = volSurfaceDefinitionSource.getDefinition(_surfaceCurrency, _definitionName);
    final ConfigDBVolatilitySurfaceSpecificationSource volatilitySurfaceSpecificationSource = new ConfigDBVolatilitySurfaceSpecificationSource(configSource);
    _specification = volatilitySurfaceSpecificationSource.getSpecification(_surfaceCurrency, _specificationName);
    _interpolator = _definition.getInterpolatorName();
    _result = new ValueSpecification(ValueRequirementNames.VOLATILITY_SURFACE_DATA, new ComputationTargetSpecification(_definition.getCurrency()),
        createValueProperties().with(PROPERTY_SURFACE_DEFINITION_NAME, _definitionName).get());
    _results = Collections.singleton(_result);
  }

  @Override
  public String getShortName() {
    return _surfaceCurrency + "-" + _definitionName + " from " + _specificationName + " Volatility Surface Data";
  }

  @SuppressWarnings("unchecked")
  public static <X, Y> Set<ValueRequirement> buildRequirements(final VolatilitySurfaceSpecification specification,
                                                        final VolatilitySurfaceDefinition<X, Y> definition,
                                                        final FunctionCompilationContext context) {
    final Set<ValueRequirement> result = new HashSet<ValueRequirement>();
    for (X x : definition.getXs()) {
      // don't care what these are
      for (Y y : definition.getYs()) {
        Identifier identifier = ((SurfaceInstrumentProvider<X, Y>) specification.getSurfaceInstrumentProvider()).getInstrument(x, y);
        result.add(new ValueRequirement(MarketDataRequirementNames.MARKET_VALUE, identifier));
      }
    }
    return result;
  }

  @Override
  public CompiledFunctionDefinition compile(final FunctionCompilationContext context, final InstantProvider atInstantProvider) {
    final ZonedDateTime atInstant = ZonedDateTime.ofInstant(atInstantProvider, TimeZone.UTC);
    final Set<ValueRequirement> requirements = Collections.unmodifiableSet(buildRequirements(_specification, _definition, context));
    // ENG-252 see MarkingInstrumentImpliedYieldCurveFunction; need to work out the expiry more efficiently
    return new AbstractInvokingCompiledFunction(atInstant.withTime(0, 0), atInstant.plusDays(1).withTime(0, 0).minusNanos(1000000)) {

      @Override
      public ComputationTargetType getTargetType() {
        return ComputationTargetType.PRIMITIVE;
      }

      @Override
      public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
        if (canApplyTo(context, target)) {
          return _results;
        }
        return null;
      }

      @Override
      public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
        if (canApplyTo(context, target)) {
          return requirements;
        }
        return null;
      }

      @Override
      public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
        if (target.getType() != ComputationTargetType.PRIMITIVE) {
          return false;
        }
        // REVIEW: jim 23-July-2010 is this enough? Probably not, but I'm not entirely sure what the deal with the Ids is...
        return ObjectUtils.equals(target.getUniqueId(), _definition.getCurrency().getUniqueId());
      }

      @SuppressWarnings("unchecked")
      @Override
      public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target,
          final Set<ValueRequirement> desiredValues) {
        final Map<Pair<Object, Object>, Double> volatilityValues = new HashMap<Pair<Object, Object>, Double>();
        for (final Object x : _definition.getXs()) {
          for (final Object y : _definition.getYs()) {
            Identifier identifier = ((SurfaceInstrumentProvider<Object, Object>) _specification.getSurfaceInstrumentProvider()).getInstrument(x, y);
            ValueRequirement requirement = new ValueRequirement(MarketDataRequirementNames.MARKET_VALUE, identifier);
            Double volatility = (Double) inputs.getValue(requirement);
            volatilityValues.put(Pair.of(x, y), volatility);
          }
        }
        final VolatilitySurfaceData<?, ?> volSurfaceData = new VolatilitySurfaceData<Object, Object>(_definition.getName(), _specification.getName(), 
                                                                                                     _definition.getCurrency(), _definition.getInterpolatorName(), 
                                                                                                     _definition.getXs(), _definition.getYs(), volatilityValues);
        final ComputedValue resultValue = new ComputedValue(_result, volSurfaceData);
        return Collections.singleton(resultValue);
      }

    };
  }
}
