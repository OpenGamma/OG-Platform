/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.blotter;

import org.joda.beans.MetaBean;

/**
 * Factory that can provide {@link MetaBean} instances for building beans from a {@link BeanDataSource}.
 */
/* package */ interface MetaBeanFactory {

  /**
   * @param beanData Data for building a Joda bean
   * @return A MetaBean instance that can be used to build the bean
   */
  MetaBean beanFor(BeanDataSource beanData);
}
