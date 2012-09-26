/*
 * Copyright 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.analytics.Cell',
    dependencies: ['og.api.rest', 'og.analytics.Data', 'og.analytics.events'],
    obj: function () {
        var events = og.analytics.events, constructor = function (config) {
            var cell = this;
            cell.events = {data: []};
            new og.analytics.Data(config.source).viewport({rows: [config.row], cols: [config.col], expanded: true})
                .on('data', function (data) {events.fire(cell.events.data, data[0]);});
        };
        constructor.prototype.on = events.on;
        return constructor;
    }
});