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
import com.opengamma.core.security.SecuritySource;
import com.opengamma.financial.security.DataFinancialSecuritySourceResource;
import com.opengamma.financial.security.DelegatingFinancialSecuritySource;
import com.opengamma.financial.security.EHCachingFinancialSecuritySource;
import com.opengamma.financial.security.FinancialSecuritySource;
import com.opengamma.financial.security.MasterFinancialSecuritySource;
import com.opengamma.financial.security.RemoteFinancialSecuritySource;
import com.opengamma.master.security.SecurityMaster;

/**
 * Component factory for the security source.
 * <p>
 * This factory creates security sources for the underlying and user masters as well as a combined source.
 */
@BeanDefinition
public class UserFinancialSecuritySourceComponentFactory extends AbstractComponentFactory {

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
   * The security master (underlying master).
   */
  @PropertyDefinition(validate = "notNull")
  private SecurityMaster _underlyingSecurityMaster;
  /**
   * The classifier that the factory should publish under (user master).
   */
  @PropertyDefinition
  private String _userClassifier;
  /**
   * The security master (user master).
   */
  @PropertyDefinition
  private SecurityMaster _userSecurityMaster;

  //-------------------------------------------------------------------------
  @Override
  public void init(ComponentRepository repo, LinkedHashMap<String, String> configuration) {
    FinancialSecuritySource source = initUnderlying(repo, configuration);
    // add user level if requested
    FinancialSecuritySource userSource = initUser(repo, configuration);
    Map<String, FinancialSecuritySource> map = new HashMap<String, FinancialSecuritySource>();
    if (userSource != null) {
      String scheme = repo.getInfo(getUserSecurityMaster()).getAttribute(ComponentInfoAttributes.UNIQUE_ID_SCHEME);
      map.put(scheme, userSource);
      source = new DelegatingFinancialSecuritySource(source, map);
    }
    // wrap it in a cache
    if (getCacheManager() != null) {
      source = new EHCachingFinancialSecuritySource(source, getCacheManager());
    }
    // register
    ComponentInfo info = new ComponentInfo(SecuritySource.class, getClassifier());
    info.addAttribute(ComponentInfoAttributes.LEVEL, 2);
    info.addAttribute(ComponentInfoAttributes.REMOTE_CLIENT_JAVA, RemoteFinancialSecuritySource.class);
    repo.registerComponent(info, source);
    if (isPublishRest()) {
      repo.getRestComponents().publish(info, new DataFinancialSecuritySourceResource(source));
    }
  }

  protected FinancialSecuritySource initUnderlying(ComponentRepository repo, LinkedHashMap<String, String> configuration) {
    FinancialSecuritySource source = new MasterFinancialSecuritySource(getUnderlyingSecurityMaster());

    // REVIEW kirk 2013-04-19 -- The block below should only be enabled when developing
    // the RedisCachingFinancialSecuritySource.
    /*JedisPool jedisPool = new JedisPool("localhost");
    source = new RedisCachingFinancialSecuritySource(source, jedisPool, "", OpenGammaFudgeContext.getInstance());*/

    if (getUnderlyingClassifier() != null) {
      // [PLAT-4986] Note: the composite source is wrapped in a cache, but the underlying source isn't and will not be very efficient to use
      ComponentInfo info = new ComponentInfo(SecuritySource.class, getUnderlyingClassifier());
      info.addAttribute(ComponentInfoAttributes.LEVEL, 1);
      info.addAttribute(ComponentInfoAttributes.REMOTE_CLIENT_JAVA, RemoteFinancialSecuritySource.class);
      repo.registerComponent(info, source);
      if (isPublishRest()) {
        repo.getRestComponents().publish(info, new DataFinancialSecuritySourceResource(source));
      }
    }
    return source;
  }

  protected FinancialSecuritySource initUser(ComponentRepository repo, LinkedHashMap<String, String> configuration) {
    if (getUserSecurityMaster() == null) {
      return null;
    }
    FinancialSecuritySource source = new MasterFinancialSecuritySource(getUserSecurityMaster());
    if (getUserClassifier() != null) {
      // [PLAT-4986] Note: the composite source is wrapped in a cache, but the user source isn't and will not be very efficient to use
      ComponentInfo info = new ComponentInfo(SecuritySource.class, getUserClassifier());
      info.addAttribute(ComponentInfoAttributes.LEVEL, 1);
      info.addAttribute(ComponentInfoAttributes.REMOTE_CLIENT_JAVA, RemoteFinancialSecuritySource.class);
      repo.registerComponent(info, source);
      if (isPublishRest()) {
        repo.getRestComponents().publish(info, new DataFinancialSecuritySourceResource(source));
      }
    }
    return source;
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code UserFinancialSecuritySourceComponentFactory}.
   * @return the meta-bean, not null
   */
  public static UserFinancialSecuritySourceComponentFactory.Meta meta() {
    return UserFinancialSecuritySourceComponentFactory.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(UserFinancialSecuritySourceComponentFactory.Meta.INSTANCE);
  }

