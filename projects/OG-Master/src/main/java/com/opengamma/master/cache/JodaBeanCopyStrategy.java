/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.cache;

import org.joda.beans.Bean;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaProperty;

import com.opengamma.master.AbstractLink;

import net.sf.ehcache.Element;
import net.sf.ehcache.store.compound.ReadWriteCopyStrategy;

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
      Element result = new Element(element.getObjectKey(), JodaBeanUtils.clone((Bean) element.getObjectValue()));

      // Clear any resolved links that point to other documents (e.g. positions linking to securities)
      for (MetaProperty<?> metaProperty :((Bean) element.getObjectValue()).metaBean().metaPropertyIterable()) {
        if (AbstractLink.class.isAssignableFrom(metaProperty.propertyType())) {
          ((AbstractLink<?>) metaProperty.get((Bean) element.getObjectValue())).setTarget(null);
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
      return new Element(element.getObjectKey(), JodaBeanUtils.clone((Bean) element.getObjectValue()));
    }
  }
}
