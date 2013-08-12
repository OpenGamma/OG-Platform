/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.core.historicaltimeseries.impl;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertNull;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;

import com.opengamma.core.historicaltimeseries.HistoricalTimeSeries;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.util.test.AbstractRedisTestCase;
import com.opengamma.util.tuple.Pair;

/**
 * 
 */
@Test(enabled=false)
public class NonVersionedRedisHistoricalTimeSeriesSourceTest extends AbstractRedisTestCase {
  
  public void basicOperation() {
    NonVersionedRedisHistoricalTimeSeriesSource source = new NonVersionedRedisHistoricalTimeSeriesSource(getJedisPool(), getRedisPrefix());
    
    UniqueId id1 = UniqueId.of("Test", "1");
    UniqueId id2 = UniqueId.of("Test", "2");
    UniqueId id3 = UniqueId.of("Test", "3");
    
    source.setTimeSeriesPoint(id1, LocalDate.parse("2013-06-04"), 14.0);
    source.setTimeSeriesPoint(id1, LocalDate.parse("2013-06-05"), 15.0);
    source.setTimeSeriesPoint(id1, LocalDate.parse("2013-06-06"), 16.0);
    source.setTimeSeriesPoint(id1, LocalDate.parse("2013-06-07"), 17.0);
    source.setTimeSeriesPoint(id1, LocalDate.parse("2013-06-08"), 18.0);

    source.setTimeSeriesPoint(id2, LocalDate.parse("2013-06-04"), 24.0);
    source.setTimeSeriesPoint(id2, LocalDate.parse("2013-06-05"), 25.0);
    source.setTimeSeriesPoint(id2, LocalDate.parse("2013-06-06"), 26.0);
    source.setTimeSeriesPoint(id2, LocalDate.parse("2013-06-07"), 27.0);
    source.setTimeSeriesPoint(id2, LocalDate.parse("2013-06-08"), 28.0);

    source.setTimeSeriesPoint(id3, LocalDate.parse("2013-06-04"), 34.0);
    source.setTimeSeriesPoint(id3, LocalDate.parse("2013-06-05"), 35.0);
    source.setTimeSeriesPoint(id3, LocalDate.parse("2013-06-06"), 36.0);
    source.setTimeSeriesPoint(id3, LocalDate.parse("2013-06-07"), 37.0);
    source.setTimeSeriesPoint(id3, LocalDate.parse("2013-06-08"), 38.0);
    
    Pair<LocalDate, Double> pair = null;
    HistoricalTimeSeries hts = null;
    LocalDateDoubleTimeSeries ts = null; 
    
    pair = source.getLatestDataPoint(id3);
    assertNotNull(pair);
    assertEquals(LocalDate.parse("2013-06-08"), pair.getFirst());
    assertEquals(38.0, pair.getSecond(), 0.000001);
    
    assertNull(source.getHistoricalTimeSeries(UniqueId.of("Test", "5")));
    
    hts = source.getHistoricalTimeSeries(id2);
    assertNotNull(hts);
    assertEquals(id2, hts.getUniqueId());
    ts = hts.getTimeSeries();
    assertNotNull(ts);
    assertEquals(5, ts.size());
    assertEquals(24.0, ts.getValue(LocalDate.parse("2013-06-04")), 0.00001);
    assertEquals(25.0, ts.getValue(LocalDate.parse("2013-06-05")), 0.00001);
    assertEquals(26.0, ts.getValue(LocalDate.parse("2013-06-06")), 0.00001);
    assertEquals(27.0, ts.getValue(LocalDate.parse("2013-06-07")), 0.00001);
    assertEquals(28.0, ts.getValue(LocalDate.parse("2013-06-08")), 0.00001);
    
    hts = source.getHistoricalTimeSeries(ExternalIdBundle.of(ExternalId.of("Test", "1")), LocalDate.now(), "Data Source", "Data Provider", "Data Field");
    assertNotNull(hts);
    assertEquals(id1, hts.getUniqueId());
    ts = hts.getTimeSeries();
    assertNotNull(ts);
    assertEquals(5, ts.size());
    assertEquals(14.0, ts.getValue(LocalDate.parse("2013-06-04")), 0.00001);
    assertEquals(15.0, ts.getValue(LocalDate.parse("2013-06-05")), 0.00001);
    assertEquals(16.0, ts.getValue(LocalDate.parse("2013-06-06")), 0.00001);
    assertEquals(17.0, ts.getValue(LocalDate.parse("2013-06-07")), 0.00001);
    assertEquals(18.0, ts.getValue(LocalDate.parse("2013-06-08")), 0.00001);
  }

}
