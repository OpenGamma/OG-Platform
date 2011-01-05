/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.value;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.ObjectUtils;

import com.google.common.collect.Sets;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.PublicAPI;

/**
 * An immutable set of constraints on the values required, or properties of the value produced.
 * 
 * @see ValuePropertyNames
 */
@PublicAPI
public abstract class ValueProperties implements Serializable, Comparable<ValueProperties> {

  /**
   * Builder pattern for constructing {@link ValueProperties} objects.
   */
  public static final class Builder {

    private final Map<String, Set<String>> _properties;
    private Set<String> _optional;

    private Builder() {
      _properties = new HashMap<String, Set<String>>();
    }

    private Builder(final Map<String, Set<String>> properties, final Set<String> optional) {
      _properties = new HashMap<String, Set<String>>(properties);
      _optional = (optional != null) ? new HashSet<String>(optional) : null;
    }

    /**
     * Adds a property value to the builder. If the property is already a wild-card, the builder is left
     * unchanged.
     * 
     * @param propertyName name of the property, not {@code null}
     * @param propertyValue value, not {@code null}
     * @return the builder instance
     */
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

    /**
     * Adds property values to the builder. If the property is already a wild-card, the builder is left
     * unchanged.
     * 
     * @param propertyName name of the property, not {@code null}
     * @param propertyValues values to add, not {@code null} and not containing {@code null}s.
     * @return the builder instance
     */
    public Builder with(final String propertyName, final String... propertyValues) {
      ArgumentChecker.notNull(propertyValues, "propertyValues");
      return with(propertyName, Arrays.asList(propertyValues));
    }

    /**
     * Adds property values to the builder. If the property is already a wild-card, the builder is left
     * unchanged.
     * 
     * @param propertyName name of the property, not {@code null}
     * @param propertyValues values to add, not {@code null} and not containing {@code null}s
     * @return the builder instance
     */
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

    /**
     * Adds a wild-card property value. If explicit values were previously set for the property, they
     * are removed to leave the wild-card definition.
     * 
     * @param propertyName name of the property, not {@code null}
     * @return the builder instance
     */
    public Builder withAny(final String propertyName) {
      ArgumentChecker.notNull(propertyName, "propertyName");
      _properties.put(propertyName.intern(), Collections.<String> emptySet());
      return this;
    }

    /**
     * Declares a property as optional when used as a constraint. By default constraints are required, and
     * can only be satisfied if the other property set defines a matching value. If a constraint is optional
     * the other set may define a matching value, or have no definition for the property. If no explicit values
     * for the property are set with one of the other calls, the property will have a wild-card value (i.e.
     * as if {@link #withAny (String)} had been called.
     * 
     * @param propertyName name of the property, not {@code null}
     * @return the builder instance 
     */
    public Builder withOptional(final String propertyName) {
      ArgumentChecker.notNull(propertyName, "propertyName");
      if (_optional == null) {
        _optional = new HashSet<String>();
      }
      _optional.add(propertyName);
      return this;
    }

    /**
     * Removes a property from the builder definition.
     * 
     * @param propertyName name of the property, not {@code null}
     * @return the builder instance
     */
    public Builder withoutAny(final String propertyName) {
      ArgumentChecker.notNull(propertyName, "propertyName");
      _properties.remove(propertyName);
      _optional.remove(propertyName);
      return this;
    }

    /**
     * Constructs and returns a {@link ValueProperties} instance based on the builder's current state.
     * 
     * @return the property set
     */
    public ValueProperties get() {
      if (_optional != null) {
        for (String optionalProperty : _optional) {
          if (!_properties.containsKey(optionalProperty)) {
            _properties.put(optionalProperty, Collections.<String> emptySet());
          }
        }
        return new ValuePropertiesImpl(Collections.unmodifiableMap(_properties), Collections.unmodifiableSet(_optional));
      } else {
        if (_properties.isEmpty()) {
          return EMPTY;
        }
        return new ValuePropertiesImpl(Collections.unmodifiableMap(_properties), Collections.<String> emptySet());
      }
    }

  }

