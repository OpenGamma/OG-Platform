/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.value;

import java.io.ObjectInputStream;
import java.io.Serializable;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.text.StrBuilder;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.MemoryUtils;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.id.UniqueId;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.PublicAPI;

/**
 * An immutable representation of the metadata that describes an actual value.
 * <p>
 * This may be a value a function is capable of producing, or describe a resolved value passed into a function to satisfy a {@link ValueRequirement}.
 * <p>
 * For example the {@code ValueRequirement} for a currency converting function may state a constraint such as "any currency" on its input values. After the graph has been built, the actual value will
 * be specified including the specific currency. Similarly a constraint on a {@link ValueRequirement} might restrict the function to be used (or the default of omission allows any) whereas the
 * {@link ValueSpecification} will indicate which function was used to compute that value.
 * <p>
 * This class is immutable and thread-safe.
 */
@PublicAPI
public class ValueSpecification implements Serializable {

  private static final long serialVersionUID = 1L;

  /**
   * The name of the value being requested. This matches that of a {@link ValueRequirement} satisfied by this specification.
   */
  private String _valueName;
  /**
   * The specification of the object that the value refers to. This matches that of a {@link ValueRequirement} satisfied by this specification.
   */
  private final ComputationTargetSpecification _targetSpecification;
  /**
   * The properties of the value described. This property set will satisfy the constraints of all {@link ValueRequirement}s satisfied by this specification.
   */
  private final ValueProperties _properties;

  /**
   * The cached hash code.
   */
  private transient volatile int _hashCode;

  /**
   * Obtains a {@code ValueSpecification} from a target, building the target specification according to the type of object the target refers to. The properties must include the function identifier.
   *
   * @param valueName the name of the value created, not null
   * @param computationTargetSpecification the ComputationTargetSpecification, not null
   * @param properties the value properties, not null and must include the function identifier
   * @return the created specification, not null
   */
  public static ValueSpecification of(final String valueName, final ComputationTargetSpecification computationTargetSpecification, final ValueProperties properties) {
    ArgumentChecker.notNull(computationTargetSpecification, "computationTargetSpecification");
    ArgumentChecker.notNull(properties, "uid");
    return new ValueSpecification(valueName, computationTargetSpecification, properties);
  }

  /**
   * Obtains a {@code ValueSpecification} from a target, building the target specification according to the type of object the target refers to. The properties must include the function identifier.
   * 
   * @param valueName the name of the value created, not null
   * @param targetType the ComputationTargetType, not null
   * @param targetId the unique id of the target, not null
   * @param properties the value properties, not null and must include the function identifier
   * @return the created specification, not null
   */
  public static ValueSpecification of(final String valueName, final ComputationTargetType targetType, final UniqueId targetId, final ValueProperties properties) {
    ArgumentChecker.notNull(targetType, "targetType");
    ArgumentChecker.notNull(properties, "uid");
    return new ValueSpecification(valueName, new ComputationTargetSpecification(targetType, targetId), properties);
  }

  /**
   * Obtains a {@code ValueSpecification} from a target, building the target specification according to the type of object the target refers to. The properties must include the function identifier
   * unless it's provided separately in which case it will be added to the properties if any others are provided.
   * 
   * @param valueName the name of the value created, not null
   * @param targetType the ComputationTargetType, not null
   * @param targetId the unique id of the target, not null
   * @param functionIdentifier the function identifier, may be null
   * @param currencyISO the currency ISO code, may be null
   * @param properties the value properties, or can be null if the function identifier provided separately
   * @return the created specification, not null
   */
  public static ValueSpecification of(
      final String valueName, final ComputationTargetType targetType, final UniqueId targetId, final String functionIdentifier,
      final String currencyISO, final ValueProperties properties) {
    ArgumentChecker.notNull(targetType, "targetType");
    ArgumentChecker.notNull(properties, "uid");
    ValueProperties props;
    if ((functionIdentifier == null) && (currencyISO == null)) {
      props = properties;
    } else {
      ValueProperties.Builder builder;
      if (properties == null) {
        builder = ValueProperties.builder();
      } else {
        builder = properties.copy();
      }
      if (currencyISO != null) {
        builder = builder.with(ValuePropertyNames.CURRENCY, currencyISO);
      }
      if (functionIdentifier != null) {
        builder = builder.with(ValuePropertyNames.FUNCTION, functionIdentifier);
      }
      props = builder.get();
    }
    return new ValueSpecification(valueName, new ComputationTargetSpecification(targetType, targetId), props);
  }

