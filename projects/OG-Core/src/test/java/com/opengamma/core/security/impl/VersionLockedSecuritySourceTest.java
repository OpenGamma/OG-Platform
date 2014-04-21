/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.security.impl;

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
import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.test.TestGroup;

/**
 * Tests the {@link VersionLockedSecuritySource} class.
 */
@Test(groups = TestGroup.UNIT)
@SuppressWarnings("deprecation")
public class VersionLockedSecuritySourceTest {

  public void test_get_ExternalIdBundle_VersionCorrection() {
    final SecuritySource underlying = Mockito.mock(SecuritySource.class);
    final Instant t1 = Instant.ofEpochMilli(1L);
    final Instant t2 = Instant.ofEpochMilli(2L);
    final Instant t3 = Instant.ofEpochMilli(3L);
    final Instant t4 = Instant.ofEpochMilli(4L);
    final SecuritySource test = new VersionLockedSecuritySource(underlying, VersionCorrection.of(t1, t2));
    Collection<Security> result = Collections.singleton(Mockito.mock(Security.class));
    Mockito.when(underlying.get(ExternalId.of("Test", "Foo").toBundle(), VersionCorrection.of(t1, t2))).thenReturn(result);
    assertSame(test.get(ExternalId.of("Test", "Foo").toBundle(), VersionCorrection.LATEST), result);
    assertSame(test.get(ExternalId.of("Test", "Foo").toBundle(), VersionCorrection.ofVersionAsOf(t1)), result);
    assertSame(test.get(ExternalId.of("Test", "Foo").toBundle(), VersionCorrection.ofCorrectedTo(t2)), result);
    result = Collections.singleton(Mockito.mock(Security.class));
    Mockito.when(underlying.get(ExternalId.of("Test", "Foo").toBundle(), VersionCorrection.of(t3, t2))).thenReturn(result);
    assertSame(test.get(ExternalId.of("Test", "Foo").toBundle(), VersionCorrection.ofVersionAsOf(t3)), result);
    result = Collections.singleton(Mockito.mock(Security.class));
    Mockito.when(underlying.get(ExternalId.of("Test", "Foo").toBundle(), VersionCorrection.of(t1, t3))).thenReturn(result);
    assertSame(test.get(ExternalId.of("Test", "Foo").toBundle(), VersionCorrection.ofCorrectedTo(t3)), result);
    result = Collections.singleton(Mockito.mock(Security.class));
    Mockito.when(underlying.get(ExternalId.of("Test", "Foo").toBundle(), VersionCorrection.of(t3, t4))).thenReturn(result);
    assertSame(test.get(ExternalId.of("Test", "Foo").toBundle(), VersionCorrection.of(t3, t4)), result);
  }

  public void test_getAll_Collection_VersionCorrection() {
    final SecuritySource underlying = Mockito.mock(SecuritySource.class);
    final Instant t1 = Instant.ofEpochMilli(1L);
    final Instant t2 = Instant.ofEpochMilli(2L);
    final Instant t3 = Instant.ofEpochMilli(3L);
    final Instant t4 = Instant.ofEpochMilli(4L);
    final SecuritySource test = new VersionLockedSecuritySource(underlying, VersionCorrection.of(t1, t2));
    Collection<ExternalIdBundle> ids = Arrays.asList(ExternalId.of("Test", "Foo").toBundle(), ExternalId.of("Test", "Bar").toBundle());
    Map<ExternalIdBundle, Collection<Security>> result = ImmutableMap.<ExternalIdBundle, Collection<Security>>of(ExternalId.of("Test", "Foo").toBundle(),
        Collections.singleton(Mockito.mock(Security.class)), ExternalId.of("Test", "Bar").toBundle(), Collections.singleton(Mockito.mock(Security.class)));
    Mockito.when(underlying.getAll(ids, VersionCorrection.of(t1, t2))).thenReturn(result);
    assertSame(test.getAll(ids, VersionCorrection.LATEST), result);
    assertSame(test.getAll(ids, VersionCorrection.ofVersionAsOf(t1)), result);
    assertSame(test.getAll(ids, VersionCorrection.ofCorrectedTo(t2)), result);
    result = ImmutableMap.<ExternalIdBundle, Collection<Security>>of(ExternalId.of("Test", "Foo").toBundle(), Collections.singleton(Mockito.mock(Security.class)),
        ExternalId.of("Test", "Bar").toBundle(), Collections.singleton(Mockito.mock(Security.class)));
    Mockito.when(underlying.getAll(ids, VersionCorrection.of(t3, t2))).thenReturn(result);
    assertSame(test.getAll(ids, VersionCorrection.ofVersionAsOf(t3)), result);
    result = ImmutableMap.<ExternalIdBundle, Collection<Security>>of(ExternalId.of("Test", "Foo").toBundle(), Collections.singleton(Mockito.mock(Security.class)),
        ExternalId.of("Test", "Bar").toBundle(), Collections.singleton(Mockito.mock(Security.class)));
    Mockito.when(underlying.getAll(ids, VersionCorrection.of(t1, t3))).thenReturn(result);
    assertSame(test.getAll(ids, VersionCorrection.ofCorrectedTo(t3)), result);
    result = ImmutableMap.<ExternalIdBundle, Collection<Security>>of(ExternalId.of("Test", "Foo").toBundle(), Collections.singleton(Mockito.mock(Security.class)),
        ExternalId.of("Test", "Bar").toBundle(), Collections.singleton(Mockito.mock(Security.class)));
    Mockito.when(underlying.getAll(ids, VersionCorrection.of(t3, t4))).thenReturn(result);
    assertSame(test.getAll(ids, VersionCorrection.of(t3, t4)), result);
  }

