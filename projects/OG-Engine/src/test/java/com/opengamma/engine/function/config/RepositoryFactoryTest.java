/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.function.config;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertSame;
import static org.testng.AssertJUnit.assertTrue;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.google.common.collect.Lists;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.CachingFunctionRepositoryCompiler;
import com.opengamma.engine.function.CompiledFunctionRepository;
import com.opengamma.engine.function.CompiledFunctionService;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionDefinition;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.function.FunctionInvoker;
import com.opengamma.engine.function.InMemoryFunctionRepository;
import com.opengamma.engine.function.NoOpFunction;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;

/**
 * 
 */
@Test
public class RepositoryFactoryTest {

  public static class MockEmptyFunction extends AbstractFunction.NonCompiledInvoker {

    @Override
    public boolean canApplyTo(FunctionCompilationContext context, ComputationTarget target) {
      return false;
    }

    @Override
    public Set<ValueRequirement> getRequirements(FunctionCompilationContext context, ComputationTarget target, final ValueRequirement desiredValue) {
      return null;
    }

    @Override
    public Set<ValueSpecification> getResults(FunctionCompilationContext context, ComputationTarget target) {
      return null;
    }

    @Override
    public String getShortName() {
      return null;
    }

    @Override
    public ComputationTargetType getTargetType() {
      return null;
    }

    @Override
    public Set<ComputedValue> execute(FunctionExecutionContext executionContext, FunctionInputs inputs, ComputationTarget target, Set<ValueRequirement> desiredValues) {
      return null;
    }

  }

  public static class MockSingleArgumentFunction extends MockEmptyFunction {
    private final String _param;

    public MockSingleArgumentFunction(String param) {
      _param = param;
    }

    public String getParam() {
      return _param;
    }
  }

  public static class MockMultiArgumentFunctionIndividualParameterForm extends MockEmptyFunction {
    private final String _param1;
    private final String _param2;

    public MockMultiArgumentFunctionIndividualParameterForm(String param1, String param2) {
      _param1 = param1;
      _param2 = param2;
    }

    public String getParam1() {
      return _param1;
    }

    public String getParam2() {
      return _param2;
    }

  }

  public static class MockMultiArgumentFunctionArrayForm extends MockEmptyFunction {
    private final String[] _params;

    public MockMultiArgumentFunctionArrayForm(String... strings) {
      _params = strings;
    }

    public String[] getParams() {
      return _params;
    }
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void nullConfiguration() {
    RepositoryFactory.constructRepository(null);
  }

  public void emptyConfiguration() {
    final RepositoryConfiguration configuration = new RepositoryConfiguration();
    final InMemoryFunctionRepository repo = RepositoryFactory.constructRepository(configuration);
    assertNotNull(repo);
    assertEquals(repo.getAllFunctions().size(), 1);
    final FunctionDefinition definition = repo.getAllFunctions().iterator().next();
    assertTrue(definition instanceof NoOpFunction);
    assertNotNull(definition.getUniqueId());
  }

  public void singleConfigurationNoArgs() {
    final RepositoryConfiguration configuration = new RepositoryConfiguration();
    configuration.addFunctions(new StaticFunctionConfiguration(MockEmptyFunction.class.getName()));
    final InMemoryFunctionRepository repo = RepositoryFactory.constructRepository(configuration);
    assertNotNull(repo);
    final Collection<FunctionDefinition> definitions = repo.getAllFunctions();
    assertNotNull(definitions);
    assertEquals(2, definitions.size());
    FunctionDefinition definition = null;
    for (FunctionDefinition d : definitions) {
      if (d instanceof MockEmptyFunction) {
        assertNotNull(d.getUniqueId());
        definition = d;
      }
    }
    assertNotNull(definition);
    final CompiledFunctionService cfs = new CompiledFunctionService(repo, new CachingFunctionRepositoryCompiler(), new FunctionCompilationContext());
    cfs.initialize();
    final CompiledFunctionRepository compiledRepo = cfs.compileFunctionRepository(System.currentTimeMillis());
    assertNotNull(compiledRepo.getDefinition(definition.getUniqueId()));
    final FunctionInvoker invoker = compiledRepo.getInvoker(definition.getUniqueId());
    assertNotNull(invoker);
    assertTrue(invoker instanceof MockEmptyFunction);
    assertSame(definition, invoker);
  }

  public void twoConfigurationsWithArgs() {
    final RepositoryConfiguration configuration = new RepositoryConfiguration();
    configuration.addFunctions(new ParameterizedFunctionConfiguration(MockSingleArgumentFunction.class.getName(), Collections.singleton("foo")));
    configuration.addFunctions(new ParameterizedFunctionConfiguration(MockMultiArgumentFunctionArrayForm.class.getName(), Lists.newArrayList(
        "foo1", "foo2")));
    configuration.addFunctions(new ParameterizedFunctionConfiguration(MockMultiArgumentFunctionIndividualParameterForm.class.getName(), Lists.newArrayList("bar1", "bar2")));
    final InMemoryFunctionRepository repo = RepositoryFactory.constructRepository(configuration);
    assertNotNull(repo);

    final Collection<FunctionDefinition> definitions = repo.getAllFunctions();
    assertNotNull(definitions);
    assertEquals(4, definitions.size());
    for (FunctionDefinition definition : definitions) {
      if (definition instanceof MockSingleArgumentFunction) {
        MockSingleArgumentFunction single = (MockSingleArgumentFunction) definition;
        assertEquals("foo", single.getParam());
      } else if (definition instanceof MockMultiArgumentFunctionArrayForm) {
        MockMultiArgumentFunctionArrayForm multi = (MockMultiArgumentFunctionArrayForm) definition;
        assertEquals(Arrays.asList("foo1", "foo2"), Arrays.asList(multi.getParams()));
      } else if (definition instanceof MockMultiArgumentFunctionIndividualParameterForm) {
        MockMultiArgumentFunctionIndividualParameterForm multi = (MockMultiArgumentFunctionIndividualParameterForm) definition;
        assertEquals("bar1", multi.getParam1());
        assertEquals("bar2", multi.getParam2());
      } else if (definition instanceof NoOpFunction) {
        // Ignore
      } else {
        Assert.fail("Unexpected type of definition " + definition);
      }
    }
  }

}
