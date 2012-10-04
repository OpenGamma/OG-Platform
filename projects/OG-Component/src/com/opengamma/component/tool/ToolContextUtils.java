/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.component.tool;

import java.net.URI;
import java.util.Arrays;
import java.util.List;

import org.joda.beans.MetaProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.component.ComponentInfo;
import com.opengamma.component.ComponentManager;
import com.opengamma.component.ComponentRepository;
import com.opengamma.component.ComponentServer;
import com.opengamma.component.factory.ComponentInfoAttributes;
import com.opengamma.component.rest.RemoteComponentServer;
import com.opengamma.financial.tool.ToolContext;

/**
 * Utilities that assist with obtaining a tool context.
 * <p>
 * This is a thread-safe static utility class.
 */
public final class ToolContextUtils {

  private static final Logger s_logger = LoggerFactory.getLogger(ToolContextUtils.class);

  /**
   * The default classifier chain for selecting components from a server
   */
  private static final List<String> DEFAULT_CLASSIFIER_CHAIN =
      Arrays.asList("central", "main", "default", "shared", "combined");

  /**
   * Restricted constructor.
   */
  private ToolContextUtils() {
  }

  //-------------------------------------------------------------------------
  /**
   * Uses a {@code ComponentManager} or a {@code ComponentServer} to start and load a {@code ToolContext}.
   * <p>
   * The context should be closed after use.
   * 
   * @param configResourceLocation  the location of the context resource file, not null
   * @param toolContextClazz        the type of tool context to return
   * @return the context, not null
   */
  public static ToolContext getToolContext(String configResourceLocation, Class<? extends ToolContext> toolContextClazz) {
    return getToolContext(configResourceLocation, toolContextClazz, DEFAULT_CLASSIFIER_CHAIN);
  }

  //-------------------------------------------------------------------------
  /**
   * Uses a {@code ComponentManager} or a {@code ComponentServer} to start and load a {@code ToolContext}.
   * <p>
   * The context should be closed after use.
   *
   * @param configResourceLocation  the location of the context resource file, not null
   * @param toolContextClazz        the type of tool context to return
   * @param classifierChain         the classifier chain to use when determining which components to select
   * @return the context, not null
   */
  public static ToolContext getToolContext(String configResourceLocation, Class<? extends ToolContext> toolContextClazz, List<String> classifierChain) {

    configResourceLocation = configResourceLocation.trim();
    if (configResourceLocation.startsWith("http://")) {

      // Fix passed-in URI
      if (!configResourceLocation.endsWith("/jax")) {
        if (configResourceLocation.endsWith("/")) {
          configResourceLocation += "jax";
        } else {
          configResourceLocation += "/jax";
        }
      }

      // Get the remote component server using the supplied URI
      RemoteComponentServer remoteComponentServer = new RemoteComponentServer(URI.create(configResourceLocation));
      ComponentServer componentServer = remoteComponentServer.getComponentServer();

      // Attempt to build a tool context of the specified type
      ToolContext toolContext;
      try {
        toolContext = toolContextClazz.newInstance();
      } catch (Throwable t) {
        return null;
      }

      // Populate the tool context from the remote component server
      for (MetaProperty<?> metaProperty : toolContext.metaBean().metaPropertyIterable()) {
        if (!metaProperty.name().equals("contextManager")) {
          try {
            ComponentInfo componentInfo =
                getComponentInfo(componentServer, classifierChain, metaProperty.propertyType());
            if (componentInfo == null) {
              s_logger.warn("Could not populate tool context " + metaProperty.name() +
                  " because no appropriate component was found on the server");
              continue;
            }
            String clazzName = componentInfo.getAttribute(ComponentInfoAttributes.REMOTE_CLIENT_JAVA);
            if (clazzName == null) {
              s_logger.warn("Could not populate tool context " + metaProperty.name() +
                  " because no remote access class could be identified");
              continue;
            }
            Class<?> clazz = Class.forName(clazzName);
            metaProperty.set(toolContext, clazz.getConstructor(URI.class).newInstance(componentInfo.getUri()));
            s_logger.info("Populated tool context " + metaProperty.name() + " with " + metaProperty.get(toolContext));
          } catch (Throwable e) {
            s_logger.warn("Could not populate tool context " + metaProperty.name() + " because: " +
                e.getMessage());
          }
        }
      }
      return toolContext;

    // Populate the tool context from a local properties file
    } else {
      ComponentManager manager = new ComponentManager("toolcontext");
      manager.start(configResourceLocation);
      ComponentRepository repo = manager.getRepository();
      return repo.getInstance(ToolContext.class, "tool");
    }
  }

  private static ComponentInfo getComponentInfo(ComponentServer componentServer, List<String> preferenceList, Class<?> type) {
    if (preferenceList != null) {
      for (String preference : preferenceList) {
        try {
          ComponentInfo componentInfo = componentServer.getComponentInfo(type, preference);
          if (componentInfo != null) {
            return componentInfo;
          }
        } catch (IllegalArgumentException iae) {
          // do nothing and try the next one.
        }
      }
    }
    List<ComponentInfo> componentInfos = componentServer.getComponentInfos(type);
    return componentInfos.size() == 0 ? null : componentInfos.get(0);
  }

}
