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
 * Test the {@link DataDecoration} and {@link DataDecorator} utility classes.
 */
@Test(groups = TestGroup.UNIT)
public class DataDecorationTest {

  private static final class Decoration extends DataDecoration {

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

  private static final class Decorator extends DataDecorator<Decoration> {

    @Override
    public Decoration create() {
      return new Decoration(this);
    }

  }

  public void testOperations() {
    final Decorator decorator = new Decorator();
    Data d1 = new Data();
    Data d2 = new Data();
    Decoration decoration = decorator.get(d1);
    assertNull(decoration);
    decoration = decorator.get(d2);
    assertNull(decoration);
    decoration = decorator.create();
    decoration.setFoo(42);
    d1 = decoration.applyTo(d1);
    decoration = decorator.create();
    d2 = decoration.applyTo(d2);
    decoration.setFoo(24);
    decoration = decorator.get(d1);
    assertNotNull(decoration);
    assertEquals(decoration.getFoo(), 42);
    decoration = decorator.get(d2);
    assertNotNull(decoration);
    assertEquals(decoration.getFoo(), 24);
  }

}
