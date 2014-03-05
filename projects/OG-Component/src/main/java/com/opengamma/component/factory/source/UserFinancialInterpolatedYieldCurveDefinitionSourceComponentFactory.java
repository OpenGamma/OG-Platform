/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.component.factory.source;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import net.sf.ehcache.CacheManager;

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
import com.opengamma.component.factory.ComponentInfoAttributes;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.financial.analytics.ircurve.AggregatingInterpolatedYieldCurveDefinitionSource;
import com.opengamma.financial.analytics.ircurve.ConfigDBInterpolatedYieldCurveDefinitionSource;
import com.opengamma.financial.analytics.ircurve.EHCachingInterpolatedYieldCurveDefinitionSource;
import com.opengamma.financial.analytics.ircurve.InMemoryInterpolatedYieldCurveDefinitionMaster;
import com.opengamma.financial.analytics.ircurve.InterpolatedYieldCurveDefinitionMaster;
import com.opengamma.financial.analytics.ircurve.InterpolatedYieldCurveDefinitionSource;
import com.opengamma.financial.analytics.ircurve.rest.DataInterpolatedYieldCurveDefinitionMasterResource;
import com.opengamma.financial.analytics.ircurve.rest.DataInterpolatedYieldCurveDefinitionSourceResource;
import com.opengamma.financial.analytics.ircurve.rest.RemoteInterpolatedYieldCurveDefinitionMaster;
import com.opengamma.financial.analytics.ircurve.rest.RemoteInterpolatedYieldCurveDefinitionSource;

/**
 * Component factory for the yield curve definition source.
 * <p>
 * This factory creates yield curve definition sources for the underlying and user masters
 * as well as a combined source.
 */
@BeanDefinition
public class UserFinancialInterpolatedYieldCurveDefinitionSourceComponentFactory extends AbstractComponentFactory {

  /**
   * The classifier that the factory should publish under.
   */
  @PropertyDefinition(validate = "notNull")
  private String _classifier;
  /**
   * The flag determining whether the component should be published by REST (default true).
   */
  @PropertyDefinition
  private boolean _publishRest = true;
  /**
   * The cache manager.
   */
  @PropertyDefinition
  private CacheManager _cacheManager;
  /**
   * The classifier that the factory should publish under (underlying source).
   */
  @PropertyDefinition
  private String _underlyingClassifier;
  /**
   * The config source (underlying source).
   */
  @PropertyDefinition(validate = "notNull")
  private ConfigSource _underlyingConfigSource;
  /**
   * The classifier that the factory should publish under (user source).
   */
  @PropertyDefinition
  private String _userClassifier;

  //-------------------------------------------------------------------------
  @Override
  public void init(ComponentRepository repo, LinkedHashMap<String, String> configuration) {
    InterpolatedYieldCurveDefinitionSource source = initUnderlying(repo, configuration);
    
    // add user level if requested
    InterpolatedYieldCurveDefinitionSource userSource = initUser(repo, configuration);
    if (userSource != null) {
      Collection<InterpolatedYieldCurveDefinitionSource> coll = new ArrayList<InterpolatedYieldCurveDefinitionSource>();
      coll.add(source);
      coll.add(userSource);
      source = new AggregatingInterpolatedYieldCurveDefinitionSource(coll);
    }
    
    // register
    ComponentInfo info = new ComponentInfo(InterpolatedYieldCurveDefinitionSource.class, getClassifier());
    info.addAttribute(ComponentInfoAttributes.LEVEL, 2);
    info.addAttribute(ComponentInfoAttributes.REMOTE_CLIENT_JAVA, RemoteInterpolatedYieldCurveDefinitionSource.class);
    repo.registerComponent(info, source);
    
    if (isPublishRest()) {
      repo.getRestComponents().publish(info, new DataInterpolatedYieldCurveDefinitionSourceResource(source));
    }
  }

