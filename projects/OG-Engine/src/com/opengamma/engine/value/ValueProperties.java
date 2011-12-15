/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
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
import java.util.regex.Pattern;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;

import com.google.common.collect.Sets;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.PublicAPI;

/**
 * An immutable set of constraints on the values required, or properties of the value produced.
 * <p>
 * This class is immutable and thread-safe.
 * 
 * @see ValuePropertyNames
 */
@PublicAPI
public abstract class ValueProperties implements Serializable, Comparable<ValueProperties> {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  /**
   * Builder pattern for constructing {@link ValueProperties} objects.
   */
  public static final class Builder {
    /**
     * The required properties.
     */
    private final Map<String, Set<String>> _properties;
    /**
     * The optional properties.
     */
    private Set<String> _optional;

    /**
     * Creates an instance.
     */
    private Builder() {
      _properties = new HashMap<String, Set<String>>();
    }

    /**
     * Creates an instance.
     * 
     * @param properties  the required properties, not null
     * @param optional  the optional properties, not null
     */
    private Builder(final Map<String, Set<String>> properties, final Set<String> optional) {
      _properties = new HashMap<String, Set<String>>(properties);
      _optional = (optional != null) ? new HashSet<String>(optional) : null;
    }

    /**
     * Adds a property value to the builder.
     * <p>
     * If the property is already a wild-card, the builder is left unchanged.
     * 
     * @param propertyName  the name of the property, not null
     * @param propertyValue  the value to add, not null
     * @return {@code this} for chaining in the builder pattern, not null
     */
    public Builder with(String propertyName, final String propertyValue) {
      ArgumentChecker.notNull(propertyName, "propertyName");
      ArgumentChecker.notNull(propertyValue, "propertyValue");
      propertyName = ValueRequirement.getInterned(propertyName);
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
     * Adds property values to the builder.
     * <p>
     * If the property is already a wild-card, the builder is left unchanged.
     * 
     * @param propertyName  the name of the property, not null
     * @param propertyValues  the values to add, not null and not containing nulls
     * @return {@code this} for chaining in the builder pattern, not null
     */
    public Builder with(final String propertyName, final String... propertyValues) {
      ArgumentChecker.notNull(propertyValues, "propertyValues");
      return with(propertyName, Arrays.asList(propertyValues));
    }

    /**
     * Adds property values to the builder.
     * <p>
     * If the property is already a wild-card, the builder is left unchanged.
     * 
     * @param propertyName  the name of the property, not null
     * @param propertyValues  the values to add, not null and not containing nulls
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
     * Adds a wild-card property value.
     * <p>
     * If explicit values were previously set for the property, they are removed
     * to leave the wild-card definition.
     * 
     * @param propertyName  the name of the property, not null
     * @return {@code this} for chaining in the builder pattern, not null
     */
    public Builder withAny(final String propertyName) {
      ArgumentChecker.notNull(propertyName, "propertyName");
      _properties.put(propertyName.intern(), Collections.<String>emptySet());
      return this;
    }

    /**
     * Declares a property as optional when used as a constraint.
     * <p>
     * By default constraints are required, and can only be satisfied if the other property
     * set defines a matching value. If a constraint is optional the other set may define
     * a matching value, or have no definition for the property. If no explicit values
     * for the property are set with one of the other calls, the property will have a
     * wild-card value (i.e. as if {@link #withAny (String)} had been called.
     * 
     * @param propertyName  the name of the property, not null
     * @return {@code this} for chaining in the builder pattern, not null
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
     * @param propertyName  the name of the property, not null
     * @return {@code this} for chaining in the builder pattern, not null
     */
    public Builder withoutAny(final String propertyName) {
      ArgumentChecker.notNull(propertyName, "propertyName");
      _properties.remove(propertyName);
      if (_optional != null) {
        _optional.remove(propertyName);
        if (_optional.isEmpty()) {
          _optional = null;
        }
      }
      return this;
    }

    /**
     * Completes the builder, creating a {@code ValueProperties} instance based
     * on the current state of the builder.
     * 
     * @return the property set
     */
    public ValueProperties get() {
      if (_optional != null) {
        for (String optionalProperty : _optional) {
          if (!_properties.containsKey(optionalProperty)) {
            _properties.put(optionalProperty, Collections.<String>emptySet());
          }
        }
        return new ValuePropertiesImpl(new HashMap<String, Set<String>>(_properties), new HashSet<String>(_optional));
      } else {
        if (_properties.isEmpty()) {
          return EMPTY;
        }
        return new ValuePropertiesImpl(new HashMap<String, Set<String>>(_properties), Collections.<String>emptySet());
      }
    }

  }

