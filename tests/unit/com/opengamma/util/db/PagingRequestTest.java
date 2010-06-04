/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.util.db;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Test PagingRequest.
 */
public final class PagingRequestTest {

  @Test
  public void test_ALL() {
    PagingRequest test = PagingRequest.ALL;
    assertEquals(1, test.getPage());
    assertEquals(Integer.MAX_VALUE, test.getPagingSize());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_constructor() {
    PagingRequest test = new PagingRequest();
    assertEquals(1, test.getPage());
    assertEquals(20, test.getPagingSize());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_constructor_1int() {
    PagingRequest test = new PagingRequest(2);
    assertEquals(2, test.getPage());
    assertEquals(20, test.getPagingSize());
  }

  @Test(expected=IllegalArgumentException.class)
  public void test_constructor_1int_page0() {
    new PagingRequest(0);
  }

  @Test(expected=IllegalArgumentException.class)
  public void test_constructor_1int_pageNegative() {
    new PagingRequest(-1);
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_constructor_2ints() {
    PagingRequest test = new PagingRequest(2, 40);
    assertEquals(2, test.getPage());
    assertEquals(40, test.getPagingSize());
  }

  @Test(expected=IllegalArgumentException.class)
  public void test_constructor_2ints_page0() {
    new PagingRequest(0, 40);
  }

  @Test(expected=IllegalArgumentException.class)
  public void test_constructor_2ints_pageNegative() {
    new PagingRequest(-1, 40);
  }

  @Test(expected=IllegalArgumentException.class)
  public void test_constructor_2ints_pagingSize0() {
    new PagingRequest(1, 0);
  }

  @Test(expected=IllegalArgumentException.class)
  public void test_constructor_2ints_pagingSizeNegative() {
    new PagingRequest(1, -1);
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_of_2ints() {
    PagingRequest test = PagingRequest.of(2, 40);
    assertEquals(2, test.getPage());
    assertEquals(40, test.getPagingSize());
  }

  public void test_of_2ints_page0() {
    PagingRequest test = PagingRequest.of(0, 40);
    assertEquals(1, test.getPage());
    assertEquals(40, test.getPagingSize());
  }

  public void test_of_2ints_pagingSize0() {
    PagingRequest test = PagingRequest.of(2, 0);
    assertEquals(2, test.getPage());
    assertEquals(20, test.getPagingSize());
  }

  @Test(expected=IllegalArgumentException.class)
  public void test_of_2ints_pageNegative() {
    new PagingRequest(-1, 40);
  }

  @Test(expected=IllegalArgumentException.class)
  public void test_of_2ints_pagingSizeNegative() {
    new PagingRequest(1, -1);
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_getItems_page1() {
    PagingRequest test = new PagingRequest(1, 20);
    assertEquals(1, test.getFirstItem());
    assertEquals(0, test.getFirstItemIndex());
    assertEquals(20, test.getLastItem());
    assertEquals(20, test.getLastItemIndex());
  }

  @Test
  public void test_getItems_page2() {
    PagingRequest test = new PagingRequest(2, 20);
    assertEquals(21, test.getFirstItem());
    assertEquals(20, test.getFirstItemIndex());
    assertEquals(40, test.getLastItem());
    assertEquals(40, test.getLastItemIndex());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_equals_equal() {
    PagingRequest test1 = new PagingRequest(1, 20);
    PagingRequest test2 = new PagingRequest(1, 20);
    assertEquals(true, test1.equals(test1));
    assertEquals(true, test1.equals(test2));
    assertEquals(true, test2.equals(test1));
    assertEquals(true, test2.equals(test2));
  }

  @Test
  public void test_equals_notEqualPage() {
    PagingRequest test1 = new PagingRequest(1, 20);
    PagingRequest test2 = new PagingRequest(2, 20);
    assertEquals(false, test1.equals(test2));
    assertEquals(false, test2.equals(test1));
  }

  @Test
  public void test_equals_notEqualPagingSize() {
    PagingRequest test1 = new PagingRequest(1, 20);
    PagingRequest test2 = new PagingRequest(1, 30);
    assertEquals(false, test1.equals(test2));
    assertEquals(false, test2.equals(test1));
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_hashCode_equal() {
    PagingRequest test1 = new PagingRequest(2, 40);
    PagingRequest test2 = new PagingRequest(2, 40);
    assertEquals(test1.hashCode(), test2.hashCode());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_toString() {
    PagingRequest test = new PagingRequest(2, 40);
    assertEquals("PagingRequest[page=2, pagingSize=40]", test.toString());
  }

}
