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
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.Currency;
import com.opengamma.financial.OpenGammaCompilationContext;
import com.opengamma.financial.model.interestrate.curve.InterpolatedDiscountCurve;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.livedata.normalization.MarketDataFieldNames;
import com.opengamma.math.interpolation.Interpolator1D;
import com.opengamma.math.interpolation.Interpolator1DFactory;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class DiscountCurveFunction
  extends AbstractFunction 
  implements FunctionInvoker {
  
  @SuppressWarnings("unchecked")
  private Interpolator1D _interpolator;
  private DiscountCurveDefinition _definition;
  private Set<ValueRequirement> _requirements;
  private ValueSpecification _result;
  private Set<ValueSpecification> _results;
  private Currency _curveCurrency;
  private String _curveName;
  
  public DiscountCurveFunction(Currency currency, String name) {
    ArgumentChecker.notNull(currency, "Currency");
    ArgumentChecker.notNull(name, "Name");
    _definition = null;
    _curveCurrency = currency;
    _curveName = name;
    _interpolator = null;
    _requirements = null;
    _result = null;
    _results = null;
  }

  @Override
  public void init(FunctionCompilationContext context) {
    DiscountCurveSource curveSource = OpenGammaCompilationContext.getDiscountCurveSource(context);
    _definition = curveSource.getDefinition(_curveCurrency, _curveName);
    _interpolator = Interpolator1DFactory.getInterpolator(_definition.getInterpolatorName());
    _requirements = Collections.unmodifiableSet(buildRequirements(_definition));
    _result = new ValueSpecification(new ValueRequirement(ValueRequirementNames.DISCOUNT_CURVE, _definition.getCurrency()));
    _results = Collections.singleton(_result);
  }
  
  public static Set<ValueRequirement> buildRequirements(DiscountCurveDefinition definition) {
    Set<ValueRequirement> result = new HashSet<ValueRequirement>();
    for (FixedIncomeStrip strip : definition.getStrips()) {
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
    if (target.getType() != ComputationTargetType.PRIMITIVE) {
      return false;
    }
    return ObjectUtils.equals(
        target.getUniqueIdentifier(),
        getDefinition().getCurrency().getUniqueIdentifier());
  }

  @Override
  public Set<ValueRequirement> getRequirements(FunctionCompilationContext context, ComputationTarget target) {
    if (canApplyTo(context, target)) {
      return _requirements;
    }
    return null;
  }

  @Override
  public Set<ValueSpecification> getResults(FunctionCompilationContext context, ComputationTarget target) {
    if (canApplyTo(context, target)) {
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
    return _curveCurrency + "-" + _curveName + " Discount Curve";
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.PRIMITIVE;
  }

  @Override
  public Set<ComputedValue> execute(
      FunctionExecutionContext executionContext,
      FunctionInputs inputs,
      ComputationTarget target,
      Set<ValueRequirement> desiredValues) {
    // Gather market data rates
    // Note that this assumes that all strips are priced in decimal percent. We need to resolve
    // that ultimately in OG-LiveData normalization and pull out the OGRate key rather than
    // the crazy IndicativeValue name.
    Map<Double, Double> timeInYearsToRates = new TreeMap<Double, Double>();
    // Always start with 0 at the 0 point for super-short-end expiry options.
    timeInYearsToRates.put(0., 0.);
    for (FixedIncomeStrip strip : getDefinition().getStrips()) {
      ValueRequirement stripRequirement = new ValueRequirement(ValueRequirementNames.MARKET_DATA_HEADER, strip.getMarketDataSpecification());
      FudgeFieldContainer fieldContainer = (FudgeFieldContainer) inputs.getValue(stripRequirement);
      Double price = fieldContainer.getDouble(MarketDataFieldNames.INDICATIVE_VALUE_FIELD);
      timeInYearsToRates.put(strip.getNumYears(), price);
    }
    // Bootstrap the discount curve
    @SuppressWarnings("unchecked")
    YieldAndDiscountCurve discountCurve = new InterpolatedDiscountCurve(timeInYearsToRates, _interpolator);
    // Prepare results
    ComputedValue resultValue = new ComputedValue(_result, discountCurve);
    return Collections.singleton(resultValue);
  }

}
