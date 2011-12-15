/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.component;

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
   * Starts the components defined in the specified resource.
   * 
   * @param resource  the config resource to load
   */
  public void start(Resource resource) {
    ComponentConfigLoader loader = new ComponentConfigLoader();
    ComponentConfig config = loader.load(resource);
    
    _repo.pushThreadLocal();
    for (String group : config.getGroups()) {
      LinkedHashMap<String, String> groupData = config.getGroup(group);
      if (groupData.containsKey("factory")) {
        startComponent(groupData);
      }
    }
    _repo.ready();
  }

  private void startComponent(LinkedHashMap<String, String> groupData) {
    // create component
    groupData = new LinkedHashMap<String, String>(groupData);
    String typeStr = groupData.remove("factory");
    s_logger.info("Starting component: {} with properties {}", typeStr, groupData);
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
    
    // set properties
    if (factory instanceof Bean) {
      Bean bean = (Bean) factory;
      for (Entry<String, String> entry : groupData.entrySet()) {
        if (bean.propertyNames().contains(entry.getKey())) {
          MetaProperty<Object> mp = bean.metaBean().metaProperty(entry.getKey());
          setProperty(bean, mp, entry.getValue());
        }
      }
    }
    
    // start
    try {
      factory.start(_repo);
    } catch (Exception ex) {
      throw new OpenGammaRuntimeException(ex.getMessage(), ex);
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
  public void setProperty(Bean bean, MetaProperty<Object> mp, String value) {
    try {
      mp.setString(bean, value);
    } catch (RuntimeException ex) {
      Class<?> cls = mp.propertyType();
      Object converted;
      try {
        converted = _repo.getInstance(cls, value);
      } catch (RuntimeException ex2) {
        throw new IllegalArgumentException("Unable to convert value for " + mp, ex2);
      }
      mp.set(bean, converted);
    }
  }

}
