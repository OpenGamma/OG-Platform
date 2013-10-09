/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.component.factory;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.management.MBeanServer;

import org.eclipse.jetty.jmx.MBeanContainer;
import org.eclipse.jetty.plus.jaas.JAASLoginService;
import org.eclipse.jetty.security.ConstraintMapping;
import org.eclipse.jetty.security.ConstraintSecurityHandler;
import org.eclipse.jetty.security.DefaultIdentityService;
import org.eclipse.jetty.security.authentication.BasicAuthenticator;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.util.security.Constraint;
import org.eclipse.jetty.webapp.WebAppContext;
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
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.Resource;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.component.ComponentFactory;
import com.opengamma.component.ComponentRepository;
import com.opengamma.component.rest.RestComponents;
import com.opengamma.transport.jaxrs.FudgeObjectBinaryConsumer;
import com.opengamma.transport.jaxrs.FudgeObjectBinaryProducer;
import com.opengamma.transport.jaxrs.FudgeObjectJSONConsumer;
import com.opengamma.transport.jaxrs.FudgeObjectJSONProducer;
import com.opengamma.transport.jaxrs.FudgeObjectXMLConsumer;
import com.opengamma.transport.jaxrs.FudgeObjectXMLProducer;
import com.opengamma.util.ResourceUtils;
import com.opengamma.util.rest.DataDuplicationExceptionMapper;
import com.opengamma.util.rest.DataNotFoundExceptionMapper;
import com.opengamma.util.rest.IllegalArgumentExceptionMapper;
import com.opengamma.util.rest.ThrowableExceptionMapper;
import com.opengamma.util.rest.UnsupportedOperationExceptionMapper;
import com.opengamma.util.rest.WebApplicationExceptionMapper;

/**
 * Component definition for the Jetty server defined in Spring.
 * <p>
 * This reads a Spring file to start the Jetty server.
 */
@BeanDefinition
public class SpringJettyComponentFactory extends AbstractSpringComponentFactory implements ComponentFactory {

  private static final String AUTH_LOGIN_CONFIG_PROPERTY = "java.security.auth.login.config";
  private static final String DEFAULT_LOGIN_CONFIG = "classpath:og.login.conf";
  
  /**
   * The flag indicating if the component is active.
   * This can be used from configuration to disable the Jetty server.
   * True by default.
   */
  @PropertyDefinition
  private boolean _active = true;
  /**
   * The flag indicating whether to enable authentication.
   */
  @PropertyDefinition
  private boolean _requireAuthentication;
  /**
   * The login configuration file to set.
   */
  @PropertyDefinition
  private String _loginConfig = DEFAULT_LOGIN_CONFIG;

