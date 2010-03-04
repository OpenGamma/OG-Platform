/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.function;

import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * Contains objects useful to {@link FunctionDefinition} instances
 * during expression compilation.
 *
 * @author kirk
 */
public class FunctionCompilationContext {
  private final Map<String, Object> _backingMap = new ConcurrentSkipListMap<String, Object>();
    
  public Object get(String elementName) {
    return _backingMap.get(elementName);
  }
  
  public Object put(String elementName, Object value) {
    return _backingMap.put(elementName, value);
  }
}
