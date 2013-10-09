/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.portfolio;

import java.util.List;

import com.google.common.collect.Lists;
import com.opengamma.DataNotFoundException;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.portfolio.ManageablePortfolio;
import com.opengamma.master.portfolio.ManageablePortfolioNode;
import com.opengamma.master.portfolio.PortfolioMaster;
import com.opengamma.master.position.ManageablePosition;
import com.opengamma.master.position.PositionMaster;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.master.security.SecurityMaster;
import com.opengamma.master.security.SecuritySearchRequest;
import com.opengamma.master.security.SecuritySearchResult;
import com.opengamma.master.security.impl.MasterSecuritySource;
import com.opengamma.util.ArgumentChecker;

/**
 * Helper class that does a depth first search over a portfolio tree calling a {@link Function} for each
 * position it encounters and collecting the results. It takes care of the boilerplate of looking up positions,
 * securities and underlying securities in the appropriate masters when traversing a portfolio structure. Null
 * results are discarded.
 */
public class PositionMapper {

  private final PositionMaster _positionMaster;
  private final SecuritySource _securitySource;
  private final VersionCorrection _versionCorrection;
  private final PortfolioMaster _portfolioMaster;
  private final SecurityMaster _securityMaster;

  /**
   * @param portfolioMaster For looking up the portfolio
   * @param positionMaster For looking up positions
   * @param securityMaster For looking up securities
   * @param versionCorrection Version correction used when querying the masters
   */
  public PositionMapper(PortfolioMaster portfolioMaster,
                        PositionMaster positionMaster,
                        SecurityMaster securityMaster,
                        VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(portfolioMaster, "portfolioMaster");
    ArgumentChecker.notNull(positionMaster, "positionMaster");
    ArgumentChecker.notNull(securityMaster, "securityMaster");
    ArgumentChecker.notNull(versionCorrection, "versionCorrection");
    ArgumentChecker.notNull(securityMaster, "securityMaster");
    _securityMaster = securityMaster;
    _portfolioMaster = portfolioMaster;
    _versionCorrection = versionCorrection;
    _positionMaster = positionMaster;
    _securitySource = new MasterSecuritySource(securityMaster);
  }

  /**
   * Creates a mapping that uses {@link VersionCorrection#LATEST} in all master lookups.
   * @param portfolioMaster For looking up the portfolio
   * @param positionMaster For looking up positions
   * @param securityMaster For looking up securities
   */
  public PositionMapper(PortfolioMaster portfolioMaster, PositionMaster positionMaster, SecurityMaster securityMaster) {
    this(portfolioMaster, positionMaster, securityMaster, VersionCorrection.LATEST);
  }

  /**
   * Calls the function for every position in the portfolio and collects the results. Null results aren't included
   * @param portfolioObjectId Object ID of the portfolio
   * @param function Called for every position in the portfolio
   * @param <T> Type of the function's result
   * @return Values returned from the function, not including any nulls
   */
  public <T> List<T> map(String portfolioObjectId, Function<T> function) {
    ObjectId objectId = ObjectId.parse(portfolioObjectId);
    ManageablePortfolio portfolio = _portfolioMaster.get(objectId, _versionCorrection).getPortfolio();
    return map(portfolio.getRootNode(), function);
  }

  /**
   * Calls the function for every position in the portfolio and collects the results. Null results aren't included
   * @param portfolioId ID of the portfolio
   * @param function Called for every position in the portfolio
   * @param <T> Type of the function's result
   * @return Values returned from the function, not including any nulls
   */
  public <T> List<T> map(ObjectId portfolioId, Function<T> function) {
    ManageablePortfolio portfolio = _portfolioMaster.get(portfolioId, _versionCorrection).getPortfolio();
    return map(portfolio.getRootNode(), function);
  }

  /**
   * Calls the function for every position in the portfolio and collects the results. Null results aren't included.
   * The ID shouldn't include a version as the version correction is included in the class constructor.
   * @param unversionedPortfolioId Unique ID of the portfolio without a version
   * @param function Called for every position in the portfolio
   * @param <T> Type of the function's result
   * @return Values returned from the function, not including any nulls
   */
  public <T> List<T> map(UniqueId unversionedPortfolioId, Function<T> function) {
    if (unversionedPortfolioId.isVersioned()) {
      throw new IllegalArgumentException("Portfolio ID " + unversionedPortfolioId + " should be unversioned, " +
                                              "version/correction is set in the constructor");
    }
    ObjectId objectId = unversionedPortfolioId.getObjectId();
    ManageablePortfolio portfolio = _portfolioMaster.get(objectId, _versionCorrection).getPortfolio();
    return map(portfolio.getRootNode(), function);
  }

  /**
   * Calls the function for every position in the portfolio tree and collects the results. Null results aren't included
   * @param node Root of the portfolio tree
   * @param function Called for every position in the portfolio
   * @param <T> Type of the function's result
   * @return Values returned from the function, not including any nulls
   */
  public <T> List<T> map(ManageablePortfolioNode node, Function<T> function) {
    List<T> results = Lists.newArrayList();
    for (ObjectId positionId : node.getPositionIds()) {
      ManageablePosition position = _positionMaster.get(positionId, _versionCorrection).getPosition();
      if (position == null) {
        throw new DataNotFoundException("No position found with ID " + positionId + " and " +
                                            "version-correction " + _versionCorrection);
      }
      ManageableSecurity security = (ManageableSecurity) position.getSecurityLink().resolve(_securitySource);
      ExternalId underlyingId = FinancialSecurityUtils.getUnderlyingId(security);
      ManageableSecurity underlying;
      if (underlyingId != null) {
        SecuritySearchResult searchResult = _securityMaster.search(new SecuritySearchRequest(underlyingId));
        underlying = searchResult.getFirstSecurity();
      } else {
        underlying = null;
      }
      T result = function.apply(node, position, security, underlying);
      if (result != null) {
        results.add(result);
      }
    }
    for (ManageablePortfolioNode childNode : node.getChildNodes()) {
      results.addAll(map(childNode, function));
    }
    return results;
  }

  /**
   * Function which is invoked for every position encountered in the portfolio tree.
   * @param <T> Type of the return value
   */
  public interface Function<T> {

    /**
     * Invoked for every position encountered in the portfolio tree.
     * @param node The position's parent node, not null
     * @param position The position, not null
     * @param security The position's security, not null
     * @param underlying The position's underlying security, possibly null
     * @return A value derived from the position data, possibly null. Null values aren't included in the results
     */
    T apply(ManageablePortfolioNode node,
            ManageablePosition position,
            ManageableSecurity security,
            ManageableSecurity underlying);
  }
}
