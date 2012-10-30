/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.value;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertSame;
import static org.testng.AssertJUnit.assertTrue;

import java.util.Arrays;
import java.util.Collections;

import org.testng.annotations.Test;

/**
 * Tests for the {@link ValueProperties} class.
 */
@Test
public class ValuePropertiesTest {

  public void testNone() {
    final ValueProperties none = ValueProperties.none();
    assertNotNull(none);
    assertTrue(none.isEmpty());
    assertNull(none.getProperties());
  }

  public void testAll() {
    final ValueProperties all = ValueProperties.all();
    assertNotNull(all);
    assertFalse(all.isEmpty());
    assertTrue(all.getProperties().isEmpty());
  }

  public void testWithCollection() {
    ValueProperties props = ValueProperties.with("A", Arrays.asList("1", "2")).get();
    assertEquals(2, props.getValues("A").size());
    assertTrue(props.getValues("A").contains("1"));
    assertTrue(props.getValues("A").contains("2"));
    assertFalse(props.getValues("A").contains("3"));
    props = ValueProperties.with("A", Arrays.asList("1", "2")).with("A", Arrays.asList("2", "3")).get();
    assertEquals(3, props.getValues("A").size());
    assertTrue(props.getValues("A").contains("1"));
    assertTrue(props.getValues("A").contains("2"));
    assertTrue(props.getValues("A").contains("3"));
    props = ValueProperties.with("A", Arrays.asList("1", "2")).withAny("A").get();
    assertTrue(props.getValues("A").isEmpty());
    props = ValueProperties.with("A", Arrays.asList("1", "2")).withAny("A").with("A", Arrays.asList("2", "3")).get();
    assertTrue(props.getValues("A").isEmpty());
  }

  public void testWithSingle() {
    ValueProperties props = ValueProperties.with("A", "1").get();
    assertEquals(1, props.getValues("A").size());
    assertTrue(props.getValues("A").contains("1"));
    assertFalse(props.getValues("A").contains("2"));
    props = ValueProperties.with("A", "1").with("A", "2").get();
    assertEquals(2, props.getValues("A").size());
    assertTrue(props.getValues("A").contains("1"));
    assertTrue(props.getValues("A").contains("2"));
    props = ValueProperties.with("A", "1").withAny("A").get();
    assertTrue(props.getValues("A").isEmpty());
    props = ValueProperties.with("A", "1").withAny("A").with("A", "2").get();
    assertTrue(props.getValues("A").isEmpty());
  }

  public void testWithArray() {
    ValueProperties props = ValueProperties.with("A", "1", "2").get();
    assertEquals(2, props.getValues("A").size());
    assertTrue(props.getValues("A").contains("1"));
    assertTrue(props.getValues("A").contains("2"));
    assertFalse(props.getValues("A").contains("3"));
    props = ValueProperties.with("A", "1", "2").with("A", "2", "3").get();
    assertEquals(3, props.getValues("A").size());
    assertTrue(props.getValues("A").contains("1"));
    assertTrue(props.getValues("A").contains("2"));
    assertTrue(props.getValues("A").contains("3"));
    props = ValueProperties.with("A", "1", "2").withAny("A").get();
    assertTrue(props.getValues("A").isEmpty());
    props = ValueProperties.with("A", "1", "2").withAny("A").with("A", "2", "3").get();
    assertTrue(props.getValues("A").isEmpty());
  }

  public void testWithAny() {
    ValueProperties props = ValueProperties.withAny("A").get();
    assertTrue(props.getValues("A").isEmpty());
  }

  public void testWithOptional() {
    ValueProperties props = ValueProperties.withAny("A").get();
    assertTrue(props.getValues("A").isEmpty());
    assertFalse(props.isOptional("A"));
    props = ValueProperties.withOptional("A").get();
    assertTrue(props.getValues("A").isEmpty());
    assertTrue(props.isOptional("A"));
    props = ValueProperties.withOptional("A").with("A", "1").get();
    assertTrue(props.getValues("A").contains("1"));
    assertTrue(props.isOptional("A"));
    props = ValueProperties.with("A", "1").withOptional("A").get();
    assertTrue(props.getValues("A").contains("1"));
    assertTrue(props.isOptional("A"));
  }

