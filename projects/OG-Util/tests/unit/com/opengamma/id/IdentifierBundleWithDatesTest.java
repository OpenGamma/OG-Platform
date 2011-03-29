/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.id;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.time.calendar.LocalDate;
import javax.time.calendar.MonthOfYear;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeFieldContainer;
import org.testng.annotations.Test;

import com.google.common.collect.Sets;

/**
 * Test IdentifierBundleWithDates
 */
@Test
public class IdentifierBundleWithDatesTest {

  private final Identifier _id11 = Identifier.of("D1", "V1");
  private final IdentifierWithDates _idwd11 = IdentifierWithDates.of(_id11, LocalDate.of(2000, MonthOfYear.JANUARY, 1), LocalDate.of(2001, MonthOfYear.JANUARY, 1));
  private final Identifier _id21 = Identifier.of("D2", "V1");
  private final IdentifierWithDates _idwd21 = IdentifierWithDates.of(_id21, null, null);
  private final Identifier _id12 = Identifier.of("D1", "V2");
  private final IdentifierWithDates _idwd12 = IdentifierWithDates.of(_id12, LocalDate.of(2001, MonthOfYear.JANUARY, 2), null);
  private final Identifier _id22 = Identifier.of("D2", "V2");
  private final IdentifierWithDates _idwd22 = IdentifierWithDates.of(_id22, null, LocalDate.of(2010, MonthOfYear.DECEMBER, 30));
  
  public void singleton_empty() {
    assertEquals(0, IdentifierBundleWithDates.EMPTY.size());
  }

  //-------------------------------------------------------------------------
  public void factory_of_varargs_noIdentifiers() {
    IdentifierBundleWithDates test = IdentifierBundleWithDates.of();
    assertEquals(0, test.size());
  }

  public void factory_of_varargs_oneIdentifier() {
    IdentifierBundleWithDates test = IdentifierBundleWithDates.of(_idwd11);
    assertEquals(1, test.size());
    assertEquals(Sets.newHashSet(_idwd11), test.getIdentifiers());
  }

