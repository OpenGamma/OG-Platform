/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.component.factory;

import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.joda.beans.Bean;
import org.joda.beans.BeanBuilder;
import org.joda.beans.BeanDefinition;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaProperty;
import org.joda.beans.Property;
import org.joda.beans.PropertyDefinition;
import org.joda.beans.impl.direct.DirectBean;
import org.joda.beans.impl.direct.DirectMetaBean;
import org.joda.beans.impl.direct.DirectMetaProperty;
import org.joda.beans.impl.direct.DirectMetaPropertyMap;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.ResourceEntityResolver;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.Resource;

import com.opengamma.component.ComponentFactory;
import com.opengamma.component.ComponentRepository;
import com.opengamma.component.spring.ComponentRepositoryBeanPostProcessor;

/**
 * Base factory for reading components from a Spring file.
 * <p>
 * The component is configured with a Spring XML file and a single properties file.
 * This class contains the tools to read the file, parse it and extract components.
 * <p>
 * The Spring file may use {@link ComponentRepositoryBeanPostProcessor} to pull the
 * components into the Spring context.
 */
@BeanDefinition
public abstract class AbstractSpringComponentFactory extends DirectBean implements ComponentFactory {

  /**
   * The config for the Spring file.
   */
  @PropertyDefinition
  private Resource _springFile;
  /**
   * The config for the properties file.
   */
  @PropertyDefinition
  private Resource _propertiesFile;

  //-------------------------------------------------------------------------
  /**
   * Creates the application context.
   * 
   * @param repo  the component repository, not null
   * @return the Spring application context, not null
   */
  protected GenericApplicationContext createApplicationContext(ComponentRepository repo) {
    Resource springFile = getSpringFile();
    try {
      repo.getLogger().logDebug("  Spring file: " + springFile.getURI());
    } catch (Exception ex) {
      // ignore
    }
    
    DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
    GenericApplicationContext appContext = new GenericApplicationContext(beanFactory);
    
    PropertyPlaceholderConfigurer properties = new PropertyPlaceholderConfigurer();
    properties.setLocation(getPropertiesFile());
    
    XmlBeanDefinitionReader beanDefinitionReader = new XmlBeanDefinitionReader(beanFactory);
    beanDefinitionReader.setValidating(true);
    beanDefinitionReader.setResourceLoader(appContext);
    beanDefinitionReader.setEntityResolver(new ResourceEntityResolver(appContext));
    beanDefinitionReader.loadBeanDefinitions(springFile);
    
    appContext.getBeanFactory().registerSingleton("injectedProperties", properties);
    appContext.getBeanFactory().registerSingleton("componentRepository", repo);
    
    appContext.refresh();
    return appContext;
  }

  /**
   * Registers a set of beans by type.
   * 
   * @param <T> the type
   * @param repo  the repository to register in, not null
   * @param type  the type of bean to extract from the Spring context, not null
   * @param appContext  the Spring context, not null
   */
  protected <T> void registerInfrastructureByType(ComponentRepository repo, Class<T> type, GenericApplicationContext appContext) {
    String[] beanNames = appContext.getBeanNamesForType(type);
    for (String beanName : beanNames) {
      T bean = appContext.getBean(beanName, type);
      String name = simplifyName(type, beanName);
      repo.registerComponent(type, name, bean);
      String[] aliases = appContext.getAliases(beanName);
      for (String alias : aliases) {
        name = simplifyName(type, alias);
        repo.registerComponent(type, name, bean);
      }
    }
  }

  /**
   * Simplifies the name of a Spring bean.
   * <p>
   * This removes the end of the bean name if it matches the class name.
   * 
   * @param type  the bean type, not null
   * @param springBeanName  the Spring bean name, not null
   * @return the simplified name, not null
   */
  protected String simplifyName(Class<?> type, String springBeanName) {
    return StringUtils.chomp(springBeanName, type.getSimpleName());
  }

  /**
   * Registers the spring context to be stopped at the end of the application.
   * <p>
   * This will call {@link ConfigurableListableBeanFactory#destroySingletons()}
   * at the end of the application.
   * 
   * @param repo  the repository to register in, not null
   * @param appContext  the Spring context, not null
   */
  protected void registerSpringLifecycleStop(ComponentRepository repo, GenericApplicationContext appContext) {
    repo.registerLifecycleStop(appContext.getBeanFactory(), "destroySingletons");
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code AbstractSpringComponentFactory}.
   * @return the meta-bean, not null
   */
  public static AbstractSpringComponentFactory.Meta meta() {
    return AbstractSpringComponentFactory.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(AbstractSpringComponentFactory.Meta.INSTANCE);
  }

