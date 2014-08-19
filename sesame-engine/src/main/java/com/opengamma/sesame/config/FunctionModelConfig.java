/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.config;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import javax.annotation.Nullable;
import javax.inject.Provider;

import org.joda.beans.Bean;
import org.joda.beans.BeanBuilder;
import org.joda.beans.BeanDefinition;
import org.joda.beans.ImmutableBean;
import org.joda.beans.ImmutableConstructor;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaProperty;
import org.joda.beans.Property;
import org.joda.beans.PropertyDefinition;
import org.joda.beans.impl.direct.DirectFieldsBeanBuilder;
import org.joda.beans.impl.direct.DirectMetaBean;
import org.joda.beans.impl.direct.DirectMetaProperty;
import org.joda.beans.impl.direct.DirectMetaPropertyMap;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import com.opengamma.sesame.function.Parameter;
import com.opengamma.util.ArgumentChecker;

/**
 * Configuration for individual functions in the function model.
 * <p>
 * Provides the implementation types for function interfaces and the
 * arguments for creating function instances.
 */
@BeanDefinition(builderScope = "private")
public class FunctionModelConfig implements ImmutableBean {

  /**
   * Singleton instance of an empty configuration.
   * <p>
   * Always returns a null implementation class and empty arguments.
   */
  public static final FunctionModelConfig EMPTY =
      new FunctionModelConfig(ImmutableMap.<Class<?>, Class<?>>of(), ImmutableMap.<Class<?>, FunctionArguments>of());

  /**
   * The function implementation classes keyed by function interface.
   * This only needs to be populated if the implementation is not the default.
   */
  @PropertyDefinition(validate = "notNull")
  private final ImmutableMap<Class<?>, Class<?>> _implementations;

  /** The user-specified function arguments keyed by function implementation. */
  @PropertyDefinition(validate = "notNull")
  private final ImmutableMap<Class<?>, FunctionArguments> _arguments;

  /**
   * Decorator types, keyed by the type of the function interface they decorate.
   * <p>
   * The first decorator in the set is the first one that is invoked, i.e. the outermost. Each decorator
   * delegates to the next decorator except the last, which delegates to the underlying function.
   */
  @PropertyDefinition(validate = "notNull", get = "private")
  private final Map<Class<?>, LinkedHashSet<Class<?>>> _decoratorsByFn;

  /**
   * Function implementation classes keyed by the parameter where they are injected.
   * The parameter is a constructor parameter of a decorator and the class is the type being decorated.
   * The value class can be a decorator if there are multiple decorators attached to the same function.
   */
  private final ImmutableMap<Parameter, Class<?>> _implementationByParameter;

  @ImmutableConstructor
  private FunctionModelConfig(Map<Class<?>, Class<?>> implementations,
                              Map<Class<?>, FunctionArguments> arguments,
                              Map<Class<?>, LinkedHashSet<Class<?>>> decoratorsByFn) {
    _implementations = ImmutableMap.copyOf(implementations);
    _arguments = ImmutableMap.copyOf(arguments);
    _decoratorsByFn = ImmutableMap.copyOf(decoratorsByFn);

    ImmutableMap.Builder<Parameter, Class<?>> builder = ImmutableMap.builder();

    for (Map.Entry<Class<?>, LinkedHashSet<Class<?>>> entry : decoratorsByFn.entrySet()) {
      Class<?> functionInterface = entry.getKey();
      Set<Class<?>> decorators = entry.getValue();
      // make a list of decorators because we need random access
      List<Class<?>> decoratorList = new ArrayList<>(decorators);

      // use (size - 1) because we want to skip the last element in the list
      // the last decorator in the list is the one that delegates to the real function
      for (int i = 0; i < decoratorList.size() - 1; i++) {
        Class<?> firstDecoratorType = decoratorList.get(i);
        Class<?> secondDecoratorType = decoratorList.get(i + 1);
        Constructor<?> constructor = EngineUtils.getConstructor(firstDecoratorType);
        // the constructor parameter where the delegate is passed to the decorator
        Parameter delegateParameter = Parameter.ofType(functionInterface, constructor);
        builder.put(delegateParameter, secondDecoratorType);
      }
      Class<?> lastDecoratorType = decoratorList.get(decoratorList.size() - 1);
      Constructor<?> constructor = EngineUtils.getConstructor(lastDecoratorType);
      Parameter delegateParameter = Parameter.ofType(functionInterface, constructor);
      Class<?> functionImpl = _implementations.get(functionInterface);

      // for the last decorator I want to use the function type to look up in implementations
      if (functionImpl != null) {
        builder.put(delegateParameter, functionImpl);
      }
    }
    _implementationByParameter = builder.build();
  }

