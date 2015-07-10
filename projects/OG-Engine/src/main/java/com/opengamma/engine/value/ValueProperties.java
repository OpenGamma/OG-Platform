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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.wire.types.FudgeWireType;

import com.google.common.collect.Sets;
import com.opengamma.engine.fudgemsg.ValuePropertiesFudgeBuilder;
import com.opengamma.engine.value.properties.AbstractValueProperty;
import com.opengamma.engine.value.properties.AdditivePropertiesBuilder;
import com.opengamma.engine.value.properties.PropertyNameSet;
import com.opengamma.engine.value.properties.SubtractivePropertiesBuilder;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.PublicAPI;

/**
 * An immutable set of constraints on the values required, or properties of the value produced.
 * <p>
 * This class is immutable and thread-safe. The builders used to create instances are not thread-safe.
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
  public abstract static class Builder {

    /**
     * Creates a new instance, backed by the property array.
     * 
     * @param properties the property hash, never null. This will be owned by the object and must not be modified after calling.
     * @return the new instance
     */
    protected static ValueProperties createAdditive(final AbstractValueProperty[] properties) {
      return new SimpleValueProperties(properties);
    }

    /**
     * Creates a new instance, backed by the given set.
     * 
     * @param properties the set of missing properties. This will be owned by the object and must not be modified after calling.
     * @return the new instance
     */
    protected static ValueProperties createSubtractive(final Set<String> properties) {
      return new NearInfiniteValueProperties(properties);
    }

    /**
     * Adds a property value to the builder.
     * <p>
     * If the property is already a wild-card, the builder is left unchanged.
     * 
     * @param propertyName the name of the property, not null
     * @param propertyValue the value to add, not null
     * @return {@code this} for chaining in the builder pattern, not null
     */
    public abstract Builder with(String propertyName, String propertyValue);

    /**
     * Adds property values to the builder.
     * <p>
     * If the property is already a wild-card, the builder is left unchanged.
     * 
     * @param propertyName the name of the property, not null
     * @param propertyValues the values to add, not null and not containing nulls
     * @return {@code this} for chaining in the builder pattern, not null
     */
    public abstract Builder with(String propertyName, String... propertyValues);

    /**
     * Adds property values to the builder.
     * <p>
     * If the property is already a wild-card, the builder is left unchanged.
     * 
     * @param propertyName the name of the property, not null
     * @param propertyValues the values to add, not null or empty, and not containing nulls
     * @return the builder instance
     */
    public abstract Builder with(String propertyName, Collection<String> propertyValues);

    /**
     * Adds a wild-card property value.
     * <p>
     * If explicit values were previously set for the property, they are removed to leave the wild-card definition.
     * 
     * @param propertyName the name of the property, not null
     * @return {@code this} for chaining in the builder pattern, not null
     */
    public abstract Builder withAny(String propertyName);

    /**
     * Declares a property as optional when used as a constraint.
     * <p>
     * By default constraints are required, and can only be satisfied if the other property set defines a matching value. If a constraint is optional the other set may define a matching value, or have
     * no definition for the property. If no explicit values for the property are set with one of the other calls, the property will have a wild-card value (i.e. as if {@link #withAny (String)} had
     * been called.
     * 
     * @param propertyName the name of the property, not null
     * @return {@code this} for chaining in the builder pattern, not null
     */
    public abstract Builder withOptional(String propertyName);

    /**
     * Clears the optional flag of a property.
     * 
     * @param propertyName the name of the property, not null
     * @return {@code this} for chaining in the builder pattern, not null
     */
    public abstract Builder notOptional(String propertyName);

    /**
     * Removes a property from the builder definition.
     * 
     * @param propertyName the name of the property, not null
     * @return {@code this} for chaining in the builder pattern, not null
     */
    public abstract Builder withoutAny(String propertyName);

    /**
     * Completes the builder, creating a {@code ValueProperties} instance based on the current state of the builder.
     * 
     * @return the property set
     */
    public abstract ValueProperties get();

    /**
     * Creates a deep copy of the builder.
     * 
     * @return a copy of the builder
     */
    public abstract Builder copy();

  }

  private static String escape(Pattern p, String s) {
    return p.matcher(s).replaceAll("\\\\$0");
  }

  /**
   * Compares two sets.
   * 
   * @param s1 the first set, may be null
   * @param s2 the second set, may be null
   * @return negative if the first is less, zero if equal, positive if greater
   */
  private static int compareSet(final Set<String> s1, final Set<String> s2) {
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

  /**
   * An implementation holding a simple set of properties.
   */
  private static final class SimpleValueProperties extends ValueProperties {

    private static final long serialVersionUID = 1L;

    private final AbstractValueProperty[] _properties;

    private final int _hashCode;

    /**
     * Creates a new instance, backed by the property array.
     * 
     * @param properties the property hash, never null. This will be owned by the object and must not be modified after calling.
     */
    private SimpleValueProperties(final AbstractValueProperty[] properties) {
      _properties = properties;
      _hashCode = Arrays.hashCode(properties);
    }

    private boolean isLongerPropertyCount(final SimpleValueProperties other) {
      if (_properties.length < other._properties.length) {
        return true;
      }
      if (_properties.length > other._properties.length) {
        return false;
      }
      int countSelf = 0;
      for (AbstractValueProperty property : _properties) {
        for (; property != null; property = property.getNext()) {
          countSelf++;
        }
      }
      for (AbstractValueProperty property : other._properties) {
        for (; property != null; property = property.getNext()) {
          countSelf--;
        }
      }
      return countSelf > 0;
    }

    // internal operations

    @Override
    /* package */boolean isSatisfySimple(final SimpleValueProperties other) {
      for (AbstractValueProperty property : other._properties) {
        propertyLoop: for (; property != null; property = property.getNext()) { //CSIGNORE
          final int hc = property.getKey().hashCode() & 0x7FFFFFFF;
          final int index = hc % _properties.length;
          AbstractValueProperty bucket = _properties[index];
          while (bucket != null) {
            if (bucket.getKey() == property.getKey()) {
              if (bucket.isSatisfyValue(property)) {
                // Satisfied
                continue propertyLoop;
              } else {
                // Didn't satisfy
                return false;
              }
            }
            bucket = bucket.getNext();
          }
          if (!property.isOptional()) {
            // Didn't define non-optional
            return false;
          }
        }
      }
      return true;
    }

    @Override
    /* package */boolean isSatisfyNearInfinite(final NearInfiniteValueProperties other) {
      // Can never satisfy
      return false;
    }

    @Override
    /* package */Set<String> getUnsatisfiedFromSimple(final SimpleValueProperties other) {
      Set<String> unsatisfied = null;
      for (AbstractValueProperty property : other._properties) {
        propertyLoop: for (; property != null; property = property.getNext()) { //CSIGNORE
          final int hc = property.getKey().hashCode() & 0x7FFFFFFF;
          final int index = hc % _properties.length;
          AbstractValueProperty bucket = _properties[index];
          while (bucket != null) {
            if (bucket.getKey() == property.getKey()) {
              if (!bucket.isSatisfyValue(property)) {
                // Didn't satisfy
                if (unsatisfied == null) {
                  unsatisfied = new HashSet<String>();
                }
                unsatisfied.add(property.getKey());
              }
              continue propertyLoop;
            }
            bucket = bucket.getNext();
          }
          if (!property.isOptional()) {
            // Didn't define non-optional
            if (unsatisfied == null) {
              unsatisfied = new HashSet<String>();
            }
            unsatisfied.add(property.getKey());
          }
        }
      }
      return unsatisfied;
    }

    @Override
    /* package */Set<String> getUnsatisfiedFromNearInfinite(final NearInfiniteValueProperties other) {
      // Everything from (near) infinite set is unsatisfied
      return Collections.emptySet();
    }

    @Override
    /* package */Set<String> getUnsatisfiedFromInfinite() {
      // Everything from infinite set is unsatisfied
      return Collections.emptySet();
    }

    @Override
    /* package */ValueProperties rightIntersectSimple(final SimpleValueProperties other) {
      final AdditivePropertiesBuilder builder = new AdditivePropertiesBuilder(other._properties);
      for (AbstractValueProperty property : _properties) {
        for (; property != null; property = property.getNext()) {
          builder.compose(property);
        }
      }
      if (builder.hasLocalCopy()) {
        return builder.get();
      } else {
        return other;
      }
    }

    private ValueProperties intersectSimpleImpl(final SimpleValueProperties other) {
      final AdditivePropertiesBuilder builder = new AdditivePropertiesBuilder();
      boolean equalsThis = true;
      for (AbstractValueProperty property : _properties) {
        propertyLoop: for (; property != null; property = property.getNext()) { //CSIGNORE
          final int hc = property.getKey().hashCode() & 0x7FFFFFFF;
          final int index = hc % other._properties.length;
          AbstractValueProperty bucket = other._properties[index];
          while (bucket != null) {
            if (bucket.getKey() == property.getKey()) {
              final AbstractValueProperty intersect = bucket.intersectValues(property);
              if (intersect != null) {
                if (intersect != property) {
                  equalsThis = false;
                }
                builder.union(intersect);
              } else {
                equalsThis = false;
              }
              continue propertyLoop;
            }
            bucket = bucket.getNext();
          }
          equalsThis = false;
        }
      }
      if (equalsThis) {
        return this;
      } else {
        return builder.get();
      }
    }

    @Override
    /* package */ValueProperties intersectSimple(final SimpleValueProperties other) {
      if (isLongerPropertyCount(other)) {
        return other.intersectSimpleImpl(this);
      } else {
        return intersectSimpleImpl(other);
      }
    }

    @Override
    /* package */ValueProperties intersectNearInfinite(final NearInfiniteValueProperties other) {
      // Intersection is without anything defined as absent and nothing optional
      AbstractValueProperty[] result = null;
      boolean[] copied = null;
      int numEntries = 0;
      for (int i = 0; i < _properties.length; i++) {
        AbstractValueProperty property = _properties[i];
        for (; property != null; property = property.getNext()) {
          if (other._properties.contains(property.getKey())) {
            if (result == null) {
              result = Arrays.copyOf(_properties, _properties.length);
              copied = new boolean[result.length];
            }
            if (!copied[i]) {
              // Copy the bucket, and advance to this position in the copy
              final String search = property.getKey();
              property = result[i].copy();
              result[i] = property;
              copied[i] = true;
              while (property.getKey() != search) {
                property = property.getNext();
              }
            }
            // Remove this item
            result[i] = result[i].remove(property.getKey());
            continue;
          }
          numEntries++;
          if (property.isOptional()) {
            if (result == null) {
              result = Arrays.copyOf(_properties, _properties.length);
              copied = new boolean[result.length];
            }
            if (!copied[i]) {
              // Copy the bucket, and advance to this position in the copy
              final String search = property.getKey();
              property = result[i].copy();
              result[i] = property;
              copied[i] = true;
              while (property.getKey() != search) {
                property = property.getNext();
              }
            }
            // Mark this item non-optional
            property.setOptional(false);
          }
        }
      }
      if (result != null) {
        // Created an intersection
        final int desiredSize = AbstractValueProperty.getDesiredHashSize(numEntries);
        if (desiredSize != result.length) {
          final AbstractValueProperty[] rehashed = new AbstractValueProperty[desiredSize];
          AbstractValueProperty.rehash(result, rehashed, copied, new boolean[desiredSize]);
          result = rehashed;
        }
        return new SimpleValueProperties(result);
      }
      // No change
      return this;
    }

    @Override
    /* package */ValueProperties intersectInfinite() {
      // Intersection is these properties with no optional values
      AbstractValueProperty[] result = null;
      for (int i = 0; i < _properties.length; i++) {
        AbstractValueProperty property = _properties[i];
        boolean copied = false;
        for (; property != null; property = property.getNext()) {
          if (property.isOptional()) {
            if (result == null) {
              result = Arrays.copyOf(_properties, _properties.length);
            }
            if (!copied) {
              // Copy the bucket, and advance to this position in the copy
              final String search = property.getKey();
              property = result[i].copy();
              result[i] = property;
              copied = true;
              while (property.getKey() != search) {
                property = property.getNext();
              }
            }
            property.setOptional(false);
          }
        }
      }
      if (result != null) {
        // Create a copy
        return new SimpleValueProperties(result);
      }
      // Didn't have anything optional
      return this;
    }

    private ValueProperties unionSimpleImpl(final SimpleValueProperties other) {
      final AdditivePropertiesBuilder builder = new AdditivePropertiesBuilder(other._properties);
      for (AbstractValueProperty property : _properties) {
        for (; property != null; property = property.getNext()) {
          builder.union(property);
        }
      }
      if (builder.hasLocalCopy()) {
        return builder.get();
      } else {
        return this;
      }
    }

    @Override
    /* package */ValueProperties unionSimple(final SimpleValueProperties other) {
      if (isLongerPropertyCount(other)) {
        return other.unionSimpleImpl(this);
      } else {
        return unionSimpleImpl(other);
      }
    }

    @Override
    /* package */ValueProperties unionNearInfinite(final NearInfiniteValueProperties other) {
      return other.unionSimple(this);
    }

    @Override
    /* package */int compareToSimple(final SimpleValueProperties other) {
      final int r = _hashCode - other._hashCode;
      if (r != 0) {
        return r;
      }
      final Set<String> propThis = getProperties();
      final Set<String> propOther = other.getProperties();
      int c = compareSet(propThis, propOther);
      if (c != 0) {
        return c;
      }
      final List<String> sorted = new ArrayList<String>(propThis);
      Collections.sort(sorted);
      for (String property : sorted) {
        c = compareSet(getValues(property), other.getValues(property));
        if (c != 0) {
          return c;
        }
      }
      return 0;
    }

    @Override
    /* package */int compareToNearInfinite(final NearInfiniteValueProperties other) {
      // Simple properties always before near-infinite
      return -1;
    }

    // public API methods

    @Override
    public Builder copy() {
      return new AdditivePropertiesBuilder(_properties);
    }

    @Override
    public Set<String> getProperties() {
      return new PropertyNameSet(_properties);
    }

    @Override
    public boolean isDefined(final String propertyName) {
      final int hc = propertyName.hashCode() & 0x7FFFFFFF;
      final int i = hc % _properties.length;
      AbstractValueProperty property = _properties[i];
      while (property != null) {
        if (propertyName.equals(property.getKey())) {
          return true;
        }
        property = property.getNext();
      }
      return false;
    }

    @Override
    public Set<String> getValues(final String propertyName) {
      final int hc = propertyName.hashCode() & 0x7FFFFFFF;
      final int i = hc % _properties.length;
      final AbstractValueProperty property = _properties[i];
      if (property != null) {
        return property.getValues(propertyName);
      } else {
        return null;
      }
    }

    @Override
    public String getStrictValue(final String propertyName) {
      final int hc = propertyName.hashCode() & 0x7FFFFFFF;
      final int i = hc % _properties.length;
      final AbstractValueProperty property = _properties[i];
      if (property != null) {
        return property.getStrictValue(propertyName);
      } else {
        return null;
      }
    }

    @Override
    public String getSingleValue(final String propertyName) {
      final int hc = propertyName.hashCode() & 0x7FFFFFFF;
      final int i = hc % _properties.length;
      final AbstractValueProperty property = _properties[i];
      if (property != null) {
        return property.getSingleValue(propertyName);
      } else {
        return null;
      }
    }

    @Override
    public boolean isOptional(final String propertyName) {
      final int hc = propertyName.hashCode() & 0x7FFFFFFF;
      final int i = hc % _properties.length;
      final AbstractValueProperty property = _properties[i];
      if (property != null) {
        return property.isOptional(propertyName);
      } else {
        return false;
      }
    }

    @Override
    public boolean isSatisfiedBy(final ValueProperties properties) {
      if (properties == this) {
        return true;
      }
      return properties.isSatisfySimple(this);
    }

    @Override
    public Set<String> getUnsatisfied(final ValueProperties properties) {
      if (properties == this) {
        return null;
      }
      return properties.getUnsatisfiedFromSimple(this);
    }

    @Override
    public ValueProperties compose(final ValueProperties properties) {
      if (properties == this) {
        return this;
      }
      return properties.rightIntersectSimple(this);
    }

    @Override
    public ValueProperties intersect(final ValueProperties properties) {
      if (properties == this) {
        return this;
      }
      return properties.intersectSimple(this);
    }

    @Override
    public ValueProperties union(final ValueProperties properties) {
      if (properties == this) {
        return this;
      }
      return properties.unionSimple(this);
    }

    @Override
    public boolean isStrict() {
      for (AbstractValueProperty property : _properties) {
        for (; property != null; property = property.getNext()) {
          if (property.getStrict() == null) {
            return false;
          }
        }
      }
      return true;
    }

    @Override
    public boolean isEmpty() {
      return false;
    }

    @Override
    public ValueProperties withoutAny(final String propertyName) {
      return copy().withoutAny(propertyName).get();
    }

    private String toString(final boolean strict) {
      final StringBuilder sb = new StringBuilder();
      Pattern escapePattern = Pattern.compile("[=\\?\\[\\],\\\\ ]");
      boolean first = true;
      if (strict) {
        sb.append('{');
      }
      for (AbstractValueProperty property : _properties) {
        for (; property != null; property = property.getNext()) {
          if (first) {
            first = false;
          } else {
            sb.append(",");
          }
          sb.append(escape(escapePattern, property.getKey())).append("=");
          boolean grouped = strict || property.getValues().size() > 1 || property.isOptional();
          if (grouped) {
            sb.append("[");
          }
          boolean firstValue = true;
          for (String value : property.getValues()) {
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
          if (property.isOptional()) {
            sb.append("?");
          }
        }
      }
      if (strict) {
        sb.append('}');
      }
      return sb.toString();
    }

    @Override
    public String toString() {
      return toString(true);
    }

    @Override
    public String toSimpleString() {
      return toString(false);
    }

    @Override
    public int compareTo(final ValueProperties other) {
      if (other == this) {
        return 0;
      }
      return -other.compareToSimple(this);
    }

    @Override
    public int hashCode() {
      return _hashCode;
    }

    @Override
    public boolean equals(final Object o) {
      if (o == this) {
        return true;
      }
      if (!(o instanceof SimpleValueProperties)) {
        return false;
      }
      final SimpleValueProperties other = (SimpleValueProperties) o;
      if (other._hashCode != _hashCode) {
        return false;
      }
      return Arrays.equals(_properties, other._properties);
    }

    @Override
    public void toFudgeMsg(final MutableFudgeMsg msg) {
      final MutableFudgeMsg subMsg = msg.addSubMessage(ValuePropertiesFudgeBuilder.WITH_FIELD, null);
      for (AbstractValueProperty property : _properties) {
        for (; property != null; property = property.getNext()) {
          property.toFudgeMsg(subMsg);
        }
      }
    }

  }

  /**
   * An implementation representing the empty property set.
   */
  private static final class EmptyValueProperties extends ValueProperties {

    private static final long serialVersionUID = 1L;

    // internal operations

    @Override
    /* package */boolean isSatisfySimple(final SimpleValueProperties other) {
      // Can only satisfy if the other has only optional properties
      for (AbstractValueProperty property : other._properties) {
        for (; property != null; property = property.getNext()) {
          if (!property.isOptional()) {
            return false;
          }
        }
      }
      return true;
    }

    @Override
    /* package */boolean isSatisfyNearInfinite(final NearInfiniteValueProperties other) {
      // Can't satisfy
      return false;
    }

    @Override
    /* package */Set<String> getUnsatisfiedFromSimple(final SimpleValueProperties other) {
      // Everything is unsatisfied
      return other.getProperties();
    }

    @Override
    /* package */Set<String> getUnsatisfiedFromNearInfinite(final NearInfiniteValueProperties other) {
      // Everything in the (near) infinite set is unsatisfied by us
      return Collections.emptySet();
    }

    @Override
    /* package */Set<String> getUnsatisfiedFromInfinite() {
      // Everything in the infinite set is unsatisfied by us
      return Collections.emptySet();
    }

    @Override
    /* package */ValueProperties rightIntersectSimple(final SimpleValueProperties other) {
      // Right intersection is the other set
      return other;
    }

    @Override
    /* package */ValueProperties intersectSimple(final SimpleValueProperties other) {
      // Intersection is always the empty set
      return this;
    }

    @Override
    /* package */ValueProperties intersectNearInfinite(final NearInfiniteValueProperties other) {
      // Intersection is always the empty set
      return this;
    }

    @Override
    /* package */ValueProperties intersectInfinite() {
      // Intersection is always the empty set
      return this;
    }

    @Override
    /* package */ValueProperties unionSimple(final SimpleValueProperties other) {
      // Union is always the other set
      return other;
    }

    @Override
    /* package */ValueProperties unionNearInfinite(final NearInfiniteValueProperties other) {
      // Union is always the other set
      return other;
    }

    @Override
    /* package */int compareToSimple(final SimpleValueProperties other) {
      // Empty is always before anything else
      return -1;
    }

    @Override
    /* package */int compareToNearInfinite(final NearInfiniteValueProperties other) {
      // Empty is always before anything else
      return -1;
    }

    // public API methods

    @Override
    public Builder copy() {
      return new AdditivePropertiesBuilder();
    }

    @Override
    public Set<String> getProperties() {
      // No properties
      return null;
    }

    @Override
    public boolean isDefined(final String propertyName) {
      // No properties defined
      return false;
    }

    @Override
    public Set<String> getValues(final String propertyName) {
      // No properties defined
      return null;
    }

    @Override
    public String getStrictValue(final String propertyName) {
      // Nothing defined
      return null;
    }

    @Override
    public String getSingleValue(final String propertyName) {
      // Nothing defined
      return null;
    }

    @Override
    public boolean isOptional(final String propertyName) {
      // Nothing defined, so nothing optional
      return false;
    }

    @Override
    public boolean isSatisfiedBy(final ValueProperties properties) {
      // Empty set is satisfied by everything
      return true;
    }

    @Override
    public Set<String> getUnsatisfied(final ValueProperties properties) {
      // Always satisfied
      return null;
    }

    @Override
    public ValueProperties compose(final ValueProperties properties) {
      // Left intersection is always empty
      return this;
    }

    @Override
    public ValueProperties intersect(final ValueProperties properties) {
      // Intersection is always empty
      return this;
    }

    @Override
    public ValueProperties union(final ValueProperties properties) {
      // Union is always the other set
      return properties;
    }

    @Override
    public boolean isStrict() {
      return true;
    }

    @Override
    public boolean isEmpty() {
      return true;
    }

    @Override
    public ValueProperties withoutAny(final String propertyName) {
      // No-op as we don't contain the property
      return this;
    }

    @Override
    public String toString() {
      return "EMPTY";
    }

    @Override
    public String toSimpleString() {
      return toString();
    }

    @Override
    public int compareTo(final ValueProperties other) {
      if (other == this) {
        return 0;
      }
      // Empty is always before anything else
      return -1;
    }

    @Override
    public void toFudgeMsg(final MutableFudgeMsg msg) {
      // No-op; empty Fudge message
    }

    private Object readResolve() throws Exception {
      return EMPTY;
    }

  }

  /**
   * An implementation representing the infinite property set.
   */
  private static final class InfiniteValueProperties extends ValueProperties {

    private static final long serialVersionUID = 1L;

    // internal operations

    @Override
    /* package */boolean isSatisfySimple(final SimpleValueProperties other) {
      // Infinite properties can satisfy anything
      return true;
    }

    @Override
    /* package */boolean isSatisfyNearInfinite(final NearInfiniteValueProperties other) {
      // Infinite properties can satisfy anything
      return true;
    }

    @Override
    /* package */Set<String> getUnsatisfiedFromSimple(final SimpleValueProperties other) {
      // Can satisfy anything
      return null;
    }

    @Override
    /* package */Set<String> getUnsatisfiedFromNearInfinite(final NearInfiniteValueProperties other) {
      // Can satisfy anything
      return null;
    }

    @Override
    /* package */Set<String> getUnsatisfiedFromInfinite() {
      // Can satisfy anything
      return null;
    }

    @Override
    /* package */ValueProperties rightIntersectSimple(final SimpleValueProperties other) {
      // Right intersection is the other set but without anything optional
      return other.intersectInfinite();
    }

    @Override
    /* package */ValueProperties intersectSimple(final SimpleValueProperties other) {
      // Intersection is the other set but without anything optional
      return other.intersectInfinite();
    }

    @Override
    /* package */ValueProperties intersectNearInfinite(final NearInfiniteValueProperties other) {
      // Intersection is the near-infinite set
      return other;
    }

    @Override
    /* package */ValueProperties intersectInfinite() {
      // Intersection with self
      return this;
    }

    @Override
    /* package */ValueProperties unionSimple(final SimpleValueProperties other) {
      // Union is infinite
      return this;
    }

    @Override
    /* package */ValueProperties unionNearInfinite(final NearInfiniteValueProperties other) {
      // Union is infinite
      return this;
    }

    @Override
    /* package */int compareToSimple(final SimpleValueProperties other) {
      // Infinite is always after anything else
      return 1;
    }

    @Override
    /* package */int compareToNearInfinite(final NearInfiniteValueProperties other) {
      // Infinite is always after anything else
      return 1;
    }

    // public API methods

    @Override
    public Builder copy() {
      return new SubtractivePropertiesBuilder(Collections.<String>emptySet());
    }

    @Override
    public Set<String> getProperties() {
      return Collections.emptySet();
    }

    @Override
    public boolean isDefined(final String propertyName) {
      return true;
    }

    @Override
    public Set<String> getValues(final String propertyName) {
      return Collections.emptySet();
    }

    @Override
    public String getStrictValue(final String propertyName) {
      // Not-strict
      return null;
    }

    @Override
    public String getSingleValue(final String propertyName) {
      // Not a single value
      return null;
    }

    @Override
    public boolean isOptional(final String propertyName) {
      // Everything's required
      return false;
    }

    @Override
    public boolean isSatisfiedBy(final ValueProperties properties) {
      // Only the infinite set can satisfy
      return (properties == this);
    }

    @Override
    public Set<String> getUnsatisfied(final ValueProperties properties) {
      return properties.getUnsatisfiedFromInfinite();
    }

    @Override
    public ValueProperties compose(final ValueProperties properties) {
      // Composition yields the infinite set
      return this;
    }

    @Override
    public ValueProperties intersect(final ValueProperties properties) {
      return properties.intersectInfinite();
    }

    @Override
    public ValueProperties union(final ValueProperties properties) {
      // Union is always the infinite set
      return this;
    }

    @Override
    public boolean isStrict() {
      return false;
    }

    @Override
    public boolean isEmpty() {
      return false;
    }

    @Override
    public ValueProperties withoutAny(final String propertyName) {
      ArgumentChecker.notNull(propertyName, "propertyName");
      return new NearInfiniteValueProperties(Collections.singleton(propertyName));
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
    public int compareTo(final ValueProperties other) {
      if (other == this) {
        return 0;
      }
      // Infinite is always after anything else
      return 1;
    }

    @Override
    public void toFudgeMsg(final MutableFudgeMsg msg) {
      msg.addSubMessage(ValuePropertiesFudgeBuilder.WITHOUT_FIELD, null);
    }

    private Object readResolve() throws Exception {
      return INFINITE;
    }

  }

  /**
   * An implementation representing a near-infinite property set.
   */
  private static final class NearInfiniteValueProperties extends ValueProperties {

    private static final long serialVersionUID = 1L;

    private final Set<String> _properties;

    /**
     * Creates a new instance, backed by the property array.
     * 
     * @param properties the property hash, never null. This will be owned by the object and must not be modified after calling.
     */
    public NearInfiniteValueProperties(final Set<String> properties) {
      _properties = properties;
    }

    // internal operations

    @Override
    /* package */boolean isSatisfySimple(final SimpleValueProperties other) {
      // Can satisfy unless there is a non-optional requirement for one of the absent properties
      for (AbstractValueProperty property : other._properties) {
        for (; property != null; property = property.getNext()) {
          if (!property.isOptional() && _properties.contains(property.getKey())) {
            return false;
          }
        }
      }
      return true;
    }

    @Override
    /* package */boolean isSatisfyNearInfinite(final NearInfiniteValueProperties other) {
      for (String property : _properties) {
        if (!other._properties.contains(property)) {
          // Other does not exclude one that we do - can't satisfy it
          return false;
        }
      }
      return true;
    }

    @Override
    /* package */Set<String> getUnsatisfiedFromSimple(final SimpleValueProperties other) {
      Set<String> properties = null;
      for (AbstractValueProperty property : other._properties) {
        for (; property != null; property = property.getNext()) {
          if (!property.isOptional() && _properties.contains(property.getKey())) {
            // Not optional, and we exclude so can't satisfy it
            if (properties == null) {
              properties = Sets.newHashSetWithExpectedSize(_properties.size());
            }
            properties.add(property.getKey());
          }
        }
      }
      return properties;
    }

    @Override
    /* package */Set<String> getUnsatisfiedFromNearInfinite(final NearInfiniteValueProperties other) {
      final Set<String> properties = new HashSet<String>(_properties);
      properties.removeAll(other._properties);
      if (properties.isEmpty()) {
        return null;
      }
      return properties;
    }

    @Override
    /* package */Set<String> getUnsatisfiedFromInfinite() {
      return Collections.unmodifiableSet(_properties);
    }

    @Override
    /* package */ValueProperties rightIntersectSimple(final SimpleValueProperties other) {
      // Right intersection is the other properties, but with anything defined here as non-optional
      AbstractValueProperty[] result = null;
      for (int i = 0; i < other._properties.length; i++) {
        AbstractValueProperty property = other._properties[i];
        boolean copied = false;
        for (; property != null; property = property.getNext()) {
          if (property.isOptional() && !_properties.contains(property.getKey())) {
            if (result == null) {
              result = Arrays.copyOf(other._properties, other._properties.length);
            }
            if (!copied) {
              // Copy the bucket, and advance to this position in the copy
              final String search = property.getKey();
              property = result[i].copy();
              result[i] = property;
              copied = true;
              while (property.getKey() != search) {
                property = property.getNext();
              }
            }
            property.setOptional(false);
          }
        }
      }
      if (result != null) {
        // Created an intersection
        return new SimpleValueProperties(result);
      }
      // Right intersection is the other object unchanged
      return other;
    }

    @Override
    /* package */ValueProperties intersectSimple(final SimpleValueProperties other) {
      return other.intersectNearInfinite(this);
    }

    @Override
    /* package */ValueProperties intersectNearInfinite(final NearInfiniteValueProperties other) {
      // Intersection is the union of the absent properties
      final Set<String> union = new HashSet<String>(_properties);
      union.addAll(other._properties);
      if (union.size() == _properties.size()) {
        // Intersection is the same
        return this;
      } else if (union.size() == other._properties.size()) {
        // Intersection is the same
        return other;
      } else {
        return new NearInfiniteValueProperties(union);
      }
    }

    @Override
    /* package */ValueProperties intersectInfinite() {
      // Intersection is this
      return this;
    }

    @Override
    /* package */ValueProperties unionSimple(final SimpleValueProperties other) {
      // Best efforts
      final Set<String> result = new HashSet<String>(_properties);
      for (AbstractValueProperty property : other._properties) {
        for (; property != null; property = property.getNext()) {
          result.remove(property.getKey());
        }
      }
      if (result.isEmpty()) {
        return INFINITE;
      } else if (result.size() == _properties.size()) {
        return this;
      } else {
        return new NearInfiniteValueProperties(result);
      }
    }

    @Override
    /* package */ValueProperties unionNearInfinite(final NearInfiniteValueProperties other) {
      // Union is the intersection of the absent properties
      final Set<String> intersect = new HashSet<String>(_properties);
      intersect.retainAll(other._properties);
      if (intersect.isEmpty()) {
        // Produced infinite set
        return INFINITE;
      } else if (intersect.size() == _properties.size()) {
        // Union is the same
        return this;
      } else if (intersect.size() == other._properties.size()) {
        // Union is the same
        return other;
      } else {
        return new NearInfiniteValueProperties(intersect);
      }
    }

    @Override
    /* package */int compareToSimple(final SimpleValueProperties other) {
      // Near infinite will always be after a simple value set
      return 1;
    }

    @Override
    /* package */int compareToNearInfinite(final NearInfiniteValueProperties other) {
      return compareSet(_properties, other._properties);
    }

    // public API methods

    @Override
    public Builder copy() {
      return new SubtractivePropertiesBuilder(_properties);
    }

    @Override
    public Set<String> getProperties() {
      return Collections.emptySet();
    }

    @Override
    public boolean isDefined(final String propertyName) {
      return !_properties.contains(propertyName);
    }

    @Override
    public Set<String> getValues(final String propertyName) {
      if (_properties.contains(propertyName)) {
        // Not defined
        return null;
      } else {
        // Wild-card
        return Collections.emptySet();
      }
    }

    @Override
    public String getStrictValue(final String propertyName) {
      // Either defined but not-strict, or not defined
      return null;
    }

    @Override
    public String getSingleValue(final String propertyName) {
      // Either defined but not a single value, or not defined
      return null;
    }

    @Override
    public boolean isOptional(final String propertyName) {
      // Either not optional, or not defined
      return false;
    }

    @Override
    public boolean isSatisfiedBy(final ValueProperties properties) {
      return properties.isSatisfyNearInfinite(this);
    }

    @Override
    public Set<String> getUnsatisfied(final ValueProperties properties) {
      if (properties == this) {
        return null;
      }
      return properties.getUnsatisfiedFromNearInfinite(this);
    }

    @Override
    public ValueProperties compose(final ValueProperties properties) {
      // Not strictly true, but close enough
      return this;
    }

    @Override
    public ValueProperties intersect(final ValueProperties properties) {
      return properties.intersectNearInfinite(this);
    }

    @Override
    public ValueProperties union(final ValueProperties properties) {
      return properties.unionNearInfinite(this);
    }

    @Override
    public boolean isStrict() {
      return false;
    }

    @Override
    public boolean isEmpty() {
      return false;
    }

    @Override
    public ValueProperties withoutAny(final String propertyName) {
      ArgumentChecker.notNull(propertyName, "propertyName");
      if (_properties.contains(propertyName)) {
        return this;
      }
      final Set<String> properties = new HashSet<String>(_properties);
      properties.add(propertyName);
      return new NearInfiniteValueProperties(properties);
    }

    @Override
    public String toString() {
      final StringBuilder sb = new StringBuilder("INFINITE-{");
      boolean first = true;
      for (String property : _properties) {
        if (first) {
          first = false;
        } else {
          sb.append(',');
        }
        sb.append(property);
      }
      sb.append('}');
      return sb.toString();
    }

    @Override
    public String toSimpleString() {
      return toString();
    }

    @Override
    public int compareTo(final ValueProperties other) {
      if (other == this) {
        return 0;
      }
      return -other.compareToNearInfinite(this);
    }

    @Override
    public int hashCode() {
      return _properties.hashCode();
    }

    @Override
    public boolean equals(final Object o) {
      if (o == this) {
        return true;
      }
      if (!(o instanceof NearInfiniteValueProperties)) {
        return false;
      }
      return _properties.equals(((NearInfiniteValueProperties) o)._properties);
    }

    @Override
    public void toFudgeMsg(final MutableFudgeMsg msg) {
      final MutableFudgeMsg subMsg = msg.addSubMessage(ValuePropertiesFudgeBuilder.WITHOUT_FIELD, null);
      int ordinal = 0;
      for (String property : _properties) {
        subMsg.add(null, ordinal++, FudgeWireType.STRING, property);
      }
    }

  }

  /**
   * Tests if a property instance corresponds to a near-infinite set.
   * <p>
   * This method was introduced to assist in migrating code that was explicitly handling the previously publicly classes. The empty and infinite tests can be identified by comparison with the values
   * returned by {@link #all} or {@code #none}. For any thing else, a normal property set (previously publicly visible as ValuePropertiesImpl) will return false from this and a near-infinite set
   * (previously publicly visible as NearlyInfinitePropertiesImpl) will return true.
   * 
   * @param properties the properties to test
   * @return true if the properties are near-infinite, false otherwise
   * @deprecated Testing for, and handling this as a special case is not recommended - it is better to write in terms of the set operation available
   */
  @Deprecated
  public static boolean isNearInfiniteProperties(final ValueProperties properties) {
    return properties instanceof NearInfiniteValueProperties;
  }

  /**
   * The empty set.
   */
  private static final ValueProperties EMPTY = new EmptyValueProperties();

  /**
   * The infinite property set.
   */
  private static final ValueProperties INFINITE = new InfiniteValueProperties();

  // internal implementation

  /* package */ValueProperties() {
  }

  /* package */abstract boolean isSatisfySimple(SimpleValueProperties other);

  /* package */abstract boolean isSatisfyNearInfinite(NearInfiniteValueProperties other);

  /* package */abstract Set<String> getUnsatisfiedFromSimple(SimpleValueProperties other);

  /* package */abstract Set<String> getUnsatisfiedFromNearInfinite(NearInfiniteValueProperties other);

  /* package */abstract Set<String> getUnsatisfiedFromInfinite();

  /* package */abstract ValueProperties rightIntersectSimple(SimpleValueProperties other);

  /* package */abstract ValueProperties intersectSimple(SimpleValueProperties other);

  /* package */abstract ValueProperties intersectNearInfinite(NearInfiniteValueProperties other);

  /* package */abstract ValueProperties intersectInfinite();

  /* package */abstract ValueProperties unionSimple(SimpleValueProperties other);

  /* package */abstract ValueProperties unionNearInfinite(NearInfiniteValueProperties other);

  /* package */abstract int compareToSimple(SimpleValueProperties other);

  /* package */abstract int compareToNearInfinite(NearInfiniteValueProperties other);

  // public API

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
   * This should be used with caution. Well written functions should build a property set explicitly stating the properties recognized.
   * 
   * @return the "infinite" property set, not null
   */
  public static ValueProperties all() {
    return INFINITE;
  }

  /**
   * Creates a builder for constructing value properties.
   * <p>
   * {@code ValueProperties} is immutable, but the builder is mutable allowing instances to be created efficiently.
   * 
   * @return the builder, not null
   */
  public static Builder builder() {
    return new AdditivePropertiesBuilder();
  }

  /**
   * Creates a builder for constructing value properties with the given property defined.
   * 
   * @param propertyName the name of the property to define, not null
   * @param propertyValue the property value, not null
   * @return the builder, not null
   */
  public static Builder with(final String propertyName, final String propertyValue) {
    return builder().with(propertyName, propertyValue);
  }

  /**
   * Creates a builder for constructing value properties with the given property defined.
   * 
   * @param propertyName the name of the property to define, not null
   * @param propertyValues the property values, not null and not containing null
   * @return the builder instance
   */
  public static Builder with(final String propertyName, final String... propertyValues) {
    return builder().with(propertyName, propertyValues);
  }

  /**
   * Creates a builder for constructing value properties with the given property defined.
   * 
   * @param propertyName the name of the property to define, not null
   * @param propertyValues the property values, not null and not containing null
   * @return the builder instance
   */
  public static Builder with(final String propertyName, final Collection<String> propertyValues) {
    return builder().with(propertyName, propertyValues);
  }

  /**
   * Creates a builder for constructing value properties with the given property defined as a wild-card.
   * 
   * @param propertyName the name of the property to define, not null
   * @return the builder instance
   */
  public static Builder withAny(final String propertyName) {
    return builder().withAny(propertyName);
  }

  /**
   * Creates a builder for constructing value properties with the given property defined as optional.
   * 
   * @param propertyName the name of the property to define, not null
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
   * Tests whether a property is defined.
   * <p>
   * If the name is defined, {@link #getValues} will not return null. This may be a cheaper test however.
   * 
   * @param propertyName the name required, not null
   * @return true if the property is defined, false otherwise
   */
  public abstract boolean isDefined(String propertyName);

  /**
   * Gets an immutable set of values for a given property name.
   * <p>
   * If the name is not defined null is returned. If the name has a wild-card value, the empty set is returned.
   * 
   * @param propertyName the name required, not null
   * @return the set of values, empty if wild-card, null if not defined
   */
  public abstract Set<String> getValues(String propertyName);

  /**
   * Returns a single value for a property that is part of a strict set.
   * <p>
   * If defined, the property must have a single value only. This is provided as a more efficient form than using the {@link Set} returned by {@link #getValues}.
   * 
   * @param propertyName the name required, not null
   * @return the strict value or null if the property is absent or not strict
   */
  public abstract String getStrictValue(String propertyName);

  /**
   * Returns a single value for any property that is not a wild-card.
   * <p>
   * If defined, the property must have at least one value. If there are multiple values then an arbitrary one is returned. This is provides as a more efficient form than using the {@link Set}
   * returned by {@link #getValues} to obtain an arbitrary single element.
   * 
   * @param propertyName the name required, not null
   * @return a value if the property is defined and not a wild-card, null otherwise
   */
  public abstract String getSingleValue(String propertyName);

  /**
   * Checks if a property may be omitted.
   * 
   * @param propertyName the name required, not null
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
   * @param properties the other property set to check against, not null
   * @return true if this set of properties can be satisfied by the other set
   */
  public abstract boolean isSatisfiedBy(ValueProperties properties);

  /**
   * Returns the properties in this instance that aren't satisfied by the other, as defined by {@link #isSatisfiedBy}.
   * 
   * @param properties the other property set to check against, not null
   * @return The set of unmatched property names, empty if wild-card, null if there are none
   */
  public abstract Set<String> getUnsatisfied(ValueProperties properties);

  /**
   * Composes two value properties by taking a "left" intersection.
   * <p>
   * This produces a set of properties such that for any properties defined by the other, the intersection of the property values is taken. Any properties defined in this set but not in the other
   * remain untouched.
   * 
   * @param properties the other property set to compose against, not null
   * @return the new set of properties, or this object if the composition result is equal, not null
   */
  public abstract ValueProperties compose(ValueProperties properties);

  /**
   * Produces the strict intersection of two property sets.
   * <p>
   * This produces a set of properties such that only properties defined in both this and the other are present in the output. For these, the intersection of common values is available for each
   * property. If there are no common property values, the property is omitted from the result.
   * 
   * @param properties the other property set to compose against, not null
   * @return the new set of properties, or this (or the other) object if the intersection result is equal, not null
   */
  public abstract ValueProperties intersect(ValueProperties properties);

  /**
   * Produces the union of two property sets.
   * <p>
   * This produces a set of properties such that any properties defined in either this or the other are present in the output. For these, the union of values from each property set is taken.
   * 
   * @param properties the other property set to compose against, not null
   * @return the new set of properties, or this (or the other) object if the union result is equal, not null
   */
  public abstract ValueProperties union(ValueProperties properties);

  /**
   * Checks if the set of properties is strict.
   * <p>
   * A property set is strict if there is only one value for each property or the property set is empty.
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
   * @param propertyName the property name to remove, not null
   * @return a value properties with the given property removed, not null
   */
  public abstract ValueProperties withoutAny(String propertyName);

  /**
   * Returns a simple string representation of the {@link ValueProperties} instance. This simple representation omits unnecessary brackets for better readability. The output remains valid as the input
   * to {@link #parse(String)}.
   * 
   * @return a simple string representation
   */
  public abstract String toSimpleString();

  /**
   * Adds fields describing this instance to a Fudge message.
   * <p>
   * See {@link ValuePropertiesFudgeBuilder} for a description of the message format.
   * 
   * @param message the message to add the fields to, not null
   */
  public abstract void toFudgeMsg(MutableFudgeMsg message);

  /**
   * Parses value property strings of the forms:
   * <ul>
   * <li>EMPTY
   * <li>INFINITE
   * <li>INFINITE-{name1,name2}
   * <li>{name1=[value1,value2],name2=[value3]}
   * </ul>
   * These are intentionally the same as the forms generated by {@link #toString()}. For maximum flexibility, and especially for user input, more abbreviated forms are also valid. In particular:
   * <ul>
   * <li>Curly braces may be omitted
   * <li>Square brackets around single values may be omitted
   * <li>'name1=[]' is same as 'name1'
   * <li>Spaces are trimmed unless they are in the middle of a name/value
   * </ul>
   * Escape sequences may be used for the following special characters: ',', '=', '[', ']', '?', '\' and ' '. An escape sequence begins with '\'.
   * <p>
   * A null or empty input string is treated as the empty set of value properties.
   * 
   * @param s the string to parse
   * @return the value properties, not null
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
      nearlyInfiniteResult = INFINITE;
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
    int space = -1;
    while (pos <= s.length()) {
      char next = pos < s.length() ? s.charAt(pos) : 0;
      if (next == '\\') { // Begin escape sequence
        pos++;
        if (pos < s.length()) {
          if (space > 0) {
            for (int i = 0; i < space; i++) {
              substring.append(' ');
            }
          }
          space = 0;
          char escapedCharacter = s.charAt(pos);
          if (escapedCharacter == '\\' || escapedCharacter == ',' || escapedCharacter == '=' || escapedCharacter == '[' || escapedCharacter == ']' || escapedCharacter == '?' ||
              escapedCharacter == ' ') {
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
        space = -1;
        name = substring.toString();
        substring = new StringBuilder();
        inValue = true;
        while (pos + 1 < s.length()) {
          if (s.charAt(pos + 1) == ' ') {
            pos++;
            continue;
          }
          if (s.charAt(pos + 1) == '[') {
            bracketedValue = true;
            pos++;
          }
          break;
        }
      } else if (next == ']') { // End of values
        space = -1;
        inValue = false;
        while (pos + 1 < s.length()) {
          if (s.charAt(pos + 1) == ' ') {
            pos++;
            continue;
          }
          if (s.charAt(pos + 1) == '?') {
            isOptional = true;
            pos++;
          }
          break;
        }
      } else if (next == ',' || next == 0) { // Separator between values in a group or between properties
        space = -1;
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
        if (space > 0) {
          for (int i = 0; i < space; i++) {
            substring.append(' ');
          }
        }
        space = 0;
        substring.append(next);
      } else {
        if (space >= 0) {
          space++;
        }
      }
      pos++;
    }
    if (name != null || substring.length() > 0) {
      throw new IllegalArgumentException("Unexpected end of ValueProperties string: " + s);
    }
    return nearlyInfinite ? nearlyInfiniteResult : builder.get();
  }

  /**
   * Produces a string representation of the content of a {@link ValueProperties} object.
   * 
   * @param properties the property names and values, not null
   * @param optional any properties that are optional, not null
   * @param strict whether to include delimiting '{' and '}' characters in the string
   * @return the string form
   * @deprecated This is based on the internal representation of {@code ValueProperties} from an older version and used mainly to implement the {@link #toString()} and {@link #toSimpleString()}
   *             methods. New code should not be calling it as it may be removed in future versions of the platform.
   */
  @Deprecated
  public static String toString(final Map<String, Set<String>> properties, final Set<String> optional, final boolean strict) {
    Pattern escapePattern = Pattern.compile("[=\\?\\[\\],\\\\ ]");
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

}
