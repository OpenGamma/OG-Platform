/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bloombergexample.loader;


import static com.google.common.collect.Sets.newHashSet;
import static com.opengamma.util.functional.Functional.map;

import java.util.*;

import javax.time.calendar.Clock;
import javax.time.calendar.LocalDate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.collections.Lists;

import com.opengamma.bloombergexample.tool.AbstractExampleTool;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.core.security.SecurityUtils;
import com.opengamma.financial.analytics.ircurve.*;
import com.opengamma.financial.convention.ConventionBundle;
import com.opengamma.financial.convention.ConventionBundleMaster;
import com.opengamma.financial.convention.DefaultConventionBundleSource;
import com.opengamma.financial.convention.InMemoryConventionBundleMaster;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.master.config.ConfigDocument;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.master.config.ConfigSearchRequest;
import com.opengamma.master.config.ConfigSearchResult;
import com.opengamma.util.functional.Function1;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.Pair;


/**
 * Example code to load a very simple swap portfolio.
 * <p>
 * This code is kept deliberately as simple as possible.
 * There are no checks for the securities or portfolios already existing, so if you run it
 * more than once you will get multiple copies portfolios and securities with the same names.
 * It is designed to run against the HSQLDB example database.
 */
public class CurveNodeHistoricalDataLoader extends AbstractExampleTool {
  /**
   * Logger.
   */
  private static Logger s_logger = LoggerFactory.getLogger(CurveNodeHistoricalDataLoader.class);

  private Set<ExternalId> _curveNodesExternalIds;

  private Set<ExternalId> _initialRateExternalIds;
  
  private Set<ExternalIdBundle> _futuresExternalIds;

  public Set<ExternalId> getCurveNodesExternalIds() {
    return _curveNodesExternalIds;
  }

  public Set<ExternalId> getInitialRateExternalIds() {
    return _initialRateExternalIds;
  }

  public Set<ExternalIdBundle> getFuturesExternalIds() {
    return _futuresExternalIds;
  }

  //-------------------------------------------------------------------------

  /**
   * Main method to run the tool.
   * No arguments are needed.
   *
   * @param args  the arguments, unused
   */
  public static void main(String[] args) {  // CSIGNORE
    new CurveNodeHistoricalDataLoader().initAndRun(args);
    System.exit(0);
  }

  //-------------------------------------------------------------------------
  @Override
  protected void doRun() {
    ConfigSource configSource = getToolContext().getConfigSource();
    ConfigMaster configMaster = getToolContext().getConfigMaster();
    YieldCurveConfigPopulator.populateCurveConfigMaster(configMaster);
    List<YieldCurveDefinition> curves = getForwardAndFundingCurves(configMaster);

    Set<Currency> currencies = newHashSet();

    for (YieldCurveDefinition curve : curves) {
      currencies.add(curve.getCurrency());
    }

    _initialRateExternalIds = getInitialRateExternalIds(currencies);

    List<LocalDate> dates = buildDates();

    Set<String> curveNames = map(new HashSet<String>(), curves, new Function1<YieldCurveDefinition, String>() {
      @Override
      public String execute(YieldCurveDefinition yieldCurveDefinition) {
        return yieldCurveDefinition.getName()+"_"+yieldCurveDefinition.getCurrency().getCode();
      }
    });
    _curveNodesExternalIds = getCurves(configSource, curveNames, dates);
    
    _futuresExternalIds = getFutures(configSource, curveNames, dates);

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
          externalInitialRateId.add(realIdConvention.getIdentifiers().getExternalId(SecurityUtils.BLOOMBERG_TICKER));
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
   * Get all the curves starting with FUNDING or FORWARD
   * @param configMaster
   * @return list of yield curve definition config object names
   */
  private List<YieldCurveDefinition> getForwardAndFundingCurves(ConfigMaster configMaster) {
    List<YieldCurveDefinition> forwardCurves = getCurveDefinitionNames(configMaster, "FORWARD*");
    List<YieldCurveDefinition> fundingCurves = getCurveDefinitionNames(configMaster, "FUNDING*");
    List<YieldCurveDefinition> allCurves = Lists.newArrayList();
    allCurves.addAll(forwardCurves);
    allCurves.addAll(fundingCurves);
    return allCurves;
  }

  /**
   * Get all the curve definition config object names specified by glob expression.
   * @param configMaster
   * @param nameExpr glob type expression - e.g. blah*
   * @return list of names of config objects matching glob expression
   */
  private List<YieldCurveDefinition> getCurveDefinitionNames(ConfigMaster configMaster, String nameExpr) {
    List<YieldCurveDefinition> results = new ArrayList<YieldCurveDefinition>();
    ConfigSearchRequest<YieldCurveDefinition> searchReq = new ConfigSearchRequest<YieldCurveDefinition>(YieldCurveDefinition.class);
    searchReq.setName(nameExpr);
    ConfigSearchResult<YieldCurveDefinition> result = configMaster.search(searchReq);
    for (ConfigDocument<YieldCurveDefinition> document : result.getDocuments()) {
      results.add(document.getValue());
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
  private Set<ExternalId> getCurves(ConfigSource configSource, Collection<String> names, List<LocalDate> dates) {
    Set<ExternalId> externalIds = newHashSet();
    Set<ExternalId> futuresIds = newHashSet();
    for (String name : names) {
      s_logger.info("Processing curve " + name);
      YieldCurveDefinition curveDefinition = configSource.getByName(YieldCurveDefinition.class, name, null);
      if (curveDefinition != null) {
        InterpolatedYieldCurveSpecificationBuilder builder = new ConfigDBInterpolatedYieldCurveSpecificationBuilder(configSource);
        for (LocalDate date : dates) {
          s_logger.info("Processing curve date " + date);
          InterpolatedYieldCurveSpecification curveSpec = builder.buildCurve(date, curveDefinition);
          for (FixedIncomeStripWithIdentifier strip : curveSpec.getStrips()) {
            s_logger.info("Processing strip " + strip.getSecurity());
            externalIds.add(strip.getSecurity());
          }
        }
      } else {
        s_logger.warn("No curve definition with '{}' name", name);
      }
    }
    return externalIds;
  }
  
  /**
   * For a given list of curve names, on a given list of dates, get the superset of all ids which are futures
   * @param configSource
   * @param names
   * @param dates
   * @return list of all ids required by curves
   */
  private Set<ExternalIdBundle> getFutures(ConfigSource configSource, Collection<String> names, List<LocalDate> dates) {
    Set<ExternalIdBundle> externalIds = newHashSet();
    for (String name : names) {
      s_logger.info("Processing curve " + name);
      YieldCurveDefinition curveDefinition = configSource.getByName(YieldCurveDefinition.class, name, null);
      if (curveDefinition != null) {
        InterpolatedYieldCurveSpecificationBuilder builder = new ConfigDBInterpolatedYieldCurveSpecificationBuilder(configSource);
        for (LocalDate date : dates) {
          s_logger.info("Processing curve date " + date);
          InterpolatedYieldCurveSpecification curveSpec = builder.buildCurve(date, curveDefinition);
          for (FixedIncomeStripWithIdentifier strip : curveSpec.getStrips()) {
            s_logger.info("Processing strip " + strip.getSecurity());
            if(strip.getStrip().getInstrumentType().equals(StripInstrumentType.FUTURE)){
              externalIds.add(ExternalIdBundle.of(strip.getSecurity()));
            }
          }
        }
      } else {
        s_logger.warn("No curve definition with '{}' name", name);
      }
    }
    return externalIds;
  }

}
