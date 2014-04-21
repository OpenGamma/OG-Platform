/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.config.impl;

import static org.testng.Assert.assertSame;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.mockito.Mockito;
import org.testng.annotations.Test;
import org.threeten.bp.Instant;

import com.google.common.collect.ImmutableMap;
import com.opengamma.core.change.ChangeManager;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.core.convention.Convention;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.test.TestGroup;

/**
 * Tests the {@link VersionLockedConfigSource} class.
 */
@Test(groups = TestGroup.UNIT)
@SuppressWarnings("deprecation")
public class VersionLockedConfigSourceTest {

  public void test_get_Collection() {
    final ConfigSource underlying = Mockito.mock(ConfigSource.class);
    final ConfigSource test = new VersionLockedConfigSource(underlying, VersionCorrection.LATEST);
    final Collection<UniqueId> params = Arrays.asList(UniqueId.of("Test", "Foo"), UniqueId.of("Test", "Bar"));
    final Map<UniqueId, ConfigItem<?>> result = ImmutableMap.<UniqueId, ConfigItem<?>>of(UniqueId.of("Test", "Foo"), Mockito.mock(ConfigItem.class), UniqueId.of("Test", "Bar"),
        Mockito.mock(ConfigItem.class));
    Mockito.when(underlying.get(params)).thenReturn(result);
    assertSame(test.get(params), result);
  }

  public void test_get_Collection_VersionCorrection() {
    final ConfigSource underlying = Mockito.mock(ConfigSource.class);
    final Instant t1 = Instant.ofEpochMilli(1L);
    final Instant t2 = Instant.ofEpochMilli(2L);
    final Instant t3 = Instant.ofEpochMilli(3L);
    final Instant t4 = Instant.ofEpochMilli(4L);
    final ConfigSource test = new VersionLockedConfigSource(underlying, VersionCorrection.of(t1, t2));
    Collection<ObjectId> ids = Arrays.asList(ObjectId.of("Test", "Foo"), ObjectId.of("Test", "Bar"));
    Map<ObjectId, ConfigItem<?>> result = ImmutableMap.<ObjectId, ConfigItem<?>>of(ObjectId.of("Test", "Foo"), Mockito.mock(ConfigItem.class), ObjectId.of("Test", "Bar"),
        Mockito.mock(ConfigItem.class));
    Mockito.when(underlying.get(ids, VersionCorrection.of(t1, t2))).thenReturn(result);
    assertSame(test.get(ids, VersionCorrection.LATEST), result);
    assertSame(test.get(ids, VersionCorrection.ofVersionAsOf(t1)), result);
    assertSame(test.get(ids, VersionCorrection.ofCorrectedTo(t2)), result);
    result = ImmutableMap.<ObjectId, ConfigItem<?>>of(ObjectId.of("Test", "Foo"), Mockito.mock(ConfigItem.class), ObjectId.of("Test", "Bar"), Mockito.mock(ConfigItem.class));
    Mockito.when(underlying.get(ids, VersionCorrection.of(t3, t2))).thenReturn(result);
    assertSame(test.get(ids, VersionCorrection.ofVersionAsOf(t3)), result);
    result = ImmutableMap.<ObjectId, ConfigItem<?>>of(ObjectId.of("Test", "Foo"), Mockito.mock(ConfigItem.class), ObjectId.of("Test", "Bar"), Mockito.mock(ConfigItem.class));
    Mockito.when(underlying.get(ids, VersionCorrection.of(t1, t3))).thenReturn(result);
    assertSame(test.get(ids, VersionCorrection.ofCorrectedTo(t3)), result);
    result = ImmutableMap.<ObjectId, ConfigItem<?>>of(ObjectId.of("Test", "Foo"), Mockito.mock(ConfigItem.class), ObjectId.of("Test", "Bar"), Mockito.mock(ConfigItem.class));
    Mockito.when(underlying.get(ids, VersionCorrection.of(t3, t4))).thenReturn(result);
    assertSame(test.get(ids, VersionCorrection.of(t3, t4)), result);
  }

