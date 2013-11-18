/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.master.impl;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertSame;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.mockito.Mockito;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableMap;
import com.opengamma.core.change.ChangeManager;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.AbstractChangeProvidingMaster;
import com.opengamma.master.AbstractDocument;
import com.opengamma.util.test.TestGroup;

/**
 * Tests the {@link AbstractQuerySplittingMaster} class.
 */
@Test(groups = TestGroup.UNIT)
@SuppressWarnings({"rawtypes", "unchecked" })
public class QuerySplittingMasterTest {

  private AbstractQuerySplittingMaster instance(final AbstractChangeProvidingMaster underlying) {
    return new AbstractQuerySplittingMaster(underlying) {
    };
  }

  public void testGet_uid() {
    final AbstractChangeProvidingMaster mock = Mockito.mock(AbstractChangeProvidingMaster.class);
    final AbstractQuerySplittingMaster instance = instance(mock);
    final AbstractDocument doc = Mockito.mock(AbstractDocument.class);
    Mockito.when(mock.get(UniqueId.of("Foo", "Bar"))).thenReturn(doc);
    assertSame(instance.get(UniqueId.of("Foo", "Bar")), doc);
  }

  public void testGet_oid() {
    final AbstractChangeProvidingMaster mock = Mockito.mock(AbstractChangeProvidingMaster.class);
    final AbstractQuerySplittingMaster instance = instance(mock);
    final AbstractDocument doc = Mockito.mock(AbstractDocument.class);
    Mockito.when(mock.get(ObjectId.of("Foo", "Bar"), VersionCorrection.LATEST)).thenReturn(doc);
    assertSame(instance.get(ObjectId.of("Foo", "Bar"), VersionCorrection.LATEST), doc);
  }

  public void testGet_multiple_uid_disabled() {
    final AbstractChangeProvidingMaster mock = Mockito.mock(AbstractChangeProvidingMaster.class);
    final AbstractQuerySplittingMaster instance = instance(mock);
    final Map<UniqueId, AbstractDocument> docs = Collections.singletonMap(UniqueId.of("Foo", "Bar"), Mockito.mock(AbstractDocument.class));
    Mockito.when(mock.get(Collections.singleton(UniqueId.of("Foo", "Bar")))).thenReturn(docs);
    assertEquals(instance.getMaxGetRequest(), 0);
    assertSame(instance.get(Collections.singleton(UniqueId.of("Foo", "Bar"))), docs);
  }

  public void testGet_multiple_uid_small() {
    final AbstractChangeProvidingMaster mock = Mockito.mock(AbstractChangeProvidingMaster.class);
    final AbstractQuerySplittingMaster instance = instance(mock);
    final Map<UniqueId, AbstractDocument> docs = Collections.singletonMap(UniqueId.of("Foo", "Bar"), Mockito.mock(AbstractDocument.class));
    Mockito.when(mock.get(Collections.singleton(UniqueId.of("Foo", "Bar")))).thenReturn(docs);
    instance.setMaxGetRequest(1);
    assertSame(instance.get(Collections.singleton(UniqueId.of("Foo", "Bar"))), docs);
  }

  public void testGet_multiple_uid_large() {
    final AbstractChangeProvidingMaster mock = Mockito.mock(AbstractChangeProvidingMaster.class);
    final AbstractQuerySplittingMaster instance = instance(mock);
    final Map<UniqueId, AbstractDocument> docs1 = ImmutableMap.of(UniqueId.of("Test", "1"), Mockito.mock(AbstractDocument.class));
    final Map<UniqueId, AbstractDocument> docs2 = ImmutableMap.of(UniqueId.of("Test", "2"), Mockito.mock(AbstractDocument.class), UniqueId.of("Test", "3"), Mockito.mock(AbstractDocument.class));
    final Map<UniqueId, AbstractDocument> docs3 = ImmutableMap.of(UniqueId.of("Test", "4"), Mockito.mock(AbstractDocument.class), UniqueId.of("Test", "5"), Mockito.mock(AbstractDocument.class));
    Mockito.when(mock.get(Arrays.asList(UniqueId.of("Test", "1")))).thenReturn(docs1);
    Mockito.when(mock.get(Arrays.asList(UniqueId.of("Test", "2"), UniqueId.of("Test", "3")))).thenReturn(docs2);
    Mockito.when(mock.get(Arrays.asList(UniqueId.of("Test", "4"), UniqueId.of("Test", "5")))).thenReturn(docs3);
    instance.setMaxGetRequest(2);
    final Map<UniqueId, AbstractDocument> docs = new HashMap<UniqueId, AbstractDocument>();
    docs.putAll(docs1);
    docs.putAll(docs2);
    docs.putAll(docs3);
    assertEquals(instance.get(Arrays.asList(UniqueId.of("Test", "1"), UniqueId.of("Test", "2"), UniqueId.of("Test", "3"), UniqueId.of("Test", "4"), UniqueId.of("Test", "5"))), docs);
  }

