/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.production.tool.portfolio;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.time.InstantProvider;
import javax.time.calendar.OffsetDateTime;
import javax.time.calendar.Period;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;

import com.opengamma.core.config.ConfigSource;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.financial.aggregation.AggregationFunction;
import com.opengamma.financial.aggregation.AssetClassAggregationFunction;
import com.opengamma.financial.aggregation.CurrencyAggregationFunction;
import com.opengamma.financial.aggregation.DetailedAssetClassAggregationFunction;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdSearch;
import com.opengamma.id.ExternalScheme;
import com.opengamma.id.UniqueId;
import com.opengamma.master.position.PositionDocument;
import com.opengamma.master.position.PositionSearchRequest;
import com.opengamma.master.position.PositionSearchResult;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.master.security.SecuritySearchRequest;
import com.opengamma.master.security.SecuritySearchResult;
import com.opengamma.masterdb.portfolio.DbPortfolioMaster;
import com.opengamma.masterdb.position.DbPositionMaster;
import com.opengamma.masterdb.security.DbSecurityMaster;
import com.opengamma.util.PlatformConfigUtils;
import com.opengamma.util.db.DbDateUtils;
import com.opengamma.util.tuple.Pair;

/**
 * Tool to remove positions with expired bonds. (Unfinished)
 * <p>
 * This will not remove the bonds themselves, or the references from portfolios.
 */
public class PortfolioAggregationTool {
  private static final Logger s_logger = LoggerFactory.getLogger(PortfolioAggregationTool.class);
  /** The scheme to use. */
  private static final String SCHEME = "BLOOMBERG_BUID";
  /** The period to remove. */
  private static final Period IN_FUTURE = Period.ofMonths(3);
  /** Whether to actually remove the positions. */
  private static final boolean REMOVE = false;

  private static final Map<String, AggregationFunction<?>> s_aggregationFunctions = new HashMap<String, AggregationFunction<?>>();
  static {
    s_aggregationFunctions.put("AssetClass", new AssetClassAggregationFunction());
    s_aggregationFunctions.put("Currency", new CurrencyAggregationFunction());
    s_aggregationFunctions.put("DetailedAssetClass", new DetailedAssetClassAggregationFunction());
  }
  
