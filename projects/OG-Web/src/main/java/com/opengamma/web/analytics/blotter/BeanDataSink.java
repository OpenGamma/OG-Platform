/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.blotter;

import java.util.Collection;
import java.util.Map;

import org.joda.beans.Bean;
import org.joda.beans.MetaBean;

/**
 * Interface for classes that receive data from a bean and create another object from it.
 */
/* package */ interface BeanDataSink<T> {

  void setValue(String propertyName, Object value);

  void setCollectionValues(String propertyName, Collection<?> values);

  void setMapValues(String propertyName, Map<?, ?> values);

  void setBeanData(String propertyName, Bean bean, BeanTraverser traverser);

  void setBeanData(MetaBean metaBean, Bean bean);

  T finish();
}
