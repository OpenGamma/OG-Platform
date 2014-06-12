/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.component.factory;

import java.util.ArrayList;
import java.util.EventListener;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.management.MBeanServer;

import org.eclipse.jetty.jmx.MBeanContainer;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.util.resource.ResourceCollection;
import org.eclipse.jetty.webapp.WebAppContext;
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
import org.springframework.context.Lifecycle;
import org.springframework.core.io.Resource;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.component.ComponentInfo;
import com.opengamma.component.ComponentRepository;
import com.opengamma.component.rest.ComponentRepositoryServletContextListener;
import com.opengamma.component.rest.PlainTextErrorHandler;
import com.opengamma.component.rest.RestComponents;
import com.opengamma.transport.jaxrs.FudgeObjectBinaryConsumer;
import com.opengamma.transport.jaxrs.FudgeObjectBinaryProducer;
import com.opengamma.transport.jaxrs.FudgeObjectJSONConsumer;
import com.opengamma.transport.jaxrs.FudgeObjectJSONProducer;
import com.opengamma.transport.jaxrs.FudgeObjectXMLConsumer;
import com.opengamma.transport.jaxrs.FudgeObjectXMLProducer;
import com.opengamma.transport.jaxrs.JodaBeanBinaryProducerConsumer;
import com.opengamma.transport.jaxrs.JodaBeanXmlProducerConsumer;
import com.opengamma.util.rest.AuthorizationExceptionMapper;
import com.opengamma.util.rest.DataDuplicationExceptionMapper;
import com.opengamma.util.rest.DataNotFoundExceptionMapper;
import com.opengamma.util.rest.DataVersionExceptionMapper;
import com.opengamma.util.rest.IllegalArgumentExceptionMapper;
import com.opengamma.util.rest.ThrowableExceptionMapper;
import com.opengamma.util.rest.UnsupportedOperationExceptionMapper;
import com.opengamma.util.rest.WebApplicationExceptionMapper;

/**
 * Component factory for the embedded Jetty server.
 */
@BeanDefinition
public class EmbeddedJettyComponentFactory extends AbstractComponentFactory {

  /**
   * The flag indicating if the component is active.
   * This can be used from configuration to disable the Jetty server.
   * True by default.
   */
  @PropertyDefinition
  private boolean _active = true;
  /**
   * The port on which Jetty listens for HTTP requests.
   */
  @PropertyDefinition
  private int _port = 8080;
  /**
   * The port on which Jetty listens for HTTPS requests.
   */
  @PropertyDefinition
  private int _securePort = 8443;
  /**
   * The Jetty resource base.
   */
  @PropertyDefinition(validate = "notNull")
  private Resource _resourceBase;
  /**
   * The location for alternate resources.
   */
  @PropertyDefinition
  private final List<Resource> _secondaryResourceBases = new ArrayList<>();

  //-------------------------------------------------------------------------
  @Override
  public void init(ComponentRepository repo, LinkedHashMap<String, String> configuration) throws Exception {
    if (isActive() == false) {
      return;
    }
    
    Server server = initJettyServer(repo);
    
    // JMX
    final MBeanServer jmxServer = repo.findInstance(MBeanServer.class);
    if (jmxServer != null) {
      MBeanContainer jettyJmx = new MBeanContainer(jmxServer);
      server.getContainer().addEventListener(jettyJmx);
      server.addBean(jettyJmx);
    }
    // basic RESTful helpers
    registerJettyRestBasics(repo);
  }

  private Server initJettyServer(ComponentRepository repo) {
    SelectChannelConnector connector = new SelectChannelConnector();
    connector.setPort(getPort());
    connector.setConfidentialPort(getSecurePort());
    connector.setRequestHeaderSize(16384);

    Server jettyServer = new Server();
    jettyServer.setConnectors(new Connector[] {connector});
    
    ContextHandlerCollection contexts = new ContextHandlerCollection();
    HandlerCollection handlers = new HandlerCollection();
    handlers.addHandler(contexts);
    addHandlers(repo, jettyServer, contexts);
    
    jettyServer.setHandler(handlers);
    jettyServer.setStopAtShutdown(true);
    jettyServer.setGracefulShutdown(2000);
    jettyServer.setSendDateHeader(true);
    jettyServer.setSendServerVersion(true);
    
    ComponentInfo info = new ComponentInfo(Server.class, "jetty");
    repo.registerComponent(info, jettyServer);
    repo.registerLifecycle(new ServerLifecycle(jettyServer));
    return jettyServer;
  }

