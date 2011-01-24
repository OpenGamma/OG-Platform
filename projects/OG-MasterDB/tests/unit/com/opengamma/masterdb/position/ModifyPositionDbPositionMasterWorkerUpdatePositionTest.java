/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.position;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.math.BigDecimal;
import java.util.TimeZone;

import javax.time.Instant;

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

  public ModifyPositionDbPositionMasterWorkerUpdatePositionTest(String databaseType, String databaseVersion) {
    super(databaseType, databaseVersion);
    s_logger.info("running testcases for {}", databaseType);
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
  }

  //-------------------------------------------------------------------------
  @Test(expected = IllegalArgumentException.class)
  public void test_update_nullDocument() {
    _posMaster.update(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void test_update_noPositionId() {
    ManageablePosition position = new ManageablePosition(BigDecimal.TEN, Identifier.of("A", "B"));
    PositionDocument doc = new PositionDocument();
    doc.setPosition(position);
    _posMaster.update(doc);
  }

  @Test(expected = IllegalArgumentException.class)
  public void test_update_noPosition() {
    PositionDocument doc = new PositionDocument();
    doc.setUniqueId(UniqueIdentifier.of("DbPos", "121", "0"));
    _posMaster.update(doc);
  }

  @Test(expected = DataNotFoundException.class)
  public void test_update_notFound() {
    ManageablePosition pos = new ManageablePosition(BigDecimal.TEN, Identifier.of("A", "B"));
    pos.setUniqueId(UniqueIdentifier.of("DbPos", "0", "0"));
    PositionDocument doc = new PositionDocument(pos);
    _posMaster.update(doc);
  }

  @Test(expected = IllegalArgumentException.class)
  public void test_update_notLatestVersion() {
    ManageablePosition pos = new ManageablePosition(BigDecimal.TEN, Identifier.of("A", "B"));
    pos.setUniqueId(UniqueIdentifier.of("DbPos", "221", "0"));
    PositionDocument doc = new PositionDocument(pos);
    _posMaster.update(doc);
  }

  @Test
  public void test_update_getUpdateGet() {
    Instant now = Instant.now(_posMaster.getTimeSource());
    
    PositionDocument base = _posMaster.get(UniqueIdentifier.of("DbPos", "121", "0"));
    ManageablePosition pos = new ManageablePosition(BigDecimal.TEN, Identifier.of("A", "B"));
    pos.setUniqueId(UniqueIdentifier.of("DbPos", "121", "0"));
    PositionDocument input = new PositionDocument(pos);
    
    PositionDocument updated = _posMaster.update(input);
    assertEquals(false, base.getUniqueId().equals(updated.getUniqueId()));
    assertEquals(now, updated.getVersionFromInstant());
    assertEquals(null, updated.getVersionToInstant());
    assertEquals(now, updated.getCorrectionFromInstant());
    assertEquals(null, updated.getCorrectionToInstant());
    assertEquals(input.getPosition(), updated.getPosition());
    
    PositionDocument old = _posMaster.get(UniqueIdentifier.of("DbPos", "121", "0"));
    assertEquals(base.getUniqueId(), old.getUniqueId());
    assertEquals(base.getVersionFromInstant(), old.getVersionFromInstant());
    assertEquals(now, old.getVersionToInstant());  // old version ended
    assertEquals(base.getCorrectionFromInstant(), old.getCorrectionFromInstant());
    assertEquals(base.getCorrectionToInstant(), old.getCorrectionToInstant());
    assertEquals(base.getPosition(), old.getPosition());
    
    PositionHistoryRequest search = new PositionHistoryRequest(base.getUniqueId(), null, now);
    PositionHistoryResult searchResult = _posMaster.history(search);
    assertEquals(2, searchResult.getDocuments().size());
  }

  @Test
  public void test_update_rollback() {
    DbPositionMaster w = new DbPositionMaster(_posMaster.getDbSource()) {
      protected String sqlInsertIdKey() {
        return "INSERT";  // bad sql
      }
    };
    final PositionDocument base = _posMaster.get(UniqueIdentifier.of("DbPos", "121", "0"));
    ManageablePosition pos = new ManageablePosition(BigDecimal.TEN, Identifier.of("A", "B"));
    pos.setUniqueId(UniqueIdentifier.of("DbPos", "121", "0"));
    PositionDocument input = new PositionDocument(pos);
    try {
      w.update(input);
      fail();
    } catch (BadSqlGrammarException ex) {
      // expected
    }
    final PositionDocument test = _posMaster.get(UniqueIdentifier.of("DbPos", "121", "0"));
    
    assertEquals(base, test);
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_toString() {
    assertEquals(_posMaster.getClass().getSimpleName() + "[DbPos]", _posMaster.toString());
  }

}
