/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.security;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

import org.fudgemsg.FudgeMsgEnvelope;
import org.joda.beans.impl.flexi.FlexiBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.core.historicaltimeseries.HistoricalTimeSeries;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSource;
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
import com.opengamma.financial.security.option.EquityBarrierOptionSecurity;
import com.opengamma.financial.security.option.EquityIndexOptionSecurity;
import com.opengamma.financial.security.option.EquityOptionSecurity;
import com.opengamma.financial.security.option.IRFutureOptionSecurity;
import com.opengamma.financial.security.option.SwaptionSecurity;
import com.opengamma.financial.security.swap.FixedInterestRateLeg;
import com.opengamma.financial.security.swap.FixedVarianceSwapLeg;
import com.opengamma.financial.security.swap.FloatingGearingIRLeg;
import com.opengamma.financial.security.swap.FloatingInterestRateLeg;
import com.opengamma.financial.security.swap.FloatingSpreadIRLeg;
import com.opengamma.financial.security.swap.FloatingVarianceSwapLeg;
import com.opengamma.financial.security.swap.SwapLegVisitor;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.financial.sensitivities.FactorExposureData;
import com.opengamma.financial.sensitivities.SecurityEntryData;
import com.opengamma.id.ExternalId;
import com.opengamma.id.UniqueId;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.master.security.RawSecurity;
import com.opengamma.master.security.SecurityLoader;
import com.opengamma.master.security.SecurityMaster;
import com.opengamma.master.security.SecuritySearchRequest;
import com.opengamma.master.security.SecuritySearchResult;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;
import com.opengamma.web.AbstractPerRequestWebResource;
import com.opengamma.web.WebHomeUris;

/**
 * Abstract base class for RESTful security resources.
 */
public abstract class AbstractWebSecurityResource extends AbstractPerRequestWebResource {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(AbstractWebSecurityResource.class);

  /**
   * The backing bean.
   */
  private final WebSecuritiesData _data;

  /**
   * Creates the resource.
   * @param securityMaster  the security master, not null
   * @param securityLoader  the security loader, not null
   * @param htsSource  the historical time series source, not null
   */
  protected AbstractWebSecurityResource(final SecurityMaster securityMaster, final SecurityLoader securityLoader, final HistoricalTimeSeriesSource htsSource) {
    ArgumentChecker.notNull(securityMaster, "securityMaster");
    ArgumentChecker.notNull(securityLoader, "securityLoader");
    ArgumentChecker.notNull(htsSource, "htsSource");
    _data = new WebSecuritiesData();
    data().setSecurityMaster(securityMaster);
    data().setSecurityLoader(securityLoader);    
    data().setHistoricalTimeSeriesSource(htsSource);
  }

  /**
   * Creates the resource.
   * @param parent  the parent resource, not null
   */
  protected AbstractWebSecurityResource(final AbstractWebSecurityResource parent) {
    super(parent);
    _data = parent._data;
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
    if (security.getSecurityType().equals(EquityIndexOptionSecurity.SECURITY_TYPE)) {
      EquityIndexOptionSecurity equityIndxOption = (EquityIndexOptionSecurity) security;
      addUnderlyingSecurity(out, equityIndxOption.getUnderlyingId());
    }
    if (security.getSecurityType().equals(FRASecurity.SECURITY_TYPE)) {
      FRASecurity fraSecurity = (FRASecurity) security;
      addUnderlyingSecurity(out, fraSecurity.getUnderlyingId());
    }
    if (security.getSecurityType().equals(SecurityEntryData.EXTERNAL_SENSITIVITIES_SECURITY_TYPE)) {
      RawSecurity rawSecurity = (RawSecurity) security;
      FudgeMsgEnvelope msg = OpenGammaFudgeContext.getInstance().deserialize(rawSecurity.getRawData());
      SecurityEntryData securityEntryData = OpenGammaFudgeContext.getInstance().fromFudgeMsg(SecurityEntryData.class, msg.getMessage());

      out.put("securityEntryData", securityEntryData);
      out.put("securityAttributes", security.getAttributes());
      RawSecurity underlyingRawSecurity = (RawSecurity) getSecurity(securityEntryData.getFactorSetId());
      if (underlyingRawSecurity != null) {
        FudgeMsgEnvelope factorIdMsg = OpenGammaFudgeContext.getInstance().deserialize(underlyingRawSecurity.getRawData());
        @SuppressWarnings("unchecked")
        List<FactorExposureData> factorExposureDataList = OpenGammaFudgeContext.getInstance().fromFudgeMsg(List.class, factorIdMsg.getMessage());
        s_logger.error(factorExposureDataList.toString());
        List<FactorExposure> factorExposuresList = convertToFactorExposure(factorExposureDataList);
        out.put("factorExposuresList", factorExposuresList);
      } else {
        s_logger.error("Couldn't find security");
      }
      
    }
    if (security.getSecurityType().equals(FactorExposureData.EXTERNAL_SENSITIVITIES_RISK_FACTORS_SECURITY_TYPE)) {
      RawSecurity rawSecurity = (RawSecurity) security;
      FudgeMsgEnvelope msg = OpenGammaFudgeContext.getInstance().deserialize(rawSecurity.getRawData());
      @SuppressWarnings("unchecked")
      List<FactorExposureData> factorExposureDataList = OpenGammaFudgeContext.getInstance().fromFudgeMsg(List.class, msg.getMessage());
      List<FactorExposure> factorExposuresList = convertToFactorExposure(factorExposureDataList);
      out.put("factorExposuresList", factorExposuresList);
    }
  }
  
