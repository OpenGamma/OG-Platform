/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.marketdata;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.ZoneId;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.DefaultComputationTargetResolver;
import com.opengamma.engine.InMemorySecuritySource;
import com.opengamma.engine.marketdata.OverrideOperation;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.daycount.DayCounts;
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
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.Expiry;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
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
    final SwapLeg swapLeg = new FixedInterestRateLeg(DayCounts.ACT_365, SimpleFrequency.SEMI_ANNUAL,
        ExternalId.of("Financial", "US"), BusinessDayConventions.FOLLOWING, new InterestRateNotional(Currency.USD, 10e6), false, 0.01);
    _swap = new SwapSecurity(ZonedDateTime.now(), ZonedDateTime.now(), ZonedDateTime.now(), "counterParty", swapLeg, swapLeg);
    _swap.addExternalId(ExternalId.of("Test", "Swap"));
  }

  @Test(expectedExceptions = {IllegalArgumentException.class })
  public void testInvalidExpression() {
    final MarketDataELCompiler compiler = new MarketDataELCompiler();
    compiler.compile("Not a valid expression", new DefaultComputationTargetResolver().atVersionCorrection(VersionCorrection.LATEST));
  }

  public void testConstantMultiplier() {
    final MarketDataELCompiler compiler = new MarketDataELCompiler();
    final OverrideOperation operation = compiler.compile("x * 0.9", new DefaultComputationTargetResolver().atVersionCorrection(VersionCorrection.LATEST));
    assertNotNull(operation);
    final ValueRequirement req = new ValueRequirement("Foo", ComputationTargetSpecification.of(UniqueId.of("Test", "Bar")));
    final Object result = operation.apply(req, 42d);
    assertEquals(result, 42d * 0.9);
  }

  public void testConstantAddition() {
    final MarketDataELCompiler compiler = new MarketDataELCompiler();
    final OverrideOperation operation = compiler.compile("x + 0.42", new DefaultComputationTargetResolver().atVersionCorrection(VersionCorrection.LATEST));
    assertNotNull(operation);
    final ValueRequirement req = new ValueRequirement("Foo", ComputationTargetSpecification.of(UniqueId.of("Test", "Bar")));
    final Object result = operation.apply(req, 0.9d);
    assertEquals(result, 0.9 + 0.42);
  }

  public void testConditionalExpression() {
    final InMemorySecuritySource securities = new InMemorySecuritySource();
    securities.addSecurity(_fooEquity);
    securities.addSecurity(_swap);
    final MarketDataELCompiler compiler = new MarketDataELCompiler();
    final OverrideOperation operation = compiler.compile("if (security.type == \"EQUITY\") x * 0.9", new DefaultComputationTargetResolver(securities).atVersionCorrection(VersionCorrection.LATEST));
    assertNotNull(operation);
    Object result = operation.apply(new ValueRequirement("Foo", ComputationTargetSpecification.of(_fooEquity)), 42d);
    assertEquals (result, 42d * 0.9);
    result = operation.apply(new ValueRequirement("Foo", ComputationTargetSpecification.of(_swap)), 42d);
    assertEquals (result, 42d);
  }

  public void testMultipleConditionalExpression() {
    final InMemorySecuritySource securities = new InMemorySecuritySource();
    securities.addSecurity(_fooEquity);
    securities.addSecurity(_barEquity);
    securities.addSecurity(_swap);
    final MarketDataELCompiler compiler = new MarketDataELCompiler();
    final OverrideOperation operation = compiler.compile(
        "if (security.type == \"EQUITY\" && security.name == \"Foo\") x * 0.9; if (security.type == \"EQUITY\") x * 1.1; if (security.cow == 42) x * 0", new DefaultComputationTargetResolver(
            securities).atVersionCorrection(VersionCorrection.LATEST));
    assertNotNull(operation);
    // First rule should match
    Object result = operation.apply(new ValueRequirement("Foo", ComputationTargetSpecification.of(_fooEquity)), 42d);
    assertEquals (result, 42d * 0.9);
    // Second rule should match
    result = operation.apply(new ValueRequirement("Foo", ComputationTargetSpecification.of(_barEquity)), 42d);
    assertEquals(result, 42d * 1.1);
    // Third rule won't match but won't throw an error
    result = operation.apply(new ValueRequirement("Foo", ComputationTargetSpecification.of(_swap)), 42d);
    assertEquals(result, 42d);
  }

  public void testValueExpression() {
    final MarketDataELCompiler compiler = new MarketDataELCompiler();
    final Object result = compiler.compile("value", new DefaultComputationTargetResolver().atVersionCorrection(VersionCorrection.LATEST)).apply(
        new ValueRequirement("Foo", ComputationTargetSpecification.of(Currency.USD)), null);
    assertEquals(result, "Foo");
  }

  public void testUnderlyingExpression () {
    final InMemorySecuritySource securities = new InMemorySecuritySource();
    securities.addSecurity(_fooEquity);
    final EquityOptionSecurity fooOption = new EquityOptionSecurity (OptionType.PUT, 10d, Currency.USD, ExternalId.of("Test", "FooEquity"),
        new AmericanExerciseType(), new Expiry(zdt(2020, 11, 25, 12, 0, 0, 0, ZoneOffset.UTC)), 42d, "EXCH");
    fooOption.addExternalId(ExternalId.of("Test", "FooOption"));
    securities.addSecurity(fooOption);
    final MarketDataELCompiler compiler = new MarketDataELCompiler();
    Object result = compiler.compile("security.underlyingId", new DefaultComputationTargetResolver(securities).atVersionCorrection(VersionCorrection.LATEST)).apply(
        new ValueRequirement("Foo", ComputationTargetSpecification.of(fooOption)), null);
    assertEquals(result, ExternalId.of("Test", "FooEquity"));
    result = compiler.compile("Security:get(security.underlyingId)", new DefaultComputationTargetResolver(securities).atVersionCorrection(VersionCorrection.LATEST)).apply(
        new ValueRequirement("Foo", ComputationTargetSpecification.of(fooOption)), null);
    assertEquals(result, _fooEquity);
  }

  //-------------------------------------------------------------------------
  private static ZonedDateTime zdt(final int y, final int m, final int d, final int hr, final int min, final int sec, final int nanos, final ZoneId zone) {
    return LocalDateTime.of(y, m, d, hr, min, sec, nanos).atZone(zone);
  }

}
