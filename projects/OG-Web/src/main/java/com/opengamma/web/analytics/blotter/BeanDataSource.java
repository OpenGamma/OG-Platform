/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.blotter;

import java.util.List;
import java.util.Map;

/**
 * TODO should the methods take something to say whether the type is an object or bean? or types for the map method
 */
/* package */ interface BeanDataSource {

  Object getValue(String propertyName);

  List<?> getCollectionValues(String propertyName);

  Map<?, ?> getMapValues(String propertyName);

  String getBeanTypeName();
}
