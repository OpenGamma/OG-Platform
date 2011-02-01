/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.config.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.opengamma.DataNotFoundException;
import com.opengamma.id.Identifier;
import com.opengamma.id.ObjectIdentifierSupplier;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.master.config.ConfigDocument;
import com.opengamma.master.config.ConfigSearchRequest;
import com.opengamma.master.config.ConfigSearchResult;

/**
 * Test InMemoryConfigMaster.
 */
public class InMemoryConfigTypeMasterTest {

  private static final UniqueIdentifier OTHER_UID = UniqueIdentifier.of("U", "1");
  private static final Identifier VAL1 = Identifier.of ("Test", "sec1");
  private static final Identifier VAL2 = Identifier.of ("Test", "sec2");

  private InMemoryConfigTypeMaster<Identifier> testEmpty;
  private InMemoryConfigTypeMaster<Identifier> testPopulated;
  private ConfigDocument<Identifier> doc1;
  private ConfigDocument<Identifier> doc2;

  @Before
  public void setUp() {
    testEmpty = new InMemoryConfigTypeMaster<Identifier>(new ObjectIdentifierSupplier("Test"));
    testPopulated = new InMemoryConfigTypeMaster<Identifier>(new ObjectIdentifierSupplier("Test"));
    doc1 = new ConfigDocument<Identifier>();
    doc1.setName("ONE");
    doc1.setValue(VAL1);
    doc1 = testPopulated.add(doc1);
    doc2 = new ConfigDocument<Identifier>();
    doc2.setName("TWO");
    doc2.setValue(VAL2);
    doc2 = testPopulated.add(doc2);
  }

  //-------------------------------------------------------------------------
  @Test(expected = IllegalArgumentException.class)
  public void test_constructor_nullSupplier() {
    new InMemoryConfigTypeMaster<Identifier>(null);
  }

  @Test
  public void test_defaultSupplier() {
    InMemoryConfigTypeMaster<Identifier> master = new InMemoryConfigTypeMaster<Identifier>();
    ConfigDocument<Identifier> doc = new ConfigDocument<Identifier>();
    doc.setName("ONE");
    doc.setValue(VAL1);
    ConfigDocument<Identifier> added = master.add(doc);
    assertEquals("MemCfg", added.getUniqueId().getScheme());
  }

  @Test
  public void test_alternateSupplier() {
    InMemoryConfigTypeMaster<Identifier> master = new InMemoryConfigTypeMaster<Identifier>(new ObjectIdentifierSupplier("Hello"));
    ConfigDocument<Identifier> doc = new ConfigDocument<Identifier>();
    doc.setName("ONE");
    doc.setValue(VAL1);
    ConfigDocument<Identifier> added = master.add(doc);
    assertEquals("Hello", added.getUniqueId().getScheme());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_emptyMaster() {
    ConfigSearchRequest request = new ConfigSearchRequest();
    ConfigSearchResult<Identifier> result = testEmpty.search(request);
    assertEquals(0, result.getPaging().getTotalItems());
    assertEquals(0, result.getDocuments().size());
  }

  @Test
  public void test_search_populatedMaster_all() {
    ConfigSearchRequest request = new ConfigSearchRequest();
    ConfigSearchResult<Identifier> result = testPopulated.search(request);
    assertEquals(2, result.getPaging().getTotalItems());
    List<ConfigDocument<Identifier>> docs = result.getDocuments();
    assertEquals(2, docs.size());
    assertEquals(true, docs.contains(doc1));
    assertEquals(true, docs.contains(doc2));
  }

  @Test
  public void test_search_populatedMaster_filterByName() {
    ConfigSearchRequest request = new ConfigSearchRequest();
    request.setName("ONE");
    ConfigSearchResult<Identifier> result = testPopulated.search(request);
    assertEquals(1, result.getPaging().getTotalItems());
    assertEquals(1, result.getDocuments().size());
    assertEquals(true, result.getDocuments().contains(doc1));
  }

  //-------------------------------------------------------------------------
  @Test(expected = DataNotFoundException.class)
  public void test_get_emptyMaster() {
    assertNull(testEmpty.get(OTHER_UID));
  }

  @Test
  public void test_get_populatedMaster() {
    assertSame(doc1, testPopulated.get(doc1.getUniqueId()));
    assertSame(doc2, testPopulated.get(doc2.getUniqueId()));
  }

  //-------------------------------------------------------------------------
  public void test_add_emptyMaster() {
    ConfigDocument<Identifier> doc = new ConfigDocument<Identifier>();
    doc.setValue(VAL1);
    ConfigDocument<Identifier> added = testEmpty.add(doc);
    assertNotNull(added.getVersionFromInstant());
    assertEquals("Test", added.getUniqueId().getScheme());
    assertSame(VAL1, added.getValue());
  }

  //-------------------------------------------------------------------------
  @Test(expected = DataNotFoundException.class)
  public void test_update_emptyMaster() {
    ConfigDocument<Identifier> doc = new ConfigDocument<Identifier>();
    doc.setValue(VAL1);
    doc.setUniqueId(OTHER_UID);
    testEmpty.update(doc);
  }

  public void test_update_populatedMaster() {
    ConfigDocument<Identifier> doc = new ConfigDocument<Identifier>();
    doc.setValue(VAL1);
    doc.setUniqueId(doc1.getUniqueId());
    ConfigDocument<Identifier> updated = testPopulated.update(doc);
    assertEquals(doc1.getUniqueId(), updated.getUniqueId());
    assertNotNull(doc1.getVersionFromInstant());
    assertNotNull(updated.getVersionFromInstant());
    assertEquals(false, doc1.getVersionFromInstant().equals(updated.getVersionFromInstant()));
  }

  //-------------------------------------------------------------------------
  @Test(expected = DataNotFoundException.class)
  public void test_remove_emptyMaster() {
    testEmpty.remove(OTHER_UID);
  }

  @Test
  public void test_remove_populatedMaster() {
    testPopulated.remove(doc1.getUniqueId());
    ConfigSearchRequest request = new ConfigSearchRequest();
    ConfigSearchResult<Identifier> result = testPopulated.search(request);
    assertEquals(1, result.getPaging().getTotalItems());
    List<ConfigDocument<Identifier>> docs = result.getDocuments();
    assertEquals(1, docs.size());
    assertEquals(true, docs.contains(doc2));
  }

}
