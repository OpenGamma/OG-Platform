/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.ircurve;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Set;
import java.util.TreeSet;

import org.junit.Test;

import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.financial.Currency;

/**
 * 
 *
 * @author kirk
 */
public class NewDiscountCurveFunctionTest {
  
  protected static NewDiscountCurveDefinition constructDefinition() {
    Currency currency = Currency.getInstance("USD");
    String name = "Test Curve";
    NewDiscountCurveDefinition definition = new NewDiscountCurveDefinition(currency, name);
    definition.addStrip(new NewFixedIncomeStrip(1, "USSW1 Curncy"));
    definition.addStrip(new NewFixedIncomeStrip(2, "USSW2 Curncy"));
    definition.addStrip(new NewFixedIncomeStrip(3, "USSW3 Curncy"));
    return definition;
  }
  
  @Test
  public void requirements() {
    NewDiscountCurveDefinition definition = constructDefinition();
    NewDiscountCurveFunction function = new NewDiscountCurveFunction(definition);
    Set<ValueRequirement> requirements = null;
    requirements = function.getRequirements(Currency.getInstance("USD"));
    assertNotNull(requirements);
    assertEquals(3, requirements.size());
    Set<String> foundKeys = new TreeSet<String>();
    for(ValueRequirement requirement : requirements) {
      assertNotNull(requirement);
      assertEquals(ValueRequirementNames.MARKET_DATA_HEADER, requirement.getValueName());
      assertNotNull(requirement.getTargetSpecification());
      assertEquals(ComputationTargetType.PRIMITIVE, requirement.getTargetSpecification().getType());
      foundKeys.add(requirement.getTargetSpecification().getIdentifier());
    }
    assertEquals(3, foundKeys.size());
    
    for(NewFixedIncomeStrip strip : definition.getStrips()) {
      assertTrue(foundKeys.contains(strip.getMarketDataKey()));
    }
  }
  
  @Test
  public void notMatchingRequirements() {
    NewDiscountCurveDefinition definition = constructDefinition();
    NewDiscountCurveFunction function = new NewDiscountCurveFunction(definition);
    Set<ValueRequirement> requirements = null;
    
    requirements = function.getRequirements("USD");
    assertNull(requirements);
    
    requirements = function.getRequirements(Currency.getInstance("EUR"));
    assertNull(requirements);
  }

}
