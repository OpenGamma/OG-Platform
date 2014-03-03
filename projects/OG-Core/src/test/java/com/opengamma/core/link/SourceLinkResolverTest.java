/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.link;


import static org.mockito.Mockito.mock;

import java.util.HashMap;
import java.util.Map;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.opengamma.core.config.ConfigSource;
import com.opengamma.id.VersionCorrection;
import com.opengamma.service.ServiceContext;
import com.opengamma.service.ThreadLocalServiceContext;
import com.opengamma.service.VersionCorrectionProvider;
import com.opengamma.util.test.TestGroup;

@Test(groups = TestGroup.UNIT)
public class SourceLinkResolverTest {

  @BeforeMethod
  public void setup() {
    // Ensure we don't have a thread local service context which could be used
    ThreadLocalServiceContext.init(null);
  }

  @Test(expectedExceptions = IllegalStateException.class)
  public void noThreadLocalContextGivesError() {

    final SourceLinkResolver<String, Object, ConfigSource> resolver =
        createSourceLinkResolver(null);

    resolver.resolve();
  }

  public void threadLocalContextGetsUsed() {

    ServiceContext serviceContext = createContext(ConfigSource.class, VersionCorrectionProvider.class);

    ThreadLocalServiceContext.init(serviceContext);
    SourceLinkResolver<String, Object, ConfigSource> resolver = createSourceLinkResolver(null);

    resolver.resolve();
  }

  private ServiceContext createContext(Class<?>... services) {

    Map<Class<?>, Object> serviceMap = new HashMap<>();
    for (Class<?> aClass : services) {
      serviceMap.put(aClass, mock(aClass));
    }
    return ServiceContext.of(serviceMap);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void noVersionCorrectionGivesError() {

    ServiceContext serviceContext = createContext(ConfigSource.class);
    SourceLinkResolver<String, Object, ConfigSource> resolver = createSourceLinkResolver(serviceContext);

    resolver.resolve();
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void noSourceGivesError() {

    ServiceContext serviceContext = createContext(VersionCorrectionProvider.class);
    SourceLinkResolver<String, Object, ConfigSource> resolver = createSourceLinkResolver(serviceContext);

    resolver.resolve();
  }

  private SourceLinkResolver<String, Object, ConfigSource> createSourceLinkResolver(final ServiceContext serviceContext) {
    return new SourceLinkResolver<String, Object, ConfigSource>("id1", serviceContext) {
        @Override
        protected Class<ConfigSource> getSourceClass() {
          return ConfigSource.class;
        }

        @Override
        protected VersionCorrection getVersionCorrection(VersionCorrectionProvider vcProvider) {
          return vcProvider.getConfigVersionCorrection();
        }

        @Override
        protected Object executeQuery(ConfigSource source, VersionCorrection versionCorrection) {
          return source.getLatestByName(Object.class, getIdentifier());
        }
      };
  }

}