  /**
   * Adds handlers to the set of Jetty handlers.
   * <p>
   * This adds the webapp context using {@link #createWebAppContext}.
   * 
   * @param repo  the component repository, not null
   * @param jettyServer  the Jetty server instance, not null
   * @param handlers  the set of handlers to add to, not null
   */
  protected void addHandlers(ComponentRepository repo, Server jettyServer, ContextHandlerCollection handlers) {
    handlers.addHandler(createWebAppContext(repo, "OpenGamma", "/"));
  }

  /**
   * Creates the webapp context, merging base resources if defined.
   * 
   * @param repo  the component repository, not null
   * @param name  the webapp name, not null
   * @param contextPath  the context path to use as the base, not null
   * @return the webapp context, not null
   */
  protected WebAppContext createWebAppContext(ComponentRepository repo, String name, String contextPath) {
    WebAppContext ogWebAppContext = new WebAppContext(name, contextPath);
    ogWebAppContext.setParentLoaderPriority(true);
    ogWebAppContext.setBaseResource(createJettyResource());
    ogWebAppContext.setDisplayName(getResourceBase().getDescription());
    ogWebAppContext.setErrorHandler(new PlainTextErrorHandler());
    ogWebAppContext.setEventListeners(new EventListener[] {new ComponentRepositoryServletContextListener(repo)});
    return ogWebAppContext;
  }

  /**
   * Converts the configured Spring resources to a combined Jetty resource, handling exceptions.
   * <p>
   * The result combines the base and any secondary resources.
   * 
   * @return the Jetty resource base, not null
   */
  protected org.eclipse.jetty.util.resource.Resource createJettyResource() {
    org.eclipse.jetty.util.resource.Resource baseResource = createJettyResource(getResourceBase());
    if (getSecondaryResourceBases().size() > 0) {
      List<org.eclipse.jetty.util.resource.Resource> resources = new ArrayList<>();
      resources.add(baseResource);
      for (Resource resource : getSecondaryResourceBases()) {
        resources.add(createJettyResource(resource));
      }
      baseResource = new ResourceCollection((org.eclipse.jetty.util.resource.Resource[]) resources.toArray(new org.eclipse.jetty.util.resource.Resource[resources.size()]));
    }
    return baseResource;
  }

  /**
   * Converts a Spring resource to a Jetty resource, handling exceptions.
   * 
   * @param resource  the resource to convert, not null
   * @return the Jetty resource, not null
   */
  protected org.eclipse.jetty.util.resource.Resource createJettyResource(Resource resource) {
    try {
      return org.eclipse.jetty.util.resource.Resource.newResource(resource.getFile());
    } catch (Exception ex) {
      throw new OpenGammaRuntimeException("Unable to find resource for Jetty: " + resource, ex);
    }
  }

  /**
   * Registers the basic RESTful helpers.
   * 
   * @param repo  the component repository, not null
   */
  protected void registerJettyRestBasics(ComponentRepository repo) {
    RestComponents restComponents = repo.getRestComponents();
    restComponents.publishHelper(new FudgeObjectJSONConsumer());
    restComponents.publishHelper(new FudgeObjectJSONProducer());
    restComponents.publishHelper(new FudgeObjectXMLConsumer());
    restComponents.publishHelper(new FudgeObjectXMLProducer());
    restComponents.publishHelper(new FudgeObjectBinaryConsumer());
    restComponents.publishHelper(new FudgeObjectBinaryProducer());
    restComponents.publishHelper(new JodaBeanBinaryProducerConsumer());
    restComponents.publishHelper(new JodaBeanXmlProducerConsumer());
    restComponents.publishHelper(new AuthorizationExceptionMapper());
    restComponents.publishHelper(new DataNotFoundExceptionMapper());
    restComponents.publishHelper(new DataDuplicationExceptionMapper());
    restComponents.publishHelper(new DataVersionExceptionMapper());
    restComponents.publishHelper(new IllegalArgumentExceptionMapper());
    restComponents.publishHelper(new UnsupportedOperationExceptionMapper());
    restComponents.publishHelper(new WebApplicationExceptionMapper());
    restComponents.publishHelper(new ThrowableExceptionMapper());
  }

  //-------------------------------------------------------------------------
  /**
   * Wraps Jetty LifeCycle in Spring's Lifecycle.
   */
  static class ServerLifecycle implements Lifecycle {
    private final Server _server;

    public ServerLifecycle(Server server) {
      _server = server;
    }

    @Override
    public void start() {
      try {
        _server.start();
      } catch (Exception ex) {
        throw new OpenGammaRuntimeException(ex.getMessage(), ex);
      }
    }

    @Override
    public void stop() {
      try {
        _server.stop();
      } catch (Exception ex) {
        throw new OpenGammaRuntimeException(ex.getMessage(), ex);
      }
    }

    @Override
    public boolean isRunning() {
      return _server.isStarted();
    }
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code EmbeddedJettyComponentFactory}.
   * @return the meta-bean, not null
   */
  public static EmbeddedJettyComponentFactory.Meta meta() {
    return EmbeddedJettyComponentFactory.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(EmbeddedJettyComponentFactory.Meta.INSTANCE);
  }

