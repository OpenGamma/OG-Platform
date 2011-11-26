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
                'BLOOMBERG_TICKER': 'Bloomberg Ticker',
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
                'EUROPEAN': 'European',
                'FLOAT': 'Float',
                'FIXED': 'Fixed',
                'FRADeal': 'FRA Deal',
                'GLOBAL': 'Global',
                'JavaClass': 'Java Class',
                'OptionDeal': 'Option Deal',
                'putCall': 'Put Call',
                'PUT': 'Put',
                'PLAIN_VANILLA': 'Plain Vanilla',
                'settlementDate': 'Settlement Date',
                'SELL': 'Sell',
                'SWAPTION': 'Swaption'
            }, og_class = 'com.opengamma';
            if (map[str]) return map[str];
            if (0 === str.indexOf(og_class)) return str;
            if (skip_format) return str;
            return str.replace(/([a-z])([A-Z])/g, '$1 $2') // first add space between uppercase characters
                .replace(/^[a-z]/, function (chr) {return chr.toUpperCase();}); // then uppercase first character
        }
    }
});