  public void factory_of_varargs_twoIdentifiers() {
    IdentifierBundleWithDates test = IdentifierBundleWithDates.of(_idwd11, _idwd12);
    assertEquals(2, test.size());
    assertEquals(Sets.newHashSet(_idwd11, _idwd12), test.getIdentifiers());
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void factory_of_varargs_null() {
    IdentifierBundleWithDates.of((IdentifierWithDates[]) null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void factory_of_varargs_noNulls() {
    IdentifierBundleWithDates.of(_idwd11, null, _idwd12);
  }

  //-------------------------------------------------------------------------
  public void constructor_noargs() {
    IdentifierBundleWithDates test = new IdentifierBundleWithDates();
    assertEquals(0, test.size());
  }

  //-------------------------------------------------------------------------
  public void constructor_Identifier() {
    IdentifierBundleWithDates test = new IdentifierBundleWithDates(_idwd11);
    assertEquals(1, test.size());
    assertEquals(Sets.newHashSet(_idwd11), test.getIdentifiers());
  }

  public void constructor_Identifier_null() {
    IdentifierBundleWithDates test = new IdentifierBundleWithDates((IdentifierWithDates) null);
    assertEquals(0, test.size());
  }

  //-------------------------------------------------------------------------
  public void constructor_varargs_empty() {
    IdentifierBundleWithDates test = new IdentifierBundleWithDates(new IdentifierWithDates[0]);
    assertEquals(0, test.size());
  }

  public void constructor_varargs_two() {
    IdentifierBundleWithDates test = new IdentifierBundleWithDates(_idwd11, _idwd12);
    assertEquals(2, test.size());
    assertEquals(Sets.newHashSet(_idwd11, _idwd12), test.getIdentifiers());
  }

  public void constructor_varargs_null() {
    IdentifierBundleWithDates test = new IdentifierBundleWithDates((IdentifierWithDates[]) null);
    assertEquals(0, test.size());
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void constructor_varargs_noNulls() {
    new IdentifierBundleWithDates(_idwd11, null, _idwd12);
  }

  //-------------------------------------------------------------------------
  public void constructor_Collection_empty() {
    IdentifierBundleWithDates test = new IdentifierBundleWithDates(new ArrayList<IdentifierWithDates>());
    assertEquals(0, test.size());
  }

  public void constructor_Collection_two() {
    IdentifierBundleWithDates test = new IdentifierBundleWithDates(Arrays.asList(_idwd11, _idwd12));
    assertEquals(2, test.size());
    assertEquals(Sets.newHashSet(_idwd11, _idwd12), test.getIdentifiers());
  }

  public void constructor_Collection_null() {
    IdentifierBundleWithDates test = new IdentifierBundleWithDates((Collection<IdentifierWithDates>) null);
    assertEquals(0, test.size());
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void constructor_Collection_noNulls() {
    new IdentifierBundleWithDates(Arrays.asList(_idwd11, null, _idwd12));
  }

  //-------------------------------------------------------------------------
  public void singleIdentifierDifferentConstructors() {
    assertTrue(new IdentifierBundleWithDates(_idwd11).equals(new IdentifierBundleWithDates(Collections.singleton(_idwd11))));
  }

  public void singleVersusMultipleIdentifier() {
    assertFalse(new IdentifierBundleWithDates(_idwd11).equals(new IdentifierBundleWithDates(_idwd11, _idwd12)));
    assertFalse(new IdentifierBundleWithDates(_idwd11, _idwd12).equals(new IdentifierBundleWithDates(_idwd11)));
  }

  //-------------------------------------------------------------------------
  public void asIdentifierBundle() {
    IdentifierBundleWithDates bundleWithDates = new IdentifierBundleWithDates(_idwd11, _idwd22);
    assertEquals(IdentifierBundle.of(_id11, _id22), bundleWithDates.asIdentifierBundle());
  }

  public void withIdentifier() {
    IdentifierBundleWithDates base = new IdentifierBundleWithDates(_idwd11);
    IdentifierBundleWithDates test = base.withIdentifier(_idwd21);
    assertEquals(1, base.size());
    assertEquals(2, test.size());
    assertTrue(test.getIdentifiers().contains(_idwd11));
    assertTrue(test.getIdentifiers().contains(_idwd21));
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void withIdentifier_null() {
    IdentifierBundleWithDates base = new IdentifierBundleWithDates(_idwd11);
    base.withIdentifier(null);
  }

  public void withoutIdentifier_match() {
    IdentifierBundleWithDates base = new IdentifierBundleWithDates(_idwd11);
    IdentifierBundleWithDates test = base.withoutIdentifier(_idwd11);
    assertEquals(1, base.size());
    assertEquals(0, test.size());
  }

  public void withoutIdentifier_noMatch() {
    IdentifierBundleWithDates base = new IdentifierBundleWithDates(_idwd11);
    IdentifierBundleWithDates test = base.withoutIdentifier(_idwd12);
    assertEquals(1, base.size());
    assertEquals(1, test.size());
    assertTrue(test.getIdentifiers().contains(_idwd11));
  }

  public void withoutIdentifier_null() {
    IdentifierBundleWithDates base = new IdentifierBundleWithDates(_idwd11);
    IdentifierBundleWithDates test = base.withoutIdentifier(null);
    assertEquals(1, base.size());
    assertEquals(1, test.size());
    assertTrue(test.getIdentifiers().contains(_idwd11));
  }

  //-------------------------------------------------------------------------
  public void test_size() {
    assertEquals(0, new IdentifierBundleWithDates().size());
    assertEquals(1, new IdentifierBundleWithDates(_idwd11).size());
    assertEquals(2, new IdentifierBundleWithDates(_idwd11, _idwd12).size());
  }

  //-------------------------------------------------------------------------
  public void test_iterator() {
    Set<IdentifierWithDates> expected = new HashSet<IdentifierWithDates>();
    expected.add(_idwd11);
    expected.add(_idwd12);
    Iterable<IdentifierWithDates> base = new IdentifierBundleWithDates(_idwd11, _idwd12);
    Iterator<IdentifierWithDates> test = base.iterator();
    assertEquals(true, test.hasNext());
    assertEquals(true, expected.remove(test.next()));
    assertEquals(true, test.hasNext());
    assertEquals(true, expected.remove(test.next()));
    assertEquals(false, test.hasNext());
    assertEquals(0, expected.size());
  }

  //-------------------------------------------------------------------------
  public void test_containsAny() {
    IdentifierBundleWithDates test = new IdentifierBundleWithDates(_idwd11, _idwd12);
    assertEquals(true, test.containsAny(new IdentifierBundleWithDates(_idwd11, _idwd12)));
    assertEquals(true, test.containsAny(new IdentifierBundleWithDates(_idwd11)));
    assertEquals(true, test.containsAny(new IdentifierBundleWithDates(_idwd12)));
    assertEquals(false, test.containsAny(new IdentifierBundleWithDates(_idwd21)));
    assertEquals(false, test.containsAny(new IdentifierBundleWithDates()));
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_containsAny_null() {
    IdentifierBundleWithDates test = new IdentifierBundleWithDates(_idwd11, _idwd12);
    test.containsAny(null);
  }

  //-------------------------------------------------------------------------
  public void test_contains() {
    IdentifierBundleWithDates test = new IdentifierBundleWithDates(_idwd11, _idwd12);
    assertEquals(true, test.contains(_idwd11));
    assertEquals(true, test.contains(_idwd11));
    assertEquals(false, test.contains(_idwd21));
  }

  public void test_contains_null() {
    IdentifierBundleWithDates test = new IdentifierBundleWithDates(_idwd11, _idwd12);
    assertEquals(false, test.contains(null));
  }

  //-------------------------------------------------------------------------
  public void test_toStringList() {
    IdentifierBundleWithDates test = new IdentifierBundleWithDates(_idwd11, _idwd12);
    assertEquals(Arrays.asList(_idwd11.toString(), _idwd12.toString()), test.toStringList());
  }

  public void test_toStringList_empty() {
    IdentifierBundleWithDates test = new IdentifierBundleWithDates();
    assertEquals(new ArrayList<String>(), test.toStringList());
  }

  //-------------------------------------------------------------------------
  public void test_compareTo_differentSizes() {
    IdentifierBundleWithDates a1 = new IdentifierBundleWithDates();
    IdentifierBundleWithDates a2 = new IdentifierBundleWithDates(_idwd11);
    
    assertEquals(true, a1.compareTo(a1) == 0);
    assertEquals(true, a1.compareTo(a2) < 0);
    assertEquals(true, a2.compareTo(a1) > 0);
    assertEquals(true, a2.compareTo(a2) == 0);
  }

  public void test_compareTo_sameSizes() {
    IdentifierBundleWithDates a1 = new IdentifierBundleWithDates(_idwd11);
    IdentifierBundleWithDates a2 = new IdentifierBundleWithDates(_idwd12);
    
    assertEquals(true, a1.compareTo(a1) == 0);
    assertEquals(true, a1.compareTo(a2) < 0);
    assertEquals(true, a2.compareTo(a1) > 0);
    assertEquals(true, a2.compareTo(a2) == 0);
  }

  //-------------------------------------------------------------------------
  public void test_equals_same_empty() {
    IdentifierBundleWithDates a1 = new IdentifierBundleWithDates();
    IdentifierBundleWithDates a2 = new IdentifierBundleWithDates();
    
    assertEquals(true, a1.equals(a1));
    assertEquals(true, a1.equals(a2));
    assertEquals(true, a2.equals(a1));
    assertEquals(true, a2.equals(a2));
  }

  public void test_equals_same_nonEmpty() {
    IdentifierBundleWithDates a1 = new IdentifierBundleWithDates(_idwd11, _idwd12);
    IdentifierBundleWithDates a2 = new IdentifierBundleWithDates(_idwd11, _idwd12);
    
    assertEquals(true, a1.equals(a1));
    assertEquals(true, a1.equals(a2));
    assertEquals(true, a2.equals(a1));
    assertEquals(true, a2.equals(a2));
  }

  public void test_equals_different() {
    IdentifierBundleWithDates a = new IdentifierBundleWithDates();
    IdentifierBundleWithDates b = new IdentifierBundleWithDates(_idwd11, _idwd12);
    
    assertEquals(true, a.equals(a));
    assertEquals(false, a.equals(b));
    assertEquals(false, b.equals(a));
    assertEquals(true, b.equals(b));
    
    assertEquals(false, b.equals("Rubbish"));
    assertEquals(false, b.equals(null));
  }

  public void test_hashCode() {
    IdentifierBundleWithDates a = new IdentifierBundleWithDates(_idwd11, _idwd12);
    IdentifierBundleWithDates b = new IdentifierBundleWithDates(_idwd11, _idwd12);
    
    assertEquals(a.hashCode(), b.hashCode());
  }

  public void test_toString_empty() {
    IdentifierBundleWithDates test = new IdentifierBundleWithDates();
    assertEquals("BundleWithDates[]", test.toString());
  }

  public void test_toString_nonEmpty() {
    IdentifierBundleWithDates test = new IdentifierBundleWithDates(_idwd11, _idwd12);
    assertEquals("BundleWithDates[" + _idwd11.toString() + ", " + _idwd12.toString() + "]", test.toString());
  }

  //-------------------------------------------------------------------------
  public void fudgeEncoding() {
    IdentifierBundleWithDates input = new IdentifierBundleWithDates(_idwd11, _idwd12);
    FudgeFieldContainer msg = input.toFudgeMsg(new FudgeContext());
    assertNotNull(msg);
    assertEquals(2, msg.getNumFields());
    
    IdentifierBundleWithDates decoded = IdentifierBundleWithDates.fromFudgeMsg(msg);
    assertEquals(input, decoded);
  }

}
