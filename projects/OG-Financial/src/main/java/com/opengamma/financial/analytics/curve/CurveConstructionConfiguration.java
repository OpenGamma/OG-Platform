/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.curve;

import java.io.Serializable;
import java.util.List;
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

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.opengamma.core.config.Config;
import com.opengamma.core.config.ConfigGroups;
import com.opengamma.core.link.ConfigLink;
import com.opengamma.core.link.SnapshotLink;
import com.opengamma.financial.analytics.parameters.HullWhiteOneFactorParametersSnapshot;
import com.opengamma.id.MutableUniqueIdentifiable;
import com.opengamma.id.UniqueId;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.service.ServiceContext;

/**
 * Configuration object that contains a list of {@link CurveGroupConfiguration}s and any
 * exogenous curve configurations that are required. Also
 */
@BeanDefinition
@Config(description = "Curve construction configuration", group = ConfigGroups.CURVES)
public class CurveConstructionConfiguration extends DirectBean implements Serializable, UniqueIdentifiable, MutableUniqueIdentifiable {

  /** Serialization version */
  private static final long serialVersionUID = 1L;

  /**
   * The unique id.
   */
  @PropertyDefinition
  private UniqueId _uniqueId;

  /**
   * The name of this configuration.
   */
  @PropertyDefinition(validate = "notNull")
  private String _name;

  /**
   * The curve groups.
   */
  @PropertyDefinition(validate = "notNull")
  private List<CurveGroupConfiguration> _curveGroups;

  /**
   * The name(s) of any exogenous curve configurations.
   */
  @PropertyDefinition(set = "manual", validate = "notNull")
  private List<String> _exogenousConfigurations = ImmutableList.of();

  /**
   * A list of snapshot links holding Hull White convexity adjustment
   * parameters. If parameter snapshots are available then the first
   * applicable one will be used during curve calibration if futures
   * are specified in the curve.
   */
  @PropertyDefinition(validate = "notNull")
  private List<SnapshotLink<HullWhiteOneFactorParametersSnapshot>> _convexityAdjustmentLinks = ImmutableList.of();

  /**
   * Indicates if convexity adjustments are defined whether they
   * should be applied for curves in this configuration. By default
   * they will be applied.
   */
  @PropertyDefinition
  private boolean _applyConvexityAdjustment = true;

  /**
   * The links to any exogenous curve configs.
   * Currently these are private - in the future we may want to expose the links directly.
   */
  private List<ConfigLink<CurveConstructionConfiguration>> _exogenousLinks = ImmutableList.of();

  /**
   * Creates a CurveConstructionConfiguration. Intended for use by the builder.
   */
  CurveConstructionConfiguration() {
  }

  /**
   * Creates a CurveConstructionConfiguration.
   *
   * @param name The curve construction configuration name, not null
   * @param curveGroups The curve groups, not null
   * @param exogenousConfigurations The exogenous configuration to be used in curve construction
   */
  public CurveConstructionConfiguration(String name, List<CurveGroupConfiguration> curveGroups,
                                        List<String> exogenousConfigurations) {
    this(name, curveGroups, exogenousConfigurations,
         ImmutableList.<SnapshotLink<HullWhiteOneFactorParametersSnapshot>>of(), true);
  }

  /**
   * Creates a CurveConstructionConfiguration.
   *
   * @param name  the curve construction configuration name, not null
   * @param curveGroups  the curve groups, not null
   * @param exogenousConfigurations  the exogenous configuration to be
   *   used in curve construction
   * @param convexityAdjustmentParameterLinks  a list of snapshot links
   *   holding Hull White convexity adjustment parameters. If parameter
   *   snapshots are available then the first applicable one will be
   *   used during curve calibration if futures are specified in the curve.
   * @param applyConvexityAdjustment  if convexity adjustments are defined,
   *   whether they should be applied for curves in this configuration
   */
  public CurveConstructionConfiguration(
      String name,
      List<CurveGroupConfiguration> curveGroups,
      List<String> exogenousConfigurations,
      List<SnapshotLink<HullWhiteOneFactorParametersSnapshot>> convexityAdjustmentParameterLinks,
      boolean applyConvexityAdjustment) {
    setName(name);
    setCurveGroups(curveGroups);
    setExogenousConfigurations(exogenousConfigurations);
    setConvexityAdjustmentLinks(convexityAdjustmentParameterLinks);
    setApplyConvexityAdjustment(applyConvexityAdjustment);
  }

