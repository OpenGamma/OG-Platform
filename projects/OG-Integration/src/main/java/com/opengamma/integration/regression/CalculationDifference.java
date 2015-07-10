/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.regression;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import org.joda.beans.Bean;
import org.joda.beans.BeanDefinition;
import org.joda.beans.DerivedProperty;
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
import org.threeten.bp.Instant;

import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 *
 */
@BeanDefinition
public final class CalculationDifference implements ImmutableBean {

  @PropertyDefinition
  private final int _equalResultCount;

  @PropertyDefinition(validate = "notNull")
  private final String _viewDefinitionName;

  @PropertyDefinition(validate = "notNull")
  private final String _snapshotName;

  @PropertyDefinition(validate = "notNull")
  private final Instant _valuationTime;

  @PropertyDefinition(validate = "notNull")
  private final String _baseVersion;

  @PropertyDefinition(validate = "notNull")
  private final String _testVersion;

  @PropertyDefinition(validate = "notNull")
  private final Map<CalculationResultKey, CalculatedValue> _onlyBase;

  @PropertyDefinition(validate = "notNull")
  private final Map<CalculationResultKey, CalculatedValue> _onlyTest;

  @PropertyDefinition(validate = "notNull")
  private final Map<CalculationResultKey, Pair<CalculatedValue, CalculatedValue>> _different;

  @PropertyDefinition(validate = "notNull")
  private final Map<CalculationResultKey, Pair<CalculatedValue, CalculatedValue>> _differentProperties;

  private final TestStatus _status;

  @ImmutableConstructor
  private CalculationDifference(
      int equalResultCount,
      String viewDefinitionName,
      String snapshotName,
      Instant valuationTime,
      String baseVersion,
      String testVersion,
      Map<CalculationResultKey, CalculatedValue> onlyBase,
      Map<CalculationResultKey, CalculatedValue> onlyTest,
      Map<CalculationResultKey, Pair<CalculatedValue, CalculatedValue>> different,
      Map<CalculationResultKey, Pair<CalculatedValue, CalculatedValue>> differentProperties) {
    JodaBeanUtils.notNull(viewDefinitionName, "viewDefinitionName");
    JodaBeanUtils.notNull(snapshotName, "snapshotName");
    JodaBeanUtils.notNull(valuationTime, "valuationTime");
    JodaBeanUtils.notNull(baseVersion, "baseVersion");
    JodaBeanUtils.notNull(testVersion, "testVersion");
    JodaBeanUtils.notNull(onlyBase, "onlyBase");
    JodaBeanUtils.notNull(onlyTest, "onlyTest");
    JodaBeanUtils.notNull(different, "different");
    JodaBeanUtils.notNull(differentProperties, "differentProperties");
    _equalResultCount = equalResultCount;
    _viewDefinitionName = viewDefinitionName;
    _snapshotName = snapshotName;
    _valuationTime = valuationTime;
    _baseVersion = baseVersion;
    _testVersion = testVersion;
    _onlyBase = ImmutableSortedMap.copyOf(onlyBase);
    _onlyTest = ImmutableSortedMap.copyOf(onlyTest);
    _different = ImmutableSortedMap.copyOf(different);
    _differentProperties = ImmutableSortedMap.copyOf(differentProperties);
    if (_different.isEmpty() && _onlyBase.isEmpty() && _onlyTest.isEmpty()) {
      if (_differentProperties.isEmpty()) {
        _status = TestStatus.PASS;
      } else {
        _status = TestStatus.WARN;
      }
    } else {
      _status = TestStatus.FAIL;
    }
  }

  
  
  /**
   * Generates differences between two {@link CalculationResults} objects.
   */
  public static final class Generator {
    private final double _delta;
    private boolean _compareValueProperties = true;
    
    private Generator(double delta) {
      this._delta = delta;
    }
    
    /**
     * Whether to consider differences between value properties.
     * @param compareValueProperties boolean
     * @return this generator
     */
    public Generator compareValueProperties(boolean compareValueProperties) {
      this._compareValueProperties = compareValueProperties; return this;
    }
    
