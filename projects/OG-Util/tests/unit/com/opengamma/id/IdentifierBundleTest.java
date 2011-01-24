/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.id;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeFieldContainer;
import org.junit.Test;

import com.google.common.collect.Sets;

/**
 * Test IdentifierBundle.
 */
public class IdentifierBundleTest {

  private final Identifier _id11 = Identifier.of("D1", "V1");
  private final Identifier _id21 = Identifier.of("D2", "V1");
  private final Identifier _id12 = Identifier.of("D1", "V2");
  private final Identifier _id22 = Identifier.of("D2", "V2");

  @Test
  public void singleton_empty() {
    assertEquals(0, IdentifierBundle.EMPTY.size());
  }

  //-------------------------------------------------------------------------
  @Test(expected = IllegalArgumentException.class)
  public void factory_of_Identifier_null() {
    IdentifierBundle.of((Identifier) null);
  }

  @Test
  public void factory_of_Identifier() {
    IdentifierBundle test = IdentifierBundle.of(_id11);
    assertEquals(1, test.size());
    assertEquals(Sets.newHashSet(_id11), test.getIdentifiers());
  }

  //-------------------------------------------------------------------------
  @Test
  public void factory_of_varargs_noIdentifiers() {
    IdentifierBundle test = IdentifierBundle.of();
    assertEquals(0, test.size());
  }

  @Test
  public void factory_of_varargs_oneIdentifier() {
    IdentifierBundle test = IdentifierBundle.of(_id11);
    assertEquals(1, test.size());
    assertEquals(Sets.newHashSet(_id11), test.getIdentifiers());
  }

  @Test
  public void factory_of_varargs_twoIdentifiers() {
    IdentifierBundle test = IdentifierBundle.of(_id11, _id12);
    assertEquals(2, test.size());
    assertEquals(Sets.newHashSet(_id11, _id12), test.getIdentifiers());
  }

  @Test(expected=IllegalArgumentException.class)
  public void factory_of_varargs_null() {
    IdentifierBundle.of((Identifier[]) null);
  }

  @Test(expected=IllegalArgumentException.class)
  public void factory_of_varargs_noNulls() {
    IdentifierBundle.of(_id11, null, _id12);
  }

  //-------------------------------------------------------------------------
  @Test
  public void factory_of_Collection_empty() {
    IdentifierBundle test = IdentifierBundle.of(new ArrayList<Identifier>());
    assertEquals(0, test.size());
  }

  @Test
  public void factory_of_Collection_two() {
    IdentifierBundle test = IdentifierBundle.of(Arrays.asList(_id11, _id12));
    assertEquals(2, test.size());
    assertEquals(Sets.newHashSet(_id11, _id12), test.getIdentifiers());
  }

  @Test(expected = IllegalArgumentException.class)
  public void factory_of_Collection_null() {
    IdentifierBundle.of((Collection<Identifier>) null);
  }

  @Test(expected=IllegalArgumentException.class)
  public void factory_of_Collection_noNulls() {
    IdentifierBundle.of(Arrays.asList(_id11, null, _id12));
  }

  //-------------------------------------------------------------------------
  @Test
  public void constructor_noargs() {
    IdentifierBundle test = new IdentifierBundle();
    assertEquals(0, test.size());
  }

  //-------------------------------------------------------------------------
  @Test
  public void singleIdentifierDifferentConstructors() {
    assertTrue(IdentifierBundle.of(_id11).equals(IdentifierBundle.of(Collections.singleton(_id11))));
  }

  @Test
  public void singleVersusMultipleIdentifier() {
    assertFalse(IdentifierBundle.of(_id11).equals(IdentifierBundle.of(_id11, _id12)));
    assertFalse(IdentifierBundle.of(_id11, _id12).equals(IdentifierBundle.of(_id11)));
  }

  //-------------------------------------------------------------------------
  @Test
  public void mapForm() {
    IdentifierBundle input = IdentifierBundle.of(_id11, _id22);
    
    assertEquals("V1", input.getIdentifier(IdentificationScheme.of("D1")));
    assertEquals("V2", input.getIdentifier(IdentificationScheme.of("D2")));
    assertNull(input.getIdentifier(IdentificationScheme.of("Kirk Wylie")));
    assertNull(input.getIdentifier(null));
  }

  @Test
  public void withIdentifier() {
    IdentifierBundle base = IdentifierBundle.of(Identifier.of("A", "B"));
    IdentifierBundle test = base.withIdentifier(Identifier.of("A", "C"));
    assertEquals(1, base.size());
    assertEquals(2, test.size());
    assertTrue(test.getIdentifiers().contains(Identifier.of("A", "B")));
    assertTrue(test.getIdentifiers().contains(Identifier.of("A", "C")));
  }

