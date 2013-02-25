/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.blotter;

import java.util.List;
import java.util.Map;

/**
 * Source of data used to build a bean instance. The structure of the data source is likely to match the structure
 * of the bean.
 */
/* package */ interface BeanDataSource {

  /**
   * Gets the value for a property
   * @param propertyName The property name
   * @return The property value as a string if it's a simple value or a {@link BeanDataSource} if it has structure
   */
  Object getValue(String propertyName);

  /**
   * Gets the values for a collection property
   * @param propertyName The property name
   * @return The property values as strings if they're simple or as {@link BeanDataSource}s if they have structure
   */
  List<?> getCollectionValues(String propertyName);

  /**
   * Gets the values for a map property
   * @param propertyName The property name
   * @return The property value map. The keys and values are strings if they're simple or {@link BeanDataSource}s
   * if they have structure
   */
  Map<?, ?> getMapValues(String propertyName);

  /**
   * @return The type name of the bean, used to identify its meta bean.
   */
  String getBeanTypeName();
}
