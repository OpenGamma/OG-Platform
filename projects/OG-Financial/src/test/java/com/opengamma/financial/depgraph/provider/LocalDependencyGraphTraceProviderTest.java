/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.depgraph.provider;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.opengamma.financial.depgraph.rest.DependencyGraphBuildTrace;
import com.opengamma.financial.depgraph.rest.DependencyGraphTraceBuilder;
import com.opengamma.financial.depgraph.rest.DependencyGraphTraceBuilderProperties;
import com.opengamma.util.test.TestGroup;

/**
 * Test for {@link LocalDependencyGraphTraceProvider}
 */
@Test(groups = TestGroup.UNIT)
public class LocalDependencyGraphTraceProviderTest {

  @Test
  public void LocalDependencyGraphTraceProvider() {
    assertEquals(_builder, _provider.getTraceBuilder());
  }

  private DependencyGraphTraceBuilder _builder;
  private LocalDependencyGraphTraceProvider _provider;
  private DependencyGraphBuildTrace _sampleResult;

  @BeforeMethod
  public void beforeTest() {
    _builder = mock(DependencyGraphTraceBuilder.class);
    _provider = new LocalDependencyGraphTraceProvider(_builder);
    _sampleResult = DependencyGraphBuildTrace.of(null, null, null, null);
  }

  @Test
  public void getTrace() {
    DependencyGraphTraceBuilderProperties properties = new DependencyGraphTraceBuilderProperties();
    when(_builder.build(properties)).thenReturn(_sampleResult);

    DependencyGraphBuildTrace result = _provider.getTrace(properties);

    verify(_builder).build(properties);
    assertEquals(_sampleResult, result);

  }

}