  private static final class ValuePropertiesImpl extends ValueProperties {

    private final Map<String, Set<String>> _properties;
    private final Set<String> _optional;

    private ValuePropertiesImpl(final Map<String, Set<String>> properties, final Set<String> optional) {
      _properties = properties;
      _optional = optional;
    }

    @Override
    public Builder copy() {
      return new Builder(_properties, _optional);
    }

    @Override
    public Set<String> getProperties() {
      return _properties.keySet();
    }

    @Override
    public Set<String> getValues(final String propertyName) {
      return _properties.get(propertyName);
    }

    @Override
    public boolean isOptional(final String propertyName) {
      return _optional.contains(propertyName);
    }

    @Override
    public boolean isSatisfiedBy(final ValueProperties properties) {
      assert properties != null;
      nextProperty: for (Map.Entry<String, Set<String>> property : _properties.entrySet()) {
        final Set<String> available = properties.getValues(property.getKey());
        if (available == null) {
          if (!isOptional(property.getKey())) {
            // Can't be satisfied - required property never defined
            return false;
          }
          continue;
        }
        if (!isOptional(property.getKey())) {
          if (properties.isOptional(property.getKey())) {
            // Can't be satisfied - required property might not be defined
            return false;
          }
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
      // All requirements can be satisfied
      return true;
    }

    @Override
    public ValueProperties compose(final ValueProperties properties) {
      assert properties != null;
      if ((properties == EMPTY) || (properties == INFINITE)) {
        return this;
      }
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
      Set<String> optional = null;
      int otherAvailable = 0;
      nextProperty: for (Map.Entry<String, Set<String>> property : _properties.entrySet()) {
        final Set<String> available = properties.getValues(property.getKey());
        if (available == null) {
          // Other is not defined, so use current value
          composed.put(property.getKey(), property.getValue());
          // Preserve optionality from this property set
          if (isOptional(property.getKey())) {
            if (optional == null) {
              optional = new HashSet<String>();
            }
            optional.add(property.getKey());
          }
          continue;
        }
        // Preserve optionality from the other property set
        if (properties.isOptional(property.getKey())) {
          if (optional == null) {
            optional = new HashSet<String>();
          }
          optional.add(property.getKey());
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
          final Set<String> intersection = Sets.intersection(property.getValue(), available);
          // An empty intersection means no resulting property
          if (!intersection.isEmpty()) {
            composed.put(property.getKey(), intersection);
          }
          continue;
        }
        for (String value : property.getValue()) {
          if (!available.contains(value)) {
            final Set<String> intersection = Sets.intersection(property.getValue(), available);
            // An empty intersection means no resulting property
            if (!intersection.isEmpty()) {
              composed.put(property.getKey(), intersection);
            }
            continue nextProperty;
          }
        }
        // Property is identical in both
        composed.put(property.getKey(), property.getValue());
      }
      if ((composed.size() == otherAvailable) && (otherAvailable == _properties.size())) {
        // We've just built a map containing only the other property values, so return that original
        return properties;
      } else {
        return new ValuePropertiesImpl(Collections.unmodifiableMap(composed), (optional != null) ? Collections.unmodifiableSet(optional) : Collections.<String> emptySet());
      }
    }

    @Override
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
      if (!(o instanceof ValuePropertiesImpl)) {
        return false;
      }
      final ValuePropertiesImpl other = (ValuePropertiesImpl) o;
      return _properties.equals(other._properties) && ObjectUtils.equals(_optional, other._optional);
    }

    @Override
    public int hashCode() {
      return _properties.hashCode();
    }

    @Override
    public boolean isEmpty() {
      return _properties.isEmpty();
    }

    public static String toString(final Map<String, Set<String>> properties, final Set<String> optional) {
      final StringBuilder sb = new StringBuilder();
      sb.append("{");
      boolean first = true;
      for (Map.Entry<String, Set<String>> property : properties.entrySet()) {
        if (first) {
          first = false;
        } else {
          sb.append(",");
        }
        sb.append(property.getKey()).append("=").append(property.getValue());
        if (optional.contains(property.getKey())) {
          sb.append("?");
        }
      }
      sb.append("}");
      return sb.toString();
    }

    @Override
    public String toString() {
      return toString(_properties, _optional);
    }
  }