  /**
   * Creates new configuration using the specified function implementations and arguments.
   *
   * @param implementations  the implementation classes to use for functions, keyed by function interface
   * @param arguments  the arguments for the function implementations, keyed by implementation type
   */
  public FunctionModelConfig(Map<Class<?>, Class<?>> implementations, Map<Class<?>, FunctionArguments> arguments) {
    this(implementations, arguments, Collections.<Class<?>, LinkedHashSet<Class<?>>>emptyMap());
  }

  /**
   * Creates new configuration using the specified function implementations.
   *
   * @param implementations  the implementation classes to use for functions, keyed by function interface
   */
  public FunctionModelConfig(Map<Class<?>, Class<?>> implementations) {
    this(implementations, Collections.<Class<?>, FunctionArguments>emptyMap());
  }

  /**
   * Gets the implementation that should be used for creating instances of a type for injecting into a constructor.
   * <p>
   * The result implementation can be:
   * <ul>
   *   <li>An implementation of an interface</li>
   *   <li>A {@link Provider} that can provide the implementation</li>
   * </ul>
   *
   * @param parameter the constructor parameter for which an implementation is required
   * @return the implementation that should be used, null if unknown
   */
  public Class<?> getFunctionImplementation(@Nullable Parameter parameter, Class<?> functionType) {
    ArgumentChecker.notNull(functionType, "functionType");

    if (parameter != null) {
      Class<?> type = _implementationByParameter.get(parameter);

      if (type != null) {
        return type;
      }
    }
    LinkedHashSet<Class<?>> decorators = _decoratorsByFn.get(functionType);

    if (decorators != null) {
      return decorators.iterator().next();
    }
    return _implementations.get(functionType);
  }

  /**
   * Gets the arguments for a function.
   *
   * @param functionType  the type of function, not null
   * @return the arguments, empty if not found, not null
   */
  public FunctionArguments getFunctionArguments(Class<?> functionType) {
    FunctionArguments functionArguments = _arguments.get(functionType);
    return functionArguments == null ? FunctionArguments.EMPTY : functionArguments;
  }

  /**
   * Merges this configuration with another set, this configuration takes priority where there are duplicates.
   *
   * @param other configuration to merge with
   * @return the union of the configuration with settings from this instance taking priority
   */
  public FunctionModelConfig mergedWith(FunctionModelConfig other, FunctionModelConfig... others) {
    ArgumentChecker.notNull(other, "other");
    FunctionModelConfig config = this.merge(other);

    for (FunctionModelConfig otherConfig : others) {
      config = config.merge(otherConfig);
    }
    return config;
  }

  private FunctionModelConfig merge(FunctionModelConfig other) {
    Map<Class<?>, Class<?>> implementations = new HashMap<>(other._implementations);
    implementations.putAll(_implementations);

    Set<Class<?>> functionTypesWithArgs = Sets.union(_arguments.keySet(), other._arguments.keySet());
    Map<Class<?>, FunctionArguments> arguments = new HashMap<>();
    for (Class<?> fnType : functionTypesWithArgs) {
      arguments.put(fnType, mergeArguments(_arguments.get(fnType), other._arguments.get(fnType)));
    }

    Set<Class<?>> decoratedFunctionTypes = Sets.union(_decoratorsByFn.keySet(), other._decoratorsByFn.keySet());
    Map<Class<?>, LinkedHashSet<Class<?>>> decoratorsByFn = new HashMap<>();
    for (Class<?> fnType : decoratedFunctionTypes) {
      decoratorsByFn.put(fnType, mergeDecorators(_decoratorsByFn.get(fnType), other._decoratorsByFn.get(fnType)));
    }

    return new FunctionModelConfig(implementations, arguments, decoratorsByFn);
  }