    public CalculationDifference between(CalculationResults results1, CalculationResults results2) {
      Set<CalculationResultKey> only1Keys = Sets.difference(results1.getValues().keySet(), results2.getValues().keySet());
      Set<CalculationResultKey> only2Keys = Sets.difference(results2.getValues().keySet(), results1.getValues().keySet());
      Map<CalculationResultKey, Pair<CalculatedValue, CalculatedValue>> diffs = Maps.newHashMap();
      Map<CalculationResultKey, Pair<CalculatedValue, CalculatedValue>> differentProps = Maps.newHashMap();
      Set<CalculationResultKey> bothKeys = Sets.intersection(results1.getValues().keySet(), results2.getValues().keySet());
      int equalResultCount = 0;
      for (CalculationResultKey key : bothKeys) {
        CalculatedValue value1 = results1.getValues().get(key);
        CalculatedValue value2 = results2.getValues().get(key);
        if (!EqualityChecker.equals(value1.getValue(), value2.getValue(), _delta)) {
          diffs.put(key, Pairs.of(value1, value2));
        } else {
          if (_compareValueProperties && !value1.getSpecificationProperties().equals(value2.getSpecificationProperties())) {
            differentProps.put(key, Pairs.of(value1, value2));
          } else {
            equalResultCount++;
          }
        }
      }
      Map<CalculationResultKey, CalculatedValue> only1 = getValues(only1Keys, results1.getValues());
      Map<CalculationResultKey, CalculatedValue> only2 = getValues(only2Keys, results2.getValues());
      String viewDefName = results1.getViewDefinitionName();
      String snapshotName = results1.getSnapshotName();
      Instant valuationTime = results1.getValuationTime();
      if (!valuationTime.equals(results2.getValuationTime())) {
        throw new IllegalArgumentException("The results must have the same valuation time");
      }
      return new CalculationDifference(equalResultCount,
                                       viewDefName,
                                       snapshotName,
                                       valuationTime,
                                       results1.getVersion(),
                                       results2.getVersion(),
                                       only1,
                                       only2,
                                       diffs,
                                       differentProps);
    }
    
  }
  
  
  /**
   * Returns a generator object which can be used to generate a comparison.
   * @param delta the delta to use for comparisons
   * @return a {@link Generator} object
   */
  public static Generator generatorWithDelta(double delta) {
    return new Generator(delta);
  }
  
  
  /**
   * Convenience method for comparing two result sets. Simply calls through to a {@link Generator} using default settings.
   * @param results1 The first set of results
   * @param results2 The second set of results
   * @param delta the delta to use
   * @return the CalculationDifference
   */
  public static CalculationDifference between(CalculationResults results1, CalculationResults results2, double delta) {
    return generatorWithDelta(delta).between(results1, results2);
  }

  /**
   * This only exists to workaround the inadequacy of Freemarker.
   * @param key  the key
   * @return the value
   */
  public CalculatedValue getOnlyBaseValue(CalculationResultKey key) {
    return _onlyBase.get(key);
  }

  /**
   * This only exists to workaround the inadequacy of Freemarker.
   * @param key  the key
   * @return the value
   */
  public CalculatedValue getOnlyTestValue(CalculationResultKey key) {
    return _onlyTest.get(key);
  }

  /**
   * This only exists to workaround the inadequacy of Freemarker.
   * @param key  the key
   * @return the value
   */
  public Pair<CalculatedValue, CalculatedValue> getDifferentValue(CalculationResultKey key) {
    return _different.get(key);
  }

  /**
   * This only exists to workaround the inadequacy of Freemarker.
   * @param key  the key
   * @return the value
   */
  public Pair<CalculatedValue, CalculatedValue> getDifferentPropertiesValue(CalculationResultKey key) {
    return _differentProperties.get(key);
  }

