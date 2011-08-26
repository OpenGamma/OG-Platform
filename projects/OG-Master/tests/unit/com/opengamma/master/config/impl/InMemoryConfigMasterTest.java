/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.config.impl;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertSame;
import static org.testng.AssertJUnit.assertTrue;

import java.util.List;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.common.base.Supplier;
import com.opengamma.DataNotFoundException;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ObjectId;
import com.opengamma.id.ObjectIdSupplier;
import com.opengamma.id.UniqueId;
import com.opengamma.master.config.ConfigDocument;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.master.config.ConfigMetaDataRequest;
import com.opengamma.master.config.ConfigMetaDataResult;
import com.opengamma.master.config.ConfigSearchRequest;
import com.opengamma.master.config.ConfigSearchResult;

/**
 * Test {@link InMemoryConfigMaster}.
 */
@Test
public class InMemoryConfigMasterTest {

  private static final UniqueId OTHER_UID = UniqueId.of("U", "1");
  private static final ExternalId VAL1 = ExternalId.of ("Test", "sec1");
  private static final ExternalId VAL2 = ExternalId.of ("Test", "sec2");
  private static final ExternalIdBundle VAL3 = ExternalIdBundle.of(VAL1);
  private static final ExternalIdBundle VAL4 = ExternalIdBundle.of(VAL2);

  private ConfigMaster _testEmpty;
  private ConfigMaster _testPopulated;
  private ConfigDocument<ExternalId> _doc1;
  private ConfigDocument<ExternalId> _doc2;
  private ConfigDocument<ExternalIdBundle> _doc3;
  private ConfigDocument<ExternalIdBundle> _doc4;

  @BeforeMethod
  public void setUp() {
    _testEmpty = new InMemoryConfigMaster(new ObjectIdSupplier("Test"));
    _testPopulated = new InMemoryConfigMaster(new ObjectIdSupplier("Test"));
    _doc1 = new ConfigDocument<ExternalId>(ExternalId.class);
    _doc1.setName("ONE");
    _doc1.setValue(VAL1);
    _doc1 = _testPopulated.add(_doc1);
    _doc2 = new ConfigDocument<ExternalId>(ExternalId.class);
    _doc2.setName("TWO");
    _doc2.setValue(VAL2);
    _doc2 = _testPopulated.add(_doc2);
    _doc3 = new ConfigDocument<ExternalIdBundle>(ExternalIdBundle.class);
    _doc3.setName("THREE");
    _doc3.setValue(VAL3);
    _doc3 = _testPopulated.add(_doc3);
    _doc4 = new ConfigDocument<ExternalIdBundle>(ExternalIdBundle.class);
    _doc4.setName("FOUR");
    _doc4.setValue(VAL4);
    _doc4 = _testPopulated.add(_doc4);
  }

  //-------------------------------------------------------------------------
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_constructor_nullSupplier() {
    new InMemoryConfigMaster((Supplier<ObjectId>) null);
  }

  public void test_defaultSupplier() {
    InMemoryConfigMaster master = new InMemoryConfigMaster();
    ConfigDocument<ExternalId> doc = new ConfigDocument<ExternalId>(ExternalId.class);
    doc.setName("ONE");
    doc.setValue(VAL1);
    ConfigDocument<ExternalId> added = master.add(doc);
    assertEquals("MemCfg", added.getUniqueId().getScheme());
  }

  public void test_alternateSupplier() {
    InMemoryConfigMaster master = new InMemoryConfigMaster(new ObjectIdSupplier("Hello"));
    ConfigDocument<ExternalId> doc = new ConfigDocument<ExternalId>(ExternalId.class);
    doc.setName("ONE");
    doc.setValue(VAL1);
    ConfigDocument<ExternalId> added = master.add(doc);
    assertEquals("Hello", added.getUniqueId().getScheme());
  }

  //-------------------------------------------------------------------------
  public void test_search_oneId_noMatch() {
    ConfigSearchRequest<ExternalId> request = new ConfigSearchRequest<ExternalId>();
    request.addConfigId(ObjectId.of("A", "UNREAL"));
    ConfigSearchResult<ExternalId> result = _testPopulated.search(request);
    assertEquals(0, result.getDocuments().size());
  }

  public void test_search_oneId() {
    ConfigSearchRequest<ExternalId> request = new ConfigSearchRequest<ExternalId>();
    request.addConfigId(_doc2.getObjectId());
    ConfigSearchResult<ExternalId> result = _testPopulated.search(request);
    assertEquals(1, result.getDocuments().size());
    assertEquals(_doc2, result.getFirstDocument());
  }

  //-------------------------------------------------------------------------
  public void test_metaData() {
    ConfigMetaDataResult test = _testPopulated.metaData(new ConfigMetaDataRequest());
    assertNotNull(test);
    assertEquals(2, test.getConfigTypes().size());
    assertTrue(test.getConfigTypes().contains(ExternalId.class));
    assertTrue(test.getConfigTypes().contains(ExternalIdBundle.class));
  }

