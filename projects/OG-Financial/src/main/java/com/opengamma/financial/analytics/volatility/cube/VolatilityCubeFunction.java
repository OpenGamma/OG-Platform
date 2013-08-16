/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
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
import com.opengamma.core.marketdatasnapshot.VolatilityCubeData;
import com.opengamma.core.marketdatasnapshot.VolatilityPoint;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.CompiledFunctionDefinition;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Tenor;
import com.opengamma.util.tuple.Pair;

/**
 *
 */
public class VolatilityCubeFunction extends AbstractFunction {
  private final VolatilityCubeFunctionHelper _helper;
  private ValueSpecification _cubeResult;
  private HashSet<ValueSpecification> _results;

  public VolatilityCubeFunction(final String currency, final String definitionName) {
    this(Currency.of(currency), definitionName);
  }

  public VolatilityCubeFunction(final Currency currency, final String definitionName) {
    _helper = new VolatilityCubeFunctionHelper(currency, definitionName);
  }

  @Override
  public void init(final FunctionCompilationContext context) {
    final ComputationTargetSpecification currencyTargetSpec = ComputationTargetSpecification.of(_helper.getCurrency());
    _cubeResult = new ValueSpecification(ValueRequirementNames.STANDARD_VOLATILITY_CUBE_DATA, currencyTargetSpec,
        createValueProperties().with(ValuePropertyNames.CUBE, _helper.getDefinitionName()).get());
    _results = Sets.newHashSet(_cubeResult);
  }

  @Override
  public CompiledFunctionDefinition compile(final FunctionCompilationContext context, final Instant atInstant) {
    final Set<ValueRequirement> requirements = Sets.newHashSet(getMarketDataRequirement());
    return new AbstractFunction.AbstractInvokingCompiledFunction() {

      @Override
      public ComputationTargetType getTargetType() {
        return ComputationTargetType.CURRENCY;
      }

      @Override
      public Set<ValueSpecification> getResults(final FunctionCompilationContext compilationContext, final ComputationTarget target) {
        return _results;
      }

      @Override
      public Set<ValueRequirement> getRequirements(final FunctionCompilationContext compilationContext, final ComputationTarget target,
          final ValueRequirement desiredValue) {
        return requirements;
      }

      @Override
      public boolean canApplyTo(final FunctionCompilationContext compilationContext, final ComputationTarget target) {
        return _helper.getCurrency().equals(target.getValue());
      }

      @Override
      public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs,
          final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
        final VolatilityCubeData data = (VolatilityCubeData) inputs.getValue(getMarketDataRequirement());
        final VolatilityCubeData normalizedData = new VolatilityCubeData();
        final Map<VolatilityPoint, Double> volatilityPoints = data.getDataPoints();
        final Map<VolatilityPoint, Double> normalizedVolatilityPoints = new HashMap<>();
        final Map<VolatilityPoint, ExternalIdBundle> volatilityPointIds = data.getDataIds();
        final Map<VolatilityPoint, ExternalIdBundle> normalizedVolatilityPointIds = new HashMap<>();
        final Map<VolatilityPoint, Double> relativeStrikes = data.getRelativeStrikes();
        final Map<VolatilityPoint, Double> normalizedRelativeStrikes = new HashMap<>();
        final Map<Pair<Tenor, Tenor>, Double> atmStrikes = data.getATMStrikes();
        final Map<Pair<Tenor, Tenor>, Double> normalizedATMStrikes = new HashMap<>();
        final Map<Pair<Tenor, Tenor>, Double> normalizedATMVols = new HashMap<>();
        for (final Map.Entry<VolatilityPoint, Double> entry : volatilityPoints.entrySet()) {
          final VolatilityPoint oldPoint = entry.getKey();
          final Tenor swapTenor = oldPoint.getSwapTenor();
          final Tenor swaptionExpiry = oldPoint.getOptionExpiry();
          final double relativeStrike = oldPoint.getRelativeStrike();
          if (atmStrikes.containsKey(Pair.of(swapTenor, swaptionExpiry))) {
            final Pair<Tenor, Tenor> tenorPair = Pair.of(swapTenor, swaptionExpiry);
            final double absoluteStrike = atmStrikes.get(tenorPair) + relativeStrike / 10000;
            final double vol = entry.getValue();
            final VolatilityPoint newPoint = new VolatilityPoint(swapTenor, swaptionExpiry, absoluteStrike);
            if (Double.doubleToLongBits(relativeStrike) == 0) {
              if (normalizedATMStrikes.containsKey(tenorPair)) {
                throw new OpenGammaRuntimeException("Normalized ATM strike data set already contains value for " + tenorPair);
              }
              normalizedATMStrikes.put(tenorPair, atmStrikes.get(tenorPair));
              normalizedATMVols.put(tenorPair, vol);
            }
            normalizedVolatilityPoints.put(newPoint, vol);
            if (volatilityPointIds != null) {
              normalizedVolatilityPointIds.put(newPoint, volatilityPointIds.get(oldPoint));
            }
            if (relativeStrikes != null) {
              normalizedRelativeStrikes.put(newPoint, relativeStrikes.get(oldPoint));
            } else {
              normalizedRelativeStrikes.put(newPoint, relativeStrike);
            }
          }
        }
        normalizedData.setDataPoints(normalizedVolatilityPoints);
        normalizedData.setOtherData(data.getOtherData());
        normalizedData.setATMStrikes(normalizedATMStrikes);
        normalizedData.setATMVolatilities(normalizedATMVols);
        normalizedData.setDataIds(normalizedVolatilityPointIds);
        normalizedData.setRelativeStrikes(normalizedRelativeStrikes);
        return Sets.newHashSet(new ComputedValue(_cubeResult, normalizedData));
      }
    };
  }

  private ValueRequirement getMarketDataRequirement() {
    return new ValueRequirement(ValueRequirementNames.VOLATILITY_CUBE_MARKET_DATA,
        ComputationTargetSpecification.of(_helper.getCurrency()),
        ValueProperties.with(ValuePropertyNames.CUBE, _helper.getDefinitionName()).get());
  }

}
