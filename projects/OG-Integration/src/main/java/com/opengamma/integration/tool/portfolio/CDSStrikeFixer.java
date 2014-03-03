/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.portfolio;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;
import static java.lang.String.format;

import java.security.SecureRandom;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.threeten.bp.LocalDate;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.bbg.BloombergIdentifierProvider;
import com.opengamma.bbg.loader.hts.BloombergHistoricalTimeSeriesLoader;
import com.opengamma.component.tool.AbstractTool;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeries;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSource;
import com.opengamma.core.marketdatasnapshot.impl.ManageableMarketDataSnapshot;
import com.opengamma.core.security.Security;
import com.opengamma.financial.analytics.curve.CurveDefinition;
import com.opengamma.financial.analytics.curve.CurveNodeIdMapper;
import com.opengamma.financial.security.cds.CreditDefaultSwapSecurity;
import com.opengamma.financial.security.option.CreditDefaultSwapOptionSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.integration.tool.IntegrationToolContext;
import com.opengamma.master.config.ConfigSearchRequest;
import com.opengamma.master.config.impl.MasterConfigSource;
import com.opengamma.master.historicaltimeseries.impl.HistoricalTimeSeriesRatingFieldNames;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotSearchRequest;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotSearchResult;
import com.opengamma.master.portfolio.ManageablePortfolio;
import com.opengamma.master.portfolio.PortfolioSearchRequest;
import com.opengamma.master.position.ManageablePosition;
import com.opengamma.master.position.PositionDocument;
import com.opengamma.master.security.ManageableSecurityLink;
import com.opengamma.master.security.SecurityDocument;
import com.opengamma.master.security.SecurityMaster;
import com.opengamma.master.security.SecuritySearchRequest;
import com.opengamma.master.security.SecuritySearchResult;
import com.opengamma.scripts.Scriptable;
import com.opengamma.util.time.Tenor;

/**
 * CDS strike fixer.
 */
@Scriptable
public class CDSStrikeFixer extends AbstractTool<IntegrationToolContext> {

  /**
   * The name of the portfolio.
   */
  public static final String PORTFOLIO_NAME = "MultiCurrency Swap Portfolio";

  //-------------------------------------------------------------------------
  /**
   * Main method to run the tool.
   * 
   * @param args  the standard tool arguments, not null
   */
  public static void main(final String[] args) {  // CSIGNORE
    new CDSStrikeFixer().invokeAndTerminate(args);
  }

  //-------------------------------------------------------------------------
  @Override
  protected void doRun() {
    fixCDSOptionsStrike();
  }

