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

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.position.PortfolioNodeImpl;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.financial.Currency;
import com.opengamma.math.interpolation.Interpolator1DFactory;

/**
 * 
 *
 * @author kirk
 */
public class DiscountCurveFunctionTest {
  
  protected static DiscountCurveDefinition constructDefinition() {
    Currency currency = Currency.getInstance("USD");
    String name = "Test Curve";
    DiscountCurveDefinition definition = new DiscountCurveDefinition(currency, name, Interpolator1DFactory.LINEAR);
    definition.addStrip(new FixedIncomeStrip(1, "USSW1 Curncy"));
    definition.addStrip(new FixedIncomeStrip(2, "USSW2 Curncy"));
    definition.addStrip(new FixedIncomeStrip(3, "USSW3 Curncy"));
    return definition;
  }
  
  @Test
  public void requirements() {
    DiscountCurveDefinition definition = constructDefinition();
    DiscountCurveFunction function = new DiscountCurveFunction(definition);
    Set<ValueRequirement> requirements = null;
    FunctionCompilationContext context = new FunctionCompilationContext();
    requirements = function.getRequirements(context, new ComputationTarget(ComputationTargetType.PRIMITIVE, "USD"));
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
    
    for(FixedIncomeStrip strip : definition.getStrips()) {
      assertTrue(foundKeys.contains(strip.getMarketDataKey()));
    }
  }
  
  @Test
  public void notMatchingRequirements() {
    DiscountCurveDefinition definition = constructDefinition();
    DiscountCurveFunction function = new DiscountCurveFunction(definition);
    Set<ValueRequirement> requirements = null;
    
    FunctionCompilationContext context = new FunctionCompilationContext();
    
    requirements = function.getRequirements(context, new ComputationTarget(ComputationTargetType.PRIMITIVE, Currency.getInstance("USD")));
    assertNull(requirements);
    
    requirements = function.getRequirements(context, new ComputationTarget(ComputationTargetType.PRIMITIVE, "EUR"));
    assertNull(requirements);

    requirements = function.getRequirements(context, new ComputationTarget(ComputationTargetType.MULTIPLE_POSITIONS, new PortfolioNodeImpl()));
    assertNull(requirements);
  }

}
