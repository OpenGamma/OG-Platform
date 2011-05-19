/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.db;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import java.util.Arrays;
import java.util.NoSuchElementException;

import org.testng.annotations.Test;

/**
 * Test Paging.
 */
@Test
public final class PagingTest {

  public void test_factory_of_Collection_empty() {
    Paging test = Paging.of(Arrays.asList());
    assertEquals(1, test.getPage());
    assertEquals(Integer.MAX_VALUE, test.getPagingSize());
    assertEquals(0, test.getTotalItems());
  }

  public void test_factory_of_Collection_sizeTwo() {
    Paging test = Paging.of(Arrays.asList("Hello", "There"));
    assertEquals(1, test.getPage());
    assertEquals(Integer.MAX_VALUE, test.getPagingSize());
    assertEquals(2, test.getTotalItems());
  }

  //-------------------------------------------------------------------------
  public void test_factory_of_Collection_PagingRequest_empty() {
    PagingRequest request = PagingRequest.of(1, 20);
    Paging test = Paging.of(request, Arrays.asList());
    assertEquals(1, test.getPage());
    assertEquals(20, test.getPagingSize());
    assertEquals(0, test.getTotalItems());
  }

  public void test_factory_of_Collection_PagingRequest_sizeTwo() {
    PagingRequest request = PagingRequest.of(1, 20);
    Paging test = Paging.of(request, Arrays.asList("Hello", "There"));
    assertEquals(1, test.getPage());
    assertEquals(20, test.getPagingSize());
    assertEquals(2, test.getTotalItems());
  }

