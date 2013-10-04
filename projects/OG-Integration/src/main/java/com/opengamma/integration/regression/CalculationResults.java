/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.regression;


import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.joda.beans.Bean;
import org.joda.beans.BeanDefinition;
import org.joda.beans.ImmutableBean;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaProperty;
import org.joda.beans.Property;
import org.joda.beans.PropertyDefinition;
import org.joda.beans.impl.BasicImmutableBeanBuilder;
import org.joda.beans.impl.direct.DirectMetaBean;
import org.joda.beans.impl.direct.DirectMetaProperty;
import org.joda.beans.impl.direct.DirectMetaPropertyMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.position.PortfolioNode;
import com.opengamma.core.position.PositionSource;
import com.opengamma.core.position.Trade;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.target.ComputationTargetReference;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ComputedValueResult;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.ViewComputationResultModel;
import com.opengamma.engine.view.ViewResultEntry;
import com.opengamma.engine.view.compilation.CompiledViewDefinition;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.util.ArgumentChecker;

/**
 *
 */
@BeanDefinition
public final class CalculationResults implements ImmutableBean {

  private static final Logger s_logger = LoggerFactory.getLogger(CalculationResults.class);
  private static final Pattern FUNCTION_PATTERN = Pattern.compile("\\d+ \\((.*)\\)");

  // TODO store the new UniqueId as well as the original? for generating a report will probably need security/trade names
  @PropertyDefinition(validate = "notNull")
  private final Map<CalculationResultKey, Object> _values;

  @PropertyDefinition(validate = "notNull")
  private final String _viewDefinitionName;

  @PropertyDefinition(validate = "notNull")
  private final String _snapshotName;

  // TODO test case
  public static CalculationResults create(CompiledViewDefinition viewDef,
                                          String snapshotName,
                                          ViewComputationResultModel results,
                                          PositionSource positionSource) {
    ArgumentChecker.notNull(viewDef, "viewDef");
    ArgumentChecker.notNull(results, "results");
    List<ViewResultEntry> allResults = results.getAllResults();
    Map<CalculationResultKey, Object> valueMap = Maps.newHashMapWithExpectedSize(allResults.size());
    Map<UniqueId, List<String>> nodesToPaths = nodesToPaths(viewDef.getPortfolio().getRootNode(),
                                                            Collections.<String>emptyList());
    for (ViewResultEntry entry : allResults) {
      ComputedValueResult computedValue = entry.getComputedValue();
      ValueSpecification valueSpec = computedValue.getSpecification();
      ComputationTargetSpecification targetSpec = valueSpec.getTargetSpecification();
      CalculationResultKey key = getResultKey(entry, valueSpec, targetSpec, nodesToPaths, positionSource);
      if (key != null) {
        valueMap.put(key, computedValue.getValue());
      }
    }
    return new CalculationResults(valueMap, viewDef.getViewDefinition().getName(), snapshotName);
  }

  private static CalculationResultKey getResultKey(ViewResultEntry entry,
                                                   ValueSpecification valueSpec,
                                                   ComputationTargetSpecification targetSpec,
                                                   Map<UniqueId, List<String>> nodesToPaths,
                                                   PositionSource positionSource) {
    CalculationResultKey key;
    ValueProperties properties = removeFunctionIds(valueSpec.getProperties());
    ComputationTargetType targetType = targetSpec.getType();
    if (targetType.equals(ComputationTargetType.POSITION)) {
      ComputationTargetReference nodeRef = targetSpec.getParent();
      UniqueId positionId = targetSpec.getUniqueId();
      String idAttr = positionSource.getPosition(positionId).getAttributes().get(DatabaseRestore.REGRESSION_ID);
      if (idAttr == null) {
        throw new OpenGammaRuntimeException("No ID attribute found for " + positionId);
      }
      // position targets can have a parent node but it's not guaranteed
      if (nodeRef != null) {
        UniqueId nodeId = nodeRef.getSpecification().getUniqueId();
        List<String> path = nodesToPaths.get(nodeId);
        key = CalculationResultKey.forPositionWithParentNode(entry.getCalculationConfiguration(),
                                                             valueSpec.getValueName(),
                                                             properties,
                                                             path,
                                                             ObjectId.parse(idAttr));
      } else {
        key = CalculationResultKey.forPosition(entry.getCalculationConfiguration(),
                                               valueSpec.getValueName(),
                                               properties,
                                               ObjectId.parse(idAttr));
      }
    } else if (targetType.equals(ComputationTargetType.PORTFOLIO_NODE)) {
      UniqueId nodeId = targetSpec.getUniqueId();
      List<String> path = nodesToPaths.get(nodeId);
      key = CalculationResultKey.forNode(entry.getCalculationConfiguration(), valueSpec.getValueName(), properties, path);
    } else if (targetType.equals(ComputationTargetType.TRADE)) {
      // TODO this assumes a trade target spec will never have a parent
      // this is true at the moment but subject to change. see PLAT-2286
      // and PortfolioCompilerTraversalCallback.preOrderOperation
      UniqueId tradeId = targetSpec.getUniqueId();
      Trade trade = positionSource.getTrade(tradeId);
      String idAttr = trade.getAttributes().get(DatabaseRestore.REGRESSION_ID);
      key = CalculationResultKey.forTrade(entry.getCalculationConfiguration(),
                                          valueSpec.getValueName(),
                                          properties,
                                          ObjectId.parse(idAttr));
    } else if (targetType.equals(ComputationTargetType.CURRENCY)) {
      key = CalculationResultKey.forCurrency(entry.getCalculationConfiguration(),
                                             valueSpec.getValueName(),
                                             properties,
                                             targetSpec.getUniqueId().getObjectId());
    } else {
      s_logger.warn("Ignoring target with type {}", targetType);
      key = null;
    }
    return key;
  }

