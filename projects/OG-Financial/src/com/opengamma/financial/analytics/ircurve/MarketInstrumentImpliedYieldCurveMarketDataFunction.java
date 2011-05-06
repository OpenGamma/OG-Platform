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

import com.google.common.collect.Sets;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
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
public class MarketInstrumentImpliedYieldCurveMarketDataFunction extends MarketInstrumentImpliedYieldCurveFunctionHelper {

  private ValueSpecification _marketDataResult;
  
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
    
    _marketDataResult = new ValueSpecification(ValueRequirementNames.YIELD_CURVE_MARKET_DATA, new ComputationTargetSpecification(getCurrency()),
        createValueProperties().with(ValuePropertyNames.CURVE, getFundingCurveDefinitionName()).with(ValuePropertyNames.CURVE, getForwardCurveDefinitionName()).get());
    _results = Sets.newHashSet(_marketDataResult);
  }


  /**
   * 
   */
  private final class CompiledImpl extends Compiled {

    private CompiledImpl(final InstantProvider earliest, final InstantProvider latest, final Currency targetCurrency,
        final Set<ValueRequirement> fundingCurveRequirements, final Set<ValueRequirement> forwardCurveRequirements) {
      super(earliest, latest, targetCurrency, fundingCurveRequirements, forwardCurveRequirements);
    }

    @Override
    public Set<ComputedValue> execute(FunctionExecutionContext executionContext, FunctionInputs inputs,
        ComputationTarget target, Set<ValueRequirement> desiredValues) {
      return Sets.newHashSet(new ComputedValue(_marketDataResult, buildMarketDataMap(inputs)));
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
  }

  

  private Map<Identifier, Double> buildMarketDataMap(final FunctionInputs inputs) {
    final Map<Identifier, Double> marketDataMap = new HashMap<Identifier, Double>();
    for (final ComputedValue value : inputs.getAllValues()) {
      final ComputationTargetSpecification targetSpecification = value.getSpecification().getTargetSpecification();
      if (value.getValue() instanceof Double) {
        marketDataMap.put(targetSpecification.getIdentifier(), (Double) value.getValue());
      }
    }
    return marketDataMap;
  }

  @Override
  protected Compiled compileImpl(InstantProvider earliest, InstantProvider latest,
      InterpolatedYieldCurveSpecification fundingCurveSpecification, Set<ValueRequirement> fundingCurveRequirements,
      InterpolatedYieldCurveSpecification forwardCurveSpecification, Set<ValueRequirement> forwardCurveRequirements) {
    return new CompiledImpl(earliest, latest, getCurrency(), fundingCurveRequirements, forwardCurveRequirements);
  }



  

}
