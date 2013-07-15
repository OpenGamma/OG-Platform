/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.function;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentSkipListMap;

import com.opengamma.util.PublicAPI;

/**
 * The context used while evaluating functions.
 * <p>
 * Most functions do not live in isolation, instead they rely on contextual data.
 * This class and its subclasses provide a multi-valued map-like context for functions.
 * <p>
 * This class is abstract and mutable with some degree of thread-safety using a concurrent map.
 * It is not serializable as it is intended for runtime configuration, holding many non-serializable items.
 */
@PublicAPI
/* package */abstract class AbstractFunctionContext {

  /**
   * The concurrent backing map.
   * This ensures that each get/put is safe, but operations relying on two different gets
   * could see inconsistent state.
   */
  private final Map<String, Object> _backingMap = new ConcurrentSkipListMap<String, Object>();

  /**
   * Constructor.
   */
  protected AbstractFunctionContext() {
  }

  /**
   * Constructor that assigns all items from the specified context to the new context.
   * The copy is shallow - elements are not cloned.
   *
   * @param copyFrom  the object to copy from, not null
   */
  protected AbstractFunctionContext(final AbstractFunctionContext copyFrom) {
    _backingMap.putAll(copyFrom._backingMap);
  }

  /**
   * Returns a value from the context.
   * <p>
   * This is not intended to be called directly from function code -
   * static context wrappers should provide type safe access to elements.
   * For example:
   * <pre>
   * public class MyFunctionContext {
   *
   *   private static final String FOO_NAME = "Foo";
   *
   *   // ...
   *
   *   public static Foo getFoo(AbstractFunctionContext context) {
   *     return (Foo) context.get(FOO);
   *   }
   *
   *   public static void setFoo(AbstractFunctionContext context, Foo foo) {
   *     context.set(FOO, foo);
   *   }
   *
   *   // ...
   *
   * }
   * </pre>
   *
   * @param elementName  the name of the element to lookup, not null
   * @return the value, null if none is defined
   */
  public Object get(final String elementName) {
    return _backingMap.get(elementName);
  }

  /**
   * Stores a value in the context.
   * <p>
   * This is not intended to be called directly from function code -
   * static context wrappers should provide type safe access to elements.
   * See the example for {@link #get(String)}.
   *
   * @param elementName  the name of the element to set, not null
   * @param value the value to set, not null
   * @return the previous value for the element, null if none was defined
   */
  public Object put(final String elementName, final Object value) {
    return _backingMap.put(elementName, value);
  }

  /**
   * Removes a value from the context.
   * This is not intended to be called directly from function code.
   *
   * @param elementName  the name of the element to remove, not null
   * @return the previous value for the element, null if none was defined
   */
  public Object remove(final String elementName) {
    return _backingMap.remove(elementName);
  }

  /**
   * Returns all element names currently defined in the context.
   * These can be used with {@link #get(String)} to retrieve the values.
   *
   * @return the set of element names, not null
   */
  public Set<String> getAllElementNames() {
    // See UTL-20. No need to reorder into a TreeSet<>.
    return new TreeSet<String>(_backingMap.keySet());
  }

  protected Collection<Object> getAllElements() {
    return new ArrayList<Object>(_backingMap.values());
  }

  /**
   * Clones this context creating a copy that has an independent backing map.
   * The clone is shallow - elements are not cloned.
   * The copy will not be affected by changes to the original instance.
   *
   * @return a copy of the context
   */
  @Override
  public abstract AbstractFunctionContext clone();

}
