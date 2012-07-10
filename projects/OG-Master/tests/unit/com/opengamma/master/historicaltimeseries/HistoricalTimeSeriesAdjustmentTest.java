/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.master.historicaltimeseries;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import javax.time.calendar.LocalDate;

import org.testng.annotations.Test;

import com.opengamma.core.historicaltimeseries.HistoricalTimeSeries;
import com.opengamma.core.historicaltimeseries.impl.SimpleHistoricalTimeSeries;
import com.opengamma.id.UniqueId;
import com.opengamma.util.timeseries.localdate.ArrayLocalDateDoubleTimeSeries;

/**
 * Tests the {@link HistoricalTimeSeriesAdjustment} class.
 */
@Test
public class HistoricalTimeSeriesAdjustmentTest {

  private HistoricalTimeSeries createTestSeries() {
    return new SimpleHistoricalTimeSeries(UniqueId.of("HTS", "Test"), new ArrayLocalDateDoubleTimeSeries(new LocalDate[] {LocalDate.now() }, new double[] {100d }));
  }

  public void testNoOp() {
    final HistoricalTimeSeriesAdjustment noop = HistoricalTimeSeriesAdjustment.NoOp.INSTANCE;
    assertEquals(noop.toString(), "");
    assertTrue(HistoricalTimeSeriesAdjustment.parse("") instanceof HistoricalTimeSeriesAdjustment.NoOp);
    final HistoricalTimeSeries hts = noop.adjust(createTestSeries());
    assertEquals(hts.getTimeSeries().getLatestValue(), 100d);
  }

  public void testDivideBy() {
    final HistoricalTimeSeriesAdjustment div = new HistoricalTimeSeriesAdjustment.DivideBy(100d);
    assertEquals(div.toString(), "100.0 /");
    final HistoricalTimeSeriesAdjustment dec = HistoricalTimeSeriesAdjustment.parse("100.0 /");
    assertEquals(((HistoricalTimeSeriesAdjustment.DivideBy) dec).getAmountToDivideBy(), 100d);
    final HistoricalTimeSeries hts = dec.adjust(createTestSeries());
    assertEquals(hts.getTimeSeries().getLatestValue(), 1d);
  }

  public void testSubtractFrom() {
    final HistoricalTimeSeriesAdjustment sub = new HistoricalTimeSeriesAdjustment.Subtract(1d);
    assertEquals(sub.toString(), "1.0 -");
    final HistoricalTimeSeriesAdjustment dec = HistoricalTimeSeriesAdjustment.parse("1.0 -");
    assertEquals(((HistoricalTimeSeriesAdjustment.Subtract) dec).getAmountToSubtract(), 1d);
    final HistoricalTimeSeries hts = dec.adjust(createTestSeries());
    assertEquals(hts.getTimeSeries().getLatestValue(), 99d);
  }

  public void testSequence() {
    final HistoricalTimeSeriesAdjustment seq = new HistoricalTimeSeriesAdjustment.Sequence(new HistoricalTimeSeriesAdjustment.Sequence(new HistoricalTimeSeriesAdjustment.Subtract(50d),
        new HistoricalTimeSeriesAdjustment.DivideBy(100d)), new HistoricalTimeSeriesAdjustment.Subtract(0.1));
    assertEquals(seq.toString(), "50.0 - 100.0 / 0.1 -");
    final HistoricalTimeSeriesAdjustment dec = HistoricalTimeSeriesAdjustment.parse("50.0 - 100.0 / 0.1 -");
    // Note that sequence is commutative, but right associates in the parser
    assertTrue(((HistoricalTimeSeriesAdjustment.Sequence) dec).getFirst() instanceof HistoricalTimeSeriesAdjustment.Subtract);
    assertTrue(((HistoricalTimeSeriesAdjustment.Sequence) dec).getSecond() instanceof HistoricalTimeSeriesAdjustment.Sequence);
    final HistoricalTimeSeries hts = dec.adjust(createTestSeries());
    assertEquals(hts.getTimeSeries().getLatestValue(), 0.4);
  }

}
