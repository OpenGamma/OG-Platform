/*
 * @copyright 2012 - present by OpenGamma Inc
 * @license See distribution for license
 */
$.register_module({
    name: 'og.analytics.Grid',
    dependencies: ['og.api.text', 'og.analytics.Data'],
    obj: function () {
        var module = this, api_text = og.api.text, counter = 1, compile = Handlebars.compile,
            css_tmpl = api_text.partial({url: module.html_root + 'analytics/grid/og.analytics.grid_tash.css'}),
            header_tmpl = api_text.partial({module: 'og.analytics.grid.header_tash'}),
            container_tmpl = api_text.partial({module: 'og.analytics.grid.container_tash'}),
            templates = null, scrollbar_size = 19, header_height = 49, row_height = 19;
        var col_css = function (id, columns, col_offset) {
            var partial_width = 0, total_width = columns.reduce(function (acc, val) {return +val.width + acc;}, 0);
            return columns.map(function (val, idx) {
                var width = +val.width, css = {
                    prefix: id, selector: 'c' + (idx + (col_offset || 0)),
                    left: partial_width, right: total_width - partial_width - width
                };
                return (partial_width += width), css;
            });
        };
        var load_css = function (css) {
            var $style = $('<style type="text/css" />').appendTo($('head'));
            if ($style[0].styleSheet) return $style[0].styleSheet.cssText = css; // IE
            $style[0].appendChild(document.createTextNode(css));
            return $style;
        };
        return function (config) {
            var grid = this, selector = config.selector, id = '#analytics_grid_' + counter++,
                total_width = config.width, total_height = config.height, meta, dataman,
                $fixed, $head, top = 'scrollTop', left = 'scrollLeft';
            var set_viewport = function (handler) {
                var viewport = meta.viewport,
                    top_position = ($fixed || ($fixed = $(id + ' .OG-g-b-fixed')))[top](),
                    left_position = ($head || ($head = $(id + ' .OG-g-h-scroll')))[left](),
                    scroll_position = left_position + viewport.width,
                    row_start, partial_left;
                viewport.rows = [
                    (row_start = Math.floor((top_position / viewport.height) * meta.rows)),
                    row_start + meta.visible_rows
                ];
                viewport.cols = meta.columns.scroll.reduce(function (acc, val, idx) {
                    if (!('scan' in acc)) return acc;
                    if ((acc.scan += val.width) >= left_position) acc.cols.push(idx + meta.columns.fixed.length);
                    if (acc.scan > scroll_position) delete acc.scan;
                    return acc;
                }, {scan: 0, cols: []}).cols;
                dataman.viewport(viewport);
                if (handler) handler();
            };
            var scroll_observer = (function (timeout, update_viewport) {
                return function (event) { // sync scroll
                    dataman.busy(true);
                    var $section = $(id + ' .OG-g-b-scroll');
                    $fixed[top]($section[top]());
                    $head[left]($section[left]());
                    timeout = clearTimeout(timeout) || setTimeout(update_viewport, 200);
                }
            })(null, set_viewport.partial(function () {dataman.busy(false);}));
            $.when(css_tmpl(), header_tmpl(), container_tmpl()).then(function (css_tmpl, header_tmpl, container_tmpl) {
                var alive, resize, render_rows, render_cols;
                dataman = new og.analytics.Data;
                if (!templates) templates = {
                    css: compile(css_tmpl), header: compile(header_tmpl), container: compile(container_tmpl)
                };
                render_cols = function () {
                    var width = meta.columns.width, gen_cols = function (column_set, start_at) {
                        return templates.header({
                            width: start_at ? width.scroll : width.fixed,
                            padding_right: start_at ? scrollbar_size : 0,
                            cols: column_set.reduce(function (acc, val, idx) {
                                return acc + '<div class="OG-g-h-col c' + (idx + (start_at || 0)) + '">' + val.name +
                                    '</div>';
                            }, '')
                        });
                    };
                    $(id + ' .OG-g-h-fixed').html(gen_cols(meta.columns.fixed));
                    $(id + ' .OG-g-h-scroll').html(gen_cols(meta.columns.scroll, meta.columns.fixed.length));
                };
                render_rows = function (data) {
                    if (dataman.busy()) return; // just bail
                    var gen_rows = function (fixed) {
                        var html, columns = meta.columns, col_from = fixed ? 0 : columns.fixed.length,
                            viewport = meta.viewport,
                            col_to = fixed ? columns.fixed.length : columns.fixed.length + columns.scroll.length,
                            start = '<div class="OG-g-rows" style="height: ' +
                                (viewport.height + (fixed ? scrollbar_size : 0)) + 'px">',
                            top_offset = Math.floor(viewport.rows[0] / meta.rows) * row_height;
                        html = data.reduce(function (acc, row, idx) {
                            var top = (idx + viewport.rows[0]) * row_height;
                            acc.push('<div class="OG-g-row r'+ idx +'" style="top: ' + top + 'px">');
                            acc.push.apply(acc, row.slice(col_from, col_to).reduce(function (acc, val, idx) {
                                if (!val) return acc; // don't bother putting undefined things into the DOM
                                return acc.concat('<div class="OG-g-cell c' + (col_from + idx) + '">' + val + '</div>');
                            }, []));
                            acc.push('</div>\n\n');
                            return acc;
                        }, [start]);
                        html.push('</div>');
                        return html.join('');
                    };
                    $(id + ' .OG-g-b-fixed').html(gen_rows(true));
                    $(id + ' .OG-g-b-scroll').html(gen_rows(false));
                };
                initialize = function (metadata) {
                    var columns = metadata.columns, $style;
                    columns.width = (function () { // width of fixed and scrollable column areas
                        return {
                            fixed: columns.fixed.reduce(function (acc, val) {return acc + val.width;}, 0),
                            scroll: columns.scroll.reduce(function (acc, val) {return acc + val.width;}, 0)
                        };
                    })();
                    grid.meta = meta = metadata; // set instance-wide reference
                    grid.meta.viewport = {
                        height: meta.rows * row_height,
                        width: total_width - columns.width.fixed
                    };
                    meta.visible_rows = Math.ceil((total_height - header_height) / row_height);
                    $style = load_css(templates.css({
                        id: id,
                        height: total_height - header_height,
                        header_height: header_height,
                        row_height: row_height,
                        viewport_width: meta.viewport.width,
                        scroll_width: columns.width.scroll,
                        fixed_width: columns.width.fixed,
                        columns: col_css(id, columns.fixed)
                            .concat(col_css(id, columns.scroll, columns.fixed.length))
                    }));
                    $(selector)
                        .html(templates.container({id: id.substring(1), height: total_height, width: total_width}));
                    $(id + ' .OG-g-b-scroll').scroll(scroll_observer);
                    render_cols();
                    set_viewport();
                    dataman.on('data', render_rows);
                    og.common.gadgets.manager.register({
                        alive: function () {return $(id).length ? true : !$style.remove();},
                        resize: function () {console.log('grid resize');}
                    });
                };
                dataman.on('init', initialize);
            });
        };
    }
});