  //-------------------------------------------------------------------------
  @Override
  public void init(ComponentRepository repo, LinkedHashMap<String, String> configuration) throws Exception {
    if (isActive() == false) {
      return;
    }
    
    GenericApplicationContext appContext = createApplicationContext(repo);
    
    String[] beanNames = appContext.getBeanNamesForType(Server.class);
    if (beanNames.length != 1) {
      throw new IllegalStateException("Expected 1 Jetty server, but found " + beanNames.length);
    }
    Server server = appContext.getBean(beanNames[0], Server.class);
    
    if (isRequireAuthentication()) {
      configureAuthentication(repo, server);
    }
    
    repo.registerComponent(Server.class, "jetty", server);
    repo.registerLifecycle(new ServerLifecycle(server));
    
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

  private void configureAuthentication(ComponentRepository repo, Server server) throws IOException {
    if (System.getProperty(AUTH_LOGIN_CONFIG_PROPERTY) == null) {
      Resource loginConfigResource = ResourceUtils.createResource(getLoginConfig());
      if (loginConfigResource.getFile() == null) {
        throw new IllegalArgumentException("Unable to find login config resource: " + getLoginConfig());
      }
      System.setProperty(AUTH_LOGIN_CONFIG_PROPERTY, loginConfigResource.getFile().getPath());
    }
    
    Constraint userConstraint = new Constraint();
    userConstraint.setRoles(new String[] {"user"});
    userConstraint.setAuthenticate(true);
    ConstraintMapping restrictedConstraintMapping = new ConstraintMapping();
    restrictedConstraintMapping.setConstraint(userConstraint);
    restrictedConstraintMapping.setPathSpec("/*");
    
    Constraint noAuthenticationConstraint = new Constraint();
    noAuthenticationConstraint.setAuthenticate(false);
    ConstraintMapping publicConstraintMapping = new ConstraintMapping();
    publicConstraintMapping.setConstraint(noAuthenticationConstraint);
    publicConstraintMapping.setPathSpec("/jax/bundles/fm/prototype/login.ftl");
    
    JAASLoginService loginService = new JAASLoginService("OpenGamma");
    loginService.setLoginModuleName("og");
    server.addBean(loginService);

    ConstraintSecurityHandler securityHandler = new ConstraintSecurityHandler();
    securityHandler.addConstraintMapping(restrictedConstraintMapping);
    securityHandler.addConstraintMapping(publicConstraintMapping);
    securityHandler.setAuthenticator(new BasicAuthenticator());
    securityHandler.setLoginService(loginService);
    securityHandler.setIdentityService(new DefaultIdentityService());
    securityHandler.setStrict(false);
    securityHandler.setRealmName("OpenGamma");
    server.addBean(securityHandler);

    // Insert the security handler in the chain before the existing handler
    securityHandler.setHandler(server.getHandler());
    server.setHandler(securityHandler);
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
    restComponents.publishHelper(new DataNotFoundExceptionMapper());
    restComponents.publishHelper(new DataDuplicationExceptionMapper());
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
   * The meta-bean for {@code SpringJettyComponentFactory}.
   * @return the meta-bean, not null
   */
  public static SpringJettyComponentFactory.Meta meta() {
    return SpringJettyComponentFactory.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(SpringJettyComponentFactory.Meta.INSTANCE);
  }

  @Override
  public SpringJettyComponentFactory.Meta metaBean() {
    return SpringJettyComponentFactory.Meta.INSTANCE;
  }

  @Override
  protected Object propertyGet(String propertyName, boolean quiet) {
    switch (propertyName.hashCode()) {
      case -1422950650:  // active
        return isActive();
      case 2012797757:  // requireAuthentication
        return isRequireAuthentication();
      case 852061195:  // loginConfig
        return getLoginConfig();
    }
    return super.propertyGet(propertyName, quiet);
  }

  @Override
  protected void propertySet(String propertyName, Object newValue, boolean quiet) {
    switch (propertyName.hashCode()) {
      case -1422950650:  // active
        setActive((Boolean) newValue);
        return;
      case 2012797757:  // requireAuthentication
        setRequireAuthentication((Boolean) newValue);
        return;
      case 852061195:  // loginConfig
        setLoginConfig((String) newValue);
        return;
    }
    super.propertySet(propertyName, newValue, quiet);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      SpringJettyComponentFactory other = (SpringJettyComponentFactory) obj;
      return JodaBeanUtils.equal(isActive(), other.isActive()) &&
          JodaBeanUtils.equal(isRequireAuthentication(), other.isRequireAuthentication()) &&
          JodaBeanUtils.equal(getLoginConfig(), other.getLoginConfig()) &&
          super.equals(obj);
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash += hash * 31 + JodaBeanUtils.hashCode(isActive());
    hash += hash * 31 + JodaBeanUtils.hashCode(isRequireAuthentication());
    hash += hash * 31 + JodaBeanUtils.hashCode(getLoginConfig());
    return hash ^ super.hashCode();
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
   * Gets the flag indicating whether to enable authentication.
   * @return the value of the property
   */
  public boolean isRequireAuthentication() {
    return _requireAuthentication;
  }

  /**
   * Sets the flag indicating whether to enable authentication.
   * @param requireAuthentication  the new value of the property
   */
  public void setRequireAuthentication(boolean requireAuthentication) {
    this._requireAuthentication = requireAuthentication;
  }

  /**
   * Gets the the {@code requireAuthentication} property.
   * @return the property, not null
   */
  public final Property<Boolean> requireAuthentication() {
    return metaBean().requireAuthentication().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the login configuration file to set.
   * @return the value of the property
   */
  public String getLoginConfig() {
    return _loginConfig;
  }

  /**
   * Sets the login configuration file to set.
   * @param loginConfig  the new value of the property
   */
  public void setLoginConfig(String loginConfig) {
    this._loginConfig = loginConfig;
  }

  /**
   * Gets the the {@code loginConfig} property.
   * @return the property, not null
   */
  public final Property<String> loginConfig() {
    return metaBean().loginConfig().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code SpringJettyComponentFactory}.
   */
  public static class Meta extends AbstractSpringComponentFactory.Meta {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code active} property.
     */
    private final MetaProperty<Boolean> _active = DirectMetaProperty.ofReadWrite(
        this, "active", SpringJettyComponentFactory.class, Boolean.TYPE);
    /**
     * The meta-property for the {@code requireAuthentication} property.
     */
    private final MetaProperty<Boolean> _requireAuthentication = DirectMetaProperty.ofReadWrite(
        this, "requireAuthentication", SpringJettyComponentFactory.class, Boolean.TYPE);
    /**
     * The meta-property for the {@code loginConfig} property.
     */
    private final MetaProperty<String> _loginConfig = DirectMetaProperty.ofReadWrite(
        this, "loginConfig", SpringJettyComponentFactory.class, String.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, (DirectMetaPropertyMap) super.metaPropertyMap(),
        "active",
        "requireAuthentication",
        "loginConfig");

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
        case 2012797757:  // requireAuthentication
          return _requireAuthentication;
        case 852061195:  // loginConfig
          return _loginConfig;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends SpringJettyComponentFactory> builder() {
      return new DirectBeanBuilder<SpringJettyComponentFactory>(new SpringJettyComponentFactory());
    }

    @Override
    public Class<? extends SpringJettyComponentFactory> beanType() {
      return SpringJettyComponentFactory.class;
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
     * The meta-property for the {@code requireAuthentication} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Boolean> requireAuthentication() {
      return _requireAuthentication;
    }

    /**
     * The meta-property for the {@code loginConfig} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> loginConfig() {
      return _loginConfig;
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
