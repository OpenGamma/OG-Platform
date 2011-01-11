/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.position;

import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;
import java.util.TimeZone;

import javax.time.Instant;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
public class ModifyPositionDbPositionMasterWorkerCorrectPositionTest extends AbstractDbPositionMasterWorkerTest {
  // superclass sets up dummy database

  private static final Logger s_logger = LoggerFactory.getLogger(ModifyPositionDbPositionMasterWorkerCorrectPositionTest.class);

  public ModifyPositionDbPositionMasterWorkerCorrectPositionTest(String databaseType, String databaseVersion) {
    super(databaseType, databaseVersion);
    s_logger.info("running testcases for {}", databaseType);
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
  }

  //-------------------------------------------------------------------------
  @Test(expected = IllegalArgumentException.class)
  public void test_correct_nullDocument() {
    _posMaster.correct(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void test_correct_noPositionId() {
    ManageablePosition position = new ManageablePosition(BigDecimal.TEN, Identifier.of("A", "B"));
    PositionDocument doc = new PositionDocument();
    doc.setPosition(position);
    _posMaster.correct(doc);
  }

  @Test(expected = IllegalArgumentException.class)
  public void test_correct_noPosition() {
    PositionDocument doc = new PositionDocument();
    doc.setUniqueId(UniqueIdentifier.of("DbPos", "121", "0"));
    _posMaster.correct(doc);
  }

  @Test(expected = DataNotFoundException.class)
  public void test_correct_notFound() {
    ManageablePosition pos = new ManageablePosition(BigDecimal.TEN, Identifier.of("A", "B"));
    pos.setUniqueId(UniqueIdentifier.of("DbPos", "0", "0"));
    PositionDocument doc = new PositionDocument(pos);
    _posMaster.correct(doc);
  }

//  @Test(expected = IllegalArgumentException.class)
//  public void test_correct_notLatestCorrection() {
//    PortfolioTreePosition pos = new PortfolioTreePosition(UniqueIdentifier.of("DbPos", "221", "221"), BigDecimal.TEN, Identifier.of("A", "B"));
//    PositionDocument doc = new PositionDocument(pos);
//    _posMaster.correct(doc);
//  }

  @Test
  public void test_correct_getUpdateGet() {
    Instant now = Instant.now(_posMaster.getTimeSource());
    
    PositionDocument base = _posMaster.get(UniqueIdentifier.of("DbPos", "121", "0"));
    ManageablePosition pos = new ManageablePosition(BigDecimal.TEN, Identifier.of("A", "B"));
    pos.setUniqueId(UniqueIdentifier.of("DbPos", "121", "0"));
    PositionDocument input = new PositionDocument(pos);
    
    PositionDocument corrected = _posMaster.correct(input);
    assertEquals(false, base.getUniqueId().equals(corrected.getUniqueId()));
    assertEquals(base.getVersionFromInstant(), corrected.getVersionFromInstant());
    assertEquals(base.getVersionToInstant(), corrected.getVersionToInstant());
    assertEquals(now, corrected.getCorrectionFromInstant());
    assertEquals(null, corrected.getCorrectionToInstant());
    assertEquals(input.getPosition(), corrected.getPosition());
    
    PositionDocument old = _posMaster.get(UniqueIdentifier.of("DbPos", "121", "0"));
    assertEquals(base.getUniqueId(), old.getUniqueId());
    assertEquals(base.getVersionFromInstant(), old.getVersionFromInstant());
    assertEquals(base.getVersionToInstant(), old.getVersionToInstant());
    assertEquals(base.getCorrectionFromInstant(), old.getCorrectionFromInstant());
    assertEquals(now, old.getCorrectionToInstant());  // old version ended
    assertEquals(base.getPosition(), old.getPosition());
    
    PositionHistoryRequest search = new PositionHistoryRequest(base.getUniqueId(), now, null);
    PositionHistoryResult searchResult = _posMaster.history(search);
    assertEquals(2, searchResult.getDocuments().size());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_toString() {
    assertEquals(_posMaster.getClass().getSimpleName() + "[DbPos]", _posMaster.toString());
  }

}