  /**
   * Gets the resolved exogenous configurations. Note that this method
   * requires the use of a {@link ServiceContext} and will throw an
   * exception without one. Note also that in order to resolve the
   * configs it may invoke database calls.
   *
   * @return the exogenous configurations, not null
   * @throws IllegalStateException if no {@link ServiceContext} is available
   */
  public List<CurveConstructionConfiguration> resolveCurveConfigurations() {

    return Lists.transform(_exogenousLinks,
        new Function<ConfigLink<CurveConstructionConfiguration>, CurveConstructionConfiguration>() {
          @Override
          public CurveConstructionConfiguration apply(final ConfigLink<CurveConstructionConfiguration> input) {
            return input.resolve();
          }
        }
    );
  }

  /**
   * Sets the name(s) of any exogenous curve configurations. Additionally, sets up
   * config links for the exogenous curves.
   *
   * @param exogenousConfigurations the new value of the property, not null
   */
  public void setExogenousConfigurations(final List<String> exogenousConfigurations) {

    // if empty, we already have suitable defaults configured
    if (!exogenousConfigurations.isEmpty()) {
      _exogenousConfigurations = exogenousConfigurations;
      _exogenousLinks = Lists.transform(_exogenousConfigurations,
          new Function<String, ConfigLink<CurveConstructionConfiguration>>() {
            @Override
            public ConfigLink<CurveConstructionConfiguration> apply(final String config) {
              return ConfigLink.resolvable(config, CurveConstructionConfiguration.class);
            }
          }
      );
    }
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code CurveConstructionConfiguration}.
   * @return the meta-bean, not null
   */
  public static CurveConstructionConfiguration.Meta meta() {
    return CurveConstructionConfiguration.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(CurveConstructionConfiguration.Meta.INSTANCE);
  }

