/*
 * @copyright 2011 - present by OpenGamma Inc
 * @license See distribution for license
 */
$.register_module({
    name: 'og.common.lang',
    dependencies: [],
    obj: function () {
        return function (str, skip_format) {
            var map = {
                '': '(empty value)',
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
                'FLOAT': 'Float',
                'FX_GENERATOR': 'FX Generator',
                'FX_BARRIER_OPTION': 'FX Barrier Option',
                'FX_FORWARD': 'FX Forward',
                'FX_OPTION': 'FX Option',
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
        }
    }
});