  /**
   * Creates a new specification from a target specification.
   * <p>
   * The properties must include the function identifier.
   * 
   * @param valueName the name of the value created, not null
   * @param targetSpecification the target specification, not null
   * @param properties the value properties, not null and must include the function identifier
   */
  public ValueSpecification(final String valueName, final ComputationTargetSpecification targetSpecification, final ValueProperties properties) {
    ArgumentChecker.notNull(valueName, "valueName");
    ArgumentChecker.notNull(targetSpecification, "targetSpecification");
    ArgumentChecker.notNull(properties, "properties");
    ArgumentChecker.isTrue(properties.isDefined(ValuePropertyNames.FUNCTION), "properties.FUNCTION");
    _valueName = ValueRequirement.getInterned(valueName);
    _targetSpecification = targetSpecification;
    _properties = properties;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the value name.
   * 
   * @return the value name, not null
   */
  public String getValueName() {
    return _valueName;
  }

  /**
   * Gets the target specification.
   * 
   * @return the target specification, not null
   */
  public ComputationTargetSpecification getTargetSpecification() {
    return _targetSpecification;
  }

  /**
   * Gets the value properties.
   * <p>
   * At the minimum the property set will contain the function identifier.
   * 
   * @return the properties, not null
   */
  public ValueProperties getProperties() {
    return _properties;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets a specific property by name.
   * <p>
   * If multiple values are set for a property then an arbitrary choice is made.
   * 
   * @param propertyName name of the property to search for, not null
   * @return the matched property value, null if not found
   * @throws IllegalArgumentException if the property has a wild-card definition
   */
  public String getProperty(final String propertyName) {
    final String value = _properties.getSingleValue(propertyName);
    if (value == null) {
      if (_properties.isDefined(propertyName)) {
        throw new IllegalArgumentException("property " + propertyName + " contains only wild-card values");
      }
    }
    return value;
  }

  /**
   * Creates a maximal {@link ValueRequirement} that would be satisfied by this value specification.
   * 
   * @return the value requirement, not null
   * @deprecated Conversion to a value requirement should not be needed - locations where this is called from should probably be using value requirements in the first place
   */
  @Deprecated
  public ValueRequirement toRequirementSpecification() {
    return new ValueRequirement(_valueName, _targetSpecification, _properties);
  }

  /**
   * Gets the identifier of the function that calculates this value.
   * 
   * @return the function identifier, not null
   **/
  public String getFunctionUniqueId() {
    return _properties.getStrictValue(ValuePropertyNames.FUNCTION);
  }

  /**
   * Respecifies the properties to match a tighter requirement.
   * 
   * @param requirement additional requirement to reduce properties against
   * @return the value specification based on this with the additional requirement added, not null
   */
  public ValueSpecification compose(final ValueRequirement requirement) {
    assert requirement.getValueName() == getValueName();
    assert requirement.getConstraints().isSatisfiedBy(getProperties());
    final ValueProperties oldProperties = getProperties();
    final ValueProperties newProperties = oldProperties.compose(requirement.getConstraints());
    if (newProperties == oldProperties) {
      return this;
    } else {
      return new ValueSpecification(getValueName(), getTargetSpecification(), newProperties);
    }
  }

  //-------------------------------------------------------------------------
  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj instanceof ValueSpecification) {
      final ValueSpecification other = (ValueSpecification) obj;
      // valueName is interned
      return (_valueName == other._valueName) &&
          ObjectUtils.equals(_targetSpecification, other._targetSpecification) &&
          ObjectUtils.equals(_properties, other._properties);
    }
    return false;
  }

  @Override
  public int hashCode() {
    if (_hashCode == 0) {
      final int prime = 37;
      int result = 1;
      result = (result * prime) + _valueName.hashCode();
      result = (result * prime) + _targetSpecification.hashCode();
      result = (result * prime) + _properties.hashCode();
      _hashCode = result;
    }
    return _hashCode;
  }

  @Override
  public String toString() {
    return new StrBuilder()
        .append("VSpec[")
        .append(getValueName())
        .append(", ")
        .append(getTargetSpecification())
        .append(", ")
        .append(getProperties())
        .append(']')
        .toString();
  }

  private void readObject(final ObjectInputStream in) throws Exception {
    in.defaultReadObject();
    // Serialization loses the "intern" nature of the string
    _valueName = ValueRequirement.getInterned(_valueName);
  }

  private Object readResolve() throws Exception {
    return MemoryUtils.instance(this);
  }

}
