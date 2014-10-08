/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableMap;
import com.opengamma.financial.analytics.curve.CurveConstructionConfiguration;
import com.opengamma.financial.analytics.curve.CurveGroupConfiguration;
import com.opengamma.id.VersionCorrection;
import com.opengamma.service.ServiceContext;
import com.opengamma.service.ThreadLocalServiceContext;
import com.opengamma.service.VersionCorrectionProvider;
import com.opengamma.sesame.engine.ComponentMap;
import com.opengamma.sesame.graph.FunctionModel;
import com.opengamma.sesame.sources.BondMockSources;
import com.opengamma.util.result.Result;
import com.opengamma.util.test.TestGroup;

@Test(groups = TestGroup.UNIT)
public class DiscountingIssuerProviderBundleFnTest {

  private DefaultDiscountingIssuerProviderBundleFn _bundleFn;
  private static final Environment ENV = BondMockSources.ENV;

  @BeforeClass
  public void init() throws IOException {

    VersionCorrectionProvider vcProvider = new VersionCorrectionProvider() {
      @Override
      public VersionCorrection getPortfolioVersionCorrection() {
        return VersionCorrection.LATEST;
      }

      @Override
      public VersionCorrection getConfigVersionCorrection() {
        return VersionCorrection.LATEST;
      }
    };

    ImmutableMap<Class<?>, Object> components = BondMockSources.generateBaseComponents();

    ThreadLocalServiceContext.init(ServiceContext.of(components).with(VersionCorrectionProvider.class,vcProvider));
    _bundleFn = FunctionModel.build(DefaultDiscountingIssuerProviderBundleFn.class,
                                    BondMockSources.getConfig(),
                                    ComponentMap.of(components));

  }

  @Test
  public void testCurveBundle() {
    CurveConstructionConfiguration curveConfig = BondMockSources.getBondCurveConfig();
    Result<IssuerProviderBundle> issuerProviderBundleResult = _bundleFn.generateBundle(ENV, curveConfig);
    assertThat(issuerProviderBundleResult.isSuccess(), is(true));

    IssuerProviderBundle providerBundle = issuerProviderBundleResult.getValue();
    assertThat(providerBundle.getCurveBuildingBlockBundle().getData().isEmpty(), is(false));
    assertThat(providerBundle.getParameterIssuerProvider().getAllCurveNames().isEmpty(), is(false));

  }

  @Test
  public void testEmptyCurveBundle() {
    List<String> exogenous = Collections.emptyList();
    List<CurveGroupConfiguration> groups = Collections.emptyList();
    CurveConstructionConfiguration curveConfig = new CurveConstructionConfiguration("Empty", groups, exogenous);;
    Result<IssuerProviderBundle> issuerProviderBundleResult = _bundleFn.generateBundle(ENV, curveConfig);
    assertThat(issuerProviderBundleResult.isSuccess(), is(true));

    IssuerProviderBundle providerBundle = issuerProviderBundleResult.getValue();
    assertThat(providerBundle.getCurveBuildingBlockBundle().getData().isEmpty(), is(true));
    assertThat(providerBundle.getParameterIssuerProvider().getAllCurveNames().isEmpty(), is(true));

  }

}