  private static Pair<String, List<AggregationFunction<?>>> parseCommandLineArgs(String[] args) {
    Options options = new Options();
    @SuppressWarnings("static-access")
    Option baseViewOption = OptionBuilder.withLongOpt("base-view")
                                         .hasArg()
                                         .isRequired()
                                         .withDescription("The view name on which to base aggregated resulting views")
                                         .create();
    options.addOption(baseViewOption);
    @SuppressWarnings("static-access")
    Option aggregationTypesOption = OptionBuilder.withLongOpt("aggregation-types")
                                                 .hasArgs()
                                                 .isRequired()
                                                 .withValueSeparator(',')
                                                 .withDescription("The (comma, no space seperated) names of the aggregation" +
                                                                  " styles to use: e.g AssetClass, Currency, DetailtedAssetClass")
                                                 .create();
    options.addOption(aggregationTypesOption);
    CommandLineParser parser = new GnuParser();
    try {
      CommandLine commandLine = parser.parse(options, args);
      String baseViewName = commandLine.getOptionValue("base-view");
      String[] aggregationTypes = commandLine.getOptionValues("aggregation-types");
      List<AggregationFunction<?>> aggregationFunctions = new ArrayList<AggregationFunction<?>>();
      for (String aggregationType : aggregationTypes) {
        if (s_aggregationFunctions.containsKey(aggregationType)) {
          aggregationFunctions.add(s_aggregationFunctions.get(aggregationType));
        } else {
          s_logger.error("Can't find an aggregation function called:" + aggregationType + ", skipping...");
        }
      }
      return Pair.of(baseViewName, aggregationFunctions);
    } catch (ParseException e) {
      s_logger.error("Error parsing command line arguments " + e.getMessage());
      System.exit(1);
      return null;
    }
  }
  /**
   * Runs the tool.
   * 
   * @param args  empty arguments
   */
  public static void main(String[] args) {  // CSIGNORE
    try {
      Pair<String, List<AggregationFunction<?>>> pair = parseCommandLineArgs(args);
      String baseViewName = pair.getFirst();
      List<AggregationFunction<?>> aggFunctions = pair.getSecond();
      LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
      JoranConfigurator configurator = new JoranConfigurator();
      configurator.setContext(lc);
      lc.reset(); 
      configurator.doConfigure("src/com/opengamma/production/server/logback.xml");
      
      PlatformConfigUtils.configureSystemProperties();
      System.out.println("Starting connections");
      AbstractApplicationContext context = new ClassPathXmlApplicationContext("demoIntegrationMasters.xml");
      context.start();
      
      try {
        DbSecurityMaster secMaster = context.getBean("dbSecurityMaster", DbSecurityMaster.class);
        DbPositionMaster posMaster = context.getBean("dbPositionMaster", DbPositionMaster.class);
        DbPortfolioMaster portfolioMaster = context.getBean("dbPortfolioMaster", DbPortfolioMaster.class);
        ConfigSource configSource = context.getBean("sharedConfigSource", ConfigSource.class);
        ViewDefinition baseViewDefinition = configSource.getLatestByName(ViewDefinition.class, baseViewName);
        List<UniqueId> secIds = findSecurityIds(secMaster);
        List<ExternalId> secKeys = findSecurityKeys(secMaster, secIds);
        removePositions(posMaster, secKeys);
      } finally {
        context.close();
      }
      System.out.println("Finished");
      
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    System.exit(0);
  }

  private static List<UniqueId> findSecurityIds(DbSecurityMaster secMaster) {
    InstantProvider instant = OffsetDateTime.now().plus(IN_FUTURE);
    String selectExpiredBonds = "SELECT security_id FROM sec_bond WHERE maturity_date < ?";
    String selectSecurityUids = "SELECT id, oid FROM sec_security WHERE id IN (" + selectExpiredBonds + ")";
    final SimpleJdbcTemplate template = secMaster.getDbConnector().getJdbcTemplate();
    List<Map<String, Object>> results = template.queryForList(selectSecurityUids, DbDateUtils.toSqlTimestamp(instant));
    List<UniqueId> secIds = new ArrayList<UniqueId>();
    for (Map<String, Object> map : results) {
      UniqueId secId = secMaster.createUniqueId((Long) map.get("OID"), (Long) map.get("ID"));
      secIds.add(secId);
      System.out.println("Security id: " + secId);
    }
    return secIds;
  }

  private static List<ExternalId> findSecurityKeys(DbSecurityMaster secMaster, List<UniqueId> secIds) {
    SecuritySearchRequest request = new SecuritySearchRequest();
    request.setObjectIds(secIds);
    SecuritySearchResult result = secMaster.search(request);
    List<ExternalId> secKeys = new ArrayList<ExternalId>();
    for (ManageableSecurity sec : result.getSecurities()) {
      String value = sec.getExternalIdBundle().getExternalId(ExternalScheme.of(SCHEME)).getValue();
      if (value != null) {
        ExternalId secKey = ExternalId.of(ExternalScheme.of(SCHEME), value);
        secKeys.add(secKey);
        System.out.println("Security key: " + secKey);
      }
    }
    return secKeys;
  }

  private static void removePositions(DbPositionMaster posMaster, List<ExternalId> secKeys) {
    PositionSearchRequest request = new PositionSearchRequest();
    request.setSecurityIdSearch(new ExternalIdSearch(secKeys));
    PositionSearchResult result = posMaster.search(request);
    for (PositionDocument posDoc : result.getDocuments()) {
      System.out.println("Position to remove: " + posDoc);
      if (REMOVE) {
        posMaster.remove(posDoc.getUniqueId());
      }
    }
  }

}