  @Test(expected=IllegalArgumentException.class)
  public void withIdentifier_null() {
    IdentifierBundle base = IdentifierBundle.of(Identifier.of("A", "B"));
    base.withIdentifier(null);
  }

  @Test
  public void withoutIdentifier_match() {
    IdentifierBundle base = IdentifierBundle.of(Identifier.of("A", "B"));
    IdentifierBundle test = base.withoutIdentifier(Identifier.of("A", "B"));
    assertEquals(1, base.size());
    assertEquals(0, test.size());
  }

  @Test
  public void withoutIdentifier_noMatch() {
    IdentifierBundle base = IdentifierBundle.of(Identifier.of("A", "B"));
    IdentifierBundle test = base.withoutIdentifier(Identifier.of("A", "C"));
    assertEquals(1, base.size());
    assertEquals(1, test.size());
    assertTrue(test.getIdentifiers().contains(Identifier.of("A", "B")));
  }

  @Test
  public void withoutIdentifier_null() {
    IdentifierBundle base = IdentifierBundle.of(Identifier.of("A", "B"));
    IdentifierBundle test = base.withoutIdentifier(null);
    assertEquals(1, base.size());
    assertEquals(1, test.size());
    assertTrue(test.getIdentifiers().contains(Identifier.of("A", "B")));
  }

  //-------------------------------------------------------------------------
  @Test
  public void withoutScheme_IdentificationScheme_match() {
    IdentifierBundle base = IdentifierBundle.of(Identifier.of("A", "B"));
    IdentifierBundle test = base.withoutScheme(IdentificationScheme.of("A"));
    assertEquals(1, base.size());
    assertEquals(0, test.size());
  }

  @Test
  public void withoutScheme_IdentificationScheme_noMatch() {
    IdentifierBundle base = IdentifierBundle.of(Identifier.of("A", "B"));
    IdentifierBundle test = base.withoutScheme(IdentificationScheme.of("BLOOMBERG_BUID"));
    assertEquals(1, base.size());
    assertEquals(1, test.size());
    assertTrue(test.getIdentifiers().contains(Identifier.of("A", "B")));
  }

