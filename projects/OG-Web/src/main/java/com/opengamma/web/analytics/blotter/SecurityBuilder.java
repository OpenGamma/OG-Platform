/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.blotter;

import java.util.List;
import java.util.Set;

import org.joda.beans.MetaBean;
import org.joda.beans.MetaProperty;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import com.opengamma.financial.conversion.JodaBeanConverters;
import com.opengamma.financial.security.fx.FXForwardSecurity;
import com.opengamma.financial.security.swap.FixedInterestRateLeg;
import com.opengamma.financial.security.swap.FloatingInterestRateLeg;
import com.opengamma.financial.security.swap.InterestRateNotional;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.master.security.ManageableSecurity;

/**
 *
 */
/* package */ class SecurityBuilder {

  /** Properties that aren't set from the data. TODO this should be necessary after PLAT-2863 is fixed */
  private static final Set<String> s_ignorePropertyNames = Sets.newHashSet("securityType");
  // TODO is this actually necessary? if underlying ID is set for non-fungible underlyings it will be overwritten anyway
  // still need a way to know whether to expect an underlying definition for a particular security type
  private static final Set<MetaProperty<?>> s_ignoreProperties = Sets.newHashSet();

  private static final List<MetaBean> s_metaBeans = ImmutableList.<MetaBean>of(
      FXForwardSecurity.meta(),
      SwapSecurity.meta(),
      FixedInterestRateLeg.meta(),
      FloatingInterestRateLeg.meta(),
      InterestRateNotional.meta()
  );

  static {
    JodaBeanConverters.getInstance(); // make sure the converters are loaded and registered
  }

  /* package */ @SuppressWarnings("unchecked")
  static ManageableSecurity buildBean(BeanDataSource data) {
    MetaBeanFactory metaBeanFactory = new MapMetaBeanFactory(s_metaBeans);
    BeanVisitor<ManageableSecurity> visitor = new BeanBuildingVisitor<ManageableSecurity>(data, metaBeanFactory);
    return new BeanTraverser(s_ignoreProperties, s_ignorePropertyNames).traverse(metaBeanFactory.beanFor(data), visitor);
  }
}