  protected InterpolatedYieldCurveDefinitionSource initUnderlying(ComponentRepository repo, LinkedHashMap<String, String> configuration) {
    InterpolatedYieldCurveDefinitionSource source = new ConfigDBInterpolatedYieldCurveDefinitionSource(getUnderlyingConfigSource());
    if (getCacheManager() != null) {
      source = new EHCachingInterpolatedYieldCurveDefinitionSource(source, getCacheManager());
    }
    if (getUnderlyingClassifier() != null) {
      ComponentInfo info = new ComponentInfo(InterpolatedYieldCurveDefinitionSource.class, getUnderlyingClassifier());
      info.addAttribute(ComponentInfoAttributes.LEVEL, 1);
      info.addAttribute(ComponentInfoAttributes.REMOTE_CLIENT_JAVA, RemoteInterpolatedYieldCurveDefinitionSource.class);
      repo.registerComponent(info, source);
      
      if (isPublishRest()) {
        repo.getRestComponents().publish(info, new DataInterpolatedYieldCurveDefinitionSourceResource(source));
      }
    }
    return source;
  }

  protected InterpolatedYieldCurveDefinitionSource initUser(ComponentRepository repo, LinkedHashMap<String, String> configuration) {
    if (getUserClassifier() == null) {
      return null;
    }
    InMemoryInterpolatedYieldCurveDefinitionMaster masterAndSource = new InMemoryInterpolatedYieldCurveDefinitionMaster();
    ComponentInfo infoMaster = new ComponentInfo(InterpolatedYieldCurveDefinitionMaster.class, getUserClassifier());
    infoMaster.addAttribute(ComponentInfoAttributes.LEVEL, 1);
    infoMaster.addAttribute(ComponentInfoAttributes.REMOTE_CLIENT_JAVA, RemoteInterpolatedYieldCurveDefinitionMaster.class);
    repo.registerComponent(infoMaster, masterAndSource);
    ComponentInfo infoSource = new ComponentInfo(InterpolatedYieldCurveDefinitionSource.class, getUserClassifier());
    infoSource.addAttribute(ComponentInfoAttributes.LEVEL, 1);
    infoSource.addAttribute(ComponentInfoAttributes.REMOTE_CLIENT_JAVA, RemoteInterpolatedYieldCurveDefinitionSource.class);
    repo.registerComponent(infoSource, masterAndSource);
    
    if (isPublishRest()) {
      repo.getRestComponents().publish(infoMaster, new DataInterpolatedYieldCurveDefinitionMasterResource(masterAndSource));
      repo.getRestComponents().publish(infoSource, new DataInterpolatedYieldCurveDefinitionSourceResource(masterAndSource));
    }
    return masterAndSource;
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code UserFinancialInterpolatedYieldCurveDefinitionSourceComponentFactory}.
   * @return the meta-bean, not null
   */
  public static UserFinancialInterpolatedYieldCurveDefinitionSourceComponentFactory.Meta meta() {
    return UserFinancialInterpolatedYieldCurveDefinitionSourceComponentFactory.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(UserFinancialInterpolatedYieldCurveDefinitionSourceComponentFactory.Meta.INSTANCE);
  }

