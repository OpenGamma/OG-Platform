/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.language;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;

import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;

/**
 * Test the {@link ValueDecoration} and {@link ValueDecorator} utility classes.
 */
@Test(groups = TestGroup.UNIT)
public class ValueDecorationTest {

  private static final class Decoration extends ValueDecoration {

    private int _foo;

    private Decoration(final Decorator decorator) {
      super(decorator);
    }

    public int getFoo() {
      return _foo;
    }

    public void setFoo(final int foo) {
      _foo = foo;
    }

  }

  private static final class Decorator extends ValueDecorator<Decoration> {

    @Override
    public Decoration create() {
      return new Decoration(this);
    }

  }

  public void testOperations() {
    final Decorator decorator = new Decorator();
    Value v1 = new Value();
    Value v2 = new Value();
    Decoration decoration = decorator.get(v1);
    assertNull(decoration);
    decoration = decorator.get(v2);
    assertNull(decoration);
    decoration = decorator.create();
    decoration.setFoo(42);
    v1 = decoration.applyTo(v1);
    decoration = decorator.create();
    v2 = decoration.applyTo(v2);
    decoration.setFoo(24);
    decoration = decorator.get(v1);
    assertNotNull(decoration);
    assertEquals(decoration.getFoo(), 42);
    decoration = decorator.get(v2);
    assertNotNull(decoration);
    assertEquals(decoration.getFoo(), 24);
  }

}
