/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.depgraph;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.function.FunctionDefinition;
import com.opengamma.engine.function.FunctionRepository;
import com.opengamma.engine.value.ValueRequirement;

/**
 * Runtime exception thrown when a dependency graph cannot be constructed.
 * <p>
 * This is thrown during the creation of a dependency graph.
 * It indicates that the {@link FunctionDefinition}s available in the provided
 * {@link FunctionRepository} were insufficient to meet the requirements.
 */
public class UnsatisfiableDependencyGraphException extends OpenGammaRuntimeException {

  /** Serialization version. */
  private static final long serialVersionUID = 1L;

  /**
   * The requirement that could not be met.
   */
  private final ValueRequirement _requirement;
  /**
   * The state of key information.
   */
  private final Map<String, Object> _state = new HashMap<String, Object>();

  /**
   * Creates an instance based on a requirement.
   * 
   * @param requirement  the requirement, should not be null
   */
  public UnsatisfiableDependencyGraphException(final ValueRequirement requirement) {
    super("");
    _requirement = requirement;
  }

  /**
   * Creates an instance based on a requirement with optional message and detailed state.
   * 
   * @param requirement  the requirement, should not be null
   * @param message  a message to append to the main message summarizing the problem, may be null
   */
  public UnsatisfiableDependencyGraphException(final ValueRequirement requirement, final String message) {
    super(message);
    _requirement = requirement;
  }

  /**
   * Creates an instance based on a requirement and exception.
   * 
   * @param requirement  the requirement, should not be null
   * @param cause  the cause of the exception, may be null
   */
  public UnsatisfiableDependencyGraphException(final ValueRequirement requirement, final Throwable cause) {
    super("", cause);
    _requirement = requirement;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the value requirement that could not be met.
   * 
   * @return the value requirement, should not be null
   */
  public ValueRequirement getRequirement() {
    return _requirement;
  }

  /**
   * Gets a map of key objects that were in use and caused the failure.
   * 
   * @return the key state, not null
   */
  public Map<String, Object> getState() {
    return _state;
  }

  //-------------------------------------------------------------------------
  /**
   * Add to the map of key objects that were in use.
   * 
   * @param object  the state object, null ignored
   * @return this, for chaining, not null
   */
  public UnsatisfiableDependencyGraphException addState(final Object object) {
    if (object != null) {
      _state.put(object.getClass().getSimpleName(), object);
    }
    return this;
  }

  /**
   * Add to the map of key objects that were in use.
   * 
   * @param name  the description of the state object, may be null
   * @param object  the state object, may be null
   * @return this, for chaining, not null
   */
  public UnsatisfiableDependencyGraphException addState(final String name, final Object object) {
    _state.put(name, object);
    return this;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the message describing the exception.
   * <p>
   * This is lazily created here for performance.
   *
   * @return the detail message, not null
   */
  @Override
  public String getMessage() {
    String base = "Unable to satisfy value requirement: ";
    if (super.getMessage() != null) {
      base = "Unable to satisfy value requirement, " + super.getMessage() + ": ";
    }
    base = base + getRequirement();
    if (_state.size() > 0) {
      base += ", StateOfTheWorld=";
      for (String name : _state.keySet()) {
        base += "\n| " + name + "=";
        Object obj = _state.get(name);
        if (obj instanceof Collection) {
          for (Object loop : (Collection<?>) obj) {
            base += "\n||  " + loop;
          }
        } else if (obj instanceof Map) {
          for (Object loop : ((Map<?, ?>) obj).entrySet()) {
            base += "\n||  " + loop;
          }
        } else {
          base += obj;
        }
      }
    }
    return base;
  }

}