  /**
   * The Function property contains an arbitrary function ID which is different between runs.
   * @param properties Properties to clean up
   * @return The properties with the ID removed from the function name property
   */
  private static ValueProperties removeFunctionIds(ValueProperties properties) {
    Set<String> functions = properties.getValues(ValuePropertyNames.FUNCTION);
    Set<String> functionsNoId = Sets.newHashSet();
    for (String function : functions) {
      functionsNoId.add(removeFunctionId(function));
    }
    return properties.copy().withoutAny(ValuePropertyNames.FUNCTION).with(ValuePropertyNames.FUNCTION, functionsNoId).get();
  }

  /**
   * Removes the ID from a function name, e.g. "123 (Function Name)" becomes "Function Name".
   * @return The function name string with the ID removed
   */
  private static String removeFunctionId(String functionString) {
    Matcher matcher = FUNCTION_PATTERN.matcher(functionString);
    if (matcher.matches()) {
      return matcher.group(1);
    } else {
      return functionString;
    }
  }

  // TODO test case
  private static Map<UniqueId, List<String>> nodesToPaths(PortfolioNode node, List<String> parentPath) {
    String name = node.getName();
    List<String> path = ImmutableList.<String>builder().addAll(parentPath).add(name).build();
    Map<UniqueId, List<String>> map = Maps.newHashMap();
    map.put(node.getUniqueId(), path);
    for (PortfolioNode childNode : node.getChildNodes()) {
      map.putAll(nodesToPaths(childNode, path));
    }
    return map;
  }
  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code CalculationResults}.
   * @return the meta-bean, not null
   */
  public static CalculationResults.Meta meta() {
    return CalculationResults.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(CalculationResults.Meta.INSTANCE);
  }

  /**
   * Returns a builder used to create an instance of the bean.
   *
   * @return the builder, not null
   */
  public static CalculationResults.Builder builder() {
    return new CalculationResults.Builder();
  }

  private CalculationResults(
      Map<CalculationResultKey, Object> values,
      String viewDefinitionName,
      String snapshotName) {
    JodaBeanUtils.notNull(values, "values");
    JodaBeanUtils.notNull(viewDefinitionName, "viewDefinitionName");
    JodaBeanUtils.notNull(snapshotName, "snapshotName");
    this._values = ImmutableMap.copyOf(values);
    this._viewDefinitionName = viewDefinitionName;
    this._snapshotName = snapshotName;
  }

  @Override
  public CalculationResults.Meta metaBean() {
    return CalculationResults.Meta.INSTANCE;
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
   * Gets the values.
   * @return the value of the property, not null
   */
  public Map<CalculationResultKey, Object> getValues() {
    return _values;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the viewDefinitionName.
   * @return the value of the property, not null
   */
  public String getViewDefinitionName() {
    return _viewDefinitionName;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the snapshotName.
   * @return the value of the property, not null
   */
  public String getSnapshotName() {
    return _snapshotName;
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
  public CalculationResults clone() {
    return this;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      CalculationResults other = (CalculationResults) obj;
      return JodaBeanUtils.equal(getValues(), other.getValues()) &&
          JodaBeanUtils.equal(getViewDefinitionName(), other.getViewDefinitionName()) &&
          JodaBeanUtils.equal(getSnapshotName(), other.getSnapshotName());
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = getClass().hashCode();
    hash += hash * 31 + JodaBeanUtils.hashCode(getValues());
    hash += hash * 31 + JodaBeanUtils.hashCode(getViewDefinitionName());
    hash += hash * 31 + JodaBeanUtils.hashCode(getSnapshotName());
    return hash;
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(128);
    buf.append("CalculationResults{");
    buf.append("values").append('=').append(getValues()).append(',').append(' ');
    buf.append("viewDefinitionName").append('=').append(getViewDefinitionName()).append(',').append(' ');
    buf.append("snapshotName").append('=').append(getSnapshotName());
    buf.append('}');
    return buf.toString();
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code CalculationResults}.
   */
  public static final class Meta extends DirectMetaBean {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code values} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<Map<CalculationResultKey, Object>> _values = DirectMetaProperty.ofImmutable(
        this, "values", CalculationResults.class, (Class) Map.class);
    /**
     * The meta-property for the {@code viewDefinitionName} property.
     */
    private final MetaProperty<String> _viewDefinitionName = DirectMetaProperty.ofImmutable(
        this, "viewDefinitionName", CalculationResults.class, String.class);
    /**
     * The meta-property for the {@code snapshotName} property.
     */
    private final MetaProperty<String> _snapshotName = DirectMetaProperty.ofImmutable(
        this, "snapshotName", CalculationResults.class, String.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, null,
        "values",
        "viewDefinitionName",
        "snapshotName");

    /**
     * Restricted constructor.
     */
    protected Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case -823812830:  // values
          return _values;
        case -10926973:  // viewDefinitionName
          return _viewDefinitionName;
        case -931708305:  // snapshotName
          return _snapshotName;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public CalculationResults.Builder builder() {
      return new CalculationResults.Builder();
    }

    @Override
    public Class<? extends CalculationResults> beanType() {
      return CalculationResults.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return _metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code values} property.
     * @return the meta-property, not null
     */
    public MetaProperty<Map<CalculationResultKey, Object>> values() {
      return _values;
    }

    /**
     * The meta-property for the {@code viewDefinitionName} property.
     * @return the meta-property, not null
     */
    public MetaProperty<String> viewDefinitionName() {
      return _viewDefinitionName;
    }

    /**
     * The meta-property for the {@code snapshotName} property.
     * @return the meta-property, not null
     */
    public MetaProperty<String> snapshotName() {
      return _snapshotName;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -823812830:  // values
          return ((CalculationResults) bean).getValues();
        case -10926973:  // viewDefinitionName
          return ((CalculationResults) bean).getViewDefinitionName();
        case -931708305:  // snapshotName
          return ((CalculationResults) bean).getSnapshotName();
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
   * The bean-builder for {@code CalculationResults}.
   */
  public static final class Builder extends BasicImmutableBeanBuilder<CalculationResults> {

    private Map<CalculationResultKey, Object> _values = new HashMap<CalculationResultKey, Object>();
    private String _viewDefinitionName;
    private String _snapshotName;

    /**
     * Restricted constructor.
     */
    private Builder() {
      super(CalculationResults.Meta.INSTANCE);
    }

    /**
     * Restricted copy constructor.
     * @param beanToCopy  the bean to copy from, not null
     */
    private Builder(CalculationResults beanToCopy) {
      super(CalculationResults.Meta.INSTANCE);
      this._values = new HashMap<CalculationResultKey, Object>(beanToCopy.getValues());
      this._viewDefinitionName = beanToCopy.getViewDefinitionName();
      this._snapshotName = beanToCopy.getSnapshotName();
    }

    //-----------------------------------------------------------------------
    @SuppressWarnings("unchecked")
    @Override
    public Builder set(String propertyName, Object newValue) {
      switch (propertyName.hashCode()) {
        case -823812830:  // values
          this._values = (Map<CalculationResultKey, Object>) newValue;
          break;
        case -10926973:  // viewDefinitionName
          this._viewDefinitionName = (String) newValue;
          break;
        case -931708305:  // snapshotName
          this._snapshotName = (String) newValue;
          break;
        default:
          throw new NoSuchElementException("Unknown property: " + propertyName);
      }
      return this;
    }

    @Override
    public CalculationResults build() {
      return new CalculationResults(
          _values,
          _viewDefinitionName,
          _snapshotName);
    }

    //-----------------------------------------------------------------------
    /**
     * Sets the {@code values} property in the builder.
     * @param values  the new value, not null
     * @return this, for chaining, not null
     */
    public Builder values(Map<CalculationResultKey, Object> values) {
      JodaBeanUtils.notNull(values, "values");
      this._values = values;
      return this;
    }

    /**
     * Sets the {@code viewDefinitionName} property in the builder.
     * @param viewDefinitionName  the new value, not null
     * @return this, for chaining, not null
     */
    public Builder viewDefinitionName(String viewDefinitionName) {
      JodaBeanUtils.notNull(viewDefinitionName, "viewDefinitionName");
      this._viewDefinitionName = viewDefinitionName;
      return this;
    }

    /**
     * Sets the {@code snapshotName} property in the builder.
     * @param snapshotName  the new value, not null
     * @return this, for chaining, not null
     */
    public Builder snapshotName(String snapshotName) {
      JodaBeanUtils.notNull(snapshotName, "snapshotName");
      this._snapshotName = snapshotName;
      return this;
    }

    //-----------------------------------------------------------------------
    @Override
    public String toString() {
      StringBuilder buf = new StringBuilder(128);
      buf.append("CalculationResults.Builder{");
      buf.append("values").append('=').append(_values).append(',').append(' ');
      buf.append("viewDefinitionName").append('=').append(_viewDefinitionName).append(',').append(' ');
      buf.append("snapshotName").append('=').append(_snapshotName);
      buf.append('}');
      return buf.toString();
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
