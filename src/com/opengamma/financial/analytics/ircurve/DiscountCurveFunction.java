/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.ircurve;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.fudgemsg.FudgeFieldContainer;

import com.opengamma.engine.function.AbstractPrimitiveFunction;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.function.PrimitiveFunctionDefinition;
import com.opengamma.engine.function.PrimitiveFunctionInvoker;
import com.opengamma.engine.value.AnalyticValueDefinition;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.MarketDataComputedValue;
import com.opengamma.financial.Currency;
import com.opengamma.financial.analytics.DiscountCurveComputedValue;
import com.opengamma.financial.analytics.DiscountCurveValueDefinition;
import com.opengamma.financial.model.interestrate.curve.DiscountCurve;
import com.opengamma.financial.model.interestrate.curve.InterpolatedDiscountCurve;
import com.opengamma.math.interpolation.Interpolator1D;
import com.opengamma.math.interpolation.LinearInterpolator1D;

/**
 * 
 *
 * @author kirk
 */
public class DiscountCurveFunction extends AbstractPrimitiveFunction
implements PrimitiveFunctionDefinition, PrimitiveFunctionInvoker {
  public static final String PRICE_FIELD_NAME = "PRICE";
  private static final Interpolator1D s_interpolator = new LinearInterpolator1D(); 
  
  private final DiscountCurveDefinition _definition;
  private final AnalyticValueDefinition<DiscountCurve> _discountCurveDefinition;
  private final Set<AnalyticValueDefinition<?>> _inputs;
  
  public DiscountCurveFunction(DiscountCurveDefinition definition) {
    _definition = definition;
    _discountCurveDefinition = constructDiscountCurveValueDefinition(definition.getCurrency());
    _inputs = definition.getRequiredInputs();
  }

  /**
   * @return the definition
   */
  public DiscountCurveDefinition getDefinition() {
    return _definition;
  }

  public AnalyticValueDefinition<DiscountCurve> getDiscountCurveValueDefinition() {
    return _discountCurveDefinition;
  }

  public static AnalyticValueDefinition<DiscountCurve> constructDiscountCurveValueDefinition(Currency currency) {
    if(currency == null) {
      return new DiscountCurveValueDefinition();
    }
    return new DiscountCurveValueDefinition(currency);
  }

  @Override
  public Collection<AnalyticValueDefinition<?>> getInputs() {
    return _inputs;
  }

  @Override
  public Collection<AnalyticValueDefinition<?>> getPossibleResults() {
    return Collections.<AnalyticValueDefinition<?>>singleton(getDiscountCurveValueDefinition());
  }

  @Override
  public Collection<ComputedValue<?>> execute(
      FunctionExecutionContext executionContext, FunctionInputs inputs) {
    Map<Double, Double> timeInYearsToRates = new HashMap<Double, Double>();
    for(FixedIncomeStrip strip : getDefinition().getStrips()) {
      FudgeFieldContainer fieldContainer = (FudgeFieldContainer)inputs.getValue(strip.getStripValueDefinition());
      Double price = fieldContainer.getDouble(MarketDataComputedValue.INDICATIVE_VALUE_NAME);
      timeInYearsToRates.put(strip.getNumYears(), price);
    }
    DiscountCurve discountCurve = new InterpolatedDiscountCurve(timeInYearsToRates, s_interpolator);

    return Collections.<ComputedValue<?>>singleton(new DiscountCurveComputedValue(getDiscountCurveValueDefinition(), discountCurve));
  }

  @Override
  public String getShortName() {
    return "" + getDefinition().getCurrency().getISOCode() + "-" + getDefinition().getCurrency() + " Discount Curve Builder";
  }
}
