/*
 * @copyright 2009 - present by OpenGamma Inc
 * @license See distribution for license
 */
$.register_module({
    name: 'og.common.lang',
    dependencies: [],
    obj: function () {
        return function (str, bool) {
            return ({
                'AUTO': 'Auto',
                'BLOOMBERG_TICKER': 'Bloomberg Ticker',
                'bloombergTicker': 'Bloomberg Ticker',
                'BLOOMBERG_BUID': 'Bloomberg BUID',
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
                'SWAPTION': 'Swapton'
            })[str] || (str.substring(0, 13) === 'com.opengamma' && str)
                    || (bool === void 0 ? str
                    .replace(/([a-z])([A-Z])/g, '$1 $2') // add space between uppercase character
                    .replace(/^[a-z]/, function(txt){return txt.toUpperCase()}) : // uppercase first character
                str);
        }
    }
});