  @Test
  public void withoutScheme_IdentificationScheme_null() {
    IdentifierBundle base = IdentifierBundle.of(Identifier.of("A", "B"));
    IdentifierBundle test = base.withoutScheme(null);
    assertEquals(1, base.size());
    assertEquals(1, test.size());
    assertTrue(test.getIdentifiers().contains(Identifier.of("A", "B")));
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_size() {
    assertEquals(0, IdentifierBundle.EMPTY.size());
    assertEquals(1, IdentifierBundle.of(_id11).size());
    assertEquals(2, IdentifierBundle.of(_id11, _id12).size());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_iterator() {
    Set<Identifier> expected = new HashSet<Identifier>();
    expected.add(_id11);
    expected.add(_id12);
    Iterable<Identifier> base = IdentifierBundle.of(_id11, _id12);
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
  public void test_containsAll1() {
    IdentifierBundle test = IdentifierBundle.of(_id11);
    assertEquals(false, test.containsAll(IdentifierBundle.of(_id11, _id12)));
    assertEquals(true, test.containsAll(IdentifierBundle.of(_id11)));
    assertEquals(false, test.containsAll(IdentifierBundle.of(_id12)));
    assertEquals(false, test.containsAll(IdentifierBundle.of(_id21)));
    assertEquals(true, test.containsAll(IdentifierBundle.EMPTY));
  }

  @Test
  public void test_containsAll2() {
    IdentifierBundle test = IdentifierBundle.of(_id11, _id12);
    assertEquals(true, test.containsAll(IdentifierBundle.of(_id11, _id12)));
    assertEquals(true, test.containsAll(IdentifierBundle.of(_id11)));
    assertEquals(true, test.containsAll(IdentifierBundle.of(_id12)));
    assertEquals(false, test.containsAll(IdentifierBundle.of(_id21)));
    assertEquals(true, test.containsAll(IdentifierBundle.EMPTY));
  }

  @Test(expected=IllegalArgumentException.class)
  public void test_containsAll_null() {
    IdentifierBundle test = IdentifierBundle.of(_id11, _id12);
    test.containsAll(null);
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_containsAny() {
    IdentifierBundle test = IdentifierBundle.of(_id11, _id12);
    assertEquals(true, test.containsAny(IdentifierBundle.of(_id11, _id12)));
    assertEquals(true, test.containsAny(IdentifierBundle.of(_id11)));
    assertEquals(true, test.containsAny(IdentifierBundle.of(_id12)));
    assertEquals(false, test.containsAny(IdentifierBundle.of(_id21)));
    assertEquals(false, test.containsAny(IdentifierBundle.EMPTY));
  }

  @Test(expected=IllegalArgumentException.class)
  public void test_containsAny_null() {
    IdentifierBundle test = IdentifierBundle.of(_id11, _id12);
    test.containsAny(null);
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_contains() {
    IdentifierBundle test = IdentifierBundle.of(_id11, _id12);
    assertEquals(true, test.contains(_id11));
    assertEquals(true, test.contains(_id11));
    assertEquals(false, test.contains(_id21));
  }

  @Test
  public void test_contains_null() {
    IdentifierBundle test = IdentifierBundle.of(_id11, _id12);
    assertEquals(false, test.contains(null));
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_toStringList() {
    IdentifierBundle test = IdentifierBundle.of(_id11, _id12);
    assertEquals(Arrays.asList(_id11.toString(), _id12.toString()), test.toStringList());
  }

  @Test
  public void test_toStringList_empty() {
    IdentifierBundle test = IdentifierBundle.EMPTY;
    assertEquals(new ArrayList<String>(), test.toStringList());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_compareTo_differentSizes() {
    IdentifierBundle a1 = IdentifierBundle.EMPTY;
    IdentifierBundle a2 = IdentifierBundle.of(Identifier.of("A", "B"));
    
    assertEquals(true, a1.compareTo(a1) == 0);
    assertEquals(true, a1.compareTo(a2) < 0);
    assertEquals(true, a2.compareTo(a1) > 0);
    assertEquals(true, a2.compareTo(a2) == 0);
  }

  @Test
  public void test_compareTo_sameSizes() {
    IdentifierBundle a1 = IdentifierBundle.of(Identifier.of("A", "B"));
    IdentifierBundle a2 = IdentifierBundle.of(Identifier.of("A", "C"));
    
    assertEquals(true, a1.compareTo(a1) == 0);
    assertEquals(true, a1.compareTo(a2) < 0);
    assertEquals(true, a2.compareTo(a1) > 0);
    assertEquals(true, a2.compareTo(a2) == 0);
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_equals_same_empty() {
    IdentifierBundle a1 = IdentifierBundle.EMPTY;
    IdentifierBundle a2 = new IdentifierBundle();
    
    assertEquals(true, a1.equals(a1));
    assertEquals(true, a1.equals(a2));
    assertEquals(true, a2.equals(a1));
    assertEquals(true, a2.equals(a2));
  }

  @Test
  public void test_equals_same_nonEmpty() {
    IdentifierBundle a1 = IdentifierBundle.of(_id11, _id12);
    IdentifierBundle a2 = IdentifierBundle.of(_id11, _id12);
    
    assertEquals(true, a1.equals(a1));
    assertEquals(true, a1.equals(a2));
    assertEquals(true, a2.equals(a1));
    assertEquals(true, a2.equals(a2));
  }

  @Test
  public void test_equals_different() {
    IdentifierBundle a = IdentifierBundle.EMPTY;
    IdentifierBundle b = IdentifierBundle.of(_id11, _id12);
    
    assertEquals(true, a.equals(a));
    assertEquals(false, a.equals(b));
    assertEquals(false, b.equals(a));
    assertEquals(true, b.equals(b));
    
    assertEquals(false, b.equals("Rubbish"));
    assertEquals(false, b.equals(null));
  }

  @Test
  public void test_hashCode() {
    IdentifierBundle a = IdentifierBundle.of(_id11, _id12);
    IdentifierBundle b = IdentifierBundle.of(_id11, _id12);
    
    assertEquals(a.hashCode(), b.hashCode());
  }

  @Test
  public void test_toString_empty() {
    IdentifierBundle test = IdentifierBundle.EMPTY;
    assertEquals("Bundle[]", test.toString());
  }

  @Test
  public void test_toString_nonEmpty() {
    IdentifierBundle test = IdentifierBundle.of(_id11, _id12);
    assertEquals("Bundle[" + _id11.toString() + ", " + _id12.toString() + "]", test.toString());
  }

  //-------------------------------------------------------------------------
  @Test
  public void fudgeEncoding() {
    IdentifierBundle input = IdentifierBundle.of(
        Identifier.of("id1", "value1"),
        Identifier.of("id2", "value2")
      );
    FudgeFieldContainer msg = input.toFudgeMsg(new FudgeContext());
    assertNotNull(msg);
    assertEquals(2, msg.getNumFields());
    
    IdentifierBundle decoded = IdentifierBundle.fromFudgeMsg(msg);
    assertEquals(input, decoded);
  }

}
