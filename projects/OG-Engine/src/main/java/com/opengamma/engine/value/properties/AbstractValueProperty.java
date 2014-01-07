/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.value.properties;

import java.io.Serializable;
import java.util.Collection;
import java.util.Set;

import org.fudgemsg.MutableFudgeMsg;

import com.opengamma.engine.value.ValueProperties;

/**
 * Base class for internal state used to implement {@link ValueProperties}.
 * <p>
 * These state objects are chained together in buckets in a hash to implement the full value properties structure.
 */
public abstract class AbstractValueProperty implements Serializable {

  private static final long serialVersionUID = 1L;

  /**
   * The property name for this entry.
   */
  private final String _key;

  /**
   * The 'optional' flag for this entry.
   */
  private boolean _optional;

  /**
   * The next entry in the bucket. Each bucket is a singly-linked list.
   */
  private AbstractValueProperty _next;

  // construction

  /**
   * Creates a new instance.
   * 
   * @param key the property name, never null
   * @param optional the optional flag
   * @param next the next property in the bucket, or null if this is the last
   */
  protected AbstractValueProperty(final String key, final boolean optional, final AbstractValueProperty next) {
    _key = key;
    _optional = optional;
    _next = next;
  }

  /**
   * Creates a copy of the property and everything chained after it in the bucket.
   * 
   * @return the new instance chain, never null
   */
  public AbstractValueProperty copy() {
    AbstractValueProperty next = _next;
    if (next != null) {
      next = next.copy();
    }
    return copy(next);
  }

  /**
   * Creates a copy of the property, chained to the given property in the bucket.
   * 
   * @param next the next property in the bucket, or null if this is to be the last
   * @return the new instance, never null
   */
  public abstract AbstractValueProperty copy(AbstractValueProperty next);

  /**
   * Creates a new instance with the given optional flag.
   * 
   * @param optional the new flag
   * @return the new instance, or this if the optional flag is unchanged, never null
   */
  protected abstract AbstractValueProperty withOptional(boolean optional);

  // query/update self

  /**
   * Returns the property name for this entry.
   * 
   * @return the property name, never null
   */
  public String getKey() {
    return _key;
  }

  /**
   * Returns the next property chained in the same hash bucket.
   * 
   * @return the next property or null if this is the last in the chain
   */
  public AbstractValueProperty getNext() {
    return _next;
  }

  /**
   * Returns the optional flag for this property.
   * 
   * @return true if the optional flag is set, false otherwise
   */
  public boolean isOptional() {
    return _optional;
  }

  /**
   * Sets the optional flag for this property.
   * 
   * @param optional true to mark this property optional, false otherwise
   */
  public void setOptional(final boolean optional) {
    _optional = optional;
  }

  /**
   * Returns the values from this property as an immutable set.
   * 
   * @return the values defined for this property, or null for the wild-card
   */
  public abstract Set<String> getValues();

  /**
   * Adds a value to the property.
   * 
   * @param value the value to add, never null
   * @return the updated property, either this or a new object, never null
   */
  protected abstract AbstractValueProperty addValueImpl(String value);

  /**
   * Adds one or more values to a property.
   * 
   * @param values the values to add, never null, not containing null and not empty
   * @return the updated property, either this or a new object, never null
   */
  protected abstract AbstractValueProperty addValuesImpl(String[] values);

  /**
   * Adds one or more values to a property.
   * 
   * @param values the values to add, never null, not containing null and not empty
   * @return the updated property, either this or a new object, never null
   */
  protected abstract AbstractValueProperty addValuesImpl(Collection<String> values);

  /**
   * Adds the values from this property to the one passed as parameter.
   * 
   * @param addTo the property object to update, never null
   * @return the updated property, either the parameter or a new object, never null
   */
  protected abstract AbstractValueProperty addValuesToImpl(AbstractValueProperty addTo);

