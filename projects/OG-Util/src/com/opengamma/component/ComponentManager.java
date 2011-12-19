/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.component;

import java.util.HashMap;
import java.util.Iterator;
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
 * This class loads and starts the components based on configuration.
 * The end result is a populated {@link ComponentRepository}.
 */
public class ComponentManager {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(ComponentManager.class);

  /**
   * The component repository.
   */
  private final ComponentRepository _repo = new ComponentRepository();

  /**
   * Creates an instance.
   */
  public ComponentManager() {
  }

  //-------------------------------------------------------------------------
  /**
   * Initializes the components based on the specified resource.
   * 
   * @return the repository, not null
   */
  public ComponentRepository getRepository() {
    return _repo;
  }

  //-------------------------------------------------------------------------
  /**
   * Initializes the components based on the specified resource.
   * 
   * @param resource  the config resource to load
   */
  public void start(Resource resource) {
    ComponentConfigLoader loader = new ComponentConfigLoader();
    ComponentConfig config = loader.load(resource, new HashMap<String, String>());
    _repo.pushThreadLocal();
    init(config);
    start();
  }

  //-------------------------------------------------------------------------
  /**
   * Initializes the repository from the config.
   * 
   * @param config  the loaded config, not null
   */
  protected void init(ComponentConfig config) {
    for (String groupName : config.getGroups()) {
      LinkedHashMap<String, String> groupData = config.getGroup(groupName);
      if (groupData.containsKey("factory")) {
        initComponent(groupName, groupData);
      }
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Starts the components.
   */
  protected void start() {
    _repo.start();
  }

  //-------------------------------------------------------------------------
  /**
   * Initialize the component.
   * 
   * @param groupName  the group name, not null
   * @param groupData  the config data, not null
   */
  protected void initComponent(String groupName, LinkedHashMap<String, String> groupData) {
    groupData = new LinkedHashMap<String, String>(groupData);
    String typeStr = groupData.remove("factory");
    s_logger.info("Starting component: {} with properties {}", typeStr, groupData);
    
    // load and init
    ComponentFactory factory = loadFactory(typeStr);
    setFactoryProperties(factory, groupData);
    initFactory(factory, groupData);
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
      for (Iterator<Entry<String, String>> it = groupData.entrySet().iterator(); it.hasNext(); ) {
        Entry<String, String> entry = it.next();
        if (bean.propertyNames().contains(entry.getKey())) {
          MetaProperty<Object> mp = bean.metaBean().metaProperty(entry.getKey());
          setFactoryProperty(bean, mp, entry.getValue());
          it.remove();
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
   * @param groupData  the remaining configuration data, not null
   */
  protected void initFactory(ComponentFactory factory, LinkedHashMap<String, String> groupData) {
    try {
      factory.init(_repo, groupData);
    } catch (Exception ex) {
      throw new OpenGammaRuntimeException(ex.getMessage(), ex);
    }
  }

}
