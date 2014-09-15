/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.function.scenarios;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import org.joda.beans.Bean;
import org.joda.beans.BeanDefinition;
import org.joda.beans.ImmutableBean;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaProperty;
import org.joda.beans.Property;
import org.joda.beans.PropertyDefinition;
import org.joda.beans.impl.direct.DirectFieldsBeanBuilder;
import org.joda.beans.impl.direct.DirectMetaBean;
import org.joda.beans.impl.direct.DirectMetaProperty;
import org.joda.beans.impl.direct.DirectMetaPropertyMap;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ListMultimap;
import com.opengamma.sesame.config.ViewConfig;
import com.opengamma.util.ArgumentChecker;

/**
 * Definition of a scenario - a set of transformations to apply to values when performing calculations.
 * <p>
 * A definition contains multiple instances of {@link ScenarioArgument}, each of which defines a transformation
 * to apply to calculated values. There is a set of arguments that apply to all calculations in a view
 * and sets that apply to individual columns or non-portfolio outputs.
 * <p>
 * A definition also knows which {@link ScenarioFunction} types must be added to the configuration to perform
 * the transformations. This allows a scenario definition to be applied to an existing view (see
 * {@link ViewConfig#withScenario(ScenarioDefinition)}).
 */
@BeanDefinition
public final class ScenarioDefinition implements ImmutableBean {

  /** Empty instance containing no arguments. */
  public static final ScenarioDefinition EMPTY = new ScenarioDefinition();

  /** Arguments for scenario functions, keyed by the type of the function that consumes them. */
  @PropertyDefinition(validate = "notNull", get = "private")
  private final ImmutableListMultimap<Class<? extends ScenarioFunction<?>>, ScenarioArgument<?>> _arguments;

  /**
   * Arguments for scenario functions that are only applied to specific columns, keyed by the column name and
   * the type of the function that consumes them.
   */
  @PropertyDefinition(validate = "notNull", get = "private")
  private final ImmutableListMultimap<ScenarioArgumentKey, ScenarioArgument<?>> _columnArguments;

  /**
   * Creates a new definition with arguments that will apply to all calculations
   *
   * @param arguments  scenario arguments defining transformations with will be applied to all calculations
   */
  public ScenarioDefinition(ScenarioArgument<?>... arguments) {
    this(Arrays.asList(arguments), Collections.<String, List<ScenarioArgument<?>>>emptyMap());
  }

  /**
   * Creates a new definition with arguments that will apply to all calculations
   *
   * @param arguments  scenario arguments defining transformations with will be applied to all calculations
   */
  public ScenarioDefinition(List<ScenarioArgument<?>> arguments) {
    this(arguments, Collections.<String, List<ScenarioArgument<?>>>emptyMap());
  }

  /**
   * Creates a new definition with some arguments that will be used for all calculations and some that will only
   * be used for specific named columns or non-portfolio outputs.
   *
   * @param arguments  scenario arguments defining transformations with will be applied to all calculations
   * @param columnArguments  scenario arguments defining transformations with will be applied to specific named
   *   columns or non-portfolio outputs
   */
  public ScenarioDefinition(List<ScenarioArgument<?>> arguments,
                            Map<String, List<ScenarioArgument<?>>> columnArguments) {
    _arguments = buildArguments(arguments);
    _columnArguments = buildColumnArguments(columnArguments);
  }

  private ScenarioDefinition(List<ScenarioArgument<?>> arguments,
                             ImmutableListMultimap<ScenarioArgumentKey, ScenarioArgument<?>> columnArguments) {
    _arguments = buildArguments(arguments);
    _columnArguments = columnArguments;
  }

  /**
   * Helper method to build a multimap for the {@link #_columnArguments} field.
   *
   * @param columnArguments  map of arguments, keyed by the name of the column or non-portfolio output to which
   *   they apply
   * @return  multimap of the arguments suitable for the {@link #_columnArguments} field
   */
  private static ImmutableListMultimap<ScenarioArgumentKey, ScenarioArgument<?>> buildColumnArguments(
      Map<String, List<ScenarioArgument<?>>> columnArguments) {

    ImmutableListMultimap.Builder<ScenarioArgumentKey, ScenarioArgument<?>> colArgBuilder =
        ImmutableListMultimap.builder();

    for (Map.Entry<String, List<ScenarioArgument<?>>> entry : columnArguments.entrySet()) {
      String columnName = entry.getKey();
      List<ScenarioArgument<?>> colArgs = entry.getValue();

      for (ScenarioArgument<?> colArg : colArgs) {
        ScenarioArgumentKey key = new ScenarioArgumentKey(columnName, colArg.getFunctionType());
        colArgBuilder.put(key, colArg);
      }
    }
    return colArgBuilder.build();
  }

