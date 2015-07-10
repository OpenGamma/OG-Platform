/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.component.factory.web;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.ServletContext;

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
import org.springframework.web.context.ServletContextAware;

import com.opengamma.component.ComponentRepository;
import com.opengamma.component.factory.AbstractComponentFactory;
import com.opengamma.component.rest.JerseyRestResourceFactory;
import com.opengamma.web.bundle.BundleCompressor;
import com.opengamma.web.bundle.BundleManagerFactory;
import com.opengamma.web.bundle.DeployMode;
import com.opengamma.web.bundle.EHCachingBundleCompressor;
import com.opengamma.web.bundle.WebBundlesResource;
import com.opengamma.web.bundle.YUIBundleCompressor;
import com.opengamma.web.bundle.YUICompressorOptions;

/**
 * Component factory for the main website.
 */
@BeanDefinition
public class WebsiteBundleComponentFactory extends AbstractComponentFactory {

  /**
   * The bundle configuration file.
   */
  @PropertyDefinition
  private String _configXmlPath;
  /**
   * The base directory for the files to be served.
   */
  @PropertyDefinition(validate = "notNull")
  private String _baseDir;
  /**
   * The deployment mode.
   */
  @PropertyDefinition(validate = "notNull")
  private DeployMode _deployMode;
  /**
   * The cache for the bundles.
   */
  @PropertyDefinition
  private CacheManager _cacheManager;
  /**
   * The configuration of the compressor (default -1).
   */
  @PropertyDefinition
  private int _compressorLineBreakPosition = -1;
  /**
   * The configuration of the compressor (default true).
   */
  @PropertyDefinition
  private boolean _compressorMunge = true;
  /**
   * The configuration of the compressor (default true).
   */
  @PropertyDefinition
  private boolean _compressorPreserveAllSemiColons = true;
  /**
   * The configuration of the compressor (default true).
   */
  @PropertyDefinition
  private boolean _compressorOptimize = true;
  /**
   * The configuration of the compressor (default false).
   */
  @PropertyDefinition
  private boolean _compressorWarn;

  //-------------------------------------------------------------------------
  @Override
  public void init(ComponentRepository repo, LinkedHashMap<String, String> configuration) {
    final WebResourceBundleInitializer webResourceInitializer = new WebResourceBundleInitializer(buildCompressorOptions(), 
        buildBundleManager(), getCacheManager(), getDeployMode(), repo);
    repo.registerServletContextAware(webResourceInitializer);
  }
  
  //-------------------------------------------------------------------------
  static final class WebResourceBundleInitializer implements ServletContextAware {
    private YUICompressorOptions _compressorOptions;
    private BundleManagerFactory _bundleManagerFactory;
    private CacheManager _cacheManager;
    private DeployMode _deployMode;
    private ComponentRepository _repo;
    
    public WebResourceBundleInitializer(YUICompressorOptions compressorOptions, BundleManagerFactory bundleManagerFactory, 
        CacheManager cacheManager, DeployMode deployMode, ComponentRepository repo) {
      _compressorOptions = compressorOptions;
      _bundleManagerFactory = bundleManagerFactory;
      _cacheManager = cacheManager;
      _deployMode = deployMode;
      _repo = repo;
    }
    
    @Override
    public void setServletContext(ServletContext servletContext) {
      BundleCompressor compressor = new YUIBundleCompressor(_compressorOptions);
      if (_cacheManager != null) {
        compressor = new EHCachingBundleCompressor(compressor, _cacheManager);
      } else {
        if (_deployMode == DeployMode.PROD) {
          throw new IllegalArgumentException("CacheManager required for production deployment");
        }
      }
      
      JerseyRestResourceFactory resource = new JerseyRestResourceFactory(WebBundlesResource.class, _bundleManagerFactory, compressor, _deployMode);
      _repo.getRestComponents().publishResource(resource);
    }
 
  }

  protected BundleManagerFactory buildBundleManager() {
    BundleManagerFactory managerFactory = new BundleManagerFactory();
    managerFactory.setBaseDir(getBaseDir());
    managerFactory.setConfigXmlPath(getConfigXmlPath());
    return managerFactory;
  }

