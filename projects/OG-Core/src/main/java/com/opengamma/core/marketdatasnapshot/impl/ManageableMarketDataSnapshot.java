/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.marketdatasnapshot.impl;

import java.util.Map;

import org.joda.beans.Bean;
import org.joda.beans.BeanBuilder;
import org.joda.beans.BeanDefinition;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaProperty;
import org.joda.beans.Property;
import org.joda.beans.PropertyDefinition;
import org.joda.beans.impl.direct.DirectBean;
import org.joda.beans.impl.direct.DirectBeanBuilder;
import org.joda.beans.impl.direct.DirectMetaBean;
import org.joda.beans.impl.direct.DirectMetaProperty;
import org.joda.beans.impl.direct.DirectMetaPropertyMap;
import org.threeten.bp.Instant;

import com.google.common.collect.Maps;
import com.opengamma.core.marketdatasnapshot.CurveKey;
import com.opengamma.core.marketdatasnapshot.CurveSnapshot;
import com.opengamma.core.marketdatasnapshot.NamedSnapshot;
import com.opengamma.core.marketdatasnapshot.StructuredMarketDataSnapshot;
import com.opengamma.core.marketdatasnapshot.SurfaceKey;
import com.opengamma.core.marketdatasnapshot.SurfaceSnapshot;
import com.opengamma.core.marketdatasnapshot.UnstructuredMarketDataSnapshot;
import com.opengamma.core.marketdatasnapshot.VolatilityCubeKey;
import com.opengamma.core.marketdatasnapshot.VolatilityCubeSnapshot;
import com.opengamma.core.marketdatasnapshot.VolatilitySurfaceKey;
import com.opengamma.core.marketdatasnapshot.VolatilitySurfaceSnapshot;
import com.opengamma.core.marketdatasnapshot.YieldCurveKey;
import com.opengamma.core.marketdatasnapshot.YieldCurveSnapshot;
import com.opengamma.id.UniqueId;
import com.opengamma.util.PublicSPI;

/**
 * A snapshot of market data potentially altered by hand.
 */
@BeanDefinition
@PublicSPI
public class ManageableMarketDataSnapshot extends DirectBean implements StructuredMarketDataSnapshot {

  /**
   * The unique identifier of the snapshot. This must be null when adding to a master and not null when retrieved from a master.
   */
  @PropertyDefinition
  private UniqueId _uniqueId;

  /**
   * The name of the snapshot intended for display purposes.
   */
  @PropertyDefinition
  private String _name;

  /**
   * The name of the view on which this snapshot was based
   */
  @PropertyDefinition
  private String _basisViewName;

  @PropertyDefinition
  private ManageableUnstructuredMarketDataSnapshot _globalValues;

  /**
   * The yield curves in this snapshot
   */
  @PropertyDefinition
  private Map<YieldCurveKey, YieldCurveSnapshot> _yieldCurves;

  /**
   * The yield curves in this snapshot
   */
  @PropertyDefinition
  private Map<CurveKey, CurveSnapshot> _curves;

  /**
   * The vol cubes in this snapshot
   */
  @PropertyDefinition
  private Map<VolatilityCubeKey, VolatilityCubeSnapshot> _volatilityCubes;

  /**
   * The vol surfaces in this snapshot
   */
  @PropertyDefinition
  private Map<VolatilitySurfaceKey, VolatilitySurfaceSnapshot> _volatilitySurfaces = Maps.newHashMap(); //Initialize in order to handle old format

  /**
   * The surfaces in this snapshot
   */
  @PropertyDefinition
  private Map<SurfaceKey, SurfaceSnapshot> _surfaces;

  /**
   * The valuation time of the snapshot
   */
  @PropertyDefinition(get = "manual")
  private Instant _valuationTime;

  /**
   * Creates a snapshot
   */
  public ManageableMarketDataSnapshot() {
    super();
  }

