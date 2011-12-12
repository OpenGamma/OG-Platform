/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.historicaltimeseries.impl;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertSame;

import java.util.List;

import javax.time.calendar.LocalDate;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.common.base.Supplier;
import com.opengamma.DataNotFoundException;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ExternalIdBundleWithDates;
import com.opengamma.id.ObjectId;
import com.opengamma.id.ObjectIdSupplier;
import com.opengamma.id.UniqueId;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesGetFilter;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoDocument;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoSearchRequest;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoSearchResult;
import com.opengamma.master.historicaltimeseries.ManageableHistoricalTimeSeries;
import com.opengamma.master.historicaltimeseries.ManageableHistoricalTimeSeriesInfo;
import com.opengamma.util.timeseries.localdate.ArrayLocalDateDoubleTimeSeries;
import com.opengamma.util.timeseries.localdate.LocalDateDoubleTimeSeries;

/**
 * Test {@link InMemoryHistoricalTimeSeriesMaster}.
 */
@Test
public class InMemoryHistoricalTimeSeriesMasterTest {

  // TODO Move the logical tests from here to the generic SecurityMasterTestCase then we can just extend from that

  private static final UniqueId OTHER_UID = UniqueId.of("U", "1");
  private static final ExternalId ID1 = ExternalId.of("A", "B");
  private static final ExternalId ID2 = ExternalId.of("A", "C");
  private static final ExternalIdBundle BUNDLE1 = ExternalIdBundle.of(ID1);
  private static final ExternalIdBundle BUNDLE2 = ExternalIdBundle.of(ID2);

  private InMemoryHistoricalTimeSeriesMaster testEmpty;
  private InMemoryHistoricalTimeSeriesMaster testPopulated;
  private HistoricalTimeSeriesInfoDocument doc1;
  private HistoricalTimeSeriesInfoDocument doc2;
  private ManageableHistoricalTimeSeriesInfo info1;
  private ManageableHistoricalTimeSeriesInfo info2;

  @BeforeMethod
  public void setUp() {
    testEmpty = new InMemoryHistoricalTimeSeriesMaster(new ObjectIdSupplier("Test"));
    testPopulated = new InMemoryHistoricalTimeSeriesMaster(new ObjectIdSupplier("Test"));
    info1 = new ManageableHistoricalTimeSeriesInfo();
    info1.setName("Name1");
    info1.setDataField("DF1");
    info1.setDataSource("DS1");
    info1.setDataProvider("DP1");
    info1.setObservationTime("OT1");
    info1.setExternalIdBundle(ExternalIdBundleWithDates.of(BUNDLE1));
    doc1 = new HistoricalTimeSeriesInfoDocument();
    doc1.setInfo(info1);
    doc1 = testPopulated.add(doc1);
    info2 = new ManageableHistoricalTimeSeriesInfo();
    info2.setName("Name2");
    info2.setDataField("DF2");
    info2.setDataSource("DS2");
    info2.setDataProvider("DP2");
    info2.setObservationTime("OT2");
    info2.setExternalIdBundle(ExternalIdBundleWithDates.of(BUNDLE2));
    doc2 = new HistoricalTimeSeriesInfoDocument();
    doc2.setInfo(info2);
    doc2 = testPopulated.add(doc2);
  }

  //-------------------------------------------------------------------------
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_constructor_nullSupplier() {
    new InMemoryHistoricalTimeSeriesMaster((Supplier<ObjectId>) null);
  }

  public void test_defaultSupplier() {
    InMemoryHistoricalTimeSeriesMaster master = new InMemoryHistoricalTimeSeriesMaster();
    HistoricalTimeSeriesInfoDocument doc = new HistoricalTimeSeriesInfoDocument();
    doc.setInfo(info1);
    HistoricalTimeSeriesInfoDocument added = master.add(doc);
    assertEquals("MemHts", added.getUniqueId().getScheme());
  }