  private List<FactorExposure> convertToFactorExposure(List<FactorExposureData> factorExposureDataList) {
    List<FactorExposure> results = new ArrayList<FactorExposure>();
    for (FactorExposureData exposure : factorExposureDataList) {
      HistoricalTimeSeries exposureHTS = data().getHistoricalTimeSeriesSource().getHistoricalTimeSeries("EXPOSURE", exposure.getExposureExternalId().toBundle(), null);
      HistoricalTimeSeries convexityHTS = data().getHistoricalTimeSeriesSource().getHistoricalTimeSeries("CONVEXITY", exposure.getExposureExternalId().toBundle(), null);
      HistoricalTimeSeries priceHTS = data().getHistoricalTimeSeriesSource().getHistoricalTimeSeries("PX_LAST", exposure.getFactorExternalId().toBundle(), null);
      results.add(new FactorExposure(exposure.getFactorType().getFactorType(),
                                     exposure.getFactorName(),
                                     exposure.getNode(),
                                     priceHTS != null ? priceHTS.getUniqueId() : null,
                                     priceHTS != null ? priceHTS.getTimeSeries().getLatestValue() : null,
                                     exposureHTS != null ? exposureHTS.getUniqueId() : null,
                                     exposureHTS != null ? exposureHTS.getTimeSeries().getLatestValue() : null,
                                     convexityHTS != null ? convexityHTS.getUniqueId() : null,
                                     convexityHTS != null ? convexityHTS.getTimeSeries().getLatestValue() : null));
    }
    return results;
  }
  
  /**
   * Container for a row of a displayed factor.
   */
  public class FactorExposure {
    private final String _factorType;
    private final String _factorName;
    private final String _node;
    private final UniqueId _priceTsId;
    private final Double _lastPrice;
    private final UniqueId _exposureTsId;
    private final Double _lastExposure;
    private final UniqueId _convexityTsId;
    private final Double _lastConvexity;
    
    public FactorExposure(String factorType, String factorName, String node, 
                          UniqueId priceTsId, Double lastPrice, 
                          UniqueId exposureTsId, Double lastExposure,
                          UniqueId convexityTsId, Double lastConvexity) {
      _factorType = factorType;
      _factorName = factorName;
      _node = node;
      _priceTsId = priceTsId;
      _lastPrice = lastPrice;
      _exposureTsId = exposureTsId;
      _lastExposure = lastExposure;
      _convexityTsId = convexityTsId;
      _lastConvexity = lastConvexity;
    }
    
    public String getFactorType() {
      return _factorType;
    }
    
    public String getFactorName() {
      return _factorName;
    }
    
    public String getNode() {
      return _node;
    }
    
    public UniqueId getPriceTsId() {
      return _priceTsId;
    }
    
    public Double getLastPrice() {
      return _lastPrice;
    }
    
    public UniqueId getExposureTsId() {
      return _exposureTsId;
    }
    
    public Double getLastExposure() {
      return _lastExposure;
    }
    
    public UniqueId getConvexityTsId() {
      return _convexityTsId;
    }
    
    public Double getLastConvexity() {
      return _lastConvexity;
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

  private Map<String, String> getBondFutureBasket(ManageableSecurity security) {
    Map<String, String> result = new TreeMap<String, String>();
    if (security instanceof BondFutureSecurity) {
      BondFutureSecurity bondFutureSecurity = (BondFutureSecurity) security;
      List<BondFutureDeliverable> basket = bondFutureSecurity.getBasket();
      for (BondFutureDeliverable bondFutureDeliverable : basket) {
        String identifierValue = bondFutureDeliverable.getIdentifiers().getValue(SecurityUtils.BLOOMBERG_BUID);
        result.put(SecurityUtils.BLOOMBERG_BUID.getName() + "-" + identifierValue, String.valueOf(bondFutureDeliverable.getConversionFactor()));
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

    @Override
    public String visitFixedVarianceSwapLeg(FixedVarianceSwapLeg swapLeg) {
      return "FixedVarianceLeg";
    }

    @Override
    public String visitFloatingVarianceSwapLeg(FloatingVarianceSwapLeg swapLeg) {
      return "FloatingVarianceLeg";
    }
  }
  
}
