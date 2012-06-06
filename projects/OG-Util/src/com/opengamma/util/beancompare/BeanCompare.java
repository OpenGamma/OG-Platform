/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.beancompare;

import com.google.common.collect.ImmutableList;
import com.opengamma.util.ArgumentChecker;
import org.joda.beans.Bean;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaProperty;

import java.util.*;

/**
 * Compares two Joda beans of the same class and returns details of any differences.
 */
public class BeanCompare {

  /** Comparators for bean properties keyed on the property they can compare. */
  private final Map<MetaProperty<?>, Comparator<Object>> _propertyComparators;
  private final Map<Class<?>, Comparator<Object>> _typeComparators;

  /**
   * Creates a new instance that uses the default comparison logic when comparing bean property values.
   */
  public BeanCompare() {
    this(Collections.<MetaProperty<?>, Comparator<Object>>emptyMap(),
         Collections.<Class<?>, Comparator<Object>>emptyMap());
  }

  /**
   * Creates a new instance which uses the {@link java.util.Comparator} instances in {@code propertyComparators}
   * and {@code typeComparators} when comparing bean property values.  If there is no comparator for the property or
   * type being compared the default comparison logic is used.
   * @param propertyComparators Comparators used for comparing properties keyed by the property they apply to
   * @param typeComparators Comparators used for comparing properties keyed by the type they apply to
   */
  public BeanCompare(Map<MetaProperty<?>, Comparator<Object>> propertyComparators,
                     Map<Class<?>, Comparator<Object>> typeComparators) {
    ArgumentChecker.notNull(propertyComparators, "propertyComparators");
    ArgumentChecker.notNull(typeComparators, "typeComparators");
    _propertyComparators = propertyComparators;
    _typeComparators = typeComparators;
  }

  /**
   * Compares two beans of the same class and returns details of any differences.  If any of the bean properties
   * are beans themselves they are compared recursively.
   * @param bean1 A bean, not null
   * @param bean2 Another bean of the same type, not null
   * @return The differences between the beans or an empty list if they are identical
   * @throws IllegalArgumentException If the beans' classes are different
   */
  public List<BeanDifference<?>> compare(Bean bean1, Bean bean2) {
    ArgumentChecker.notNull(bean1, "bean1");
    ArgumentChecker.notNull(bean2, "bean2");
    if (!sameClass(bean1, bean2)) {
      throw new IllegalArgumentException("Beans must be of the same class. bean1 class: " + bean1.getClass() +
          ", bean2 class: " + bean2.getClass());
    }
    return compare(bean1, bean2, Collections.<MetaProperty<?>>emptyList());
  }

    /**
    * @param path Properties required to get from the root beans to the current beans, an empty list if the current
    * beans are the root beans
    */
  private List<BeanDifference<?>> compare(Bean bean1, Bean bean2, List<MetaProperty<?>> path) {
    Iterable<MetaProperty<?>> properties = bean1.metaBean().metaPropertyIterable();
    List<BeanDifference<?>> differences = new ArrayList<BeanDifference<?>>();

    for (MetaProperty<?> property : properties) {
      Object value1 = property.get(bean1);
      Object value2 = property.get(bean2);
      if (value1 instanceof Bean && value2 instanceof Bean && sameClass(value1, value2)) {
        List<MetaProperty<?>> newPath = ImmutableList.<MetaProperty<?>>builder().addAll(path).add(property).build();
        differences.addAll(compare(((Bean) value1), ((Bean) value2), newPath));
      } else {
        if (!equal(property, value1, value2)) {
          differences.add(new BeanDifference<Object>(property, value1, value2, path));
        }
      }
    }
    return differences;
  }

  /**
   * Returns {@code true} if the two values of a property are equal.  If there is a custom comparator for
   * {@code property} it is used for the comparison, otherwise the default comparison logic is used to compare the
   * values ({@link org.joda.beans.JodaBeanUtils#equal(Object, Object)}.
   * @param property The property whose values are being tested for equality
   * @param value1 A property value
   * @param value2 Another property value
   * @return {@code true} if the values are equal according to the comparator for {@code property} or
   * {@link org.joda.beans.JodaBeanUtils#equal(Object, Object)} if there is no comparator for the property
   */
  private boolean equal(MetaProperty<?> property, Object value1, Object value2) {
    Comparator<Object> comparator = _propertyComparators.get(property);
    if (comparator == null) {
      comparator = _typeComparators.get(property.propertyType());
    }
    if (value1 == null || value2 == null || comparator == null) {
      return JodaBeanUtils.equal(value1, value2);
    } else {
      return comparator.compare(value1, value2) == 0;
    }
  }

  private static boolean sameClass(Object value1, Object value2) {
    return value1.getClass().equals(value2.getClass());
  }
}
