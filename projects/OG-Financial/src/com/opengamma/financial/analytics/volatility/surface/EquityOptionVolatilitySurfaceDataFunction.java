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
import javax.time.calendar.Clock;
import javax.time.calendar.LocalDate;
import javax.time.calendar.TimeZone;
import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.core.marketdatasnapshot.VolatilitySurfaceData;
import com.opengamma.core.security.SecurityUtils;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.CompiledFunctionDefinition;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.OpenGammaCompilationContext;
import com.opengamma.financial.analytics.model.equity.variance.EquityVarianceSwapFunction;
import com.opengamma.financial.equity.variance.pricing.VarianceSwapStaticReplication;
import com.opengamma.id.ExternalId;
import com.opengamma.livedata.normalization.MarketDataRequirementNames;
import com.opengamma.util.tuple.Pair;

/**
 * 
 */
//TODO this class needs to be re-written, as each instrument type needs a different set of inputs
public class EquityOptionVolatilitySurfaceDataFunction extends AbstractFunction {
  private static final Logger s_logger = LoggerFactory.getLogger(EquityOptionVolatilitySurfaceDataFunction.class);
  private VolatilitySurfaceDefinition<?, ?> _definition;
  private ValueSpecification _result;
  private Set<ValueSpecification> _results;
  private String _underlyingIdentifierAsString;
  private final String _definitionName;
  private final String _specificationName;
  private String _instrumentType;
  private VolatilitySurfaceSpecification _specification;

  public EquityOptionVolatilitySurfaceDataFunction(final String definitionName, final String instrumentType, final String specificationName) {
    Validate.notNull(definitionName, "Definition Name");
    Validate.notNull(instrumentType, "Instrument Type");
    Validate.notNull(specificationName, "Specification Name");
    _definition = null;
    _definitionName = definitionName;
    _instrumentType = instrumentType;
    _specificationName = specificationName;
    _result = null;
    _results = null;
  }

  public String getCurrencyLabel() {
    return _underlyingIdentifierAsString;
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
    _definition = volSurfaceDefinitionSource.getDefinition(_definitionName, _instrumentType);
    if (_definition == null) {
      throw new OpenGammaRuntimeException("Couldn't find Equity Option Volatility Surface Definition " + _definitionName);
    }
    final ConfigDBVolatilitySurfaceSpecificationSource volatilitySurfaceSpecificationSource = new ConfigDBVolatilitySurfaceSpecificationSource(configSource);
    _specification = volatilitySurfaceSpecificationSource.getSpecification(_specificationName, _instrumentType);
    if (_specification == null) {
      throw new OpenGammaRuntimeException("Couldn't find Equity Option Volatility Surface Specification " + _specificationName);
    }
    _result = new ValueSpecification(ValueRequirementNames.STANDARD_VOLATILITY_SURFACE_DATA, new ComputationTargetSpecification(_definition.getTarget().getUniqueId()),
        createValueProperties().with(ValuePropertyNames.SURFACE, _definitionName)
                               .with(RawVolatilitySurfaceDataFunction.PROPERTY_SURFACE_INSTRUMENT_TYPE, _instrumentType)
                               .with(EquityVarianceSwapFunction.STRIKE_PARAMETERIZATION_METHOD, VarianceSwapStaticReplication.StrikeParameterization.STRIKE.toString()).get());
    _results = Collections.singleton(_result);
  }

  @Override
  public String getShortName() {
    return _underlyingIdentifierAsString + "-" + _definitionName + " for " + _instrumentType + " from " + _specificationName + " Volatility Surface Data";
  }

  public static <X, Y> Set<ValueRequirement> buildRequirements(final VolatilitySurfaceSpecification specification,
                                                        final VolatilitySurfaceDefinition<X, Y> definition,                                                        
                                                        final ZonedDateTime atInstant) {
    final Set<ValueRequirement> result = new HashSet<ValueRequirement>();
    
    final BloombergEquityOptionVolatilitySurfaceInstrumentProvider provider = (BloombergEquityOptionVolatilitySurfaceInstrumentProvider) specification.getSurfaceInstrumentProvider();
    for (final X x : definition.getXs()) {
      // don't care what these are
      for (final Y y : definition.getYs()) {
        provider.init(true); // generate puts
        final ExternalId putIdentifier = provider.getInstrument((LocalDate) x, (Double) y, atInstant.toLocalDate());
        result.add(new ValueRequirement(provider.getDataFieldName(), putIdentifier));
        provider.init(false);
        final ExternalId callIdentifier = provider.getInstrument((LocalDate) x, (Double) y, atInstant.toLocalDate());
        result.add(new ValueRequirement(provider.getDataFieldName(), callIdentifier));
      }
    }
    // add the underlying    
    ExternalId temp = ExternalId.of(SecurityUtils.BLOOMBERG_TICKER, definition.getTarget().getUniqueId().getValue());
    result.add(new ValueRequirement(MarketDataRequirementNames.MARKET_VALUE, temp));
    return result;
  }