  public void test_get_ExternalIdBundle() {
    final SecuritySource underlying = Mockito.mock(SecuritySource.class);
    final Instant t1 = Instant.ofEpochMilli(1L);
    final Instant t2 = Instant.ofEpochMilli(2L);
    final SecuritySource test = new VersionLockedSecuritySource(underlying, VersionCorrection.of(t1, t2));
    Collection<Security> result = Collections.singleton(Mockito.mock(Security.class));
    Mockito.when(underlying.get(ExternalId.of("Test", "Foo").toBundle(), VersionCorrection.of(t1, t2))).thenReturn(result);
    assertSame(test.get(ExternalId.of("Test", "Foo").toBundle()), result);
  }

  public void test_getSingle_ExternalIdBundle() {
    final SecuritySource underlying = Mockito.mock(SecuritySource.class);
    final Instant t1 = Instant.ofEpochMilli(1L);
    final Instant t2 = Instant.ofEpochMilli(2L);
    final SecuritySource test = new VersionLockedSecuritySource(underlying, VersionCorrection.of(t1, t2));
    Security result = Mockito.mock(Security.class);
    Mockito.when(underlying.getSingle(ExternalId.of("Test", "Foo").toBundle(), VersionCorrection.of(t1, t2))).thenReturn(result);
    assertSame(test.getSingle(ExternalId.of("Test", "Foo").toBundle()), result);
  }

  public void test_getSingle_ExternalIdBundle_VersionCorrection() {
    final SecuritySource underlying = Mockito.mock(SecuritySource.class);
    final Instant t1 = Instant.ofEpochMilli(1L);
    final Instant t2 = Instant.ofEpochMilli(2L);
    final Instant t3 = Instant.ofEpochMilli(3L);
    final Instant t4 = Instant.ofEpochMilli(4L);
    final SecuritySource test = new VersionLockedSecuritySource(underlying, VersionCorrection.of(t1, t2));
    Security result = Mockito.mock(Security.class);
    Mockito.when(underlying.getSingle(ExternalId.of("Test", "Foo").toBundle(), VersionCorrection.of(t1, t2))).thenReturn(result);
    assertSame(test.getSingle(ExternalId.of("Test", "Foo").toBundle(), VersionCorrection.LATEST), result);
    assertSame(test.getSingle(ExternalId.of("Test", "Foo").toBundle(), VersionCorrection.ofVersionAsOf(t1)), result);
    assertSame(test.getSingle(ExternalId.of("Test", "Foo").toBundle(), VersionCorrection.ofCorrectedTo(t2)), result);
    result = Mockito.mock(Security.class);
    Mockito.when(underlying.getSingle(ExternalId.of("Test", "Foo").toBundle(), VersionCorrection.of(t3, t2))).thenReturn(result);
    assertSame(test.getSingle(ExternalId.of("Test", "Foo").toBundle(), VersionCorrection.ofVersionAsOf(t3)), result);
    result = Mockito.mock(Security.class);
    Mockito.when(underlying.getSingle(ExternalId.of("Test", "Foo").toBundle(), VersionCorrection.of(t1, t3))).thenReturn(result);
    assertSame(test.getSingle(ExternalId.of("Test", "Foo").toBundle(), VersionCorrection.ofCorrectedTo(t3)), result);
    result = Mockito.mock(Security.class);
    Mockito.when(underlying.getSingle(ExternalId.of("Test", "Foo").toBundle(), VersionCorrection.of(t3, t4))).thenReturn(result);
    assertSame(test.getSingle(ExternalId.of("Test", "Foo").toBundle(), VersionCorrection.of(t3, t4)), result);
  }

