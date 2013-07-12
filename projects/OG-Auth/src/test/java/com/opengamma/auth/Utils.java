package com.opengamma.auth;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Arrays.asList;

import java.util.Collection;

import com.opengamma.auth.master.portfolio.PortfolioCapability;
import com.opengamma.auth.master.portfolio.PortfolioEntitlement;

/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * <p/>
 * Please see distribution for license.
 */
public class Utils {

  public static PortfolioCapability toCapability(PortfolioEntitlement... entitlements) {
    return toCapability(asList(entitlements));
  }

  public static PortfolioCapability toCapability(Collection<PortfolioEntitlement> entitlements) {
    Collection<SignedMessage<PortfolioEntitlement>> messages = newArrayList();
    for (PortfolioEntitlement entitlement : entitlements) {
      messages.add(Signer.sign(entitlement));
    }
    return PortfolioCapability.of(messages);
  }
}
