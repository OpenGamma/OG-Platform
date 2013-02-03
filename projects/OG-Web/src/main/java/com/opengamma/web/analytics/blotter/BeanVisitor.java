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

  // TODO this is inconsistent. normal properties have a different method if the type is a bean
  // shouldn't the others? map would need 3 for bean keys, values and both.
  // just get rid of visitbeanproperty?
  // or pass the types into the collection and map methods?

  // TODO should the collection and map methods have extra args specifying the types of their members?

  void visitBeanProperty(MetaProperty<?> property, BeanTraverser traverser);

  void visitCollectionProperty(MetaProperty<?> property, BeanTraverser traverser);

  void visitSetProperty(MetaProperty<?> property, BeanTraverser traverser);

  void visitListProperty(MetaProperty<?> property, BeanTraverser traverser);

  void visitMapProperty(MetaProperty<?> property, BeanTraverser traverser);

  void visitProperty(MetaProperty<?> property, BeanTraverser traverser);

  T finish();
}
