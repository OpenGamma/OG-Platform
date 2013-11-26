/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.examples.simulated.engine;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.opengamma.component.ComponentManager;
import com.opengamma.component.ComponentRepository;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.engine.depgraph.ambiguity.ViewDefinitionAmbiguityTest;
import com.opengamma.engine.function.CompiledFunctionService;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.exclusion.FunctionExclusionGroups;
import com.opengamma.engine.function.resolver.FunctionResolver;
import com.opengamma.engine.view.ViewProcessor;
import com.opengamma.util.test.TestGroup;

/**
 * Tests the example views for ambiguous requirements.
 */
@Test(groups = TestGroup.INTEGRATION)
public class ExampleViewDefinitionAmbiguityTest extends ViewDefinitionAmbiguityTest {

  private ComponentRepository _repo;
  private ConfigSource _configSource;

  @BeforeClass(timeOut = 40_000L)
  public void initialise() {
    final ComponentManager manager = new ComponentManager("test");
    manager.start("classpath:/fullstack/fullstack-examplessimulated-test.properties");
    _repo = manager.getRepository();
    _configSource = _repo.getInstance(ViewProcessor.class, "main").getConfigSource();
  }

  @AfterClass(timeOut = 40_000L)
  public void cleanup() {
    if (_repo != null) {
      _repo.stop();
    }
  }

  @Override
  protected FunctionCompilationContext createFunctionCompilationContext() {
    return _repo.getInstance(CompiledFunctionService.class, "main").getFunctionCompilationContext();
  }

  @Override
  protected FunctionResolver createFunctionResolver() {
    return _repo.getInstance(FunctionResolver.class, "main");
  }

  @Override
  protected FunctionExclusionGroups createFunctionExclusionGroups() {
    return _repo.getInstance(FunctionExclusionGroups.class, "main");
  }

  @Override
  protected ConfigSource getConfigSource() {
    return _configSource;
  }

}
