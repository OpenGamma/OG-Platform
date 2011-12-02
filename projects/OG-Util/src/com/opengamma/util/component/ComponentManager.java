/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.component;

import java.util.LinkedHashMap;
import java.util.Map.Entry;

import org.joda.beans.Bean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;

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
   * Starts the components defined in the specified resource.
   * 
   * @param resource  the config resource to load
   */
  public void start(Resource resource) {
    ComponentConfigLoader loader = new ComponentConfigLoader();
    ComponentConfig config = loader.load(resource);
    
    ComponentRepository repo = new ComponentRepository();
    repo.pushThreadLocal();
    
    for (String group : config.getGroups()) {
      LinkedHashMap<String, String> groupData = config.getGroup(group);
      if (groupData.containsKey("component")) {
        startComponent(groupData, repo);
      }
    }
  }

  private void startComponent(LinkedHashMap<String, String> groupData, ComponentRepository repo) {
    // create component
    groupData = new LinkedHashMap<String, String>(groupData);
    String typeStr = groupData.remove("component");
    s_logger.info("Starting component: {} with properties {}", typeStr, groupData);
    Component component;
    try {
      component = getClass().getClassLoader().loadClass(typeStr).asSubclass(Component.class).newInstance();
    } catch (ClassNotFoundException ex) {
      throw new IllegalArgumentException("Unknown component: " + typeStr, ex);
    } catch (InstantiationException ex) {
      throw new IllegalArgumentException("Unable to create component: " + typeStr, ex);
    } catch (IllegalAccessException ex) {
      throw new IllegalArgumentException("Unable to create component: " + typeStr, ex);
    }
    
    // set properties
    if (component instanceof Bean) {
      Bean bean = (Bean) component;
      for (Entry<String, String> entry : groupData.entrySet()) {
        if (bean.propertyNames().contains(entry.getKey())) {
          bean.metaBean().metaProperty(entry.getKey()).setString(bean, entry.getValue());
        }
      }
    }
    
    // start
    component.start(repo);
  }

}
