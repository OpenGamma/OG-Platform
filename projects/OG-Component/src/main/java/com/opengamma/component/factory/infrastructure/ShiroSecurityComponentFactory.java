/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.component.factory.infrastructure;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.shiro.authc.credential.DefaultPasswordService;
import org.apache.shiro.authc.credential.PasswordMatcher;
import org.apache.shiro.authc.credential.PasswordService;
import org.apache.shiro.authz.ModularRealmAuthorizer;
import org.apache.shiro.cache.MemoryConstrainedCacheManager;
import org.apache.shiro.crypto.hash.DefaultHashService;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.util.SimpleByteSource;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
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
import com.opengamma.core.user.UserSource;
import com.opengamma.core.user.impl.UserSourceRealm;
import com.opengamma.master.user.UserMaster;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.auth.AuthUtils;
import com.opengamma.util.auth.PermissiveSecurityManager;

/**
 * Component Factory for an Apache Shiro {@code SecurityManager}.
 * <p>
 * The security manager is used throughout the system.
 * <p>
 * This class is designed to allow protected methods to be overridden.
 */
@BeanDefinition
public class ShiroSecurityComponentFactory extends AbstractComponentFactory {

  /**
   * The classifier that the factory should publish under.
   */
  @PropertyDefinition(validate = "notNull")
  private String _classifier;
  /**
   * The flag enabling security.
   */
  @PropertyDefinition
  private boolean _enabled = true;
  /**
   * The user source.
   */
  @PropertyDefinition
  private UserSource _userSource;
  /**
   * The user master.
   */
  @PropertyDefinition
  private UserMaster _userMaster;
  /**
   * The name of the hash algorithm.
   */
  @PropertyDefinition
  private String _hashAlgorithm = "SHA-512";
  /**
   * The number of hash iterations.
   */
  @PropertyDefinition
  private Integer _hashIterations;
  /**
   * The private salt.
   */
  @PropertyDefinition
  private String _privateSalt;

