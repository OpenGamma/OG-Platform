/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.generator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.core.position.PortfolioNode;
import com.opengamma.financial.security.equity.EquitySecurity;
import com.opengamma.financial.security.option.EquityOptionSecurity;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.master.security.SecuritySearchRequest;
import com.opengamma.master.security.SecuritySearchResult;

/**
 * Utility for constructing a random equity option portfolio for options already in the portfolio.
 */
public abstract class AbstractEquityOptionPortfolioGeneratorTool extends AbstractPortfolioGeneratorTool {

  private static final Logger s_logger = LoggerFactory.getLogger(AbstractEquityOptionPortfolioGeneratorTool.class);

  private static final int OPTION_COUNT = 16;

  protected abstract AbstractEquityOptionSecurityGenerator createEquityOptionSecurityGenerator(EquitySecurity underlying);

  @Override
  public PortfolioNodeGenerator createPortfolioNodeGenerator(final int size) {
    return new PortfolioNodeGenerator() {
      @Override
      public PortfolioNode createPortfolioNode() {
        final SecuritySearchRequest request = new SecuritySearchRequest();
        request.setFullDetail(true);
        request.setSecurityType("EQUITY");
        final SecuritySearchResult result = getToolContext().getSecurityMaster().search(request);
        final TreePortfolioNodeGenerator node = new TreePortfolioNodeGenerator(new StaticNameGenerator("Equity Option"));
        final List<ManageableSecurity> securities = new ArrayList<ManageableSecurity>(result.getSecurities());
        Collections.shuffle(securities);
        int n = size / OPTION_COUNT;
        for (ManageableSecurity security : securities) {
          if (!(security instanceof EquitySecurity)) {
            continue;
          }
          final EquitySecurity equitySecurity = (EquitySecurity) security;
          final AbstractEquityOptionSecurityGenerator options = createEquityOptionSecurityGenerator(equitySecurity);
          if (getToolContext().getHistoricalTimeSeriesSource().getHistoricalTimeSeries(options.getPriceSeriesDataField(), security.getExternalIdBundle(), null) != null) {
            s_logger.debug("Found price time series for {}", equitySecurity);
            configure(options);
            final PositionGenerator positions = new SimplePositionGenerator<EquityOptionSecurity>(new StaticQuantityGenerator(100), options, getSecurityPersister());
            node.addChildNode(new LeafPortfolioNodeGenerator(new StaticNameGenerator("Options on " + equitySecurity.getName()), positions, OPTION_COUNT));
            if ((--n) <= 0) {
              break;
            }
          } else {
            s_logger.debug("No price time series for {}", equitySecurity);
          }
        }
        return node.createPortfolioNode();
      }
    };
  }

}
