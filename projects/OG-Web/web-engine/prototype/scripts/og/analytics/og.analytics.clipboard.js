/*
 * Copyright 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.analytics.Clipboard',
    dependencies: ['og.analytics.Data'],
    obj: function () {
        var module = this, textarea, node, formatters = {
            CURVE: function (value, standalone) {
                if (!standalone) return '**CURVE**';
                return ($.isArray(value) ? value : value.v || [])
                    .map(function (row) {return row.join('\t');}).join('\n');
            },
            DOUBLE: function (value) {
                return typeof value === 'string' ? value : typeof value.v === 'string' ? value.v : ''
            },
            LABELLED_MATRIX_1D: function (value, standalone) {
                if (!standalone) return typeof value === 'string' ? value : typeof value.v === 'string' ? value.v : '';
                return ($.isArray(value) ? value : value.v || [])
                    .map(function (row) {return row.join('\t');}).join('\n');
            },
            LABELLED_MATRIX_2D: function (value, standalone) {
                if (!standalone) return typeof value === 'string' ? value : typeof value.v === 'string' ? value.v : '';
                var rows, cols, matrix;
                value = value.v || value;
                cols = [''].concat(value['xLabels']);
                rows = value['yLabels'];
                matrix = value.matrix;
                return cols.join('\t') + '\n' + matrix
                    .map(function (row, idx) {return rows[idx] + '\t' + row.join('\t');}).join('\n');
            },
            PRIMITIVE: function (value) {
                return typeof value === 'string' ? value : typeof value.v === 'string' ? value.v : '';
            },
            SURFACE_DATA: function (value, standalone) {
                if (!standalone) return '** SURFACE DATA **';
                var rows, cols, data, index = 0, row_len, col_len, i, j, result, row;
                value = value.v || value;
                col_len = (cols = value.x_labels).length;
                row_len = (rows = value.y_labels).length;
                data = value.vol;
                result = ['\t' + cols.join('\t')];
                for (i = 0; i < row_len; i += 1) {
                    for (j = 0, row = [rows[i]]; j < col_len; j += 1) row.push(data[index++]);
                    result.push(row.join('\t'));
                }
                return result.join('\n');
            },
            TIME_SERIES: function (value, standalone) {
                if (!standalone) return '**TIME SERIES**';
                var rows, cols;
                var pad = function (digit) {return digit < 10 ? '0' + digit : digit;};
                value = value.v || value;
                rows = value.timeseries.data;
                cols = value.timeseries['fieldLabels'];
                return cols.join('\t') + '\n' + rows.map(function (row) {
                    var date = new Date(row[0]);
                    row[0] = date.getUTCFullYear() + '-' + pad(date.getUTCMonth() + 1) + '-' + pad(date.getUTCDate()) +
                        '  ' + pad(date.getUTCHours()) + ':' + pad(date.getUTCMinutes()) + ':' +
                        pad(date.getUTCSeconds());
                    return row.join('\t');
                }).join('\n');
            },
            UNKNOWN: function (value, standalone) {
                var type = value.t;
                return value && formatters[type] ? formatters[type](value, standalone) : value && value.v || '';
            }
        };
        var constructor = function (grid) {
            var clipboard = this;
            clipboard.data = clipboard.selection = null;
            clipboard.dataman = new og.analytics.Data(grid.source, true).on('data', data_handler, clipboard);
            clipboard.grid = grid.on('select', function (selection) {clipboard.viewport(selection);});
        };
        var data_handler = function (data) {
            var clipboard = this, grid = clipboard.grid, lcv, index = 0, selection = clipboard.selection,
                rows = selection.rows.length, cols = selection.cols.length, row;
            clipboard.data = [];
            while (rows--) for (clipboard.data.push(row = []), lcv = 0; lcv < cols; lcv += 1)
                row.push({value: data[index++], type: clipboard.selection.type[lcv]});
            if (!grid.selector.copyable) grid.selector.render();
        };
        var format = function (cell, standalone) {
            if (typeof cell.value === 'undefined') return '';
            if (formatters[cell.type]) return formatters[cell.type](cell.value, standalone);
            og.dev.warn(module.name + ': no formatter for ' + cell.type, cell);
            return typeof cell.value.v === 'string' ? cell.value.v : '';
        };
        var same_viewport = function (one, two) {
            if ((!one || !two) && one !== two) return false; // if either viewport is null
            return one.rows.join('|') === two.rows.join('|') && one.cols.join('|') === two.cols.join('|');
        };
        var select = function (text) {textarea.val(text).focus().select();};
        constructor.prototype.clear = function () {
            var clipboard = this;
            if (clipboard.selection) clipboard.dataman.viewport(clipboard.selection = clipboard.data = null);
        };
        constructor.prototype.has = function (selection) {
            var clipboard = this, grid = clipboard.grid,
                expanded = selection.rows.length === 1 && selection.cols.length === 1;
            clipboard.viewport(selection);
            return !!selection && !!(clipboard.data || (clipboard.data = grid.range(selection, expanded)));
        };
        constructor.prototype.select = function () {
            var clipboard = this, data = clipboard.data, selection = clipboard.selection,
                standalone = selection.rows.length === 1 && selection.cols.length === 1;
            if (!data || !data.length) return og.dev.warn(module.name + ': no data to select'), select('');
            select(data.map(function (row) {
                return row.map(function (col) {return format(col, standalone);}).join('\t');
            }).join('\n'));
        };
        constructor.prototype.viewport = function (selection) {
            var clipboard = this, grid = clipboard.grid, grid_data, data_viewport = clipboard.dataman.meta.viewport,
                expanded = selection.rows.length === 1 && selection.cols.length === 1;
            if (selection === null) return clipboard.dataman.viewport(clipboard.selection = clipboard.data = null);
            grid_data = grid.range(selection, expanded);
            if (same_viewport(clipboard.selection, selection)) if (same_viewport(selection, data_viewport))
                return grid_data ? (clipboard.dataman.viewport(null), clipboard.data = grid_data) : null;
            return (clipboard.selection = selection) && grid_data ?
                (clipboard.dataman.viewport(null), clipboard.data = grid_data)
                    : (clipboard.dataman.viewport({rows: selection.rows, cols: selection.cols, expanded: expanded}),
                        clipboard.data = null);
        };
        $(function () {
            node = (textarea = $('<textarea />').appendTo('body')
                .css({position: 'absolute', top: '-500px', left: '-500px', width: '100px', height: '100px'}))[0];
        });
        return constructor;
    }
});
