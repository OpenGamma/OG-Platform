/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.id;

import static org.testng.AssertJUnit.assertEquals;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.List;

import org.testng.annotations.Test;

import com.google.common.collect.ImmutableList;
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

  //-------------------------------------------------------------------------
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

  //-------------------------------------------------------------------------
  public void test_toStringList() {
    Iterable<ObjectIdentifiable> objectIds = ImmutableList.<ObjectIdentifiable>of(ObjectId.of("A", "X"), UniqueId.of("B", "Y", "1"), ObjectId.of("C", "Z"));
    Iterable<String> expected = ImmutableList.of("A~X", "B~Y~1", "C~Z");
    List<String> test = IdUtils.toStringList(objectIds);
    assertEquals(expected, test);
  }

  public void test_toStringList_null() {
    List<String> test = IdUtils.toStringList(null);
    assertEquals(0, test.size());
  }

  //-------------------------------------------------------------------------
  public void test_parseObjectIds() {
    Iterable<String> objectIds = ImmutableList.of("A~X", "B~Y", "C~Z");
    Iterable<ObjectId> expected = ImmutableList.of(ObjectId.of("A", "X"), ObjectId.of("B", "Y"), ObjectId.of("C", "Z"));
    List<ObjectId> test = IdUtils.parseObjectIds(objectIds);
    assertEquals(expected, test);
  }

  public void test_parseObjectIds_null() {
    List<ObjectId> test = IdUtils.parseObjectIds(null);
    assertEquals(0, test.size());
  }

  //-------------------------------------------------------------------------
  public void test_parseUniqueIds() {
    Iterable<String> objectIds = ImmutableList.of("A~X", "B~Y~1", "C~Z");
    Iterable<UniqueId> expected = ImmutableList.of(UniqueId.of("A", "X"), UniqueId.of("B", "Y", "1"), UniqueId.of("C", "Z"));
    List<UniqueId> test = IdUtils.parseUniqueIds(objectIds);
    assertEquals(expected, test);
  }

  public void test_parseUniqueIds_null() {
    List<UniqueId> test = IdUtils.parseUniqueIds(null);
    assertEquals(0, test.size());
  }

}
