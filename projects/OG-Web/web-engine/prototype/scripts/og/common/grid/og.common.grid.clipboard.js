/*
 * Copyright 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.common.grid.Clipboard',
    dependencies: [],
    obj: function () {
        var module = this, tab = '\t', line = '\n', textarea, is_array = $.isArray, formatters = {
            CURVE: function (value, single) {
                if (!single) return '**CURVE**';
                return (is_array(value) ? value : value.v || []).map(function (row) {return row.join(tab);}).join(line);
            },
            DOUBLE: function (value) {return value.v || '';},
            DOUBLE_GADGET: function (value) {return value.v || '';},
            ERROR: function (value) {
                return value['logOutput'].map(function (row) {
                    return [
                        ['Function:', row['functionName']].join(' '),
                        ['Target:', row.target].join(' '),
                        ['Exception Class:', row['exceptionClass']].join(' '),
                        ['Exception Message:', row['exceptionMessage']].join(' '),
                        ['Events:', (row.events && row.events.length ? '' : 'N/A')].join(' '),
                        !row.events && !row.events.length ? null : row.events
                            .map(function (event) {return [tab, event.level, event.message].join(' ');}).join(line),
                        ['Stack Trace:', row['exceptionStackTrace'] ? '' : 'N/A'].join(' '),
                        !row['exceptionStackTrace'] ? null : row['exceptionStackTrace'].split(line)
                            .map(function (val) {return [tab, val].join('');}).join(line)
                    ].filter(Boolean).join(line);
                }).join(line + '------------------------------------------------------------------------' + line);
            },
            FUNGIBLE_TRADE: function (value) {return value.v && value.v.name || '';},
            LABELLED_MATRIX_1D: function (value, single) {
                if (!single) return value.v || '**1D MATRIX**';
                return [value.v.labels.join(tab)]
                    .concat(value.v.data.map(function (row) {return row.join(tab);})).join(line);
            },
            LABELLED_MATRIX_2D: function (value, single) {
                if (!single) return value.v || '**2D MATRIX**';
                var rows, cols, matrix;
                value = value.v || value;
                rows = value['yLabels'];
                matrix = value.matrix;
                cols = value['xLabels'].length === 1 + matrix[0].length ? value['xLabels']
                    : [''].concat(value['xLabels']);
                return cols.join(tab) + line + matrix
                    .map(function (row, idx) {return rows[idx] + tab + row.join(tab);}).join(line);
            },
            LABELLED_MATRIX_3D: function (value, single) {return '**3D MATRIX**';},
            MATRIX_2D: function (value, single) {
                if (!single) return value.v || '**2D MATRIX**';
                value = value.v || value;
                return  value.map(function (row, idx) {return row.join(tab);}).join(line);
            },
            NODE: function (value) {return value.v && value.v.name || '';},
            OTC_TRADE: function (value) {return value.v && value.v.name || '';},
            POSITION: function (value) {return value.v && value.v.name || '';},
            STRING: function (value) {return value.v || '';},
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
                var rows, cols;
                value = value.v || value;
                rows = value.timeseries.data;
                cols = value.timeseries['fieldLabels'];
                return cols.join(tab) + line + rows
                    .map(function (row) {return (row[0] = og.common.util.date(row[0])), row.join(tab);}).join(line);
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
        var Clipboard = function (grid, local) {
            var clipboard = this;
            clipboard.data = clipboard.selection = null;
            clipboard.dataman = new (grid.config.dataman || og.analytics.Data)(grid.source, {
                label: 'clipboard', parent: grid.dataman, bypass: true
            }).on('data', data_handler, clipboard);
            clipboard.grid = grid
                .on('select', function (selection) {clipboard.viewport(selection);})
                .on('deselect', function () {clipboard.clear();});
        };
        var data_handler = function (data) {
            var clipboard = this, grid = clipboard.grid, lcv, index,
                selection = clipboard.selection, rows, cols, row, cell, single;
            if (!clipboard.selection || !data) return; // user has deselected before handler came back, so bail
            index = 0; rows = selection.rows.length; cols = selection.cols.length; single = rows === 1 && cols === 1;
            if (single && data[0].error && !data[0]['logOutput']) // if there's no error output yet, bail
                return clipboard.data = null;
            clipboard.data = [];
            while (rows--) for (clipboard.data.push(row = []), lcv = 0; lcv < cols; lcv += 1) {
                cell = data[index++];
                row.push({value: cell, type: cell && cell['logOutput'] ? 'ERROR' : clipboard.selection.type[lcv]});
            }
            grid.selector.render();
        };
        var format = function (cell, single) {
            if (typeof cell.value === 'undefined') return '';
            if (cell.value.error && cell.type !== 'ERROR')
                return typeof cell.value.v === 'string' ? cell.value.v : '#ERROR';
            if (formatters[cell.type]) return formatters[cell.type](cell.value, single);
            og.dev.warn(module.name + ': no formatter for ' + cell.type, cell);
            return typeof cell.value.v === 'string' ? cell.value.v : '';
        };
        var select = function (text) {textarea.val(text).focus().select();};
        Clipboard.prototype.clear = function () {
            var clipboard = this;
            clipboard.selection = clipboard.data = null;
            if (clipboard.selection) clipboard.dataman.viewport({clipboard: 'clear'});
        };
        Clipboard.prototype.has = function (selection) {
            var clipboard = this, grid = clipboard.grid, grid_data,
                expanded = selection && selection.rows.length === 1 && selection.cols.length === 1;
            if (clipboard.selection && selection && !Object.equals(clipboard.selection, selection))
                clipboard.viewport(selection);
            if (!selection) return false;
            if (clipboard.data) return true;
            grid_data = grid.range(selection, expanded);
            if (expanded && grid_data.raw && grid_data.raw[0][0].value.error) grid_data.data = null;
            return !!(clipboard.data = grid_data.data);
        };
        Clipboard.prototype.select = function () {
            var clipboard = this, data = clipboard.data, selection = clipboard.selection, single;
            if (!selection) return;
            single = selection.rows.length === 1 && selection.cols.length === 1;
            if (!data || !data.length) return og.dev.warn(module.name + ': no data to select'), select('');
            if (!data.formatted) data.formatted = data.map(function (row) { // only format once per tick
                return row.map(function (col) {return format(col, single);}).join(tab);
            }).join(line);
            select(data.formatted);
        };
        Clipboard.prototype.viewport = function (selection) {
            var clipboard = this, grid = clipboard.grid, grid_data, data_viewport = clipboard.dataman.meta.viewport,
                expanded = selection && selection.rows.length === 1 && selection.cols.length === 1,
                format = expanded ? 'EXPANDED' : 'CELL', log = false;
            if (selection === null) {
                clipboard.selection = clipboard.data = null;
                return clipboard.dataman.viewport({clipboard: 'clear'});
            }
            grid_data = grid.range(selection, expanded);
            if (format === 'EXPANDED' && grid_data.raw && grid_data.raw[0][0].value.error){
                (log = true), grid_data.data = null;
            }
            if (clipboard.selection && selection && Object.equals(clipboard.selection, selection)) {
                if (selection && data_viewport && Object.equals(selection, data_viewport)) {
                    return grid_data.data ? (clipboard.dataman.viewport({clipboard: 'clear'}),
                        clipboard.data = grid_data.data) : null;
                }
            }
            clipboard.selection = selection;
            if (grid_data.data) {
                return clipboard.dataman.viewport({clipboard: 'clear'}), clipboard.data = grid_data.data;
            }
            clipboard.dataman.viewport({rows: selection.rows, cols: selection.cols, format: format, log: log});
            clipboard.data = null;
        };
        $(function () {
            (textarea = $('<textarea readonly="readonly" />').appendTo('body')
                .css({position: 'absolute', top: '-500px', left: '-500px', width: '100px', height: '100px'}))[0];
        });
        return Clipboard;
    }
});