  private void fixCDSOptionsStrike() {

    final MasterConfigSource configSource = new MasterConfigSource(getToolContext().getConfigMaster());
    final SecurityMaster securityMaster = getToolContext().getSecurityMaster();

    String snapshotName = "Sameday spread";
    MarketDataSnapshotSearchRequest marketDataSnapshotSearchRequest = new MarketDataSnapshotSearchRequest();
    marketDataSnapshotSearchRequest.setName(snapshotName);
    MarketDataSnapshotSearchResult marketDataSnapshotSearchResult = getToolContext().getMarketDataSnapshotMaster().search(marketDataSnapshotSearchRequest);
    ManageableMarketDataSnapshot snapshot = marketDataSnapshotSearchResult.getFirstSnapshot();

    PortfolioSearchRequest portfolioSearchRequest = new PortfolioSearchRequest();
    portfolioSearchRequest.setName("CDSOpts");
    //portfolioSearchRequest.setName("Standard CDS Portfolio");
    List<ManageablePortfolio> portfolios = getToolContext().getPortfolioMaster().search(portfolioSearchRequest).getPortfolios();
    for (ManageablePortfolio portfolio : portfolios) {

      List<ObjectId> positionIds = portfolio.getRootNode().getPositionIds();
      List<UniqueId> positionUids = newArrayList();
      for (ObjectId positionId : positionIds) {
        positionUids.add(positionId.atLatestVersion());
      }
      Map<UniqueId, PositionDocument> positions = getToolContext().getPositionMaster().get(positionUids);

      // Ticker, RedCode, Currency, Term, Seniority, RestructuringClause
      final List<ExternalId> cdsArgs = newArrayList();


      final SecureRandom random = new SecureRandom();
      for (PositionDocument positionDocument : positions.values()) {
        ManageablePosition position = positionDocument.getValue();

        ManageableSecurityLink link = position.getSecurityLink();
        SecuritySearchRequest ssr = new SecuritySearchRequest();
        ssr.addExternalIds(link.getExternalIds());
        SecuritySearchResult securitySearchResult = getToolContext().getSecurityMaster().search(ssr);
        Security security = securitySearchResult.getFirstSecurity();
        //Security security = position.getSecurity();
        if (security != null && security instanceof CreditDefaultSwapOptionSecurity) {
          CreditDefaultSwapOptionSecurity cdsOption = (CreditDefaultSwapOptionSecurity) security;
          CreditDefaultSwapSecurity cds = (CreditDefaultSwapSecurity) this.getToolContext().getSecuritySource().getSingle(
              cdsOption.getUnderlyingId().toBundle());

          String curveDefinitionID = "SAMEDAY_" + cds.getReferenceEntity().getValue() + "_" + cds.getNotional().getCurrency() + "_" +
              cds.getDebtSeniority().toString() + "_" + cds.getRestructuringClause();

          ConfigSearchRequest<CurveDefinition> curveDefinitionConfigSearchRequest = new ConfigSearchRequest<CurveDefinition>(CurveDefinition.class);
          curveDefinitionConfigSearchRequest.setName(curveDefinitionID);
          CurveDefinition curveDefinition = getToolContext().getConfigMaster().search(
              curveDefinitionConfigSearchRequest).getFirstValue().getValue();
        /*final CurveDefinition curveDefinition = configSource.getSingle(CurveDefinition.class,
                                                                       curveName,
                                                                       VersionCorrection.LATEST);

        if (curveDefinition == null) {
          throw new OpenGammaRuntimeException("No curve definition for " + curveName);
        }

        //Map<Tenor, CurveNode> curveNodesByTenors = new HashMap<Tenor, CurveNode>();
        //for (CurveNode curveNode : curveDefinition.getNodes()) {
        //  curveNodesByTenors.put(curveNode.getResolvedMaturity(), curveNode);
        //}
        Map<Tenor, CurveNode> curveNodesByTenors = functional(curveDefinition.getNodes()).groupBy(new Function1<CurveNode, Tenor>() {
          @Override
          public Tenor execute(CurveNode curveNode) {
            return curveNode.getResolvedMaturity();
          }
        });*/

          ZonedDateTime start = cds.getStartDate();
          ZonedDateTime maturity = cds.getMaturityDate();
          Period period = Period.between(start.toLocalDate(), maturity.toLocalDate());
          Tenor tenor = Tenor.of(period);


          final CurveNodeIdMapper curveNodeIdMapper = configSource.getSingle(CurveNodeIdMapper.class,
                                                                             curveDefinitionID,
                                                                             VersionCorrection.LATEST);


          try {
            tenor = Tenor.of(Period.ofYears(5));
            ExternalId timeSeriesId = curveNodeIdMapper.getCreditSpreadNodeId(null /* magic null - ask Elaine */, tenor);

            
            Object strikeObj = snapshot.getGlobalValues().getValue(timeSeriesId, "PX_LAST").getMarketValue();
            if ((strikeObj instanceof Double)) {
              cdsOption.setStrike((Double) strikeObj);
            } else {
              throw new OpenGammaRuntimeException(format("Double expected for strike but '%s' found instead.", String.valueOf(strikeObj)));
            }
            //else throw?
            //snapshot.getGlobalValues().getValue()
            //cdsArgs.add(timeSeriesId);
            //loadTimeSeries(newArrayList(timeSeriesId));
            //LocalDate tradeDate = functional(position.getTrades()).first().getTradeDate();
            //Double strike = getFixedRate(random, tradeDate, timeSeriesId);
            securityMaster.update(new SecurityDocument(cdsOption));
          } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
          }
        }
      }
    }

  }

  private void loadTimeSeries(final List<ExternalId> curveNodeIDs) {
    final Set<ExternalId> externalIds = newHashSet(curveNodeIDs);
    final BloombergHistoricalTimeSeriesLoader loader = new BloombergHistoricalTimeSeriesLoader(
        getToolContext().getHistoricalTimeSeriesMaster(),
        getToolContext().getHistoricalTimeSeriesProvider(),
        new BloombergIdentifierProvider(getToolContext().getBloombergReferenceDataProvider()));
    loader.loadTimeSeries(externalIds, "UNKNOWN", "PX_LAST", LocalDate.now().minusYears(1), LocalDate.now());
  }

  private Double getFixedRate(final SecureRandom random,
                              final LocalDate tradeDate,
                              final ExternalId curveId) {
    final HistoricalTimeSeriesSource historicalSource = getToolContext().getHistoricalTimeSeriesSource();
    final MasterConfigSource configSource = new MasterConfigSource(getToolContext().getConfigMaster());

    final HistoricalTimeSeries fixedRateSeries = historicalSource.getHistoricalTimeSeries("PX_LAST",
                                                                                          curveId.toBundle(),
                                                                                          HistoricalTimeSeriesRatingFieldNames.DEFAULT_CONFIG_NAME,
                                                                                          tradeDate,
                                                                                          true,
                                                                                          LocalDate.now(),
                                                                                          true);
    if (fixedRateSeries == null) {
      throw new OpenGammaRuntimeException("can't find time series for " + curveId + " on " + tradeDate);
    }
    return fixedRateSeries.getTimeSeries().getLatestValue();
  }

}
