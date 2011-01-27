/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.batch;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.concurrent.Future;

import org.junit.Test;

import com.opengamma.engine.depgraph.DependencyGraph;
import com.opengamma.engine.test.TestDependencyGraphExecutor;
import com.opengamma.engine.view.calcnode.CalculationJobResult;
import com.opengamma.engine.view.calcnode.CalculationJobResultItem;
import com.opengamma.engine.view.calcnode.CalculationJobSpecification;

/**
 * 
 */
public class BatchResultWriterExecutorTest {
  
  @Test
  public void basicOperation() {
    
    CalculationJobSpecification spec = new CalculationJobSpecification("view", "config", 1L, 1L);
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
    
    verify(writer).getGraphToExecute(graph);
    verify(writer).write(result, graph);
  }

}