  public void test_changeManager() {
    final ConfigSource underlying = Mockito.mock(ConfigSource.class);
    final ConfigSource test = new VersionLockedConfigSource(underlying, VersionCorrection.LATEST);
    final ChangeManager result = Mockito.mock(ChangeManager.class);
    Mockito.when(underlying.changeManager()).thenReturn(result);
    assertSame(test.changeManager(), result);
  }

  @SuppressWarnings({"unchecked", "rawtypes" })
  public void test_get_UniqueId() {
    final ConfigSource underlying = Mockito.mock(ConfigSource.class);
    final ConfigSource test = new VersionLockedConfigSource(underlying, VersionCorrection.LATEST);
    final ConfigItem result = Mockito.mock(ConfigItem.class);
    Mockito.when(underlying.get(UniqueId.of("Test", "Foo"))).thenReturn(result);
    assertSame(test.get(UniqueId.of("Test", "Foo")), result);
  }

  @SuppressWarnings({"unchecked", "rawtypes" })
  public void test_get_ObjectId_VersionCorrection() {
    final ConfigSource underlying = Mockito.mock(ConfigSource.class);
    final Instant t1 = Instant.ofEpochMilli(1L);
    final Instant t2 = Instant.ofEpochMilli(2L);
    final Instant t3 = Instant.ofEpochMilli(3L);
    final Instant t4 = Instant.ofEpochMilli(4L);
    final ConfigSource test = new VersionLockedConfigSource(underlying, VersionCorrection.of(t1, t2));
    ConfigItem result = Mockito.mock(ConfigItem.class);
    Mockito.when(underlying.get(ObjectId.of("Test", "Foo"), VersionCorrection.of(t1, t2))).thenReturn(result);
    assertSame(test.get(ObjectId.of("Test", "Foo"), VersionCorrection.LATEST), result);
    assertSame(test.get(ObjectId.of("Test", "Foo"), VersionCorrection.ofVersionAsOf(t1)), result);
    assertSame(test.get(ObjectId.of("Test", "Foo"), VersionCorrection.ofCorrectedTo(t2)), result);
    result = Mockito.mock(ConfigItem.class);
    Mockito.when(underlying.get(ObjectId.of("Test", "Foo"), VersionCorrection.of(t3, t2))).thenReturn(result);
    assertSame(test.get(ObjectId.of("Test", "Foo"), VersionCorrection.ofVersionAsOf(t3)), result);
    result = Mockito.mock(ConfigItem.class);
    Mockito.when(underlying.get(ObjectId.of("Test", "Foo"), VersionCorrection.of(t1, t3))).thenReturn(result);
    assertSame(test.get(ObjectId.of("Test", "Foo"), VersionCorrection.ofCorrectedTo(t3)), result);
    result = Mockito.mock(ConfigItem.class);
    Mockito.when(underlying.get(ObjectId.of("Test", "Foo"), VersionCorrection.of(t3, t4))).thenReturn(result);
    assertSame(test.get(ObjectId.of("Test", "Foo"), VersionCorrection.of(t3, t4)), result);
  }

  @SuppressWarnings({"unchecked", "rawtypes" })
  public void test_Class_String_VersionCorrection() {
    final ConfigSource underlying = Mockito.mock(ConfigSource.class);
    final Instant t1 = Instant.ofEpochMilli(1L);
    final Instant t2 = Instant.ofEpochMilli(2L);
    final Instant t3 = Instant.ofEpochMilli(3L);
    final Instant t4 = Instant.ofEpochMilli(4L);
    final ConfigSource test = new VersionLockedConfigSource(underlying, VersionCorrection.of(t1, t2));
    Collection result = Collections.singleton(Mockito.mock(ConfigItem.class));
    Mockito.when(underlying.get(Convention.class, "Foo", VersionCorrection.of(t1, t2))).thenReturn(result);
    assertSame(test.get(Convention.class, "Foo", VersionCorrection.LATEST), result);
    assertSame(test.get(Convention.class, "Foo", VersionCorrection.ofVersionAsOf(t1)), result);
    assertSame(test.get(Convention.class, "Foo", VersionCorrection.ofCorrectedTo(t2)), result);
    result = Collections.singleton(Mockito.mock(ConfigItem.class));
    Mockito.when(underlying.get(Convention.class, "Foo", VersionCorrection.of(t3, t2))).thenReturn(result);
    assertSame(test.get(Convention.class, "Foo", VersionCorrection.ofVersionAsOf(t3)), result);
    result = Collections.singleton(Mockito.mock(ConfigItem.class));
    Mockito.when(underlying.get(Convention.class, "Foo", VersionCorrection.of(t1, t3))).thenReturn(result);
    assertSame(test.get(Convention.class, "Foo", VersionCorrection.ofCorrectedTo(t3)), result);
    result = Collections.singleton(Mockito.mock(ConfigItem.class));
    Mockito.when(underlying.get(Convention.class, "Foo", VersionCorrection.of(t3, t4))).thenReturn(result);
    assertSame(test.get(Convention.class, "Foo", VersionCorrection.of(t3, t4)), result);
  }

