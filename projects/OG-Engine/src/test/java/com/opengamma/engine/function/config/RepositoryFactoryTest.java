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

import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.threeten.bp.Instant;

import com.google.common.collect.Lists;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.DefaultComputationTargetResolver;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.CachingFunctionRepositoryCompiler;
import com.opengamma.engine.function.CompiledFunctionRepository;
import com.opengamma.engine.function.CompiledFunctionService;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionDefinition;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.function.FunctionInvoker;
import com.opengamma.engine.function.FunctionRepository;
import com.opengamma.engine.function.InMemoryFunctionRepository;
import com.opengamma.engine.function.MarketDataAliasingFunction;
import com.opengamma.engine.function.NoOpFunction;
import com.opengamma.engine.function.StructureManipulationFunction;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.test.TestLifecycle;

/**
 * 
 */
@Test(groups = TestGroup.UNIT)
public class RepositoryFactoryTest {

  public static class MockEmptyFunction extends AbstractFunction.NonCompiledInvoker {

    @Override
    public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
      return false;
    }

    @Override
    public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
      return null;
    }

    @Override
    public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
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
    public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
      return null;
    }

  }

  public static class MockSingleArgumentFunction extends MockEmptyFunction {
    private final String _param;

    public MockSingleArgumentFunction(final String param) {
      _param = param;
    }

    public String getParam() {
      return _param;
    }
  }

  public static class MockMultiArgumentFunctionIndividualParameterForm extends MockEmptyFunction {
    private final String _param1;
    private final String _param2;

    public MockMultiArgumentFunctionIndividualParameterForm(final String param1, final String param2) {
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

    public MockMultiArgumentFunctionArrayForm(final String... strings) {
      _params = strings;
    }

    public String[] getParams() {
      return _params;
    }
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void nullConfiguration() {
    FunctionRepositoryFactory.constructRepository((FunctionConfigurationBundle) null);
  }

  public void emptyConfiguration() {
    final FunctionConfigurationBundle configuration = new FunctionConfigurationBundle();
    final InMemoryFunctionRepository repo = FunctionRepositoryFactory.constructRepository(configuration);
    assertNotNull(repo);
    assertEquals(repo.getAllFunctions().size(), FunctionRepositoryFactory.INTRINSIC_FUNCTION_COUNT);
    for (final FunctionDefinition definition : repo.getAllFunctions()) {
      assertTrue(isIntrinsicFunctionDefinition(definition));
      assertNotNull(definition.getUniqueId());
    }
  }

  public void singleConfigurationNoArgs() {
    TestLifecycle.begin();
    try {
      final FunctionConfigurationBundle configuration = new FunctionConfigurationBundle();
      configuration.addFunctions(new StaticFunctionConfiguration(MockEmptyFunction.class.getName()));
      final InMemoryFunctionRepository repo = FunctionRepositoryFactory.constructRepository(configuration);
      assertNotNull(repo);
      final Collection<FunctionDefinition> definitions = repo.getAllFunctions();
      assertNotNull(definitions);
      assertEquals(FunctionRepositoryFactory.INTRINSIC_FUNCTION_COUNT + 1, definitions.size());
      FunctionDefinition definition = null;
      for (final FunctionDefinition d : definitions) {
        if (d instanceof MockEmptyFunction) {
          assertNotNull(d.getUniqueId());
          definition = d;
        }
      }
      assertNotNull(definition);
      final FunctionCompilationContext context = new FunctionCompilationContext();
      context.setRawComputationTargetResolver(new DefaultComputationTargetResolver());
      final CompiledFunctionService cfs = new CompiledFunctionService(repo, new CachingFunctionRepositoryCompiler(), context);
      TestLifecycle.register(cfs);
      cfs.initialize();
      final CompiledFunctionRepository compiledRepo = cfs.compileFunctionRepository(System.currentTimeMillis());
      assertNotNull(compiledRepo.getDefinition(definition.getUniqueId()));
      final FunctionInvoker invoker = compiledRepo.getInvoker(definition.getUniqueId());
      assertNotNull(invoker);
      assertTrue(invoker instanceof MockEmptyFunction);
      assertSame(definition, invoker);
    } finally {
      TestLifecycle.end();
    }
  }

  public void twoConfigurationsWithArgs() {
    final FunctionConfigurationBundle configuration = new FunctionConfigurationBundle();
    configuration.addFunctions(new ParameterizedFunctionConfiguration(MockSingleArgumentFunction.class.getName(), Collections.singleton("foo")));
    configuration.addFunctions(new ParameterizedFunctionConfiguration(MockMultiArgumentFunctionArrayForm.class.getName(), Lists.newArrayList("foo1", "foo2")));
    configuration.addFunctions(new ParameterizedFunctionConfiguration(MockMultiArgumentFunctionIndividualParameterForm.class.getName(), Lists.newArrayList("bar1", "bar2")));
    final InMemoryFunctionRepository repo = FunctionRepositoryFactory.constructRepository(configuration);
    assertNotNull(repo);

    final Collection<FunctionDefinition> definitions = repo.getAllFunctions();
    assertNotNull(definitions);
    assertEquals(FunctionRepositoryFactory.INTRINSIC_FUNCTION_COUNT + 3, definitions.size());
    for (final FunctionDefinition definition : definitions) {
      if (definition instanceof MockSingleArgumentFunction) {
        final MockSingleArgumentFunction single = (MockSingleArgumentFunction) definition;
        assertEquals("foo", single.getParam());
      } else if (definition instanceof MockMultiArgumentFunctionArrayForm) {
        final MockMultiArgumentFunctionArrayForm multi = (MockMultiArgumentFunctionArrayForm) definition;
        assertEquals(Arrays.asList("foo1", "foo2"), Arrays.asList(multi.getParams()));
      } else if (definition instanceof MockMultiArgumentFunctionIndividualParameterForm) {
        final MockMultiArgumentFunctionIndividualParameterForm multi = (MockMultiArgumentFunctionIndividualParameterForm) definition;
        assertEquals("bar1", multi.getParam1());
        assertEquals("bar2", multi.getParam2());
      } else if (isIntrinsicFunctionDefinition(definition)) {
        // Ignore
      } else {
        Assert.fail("Unexpected type of definition " + definition);
      }
    }
  }

  private boolean isIntrinsicFunctionDefinition(FunctionDefinition definition) {
    return (definition instanceof NoOpFunction) || (definition instanceof MarketDataAliasingFunction) || (definition instanceof StructureManipulationFunction);
  }

  public void testStaticRepository() {
    final FunctionRepository functionRepository = new InMemoryFunctionRepository();
    final FunctionRepositoryFactory instance = FunctionRepositoryFactory.constructRepositoryFactory(functionRepository);
    assertSame(instance.constructRepository(Instant.now()), functionRepository);
  }

  public void testDynamicRepository() {
    final FunctionConfigurationSource configSource = Mockito.mock(FunctionConfigurationSource.class);
    final FunctionRepositoryFactory instance = FunctionRepositoryFactory.constructRepositoryFactory(configSource);
    final Instant t1 = Instant.ofEpochMilli(1L);
    final Instant t2 = Instant.ofEpochMilli(2L);
    final Instant t3 = Instant.ofEpochMilli(3L);
    final FunctionConfigurationBundle configuration1 = new FunctionConfigurationBundle();
    final FunctionConfigurationBundle configuration2 = new FunctionConfigurationBundle();
    configuration2.addFunctions(new ParameterizedFunctionConfiguration(MockSingleArgumentFunction.class.getName(), Collections.singleton("foo")));
    Mockito.when(configSource.getFunctionConfiguration(t1)).thenReturn(configuration1);
    Mockito.when(configSource.getFunctionConfiguration(t2)).thenReturn(configuration1);
    Mockito.when(configSource.getFunctionConfiguration(t3)).thenReturn(configuration2);
    final FunctionRepository repo1 = instance.constructRepository(t1);
    final FunctionRepository repo2 = instance.constructRepository(t2);
    assertEquals(repo1.getAllFunctions().size(), FunctionRepositoryFactory.INTRINSIC_FUNCTION_COUNT);
    assertSame(repo1, repo2);
    final FunctionRepository repo3 = instance.constructRepository(t3);
    assertEquals(repo3.getAllFunctions().size(), FunctionRepositoryFactory.INTRINSIC_FUNCTION_COUNT + 1);
  }

}
