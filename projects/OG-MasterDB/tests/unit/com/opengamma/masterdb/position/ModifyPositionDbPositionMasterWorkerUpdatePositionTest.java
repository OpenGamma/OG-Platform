/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.position;

import static org.testng.AssertJUnit.*;

import java.math.BigDecimal;
import java.util.TimeZone;

import javax.time.Instant;
import javax.time.calendar.LocalDate;
import javax.time.calendar.OffsetTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.BadSqlGrammarException;
import org.testng.Assert;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import com.opengamma.DataNotFoundException;
import com.opengamma.id.Identifier;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.master.position.ManageablePosition;
import com.opengamma.master.position.ManageableTrade;
import com.opengamma.master.position.PositionDocument;
import com.opengamma.master.position.PositionHistoryRequest;
import com.opengamma.master.position.PositionHistoryResult;
import com.opengamma.util.test.DBTest;

/**
 * Tests ModifyPositionDbPositionMasterWorker.
 */
public class ModifyPositionDbPositionMasterWorkerUpdatePositionTest extends AbstractDbPositionMasterWorkerTest {
  // superclass sets up dummy database

  private static final Logger s_logger = LoggerFactory.getLogger(ModifyPositionDbPositionMasterWorkerUpdatePositionTest.class);

  @Factory(dataProvider = "databases", dataProviderClass = DBTest.class)
  public ModifyPositionDbPositionMasterWorkerUpdatePositionTest(String databaseType, String databaseVersion) {
    super(databaseType, databaseVersion);
    s_logger.info("running testcases for {}", databaseType);
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
  }

  //-------------------------------------------------------------------------
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_update_nullDocument() {
    _posMaster.update(null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_update_noPositionId() {
    ManageablePosition position = new ManageablePosition(BigDecimal.TEN, Identifier.of("A", "B"));
    PositionDocument doc = new PositionDocument();
    doc.setPosition(position);
    _posMaster.update(doc);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_update_noPosition() {
    PositionDocument doc = new PositionDocument();
    doc.setUniqueId(UniqueIdentifier.of("DbPos", "121", "0"));
    _posMaster.update(doc);
  }

  @Test(expectedExceptions = DataNotFoundException.class)
  public void test_update_notFound() {
    ManageablePosition pos = new ManageablePosition(BigDecimal.TEN, Identifier.of("A", "B"));
    pos.setUniqueId(UniqueIdentifier.of("DbPos", "0", "0"));
    PositionDocument doc = new PositionDocument(pos);
    _posMaster.update(doc);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
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
  public void test_updateWithTrades_getUpdateGet() {
    Instant now = Instant.now(_posMaster.getTimeSource());
    
    PositionDocument base = _posMaster.get(UniqueIdentifier.of("DbPos", "121", "0"));
    ManageablePosition pos = new ManageablePosition(BigDecimal.TEN, Identifier.of("A", "B"));
    pos.setUniqueId(UniqueIdentifier.of("DbPos", "121", "0"));
    pos.addTrade(new ManageableTrade(BigDecimal.TEN, Identifier.of("C", "D"), _now.toLocalDate(), _now.toOffsetTime().minusSeconds(500), Identifier.of("CPS2", "CPV2")));
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
    
    PositionDocument latestDoc = _posMaster.get(updated.getUniqueId());
    assertNotNull(latestDoc);
    assertEquals(updated.getPosition(), latestDoc.getPosition());
    
    PositionHistoryRequest search = new PositionHistoryRequest(base.getUniqueId(), null, now);
    PositionHistoryResult searchResult = _posMaster.history(search);
    assertEquals(2, searchResult.getDocuments().size());
  }
  
  @Test
  public void test_updateTradeAttributes() {
    ManageablePosition pos1 = new ManageablePosition(BigDecimal.TEN, Identifier.of("A", "B"));
    
    LocalDate tradeDate = _now.toLocalDate();
    OffsetTime tradeTime = _now.toOffsetTime().minusSeconds(500);
    
    ManageableTrade tradeA = new ManageableTrade(BigDecimal.TEN, Identifier.of("A", "B"), tradeDate, tradeTime, Identifier.of("CPS", "CPV"));
    tradeA.addAttribute("key11", "Value11");
    tradeA.addAttribute("key12", "Value12");
    pos1.addTrade(tradeA);
    
    ManageableTrade tradeB = new ManageableTrade(BigDecimal.TEN, Identifier.of("C", "D"), tradeDate, tradeTime, Identifier.of("CPS2", "CPV2"));
    tradeB.addAttribute("key21", "Value21");
    tradeB.addAttribute("key22", "Value22");
    pos1.addTrade(tradeB);
    
    PositionDocument doc = new PositionDocument(pos1);
    PositionDocument version1 = _posMaster.add(doc);
    assertNotNull(version1.getUniqueId());
    assertNotNull(tradeA.getUniqueId());
    assertNotNull(tradeB.getUniqueId());
    assertEquals(version1.getPosition(), _posMaster.get(pos1.getUniqueId()).getPosition());
    
    ManageablePosition pos2 = new ManageablePosition(BigDecimal.TEN, Identifier.of("A", "B"));
    pos2.setUniqueId(version1.getUniqueId());
    ManageableTrade tradeC = new ManageableTrade(BigDecimal.TEN, Identifier.of("A", "B"), tradeDate, tradeTime, Identifier.of("CPS", "CPV"));
    tradeC.addAttribute("A", "B");
    tradeC.addAttribute("C", "D");
    tradeC.addAttribute("E", "F");
    pos2.addTrade(tradeC);
    
    PositionDocument version2 = _posMaster.update(new PositionDocument(pos2));
    assertNotNull(version2);
    assertEquals(false, version1.getUniqueId().equals(version2.getUniqueId()));
    assertNotNull(tradeC.getUniqueId());
    assertEquals(version2.getPosition(), _posMaster.get(version2.getUniqueId()).getPosition());
    
    PositionHistoryRequest search = new PositionHistoryRequest(version1.getUniqueId(), null, Instant.now(_posMaster.getTimeSource()));
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
      Assert.fail();
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
