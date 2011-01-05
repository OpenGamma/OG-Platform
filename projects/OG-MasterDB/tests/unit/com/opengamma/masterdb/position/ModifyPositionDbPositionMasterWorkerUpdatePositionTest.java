/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.position;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.math.BigDecimal;
import java.util.TimeZone;

import javax.time.Instant;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.BadSqlGrammarException;

import com.opengamma.DataNotFoundException;
import com.opengamma.id.Identifier;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.master.position.ManageablePosition;
import com.opengamma.master.position.PositionDocument;
import com.opengamma.master.position.PositionHistoryRequest;
import com.opengamma.master.position.PositionHistoryResult;

/**
 * Tests ModifyPositionDbPositionMasterWorker.
 */
public class ModifyPositionDbPositionMasterWorkerUpdatePositionTest extends AbstractDbPositionMasterWorkerTest {
  // superclass sets up dummy database

  private static final Logger s_logger = LoggerFactory.getLogger(ModifyPositionDbPositionMasterWorkerUpdatePositionTest.class);

  private ModifyPositionDbPositionMasterWorker _worker;
  private DbPositionMasterWorker _queryWorker;

  public ModifyPositionDbPositionMasterWorkerUpdatePositionTest(String databaseType, String databaseVersion) {
    super(databaseType, databaseVersion);
    s_logger.info("running testcases for {}", databaseType);
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
  }

  @Before
  public void setUp() throws Exception {
    super.setUp();
    _worker = new ModifyPositionDbPositionMasterWorker();
    _worker.init(_posMaster);
    _queryWorker = new QueryPositionDbPositionMasterWorker();
    _queryWorker.init(_posMaster);
  }

  @After
  public void tearDown() throws Exception {
    super.tearDown();
    _worker = null;
    _queryWorker = null;
  }

  //-------------------------------------------------------------------------
  @Test(expected = NullPointerException.class)
  public void test_update_nullDocument() {
    _worker.update(null);
  }

  @Test(expected = NullPointerException.class)
  public void test_update_noPositionId() {
    ManageablePosition position = new ManageablePosition(BigDecimal.TEN, Identifier.of("A", "B"));
    PositionDocument doc = new PositionDocument();
    doc.setPosition(position);
    _worker.update(doc);
  }

  @Test(expected = NullPointerException.class)
  public void test_update_noPosition() {
    PositionDocument doc = new PositionDocument();
    doc.setUniqueId(UniqueIdentifier.of("DbPos", "121", "0"));
    _worker.update(doc);
  }

  @Test(expected = DataNotFoundException.class)
  public void test_update_notFound() {
    ManageablePosition pos = new ManageablePosition(BigDecimal.TEN, Identifier.of("A", "B"));
    pos.setUniqueId(UniqueIdentifier.of("DbPos", "0", "0"));
    PositionDocument doc = new PositionDocument(pos);
    _worker.update(doc);
  }

  @Test(expected = IllegalArgumentException.class)
  public void test_update_notLatestVersion() {
    ManageablePosition pos = new ManageablePosition(BigDecimal.TEN, Identifier.of("A", "B"));
    pos.setUniqueId(UniqueIdentifier.of("DbPos", "221", "0"));
    PositionDocument doc = new PositionDocument(pos);
    _worker.update(doc);
  }

  @Test
  public void test_update_getUpdateGet() {
    Instant now = Instant.now(_posMaster.getTimeSource());
    
    PositionDocument base = _queryWorker.get(UniqueIdentifier.of("DbPos", "121", "0"));
    ManageablePosition pos = new ManageablePosition(BigDecimal.TEN, Identifier.of("A", "B"));
    pos.setUniqueId(UniqueIdentifier.of("DbPos", "121", "0"));
    PositionDocument input = new PositionDocument(pos);
    
    PositionDocument updated = _worker.update(input);
    assertEquals(false, base.getUniqueId().equals(updated.getUniqueId()));
    assertEquals(now, updated.getVersionFromInstant());
    assertEquals(null, updated.getVersionToInstant());
    assertEquals(now, updated.getCorrectionFromInstant());
    assertEquals(null, updated.getCorrectionToInstant());
    assertEquals(input.getPosition(), updated.getPosition());
    
    PositionDocument old = _queryWorker.get(UniqueIdentifier.of("DbPos", "121", "0"));
    assertEquals(base.getUniqueId(), old.getUniqueId());
    assertEquals(base.getVersionFromInstant(), old.getVersionFromInstant());
    assertEquals(now, old.getVersionToInstant());  // old version ended
    assertEquals(base.getCorrectionFromInstant(), old.getCorrectionFromInstant());
    assertEquals(base.getCorrectionToInstant(), old.getCorrectionToInstant());
    assertEquals(base.getPosition(), old.getPosition());
    
    PositionHistoryRequest search = new PositionHistoryRequest(base.getUniqueId(), null, now);
    PositionHistoryResult searchResult = _queryWorker.history(search);
    assertEquals(2, searchResult.getDocuments().size());
  }

  @Test
  public void test_update_rollback() {
    ModifyPositionDbPositionMasterWorker w = new ModifyPositionDbPositionMasterWorker() {
      protected String sqlInsertIdKey() {
        return "INSERT";  // bad sql
      };
    };
    w.init(_posMaster);
    final PositionDocument base = _queryWorker.get(UniqueIdentifier.of("DbPos", "121", "0"));
    ManageablePosition pos = new ManageablePosition(BigDecimal.TEN, Identifier.of("A", "B"));
    pos.setUniqueId(UniqueIdentifier.of("DbPos", "121", "0"));
    PositionDocument input = new PositionDocument(pos);
    try {
      w.update(input);
      fail();
    } catch (BadSqlGrammarException ex) {
      // expected
    }
    final PositionDocument test = _queryWorker.get(UniqueIdentifier.of("DbPos", "121", "0"));
    
    assertEquals(base, test);
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_toString() {
    assertEquals(_worker.getClass().getSimpleName() + "[DbPos]", _worker.toString());
  }

}
