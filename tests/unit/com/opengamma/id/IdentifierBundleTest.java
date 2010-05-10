/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.id;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeFieldContainer;
import org.junit.Test;

/**
 * Test IdentifierBundle.
 */
public class IdentifierBundleTest {

  private final Identifier _id11 = Identifier.of(new IdentificationScheme("D1"), "V1");
  private final Identifier _id21 = Identifier.of(new IdentificationScheme("D2"), "V1");
  private final Identifier _id12 = Identifier.of(new IdentificationScheme("D1"), "V2");
  private final Identifier _id22 = Identifier.of(new IdentificationScheme("D2"), "V2");

  @Test
  public void noIdentifiers() {
    IdentifierBundle dsi1 = new IdentifierBundle();
    IdentifierBundle dsi2 = new IdentifierBundle();
    
    assertTrue(dsi1.equals(dsi1));
    assertTrue(dsi1.equals(dsi2));
  }

  @Test
  public void singleIdentifier() {
    assertTrue(new IdentifierBundle(_id11).equals(new IdentifierBundle(_id11)));
    assertFalse(new IdentifierBundle(_id11).equals(new IdentifierBundle(_id21)));
    assertFalse(new IdentifierBundle(_id11).equals(new IdentifierBundle(_id12)));
    assertFalse(new IdentifierBundle(_id11).equals(new IdentifierBundle(_id22)));
  }

  @Test
  public void singleIdentifierDifferentConstructors() {
    assertTrue(new IdentifierBundle(_id11).equals(new IdentifierBundle(Collections.singleton(_id11))));
  }

  @Test
  public void singleVersusMultipleIdentifier() {
    assertFalse(new IdentifierBundle(_id11).equals(new IdentifierBundle(_id11, _id12)));
    assertFalse(new IdentifierBundle(_id11, _id12).equals(new IdentifierBundle(_id11)));
  }

  @Test
  public void multipleIdentifier() {
    assertTrue(new IdentifierBundle(_id11, _id12).equals(new IdentifierBundle(_id11, _id12)));
    assertFalse(new IdentifierBundle(_id11, _id22).equals(new IdentifierBundle(_id11, _id12)));
    assertFalse(new IdentifierBundle(_id21, _id22).equals(new IdentifierBundle(_id11, _id12)));
  }

  @Test(expected=NullPointerException.class)
  public void multipleIdentifier_noNulls() {
    new IdentifierBundle(_id11, null, _id12);
  }

  @Test
  public void nullIdentifierConstructor() {
    IdentifierBundle bundle = new IdentifierBundle((Identifier)null);
    assertNotNull(bundle.getIdentifiers());
    assertTrue(bundle.getIdentifiers().isEmpty());
  }

  @Test
  public void emptyIdentifierArrayConstructor() {
    IdentifierBundle bundle = new IdentifierBundle(new Identifier[0]);
    assertNotNull(bundle.getIdentifiers());
    assertTrue(bundle.getIdentifiers().isEmpty());
  }

  @Test
  public void nullIdentifierCollectionConstructor() {
    IdentifierBundle bundle = new IdentifierBundle((Collection<Identifier>) null);
    assertNotNull(bundle.getIdentifiers());
    assertTrue(bundle.getIdentifiers().isEmpty());
  }
  
  @Test
  public void mapForm() {
    IdentifierBundle input = new IdentifierBundle(_id11, _id22);
    
    assertEquals("V1", input.getIdentifier(new IdentificationScheme("D1")));
    assertEquals("V2", input.getIdentifier(new IdentificationScheme("D2")));
    assertNull(input.getIdentifier(new IdentificationScheme("Kirk Wylie")));
    assertNull(input.getIdentifier(null));
  }

  @Test
  public void withIdentifier() {
    IdentifierBundle base = new IdentifierBundle(Identifier.of("A", "B"));
    IdentifierBundle test = base.withIdentifier(Identifier.of("A", "C"));
    assertEquals(1, base.size());
    assertEquals(2, test.size());
    assertTrue(test.getIdentifiers().contains(Identifier.of("A", "B")));
    assertTrue(test.getIdentifiers().contains(Identifier.of("A", "C")));
  }

  @Test(expected=NullPointerException.class)
  public void withIdentifier_null() {
    IdentifierBundle base = new IdentifierBundle(Identifier.of("A", "B"));
    base.withIdentifier(null);
  }

  @Test
  public void withoutIdentifier_match() {
    IdentifierBundle base = new IdentifierBundle(Identifier.of("A", "B"));
    IdentifierBundle test = base.withoutIdentifier(Identifier.of("A", "B"));
    assertEquals(1, base.size());
    assertEquals(0, test.size());
  }

  @Test
  public void withoutIdentifier_noMatch() {
    IdentifierBundle base = new IdentifierBundle(Identifier.of("A", "B"));
    IdentifierBundle test = base.withoutIdentifier(Identifier.of("A", "C"));
    assertEquals(1, base.size());
    assertEquals(1, test.size());
    assertTrue(test.getIdentifiers().contains(Identifier.of("A", "B")));
  }