  /**
   * Creates a snapshot
   * 
   * @param name the name of the snapshot
   * @param globalValues the snapshot for the global scope
   * @param yieldCurves the yield curves
   */
  public ManageableMarketDataSnapshot(final String name, final UnstructuredMarketDataSnapshot globalValues,
      final Map<YieldCurveKey, YieldCurveSnapshot> yieldCurves) {
    _name = name;
    _globalValues = (globalValues != null) ? new ManageableUnstructuredMarketDataSnapshot(globalValues) : null;
    _yieldCurves = yieldCurves;
  }

  /**
   * Creates a snapshot.
   * 
   * @param name the name of the snapshot
   * @param globalValues the snapshot for the global scope
   * @param yieldCurves the yield curves
   * @param volatilitySurfaces the volatility surfaces
   * @param volatilityCubes the volatility cubes
   */
  public ManageableMarketDataSnapshot(final String name, final UnstructuredMarketDataSnapshot globalValues,
      final Map<YieldCurveKey, YieldCurveSnapshot> yieldCurves, final Map<VolatilitySurfaceKey,
      VolatilitySurfaceSnapshot> volatilitySurfaces, final Map<VolatilityCubeKey, VolatilityCubeSnapshot> volatilityCubes) {
    _name = name;
    _globalValues = (globalValues != null) ? new ManageableUnstructuredMarketDataSnapshot(globalValues) : null;
    _yieldCurves = yieldCurves;
    _curves = null;
    _volatilitySurfaces = volatilitySurfaces;
    _volatilityCubes = volatilityCubes;
  }

  /**
   * Creates a snapshot.
   *
   * @param name the name of the snapshot
   * @param globalValues the snapshot for the global scope
   * @param yieldCurves the yield curves
   * @param curves the curves
   * @param volatilitySurfaces the volatility surfaces
   * @param volatilityCubes the volatility cubes
   */
  public ManageableMarketDataSnapshot(final String name, final UnstructuredMarketDataSnapshot globalValues,
      final Map<YieldCurveKey, YieldCurveSnapshot> yieldCurves, final Map<CurveKey, CurveSnapshot> curves, final Map<VolatilitySurfaceKey,
      VolatilitySurfaceSnapshot> volatilitySurfaces, final Map<VolatilityCubeKey, VolatilityCubeSnapshot> volatilityCubes) {
    _name = name;
    _globalValues = (globalValues != null) ? new ManageableUnstructuredMarketDataSnapshot(globalValues) : null;
    _yieldCurves = yieldCurves;
    _curves = curves;
    _volatilitySurfaces = volatilitySurfaces;
    _volatilityCubes = volatilityCubes;
  }

  /**
   * Creates a snapshot.
   *
   * @param name the name of the snapshot
   * @param globalValues the snapshot for the global scope
   * @param yieldCurves the yield curves
   * @param curves the curves
   * @param volatilitySurfaces the volatility surfaces
   * @param volatilityCubes the volatility cubes
   * @param surfaces the surfaces
   */
  public ManageableMarketDataSnapshot(final String name, final UnstructuredMarketDataSnapshot globalValues,
      final Map<YieldCurveKey, YieldCurveSnapshot> yieldCurves, final Map<CurveKey, CurveSnapshot> curves,
      final Map<VolatilitySurfaceKey, VolatilitySurfaceSnapshot> volatilitySurfaces,
      final Map<VolatilityCubeKey, VolatilityCubeSnapshot> volatilityCubes,
      final Map<SurfaceKey, SurfaceSnapshot> surfaces) {
    _name = name;
    _globalValues = (globalValues != null) ? new ManageableUnstructuredMarketDataSnapshot(globalValues) : null;
    _yieldCurves = yieldCurves;
    _curves = curves;
    _volatilitySurfaces = volatilitySurfaces;
    _volatilityCubes = volatilityCubes;
    _surfaces = surfaces;
  }

