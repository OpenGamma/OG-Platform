/**
 * Copyright 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.common.gadgets.Data',
    dependencies: ['og.common.gadgets.manager', 'og.common.events', 'og.common.gadgets.Grid'],
    obj: function () {
        var module = this, Grid = og.common.gadgets.Grid, loading_template, formatters, char_width = 8,
            DOUBLE = 'DOUBLE_GADGET', STRING = 'STRING';
        var cell_value = function (v) {return {v: v + ''};};
        var col_names = (function () {
            var letters = 'ABΓΔΕΖΗΘΙΚΛΜΝΞΟΠΡΣΤΥΦΧΨΩ'.split(''), base = letters.length, base_log = Math.log(base);
            var letter = function (num) {
                if (num < base) return letters[num];
                var digits = Math.floor(Math.log(num) / base_log) + 1, lcv, result = '', digit, power;
                for (lcv = digits; lcv > 0; lcv -= 1) {
                    num -= ((digit = Math.floor(num / (power = Math.pow(base, lcv - 1)))) * power);
                    // because A plays both the role of 0 and 1 when it is the first digit
                    result += letters[lcv === digits ? Math.max(0, digit - 1) : digit];
                }
                return result;
            };
            return function (len) {
                var lcv, result = [];
                for (lcv = 0; lcv < len; lcv += 1) {
                    result.push(letter(lcv));
                }
                return result;
            };
        })();
        var get_viewport_data = function (dataman) {
            var data = dataman.formatted.data, viewport = dataman.meta.viewport, rows = dataman.meta.columns.total;
            if (!viewport.rows) return; // clipboard.clear ends up in this state
            return viewport.rows.reduce(function (acc, row) {
                var start = row * rows;
                return acc.concat(viewport.cols.map(function (col) {return start + col;}));
            }, []).map(function (index) {return data[index];});
        };
        var meta = function (dataman, rows, cols, fixed_width, first) {
            var dimensions = rows + '|' + cols.length, meta;
            if (dataman.dimensions === dimensions) {
                return null;
            } else {
                dataman.dimensions = dimensions;
            }
            return {
                structure: [], data_rows: rows,
                columns: {
                    fixed: [{columns: [{type: STRING, header: first ? cols[0] : '', width: fixed_width}]}],
                    scroll: [{
                        columns: cols.slice(first ? 1 : 0).map(function (col) {return {type: DOUBLE, header: col};})
                    }],
                    total: first ? cols.length : cols.length + 1
                }
            };
        };
        var viewport = function (new_viewport) {
            var dataman = this;
            if (!new_viewport) {
                dataman.meta.viewport.cols = [];
                dataman.meta.viewport.rows = [];
                return dataman;
            }
            dataman.meta.viewport = new_viewport;
            if (dataman.formatted.data) {
                setTimeout(function () {dataman.fire('data', get_viewport_data(dataman)); });
            }
            return dataman;
        }
        formatters = {
            CURVE: function (dataman, data) {
                if (!data || !data.length) return;
                return {
                    meta: meta(dataman, data.length, ['X', 'Y'], 50),
                    data: data.reduce(function (acc, val, idx) {
                        return acc.concat({v: idx + 1}, val.map(cell_value));
                    }, [])
                };
            },
            LABELLED_MATRIX_1D: function (dataman, data) {
                if (!data || !data.data || !data.data.length) return;
                var cols = data.labels, fixed_width = Math.max(
                    data.data[0] ? data.data[0][0].length * char_width : 100,
                    data.labels[0].length * char_width
                );
                return {
                    meta: meta(dataman, data.data.length, cols, fixed_width, true),
                    data: data.data.reduce(function (acc, val) {
                        return acc.concat(val.map(cell_value));
                    }, [])
                };
            },
            LABELLED_MATRIX_2D: function (dataman, data) {
                if (!data || !data.matrix || !data.matrix.length) return;
                var cols = data.xLabels, rows = data.yLabels.map(cell_value),
                    // Try to work out a reasonable header column width by taking the maximum number of characters in
                    // the header column and multiplying by an average character width. Ensure column width is no
                    // smaller than the grid's minimum of 50.
                    fixed_width = Math.max(50, Math.max.apply(null, data.yLabels.pluck('length')) * char_width);
                return {
                    meta: meta(dataman, rows.length, cols, fixed_width, cols.length === 1 + data.matrix[0].length),
                    data: data.matrix.reduce(function (acc, val, idx) {
                        return acc.concat(rows[idx], val.map(cell_value));
                    }, [])
                };
            },
            MATRIX_2D: function (dataman, data) {
                if (!data || !data.length) return;
                var cols = col_names(data[0].length);
                return {
                    meta: meta(dataman, data.length, cols, 50),
                    data: data.reduce(function (acc, val, idx) {
                        return acc.concat({v: idx + 1}, val.map(cell_value));
                    }, [])
                };
            },
            SURFACE_DATA: function (dataman, data) {
                if (!data || !data.vol || !data.vol.length) return;
                var rows, cols, data, index = 0, row_len, col_len, i, j, result = [], row, cell;
                col_len = (cols = data.xLabels).length;
                row_len = (rows = data.yLabels).length;
                for (i = 0; i < row_len; i += 1) {
                    cell = rows[i];
                    result.push({v: typeof cell === 'number' ? '' + cell : cell});
                    for (j = 0; j < col_len; j += 1) {
                        cell = data.vol[index++];
                        result.push({v: typeof cell === 'number' ? '' + cell : cell});
                    }
                }
                return {meta: meta(dataman, row_len, cols, 150), data: result};
            },
            TIME_SERIES: function (dataman, data) {
                if (!data || !data.timeseries.data.length) return;
                return {
                    meta: meta(dataman, data.timeseries.data.length, data.timeseries.fieldLabels, 125, true),
                    data: data.timeseries.data.reverse().reduce(function (acc, val) {
                        return (val[0] = og.common.util.date(val[0])), acc.concat(val.map(cell_value));
                    }, [])
                };
            }
        };
        var DataMan = function (req, colset, row, col, type, source, config) {
            var dataman = this, format = formatters[type].partial(dataman);
            dataman.cell = (config.parent ? config.parent.cell : new og.analytics
                .Cells({ // TODO: stop special casing CURVE gadgets (they need nodal + interpolated)
                    source: source, single: {req: req, colset: colset, row: row, col: col},
                    format: type === 'CURVE' ? 'CELL' : 'EXPANDED'
                }, config.label))
                .on('title', function (row_name, col_name, name) {dataman.fire('title', row_name, col_name, name);})
                .on('data', function (raw) {
                    var message, viewport = dataman.meta.viewport;
                    if (raw.error || !raw.v) return;
                    try {dataman.formatted = config.parent ? config.parent.formatted : format(raw.v);} catch (error) {
                        og.dev.warn(message = module.name + ': formatting ' + type + ' failed, ' + error.message);
                        return dataman.kill(), dataman.fire('fatal', message);
                    }
                    if (!dataman.formatted) return; // only a clipboard will ever be in this state
                    if (dataman.formatted.meta) {
                        Object.keys(dataman.formatted.meta) // populate dataman.meta
                            .forEach(function (key) {dataman.meta[key] = dataman.formatted.meta[key];});
                        dataman.fire('meta', dataman.meta);
                    }
                    if (dataman.formatted.data && viewport && viewport.cols && viewport.cols.length && viewport.rows.length)
                        dataman.fire('data', get_viewport_data(dataman));
                })
                .on('fatal', function (message) {
                    try {dataman.fire('fatal', message);}
                    catch (error) {og.dev.warn(module.name + ': a fatal handler threw ', error);}
                });
            dataman.id = dataman.cell.id;
            dataman.meta = {viewport: {rows: [], cols: []}};
        };
        var RestDataMan = function (resource, rest_options, type) {
            var dataman = this, format = formatters[type].partial(dataman);
            og.api.rest[resource].get(rest_options).pipe(function (result) {
                if (result.error) dataman.fire('fatal', result.message);
                var message, viewport = dataman.meta.viewport;
                try {dataman.formatted = format(result.data)} catch (error) {
                    og.dev.warn(message = module.name + ': formatting ' + type + ' failed, ' + error.message);
                    return dataman.kill(), dataman.fire('fatal', message);
                }
                if (!dataman.formatted) return; // only a clipboard will ever be in this state
                if (dataman.formatted.meta) {
                    Object.keys(dataman.formatted.meta) // populate dataman.meta
                        .forEach(function (key) {dataman.meta[key] = dataman.formatted.meta[key];});
                    dataman.fire('title', null, null, result.data.template_data.name)
                    dataman.fire('meta', dataman.meta);
                }
                if (dataman.formatted.data && viewport && viewport.cols.length && viewport.rows.length)
                    dataman.fire('data', get_viewport_data(dataman));
            });
            dataman.meta = {viewport: {rows: [], cols: []}};
        };
        ['fire', 'off', 'on'].forEach(function (key)
            {DataMan.prototype[key] = RestDataMan.prototype[key] = og.common.events[key];});
        DataMan.prototype.kill = function () {if (this.cell) this.cell.kill();};
        DataMan.prototype.viewport = RestDataMan.prototype.viewport = viewport;
        var Gadget = function (config) {
            var gadget = this;
            if (!formatters[config.type]) // return null or a primitive because this is a constructor
                return $(config.selector).html('Data gadget cannot render ' + config.type), null;
            // if config.req or config.colset do not exist (grids opened off a dependency graph) then null
            // needs to be passed to DataMan.partial ignores them.
            Grid.call(gadget, {
                selector: config.selector, child: config.child, show_sets: false, show_views: false,
                source: config.source, dataman: config.rest_options
                    ? RestDataMan.partial(config.resource, config.rest_options, config.type)
                    : DataMan.partial(config.req || null, config.colset || null, config.row, config. col, config.type)
            });
            gadget.on('fatal', function (message) {$(config.selector).html(message);});
        };
        Gadget.prototype = Object.create(Grid.prototype);
        Gadget.prototype.label = 'datagadget';
        return Gadget;
    }
});