/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.tuple;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import java.util.AbstractMap.SimpleEntry;
import java.util.List;

import org.testng.annotations.Test;

/**
 * Test ObjectsPair.
 */
@Test
public class ObjectsPairTest {

  public void test_Pair_Of_String_String() {
    ObjectsPair<String, String> test = Pair.of("A", "B");
    assertEquals("A", test.getFirst());
    assertEquals("B", test.getSecond());
    assertEquals("A", test.getKey());
    assertEquals("B", test.getValue());
  }

  public void test_Pair_Of_Double_double() {
    ObjectsPair<Double,Double> test = Pair.of(Double.valueOf(1.5), -0.3d);
    assertEquals(Double.valueOf(1.5d), test.getFirst());
    assertEquals(Double.valueOf(-0.3d), test.getSecond());
    assertEquals(Double.valueOf(1.5d), test.getKey());
    assertEquals(Double.valueOf(-0.3d), test.getValue());
  }

  public void test_Pair_Of_double_Double() {
    ObjectsPair<Double,Double> test = Pair.of(1.5d, Double.valueOf(-0.3d));
    assertEquals(Double.valueOf(1.5d), test.getFirst());
    assertEquals(Double.valueOf(-0.3d), test.getSecond());
    assertEquals(Double.valueOf(1.5d), test.getKey());
    assertEquals(Double.valueOf(-0.3d), test.getValue());
  }

  //-------------------------------------------------------------------------
  public void test_ObjectsPair_Of_String_String() {
    ObjectsPair<String, String> test = ObjectsPair.of("A", "B");
    assertEquals("A", test.getFirst());
    assertEquals("B", test.getSecond());
    assertEquals("A", test.getKey());
    assertEquals("B", test.getValue());
  }

  //-------------------------------------------------------------------------
  public void testPair_Object_Object() {
    ObjectsPair<String, String> test = new ObjectsPair<String, String>("A", "B");
    assertEquals("A", test.getFirst());
    assertEquals("B", test.getSecond());
    assertEquals("A", test.getKey());
    assertEquals("B", test.getValue());
  }

  public void testPair_Object_null() {
    ObjectsPair<String, String> test = new ObjectsPair<String, String>("A", null);
    assertEquals("A", test.getFirst());
    assertEquals(null, test.getSecond());
    assertEquals("A", test.getKey());
    assertEquals(null, test.getValue());
  }

  public void testPair_null_Object() {
    ObjectsPair<String, String> test = new ObjectsPair<String, String>(null, "B");
    assertEquals(null, test.getFirst());
    assertEquals("B", test.getSecond());
    assertEquals(null, test.getKey());
    assertEquals("B", test.getValue());
  }

  public void testPair_null_null() {
    ObjectsPair<String, String> test = new ObjectsPair<String, String>(null, null);
    assertEquals(null, test.getFirst());
    assertEquals(null, test.getSecond());
    assertEquals(null, test.getKey());
    assertEquals(null, test.getValue());
  }

  //-------------------------------------------------------------------------
  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testSetValue() {
    ObjectsPair<String,String> pair = new ObjectsPair<String, String>("A", "B");
    pair.setValue("C");
  }

  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testSetValue_null() {
    ObjectsPair<String,String> pair = new ObjectsPair<String, String>("A", "B");
    pair.setValue(null);
  }

  //-------------------------------------------------------------------------
  public void test_toList() {
    Pair<String, String> ab = Pair.of("A", "B");
    List<String> test = ab.toList();
    assertEquals("A", test.get(0));
    assertEquals("B", test.get(1));
  }

  //-------------------------------------------------------------------------
  public void test_compareTo() {
    ObjectsPair<String, String> ab = Pair.of("A", "B");
    ObjectsPair<String, String> ac = Pair.of("A", "C");
    ObjectsPair<String, String> ba = Pair.of("B", "A");
    
    assertTrue(ab.compareTo(ab) == 0);
    assertTrue(ab.compareTo(ac) < 0);
    assertTrue(ab.compareTo(ba) < 0);
    
    assertTrue(ac.compareTo(ab) > 0);
    assertTrue(ac.compareTo(ac) == 0);
    assertTrue(ac.compareTo(ba) < 0);
    
    assertTrue(ba.compareTo(ab) > 0);
    assertTrue(ba.compareTo(ac) > 0);
    assertTrue(ba.compareTo(ba) == 0);
  }

