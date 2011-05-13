/*
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.ircurve;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.time.InstantProvider;

import org.apache.commons.lang.NotImplementedException;

import com.google.common.collect.Sets;
import com.opengamma.core.marketdatasnapshot.SnapshotDataBundle;
import com.opengamma.core.marketdatasnapshot.YieldCurveKey;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.function.YieldCurveDataSourcingFunction;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.id.Identifier;
import com.opengamma.util.money.Currency;

/**
 * 
 */
public class MarketInstrumentImpliedYieldCurveMarketDataFunction extends MarketInstrumentImpliedYieldCurveFunctionHelper  {

  private ValueSpecification _fundingCurveMarketDataResult;
  private ValueSpecification _forwardCurveMarketDataResult;
  
  private Set<ValueSpecification> _results;
  
  public MarketInstrumentImpliedYieldCurveMarketDataFunction(final String currency, final String curveDefinitionName) {
    this(currency, curveDefinitionName, curveDefinitionName);
  }

  public MarketInstrumentImpliedYieldCurveMarketDataFunction(final String currency, final String fundingCurveDefinitionName,
      final String forwardCurveDefinitionName) {
    this(Currency.of(currency), fundingCurveDefinitionName, forwardCurveDefinitionName);
  }

  public MarketInstrumentImpliedYieldCurveMarketDataFunction(final Currency currency, final String curveDefinitionName) {
    this(currency, curveDefinitionName, curveDefinitionName);
  }

  public MarketInstrumentImpliedYieldCurveMarketDataFunction(final Currency currency, final String fundingCurveDefinitionName,
      final String forwardCurveDefinitionName) {
    super(currency, fundingCurveDefinitionName, forwardCurveDefinitionName);
  }


  @Override
  public void init(final FunctionCompilationContext context) {
    super.init(context);
    
    _fundingCurveMarketDataResult = new ValueSpecification(ValueRequirementNames.YIELD_CURVE_MARKET_DATA, new ComputationTargetSpecification(getCurrency()),
        createValueProperties().with(ValuePropertyNames.CURVE, getFundingCurveDefinitionName()).get());
    _forwardCurveMarketDataResult = new ValueSpecification(ValueRequirementNames.YIELD_CURVE_MARKET_DATA, new ComputationTargetSpecification(getCurrency()),
        createValueProperties().with(ValuePropertyNames.CURVE, getForwardCurveDefinitionName()).get());
    _results = Sets.newHashSet(_fundingCurveMarketDataResult, _forwardCurveMarketDataResult);
  }


  /**
   * 
   */
  private final class CompiledImpl extends Compiled  implements YieldCurveDataSourcingFunction {

    private CompiledImpl(final InstantProvider earliest, final InstantProvider latest, final Currency targetCurrency,
        final Set<ValueRequirement> fundingCurveRequirements, final Set<ValueRequirement> forwardCurveRequirements) {
      super(earliest, latest, targetCurrency, fundingCurveRequirements, forwardCurveRequirements);
    }

    @Override
    public Set<ComputedValue> execute(FunctionExecutionContext executionContext, FunctionInputs inputs,
        ComputationTarget target, Set<ValueRequirement> desiredValues) {
      SnapshotDataBundle map = buildMarketDataMap(inputs);
      return Sets.newHashSet(
          new ComputedValue(_fundingCurveMarketDataResult, map),
          new ComputedValue(_forwardCurveMarketDataResult, map));
    }

    @Override
    public Set<ValueRequirement> getRequirements(FunctionCompilationContext context, ComputationTarget target,
        ValueRequirement desiredValue) {
      final Set<ValueRequirement> result = new HashSet<ValueRequirement>();
      result.addAll(getFundingCurveRequirements());
      result.addAll(getForwardCurveRequirements());
      return result;
    }

    @Override
    public Set<ValueSpecification> getResults(FunctionCompilationContext context, ComputationTarget target) {
      return _results;
    }

    public String getCurveName() {
      if (!getForwardCurveDefinitionName().equals(getFundingCurveDefinitionName())) {
        throw new NotImplementedException("Don't currently represent the two curve names");
      }
      return getForwardCurveDefinitionName();
    }

    @Override
    public Set<YieldCurveKey> getYieldCurveKeys() {
      return Sets.newHashSet(new YieldCurveKey(getCurrency(), getFundingCurveDefinitionName()), new YieldCurveKey(getCurrency(), getForwardCurveDefinitionName()));
    }
    
    
  }

  

  private SnapshotDataBundle buildMarketDataMap(final FunctionInputs inputs) {
    final Map<Identifier, Double> marketDataMap = new HashMap<Identifier, Double>();
    for (final ComputedValue value : inputs.getAllValues()) {
      final ComputationTargetSpecification targetSpecification = value.getSpecification().getTargetSpecification();
      if (value.getValue() instanceof Double) {
        marketDataMap.put(targetSpecification.getIdentifier(), (Double) value.getValue());
      }
    }
    SnapshotDataBundle snapshotDataBundle = new SnapshotDataBundle();
    snapshotDataBundle.setDataPoints(marketDataMap);
    return snapshotDataBundle;
  }

  @Override
  protected Compiled compileImpl(InstantProvider earliest, InstantProvider latest,
      InterpolatedYieldCurveSpecification fundingCurveSpecification, Set<ValueRequirement> fundingCurveRequirements,
      InterpolatedYieldCurveSpecification forwardCurveSpecification, Set<ValueRequirement> forwardCurveRequirements) {
    return new CompiledImpl(earliest, latest, getCurrency(), fundingCurveRequirements, forwardCurveRequirements);
  }



  

}
