/*
 * Copyright 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.analytics.Grid',
    dependencies: ['og.api.text', 'og.common.events', 'og.analytics.Data', 'og.analytics.CellMenu'],
    obj: function () {
        var module = this, counter = 1, row_height = 19, templates = null,
            fire = og.common.events.fire, scrollbar_size = (function () {
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
            return function (meta) {return unravel(meta.nodes, meta.structure, []);};
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
        var compile_templates = function (handler, context) {
            var css = og.api.text({url: module.html_root + 'analytics/grid/og.analytics.grid_tash.css'}),
                header = og.api.text({module: 'og.analytics.grid.header_tash'}),
                container = og.api.text({module: 'og.analytics.grid.container_tash'}),
                row = og.api.text({module: 'og.analytics.grid.row_tash'}), compile = Handlebars.compile;
            $.when(css, header, container, row).then(function (css, header, container, row) {
                templates = {
                    css: compile(css), header: compile(header), container: compile(container), row: compile(row)
                };
                handler.call(context);
            });
        };
        var constructor = function (config, dollar) {
            var grid = this;
            grid.$ = dollar || $; // each grid holds a reference to its frame/window's $
            grid.config = config || {};
            grid.elements = {empty: true};
            grid.events = {
                cellhoverin: [], cellhoverout: [], cellselect: [],
                mousedown: [], rangeselect: [], render: [], scroll: [], select: []
            };
            grid.formatter = new og.analytics.Formatter(grid);
            grid.id = '#analytics_grid_' + counter++;
            grid.meta = null;
            grid.source = config.source;
            grid.updated = (function (last, delta) {
                return function (time) {
                    return time ? (last ? ((delta = time - last), (last = time), delta) : ((last = time), 0)) : delta;
                };
            })(null, 0);
            if (templates) grid.init_data(); else compile_templates(grid.init_data, grid);
        };
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
        constructor.prototype.alive = function () {
            var grid = this, $ = grid.$;
            return $(grid.id).length ? true : !grid.elements.style.remove();
        };
        constructor.prototype.cell = function (selection) {
            var grid = this, cell, viewport = grid.meta.viewport, rows = viewport.rows, cols = viewport.cols;
            return 1 !== selection.rows.length || 1 !== selection.cols.length ? null : {
                row: selection.rows[0], col: selection.cols[0], value: cell = grid
                    .data[rows.indexOf(selection.rows[0]) * cols.length + cols.indexOf(selection.cols[0])],
                type: cell.t || selection.type[0]
            };
        };
        constructor.prototype.init_data = function () {
            var grid = this, $ = grid.$, config = grid.config;
            grid.dataman = new og.analytics.Data(grid.source)
                .on('meta', grid.init_grid, grid)
                .on('data', grid.render_rows, grid);
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
                return false; // kill bubbling if it's a node
            });
        };
        constructor.prototype.init_elements = function () {
            var grid = this, $ = grid.$, config = grid.config, elements, cellmenu,
                last_x, last_y, page_x, page_y, last_corner, cell // cached values for mousemove and mouseleave events;
            (elements = grid.elements).style = $('<style type="text/css" />').appendTo('head');
            elements.parent = $(config.selector).html(templates.container({id: grid.id.substring(1)}))
                .on('mousedown', function (event) {event.preventDefault(), fire(grid.events.mousedown, event);})
                .on('mousemove', '.OG-g-sel, .OG-g-cell', (function () {
                    var resolution = 8, counter = 0; // only accept 1/resolution of the mouse moves, we have too many
                    return function (event) {
                        (page_x = event.pageX), (page_y = event.pageY);
                        if (counter++ % resolution) return;
                        if (counter > resolution) counter = 1;
                        if (grid.selector.busy()) return last_x = last_y = last_corner = null;
                        if (page_x === last_x && page_y === last_y) return;
                        var scroll_left = grid.elements.scroll_body.scrollLeft(),
                            scroll_top = grid.elements.scroll_body.scrollTop(),
                            fixed_width = grid.meta.columns.width.fixed,
                            x = page_x - grid.offset.left + (page_x > fixed_width ? scroll_left : 0),
                            y = page_y - grid.offset.top + scroll_top - grid.meta.header_height, corner, corner_cache,
                            rectangle = {top_left: (corner = grid.nearest_cell(x, y)), bottom_right: corner};
                        if (last_corner === (corner_cache = JSON.stringify(corner))) return;
                        cell = grid.cell(grid.transpose_selection(grid.selector.selection(rectangle)));
                        cell.top = corner.top - scroll_top + grid.meta.header_height;
                        cell.right = corner.right - (page_x > fixed_width ? scroll_left : 0);
                        fire(grid.events.cellhoverin, cell);
                        last_corner = corner_cache; last_x = page_x; last_y = page_y;
                    }
                })(last_x = null, last_y = null, page_x, page_y, last_corner))
                .on('mouseleave', function (event) {
                    if (last_corner) (last_corner = null), fire(grid.events.cellhoverout, cell);
                });
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
                        if (cellmenu) cellmenu.hide();
                        elements.scroll_body.scrollTop(elements.fixed_body.scrollTop());
                        timeout = clearTimeout(timeout) || setTimeout(jump, pause);
                    };
                })(null));
                elements.scroll_body.on('scroll', (function (timeout) {
                    return function (event) { // sync scroll instantaneously and set viewport after scroll stops
                        if (!started && $(event.target).is(elements.scroll_body)) started = 'scroll';
                        if (started !== 'scroll') return clearTimeout(timeout);
                        grid.dataman.busy(true);
                        if (cellmenu) cellmenu.hide();
                        elements.scroll_head.scrollLeft(elements.scroll_body.scrollLeft());
                        elements.fixed_body.scrollTop(elements.scroll_body.scrollTop());
                        timeout = clearTimeout(timeout) || setTimeout(jump, pause);
                    };
                })(null));
            })();
            grid.selector = new og.analytics.Selector(grid).on('select', function (raw) {
                var cell, meta = grid.meta, selection = grid.transpose_selection(raw);
                if (1 === selection.rows.length && 1 === selection.cols.length)
                    fire(grid.events.cellselect, grid.cell(selection));
                else
                    fire(grid.events.rangeselect, selection);
                fire(grid.events.select, selection); // fire for both single and multiple selection
            });
            if (config.cellmenu) cellmenu = new og.analytics.CellMenu(grid);
            og.common.gadgets.manager.register({alive: grid.alive, resize: grid.resize, context: grid});
            elements.empty = false;
        };
        constructor.prototype.init_grid = function (metadata) {
            var grid = this, config = grid.config, columns = metadata.columns;
            grid.meta = metadata;
            grid.meta.row_height = row_height;
            grid.meta.sets_height = config.source.depgraph ? 0 : 24;
            grid.meta.col_lbl_height = 31;
            grid.meta.header_height = grid.meta.sets_height + grid.meta.col_lbl_height;
            grid.meta.fixed_length = grid.meta.columns.fixed
                .reduce(function (acc, set) {return acc + set.columns.length;}, 0);
            grid.meta.scroll_length = grid.meta.columns.fixed
                .reduce(function (acc, set) {return acc + set.columns.length;}, 0);
            grid.meta.columns.types = columns.fixed[0].columns.map(function (col) {return col.type;})
                .concat(columns.scroll.reduce(function (acc, set) {
                    return acc.concat(set.columns.map(function (col) {return col.type;}));
                }, []));
            grid.unravel_structure();
            if (grid.elements.empty) grid.init_elements();
            grid.resize(grid.render_header);
        };
        constructor.prototype.nearest_cell = function (x, y) {
            var grid = this, top, bottom, lcv, scan = grid.meta.columns.scan.all, len = scan.length;
            for (lcv = 0; lcv < len; lcv += 1) if (scan[lcv] > x) break;
            bottom = (Math.floor(y / grid.meta.row_height) + 1) * grid.meta.row_height;
            top = bottom - grid.meta.row_height;
            return {top: top, bottom: bottom, left: scan[lcv - 1] || 0, right: scan[lcv]};
        };
        constructor.prototype.off = og.common.events.off;
        constructor.prototype.on = og.common.events.on;
        constructor.prototype.render_header = (function () {
            var head_data = function (meta, sets, depgraph, col_offset, set_offset) {
                var width = meta.columns.width, index = 0;
                return {
                    width: col_offset ? width.scroll : width.fixed, padding_right: col_offset ? scrollbar_size : 0,
                    sets: sets.map(function (set, idx) {
                        var columns = set.columns.map(function (col) {
                            return {index: (col_offset || 0) + index++, name: col.header, width: col.width};
                        });
                        return {
                            name: set.name, index: idx + (set_offset || 0), columns: columns, depgraph: depgraph,
                            width: columns.reduce(function (acc, col) {return acc + col.width;}, 0)
                        };
                    })
                };
            };
            return function () {
                var grid = this, meta = grid.meta, columns = meta.columns, fixed_sets = meta.columns.fixed.length,
                    depgraph = grid.config.source.depgraph;
                grid.elements.fixed_head.html(templates.header(head_data(meta, columns.fixed, depgraph)));
                grid.elements.scroll_head
                    .html(templates.header(head_data(meta, columns.scroll, depgraph, meta.fixed_length, fixed_sets)));
            };
        })();
        constructor.prototype.render_rows = (function () {
            var row_data = function (grid, data, fixed) {
                var meta = grid.meta, viewport = meta.viewport, fixed_len = meta.fixed_length, i, j, index, data_row,
                    cols = viewport.cols, rows = viewport.rows, grid_row = meta.available.indexOf(rows[0]), value,
                    types = meta.columns.types, type, total_cols = cols.length, formatter = grid.formatter, col_end,
                    row_len = rows.length, col_len = fixed ? fixed_len : total_cols - fixed_len, column, cells,
                    result = {holder_height: viewport.height + (fixed ? scrollbar_size : 0), rows: []};
                for (i = 0; i < row_len; i += 1) {
                    result.rows.push({top: grid_row++ * row_height, cells: (cells = [])});
                    if (fixed) {j = 0; col_end = col_len;} else {j = fixed_len; col_end = col_len + fixed_len;}
                    for (data_row = rows[i]; j < col_end; j += 1) {
                        index = i * total_cols + j; column = cols[j];
                        value = (formatter[type = types[column]] ? formatter[type](data[index]) : data[index]) || '';
                        cells.push({column: column, value: fixed && !j ? meta.unraveled[data_row] + value : value});
                    }
                }
                return result;
            };
            return function (data) {
                var grid = this;
                if (grid.dataman.busy()) return; else grid.dataman.busy(true); // don't accept more data if rendering
                grid.data = data;
                grid.elements.fixed_body.html(templates.row(row_data(grid, data, true)));
                grid.elements.scroll_body.html(templates.row(row_data(grid, data, false)));
                grid.updated(+new Date);
                fire(grid.events.render);
                grid.dataman.busy(false);
                grid.elements.scroll_body.focus(); // focus on scroll body so arrow keys will work
            };
        })();
        constructor.prototype.resize = function (handler) {
            var grid = this, config = grid.config, meta = grid.meta, columns = meta.columns, id = grid.id, css,
                width = config.width || grid.elements.parent.width(), data_width,
                height = config.height || grid.elements.parent.height(), header_height = grid.meta.header_height;
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
            data_width = meta.columns.scan.all[meta.columns.scan.all.length - 1] + scrollbar_size;
            meta.rows = (meta.available = available(grid.meta)).length;
            meta.viewport = {
                height: meta.rows * row_height, width: Math.min(width, data_width) - meta.columns.width.fixed
            };
            meta.visible_rows = Math.min(Math.ceil((height - header_height) / row_height), meta.rows);
            css = templates.css({
                id: id, viewport_width: meta.viewport.width,
                fixed_bg: background(columns.fixed, meta.columns.width.fixed, 'ecf5fa'),
                scroll_bg: background(columns.scroll, meta.columns.width.scroll, 'ffffff'),
                scroll_width: columns.width.scroll, fixed_width: columns.width.fixed + scrollbar_size,
                scroll_left: columns.width.fixed,
                height: height - header_height, header_height: header_height, row_height: row_height,
                sets_height: grid.meta.sets_height,
                columns: col_css(id, columns.fixed).concat(col_css(id, columns.scroll, meta.fixed_length)),
                sets: set_css(id, columns.fixed).concat(set_css(id, columns.scroll, columns.fixed.length))
            });
            if (grid.elements.style[0].styleSheet) grid.elements.style[0].styleSheet.cssText = css; // IE
            else grid.elements.style[0].appendChild(document.createTextNode(css));
            grid.offset = grid.elements.parent.offset();
            return grid.viewport(handler);
        };
        constructor.prototype.transpose_selection = function (raw) {
            var grid = this, meta = grid.meta;
            return {
                cols: raw.cols, rows: raw.rows.map(function (row) {return meta.available[row];}),
                type: raw.cols.map(function (col) {return meta.columns.types[col];})
            };
        };
        constructor.prototype.unravel_structure = (function () {
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
            return function () {
                var grid = this, unraveled;
                grid.meta.nodes = (unraveled = unravel(grid.meta.structure, 0))
                    .reduce(function (acc, val, idx) {
                        if (val.node) (acc[idx] = true), (acc.all.push(idx)), (acc.ranges.push(val.length));
                        return acc;
                    }, {all: [], ranges: []});
                grid.meta.unraveled = unraveled.pluck('prefix');
            };
        })();
        constructor.prototype.viewport = function (handler) {
            var grid = this, meta = grid.meta, viewport = meta.viewport, elements = grid.elements,
                top_position = elements.scroll_body.scrollTop(), left_position = elements.scroll_head.scrollLeft(),
                fixed_len = meta.fixed_length, row_start = Math.floor((top_position / viewport.height) * meta.rows),
                row_len = meta.visible_rows, row_end = Math.min(row_start + row_len, meta.available.length),
                lcv = row_start, scroll_position = left_position + viewport.width, scroll_cols = meta.columns.scroll
                    .reduce(function (acc, set) {return acc.concat(set.columns);}, []);
            viewport.rows = [];
            while (lcv < row_end) viewport.rows.push(meta.available[lcv++]);
            (viewport.cols = []), (lcv = 0);
            while (lcv < fixed_len) viewport.cols.push(lcv++);
            viewport.cols = viewport.cols.concat(scroll_cols.reduce(function (acc, col, idx) {
                if (!('scan' in acc)) return acc;
                if ((acc.scan += col.width) >= left_position) acc.cols.push(idx + fixed_len);
                if (acc.scan > scroll_position) delete acc.scan;
                return acc;
            }, {scan: 0, cols: []}).cols);
            grid.dataman.viewport(viewport);
            if (handler) handler.call(grid);
            return grid;
        };
        return constructor;
    }
});