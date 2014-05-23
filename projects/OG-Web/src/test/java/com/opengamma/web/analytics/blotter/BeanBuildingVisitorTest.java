/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.blotter;

import static org.testng.Assert.assertTrue;

import java.util.Map;

import org.joda.beans.Bean;
import org.joda.beans.BeanBuilder;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaBean;
import org.joda.beans.MetaProperty;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.opengamma.financial.security.fx.FXForwardSecurity;
import com.opengamma.financial.security.swap.FixedInterestRateLeg;
import com.opengamma.financial.security.swap.FloatingInterestRateLeg;
import com.opengamma.financial.security.swap.InterestRateNotional;
import com.opengamma.financial.security.swap.SwapLeg;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
@SuppressWarnings("unchecked")
public class BeanBuildingVisitorTest {

  private static final BeanVisitorDecorator s_securityTypeFilter = new PropertyFilter(ManageableSecurity.meta().securityType());

  private static final MetaBeanFactory META_BEAN_FACTORY = new MapMetaBeanFactory(ImmutableSet.<MetaBean>of(
      FXForwardSecurity.meta(),
      SwapSecurity.meta(),
      FixedInterestRateLeg.meta(),
      FloatingInterestRateLeg.meta(),
      InterestRateNotional.meta()));

  private static Map<MetaProperty<?>, Converter<?, ?>> s_regionConverters =
      ImmutableMap.<MetaProperty<?>, Converter<?, ?>>of(
          FXForwardSecurity.meta().regionId(), new FXRegionConverter(),
          SwapLeg.meta().regionId(), new StringToRegionIdConverter());

  private static final Converters CONVERTERS = new Converters(s_regionConverters,
                                                              BlotterUtils.getStringConvert());

  /**
   * test building a nice simple security
   */
  @Test
  public void buildFXForwardSecurity() {
    BeanVisitor<BeanBuilder<Bean>> visitor = new BeanBuildingVisitor<>(BlotterTestUtils.FX_FORWARD_DATA_SOURCE,
                                                                       META_BEAN_FACTORY,
                                                                       CONVERTERS);
    MetaBean metaBean = META_BEAN_FACTORY.beanFor(BlotterTestUtils.FX_FORWARD_DATA_SOURCE);
    BeanBuilder<ManageableSecurity> beanBuilder =
        (BeanBuilder<ManageableSecurity>) new BeanTraverser(s_securityTypeFilter).traverse(metaBean, visitor);
    ManageableSecurity security = beanBuilder.build();
    assertTrue(JodaBeanUtils.equalIgnoring(BlotterTestUtils.FX_FORWARD, security, ManageableSecurity.meta().uniqueId()));
  }

  /**
   * test building a security with fields that are also beans and have bean fields themsevles (legs, notionals)
   */
  @Test
  public void buildSwapSecurity() {
    BeanVisitor<BeanBuilder<Bean>> visitor = new BeanBuildingVisitor<>(BlotterTestUtils.SWAP_DATA_SOURCE,
                                                                       META_BEAN_FACTORY,
                                                                       CONVERTERS);
    MetaBean metaBean = META_BEAN_FACTORY.beanFor(BlotterTestUtils.SWAP_DATA_SOURCE);
    BeanBuilder<ManageableSecurity> beanBuilder =
        (BeanBuilder<ManageableSecurity>) new BeanTraverser(s_securityTypeFilter).traverse(metaBean, visitor);
    ManageableSecurity security = beanBuilder.build();
    assertTrue(JodaBeanUtils.equalIgnoring(BlotterTestUtils.SWAP, security, ManageableSecurity.meta().uniqueId()));
  }
}
