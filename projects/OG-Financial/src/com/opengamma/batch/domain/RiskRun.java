/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.batch.domain;

import com.google.common.collect.Sets;
import com.opengamma.batch.BatchMaster;
import com.opengamma.batch.SnapshotMode;
import com.opengamma.engine.view.CycleInfo;
import com.opengamma.id.ObjectId;
import com.opengamma.id.ObjectIdentifiable;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.functional.Function1;
import org.joda.beans.*;
import org.joda.beans.impl.direct.*;

import javax.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static com.opengamma.util.functional.Functional.map;
import org.joda.beans.BeanBuilder;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaProperty;
import org.joda.beans.Property;
import org.joda.beans.impl.direct.DirectBean;
import org.joda.beans.impl.direct.DirectBeanBuilder;
import org.joda.beans.impl.direct.DirectMetaBean;
import org.joda.beans.impl.direct.DirectMetaProperty;
import org.joda.beans.impl.direct.DirectMetaPropertyMap;

@BeanDefinition
public class RiskRun extends DirectBean implements ObjectIdentifiable {

  @PropertyDefinition
  private long _id;

  @PropertyDefinition
  private MarketData _marketData;

  @PropertyDefinition
  private Instant _createInstant;

  @PropertyDefinition
  private Instant _startInstant;

  @PropertyDefinition
  private Instant _endInstant;

  @PropertyDefinition
  private Instant _valuationTime;

  @PropertyDefinition
  private int _numRestarts;

  @PropertyDefinition
  private Set<CalculationConfiguration> _calculationConfigurations = new HashSet<CalculationConfiguration>();

  @PropertyDefinition
  private Set<RiskRunProperty> _properties = new HashSet<RiskRunProperty>();

  @PropertyDefinition
  private boolean _complete;

  @PropertyDefinition
  private VersionCorrection _versionCorrection;

  @PropertyDefinition
  private String _viewDefinitionUidScheme;

  @PropertyDefinition
  private String _viewDefinitionUidValue;
  
  @PropertyDefinition
  private String _viewDefinitionUidVersion;
  
  @PropertyDefinition
  private SnapshotMode _snapshotMode;  
  
  /**
   * Gets the viewDefinitionUid.
   * @return the value of the property
   */
  public UniqueId getViewDefinitionUid() {
    return UniqueId.of(getViewDefinitionUidScheme(), getViewDefinitionUidValue(), getViewDefinitionUidVersion());
  }

  /**
   * Sets the viewDefinitionUid.
   * @param viewDefinitionUid  the new value of the property
   */
  public void setViewDefinitionUid(UniqueId viewDefinitionUid) {
    setViewDefinitionUidScheme(viewDefinitionUid.getScheme());
    setViewDefinitionUidValue(viewDefinitionUid.getValue());
    setViewDefinitionUidVersion(viewDefinitionUid.getVersion());
  }

  @Override
  public ObjectId getObjectId() {
    return ObjectId.of(BatchMaster.BATCH_IDENTIFIER_SCHEME, Long.toString(getId()));
  }

  public RiskRun() {
  }

  public RiskRun(MarketData marketData, Instant createInstant, Instant valuationTime, int numRestarts, Set<CalculationConfiguration> calculationConfigurations, Set<RiskRunProperty> properties, boolean complete, VersionCorrection versionCorrection, UniqueId viewDefinitionUid) {
    this._marketData = marketData;
    this._createInstant = createInstant;
    this._valuationTime = valuationTime;
    this._numRestarts = numRestarts;
    this._calculationConfigurations = calculationConfigurations;
    this._properties = properties;
    this._complete = complete;
    this._versionCorrection = versionCorrection;
    setViewDefinitionUid(viewDefinitionUid);
    //
    for (RiskRunProperty property : properties) {
      property.setRiskRun(this);
    }
  }

  public RiskRun(final CycleInfo cycleInfo) {
    this(new MarketData(cycleInfo.getMarketDataSnapshotUniqueId()),
      Instant.now(),
      cycleInfo.getValuationTime(),
      0,
      map(Sets.<CalculationConfiguration>newHashSet(), cycleInfo.getAllCalculationConfigurationNames(), new Function1<String, CalculationConfiguration>() {
        @Override
        public CalculationConfiguration execute(String configName) {
          return new CalculationConfiguration(configName);
        }
      }),
      Sets.<RiskRunProperty>newHashSet(),
      false,
      cycleInfo.getVersionCorrection(),
      cycleInfo.getViewDefinitionUid()
    );
  }

