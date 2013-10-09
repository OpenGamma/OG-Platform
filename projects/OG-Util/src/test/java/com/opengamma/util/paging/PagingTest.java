/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.paging;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import java.util.Arrays;
import java.util.NoSuchElementException;

import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;

/**
 * Test Paging.
 */
@Test(groups = TestGroup.UNIT)
public final class PagingTest {

  public void test_factory_of_Collection_empty() {
    Paging test = Paging.ofAll(Arrays.asList());
    assertEquals(PagingRequest.ALL, test.getRequest());
    assertEquals(0, test.getTotalItems());
  }

  public void test_factory_of_Collection_sizeTwo() {
    Paging test = Paging.ofAll(Arrays.asList("Hello", "There"));
    assertEquals(PagingRequest.ALL, test.getRequest());
    assertEquals(2, test.getTotalItems());
  }

  //-------------------------------------------------------------------------
  public void test_factory_of_Collection_PagingRequest_empty() {
    PagingRequest request = PagingRequest.ofPage(1, 20);
    Paging test = Paging.of(request, Arrays.asList());
    assertEquals(request, test.getRequest());
    assertEquals(0, test.getTotalItems());
  }

  public void test_factory_of_Collection_PagingRequest_sizeTwo() {
    PagingRequest request = PagingRequest.ofPage(1, 20);
    Paging test = Paging.of(request, Arrays.asList("Hello", "There"));
    assertEquals(request, test.getRequest());
    assertEquals(2, test.getTotalItems());
  }