  /**
   * Helper method to build a multimap for the {@link #_arguments} field.
   *
   * @param arguments  list of scenario arguments defining transformations to apply to calculated values
   * @return  multimap of the arguments suitable for the {@link #_arguments} field
   */
  private static ImmutableListMultimap<Class<? extends ScenarioFunction<?>>, ScenarioArgument<?>> buildArguments(
      List<ScenarioArgument<?>> arguments) {

    ArgumentChecker.notNull(arguments, "arguments");

    ImmutableListMultimap.Builder<Class<? extends ScenarioFunction<?>>, ScenarioArgument<?>> argBuilder =
        ImmutableListMultimap.builder();

    for (ScenarioArgument<?> argument : arguments) {
      argBuilder.put(argument.getFunctionType(), argument);
    }
    return argBuilder.build();
  }

  /**
   * Builds a scenario definition by merging this definition with another.
   *
   * @param other  another scenario definition
   * @return  a new definition containing the arguments from both definitions
   */
  public ScenarioDefinition mergedWith(ScenarioDefinition other) {
    ArgumentChecker.notNull(other, "other");

    // merge the arguments that apply to all columns / non-portfolio outputs
    ImmutableListMultimap<Class<? extends ScenarioFunction<?>>, ScenarioArgument<?>> arguments;

    if (_arguments.isEmpty()) {
      arguments = other._arguments;
    } else if (other._arguments.isEmpty()) {
      arguments = _arguments;
    } else {
      ImmutableListMultimap.Builder<Class<? extends ScenarioFunction<?>>, ScenarioArgument<?>> builder =
          ImmutableListMultimap.builder();
      arguments = builder.putAll(other._arguments).putAll(_arguments).build();
    }

    // merge the arguments targeted at specific columns or non-portfolio outputs
    ImmutableListMultimap<ScenarioArgumentKey, ScenarioArgument<?>> columnArguments;

    if (_columnArguments.isEmpty()) {
      columnArguments = other._columnArguments;
    } else if (other._columnArguments.isEmpty()) {
      columnArguments = _columnArguments;
    } else {
      ImmutableListMultimap.Builder<ScenarioArgumentKey, ScenarioArgument<?>> builder = ImmutableListMultimap.builder();
      columnArguments = builder.putAll(other._columnArguments).putAll(_columnArguments).build();
    }

    return new ScenarioDefinition(arguments, columnArguments);
  }

  /**
   * Builds a new definition using this definition's arguments combined with additional arguments.
   *
   * @param arguments  some arguments
   * @return  a new definition containing this definition's arguments and the arguments passed in
   */
  public ScenarioDefinition with(List<ScenarioArgument<?>> arguments) {
    List<ScenarioArgument<?>> newArgs = new ArrayList<>(_arguments.values());
    newArgs.addAll(arguments);
    return new ScenarioDefinition(newArgs, _columnArguments);
  }

  /**
   * Builds a new definition using this definition's arguments combined with additional arguments.
   *
   * @param arguments  some arguments
   * @return  a new definition containing this definition's arguments and the arguments passed in
   */
  public ScenarioDefinition with(ScenarioArgument<?>... arguments) {
    return with(Arrays.asList(arguments));
  }

  /**
   * Builds a new definition using this definition's arguments combined with additional arguments.
   *
   * @param arguments  the arguments
   * @param names  the names of the columns or non-portfolio outputs to which they should be applied
   * @return  a new definition using this definition's arguments combined with the additional arguments
   */
  public ScenarioDefinition with(List<ScenarioArgument<?>> arguments, String... names) {
    ListMultimap<ScenarioArgumentKey, ScenarioArgument<?>> colArgs = ArrayListMultimap.create(_columnArguments);

    for (String name : names) {
      for (ScenarioArgument<?> argument : arguments) {
        colArgs.put(new ScenarioArgumentKey(name, argument.getFunctionType()), argument);
      }
    }
    return new ScenarioDefinition(_arguments, colArgs);
  }

  /**
   * Builds a new definition using this definition's arguments combined with an additional argument.
   *
   * @param argument  the argument
   * @param names  the names of the columns or non-portfolio outputs to which it should be applied
   * @return  a new definition using this definition's arguments combined with the additional argument
   */
  public ScenarioDefinition with(ScenarioArgument<?> argument, String... names) {
    return with(ImmutableList.<ScenarioArgument<?>>of(argument), names);
  }

