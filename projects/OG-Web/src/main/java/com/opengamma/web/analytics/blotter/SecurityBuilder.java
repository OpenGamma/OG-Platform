/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.blotter;

import java.util.Set;

import org.joda.beans.MetaBean;

import com.opengamma.master.security.ManageableSecurity;

/**
 *
 */
/* package */ class SecurityBuilder {

  private final Set<MetaBean> _metaBeans;

  /* package */ SecurityBuilder(Set<MetaBean> metaBeans) {
    _metaBeans = metaBeans;
  }

  /* package */ ManageableSecurity buildSecurity(BeanDataSource data) {
    MetaBeanFactory metaBeanFactory = new MapMetaBeanFactory(_metaBeans);
    BeanVisitor<ManageableSecurity> visitor = new BeanBuildingVisitor<ManageableSecurity>(data, metaBeanFactory);
    // TODO filter out security name and underlyingId for securities with OTC underlyings
    return new BeanTraverser().traverse(metaBeanFactory.beanFor(data), visitor);
  }
}

