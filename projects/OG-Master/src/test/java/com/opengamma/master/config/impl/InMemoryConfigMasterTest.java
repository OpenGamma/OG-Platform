/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.config.impl;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertSame;
import static org.testng.AssertJUnit.assertTrue;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.common.base.Supplier;
import com.opengamma.DataNotFoundException;
import com.opengamma.core.config.impl.ConfigItem;
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
import com.opengamma.util.test.TestGroup;

/**
 * Test {@link InMemoryConfigMaster}.
 */
@Test(groups = TestGroup.UNIT)
public class InMemoryConfigMasterTest {

  private static final UniqueId OTHER_UID = UniqueId.of("U", "1");
  private static final ExternalId VAL1 = ExternalId.of("Test", "sec1");
  private static final ExternalId VAL2 = ExternalId.of("Test", "sec2");
  private static final ExternalIdBundle VAL3 = ExternalIdBundle.of(VAL1);
  private static final ExternalIdBundle VAL4 = ExternalIdBundle.of(VAL2);

  private ConfigMaster _testEmpty;
  private ConfigMaster _testPopulated;
  private ConfigItem<ExternalId> _item1;
  private ConfigItem<ExternalId> _item2;
  private ConfigItem<ExternalIdBundle> _item3;
  private ConfigItem<ExternalIdBundle> _item4;

  @SuppressWarnings("unchecked")
  @BeforeMethod
  public void setUp() {
    _testEmpty = new InMemoryConfigMaster(new ObjectIdSupplier("Test"));
    _testPopulated = new InMemoryConfigMaster(new ObjectIdSupplier("Test"));
    _item1 = ConfigItem.of(VAL1);
    _item1.setName("ONE");
    _item1 = (ConfigItem<ExternalId>) _testPopulated.add(new ConfigDocument(_item1)).getConfig();
    _item2 = ConfigItem.of(VAL2);
    _item2.setName("TWO");
    _item2 = (ConfigItem<ExternalId>) _testPopulated.add(new ConfigDocument(_item2)).getConfig();
    _item3 = ConfigItem.of(VAL3);
    _item3.setName("THREE");
    _item3 = (ConfigItem<ExternalIdBundle>) _testPopulated.add(new ConfigDocument(_item3)).getConfig();
    _item4 = ConfigItem.of(VAL4);
    _item4.setName("FOUR");
    _item4 = (ConfigItem<ExternalIdBundle>) _testPopulated.add(new ConfigDocument(_item4)).getConfig();
  }

  //-------------------------------------------------------------------------
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_constructor_nullSupplier() {
    new InMemoryConfigMaster((Supplier<ObjectId>) null);
  }

  @SuppressWarnings("unchecked")
  public void test_defaultSupplier() {
    InMemoryConfigMaster master = new InMemoryConfigMaster();
    ConfigItem<ExternalId> item = ConfigItem.of(VAL1);
    item.setName("ONE");
    ConfigItem<ExternalId> added = (ConfigItem<ExternalId>) master.add(new ConfigDocument(item)).getConfig();
    assertEquals("MemCfg", added.getUniqueId().getScheme());
  }

  @SuppressWarnings("unchecked")
  public void test_alternateSupplier() {
    InMemoryConfigMaster master = new InMemoryConfigMaster(new ObjectIdSupplier("Hello"));
    ConfigItem<ExternalId> item = ConfigItem.of(VAL1);
    item.setName("ONE");
    ConfigItem<ExternalId> added = (ConfigItem<ExternalId>) master.add(new ConfigDocument(item)).getConfig();
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
    request.addConfigId(_item2.getObjectId());
    ConfigSearchResult<ExternalId> result = _testPopulated.search(request);
    assertEquals(1, result.getDocuments().size());
    assertEquals(_item2, result.getFirstDocument().getConfig());
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
    List<ConfigDocument> docs = result.getDocuments();
    Set<ConfigItem<?>> items = new HashSet<ConfigItem<?>>();
    for (ConfigDocument doc : docs) {
      items.add(doc.getConfig());
    }
    assertEquals(4, items.size());
    assertEquals(true, items.contains(_item1));
    assertEquals(true, items.contains(_item2));
    assertEquals(true, items.contains(_item3));
    assertEquals(true, items.contains(_item4));
  }

  public void test_search_populatedMaster_filterByName() {
    ConfigSearchRequest<Object> request = new ConfigSearchRequest<Object>();
    request.setName("ONE");
    ConfigSearchResult<Object> result = _testPopulated.search(request);
    assertEquals(1, result.getPaging().getTotalItems());
    assertEquals(1, result.getDocuments().size());
    assertEquals(true, result.getValues().contains(_item1));
  }

