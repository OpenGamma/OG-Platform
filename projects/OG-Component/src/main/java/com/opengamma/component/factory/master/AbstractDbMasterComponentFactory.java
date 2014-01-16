/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.component.factory.master;

import java.util.LinkedHashMap;
import java.util.Map;

import org.joda.beans.Bean;
import org.joda.beans.BeanBuilder;
import org.joda.beans.BeanDefinition;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaProperty;
import org.joda.beans.Property;
import org.joda.beans.PropertyDefinition;
import org.joda.beans.impl.direct.DirectMetaProperty;
import org.joda.beans.impl.direct.DirectMetaPropertyMap;

import com.opengamma.component.ComponentInfo;
import com.opengamma.component.ComponentRepository;
import com.opengamma.component.factory.AbstractComponentFactory;
import com.opengamma.component.factory.ComponentInfoAttributes;
import com.opengamma.master.impl.AbstractRemoteMaster;
import com.opengamma.masterdb.AbstractDbMaster;
import com.opengamma.masterdb.ConfigurableDbMaster;
import com.opengamma.util.db.DbConnector;
import com.opengamma.util.rest.AbstractDataResource;

/**
 * Base component factory delegate for all {@link AbstractDbMaster} implementations.
 * @param <I> the master interface to use. this must be a super-interface of M and would typically
 * extend from AbstractDbMaster. (Not stated explicitly due to limitation in Joda-beans).
 * @param <M> the db master type
 */
@BeanDefinition
public abstract class AbstractDbMasterComponentFactory<I, M extends ConfigurableDbMaster> extends AbstractComponentFactory {


  /**
   * The classifier that the factory should publish under.
   */
  @PropertyDefinition
  private String _classifier;
  /**
   * The flag determining whether the component should be published by REST (default true).
   */
  @PropertyDefinition
  private boolean _publishRest = true;
  
  /**
   * The database connector.
   */
  @PropertyDefinition
  private DbConnector _dbConnector;
  /**
   * The flag determining whether to enforce the schema version, preventing the server from starting if the version
   * does not match the expected version.
   */
  @PropertyDefinition
  private boolean _enforceSchemaVersion = true;
  /**
   * The flag determining whether to manage the database objects automatically.
   * <p>
   * The database objects will be created if they do not exist, and will be upgraded if their version is older than the
   * server expects. Database objects will never be deleted and the server will fail to start if the database is found
   * in an unexpected state.
   * <p>
   * This flag is intended for use with temporary user databases. 
   */
  @PropertyDefinition
  private boolean _autoSchemaManagement;
  
  /**
   * The scheme used by the {@code UniqueId}.
   */
  @PropertyDefinition
  private String _uniqueIdScheme;

  
  /**
   * The maximum number of retries when updating.
   */
  @PropertyDefinition
  private Integer _maxRetries;
  
  /**
   * Whether to enable tracking mode. This will result in the implementing component factory being called with 
   * wrapMasterWithTrackingInterface().
   */
  @PropertyDefinition
  private boolean _trackingMode;
  
  
  private final String _schemaName;
  private final Class<I> _masterInterface; 
  private final Class<? extends AbstractRemoteMaster> _remoteInterface;

  
  protected AbstractDbMasterComponentFactory(String schemaName, Class<I> masterInterface, Class<? extends AbstractRemoteMaster> remoteInterface) {
    _schemaName = schemaName;
    _masterInterface = masterInterface;
    _remoteInterface = remoteInterface;
  }
  