  public void test_alternateSupplier() {
    InMemoryHistoricalTimeSeriesMaster master = new InMemoryHistoricalTimeSeriesMaster(new ObjectIdSupplier("Hello"));
    HistoricalTimeSeriesInfoDocument doc = new HistoricalTimeSeriesInfoDocument();
    doc.setInfo(info1);
    HistoricalTimeSeriesInfoDocument added = master.add(doc);
    assertEquals("Hello", added.getUniqueId().getScheme());
  }

  //-------------------------------------------------------------------------
  public void test_search_emptyMaster() {
    HistoricalTimeSeriesInfoSearchRequest request = new HistoricalTimeSeriesInfoSearchRequest();
    HistoricalTimeSeriesInfoSearchResult result = testEmpty.search(request);
    assertEquals(0, result.getPaging().getTotalItems());
    assertEquals(0, result.getDocuments().size());
  }

  public void test_search_populatedMaster_all() {
    HistoricalTimeSeriesInfoSearchRequest request = new HistoricalTimeSeriesInfoSearchRequest();
    HistoricalTimeSeriesInfoSearchResult result = testPopulated.search(request);
    assertEquals(2, result.getPaging().getTotalItems());
    List<HistoricalTimeSeriesInfoDocument> docs = result.getDocuments();
    assertEquals(2, docs.size());
    assertEquals(true, docs.contains(doc1));
    assertEquals(true, docs.contains(doc2));
  }

  public void test_search_populatedMaster_filterByBundle() {
    HistoricalTimeSeriesInfoSearchRequest request = new HistoricalTimeSeriesInfoSearchRequest(BUNDLE1);
    HistoricalTimeSeriesInfoSearchResult result = testPopulated.search(request);
    assertEquals(1, result.getPaging().getTotalItems());
    assertEquals(1, result.getDocuments().size());
    assertEquals(true, result.getDocuments().contains(doc1));
  }

  public void test_search_populatedMaster_filterByBundle_both() {
    HistoricalTimeSeriesInfoSearchRequest request = new HistoricalTimeSeriesInfoSearchRequest();
    request.addExternalIds(BUNDLE1);
    request.addExternalIds(BUNDLE2);
    HistoricalTimeSeriesInfoSearchResult result = testPopulated.search(request);
    assertEquals(2, result.getPaging().getTotalItems());
    List<HistoricalTimeSeriesInfoDocument> docs = result.getDocuments();
    assertEquals(2, docs.size());
    assertEquals(true, docs.contains(doc1));
    assertEquals(true, docs.contains(doc2));
  }

  public void test_search_popluatedMaster_filterByExternalIdValue() {
    HistoricalTimeSeriesInfoSearchRequest request = new HistoricalTimeSeriesInfoSearchRequest();
    request.setExternalIdValue("B");
    HistoricalTimeSeriesInfoSearchResult result = testPopulated.search(request);
    assertEquals(1, result.getPaging().getTotalItems());
    List<HistoricalTimeSeriesInfoDocument> docs = result.getDocuments();
    assertEquals(1, docs.size());
    assertEquals(true, docs.contains(doc1));
  }

  public void test_search_popluatedMaster_filterByExternalIdValue_case() {
    HistoricalTimeSeriesInfoSearchRequest request = new HistoricalTimeSeriesInfoSearchRequest();
    request.setExternalIdValue("b");
    HistoricalTimeSeriesInfoSearchResult result = testPopulated.search(request);
    assertEquals(1, result.getPaging().getTotalItems());
    List<HistoricalTimeSeriesInfoDocument> docs = result.getDocuments();
    assertEquals(1, docs.size());
    assertEquals(true, docs.contains(doc1));
  }

  public void test_search_populatedMaster_filterByName() {
    HistoricalTimeSeriesInfoSearchRequest request = new HistoricalTimeSeriesInfoSearchRequest();
    request.setName("*ame2");
    HistoricalTimeSeriesInfoSearchResult result = testPopulated.search(request);
    assertEquals(1, result.getPaging().getTotalItems());
    List<HistoricalTimeSeriesInfoDocument> docs = result.getDocuments();
    assertEquals(1, docs.size());
    assertEquals(true, docs.contains(doc2));
  }

