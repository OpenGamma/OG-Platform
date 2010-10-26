/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.value;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.PublicAPI;

/**
 * An immutable set of constraints on the values required, or properties of the value produced.
 */
@PublicAPI
public final class ValueProperties implements Serializable {

  /**
   * Builder pattern for constructing properties.
   */
  public static final class Builder {

    private final Map<String, Set<String>> _properties;

    private Builder() {
      _properties = new HashMap<String, Set<String>>();
    }

    private Builder(final Map<String, Set<String>> properties) {
      _properties = new HashMap<String, Set<String>>(properties);
    }

    public Builder with(String propertyName, final String propertyValue) {
      ArgumentChecker.notNull(propertyName, "propertyName");
      ArgumentChecker.notNull(propertyValue, "propertyValue");
      propertyName = propertyName.intern();
      final Set<String> previous = _properties.put(propertyName, Collections.singleton(propertyValue));
      if (previous != null) {
        if (previous.isEmpty()) {
          _properties.put(propertyName, previous);
        } else {
          final Set<String> replacement = new HashSet<String>(previous);
          replacement.add(propertyValue);
          _properties.put(propertyName, Collections.unmodifiableSet(replacement));
        }
      }
      return this;
    }

    public Builder with(final String propertyName, final String... propertyValues) {
      ArgumentChecker.notNull(propertyValues, "propertyValues");
      return with(propertyName, Arrays.asList(propertyValues));
    }

    public Builder with(String propertyName, final Collection<String> propertyValues) {
      ArgumentChecker.notNull(propertyName, "propertyName");
      ArgumentChecker.notNull(propertyValues, "propertyValues");
      final Set<String> values = new HashSet<String>(propertyValues);
      if (values.isEmpty()) {
        throw new IllegalArgumentException("propertyValues must contain at least one element");
      }
      if (values.contains(null)) {
        throw new IllegalArgumentException("propertyValues cannot contain null");
      }
      propertyName = propertyName.intern();
      final Set<String> previous = _properties.put(propertyName, Collections.unmodifiableSet(values));
      if (previous != null) {
        if (previous.isEmpty()) {
          _properties.put(propertyName, previous);
        } else {
          final Set<String> replacement = new HashSet<String>(previous);
          replacement.addAll(propertyValues);
          _properties.put(propertyName, Collections.unmodifiableSet(replacement));
        }
      }
      return this;
    }

    public Builder withAny(final String propertyName) {
      ArgumentChecker.notNull(propertyName, "propertyName");
      _properties.put(propertyName.intern(), Collections.<String> emptySet());
      return this;
    }

    public ValueProperties get() {
      if (_properties.isEmpty()) {
        return EMPTY;
      }
      return new ValueProperties(Collections.unmodifiableMap(_properties));
    }

  }

  private static final ValueProperties EMPTY = new ValueProperties(Collections.<String, Set<String>> emptyMap());

  private final Map<String, Set<String>> _properties;

  private ValueProperties(final Map<String, Set<String>> properties) {
    _properties = properties;
  }

  public static ValueProperties none() {
    return EMPTY;
  }

  public static Builder builder() {
    return new Builder();
  }

  public Builder copy() {
    return new Builder(_properties);
  }

  public static Builder with(final String propertyName, final String propertyValue) {
    return builder().with(propertyName, propertyValue);
  }

  public static Builder with(final String propertyName, final String... propertyValues) {
    return builder().with(propertyName, propertyValues);
  }

  public static Builder with(final String propertyName, final Collection<String> propertyValues) {
    return builder().with(propertyName, propertyValues);
  }

  public static Builder withAny(final String propertyName) {
    return builder().withAny(propertyName);
  }

  /**
   * Returns an immutable set of defined property names.
   * 
   * @return the property names, not {@code null}
   */
  public Set<String> getProperties() {
    return _properties.keySet();
  }

  /**
   * Returns an immutable set of values for a given property name. If the name is not defined,
   * returns {@code null}. If the name has a wild-card value, the empty set is returned.
   * 
   * @param propertyName the name required, not {@code null}
   * @return the set of values, empty if wild-card, {@code null} if not defined
   */
  public Set<String> getValues(final String propertyName) {
    return _properties.get(propertyName);
  }

