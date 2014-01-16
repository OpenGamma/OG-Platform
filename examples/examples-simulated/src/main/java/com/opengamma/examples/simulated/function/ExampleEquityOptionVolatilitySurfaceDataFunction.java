/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.examples.simulated.function;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.Clock;
import org.threeten.bp.Instant;
import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalTime;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.core.marketdatasnapshot.VolatilitySurfaceData;
import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
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
import com.opengamma.examples.simulated.volatility.surface.ExampleEquityOptionVolatilitySurfaceInstrumentProvider;
import com.opengamma.financial.analytics.model.InstrumentTypeProperties;
import com.opengamma.financial.analytics.model.equity.varianceswap.EquityVarianceSwapStaticReplicationFunction;
import com.opengamma.financial.analytics.volatility.surface.ConfigDBVolatilitySurfaceDefinitionSource;
import com.opengamma.financial.analytics.volatility.surface.ConfigDBVolatilitySurfaceSpecificationSource;
import com.opengamma.financial.analytics.volatility.surface.VolatilitySurfaceDefinition;
import com.opengamma.financial.analytics.volatility.surface.VolatilitySurfaceSpecification;
import com.opengamma.id.ExternalId;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 * 
 */
//TODO this class needs to be re-written, as each instrument type needs a different set of inputs
public class ExampleEquityOptionVolatilitySurfaceDataFunction extends AbstractFunction {
  private static final Logger s_logger = LoggerFactory.getLogger(ExampleEquityOptionVolatilitySurfaceDataFunction.class);
  private VolatilitySurfaceDefinition<?, ?> _definition;
  private ValueSpecification _result;
  private Set<ValueSpecification> _results;
  private String _underlyingIdentifierAsString;
  private final String _definitionName;
  private final String _specificationName;
  private final String _instrumentType;
  private VolatilitySurfaceSpecification _specification;

  public ExampleEquityOptionVolatilitySurfaceDataFunction(final String definitionName, final String instrumentType, final String specificationName) {
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
    _definition = ConfigDBVolatilitySurfaceDefinitionSource.init(context, this).getDefinition(_definitionName, _instrumentType);
    if (_definition == null) {
      throw new OpenGammaRuntimeException("Couldn't find Equity Option Volatility Surface Definition " + _definitionName);
    }
    _specification = ConfigDBVolatilitySurfaceSpecificationSource.init(context, this).getSpecification(_specificationName, _instrumentType);
    if (_specification == null) {
      throw new OpenGammaRuntimeException("Couldn't find Equity Option Volatility Surface Specification " + _specificationName);
    }
    _result = new ValueSpecification(ValueRequirementNames.STANDARD_VOLATILITY_SURFACE_DATA, ComputationTargetSpecification.of(_definition.getTarget().getUniqueId()),
        createValueProperties().with(ValuePropertyNames.SURFACE, _definitionName).with(InstrumentTypeProperties.PROPERTY_SURFACE_INSTRUMENT_TYPE, _instrumentType)
            .withAny(EquityVarianceSwapStaticReplicationFunction.STRIKE_PARAMETERIZATION_METHOD/*, VarianceSwapStaticReplication.StrikeParameterization.STRIKE.toString()*/).get());
    _results = Collections.singleton(_result);
  }

  @Override
  public String getShortName() {
    return "ExampleEquityOptionVolatilitySurfaceDataFunction " + _underlyingIdentifierAsString + "-" + _definitionName + " for " + _instrumentType + " from " + _specificationName +
        " Volatility Surface Data";
  }