  public void test_search_populatedMaster_filterByDataField() {
    HistoricalTimeSeriesInfoSearchRequest request = new HistoricalTimeSeriesInfoSearchRequest();
    request.setDataField("DF2");
    HistoricalTimeSeriesInfoSearchResult result = testPopulated.search(request);
    assertEquals(1, result.getPaging().getTotalItems());
    List<HistoricalTimeSeriesInfoDocument> docs = result.getDocuments();
    assertEquals(1, docs.size());
    assertEquals(true, docs.contains(doc2));
  }

  public void test_search_populatedMaster_filterByDataSource() {
    HistoricalTimeSeriesInfoSearchRequest request = new HistoricalTimeSeriesInfoSearchRequest();
    request.setDataSource("DS2");
    HistoricalTimeSeriesInfoSearchResult result = testPopulated.search(request);
    assertEquals(1, result.getPaging().getTotalItems());
    List<HistoricalTimeSeriesInfoDocument> docs = result.getDocuments();
    assertEquals(1, docs.size());
    assertEquals(true, docs.contains(doc2));
  }

  public void test_search_populatedMaster_filterByDataProvider() {
    HistoricalTimeSeriesInfoSearchRequest request = new HistoricalTimeSeriesInfoSearchRequest();
    request.setDataProvider("DP2");
    HistoricalTimeSeriesInfoSearchResult result = testPopulated.search(request);
    assertEquals(1, result.getPaging().getTotalItems());
    List<HistoricalTimeSeriesInfoDocument> docs = result.getDocuments();
    assertEquals(1, docs.size());
    assertEquals(true, docs.contains(doc2));
  }

  public void test_search_populatedMaster_filterByObservationTime() {
    HistoricalTimeSeriesInfoSearchRequest request = new HistoricalTimeSeriesInfoSearchRequest();
    request.setObservationTime("OT2");
    HistoricalTimeSeriesInfoSearchResult result = testPopulated.search(request);
    assertEquals(1, result.getPaging().getTotalItems());
    List<HistoricalTimeSeriesInfoDocument> docs = result.getDocuments();
    assertEquals(1, docs.size());
    assertEquals(true, docs.contains(doc2));
  }

  //-------------------------------------------------------------------------
  @Test(expectedExceptions = DataNotFoundException.class)
  public void test_get_emptyMaster() {
    assertNull(testEmpty.get(OTHER_UID));
  }

  public void test_get_populatedMaster() {
    assertSame(doc1, testPopulated.get(doc1.getUniqueId()));
    assertSame(doc2, testPopulated.get(doc2.getUniqueId()));
  }

  //-------------------------------------------------------------------------
  public void test_add_emptyMaster() {
    HistoricalTimeSeriesInfoDocument doc = new HistoricalTimeSeriesInfoDocument();
    doc.setInfo(info1);
    HistoricalTimeSeriesInfoDocument added = testEmpty.add(doc);
    assertNotNull(added.getVersionFromInstant());
    assertNotNull(added.getCorrectionFromInstant());
    assertEquals(added.getVersionFromInstant(), added.getCorrectionFromInstant());
    assertEquals("Test", added.getUniqueId().getScheme());
    added.setUniqueId(null);
    added.getInfo().setTimeSeriesObjectId(null);
    assertEquals(info1, added.getInfo());
  }

  //-------------------------------------------------------------------------
  @Test(expectedExceptions = DataNotFoundException.class)
  public void test_update_emptyMaster() {
    HistoricalTimeSeriesInfoDocument doc = new HistoricalTimeSeriesInfoDocument();
    doc.setInfo(info1);
    doc.setUniqueId(OTHER_UID);
    testEmpty.update(doc);
  }

