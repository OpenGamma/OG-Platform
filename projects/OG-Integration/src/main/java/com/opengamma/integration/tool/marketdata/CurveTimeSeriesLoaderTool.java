/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.marketdata;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.LocalDate;

import com.opengamma.bbg.BloombergIdentifierProvider;
import com.opengamma.bbg.loader.hts.BloombergHistoricalTimeSeriesLoader;
import com.opengamma.component.tool.AbstractTool;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.financial.analytics.curve.CurveConstructionConfiguration;
import com.opengamma.financial.analytics.curve.CurveGroupConfiguration;
import com.opengamma.financial.analytics.curve.CurveTypeConfiguration;
import com.opengamma.financial.analytics.curve.DiscountingCurveTypeConfiguration;
import com.opengamma.financial.analytics.curve.IborCurveTypeConfiguration;
import com.opengamma.financial.analytics.curve.InflationCurveTypeConfiguration;
import com.opengamma.financial.analytics.curve.InflationIssuerCurveTypeConfiguration;
import com.opengamma.financial.analytics.curve.IssuerCurveTypeConfiguration;
import com.opengamma.financial.analytics.curve.OvernightCurveTypeConfiguration;
import com.opengamma.financial.security.index.IborIndex;
import com.opengamma.financial.security.index.OvernightIndex;
import com.opengamma.id.ExternalId;
import com.opengamma.integration.tool.IntegrationToolContext;
import com.opengamma.master.config.ConfigDocument;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.master.config.ConfigSearchRequest;
import com.opengamma.master.config.impl.ConfigSearchIterator;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.master.security.SecurityMaster;
import com.opengamma.master.security.SecuritySearchRequest;
import com.opengamma.master.security.SecuritySearchResult;
import com.opengamma.scripts.Scriptable;

/**
 * Tool to load required time series for curve construction (for post 2.1.0 multi-curve framework)
 */
