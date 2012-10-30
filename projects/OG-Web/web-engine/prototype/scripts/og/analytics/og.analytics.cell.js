/*
 * Copyright 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.analytics.Cell',
    dependencies: ['og.api.rest', 'og.analytics.Data', 'og.common.events'],
    obj: function () {
        var module = this, events = og.common.events, constructor = function (config) {
            var cell = this;
            cell.dataman = new og.analytics.Data(config.source, true /* bypass checking for primitives/portfolio */)
                .viewport({rows: [config.row], cols: [config.col], expanded: true})
                .on('data', function (data) {
                    var cell_data = data[0];
                    try {events.fire(cell.events.data, cell_data);}
                    catch (error) {og.dev.warn(module.name + ': a data handler threw ', error);}
                })
                .on('fatal', function (message) {
                    try {events.fire(cell.events.fatal, message);}
                    catch (error) {og.dev.warn(module.name + ': a fatal handler threw ', error);}
                });
            cell.events = {data: [], fatal: []};
        };
        constructor.prototype.kill = function () {this.dataman.kill();};
        constructor.prototype.on = events.on;
        return constructor;
    }
});