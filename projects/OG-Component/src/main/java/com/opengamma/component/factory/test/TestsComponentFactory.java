/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.component.factory.test;

import java.util.LinkedHashMap;
import java.util.Map;

import org.joda.beans.Bean;
import org.joda.beans.BeanBuilder;
import org.joda.beans.BeanDefinition;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaProperty;
import org.joda.beans.Property;
import org.joda.beans.PropertyDefinition;
import org.joda.beans.impl.direct.DirectBeanBuilder;
import org.joda.beans.impl.direct.DirectMetaProperty;
import org.joda.beans.impl.direct.DirectMetaPropertyMap;

import com.opengamma.component.ComponentInfo;
import com.opengamma.component.ComponentRepository;
import com.opengamma.component.factory.AbstractComponentFactory;
import com.opengamma.core.marketdatasnapshot.MarketDataSnapshotSource;
import com.opengamma.core.marketdatasnapshot.impl.DataMarketDataSnapshotSourceResource;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.core.security.impl.DataSecuritySourceResource;
import com.opengamma.financial.analytics.ircurve.InMemoryInterpolatedYieldCurveDefinitionMaster;
import com.opengamma.financial.analytics.ircurve.InterpolatedYieldCurveDefinitionMaster;
import com.opengamma.financial.analytics.ircurve.InterpolatedYieldCurveDefinitionSource;
import com.opengamma.financial.analytics.ircurve.rest.DataInterpolatedYieldCurveDefinitionMasterResource;
import com.opengamma.financial.analytics.ircurve.rest.DataInterpolatedYieldCurveDefinitionSourceResource;
import com.opengamma.financial.security.MasterFinancialSecuritySource;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotMaster;
import com.opengamma.master.marketdatasnapshot.impl.DataMarketDataSnapshotMasterResource;
import com.opengamma.master.marketdatasnapshot.impl.InMemorySnapshotMaster;
import com.opengamma.master.marketdatasnapshot.impl.MasterSnapshotSource;
import com.opengamma.master.security.SecurityMaster;
import com.opengamma.master.security.impl.DataSecurityMasterResource;
import com.opengamma.master.security.impl.InMemorySecurityMaster;

/**
 * Component factory for setting up test masters and sources.
 */
@BeanDefinition
public class TestsComponentFactory extends AbstractComponentFactory {

  @PropertyDefinition
  private boolean _enableSecurities;

  @PropertyDefinition
  private boolean _enableSnapshots;

  @PropertyDefinition
  private boolean _enableYieldCurves;

  //-------------------------------------------------------------------------
  @Override
  public void init(final ComponentRepository repo, final LinkedHashMap<String, String> configuration) {
    if (isEnableSecurities()) {
      initSecurities(repo);
    }
    if (isEnableSnapshots()) {
      initSnapshots(repo);
    }
    if (isEnableYieldCurves()) {
      initYieldCurves(repo);
    }
  }

  protected void initSecurities(final ComponentRepository repo) {
    final SecurityMaster master = new InMemorySecurityMaster();
    final MasterFinancialSecuritySource source = new MasterFinancialSecuritySource(master);

    final ComponentInfo infoMaster = new ComponentInfo(SecurityMaster.class, "test");
    repo.registerComponent(infoMaster, master);
    repo.getRestComponents().publish(infoMaster, new DataSecurityMasterResource(master));

    final ComponentInfo infoSource = new ComponentInfo(SecuritySource.class, "test");
    repo.registerComponent(infoSource, source);
    repo.getRestComponents().publish(infoSource, new DataSecuritySourceResource(source));
  }

