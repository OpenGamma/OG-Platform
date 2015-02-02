/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.bbg.util;


import static org.threeten.bp.DayOfWeek.FRIDAY;

import java.util.ArrayList;
import java.util.List;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeSet;

import org.threeten.bp.LocalDate;
import org.threeten.bp.ZonedDateTime;

import com.google.common.base.Objects;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.opengamma.financial.convention.daycount.ActualActualISDA;
import com.opengamma.financial.security.option.OptionType;
import com.opengamma.id.ExternalId;
import com.opengamma.util.OpenGammaClock;

/**
 * <p>
 * Class for narrowing an option chain by expiry, strike, and option type.  Typically, this class is initialized with the output
 * of {@link com.opengamma.bbg.util.BloombergDataUtils#getOptionChain(org.slf4j.Logger, com.opengamma.bbg.ReferenceDataProvider, String) BloombergDataUtil.getOptionChain()}.
 * The caller then uses the different {@code narrow*} methods to select the desired option or options.  In this way, the caller can pick
 * very specific options before looking up their information in Bloomberg.
 * </p>
 * <p>
 * Each instance of {@code BloombergEQVanillaOptionChain} is immutable. Each of the {@code narrow*} methods returns a new immutable instance
 * of {@code BloombergEQVanillaOptionChain}. The chain is a wrapper around a list of {@link com.opengamma.id.ExternalId Identifiers}, which 
 * are obtainable via a getter method after narrowing is complete. This getter method returns the {@code Identifiers} as a {@code Set}, in order
 * to mimic the behavior of {@code BloombergDataUtil.getOptionChain()}. Different instances of the chain may share instances of the contained 
 * {@code Identifiers}, but these are themselves immutable, so this is completely safe.
 * </p>
 * <p>
 * The above setup works well with a method-chaining idiom. For example, here is how you would obtain a single call option that expires 2
 * or more months from the present, and is within 1 strike of the current price:
 * </p>
 * <pre>
   double currentPrice = . . . ;
   Set&lt;Identifier&gt; identifiers = BloombergDataUtil.getOptionChain( . . . .);
   BloombergEQVanillaOptionChain chain = new BloombergEQVanillaOptionChain (identifiers);
    
   LocalDate today = LocalDate.now();
   Identifier desiredOption = chain.narrowByExpiry(today, 2).narrowByStrike(currentPrice, 1).
     narrowByOptionType(OptionType.CALL).getIdentifiers().first();
 * </pre>
 * <p>
 * To get both the put and call options, one could reuse a chain instance, like so:
 * </p>
 * <pre>
   double currentPrice = . . . ;
   Set&lt;Identifier&gt; identifiers = BloombergDataUtil.getOptionChain( . . . .);
   BloombergEQVanillaOptionChain chain = new BloombergEQVanillaOptionChain (identifiers);
    
   LocalDate today = LocalDate.now();
   BloombergEQVanillaOptionChain optionPair = chain.narrowByExpiry(today, 2).narrowByStrike(currentPrice, 1);
   
   Identifier desiredCall = optionPair.narrowByOptionType(OptionType.CALL).getIdentifiers().first();
   Identifier desiredPut  = optionPair.narrowByOptionType(OptionType.PUT).getIdentifiers().first();
 * </pre>
 * <p>
 * The narrow methods are <b>guaranteed</b> to return a chain with at least one element, <b>unless</b> the
 * chain was built around an empty {@code Identifier} set to begin with, in which case the methods will return
 * an empty, non-null chain.
 * </p>
 * Internally, this class uses {@link BloombergTickerParserEQVanillaOption}. 
 */
public class BloombergEQVanillaOptionChain {
  // ------------ FIELDS ------------
  private List<ExternalId> _identifiers;
  private ActualActualISDA _dayCount = new ActualActualISDA();

  
  