  @Override
  public UserFinancialSecuritySourceComponentFactory.Meta metaBean() {
    return UserFinancialSecuritySourceComponentFactory.Meta.INSTANCE;
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
   * Gets the security master (underlying master).
   * @return the value of the property, not null
   */
  public SecurityMaster getUnderlyingSecurityMaster() {
    return _underlyingSecurityMaster;
  }

  /**
   * Sets the security master (underlying master).
   * @param underlyingSecurityMaster  the new value of the property, not null
   */
  public void setUnderlyingSecurityMaster(SecurityMaster underlyingSecurityMaster) {
    JodaBeanUtils.notNull(underlyingSecurityMaster, "underlyingSecurityMaster");
    this._underlyingSecurityMaster = underlyingSecurityMaster;
  }

  /**
   * Gets the the {@code underlyingSecurityMaster} property.
   * @return the property, not null
   */
  public final Property<SecurityMaster> underlyingSecurityMaster() {
    return metaBean().underlyingSecurityMaster().createProperty(this);
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
   * Gets the security master (user master).
   * @return the value of the property
   */
  public SecurityMaster getUserSecurityMaster() {
    return _userSecurityMaster;
  }

  /**
   * Sets the security master (user master).
   * @param userSecurityMaster  the new value of the property
   */
  public void setUserSecurityMaster(SecurityMaster userSecurityMaster) {
    this._userSecurityMaster = userSecurityMaster;
  }

  /**
   * Gets the the {@code userSecurityMaster} property.
   * @return the property, not null
   */
  public final Property<SecurityMaster> userSecurityMaster() {
    return metaBean().userSecurityMaster().createProperty(this);
  }

  //-----------------------------------------------------------------------
  @Override
  public UserFinancialSecuritySourceComponentFactory clone() {
    return JodaBeanUtils.cloneAlways(this);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      UserFinancialSecuritySourceComponentFactory other = (UserFinancialSecuritySourceComponentFactory) obj;
      return JodaBeanUtils.equal(getClassifier(), other.getClassifier()) &&
          (isPublishRest() == other.isPublishRest()) &&
          JodaBeanUtils.equal(getCacheManager(), other.getCacheManager()) &&
          JodaBeanUtils.equal(getUnderlyingClassifier(), other.getUnderlyingClassifier()) &&
          JodaBeanUtils.equal(getUnderlyingSecurityMaster(), other.getUnderlyingSecurityMaster()) &&
          JodaBeanUtils.equal(getUserClassifier(), other.getUserClassifier()) &&
          JodaBeanUtils.equal(getUserSecurityMaster(), other.getUserSecurityMaster()) &&
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
    hash = hash * 31 + JodaBeanUtils.hashCode(getUnderlyingSecurityMaster());
    hash = hash * 31 + JodaBeanUtils.hashCode(getUserClassifier());
    hash = hash * 31 + JodaBeanUtils.hashCode(getUserSecurityMaster());
    return hash ^ super.hashCode();
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(256);
    buf.append("UserFinancialSecuritySourceComponentFactory{");
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
    buf.append("underlyingSecurityMaster").append('=').append(JodaBeanUtils.toString(getUnderlyingSecurityMaster())).append(',').append(' ');
    buf.append("userClassifier").append('=').append(JodaBeanUtils.toString(getUserClassifier())).append(',').append(' ');
    buf.append("userSecurityMaster").append('=').append(JodaBeanUtils.toString(getUserSecurityMaster())).append(',').append(' ');
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code UserFinancialSecuritySourceComponentFactory}.
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
        this, "classifier", UserFinancialSecuritySourceComponentFactory.class, String.class);
    /**
     * The meta-property for the {@code publishRest} property.
     */
    private final MetaProperty<Boolean> _publishRest = DirectMetaProperty.ofReadWrite(
        this, "publishRest", UserFinancialSecuritySourceComponentFactory.class, Boolean.TYPE);
    /**
     * The meta-property for the {@code cacheManager} property.
     */
    private final MetaProperty<CacheManager> _cacheManager = DirectMetaProperty.ofReadWrite(
        this, "cacheManager", UserFinancialSecuritySourceComponentFactory.class, CacheManager.class);
    /**
     * The meta-property for the {@code underlyingClassifier} property.
     */
    private final MetaProperty<String> _underlyingClassifier = DirectMetaProperty.ofReadWrite(
        this, "underlyingClassifier", UserFinancialSecuritySourceComponentFactory.class, String.class);
    /**
     * The meta-property for the {@code underlyingSecurityMaster} property.
     */
    private final MetaProperty<SecurityMaster> _underlyingSecurityMaster = DirectMetaProperty.ofReadWrite(
        this, "underlyingSecurityMaster", UserFinancialSecuritySourceComponentFactory.class, SecurityMaster.class);
    /**
     * The meta-property for the {@code userClassifier} property.
     */
    private final MetaProperty<String> _userClassifier = DirectMetaProperty.ofReadWrite(
        this, "userClassifier", UserFinancialSecuritySourceComponentFactory.class, String.class);
    /**
     * The meta-property for the {@code userSecurityMaster} property.
     */
    private final MetaProperty<SecurityMaster> _userSecurityMaster = DirectMetaProperty.ofReadWrite(
        this, "userSecurityMaster", UserFinancialSecuritySourceComponentFactory.class, SecurityMaster.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, (DirectMetaPropertyMap) super.metaPropertyMap(),
        "classifier",
        "publishRest",
        "cacheManager",
        "underlyingClassifier",
        "underlyingSecurityMaster",
        "userClassifier",
        "userSecurityMaster");

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
        case 512264831:  // underlyingSecurityMaster
          return _underlyingSecurityMaster;
        case 473030732:  // userClassifier
          return _userClassifier;
        case -1532897683:  // userSecurityMaster
          return _userSecurityMaster;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends UserFinancialSecuritySourceComponentFactory> builder() {
      return new DirectBeanBuilder<UserFinancialSecuritySourceComponentFactory>(new UserFinancialSecuritySourceComponentFactory());
    }

    @Override
    public Class<? extends UserFinancialSecuritySourceComponentFactory> beanType() {
      return UserFinancialSecuritySourceComponentFactory.class;
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
     * The meta-property for the {@code underlyingSecurityMaster} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<SecurityMaster> underlyingSecurityMaster() {
      return _underlyingSecurityMaster;
    }

    /**
     * The meta-property for the {@code userClassifier} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> userClassifier() {
      return _userClassifier;
    }

    /**
     * The meta-property for the {@code userSecurityMaster} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<SecurityMaster> userSecurityMaster() {
      return _userSecurityMaster;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -281470431:  // classifier
          return ((UserFinancialSecuritySourceComponentFactory) bean).getClassifier();
        case -614707837:  // publishRest
          return ((UserFinancialSecuritySourceComponentFactory) bean).isPublishRest();
        case -1452875317:  // cacheManager
          return ((UserFinancialSecuritySourceComponentFactory) bean).getCacheManager();
        case 1705602398:  // underlyingClassifier
          return ((UserFinancialSecuritySourceComponentFactory) bean).getUnderlyingClassifier();
        case 512264831:  // underlyingSecurityMaster
          return ((UserFinancialSecuritySourceComponentFactory) bean).getUnderlyingSecurityMaster();
        case 473030732:  // userClassifier
          return ((UserFinancialSecuritySourceComponentFactory) bean).getUserClassifier();
        case -1532897683:  // userSecurityMaster
          return ((UserFinancialSecuritySourceComponentFactory) bean).getUserSecurityMaster();
      }
      return super.propertyGet(bean, propertyName, quiet);
    }

    @Override
    protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -281470431:  // classifier
          ((UserFinancialSecuritySourceComponentFactory) bean).setClassifier((String) newValue);
          return;
        case -614707837:  // publishRest
          ((UserFinancialSecuritySourceComponentFactory) bean).setPublishRest((Boolean) newValue);
          return;
        case -1452875317:  // cacheManager
          ((UserFinancialSecuritySourceComponentFactory) bean).setCacheManager((CacheManager) newValue);
          return;
        case 1705602398:  // underlyingClassifier
          ((UserFinancialSecuritySourceComponentFactory) bean).setUnderlyingClassifier((String) newValue);
          return;
        case 512264831:  // underlyingSecurityMaster
          ((UserFinancialSecuritySourceComponentFactory) bean).setUnderlyingSecurityMaster((SecurityMaster) newValue);
          return;
        case 473030732:  // userClassifier
          ((UserFinancialSecuritySourceComponentFactory) bean).setUserClassifier((String) newValue);
          return;
        case -1532897683:  // userSecurityMaster
          ((UserFinancialSecuritySourceComponentFactory) bean).setUserSecurityMaster((SecurityMaster) newValue);
          return;
      }
      super.propertySet(bean, propertyName, newValue, quiet);
    }

    @Override
    protected void validate(Bean bean) {
      JodaBeanUtils.notNull(((UserFinancialSecuritySourceComponentFactory) bean)._classifier, "classifier");
      JodaBeanUtils.notNull(((UserFinancialSecuritySourceComponentFactory) bean)._underlyingSecurityMaster, "underlyingSecurityMaster");
      super.validate(bean);
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
