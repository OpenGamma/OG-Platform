package com.opengamma.util;

import static org.junit.Assert.*;

import org.junit.Test;

public class KeyValuePairTest {
  @Test
  public void testHashCode() {
    assertEquals(new KeyValuePair<Integer, String>(1, "Hello").hashCode(), 
                 new KeyValuePair<Integer, String>(1, "Hello").hashCode());
    assertTrue(new KeyValuePair<Integer, String>(1, "Hello").hashCode() != 
               new KeyValuePair<Integer, String>(2, "Goodbye").hashCode());
    assertTrue(new KeyValuePair<Integer, String>(1, "Hello").hashCode() != 
               new KeyValuePair<Integer, String>(1, "Goodbye").hashCode());
    assertTrue(new KeyValuePair<Integer, String>(1, "Hello").hashCode() != 
               new KeyValuePair<Integer, String>(2, "Hello").hashCode());
  }

  @Test
  public void testKeyValuePair() {
    new KeyValuePair<String, String>(null, null);
    new KeyValuePair<String, String>(null, "Value");
    new KeyValuePair<String, String>("Key", null);
    new KeyValuePair<String, String>("Key", "Value");
  }

  @Test
  public void testGetKey() {
    KeyValuePair<String, String> pair = new KeyValuePair<String, String>("One", "Two");
    assertEquals(pair.getKey(), "One");
  }

  @Test
  public void testGetValue() {
    KeyValuePair<String, String> pair = new KeyValuePair<String, String>("One", "Two");
    assertEquals(pair.getValue(), "Two");
  }

  @Test
  public void testSetValue() {
    KeyValuePair<String,String> pair = new KeyValuePair<String, String>("One", "Two");
    pair.setValue("Three");
    assertEquals(pair.getValue(), "Three");
  }

  @Test
  public void testEqualsObject() {
    assertEquals(new KeyValuePair<Integer, String>(1, "Hello"),
                 new KeyValuePair<Integer, String>(1, "Hello"));
    assertFalse(new KeyValuePair<Integer, String>(1, "Hello").equals(
                new KeyValuePair<Integer, String>(1, "Goodbye")));
    assertFalse(new KeyValuePair<Integer, String>(1, "Hello").equals(
                new KeyValuePair<Integer, String>(2, "Hello")));
    assertFalse(new KeyValuePair<Integer, String>(1, "Hello").equals(
                new KeyValuePair<Integer, String>(2, "Goodbye")));
  }

}
