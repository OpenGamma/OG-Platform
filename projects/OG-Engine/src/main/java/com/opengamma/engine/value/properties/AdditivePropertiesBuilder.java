/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.value.properties;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.google.common.collect.Sets;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueProperties.Builder;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.util.ArgumentChecker;

/**
 * Implementation of the {@link ValueProperties.Builder} for additive composition of properties.
 */
public class AdditivePropertiesBuilder extends ValueProperties.Builder {

  /**
   * The properties.
   */
  private AbstractValueProperty[] _properties;

  /**
   * Whether the property chain in a bucket is "owned" by this builder or not, or null if the array isn't even owned.
   */
  private boolean[] _copies;

  /**
   * The number of property entries in the builder, or negative if the count is unknown.
   */
  private int _numEntries;

  /**
   * Creates an empty instance.
   */
  public AdditivePropertiesBuilder() {
    _properties = null;
    _copies = null;
    _numEntries = 0;
  }

  /**
   * Creates an instance as a deep copy of another.
   * <p>
   * A full copy is performed rather than taking an unowned reference. The latter approach works when referencing the immutable content of an existing value property set, but not when the owner is a
   * builder as that may continue to modify the structure.
   * 
   * @param copyFrom the builder to copy from
   */
  private AdditivePropertiesBuilder(final AdditivePropertiesBuilder copyFrom) {
    if (copyFrom._properties != null) {
      final int l = copyFrom._properties.length;
      _properties = new AbstractValueProperty[l];
      _copies = new boolean[l];
      for (int i = 0; i < l; i++) {
        if (copyFrom._properties[i] != null) {
          _properties[i] = copyFrom._properties[i].copy();
          _copies[i] = true;
        }
      }
    } else {
      _properties = null;
      _copies = null;
    }
    _numEntries = copyFrom._numEntries;
  }

  /**
   * Creates an instance with default properties owned by something else.
   * 
   * @param properties the properties to populate with, not null. The array (and its contents) will not be modified - a copy will be taken when needed
   */
  public AdditivePropertiesBuilder(final AbstractValueProperty[] properties) {
    _properties = properties;
    _copies = null;
    _numEntries = Integer.MIN_VALUE;
  }

  private int count() {
    int numEntries = _numEntries;
    if (numEntries < 0) {
      numEntries = 0;
      for (AbstractValueProperty property : _properties) {
        for (; property != null; property = property.getNext()) {
          numEntries++;
        }
      }
      _numEntries = numEntries;
    }
    return numEntries;
  }

  private void rehash(final int count) {
    final int desiredSize = AbstractValueProperty.getDesiredHashSize(count);
    if (desiredSize == _properties.length) {
      return;
    }
    final AbstractValueProperty[] newProperties = new AbstractValueProperty[desiredSize];
    final boolean[] newCopies = new boolean[desiredSize];
    AbstractValueProperty.rehash(_properties, newProperties, _copies, newCopies);
    _properties = newProperties;
    _copies = newCopies;
  }

  private void enlarge() {
    final int numEntries = count() + 1;
    _numEntries = numEntries;
    rehash(numEntries);
  }

  private void localCopyImpl() {
    _properties = Arrays.copyOf(_properties, _properties.length);
    _copies = new boolean[_properties.length];
  }

  private void localCopy() {
    if (_copies == null) {
      localCopyImpl();
    }
  }

  public boolean hasLocalCopy() {
    return _copies != null;
  }