  public ManageableMarketDataSnapshot(final StructuredMarketDataSnapshot copyFrom) {
    this(copyFrom.getName(), copyFrom.getGlobalValues(), copyFrom.getYieldCurves(), copyFrom.getCurves(), copyFrom.getVolatilitySurfaces(),
        copyFrom.getVolatilityCubes(), copyFrom.getSurfaces());
    _basisViewName = copyFrom.getBasisViewName();
    _valuationTime = copyFrom.getValuationTime();
  }

  @Override
  public NamedSnapshot withUniqueId(UniqueId uniqueId) {
    // As this is a mutable object, we just update and return it
    setUniqueId(uniqueId);
    return this;
  }

  /**
   * Gets the valuation time of the snapshot
   * @return the valuation time of the snapshot, or null if
   * no valuation time could be inferred from the snapshot
   */
  @Override
  public Instant getValuationTime() {
    //[PLAT-1393] manual implementation provided for backwards compatibility.
    //older versions took the valuation time from the yieldCurves
    if (_valuationTime != null) {
      return _valuationTime;
    }
    Instant latestTimestamp = null;
    final Map<YieldCurveKey, YieldCurveSnapshot> yieldCurves = getYieldCurves();
    if (yieldCurves != null) {
      for (final YieldCurveSnapshot yieldCurveSnapshot : yieldCurves.values()) {
        if (latestTimestamp == null || latestTimestamp.isBefore(yieldCurveSnapshot.getValuationTime())) {
          latestTimestamp = yieldCurveSnapshot.getValuationTime();
        }
      }
    }
    final Map<CurveKey, CurveSnapshot> curves = getCurves();
    if (curves != null) {
      for (final CurveSnapshot curveSnapshot : curves.values()) {
        if (latestTimestamp == null || latestTimestamp.isBefore(curveSnapshot.getValuationTime())) {
          latestTimestamp = curveSnapshot.getValuationTime();
        }
      }
    }
    return latestTimestamp;

  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code ManageableMarketDataSnapshot}.
   * @return the meta-bean, not null
   */
  public static ManageableMarketDataSnapshot.Meta meta() {
    return ManageableMarketDataSnapshot.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(ManageableMarketDataSnapshot.Meta.INSTANCE);
  }

