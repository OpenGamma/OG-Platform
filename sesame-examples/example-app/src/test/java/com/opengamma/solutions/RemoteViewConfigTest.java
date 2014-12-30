/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.solutions;

import static com.opengamma.sesame.config.ConfigBuilder.configureView;
import static com.opengamma.util.result.ResultTestUtils.assertSuccess;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;

import java.net.URI;
import java.util.Collection;
import java.util.List;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.opengamma.component.ComponentInfo;
import com.opengamma.component.ComponentServer;
import com.opengamma.component.rest.RemoteComponentServer;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.core.config.impl.ConfigItem;
import com.opengamma.core.config.impl.RemoteConfigSource;
import com.opengamma.core.link.ConfigLink;
import com.opengamma.core.marketdatasnapshot.impl.ManageableMarketDataSnapshot;
import com.opengamma.core.marketdatasnapshot.impl.RemoteMarketDataSnapshotSource;
import com.opengamma.engine.marketdata.spec.MarketDataSpecification;
import com.opengamma.engine.marketdata.spec.UserMarketDataSpecification;
import com.opengamma.financial.analytics.curve.exposure.ExposureFunctions;
import com.opengamma.financial.currency.CurrencyMatrix;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.config.ConfigDocument;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.master.config.impl.RemoteConfigMaster;
import com.opengamma.sesame.OutputNames;
import com.opengamma.sesame.config.ViewConfig;
import com.opengamma.sesame.engine.CalculationArguments;
import com.opengamma.sesame.engine.Engine;
import com.opengamma.sesame.engine.RemoteEngine;
import com.opengamma.sesame.engine.Results;
import com.opengamma.sesame.marketdata.MarketDataEnvironment;
import com.opengamma.sesame.marketdata.MarketDataEnvironmentBuilder;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.result.Result;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Integration tests run against a remote server
 * Test the creation and access of a view config
 */
@Test(groups = TestGroup.INTEGRATION)
public class RemoteViewConfigTest {

  private ConfigLink<ExposureFunctions> _exposureConfig;
  private ConfigLink<CurrencyMatrix> _currencyMatrixLink;
  private String _url;
  private static final String CONFIG_NAME = "Remote view";

  @BeforeClass
  public void setUp() {
    String property = System.getProperty("server.url");
    _url = property == null ? "http://localhost:8080/jax/" : property;
    _exposureConfig = ConfigLink.resolvable("USD-GBP-FF-1", ExposureFunctions.class);
    _currencyMatrixLink = ConfigLink.resolvable("BBG-Matrix", CurrencyMatrix.class);
  }

  @Test(enabled = true)
  public void persistAndAccessViewConfig() {

    // Access the components running on the remote server
    RemoteComponentServer remoteComponentServer = new RemoteComponentServer(URI.create(_url));
    ComponentServer componentServer = remoteComponentServer.getComponentServer();

    // Get the info on the ConfigMaster (write access)
    ComponentInfo configMasterInfo = componentServer.getComponentInfo(ConfigMaster.class, "default");

    // Create the config item and document
    ConfigItem<ViewConfig> columnConfigItem = ConfigItem.of(createViewConfig(), "View Config", ViewConfig.class);
    ConfigDocument doc = new ConfigDocument(columnConfigItem);

    // Persist in the Master
    RemoteConfigMaster configMaster = new RemoteConfigMaster(configMasterInfo.getUri());
    configMaster.add(doc);

    // Get the info on the ConfigSource (read access)
    ComponentInfo configSourceInfo = componentServer.getComponentInfo(ConfigSource.class, "default");
    RemoteConfigSource configSource = new RemoteConfigSource(configSourceInfo.getUri());

    // Query the source
    ViewConfig configItem = configSource.getSingle(ViewConfig.class,
                                                    "View Config",
                                                    VersionCorrection.LATEST);

    assertThat(configItem.getName(), is(CONFIG_NAME));
    assertThat(configItem.getColumns().size(), is(1));
    assertThat(configItem.getColumns().get(0).getName(), is(OutputNames.PRESENT_VALUE));
  }

  private ViewConfig createViewConfig() {
    return
        configureView(
            CONFIG_NAME,
            RemoteViewFraUtils.createFraViewColumn(
                OutputNames.PRESENT_VALUE,
                _exposureConfig,
                _currencyMatrixLink));
  }

}