  @SuppressWarnings({"unchecked", "rawtypes" })
  public void test_getAll_Class_VersionCorrection() {
    final ConfigSource underlying = Mockito.mock(ConfigSource.class);
    final Instant t1 = Instant.ofEpochMilli(1L);
    final Instant t2 = Instant.ofEpochMilli(2L);
    final Instant t3 = Instant.ofEpochMilli(3L);
    final Instant t4 = Instant.ofEpochMilli(4L);
    final ConfigSource test = new VersionLockedConfigSource(underlying, VersionCorrection.of(t1, t2));
    Collection result = Collections.singleton(Mockito.mock(ConfigItem.class));
    Mockito.when(underlying.getAll(Convention.class, VersionCorrection.of(t1, t2))).thenReturn(result);
    assertSame(test.getAll(Convention.class, VersionCorrection.LATEST), result);
    assertSame(test.getAll(Convention.class, VersionCorrection.ofVersionAsOf(t1)), result);
    assertSame(test.getAll(Convention.class, VersionCorrection.ofCorrectedTo(t2)), result);
    result = Collections.singleton(Mockito.mock(ConfigItem.class));
    Mockito.when(underlying.getAll(Convention.class, VersionCorrection.of(t3, t2))).thenReturn(result);
    assertSame(test.getAll(Convention.class, VersionCorrection.ofVersionAsOf(t3)), result);
    result = Collections.singleton(Mockito.mock(ConfigItem.class));
    Mockito.when(underlying.getAll(Convention.class, VersionCorrection.of(t1, t3))).thenReturn(result);
    assertSame(test.getAll(Convention.class, VersionCorrection.ofCorrectedTo(t3)), result);
    result = Collections.singleton(Mockito.mock(ConfigItem.class));
    Mockito.when(underlying.getAll(Convention.class, VersionCorrection.of(t3, t4))).thenReturn(result);
    assertSame(test.getAll(Convention.class, VersionCorrection.of(t3, t4)), result);
  }

  public void test_getConfig_Class_UniqueId() {
    final ConfigSource underlying = Mockito.mock(ConfigSource.class);
    final ConfigSource test = new VersionLockedConfigSource(underlying, VersionCorrection.LATEST);
    final Convention result = Mockito.mock(Convention.class);
    Mockito.when(underlying.getConfig(Convention.class, UniqueId.of("Test", "Foo"))).thenReturn(result);
    assertSame(test.getConfig(Convention.class, UniqueId.of("Test", "Foo")), result);
  }

