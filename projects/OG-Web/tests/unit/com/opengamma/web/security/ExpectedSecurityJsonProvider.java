/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.web.security;

import static org.testng.AssertJUnit.assertNotNull;

import java.io.File;
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.json.JSONObject;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.financial.security.FinancialSecurityVisitor;
import com.opengamma.financial.security.bond.BondSecurity;
import com.opengamma.financial.security.capfloor.CapFloorCMSSpreadSecurity;
import com.opengamma.financial.security.capfloor.CapFloorSecurity;
import com.opengamma.financial.security.cash.CashSecurity;
import com.opengamma.financial.security.equity.EquitySecurity;
import com.opengamma.financial.security.equity.EquityVarianceSwapSecurity;
import com.opengamma.financial.security.fra.FRASecurity;
import com.opengamma.financial.security.future.AgricultureFutureSecurity;
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
import com.opengamma.financial.security.option.EquityIndexOptionSecurity;
import com.opengamma.financial.security.option.EquityOptionSecurity;
import com.opengamma.financial.security.option.FXBarrierOptionSecurity;
import com.opengamma.financial.security.option.FXOptionSecurity;
import com.opengamma.financial.security.option.IRFutureOptionSecurity;
import com.opengamma.financial.security.option.NonDeliverableFXOptionSecurity;
import com.opengamma.financial.security.option.SwaptionSecurity;
import com.opengamma.financial.security.swap.SwapSecurity;

/**
 * Gets expected Json document for different security types
 */
/* package */ class ExpectedSecurityJsonProvider implements FinancialSecurityVisitor<JSONObject> {
  
  private JSONObject loadJsonFile(String filename) {
    assertNotNull(filename);
    URL jsonResource = getClass().getResource(filename);
    assertNotNull(jsonResource);
    JSONObject expectedJson = null;
    try {
      expectedJson = new JSONObject(FileUtils.readFileToString(new File(jsonResource.getPath())));
    } catch (Exception ex) {
      throw new OpenGammaRuntimeException("Problem creating json document from " + filename, ex);
    }
    return expectedJson;
  }

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
    return loadJsonFile("equityJson.txt");
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
        return loadJsonFile("bondFutureJson.txt");
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

}
