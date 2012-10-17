/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.marketdata;


import static com.google.common.collect.Sets.newHashSet;
import static com.opengamma.util.functional.Functional.map;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.time.calendar.Clock;
import javax.time.calendar.LocalDate;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.bbg.BloombergIdentifierProvider;
import com.opengamma.bbg.loader.BloombergHistoricalTimeSeriesLoader;
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
import com.opengamma.integration.tool.IntegrationToolContext;
import com.opengamma.master.config.ConfigDocument;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.master.config.ConfigSearchRequest;
import com.opengamma.master.config.impl.ConfigMasterIterator;
import com.opengamma.util.functional.Function1;
import com.opengamma.util.generate.scripts.Scriptable;
import com.opengamma.util.money.Currency;


/**
 */
@Scriptable
public class CurveHtsResolverTool extends AbstractTool<IntegrationToolContext> {
  /**
   * Logger.
   */
  private static Logger s_logger = LoggerFactory.getLogger(CurveHtsResolverTool.class);

  /** Portfolio name option flag*/
  private static final String CURVE_NAME_OPT = "n";
  /** Write option flag */
  private static final String WRITE_OPT = "w";
  /** Verbose option flag */
  private static final String VERBOSE_OPT = "v";
  /** Time series data provider option flag*/
  private static final String TIME_SERIES_DATAPROVIDER_OPT = "p";
  /** Time series data field option flag*/
  private static final String TIME_SERIES_DATAFIELD_OPT = "d";
  
  //-------------------------------------------------------------------------

  /**
   * Main method to run the tool.
   * No arguments are needed.
   *
   * @param args  the arguments, unused
   */
  public static void main(String[] args) {  // CSIGNORE
    new CurveHtsResolverTool().initAndRun(args, IntegrationToolContext.class);
    System.exit(0);
  }

  //-------------------------------------------------------------------------
  @SuppressWarnings("unchecked")
  @Override
  protected void doRun() {
    
    Set<ExternalId> curveNodesExternalIds;

    Set<ExternalId> initialRateExternalIds;
        
    ConfigSource configSource = getToolContext().getConfigSource();
    ConfigMaster configMaster = getToolContext().getConfigMaster();

    // Find all matching curves
    List<YieldCurveDefinition> curves = getCurveDefinitionNames(configMaster, getCommandLine().getOptionValue(CURVE_NAME_OPT));

    // Get initial rate hts external ids for curves
    Set<Currency> currencies = newHashSet();
    for (YieldCurveDefinition curve : curves) {
      currencies.add(curve.getCurrency());
    }
    initialRateExternalIds = getInitialRateExternalIds(currencies);

    // Get all other required hts external ids for curves
    List<LocalDate> dates = buildDates();
    Set<String> curveNames = map(new HashSet<String>(), curves, new Function1<YieldCurveDefinition, String>() {
      @Override
      public String execute(YieldCurveDefinition yieldCurveDefinition) {
        return yieldCurveDefinition.getName() + "_" + yieldCurveDefinition.getCurrency().getCode();
      }
    });
    curveNodesExternalIds = getCurveRequiredExternalIds(configSource, curveNames, dates);
    
    // Load the required time series
    loadHistoricalData(
        getCommandLine().hasOption(WRITE_OPT),
        getCommandLine().getOptionValues(TIME_SERIES_DATAFIELD_OPT) == null ? new String[] {"PX_LAST"} : getCommandLine().getOptionValues(TIME_SERIES_DATAFIELD_OPT),
        getCommandLine().getOptionValue(TIME_SERIES_DATAPROVIDER_OPT) == null ? "CMPL" : getCommandLine().getOptionValue(TIME_SERIES_DATAPROVIDER_OPT),
        initialRateExternalIds, 
        curveNodesExternalIds);
  }