  @Override
  public ManageableMarketDataSnapshot.Meta metaBean() {
    return ManageableMarketDataSnapshot.Meta.INSTANCE;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the unique identifier of the snapshot. This must be null when adding to a master and not null when retrieved from a master.
   * @return the value of the property
   */
  public UniqueId getUniqueId() {
    return _uniqueId;
  }

  /**
   * Sets the unique identifier of the snapshot. This must be null when adding to a master and not null when retrieved from a master.
   * @param uniqueId  the new value of the property
   */
  public void setUniqueId(UniqueId uniqueId) {
    this._uniqueId = uniqueId;
  }

  /**
   * Gets the the {@code uniqueId} property.
   * @return the property, not null
   */
  public final Property<UniqueId> uniqueId() {
    return metaBean().uniqueId().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the name of the snapshot intended for display purposes.
   * @return the value of the property
   */
  public String getName() {
    return _name;
  }

  /**
   * Sets the name of the snapshot intended for display purposes.
   * @param name  the new value of the property
   */
  public void setName(String name) {
    this._name = name;
  }

  /**
   * Gets the the {@code name} property.
   * @return the property, not null
   */
  public final Property<String> name() {
    return metaBean().name().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the name of the view on which this snapshot was based
   * @return the value of the property
   */
  public String getBasisViewName() {
    return _basisViewName;
  }

  /**
   * Sets the name of the view on which this snapshot was based
   * @param basisViewName  the new value of the property
   */
  public void setBasisViewName(String basisViewName) {
    this._basisViewName = basisViewName;
  }

  /**
   * Gets the the {@code basisViewName} property.
   * @return the property, not null
   */
  public final Property<String> basisViewName() {
    return metaBean().basisViewName().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the globalValues.
   * @return the value of the property
   */
  public ManageableUnstructuredMarketDataSnapshot getGlobalValues() {
    return _globalValues;
  }

  /**
   * Sets the globalValues.
   * @param globalValues  the new value of the property
   */
  public void setGlobalValues(ManageableUnstructuredMarketDataSnapshot globalValues) {
    this._globalValues = globalValues;
  }

  /**
   * Gets the the {@code globalValues} property.
   * @return the property, not null
   */
  public final Property<ManageableUnstructuredMarketDataSnapshot> globalValues() {
    return metaBean().globalValues().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the yield curves in this snapshot
   * @return the value of the property
   */
  public Map<YieldCurveKey, YieldCurveSnapshot> getYieldCurves() {
    return _yieldCurves;
  }

  /**
   * Sets the yield curves in this snapshot
   * @param yieldCurves  the new value of the property
   */
  public void setYieldCurves(Map<YieldCurveKey, YieldCurveSnapshot> yieldCurves) {
    this._yieldCurves = yieldCurves;
  }

  /**
   * Gets the the {@code yieldCurves} property.
   * @return the property, not null
   */
  public final Property<Map<YieldCurveKey, YieldCurveSnapshot>> yieldCurves() {
    return metaBean().yieldCurves().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the yield curves in this snapshot
   * @return the value of the property
   */
  public Map<CurveKey, CurveSnapshot> getCurves() {
    return _curves;
  }

  /**
   * Sets the yield curves in this snapshot
   * @param curves  the new value of the property
   */
  public void setCurves(Map<CurveKey, CurveSnapshot> curves) {
    this._curves = curves;
  }

  /**
   * Gets the the {@code curves} property.
   * @return the property, not null
   */
  public final Property<Map<CurveKey, CurveSnapshot>> curves() {
    return metaBean().curves().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the vol cubes in this snapshot
   * @return the value of the property
   */
  public Map<VolatilityCubeKey, VolatilityCubeSnapshot> getVolatilityCubes() {
    return _volatilityCubes;
  }

  /**
   * Sets the vol cubes in this snapshot
   * @param volatilityCubes  the new value of the property
   */
  public void setVolatilityCubes(Map<VolatilityCubeKey, VolatilityCubeSnapshot> volatilityCubes) {
    this._volatilityCubes = volatilityCubes;
  }

  /**
   * Gets the the {@code volatilityCubes} property.
   * @return the property, not null
   */
  public final Property<Map<VolatilityCubeKey, VolatilityCubeSnapshot>> volatilityCubes() {
    return metaBean().volatilityCubes().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the vol surfaces in this snapshot
   * @return the value of the property
   */
  public Map<VolatilitySurfaceKey, VolatilitySurfaceSnapshot> getVolatilitySurfaces() {
    return _volatilitySurfaces;
  }

  /**
   * Sets the vol surfaces in this snapshot
   * @param volatilitySurfaces  the new value of the property
   */
  public void setVolatilitySurfaces(Map<VolatilitySurfaceKey, VolatilitySurfaceSnapshot> volatilitySurfaces) {
    this._volatilitySurfaces = volatilitySurfaces;
  }

  /**
   * Gets the the {@code volatilitySurfaces} property.
   * @return the property, not null
   */
  public final Property<Map<VolatilitySurfaceKey, VolatilitySurfaceSnapshot>> volatilitySurfaces() {
    return metaBean().volatilitySurfaces().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the surfaces in this snapshot
   * @return the value of the property
   */
  public Map<SurfaceKey, SurfaceSnapshot> getSurfaces() {
    return _surfaces;
  }

  /**
   * Sets the surfaces in this snapshot
   * @param surfaces  the new value of the property
   */
  public void setSurfaces(Map<SurfaceKey, SurfaceSnapshot> surfaces) {
    this._surfaces = surfaces;
  }

  /**
   * Gets the the {@code surfaces} property.
   * @return the property, not null
   */
  public final Property<Map<SurfaceKey, SurfaceSnapshot>> surfaces() {
    return metaBean().surfaces().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Sets the valuation time of the snapshot
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
  @Override
  public ManageableMarketDataSnapshot clone() {
    return JodaBeanUtils.cloneAlways(this);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      ManageableMarketDataSnapshot other = (ManageableMarketDataSnapshot) obj;
      return JodaBeanUtils.equal(getUniqueId(), other.getUniqueId()) &&
          JodaBeanUtils.equal(getName(), other.getName()) &&
          JodaBeanUtils.equal(getBasisViewName(), other.getBasisViewName()) &&
          JodaBeanUtils.equal(getGlobalValues(), other.getGlobalValues()) &&
          JodaBeanUtils.equal(getYieldCurves(), other.getYieldCurves()) &&
          JodaBeanUtils.equal(getCurves(), other.getCurves()) &&
          JodaBeanUtils.equal(getVolatilityCubes(), other.getVolatilityCubes()) &&
          JodaBeanUtils.equal(getVolatilitySurfaces(), other.getVolatilitySurfaces()) &&
          JodaBeanUtils.equal(getSurfaces(), other.getSurfaces()) &&
          JodaBeanUtils.equal(getValuationTime(), other.getValuationTime());
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = getClass().hashCode();
    hash += hash * 31 + JodaBeanUtils.hashCode(getUniqueId());
    hash += hash * 31 + JodaBeanUtils.hashCode(getName());
    hash += hash * 31 + JodaBeanUtils.hashCode(getBasisViewName());
    hash += hash * 31 + JodaBeanUtils.hashCode(getGlobalValues());
    hash += hash * 31 + JodaBeanUtils.hashCode(getYieldCurves());
    hash += hash * 31 + JodaBeanUtils.hashCode(getCurves());
    hash += hash * 31 + JodaBeanUtils.hashCode(getVolatilityCubes());
    hash += hash * 31 + JodaBeanUtils.hashCode(getVolatilitySurfaces());
    hash += hash * 31 + JodaBeanUtils.hashCode(getSurfaces());
    hash += hash * 31 + JodaBeanUtils.hashCode(getValuationTime());
    return hash;
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(352);
    buf.append("ManageableMarketDataSnapshot{");
    int len = buf.length();
    toString(buf);
    if (buf.length() > len) {
      buf.setLength(buf.length() - 2);
    }
    buf.append('}');
    return buf.toString();
  }

  protected void toString(StringBuilder buf) {
    buf.append("uniqueId").append('=').append(JodaBeanUtils.toString(getUniqueId())).append(',').append(' ');
    buf.append("name").append('=').append(JodaBeanUtils.toString(getName())).append(',').append(' ');
    buf.append("basisViewName").append('=').append(JodaBeanUtils.toString(getBasisViewName())).append(',').append(' ');
    buf.append("globalValues").append('=').append(JodaBeanUtils.toString(getGlobalValues())).append(',').append(' ');
    buf.append("yieldCurves").append('=').append(JodaBeanUtils.toString(getYieldCurves())).append(',').append(' ');
    buf.append("curves").append('=').append(JodaBeanUtils.toString(getCurves())).append(',').append(' ');
    buf.append("volatilityCubes").append('=').append(JodaBeanUtils.toString(getVolatilityCubes())).append(',').append(' ');
    buf.append("volatilitySurfaces").append('=').append(JodaBeanUtils.toString(getVolatilitySurfaces())).append(',').append(' ');
    buf.append("surfaces").append('=').append(JodaBeanUtils.toString(getSurfaces())).append(',').append(' ');
    buf.append("valuationTime").append('=').append(JodaBeanUtils.toString(getValuationTime())).append(',').append(' ');
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code ManageableMarketDataSnapshot}.
   */
  public static class Meta extends DirectMetaBean {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code uniqueId} property.
     */
    private final MetaProperty<UniqueId> _uniqueId = DirectMetaProperty.ofReadWrite(
        this, "uniqueId", ManageableMarketDataSnapshot.class, UniqueId.class);
    /**
     * The meta-property for the {@code name} property.
     */
    private final MetaProperty<String> _name = DirectMetaProperty.ofReadWrite(
        this, "name", ManageableMarketDataSnapshot.class, String.class);
    /**
     * The meta-property for the {@code basisViewName} property.
     */
    private final MetaProperty<String> _basisViewName = DirectMetaProperty.ofReadWrite(
        this, "basisViewName", ManageableMarketDataSnapshot.class, String.class);
    /**
     * The meta-property for the {@code globalValues} property.
     */
    private final MetaProperty<ManageableUnstructuredMarketDataSnapshot> _globalValues = DirectMetaProperty.ofReadWrite(
        this, "globalValues", ManageableMarketDataSnapshot.class, ManageableUnstructuredMarketDataSnapshot.class);
    /**
     * The meta-property for the {@code yieldCurves} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<Map<YieldCurveKey, YieldCurveSnapshot>> _yieldCurves = DirectMetaProperty.ofReadWrite(
        this, "yieldCurves", ManageableMarketDataSnapshot.class, (Class) Map.class);
    /**
     * The meta-property for the {@code curves} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<Map<CurveKey, CurveSnapshot>> _curves = DirectMetaProperty.ofReadWrite(
        this, "curves", ManageableMarketDataSnapshot.class, (Class) Map.class);
    /**
     * The meta-property for the {@code volatilityCubes} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<Map<VolatilityCubeKey, VolatilityCubeSnapshot>> _volatilityCubes = DirectMetaProperty.ofReadWrite(
        this, "volatilityCubes", ManageableMarketDataSnapshot.class, (Class) Map.class);
    /**
     * The meta-property for the {@code volatilitySurfaces} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<Map<VolatilitySurfaceKey, VolatilitySurfaceSnapshot>> _volatilitySurfaces = DirectMetaProperty.ofReadWrite(
        this, "volatilitySurfaces", ManageableMarketDataSnapshot.class, (Class) Map.class);
    /**
     * The meta-property for the {@code surfaces} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<Map<SurfaceKey, SurfaceSnapshot>> _surfaces = DirectMetaProperty.ofReadWrite(
        this, "surfaces", ManageableMarketDataSnapshot.class, (Class) Map.class);
    /**
     * The meta-property for the {@code valuationTime} property.
     */
    private final MetaProperty<Instant> _valuationTime = DirectMetaProperty.ofReadWrite(
        this, "valuationTime", ManageableMarketDataSnapshot.class, Instant.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, null,
        "uniqueId",
        "name",
        "basisViewName",
        "globalValues",
        "yieldCurves",
        "curves",
        "volatilityCubes",
        "volatilitySurfaces",
        "surfaces",
        "valuationTime");

    /**
     * Restricted constructor.
     */
    protected Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case -294460212:  // uniqueId
          return _uniqueId;
        case 3373707:  // name
          return _name;
        case 858810670:  // basisViewName
          return _basisViewName;
        case -591591771:  // globalValues
          return _globalValues;
        case 119589713:  // yieldCurves
          return _yieldCurves;
        case -1349116572:  // curves
          return _curves;
        case -2137883207:  // volatilityCubes
          return _volatilityCubes;
        case -1329840981:  // volatilitySurfaces
          return _volatilitySurfaces;
        case -1615615642:  // surfaces
          return _surfaces;
        case 113591406:  // valuationTime
          return _valuationTime;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends ManageableMarketDataSnapshot> builder() {
      return new DirectBeanBuilder<ManageableMarketDataSnapshot>(new ManageableMarketDataSnapshot());
    }

    @Override
    public Class<? extends ManageableMarketDataSnapshot> beanType() {
      return ManageableMarketDataSnapshot.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return _metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code uniqueId} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<UniqueId> uniqueId() {
      return _uniqueId;
    }

    /**
     * The meta-property for the {@code name} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> name() {
      return _name;
    }

    /**
     * The meta-property for the {@code basisViewName} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> basisViewName() {
      return _basisViewName;
    }

    /**
     * The meta-property for the {@code globalValues} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<ManageableUnstructuredMarketDataSnapshot> globalValues() {
      return _globalValues;
    }

    /**
     * The meta-property for the {@code yieldCurves} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Map<YieldCurveKey, YieldCurveSnapshot>> yieldCurves() {
      return _yieldCurves;
    }

    /**
     * The meta-property for the {@code curves} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Map<CurveKey, CurveSnapshot>> curves() {
      return _curves;
    }

    /**
     * The meta-property for the {@code volatilityCubes} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Map<VolatilityCubeKey, VolatilityCubeSnapshot>> volatilityCubes() {
      return _volatilityCubes;
    }

    /**
     * The meta-property for the {@code volatilitySurfaces} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Map<VolatilitySurfaceKey, VolatilitySurfaceSnapshot>> volatilitySurfaces() {
      return _volatilitySurfaces;
    }

    /**
     * The meta-property for the {@code surfaces} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Map<SurfaceKey, SurfaceSnapshot>> surfaces() {
      return _surfaces;
    }

    /**
     * The meta-property for the {@code valuationTime} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Instant> valuationTime() {
      return _valuationTime;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -294460212:  // uniqueId
          return ((ManageableMarketDataSnapshot) bean).getUniqueId();
        case 3373707:  // name
          return ((ManageableMarketDataSnapshot) bean).getName();
        case 858810670:  // basisViewName
          return ((ManageableMarketDataSnapshot) bean).getBasisViewName();
        case -591591771:  // globalValues
          return ((ManageableMarketDataSnapshot) bean).getGlobalValues();
        case 119589713:  // yieldCurves
          return ((ManageableMarketDataSnapshot) bean).getYieldCurves();
        case -1349116572:  // curves
          return ((ManageableMarketDataSnapshot) bean).getCurves();
        case -2137883207:  // volatilityCubes
          return ((ManageableMarketDataSnapshot) bean).getVolatilityCubes();
        case -1329840981:  // volatilitySurfaces
          return ((ManageableMarketDataSnapshot) bean).getVolatilitySurfaces();
        case -1615615642:  // surfaces
          return ((ManageableMarketDataSnapshot) bean).getSurfaces();
        case 113591406:  // valuationTime
          return ((ManageableMarketDataSnapshot) bean).getValuationTime();
      }
      return super.propertyGet(bean, propertyName, quiet);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -294460212:  // uniqueId
          ((ManageableMarketDataSnapshot) bean).setUniqueId((UniqueId) newValue);
          return;
        case 3373707:  // name
          ((ManageableMarketDataSnapshot) bean).setName((String) newValue);
          return;
        case 858810670:  // basisViewName
          ((ManageableMarketDataSnapshot) bean).setBasisViewName((String) newValue);
          return;
        case -591591771:  // globalValues
          ((ManageableMarketDataSnapshot) bean).setGlobalValues((ManageableUnstructuredMarketDataSnapshot) newValue);
          return;
        case 119589713:  // yieldCurves
          ((ManageableMarketDataSnapshot) bean).setYieldCurves((Map<YieldCurveKey, YieldCurveSnapshot>) newValue);
          return;
        case -1349116572:  // curves
          ((ManageableMarketDataSnapshot) bean).setCurves((Map<CurveKey, CurveSnapshot>) newValue);
          return;
        case -2137883207:  // volatilityCubes
          ((ManageableMarketDataSnapshot) bean).setVolatilityCubes((Map<VolatilityCubeKey, VolatilityCubeSnapshot>) newValue);
          return;
        case -1329840981:  // volatilitySurfaces
          ((ManageableMarketDataSnapshot) bean).setVolatilitySurfaces((Map<VolatilitySurfaceKey, VolatilitySurfaceSnapshot>) newValue);
          return;
        case -1615615642:  // surfaces
          ((ManageableMarketDataSnapshot) bean).setSurfaces((Map<SurfaceKey, SurfaceSnapshot>) newValue);
          return;
        case 113591406:  // valuationTime
          ((ManageableMarketDataSnapshot) bean).setValuationTime((Instant) newValue);
          return;
      }
      super.propertySet(bean, propertyName, newValue, quiet);
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
