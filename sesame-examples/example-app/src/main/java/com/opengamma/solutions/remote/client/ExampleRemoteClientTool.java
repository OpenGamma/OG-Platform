/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.solutions.remote.client;

import com.google.common.collect.ImmutableList;
import com.opengamma.component.tool.AbstractTool;
import com.opengamma.core.link.ConfigLink;
import com.opengamma.engine.marketdata.spec.LiveMarketDataSpecification;
import com.opengamma.engine.marketdata.spec.MarketDataSpecification;
import com.opengamma.engine.marketdata.spec.UserMarketDataSpecification;
import com.opengamma.financial.analytics.curve.exposure.ExposureFunctions;
import com.opengamma.financial.currency.CurrencyMatrix;
import com.opengamma.financial.security.irs.InterestRateSwapSecurity;
import com.opengamma.financial.tool.ToolContext;
import com.opengamma.id.UniqueId;
import com.opengamma.master.security.SecurityDocument;
import com.opengamma.master.security.SecurityMaster;
import com.opengamma.scripts.Scriptable;
import com.opengamma.sesame.OutputNames;
import com.opengamma.sesame.engine.ResultRow;
import com.opengamma.sesame.engine.Results;
import com.opengamma.sesame.server.FunctionServer;
import com.opengamma.sesame.server.FunctionServerRequest;
import com.opengamma.sesame.server.IndividualCycleOptions;
import com.opengamma.sesame.server.RemoteFunctionServer;
import com.opengamma.sesame.trade.InterestRateSwapTrade;
import com.opengamma.solutions.util.SwapViewUtils;
import com.opengamma.util.time.DateUtils;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.threeten.bp.LocalDate;
import org.threeten.bp.ZonedDateTime;

import java.net.URI;

import static com.opengamma.sesame.config.ConfigBuilder.configureView;

/** The entry point for running an example remote view. */
@Scriptable
public class ExampleRemoteClientTool extends AbstractTool<ToolContext> {

  /** Exposure Function name flag */
  private static final String EXPOSURE_FUNCTION = "ef";
  /** Snapshot uid flag */
  private static final String SNAPSHOT_UID = "s";
  /** Live data flag name flag */
  private static final String LIVE_DATA = "ld";
  /** Valuation data flag. Format: YYYYMMDD */
  private static final String VALUATION_DATE = "d";
  /** Security inputs */
  private static final String SECURITY_INPUTS = "is";

  private static ToolContext s_context;

  //-------------------------------------------------------------------------
  /**
   * Main method to run the tool.
   *
   * @param args the standard tool arguments, not null
   */
  public static void main(final String[] args) { // CSIGNORE
    new ExampleRemoteClientTool().invokeAndTerminate(args);
  }