  /**
   * Returns a copy of this configuration decorated with a decorator.
   *
   * @param decorator  a decorator type
   * @return  a copy of this configuration decorated with the decorator
   */
  public FunctionModelConfig decoratedWith(Class<?> decorator) {
    return decoratedWith(decorator, Collections.<Class<?>, FunctionArguments>emptyMap());
  }

  /**
   * Returns a copy of this configuration decorated with a decorator.
   *
   * @param decorator  a decorator type
   * @param arguments  function arguments for building the decorator instance
   * @return  a copy of this configuration decorated with the decorator
   */
  public FunctionModelConfig decoratedWith(Class<?> decorator, Map<Class<?>, FunctionArguments> arguments) {
    ArgumentChecker.notNull(decorator, "decorator");

    Set<Class<?>> functionTypesWithArgs = Sets.union(_arguments.keySet(), arguments.keySet());
    Map<Class<?>, FunctionArguments> mergedArguments = new HashMap<>();
    for (Class<?> fnType : functionTypesWithArgs) {
      mergedArguments.put(fnType, mergeArguments(_arguments.get(fnType), arguments.get(fnType)));
    }

    // get the set of interfaces implemented by the decorator - only one is supported at the moment
    Set<Class<?>> interfaces = EngineUtils.getInterfaces(decorator);

    if (interfaces.size() != 1) {
      throw new IllegalArgumentException("Decorator class " + decorator.getName() + " must implement exactly one interface");
    }
    Class<?> interfaceType = interfaces.iterator().next();
    LinkedHashSet<Class<?>> decorators = _decoratorsByFn.get(interfaceType);
    LinkedHashSet<Class<?>> mergedDecorators = new LinkedHashSet<>();

    mergedDecorators.add(decorator);
    if (decorators != null) {
      mergedDecorators.addAll(decorators);
    }
    Map<Class<?>, LinkedHashSet<Class<?>>> decoratorsByFn = new HashMap<>(_decoratorsByFn);
    decoratorsByFn.put(interfaceType, mergedDecorators);

    return new FunctionModelConfig(_implementations, mergedArguments, decoratorsByFn);
  }

  /**
   * Null safe merge of arguments. Both arguments are nullable but only one can be null in any given call.
   * If there are duplicates the arguments from {@code args1} take priority.
   *
   * @param args1  some arguments
   * @param args2  some arguments
   * @return  the merged arguments
   */
  private static FunctionArguments mergeArguments(@Nullable FunctionArguments args1, @Nullable FunctionArguments args2) {
    if (args1 == null) {
      return args2;
    } else if (args2 == null) {
      return args1;
    } else {
      return args1.mergeWith(args2);
    }
  }

  /**
   * Null safe merge of decorators. Both arguments are nullable but only one can be null in any given call.
   *
   * @param decorators1  the first set of decorator types
   * @param decorators2  the second set of decorator types
   * @return  the merged set of decorator types
   */
  private static LinkedHashSet<Class<?>> mergeDecorators(@Nullable LinkedHashSet<Class<?>> decorators1,
                                                         @Nullable LinkedHashSet<Class<?>> decorators2) {
    if (decorators1 == null) {
      return decorators2;
    } else if (decorators2 == null) {
      return decorators1;
    } else {
      LinkedHashSet<Class<?>> merged = new LinkedHashSet<>();
      merged.addAll(decorators1);
      merged.addAll(decorators2);
      return merged;
    }
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code FunctionModelConfig}.
   * @return the meta-bean, not null
   */
  public static FunctionModelConfig.Meta meta() {
    return FunctionModelConfig.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(FunctionModelConfig.Meta.INSTANCE);
  }

