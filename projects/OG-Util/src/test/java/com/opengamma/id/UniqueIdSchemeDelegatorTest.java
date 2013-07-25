/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.id;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.fail;

import java.util.Collections;
import java.util.Map;

import org.testng.annotations.Test;

import com.google.common.collect.ImmutableMap;
import com.opengamma.util.test.TestGroup;

/**
 * Test {@link UniqueIdSchemeDelegator}. 
 */
@Test(groups = TestGroup.UNIT)
public class UniqueIdSchemeDelegatorTest {

  public void test_constructor_defaultOnly() {
    UniqueIdSchemeDelegator<String> test = new UniqueIdSchemeDelegator<String>("default");
    assertEquals("default", test.chooseDelegate("A"));
    assertEquals("default", test.chooseDelegate("B"));
    assertEquals("default", test.getDefaultDelegate());
    assertEquals(Collections.emptyMap(), test.getDelegates());
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_constructor_defaultOnly_nullDefault() {
    new UniqueIdSchemeDelegator<String>(null);
  }

  //-------------------------------------------------------------------------
  public void test_constructor_defaultAndMap() {
    Map<String, String> map = ImmutableMap.of("A", "adapt", "B", "bootup", "C", "curve");
    UniqueIdSchemeDelegator<String> test = new UniqueIdSchemeDelegator<String>("default", map);
    assertEquals("adapt", test.chooseDelegate("A"));
    assertEquals("bootup", test.chooseDelegate("B"));
    assertEquals("curve", test.chooseDelegate("C"));
    assertEquals("default", test.chooseDelegate("D"));
    assertEquals("default", test.getDefaultDelegate());
    assertEquals(map, test.getDelegates());
    try {
      test.getDelegates().clear();
      fail();
    } catch (UnsupportedOperationException ex) {
      // expected
    }
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_constructor_defaultAndMap_nullDefault() {
    Map<String, String> map = ImmutableMap.of("A", "adapt", "B", "bootup", "C", "curve");
    new UniqueIdSchemeDelegator<String>(null, map);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_constructor_defaultAndMap_nullMap() {
    new UniqueIdSchemeDelegator<String>("default", null);
  }

  //-------------------------------------------------------------------------
  public void test_registerDelegate_removeDelegate() {
    Map<String, String> map = ImmutableMap.of("A", "adapt", "B", "bootup");
    UniqueIdSchemeDelegator<String> test = new UniqueIdSchemeDelegator<String>("default", map);
    assertEquals("adapt", test.chooseDelegate("A"));
    assertEquals("bootup", test.chooseDelegate("B"));
    assertEquals("default", test.chooseDelegate("C"));
    assertEquals("default", test.chooseDelegate("D"));
    assertEquals(true, test.registerDelegate("C", "curve"));
    assertEquals(false, test.registerDelegate("C", "curve"));
    assertEquals("adapt", test.chooseDelegate("A"));
    assertEquals("bootup", test.chooseDelegate("B"));
    assertEquals("curve", test.chooseDelegate("C"));
    assertEquals("default", test.chooseDelegate("D"));
    test.removeDelegate("C");
    assertEquals("adapt", test.chooseDelegate("A"));
    assertEquals("bootup", test.chooseDelegate("B"));
    assertEquals("default", test.chooseDelegate("C"));
    assertEquals("default", test.chooseDelegate("D"));
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_registerDelegate_nullScheme() {
    UniqueIdSchemeDelegator<String> test = new UniqueIdSchemeDelegator<String>("default");
    test.registerDelegate(null, "default");
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_registerDelegate_nullDelegate() {
    UniqueIdSchemeDelegator<String> test = new UniqueIdSchemeDelegator<String>("default");
    test.registerDelegate("default", null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_removeDelegate_null() {
    UniqueIdSchemeDelegator<String> test = new UniqueIdSchemeDelegator<String>("default");
    test.removeDelegate(null);
  }

}
