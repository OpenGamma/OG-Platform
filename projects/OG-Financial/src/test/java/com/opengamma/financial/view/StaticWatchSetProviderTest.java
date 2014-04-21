/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.view;

import static org.testng.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collections;

import org.testng.annotations.Test;

import com.google.common.collect.ImmutableSet;
import com.opengamma.id.ObjectId;
import com.opengamma.util.test.TestGroup;

/**
 * Tests the {@link StaticWatchSetProvider} class.
 */
@Test(groups = TestGroup.UNIT)
public class StaticWatchSetProviderTest {

  public void testEmptyWatchSet() {
    final StaticWatchSetProvider provider = new StaticWatchSetProvider(Collections.emptyMap());
    assertEquals(provider.getAdditionalWatchSet(Collections.<ObjectId>emptySet()), Collections.emptySet());
    assertEquals(provider.getAdditionalWatchSet(Collections.<ObjectId>singleton(ObjectId.of("Test", "Foo"))), Collections.emptySet());
  }

  public void test_string_string() {
    final StaticWatchSetProvider provider = new StaticWatchSetProvider(Collections.singletonMap("Test~Foo", "Test~Bar"));
    assertEquals(provider.getAdditionalWatchSet(Collections.<ObjectId>emptySet()), Collections.emptySet());
    assertEquals(provider.getAdditionalWatchSet(Collections.<ObjectId>singleton(ObjectId.of("Test", "Foo"))), Collections.<ObjectId>singleton(ObjectId.of("Test", "Bar")));
    assertEquals(provider.getAdditionalWatchSet(Collections.<ObjectId>singleton(ObjectId.of("Test", "Bar"))), Collections.emptySet());
  }

  public void test_string_objectId() {
    final StaticWatchSetProvider provider = new StaticWatchSetProvider(Collections.singletonMap("Test~Foo", ObjectId.of("Test", "Bar")));
    assertEquals(provider.getAdditionalWatchSet(Collections.<ObjectId>emptySet()), Collections.emptySet());
    assertEquals(provider.getAdditionalWatchSet(Collections.<ObjectId>singleton(ObjectId.of("Test", "Foo"))), Collections.<ObjectId>singleton(ObjectId.of("Test", "Bar")));
    assertEquals(provider.getAdditionalWatchSet(Collections.<ObjectId>singleton(ObjectId.of("Test", "Bar"))), Collections.emptySet());
  }

  public void test_string_collection() {
    final StaticWatchSetProvider provider = new StaticWatchSetProvider(Collections.singletonMap("Test~Foo", Arrays.asList("Test~Bar1", ObjectId.of("Test", "Bar2"))));
    assertEquals(provider.getAdditionalWatchSet(Collections.<ObjectId>emptySet()), Collections.emptySet());
    assertEquals(provider.getAdditionalWatchSet(Collections.<ObjectId>singleton(ObjectId.of("Test", "Foo"))),
        ImmutableSet.<ObjectId>of(ObjectId.of("Test", "Bar1"), ObjectId.of("Test", "Bar2")));
    assertEquals(provider.getAdditionalWatchSet(Collections.<ObjectId>singleton(ObjectId.of("Test", "Bar1"))), Collections.emptySet());
    assertEquals(provider.getAdditionalWatchSet(Collections.<ObjectId>singleton(ObjectId.of("Test", "Bar2"))), Collections.emptySet());
  }

  public void test_objectId_string() {
    final StaticWatchSetProvider provider = new StaticWatchSetProvider(Collections.singletonMap(ObjectId.of("Test", "Foo"), "Test~Bar"));
    assertEquals(provider.getAdditionalWatchSet(Collections.<ObjectId>emptySet()), Collections.emptySet());
    assertEquals(provider.getAdditionalWatchSet(Collections.<ObjectId>singleton(ObjectId.of("Test", "Foo"))), Collections.<ObjectId>singleton(ObjectId.of("Test", "Bar")));
    assertEquals(provider.getAdditionalWatchSet(Collections.<ObjectId>singleton(ObjectId.of("Test", "Bar"))), Collections.emptySet());
  }

}
