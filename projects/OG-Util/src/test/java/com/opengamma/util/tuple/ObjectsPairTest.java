/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.tuple;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

import java.util.List;

import org.joda.beans.ImmutableBean;
import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;

/**
 * Test ObjectsPair.
 */
@Test(groups = TestGroup.UNIT)
public class ObjectsPairTest {

  public void testOf_Object_Object() {
    ObjectsPair<String, String> test = ObjectsPair.of("A", "B");
    assertEquals(test.getFirst(), "A");
    assertEquals(test.getSecond(), "B");
  }

  public void testObjectsPair_Object_null() {
    ObjectsPair<String, String> test = ObjectsPair.of("A", null);
    assertEquals(test.getFirst(), "A");
    assertEquals(test.getSecond(), null);
  }

  public void testObjectsPair_null_Object() {
    ObjectsPair<String, String> test = ObjectsPair.of(null, "B");
    assertEquals(test.getFirst(), null);
    assertEquals(test.getSecond(), "B");
  }

  public void testObjectsPair_null_null_null() {
    ObjectsPair<String, String> test = ObjectsPair.of(null, null);
    assertEquals(test.getFirst(), null);
    assertEquals(test.getSecond(), null);
  }

  //-------------------------------------------------------------------------
  public void compareTo() {
    ObjectsPair<String, String> ab = ObjectsPair.of("A", "B");
    ObjectsPair<String, String> ad = ObjectsPair.of("A", "D");
    ObjectsPair<String, String> ba = ObjectsPair.of("B", "A");
    
    assertTrue(ab.compareTo(ab) == 0);
    assertTrue(ab.compareTo(ad) < 0);
    assertTrue(ab.compareTo(ba) < 0);
    
    assertTrue(ad.compareTo(ab) > 0);
    assertTrue(ad.compareTo(ad) == 0);
    assertTrue(ad.compareTo(ba) < 0);
    
    assertTrue(ba.compareTo(ab) > 0);
    assertTrue(ba.compareTo(ad) > 0);
    assertTrue(ba.compareTo(ba) == 0);
  }

  public void compareTo_null() {
    ObjectsPair<String, String> nn = ObjectsPair.of(null, null);
    ObjectsPair<String, String> na = ObjectsPair.of(null, "A");
    ObjectsPair<String, String> an = ObjectsPair.of("A", null);
    ObjectsPair<String, String> aa = ObjectsPair.of("A", "A");
    
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

  public void testEquals() {
    ObjectsPair<Integer, String> a = ObjectsPair.of(1, "Hello");
    ObjectsPair<Integer, String> b = ObjectsPair.of(1, "Goodbye");
    ObjectsPair<Integer, String> c = ObjectsPair.of(2, "Hello");
    ObjectsPair<Integer, String> d = ObjectsPair.of(2, "Goodbye");
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
    
    assertEquals(a.equals("RUBBISH"), false);
  }

  public void testEquals_null() {
    ObjectsPair<Integer, String> a = ObjectsPair.of(Integer.valueOf(1), "Hello");
    ObjectsPair<Integer, String> b = ObjectsPair.of(null, "Hello");
    ObjectsPair<Integer, String> c = ObjectsPair.of(Integer.valueOf(1), null);
    ObjectsPair<Integer, String> d = ObjectsPair.of(null, null);
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

  public void testHashCode() {
    ObjectsPair<Integer, String> a = ObjectsPair.of(Integer.valueOf(1), "Hello");
    ObjectsPair<Integer, String> b = ObjectsPair.of(null, "Hello");
    ObjectsPair<Integer, String> c = ObjectsPair.of(Integer.valueOf(1), null);
    ObjectsPair<Integer, String> d = ObjectsPair.of(null, null);
    assertEquals(a.hashCode(), a.hashCode());
    assertEquals(b.hashCode(), b.hashCode());
    assertEquals(c.hashCode(), c.hashCode());
    assertEquals(d.hashCode(), d.hashCode());
    // can't test for different hash codes as they might not be different
  }

  public void toList() {
    ObjectsPair<String, String> a = ObjectsPair.of("Jay-Z", "Black Album");
    List<String> asList = a.toList();
    assertNotNull(asList);
    assertEquals(2, asList.size());
    assertEquals("Jay-Z", asList.get(0));
    assertEquals("Black Album", asList.get(1));
  }

  public void test_toString() {
    ObjectsPair<String, String> test = ObjectsPair.of("A", "B");
    assertEquals("[A, B]", test.toString());
  }

  public void bean() {
    ObjectsPair<String, String> triple = ObjectsPair.of("Jay-Z", "Black Album");
    assertTrue(triple instanceof ImmutableBean);
    assertNotNull(triple.metaBean());
    assertNotNull(triple.metaBean().first());
    assertNotNull(triple.metaBean().second());
    assertEquals("Jay-Z", triple.metaBean().first().get(triple));
    assertEquals("Black Album", triple.metaBean().second().get(triple));
    assertEquals("Jay-Z", triple.property("first").get());
    assertEquals("Black Album", triple.property("second").get());
  }

}
