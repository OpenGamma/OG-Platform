/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.config;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.opengamma.config.ConfigDocument;
import com.opengamma.config.ConfigMaster;
import com.opengamma.config.ConfigTypeMaster;
import com.opengamma.config.ConfigHistoryRequest;
import com.opengamma.config.ConfigHistoryResult;
import com.opengamma.config.ConfigSearchRequest;
import com.opengamma.config.ConfigSearchResult;
import com.opengamma.id.Identifier;
import com.opengamma.id.UniqueIdentifier;

/**
 * Test MasterConfigSource.
 */
public class MasterConfigSourceTest {

  private static final ConfigDocument<Identifier> DOC;
  static {
    ConfigDocument<Identifier> doc = new ConfigDocument<Identifier>();
    doc.setConfigId(UniqueIdentifier.of("U", "1"));
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
        public ConfigDocument<Identifier> get(UniqueIdentifier uid) {
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
        public void remove(UniqueIdentifier uid) {
          throw new UnsupportedOperationException();
        }
        @Override
        public ConfigHistoryResult<Identifier> history(ConfigHistoryRequest request) {
          throw new UnsupportedOperationException();
        }
      };
    }
  }

  private ConfigSource _configSource;

  @Before
  public void setUp() throws Exception {
    _configSource = new MasterConfigSource(new MockMaster());
  }

  @After
  public void tearDown() throws Exception {
    _configSource = null;
  }

  //-------------------------------------------------------------------------
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
