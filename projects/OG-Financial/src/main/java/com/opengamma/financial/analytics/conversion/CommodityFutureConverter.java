/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.conversion;

import java.util.Set;

import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.Validate;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.commodity.definition.AgricultureFutureDefinition;
import com.opengamma.analytics.financial.commodity.definition.CommodityFutureDefinition;
import com.opengamma.analytics.financial.commodity.definition.EnergyFutureDefinition;
import com.opengamma.analytics.financial.commodity.definition.MetalFutureDefinition;
import com.opengamma.analytics.financial.commodity.definition.SettlementType;
import com.opengamma.financial.security.future.AgricultureFutureSecurity;
import com.opengamma.financial.security.future.EnergyFutureSecurity;
import com.opengamma.financial.security.future.MetalFutureSecurity;
import com.opengamma.financial.security.future.StockFutureSecurity;
import com.opengamma.id.ExternalId;

/**
 *
 */
public class CommodityFutureConverter extends AbstractFutureSecurityVisitor<CommodityFutureDefinition<?>> {

  @Override
  public CommodityFutureDefinition<?> visitAgricultureFutureSecurity(final AgricultureFutureSecurity security) {
    return visitAgricultureFutureSecurity(security, 0.0);
  }

  public AgricultureFutureDefinition visitAgricultureFutureSecurity(final AgricultureFutureSecurity security, final double referencePrice) {
    Validate.notNull(security, "security");
    final ZonedDateTime expiry = security.getExpiry().getExpiry();
    final Set<ExternalId> externalIds = security.getExternalIdBundle().getExternalIds();
    if (externalIds == null) {
      throw new OpenGammaRuntimeException("Can get security id");
    }
    return new AgricultureFutureDefinition(expiry, externalIds.iterator().next(), security.getUnitAmount(), null, null,
        security.getUnitNumber(), security.getUnitName(), SettlementType.CASH, referencePrice, security.getCurrency(), expiry);
  }

  @Override
  public EnergyFutureDefinition visitEnergyFutureSecurity(final EnergyFutureSecurity security) {
    return visitEnergyFutureSecurity(security, 0.0);
  }

  public EnergyFutureDefinition visitEnergyFutureSecurity(final EnergyFutureSecurity security, final double referencePrice) {
    Validate.notNull(security, "security");
    final ZonedDateTime expiry = security.getExpiry().getExpiry();
    //final ExternalId externalId = security.getExternalIdBundle().getExternalId(SCHEME);
    final Set<ExternalId> externalIds = security.getExternalIdBundle().getExternalIds();
    if (externalIds == null) {
      throw new OpenGammaRuntimeException("Can get security id");
    }
    return new EnergyFutureDefinition(expiry, externalIds.iterator().next(), security.getUnitAmount(), null, null,
        security.getUnitNumber(), security.getUnitName(), SettlementType.CASH, referencePrice, security.getCurrency(), expiry);
  }

  @Override
  public MetalFutureDefinition visitMetalFutureSecurity(final MetalFutureSecurity security) {
    return visitMetalFutureSecurity(security, 0.0);
  }

  public MetalFutureDefinition visitMetalFutureSecurity(final MetalFutureSecurity security, final double referencePrice) {
    Validate.notNull(security, "security");
    final ZonedDateTime expiry = security.getExpiry().getExpiry();
    final Set<ExternalId> externalIds = security.getExternalIdBundle().getExternalIds();
    if (externalIds == null) {
      throw new OpenGammaRuntimeException("Cannot get security id");
    }
    return new MetalFutureDefinition(expiry, externalIds.iterator().next(), security.getUnitAmount(), null, null,
        security.getUnitNumber(), security.getUnitName(), SettlementType.CASH, referencePrice, security.getCurrency(), expiry);
  }

  @Override
  public CommodityFutureDefinition<?> visitStockFutureSecurity(final StockFutureSecurity security) {
    throw new UnsupportedOperationException("Cannot use this converter for InterestRateFutureSecurity");
  }

}
