/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.copier;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.mockito.stubbing.OngoingStubbing;
import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;
import org.threeten.bp.format.DateTimeFormatter;
import org.threeten.bp.format.DateTimeFormatterBuilder;

import au.com.bytecode.opencsv.CSVReader;

import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundleWithDates;
import com.opengamma.id.ExternalIdSearch;
import com.opengamma.integration.copier.sheet.SheetFormat;
import com.opengamma.integration.copier.sheet.reader.SheetReader;
import com.opengamma.integration.copier.timeseries.reader.SingleSheetMultiTimeSeriesReader;
import com.opengamma.integration.copier.timeseries.reader.TimeSeriesReader;
import com.opengamma.integration.copier.timeseries.writer.MasterTimeSeriesWriter;
import com.opengamma.integration.copier.timeseries.writer.TimeSeriesWriter;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoDocument;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoSearchRequest;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoSearchResult;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesMaster;
import com.opengamma.master.historicaltimeseries.ManageableHistoricalTimeSeriesInfo;
import com.opengamma.master.historicaltimeseries.impl.InMemoryHistoricalTimeSeriesMaster;
import com.opengamma.timeseries.date.localdate.ImmutableLocalDateDoubleTimeSeries;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class TimeSeriesLoaderTest {
  
  private static final String FILENAME = "src/test/java/com/opengamma/integration/copier/TimeSeries.csv";
  private static final String DATA_SOURCE = "source";
  private static final String DATA_PROVIDER = "provider";
  private static final String DATA_FIELD = "field";
  private static final String OBSERVATION_TIME = "observation";
  private static final String DATE_FORMAT = "yyyyMMdd";
  private static final String ID_SCHEME = "scheme";
  
  private static final ExternalId EXISTING_HTSINFO_EXTERNALID = ExternalId.of(ID_SCHEME, "abc");
  private static final ExternalId NEW_HTSINFO_EXTERNALID = ExternalId.of(ID_SCHEME, "def");
  
  /**
   * Tests the time series readers in isolation through the use of a mock sheet reader and a mock time series writer.
   * A time series is constructed, converted to a sheet (list of rows) and fed through the mock sheet reader into the 
   * time series reader, which should then reconstruct the time series and write it to the mock time series writer.
   * We verify whether this actually happens for the particular test case.
   * NOTE: this tests SingleSheetMultiTimeSeriesReader alone, the only reader in use at the moment
   */
  @Test
  public void testTimeSeriesReaders() {
    
    // Build a mock sheet reader with some rows
    LocalDate[] dates = {LocalDate.of(2010,1,1), LocalDate.of(2011,1,1)};
    double[] times = {1.0, 2.0};
    LocalDateDoubleTimeSeries lddts = ImmutableLocalDateDoubleTimeSeries.of(dates, times);
    SheetReader mockSheetReader = buildMockSheetReader(lddts);
    TimeSeriesReader reader = new SingleSheetMultiTimeSeriesReader(
            mockSheetReader, DATA_SOURCE, DATA_PROVIDER, DATA_FIELD, OBSERVATION_TIME, ID_SCHEME, DATE_FORMAT);
    
    // Write 
    TimeSeriesWriter mockTimeSeriesWriter = mock(TimeSeriesWriter.class);
    
    reader.writeTo(mockTimeSeriesWriter);
    
    // check sheet reader calls and ts writer calls
    verify(mockTimeSeriesWriter, times(1)).writeDataPoints(
        EXISTING_HTSINFO_EXTERNALID, DATA_SOURCE, DATA_PROVIDER, DATA_FIELD, OBSERVATION_TIME, 
        lddts); 
  }
  
  /**
   * Tests the time series writers in isolation through the use of  a mock tool context that
   * references a specially-created and populated InMemoryHistoricalTimeSeriesMaster. The time series writer
   * to be tested is supplied with a time series to write, which already exists in the master, with some data points. 
   * We then attempt to retrieve the supposedly updated time series from the in-memory master and assert that this is 
   * the same as the one passed to the writer merged with its original data points.
   * NOTE: this tests MasterTimeSeriesWriter alone, the only writer in use at the moment
   */
  @Test
  public void testTimeSeriesWritersExistingHts() {
    
    // Build a mock master with an existing hts
    LocalDate[] existingDates = {LocalDate.of(2010,1,1), LocalDate.of(2011,1,1)};
    double[] existingValues = {1.0, 2.0};
    LocalDateDoubleTimeSeries existingDataPoints = ImmutableLocalDateDoubleTimeSeries.of(existingDates, existingValues); 

    LocalDate[] newDates = {LocalDate.of(2010,2,1), LocalDate.of(2011,2,1)};
    double[] newValues = {1.5, 2.5};
    LocalDateDoubleTimeSeries newDataPoints = ImmutableLocalDateDoubleTimeSeries.of(newDates, newValues);
    HistoricalTimeSeriesMaster htsMaster = buildHistoricalTimeSeriesMaster(existingDataPoints);

    // Write the new data points to an existing hts
    TimeSeriesWriter writer = new MasterTimeSeriesWriter(htsMaster);
    writer.writeDataPoints(EXISTING_HTSINFO_EXTERNALID, DATA_SOURCE, DATA_PROVIDER, DATA_FIELD, OBSERVATION_TIME, 
        newDataPoints);
    
    // Retrieve the hts contents from the master
    HistoricalTimeSeriesInfoSearchRequest request = new HistoricalTimeSeriesInfoSearchRequest();
    request.setExternalIdSearch(ExternalIdSearch.of(EXISTING_HTSINFO_EXTERNALID));
    HistoricalTimeSeriesInfoSearchResult result = htsMaster.search(request);
    LocalDateDoubleTimeSeries retrievedDataPoints = null;
    if (result != null && result.getFirstInfo() != null) {
      retrievedDataPoints = htsMaster.getTimeSeries(
          htsMaster.get(result.getFirstInfo().getUniqueId()).getInfo().getTimeSeriesObjectId().atLatestVersion()).getTimeSeries();
    }
    
    // Assert that the retrieved contents is as expected (combined existing and new dates/values)
    LocalDate[] combinedDates = {LocalDate.of(2010,1,1), LocalDate.of(2010,2,1), LocalDate.of(2011,1,1), LocalDate.of(2011,2,1)};
    double[] combinedValues = {1.0, 1.5, 2.0, 2.5};
    LocalDateDoubleTimeSeries combinedDataPoints = ImmutableLocalDateDoubleTimeSeries.of(combinedDates, combinedValues);
    assert(combinedDataPoints.equals(retrievedDataPoints));
  }

  
  /**
   * Tests the time series writers in isolation through the use of  a mock tool context that
   * references a specially-created and populated InMemoryHistoricalTimeSeriesMaster. The time series writer
   * to be tested is supplied with a time series to write, which doesn't already exist in the master. 
   * We then attempt to retrieve the new time series from the in-memory master and assert that this is 
   * the same as the one passed to the writer.
   * NOTE: this tests MasterTimeSeriesWriter alone, the only writer in use at the moment
   */
  @Test
  public void testTimeSeriesWritersNewHts() {
    
    // Build a mock master with an existing hts
    LocalDate[] dates = {LocalDate.of(2010,1,1), LocalDate.of(2011,1,1)};
    double[] values = {1.0, 2.0};
    LocalDateDoubleTimeSeries dataPoints = ImmutableLocalDateDoubleTimeSeries.of(dates, values); 
    HistoricalTimeSeriesMaster htsMaster = buildHistoricalTimeSeriesMaster(dataPoints);
    // Write the new data points to an existing hts
    TimeSeriesWriter writer = new MasterTimeSeriesWriter(htsMaster);
    writer.writeDataPoints(NEW_HTSINFO_EXTERNALID, DATA_SOURCE, DATA_PROVIDER, DATA_FIELD, OBSERVATION_TIME, 
        dataPoints);
    
    // Retrieve and check the hts master contents
    HistoricalTimeSeriesInfoSearchRequest request = new HistoricalTimeSeriesInfoSearchRequest();
    request.setExternalIdSearch(ExternalIdSearch.of(NEW_HTSINFO_EXTERNALID));
    HistoricalTimeSeriesInfoSearchResult result = htsMaster.search(request);
    LocalDateDoubleTimeSeries retrievedDataPoints = null;
    if (result != null && result.getFirstInfo() != null) {
      retrievedDataPoints = htsMaster.getTimeSeries(
          htsMaster.get(result.getFirstInfo().getUniqueId()).getInfo().getTimeSeriesObjectId().atLatestVersion()).getTimeSeries();
    }
    assert(dataPoints.equals(retrievedDataPoints));
  }
  
  /**
   * Tests the entire time series import pipeline, sending data points all the way from a test input file to 
   * an in-memory master and ensuring that the same points are retrieved.
   */
  @Test
  public void testTimeSeriesLoaderTool() throws FileNotFoundException {
    
    // Build a mock master with an existing hts
    LocalDate[] existingDates = {LocalDate.of(2010,1,1), LocalDate.of(2011,1,1)};
    double[] existingValues = {1.0, 2.0};
    LocalDateDoubleTimeSeries existingDataPoints = ImmutableLocalDateDoubleTimeSeries.of(existingDates, existingValues); 

    // Read in the time series directly from the file and construct a time series to compare with
    DateTimeFormatterBuilder builder = new DateTimeFormatterBuilder();
    builder.appendPattern(DATE_FORMAT);
    DateTimeFormatter dateFormat = builder.toFormatter();

    String readId = "";
    List<LocalDate> dates = new ArrayList<LocalDate>();
    List<Double> values = new ArrayList<Double>();
    CSVReader csvReader = null;
    try {
      csvReader = new CSVReader(new FileReader(FILENAME));
      csvReader.readNext(); // discard header row
      String[] row;
      while ((row = csvReader.readNext()) != null) {
        // assume columns in this order: id, date, value; ignore id
        dates.add(LocalDate.parse(row[1], dateFormat));
        values.add(Double.parseDouble(row[2]));
        readId = row[0];
      }
    } catch (Throwable ex) {
      // TODO Auto-generated catch block
      ex.printStackTrace();
    } finally {
      IOUtils.closeQuietly(csvReader);
    }
    LocalDateDoubleTimeSeries compareDataPoints = ImmutableLocalDateDoubleTimeSeries.of(dates, values);

    // Set up the reader to read from file and the writer to write to the in-memory master, and do the import 
    InputStream fileStream = new BufferedInputStream(new FileInputStream(FILENAME));
    TimeSeriesReader reader = new SingleSheetMultiTimeSeriesReader(
        SheetFormat.of(FILENAME), fileStream, DATA_SOURCE, DATA_PROVIDER, DATA_FIELD, OBSERVATION_TIME, ID_SCHEME, DATE_FORMAT);
    HistoricalTimeSeriesMaster htsMaster = buildHistoricalTimeSeriesMaster(existingDataPoints);
    TimeSeriesWriter writer = new MasterTimeSeriesWriter(htsMaster);
    reader.writeTo(writer);
    
    // Retrieve hts master contents
    HistoricalTimeSeriesInfoSearchRequest request = new HistoricalTimeSeriesInfoSearchRequest();
    request.setExternalIdSearch(ExternalIdSearch.of(ExternalId.of(ID_SCHEME, readId)));
    HistoricalTimeSeriesInfoSearchResult result = htsMaster.search(request);
    LocalDateDoubleTimeSeries retrievedDataPoints = null;
    if (result != null && result.getFirstInfo() != null) {
      retrievedDataPoints = htsMaster.getTimeSeries(
          htsMaster.get(result.getFirstInfo().getUniqueId()).getInfo().getTimeSeriesObjectId().atLatestVersion()).getTimeSeries();
    }
    
    assert(compareDataPoints.equals(retrievedDataPoints));
  }
  
  
   
  private SheetReader buildMockSheetReader(LocalDateDoubleTimeSeries lddts) { 
    
    DateTimeFormatterBuilder builder = new DateTimeFormatterBuilder();
    builder.appendPattern(DATE_FORMAT);
    DateTimeFormatter dateFormat = builder.toFormatter();

    SheetReader mock = mock(SheetReader.class);
    OngoingStubbing<Map<String, String>> stub = when(mock.loadNextRow());
    for (Map.Entry<LocalDate, Double> entry : lddts) {
      Map<String, String> row = new HashMap<String, String>();
      row.put("id", EXISTING_HTSINFO_EXTERNALID.getValue());
      row.put("date", entry.getKey().toString(dateFormat));
      row.put("value", entry.getValue().toString());
      stub = stub.thenReturn(row);
    }
    stub.thenReturn(null);
    return mock;    
  }

  private HistoricalTimeSeriesMaster buildHistoricalTimeSeriesMaster(LocalDateDoubleTimeSeries lddts) {
    // Create HTS info and infoDoc
    ManageableHistoricalTimeSeriesInfo mHtsInfo = new ManageableHistoricalTimeSeriesInfo();
    mHtsInfo.setDataField(DATA_FIELD);
    mHtsInfo.setDataProvider(DATA_PROVIDER);
    mHtsInfo.setDataSource(DATA_SOURCE);
    mHtsInfo.setObservationTime(OBSERVATION_TIME);
    mHtsInfo.setExternalIdBundle(ExternalIdBundleWithDates.of(EXISTING_HTSINFO_EXTERNALID.toBundle()));
    mHtsInfo.setName("Test HTS");
    HistoricalTimeSeriesInfoDocument htsInfoDoc = new HistoricalTimeSeriesInfoDocument(mHtsInfo);

    // Create in memory HTS master and add HTS info/doc
    HistoricalTimeSeriesMaster htsMaster = new InMemoryHistoricalTimeSeriesMaster();
    htsInfoDoc = htsMaster.add(htsInfoDoc);

    // Create the actual time series and add to HTS master
    htsMaster.updateTimeSeriesDataPoints(htsInfoDoc.getInfo().getTimeSeriesObjectId(), lddts);
    return htsMaster;
  }

}