@Scriptable
public final class CurveTimeSeriesLoaderTool extends AbstractTool<IntegrationToolContext> {
  /** Logger. */
  private static Logger s_logger = LoggerFactory.getLogger(CurveTimeSeriesLoaderTool.class);

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
    new CurveTimeSeriesLoaderTool().invokeAndTerminate(args);
  }

  //-------------------------------------------------------------------------

  @Override
  protected void doRun() {
    final ConfigMaster configMaster = getToolContext().getConfigMaster();
    final SecurityMaster securityMaster = getToolContext().getSecurityMaster();
        
    // Find all matching curves
    final List<CurveConstructionConfiguration> curveConstructionConfigs = getCurveConstructionConfigs(configMaster);
    Set<ExternalId> ids = new LinkedHashSet<>();
    for (CurveConstructionConfiguration curveConstructionConfig : curveConstructionConfigs) {
      // search config for ids
      ids.addAll(grepCurveConstructionConfigurationForIds(curveConstructionConfig));
    }
    // add ids for ibor indices currently loaded in sec master
    ids.addAll(getIndexIds(securityMaster, IborIndex.INDEX_TYPE));
    // add ids for overnight indices currently loaded in sec master
    ids.addAll(getIndexIds(securityMaster, OvernightIndex.INDEX_TYPE));

    // Load the required time series
    loadHistoricalData(getCommandLine().hasOption(WRITE_OPT), 
        getCommandLine().getOptionValues(TIME_SERIES_DATAFIELD_OPT) == null ? new String[] {"PX_LAST" } : getCommandLine().getOptionValues(TIME_SERIES_DATAFIELD_OPT),
        getCommandLine().getOptionValue(TIME_SERIES_DATAPROVIDER_OPT, "DEFAULT"), 
        ids);
  }

  /**
   * Get all the curve construction configuration config objects
   * @param configMaster
   * @return list of curve construction configurations
   */
  private List<CurveConstructionConfiguration> getCurveConstructionConfigs(final ConfigMaster configMaster) {
    final List<CurveConstructionConfiguration> results = new ArrayList<CurveConstructionConfiguration>();
    final ConfigSearchRequest<CurveConstructionConfiguration> request = new ConfigSearchRequest<CurveConstructionConfiguration>(CurveConstructionConfiguration.class);
    for (final ConfigDocument doc : ConfigSearchIterator.iterable(configMaster, request)) {
      results.add((CurveConstructionConfiguration) doc.getConfig().getValue());
    }
    return results;
  }
  
  /**
   * go through the curve construction configuration and make a list of all the referenced ids that may need loading.
   * @param curveConstructionConfig
   * @return
   */
  private Set<ExternalId> grepCurveConstructionConfigurationForIds(CurveConstructionConfiguration curveConstructionConfig) {
    Set<ExternalId> externalIds = new HashSet<>();
    for (CurveGroupConfiguration curveGroupConfig : curveConstructionConfig.getCurveGroups()) {
      for (List<? extends CurveTypeConfiguration> curveTypeConfigurations : curveGroupConfig.getTypesForCurves().values()) {
        for (CurveTypeConfiguration curveTypeConfiguration : curveTypeConfigurations) {
          if (curveTypeConfiguration instanceof DiscountingCurveTypeConfiguration) {
            @SuppressWarnings("unused")
            DiscountingCurveTypeConfiguration discountingCurveTypeConfiguration = (DiscountingCurveTypeConfiguration) curveTypeConfiguration;
            // do nothing for now
          } else if (curveTypeConfiguration instanceof IborCurveTypeConfiguration) {
            IborCurveTypeConfiguration iborCurveTypeConfiguration = (IborCurveTypeConfiguration) curveTypeConfiguration;
            ExternalId convention = iborCurveTypeConfiguration.getConvention();
            if (!convention.getScheme().equals("CONVENTION")) {
              externalIds.add(convention);
            }
          } else if (curveTypeConfiguration instanceof InflationCurveTypeConfiguration) {
            InflationCurveTypeConfiguration inflationCurveTypeConfiguration = (InflationCurveTypeConfiguration) curveTypeConfiguration;
            ExternalId priceIndex = inflationCurveTypeConfiguration.getPriceIndex();
            externalIds.add(priceIndex);
          } else if (curveTypeConfiguration instanceof InflationIssuerCurveTypeConfiguration) {
            InflationIssuerCurveTypeConfiguration inflationIssuerCurveTypeConfiguration = (InflationIssuerCurveTypeConfiguration) curveTypeConfiguration;
            ExternalId priceIndex = inflationIssuerCurveTypeConfiguration.getPriceIndex();
            externalIds.add(priceIndex);
          } else if (curveTypeConfiguration instanceof IssuerCurveTypeConfiguration) {
            @SuppressWarnings("unused")
            IssuerCurveTypeConfiguration issuerCurveTypeConfiguration = (IssuerCurveTypeConfiguration) curveTypeConfiguration;
            // do nothing for now
          } else if (curveTypeConfiguration instanceof OvernightCurveTypeConfiguration) {
            OvernightCurveTypeConfiguration overnightCurveTypeConfiguration = (OvernightCurveTypeConfiguration) curveTypeConfiguration;
            ExternalId convention = overnightCurveTypeConfiguration.getConvention();
            if (!convention.getScheme().equals("CONVENTION")) {
              externalIds.add(convention);
            }
          } else {
            s_logger.error("Not handling curve type configuration of class {}, skippings", curveTypeConfiguration.getClass());
          }
        }
      }
    }
    return externalIds;
  }
  
  /**
   * Search the security master for indices of the specified type and return the bloomberg tickers for each.
   * @param secMaster the security master
   * @param indexType the type of index (e.g. INDEX_TYPE on each Index subclass).
   * @return bloomberg tickers for each index of given type
   */
  private Collection<ExternalId> getIndexIds(SecurityMaster secMaster, String indexType) {
    Set<ExternalId> externalIds = new LinkedHashSet<>();
    SecuritySearchRequest searchRequest = new SecuritySearchRequest();
    searchRequest.setSecurityType(indexType);
    SecuritySearchResult searchResult = secMaster.search(searchRequest);
    for (ManageableSecurity security : searchResult.getSecurities()) {
      ExternalId externalId = security.getExternalIdBundle().getExternalId(ExternalSchemes.BLOOMBERG_TICKER);
      if (externalId != null) {
        externalIds.add(externalId);
      } else {
        s_logger.warn("No BLOOMBERG_TICKER for {}, skipping", security);
      }
    }
    return externalIds;
  }

  /**
   * Load the historical data
   * @param write whether to actually add the data
   * @param dataFields a list of fields to load
   * @param dataProvider a data provider
   * @param externalIdSets a varargs list of sets of external ids
   */
  @SafeVarargs
  private final void loadHistoricalData(final boolean write, final String[] dataFields, final String dataProvider, final Set<ExternalId>... externalIdSets) { //CSIGNORE
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
