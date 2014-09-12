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
import com.opengamma.id.UniqueId;
import com.opengamma.sesame.OutputNames;
import com.opengamma.sesame.config.ViewConfig;
import com.opengamma.sesame.engine.Results;
import com.opengamma.sesame.server.FunctionServer;
import com.opengamma.sesame.server.FunctionServerRequest;
import com.opengamma.sesame.server.IndividualCycleOptions;
import com.opengamma.sesame.server.RemoteFunctionServer;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.result.Result;
import com.opengamma.util.result.SuccessResult;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.net.URI;

import static com.opengamma.sesame.config.ConfigBuilder.configureView;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;

/**
 * Integration tests run against a remote server
 * Input: Vanilla Interest Rate Swaps, Snapshot Market Data
 * Output: Present Value
 */

@Test(groups = TestGroup.INTEGRATION, enabled = true)
public class RemoteFraTest {

  private static final String URL = "http://localhost:8080/jax";
  private FunctionServer _functionServer;
  private IndividualCycleOptions _cycleOptions;
  private ConfigLink<ExposureFunctions> _exposureConfig;
  private ConfigLink<CurrencyMatrix> _currencyMatrixLink;
  private Results _results;

  private static final double STD_TOLERANCE_PV = 1.0E-3;

  @BeforeClass
  public void setUp() {

    _functionServer = new RemoteFunctionServer(URI.create(URL));
    _cycleOptions = IndividualCycleOptions.builder()
        .valuationTime(DateUtils.getUTCDate(2014, 1, 22))
        .marketDataSpec(UserMarketDataSpecification.of(UniqueId.of("DbSnp", "1000")))
        .build();

    _exposureConfig = ConfigLink.resolvable("USD CSA Exposure Functions", ExposureFunctions.class);
    _currencyMatrixLink = ConfigLink.resolvable("BloombergLiveData", CurrencyMatrix.class);

    FunctionServerRequest<IndividualCycleOptions> request =
        FunctionServerRequest.<IndividualCycleOptions>builder()
            .viewConfig(createViewConfig())
            .inputs(RemoteViewFraUtils.INPUTS)
            .cycleOptions(_cycleOptions)
            .build();

    _results = _functionServer.executeSingleCycle(request);

  }

  private ViewConfig createViewConfig() {

    return
        configureView(
            "FRA Remote view",
            RemoteViewFraUtils.createFraViewColumn(
                OutputNames.PRESENT_VALUE,
                _exposureConfig,
                _currencyMatrixLink)
        );
  }

  @Test(enabled = true)
  public void testForwardRateAgreementPV() {

    Result result = _results.get(0, 0).getResult();
    assertThat(result.isSuccess(), is(true));
    assertThat(result.getValue(), is(instanceOf(MultipleCurrencyAmount.class)));
    MultipleCurrencyAmount mca = (MultipleCurrencyAmount) result.getValue();
    assertThat(mca.getCurrencyAmount(Currency.USD).getAmount(), is(closeTo(21751.363420380032, STD_TOLERANCE_PV)));

  }

}
