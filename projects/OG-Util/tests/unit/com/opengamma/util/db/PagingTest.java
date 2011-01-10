/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.util.db;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.junit.Test;

/**
 * Test Paging.
 */
public final class PagingTest {

  @Test
  public void test_factory_of_Collection_empty() {
    Paging test = Paging.of(Arrays.asList());
    assertEquals(1, test.getPage());
    assertEquals(Integer.MAX_VALUE, test.getPagingSize());
    assertEquals(0, test.getTotalItems());
  }

  @Test
  public void test_factory_of_Collection_sizeTwo() {
    Paging test = Paging.of(Arrays.asList("Hello", "There"));
    assertEquals(1, test.getPage());
    assertEquals(Integer.MAX_VALUE, test.getPagingSize());
    assertEquals(2, test.getTotalItems());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_factory_of_Collection_PagingRequest_empty() {
    PagingRequest request = new PagingRequest(1, 20);
    Paging test = Paging.of(Arrays.asList(), request);
    assertEquals(1, test.getPage());
    assertEquals(20, test.getPagingSize());
    assertEquals(0, test.getTotalItems());
  }

  @Test
  public void test_factory_of_Collection_PagingRequest_sizeTwo() {
    PagingRequest request = new PagingRequest(1, 20);
    Paging test = Paging.of(Arrays.asList("Hello", "There"), request);
    assertEquals(1, test.getPage());
    assertEquals(20, test.getPagingSize());
    assertEquals(2, test.getTotalItems());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_constructor_3ints() {
    Paging test = new Paging(1, 20, 32);
    assertEquals(1, test.getPage());
    assertEquals(20, test.getPagingSize());
    assertEquals(32, test.getTotalItems());
  }

  @Test
  public void test_constructor_3ints_empty() {
    Paging test = new Paging(1, 20, 0);
    assertEquals(1, test.getPage());
    assertEquals(20, test.getPagingSize());
    assertEquals(0, test.getTotalItems());
  }

  @Test(expected=IllegalArgumentException.class)
  public void test_constructor_3ints_page0() {
    new Paging(0, 20, 32);
  }

  @Test(expected=IllegalArgumentException.class)
  public void test_constructor_3ints_pageNegative() {
    new Paging(-1, 20, 32);
  }

  @Test(expected=IllegalArgumentException.class)
  public void test_constructor_3ints_pagingSize0() {
    new Paging(1, 0, 32);
  }

  @Test(expected=IllegalArgumentException.class)
  public void test_constructor_3ints_pagingSizeNegative() {
    new Paging(1, -1, 32);
  }

  @Test(expected=IllegalArgumentException.class)
  public void test_constructor_3ints_totalItemsNegative() {
    new Paging(1, 20, -1);
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_constructor_PagingRequest_int() {
    Paging test = new Paging(new PagingRequest(1, 20), 32);
    assertEquals(1, test.getPage());
    assertEquals(20, test.getPagingSize());
    assertEquals(32, test.getTotalItems());
  }

  @Test(expected=IllegalArgumentException.class)
  public void test_constructor_PagingRequest_int_totalItemsNegative() {
    new Paging(new PagingRequest(1, 20), -1);
  }

  @Test(expected=IllegalArgumentException.class)
  public void test_constructor_PagingRequest_int_null() {
    new Paging(null, 0);
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_getItems_page1() {
    Paging test = new Paging(1, 20, 52);
    assertEquals(1, test.getFirstItem());
    assertEquals(0, test.getFirstItemIndex());
    assertEquals(20, test.getLastItem());
    assertEquals(20, test.getLastItemIndex());
  }

  @Test
  public void test_getItems_page2() {
    Paging test = new Paging(2, 20, 52);
    assertEquals(21, test.getFirstItem());
    assertEquals(20, test.getFirstItemIndex());
    assertEquals(40, test.getLastItem());
    assertEquals(40, test.getLastItemIndex());
  }

  @Test
  public void test_getItems_page3() {
    Paging test = new Paging(3, 20, 52);
    assertEquals(41, test.getFirstItem());
    assertEquals(40, test.getFirstItemIndex());
    assertEquals(52, test.getLastItem());
    assertEquals(52, test.getLastItemIndex());
  }

  @Test
  public void test_getTotalPages() {
    assertEquals(2, new Paging(1, 20, 39).getTotalPages());
    assertEquals(2, new Paging(1, 20, 40).getTotalPages());
    assertEquals(3, new Paging(1, 20, 41).getTotalPages());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_equals_equal() {
    Paging test1 = new Paging(1, 20, 52);
    Paging test2 = new Paging(1, 20, 52);
    assertEquals(true, test1.equals(test1));
    assertEquals(true, test1.equals(test2));
    assertEquals(true, test2.equals(test1));
    assertEquals(true, test2.equals(test2));
  }

  @Test
  public void test_equals_notEqualPage() {
    Paging test1 = new Paging(1, 20, 52);
    Paging test2 = new Paging(2, 20, 52);
    assertEquals(false, test1.equals(test2));
    assertEquals(false, test2.equals(test1));
  }

  @Test
  public void test_equals_notEqualPagingSize() {
    Paging test1 = new Paging(1, 20, 52);
    Paging test2 = new Paging(1, 30, 52);
    assertEquals(false, test1.equals(test2));
    assertEquals(false, test2.equals(test1));
  }

  @Test
  public void test_equals_notEqualTotalItems() {
    Paging test1 = new Paging(1, 20, 52);
    Paging test2 = new Paging(1, 20, 12);
    assertEquals(false, test1.equals(test2));
    assertEquals(false, test2.equals(test1));
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_hashCode_equal() {
    Paging test1 = new Paging(1, 20, 52);
    Paging test2 = new Paging(1, 20, 52);
    assertEquals(test1.hashCode(), test2.hashCode());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_toString() {
    Paging test = new Paging(1, 20, 52);
    assertEquals("Paging[page=1, pagingSize=20, totalItems=52]", test.toString());
  }

}
