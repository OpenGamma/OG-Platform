/*
 * Copyright 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.analytics.Clipboard',
    dependencies: ['og.analytics.Data'],
    obj: function () {
        var module = this, tab = '\t', line = '\n', textarea, node, is_array = $.isArray, formatters = {
            CURVE: function (value, single) {
                if (!single) return '**CURVE**';
                return (is_array(value) ? value : value.v || []).map(function (row) {return row.join(tab);}).join(line);
            },
            DOUBLE: function (value) {return value.v || '';},
            LABELLED_MATRIX_1D: function (value, single) {
                if (!single) return value.v || '**1D MATRIX**';
                return [value.v.labels.join(tab)]
                    .concat(value.v.data.map(function (row) {return row.join(tab);})).join(line);
            },
            LABELLED_MATRIX_2D: function (value, single) {
                if (!single) return value.v || '**2D MATRIX**';
                var rows, cols, matrix;
                value = value.v || value;
                cols = [''].concat(value['xLabels']);
                rows = value['yLabels'];
                matrix = value.matrix;
                return cols.join(tab) + line + matrix
                    .map(function (row, idx) {return rows[idx] + tab + row.join(tab);}).join(line);
            },
            LABELLED_MATRIX_3D: function (value, single) {return '**3D MATRIX**';},
            PRIMITIVE: function (value) {return value.v || '';},
            SURFACE_DATA: function (value, single) {
                if (!single) return value.v || '**SURFACE DATA**';
                var rows, cols, data, index = 0, row_len, col_len, i, j, result, row;
                value = value.v || value;
                col_len = (cols = value.xLabels).length;
                row_len = (rows = value.yLabels).length;
                data = value.vol;
                result = [tab + cols.join(tab)];
                for (i = 0; i < row_len; i += 1) {
                    for (j = 0, row = [rows[i]]; j < col_len; j += 1) row.push(data[index++]);
                    result.push(row.join(tab));
                }
                return result.join(line);
            },
            TIME_SERIES: function (value, single) {
                if (!single) return '**TIME SERIES**';
                var rows, cols, pad = function (digit) {return digit < 10 ? '0' + digit : digit;};
                value = value.v || value;
                rows = value.timeseries.data;
                cols = value.timeseries['fieldLabels'];
                return cols.join(tab) + line + rows.map(function (row) {
                    var date = new Date(row[0]);
                    row[0] = date.getUTCFullYear() + '-' + pad(date.getUTCMonth() + 1) + '-' + pad(date.getUTCDate()) +
                        '  ' + pad(date.getUTCHours()) + ':' + pad(date.getUTCMinutes()) + ':' +
                        pad(date.getUTCSeconds());
                    return row.join(tab);
                }).join(line);
            },
            UNKNOWN: function (value, single) {
                var type = value.t;
                return value && formatters[type] ? formatters[type](value, single) : value && value.v || '';
            },
            UNPLOTTABLE_SURFACE_DATA: function (value, single) {return formatters.LABELLED_MATRIX_2D(value, single);},
            VECTOR: function (value, single) {
                if (!single) return value.v || '**VECTOR**';
                if (value.v.label) value.v.data.unshift(value.v.label);
                return value.v.data.join(line);
            }
        };
        var constructor = function (grid) {
            var clipboard = this;
            clipboard.data = clipboard.selection = null;
            clipboard.dataman = new og.analytics.Data(grid.source, {label: 'clipboard', parent: grid.dataman})
                .on('data', data_handler, clipboard);
            clipboard.grid = grid
                .on('select', function (selection) {clipboard.viewport(selection);})
                .on('deselect', function () {clipboard.clear();});
        };
        var data_handler = function (data) {
            var clipboard = this, grid = clipboard.grid, lcv, index, selection = clipboard.selection, rows, cols, row;
            if (!clipboard.selection) return; // user has deselected before handler came back, so bail
            index = 0; rows = selection.rows.length; cols = selection.cols.length;
            clipboard.data = [];
            while (rows--) for (clipboard.data.push(row = []), lcv = 0; lcv < cols; lcv += 1)
                row.push({value: data[index++], type: clipboard.selection.type[lcv]});
            if (!grid.selector.copyable) grid.selector.render();
        };
        var format = function (cell, single) {
            var formatted;
            if (typeof cell.value === 'undefined') return '';
            if (cell.value.error) {
                formatted = typeof cell.value.v === 'string' ? cell.value.v : '***ERROR***';
                og.dev.warn(module.name + ': ' + formatted);
                return formatted;
            };
            if (formatters[cell.type]) return formatters[cell.type](cell.value, single);
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
                single = selection.rows.length === 1 && selection.cols.length === 1;
            if (!data || !data.length) return og.dev.warn(module.name + ': no data to select'), select('');
            if (!data.formatted) data.formatted = data.map(function (row) { // only format once per tick
                return row.map(function (col) {return format(col, single);}).join(tab);
            }).join(line);
            select(data.formatted);
        };
        constructor.prototype.viewport = function (selection) {
            var clipboard = this, grid = clipboard.grid, grid_data, data_viewport = clipboard.dataman.meta.viewport,
                expanded = selection && selection.rows.length === 1 && selection.cols.length === 1,
                format = expanded ? 'EXPANDED' : 'CELL';
            if (selection === null) return clipboard.dataman.viewport(clipboard.selection = clipboard.data = null);
            grid_data = grid.range(selection, expanded);
            if (same_viewport(clipboard.selection, selection)) if (same_viewport(selection, data_viewport))
                return grid_data ? (clipboard.dataman.viewport(null), clipboard.data = grid_data) : null;
            return (clipboard.selection = selection) && grid_data ?
                (clipboard.dataman.viewport(null), clipboard.data = grid_data)
                    : (clipboard.dataman.viewport({rows: selection.rows, cols: selection.cols, format: format}),
                        clipboard.data = null);
        };
        $(function () {
            node = (textarea = $('<textarea />').appendTo('body')
                .css({position: 'absolute', top: '-500px', left: '-500px', width: '100px', height: '100px'}))[0];
        });
        return constructor;
    }
});
