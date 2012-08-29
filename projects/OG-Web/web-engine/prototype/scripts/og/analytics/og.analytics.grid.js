/*
 * Copyright 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.analytics.Grid',
    dependencies: ['og.api.text', 'og.analytics.Data'],
    obj: function () {
        var module = this, counter = 1, header_height = 55, row_height = 19, templates = null,
            scrollbar_size = (function () {
                var html = '<div style="width: 100px; height: 100px; position: absolute; \
                    visibility: hidden; overflow: auto; left: -10000px; z-index: -10000; bottom: 100px" />';
                return 100 - $(html).appendTo('body').append('<div />').find('div').css('height', '200px').width();
            })();
        if (window.parent !== window && window.parent.og.analytics && window.parent.og.analytics.Grid)
            return window.parent.og.analytics.Grid.partial(undefined, $); // if already compiled, use that
        var available = (function () {
            var unravel = function (nodes, arr, result) {
                var start = arr[0], end = arr[1], children = arr[2];
                result.push(start);
                if (!nodes[start]) return result;
                if (children.length) return children.forEach(function (child) {unravel(nodes, child, result);}), result;
                while (++start <= end) result.push(start);
                return result;
            };
            return function (grid) {return unravel(grid.meta.nodes, grid.meta.structure, []);};
        })();
        var background = function (sets, width, bg_color) {
            var columns = sets.reduce(function (acc, set) {return acc.concat(set.columns);}, []),
                height = row_height, pixels = [], lcv, fg_color = 'dadcdd', dots = columns
                    .reduce(function (acc, col) {return acc.concat([[bg_color, col.width - 1], [fg_color, 1]]);}, []);
            for (lcv = 0; lcv < height - 1; lcv += 1) Array.prototype.push.apply(pixels, dots);
            pixels.push([fg_color, width]);
            return BMP.rle8(width, height, pixels);
        };
        var col_css = function (id, sets, offset) {
            var partial_width = 0, columns = sets.reduce(function (acc, set) {return acc.concat(set.columns);}, []),
                total_width = columns.reduce(function (acc, val) {return val.width + acc;}, 0);
            return columns.map(function (val, idx) {
                var css = {
                    prefix: id, index: idx + (offset || 0),
                    left: partial_width, right: total_width - partial_width - val.width
                };
                return (partial_width += val.width), css;
            });
        };
        var compile_templates = function (handler) {
            var css = og.api.text({url: module.html_root + 'analytics/grid/og.analytics.grid_tash.css'}),
                header = og.api.text({module: 'og.analytics.grid.header_tash'}),
                container = og.api.text({module: 'og.analytics.grid.container_tash'}),
                row = og.api.text({module: 'og.analytics.grid.row_tash'}), compile = Handlebars.compile;
            $.when(css, header, container, row).then(function (css, header, container, row) {
                templates = {
                    css: compile(css), header: compile(header), container: compile(container), row: compile(row)
                };
                handler();
            });
        };
        var fire = function (grid, type) {
            var args = Array.prototype.slice.call(arguments, 2), events = grid.events[type], lcv, len = events.length;
            for (lcv = 0; lcv < len; lcv += 1)
                if (false === events[lcv].handler.apply(null, events[lcv].args.concat(args))) break;
        };
        var format = function (grid, value, type) {
            return grid.formatter[type] ? grid.formatter[type](value) : value || '';
        };
        var init_data = function (grid, config) {
            grid.alive = function () {return grid.$(grid.id).length ? true : !grid.elements.style.remove();};
            grid.sparklines = !!config.sparklines;
            grid.elements = {empty: true};
            grid.id = '#analytics_grid_' + counter++;
            grid.meta = null;
            grid.resize = set_size.partial(grid, config);
            grid.viewport = set_viewport.partial(grid);
            grid.source = config.source;
            (grid.dataman = new og.analytics.Data(grid.source))
                .on('meta', init_grid, grid, config)
                .on('data', render_rows, grid);
            grid.events = {mousedown: [], scroll: [], render: [], select: []};
            grid.on = function (type, handler) {
                if (type in grid.events)
                    grid.events[type].push({handler: handler, args: Array.prototype.slice.call(arguments, 2)});
                return grid;
            };
            grid.formatter = new og.analytics.Formatter(grid);
            grid.on('render', function () {
                grid.elements.main.find('.node').each(function (idx, val) {
                    var $node = $(this);
                    $node.addClass(grid.meta.nodes[$node.attr('data-row')] ? 'collapse' : 'expand');
                });
            }).on('mousedown', function (event) {
                var $target = $(event.target), row;
                if (!$target.is('.node')) return;
                grid.meta.nodes[row = $target.attr('data-row')] = !grid.meta.nodes[row];
                grid.resize().selector.clear();
                return false;
            }).on('select', function (selection) {
                var row = selection.rows[0], col = selection.cols[0], type = selection.types[0];
                if (!(1 === selection.rows.length && 1 === selection.cols.length)) return; // multiple selection
            });
        };
        var init_elements = function (grid, config, elements) {
            (elements = grid.elements).style = $('<style type="text/css" />').appendTo('head');
            elements.parent = $(config.selector).html(templates.container({id: grid.id.substring(1)}))
                .on('mousedown', function (event) {event.preventDefault(), fire(grid, 'mousedown', event);});
            elements.parent[0].onselectstart = function () {return false;}; // stop selections in IE
            elements.main = $(grid.id);
            elements.fixed_body = $(grid.id + ' .OG-g-b-fixed');
            elements.scroll_body = $(grid.id + ' .OG-g-b-scroll');
            elements.scroll_head = $(grid.id + ' .OG-g-h-scroll');
            elements.fixed_head = $(grid.id + ' .OG-g-h-fixed');
            (function () {
                var started, pause = 200,
                    jump = function () {grid.viewport(function () {grid.dataman.busy(false);}), started = null;};
                elements.fixed_body.on('scroll', (function (timeout) {
                    return function (event) { // sync scroll instantaneously and set viewport after scroll stops
                        if (!started && $(event.target).is(elements.fixed_body)) started = 'fixed';
                        if (started !== 'fixed') return clearTimeout(timeout);
                        grid.dataman.busy(true);
                        elements.scroll_body.scrollTop(elements.fixed_body.scrollTop());
                        timeout = clearTimeout(timeout) || setTimeout(jump, pause);
                    }
                })(null));
                elements.scroll_body.on('scroll', (function (timeout) {
                    return function (event) { // sync scroll instantaneously and set viewport after scroll stops
                        if (!started && $(event.target).is(elements.scroll_body)) started = 'scroll';
                        if (started !== 'scroll') return clearTimeout(timeout);
                        grid.dataman.busy(true);
                        elements.scroll_head.scrollLeft(elements.scroll_body.scrollLeft());
                        elements.fixed_body.scrollTop(elements.scroll_body.scrollTop());
                        timeout = clearTimeout(timeout) || setTimeout(jump, pause);
                    }
                })(null));
            })();
            grid.selector = new og.analytics.Selector(grid).on('select', function (selection) {
                var absolute_selection = {
                    rows: selection.rows.map(function (v) {return grid.meta.available[v];}),
                    cols: selection.cols,
                    types: selection.cols.map(function (v) {return grid.meta.columns.types[v];})
                }
                fire(grid, 'select', absolute_selection);
            });
            og.common.gadgets.manager.register({alive: grid.alive, resize: grid.resize});
            elements.empty = false;
        };
        var init_grid = function (grid, config, metadata) {
            var $ = grid.$, columns = metadata.columns;
            grid.meta = metadata;
            grid.meta.row_height = row_height;
            grid.meta.header_height = header_height;
            grid.meta.fixed_length = grid.meta.columns.fixed
                .reduce(function (acc, set) {return acc + set.columns.length;}, 0);
            grid.meta.scroll_length = grid.meta.columns.fixed
                .reduce(function (acc, set) {return acc + set.columns.length;}, 0);
            grid.meta.columns.types = columns.fixed[0].columns.map(function (col) {return col.type;})
                .concat(columns.scroll.reduce(function (acc, set) {
                    return acc.concat(set.columns.map(function (col) {return col.type;}));
                }, []));
            unravel_structure(grid);
            if (grid.elements.empty) init_elements(grid, config);
            grid.resize();
            render_header(grid);
        };
        var render_header = (function () {
            var head_data = function (meta, sets, col_offset, set_offset) {
                var width = meta.columns.width, index = 0;
                return {
                    width: col_offset ? width.scroll : width.fixed, padding_right: col_offset ? scrollbar_size : 0,
                    sets: sets.map(function (set, idx) {
                        var columns = set.columns.map(function (col) {
                            return {index: (col_offset || 0) + index++, name: col.header, width: col.width};
                        });
                        return {
                            name: set.name, index: idx + (set_offset || 0), columns: columns,
                            width: columns.reduce(function (acc, col) {return acc + col.width;}, 0)
                        };
                    })
                };
            };
            return function (grid) {
                var meta = grid.meta, columns = meta.columns, fixed_sets = meta.columns.fixed.length;
                grid.elements.fixed_head.html(templates.header(head_data(meta, columns.fixed)));
                grid.elements.scroll_head
                    .html(templates.header(head_data(meta, columns.scroll, meta.fixed_length, fixed_sets)));
            };
        })();
        var render_rows = (function () {
            var row_data = function (grid, data, fixed) {
                var meta = grid.meta, fixed_length = meta.fixed_length, i, j, data_len = data.length, slice, slice_len,
                    result = {holder_height: meta.viewport.height + (fixed ? scrollbar_size : 0), rows: []},
                    data_row, grid_row, value, column, cells;
                for (i = 0, grid_row = meta.available.indexOf(meta.viewport.rows[0]); i < data_len; i += 1) {
                    slice = fixed ? data[i].slice(0, fixed_length) : data[i].slice(fixed_length);
                    result.rows.push({top: grid_row++ * row_height, cells: (cells = [])});
                    for (j = 0, slice_len = slice.length, data_row = meta.viewport.rows[i]; j < slice_len; j += 1) {
                        column = meta.viewport.cols[fixed ? j : fixed_length + j];
                        value = format(grid, slice[j], meta.columns.types[column]);
                        cells.push({column: column, value: fixed && !j ? meta.unraveled[data_row] + value : value});
                    }
                }
                return result;
            };
            return function (grid, data) {
                if (grid.dataman.busy()) return; else grid.dataman.busy(true); // don't accept more data if rendering
                grid.elements.fixed_body.html(templates.row(row_data(grid, data, true)));
                grid.elements.scroll_body.html(templates.row(row_data(grid, data, false)));
                fire(grid, 'render');
                grid.dataman.busy(false);
                grid.elements.scroll_body.focus(); // focus on scroll body so arrow keys will work
            };
        })();
        var set_css = function (id, sets, offset) {
            var partial_width = 0,
                columns = sets.reduce(function (acc, set) {return acc.concat(set.columns);}, []),
                total_width = columns.reduce(function (acc, val) {return val.width + acc;}, 0);
            return sets.map(function (set, idx) {
                var set_width = set.columns.reduce(function (acc, val) {return val.width + acc;}, 0), css;
                css = {
                    prefix: id, index: idx + (offset || 0),
                    left: partial_width, right: total_width - partial_width - set_width
                };
                return (partial_width += set_width), css;
            });
        };
        var set_size = function (grid, config) {
            config = config || {};
            var meta = grid.meta, css, width = config.width || grid.elements.parent.width(),
                height = config.height || grid.elements.parent.height(), columns = meta.columns, id = grid.id;
            meta.columns.width = {
                fixed: meta.columns.fixed.reduce(function (acc, set) {
                    return acc + set.columns.reduce(function (acc, col) {return acc + col.width;}, 0);
                }, 0),
                scroll: meta.columns.scroll.reduce(function (acc, set) {
                    return acc + set.columns.reduce(function (acc, col) {return acc + col.width;}, 0);
                }, 0)
            };
            meta.columns.scan = {
                fixed: meta.columns.fixed.reduce(function (acc, set) {
                    return set.columns
                        .reduce(function (acc, col) {return acc.arr.push(acc.val += col.width), acc;}, acc);
                }, {arr: [], val: 0}).arr,
                scroll: meta.columns.scroll.reduce(function (acc, set) {
                    return set.columns
                        .reduce(function (acc, col) {return acc.arr.push(acc.val += col.width), acc;}, acc);
                }, {arr: [], val: 0}).arr
            };
            meta.columns.scan.all = meta.columns.scan.fixed
                .concat(meta.columns.scan.scroll.map(function (val) {return val + meta.columns.width.fixed;}));
            meta.rows = (meta.available = available(grid)).length;
            meta.viewport = {height: meta.rows * row_height, width: width - meta.columns.width.fixed};
            meta.visible_rows = Math.min(Math.ceil((height - header_height) / row_height), meta.rows);
            css = templates.css({
                id: id, viewport_width: meta.viewport.width,
                fixed_bg: background(columns.fixed, meta.columns.width.fixed, 'ecf5fa'),
                scroll_bg: background(columns.scroll, meta.columns.width.scroll, 'ffffff'),
                scroll_width: columns.width.scroll, fixed_width: columns.width.fixed + scrollbar_size,
                scroll_left: columns.width.fixed,
                height: height - header_height, header_height: header_height, row_height: row_height,
                columns: col_css(id, columns.fixed).concat(col_css(id, columns.scroll, meta.fixed_length)),
                sets: set_css(id, columns.fixed).concat(set_css(id, columns.scroll, columns.fixed.length))
            });
            if (grid.elements.style[0].styleSheet) grid.elements.style[0].styleSheet.cssText = css; // IE
            else grid.elements.style[0].appendChild(document.createTextNode(css));
            return grid.viewport();
        };
        var set_viewport = function (grid, handler) {
            var top_position = grid.elements.scroll_body.scrollTop(),
                left_position = grid.elements.scroll_head.scrollLeft(),
                fixed_len = grid.meta.fixed_length,
                row_start = Math.floor((top_position / grid.meta.viewport.height) * grid.meta.rows),
                row_len = grid.meta.visible_rows, row_end = row_start + row_len, lcv = row_start,
                scroll_position = left_position + grid.meta.viewport.width,
                scroll_cols = grid.meta.columns.scroll
                    .reduce(function (acc, set) {return acc.concat(set.columns);}, []);
            grid.meta.viewport.rows = [];
            while (lcv < row_end) grid.meta.viewport.rows.push(grid.meta.available[lcv++]);
            (grid.meta.viewport.cols = []), (lcv = 0);
            while (lcv < fixed_len) grid.meta.viewport.cols.push(lcv++);
            grid.meta.viewport.cols = grid.meta.viewport.cols.concat(scroll_cols.reduce(function (acc, col, idx) {
                if (!('scan' in acc)) return acc;
                if ((acc.scan += col.width) >= left_position) acc.cols.push(idx + fixed_len);
                if (acc.scan > scroll_position) delete acc.scan;
                return acc;
            }, {scan: 0, cols: []}).cols);
            grid.dataman.viewport(grid.meta.viewport);
            if (handler) handler();
            return grid;
        };
        var unravel_structure = (function () {
            var times = function (str, rep) {var result = ''; if (rep) while (--rep) result += str; return result;};
            var unravel = function (arr, indent) {
                var start = arr[0], end = arr[1], children = arr[2], str = '&nbsp;&nbsp;&nbsp;', result = [], prefix;
                result.push(end > start ? {
                    prefix: times(str, indent + 1) + '<span data-row="' + start + '" class="node"></span>&nbsp;',
                    node: true, length: end - start
                } : {prefix: times(str, indent)});
                if (children.length) return children.map(function (child) {return unravel(child, indent + 1);})
                    .forEach(function (child) {Array.prototype.push.apply(result, child);}), result;
                while (start++ < end) result.push({prefix: prefix || (prefix = times(str, indent + 2))});
                return result;
            };
            return function (grid, unraveled) {
                grid.meta.nodes = (unraveled = unravel(grid.meta.structure, 0))
                    .reduce(function (acc, val, idx) {
                        if (val.node) (acc[idx] = true), (acc.all.push(idx)), (acc.ranges.push(val.length));
                        return acc;
                    }, {all: [], ranges: []});
                grid.meta.unraveled = unraveled.pluck('prefix');
            };
        })();
        return function (config, dollar) {
            this.$ = dollar || $; // each grid holds a reference to its frame/window's $
            if (templates) init_data(this, config); else compile_templates(init_data.partial(this, config));
        };
    }
});