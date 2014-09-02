/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.solutions;

import com.opengamma.core.link.ConfigLink;
import com.opengamma.engine.marketdata.spec.UserMarketDataSpecification;
import com.opengamma.financial.analytics.curve.exposure.ExposureFunctions;
import com.opengamma.financial.currency.CurrencyMatrix;
import com.opengamma.financial.security.irs.InterestRateSwapSecurity;
import com.opengamma.id.UniqueId;
import com.opengamma.sesame.OutputNames;
import com.opengamma.sesame.config.ViewConfig;
import com.opengamma.sesame.engine.ResultRow;
import com.opengamma.sesame.engine.Results;
import com.opengamma.sesame.server.FunctionServer;
import com.opengamma.sesame.server.FunctionServerRequest;
import com.opengamma.sesame.server.IndividualCycleOptions;
import com.opengamma.sesame.server.RemoteFunctionServer;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.net.URI;

import static com.opengamma.sesame.config.ConfigBuilder.configureView;

/**
 * Integration tests run against a remote server
 * Input: Vanilla Interest Rate Swaps, Snapshot Market Data
 * Output: Present Value
 */

@Test(groups = TestGroup.INTEGRATION, enabled = true)
public class RemoteVanillaSwapTest {

  private static final String URL = "http://localhost:8080/jax";
  private FunctionServer _functionServer;
  private IndividualCycleOptions _cycleOptions;
  private ConfigLink<ExposureFunctions> _exposureConfig;
  private ConfigLink<CurrencyMatrix> _currencyMatrixLink;

  @BeforeClass
  public void setUp() {

    _functionServer = new RemoteFunctionServer(URI.create(URL));
    _cycleOptions = IndividualCycleOptions.builder()
        .valuationTime(DateUtils.getUTCDate(2014, 1, 22))
        .marketDataSpec(UserMarketDataSpecification.of(UniqueId.of("DbSnp", "1000")))
        .build();

    _exposureConfig = ConfigLink.resolvable("USD CSA Exposure Functions", ExposureFunctions.class);
    _currencyMatrixLink = ConfigLink.resolvable("BloombergLiveData", CurrencyMatrix.class);

  }

  private ViewConfig createViewConfig() {

    return
        configureView(
            "IRS Remote view",
            RemoteViewUtils.createInterestRateSwapViewColumn(OutputNames.PRESENT_VALUE,
                                                             _exposureConfig,
                                                             _currencyMatrixLink),
            RemoteViewUtils.createInterestRateSwapViewColumn(OutputNames.BUCKETED_PV01,
                                                             _exposureConfig,
                                                             _currencyMatrixLink)
        );
  }

  @Test(enabled = true)
  public void testSwapPVExecution() {

    FunctionServerRequest<IndividualCycleOptions> request =
        FunctionServerRequest.<IndividualCycleOptions>builder()
            .viewConfig(createViewConfig())
            .inputs(RemoteViewUtils.VANILLA_INPUTS)
            .cycleOptions(_cycleOptions)
            .build();

    Results results = _functionServer.executeSingleCycle(request);

    for (ResultRow row : results.getRows()) {
      InterestRateSwapSecurity irs =  (InterestRateSwapSecurity) row.getInput();
      // Output PV
      RemoteViewUtils.outputMultipleCurrencyAmount(irs.getName(),
                                                   row.get(0).getResult());
      // Output Bucketed PV01
      RemoteViewUtils.outputBucketedCurveSensitivities(irs.getName(), row.get(1).getResult());
    }

  }

}
