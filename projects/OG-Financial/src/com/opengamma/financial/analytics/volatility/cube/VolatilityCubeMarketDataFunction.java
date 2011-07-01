/*
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.volatility.cube;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.time.InstantProvider;

import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.marketdatasnapshot.SnapshotDataBundle;
import com.opengamma.core.marketdatasnapshot.VolatilityCubeData;
import com.opengamma.core.marketdatasnapshot.VolatilityPoint;
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
import com.opengamma.id.Identifier;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.livedata.normalization.MarketDataRequirementNames;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Tenor;
import com.opengamma.util.tuple.ObjectsPair;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Triple;

/**
 * 
 */
public class VolatilityCubeMarketDataFunction extends AbstractFunction {

  private static final VolatilityCubeInstrumentProvider INSTRUMENT_PROVIDER = VolatilityCubeInstrumentProvider.BLOOMBERG;
  
  private ValueSpecification _marketDataResult;
  private Set<ValueSpecification> _results;
  private final VolatilityCubeFunctionHelper _helper;
  private VolatilityCubeDefinition _definition;

  private Map<Identifier, VolatilityPoint> _pointsById;
  private Map<Identifier, Pair<Tenor, Tenor>> _strikesById;
  
  public VolatilityCubeMarketDataFunction(final String currency, final String definitionName) {
    this(Currency.of(currency), definitionName);
  }

  public VolatilityCubeMarketDataFunction(Currency currency, String definitionName) {
    _helper = new VolatilityCubeFunctionHelper(currency, definitionName);
  }
  
  @Override
  public void init(final FunctionCompilationContext context) {
    _definition = _helper.init(context, this);
    
    final ComputationTargetSpecification currencySpec = new ComputationTargetSpecification(_helper.getCurrency());
    
    _marketDataResult = new ValueSpecification(ValueRequirementNames.VOLATILITY_CUBE_MARKET_DATA, currencySpec,
        createValueProperties().with(ValuePropertyNames.CUBE, _helper.getDefinitionName()).get());
    _results = Sets.newHashSet(_marketDataResult);
  }
  
  @Override
  public CompiledFunctionDefinition compile(FunctionCompilationContext context, InstantProvider atInstant) {
    Triple<InstantProvider, InstantProvider, VolatilityCubeSpecification> compile = _helper.compile(context, atInstant);
    return new CompiledImpl(compile.getFirst(), compile.getSecond(), buildRequirements(compile.getThird(), context));
  }
  
  private Set<ValueRequirement> buildRequirements(VolatilityCubeSpecification third, FunctionCompilationContext context) {
    _pointsById =  new HashMap<Identifier, VolatilityPoint>();
    _strikesById = new HashMap<Identifier, Pair<Tenor, Tenor>>();
    
    HashSet<ValueRequirement> ret = new HashSet<ValueRequirement>();
    Iterable<VolatilityPoint> allPoints = _definition.getAllPoints();
    for (VolatilityPoint point : allPoints) {
      Set<ValueRequirement> valueRequirements = getValueRequirements(point);
      ret.addAll(valueRequirements);
    }
    ret.addAll(getOtherRequirements());
    return ret;
  }

  private Set<ValueRequirement> getOtherRequirements() {
    //TODO this
    return new HashSet<ValueRequirement>();
  }

  private Set<ValueRequirement> getValueRequirements(VolatilityPoint point) {
    Set<Identifier> instruments = INSTRUMENT_PROVIDER.getInstruments(_helper.getCurrency(), point);
    if (instruments != null) {
      for (Identifier identifier : instruments) {
        _pointsById.put(identifier, point);
      }
      
      Identifier strikeInstruments = INSTRUMENT_PROVIDER.getStrikeInstrument(_helper.getCurrency(), point);
      if (strikeInstruments != null) {
        Set<Identifier> instrumentsWithStrike = new HashSet<Identifier>(instruments);
        ObjectsPair<Tenor, Tenor> strikePoint = Pair.of(point.getSwapTenor(), point.getOptionExpiry());
        Pair<Tenor, Tenor> previous = _strikesById.put(strikeInstruments, strikePoint);
        if (previous != null && !previous.equals(strikePoint)) {
          throw new OpenGammaRuntimeException("Mismatched volatility strike rate instrument");
        }
        instrumentsWithStrike.add(strikeInstruments);
        instruments = instrumentsWithStrike;
      }
    }
    
    return getMarketValueReqs(instruments);
  }

