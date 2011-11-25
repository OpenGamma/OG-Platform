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
import com.opengamma.id.ExternalId;
import com.opengamma.id.UniqueId;
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

  public VolatilityCubeMarketDataFunction(final String currency, final String definitionName) {
    this(Currency.of(currency), definitionName);
  }

  public VolatilityCubeMarketDataFunction(final Currency currency, final String definitionName) {
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

  @SuppressWarnings("synthetic-access")
  @Override
  public CompiledFunctionDefinition compile(final FunctionCompilationContext context, final InstantProvider atInstant) {
    final Triple<InstantProvider, InstantProvider, VolatilityCubeSpecification> compile = _helper.compile(context, atInstant);
    
    Map<ExternalId, VolatilityPoint> pointsById = getPointsById();
    Map<ExternalId, Pair<Tenor, Tenor>> strikesById = getStrikesById();
    
    Set<ValueRequirement> reqs = buildRequirements(pointsById, strikesById);
    return new CompiledImpl(compile.getFirst(), compile.getSecond(), reqs, pointsById, strikesById);
  }

  private Set<ValueRequirement> buildRequirements(Map<ExternalId, VolatilityPoint> pointsById,
      Map<ExternalId, Pair<Tenor, Tenor>> strikesById) {
    final HashSet<ValueRequirement> ret = new HashSet<ValueRequirement>();
    ret.addAll(getMarketValueReqs(pointsById.keySet()));
    ret.addAll(getMarketValueReqs(strikesById.keySet()));
    ret.addAll(getOtherRequirements());
    return ret;
  }

  private Map<ExternalId, VolatilityPoint> getPointsById() {
    Map<ExternalId, VolatilityPoint> pointsById = new HashMap<ExternalId, VolatilityPoint>();

    final Iterable<VolatilityPoint> allPoints = _definition.getAllPoints();
    for (final VolatilityPoint point : allPoints) {
      Set<ExternalId> instruments = INSTRUMENT_PROVIDER.getInstruments(_helper.getCurrency(), point);
      if (instruments != null) {
        for (final ExternalId identifier : instruments) {
          pointsById.put(identifier, point);
        }
      }
    }
    return pointsById;
  }


  private Map<ExternalId, Pair<Tenor, Tenor>> getStrikesById() {
    Map<ExternalId, Pair<Tenor, Tenor>> strikesById = new HashMap<ExternalId, Pair<Tenor, Tenor>>();

    final Iterable<VolatilityPoint> allPoints = _definition.getAllPoints();
    for (final VolatilityPoint point : allPoints) {

      final ExternalId strikeInstruments = INSTRUMENT_PROVIDER.getStrikeInstrument(_helper.getCurrency(), point);
      if (strikeInstruments != null) {
        final ObjectsPair<Tenor, Tenor> strikePoint = Pair.of(point.getSwapTenor(), point.getOptionExpiry());
        final Pair<Tenor, Tenor> previous = strikesById.put(strikeInstruments, strikePoint);
        if (previous != null && !previous.equals(strikePoint)) {
          throw new OpenGammaRuntimeException("Mismatched volatility strike rate instrument");
        }
      }

    }
    return strikesById;
  }

  
  private Set<ValueRequirement> getOtherRequirements() {
    //TODO this
    return new HashSet<ValueRequirement>();
  }

  private Set<ValueRequirement> getMarketValueReqs(final Set<ExternalId> instruments) {
    final HashSet<ValueRequirement> ret = new HashSet<ValueRequirement>();
    if (instruments != null) {
      for (final ExternalId id : instruments) {
        ret.add(new ValueRequirement(MarketDataRequirementNames.MARKET_VALUE, new ComputationTargetSpecification(id)));
      }
    }
    return ret;
  }

  /**
   * 
   */
  private final class CompiledImpl extends AbstractFunction.AbstractInvokingCompiledFunction {

    private final Set<ValueRequirement> _requirements;
    private final Map<ExternalId, VolatilityPoint> _pointsById;
    private final Map<ExternalId, Pair<Tenor, Tenor>> _strikesById;

    private CompiledImpl(final InstantProvider earliest, final InstantProvider latest,
        final Set<ValueRequirement> requirements, Map<ExternalId, VolatilityPoint> pointsById, Map<ExternalId, Pair<Tenor, Tenor>> strikesById) {
      super(earliest, latest);
      _requirements = requirements;
      _pointsById = pointsById;
      _strikesById = strikesById;
    }

    @SuppressWarnings("synthetic-access")
    @Override
    public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs,
        final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
      final VolatilityCubeData map = buildMarketDataMap(inputs);
      return Sets.newHashSet(new ComputedValue(_marketDataResult, map));
    }

    @Override
    public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target,
        final ValueRequirement desiredValue) {
      if (canApplyTo(context, target)) {
        return _requirements;
      }
      return null;
    }

    @SuppressWarnings("synthetic-access")
    @Override
    public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
      return _results;
    }

    @Override
    public ComputationTargetType getTargetType() {
      return ComputationTargetType.PRIMITIVE;
    }

    @SuppressWarnings("synthetic-access")
    @Override
    public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
      return _helper.canApplyTo(context, target);
    }

    @Override
    public boolean canHandleMissingInputs() {
      return true;
    }
    
    private VolatilityCubeData buildMarketDataMap(final FunctionInputs inputs) {
      final HashMap<VolatilityPoint, Double> dataPoints = new HashMap<VolatilityPoint, Double>();
      final HashMap<VolatilityPoint, ExternalId> dataIds = new HashMap<VolatilityPoint, ExternalId>();
      final HashMap<VolatilityPoint, Double> relativeStrikes = new HashMap<VolatilityPoint, Double>();
      final HashMap<Pair<Tenor, Tenor>, Double> strikes = new HashMap<Pair<Tenor, Tenor>, Double>();

      final HashMap<UniqueId, Double> otherData = new HashMap<UniqueId, Double>();

      for (final ComputedValue value : inputs.getAllValues()) {
        if (!(value.getValue() instanceof Double)) {
          continue;
        }
        final Double dValue = (Double) value.getValue();
        final VolatilityPoint volatilityPoint = getVolatilityPoint(value.getSpecification());
        final Pair<Tenor, Tenor> strikePoint = getStrikePoint(value.getSpecification());
        if (volatilityPoint == null && strikePoint == null) {
          otherData.put(value.getSpecification().getTargetSpecification().getUniqueId(), dValue);
        } else if (volatilityPoint != null && strikePoint == null) {
          if (volatilityPoint.getRelativeStrike() > -50) {
            final Double previous = dataPoints.put(volatilityPoint, dValue);
            final ExternalId previousId = dataIds.put(volatilityPoint, value.getSpecification().getTargetSpecification().getIdentifier());
            final Double previousRelativeStrike = relativeStrikes.put(volatilityPoint, volatilityPoint.getRelativeStrike()); 
            if (previous != null && previous > dValue) {
              //TODO: this is a hack because we don't understand which tickers are for straddles, so we presume that the straddle has lower vol
              dataPoints.put(volatilityPoint, previous);
              dataIds.put(volatilityPoint, previousId);
              relativeStrikes.put(volatilityPoint, previousRelativeStrike);
            }
          }
        } else if (volatilityPoint == null && strikePoint != null) {
          final Double previous = strikes.put(strikePoint, dValue);
          if (previous != null) {
            throw new OpenGammaRuntimeException("Got two values for strike ");
          }
        } else {
          throw new OpenGammaRuntimeException("Instrument is both a volatility and a strike");
        }
      }

      final VolatilityCubeData volatilityCubeData = new VolatilityCubeData();
      volatilityCubeData.setDataPoints(dataPoints);
      final SnapshotDataBundle bundle = new SnapshotDataBundle();
      bundle.setDataPoints(otherData);
      volatilityCubeData.setOtherData(bundle);
      volatilityCubeData.setDataIds(dataIds);
      volatilityCubeData.setRelativeStrikes(relativeStrikes);
      volatilityCubeData.setATMStrikes(strikes);
      return volatilityCubeData;
    }
    
    private VolatilityPoint getVolatilityPoint(final ValueSpecification spec) {
      if (spec.getValueName() != MarketDataRequirementNames.MARKET_VALUE) {
        return null;
      }
      return _pointsById.get(spec.getTargetSpecification().getIdentifier());
    }

    private Pair<Tenor, Tenor> getStrikePoint(final ValueSpecification spec) {
      if (spec.getValueName() != MarketDataRequirementNames.MARKET_VALUE) {
        return null;
      }
      return _strikesById.get(spec.getTargetSpecification().getIdentifier());
    }
  }
}
