package com.opengamma.financial.sensitivities;

import org.fudgemsg.FudgeMsgEnvelope;

import com.opengamma.core.security.Security;
import com.opengamma.master.security.RawSecurity;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;

public class RawSecurityUtils {
  /**
   * returns true if security is externally provided sensitivities security
   */
  public static boolean isExternallyProvidedSensitivitiesSecurity(Security security) {
    return security.getSecurityType().equals(SecurityEntryData.EXTERNAL_SENSITIVITIES_SECURITY_TYPE);
  }
  
  public static boolean isExternallyProvidedSensitivitiesFactorSetSecurity(Security security) {
    return security.getSecurityType().equals(FactorExposureData.EXTERNAL_SENSITIVITIES_RISK_FACTORS_SECURITY_TYPE);
  }
  
  public static SecurityEntryData deserialize(RawSecurity rawSecurity) {
    FudgeMsgEnvelope msg = OpenGammaFudgeContext.getInstance().deserialize(rawSecurity.getRawData());
    SecurityEntryData securityEntryData = OpenGammaFudgeContext.getInstance().fromFudgeMsg(SecurityEntryData.class, msg.getMessage());
    return securityEntryData;
  }
}
