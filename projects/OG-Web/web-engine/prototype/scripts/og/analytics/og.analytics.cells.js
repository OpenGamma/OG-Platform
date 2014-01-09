/*
 * Copyright 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.analytics.Cells',
    dependencies: ['og.analytics.Data', 'og.common.events'],
    obj: function () {
        var module = this, events = og.common.events;
        var Cells = function (config, label) {
            var cell = this, options = {bypass: true, label: 'cell' + label, req: config.single.req, colset: config.single.colset},
                row = config.single.row, col = config.single.col, headers = [];
            cell.row_name = null;
            cell.col_name = null;
            label = label ? '[' + label + ']' : '';
            var fatal_handler = function (message) {
                try {
                    cell.fire('fatal', message);
                } catch (error) {
                    og.dev.warn(module.name + ': a fatal handler threw ', error);
                }
            };
            if (!config.multiple && !config.single) {
                throw new TypeError(module.name + ': single or multiple must be defined');
            }
            cell.dataman = new og.analytics.Data(config.source, options).on('meta', function (meta) {
                var coordinate = [row, col, config.format].join(','), title = [row, '0', 'CELL'].join(','),
                    fixed_headers = meta.columns.fixed[0].columns.pluck('header'), // only one fixed set ever
                    scroll_headers = meta.columns.scroll.pluck('columns').reduce(function (acc, val) {
                        return acc.concat(val);
                    }, []).pluck('header');
                headers = fixed_headers.concat(scroll_headers);
                cell.dataman.viewport({cells: [title, coordinate], log: config.log});
            }).on('data', function (data) {
                try {
                    if ((cell.row_name !== (data[0].v.name || data[0].v)) || (cell.col_name !== headers[col])) {
                        cell.fire('title', cell.row_name = data[0].v.name || data[0].v, cell.col_name = headers[col]);
                    }
                    cell.fire('data', data[1]);
                } catch (error) {
                    cell.kill();
                    fatal_handler(module.name + ': a data handler threw error: ' +
                        (error.message || 'an unknown error') + ' (check console.$)');
                    throw error; // let og.analytics.Data catch it
                }
            }).on('fatal', fatal_handler);
            cell.id = cell.dataman.id;
        };
        ['fire', 'off', 'on'].forEach(function (key) {Cells.prototype[key] = events[key]; });
        Cells.prototype.kill = function () {this.dataman.kill(); };
        return Cells;
    }
});
