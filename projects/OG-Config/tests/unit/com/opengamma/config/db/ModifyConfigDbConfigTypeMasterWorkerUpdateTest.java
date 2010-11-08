/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.config.db;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.TimeZone;

import javax.time.Instant;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.BadSqlGrammarException;

import com.opengamma.DataNotFoundException;
import com.opengamma.config.ConfigDocument;
import com.opengamma.config.ConfigHistoryRequest;
import com.opengamma.config.ConfigHistoryResult;
import com.opengamma.id.Identifier;
import com.opengamma.id.UniqueIdentifier;

/**
 * Tests ModifyConfigDbConfigMasterWorker.
 */
public class ModifyConfigDbConfigTypeMasterWorkerUpdateTest extends AbstractDbConfigTypeMasterWorkerTest {
  // superclass sets up dummy database

  private static final Logger s_logger = LoggerFactory.getLogger(ModifyConfigDbConfigTypeMasterWorkerUpdateTest.class);

  private ModifyConfigDbConfigTypeMasterWorker<Identifier> _worker;
  private DbConfigTypeMasterWorker<Identifier> _queryWorker;

  public ModifyConfigDbConfigTypeMasterWorkerUpdateTest(String databaseType, String databaseVersion) {
    super(databaseType, databaseVersion);
    s_logger.info("running testcases for {}", databaseType);
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
  }

  @Before
  public void setUp() throws Exception {
    super.setUp();
    _worker = new ModifyConfigDbConfigTypeMasterWorker<Identifier>();
    _worker.init(_cfgMaster);
    _queryWorker = new QueryConfigDbConfigTypeMasterWorker<Identifier>();
    _queryWorker.init(_cfgMaster);
  }

  @After
  public void tearDown() throws Exception {
    super.tearDown();
    _worker = null;
    _queryWorker = null;
  }

  //-------------------------------------------------------------------------
  @Test(expected = NullPointerException.class)
  public void test_updateConfig_nullDocument() {
    _worker.update(null);
  }

  @Test(expected = NullPointerException.class)
  public void test_update_noConfigId() {
    ConfigDocument<Identifier> doc = new ConfigDocument<Identifier>();
    doc.setName("Name");
    doc.setValue(Identifier.of("A", "B"));
    _worker.update(doc);
  }

  @Test(expected = DataNotFoundException.class)
  public void test_update_notFound() {
    UniqueIdentifier uid = UniqueIdentifier.of("DbCfg", "0", "0");
    ConfigDocument<Identifier> doc = new ConfigDocument<Identifier>();
    doc.setConfigId(uid);
    doc.setName("Name");
    doc.setValue(Identifier.of("A", "B"));
    _worker.update(doc);
  }

  @Test(expected = IllegalArgumentException.class)
  public void test_update_notLatestVersion() {
    UniqueIdentifier uid = UniqueIdentifier.of("DbCfg", "201", "0");
    ConfigDocument<Identifier> doc = new ConfigDocument<Identifier>();
    doc.setConfigId(uid);
    doc.setName("Name");
    doc.setValue(Identifier.of("A", "B"));
    _worker.update(doc);
  }

  @Test
  public void test_update_getUpdateGet() {
    Instant now = Instant.now(_cfgMaster.getTimeSource());
    
    UniqueIdentifier uid = UniqueIdentifier.of("DbCfg", "101", "0");
    ConfigDocument<Identifier> base = _queryWorker.get(uid);
    ConfigDocument<Identifier> input = new ConfigDocument<Identifier>();
    input.setConfigId(uid);
    input.setName("Name");
    input.setValue(Identifier.of("A", "B"));
    
    ConfigDocument<Identifier> updated = _worker.update(input);
    assertEquals(false, base.getConfigId().equals(updated.getConfigId()));
    assertEquals(now, updated.getVersionFromInstant());
    assertEquals(null, updated.getVersionToInstant());
    assertEquals(input.getValue(), updated.getValue());
    
    ConfigDocument<Identifier> old = _queryWorker.get(uid);
    assertEquals(base.getConfigId(), old.getConfigId());
    assertEquals(base.getVersionFromInstant(), old.getVersionFromInstant());
    assertEquals(now, old.getVersionToInstant());  // old version ended
    assertEquals(base.getValue(), old.getValue());
    
    ConfigHistoryRequest search = new ConfigHistoryRequest(base.getConfigId(), null);
    ConfigHistoryResult<Identifier> searchResult = _queryWorker.history(search);
    assertEquals(2, searchResult.getDocuments().size());
  }

  @Test
  public void test_update_rollback() {
    ModifyConfigDbConfigTypeMasterWorker<Identifier> w = new ModifyConfigDbConfigTypeMasterWorker<Identifier>() {
      @Override
      protected String sqlInsertConfig() {
        return "INSERT";  // bad sql
      };
    };
    w.init(_cfgMaster);
    final ConfigDocument<Identifier> base = _queryWorker.get(UniqueIdentifier.of("DbCfg", "101", "0"));
    UniqueIdentifier uid = UniqueIdentifier.of("DbCfg", "101", "0");
    ConfigDocument<Identifier> input = new ConfigDocument<Identifier>();
    input.setConfigId(uid);
    input.setName("Name");
    input.setValue(Identifier.of("A", "B"));
    try {
      w.update(input);
      fail();
    } catch (BadSqlGrammarException ex) {
      // expected
    }
    final ConfigDocument<Identifier> test = _queryWorker.get(UniqueIdentifier.of("DbCfg", "101", "0"));
    
    assertEquals(base, test);
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_toString() {
    assertEquals(_worker.getClass().getSimpleName() + "[DbCfg]", _worker.toString());
  }

}