  public void test_getSingle_Collection_VersionCorrection() {
    final SecuritySource underlying = Mockito.mock(SecuritySource.class);
    final Instant t1 = Instant.ofEpochMilli(1L);
    final Instant t2 = Instant.ofEpochMilli(2L);
    final Instant t3 = Instant.ofEpochMilli(3L);
    final Instant t4 = Instant.ofEpochMilli(4L);
    final SecuritySource test = new VersionLockedSecuritySource(underlying, VersionCorrection.of(t1, t2));
    Collection<ExternalIdBundle> ids = Arrays.asList(ExternalId.of("Test", "Foo").toBundle(), ExternalId.of("Test", "Bar").toBundle());
    Map<ExternalIdBundle, Security> result = ImmutableMap.<ExternalIdBundle, Security>of(ExternalId.of("Test", "Foo").toBundle(), Mockito.mock(Security.class), ExternalId.of("Test", "Bar")
        .toBundle(), Mockito.mock(Security.class));
    Mockito.when(underlying.getSingle(ids, VersionCorrection.of(t1, t2))).thenReturn(result);
    assertSame(test.getSingle(ids, VersionCorrection.LATEST), result);
    assertSame(test.getSingle(ids, VersionCorrection.ofVersionAsOf(t1)), result);
    assertSame(test.getSingle(ids, VersionCorrection.ofCorrectedTo(t2)), result);
    result = ImmutableMap.<ExternalIdBundle, Security>of(ExternalId.of("Test", "Foo").toBundle(), Mockito.mock(Security.class), ExternalId.of("Test", "Bar").toBundle(),
        Mockito.mock(Security.class));
    Mockito.when(underlying.getSingle(ids, VersionCorrection.of(t3, t2))).thenReturn(result);
    assertSame(test.getSingle(ids, VersionCorrection.ofVersionAsOf(t3)), result);
    result = ImmutableMap.<ExternalIdBundle, Security>of(ExternalId.of("Test", "Foo").toBundle(), Mockito.mock(Security.class), ExternalId.of("Test", "Bar").toBundle(),
        Mockito.mock(Security.class));
    Mockito.when(underlying.getSingle(ids, VersionCorrection.of(t1, t3))).thenReturn(result);
    assertSame(test.getSingle(ids, VersionCorrection.ofCorrectedTo(t3)), result);
    result = ImmutableMap.<ExternalIdBundle, Security>of(ExternalId.of("Test", "Foo").toBundle(), Mockito.mock(Security.class), ExternalId.of("Test", "Bar").toBundle(),
        Mockito.mock(Security.class));
    Mockito.when(underlying.getSingle(ids, VersionCorrection.of(t3, t4))).thenReturn(result);
    assertSame(test.getSingle(ids, VersionCorrection.of(t3, t4)), result);
  }

  public void test_get_UniqueId() {
    final SecuritySource underlying = Mockito.mock(SecuritySource.class);
    final SecuritySource test = new VersionLockedSecuritySource(underlying, VersionCorrection.LATEST);
    final Security result = Mockito.mock(Security.class);
    Mockito.when(underlying.get(UniqueId.of("Test", "Foo"))).thenReturn(result);
    assertSame(test.get(UniqueId.of("Test", "Foo")), result);
  }

  public void test_get_ObjectId_VersionCorrection() {
    final SecuritySource underlying = Mockito.mock(SecuritySource.class);
    final Instant t1 = Instant.ofEpochMilli(1L);
    final Instant t2 = Instant.ofEpochMilli(2L);
    final Instant t3 = Instant.ofEpochMilli(3L);
    final Instant t4 = Instant.ofEpochMilli(4L);
    final SecuritySource test = new VersionLockedSecuritySource(underlying, VersionCorrection.of(t1, t2));
    Security result = Mockito.mock(Security.class);
    Mockito.when(underlying.get(ObjectId.of("Test", "Foo"), VersionCorrection.of(t1, t2))).thenReturn(result);
    assertSame(test.get(ObjectId.of("Test", "Foo"), VersionCorrection.LATEST), result);
    assertSame(test.get(ObjectId.of("Test", "Foo"), VersionCorrection.ofVersionAsOf(t1)), result);
    assertSame(test.get(ObjectId.of("Test", "Foo"), VersionCorrection.ofCorrectedTo(t2)), result);
    result = Mockito.mock(Security.class);
    Mockito.when(underlying.get(ObjectId.of("Test", "Foo"), VersionCorrection.of(t3, t2))).thenReturn(result);
    assertSame(test.get(ObjectId.of("Test", "Foo"), VersionCorrection.ofVersionAsOf(t3)), result);
    result = Mockito.mock(Security.class);
    Mockito.when(underlying.get(ObjectId.of("Test", "Foo"), VersionCorrection.of(t1, t3))).thenReturn(result);
    assertSame(test.get(ObjectId.of("Test", "Foo"), VersionCorrection.ofCorrectedTo(t3)), result);
    result = Mockito.mock(Security.class);
    Mockito.when(underlying.get(ObjectId.of("Test", "Foo"), VersionCorrection.of(t3, t4))).thenReturn(result);
    assertSame(test.get(ObjectId.of("Test", "Foo"), VersionCorrection.of(t3, t4)), result);
  }