  public void test_update_populatedMaster() {
    HistoricalTimeSeriesInfoDocument doc = new HistoricalTimeSeriesInfoDocument();
    doc.setInfo(info1);
    doc.setUniqueId(doc1.getUniqueId());
    HistoricalTimeSeriesInfoDocument updated = testPopulated.update(doc);
    assertEquals(doc1.getUniqueId(), updated.getUniqueId());
    assertNotNull(doc1.getVersionFromInstant());
    assertNotNull(updated.getVersionFromInstant());
  }

  //-------------------------------------------------------------------------
  @Test(expectedExceptions = DataNotFoundException.class)
  public void test_remove_emptyMaster() {
    testEmpty.remove(OTHER_UID);
  }

  public void test_remove_populatedMaster() {
    testPopulated.remove(doc1.getUniqueId());
    HistoricalTimeSeriesInfoSearchRequest request = new HistoricalTimeSeriesInfoSearchRequest();
    HistoricalTimeSeriesInfoSearchResult result = testPopulated.search(request);
    assertEquals(1, result.getPaging().getTotalItems());
    List<HistoricalTimeSeriesInfoDocument> docs = result.getDocuments();
    assertEquals(1, docs.size());
    assertEquals(true, docs.contains(doc2));
  }

  //-------------------------------------------------------------------------
  @Test(expectedExceptions = DataNotFoundException.class)
  public void test_getTS_UID_otherId() {
    testEmpty.getTimeSeries(OTHER_UID);
  }

  public void test_points_update_correct() {
    LocalDate[] dates = {LocalDate.of(2011, 1, 1), LocalDate.of(2011, 1, 2)};
    double[] values = {1.1d, 2.2d};
    LocalDateDoubleTimeSeries input = new ArrayLocalDateDoubleTimeSeries(dates, values);
    
    UniqueId uniqueId = testPopulated.updateTimeSeriesDataPoints(doc1.getUniqueId(), input);
    assertEquals(doc1.getUniqueId().getObjectId(), uniqueId.getObjectId());
    
    ManageableHistoricalTimeSeries test = testPopulated.getTimeSeries(uniqueId);
    assertEquals(uniqueId, test.getUniqueId());
    assertEquals(input, test.getTimeSeries());
    
    LocalDate[] dates2 = {LocalDate.of(2011, 1, 1), LocalDate.of(2011, 1, 3)};
    double[] values2 = {1.5d, 2.5d};
    LocalDateDoubleTimeSeries input2 = new ArrayLocalDateDoubleTimeSeries(dates2, values2);
    
    UniqueId uniqueId2 = testPopulated.correctTimeSeriesDataPoints(doc1.getUniqueId(), input2);
    assertEquals(doc1.getUniqueId().getObjectId(), uniqueId2.getObjectId());
    
    LocalDate[] expectedDates = {LocalDate.of(2011, 1, 1), LocalDate.of(2011, 1, 2), LocalDate.of(2011, 1, 3)};
    double[] expectedValues = {1.5d, 2.2d, 2.5d};
    LocalDateDoubleTimeSeries expected = new ArrayLocalDateDoubleTimeSeries(expectedDates, expectedValues);
    ManageableHistoricalTimeSeries test2 = testPopulated.getTimeSeries(uniqueId);
    assertEquals(uniqueId, test2.getUniqueId());
    assertEquals(expected, test2.getTimeSeries());
  }

