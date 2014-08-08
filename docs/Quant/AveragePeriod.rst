Average coupon: average of one index over several dates
=======================================================

Introduction
------------

This documentation describes a type of coupon which is Ibor-indexed and for which the payment is computed through the arithmetic average of the fixing at different dates. The coupon is characterised by a payment date :math:`t_p` and a payment amount. The amount is computed by reference to a unique Ibor-like index. The index fixing is used for a set of dates (not a unique date). The index fixing on date :math:`t` is denoted :math:`I^{j}(t)`. All the coupons discussed in this documentation are single currency coupons.

Three variations of the same coupon are described below: single period averaging, sub-periods averaging with compounding (without spread) and sub-periods averaging with compounding and spread. In practice, only the most generic one has been implemented but the intermediary step are useful to understand the structure of the coupons.

Pay-off description: single period
----------------------------------

The coupon accrual factor is denoted by :math:`\delta`, its payment date is :math:`t^p` and its notional is :math:`N`. There is one set of fixing dates :math:`(t_i)_{i=1, \ldots, n}` with :math:`t_1 < t_2 < \cdots < t_i < \cdots < t_n \leq t^p` and one set of weights or quantities :math:`(w_i)_{i=1, \ldots, n}`. The pay-off in :math:`t^p` is

.. math::

   \operatorname{PayOff} = N \delta \sum_{i=1}^{n} w_i I^{j}(t_i)

Pay-off description: compounding over multiple periods
------------------------------------------------------

In some cases, the averaging mechanism is mixed with a compounding feature. Suppose the amount is paid in :math:`t^p` and there are :math:`m` compounding sub-periods, each with an accrual factor of :math:`\delta_k`. The fixing times are now denoted :math:`(t_{i,k})_{i=1,\ldots n_k ; k=1,\cdots m}` and the weights for each fixing are :math:`w_{i,k}`. In that case the pay-off in :math:`t^p` becomes

.. math::
   :nowrap:

   \begin{eqnarray}
      R_k                   & = & \sum_{i=1}^{n_k} w_{i,k} I^{j}(t_{i,k}) \nonumber \\ 
      \operatorname{PayOff} & = & N \left( \prod_{k=1}^m \left( 1 + \delta_k R_k \right)  - 1 \right) \nonumber
   \end{eqnarray}

Pay-off description: (flat) compounding with spread
---------------------------------------------------

For compounding with spread, ISDA (see [ISD.2009.1]_) describes several compounding method. The one the most often associated to the average over periods is the *Flat Compounding*. The spread is denoted :math:`s`.


Let :math:`\operatorname{CPA}_k` denotes the Compounding Period Amount for Compounding Period :math:`k`. The amount paid in :math:`t^p` is compute through a recursive process, using previous paragraph averaged :math:`R_k`, by:

.. math::
   :nowrap:

   \begin{eqnarray}
      \operatorname{CPA}_1 & = & N  \delta_1\left(R_1 + s \right) \\
      \operatorname{CPA}_{k} & = & N  \delta_k\left(R_k + s \right) + \sum_{i=1}^{k-1} \operatorname{CPA}_i \delta_k R_k \\
      \operatorname{PayOff} & = & \sum_{i=1}^n \operatorname{CPA}_i   
   \end{eqnarray}

Related indexes
===============

The type of pay-off described in the introduction is used in particular in USD in relation to the Commercial Paper (CP) index. One of such index is published by the Federal Reserve on the so-called H.15 pages: `http://www.federalreserve.gov/releases/h15/data.htm <http://www.federalreserve.gov/releases/h15/data.htm>`_.

Indexes for commercial papers are published with tenor 1, 2 and 3 months. It is published for non financial and financial institutions. The interest rates are interpolated from data on certain commercial paper trades settled by The Depository Trust Company. The trades represent sales of commercial paper by dealers or direct issuers to investors (that is, the offer side).
The commercial paper rates are collected on an discount basis with ACT/360 day count.

The most liquid swap type based on those indexes are the Commercial-Paper (CP) versus USD LIBOR three month. The CP leg corresponds to unweighted monthly average of daily resets of 1-month CP rate taken from the above index compounded monthly at CP Flat, minus spread versus 3m-Libor. Both legs are paid quarterly with day count ACT/360. There is a two day cut-off in the last monthly averaging period. The cut-off offers a couple of days between the last fixing date and the payment date which allows for smoother settlement.

In practice the 3 month accrual period of the deal is split in three monthly sub period with start dates denoted :math:`(t_{1,k})_{k=1,2,3}` and final date :math:`t^p` one month apart (in the modified following, end-of-month rule). Between those dates, each business date (in NYC calendar) provide a fixing. The dates are denoted :math:`(t_{i,k})_{i=1,\ldots, n_k; k=1,2,3}`; the number of fixing :math:`n_k` is approximatively 20. The expection is the last period where the last date :math:`t_{3,n_3}` is not the last day before :math:`t^p` but two (business) days before (one day is missing with respect to a no cut-off approach). The weights are the same for each fixing in a sub-period, i.e. :math:`w_{i,k} = 1/n_{k}`. The accrual factors are the accrual factor between the sub-periods end dates in the ACT/360 day count. The spread is added on the CP leg using the flat compounding approach (see Section~\ref{SubSecFlatCmp}).