  @Override
  public CurveConstructionConfiguration.Meta metaBean() {
    return CurveConstructionConfiguration.Meta.INSTANCE;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the unique id.
   * @return the value of the property
   */
  public UniqueId getUniqueId() {
    return _uniqueId;
  }

  /**
   * Sets the unique id.
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
   * Gets the name of this configuration.
   * @return the value of the property, not null
   */
  public String getName() {
    return _name;
  }

  /**
   * Sets the name of this configuration.
   * @param name  the new value of the property, not null
   */
  public void setName(String name) {
    JodaBeanUtils.notNull(name, "name");
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
   * Gets the curve groups.
   * @return the value of the property, not null
   */
  public List<CurveGroupConfiguration> getCurveGroups() {
    return _curveGroups;
  }

  /**
   * Sets the curve groups.
   * @param curveGroups  the new value of the property, not null
   */
  public void setCurveGroups(List<CurveGroupConfiguration> curveGroups) {
    JodaBeanUtils.notNull(curveGroups, "curveGroups");
    this._curveGroups = curveGroups;
  }

  /**
   * Gets the the {@code curveGroups} property.
   * @return the property, not null
   */
  public final Property<List<CurveGroupConfiguration>> curveGroups() {
    return metaBean().curveGroups().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the name(s) of any exogenous curve configurations.
   * @return the value of the property, not null
   */
  public List<String> getExogenousConfigurations() {
    return _exogenousConfigurations;
  }

  /**
   * Gets the the {@code exogenousConfigurations} property.
   * @return the property, not null
   */
  public final Property<List<String>> exogenousConfigurations() {
    return metaBean().exogenousConfigurations().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets a list of snapshot links holding Hull White convexity adjustment
   * parameters. If parameter snapshots are available then the first
   * applicable one will be used during curve calibration if futures
   * are specified in the curve.
   * @return the value of the property, not null
   */
  public List<SnapshotLink<HullWhiteOneFactorParametersSnapshot>> getConvexityAdjustmentLinks() {
    return _convexityAdjustmentLinks;
  }

  /**
   * Sets a list of snapshot links holding Hull White convexity adjustment
   * parameters. If parameter snapshots are available then the first
   * applicable one will be used during curve calibration if futures
   * are specified in the curve.
   * @param convexityAdjustmentLinks  the new value of the property, not null
   */
  public void setConvexityAdjustmentLinks(List<SnapshotLink<HullWhiteOneFactorParametersSnapshot>> convexityAdjustmentLinks) {
    JodaBeanUtils.notNull(convexityAdjustmentLinks, "convexityAdjustmentLinks");
    this._convexityAdjustmentLinks = convexityAdjustmentLinks;
  }

  /**
   * Gets the the {@code convexityAdjustmentLinks} property.
   * parameters. If parameter snapshots are available then the first
   * applicable one will be used during curve calibration if futures
   * are specified in the curve.
   * @return the property, not null
   */
  public final Property<List<SnapshotLink<HullWhiteOneFactorParametersSnapshot>>> convexityAdjustmentLinks() {
    return metaBean().convexityAdjustmentLinks().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets indicates if convexity adjustments are defined whether they
   * should be applied for curves in this configuration. By default
   * they will be applied.
   * @return the value of the property
   */
  public boolean isApplyConvexityAdjustment() {
    return _applyConvexityAdjustment;
  }

  /**
   * Sets indicates if convexity adjustments are defined whether they
   * should be applied for curves in this configuration. By default
   * they will be applied.
   * @param applyConvexityAdjustment  the new value of the property
   */
  public void setApplyConvexityAdjustment(boolean applyConvexityAdjustment) {
    this._applyConvexityAdjustment = applyConvexityAdjustment;
  }

  /**
   * Gets the the {@code applyConvexityAdjustment} property.
   * should be applied for curves in this configuration. By default
   * they will be applied.
   * @return the property, not null
   */
  public final Property<Boolean> applyConvexityAdjustment() {
    return metaBean().applyConvexityAdjustment().createProperty(this);
  }

  //-----------------------------------------------------------------------
  @Override
  public CurveConstructionConfiguration clone() {
    return JodaBeanUtils.cloneAlways(this);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      CurveConstructionConfiguration other = (CurveConstructionConfiguration) obj;
      return JodaBeanUtils.equal(getUniqueId(), other.getUniqueId()) &&
          JodaBeanUtils.equal(getName(), other.getName()) &&
          JodaBeanUtils.equal(getCurveGroups(), other.getCurveGroups()) &&
          JodaBeanUtils.equal(getExogenousConfigurations(), other.getExogenousConfigurations()) &&
          JodaBeanUtils.equal(getConvexityAdjustmentLinks(), other.getConvexityAdjustmentLinks()) &&
          (isApplyConvexityAdjustment() == other.isApplyConvexityAdjustment());
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = getClass().hashCode();
    hash += hash * 31 + JodaBeanUtils.hashCode(getUniqueId());
    hash += hash * 31 + JodaBeanUtils.hashCode(getName());
    hash += hash * 31 + JodaBeanUtils.hashCode(getCurveGroups());
    hash += hash * 31 + JodaBeanUtils.hashCode(getExogenousConfigurations());
    hash += hash * 31 + JodaBeanUtils.hashCode(getConvexityAdjustmentLinks());
    hash += hash * 31 + JodaBeanUtils.hashCode(isApplyConvexityAdjustment());
    return hash;
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(224);
    buf.append("CurveConstructionConfiguration{");
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
    buf.append("curveGroups").append('=').append(JodaBeanUtils.toString(getCurveGroups())).append(',').append(' ');
    buf.append("exogenousConfigurations").append('=').append(JodaBeanUtils.toString(getExogenousConfigurations())).append(',').append(' ');
    buf.append("convexityAdjustmentLinks").append('=').append(JodaBeanUtils.toString(getConvexityAdjustmentLinks())).append(',').append(' ');
    buf.append("applyConvexityAdjustment").append('=').append(JodaBeanUtils.toString(isApplyConvexityAdjustment())).append(',').append(' ');
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code CurveConstructionConfiguration}.
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
        this, "uniqueId", CurveConstructionConfiguration.class, UniqueId.class);
    /**
     * The meta-property for the {@code name} property.
     */
    private final MetaProperty<String> _name = DirectMetaProperty.ofReadWrite(
        this, "name", CurveConstructionConfiguration.class, String.class);
    /**
     * The meta-property for the {@code curveGroups} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<List<CurveGroupConfiguration>> _curveGroups = DirectMetaProperty.ofReadWrite(
        this, "curveGroups", CurveConstructionConfiguration.class, (Class) List.class);
    /**
     * The meta-property for the {@code exogenousConfigurations} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<List<String>> _exogenousConfigurations = DirectMetaProperty.ofReadWrite(
        this, "exogenousConfigurations", CurveConstructionConfiguration.class, (Class) List.class);
    /**
     * The meta-property for the {@code convexityAdjustmentLinks} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<List<SnapshotLink<HullWhiteOneFactorParametersSnapshot>>> _convexityAdjustmentLinks = DirectMetaProperty.ofReadWrite(
        this, "convexityAdjustmentLinks", CurveConstructionConfiguration.class, (Class) List.class);
    /**
     * The meta-property for the {@code applyConvexityAdjustment} property.
     */
    private final MetaProperty<Boolean> _applyConvexityAdjustment = DirectMetaProperty.ofReadWrite(
        this, "applyConvexityAdjustment", CurveConstructionConfiguration.class, Boolean.TYPE);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, null,
        "uniqueId",
        "name",
        "curveGroups",
        "exogenousConfigurations",
        "convexityAdjustmentLinks",
        "applyConvexityAdjustment");

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
        case -2135025757:  // curveGroups
          return _curveGroups;
        case -1107510858:  // exogenousConfigurations
          return _exogenousConfigurations;
        case 1502752485:  // convexityAdjustmentLinks
          return _convexityAdjustmentLinks;
        case 980046566:  // applyConvexityAdjustment
          return _applyConvexityAdjustment;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends CurveConstructionConfiguration> builder() {
      return new DirectBeanBuilder<CurveConstructionConfiguration>(new CurveConstructionConfiguration());
    }

    @Override
    public Class<? extends CurveConstructionConfiguration> beanType() {
      return CurveConstructionConfiguration.class;
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
     * The meta-property for the {@code curveGroups} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<List<CurveGroupConfiguration>> curveGroups() {
      return _curveGroups;
    }

    /**
     * The meta-property for the {@code exogenousConfigurations} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<List<String>> exogenousConfigurations() {
      return _exogenousConfigurations;
    }

    /**
     * The meta-property for the {@code convexityAdjustmentLinks} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<List<SnapshotLink<HullWhiteOneFactorParametersSnapshot>>> convexityAdjustmentLinks() {
      return _convexityAdjustmentLinks;
    }

    /**
     * The meta-property for the {@code applyConvexityAdjustment} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Boolean> applyConvexityAdjustment() {
      return _applyConvexityAdjustment;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -294460212:  // uniqueId
          return ((CurveConstructionConfiguration) bean).getUniqueId();
        case 3373707:  // name
          return ((CurveConstructionConfiguration) bean).getName();
        case -2135025757:  // curveGroups
          return ((CurveConstructionConfiguration) bean).getCurveGroups();
        case -1107510858:  // exogenousConfigurations
          return ((CurveConstructionConfiguration) bean).getExogenousConfigurations();
        case 1502752485:  // convexityAdjustmentLinks
          return ((CurveConstructionConfiguration) bean).getConvexityAdjustmentLinks();
        case 980046566:  // applyConvexityAdjustment
          return ((CurveConstructionConfiguration) bean).isApplyConvexityAdjustment();
      }
      return super.propertyGet(bean, propertyName, quiet);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -294460212:  // uniqueId
          ((CurveConstructionConfiguration) bean).setUniqueId((UniqueId) newValue);
          return;
        case 3373707:  // name
          ((CurveConstructionConfiguration) bean).setName((String) newValue);
          return;
        case -2135025757:  // curveGroups
          ((CurveConstructionConfiguration) bean).setCurveGroups((List<CurveGroupConfiguration>) newValue);
          return;
        case -1107510858:  // exogenousConfigurations
          ((CurveConstructionConfiguration) bean).setExogenousConfigurations((List<String>) newValue);
          return;
        case 1502752485:  // convexityAdjustmentLinks
          ((CurveConstructionConfiguration) bean).setConvexityAdjustmentLinks((List<SnapshotLink<HullWhiteOneFactorParametersSnapshot>>) newValue);
          return;
        case 980046566:  // applyConvexityAdjustment
          ((CurveConstructionConfiguration) bean).setApplyConvexityAdjustment((Boolean) newValue);
          return;
      }
      super.propertySet(bean, propertyName, newValue, quiet);
    }

    @Override
    protected void validate(Bean bean) {
      JodaBeanUtils.notNull(((CurveConstructionConfiguration) bean)._name, "name");
      JodaBeanUtils.notNull(((CurveConstructionConfiguration) bean)._curveGroups, "curveGroups");
      JodaBeanUtils.notNull(((CurveConstructionConfiguration) bean)._exogenousConfigurations, "exogenousConfigurations");
      JodaBeanUtils.notNull(((CurveConstructionConfiguration) bean)._convexityAdjustmentLinks, "convexityAdjustmentLinks");
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
