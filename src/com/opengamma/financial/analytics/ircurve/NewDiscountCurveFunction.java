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
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.NewFunctionDefinition;
import com.opengamma.engine.function.NewFunctionInputs;
import com.opengamma.engine.function.NewFunctionInvoker;
import com.opengamma.engine.value.MarketDataComputedValue;
import com.opengamma.engine.value.NewComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.Currency;
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
public class NewDiscountCurveFunction
extends AbstractFunction 
implements NewFunctionDefinition, NewFunctionInvoker {
  private final Interpolator1D _interpolator; 
  private final NewDiscountCurveDefinition _definition;
  private final Set<ValueRequirement> _requirements;
  private final ValueSpecification _result;
  private final Set<ValueSpecification> _results;
  
  public NewDiscountCurveFunction(NewDiscountCurveDefinition definition) {
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
  public static Set<ValueRequirement> buildRequirements(NewDiscountCurveDefinition definition) {
    Set<ValueRequirement> result = new HashSet<ValueRequirement>();
    for(NewFixedIncomeStrip strip : definition.getStrips()) {
      ValueRequirement requirement = new ValueRequirement(ValueRequirementNames.MARKET_DATA_HEADER, strip.getMarketDataSpecification());
      result.add(requirement);
    }
    return result;
  }

  /**
   * @return the definition
   */
  public NewDiscountCurveDefinition getDefinition() {
    return _definition;
  }

  @Override
  public boolean canApplyTo(ComputationTarget target) {
    if(target.getType() != ComputationTargetType.PRIMITIVE) {
      return false;
    }
    if(target.getValue() instanceof Currency) {
      Currency currencyKey = (Currency) target.getValue();
      return ObjectUtils.equals(currencyKey, getDefinition().getCurrency());
    }
    return false;
  }

  @Override
  public Set<ValueRequirement> getRequirements(ComputationTarget target) {
    if(canApplyTo(target)) {
      return _requirements;
    }
    return null;
  }

  @Override
  public Set<ValueSpecification> getResults(ComputationTarget target) {
    if(canApplyTo(target)) {
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
  public Set<NewComputedValue> execute(
      FunctionExecutionContext executionContext,
      NewFunctionInputs inputs,
      ComputationTarget target) {
    // Gather market data rates
    // Note that this assumes that all strips are priced in decimal percent. We need to resolve
    // that ultimately in OG-LiveData normalization and pull out the OGRate key rather than
    // the crazy IndicativeValue name.
    Map<Double, Double> timeInYearsToRates = new TreeMap<Double, Double>();
    for(NewFixedIncomeStrip strip : getDefinition().getStrips()) {
      ValueRequirement stripRequirement = new ValueRequirement(ValueRequirementNames.MARKET_DATA_HEADER, strip.getMarketDataSpecification());
      FudgeFieldContainer fieldContainer = (FudgeFieldContainer) inputs.getValue(stripRequirement);
      Double price = fieldContainer.getDouble(MarketDataComputedValue.INDICATIVE_VALUE_NAME);
      timeInYearsToRates.put(strip.getNumYears(), price);
    }
    // Bootstrap the discount curve
    DiscountCurve discountCurve = new InterpolatedDiscountCurve(timeInYearsToRates, _interpolator);
    // Prepare results
    NewComputedValue resultValue = new NewComputedValue(_result, discountCurve);
    return Collections.singleton(resultValue);
  }

}