  /**
   * Tests whether this property contains the given value.
   * 
   * @param value the value to test for, never null
   * @return true if the value is contained, false otherwise
   */
  protected abstract boolean containsValue(String value);

  /**
   * Tests whether this property contains all of the given values.
   * 
   * @param values the values to test for, never null or containing nulls
   * @return true if the values are all contained, false otherwise
   */
  protected abstract boolean containsAllValues(String[] values);

  /**
   * Tests whether this property contains all of the given values.
   * 
   * @param values the values to test for, never null or containing nulls
   * @return true if the values are all contained, false otherwise
   */
  protected abstract boolean containsAllValues(Collection<String> values);

  /**
   * Tests whether all of the values described by this property are contained by another.
   * 
   * @param other the property to test whether it contains this one's values, never null
   * @return true if the values are all contained, false otherwise
   */
  protected abstract boolean valuesContainedBy(AbstractValueProperty other);

  /**
   * Tests if this is a wild-card property.
   * 
   * @return true if this is a wild-card, false otherwise
   */
  public abstract boolean isWildcard();

  /**
   * Tests if this is a strictly defined property (one and only one value).
   * 
   * @return the strict value if defined, null otherwise
   */
  public abstract String getStrict();

  /**
   * Returns an arbitrary value if possible.
   * 
   * @return a value, or null if this is a wild-card definition
   */
  public abstract String getSingle();

  /**
   * Marks the property as a wild-card.
   * 
   * @return the new instance with the updated state, or this if it is already a wild-card
   */
  protected abstract AbstractValueProperty setWildcardImpl();

  /**
   * Tests whether the values from this property are satisfied by the given value.
   * 
   * @param value the candidate satisfying value, never null
   * @return true if the value satisfies this property, false otherwise
   */
  protected abstract boolean isSatisfiedBy(String value);

  /**
   * Tests whether the values in this property can satisfy the given property.
   * 
   * @param property the property to test satisfaction against, never null
   * @return true if the value satisfies this property, false otherwise
   */
  public abstract boolean isSatisfyValue(AbstractValueProperty property);

  /**
   * Forms the intersection of this and another property values.
   * 
   * @param other the property to intersect with
   * @return the updated property, either this or a new instance, or null if there is no intersection
   */
  protected abstract AbstractValueProperty intersectSingletonValue(SingletonValueProperty other);

  /**
   * Forms the intersection of this and another property values.
   * 
   * @param other the property to intersect with
   * @return the updated property, either this or a new instance, or null if there is no intersection
   */
  protected abstract AbstractValueProperty intersectArrayValue(ArrayValueProperty other);

  /**
   * Forms the intersection of this and another property values.
   * 
   * @param other the property to intersect with
   * @return the updated property, either this or a new instance, or null if there is no intersection
   */
  protected abstract AbstractValueProperty intersectSetValue(SetValueProperty other);

  /**
   * Forms the intersection of this and another property values.
   * 
   * @param other the property to intersect with
   * @return the updated property, either this or a new instance, or null if there is no intersection
   */
  public abstract AbstractValueProperty intersectValues(AbstractValueProperty other);

  /**
   * Adds a representation of this property to a Fudge message.
   * 
   * @param msg the message to update, never null
   */
  public abstract void toFudgeMsg(MutableFudgeMsg msg);

  // query/update bucket

  /**
   * Removes the named property from this bucket.
   * 
   * @param key the property name to remove
   * @return the updated property chain, or null if the chain is now empty
   */
  public AbstractValueProperty remove(final String key) {
    if (_key.equals(key)) {
      return _next;
    }
    if (_next != null) {
      _next = _next.remove(key);
    }
    return this;
  }

  /**
   * Tests whether the named property in this bucket chain is optional.
   * 
   * @param key the property name to test
   * @return true if the named property exists and is optional, false otherwise
   */
  public boolean isOptional(final String key) {
    if (_key.equals(key)) {
      return _optional;
    }
    if (_next == null) {
      return false;
    }
    return _next.isOptional(key);
  }