  public Map<String, String> getPropertiesMap() {
    Map<String, String> returnValue = new HashMap<String, String>();

    for (RiskRunProperty property : getProperties()) {
      returnValue.put(property.getPropertyKey(), property.getPropertyValue());
    }

    return returnValue;
  }


  public void addProperty(RiskRunProperty property) {
    _properties.add(property);
  }


  public void addProperty(String key, String value) {
    RiskRunProperty property = new RiskRunProperty();
    property.setRiskRun(this);
    property.setPropertyKey(key);
    property.setPropertyValue(value);
    addProperty(property);
  }

  
  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code RiskRun}.
   * @return the meta-bean, not null
   */
  public static RiskRun.Meta meta() {
    return RiskRun.Meta.INSTANCE;
  }
  static {
    JodaBeanUtils.registerMetaBean(RiskRun.Meta.INSTANCE);
  }

  @Override
  public RiskRun.Meta metaBean() {
    return RiskRun.Meta.INSTANCE;
  }

  @Override
  protected Object propertyGet(String propertyName, boolean quiet) {
    switch (propertyName.hashCode()) {
      case 3355:  // id
        return getId();
      case 1116764678:  // marketData
        return getMarketData();
      case -90879675:  // createInstant
        return getCreateInstant();
      case 1823123231:  // startInstant
        return getStartInstant();
      case -2109892474:  // endInstant
        return getEndInstant();
      case 113591406:  // valuationTime
        return getValuationTime();
      case -1329836566:  // numRestarts
        return getNumRestarts();
      case -1619672730:  // calculationConfigurations
        return getCalculationConfigurations();
      case -926053069:  // properties
        return getProperties();
      case -599445191:  // complete
        return isComplete();
      case -2031293866:  // versionCorrection
        return getVersionCorrection();
      case -250012899:  // viewDefinitionUidScheme
        return getViewDefinitionUidScheme();
      case -559538951:  // viewDefinitionUidValue
        return getViewDefinitionUidValue();
      case -726014368:  // viewDefinitionUidVersion
        return getViewDefinitionUidVersion();
      case -931724921:  // snapshotMode
        return getSnapshotMode();
    }
    return super.propertyGet(propertyName, quiet);
  }

  @SuppressWarnings("unchecked")
  @Override
  protected void propertySet(String propertyName, Object newValue, boolean quiet) {
    switch (propertyName.hashCode()) {
      case 3355:  // id
        setId((Long) newValue);
        return;
      case 1116764678:  // marketData
        setMarketData((MarketData) newValue);
        return;
      case -90879675:  // createInstant
        setCreateInstant((Instant) newValue);
        return;
      case 1823123231:  // startInstant
        setStartInstant((Instant) newValue);
        return;
      case -2109892474:  // endInstant
        setEndInstant((Instant) newValue);
        return;
      case 113591406:  // valuationTime
        setValuationTime((Instant) newValue);
        return;
      case -1329836566:  // numRestarts
        setNumRestarts((Integer) newValue);
        return;
      case -1619672730:  // calculationConfigurations
        setCalculationConfigurations((Set<CalculationConfiguration>) newValue);
        return;
      case -926053069:  // properties
        setProperties((Set<RiskRunProperty>) newValue);
        return;
      case -599445191:  // complete
        setComplete((Boolean) newValue);
        return;
      case -2031293866:  // versionCorrection
        setVersionCorrection((VersionCorrection) newValue);
        return;
      case -250012899:  // viewDefinitionUidScheme
        setViewDefinitionUidScheme((String) newValue);
        return;
      case -559538951:  // viewDefinitionUidValue
        setViewDefinitionUidValue((String) newValue);
        return;
      case -726014368:  // viewDefinitionUidVersion
        setViewDefinitionUidVersion((String) newValue);
        return;
      case -931724921:  // snapshotMode
        setSnapshotMode((SnapshotMode) newValue);
        return;
    }
    super.propertySet(propertyName, newValue, quiet);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      RiskRun other = (RiskRun) obj;
      return JodaBeanUtils.equal(getId(), other.getId()) &&
          JodaBeanUtils.equal(getMarketData(), other.getMarketData()) &&
          JodaBeanUtils.equal(getCreateInstant(), other.getCreateInstant()) &&
          JodaBeanUtils.equal(getStartInstant(), other.getStartInstant()) &&
          JodaBeanUtils.equal(getEndInstant(), other.getEndInstant()) &&
          JodaBeanUtils.equal(getValuationTime(), other.getValuationTime()) &&
          JodaBeanUtils.equal(getNumRestarts(), other.getNumRestarts()) &&
          JodaBeanUtils.equal(getCalculationConfigurations(), other.getCalculationConfigurations()) &&
          JodaBeanUtils.equal(getProperties(), other.getProperties()) &&
          JodaBeanUtils.equal(isComplete(), other.isComplete()) &&
          JodaBeanUtils.equal(getVersionCorrection(), other.getVersionCorrection()) &&
          JodaBeanUtils.equal(getViewDefinitionUidScheme(), other.getViewDefinitionUidScheme()) &&
          JodaBeanUtils.equal(getViewDefinitionUidValue(), other.getViewDefinitionUidValue()) &&
          JodaBeanUtils.equal(getViewDefinitionUidVersion(), other.getViewDefinitionUidVersion()) &&
          JodaBeanUtils.equal(getSnapshotMode(), other.getSnapshotMode());
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = getClass().hashCode();
    hash += hash * 31 + JodaBeanUtils.hashCode(getId());
    hash += hash * 31 + JodaBeanUtils.hashCode(getMarketData());
    hash += hash * 31 + JodaBeanUtils.hashCode(getCreateInstant());
    hash += hash * 31 + JodaBeanUtils.hashCode(getStartInstant());
    hash += hash * 31 + JodaBeanUtils.hashCode(getEndInstant());
    hash += hash * 31 + JodaBeanUtils.hashCode(getValuationTime());
    hash += hash * 31 + JodaBeanUtils.hashCode(getNumRestarts());
    hash += hash * 31 + JodaBeanUtils.hashCode(getCalculationConfigurations());
    hash += hash * 31 + JodaBeanUtils.hashCode(getProperties());
    hash += hash * 31 + JodaBeanUtils.hashCode(isComplete());
    hash += hash * 31 + JodaBeanUtils.hashCode(getVersionCorrection());
    hash += hash * 31 + JodaBeanUtils.hashCode(getViewDefinitionUidScheme());
    hash += hash * 31 + JodaBeanUtils.hashCode(getViewDefinitionUidValue());
    hash += hash * 31 + JodaBeanUtils.hashCode(getViewDefinitionUidVersion());
    hash += hash * 31 + JodaBeanUtils.hashCode(getSnapshotMode());
    return hash;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the id.
   * @return the value of the property
   */
  public long getId() {
    return _id;
  }

  /**
   * Sets the id.
   * @param id  the new value of the property
   */
  public void setId(long id) {
    this._id = id;
  }

  /**
   * Gets the the {@code id} property.
   * @return the property, not null
   */
  public final Property<Long> id() {
    return metaBean().id().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the marketData.
   * @return the value of the property
   */
  public MarketData getMarketData() {
    return _marketData;
  }

  /**
   * Sets the marketData.
   * @param marketData  the new value of the property
   */
  public void setMarketData(MarketData marketData) {
    this._marketData = marketData;
  }

  /**
   * Gets the the {@code marketData} property.
   * @return the property, not null
   */
  public final Property<MarketData> marketData() {
    return metaBean().marketData().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the createInstant.
   * @return the value of the property
   */
  public Instant getCreateInstant() {
    return _createInstant;
  }

  /**
   * Sets the createInstant.
   * @param createInstant  the new value of the property
   */
  public void setCreateInstant(Instant createInstant) {
    this._createInstant = createInstant;
  }

  /**
   * Gets the the {@code createInstant} property.
   * @return the property, not null
   */
  public final Property<Instant> createInstant() {
    return metaBean().createInstant().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the startInstant.
   * @return the value of the property
   */
  public Instant getStartInstant() {
    return _startInstant;
  }

  /**
   * Sets the startInstant.
   * @param startInstant  the new value of the property
   */
  public void setStartInstant(Instant startInstant) {
    this._startInstant = startInstant;
  }

  /**
   * Gets the the {@code startInstant} property.
   * @return the property, not null
   */
  public final Property<Instant> startInstant() {
    return metaBean().startInstant().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the endInstant.
   * @return the value of the property
   */
  public Instant getEndInstant() {
    return _endInstant;
  }

  /**
   * Sets the endInstant.
   * @param endInstant  the new value of the property
   */
  public void setEndInstant(Instant endInstant) {
    this._endInstant = endInstant;
  }

  /**
   * Gets the the {@code endInstant} property.
   * @return the property, not null
   */
  public final Property<Instant> endInstant() {
    return metaBean().endInstant().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the valuationTime.
   * @return the value of the property
   */
  public Instant getValuationTime() {
    return _valuationTime;
  }

  /**
   * Sets the valuationTime.
   * @param valuationTime  the new value of the property
   */
  public void setValuationTime(Instant valuationTime) {
    this._valuationTime = valuationTime;
  }

  /**
   * Gets the the {@code valuationTime} property.
   * @return the property, not null
   */
  public final Property<Instant> valuationTime() {
    return metaBean().valuationTime().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the numRestarts.
   * @return the value of the property
   */
  public int getNumRestarts() {
    return _numRestarts;
  }

  /**
   * Sets the numRestarts.
   * @param numRestarts  the new value of the property
   */
  public void setNumRestarts(int numRestarts) {
    this._numRestarts = numRestarts;
  }

  /**
   * Gets the the {@code numRestarts} property.
   * @return the property, not null
   */
  public final Property<Integer> numRestarts() {
    return metaBean().numRestarts().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the calculationConfigurations.
   * @return the value of the property
   */
  public Set<CalculationConfiguration> getCalculationConfigurations() {
    return _calculationConfigurations;
  }

  /**
   * Sets the calculationConfigurations.
   * @param calculationConfigurations  the new value of the property
   */
  public void setCalculationConfigurations(Set<CalculationConfiguration> calculationConfigurations) {
    this._calculationConfigurations = calculationConfigurations;
  }

  /**
   * Gets the the {@code calculationConfigurations} property.
   * @return the property, not null
   */
  public final Property<Set<CalculationConfiguration>> calculationConfigurations() {
    return metaBean().calculationConfigurations().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the properties.
   * @return the value of the property
   */
  public Set<RiskRunProperty> getProperties() {
    return _properties;
  }

  /**
   * Sets the properties.
   * @param properties  the new value of the property
   */
  public void setProperties(Set<RiskRunProperty> properties) {
    this._properties = properties;
  }

  /**
   * Gets the the {@code properties} property.
   * @return the property, not null
   */
  public final Property<Set<RiskRunProperty>> properties() {
    return metaBean().properties().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the complete.
   * @return the value of the property
   */
  public boolean isComplete() {
    return _complete;
  }

  /**
   * Sets the complete.
   * @param complete  the new value of the property
   */
  public void setComplete(boolean complete) {
    this._complete = complete;
  }

  /**
   * Gets the the {@code complete} property.
   * @return the property, not null
   */
  public final Property<Boolean> complete() {
    return metaBean().complete().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the versionCorrection.
   * @return the value of the property
   */
  public VersionCorrection getVersionCorrection() {
    return _versionCorrection;
  }

  /**
   * Sets the versionCorrection.
   * @param versionCorrection  the new value of the property
   */
  public void setVersionCorrection(VersionCorrection versionCorrection) {
    this._versionCorrection = versionCorrection;
  }

  /**
   * Gets the the {@code versionCorrection} property.
   * @return the property, not null
   */
  public final Property<VersionCorrection> versionCorrection() {
    return metaBean().versionCorrection().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the viewDefinitionUidScheme.
   * @return the value of the property
   */
  public String getViewDefinitionUidScheme() {
    return _viewDefinitionUidScheme;
  }

  /**
   * Sets the viewDefinitionUidScheme.
   * @param viewDefinitionUidScheme  the new value of the property
   */
  public void setViewDefinitionUidScheme(String viewDefinitionUidScheme) {
    this._viewDefinitionUidScheme = viewDefinitionUidScheme;
  }

  /**
   * Gets the the {@code viewDefinitionUidScheme} property.
   * @return the property, not null
   */
  public final Property<String> viewDefinitionUidScheme() {
    return metaBean().viewDefinitionUidScheme().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the viewDefinitionUidValue.
   * @return the value of the property
   */
  public String getViewDefinitionUidValue() {
    return _viewDefinitionUidValue;
  }

  /**
   * Sets the viewDefinitionUidValue.
   * @param viewDefinitionUidValue  the new value of the property
   */
  public void setViewDefinitionUidValue(String viewDefinitionUidValue) {
    this._viewDefinitionUidValue = viewDefinitionUidValue;
  }

  /**
   * Gets the the {@code viewDefinitionUidValue} property.
   * @return the property, not null
   */
  public final Property<String> viewDefinitionUidValue() {
    return metaBean().viewDefinitionUidValue().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the viewDefinitionUidVersion.
   * @return the value of the property
   */
  public String getViewDefinitionUidVersion() {
    return _viewDefinitionUidVersion;
  }

  /**
   * Sets the viewDefinitionUidVersion.
   * @param viewDefinitionUidVersion  the new value of the property
   */
  public void setViewDefinitionUidVersion(String viewDefinitionUidVersion) {
    this._viewDefinitionUidVersion = viewDefinitionUidVersion;
  }

  /**
   * Gets the the {@code viewDefinitionUidVersion} property.
   * @return the property, not null
   */
  public final Property<String> viewDefinitionUidVersion() {
    return metaBean().viewDefinitionUidVersion().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the snapshotMode.
   * @return the value of the property
   */
  public SnapshotMode getSnapshotMode() {
    return _snapshotMode;
  }

  /**
   * Sets the snapshotMode.
   * @param snapshotMode  the new value of the property
   */
  public void setSnapshotMode(SnapshotMode snapshotMode) {
    this._snapshotMode = snapshotMode;
  }

  /**
   * Gets the the {@code snapshotMode} property.
   * @return the property, not null
   */
  public final Property<SnapshotMode> snapshotMode() {
    return metaBean().snapshotMode().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code RiskRun}.
   */
  public static class Meta extends DirectMetaBean {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code id} property.
     */
    private final MetaProperty<Long> _id = DirectMetaProperty.ofReadWrite(
        this, "id", RiskRun.class, Long.TYPE);
    /**
     * The meta-property for the {@code marketData} property.
     */
    private final MetaProperty<MarketData> _marketData = DirectMetaProperty.ofReadWrite(
        this, "marketData", RiskRun.class, MarketData.class);
    /**
     * The meta-property for the {@code createInstant} property.
     */
    private final MetaProperty<Instant> _createInstant = DirectMetaProperty.ofReadWrite(
        this, "createInstant", RiskRun.class, Instant.class);
    /**
     * The meta-property for the {@code startInstant} property.
     */
    private final MetaProperty<Instant> _startInstant = DirectMetaProperty.ofReadWrite(
        this, "startInstant", RiskRun.class, Instant.class);
    /**
     * The meta-property for the {@code endInstant} property.
     */
    private final MetaProperty<Instant> _endInstant = DirectMetaProperty.ofReadWrite(
        this, "endInstant", RiskRun.class, Instant.class);
    /**
     * The meta-property for the {@code valuationTime} property.
     */
    private final MetaProperty<Instant> _valuationTime = DirectMetaProperty.ofReadWrite(
        this, "valuationTime", RiskRun.class, Instant.class);
    /**
     * The meta-property for the {@code numRestarts} property.
     */
    private final MetaProperty<Integer> _numRestarts = DirectMetaProperty.ofReadWrite(
        this, "numRestarts", RiskRun.class, Integer.TYPE);
    /**
     * The meta-property for the {@code calculationConfigurations} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<Set<CalculationConfiguration>> _calculationConfigurations = DirectMetaProperty.ofReadWrite(
        this, "calculationConfigurations", RiskRun.class, (Class) Set.class);
    /**
     * The meta-property for the {@code properties} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<Set<RiskRunProperty>> _properties = DirectMetaProperty.ofReadWrite(
        this, "properties", RiskRun.class, (Class) Set.class);
    /**
     * The meta-property for the {@code complete} property.
     */
    private final MetaProperty<Boolean> _complete = DirectMetaProperty.ofReadWrite(
        this, "complete", RiskRun.class, Boolean.TYPE);
    /**
     * The meta-property for the {@code versionCorrection} property.
     */
    private final MetaProperty<VersionCorrection> _versionCorrection = DirectMetaProperty.ofReadWrite(
        this, "versionCorrection", RiskRun.class, VersionCorrection.class);
    /**
     * The meta-property for the {@code viewDefinitionUidScheme} property.
     */
    private final MetaProperty<String> _viewDefinitionUidScheme = DirectMetaProperty.ofReadWrite(
        this, "viewDefinitionUidScheme", RiskRun.class, String.class);
    /**
     * The meta-property for the {@code viewDefinitionUidValue} property.
     */
    private final MetaProperty<String> _viewDefinitionUidValue = DirectMetaProperty.ofReadWrite(
        this, "viewDefinitionUidValue", RiskRun.class, String.class);
    /**
     * The meta-property for the {@code viewDefinitionUidVersion} property.
     */
    private final MetaProperty<String> _viewDefinitionUidVersion = DirectMetaProperty.ofReadWrite(
        this, "viewDefinitionUidVersion", RiskRun.class, String.class);
    /**
     * The meta-property for the {@code snapshotMode} property.
     */
    private final MetaProperty<SnapshotMode> _snapshotMode = DirectMetaProperty.ofReadWrite(
        this, "snapshotMode", RiskRun.class, SnapshotMode.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, null,
        "id",
        "marketData",
        "createInstant",
        "startInstant",
        "endInstant",
        "valuationTime",
        "numRestarts",
        "calculationConfigurations",
        "properties",
        "complete",
        "versionCorrection",
        "viewDefinitionUidScheme",
        "viewDefinitionUidValue",
        "viewDefinitionUidVersion",
        "snapshotMode");

    /**
     * Restricted constructor.
     */
    protected Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case 3355:  // id
          return _id;
        case 1116764678:  // marketData
          return _marketData;
        case -90879675:  // createInstant
          return _createInstant;
        case 1823123231:  // startInstant
          return _startInstant;
        case -2109892474:  // endInstant
          return _endInstant;
        case 113591406:  // valuationTime
          return _valuationTime;
        case -1329836566:  // numRestarts
          return _numRestarts;
        case -1619672730:  // calculationConfigurations
          return _calculationConfigurations;
        case -926053069:  // properties
          return _properties;
        case -599445191:  // complete
          return _complete;
        case -2031293866:  // versionCorrection
          return _versionCorrection;
        case -250012899:  // viewDefinitionUidScheme
          return _viewDefinitionUidScheme;
        case -559538951:  // viewDefinitionUidValue
          return _viewDefinitionUidValue;
        case -726014368:  // viewDefinitionUidVersion
          return _viewDefinitionUidVersion;
        case -931724921:  // snapshotMode
          return _snapshotMode;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends RiskRun> builder() {
      return new DirectBeanBuilder<RiskRun>(new RiskRun());
    }

    @Override
    public Class<? extends RiskRun> beanType() {
      return RiskRun.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return _metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code id} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Long> id() {
      return _id;
    }

    /**
     * The meta-property for the {@code marketData} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<MarketData> marketData() {
      return _marketData;
    }

    /**
     * The meta-property for the {@code createInstant} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Instant> createInstant() {
      return _createInstant;
    }

    /**
     * The meta-property for the {@code startInstant} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Instant> startInstant() {
      return _startInstant;
    }

    /**
     * The meta-property for the {@code endInstant} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Instant> endInstant() {
      return _endInstant;
    }

    /**
     * The meta-property for the {@code valuationTime} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Instant> valuationTime() {
      return _valuationTime;
    }

    /**
     * The meta-property for the {@code numRestarts} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Integer> numRestarts() {
      return _numRestarts;
    }

    /**
     * The meta-property for the {@code calculationConfigurations} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Set<CalculationConfiguration>> calculationConfigurations() {
      return _calculationConfigurations;
    }

    /**
     * The meta-property for the {@code properties} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Set<RiskRunProperty>> properties() {
      return _properties;
    }

    /**
     * The meta-property for the {@code complete} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Boolean> complete() {
      return _complete;
    }

    /**
     * The meta-property for the {@code versionCorrection} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<VersionCorrection> versionCorrection() {
      return _versionCorrection;
    }

    /**
     * The meta-property for the {@code viewDefinitionUidScheme} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> viewDefinitionUidScheme() {
      return _viewDefinitionUidScheme;
    }

    /**
     * The meta-property for the {@code viewDefinitionUidValue} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> viewDefinitionUidValue() {
      return _viewDefinitionUidValue;
    }

    /**
     * The meta-property for the {@code viewDefinitionUidVersion} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> viewDefinitionUidVersion() {
      return _viewDefinitionUidVersion;
    }

    /**
     * The meta-property for the {@code snapshotMode} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<SnapshotMode> snapshotMode() {
      return _snapshotMode;
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