  public void testIsSatisfiedBy() {
    final ValueProperties requirement = ValueProperties.with("A", "1").with("B", "2", "3").withAny("C").withOptional("D").with("E", "1").withOptional("E").get();
    assertTrue(requirement.isSatisfiedBy(requirement));
    assertTrue(requirement.isSatisfiedBy(ValueProperties.all()));
    assertFalse(requirement.isSatisfiedBy(ValueProperties.none()));
    assertTrue(ValueProperties.none().isSatisfiedBy(requirement));
    assertTrue(ValueProperties.none().isSatisfiedBy(ValueProperties.all()));
    assertFalse(ValueProperties.all().isSatisfiedBy(ValueProperties.none()));
    assertTrue(ValueProperties.all().isSatisfiedBy(ValueProperties.all()));
    assertTrue(requirement.isSatisfiedBy(ValueProperties.with("A", "1").with("B", "2", "3").withAny("C").get()));
    assertTrue(requirement.isSatisfiedBy(ValueProperties.withAny("A").with("B", "2", "3").withAny("C").get()));
    assertTrue(requirement.isSatisfiedBy(ValueProperties.with("A", "1").with("B", "2").withAny("C").get()));
    assertTrue(requirement.isSatisfiedBy(ValueProperties.with("A", "1").with("B", "2", "3").with("C", "1").get()));
    assertTrue(requirement.isSatisfiedBy(ValueProperties.with("A", "1").with("B", "2", "3").withAny("C").with("D", "4").get()));
    assertFalse(requirement.isSatisfiedBy(ValueProperties.with("B", "2", "3").withAny("C").get()));
    assertFalse(requirement.isSatisfiedBy(ValueProperties.with("A", "1").withAny("C").get()));
    assertFalse(requirement.isSatisfiedBy(ValueProperties.with("A", "1").with("B", "2", "3").get()));
    assertFalse(requirement.isSatisfiedBy(ValueProperties.with("A", "5").with("B", "2", "3").withAny("C").get()));
    assertFalse(requirement.isSatisfiedBy(ValueProperties.with("A", "1").with("B", "6", "7").withAny("C").get()));
    assertTrue(requirement.isSatisfiedBy(ValueProperties.withAny("A").withAny("B").withAny("C").with("E", "1").get()));
    assertTrue(requirement.isSatisfiedBy(ValueProperties.withAny("A").withAny("B").withAny("C").withAny("E").get()));
    assertFalse(requirement.isSatisfiedBy(ValueProperties.withAny("A").withAny("B").withAny("C").with("E", "2").get()));
  }

  public void testCompose() {
    final ValueProperties requirement = ValueProperties.with("A", "1").with("B", "2", "3").withAny("C").withOptional("D").with("E", "1").withOptional("E").get();
    ValueProperties offering = ValueProperties.with("A", "1").with("B", "2", "3").withAny("C").get();
    ValueProperties props = offering.compose(requirement);
    assertSame(offering, props);
    props = offering.compose(ValueProperties.none());
    assertSame(offering, props);
    props = offering.compose(ValueProperties.all());
    assertSame(offering, props);
    offering = ValueProperties.with("A", "1").with("B", "2", "3").with("C", "1").get();
    props = offering.compose(requirement);
    assertSame(offering, props);
    offering = ValueProperties.with("A", "1").with("B", "2", "3").withAny("C").with("D", "4").get();
    props = offering.compose(requirement);
    assertSame(offering, props);
    offering = ValueProperties.with("A", "1", "2").with("B", "2", "4").with("C", "1").withAny("D").get();
    props = offering.compose(requirement);
    assertEquals(requirement.getValues("A"), props.getValues("A"));
    assertEquals(Collections.singleton("2"), props.getValues("B"));
    assertSame(offering.getValues("C"), props.getValues("C"));
    assertSame(offering.getValues("D"), props.getValues("D"));
    offering = ValueProperties.withAny("A").withAny("B").withAny("C").get();
    props = offering.compose(requirement);
    assertSame(requirement.getValues("A"), props.getValues("A"));
    assertSame(requirement.getValues("B"), props.getValues("B"));
    assertSame(requirement.getValues("C"), props.getValues("C"));
    assertNull(props.getValues("D"));
    assertNull(props.getValues("E"));
    offering = ValueProperties.withAny("A").withAny("B").withAny("C").with("E", "1").get();
    props = offering.compose(requirement);
    assertSame(requirement.getValues("A"), props.getValues("A"));
    assertSame(requirement.getValues("B"), props.getValues("B"));
    assertSame(requirement.getValues("C"), props.getValues("C"));
    assertSame(offering.getValues("E"), props.getValues("E"));
    offering = ValueProperties.with("A", "1").with("B", "2", "3").withOptional("C").withOptional("D").with("E", "1").withOptional("E").get();
    props = offering.compose(requirement);
    assertEquals(requirement, props);
    assertFalse(offering.equals(props));
  }
  