  /**
   * Implements a nearly infinite property set.
   */
  private static final class NearlyInfinitePropertiesImpl extends ValueProperties {

    private final Set<String> _without;

    private NearlyInfinitePropertiesImpl(final Set<String> without) {
      _without = without;
    }

    @Override
    public ValueProperties compose(ValueProperties properties) {
      // Yields the same
      return this;
    }

    @Override
    public Builder copy() {
      throw new UnsupportedOperationException("Can't copy the nearly infinite set");
    }

    @Override
    public Set<String> getProperties() {
      return Collections.emptySet();
    }

    @Override
    public Set<String> getValues(String propertyName) {
      if (_without.contains(propertyName)) {
        return null;
      } else {
        return Collections.emptySet();
      }
    }

    @Override
    public boolean isEmpty() {
      return false;
    }

    @Override
    public boolean isOptional(String propertyName) {
      return false;
    }

    @Override
    public boolean isSatisfiedBy(ValueProperties properties) {
      if (properties == INFINITE) {
        return true;
      }
      if (!(properties instanceof NearlyInfinitePropertiesImpl)) {
        return false;
      }
      final Set<String> otherWithouts = ((NearlyInfinitePropertiesImpl) properties)._without;
      for (String otherWithout : otherWithouts) {
        if (!_without.contains(otherWithout)) {
          return false;
        }
      }
      return true;
    }

    @Override
    public boolean isStrict() {
      return false;
    }

    @Override
    public ValueProperties withoutAny(final String propertyName) {
      ArgumentChecker.notNull(propertyName, "propertyName");
      if (_without.contains(propertyName)) {
        return this;
      } else {
        final Set<String> without = new HashSet<String>(_without);
        without.add(propertyName);
        return new NearlyInfinitePropertiesImpl(without);
      }
    }

    @Override
    public String toString() {
      final StringBuilder sb = new StringBuilder("INFINITE-{");
      boolean first = true;
      for (String without : _without) {
        if (first) {
          first = false;
        } else {
          sb.append(',');
        }
        sb.append(without);
      }
      sb.append('}');
      return sb.toString();
    }

    @Override
    public boolean equals(final Object o) {
      if (o == this) {
        return true;
      }
      if (!(o instanceof NearlyInfinitePropertiesImpl)) {
        return false;
      }
      final NearlyInfinitePropertiesImpl otherImpl = (NearlyInfinitePropertiesImpl) o;
      return _without.equals(otherImpl._without);
    }

    @Override
    public int hashCode() {
      return _without.hashCode();
    }

    @Override
    public int compareTo(final ValueProperties other) {
      if (other == this) {
        return 0;
      }
      if (other == INFINITE) {
        return -1;
      }
      if (other instanceof NearlyInfinitePropertiesImpl) {
        final NearlyInfinitePropertiesImpl otherImpl = (NearlyInfinitePropertiesImpl) other;
        return compareSet(_without, otherImpl._without);
      }
      return 1;
    }

  }

