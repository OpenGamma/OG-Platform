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

  private final Set<String> _propertyNames;

  /* package */ PropertyNameFilter(String... propertyNames) {
    ArgumentChecker.notNull(propertyNames, "propertyNames");
    _propertyNames = Sets.newHashSet(Arrays.asList(propertyNames));
  }

  @Override
  public BeanVisitor<?> decorate(final BeanVisitor<?> visitor) {
    return new BeanVisitor<Object>() {
      public void visitBean(MetaBean metaBean) {
        visitor.visitBean(metaBean);
      }

      public void visitBeanProperty(MetaProperty<?> property, BeanTraverser traverser) {
        if (!_propertyNames.contains(property.name())) {
          visitor.visitBeanProperty(property, traverser);
        }
      }

      public void visitCollectionProperty(MetaProperty<?> property) {
        if (!_propertyNames.contains(property.name())) {
          visitor.visitCollectionProperty(property);
        }
      }

      public void visitSetProperty(MetaProperty<?> property) {
        if (!_propertyNames.contains(property.name())) {
          visitor.visitSetProperty(property);
        }
      }

      public void visitListProperty(MetaProperty<?> property) {
        if (!_propertyNames.contains(property.name())) {
          visitor.visitListProperty(property);
        }
      }

      public void visitMapProperty(MetaProperty<?> property) {
        if (!_propertyNames.contains(property.name())) {
          visitor.visitMapProperty(property);
        }
      }

      public void visitProperty(MetaProperty<?> property) {
        if (!_propertyNames.contains(property.name())) {
          visitor.visitProperty(property);
        }
      }

      public Object finish() {
        return visitor.finish();
      }
    };
  }
}
