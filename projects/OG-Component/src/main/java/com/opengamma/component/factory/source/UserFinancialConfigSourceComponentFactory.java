/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.component.factory.source;

import java.util.HashMap;
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
import com.opengamma.core.config.impl.DataConfigSourceResource;
import com.opengamma.core.config.impl.DelegatingConfigSource;
import com.opengamma.core.config.impl.RemoteConfigSource;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.master.config.impl.EHCachingMasterConfigSource;
import com.opengamma.master.config.impl.MasterConfigSource;

/**
 * Component factory for the config source.
 * <p>
 * This factory creates config sources for the underlying and user masters
 * as well as a combined source.
 */
@BeanDefinition
public class UserFinancialConfigSourceComponentFactory extends AbstractComponentFactory {

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
   * The classifier that the factory should publish under (underlying master).
   */
  @PropertyDefinition
  private String _underlyingClassifier;
  /**
   * The config master (underlying master).
   */
  @PropertyDefinition(validate = "notNull")
  private ConfigMaster _underlyingConfigMaster;
  /**
   * The classifier that the factory should publish under (user master).
   */
  @PropertyDefinition
  private String _userClassifier;
  /**
   * The config master (user master).
   */
  @PropertyDefinition
  private ConfigMaster _userConfigMaster;


  //-------------------------------------------------------------------------
  @Override
  public void init(ComponentRepository repo, LinkedHashMap<String, String> configuration) {
    ConfigSource source = initUnderlying(repo, configuration);

    // add user level if requested
    ConfigSource userSource = initUser(repo, configuration);
    Map<String, ConfigSource> map = new HashMap<String, ConfigSource>();
    if (userSource != null) {
      String scheme = repo.getInfo(getUserConfigMaster()).getAttribute(ComponentInfoAttributes.UNIQUE_ID_SCHEME);
      map.put(scheme, userSource);
      source = new DelegatingConfigSource(source, map);
    }

    // register
    ComponentInfo info = new ComponentInfo(ConfigSource.class, getClassifier());
    info.addAttribute(ComponentInfoAttributes.LEVEL, 2);
    info.addAttribute(ComponentInfoAttributes.REMOTE_CLIENT_JAVA, RemoteConfigSource.class);
    repo.registerComponent(info, source);
    if (isPublishRest()) {
      repo.getRestComponents().publish(info, new DataConfigSourceResource(source));
    }
  }

  protected ConfigSource initUnderlying(ComponentRepository repo, LinkedHashMap<String, String> configuration) {
    ConfigSource source = new MasterConfigSource(getUnderlyingConfigMaster());
    if (getCacheManager() != null) {
      source = new EHCachingMasterConfigSource(getUnderlyingConfigMaster(), getCacheManager());
    }
    if (getUnderlyingClassifier() != null) {
      ComponentInfo info = new ComponentInfo(ConfigSource.class, getUnderlyingClassifier());
      info.addAttribute(ComponentInfoAttributes.LEVEL, 1);
      info.addAttribute(ComponentInfoAttributes.REMOTE_CLIENT_JAVA, RemoteConfigSource.class);
      repo.registerComponent(info, source);
      if (isPublishRest()) {
        repo.getRestComponents().publish(info, new DataConfigSourceResource(source));
      }
    }
    return source;
  }

  protected ConfigSource initUser(ComponentRepository repo, LinkedHashMap<String, String> configuration) {
    if (getUserConfigMaster() == null) {
      return null;
    }
    ConfigSource source = new MasterConfigSource(getUserConfigMaster());
    if (getUserClassifier() != null) {
      ComponentInfo info = new ComponentInfo(ConfigSource.class, getUserClassifier());
      info.addAttribute(ComponentInfoAttributes.LEVEL, 1);
      info.addAttribute(ComponentInfoAttributes.REMOTE_CLIENT_JAVA, RemoteConfigSource.class);
      repo.registerComponent(info, source);
      if (isPublishRest()) {
        repo.getRestComponents().publish(info, new DataConfigSourceResource(source));
      }
    }
    return source;
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code UserFinancialConfigSourceComponentFactory}.
   * @return the meta-bean, not null
   */
  public static UserFinancialConfigSourceComponentFactory.Meta meta() {
    return UserFinancialConfigSourceComponentFactory.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(UserFinancialConfigSourceComponentFactory.Meta.INSTANCE);
  }

