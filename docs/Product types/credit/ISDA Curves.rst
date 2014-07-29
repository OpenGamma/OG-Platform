===========
ISDA curves
===========

Yield curve data
================

Interest rate data for building ISDA yield curves is imported into OpenGamma via snapshots.
A simple domain model is used to store this data, consisting of two classes:

* ``com.opengamma.financial.analytics.isda.credit.YieldCurveData``
* ``com.opengamma.financial.analytics.isda.credit.YieldCurveDataSnapshot``

``YieldCurveData`` holds the values required to calibrate the yield curve, namely market data
and a set of conventions. ISDA yield curves support two instrument types - cash deposits
and swaps. Two maps, both indexed by tenor, are defined on the ``YieldCurveData`` class for
holding market data values. The curve's term structure is implied from the tenors defined across
the two maps.

Other fields on ``YieldCurveData`` define conventions required for bootstrapping.

``YieldCurveDataSnapshot`` is the snapshot object used to store the full set of ``YieldCurveData``
instances. When pricing credit instruments, yield curves are typically resolved at the currency
level. Hence they are keyed by currency in the snapshot.

For further information on yield curve bootstrapping in the ISDA model, see the
`OpenGamma quantitative research document`_ on the subject.

Credit curve data
=================

As with yield curve data, credit curves in OpenGamma are built from market data snapshots.
Term structure, market data and bootstrap parameters are all defined here.

Important domain object classes are as follows:

* ``com.opengamma.financial.analytics.isda.credit.CreditCurveDataSnapshot``
   Top level snapshot class. Holds the full set of credit curves required
   to price a portfolio of credit trades.

* ``com.opengamma.financial.analytics.isda.credit.CreditCurveDataKey``
   Used for indexing curves in the snapshot and credit curve resolution.
   See doc on market data resolution for further details on how this is used.
   
* ``com.opengamma.financial.analytics.isda.credit.CreditCurveData``
   Holds the market data values used for bootstrapping curves and the
   implicit term structure. Quotes are held in a map indexed by tenor.
   The term structure is infered from the key. Also holds the 
   recovery rate.
 
* ``com.opengamma.financial.convention.IsdaCreditCurveConvention``
   Fields hold the conventions used when bootstrapping a credit curve.
   This is referenced by each ``CreditCurveData`` object.
   
* ``com.opengamma.financial.analytics.isda.credit.CdsQuote``
   A CDS quote used for bootstrapping a curve. Defines a single 
   method (``toQuoteConvention()``) which converts the instance to
   its analytics equivalent. Standard subtypes are:

   * ``com.opengamma.financial.analytics.isda.credit.FlatSpreadQuote``
   * ``com.opengamma.financial.analytics.isda.credit.ParSpreadQuote``
   * ``com.opengamma.financial.analytics.isda.credit.PointsUpFrontQuote``

Bootstrapping a credit curve requires a fully calibrated ISDA yield curve
instance. The yield curve used is resolved using the currency on the
credit curve key. This is sourced from the configured 
``YieldCurveDataSnapshot`` instance.

For further information on credit curve bootstrapping in the ISDA model, see the
`OpenGamma quantitative research document`_ on the subject.

.. _OpenGamma quantitative research document: http://developers.opengamma.com/quantitative-research/Pricing-and-Risk-Management-of-Credit-Default-Swaps-OpenGamma.pdf
