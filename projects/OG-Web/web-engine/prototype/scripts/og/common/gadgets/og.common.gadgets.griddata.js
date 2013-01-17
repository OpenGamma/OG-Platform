/**
 * Copyright 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.common.gadgets.GridData',
    dependencies: ['og.common.gadgets.manager', 'og.common.events'],
    obj: function () {
        var module = this, loading_template, formatters;
        var meta = function (dataman, row_names, col_names, fixed_width) {
            var dimensions = row_names.length + '|' + col_names.length;
            if (dataman.dimensions === dimensions) return null;
            return (dataman.dimensions = dimensions), {
                structure: [], data_rows: row_names.length,
                columns: {
                    fixed: [{columns: [{type: 'STRING', header: '', width: fixed_width}]}],
                    scroll: [{
                        columns: col_names.map(function (name) {return {type: 'STRING', header: name};})
                    }]
                }
            };
        };
        var cell_value = function (v) {return {v: v + ''};};
        formatters = {
            LABELLED_MATRIX_2D: function (dataman, data) {
                var cols = data.xLabels, rows = data.yLabels.map(cell_value);
                return {
                    meta: meta(dataman, rows, cols, 100),
                    data: data.matrix
                        .reduce(function (acc, val, idx) {return acc.concat(rows[idx], val.map(cell_value));}, []),
                };
            }
        };
        var DataMan = function (row, col, type, source) {
            var dataman = this, format = formatters[type].partial(dataman), last_dimensions;
            dataman.cell = new og.analytics.Cell({source: source, col: col, row: row, format: 'EXPANDED'}, 'griddata')
                .on('data', function (raw) {
                    if (raw.error) return og.dev.warn(raw);
                    var formatted = format(raw.v);
                    if (formatted.meta) dataman.fire('meta', formatted.meta);
                    dataman.fire('data', formatted.data);
                });
            dataman.id = dataman.cell.id;
        };
        DataMan.prototype.fire = og.common.events.fire;
        DataMan.prototype.kill = function () {
            var dataman = this;
            dataman.cell.kill();
        };
        DataMan.prototype.off = og.common.events.off;
        DataMan.prototype.on = og.common.events.on;
        DataMan.prototype.viewport = function (viewport) {
        };
        var GridData = function (config) {
            var grid = this;
            if (!formatters[config.type]) {
                $(config.selector).html('GridData cannot render: ' + config.type);
                return;
            }
            og.analytics.Grid.call(grid, {
                selector: config.selector, child: config.child,
                dataman: DataMan.partial(config.row, config.col, config.type),
                local_clipboard: true, show_sets: false, show_views: false, source: config.source
            });
        };
        GridData.prototype = new og.analytics.Grid;
        return GridData;
    }
});