  //-------------------------------------------------------------------------
  public void test_factory_of_PagingRequest_int() {
    Paging test = Paging.of(PagingRequest.of(1, 20), 32);
    assertEquals(1, test.getPage());
    assertEquals(20, test.getPagingSize());
    assertEquals(32, test.getTotalItems());
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_factory_of_PagingRequest_int_totalItemsNegative() {
    Paging.of(PagingRequest.of(1, 20), -1);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_factory_of_PagingRequest_int_null() {
    Paging.of(null, 0);
  }

  //-------------------------------------------------------------------------
  public void test_factory_of_3ints() {
    Paging test = Paging.of(1, 20, 32);
    assertEquals(1, test.getPage());
    assertEquals(20, test.getPagingSize());
    assertEquals(32, test.getTotalItems());
  }

  public void test_factory_of_3ints_empty() {
    Paging test = Paging.of(1, 20, 0);
    assertEquals(1, test.getPage());
    assertEquals(20, test.getPagingSize());
    assertEquals(0, test.getTotalItems());
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_factory_of_3ints_page0() {
    Paging.of(0, 20, 32);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_factory_of_3ints_pageNegative() {
    Paging.of(-1, 20, 32);
  }

  public void test_factory_of_3ints_pagingSize0() {
    Paging test = Paging.of(1, 0, 0);
    assertEquals(1, test.getPage());
    assertEquals(0, test.getPagingSize());
    assertEquals(0, test.getTotalItems());
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_factory_of_3ints_pagingSizeNegative() {
    Paging.of(1, -1, 32);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_factory_of_3ints_totalItemsNegative() {
    Paging.of(1, 20, -1);
  }

  //-------------------------------------------------------------------------
  public void test_getItems_page1() {
    Paging test = Paging.of(1, 20, 52);
    assertEquals(1, test.getFirstItem());
    assertEquals(0, test.getFirstItemIndex());
    assertEquals(20, test.getLastItem());
    assertEquals(20, test.getLastItemIndex());
  }

  public void test_getItems_page2() {
    Paging test = Paging.of(2, 20, 52);
    assertEquals(21, test.getFirstItem());
    assertEquals(20, test.getFirstItemIndex());
    assertEquals(40, test.getLastItem());
    assertEquals(40, test.getLastItemIndex());
  }

  public void test_getItems_page3() {
    Paging test = Paging.of(3, 20, 52);
    assertEquals(41, test.getFirstItem());
    assertEquals(40, test.getFirstItemIndex());
    assertEquals(52, test.getLastItem());
    assertEquals(52, test.getLastItemIndex());
  }

  public void test_getTotalPages() {
    assertEquals(2, Paging.of(1, 20, 39).getTotalPages());
    assertEquals(2, Paging.of(1, 20, 40).getTotalPages());
    assertEquals(3, Paging.of(1, 20, 41).getTotalPages());
  }

  //-------------------------------------------------------------------------
  public void test_isSizeOnly() {
    assertTrue(Paging.of(1, 0, 39).isSizeOnly());
    assertFalse(Paging.of(1, 20, 39).isSizeOnly());
  }

  public void test_isNextPage() {
    assertFalse(Paging.of(2, 20, 39).isNextPage());
    assertFalse(Paging.of(2, 20, 40).isNextPage());
    assertTrue(Paging.of(2, 20, 41).isNextPage());
    assertTrue(Paging.of(1, 20, 39).isNextPage());
    assertTrue(Paging.of(1, 20, 40).isNextPage());
  }

  public void test_isLastPage() {
    assertTrue(Paging.of(2, 20, 39).isLastPage());
    assertTrue(Paging.of(2, 20, 40).isLastPage());
    assertFalse(Paging.of(2, 20, 41).isLastPage());
    assertFalse(Paging.of(1, 20, 39).isLastPage());
    assertFalse(Paging.of(1, 20, 40).isLastPage());
  }

  public void test_isPreviousPage() {
    assertTrue(Paging.of(2, 20, 39).isPreviousPage());
    assertTrue(Paging.of(2, 20, 40).isPreviousPage());
    assertTrue(Paging.of(2, 20, 41).isPreviousPage());
    assertFalse(Paging.of(1, 20, 39).isPreviousPage());
    assertFalse(Paging.of(1, 20, 40).isPreviousPage());
  }

  public void test_isFirstPage() {
    assertFalse(Paging.of(2, 20, 39).isFirstPage());
    assertFalse(Paging.of(2, 20, 40).isFirstPage());
    assertFalse(Paging.of(2, 20, 41).isFirstPage());
    assertTrue(Paging.of(1, 20, 39).isFirstPage());
    assertTrue(Paging.of(1, 20, 40).isFirstPage());
  }

  //-------------------------------------------------------------------------
  public void test_toPagingRequest() {
    assertEquals(PagingRequest.of(2, 20), Paging.of(2, 20, 39).toPagingRequest());
    assertEquals(new PagingRequest(1, 0), Paging.of(1, 0, 349).toPagingRequest());
  }

  public void test_nextPagingRequest() {
    assertEquals(PagingRequest.of(2, 20), Paging.of(1, 20, 39).nextPagingRequest());
  }

  @Test(expectedExceptions = IllegalStateException.class)
  public void test_nextPagingRequest_pagingSizeZero() {
    Paging.of(1, 0, 39).nextPagingRequest();
  }

  @Test(expectedExceptions = NoSuchElementException.class)
  public void test_nextPagingRequest_lastPage() {
    Paging.of(2, 20, 39).nextPagingRequest();
  }

  public void test_previousPagingRequest() {
    assertEquals(PagingRequest.of(1, 20), Paging.of(2, 20, 39).previousPagingRequest());
  }

  @Test(expectedExceptions = IllegalStateException.class)
  public void test_previousPagingRequest_pagingSizeZero() {
    Paging.of(1, 0, 39).previousPagingRequest();
  }

  @Test(expectedExceptions = NoSuchElementException.class)
  public void test_previousPagingRequest_lastPage() {
    Paging.of(1, 20, 39).previousPagingRequest();
  }

  //-------------------------------------------------------------------------
  public void test_equals_equal() {
    Paging test1 = Paging.of(1, 20, 52);
    Paging test2 = Paging.of(1, 20, 52);
    assertEquals(true, test1.equals(test1));
    assertEquals(true, test1.equals(test2));
    assertEquals(true, test2.equals(test1));
    assertEquals(true, test2.equals(test2));
  }

  public void test_equals_notEqualPage() {
    Paging test1 = Paging.of(1, 20, 52);
    Paging test2 = Paging.of(2, 20, 52);
    assertEquals(false, test1.equals(test2));
    assertEquals(false, test2.equals(test1));
  }

  public void test_equals_notEqualPagingSize() {
    Paging test1 = Paging.of(1, 20, 52);
    Paging test2 = Paging.of(1, 30, 52);
    assertEquals(false, test1.equals(test2));
    assertEquals(false, test2.equals(test1));
  }

  public void test_equals_notEqualTotalItems() {
    Paging test1 = Paging.of(1, 20, 52);
    Paging test2 = Paging.of(1, 20, 12);
    assertEquals(false, test1.equals(test2));
    assertEquals(false, test2.equals(test1));
  }

  //-------------------------------------------------------------------------
  public void test_hashCode_equal() {
    Paging test1 = Paging.of(1, 20, 52);
    Paging test2 = Paging.of(1, 20, 52);
    assertEquals(test1.hashCode(), test2.hashCode());
  }

  //-------------------------------------------------------------------------
  public void test_toString() {
    Paging test = Paging.of(1, 20, 52);
    assertEquals("Paging[page=1, pagingSize=20, totalItems=52]", test.toString());
  }

}
