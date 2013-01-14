/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.sensitivities;

import java.util.List;

import org.fudgemsg.FudgeMsgEnvelope;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.master.security.RawSecurity;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;

/**
 * Utilities for raw securities
 */
public class RawSecurityUtils {
  
  /**
   * @param security The security
   * @return true if security is externally provided sensitivities security
   */
  public static boolean isExternallyProvidedSensitivitiesSecurity(Security security) {
    return (security instanceof RawSecurity) && security.getSecurityType().equals(SecurityEntryData.EXTERNAL_SENSITIVITIES_SECURITY_TYPE);
  }
  
  public static boolean isExternallyProvidedSensitivitiesFactorSetSecurity(Security security) {
    return (security instanceof RawSecurity) && security.getSecurityType().equals(FactorExposureData.EXTERNAL_SENSITIVITIES_RISK_FACTORS_SECURITY_TYPE);
  }
  
  public static SecurityEntryData decodeSecurityEntryData(RawSecurity rawSecurity) {
    FudgeMsgEnvelope msg = OpenGammaFudgeContext.getInstance().deserialize(rawSecurity.getRawData());
    SecurityEntryData securityEntryData = OpenGammaFudgeContext.getInstance().fromFudgeMsg(SecurityEntryData.class, msg.getMessage());
    return securityEntryData;
  }
  
  public static List<FactorExposureData> decodeFactorExposureData(SecuritySource secSource, RawSecurity rawSecurity) {
    FudgeMsgEnvelope msg = OpenGammaFudgeContext.getInstance().deserialize(rawSecurity.getRawData());
    SecurityEntryData securityEntryData = OpenGammaFudgeContext.getInstance().fromFudgeMsg(SecurityEntryData.class, msg.getMessage());
    RawSecurity underlyingRawSecurity = (RawSecurity) secSource.getSingle(securityEntryData.getFactorSetId().toBundle());
    if (underlyingRawSecurity != null) {
      FudgeMsgEnvelope factorIdMsg = OpenGammaFudgeContext.getInstance().deserialize(underlyingRawSecurity.getRawData());
      @SuppressWarnings("unchecked")
      List<FactorExposureData> factorExposureDataList = OpenGammaFudgeContext.getInstance().fromFudgeMsg(List.class, factorIdMsg.getMessage());     
      return factorExposureDataList;
    } else {
      throw new OpenGammaRuntimeException("Couldn't find factor list security " + securityEntryData.getFactorSetId());
    }
  }
}