  @Override
  public void init(ComponentRepository repo, LinkedHashMap<String, String> configuration) throws Exception {
    
    ComponentInfo info = new ComponentInfo(_masterInterface, getClassifier());
    
    M dbMaster = createMaster(repo, info);
    
    if (getUniqueIdScheme() != null) {
      dbMaster.setUniqueIdScheme(getUniqueIdScheme());
    }
    
    String resolvedScheme = dbMaster.getUniqueIdScheme();
    
    if (getMaxRetries() != null) {
      dbMaster.setMaxRetries(getMaxRetries());
    }
    
    OGSchema ogSchema = OGSchema.on(getDbConnector())
        .enforcingSchemaVersion(isEnforceSchemaVersion())
        .withAutoSchemaManagement(isAutoSchemaManagement())
        .build();

    ogSchema.checkSchema(dbMaster.getSchemaVersion(), _schemaName);

    I postProcessedMaster = postProcess(dbMaster);
    
    if (isTrackingMode()) {
      postProcessedMaster = wrapMasterWithTrackingInterface(postProcessedMaster);
    }
    
    // register
    info.addAttribute(ComponentInfoAttributes.LEVEL, 1);
    info.addAttribute(ComponentInfoAttributes.REMOTE_CLIENT_JAVA, _remoteInterface);
    info.addAttribute(ComponentInfoAttributes.UNIQUE_ID_SCHEME, resolvedScheme);
    repo.registerComponent(info, postProcessedMaster);

    if (isPublishRest()) {
      repo.getRestComponents().publish(info, createPublishedResource(dbMaster, postProcessedMaster));
    }
    
  }
  
  
  /**
   * Adds tracking to the master by wrapping it, if it is supported.
   * Only called if trackingMode is enabled.
   * @param postProcessedMaster the master to wrap
   * @return the wrapped master
   * @throws UnsupportedOperationException if this master doesn't support tracking
   */
  protected abstract I wrapMasterWithTrackingInterface(I postProcessedMaster);
  
  
  protected abstract M createMaster(ComponentRepository repo, ComponentInfo info) throws Exception;

  /**
   * Create the rest-published resource. The resource can be created using the original
   * db-master or the post-processed master, whichever is required.
   * @param dbMaster the master instance created by the {@link #createMaster(ComponentRepository, ComponentInfo)}
   *  call.
   * @param postProcessedMaster whatever was returned from the postProcess method
   * @return the {@link AbstractDataResource} to publish
   */
  protected abstract AbstractDataResource createPublishedResource(M dbMaster, I postProcessedMaster);

