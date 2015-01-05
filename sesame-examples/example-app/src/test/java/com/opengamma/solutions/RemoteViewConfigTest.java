/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.solutions;

import static com.opengamma.sesame.config.ConfigBuilder.configureView;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.opengamma.core.config.ConfigSource;
import com.opengamma.core.config.impl.ConfigItem;
import com.opengamma.core.link.ConfigLink;
import com.opengamma.financial.analytics.curve.exposure.ExposureFunctions;
import com.opengamma.financial.currency.CurrencyMatrix;
import com.opengamma.id.VersionCorrection;
import com.opengamma.integration.server.RemoteServer;
import com.opengamma.master.config.ConfigDocument;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.sesame.OutputNames;
import com.opengamma.sesame.config.ViewConfig;
import com.opengamma.solutions.util.RemoteViewFraUtils;
import com.opengamma.util.test.TestGroup;

/**
 * Integration tests run against a remote server
 * Test the creation and access of a view config
 */
@Test(groups = TestGroup.INTEGRATION)
public class RemoteViewConfigTest {

  private ConfigLink<ExposureFunctions> _exposureConfig;
  private ConfigLink<CurrencyMatrix> _currencyMatrixLink;
  private String _url;

  @BeforeClass
  public void setUp() {
    String serverUrl = System.getProperty("server.url");
    _url = serverUrl == null ? RemoteTestUtils.LOCALHOST : serverUrl;
    _exposureConfig = ConfigLink.resolvable(RemoteTestUtils.USD_GBP_FF_EXPOSURE, ExposureFunctions.class);
    _currencyMatrixLink = ConfigLink.resolvable(RemoteTestUtils.CURRENCY_MATRIX, CurrencyMatrix.class);
  }

  @Test(enabled = true)
  public void persistAndAccessViewConfig() {

    RemoteServer server = RemoteServer.create(_url);

    // Create the config item and document
    ConfigItem<ViewConfig> columnConfigItem = ConfigItem.of(createViewConfig(), "View Config", ViewConfig.class);
    ConfigDocument doc = new ConfigDocument(columnConfigItem);

    // Persist in the Master
    ConfigMaster configMaster = server.getConfigMaster();
    configMaster.add(doc);

    // Get the ConfigSource (read access)
    ConfigSource configSource = server.getConfigSource();

    // Query the source
    ViewConfig configItem = configSource.getSingle(ViewConfig.class,
                                                   "View Config",
                                                   VersionCorrection.LATEST);

    assertThat(configItem.getName(), is(RemoteTestUtils.CONFIG_NAME));
    assertThat(configItem.getColumns().size(), is(1));
    assertThat(configItem.getColumns().get(0).getName(), is(OutputNames.PRESENT_VALUE));
  }

  private ViewConfig createViewConfig() {
    return
        configureView(
            RemoteTestUtils.CONFIG_NAME,
            RemoteViewFraUtils.createFraViewColumn(
                OutputNames.PRESENT_VALUE,
                _exposureConfig,
                _currencyMatrixLink));
  }

}
