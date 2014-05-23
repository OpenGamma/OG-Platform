/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.historicaltimeseries;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.threeten.bp.Instant;
import org.threeten.bp.LocalDate;

import com.google.common.collect.Sets;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundleWithDates;
import com.opengamma.id.ExternalIdWithDates;
import com.opengamma.id.UniqueId;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoDocument;
import com.opengamma.master.historicaltimeseries.ManageableHistoricalTimeSeriesInfo;
import com.opengamma.util.test.DbTest;
import com.opengamma.util.test.TestGroup;

/**
 * Tests DbHistoricalTimeSeriesMaster.
 */
@Test(groups = TestGroup.UNIT_DB)
public class DbHistoricalTimeSeriesMasterWorkerAddTest extends AbstractDbHistoricalTimeSeriesMasterWorkerTest {
  // superclass sets up dummy database

  private static final Logger s_logger = LoggerFactory.getLogger(DbHistoricalTimeSeriesMasterWorkerAddTest.class);

  @Factory(dataProvider = "databases", dataProviderClass = DbTest.class)
  public DbHistoricalTimeSeriesMasterWorkerAddTest(String databaseType, String databaseVersion) {
    super(databaseType, databaseVersion);
    s_logger.info("running testcases for {}", databaseType);
  }

  //-------------------------------------------------------------------------
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_add_nullDocument() {
    _htsMaster.add(null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_add_noHistoricalTimeSeries() {
    HistoricalTimeSeriesInfoDocument doc = new HistoricalTimeSeriesInfoDocument();
    _htsMaster.add(doc);
  }

  @Test
  public void test_add_add() {
    Instant now = Instant.now(_htsMaster.getClock());
    
    ManageableHistoricalTimeSeriesInfo info = new ManageableHistoricalTimeSeriesInfo();
    info.setName("Added");
    info.setDataField("DF");
    info.setDataSource("DS");
    info.setDataProvider("DP");
    info.setObservationTime("OT");
    ExternalIdWithDates id = ExternalIdWithDates.of(ExternalId.of("A", "B"), LocalDate.of(2011, 6, 30), null);
    ExternalIdBundleWithDates bundle = ExternalIdBundleWithDates.of(id);
    info.setExternalIdBundle(bundle);
    HistoricalTimeSeriesInfoDocument doc = new HistoricalTimeSeriesInfoDocument(info);
    HistoricalTimeSeriesInfoDocument test = _htsMaster.add(doc);
    
    UniqueId uniqueId = test.getUniqueId();
    assertNotNull(uniqueId);
    assertEquals("DbHts", uniqueId.getScheme());
    assertTrue(uniqueId.isVersioned());
    assertTrue(Long.parseLong(uniqueId.getValue()) >= 1000);
    assertEquals("0", uniqueId.getVersion());
    assertEquals(now, test.getVersionFromInstant());
    assertEquals(null, test.getVersionToInstant());
    assertEquals(now, test.getCorrectionFromInstant());
    assertEquals(null, test.getCorrectionToInstant());
    ManageableHistoricalTimeSeriesInfo testInfo = test.getInfo();
    assertNotNull(testInfo);
    assertEquals(uniqueId, testInfo.getUniqueId());
    assertEquals("Added", testInfo.getName());
    assertEquals("DF", testInfo.getDataField());
    assertEquals("DS", testInfo.getDataSource());
    assertEquals("DP", testInfo.getDataProvider());
    assertEquals("OT", testInfo.getObservationTime());
    assertEquals(1, testInfo.getExternalIdBundle().size());
    assertTrue(testInfo.getExternalIdBundle().getExternalIds().contains(id));
  }

  @Test
  public void test_add_addWithPermission() {
    Instant now = Instant.now(_htsMaster.getClock());

    ManageableHistoricalTimeSeriesInfo info = new ManageableHistoricalTimeSeriesInfo();
    info.setName("Added");
    info.setDataField("DF");
    info.setDataSource("DS");
    info.setDataProvider("DP");
    info.setObservationTime("OT");
    ExternalIdWithDates id = ExternalIdWithDates.of(ExternalId.of("A", "B"), LocalDate.of(2011, 6, 30), null);
    ExternalIdBundleWithDates bundle = ExternalIdBundleWithDates.of(id);
    info.setExternalIdBundle(bundle);
    info.setRequiredPermissions(Sets.newHashSet("A", "B", "C"));
    HistoricalTimeSeriesInfoDocument doc = new HistoricalTimeSeriesInfoDocument(info);
    HistoricalTimeSeriesInfoDocument test = _htsMaster.add(doc);

    UniqueId uniqueId = test.getUniqueId();
    assertNotNull(uniqueId);
    assertEquals("DbHts", uniqueId.getScheme());
    assertTrue(uniqueId.isVersioned());
    assertTrue(Long.parseLong(uniqueId.getValue()) >= 1000);
    assertEquals("0", uniqueId.getVersion());
    assertEquals(now, test.getVersionFromInstant());
    assertEquals(null, test.getVersionToInstant());
    assertEquals(now, test.getCorrectionFromInstant());
    assertEquals(null, test.getCorrectionToInstant());
    ManageableHistoricalTimeSeriesInfo testInfo = test.getInfo();
    assertNotNull(testInfo);
    assertEquals(uniqueId, testInfo.getUniqueId());
    assertEquals("Added", testInfo.getName());
    assertEquals("DF", testInfo.getDataField());
    assertEquals("DS", testInfo.getDataSource());
    assertEquals("DP", testInfo.getDataProvider());
    assertEquals("OT", testInfo.getObservationTime());
    assertEquals(1, testInfo.getExternalIdBundle().size());
    assertTrue(testInfo.getExternalIdBundle().getExternalIds().contains(id));
    assertNotNull(testInfo.getRequiredPermissions());
    assertEquals(3, testInfo.getRequiredPermissions().size());
    assertTrue(testInfo.getRequiredPermissions().contains("A"));
    assertTrue(testInfo.getRequiredPermissions().contains("B"));
    assertTrue(testInfo.getRequiredPermissions().contains("C"));
  }

  @Test
  public void test_add_addThenGet() {
    ManageableHistoricalTimeSeriesInfo info = new ManageableHistoricalTimeSeriesInfo();
    info.setName("Added");
    info.setDataField("DF");
    info.setDataSource("DS");
    info.setDataProvider("DP");
    info.setObservationTime("OT");
    ExternalIdWithDates id = ExternalIdWithDates.of(ExternalId.of("A", "B"), LocalDate.of(2011, 6, 30), null);
    ExternalIdBundleWithDates bundle = ExternalIdBundleWithDates.of(id);
    info.setExternalIdBundle(bundle);
    HistoricalTimeSeriesInfoDocument doc = new HistoricalTimeSeriesInfoDocument(info);
    HistoricalTimeSeriesInfoDocument added = _htsMaster.add(doc);
    
    HistoricalTimeSeriesInfoDocument test = _htsMaster.get(added.getUniqueId());
    assertEquals(added, test);
  }

  @Test
  public void test_add_addThenGetWithPermission() {
    ManageableHistoricalTimeSeriesInfo info = new ManageableHistoricalTimeSeriesInfo();
    info.setName("Added");
    info.setDataField("DF");
    info.setDataSource("DS");
    info.setDataProvider("DP");
    info.setObservationTime("OT");
    ExternalIdWithDates id = ExternalIdWithDates.of(ExternalId.of("A", "B"), LocalDate.of(2011, 6, 30), null);
    ExternalIdBundleWithDates bundle = ExternalIdBundleWithDates.of(id);
    info.setExternalIdBundle(bundle);
    info.setRequiredPermissions(Sets.newHashSet("A", "B", "C"));
    HistoricalTimeSeriesInfoDocument doc = new HistoricalTimeSeriesInfoDocument(info);
    HistoricalTimeSeriesInfoDocument added = _htsMaster.add(doc);
    assertNotNull(added);
    assertNotNull(added.getValue());
    assertNotNull(added.getUniqueId());
    assertEquals("Added", added.getValue().getName());
    assertEquals("DF", added.getValue().getDataField());
    assertEquals("DS", added.getValue().getDataSource());
    assertEquals("DP", added.getValue().getDataProvider());
    assertEquals("OT", added.getValue().getObservationTime());
    assertEquals(bundle, added.getValue().getExternalIdBundle());
    Set<String> permissions = added.getValue().getRequiredPermissions();
    assertNotNull(permissions);
    assertEquals(3, permissions.size());
    assertTrue(permissions.contains("A"));
    assertTrue(permissions.contains("B"));
    assertTrue(permissions.contains("C"));

    HistoricalTimeSeriesInfoDocument test = _htsMaster.get(added.getUniqueId());
    assertEquals(added, test);
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_toString() {
    assertEquals(_htsMaster.getClass().getSimpleName() + "[DbHts]", _htsMaster.toString());
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_add_addWithMissingNameProperty() {
    ManageableHistoricalTimeSeriesInfo info = new ManageableHistoricalTimeSeriesInfo();
    info.setDataField("DF");
    info.setDataSource("DS");
    info.setDataProvider("DP");
    info.setObservationTime("OT");
    ExternalIdWithDates id = ExternalIdWithDates.of(ExternalId.of("A", "B"), LocalDate.of(2011, 6, 30), null);
    ExternalIdBundleWithDates bundle = ExternalIdBundleWithDates.of(id);
    info.setExternalIdBundle(bundle);
    HistoricalTimeSeriesInfoDocument doc = new HistoricalTimeSeriesInfoDocument(info);
    _htsMaster.add(doc);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_add_addWithMissingDataFieldProperty() {
    ManageableHistoricalTimeSeriesInfo info = new ManageableHistoricalTimeSeriesInfo();
    info.setName("Test");
    info.setDataSource("DS");
    info.setDataProvider("DP");
    info.setObservationTime("OT");
    ExternalIdWithDates id = ExternalIdWithDates.of(ExternalId.of("A", "B"), LocalDate.of(2011, 6, 30), null);
    ExternalIdBundleWithDates bundle = ExternalIdBundleWithDates.of(id);
    info.setExternalIdBundle(bundle);
    HistoricalTimeSeriesInfoDocument doc = new HistoricalTimeSeriesInfoDocument(info);
    _htsMaster.add(doc);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_add_addWithMissingDataSourceProperty() {
    ManageableHistoricalTimeSeriesInfo info = new ManageableHistoricalTimeSeriesInfo();
    info.setName("Test");
    info.setDataField("DF");
    info.setDataProvider("DP");
    info.setObservationTime("OT");
    ExternalIdWithDates id = ExternalIdWithDates.of(ExternalId.of("A", "B"), LocalDate.of(2011, 6, 30), null);
    ExternalIdBundleWithDates bundle = ExternalIdBundleWithDates.of(id);
    info.setExternalIdBundle(bundle);
    HistoricalTimeSeriesInfoDocument doc = new HistoricalTimeSeriesInfoDocument(info);
    _htsMaster.add(doc);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_add_addWithMissingDataProviderProperty() {
    ManageableHistoricalTimeSeriesInfo info = new ManageableHistoricalTimeSeriesInfo();
    info.setName("Test");
    info.setDataField("DF");
    info.setDataSource("DS");
    info.setObservationTime("OT");
    ExternalIdWithDates id = ExternalIdWithDates.of(ExternalId.of("A", "B"), LocalDate.of(2011, 6, 30), null);
    ExternalIdBundleWithDates bundle = ExternalIdBundleWithDates.of(id);
    info.setExternalIdBundle(bundle);
    HistoricalTimeSeriesInfoDocument doc = new HistoricalTimeSeriesInfoDocument(info);
    _htsMaster.add(doc);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_add_addWithMissingObservationTimeProperty() {
    ManageableHistoricalTimeSeriesInfo info = new ManageableHistoricalTimeSeriesInfo();
    info.setName("Test");
    info.setDataField("DF");
    info.setDataSource("DS");
    info.setDataProvider("DP");
    ExternalIdWithDates id = ExternalIdWithDates.of(ExternalId.of("A", "B"), LocalDate.of(2011, 6, 30), null);
    ExternalIdBundleWithDates bundle = ExternalIdBundleWithDates.of(id);
    info.setExternalIdBundle(bundle);
    HistoricalTimeSeriesInfoDocument doc = new HistoricalTimeSeriesInfoDocument(info);
    _htsMaster.add(doc);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_add_addWithMissingExternalIdBundleProperty() {
    ManageableHistoricalTimeSeriesInfo info = new ManageableHistoricalTimeSeriesInfo();
    info.setName("Test");
    info.setDataField("DF");
    info.setDataSource("DS");
    info.setDataProvider("DP");
    info.setObservationTime("OT");
    HistoricalTimeSeriesInfoDocument doc = new HistoricalTimeSeriesInfoDocument(info);
    _htsMaster.add(doc);
  }

  @Test
  public void test_add_addWithMinimalProperties() {
    ManageableHistoricalTimeSeriesInfo info = new ManageableHistoricalTimeSeriesInfo();
    info.setName("Test");
    info.setDataField("DF");
    info.setDataSource("DS");
    info.setDataProvider("DP");
    info.setObservationTime("OT");
    ExternalIdWithDates id = ExternalIdWithDates.of(ExternalId.of("A", "B"), LocalDate.of(2011, 6, 30), null);
    ExternalIdBundleWithDates bundle = ExternalIdBundleWithDates.of(id);
    info.setExternalIdBundle(bundle);
    HistoricalTimeSeriesInfoDocument doc = new HistoricalTimeSeriesInfoDocument(info);
    _htsMaster.add(doc);
  }

}
