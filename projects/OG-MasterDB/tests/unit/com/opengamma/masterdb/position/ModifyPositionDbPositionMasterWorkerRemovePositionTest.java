/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.position;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;

import java.math.BigDecimal;
import java.util.TimeZone;

import javax.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import com.opengamma.DataNotFoundException;
import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.master.position.ManageablePosition;
import com.opengamma.master.position.PositionDocument;
import com.opengamma.util.test.DBTest;

/**
 * Tests ModifyPositionDbPositionMasterWorker.
 */
public class ModifyPositionDbPositionMasterWorkerRemovePositionTest extends AbstractDbPositionMasterWorkerTest {
  // superclass sets up dummy database

  private static final Logger s_logger = LoggerFactory.getLogger(ModifyPositionDbPositionMasterWorkerRemovePositionTest.class);

  @Factory(dataProvider = "databases", dataProviderClass = DBTest.class)
  public ModifyPositionDbPositionMasterWorkerRemovePositionTest(String databaseType, String databaseVersion) {
    super(databaseType, databaseVersion);
    s_logger.info("running testcases for {}", databaseType);
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
  }

  //-------------------------------------------------------------------------
  @Test(expectedExceptions = DataNotFoundException.class)
  public void test_removePosition_versioned_notFound() {
    UniqueIdentifier uid = UniqueIdentifier.of("DbPos", "0", "0");
    _posMaster.remove(uid);
  }

  @Test
  public void test_removePosition_removed() {
    Instant now = Instant.now(_posMaster.getTimeSource());
    
    UniqueIdentifier uid = UniqueIdentifier.of("DbPos", "122", "0");
    _posMaster.remove(uid);
    PositionDocument test = _posMaster.get(uid);
    
    assertEquals(uid, test.getUniqueId());
    assertEquals(_version1Instant, test.getVersionFromInstant());
    assertEquals(now, test.getVersionToInstant());
    assertEquals(_version1Instant, test.getCorrectionFromInstant());
    assertEquals(null, test.getCorrectionToInstant());
    ManageablePosition position = test.getPosition();
    assertNotNull(position);
    assertEquals(uid, position.getUniqueId());
    assertEquals(BigDecimal.valueOf(122.987), position.getQuantity());
    IdentifierBundle secKey = position.getSecurityLink().getWeakId();
    assertEquals(1, secKey.size());
    assertEquals(Identifier.of("TICKER", "ORCL"), secKey.getIdentifiers().iterator().next());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_toString() {
    assertEquals(_posMaster.getClass().getSimpleName() + "[DbPos]", _posMaster.toString());
  }

}
