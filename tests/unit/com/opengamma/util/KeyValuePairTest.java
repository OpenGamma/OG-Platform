/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Test KeyValuePair.
 */
public class KeyValuePairTest {

  @Test
  public void testOf_Object_Object() {
    KeyValuePair<String, String> test = KeyValuePair.of("A", "B");
    assertEquals(test.getKey(), "A");
    assertEquals(test.getValue(), "B");
  }

  //-------------------------------------------------------------------------
  @Test
  public void testKeyValuePair_Object_Object() {
    KeyValuePair<String, String> test = new KeyValuePair<String, String>("A", "B");
    assertEquals(test.getKey(), "A");
    assertEquals(test.getValue(), "B");
  }

  @Test
  public void testKeyValuePair_Object_null() {
    KeyValuePair<String, String> test = new KeyValuePair<String, String>("A", null);
    assertEquals(test.getKey(), "A");
    assertEquals(test.getValue(), null);
  }

  @Test
  public void testKeyValuePair_null_Object() {
    KeyValuePair<String, String> test = new KeyValuePair<String, String>(null, "B");
    assertEquals(test.getKey(), null);
    assertEquals(test.getValue(), "B");
  }

  @Test
  public void testKeyValuePair_null_null() {
    KeyValuePair<String, String> test = new KeyValuePair<String, String>(null, null);
    assertEquals(test.getKey(), null);
    assertEquals(test.getValue(), null);
  }

  //-------------------------------------------------------------------------
  @Test
  public void testSetValue() {
    KeyValuePair<String,String> pair = new KeyValuePair<String, String>("One", "Two");
    String old = pair.setValue("Three");
    assertEquals(pair.getValue(), "Three");
    assertEquals(old, "Two");
  }

  @Test
  public void testSetValue_null() {
    KeyValuePair<String,String> pair = new KeyValuePair<String, String>("One", "Two");
    String old = pair.setValue(null);
    assertEquals(pair.getValue(), null);
    assertEquals(old, "Two");
  }

  //-------------------------------------------------------------------------
  @Test
  public void testEquals() {
    KeyValuePair<Integer, String> a = new KeyValuePair<Integer, String>(1, "Hello");
    KeyValuePair<Integer, String> b = new KeyValuePair<Integer, String>(1, "Goodbye");
    KeyValuePair<Integer, String> c = new KeyValuePair<Integer, String>(2, "Hello");
    KeyValuePair<Integer, String> d = new KeyValuePair<Integer, String>(2, "Goodbye");
    assertEquals(a.equals(a), true);
    assertEquals(a.equals(b), false);
    assertEquals(a.equals(c), false);
    assertEquals(a.equals(d), false);
    
    assertEquals(b.equals(a), false);
    assertEquals(b.equals(b), true);
    assertEquals(b.equals(c), false);
    assertEquals(b.equals(d), false);
    
    assertEquals(c.equals(a), false);
    assertEquals(c.equals(b), false);
    assertEquals(c.equals(c), true);
    assertEquals(c.equals(d), false);
    
    assertEquals(d.equals(a), false);
    assertEquals(d.equals(b), false);
    assertEquals(d.equals(c), false);
    assertEquals(d.equals(d), true);
  }

  @Test
  public void testEquals_null() {
    KeyValuePair<Integer, String> a = new KeyValuePair<Integer, String>(1, "Hello");
    KeyValuePair<Integer, String> b = new KeyValuePair<Integer, String>(null, "Hello");
    KeyValuePair<Integer, String> c = new KeyValuePair<Integer, String>(1, null);
    KeyValuePair<Integer, String> d = new KeyValuePair<Integer, String>(null, null);
    assertEquals(a.equals(a), true);
    assertEquals(a.equals(b), false);
    assertEquals(a.equals(c), false);
    assertEquals(a.equals(d), false);
    
    assertEquals(b.equals(a), false);
    assertEquals(b.equals(b), true);
    assertEquals(b.equals(c), false);
    assertEquals(b.equals(d), false);
    
    assertEquals(c.equals(a), false);
    assertEquals(c.equals(b), false);
    assertEquals(c.equals(c), true);
    assertEquals(c.equals(d), false);
    
    assertEquals(d.equals(a), false);
    assertEquals(d.equals(b), false);
    assertEquals(d.equals(c), false);
    assertEquals(d.equals(d), true);
  }

  @Test
  public void testHashCode() {
    KeyValuePair<Integer, String> a = new KeyValuePair<Integer, String>(1, "Hello");
    KeyValuePair<Integer, String> b = new KeyValuePair<Integer, String>(null, "Hello");
    KeyValuePair<Integer, String> c = new KeyValuePair<Integer, String>(1, null);
    KeyValuePair<Integer, String> d = new KeyValuePair<Integer, String>(null, null);
    assertEquals(a.hashCode(), a.hashCode());
    assertEquals(b.hashCode(), b.hashCode());
    assertEquals(c.hashCode(), c.hashCode());
    assertEquals(d.hashCode(), d.hashCode());
    
    assertEquals(a.hashCode(), 1 ^ "Hello".hashCode());
    assertEquals(b.hashCode(), "Hello".hashCode());
    assertEquals(c.hashCode(), 1);
    assertEquals(d.hashCode(), 0);
    // can't test for different hash codes as they might not be different
  }

}
