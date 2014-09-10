package com.opengamma.financial.analytics.parameters;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;

import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.Tenor;

/**
 * Simple tests for HullWhiteOneFactorVolatilityEntry.
 */
@Test(groups = TestGroup.UNIT)
public class HullWhiteOneFactorVolatilityEntryTest {

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void startTenorMustBeBeforeEnd() {
    HullWhiteOneFactorVolatilityEntry.builder()
        .startTenor(Tenor.ofDays(10))
        .endTenor(Tenor.ofDays(5))
        .volatility(1.0)
        .build();
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void startTenorCannotBeSameAsEnd() {
    HullWhiteOneFactorVolatilityEntry.builder()
        .startTenor(Tenor.ofDays(10))
        .endTenor(Tenor.ofDays(10))
        .volatility(1.0)
        .build();
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void startVolatilityCannotBeNegative() {
    HullWhiteOneFactorVolatilityEntry.builder()
        .startTenor(Tenor.ofDays(5))
        .endTenor(Tenor.ofDays(10))
        .volatility(-1.0)
        .build();
  }

  @Test
  public void tenorsInCorrectOrderAreSuccessful() {
    HullWhiteOneFactorVolatilityEntry entry = HullWhiteOneFactorVolatilityEntry.builder()
        .startTenor(Tenor.ofDays(5))
        .endTenor(Tenor.ofDays(10))
        .volatility(1.0)
        .build();
    assertThat(entry.getStartTenor().toFormattedString(), is("P5D"));
    assertThat(entry.getEndTenor().toFormattedString(), is("P10D"));
    assertThat(entry.getVolatility(), is(1.0));
  }

  @Test
  public void startTenorCanBeZero() {
    HullWhiteOneFactorVolatilityEntry entry = HullWhiteOneFactorVolatilityEntry.builder()
        .startTenor(Tenor.ofDays(0))
        .endTenor(Tenor.ofDays(10))
        .volatility(1.0)
        .build();
    assertThat(entry.getStartTenor().toFormattedString(), is("P0D"));
  }

  @Test
  public void volatilityCanBeZero() {
    HullWhiteOneFactorVolatilityEntry entry = HullWhiteOneFactorVolatilityEntry.builder()
        .startTenor(Tenor.ofDays(0))
        .endTenor(Tenor.ofDays(10))
        .volatility(0)
        .build();
    assertThat(entry.getVolatility(), is(0.0));
  }


  @Test
  public void endTenorCanBeUnspecified() {
    HullWhiteOneFactorVolatilityEntry entry = HullWhiteOneFactorVolatilityEntry.builder()
        .startTenor(Tenor.ofDays(0))
        .volatility(1.0)
        .build();
    assertThat(entry.getEndTenor(), is(nullValue()));
  }

  @Test
  public void startTenorsAreUsedForComparison() {

    HullWhiteOneFactorVolatilityEntry entry1 = HullWhiteOneFactorVolatilityEntry.builder()
        .startTenor(Tenor.ofDays(0))
        .endTenor(Tenor.ofDays(10))
        .volatility(1.0)
        .build();

    HullWhiteOneFactorVolatilityEntry entry2 = HullWhiteOneFactorVolatilityEntry.builder()
        .startTenor(Tenor.ofDays(5))
        .endTenor(Tenor.ofDays(10))
        .volatility(2.0)
        .build();

    assertThat(entry1.compareTo(entry2), is(-1));
  }

  @Test
  public void endTenorsAreUsedForComparisonIfStartsEqual() {

    HullWhiteOneFactorVolatilityEntry entry1 = HullWhiteOneFactorVolatilityEntry.builder()
        .startTenor(Tenor.ofDays(0))
        .endTenor(Tenor.ofDays(5))
        .volatility(1.0)
        .build();

    HullWhiteOneFactorVolatilityEntry entry2 = HullWhiteOneFactorVolatilityEntry.builder()
        .startTenor(Tenor.ofDays(0))
        .endTenor(Tenor.ofDays(10))
        .volatility(2.0)
        .build();

    assertThat(entry1.compareTo(entry2), is(-1));
  }

  @Test
  public void nullEndTenorIsOrderedLastIfStartsEqual() {

    HullWhiteOneFactorVolatilityEntry entry1 = HullWhiteOneFactorVolatilityEntry.builder()
        .startTenor(Tenor.ofDays(0))
        .endTenor(Tenor.ofDays(5))
        .volatility(1.0)
        .build();

    HullWhiteOneFactorVolatilityEntry entry2 = HullWhiteOneFactorVolatilityEntry.builder()
        .startTenor(Tenor.ofDays(0))
        .volatility(2.0)
        .build();

    assertThat(entry1.compareTo(entry2), is(-1));
  }

  @Test
  public void compareSameIfStartAndEndSame() {

    HullWhiteOneFactorVolatilityEntry entry1 = HullWhiteOneFactorVolatilityEntry.builder()
        .startTenor(Tenor.ofDays(0))
        .endTenor(Tenor.ofDays(5))
        .volatility(1.0)
        .build();

    HullWhiteOneFactorVolatilityEntry entry2 = HullWhiteOneFactorVolatilityEntry.builder()
        .startTenor(Tenor.ofDays(0))
        .endTenor(Tenor.ofDays(5))
        .volatility(2.0)
        .build();

    assertThat(entry1.compareTo(entry2), is(0));
  }

  @Test
  public void compareSameIfStartAndEndNull() {

    HullWhiteOneFactorVolatilityEntry entry1 = HullWhiteOneFactorVolatilityEntry.builder()
        .startTenor(Tenor.ofDays(0))
        .volatility(1.0)
        .build();

    HullWhiteOneFactorVolatilityEntry entry2 = HullWhiteOneFactorVolatilityEntry.builder()
        .startTenor(Tenor.ofDays(0))
        .volatility(2.0)
        .build();

    assertThat(entry1.compareTo(entry2), is(0));
  }



}
