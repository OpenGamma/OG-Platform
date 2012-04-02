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
import com.bloomberglp.blpapi.SessionOptions;
import com.google.common.base.CharMatcher;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.bbg.referencedata.statistics.BloombergReferenceDataStatistics;
import com.opengamma.bbg.referencedata.statistics.NullBloombergReferenceDataStatistics;
import com.opengamma.bbg.util.BloombergDataUtils;
import com.opengamma.util.ArgumentChecker;

/**
 * An implemention of {@link ReferenceDataProvider} that makes requests directly against
 * the Bloomberg Server API.
 */
public class BloombergReferenceDataProvider extends AbstractBloombergStaticDataProvider implements ReferenceDataProvider {
  
  private static final Logger s_logger = LoggerFactory.getLogger(BloombergReferenceDataProvider.class);
  private static final String ERROR_MESSAGE_FORMAT = "{0}:{1}/{2} - {3}";
  
  private final BloombergReferenceDataStatistics _statistics;
  private Service _refDataService;
  
  /**
   * @param sessionOptions the bloomberg session options
   */
  public BloombergReferenceDataProvider(SessionOptions sessionOptions) {
    this(sessionOptions, NullBloombergReferenceDataStatistics.INSTANCE);
  }
  
  /**
   * @param sessionOptions the bloomberg session options
   * @param statistics the statistics to record
   */
  public BloombergReferenceDataProvider(SessionOptions sessionOptions, BloombergReferenceDataStatistics statistics) {
    super(sessionOptions);
    _statistics = statistics;
  }
  
  @Override
  protected Logger getLogger() {
    return s_logger;
  }
  
  @Override
  protected void openServices() {
    Service refDataService = openService(BloombergConstants.REF_DATA_SVC_NAME);
    setRefDataService(refDataService);
  }
  
  private void setRefDataService(Service refDataService) {
    _refDataService = refDataService;
  }
  
  private Service getRefDataService() {
    return _refDataService;
  }
    
  @Override
  public ReferenceDataResult getFields(Set<String> securities,
      Set<String> fields) {
    ArgumentChecker.notEmpty(securities, "Securities");
    ArgumentChecker.notEmpty(fields, "Field Names");
    checkAllSecuritiesValid(securities);
    
    ensureStarted();
    _statistics.gotFields(securities, fields);
    s_logger.info("Requesting fields {} for securities {}", fields, securities);
    Request request = composeRequest(securities, fields);
    CorrelationID cid = submitBloombergRequest(request);
    BlockingQueue<Element> resultElements = getResultElement(cid);
    
    if (resultElements == null || resultElements.isEmpty()) {
      throw new OpenGammaRuntimeException("Unable to get a Bloomberg response for " + fields + " fields for " + securities);
    }
    
    ReferenceDataResult result = new ReferenceDataResult();
    for (Element resultElem : resultElements) {
      if (resultElem.hasElement(RESPONSE_ERROR)) {
        Element responseError = resultElem.getElement(RESPONSE_ERROR);
        
        String category = responseError.getElementAsString(BloombergConstants.CATEGORY);
        if ("LIMIT".equals(category)) {
          s_logger.error("Limit reached {}", responseError);
        }
        throw new OpenGammaRuntimeException("Unable to get a Bloomberg response for " + fields + " fields for " + securities + ": " + responseError);
      }
      
      Element securityDataArray = resultElem.getElement(SECURITY_DATA);
      int numSecurities = securityDataArray.numValues();
      for (int iSecurityElem = 0; iSecurityElem < numSecurities; iSecurityElem++) {
        Element securityElem = securityDataArray.getValueAsElement(iSecurityElem);
        String security = securityElem.getElementAsString(SECURITY);
        PerSecurityReferenceDataResult perSecResult = new PerSecurityReferenceDataResult(security);
        if (securityElem.hasElement(SECURITY_ERROR)) {
          processSecurityError(perSecResult, securityElem.getElement(SECURITY_ERROR));
        }
        if (securityElem.hasElement(FIELD_DATA)) {
          processFieldData(perSecResult, securityElem.getElement(FIELD_DATA));
        }
        if (securityElem.hasElement(FIELD_EXCEPTIONS)) {
          processFieldExceptions(perSecResult, securityElem.getElement(FIELD_EXCEPTIONS));
        }
        if (securityElem.hasElement(EID_DATA)) {
          processEidData(perSecResult, securityElem.getElement(FIELD_DATA));
        }
        
        result.addResult(perSecResult);
      }
    }
    
    
    return result;
  }

  private void checkAllSecuritiesValid(Set<String> securities) {
    Set<String> excluded = new HashSet<String>();
    for (String security : securities) {
      if (StringUtils.isEmpty(security)) {
        throw new IllegalArgumentException("Must not have any null or empty securities");
      }
      if (!CharMatcher.ASCII.matchesAllOf(security)) {
        //[BBG-93] - The C++ interface is declared as UChar, so this just enforces that restriction   
        excluded.add(security);
      }
    }
    if (!excluded.isEmpty()) {
      //TODO - should we allow the rest of the request to continue? 
      String message = MessageFormatter.format("Request contains invalid securities {} from ({})", excluded, securities);
      s_logger.error(message);
      throw new OpenGammaRuntimeException(message);
    }
  }

  /**
   * @param perSecResult the persecurity reference data result 
   * @param element the bloomberg element
   */
  protected void processFieldData(PerSecurityReferenceDataResult perSecResult,
      Element element) {
    FudgeMsg fieldData = BloombergDataUtils.parseElement(element);
    perSecResult.setFieldData(fieldData);
  }
  
  private void processFieldExceptions(PerSecurityReferenceDataResult perSecResult, Element fieldExceptionArray) {
    int numExceptions = fieldExceptionArray.numValues();
    for (int i = 0; i < numExceptions; i++) {
      Element exceptionElem = fieldExceptionArray.getValueAsElement(i);
      String fieldId = exceptionElem.getElementAsString(FIELD_ID);
      ErrorInfo errorInfo = new ErrorInfo(exceptionElem.getElement(ERROR_INFO));
      perSecResult.addFieldException(fieldId, errorInfo);
    }
  }
  
  private void processEidData(PerSecurityReferenceDataResult perSecResult,
      Element element) {
    perSecResult.setEidData(element);
  }
  
  /**
   * @param securities the set of bloomberg security keys
   * @param fields the set of bloomberg fields
   * @return bloomberg request
   */
  protected Request composeRequest(Set<String> securities, Set<String> fields) {
    Request request = getRefDataService().createRequest(BLOOMBERG_REFERENCE_DATA_REQUEST);
    Element securitiesElem = request.getElement(BLOOMBERG_SECURITIES_REQUEST);
    for (String security : securities) {
      if (StringUtils.isEmpty(security)) {
        throw new IllegalArgumentException("Must not have any null or empty securities");
      }
      securitiesElem.appendValue(security);
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
  
  /**
   * @param perSecResult the persecurity reference data result
   * @param element the bloomberg element
   */
  protected void processSecurityError(PerSecurityReferenceDataResult perSecResult, Element element) {
    ErrorInfo error = new ErrorInfo(element);
    String errorMessage = MessageFormat.format(ERROR_MESSAGE_FORMAT, error.getCode(), error.getCategory(), error.getSubcategory(), error.getMessage());
    perSecResult.getExceptions().add(errorMessage);
  }
}
