/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.web.security;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;

import com.google.common.collect.Maps;
import com.opengamma.core.security.SecurityUtils;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.FinancialSecurityVisitor;
import com.opengamma.financial.security.bond.BondSecurity;
import com.opengamma.financial.security.capfloor.CapFloorCMSSpreadSecurity;
import com.opengamma.financial.security.capfloor.CapFloorSecurity;
import com.opengamma.financial.security.cash.CashSecurity;
import com.opengamma.financial.security.equity.EquitySecurity;
import com.opengamma.financial.security.equity.EquityVarianceSwapSecurity;
import com.opengamma.financial.security.fra.FRASecurity;
import com.opengamma.financial.security.future.AgricultureFutureSecurity;
import com.opengamma.financial.security.future.BondFutureDeliverable;
import com.opengamma.financial.security.future.BondFutureSecurity;
import com.opengamma.financial.security.future.EnergyFutureSecurity;
import com.opengamma.financial.security.future.EquityFutureSecurity;
import com.opengamma.financial.security.future.EquityIndexDividendFutureSecurity;
import com.opengamma.financial.security.future.FXFutureSecurity;
import com.opengamma.financial.security.future.FutureSecurity;
import com.opengamma.financial.security.future.FutureSecurityVisitor;
import com.opengamma.financial.security.future.IndexFutureSecurity;
import com.opengamma.financial.security.future.InterestRateFutureSecurity;
import com.opengamma.financial.security.future.MetalFutureSecurity;
import com.opengamma.financial.security.future.StockFutureSecurity;
import com.opengamma.financial.security.fx.FXForwardSecurity;
import com.opengamma.financial.security.fx.FXSecurity;
import com.opengamma.financial.security.fx.NonDeliverableFXForwardSecurity;
import com.opengamma.financial.security.option.EquityBarrierOptionSecurity;
import com.opengamma.financial.security.option.EquityIndexDividendFutureOptionSecurity;
import com.opengamma.financial.security.option.EquityIndexOptionSecurity;
import com.opengamma.financial.security.option.EquityOptionSecurity;
import com.opengamma.financial.security.option.FXBarrierOptionSecurity;
import com.opengamma.financial.security.option.FXOptionSecurity;
import com.opengamma.financial.security.option.IRFutureOptionSecurity;
import com.opengamma.financial.security.option.NonDeliverableFXOptionSecurity;
import com.opengamma.financial.security.option.SwaptionSecurity;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.time.Expiry;

/**
 * Gets expected Json document for different security types
 */
