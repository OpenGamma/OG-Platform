/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.opengamma.util.ArgumentChecker;

/**
 * A repository for OpenGamma components.
 * <p>
 * The OpenGamma logical architecture consists of a set of components.
 * This repository manages the components.
 * <p>
 * This class is thread-safe via concurrent collections.
 */
public class ComponentRepository {

  /**
   * The repository of components.
   */
  private final ConcurrentMap<Class<?>, Repo> _repoMap = new ConcurrentHashMap<Class<?>, Repo>();

  /**
   * Creates an instance.
   */
  public ComponentRepository() {
  }

  /**
   * Gets an instance of a component.
   * <p>
   * This finds a component that matches the specified type, using the tag as a hint.
   * 
   * @param <T>  the type
   * @param type  the type to get, not null
   * @param tag  the tag to help distinguish the component, not null
   * @return the component, not null
   * @throws IllegalArgumentException if no component is available
   */
  public <T> T get(Class<T> type, String tag) {
    ArgumentChecker.notNull(type, "type");
    ArgumentChecker.notNull(tag, "tag");
    Repo repo = _repoMap.get(tag);
    if (repo == null) {
      throw new IllegalArgumentException("No component available for specified type: " + type.getName());
    }
    return repo.bestMatch(type, tag);
  }

  /**
   * Gets an instance of a component using a class as the tag.
   * <p>
   * This finds a component that matches the specified type, using the tag as a hint.
   * 
   * @param <T>  the type
   * @param type  the type to get, not null
   * @param tag  the tag to help distinguish the component, not null
   * @return the component, not null
   * @throws IllegalArgumentException if no component is available
   */
  public <T> T get(Class<T> type, Class<?> tag) {
    ArgumentChecker.notNull(type, "type");
    ArgumentChecker.notNull(tag, "tag");
    return get(type, tag.getSimpleName());
  }

  /**
   * Registers the component.
   * 
   * @param <T>  the type
   * @param type  the type to register under, not null
   * @param component  the component to register, not null
   * @param tags  the tags to help distinguish the component, not null
   */
  public <T> void register(Class<T> type, T component, String... tags) {
    ArgumentChecker.notNull(type, "type");
    ArgumentChecker.notNull(component, "component");
    ArgumentChecker.noNulls(tags, "tags");
    _repoMap.putIfAbsent(type, new Repo());
    Repo repo = _repoMap.get(type);
    repo.register(component, tags);
  }

  //-------------------------------------------------------------------------
  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(1024);
    buf.append(getClass().getSimpleName()).append('[');
    for (Class<?> type : _repoMap.keySet()) {
      buf.append('\n').append(type.getName()).append("=").append(_repoMap.get(type));
    }
    return buf.append(']').toString();
  }

  //-------------------------------------------------------------------------
  /**
   * The tagged repository for a given type.
   */
  static final class Repo {
    /**
     * The repository of components.
     */
    private final ConcurrentMap<String, Object> _repo = new ConcurrentHashMap<String, Object>();

    /**
     * Finds the best match for the given tag.
     * 
     * @param <T>  the type
     * @param type  the type to get, not null
     * @param tag  the tag to help distinguish the component, not null
     * @return the best match, not null
     * @throws IllegalArgumentException if no component is available
     */
    <T> T bestMatch(Class<T> type, String tag) {
      final Map<String, Object> clone = new HashMap<String, Object>(_repo);
      
      // try exact match
      Object matched = clone.get(tag);
      if (matched != null) {
        return type.cast(matched);
      }
      
      // try partial match
      if (tag.length() >= 3) {
        final List<Object> possible = new ArrayList<Object>();
        for (String key : clone.keySet()) {
          if (key.contains(tag)) {
            possible.add(clone.get(key));
          }
        }
        switch (possible.size()) {
          case 0:
            break;
          case 1:
            return type.cast(possible.get(0));
          default:
            throw new IllegalArgumentException("Multiple components available for specified tag: " + tag + ": " + type.getName());
        }
      }
      
      // try default
      matched = clone.get("DEFAULT");
      if (matched != null) {
        return type.cast(matched);
      }
      
      throw new IllegalArgumentException("No component available for specified tag: " + tag + ": " + type.getName());
    }

    /**
     * Registers the component.
     * 
     * @param component  the component to register, not null
     * @param tags  the tags to help distinguish the component, not null
     */
    void register(Object component, String... tags) {
      for (String tag : tags) {
        _repo.putIfAbsent(tag, component);
      }
    }
  }

}
