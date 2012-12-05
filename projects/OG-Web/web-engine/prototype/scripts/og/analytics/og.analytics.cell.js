/*
 * Copyright 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.analytics.Cell',
    dependencies: ['og.api.rest', 'og.analytics.Data', 'og.common.events'],
    obj: function () {
        var module = this, events = og.common.events;
        var Cell = function (config, label) {
            var cell = this, options = {bypass: true, label: 'cell' + label};
            label = label ? '[' + label + ']' : '';
            var fatal_handler = function (message) {
                try {cell.fire('fatal', message);}
                catch (error) {og.dev.warn(module.name + ': a fatal handler threw ', error);}
            };
            if (typeof config.row === 'undefined' || typeof config.col === 'undefined')
                throw new TypeError(module.name + ': {row & col} are undefined');
            cell.dataman = new og.analytics.Data(config.source, options).on('meta', function (meta, raw) {
                var coordinate = [config.row, config.col].join(',');
                cell.dataman.viewport({cells: [coordinate], format: config.format, log: config.log});
            }).on('data', function (data) {
                var cell_data = data[0];
                try {cell.fire('data', cell_data);} catch (error) {
                    cell.kill();
                    fatal_handler(module.name + ': a data handler threw error: ' +
                        (error.message || 'an unknown error') + ' (check console.$)');
                    throw error; // let og.analytics.Data catch it
                }
            }).on('fatal', fatal_handler);
        };
        Cell.prototype.fire = events.fire;
        Cell.prototype.kill = function () {this.dataman.kill();};
        Cell.prototype.off = events.off;
        Cell.prototype.on = events.on;
        return Cell;
    }
});