  /**
   * Apply any post-processing, such as wrapping. Can be overridden
   * if required.
   * @param master the master to apply post-processing to
   * @return the post-processed master
   */
  protected I postProcess(M master) {
    return _masterInterface.cast(master);
  }
  
  
  
  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code AbstractDbMasterComponentFactory}.
   * @return the meta-bean, not null
   */
  @SuppressWarnings("rawtypes")
  public static AbstractDbMasterComponentFactory.Meta meta() {
    return AbstractDbMasterComponentFactory.Meta.INSTANCE;
  }

  /**
   * The meta-bean for {@code AbstractDbMasterComponentFactory}.
   * @param <R>  the first generic type
   * @param <S>  the second generic type
   * @param cls1  the first generic type
   * @param cls2  the second generic type
   * @return the meta-bean, not null
   */
  @SuppressWarnings("unchecked")
  public static <R, S extends ConfigurableDbMaster> AbstractDbMasterComponentFactory.Meta<R, S> metaAbstractDbMasterComponentFactory(Class<R> cls1, Class<S> cls2) {
    return AbstractDbMasterComponentFactory.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(AbstractDbMasterComponentFactory.Meta.INSTANCE);
  }

  @SuppressWarnings("unchecked")
  @Override
  public AbstractDbMasterComponentFactory.Meta<I, M> metaBean() {
    return AbstractDbMasterComponentFactory.Meta.INSTANCE;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the classifier that the factory should publish under.
   * @return the value of the property
   */
  public String getClassifier() {
    return _classifier;
  }

  /**
   * Sets the classifier that the factory should publish under.
   * @param classifier  the new value of the property
   */
  public void setClassifier(String classifier) {
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
   * Gets the database connector.
   * @return the value of the property
   */
  public DbConnector getDbConnector() {
    return _dbConnector;
  }

  /**
   * Sets the database connector.
   * @param dbConnector  the new value of the property
   */
  public void setDbConnector(DbConnector dbConnector) {
    this._dbConnector = dbConnector;
  }

  /**
   * Gets the the {@code dbConnector} property.
   * @return the property, not null
   */
  public final Property<DbConnector> dbConnector() {
    return metaBean().dbConnector().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the flag determining whether to enforce the schema version, preventing the server from starting if the version
   * does not match the expected version.
   * @return the value of the property
   */
  public boolean isEnforceSchemaVersion() {
    return _enforceSchemaVersion;
  }

  /**
   * Sets the flag determining whether to enforce the schema version, preventing the server from starting if the version
   * does not match the expected version.
   * @param enforceSchemaVersion  the new value of the property
   */
  public void setEnforceSchemaVersion(boolean enforceSchemaVersion) {
    this._enforceSchemaVersion = enforceSchemaVersion;
  }

  /**
   * Gets the the {@code enforceSchemaVersion} property.
   * does not match the expected version.
   * @return the property, not null
   */
  public final Property<Boolean> enforceSchemaVersion() {
    return metaBean().enforceSchemaVersion().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the flag determining whether to manage the database objects automatically.
   * <p>
   * The database objects will be created if they do not exist, and will be upgraded if their version is older than the
   * server expects. Database objects will never be deleted and the server will fail to start if the database is found
   * in an unexpected state.
   * <p>
   * This flag is intended for use with temporary user databases.
   * @return the value of the property
   */
  public boolean isAutoSchemaManagement() {
    return _autoSchemaManagement;
  }

  /**
   * Sets the flag determining whether to manage the database objects automatically.
   * <p>
   * The database objects will be created if they do not exist, and will be upgraded if their version is older than the
   * server expects. Database objects will never be deleted and the server will fail to start if the database is found
   * in an unexpected state.
   * <p>
   * This flag is intended for use with temporary user databases.
   * @param autoSchemaManagement  the new value of the property
   */
  public void setAutoSchemaManagement(boolean autoSchemaManagement) {
    this._autoSchemaManagement = autoSchemaManagement;
  }

  /**
   * Gets the the {@code autoSchemaManagement} property.
   * <p>
   * The database objects will be created if they do not exist, and will be upgraded if their version is older than the
   * server expects. Database objects will never be deleted and the server will fail to start if the database is found
   * in an unexpected state.
   * <p>
   * This flag is intended for use with temporary user databases.
   * @return the property, not null
   */
  public final Property<Boolean> autoSchemaManagement() {
    return metaBean().autoSchemaManagement().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the scheme used by the {@code UniqueId}.
   * @return the value of the property
   */
  public String getUniqueIdScheme() {
    return _uniqueIdScheme;
  }

  /**
   * Sets the scheme used by the {@code UniqueId}.
   * @param uniqueIdScheme  the new value of the property
   */
  public void setUniqueIdScheme(String uniqueIdScheme) {
    this._uniqueIdScheme = uniqueIdScheme;
  }

  /**
   * Gets the the {@code uniqueIdScheme} property.
   * @return the property, not null
   */
  public final Property<String> uniqueIdScheme() {
    return metaBean().uniqueIdScheme().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the maximum number of retries when updating.
   * @return the value of the property
   */
  public Integer getMaxRetries() {
    return _maxRetries;
  }

  /**
   * Sets the maximum number of retries when updating.
   * @param maxRetries  the new value of the property
   */
  public void setMaxRetries(Integer maxRetries) {
    this._maxRetries = maxRetries;
  }

  /**
   * Gets the the {@code maxRetries} property.
   * @return the property, not null
   */
  public final Property<Integer> maxRetries() {
    return metaBean().maxRetries().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets whether to enable tracking mode. This will result in the implementing component factory being called with
   * wrapMasterWithTrackingInterface().
   * @return the value of the property
   */
  public boolean isTrackingMode() {
    return _trackingMode;
  }

  /**
   * Sets whether to enable tracking mode. This will result in the implementing component factory being called with
   * wrapMasterWithTrackingInterface().
   * @param trackingMode  the new value of the property
   */
  public void setTrackingMode(boolean trackingMode) {
    this._trackingMode = trackingMode;
  }

  /**
   * Gets the the {@code trackingMode} property.
   * wrapMasterWithTrackingInterface().
   * @return the property, not null
   */
  public final Property<Boolean> trackingMode() {
    return metaBean().trackingMode().createProperty(this);
  }

  //-----------------------------------------------------------------------
  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      AbstractDbMasterComponentFactory<?, ?> other = (AbstractDbMasterComponentFactory<?, ?>) obj;
      return JodaBeanUtils.equal(getClassifier(), other.getClassifier()) &&
          (isPublishRest() == other.isPublishRest()) &&
          JodaBeanUtils.equal(getDbConnector(), other.getDbConnector()) &&
          (isEnforceSchemaVersion() == other.isEnforceSchemaVersion()) &&
          (isAutoSchemaManagement() == other.isAutoSchemaManagement()) &&
          JodaBeanUtils.equal(getUniqueIdScheme(), other.getUniqueIdScheme()) &&
          JodaBeanUtils.equal(getMaxRetries(), other.getMaxRetries()) &&
          (isTrackingMode() == other.isTrackingMode()) &&
          super.equals(obj);
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash += hash * 31 + JodaBeanUtils.hashCode(getClassifier());
    hash += hash * 31 + JodaBeanUtils.hashCode(isPublishRest());
    hash += hash * 31 + JodaBeanUtils.hashCode(getDbConnector());
    hash += hash * 31 + JodaBeanUtils.hashCode(isEnforceSchemaVersion());
    hash += hash * 31 + JodaBeanUtils.hashCode(isAutoSchemaManagement());
    hash += hash * 31 + JodaBeanUtils.hashCode(getUniqueIdScheme());
    hash += hash * 31 + JodaBeanUtils.hashCode(getMaxRetries());
    hash += hash * 31 + JodaBeanUtils.hashCode(isTrackingMode());
    return hash ^ super.hashCode();
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(288);
    buf.append("AbstractDbMasterComponentFactory{");
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
    buf.append("dbConnector").append('=').append(JodaBeanUtils.toString(getDbConnector())).append(',').append(' ');
    buf.append("enforceSchemaVersion").append('=').append(JodaBeanUtils.toString(isEnforceSchemaVersion())).append(',').append(' ');
    buf.append("autoSchemaManagement").append('=').append(JodaBeanUtils.toString(isAutoSchemaManagement())).append(',').append(' ');
    buf.append("uniqueIdScheme").append('=').append(JodaBeanUtils.toString(getUniqueIdScheme())).append(',').append(' ');
    buf.append("maxRetries").append('=').append(JodaBeanUtils.toString(getMaxRetries())).append(',').append(' ');
    buf.append("trackingMode").append('=').append(JodaBeanUtils.toString(isTrackingMode())).append(',').append(' ');
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code AbstractDbMasterComponentFactory}.
   */
  public static class Meta<I, M extends ConfigurableDbMaster> extends AbstractComponentFactory.Meta {
    /**
     * The singleton instance of the meta-bean.
     */
    @SuppressWarnings("rawtypes")
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code classifier} property.
     */
    private final MetaProperty<String> _classifier = DirectMetaProperty.ofReadWrite(
        this, "classifier", AbstractDbMasterComponentFactory.class, String.class);
    /**
     * The meta-property for the {@code publishRest} property.
     */
    private final MetaProperty<Boolean> _publishRest = DirectMetaProperty.ofReadWrite(
        this, "publishRest", AbstractDbMasterComponentFactory.class, Boolean.TYPE);
    /**
     * The meta-property for the {@code dbConnector} property.
     */
    private final MetaProperty<DbConnector> _dbConnector = DirectMetaProperty.ofReadWrite(
        this, "dbConnector", AbstractDbMasterComponentFactory.class, DbConnector.class);
    /**
     * The meta-property for the {@code enforceSchemaVersion} property.
     */
    private final MetaProperty<Boolean> _enforceSchemaVersion = DirectMetaProperty.ofReadWrite(
        this, "enforceSchemaVersion", AbstractDbMasterComponentFactory.class, Boolean.TYPE);
    /**
     * The meta-property for the {@code autoSchemaManagement} property.
     */
    private final MetaProperty<Boolean> _autoSchemaManagement = DirectMetaProperty.ofReadWrite(
        this, "autoSchemaManagement", AbstractDbMasterComponentFactory.class, Boolean.TYPE);
    /**
     * The meta-property for the {@code uniqueIdScheme} property.
     */
    private final MetaProperty<String> _uniqueIdScheme = DirectMetaProperty.ofReadWrite(
        this, "uniqueIdScheme", AbstractDbMasterComponentFactory.class, String.class);
    /**
     * The meta-property for the {@code maxRetries} property.
     */
    private final MetaProperty<Integer> _maxRetries = DirectMetaProperty.ofReadWrite(
        this, "maxRetries", AbstractDbMasterComponentFactory.class, Integer.class);
    /**
     * The meta-property for the {@code trackingMode} property.
     */
    private final MetaProperty<Boolean> _trackingMode = DirectMetaProperty.ofReadWrite(
        this, "trackingMode", AbstractDbMasterComponentFactory.class, Boolean.TYPE);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, (DirectMetaPropertyMap) super.metaPropertyMap(),
        "classifier",
        "publishRest",
        "dbConnector",
        "enforceSchemaVersion",
        "autoSchemaManagement",
        "uniqueIdScheme",
        "maxRetries",
        "trackingMode");

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
        case 39794031:  // dbConnector
          return _dbConnector;
        case 2128193333:  // enforceSchemaVersion
          return _enforceSchemaVersion;
        case 1236703379:  // autoSchemaManagement
          return _autoSchemaManagement;
        case -1737146991:  // uniqueIdScheme
          return _uniqueIdScheme;
        case -2022653118:  // maxRetries
          return _maxRetries;
        case -1884120838:  // trackingMode
          return _trackingMode;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends AbstractDbMasterComponentFactory<I, M>> builder() {
      throw new UnsupportedOperationException("AbstractDbMasterComponentFactory is an abstract class");
    }

    @SuppressWarnings({"unchecked", "rawtypes" })
    @Override
    public Class<? extends AbstractDbMasterComponentFactory<I, M>> beanType() {
      return (Class) AbstractDbMasterComponentFactory.class;
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
     * The meta-property for the {@code dbConnector} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<DbConnector> dbConnector() {
      return _dbConnector;
    }

    /**
     * The meta-property for the {@code enforceSchemaVersion} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Boolean> enforceSchemaVersion() {
      return _enforceSchemaVersion;
    }

    /**
     * The meta-property for the {@code autoSchemaManagement} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Boolean> autoSchemaManagement() {
      return _autoSchemaManagement;
    }

    /**
     * The meta-property for the {@code uniqueIdScheme} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> uniqueIdScheme() {
      return _uniqueIdScheme;
    }

    /**
     * The meta-property for the {@code maxRetries} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Integer> maxRetries() {
      return _maxRetries;
    }

    /**
     * The meta-property for the {@code trackingMode} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Boolean> trackingMode() {
      return _trackingMode;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -281470431:  // classifier
          return ((AbstractDbMasterComponentFactory<?, ?>) bean).getClassifier();
        case -614707837:  // publishRest
          return ((AbstractDbMasterComponentFactory<?, ?>) bean).isPublishRest();
        case 39794031:  // dbConnector
          return ((AbstractDbMasterComponentFactory<?, ?>) bean).getDbConnector();
        case 2128193333:  // enforceSchemaVersion
          return ((AbstractDbMasterComponentFactory<?, ?>) bean).isEnforceSchemaVersion();
        case 1236703379:  // autoSchemaManagement
          return ((AbstractDbMasterComponentFactory<?, ?>) bean).isAutoSchemaManagement();
        case -1737146991:  // uniqueIdScheme
          return ((AbstractDbMasterComponentFactory<?, ?>) bean).getUniqueIdScheme();
        case -2022653118:  // maxRetries
          return ((AbstractDbMasterComponentFactory<?, ?>) bean).getMaxRetries();
        case -1884120838:  // trackingMode
          return ((AbstractDbMasterComponentFactory<?, ?>) bean).isTrackingMode();
      }
      return super.propertyGet(bean, propertyName, quiet);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -281470431:  // classifier
          ((AbstractDbMasterComponentFactory<I, M>) bean).setClassifier((String) newValue);
          return;
        case -614707837:  // publishRest
          ((AbstractDbMasterComponentFactory<I, M>) bean).setPublishRest((Boolean) newValue);
          return;
        case 39794031:  // dbConnector
          ((AbstractDbMasterComponentFactory<I, M>) bean).setDbConnector((DbConnector) newValue);
          return;
        case 2128193333:  // enforceSchemaVersion
          ((AbstractDbMasterComponentFactory<I, M>) bean).setEnforceSchemaVersion((Boolean) newValue);
          return;
        case 1236703379:  // autoSchemaManagement
          ((AbstractDbMasterComponentFactory<I, M>) bean).setAutoSchemaManagement((Boolean) newValue);
          return;
        case -1737146991:  // uniqueIdScheme
          ((AbstractDbMasterComponentFactory<I, M>) bean).setUniqueIdScheme((String) newValue);
          return;
        case -2022653118:  // maxRetries
          ((AbstractDbMasterComponentFactory<I, M>) bean).setMaxRetries((Integer) newValue);
          return;
        case -1884120838:  // trackingMode
          ((AbstractDbMasterComponentFactory<I, M>) bean).setTrackingMode((Boolean) newValue);
          return;
      }
      super.propertySet(bean, propertyName, newValue, quiet);
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
