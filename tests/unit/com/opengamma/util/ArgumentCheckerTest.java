/**
 * Copyright (C) 2010 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.util;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.junit.Test;

/**
 * Test ArgumentChecker.
 */
public class ArgumentCheckerTest {

  //-------------------------------------------------------------------------
  @Test
  public void test_notNull_ok() {
    ArgumentChecker.notNull("Kirk", "name");
  }

  @Test(expected=NullPointerException.class)
  public void test_notNull_null() {
    try {
      ArgumentChecker.notNull(null, "name");
    } catch (NullPointerException ex) {
      assertEquals(ex.getMessage().contains("'name'"), true);
      assertEquals(ex.getMessage().contains("Injected"), false);
      throw ex;
    }
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_notNullInjected_ok() {
    ArgumentChecker.notNullInjected("Kirk", "name");
  }

  @Test(expected=NullPointerException.class)
  public void test_notNullInjected_null() {
    try {
      ArgumentChecker.notNullInjected(null, "name");
    } catch (NullPointerException ex) {
      assertEquals(ex.getMessage().contains("'name'"), true);
      assertEquals(ex.getMessage().contains("Injected"), true);
      throw ex;
    }
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_notEmpty_String_ok() {
    String str = "Kirk";
    ArgumentChecker.notEmpty(str, "name");
  }

  @Test(expected=NullPointerException.class)
  public void test_notEmpty_String_null() {
    String str = null;
    try {
      ArgumentChecker.notEmpty(str, "name");
    } catch (NullPointerException ex) {
      assertEquals(ex.getMessage().contains("'name'"), true);
      throw ex;
    }
  }

  @Test(expected=IllegalArgumentException.class)
  public void test_notEmpty_String_empty() {
    String str = "";
    try {
      ArgumentChecker.notEmpty(str, "name");
    } catch (IllegalArgumentException ex) {
      assertEquals(ex.getMessage().contains("'name'"), true);
      throw ex;
    }
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_notEmpty_Array_ok() {
    Object[] array = new Object[] {"Element"};
    ArgumentChecker.notEmpty(array, "name");
  }

  @Test(expected=NullPointerException.class)
  public void test_notEmpty_Array_null() {
    Object[] array = null;
    try {
      ArgumentChecker.notEmpty(array, "name");
    } catch (NullPointerException ex) {
      assertEquals(ex.getMessage().contains("'name'"), true);
      throw ex;
    }
  }

  @Test(expected=IllegalArgumentException.class)
  public void test_notEmpty_Array_empty() {
    Object[] array = new Object[] {};
    try {
      ArgumentChecker.notEmpty(array, "name");
    } catch (IllegalArgumentException ex) {
      assertEquals(ex.getMessage().contains("'name'"), true);
      throw ex;
    }
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_notEmpty_Collection_ok() {
    Collection<?> coll = Arrays.asList("Element");
    ArgumentChecker.notEmpty(coll, "name");
  }

  @Test(expected=NullPointerException.class)
  public void test_notEmpty_Collection_null() {
    Collection<?> coll = null;
    try {
      ArgumentChecker.notEmpty(coll, "name");
    } catch (NullPointerException ex) {
      assertEquals(ex.getMessage().contains("'name'"), true);
      throw ex;
    }
  }

  @Test(expected=IllegalArgumentException.class)
  public void test_notEmpty_Collection_empty() {
    Collection<?> coll = Collections.emptyList();
    try {
      ArgumentChecker.notEmpty(coll, "name");
    } catch (IllegalArgumentException ex) {
      assertEquals(ex.getMessage().contains("'name'"), true);
      throw ex;
    }
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_notEmpty_Map_ok() {
    Map<?, ?> map = Collections.singletonMap("Element", "Element");
    ArgumentChecker.notEmpty(map, "name");
  }

  @Test(expected=NullPointerException.class)
  public void test_notEmpty_Map_null() {
    Map<?, ?> map = null;
    try {
      ArgumentChecker.notEmpty(map, "name");
    } catch (NullPointerException ex) {
      assertEquals(ex.getMessage().contains("'name'"), true);
      throw ex;
    }
  }

  @Test(expected=IllegalArgumentException.class)
  public void test_notEmpty_Map_empty() {
    Map<?, ?> map = Collections.emptyMap();
    try {
      ArgumentChecker.notEmpty(map, "name");
    } catch (IllegalArgumentException ex) {
      assertEquals(ex.getMessage().contains("'name'"), true);
      throw ex;
    }
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_noNulls_Array_ok() {
    Object[] array = new Object[] {"Element"};
    ArgumentChecker.noNulls(array, "name");
  }

  @Test
  public void test_noNulls_Array_ok_empty() {
    Object[] array = new Object[] {};
    ArgumentChecker.noNulls(array, "name");
  }

  @Test(expected=NullPointerException.class)
  public void test_noNulls_Array_null() {
    Object[] array = null;
    try {
      ArgumentChecker.noNulls(array, "name");
    } catch (NullPointerException ex) {
      assertEquals(ex.getMessage().contains("'name'"), true);
      throw ex;
    }
  }

  @Test(expected=NullPointerException.class)
  public void test_noNulls_Array_nullElement() {
    Object[] array = new Object[] {null};
    try {
      ArgumentChecker.noNulls(array, "name");
    } catch (IllegalArgumentException ex) {
      assertEquals(ex.getMessage().contains("'name'"), true);
      throw ex;
    }
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_noNulls_Collection_ok() {
    Collection<?> coll = Arrays.asList("Element");
    ArgumentChecker.noNulls(coll, "name");
  }

  @Test
  public void test_noNulls_Collection_ok_empty() {
    Collection<?> coll = Arrays.asList();
    ArgumentChecker.noNulls(coll, "name");
  }

  @Test(expected=NullPointerException.class)
  public void test_noNulls_Collection_null() {
    Collection<?> coll = null;
    try {
      ArgumentChecker.noNulls(coll, "name");
    } catch (NullPointerException ex) {
      assertEquals(ex.getMessage().contains("'name'"), true);
      throw ex;
    }
  }

  @Test(expected=NullPointerException.class)
  public void test_noNulls_Collection_nullElement() {
    Collection<?> coll = Arrays.asList((Object) null);
    try {
      ArgumentChecker.noNulls(coll, "name");
    } catch (IllegalArgumentException ex) {
      assertEquals(ex.getMessage().contains("'name'"), true);
      throw ex;
    }
  }

}
