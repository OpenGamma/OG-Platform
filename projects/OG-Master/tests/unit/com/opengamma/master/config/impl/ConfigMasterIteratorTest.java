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

  private ConfigDocument<ExternalId> _doc1;
  private ConfigDocument<ExternalId> _doc2;
  private ConfigDocument<ExternalId> _doc3;
  private ConfigDocument<ExternalId> _doc4;

  private ConfigMaster _configMaster;

  @BeforeMethod
  public void setUp() throws Exception {
    ConfigDocument<ExternalId> doc1 = new ConfigDocument<ExternalId>(ExternalId.class);
    doc1.setName("Test1");
    doc1.setValue(ExternalId.of("A", "B"));
    _doc1 = doc1;
    ConfigDocument<ExternalId> doc2 = new ConfigDocument<ExternalId>(ExternalId.class);
    doc2.setName("Test2");
    doc2.setValue(ExternalId.of("C", "D"));
    _doc2 = doc2;
    ConfigDocument<ExternalId> doc3 = new ConfigDocument<ExternalId>(ExternalId.class);
    doc3.setName("Test3");
    doc3.setValue(ExternalId.of("E", "F"));
    _doc3 = doc3;
    ConfigDocument<ExternalId> doc4 = new ConfigDocument<ExternalId>(ExternalId.class);
    doc4.setName("Test4");
    doc4.setValue(ExternalId.of("G", "H"));
    _doc4 = doc4;
    
    InMemoryConfigMaster configMaster = new InMemoryConfigMaster();
    _doc1 = configMaster.add(_doc1);
    _doc2 = configMaster.add(_doc2);
    _doc3 = configMaster.add(_doc3);
    _doc4 = configMaster.add(_doc4);
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
    
    ConfigMasterIterator<ExternalId> iterator = new ConfigMasterIterator<ExternalId>(mockMaster, request);
    iterator.hasNext();
  }

}