  /**
   * Adds a value to the builder, taking the union with any existing values.
   * 
   * @param value the value to add or take the union with.
   */
  public void union(final AbstractValueProperty value) {
    final int hc = value.getKey().hashCode() & 0x7FFFFFFF;
    if (_properties == null) {
      _properties = new AbstractValueProperty[AbstractValueProperty.getDesiredHashSize(1)];
      _copies = new boolean[_properties.length];
      _numEntries = 0;
    }
    int index = hc % _properties.length;
    AbstractValueProperty e = _properties[index];
    if (e != null) {
      if (_copies == null) {
        if (!e.unionChanges(value)) {
          // Union will have no effect
          return;
        }
        localCopyImpl();
      }
      if (!_copies[index]) {
        e = e.copy();
        _properties[index] = e;
        _copies[index] = true;
      }
      e = e.union(value);
      if (e != null) {
        _properties[index] = e;
      } else {
        enlarge();
        index = hc % _properties.length;
        e = _properties[index];
        if (!_copies[index]) {
          if (e != null) {
            e = e.copy();
          }
          _copies[index] = true;
        }
        _properties[index] = value.copy(e);
      }
    } else {
      if (_copies == null) {
        localCopyImpl();
      }
      _copies[index] = true;
      _properties[index] = value.copy(null);
      _numEntries++;
    }
  }

  /**
   * Finds a matching entry and takes the value intersection. If there is no intersection the entry is removed.
   * 
   * @param value the value to compose against if matched
   */
  public void compose(final AbstractValueProperty value) {
    if (_properties == null) {
      return;
    }
    final int hc = value.getKey().hashCode() & 0x7FFFFFFF;
    int index = hc % _properties.length;
    AbstractValueProperty e = _properties[index];
    if (e == null) {
      return;
    }
    if (_copies == null) {
      if (!e.composeChanges(value)) {
        // Compose will have no effect
        return;
      }
      localCopyImpl();
    }
    if (!_copies[index]) {
      e = e.copy();
      _copies[index] = true;
    }
    _properties[index] = e.compose(value);
    _numEntries = Integer.MIN_VALUE;
  }

  private AbstractValueProperty createValuePropertyFromSet(final String propertyName, final Set<String> values, final AbstractValueProperty next) {
    final int size = values.size();
    if (size == 1) {
      return new SingletonValueProperty(propertyName, false, values.iterator().next(), next);
    } else if (size <= ArrayValueProperty.MAX_ARRAY_LENGTH) {
      return new ArrayValueProperty(propertyName, false, values.toArray(new String[values.size()]), next);
    } else {
      return new SetValueProperty(propertyName, false, values, next);
    }
  }

  private AbstractValueProperty createValueProperty(final String propertyName, final String[] propertyValues, final AbstractValueProperty next) {
    final int size = propertyValues.length;
    if (size <= ArrayValueProperty.MAX_ARRAY_LENGTH) {
      return new ArrayValueProperty(propertyName, false, propertyValues, next);
    } else {
      final Set<String> values = Sets.newHashSetWithExpectedSize(size);
      for (String value : propertyValues) {
        values.add(value);
      }
      return createValuePropertyFromSet(propertyName, values, next);
    }
  }

  private AbstractValueProperty createValueProperty(final String propertyName, final Collection<String> propertyValues, final AbstractValueProperty next) {
    final int size = propertyValues.size();
    if (size == 1) {
      return new SingletonValueProperty(propertyName, false, propertyValues.iterator().next(), next);
    } else if (size <= ArrayValueProperty.MAX_ARRAY_LENGTH) {
      return new ArrayValueProperty(propertyName, false, propertyValues.toArray(new String[size]), next);
    } else {
      final Set<String> values = new HashSet<String>(propertyValues);
      return createValuePropertyFromSet(propertyName, values, next);
    }
  }

  // Builder

  @Override
  public Builder with(String propertyName, final String propertyValue) {
    ArgumentChecker.notNull(propertyName, "propertyName");
    ArgumentChecker.notNull(propertyValue, "propertyValue");
    propertyName = ValueRequirement.getInterned(propertyName);
    if (_properties == null) {
      _properties = new AbstractValueProperty[] {new SingletonValueProperty(propertyName, false, propertyValue, null) };
      _copies = new boolean[] {true };
      _numEntries = 1;
    } else {
      localCopy();
      final int hc = propertyName.hashCode() & 0x7FFFFFFF;
      int i = hc % _properties.length;
      AbstractValueProperty e = _properties[i];
      if (e != null) {
        if (!_copies[i]) {
          e = e.copy();
          _properties[i] = e;
          _copies[i] = true;
        }
        e = e.addValue(propertyName, propertyValue);
        if (e == null) {
          enlarge();
          i = hc % _properties.length;
          e = _properties[i];
          if (e != null) {
            if (_copies[i]) {
              e = e.copy();
              _copies[i] = true;
            }
          }
          _properties[i] = new SingletonValueProperty(propertyName, false, propertyValue, e);
        } else {
          _properties[i] = e;
        }
      } else {
        _properties[i] = new SingletonValueProperty(propertyName, false, propertyValue, null);
        _copies[i] = true;
        _numEntries++;
      }
    }
    return this;
  }

