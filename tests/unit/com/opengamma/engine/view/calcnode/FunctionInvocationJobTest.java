/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calcnode;

import static org.junit.Assert.assertEquals;

import java.util.Collections;

import org.junit.Test;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.InMemoryFunctionRepository;
import com.opengamma.engine.function.MockFunction;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.cache.MapViewComputationCache;

/**
 * 
 */
public class FunctionInvocationJobTest {
  
  /**
   * Just test a basic no-op function invocation. Because there are no inputs
   * or outputs, this is really just here to make sure that the class itself
   * can work in the absence of doing any real work.
   */
  @Test
  public void mockFunctionInvocationNoInputsOutputs() {
    ComputationTarget target = new ComputationTarget(ComputationTargetType.PRIMITIVE, "USD");
    MockFunction fn = new MockFunction(target);
    InMemoryFunctionRepository functionRepo = new InMemoryFunctionRepository();
    functionRepo.addFunction(fn, fn);
    
    long iterationTimestamp = System.currentTimeMillis();
    CalculationJobSpecification jobSpec = new CalculationJobSpecification("view", "config", iterationTimestamp, 1L);
    MapViewComputationCache cache = new MapViewComputationCache();
    FunctionExecutionContext execContext = new FunctionExecutionContext();
    ViewProcessorQuery viewProcessorQuery = new ViewProcessorQuery(null, null);
    
    FunctionInvocationJob job = new FunctionInvocationJob(jobSpec, fn.getUniqueIdentifier(), Collections.<ValueSpecification>emptySet(), cache, functionRepo, execContext, viewProcessorQuery, target, Collections.<ValueRequirement>emptySet());
    
    job.run();
    
    assertEquals(0, cache.size());
  }

}
