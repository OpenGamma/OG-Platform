/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.value;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Test;

/**
 * Tests for the {@link ValueProperties} class.
 */
public class ValuePropertiesTest {

  @Test
  public void testNone() {
    final ValueProperties none = ValueProperties.none();
    assertNotNull(none);
    assertTrue(none.isEmpty());
    assertNull(none.getProperties());
  }
  
  @Test
  public void testAll () {
    final ValueProperties all = ValueProperties.all ();
    assertNotNull (all);
    assertFalse(all.isEmpty ());
    assertTrue(all.getProperties().isEmpty ());
  }

  @Test
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

  @Test
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

  @Test
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

  @Test
  public void testWithAny() {
    ValueProperties props = ValueProperties.withAny("A").get();
    assertTrue(props.getValues("A").isEmpty());
  }
  
  @Test
  public void testWithOptional () {
    ValueProperties props = ValueProperties.withAny("A").get();
    assertTrue(props.getValues("A").isEmpty ());
    assertFalse(props.isOptional ("A"));
    props = ValueProperties.withOptional("A").get ();
    assertTrue(props.getValues("A").isEmpty ());
    assertTrue(props.isOptional ("A"));
    props = ValueProperties.withOptional("A").with ("A", "1").get ();
    assertTrue(props.getValues("A").contains ("1"));
    assertTrue(props.isOptional ("A"));
    props = ValueProperties.with("A", "1").withOptional("A").get ();
    assertTrue(props.getValues("A").contains ("1"));
    assertTrue(props.isOptional ("A"));
  }
  
  @Test
  public void testIsSatisfiedBy() {
    final ValueProperties requirement = ValueProperties.with("A", "1").with("B", "2", "3").withAny("C").withOptional ("D").with("E", "1").withOptional("E").get();
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
    assertTrue(requirement.isSatisfiedBy(ValueProperties.withAny("A").withAny("B").withAny("C").with ("E", "1").get ()));
    assertTrue(requirement.isSatisfiedBy(ValueProperties.withAny("A").withAny("B").withAny("C").withAny ("E").get ()));
    assertFalse(requirement.isSatisfiedBy(ValueProperties.withAny("A").withAny("B").withAny("C").with ("E", "2").get ()));
  }

  @Test
  public void testCompose() {
    final ValueProperties requirement = ValueProperties.with("A", "1").with("B", "2", "3").withAny("C").withOptional("D").with ("E", "1").withOptional("E").get();
    ValueProperties offering = ValueProperties.with ("A", "1").with ("B", "2", "3").withAny ("C").get ();
    ValueProperties props = offering.compose(requirement);
    assertSame (offering, props);
    props = offering.compose(ValueProperties.none ());
    assertSame (offering, props);
    props = offering.compose(ValueProperties.all ());
    assertSame (offering, props);
    offering = ValueProperties.with ("A", "1").with ("B", "2", "3").with ("C", "1").get ();
    props = offering.compose(requirement);
    assertSame (offering, props);
    offering = ValueProperties.with ("A", "1").with ("B", "2", "3").withAny ("C").with ("D", "4").get ();
    props = offering.compose(requirement);
    assertSame (offering, props);
    offering = ValueProperties.with ("A", "1", "2").with ("B", "2", "4").with ("C", "1").withAny ("D").get ();
    props = offering.compose (requirement);
    assertEquals (requirement.getValues ("A"), props.getValues ("A"));
    assertEquals (Collections.singleton("2"), props.getValues ("B"));
    assertSame (offering.getValues ("C"), props.getValues ("C"));
    assertSame (offering.getValues ("D"), props.getValues ("D"));
    offering = ValueProperties.withAny ("A").withAny("B").withAny("C").get ();
    props = offering.compose (requirement);
    assertSame (requirement.getValues ("A"), props.getValues ("A"));
    assertSame (requirement.getValues ("B"), props.getValues ("B"));
    assertSame (requirement.getValues ("C"), props.getValues ("C"));
    assertSame (requirement, props);
    offering = ValueProperties.withAny ("A").withAny("B").withAny("C").with("E", "1").get ();
    props = offering.compose (requirement);
    assertSame (requirement.getValues ("A"), props.getValues ("A"));
    assertSame (requirement.getValues ("B"), props.getValues ("B"));
    assertSame (requirement.getValues ("C"), props.getValues ("C"));
    assertSame (offering.getValues ("E"), props.getValues ("E"));
  }

}