  public void test_compareTo_withNull() {
    ObjectsPair<String, String> nn = Pair.of(null, null);
    ObjectsPair<String, String> na = Pair.of(null, "A");
    ObjectsPair<String, String> an = Pair.of("A", null);
    ObjectsPair<String, String> aa = Pair.of("A", "A");
    
    assertTrue(nn.compareTo(nn) == 0);
    assertTrue(nn.compareTo(na) < 0);
    assertTrue(nn.compareTo(an) < 0);
    assertTrue(nn.compareTo(aa) < 0);
    
    assertTrue(na.compareTo(nn) > 0);
    assertTrue(na.compareTo(na) == 0);
    assertTrue(na.compareTo(an) < 0);
    assertTrue(na.compareTo(aa) < 0);
    
    assertTrue(an.compareTo(nn) > 0);
    assertTrue(an.compareTo(na) > 0);
    assertTrue(an.compareTo(an) == 0);
    assertTrue(an.compareTo(aa) < 0);
    
    assertTrue(aa.compareTo(nn) > 0);
    assertTrue(aa.compareTo(na) > 0);
    assertTrue(aa.compareTo(an) > 0);
    assertTrue(aa.compareTo(aa) == 0);
  }

  public void test_equals() {
    ObjectsPair<Integer, String> a = new ObjectsPair<Integer, String>(1, "Hello");
    ObjectsPair<Integer, String> b = new ObjectsPair<Integer, String>(1, "Goodbye");
    ObjectsPair<Integer, String> c = new ObjectsPair<Integer, String>(2, "Hello");
    ObjectsPair<Integer, String> d = new ObjectsPair<Integer, String>(2, "Goodbye");
    assertTrue(a.equals(a));
    assertFalse(a.equals(b));
    assertFalse(a.equals(c));
    assertFalse(a.equals(d));
    
    assertFalse(b.equals(a));
    assertTrue(b.equals(b));
    assertFalse(b.equals(c));
    assertFalse(b.equals(d));
    
    assertFalse(c.equals(a));
    assertFalse(c.equals(b));
    assertTrue(c.equals(c));
    assertFalse(c.equals(d));
    
    assertFalse(d.equals(a));
    assertFalse(d.equals(b));
    assertFalse(d.equals(c));
    assertTrue(d.equals(d));
  }

  public void test_equals_withNull() {
    ObjectsPair<Integer, String> a = new ObjectsPair<Integer, String>(1, "Hello");
    ObjectsPair<Integer, String> b = new ObjectsPair<Integer, String>(null, "Hello");
    ObjectsPair<Integer, String> c = new ObjectsPair<Integer, String>(1, null);
    ObjectsPair<Integer, String> d = new ObjectsPair<Integer, String>(null, null);
    assertTrue(a.equals(a));
    assertFalse(a.equals(b));
    assertFalse(a.equals(c));
    assertFalse(a.equals(d));
    
    assertFalse(b.equals(a));
    assertTrue(b.equals(b));
    assertFalse(b.equals(c));
    assertFalse(b.equals(d));
    
    assertFalse(c.equals(a));
    assertFalse(c.equals(b));
    assertTrue(c.equals(c));
    assertFalse(c.equals(d));
    
    assertFalse(d.equals(a));
    assertFalse(d.equals(b));
    assertFalse(d.equals(c));
    assertTrue(d.equals(d));
  }

  public void test_equals_other() {
    ObjectsPair<Integer, String> a = new ObjectsPair<Integer, String>(1, "Hello");
    
    assertFalse(a.equals(""));
    assertFalse(a.equals(null));
  }

  public void test_hashCode() {
    ObjectsPair<Integer, String> a = new ObjectsPair<Integer, String>(1, "Hello");
    ObjectsPair<Integer, String> b = new ObjectsPair<Integer, String>(null, "Hello");
    ObjectsPair<Integer, String> c = new ObjectsPair<Integer, String>(1, null);
    ObjectsPair<Integer, String> d = new ObjectsPair<Integer, String>(null, null);
    assertEquals(a.hashCode(), a.hashCode());
    assertEquals(b.hashCode(), b.hashCode());
    assertEquals(c.hashCode(), c.hashCode());
    assertEquals(d.hashCode(), d.hashCode());
    
    assertEquals(1 ^ "Hello".hashCode(), a.hashCode());
    assertEquals("Hello".hashCode(), b.hashCode());
    assertEquals(1, c.hashCode());
    assertEquals(0, d.hashCode());
    // can't test for different hash codes as they might not be different
  }

  public void test_mapEntry() {
    ObjectsPair<Integer, String> a = new ObjectsPair<Integer, String>(1, "Hello");
    SimpleEntry<Integer, String> b = new SimpleEntry<Integer, String>(1, "Hello");
    
    assertTrue(a.equals(a));
    assertTrue(a.equals(b));
    assertTrue(b.equals(a));
    assertTrue(b.equals(b));
    assertEquals(a.hashCode(), b.hashCode());
  }

  public void test_toString() {
    Pair<String, String> test = Pair.of("A", "B");
    assertEquals("[A, B]", test.toString());
  }

}
