/*
 * Copyright 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.analytics.Cell',
    dependencies: ['og.api.rest', 'og.analytics.Data'],
    obj: function () {
        var module = this;
        return function (config) {
            var cell = this, source = config.source, row = config.row, col = config.col, events = {data: []};
            var fire = function (type) {
                var args = Array.prototype.slice.call(arguments, 1), lcv, len = events[type].length;
                for (lcv = 0; lcv < len; lcv += 1)
                    if (false === events[type][lcv].handler.apply(null, events[type][lcv].args.concat(args))) break;
            };
            cell.dataman = new og.analytics.Data(source)
                .viewport({rows: [row], cols: [col], expanded: true})
                .on('data', function (data) {fire('data', data[0][0]);});
            cell.on = function (type, handler) {
                if (type in events)
                    events[type].push({handler: handler, args: Array.prototype.slice.call(arguments, 2)});
                return cell;
            };
        };
    }
});