  protected void initSnapshots(final ComponentRepository repo) {
    final MarketDataSnapshotMaster master = new InMemorySnapshotMaster();
    final MarketDataSnapshotSource source = new MasterSnapshotSource(master);

    final ComponentInfo infoMaster = new ComponentInfo(MarketDataSnapshotMaster.class, "test");
    repo.registerComponent(infoMaster, master);
    repo.getRestComponents().publish(infoMaster, new DataMarketDataSnapshotMasterResource(master));

    final ComponentInfo infoSource = new ComponentInfo(MarketDataSnapshotSource.class, "test");
    repo.registerComponent(infoSource, source);
    repo.getRestComponents().publish(infoSource, new DataMarketDataSnapshotSourceResource(source));
  }

  protected void initYieldCurves(final ComponentRepository repo) {
    final InMemoryInterpolatedYieldCurveDefinitionMaster masterAndSource = new InMemoryInterpolatedYieldCurveDefinitionMaster();
    masterAndSource.setUniqueIdScheme("TestCurves");

    final ComponentInfo infoMaster = new ComponentInfo(InterpolatedYieldCurveDefinitionMaster.class, "test");
    repo.registerComponent(infoMaster, masterAndSource);
    repo.getRestComponents().publish(infoMaster, new DataInterpolatedYieldCurveDefinitionMasterResource(masterAndSource));

    final ComponentInfo infoSource = new ComponentInfo(InterpolatedYieldCurveDefinitionSource.class, "test");
    repo.registerComponent(infoSource, masterAndSource);
    repo.getRestComponents().publish(infoSource, new DataInterpolatedYieldCurveDefinitionSourceResource(masterAndSource));
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code TestsComponentFactory}.
   * @return the meta-bean, not null
   */
  public static TestsComponentFactory.Meta meta() {
    return TestsComponentFactory.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(TestsComponentFactory.Meta.INSTANCE);
  }

