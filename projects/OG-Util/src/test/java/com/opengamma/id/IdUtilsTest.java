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

import com.opengamma.util.test.TestGroup;

/**
 * Test {@link IdUtils}. 
 */
@Test(groups = TestGroup.UNIT)
public class IdUtilsTest {

  @SuppressWarnings("unchecked")
  public void test_constructor() throws Exception {
    Constructor<?>[] cons = IdUtils.class.getDeclaredConstructors();
    assertEquals(1, cons.length);
    assertEquals(0, cons[0].getParameterTypes().length);
    assertEquals(true, Modifier.isPrivate(cons[0].getModifiers()));
    Constructor<IdUtils> con = (Constructor<IdUtils>) cons[0];
    con.setAccessible(true);
    con.newInstance();
  }

  public void test_set_success() {
    UniqueId uniqueId = UniqueId.of("A", "B");
    MockMutable mock = new MockMutable();
    IdUtils.setInto(mock, uniqueId);
    assertEquals(uniqueId, mock.uniqueId);
  }

  public void test_set_notMutableUniqueIdentifiable() {
    UniqueId uniqueId = UniqueId.of("A", "B");
    IdUtils.setInto(new Object(), uniqueId);
    // no error
  }

  static class MockMutable implements MutableUniqueIdentifiable {
    UniqueId uniqueId;
    @Override
    public void setUniqueId(UniqueId uniqueId) {
      this.uniqueId = uniqueId;
    }
  }

}