  public void testIntersect() {
    assertSame (ValueProperties.none (), ValueProperties.all ().intersect (ValueProperties.none ()));
    assertSame (ValueProperties.none (), ValueProperties.none ().intersect (ValueProperties.all ()));
    final ValueProperties a = ValueProperties.with("A", "1").with("B", "2", "3").withAny ("C").withOptional("D").with("E", "1").withOptional("E").get ();
    assertSame (ValueProperties.none (), a.intersect (ValueProperties.none ()));
    assertSame (ValueProperties.none (), ValueProperties.none ().intersect (a));
    assertSame (a, a.intersect (a));
    final ValueProperties aNoOpt = ValueProperties.with("A", "1").with("B", "2", "3").withAny ("C").withAny("D").with("E", "1").get ();
    assertEquals (aNoOpt, a.intersect (ValueProperties.all ()));
    assertEquals (aNoOpt, ValueProperties.all().intersect (a));
    final ValueProperties b = ValueProperties.with("A", "1").with("B", "2", "4").with ("C", "3", "4").withAny("D").withAny("E").withOptional("E").get ();
    final ValueProperties ab = ValueProperties.with ("A", "1").with ("B", "2").with ("C", "3", "4").withAny ("D").with("E", "1").withOptional ("E").get ();
    assertEquals (ab, a.intersect (b));
    assertEquals (ab, b.intersect (a));
    assertEquals (ab, a.intersect (ab));
    assertEquals (ab, b.intersect (ab));
    assertSame (ab, ab.intersect (a));
    assertSame (ab, ab.intersect (b));
    assertSame (ab, ab.intersect (ab));
  }

  public void testEquals() {
    final ValueProperties requirement1 = ValueProperties.with("A", "1").with("B", "2", "3").get();
    final ValueProperties requirement2 = ValueProperties.with("A", "1").with("B", "3").get();
    final ValueProperties requirement3 = ValueProperties.with("B", "2", "3").get();
    final ValueProperties requirement4 = ValueProperties.withOptional("A").with("A", "1").with("B", "2", "3").get();
    final ValueProperties requirement5 = requirement1.copy().get();
    final ValueProperties requirement6 = requirement2.copy().with("B", "2").get();
    final ValueProperties requirement7 = requirement1.copy().withOptional("A").get();
    assertTrue(requirement1.equals(requirement1));
    assertFalse(requirement1.equals(requirement2));
    assertFalse(requirement1.equals(requirement3));
    assertFalse(requirement1.equals(requirement4));
    assertTrue(requirement1.equals(requirement5));
    assertTrue(requirement1.equals(requirement6));
    assertFalse(requirement1.equals(requirement7));
    assertTrue(requirement1.equals(requirement1));
    assertFalse(requirement2.equals(requirement1));
    assertFalse(requirement3.equals(requirement1));
    assertFalse(requirement4.equals(requirement1));
    assertTrue(requirement5.equals(requirement1));
    assertTrue(requirement6.equals(requirement1));
    assertFalse(requirement7.equals(requirement1));
    assertFalse(requirement2.equals(requirement6));
    assertFalse(requirement6.equals(requirement2));
    assertTrue(requirement4.equals(requirement7));
    assertTrue(requirement7.equals(requirement4));
  }

  private static void compare(final ValueProperties lesser, final ValueProperties greater) {
    assertEquals(-1, lesser.compareTo(greater));
    assertEquals(1, greater.compareTo(lesser));
  }

  public void testCompareTo() {
    final ValueProperties empty = ValueProperties.none();
    final ValueProperties all = ValueProperties.all();
    final ValueProperties allBarFoo = all.withoutAny("Foo");
    final ValueProperties allBarBar = all.withoutAny("Bar");
    final ValueProperties allBarFooBar = allBarFoo.withoutAny("Bar");
    final ValueProperties oneFoo = ValueProperties.with("Foo", "1").get();
    final ValueProperties oneBar = ValueProperties.with("Bar", "1").get();
    final ValueProperties oneFooAnyBar = ValueProperties.with("Foo", "1").withAny("Bar").get();
    final ValueProperties oneBarAnyFoo = ValueProperties.with("Bar", "1").withAny("Foo").get();
    compare(empty, all);
    compare(allBarFoo, all);
    compare(allBarBar, all);
    compare(allBarBar, allBarFoo);
    compare(allBarFoo, allBarFooBar);
    compare(allBarBar, allBarFooBar);
    compare(oneFoo, allBarBar);
    compare(oneFoo, all);
    compare(oneBar, oneFoo);
    compare(oneFoo, oneFooAnyBar);
    compare(oneBar, oneFooAnyBar);
    compare(oneFoo, oneBarAnyFoo);
    compare(oneBar, oneBarAnyFoo);
    compare(oneBarAnyFoo, oneFooAnyBar);
  }
  
