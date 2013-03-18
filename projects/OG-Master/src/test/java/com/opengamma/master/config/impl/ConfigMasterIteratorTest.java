/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.config.impl;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.config.impl.ConfigItem;
import com.opengamma.id.ExternalId;
import com.opengamma.master.config.ConfigDocument;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.master.config.ConfigSearchRequest;
import com.opengamma.master.config.ConfigSearchSortOrder;
import com.opengamma.util.test.TestGroup;

/**
 * Test {@link ConfigSearchIterator}.
 */
@Test(groups = TestGroup.UNIT)
public class ConfigMasterIteratorTest {

  private ConfigDocument _doc1;
  private ConfigDocument _doc2;
  private ConfigDocument _doc3;
  private ConfigDocument _doc4;

  private ConfigMaster _configMaster;

  @BeforeMethod
  public void setUp() throws Exception {
    ConfigItem<ExternalId> item1 = ConfigItem.of(ExternalId.of("A", "B"), "Test1");
    ConfigItem<ExternalId> item2 = ConfigItem.of(ExternalId.of("C", "D"), "Test2");
    ConfigItem<ExternalId> item3 = ConfigItem.of(ExternalId.of("E", "F"), "Test3");
    ConfigItem<ExternalId> item4 = ConfigItem.of(ExternalId.of("E", "F"), "Test3");
    
    InMemoryConfigMaster configMaster = new InMemoryConfigMaster();
    _doc1 = configMaster.add(new ConfigDocument(item1));
    _doc2 = configMaster.add(new ConfigDocument(item2));
    _doc3 = configMaster.add(new ConfigDocument(item3));
    _doc4 = configMaster.add(new ConfigDocument(item4));
    _configMaster = configMaster;
  }

  @AfterMethod
  public void tearDown() throws Exception {
    _configMaster = null;
  }

  //-------------------------------------------------------------------------
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_constructor_2arg_nullMaster() throws Exception {
    new ConfigSearchIterator<ExternalId>(null, new ConfigSearchRequest<ExternalId>());
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_constructor_2arg_nullRequest() throws Exception {
    new ConfigSearchIterator<ExternalId>(_configMaster, null);
  }

  public void iterate() throws Exception {
    ConfigSearchRequest<ExternalId> request = new ConfigSearchRequest<ExternalId>();
    request.setType(ExternalId.class);
    request.setSortOrder(ConfigSearchSortOrder.NAME_ASC);
    
    ConfigSearchIterator<ExternalId> iterator = new ConfigSearchIterator<ExternalId>(_configMaster, request);
    assertEquals(true, iterator.hasNext());
    assertEquals(0, iterator.nextIndex());
    assertEquals(_doc1, iterator.next());
    assertEquals(1, iterator.nextIndex());
    
    assertEquals(true, iterator.hasNext());
    assertEquals(1, iterator.nextIndex());
    assertEquals(_doc2, iterator.next());
    assertEquals(2, iterator.nextIndex());
    
    assertEquals(true, iterator.hasNext());
    assertEquals(2, iterator.nextIndex());
    assertEquals(_doc3, iterator.next());
    assertEquals(3, iterator.nextIndex());
    
    assertEquals(true, iterator.hasNext());
    assertEquals(3, iterator.nextIndex());
    assertEquals(_doc4, iterator.next());
    assertEquals(4, iterator.nextIndex());
    
    assertEquals(false, iterator.hasNext());
    assertEquals(4, iterator.nextIndex());
  }

  @SuppressWarnings("unchecked")
  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void iterateError() throws Exception {
    ConfigSearchRequest<ExternalId> request = new ConfigSearchRequest<ExternalId>();
    request.setType(ExternalId.class);
    request.setSortOrder(ConfigSearchSortOrder.NAME_ASC);
    
    ConfigMaster mockMaster = mock(ConfigMaster.class);
    when(mockMaster.search(any(ConfigSearchRequest.class))).thenThrow(new IllegalStateException());
    
    ConfigSearchIterator<ExternalId> iterator = new ConfigSearchIterator<ExternalId>(mockMaster, request);
    iterator.hasNext();
  }

}
