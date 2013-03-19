/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.id;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.testng.annotations.Test;

import com.google.common.collect.Sets;
import com.opengamma.util.test.TestGroup;

/**
 * Test {@link ExternalIdSearch}.
 */
@Test(groups = TestGroup.UNIT)
public class ExternalIdSearchTest {

  private final ExternalId _id11 = ExternalId.of("D1", "V1");
  private final ExternalId _id21 = ExternalId.of("D2", "V1");
  private final ExternalId _id12 = ExternalId.of("D1", "V2");

  //-------------------------------------------------------------------------
  public void test_constructor_noargs() {
    ExternalIdSearch test = new ExternalIdSearch();
    assertEquals(0, test.size());
  }

  //-------------------------------------------------------------------------
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_constructor_ExternalId_null() {
    new ExternalIdSearch((ExternalId) null);
  }

  public void test_constructor_ExternalId() {
    ExternalIdSearch test = new ExternalIdSearch(_id11);
    assertEquals(1, test.size());
    assertEquals(Sets.newHashSet(_id11), test.getExternalIds());
    assertEquals(ExternalIdSearchType.ANY, test.getSearchType());
  }

  //-------------------------------------------------------------------------
  public void test_constructor_varargs_noExternalIds() {
    ExternalId[] args = new ExternalId[0];
    ExternalIdSearch test = new ExternalIdSearch(args);
    assertEquals(0, test.size());
  }

  public void test_constructor_varargs_oneExternalId() {
    ExternalId[] args = new ExternalId[] {_id11};
    ExternalIdSearch test = new ExternalIdSearch(args);
    assertEquals(1, test.size());
    assertEquals(Sets.newHashSet(_id11), test.getExternalIds());
  }