  /**
   * A value properties implementation holding a set of properties.
   */
  public static final class ValuePropertiesImpl extends ValueProperties {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    /**
     * The properties.
     */
    private final Map<String, Set<String>> _properties;
    /**
     * The optional properties.
     */
    private final Set<String> _optional;

    /**
     * Creates an instance.
     * 
     * @param properties  the required properties, not null
     * @param optional  the optional properties, not null
     */
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
      nextProperty:
      // CSIGNORE [DVI-122]
      for (Map.Entry<String, Set<String>> property : _properties.entrySet()) {
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
        if (available == null) {
          // This property unchanged in output
          continue;
        }
        if (available.isEmpty()) {
          // This property different in output if optional here, and composed against a required
          if (isOptional(property.getKey()) && !properties.isOptional(property.getKey())) {
            return composeImpl(properties);
          }
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
      nextProperty:
      // CSIGNORE [DVI-122]
      for (Map.Entry<String, Set<String>> property : _properties.entrySet()) {
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
        // Preserve least optionality from property sets
        if (properties.isOptional(property.getKey()) && isOptional(property.getKey())) {
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
        // We've just built a map containing only the other property values, so possibly return that original
        if (properties instanceof ValuePropertiesImpl) {
          if (otherAvailable == ((ValuePropertiesImpl) properties)._properties.size()) {
            return properties;
          }
        }
      }
      return new ValuePropertiesImpl(Collections.unmodifiableMap(composed), (optional != null) ? Collections.unmodifiableSet(optional) : Collections.<String>emptySet());
    }

    @Override
    public ValueProperties intersect(final ValueProperties other) {
      // Our property values are present unless missing from the other set
      final Map<String, Set<String>> intersection = new HashMap<String, Set<String>>();
      Set<String> optional = null;
      for (Map.Entry<String, Set<String>> property : _properties.entrySet()) {
        final Set<String> otherValues = other.getValues(property.getKey());
        if (otherValues == null) {
          // Property not defined in the other set
          continue;
        }
        final Set<String> commonValues;
        if (otherValues.isEmpty()) {
          // Other set is wild-card, so take our values
          commonValues = property.getValue();
        } else {
          commonValues = Sets.intersection(property.getValue(), otherValues);
          if (commonValues.isEmpty()) {
            // No common values
            continue;
          }
        }
        intersection.put(property.getKey(), commonValues);
        // Preserve least optionality
        if (isOptional(property.getKey()) && other.isOptional(property.getKey())) {
          if (optional == null) {
            optional = new HashSet<String>();
          }
          optional.add(property.getKey());
        }
      }
      return new ValuePropertiesImpl(Collections.unmodifiableMap(intersection), (optional != null) ? Collections.unmodifiableSet(optional) : Collections.<String>emptySet());
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

    public static String toString(final Map<String, Set<String>> properties, final Set<String> optional, final boolean strict) {
      Pattern escapePattern = Pattern.compile("[=\\?\\[\\],\\\\]");
      final StringBuilder sb = new StringBuilder();
      if (strict) {
        sb.append("{");
      }
      boolean first = true;
      for (Map.Entry<String, Set<String>> property : properties.entrySet()) {
        if (first) {
          first = false;
        } else {
          sb.append(",");
        }
        sb.append(escape(escapePattern, property.getKey())).append("=");
        boolean isOptional = optional.contains(property.getKey());
        boolean grouped = strict || property.getValue().size() > 1 || isOptional;
        if (grouped) {
          sb.append("[");
        }
        boolean firstValue = true;
        for (String value : property.getValue()) {
          if (firstValue) {
            firstValue = false;
          } else {
            sb.append(",");
          }
          sb.append(escape(escapePattern, value));
        }
        if (grouped) {
          sb.append("]");
        }
        if (isOptional) {
          sb.append("?");
        }
      }
      if (strict) {
        sb.append("}");
      }
      return sb.toString();
    }

    @Override
    public String toSimpleString() {
      return toString(_properties, _optional, false);
    }

    @Override
    public String toString() {
      return toString(_properties, _optional, true);
    }
  }

  private static String escape(Pattern p, String s) {
    return p.matcher(s).replaceAll("\\\\$0");
  }

  /**
   * Parses value property strings of the forms:
   * <ul>
   *   <li> EMPTY
   *   <li> INFINITE
   *   <li> INFINITE-{name1,name2}
   *   <li> {name1=[value1,value2],name2=[value3]}
   * </ul>
   * These are intentionally the same as the forms generated by {@link #toString()}. For maximum flexibility, and
   * especially for user input, more abbreviated forms are also valid. In particular:
   * <ul>
   *   <li> Curly braces may be omitted
   *   <li> Square brackets around single values may be omitted
   *   <li> 'name1=[]' is same as 'name1'
   *   <li> Spaces are trimmed
   * </ul>
   * Escape sequences may be used for the following special characters: ',', '=', '[', ']', '?', '\' and ' '. An escape
   * sequence begins with '\'.
   * <p>
   * A null or empty input string is treated as the empty set of value properties.
   * 
   * @param s  the string to parse
   * @return  the value properties, not null
   */
  public static ValueProperties parse(String s) {
    // REVIEW jonathan 2011-05-11 -- this is bordering on being complex enough to write a grammar and auto-generate the
    // lexer, but it works and ValueProperties is unlikely to change.
    // REVIEW andrew 2011-08-08 -- even as a big fan of crazy-mad home brew state machines I'd use an auto-generated lexer
    if (StringUtils.isBlank(s) || EMPTY.toString().equals(s)) {
      return EMPTY;
    }
    s = s.trim();
    if (INFINITE.toString().equals(s)) {
      return INFINITE;
    }
    boolean nearlyInfinite = false;
    Builder builder = null;
    ValueProperties nearlyInfiniteResult = null;
    if (s.startsWith("INFINITE-")) {
      nearlyInfinite = true;
      s = s.substring(9, s.length());
      nearlyInfiniteResult = all();
    } else {
      builder = builder();
    }

    if (s.charAt(0) == '{' && s.charAt(s.length() - 1) == '}') {
      // Strip away any curly brace wrappers
      s = s.substring(1, s.length() - 1);
    }

    int pos = 0;
    boolean isOptional = false;
    StringBuilder substring = new StringBuilder();
    String name = null;
    Set<String> values = new HashSet<String>();
    boolean bracketedValue = false;
    boolean inValue = false;
    while (pos <= s.length()) {
      char next = pos < s.length() ? s.charAt(pos) : 0;
      if (next == '\\') { // Begin escape sequence
        pos++;
        if (pos < s.length()) {
          char escapedCharacter = s.charAt(pos);
          if (escapedCharacter == '\\' || escapedCharacter == ',' || escapedCharacter == '='
              || escapedCharacter == '[' || escapedCharacter == ']' || escapedCharacter == '?'
              || escapedCharacter == ' ') {
            substring.append(escapedCharacter);
          } else {
            throw new IllegalArgumentException("Unrecognised escape sequence: \\" + escapedCharacter);
          }
        } else {
          throw new IllegalArgumentException("Unexpected end of ValueProperties string: " + s);
        }
      } else if (next == '=') { // Separator between name and values
        if (inValue) {
          throw new IllegalArgumentException("Unexpected '=' at position " + pos);
        }
        name = substring.toString();
        substring = new StringBuilder();
        inValue = true;
        if (pos + 1 < s.length() && s.charAt(pos + 1) == '[') {
          bracketedValue = true;
          pos++;
        }
      } else if (next == ']') { // End of values
        inValue = false;
        if (s.length() > pos + 1 && s.charAt(pos + 1) == '?') {
          isOptional = true;
          pos++;
        }
      } else if (next == ',' || next == 0) { // Separator between values in a group or between properties
        if (substring.length() > 0) {
          if (name == null) {
            name = substring.toString();
          } else {
            values.add(substring.toString());
          }
          substring = new StringBuilder();
        }
        if (!inValue || !bracketedValue) {
          // End of a property
          if (values.isEmpty()) {
            if (nearlyInfinite) {
              nearlyInfiniteResult = nearlyInfiniteResult.withoutAny(name);
            } else {
              builder.withAny(name);
            }
          } else {
            if (nearlyInfinite) {
              throw new IllegalArgumentException("Property values not supported in nearly infinite ValueProperties. Found: " + values);
            }
            builder.with(name, values);
          }
          if (isOptional) {
            builder.withOptional(name);
          }
          name = null;
          values = new HashSet<String>();
          isOptional = false;
          inValue = false;
          bracketedValue = false;
        }
      } else if (next != ' ') {
        substring.append(next);
      }
      pos++;
    }

    if (name != null || substring.length() > 0) {
      throw new IllegalArgumentException("Unexpected end of ValueProperties string: " + s);
    }

    return nearlyInfinite ? nearlyInfiniteResult : builder.get();
  }

  /**
   * A value properties implementation representing a nearly infinite property set.
   */
  public static final class NearlyInfinitePropertiesImpl extends ValueProperties {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    /**
     * The set of properties not included.
     */
    private final Set<String> _without;

    /**
     * Gets the properties not included
     * @return the properties not included
     */
    public Set<String> getWithout() {
      return _without;
    }

    /**
     * Creates an instance.
     * 
     * @param without  the set of properties not included, not null
     */
    private NearlyInfinitePropertiesImpl(final Set<String> without) {
      _without = without;
    }

    @Override
    public ValueProperties compose(ValueProperties properties) {
      // Yields the same
      return this;
    }

    @Override
    public ValueProperties intersect(final ValueProperties properties) {
      // Yields the same unless we are intersecting against another "nearly infinite" set
      if (properties instanceof NearlyInfinitePropertiesImpl) {
        final NearlyInfinitePropertiesImpl other = (NearlyInfinitePropertiesImpl) properties;
        // Intersection is the UNION of the things we DON'T contain subtracted from the INFINITE set
        return new NearlyInfinitePropertiesImpl(Collections.unmodifiableSet(Sets.union(_without, other._without)));
      } else {
        return this;
      }
    }

    @Override
    public Builder copy() {
      throw new UnsupportedOperationException("Cannot copy the nearly infinite set");
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
    public String toSimpleString() {
      return toString();
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
   * The infinite property set.
   */
  private static final ValueProperties INFINITE = new InfinitePropertiesImpl();

  /**
   * A value properties implementation representing an infinite property set.
   */
  public static final class InfinitePropertiesImpl extends ValueProperties {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    /**
     * Creates an instance.
     */
    private InfinitePropertiesImpl() {
    }

    @Override
    public ValueProperties compose(final ValueProperties properties) {
      // Composition yields the infinite set
      return this;
    }
    
    @Override
    public ValueProperties intersect(final ValueProperties properties) {
      // Intersection yields the other set
      return properties;
    }

    @Override
    public Builder copy() {
      throw new UnsupportedOperationException("Cannot copy the infinite set");
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
    public String toSimpleString() {
      return toString();
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

  }

  /**
   * The empty set.
   */
  private static final ValueProperties EMPTY = new EmptyPropertiesImpl();

  /**
   * A value properties implementation representing an empty property set.
   */
  private static final class EmptyPropertiesImpl extends ValueProperties {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    @Override
    public ValueProperties compose(ValueProperties properties) {
      // The only thing satisfied by the empty set is the empty set, or a set with only optional properties
      return this;
    }
    
    @Override
    public ValueProperties intersect(final ValueProperties properties) {
      // Nothing to intersect with, so still empty
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

    @Override
    public String toSimpleString() {
      return toString();
    }
  }

  /**
   * Returns the empty property set, typically indicating no value constraints.
   * 
   * @return the empty property set, not null
   */
  public static ValueProperties none() {
    return EMPTY;
  }

  /**
   * Returns a property set that simulates all possible properties.
   * <p>
   * This should be used with caution. Well written functions should build a
   * property set explicitly stating the properties recognized.
   * 
   * @return the "infinite" property set, not null
   */
  public static ValueProperties all() {
    return INFINITE;
  }

  /**
   * Creates a builder for constructing value properties.
   * <p>
   * {@code ValueProperties} is immutable, but the builder is mutable allowing
   * instances to be created efficiently.
   * 
   * @return the builder, not null
   */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * Creates a builder for constructing value properties with the given property defined.
   * 
   * @param propertyName  the name of the property to define, not null
   * @param propertyValue  the property value, not null
   * @return the builder, not null
   */
  public static Builder with(final String propertyName, final String propertyValue) {
    return builder().with(propertyName, propertyValue);
  }

  /**
   * Creates a builder for constructing value properties with the given property defined.
   * 
   * @param propertyName  the name of the property to define, not null
   * @param propertyValues  the property values, not null and not containing null
   * @return the builder instance
   */
  public static Builder with(final String propertyName, final String... propertyValues) {
    return builder().with(propertyName, propertyValues);
  }

  /**
   * Creates a builder for constructing value properties with the given property defined.
   * 
   * @param propertyName  the name of the property to define, not null
   * @param propertyValues  the property values, not null and not containing null
   * @return the builder instance
   */
  public static Builder with(final String propertyName, final Collection<String> propertyValues) {
    return builder().with(propertyName, propertyValues);
  }

  /**
   * Creates a builder for constructing value properties with the given property defined as a wild-card.
   * 
   * @param propertyName  the name of the property to define, not null
   * @return the builder instance
   */
  public static Builder withAny(final String propertyName) {
    return builder().withAny(propertyName);
  }

  /**
   * Creates a builder for constructing value properties with the given property defined as optional.
   * 
   * @param propertyName  the name of the property to define, not null
   * @return the builder instance
   */
  public static Builder withOptional(final String propertyName) {
    return builder().withOptional(propertyName);
  }

  /**
   * Returns a builder pre-populated with the properties from this set.
   * 
   * @return the builder, not null
   */
  public abstract Builder copy();

  /**
   * Gets an immutable set of the defined property names.
   * 
   * @return the property names, null if there are no properties, or the empty set for an infinite(ish) set 
   */
  public abstract Set<String> getProperties();

  /**
   * Gets an immutable set of values for a given property name.
   * <p>
   * If the name is not defined null is returned.
   * If the name has a wild-card value, the empty set is returned.
   * 
   * @param propertyName  the name required, not null
   * @return the set of values, empty if wild-card, null if not defined
   */
  public abstract Set<String> getValues(String propertyName);

  /**
   * Checks if a property may be omitted.
   * 
   * @param propertyName  the name required, not null
   * @return true if the property is optional, false if it is not defined or required
   */
  public abstract boolean isOptional(String propertyName);

  /**
   * Checks if this set of properties can be satisfied by the other set.
   * <p>
   * An individual property is satisfied if one of the following is true:
   * <ul>
   * <li>it is a wild-card and the other property set has a definition for it
   * <li>the other property set has a wild-card definition for it
   * <li>the other property set provides at least one of the possible property values
   * </ul>
   * The property set is satisfied if each of the individual properties can be satisfied. 
   * 
   * @param properties  the other property set to check against, not null
   * @return true if this set of properties can be satisfied by the other set
   */
  public abstract boolean isSatisfiedBy(ValueProperties properties);

  /**
   * Composes two value properties by taking a "left" intersection.
   * <p>
   * This produces a set of properties such that for any properties defined by the other,
   * the intersection of the property values is taken. Any properties defined in this set
   * but not in the other remain untouched.
   * 
   * @param properties  the other property set to compose against, not null
   * @return the new set of properties, or this object if the composition result is equal, not null
   */
  public abstract ValueProperties compose(ValueProperties properties);

  /**
   * Produces the strict intersection of two property sets.
   * <p>
   * This produces a set of properties such that only properties defined in both this and
   * the other are present in the output. For these, the intersection of common values is
   * available for each property. If there are no common property values, the property is
   * ommited from the result.
   * 
   * @param properties the other property set to compose against, not null
   * @return the new set of properties, or this object if the intersection result is equal, not null
   */
  public abstract ValueProperties intersect(ValueProperties properties);

  /**
   * Checks if the set of properties is strict.
   * <p>
   * A property set is strict if there is only one value for each property
   * or the property set is empty.
   * 
   * @return true if the property set is strict
   */
  public abstract boolean isStrict();

  /**
   * Checks if the set of properties is empty.
   * 
   * @return true if the property set is empty
   */
  public abstract boolean isEmpty();

  /**
   * Equivalent to calling {@code copy().withoutAny(propertyName).get()}.
   * 
   * @param propertyName  the property name to remove, not null
   * @return a value properties with the given property removed, not null
   */
  public ValueProperties withoutAny(final String propertyName) {
    return copy().withoutAny(propertyName).get();
  }

  /**
   * Returns a simple string representation of the {@link ValueProperties} instance. This simple representation omits
   * unnecessary brackets for better readability. The output remains valid as the input to {@link #parse(String)}.
   * 
   * @return a simple string representation
   */
  public abstract String toSimpleString();

  /**
   * Compares two sets.
   * 
   * @param s1  the first set, may be null
   * @param s2  the second set, may be null
   * @return negative if the first is less, zero if equal, positive if greater
   */
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
