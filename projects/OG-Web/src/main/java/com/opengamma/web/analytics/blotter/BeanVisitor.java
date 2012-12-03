/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.blotter;

import org.joda.beans.MetaBean;
import org.joda.beans.MetaProperty;

/**
 *
 */
/* package */ interface BeanVisitor<T> {

  void visitBean(MetaBean metaBean);

  void visitBeanProperty(MetaProperty<?> property, BeanTraverser traverser);

  void visitCollectionProperty(MetaProperty<?> property);

  void visitSetProperty(MetaProperty<?> property);

  void visitListProperty(MetaProperty<?> property);

  void visitMapProperty(MetaProperty<?> property);

  void visitProperty(MetaProperty<?> property);

  T finish();
}
