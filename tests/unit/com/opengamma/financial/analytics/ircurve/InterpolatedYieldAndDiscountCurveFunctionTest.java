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

import javax.time.calendar.Period;

import org.junit.Test;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.position.PortfolioNodeImpl;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.financial.Currency;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.livedata.normalization.MarketDataRequirementNames;
import com.opengamma.math.interpolation.Interpolator1DFactory;

/**
 * Test InterpolatedYieldAndDiscountCurveFunction.
 */
public class InterpolatedYieldAndDiscountCurveFunctionTest {

  protected static InterpolatedYieldAndDiscountCurveDefinition constructDefinition() {
    Currency currency = Currency.getInstance("USD");
    String name = "Test Curve";
    InterpolatedYieldAndDiscountCurveDefinition definition = new InterpolatedYieldAndDiscountCurveDefinition(currency, name, Interpolator1DFactory.LINEAR);
    definition.addStrip(new FixedIncomeStrip(Period.ofYears(1), UniqueIdentifier.of("Test", "USSW1 Curncy"), StripInstrument.SWAP));
    definition.addStrip(new FixedIncomeStrip(Period.ofYears(2), UniqueIdentifier.of("Test", "USSW2 Curncy"), StripInstrument.SWAP));
    definition.addStrip(new FixedIncomeStrip(Period.ofYears(3), UniqueIdentifier.of("Test", "USSW3 Curncy"), StripInstrument.SWAP));
    return definition;
  }

  @Test
  public void discountCurveRequirements() {
    InterpolatedYieldAndDiscountCurveDefinition definition = constructDefinition();
    DefaultInterpolatedYieldAndDiscountCurveSource curveSource = new DefaultInterpolatedYieldAndDiscountCurveSource();
    curveSource.addDefinition(Currency.getInstance("USD"), "DEFAULT", definition);
    SimpleInterpolatedYieldAndDiscountCurveFunction function = new SimpleInterpolatedYieldAndDiscountCurveFunction(Currency
        .getInstance("USD"), "DEFAULT", false);
    function.setUniqueIdentifier("testId");
    Set<ValueRequirement> requirements = null;
    FunctionCompilationContext context = new FunctionCompilationContext();
    context.put("discountCurveSource", curveSource);
    
    function.init(context);
    
    requirements = function.getRequirements(context, new ComputationTarget(ComputationTargetType.PRIMITIVE, Currency.getInstance("USD")));
    assertNotNull(requirements);
    assertEquals(3, requirements.size());
    Set<UniqueIdentifier> foundKeys = new TreeSet<UniqueIdentifier>();
    for (ValueRequirement requirement : requirements) {
      assertNotNull(requirement);
      assertEquals(MarketDataRequirementNames.MARKET_VALUE, requirement.getValueName());
      assertNotNull(requirement.getTargetSpecification());
      assertEquals(ComputationTargetType.PRIMITIVE, requirement.getTargetSpecification().getType());
      foundKeys.add(requirement.getTargetSpecification().getUniqueIdentifier());
    }
    assertEquals(3, foundKeys.size());
    
    for (FixedIncomeStrip strip : definition.getStrips()) {
      assertTrue(foundKeys.contains(strip.getMarketDataKey()));
    }
  }

  @Test
  public void yieldCurveRequirements() {
    InterpolatedYieldAndDiscountCurveDefinition definition = constructDefinition();
    DefaultInterpolatedYieldAndDiscountCurveSource curveSource = new DefaultInterpolatedYieldAndDiscountCurveSource();
    curveSource.addDefinition(Currency.getInstance("USD"), "DEFAULT", definition);
    SimpleInterpolatedYieldAndDiscountCurveFunction function = new SimpleInterpolatedYieldAndDiscountCurveFunction(Currency
        .getInstance("USD"), "DEFAULT", true);
    function.setUniqueIdentifier("testId");
    Set<ValueRequirement> requirements = null;
    FunctionCompilationContext context = new FunctionCompilationContext();
    context.put("discountCurveSource", curveSource);

    function.init(context);

    requirements = function.getRequirements(context, new ComputationTarget(ComputationTargetType.PRIMITIVE, Currency
        .getInstance("USD")));
    assertNotNull(requirements);
    assertEquals(3, requirements.size());
    Set<UniqueIdentifier> foundKeys = new TreeSet<UniqueIdentifier>();
    for (ValueRequirement requirement : requirements) {
      assertNotNull(requirement);
      assertEquals(MarketDataRequirementNames.MARKET_VALUE, requirement.getValueName());
      assertNotNull(requirement.getTargetSpecification());
      assertEquals(ComputationTargetType.PRIMITIVE, requirement.getTargetSpecification().getType());
      foundKeys.add(requirement.getTargetSpecification().getUniqueIdentifier());
    }
    assertEquals(3, foundKeys.size());

    for (FixedIncomeStrip strip : definition.getStrips()) {
      assertTrue(foundKeys.contains(strip.getMarketDataKey()));
    }
  }

  @Test
  public void discountCurveNotMatchingRequirements() {
    InterpolatedYieldAndDiscountCurveDefinition definition = constructDefinition();
    DefaultInterpolatedYieldAndDiscountCurveSource curveSource = new DefaultInterpolatedYieldAndDiscountCurveSource();
    curveSource.addDefinition(Currency.getInstance("USD"), "DEFAULT", definition);
    SimpleInterpolatedYieldAndDiscountCurveFunction function = new SimpleInterpolatedYieldAndDiscountCurveFunction(Currency
        .getInstance("USD"), "DEFAULT", false);
    function.setUniqueIdentifier("testId");
    Set<ValueRequirement> requirements = null;
    FunctionCompilationContext context = new FunctionCompilationContext();
    context.put("discountCurveSource", curveSource);
    
    function.init(context);
    
    requirements = function.getRequirements(context, new ComputationTarget(ComputationTargetType.PRIMITIVE, Currency.getInstance("EUR")));
    assertNull(requirements);
    
    requirements = function.getRequirements(context, new ComputationTarget(ComputationTargetType.PORTFOLIO_NODE, new PortfolioNodeImpl()));
    assertNull(requirements);
  }

  @Test
  public void yieldCurveNotMatchingRequirements() {
    InterpolatedYieldAndDiscountCurveDefinition definition = constructDefinition();
    DefaultInterpolatedYieldAndDiscountCurveSource curveSource = new DefaultInterpolatedYieldAndDiscountCurveSource();
    curveSource.addDefinition(Currency.getInstance("USD"), "DEFAULT", definition);
    SimpleInterpolatedYieldAndDiscountCurveFunction function = new SimpleInterpolatedYieldAndDiscountCurveFunction(Currency
        .getInstance("USD"), "DEFAULT", true);
    function.setUniqueIdentifier("testId");
    Set<ValueRequirement> requirements = null;
    FunctionCompilationContext context = new FunctionCompilationContext();
    context.put("discountCurveSource", curveSource);

    function.init(context);

    requirements = function.getRequirements(context, new ComputationTarget(ComputationTargetType.PRIMITIVE, Currency
        .getInstance("EUR")));
    assertNull(requirements);

    requirements = function.getRequirements(context, new ComputationTarget(ComputationTargetType.PORTFOLIO_NODE,
        new PortfolioNodeImpl()));
    assertNull(requirements);
  }

}
