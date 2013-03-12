/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.target.resolver;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

import com.opengamma.engine.target.Primitive;
import com.opengamma.id.ExternalBundleIdentifiable;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ExternalIdentifiable;
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

  public void testPrimitiveResolver() {
    final Resolver<?> resolver = new PrimitiveResolver();
    assertEquals(resolver.resolveObject(UniqueId.of("Foo", "Bar"), VersionCorrection.LATEST), new Primitive(UniqueId.of("Foo", "Bar")));
    UniqueId uid = resolver.resolveExternalId(ExternalId.of("Foo", "Bar").toBundle(), VersionCorrection.LATEST);
    Object o = resolver.resolveObject(uid, VersionCorrection.LATEST);
    assertTrue(o instanceof ExternalIdentifiable);
    assertEquals(((ExternalIdentifiable) o).getExternalId(), ExternalId.of("Foo", "Bar"));
    uid = resolver.resolveExternalId(ExternalIdBundle.of(ExternalId.of("Foo-", "A"), ExternalId.of("Bar\\", "B")), VersionCorrection.LATEST);
    o = resolver.resolveObject(uid, VersionCorrection.LATEST);
    assertTrue(o instanceof ExternalBundleIdentifiable);
    assertEquals(((ExternalBundleIdentifiable) o).getExternalIdBundle(), ExternalIdBundle.of(ExternalId.of("Foo-", "A"), ExternalId.of("Bar\\", "B")));
  }

}
