/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.function.config;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import org.junit.Test;

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
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;

/**
 * 
 */
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

  @Test(expected = NullPointerException.class)
  public void nullConfiguration() {
    RepositoryFactory.constructRepository(null);
  }

  @Test
  public void emptyConfiguration() {
    RepositoryConfiguration configuration = new RepositoryConfiguration();
    InMemoryFunctionRepository repo = RepositoryFactory.constructRepository(configuration);
    assertNotNull(repo);
    assertTrue(repo.getAllFunctions().isEmpty());
  }

  @Test
  public void singleConfigurationNoArgs() {
    RepositoryConfiguration configuration = new RepositoryConfiguration();
    configuration.addFunctions(new StaticFunctionConfiguration(MockEmptyFunction.class.getName()));
    InMemoryFunctionRepository repo = RepositoryFactory.constructRepository(configuration);
    assertNotNull(repo);

    Collection<FunctionDefinition> definitions = repo.getAllFunctions();
    assertNotNull(definitions);
    assertEquals(1, definitions.size());
    FunctionDefinition definition = definitions.iterator().next();
    assertTrue(definition instanceof MockEmptyFunction);
    assertNotNull(definition.getUniqueId());

    final CompiledFunctionRepository compiledRepo = new CompiledFunctionService (repo, new CachingFunctionRepositoryCompiler (), new FunctionCompilationContext ()).compileFunctionRepository(System.currentTimeMillis ());
    assertNotNull(compiledRepo.getDefinition(definition.getUniqueId()));
    FunctionInvoker invoker = compiledRepo.getInvoker(definition.getUniqueId());
    assertNotNull(invoker);
    assertTrue(invoker instanceof MockEmptyFunction);
    assertSame(definition, invoker);
  }

  @Test
  public void twoConfigurationsWithArgs() {
    RepositoryConfiguration configuration = new RepositoryConfiguration();
    configuration.addFunctions(new ParameterizedFunctionConfiguration(MockSingleArgumentFunction.class.getName(), Collections.singleton("foo")));
    configuration.addFunctions(new ParameterizedFunctionConfiguration(MockMultiArgumentFunctionArrayForm.class.getName(), Lists.newArrayList(
        "foo1", "foo2")));
    configuration.addFunctions(new ParameterizedFunctionConfiguration(MockMultiArgumentFunctionIndividualParameterForm.class.getName(), Lists.newArrayList("bar1", "bar2")));
    InMemoryFunctionRepository repo = RepositoryFactory.constructRepository(configuration);
    assertNotNull(repo);

    Collection<FunctionDefinition> definitions = repo.getAllFunctions();
    assertNotNull(definitions);
    assertEquals(3, definitions.size());
    for (FunctionDefinition definition : definitions) {
      if (definition instanceof MockSingleArgumentFunction) {
        MockSingleArgumentFunction single = (MockSingleArgumentFunction) definition;
        assertEquals("foo", single.getParam());
      } else if (definition instanceof MockMultiArgumentFunctionArrayForm) {
        MockMultiArgumentFunctionArrayForm multi = (MockMultiArgumentFunctionArrayForm) definition;
        assertArrayEquals(new String[] {"foo1", "foo2"}, multi.getParams());
      } else if (definition instanceof MockMultiArgumentFunctionIndividualParameterForm) {
        MockMultiArgumentFunctionIndividualParameterForm multi = (MockMultiArgumentFunctionIndividualParameterForm) definition;
        assertEquals("bar1", multi.getParam1());
        assertEquals("bar2", multi.getParam2());
      } else {
        fail("Unexpected type of definition " + definition);
      }
    }
  }

}