  @Override
  public TestsComponentFactory.Meta metaBean() {
    return TestsComponentFactory.Meta.INSTANCE;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the enableSecurities.
   * @return the value of the property
   */
  public boolean isEnableSecurities() {
    return _enableSecurities;
  }

  /**
   * Sets the enableSecurities.
   * @param enableSecurities  the new value of the property
   */
  public void setEnableSecurities(boolean enableSecurities) {
    this._enableSecurities = enableSecurities;
  }

  /**
   * Gets the the {@code enableSecurities} property.
   * @return the property, not null
   */
  public final Property<Boolean> enableSecurities() {
    return metaBean().enableSecurities().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the enableSnapshots.
   * @return the value of the property
   */
  public boolean isEnableSnapshots() {
    return _enableSnapshots;
  }

  /**
   * Sets the enableSnapshots.
   * @param enableSnapshots  the new value of the property
   */
  public void setEnableSnapshots(boolean enableSnapshots) {
    this._enableSnapshots = enableSnapshots;
  }

  /**
   * Gets the the {@code enableSnapshots} property.
   * @return the property, not null
   */
  public final Property<Boolean> enableSnapshots() {
    return metaBean().enableSnapshots().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the enableYieldCurves.
   * @return the value of the property
   */
  public boolean isEnableYieldCurves() {
    return _enableYieldCurves;
  }

  /**
   * Sets the enableYieldCurves.
   * @param enableYieldCurves  the new value of the property
   */
  public void setEnableYieldCurves(boolean enableYieldCurves) {
    this._enableYieldCurves = enableYieldCurves;
  }

  /**
   * Gets the the {@code enableYieldCurves} property.
   * @return the property, not null
   */
  public final Property<Boolean> enableYieldCurves() {
    return metaBean().enableYieldCurves().createProperty(this);
  }

  //-----------------------------------------------------------------------
  @Override
  public TestsComponentFactory clone() {
    return JodaBeanUtils.cloneAlways(this);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      TestsComponentFactory other = (TestsComponentFactory) obj;
      return (isEnableSecurities() == other.isEnableSecurities()) &&
          (isEnableSnapshots() == other.isEnableSnapshots()) &&
          (isEnableYieldCurves() == other.isEnableYieldCurves()) &&
          super.equals(obj);
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash += hash * 31 + JodaBeanUtils.hashCode(isEnableSecurities());
    hash += hash * 31 + JodaBeanUtils.hashCode(isEnableSnapshots());
    hash += hash * 31 + JodaBeanUtils.hashCode(isEnableYieldCurves());
    return hash ^ super.hashCode();
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(128);
    buf.append("TestsComponentFactory{");
    int len = buf.length();
    toString(buf);
    if (buf.length() > len) {
      buf.setLength(buf.length() - 2);
    }
    buf.append('}');
    return buf.toString();
  }

  @Override
  protected void toString(StringBuilder buf) {
    super.toString(buf);
    buf.append("enableSecurities").append('=').append(JodaBeanUtils.toString(isEnableSecurities())).append(',').append(' ');
    buf.append("enableSnapshots").append('=').append(JodaBeanUtils.toString(isEnableSnapshots())).append(',').append(' ');
    buf.append("enableYieldCurves").append('=').append(JodaBeanUtils.toString(isEnableYieldCurves())).append(',').append(' ');
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code TestsComponentFactory}.
   */
  public static class Meta extends AbstractComponentFactory.Meta {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code enableSecurities} property.
     */
    private final MetaProperty<Boolean> _enableSecurities = DirectMetaProperty.ofReadWrite(
        this, "enableSecurities", TestsComponentFactory.class, Boolean.TYPE);
    /**
     * The meta-property for the {@code enableSnapshots} property.
     */
    private final MetaProperty<Boolean> _enableSnapshots = DirectMetaProperty.ofReadWrite(
        this, "enableSnapshots", TestsComponentFactory.class, Boolean.TYPE);
    /**
     * The meta-property for the {@code enableYieldCurves} property.
     */
    private final MetaProperty<Boolean> _enableYieldCurves = DirectMetaProperty.ofReadWrite(
        this, "enableYieldCurves", TestsComponentFactory.class, Boolean.TYPE);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, (DirectMetaPropertyMap) super.metaPropertyMap(),
        "enableSecurities",
        "enableSnapshots",
        "enableYieldCurves");

    /**
     * Restricted constructor.
     */
    protected Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case 1339404737:  // enableSecurities
          return _enableSecurities;
        case -1983160084:  // enableSnapshots
          return _enableSnapshots;
        case 1436798414:  // enableYieldCurves
          return _enableYieldCurves;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends TestsComponentFactory> builder() {
      return new DirectBeanBuilder<TestsComponentFactory>(new TestsComponentFactory());
    }

    @Override
    public Class<? extends TestsComponentFactory> beanType() {
      return TestsComponentFactory.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return _metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code enableSecurities} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Boolean> enableSecurities() {
      return _enableSecurities;
    }

    /**
     * The meta-property for the {@code enableSnapshots} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Boolean> enableSnapshots() {
      return _enableSnapshots;
    }

    /**
     * The meta-property for the {@code enableYieldCurves} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Boolean> enableYieldCurves() {
      return _enableYieldCurves;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case 1339404737:  // enableSecurities
          return ((TestsComponentFactory) bean).isEnableSecurities();
        case -1983160084:  // enableSnapshots
          return ((TestsComponentFactory) bean).isEnableSnapshots();
        case 1436798414:  // enableYieldCurves
          return ((TestsComponentFactory) bean).isEnableYieldCurves();
      }
      return super.propertyGet(bean, propertyName, quiet);
    }

    @Override
    protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {
      switch (propertyName.hashCode()) {
        case 1339404737:  // enableSecurities
          ((TestsComponentFactory) bean).setEnableSecurities((Boolean) newValue);
          return;
        case -1983160084:  // enableSnapshots
          ((TestsComponentFactory) bean).setEnableSnapshots((Boolean) newValue);
          return;
        case 1436798414:  // enableYieldCurves
          ((TestsComponentFactory) bean).setEnableYieldCurves((Boolean) newValue);
          return;
      }
      super.propertySet(bean, propertyName, newValue, quiet);
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
