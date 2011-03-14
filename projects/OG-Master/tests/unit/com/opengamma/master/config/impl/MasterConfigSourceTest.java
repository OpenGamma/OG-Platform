/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.config.impl;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.opengamma.id.Identifier;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.master.config.ConfigDocument;
import com.opengamma.master.config.ConfigSearchRequest;

/**
 * Test MasterConfigSource.
 */
public class MasterConfigSourceTest {

  private static final ConfigDocument<Identifier> DOC;
  static {
    ConfigDocument<Identifier> doc = new ConfigDocument<Identifier>();
    doc.setName("Test");
    doc.setValue(Identifier.of("A", "B"));
    DOC = doc;
  }
  
  private MasterConfigSource _configSource;

  @Before
  public void setUp() throws Exception {
    InMemoryConfigMaster configMaster = new InMemoryConfigMaster();
    ConfigDocument<?> added = configMaster.add(DOC);
    DOC.setUniqueId(added.getUniqueId());
    _configSource = new MasterConfigSource(configMaster);
  }

  @After
  public void tearDown() throws Exception {
    _configSource = null;
  }

  //-------------------------------------------------------------------------
  @Test(expected = IllegalArgumentException.class)
  public void test_constructor_1arg_nullMaster() throws Exception {
    new MasterConfigSource(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void test_constructor_2arg_nullMaster() throws Exception {
    new MasterConfigSource(null, null);
  }

  @Test
  public void search() throws Exception {
    ConfigSearchRequest<Identifier> request = new ConfigSearchRequest<Identifier>();
    request.setName("Test");
    request.setType(Identifier.class);
    List<Identifier> searchResult = _configSource.search(request);
    assertTrue(searchResult.size() == 1);
    assertEquals(Identifier.of("A", "B"), searchResult.get(0));
  }

  @Test
  public void get() throws Exception {
    Identifier test = _configSource.get(Identifier.class, DOC.getUniqueId());
    assertEquals(Identifier.of("A", "B"), test);
  }

  @Test
  public void accessInvalidDocument() throws Exception {
    UniqueIdentifier uniqueIdentifier = _configSource.get(UniqueIdentifier.class, UniqueIdentifier.of("U", "1"));
    assertNull(uniqueIdentifier);
  }

}
