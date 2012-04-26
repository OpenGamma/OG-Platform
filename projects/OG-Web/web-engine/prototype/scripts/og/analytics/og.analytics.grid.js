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
            var grid = this, id = '#analytics_grid_' + counter++, meta, dataman;
            var set_viewport = function (handler) {
                var viewport = meta.viewport, row_start,
                    top_position = $(id + ' .OG-g-b-fixed').scrollTop(),
                    left_position = $(id + ' .OG-g-h-scroll').scrollLeft(),
                    scroll_position = left_position + viewport.width;
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
            var scroll_observer = (function (timeout, update_viewport, $section, $fixed, $head) {
                return function (event) { // sync scroll instantaneously and set viewport after scroll stops
                    dataman.busy(true);
                    ($fixed || ($fixed = $(id + ' .OG-g-b-fixed'))) // cache DOM reference
                        .scrollTop(($section || ($section = $(id + ' .OG-g-b-scroll'))).scrollTop());
                    ($head || ($head = $(id + ' .OG-g-h-scroll'))) // cache DOM reference
                        .scrollLeft($section.scrollLeft());
                    timeout = clearTimeout(timeout) || setTimeout(update_viewport, 200);
                }
            })(null, set_viewport.partial(function () {dataman.busy(false);}));
            var render_cols = (function () {
                var gen_cols = function (column_set, offset) {
                    var width = meta.columns.width;
                    return templates.header({
                        width: offset ? width.scroll : width.fixed,
                        padding_right: offset ? scrollbar_size : 0,
                        cols: column_set.reduce(function (acc, val, idx) {
                            return acc + '<div class="OG-g-h-col c' + (idx + (offset || 0)) + '">' + val.name +
                                '</div>';
                        }, '')
                    });
                }
                return function () {
                    $(id + ' .OG-g-h-fixed').html(gen_cols(meta.columns.fixed));
                    $(id + ' .OG-g-h-scroll').html(gen_cols(meta.columns.scroll, meta.columns.fixed.length));
                }
            })();
            var render_rows = (function () {
                var gen_rows = function (data, fixed) {
                    var html, columns = meta.columns, viewport = meta.viewport,
                        fixed_length = columns.fixed.length, offset = fixed ? 0 : fixed_length,
                        start = '<div class="OG-g-rows" style="height: ' +
                            (viewport.height + (fixed ? scrollbar_size : 0)) + 'px;">';
                    html = data.reduce(function (acc, row, idx) {
                        var slice = row.slice(fixed ? 0 : fixed_length, fixed ? fixed_length : undefined);
                        acc.push('<div class="OG-g-row" style="top: ' +
                            ((idx + viewport.rows[0]) * row_height) + 'px">');
                        acc.push.apply(acc, slice.reduce(function (acc, val, idx) {
                            return val === null ? acc // don't bother putting undefined things into the DOM
                                : acc.concat('<div class="OG-g-cell c' + (offset + idx) + '">' + val + '</div>');
                        }, []));
                        acc.push('</div>');
                        return acc;
                    }, [start]);
                    html.push('</div>');
                    return html.join('');
                };
                return function (data) {
                    if (dataman.busy()) return; // just bail
                    $(id + ' .OG-g-b-fixed').html(gen_rows(data, true));
                    $(id + ' .OG-g-b-scroll').html(gen_rows(data, false));
                };
            })();
            var initialize = function (metadata) {
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
                    width: config.width - columns.width.fixed
                };
                meta.visible_rows = Math.ceil((config.height - header_height) / row_height);
                $style = load_css(templates.css({
                    id: id,
                    height: config.height - header_height,
                    header_height: header_height,
                    row_height: row_height,
                    viewport_width: meta.viewport.width,
                    scroll_width: columns.width.scroll,
                    fixed_width: columns.width.fixed,
                    columns: col_css(id, columns.fixed)
                        .concat(col_css(id, columns.scroll, columns.fixed.length))
                }));
                $(config.selector)
                    .html(templates.container({id: id.substring(1), height: config.height, width: config.width}));
                $(id + ' .OG-g-b-scroll').scroll(scroll_observer);
                render_cols();
                set_viewport();
                dataman.on('data', render_rows);
                og.common.gadgets.manager.register({
                    alive: function () {return $(id).length ? true : !$style.remove();},
                    resize: function () {console.log('grid resize');}
                });
            };
            $.when(css_tmpl(), header_tmpl(), container_tmpl())
                .then(function (css_tmpl, header_tmpl, container_tmpl) {
                    dataman = new og.analytics.Data;
                    if (!templates) templates = {
                        css: compile(css_tmpl), header: compile(header_tmpl), container: compile(container_tmpl)
                    };
                    dataman.on('init', initialize);
                });
        };
    }
});