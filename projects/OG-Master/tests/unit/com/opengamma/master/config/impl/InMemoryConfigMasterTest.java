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

import com.google.common.base.Supplier;
import com.opengamma.DataNotFoundException;
import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.ObjectIdentifier;
import com.opengamma.id.ObjectIdentifierSupplier;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.master.config.ConfigDocument;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.master.config.ConfigSearchRequest;
import com.opengamma.master.config.ConfigSearchResult;

/**
 * Test InMemoryConfigMaster.
 */
public class InMemoryConfigMasterTest {

  private static final UniqueIdentifier OTHER_UID = UniqueIdentifier.of("U", "1");
  private static final Identifier VAL1 = Identifier.of ("Test", "sec1");
  private static final Identifier VAL2 = Identifier.of ("Test", "sec2");
  private static final IdentifierBundle VAL3 = IdentifierBundle.of(VAL1);
  private static final IdentifierBundle VAL4 = IdentifierBundle.of(VAL2);

  private ConfigMaster _testEmpty;
  private ConfigMaster _testPopulated;
  private ConfigDocument<Identifier> _doc1;
  private ConfigDocument<Identifier> _doc2;
  private ConfigDocument<IdentifierBundle> _doc3;
  private ConfigDocument<IdentifierBundle> _doc4;

  @Before
  public void setUp() {
    _testEmpty = new InMemoryConfigMaster(new ObjectIdentifierSupplier("Test"));
    _testPopulated = new InMemoryConfigMaster(new ObjectIdentifierSupplier("Test"));
    _doc1 = new ConfigDocument<Identifier>();
    _doc1.setName("ONE");
    _doc1.setValue(VAL1);
    _doc1 = _testPopulated.add(_doc1);
    _doc2 = new ConfigDocument<Identifier>();
    _doc2.setName("TWO");
    _doc2.setValue(VAL2);
    _doc2 = _testPopulated.add(_doc2);
    _doc3 = new ConfigDocument<IdentifierBundle>();
    _doc3.setName("THREE");
    _doc3.setValue(VAL3);
    _doc3 = _testPopulated.add(_doc3);
    _doc4 = new ConfigDocument<IdentifierBundle>();
    _doc4.setName("FOUR");
    _doc4.setValue(VAL4);
    _doc4 = _testPopulated.add(_doc4);
  }

  //-------------------------------------------------------------------------
  @Test(expected = IllegalArgumentException.class)
  public void test_constructor_nullSupplier() {
    new InMemoryConfigMaster((Supplier<ObjectIdentifier>) null);
  }

  @Test
  public void test_defaultSupplier() {
    InMemoryConfigMaster master = new InMemoryConfigMaster();
    ConfigDocument<Identifier> doc = new ConfigDocument<Identifier>();
    doc.setName("ONE");
    doc.setValue(VAL1);
    ConfigDocument<Identifier> added = master.add(doc);
    assertEquals("MemCfg", added.getUniqueId().getScheme());
  }

