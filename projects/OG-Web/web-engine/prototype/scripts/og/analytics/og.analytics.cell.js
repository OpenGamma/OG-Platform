/*
 * Copyright 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.analytics.Cell',
    dependencies: ['og.api.rest', 'og.analytics.Data', 'og.common.events'],
    obj: function () {
        var events = og.common.events, constructor = function (config) {
            var cell = this;
            cell.dataman = new og.analytics.Data(config.source)
                .viewport({rows: [config.row], cols: [config.col], expanded: true})
                .on('data', function (data) {
                    var cell_data = data[0];
                    if (cell_data.t === 'PRIMITIVE') return; // we never subscribe to primitives
                    events.fire(cell.events.data, cell_data.v || cell_data);
                });
            cell.events = {data: []};
        };
        constructor.prototype.kill = function () {this.dataman.kill();};
        constructor.prototype.on = events.on;
        return constructor;
    }
});