  // ------------ METHODS ------------
  // -------- CONSTRUCTORS --------
  /**
   * <p>
   * Create an option chain around a {@link java.util.Set} of identifiers. Typically, these come from the result of 
   * a call to {@link com.opengamma.bbg.util.BloombergDataUtils#getOptionChain BloombergDataUtil.getOptionChain()}.
   * </p>
   * <p>
   * The identifiers must have an {@link com.opengamma.id.ExternalScheme} of {@link com.opengamma.core.id.ExternalSchemes#BLOOMBERG_TICKER}. 
   * </p>
   * @param identifiers the identifiers that comprise the chain
   */
  public BloombergEQVanillaOptionChain(Set<ExternalId> identifiers) {
    _identifiers = new ArrayList<ExternalId>(identifiers);
  }
  
  /**
   * <p>
   * Create an option chain around a {@link java.util.List} of identifiers. Typically, these come from the result of 
   * a call to {@link com.opengamma.bbg.util.BloombergDataUtils#getOptionChain BloombergDataUtil.getOptionChain()}.
   * </p>
   * <p>
   * The identifiers must have an {@link com.opengamma.id.ExternalScheme} of {@link com.opengamma.core.id.ExternalSchemes#BLOOMBERG_TICKER}. 
   * </p>
   * @param identifiers the identifiers that comprise the chain
   */
  public BloombergEQVanillaOptionChain(List<ExternalId> identifiers) {
    _identifiers = identifiers;
  }
  
  /**
   * Creates a copy of a {@code BloombergEQVanillaOptionChain}. Note that this is a shallow
   * copy; since both {@code Identifier} and {@code BloombergEQVanillaOptionChain} are immutable,
   * this is not an issue.
   * @param original the original chain to copy
   */
  public BloombergEQVanillaOptionChain(BloombergEQVanillaOptionChain original) {
    _identifiers = original._identifiers;
  }
  
  
  // -------- MAIN OPERATIONS --------
  /**
   * Returns a new chain narrowed by option type (either {@link com.opengamma.financial.security.option.OptionType#CALL CALL} 
   * or {@link com.opengamma.financial.security.option.OptionType#PUT PUT}).
   * @param optionType the option type to narrow on
   * @return a new chain narrowed by option type
   */
  public BloombergEQVanillaOptionChain narrowByOptionType(final OptionType optionType) {
    // Simple O(n) filtering on optiontype
    List<ExternalId> result = new ArrayList<ExternalId>(Collections2.filter(_identifiers, new Predicate<ExternalId>() {
      public boolean apply(ExternalId identifier) {
        return new BloombergTickerParserEQVanillaOption(identifier).getOptionType().equals(optionType);
      }
    }));
    return new BloombergEQVanillaOptionChain(result);
  }
 
  
  /**
   * Returns a new chain narrowed by expiry.  The expiry is specified as being at least {@code monthsFromToday} months from
   * today. See {@link #narrowByExpiry(LocalDate, int)} for details of the algorithm. 
   * @param monthsFromToday number of months from today to start searching for the expiry
   * @return a new chain narrowed by expiry
   */
  public BloombergEQVanillaOptionChain narrowByExpiry(int monthsFromToday) {
    return narrowByExpiry(LocalDate.now(OpenGammaClock.getInstance()), monthsFromToday);
  }
  