  @Test
  public void withoutIdentifier_null() {
    IdentifierBundle base = new IdentifierBundle(Identifier.of("A", "B"));
    IdentifierBundle test = base.withoutIdentifier(null);
    assertEquals(1, base.size());
    assertEquals(1, test.size());
    assertTrue(test.getIdentifiers().contains(Identifier.of("A", "B")));
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_size() {
    assertEquals(0, new IdentifierBundle().size());
    assertEquals(1, new IdentifierBundle(_id11).size());
    assertEquals(2, new IdentifierBundle(_id11, _id12).size());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_iterator() {
    Set<Identifier> expected = new HashSet<Identifier>();
    expected.add(_id11);
    expected.add(_id12);
    Iterable<Identifier> base = new IdentifierBundle(_id11, _id12);
    Iterator<Identifier> test = base.iterator();
    assertEquals(true, test.hasNext());
    assertEquals(true, expected.remove(test.next()));
    assertEquals(true, test.hasNext());
    assertEquals(true, expected.remove(test.next()));
    assertEquals(false, test.hasNext());
    assertEquals(0, expected.size());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_containsAny() {
    IdentifierBundle test = new IdentifierBundle(_id11, _id12);
    assertEquals(true, test.containsAny(new IdentifierBundle(_id11, _id12)));
    assertEquals(true, test.containsAny(new IdentifierBundle(_id11)));
    assertEquals(true, test.containsAny(new IdentifierBundle(_id12)));
    assertEquals(false, test.containsAny(new IdentifierBundle(_id21)));
    assertEquals(false, test.containsAny(new IdentifierBundle()));
  }

  @Test(expected=NullPointerException.class)
  public void test_containsAny_null() {
    IdentifierBundle test = new IdentifierBundle(_id11, _id12);
    test.containsAny(null);
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_contains() {
    IdentifierBundle test = new IdentifierBundle(_id11, _id12);
    assertEquals(true, test.contains(_id11));
    assertEquals(true, test.contains(_id11));
    assertEquals(false, test.contains(_id21));
  }

  @Test
  public void test_contains_null() {
    IdentifierBundle test = new IdentifierBundle(_id11, _id12);
    assertEquals(false, test.contains(null));
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_equals_same_empty() {
    IdentifierBundle a1 = new IdentifierBundle();
    IdentifierBundle a2 = new IdentifierBundle();
    
    assertEquals(true, a1.equals(a1));
    assertEquals(true, a1.equals(a2));
    assertEquals(true, a2.equals(a1));
    assertEquals(true, a2.equals(a2));
  }

  @Test
  public void test_equals_same_nonEmpty() {
    IdentifierBundle a1 = new IdentifierBundle(_id11, _id12);
    IdentifierBundle a2 = new IdentifierBundle(_id11, _id12);
    
    assertEquals(true, a1.equals(a1));
    assertEquals(true, a1.equals(a2));
    assertEquals(true, a2.equals(a1));
    assertEquals(true, a2.equals(a2));
  }

  @Test
  public void test_equals_different() {
    IdentifierBundle a = new IdentifierBundle();
    IdentifierBundle b = new IdentifierBundle(_id11, _id12);
    
    assertEquals(true, a.equals(a));
    assertEquals(false, a.equals(b));
    assertEquals(false, b.equals(a));
    assertEquals(true, b.equals(b));
    
    assertEquals(false, b.equals("Rubbish"));
    assertEquals(false, b.equals(null));
  }

  @Test
  public void test_hashCode() {
    IdentifierBundle a = new IdentifierBundle(_id11, _id12);
    IdentifierBundle b = new IdentifierBundle(_id11, _id12);
    
    assertEquals(a.hashCode(), b.hashCode());
  }

  @Test
  public void test_toString_empty() {
    IdentifierBundle test = new IdentifierBundle();
    assertEquals("Bundle[]", test.toString());
  }

  @Test
  public void test_toString_nonEmpty() {
    IdentifierBundle test = new IdentifierBundle(_id11, _id12);
    assertEquals("Bundle[" + _id11.toString() + ", " + _id12.toString() + "]", test.toString());
  }

  //-------------------------------------------------------------------------
  @Test
  public void fudgeEncoding() {
    IdentifierBundle input = new IdentifierBundle(
        Identifier.of(new IdentificationScheme("id1"), "value1"),
        Identifier.of(new IdentificationScheme("id2"), "value2")
      );
    FudgeFieldContainer msg = input.toFudgeMsg(new FudgeContext());
    assertNotNull(msg);
    assertEquals(2, msg.getNumFields());
    
    IdentifierBundle decoded = IdentifierBundle.fromFudgeMsg(msg);
    assertEquals(input, decoded);
  }

}