  public void test_getConfig_Class_ObjectId_VersionCorrection() {
    final ConfigSource underlying = Mockito.mock(ConfigSource.class);
    final Instant t1 = Instant.ofEpochMilli(1L);
    final Instant t2 = Instant.ofEpochMilli(2L);
    final Instant t3 = Instant.ofEpochMilli(3L);
    final Instant t4 = Instant.ofEpochMilli(4L);
    final ConfigSource test = new VersionLockedConfigSource(underlying, VersionCorrection.of(t1, t2));
    Convention result = Mockito.mock(Convention.class);
    Mockito.when(underlying.getConfig(Convention.class, ObjectId.of("Test", "Foo"), VersionCorrection.of(t1, t2))).thenReturn(result);
    assertSame(test.getConfig(Convention.class, ObjectId.of("Test", "Foo"), VersionCorrection.LATEST), result);
    assertSame(test.getConfig(Convention.class, ObjectId.of("Test", "Foo"), VersionCorrection.ofVersionAsOf(t1)), result);
    assertSame(test.getConfig(Convention.class, ObjectId.of("Test", "Foo"), VersionCorrection.ofCorrectedTo(t2)), result);
    result = Mockito.mock(Convention.class);
    Mockito.when(underlying.getConfig(Convention.class, ObjectId.of("Test", "Foo"), VersionCorrection.of(t3, t2))).thenReturn(result);
    assertSame(test.getConfig(Convention.class, ObjectId.of("Test", "Foo"), VersionCorrection.ofVersionAsOf(t3)), result);
    result = Mockito.mock(Convention.class);
    Mockito.when(underlying.getConfig(Convention.class, ObjectId.of("Test", "Foo"), VersionCorrection.of(t1, t3))).thenReturn(result);
    assertSame(test.getConfig(Convention.class, ObjectId.of("Test", "Foo"), VersionCorrection.ofCorrectedTo(t3)), result);
    result = Mockito.mock(Convention.class);
    Mockito.when(underlying.getConfig(Convention.class, ObjectId.of("Test", "Foo"), VersionCorrection.of(t3, t4))).thenReturn(result);
    assertSame(test.getConfig(Convention.class, ObjectId.of("Test", "Foo"), VersionCorrection.of(t3, t4)), result);
  }

  public void test_getSingle_Class_String_VersionCorrection() {
    final ConfigSource underlying = Mockito.mock(ConfigSource.class);
    final Instant t1 = Instant.ofEpochMilli(1L);
    final Instant t2 = Instant.ofEpochMilli(2L);
    final Instant t3 = Instant.ofEpochMilli(3L);
    final Instant t4 = Instant.ofEpochMilli(4L);
    final ConfigSource test = new VersionLockedConfigSource(underlying, VersionCorrection.of(t1, t2));
    Convention result = Mockito.mock(Convention.class);
    Mockito.when(underlying.getSingle(Convention.class, "Foo", VersionCorrection.of(t1, t2))).thenReturn(result);
    assertSame(test.getSingle(Convention.class, "Foo", VersionCorrection.LATEST), result);
    assertSame(test.getSingle(Convention.class, "Foo", VersionCorrection.ofVersionAsOf(t1)), result);
    assertSame(test.getSingle(Convention.class, "Foo", VersionCorrection.ofCorrectedTo(t2)), result);
    result = Mockito.mock(Convention.class);
    Mockito.when(underlying.getSingle(Convention.class, "Foo", VersionCorrection.of(t3, t2))).thenReturn(result);
    assertSame(test.getSingle(Convention.class, "Foo", VersionCorrection.ofVersionAsOf(t3)), result);
    result = Mockito.mock(Convention.class);
    Mockito.when(underlying.getSingle(Convention.class, "Foo", VersionCorrection.of(t1, t3))).thenReturn(result);
    assertSame(test.getSingle(Convention.class, "Foo", VersionCorrection.ofCorrectedTo(t3)), result);
    result = Mockito.mock(Convention.class);
    Mockito.when(underlying.getSingle(Convention.class, "Foo", VersionCorrection.of(t3, t4))).thenReturn(result);
    assertSame(test.getSingle(Convention.class, "Foo", VersionCorrection.of(t3, t4)), result);
  }

  public void test_getLatestByName_Class_String() {
    final ConfigSource underlying = Mockito.mock(ConfigSource.class);
    final Instant t1 = Instant.ofEpochMilli(1L);
    final Instant t2 = Instant.ofEpochMilli(2L);
    final ConfigSource test = new VersionLockedConfigSource(underlying, VersionCorrection.of(t1, t2));
    Convention result = Mockito.mock(Convention.class);
    Mockito.when(underlying.getSingle(Convention.class, "Foo", VersionCorrection.of(t1, t2))).thenReturn(result);
    assertSame(test.getLatestByName(Convention.class, "Foo"), result);
  }

}