  /**
   * Sets the optional flag on a named property in this bucket chain.
   * 
   * @param key the property name to search for
   * @param optional the new optional flag for the property
   * @return the updated property chain, or null if the property was not found
   */
  public AbstractValueProperty setOptional(final String key, final boolean optional) {
    if (_key.equals(key)) {
      setOptional(optional);
      return this;
    }
    if (_next == null) {
      return null;
    }
    final AbstractValueProperty newNext = _next.setOptional(key, optional);
    if (newNext == null) {
      return null;
    }
    _next = newNext;
    return this;
  }

  /**
   * Fetches the property values for a named property in this bucket chain.
   * 
   * @param key the property name to search for
   * @return the property values or null if not found
   */
  public Set<String> getValues(final String key) {
    if (_key.equals(key)) {
      return getValues();
    }
    if (_next == null) {
      return null;
    }
    return _next.getValues(key);
  }

  /**
   * Fetches a single value for a named property in this bucket chain, if it is strictly defined.
   * 
   * @param key the property name to search for
   * @return the single property value or null if not found
   */
  public String getStrictValue(final String key) {
    if (_key.equals(key)) {
      return getStrict();
    }
    if (_next == null) {
      return null;
    }
    return _next.getStrictValue(key);
  }

  /**
   * Fetches a single value for a named property in this bucket chain.
   * 
   * @param key the property name to search for
   * @return a property value or null if not found (or a wild-card)
   */
  public String getSingleValue(final String key) {
    if (_key.equals(key)) {
      return getSingle();
    }
    if (_next == null) {
      return null;
    }
    return _next.getSingleValue(key);
  }

  /**
   * Adds a value to an existing named property.
   * 
   * @param key the property name to search for, must be interned
   * @param value the value to add, never null
   * @return the updated property chain or null if not found
   */
  public AbstractValueProperty addValue(final String key, final String value) {
    if (_key == key) {
      return addValueImpl(value);
    }
    if (_next == null) {
      return null;
    }
    final AbstractValueProperty newNext = _next.addValue(key, value);
    if (newNext == null) {
      return null;
    }
    _next = newNext;
    return this;
  }

  /**
   * Adds values to an existing named property.
   * 
   * @param key the property name to search for, must be interned
   * @param values the values to add, never null or containing nulls or duplicates
   * @return the updated property chain or null if not found
   */
  public AbstractValueProperty addValues(final String key, final String[] values) {
    if (_key == key) {
      return addValuesImpl(values);
    }
    if (_next == null) {
      return null;
    }
    final AbstractValueProperty newNext = _next.addValues(key, values);
    if (newNext == null) {
      return null;
    }
    _next = newNext;
    return this;
  }

  /**
   * Adds values to an existing named property.
   * 
   * @param key the property name to search for, must be interned
   * @param values the values to add, never null or containing nulls or duplicates
   * @return the updated property chain or null if not found
   */
  public AbstractValueProperty addValues(final String key, final Collection<String> values) {
    if (_key == key) {
      return addValuesImpl(values);
    }
    if (_next == null) {
      return null;
    }
    final AbstractValueProperty newNext = _next.addValues(key, values);
    if (newNext == null) {
      return null;
    }
    _next = newNext;
    return this;
  }

  /**
   * Marks an existing named property as a wild-card.
   * 
   * @param key the property name to search for, must be interned
   * @return the updated property chain or null if not found
   */
  public AbstractValueProperty setWildcard(final String key) {
    if (_key == key) {
      return setWildcardImpl();
    }
    if (_next == null) {
      return null;
    }
    final AbstractValueProperty newNext = _next.setWildcard(key);
    if (newNext == null) {
      return null;
    }
    _next = newNext;
    return this;
  }