  private static Map<CalculationResultKey, CalculatedValue> getValues(Set<CalculationResultKey> keys,
                                                                      Map<CalculationResultKey, CalculatedValue> map) {
    Map<CalculationResultKey, CalculatedValue> retMap = Maps.newTreeMap();
    for (CalculationResultKey key : keys) {
      if (map.containsKey(key)) {
        retMap.put(key, map.get(key));
      }
    }
    return retMap;
  }

  @DerivedProperty
  public TestStatus getStatus() {
    return _status;
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code CalculationDifference}.
   * @return the meta-bean, not null
   */
  public static CalculationDifference.Meta meta() {
    return CalculationDifference.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(CalculationDifference.Meta.INSTANCE);
  }

  /**
   * Returns a builder used to create an instance of the bean.
   * @return the builder, not null
   */
  public static CalculationDifference.Builder builder() {
    return new CalculationDifference.Builder();
  }

  @Override
  public CalculationDifference.Meta metaBean() {
    return CalculationDifference.Meta.INSTANCE;
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
   * Gets the equalResultCount.
   * @return the value of the property
   */
  public int getEqualResultCount() {
    return _equalResultCount;
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
   * Gets the baseVersion.
   * @return the value of the property, not null
   */
  public String getBaseVersion() {
    return _baseVersion;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the testVersion.
   * @return the value of the property, not null
   */
  public String getTestVersion() {
    return _testVersion;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the onlyBase.
   * @return the value of the property, not null
   */
  public Map<CalculationResultKey, CalculatedValue> getOnlyBase() {
    return _onlyBase;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the onlyTest.
   * @return the value of the property, not null
   */
  public Map<CalculationResultKey, CalculatedValue> getOnlyTest() {
    return _onlyTest;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the different.
   * @return the value of the property, not null
   */
  public Map<CalculationResultKey, Pair<CalculatedValue, CalculatedValue>> getDifferent() {
    return _different;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the differentProperties.
   * @return the value of the property, not null
   */
  public Map<CalculationResultKey, Pair<CalculatedValue, CalculatedValue>> getDifferentProperties() {
    return _differentProperties;
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
      CalculationDifference other = (CalculationDifference) obj;
      return (getEqualResultCount() == other.getEqualResultCount()) &&
          JodaBeanUtils.equal(getViewDefinitionName(), other.getViewDefinitionName()) &&
          JodaBeanUtils.equal(getSnapshotName(), other.getSnapshotName()) &&
          JodaBeanUtils.equal(getValuationTime(), other.getValuationTime()) &&
          JodaBeanUtils.equal(getBaseVersion(), other.getBaseVersion()) &&
          JodaBeanUtils.equal(getTestVersion(), other.getTestVersion()) &&
          JodaBeanUtils.equal(getOnlyBase(), other.getOnlyBase()) &&
          JodaBeanUtils.equal(getOnlyTest(), other.getOnlyTest()) &&
          JodaBeanUtils.equal(getDifferent(), other.getDifferent()) &&
          JodaBeanUtils.equal(getDifferentProperties(), other.getDifferentProperties());
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = getClass().hashCode();
    hash = hash * 31 + JodaBeanUtils.hashCode(getEqualResultCount());
    hash = hash * 31 + JodaBeanUtils.hashCode(getViewDefinitionName());
    hash = hash * 31 + JodaBeanUtils.hashCode(getSnapshotName());
    hash = hash * 31 + JodaBeanUtils.hashCode(getValuationTime());
    hash = hash * 31 + JodaBeanUtils.hashCode(getBaseVersion());
    hash = hash * 31 + JodaBeanUtils.hashCode(getTestVersion());
    hash = hash * 31 + JodaBeanUtils.hashCode(getOnlyBase());
    hash = hash * 31 + JodaBeanUtils.hashCode(getOnlyTest());
    hash = hash * 31 + JodaBeanUtils.hashCode(getDifferent());
    hash = hash * 31 + JodaBeanUtils.hashCode(getDifferentProperties());
    return hash;
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(384);
    buf.append("CalculationDifference{");
    buf.append("equalResultCount").append('=').append(getEqualResultCount()).append(',').append(' ');
    buf.append("viewDefinitionName").append('=').append(getViewDefinitionName()).append(',').append(' ');
    buf.append("snapshotName").append('=').append(getSnapshotName()).append(',').append(' ');
    buf.append("valuationTime").append('=').append(getValuationTime()).append(',').append(' ');
    buf.append("baseVersion").append('=').append(getBaseVersion()).append(',').append(' ');
    buf.append("testVersion").append('=').append(getTestVersion()).append(',').append(' ');
    buf.append("onlyBase").append('=').append(getOnlyBase()).append(',').append(' ');
    buf.append("onlyTest").append('=').append(getOnlyTest()).append(',').append(' ');
    buf.append("different").append('=').append(getDifferent()).append(',').append(' ');
    buf.append("differentProperties").append('=').append(getDifferentProperties()).append(',').append(' ');
    buf.append("status").append('=').append(JodaBeanUtils.toString(getStatus()));
    buf.append('}');
    return buf.toString();
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code CalculationDifference}.
   */
  public static final class Meta extends DirectMetaBean {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code equalResultCount} property.
     */
    private final MetaProperty<Integer> _equalResultCount = DirectMetaProperty.ofImmutable(
        this, "equalResultCount", CalculationDifference.class, Integer.TYPE);
    /**
     * The meta-property for the {@code viewDefinitionName} property.
     */
    private final MetaProperty<String> _viewDefinitionName = DirectMetaProperty.ofImmutable(
        this, "viewDefinitionName", CalculationDifference.class, String.class);
    /**
     * The meta-property for the {@code snapshotName} property.
     */
    private final MetaProperty<String> _snapshotName = DirectMetaProperty.ofImmutable(
        this, "snapshotName", CalculationDifference.class, String.class);
    /**
     * The meta-property for the {@code valuationTime} property.
     */
    private final MetaProperty<Instant> _valuationTime = DirectMetaProperty.ofImmutable(
        this, "valuationTime", CalculationDifference.class, Instant.class);
    /**
     * The meta-property for the {@code baseVersion} property.
     */
    private final MetaProperty<String> _baseVersion = DirectMetaProperty.ofImmutable(
        this, "baseVersion", CalculationDifference.class, String.class);
    /**
     * The meta-property for the {@code testVersion} property.
     */
    private final MetaProperty<String> _testVersion = DirectMetaProperty.ofImmutable(
        this, "testVersion", CalculationDifference.class, String.class);
    /**
     * The meta-property for the {@code onlyBase} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<Map<CalculationResultKey, CalculatedValue>> _onlyBase = DirectMetaProperty.ofImmutable(
        this, "onlyBase", CalculationDifference.class, (Class) Map.class);
    /**
     * The meta-property for the {@code onlyTest} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<Map<CalculationResultKey, CalculatedValue>> _onlyTest = DirectMetaProperty.ofImmutable(
        this, "onlyTest", CalculationDifference.class, (Class) Map.class);
    /**
     * The meta-property for the {@code different} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<Map<CalculationResultKey, Pair<CalculatedValue, CalculatedValue>>> _different = DirectMetaProperty.ofImmutable(
        this, "different", CalculationDifference.class, (Class) Map.class);
    /**
     * The meta-property for the {@code differentProperties} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<Map<CalculationResultKey, Pair<CalculatedValue, CalculatedValue>>> _differentProperties = DirectMetaProperty.ofImmutable(
        this, "differentProperties", CalculationDifference.class, (Class) Map.class);
    /**
     * The meta-property for the {@code status} property.
     */
    private final MetaProperty<TestStatus> _status = DirectMetaProperty.ofDerived(
        this, "status", CalculationDifference.class, TestStatus.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, null,
        "equalResultCount",
        "viewDefinitionName",
        "snapshotName",
        "valuationTime",
        "baseVersion",
        "testVersion",
        "onlyBase",
        "onlyTest",
        "different",
        "differentProperties",
        "status");

    /**
     * Restricted constructor.
     */
    private Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case -1019962594:  // equalResultCount
          return _equalResultCount;
        case -10926973:  // viewDefinitionName
          return _viewDefinitionName;
        case -931708305:  // snapshotName
          return _snapshotName;
        case 113591406:  // valuationTime
          return _valuationTime;
        case -1641901881:  // baseVersion
          return _baseVersion;
        case -40990746:  // testVersion
          return _testVersion;
        case -2069633891:  // onlyBase
          return _onlyBase;
        case -2069093794:  // onlyTest
          return _onlyTest;
        case 1302679609:  // different
          return _different;
        case 567051852:  // differentProperties
          return _differentProperties;
        case -892481550:  // status
          return _status;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public CalculationDifference.Builder builder() {
      return new CalculationDifference.Builder();
    }

    @Override
    public Class<? extends CalculationDifference> beanType() {
      return CalculationDifference.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return _metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code equalResultCount} property.
     * @return the meta-property, not null
     */
    public MetaProperty<Integer> equalResultCount() {
      return _equalResultCount;
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
     * The meta-property for the {@code baseVersion} property.
     * @return the meta-property, not null
     */
    public MetaProperty<String> baseVersion() {
      return _baseVersion;
    }

    /**
     * The meta-property for the {@code testVersion} property.
     * @return the meta-property, not null
     */
    public MetaProperty<String> testVersion() {
      return _testVersion;
    }

    /**
     * The meta-property for the {@code onlyBase} property.
     * @return the meta-property, not null
     */
    public MetaProperty<Map<CalculationResultKey, CalculatedValue>> onlyBase() {
      return _onlyBase;
    }

    /**
     * The meta-property for the {@code onlyTest} property.
     * @return the meta-property, not null
     */
    public MetaProperty<Map<CalculationResultKey, CalculatedValue>> onlyTest() {
      return _onlyTest;
    }

    /**
     * The meta-property for the {@code different} property.
     * @return the meta-property, not null
     */
    public MetaProperty<Map<CalculationResultKey, Pair<CalculatedValue, CalculatedValue>>> different() {
      return _different;
    }

    /**
     * The meta-property for the {@code differentProperties} property.
     * @return the meta-property, not null
     */
    public MetaProperty<Map<CalculationResultKey, Pair<CalculatedValue, CalculatedValue>>> differentProperties() {
      return _differentProperties;
    }

    /**
     * The meta-property for the {@code status} property.
     * @return the meta-property, not null
     */
    public MetaProperty<TestStatus> status() {
      return _status;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -1019962594:  // equalResultCount
          return ((CalculationDifference) bean).getEqualResultCount();
        case -10926973:  // viewDefinitionName
          return ((CalculationDifference) bean).getViewDefinitionName();
        case -931708305:  // snapshotName
          return ((CalculationDifference) bean).getSnapshotName();
        case 113591406:  // valuationTime
          return ((CalculationDifference) bean).getValuationTime();
        case -1641901881:  // baseVersion
          return ((CalculationDifference) bean).getBaseVersion();
        case -40990746:  // testVersion
          return ((CalculationDifference) bean).getTestVersion();
        case -2069633891:  // onlyBase
          return ((CalculationDifference) bean).getOnlyBase();
        case -2069093794:  // onlyTest
          return ((CalculationDifference) bean).getOnlyTest();
        case 1302679609:  // different
          return ((CalculationDifference) bean).getDifferent();
        case 567051852:  // differentProperties
          return ((CalculationDifference) bean).getDifferentProperties();
        case -892481550:  // status
          return ((CalculationDifference) bean).getStatus();
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
   * The bean-builder for {@code CalculationDifference}.
   */
  public static final class Builder extends DirectFieldsBeanBuilder<CalculationDifference> {

    private int _equalResultCount;
    private String _viewDefinitionName;
    private String _snapshotName;
    private Instant _valuationTime;
    private String _baseVersion;
    private String _testVersion;
    private Map<CalculationResultKey, CalculatedValue> _onlyBase = new HashMap<CalculationResultKey, CalculatedValue>();
    private Map<CalculationResultKey, CalculatedValue> _onlyTest = new HashMap<CalculationResultKey, CalculatedValue>();
    private Map<CalculationResultKey, Pair<CalculatedValue, CalculatedValue>> _different = new HashMap<CalculationResultKey, Pair<CalculatedValue, CalculatedValue>>();
    private Map<CalculationResultKey, Pair<CalculatedValue, CalculatedValue>> _differentProperties = new HashMap<CalculationResultKey, Pair<CalculatedValue, CalculatedValue>>();

    /**
     * Restricted constructor.
     */
    private Builder() {
    }

    /**
     * Restricted copy constructor.
     * @param beanToCopy  the bean to copy from, not null
     */
    private Builder(CalculationDifference beanToCopy) {
      this._equalResultCount = beanToCopy.getEqualResultCount();
      this._viewDefinitionName = beanToCopy.getViewDefinitionName();
      this._snapshotName = beanToCopy.getSnapshotName();
      this._valuationTime = beanToCopy.getValuationTime();
      this._baseVersion = beanToCopy.getBaseVersion();
      this._testVersion = beanToCopy.getTestVersion();
      this._onlyBase = new HashMap<CalculationResultKey, CalculatedValue>(beanToCopy.getOnlyBase());
      this._onlyTest = new HashMap<CalculationResultKey, CalculatedValue>(beanToCopy.getOnlyTest());
      this._different = new HashMap<CalculationResultKey, Pair<CalculatedValue, CalculatedValue>>(beanToCopy.getDifferent());
      this._differentProperties = new HashMap<CalculationResultKey, Pair<CalculatedValue, CalculatedValue>>(beanToCopy.getDifferentProperties());
    }

    //-----------------------------------------------------------------------
    @Override
    public Object get(String propertyName) {
      switch (propertyName.hashCode()) {
        case -1019962594:  // equalResultCount
          return _equalResultCount;
        case -10926973:  // viewDefinitionName
          return _viewDefinitionName;
        case -931708305:  // snapshotName
          return _snapshotName;
        case 113591406:  // valuationTime
          return _valuationTime;
        case -1641901881:  // baseVersion
          return _baseVersion;
        case -40990746:  // testVersion
          return _testVersion;
        case -2069633891:  // onlyBase
          return _onlyBase;
        case -2069093794:  // onlyTest
          return _onlyTest;
        case 1302679609:  // different
          return _different;
        case 567051852:  // differentProperties
          return _differentProperties;
        default:
          throw new NoSuchElementException("Unknown property: " + propertyName);
      }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Builder set(String propertyName, Object newValue) {
      switch (propertyName.hashCode()) {
        case -1019962594:  // equalResultCount
          this._equalResultCount = (Integer) newValue;
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
        case -1641901881:  // baseVersion
          this._baseVersion = (String) newValue;
          break;
        case -40990746:  // testVersion
          this._testVersion = (String) newValue;
          break;
        case -2069633891:  // onlyBase
          this._onlyBase = (Map<CalculationResultKey, CalculatedValue>) newValue;
          break;
        case -2069093794:  // onlyTest
          this._onlyTest = (Map<CalculationResultKey, CalculatedValue>) newValue;
          break;
        case 1302679609:  // different
          this._different = (Map<CalculationResultKey, Pair<CalculatedValue, CalculatedValue>>) newValue;
          break;
        case 567051852:  // differentProperties
          this._differentProperties = (Map<CalculationResultKey, Pair<CalculatedValue, CalculatedValue>>) newValue;
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
    public CalculationDifference build() {
      return new CalculationDifference(
          _equalResultCount,
          _viewDefinitionName,
          _snapshotName,
          _valuationTime,
          _baseVersion,
          _testVersion,
          _onlyBase,
          _onlyTest,
          _different,
          _differentProperties);
    }

    //-----------------------------------------------------------------------
    /**
     * Sets the {@code equalResultCount} property in the builder.
     * @param equalResultCount  the new value
     * @return this, for chaining, not null
     */
    public Builder equalResultCount(int equalResultCount) {
      this._equalResultCount = equalResultCount;
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
     * Sets the {@code baseVersion} property in the builder.
     * @param baseVersion  the new value, not null
     * @return this, for chaining, not null
     */
    public Builder baseVersion(String baseVersion) {
      JodaBeanUtils.notNull(baseVersion, "baseVersion");
      this._baseVersion = baseVersion;
      return this;
    }

    /**
     * Sets the {@code testVersion} property in the builder.
     * @param testVersion  the new value, not null
     * @return this, for chaining, not null
     */
    public Builder testVersion(String testVersion) {
      JodaBeanUtils.notNull(testVersion, "testVersion");
      this._testVersion = testVersion;
      return this;
    }

    /**
     * Sets the {@code onlyBase} property in the builder.
     * @param onlyBase  the new value, not null
     * @return this, for chaining, not null
     */
    public Builder onlyBase(Map<CalculationResultKey, CalculatedValue> onlyBase) {
      JodaBeanUtils.notNull(onlyBase, "onlyBase");
      this._onlyBase = onlyBase;
      return this;
    }

    /**
     * Sets the {@code onlyTest} property in the builder.
     * @param onlyTest  the new value, not null
     * @return this, for chaining, not null
     */
    public Builder onlyTest(Map<CalculationResultKey, CalculatedValue> onlyTest) {
      JodaBeanUtils.notNull(onlyTest, "onlyTest");
      this._onlyTest = onlyTest;
      return this;
    }

    /**
     * Sets the {@code different} property in the builder.
     * @param different  the new value, not null
     * @return this, for chaining, not null
     */
    public Builder different(Map<CalculationResultKey, Pair<CalculatedValue, CalculatedValue>> different) {
      JodaBeanUtils.notNull(different, "different");
      this._different = different;
      return this;
    }

    /**
     * Sets the {@code differentProperties} property in the builder.
     * @param differentProperties  the new value, not null
     * @return this, for chaining, not null
     */
    public Builder differentProperties(Map<CalculationResultKey, Pair<CalculatedValue, CalculatedValue>> differentProperties) {
      JodaBeanUtils.notNull(differentProperties, "differentProperties");
      this._differentProperties = differentProperties;
      return this;
    }

    //-----------------------------------------------------------------------
    @Override
    public String toString() {
      StringBuilder buf = new StringBuilder(352);
      buf.append("CalculationDifference.Builder{");
      buf.append("equalResultCount").append('=').append(JodaBeanUtils.toString(_equalResultCount)).append(',').append(' ');
      buf.append("viewDefinitionName").append('=').append(JodaBeanUtils.toString(_viewDefinitionName)).append(',').append(' ');
      buf.append("snapshotName").append('=').append(JodaBeanUtils.toString(_snapshotName)).append(',').append(' ');
      buf.append("valuationTime").append('=').append(JodaBeanUtils.toString(_valuationTime)).append(',').append(' ');
      buf.append("baseVersion").append('=').append(JodaBeanUtils.toString(_baseVersion)).append(',').append(' ');
      buf.append("testVersion").append('=').append(JodaBeanUtils.toString(_testVersion)).append(',').append(' ');
      buf.append("onlyBase").append('=').append(JodaBeanUtils.toString(_onlyBase)).append(',').append(' ');
      buf.append("onlyTest").append('=').append(JodaBeanUtils.toString(_onlyTest)).append(',').append(' ');
      buf.append("different").append('=').append(JodaBeanUtils.toString(_different)).append(',').append(' ');
      buf.append("differentProperties").append('=').append(JodaBeanUtils.toString(_differentProperties));
      buf.append('}');
      return buf.toString();
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}


