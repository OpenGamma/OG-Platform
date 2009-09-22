/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.analytics;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.opengamma.engine.depgraph.DependencyNode;
import com.opengamma.engine.depgraph.DependencyNodeResolver;
import com.opengamma.engine.position.Position;
import com.opengamma.engine.security.Security;
import com.opengamma.financial.model.interestrate.curve.DiscountCurve;
import com.opengamma.financial.securities.Currency;
import com.opengamma.math.interpolation.Interpolator1D;
import com.opengamma.math.interpolation.LinearInterpolator1D;
import com.opengamma.util.Pair;
import com.opengamma.util.time.DateUtil;

// REVIEW kirk 2009-09-16 -- Changed name to USD as it's holding all the strips
// that are specific to USD, and can only generate one type of result definition.
// This would not be usual practice.
// REVIEW jim 2009-09-16 -- You don't say...
/**
 * 
 *
 * @author jim
 */
public class HardCodedUSDDiscountCurveAnalyticFunction implements AnalyticFunction {
  public static final String[] STRIPS = new String[] {
    "US1D", "US2D", "US7D", "US1M", "US3M", "US6M",
    "USSW1", "USSW2", "USSW3", "USSW4", "USSW5", "USSW6",
    "USSW7", "USSW8", "USSW9", "USSW10"
  };
  public static final String PRICE_FIELD_NAME = "PRICE";

  private static Map<String, Double> _securities = new HashMap<String, Double>();
  private static final List<AnalyticValueDefinition<?>> s_inputDefinitions;
  private static final Interpolator1D s_interpolator = new LinearInterpolator1D(); 
  private static final double ONEYEAR = 365.25;
  private static final AnalyticValueDefinition<DiscountCurve> s_discountCurveDefinition = constructDiscountCurveValueDefinition();
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
    
    List<AnalyticValueDefinition<?>> inputDefinitions = new ArrayList<AnalyticValueDefinition<?>>();
    for (String security : _securities.keySet()) {
      inputDefinitions.add(constructDefinition(security));
    }
    s_inputDefinitions = Collections.<AnalyticValueDefinition<?>>unmodifiableList(inputDefinitions);
  }
  
  public static AnalyticValueDefinition<Map<String, Double>> constructDefinition(String bbTicker) {
    @SuppressWarnings("unchecked")
    AnalyticValueDefinitionImpl<Map<String, Double>> definition = new AnalyticValueDefinitionImpl<Map<String, Double>>(
        new Pair<String, Object>("DATA_SOURCE", "BLOOMBERG"),
        new Pair<String, Object>("TYPE", "MARKET_DATA_HEADER"),
        new Pair<String, Object>("BB_TICKER", bbTicker)
        );
    return definition;
  }
  
  @Override
  public Collection<AnalyticValue<?>> execute(AnalyticFunctionInputs inputs,
      Position position) {
    throw new UnsupportedOperationException();
  }
  
  @SuppressWarnings("unchecked")
  @Override
  public Collection<AnalyticValue<?>> execute(AnalyticFunctionInputs inputs,
      Security security) {
    Map<Double, Double> timeInYearsToRates = new HashMap<Double, Double>();
    for(Map.Entry<String, Double> secEntry : _securities.entrySet()) {
      String ticker = secEntry.getKey();
      Map<String, Double> dataFields = (Map<String, Double>)inputs.getValue("BB_TICKER", ticker);
      Double price = dataFields.get(PRICE_FIELD_NAME);
      
      double years = _securities.get(ticker);
      timeInYearsToRates.put(years, price);
    }
    DiscountCurve discountCurve = new DiscountCurve(DateUtil.today(), timeInYearsToRates, s_interpolator);

    return Collections.<AnalyticValue<?>>singleton(new DiscountCurveAnalyticValue(getDiscountCurveValueDefinition(), discountCurve));
  }
  
  private static AnalyticValueDefinition<DiscountCurve> constructDiscountCurveValueDefinition() {
    Map<String, Object> map = new HashMap<String, Object>();
    map.put("Currency", Currency.getInstance("USD"));
    map.put("TYPE", "DISCOUNT_CURVE");
    return new AnalyticValueDefinitionImpl<DiscountCurve>(map);
  }

  @Override
  public Collection<AnalyticValueDefinition<?>> getInputs(Security security) {
    return s_inputDefinitions;
  }

  @Override
  public Collection<AnalyticValueDefinition<?>> getPossibleResults() {
    return Collections.<AnalyticValueDefinition<?>>singleton(getDiscountCurveValueDefinition());
  }

  public static AnalyticValueDefinition<DiscountCurve> getDiscountCurveValueDefinition() {
    return s_discountCurveDefinition;
  }

  @Override
  public String getShortName() {
    return "HardCodedUSDDiscountCurve";
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

  @Override
  public DependencyNode buildSubGraph(Security security,
      AnalyticFunctionResolver functionResolver,
      DependencyNodeResolver dependencyNodeResolver) {
    throw new UnsupportedOperationException("Does not build own sub graph.");
  }

  @Override
  public boolean buildsOwnSubGraph() {
    return false;
  }

}