  public static <X, Y> Set<ValueRequirement> buildRequirements(final VolatilitySurfaceSpecification specification, final VolatilitySurfaceDefinition<X, Y> definition,

  final ZonedDateTime atInstant) {
    final Set<ValueRequirement> result = new HashSet<ValueRequirement>();

    final ExampleEquityOptionVolatilitySurfaceInstrumentProvider provider = (ExampleEquityOptionVolatilitySurfaceInstrumentProvider) specification.getSurfaceInstrumentProvider();
    for (final X x : definition.getXs()) {
      // don't care what these are
      for (final Y y : definition.getYs()) {
        provider.init(true); // generate puts
        final ExternalId putIdentifier = provider.getInstrument((LocalDate) x, (Double) y, atInstant.toLocalDate());
        result.add(new ValueRequirement(provider.getDataFieldName(), ComputationTargetType.PRIMITIVE, putIdentifier));
        provider.init(false);
        final ExternalId callIdentifier = provider.getInstrument((LocalDate) x, (Double) y, atInstant.toLocalDate());
        result.add(new ValueRequirement(provider.getDataFieldName(), ComputationTargetType.PRIMITIVE, callIdentifier));
      }
    }
    // add the underlying
    final ExternalId temp = ExternalId.of(ExternalSchemes.OG_SYNTHETIC_TICKER, definition.getTarget().getUniqueId().getValue());
    result.add(new ValueRequirement(MarketDataRequirementNames.MARKET_VALUE, ComputationTargetType.PRIMITIVE, temp));
    return result;
  }

  @Override
  public CompiledFunctionDefinition compile(final FunctionCompilationContext context, final Instant atInstant) {
    final ZonedDateTime atZDT = ZonedDateTime.ofInstant(atInstant, ZoneOffset.UTC);
    final Set<ValueRequirement> requirements = Collections.unmodifiableSet(buildRequirements(_specification, _definition, atZDT));
    //TODO ENG-252 see MarketInstrumentImpliedYieldCurveFunction; need to work out the expiry more efficiently
    return new AbstractInvokingCompiledFunction(atZDT.with(LocalTime.MIDNIGHT), atZDT.plusDays(1).with(LocalTime.MIDNIGHT).minusNanos(1000000)) {

      @Override
      public ComputationTargetType getTargetType() {
        return ComputationTargetType.ANYTHING; // [PLAT-2286] Change to something more specific if possible, but the definition's target could be anything unique identifiable
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
        return ObjectUtils.equals(target.getUniqueId(), _definition.getTarget().getUniqueId());
      }

      @SuppressWarnings("synthetic-access")
      @Override
      public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target,
          final Set<ValueRequirement> desiredValues) {
        final Clock snapshotClock = executionContext.getValuationClock();
        final ExternalId temp = ExternalId.of(ExternalSchemes.OG_SYNTHETIC_TICKER, _definition.getTarget().getUniqueId().getValue());
        final ValueRequirement underlyingSpotValueRequirement = new ValueRequirement(MarketDataRequirementNames.MARKET_VALUE, ComputationTargetType.PRIMITIVE, temp);
        final Double underlyingSpot = (Double) inputs.getValue(underlyingSpotValueRequirement);
        if (underlyingSpot == null) {
          s_logger.error("Could not get underlying spot value for " + _definition.getTarget().getUniqueId());
          return Collections.emptySet();
        }
        final ZonedDateTime now = ZonedDateTime.now(snapshotClock);
        final Map<Pair<Object, Object>, Double> volatilityValues = new HashMap<Pair<Object, Object>, Double>();
        //int numFound = 0;
        for (final Object x : _definition.getXs()) {
          for (final Object y : _definition.getYs()) {
            final double strike = (Double) y;
            final LocalDate expiry = (LocalDate) x;
            final ExampleEquityOptionVolatilitySurfaceInstrumentProvider provider = (ExampleEquityOptionVolatilitySurfaceInstrumentProvider) _specification.getSurfaceInstrumentProvider();
            if (strike < underlyingSpot) {
              provider.init(false); // generate identifiers for call options
            } else {
              provider.init(true); // generate identifiers for put options
            }
            final ExternalId identifier = provider.getInstrument(expiry, strike, now.toLocalDate());
            final ValueRequirement requirement = new ValueRequirement(provider.getDataFieldName(), ComputationTargetType.PRIMITIVE, identifier);
            if (inputs.getValue(requirement) != null) {
              final Double volatility = (Double) inputs.getValue(requirement);
              volatilityValues.put(Pairs.of((Object) expiry, (Object) strike), volatility / 100);
            }
          }
        }
        final VolatilitySurfaceData<?, ?> volSurfaceData = new VolatilitySurfaceData<Object, Object>(_definition.getName(), _specification.getName(), _definition.getTarget().getUniqueId(),
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
