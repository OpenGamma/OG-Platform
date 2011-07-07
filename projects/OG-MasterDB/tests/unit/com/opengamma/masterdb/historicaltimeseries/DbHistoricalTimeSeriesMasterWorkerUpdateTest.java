/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.historicaltimeseries;

import static org.testng.AssertJUnit.assertEquals;

import java.util.TimeZone;

import javax.time.Instant;
import javax.time.calendar.LocalDate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.BadSqlGrammarException;
import org.testng.Assert;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import com.opengamma.DataNotFoundException;
import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundleWithDates;
import com.opengamma.id.IdentifierWithDates;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesDocument;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesHistoryRequest;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesHistoryResult;
import com.opengamma.master.historicaltimeseries.ManageableHistoricalTimeSeries;
import com.opengamma.util.test.DBTest;

/**
 * Tests DbHistoricalTimeSeriesMaster.
 */
public class DbHistoricalTimeSeriesMasterWorkerUpdateTest extends AbstractDbHistoricalTimeSeriesMasterWorkerTest {
  // superclass sets up dummy database

  private static final Logger s_logger = LoggerFactory.getLogger(DbHistoricalTimeSeriesMasterWorkerUpdateTest.class);

  @Factory(dataProvider = "databases", dataProviderClass = DBTest.class)
  public DbHistoricalTimeSeriesMasterWorkerUpdateTest(String databaseType, String databaseVersion) {
    super(databaseType, databaseVersion);
    s_logger.info("running testcases for {}", databaseType);
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
  }

