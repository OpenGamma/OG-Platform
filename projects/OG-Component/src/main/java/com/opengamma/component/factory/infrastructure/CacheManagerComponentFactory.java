/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.component.factory.infrastructure;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanServer;

import net.sf.ehcache.CacheException;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.management.ManagementService;

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
import org.springframework.cache.ehcache.EhCacheManagerFactoryBean;
import org.springframework.context.Lifecycle;

import com.opengamma.component.ComponentRepository;
import com.opengamma.component.factory.AbstractAliasedComponentFactory;
import com.opengamma.util.ResourceUtils;

/**
 * Component Factory for an Ehcache CacheManager using the standard OG Ehcache config found on the classpath.
 * <p>
 * The cache is shared by default, but this can be overridden.
 * The config is plucked from the classpath by default, but can be explicitly specified.
 * <p>
 * The shutdown method is registered for lifecycleStop.
 * <p>
 * The cache manager can be registered with JMX. Because the MBeanServer is expected to run for the life
 * of the VM and because CacheManagers can come and go, there is proper lifecycle handling to clean up
 * instances of ManagementService associated with the CacheManagers to prevent memory leaks.
 * <p>
 * This class is designed to allow protected methods to be overridden.
 */
@BeanDefinition
public class CacheManagerComponentFactory extends AbstractAliasedComponentFactory {

  /**
   * The default configuration location.
   */
  private static final String DEFAULT_EHCACHE_CONFIG = "classpath:common/default-ehcache.xml";

  /**
   * Whether the manager is shared.
   */
  @PropertyDefinition
  private boolean _shared = true;
  /**
   * The location of the configuration.
   */
  @PropertyDefinition(validate = "notNull")
  private String _configLocation = DEFAULT_EHCACHE_CONFIG;

  //-------------------------------------------------------------------------
  @Override
  public void init(ComponentRepository repo, LinkedHashMap<String, String> configuration) throws Exception {
    CacheManager cacheManager = createCacheManager(repo);
    registerComponentAndAliases(repo, CacheManager.class, cacheManager);
    repo.registerLifecycleStop(cacheManager, "shutdown");
    registerMBean(repo, cacheManager);
  }

  /**
   * Creates the cache manager without registering it.
   * 
   * @param repo  the component repository, only used to register secondary items like lifecycle, not null
   * @return the cache manager, not null
   */
  protected CacheManager createCacheManager(ComponentRepository repo) throws IOException {
    EhCacheManagerFactoryBean factoryBean = new EhCacheManagerFactoryBean();
    factoryBean.setShared(isShared());
    factoryBean.setConfigLocation(ResourceUtils.createResource(getConfigLocation()));
    factoryBean.afterPropertiesSet();
    return factoryBean.getObject();
  }

  /**
   * Registers a JMX MBean for the cache manager.
   * <p>
   * Cannot assume MBean server exists at this point, so a lifecycle is used.
   * 
   * @param repo  the component repository, not null
   * @param cacheManager  the cache manager, not null
   */
  protected void registerMBean(ComponentRepository repo, CacheManager cacheManager) {
    repo.registerLifecycle(new CacheManagerLifecycle(repo, cacheManager));
  }

  //-------------------------------------------------------------------------
  /**
   * Lifecycle for cache manager.
   * This delays registering the cache manager with the MBean server until necessary.
   */
  static final class CacheManagerLifecycle implements Lifecycle {
    private volatile ComponentRepository _repo;
    private volatile CacheManager _cacheManager;
    private volatile ManagementService _jmxService;
    CacheManagerLifecycle(ComponentRepository repo, CacheManager cacheManager) {
      _repo = repo;
      _cacheManager = cacheManager;
    }
    @Override
    public void start() {
      MBeanServer mbeanServer = _repo.findInstance(MBeanServer.class);
      if (mbeanServer != null) {
        _jmxService = new ManagementService(_cacheManager, mbeanServer, true, true, true, true);
        try {
          _jmxService.init();
        } catch (CacheException ex) {
          if (ex.getCause() instanceof InstanceAlreadyExistsException == false) {
            throw ex;
          }
        }
      }
    }
    @Override
    public void stop() {
      if (_jmxService != null) {
        _jmxService.dispose();
        _jmxService = null;
      }
    }
    @Override
    public boolean isRunning() {
      return _jmxService != null;
    }
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code CacheManagerComponentFactory}.
   * @return the meta-bean, not null
   */
  public static CacheManagerComponentFactory.Meta meta() {
    return CacheManagerComponentFactory.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(CacheManagerComponentFactory.Meta.INSTANCE);
  }

