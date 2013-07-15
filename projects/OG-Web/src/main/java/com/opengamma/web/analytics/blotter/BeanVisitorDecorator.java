/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.blotter;

/**
 * TODO should this have a type param or not? it's useful in some cases
 * TODO is there any guarantee the wrapping visitor will return the same type as the wrapped visitor?
 */
/* package */ interface BeanVisitorDecorator {

  BeanVisitor<?> decorate(BeanVisitor<?> visitor);
}
