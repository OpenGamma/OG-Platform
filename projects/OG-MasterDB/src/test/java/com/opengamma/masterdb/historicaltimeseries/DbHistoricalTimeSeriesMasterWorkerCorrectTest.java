/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.historicaltimeseries;

import static org.testng.AssertJUnit.assertEquals;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.threeten.bp.Instant;
import org.threeten.bp.LocalDate;

import com.opengamma.DataNotFoundException;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundleWithDates;
import com.opengamma.id.ExternalIdWithDates;
import com.opengamma.id.UniqueId;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoDocument;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoHistoryRequest;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoHistoryResult;
import com.opengamma.master.historicaltimeseries.ManageableHistoricalTimeSeriesInfo;
import com.opengamma.util.test.DbTest;
import com.opengamma.util.test.TestGroup;

/**
 * Tests DbHistoricalTimeSeriesMaster.
 */
@Test(groups = TestGroup.UNIT_DB)
public class DbHistoricalTimeSeriesMasterWorkerCorrectTest extends AbstractDbHistoricalTimeSeriesMasterWorkerTest {
  // superclass sets up dummy database

  private static final Logger s_logger = LoggerFactory.getLogger(DbHistoricalTimeSeriesMasterWorkerCorrectTest.class);

  @Factory(dataProvider = "databases", dataProviderClass = DbTest.class)
  public DbHistoricalTimeSeriesMasterWorkerCorrectTest(String databaseType, String databaseVersion) {
    super(databaseType, databaseVersion);
    s_logger.info("running testcases for {}", databaseType);
  }

  //-------------------------------------------------------------------------
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_correct_nullDocument() {
    _htsMaster.correct(null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_correct_noHistoricalTimeSeriesId() {
    ManageableHistoricalTimeSeriesInfo info = new ManageableHistoricalTimeSeriesInfo();
    info.setName("Corrected");
    info.setDataField("DF");
    info.setDataSource("DS");
    info.setDataProvider("DP");
    info.setObservationTime("OT");
    ExternalIdWithDates id = ExternalIdWithDates.of(ExternalId.of("A", "B"), LocalDate.of(2011, 6, 30), null);
    ExternalIdBundleWithDates bundle = ExternalIdBundleWithDates.of(id);
    info.setExternalIdBundle(bundle);
    HistoricalTimeSeriesInfoDocument doc = new HistoricalTimeSeriesInfoDocument(info);
    _htsMaster.correct(doc);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_correct_noHistoricalTimeSeries() {
    HistoricalTimeSeriesInfoDocument doc = new HistoricalTimeSeriesInfoDocument();
    doc.setUniqueId(UniqueId.of("DbHts", "101", "0"));
    _htsMaster.correct(doc);
  }

  @Test(expectedExceptions = DataNotFoundException.class)
  public void test_correct_notFound() {
    ManageableHistoricalTimeSeriesInfo info = new ManageableHistoricalTimeSeriesInfo();
    info.setUniqueId(UniqueId.of("DbHts", "0", "0"));
    info.setName("Corrected");
    info.setDataField("DF");
    info.setDataSource("DS");
    info.setDataProvider("DP");
    info.setObservationTime("OT");
    ExternalIdWithDates id = ExternalIdWithDates.of(ExternalId.of("A", "B"), LocalDate.of(2011, 6, 30), null);
    ExternalIdBundleWithDates bundle = ExternalIdBundleWithDates.of(id);
    info.setExternalIdBundle(bundle);
    HistoricalTimeSeriesInfoDocument doc = new HistoricalTimeSeriesInfoDocument(info);
    _htsMaster.correct(doc);
  }

  @Test
  public void test_correct_getUpdateGet() {
    Instant now = Instant.now(_htsMaster.getClock());
    
    HistoricalTimeSeriesInfoDocument base = _htsMaster.get(UniqueId.of("DbHts", "101", "0"));
    ManageableHistoricalTimeSeriesInfo info = new ManageableHistoricalTimeSeriesInfo();
    info.setUniqueId(UniqueId.of("DbHts", "101", "0"));
    info.setName("Corrected");
    info.setDataField("DF");
    info.setDataSource("DS");
    info.setDataProvider("DP");
    info.setObservationTime("OT");
    ExternalIdWithDates id = ExternalIdWithDates.of(ExternalId.of("A", "B"), LocalDate.of(2011, 6, 30), null);
    ExternalIdBundleWithDates bundle = ExternalIdBundleWithDates.of(id);
    info.setExternalIdBundle(bundle);
    HistoricalTimeSeriesInfoDocument input = new HistoricalTimeSeriesInfoDocument(info);
    
    HistoricalTimeSeriesInfoDocument corrected = _htsMaster.correct(input);
    assertEquals(false, base.getUniqueId().equals(corrected.getUniqueId()));
    assertEquals(base.getVersionFromInstant(), corrected.getVersionFromInstant());
    assertEquals(base.getVersionToInstant(), corrected.getVersionToInstant());
    assertEquals(now, corrected.getCorrectionFromInstant());
    assertEquals(null, corrected.getCorrectionToInstant());
    assertEquals(input.getInfo(), corrected.getInfo());
    
    HistoricalTimeSeriesInfoDocument old = _htsMaster.get(UniqueId.of("DbHts", "101", "0"));
    assertEquals(base.getUniqueId(), old.getUniqueId());
    assertEquals(base.getVersionFromInstant(), old.getVersionFromInstant());
    assertEquals(base.getVersionToInstant(), old.getVersionToInstant());
    assertEquals(base.getCorrectionFromInstant(), old.getCorrectionFromInstant());
    assertEquals(now, old.getCorrectionToInstant());  // old version ended
    assertEquals(base.getInfo(), old.getInfo());
    
    HistoricalTimeSeriesInfoHistoryRequest search = new HistoricalTimeSeriesInfoHistoryRequest(base.getUniqueId(), now, null);
    HistoricalTimeSeriesInfoHistoryResult searchResult = _htsMaster.history(search);
    assertEquals(2, searchResult.getDocuments().size());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_toString() {
    assertEquals(_htsMaster.getClass().getSimpleName() + "[DbHts]", _htsMaster.toString());
  }

}
