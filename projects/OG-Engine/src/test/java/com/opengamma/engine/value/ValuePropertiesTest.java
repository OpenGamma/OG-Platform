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

import com.opengamma.util.test.TestGroup;

/**
 * Tests for the {@link ValueProperties} class.
 */
@Test(groups = TestGroup.UNIT)
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
    assertEquals(offering.getValues("C"), props.getValues("C"));
    assertSame(offering.getValues("D"), props.getValues("D"));
    offering = ValueProperties.withAny("A").withAny("B").withAny("C").get();
    props = offering.compose(requirement);
    assertEquals(requirement.getValues("A"), props.getValues("A"));
    assertEquals(requirement.getValues("B"), props.getValues("B"));
    assertSame(requirement.getValues("C"), props.getValues("C"));
    assertNull(props.getValues("D"));
    assertNull(props.getValues("E"));
    offering = ValueProperties.withAny("A").withAny("B").withAny("C").with("E", "1").get();
    props = offering.compose(requirement);
    assertEquals(requirement.getValues("A"), props.getValues("A"));
    assertEquals(requirement.getValues("B"), props.getValues("B"));
    assertSame(requirement.getValues("C"), props.getValues("C"));
    assertEquals(offering.getValues("E"), props.getValues("E"));
    offering = ValueProperties.with("A", "1").with("B", "2", "3").withOptional("C").withOptional("D").with("E", "1").withOptional("E").get();
    props = offering.compose(requirement);
    assertEquals(requirement, props);
    assertFalse(offering.equals(props));
  }

  public void testIntersect() {
    assertSame(ValueProperties.none(), ValueProperties.all().intersect(ValueProperties.none()));
    assertSame(ValueProperties.none(), ValueProperties.none().intersect(ValueProperties.all()));
    final ValueProperties a = ValueProperties.with("A", "1").with("B", "2", "3").withAny("C").withOptional("D").with("E", "1").withOptional("E").get();
    assertSame(ValueProperties.none(), a.intersect(ValueProperties.none()));
    assertSame(ValueProperties.none(), ValueProperties.none().intersect(a));
    assertSame(a, a.intersect(a));
    final ValueProperties aNoOpt = ValueProperties.with("A", "1").with("B", "2", "3").withAny("C").withAny("D").with("E", "1").get();
    assertEquals(aNoOpt, a.intersect(ValueProperties.all()));
    assertEquals(aNoOpt, ValueProperties.all().intersect(a));
    final ValueProperties b = ValueProperties.with("A", "1").with("B", "2", "4").with("C", "3", "4").withAny("D").withAny("E").withOptional("E").get();
    final ValueProperties ab = ValueProperties.with("A", "1").with("B", "2").with("C", "3", "4").withAny("D").with("E", "1").withOptional("E").get();
    assertEquals(ab, a.intersect(b));
    assertEquals(ab, b.intersect(a));
    assertSame(ab, a.intersect(ab));
    assertSame(ab, b.intersect(ab));
    assertEquals(ab, ab.intersect(a));
    assertEquals(ab, ab.intersect(b));
    assertSame(ab, ab.intersect(ab));
    final ValueProperties c = ValueProperties.all().withoutAny("A");
    assertEquals(ValueProperties.with("B", "2", "3").withAny("C").withAny("D").with("E", "1").get(), a.intersect(c));
    assertEquals(ValueProperties.with("B", "2", "3").withAny("C").withAny("D").with("E", "1").get(), c.intersect(a));
    assertSame(c, c.intersect(ValueProperties.all()));
    assertSame(c, ValueProperties.all().intersect(c));
    final ValueProperties d = ValueProperties.all().withoutAny("D");
    assertEquals(ValueProperties.all().withoutAny("A").withoutAny("D"), c.intersect(d));
    assertEquals(ValueProperties.all().withoutAny("A").withoutAny("D"), d.intersect(c));
  }

  public void testUnion() {
    assertSame(ValueProperties.all(), ValueProperties.all().union(ValueProperties.none()));
    assertSame(ValueProperties.all(), ValueProperties.none().union(ValueProperties.all()));
    final ValueProperties a = ValueProperties.with("A", "1").with("B", "2", "3").withAny("C").withOptional("D").with("E", "1").withOptional("E").get();
    assertSame(a, a.union(ValueProperties.none()));
    assertSame(a, ValueProperties.none().union(a));
    assertSame(ValueProperties.all(), a.union(ValueProperties.all()));
    assertSame(ValueProperties.all(), ValueProperties.all().union(a));
    assertSame(a, a.union(a));
    final ValueProperties b = ValueProperties.with("A", "1").with("B", "2", "4").with("C", "3", "4").withAny("D").withAny("E").withOptional("E").get();
    final ValueProperties ab = ValueProperties.with("A", "1").with("B", "2", "3", "4").withAny("C").withAny("D").withAny("E").withOptional("E").get();
    assertEquals(ab, a.union(b));
    assertEquals(ab, b.union(a));
    final ValueProperties c = ValueProperties.all().withoutAny("C");
    assertSame(ValueProperties.all(), a.union(c));
    assertSame(ValueProperties.all(), c.union(a));
    final ValueProperties d = ValueProperties.all().withoutAny("D").withoutAny("Y");
    assertSame(ValueProperties.all(), c.union(d));
    assertSame(ValueProperties.all(), d.union(c));
    final ValueProperties e = ValueProperties.all().withoutAny("C").withoutAny("D");
    assertEquals(ValueProperties.all().withoutAny("D"), d.union(e));
    final ValueProperties f = ValueProperties.with("X", "A").get();
    final ValueProperties g = ValueProperties.with("Y", "B").get();
    final ValueProperties fg = ValueProperties.with("X", "A").with("Y", "B").get();
    assertEquals(fg, f.union(g));
    assertEquals(fg, g.union(f));
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
    final int c1 = lesser.compareTo(greater);
    final int c2 = greater.compareTo(lesser);
    final String message = ("lesser = " + c1 + ", greater = " + c2);
    assertTrue(message, lesser.compareTo(greater) < 0);
    assertTrue(message, greater.compareTo(lesser) > 0);
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
    parseCycle(ValueProperties.with("Foo", "1").with("Bar", "45 6").get());
    parseCycle(ValueProperties.with("Foo", " 1").withAny("Bar ").get());
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
    assertEquals(ValueProperties.with("F  oo", "45 6").get(), ValueProperties.parse(" F  oo = [ 45 6 ]"));
  }

  public void testWithWithoutIsNone() {
    final ValueProperties none = ValueProperties.none();
    final ValueProperties withWithout = ValueProperties.builder().with("A", "B").withoutAny("A").get();
    assertSame(none, withWithout);
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

  public void testBuilderCopy() {
    final ValueProperties.Builder builder1 = ValueProperties.with("Foo", "Bar");
    final ValueProperties.Builder builder2 = builder1.copy();
    builder1.with("A", "B");
    builder2.with("X", "Y");
    assertEquals(builder1.get(), ValueProperties.with("Foo", "Bar").with("A", "B").get());
    assertEquals(builder2.get(), ValueProperties.with("Foo", "Bar").with("X", "Y").get());
    builder1.withoutAny("Foo");
    assertEquals(builder1.get(), ValueProperties.with("A", "B").get());
    assertEquals(builder2.get(), ValueProperties.with("Foo", "Bar").with("X", "Y").get());
    builder2.withoutAny("Foo");
    assertEquals(builder2.get(), ValueProperties.with("X", "Y").get());
  }

}
