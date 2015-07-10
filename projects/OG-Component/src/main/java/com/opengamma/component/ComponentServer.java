/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.component;

import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.joda.beans.Bean;
import org.joda.beans.BeanBuilder;
import org.joda.beans.BeanDefinition;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaProperty;
import org.joda.beans.Property;
import org.joda.beans.PropertyDefinition;
import org.joda.beans.impl.direct.DirectBeanBuilder;
import org.joda.beans.impl.direct.DirectMetaBean;
import org.joda.beans.impl.direct.DirectMetaProperty;
import org.joda.beans.impl.direct.DirectMetaPropertyMap;
import org.threeten.bp.Instant;

import com.opengamma.component.rest.RemoteComponentServer;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.VersionUtils;

/**
 * Information about a server of the OpenGamma system.
 * <p>
 * This object is used to represent the entire set of components
 * available at a single server within an OpenGamma system.
 * Its primary purpose is to allow that data to be transferred across the network.
 */
@BeanDefinition
public class ComponentServer implements Bean {

  /**
   * The URI that the server is published at.
   * <p>
   * This is set by {@link RemoteComponentServer}.
   */
  @PropertyDefinition(validate = "notNull")
  private URI _uri;
  /**
   * The software version running at the server.
   */
  @PropertyDefinition(set = "private")
  private String _version;
  /**
   * The software build running at the server.
   */
  @PropertyDefinition(set = "private")
  private String _build;
  /**
   * The software build ID running at the server.
   */
  @PropertyDefinition(set = "private")
  private String _buildId;
  /**
   * The current instant based on the clock of the server.
   */
  @PropertyDefinition(set = "private")
  private Instant _currentInstant;
  /**
   * The complete set of available components.
   */
  @PropertyDefinition(validate = "notNull")
  private List<ComponentInfo> _componentInfos = new ArrayList<ComponentInfo>();

  /**
   * Creates an instance.
   */
  protected ComponentServer() {
  }

  /**
   * Creates an instance.
   * 
   * @param uri  the URI of the server, not null
   */
  public ComponentServer(URI uri) {
    setUri(uri);
    setVersion(VersionUtils.deriveVersion());
    setBuild(VersionUtils.deriveBuild());
    setBuildId(VersionUtils.deriveBuildId());
    setCurrentInstant(Instant.now());
  }

  //-------------------------------------------------------------------------
  /**
   * Finds a component by type and classifier.
   * <p>
   * The returned information will contain the URI of the component for access.
   * 
   * @param type  the type of the component, typically an interface
   * @param classifier  the classifier of the type, used to name instances of the same type
   * @return the info for the component, not null
   * @throws IllegalArgumentException if the component cannot be found
   */
  public ComponentInfo getComponentInfo(Class<?> type, String classifier) {
    for (ComponentInfo info : getComponentInfos()) {
      if (info.matches(type, classifier)) {
        return info;
      }
    }
    throw new IllegalArgumentException("Component not found: " + type + "::" + classifier);
  }

  /**
   * Finds a component by type.
   * <p>
   * The returned information will contain the URI of the component for access.
   * 
   * @param type  the type of the component, typically an interface
   * @return all the matching components, not null
   */
  public List<ComponentInfo> getComponentInfos(Class<?> type) {
    List<ComponentInfo> result = new ArrayList<ComponentInfo>();
    for (ComponentInfo info : getComponentInfos()) {
      if (info.getType().equals(type)) {
        result.add(info);
      }
    }
    return result;
  }

  /**
   * Gets a map of component information for a given type, keyed by classifier.
   * <p>
   * The returned information will contain the URI of the component for access.
   * 
   * @param type  the type of the component, typically an interface
   * @return all the matching components, keyed by classifier, not null
   */
  public Map<String, ComponentInfo> getComponentInfoMap(Class<?> type) {
    Map<String, ComponentInfo> result = new LinkedHashMap<String, ComponentInfo>();
    for (ComponentInfo info : getComponentInfos()) {
      if (info.getType().equals(type)) {
        result.put(info.getClassifier(), info);
      }
    }
    return result;
  }

  //-------------------------------------------------------------------------
  /**
   * Applies the URI for the server.
   * <p>
   * This recursively sets the URI onto any component informations that have a relative URI.
   * This is normally called after the information is retrieved from a remote server.
   *
   * @param baseUri  the base URI of the server, not null
   */
  public void applyBaseUri(URI baseUri) {
    ArgumentChecker.notNull(baseUri, "baseUri");
    setUri(baseUri);
    for (ComponentInfo info : getComponentInfos()) {
      info.setUri(baseUri.resolve(info.getUri()));
    }
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code ComponentServer}.
   * @return the meta-bean, not null
   */
  public static ComponentServer.Meta meta() {
    return ComponentServer.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(ComponentServer.Meta.INSTANCE);
  }

  @Override
  public ComponentServer.Meta metaBean() {
    return ComponentServer.Meta.INSTANCE;
  }

