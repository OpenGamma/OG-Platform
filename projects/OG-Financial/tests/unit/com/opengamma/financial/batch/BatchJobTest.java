/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.batch;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;

import javax.time.calendar.LocalDate;
import javax.time.calendar.ZonedDateTime;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.PosixParser;
import org.junit.Test;

import com.opengamma.core.common.Currency;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.engine.view.ViewInternal;
import com.opengamma.financial.ViewTestUtils;
import com.opengamma.id.Identifier;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.master.config.ConfigDocument;
import com.opengamma.master.config.impl.MockConfigSource;
import com.opengamma.master.holiday.impl.CoppClarkHolidayFileReader;
import com.opengamma.master.holiday.impl.InMemoryHolidayMaster;

/**
 * Test batchJob.
 */
public class BatchJobTest {
  
  @Test(expected=IllegalStateException.class)
  public void emptyCommandLine() throws Exception {
    CommandLineBatchJob job = new CommandLineBatchJob();
    CommandLineParser parser = new PosixParser();
    CommandLine line = parser.parse(CommandLineBatchJob.getOptions(), "".split(" "));
    job.initialize(line, null);
  }
  
  @Test(expected=IllegalStateException.class)
  public void noViewName() throws Exception {
    CommandLineBatchJob job = new CommandLineBatchJob();
    CommandLineParser parser = new PosixParser();
    CommandLine line = parser.parse(CommandLineBatchJob.getOptions(), "-springXml batch.xml".split(" "));
    job.initialize(line, null);
  }
  
  @Test(expected=IllegalStateException.class)
  public void noSpringXml() throws Exception {
    CommandLineBatchJob job = new CommandLineBatchJob();
    CommandLineParser parser = new PosixParser();
    CommandLine line = parser.parse(CommandLineBatchJob.getOptions(), "-view TestPortfolio".split(" "));
    job.initialize(line, null);
  }

  @Test
  public void minimumCommandLine() throws Exception {
    CommandLineBatchJob job = new CommandLineBatchJob();
    CommandLineParser parser = new PosixParser();
    CommandLine line = parser.parse(CommandLineBatchJob.getOptions(), "-view TestPortfolio -springXml batch.xml".split(" "));
    job.initialize(line, null);
    assertEquals(1, job.getRuns().size());
    CommandLineBatchJobRun run = job.getRuns().get(0);
    
    assertEquals(job.getCreationTime().toInstant(), run.getValuationTime());
    assertEquals(job.getCreationTime().toInstant(), run.getConfigDbTime());
    assertEquals(job.getCreationTime().toInstant(), run.getStaticDataTime());
    
    assertEquals(job.getCreationTime().toLocalDate(), run.getObservationDate());
    assertEquals(BatchJobParameters.AD_HOC_OBSERVATION_TIME, run.getObservationTime());
    
    assertEquals(job.getCreationTime().toLocalDate(), run.getSnapshotObservationDate());
    assertEquals(BatchJobParameters.AD_HOC_OBSERVATION_TIME, run.getSnapshotObservationTime());
    
    assertEquals("Manual run started on " + 
        job.getCreationTime() + " by " + 
        System.getProperty("user.name"), run.getRunReason());
  }
  
  @Test
  public void configDbParameters() throws Exception {
    CommandLineBatchJob job = new CommandLineBatchJob();
    CommandLineParser parser = new PosixParser();
    CommandLine line = parser.parse(CommandLineBatchJob.getOptions(), "-springXml batch.xml".split(" "));
    BatchJobParameters parameters = new BatchJobParameters();
    parameters.setViewName("TestPortfolio1");
    job.initialize(line, parameters);
    assertEquals("TestPortfolio1", job.getParameters().getViewName());
  }
  
