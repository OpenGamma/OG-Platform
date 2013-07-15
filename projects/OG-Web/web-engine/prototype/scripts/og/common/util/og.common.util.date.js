/**
 * Copyright 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.common.util.date',
    dependencies: [],
    obj: function () {
        var pad = function (digit) {return (digit < 10 ? '0' : '') + digit;};
        return function (timestamp) {
            var date = new Date(timestamp);
            // the timestamp format isnt supported by for IE or safari, we need to change the format
            if (isNaN(date.getDate())) return timestamp;
            return date.getUTCFullYear() + '-' + pad(date.getUTCMonth() + 1) + '-' + pad(date.getUTCDate()) +
                ' ' + pad(date.getUTCHours()) + ':' + pad(date.getUTCMinutes()) + ':' + pad(date.getUTCSeconds());
        };
    }
});