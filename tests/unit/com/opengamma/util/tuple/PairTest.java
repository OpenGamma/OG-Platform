/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.util.tuple;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.opengamma.util.tuple.Pair;

/**
 * Test Pair.
 */
public class PairTest {

  @Test
  public void testOf_Object_Object() {
    Pair<String, String> test = Pair.of("A", "B");
    assertEquals(test.getFirst(), "A");
    assertEquals(test.getSecond(), "B");
    assertEquals(test.getKey(), "A");
    assertEquals(test.getValue(), "B");
  }

  //-------------------------------------------------------------------------
  @Test
  public void testPair_Object_Object() {
    Pair<String, String> test = new Pair<String, String>("A", "B");
    assertEquals(test.getFirst(), "A");
    assertEquals(test.getSecond(), "B");
    assertEquals(test.getKey(), "A");
    assertEquals(test.getValue(), "B");
  }

  @Test
  public void testPair_Object_null() {
    Pair<String, String> test = new Pair<String, String>("A", null);
    assertEquals(test.getFirst(), "A");
    assertEquals(test.getSecond(), null);
    assertEquals(test.getKey(), "A");
    assertEquals(test.getValue(), null);
  }

  @Test
  public void testPair_null_Object() {
    Pair<String, String> test = new Pair<String, String>(null, "B");
    assertEquals(test.getFirst(), null);
    assertEquals(test.getSecond(), "B");
    assertEquals(test.getKey(), null);
    assertEquals(test.getValue(), "B");
  }

  @Test
  public void testPair_null_null() {
    Pair<String, String> test = new Pair<String, String>(null, null);
    assertEquals(test.getFirst(), null);
    assertEquals(test.getSecond(), null);
    assertEquals(test.getKey(), null);
    assertEquals(test.getValue(), null);
  }

  //-------------------------------------------------------------------------
  @Test(expected=UnsupportedOperationException.class)
  public void testSetValue() {
    Pair<String,String> pair = new Pair<String, String>("A", "B");
    pair.setValue("C");
  }

  @Test(expected=UnsupportedOperationException.class)
  public void testSetValue_null() {
    Pair<String,String> pair = new Pair<String, String>("A", "B");
    pair.setValue(null);
  }

  //-------------------------------------------------------------------------
  @Test
  public void compareTo() {
    Pair<String, String> ab = Pair.of("A", "B");
    Pair<String, String> ac = Pair.of("A", "C");
    Pair<String, String> ba = Pair.of("B", "A");
    
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

  @Test
  public void compareTo_null() {
    Pair<String, String> nn = Pair.of(null, null);
    Pair<String, String> na = Pair.of(null, "A");
    Pair<String, String> an = Pair.of("A", null);
    Pair<String, String> aa = Pair.of("A", "A");
    
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

  @Test
  public void testEquals() {
    Pair<Integer, String> a = new Pair<Integer, String>(1, "Hello");
    Pair<Integer, String> b = new Pair<Integer, String>(1, "Goodbye");
    Pair<Integer, String> c = new Pair<Integer, String>(2, "Hello");
    Pair<Integer, String> d = new Pair<Integer, String>(2, "Goodbye");
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
    Pair<Integer, String> a = new Pair<Integer, String>(1, "Hello");
    Pair<Integer, String> b = new Pair<Integer, String>(null, "Hello");
    Pair<Integer, String> c = new Pair<Integer, String>(1, null);
    Pair<Integer, String> d = new Pair<Integer, String>(null, null);
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
    Pair<Integer, String> a = new Pair<Integer, String>(1, "Hello");
    Pair<Integer, String> b = new Pair<Integer, String>(null, "Hello");
    Pair<Integer, String> c = new Pair<Integer, String>(1, null);
    Pair<Integer, String> d = new Pair<Integer, String>(null, null);
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
