/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.examples.simulated.engine;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.opengamma.component.ComponentManager;
import com.opengamma.component.ComponentRepository;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.core.config.impl.ConfigItem;
import com.opengamma.engine.depgraph.ambiguity.ViewDefinitionAmbiguityTest;
import com.opengamma.engine.function.CompiledFunctionService;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.exclusion.FunctionExclusionGroups;
import com.opengamma.engine.function.resolver.FunctionResolver;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.engine.view.ViewProcessor;
import com.opengamma.id.VersionCorrection;
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

  protected ConfigSource getConfigSource() {
    return _configSource;
  }

  @DataProvider(name = "viewDefinitions")
  public Object[][] viewDefinitionsProvider() {
    final Collection<ConfigItem<ViewDefinition>> items = getConfigSource().getAll(ViewDefinition.class, VersionCorrection.LATEST);
    final Object[][] viewDefinitions = new Object[items.size()][1];
    int i = 0;
    for (final ConfigItem<ViewDefinition> item : items) {
      viewDefinitions[i++][0] = item.getValue();
    }
    Arrays.sort(viewDefinitions, new Comparator<Object[]>() {
      @Override
      public int compare(Object[] o1, Object[] o2) {
        return ((ViewDefinition) o1[0]).getName().compareTo(((ViewDefinition) o2[0]).getName());
      }
    });
    return viewDefinitions;
  }

  @Override
  @Test(dataProvider = "viewDefinitions", enabled = false, groups = TestGroup.INTEGRATION)
  public void runAmbiguityTest(final ViewDefinition view) throws InterruptedException {
    super.runAmbiguityTest(view);
  }

}
