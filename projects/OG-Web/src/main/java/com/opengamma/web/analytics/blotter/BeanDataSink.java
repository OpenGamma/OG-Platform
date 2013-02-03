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
 * @param <T> The type of object created by the sink
 */
/* package */ interface BeanDataSink<T> {

  void setBeanData(MetaBean metaBean, Bean bean);

  void setValue(String propertyName, Object value);

  void setCollection(String propertyName, Collection<?> values);

  void setMap(String propertyName, Map<?, ?> values);

  // TODO why does the sink need to do the conversion?
  Object convert(Object value, Class<?> type, BeanTraverser traverser);

  T finish();
}
