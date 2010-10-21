package com.opengamma.engine.value;

import java.util.Set;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.PublicAPI;

/**
 * An immutable representation of the metadata that describes an actual value. This may be a value
 * a function is capable of producing, or the describe resolved value passed into a function to
 * satisfy a {@link ValueRequirement}.
 * 
 * For example the {@code ValueRequirement} for an FX function may state a constraint such as
 * "any currency" on a value. After the graph has been built, the actual value will be specified
 * including the specific currency. Similarly a constraint on a {@link ValueRequirement} might restrict
 * the function to be used (or the default of omission allows any) whereas the {@link ValueSpecification}
 * will indicate which function was used to compute that value.
 */
@PublicAPI
public class ValueSpecification implements java.io.Serializable {

  // DO WE WANT THE ORIGINAL REQUIREMENT SPECIFICATION AS THE CONSTRAINTS BUNDLED WITH THAT ARE NOT RELEVANT ANY MORE
  private final ValueRequirement _requirementSpecification;

  private final ValueProperties _properties;

  public ValueSpecification(ValueRequirement requirementSpecification, String functionIdentifier) {
    ArgumentChecker.notNull(requirementSpecification, "requirementSpecification");
    ArgumentChecker.notNull(functionIdentifier, "functionIdentifier");
    _requirementSpecification = requirementSpecification;
    _properties = requirementSpecification.getConstraints().copy().with(ValuePropertyNames.FUNCTION, functionIdentifier).get();
  }

  public ValueSpecification(ValueRequirement requirementSpecification, ValueProperties properties) {
    ArgumentChecker.notNull(requirementSpecification, "requirementSpecification");
    ArgumentChecker.notNull(properties, "properties");
    _requirementSpecification = requirementSpecification;
    _properties = properties;
  }

  /**
   * Get the requirementSpecification field.
   * @return the requirementSpecification
   **/
  public ValueRequirement getRequirementSpecification() {
    return _requirementSpecification;
  }

  public ValueProperties getProperties() {
    return _properties;
  }

  /**
   * Gets the functionUniqueId field.
   * @return the functionUniqueId
   **/
  public String getFunctionUniqueId() {
    final Set<String> values = _properties.getValues(ValuePropertyNames.FUNCTION);
    if (values == null) {
      return null;
    } else {
      return values.iterator().next();
    }
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj instanceof ValueSpecification) {
      ValueSpecification other = (ValueSpecification) obj;
      return ObjectUtils.equals(_requirementSpecification, other._requirementSpecification) && ObjectUtils.equals(_properties, other._properties);
    }
    return false;
  }

  @Override
  public int hashCode() {
    final int prime = 37;
    int result = 1;
    result = (result * prime) + _requirementSpecification.hashCode();
    result = (result * prime) + _properties.hashCode();
    return result;
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
  }
}
