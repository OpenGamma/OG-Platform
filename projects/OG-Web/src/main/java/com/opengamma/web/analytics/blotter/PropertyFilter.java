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
 *
 */
/* package */ class PropertyFilter implements BeanVisitorDecorator {

  private final Set<MetaProperty<?>> _properties;

  /* package */ PropertyFilter(MetaProperty<?>... properties) {
    ArgumentChecker.notNull(properties, "properties");
    _properties = Sets.newHashSet(Arrays.asList(properties));
  }

  @Override
  public BeanVisitor<?> decorate(final BeanVisitor<?> visitor) {
    return new BeanVisitor<Object>() {
      public void visitMetaBean(MetaBean metaBean) {
        visitor.visitMetaBean(metaBean);
      }

      public void visitBeanProperty(MetaProperty<?> property, BeanTraverser traverser) {
        if (!_properties.contains(property)) {
          visitor.visitBeanProperty(property, traverser);
        }
      }

      public void visitCollectionProperty(MetaProperty<?> property) {
        if (!_properties.contains(property)) {
          visitor.visitCollectionProperty(property);
        }
      }

      public void visitSetProperty(MetaProperty<?> property) {
        if (!_properties.contains(property)) {
          visitor.visitSetProperty(property);
        }
      }

      public void visitListProperty(MetaProperty<?> property) {
        if (!_properties.contains(property)) {
          visitor.visitListProperty(property);
        }
      }

      public void visitMapProperty(MetaProperty<?> property) {
        if (!_properties.contains(property)) {
          visitor.visitMapProperty(property);
        }
      }

      public void visitProperty(MetaProperty<?> property) {
        if (!_properties.contains(property)) {
          visitor.visitProperty(property);
        }
      }

      public Object finish() {
        return visitor.finish();
      }
    };
  }
}
