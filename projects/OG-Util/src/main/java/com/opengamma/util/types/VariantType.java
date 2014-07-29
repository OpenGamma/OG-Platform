/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.types;

import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * Represents a type expression that can be one of a number of known alternatives. This is a specialized form of {@link WildcardType} that does not have an explicit lower bound from syntax (as defined
 * by the Java language specification) but has logical types defined from the application logic. For example, a type defined in code as {@code Set&lt;? extends Number&gt;} could be refined at the
 * application level, based on the known behavior of code, to indicate that the type can only be either {@code Integer} or {@code Double} (type logical types) rather than any arbitrary sub-class of
 * {@code Number}.
 * <p>
 * Note that only two or more logical types are permitted - if there is only one logical type then there is no choice and that type should be used directly rather than as a bound.
 * <p>
 * This type is typically used as part of object meta-data, delivered explicitly from the application API/SPI rather than by Java reflection, to assist in user interface code that requires more
 * sophisticated type information than would otherwise be permitted. For example, a class might need to declare a property of type Set&lt;Object&gt; but the exact content type (required to deliver
 * meaningful user interactions when populating it) may be determined by a service object held in another property. That service object could have a method returning {@link Type} which indicates the
 * valid classes to put into the set. Depending on the complexity of the application logic, that type might then need to be, or need to reference, one of these variant types.
 */
public final class VariantType implements WildcardType {

  private static final Type[] s_lower = new Type[0];

  private final Type _upper;
  private final Type[] _logicalTypes;

  private VariantType(final Type upperBound, final Type[] logicalTypes) {
    _upper = upperBound;
    _logicalTypes = logicalTypes;
  }

  private static Class<?> toClass(final Type[] bounds) {
    if (bounds.length != 1) {
      // Note: possibly not correct but a good fall-back position
      return Object.class;
    }
    return toClass(bounds[0]);
  }

  private static Class<?> toClass(final Type type) {
    if (type instanceof Class) {
      return (Class<?>) type;
    }
    if (type instanceof GenericArrayType) {
      final GenericArrayType array = (GenericArrayType) type;
      return Array.newInstance(toClass(array.getGenericComponentType()), 0).getClass();
    }
    if (type instanceof ParameterizedType) {
      final ParameterizedType param = (ParameterizedType) type;
      return toClass(param.getRawType());
    }
    if (type instanceof TypeVariable) {
      final TypeVariable<?> variable = (TypeVariable<?>) type;
      return toClass(variable.getBounds());
    }
    if (type instanceof WildcardType) {
      final WildcardType wildcard = (WildcardType) type;
      return toClass(wildcard.getUpperBounds());
    }
    throw new IllegalArgumentException("Can't handle " + type);
  }

  private static Type guessUpperBound(final Type a, final Type b) {
    // Note: This is sufficient for what this class currently gets used for, but isn't ideal
    Class<?> classA = toClass(a);
    final Class<?> classB = toClass(b);
    do {
      if (classA.isAssignableFrom(classB)) {
        return classA;
      }
      classA = classA.getSuperclass();
    } while (a != null);
    throw new IllegalArgumentException("A=" + a + ", B=" + b);
  }

  private VariantType with(final Type logicalType) {
    final Set<Type> types = new HashSet<Type>();
    for (Type type : _logicalTypes) {
      types.add(type);
    }
    final Type upper;
    if (logicalType instanceof VariantType) {
      final VariantType variant = (VariantType) logicalType;
      for (Type type : variant._logicalTypes) {
        types.add(type);
      }
      upper = guessUpperBound(_upper, variant._upper);
    } else {
      if (!types.add(logicalType)) {
        return this;
      }
      upper = guessUpperBound(_upper, logicalType);
    }
    return new VariantType(upper, types.toArray(new Type[types.size()]));
  }

  /**
   * Creates a new variant type that references the two possible logical types.
   * <p>
   * If either parameter is null then the other is returned.
   * <p>
   * If either parameter is itself a variant type then the resulting type is the union of all logical types.
   * 
   * @param type1 the first logical type, possibly null
   * @param type2 the second logical type, possibly null
   * @return the resulting type.
   */
  public static Type either(final Type type1, final Type type2) {
    if (type1 == null) {
      return type2;
    }
    if (type2 == null) {
      return type1;
    }
    if (type1.equals(type2)) {
      return type1;
    }
    if (type1 instanceof VariantType) {
      return ((VariantType) type1).with(type2);
    }
    if (type2 instanceof VariantType) {
      return ((VariantType) type2).with(type1);
    }
    return new VariantType(guessUpperBound(type1, type2), new Type[] {type1, type2 });
  }

  @Override
  public Type[] getUpperBounds() {
    return new Type[] {_upper };
  }

  @Override
  public Type[] getLowerBounds() {
    return s_lower;
  }

  public Type[] getLogicalTypes() {
    return _logicalTypes.clone();
  }

  /**
   * Tests for equality with another object. For compatibility with the standard Java type system, an instance is considered equal to any other {@link WildcardType} that has the same upper bound.
   * 
   * @param o the object to compare to
   * @return true if the other object is a {@code WildcardType} with the same upper bound and no lower bound, false otherwise
   */
  @Override
  public boolean equals(final Object o) {
    if (o == this) {
      return true;
    }
    if (!(o instanceof WildcardType)) {
      return false;
    }
    final WildcardType other = (WildcardType) o;
    if (other.getLowerBounds().length != 0) {
      return false;
    }
    final Type[] upper = other.getUpperBounds();
    if (upper.length != 1) {
      return false;
    }
    return _upper.equals(upper[0]);
  }

  /**
   * Returns the hash code of this object. For compatability with the standard Java type system, the hashcode will be the same as any other {@link WildcardType} that has the same upper bound and no
   * lower bounds.
   * 
   * @return the hash code
   */
  @Override
  public int hashCode() {
    // Arrays.hashCode(getUpperBounds()) ^ Arrays.hashCode(getLowerBounds())
    return (31 + _upper.hashCode()) ^ 1;
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
  }

}
