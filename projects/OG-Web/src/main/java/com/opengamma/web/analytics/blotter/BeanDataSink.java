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
 * TODO is this interface worth the complication or would it be better to imlement BeanVisitor directly?
 */
/* package */ interface BeanDataSink<T> {

  void setBeanData(MetaBean metaBean, Bean bean);

  void setValue(String propertyName, Object value);

  void setCollectionValues(String propertyName, Collection<?> values);

  void setMapValues(String propertyName, Map<?, ?> values);

  void setBeanValue(String propertyName, Bean bean, BeanTraverser traverser);

  T finish();
}