  /**
   * <p>
   * Returns a new chain narrowed by expiry.  The expiry is specified as being at least {@code monthsFromToday} months from
   * an arbitrary reference date.  Both positive and negative month offsets are supported.  
   * </p>
   * The search algorithm is as follows:
   * <ol>
   * <li>Search the chain for an expiry date exactly {@code monthsFromReferenceDate} months from the reference date. An expiry date is
   * defined as the Saturday after the third Friday of the month. If {@code monthsFromReferenceDate} is {@code 0}, then the algorithm
   * will start in month of the reference date, but will skip the expiry if it has already passed.</li>
   * <li>If options with the above expiry were found, then the algorithm stops, and the resulting chain is returned.</li>
   * <li>If not, then the algorithm will find the expiry that is nearest to the exact expiry from step 1. It measures the difference using
   * actual days.  The resulting chain is then returned.
   * </ol> 
   * <p>
   * Below, we provide a table showing sample results. We use US date format for Bloomberg compatibility. We cover only &#8805; 0 offsets
   * since that is the more usual case. Assume that:
   * <ul>
   * <li>Reference date is 4/18/2011</li>
   * <li>The option chain has expiries of 4/16/2011, 5/21/2011, 6/18/2011, 7/16/2011, 10/22/2011, 1/21/2012, 1/19/2013</li>
   * </ul>
   * <table>
   * <tr><th>Requested offset</th><th>Resulting Expiry</th></tr>
   * <tr><td>0</td><td>05/21/2011</td></tr>
   * <tr><td>1</td><td>05/21/2011</td></tr>
   * <tr><td>2</td><td>06/18/2011</td></tr>
   * <tr><td>3</td><td>07/16/2011</td></tr>
   * <tr><td>4</td><td>07/16/2011</td></tr>
   * <tr><td>5</td><td>10/22/2011</td></tr>
   * <tr><td>6</td><td>10/22/2011</td></tr>
   * <tr><td>7</td><td>10/22/2011</td></tr>
   * <tr><td>8</td><td>01/21/2012</td></tr>
   * <tr><td>9</td><td>01/21/2012</td></tr>
   * <tr><td>10</td><td>01/21/2012</td></tr>
   * <tr><td>11</td><td>01/21/2012</td></tr>
   * <tr><td>12</td><td>01/21/2012</td></tr>
   * <tr><td>13</td><td>01/21/2012</td></tr>
   * <tr><td>14</td><td>01/21/2012</td></tr>
   * <tr><td>15</td><td>01/21/2012</td></tr>
   * <tr><td>16</td><td>01/19/2013</td></tr>
   * <tr><td>17</td><td>01/19/2013</td></tr>
   * <tr><td>18</td><td>01/19/2013</td></tr>
   * <tr><td>19</td><td>01/19/2013</td></tr>
   * <tr><td>20</td><td>01/19/2013</td></tr>
   * </table>
   * <p>
   * Notice how, for the {@code 0} offset, the result was {@code 05/21/2011} and not {@code 04/16/2011}. That is because the reference date
   * was {@code 04/18/2011}, which is after {@code 04/16/2011}.  If the reference date had been, for example, {@code 04/11/2011}, then the 
   * result would have been {@code 4/16/2011} for a {@code 0} offset.
   * </p>
   * @param referenceDate the date to which the expiry is in reference
   * @param monthsFromReferenceDate number of months from the reference date to start searching for the expiry
   * @return a new chain narrowed by expiry
   */
  public BloombergEQVanillaOptionChain narrowByExpiry(LocalDate referenceDate, final int monthsFromReferenceDate) {
    // Special case - return empty on empty input
    if (_identifiers.isEmpty()) {
      return new BloombergEQVanillaOptionChain(_identifiers);
    }
    
    // Create parser version of chain
    // Collect all unique property values from chain
    List<BloombergTickerParserEQVanillaOption> parsers = new ArrayList<BloombergTickerParserEQVanillaOption>(_identifiers.size());
    NavigableSet<LocalDate> expiries = new TreeSet<LocalDate>();
    for (ExternalId identifier : _identifiers) {
      BloombergTickerParserEQVanillaOption parser = new BloombergTickerParserEQVanillaOption(identifier);
      parsers.add(parser);
      LocalDate expiry = parser.getExpiry();
      expiries.add(expiry);      
    }
    
    // Find the desired expiry
    LocalDate thirdSaturdayOfTargetMonth = determineTargetExpiry(referenceDate, monthsFromReferenceDate);    
    LocalDate floorValue = Objects.firstNonNull(expiries.floor(thirdSaturdayOfTargetMonth), expiries.first());
    LocalDate targetValue = floorValue;
    if (!floorValue.equals(thirdSaturdayOfTargetMonth)) {
      LocalDate ceilingValue = Objects.firstNonNull(expiries.ceiling(thirdSaturdayOfTargetMonth), expiries.last());
      double diff1 = calcDayDiff(thirdSaturdayOfTargetMonth, floorValue);
      double diff2 = calcDayDiff(thirdSaturdayOfTargetMonth, ceilingValue);
      if (diff1 < diff2) {
        targetValue = floorValue;
      } else {
        targetValue = ceilingValue;
      } 
    }
    
    // Collect all identifiers with this expiry
    // (Note: tried pre-hashing and retrieving, was slower than this O(n) approach)
    List<ExternalId> identifiers = new ArrayList<ExternalId>();
    for (BloombergTickerParserEQVanillaOption parser : parsers) {
      if (parser.getExpiry().equals(targetValue)) {
        identifiers.add(parser.getIdentifier());
      }
    }
    
    // Done
    return new BloombergEQVanillaOptionChain(identifiers);
  }
  
