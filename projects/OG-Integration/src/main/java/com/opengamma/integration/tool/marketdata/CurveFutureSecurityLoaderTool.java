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
import java.util.Map;
import java.util.Set;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.Clock;
import org.threeten.bp.LocalDate;

import com.opengamma.bbg.loader.BloombergBulkSecurityLoader;
import com.opengamma.component.tool.AbstractTool;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.financial.analytics.ircurve.ConfigDBInterpolatedYieldCurveSpecificationBuilder;
import com.opengamma.financial.analytics.ircurve.FixedIncomeStripWithIdentifier;
import com.opengamma.financial.analytics.ircurve.InterpolatedYieldCurveSpecification;
import com.opengamma.financial.analytics.ircurve.InterpolatedYieldCurveSpecificationBuilder;
import com.opengamma.financial.analytics.ircurve.StripInstrumentType;
import com.opengamma.financial.analytics.ircurve.YieldCurveDefinition;
import com.opengamma.financial.security.future.InterestRateFutureSecurity;
import com.opengamma.financial.timeseries.exchange.DefaultExchangeDataProvider;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.VersionCorrection;
import com.opengamma.integration.tool.IntegrationToolContext;
import com.opengamma.master.config.ConfigDocument;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.master.config.ConfigSearchRequest;
import com.opengamma.master.config.impl.ConfigSearchIterator;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.master.security.SecurityMaster;
import com.opengamma.master.security.SecurityMasterUtils;
import com.opengamma.scripts.Scriptable;

/**
 */
@Scriptable
public class CurveFutureSecurityLoaderTool extends AbstractTool<IntegrationToolContext> {

  /** Logger. */
  private static Logger s_logger = LoggerFactory.getLogger(CurveFutureSecurityLoaderTool.class);

  /** Portfolio name option flag */
  private static final String CURVE_NAME_OPT = "n";
  /** Write option flag */
  private static final String WRITE_OPT = "w";
  /** Verbose option flag */
  private static final String VERBOSE_OPT = "v";

  //-------------------------------------------------------------------------
  /**
   * Main method to run the tool.
   * 
   * @param args  the standard tool arguments, not null
   */
  public static void main(final String[] args) { // CSIGNORE
    new CurveFutureSecurityLoaderTool().invokeAndTerminate(args);
  }

  //-------------------------------------------------------------------------
  @Override
  protected void doRun() {
    final ConfigSource configSource = getToolContext().getConfigSource();
    final ConfigMaster configMaster = getToolContext().getConfigMaster();

    // Find all matching curves
    final List<YieldCurveDefinition> curves = getCurveDefinitionNames(configMaster, getCommandLine().getOptionValue(CURVE_NAME_OPT));

    // build list of curve dates so that we pre-load contracts out several years where possible.
    final List<LocalDate> dates = buildDates();

    // build list of futures ids
    final Set<ExternalId> curveNodesExternalIds = getCurveFutureExternalIds(configSource, curves, dates);

    // filter out ids that are already loaded into the sec master
    final Set<ExternalId> unloadedIds = filterPresentIds(curveNodesExternalIds);

    // Load the required future securities
    loadSecuritylData(getCommandLine().hasOption(WRITE_OPT), unloadedIds);
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
   * For a given list of curve definitions, on a given list of dates, get all ids on futures required by those curves.
   * 
   * @param configSource configuration source
   * @param curveDefs curve definitions
   * @param dates list of dates to construct the curve on
   * @return list of all futures ids required by curves
   */
  private Set<ExternalId> getCurveFutureExternalIds(final ConfigSource configSource, final Collection<YieldCurveDefinition> curveDefs, final List<LocalDate> dates) {
    final Set<ExternalId> externalIds = newHashSet();
    for (final YieldCurveDefinition curveDefinition : curveDefs) {
      if (curveDefinition != null) {
        InterpolatedYieldCurveSpecificationBuilder builder = new ConfigDBInterpolatedYieldCurveSpecificationBuilder(configSource);
        for (LocalDate date : dates) {
          if (isVerbose()) {
            System.out.println("Processing curve " + curveDefinition.getName() + " for date " + date);
          }
          try {
            final InterpolatedYieldCurveSpecification curveSpec = builder.buildCurve(date, curveDefinition, VersionCorrection.LATEST);
            for (final FixedIncomeStripWithIdentifier strip : curveSpec.getStrips()) {
              s_logger.info("Processing strip " + strip.getSecurity());
              if (strip.getInstrumentType() == StripInstrumentType.FUTURE) {
                externalIds.add(strip.getSecurity());
              }
            }
          } catch (final Throwable t) {
            s_logger.warn("Unable to build curve " + t.getMessage());
          }
        }
      } else {
        s_logger.warn("Null curve definition, skipping.");
      }
    }
    return externalIds;
  }

  private void loadSecuritylData(final boolean write, final Set<ExternalId> externalIds) {
    BloombergBulkSecurityLoader bulkSecurityLoader = new BloombergBulkSecurityLoader(getToolContext().getBloombergReferenceDataProvider(), DefaultExchangeDataProvider.getInstance());
    SecurityMaster secMaster = getToolContext().getSecurityMaster();
    Set<ExternalIdBundle> externalIdBundles = new HashSet<>();
    for (ExternalId externalId : externalIds) {
      externalIdBundles.add(externalId.toBundle());
    }
    Map<ExternalIdBundle, ManageableSecurity> loadedSecurities = bulkSecurityLoader.loadSecurity(externalIdBundles);
    for (Map.Entry<ExternalIdBundle, ManageableSecurity> entry : loadedSecurities.entrySet()) {
      SecurityMasterUtils.addOrUpdateSecurity(secMaster, entry.getValue());
      if (isVerbose()) {
        System.out.println("Loading security " + entry.getKey().getExternalId(ExternalSchemes.BLOOMBERG_TICKER));
      }
    }
  }

  private boolean isVerbose() {
    return getCommandLine().hasOption(VERBOSE_OPT);
  }

  private Set<ExternalId> filterPresentIds(Set<ExternalId> externalIds) {
    Set<ExternalId> filtered = new HashSet<>();
    SecuritySource securitySource = getToolContext().getSecuritySource();
    for (ExternalId externalId : externalIds) {
      Security security = securitySource.getSingle(externalId.toBundle());
      if (security instanceof InterestRateFutureSecurity) {
        continue;
      }
      filtered.add(externalId);
    }
    if (isVerbose()) {
      System.out.println("Of " + externalIds.size() + " ids, " + filtered.size() + " were not present in the security master");
    }
    return filtered;
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

    return options;
  }

}