  public void testAdd() {
    final AbstractChangeProvidingMaster mock = Mockito.mock(AbstractChangeProvidingMaster.class);
    final AbstractQuerySplittingMaster instance = instance(mock);
    final AbstractDocument doc1 = Mockito.mock(AbstractDocument.class);
    final AbstractDocument doc2 = Mockito.mock(AbstractDocument.class);
    Mockito.when(mock.add(doc1)).thenReturn(doc2);
    assertSame(instance.add(doc1), doc2);
  }

  public void testUpdate() {
    final AbstractChangeProvidingMaster mock = Mockito.mock(AbstractChangeProvidingMaster.class);
    final AbstractQuerySplittingMaster instance = instance(mock);
    final AbstractDocument doc1 = Mockito.mock(AbstractDocument.class);
    final AbstractDocument doc2 = Mockito.mock(AbstractDocument.class);
    Mockito.when(mock.update(doc1)).thenReturn(doc2);
    assertSame(instance.update(doc1), doc2);
  }

  public void testRemove() {
    final AbstractChangeProvidingMaster mock = Mockito.mock(AbstractChangeProvidingMaster.class);
    final AbstractQuerySplittingMaster instance = instance(mock);
    instance.remove(ObjectId.of("Foo", "Bar"));
    Mockito.verify(mock, Mockito.only()).remove(ObjectId.of("Foo", "Bar"));
  }

  public void testCorrect() {
    final AbstractChangeProvidingMaster mock = Mockito.mock(AbstractChangeProvidingMaster.class);
    final AbstractQuerySplittingMaster instance = instance(mock);
    final AbstractDocument doc1 = Mockito.mock(AbstractDocument.class);
    final AbstractDocument doc2 = Mockito.mock(AbstractDocument.class);
    Mockito.when(mock.correct(doc1)).thenReturn(doc2);
    assertSame(instance.correct(doc1), doc2);
  }

  public void testReplaceVersion_uid() {
    final AbstractChangeProvidingMaster mock = Mockito.mock(AbstractChangeProvidingMaster.class);
    final AbstractQuerySplittingMaster instance = instance(mock);
    Mockito.when(mock.replaceVersion(UniqueId.of("Foo", "Bar"), Collections.emptyList())).thenReturn(Collections.emptyList());
    assertEquals(instance.replaceVersion(UniqueId.of("Foo", "Bar"), Collections.emptyList()), Collections.emptyList());
  }

  public void testReplaceAllVersions() {
    final AbstractChangeProvidingMaster mock = Mockito.mock(AbstractChangeProvidingMaster.class);
    final AbstractQuerySplittingMaster instance = instance(mock);
    Mockito.when(mock.replaceAllVersions(ObjectId.of("Foo", "Bar"), Collections.emptyList())).thenReturn(Collections.emptyList());
    assertEquals(instance.replaceAllVersions(ObjectId.of("Foo", "Bar"), Collections.emptyList()), Collections.emptyList());
  }

  public void testReplaceVersions() {
    final AbstractChangeProvidingMaster mock = Mockito.mock(AbstractChangeProvidingMaster.class);
    final AbstractQuerySplittingMaster instance = instance(mock);
    Mockito.when(mock.replaceVersions(ObjectId.of("Foo", "Bar"), Collections.emptyList())).thenReturn(Collections.emptyList());
    assertEquals(instance.replaceVersions(ObjectId.of("Foo", "Bar"), Collections.emptyList()), Collections.emptyList());
  }

  public void testReplaceVersion_doc() {
    final AbstractChangeProvidingMaster mock = Mockito.mock(AbstractChangeProvidingMaster.class);
    final AbstractQuerySplittingMaster instance = instance(mock);
    final AbstractDocument doc = Mockito.mock(AbstractDocument.class);
    Mockito.when(mock.replaceVersion(doc)).thenReturn(UniqueId.of("Foo", "Bar"));
    assertEquals(instance.replaceVersion(doc), UniqueId.of("Foo", "Bar"));
  }

  public void testRemoveVersion() {
    final AbstractChangeProvidingMaster mock = Mockito.mock(AbstractChangeProvidingMaster.class);
    final AbstractQuerySplittingMaster instance = instance(mock);
    instance.removeVersion(UniqueId.of("Foo", "Bar"));
    Mockito.verify(mock, Mockito.only()).removeVersion(UniqueId.of("Foo", "Bar"));
  }

  public void testAddVersion() {
    final AbstractChangeProvidingMaster mock = Mockito.mock(AbstractChangeProvidingMaster.class);
    final AbstractQuerySplittingMaster instance = instance(mock);
    final AbstractDocument doc = Mockito.mock(AbstractDocument.class);
    Mockito.when(mock.addVersion(ObjectId.of("Foo", "Bar"), doc)).thenReturn(UniqueId.of("Foo", "Bar", "1"));
    assertEquals(instance.addVersion(ObjectId.of("Foo", "Bar"), doc), UniqueId.of("Foo", "Bar", "1"));
  }

  public void testChangeManager() {
    final AbstractChangeProvidingMaster mock = Mockito.mock(AbstractChangeProvidingMaster.class);
    final AbstractQuerySplittingMaster instance = instance(mock);
    final ChangeManager cm = Mockito.mock(ChangeManager.class);
    Mockito.when(mock.changeManager()).thenReturn(cm);
    assertSame(instance.changeManager(), cm);
  }

}
