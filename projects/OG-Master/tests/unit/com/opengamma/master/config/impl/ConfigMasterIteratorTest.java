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

/**
 * Test {@link ConfigMasterIterator}.
 */
@Test
public class ConfigMasterIteratorTest {

  private ConfigItem<ExternalId> _item1;
  private ConfigItem<ExternalId> _item2;
  private ConfigItem<ExternalId> _item3;
  private ConfigItem<ExternalId> _item4;

  private ConfigMaster _configMaster;

  @SuppressWarnings("unchecked")
  @BeforeMethod
  public void setUp() throws Exception {
    _item1 = ConfigItem.of(ExternalId.of("A", "B"), "Test1");
    _item2 = ConfigItem.of(ExternalId.of("C", "D"), "Test2");
    _item3 = ConfigItem.of(ExternalId.of("E", "F"), "Test3");
    _item4 = ConfigItem.of(ExternalId.of("E", "F"), "Test3");
    
    InMemoryConfigMaster configMaster = new InMemoryConfigMaster();
    _item1 = (ConfigItem<ExternalId>) configMaster.add(new ConfigDocument(_item1)).getObject();
    _item2 = (ConfigItem<ExternalId>) configMaster.add(new ConfigDocument(_item2)).getObject();
    _item3 = (ConfigItem<ExternalId>) configMaster.add(new ConfigDocument(_item3)).getObject();
    _item4 = (ConfigItem<ExternalId>) configMaster.add(new ConfigDocument(_item4)).getObject();
    _configMaster = configMaster;
  }

  @AfterMethod
  public void tearDown() throws Exception {
    _configMaster = null;
  }

  //-------------------------------------------------------------------------
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_constructor_2arg_nullMaster() throws Exception {
    new ConfigMasterIterator<ExternalId>(null, new ConfigSearchRequest<ExternalId>());
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_constructor_2arg_nullRequest() throws Exception {
    new ConfigMasterIterator<ExternalId>(_configMaster, null);
  }

  public void iterate() throws Exception {
    ConfigSearchRequest<ExternalId> request = new ConfigSearchRequest<ExternalId>();
    request.setType(ExternalId.class);
    request.setSortOrder(ConfigSearchSortOrder.NAME_ASC);
    
    ConfigMasterIterator<ExternalId> iterator = new ConfigMasterIterator<ExternalId>(_configMaster, request);
    assertEquals(true, iterator.hasNext());
    assertEquals(0, iterator.nextIndex());
    assertEquals(_item1, iterator.next());
    assertEquals(1, iterator.nextIndex());
    
    assertEquals(true, iterator.hasNext());
    assertEquals(1, iterator.nextIndex());
    assertEquals(_item2, iterator.next());
    assertEquals(2, iterator.nextIndex());
    
    assertEquals(true, iterator.hasNext());
    assertEquals(2, iterator.nextIndex());
    assertEquals(_item3, iterator.next());
    assertEquals(3, iterator.nextIndex());
    
    assertEquals(true, iterator.hasNext());
    assertEquals(3, iterator.nextIndex());
    assertEquals(_item4, iterator.next());
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
    
    ConfigMasterIterator<ExternalId> iterator = new ConfigMasterIterator<ExternalId>(mockMaster, request);
    iterator.hasNext();
  }

}