  //-------------------------------------------------------------------------
  @Override
  protected void doRun() throws Exception {

    s_context = getToolContext();
    CommandLine commandLine  = getCommandLine();
    ImmutableList.Builder<Object> inputs = ImmutableList.<Object>builder();

    /* Create a RemoteFunctionServer to executes view requests RESTfully.*/
    String url = commandLine.getOptionValue(CONFIG_RESOURCE_OPTION) + "/jax";
    FunctionServer functionServer = new RemoteFunctionServer(URI.create(url));

    /* Single cycle options containing the market data specification and valuation time. */
    IndividualCycleOptions.Builder builder = IndividualCycleOptions.builder();
    if (commandLine.hasOption(VALUATION_DATE)) {
      LocalDate val = DateUtils.toLocalDate(commandLine.getOptionValue(VALUATION_DATE));
      builder.valuationTime(DateUtils.getUTCDate(val.getYear(), val.getMonthValue(), val.getDayOfMonth()));
    } else {
      builder.valuationTime(ZonedDateTime.now());
    }

    if (commandLine.hasOption(SNAPSHOT_UID)) {
      UniqueId snapshotId = UniqueId.parse(commandLine.getOptionValue(SNAPSHOT_UID));
      MarketDataSpecification marketDataSpecification = UserMarketDataSpecification.of(snapshotId);
      builder.marketDataSpecs(ImmutableList.of(marketDataSpecification));
    } else if (commandLine.hasOption(LIVE_DATA)) {
      String liveDataSource = commandLine.getOptionValue(LIVE_DATA);
      MarketDataSpecification marketDataSpecification = LiveMarketDataSpecification.of(liveDataSource);
      builder.marketDataSpecs(ImmutableList.of(marketDataSpecification));
    } else {
      // Default to Bloomberg if snapshot or live data provider is not stipulated
      MarketDataSpecification marketDataSpecification = LiveMarketDataSpecification.of("Bloomberg");
      builder.marketDataSpecs(ImmutableList.of(marketDataSpecification));
    }

    if (commandLine.hasOption(SECURITY_INPUTS)) {
      SecurityMaster securityMaster = s_context.getSecurityMaster();
      SecurityDocument doc = securityMaster.get(UniqueId.parse(commandLine.getOptionValue(SECURITY_INPUTS)));
      inputs.add(doc.getSecurity());
    } else {
      inputs.addAll(SwapViewUtils.SWAP_INPUTS);
    }

    IndividualCycleOptions cycleOptions = builder.build();

    /* Configuration links matching the curve exposure function and currency matrix as named on the remote server.
       These are needed as specific arguments in the creation of the ViewConfig. */
    ConfigLink<ExposureFunctions> exposureConfig =
        ConfigLink.resolvable(commandLine.getOptionValue(EXPOSURE_FUNCTION), ExposureFunctions.class);
    ConfigLink<CurrencyMatrix> currencyMatrixLink = ConfigLink.resolvable("BBG-Matrix", CurrencyMatrix.class);


    /* Building the output specific request, based on a the view config, the single cycle options
       and the List<ManageableSecurity> containing the swaps */
    FunctionServerRequest<IndividualCycleOptions> request =
        FunctionServerRequest.<IndividualCycleOptions>builder()
            .viewConfig(configureView(
                "IRS remote view",
                SwapViewUtils.createInterestRateSwapViewColumn(OutputNames.PRESENT_VALUE,
                                                               exposureConfig,
                                                               currencyMatrixLink),
                SwapViewUtils.createInterestRateSwapViewColumn(OutputNames.BUCKETED_PV01,
                                                               exposureConfig,
                                                               currencyMatrixLink)))
            .inputs(inputs.build())
            .cycleOptions(cycleOptions)
            .build();

    /* Execute the engine cycle and extract the first result result */
    Results results = functionServer.executeSingleCycle(request);

    for (ResultRow row : results.getRows()) {
      Object input = row.getInput();
      String name = "Security";
      if (input instanceof InterestRateSwapTrade) {
        name = ((InterestRateSwapTrade) input).getTradeBundle().getSecurity().getName();
      } else if (input instanceof InterestRateSwapSecurity) {
        name = ((InterestRateSwapSecurity) input).getName();
      } else {
        System.out.println("Unsupported Output");
      }
    }
    System.exit(0);
  }

  //-------------------------------------------------------------------------
  @Override
  protected Options createOptions(boolean mandatoryConfig) {
    final Options options = super.createOptions(mandatoryConfig);
    options.addOption(createSnapshotUidOption());
    options.addOption(createValuationDateOption());
    options.addOption(createExposureFunctionOption());
    options.addOption(createLiveDataNameOption());
    options.addOption(createSecurityInputsOption());
    return options;
  }

  private static Option createValuationDateOption() {
    final Option option = new Option(VALUATION_DATE, "valuationDate", true,
                                     "valuation Date, in the format YYYYMMDD, default to now");
    option.setArgName("valuation date");
    return option;
  }

  private static Option createSecurityInputsOption() {
    final Option option = new Option(SECURITY_INPUTS, "inputs", true, "security inputs by ID");
    option.setArgName("security inputs");
    return option;
  }

  private static Option createExposureFunctionOption() {
    final Option option = new Option(EXPOSURE_FUNCTION, "exposureFunction", true,
                                     "name of the exposure function configuration");
    option.setRequired(true);
    option.setArgName("exposure function");
    return option;
  }

  private static Option createSnapshotUidOption() {
    final Option option = new Option(SNAPSHOT_UID, "snapshotUid", true, "snapshot unique identifier to use");
    option.setArgName("snapshot uid");
    option.setOptionalArg(true);
    return option;
  }

  private static Option createLiveDataNameOption() {
    final Option option = new Option(LIVE_DATA,
        "liveData",
        true,
        "live data provider, defaults to Bloomberg if no snapshot or live data is specified");
    option.setArgName("live data provider");
    option.setOptionalArg(true);
    return option;
  }

}