  /**
   * Implements the infinite property set.
   */
  private static final ValueProperties INFINITE = new ValueProperties() {

    @Override
    public ValueProperties compose(final ValueProperties properties) {
      // Composition yields the infinite set
      return this;
    }

    @Override
    public Builder copy() {
      throw new UnsupportedOperationException("Can't copy the infinite set");
    }

    @Override
    public Set<String> getProperties() {
      return Collections.emptySet();
    }

    @Override
    public Set<String> getValues(String propertyName) {
      return Collections.emptySet();
    }

    @Override
    public boolean isOptional(String propertyName) {
      // Everything's required
      return false;
    }

    @Override
    public boolean isSatisfiedBy(ValueProperties properties) {
      // Only the infinite set can satisfy
      return (properties == this);
    }

    @Override
    public boolean isStrict() {
      // Not strict by definition
      return false;
    }

    @Override
    public boolean isEmpty() {
      // Not empty by definition
      return false;
    }

    @Override
    public String toString() {
      return "INFINITE";
    }

    @Override
    public ValueProperties withoutAny(final String propertyName) {
      ArgumentChecker.notNull(propertyName, "propertyName");
      return new NearlyInfinitePropertiesImpl(Collections.singleton(propertyName));
    }

    @Override
    public int compareTo(final ValueProperties other) {
      if (other == this) {
        return 0;
      }
      return 1;
    }

  };

  /**
   * Implements the empty set.
   */
  private static final ValueProperties EMPTY = new ValueProperties() {

    @Override
    public ValueProperties compose(ValueProperties properties) {
      // The only thing satisfied by the empty set is the empty set, or a set with only optional properties
      return this;
    }

    @Override
    public Builder copy() {
      return new Builder();
    }

    @Override
    public Set<String> getProperties() {
      // No properties
      return null;
    }

    @Override
    public Set<String> getValues(String propertyName) {
      // No values for anything
      return null;
    }

    @Override
    public boolean isOptional(String propertyName) {
      // Alwyas false as no properties in the set
      return false;
    }

    @Override
    public boolean isEmpty() {
      // Always empty
      return true;
    }

    @Override
    public boolean isSatisfiedBy(ValueProperties properties) {
      // Satisfied by anything
      return true;
    }

    @Override
    public boolean isStrict() {
      // Empty is strict
      return true;
    }

    @Override
    public String toString() {
      return "EMPTY";
    }

  };

  /**
   * Returns the empty property set, typically indicating no value constraints.
   * 
   * @return the empty property set
   */
  public static ValueProperties none() {
    return EMPTY;
  }

  /**
   * Returns a property set that simulates all possible properties. This should be used with caution - well written
   * functions should build a property set explicitly stating the properties recognized.
   * 
   * @return the "infinite" property set 
   */
  public static ValueProperties all() {
    return INFINITE;
  }

  /**
   * Creates a builder for constructing a {@link ValueProperties} object.
   * 
   * @return the builder
   */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * Creates a builder for constructing a {@link ValueProperties} object with the given property defined.
   * 
   * @param propertyName name of the property to define, not {@code null}
   * @param propertyValue property value, not {@code null}
   * @return the builder instance
   */
  public static Builder with(final String propertyName, final String propertyValue) {
    return builder().with(propertyName, propertyValue);
  }

  /**
   * Creates a builder for constructing a {@link ValueProperties} object with the given property defined.
   * 
   * @param propertyName name of the property to define, not {@code null}
   * @param propertyValues property values, not {@code null} and not containing {@code null}
   * @return the builder instance
   */
  public static Builder with(final String propertyName, final String... propertyValues) {
    return builder().with(propertyName, propertyValues);
  }

  /**
   * Creates a builder for constructing a {@link ValueProperties} object with the given property defined.
   * 
   * @param propertyName name of the property to define, not {@code null}
   * @param propertyValues property values, not {@code null} and not containing {@code null}
   * @return the builder instance
   */
  public static Builder with(final String propertyName, final Collection<String> propertyValues) {
    return builder().with(propertyName, propertyValues);
  }

  /**
   * Creates a builder for constructing a {@link ValueProperties} object with the given property defined
   * as a wild-card.
   * 
   * @param propertyName name of the property to define, not {@code null}
   * @return the builder instance
   */
  public static Builder withAny(final String propertyName) {
    return builder().withAny(propertyName);
  }

