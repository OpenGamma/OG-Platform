/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.marketdata;

import static com.google.common.collect.Sets.newHashSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.Clock;
import org.threeten.bp.LocalDate;

import com.opengamma.bbg.BloombergIdentifierProvider;
import com.opengamma.bbg.loader.hts.BloombergHistoricalTimeSeriesLoader;
import com.opengamma.component.tool.AbstractTool;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.financial.analytics.ircurve.ConfigDBInterpolatedYieldCurveSpecificationBuilder;
import com.opengamma.financial.analytics.ircurve.FixedIncomeStripWithIdentifier;
import com.opengamma.financial.analytics.ircurve.InterpolatedYieldCurveSpecification;
import com.opengamma.financial.analytics.ircurve.InterpolatedYieldCurveSpecificationBuilder;
import com.opengamma.financial.analytics.ircurve.YieldCurveDefinition;
import com.opengamma.financial.convention.ConventionBundle;
import com.opengamma.financial.convention.ConventionBundleMaster;
import com.opengamma.financial.convention.DefaultConventionBundleSource;
import com.opengamma.financial.convention.InMemoryConventionBundleMaster;
import com.opengamma.id.ExternalId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.integration.tool.IntegrationToolContext;
import com.opengamma.master.config.ConfigDocument;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.master.config.ConfigSearchRequest;
import com.opengamma.master.config.impl.ConfigSearchIterator;
import com.opengamma.scripts.Scriptable;
import com.opengamma.util.money.Currency;

/**
 */
@Scriptable
public class CurveHtsResolverTool extends AbstractTool<IntegrationToolContext> {

  /** Logger. */
  private static Logger s_logger = LoggerFactory.getLogger(CurveHtsResolverTool.class);

  /** Portfolio name option flag */
  private static final String CURVE_NAME_OPT = "n";
  /** Write option flag */
  private static final String WRITE_OPT = "w";
  /** Verbose option flag */
  private static final String VERBOSE_OPT = "v";
  /** Time series data provider option flag */
  private static final String TIME_SERIES_DATAPROVIDER_OPT = "p";
  /** Time series data field option flag */
  private static final String TIME_SERIES_DATAFIELD_OPT = "d";

  //-------------------------------------------------------------------------
  /**
   * Main method to run the tool.
   * 
   * @param args  the standard tool arguments, not null
   */
  public static void main(final String[] args) { // CSIGNORE
    new CurveHtsResolverTool().invokeAndTerminate(args);
  }

  //-------------------------------------------------------------------------
  @SuppressWarnings("unchecked")
  @Override
  protected void doRun() {

    Set<ExternalId> curveNodesExternalIds;

    Set<ExternalId> initialRateExternalIds;

    final ConfigSource configSource = getToolContext().getConfigSource();
    final ConfigMaster configMaster = getToolContext().getConfigMaster();

    // Find all matching curves
    final List<YieldCurveDefinition> curves = getCurveDefinitionNames(configMaster, getCommandLine().getOptionValue(CURVE_NAME_OPT));

    // Get initial rate hts external ids for curves
    final Set<Currency> currencies = newHashSet();
    for (final YieldCurveDefinition curve : curves) {
      currencies.add(curve.getCurrency());
    }
    initialRateExternalIds = getInitialRateExternalIds(currencies);

    // Get all other required hts external ids for curves
    final List<LocalDate> dates = buildDates();
    final Set<String> curveNames = new HashSet<>();
    for (YieldCurveDefinition ycd : curves) {
      curveNames.add(ycd.getName() + "_" + ycd.getCurrency().getCode());
    }
    curveNodesExternalIds = getCurveRequiredExternalIds(configSource, curveNames, dates);

    // Load the required time series
    loadHistoricalData(getCommandLine().hasOption(WRITE_OPT), getCommandLine().getOptionValues(TIME_SERIES_DATAFIELD_OPT) == null ? new String[] {"PX_LAST" } : getCommandLine()
        .getOptionValues(TIME_SERIES_DATAFIELD_OPT),
        getCommandLine().getOptionValue(TIME_SERIES_DATAPROVIDER_OPT) == null ? "CMPL" : getCommandLine().getOptionValue(TIME_SERIES_DATAPROVIDER_OPT), initialRateExternalIds,
        curveNodesExternalIds);
  }

