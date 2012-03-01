/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.component.tool;

import com.opengamma.component.ComponentManager;
import com.opengamma.component.ComponentRepository;
import com.opengamma.financial.tool.ToolContext;

/**
 * Utilities that assist with obtaining a tool context.
 * <p>
 * This is a thread-safe static utility class.
 */
public final class ToolContextUtils {

  /**
   * Restricted constructor.
   */
  private ToolContextUtils() {
  }

  //-------------------------------------------------------------------------
  /**
   * Uses a {@code ComponentManager} to start and load a {@code ToolContext}.
   * <p>
   * The context should be closed after use.
   * 
   * @param configResourceLocation  the location of the context resource file, not null
   * @return the context, not null
   */
  public static ToolContext getToolContext(String configResourceLocation) {
    ComponentManager manager = new ComponentManager("toolcontext");
    manager.start(configResourceLocation);
    ComponentRepository repo = manager.getRepository();
    return repo.getInstance(ToolContext.class, "tool");
  }

}
