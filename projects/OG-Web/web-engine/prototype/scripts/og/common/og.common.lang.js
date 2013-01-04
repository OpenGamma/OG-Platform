/*
 * Copyright 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.common.lang',
    dependencies: [],
    obj: function () {
        return function (str, skip_format) {
            var map = {
                '': '<span class="OG-lang-empty">(empty)</span>',
                'AUTO': 'Auto',
                'ACTIVFEED_TICKER': 'Activfeed Ticker',
                'BLOOMBERG_TICKER': 'Bloomberg Ticker',
                'BLOOMBERG': 'Bloomberg',
                'bloombergTicker': 'Bloomberg Ticker',
                'BLOOMBERG_BUID': 'Bloomberg BUID',
                'BLOOMBERG_TCM': 'Bloomberg TCM',
                'buySell': 'Buy Sell',
                'BUY_LONG': 'Buy Long',
                'BUY': 'Buy',
                'CALL': 'Call',
                'CASH': 'Cash',
                'dealType': 'Deal Type',
                'dealAttributes': 'Deal Attributes',
                'userAttributes': 'User Attributes',
                'expirationDate': 'Expiration Date',
                'EQUITY_INDEX_OPTION': 'Equity Index Option',
                'EQUITY_OPTION': 'Equity Option',
                'EQUITY_BARRIER_OPTION': 'Equity Barrier Option',
                'EUROPEAN': 'European',
                'EXTERNAL_SENSITIVITY_RISK_FACTORS': 'External Sensitivities Risk Factors',
                'EXTERNAL_SENSITIVITIES_SECURITY': 'External Sensitivities Security',
                'EXTERNAL_SENSITIVITIES_FACTOR_SET': 'External Sensitivities Factor Set',
                'FLOAT': 'Float',
                'FX_GENERATOR': 'FX Generator',
                'FX_BARRIER_OPTION': 'FX Barrier Option',
                'FX_FORWARD': 'FX Forward',
                'FX_OPTION': 'FX Option',
                'NONDELIVERABLE_FX_FORWARD': 'Non-deliverable FX Forward',
                'FX_DIGITAL_OPTION': 'FX Digital Option',
                'NONDELIVERABLE_FX_DIGITAL_OPTION': 'Non-deliverable FX Digital Option',
                'FIXED': 'Fixed',
                'FRADeal': 'FRA Deal',
                'GLOBAL': 'Global',
                'IRFUTURE_OPTION': 'IR Future Option',
                'IR_FUTURE_OPTION_LOADER': 'IR Future Option Loader',
                'JavaClass': 'Java Class',
                'KNOCK_IN': 'Knock In',
                'MIXED_PORFOLIO_LOADER': 'Mixed Portfolio Loader',
                'NONDELIVERABLE_FX_OPTION': 'Non-Deliverable FX Option',
                'OptionDeal': 'Option Deal',
                'putCall': 'Put Call',
                'PUT': 'Put',
                'PLAIN_VANILLA': 'Plain Vanilla',
                'PX_LAST': 'PX LAST',
                'settlementDate': 'Settlement Date',
                'SELL': 'Sell',
                'SWAPTION': 'Swaption',
                'SWAP_GENERATOR': 'Swap Generator'
            }, og_class = 'com.opengamma';
            if (map[str]) return map[str];
            if (0 === str.indexOf(og_class)) return str;
            if (skip_format) return str;
            return str.replace(/([a-z])([A-Z])/g, '$1 $2') // first add space between uppercase characters
                .replace(/^[a-z]/, function (chr) {return chr.toUpperCase();}); // then uppercase first character
        };
    }
});