  @Override
  public Builder with(String propertyName, final String... propertyValues) {
    ArgumentChecker.notNull(propertyName, "propertyName");
    ArgumentChecker.notNull(propertyValues, "propertyValues");
    ArgumentChecker.noNulls(propertyValues, "propertyValues");
    if (propertyValues.length == 0) {
      throw new IllegalArgumentException("propertyValues must contain at least one element");
    }
    if (propertyValues.length == 1) {
      return with(propertyName, propertyValues[0]);
    }
    propertyName = ValueRequirement.getInterned(propertyName);
    if (_properties == null) {
      _properties = new AbstractValueProperty[] {createValueProperty(propertyName, propertyValues, null) };
      _copies = new boolean[] {true };
      _numEntries = 1;
    } else {
      localCopy();
      final int hc = propertyName.hashCode() & 0x7FFFFFFF;
      int i = hc % _properties.length;
      AbstractValueProperty e = _properties[i];
      if (e != null) {
        if (!_copies[i]) {
          e = e.copy();
          _properties[i] = e;
          _copies[i] = true;
        }
        e = e.addValues(propertyName, propertyValues);
        if (e == null) {
          enlarge();
          i = hc % _properties.length;
          e = _properties[i];
          if (e != null) {
            if (_copies[i]) {
              e = e.copy();
              _copies[i] = true;
            }
          }
          _properties[i] = createValueProperty(propertyName, propertyValues, e);
        } else {
          _properties[i] = e;
        }
      } else {
        _properties[i] = createValueProperty(propertyName, propertyValues, null);
        _copies[i] = true;
        _numEntries++;
      }
    }
    return this;
  }

  @Override
  public Builder with(String propertyName, final Collection<String> propertyValues) {
    ArgumentChecker.notNull(propertyName, "propertyName");
    ArgumentChecker.notNull(propertyValues, "propertyValues");
    if (propertyValues.isEmpty()) {
      throw new IllegalArgumentException("propertyValues must contain at least one element");
    }
    if (propertyValues.contains(null)) {
      throw new IllegalArgumentException("propertyValues cannot contain null");
    }
    propertyName = ValueRequirement.getInterned(propertyName);
    if (_properties == null) {
      _properties = new AbstractValueProperty[] {createValueProperty(propertyName, propertyValues, null) };
      _copies = new boolean[] {true };
      _numEntries = 1;
    } else {
      localCopy();
      final int hc = propertyName.hashCode() & 0x7FFFFFFF;
      int i = hc % _properties.length;
      AbstractValueProperty e = _properties[i];
      if (e != null) {
        if (!_copies[i]) {
          e = e.copy();
          _properties[i] = e;
          _copies[i] = true;
        }
        e = e.addValues(propertyName, propertyValues);
        if (e == null) {
          enlarge();
          i = hc % _properties.length;
          e = _properties[i];
          if (e != null) {
            if (_copies[i]) {
              e = e.copy();
              _copies[i] = true;
            }
          }
          _properties[i] = createValueProperty(propertyName, propertyValues, e);
        } else {
          _properties[i] = e;
        }
      } else {
        _properties[i] = createValueProperty(propertyName, propertyValues, null);
        _copies[i] = true;
        _numEntries++;
      }
    }
    return this;
  }

