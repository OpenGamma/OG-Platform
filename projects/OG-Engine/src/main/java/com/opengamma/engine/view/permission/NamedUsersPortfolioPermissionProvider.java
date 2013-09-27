/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.permission;

import java.util.Map;
import java.util.Set;

import com.opengamma.engine.view.client.NodeCheckingPortfolioFilter;
import com.opengamma.engine.view.client.PortfolioFilter;
import com.opengamma.engine.view.client.UserPermissionNodeChecker;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.util.ArgumentChecker;

/**
 *
 */
public class NamedUsersPortfolioPermissionProvider implements ViewPortfolioPermissionProvider {

  public static final NoOpPortfolioFilter NOOP_PORTFOLIO_FILTER = new NoOpPortfolioFilter();

  /**
   * The set of users to whom restrictions should be applied. Users not in this set will
   * have unfettered access, not null.
   */
  private final Set<String> _restrictedUsers;

  /**
   * Mapping of portfolio to the user who is allowed to see it. Portfolios not listed
   * have no restrictions on who can view them, not null.
   */
  private final Map<String, String> _portfolioUserMapping;

  /**
   * Create the permission provider for the users and portfolios.
   *
   * @param restrictedUsers the set of users to whom restrictions
   * should be applied. Users not in this set will have unfettered
   * access. Not null.
   * @param portfolioUserMapping mapping of portfolio to the user
   * who is allowed to see it. Portfolios not listed have no
   * restrictions on who can view them. Not null.
   */
  public NamedUsersPortfolioPermissionProvider(Set<String> restrictedUsers,
                                               Map<String, String> portfolioUserMapping) {
    ArgumentChecker.notNull(restrictedUsers, "restrictedUsers");
    ArgumentChecker.notNull(portfolioUserMapping, "portfolioUserMapping");
    _restrictedUsers = restrictedUsers;
    _portfolioUserMapping = portfolioUserMapping;
  }

  @Override
  public PortfolioFilter createPortfolioFilter(UserPrincipal user) {

    return _restrictedUsers.contains(user.getUserName()) ?
        new NodeCheckingPortfolioFilter(new UserPermissionNodeChecker(_portfolioUserMapping, user)) :
        NOOP_PORTFOLIO_FILTER;
  }
}
