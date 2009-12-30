/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.ircurve;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.NewPrimitiveFunctionDefinition;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.Currency;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 *
 * @author kirk
 */
public class NewDiscountCurveFunction
extends AbstractFunction 
implements NewPrimitiveFunctionDefinition {
  private final NewDiscountCurveDefinition _definition;
  private final Set<ValueRequirement> _requirements;
  private final Set<ValueSpecification> _results;
  
  public NewDiscountCurveFunction(NewDiscountCurveDefinition definition) {
    ArgumentChecker.checkNotNull(definition, "Discount Curve Definition");
    _definition = definition;
    
    _requirements = Collections.unmodifiableSet(buildRequirements(_definition));
    ValueSpecification result = new ValueSpecification(new ValueRequirement(ValueRequirementNames.DISCOUNT_CURVE, ComputationTargetType.PRIMITIVE, _definition.getCurrency().getISOCode()));
    _results = Collections.singleton(result);
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
  public boolean canApplyTo(Object primitiveKey) {
    if(primitiveKey instanceof Currency) {
      Currency currencyKey = (Currency) primitiveKey;
      return ObjectUtils.equals(currencyKey, getDefinition().getCurrency());
    }
    return false;
  }

  @Override
  public Set<ValueRequirement> getRequirements(Object primitiveKey) {
    if(canApplyTo(primitiveKey)) {
      return _requirements;
    }
    return null;
  }

  @Override
  public Set<ValueSpecification> getResults(Object primitiveKey) {
    if(canApplyTo(primitiveKey)) {
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

}
