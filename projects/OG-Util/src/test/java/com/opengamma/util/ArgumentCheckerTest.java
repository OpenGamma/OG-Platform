/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.opengamma.util.tuple.FirstThenSecondPairComparator;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;

import com.google.common.collect.Sets;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.tuple.ObjectsPair;
import com.opengamma.util.tuple.Pair;

/**
 * Test ArgumentChecker.
 */
@Test(groups = TestGroup.UNIT)
public class ArgumentCheckerTest {

  //-------------------------------------------------------------------------
  public void test_isTrue_ok() {
    ArgumentChecker.isTrue(true, "Message");
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_isTrue_false() {
    try {
      ArgumentChecker.isTrue(false, "Message");
    } catch (IllegalArgumentException ex) {
      assertEquals(ex.getMessage().equals("Message"), true);
      throw ex;
    }
  }

  public void test_isTrue_ok_args() {
    ArgumentChecker.isTrue(true, "Message {} {} {}", "A", 2, 3.);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_isTrue_false_args() {
    try {
      ArgumentChecker.isTrue(false, "Message {} {} {}", "A", 2, 3.);
    } catch (IllegalArgumentException ex) {
      assertEquals(ex.getMessage().equals("Message A 2 3.0"), true);
      throw ex;
    }
  }
  
  public void test_isFalse_ok() {
    ArgumentChecker.isFalse(false, "Message");
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_isFalse_true() {
    try {
      ArgumentChecker.isFalse(true, "Message");
    } catch (IllegalArgumentException ex) {
      assertEquals(ex.getMessage().equals("Message"), true);
      throw ex;
    }
  }

  public void test_isFalse_ok_args() {
    ArgumentChecker.isFalse(false, "Message {} {} {}", "A", 2., 3, true);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_isFalse_true_args() {
    try {
      ArgumentChecker.isFalse(true, "Message {} {} {} {}", "A", 2., 3, true);
    } catch (IllegalArgumentException ex) {
      assertEquals(ex.getMessage().equals("Message A 2.0 3 true"), true);
      throw ex;
    }
  }
  
  //-------------------------------------------------------------------------
  public void test_notNull_ok() {
    ArgumentChecker.notNull("Kirk", "name");
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_notNull_null() {
    try {
      ArgumentChecker.notNull(null, "name");
    } catch (IllegalArgumentException ex) {
      assertEquals(ex.getMessage().contains("'name'"), true);
      assertEquals(ex.getMessage().contains("Injected"), false);
      throw ex;
    }
  }

  //-------------------------------------------------------------------------
  public void test_notNullInjected_ok() {
    ArgumentChecker.notNullInjected("Kirk", "name");
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_notNullInjected_null() {
    try {
      ArgumentChecker.notNullInjected(null, "name");
    } catch (IllegalArgumentException ex) {
      assertEquals(ex.getMessage().contains("'name'"), true);
      assertEquals(ex.getMessage().contains("Injected"), true);
      throw ex;
    }
  }

  //-------------------------------------------------------------------------
  public void test_notBlank_String_ok() {
    String str = "Kirk";
    assertEquals("Kirk", ArgumentChecker.notBlank(str, "name"));
  }

  public void test_notBlank_String_ok_trimmed() {
    String str = " Kirk ";
    assertEquals("Kirk", ArgumentChecker.notBlank(str, "name"));
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_notBlank_String_null() {
    String str = null;
    try {
      ArgumentChecker.notBlank(str, "name");
    } catch (IllegalArgumentException ex) {
      assertEquals(ex.getMessage().contains("'name'"), true);
      throw ex;
    }
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_notBlank_String_empty() {
    String str = "";
    try {
      ArgumentChecker.notBlank(str, "name");
    } catch (IllegalArgumentException ex) {
      assertEquals(ex.getMessage().contains("'name'"), true);
      throw ex;
    }
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_notBlank_String_spaces() {
    String str = "  ";
    try {
      ArgumentChecker.notBlank(str, "name");
    } catch (IllegalArgumentException ex) {
      assertEquals(ex.getMessage().contains("'name'"), true);
      throw ex;
    }
  }

  //-------------------------------------------------------------------------
  public void test_notEmpty_String_ok() {
    String str = "Kirk";
    ArgumentChecker.notEmpty(str, "name");
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_notEmpty_String_null() {
    String str = null;
    try {
      ArgumentChecker.notEmpty(str, "name");
    } catch (IllegalArgumentException ex) {
      assertEquals(ex.getMessage().contains("'name'"), true);
      throw ex;
    }
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
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
  public void test_notEmpty_Array_ok() {
    Object[] array = new Object[] {"Element"};
    ArgumentChecker.notEmpty(array, "name");
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_notEmpty_Array_null() {
    Object[] array = null;
    try {
      ArgumentChecker.notEmpty(array, "name");
    } catch (IllegalArgumentException ex) {
      assertEquals(ex.getMessage().contains("'name'"), true);
      throw ex;
    }
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_notEmpty_Array_empty() {
    Object[] array = new Object[] {};
    try {
      ArgumentChecker.notEmpty(array, "name");
    } catch (IllegalArgumentException ex) {
      assertEquals(ex.getMessage().contains("'name'"), true);
      throw ex;
    }
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_notEmpty_2DArray_null() {
    Object[][] array = null;
    try {
      ArgumentChecker.notEmpty(array, "name");
    } catch (IllegalArgumentException ex) {
      assertEquals(ex.getMessage().contains("'name'"), true);
      throw ex;
    }
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_notEmpty_2DArray_empty() {
    Object[][] array = new Object[0][0];
    try {
      ArgumentChecker.notEmpty(array, "name");
    } catch (IllegalArgumentException ex) {
      assertEquals(ex.getMessage().contains("'name'"), true);
      throw ex;
    }
  }

  //-------------------------------------------------------------------------
  public void test_notEmpty_intArray_ok() {
    int[] array = new int[] {6};
    ArgumentChecker.notEmpty(array, "name");
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_notEmpty_intArray_null() {
    int[] array = null;
    try {
      ArgumentChecker.notEmpty(array, "name");
    } catch (IllegalArgumentException ex) {
      assertEquals(ex.getMessage().contains("'name'"), true);
      throw ex;
    }
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_notEmpty_intArray_empty() {
    int[] array = new int[0];
    try {
      ArgumentChecker.notEmpty(array, "name");
    } catch (IllegalArgumentException ex) {
      assertEquals(ex.getMessage().contains("'name'"), true);
      throw ex;
    }
  }

  //-------------------------------------------------------------------------
  public void test_notEmpty_longArray_ok() {
    long[] array = new long[] {6L};
    ArgumentChecker.notEmpty(array, "name");
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_notEmpty_longArray_null() {
    long[] array = null;
    try {
      ArgumentChecker.notEmpty(array, "name");
    } catch (IllegalArgumentException ex) {
      assertEquals(ex.getMessage().contains("'name'"), true);
      throw ex;
    }
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_notEmpty_longArray_empty() {
    long[] array = new long[0];
    try {
      ArgumentChecker.notEmpty(array, "name");
    } catch (IllegalArgumentException ex) {
      assertEquals(ex.getMessage().contains("'name'"), true);
      throw ex;
    }
  }

  //-------------------------------------------------------------------------
  public void test_notEmpty_doubleArray_ok() {
    double[] array = new double[] {6.0d};
    ArgumentChecker.notEmpty(array, "name");
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_notEmpty_doubleArray_null() {
    double[] array = null;
    try {
      ArgumentChecker.notEmpty(array, "name");
    } catch (IllegalArgumentException ex) {
      assertEquals(ex.getMessage().contains("'name'"), true);
      throw ex;
    }
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_notEmpty_doubleArray_empty() {
    double[] array = new double[0];
    try {
      ArgumentChecker.notEmpty(array, "name");
    } catch (IllegalArgumentException ex) {
      assertEquals(ex.getMessage().contains("'name'"), true);
      throw ex;
    }
  }

  //-------------------------------------------------------------------------
  public void test_notEmpty_Iterable_ok() {
    Iterable<?> coll = Arrays.asList("Element");
    ArgumentChecker.notEmpty(coll, "name");
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_notEmpty_Iterable_null() {
    Iterable<?> coll = null;
    try {
      ArgumentChecker.notEmpty(coll, "name");
    } catch (IllegalArgumentException ex) {
      assertEquals(ex.getMessage().contains("'name'"), true);
      throw ex;
    }
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_notEmpty_Iterable_empty() {
    Iterable<?> coll = Collections.emptyList();
    try {
      ArgumentChecker.notEmpty(coll, "name");
    } catch (IllegalArgumentException ex) {
      assertEquals(ex.getMessage().contains("'name'"), true);
      throw ex;
    }
  }

  //-------------------------------------------------------------------------
  public void test_notEmpty_Map_ok() {
    Map<?, ?> map = Collections.singletonMap("Element", "Element");
    ArgumentChecker.notEmpty(map, "name");
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_notEmpty_Map_null() {
    Map<?, ?> map = null;
    try {
      ArgumentChecker.notEmpty(map, "name");
    } catch (IllegalArgumentException ex) {
      assertEquals(ex.getMessage().contains("'name'"), true);
      throw ex;
    }
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
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
  public void test_noNulls_Array_ok() {
    Object[] array = new Object[] {"Element"};
    ArgumentChecker.noNulls(array, "name");
  }

  public void test_noNulls_Array_ok_empty() {
    Object[] array = new Object[] {};
    ArgumentChecker.noNulls(array, "name");
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_noNulls_Array_null() {
    Object[] array = null;
    try {
      ArgumentChecker.noNulls(array, "name");
    } catch (IllegalArgumentException ex) {
      assertEquals(ex.getMessage().contains("'name'"), true);
      throw ex;
    }
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
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
  public void test_noNulls_Iterable_ok() {
    Iterable<?> coll = Arrays.asList("Element");
    ArgumentChecker.noNulls(coll, "name");
  }

  public void test_noNulls_Iterable_ok_empty() {
    Iterable<?> coll = Arrays.asList();
    ArgumentChecker.noNulls(coll, "name");
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_noNulls_Iterable_null() {
    Iterable<?> coll = null;
    try {
      ArgumentChecker.noNulls(coll, "name");
    } catch (IllegalArgumentException ex) {
      assertEquals(ex.getMessage().contains("'name'"), true);
      throw ex;
    }
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_noNulls_Iterable_nullElement() {
    Iterable<?> coll = Arrays.asList((Object) null);
    try {
      ArgumentChecker.noNulls(coll, "name");
    } catch (IllegalArgumentException ex) {
      assertEquals(ex.getMessage().contains("'name'"), true);
      throw ex;
    }
  }

  //-------------------------------------------------------------------------
  public void test_noNulls_Map_ok() {
    Map<Object, Object> map = new HashMap<>();
    map.put("A", "B");
    ArgumentChecker.noNulls(map, "name");
  }

  public void test_noNulls_Map_ok_empty() {
    Map<Object, Object> map = new HashMap<>();
    ArgumentChecker.noNulls(map, "name");
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_noNulls_Map_null() {
    Map<Object, Object> map = null;
    try {
      ArgumentChecker.noNulls(map, "name");
    } catch (IllegalArgumentException ex) {
      assertEquals(ex.getMessage().contains("'name'"), true);
      throw ex;
    }
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_noNulls_Map_nullKey() {
    Map<Object, Object> map = new HashMap<>();
    map.put("A", "B");
    map.put(null, "Z");
    try {
      ArgumentChecker.noNulls(map, "name");
    } catch (IllegalArgumentException ex) {
      assertEquals(ex.getMessage().contains("'name'"), true);
      throw ex;
    }
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_noNulls_Map_nullValue() {
    Map<Object, Object> map = new HashMap<>();
    map.put("A", "B");
    map.put("Z", null);
    try {
      ArgumentChecker.noNulls(map, "name");
    } catch (IllegalArgumentException ex) {
      assertEquals(ex.getMessage().contains("'name'"), true);
      throw ex;
    }
  }

  //-------------------------------------------------------------------------
  public void test_notNegative_int_ok() {
    ArgumentChecker.notNegative(0, "name");
    ArgumentChecker.notNegative(1, "name");
  }

  @Test(expectedExceptions = IllegalArgumentException.class) 
  public void test_notNegative_int_negative() {
    try {
      ArgumentChecker.notNegative(-1, "name");
    } catch(IllegalArgumentException iae) {
      assertEquals(iae.getMessage().contains("'name'"), true);
      throw iae;
    }
  }

  public void test_notNegative_long_ok() {
    ArgumentChecker.notNegative(0L, "name");
    ArgumentChecker.notNegative(1L, "name");
  }

  @Test(expectedExceptions = IllegalArgumentException.class) 
  public void test_notNegative_long_negative() {
    try {
      ArgumentChecker.notNegative(-1L, "name");
    } catch(IllegalArgumentException iae) {
      assertEquals(iae.getMessage().contains("'name'"), true);
      throw iae;
    }
  }

  public void test_notNegative_double_ok() {
    ArgumentChecker.notNegative(0.0d, "name");
    ArgumentChecker.notNegative(1.0d, "name");
  }

  @Test(expectedExceptions = IllegalArgumentException.class) 
  public void test_notNegative_double_negative() {
    try {
      ArgumentChecker.notNegative(-1.0d, "name");
    } catch(IllegalArgumentException iae) {
      assertEquals(iae.getMessage().contains("'name'"), true);
      throw iae;
    }
  }

  //-------------------------------------------------------------------------
  public void test_notNegativeOrZero_int_ok() {
    ArgumentChecker.notNegativeOrZero(1, "name");
  }

  @Test(expectedExceptions = IllegalArgumentException.class) 
  public void test_notNegativeOrZero_int_zero() {
    try {
      ArgumentChecker.notNegativeOrZero(0, "name");
    } catch(IllegalArgumentException iae) {
      assertEquals(iae.getMessage().contains("'name'"), true);
      throw iae;
    }
  }

  @Test(expectedExceptions = IllegalArgumentException.class) 
  public void test_notNegativeOrZero_int_negative() {
    try {
      ArgumentChecker.notNegativeOrZero(-1, "name");
    } catch(IllegalArgumentException iae) {
      assertEquals(iae.getMessage().contains("'name'"), true);
      throw iae;
    }
  }

  public void test_notNegativeOrZero_double_ok() {
    ArgumentChecker.notNegativeOrZero(1.0d, "name");
  }

  @Test(expectedExceptions = IllegalArgumentException.class) 
  public void test_notNegativeOrZero_double_zero() {
    try {
      ArgumentChecker.notNegativeOrZero(0.0d, "name");
    } catch(IllegalArgumentException iae) {
      assertEquals(iae.getMessage().contains("'name'"), true);
      throw iae;
    }
  }

  @Test(expectedExceptions = IllegalArgumentException.class) 
  public void test_notNegativeOrZero_double_negative() {
    try {
      ArgumentChecker.notNegativeOrZero(-1.0d, "name");
    } catch(IllegalArgumentException iae) {
      assertEquals(iae.getMessage().contains("'name'"), true);
      throw iae;
    }
  }

  //-------------------------------------------------------------------------
  public void test_notZero_double_ok() {
    ArgumentChecker.notZero(1d, 0.1d, "name");
  }

  @Test(expectedExceptions = IllegalArgumentException.class) 
  public void test_notZero_double_zero() {
    try {
      ArgumentChecker.notZero(0d, 0.1d, "name");
    } catch(IllegalArgumentException iae) {
      assertEquals(iae.getMessage().contains("'name'"), true);
      throw iae;
    }
  }

  public void test_notZero_double_negative() {
    try {
      ArgumentChecker.notZero(-1d, 0.1d, "name");
    } catch(IllegalArgumentException iae) {
      assertEquals(iae.getMessage().contains("'name'"), true);
      throw iae;
    }
  }

  //-------------------------------------------------------------------------
  public void testHasNullElement() {
    Collection<?> c = Sets.newHashSet(null, new Object(), new Object());
    assertTrue(ArgumentChecker.hasNullElement(c));
    c = Sets.newHashSet(new Object(), new Object());
    assertFalse(ArgumentChecker.hasNullElement(c));
  }
  
  public void testHasNegativeElement() {
    Collection<Double> c = Sets.newHashSet(4., -5., -6.);
    assertTrue(ArgumentChecker.hasNegativeElement(c));
    c = Sets.newHashSet(1., 2., 3.);
    assertFalse(ArgumentChecker.hasNegativeElement(c));
  }
  
  public void testIsInRange() {
    double low = 0;
    double high = 1;    
    assertTrue(ArgumentChecker.isInRangeExclusive(low, high, 0.5));
    assertFalse(ArgumentChecker.isInRangeExclusive(low, high, -high));
    assertFalse(ArgumentChecker.isInRangeExclusive(low, high, 2 * high));
    assertFalse(ArgumentChecker.isInRangeExclusive(low, high, low));
    assertFalse(ArgumentChecker.isInRangeExclusive(low, high, high));
    assertTrue(ArgumentChecker.isInRangeInclusive(low, high, 0.5));
    assertFalse(ArgumentChecker.isInRangeInclusive(low, high, -high));
    assertFalse(ArgumentChecker.isInRangeInclusive(low, high, 2 * high));
    assertTrue(ArgumentChecker.isInRangeInclusive(low, high, low));
    assertTrue(ArgumentChecker.isInRangeInclusive(low, high, high));
    assertTrue(ArgumentChecker.isInRangeExcludingLow(low, high, 0.5));
    assertFalse(ArgumentChecker.isInRangeExcludingLow(low, high, -high));
    assertFalse(ArgumentChecker.isInRangeExcludingLow(low, high, 2 * high));
    assertFalse(ArgumentChecker.isInRangeExcludingLow(low, high, low));
    assertTrue(ArgumentChecker.isInRangeExcludingLow(low, high, high));
    assertTrue(ArgumentChecker.isInRangeExcludingHigh(low, high, 0.5));
    assertFalse(ArgumentChecker.isInRangeExcludingHigh(low, high, -high));
    assertFalse(ArgumentChecker.isInRangeExcludingHigh(low, high, 2 * high));
    assertTrue(ArgumentChecker.isInRangeExcludingHigh(low, high, low));
    assertFalse(ArgumentChecker.isInRangeExcludingHigh(low, high, high));
  }
  
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNotEmptyDoubleArray() {
    double[] d = new double[0];
    try {
      ArgumentChecker.notEmpty(d, "name");
    } catch (IllegalArgumentException ex) {
      assertEquals(ex.getMessage().contains("'name'"), true);
      throw ex;
    }
  } 
  
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNotEmptyLongArray() {
    double[] d = new double[0];
    try {
      ArgumentChecker.notEmpty(d, "name");
    } catch (IllegalArgumentException ex) {
      assertEquals(ex.getMessage().contains("'name'"), true);
      throw ex;
    }
  }

  //-------------------------------------------------------------------------
  public void test_inOrderOrEqual_true() {
    LocalDate a = LocalDate.of(2011, 7, 2);
    LocalDate b = LocalDate.of(2011, 7, 3);
    ArgumentChecker.inOrderOrEqual(a, b, "a", "b");
    ArgumentChecker.inOrderOrEqual(a, a, "a", "b");
    ArgumentChecker.inOrderOrEqual(b, b, "a", "b");
  }

  public void test_inOrderOrEqual_generics() {
    final Pair<String, String> a = ObjectsPair.of("c", "d");
    final Pair<String, String> b = ObjectsPair.of("e", "f");
    final FirstThenSecondPairComparator<String, String> comparator = new FirstThenSecondPairComparator<String, String>();
    Comparable<? super Pair<String, String>> ca = new Comparable<Pair<String, String>>() {
      @Override
      public int compareTo(Pair<String, String> other) {
        return comparator.compare(a, other);
      }
    };
    Comparable<? super Pair<String, String>> cb = new Comparable<Pair<String, String>>() {
      @Override
      public int compareTo(Pair<String, String> other) {
        return comparator.compare(b, other);
      }
    };
    ArgumentChecker.inOrderOrEqual(ca, b, "a", "b");
    ArgumentChecker.inOrderOrEqual(ca, a, "a", "b");
    ArgumentChecker.inOrderOrEqual(cb, b, "a", "b");
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_inOrderOrEqual_false() {
    LocalDate a = LocalDate.of(2011, 7, 3);
    LocalDate b = LocalDate.of(2011, 7, 2);
    try {
      ArgumentChecker.inOrderOrEqual(a, b, "a", "b");
    } catch (IllegalArgumentException ex) {
      assertEquals(ex.getMessage().contains("'a'"), true);
      assertEquals(ex.getMessage().contains("'b'"), true);
      throw ex;
    }
  }

}
