/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.cache;

import net.sf.ehcache.Element;
import net.sf.ehcache.store.compound.ReadWriteCopyStrategy;

import org.joda.beans.Bean;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaProperty;

import com.opengamma.master.AbstractLink;

/**
 * Strategy based on Joda beans.
 */
public class JodaBeanCopyStrategy implements ReadWriteCopyStrategy<Element> {

  /** Serialization version. */
  private static final long serialVersionUID = -7446127079130032128L;

  @Override
  public Element copyForWrite(Element element) {
    if (element == null) {
      return null;
    } else {
      Bean bean = (Bean) element.getObjectValue();
      Element result = new Element(element.getObjectKey(), JodaBeanUtils.clone(bean));

      // Clear any resolved links that point to other documents (e.g. positions linking to securities)
      for (MetaProperty<?> metaProperty : bean.metaBean().metaPropertyIterable()) {
        if (AbstractLink.class.isAssignableFrom(metaProperty.propertyType())) {
          AbstractLink<?> link = (AbstractLink<?>) metaProperty.get(bean);
          link.setTarget(null);
        }
      }
      return result;
    }
  }

  @Override
  public Element copyForRead(Element element) {
    if (element == null) {
      return null;
    } else {
      Bean bean = (Bean) element.getObjectValue();
      return new Element(element.getObjectKey(), JodaBeanUtils.clone(bean));
    }
  }

}
