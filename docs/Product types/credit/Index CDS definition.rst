Index CDS definition
====================

A CDS index definition is represented in the system by an instance of ``com.opengamma.financial.security.credit.IndexCDSDefinitionSecurity``.
This defines the index terms, constituents, currency and other fixed attributes of the index.
This is used in conjunction with ``com.opengamma.financial.security.credit.IndexCDSSecurity`` which contains the trade specific details of a contract on the index.

Index Identifier
----------------
An identifier scheme and id that identifies this index. Common schemes are Markit RED codes,
e.g. ``MARKIT_RED_CODE~2I666VBB0`` which corresponds to iTRAXX Europe S21.

Family
------
The family the index is a part of. iTRAXX Europe S21 is part of the ``main`` family.

Series
------
The series this index is a part of. iTRAXX Europe S21, is part of the 21st series.

Version
-------
The version number of this index. Usually incremented each time the constituents are changed (e.g. on a member default).

Coupon
------
The coupon amount that the protection buyer must pay. The amount must be given as a fraction (e.g. 100 bps should be represented as 0.01).
iTRAXX Europe S21 has a coupon of 0.01 (i.e. 100 bps).

Coupon frequency
----------------
The frequency with which coupons are to be paid. Usually quarterly.

Start date
----------
The date the index started.

Currency
--------
The currency the index is denominated in.

Recovery rate
-------------
The recovery rate for this index. 0.4 (i.e. 40%) is common.

.. include:: ../Interest Rates/Frequency.rst

Tenors (aka Terms)
------------------
The set of tenor maturities available (5Y, 10Y etc) for the index.

Calendars
---------
The calendars used to determine good business days for coupon payments.

.. include:: ../Interest Rates/Calendars.rst

Business day convention
-----------------------
The business day adjustment to make if a payment falls on a non business day.

.. include:: ../Interest Rates/BusinessDayConvention.rst

Index components
----------------
The constituent components (i.e. single names) that compose the index.
This excludes components that defaulted in previous versions of the index and were thus excluded from this version.

Component identifier
~~~~~~~~~~~~~~~~~~~~
An identifier for the index component. Generally a single name Markit RED codes, e.g. ``MARKIT_RED_CODE~16B9CT`` for Centrica Plc.

Component name
~~~~~~~~~~~~~~
The index components name.

Component weight
~~~~~~~~~~~~~~~~
The weight the component has in the index. Generally all constituents are equally weighted.

Bond identifier
~~~~~~~~~~~~~~~
An optional identifier linking to the bond issue the cds is based on.