  public void test_search_populatedMaster_filterByType() {
    ConfigSearchRequest<ExternalId> request = new ConfigSearchRequest<ExternalId>();
    request.setType(ExternalId.class);
    ConfigSearchResult<ExternalId> result = _testPopulated.search(request);
    assertEquals(2, result.getPaging().getTotalItems());
    assertEquals(2, result.getDocuments().size());
    assertEquals(true, result.getValues().contains(_item1));
    assertEquals(true, result.getValues().contains(_item2));
  }

  //-------------------------------------------------------------------------
  @Test(expectedExceptions = DataNotFoundException.class)
  public void test_get_emptyMaster() {
    assertNull(_testEmpty.get(OTHER_UID));
  }

  public void test_get_populatedMaster() {
    assertSame(_item1, _testPopulated.get(_item1.getUniqueId()).getConfig());
    assertSame(_item2, _testPopulated.get(_item2.getUniqueId()).getConfig());
    assertSame(_item3, _testPopulated.get(_item3.getUniqueId()).getConfig());
    assertSame(_item4, _testPopulated.get(_item4.getUniqueId()).getConfig());
  }

  public void test_get_typed_populatedMaster() {
    ConfigItem<?> storedDoc1 = _testPopulated.get(_item1.getUniqueId()).getConfig();
    assertSame(_item1, storedDoc1);
    ConfigItem<?> storedDoc2 = _testPopulated.get(_item2.getUniqueId()).getConfig();
    assertSame(_item2, storedDoc2);

    ConfigItem<?> storedDoc3 = _testPopulated.get(_item3.getUniqueId()).getConfig();
    assertSame(_item3, storedDoc3);
    ConfigItem<?> storedDoc4 = _testPopulated.get(_item4.getUniqueId()).getConfig();
    assertSame(_item4, storedDoc4);
  }

  public void test_get_invalid_typed_populatedMaster() {
    ConfigItem<?> storedDoc1 = _testPopulated.get(_item1.getUniqueId()).getConfig();
    assertSame(_item1, storedDoc1);
  }

  //-------------------------------------------------------------------------
  public void test_add_emptyMaster() {
    ConfigItem<ExternalId> item = ConfigItem.of(VAL1);
    item.setName("Test");
    ConfigDocument doc = _testEmpty.add(new ConfigDocument(item));
    assertNotNull(doc.getVersionFromInstant());
    assertEquals("Test", doc.getUniqueId().getScheme());
    assertSame(VAL1, doc.getConfig().getValue());
  }

  //-------------------------------------------------------------------------
  @Test(expectedExceptions = DataNotFoundException.class)
  public void test_update_emptyMaster() {
    ConfigItem<ExternalId> item = ConfigItem.of(VAL1);
    item.setUniqueId(OTHER_UID);
    _testEmpty.update(new ConfigDocument(item));
  }

  public void test_update_populatedMaster() {
    ConfigItem<ExternalId> item = ConfigItem.of(VAL1);
    item.setUniqueId(_item1.getUniqueId());
    ConfigDocument updated = _testPopulated.update(new ConfigDocument(item));
    assertTrue(_item1.getUniqueId().getScheme().equals(updated.getUniqueId().getScheme()));
    assertTrue(_item1.getUniqueId().getValue().equals(updated.getUniqueId().getValue()));
    assertFalse(_item1.getUniqueId().getVersion().equals(updated.getUniqueId().getVersion()));
    assertNotNull(updated.getVersionFromInstant());
    assertNotNull(updated.getVersionFromInstant());
  }

  //-------------------------------------------------------------------------
  @Test(expectedExceptions = DataNotFoundException.class)
  public void test_remove_emptyMaster() {
    _testEmpty.remove(OTHER_UID);
  }

  public void test_remove_populatedMaster() {
    _testPopulated.remove(_item1.getUniqueId());
    ConfigSearchRequest<Object> request = new ConfigSearchRequest<Object>();
    ConfigSearchResult<Object> result = _testPopulated.search(request);
    assertEquals(3, result.getPaging().getTotalItems());
    List<ConfigDocument> docs = result.getDocuments();
    Set<ConfigItem<?>> items = new HashSet<ConfigItem<?>>();
    for (ConfigDocument doc : docs) {
      items.add(doc.getConfig());
    }
    assertEquals(3, items.size());
    assertEquals(true, items.contains(_item2));
    assertEquals(true, items.contains(_item3));
    assertEquals(true, items.contains(_item4));
  }

}
