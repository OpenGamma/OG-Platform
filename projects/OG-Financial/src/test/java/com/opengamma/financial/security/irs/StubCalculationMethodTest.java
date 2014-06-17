package com.opengamma.financial.security.irs;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;

import com.opengamma.financial.convention.StubType;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalScheme;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.Tenor;

@Test(groups = TestGroup.UNIT)
public class StubCalculationMethodTest {

  String USD_LIBOR1M = "1MLIBOR";
  String USD_LIBOR2M = "2MLIBOR";
  String USD_LIBOR3M = "3MLIBOR";
  
  ExternalId _firstStubStartReferenceRateId = ExternalId.of(ExternalScheme.of("CONVENTION"), USD_LIBOR1M);
  ExternalId _firstStubEndReferenceRateId = ExternalId.of(ExternalScheme.of("CONVENTION"), USD_LIBOR3M);
  ExternalId _secondStubStartIndexId = ExternalId.of(ExternalScheme.of("CONVENTION"), USD_LIBOR2M);
  ExternalId secondStubEndIndexId = _firstStubEndReferenceRateId;
  
  @Test(expectedExceptions = IllegalArgumentException.class)
  private void shortStartStubIllegalDefnTest() {
    
    StubCalculationMethod.Builder stubBuilder = StubCalculationMethod.builder()
        .type(StubType.SHORT_START)
        .firstStubStartIndex(Tenor.ONE_MONTH)
        .firstStubEndIndex(Tenor.THREE_MONTHS);
    
    StubCalculationMethod stub = stubBuilder.build();
    stub.validate();
  }
  
  @Test(expectedExceptions = IllegalArgumentException.class)
  private void longStartStubIllegalDefnTest() {
    
    StubCalculationMethod.Builder stubBuilder = StubCalculationMethod.builder()
        .type(StubType.LONG_START)
        .firstStubStartIndex(Tenor.THREE_MONTHS)
        .firstStubEndIndex(Tenor.SIX_MONTHS);
    
    StubCalculationMethod stub = stubBuilder.build();
    stub.validate();
  }
  
  @Test(expectedExceptions = IllegalArgumentException.class)
  private void shortEndStubIllegalDefnTest() {
    
    StubCalculationMethod.Builder stubBuilder = StubCalculationMethod.builder()
        .type(StubType.SHORT_END)
        .firstStubStartIndex(Tenor.ONE_WEEK);
    
    StubCalculationMethod stub = stubBuilder.build();
    stub.validate();
  }
  
  @Test(expectedExceptions = IllegalArgumentException.class)
  private void longEndStubIllegalDefnTest() {
    
    StubCalculationMethod.Builder stubBuilder = StubCalculationMethod.builder()
        .firstStubEndIndex(Tenor.THREE_MONTHS);
    
    StubCalculationMethod stub = stubBuilder.build();
    stub.validate();
  }
  
  @Test
  private void shortStubBuilderValidationTest() {
    
    StubCalculationMethod.Builder stubBuilder = StubCalculationMethod.builder()
        .type(StubType.SHORT_START)
        .firstStubStartIndex(Tenor.ONE_MONTH)
        .firstStubStartReferenceRateId(_firstStubStartReferenceRateId)
        .firstStubEndIndex(Tenor.THREE_MONTHS)
        .firstStubEndReferenceRateId(_firstStubEndReferenceRateId);
    StubCalculationMethod stub = stubBuilder.build();
    stub.validate();
  }
  
  @Test
  private void shortStubBuilderValidationTest2() {
    
    StubCalculationMethod.Builder stubBuilder = StubCalculationMethod.builder()
        .type(StubType.BOTH)
        .firstStubEndDate(LocalDate.of(2014, 06, 18))
        .firstStubStartIndex(Tenor.ONE_MONTH)
        .firstStubStartReferenceRateId(_firstStubStartReferenceRateId)
        .firstStubEndIndex(Tenor.THREE_MONTHS)
        .firstStubEndReferenceRateId(_firstStubEndReferenceRateId)
        .lastStubEndDate(LocalDate.of(2016, 06, 18))
        .lastStubStartIndex(Tenor.TWO_MONTHS)
        .lastStubStartReferenceRateId(_secondStubStartIndexId)
        .lastStubEndIndex(Tenor.THREE_MONTHS)
        .lastStubEndReferenceRateId(secondStubEndIndexId);
    StubCalculationMethod stub = stubBuilder.build();
    stub.validate();
  }
}