  public void test_points_update_remove() {
    LocalDate[] dates = {LocalDate.of(2011, 1, 1), LocalDate.of(2011, 1, 2)};
    double[] values = {1.1d, 2.2d};
    LocalDateDoubleTimeSeries input = new ArrayLocalDateDoubleTimeSeries(dates, values);
    
    UniqueId uniqueId = testPopulated.updateTimeSeriesDataPoints(doc1.getUniqueId(), input);
    assertEquals(doc1.getUniqueId().getObjectId(), uniqueId.getObjectId());
    
    ManageableHistoricalTimeSeries test = testPopulated.getTimeSeries(uniqueId);
    assertEquals(uniqueId, test.getUniqueId());
    assertEquals(input, test.getTimeSeries());
    
    UniqueId uniqueId2 = testPopulated.removeTimeSeriesDataPoints(doc1.getUniqueId(), LocalDate.of(2011, 1, 2), null);
    assertEquals(doc1.getUniqueId().getObjectId(), uniqueId2.getObjectId());
    
    LocalDate[] expectedDates = {LocalDate.of(2011, 1, 1)};
    double[] expectedValues = {1.1d};
    LocalDateDoubleTimeSeries expected = new ArrayLocalDateDoubleTimeSeries(expectedDates, expectedValues);
    ManageableHistoricalTimeSeries test2 = testPopulated.getTimeSeries(uniqueId);
    assertEquals(uniqueId, test2.getUniqueId());
    assertEquals(expected, test2.getTimeSeries());
  }

