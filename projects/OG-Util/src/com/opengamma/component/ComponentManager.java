/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.component;

import java.util.HashMap;
import java.util.LinkedHashMap;

import org.joda.beans.Bean;
import org.joda.beans.MetaProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.util.PlatformConfigUtils;

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
    initGlobal(config);
    init(config);
    start();
  }

  //-------------------------------------------------------------------------
  protected void initGlobal(ComponentConfig config) {
    LinkedHashMap<String, String> global = config.getGroup("global");
    if (global != null) {
      String runMode = global.get("run.mode");
      String mds = global.get("market.data.source");
      if (runMode != null && mds != null) {
        PlatformConfigUtils.configureSystemProperties(runMode, mds);
      } else if (runMode != null && mds == null) {
        PlatformConfigUtils.configureSystemProperties(runMode);
      }
    }
  }

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
    s_logger.info("Starting repository");
    _repo.start();
  }

  //-------------------------------------------------------------------------
  /**
   * Initialize the component.
   * 
   * @param groupName  the group name, not null
   * @param groupConfig  the config data, not null
   */
  protected void initComponent(String groupName, LinkedHashMap<String, String> groupConfig) {
    LinkedHashMap<String, String> remainingConfig = new LinkedHashMap<String, String>(groupConfig);
    String typeStr = remainingConfig.remove("factory");
    s_logger.info("Initializing component: {} with properties {}", typeStr, remainingConfig);
    
    // load factory
    ComponentFactory factory = loadFactory(typeStr);
    
    // set properties
    try {
      setFactoryProperties(factory, remainingConfig);
    } catch (Exception ex) {
      throw new OpenGammaRuntimeException("Failed to set component factory properties: '" + groupName + "' with " + groupConfig, ex);
    }
    
    // init
    try {
      initFactory(factory, remainingConfig);
    } catch (Exception ex) {
      throw new OpenGammaRuntimeException("Failed to init component factory: '" + groupName + "' with " + groupConfig, ex);
    }
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
   * @param remainingConfig  the config data, not null
   */
  protected void setFactoryProperties(ComponentFactory factory, LinkedHashMap<String, String> remainingConfig) {
    if (factory instanceof Bean) {
      Bean bean = (Bean) factory;
      for (MetaProperty<Object> mp : bean.metaBean().metaPropertyIterable()) {
        String value = remainingConfig.remove(mp.name());
        if (value == null) {
          // set to ensure validated by factory
          mp.set(bean, mp.propertyType().isPrimitive() ? mp.get(bean) : null);
        } else {
          // set value
          setFactoryProperty(bean, mp, value);
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
   * @param value  the value, not null
   */
  protected void setFactoryProperty(Bean bean, MetaProperty<Object> mp, String value) {
    Class<?> propertyType = mp.propertyType();
    if (propertyType == ComponentRepository.class) {
      // set the repo
      mp.set(bean, _repo);
      
    } else {
      // set property by value type conversion from String
      try {
        mp.setString(bean, value);
        
      } catch (RuntimeException ex) {
        // set property by repo lookup
        try {
          mp.set(bean, _repo.getInstance(propertyType, value));
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
   * @param remainingConfig  the remaining configuration data, not null
   * @throws Exception to allow components to throw checked exceptions
   */
  protected void initFactory(ComponentFactory factory, LinkedHashMap<String, String> remainingConfig) throws Exception {
    factory.init(_repo, remainingConfig);
    if (remainingConfig.size() > 0) {
      throw new IllegalStateException("Configuration was specified but not used: " + remainingConfig);
    }
  }

}
