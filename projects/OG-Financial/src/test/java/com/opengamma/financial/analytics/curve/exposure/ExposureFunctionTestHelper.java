/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.curve.exposure;

import java.util.Collection;
import java.util.Map;

import com.opengamma.core.change.ChangeManager;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.security.cash.CashSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtils;

/**
 * 
 */
public class ExposureFunctionTestHelper {
  private static final Currency USD = Currency.USD;
  private static final ExternalId US = ExternalId.of("Test", "US");
  private static final DayCount DC = DayCountFactory.INSTANCE.getDayCount("30/360");

  public static CashSecurity getCash() {
    final CashSecurity cash = new CashSecurity(USD, US, DateUtils.getUTCDate(2013, 1, 1), DateUtils.getUTCDate(2014, 1, 1), DC, 0.01, 10000);
    cash.setUniqueId(UniqueId.of(UniqueId.EXTERNAL_SCHEME.getName(), "123"));
    return cash;
  }

  public static SecuritySource getSecuritySource(final Security security) {
    return new SecuritySource() {

      @Override
      public Collection<Security> get(final ExternalIdBundle bundle, final VersionCorrection versionCorrection) {
        return null;
      }

      @Override
      public Map<ExternalIdBundle, Collection<Security>> getAll(final Collection<ExternalIdBundle> bundles, final VersionCorrection versionCorrection) {
        return null;
      }

      @Override
      public Collection<Security> get(final ExternalIdBundle bundle) {
        return null;
      }

      @Override
      public Security getSingle(final ExternalIdBundle bundle) {
        return security;
      }

      @Override
      public Security getSingle(final ExternalIdBundle bundle, final VersionCorrection versionCorrection) {
        return null;
      }

      @Override
      public Map<ExternalIdBundle, Security> getSingle(final Collection<ExternalIdBundle> bundles, final VersionCorrection versionCorrection) {
        return null;
      }

      @Override
      public Security get(final UniqueId uniqueId) {
        return null;
      }

      @Override
      public Security get(final ObjectId objectId, final VersionCorrection versionCorrection) {
        return null;
      }

      @Override
      public Map<UniqueId, Security> get(final Collection<UniqueId> uniqueIds) {
        return null;
      }

      @Override
      public Map<ObjectId, Security> get(final Collection<ObjectId> objectIds, final VersionCorrection versionCorrection) {
        return null;
      }

      @Override
      public ChangeManager changeManager() {
        return null;
      }

    };
  }
}
