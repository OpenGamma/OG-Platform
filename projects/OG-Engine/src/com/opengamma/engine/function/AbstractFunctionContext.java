/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.function;

import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentSkipListMap;

import com.opengamma.util.PublicAPI;

// NOTE kirk 2010-03-07 -- This class is intentionally NOT Serializable, as we expect
// that this will contain lots of interface implementations for things like data providers
// which are not Serializable. It's thus a runtime configuration object rather than a
// static configuration object.

/**
 * The base class for any multi-valued map-like context which may be provided
 * to a {@link FunctionDefinition} or {@link FunctionInvoker}.
 */
@PublicAPI
/* package */abstract class AbstractFunctionContext {
  private final Map<String, Object> _backingMap = new ConcurrentSkipListMap<String, Object>();

  protected AbstractFunctionContext() {
  }

  protected AbstractFunctionContext(final AbstractFunctionContext copyFrom) {
    _backingMap.putAll(copyFrom._backingMap);
  }

  /**
   * Returns a value from the context. This should not be called directly from function code -
   * static context wrappers should provide type safe access to elements. For example:
   * <pre>
   * public class MyFunctionContext {
   * 
   *   private static final String FOO_NAME = "Foo";
   *   
   *   // ...
   *   
   *   public static Foo getFoo (AbstractFunctionContext context) {
   *     return (Foo)context.get (FOO);
   *   }
   *   
   *   public static void setFoo (AbstractFunctionContext context, Foo foo) {
   *     context.set (FOO, foo);
   *   }
   *   
   *   // ...
   *   
   * }
   * </pre>
   * 
   * @param elementName the value to return
   * @return the value or {@code null} if none is defined
   */
  public Object get(String elementName) {
    return _backingMap.get(elementName);
  }

  /**
   * Stores a value in the context. This should not be called directly from function code -
   * static context wrappers should provide type safe access to elements. See the example for {@link #get (String)}.
   * 
   * @param elementName name of the element to set
   * @param value the value to set, not {@code null}
   * @return the previous value set for the element or {@code null} if none was defined
   */
  public Object put(String elementName, Object value) {
    return _backingMap.put(elementName, value);
  }

  /**
   * Removes a value from the context. This should not be called directly from function code.
   * 
   * @param elementName name of the element to remove, not {@code null}
   * @return the previous value set for the element or {@code null} if none was defined.
   */
  public Object remove(String elementName) {
    return _backingMap.remove(elementName);
  }

  /**
   * Returns all element names currently defined in the context. These can be used with {@link #get (String)} to
   * retrieve the values.
   * 
   * @return the set of element names
   */
  public Set<String> getAllElementNames() {
    // See UTL-20. No need to reorder into a TreeSet<>.
    return new TreeSet<String>(_backingMap.keySet());
  }

  /**
   * Duplicates this context as a deep copy. The copy will not be affected by changes to the original instance.
   * 
   * @return a copy of the context
   */
  public abstract AbstractFunctionContext clone();

}
