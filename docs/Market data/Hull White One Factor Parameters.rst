Hull White One Factor Parameters
================================

Introduction
------------

Hull White One Factor Parameters are used to supply data during curve
calibration so that convexity adjustments can be applied to futures
in the curve.

Data Structure
--------------

The data is held in a market data snapshot in a ``HullWhiteOneFactorParametersSnapshot``
instance. This object contains:

- a name so that it can be referred to from other parts of the system
- the currency that the parameters are defined for
- the mean reversion
- a ``HullWhiteOneFactorVolatilityTable`` instance which defines the
  volatilities between particular tenors

The volatility table defines a structure like:

=========== ========= ==========
Start Tenor End Tenor Volatility
=========== ========= ==========
0           6M        0.01
6M          1Y        0.011
1Y          2Y        0.012
2Y          5Y        0.013
5Y          null      0.014
=========== ========= ==========

As an example, when a curve is being calibrated, a future which falls between
the 6M (inclusive) and 1Y (exclusive) points will use a volatility value of 0.011.

There is validation of the table such that:

- there must be at least one row
- the start tenor of the first row is 0
- the end tenor of every row except the last must match the start tenor
  of the subsequent row
- the end tenor of the last row must be null

In its simplest form the table could look like:

=========== ========= ==========
Start Tenor End Tenor Volatility
=========== ========= ==========
0           null      0.01
=========== ========= ==========

XML Format
----------
The ``HullWhiteOneFactorParametersSnapshot`` can be represented in an xml format
which can be used when storing in a database. Below is a sample:

.. code:: xml

  <?xml version="1.0" encoding="UTF-8"?>
  <bean type="com.opengamma.financial.analytics.parameters.HullWhiteOneFactorParametersSnapshot">
   <name>testSnapshot</name>
   <currency>USD</currency>
   <meanReversion>0.01</meanReversion>
   <volatilityTable>
    <entries>
     <item>
      <startTenor>P0D</startTenor>
      <endTenor>P6M</endTenor>
      <volatility>0.01</volatility>
     </item>
     <item>
      <startTenor>P6M</startTenor>
      <endTenor>P1Y</endTenor>
      <volatility>0.011</volatility>
     </item>
     <item>
      <startTenor>P1Y</startTenor>
      <endTenor>P2Y</endTenor>
      <volatility>0.012</volatility>
     </item>
     <item>
      <startTenor>P2Y</startTenor>
      <endTenor>P5Y</endTenor>
      <volatility>0.013</volatility>
     </item>
     <item>
      <startTenor>P5Y</startTenor>
      <volatility>0.014</volatility>
     </item>
    </entries>
   </volatilityTable>
  </bean>


Creating in Database
--------------------

A new set of parameter data can be loaded into the database using the
configuration screens:

#. Select NamedSnapshots from the list of available items
#. Enter the xml data representing your parameter data
#. Click add

Note that the entries in the volatility table can be in any order. However,
when saved they will automatically be ordered by start tenor.

Creating Programmatically
-------------------------

The following code can be used to create the parameter data programmatically:

.. code:: java

  SortedSet<HullWhiteOneFactorVolatilityEntry> entries = ImmutableSortedSet.of(
      HullWhiteOneFactorVolatilityEntry.builder()
          // first start tenor must be zero
          .startTenor(Tenor.ofDays(0))
          .endTenor(Tenor.SIX_MONTHS)
          .volatility(0.01)
          .build(),
      HullWhiteOneFactorVolatilityEntry.builder()
          .startTenor(Tenor.SIX_MONTHS)
          .endTenor(Tenor.ONE_YEAR)
          .volatility(0.011)
          .build(),
      HullWhiteOneFactorVolatilityEntry.builder()
          .startTenor(Tenor.ONE_YEAR)
          .endTenor(Tenor.TWO_YEARS)
          .volatility(0.012)
          .build(),
      HullWhiteOneFactorVolatilityEntry.builder()
          .startTenor(Tenor.TWO_YEARS)
          .endTenor(Tenor.FIVE_YEARS)
          .volatility(0.013)
          .build(),
      HullWhiteOneFactorVolatilityEntry.builder()
          .startTenor(Tenor.FIVE_YEARS)
          // No end tenor for final entry
          .volatility(0.014)
          .build());

  HullWhiteOneFactorVolatilityTable table = HullWhiteOneFactorVolatilityTable.builder()
      .entries(entries)
      .build();

  HullWhiteOneFactorParametersSnapshot snapshot = HullWhiteOneFactorParametersSnapshot.builder()
      .name("testSnapshot")
      .currency(Currency.USD)
      .meanReversion(0.01)
      .volatilityTable(table)
      .build();