/* package */ class ExpectedSecurityJsonProvider implements FinancialSecurityVisitor<JSONObject> {
  
  private static final String TEMPLATE_DATA = "template_data";
 
  @Override
  public JSONObject visitBondSecurity(BondSecurity security) {
    return null;
  }

  @Override
  public JSONObject visitCashSecurity(CashSecurity security) {
    return null;
  }

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
    addExternalIds(security, secMap);
    return new JSONObject(secMap);
  }

  @Override
  public JSONObject visitFRASecurity(FRASecurity security) {
    return null;
  }

  @Override
  public JSONObject visitFutureSecurity(FutureSecurity security) {
    JSONObject result = security.accept(new FutureSecurityVisitor<JSONObject>() {

      @Override
      public JSONObject visitAgricultureFutureSecurity(AgricultureFutureSecurity security) {
        return null;
      }

      @Override
      public JSONObject visitBondFutureSecurity(BondFutureSecurity security) {
        Map<String, Object> secMap = Maps.newHashMap();
        
        Map<String, Object> templateData = Maps.newHashMap();
        addDefaultFields(security, templateData);
        addExpiry(templateData, security.getExpiry());
        
        if (StringUtils.isNotBlank(security.getTradingExchange())) {
          templateData.put("tradingExchange", security.getTradingExchange());
        }
        if (StringUtils.isNotBlank(security.getSettlementExchange())) {
          templateData.put("settlementExchange", security.getSettlementExchange());
        }
        if (security.getCurrency() != null && StringUtils.isNotBlank(security.getCurrency().getCode())) {
          templateData.put("redemptionValue", security.getCurrency().getCode());
        }
        List<BondFutureDeliverable> basket = security.getBasket();
        if (!basket.isEmpty()) {
          Map<String, String> underlyingBond = Maps.newHashMap();
          for (BondFutureDeliverable bondFutureDeliverable : basket) {
            underlyingBond.put(SecurityUtils.BLOOMBERG_BUID.getName() + "-" + bondFutureDeliverable.getIdentifiers().getValue(SecurityUtils.BLOOMBERG_BUID), 
                String.valueOf(bondFutureDeliverable.getConversionFactor()));
          }
          templateData.put("underlyingBond", underlyingBond);
        }
        secMap.put(TEMPLATE_DATA, templateData);
        addExternalIds(security, secMap);
        return new JSONObject(secMap);
      }

      @Override
      public JSONObject visitEnergyFutureSecurity(EnergyFutureSecurity security) {
        return null;
      }

      @Override
      public JSONObject visitEquityFutureSecurity(EquityFutureSecurity security) {
        return null;
      }

      @Override
      public JSONObject visitEquityIndexDividendFutureSecurity(EquityIndexDividendFutureSecurity security) {
        return null;
      }

      @Override
      public JSONObject visitFXFutureSecurity(FXFutureSecurity security) {
        return null;
      }

      @Override
      public JSONObject visitIndexFutureSecurity(IndexFutureSecurity security) {
        return null;
      }

      @Override
      public JSONObject visitInterestRateFutureSecurity(InterestRateFutureSecurity security) {
        return null;
      }

      @Override
      public JSONObject visitMetalFutureSecurity(MetalFutureSecurity security) {
        return null;
      }

      @Override
      public JSONObject visitStockFutureSecurity(StockFutureSecurity security) {
        return null;
      }
    });
    return result;
  }

  @Override
  public JSONObject visitSwapSecurity(SwapSecurity security) {
    return null;
  }

  @Override
  public JSONObject visitEquityIndexOptionSecurity(EquityIndexOptionSecurity security) {
    return null;
  }

  @Override
  public JSONObject visitEquityOptionSecurity(EquityOptionSecurity security) {
    return null;
  }

  @Override
  public JSONObject visitFXOptionSecurity(FXOptionSecurity security) {
    return null;
  }

  @Override
  public JSONObject visitSwaptionSecurity(SwaptionSecurity security) {
    return null;
  }

  @Override
  public JSONObject visitIRFutureOptionSecurity(IRFutureOptionSecurity security) {
    return null;
  }
  
  @Override
  public JSONObject visitEquityIndexDividendFutureOptionSecurity(
      EquityIndexDividendFutureOptionSecurity equityIndexDividendFutureOptionSecurity) {
    return null;
  }

  @Override
  public JSONObject visitFXBarrierOptionSecurity(FXBarrierOptionSecurity security) {
    return null;
  }

  @Override
  public JSONObject visitFXSecurity(FXSecurity security) {
    return null;
  }

  @Override
  public JSONObject visitFXForwardSecurity(FXForwardSecurity security) {
    return null;
  }

  @Override
  public JSONObject visitCapFloorSecurity(CapFloorSecurity security) {
    return null;
  }

  @Override
  public JSONObject visitCapFloorCMSSpreadSecurity(CapFloorCMSSpreadSecurity security) {
    return null;
  }

  @Override
  public JSONObject visitEquityVarianceSwapSecurity(EquityVarianceSwapSecurity security) {
    return null;
  }

  @Override
  public JSONObject visitEquityBarrierOptionSecurity(EquityBarrierOptionSecurity security) {
    return null;
  }

  @Override
  public JSONObject visitNonDeliverableFXOptionSecurity(NonDeliverableFXOptionSecurity security) {
    return null;
  }

  @Override
  public JSONObject visitNonDeliverableFXForwardSecurity(NonDeliverableFXForwardSecurity security) {
    return null;
  }
  
  private void addExternalIds(FinancialSecurity security, Map<String, Object> secMap) {
    Map<String, String> identifiers = Maps.newHashMap();
    ExternalIdBundle externalIdBundle = security.getExternalIdBundle();
    if (externalIdBundle.getExternalId(SecurityUtils.BLOOMBERG_BUID) != null) {
      identifiers.put(SecurityUtils.BLOOMBERG_BUID.getName(),  SecurityUtils.BLOOMBERG_BUID.getName() + "-" + externalIdBundle.getValue(SecurityUtils.BLOOMBERG_BUID));
    }
    if (externalIdBundle.getExternalId(SecurityUtils.BLOOMBERG_TICKER) != null) {
      identifiers.put(SecurityUtils.BLOOMBERG_TICKER.getName(), SecurityUtils.BLOOMBERG_TICKER.getName() + "-" + externalIdBundle.getValue(SecurityUtils.BLOOMBERG_TICKER));
    }
    if (externalIdBundle.getExternalId(SecurityUtils.CUSIP) != null) {
      identifiers.put(SecurityUtils.CUSIP.getName(), SecurityUtils.CUSIP.getName() + "-" + externalIdBundle.getValue(SecurityUtils.CUSIP));
    }
    if (externalIdBundle.getExternalId(SecurityUtils.ISIN) != null) {
      identifiers.put(SecurityUtils.ISIN.getName(), SecurityUtils.ISIN.getName() + "-" + externalIdBundle.getValue(SecurityUtils.ISIN));
    }
    if (externalIdBundle.getExternalId(SecurityUtils.SEDOL1) != null) {
      identifiers.put(SecurityUtils.SEDOL1.getName(), SecurityUtils.SEDOL1.getName() + "-" + externalIdBundle.getValue(SecurityUtils.SEDOL1));
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
  }
  
  private void addExpiry(Map<String, Object> templateData, Expiry expiry) {
    Map<String, Object> expiryDateMap = Maps.newHashMap();
    expiryDateMap.put("datetime", expiry.getExpiry().toOffsetDateTime().toString());
    expiryDateMap.put("timezone", expiry.getExpiry().getZone().toString());
    templateData.put("expiryAccuracy", expiry.getAccuracy().toString().replace("_", " "));
    templateData.put("expirydate", expiryDateMap);
  }

}