  /**
   * Returns a copy containing the subset of arguments for the specified column or non-portfolio output.
   *
   * @param name  the name of the column or non-portfolio output
   * @return  scenario definition containing only the arguments that apply to the named column or output
   */
  public FilteredScenarioDefinition filter(String name) {
    ArgumentChecker.notEmpty(name, "name");

    List<ScenarioArgument<?>> args = new ArrayList<>();

    // only add the column arguments with matching names
    for (Map.Entry<ScenarioArgumentKey, ScenarioArgument<?>> entry : _columnArguments.entries()) {
      if (entry.getKey().getName().equals(name)) {
        args.add(entry.getValue());
      }
    }
    // _arguments apply to all columns
    for (ScenarioArgument<?> argument : _arguments.values()) {
      args.add(argument);
    }
    return new FilteredScenarioDefinition(args);
  }

  /**
   * Returns the scenario functions required to execute this scenario.
   * <p>
   * This information allows a scenario to be applied to an existing {@link ViewConfig}. The arguments in
   * the definition describe the transformations to apply to the data and this method specifies the functions
   * that must be added to the view configuration to perform the transformations.
   *
   * @return  the scenario functions required to execute this scenario
   */
  public Set<Class<? extends ScenarioFunction<?>>> getFunctionTypes() {
    Set<Class<? extends ScenarioFunction<?>>> functionTypes = new HashSet<>(_arguments.keySet());

    for (ScenarioArgumentKey pair : _columnArguments.keySet()) {
      functionTypes.add(pair.getFunctionType());
    }
    return functionTypes;
  }

  /**
   * @return  whether this definition is empty
   */
  public boolean isEmpty() {
    return _arguments.isEmpty() && _columnArguments.isEmpty();
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code ScenarioDefinition}.
   * @return the meta-bean, not null
   */
  public static ScenarioDefinition.Meta meta() {
    return ScenarioDefinition.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(ScenarioDefinition.Meta.INSTANCE);
  }

  /**
   * Returns a builder used to create an instance of the bean.
   * @return the builder, not null
   */
  public static ScenarioDefinition.Builder builder() {
    return new ScenarioDefinition.Builder();
  }

  private ScenarioDefinition(
      ListMultimap<Class<? extends ScenarioFunction<?>>, ScenarioArgument<?>> arguments,
      ListMultimap<ScenarioArgumentKey, ScenarioArgument<?>> columnArguments) {
    JodaBeanUtils.notNull(arguments, "arguments");
    JodaBeanUtils.notNull(columnArguments, "columnArguments");
    this._arguments = ImmutableListMultimap.copyOf(arguments);
    this._columnArguments = ImmutableListMultimap.copyOf(columnArguments);
  }

