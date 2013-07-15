/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg.referencedata.impl;

import static com.opengamma.bbg.BloombergConstants.BLOOMBERG_FIELDS_REQUEST;
import static com.opengamma.bbg.BloombergConstants.BLOOMBERG_REFERENCE_DATA_REQUEST;
import static com.opengamma.bbg.BloombergConstants.BLOOMBERG_SECURITIES_REQUEST;
import static com.opengamma.bbg.BloombergConstants.EID_DATA;
import static com.opengamma.bbg.BloombergConstants.ERROR_INFO;
import static com.opengamma.bbg.BloombergConstants.FIELD_DATA;
import static com.opengamma.bbg.BloombergConstants.FIELD_EXCEPTIONS;
import static com.opengamma.bbg.BloombergConstants.FIELD_ID;
import static com.opengamma.bbg.BloombergConstants.RESPONSE_ERROR;
import static com.opengamma.bbg.BloombergConstants.SECURITY;
import static com.opengamma.bbg.BloombergConstants.SECURITY_DATA;
import static com.opengamma.bbg.BloombergConstants.SECURITY_ERROR;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.BlockingQueue;

import org.apache.commons.lang.StringUtils;
import org.fudgemsg.FudgeMsg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.MessageFormatter;
import org.springframework.context.Lifecycle;

import com.bloomberglp.blpapi.CorrelationID;
import com.bloomberglp.blpapi.Element;
import com.bloomberglp.blpapi.Request;
import com.google.common.base.CharMatcher;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.bbg.AbstractBloombergStaticDataProvider;
import com.opengamma.bbg.BloombergConnector;
import com.opengamma.bbg.BloombergConstants;
import com.opengamma.bbg.referencedata.ReferenceData;
import com.opengamma.bbg.referencedata.ReferenceDataError;
import com.opengamma.bbg.referencedata.ReferenceDataProviderGetRequest;
import com.opengamma.bbg.referencedata.ReferenceDataProviderGetResult;
import com.opengamma.bbg.referencedata.statistics.BloombergReferenceDataStatistics;
import com.opengamma.bbg.util.BloombergDataUtils;
import com.opengamma.util.ArgumentChecker;

/**
 * Provider of reference-data from the Bloomberg data source.
 */
public class BloombergReferenceDataProvider extends AbstractReferenceDataProvider implements Lifecycle {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(BloombergReferenceDataProvider.class);

  /**
   * Implementation class.
   */
  private final BloombergReferenceDataProviderImpl _impl;

  /**
   * Creates an instance.
   * <p>
   * This will use the statistics tool in the connector.
   * 
   * @param bloombergConnector  the Bloomberg connector, not null
   */
  public BloombergReferenceDataProvider(BloombergConnector bloombergConnector) {
    this(bloombergConnector, bloombergConnector.getReferenceDataStatistics());
  }

  /**
   * Creates an instance with statistics gathering.
   * 
   * @param bloombergConnector  the Bloomberg connector, not null
   * @param statistics  the statistics to collect, not null
   */
  public BloombergReferenceDataProvider(BloombergConnector bloombergConnector, BloombergReferenceDataStatistics statistics) {
    _impl = new BloombergReferenceDataProviderImpl(bloombergConnector, statistics);
  }

  //-------------------------------------------------------------------------
  @Override
  protected ReferenceDataProviderGetResult doBulkGet(ReferenceDataProviderGetRequest request) {
    return _impl.doBulkGet(request);
  }

  //-------------------------------------------------------------------------
  @Override
  public void start() {
    _impl.start();
  }

  @Override
  public void stop() {
    _impl.stop();
  }

  @Override
  public boolean isRunning() {
    return _impl.isRunning();
  }

  //-------------------------------------------------------------------------
  /**
   * Loads reference-data from Bloomberg.
   */
  static class BloombergReferenceDataProviderImpl extends AbstractBloombergStaticDataProvider {

    /**
     * Bloomberg statistics.
     */
    private final BloombergReferenceDataStatistics _statistics;

    /**
     * Creates an instance.
     * 
     * @param provider  the provider, not null
     */
    public BloombergReferenceDataProviderImpl(BloombergConnector bloombergConnector, BloombergReferenceDataStatistics statistics) {
      super(bloombergConnector, BloombergConstants.REF_DATA_SVC_NAME);
      ArgumentChecker.notNull(statistics, "statistics");
      _statistics = statistics;
    }