  /**
   * Tests if this set of properties can be satisfied by the other set. An individual property is
   * satisfied if:
   * 
   * It is a wild-card and the other property set has a definition for it; or
   * The other property set has a wild-card definition for it; or
   * The other property set provides at least one of the possible property values
   * 
   * The property set is satisfied if each of the individual properties can be satisfied. 
   * 
   * @param properties other property set to check against, not {@code null}
   * @return {@code true} if this set of properties can be satisfied by the other set, {@code false} otherwise
   */
  public boolean isSatisfiedBy(final ValueProperties properties) {
    assert properties != null;
    nextProperty: for (Map.Entry<String, Set<String>> property : _properties.entrySet()) {
      final Set<String> available = properties.getValues(property.getKey());
      if (available == null) {
        // Can't be satisfied - required property never defined
        return false;
      }
      if (available.isEmpty() || property.getValue().isEmpty()) {
        // Other properties can supply anything - satisfying this requirement
        // or this requirement is for anything and so satisfied.
        continue;
      }
      for (String value : property.getValue()) {
        if (available.contains(value)) {
          // There is at least one value that can satisfy this requirement
          continue nextProperty;
        }
      }
      // This requirement cannot be satisfied
      return false;
    }
    return true;
  }

  /**
   * Produces a set of properties such that for any properties defined by the other, the intersection
   * of the property values is taken. Any properties defined in this set not but not in the other remain
   * untouched. Requires {@code properties.isSatisfiedBy (this) == true}.
   * 
   * @param properties other property set to compose against, not {@code null}
   * @return the new set of properties, or this object if the composition result is equal
   */
  public ValueProperties compose(final ValueProperties properties) {
    assert properties != null;
    assert properties.isSatisfiedBy(this);
    for (Map.Entry<String, Set<String>> property : _properties.entrySet()) {
      final Set<String> available = properties.getValues(property.getKey());
      if ((available == null) || available.isEmpty()) {
        // This property unchanged in output
        continue;
      }
      if (property.getValue().isEmpty()) {
        // Requires a subset in the output
        return composeImpl(properties);
      }
      if (property.getValue().size() != available.size()) {
        // Requires an intersection in the output
        return composeImpl(properties);
      }
      for (String value : property.getValue()) {
        if (!available.contains(value)) {
          // Requires an intersection in the output
          return composeImpl(properties);
        }
      }
    }
    return this;
  }

  private ValueProperties composeImpl(final ValueProperties properties) {
    final Map<String, Set<String>> composed = new HashMap<String, Set<String>>();
    int otherAvailable = 0;
    nextProperty: for (Map.Entry<String, Set<String>> property : _properties.entrySet()) {
      final Set<String> available = properties.getValues(property.getKey());
      if (available == null) {
        // Other is nothing, or wild-card so use these values
        composed.put(property.getKey(), property.getValue());
        continue;
      }
      if (property.getValue().isEmpty()) {
        // This is a wild-card so use other values
        composed.put(property.getKey(), available);
        otherAvailable++;
        continue;
      }
      if (available.isEmpty()) {
        // Other is wild-card so use current value
        composed.put(property.getKey(), property.getValue());
        continue;
      }
      if (property.getValue().size() != available.size()) {
        composed.put(property.getKey(), intersect(property.getValue(), available));
        continue;
      }
      for (String value : property.getValue()) {
        if (!available.contains(value)) {
          composed.put(property.getKey(), intersect(property.getValue(), available));
          continue nextProperty;
        }
      }
      // Property is identical in both
      composed.put(property.getKey(), property.getValue());
    }
    if (otherAvailable == _properties.size()) {
      return properties;
    } else {
      return new ValueProperties(Collections.unmodifiableMap(composed));
    }
  }

  private static <T> Set<T> intersect(final Set<T> a, final Set<T> b) {
    final Set<T> result = new HashSet<T>();
    for (T v : a) {
      if (b.contains(v)) {
        result.add(v);
      }
    }
    return result;
  }

  /**
   * Tests if the set of properties is strict. A property set is strict if there is only one value
   * for each property (or the property set is empty).
   * 
   * @return {@code true} if the property set is strict, {@code false} otherwise
   */
  public boolean isStrict() {
    for (Set<String> property : _properties.values()) {
      if (property.size() != 1) {
        return false;
      }
    }
    return true;
  }

  @Override
  public boolean equals(final Object o) {
    if (o == this) {
      return true;
    }
    if (!(o instanceof ValueProperties)) {
      return false;
    }
    final ValueProperties other = (ValueProperties) o;
    return _properties.equals(other._properties);
  }

  @Override
  public int hashCode() {
    return _properties.hashCode();
  }

  public boolean isEmpty() {
    return _properties.isEmpty();
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    sb.append("{");
    boolean first = true;
    for (Map.Entry<String, Set<String>> property : _properties.entrySet()) {
      if (first) {
        first = false;
      } else {
        sb.append(", ");
      }
      sb.append(property.getKey()).append("=").append(property.getValue());
    }
    sb.append("}");
    return sb.toString();
  }

}
