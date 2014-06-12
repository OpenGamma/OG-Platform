/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.historicaltimeseries;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.BadSqlGrammarException;
import org.testng.Assert;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.threeten.bp.Instant;
import org.threeten.bp.LocalDate;

import com.google.common.collect.Sets;
import com.opengamma.DataNotFoundException;
import com.opengamma.elsql.ElSqlBundle;
import com.opengamma.elsql.ElSqlConfig;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundleWithDates;
import com.opengamma.id.ExternalIdWithDates;
import com.opengamma.id.UniqueId;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoDocument;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoHistoryRequest;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoHistoryResult;
import com.opengamma.master.historicaltimeseries.ManageableHistoricalTimeSeriesInfo;
import com.opengamma.util.OpenGammaClock;
import com.opengamma.util.test.DbTest;
import com.opengamma.util.test.TestGroup;

/**
 * Tests DbHistoricalTimeSeriesMaster.
 */
@Test(groups = TestGroup.UNIT_DB)
public class DbHistoricalTimeSeriesMasterWorkerUpdateTest extends AbstractDbHistoricalTimeSeriesMasterWorkerTest {
  // superclass sets up dummy database

  private static final Logger s_logger = LoggerFactory.getLogger(DbHistoricalTimeSeriesMasterWorkerUpdateTest.class);

  @Factory(dataProvider = "databases", dataProviderClass = DbTest.class)
  public DbHistoricalTimeSeriesMasterWorkerUpdateTest(String databaseType, String databaseVersion) {
    super(databaseType, databaseVersion);
    s_logger.info("running testcases for {}", databaseType);
  }