  @Test
  public void commandLineOverridesParameters() throws Exception {
    CommandLineBatchJob job = new CommandLineBatchJob();
    CommandLineParser parser = new PosixParser();
    CommandLine line = parser.parse(CommandLineBatchJob.getOptions(), "-view TestPortfolio1 -springXml batch.xml".split(" "));
    BatchJobParameters parameters = new BatchJobParameters();
    parameters.setViewName("TestPortfolio2");
    job.initialize(line, parameters);
    assertEquals("TestPortfolio1", job.getParameters().getViewName());
  }

  @Test
  public void dateRangeCommandLineSnapshotAvailability() throws Exception {
    SnapshotId snapshotId1 = new SnapshotId(LocalDate.of(2010, 9, 1), "LDN_CLOSE");
    SnapshotId snapshotId2 = new SnapshotId(LocalDate.of(2010, 9, 4), "LDN_CLOSE");
    SnapshotId snapshotId3 = new SnapshotId(LocalDate.of(2010, 9, 7), "LDN_CLOSE");
    
    LiveDataValue value = new LiveDataValue(new ComputationTargetSpecification(
        Identifier.of("BUID", "EQ12345")), "BID", 11.22);
    
    DummyBatchDbManager dbManager = new DummyBatchDbManager();
    dbManager.addLiveData(snapshotId1, value);
    dbManager.addLiveData(snapshotId2, value);
    dbManager.addLiveData(snapshotId3, value);
    
    CommandLineBatchJob job = new CommandLineBatchJob();
    job.setBatchDbManager(dbManager);
    
    CommandLineParser parser = new PosixParser();
    CommandLine line = parser.parse(CommandLineBatchJob.getOptions(), 
        "-view TestPortfolio -springXml batch.xml -dateRangeStart 20100901 -dateRangeEnd 20100907 -snapshotDateRange -observationTime LDN_CLOSE".split(" "));
    job.initialize(line, null);
    
    assertEquals(3, job.getRuns().size()); // days 2, 3, 5, 6: no snapshot -> no run 
    
    HashSet<LocalDate> observationDates = new HashSet<LocalDate>();
    HashSet<LocalDate> snapshotObservationDates = new HashSet<LocalDate>();
    
    for (BatchJobRun run : job.getRuns()) {
      observationDates.add(run.getObservationDate());
      snapshotObservationDates.add(run.getSnapshotObservationDate());
      
      assertEquals(job.getCreationTime().toLocalTime(), ZonedDateTime.ofInstant(run.getValuationTime(), job.getCreationTime().getZone()).toLocalTime());
    }
    
    assertTrue(observationDates.contains(LocalDate.of(2010, 9, 1)));
    assertTrue(observationDates.contains(LocalDate.of(2010, 9, 4)));
    assertTrue(observationDates.contains(LocalDate.of(2010, 9, 7)));
    
    assertEquals(observationDates, snapshotObservationDates);
  }
  
  @Test
  public void dateRangeCommandLineHolidayMaster() throws Exception {
    HolidaySource holidaySource = CoppClarkHolidayFileReader.createPopulated(new InMemoryHolidayMaster());
    
    CommandLineBatchJob job = new CommandLineBatchJob();
    job.setBatchDbManager(new DummyBatchDbManager());
    job.setHolidaySource(holidaySource);
    job.setHolidayCurrency(Currency.getInstance("USD"));
    
    CommandLineParser parser = new PosixParser();
    CommandLine line = parser.parse(CommandLineBatchJob.getOptions(), 
        "-view TestPortfolio -springXml batch.xml -dateRangeStart 20100114 -dateRangeEnd 20100119 -observationTime LDN_CLOSE".split(" "));
    job.initialize(line, null);
    
    assertEquals(3, job.getRuns().size()); // 14 = thursday, 15 = friday, 18 = Martin L King's Birthday, 19 = tuesday 
    
    HashSet<LocalDate> observationDates = new HashSet<LocalDate>();
    HashSet<LocalDate> snapshotObservationDates = new HashSet<LocalDate>();
    
    for (BatchJobRun run : job.getRuns()) {
      observationDates.add(run.getObservationDate());
      snapshotObservationDates.add(run.getSnapshotObservationDate());
      
      assertEquals(job.getCreationTime().toLocalTime(), ZonedDateTime.ofInstant(run.getValuationTime(), job.getCreationTime().getZone()).toLocalTime());
    }
    
    assertTrue(observationDates.contains(LocalDate.of(2010, 1, 14)));
    assertTrue(observationDates.contains(LocalDate.of(2010, 1, 15)));
    assertTrue(observationDates.contains(LocalDate.of(2010, 1, 19)));
    
    assertEquals(observationDates, snapshotObservationDates);
  }
  
