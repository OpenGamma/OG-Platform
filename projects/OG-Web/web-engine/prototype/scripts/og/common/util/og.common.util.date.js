/**
 * Copyright 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.common.util.date',
    dependencies: [],
    obj: function () {
        return function (timestamp, format) {
            // the timestamp format isnt supported by for IE or safari, we need to change the format
            if (isNaN(new Date(timestamp).getDate())) return timestamp;
            var add_zero = function (n) {return n < 10 ? '0' + n : '' + n;},
                obj = new Date(timestamp),
                d = add_zero(obj.getUTCDate()),
                M = add_zero(obj.getUTCMonth() + 1),
                y = obj.getUTCFullYear(),
                h = add_zero(obj.getUTCHours()),
                m = add_zero(obj.getUTCMinutes()),
                s = add_zero(obj.getUTCSeconds()),
                date = d + '<span> / </span>' + M + '<span> / </span>' + y,
                time = (format === "dateonly") ? '' : '<span> @ </span><span>' + h + ':' + m + ':' + s + '</span>';
            return '<time title="day / month / year">' + date + time + '</time>';
        }
    }
});