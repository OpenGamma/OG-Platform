/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.security;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.joda.beans.Bean;
import org.json.JSONObject;

import com.google.common.collect.Maps;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.FinancialSecurityVisitorSameValueAdapter;
import com.opengamma.financial.security.equity.EquitySecurity;
import com.opengamma.financial.security.future.AgricultureFutureSecurity;
import com.opengamma.financial.security.future.BondFutureDeliverable;
import com.opengamma.financial.security.future.BondFutureSecurity;
import com.opengamma.financial.security.future.EnergyFutureSecurity;
import com.opengamma.financial.security.future.EquityFutureSecurity;
import com.opengamma.financial.security.future.EquityIndexDividendFutureSecurity;
import com.opengamma.financial.security.future.FXFutureSecurity;
import com.opengamma.financial.security.future.FutureSecurity;
import com.opengamma.financial.security.future.IndexFutureSecurity;
import com.opengamma.financial.security.future.InterestRateFutureSecurity;
import com.opengamma.financial.security.future.MetalFutureSecurity;
import com.opengamma.financial.security.future.StockFutureSecurity;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.JodaBeanSerialization;
import com.opengamma.util.time.Expiry;

/**
 * Gets expected Json document for different security types
 */
/* package */ class ExpectedSecurityJsonProvider extends FinancialSecurityVisitorSameValueAdapter<JSONObject> {

  ExpectedSecurityJsonProvider() {
    super(null);
  }

  private static final String TEMPLATE_DATA = "template_data";

  @Override
  public JSONObject visitEquitySecurity(EquitySecurity security) {
    Map<String, Object> secMap = Maps.newHashMap();

    Map<String, Object> templateData = Maps.newHashMap();
    addDefaultFields(security, templateData);
    
    if (StringUtils.isNotBlank(security.getShortName())) {
      templateData.put("shortName", security.getShortName());
    }
    if (StringUtils.isNotBlank(security.getExchange())) {
      templateData.put("exchange", security.getExchange());
    }
    if (security.getCurrency() != null && StringUtils.isNotBlank(security.getCurrency().getCode())) {
      templateData.put("currency", security.getCurrency().getCode());
    }
    if (StringUtils.isNotBlank(security.getCompanyName())) {
      templateData.put("companyName", security.getCompanyName());
    }
    if (StringUtils.isNotBlank(security.getExchangeCode())) {
      templateData.put("exchangeCode", security.getExchangeCode());
    }
    if (security.getGicsCode() != null && StringUtils.isNotBlank(security.getGicsCode().toString())) {
      templateData.put("gicsCode", security.getGicsCode().toString());
    }
    secMap.put(TEMPLATE_DATA, templateData);
    addSecurityXml(security, secMap);
    addExternalIds(security, secMap);
    return new JSONObject(secMap);
  }

  @Override
  public JSONObject visitAgricultureFutureSecurity(AgricultureFutureSecurity security) {
    return visitFutureSecurity(security);
  }

  @Override
  public JSONObject visitBondFutureSecurity(BondFutureSecurity security) {
    return visitFutureSecurity(security);
  }

  @Override
  public JSONObject visitEnergyFutureSecurity(EnergyFutureSecurity security) {
    return visitFutureSecurity(security);
  }

  @Override
  public JSONObject visitEquityFutureSecurity(EquityFutureSecurity security) {
    return visitFutureSecurity(security);
  }

  @Override
  public JSONObject visitEquityIndexDividendFutureSecurity(EquityIndexDividendFutureSecurity security) {
    return visitFutureSecurity(security);
  }

  @Override
  public JSONObject visitFXFutureSecurity(FXFutureSecurity security) {
    return visitFutureSecurity(security);
  }

  @Override
  public JSONObject visitIndexFutureSecurity(IndexFutureSecurity security) {
    return visitFutureSecurity(security);
  }

  @Override
  public JSONObject visitInterestRateFutureSecurity(InterestRateFutureSecurity security) {
    return visitFutureSecurity(security);
  }

  @Override
  public JSONObject visitMetalFutureSecurity(MetalFutureSecurity security) {
    return visitFutureSecurity(security);
  }

  @Override
  public JSONObject visitStockFutureSecurity(StockFutureSecurity security) {
    return visitFutureSecurity(security);
  }

  private JSONObject visitFutureSecurity(FutureSecurity security) {
    JSONObject result = security.accept(new FinancialSecurityVisitorSameValueAdapter<JSONObject>(null) {

      @Override
      public JSONObject visitBondFutureSecurity(BondFutureSecurity security) {
        Map<String, Object> secMap = Maps.newHashMap();

        Map<String, Object> templateData = Maps.newHashMap();
        addDefaultFields(security, templateData);
        addExpiry(templateData, security.getExpiry());
        templateData.put("firstDeliveryDate", security.getFirstDeliveryDate().toString());
        templateData.put("lastDeliveryDate", security.getLastDeliveryDate().toString());
        if (StringUtils.isNotBlank(security.getTradingExchange())) {
          templateData.put("tradingExchange", security.getTradingExchange());
        }
        if (StringUtils.isNotBlank(security.getSettlementExchange())) {
          templateData.put("settlementExchange", security.getSettlementExchange());
        }
        if (security.getCurrency() != null && StringUtils.isNotBlank(security.getCurrency().getCode())) {
          templateData.put("currency", security.getCurrency().getCode());
        }
        List<BondFutureDeliverable> basket = security.getBasket();
        if (!basket.isEmpty()) {
          Map<String, String> underlyingBond = Maps.newHashMap();
          for (BondFutureDeliverable bondFutureDeliverable : basket) {
            underlyingBond.put(ExternalSchemes.BLOOMBERG_TICKER.getName() + "-" + bondFutureDeliverable.getIdentifiers().getValue(ExternalSchemes.BLOOMBERG_TICKER),
              String.valueOf(bondFutureDeliverable.getConversionFactor()));
          }
          templateData.put("underlyingBond", underlyingBond);
        }
        templateData.put("unitAmount", security.getUnitAmount());
        secMap.put(TEMPLATE_DATA, templateData);
        addSecurityXml(security, secMap);
        addExternalIds(security, secMap);
        return new JSONObject(secMap);
      }
    });
    return result;
  }
  
  private void addSecurityXml(FinancialSecurity security, Map<String, Object> secMap) {
    String secXml = JodaBeanSerialization.serializer(true).xmlWriter().write((Bean) security, true);
    secMap.put("securityXml", secXml);
  }

  private void addExternalIds(FinancialSecurity security, Map<String, Object> secMap) {
    Map<String, String> identifiers = Maps.newHashMap();
    ExternalIdBundle externalIdBundle = security.getExternalIdBundle();
    if (externalIdBundle.getExternalId(ExternalSchemes.BLOOMBERG_BUID) != null) {
      identifiers.put(ExternalSchemes.BLOOMBERG_BUID.getName(), ExternalSchemes.BLOOMBERG_BUID.getName() + "-" + externalIdBundle.getValue(ExternalSchemes.BLOOMBERG_BUID));
    }
    if (externalIdBundle.getExternalId(ExternalSchemes.BLOOMBERG_TICKER) != null) {
      identifiers.put(ExternalSchemes.BLOOMBERG_TICKER.getName(), ExternalSchemes.BLOOMBERG_TICKER.getName() + "-" + externalIdBundle.getValue(ExternalSchemes.BLOOMBERG_TICKER));
    }
    if (externalIdBundle.getExternalId(ExternalSchemes.CUSIP) != null) {
      identifiers.put(ExternalSchemes.CUSIP.getName(), ExternalSchemes.CUSIP.getName() + "-" + externalIdBundle.getValue(ExternalSchemes.CUSIP));
    }
    if (externalIdBundle.getExternalId(ExternalSchemes.ISIN) != null) {
      identifiers.put(ExternalSchemes.ISIN.getName(), ExternalSchemes.ISIN.getName() + "-" + externalIdBundle.getValue(ExternalSchemes.ISIN));
    }
    if (externalIdBundle.getExternalId(ExternalSchemes.SEDOL1) != null) {
      identifiers.put(ExternalSchemes.SEDOL1.getName(), ExternalSchemes.SEDOL1.getName() + "-" + externalIdBundle.getValue(ExternalSchemes.SEDOL1));
    }
    secMap.put("identifiers", identifiers);
  }

  private void addDefaultFields(FinancialSecurity security, Map<String, Object> templateData) {
    if (StringUtils.isNotBlank(security.getName())) {
      templateData.put("name", security.getName());
    }
    if (StringUtils.isNotBlank(security.getSecurityType())) {
      templateData.put("securityType", security.getSecurityType());
    }
    if (security.getUniqueId() != null && security.getUniqueId().getObjectId() != null && StringUtils.isNotBlank(security.getUniqueId().getObjectId().toString())) {
      templateData.put("object_id", security.getUniqueId().getObjectId().toString());
    }
    if (security.getUniqueId() != null && StringUtils.isNotBlank(security.getUniqueId().getVersion())) {
      templateData.put("version_id", security.getUniqueId().getVersion());
    }
    if (security.getAttributes() != null && !security.getAttributes().isEmpty()) {
      templateData.put("attributes", security.getAttributes());
    }
  }

  private void addExpiry(Map<String, Object> templateData, Expiry expiry) {
    Map<String, Object> expiryDateMap = Maps.newHashMap();
    expiryDateMap.put("datetime", expiry.getExpiry().toOffsetDateTime().toString());
    expiryDateMap.put("timezone", expiry.getExpiry().getZone().toString());
    templateData.put("expiryAccuracy", expiry.getAccuracy().toString().replace("_", " "));
    templateData.put("expirydate", expiryDateMap);
  }

}
