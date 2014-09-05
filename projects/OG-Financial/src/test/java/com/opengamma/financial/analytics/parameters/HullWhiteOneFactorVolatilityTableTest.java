package com.opengamma.financial.analytics.parameters;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import java.util.Comparator;
import java.util.SortedSet;
import java.util.TreeSet;

import org.testng.annotations.Test;

import com.google.common.collect.ImmutableSortedSet;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.Tenor;

/**
 * Check that the table of entries is correctly validated.
 */
@Test(groups = TestGroup.UNIT)
public class HullWhiteOneFactorVolatilityTableTest {

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void tableWithNoEntriesIsNotValid() {
    HullWhiteOneFactorVolatilityTable.builder()
        .entries(ImmutableSortedSet.<HullWhiteOneFactorVolatilityEntry>of())
        .build();
  }

  // NPE is expected in this case as it is the Guava immutable
  // collections throwing within the Joda-beans convert
  @Test(expectedExceptions = NullPointerException.class)
  public void nullEntryIsNotValid() {

    // We have to override the comparator as the one defined for
    // HullWhiteOneFactorVolatilityEntry doesn't support nulls
    SortedSet<HullWhiteOneFactorVolatilityEntry> entries = new TreeSet<>(
        new Comparator<HullWhiteOneFactorVolatilityEntry>() {
          @Override
          public int compare(HullWhiteOneFactorVolatilityEntry o1, HullWhiteOneFactorVolatilityEntry o2) {
            return 0;
          }
        });
    entries.add(null);

    HullWhiteOneFactorVolatilityTable.builder()
        .entries(entries)
        .build();
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void singleEntryWithEndIsNotValid() {

    SortedSet<HullWhiteOneFactorVolatilityEntry> entries = ImmutableSortedSet.of(
        HullWhiteOneFactorVolatilityEntry.builder()
            .startTenor(Tenor.ofDays(0))
            .endTenor(Tenor.ofDays(10))
            .volatility(1.0)
            .build());

    HullWhiteOneFactorVolatilityTable.builder()
        .entries(entries)
        .build();
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void tableWithOverlappingEntriesIsNotValid() {

    SortedSet<HullWhiteOneFactorVolatilityEntry> entries = ImmutableSortedSet.of(
        HullWhiteOneFactorVolatilityEntry.builder()
            .startTenor(Tenor.ofDays(0))
            .endTenor(Tenor.ofDays(5))
            .volatility(1.0)
            .build(),
        HullWhiteOneFactorVolatilityEntry.builder()
            .startTenor(Tenor.ofDays(3))
            .endTenor(Tenor.ofDays(10))
            .volatility(2.0)
            .build());

    HullWhiteOneFactorVolatilityTable.builder()
        .entries(entries)
        .build();
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void tableWithDisjointEntriesIsNotValid() {

    SortedSet<HullWhiteOneFactorVolatilityEntry> entries = ImmutableSortedSet.of(
        HullWhiteOneFactorVolatilityEntry.builder()
            .startTenor(Tenor.ofDays(0))
            .endTenor(Tenor.ofDays(5))
            .volatility(1.0)
            .build(),
        HullWhiteOneFactorVolatilityEntry.builder()
            .startTenor(Tenor.ofDays(7))
            .endTenor(Tenor.ofDays(10))
            .volatility(2.0)
            .build());

    HullWhiteOneFactorVolatilityTable.builder()
        .entries(entries)
        .build();
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void tableWithNonZeroStartIsNotValid() {

    SortedSet<HullWhiteOneFactorVolatilityEntry> entries = ImmutableSortedSet.of(
        HullWhiteOneFactorVolatilityEntry.builder()
            .startTenor(Tenor.ofDays(1))
            .endTenor(Tenor.ofDays(5))
            .volatility(1.0)
            .build(),
        HullWhiteOneFactorVolatilityEntry.builder()
            .startTenor(Tenor.ofDays(5))
            .volatility(2.0)
            .build());

    HullWhiteOneFactorVolatilityTable.builder()
        .entries(entries)
        .build();
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void tableWithFixedEndIsNotValid() {

    SortedSet<HullWhiteOneFactorVolatilityEntry> entries = ImmutableSortedSet.of(
        HullWhiteOneFactorVolatilityEntry.builder()
            .startTenor(Tenor.ofDays(0))
            .endTenor(Tenor.ofDays(5))
            .volatility(1.0)
            .build(),
        HullWhiteOneFactorVolatilityEntry.builder()
            .startTenor(Tenor.ofDays(5))
            .endTenor(Tenor.ofDays(10))
            .volatility(2.0)
            .build());

    HullWhiteOneFactorVolatilityTable.builder()
        .entries(entries)
        .build();
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void tableWithEmptyEndTenorInMiddleIsNotValid() {

    SortedSet<HullWhiteOneFactorVolatilityEntry> entries = ImmutableSortedSet.of(
        HullWhiteOneFactorVolatilityEntry.builder()
            .startTenor(Tenor.ofDays(0))
            .volatility(1.0)
            .build(),
        HullWhiteOneFactorVolatilityEntry.builder()
            .startTenor(Tenor.ofDays(5))
            .endTenor(Tenor.ofDays(10))
            .volatility(2.0)
            .build());

    HullWhiteOneFactorVolatilityTable.builder()
        .entries(entries)
        .build();
  }

  @Test
  public void singleEntryIsValid() {

    SortedSet<HullWhiteOneFactorVolatilityEntry> entries = ImmutableSortedSet.of(
        HullWhiteOneFactorVolatilityEntry.builder()
            .startTenor(Tenor.ofDays(0))
            .volatility(1.0)
            .build());

    HullWhiteOneFactorVolatilityTable table = HullWhiteOneFactorVolatilityTable.builder()
        .entries(entries)
        .build();

    assertThat(table.getEntries().size(), is(1));
  }

  @Test
  public void multipleEntriesAreValid() {

    SortedSet<HullWhiteOneFactorVolatilityEntry> entries = ImmutableSortedSet.of(
        HullWhiteOneFactorVolatilityEntry.builder()
            .startTenor(Tenor.ofDays(0))
            .endTenor(Tenor.SIX_MONTHS)
            .volatility(1.0)
            .build(),
        HullWhiteOneFactorVolatilityEntry.builder()
            .startTenor(Tenor.SIX_MONTHS)
            .endTenor(Tenor.ONE_YEAR)
            .volatility(2.0)
            .build(),
        HullWhiteOneFactorVolatilityEntry.builder()
            .startTenor(Tenor.ONE_YEAR)
            .endTenor(Tenor.THREE_YEARS)
            .volatility(1.0)
            .build(),
        HullWhiteOneFactorVolatilityEntry.builder()
            .startTenor(Tenor.THREE_YEARS)
            .endTenor(Tenor.TEN_YEARS)
            .volatility(2.0)
            .build(),
        HullWhiteOneFactorVolatilityEntry.builder()
            .startTenor(Tenor.TEN_YEARS)
            .volatility(1.0)
            .build());

    HullWhiteOneFactorVolatilityTable table = HullWhiteOneFactorVolatilityTable.builder()
        .entries(entries)
        .build();

    assertThat(table.getEntries().size(), is(5));
  }

}
