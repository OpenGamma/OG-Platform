/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.batch;

import org.junit.Ignore;
import org.junit.Test;

/**
 * 
 */
@Ignore
public class BatchJobTest {
  
  @Test
  public void minimumCommandLine() throws Exception {
    BatchJob job = new BatchJob();
    job.parse("-view TestPortfolio".split(" "));
  }

}
