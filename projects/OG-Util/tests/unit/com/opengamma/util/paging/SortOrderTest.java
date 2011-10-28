/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.paging;

import static org.testng.AssertJUnit.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.joda.beans.impl.flexi.FlexiBean;
import org.testng.annotations.Test;

/**
 * Test SortOrder.
 */
@Test
public final class SortOrderTest {

  private static FlexiBean BL = new FlexiBean();
  private static FlexiBean BM = new FlexiBean();
  private static FlexiBean AN = new FlexiBean();
  static {
    BL.append("basic", "B");
    BL.append("field", "L");
    BM.append("basic", "B");
    BM.append("field", "M");
    AN.append("basic", "A");
    AN.append("field", "N");
  }

  public void test_factory_of_natural() {
    SortOrder test = SortOrder.of();
    assertEquals(0, test.getSortElements().size());
    List<FlexiBean> list = Arrays.asList(AN, BL, BM);
    test.sort(list);
    assertEquals(Arrays.asList(AN, BL, BM), list);
  }

  public void test_factory_of_oneElement_asc() {
    SortOrder test = SortOrder.of(SortElement.ofAscending(BL.metaBean().metaProperty("field")));
    assertEquals(1, test.getSortElements().size());
    assertEquals(true, test.getSortElements().get(0).isAscending());
    assertEquals(false, test.getSortElements().get(0).isDescending());
    assertEquals(BL.metaBean().metaProperty("field"), test.getSortElements().get(0).getBeanQuery());
    List<FlexiBean> list = Arrays.asList(AN, BL, BM);
    test.sort(list);
    assertEquals(Arrays.asList(BL, BM, AN), list);
  }

  public void test_factory_of_oneElement_desc() {
    SortOrder test = SortOrder.of(SortElement.ofDescending(BL.metaBean().metaProperty("field")));
    assertEquals(1, test.getSortElements().size());
    assertEquals(false, test.getSortElements().get(0).isAscending());
    assertEquals(true, test.getSortElements().get(0).isDescending());
    assertEquals(BL.metaBean().metaProperty("field"), test.getSortElements().get(0).getBeanQuery());
    List<FlexiBean> list = Arrays.asList(AN, BL, BM);
    test.sort(list);
    assertEquals(Arrays.asList(AN, BM, BL), list);
  }

  public void test_factory_of_twoElements_asc_asc() {
    SortOrder test = SortOrder.of(SortElement.ofAscending(BL.metaBean().metaProperty("basic")), SortElement.ofAscending(BL.metaBean().metaProperty("field")));
    assertEquals(2, test.getSortElements().size());
    assertEquals(true, test.getSortElements().get(0).isAscending());
    assertEquals(false, test.getSortElements().get(0).isDescending());
    assertEquals(BL.metaBean().metaProperty("basic"), test.getSortElements().get(0).getBeanQuery());
    assertEquals(true, test.getSortElements().get(1).isAscending());
    assertEquals(false, test.getSortElements().get(1).isDescending());
    assertEquals(BL.metaBean().metaProperty("field"), test.getSortElements().get(1).getBeanQuery());
    List<FlexiBean> list = Arrays.asList(AN, BM, BL);
    test.sort(list);
    assertEquals(Arrays.asList(AN, BL, BM), list);
  }

  public void test_factory_of_twoElements_desc_desc() {
    SortOrder test = SortOrder.of(SortElement.ofDescending(BL.metaBean().metaProperty("basic")), SortElement.ofDescending(BL.metaBean().metaProperty("field")));
    assertEquals(2, test.getSortElements().size());
    assertEquals(false, test.getSortElements().get(0).isAscending());
    assertEquals(true, test.getSortElements().get(0).isDescending());
    assertEquals(BL.metaBean().metaProperty("basic"), test.getSortElements().get(0).getBeanQuery());
    assertEquals(false, test.getSortElements().get(1).isAscending());
    assertEquals(true, test.getSortElements().get(1).isDescending());
    assertEquals(BL.metaBean().metaProperty("field"), test.getSortElements().get(1).getBeanQuery());
    List<FlexiBean> list = Arrays.asList(AN, BM, BL);
    test.sort(list);
    assertEquals(Arrays.asList(BM, BL, AN), list);
  }

  public void test_factory_of_twoElements_desc_asc() {
    SortOrder test = SortOrder.of(SortElement.ofDescending(BL.metaBean().metaProperty("basic")), SortElement.ofAscending(BL.metaBean().metaProperty("field")));
    assertEquals(2, test.getSortElements().size());
    assertEquals(false, test.getSortElements().get(0).isAscending());
    assertEquals(true, test.getSortElements().get(0).isDescending());
    assertEquals(BL.metaBean().metaProperty("basic"), test.getSortElements().get(0).getBeanQuery());
    assertEquals(true, test.getSortElements().get(1).isAscending());
    assertEquals(false, test.getSortElements().get(1).isDescending());
    assertEquals(BL.metaBean().metaProperty("field"), test.getSortElements().get(1).getBeanQuery());
    List<FlexiBean> list = Arrays.asList(AN, BM, BL);
    test.sort(list);
    assertEquals(Arrays.asList(BL, BM, AN), list);
  }

  //-------------------------------------------------------------------------
  public void test_toString() {
    SortOrder test = SortOrder.of(SortElement.ofAscending(BL.metaBean().metaProperty("field")));
    assertEquals("SortOrder[[FlexiBean:field ASC]]", test.toString());
  }

}
