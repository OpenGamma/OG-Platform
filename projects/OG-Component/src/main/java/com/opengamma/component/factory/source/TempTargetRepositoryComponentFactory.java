/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.component.factory.source;

import java.io.File;
import java.net.URI;
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

import com.google.common.io.Files;
import com.opengamma.component.ComponentInfo;
import com.opengamma.component.ComponentRepository;
import com.opengamma.component.factory.AbstractComponentFactory;
import com.opengamma.component.factory.ComponentInfoAttributes;
import com.opengamma.financial.temptarget.BerkeleyDBTempTargetRepository;
import com.opengamma.financial.temptarget.EHCachingTempTargetRepository;
import com.opengamma.financial.temptarget.TempTargetRepository;
import com.opengamma.financial.temptarget.TempTargetSource;
import com.opengamma.financial.temptarget.rest.DataTempTargetRepositoryResource;
import com.opengamma.financial.temptarget.rest.DataTempTargetSourceResource;
import com.opengamma.financial.temptarget.rest.RemoteTempTargetRepository;
import com.opengamma.financial.temptarget.rest.RemoteTempTargetSource;

/**
 * Component factory providing the {@code TempTargetRepository} and {@code TempTargetSource}.
 */
@BeanDefinition
public class TempTargetRepositoryComponentFactory extends AbstractComponentFactory {

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
   * Gets the path to use for storage if required and created locally.
   * The folder will be created if it doesn't exist.
   * If it does exist, any files or folders within it may be destroyed.
   * If omitted a folder will be created with {@link Files#createTempDir}.
   * <p>
   * Ignored if {@link #_remoteURL} is specified.
   */
  @PropertyDefinition
  private String _path;
  /**
   * The URI of a remote repository to be used.
   * When this is set, {@link #_publishRest} should normally be set to false
   * - it is normally inefficient to "republish" a REST resource this way.
   * <p>
   * If omitted, a local repository will be created.
   */
  @PropertyDefinition
  private URI _remote;
  /**
   * The cache manager to use if the raw repository should be cached.
   * <p>
   * If omitted there will be no local caching.
   */
  @PropertyDefinition
  private CacheManager _cacheManager;

  //-------------------------------------------------------------------------
  protected void registerSource(final ComponentRepository repo, final TempTargetSource instance) {
    final ComponentInfo info = new ComponentInfo(TempTargetSource.class, getClassifier());
    info.addAttribute(ComponentInfoAttributes.LEVEL, 1);
    info.addAttribute(ComponentInfoAttributes.REMOTE_CLIENT_JAVA, RemoteTempTargetSource.class);
    repo.registerComponent(info, instance);
    if (isPublishRest()) {
      repo.getRestComponents().publish(info, new DataTempTargetSourceResource(instance));
    }
  }

  protected void registerRepository(final ComponentRepository repo, final TempTargetRepository instance) {
    final ComponentInfo info = new ComponentInfo(TempTargetRepository.class, getClassifier());
    info.addAttribute(ComponentInfoAttributes.LEVEL, 1);
    info.addAttribute(ComponentInfoAttributes.REMOTE_CLIENT_JAVA, RemoteTempTargetRepository.class);
    repo.registerComponent(info, instance);
    if (isPublishRest()) {
      repo.getRestComponents().publish(info, new DataTempTargetRepositoryResource(instance));
    }
  }

  protected TempTargetRepository createRemoteRepository(final ComponentRepository repo, final LinkedHashMap<String, String> configuration) {
    return new RemoteTempTargetRepository(getRemote());
  }

  protected TempTargetRepository createLocalRepository(final ComponentRepository repo, final LinkedHashMap<String, String> configuration) {
    final File path;
    if (getPath() != null) {
      path = new File(getPath());
    } else {
      path = Files.createTempDir();
    }
    return new BerkeleyDBTempTargetRepository(path);
  }

  protected TempTargetRepository createRepository(final ComponentRepository repo, final LinkedHashMap<String, String> configuration) {
    if (getRemote() != null) {
      return createRemoteRepository(repo, configuration);
    } else {
      return createLocalRepository(repo, configuration);
    }
  }

  protected TempTargetRepository createCachedRepository(final TempTargetRepository tempTargets) {
    if (getCacheManager() != null) {
      return new EHCachingTempTargetRepository(tempTargets, getCacheManager());
    } else {
      return tempTargets;
    }
  }

  @Override
  public void init(final ComponentRepository repo, final LinkedHashMap<String, String> configuration) {
    final TempTargetRepository tempTargets = createCachedRepository(createRepository(repo, configuration));
    registerSource(repo, tempTargets);
    registerRepository(repo, tempTargets);
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code TempTargetRepositoryComponentFactory}.
   * @return the meta-bean, not null
   */
  public static TempTargetRepositoryComponentFactory.Meta meta() {
    return TempTargetRepositoryComponentFactory.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(TempTargetRepositoryComponentFactory.Meta.INSTANCE);
  }

  @Override
  public TempTargetRepositoryComponentFactory.Meta metaBean() {
    return TempTargetRepositoryComponentFactory.Meta.INSTANCE;
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
   * Gets gets the path to use for storage if required and created locally.
   * The folder will be created if it doesn't exist.
   * If it does exist, any files or folders within it may be destroyed.
   * If omitted a folder will be created with {@link Files#createTempDir}.
   * <p>
   * Ignored if {@link #_remoteURL} is specified.
   * @return the value of the property
   */
  public String getPath() {
    return _path;
  }

  /**
   * Sets gets the path to use for storage if required and created locally.
   * The folder will be created if it doesn't exist.
   * If it does exist, any files or folders within it may be destroyed.
   * If omitted a folder will be created with {@link Files#createTempDir}.
   * <p>
   * Ignored if {@link #_remoteURL} is specified.
   * @param path  the new value of the property
   */
  public void setPath(String path) {
    this._path = path;
  }

  /**
   * Gets the the {@code path} property.
   * The folder will be created if it doesn't exist.
   * If it does exist, any files or folders within it may be destroyed.
   * If omitted a folder will be created with {@link Files#createTempDir}.
   * <p>
   * Ignored if {@link #_remoteURL} is specified.
   * @return the property, not null
   */
  public final Property<String> path() {
    return metaBean().path().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the URI of a remote repository to be used.
   * When this is set, {@link #_publishRest} should normally be set to false
   * - it is normally inefficient to "republish" a REST resource this way.
   * <p>
   * If omitted, a local repository will be created.
   * @return the value of the property
   */
  public URI getRemote() {
    return _remote;
  }

  /**
   * Sets the URI of a remote repository to be used.
   * When this is set, {@link #_publishRest} should normally be set to false
   * - it is normally inefficient to "republish" a REST resource this way.
   * <p>
   * If omitted, a local repository will be created.
   * @param remote  the new value of the property
   */
  public void setRemote(URI remote) {
    this._remote = remote;
  }

  /**
   * Gets the the {@code remote} property.
   * When this is set, {@link #_publishRest} should normally be set to false
   * - it is normally inefficient to "republish" a REST resource this way.
   * <p>
   * If omitted, a local repository will be created.
   * @return the property, not null
   */
  public final Property<URI> remote() {
    return metaBean().remote().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the cache manager to use if the raw repository should be cached.
   * <p>
   * If omitted there will be no local caching.
   * @return the value of the property
   */
  public CacheManager getCacheManager() {
    return _cacheManager;
  }

  /**
   * Sets the cache manager to use if the raw repository should be cached.
   * <p>
   * If omitted there will be no local caching.
   * @param cacheManager  the new value of the property
   */
  public void setCacheManager(CacheManager cacheManager) {
    this._cacheManager = cacheManager;
  }

  /**
   * Gets the the {@code cacheManager} property.
   * <p>
   * If omitted there will be no local caching.
   * @return the property, not null
   */
  public final Property<CacheManager> cacheManager() {
    return metaBean().cacheManager().createProperty(this);
  }

  //-----------------------------------------------------------------------
  @Override
  public TempTargetRepositoryComponentFactory clone() {
    return JodaBeanUtils.cloneAlways(this);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      TempTargetRepositoryComponentFactory other = (TempTargetRepositoryComponentFactory) obj;
      return JodaBeanUtils.equal(getClassifier(), other.getClassifier()) &&
          (isPublishRest() == other.isPublishRest()) &&
          JodaBeanUtils.equal(getPath(), other.getPath()) &&
          JodaBeanUtils.equal(getRemote(), other.getRemote()) &&
          JodaBeanUtils.equal(getCacheManager(), other.getCacheManager()) &&
          super.equals(obj);
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = hash * 31 + JodaBeanUtils.hashCode(getClassifier());
    hash = hash * 31 + JodaBeanUtils.hashCode(isPublishRest());
    hash = hash * 31 + JodaBeanUtils.hashCode(getPath());
    hash = hash * 31 + JodaBeanUtils.hashCode(getRemote());
    hash = hash * 31 + JodaBeanUtils.hashCode(getCacheManager());
    return hash ^ super.hashCode();
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(192);
    buf.append("TempTargetRepositoryComponentFactory{");
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
    buf.append("path").append('=').append(JodaBeanUtils.toString(getPath())).append(',').append(' ');
    buf.append("remote").append('=').append(JodaBeanUtils.toString(getRemote())).append(',').append(' ');
    buf.append("cacheManager").append('=').append(JodaBeanUtils.toString(getCacheManager())).append(',').append(' ');
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code TempTargetRepositoryComponentFactory}.
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
        this, "classifier", TempTargetRepositoryComponentFactory.class, String.class);
    /**
     * The meta-property for the {@code publishRest} property.
     */
    private final MetaProperty<Boolean> _publishRest = DirectMetaProperty.ofReadWrite(
        this, "publishRest", TempTargetRepositoryComponentFactory.class, Boolean.TYPE);
    /**
     * The meta-property for the {@code path} property.
     */
    private final MetaProperty<String> _path = DirectMetaProperty.ofReadWrite(
        this, "path", TempTargetRepositoryComponentFactory.class, String.class);
    /**
     * The meta-property for the {@code remote} property.
     */
    private final MetaProperty<URI> _remote = DirectMetaProperty.ofReadWrite(
        this, "remote", TempTargetRepositoryComponentFactory.class, URI.class);
    /**
     * The meta-property for the {@code cacheManager} property.
     */
    private final MetaProperty<CacheManager> _cacheManager = DirectMetaProperty.ofReadWrite(
        this, "cacheManager", TempTargetRepositoryComponentFactory.class, CacheManager.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, (DirectMetaPropertyMap) super.metaPropertyMap(),
        "classifier",
        "publishRest",
        "path",
        "remote",
        "cacheManager");

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
        case 3433509:  // path
          return _path;
        case -934610874:  // remote
          return _remote;
        case -1452875317:  // cacheManager
          return _cacheManager;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends TempTargetRepositoryComponentFactory> builder() {
      return new DirectBeanBuilder<TempTargetRepositoryComponentFactory>(new TempTargetRepositoryComponentFactory());
    }

    @Override
    public Class<? extends TempTargetRepositoryComponentFactory> beanType() {
      return TempTargetRepositoryComponentFactory.class;
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
     * The meta-property for the {@code path} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> path() {
      return _path;
    }

    /**
     * The meta-property for the {@code remote} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<URI> remote() {
      return _remote;
    }

    /**
     * The meta-property for the {@code cacheManager} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<CacheManager> cacheManager() {
      return _cacheManager;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -281470431:  // classifier
          return ((TempTargetRepositoryComponentFactory) bean).getClassifier();
        case -614707837:  // publishRest
          return ((TempTargetRepositoryComponentFactory) bean).isPublishRest();
        case 3433509:  // path
          return ((TempTargetRepositoryComponentFactory) bean).getPath();
        case -934610874:  // remote
          return ((TempTargetRepositoryComponentFactory) bean).getRemote();
        case -1452875317:  // cacheManager
          return ((TempTargetRepositoryComponentFactory) bean).getCacheManager();
      }
      return super.propertyGet(bean, propertyName, quiet);
    }

    @Override
    protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -281470431:  // classifier
          ((TempTargetRepositoryComponentFactory) bean).setClassifier((String) newValue);
          return;
        case -614707837:  // publishRest
          ((TempTargetRepositoryComponentFactory) bean).setPublishRest((Boolean) newValue);
          return;
        case 3433509:  // path
          ((TempTargetRepositoryComponentFactory) bean).setPath((String) newValue);
          return;
        case -934610874:  // remote
          ((TempTargetRepositoryComponentFactory) bean).setRemote((URI) newValue);
          return;
        case -1452875317:  // cacheManager
          ((TempTargetRepositoryComponentFactory) bean).setCacheManager((CacheManager) newValue);
          return;
      }
      super.propertySet(bean, propertyName, newValue, quiet);
    }

    @Override
    protected void validate(Bean bean) {
      JodaBeanUtils.notNull(((TempTargetRepositoryComponentFactory) bean)._classifier, "classifier");
      super.validate(bean);
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
