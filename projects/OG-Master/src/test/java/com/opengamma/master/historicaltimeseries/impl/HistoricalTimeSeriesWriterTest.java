/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.master.historicaltimeseries.impl;

import static org.testng.AssertJUnit.assertEquals;

import java.util.List;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;

import com.google.common.collect.ImmutableList;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesMaster;
import com.opengamma.master.historicaltimeseries.ManageableHistoricalTimeSeries;
import com.opengamma.timeseries.date.localdate.ImmutableLocalDateDoubleTimeSeries;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.util.test.TestGroup;

/**
 * Test {@link HistoricalTimeSeriesMasterUtils}.
 */
@Test(groups = TestGroup.UNIT)
public class HistoricalTimeSeriesWriterTest {
  
  private static final String DESCRIPTION = "Description";
  private static final String DATA_SOURCE = "Source";
  private static final String DATA_PROVIDER = "Provider";
  private static final String DATA_FIELD = "Field";
  private static final String OBSERVATION_TIME = "Time";
  private static final ExternalId ID = ExternalId.of("Test", "1");
  
  private HistoricalTimeSeriesMaster _htsMaster;
  private HistoricalTimeSeriesMasterUtils _htsWriter;
  private LocalDate _today;
  
  @BeforeMethod
  public void setup() {
    _htsMaster = new InMemoryHistoricalTimeSeriesMaster();
    _htsWriter = new HistoricalTimeSeriesMasterUtils(_htsMaster);
    _today = LocalDate.now();
  }
  
  public void testAddTimeSeries() {
    List<LocalDate> dates = ImmutableList.of(_today.minusDays(2), _today.minusDays(1), _today);
    List<Double> values = ImmutableList.of(1d, 2d, 3d);
    ImmutableLocalDateDoubleTimeSeries origTs = ImmutableLocalDateDoubleTimeSeries.of(dates, values);
    UniqueId id = _htsWriter.writeTimeSeries(DESCRIPTION, DATA_SOURCE, DATA_PROVIDER, DATA_FIELD, OBSERVATION_TIME, ExternalIdBundle.of(ID), origTs);
    
    ManageableHistoricalTimeSeries manageableTs = _htsMaster.getTimeSeries(id);
    LocalDateDoubleTimeSeries readTs = manageableTs.getTimeSeries();
    assertEquals(origTs, readTs);
  }
  
  public void testUpdateTimeSeriesCorrectionOnly() {
    // Add the test series
    testAddTimeSeries();
    
    List<LocalDate> dates = ImmutableList.of(_today.minusDays(2), _today.minusDays(1), _today);
    List<Double> values = ImmutableList.of(2d, 3d, 4d);
    ImmutableLocalDateDoubleTimeSeries updatedTs = ImmutableLocalDateDoubleTimeSeries.of(dates, values);
    UniqueId id = _htsWriter.writeTimeSeries(DESCRIPTION, DATA_SOURCE, DATA_PROVIDER, DATA_FIELD, OBSERVATION_TIME, ExternalIdBundle.of(ID), updatedTs);
    
    ManageableHistoricalTimeSeries manageableTs = _htsMaster.getTimeSeries(id);
    LocalDateDoubleTimeSeries readTs = manageableTs.getTimeSeries();
    assertEquals(updatedTs, readTs);    
  }
  
  public void testUpdateTimeSeriesAddNewLaterPointsOnly() {
    // Add the test series
    testAddTimeSeries();
    
    List<LocalDate> dates = ImmutableList.of(_today.plusDays(1));
    List<Double> values = ImmutableList.of(4d);
    ImmutableLocalDateDoubleTimeSeries newTs = ImmutableLocalDateDoubleTimeSeries.of(dates, values);
    UniqueId id = _htsWriter.writeTimeSeries(DESCRIPTION, DATA_SOURCE, DATA_PROVIDER, DATA_FIELD, OBSERVATION_TIME, ExternalIdBundle.of(ID), newTs);
    
    ManageableHistoricalTimeSeries manageableTs = _htsMaster.getTimeSeries(id);
    LocalDateDoubleTimeSeries readTs = manageableTs.getTimeSeries();
    List<LocalDate> expectedDates = ImmutableList.of(_today.minusDays(2), _today.minusDays(1), _today, _today.plusDays(1));
    List<Double> expectedValues = ImmutableList.of(1d, 2d, 3d, 4d);
    ImmutableLocalDateDoubleTimeSeries expectedTs = ImmutableLocalDateDoubleTimeSeries.of(expectedDates, expectedValues);
    assertEquals(expectedTs, readTs);
  }