  @Test
  public void dateRangeCommandLineNoHolidayMaster() throws Exception {
    CommandLineBatchJob job = new CommandLineBatchJob();
    job.setBatchDbManager(new DummyBatchDbManager());
    
    CommandLineParser parser = new PosixParser();
    CommandLine line = parser.parse(CommandLineBatchJob.getOptions(), 
        "-view TestPortfolio -springXml batch.xml -dateRangeStart 20100114 -dateRangeEnd 20100119 -observationTime LDN_CLOSE".split(" "));
    job.initialize(line, null);
    
    assertEquals(4, job.getRuns().size()); // 14 = thursday, 15 = friday, 18 = Martin L King's Birthday, 19 = tuesday 
    
    HashSet<LocalDate> observationDates = new HashSet<LocalDate>();
    HashSet<LocalDate> snapshotObservationDates = new HashSet<LocalDate>();
    
    for (BatchJobRun run : job.getRuns()) {
      observationDates.add(run.getObservationDate());
      snapshotObservationDates.add(run.getSnapshotObservationDate());
      
      assertEquals(job.getCreationTime().toLocalTime(), ZonedDateTime.ofInstant(run.getValuationTime(), job.getCreationTime().getZone()).toLocalTime());
    }
    
    assertTrue(observationDates.contains(LocalDate.of(2010, 1, 14)));
    assertTrue(observationDates.contains(LocalDate.of(2010, 1, 15)));
    assertTrue(observationDates.contains(LocalDate.of(2010, 1, 18)));
    assertTrue(observationDates.contains(LocalDate.of(2010, 1, 19)));
    
    assertEquals(observationDates, snapshotObservationDates);
  }

  @Test
  public void initView() throws Exception {
    ViewInternal testView = ViewTestUtils.getMockView();
    
    final ConfigDocument<ViewDefinition> cfgDocument = new ConfigDocument<ViewDefinition>();
    cfgDocument.setUniqueId(UniqueIdentifier.of("BatchJobTest", "1"));
    cfgDocument.setName("MyView");
    cfgDocument.setValue(testView.getDefinition());
    MockConfigSource cfgSource = new MockConfigSource();
    cfgSource.add(cfgDocument);
    
    SnapshotId snapshotId = new SnapshotId(LocalDate.of(9999, 9, 1), "AD_HOC_RUN");
    LiveDataValue value = new LiveDataValue(new ComputationTargetSpecification(
        Identifier.of("BUID", "EQ12345")), "BID", 11.22);
    
    DummyBatchDbManager dbManager = new DummyBatchDbManager();
    dbManager.addLiveData(snapshotId, value);

    CommandLineBatchJob job = new CommandLineBatchJob();
    job.setBatchDbManager(dbManager);
    job.setPositionSource(testView.getProcessingContext().getPositionSource());
    job.setSecuritySource(testView.getProcessingContext().getSecuritySource());
    job.setFunctionCompilationService(testView.getProcessingContext().getFunctionCompilationService());
    job.setFunctionExecutionContext(new FunctionExecutionContext());
    job.setConfigSource(cfgSource);
    
    CommandLineParser parser = new PosixParser();
    CommandLine line = parser.parse(CommandLineBatchJob.getOptions(), 
        "-view MyView -springXml batch.xml -observationDate 99990901".split(" "));
    job.initialize(line, null);
    
    job.getRuns().get(0).createViewDefinition();
    job.getRuns().get(0).createView();
  }

}
