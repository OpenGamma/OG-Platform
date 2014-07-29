Interest Rate Swap Security
===========================

Supported types
----------------
* Single currency vanilla swaps
* Ibor swaps
* OIS swaps
* Basis swaps
* Variable notional i.e. amortizing, accreting & rollercoaster
* Single ended stubs
* Double ended stubs
* Zero coupon
* Compounding

Unsupported types
-----------------
* Cross currency swaps.

Swap level attributes
---------------------

Effective date (required)
~~~~~~~~~~~~~~~~~~~~~~~~~
The effective date of the swap. This does not need to be business day adjusted as any necessary adjustment will be made during pricing.

  If represented as a string use the format YYYY-MM-DD.

Maturity date (required)
~~~~~~~~~~~~~~~~~~~~~~~~
The maturity date of the swap. This does not need to be business day adjusted as any necessary adjustment will be made during pricing.

  If represented as a string use the format YYYY-MM-DD.

Legs (required, at least 2)
~~~~~~~~~~~~~~~~~~~~~~~~~~~
The swap legs, at least 2 must be present for a valid swap. For more details see FixedInterestRateSwapLeg and FloatingInterestRateSwapLeg.

Notional Exchange (optional)
~~~~~~~~~~~~~~~~~~~~~~~~~~~~
Describes any notional exchanges that take place during the swap lifetime. If not specified no exchange is assumed. Exchanges at the beginning of the swap (initial), end (final) & during the swap period (interim) are accepted.

Common interest rate swap leg attributes
----------------------------------------

Notional (required)
~~~~~~~~~~~~~~~~~~~

.. include:: InterestRateSwapNotional.rst
  :start-line: 2 

Pay Or Receive (required)
~~~~~~~~~~~~~~~~~~~~~~~~~
Describes whether this leg represents an amount to be paid or received.

Day count convention (required)
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
Determines how to calculate the fraction of a year between 2 dates. Values should be specified as in the `FpML daycount specification`_. e.g.
* 30/360
* ACT/360
* ACT/365.FIXED
* ACT/ACT.ISDA
* BUS/252

Roll date convention (optional)
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
Controls how to adjust dates when rolling from one period in the swap to the next. Defaults to no date adjustment.

* NONE, no adjustment.
* EOM, end of month.
* IMM, IMM settlement dates (3rd Wednesday of March, June, September & December)
* IMM_NZD, the Wednesday after the 9th day of the month.
* SFE, Sydney Futures Exchange, 2nd Friday of the month.
* TBILL, Monday of that week.
* 1..30, that day of the month.
* MON..FRI, that day of the week.

Maturity business day convention (required)
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
The business day convention used to adjust the maturity date. 

.. include:: BusinessDayConvention.rst

Maturity calendars (optional)
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
The calendars used to adjust the maturity date. Multiple calendars can be provided.

.. include:: Calendars.rst

Payment business day convention (required)
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
The business day convention used to adjust payment dates. For the full specification see `Maturity business day convention (required)`_.

Payment calendars (optional)
~~~~~~~~~~~~~~~~~~~~~~~~~~~~
The calendars used to adjust payment dates in conjunction with the business day convention. Multiple calendars may be provided.

.. include:: Calendars.rst

Payment frequency (required)
~~~~~~~~~~~~~~~~~~~~~~~~~~~~
The frequency of payments. 

.. include:: Frequency.rst

Payment offset (optional)
~~~~~~~~~~~~~~~~~~~~~~~~~
The number of days payment offset to the accrual period. Defaults to 0.

Payment offset type (optional)
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
The type of days used to offset the payment date, of which the following are supported:
* BUSINESS
* CALENDAR

Payment relative to (optional)
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
Controls if the payment date is relative to the start or end of the accrual period. Defaults to END, with values:
* START
* END

Accrual period business day convention (required)
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
The business day convention used to determine calculation dates.

.. include:: BusinessDayConvention.rst

Accrual period calendars (optional)
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
The calendars used to determine calculation dates. Multiple calendars can be provided.

.. include:: Calendars.rst

Accrual period frequency (required)
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
The frequency used for calculation periods. 

.. include:: Frequency.rst


Stubs (optional)
~~~~~~~~~~~~~~~~
The stub description consists of the following:

* Stub type (e.g. SHORT_START)
* Effective date (optional, mandatory for dual stubs unless start/end index provided)
* Stub rate (optional)
* Interpolated start index (optional, mandatory if dates not provided)
* Interpolated end index (optional)
* Interpolator (optional, defaults to linear)

If not specified short start is assumed. The following stub types are supported:

* NONE
* SHORT_START
* LONG_START
* SHORT_END
* LONG_END
* BOTH (aka dual stubs)

Fixed Interest Rate Swap Leg
----------------------------

This represents the fixed leg of an interest rate swap.

Rate (required)
~~~~~~~~~~~~~~~
The fixed rate this leg pay or receives as a per annum rate.

> A rate of 1% would be represented as 0.01.

Floating Interest Rate Swap Leg
-------------------------------
This represents the floating leg of an interest rate swap.

Rate type (required)
~~~~~~~~~~~~~~~~~~~~
The rate type, used to control pricing methodology. Available types are:
* IBOR
* OIS
* CMS

Index identifier (required)
~~~~~~~~~~~~~~~~~~~~~~~~~~~
The identifier for the index this leg is based on. The index must be in the ISDA schema and specified as CCY-NAME-DATAPROVIDER-PERIOD (period to be omitted for overnight rates), eg.

* USD-LIBOR-BBA-3M
* USD-Federal Funds-H.15
* EUR-EURIBOR-Reuters-6M
* EUR-EONIA-OIS-COMPOUND
* GBP-LIBOR-BBA-1M
* GBP-WMBA-SONIA

See the `ISDA index list`_ for available rates.

Spread (optional)
~~~~~~~~~~~~~~~~~
Describes a spread on top of the index rate. Both constant and variable spreads are supported. Spread should be given on a per year basis, e.g. a 1% spread would be 0.01. A spread schedule is specified in the same way as a notional schedule, see [here][7] for details.

Reset period frequency (required)
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
The reset period frequency.

.. include:: Frequency.rst

Reset period business day convention (required)
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
The business day convention used when calculating reset dates.

.. include:: BusinessDayConvention.rst

Reset relative to (optional)
~~~~~~~~~~~~~~~~~~~~~~~~~~~~
Controls if the reset periods are relative to the start or end of the accrual period. Defaults to START. See [here][9] for valid parameters.

Compounding method (optional)
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

.. include:: :start-line: 4 Compounding.rst

Fixing date business day convention (required)
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
The business day convention used to determine the fixing dates.

.. include:: BusinessDayConvention.rst

Fixing date calendars (optional)
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
The list of calendars used to determine the fixing dates.

.. include:: Calendars.rst

Fixing date offset (optional)
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
The number of offset days relative to the reset period start date.

Fixing date offset day type (optional)
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

The type of offset days. See [here][8] for valid parameters.

Rate averaging method
~~~~~~~~~~~~~~~~~~~~~

Flag to indicate whether the rate is weighted or unweighted. Valid values are:

* WEIGHTED
* UNWEIGHTED

.. _FpML daycount specification: http://www.fpml.org/coding-scheme/day-count-fraction
.. _ISDA index list: http://www.isda.org/c_and_a/pdf/ISDA-Definitions-Rate-Cross-Reference-Chart-051804.pdf
  
