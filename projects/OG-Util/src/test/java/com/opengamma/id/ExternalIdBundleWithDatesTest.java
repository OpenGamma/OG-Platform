/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.id;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;
import static org.threeten.bp.Month.DECEMBER;
import static org.threeten.bp.Month.JANUARY;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;

import com.google.common.collect.Sets;
import com.opengamma.util.test.TestGroup;

/**
 * Test {@link ExternalIdBundleWithDates}.
 */
@Test(groups = TestGroup.UNIT)
public class ExternalIdBundleWithDatesTest {

  private final ExternalId _id11 = ExternalId.of("D1", "V1");
  private final ExternalIdWithDates _idwd11 = ExternalIdWithDates.of(_id11, LocalDate.of(2000, JANUARY, 1), LocalDate.of(2001, JANUARY, 1));
  private final ExternalId _id21 = ExternalId.of("D2", "V1");
  private final ExternalIdWithDates _idwd21 = ExternalIdWithDates.of(_id21, null, null);
  private final ExternalId _id12 = ExternalId.of("D1", "V2");
  private final ExternalIdWithDates _idwd12 = ExternalIdWithDates.of(_id12, LocalDate.of(2001, JANUARY, 2), null);
  private final ExternalId _id22 = ExternalId.of("D2", "V2");
  private final ExternalIdWithDates _idwd22 = ExternalIdWithDates.of(_id22, null, LocalDate.of(2010, DECEMBER, 30));
  
  public void singleton_empty() {
    assertEquals(0, ExternalIdBundleWithDates.EMPTY.size());
  }

  //-------------------------------------------------------------------------
  public void factory_of_varargs_noExternalIds() {
    ExternalIdBundleWithDates test = ExternalIdBundleWithDates.of();
    assertEquals(0, test.size());
  }

  public void factory_of_varargs_oneExternalId() {
    ExternalIdBundleWithDates test = ExternalIdBundleWithDates.of(_idwd11);
    assertEquals(1, test.size());
    assertEquals(Sets.newHashSet(_idwd11), test.getExternalIds());
  }