    //-------------------------------------------------------------------------
    @Override
    protected Logger getLogger() {
      return s_logger;
    }

    //-------------------------------------------------------------------------
    /**
     * Get reference-data from Bloomberg.
     * 
     * @param request  the request, not null
     * @return the reference-data result, not null
     */
    ReferenceDataProviderGetResult doBulkGet(ReferenceDataProviderGetRequest request) {
      Set<String> identifiers = request.getIdentifiers();
      Set<String> dataFields = request.getFields();
      validateIdentifiers(identifiers);
      validateFields(dataFields);
      
      ensureStarted();
      s_logger.debug("Getting reference data for {}, fields {}", identifiers, dataFields);
      
      Request bbgRequest = createRequest(identifiers, dataFields);
      _statistics.recordStatistics(identifiers, dataFields);
      BlockingQueue<Element> resultElements = callBloomberg(bbgRequest);
      if (resultElements == null || resultElements.isEmpty()) {
        throw new OpenGammaRuntimeException("Unable to get a Bloomberg response for " + dataFields + " fields for " + identifiers);
      }
      return parse(identifiers, dataFields, resultElements);
    }

    //-------------------------------------------------------------------------
    /**
     * Checks that all the identifiers are valid.
     * 
     * @param identifiers  the set of identifiers, not null
     */
    private void validateIdentifiers(Set<String> identifiers) {
      Set<String> excluded = new HashSet<String>();
      for (String identifier : identifiers) {
        if (StringUtils.isEmpty(identifier)) {
          throw new IllegalArgumentException("Must not have any null or empty identifiers");
        }
        if (CharMatcher.ASCII.matchesAllOf(identifier) == false) {
          //[BBG-93] - The C++ interface is declared as UChar, so this just enforces that restriction   
          excluded.add(identifier);
        }
      }
      if (excluded.size() > 0) {
        String message = MessageFormatter.format("Request contains invalid identifiers {} from ({})", excluded, identifiers).getMessage();
        s_logger.error(message);
        throw new OpenGammaRuntimeException(message);
      }
    }

    /**
     * Checks that all the fields are valid.
     * 
     * @param fields  the set of fields, not null
     */
    private void validateFields(Set<String> fields) {
      Set<String> excluded = new HashSet<String>();
      for (String field : fields) {
        if (StringUtils.isEmpty(field)) {
          throw new IllegalArgumentException("Must not have any null or empty fields");
        }
        if (CharMatcher.ASCII.matchesAllOf(field) == false) {
          excluded.add(field);
        }
      }
      if (excluded.size() > 0) {
        String message = MessageFormatter.format("Request contains invalid fields {} from ({})", excluded, fields).getMessage();
        s_logger.error(message);
        throw new OpenGammaRuntimeException(message);
      }
    }

    //-------------------------------------------------------------------------
    /**
     * Creates the Bloomberg request.
     */
    private Request createRequest(Set<String> identifiers, Set<String> dataFields) {
      // create request
      Request request = getService().createRequest(BLOOMBERG_REFERENCE_DATA_REQUEST);
      Element securitiesElem = request.getElement(BLOOMBERG_SECURITIES_REQUEST);
      
      // identifiers
      for (String identifier : identifiers) {
        if (StringUtils.isEmpty(identifier)) {
          throw new IllegalArgumentException("Must not have any null or empty securities");
        }
        securitiesElem.appendValue(identifier);
      }
      
      // fields
      Element fieldElem = request.getElement(BLOOMBERG_FIELDS_REQUEST);
      for (String dataField : dataFields) {
        if (StringUtils.isEmpty(dataField)) {
          throw new IllegalArgumentException("Must not have any null or empty fields");
        }
        if (dataField.equals(BloombergConstants.FIELD_EID_DATA) == false) {
          fieldElem.appendValue(dataField);
        }
      }
      if (dataFields.contains(BloombergConstants.FIELD_EID_DATA)) {
        request.set("returnEids", true);
      }
      return request;
    }

    //-------------------------------------------------------------------------
    /**
     * Call Bloomberg.
     * 
     * @param request  the request, not null
     * @return the response, may be null
     */
    private BlockingQueue<Element> callBloomberg(Request request) {
      CorrelationID cid = submitBloombergRequest(request);
      return getResultElement(cid);
    }