  //-------------------------------------------------------------------------
  @Override
  public void init(ComponentRepository repo, LinkedHashMap<String, String> configuration) throws Exception {
    if (isEnabled()) {
      if (getUserSource() == null && getUserMaster() == null) {
        ArgumentChecker.notNull(getUserSource(), "userSource");
      }
      ArgumentChecker.notNull(getHashAlgorithm(), "hashAlgorithm");
      ArgumentChecker.notNull(getHashIterations(), "hashIterations");
      ArgumentChecker.notNull(getPrivateSalt(), "privateSalt");
      PasswordService pwService = initPasswordService(repo);
      SecurityManager securityManager = initSecurityManager(repo, pwService);
      AuthUtils.setSecurityManager(securityManager);
    } else {
      SecurityManager securityManager = initPermissiveSecurityManager(repo);
      AuthUtils.setSecurityManager(securityManager);
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Initializes the password service via {@code createPasswordService}.
   * 
   * @param repo the component repository, not null
   * @return the password service, not null
   */
  protected PasswordService initPasswordService(ComponentRepository repo) {
    PasswordService pwService = createPasswordService(repo);
    ComponentInfo info = new ComponentInfo(PasswordService.class, getClassifier());
    repo.registerComponent(info, pwService);
    return pwService;
  }

  /**
   * Creates the password service without registering it.
   * 
   * @param repo the component repository, only used to register secondary items like lifecycle, not null
   * @return the password service, not null
   */
  protected PasswordService createPasswordService(ComponentRepository repo) {
    DefaultHashService hashService = new DefaultHashService();
    hashService.setHashAlgorithmName(getHashAlgorithm());
    hashService.setHashIterations(getHashIterations());
    hashService.setGeneratePublicSalt(true);
    hashService.setPrivateSalt(new SimpleByteSource(getPrivateSalt()));
    DefaultPasswordService pwService = new DefaultPasswordService();
    pwService.setHashService(hashService);
    return pwService;
  }

  //-------------------------------------------------------------------------
  /**
   * Initializes the security manager via {@code createSecurityManager}.
   * 
   * @param repo the component repository, not null
   * @param pwService  the password service, not null
   * @return the security manager, not null
   */
  protected SecurityManager initSecurityManager(ComponentRepository repo, PasswordService pwService) throws IOException {
    SecurityManager securityManager = createSecurityManager(repo, pwService);
    final ComponentInfo info = new ComponentInfo(SecurityManager.class, getClassifier());
    repo.registerComponent(info, securityManager);
    repo.registerLifecycleStop(securityManager, "destroy");
    return securityManager;
  }

  /**
   * Creates the security manager without registering it.
   * 
   * @param repo the component repository, only used to register secondary items like lifecycle, not null
   * @param pwService  the password service, not null
   * @return the security manager, not null
   */
  protected SecurityManager createSecurityManager(ComponentRepository repo, PasswordService pwService) throws IOException {
    // password matcher
    PasswordMatcher pwMatcher = new PasswordMatcher();
    pwMatcher.setPasswordService(pwService);
    // user database realm
    UserSource userSource = getUserSource();
    if (userSource == null) {
      userSource = getUserMaster();
    }
    UserSourceRealm realm = new UserSourceRealm(userSource);
    realm.setAuthenticationCachingEnabled(true);
    realm.setAuthorizationCachingEnabled(true);
    realm.setCredentialsMatcher(pwMatcher);
    // security manager
    DefaultWebSecurityManager sm = new DefaultWebSecurityManager();
    sm.setRealm(realm);
    sm.setCacheManager(new MemoryConstrainedCacheManager());
    // unchecked cast to cause RuntimeException if Apache Shiro changed
    ((ModularRealmAuthorizer) sm.getAuthorizer()).setPermissionResolver(AuthUtils.getPermissionResolver());
    return sm;
  }

  /**
   * Initializes the permissive security manager.
   * 
   * @param repo the component repository, not null
   * @return the security manager, not null
   */
  protected SecurityManager initPermissiveSecurityManager(ComponentRepository repo) {
    DefaultWebSecurityManager securityManager = new PermissiveSecurityManager();
    final ComponentInfo info = new ComponentInfo(SecurityManager.class, getClassifier());
    repo.registerComponent(info, securityManager);
    repo.registerLifecycleStop(securityManager, "destroy");
    return securityManager;
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code ShiroSecurityComponentFactory}.
   * @return the meta-bean, not null
   */
  public static ShiroSecurityComponentFactory.Meta meta() {
    return ShiroSecurityComponentFactory.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(ShiroSecurityComponentFactory.Meta.INSTANCE);
  }

  @Override
  public ShiroSecurityComponentFactory.Meta metaBean() {
    return ShiroSecurityComponentFactory.Meta.INSTANCE;
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
   * Gets the flag enabling security.
   * @return the value of the property
   */
  public boolean isEnabled() {
    return _enabled;
  }

  /**
   * Sets the flag enabling security.
   * @param enabled  the new value of the property
   */
  public void setEnabled(boolean enabled) {
    this._enabled = enabled;
  }

  /**
   * Gets the the {@code enabled} property.
   * @return the property, not null
   */
  public final Property<Boolean> enabled() {
    return metaBean().enabled().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the user source.
   * @return the value of the property
   */
  public UserSource getUserSource() {
    return _userSource;
  }

  /**
   * Sets the user source.
   * @param userSource  the new value of the property
   */
  public void setUserSource(UserSource userSource) {
    this._userSource = userSource;
  }

  /**
   * Gets the the {@code userSource} property.
   * @return the property, not null
   */
  public final Property<UserSource> userSource() {
    return metaBean().userSource().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the user master.
   * @return the value of the property
   */
  public UserMaster getUserMaster() {
    return _userMaster;
  }

  /**
   * Sets the user master.
   * @param userMaster  the new value of the property
   */
  public void setUserMaster(UserMaster userMaster) {
    this._userMaster = userMaster;
  }

  /**
   * Gets the the {@code userMaster} property.
   * @return the property, not null
   */
  public final Property<UserMaster> userMaster() {
    return metaBean().userMaster().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the name of the hash algorithm.
   * @return the value of the property
   */
  public String getHashAlgorithm() {
    return _hashAlgorithm;
  }

  /**
   * Sets the name of the hash algorithm.
   * @param hashAlgorithm  the new value of the property
   */
  public void setHashAlgorithm(String hashAlgorithm) {
    this._hashAlgorithm = hashAlgorithm;
  }

  /**
   * Gets the the {@code hashAlgorithm} property.
   * @return the property, not null
   */
  public final Property<String> hashAlgorithm() {
    return metaBean().hashAlgorithm().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the number of hash iterations.
   * @return the value of the property
   */
  public Integer getHashIterations() {
    return _hashIterations;
  }

  /**
   * Sets the number of hash iterations.
   * @param hashIterations  the new value of the property
   */
  public void setHashIterations(Integer hashIterations) {
    this._hashIterations = hashIterations;
  }

  /**
   * Gets the the {@code hashIterations} property.
   * @return the property, not null
   */
  public final Property<Integer> hashIterations() {
    return metaBean().hashIterations().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the private salt.
   * @return the value of the property
   */
  public String getPrivateSalt() {
    return _privateSalt;
  }

  /**
   * Sets the private salt.
   * @param privateSalt  the new value of the property
   */
  public void setPrivateSalt(String privateSalt) {
    this._privateSalt = privateSalt;
  }

  /**
   * Gets the the {@code privateSalt} property.
   * @return the property, not null
   */
  public final Property<String> privateSalt() {
    return metaBean().privateSalt().createProperty(this);
  }

  //-----------------------------------------------------------------------
  @Override
  public ShiroSecurityComponentFactory clone() {
    return JodaBeanUtils.cloneAlways(this);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      ShiroSecurityComponentFactory other = (ShiroSecurityComponentFactory) obj;
      return JodaBeanUtils.equal(getClassifier(), other.getClassifier()) &&
          (isEnabled() == other.isEnabled()) &&
          JodaBeanUtils.equal(getUserSource(), other.getUserSource()) &&
          JodaBeanUtils.equal(getUserMaster(), other.getUserMaster()) &&
          JodaBeanUtils.equal(getHashAlgorithm(), other.getHashAlgorithm()) &&
          JodaBeanUtils.equal(getHashIterations(), other.getHashIterations()) &&
          JodaBeanUtils.equal(getPrivateSalt(), other.getPrivateSalt()) &&
          super.equals(obj);
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash += hash * 31 + JodaBeanUtils.hashCode(getClassifier());
    hash += hash * 31 + JodaBeanUtils.hashCode(isEnabled());
    hash += hash * 31 + JodaBeanUtils.hashCode(getUserSource());
    hash += hash * 31 + JodaBeanUtils.hashCode(getUserMaster());
    hash += hash * 31 + JodaBeanUtils.hashCode(getHashAlgorithm());
    hash += hash * 31 + JodaBeanUtils.hashCode(getHashIterations());
    hash += hash * 31 + JodaBeanUtils.hashCode(getPrivateSalt());
    return hash ^ super.hashCode();
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(256);
    buf.append("ShiroSecurityComponentFactory{");
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
    buf.append("enabled").append('=').append(JodaBeanUtils.toString(isEnabled())).append(',').append(' ');
    buf.append("userSource").append('=').append(JodaBeanUtils.toString(getUserSource())).append(',').append(' ');
    buf.append("userMaster").append('=').append(JodaBeanUtils.toString(getUserMaster())).append(',').append(' ');
    buf.append("hashAlgorithm").append('=').append(JodaBeanUtils.toString(getHashAlgorithm())).append(',').append(' ');
    buf.append("hashIterations").append('=').append(JodaBeanUtils.toString(getHashIterations())).append(',').append(' ');
    buf.append("privateSalt").append('=').append(JodaBeanUtils.toString(getPrivateSalt())).append(',').append(' ');
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code ShiroSecurityComponentFactory}.
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
        this, "classifier", ShiroSecurityComponentFactory.class, String.class);
    /**
     * The meta-property for the {@code enabled} property.
     */
    private final MetaProperty<Boolean> _enabled = DirectMetaProperty.ofReadWrite(
        this, "enabled", ShiroSecurityComponentFactory.class, Boolean.TYPE);
    /**
     * The meta-property for the {@code userSource} property.
     */
    private final MetaProperty<UserSource> _userSource = DirectMetaProperty.ofReadWrite(
        this, "userSource", ShiroSecurityComponentFactory.class, UserSource.class);
    /**
     * The meta-property for the {@code userMaster} property.
     */
    private final MetaProperty<UserMaster> _userMaster = DirectMetaProperty.ofReadWrite(
        this, "userMaster", ShiroSecurityComponentFactory.class, UserMaster.class);
    /**
     * The meta-property for the {@code hashAlgorithm} property.
     */
    private final MetaProperty<String> _hashAlgorithm = DirectMetaProperty.ofReadWrite(
        this, "hashAlgorithm", ShiroSecurityComponentFactory.class, String.class);
    /**
     * The meta-property for the {@code hashIterations} property.
     */
    private final MetaProperty<Integer> _hashIterations = DirectMetaProperty.ofReadWrite(
        this, "hashIterations", ShiroSecurityComponentFactory.class, Integer.class);
    /**
     * The meta-property for the {@code privateSalt} property.
     */
    private final MetaProperty<String> _privateSalt = DirectMetaProperty.ofReadWrite(
        this, "privateSalt", ShiroSecurityComponentFactory.class, String.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, (DirectMetaPropertyMap) super.metaPropertyMap(),
        "classifier",
        "enabled",
        "userSource",
        "userMaster",
        "hashAlgorithm",
        "hashIterations",
        "privateSalt");

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
        case -1609594047:  // enabled
          return _enabled;
        case 1587608518:  // userSource
          return _userSource;
        case 1402846733:  // userMaster
          return _userMaster;
        case 1962894081:  // hashAlgorithm
          return _hashAlgorithm;
        case 568332516:  // hashIterations
          return _hashIterations;
        case 1971576953:  // privateSalt
          return _privateSalt;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends ShiroSecurityComponentFactory> builder() {
      return new DirectBeanBuilder<ShiroSecurityComponentFactory>(new ShiroSecurityComponentFactory());
    }

    @Override
    public Class<? extends ShiroSecurityComponentFactory> beanType() {
      return ShiroSecurityComponentFactory.class;
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
     * The meta-property for the {@code enabled} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Boolean> enabled() {
      return _enabled;
    }

    /**
     * The meta-property for the {@code userSource} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<UserSource> userSource() {
      return _userSource;
    }

    /**
     * The meta-property for the {@code userMaster} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<UserMaster> userMaster() {
      return _userMaster;
    }

    /**
     * The meta-property for the {@code hashAlgorithm} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> hashAlgorithm() {
      return _hashAlgorithm;
    }

    /**
     * The meta-property for the {@code hashIterations} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Integer> hashIterations() {
      return _hashIterations;
    }

    /**
     * The meta-property for the {@code privateSalt} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> privateSalt() {
      return _privateSalt;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -281470431:  // classifier
          return ((ShiroSecurityComponentFactory) bean).getClassifier();
        case -1609594047:  // enabled
          return ((ShiroSecurityComponentFactory) bean).isEnabled();
        case 1587608518:  // userSource
          return ((ShiroSecurityComponentFactory) bean).getUserSource();
        case 1402846733:  // userMaster
          return ((ShiroSecurityComponentFactory) bean).getUserMaster();
        case 1962894081:  // hashAlgorithm
          return ((ShiroSecurityComponentFactory) bean).getHashAlgorithm();
        case 568332516:  // hashIterations
          return ((ShiroSecurityComponentFactory) bean).getHashIterations();
        case 1971576953:  // privateSalt
          return ((ShiroSecurityComponentFactory) bean).getPrivateSalt();
      }
      return super.propertyGet(bean, propertyName, quiet);
    }

    @Override
    protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -281470431:  // classifier
          ((ShiroSecurityComponentFactory) bean).setClassifier((String) newValue);
          return;
        case -1609594047:  // enabled
          ((ShiroSecurityComponentFactory) bean).setEnabled((Boolean) newValue);
          return;
        case 1587608518:  // userSource
          ((ShiroSecurityComponentFactory) bean).setUserSource((UserSource) newValue);
          return;
        case 1402846733:  // userMaster
          ((ShiroSecurityComponentFactory) bean).setUserMaster((UserMaster) newValue);
          return;
        case 1962894081:  // hashAlgorithm
          ((ShiroSecurityComponentFactory) bean).setHashAlgorithm((String) newValue);
          return;
        case 568332516:  // hashIterations
          ((ShiroSecurityComponentFactory) bean).setHashIterations((Integer) newValue);
          return;
        case 1971576953:  // privateSalt
          ((ShiroSecurityComponentFactory) bean).setPrivateSalt((String) newValue);
          return;
      }
      super.propertySet(bean, propertyName, newValue, quiet);
    }

    @Override
    protected void validate(Bean bean) {
      JodaBeanUtils.notNull(((ShiroSecurityComponentFactory) bean)._classifier, "classifier");
      super.validate(bean);
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