  /**
   * Matches an existing property and forms the union of its values as part of the {@link ValueProperties#union} operation.
   * <p>
   * Note that if an existing property is not found then this will make no change but a new value property must be added to the containing hash to complete the logical {@code union} operation.
   * 
   * @param property the property name to search for and values to union, never null
   * @return the updated property chain or null if not found
   */
  public AbstractValueProperty union(final AbstractValueProperty property) {
    if (_key == property.getKey()) {
      _optional &= property._optional;
      return property.addValuesToImpl(this);
    }
    if (_next == null) {
      return null;
    }
    final AbstractValueProperty newNext = _next.union(property);
    if (newNext == null) {
      return null;
    }
    _next = newNext;
    return this;
  }

  /**
   * Tests whether the {@code union} operation should affect a change or not.
   * 
   * @param property the 'property' parameter that would be passed to {@code union}
   * @return true if a change would be made, false otherwise
   */
  public boolean unionChanges(final AbstractValueProperty property) {
    if (_key == property.getKey()) {
      if (_optional && !property._optional) {
        // Union has different optionality
        return true;
      }
      return !property.valuesContainedBy(this);
    }
    if (_next == null) {
      // Union will mean a property addition
      return true;
    }
    return _next.unionChanges(property);
  }

  /**
   * Matches an existing property and forms the intersection of its values as part of the {@link ValueProperties#compose} operation.
   * <p>
   * If the named property is found but there is no intersection then it is removed.
   * 
   * @param property the property name to search for and values to intersect with, never null
   * @return the updated property chain, or null if the chain is now empty
   */
  public AbstractValueProperty compose(final AbstractValueProperty property) {
    if (_key == property.getKey()) {
      _optional &= property._optional;
      final AbstractValueProperty composed = intersectValues(property);
      if (composed == null) {
        // No intersection - delete
        return _next;
      } else {
        if (composed._next != _next) {
          return composed.copy(_next);
        } else {
          return composed;
        }
      }
    }
    if (_next != null) {
      _next = _next.compose(property);
    }
    return this;
  }

  /**
   * Tests whether the {@link #compose} optional will affect a change or not.
   * 
   * @param property the 'property' parameter that would be passed to {@code compose}
   * @return true if a change would be made, false otherwise
   */
  public boolean composeChanges(final AbstractValueProperty property) {
    if (_key == property.getKey()) {
      if (_optional && !property._optional) {
        // Composition has different optionality
        return true;
      }
      return !valuesContainedBy(property);
    }
    if (_next == null) {
      return false;
    }
    return _next.composeChanges(property);
  }

  // Object

  /**
   * Returns a hash code of the value(s).
   * 
   * @return the hash code
   */
  protected abstract int valueHashCode();

  @Override
  public int hashCode() {
    // Hash code is the sum of the hashes of the items in this bucket.
    int hc = _key.hashCode();
    if (_optional) {
      hc = ~hc;
    }
    hc = (hc * 31) + valueHashCode();
    if (_next != null) {
      hc += _next.hashCode();
    }
    return hc;
  }

  /**
   * Tests whether the value is a singleton and equals the given value.
   * 
   * @param value the value to test, never null
   * @return true if this is a singleton matching the given value, false otherwise
   */
  protected abstract boolean equalsSingleton(String value);

  /**
   * Tests whether this is an array containing the given values.
   * 
   * @param values the values to test, never null or containing nulls
   * @return true if this is an array that contains exactly the given values, false otherwise
   */
  protected abstract boolean equalsArray(String[] values);

  /**
   * Tests whether this is a set containing the given values.
   * 
   * @param values the values to test, never null or containing nulls
   * @return true if this is a set that contains exactly the given values, false otherwise
   */
  protected abstract boolean equalsSet(Set<String> values);

  /**
   * Tests whether the values of this property are equal to another.
   * 
   * @param other the other property to test, never null
   * @return true if the values are equal, false otherwise
   */
  protected abstract boolean equalsValue(AbstractValueProperty other);