  @Test
  public void test_alternateSupplier() {
    InMemoryConfigMaster master = new InMemoryConfigMaster(new ObjectIdentifierSupplier("Hello"));
    ConfigDocument<Identifier> doc = new ConfigDocument<Identifier>();
    doc.setName("ONE");
    doc.setValue(VAL1);
    ConfigDocument<Identifier> added = master.add(doc);
    assertEquals("Hello", added.getUniqueId().getScheme());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_emptyMaster() {
    ConfigSearchRequest<Object> request = new ConfigSearchRequest<Object>();
    ConfigSearchResult<Object> result = _testEmpty.search(request);
    assertEquals(0, result.getPaging().getTotalItems());
    assertEquals(0, result.getDocuments().size());
  }

  @Test
  public void test_search_populatedMaster_all() {
    ConfigSearchRequest<Object> request = new ConfigSearchRequest<Object>();
    ConfigSearchResult<Object> result = _testPopulated.search(request);
    assertEquals(4, result.getPaging().getTotalItems());
    List<ConfigDocument<Object>> docs = result.getDocuments();
    assertEquals(4, docs.size());
    assertEquals(true, docs.contains(_doc1));
    assertEquals(true, docs.contains(_doc2));
    assertEquals(true, docs.contains(_doc3));
    assertEquals(true, docs.contains(_doc4));
  }

  @Test
  public void test_search_populatedMaster_filterByName() {
    ConfigSearchRequest<Object> request = new ConfigSearchRequest<Object>();
    request.setName("ONE");
    ConfigSearchResult<Object> result = _testPopulated.search(request);
    assertEquals(1, result.getPaging().getTotalItems());
    assertEquals(1, result.getDocuments().size());
    assertEquals(true, result.getDocuments().contains(_doc1));
  }
  
  @Test
  public void test_search_populatedMaster_filterByType() {
    ConfigSearchRequest<Identifier> request = new ConfigSearchRequest<Identifier>();
    request.setType(Identifier.class);
    ConfigSearchResult<Identifier> result = _testPopulated.search(request);
    assertEquals(2, result.getPaging().getTotalItems());
    assertEquals(2, result.getDocuments().size());
    assertEquals(true, result.getDocuments().contains(_doc1));
    assertEquals(true, result.getDocuments().contains(_doc2));
  }
  
  //-------------------------------------------------------------------------
  @Test(expected = DataNotFoundException.class)
  public void test_get_emptyMaster() {
    assertNull(_testEmpty.get(OTHER_UID, Identifier.class));
  }

  @Test
  public void test_get_populatedMaster() {
    assertSame(_doc1, _testPopulated.get(_doc1.getUniqueId()));
    assertSame(_doc2, _testPopulated.get(_doc2.getUniqueId()));
    assertSame(_doc3, _testPopulated.get(_doc3.getUniqueId()));
    assertSame(_doc4, _testPopulated.get(_doc4.getUniqueId()));
  }
  
  @Test
  public void test_get_typed_populatedMaster() {
    ConfigDocument<Identifier> storedDoc1 = _testPopulated.get(_doc1.getUniqueId(), Identifier.class);
    assertSame(_doc1, storedDoc1);
    ConfigDocument<Identifier> storedDoc2 = _testPopulated.get(_doc2.getUniqueId(), Identifier.class);
    assertSame(_doc2, storedDoc2);
    
    ConfigDocument<IdentifierBundle> storedDoc3 = _testPopulated.get(_doc3.getUniqueId(), IdentifierBundle.class);
    assertSame(_doc3, storedDoc3);
    ConfigDocument<IdentifierBundle> storedDoc4 = _testPopulated.get(_doc4.getUniqueId(), IdentifierBundle.class);
    assertSame(_doc4, storedDoc4);
  }
  
  @Test(expected = DataNotFoundException.class)
  public void test_get_invalid_typed_populatedMaster() {
    ConfigDocument<IdentifierBundle> storedDoc1 = _testPopulated.get(_doc1.getUniqueId(), IdentifierBundle.class);
    assertSame(_doc1, storedDoc1);
  }

  //-------------------------------------------------------------------------
  public void test_add_emptyMaster() {
    ConfigDocument<Identifier> doc = new ConfigDocument<Identifier>();
    doc.setValue(VAL1);
    ConfigDocument<Identifier> added = _testEmpty.add(doc);
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
    _testEmpty.update(doc);
  }

  public void test_update_populatedMaster() {
    ConfigDocument<Identifier> doc = new ConfigDocument<Identifier>();
    doc.setValue(VAL1);
    doc.setUniqueId(_doc1.getUniqueId());
    ConfigDocument<Identifier> updated = _testPopulated.update(doc);
    assertEquals(_doc1.getUniqueId(), updated.getUniqueId());
    assertNotNull(_doc1.getVersionFromInstant());
    assertNotNull(updated.getVersionFromInstant());
    assertEquals(false, _doc1.getVersionFromInstant().equals(updated.getVersionFromInstant()));
  }

  //-------------------------------------------------------------------------
  @Test(expected = DataNotFoundException.class)
  public void test_remove_emptyMaster() {
    _testEmpty.remove(OTHER_UID);
  }

  @Test
  public void test_remove_populatedMaster() {
    _testPopulated.remove(_doc1.getUniqueId());
    ConfigSearchRequest<Object> request = new ConfigSearchRequest<Object>();
    ConfigSearchResult<Object> result = _testPopulated.search(request);
    assertEquals(3, result.getPaging().getTotalItems());
    List<ConfigDocument<Object>> docs = result.getDocuments();
    assertEquals(3, docs.size());
    assertEquals(true, docs.contains(_doc2));
    assertEquals(true, docs.contains(_doc3));
    assertEquals(true, docs.contains(_doc4));
  }

}
