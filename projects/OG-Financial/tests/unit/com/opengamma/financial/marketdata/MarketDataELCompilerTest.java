/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.marketdata;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import javax.time.calendar.TimeZone;
import javax.time.calendar.ZonedDateTime;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.marketdata.OverrideOperation;
import com.opengamma.engine.test.MockSecuritySource;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.convention.frequency.SimpleFrequency;
import com.opengamma.financial.security.equity.EquitySecurity;
import com.opengamma.financial.security.option.AmericanExerciseType;
import com.opengamma.financial.security.option.EquityOptionSecurity;
import com.opengamma.financial.security.option.OptionType;
import com.opengamma.financial.security.swap.FixedInterestRateLeg;
import com.opengamma.financial.security.swap.InterestRateNotional;
import com.opengamma.financial.security.swap.SwapLeg;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.id.UniqueId;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Expiry;

/**
 * Tests the {@link MarketDataELCompiler} class.
 */
@Test
public class MarketDataELCompilerTest {
  
  private EquitySecurity _fooEquity;
  private EquitySecurity _barEquity;
  private SwapSecurity _swap;
  
  @BeforeMethod
  public void setUp() throws Exception {
    _fooEquity =  new EquitySecurity("exchange", "exchangeCode", "Foo", Currency.USD);
    _fooEquity.addExternalId(ExternalId.of("Test", "FooEquity"));
    _fooEquity.setName("Foo");
    _barEquity = new EquitySecurity("exchange", "exchangeCode", "Bar", Currency.USD);
    _barEquity.addExternalId(ExternalId.of("Test", "BarEquity"));
    _barEquity.setName("Bar");
    final SwapLeg swapLeg = new FixedInterestRateLeg(DayCountFactory.INSTANCE.getDayCount("ACT/365"), SimpleFrequency.SEMI_ANNUAL,  
        ExternalId.of("Financial", "US"), BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Following"), new InterestRateNotional(Currency.USD, 10e6), false, 0.01);
    _swap = new SwapSecurity(ZonedDateTime.now(), ZonedDateTime.now(), ZonedDateTime.now(), "counterParty", swapLeg, swapLeg);
    _swap.addExternalId(ExternalId.of("Test", "Swap"));
  }

  @Test(expectedExceptions = {IllegalArgumentException.class })
  public void testInvalidExpression() {
    final MarketDataELCompiler compiler = new MarketDataELCompiler(new MockSecuritySource());
    compiler.compile("Not a valid expression");
  }

  public void testConstantMultiplier() {
    final MarketDataELCompiler compiler = new MarketDataELCompiler(new MockSecuritySource());
    final OverrideOperation operation = compiler.compile("x * 0.9");
    assertNotNull(operation);
    final ValueRequirement req = new ValueRequirement("Foo", new ComputationTargetSpecification(UniqueId.of("Test", "Bar")));
    final Object result = operation.apply(req, 42d);
    assertEquals(result, 42d * 0.9);
  }

  public void testConstantAddition() {
    final MarketDataELCompiler compiler = new MarketDataELCompiler(new MockSecuritySource());
    final OverrideOperation operation = compiler.compile("x + 0.42");
    assertNotNull(operation);
    final ValueRequirement req = new ValueRequirement("Foo", new ComputationTargetSpecification(UniqueId.of("Test", "Bar")));
    final Object result = operation.apply(req, 0.9d);
    assertEquals(result, 0.9 + 0.42);
  }

  public void testConditionalExpression() {
    final MockSecuritySource securities = new MockSecuritySource();
    securities.addSecurity(_fooEquity);
    securities.addSecurity(_swap);
    final MarketDataELCompiler compiler = new MarketDataELCompiler(securities);
    final OverrideOperation operation = compiler.compile("if (security.type == \"EQUITY\") x * 0.9");
    assertNotNull(operation);
    Object result = operation.apply(new ValueRequirement("Foo", new ComputationTargetSpecification(_fooEquity)), 42d);
    assertEquals (result, 42d * 0.9);
    result = operation.apply(new ValueRequirement("Foo", new ComputationTargetSpecification(_swap)), 42d);
    assertEquals (result, 42d);
  }

  public void testMultipleConditionalExpression() {
    final MockSecuritySource securities = new MockSecuritySource();
    securities.addSecurity(_fooEquity);
    securities.addSecurity(_barEquity);
    securities.addSecurity(_swap);
    final MarketDataELCompiler compiler = new MarketDataELCompiler(securities);
    final OverrideOperation operation = compiler
        .compile("if (security.type == \"EQUITY\" && security.name == \"Foo\") x * 0.9; if (security.type == \"EQUITY\") x * 1.1; if (security.cow == 42) x * 0");
    assertNotNull(operation);
    // First rule should match
    Object result = operation.apply(new ValueRequirement("Foo", new ComputationTargetSpecification(_fooEquity)), 42d);
    assertEquals (result, 42d * 0.9);
    // Second rule should match
    result = operation.apply(new ValueRequirement("Foo", new ComputationTargetSpecification(_barEquity)), 42d);
    assertEquals(result, 42d * 1.1);
    // Third rule won't match but won't throw an error
    result = operation.apply(new ValueRequirement("Foo", new ComputationTargetSpecification(_swap)), 42d);
    assertEquals(result, 42d);
  }

  public void testValueExpression() {
    final MockSecuritySource securities = new MockSecuritySource();
    final MarketDataELCompiler compiler = new MarketDataELCompiler(securities);
    final Object result = compiler.compile("value").apply(new ValueRequirement("Foo", new ComputationTargetSpecification("X")), null);
    assertEquals(result, "Foo");
  }

  public void testUnderlyingExpression () {
    final MockSecuritySource securities = new MockSecuritySource ();
    securities.addSecurity(_fooEquity);
    final EquityOptionSecurity fooOption = new EquityOptionSecurity (OptionType.PUT, 10d, Currency.USD, ExternalId.of("Test", "FooEquity"), new AmericanExerciseType(), new Expiry(ZonedDateTime.of(2020, 11, 25, 12, 0, 0, 0, TimeZone.UTC)), 42d, "EXCH"); 
    fooOption.addExternalId(ExternalId.of("Test", "FooOption"));
    securities.addSecurity(fooOption);
    final MarketDataELCompiler compiler = new MarketDataELCompiler(securities);
    Object result = compiler.compile("security.underlyingId").apply(new ValueRequirement("Foo", new ComputationTargetSpecification(fooOption)), null);
    assertEquals(result, ExternalId.of("Test", "FooEquity"));
    result = compiler.compile("Security:get(security.underlyingId)").apply(new ValueRequirement("Foo", new ComputationTargetSpecification(fooOption)), null);
    assertEquals(result, _fooEquity);
  }

}
