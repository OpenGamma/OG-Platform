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
import org.joda.beans.MetaProperty;

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
  // because when it needs to traverse a BeanDataSource only the sink knows how to create another sink
  Object convert(Object value, MetaProperty<?> property, Class<?> expectedType, BeanTraverser traverser);

  T finish();
}
