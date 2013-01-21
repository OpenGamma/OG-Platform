/**
 * Copyright 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.common.gadgets.Data',
    dependencies: ['og.common.gadgets.manager', 'og.common.events', 'og.analytics.Grid'],
    obj: function () {
        var module = this, loading_template, formatters, char_width = 9, STRING = 'STRING';
        var cell_value = function (v) {return {v: v + ''};};
        var meta = function (dataman, rows, cols, fixed_width, first) {
            var dimensions = rows + '|' + cols.length, meta;
            if (dataman.dimensions === dimensions) return null; else dataman.dimensions = dimensions;
            meta = {
                structure: [], data_rows: rows,
                columns: {
                    fixed: [{columns: [{type: STRING, header: first ? cols[0] : '', width: fixed_width}]}],
                    scroll: [{
                        columns: cols.slice(first ? 1 : 0).map(function (col) {return {type: STRING, header: col};})
                    }],
                    total: first ? cols.length : cols.length + 1
                }
            };
            return Object.keys(meta).forEach(function (key) {dataman.meta[key] = meta[key];}), meta;
        };
        var pad = function (digit) {return (digit < 10 ? '0' : '') + digit;};
        var viewport = function (data, viewport, row_length) {
            return viewport.rows.reduce(function (acc, row) {
                var start = row * row_length;
                return acc.concat(viewport.cols.map(function (col) {return start + col;}));
            }, []).map(function (index) {return data[index];});
        };
        formatters = {
            CURVE: function (dataman, data) {
                return {
                    meta: meta(dataman, data.length, ['X', 'Y'], 50),
                    data: data
                        .reduce(function (acc, val, idx) {return acc.concat({v: idx + 1}, val.map(cell_value));}, []),
                };
            },
            LABELLED_MATRIX_1D: function (dataman, data) {
                var cols = data.labels, fixed_width = Math.max(
                    data.data[0] ? data.data[0][0].length * char_width : 100,
                    data.labels[0].length * char_width
                );
                return {
                    meta: meta(dataman, data.data.length / cols.length, cols, fixed_width, true),
                    data: data.data.reduce(function (acc, val) {return acc.concat(val.map(cell_value));}, []),
                };
            },
            LABELLED_MATRIX_2D: function (dataman, data) {
                var cols = data.xLabels, rows = data.yLabels.map(cell_value),
                    fixed_width = data.yLabels[rows.length - 1].length * char_width;
                return {
                    meta: meta(dataman, rows.length, cols, fixed_width),
                    data: data.matrix
                        .reduce(function (acc, val, idx) {return acc.concat(rows[idx], val.map(cell_value));}, []),
                };
            },
            TIME_SERIES: function (dataman, data) {
                return {
                    meta: meta(dataman, data.timeseries.data.length, data.timeseries.fieldLabels, 125, true),
                    data: data.timeseries.data.reduce(function (acc, val) {
                        var date = new Date(val[0]);
                        val[0] = date.getUTCFullYear() + '-' + pad(date.getUTCMonth() + 1) + '-' +
                            pad(date.getUTCDate()) + '  ' + pad(date.getUTCHours()) + ':' + pad(date.getUTCMinutes()) +
                            ':' + pad(date.getUTCSeconds());
                        return acc.concat(val.map(cell_value));
                    }, [])
                };
            }
        };
        var DataMan = function (row, col, type, source, config) {
            var dataman = this, format = formatters[type].partial(dataman), last_dimensions;
            dataman.cell = config.parent ? config.parent.cell : new og.analytics
                .Cell({source: source, col: col, row: row, format: 'EXPANDED'}, 'griddata')
            dataman.cell.on('data', function (raw) {
                var message;
                if (raw.error || !raw.v) return;
                try {dataman.formatted = format(raw.v);} catch (error) {
                    og.dev.warn(message = module.name + ': formatting ' + type + ' failed, ' + error.message);
                    return dataman.kill(), dataman.fire('fatal', message);
                }
                if (dataman.formatted.meta) {
                    dataman.fire('meta', dataman.meta);
                }
                if (dataman.formatted.data && dataman.meta.viewport) dataman
                    .fire('data', viewport(dataman.formatted.data, dataman.meta.viewport, dataman.meta.columns.total));
            });
            dataman.id = dataman.cell.id;
            dataman.label = config.label;
            dataman.meta = {viewport: {rows: [], cols: []}};
        };
        DataMan.prototype.fire = og.common.events.fire;
        DataMan.prototype.kill = function () {
            var dataman = this;
            dataman.cell.kill();
        };
        DataMan.prototype.off = og.common.events.off;
        DataMan.prototype.on = og.common.events.on;
        DataMan.prototype.viewport = function (new_viewport) {
            var dataman = this;
            if (!new_viewport) return (dataman.meta.viewport.cols = []), (dataman.meta.viewport.rows = []);
            dataman.meta.viewport = new_viewport;
            setTimeout(function () {
                dataman.fire('data', viewport(dataman.formatted.data, new_viewport, dataman.meta.columns.total));
            });
        };
        var GridData = function (config) {
            var grid = this;
            if (!formatters[config.type]) {
                $(config.selector).html('GridData cannot render: ' + config.type);
                return;
            }
            og.analytics.Grid.call(grid, {
                selector: config.selector, child: config.child, label: 'datagadget',
                dataman: DataMan.partial(config.row, config.col, config.type),
                show_sets: false, show_views: false, source: config.source
            });
        };
        GridData.prototype = new og.analytics.Grid;
        return GridData;
    }
});