Pricing method: no convexity adjustment
=======================================

In this section, we present a simplified approach to the pricing of the average coupon in the multi-curve framework. The approach is simplified in the sense that we do not take into account the adjustment that are required as the payment date of the coupons are not the same as the end of fixing period associated to the fixing index.

In this simplified pricing approach, we compute the coupon value by forward and discounting. The fixing have the same underlying index but have different fixing dates. Which means that not all fixing can have a natural payment date equal to :math:`t_p` and at least one adjustment would be required. This adjustment is ignored here. 

Let :math:`0` be the pricing date. The discounting factor for the currency of the coupon at time :math:`u` is denoted :math:`P^D_X(u)`. The forward rates (see [HEN.2014.1]_ for all the theoretical details) for the index :math:`j` between the dates :math:`u` and :math:`v` are denoted :math:`F^{j}(u,v)`. As the fixing dates are different, the start and end dates of the fixing periods will be different, we denote the starting dates and end dates by :math:`(u^i, v^i)` in the one period case and :math:`(u^{i,k}, v^{i,k})` in the multiple sub-periods case.

Present value: single period
----------------------------

The present value, when the valuation date is before the first fixing time, is:

.. math::

   P^D_X(t^p) N \delta \sum_{i=1}^{n} w_i F^j(u_i, v_i).

When the value is computed during the fixing period, part of the rates will be known and part will be estimated. Suppose that :math:`t_l \leq 0 < t_{l+1}`. The fixings in :math:`t_1` to :math:`t_l` are known. The pricing formula is

.. math::

   P^D_X(t^p) N \delta \left( \sum_{i=1}^{l} w_i I^{j}(t_i) + \sum_{i=l+1}^{n} w_i F^j(u_i, v_i) \right).

The quantities :math:`I^{j}(t_i)` have to be retrieve from historical time series and are not provided by the curves :math:`F^j` which provide the forward rates, not the past rates.

Present value: compounding over multiple periods
------------------------------------------------

For the compounded without spread case, the pricing formula, when the valuation date is before the first fixing time, is:

.. math::
   :nowrap:

   \begin{eqnarray}
      R^F_k & = & \sum_{i=1}^{n_k} w_{i,k} F^j(u_{i,k}, v_{i,k}) \\ 
      \operatorname{PresentValue} & = & P^D_X(t^p) N \left( \prod_{k=1}^m \left( 1 + \delta_k R_k^F \right)  - 1 \right)
   \end{eqnarray}

When the value is computed during the fixing period, part of the rates will be known and part will be estimated. Suppose that :math:`t_{(l, m)} \leq 0 < t_{(l,m)+1}`. The fixings in :math:`t_{(1,1)}` to :math:`t_{(l,m)}` are known. The pricing formula is

.. math::
   :nowrap:

   \begin{eqnarray}
      R^F_k & = & \sum_{i=1}^{n_k} w_{i,k} I^j(t_{i,k}) \quad (1 \leq k < m) \nonumber \\
      R^F_m & = & \sum_{i=1}^{l} w_{i,m} I^j(t_{i,m})  + \sum_{i=l}^{n_m} w_{i,m} F^j(u_{i,m}, v_{i,m}) \nonumber \\ 
      R^F_k & = & \sum_{i=1}^{n_k} w_{i,k} F^j(u_{i,k}, v_{i,k}) \quad (k > m) \nonumber \\ 
      \operatorname{PresentValue} & = & P^D_X(t^p) N \left( \prod_{k=1}^m \left( 1 + \delta_k R_k \right)  - 1 \right).
   \end{eqnarray}

Present value: (flat) compounding with spread
---------------------------------------------

For the compounded with spread case, the pricing formula used here is:

.. math::
   :nowrap:

   \begin{eqnarray}
      \operatorname{CPA}^F_1 & = & N  \delta_1 \left(R^F_1 + s \right) \\
      \operatorname{CPA}^F_{k} & = & N \delta_k \left(R^F_k + s \right) + \sum_{i=1}^{k-1} \operatorname{CPA}_{k-1} \delta_k R^F_k \\
      \operatorname{PayOff} & = & P^D_X(t^p) \sum_{i=1}^n \operatorname{CPA}^F_i
   \end{eqnarray}

Implementation
==============

The classes with the coupon description are ``CouponIborAverageFixinDates``, ``CouponIborAverageCompounding`` and ``CouponIborAverageCompoundingSpread``.

The pricing formulas are in the pricing method ``CouponIbor...``.

.. [ISD.2009.1] Mengle, D. (2009). Alternative compounding methods for over-the-counter derivative transactions. ISDA. 1
.. [HEN.2014.1] Henrard, M. (2014). Interest Rate Modelling in the Multi-curve Framework. Applied Quantitative Finance. Palgrave Macmillan. ISBN: 978-1-137-37465-3. 3

.. [#f1] First version: 16 June 2014; this version: 0.1 - 19 June 2014.

