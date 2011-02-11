/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.config.impl;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.opengamma.id.Identifier;
import com.opengamma.id.ObjectIdentifiable;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.config.ConfigDocument;
import com.opengamma.master.config.ConfigHistoryRequest;
import com.opengamma.master.config.ConfigHistoryResult;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.master.config.ConfigSearchRequest;
import com.opengamma.master.config.ConfigSearchResult;
import com.opengamma.master.config.ConfigTypeMaster;
import com.opengamma.master.listener.MasterChangeManager;

/**
 * Test MasterConfigSource.
 */
public class MasterConfigSourceTest {

  private static final ConfigDocument<Identifier> DOC;
  static {
    ConfigDocument<Identifier> doc = new ConfigDocument<Identifier>();
    doc.setUniqueId(UniqueIdentifier.of("U", "1"));
    doc.setName("Test");
    doc.setValue(Identifier.of("A", "B"));
    DOC = doc;
  }

  static class MockMaster implements ConfigMaster {
    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public <T> ConfigTypeMaster<T> typed(Class<T> clazz) {
      if (clazz == Identifier.class == false) {
        throw new IllegalArgumentException();
      }
      return (ConfigTypeMaster) new ConfigTypeMaster<Identifier>() {
        @Override
        public ConfigSearchResult<Identifier> search(ConfigSearchRequest request) {
          ConfigSearchResult<Identifier> result = new ConfigSearchResult<Identifier>();
          result.getDocuments().add(DOC);
          return result;
        }
        @Override
        public ConfigDocument<Identifier> get(UniqueIdentifier uniqueId) {
          return DOC;
        }
        @Override
        public ConfigDocument<Identifier> get(ObjectIdentifiable objectId, VersionCorrection versionCorrection) {
          return DOC;
        }
        @Override
        public ConfigDocument<Identifier> add(ConfigDocument<Identifier> document) {
          throw new UnsupportedOperationException();
        }
        @Override
        public ConfigDocument<Identifier> update(ConfigDocument<Identifier> document) {
          throw new UnsupportedOperationException();
        }
        @Override
        public void remove(UniqueIdentifier uniqueId) {
          throw new UnsupportedOperationException();
        }
        @Override
        public ConfigHistoryResult<Identifier> history(ConfigHistoryRequest request) {
          throw new UnsupportedOperationException();
        }
        @Override
        public ConfigDocument<Identifier> correct(ConfigDocument<Identifier> document) {
          throw new UnsupportedOperationException();
        }
        @Override
        public MasterChangeManager changeManager() {
          throw new UnsupportedOperationException();
        }
      };
    }
  }

  private MasterConfigSource _configSource;

  @Before
  public void setUp() throws Exception {
    _configSource = new MasterConfigSource(new MockMaster());
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
    ConfigSearchRequest request = new ConfigSearchRequest();
    request.setName("Test");
    List<Identifier> searchResult = _configSource.search(Identifier.class, request);
    assertTrue(searchResult.size() == 1);
    assertEquals(Identifier.of("A", "B"), searchResult.get(0));
  }

  @Test
  public void get() throws Exception {
    Identifier test = _configSource.get(Identifier.class, UniqueIdentifier.of("U", "1"));
    assertEquals(Identifier.of("A", "B"), test);
  }

  @Test(expected=IllegalArgumentException.class)
  public void accessInvalidType() throws Exception {
    _configSource.get(UniqueIdentifier.class, UniqueIdentifier.of("U", "1"));
  }

}