  public void test_get_Collection() {
    final SecuritySource underlying = Mockito.mock(SecuritySource.class);
    final SecuritySource test = new VersionLockedSecuritySource(underlying, VersionCorrection.LATEST);
    final Collection<UniqueId> params = Arrays.asList(UniqueId.of("Test", "Foo"), UniqueId.of("Test", "Bar"));
    final Map<UniqueId, Security> result = ImmutableMap.<UniqueId, Security>of(UniqueId.of("Test", "Foo"), Mockito.mock(Security.class), UniqueId.of("Test", "Bar"),
        Mockito.mock(Security.class));
    Mockito.when(underlying.get(params)).thenReturn(result);
    assertSame(test.get(params), result);
  }

  public void test_get_Collection_VersionCorrection() {
    final SecuritySource underlying = Mockito.mock(SecuritySource.class);
    final Instant t1 = Instant.ofEpochMilli(1L);
    final Instant t2 = Instant.ofEpochMilli(2L);
    final Instant t3 = Instant.ofEpochMilli(3L);
    final Instant t4 = Instant.ofEpochMilli(4L);
    final SecuritySource test = new VersionLockedSecuritySource(underlying, VersionCorrection.of(t1, t2));
    Collection<ObjectId> ids = Arrays.asList(ObjectId.of("Test", "Foo"), ObjectId.of("Test", "Bar"));
    Map<ObjectId, Security> result = ImmutableMap.<ObjectId, Security>of(ObjectId.of("Test", "Foo"), Mockito.mock(Security.class), ObjectId.of("Test", "Bar"), Mockito.mock(Security.class));
    Mockito.when(underlying.get(ids, VersionCorrection.of(t1, t2))).thenReturn(result);
    assertSame(test.get(ids, VersionCorrection.LATEST), result);
    assertSame(test.get(ids, VersionCorrection.ofVersionAsOf(t1)), result);
    assertSame(test.get(ids, VersionCorrection.ofCorrectedTo(t2)), result);
    result = ImmutableMap.<ObjectId, Security>of(ObjectId.of("Test", "Foo"), Mockito.mock(Security.class), ObjectId.of("Test", "Bar"), Mockito.mock(Security.class));
    Mockito.when(underlying.get(ids, VersionCorrection.of(t3, t2))).thenReturn(result);
    assertSame(test.get(ids, VersionCorrection.ofVersionAsOf(t3)), result);
    result = ImmutableMap.<ObjectId, Security>of(ObjectId.of("Test", "Foo"), Mockito.mock(Security.class), ObjectId.of("Test", "Bar"), Mockito.mock(Security.class));
    Mockito.when(underlying.get(ids, VersionCorrection.of(t1, t3))).thenReturn(result);
    assertSame(test.get(ids, VersionCorrection.ofCorrectedTo(t3)), result);
    result = ImmutableMap.<ObjectId, Security>of(ObjectId.of("Test", "Foo"), Mockito.mock(Security.class), ObjectId.of("Test", "Bar"), Mockito.mock(Security.class));
    Mockito.when(underlying.get(ids, VersionCorrection.of(t3, t4))).thenReturn(result);
    assertSame(test.get(ids, VersionCorrection.of(t3, t4)), result);
  }

  public void test_changeManager() {
    final SecuritySource underlying = Mockito.mock(SecuritySource.class);
    final SecuritySource test = new VersionLockedSecuritySource(underlying, VersionCorrection.LATEST);
    final ChangeManager result = Mockito.mock(ChangeManager.class);
    Mockito.when(underlying.changeManager()).thenReturn(result);
    assertSame(test.changeManager(), result);
  }

}
