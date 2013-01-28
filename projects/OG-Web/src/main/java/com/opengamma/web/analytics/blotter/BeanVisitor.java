/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.blotter;

import org.joda.beans.MetaBean;
import org.joda.beans.MetaProperty;

/**
 * Visitor containing callback methods which are invoked when traversing the structure of a {@link MetaBean}.
 * TODO it it worth having the type parameter any more?
 * @param <T> The type of object created by this visitor.
 */
/* package */ interface BeanVisitor<T> {

  void visitMetaBean(MetaBean metaBean);

  void visitBeanProperty(MetaProperty<?> property, BeanTraverser traverser);

  void visitCollectionProperty(MetaProperty<?> property);

  void visitSetProperty(MetaProperty<?> property);

  void visitListProperty(MetaProperty<?> property);

  void visitMapProperty(MetaProperty<?> property);

  void visitProperty(MetaProperty<?> property);

  T finish();
}