  @Test
  public void testUpdateTimeSeriesAddEarlierAndLaterPoints() {
    // Add the test series
    testAddTimeSeries();
    
    List<LocalDate> dates = ImmutableList.of(_today.minusDays(3), _today.plusDays(1));
    List<Double> values = ImmutableList.of(0d, 4d);
    ImmutableLocalDateDoubleTimeSeries newTs = ImmutableLocalDateDoubleTimeSeries.of(dates, values);
    UniqueId id = _htsWriter.writeTimeSeries(DESCRIPTION, DATA_SOURCE, DATA_PROVIDER, DATA_FIELD, OBSERVATION_TIME, ExternalIdBundle.of(ID), newTs);
    
    // Current implementation drops new, earlier points
    ManageableHistoricalTimeSeries manageableTs = _htsMaster.getTimeSeries(id);
    LocalDateDoubleTimeSeries readTs = manageableTs.getTimeSeries();
    List<LocalDate> expectedDates = ImmutableList.of(_today.minusDays(2), _today.minusDays(1), _today, _today.plusDays(1));
    List<Double> expectedValues = ImmutableList.of(1d, 2d, 3d, 4d);
    ImmutableLocalDateDoubleTimeSeries expectedTs = ImmutableLocalDateDoubleTimeSeries.of(expectedDates, expectedValues);
    assertEquals(expectedTs, readTs);    
  }
  
  @Test(enabled = false) // Current implementation does not support removing points
  public void testUpdateTimeSeriesRemoveExistingPoints() {
    // Add the test series
    testAddTimeSeries();
    
    List<LocalDate> dates = ImmutableList.of(_today.minusDays(2), _today);
    List<Double> values = ImmutableList.of(6d, 7d);
    ImmutableLocalDateDoubleTimeSeries updatedTs = ImmutableLocalDateDoubleTimeSeries.of(dates, values);
    UniqueId id = _htsWriter.writeTimeSeries(DESCRIPTION, DATA_SOURCE, DATA_PROVIDER, DATA_FIELD, OBSERVATION_TIME, ExternalIdBundle.of(ID), updatedTs);
    
    ManageableHistoricalTimeSeries manageableTs = _htsMaster.getTimeSeries(id);
    LocalDateDoubleTimeSeries readTs = manageableTs.getTimeSeries();
    assertEquals(updatedTs, readTs);
  }
  
  public void testAddUpdateTimeSeriesSingleExistingPoint() {
    List<LocalDate> dates = ImmutableList.of(_today);
    List<Double> origValues = ImmutableList.of(1d);
    ImmutableLocalDateDoubleTimeSeries origTs = ImmutableLocalDateDoubleTimeSeries.of(dates, origValues);
    UniqueId id = _htsWriter.writeTimeSeries(DESCRIPTION, DATA_SOURCE, DATA_PROVIDER, DATA_FIELD, OBSERVATION_TIME, ExternalIdBundle.of(ID), origTs);
    
    ManageableHistoricalTimeSeries manageableTs = _htsMaster.getTimeSeries(id);
    LocalDateDoubleTimeSeries readTs = manageableTs.getTimeSeries();
    assertEquals(origTs, readTs);
    
    List<Double> updatedValues = ImmutableList.of(2d);
    ImmutableLocalDateDoubleTimeSeries updatedTs = ImmutableLocalDateDoubleTimeSeries.of(dates, updatedValues);
    id = _htsWriter.writeTimeSeries(DESCRIPTION, DATA_SOURCE, DATA_PROVIDER, DATA_FIELD, OBSERVATION_TIME, ExternalIdBundle.of(ID), updatedTs);
    
    manageableTs = _htsMaster.getTimeSeries(id);
    readTs = manageableTs.getTimeSeries();
    assertEquals(updatedTs, readTs);
  }
  
}