  @Override
  public AbstractSpringComponentFactory.Meta metaBean() {
    return AbstractSpringComponentFactory.Meta.INSTANCE;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the config for the Spring file.
   * @return the value of the property
   */
  public Resource getSpringFile() {
    return _springFile;
  }

  /**
   * Sets the config for the Spring file.
   * @param springFile  the new value of the property
   */
  public void setSpringFile(Resource springFile) {
    this._springFile = springFile;
  }

  /**
   * Gets the the {@code springFile} property.
   * @return the property, not null
   */
  public final Property<Resource> springFile() {
    return metaBean().springFile().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the config for the properties file.
   * @return the value of the property
   */
  public Resource getPropertiesFile() {
    return _propertiesFile;
  }

  /**
   * Sets the config for the properties file.
   * @param propertiesFile  the new value of the property
   */
  public void setPropertiesFile(Resource propertiesFile) {
    this._propertiesFile = propertiesFile;
  }

  /**
   * Gets the the {@code propertiesFile} property.
   * @return the property, not null
   */
  public final Property<Resource> propertiesFile() {
    return metaBean().propertiesFile().createProperty(this);
  }

  //-----------------------------------------------------------------------
  @Override
  public AbstractSpringComponentFactory clone() {
    return JodaBeanUtils.cloneAlways(this);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      AbstractSpringComponentFactory other = (AbstractSpringComponentFactory) obj;
      return JodaBeanUtils.equal(getSpringFile(), other.getSpringFile()) &&
          JodaBeanUtils.equal(getPropertiesFile(), other.getPropertiesFile());
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = getClass().hashCode();
    hash = hash * 31 + JodaBeanUtils.hashCode(getSpringFile());
    hash = hash * 31 + JodaBeanUtils.hashCode(getPropertiesFile());
    return hash;
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(96);
    buf.append("AbstractSpringComponentFactory{");
    int len = buf.length();
    toString(buf);
    if (buf.length() > len) {
      buf.setLength(buf.length() - 2);
    }
    buf.append('}');
    return buf.toString();
  }

  protected void toString(StringBuilder buf) {
    buf.append("springFile").append('=').append(JodaBeanUtils.toString(getSpringFile())).append(',').append(' ');
    buf.append("propertiesFile").append('=').append(JodaBeanUtils.toString(getPropertiesFile())).append(',').append(' ');
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code AbstractSpringComponentFactory}.
   */
  public static class Meta extends DirectMetaBean {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code springFile} property.
     */
    private final MetaProperty<Resource> _springFile = DirectMetaProperty.ofReadWrite(
        this, "springFile", AbstractSpringComponentFactory.class, Resource.class);
    /**
     * The meta-property for the {@code propertiesFile} property.
     */
    private final MetaProperty<Resource> _propertiesFile = DirectMetaProperty.ofReadWrite(
        this, "propertiesFile", AbstractSpringComponentFactory.class, Resource.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, null,
        "springFile",
        "propertiesFile");

    /**
     * Restricted constructor.
     */
    protected Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case 1361354025:  // springFile
          return _springFile;
        case 1613702479:  // propertiesFile
          return _propertiesFile;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends AbstractSpringComponentFactory> builder() {
      throw new UnsupportedOperationException("AbstractSpringComponentFactory is an abstract class");
    }

    @Override
    public Class<? extends AbstractSpringComponentFactory> beanType() {
      return AbstractSpringComponentFactory.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return _metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code springFile} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Resource> springFile() {
      return _springFile;
    }

    /**
     * The meta-property for the {@code propertiesFile} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Resource> propertiesFile() {
      return _propertiesFile;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case 1361354025:  // springFile
          return ((AbstractSpringComponentFactory) bean).getSpringFile();
        case 1613702479:  // propertiesFile
          return ((AbstractSpringComponentFactory) bean).getPropertiesFile();
      }
      return super.propertyGet(bean, propertyName, quiet);
    }

    @Override
    protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {
      switch (propertyName.hashCode()) {
        case 1361354025:  // springFile
          ((AbstractSpringComponentFactory) bean).setSpringFile((Resource) newValue);
          return;
        case 1613702479:  // propertiesFile
          ((AbstractSpringComponentFactory) bean).setPropertiesFile((Resource) newValue);
          return;
      }
      super.propertySet(bean, propertyName, newValue, quiet);
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
