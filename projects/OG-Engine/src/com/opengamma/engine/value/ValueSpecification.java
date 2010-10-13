package com.opengamma.engine.value;

import org.apache.commons.lang.ObjectUtils;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.PublicAPI;

/**
 * Representation of a ValueSpecification, which ties a @see ValueRequirement to a specific function and so it is more specific
 * For example, think: Requirement = YIELD_CURVE on USD, Specification = YIELD_CURVE on USD computed by MarketInstrumentImpliedYieldCurveFunction.
 */
@PublicAPI
public class ValueSpecification implements java.io.Serializable {
  private final ValueRequirement _requirementSpecification;
  private final String _functionUniqueId;

  public ValueSpecification(ValueRequirement requirementSpecification, String functionUniqueId) {
    ArgumentChecker.notNull(requirementSpecification, "Value requirement specification");
    ArgumentChecker.notNull(functionUniqueId, "_functionUniqueId");
    _requirementSpecification = requirementSpecification;
    _functionUniqueId = functionUniqueId;
  }

  /**
 *    * Get the requirementSpecification field.
 *       * @return the requirementSpecification
 *          */
  public ValueRequirement getRequirementSpecification() {
    return _requirementSpecification;
  }

  /**
 *    * Gets the functionUniqueId field.
 *       * @return the functionUniqueId
 *          */
  public String getFunctionUniqueId() {
    return _functionUniqueId;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj instanceof ValueSpecification) {
      ValueSpecification other = (ValueSpecification) obj;
      return ObjectUtils.equals(_requirementSpecification, other._requirementSpecification) && ObjectUtils.equals(_functionUniqueId, other._functionUniqueId);
    }
    return false;
  }

  @Override
  public int hashCode() {
    final int prime = 37;
    int result = 1;
    result = (result * prime) + ObjectUtils.hashCode(getRequirementSpecification());
    result = (result * prime) + ObjectUtils.hashCode(getFunctionUniqueId());
    return result;
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
  }
}