  public void testParseCycle() {
    parseCycle(ValueProperties.none());
    parseCycle(ValueProperties.all());
    parseCycle(ValueProperties.with("Foo", "1").get());
    parseCycle(ValueProperties.with("Foo", "1").with("Bar", "456").get());
    parseCycle(ValueProperties.with("Foo", "1").withAny("Bar").get());
    parseCycle(ValueProperties.with("Foo", "1").withOptional("Bar").get());
    
    parseCycle(ValueProperties.all().withoutAny("ABC"));
    parseCycle(ValueProperties.all().withoutAny("ABC").withoutAny("DEF"));
  }
  
  public void testParseFlexibility() {   
    assertEquals(ValueProperties.with("Ccy", "USD").get(), ValueProperties.parse("Ccy=USD"));
    assertEquals(ValueProperties.with("Ccy", "USD", "GBP").get(), ValueProperties.parse("Ccy=[GBP,USD]"));
    
    assertEquals(ValueProperties.withAny("Foo").get(), ValueProperties.parse("Foo"));
    
    ValueProperties twoFooOneBar = ValueProperties.with("Foo", "123", "456").with("Bar", "7").get();
    assertEquals(twoFooOneBar, ValueProperties.parse("Foo=[123,456],Bar=7"));
    assertEquals(twoFooOneBar, ValueProperties.parse("Foo=[123, 456],Bar=7"));
    assertEquals(twoFooOneBar, ValueProperties.parse("Foo=[123,456], Bar=7"));
    assertEquals(twoFooOneBar, ValueProperties.parse("Bar=7, Foo=[123,456]"));
    
    assertEquals(ValueProperties.withOptional("Foo").get(), ValueProperties.parse("Foo=[]?"));
    assertEquals(ValueProperties.with("Foo", "1").withOptional("Foo").get(), ValueProperties.parse("Foo=[1]?"));
    
    ValueProperties oneOptionalFooTwoBar = ValueProperties.with("Foo", "123").withOptional("Foo").with("Bar", "7", "8").get();
    assertEquals(oneOptionalFooTwoBar, ValueProperties.parse("Foo=[123]?,Bar=[7,8]"));
    assertEquals(oneOptionalFooTwoBar, ValueProperties.parse("Bar=[7,8],Foo=[123]?"));

    assertEquals(ValueProperties.withAny("ValueName").get(), ValueProperties.parse("ValueName="));
    assertEquals(ValueProperties.withAny("ValueName").get(), ValueProperties.parse("ValueName=[]"));
    
    ValueProperties allButFoo = ValueProperties.all().withoutAny("Foo");
    assertEquals(allButFoo, ValueProperties.parse("INFINITE-{Foo}"));
    assertEquals(allButFoo, ValueProperties.parse("INFINITE-{ Foo }"));
    assertEquals(allButFoo, ValueProperties.parse("INFINITE-{ Foo=[] }"));
    assertEquals(allButFoo, ValueProperties.parse("INFINITE-Foo"));
    assertEquals(allButFoo, ValueProperties.parse("INFINITE- Foo"));
  }
  
  public void testCharacterEscaping() {
    assertEquals(ValueProperties.with("[", " ]").get(), ValueProperties.parse("\\[=\\ \\]"));
    assertEquals(ValueProperties.with(",", "=").get(), ValueProperties.parse("\\,=\\="));
  }
  

  public void testWithWithoutIsNone() {
    final ValueProperties none = ValueProperties.none();
    final ValueProperties withWithout = ValueProperties.builder().with("A", "B").withoutAny("A").get();
    assertEquals(none, withWithout);
    assertEquals(withWithout, none);
    
    final ValueProperties withOptionalWithout = ValueProperties.builder().with("A", "B").withOptional("A").withoutAny("A").get();
    assertEquals(none, withOptionalWithout);
    assertEquals(withOptionalWithout, none);
  }
  

  
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testParseInvalidNoValue() {
    ValueProperties.parse("ValueName=[");
  }
  
  private static void parseCycle(ValueProperties original) {
    String vpString = original.toString();
    ValueProperties parsed = ValueProperties.parse(vpString);
    assertEquals(original, parsed);
  }

}