    //-------------------------------------------------------------------------
    /**
     * Performs the main work to parse the result from Bloomberg.
     * <p>
     * This is part of {@link #getFields(Set, Set)}.
     * 
     * @param securityKeys  the set of securities, not null
     * @param fields  the set of fields, not null
     * @param resultElements  the result elements from Bloomberg, not null
     * @return the parsed result, not null
     */
    protected ReferenceDataProviderGetResult parse(Set<String> securityKeys, Set<String> fields, BlockingQueue<Element> resultElements) {
      ReferenceDataProviderGetResult result = new ReferenceDataProviderGetResult();
      for (Element resultElem : resultElements) {
        if (resultElem.hasElement(RESPONSE_ERROR)) {
          Element responseError = resultElem.getElement(RESPONSE_ERROR);
          String category = responseError.getElementAsString(BloombergConstants.CATEGORY);
          if ("LIMIT".equals(category)) {
            s_logger.error("Limit reached {}", responseError);
          }
          throw new OpenGammaRuntimeException("Unable to get a Bloomberg response for " + fields + " fields for " + securityKeys + ": " + responseError);
        }
        
        Element securityDataArray = resultElem.getElement(SECURITY_DATA);
        int numSecurities = securityDataArray.numValues();
        for (int iSecurityElem = 0; iSecurityElem < numSecurities; iSecurityElem++) {
          Element securityElem = securityDataArray.getValueAsElement(iSecurityElem);
          String securityKey = securityElem.getElementAsString(SECURITY);
          ReferenceData refData = new ReferenceData(securityKey);
          if (securityElem.hasElement(SECURITY_ERROR)) {
            parseIdentifierError(refData, securityElem.getElement(SECURITY_ERROR));
          }
          if (securityElem.hasElement(FIELD_DATA)) {
            parseFieldData(refData, securityElem.getElement(FIELD_DATA));
          }
          if (securityElem.hasElement(FIELD_EXCEPTIONS)) {
            parseFieldExceptions(refData, securityElem.getElement(FIELD_EXCEPTIONS));
          }
          if (securityElem.hasElement(EID_DATA)) {
            parseEidData(refData, securityElem.getElement(FIELD_DATA));
          }
          result.addReferenceData(refData);
        }
      }
      return result;
    }

    /**
     * Processes an error affecting the whole identifier.
     * 
     * @param refData  the per identifier reference data result, not null
     * @param element  the bloomberg element, not null
     */
    protected void parseIdentifierError(ReferenceData refData, Element element) {
      ReferenceDataError error = buildError(null, element);
      refData.addError(error);
    }

    /**
     * Processes the field data.
     * 
     * @param refData  the per identifier reference data result, not null
     * @param element  the bloomberg element, not null
     */
    protected void parseFieldData(ReferenceData refData, Element element) {
      FudgeMsg fieldData = BloombergDataUtils.parseElement(element);
      refData.setFieldValues(fieldData);
    }

    /**
     * Processes the an error affecting a single field on a one identifier.
     * 
     * @param refData  the per identifier reference data result, not null
     * @param fieldExceptionArray  the bloomberg data, not null
     */
    protected void parseFieldExceptions(ReferenceData refData, Element fieldExceptionArray) {
      int numExceptions = fieldExceptionArray.numValues();
      for (int i = 0; i < numExceptions; i++) {
        Element exceptionElem = fieldExceptionArray.getValueAsElement(i);
        String fieldId = exceptionElem.getElementAsString(FIELD_ID);
        ReferenceDataError error = buildError(fieldId, exceptionElem.getElement(ERROR_INFO));
        refData.addError(error);
      }
    }

    /**
     * Processes the EID data.
     * 
     * @param refData  the per identifier reference data result, not null
     * @param element  the bloomberg element, not null
     */
    protected void parseEidData(ReferenceData refData, Element element) {
      refData.setEntitlementInfo(element);
    }

    /**
     * Creates an instance from a Bloomberg element.
     * 
     * @param field  the field, null if linked to the identifier rather than a field
     * @param element  the element, not null
     * @return the error, not null
     */
    public ReferenceDataError buildError(String field, Element element) {
      return new ReferenceDataError(field, element.getElementAsInt32("code"), element.getElementAsString("category"),
          element.getElementAsString("subcategory"), element.getElementAsString("message"));
    }
  }

}
