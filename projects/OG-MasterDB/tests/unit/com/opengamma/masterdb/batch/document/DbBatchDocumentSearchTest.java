/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.batch.document;

import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.test.DbTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import javax.time.Duration;
import javax.time.Instant;

import static org.testng.Assert.assertTrue;

/**
 * Tests DbBatchDocumentGetTest.
 */
public class DbBatchDocumentSearchTest extends AbstractDbBatchDocumentMasterTest {
  // superclass sets up dummy database

  private static final Logger s_logger = LoggerFactory.getLogger(DbBatchDocumentSearchTest.class);

  @Factory(dataProvider = "databases", dataProviderClass = DbTest.class)
  public DbBatchDocumentSearchTest(String databaseType, String databaseVersion) {
    super(databaseType, databaseVersion);
    s_logger.info("running testcases for {}", databaseType);
  }

  //-------------------------------------------------------------------------

  @Test
  public void testSearchBatchByMarketSnapshotUid() {
    BatchSearchRequest batchSearchRequest = new BatchSearchRequest();
    batchSearchRequest.setMarketDataSnapshotUid(_marketDataSnapshotUid);
    BatchSearchResult searchResult = _batchMaster.search(batchSearchRequest);
    assertTrue(searchResult.getDocuments().size() > 0);
  }

  @Test
  public void testSearchBatchByMarketSnapshotUidNoResults() {
    UniqueId nonExistentUid = UniqueId.of("MrkDta", "non_existent_market_data_snapshot_uid");
    BatchSearchRequest batchSearchRequest = new BatchSearchRequest();
    batchSearchRequest.setMarketDataSnapshotUid(nonExistentUid);
    BatchSearchResult searchResult = _batchMaster.search(batchSearchRequest);
    assertTrue(searchResult.getDocuments().size() == 0);
  }

  @Test
  public void testSearchBatchByVersionCorrection() {
    BatchSearchRequest batchSearchRequest = new BatchSearchRequest();
    batchSearchRequest.setVersionCorrection(_versionCorrection);
    BatchSearchResult searchResult = _batchMaster.search(batchSearchRequest);
    assertTrue(searchResult.getDocuments().size() > 0);
  }

  @Test
  public void testSearchBatchByVersionCorrectionNoResults() {
    BatchSearchRequest batchSearchRequest = new BatchSearchRequest();
    batchSearchRequest.setVersionCorrection(VersionCorrection.of(Instant.now().minus(Duration.ofStandardHours(3)), Instant.now()));
    BatchSearchResult searchResult = _batchMaster.search(batchSearchRequest);
    assertTrue(searchResult.getDocuments().size() == 0);
  }

  @Test
  public void testSearchBatchByValuationTime() {
    BatchSearchRequest batchSearchRequest = new BatchSearchRequest();
    batchSearchRequest.setValuationTime(_valuationTime);
    BatchSearchResult searchResult = _batchMaster.search(batchSearchRequest);
    assertTrue(searchResult.getDocuments().size() > 0);
  }

  @Test
  public void testSearchBatchByValuationTimeNoResults() {
    BatchSearchRequest batchSearchRequest = new BatchSearchRequest();
    batchSearchRequest.setValuationTime(Instant.now());
    BatchSearchResult searchResult = _batchMaster.search(batchSearchRequest);
    assertTrue(searchResult.getDocuments().size() == 0);
  }

  @Test
  public void testSearchBatchByViewDefinition() {
    BatchSearchRequest batchSearchRequest = new BatchSearchRequest();
    batchSearchRequest.setViewDefinitionUid(_viewDefinitionUid);
    BatchSearchResult searchResult = _batchMaster.search(batchSearchRequest);
    assertTrue(searchResult.getDocuments().size() > 0);
  }

  @Test
  public void testSearchBatchByViewDefinitionNoResults() {
    UniqueId nonExistentUid = UniqueId.of("ViewDef", "non_existent_view_definition_uid");
    BatchSearchRequest batchSearchRequest = new BatchSearchRequest();
    batchSearchRequest.setViewDefinitionUid(nonExistentUid);
    BatchSearchResult searchResult = _batchMaster.search(batchSearchRequest);
    assertTrue(searchResult.getDocuments().size() == 0);
  }
}
