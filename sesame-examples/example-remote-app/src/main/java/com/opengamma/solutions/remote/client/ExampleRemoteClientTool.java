/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.solutions.remote.client;

import static com.opengamma.sesame.config.ConfigBuilder.configureView;

import java.net.URI;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.threeten.bp.LocalDate;
import org.threeten.bp.ZonedDateTime;

import com.google.common.collect.ImmutableList;
import com.opengamma.component.tool.AbstractTool;
import com.opengamma.core.link.ConfigLink;
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
import com.opengamma.sesame.config.ViewConfig;
import com.opengamma.sesame.engine.CalculationArguments;
import com.opengamma.sesame.engine.RemoteViewRunner;
import com.opengamma.sesame.engine.ResultRow;
import com.opengamma.sesame.engine.Results;
import com.opengamma.sesame.engine.ViewRunner;
import com.opengamma.sesame.marketdata.MarketDataEnvironmentBuilder;
import com.opengamma.sesame.trade.InterestRateSwapTrade;
import com.opengamma.solutions.util.SwapViewUtils;
import com.opengamma.solutions.util.ViewUtils;
import com.opengamma.util.time.DateUtils;

/** The entry point for running an example remote view. */
@Scriptable
public class ExampleRemoteClientTool extends AbstractTool<ToolContext> {

  /** Exposure Function name flag */
  private static final String EXPOSURE_FUNCTION = "ef";
  /** Snapshot uid flag */
  private static final String SNAPSHOT_UID = "s";
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
    ViewRunner viewRunner = new RemoteViewRunner(URI.create(url));

    /* Single cycle options containing the market data specification and valuation time. */
    CalculationArguments.Builder builder  = CalculationArguments.builder();
    if (commandLine.hasOption(VALUATION_DATE)) {
      LocalDate val = DateUtils.toLocalDate(commandLine.getOptionValue(VALUATION_DATE));
      builder.valuationTime(DateUtils.getUTCDate(val.getYear(), val.getMonthValue(), val.getDayOfMonth()));
    } else {
      builder.valuationTime(ZonedDateTime.now());
    }

    UniqueId snapshotId = UniqueId.parse(commandLine.getOptionValue(SNAPSHOT_UID));
    MarketDataSpecification marketDataSpecification = UserMarketDataSpecification.of(snapshotId);
    builder.marketDataSpecification(marketDataSpecification);

    if (commandLine.hasOption(SECURITY_INPUTS)) {
      SecurityMaster securityMaster = s_context.getSecurityMaster();
      SecurityDocument doc = securityMaster.get(UniqueId.parse(commandLine.getOptionValue(SECURITY_INPUTS)));
      inputs.add(doc.getSecurity());
    } else {
      inputs.addAll(SwapViewUtils.SWAP_INPUTS);
    }

    CalculationArguments calculationArguments = builder.build();

    /* Configuration links matching the curve exposure function and currency matrix as named on the remote server.
       These are needed as specific arguments in the creation of the ViewConfig. */
    ConfigLink<ExposureFunctions> exposureConfig =
        ConfigLink.resolvable(commandLine.getOptionValue(EXPOSURE_FUNCTION), ExposureFunctions.class);
    ConfigLink<CurrencyMatrix> currencyMatrixLink = ConfigLink.resolvable("BBG-Matrix", CurrencyMatrix.class);

    /* Building the output specific request, based on a the view config, the single cycle options
       and the List<ManageableSecurity> containing the swaps */
    ViewConfig viewConfig = configureView(
        "IRS remote view",
        SwapViewUtils.createInterestRateSwapViewColumn(OutputNames.PRESENT_VALUE,
                                                       exposureConfig,
                                                       currencyMatrixLink),
        SwapViewUtils.createInterestRateSwapViewColumn(OutputNames.BUCKETED_PV01,
                                                       exposureConfig,
                                                       currencyMatrixLink));
    /* Execute the engine cycle and extract the first result result */
    Results results = viewRunner.runView(viewConfig,
                                          calculationArguments,
                                          MarketDataEnvironmentBuilder.empty(),
                                          inputs.build());

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
      // Output PV
      ViewUtils.outputMultipleCurrencyAmount(name, row.get(0).getResult());
      // Output Bucketed PV01
      ViewUtils.outputBucketedCurveSensitivities(name, row.get(1).getResult());
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
    option.setRequired(true);
    return option;
  }

}