  public void test_constructor_varargs_twoExternalIds() {
    ExternalId[] args = new ExternalId[] {_id11, _id12};
    ExternalIdSearch test = new ExternalIdSearch(args);
    assertEquals(2, test.size());
    assertEquals(Sets.newHashSet(_id11, _id12), test.getExternalIds());
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_constructor_varargs_null() {
    ExternalId[] args = null;
    new ExternalIdSearch(args);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_constructor_varargs_noNulls() {
    ExternalId[] args = new ExternalId[] {_id11, null, _id12};
    new ExternalIdSearch(args);
  }

  //-------------------------------------------------------------------------
  public void test_constructor_Iterable_empty() {
    ExternalIdSearch test = new ExternalIdSearch(new ArrayList<ExternalId>());
    assertEquals(0, test.size());
  }

  public void test_constructor_Iterable_two() {
    ExternalIdSearch test = new ExternalIdSearch(Arrays.asList(_id11, _id12));
    assertEquals(2, test.size());
    assertEquals(Sets.newHashSet(_id11, _id12), test.getExternalIds());
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_constructor_Iterable_null() {
    new ExternalIdSearch((Iterable<ExternalId>) null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_constructor_Iterable_noNulls() {
    new ExternalIdSearch(Arrays.asList(_id11, null, _id12));
  }

  //-------------------------------------------------------------------------
  public void test_constructor_IterableType_empty() {
    ExternalIdSearch test = new ExternalIdSearch(new ArrayList<ExternalId>(), ExternalIdSearchType.EXACT);
    assertEquals(0, test.size());
    assertEquals(ExternalIdSearchType.EXACT, test.getSearchType());
  }

  public void test_constructor_IterableType_two() {
    ExternalIdSearch test = new ExternalIdSearch(Arrays.asList(_id11, _id12), ExternalIdSearchType.EXACT);
    assertEquals(2, test.size());
    assertEquals(Sets.newHashSet(_id11, _id12), test.getExternalIds());
    assertEquals(ExternalIdSearchType.EXACT, test.getSearchType());
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_constructor_IterableType_null() {
    new ExternalIdSearch((Iterable<ExternalId>) null, ExternalIdSearchType.EXACT);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_constructor_IterableType_noNulls() {
    new ExternalIdSearch(Arrays.asList(_id11, null, _id12), ExternalIdSearchType.EXACT);
  }

  //-------------------------------------------------------------------------
  public void test_singleExternalIdDifferentConstructors() {
    assertTrue(new ExternalIdSearch(_id11).equals(new ExternalIdSearch(Collections.singleton(_id11))));
  }

  public void test_singleVersusMultipleExternalId() {
    assertFalse(new ExternalIdSearch(_id11).equals(new ExternalIdSearch(_id11, _id12)));
    assertFalse(new ExternalIdSearch(_id11, _id12).equals(new ExternalIdSearch(_id11)));
  }

  //-------------------------------------------------------------------------
  public void test_addExternalId() {
    ExternalIdSearch test = new ExternalIdSearch(ExternalId.of("A", "B"));
    assertEquals(1, test.size());
    test.addExternalId(ExternalId.of("A", "C"));
    assertEquals(2, test.size());
    assertTrue(test.getExternalIds().contains(ExternalId.of("A", "B")));
    assertTrue(test.getExternalIds().contains(ExternalId.of("A", "C")));
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_addExternalId_null() {
    ExternalIdSearch test = new ExternalIdSearch(ExternalId.of("A", "B"));
    test.addExternalId(null);
  }

  //-------------------------------------------------------------------------
  public void test_removeExternalId_match() {
    ExternalIdSearch test = new ExternalIdSearch(ExternalId.of("A", "B"));
    assertEquals(1, test.size());
    test.removeExternalId(ExternalId.of("A", "B"));
    assertEquals(0, test.size());
  }

  public void test_removeExternalId_noMatch() {
    ExternalIdSearch test = new ExternalIdSearch(ExternalId.of("A", "B"));
    assertEquals(1, test.size());
    test.removeExternalId(ExternalId.of("A", "C"));
    assertEquals(1, test.size());
    assertTrue(test.getExternalIds().contains(ExternalId.of("A", "B")));
  }

  public void test_removeExternalId_null() {
    ExternalIdSearch test = new ExternalIdSearch(ExternalId.of("A", "B"));
    assertEquals(1, test.size());
    test.removeExternalId(null);
    assertEquals(1, test.size());
    assertTrue(test.getExternalIds().contains(ExternalId.of("A", "B")));
  }

  //-------------------------------------------------------------------------
  public void test_setSearchType() {
    ExternalIdSearch test = new ExternalIdSearch(ExternalId.of("A", "B"));
    assertEquals(ExternalIdSearchType.ANY, test.getSearchType());
    test.setSearchType(ExternalIdSearchType.EXACT);
    assertEquals(ExternalIdSearchType.EXACT, test.getSearchType());
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_setSearchType_null() {
    ExternalIdSearch test = new ExternalIdSearch(ExternalId.of("A", "B"));
    test.setSearchType(null);
  }

  //-------------------------------------------------------------------------
  public void test_size() {
    assertEquals(0, new ExternalIdSearch().size());
    assertEquals(1, new ExternalIdSearch(_id11).size());
    assertEquals(2, new ExternalIdSearch(_id11, _id12).size());
  }

  //-------------------------------------------------------------------------
  public void test_iterator() {
    Set<ExternalId> expected = new HashSet<ExternalId>();
    expected.add(_id11);
    expected.add(_id12);
    Iterable<ExternalId> base = new ExternalIdSearch(_id11, _id12);
    Iterator<ExternalId> test = base.iterator();
    assertEquals(true, test.hasNext());
    assertEquals(true, expected.remove(test.next()));
    assertEquals(true, test.hasNext());
    assertEquals(true, expected.remove(test.next()));
    assertEquals(false, test.hasNext());
    assertEquals(0, expected.size());
  }

  //-------------------------------------------------------------------------
  public void test_matches1_EXACT() {
    ExternalIdSearch test1 = new ExternalIdSearch(_id11);
    test1.setSearchType(ExternalIdSearchType.EXACT);
    assertEquals(true, test1.matches(_id11));
    assertEquals(false, test1.matches(_id21));
    
    ExternalIdSearch test2 = new ExternalIdSearch(_id11, _id21);
    test2.setSearchType(ExternalIdSearchType.EXACT);
    assertEquals(false, test2.matches(_id11));
    assertEquals(false, test2.matches(_id12));
    assertEquals(false, test2.matches(_id21));
  }

  public void test_matches1_ALL() {
    ExternalIdSearch test1 = new ExternalIdSearch(_id11);
    test1.setSearchType(ExternalIdSearchType.ALL);
    assertEquals(true, test1.matches(_id11));
    assertEquals(false, test1.matches(_id12));
    
    ExternalIdSearch test2 = new ExternalIdSearch(_id11, _id21);
    test2.setSearchType(ExternalIdSearchType.ALL);
    assertEquals(false, test2.matches(_id11));
    assertEquals(false, test2.matches(_id12));
    assertEquals(false, test2.matches(_id21));
  }

  public void test_matches1_ANY() {
    ExternalIdSearch test1 = new ExternalIdSearch(_id11);
    test1.setSearchType(ExternalIdSearchType.ANY);
    assertEquals(true, test1.matches(_id11));
    assertEquals(false, test1.matches(_id12));
    
    ExternalIdSearch test2 = new ExternalIdSearch(_id11, _id21);
    test2.setSearchType(ExternalIdSearchType.ANY);
    assertEquals(true, test2.matches(_id11));
    assertEquals(false, test2.matches(_id12));
    assertEquals(true, test2.matches(_id21));
  }

  public void test_matches1_NONE() {
    ExternalIdSearch test1 = new ExternalIdSearch(_id11);
    test1.setSearchType(ExternalIdSearchType.NONE);
    assertEquals(false, test1.matches(_id11));
    assertEquals(true, test1.matches(_id12));
    
    ExternalIdSearch test2 = new ExternalIdSearch(_id11, _id21);
    test2.setSearchType(ExternalIdSearchType.NONE);
    assertEquals(false, test2.matches(_id11));
    assertEquals(true, test2.matches(_id12));
    assertEquals(false, test2.matches(_id21));
  }

  //-------------------------------------------------------------------------
  public void test_matches_EXACT() {
    ExternalIdSearch test = new ExternalIdSearch(_id11, _id12);
    test.setSearchType(ExternalIdSearchType.EXACT);
    assertEquals(true, test.matches(new ExternalIdSearch(_id11, _id12)));
    assertEquals(false, test.matches(new ExternalIdSearch(_id11, _id12, _id21)));
    assertEquals(false, test.matches(new ExternalIdSearch(_id11)));
    assertEquals(false, test.matches(new ExternalIdSearch(_id12)));
    assertEquals(false, test.matches(new ExternalIdSearch(_id21)));
    assertEquals(false, test.matches(new ExternalIdSearch()));
  }

  public void test_matches_ALL() {
    ExternalIdSearch test = new ExternalIdSearch(_id11, _id12);
    test.setSearchType(ExternalIdSearchType.ALL);
    assertEquals(true, test.matches(new ExternalIdSearch(_id11, _id12)));
    assertEquals(true, test.matches(new ExternalIdSearch(_id11, _id12, _id21)));
    assertEquals(false, test.matches(new ExternalIdSearch(_id11)));
    assertEquals(false, test.matches(new ExternalIdSearch(_id12)));
    assertEquals(false, test.matches(new ExternalIdSearch(_id21)));
    assertEquals(false, test.matches(new ExternalIdSearch()));
  }

  public void test_matches_ANY() {
    ExternalIdSearch test = new ExternalIdSearch(_id11, _id12);
    test.setSearchType(ExternalIdSearchType.ANY);
    assertEquals(true, test.matches(new ExternalIdSearch(_id11, _id12)));
    assertEquals(true, test.matches(new ExternalIdSearch(_id11, _id12, _id21)));
    assertEquals(true, test.matches(new ExternalIdSearch(_id11)));
    assertEquals(true, test.matches(new ExternalIdSearch(_id12)));
    assertEquals(false, test.matches(new ExternalIdSearch(_id21)));
    assertEquals(false, test.matches(new ExternalIdSearch()));
  }

  public void test_matches_NONE() {
    ExternalIdSearch test = new ExternalIdSearch(_id11, _id12);
    test.setSearchType(ExternalIdSearchType.NONE);
    assertEquals(false, test.matches(new ExternalIdSearch(_id11, _id12)));
    assertEquals(false, test.matches(new ExternalIdSearch(_id11, _id12, _id21)));
    assertEquals(false, test.matches(new ExternalIdSearch(_id11)));
    assertEquals(false, test.matches(new ExternalIdSearch(_id12)));
    assertEquals(true, test.matches(new ExternalIdSearch(_id21)));
    assertEquals(true, test.matches(new ExternalIdSearch()));
  }

  //-------------------------------------------------------------------------
  public void test_containsAll1() {
    ExternalIdSearch test = new ExternalIdSearch(_id11);
    assertEquals(false, test.containsAll(new ExternalIdSearch(_id11, _id12)));
    assertEquals(true, test.containsAll(new ExternalIdSearch(_id11)));
    assertEquals(false, test.containsAll(new ExternalIdSearch(_id12)));
    assertEquals(false, test.containsAll(new ExternalIdSearch(_id21)));
    assertEquals(true, test.containsAll(new ExternalIdSearch()));
  }

  public void test_containsAll2() {
    ExternalIdSearch test = new ExternalIdSearch(_id11, _id12);
    assertEquals(true, test.containsAll(new ExternalIdSearch(_id11, _id12)));
    assertEquals(true, test.containsAll(new ExternalIdSearch(_id11)));
    assertEquals(true, test.containsAll(new ExternalIdSearch(_id12)));
    assertEquals(false, test.containsAll(new ExternalIdSearch(_id21)));
    assertEquals(true, test.containsAll(new ExternalIdSearch()));
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_containsAll_null() {
    ExternalIdSearch test = new ExternalIdSearch(_id11, _id12);
    test.containsAll(null);
  }

  //-------------------------------------------------------------------------
  public void test_containsAny() {
    ExternalIdSearch test = new ExternalIdSearch(_id11, _id12);
    assertEquals(true, test.containsAny(new ExternalIdSearch(_id11, _id12)));
    assertEquals(true, test.containsAny(new ExternalIdSearch(_id11)));
    assertEquals(true, test.containsAny(new ExternalIdSearch(_id12)));
    assertEquals(false, test.containsAny(new ExternalIdSearch(_id21)));
    assertEquals(false, test.containsAny(new ExternalIdSearch()));
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_containsAny_null() {
    ExternalIdSearch test = new ExternalIdSearch(_id11, _id12);
    test.containsAny(null);
  }

  //-------------------------------------------------------------------------
  public void test_contains() {
    ExternalIdSearch test = new ExternalIdSearch(_id11, _id12);
    assertEquals(true, test.contains(_id11));
    assertEquals(true, test.contains(_id11));
    assertEquals(false, test.contains(_id21));
  }

  public void test_contains_null() {
    ExternalIdSearch test = new ExternalIdSearch(_id11, _id12);
    assertEquals(false, test.contains(null));
  }

  //-------------------------------------------------------------------------
  public void test_canMatch_EXACT() {
    ExternalIdSearch test = new ExternalIdSearch();
    test.setSearchType(ExternalIdSearchType.EXACT);
    assertEquals(false, ExternalIdSearch.canMatch(test));
    test.addExternalId(_id11);
    assertEquals(true, ExternalIdSearch.canMatch(test));
  }

  public void test_canMatch_ALL() {
    ExternalIdSearch test = new ExternalIdSearch();
    test.setSearchType(ExternalIdSearchType.ALL);
    assertEquals(false, ExternalIdSearch.canMatch(test));
    test.addExternalId(_id11);
    assertEquals(true, ExternalIdSearch.canMatch(test));
  }

  public void test_canMatch_ANY() {
    ExternalIdSearch test = new ExternalIdSearch();
    test.setSearchType(ExternalIdSearchType.ANY);
    assertEquals(false, ExternalIdSearch.canMatch(test));
    test.addExternalId(_id11);
    assertEquals(true, ExternalIdSearch.canMatch(test));
  }

  public void test_canMatch_NONE() {
    ExternalIdSearch test = new ExternalIdSearch();
    test.setSearchType(ExternalIdSearchType.NONE);
    assertEquals(true, ExternalIdSearch.canMatch(test));
    test.addExternalId(_id11);
    assertEquals(true, ExternalIdSearch.canMatch(test));
  }

  public void test_canMatch_null() {
    assertEquals(true, ExternalIdSearch.canMatch(null));
  }

  //-------------------------------------------------------------------------
  public void test_equals_same_empty() {
    ExternalIdSearch a1 = new ExternalIdSearch();
    ExternalIdSearch a2 = new ExternalIdSearch();
    
    assertEquals(true, a1.equals(a1));
    assertEquals(true, a1.equals(a2));
    assertEquals(true, a2.equals(a1));
    assertEquals(true, a2.equals(a2));
  }

  public void test_equals_same_nonEmpty() {
    ExternalIdSearch a1 = new ExternalIdSearch(_id11, _id12);
    ExternalIdSearch a2 = new ExternalIdSearch(_id11, _id12);
    
    assertEquals(true, a1.equals(a1));
    assertEquals(true, a1.equals(a2));
    assertEquals(true, a2.equals(a1));
    assertEquals(true, a2.equals(a2));
  }

  public void test_equals_different() {
    ExternalIdSearch a = new ExternalIdSearch();
    ExternalIdSearch b = new ExternalIdSearch(_id11, _id12);
    
    assertEquals(true, a.equals(a));
    assertEquals(false, a.equals(b));
    assertEquals(false, b.equals(a));
    assertEquals(true, b.equals(b));
    
    assertEquals(false, b.equals("Rubbish"));
    assertEquals(false, b.equals(null));
  }

  public void test_hashCode() {
    ExternalIdSearch a = new ExternalIdSearch(_id11, _id12);
    ExternalIdSearch b = new ExternalIdSearch(_id11, _id12);
    
    assertEquals(a.hashCode(), b.hashCode());
  }

  public void test_toString_empty() {
    ExternalIdSearch test = new ExternalIdSearch();
    assertEquals("Search[]", test.toString());
  }

  public void test_toString_nonEmpty() {
    ExternalIdSearch test = new ExternalIdSearch(_id11, _id12);
    assertEquals("Search[" + _id11.toString() + ", " + _id12.toString() + "]", test.toString());
  }

}