  protected YUICompressorOptions buildCompressorOptions() {
    YUICompressorOptions compressorOptions = new YUICompressorOptions();
    compressorOptions.setLineBreakPosition(getCompressorLineBreakPosition());
    compressorOptions.setMunge(isCompressorMunge());
    compressorOptions.setPreserveAllSemiColons(isCompressorPreserveAllSemiColons());
    compressorOptions.setOptimize(isCompressorOptimize());
    compressorOptions.setWarn(isCompressorWarn());
    return compressorOptions;
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code WebsiteBundleComponentFactory}.
   * @return the meta-bean, not null
   */
  public static WebsiteBundleComponentFactory.Meta meta() {
    return WebsiteBundleComponentFactory.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(WebsiteBundleComponentFactory.Meta.INSTANCE);
  }

  @Override
  public WebsiteBundleComponentFactory.Meta metaBean() {
    return WebsiteBundleComponentFactory.Meta.INSTANCE;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the bundle configuration file.
   * @return the value of the property
   */
  public String getConfigXmlPath() {
    return _configXmlPath;
  }

  /**
   * Sets the bundle configuration file.
   * @param configXmlPath  the new value of the property
   */
  public void setConfigXmlPath(String configXmlPath) {
    this._configXmlPath = configXmlPath;
  }

  /**
   * Gets the the {@code configXmlPath} property.
   * @return the property, not null
   */
  public final Property<String> configXmlPath() {
    return metaBean().configXmlPath().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the base directory for the files to be served.
   * @return the value of the property, not null
   */
  public String getBaseDir() {
    return _baseDir;
  }

  /**
   * Sets the base directory for the files to be served.
   * @param baseDir  the new value of the property, not null
   */
  public void setBaseDir(String baseDir) {
    JodaBeanUtils.notNull(baseDir, "baseDir");
    this._baseDir = baseDir;
  }

  /**
   * Gets the the {@code baseDir} property.
   * @return the property, not null
   */
  public final Property<String> baseDir() {
    return metaBean().baseDir().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the deployment mode.
   * @return the value of the property, not null
   */
  public DeployMode getDeployMode() {
    return _deployMode;
  }

  /**
   * Sets the deployment mode.
   * @param deployMode  the new value of the property, not null
   */
  public void setDeployMode(DeployMode deployMode) {
    JodaBeanUtils.notNull(deployMode, "deployMode");
    this._deployMode = deployMode;
  }

  /**
   * Gets the the {@code deployMode} property.
   * @return the property, not null
   */
  public final Property<DeployMode> deployMode() {
    return metaBean().deployMode().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the cache for the bundles.
   * @return the value of the property
   */
  public CacheManager getCacheManager() {
    return _cacheManager;
  }

  /**
   * Sets the cache for the bundles.
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
   * Gets the configuration of the compressor (default -1).
   * @return the value of the property
   */
  public int getCompressorLineBreakPosition() {
    return _compressorLineBreakPosition;
  }

  /**
   * Sets the configuration of the compressor (default -1).
   * @param compressorLineBreakPosition  the new value of the property
   */
  public void setCompressorLineBreakPosition(int compressorLineBreakPosition) {
    this._compressorLineBreakPosition = compressorLineBreakPosition;
  }

  /**
   * Gets the the {@code compressorLineBreakPosition} property.
   * @return the property, not null
   */
  public final Property<Integer> compressorLineBreakPosition() {
    return metaBean().compressorLineBreakPosition().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the configuration of the compressor (default true).
   * @return the value of the property
   */
  public boolean isCompressorMunge() {
    return _compressorMunge;
  }

  /**
   * Sets the configuration of the compressor (default true).
   * @param compressorMunge  the new value of the property
   */
  public void setCompressorMunge(boolean compressorMunge) {
    this._compressorMunge = compressorMunge;
  }

  /**
   * Gets the the {@code compressorMunge} property.
   * @return the property, not null
   */
  public final Property<Boolean> compressorMunge() {
    return metaBean().compressorMunge().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the configuration of the compressor (default true).
   * @return the value of the property
   */
  public boolean isCompressorPreserveAllSemiColons() {
    return _compressorPreserveAllSemiColons;
  }

  /**
   * Sets the configuration of the compressor (default true).
   * @param compressorPreserveAllSemiColons  the new value of the property
   */
  public void setCompressorPreserveAllSemiColons(boolean compressorPreserveAllSemiColons) {
    this._compressorPreserveAllSemiColons = compressorPreserveAllSemiColons;
  }

  /**
   * Gets the the {@code compressorPreserveAllSemiColons} property.
   * @return the property, not null
   */
  public final Property<Boolean> compressorPreserveAllSemiColons() {
    return metaBean().compressorPreserveAllSemiColons().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the configuration of the compressor (default true).
   * @return the value of the property
   */
  public boolean isCompressorOptimize() {
    return _compressorOptimize;
  }

  /**
   * Sets the configuration of the compressor (default true).
   * @param compressorOptimize  the new value of the property
   */
  public void setCompressorOptimize(boolean compressorOptimize) {
    this._compressorOptimize = compressorOptimize;
  }

  /**
   * Gets the the {@code compressorOptimize} property.
   * @return the property, not null
   */
  public final Property<Boolean> compressorOptimize() {
    return metaBean().compressorOptimize().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the configuration of the compressor (default false).
   * @return the value of the property
   */
  public boolean isCompressorWarn() {
    return _compressorWarn;
  }

  /**
   * Sets the configuration of the compressor (default false).
   * @param compressorWarn  the new value of the property
   */
  public void setCompressorWarn(boolean compressorWarn) {
    this._compressorWarn = compressorWarn;
  }

  /**
   * Gets the the {@code compressorWarn} property.
   * @return the property, not null
   */
  public final Property<Boolean> compressorWarn() {
    return metaBean().compressorWarn().createProperty(this);
  }

  //-----------------------------------------------------------------------
  @Override
  public WebsiteBundleComponentFactory clone() {
    return JodaBeanUtils.cloneAlways(this);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      WebsiteBundleComponentFactory other = (WebsiteBundleComponentFactory) obj;
      return JodaBeanUtils.equal(getConfigXmlPath(), other.getConfigXmlPath()) &&
          JodaBeanUtils.equal(getBaseDir(), other.getBaseDir()) &&
          JodaBeanUtils.equal(getDeployMode(), other.getDeployMode()) &&
          JodaBeanUtils.equal(getCacheManager(), other.getCacheManager()) &&
          (getCompressorLineBreakPosition() == other.getCompressorLineBreakPosition()) &&
          (isCompressorMunge() == other.isCompressorMunge()) &&
          (isCompressorPreserveAllSemiColons() == other.isCompressorPreserveAllSemiColons()) &&
          (isCompressorOptimize() == other.isCompressorOptimize()) &&
          (isCompressorWarn() == other.isCompressorWarn()) &&
          super.equals(obj);
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = hash * 31 + JodaBeanUtils.hashCode(getConfigXmlPath());
    hash = hash * 31 + JodaBeanUtils.hashCode(getBaseDir());
    hash = hash * 31 + JodaBeanUtils.hashCode(getDeployMode());
    hash = hash * 31 + JodaBeanUtils.hashCode(getCacheManager());
    hash = hash * 31 + JodaBeanUtils.hashCode(getCompressorLineBreakPosition());
    hash = hash * 31 + JodaBeanUtils.hashCode(isCompressorMunge());
    hash = hash * 31 + JodaBeanUtils.hashCode(isCompressorPreserveAllSemiColons());
    hash = hash * 31 + JodaBeanUtils.hashCode(isCompressorOptimize());
    hash = hash * 31 + JodaBeanUtils.hashCode(isCompressorWarn());
    return hash ^ super.hashCode();
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(320);
    buf.append("WebsiteBundleComponentFactory{");
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
    buf.append("configXmlPath").append('=').append(JodaBeanUtils.toString(getConfigXmlPath())).append(',').append(' ');
    buf.append("baseDir").append('=').append(JodaBeanUtils.toString(getBaseDir())).append(',').append(' ');
    buf.append("deployMode").append('=').append(JodaBeanUtils.toString(getDeployMode())).append(',').append(' ');
    buf.append("cacheManager").append('=').append(JodaBeanUtils.toString(getCacheManager())).append(',').append(' ');
    buf.append("compressorLineBreakPosition").append('=').append(JodaBeanUtils.toString(getCompressorLineBreakPosition())).append(',').append(' ');
    buf.append("compressorMunge").append('=').append(JodaBeanUtils.toString(isCompressorMunge())).append(',').append(' ');
    buf.append("compressorPreserveAllSemiColons").append('=').append(JodaBeanUtils.toString(isCompressorPreserveAllSemiColons())).append(',').append(' ');
    buf.append("compressorOptimize").append('=').append(JodaBeanUtils.toString(isCompressorOptimize())).append(',').append(' ');
    buf.append("compressorWarn").append('=').append(JodaBeanUtils.toString(isCompressorWarn())).append(',').append(' ');
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code WebsiteBundleComponentFactory}.
   */
  public static class Meta extends AbstractComponentFactory.Meta {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code configXmlPath} property.
     */
    private final MetaProperty<String> _configXmlPath = DirectMetaProperty.ofReadWrite(
        this, "configXmlPath", WebsiteBundleComponentFactory.class, String.class);
    /**
     * The meta-property for the {@code baseDir} property.
     */
    private final MetaProperty<String> _baseDir = DirectMetaProperty.ofReadWrite(
        this, "baseDir", WebsiteBundleComponentFactory.class, String.class);
    /**
     * The meta-property for the {@code deployMode} property.
     */
    private final MetaProperty<DeployMode> _deployMode = DirectMetaProperty.ofReadWrite(
        this, "deployMode", WebsiteBundleComponentFactory.class, DeployMode.class);
    /**
     * The meta-property for the {@code cacheManager} property.
     */
    private final MetaProperty<CacheManager> _cacheManager = DirectMetaProperty.ofReadWrite(
        this, "cacheManager", WebsiteBundleComponentFactory.class, CacheManager.class);
    /**
     * The meta-property for the {@code compressorLineBreakPosition} property.
     */
    private final MetaProperty<Integer> _compressorLineBreakPosition = DirectMetaProperty.ofReadWrite(
        this, "compressorLineBreakPosition", WebsiteBundleComponentFactory.class, Integer.TYPE);
    /**
     * The meta-property for the {@code compressorMunge} property.
     */
    private final MetaProperty<Boolean> _compressorMunge = DirectMetaProperty.ofReadWrite(
        this, "compressorMunge", WebsiteBundleComponentFactory.class, Boolean.TYPE);
    /**
     * The meta-property for the {@code compressorPreserveAllSemiColons} property.
     */
    private final MetaProperty<Boolean> _compressorPreserveAllSemiColons = DirectMetaProperty.ofReadWrite(
        this, "compressorPreserveAllSemiColons", WebsiteBundleComponentFactory.class, Boolean.TYPE);
    /**
     * The meta-property for the {@code compressorOptimize} property.
     */
    private final MetaProperty<Boolean> _compressorOptimize = DirectMetaProperty.ofReadWrite(
        this, "compressorOptimize", WebsiteBundleComponentFactory.class, Boolean.TYPE);
    /**
     * The meta-property for the {@code compressorWarn} property.
     */
    private final MetaProperty<Boolean> _compressorWarn = DirectMetaProperty.ofReadWrite(
        this, "compressorWarn", WebsiteBundleComponentFactory.class, Boolean.TYPE);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, (DirectMetaPropertyMap) super.metaPropertyMap(),
        "configXmlPath",
        "baseDir",
        "deployMode",
        "cacheManager",
        "compressorLineBreakPosition",
        "compressorMunge",
        "compressorPreserveAllSemiColons",
        "compressorOptimize",
        "compressorWarn");

    /**
     * Restricted constructor.
     */
    protected Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case 1830882106:  // configXmlPath
          return _configXmlPath;
        case -332642308:  // baseDir
          return _baseDir;
        case 1938576170:  // deployMode
          return _deployMode;
        case -1452875317:  // cacheManager
          return _cacheManager;
        case -1678733969:  // compressorLineBreakPosition
          return _compressorLineBreakPosition;
        case 1158477151:  // compressorMunge
          return _compressorMunge;
        case 1247186898:  // compressorPreserveAllSemiColons
          return _compressorPreserveAllSemiColons;
        case 1352649410:  // compressorOptimize
          return _compressorOptimize;
        case -1486371605:  // compressorWarn
          return _compressorWarn;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends WebsiteBundleComponentFactory> builder() {
      return new DirectBeanBuilder<WebsiteBundleComponentFactory>(new WebsiteBundleComponentFactory());
    }

    @Override
    public Class<? extends WebsiteBundleComponentFactory> beanType() {
      return WebsiteBundleComponentFactory.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return _metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code configXmlPath} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> configXmlPath() {
      return _configXmlPath;
    }

    /**
     * The meta-property for the {@code baseDir} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> baseDir() {
      return _baseDir;
    }

    /**
     * The meta-property for the {@code deployMode} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<DeployMode> deployMode() {
      return _deployMode;
    }

    /**
     * The meta-property for the {@code cacheManager} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<CacheManager> cacheManager() {
      return _cacheManager;
    }

    /**
     * The meta-property for the {@code compressorLineBreakPosition} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Integer> compressorLineBreakPosition() {
      return _compressorLineBreakPosition;
    }

    /**
     * The meta-property for the {@code compressorMunge} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Boolean> compressorMunge() {
      return _compressorMunge;
    }

    /**
     * The meta-property for the {@code compressorPreserveAllSemiColons} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Boolean> compressorPreserveAllSemiColons() {
      return _compressorPreserveAllSemiColons;
    }

    /**
     * The meta-property for the {@code compressorOptimize} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Boolean> compressorOptimize() {
      return _compressorOptimize;
    }

    /**
     * The meta-property for the {@code compressorWarn} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Boolean> compressorWarn() {
      return _compressorWarn;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case 1830882106:  // configXmlPath
          return ((WebsiteBundleComponentFactory) bean).getConfigXmlPath();
        case -332642308:  // baseDir
          return ((WebsiteBundleComponentFactory) bean).getBaseDir();
        case 1938576170:  // deployMode
          return ((WebsiteBundleComponentFactory) bean).getDeployMode();
        case -1452875317:  // cacheManager
          return ((WebsiteBundleComponentFactory) bean).getCacheManager();
        case -1678733969:  // compressorLineBreakPosition
          return ((WebsiteBundleComponentFactory) bean).getCompressorLineBreakPosition();
        case 1158477151:  // compressorMunge
          return ((WebsiteBundleComponentFactory) bean).isCompressorMunge();
        case 1247186898:  // compressorPreserveAllSemiColons
          return ((WebsiteBundleComponentFactory) bean).isCompressorPreserveAllSemiColons();
        case 1352649410:  // compressorOptimize
          return ((WebsiteBundleComponentFactory) bean).isCompressorOptimize();
        case -1486371605:  // compressorWarn
          return ((WebsiteBundleComponentFactory) bean).isCompressorWarn();
      }
      return super.propertyGet(bean, propertyName, quiet);
    }

    @Override
    protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {
      switch (propertyName.hashCode()) {
        case 1830882106:  // configXmlPath
          ((WebsiteBundleComponentFactory) bean).setConfigXmlPath((String) newValue);
          return;
        case -332642308:  // baseDir
          ((WebsiteBundleComponentFactory) bean).setBaseDir((String) newValue);
          return;
        case 1938576170:  // deployMode
          ((WebsiteBundleComponentFactory) bean).setDeployMode((DeployMode) newValue);
          return;
        case -1452875317:  // cacheManager
          ((WebsiteBundleComponentFactory) bean).setCacheManager((CacheManager) newValue);
          return;
        case -1678733969:  // compressorLineBreakPosition
          ((WebsiteBundleComponentFactory) bean).setCompressorLineBreakPosition((Integer) newValue);
          return;
        case 1158477151:  // compressorMunge
          ((WebsiteBundleComponentFactory) bean).setCompressorMunge((Boolean) newValue);
          return;
        case 1247186898:  // compressorPreserveAllSemiColons
          ((WebsiteBundleComponentFactory) bean).setCompressorPreserveAllSemiColons((Boolean) newValue);
          return;
        case 1352649410:  // compressorOptimize
          ((WebsiteBundleComponentFactory) bean).setCompressorOptimize((Boolean) newValue);
          return;
        case -1486371605:  // compressorWarn
          ((WebsiteBundleComponentFactory) bean).setCompressorWarn((Boolean) newValue);
          return;
      }
      super.propertySet(bean, propertyName, newValue, quiet);
    }

    @Override
    protected void validate(Bean bean) {
      JodaBeanUtils.notNull(((WebsiteBundleComponentFactory) bean)._baseDir, "baseDir");
      JodaBeanUtils.notNull(((WebsiteBundleComponentFactory) bean)._deployMode, "deployMode");
      super.validate(bean);
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