  @Override
  public Builder withAny(String propertyName) {
    ArgumentChecker.notNull(propertyName, "propertyName");
    propertyName = ValueRequirement.getInterned(propertyName);
    if (_properties == null) {
      _properties = new AbstractValueProperty[] {new WildcardValueProperty(propertyName, false, null) };
      _copies = new boolean[] {true };
      _numEntries = 1;
    } else {
      localCopy();
      final int hc = propertyName.hashCode() & 0x7FFFFFFF;
      int i = hc % _properties.length;
      AbstractValueProperty e = _properties[i];
      if (e != null) {
        if (!_copies[i]) {
          e = e.copy();
          _properties[i] = e;
          _copies[i] = true;
        }
        e = e.setWildcard(propertyName);
        if (e == null) {
          enlarge();
          i = hc % _properties.length;
          e = _properties[i];
          if (e != null) {
            if (_copies[i]) {
              e = e.copy();
              _copies[i] = true;
            }
          }
          _properties[i] = new WildcardValueProperty(propertyName, false, e);
        } else {
          _properties[i] = e;
        }
      } else {
        _properties[i] = new WildcardValueProperty(propertyName, false, null);
        _copies[i] = true;
        _numEntries++;
      }
    }
    return this;
  }

  @Override
  public Builder withOptional(String propertyName) {
    ArgumentChecker.notNull(propertyName, "propertyName");
    propertyName = ValueRequirement.getInterned(propertyName);
    if (_properties == null) {
      _properties = new AbstractValueProperty[] {new TentativeWildcardValueProperty(propertyName, null) };
      _copies = new boolean[] {true };
      _numEntries = 1;
    } else {
      localCopy();
      final int hc = propertyName.hashCode() & 0x7FFFFFFF;
      int i = hc % _properties.length;
      AbstractValueProperty e = _properties[i];
      if (e != null) {
        if (!_copies[i]) {
          e = e.copy();
          _properties[i] = e;
          _copies[i] = true;
        }
        e = e.setOptional(propertyName, true);
        if (e == null) {
          enlarge();
          i = hc % _properties.length;
          e = _properties[i];
          if (e != null) {
            if (_copies[i]) {
              e = e.copy();
              _copies[i] = true;
            }
          }
          _properties[i] = new TentativeWildcardValueProperty(propertyName, e);
        } else {
          _properties[i] = e;
        }
      } else {
        _properties[i] = new TentativeWildcardValueProperty(propertyName, null);
        _copies[i] = true;
        _numEntries++;
      }
    }
    return this;
  }

  @Override
  public Builder notOptional(final String propertyName) {
    ArgumentChecker.notNull(propertyName, "propertyName");
    if (_properties == null) {
      return this;
    } else {
      localCopy();
      final int hc = propertyName.hashCode() & 0x7FFFFFFF;
      int i = hc % _properties.length;
      AbstractValueProperty e = _properties[i];
      if (e != null) {
        if (!_copies[i]) {
          e = e.copy();
          _properties[i] = e;
          _copies[i] = true;
        }
        e = e.setOptional(propertyName, false);
        if (e != null) {
          _properties[i] = e;
        }
      }
    }
    return this;
  }

  @Override
  public Builder withoutAny(final String propertyName) {
    if (_properties == null) {
      // Nothing to remove
      return this;
    }
    final int hc = propertyName.hashCode() & 0x7FFFFFFF;
    int index = hc % _properties.length;
    AbstractValueProperty e = _properties[index];
    if (e == null) {
      // Nothing in the bucket to remove
      return this;
    }
    localCopy();
    if (!_copies[index]) {
      e = e.copy();
      _properties[index] = e;
      _copies[index] = true;
    }
    _properties[index] = e.remove(propertyName);
    _numEntries = Integer.MIN_VALUE;
    return this;
  }

  @Override
  public ValueProperties get() {
    final int count = count();
    if (count == 0) {
      return ValueProperties.none();
    } else {
      rehash(count);
      _copies = null;
      return createAdditive(_properties);
    }
  }

  @Override
  public Builder copy() {
    return new AdditivePropertiesBuilder(this);
  }

}
