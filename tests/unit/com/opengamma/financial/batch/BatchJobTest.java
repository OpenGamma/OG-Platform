/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.batch;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;

import javax.time.calendar.LocalDate;

import org.junit.Test;

import com.opengamma.config.DefaultConfigDocument;
import com.opengamma.config.db.MongoDBConfigMaster;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.view.View;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.financial.ViewTestUtils;
import com.opengamma.util.MongoDBConnectionSettings;
import com.opengamma.util.fudge.OpenGammaFudgeContext;
import com.opengamma.util.test.MongoDBTestUtils;

/**
 * 
 */
public class BatchJobTest {
  
  @Test
  public void minimumCommandLine() {
    BatchJob job = new BatchJob();
    job.parse("-view TestPortfolio".split(" "));
    assertEquals(1, job.getRuns().size());
    BatchJobRun run = job.getRuns().get(0);
    
    assertEquals(job.getCreationTime().toOffsetDateTime(), run.getValuationTime());
    
    assertEquals(job.getCreationTime().toLocalDate(), run.getObservationDate());
    assertEquals(BatchJobRun.AD_HOC_OBSERVATION_TIME, run.getObservationTime());
    
    assertEquals(job.getCreationTime().toLocalDate(), run.getSnapshotObservationDate());
    assertEquals(BatchJobRun.AD_HOC_OBSERVATION_TIME, run.getSnapshotObservationTime());
    
    assertEquals("Manual run started on " + 
        job.getCreationTime() + " by " + 
        System.getProperty("user.name"), run.getRunReason());
  }
  
  @Test
  public void dateRangeCommandLine() {
    BatchJob job = new BatchJob();
    job.parse("-view TestPortfolio -daterangestart 20100901 -daterangeend 20100907".split(" "));
    assertEquals(5, job.getRuns().size()); 
    
    HashSet<LocalDate> observationDates = new HashSet<LocalDate>();
    HashSet<LocalDate> snapshotObservationDates = new HashSet<LocalDate>();
    
    for (BatchJobRun run : job.getRuns()) {
      observationDates.add(run.getObservationDate());
      snapshotObservationDates.add(run.getSnapshotObservationDate());
      
      assertEquals(job.getCreationTime().toLocalTime(), run.getValuationTime().toLocalTime());
    }
    
    assertTrue(observationDates.contains(LocalDate.of(2010, 9, 1)));
    assertTrue(observationDates.contains(LocalDate.of(2010, 9, 2)));
    assertTrue(observationDates.contains(LocalDate.of(2010, 9, 3)));
    // 4, 5 = Saturday, Sunday -> not included
    assertTrue(observationDates.contains(LocalDate.of(2010, 9, 6)));
    assertTrue(observationDates.contains(LocalDate.of(2010, 9, 7)));
    
    assertEquals(observationDates, snapshotObservationDates);
  }
  
  @Test
  public void initViewFromMongo() {
    View testView = ViewTestUtils.getMockView();
    
    MongoDBConnectionSettings settings = MongoDBTestUtils.makeTestSettings(null, false);
    MongoDBConfigMaster<ViewDefinition> configRepo = new MongoDBConfigMaster<ViewDefinition>(ViewDefinition.class, 
        settings, OpenGammaFudgeContext.getInstance(), true, null);
    DefaultConfigDocument<ViewDefinition> configDocument = new DefaultConfigDocument<ViewDefinition>();
    configDocument.setName("MyView");
    configDocument.setValue(testView.getDefinition());
    configRepo.add(configDocument);

    BatchJob job = new BatchJob();
    job.setBatchDbManager(new DummyBatchDbManager());
    job.setPositionSource(testView.getProcessingContext().getPositionSource());
    job.setSecuritySource(testView.getProcessingContext().getSecuritySource());
    job.setFunctionRepository(testView.getProcessingContext().getFunctionRepository());
    job.setFunctionCompilationContext(new FunctionCompilationContext());
    job.setFunctionExecutionContext(new FunctionExecutionContext());
    job.setConfigDbConnectionSettings(settings);
    
    job.parse("-view MyView".split(" "));
    job.createViewDefinition();
    job.createView(job.getRuns().get(0));
  }
  
  
  
}
