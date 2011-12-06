/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.security;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

import org.joda.beans.impl.flexi.FlexiBean;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.opengamma.core.config.ConfigSource;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecurityUtils;
import com.opengamma.financial.security.capfloor.CapFloorCMSSpreadSecurity;
import com.opengamma.financial.security.capfloor.CapFloorSecurity;
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
import com.opengamma.financial.security.option.EquityBarrierOptionSecurity;
import com.opengamma.financial.security.option.EquityIndexOptionSecurity;
import com.opengamma.financial.security.option.EquityOptionSecurity;
import com.opengamma.financial.security.option.IRFutureOptionSecurity;
import com.opengamma.financial.security.option.SwaptionSecurity;
import com.opengamma.financial.security.swap.FixedInterestRateLeg;
import com.opengamma.financial.security.swap.FloatingGearingIRLeg;
import com.opengamma.financial.security.swap.FloatingInterestRateLeg;
import com.opengamma.financial.security.swap.FloatingSpreadIRLeg;
import com.opengamma.financial.security.swap.SwapLegVisitor;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesMaster;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesResolver;
import com.opengamma.master.historicaltimeseries.impl.DefaultHistoricalTimeSeriesResolver;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.master.security.SecurityLoader;
import com.opengamma.master.security.SecurityMaster;
import com.opengamma.master.security.SecuritySearchRequest;
import com.opengamma.master.security.SecuritySearchResult;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.web.AbstractWebResource;
import com.opengamma.web.WebHomeUris;

/**
 * Abstract base class for RESTful security resources.
 */
public abstract class AbstractWebSecurityResource extends AbstractWebResource {

  /**
   * The backing bean.
   */
  private final WebSecuritiesData _data;
  
  /**
   * The HTS resolver (for getting an HTS Id)
   */
  private final HistoricalTimeSeriesResolver _htsResolver;
  
  /**
   * Creates the resource.
   * @param securityMaster  the security master, not null
   * @param securityLoader  the security loader, not null
   * @param htsMaster       the HTS master, not null (needed for fetching HTS object ID)
   * @param cfgSource       the config source, not null (needed for fetching HTS object ID)
   */
  protected AbstractWebSecurityResource(
      final SecurityMaster securityMaster, final SecurityLoader securityLoader, 
      final HistoricalTimeSeriesMaster htsMaster, final ConfigSource cfgSource) {
    ArgumentChecker.notNull(securityMaster, "securityMaster");
    ArgumentChecker.notNull(securityLoader, "securityLoader");
    _data = new WebSecuritiesData();
    data().setSecurityMaster(securityMaster);
    data().setSecurityLoader(securityLoader);

    //ClassPathXmlApplicationContext appContext = new ClassPathXmlApplicationContext("com/opengamma/financial/demoMasters.xml");
    //HistoricalTimeSeriesMaster htsMaster = appContext.getBean("dbHtsMaster", HistoricalTimeSeriesMaster.class);
    //ConfigSource cfgSource = appContext.getBean("sharedConfigSource", ConfigSource.class);
    _htsResolver = new DefaultHistoricalTimeSeriesResolver(htsMaster, cfgSource);    
  }

  /**
   * Creates the resource.
   * @param parent  the parent resource, not null
   */
  protected AbstractWebSecurityResource(final AbstractWebSecurityResource parent) {
    super(parent);
    _data = parent._data;
    _htsResolver = parent._htsResolver;
  }

  /**
   * Setter used to inject the URIInfo.
   * This is a roundabout approach, because Spring and JSR-311 injection clash.
   * DO NOT CALL THIS METHOD DIRECTLY.
   * @param uriInfo  the URI info, not null
   */
  @Context
  public void setUriInfo(final UriInfo uriInfo) {
    data().setUriInfo(uriInfo);
  }