  //-------------------------------------------------------------------------
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_update_nullDocument() {
    _htsMaster.update(null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_update_noHistoricalTimeSeriesId() {
    ManageableHistoricalTimeSeriesInfo info = new ManageableHistoricalTimeSeriesInfo();
    info.setName("Updated");
    info.setDataField("DF");
    info.setDataSource("DS");
    info.setDataProvider("DP");
    info.setObservationTime("OT");
    ExternalIdWithDates id = ExternalIdWithDates.of(ExternalId.of("A", "B"), LocalDate.of(2011, 6, 30), null);
    ExternalIdBundleWithDates bundle = ExternalIdBundleWithDates.of(id);
    info.setExternalIdBundle(bundle);
    HistoricalTimeSeriesInfoDocument doc = new HistoricalTimeSeriesInfoDocument(info);
    _htsMaster.update(doc);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_update_noHistoricalTimeSeries() {
    HistoricalTimeSeriesInfoDocument doc = new HistoricalTimeSeriesInfoDocument();
    doc.setUniqueId(UniqueId.of("DbHts", "101", "0"));
    _htsMaster.update(doc);
  }

  @Test(expectedExceptions = DataNotFoundException.class)
  public void test_update_notFound() {
    ManageableHistoricalTimeSeriesInfo info = new ManageableHistoricalTimeSeriesInfo();
    info.setUniqueId(UniqueId.of("DbHts", "0", "0"));
    info.setName("Updated");
    info.setDataField("DF");
    info.setDataSource("DS");
    info.setDataProvider("DP");
    info.setObservationTime("OT");
    ExternalIdWithDates id = ExternalIdWithDates.of(ExternalId.of("A", "B"), LocalDate.of(2011, 6, 30), null);
    ExternalIdBundleWithDates bundle = ExternalIdBundleWithDates.of(id);
    info.setExternalIdBundle(bundle);
    HistoricalTimeSeriesInfoDocument doc = new HistoricalTimeSeriesInfoDocument(info);
    _htsMaster.update(doc);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_update_notLatestVersion() {
    ManageableHistoricalTimeSeriesInfo info = new ManageableHistoricalTimeSeriesInfo();
    info.setUniqueId(UniqueId.of("DbHts", "201", "0"));
    info.setName("Updated");
    info.setDataField("DF");
    info.setDataSource("DS");
    info.setDataProvider("DP");
    info.setObservationTime("OT");
    ExternalIdWithDates id = ExternalIdWithDates.of(ExternalId.of("A", "B"), LocalDate.of(2011, 6, 30), null);
    ExternalIdBundleWithDates bundle = ExternalIdBundleWithDates.of(id);
    info.setExternalIdBundle(bundle);
    HistoricalTimeSeriesInfoDocument doc = new HistoricalTimeSeriesInfoDocument(info);
    _htsMaster.update(doc);
  }

  @Test
  public void test_update_getUpdateGet() {
    Instant now = Instant.now(_htsMaster.getClock());
    
    HistoricalTimeSeriesInfoDocument base = _htsMaster.get(UniqueId.of("DbHts", "101", "0"));
    ManageableHistoricalTimeSeriesInfo info = new ManageableHistoricalTimeSeriesInfo();
    info.setUniqueId(UniqueId.of("DbHts", "101", "0"));
    info.setName("Updated");
    info.setDataField("DF");
    info.setDataSource("DS");
    info.setDataProvider("DP");
    info.setObservationTime("OT");
    ExternalIdWithDates id = ExternalIdWithDates.of(ExternalId.of("A", "B"), LocalDate.of(2011, 6, 30), null);
    ExternalIdBundleWithDates bundle = ExternalIdBundleWithDates.of(id);
    info.setExternalIdBundle(bundle);
    HistoricalTimeSeriesInfoDocument input = new HistoricalTimeSeriesInfoDocument(info);
    
    HistoricalTimeSeriesInfoDocument updated = _htsMaster.update(input);
    assertEquals(false, base.getUniqueId().equals(updated.getUniqueId()));
    assertEquals(now, updated.getVersionFromInstant());
    assertEquals(null, updated.getVersionToInstant());
    assertEquals(now, updated.getCorrectionFromInstant());
    assertEquals(null, updated.getCorrectionToInstant());
    assertEquals(input.getInfo(), updated.getInfo());
    
    HistoricalTimeSeriesInfoDocument old = _htsMaster.get(UniqueId.of("DbHts", "101", "0"));
    assertEquals(base.getUniqueId(), old.getUniqueId());
    assertEquals(base.getVersionFromInstant(), old.getVersionFromInstant());
    assertEquals(now, old.getVersionToInstant());  // old version ended
    assertEquals(base.getCorrectionFromInstant(), old.getCorrectionFromInstant());
    assertEquals(base.getCorrectionToInstant(), old.getCorrectionToInstant());
    assertEquals(base.getInfo(), old.getInfo());
    
    HistoricalTimeSeriesInfoHistoryRequest search = new HistoricalTimeSeriesInfoHistoryRequest(base.getUniqueId(), null, now);
    HistoricalTimeSeriesInfoHistoryResult searchResult = _htsMaster.history(search);
    assertEquals(2, searchResult.getDocuments().size());
  }

  @Test
  public void test_update_Permission() throws Exception {
    _htsMaster.setClock(OpenGammaClock.getInstance());

    UniqueId baseUniqueId = UniqueId.of("DbHts", "101", "0");
    HistoricalTimeSeriesInfoDocument baseDoc = _htsMaster.get(baseUniqueId);
    assertEquals(baseUniqueId, baseDoc.getUniqueId());
    ManageableHistoricalTimeSeriesInfo baseInfo = baseDoc.getValue();
    assertEquals(baseUniqueId, baseInfo.getUniqueId());
    assertTrue(baseInfo.getRequiredPermissions().isEmpty());

    ManageableHistoricalTimeSeriesInfo input = baseInfo.clone();
    input.setName("A1");
    input.setRequiredPermissions(Sets.newHashSet("A"));

    Thread.sleep(100);
    HistoricalTimeSeriesInfoDocument updated = _htsMaster.update(new HistoricalTimeSeriesInfoDocument(input));
    ManageableHistoricalTimeSeriesInfo updatedInfo = updated.getValue();
    assertFalse(baseDoc.getUniqueId().equals(updated.getUniqueId()));
    assertEquals(baseInfo.getDataField(), updatedInfo.getDataField());
    assertEquals(baseInfo.getDataProvider(), updatedInfo.getDataProvider());
    assertEquals(baseInfo.getDataSource(), updatedInfo.getDataSource());
    assertEquals(baseInfo.getExternalIdBundle(), updatedInfo.getExternalIdBundle());
    assertEquals("A1", updatedInfo.getName());
    assertEquals(baseInfo.getObservationTime(), updatedInfo.getObservationTime());
    assertEquals(baseInfo.getTimeSeriesObjectId(), updatedInfo.getTimeSeriesObjectId());
    assertNotNull(updatedInfo.getRequiredPermissions());
    assertEquals(1, updatedInfo.getRequiredPermissions().size());
    assertTrue(updatedInfo.getRequiredPermissions().contains("A"));

    assertEquals(updated, _htsMaster.get(updated.getUniqueId()));

    input = updatedInfo.clone();
    input.setName("A2");
    input.setRequiredPermissions(Sets.newHashSet("A", "B"));
    Thread.sleep(100);
    updated = _htsMaster.update(new HistoricalTimeSeriesInfoDocument(input));
    updatedInfo = updated.getValue();
    assertFalse(baseDoc.getUniqueId().equals(updated.getUniqueId()));
    assertEquals(baseInfo.getDataField(), updatedInfo.getDataField());
    assertEquals(baseInfo.getDataProvider(), updatedInfo.getDataProvider());
    assertEquals(baseInfo.getDataSource(), updatedInfo.getDataSource());
    assertEquals(baseInfo.getExternalIdBundle(), updatedInfo.getExternalIdBundle());
    assertEquals("A2", updatedInfo.getName());
    assertEquals(baseInfo.getObservationTime(), updatedInfo.getObservationTime());
    assertEquals(baseInfo.getTimeSeriesObjectId(), updatedInfo.getTimeSeriesObjectId());
    assertNotNull(updatedInfo.getRequiredPermissions());
    assertEquals(2, updatedInfo.getRequiredPermissions().size());
    assertTrue(updatedInfo.getRequiredPermissions().contains("A"));
    assertTrue(updatedInfo.getRequiredPermissions().contains("B"));

    assertEquals(updated, _htsMaster.get(updated.getUniqueId()));

    HistoricalTimeSeriesInfoHistoryRequest search = new HistoricalTimeSeriesInfoHistoryRequest(baseDoc.getUniqueId(), null, Instant.now(_htsMaster.getClock()));
    HistoricalTimeSeriesInfoHistoryResult searchResult = _htsMaster.history(search);
    assertEquals(3, searchResult.getDocuments().size());
  }

  @Test
  public void test_update_rollback() {
    DbHistoricalTimeSeriesMaster w = new DbHistoricalTimeSeriesMaster(_htsMaster.getDbConnector());
    w.setElSqlBundle(ElSqlBundle.of(new ElSqlConfig("TestRollback"), DbHistoricalTimeSeriesMaster.class));
    final HistoricalTimeSeriesInfoDocument base = _htsMaster.get(UniqueId.of("DbHts", "101", "0"));
    ManageableHistoricalTimeSeriesInfo info = new ManageableHistoricalTimeSeriesInfo();
    info.setUniqueId(UniqueId.of("DbHts", "101", "0"));
    info.setName("Updated");
    info.setDataField("DF");
    info.setDataSource("DS");
    info.setDataProvider("DP");
    info.setObservationTime("OT");
    ExternalIdWithDates id = ExternalIdWithDates.of(ExternalId.of("A", "B"), LocalDate.of(2011, 6, 30), null);
    ExternalIdBundleWithDates bundle = ExternalIdBundleWithDates.of(id);
    info.setExternalIdBundle(bundle);
    HistoricalTimeSeriesInfoDocument input = new HistoricalTimeSeriesInfoDocument(info);
    try {
      w.update(input);
      Assert.fail();
    } catch (BadSqlGrammarException ex) {
      // expected
    }
    final HistoricalTimeSeriesInfoDocument test = _htsMaster.get(UniqueId.of("DbHts", "101", "0"));
    
    assertEquals(base, test);
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_toString() {
    assertEquals(_htsMaster.getClass().getSimpleName() + "[DbHts]", _htsMaster.toString());
  }

}