  private Set<ExternalId> getInitialRateExternalIds(Set<Currency> currencies) {
    ConventionBundleMaster cbm = new InMemoryConventionBundleMaster();
    DefaultConventionBundleSource cbs = new DefaultConventionBundleSource(cbm);
    Set<ExternalId> externalInitialRateId = newHashSet();
    for (Currency currency : currencies) {
      for (String swapType : new String[]{"SWAP", "3M_SWAP", "6M_SWAP"}) {
        String product = currency.getCode() + "_" + swapType;
        ConventionBundle convention = cbs.getConventionBundle(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, product));
        if (convention != null) {
          ExternalId initialRate = convention.getSwapFloatingLegInitialRate();
          ConventionBundle realIdConvention = cbs.getConventionBundle(initialRate);
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
   * @return list of dates
   */
  private List<LocalDate> buildDates() {
    Clock clock = Clock.systemDefaultZone();
    List<LocalDate> dates = new ArrayList<LocalDate>();
    LocalDate twoYearsAgo = clock.today().minusYears(2);
    LocalDate twoYearsTime = clock.today().plusYears(2);
    for (LocalDate next = twoYearsAgo; next.isBefore(twoYearsTime); next = next.plusMonths(3)) {
      dates.add(next);
    }
    return dates;
  }

  /**
   * Get all the curve definition config object names specified by glob expression.
   * @param configMaster
   * @param nameExpr glob type expression - e.g. blah*
   * @return list of names of config objects matching glob expression
   */
  private List<YieldCurveDefinition> getCurveDefinitionNames(ConfigMaster configMaster, String nameExpr) {
    List<YieldCurveDefinition> results = new ArrayList<YieldCurveDefinition>();
    ConfigSearchRequest<YieldCurveDefinition> request = new ConfigSearchRequest<YieldCurveDefinition>(YieldCurveDefinition.class);
    request.setName(nameExpr);
    for (ConfigDocument<YieldCurveDefinition> doc : ConfigMasterIterator.iterable(configMaster, request)) {
      results.add(doc.getValue());
    }
    return results;
  }

  /**
   * For a given list of curve names, on a given list of dates, get the superset of all ids required by those curves.
   * @param configSource
   * @param names
   * @param dates
   * @return list of all ids required by curves
   */
  private Set<ExternalId> getCurveRequiredExternalIds(ConfigSource configSource, Collection<String> names, List<LocalDate> dates) {
    Set<ExternalId> externalIds = newHashSet();
    for (String name : names) {
      s_logger.info("Processing curve " + name);
      YieldCurveDefinition curveDefinition = configSource.getByName(YieldCurveDefinition.class, name, null);
      if (curveDefinition != null) {
        InterpolatedYieldCurveSpecificationBuilder builder = new ConfigDBInterpolatedYieldCurveSpecificationBuilder(configSource);
        for (LocalDate date : dates) {
          s_logger.info("Processing curve date " + date);
          try {
            InterpolatedYieldCurveSpecification curveSpec = builder.buildCurve(date, curveDefinition);
            for (FixedIncomeStripWithIdentifier strip : curveSpec.getStrips()) {
              s_logger.info("Processing strip " + strip.getSecurity());
              externalIds.add(strip.getSecurity());
            }
          } catch (Throwable t) {
            s_logger.warn("Unable to build curve " + t.getMessage());
          }
        }
      } else {
        s_logger.warn("No curve definition with '{}' name", name);
      }
    }
    return externalIds;
  }
  
  private void loadHistoricalData(boolean write, String[] dataFields, String dataProvider, Set<ExternalId>... externalIdSets) {
    BloombergHistoricalTimeSeriesLoader loader = new BloombergHistoricalTimeSeriesLoader(
      getToolContext().getHistoricalTimeSeriesMaster(),
      getToolContext().getHistoricalTimeSeriesProvider(),
      new BloombergIdentifierProvider(getToolContext().getBloombergReferenceDataProvider()));

    for (Set<ExternalId> externalIds : externalIdSets) {
      if (externalIds.size() > 0) {
        for (String dataField : dataFields) {
          s_logger.info("Loading time series (field: " + dataField + ", provider: " + dataProvider + ") with external IDs " + externalIds);
          if (write) {
            loader.addTimeSeries(externalIds, dataProvider, dataField, LocalDate.now().minusYears(1), null);
          }
        }
      }
    }
  }

  @Override
  protected Options createOptions(boolean contextProvided) {
    
    Options options = super.createOptions(contextProvided);
    
    Option curveNameOption = new Option(
        CURVE_NAME_OPT, "name", true, "The name of the yield curve definition for which to resolve time series");
    curveNameOption.setRequired(true);
    options.addOption(curveNameOption);
    
    Option writeOption = new Option(
        WRITE_OPT, "write", false, 
        "Actually persists the time series to the database if specified, otherwise pretty-prints without persisting");
    options.addOption(writeOption);
 
    Option verboseOption = new Option(
        VERBOSE_OPT, "verbose", false, 
        "Displays progress messages on the terminal");
    options.addOption(verboseOption);
   
    Option timeSeriesDataProviderOption = new Option(
        TIME_SERIES_DATAPROVIDER_OPT, "provider", true, "The name of the time series data provider");
    options.addOption(timeSeriesDataProviderOption);
    
    Option timeSeriesDataFieldOption = new Option(
        TIME_SERIES_DATAFIELD_OPT, "field", true, "The name(s) of the time series data field(s)");
    options.addOption(timeSeriesDataFieldOption);

    return options;
  }

}