  /**
   * Tests whether this bucket chain contains another property.
   * 
   * @param other the property to search for, never null
   * @return true if this bucket chain contains a matching property, false otherwise
   */
  private boolean containsValue(final AbstractValueProperty other) {
    if (other == this) {
      return true;
    }
    if ((_optional == other._optional) && _key.equals(other._key) && equalsValue(other)) {
      return true;
    }
    if (_next != null) {
      return _next.containsValue(other);
    }
    return false;
  }

  @Override
  public boolean equals(final Object o) {
    // Tests if the other bucket contains the same as this one
    if (o == this) {
      return true;
    }
    if (!(o instanceof AbstractValueProperty)) {
      return false;
    }
    final AbstractValueProperty other = (AbstractValueProperty) o;
    AbstractValueProperty e = this;
    do {
      if (!other.containsValue(e)) {
        // We have a value that the other doesn't
        return false;
      }
      e = e._next;
    } while (e != null);
    e = other;
    do {
      if (!containsValue(e)) {
        // Other contains a value that we don't
        return false;
      }
      e = e._next;
    } while (e != null);
    return true;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    sb.append(_key);
    sb.append(" = ");
    sb.append(getValues());
    if (_optional) {
      sb.append('?');
    }
    return sb.toString();
  }

  // hashing

  /**
   * Generates a preferred hash size for a given number of elements.
   * <p>
   * The number will be as low as we can get away with, trading collisions (and speed) against memory consumption of the array. The number is also stepped to try to avoid frequent rehashing or
   * reallocation.
   * 
   * @param numElements the number of element to hold
   * @return the preferred hash size
   */
  public static int getDesiredHashSize(final int numElements) {
    return (numElements + 1) | 7;
  }

  /**
   * Rehashes an array containing buckets of property chains.
   * 
   * @param oldArray the old array of buckets, not null
   * @param newArray the new array of buckets, not null, updated by this method
   * @param oldCopies the mask of whether the entries in the old buckets are copies or not
   * @param newCopies the mask of whether the entries in the new buckets are copies or not, updated by this method
   */
  public static void rehash(final AbstractValueProperty[] oldArray, final AbstractValueProperty[] newArray, final boolean[] oldCopies, final boolean[] newCopies) {
    final int oldSize = oldArray.length;
    final int newSize = newArray.length;
    for (int oldIndex = 0; oldIndex < oldSize; oldIndex++) {
      AbstractValueProperty oldEntry = oldArray[oldIndex];
      if (oldEntry != null) {
        if (oldEntry.getNext() == null) {
          final int newIndex = (oldEntry.getKey().hashCode() & 0x7FFFFFFF) % newSize;
          AbstractValueProperty newEntry = newArray[newIndex];
          if (newEntry != null) {
            if (!newCopies[newIndex]) {
              newEntry = newEntry.copy();
              newCopies[newIndex] = true;
            }
            if (oldCopies[oldIndex]) {
              oldEntry._next = newEntry;
            } else {
              oldEntry = oldEntry.copy(newEntry);
            }
          } else {
            newCopies[newIndex] = oldCopies[oldIndex];
          }
          newArray[newIndex] = oldEntry;
        } else {
          if (!oldCopies[oldIndex]) {
            oldEntry = oldEntry.copy();
          }
          do {
            final AbstractValueProperty next = oldEntry.getNext();
            final int newIndex = (oldEntry.getKey().hashCode() & 0x7FFFFFFF) % newSize;
            AbstractValueProperty newEntry = newArray[newIndex];
            if (newEntry != null) {
              if (!newCopies[newIndex]) {
                newEntry = newEntry.copy();
                newCopies[newIndex] = true;
              }
              oldEntry._next = newEntry;
            } else {
              oldEntry._next = null;
              newCopies[newIndex] = true;
            }
            newArray[newIndex] = oldEntry;
            oldEntry = next;
          } while (oldEntry != null);
        }
      }
    }
  }

}
