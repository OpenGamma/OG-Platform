/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.target.resolver;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.UnorderedCurrencyPair;

/**
 * Test the primitive resolvers.
 */
@Test
public class PrimitiveResolverTest {

  public void testCurrencyResolver() {
    final ObjectResolver<Currency> resolver = new CurrencyResolver();
    assertEquals(resolver.resolveObject(Currency.GBP.getUniqueId(), VersionCorrection.LATEST), Currency.GBP);
    assertEquals(resolver.resolveObject(UniqueId.of("Foo", "Bar"), VersionCorrection.LATEST), null);
  }

  public void testUnorderedCurrencyPairResolver() {
    final ObjectResolver<UnorderedCurrencyPair> resolver = new UnorderedCurrencyPairResolver();
    assertEquals(resolver.resolveObject(UnorderedCurrencyPair.of(Currency.GBP, Currency.USD).getUniqueId(), VersionCorrection.LATEST), UnorderedCurrencyPair.of(Currency.GBP, Currency.USD));
    assertEquals(resolver.resolveObject(UniqueId.of("Foo", "Bar"), VersionCorrection.LATEST), null);
  }

}