  public void test_metaData_noTypes() {
    ConfigMetaDataRequest request = new ConfigMetaDataRequest();
    request.setConfigTypes(false);
    ConfigMetaDataResult test = _testPopulated.metaData(request);
    assertNotNull(test);
    assertEquals(0, test.getConfigTypes().size());
  }

  //-------------------------------------------------------------------------
  public void test_search_emptyMaster() {
    ConfigSearchRequest<Object> request = new ConfigSearchRequest<Object>();
    ConfigSearchResult<Object> result = _testEmpty.search(request);
    assertEquals(0, result.getPaging().getTotalItems());
    assertEquals(0, result.getDocuments().size());
  }

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

  public void test_search_populatedMaster_filterByName() {
    ConfigSearchRequest<Object> request = new ConfigSearchRequest<Object>();
    request.setName("ONE");
    ConfigSearchResult<Object> result = _testPopulated.search(request);
    assertEquals(1, result.getPaging().getTotalItems());
    assertEquals(1, result.getDocuments().size());
    assertEquals(true, result.getDocuments().contains(_doc1));
  }
  
  public void test_search_populatedMaster_filterByType() {
    ConfigSearchRequest<ExternalId> request = new ConfigSearchRequest<ExternalId>();
    request.setType(ExternalId.class);
    ConfigSearchResult<ExternalId> result = _testPopulated.search(request);
    assertEquals(2, result.getPaging().getTotalItems());
    assertEquals(2, result.getDocuments().size());
    assertEquals(true, result.getDocuments().contains(_doc1));
    assertEquals(true, result.getDocuments().contains(_doc2));
  }
  
  //-------------------------------------------------------------------------
  @Test(expectedExceptions = DataNotFoundException.class)
  public void test_get_emptyMaster() {
    assertNull(_testEmpty.get(OTHER_UID, ExternalId.class));
  }

  public void test_get_populatedMaster() {
    assertSame(_doc1, _testPopulated.get(_doc1.getUniqueId()));
    assertSame(_doc2, _testPopulated.get(_doc2.getUniqueId()));
    assertSame(_doc3, _testPopulated.get(_doc3.getUniqueId()));
    assertSame(_doc4, _testPopulated.get(_doc4.getUniqueId()));
  }
  
  public void test_get_typed_populatedMaster() {
    ConfigDocument<ExternalId> storedDoc1 = _testPopulated.get(_doc1.getUniqueId(), ExternalId.class);
    assertSame(_doc1, storedDoc1);
    ConfigDocument<ExternalId> storedDoc2 = _testPopulated.get(_doc2.getUniqueId(), ExternalId.class);
    assertSame(_doc2, storedDoc2);
    
    ConfigDocument<ExternalIdBundle> storedDoc3 = _testPopulated.get(_doc3.getUniqueId(), ExternalIdBundle.class);
    assertSame(_doc3, storedDoc3);
    ConfigDocument<ExternalIdBundle> storedDoc4 = _testPopulated.get(_doc4.getUniqueId(), ExternalIdBundle.class);
    assertSame(_doc4, storedDoc4);
  }
  
  @Test(expectedExceptions = DataNotFoundException.class)
  public void test_get_invalid_typed_populatedMaster() {
    ConfigDocument<ExternalIdBundle> storedDoc1 = _testPopulated.get(_doc1.getUniqueId(), ExternalIdBundle.class);
    assertSame(_doc1, storedDoc1);
  }

  //-------------------------------------------------------------------------
  public void test_add_emptyMaster() {
    ConfigDocument<ExternalId> doc = new ConfigDocument<ExternalId>(ExternalId.class);
    doc.setName("Test");
    doc.setValue(VAL1);
    ConfigDocument<ExternalId> added = _testEmpty.add(doc);
    assertNotNull(added.getVersionFromInstant());
    assertEquals("Test", added.getUniqueId().getScheme());
    assertSame(VAL1, added.getValue());
  }

  //-------------------------------------------------------------------------
  @Test(expectedExceptions = DataNotFoundException.class)
  public void test_update_emptyMaster() {
    ConfigDocument<ExternalId> doc = new ConfigDocument<ExternalId>(ExternalId.class);
    doc.setValue(VAL1);
    doc.setUniqueId(OTHER_UID);
    _testEmpty.update(doc);
  }

  public void test_update_populatedMaster() {
    ConfigDocument<ExternalId> doc = new ConfigDocument<ExternalId>(ExternalId.class);
    doc.setValue(VAL1);
    doc.setUniqueId(_doc1.getUniqueId());
    ConfigDocument<ExternalId> updated = _testPopulated.update(doc);
    assertEquals(_doc1.getUniqueId(), updated.getUniqueId());
    assertNotNull(_doc1.getVersionFromInstant());
    assertNotNull(updated.getVersionFromInstant());
  }

  //-------------------------------------------------------------------------
  @Test(expectedExceptions = DataNotFoundException.class)
  public void test_remove_emptyMaster() {
    _testEmpty.remove(OTHER_UID);
  }

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
