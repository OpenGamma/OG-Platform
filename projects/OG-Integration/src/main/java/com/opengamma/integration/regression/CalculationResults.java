/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.regression;


import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.Instant;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.opengamma.core.position.PortfolioNode;
import com.opengamma.core.position.Position;
import com.opengamma.core.position.PositionSource;
import com.opengamma.core.position.Trade;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.target.ComputationTargetReference;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ComputedValueResult;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.ViewComputationResultModel;
import com.opengamma.engine.view.ViewResultEntry;
import com.opengamma.engine.view.compilation.CompiledViewCalculationConfiguration;
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

  @PropertyDefinition(validate = "notNull")
  private final Map<CalculationResultKey, CalculatedValue> _values;

  @PropertyDefinition(validate = "notNull")
  private final String _viewDefinitionName;

  @PropertyDefinition(validate = "notNull")
  private final String _snapshotName;

  @PropertyDefinition(validate = "notNull")
  private final Instant _valuationTime;

  @PropertyDefinition
  private final String _version;

  public static CalculationResults create(ViewComputationResultModel results,
                                          CompiledViewDefinition viewDef,
                                          String snapshotName,
                                          Instant valuationTime,
                                          String version,
                                          PositionSource positionSource,
                                          SecuritySource securitySource) {
    ArgumentChecker.notNull(viewDef, "viewDef");
    ArgumentChecker.notNull(results, "results");
    List<ViewResultEntry> allResults = results.getAllResults();
    Map<CalculationResultKey, CalculatedValue> valueMap = Maps.newHashMapWithExpectedSize(allResults.size());
    Map<UniqueId, List<String>> nodesToPaths = nodesToPaths(viewDef.getPortfolio().getRootNode(),
                                                            Collections.<String>emptyList());
    for (ViewResultEntry entry : allResults) {
      ComputedValueResult computedValue = entry.getComputedValue();
      ValueSpecification valueSpec = computedValue.getSpecification();
      ComputationTargetSpecification targetSpec = valueSpec.getTargetSpecification();

      if (!targetSpec.getType().equals(ComputationTargetType.PORTFOLIO_NODE)) {
        String calcConfigName = entry.getCalculationConfiguration();
        CompiledViewCalculationConfiguration calcConfig = viewDef.getCompiledCalculationConfiguration(calcConfigName);
        Set<ValueRequirement> valueReqs = calcConfig.getTerminalOutputSpecifications().get(valueSpec);
        Set<CalculationResultKey> keys = getResultKey(entry, targetSpec, nodesToPaths, positionSource, valueReqs);
        String targetType = valueSpec.getTargetSpecification().getType().getName();
        String targetName = getTargetName(valueSpec.getTargetSpecification().getUniqueId(),
                                          valueSpec.getTargetSpecification().getType(),
                                          positionSource,
                                          securitySource,
                                          nodesToPaths);
        for (CalculationResultKey key : keys) {
          valueMap.put(key, CalculatedValue.of(computedValue.getValue(), valueSpec.getProperties(), targetType, targetName));
        }
      }
    }
    Map<CalculationResultKey, CalculatedValue> sortedValueMap = ImmutableSortedMap.copyOf(valueMap);
    return new CalculationResults(sortedValueMap, viewDef.getViewDefinition().getName(), snapshotName, valuationTime, version);
  }

  private static String getTargetName(UniqueId targetId,
                                      ComputationTargetType targetType,
                                      PositionSource positionSource,
                                      SecuritySource securitySource,
                                      Map<UniqueId, List<String>> nodesToPaths) {
    Security security;
    BigDecimal quantity;
    if (targetType.equals(ComputationTargetType.POSITION)) {
      Position position = positionSource.getPosition(targetId);
      security = position.getSecurityLink().resolve(securitySource);
      quantity = position.getQuantity();
    } else if (targetType.equals(ComputationTargetType.TRADE)) {
      Trade trade = positionSource.getTrade(targetId);
      security = trade.getSecurityLink().resolve(securitySource);
      quantity = trade.getQuantity();
    } else if (targetType.equals(ComputationTargetType.PORTFOLIO_NODE)) {
      List<String> path = nodesToPaths.get(targetId);
      return StringUtils.join(path, " / ");
    } else {
      return targetId.toString();
    }
    return quantity + " x " + security.getName();
  }

  // TODO use ValueRequirement for the key?
  // that should give far fewer false positives because
  //   a) the functions put things in the properties that cause breaks (e.g. unique IDs)
  //   b) there is so much ambiguity in graph building
  // or is it good to flag up the ambiguity?
  private static Set<CalculationResultKey> getResultKey(ViewResultEntry entry,
                                                        ComputationTargetSpecification targetSpec,
                                                        Map<UniqueId, List<String>> nodesToPaths,
                                                        PositionSource positionSource,
                                                        Set<ValueRequirement> valueReqs) {
    CalculationResultKey key;
    // TODO ugh. see AbstractTradeOrDailyPositionPnLFunction CostOfCarryTimeSeries
    Set<CalculationResultKey> keys = Sets.newHashSet();
    for (ValueRequirement valueReq : valueReqs) {
      String valueName = valueReq.getValueName();
      ValueProperties properties = valueReq.getConstraints();
      ComputationTargetType targetType = targetSpec.getType();
      if (targetType.equals(ComputationTargetType.POSITION)) {
        ComputationTargetReference nodeRef = targetSpec.getParent();
        UniqueId positionId = targetSpec.getUniqueId();
        String idAttr = positionSource.getPosition(positionId).getAttributes().get(DatabaseRestore.REGRESSION_ID);
        if (idAttr == null) {
          idAttr = positionId.getObjectId().toString();
        }
        // position targets can have a parent node but it's not guaranteed
        if (nodeRef != null) {
          UniqueId nodeId = nodeRef.getSpecification().getUniqueId();
          List<String> path = nodesToPaths.get(nodeId);
          key = CalculationResultKey.forPositionWithParentNode(entry.getCalculationConfiguration(),
                                                               valueName,
                                                               properties,
                                                               path,
                                                               ObjectId.parse(idAttr));
        } else {
          key = CalculationResultKey.forPosition(entry.getCalculationConfiguration(),
                                                 valueName,
                                                 properties,
                                                 ObjectId.parse(idAttr));
        }
      } else if (targetType.equals(ComputationTargetType.PORTFOLIO_NODE)) {
        UniqueId nodeId = targetSpec.getUniqueId();
        List<String> path = nodesToPaths.get(nodeId);
        key = CalculationResultKey.forNode(entry.getCalculationConfiguration(), valueName, properties, path);
      } else if (targetType.equals(ComputationTargetType.TRADE)) {
        // TODO this assumes a trade target spec will never have a parent
        // this is true at the moment but subject to change. see PLAT-2286
        // and PortfolioCompilerTraversalCallback.preOrderOperation
        UniqueId tradeId = targetSpec.getUniqueId();
        Trade trade = positionSource.getTrade(tradeId);
        String idAttr = trade.getAttributes().get(DatabaseRestore.REGRESSION_ID);
        if (idAttr == null) {
          idAttr = tradeId.getObjectId().toString();
        }
        key = CalculationResultKey.forTrade(entry.getCalculationConfiguration(),
                                            valueName,
                                            properties,
                                            ObjectId.parse(idAttr));
      } else if (targetType.equals(ComputationTargetType.CURRENCY)) {
        key = CalculationResultKey.forCurrency(entry.getCalculationConfiguration(),
                                               valueName,
                                               properties,
                                               targetSpec.getUniqueId().getObjectId());
      } else if (targetType.equals(ComputationTargetType.UNORDERED_CURRENCY_PAIR)) {
        key = CalculationResultKey.forCurrency(entry.getCalculationConfiguration(),
                                               valueName,
                                               properties,
                                               targetSpec.getUniqueId().getObjectId());
      } else {
        s_logger.warn("Ignoring target with type {}", targetType);
        key = null;
      }
      if (key != null) {
        keys.add(key);
      }
    }
    return keys;
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
   * @return the builder, not null
   */
  public static CalculationResults.Builder builder() {
    return new CalculationResults.Builder();
  }

  private CalculationResults(
      Map<CalculationResultKey, CalculatedValue> values,
      String viewDefinitionName,
      String snapshotName,
      Instant valuationTime,
      String version) {
    JodaBeanUtils.notNull(values, "values");
    JodaBeanUtils.notNull(viewDefinitionName, "viewDefinitionName");
    JodaBeanUtils.notNull(snapshotName, "snapshotName");
    JodaBeanUtils.notNull(valuationTime, "valuationTime");
    this._values = ImmutableMap.copyOf(values);
    this._viewDefinitionName = viewDefinitionName;
    this._snapshotName = snapshotName;
    this._valuationTime = valuationTime;
    this._version = version;
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
  public Map<CalculationResultKey, CalculatedValue> getValues() {
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
   * Gets the valuationTime.
   * @return the value of the property, not null
   */
  public Instant getValuationTime() {
    return _valuationTime;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the version.
   * @return the value of the property
   */
  public String getVersion() {
    return _version;
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
      CalculationResults other = (CalculationResults) obj;
      return JodaBeanUtils.equal(getValues(), other.getValues()) &&
          JodaBeanUtils.equal(getViewDefinitionName(), other.getViewDefinitionName()) &&
          JodaBeanUtils.equal(getSnapshotName(), other.getSnapshotName()) &&
          JodaBeanUtils.equal(getValuationTime(), other.getValuationTime()) &&
          JodaBeanUtils.equal(getVersion(), other.getVersion());
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = getClass().hashCode();
    hash = hash * 31 + JodaBeanUtils.hashCode(getValues());
    hash = hash * 31 + JodaBeanUtils.hashCode(getViewDefinitionName());
    hash = hash * 31 + JodaBeanUtils.hashCode(getSnapshotName());
    hash = hash * 31 + JodaBeanUtils.hashCode(getValuationTime());
    hash = hash * 31 + JodaBeanUtils.hashCode(getVersion());
    return hash;
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(192);
    buf.append("CalculationResults{");
    buf.append("values").append('=').append(getValues()).append(',').append(' ');
    buf.append("viewDefinitionName").append('=').append(getViewDefinitionName()).append(',').append(' ');
    buf.append("snapshotName").append('=').append(getSnapshotName()).append(',').append(' ');
    buf.append("valuationTime").append('=').append(getValuationTime()).append(',').append(' ');
    buf.append("version").append('=').append(JodaBeanUtils.toString(getVersion()));
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
    private final MetaProperty<Map<CalculationResultKey, CalculatedValue>> _values = DirectMetaProperty.ofImmutable(
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
     * The meta-property for the {@code valuationTime} property.
     */
    private final MetaProperty<Instant> _valuationTime = DirectMetaProperty.ofImmutable(
        this, "valuationTime", CalculationResults.class, Instant.class);
    /**
     * The meta-property for the {@code version} property.
     */
    private final MetaProperty<String> _version = DirectMetaProperty.ofImmutable(
        this, "version", CalculationResults.class, String.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, null,
        "values",
        "viewDefinitionName",
        "snapshotName",
        "valuationTime",
        "version");

    /**
     * Restricted constructor.
     */
    private Meta() {
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
        case 113591406:  // valuationTime
          return _valuationTime;
        case 351608024:  // version
          return _version;
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
    public MetaProperty<Map<CalculationResultKey, CalculatedValue>> values() {
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

    /**
     * The meta-property for the {@code valuationTime} property.
     * @return the meta-property, not null
     */
    public MetaProperty<Instant> valuationTime() {
      return _valuationTime;
    }

    /**
     * The meta-property for the {@code version} property.
     * @return the meta-property, not null
     */
    public MetaProperty<String> version() {
      return _version;
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
        case 113591406:  // valuationTime
          return ((CalculationResults) bean).getValuationTime();
        case 351608024:  // version
          return ((CalculationResults) bean).getVersion();
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
  public static final class Builder extends DirectFieldsBeanBuilder<CalculationResults> {

    private Map<CalculationResultKey, CalculatedValue> _values = new HashMap<CalculationResultKey, CalculatedValue>();
    private String _viewDefinitionName;
    private String _snapshotName;
    private Instant _valuationTime;
    private String _version;

    /**
     * Restricted constructor.
     */
    private Builder() {
    }

    /**
     * Restricted copy constructor.
     * @param beanToCopy  the bean to copy from, not null
     */
    private Builder(CalculationResults beanToCopy) {
      this._values = new HashMap<CalculationResultKey, CalculatedValue>(beanToCopy.getValues());
      this._viewDefinitionName = beanToCopy.getViewDefinitionName();
      this._snapshotName = beanToCopy.getSnapshotName();
      this._valuationTime = beanToCopy.getValuationTime();
      this._version = beanToCopy.getVersion();
    }

    //-----------------------------------------------------------------------
    @Override
    public Object get(String propertyName) {
      switch (propertyName.hashCode()) {
        case -823812830:  // values
          return _values;
        case -10926973:  // viewDefinitionName
          return _viewDefinitionName;
        case -931708305:  // snapshotName
          return _snapshotName;
        case 113591406:  // valuationTime
          return _valuationTime;
        case 351608024:  // version
          return _version;
        default:
          throw new NoSuchElementException("Unknown property: " + propertyName);
      }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Builder set(String propertyName, Object newValue) {
      switch (propertyName.hashCode()) {
        case -823812830:  // values
          this._values = (Map<CalculationResultKey, CalculatedValue>) newValue;
          break;
        case -10926973:  // viewDefinitionName
          this._viewDefinitionName = (String) newValue;
          break;
        case -931708305:  // snapshotName
          this._snapshotName = (String) newValue;
          break;
        case 113591406:  // valuationTime
          this._valuationTime = (Instant) newValue;
          break;
        case 351608024:  // version
          this._version = (String) newValue;
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
    public CalculationResults build() {
      return new CalculationResults(
          _values,
          _viewDefinitionName,
          _snapshotName,
          _valuationTime,
          _version);
    }

    //-----------------------------------------------------------------------
    /**
     * Sets the {@code values} property in the builder.
     * @param values  the new value, not null
     * @return this, for chaining, not null
     */
    public Builder values(Map<CalculationResultKey, CalculatedValue> values) {
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

    /**
     * Sets the {@code valuationTime} property in the builder.
     * @param valuationTime  the new value, not null
     * @return this, for chaining, not null
     */
    public Builder valuationTime(Instant valuationTime) {
      JodaBeanUtils.notNull(valuationTime, "valuationTime");
      this._valuationTime = valuationTime;
      return this;
    }

    /**
     * Sets the {@code version} property in the builder.
     * @param version  the new value
     * @return this, for chaining, not null
     */
    public Builder version(String version) {
      this._version = version;
      return this;
    }

    //-----------------------------------------------------------------------
    @Override
    public String toString() {
      StringBuilder buf = new StringBuilder(192);
      buf.append("CalculationResults.Builder{");
      buf.append("values").append('=').append(JodaBeanUtils.toString(_values)).append(',').append(' ');
      buf.append("viewDefinitionName").append('=').append(JodaBeanUtils.toString(_viewDefinitionName)).append(',').append(' ');
      buf.append("snapshotName").append('=').append(JodaBeanUtils.toString(_snapshotName)).append(',').append(' ');
      buf.append("valuationTime").append('=').append(JodaBeanUtils.toString(_valuationTime)).append(',').append(' ');
      buf.append("version").append('=').append(JodaBeanUtils.toString(_version));
      buf.append('}');
      return buf.toString();
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
