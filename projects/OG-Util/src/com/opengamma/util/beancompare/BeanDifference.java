/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.beancompare;

import com.opengamma.util.ArgumentChecker;
import org.joda.beans.MetaProperty;

import java.util.Collections;
import java.util.List;

/**
 * Encapsulates the difference in a property's value between two instances of the same bean type.  Contains the
 * property that differs, the values from each bean and the path to the property (if it is a child bean of the
 * bean at the root of the comparison).
 * @param <P> Type of the field that differs between the two beans.
 * @see BeanCompare#compare(org.joda.beans.Bean, org.joda.beans.Bean)
 */
public class BeanDifference<P> {

  /** Property that differed between the two beans */
  private final MetaProperty<? extends P> _property;

  /** Path from the root bean to the bean where the difference was found, empty list if the property belongs to the root bean */
  private final List<MetaProperty<?>> _path;

  /** Property value in bean 1 */
  private final P _value1;

  /** Property value in bean 2 */
  private final P _value2;

  /**
   * @param property Property that differed between the two beans, not null
   * @param value1 Value of the property in bean 1, can be null
   * @param value2 Value of the property in bean 2, can be null
   * @param path Path from the root bean to the bean where the difference was found.  If the difference is in
   * a property of the root bean this should be an empty list
   */
  public BeanDifference(MetaProperty<? extends P> property, P value1, P value2, List<MetaProperty<?>> path) {
    ArgumentChecker.notNull(property, "property");
    ArgumentChecker.notNull(path, "path");
    _property = property;
    _value1 = value1;
    _value2 = value2;
    _path = path;
  }

  /**
   * @param property Property that differed between the two beans, not null
   * @param value1 Value of the property in bean 1, can be null
   * @param value2 Value of the property in bean 2, can be null
   * a property of the root bean this should be an empty list
   */
  public BeanDifference(MetaProperty<? extends P> property, P value1, P value2) {
    this(property, value1, value2, Collections.<MetaProperty<?>>emptyList());
  }

  /**
   * @return The property which has a different value in the two beans
   */
  public MetaProperty<? extends P> getProperty() {
    return _property;
  }

  /**
   * @return The property value in the first bean
   */
  public P getValue1() {
    return _value1;
  }

  /**
   * @return The property value in the second bean
   */
  public P getValue2() {
    return _value2;
  }

  /**
   * If any of a bean's properties are beans themselves the comparison recursively compares all beans in the hierarchy.
   * If a difference is found in a child bean the path contains the properties leading from the root bean to the child.
   * For example consider the following classes:
   * <pre>
   * class Foo implements Bean {
   *   &#64PropertyDefinition
   *   private Bar bar;
   * }
   *
   * class Bar implements Bean {
   *   &#64PropertyDefinition
   *   private Baz baz;
   * }
   *
   * class Baz implements Bean {
   *   &#64PropertyDefinition
   *   private String name;
   * }
   * </pre>
   * Comparing two instances of {@code Foo} where {@code Baz} has a different name would yield a {@code BeanDifference}
   * with a path {@code [Foo:bar, Bar:baz]} and a property {@code Baz:name}.
   * @return The properties leading to the bean at the root of the comparison to the bean where the difference was
   * found.  Returns an empty list if the difference is in a property of the root bean.
   */
  public List<MetaProperty<?>> getPath() {
    return _path;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    BeanDifference that = (BeanDifference) o;

    if (!_path.equals(that._path)) {
      return false;
    }
    if (!_property.equals(that._property)) {
      return false;
    }
    if (_value1 != null ? !_value1.equals(that._value1) : that._value1 != null) {
      return false;
    }
    if (_value2 != null ? !_value2.equals(that._value2) : that._value2 != null) {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode() {
    int result = _property.hashCode();
    result = 31 * result + _path.hashCode();
    result = 31 * result + (_value1 != null ? _value1.hashCode() : 0);
    result = 31 * result + (_value2 != null ? _value2.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "BeanDifference{_property=" + _property + ", _value1=" + _value1 + ", _value2=" + _value2 + ", _path=" + _path + "}";
  }
}