  @Override
  public CompiledFunctionDefinition compile(final FunctionCompilationContext context, final InstantProvider atInstantProvider) {
    final ZonedDateTime atInstant = ZonedDateTime.ofInstant(atInstantProvider, TimeZone.UTC);
    final Set<ValueRequirement> requirements = Collections.unmodifiableSet(buildRequirements(_specification, _definition, atInstant));
    //TODO ENG-252 see MarketInstrumentImpliedYieldCurveFunction; need to work out the expiry more efficiently
    return new AbstractInvokingCompiledFunction(atInstant.withTime(0, 0), atInstant.plusDays(1).withTime(0, 0).minusNanos(1000000)) {

      @Override
      public ComputationTargetType getTargetType() {
        return ComputationTargetType.PRIMITIVE;
      }

      @SuppressWarnings("synthetic-access")
      @Override
      public Set<ValueSpecification> getResults(final FunctionCompilationContext myContext, final ComputationTarget target) {
        if (canApplyTo(myContext, target)) {
          return _results;
        }
        return null;
      }

      @Override
      public Set<ValueRequirement> getRequirements(final FunctionCompilationContext myContext, final ComputationTarget target, final ValueRequirement desiredValue) {
        if (canApplyTo(myContext, target)) {
          return requirements;
        }
        return null;
      }

      @SuppressWarnings("synthetic-access")
      @Override
      public boolean canApplyTo(final FunctionCompilationContext myContext, final ComputationTarget target) {
        if (target.getType() != ComputationTargetType.PRIMITIVE) {
          return false;
        }
        return ObjectUtils.equals(target.getUniqueId(), _definition.getTarget().getUniqueId());
      }

      @SuppressWarnings("synthetic-access")
      @Override
      public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target,
          final Set<ValueRequirement> desiredValues) {
        final Clock snapshotClock = executionContext.getValuationClock();
        ExternalId temp = ExternalId.of(SecurityUtils.BLOOMBERG_TICKER, _definition.getTarget().getUniqueId().getValue());
        final ValueRequirement underlyingSpotValueRequirement = new ValueRequirement(MarketDataRequirementNames.MARKET_VALUE, temp);        
        final Double underlyingSpot = (Double) inputs.getValue(underlyingSpotValueRequirement);
        if (underlyingSpot == null) {
          s_logger.error("Could not get underlying spot value for " + _definition.getTarget().getUniqueId());
          return Collections.emptySet();
        }
        final ZonedDateTime now = snapshotClock.zonedDateTime();
        final Map<Pair<Object, Object>, Double> volatilityValues = new HashMap<Pair<Object, Object>, Double>();
        //int numFound = 0;
        for (final Object x : _definition.getXs()) {
          for (final Object y : _definition.getYs()) {
            double strike = (Double) y;
            LocalDate expiry = (LocalDate) x;
            final BloombergEquityOptionVolatilitySurfaceInstrumentProvider provider = (BloombergEquityOptionVolatilitySurfaceInstrumentProvider) _specification.getSurfaceInstrumentProvider();
            if (strike < underlyingSpot) { 
              provider.init(false); // generate identifiers for call options
            } else {
              provider.init(true); // generate identifiers for put options
            }
            final ExternalId identifier = provider.getInstrument(expiry, strike, now.toLocalDate());
            final ValueRequirement requirement = new ValueRequirement(provider.getDataFieldName(), identifier);
            if (inputs.getValue(requirement) != null) {
              final Double volatility = (Double) inputs.getValue(requirement);
              volatilityValues.put(Pair.of((Object) expiry, (Object) strike), volatility / 100);
            }
          }
        }
        final VolatilitySurfaceData<?, ?> volSurfaceData = new VolatilitySurfaceData<Object, Object>(_definition.getName(), _specification.getName(),
                                                                                                     _definition.getTarget().getUniqueId(),
                                                                                                     _definition.getXs(), _definition.getYs(), volatilityValues);
        final ComputedValue resultValue = new ComputedValue(_result, volSurfaceData);
        return Collections.singleton(resultValue);
      }
      
      @Override
      public boolean canHandleMissingInputs() {
        return true;
      }
    };
  }
}
