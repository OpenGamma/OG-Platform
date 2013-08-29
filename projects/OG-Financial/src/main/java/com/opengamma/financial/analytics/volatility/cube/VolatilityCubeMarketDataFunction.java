/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.volatility.cube;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.threeten.bp.Instant;

import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.marketdatasnapshot.SnapshotDataBundle;
import com.opengamma.core.marketdatasnapshot.VolatilityCubeData;
import com.opengamma.core.marketdatasnapshot.VolatilityPoint;
import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.CompiledFunctionDefinition;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.marketdata.ExternalIdBundleResolver;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Tenor;
import com.opengamma.util.tuple.ObjectsPair;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Triple;

/**
 *
 */
public class VolatilityCubeMarketDataFunction extends AbstractFunction {

  private static final BloombergSwaptionVolatilityCubeInstrumentProvider INSTRUMENT_PROVIDER = BloombergSwaptionVolatilityCubeInstrumentProvider.BLOOMBERG;

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
    if (_definition == null) {
      throw new UnsupportedOperationException("Definition " + _helper.getDefinitionName() + " on " + _helper.getCurrency() + " failed");
    }
    final ComputationTargetSpecification currencySpec = ComputationTargetSpecification.of(_helper.getCurrency());
    _marketDataResult = new ValueSpecification(ValueRequirementNames.VOLATILITY_CUBE_MARKET_DATA, currencySpec,
        createValueProperties().with(ValuePropertyNames.CUBE, _helper.getDefinitionName()).get());
    _results = Sets.newHashSet(_marketDataResult);
  }

  @SuppressWarnings("synthetic-access")
  @Override
  public CompiledFunctionDefinition compile(final FunctionCompilationContext context, final Instant atInstant) {
    final Triple<Instant, Instant, VolatilityCubeSpecification> compile = _helper.compile(context, atInstant);

    final Map<ExternalId, VolatilityPoint> pointsById = getPointsById();
    final Map<ExternalId, Pair<Tenor, Tenor>> strikesById = getStrikesById();

    final Set<ValueRequirement> reqs = buildRequirements(pointsById, strikesById);
    return new CompiledImpl(compile.getFirst(), compile.getSecond(), reqs, pointsById, strikesById);
  }

  private static Set<ValueRequirement> buildRequirements(final Map<ExternalId, VolatilityPoint> pointsById, final Map<ExternalId, Pair<Tenor, Tenor>> strikesById) {
    final HashSet<ValueRequirement> ret = new HashSet<>();
    ret.addAll(getMarketValueReqs(pointsById.keySet()));
    ret.addAll(getMarketValueReqs(strikesById.keySet()));
    ret.addAll(getOtherRequirements());
    return ret;
  }

  private Map<ExternalId, VolatilityPoint> getPointsById() {
    final Map<ExternalId, VolatilityPoint> pointsById = new HashMap<>();

    final Iterable<VolatilityPoint> allPoints = _definition.getAllPoints();
    for (final VolatilityPoint point : allPoints) {
      final Set<ExternalId> instruments = INSTRUMENT_PROVIDER.getInstruments(_helper.getCurrency(), point);
      if (instruments != null) {
        for (final ExternalId identifier : instruments) {
          pointsById.put(identifier, point);
        }
      }
    }
    return pointsById;
  }

  private Map<ExternalId, Pair<Tenor, Tenor>> getStrikesById() {
    final Map<ExternalId, Pair<Tenor, Tenor>> strikesById = new HashMap<>();

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

  private static Set<ValueRequirement> getOtherRequirements() {
    //TODO this
    return new HashSet<>();
  }

  private static Set<ValueRequirement> getMarketValueReqs(final Set<ExternalId> instruments) {
    final HashSet<ValueRequirement> ret = new HashSet<>();
    if (instruments != null) {
      for (final ExternalId id : instruments) {
        ret.add(new ValueRequirement(MarketDataRequirementNames.MARKET_VALUE, ComputationTargetType.PRIMITIVE, id));
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

    private CompiledImpl(final Instant earliest, final Instant latest,
        final Set<ValueRequirement> requirements, final Map<ExternalId, VolatilityPoint> pointsById, final Map<ExternalId, Pair<Tenor, Tenor>> strikesById) {
      super(earliest, latest);
      _requirements = requirements;
      _pointsById = pointsById;
      _strikesById = strikesById;
    }

    @SuppressWarnings("synthetic-access")
    @Override
    public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs,
        final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
      final VolatilityCubeData map = buildMarketDataMap(executionContext, inputs);
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
      return ComputationTargetType.CURRENCY;
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

    @Override
    public boolean canHandleMissingRequirements() {
      return true;
    }

    private VolatilityCubeData buildMarketDataMap(final FunctionExecutionContext context, final FunctionInputs inputs) {
      final HashMap<VolatilityPoint, Double> dataPoints = new HashMap<>();
      final HashMap<VolatilityPoint, ExternalIdBundle> dataIds = new HashMap<>();
      final HashMap<VolatilityPoint, Double> relativeStrikes = new HashMap<>();
      final HashMap<Pair<Tenor, Tenor>, Double> strikes = new HashMap<>();
      final SnapshotDataBundle otherData = new SnapshotDataBundle();
      final ExternalIdBundleResolver resolver = new ExternalIdBundleResolver(context.getComputationTargetResolver());
      for (final ComputedValue value : inputs.getAllValues()) {
        if (!(value.getValue() instanceof Double)) {
          continue;
        }
        final Double dValue = (Double) value.getValue();
        final ExternalIdBundle identifiers = value.getSpecification().getTargetSpecification().accept(resolver);
        final VolatilityPoint volatilityPoint;
        final Pair<Tenor, Tenor> strikePoint;
        if (value.getSpecification().getValueName() == MarketDataRequirementNames.MARKET_VALUE) {
          volatilityPoint = getByIdentifier(_pointsById, identifiers);
          strikePoint = getByIdentifier(_strikesById, identifiers);
        } else {
          volatilityPoint = null;
          strikePoint = null;
        }
        if (volatilityPoint == null && strikePoint == null) {
          otherData.setDataPoint(identifiers, dValue);
        } else if (volatilityPoint != null && strikePoint == null) {
          if (volatilityPoint.getRelativeStrike() > -50) {
            final Double previous = dataPoints.put(volatilityPoint, dValue);
            final ExternalIdBundle previousIds = dataIds.put(volatilityPoint, identifiers);
            final Double previousRelativeStrike = relativeStrikes.put(volatilityPoint, volatilityPoint.getRelativeStrike());
            if (previous != null && previous > dValue) {
              //TODO: this is a hack because we don't understand which tickers are for straddles, so we presume that the straddle has lower vol
              dataPoints.put(volatilityPoint, previous);
              dataIds.put(volatilityPoint, previousIds);
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
      volatilityCubeData.setOtherData(otherData);
      volatilityCubeData.setDataIds(dataIds);
      volatilityCubeData.setRelativeStrikes(relativeStrikes);
      volatilityCubeData.setATMStrikes(strikes);
      return volatilityCubeData;
    }

  }

  private static <T> T getByIdentifier(final Map<ExternalId, T> data, final ExternalIdBundle identifiers) {
    for (final ExternalId identifier : identifiers) {
      final T value = data.get(identifier);
      if (value != null) {
        return value;
      }
    }
    return null;
  }

}