  @Override
  public FunctionModelConfig.Meta metaBean() {
    return FunctionModelConfig.Meta.INSTANCE;
  }

  @Override
  public <R> Property<R> property(String propertyName) {
    return metaBean().<R>metaProperty(propertyName).createProperty(this);
  }

  @Override
  public Set<String> propertyNames() {
    return metaBean().metaPropertyMap().keySet();
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the function implementation classes keyed by function interface.
   * This only needs to be populated if the implementation is not the default.
   * @return the value of the property, not null
   */
  public ImmutableMap<Class<?>, Class<?>> getImplementations() {
    return _implementations;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the user-specified function arguments keyed by function implementation.
   * @return the value of the property, not null
   */
  public ImmutableMap<Class<?>, FunctionArguments> getArguments() {
    return _arguments;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets decorator types, keyed by the type of the function interface they decorate.
   * <p>
   * The first decorator in the set is the first one that is invoked, i.e. the outermost. Each decorator
   * delegates to the next decorator except the last, which delegates to the underlying function.
   * @return the value of the property, not null
   */
  private Map<Class<?>, LinkedHashSet<Class<?>>> getDecoratorsByFn() {
    return _decoratorsByFn;
  }

  //-----------------------------------------------------------------------
  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      FunctionModelConfig other = (FunctionModelConfig) obj;
      return JodaBeanUtils.equal(getImplementations(), other.getImplementations()) &&
          JodaBeanUtils.equal(getArguments(), other.getArguments()) &&
          JodaBeanUtils.equal(getDecoratorsByFn(), other.getDecoratorsByFn());
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = getClass().hashCode();
    hash += hash * 31 + JodaBeanUtils.hashCode(getImplementations());
    hash += hash * 31 + JodaBeanUtils.hashCode(getArguments());
    hash += hash * 31 + JodaBeanUtils.hashCode(getDecoratorsByFn());
    return hash;
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(128);
    buf.append("FunctionModelConfig{");
    int len = buf.length();
    toString(buf);
    if (buf.length() > len) {
      buf.setLength(buf.length() - 2);
    }
    buf.append('}');
    return buf.toString();
  }

  protected void toString(StringBuilder buf) {
    buf.append("implementations").append('=').append(JodaBeanUtils.toString(getImplementations())).append(',').append(' ');
    buf.append("arguments").append('=').append(JodaBeanUtils.toString(getArguments())).append(',').append(' ');
    buf.append("decoratorsByFn").append('=').append(JodaBeanUtils.toString(getDecoratorsByFn())).append(',').append(' ');
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code FunctionModelConfig}.
   */
  public static class Meta extends DirectMetaBean {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code implementations} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<ImmutableMap<Class<?>, Class<?>>> _implementations = DirectMetaProperty.ofImmutable(
        this, "implementations", FunctionModelConfig.class, (Class) ImmutableMap.class);
    /**
     * The meta-property for the {@code arguments} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<ImmutableMap<Class<?>, FunctionArguments>> _arguments = DirectMetaProperty.ofImmutable(
        this, "arguments", FunctionModelConfig.class, (Class) ImmutableMap.class);
    /**
     * The meta-property for the {@code decoratorsByFn} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<Map<Class<?>, LinkedHashSet<Class<?>>>> _decoratorsByFn = DirectMetaProperty.ofImmutable(
        this, "decoratorsByFn", FunctionModelConfig.class, (Class) Map.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, null,
        "implementations",
        "arguments",
        "decoratorsByFn");

    /**
     * Restricted constructor.
     */
    protected Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case 643812097:  // implementations
          return _implementations;
        case -2035517098:  // arguments
          return _arguments;
        case -1632890057:  // decoratorsByFn
          return _decoratorsByFn;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends FunctionModelConfig> builder() {
      return new FunctionModelConfig.Builder();
    }

    @Override
    public Class<? extends FunctionModelConfig> beanType() {
      return FunctionModelConfig.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return _metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code implementations} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<ImmutableMap<Class<?>, Class<?>>> implementations() {
      return _implementations;
    }

    /**
     * The meta-property for the {@code arguments} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<ImmutableMap<Class<?>, FunctionArguments>> arguments() {
      return _arguments;
    }

    /**
     * The meta-property for the {@code decoratorsByFn} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Map<Class<?>, LinkedHashSet<Class<?>>>> decoratorsByFn() {
      return _decoratorsByFn;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case 643812097:  // implementations
          return ((FunctionModelConfig) bean).getImplementations();
        case -2035517098:  // arguments
          return ((FunctionModelConfig) bean).getArguments();
        case -1632890057:  // decoratorsByFn
          return ((FunctionModelConfig) bean).getDecoratorsByFn();
      }
      return super.propertyGet(bean, propertyName, quiet);
    }

    @Override
    protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {
      metaProperty(propertyName);
      if (quiet) {
        return;
      }
      throw new UnsupportedOperationException("Property cannot be written: " + propertyName);
    }

  }

  //-----------------------------------------------------------------------
  /**
   * The bean-builder for {@code FunctionModelConfig}.
   */
  private static class Builder extends DirectFieldsBeanBuilder<FunctionModelConfig> {

    private Map<Class<?>, Class<?>> _implementations = new HashMap<Class<?>, Class<?>>();
    private Map<Class<?>, FunctionArguments> _arguments = new HashMap<Class<?>, FunctionArguments>();
    private Map<Class<?>, LinkedHashSet<Class<?>>> _decoratorsByFn = new HashMap<Class<?>, LinkedHashSet<Class<?>>>();

    /**
     * Restricted constructor.
     */
    protected Builder() {
    }

    //-----------------------------------------------------------------------
    @Override
    public Object get(String propertyName) {
      switch (propertyName.hashCode()) {
        case 643812097:  // implementations
          return _implementations;
        case -2035517098:  // arguments
          return _arguments;
        case -1632890057:  // decoratorsByFn
          return _decoratorsByFn;
        default:
          throw new NoSuchElementException("Unknown property: " + propertyName);
      }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Builder set(String propertyName, Object newValue) {
      switch (propertyName.hashCode()) {
        case 643812097:  // implementations
          this._implementations = (Map<Class<?>, Class<?>>) newValue;
          break;
        case -2035517098:  // arguments
          this._arguments = (Map<Class<?>, FunctionArguments>) newValue;
          break;
        case -1632890057:  // decoratorsByFn
          this._decoratorsByFn = (Map<Class<?>, LinkedHashSet<Class<?>>>) newValue;
          break;
        default:
          throw new NoSuchElementException("Unknown property: " + propertyName);
      }
      return this;
    }

    @Override
    public Builder set(MetaProperty<?> property, Object value) {
      super.set(property, value);
      return this;
    }

    @Override
    public Builder setString(String propertyName, String value) {
      setString(meta().metaProperty(propertyName), value);
      return this;
    }

    @Override
    public Builder setString(MetaProperty<?> property, String value) {
      super.setString(property, value);
      return this;
    }

    @Override
    public Builder setAll(Map<String, ? extends Object> propertyValueMap) {
      super.setAll(propertyValueMap);
      return this;
    }

    @Override
    public FunctionModelConfig build() {
      return new FunctionModelConfig(
          _implementations,
          _arguments,
          _decoratorsByFn);
    }

    //-----------------------------------------------------------------------
    @Override
    public String toString() {
      StringBuilder buf = new StringBuilder(128);
      buf.append("FunctionModelConfig.Builder{");
      int len = buf.length();
      toString(buf);
      if (buf.length() > len) {
        buf.setLength(buf.length() - 2);
      }
      buf.append('}');
      return buf.toString();
    }

    protected void toString(StringBuilder buf) {
      buf.append("implementations").append('=').append(JodaBeanUtils.toString(_implementations)).append(',').append(' ');
      buf.append("arguments").append('=').append(JodaBeanUtils.toString(_arguments)).append(',').append(' ');
      buf.append("decoratorsByFn").append('=').append(JodaBeanUtils.toString(_decoratorsByFn)).append(',').append(' ');
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