  private Set<ValueRequirement> getMarketValueReqs(Set<Identifier> instruments) {
    HashSet<ValueRequirement> ret = new HashSet<ValueRequirement>();
    if (instruments != null) {
      for (Identifier id : instruments) {
        ret.add(new ValueRequirement(MarketDataRequirementNames.MARKET_VALUE, new ComputationTargetSpecification(id)));
      }
    }
    return ret;
  }
  
  private VolatilityPoint getVolatilityPoint(ValueSpecification spec) {
    if (spec.getValueName() != MarketDataRequirementNames.MARKET_VALUE) {
      return null;
    }
    return _pointsById.get(spec.getTargetSpecification().getIdentifier());
  }

  private Pair<Tenor, Tenor> getStrikePoint(ValueSpecification spec) {
    if (spec.getValueName() != MarketDataRequirementNames.MARKET_VALUE) {
      return null;
    }
    return _strikesById.get(spec.getTargetSpecification().getIdentifier());
  }
  private VolatilityCubeData buildMarketDataMap(final FunctionInputs inputs) {
    HashMap<VolatilityPoint, Double> dataPoints = new HashMap<VolatilityPoint, Double>();
    HashMap<Pair<Tenor, Tenor>, Double> strikes = new HashMap<Pair<Tenor, Tenor>, Double>();
    
    HashMap<UniqueIdentifier, Double> otherData = new HashMap<UniqueIdentifier, Double>();
    
    for (ComputedValue value : inputs.getAllValues()) {
      if (!(value.getValue() instanceof Double)) {
        continue;
      }
      Double dValue = (Double) value.getValue();
      VolatilityPoint volatilityPoint = getVolatilityPoint(value.getSpecification());
      Pair<Tenor, Tenor> strikePoint = getStrikePoint(value.getSpecification());
      if (volatilityPoint == null && strikePoint == null) {

        otherData.put(value.getSpecification().getTargetSpecification().getUniqueId(), dValue);
      } else if (volatilityPoint != null && strikePoint == null) {
        Double previous = dataPoints.put(volatilityPoint, dValue);
        if (previous != null && previous > dValue) {
          //TODO: this is a hack because we don't understand which tickers are for straddles, so we presume that the straddle has lower vol
          dataPoints.put(volatilityPoint, previous);
        }
      } else if (volatilityPoint == null && strikePoint != null) {
        Double previous = strikes.put(strikePoint, dValue);
        if (previous != null) {
          throw new OpenGammaRuntimeException("Got two values for strike ");
        }
      } else {
        throw new OpenGammaRuntimeException("Instrument is both a volatility and a strike");
      }
    }
        
    VolatilityCubeData volatilityCubeData = new VolatilityCubeData();
    volatilityCubeData.setDataPoints(dataPoints);
    SnapshotDataBundle bundle = new SnapshotDataBundle();
    bundle.setDataPoints(otherData);
    volatilityCubeData.setOtherData(bundle);
    
    volatilityCubeData.setStrikes(strikes);
    return volatilityCubeData;
  }
  
  /**
   * 
   */
  private final class CompiledImpl extends AbstractFunction.AbstractInvokingCompiledFunction {

    private final Set<ValueRequirement> _requirements;

    private CompiledImpl(final InstantProvider earliest, final InstantProvider latest,
        final Set<ValueRequirement> requirements) {
      super(earliest, latest);
      _requirements = requirements;
    }

    @Override
    public Set<ComputedValue> execute(FunctionExecutionContext executionContext, FunctionInputs inputs,
        ComputationTarget target, Set<ValueRequirement> desiredValues) {
      VolatilityCubeData map = buildMarketDataMap(inputs);
      return Sets.newHashSet(new ComputedValue(_marketDataResult, map));
    }

    @Override
    public Set<ValueRequirement> getRequirements(FunctionCompilationContext context, ComputationTarget target,
        ValueRequirement desiredValue) {
      if (canApplyTo(context, target)) {
        return _requirements;
      }
      return null;
    }

    @Override
    public Set<ValueSpecification> getResults(FunctionCompilationContext context, ComputationTarget target) {
      return _results;
    }

    @Override
    public ComputationTargetType getTargetType() {
      return ComputationTargetType.PRIMITIVE;
    }

    @Override
    public boolean canApplyTo(FunctionCompilationContext context, ComputationTarget target) {
      return _helper.canApplyTo(context, target);
    }
    
    @Override
    public boolean canHandleMissingInputs() {
      return true;
    }
  }
}
