/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.analytics.yc;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.time.calendar.Clock;

import com.opengamma.engine.analytics.AnalyticFunction;
import com.opengamma.engine.analytics.AnalyticFunctionInputs;
import com.opengamma.engine.analytics.AnalyticFunctionResolver;
import com.opengamma.engine.analytics.AnalyticValue;
import com.opengamma.engine.analytics.AnalyticValueDefinition;
import com.opengamma.engine.analytics.AnalyticValueDefinitionImpl;
import com.opengamma.engine.analytics.DiscountCurveAnalyticValue;
import com.opengamma.engine.depgraph.DependencyNode;
import com.opengamma.engine.depgraph.DependencyNodeResolver;
import com.opengamma.engine.position.Position;
import com.opengamma.engine.security.Security;
import com.opengamma.financial.model.interestrate.curve.DiscountCurve;
import com.opengamma.financial.securities.Currency;
import com.opengamma.math.interpolation.Interpolator1D;
import com.opengamma.math.interpolation.LinearInterpolator1D;

/**
 * 
 *
 * @author kirk
 */
public class DiscountCurveAnalyticFunction implements AnalyticFunction {
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
    Map<String, Object> map = new HashMap<String, Object>();
    map.put("Currency", currency);
    map.put("TYPE", "DISCOUNT_CURVE");
    return new AnalyticValueDefinitionImpl<DiscountCurve>(map);
  }

  @Override
  public DependencyNode buildSubGraph(Security security,
      AnalyticFunctionResolver functionResolver,
      DependencyNodeResolver dependencyNodeResolver) {
    throw new UnsupportedOperationException("Does not build own sub graph");
  }

  @Override
  public boolean buildsOwnSubGraph() {
    return false;
  }

  @Override
  public Collection<AnalyticValue<?>> execute(AnalyticFunctionInputs inputs,
      Position position) {
    throw new UnsupportedOperationException("Cannot be applied to a position.");
  }

  @Override
  public Collection<AnalyticValue<?>> execute(
      AnalyticFunctionInputs inputs,
      Security security) {
    Map<Double, Double> timeInYearsToRates = new HashMap<Double, Double>();
    for(FixedIncomeStrip strip : getDefinition().getStrips()) {
      @SuppressWarnings("unchecked")
      Map<String,Double> dataFields = (Map<String,Double>)inputs.getValue(strip.getStripValueDefinition());
      Double price = dataFields.get(PRICE_FIELD_NAME);
      timeInYearsToRates.put(strip.getNumYears(), price);
    }
    DiscountCurve discountCurve = new DiscountCurve(Clock.systemDefaultZone().instant(), timeInYearsToRates, s_interpolator);

    return Collections.<AnalyticValue<?>>singleton(new DiscountCurveAnalyticValue(getDiscountCurveValueDefinition(), discountCurve));
  }

  @Override
  public Collection<AnalyticValueDefinition<?>> getInputs(Security security) {
    return _inputs;
  }

  @Override
  public Collection<AnalyticValueDefinition<?>> getPossibleResults() {
    return Collections.<AnalyticValueDefinition<?>>singleton(getDiscountCurveValueDefinition());
  }

  @Override
  public String getShortName() {
    return "" + getDefinition().getCurrency().getISOCode() + "-" + getDefinition().getCurrency() + " Discount Curve Builder";
  }

  @Override
  public boolean isApplicableTo(String securityType) {
    return true;
  }

  @Override
  public boolean isApplicableTo(Position position) {
    return false;
  }

  @Override
  public boolean isPositionSpecific() {
    return false;
  }

  @Override
  public boolean isSecuritySpecific() {
    return false;
  }

}
