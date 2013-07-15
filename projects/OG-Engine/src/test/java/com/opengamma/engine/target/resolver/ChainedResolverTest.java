/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.target.resolver;

import static org.testng.Assert.assertEquals;

import org.mockito.Mockito;
import org.testng.annotations.Test;

import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;

/**
 * Test the {@link ChainedResolver} class
 */
@Test(groups = TestGroup.UNIT)
public class ChainedResolverTest {

  @SuppressWarnings("unchecked")
  public void testFirst() {
    final ObjectResolver first = Mockito.mock(ObjectResolver.class);
    final ObjectResolver second = Mockito.mock(ObjectResolver.class);
    final ObjectResolver chained = ChainedResolver.CREATE.execute(second, first);
    Mockito.when(first.resolveObject(UniqueId.of("Foo", "1"), VersionCorrection.LATEST)).thenReturn(Currency.USD);
    Mockito.when(second.resolveObject(UniqueId.of("Foo", "1"), VersionCorrection.LATEST)).thenReturn(Currency.GBP);
    assertEquals(chained.resolveObject(UniqueId.of("Foo", "1"), VersionCorrection.LATEST), Currency.USD);
    Mockito.verify(second, Mockito.only()).isDeepResolver();
  }

  @SuppressWarnings("unchecked")
  public void testSecond() {
    final ObjectResolver first = Mockito.mock(ObjectResolver.class);
    final ObjectResolver second = Mockito.mock(ObjectResolver.class);
    final ObjectResolver chained = ChainedResolver.CREATE.execute(second, first);
    Mockito.when(first.resolveObject(UniqueId.of("Foo", "1"), VersionCorrection.LATEST)).thenReturn(null);
    Mockito.when(second.resolveObject(UniqueId.of("Foo", "1"), VersionCorrection.LATEST)).thenReturn(Currency.GBP);
    assertEquals(chained.resolveObject(UniqueId.of("Foo", "1"), VersionCorrection.LATEST), Currency.GBP);
  }

}
