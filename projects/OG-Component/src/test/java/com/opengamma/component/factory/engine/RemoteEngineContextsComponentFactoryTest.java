/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.component.factory.engine;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedHashMap;
import java.util.Properties;

import net.sf.ehcache.CacheManager;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.testng.annotations.Test;

import com.opengamma.component.ComponentRepository;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.engine.ComputationTargetResolver;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.financial.OpenGammaExecutionContext;
import com.opengamma.transport.jaxrs.UriEndPointDescriptionProvider;
import com.opengamma.util.ehcache.EHCacheUtils;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.test.TestProperties;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 * Tests the {@link RemoteEngineContextsComponentFactory} class.
 */
@Test
public class RemoteEngineContextsComponentFactoryTest {

  protected void testFactory(final RemoteEngineContextsComponentFactory factory) throws Exception {
    factory.setClassifier("test");
    factory.setStrict(true);
    final ComponentRepository repo = new ComponentRepository(null);
    final LinkedHashMap<String, String> localConfiguration = new LinkedHashMap<String, String>();
    localConfiguration.put("ignorePnlRequirementsGatherer", "true"); // TODO: Implement this properly
    localConfiguration.put("ignoreRiskFactorsGatherer", "true"); // TODO: Implement this properly
    localConfiguration.put("permissive", "false");
    factory.init(repo, localConfiguration);
    final FunctionCompilationContext compilation = repo.findInstance(FunctionCompilationContext.class, "test");
    assertNotNull(compilation);
    assertNull(compilation.getComputationTargetResolver());
    assertNotNull(compilation.getRawComputationTargetResolver());
    final FunctionExecutionContext execution = repo.findInstance(FunctionExecutionContext.class, "test");
    assertNotNull(execution);
    assertNotNull(OpenGammaExecutionContext.getConfigSource(execution));
    assertNotNull(repo.findInstance(ComputationTargetResolver.class, "testRemoteEngine"));
    assertNotNull(repo.findInstance(SecuritySource.class, "testRemoteEngine"));
    assertNotNull(repo.findInstance(ConfigSource.class, "testRemoteEngine"));
  }

  @Test(groups = TestGroup.UNIT)
  public void testLocalSimulation() throws Exception {
    final MutableFudgeMsg configurationMsg = FudgeContext.GLOBAL_DEFAULT.newMessage();
    final RemoteEngineContextsComponentFactory factory = new RemoteEngineContextsComponentFactory() {

      @Override
      protected Pair<UriEndPointDescriptionProvider.Validater, FudgeMsg> fetchConfiguration() {
        return Pairs.<UriEndPointDescriptionProvider.Validater, FudgeMsg>of(null, configurationMsg);
      }

      @Override
      protected URI fetchURI(final Pair<UriEndPointDescriptionProvider.Validater, FudgeMsg> remoteConfiguration, final String label) {
        try {
          return new URI("http://localhost/mock/" + label);
        } catch (URISyntaxException e) {
          return null;
        }
      }

    };
    factory.setConfiguration(new URI("http://localhost/foo/bar"));
    testFactory(factory);
  }

  @Test(groups = TestGroup.INTEGRATION)
  public void testRemoteAccess() throws Exception {
    final Properties properties = TestProperties.getTestProperties();
    final String host = properties.getProperty("opengamma.engine.host", "localhost");
    final String port = properties.getProperty("opengamma.engine.port", "8080");
    final String path = properties.getProperty("opengamma.engine.path", "/");
    final RemoteEngineContextsComponentFactory factory = new RemoteEngineContextsComponentFactory();
    factory.setConfiguration(new URI("http://" + host + ":" + port + path + "jax/configuration/0"));
    final CacheManager cacheManager = EHCacheUtils.createTestCacheManager(RemoteEngineContextsComponentFactoryTest.class);
    try {
      factory.setCacheManager(cacheManager);
      testFactory(factory);
    } finally {
      cacheManager.clearAll();
      cacheManager.shutdown();
    }
  }

}
