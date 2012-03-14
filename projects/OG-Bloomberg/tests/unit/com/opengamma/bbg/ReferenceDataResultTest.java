package com.opengamma.bbg;

import static org.testng.AssertJUnit.assertEquals;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.testng.annotations.Test;

import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;

public class ReferenceDataResultTest {

  @Test
  public void roundTrip()
  {
    FudgeContext context = OpenGammaFudgeContext.getInstance();
    
    ReferenceDataResult referenceDataResult = new ReferenceDataResult();
    PerSecurityReferenceDataResult result = new PerSecurityReferenceDataResult("SomeSec");
    result.addFieldException("SomeField", new ErrorInfo(1, "Cat", "Sub", "Message"));
    MutableFudgeMsg fieldData = context.newMessage();
    fieldData.add("SomeOtherField", FudgeContext.EMPTY_MESSAGE);
    result.setFieldData(fieldData);
    referenceDataResult.addResult(result);
    
    FudgeMsg fudgeMsg = referenceDataResult.toFudgeMsg(context);
    ReferenceDataResult roundTripped = ReferenceDataResult.fromFudgeMsg(fudgeMsg, context);
    
    assertEquals(1, roundTripped.getSecurities().size());
    PerSecurityReferenceDataResult result2 = roundTripped.getResult("SomeSec");
    assertEquals(1, result2.getFieldExceptions().size());
    assertEquals(1, result2.getFieldExceptions().get("SomeField").getCode());
    assertEquals(result.getFieldData().toString(), result2.getFieldData().toString());
  }
}
