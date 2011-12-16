/**
 * @copyright 2009 - present by OpenGamma Inc
 * @license See distribution for license
 */
$.register_module({
    name: 'og.common.util.date',
    dependencies: [],
    obj: function () {
        return function (timestamp) {
            // the timestamp format isnt supported by for IE or safari, we need to change the format
            if (isNaN(new Date(timestamp).getDate())) return timestamp;
            var add_zero = function (n) {return n < 10 ? '0' + n : '' + n;},
                obj = new Date(timestamp),
                d = add_zero(obj.getDate()),
                M = add_zero(obj.getMonth() + 1),
                y = obj.getFullYear(),
                h = add_zero(obj.getHours()),
                m = add_zero(obj.getMinutes()),
                s = add_zero(obj.getSeconds()),
                date = d + '<span> / </span>' + M + '<span> / </span>' + y,
                time = '<span>' + h + ':' + m + ':' + s + '</span>';
            return '<time title="day / month / year">' + date + '<span> @ </span>' + time + '</time>';
        }
    }
});