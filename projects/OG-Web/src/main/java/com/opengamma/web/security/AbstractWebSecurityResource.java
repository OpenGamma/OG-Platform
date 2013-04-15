/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.security;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

import org.fudgemsg.FudgeMsgEnvelope;
import org.joda.beans.impl.flexi.FlexiBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.core.historicaltimeseries.HistoricalTimeSeries;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.sensitivities.FactorExposureData;
import com.opengamma.financial.sensitivities.SecurityEntryData;
import com.opengamma.id.ExternalId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoSearchRequest;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoSearchResult;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesMaster;
import com.opengamma.master.orgs.OrganizationMaster;
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
   * HTML ftl directory
   */
  protected static final String HTML_DIR = "securities/html/";
  /**
   * JSON ftl directory
   */
  protected static final String JSON_DIR = "securities/json/";

  /**
   * The backing bean.
   */
  private final WebSecuritiesData _data;
  /**
   * The template name provider
   */
  private final SecurityTemplateNameProvider _templateNameProvider = new SecurityTemplateNameProvider();

  /**
   * Creates the resource.
   * @param securityMaster  the security master, not null
   * @param securityLoader  the security loader, not null
   * @param htsMaster  the historical time series master, not null
   * @param organizationMaster the organization master, not null
   */
  protected AbstractWebSecurityResource(final SecurityMaster securityMaster, final SecurityLoader securityLoader, final HistoricalTimeSeriesMaster htsMaster, 
      final OrganizationMaster organizationMaster) {
    ArgumentChecker.notNull(securityMaster, "securityMaster");
    ArgumentChecker.notNull(securityLoader, "securityLoader");
    ArgumentChecker.notNull(htsMaster, "htsMaster");
    ArgumentChecker.notNull(organizationMaster, "organizationMaster");
    _data = new WebSecuritiesData();
    data().setSecurityMaster(securityMaster);
    data().setSecurityLoader(securityLoader);
    data().setHistoricalTimeSeriesMaster(htsMaster);
    data().setOrganizationMaster(organizationMaster);
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
   * @return the backing bean, not null
   */
  protected WebSecuritiesData data() {
    return _data;
  }
  
  /**
   * Gets the security template provider
   * @return the template provider, not null
   */
  protected SecurityTemplateNameProvider getTemplateProvider() {
    return _templateNameProvider;
  }

  protected void addSecuritySpecificMetaData(ManageableSecurity security, FlexiBean out) {
    if (security instanceof FinancialSecurity) {
      FinancialSecurity financialSec = (FinancialSecurity) security;
      financialSec.accept(new SecurityTemplateModelObjectBuilder(out, data().getSecurityMaster(), data().getOrganizationMaster()));
    } else {
      if (security.getSecurityType().equals(SecurityEntryData.EXTERNAL_SENSITIVITIES_SECURITY_TYPE)) {
        RawSecurity rawSecurity = (RawSecurity) security;
        FudgeMsgEnvelope msg = OpenGammaFudgeContext.getInstance().deserialize(rawSecurity.getRawData());
        SecurityEntryData securityEntryData = OpenGammaFudgeContext.getInstance().fromFudgeMsg(SecurityEntryData.class, msg.getMessage());

        out.put("securityEntryData", securityEntryData);
        RawSecurity underlyingRawSecurity = (RawSecurity) getSecurity(securityEntryData.getFactorSetId(), data().getSecurityMaster());
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
  }

  private List<FactorExposure> convertToFactorExposure(List<FactorExposureData> factorExposureDataList) {
    List<FactorExposure> results = new ArrayList<FactorExposure>();
    for (FactorExposureData exposure : factorExposureDataList) {
      HistoricalTimeSeriesInfoSearchRequest exposureSearchRequest = new HistoricalTimeSeriesInfoSearchRequest();
      exposureSearchRequest.addExternalId(exposure.getExposureExternalId());
      exposureSearchRequest.setDataField("EXPOSURE");
      HistoricalTimeSeriesInfoSearchResult exposureSearchResult = data().getHistoricalTimeSeriesMaster().search(exposureSearchRequest);
      HistoricalTimeSeries exposureHTS = null;
      if (exposureSearchResult.getFirstInfo() != null) {
        exposureHTS = data().getHistoricalTimeSeriesMaster().getTimeSeries(exposureSearchResult.getFirstInfo().getTimeSeriesObjectId(), VersionCorrection.LATEST);
      }

      HistoricalTimeSeriesInfoSearchRequest convexitySearchRequest = new HistoricalTimeSeriesInfoSearchRequest();
      convexitySearchRequest.addExternalId(exposure.getExposureExternalId());
      convexitySearchRequest.setDataField("CONVEXITY");
      HistoricalTimeSeriesInfoSearchResult convexitySearchResult = data().getHistoricalTimeSeriesMaster().search(convexitySearchRequest);
      HistoricalTimeSeries convexityHTS = null;
      if (convexitySearchResult.getFirstInfo() != null) {
        convexityHTS = data().getHistoricalTimeSeriesMaster().getTimeSeries(convexitySearchResult.getFirstInfo().getTimeSeriesObjectId(), VersionCorrection.LATEST);
      }

      HistoricalTimeSeriesInfoSearchRequest priceSearchRequest = new HistoricalTimeSeriesInfoSearchRequest();
      priceSearchRequest.addExternalId(exposure.getExposureExternalId());
      priceSearchRequest.setDataField("PX_LAST");
      HistoricalTimeSeriesInfoSearchResult priceSearchResult = data().getHistoricalTimeSeriesMaster().search(priceSearchRequest);
      HistoricalTimeSeries priceHTS = null;
      if (priceSearchResult.getFirstInfo() != null) {
        priceHTS = data().getHistoricalTimeSeriesMaster().getTimeSeries(priceSearchResult.getFirstInfo().getTimeSeriesObjectId(), VersionCorrection.LATEST);
      }

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

  public static ManageableSecurity getSecurity(final ExternalId underlyingIdentifier, final SecurityMaster securityMaster) {
    if (underlyingIdentifier == null) {
      return null;
    }
    SecuritySearchRequest request = new SecuritySearchRequest();
    request.addExternalId(underlyingIdentifier);
    SecuritySearchResult search = securityMaster.search(request);
    return search.getFirstSecurity();
  }
    
}