  @Override
  public ScenarioDefinition.Meta metaBean() {
    return ScenarioDefinition.Meta.INSTANCE;
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
   * Gets arguments for scenario functions, keyed by the type of the function that consumes them.
   * @return the value of the property, not null
   */
  private ImmutableListMultimap<Class<? extends ScenarioFunction<?>>, ScenarioArgument<?>> getArguments() {
    return _arguments;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets arguments for scenario functions that are only applied to specific columns, keyed by the column name and
   * the type of the function that consumes them.
   * @return the value of the property, not null
   */
  private ImmutableListMultimap<ScenarioArgumentKey, ScenarioArgument<?>> getColumnArguments() {
    return _columnArguments;
  }

  //-----------------------------------------------------------------------
  /**
   * Returns a builder that allows this bean to be mutated.
   * @return the mutable builder, not null
   */
  public Builder toBuilder() {
    return new Builder(this);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      ScenarioDefinition other = (ScenarioDefinition) obj;
      return JodaBeanUtils.equal(getArguments(), other.getArguments()) &&
          JodaBeanUtils.equal(getColumnArguments(), other.getColumnArguments());
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = getClass().hashCode();
    hash += hash * 31 + JodaBeanUtils.hashCode(getArguments());
    hash += hash * 31 + JodaBeanUtils.hashCode(getColumnArguments());
    return hash;
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(96);
    buf.append("ScenarioDefinition{");
    buf.append("arguments").append('=').append(getArguments()).append(',').append(' ');
    buf.append("columnArguments").append('=').append(JodaBeanUtils.toString(getColumnArguments()));
    buf.append('}');
    return buf.toString();
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code ScenarioDefinition}.
   */
  public static final class Meta extends DirectMetaBean {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code arguments} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<ImmutableListMultimap<Class<? extends ScenarioFunction<?>>, ScenarioArgument<?>>> _arguments = DirectMetaProperty.ofImmutable(
        this, "arguments", ScenarioDefinition.class, (Class) ImmutableListMultimap.class);
    /**
     * The meta-property for the {@code columnArguments} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<ImmutableListMultimap<ScenarioArgumentKey, ScenarioArgument<?>>> _columnArguments = DirectMetaProperty.ofImmutable(
        this, "columnArguments", ScenarioDefinition.class, (Class) ImmutableListMultimap.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, null,
        "arguments",
        "columnArguments");

    /**
     * Restricted constructor.
     */
    private Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case -2035517098:  // arguments
          return _arguments;
        case 763963040:  // columnArguments
          return _columnArguments;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public ScenarioDefinition.Builder builder() {
      return new ScenarioDefinition.Builder();
    }

    @Override
    public Class<? extends ScenarioDefinition> beanType() {
      return ScenarioDefinition.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return _metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code arguments} property.
     * @return the meta-property, not null
     */
    public MetaProperty<ImmutableListMultimap<Class<? extends ScenarioFunction<?>>, ScenarioArgument<?>>> arguments() {
      return _arguments;
    }

    /**
     * The meta-property for the {@code columnArguments} property.
     * @return the meta-property, not null
     */
    public MetaProperty<ImmutableListMultimap<ScenarioArgumentKey, ScenarioArgument<?>>> columnArguments() {
      return _columnArguments;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -2035517098:  // arguments
          return ((ScenarioDefinition) bean).getArguments();
        case 763963040:  // columnArguments
          return ((ScenarioDefinition) bean).getColumnArguments();
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
   * The bean-builder for {@code ScenarioDefinition}.
   */
  public static final class Builder extends DirectFieldsBeanBuilder<ScenarioDefinition> {

    private ListMultimap<Class<? extends ScenarioFunction<?>>, ScenarioArgument<?>> _arguments = ArrayListMultimap.create();
    private ListMultimap<ScenarioArgumentKey, ScenarioArgument<?>> _columnArguments = ArrayListMultimap.create();

    /**
     * Restricted constructor.
     */
    private Builder() {
    }

    /**
     * Restricted copy constructor.
     * @param beanToCopy  the bean to copy from, not null
     */
    private Builder(ScenarioDefinition beanToCopy) {
      this._arguments = ArrayListMultimap.create(beanToCopy.getArguments());
      this._columnArguments = ArrayListMultimap.create(beanToCopy.getColumnArguments());
    }

    //-----------------------------------------------------------------------
    @Override
    public Object get(String propertyName) {
      switch (propertyName.hashCode()) {
        case -2035517098:  // arguments
          return _arguments;
        case 763963040:  // columnArguments
          return _columnArguments;
        default:
          throw new NoSuchElementException("Unknown property: " + propertyName);
      }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Builder set(String propertyName, Object newValue) {
      switch (propertyName.hashCode()) {
        case -2035517098:  // arguments
          this._arguments = (ListMultimap<Class<? extends ScenarioFunction<?>>, ScenarioArgument<?>>) newValue;
          break;
        case 763963040:  // columnArguments
          this._columnArguments = (ListMultimap<ScenarioArgumentKey, ScenarioArgument<?>>) newValue;
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
    public ScenarioDefinition build() {
      return new ScenarioDefinition(
          _arguments,
          _columnArguments);
    }

    //-----------------------------------------------------------------------
    /**
     * Sets the {@code arguments} property in the builder.
     * @param arguments  the new value, not null
     * @return this, for chaining, not null
     */
    public Builder arguments(ListMultimap<Class<? extends ScenarioFunction<?>>, ScenarioArgument<?>> arguments) {
      JodaBeanUtils.notNull(arguments, "arguments");
      this._arguments = arguments;
      return this;
    }

    /**
     * Sets the {@code columnArguments} property in the builder.
     * @param columnArguments  the new value, not null
     * @return this, for chaining, not null
     */
    public Builder columnArguments(ListMultimap<ScenarioArgumentKey, ScenarioArgument<?>> columnArguments) {
      JodaBeanUtils.notNull(columnArguments, "columnArguments");
      this._columnArguments = columnArguments;
      return this;
    }

    //-----------------------------------------------------------------------
    @Override
    public String toString() {
      StringBuilder buf = new StringBuilder(96);
      buf.append("ScenarioDefinition.Builder{");
      buf.append("arguments").append('=').append(JodaBeanUtils.toString(_arguments)).append(',').append(' ');
      buf.append("columnArguments").append('=').append(JodaBeanUtils.toString(_columnArguments));
      buf.append('}');
      return buf.toString();
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
