/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.config;

import static com.opengamma.sesame.config.ConfigBuilder.argument;
import static com.opengamma.sesame.config.ConfigBuilder.arguments;
import static com.opengamma.sesame.config.ConfigBuilder.config;
import static com.opengamma.sesame.config.ConfigBuilder.function;
import static com.opengamma.sesame.config.ConfigBuilder.implementations;
import static org.testng.AssertJUnit.assertEquals;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.testng.annotations.Test;

import com.opengamma.sesame.function.AvailableImplementations;
import com.opengamma.sesame.function.AvailableImplementationsImpl;
import com.opengamma.sesame.function.Output;
import com.opengamma.sesame.function.Parameter;
import com.opengamma.sesame.graph.FunctionModel;
import com.opengamma.util.test.TestGroup;

@Test(groups = TestGroup.UNIT)
public class FunctionModelConfigTest {

  private static final Map<Class<?>, Annotation> ANNOTATIONS = Collections.emptyMap();
  private static final Parameter DECORATOR1_PARAM = new Parameter(Decorator1.class, "delegate", Fn.class, 0, ANNOTATIONS);
  private static final Parameter DECORATOR2_PARAM = new Parameter(Decorator2.class, "fn", Fn.class, 0, ANNOTATIONS);

  @Test
  public void mergedWithImpls() {
    FunctionModelConfig config1 = config(implementations(Object.class, String.class, Map.class, HashMap.class));
    FunctionModelConfig config2 = config(implementations(Object.class, Integer.class, Number.class, Double.class));
    FunctionModelConfig config3 = config(implementations(Set.class, HashSet.class, Set.class, HashSet.class));
    FunctionModelConfig config4 = config(implementations(Object.class, Long.class, List.class, ArrayList.class));
    FunctionModelConfig config = config1.mergedWith(config2, config3, config4);

    assertEquals(String.class, config.getFunctionImplementation(null, Object.class));
    assertEquals(Double.class, config.getFunctionImplementation(null, Number.class));
    assertEquals(HashMap.class, config.getFunctionImplementation(null, Map.class));
    assertEquals(HashSet.class, config.getFunctionImplementation(null, Set.class));
    assertEquals(ArrayList.class, config.getFunctionImplementation(null, List.class));
  }

  public void mergedWithArgs() {
    FunctionModelConfig config1 = config(arguments(function(Object.class, argument("foo", 123)),
                                                   function(String.class, argument("bar", 1.0))));
    FunctionModelConfig config2 = config(arguments(function(Object.class, argument("foo", 321)),
                                                   function(Object.class, argument("baz", "xyz"))));
    FunctionModelConfig config3 = config(arguments(function(Double.class, argument("aaa", "AAA"))));
    FunctionModelConfig config = config1.mergedWith(config2, config3);

    assertEquals(123, config.getFunctionArguments(Object.class).getArgument("foo"));
    assertEquals("xyz", config.getFunctionArguments(Object.class).getArgument("baz"));
    assertEquals(1.0, config.getFunctionArguments(String.class).getArgument("bar"));
    assertEquals("AAA", config.getFunctionArguments(Double.class).getArgument("aaa"));
  }

  @Test
  public void decoratedWithSingle() {
    FunctionModelConfig config = config(implementations(Fn.class, Impl.class));
    FunctionModelConfig decoratedConfig = config.decoratedWith(Decorator1.class);

    assertEquals(Decorator1.class, decoratedConfig.getFunctionImplementation(null, Fn.class));
    assertEquals(Impl.class, decoratedConfig.getFunctionImplementation(DECORATOR1_PARAM, Fn.class));
  }

  @Test
  public void decoratedWithMultiple() {
    FunctionModelConfig config = config(implementations(Fn.class, Impl.class));
    FunctionModelConfig decoratedConfig = config.decoratedWith(Decorator2.class).decoratedWith(Decorator1.class);

    assertEquals(Decorator1.class, decoratedConfig.getFunctionImplementation(null, Fn.class));
    assertEquals(Decorator2.class, decoratedConfig.getFunctionImplementation(DECORATOR1_PARAM, Fn.class));
    assertEquals(Impl.class, decoratedConfig.getFunctionImplementation(DECORATOR2_PARAM, Fn.class));
  }