  @Override
  public <R> Property<R> property(String propertyName) {
    return metaBean().<R>metaProperty(propertyName).createProperty(this);
  }

  @Override
  public Set<String> propertyNames() {
    return metaBean().metaPropertyMap().keySet();
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the URI that the server is published at.
   * <p>
   * This is set by {@link RemoteComponentServer}.
   * @return the value of the property, not null
   */
  public URI getUri() {
    return _uri;
  }

  /**
   * Sets the URI that the server is published at.
   * <p>
   * This is set by {@link RemoteComponentServer}.
   * @param uri  the new value of the property, not null
   */
  public void setUri(URI uri) {
    JodaBeanUtils.notNull(uri, "uri");
    this._uri = uri;
  }

  /**
   * Gets the the {@code uri} property.
   * <p>
   * This is set by {@link RemoteComponentServer}.
   * @return the property, not null
   */
  public final Property<URI> uri() {
    return metaBean().uri().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the software version running at the server.
   * @return the value of the property
   */
  public String getVersion() {
    return _version;
  }

  /**
   * Sets the software version running at the server.
   * @param version  the new value of the property
   */
  private void setVersion(String version) {
    this._version = version;
  }

  /**
   * Gets the the {@code version} property.
   * @return the property, not null
   */
  public final Property<String> version() {
    return metaBean().version().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the software build running at the server.
   * @return the value of the property
   */
  public String getBuild() {
    return _build;
  }

  /**
   * Sets the software build running at the server.
   * @param build  the new value of the property
   */
  private void setBuild(String build) {
    this._build = build;
  }

  /**
   * Gets the the {@code build} property.
   * @return the property, not null
   */
  public final Property<String> build() {
    return metaBean().build().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the software build ID running at the server.
   * @return the value of the property
   */
  public String getBuildId() {
    return _buildId;
  }

  /**
   * Sets the software build ID running at the server.
   * @param buildId  the new value of the property
   */
  private void setBuildId(String buildId) {
    this._buildId = buildId;
  }

  /**
   * Gets the the {@code buildId} property.
   * @return the property, not null
   */
  public final Property<String> buildId() {
    return metaBean().buildId().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the current instant based on the clock of the server.
   * @return the value of the property
   */
  public Instant getCurrentInstant() {
    return _currentInstant;
  }

  /**
   * Sets the current instant based on the clock of the server.
   * @param currentInstant  the new value of the property
   */
  private void setCurrentInstant(Instant currentInstant) {
    this._currentInstant = currentInstant;
  }

  /**
   * Gets the the {@code currentInstant} property.
   * @return the property, not null
   */
  public final Property<Instant> currentInstant() {
    return metaBean().currentInstant().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the complete set of available components.
   * @return the value of the property, not null
   */
  public List<ComponentInfo> getComponentInfos() {
    return _componentInfos;
  }

  /**
   * Sets the complete set of available components.
   * @param componentInfos  the new value of the property, not null
   */
  public void setComponentInfos(List<ComponentInfo> componentInfos) {
    JodaBeanUtils.notNull(componentInfos, "componentInfos");
    this._componentInfos = componentInfos;
  }

  /**
   * Gets the the {@code componentInfos} property.
   * @return the property, not null
   */
  public final Property<List<ComponentInfo>> componentInfos() {
    return metaBean().componentInfos().createProperty(this);
  }

  //-----------------------------------------------------------------------
  @Override
  public ComponentServer clone() {
    return JodaBeanUtils.cloneAlways(this);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      ComponentServer other = (ComponentServer) obj;
      return JodaBeanUtils.equal(getUri(), other.getUri()) &&
          JodaBeanUtils.equal(getVersion(), other.getVersion()) &&
          JodaBeanUtils.equal(getBuild(), other.getBuild()) &&
          JodaBeanUtils.equal(getBuildId(), other.getBuildId()) &&
          JodaBeanUtils.equal(getCurrentInstant(), other.getCurrentInstant()) &&
          JodaBeanUtils.equal(getComponentInfos(), other.getComponentInfos());
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = getClass().hashCode();
    hash = hash * 31 + JodaBeanUtils.hashCode(getUri());
    hash = hash * 31 + JodaBeanUtils.hashCode(getVersion());
    hash = hash * 31 + JodaBeanUtils.hashCode(getBuild());
    hash = hash * 31 + JodaBeanUtils.hashCode(getBuildId());
    hash = hash * 31 + JodaBeanUtils.hashCode(getCurrentInstant());
    hash = hash * 31 + JodaBeanUtils.hashCode(getComponentInfos());
    return hash;
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(224);
    buf.append("ComponentServer{");
    int len = buf.length();
    toString(buf);
    if (buf.length() > len) {
      buf.setLength(buf.length() - 2);
    }
    buf.append('}');
    return buf.toString();
  }

  protected void toString(StringBuilder buf) {
    buf.append("uri").append('=').append(JodaBeanUtils.toString(getUri())).append(',').append(' ');
    buf.append("version").append('=').append(JodaBeanUtils.toString(getVersion())).append(',').append(' ');
    buf.append("build").append('=').append(JodaBeanUtils.toString(getBuild())).append(',').append(' ');
    buf.append("buildId").append('=').append(JodaBeanUtils.toString(getBuildId())).append(',').append(' ');
    buf.append("currentInstant").append('=').append(JodaBeanUtils.toString(getCurrentInstant())).append(',').append(' ');
    buf.append("componentInfos").append('=').append(JodaBeanUtils.toString(getComponentInfos())).append(',').append(' ');
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code ComponentServer}.
   */
  public static class Meta extends DirectMetaBean {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code uri} property.
     */
    private final MetaProperty<URI> _uri = DirectMetaProperty.ofReadWrite(
        this, "uri", ComponentServer.class, URI.class);
    /**
     * The meta-property for the {@code version} property.
     */
    private final MetaProperty<String> _version = DirectMetaProperty.ofReadWrite(
        this, "version", ComponentServer.class, String.class);
    /**
     * The meta-property for the {@code build} property.
     */
    private final MetaProperty<String> _build = DirectMetaProperty.ofReadWrite(
        this, "build", ComponentServer.class, String.class);
    /**
     * The meta-property for the {@code buildId} property.
     */
    private final MetaProperty<String> _buildId = DirectMetaProperty.ofReadWrite(
        this, "buildId", ComponentServer.class, String.class);
    /**
     * The meta-property for the {@code currentInstant} property.
     */
    private final MetaProperty<Instant> _currentInstant = DirectMetaProperty.ofReadWrite(
        this, "currentInstant", ComponentServer.class, Instant.class);
    /**
     * The meta-property for the {@code componentInfos} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<List<ComponentInfo>> _componentInfos = DirectMetaProperty.ofReadWrite(
        this, "componentInfos", ComponentServer.class, (Class) List.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, null,
        "uri",
        "version",
        "build",
        "buildId",
        "currentInstant",
        "componentInfos");

    /**
     * Restricted constructor.
     */
    protected Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case 116076:  // uri
          return _uri;
        case 351608024:  // version
          return _version;
        case 94094958:  // build
          return _build;
        case 230943785:  // buildId
          return _buildId;
        case 367695400:  // currentInstant
          return _currentInstant;
        case 1349827208:  // componentInfos
          return _componentInfos;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends ComponentServer> builder() {
      return new DirectBeanBuilder<ComponentServer>(new ComponentServer());
    }

    @Override
    public Class<? extends ComponentServer> beanType() {
      return ComponentServer.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return _metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code uri} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<URI> uri() {
      return _uri;
    }

    /**
     * The meta-property for the {@code version} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> version() {
      return _version;
    }

    /**
     * The meta-property for the {@code build} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> build() {
      return _build;
    }

    /**
     * The meta-property for the {@code buildId} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> buildId() {
      return _buildId;
    }

    /**
     * The meta-property for the {@code currentInstant} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Instant> currentInstant() {
      return _currentInstant;
    }

    /**
     * The meta-property for the {@code componentInfos} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<List<ComponentInfo>> componentInfos() {
      return _componentInfos;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case 116076:  // uri
          return ((ComponentServer) bean).getUri();
        case 351608024:  // version
          return ((ComponentServer) bean).getVersion();
        case 94094958:  // build
          return ((ComponentServer) bean).getBuild();
        case 230943785:  // buildId
          return ((ComponentServer) bean).getBuildId();
        case 367695400:  // currentInstant
          return ((ComponentServer) bean).getCurrentInstant();
        case 1349827208:  // componentInfos
          return ((ComponentServer) bean).getComponentInfos();
      }
      return super.propertyGet(bean, propertyName, quiet);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {
      switch (propertyName.hashCode()) {
        case 116076:  // uri
          ((ComponentServer) bean).setUri((URI) newValue);
          return;
        case 351608024:  // version
          ((ComponentServer) bean).setVersion((String) newValue);
          return;
        case 94094958:  // build
          ((ComponentServer) bean).setBuild((String) newValue);
          return;
        case 230943785:  // buildId
          ((ComponentServer) bean).setBuildId((String) newValue);
          return;
        case 367695400:  // currentInstant
          ((ComponentServer) bean).setCurrentInstant((Instant) newValue);
          return;
        case 1349827208:  // componentInfos
          ((ComponentServer) bean).setComponentInfos((List<ComponentInfo>) newValue);
          return;
      }
      super.propertySet(bean, propertyName, newValue, quiet);
    }

    @Override
    protected void validate(Bean bean) {
      JodaBeanUtils.notNull(((ComponentServer) bean)._uri, "uri");
      JodaBeanUtils.notNull(((ComponentServer) bean)._componentInfos, "componentInfos");
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