  /**
   * <p>
   * Returns a new chain narrowed by strike.  The strike is specified as being at least {@code strikeOffset} strikes from
   * an arbitrary reference price (usually the current underlyer price.)  Both positive and negative strike offsets are supported.  
   * </p>
   * <p>
   * The search algorithm is as follows:
   * </p>
   * <ol>
   * <li>If the {@code strikeOffset} is {@code 0}, return the strike that is nearest to the reference price, whether it is above or below.</li>
   * <li>For non-zero {@code strikeOffset} values, simply find the place in the chain where the reference price would sit, and then
   * find the strike that is {@code strikeOffset} strikes away from it. Return a new option chain with all options of the resulting
   * strike.</li>
   * <li>In either case, if the algorithm reaches the beginning or end of the chain, the strike at the beginning or end will be chosen. </li>
   * </ol>
   * <p>
   * Below, we provide a table showing sample results. We cover only &#8805; 0 offsets
   * since that is the more usual case. Assume that:
   * <ul>
   * <li>Reference price is 199</li>
   * <li>The option chain has strikes of 135, 140, 145, . . . 540 at intervals of 5.</li>
   * </ul>
   * <table>
   * <tr><th>Requested Offset</th><th>Resulting Strike</th></tr>
   * <tr><td>0</td><td>200.0</td></tr>
   * <tr><td>1</td><td>200.0</td></tr>
   * <tr><td>2</td><td>205.0</td></tr>
   * <tr><td>3</td><td>210.0</td></tr>
   * <tr><td>4</td><td>215.0</td></tr>
   * <tr><td>5</td><td>220.0</td></tr>
   * <tr><td>6</td><td>225.0</td></tr>
   * <tr><td>7</td><td>230.0</td></tr>
   * <tr><td>8</td><td>235.0</td></tr>
   * <tr><td>9</td><td>240.0</td></tr>
   * <tr><td>10</td><td>245.0</td></tr>
   * <tr><td>...</td><td>...</td></tr>
   * <tr><td>68</td><td>535.0</td></tr>
   * <tr><td>69</td><td>540.0</td></tr>
   * <tr><td>70</td><td>540.0</td></tr>
   * <tr><td>71</td><td>540.0</td></tr>
   * <tr><td>72</td><td>540.0</td></tr>
   * <tr><td>73</td><td>540.0</td></tr>
   * <tr><td>74</td><td>540.0</td></tr>
   * <tr><td>75</td><td>540.0</td></tr>
   * </table>
   * <p>
   * Notice how, for both the {@code 0} and {@code 1} offsets, the result was {@code 200}. For the {@code 0} offset, the algorithm simply
   * looked for the strike nearest to {@code 199}; for the {@code 1} offset, the algorithm found the next strike above {@code 199}.  In both
   * cases, that value was {@code 200}.  Notice also that after {@code 70+} offsets, we reach the end of the chain, and the resulting strike is
   * always {@code 540}.
   * </p>
   * @param referencePrice the price to which the strike offset is in reference
   * @param strikeOffset distance of strikes from the reference price to select
   * @return a new chain narrowed by strike
   */
  public BloombergEQVanillaOptionChain narrowByStrike(final double referencePrice, int strikeOffset) {
    // Create parser version of chain
    // Collect all unique property values from chain
    List<BloombergTickerParserEQVanillaOption> parsers = new ArrayList<BloombergTickerParserEQVanillaOption>(_identifiers.size());
    NavigableSet<Double> strikes = new TreeSet<Double>();
    for (ExternalId identifier : _identifiers) {
      BloombergTickerParserEQVanillaOption parser = new BloombergTickerParserEQVanillaOption(identifier);
      parsers.add(parser);
      Double strike = parser.getStrike();
      strikes.add(strike);      
    }
    
    // Find the desired strike
    Double targetValue = null;
    
    // Special case:  offset is 0 to begin with, just find the nearest strike
    if (strikeOffset == 0) {
      double floorValue = Objects.firstNonNull(strikes.floor(referencePrice), strikes.first());
      targetValue = floorValue;
      if (floorValue != referencePrice) {
        double ceilingValue = Objects.firstNonNull(strikes.ceiling(referencePrice), strikes.last());
        double diff1 = Math.abs(referencePrice - floorValue);
        double diff2 = Math.abs(referencePrice - ceilingValue);
        if (diff1 < diff2) {
          targetValue = floorValue;
        } else {
          targetValue = ceilingValue;
        } 
      }
    } else {
      // Otherwise, move forward or backward the desired number of offsets and take that value
      targetValue = referencePrice;
      while (strikeOffset != 0) {
        Double next = null;
        if (strikeOffset > 0) {
          next = strikes.higher(targetValue);
          if (next != null) {
            targetValue = next;
            strikeOffset--;
          } else {
            break;
          }
        } else {
          next = strikes.lower(targetValue);
          if (next != null) {
            targetValue = next;
            strikeOffset++;
          } else {
            break;
          }
        }
      }
    }
    
    // Collect all identifiers with this strike
    // (Note: tried pre-hashing and retrieving, was slower than this O(n) approach)
    List<ExternalId> identifiers = new ArrayList<ExternalId>();
    for (BloombergTickerParserEQVanillaOption parser : parsers) {
      if (parser.getStrike() == targetValue) {
        identifiers.add(parser.getIdentifier());
      }
    }
    
    // Done
    return new BloombergEQVanillaOptionChain(identifiers);
  }
 
  
  // -------- PROPERTIES --------
  /**
   * Returns the {@code Identifiers} in the chain. They will be in a set sorted by alphabetical order, in order to mimic the output ordering
   * of {@link com.opengamma.bbg.util.BloombergDataUtils#getOptionChain BloombergDataUtil.getOptionChain()}.
   * @return the {@code Identifiers} in the chain
   */
  public NavigableSet<ExternalId> getIdentifiers() {
    return new TreeSet<ExternalId>(_identifiers);
  }
  
  
  // -------- PRIVATE SUBROUTINES --------
  private LocalDate determineTargetExpiry(LocalDate referenceDate, int monthsFromReferenceDate) {
    LocalDate result = referenceDate.plusMonths(monthsFromReferenceDate);
    result = LocalDate.of(result.getYear(), result.getMonth(), 1);
    while (!(result.getDayOfWeek() == FRIDAY)) {
      result = result.plusDays(1);
    }
    result = result.plusDays(15); // Saturday after third Friday
    
    // Fencepost condition: if we are looking 0 months ahead, but the referenceDate is past the
    // the Saturday after third Friday, then move forward 1 month
    if (monthsFromReferenceDate == 0 && result.isBefore(referenceDate)) {
      result = determineTargetExpiry(referenceDate, 1);
    }
    return result;
  }
  
  private double calcDayDiff(LocalDate thirdSaturdayOfTargetMonth, LocalDate expiry) {
    ZonedDateTime dummyNow = ZonedDateTime.now(OpenGammaClock.getInstance()); // "now()" is just to get dummy time of day and zone
    
    ZonedDateTime zonedThirdSaturdayOfTargetMonth = thirdSaturdayOfTargetMonth.atTime(dummyNow.toLocalTime()).atZone(dummyNow.getZone());
    ZonedDateTime zonedExpiry = expiry.atTime(dummyNow.toLocalTime()).atZone(dummyNow.getZone());
    if (expiry.isAfter(thirdSaturdayOfTargetMonth)) {
      return _dayCount.getDayCountFraction(zonedThirdSaturdayOfTargetMonth, zonedExpiry);  
    } else {
      return _dayCount.getDayCountFraction(zonedExpiry, zonedThirdSaturdayOfTargetMonth);
    }
  }
}
