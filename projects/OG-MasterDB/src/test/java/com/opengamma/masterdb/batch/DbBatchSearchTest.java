/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.batch;

import static org.testng.Assert.assertTrue;

import java.util.List;

import javax.time.Duration;
import javax.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import com.opengamma.batch.domain.RiskRun;
import com.opengamma.batch.rest.BatchRunSearchRequest;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.paging.Paging;
import com.opengamma.util.test.DbTest;
import com.opengamma.util.tuple.Pair;

/**
 * Tests DbBatchGetTest.
 */
public class DbBatchSearchTest extends AbstractDbBatchMasterTest {
  // superclass sets up dummy database

  private static final Logger s_logger = LoggerFactory.getLogger(DbBatchSearchTest.class);

  @Factory(dataProvider = "databases", dataProviderClass = DbTest.class)
  public DbBatchSearchTest(String databaseType, String databaseVersion) {
    super(databaseType, databaseVersion);
    s_logger.info("running testcases for {}", databaseType);
  }

  //-------------------------------------------------------------------------

  @Test
  public void testSearchBatchByMarketSnapshotUid() {
    BatchRunSearchRequest batchRunSearchRequest = new BatchRunSearchRequest();    
    batchRunSearchRequest.setMarketDataUid(_marketDataSnapshotUid);
    Pair<List<RiskRun>, Paging> searchResult = _batchMaster.searchRiskRun(batchRunSearchRequest);
    assertTrue(searchResult.getFirst().size() > 0);
  }

  @Test
  public void testSearchBatchByMarketSnapshotUidNoResults() {
    UniqueId nonExistentUid = UniqueId.of("MrkDta", "non_existent_market_data_snapshot_uid");
    BatchRunSearchRequest batchRunSearchRequest = new BatchRunSearchRequest();
    batchRunSearchRequest.setMarketDataUid(nonExistentUid);
    Pair<List<RiskRun>, Paging> searchResult = _batchMaster.searchRiskRun(batchRunSearchRequest);
    assertTrue(searchResult.getFirst().size() == 0);
  }

  @Test
  public void testSearchBatchByVersionCorrection() {
    BatchRunSearchRequest batchRunSearchRequest = new BatchRunSearchRequest();
    batchRunSearchRequest.setVersionCorrection(_versionCorrection);
    Pair<List<RiskRun>, Paging> searchResult = _batchMaster.searchRiskRun(batchRunSearchRequest);
    assertTrue(searchResult.getFirst().size() > 0);
  }

  @Test
  public void testSearchBatchByVersionCorrectionNoResults() {
    BatchRunSearchRequest batchRunSearchRequest = new BatchRunSearchRequest();
    batchRunSearchRequest.setVersionCorrection(VersionCorrection.of(Instant.now().minus(Duration.ofStandardHours(3)), Instant.now()));
    Pair<List<RiskRun>, Paging> searchResult = _batchMaster.searchRiskRun(batchRunSearchRequest);
    assertTrue(searchResult.getFirst().size() == 0);
  }

  @Test
  public void testSearchBatchByValuationTime() {
    BatchRunSearchRequest batchRunSearchRequest = new BatchRunSearchRequest();
    batchRunSearchRequest.setValuationTime(_valuationTime);
    Pair<List<RiskRun>, Paging> searchResult = _batchMaster.searchRiskRun(batchRunSearchRequest);
    assertTrue(searchResult.getFirst().size() > 0);
  }

  @Test
  public void testSearchBatchByValuationTimeNoResults() {
    BatchRunSearchRequest batchRunSearchRequest = new BatchRunSearchRequest();
    batchRunSearchRequest.setValuationTime(Instant.now());
    Pair<List<RiskRun>, Paging> searchResult = _batchMaster.searchRiskRun(batchRunSearchRequest);
    assertTrue(searchResult.getFirst().size() == 0);
  }

  @Test
  public void testSearchBatchByViewDefinition() {
    BatchRunSearchRequest batchRunSearchRequest = new BatchRunSearchRequest();
    batchRunSearchRequest.setViewDefinitionUid(_viewDefinitionUid);
    Pair<List<RiskRun>, Paging> searchResult = _batchMaster.searchRiskRun(batchRunSearchRequest);
    assertTrue(searchResult.getFirst().size() > 0);
  }

  @Test
  public void testSearchBatchByViewDefinitionNoResults() {
    UniqueId nonExistentUid = UniqueId.of("ViewDef", "non_existent_view_definition_uid");
    BatchRunSearchRequest batchRunSearchRequest = new BatchRunSearchRequest();
    batchRunSearchRequest.setViewDefinitionUid(nonExistentUid);
    Pair<List<RiskRun>, Paging> searchResult = _batchMaster.searchRiskRun(batchRunSearchRequest);
    assertTrue(searchResult.getFirst().size() == 0);
  }
}