  @Test
  public void orderingOfDecorators() {
    FunctionModelConfig config = config(implementations(Fn.class, Impl.class));
    FunctionModelConfig decoratedConfig = config.decoratedWith(Decorator1.class).decoratedWith(Decorator2.class);

    assertEquals(Decorator2.class, decoratedConfig.getFunctionImplementation(null, Fn.class));
    assertEquals(Decorator1.class, decoratedConfig.getFunctionImplementation(DECORATOR2_PARAM, Fn.class));
    assertEquals(Impl.class, decoratedConfig.getFunctionImplementation(DECORATOR1_PARAM, Fn.class));
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void decorateWithNonDecoratorClass() {
    config().decoratedWith(Impl.class);
  }

  @Test
  public void chainedDecoratorConfig() {
    FunctionModelConfig config = config(implementations(Fn.class, Impl.class));
    FunctionModelConfig decoratedConfig = config.decoratedWith(Decorator2.class).decoratedWith(Decorator1.class);

    assertEquals(Decorator1.class, decoratedConfig.getFunctionImplementation(null, Fn.class));
    assertEquals(Decorator2.class, decoratedConfig.getFunctionImplementation(DECORATOR1_PARAM, Fn.class));
    assertEquals(Impl.class, decoratedConfig.getFunctionImplementation(DECORATOR2_PARAM, Fn.class));
  }

  @Test
  public void multipleDecoratorConfigs() {
    FunctionModelConfig config = config(implementations(Fn.class, Impl.class));
    FunctionModelConfig decoratedConfig1 = FunctionModelConfig.EMPTY.decoratedWith(Decorator1.class);
    FunctionModelConfig decoratedConfig2 = FunctionModelConfig.EMPTY.decoratedWith(Decorator2.class);
    FunctionModelConfig mergedConfig = decoratedConfig1.mergedWith(decoratedConfig2, config);

    assertEquals(Decorator1.class, mergedConfig.getFunctionImplementation(null, Fn.class));
    assertEquals(Decorator2.class, mergedConfig.getFunctionImplementation(DECORATOR1_PARAM, Fn.class));
    assertEquals(Impl.class, mergedConfig.getFunctionImplementation(DECORATOR2_PARAM, Fn.class));
  }

  @Test
  public void undecoratedConfig() {
    FunctionModelConfig config = config(implementations(Fn.class, Impl.class, Fn2.class, Impl2.class));
    FunctionModelConfig decoratedConfig = config.decoratedWith(Decorator1.class);

    assertEquals(Impl2.class, decoratedConfig.getFunctionImplementation(null, Fn2.class));
  }

  @Test
  public void defaultImplementationProvider() {
    AvailableImplementations availableImplementations = new AvailableImplementationsImpl();
    availableImplementations.register(Impl.class);
    FunctionModelConfig config = new FunctionModelConfig(availableImplementations.getDefaultImplementations());
    FunctionModelConfig decoratedConfig = config.decoratedWith(Decorator1.class);

    assertEquals(Decorator1.class, decoratedConfig.getFunctionImplementation(null, Fn.class));
    assertEquals(Impl.class, decoratedConfig.getFunctionImplementation(DECORATOR1_PARAM, Fn.class));
    FunctionModel.build(Fn.class, decoratedConfig);
  }

  @Test
  public void decoratorInFirstConfigImplInSecondConfig() {
    AvailableImplementations availableImplementations = new AvailableImplementationsImpl();
    availableImplementations.register(Impl.class);
    FunctionModelConfig defaultImpls = new FunctionModelConfig(availableImplementations.getDefaultImplementations());
    FunctionModelConfig decoratedConfig = FunctionModelConfig.EMPTY.decoratedWith(Decorator1.class);
    FunctionModelConfig compositeConfig = decoratedConfig.mergedWith(defaultImpls);

    assertEquals(Decorator1.class, compositeConfig.getFunctionImplementation(null, Fn.class));
    assertEquals(Impl.class, compositeConfig.getFunctionImplementation(DECORATOR1_PARAM, Fn.class));
    FunctionModel.build(Fn.class, compositeConfig);
  }

  @Test
  public void implInFirstConfigDecoratorInSecondConfig() {
    AvailableImplementations availableImplementations = new AvailableImplementationsImpl();
    availableImplementations.register(Impl.class);
    FunctionModelConfig defaultImpls = new FunctionModelConfig(availableImplementations.getDefaultImplementations());
    FunctionModelConfig decoratedConfig = FunctionModelConfig.EMPTY.decoratedWith(Decorator1.class);
    FunctionModelConfig compositeConfig = defaultImpls.mergedWith(decoratedConfig);

    assertEquals(Decorator1.class, compositeConfig.getFunctionImplementation(null, Fn.class));
    assertEquals(Impl.class, compositeConfig.getFunctionImplementation(DECORATOR1_PARAM, Fn.class));
    FunctionModel.build(Fn.class, compositeConfig);
  }

  public interface Fn {

    @Output("Foo")
    String foo(Double d);
  }

  public static class Impl implements Fn {

    @Override
    public String foo(Double d) {
      return null;
    }
  }

  public static class Decorator1 implements Fn {

    public Decorator1(Fn delegate) { }

    @Override
    public String foo(Double d) {
      return null;
    }
  }

  public static class Decorator2 implements Fn {

    public Decorator2(Fn fn) { }

    @Override
    public String foo(Double d) {
      return null;
    }
  }

  public interface Fn2 {

    @Output("Bar")
    String bar(Double d);
  }

  public static class Impl2 implements Fn2 {

    @Override
    public String bar(Double d) {
      return null;
    }
  }
}
