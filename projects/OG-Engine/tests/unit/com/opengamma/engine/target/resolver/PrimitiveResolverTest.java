/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.target.resolver;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.id.UniqueId;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.UnorderedCurrencyPair;

/**
 * Test the primitive resolvers.
 */
@Test
public class PrimitiveResolverTest {

  public void testCurrencyResolver() {
    final Resolver<Currency> resolver = new CurrencyResolver();
    assertEquals(resolver.resolve(Currency.GBP.getUniqueId()), Currency.GBP);
    assertEquals(resolver.resolve(UniqueId.of("Foo", "Bar")), null);
  }

  public void testUnorderedCurrencyPairResolver() {
    final Resolver<UnorderedCurrencyPair> resolver = new UnorderedCurrencyPairResolver();
    assertEquals(resolver.resolve(UnorderedCurrencyPair.of(Currency.GBP, Currency.USD).getUniqueId()), UnorderedCurrencyPair.of(Currency.GBP, Currency.USD));
    assertEquals(resolver.resolve(UniqueId.of("Foo", "Bar")), null);
  }

}
