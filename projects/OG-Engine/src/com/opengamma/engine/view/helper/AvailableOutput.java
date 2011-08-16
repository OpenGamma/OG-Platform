/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.helper;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.engine.value.ValueProperties;
import com.opengamma.util.ArgumentChecker;

/**
 * Helper class describing an available output from a portfolio.
 * <p>
 * The named value isn't guaranteed to be computable for all nodes, positions or security types indicated as the full set of
 * functions may require underlying market data or other information that is not available or computable. Similarly the full set
 * of properties may not be available. Due to the black-box nature of the function definitions additional properties or additional
 * property values not mentioned may be available for a portfolio.
 * <p>
 * For more accurate details, refer to the documentation for the functions installed in the repository.
 */
public final class AvailableOutput {

  private final String _valueName;
  private final String _anyValue;
  private final Map<String, ValueProperties> _positionProperties = new HashMap<String, ValueProperties>();
  private ValueProperties _portfolioNodeProperties;

  protected AvailableOutput(final String valueName, final String anyValue) {
    ArgumentChecker.notNull(valueName, "valueName");
    _valueName = valueName;
    _anyValue = anyValue;
  }

  protected static AvailableOutput ofPosition(final AvailableOutput copyFrom, final String securityType) {
    final AvailableOutput newInstance = new AvailableOutput(copyFrom.getValueName(), copyFrom.getAnyValue());
    newInstance._positionProperties.put(securityType, copyFrom._positionProperties.get(securityType));
    return newInstance;
  }

  protected static AvailableOutput ofPosition(final AvailableOutput copyFrom) {
    final AvailableOutput newInstance = new AvailableOutput(copyFrom.getValueName(), copyFrom.getAnyValue());
    newInstance._positionProperties.putAll(copyFrom._positionProperties);
    return newInstance;
  }

  protected static AvailableOutput ofPortfolioNode(final AvailableOutput copyFrom) {
    final AvailableOutput newInstance = new AvailableOutput(copyFrom.getValueName(), copyFrom.getAnyValue());
    newInstance._portfolioNodeProperties = copyFrom._portfolioNodeProperties;
    return newInstance;
  }

  public String getValueName() {
    return _valueName;
  }

  /**
   * Returns the indicator used to indicate a property can take any value in addition to the listed ones. {@code null} if no
   * explicit indicator has been used. When there is no indicator, the set of property values may be incomplete - wild-card values
   * may be available.
   * 
   * @return the indicator string, or {@code null} if none
   */
  public String getAnyValue() {
    return _anyValue;
  }

  public boolean isAvailableOnPosition() {
    return !_positionProperties.isEmpty();
  }

  public boolean isAvailableOn(final String securityType) {
    return _positionProperties.containsKey(securityType);
  }

  public boolean isAvailableOnPortfolioNode() {
    return _portfolioNodeProperties != null;
  }

  /**
   * Merges the available properties (left) with an incoming set (right). The result is the possible
   * properties on the output value. If a property is not defined on all merged components it is optional.
   * If a property is defined on multiple components with different values, the union of the values is
   * taken. The result is not guaranteed to be available but is a reasonable indication of what might be
   * available on an output value.
   * <p>
   * This is a commutative operation; i.e. merge(A, B) gives the same result as merge(B, A).
   * 
   * @param left left component of merge, not {@code null}
   * @param right right component of merge, not {@code null}
   * @return the merged properties
   */
  private ValueProperties merge(final ValueProperties left, final ValueProperties right) {
    if (right.isEmpty() || right.getProperties().isEmpty()) {
      // left composed against EMPTY or INFINITE (or near-infinite) is unchanged
      return left;
    }
    ValueProperties.Builder builder = left.copy();
    for (String property : left.getProperties()) {
      final Set<String> rightValues = right.getValues(property);
      if (rightValues == null) {
        // Right doesn't define, so make optional
        builder.withOptional(property);
      }
    }
    for (String property : right.getProperties()) {
      final Set<String> rightValues = right.getValues(property);
      final Set<String> leftValues = left.getValues(property);
      if (leftValues == null) {
        // Wasn't defined on the left, so the merged output is the optional version of the right
        if (rightValues.isEmpty()) {
          builder.withAny(property);
        } else {
          builder.with(property, rightValues);
        }
        builder.withOptional(property);
      } else {
        if (leftValues.isEmpty()) {
          if (!rightValues.isEmpty()) {
            // Left is "withAny" but right is restricted so use that
            builder.withoutAny(property);
            if (getAnyValue() != null) {
              builder.with(property, getAnyValue());
            }
            builder.with(property, rightValues);
          }
        } else if (rightValues.isEmpty()) {
          if (getAnyValue() != null) {
            // Include "any" indicator
            builder.with(property, getAnyValue());
          }
        } else {
          // Merge known values from the right
          builder.with(property, rightValues);
        }
        // Use greatest optionality
        if (right.isOptional(property)) {
          builder.withOptional(property);
        }
      }
    }
    return builder.get();
  }

  protected void setPortfolioNodeProperties(final ValueProperties properties) {
    if (_portfolioNodeProperties == null) {
      _portfolioNodeProperties = properties;
    } else {
      _portfolioNodeProperties = merge(_portfolioNodeProperties, properties);
    }
  }

  protected void setPositionProperties(final ValueProperties properties, final String securityType) {
    final ValueProperties existing = _positionProperties.get(securityType);
    if (existing == null) {
      _positionProperties.put(securityType, properties);
    } else {
      _positionProperties.put(securityType, merge(existing, properties));
    }
  }

  public ValueProperties getProperties() {
    ValueProperties result = _portfolioNodeProperties;
    for (ValueProperties properties : _positionProperties.values()) {
      if (result == null) {
        result = properties;
      } else {
        result = merge(result, properties);
      }
    }
    return result;
  }

  public ValueProperties getPositionProperties(final String securityType) {
    return _positionProperties.get(securityType);
  }

  public ValueProperties getPortfolioNodeProperties() {
    return _portfolioNodeProperties;
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "[" + getValueName() + ", node=" + _portfolioNodeProperties + ", position=" + _positionProperties + "]";
  }

  @Override
  public int hashCode() {
    int hc = 1;
    hc += (hc << 4) + getValueName().hashCode();
    hc += (hc << 4) + _positionProperties.hashCode();
    hc += (hc << 4) + ObjectUtils.hashCode(_portfolioNodeProperties);
    return hc;
  }

  @Override
  public boolean equals(final Object o) {
    if (o == this) {
      return true;
    }
    if (!(o instanceof AvailableOutput)) {
      return false;
    }
    final AvailableOutput other = (AvailableOutput) o;
    if (!getValueName().equals(other.getValueName())) {
      return false;
    }
    if (!_positionProperties.equals(other._positionProperties)) {
      return false;
    }
    return ObjectUtils.equals(_portfolioNodeProperties, other._portfolioNodeProperties);
  }

}
