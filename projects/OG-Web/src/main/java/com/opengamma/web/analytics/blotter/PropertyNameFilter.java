/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.blotter;

import java.util.Arrays;
import java.util.Set;

import org.joda.beans.MetaBean;
import org.joda.beans.MetaProperty;

import com.google.common.collect.Sets;
import com.opengamma.util.ArgumentChecker;

/**
 * TODO I don't know if I'll need this after all
 */
/* package */ class PropertyNameFilter implements BeanVisitorDecorator {

  /** Names of the properties that will be filtered out. */
  private final Set<String> _propertyNames;

  /* package */ PropertyNameFilter(String... propertyNames) {
    ArgumentChecker.notNull(propertyNames, "propertyNames");
    _propertyNames = Sets.newHashSet(Arrays.asList(propertyNames));
  }

  @Override
  public BeanVisitor<?> decorate(final BeanVisitor<?> visitor) {
    return new BeanVisitor<Object>() {
      public void visitMetaBean(MetaBean metaBean) {
        visitor.visitMetaBean(metaBean);
      }

      public void visitBeanProperty(MetaProperty<?> property, BeanTraverser traverser) {
        if (!_propertyNames.contains(property.name())) {
          visitor.visitBeanProperty(property, traverser);
        }
      }

      public void visitCollectionProperty(MetaProperty<?> property, BeanTraverser traverser) {
        if (!_propertyNames.contains(property.name())) {
          visitor.visitCollectionProperty(property, traverser);
        }
      }

      public void visitSetProperty(MetaProperty<?> property, BeanTraverser traverser) {
        if (!_propertyNames.contains(property.name())) {
          visitor.visitSetProperty(property, traverser);
        }
      }

      public void visitListProperty(MetaProperty<?> property, BeanTraverser traverser) {
        if (!_propertyNames.contains(property.name())) {
          visitor.visitListProperty(property, traverser);
        }
      }

      public void visitMapProperty(MetaProperty<?> property, BeanTraverser traverser) {
        if (!_propertyNames.contains(property.name())) {
          visitor.visitMapProperty(property, traverser);
        }
      }

      public void visitProperty(MetaProperty<?> property, BeanTraverser traverser) {
        if (!_propertyNames.contains(property.name())) {
          visitor.visitProperty(property, traverser);
        }
      }

      public Object finish() {
        return visitor.finish();
      }
    };
  }
}
