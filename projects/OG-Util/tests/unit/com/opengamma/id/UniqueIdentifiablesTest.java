/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.id;

import static org.testng.AssertJUnit.assertEquals;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

import org.testng.annotations.Test;

/**
 * Test UniqueIdentifiables. 
 */
@Test
public class UniqueIdentifiablesTest {

  @SuppressWarnings("unchecked")
  public void test_constructor() throws Exception {
    Constructor<?>[] cons = UniqueIdentifiables.class.getDeclaredConstructors();
    assertEquals(1, cons.length);
    assertEquals(0, cons[0].getParameterTypes().length);
    assertEquals(true, Modifier.isPrivate(cons[0].getModifiers()));
    Constructor<UniqueIdentifiables> con = (Constructor<UniqueIdentifiables>) cons[0];
    con.setAccessible(true);
    con.newInstance();
  }

  public void test_set_success() {
    UniqueIdentifier uid = UniqueIdentifier.of("A", "B");
    MockMutable mock = new MockMutable();
    UniqueIdentifiables.setInto(mock, uid);
    assertEquals(uid, mock.uid);
  }

  public void test_set_notMutableUniqueIdentifiable() {
    UniqueIdentifier uid = UniqueIdentifier.of("A", "B");
    UniqueIdentifiables.setInto(new Object(), uid);
    // no error
  }

  static class MockMutable implements MutableUniqueIdentifiable {
    UniqueIdentifier uid;
    @Override
    public void setUniqueId(UniqueIdentifier uid) {
      this.uid = uid;
    }
  }

}
