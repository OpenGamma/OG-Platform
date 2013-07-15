/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util;

import static org.testng.Assert.assertNotSame;
import static org.testng.Assert.assertSame;

import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;

/**
 * Tests the {@link WeakInstanceCache}.
 */
@Test(groups = TestGroup.UNIT)
public class WeakInstanceCacheTest {

  private static final class Foo {

    private final int _hash;

    private final int _value;

    public Foo(final int hash, final int value) {
      _hash = hash;
      _value = value;
    }

    @Override
    public int hashCode() {
      return _hash;
    }

    @Override
    public boolean equals(final Object o) {
      return (o instanceof Foo) && (((Foo) o)._value == _value);
    }

  }

  public void testBasicOperation() {
    final WeakInstanceCache<Foo> cache = new WeakInstanceCache<Foo>();
    final Foo a1 = new Foo(1, 1);
    final Foo a2 = new Foo(1, 2);
    final Foo b1 = new Foo(3, 3);
    final Foo a1Copy = new Foo(1, 1);
    assertNotSame(a1, a1Copy);
    assertSame(cache.get(a1), a1);
    assertSame(cache.get(a2), a2);
    assertSame(cache.get(b1), b1);
    assertSame(cache.get(a1Copy), a1);
  }

}
