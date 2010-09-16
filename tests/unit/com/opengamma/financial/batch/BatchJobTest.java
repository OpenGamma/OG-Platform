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
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.view.View;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.financial.ViewTestUtils;
import com.opengamma.id.Identifier;
import com.opengamma.util.MongoDBConnectionSettings;
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
    SnapshotId snapshotId1 = new SnapshotId(LocalDate.of(2010, 9, 1), "LDN_CLOSE");
    SnapshotId snapshotId2 = new SnapshotId(LocalDate.of(2010, 9, 4), "LDN_CLOSE");
    SnapshotId snapshotId3 = new SnapshotId(LocalDate.of(2010, 9, 7), "LDN_CLOSE");
    
    LiveDataValue value = new LiveDataValue(new ComputationTargetSpecification(
        Identifier.of("BUID", "EQ12345")), "BID", 11.22);
    
    DummyBatchDbManager dbManager = new DummyBatchDbManager();
    dbManager.addLiveData(snapshotId1, value);
    dbManager.addLiveData(snapshotId2, value);
    dbManager.addLiveData(snapshotId3, value);
    
    BatchJob job = new BatchJob();
    job.setBatchDbManager(dbManager);
    job.parse("-view TestPortfolio -daterangestart 20100901 -daterangeend 20100907 -observationtime LDN_CLOSE".split(" "));
    
    assertEquals(3, job.getRuns().size()); // days 2, 3, 5, 6: no snapshot -> no run 
    
    HashSet<LocalDate> observationDates = new HashSet<LocalDate>();
    HashSet<LocalDate> snapshotObservationDates = new HashSet<LocalDate>();
    
    for (BatchJobRun run : job.getRuns()) {
      observationDates.add(run.getObservationDate());
      snapshotObservationDates.add(run.getSnapshotObservationDate());
      
      assertEquals(job.getCreationTime().toLocalTime(), run.getValuationTime().toLocalTime());
    }
    
    assertTrue(observationDates.contains(LocalDate.of(2010, 9, 1)));
    assertTrue(observationDates.contains(LocalDate.of(2010, 9, 4)));
    assertTrue(observationDates.contains(LocalDate.of(2010, 9, 7)));
    
    assertEquals(observationDates, snapshotObservationDates);
  }
  
  @Test
  public void initViewFromMongo() {
    View testView = ViewTestUtils.getMockView();
    
    MongoDBConnectionSettings settings = MongoDBTestUtils.makeTestSettings(null, false);
    MongoDBConfigMaster<ViewDefinition> configRepo = new MongoDBConfigMaster<ViewDefinition>(ViewDefinition.class, settings, true);
    DefaultConfigDocument<ViewDefinition> configDocument = new DefaultConfigDocument<ViewDefinition>();
    configDocument.setName("MyView");
    configDocument.setValue(testView.getDefinition());
    configRepo.add(configDocument);
    
    SnapshotId snapshotId = new SnapshotId(LocalDate.of(9999, 9, 1), "AD_HOC_RUN");
    LiveDataValue value = new LiveDataValue(new ComputationTargetSpecification(
        Identifier.of("BUID", "EQ12345")), "BID", 11.22);
    
    DummyBatchDbManager dbManager = new DummyBatchDbManager();
    dbManager.addLiveData(snapshotId, value);

    BatchJob job = new BatchJob();
    job.setBatchDbManager(dbManager);
    job.setPositionSource(testView.getProcessingContext().getPositionSource());
    job.setSecuritySource(testView.getProcessingContext().getSecuritySource());
    job.setFunctionRepository(testView.getProcessingContext().getFunctionRepository());
    job.setFunctionCompilationContext(new FunctionCompilationContext());
    job.setFunctionExecutionContext(new FunctionExecutionContext());
    job.setConfigDbConnectionSettings(settings);
    
    job.parse("-view MyView -observationdate 99990901".split(" "));
    job.createViewDefinition();
    job.createView(job.getRuns().get(0));
    
  }
  
  
  
}