  private Set<ExternalId> getInitialRateExternalIds(final Set<Currency> currencies) {
    final ConventionBundleMaster cbm = new InMemoryConventionBundleMaster();
    final DefaultConventionBundleSource cbs = new DefaultConventionBundleSource(cbm);
    final Set<ExternalId> externalInitialRateId = newHashSet();
    for (final Currency currency : currencies) {
      for (final String swapType : new String[] {"SWAP", "3M_SWAP", "6M_SWAP" }) {
        final String product = currency.getCode() + "_" + swapType;
        final ConventionBundle convention = cbs.getConventionBundle(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, product));
        if (convention != null) {
          final ExternalId initialRate = convention.getSwapFloatingLegInitialRate();
          final ConventionBundle realIdConvention = cbs.getConventionBundle(initialRate);
          if (realIdConvention != null) {
            externalInitialRateId.add(realIdConvention.getIdentifiers().getExternalId(ExternalSchemes.BLOOMBERG_TICKER));
          } else {
            s_logger.error("No convention for {}", initialRate.toString());
          }
        } else {
          s_logger.warn("No convention for {} product", product);
        }
      }
    }
    return externalInitialRateId;
  }

  /**
   * Generate quarterly dates +/- 2 years around today to cover futures from past and near future
   * 
   * @return list of dates
   */
  private List<LocalDate> buildDates() {
    final Clock clock = Clock.systemDefaultZone();
    final List<LocalDate> dates = new ArrayList<LocalDate>();
    final LocalDate twoYearsAgo = LocalDate.now(clock).minusYears(2);
    final LocalDate twoYearsTime = LocalDate.now(clock).plusYears(2);
    for (LocalDate next = twoYearsAgo; next.isBefore(twoYearsTime); next = next.plusMonths(3)) {
      dates.add(next);
    }
    return dates;
  }

  /**
   * Get all the curve definition config object names specified by glob expression.
   * 
   * @param configMaster
   * @param nameExpr glob type expression - e.g. blah*
   * @return list of names of config objects matching glob expression
   */
  private List<YieldCurveDefinition> getCurveDefinitionNames(final ConfigMaster configMaster, final String nameExpr) {
    final List<YieldCurveDefinition> results = new ArrayList<YieldCurveDefinition>();
    final ConfigSearchRequest<YieldCurveDefinition> request = new ConfigSearchRequest<YieldCurveDefinition>(YieldCurveDefinition.class);
    request.setName(nameExpr);
    for (final ConfigDocument doc : ConfigSearchIterator.iterable(configMaster, request)) {
      results.add((YieldCurveDefinition) doc.getConfig().getValue());
    }
    return results;
  }

  /**
   * For a given list of curve names, on a given list of dates, get the superset of all ids required by those curves.
   * 
   * @param configSource
   * @param names
   * @param dates
   * @return list of all ids required by curves
   */
  private Set<ExternalId> getCurveRequiredExternalIds(final ConfigSource configSource, final Collection<String> names, final List<LocalDate> dates) {
    final Set<ExternalId> externalIds = newHashSet();
    for (final String name : names) {
      s_logger.info("Processing curve " + name);
      YieldCurveDefinition curveDefinition = configSource.getSingle(YieldCurveDefinition.class, name, VersionCorrection.LATEST);
      if (curveDefinition != null) {
        InterpolatedYieldCurveSpecificationBuilder builder = new ConfigDBInterpolatedYieldCurveSpecificationBuilder(configSource);
        for (LocalDate date : dates) {
          s_logger.info("Processing curve date " + date);
          try {
            final InterpolatedYieldCurveSpecification curveSpec = builder.buildCurve(date, curveDefinition, VersionCorrection.LATEST);
            for (final FixedIncomeStripWithIdentifier strip : curveSpec.getStrips()) {
              s_logger.info("Processing strip " + strip.getSecurity());
              externalIds.add(strip.getSecurity());
            }
          } catch (final Throwable t) {
            s_logger.warn("Unable to build curve " + t.getMessage());
          }
        }
      } else {
        s_logger.warn("No curve definition with '{}' name", name);
      }
    }
    return externalIds;
  }

  private void loadHistoricalData(final boolean write, final String[] dataFields, final String dataProvider, final Set<ExternalId>... externalIdSets) {
    final BloombergHistoricalTimeSeriesLoader loader = new BloombergHistoricalTimeSeriesLoader(getToolContext().getHistoricalTimeSeriesMaster(), getToolContext()
        .getHistoricalTimeSeriesProvider(), new BloombergIdentifierProvider(getToolContext().getBloombergReferenceDataProvider()));

    for (final Set<ExternalId> externalIds : externalIdSets) {
      if (externalIds.size() > 0) {
        for (final String dataField : dataFields) {
          s_logger.info("Loading time series (field: " + dataField + ", provider: " + dataProvider + ") with external IDs " + externalIds);
          if (write) {
            loader.loadTimeSeries(externalIds, dataProvider, dataField, LocalDate.now().minusYears(1), null);
          }
        }
      }
    }
  }

  @Override
  protected Options createOptions(final boolean contextProvided) {

    final Options options = super.createOptions(contextProvided);

    final Option curveNameOption = new Option(CURVE_NAME_OPT, "name", true, "The name of the yield curve definition for which to resolve time series");
    curveNameOption.setRequired(true);
    options.addOption(curveNameOption);

    final Option writeOption = new Option(WRITE_OPT, "write", false, "Actually persists the time series to the database if specified, otherwise pretty-prints without persisting");
    options.addOption(writeOption);

    final Option verboseOption = new Option(VERBOSE_OPT, "verbose", false, "Displays progress messages on the terminal");
    options.addOption(verboseOption);

    final Option timeSeriesDataProviderOption = new Option(TIME_SERIES_DATAPROVIDER_OPT, "provider", true, "The name of the time series data provider");
    options.addOption(timeSeriesDataProviderOption);

    final Option timeSeriesDataFieldOption = new Option(TIME_SERIES_DATAFIELD_OPT, "field", true, "The name(s) of the time series data field(s)");
    options.addOption(timeSeriesDataFieldOption);

    return options;
  }

}
