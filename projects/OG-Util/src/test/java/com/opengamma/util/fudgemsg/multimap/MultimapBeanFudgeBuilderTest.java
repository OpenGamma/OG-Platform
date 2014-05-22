/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.fudgemsg.multimap;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import org.testng.annotations.Test;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.SortedSetMultimap;
import com.google.common.collect.TreeMultimap;
import com.opengamma.util.fudgemsg.FlexiBeanFudgeBuilder;
import com.opengamma.util.test.AbstractFudgeBuilderTestCase;
import com.opengamma.util.test.TestGroup;

/**
 * Tests the {@link FlexiBeanFudgeBuilder} class.
 */
@Test(groups = TestGroup.UNIT)
public class MultimapBeanFudgeBuilderTest extends AbstractFudgeBuilderTestCase {

  public void test_empty_simple_multimap() {
    SimpleMultimapMockBean bean = SimpleMultimapMockBean.builder()
        .simpleMultimap(ArrayListMultimap.<String, String>create())
        .build();
    assertThat(cycleObject(SimpleMultimapMockBean.class, bean), is(bean));
  }

  public void test_non_empty_simple_multimap() {
    ListMultimap<String, String> mmap = ArrayListMultimap.create();
    mmap.put("one", "1");
    mmap.put("one", "42");
    mmap.put("two", "2");

    SimpleMultimapMockBean bean = SimpleMultimapMockBean.builder()
        .simpleMultimap(mmap)
        .build();
    assertThat(cycleObject(SimpleMultimapMockBean.class, bean), is(bean));
  }

  public void test_empty_list_multimap() {
    ListMultimapMockBean bean = ListMultimapMockBean.builder()
        .listMultimap(ArrayListMultimap.<String, String>create())
        .build();
    assertThat(cycleObject(ListMultimapMockBean.class, bean), is(bean));
  }

  public void test_non_empty_list_multimap() {
    ListMultimap<String, String> mmap = ArrayListMultimap.create();
    mmap.put("one", "1");
    mmap.put("one", "42");
    mmap.put("two", "2");

    ListMultimapMockBean bean = ListMultimapMockBean.builder()
        .listMultimap(mmap)
        .build();
    assertThat(cycleObject(ListMultimapMockBean.class, bean), is(bean));
  }

  public void test_empty_hash_multimap() {
    HashMultimapMockBean bean = HashMultimapMockBean.builder()
        .hashMultimap(HashMultimap.<String, String>create())
        .build();
    assertThat(cycleObject(HashMultimapMockBean.class, bean), is(bean));
  }

  public void test_non_empty_hash_multimap() {
    HashMultimap<String, String> mmap = HashMultimap.create();
    mmap.put("one", "1");
    mmap.put("one", "42");
    mmap.put("two", "2");
    HashMultimapMockBean bean = HashMultimapMockBean.builder()
        .hashMultimap(mmap)
        .build();
    assertThat(cycleObject(HashMultimapMockBean.class, bean), is(bean));
  }

  public void test_empty_sorted_multimap() {
    SortedMultimapMockBean bean = SortedMultimapMockBean.builder()
        .sortedMultimap(TreeMultimap.<String, String>create())
        .build();
    assertThat(cycleObject(SortedMultimapMockBean.class, bean), is(bean));
  }

  public void test_non_empty_sorted_multimap() {
    SortedSetMultimap<String, String> mmap = TreeMultimap.create();
    mmap.put("one", "1");
    mmap.put("one", "42");
    mmap.put("two", "2");
    SortedMultimapMockBean bean = SortedMultimapMockBean.builder()
        .sortedMultimap(mmap)
        .build();
    assertThat(cycleObject(SortedMultimapMockBean.class, bean), is(bean));
  }

  public void test_combined_multimap() {
    ListMultimap<String, String> lmmap = ArrayListMultimap.create();
    lmmap.put("one", "1");
    lmmap.put("one", "42");
    lmmap.put("two", "2");

    HashMultimap<String, String> hmmap = HashMultimap.create();
    hmmap.put("three", "100");
    hmmap.put("three", "4200");
    hmmap.put("four", "200");

    SortedSetMultimap<String, String> smmap = TreeMultimap.create();
    smmap.put("five", "-1");
    smmap.put("five", "-42");
    smmap.put("six", "-2");

    HashMultimap<String, String> mmap = HashMultimap.create();
    mmap.put("five", "-1");
    mmap.put("five", "-42");
    mmap.put("six", "-2");

    CombinedMultimapMockBean bean = CombinedMultimapMockBean.builder()
        .hashMultimap(hmmap)
        .listMultimap(lmmap)
        .sortedMultimap(smmap)
        .noTypeMultimap(mmap)
        .build();
    assertThat(cycleObject(CombinedMultimapMockBean.class, bean), is(bean));
  }

}