  //-------------------------------------------------------------------------
  /**
   * Creates the output root data.
   * @return the output root data, not null
   */
  protected FlexiBean createRootData() {
    FlexiBean out = getFreemarker().createRootData();
    out.put("homeUris", new WebHomeUris(data().getUriInfo()));
    out.put("uris", new WebSecuritiesUris(data()));
    return out;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the backing bean.
   * @return the beacking bean, not null
   */
  protected WebSecuritiesData data() {
    return _data;
  }
  
  /**
   * Gets the HTS resolver
   * @return the HTS resolver, not null
   */
  protected HistoricalTimeSeriesResolver htsResolver() {
    return _htsResolver;
  }
  
  protected void addSecuritySpecificMetaData(ManageableSecurity security, FlexiBean out) {
    
    if (security.getSecurityType().equals(SwapSecurity.SECURITY_TYPE)) {
      SwapSecurity swapSecurity = (SwapSecurity) security;
      out.put("payLegType", swapSecurity.getPayLeg().accept(new SwapLegClassifierVisitor()));
      out.put("receiveLegType", swapSecurity.getReceiveLeg().accept(new SwapLegClassifierVisitor()));
    }
    if (security.getSecurityType().equals(FutureSecurity.SECURITY_TYPE)) {
      FutureSecurity futureSecurity = (FutureSecurity) security;
      out.put("futureSecurityType", futureSecurity.accept(new FutureSecurityTypeVisitor()));
      out.put("basket", getBondFutureBasket(security));
      Security underlyingSecurity = getUnderlyingFutureSecurity(futureSecurity);
      if (underlyingSecurity != null) {
        out.put("underlyingSecurity", underlyingSecurity);
      }
    }
    if (security.getSecurityType().equals(EquityOptionSecurity.SECURITY_TYPE)) {
      EquityOptionSecurity equityOption = (EquityOptionSecurity) security;
      addUnderlyingSecurity(out, equityOption.getUnderlyingId());
    }
    if (security.getSecurityType().equals(IRFutureOptionSecurity.SECURITY_TYPE)) {
      IRFutureOptionSecurity irFutureOption = (IRFutureOptionSecurity) security;
      addUnderlyingSecurity(out, irFutureOption.getUnderlyingId());
    }
    if (security.getSecurityType().equals(SwaptionSecurity.SECURITY_TYPE)) {
      SwaptionSecurity swaptionSecurity = (SwaptionSecurity) security;
      addUnderlyingSecurity(out, swaptionSecurity.getUnderlyingId());
    }
    if (security.getSecurityType().equals(EquityBarrierOptionSecurity.SECURITY_TYPE)) {
      EquityBarrierOptionSecurity equityBarrierOptionSecurity = (EquityBarrierOptionSecurity) security;
      addUnderlyingSecurity(out, equityBarrierOptionSecurity.getUnderlyingId());
    }
    if (security.getSecurityType().equals(CapFloorSecurity.SECURITY_TYPE)) {
      CapFloorSecurity capFloorSecurity = (CapFloorSecurity) security;
      addUnderlyingSecurity(out, capFloorSecurity.getUnderlyingId());
    }
    if (security.getSecurityType().equals(CapFloorCMSSpreadSecurity.SECURITY_TYPE)) {
      CapFloorCMSSpreadSecurity capFloorCMSSpreadSecurity = (CapFloorCMSSpreadSecurity) security;
      Security shortUnderlying = getSecurity(capFloorCMSSpreadSecurity.getShortId());
      Security longUnderlying = getSecurity(capFloorCMSSpreadSecurity.getLongId());
      if (shortUnderlying != null) {
        out.put("shortSecurity", shortUnderlying);
      }
      if (longUnderlying != null) {
        out.put("longSecurity", longUnderlying);
      }
    }
    if (security.getSecurityType().equals(FXForwardSecurity.SECURITY_TYPE)) {
      FXForwardSecurity fxforwardSecurity = (FXForwardSecurity) security;
      addUnderlyingSecurity(out, fxforwardSecurity.getUnderlyingId());
    }
    if (security.getSecurityType().equals(EquityIndexOptionSecurity.SECURITY_TYPE)) {
      EquityIndexOptionSecurity equityIndxOption = (EquityIndexOptionSecurity) security;
      addUnderlyingSecurity(out, equityIndxOption.getUnderlyingId());
    }
    if (security.getSecurityType().equals(FRASecurity.SECURITY_TYPE)) {
      FRASecurity fraSecurity = (FRASecurity) security;
      addUnderlyingSecurity(out, fraSecurity.getUnderlyingId());
    }
  }

  private void addUnderlyingSecurity(FlexiBean out, ExternalId externalId) {
    Security underlyingSec = getSecurity(externalId);
    if (underlyingSec != null) {
      out.put("underlyingSecurity", underlyingSec);
    }
  }

  private ManageableSecurity getSecurity(ExternalId underlyingIdentifier) {
    if (underlyingIdentifier == null) {
      return null;
    }
    SecurityMaster securityMaster = data().getSecurityMaster();
    SecuritySearchRequest request = new SecuritySearchRequest();
    request.addExternalId(underlyingIdentifier);
    SecuritySearchResult search = securityMaster.search(request);
    return search.getFirstSecurity();
  }

  private Map<String, Double> getBondFutureBasket(ManageableSecurity security) {
    Map<String, Double> result = new TreeMap<String, Double>();
    if (security instanceof BondFutureSecurity) {
      BondFutureSecurity bondFutureSecurity = (BondFutureSecurity) security;
      List<BondFutureDeliverable> basket = bondFutureSecurity.getBasket();
      for (BondFutureDeliverable bondFutureDeliverable : basket) {
        String identifierValue = bondFutureDeliverable.getIdentifiers().getValue(SecurityUtils.BLOOMBERG_BUID);
        result.put("BLOOMBERG BUID - " + identifierValue, bondFutureDeliverable.getConversionFactor());
      }
    }
    return result;
  }
  
  private Security getUnderlyingFutureSecurity(FutureSecurity future) {
    return future.accept(new FutureSecurityVisitor<Security>() {

      @Override
      public Security visitAgricultureFutureSecurity(AgricultureFutureSecurity security) {
        return null;
      }

      @Override
      public Security visitBondFutureSecurity(BondFutureSecurity security) {
        return null;
      }

      @Override
      public Security visitEnergyFutureSecurity(EnergyFutureSecurity security) {
        return getSecurity(security.getUnderlyingId());
      }

      @Override
      public Security visitEquityFutureSecurity(EquityFutureSecurity security) {
        return getSecurity(security.getUnderlyingId());
      }

      @Override
      public Security visitEquityIndexDividendFutureSecurity(EquityIndexDividendFutureSecurity security) {
        return getSecurity(security.getUnderlyingId());
      }

      @Override
      public Security visitFXFutureSecurity(FXFutureSecurity security) {
        return null;
      }

      @Override
      public Security visitIndexFutureSecurity(IndexFutureSecurity security) {
        return getSecurity(security.getUnderlyingId());
      }

      @Override
      public Security visitInterestRateFutureSecurity(InterestRateFutureSecurity security) {
        return getSecurity(security.getUnderlyingId());
      }

      @Override
      public Security visitMetalFutureSecurity(MetalFutureSecurity security) {
        return getSecurity(security.getUnderlyingId());
      }

      @Override
      public Security visitStockFutureSecurity(StockFutureSecurity security) {
        return getSecurity(security.getUnderlyingId());
      }
      
    });
  }

  /**
   * FutureSecurityTypeVisitor
   */
  private static class FutureSecurityTypeVisitor implements FutureSecurityVisitor<String> {

    @Override
    public String visitAgricultureFutureSecurity(AgricultureFutureSecurity security) {
      return "AgricultureFuture";
    }

    @Override
    public String visitBondFutureSecurity(BondFutureSecurity security) {
      return "BondFuture";
    }

    @Override
    public String visitEnergyFutureSecurity(EnergyFutureSecurity security) {
      return "EnergyFuture";
    }

    @Override
    public String visitEquityFutureSecurity(EquityFutureSecurity security) {
      return "EquityFuture";
    }

    @Override
    public String visitEquityIndexDividendFutureSecurity(EquityIndexDividendFutureSecurity security) {
      return "EquityIndexDividendFuture";
    }

    @Override
    public String visitFXFutureSecurity(FXFutureSecurity security) {
      return "FxFuture";
    }

    @Override
    public String visitIndexFutureSecurity(IndexFutureSecurity security) {
      return "IndexFuture";
    }

    @Override
    public String visitInterestRateFutureSecurity(InterestRateFutureSecurity security) {
      return "InterestRate";
    }

    @Override
    public String visitMetalFutureSecurity(MetalFutureSecurity security) {
      return "MetalFuture";
    }

    @Override
    public String visitStockFutureSecurity(StockFutureSecurity security) {
      return "StockFuture";
    }

  }
  
  /**
   * SwapLegClassifierVisitor
   */
  private static class SwapLegClassifierVisitor implements SwapLegVisitor<String> {
    @Override
    public String visitFixedInterestRateLeg(FixedInterestRateLeg swapLeg) {
      return "FixedInterestRateLeg";
    }

    @Override
    public String visitFloatingInterestRateLeg(FloatingInterestRateLeg swapLeg) {
      return "FloatingInterestRateLeg";
    }

    @Override
    public String visitFloatingSpreadIRLeg(FloatingSpreadIRLeg swapLeg) {
      return "FloatingSpreadInterestRateLeg";
    }

    @Override
    public String visitFloatingGearingIRLeg(FloatingGearingIRLeg swapLeg) {
      return "FloatingGearingInterestRateLeg";
    }
  }
  
}
