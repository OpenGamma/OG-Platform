/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.blotter;

/**
 *
 */
/* package */ interface BeanVisitorDecorator<T> {

  BeanVisitor<T> decorate(BeanVisitor<T> visitor);
}