  @Override
  public UserFinancialInterpolatedYieldCurveDefinitionSourceComponentFactory.Meta metaBean() {
    return UserFinancialInterpolatedYieldCurveDefinitionSourceComponentFactory.Meta.INSTANCE;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the classifier that the factory should publish under.
   * @return the value of the property, not null
   */
  public String getClassifier() {
    return _classifier;
  }

  /**
   * Sets the classifier that the factory should publish under.
   * @param classifier  the new value of the property, not null
   */
  public void setClassifier(String classifier) {
    JodaBeanUtils.notNull(classifier, "classifier");
    this._classifier = classifier;
  }

  /**
   * Gets the the {@code classifier} property.
   * @return the property, not null
   */
  public final Property<String> classifier() {
    return metaBean().classifier().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the flag determining whether the component should be published by REST (default true).
   * @return the value of the property
   */
  public boolean isPublishRest() {
    return _publishRest;
  }

  /**
   * Sets the flag determining whether the component should be published by REST (default true).
   * @param publishRest  the new value of the property
   */
  public void setPublishRest(boolean publishRest) {
    this._publishRest = publishRest;
  }

  /**
   * Gets the the {@code publishRest} property.
   * @return the property, not null
   */
  public final Property<Boolean> publishRest() {
    return metaBean().publishRest().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the cache manager.
   * @return the value of the property
   */
  public CacheManager getCacheManager() {
    return _cacheManager;
  }

  /**
   * Sets the cache manager.
   * @param cacheManager  the new value of the property
   */
  public void setCacheManager(CacheManager cacheManager) {
    this._cacheManager = cacheManager;
  }

  /**
   * Gets the the {@code cacheManager} property.
   * @return the property, not null
   */
  public final Property<CacheManager> cacheManager() {
    return metaBean().cacheManager().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the classifier that the factory should publish under (underlying source).
   * @return the value of the property
   */
  public String getUnderlyingClassifier() {
    return _underlyingClassifier;
  }

  /**
   * Sets the classifier that the factory should publish under (underlying source).
   * @param underlyingClassifier  the new value of the property
   */
  public void setUnderlyingClassifier(String underlyingClassifier) {
    this._underlyingClassifier = underlyingClassifier;
  }

  /**
   * Gets the the {@code underlyingClassifier} property.
   * @return the property, not null
   */
  public final Property<String> underlyingClassifier() {
    return metaBean().underlyingClassifier().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the config source (underlying source).
   * @return the value of the property, not null
   */
  public ConfigSource getUnderlyingConfigSource() {
    return _underlyingConfigSource;
  }

  /**
   * Sets the config source (underlying source).
   * @param underlyingConfigSource  the new value of the property, not null
   */
  public void setUnderlyingConfigSource(ConfigSource underlyingConfigSource) {
    JodaBeanUtils.notNull(underlyingConfigSource, "underlyingConfigSource");
    this._underlyingConfigSource = underlyingConfigSource;
  }

  /**
   * Gets the the {@code underlyingConfigSource} property.
   * @return the property, not null
   */
  public final Property<ConfigSource> underlyingConfigSource() {
    return metaBean().underlyingConfigSource().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the classifier that the factory should publish under (user source).
   * @return the value of the property
   */
  public String getUserClassifier() {
    return _userClassifier;
  }

  /**
   * Sets the classifier that the factory should publish under (user source).
   * @param userClassifier  the new value of the property
   */
  public void setUserClassifier(String userClassifier) {
    this._userClassifier = userClassifier;
  }

  /**
   * Gets the the {@code userClassifier} property.
   * @return the property, not null
   */
  public final Property<String> userClassifier() {
    return metaBean().userClassifier().createProperty(this);
  }

  //-----------------------------------------------------------------------
  @Override
  public UserFinancialInterpolatedYieldCurveDefinitionSourceComponentFactory clone() {
    return JodaBeanUtils.cloneAlways(this);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      UserFinancialInterpolatedYieldCurveDefinitionSourceComponentFactory other = (UserFinancialInterpolatedYieldCurveDefinitionSourceComponentFactory) obj;
      return JodaBeanUtils.equal(getClassifier(), other.getClassifier()) &&
          (isPublishRest() == other.isPublishRest()) &&
          JodaBeanUtils.equal(getCacheManager(), other.getCacheManager()) &&
          JodaBeanUtils.equal(getUnderlyingClassifier(), other.getUnderlyingClassifier()) &&
          JodaBeanUtils.equal(getUnderlyingConfigSource(), other.getUnderlyingConfigSource()) &&
          JodaBeanUtils.equal(getUserClassifier(), other.getUserClassifier()) &&
          super.equals(obj);
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash += hash * 31 + JodaBeanUtils.hashCode(getClassifier());
    hash += hash * 31 + JodaBeanUtils.hashCode(isPublishRest());
    hash += hash * 31 + JodaBeanUtils.hashCode(getCacheManager());
    hash += hash * 31 + JodaBeanUtils.hashCode(getUnderlyingClassifier());
    hash += hash * 31 + JodaBeanUtils.hashCode(getUnderlyingConfigSource());
    hash += hash * 31 + JodaBeanUtils.hashCode(getUserClassifier());
    return hash ^ super.hashCode();
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(224);
    buf.append("UserFinancialInterpolatedYieldCurveDefinitionSourceComponentFactory{");
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
    buf.append("classifier").append('=').append(JodaBeanUtils.toString(getClassifier())).append(',').append(' ');
    buf.append("publishRest").append('=').append(JodaBeanUtils.toString(isPublishRest())).append(',').append(' ');
    buf.append("cacheManager").append('=').append(JodaBeanUtils.toString(getCacheManager())).append(',').append(' ');
    buf.append("underlyingClassifier").append('=').append(JodaBeanUtils.toString(getUnderlyingClassifier())).append(',').append(' ');
    buf.append("underlyingConfigSource").append('=').append(JodaBeanUtils.toString(getUnderlyingConfigSource())).append(',').append(' ');
    buf.append("userClassifier").append('=').append(JodaBeanUtils.toString(getUserClassifier())).append(',').append(' ');
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code UserFinancialInterpolatedYieldCurveDefinitionSourceComponentFactory}.
   */
  public static class Meta extends AbstractComponentFactory.Meta {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code classifier} property.
     */
    private final MetaProperty<String> _classifier = DirectMetaProperty.ofReadWrite(
        this, "classifier", UserFinancialInterpolatedYieldCurveDefinitionSourceComponentFactory.class, String.class);
    /**
     * The meta-property for the {@code publishRest} property.
     */
    private final MetaProperty<Boolean> _publishRest = DirectMetaProperty.ofReadWrite(
        this, "publishRest", UserFinancialInterpolatedYieldCurveDefinitionSourceComponentFactory.class, Boolean.TYPE);
    /**
     * The meta-property for the {@code cacheManager} property.
     */
    private final MetaProperty<CacheManager> _cacheManager = DirectMetaProperty.ofReadWrite(
        this, "cacheManager", UserFinancialInterpolatedYieldCurveDefinitionSourceComponentFactory.class, CacheManager.class);
    /**
     * The meta-property for the {@code underlyingClassifier} property.
     */
    private final MetaProperty<String> _underlyingClassifier = DirectMetaProperty.ofReadWrite(
        this, "underlyingClassifier", UserFinancialInterpolatedYieldCurveDefinitionSourceComponentFactory.class, String.class);
    /**
     * The meta-property for the {@code underlyingConfigSource} property.
     */
    private final MetaProperty<ConfigSource> _underlyingConfigSource = DirectMetaProperty.ofReadWrite(
        this, "underlyingConfigSource", UserFinancialInterpolatedYieldCurveDefinitionSourceComponentFactory.class, ConfigSource.class);
    /**
     * The meta-property for the {@code userClassifier} property.
     */
    private final MetaProperty<String> _userClassifier = DirectMetaProperty.ofReadWrite(
        this, "userClassifier", UserFinancialInterpolatedYieldCurveDefinitionSourceComponentFactory.class, String.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, (DirectMetaPropertyMap) super.metaPropertyMap(),
        "classifier",
        "publishRest",
        "cacheManager",
        "underlyingClassifier",
        "underlyingConfigSource",
        "userClassifier");

    /**
     * Restricted constructor.
     */
    protected Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case -281470431:  // classifier
          return _classifier;
        case -614707837:  // publishRest
          return _publishRest;
        case -1452875317:  // cacheManager
          return _cacheManager;
        case 1705602398:  // underlyingClassifier
          return _underlyingClassifier;
        case -1488300550:  // underlyingConfigSource
          return _underlyingConfigSource;
        case 473030732:  // userClassifier
          return _userClassifier;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends UserFinancialInterpolatedYieldCurveDefinitionSourceComponentFactory> builder() {
      return new DirectBeanBuilder<UserFinancialInterpolatedYieldCurveDefinitionSourceComponentFactory>(new UserFinancialInterpolatedYieldCurveDefinitionSourceComponentFactory());
    }

    @Override
    public Class<? extends UserFinancialInterpolatedYieldCurveDefinitionSourceComponentFactory> beanType() {
      return UserFinancialInterpolatedYieldCurveDefinitionSourceComponentFactory.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return _metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code classifier} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> classifier() {
      return _classifier;
    }

    /**
     * The meta-property for the {@code publishRest} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Boolean> publishRest() {
      return _publishRest;
    }

    /**
     * The meta-property for the {@code cacheManager} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<CacheManager> cacheManager() {
      return _cacheManager;
    }

    /**
     * The meta-property for the {@code underlyingClassifier} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> underlyingClassifier() {
      return _underlyingClassifier;
    }

    /**
     * The meta-property for the {@code underlyingConfigSource} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<ConfigSource> underlyingConfigSource() {
      return _underlyingConfigSource;
    }

    /**
     * The meta-property for the {@code userClassifier} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> userClassifier() {
      return _userClassifier;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -281470431:  // classifier
          return ((UserFinancialInterpolatedYieldCurveDefinitionSourceComponentFactory) bean).getClassifier();
        case -614707837:  // publishRest
          return ((UserFinancialInterpolatedYieldCurveDefinitionSourceComponentFactory) bean).isPublishRest();
        case -1452875317:  // cacheManager
          return ((UserFinancialInterpolatedYieldCurveDefinitionSourceComponentFactory) bean).getCacheManager();
        case 1705602398:  // underlyingClassifier
          return ((UserFinancialInterpolatedYieldCurveDefinitionSourceComponentFactory) bean).getUnderlyingClassifier();
        case -1488300550:  // underlyingConfigSource
          return ((UserFinancialInterpolatedYieldCurveDefinitionSourceComponentFactory) bean).getUnderlyingConfigSource();
        case 473030732:  // userClassifier
          return ((UserFinancialInterpolatedYieldCurveDefinitionSourceComponentFactory) bean).getUserClassifier();
      }
      return super.propertyGet(bean, propertyName, quiet);
    }

    @Override
    protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -281470431:  // classifier
          ((UserFinancialInterpolatedYieldCurveDefinitionSourceComponentFactory) bean).setClassifier((String) newValue);
          return;
        case -614707837:  // publishRest
          ((UserFinancialInterpolatedYieldCurveDefinitionSourceComponentFactory) bean).setPublishRest((Boolean) newValue);
          return;
        case -1452875317:  // cacheManager
          ((UserFinancialInterpolatedYieldCurveDefinitionSourceComponentFactory) bean).setCacheManager((CacheManager) newValue);
          return;
        case 1705602398:  // underlyingClassifier
          ((UserFinancialInterpolatedYieldCurveDefinitionSourceComponentFactory) bean).setUnderlyingClassifier((String) newValue);
          return;
        case -1488300550:  // underlyingConfigSource
          ((UserFinancialInterpolatedYieldCurveDefinitionSourceComponentFactory) bean).setUnderlyingConfigSource((ConfigSource) newValue);
          return;
        case 473030732:  // userClassifier
          ((UserFinancialInterpolatedYieldCurveDefinitionSourceComponentFactory) bean).setUserClassifier((String) newValue);
          return;
      }
      super.propertySet(bean, propertyName, newValue, quiet);
    }

    @Override
    protected void validate(Bean bean) {
      JodaBeanUtils.notNull(((UserFinancialInterpolatedYieldCurveDefinitionSourceComponentFactory) bean)._classifier, "classifier");
      JodaBeanUtils.notNull(((UserFinancialInterpolatedYieldCurveDefinitionSourceComponentFactory) bean)._underlyingConfigSource, "underlyingConfigSource");
      super.validate(bean);
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
