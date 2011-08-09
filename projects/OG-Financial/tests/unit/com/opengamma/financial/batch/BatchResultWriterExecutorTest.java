/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.batch;

import static org.testng.AssertJUnit.assertNotNull;
import org.testng.annotations.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.concurrent.Future;

import javax.time.Instant;

import com.opengamma.engine.depgraph.DependencyGraph;
import com.opengamma.engine.test.TestDependencyGraphExecutor;
import com.opengamma.engine.view.calcnode.CalculationJobResult;
import com.opengamma.engine.view.calcnode.CalculationJobResultItem;
import com.opengamma.engine.view.calcnode.CalculationJobSpecification;
import com.opengamma.id.UniqueId;

/**
 * 
 */
public class BatchResultWriterExecutorTest {
  
  @Test
  public void basicOperation() throws Exception {
    
    CalculationJobSpecification spec = new CalculationJobSpecification(UniqueId.of("Test", "ViewProcess"), "config", Instant.now(), 1L);
    CalculationJobResult result = new CalculationJobResult(spec, 
        500, 
        Collections.<CalculationJobResultItem>emptyList(),
        "localhost");

    TestDependencyGraphExecutor<CalculationJobResult> delegate = new TestDependencyGraphExecutor<CalculationJobResult>(result);
    
    DependencyGraph graph = new DependencyGraph("foo");
    
    BatchResultWriter writer = mock(BatchResultWriter.class);
    when(writer.getGraphToExecute(graph)).thenReturn(graph);
    
    BatchResultWriterExecutor executor = new BatchResultWriterExecutor(writer, delegate);
    
    Future<Object> future = executor.execute(graph, null);
    assertNotNull(future);
    future.get();
    
    verify(writer).getGraphToExecute(graph);
    verify(writer).write(result, graph);
  }

}