  //-------------------------------------------------------------------------
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_update_nullDocument() {
    _htsMaster.update(null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_update_noHistoricalTimeSeriesId() {
    ManageableHistoricalTimeSeries hts = new ManageableHistoricalTimeSeries();
    hts.setName("Updated");
    hts.setDataField("DF");
    hts.setDataSource("DS");
    hts.setDataProvider("DP");
    hts.setObservationTime("OT");
    IdentifierWithDates id = IdentifierWithDates.of(Identifier.of("A", "B"), LocalDate.of(2011, 6, 30), null);
    IdentifierBundleWithDates bundle = IdentifierBundleWithDates.of(id);
    hts.setIdentifiers(bundle);
    HistoricalTimeSeriesDocument doc = new HistoricalTimeSeriesDocument(hts);
    _htsMaster.update(doc);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_update_noHistoricalTimeSeries() {
    HistoricalTimeSeriesDocument doc = new HistoricalTimeSeriesDocument();
    doc.setUniqueId(UniqueIdentifier.of("DbHts", "101", "0"));
    _htsMaster.update(doc);
  }

  @Test(expectedExceptions = DataNotFoundException.class)
  public void test_update_notFound() {
    ManageableHistoricalTimeSeries hts = new ManageableHistoricalTimeSeries();
    hts.setUniqueId(UniqueIdentifier.of("DbHts", "0", "0"));
    hts.setName("Updated");
    hts.setDataField("DF");
    hts.setDataSource("DS");
    hts.setDataProvider("DP");
    hts.setObservationTime("OT");
    IdentifierWithDates id = IdentifierWithDates.of(Identifier.of("A", "B"), LocalDate.of(2011, 6, 30), null);
    IdentifierBundleWithDates bundle = IdentifierBundleWithDates.of(id);
    hts.setIdentifiers(bundle);
    HistoricalTimeSeriesDocument doc = new HistoricalTimeSeriesDocument(hts);
    _htsMaster.update(doc);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_update_notLatestVersion() {
    ManageableHistoricalTimeSeries hts = new ManageableHistoricalTimeSeries();
    hts.setUniqueId(UniqueIdentifier.of("DbHts", "201", "0"));
    hts.setName("Updated");
    hts.setDataField("DF");
    hts.setDataSource("DS");
    hts.setDataProvider("DP");
    hts.setObservationTime("OT");
    IdentifierWithDates id = IdentifierWithDates.of(Identifier.of("A", "B"), LocalDate.of(2011, 6, 30), null);
    IdentifierBundleWithDates bundle = IdentifierBundleWithDates.of(id);
    hts.setIdentifiers(bundle);
    HistoricalTimeSeriesDocument doc = new HistoricalTimeSeriesDocument(hts);
    _htsMaster.update(doc);
  }

  @Test
  public void test_update_getUpdateGet() {
    Instant now = Instant.now(_htsMaster.getTimeSource());
    
    HistoricalTimeSeriesDocument base = _htsMaster.get(UniqueIdentifier.of("DbHts", "101", "0"));
    ManageableHistoricalTimeSeries hts = new ManageableHistoricalTimeSeries();
    hts.setUniqueId(UniqueIdentifier.of("DbHts", "101", "0"));
    hts.setName("Updated");
    hts.setDataField("DF");
    hts.setDataSource("DS");
    hts.setDataProvider("DP");
    hts.setObservationTime("OT");
    IdentifierWithDates id = IdentifierWithDates.of(Identifier.of("A", "B"), LocalDate.of(2011, 6, 30), null);
    IdentifierBundleWithDates bundle = IdentifierBundleWithDates.of(id);
    hts.setIdentifiers(bundle);
    HistoricalTimeSeriesDocument input = new HistoricalTimeSeriesDocument(hts);
    
    HistoricalTimeSeriesDocument updated = _htsMaster.update(input);
    assertEquals(false, base.getUniqueId().equals(updated.getUniqueId()));
    assertEquals(now, updated.getVersionFromInstant());
    assertEquals(null, updated.getVersionToInstant());
    assertEquals(now, updated.getCorrectionFromInstant());
    assertEquals(null, updated.getCorrectionToInstant());
    assertEquals(input.getSeries(), updated.getSeries());
    
    HistoricalTimeSeriesDocument old = _htsMaster.get(UniqueIdentifier.of("DbHts", "101", "0"));
    assertEquals(base.getUniqueId(), old.getUniqueId());
    assertEquals(base.getVersionFromInstant(), old.getVersionFromInstant());
    assertEquals(now, old.getVersionToInstant());  // old version ended
    assertEquals(base.getCorrectionFromInstant(), old.getCorrectionFromInstant());
    assertEquals(base.getCorrectionToInstant(), old.getCorrectionToInstant());
    assertEquals(base.getSeries(), old.getSeries());
    
    HistoricalTimeSeriesHistoryRequest search = new HistoricalTimeSeriesHistoryRequest(base.getUniqueId(), null, now);
    HistoricalTimeSeriesHistoryResult searchResult = _htsMaster.history(search);
    assertEquals(2, searchResult.getDocuments().size());
  }

  @Test
  public void test_update_rollback() {
    DbHistoricalTimeSeriesMaster w = new DbHistoricalTimeSeriesMaster(_htsMaster.getDbSource()) {
      protected String sqlInsertIdKey() {
        return "INSERT";  // bad sql
      }
    };
    final HistoricalTimeSeriesDocument base = _htsMaster.get(UniqueIdentifier.of("DbHts", "101", "0"));
    ManageableHistoricalTimeSeries hts = new ManageableHistoricalTimeSeries();
    hts.setUniqueId(UniqueIdentifier.of("DbHts", "101", "0"));
    hts.setName("Updated");
    hts.setDataField("DF");
    hts.setDataSource("DS");
    hts.setDataProvider("DP");
    hts.setObservationTime("OT");
    IdentifierWithDates id = IdentifierWithDates.of(Identifier.of("A", "B"), LocalDate.of(2011, 6, 30), null);
    IdentifierBundleWithDates bundle = IdentifierBundleWithDates.of(id);
    hts.setIdentifiers(bundle);
    HistoricalTimeSeriesDocument input = new HistoricalTimeSeriesDocument(hts);
    try {
      w.update(input);
      Assert.fail();
    } catch (BadSqlGrammarException ex) {
      // expected
    }
    final HistoricalTimeSeriesDocument test = _htsMaster.get(UniqueIdentifier.of("DbHts", "101", "0"));
    
    assertEquals(base, test);
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_toString() {
    assertEquals(_htsMaster.getClass().getSimpleName() + "[DbHts]", _htsMaster.toString());
  }

}
