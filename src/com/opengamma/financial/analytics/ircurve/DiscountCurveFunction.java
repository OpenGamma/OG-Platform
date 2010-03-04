/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.ircurve;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.lang.ObjectUtils;
import org.fudgemsg.FudgeFieldContainer;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.function.FunctionInvoker;
import com.opengamma.engine.value.MarketDataFieldNames;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.model.interestrate.curve.DiscountCurve;
import com.opengamma.financial.model.interestrate.curve.InterpolatedDiscountCurve;
import com.opengamma.math.interpolation.Interpolator1D;
import com.opengamma.math.interpolation.Interpolator1DFactory;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 *
 * @author kirk
 */
public class DiscountCurveFunction
extends AbstractFunction 
implements FunctionInvoker {
  private final Interpolator1D _interpolator; 
  private final DiscountCurveDefinition _definition;
  private final Set<ValueRequirement> _requirements;
  private final ValueSpecification _result;
  private final Set<ValueSpecification> _results;
  
  public DiscountCurveFunction(DiscountCurveDefinition definition) {
    ArgumentChecker.checkNotNull(definition, "Discount Curve Definition");
    _definition = definition;
    
    _interpolator = Interpolator1DFactory.getInterpolator(_definition.getInterpolatorName());
    _requirements = Collections.unmodifiableSet(buildRequirements(_definition));
    _result = new ValueSpecification(new ValueRequirement(ValueRequirementNames.DISCOUNT_CURVE, ComputationTargetType.PRIMITIVE, _definition.getCurrency().getISOCode()));
    _results = Collections.singleton(_result);
  }

  /**
   * @param definition
   * @return
   */
  public static Set<ValueRequirement> buildRequirements(DiscountCurveDefinition definition) {
    Set<ValueRequirement> result = new HashSet<ValueRequirement>();
    for(FixedIncomeStrip strip : definition.getStrips()) {
      ValueRequirement requirement = new ValueRequirement(ValueRequirementNames.MARKET_DATA_HEADER, strip.getMarketDataSpecification());
      result.add(requirement);
    }
    return result;
  }

  /**
   * @return the definition
   */
  public DiscountCurveDefinition getDefinition() {
    return _definition;
  }

  @Override
  public boolean canApplyTo(FunctionCompilationContext context, ComputationTarget target) {
    if(target.getType() != ComputationTargetType.PRIMITIVE) {
      return false;
    }
    if(target.getValue() instanceof String) {
      String currencyKey = (String) target.getValue();
      return ObjectUtils.equals(currencyKey, getDefinition().getCurrency().getISOCode());
    }
    return false;
  }

  @Override
  public Set<ValueRequirement> getRequirements(FunctionCompilationContext context, ComputationTarget target) {
    if(canApplyTo(context, target)) {
      return _requirements;
    }
    return null;
  }

  @Override
  public Set<ValueSpecification> getResults(FunctionCompilationContext context, ComputationTarget target, Set<ValueRequirement> requirements) {
    if(canApplyTo(context, target)) {
      return _results;
    }
    return null;
  }

  @Override
  public boolean buildsOwnSubGraph() {
    return false;
  }

  @Override
  public String getShortName() {
    return getDefinition().getCurrency() + "-" + getDefinition().getName() + " Discount Curve";
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.PRIMITIVE;
  }

  @Override
  public Set<ComputedValue> execute(
      FunctionExecutionContext executionContext,
      FunctionInputs inputs,
      ComputationTarget target) {
    // Gather market data rates
    // Note that this assumes that all strips are priced in decimal percent. We need to resolve
    // that ultimately in OG-LiveData normalization and pull out the OGRate key rather than
    // the crazy IndicativeValue name.
    Map<Double, Double> timeInYearsToRates = new TreeMap<Double, Double>();
    for(FixedIncomeStrip strip : getDefinition().getStrips()) {
      ValueRequirement stripRequirement = new ValueRequirement(ValueRequirementNames.MARKET_DATA_HEADER, strip.getMarketDataSpecification());
      FudgeFieldContainer fieldContainer = (FudgeFieldContainer) inputs.getValue(stripRequirement);
      Double price = fieldContainer.getDouble(MarketDataFieldNames.INDICATIVE_VALUE_NAME);
      timeInYearsToRates.put(strip.getNumYears(), price);
    }
    // Bootstrap the discount curve
    DiscountCurve discountCurve = new InterpolatedDiscountCurve(timeInYearsToRates, _interpolator);
    // Prepare results
    ComputedValue resultValue = new ComputedValue(_result, discountCurve);
    return Collections.singleton(resultValue);
  }

}
