/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.yc;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.fudgemsg.FudgeFieldContainer;

import com.opengamma.engine.analytics.AbstractAnalyticFunction;
import com.opengamma.engine.analytics.AnalyticFunctionInputs;
import com.opengamma.engine.analytics.AnalyticValue;
import com.opengamma.engine.analytics.AnalyticValueDefinition;
import com.opengamma.engine.analytics.FunctionExecutionContext;
import com.opengamma.engine.analytics.MarketDataAnalyticValue;
import com.opengamma.engine.analytics.PrimitiveAnalyticFunctionDefinition;
import com.opengamma.engine.analytics.PrimitiveAnalyticFunctionInvoker;
import com.opengamma.financial.analytics.DiscountCurveAnalyticValue;
import com.opengamma.financial.analytics.DiscountCurveValueDefinition;
import com.opengamma.financial.model.interestrate.curve.DiscountCurve;
import com.opengamma.financial.securities.Currency;
import com.opengamma.math.interpolation.Interpolator1D;
import com.opengamma.math.interpolation.LinearInterpolator1D;

/**
 * 
 *
 * @author kirk
 */
public class DiscountCurveAnalyticFunction extends AbstractAnalyticFunction
implements PrimitiveAnalyticFunctionDefinition, PrimitiveAnalyticFunctionInvoker {
  public static final String PRICE_FIELD_NAME = "PRICE";
  private static final Interpolator1D s_interpolator = new LinearInterpolator1D(); 
  
  private final DiscountCurveDefinition _definition;
  private final AnalyticValueDefinition<DiscountCurve> _discountCurveDefinition;
  private final Set<AnalyticValueDefinition<?>> _inputs;
  
  public DiscountCurveAnalyticFunction(DiscountCurveDefinition definition) {
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
  public Collection<AnalyticValue<?>> execute(
      FunctionExecutionContext executionContext, AnalyticFunctionInputs inputs) {
    Map<Double, Double> timeInYearsToRates = new HashMap<Double, Double>();
    for(FixedIncomeStrip strip : getDefinition().getStrips()) {
      FudgeFieldContainer fieldContainer = (FudgeFieldContainer)inputs.getValue(strip.getStripValueDefinition());
      Double price = fieldContainer.getDouble(MarketDataAnalyticValue.INDICATIVE_VALUE_NAME);
      timeInYearsToRates.put(strip.getNumYears(), price);
    }
    DiscountCurve discountCurve = new DiscountCurve(timeInYearsToRates, s_interpolator);

    return Collections.<AnalyticValue<?>>singleton(new DiscountCurveAnalyticValue(getDiscountCurveValueDefinition(), discountCurve));
  }

  @Override
  public String getShortName() {
    return "" + getDefinition().getCurrency().getISOCode() + "-" + getDefinition().getCurrency() + " Discount Curve Builder";
  }
}