  @Override
  public CacheManagerComponentFactory.Meta metaBean() {
    return CacheManagerComponentFactory.Meta.INSTANCE;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets whether the manager is shared.
   * @return the value of the property
   */
  public boolean isShared() {
    return _shared;
  }

  /**
   * Sets whether the manager is shared.
   * @param shared  the new value of the property
   */
  public void setShared(boolean shared) {
    this._shared = shared;
  }

  /**
   * Gets the the {@code shared} property.
   * @return the property, not null
   */
  public final Property<Boolean> shared() {
    return metaBean().shared().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the location of the configuration.
   * @return the value of the property, not null
   */
  public String getConfigLocation() {
    return _configLocation;
  }

  /**
   * Sets the location of the configuration.
   * @param configLocation  the new value of the property, not null
   */
  public void setConfigLocation(String configLocation) {
    JodaBeanUtils.notNull(configLocation, "configLocation");
    this._configLocation = configLocation;
  }

  /**
   * Gets the the {@code configLocation} property.
   * @return the property, not null
   */
  public final Property<String> configLocation() {
    return metaBean().configLocation().createProperty(this);
  }

  //-----------------------------------------------------------------------
  @Override
  public CacheManagerComponentFactory clone() {
    return JodaBeanUtils.cloneAlways(this);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      CacheManagerComponentFactory other = (CacheManagerComponentFactory) obj;
      return (isShared() == other.isShared()) &&
          JodaBeanUtils.equal(getConfigLocation(), other.getConfigLocation()) &&
          super.equals(obj);
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = hash * 31 + JodaBeanUtils.hashCode(isShared());
    hash = hash * 31 + JodaBeanUtils.hashCode(getConfigLocation());
    return hash ^ super.hashCode();
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(96);
    buf.append("CacheManagerComponentFactory{");
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
    buf.append("shared").append('=').append(JodaBeanUtils.toString(isShared())).append(',').append(' ');
    buf.append("configLocation").append('=').append(JodaBeanUtils.toString(getConfigLocation())).append(',').append(' ');
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code CacheManagerComponentFactory}.
   */
  public static class Meta extends AbstractAliasedComponentFactory.Meta {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code shared} property.
     */
    private final MetaProperty<Boolean> _shared = DirectMetaProperty.ofReadWrite(
        this, "shared", CacheManagerComponentFactory.class, Boolean.TYPE);
    /**
     * The meta-property for the {@code configLocation} property.
     */
    private final MetaProperty<String> _configLocation = DirectMetaProperty.ofReadWrite(
        this, "configLocation", CacheManagerComponentFactory.class, String.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, (DirectMetaPropertyMap) super.metaPropertyMap(),
        "shared",
        "configLocation");

    /**
     * Restricted constructor.
     */
    protected Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case -903566235:  // shared
          return _shared;
        case -1277483753:  // configLocation
          return _configLocation;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends CacheManagerComponentFactory> builder() {
      return new DirectBeanBuilder<CacheManagerComponentFactory>(new CacheManagerComponentFactory());
    }

    @Override
    public Class<? extends CacheManagerComponentFactory> beanType() {
      return CacheManagerComponentFactory.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return _metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code shared} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Boolean> shared() {
      return _shared;
    }

    /**
     * The meta-property for the {@code configLocation} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> configLocation() {
      return _configLocation;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -903566235:  // shared
          return ((CacheManagerComponentFactory) bean).isShared();
        case -1277483753:  // configLocation
          return ((CacheManagerComponentFactory) bean).getConfigLocation();
      }
      return super.propertyGet(bean, propertyName, quiet);
    }

    @Override
    protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -903566235:  // shared
          ((CacheManagerComponentFactory) bean).setShared((Boolean) newValue);
          return;
        case -1277483753:  // configLocation
          ((CacheManagerComponentFactory) bean).setConfigLocation((String) newValue);
          return;
      }
      super.propertySet(bean, propertyName, newValue, quiet);
    }

    @Override
    protected void validate(Bean bean) {
      JodaBeanUtils.notNull(((CacheManagerComponentFactory) bean)._configLocation, "configLocation");
      super.validate(bean);
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
