/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.language.view;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

/**
 * Tests the {@link UserViewClient} class.
 */
@Test
public class UserViewClientTest {

  private static class CustomData extends UserViewClientData {

    private String _value;

    public CustomData(final String value) {
      _value = value;
    }

    public String getValue() {
      return _value;
    }

  }

  private static final class Foo extends UserViewClientBinding<CustomData> {

    private int _counter;

    @Override
    protected CustomData create(final UserViewClient viewClient) {
      return new CustomData("Foo" + ++_counter);
    }
  }

  private static final class Bar extends UserViewClientBinding<CustomData> {

    private int _counter;

    @Override
    protected CustomData create(final UserViewClient viewClient) {
      return new CustomData("Bar" + ++_counter);
    }
  }

  public void testBinding() {
    final Foo foo = new Foo();
    final Bar bar = new Bar();
    UserViewClient client = new UserViewClient(null, null, null);
    CustomData fooValue = client.getData(foo);
    assertEquals(fooValue.getValue(), "Foo1");
    CustomData barValue = client.getData(bar);
    assertEquals(barValue.getValue(), "Bar1");
    client = new UserViewClient(null, null, null);
    fooValue = client.getData(foo);
    assertEquals(fooValue.getValue(), "Foo2");
    barValue = client.getData(bar);
    assertEquals(barValue.getValue(), "Bar2");
  }

}
