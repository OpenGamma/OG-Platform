/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.batch;

import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertEquals;
import org.testng.annotations.Test;
import javax.time.calendar.LocalDate;
import javax.time.calendar.TimeZone;

import com.opengamma.core.historicaldata.impl.MockHistoricalDataSource;
import com.opengamma.engine.livedata.HistoricalLiveDataSnapshotProvider;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.util.timeseries.localdate.ArrayLocalDateDoubleTimeSeries;

/**
 * 
 */
public class BatchLiveDataSnapshotProviderTest {
  
  @Test
  public void basicCase() {
    final LocalDate date = LocalDate.of(2005, 11, 12);
    
    ArrayLocalDateDoubleTimeSeries timeSeries = new ArrayLocalDateDoubleTimeSeries(
        new LocalDate[] { date },
        new double[] { 11.12 });
    
    CommandLineBatchJob job = new CommandLineBatchJob();
    job.getParameters().initializeDefaults(job);
    CommandLineBatchJobRun run = new CommandLineBatchJobRun(job,
        date,
        date,
        date,
        date);
    job.getParameters().setSnapshotObservationTime("LDN_CLOSE");
    
    MockHistoricalDataSource historicalDataProvider = new MockHistoricalDataSource();
    
    Identifier identifier = Identifier.of("mytimeseries", "500");
    IdentifierBundle bundle = IdentifierBundle.of(identifier);
    historicalDataProvider.storeHistoricalTimeSeries(bundle, "BLOOMBERG", "CMPL", "PX_LAST", timeSeries);
    
    HistoricalLiveDataSnapshotProvider snapshotProvider = new HistoricalLiveDataSnapshotProvider(historicalDataProvider, "BLOOMBERG", "CMPL", "PX_LAST");
    
    BatchLiveDataSnapshotProvider provider = new BatchLiveDataSnapshotProvider(run,
        new DummyBatchMaster(),
        snapshotProvider);
    
    long snapshot = provider.snapshot(LocalDate.of(2005, 11, 12).atStartOfDayInZone(TimeZone.UTC).toInstant().toEpochMillisLong());
    
    Object ts = provider.querySnapshot(snapshot, new ValueRequirement("foo", identifier));
    assertEquals(11.12, ts);
    
    assertNull(provider.querySnapshot(snapshot, new ValueRequirement("foo", Identifier.of("mytimeseries2", "500"))));
    assertNull(provider.querySnapshot(snapshot, new ValueRequirement("foo", Identifier.of("mytimeseries", "501"))));
    
    ts = provider.querySnapshot(snapshot + 1, new ValueRequirement("foo", identifier));
    // funny effect: even though snapshot() has not been called at time snapshot + 1, data
    // is still returned! this is explained by HistoricalLiveDataSnapshotProvider behaviour
    assertEquals(11.12, ts); 
  }

}
