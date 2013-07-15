/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.position;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;

import java.math.BigDecimal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.threeten.bp.Instant;

import com.opengamma.DataNotFoundException;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.master.position.ManageablePosition;
import com.opengamma.master.position.PositionDocument;
import com.opengamma.util.test.DbTest;
import com.opengamma.util.test.TestGroup;

/**
 * Tests ModifyPositionDbPositionMasterWorker.
 */
@Test(groups = TestGroup.UNIT_DB)
public class ModifyPositionDbPositionMasterWorkerRemovePositionTest extends AbstractDbPositionMasterWorkerTest {
  // superclass sets up dummy database

  private static final Logger s_logger = LoggerFactory.getLogger(ModifyPositionDbPositionMasterWorkerRemovePositionTest.class);

  @Factory(dataProvider = "databases", dataProviderClass = DbTest.class)
  public ModifyPositionDbPositionMasterWorkerRemovePositionTest(String databaseType, String databaseVersion) {
    super(databaseType, databaseVersion, false);
    s_logger.info("running testcases for {}", databaseType);
  }

  //-------------------------------------------------------------------------
  @Test(expectedExceptions = DataNotFoundException.class)
  public void test_removePosition_versioned_notFound() {
    UniqueId uniqueId = UniqueId.of("DbPos", "0", "0");
    _posMaster.remove(uniqueId);
  }

  @Test
  public void test_removePosition_removed() {
    Instant now = Instant.now(_posMaster.getClock());
    
    UniqueId uniqueId = UniqueId.of("DbPos", "122", "0");
    _posMaster.remove(uniqueId);
    PositionDocument test = _posMaster.get(uniqueId);
    
    assertEquals(uniqueId, test.getUniqueId());
    assertEquals(_version1Instant, test.getVersionFromInstant());
    assertEquals(now, test.getVersionToInstant());
    assertEquals(_version1Instant, test.getCorrectionFromInstant());
    assertEquals(null, test.getCorrectionToInstant());
    ManageablePosition position = test.getPosition();
    assertNotNull(position);
    assertEquals(uniqueId, position.getUniqueId());
    assertEquals(BigDecimal.valueOf(122.987), position.getQuantity());
    ExternalIdBundle secKey = position.getSecurityLink().getExternalId();
    assertEquals(1, secKey.size());
    assertEquals(ExternalId.of("TICKER", "ORCL"), secKey.getExternalIds().iterator().next());
  }

}