  public void test_points_getFilter() {
    
    // Set up HTS for comparison purposes
    LocalDate[] dates = {
        LocalDate.of(2011, 1, 1), 
        LocalDate.of(2011, 1, 2), 
        LocalDate.of(2011, 1, 3), 
        LocalDate.of(2011, 1, 5), 
        LocalDate.of(2011, 1, 6), 
        LocalDate.of(2011, 1, 8)
    };
    double[] values = {
        1.0d, 
        1.1d, 
        1.3d, 
        1.2d, 
        2.2d, 
        2.0d
    }; 
    LocalDateDoubleTimeSeries input = new ArrayLocalDateDoubleTimeSeries(dates, values);
    testPopulated.updateTimeSeriesDataPoints(doc1.getUniqueId(), input);
    ManageableHistoricalTimeSeries reference = testPopulated.getTimeSeries(doc1.getUniqueId());
    
    // Get entire series using blank filter
    HistoricalTimeSeriesGetFilter filter = new HistoricalTimeSeriesGetFilter();
    ManageableHistoricalTimeSeries test = testPopulated.getTimeSeries(doc1.getUniqueId(), filter);
    assertEquals(reference.getTimeSeries(), test.getTimeSeries());
    assertEquals(input, test.getTimeSeries());

    // Get filtered by time, open-ended end
    filter.setEarliestDate(reference.getTimeSeries().getTimeAt(1)); // exclude first point
    test = testPopulated.getTimeSeries(doc1.getUniqueId(), filter);
    assertEquals(reference.getTimeSeries().size() - 1, test.getTimeSeries().size());
    assertEquals(reference.getTimeSeries().getValueAt(1), test.getTimeSeries().getEarliestValue());
    assertEquals(reference.getTimeSeries().getTimeAt(1), test.getTimeSeries().getEarliestTime());
    assertEquals(reference.getTimeSeries().getValueAt(reference.getTimeSeries().size() - 1), test.getTimeSeries().getLatestValue());
    assertEquals(reference.getTimeSeries().getTimeAt(reference.getTimeSeries().size() - 1), test.getTimeSeries().getLatestTime());    

    // Get filtered by time, closed at both ends
    filter.setLatestDate(reference.getTimeSeries().getTimeAt(reference.getTimeSeries().size() - 2)); // exclude last point
    test = testPopulated.getTimeSeries(doc1.getUniqueId(), filter);
    assertEquals(reference.getTimeSeries().size() - 2, test.getTimeSeries().size());
    assertEquals(reference.getTimeSeries().getValueAt(1), test.getTimeSeries().getEarliestValue());
    assertEquals(reference.getTimeSeries().getTimeAt(1), test.getTimeSeries().getEarliestTime());
    assertEquals(reference.getTimeSeries().getValueAt(reference.getTimeSeries().size() - 2), test.getTimeSeries().getLatestValue());
    assertEquals(reference.getTimeSeries().getTimeAt(reference.getTimeSeries().size() - 2), test.getTimeSeries().getLatestTime());
    
    // Get filtered by time, open-ended start
    filter.setEarliestDate(null);
    test = testPopulated.getTimeSeries(doc1.getUniqueId(), filter);
    assertEquals(reference.getTimeSeries().size() - 1, test.getTimeSeries().size());
    assertEquals(reference.getTimeSeries().getValueAt(0), test.getTimeSeries().getEarliestValue());
    assertEquals(reference.getTimeSeries().getTimeAt(0), test.getTimeSeries().getEarliestTime());
    assertEquals(reference.getTimeSeries().getValueAt(reference.getTimeSeries().size() - 2), test.getTimeSeries().getLatestValue());
    assertEquals(reference.getTimeSeries().getTimeAt(reference.getTimeSeries().size() - 2), test.getTimeSeries().getLatestTime());

    // Get filtered by +ve maxPoints, open-ended start
    filter.setMaxPoints(2); // get earliest two points
    test = testPopulated.getTimeSeries(doc1.getUniqueId(), filter);
    assertEquals(2, test.getTimeSeries().size());
    assertEquals(reference.getTimeSeries().getTimeAt(0), test.getTimeSeries().getTimeAt(0));
    assertEquals(reference.getTimeSeries().getValueAt(0), test.getTimeSeries().getValueAt(0));
    assertEquals(reference.getTimeSeries().getTimeAt(1), test.getTimeSeries().getTimeAt(1));
    assertEquals(reference.getTimeSeries().getValueAt(1), test.getTimeSeries().getValueAt(1));
    
    // Get filtered by +ve maxPoints, closed date range
    filter.setEarliestDate(reference.getTimeSeries().getTimeAt(1)); // exclude first point
    test = testPopulated.getTimeSeries(doc1.getUniqueId(), filter);
    assertEquals(2, test.getTimeSeries().size());
    assertEquals(reference.getTimeSeries().getTimeAt(1), test.getTimeSeries().getTimeAt(0));
    assertEquals(reference.getTimeSeries().getValueAt(1), test.getTimeSeries().getValueAt(0));
    assertEquals(reference.getTimeSeries().getTimeAt(2), test.getTimeSeries().getTimeAt(1));
    assertEquals(reference.getTimeSeries().getValueAt(2), test.getTimeSeries().getValueAt(1));
    
    // Get filtered by -ve maxPoints, closed date range
    filter.setMaxPoints(-2); // get latest two points
    test = testPopulated.getTimeSeries(doc1.getUniqueId(), filter);
    assertEquals(2, test.getTimeSeries().size());
    assertEquals(reference.getTimeSeries().getTimeAt(reference.getTimeSeries().size() - 3), test.getTimeSeries().getTimeAt(0));
    assertEquals(reference.getTimeSeries().getValueAt(reference.getTimeSeries().size() - 3), test.getTimeSeries().getValueAt(0));
    assertEquals(reference.getTimeSeries().getTimeAt(reference.getTimeSeries().size() - 2), test.getTimeSeries().getTimeAt(1));
    assertEquals(reference.getTimeSeries().getValueAt(reference.getTimeSeries().size() - 2), test.getTimeSeries().getValueAt(1));
    
    // Get filtered by -ve maxPoints, open-ended end
    filter.setLatestDate(null);
    test = testPopulated.getTimeSeries(doc1.getUniqueId(), filter);
    assertEquals(2, test.getTimeSeries().size());
    assertEquals(reference.getTimeSeries().getTimeAt(reference.getTimeSeries().size() - 2), test.getTimeSeries().getTimeAt(0));
    assertEquals(reference.getTimeSeries().getValueAt(reference.getTimeSeries().size() - 2), test.getTimeSeries().getValueAt(0));
    assertEquals(reference.getTimeSeries().getTimeAt(reference.getTimeSeries().size() - 1), test.getTimeSeries().getTimeAt(1));
    assertEquals(reference.getTimeSeries().getValueAt(reference.getTimeSeries().size() - 1), test.getTimeSeries().getValueAt(1));   
  }

}
