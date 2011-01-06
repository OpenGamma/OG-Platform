/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.position;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.util.TimeZone;

import javax.time.Instant;
import javax.time.calendar.LocalDate;
import javax.time.calendar.OffsetDateTime;
import javax.time.calendar.OffsetTime;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.master.position.ManageablePosition;
import com.opengamma.master.position.ManageableTrade;
import com.opengamma.master.position.PositionDocument;

/**
 * Tests ModifyPositionDbPositionMasterWorker.
 */
public class ModifyPositionDbPositionMasterWorkerAddPositionTest extends AbstractDbPositionMasterWorkerTest {
  // superclass sets up dummy database

  private static final Logger s_logger = LoggerFactory.getLogger(ModifyPositionDbPositionMasterWorkerAddPositionTest.class);

  private ModifyPositionDbPositionMasterWorker _worker;
  private DbPositionMasterWorker _queryWorker;

  public ModifyPositionDbPositionMasterWorkerAddPositionTest(String databaseType, String databaseVersion) {
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
  public void test_add_nullDocument() {
    _worker.add(null);
  }

  @Test(expected = NullPointerException.class)
  public void test_add_noPosition() {
    PositionDocument doc = new PositionDocument();
    _worker.add(doc);
  }

  @Test
  public void test_add_add() {
    Instant now = Instant.now(_posMaster.getTimeSource());
    
    ManageablePosition position = new ManageablePosition(BigDecimal.TEN, Identifier.of("A", "B"));
    PositionDocument doc = new PositionDocument();
    doc.setPosition(position);
    PositionDocument test = _worker.add(doc);
    
    UniqueIdentifier uid = test.getUniqueId();
    assertNotNull(uid);
    assertEquals("DbPos", uid.getScheme());
    assertTrue(uid.isVersioned());
    assertTrue(Long.parseLong(uid.getValue()) >= 1000);
    assertEquals("0", uid.getVersion());
    assertEquals(now, test.getVersionFromInstant());
    assertEquals(null, test.getVersionToInstant());
    assertEquals(now, test.getCorrectionFromInstant());
    assertEquals(null, test.getCorrectionToInstant());
    ManageablePosition testPosition = test.getPosition();
    assertNotNull(testPosition);
    assertEquals(uid, testPosition.getUniqueId());
    assertEquals(BigDecimal.TEN, testPosition.getQuantity());
    IdentifierBundle secKey = testPosition.getSecurityKey();
    assertNotNull(secKey);
    assertEquals(1, secKey.size());
    assertTrue(secKey.getIdentifiers().contains(Identifier.of("A", "B")));
  }

  @Test
  public void test_addWithOneTrade_add() {
    
    LocalDate tradeDate = _now.toLocalDate();
    OffsetTime tradeTime = _now.toOffsetTime().minusSeconds(500);
    
    ManageablePosition position = new ManageablePosition(BigDecimal.TEN, Identifier.of("A", "B"));
    position.getTrades().add(new ManageableTrade(BigDecimal.TEN, Identifier.of("A", "B"), tradeDate, tradeTime, Identifier.of("CPS", "CPV")));
    
    PositionDocument doc = new PositionDocument();
    doc.setPosition(position);
    PositionDocument test = _worker.add(doc);
    
    Instant now = Instant.now(_posMaster.getTimeSource());
    UniqueIdentifier uid = test.getUniqueId();
    assertNotNull(uid);
    assertEquals("DbPos", uid.getScheme());
    assertTrue(uid.isVersioned());
    assertTrue(Long.parseLong(uid.getValue()) >= 1000);
    assertEquals("0", uid.getVersion());
    assertEquals(now, test.getVersionFromInstant());
    assertEquals(null, test.getVersionToInstant());
    assertEquals(now, test.getCorrectionFromInstant());
    assertEquals(null, test.getCorrectionToInstant());
    ManageablePosition testPosition = test.getPosition();
    assertNotNull(testPosition);
    assertEquals(uid, testPosition.getUniqueId());
    assertEquals(BigDecimal.TEN, testPosition.getQuantity());
    IdentifierBundle secKey = testPosition.getSecurityKey();
    assertNotNull(secKey);
    assertEquals(1, secKey.size());
    assertTrue(secKey.getIdentifiers().contains(Identifier.of("A", "B")));
    
    assertNotNull(testPosition.getTrades());
    assertEquals(1, testPosition.getTrades().size());
    ManageableTrade testTrade = testPosition.getTrades().get(0);
    assertNotNull(testTrade);
    assertEquals(BigDecimal.TEN, testTrade.getQuantity());
    assertEquals(tradeDate, testTrade.getTradeDate());
    assertEquals(tradeTime, testTrade.getTradeTime());
    assertEquals(Identifier.of("CPS", "CPV"), testTrade.getCounterpartyId());
    assertEquals(secKey, testTrade.getSecurityKey());
  }

  @Test
  public void test_addWithTwoTrades_add() {
    Instant now = Instant.now(_posMaster.getTimeSource());
    
    OffsetDateTime offsetDateTime = OffsetDateTime.now();
    
    ManageablePosition position = new ManageablePosition(BigDecimal.TEN, Identifier.of("A", "B"));
    position.getTrades().add(new ManageableTrade(BigDecimal.TEN, Identifier.of("A", "C"), offsetDateTime.toLocalDate(), offsetDateTime.minusSeconds(600).toOffsetTime(), Identifier.of("CPS", "CPV")));
    position.getTrades().add(new ManageableTrade(BigDecimal.TEN, Identifier.of("A", "D"), offsetDateTime.toLocalDate(), offsetDateTime.minusSeconds(500).toOffsetTime(), Identifier.of("CPS", "CPV")));
    
    PositionDocument doc = new PositionDocument();
    doc.setPosition(position);
    PositionDocument test = _worker.add(doc);
    
    UniqueIdentifier positionUid = test.getUniqueId();
    assertNotNull(positionUid);
    assertEquals("DbPos", positionUid.getScheme());
    assertTrue(positionUid.isVersioned());
    assertTrue(Long.parseLong(positionUid.getValue()) >= 1000);
    assertEquals("0", positionUid.getVersion());
    assertEquals(now, test.getVersionFromInstant());
    assertEquals(null, test.getVersionToInstant());
    assertEquals(now, test.getCorrectionFromInstant());
    assertEquals(null, test.getCorrectionToInstant());
    ManageablePosition testPosition = test.getPosition();
    assertNotNull(testPosition);
    assertEquals(positionUid, testPosition.getUniqueId());
    assertEquals(BigDecimal.TEN, testPosition.getQuantity());
    IdentifierBundle secKey = testPosition.getSecurityKey();
    assertNotNull(secKey);
    assertEquals(1, secKey.size());
    assertTrue(secKey.getIdentifiers().contains(Identifier.of("A", "B")));
    
    assertNotNull(testPosition.getTrades());
    assertTrue(testPosition.getTrades().size() == 2);
    for (ManageableTrade testTrade : testPosition.getTrades()) {
      assertNotNull(testTrade);
      UniqueIdentifier tradeUid = testTrade.getUniqueId();
      assertNotNull(tradeUid);
      assertEquals("DbPos", positionUid.getScheme());
      assertTrue(positionUid.isVersioned());
      assertTrue(Long.parseLong(positionUid.getValue()) >= 1000);
      assertEquals("0", positionUid.getVersion());
      assertEquals(positionUid, testTrade.getPositionId());
    }
  }

  @Test
  public void test_add_addThenGet() {
    ManageablePosition position = new ManageablePosition(BigDecimal.TEN, Identifier.of("A", "B"));
    PositionDocument doc = new PositionDocument();
    doc.setPosition(position);
    PositionDocument added = _worker.add(doc);
    
    PositionDocument test = _queryWorker.get(added.getUniqueId());
    assertEquals(added, test);
  }

  @Test
  public void test_addWithOneTrade_addThenGet() {
    ManageablePosition position = new ManageablePosition(BigDecimal.TEN, Identifier.of("A", "B"));
    
    LocalDate tradeDate = _now.toLocalDate();
    OffsetTime tradeTime = _now.toOffsetTime().minusSeconds(500);
    
    position.getTrades().add(new ManageableTrade(BigDecimal.TEN, Identifier.of("A", "B"), tradeDate, tradeTime, Identifier.of("CPS", "CPV")));
    
    PositionDocument doc = new PositionDocument();
    doc.setPosition(position);
    PositionDocument added = _worker.add(doc);
    
    PositionDocument test = _queryWorker.get(added.getUniqueId());
        
    assertEquals(added, test);
  }

  @Test
  public void test_addWithTwoTrades_addThenGet() {
    ManageablePosition position = new ManageablePosition(BigDecimal.valueOf(20), Identifier.of("A", "B"));
    
    OffsetDateTime offsetDateTime = OffsetDateTime.now();
    
    position.getTrades().add(new ManageableTrade(BigDecimal.TEN, Identifier.of("A", "B"), offsetDateTime.toLocalDate(), offsetDateTime.minusSeconds(600).toOffsetTime(), Identifier.of("CPS", "CPV")));
    position.getTrades().add(new ManageableTrade(BigDecimal.TEN, Identifier.of("A", "C"), offsetDateTime.toLocalDate(), offsetDateTime.minusSeconds(500).toOffsetTime(), Identifier.of("CPS", "CPV")));
    
    PositionDocument doc = new PositionDocument();
    doc.setPosition(position);
    PositionDocument added = _worker.add(doc);
    
    PositionDocument test = _queryWorker.get(added.getUniqueId());
    assertEquals(added, test);
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_toString() {
    assertEquals(_worker.getClass().getSimpleName() + "[DbPos]", _worker.toString());
  }

}
