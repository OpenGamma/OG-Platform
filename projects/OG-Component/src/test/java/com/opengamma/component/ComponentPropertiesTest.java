/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.component;

import static org.testng.AssertJUnit.assertEquals;

import java.util.Map;

import org.testng.annotations.Test;

import com.google.common.collect.ImmutableMap;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class ComponentPropertiesTest {

  public void test_basics_empty() {
    ConfigProperties test = new ConfigProperties();
    assertEquals(0, test.size());
    assertEquals(null, test.get("x"));
    assertEquals(null, test.getValue("x"));
    assertEquals(false, test.containsKey("x"));
    assertEquals(false, test.containsKey(null));
    assertEquals(0, test.keySet().size());
    assertEquals(0, test.toMap().size());
    assertEquals(0, test.loggableMap().size());
    assertEquals(null, test.loggableValue("x"));
  }

  public void test_basics() {
    ConfigProperties test = new ConfigProperties();
    test.add(ConfigProperty.of("a", "AA", false));
    assertEquals(1, test.size());
    assertEquals(ConfigProperty.of("a", "AA", false), test.get("a"));
    assertEquals("AA", test.getValue("a"));
    assertEquals(true, test.containsKey("a"));
    assertEquals(1, test.toMap().size());
    assertEquals("AA", test.toMap().get("a"));
    assertEquals(1, test.loggableMap().size());
    assertEquals("AA", test.loggableMap().get("a"));
    assertEquals("AA", test.loggableValue("a"));
    assertEquals(true, test.toString().contains("AA"));
    
    test.add(ConfigProperty.of("b", "BB", false));
    assertEquals(2, test.size());
    assertEquals(ConfigProperty.of("a", "AA", false), test.get("a"));
    assertEquals("AA", test.getValue("a"));
    assertEquals(true, test.containsKey("a"));
    assertEquals(ConfigProperty.of("b", "BB", false), test.get("b"));
    assertEquals("BB", test.getValue("b"));
    assertEquals(true, test.containsKey("b"));
    assertEquals(2, test.toMap().size());
    assertEquals("AA", test.toMap().get("a"));
    assertEquals("BB", test.toMap().get("b"));
    
    test.add(ConfigProperty.of("b", "CC", false));
    assertEquals(2, test.size());
    assertEquals("AA", test.getValue("a"));
    assertEquals("CC", test.getValue("b"));
    
    test.addIfAbsent(ConfigProperty.of("b", "DD", false));
    assertEquals(2, test.size());
    assertEquals("AA", test.getValue("a"));
    assertEquals("CC", test.getValue("b"));
    
    test.addIfAbsent(ConfigProperty.of("e", "EE", false));
    assertEquals(3, test.size());
    assertEquals("AA", test.getValue("a"));
    assertEquals("CC", test.getValue("b"));
    assertEquals("EE", test.getValue("e"));
  }

  public void test_basics_put() {
    ConfigProperties test = new ConfigProperties();
    test.put("a", "AA");
    assertEquals(1, test.size());
    assertEquals(ConfigProperty.of("a", "AA", false), test.get("a"));
    
    test.put("a", "BB");
    assertEquals(1, test.size());
    assertEquals(ConfigProperty.of("a", "BB", false), test.get("a"));
    
    Map<String, String> map = ImmutableMap.of("a", "CC", "e", "EE");
    test.putAll(map);
    assertEquals(2, test.size());
    assertEquals(ConfigProperty.of("a", "CC", false), test.get("a"));
    assertEquals(ConfigProperty.of("e", "EE", false), test.get("e"));
  }

  public void test_basics_hidden() {
    ConfigProperties test = new ConfigProperties();
    ConfigProperty resolved = test.resolveProperty("password", "abc", 0);
    assertEquals(ConfigProperty.of("password", "abc", true), resolved);
    test.add(resolved);
    assertEquals(1, test.size());
    assertEquals(ConfigProperty.of("password", "abc", true), test.get("password"));
    assertEquals("abc", test.getValue("password"));
    assertEquals(1, test.toMap().size());
    assertEquals("abc", test.toMap().get("password"));
    assertEquals(1, test.loggableMap().size());
    assertEquals(ConfigProperties.HIDDEN, test.loggableMap().get("password"));
    assertEquals(ConfigProperties.HIDDEN, test.loggableValue("password"));
    assertEquals(false, test.toString().contains("abc"));
  }

  public void test_basics_hidden_resolve() {
    ConfigProperties test = new ConfigProperties();
    test.add(ConfigProperty.of("password", "abc", true));
    ConfigProperty resolved = test.resolveProperty("other", "pw:${password}", 0);
    assertEquals(ConfigProperty.of("other", "pw:abc", true), resolved);
    test.add(resolved);
    assertEquals(2, test.size());
    assertEquals(ConfigProperty.of("password", "abc", true), test.get("password"));
    assertEquals(ConfigProperty.of("other", "pw:abc", true), test.get("other"));
    assertEquals("abc", test.getValue("password"));
    assertEquals("pw:abc", test.getValue("other"));
    assertEquals(2, test.toMap().size());
    assertEquals("abc", test.toMap().get("password"));
    assertEquals("pw:abc", test.toMap().get("other"));
    assertEquals(2, test.loggableMap().size());
    assertEquals(ConfigProperties.HIDDEN, test.loggableMap().get("password"));
    assertEquals(ConfigProperties.HIDDEN, test.loggableMap().get("other"));
    assertEquals(ConfigProperties.HIDDEN, test.loggableValue("password"));
    assertEquals(ConfigProperties.HIDDEN, test.loggableValue("other"));
    assertEquals(false, test.toString().contains("abc"));
  }

  //-------------------------------------------------------------------------
  public void test_property_withKey() {
    ConfigProperty base = ConfigProperty.of("a", "AA", false);
    ConfigProperty test = base.withKey("b");
    assertEquals(ConfigProperty.of("b", "AA", false), test);
  }

  public void test_property_equals() {
    ConfigProperty a1 = ConfigProperty.of("a", "AA", false);
    ConfigProperty a2 = ConfigProperty.of("a", "AA", false);
    ConfigProperty b = ConfigProperty.of("b", "AA", false);
    ConfigProperty c = ConfigProperty.of("a", "BB", false);
    ConfigProperty d1 = ConfigProperty.of("a", "AA", true);
    ConfigProperty d2 = ConfigProperty.of("a", "AA", true);
    assertEquals(true, a1.equals(a1));
    assertEquals(true, a1.equals(a2));
    assertEquals(false, a1.equals(b));
    assertEquals(false, a1.equals(c));
    assertEquals(false, a1.equals(d1));
    assertEquals(false, a1.equals(null));
    assertEquals(false, a1.equals(""));
    
    assertEquals(true, a1.hashCode() == a2.hashCode());
    assertEquals(true, d1.hashCode() == d2.hashCode());
  }

  public void test_property_toString() {
    assertEquals("a=AA", ConfigProperty.of("a", "AA", false).toString());
    assertEquals("a=" + ConfigProperties.HIDDEN, ConfigProperty.of("a", "AA", true).toString());
  }

}
