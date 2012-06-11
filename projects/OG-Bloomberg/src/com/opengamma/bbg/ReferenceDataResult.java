/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.lang.StringUtils;
import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.FudgeMsgEnvelope;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.wire.types.FudgeWireType;

import com.opengamma.util.ArgumentChecker;

/**
 * Holder for the reference data results for a set of securities.
 */
public class ReferenceDataResult {

  /** Fudge field name. */
  public static final String PER_SECURITY_FIELD_NAME = "PerSecurityReferenceDataResult";
  /** Fudge field name. */
  public static final String SECURITY_FIELD_NAME = "security";
  /** Fudge field name. */
  public static final String FIELD_DATA_FIELD_NAME = "fields";
  /** Fudge field name. */
  public static final String FIELD_EXCEPTIONS_FIELD_NAME = "fieldExceptions";
  /** Fudge field name. */
  public static final String EXCEPTIONS_FIELD_NAME = "exceptions";

  /**
   * Results keyed by security.
   */
  private final Map<String, PerSecurityReferenceDataResult> _resultsBySecurity = new TreeMap<String, PerSecurityReferenceDataResult>();

  /**
   * Creates an instance.
   */
  public ReferenceDataResult() {
  }

  //-------------------------------------------------------------------------
  /**
   * Add a single result to the stored set.
   * 
   * @param result  the result, not null
   */
  public void addResult(PerSecurityReferenceDataResult result) {
    ArgumentChecker.notNull(result, "Per Security Reference Data Result");
    _resultsBySecurity.put(result.getSecurity(), result);
  }

  /**
   * Gets the set of securities stored in this result.
   * 
   * @return the set of security keys, not null
   */
  public Set<String> getSecurities() {
    return Collections.unmodifiableSet(_resultsBySecurity.keySet());
  }

  /**
   * Retrieves a single result by security key.
   * 
   * @param securityKey  the security key, not null
   * @return the result, null if not found
   */
  public PerSecurityReferenceDataResult getResult(String securityKey) {
    return _resultsBySecurity.get(securityKey);
  }

  //-------------------------------------------------------------------------
  /**
   * Converts to a Fudge message.
   * 
   * @param fudgeContext  the Fudge context, not null
   * @return the Fudge message, not null
   */
  public FudgeMsg toFudgeMsg(FudgeContext fudgeContext) {
    ArgumentChecker.notNull(fudgeContext, "FudgeContext");
    MutableFudgeMsg msg = fudgeContext.newMessage();
    Set<String> securities = getSecurities();
    if (securities == null || securities.isEmpty()) {
      return msg;
    }
    for (String sec : securities) {
      PerSecurityReferenceDataResult result = getResult(sec);
      if (result != null) {
        MutableFudgeMsg subMsg = fudgeContext.newMessage();
        String security = result.getSecurity();
        subMsg.add(SECURITY_FIELD_NAME, security);
        FudgeMsg fieldData = result.getFieldData();
        subMsg.add(FIELD_DATA_FIELD_NAME, fieldData);
        List<String> exceptions = result.getExceptions();
        if (exceptions != null && !exceptions.isEmpty()) {
          for (String exception : exceptions) {
            subMsg.add(EXCEPTIONS_FIELD_NAME, exception);
          }
        }
        
        FudgeMsgEnvelope mapMsg = fudgeContext.toFudgeMsg(result.getFieldExceptions());
        subMsg.add(FIELD_EXCEPTIONS_FIELD_NAME, mapMsg.getMessage());
        msg.add(PER_SECURITY_FIELD_NAME, subMsg);
      }
    }
    return msg;
  }

  /**
   * Converts from a Fudge message.
   * 
   * @param msg  the message, not null
   * @param fudgeContext  the Fudge context, not null
   * @return the result object, not null
   */
  public static ReferenceDataResult fromFudgeMsg(FudgeMsg msg, FudgeContext fudgeContext) {
    ArgumentChecker.notNull(msg, "FudgeMsg");
    ArgumentChecker.notNull(fudgeContext, "fudgeContext");
    List<FudgeField> perSecMsgs = msg.getAllByName(PER_SECURITY_FIELD_NAME);
    ReferenceDataResult refDataResult = new ReferenceDataResult();
    for (FudgeField perSecFields : perSecMsgs) {
      if (perSecFields.getType().equals(FudgeWireType.SUB_MESSAGE)) {
        FudgeMsg perSecMsg = (FudgeMsg) perSecFields.getValue();
        FudgeField secfield = perSecMsg.getByName(SECURITY_FIELD_NAME);
        if (secfield.getType().equals(FudgeWireType.STRING)) {
          String secName = (String) secfield.getValue();
          //now I have a valid security
          if (!StringUtils.isEmpty(secName)) {
            PerSecurityReferenceDataResult perSecurity = new PerSecurityReferenceDataResult(secName);
            
            FudgeField fField = perSecMsg.getByName(FIELD_DATA_FIELD_NAME);
            if (fField.getType().equals(FudgeWireType.SUB_MESSAGE)) {
              FudgeMsg fieldData = (FudgeMsg) fField.getValue();
              perSecurity.setFieldData(fieldData);
              List<FudgeField> exceptions = perSecMsg.getAllByName(EXCEPTIONS_FIELD_NAME);
              for (FudgeField exception : exceptions) {
                if (exception.getType().equals(FudgeWireType.STRING)) {
                  perSecurity.getExceptions().add((String) exception.getValue());
                }
              }
            }
            
            FudgeMsg feMessage = perSecMsg.getMessage(FIELD_EXCEPTIONS_FIELD_NAME);
            if (feMessage != null) {
              @SuppressWarnings("unchecked")
              Map<String, ErrorInfo> map = (Map<String, ErrorInfo>) fudgeContext.fromFudgeMsg(feMessage);
              for (Entry<String, ErrorInfo> entry : map.entrySet()) {
                perSecurity.addFieldException(entry.getKey(), entry.getValue());
              }
            }
            refDataResult.addResult(perSecurity);
          }
        }
      }
    }
    return refDataResult;
  }

}
