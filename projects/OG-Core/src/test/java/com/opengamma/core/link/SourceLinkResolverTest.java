/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.link;


import org.testng.annotations.Test;

import com.opengamma.core.config.ConfigSource;
import com.opengamma.id.VersionCorrection;
import com.opengamma.service.VersionCorrectionProvider;
import com.opengamma.util.test.TestGroup;

@Test(groups = TestGroup.UNIT)
public class SourceLinkResolverTest {

  @Test(expectedExceptions = IllegalStateException.class)
  public void noThreadLocalContextGivesError() {

    final SourceLinkResolver<String, Object, ConfigSource> resolver =
        new SourceLinkResolver<String, Object, ConfigSource>("id1", null) {
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

    resolver.resolve();
  }

}
