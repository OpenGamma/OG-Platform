/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.component;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import org.joda.beans.Bean;
import org.joda.beans.MetaProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;

import com.opengamma.OpenGammaRuntimeException;

/**
 * Manages the process of starting OpenGamma components.
 * <p>
 * The OpenGamma logical architecture consists of a set of components.
 * This class loads and starts the components, using a {@link ComponentRepository}.
 */
public class ComponentManager {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(ComponentManager.class);

  /**
   * The component repository.
   */
  private final ComponentRepository _repo = new ComponentRepository();

  /**
   * Initializes the components based on the specified resource.
   * 
   * @param resource  the config resource to load
   */
  public void start(Resource resource) {
    ComponentConfigLoader loader = new ComponentConfigLoader();
    ComponentConfig config = loader.load(resource, new HashMap<String, String>());
    
    _repo.pushThreadLocal();
    for (String group : config.getGroups()) {
      LinkedHashMap<String, String> groupData = config.getGroup(group);
      if (groupData.containsKey("factory")) {
        initComponent(groupData);
      }
    }
    _repo.start();
  }

  /**
   * Initialize the component.
   * 
   * @param groupData  the config data, not null
   */
  protected void initComponent(LinkedHashMap<String, String> groupData) {
    groupData = new LinkedHashMap<String, String>(groupData);
    String typeStr = groupData.remove("factory");
    s_logger.info("Starting component: {} with properties {}", typeStr, groupData);
    
    // load and init
    ComponentFactory factory = loadFactory(typeStr);
    setFactoryProperties(factory, groupData);
    initFactory(factory);
  }

  //-------------------------------------------------------------------------
  /**
   * Loads the factory.
   * A factory should perform minimal work in the constructor.
   * 
   * @param typeStr  the factory type class name, not null
   * @return the factory, not null
   */
  protected ComponentFactory loadFactory(String typeStr) {
    ComponentFactory factory;
    try {
      Class<? extends ComponentFactory> cls = getClass().getClassLoader().loadClass(typeStr).asSubclass(ComponentFactory.class);
      factory = cls.newInstance();
    } catch (ClassNotFoundException ex) {
      throw new IllegalArgumentException("Unknown component factory: " + typeStr, ex);
    } catch (InstantiationException ex) {
      throw new IllegalArgumentException("Unable to create component factory: " + typeStr, ex);
    } catch (IllegalAccessException ex) {
      throw new IllegalArgumentException("Unable to access component factory: " + typeStr, ex);
    }
    return factory;
  }

  //-------------------------------------------------------------------------
  /**
   * Sets the properties on the factory
   * 
   * @param factory  the factory, not null
   * @param groupData  the config data, not null
   */
  protected void setFactoryProperties(ComponentFactory factory, LinkedHashMap<String, String> groupData) {
    if (factory instanceof Bean) {
      Bean bean = (Bean) factory;
      for (Entry<String, String> entry : groupData.entrySet()) {
        if (bean.propertyNames().contains(entry.getKey())) {
          MetaProperty<Object> mp = bean.metaBean().metaProperty(entry.getKey());
          setFactoryProperty(bean, mp, entry.getValue());
        }
      }
    }
  }

  /**
   * Intelligently sets the property.
   * <p>
   * This uses the repository to link properties declared with classifiers to the instance.
   * 
   * @param bean  the bean, not null
   * @param mp  the property, not null
   * @param value  the value
   */
  protected void setFactoryProperty(Bean bean, MetaProperty<Object> mp, String value) {
    Class<?> cls = mp.propertyType();
    if (cls == ComponentRepository.class) {
      // set the repo
      mp.set(bean, _repo);
      
    } else {
      // set property by value type conversion from String
      try {
        mp.setString(bean, value);
        
      } catch (RuntimeException ex) {
        // set property by repo lookup
        try {
          mp.set(bean, _repo.getInstance(cls, value));
        } catch (RuntimeException ex2) {
          throw new IllegalArgumentException("Unable to convert value for " + mp, ex2);
        }
      }
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Initializes the factory.
   * <p>
   * The real work of creating the component and registering it should be done here.
   * The factory may also publish a RESTful view and/or a life-cycle method.
   * 
   * @param factory  the factory to initialize, not null
   */
  protected void initFactory(ComponentFactory factory) {
    try {
      factory.init(_repo);
    } catch (Exception ex) {
      throw new OpenGammaRuntimeException(ex.getMessage(), ex);
    }
  }

}
