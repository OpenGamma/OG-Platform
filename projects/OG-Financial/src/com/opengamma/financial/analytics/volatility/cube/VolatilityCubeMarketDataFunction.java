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
import com.opengamma.core.marketdatasnapshot.StructuredMarketDataKey;
import com.opengamma.core.marketdatasnapshot.VolatilityCubeData;
import com.opengamma.core.marketdatasnapshot.VolatilityCubeKey;
import com.opengamma.core.marketdatasnapshot.VolatilityPoint;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.CompiledFunctionDefinition;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.function.StructuredMarketDataDataSourcingFunction;
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

  public VolatilityCubeMarketDataFunction(final Currency currency, final String definitionName) {
    _helper = new VolatilityCubeFunctionHelper(currency, definitionName);
  }

  @Override
  public void init(final FunctionCompilationContext context) {
    _definition = _helper.init(context, this);

    final ComputationTargetSpecification currencySpec = new ComputationTargetSpecification(_helper.getKey().getCurrency());

    _marketDataResult = new ValueSpecification(ValueRequirementNames.VOLATILITY_CUBE_MARKET_DATA, currencySpec,
        createValueProperties().with(ValuePropertyNames.CUBE, _helper.getKey().getName()).get());
    _results = Sets.newHashSet(_marketDataResult);
  }

  @Override
  public CompiledFunctionDefinition compile(final FunctionCompilationContext context, final InstantProvider atInstant) {
    final Triple<InstantProvider, InstantProvider, VolatilityCubeSpecification> compile = _helper.compile(context, atInstant);
    return new CompiledImpl(compile.getFirst(), compile.getSecond(), buildRequirements(compile.getThird(), context),
        _helper.getKey());
  }

  private Set<ValueRequirement> buildRequirements(final VolatilityCubeSpecification third, final FunctionCompilationContext context) {
    _pointsById = new HashMap<Identifier, VolatilityPoint>();
    _strikesById = new HashMap<Identifier, Pair<Tenor, Tenor>>();

    final HashSet<ValueRequirement> ret = new HashSet<ValueRequirement>();
    final Iterable<VolatilityPoint> allPoints = _definition.getAllPoints();
    for (final VolatilityPoint point : allPoints) {
      final Set<ValueRequirement> valueRequirements = getValueRequirements(point);
      ret.addAll(valueRequirements);
    }
    ret.addAll(getOtherRequirements());
    return ret;
  }

  private Set<ValueRequirement> getOtherRequirements() {
    //TODO this
    return new HashSet<ValueRequirement>();
  }

  private Set<ValueRequirement> getValueRequirements(final VolatilityPoint point) {
    Set<Identifier> instruments = INSTRUMENT_PROVIDER.getInstruments(_helper.getKey()
        .getCurrency(), point);
    if (instruments != null) {
      for (final Identifier identifier : instruments) {
        _pointsById.put(identifier, point);
      }

      final Identifier strikeInstruments = INSTRUMENT_PROVIDER.getStrikeInstrument(_helper.getKey().getCurrency(), point);
      if (strikeInstruments != null) {
        final Set<Identifier> instrumentsWithStrike = new HashSet<Identifier>(instruments);
        final ObjectsPair<Tenor, Tenor> strikePoint = Pair.of(point.getSwapTenor(), point.getOptionExpiry());
        final Pair<Tenor, Tenor> previous = _strikesById.put(strikeInstruments, strikePoint);
        if (previous != null && !previous.equals(strikePoint)) {
          throw new OpenGammaRuntimeException("Mismatched volatility strike rate instrument");
        }
        instrumentsWithStrike.add(strikeInstruments);
        instruments = instrumentsWithStrike;
      }
    }

    return getMarketValueReqs(instruments);
  }

  private Set<ValueRequirement> getMarketValueReqs(final Set<Identifier> instruments) {
    final HashSet<ValueRequirement> ret = new HashSet<ValueRequirement>();
    if (instruments != null) {
      for (final Identifier id : instruments) {
        ret.add(new ValueRequirement(MarketDataRequirementNames.MARKET_VALUE, new ComputationTargetSpecification(id)));
      }
    }
    return ret;
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

  private VolatilityCubeData buildMarketDataMap(final FunctionInputs inputs) {
    final HashMap<VolatilityPoint, Double> dataPoints = new HashMap<VolatilityPoint, Double>();
    final HashMap<Pair<Tenor, Tenor>, Double> strikes = new HashMap<Pair<Tenor, Tenor>, Double>();

    final HashMap<UniqueIdentifier, Double> otherData = new HashMap<UniqueIdentifier, Double>();

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
          if (previous != null && previous > dValue) {
            //TODO: this is a hack because we don't understand which tickers are for straddles, so we presume that the straddle has lower vol
            dataPoints.put(volatilityPoint, previous);
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

    volatilityCubeData.setStrikes(strikes);
    return volatilityCubeData;
  }

  /**
   * 
   */
  private final class CompiledImpl extends AbstractFunction.AbstractInvokingCompiledFunction implements StructuredMarketDataDataSourcingFunction {

    private final Set<ValueRequirement> _requirements;
    private final VolatilityCubeKey _volatilityCubeKey;

    private CompiledImpl(final InstantProvider earliest, final InstantProvider latest,
        final Set<ValueRequirement> requirements, final VolatilityCubeKey volatilityCubeKey) {
      super(earliest, latest);
      _requirements = requirements;
      _volatilityCubeKey = volatilityCubeKey;
    }

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

    @Override
    public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
      return _results;
    }

    @Override
    public ComputationTargetType getTargetType() {
      return ComputationTargetType.PRIMITIVE;
    }

    @Override
    public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
      return _helper.canApplyTo(context, target);
    }

    @Override
    public Set<Pair<StructuredMarketDataKey, ValueSpecification>> getStructuredMarketData() {
      final HashSet<Pair<StructuredMarketDataKey, ValueSpecification>> ret = new HashSet<Pair<StructuredMarketDataKey, ValueSpecification>>();
      ret.add(Pair.of((StructuredMarketDataKey) _volatilityCubeKey, _marketDataResult));
      return ret;
    }

    @Override
    public boolean canHandleMissingInputs() {
      return true;
    }
  }
}