  /**
   * Creates a builder for constructing a {@link ValueProperties} object with the given property defined
   * as optional.
   * 
   * @param propertyName name of the property to define, not {@code null}
   * @return the builder instance
   */
  public static Builder withOptional(final String propertyName) {
    return builder().withOptional(propertyName);
  }

  /**
   * Returns a builder pre-populated with the properties from this set.
   * 
   * @return the builder
   */
  public abstract Builder copy();

  /**
   * Returns an immutable set of defined property names.
   * 
   * @return the property names, not {@code null}
   */
  public abstract Set<String> getProperties();

  /**
   * Returns an immutable set of values for a given property name. If the name is not defined,
   * returns {@code null}. If the name has a wild-card value, the empty set is returned.
   * 
   * @param propertyName the name required, not {@code null}
   * @return the set of values, empty if wild-card, {@code null} if not defined
   */
  public abstract Set<String> getValues(String propertyName);

  /**
   * Indicates if a property may be omitted.
   * 
   * @param propertyName the name required, not {@code null}
   * @return {@code true} if the property is optional, {@code false} if it is not defined or required
   */
  public abstract boolean isOptional(String propertyName);

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
  public abstract boolean isSatisfiedBy(ValueProperties properties);

  /**
   * Produces a set of properties such that for any properties defined by the other, the intersection
   * of the property values is taken. Any properties defined in this set not but not in the other remain
   * untouched.
   * 
   * @param properties other property set to compose against, not {@code null}
   * @return the new set of properties, or this object if the composition result is equal
   */
  public abstract ValueProperties compose(ValueProperties properties);

  /**
   * Tests if the set of properties is strict. A property set is strict if there is only one value
   * for each property (or the property set is empty).
   * 
   * @return {@code true} if the property set is strict, {@code false} otherwise
   */
  public abstract boolean isStrict();

  /**
   * Tests if the set of properties is empty.
   * 
   * @return {@code true} if the property set is empty, {@code false} otherwise
   */
  public abstract boolean isEmpty();

  /**
   * Equivalent to calling {@code copy().withoutAny(propertyName).get()}.
   * 
   * @param propertyName property name to remove
   * @return a property set with the given property removed
   */
  public ValueProperties withoutAny(final String propertyName) {
    return copy().withoutAny(propertyName).get();
  }

  protected static int compareSet(final Set<String> s1, final Set<String> s2) {
    if (s1 == null) {
      if (s2 == null) {
        return 0;
      } else {
        return -1;
      }
    } else if (s2 == null) {
      return 1;
    }
    if (s1.isEmpty()) {
      if (s2.isEmpty()) {
        return 0;
      } else {
        return 1;
      }
    } else if (s2.isEmpty()) {
      return -1;
    }
    if (s1.size() < s2.size()) {
      return -1;
    } else if (s1.size() > s2.size()) {
      return 1;
    }
    List<String> sorted = new ArrayList<String>(Sets.symmetricDifference(s1, s2));
    Collections.sort(sorted);
    for (String s : sorted) {
      if (s1.contains(s)) {
        return -1;
      } else {
        return 1;
      }
    }
    return 0;
  }

  @Override
  public int compareTo(final ValueProperties valueProperties) {
    if (valueProperties == this) {
      return 0;
    }
    final Set<String> propThis = getProperties();
    final Set<String> propOther = valueProperties.getProperties();
    int c = compareSet(propThis, propOther);
    if (c != 0) {
      return c;
    }
    final List<String> sorted = new ArrayList<String>(propThis);
    Collections.sort(sorted);
    for (String property : sorted) {
      c = compareSet(getValues(property), valueProperties.getValues(property));
      if (c != 0) {
        return c;
      }
    }
    return 0;
  }

}