  //-------------------------------------------------------------------------
  public void test_factory_of_PagingRequest_int() {
    PagingRequest request = PagingRequest.ofPage(1, 20);
    Paging test = Paging.of(request, 32);
    assertEquals(request, test.getRequest());
    assertEquals(32, test.getTotalItems());
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_factory_of_PagingRequest_int_totalItemsNegative() {
    Paging.of(PagingRequest.ofPage(1, 20), -1);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_factory_of_PagingRequest_int_null() {
    Paging.of(null, 0);
  }

  //-------------------------------------------------------------------------
  public void test_getItems_page1() {
    Paging test = Paging.of(PagingRequest.ofPage(1, 20), 52);
    assertEquals(1, test.getPageNumber());
    assertEquals(0, test.getFirstItem());
    assertEquals(1, test.getFirstItemOneBased());
    assertEquals(20, test.getLastItem());
    assertEquals(20, test.getLastItemOneBased());
  }

  public void test_getItems_page2() {
    Paging test = Paging.of(PagingRequest.ofPage(2, 20), 52);
    assertEquals(2, test.getPageNumber());
    assertEquals(20, test.getFirstItem());
    assertEquals(21, test.getFirstItemOneBased());
    assertEquals(40, test.getLastItem());
    assertEquals(40, test.getLastItemOneBased());
  }

  public void test_getItems_page3() {
    Paging test = Paging.of(PagingRequest.ofPage(3, 20), 52);
    assertEquals(3, test.getPageNumber());
    assertEquals(40, test.getFirstItem());
    assertEquals(41, test.getFirstItemOneBased());
    assertEquals(52, test.getLastItem());
    assertEquals(52, test.getLastItemOneBased());
  }

  public void test_getTotalPages() {
    assertEquals(2, Paging.of(PagingRequest.ofPage(1, 20), 39).getTotalPages());
    assertEquals(2, Paging.of(PagingRequest.ofPage(1, 20), 40).getTotalPages());
    assertEquals(3, Paging.of(PagingRequest.ofPage(1, 20), 41).getTotalPages());
  }

  //-------------------------------------------------------------------------
  public void test_isSizeOnly() {
    assertTrue(Paging.of(PagingRequest.NONE, 39).isSizeOnly());
    assertFalse(Paging.of(PagingRequest.ofPage(1, 20), 39).isSizeOnly());
  }

  public void test_isNextPage() {
    assertFalse(Paging.of(PagingRequest.ofPage(2, 20), 39).isNextPage());
    assertFalse(Paging.of(PagingRequest.ofPage(2, 20), 40).isNextPage());
    assertTrue(Paging.of(PagingRequest.ofPage(2, 20), 41).isNextPage());
    assertTrue(Paging.of(PagingRequest.ofPage(1, 20), 39).isNextPage());
    assertTrue(Paging.of(PagingRequest.ofPage(1, 20), 40).isNextPage());
  }

  public void test_isLastPage() {
    assertTrue(Paging.of(PagingRequest.ofPage(2, 20), 39).isLastPage());
    assertTrue(Paging.of(PagingRequest.ofPage(2, 20), 40).isLastPage());
    assertFalse(Paging.of(PagingRequest.ofPage(2, 20), 41).isLastPage());
    assertFalse(Paging.of(PagingRequest.ofPage(1, 20), 39).isLastPage());
    assertFalse(Paging.of(PagingRequest.ofPage(1, 20), 40).isLastPage());
  }

  public void test_isPreviousPage() {
    assertTrue(Paging.of(PagingRequest.ofPage(2, 20), 39).isPreviousPage());
    assertTrue(Paging.of(PagingRequest.ofPage(2, 20), 40).isPreviousPage());
    assertTrue(Paging.of(PagingRequest.ofPage(2, 20), 41).isPreviousPage());
    assertFalse(Paging.of(PagingRequest.ofPage(1, 20), 39).isPreviousPage());
    assertFalse(Paging.of(PagingRequest.ofPage(1, 20), 40).isPreviousPage());
  }

  public void test_isFirstPage() {
    assertFalse(Paging.of(PagingRequest.ofPage(2, 20), 39).isFirstPage());
    assertFalse(Paging.of(PagingRequest.ofPage(2, 20), 40).isFirstPage());
    assertFalse(Paging.of(PagingRequest.ofPage(2, 20), 41).isFirstPage());
    assertTrue(Paging.of(PagingRequest.ofPage(1, 20), 39).isFirstPage());
    assertTrue(Paging.of(PagingRequest.ofPage(1, 20), 40).isFirstPage());
  }

  //-------------------------------------------------------------------------
  public void test_toPagingRequest() {
    assertEquals(PagingRequest.ofPage(2, 20), Paging.of(PagingRequest.ofPage(2, 20), 39).toPagingRequest());
    assertEquals(PagingRequest.NONE, Paging.of(PagingRequest.NONE, 349).toPagingRequest());
  }

  public void test_nextPagingRequest() {
    assertEquals(PagingRequest.ofPage(2, 20), Paging.of(PagingRequest.ofPage(1, 20), 39).nextPagingRequest());
  }

  @Test(expectedExceptions = IllegalStateException.class)
  public void test_nextPagingRequest_pagingSizeZero() {
    Paging.of(PagingRequest.NONE, 39).nextPagingRequest();
  }

  @Test(expectedExceptions = NoSuchElementException.class)
  public void test_nextPagingRequest_lastPage() {
    Paging.of(PagingRequest.ofPage(2, 20), 39).nextPagingRequest();
  }

  public void test_previousPagingRequest() {
    assertEquals(PagingRequest.ofPage(1, 20), Paging.of(PagingRequest.ofPage(2, 20), 39).previousPagingRequest());
  }

  @Test(expectedExceptions = IllegalStateException.class)
  public void test_previousPagingRequest_pagingSizeZero() {
    Paging.of(PagingRequest.NONE, 39).previousPagingRequest();
  }

  @Test(expectedExceptions = NoSuchElementException.class)
  public void test_previousPagingRequest_lastPage() {
    Paging.of(PagingRequest.ofPage(1, 20), 39).previousPagingRequest();
  }

  //-------------------------------------------------------------------------
  public void test_equals_equal() {
    Paging test1 = Paging.of(PagingRequest.ofPage(1, 20), 52);
    Paging test2 = Paging.of(PagingRequest.ofPage(1, 20), 52);
    assertEquals(true, test1.equals(test1));
    assertEquals(true, test1.equals(test2));
    assertEquals(true, test2.equals(test1));
    assertEquals(true, test2.equals(test2));
  }

  public void test_equals_notEqualPage() {
    Paging test1 = Paging.of(PagingRequest.ofPage(1, 20), 52);
    Paging test2 = Paging.of(PagingRequest.ofPage(2, 20), 52);
    assertEquals(false, test1.equals(test2));
    assertEquals(false, test2.equals(test1));
  }

  public void test_equals_notEqualPagingSize() {
    Paging test1 = Paging.of(PagingRequest.ofPage(1, 20), 52);
    Paging test2 = Paging.of(PagingRequest.NONE, 52);
    assertEquals(false, test1.equals(test2));
    assertEquals(false, test2.equals(test1));
  }

  public void test_equals_notEqualTotalItems() {
    Paging test1 = Paging.of(PagingRequest.ofPage(1, 20), 52);
    Paging test2 = Paging.of(PagingRequest.ofPage(1, 20), 12);
    assertEquals(false, test1.equals(test2));
    assertEquals(false, test2.equals(test1));
  }

  public void test_equals_other() {
    Paging test = Paging.of(PagingRequest.ofPage(1, 20), 52);
    assertEquals(false, test.equals(""));
    assertEquals(false, test.equals(null));
  }

  //-------------------------------------------------------------------------
  public void test_hashCode_equal() {
    Paging test1 = Paging.of(PagingRequest.ofPage(1, 20), 52);
    Paging test2 = Paging.of(PagingRequest.ofPage(1, 20), 52);
    assertEquals(test1.hashCode(), test2.hashCode());
  }

  //-------------------------------------------------------------------------
  public void test_toString() {
    Paging test = Paging.of(PagingRequest.ofPage(1, 20), 52);
    assertEquals("Paging[first=0, size=20, totalItems=52]", test.toString());
  }

}
