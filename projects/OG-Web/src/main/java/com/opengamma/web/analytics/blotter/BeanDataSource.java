/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.blotter;

import java.util.List;
import java.util.Map;

/**
 *
 */
/* package */ interface BeanDataSource {

  String getValue(String propertyName);

  List<String> getCollectionValues(String propertyName);

  Map<String, String> getMapValues(String propertyName);

  BeanDataSource getBeanData(String propertyName);

  String getBeanTypeName();
}
