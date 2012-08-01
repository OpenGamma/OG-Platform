/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg;

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

import java.text.MessageFormat;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.BlockingQueue;

import org.apache.commons.lang.StringUtils;
import org.fudgemsg.FudgeMsg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.MessageFormatter;

import com.bloomberglp.blpapi.CorrelationID;
import com.bloomberglp.blpapi.Element;
import com.bloomberglp.blpapi.Request;
import com.bloomberglp.blpapi.Service;
import com.google.common.base.CharMatcher;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.bbg.referencedata.statistics.BloombergReferenceDataStatistics;
import com.opengamma.bbg.util.BloombergDataUtils;
import com.opengamma.util.ArgumentChecker;

/**
 * An implemention of {@link ReferenceDataProvider} that makes requests directly against
 * the Bloomberg Server API.
 */
public class BloombergReferenceDataProvider extends AbstractBloombergStaticDataProvider implements ReferenceDataProvider {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(BloombergReferenceDataProvider.class);
  /**
   * The error message format.
   */
  private static final String ERROR_MESSAGE_FORMAT = "{0}:{1}/{2} - {3}";

  /**
   * The Bloomberg service.
   */
  private Service _refDataService;
  /**
   * The statistics for Bloomberg access.
   */
  private final BloombergReferenceDataStatistics _statistics;

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
    super(bloombergConnector);
    ArgumentChecker.notNull(statistics, "statistics");
    _statistics = statistics;
  }

  //-------------------------------------------------------------------------
  @Override
  protected Logger getLogger() {
    return s_logger;
  }

  @Override
  protected void openServices() {
    _refDataService = openService(BloombergConstants.REF_DATA_SVC_NAME);
  }

  /**
   * Gets the Bloomberg reference data service.
   * 
   * @return the service, not null once started
   */
  protected Service getRefDataService() {
    return _refDataService;
  }

  //-------------------------------------------------------------------------
  @Override
  public ReferenceDataResult getFields(Set<String> securityKeys, Set<String> fields) {
    doValidate(securityKeys, fields);
    BlockingQueue<Element> resultElements = doQuery(securityKeys, fields);
    return doParse(securityKeys, fields, resultElements);
  }

  //-------------------------------------------------------------------------
  /**
   * Performs the main work to validate the input.
   * <p>
   * This is part of {@link #getFields(Set, Set)}.
   * 
   * @param securityKeys  the set of securities, not null
   * @param fields  the set of fields, not null
   */
  protected void doValidate(Set<String> securityKeys, Set<String> fields) {
    ArgumentChecker.notEmpty(securityKeys, "securityKeys");
    ArgumentChecker.notEmpty(fields, "fields");
    validateSecurities(securityKeys);
    
    ensureStarted();
    _statistics.recordStatistics(securityKeys, fields);
    s_logger.info("Requesting fields {} for securities {}", fields, securityKeys);
  }

  /**
   * Checks that all the securities are valid.
   * 
   * @param securityKeys  the set of securities, not null
   */
  protected void validateSecurities(Set<String> securityKeys) {
    Set<String> excluded = new HashSet<String>();
    for (String securityKey : securityKeys) {
      if (StringUtils.isEmpty(securityKey)) {
        throw new IllegalArgumentException("Must not have any null or empty securities");
      }
      if (CharMatcher.ASCII.matchesAllOf(securityKey) == false) {
        //[BBG-93] - The C++ interface is declared as UChar, so this just enforces that restriction   
        excluded.add(securityKey);
      }
    }
    if (excluded.size() > 0) {
      //TODO - should we allow the rest of the request to continue? 
      String message = MessageFormatter.format("Request contains invalid securities {} from ({})", excluded, securityKeys);
      s_logger.error(message);
      throw new OpenGammaRuntimeException(message);
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Performs the main work to query Bloomberg.
   * <p>
   * This is part of {@link #getFields(Set, Set)}.
   * 
   * @param securityKeys  the set of securities, not null
   * @param fields  the set of fields, not null
   * @return the Bloomberg result, not null
   */
  protected BlockingQueue<Element> doQuery(Set<String> securityKeys, Set<String> fields) {
    Request request = composeRequest(securityKeys, fields);
    CorrelationID cid = submitBloombergRequest(request);
    BlockingQueue<Element> resultElements = getResultElement(cid);
    if (resultElements == null || resultElements.isEmpty()) {
      throw new OpenGammaRuntimeException("Unable to get a Bloomberg response for " + fields + " fields for " + securityKeys);
    }
    return resultElements;
  }

  /**
   * Composes the request to Bloomberg.
   * 
   * @param securityKeys  the set of bloomberg security keys, not null
   * @param fields  the set of bloomberg fields, not null
   * @return the bloomberg request, not null
   */
  protected Request composeRequest(Set<String> securityKeys, Set<String> fields) {
    Request request = getRefDataService().createRequest(BLOOMBERG_REFERENCE_DATA_REQUEST);
    Element securitiesElem = request.getElement(BLOOMBERG_SECURITIES_REQUEST);
    for (String securityKey : securityKeys) {
      if (StringUtils.isEmpty(securityKey)) {
        throw new IllegalArgumentException("Must not have any null or empty securities");
      }
      securitiesElem.appendValue(securityKey);
    }
    Element fieldElem = request.getElement(BLOOMBERG_FIELDS_REQUEST);
    for (String field : fields) {
      if (StringUtils.isEmpty(field)) {
        throw new IllegalArgumentException("Must not have any null or empty field mnemonics");
      }
      if (!field.equals(BloombergConstants.FIELD_EID_DATA)) {
        fieldElem.appendValue(field);
      }
    }
    
    if (fields.contains(BloombergConstants.FIELD_EID_DATA)) {
      request.set("returnEids", true);
    }
    return request;
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
  protected ReferenceDataResult doParse(Set<String> securityKeys, Set<String> fields, BlockingQueue<Element> resultElements) {
    ReferenceDataResult result = new ReferenceDataResult();
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
        PerSecurityReferenceDataResult perSecResult = new PerSecurityReferenceDataResult(securityKey);
        if (securityElem.hasElement(SECURITY_ERROR)) {
          parseSecurityError(perSecResult, securityElem.getElement(SECURITY_ERROR));
        }
        if (securityElem.hasElement(FIELD_DATA)) {
          parseFieldData(perSecResult, securityElem.getElement(FIELD_DATA));
        }
        if (securityElem.hasElement(FIELD_EXCEPTIONS)) {
          parseFieldExceptions(perSecResult, securityElem.getElement(FIELD_EXCEPTIONS));
        }
        if (securityElem.hasElement(EID_DATA)) {
          parseEidData(perSecResult, securityElem.getElement(FIELD_DATA));
        }
        result.addResult(perSecResult);
      }
    }
    return result;
  }

  /**
   * Processes a security error.
   * 
   * @param perSecResult  the per security reference data result, not null
   * @param element  the bloomberg element, not null
   */
  protected void parseSecurityError(PerSecurityReferenceDataResult perSecResult, Element element) {
    ErrorInfo error = new ErrorInfo(element);
    String errorMessage = MessageFormat.format(ERROR_MESSAGE_FORMAT, error.getCode(), error.getCategory(), error.getSubcategory(), error.getMessage());
    perSecResult.addException(errorMessage);
  }

  /**
   * Processes the field data.
   * 
   * @param perSecResult  the per security reference data result, not null
   * @param element  the bloomberg element, not null
   */
  protected void parseFieldData(PerSecurityReferenceDataResult perSecResult, Element element) {
    FudgeMsg fieldData = BloombergDataUtils.parseElement(element);
    perSecResult.setFieldData(fieldData);
  }

  /**
   * Processes the exceptions.
   * 
   * @param perSecResult  the per security reference data result, not null
   * @param fieldExceptionArray  the bloomberg data, not null
   */
  protected void parseFieldExceptions(PerSecurityReferenceDataResult perSecResult, Element fieldExceptionArray) {
    int numExceptions = fieldExceptionArray.numValues();
    for (int i = 0; i < numExceptions; i++) {
      Element exceptionElem = fieldExceptionArray.getValueAsElement(i);
      String fieldId = exceptionElem.getElementAsString(FIELD_ID);
      ErrorInfo errorInfo = new ErrorInfo(exceptionElem.getElement(ERROR_INFO));
      perSecResult.addFieldException(fieldId, errorInfo);
    }
  }

  /**
   * Processes the EID data.
   * 
   * @param perSecResult  the per security reference data result, not null
   * @param element  the bloomberg element, not null
   */
  protected void parseEidData(PerSecurityReferenceDataResult perSecResult, Element element) {
    perSecResult.setEidData(element);
  }

}