  public void factory_of_varargs_twoExternalIds() {
    ExternalIdBundleWithDates test = ExternalIdBundleWithDates.of(_idwd11, _idwd12);
    assertEquals(2, test.size());
    assertEquals(Sets.newHashSet(_idwd11, _idwd12), test.getExternalIds());
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void factory_of_varargs_null() {
    ExternalIdBundleWithDates.of((ExternalIdWithDates[]) null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void factory_of_varargs_noNulls() {
    ExternalIdBundleWithDates.of(_idwd11, null, _idwd12);
  }

  //-------------------------------------------------------------------------
  public void constructor_noargs() {
    ExternalIdBundleWithDates test = new ExternalIdBundleWithDates();
    assertEquals(0, test.size());
  }

  //-------------------------------------------------------------------------
  public void constructor_ExternalId() {
    ExternalIdBundleWithDates test = new ExternalIdBundleWithDates(_idwd11);
    assertEquals(1, test.size());
    assertEquals(Sets.newHashSet(_idwd11), test.getExternalIds());
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void constructor_ExternalId_null() {
    new ExternalIdBundleWithDates((ExternalIdWithDates) null);
  }

  //-------------------------------------------------------------------------
  public void constructor_varargs_empty() {
    ExternalIdBundleWithDates test = new ExternalIdBundleWithDates(new ExternalIdWithDates[0]);
    assertEquals(0, test.size());
  }

  public void constructor_varargs_two() {
    ExternalIdBundleWithDates test = new ExternalIdBundleWithDates(_idwd11, _idwd12);
    assertEquals(2, test.size());
    assertEquals(Sets.newHashSet(_idwd11, _idwd12), test.getExternalIds());
  }

  public void constructor_varargs_null() {
    ExternalIdBundleWithDates test = new ExternalIdBundleWithDates((ExternalIdWithDates[]) null);
    assertEquals(0, test.size());
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void constructor_varargs_noNulls() {
    new ExternalIdBundleWithDates(_idwd11, null, _idwd12);
  }

  //-------------------------------------------------------------------------
  public void constructor_Collection_empty() {
    ExternalIdBundleWithDates test = new ExternalIdBundleWithDates(new ArrayList<ExternalIdWithDates>());
    assertEquals(0, test.size());
  }

  public void constructor_Collection_two() {
    ExternalIdBundleWithDates test = new ExternalIdBundleWithDates(Arrays.asList(_idwd11, _idwd12));
    assertEquals(2, test.size());
    assertEquals(Sets.newHashSet(_idwd11, _idwd12), test.getExternalIds());
  }

  public void constructor_Collection_null() {
    ExternalIdBundleWithDates test = new ExternalIdBundleWithDates((Collection<ExternalIdWithDates>) null);
    assertEquals(0, test.size());
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void constructor_Collection_noNulls() {
    new ExternalIdBundleWithDates(Arrays.asList(_idwd11, null, _idwd12));
  }

  //-------------------------------------------------------------------------
  public void singleExternalIdDifferentConstructors() {
    assertTrue(new ExternalIdBundleWithDates(_idwd11).equals(new ExternalIdBundleWithDates(Collections.singleton(_idwd11))));
  }

  public void singleVersusMultipleExternalId() {
    assertFalse(new ExternalIdBundleWithDates(_idwd11).equals(new ExternalIdBundleWithDates(_idwd11, _idwd12)));
    assertFalse(new ExternalIdBundleWithDates(_idwd11, _idwd12).equals(new ExternalIdBundleWithDates(_idwd11)));
  }

  //-------------------------------------------------------------------------
  public void toBundle() {
    ExternalIdBundleWithDates bundleWithDates = new ExternalIdBundleWithDates(_idwd11, _idwd22);
    assertEquals(ExternalIdBundle.of(_id11, _id22), bundleWithDates.toBundle());
  }

  public void toBundle_LocalDate() {
    ExternalIdBundleWithDates bundleWithDates = new ExternalIdBundleWithDates(_idwd11, _idwd22);
    assertEquals(ExternalIdBundle.of(_id11, _id22), bundleWithDates.toBundle(LocalDate.of(2000, 6, 1)));
    assertEquals(ExternalIdBundle.of(_id22), bundleWithDates.toBundle(LocalDate.of(2002, 6, 1)));
    assertEquals(ExternalIdBundle.EMPTY, bundleWithDates.toBundle(LocalDate.of(2011, 6, 1)));
  }

  //-------------------------------------------------------------------------
  public void withExternalId() {
    ExternalIdBundleWithDates base = new ExternalIdBundleWithDates(_idwd11);
    ExternalIdBundleWithDates test = base.withExternalId(_idwd21);
    assertEquals(1, base.size());
    assertEquals(2, test.size());
    assertTrue(test.getExternalIds().contains(_idwd11));
    assertTrue(test.getExternalIds().contains(_idwd21));
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void withExternalId_null() {
    ExternalIdBundleWithDates base = new ExternalIdBundleWithDates(_idwd11);
    base.withExternalId(null);
  }

  public void withoutExternalId_match() {
    ExternalIdBundleWithDates base = new ExternalIdBundleWithDates(_idwd11);
    ExternalIdBundleWithDates test = base.withoutExternalId(_idwd11);
    assertEquals(1, base.size());
    assertEquals(0, test.size());
  }

  public void withoutExternalId_noMatch() {
    ExternalIdBundleWithDates base = new ExternalIdBundleWithDates(_idwd11);
    ExternalIdBundleWithDates test = base.withoutExternalId(_idwd12);
    assertEquals(1, base.size());
    assertEquals(1, test.size());
    assertTrue(test.getExternalIds().contains(_idwd11));
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void withoutExternalId_null() {
    ExternalIdBundleWithDates base = new ExternalIdBundleWithDates(_idwd11);
    base.withoutExternalId(null);
  }

  //-------------------------------------------------------------------------
  public void test_size() {
    assertEquals(0, new ExternalIdBundleWithDates().size());
    assertEquals(1, new ExternalIdBundleWithDates(_idwd11).size());
    assertEquals(2, new ExternalIdBundleWithDates(_idwd11, _idwd12).size());
  }

  //-------------------------------------------------------------------------
  public void test_iterator() {
    Set<ExternalIdWithDates> expected = new HashSet<ExternalIdWithDates>();
    expected.add(_idwd11);
    expected.add(_idwd12);
    Iterable<ExternalIdWithDates> base = new ExternalIdBundleWithDates(_idwd11, _idwd12);
    Iterator<ExternalIdWithDates> test = base.iterator();
    assertEquals(true, test.hasNext());
    assertEquals(true, expected.remove(test.next()));
    assertEquals(true, test.hasNext());
    assertEquals(true, expected.remove(test.next()));
    assertEquals(false, test.hasNext());
    assertEquals(0, expected.size());
  }

  //-------------------------------------------------------------------------
  public void test_containsAny() {
    ExternalIdBundleWithDates test = new ExternalIdBundleWithDates(_idwd11, _idwd12);
    assertEquals(true, test.containsAny(new ExternalIdBundleWithDates(_idwd11, _idwd12)));
    assertEquals(true, test.containsAny(new ExternalIdBundleWithDates(_idwd11)));
    assertEquals(true, test.containsAny(new ExternalIdBundleWithDates(_idwd12)));
    assertEquals(false, test.containsAny(new ExternalIdBundleWithDates(_idwd21)));
    assertEquals(false, test.containsAny(new ExternalIdBundleWithDates()));
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_containsAny_null() {
    ExternalIdBundleWithDates test = new ExternalIdBundleWithDates(_idwd11, _idwd12);
    test.containsAny(null);
  }

  //-------------------------------------------------------------------------
  public void test_contains() {
    ExternalIdBundleWithDates test = new ExternalIdBundleWithDates(_idwd11, _idwd12);
    assertEquals(true, test.contains(_idwd11));
    assertEquals(true, test.contains(_idwd11));
    assertEquals(false, test.contains(_idwd21));
  }

  public void test_contains_null() {
    ExternalIdBundleWithDates test = new ExternalIdBundleWithDates(_idwd11, _idwd12);
    assertEquals(false, test.contains(null));
  }

  //-------------------------------------------------------------------------
  public void test_toStringList() {
    ExternalIdBundleWithDates test = new ExternalIdBundleWithDates(_idwd11, _idwd12);
    assertEquals(Arrays.asList(_idwd11.toString(), _idwd12.toString()), test.toStringList());
  }

  public void test_toStringList_empty() {
    ExternalIdBundleWithDates test = new ExternalIdBundleWithDates();
    assertEquals(new ArrayList<String>(), test.toStringList());
  }

  //-------------------------------------------------------------------------
  public void test_compareTo_differentSizes() {
    ExternalIdBundleWithDates a1 = new ExternalIdBundleWithDates();
    ExternalIdBundleWithDates a2 = new ExternalIdBundleWithDates(_idwd11);
    
    assertEquals(true, a1.compareTo(a1) == 0);
    assertEquals(true, a1.compareTo(a2) < 0);
    assertEquals(true, a2.compareTo(a1) > 0);
    assertEquals(true, a2.compareTo(a2) == 0);
  }

  public void test_compareTo_sameSizes() {
    ExternalIdBundleWithDates a1 = new ExternalIdBundleWithDates(_idwd11);
    ExternalIdBundleWithDates a2 = new ExternalIdBundleWithDates(_idwd12);
    
    assertEquals(true, a1.compareTo(a1) == 0);
    assertEquals(true, a1.compareTo(a2) < 0);
    assertEquals(true, a2.compareTo(a1) > 0);
    assertEquals(true, a2.compareTo(a2) == 0);
  }

  //-------------------------------------------------------------------------
  public void test_equals_same_empty() {
    ExternalIdBundleWithDates a1 = new ExternalIdBundleWithDates();
    ExternalIdBundleWithDates a2 = new ExternalIdBundleWithDates();
    
    assertEquals(true, a1.equals(a1));
    assertEquals(true, a1.equals(a2));
    assertEquals(true, a2.equals(a1));
    assertEquals(true, a2.equals(a2));
  }

  public void test_equals_same_nonEmpty() {
    ExternalIdBundleWithDates a1 = new ExternalIdBundleWithDates(_idwd11, _idwd12);
    ExternalIdBundleWithDates a2 = new ExternalIdBundleWithDates(_idwd11, _idwd12);
    
    assertEquals(true, a1.equals(a1));
    assertEquals(true, a1.equals(a2));
    assertEquals(true, a2.equals(a1));
    assertEquals(true, a2.equals(a2));
  }

  public void test_equals_different() {
    ExternalIdBundleWithDates a = new ExternalIdBundleWithDates();
    ExternalIdBundleWithDates b = new ExternalIdBundleWithDates(_idwd11, _idwd12);
    
    assertEquals(true, a.equals(a));
    assertEquals(false, a.equals(b));
    assertEquals(false, b.equals(a));
    assertEquals(true, b.equals(b));
    
    assertEquals(false, b.equals("Rubbish"));
    assertEquals(false, b.equals(null));
  }

  public void test_hashCode() {
    ExternalIdBundleWithDates a = new ExternalIdBundleWithDates(_idwd11, _idwd12);
    ExternalIdBundleWithDates b = new ExternalIdBundleWithDates(_idwd11, _idwd12);
    
    assertEquals(a.hashCode(), b.hashCode());
  }

  public void test_toString_empty() {
    ExternalIdBundleWithDates test = new ExternalIdBundleWithDates();
    assertEquals("BundleWithDates[]", test.toString());
  }

  public void test_toString_nonEmpty() {
    ExternalIdBundleWithDates test = new ExternalIdBundleWithDates(_idwd11, _idwd12);
    assertEquals("BundleWithDates[" + _idwd11.toString() + ", " + _idwd12.toString() + "]", test.toString());
  }

}