  @Override
  public EmbeddedJettyComponentFactory.Meta metaBean() {
    return EmbeddedJettyComponentFactory.Meta.INSTANCE;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the flag indicating if the component is active.
   * This can be used from configuration to disable the Jetty server.
   * True by default.
   * @return the value of the property
   */
  public boolean isActive() {
    return _active;
  }

  /**
   * Sets the flag indicating if the component is active.
   * This can be used from configuration to disable the Jetty server.
   * True by default.
   * @param active  the new value of the property
   */
  public void setActive(boolean active) {
    this._active = active;
  }

  /**
   * Gets the the {@code active} property.
   * This can be used from configuration to disable the Jetty server.
   * True by default.
   * @return the property, not null
   */
  public final Property<Boolean> active() {
    return metaBean().active().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the port on which Jetty listens for HTTP requests.
   * @return the value of the property
   */
  public int getPort() {
    return _port;
  }

  /**
   * Sets the port on which Jetty listens for HTTP requests.
   * @param port  the new value of the property
   */
  public void setPort(int port) {
    this._port = port;
  }

  /**
   * Gets the the {@code port} property.
   * @return the property, not null
   */
  public final Property<Integer> port() {
    return metaBean().port().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the port on which Jetty listens for HTTPS requests.
   * @return the value of the property
   */
  public int getSecurePort() {
    return _securePort;
  }

  /**
   * Sets the port on which Jetty listens for HTTPS requests.
   * @param securePort  the new value of the property
   */
  public void setSecurePort(int securePort) {
    this._securePort = securePort;
  }

  /**
   * Gets the the {@code securePort} property.
   * @return the property, not null
   */
  public final Property<Integer> securePort() {
    return metaBean().securePort().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the Jetty resource base.
   * @return the value of the property, not null
   */
  public Resource getResourceBase() {
    return _resourceBase;
  }

  /**
   * Sets the Jetty resource base.
   * @param resourceBase  the new value of the property, not null
   */
  public void setResourceBase(Resource resourceBase) {
    JodaBeanUtils.notNull(resourceBase, "resourceBase");
    this._resourceBase = resourceBase;
  }

  /**
   * Gets the the {@code resourceBase} property.
   * @return the property, not null
   */
  public final Property<Resource> resourceBase() {
    return metaBean().resourceBase().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the location for alternate resources.
   * @return the value of the property, not null
   */
  public List<Resource> getSecondaryResourceBases() {
    return _secondaryResourceBases;
  }

  /**
   * Sets the location for alternate resources.
   * @param secondaryResourceBases  the new value of the property, not null
   */
  public void setSecondaryResourceBases(List<Resource> secondaryResourceBases) {
    JodaBeanUtils.notNull(secondaryResourceBases, "secondaryResourceBases");
    this._secondaryResourceBases.clear();
    this._secondaryResourceBases.addAll(secondaryResourceBases);
  }

  /**
   * Gets the the {@code secondaryResourceBases} property.
   * @return the property, not null
   */
  public final Property<List<Resource>> secondaryResourceBases() {
    return metaBean().secondaryResourceBases().createProperty(this);
  }

  //-----------------------------------------------------------------------
  @Override
  public EmbeddedJettyComponentFactory clone() {
    return JodaBeanUtils.cloneAlways(this);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      EmbeddedJettyComponentFactory other = (EmbeddedJettyComponentFactory) obj;
      return (isActive() == other.isActive()) &&
          (getPort() == other.getPort()) &&
          (getSecurePort() == other.getSecurePort()) &&
          JodaBeanUtils.equal(getResourceBase(), other.getResourceBase()) &&
          JodaBeanUtils.equal(getSecondaryResourceBases(), other.getSecondaryResourceBases()) &&
          super.equals(obj);
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash += hash * 31 + JodaBeanUtils.hashCode(isActive());
    hash += hash * 31 + JodaBeanUtils.hashCode(getPort());
    hash += hash * 31 + JodaBeanUtils.hashCode(getSecurePort());
    hash += hash * 31 + JodaBeanUtils.hashCode(getResourceBase());
    hash += hash * 31 + JodaBeanUtils.hashCode(getSecondaryResourceBases());
    return hash ^ super.hashCode();
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(192);
    buf.append("EmbeddedJettyComponentFactory{");
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
    buf.append("active").append('=').append(JodaBeanUtils.toString(isActive())).append(',').append(' ');
    buf.append("port").append('=').append(JodaBeanUtils.toString(getPort())).append(',').append(' ');
    buf.append("securePort").append('=').append(JodaBeanUtils.toString(getSecurePort())).append(',').append(' ');
    buf.append("resourceBase").append('=').append(JodaBeanUtils.toString(getResourceBase())).append(',').append(' ');
    buf.append("secondaryResourceBases").append('=').append(JodaBeanUtils.toString(getSecondaryResourceBases())).append(',').append(' ');
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code EmbeddedJettyComponentFactory}.
   */
  public static class Meta extends AbstractComponentFactory.Meta {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code active} property.
     */
    private final MetaProperty<Boolean> _active = DirectMetaProperty.ofReadWrite(
        this, "active", EmbeddedJettyComponentFactory.class, Boolean.TYPE);
    /**
     * The meta-property for the {@code port} property.
     */
    private final MetaProperty<Integer> _port = DirectMetaProperty.ofReadWrite(
        this, "port", EmbeddedJettyComponentFactory.class, Integer.TYPE);
    /**
     * The meta-property for the {@code securePort} property.
     */
    private final MetaProperty<Integer> _securePort = DirectMetaProperty.ofReadWrite(
        this, "securePort", EmbeddedJettyComponentFactory.class, Integer.TYPE);
    /**
     * The meta-property for the {@code resourceBase} property.
     */
    private final MetaProperty<Resource> _resourceBase = DirectMetaProperty.ofReadWrite(
        this, "resourceBase", EmbeddedJettyComponentFactory.class, Resource.class);
    /**
     * The meta-property for the {@code secondaryResourceBases} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<List<Resource>> _secondaryResourceBases = DirectMetaProperty.ofReadWrite(
        this, "secondaryResourceBases", EmbeddedJettyComponentFactory.class, (Class) List.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, (DirectMetaPropertyMap) super.metaPropertyMap(),
        "active",
        "port",
        "securePort",
        "resourceBase",
        "secondaryResourceBases");

    /**
     * Restricted constructor.
     */
    protected Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case -1422950650:  // active
          return _active;
        case 3446913:  // port
          return _port;
        case 1569248408:  // securePort
          return _securePort;
        case -384923649:  // resourceBase
          return _resourceBase;
        case -971230400:  // secondaryResourceBases
          return _secondaryResourceBases;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends EmbeddedJettyComponentFactory> builder() {
      return new DirectBeanBuilder<EmbeddedJettyComponentFactory>(new EmbeddedJettyComponentFactory());
    }

    @Override
    public Class<? extends EmbeddedJettyComponentFactory> beanType() {
      return EmbeddedJettyComponentFactory.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return _metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code active} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Boolean> active() {
      return _active;
    }

    /**
     * The meta-property for the {@code port} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Integer> port() {
      return _port;
    }

    /**
     * The meta-property for the {@code securePort} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Integer> securePort() {
      return _securePort;
    }

    /**
     * The meta-property for the {@code resourceBase} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Resource> resourceBase() {
      return _resourceBase;
    }

    /**
     * The meta-property for the {@code secondaryResourceBases} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<List<Resource>> secondaryResourceBases() {
      return _secondaryResourceBases;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -1422950650:  // active
          return ((EmbeddedJettyComponentFactory) bean).isActive();
        case 3446913:  // port
          return ((EmbeddedJettyComponentFactory) bean).getPort();
        case 1569248408:  // securePort
          return ((EmbeddedJettyComponentFactory) bean).getSecurePort();
        case -384923649:  // resourceBase
          return ((EmbeddedJettyComponentFactory) bean).getResourceBase();
        case -971230400:  // secondaryResourceBases
          return ((EmbeddedJettyComponentFactory) bean).getSecondaryResourceBases();
      }
      return super.propertyGet(bean, propertyName, quiet);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -1422950650:  // active
          ((EmbeddedJettyComponentFactory) bean).setActive((Boolean) newValue);
          return;
        case 3446913:  // port
          ((EmbeddedJettyComponentFactory) bean).setPort((Integer) newValue);
          return;
        case 1569248408:  // securePort
          ((EmbeddedJettyComponentFactory) bean).setSecurePort((Integer) newValue);
          return;
        case -384923649:  // resourceBase
          ((EmbeddedJettyComponentFactory) bean).setResourceBase((Resource) newValue);
          return;
        case -971230400:  // secondaryResourceBases
          ((EmbeddedJettyComponentFactory) bean).setSecondaryResourceBases((List<Resource>) newValue);
          return;
      }
      super.propertySet(bean, propertyName, newValue, quiet);
    }

    @Override
    protected void validate(Bean bean) {
      JodaBeanUtils.notNull(((EmbeddedJettyComponentFactory) bean)._resourceBase, "resourceBase");
      JodaBeanUtils.notNull(((EmbeddedJettyComponentFactory) bean)._secondaryResourceBases, "secondaryResourceBases");
      super.validate(bean);
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
