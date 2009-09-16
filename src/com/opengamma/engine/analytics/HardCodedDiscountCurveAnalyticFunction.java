/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.analytics;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Currency;
import java.util.HashMap;
import java.util.Map;

import com.opengamma.engine.position.Position;
import com.opengamma.engine.security.Security;
import com.opengamma.financial.model.interestrate.curve.DiscountCurve;
import com.opengamma.math.interpolation.Interpolator1D;
import com.opengamma.math.interpolation.LinearInterpolator1D;
import com.opengamma.util.DateUtil;
import com.opengamma.util.KeyValuePair;

/**
 * 
 *
 * @author jim
 */
public class HardCodedDiscountCurveAnalyticFunction implements AnalyticFunction {

    private static final String PRICE_FIELD_NAME = "PRICE";
  private static Map<String, Double> _securities = new HashMap<String, Double>();
  private static AnalyticValueDefinitionImpl s_definition;
  private static final Interpolator1D s_interpolator = new LinearInterpolator1D(); 
  private static final double ONEYEAR = 365.25;
  static {
    _securities.put("US1D", 1/ONEYEAR);
    _securities.put("US2D", 2/ONEYEAR);
    _securities.put("US7D", 7/ONEYEAR);
    _securities.put("US1M", 1/12.0);
    _securities.put("US3M", 0.25);
    _securities.put("US6M", 0.5);

    _securities.put("USSW1", 1.0);
    _securities.put("USSW2", 2.0);
    _securities.put("USSW3", 3.0);
    _securities.put("USSW4", 4.0);
    _securities.put("USSW5", 5.0);
    _securities.put("USSW6", 6.0);
    _securities.put("USSW7", 7.0);
    _securities.put("USSW8", 8.0);
    _securities.put("USSW9", 9.0);
    _securities.put("USSW10", 10.0);
    
    Map<String, String> definitionMap = new HashMap<String, String>(); 
    for (String security : _securities.keySet()) {
      definitionMap.put(security, PRICE_FIELD_NAME);
    }
    s_definition = new AnalyticValueDefinitionImpl(definitionMap);
  }
  @Override
  public Collection<AnalyticValue> execute(Collection<AnalyticValue> inputs,
      Position position) {
    throw new UnsupportedOperationException();
  }

  @SuppressWarnings("unchecked")
  @Override
  public Collection<AnalyticValue> execute(Collection<AnalyticValue> inputs,
      Security security) {
    Map<Double, Double> timeInYearsToRates = new HashMap<Double, Double>();
    for (AnalyticValue analyticValue : inputs) {
      KeyValuePair<String, Map<String, Double>> value = (KeyValuePair<String, Map<String, Double>>) analyticValue.getValue();
      double years = _securities.get(value.getKey());
      timeInYearsToRates.put(years, value.getValue().get(PRICE_FIELD_NAME));
    }
    DiscountCurve discountCurve = new DiscountCurve(DateUtil.today(), timeInYearsToRates, s_interpolator);

    return Collections.<AnalyticValue>singleton(new DiscountCurveAnalyticValue(getDiscountCurveValueDefinition(), discountCurve));
  }
  
  private AnalyticValueDefinition getDiscountCurveValueDefinition() {
    Map<String, Object> map = new HashMap<String, Object>();
    map.putAll(_securities);
    map.put("Currency", Currency.getInstance("USD"));
    return new AnalyticValueDefinitionImpl(map);
  }

  @Override
  public Collection<AnalyticValueDefinition> getInputs(Security security) {
    return Arrays.<AnalyticValueDefinition>asList(s_definition);
  }

  @Override
  public Collection<AnalyticValueDefinition> getPossibleResults() {
    return Collections.<AnalyticValueDefinition>singleton(getDiscountCurveValueDefinition());
  }

  @Override
  public String getShortName() {
    return "HardCodedDiscountCurve";
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
    return true;
  }

}