  @Override
  public UserFinancialConfigSourceComponentFactory.Meta metaBean() {
    return UserFinancialConfigSourceComponentFactory.Meta.INSTANCE;
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
   * Gets the classifier that the factory should publish under (underlying master).
   * @return the value of the property
   */
  public String getUnderlyingClassifier() {
    return _underlyingClassifier;
  }

  /**
   * Sets the classifier that the factory should publish under (underlying master).
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
   * Gets the config master (underlying master).
   * @return the value of the property, not null
   */
  public ConfigMaster getUnderlyingConfigMaster() {
    return _underlyingConfigMaster;
  }

  /**
   * Sets the config master (underlying master).
   * @param underlyingConfigMaster  the new value of the property, not null
   */
  public void setUnderlyingConfigMaster(ConfigMaster underlyingConfigMaster) {
    JodaBeanUtils.notNull(underlyingConfigMaster, "underlyingConfigMaster");
    this._underlyingConfigMaster = underlyingConfigMaster;
  }

  /**
   * Gets the the {@code underlyingConfigMaster} property.
   * @return the property, not null
   */
  public final Property<ConfigMaster> underlyingConfigMaster() {
    return metaBean().underlyingConfigMaster().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the classifier that the factory should publish under (user master).
   * @return the value of the property
   */
  public String getUserClassifier() {
    return _userClassifier;
  }

  /**
   * Sets the classifier that the factory should publish under (user master).
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
  /**
   * Gets the config master (user master).
   * @return the value of the property
   */
  public ConfigMaster getUserConfigMaster() {
    return _userConfigMaster;
  }

  /**
   * Sets the config master (user master).
   * @param userConfigMaster  the new value of the property
   */
  public void setUserConfigMaster(ConfigMaster userConfigMaster) {
    this._userConfigMaster = userConfigMaster;
  }

  /**
   * Gets the the {@code userConfigMaster} property.
   * @return the property, not null
   */
  public final Property<ConfigMaster> userConfigMaster() {
    return metaBean().userConfigMaster().createProperty(this);
  }

  //-----------------------------------------------------------------------
  @Override
  public UserFinancialConfigSourceComponentFactory clone() {
    return JodaBeanUtils.cloneAlways(this);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      UserFinancialConfigSourceComponentFactory other = (UserFinancialConfigSourceComponentFactory) obj;
      return JodaBeanUtils.equal(getClassifier(), other.getClassifier()) &&
          (isPublishRest() == other.isPublishRest()) &&
          JodaBeanUtils.equal(getCacheManager(), other.getCacheManager()) &&
          JodaBeanUtils.equal(getUnderlyingClassifier(), other.getUnderlyingClassifier()) &&
          JodaBeanUtils.equal(getUnderlyingConfigMaster(), other.getUnderlyingConfigMaster()) &&
          JodaBeanUtils.equal(getUserClassifier(), other.getUserClassifier()) &&
          JodaBeanUtils.equal(getUserConfigMaster(), other.getUserConfigMaster()) &&
          super.equals(obj);
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = hash * 31 + JodaBeanUtils.hashCode(getClassifier());
    hash = hash * 31 + JodaBeanUtils.hashCode(isPublishRest());
    hash = hash * 31 + JodaBeanUtils.hashCode(getCacheManager());
    hash = hash * 31 + JodaBeanUtils.hashCode(getUnderlyingClassifier());
    hash = hash * 31 + JodaBeanUtils.hashCode(getUnderlyingConfigMaster());
    hash = hash * 31 + JodaBeanUtils.hashCode(getUserClassifier());
    hash = hash * 31 + JodaBeanUtils.hashCode(getUserConfigMaster());
    return hash ^ super.hashCode();
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(256);
    buf.append("UserFinancialConfigSourceComponentFactory{");
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
    buf.append("underlyingConfigMaster").append('=').append(JodaBeanUtils.toString(getUnderlyingConfigMaster())).append(',').append(' ');
    buf.append("userClassifier").append('=').append(JodaBeanUtils.toString(getUserClassifier())).append(',').append(' ');
    buf.append("userConfigMaster").append('=').append(JodaBeanUtils.toString(getUserConfigMaster())).append(',').append(' ');
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code UserFinancialConfigSourceComponentFactory}.
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
        this, "classifier", UserFinancialConfigSourceComponentFactory.class, String.class);
    /**
     * The meta-property for the {@code publishRest} property.
     */
    private final MetaProperty<Boolean> _publishRest = DirectMetaProperty.ofReadWrite(
        this, "publishRest", UserFinancialConfigSourceComponentFactory.class, Boolean.TYPE);
    /**
     * The meta-property for the {@code cacheManager} property.
     */
    private final MetaProperty<CacheManager> _cacheManager = DirectMetaProperty.ofReadWrite(
        this, "cacheManager", UserFinancialConfigSourceComponentFactory.class, CacheManager.class);
    /**
     * The meta-property for the {@code underlyingClassifier} property.
     */
    private final MetaProperty<String> _underlyingClassifier = DirectMetaProperty.ofReadWrite(
        this, "underlyingClassifier", UserFinancialConfigSourceComponentFactory.class, String.class);
    /**
     * The meta-property for the {@code underlyingConfigMaster} property.
     */
    private final MetaProperty<ConfigMaster> _underlyingConfigMaster = DirectMetaProperty.ofReadWrite(
        this, "underlyingConfigMaster", UserFinancialConfigSourceComponentFactory.class, ConfigMaster.class);
    /**
     * The meta-property for the {@code userClassifier} property.
     */
    private final MetaProperty<String> _userClassifier = DirectMetaProperty.ofReadWrite(
        this, "userClassifier", UserFinancialConfigSourceComponentFactory.class, String.class);
    /**
     * The meta-property for the {@code userConfigMaster} property.
     */
    private final MetaProperty<ConfigMaster> _userConfigMaster = DirectMetaProperty.ofReadWrite(
        this, "userConfigMaster", UserFinancialConfigSourceComponentFactory.class, ConfigMaster.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, (DirectMetaPropertyMap) super.metaPropertyMap(),
        "classifier",
        "publishRest",
        "cacheManager",
        "underlyingClassifier",
        "underlyingConfigMaster",
        "userClassifier",
        "userConfigMaster");

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
        case -1673062335:  // underlyingConfigMaster
          return _underlyingConfigMaster;
        case 473030732:  // userClassifier
          return _userClassifier;
        case -763459665:  // userConfigMaster
          return _userConfigMaster;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends UserFinancialConfigSourceComponentFactory> builder() {
      return new DirectBeanBuilder<UserFinancialConfigSourceComponentFactory>(new UserFinancialConfigSourceComponentFactory());
    }

    @Override
    public Class<? extends UserFinancialConfigSourceComponentFactory> beanType() {
      return UserFinancialConfigSourceComponentFactory.class;
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
     * The meta-property for the {@code underlyingConfigMaster} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<ConfigMaster> underlyingConfigMaster() {
      return _underlyingConfigMaster;
    }

    /**
     * The meta-property for the {@code userClassifier} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> userClassifier() {
      return _userClassifier;
    }

    /**
     * The meta-property for the {@code userConfigMaster} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<ConfigMaster> userConfigMaster() {
      return _userConfigMaster;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -281470431:  // classifier
          return ((UserFinancialConfigSourceComponentFactory) bean).getClassifier();
        case -614707837:  // publishRest
          return ((UserFinancialConfigSourceComponentFactory) bean).isPublishRest();
        case -1452875317:  // cacheManager
          return ((UserFinancialConfigSourceComponentFactory) bean).getCacheManager();
        case 1705602398:  // underlyingClassifier
          return ((UserFinancialConfigSourceComponentFactory) bean).getUnderlyingClassifier();
        case -1673062335:  // underlyingConfigMaster
          return ((UserFinancialConfigSourceComponentFactory) bean).getUnderlyingConfigMaster();
        case 473030732:  // userClassifier
          return ((UserFinancialConfigSourceComponentFactory) bean).getUserClassifier();
        case -763459665:  // userConfigMaster
          return ((UserFinancialConfigSourceComponentFactory) bean).getUserConfigMaster();
      }
      return super.propertyGet(bean, propertyName, quiet);
    }

    @Override
    protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -281470431:  // classifier
          ((UserFinancialConfigSourceComponentFactory) bean).setClassifier((String) newValue);
          return;
        case -614707837:  // publishRest
          ((UserFinancialConfigSourceComponentFactory) bean).setPublishRest((Boolean) newValue);
          return;
        case -1452875317:  // cacheManager
          ((UserFinancialConfigSourceComponentFactory) bean).setCacheManager((CacheManager) newValue);
          return;
        case 1705602398:  // underlyingClassifier
          ((UserFinancialConfigSourceComponentFactory) bean).setUnderlyingClassifier((String) newValue);
          return;
        case -1673062335:  // underlyingConfigMaster
          ((UserFinancialConfigSourceComponentFactory) bean).setUnderlyingConfigMaster((ConfigMaster) newValue);
          return;
        case 473030732:  // userClassifier
          ((UserFinancialConfigSourceComponentFactory) bean).setUserClassifier((String) newValue);
          return;
        case -763459665:  // userConfigMaster
          ((UserFinancialConfigSourceComponentFactory) bean).setUserConfigMaster((ConfigMaster) newValue);
          return;
      }
      super.propertySet(bean, propertyName, newValue, quiet);
    }

    @Override
    protected void validate(Bean bean) {
      JodaBeanUtils.notNull(((UserFinancialConfigSourceComponentFactory) bean)._classifier, "classifier");
      JodaBeanUtils.notNull(((UserFinancialConfigSourceComponentFactory) bean)._underlyingConfigMaster, "underlyingConfigMaster");
      super.validate(bean);
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
