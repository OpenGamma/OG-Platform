/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.batch;

import org.junit.Test;

import com.opengamma.config.db.MongoDBConfigRepository;
import com.opengamma.engine.view.View;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.engine.view.ViewTestUtils;
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
  }
  
  @Test
  public void initViewFromMongo() {
    View testView = ViewTestUtils.getMockView();
    
    MongoDBConnectionSettings settings = MongoDBTestUtils.makeTestSettings(null, false);
    MongoDBConfigRepository<ViewDefinition> configRepo = new MongoDBConfigRepository<ViewDefinition>(ViewDefinition.class, settings);
    configRepo.insertNewItem("MyView", testView.getDefinition());

    BatchJob job = new BatchJob();
    job.setBatchDbManager(new DummyBatchDbManager());
    job.setPositionSource(testView.getProcessingContext().getPositionSource());
    job.setSecuritySource(testView.getProcessingContext().getSecuritySource());
    job.setFunctionRepository(testView.getProcessingContext().getFunctionRepository());
    job.setViewName("MyView");
    job.setConfigDbConnectionSettings(settings);

    job.init();
  }
  
}
