/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.marketdata;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import org.testng.annotations.Test;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.marketdata.OverrideOperation;
import com.opengamma.engine.test.MockSecuritySource;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.financial.security.equity.EquitySecurity;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.id.UniqueId;

/**
 * Tests the {@link MarketDataELCompiler} class.
 */
@Test
public class MarketDataELCompilerTest {

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
    final EquitySecurity sec1 = new EquitySecurity();
    sec1.setSecurityType("EQUITY");
    sec1.addExternalId(ExternalId.of("Test", "Equity"));
    securities.addSecurity(sec1);
    final SwapSecurity sec2 = new SwapSecurity();
    sec2.addExternalId(ExternalId.of("Test", "Swap"));
    sec2.setSecurityType("SWAP");
    securities.addSecurity(sec2);
    final MarketDataELCompiler compiler = new MarketDataELCompiler(securities);
    final OverrideOperation operation = compiler.compile("if (security.type == \"EQUITY\") x * 0.9");
    assertNotNull(operation);
    Object result = operation.apply(new ValueRequirement("Foo", new ComputationTargetSpecification(sec1)), 42d);
    assertEquals (result, 42d * 0.9);
    result = operation.apply(new ValueRequirement("Foo", new ComputationTargetSpecification(sec2)), 42d);
    assertEquals (result, 42d);
  }

  public void testMultipleConditionalExpression() {
    final MockSecuritySource securities = new MockSecuritySource();
    final EquitySecurity sec1 = new EquitySecurity();
    sec1.setSecurityType("EQUITY");
    sec1.addExternalId(ExternalId.of("Test", "FooEquity"));
    sec1.setName("Foo");
    securities.addSecurity(sec1);
    final EquitySecurity sec2 = new EquitySecurity();
    sec2.setSecurityType("EQUITY");
    sec2.addExternalId(ExternalId.of("Test", "BarEquity"));
    sec2.setName("Bar");
    securities.addSecurity(sec2);
    final SwapSecurity sec3 = new SwapSecurity();
    sec3.setSecurityType("SWAP");
    sec3.addExternalId(ExternalId.of("Test", "Swap"));
    securities.addSecurity(sec3);
    final MarketDataELCompiler compiler = new MarketDataELCompiler(securities);
    final OverrideOperation operation = compiler
        .compile("if (security.type == \"EQUITY\" && security.name == \"Foo\") x * 0.9; if (security.type == \"EQUITY\") x * 1.1; if (security.cow == 42) x * 0");
    assertNotNull(operation);
    // First rule should match
    Object result = operation.apply(new ValueRequirement("Foo", new ComputationTargetSpecification(sec1)), 42d);
    assertEquals (result, 42d * 0.9);
    // Second rule should match
    result = operation.apply(new ValueRequirement("Foo", new ComputationTargetSpecification(sec2)), 42d);
    assertEquals(result, 42d * 1.1);
    // Third rule won't match but won't throw an error
    result = operation.apply(new ValueRequirement("Foo", new ComputationTargetSpecification(sec3)), 42d);
    assertEquals(result, 42d);
  }

}
