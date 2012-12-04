/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.blotter;

import org.joda.beans.MetaBean;

/**
 *
 */
/* package */ interface MetaBeanFactory {

  MetaBean beanFor(BeanDataSource beanData);
}
