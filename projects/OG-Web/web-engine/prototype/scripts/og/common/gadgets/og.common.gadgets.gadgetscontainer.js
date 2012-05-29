/*
 * Copyright 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.common.gadgets.GadgetsContainer',
    dependencies: ['og.common.gadgets.manager', 'og.api.text'],
    obj: function () {
        var api = og.api, tabs_template, overflow_template, dropbox_template, counter = 1,
            header = ' .ui-layout-header', panels = {
                '.OG-layout-analytics-south': 'south',
                '.OG-layout-analytics-dock-north': 'dock-north',
                '.OG-layout-analytics-dock-center': 'dock-center',
                '.OG-layout-analytics-dock-south': 'dock-south'
            },
            extract_pane = function (str) {return str.replace(/^OG\-layout\-analytics\-(.*?)\s(?:.*?)$/, '$1');},
            extract_id = function (str) {return +str.replace(/^og\-tab\-(\d+)\s(?:.*)$/, '$1');};
        /**
         * @param {String} selector Selector to initialize a GadgetsContainer in
         */
        return function (selector) {
            var initialized = false, loading, gadgets = [], container = this,
                pane, // layout pannel
                live_id, // active tab id
                overflow = {}, // document offet of overflow panel
                $overflow_panel; // panel that houses non visible tabs
            var get_index = function (id) {
                return gadgets.reduce(function (acc, val, idx) {return acc + (val.id === id ? idx : 0);}, 0)
            };
            /**
             * @param {Number|Null} id
             *        if id is a Number set the active tab to that ID
             *        if id is undefined use the current Live ID and reflow tabs
             *        if id is null set tabs to a single empty tab
             */
            var update_tabs = function (id) {
                var $header = $(selector + header), tabs;
                /**
                 * @param id Id of gadget to show, hide all others
                 */
                var show_gadget = function (id) {
                    $(selector).find('.OG-gadget-container [class*=OG-gadget-]').hide()
                        .filter('.OG-gadget-' + id).show();
                    live_id = id;
                };
                var is_south_pane_open = function () {
                    return og.views.common.layout.inner.state.south.size !== $('.OG-layout-analytics-south .OG-gadget-tabs').height();
                };
                /** Manages the widths of the tabs when the panel is resized, the following stages exist:
                 *  0 - all tabs are full size
                 *  1 - all but the active tab are truncated
                 *  2 - tabs are truncated plus some/all are moved to an overflow panel
                 *  3 - same as above with active tab being truncated
                 */
                var reflow = function () {
                    var overflow_buffer = 25, // space for the overflow button
                        buttons_buffer = {'south': 23}, // add extra buffer space if the tabs area has buttons
                        min_tab_width = 50,
                        $tabs_container = $(selector + ' .OG-gadget-tabs'),
                        $tabs = $tabs_container.find('li[class^=og-tab-]'),
                        $active_tab = $(selector + ' .og-active'),
                        $overflow_button = $(selector + ' .og-overflow'),
                        active_tab_width = $active_tab.outerWidth(),
                        num_inactive_tabs = $tabs.length - 1,
                        new_tab_width, // new truncated width of a tab
                        compressed_tabs_width, // new full width of all tabs
                        space_needed, // number of pixles needed to reclaim
                        num_tabs_to_hide, // number of tabs to move to overflow
                        $tabs_to_move, // the actual tabs we are moving
                        // full available width
                        full_width = $tabs_container.width() - (overflow_buffer + (buttons_buffer[pane] || 0)),
                        // the full width of all the tabs
                        tabs_width = Array.prototype.reduce.apply($tabs
                            .map(function () {return $(this).outerWidth();}), [function (a, b) {return a + b;}, 0]);
                    // stage 1
                    if (tabs_width > full_width) {
                        new_tab_width = ~~((full_width - active_tab_width) / num_inactive_tabs);
                        new_tab_width = new_tab_width < min_tab_width ? min_tab_width : new_tab_width;
                        compressed_tabs_width = (num_inactive_tabs * new_tab_width) + active_tab_width;
                        // stage 2
                        if (compressed_tabs_width > full_width) {
                            $overflow_button.show().on('click', function (e) {
                                e.stopPropagation();
                                $overflow_panel.toggle();
                                var wins = [window].concat(Array.prototype.slice.call(window.frames));
                                if ($overflow_panel.is(":visible")) $(wins).on('click.overflow_handler', function () {
                                    $overflow_panel.hide();
                                    $(wins).off('click.overflow_handler');
                                });
                                else $(wins).off('click.overflow_handler');
                            });
                            space_needed = full_width - compressed_tabs_width;
                            num_tabs_to_hide = Math.ceil(Math.abs(space_needed) / new_tab_width);
                            if (num_tabs_to_hide) $overflow_panel.find('ul').html(
                                $tabs_to_move = $tabs.filter(':not(.og-active):lt(' + num_tabs_to_hide + ')')
                            );
                            // stage 3
                            if (num_tabs_to_hide >= $tabs.length) $active_tab.width(
                                active_tab_width
                                + (space_needed + ((num_inactive_tabs * min_tab_width) - overflow_buffer))
                                + 'px'
                            );
                        } else {
                            $overflow_panel.hide();
                        }
                        // set inactive tab widths to calculated value
                        $tabs.each(function () {if (!$(this).hasClass('og-active')) $(this).outerWidth(new_tab_width)});
                        // unset width of tabs in overflow panel
                        if ($tabs_to_move) $tabs_to_move.each(function () {$(this).attr('style', '');});
                        // set position of overflow panel
                        overflow.right = $(document).width() - ($overflow_button.offset().left + 25 - 5);
                        overflow.height = $overflow_button.height();
                        overflow.top = $overflow_button.offset().top + overflow.height + 4;
                        $overflow_panel.css({'right': overflow.right + 'px', 'top': overflow.top + 'px'});
                        $tabs.each(function () { // add tooltips to truncated tabs only
                            var $this = $(this);
                            if (!!$this.attr('style')) $this.attr('title', $this.text().trim());
                        });
                    }
                    // implement drag
                    $tabs.each(function (i) {
                        $(this).draggable({
                            revert: function (dropped) {
                                var prefix = 'gadget.ftl#/', url,
                                    index = get_index(extract_id($(this).attr('class'))),
                                    id = gadgets[index].config.options.id,
                                    type = gadgets[index].type;
                                switch (type) {
                                    case 'timeseries': url = prefix + type + '/id=' + id; break;
                                    case 'grid': url = prefix + type + '/' + id; break;
                                }
                                if (!dropped) {
                                    window.open(url);
                                    setTimeout(container.del.partial(gadgets[i]));
                                }
                            },
                            cursor: 'move', zIndex: 3,
                            iframeFix: true, appendTo: 'body', distance: 20,
                            helper: function() {return dropbox_template({label: $(this).text().trim()});}
                        }).data({gadget: gadgets[i], handler: function () {container.del(gadgets[i]);}});
                    });
                };
                if (id === null) $header.html(tabs_template({'tabs': [{'name': 'empty'}]})); // empty tabs
                else {
                    if (id === void 0) id = live_id;
                    tabs = gadgets.reduce(function (acc, val) {
                        return acc.push({
                            'name': val.config.name, 'active': (id === val.id), 'delete': true, 'id': val.id
                        }) && acc;
                    }, []);
                    if (pane === 'south') tabs.toggle = is_south_pane_open() ? 'minimize' : 'maximize'; // add toggle
                    $header.html(tabs_template({'tabs': tabs}));
                    reflow();
                    show_gadget(id);
                }
            };
            container.init = function (arr) {
                /**
                 * @param {String} pane Pane to toggle
                 * @param {Boolean} open_only Don't close, only open
                 */
                var toggle_pane = function (pane, open_only) {
                    var max = '50%', min = $('.OG-layout-analytics-' + pane + ' .OG-gadget-tabs').height(),
                        minimize = og.views.common.layout.inner.sizePane.partial(pane, min),
                        maximize = og.views.common.layout.inner.sizePane.partial(pane, max);
                    og.views.common.layout.inner.state[pane].size === min
                        ? maximize()
                        : open_only ? null : minimize();
                    },
                    toggle_dropbox = function () {
                        if ($('.og-drop').length) { //overlay exists
                            $('.OG-dropbox span').removeClass('og-icon-new-window').addClass('og-icon-drop');
                        } else {
                            $('.OG-dropbox span').removeClass('og-icon-drop').addClass('og-icon-new-window');
                        }
                    };
                loading = true;
                $.when(
                    api.text({module: 'og.analytics.tabs_tash'}),
                    api.text({module: 'og.analytics.tabs_overflow_tash'}),
                    api.text({module: 'og.analytics.dropbox_tash'})
                ).then(function (tabs_tmpl, overflow_tmpl, dropbox_tmpl) {
                    pane = panels[selector];
                    if (!tabs_template) tabs_template = Handlebars.compile(tabs_tmpl);
                    if (!overflow_template) overflow_template = Handlebars.compile(overflow_tmpl);
                    if (!dropbox_template) dropbox_template = Handlebars.compile(dropbox_tmpl);
                    if (!$overflow_panel) $overflow_panel = $(overflow_template({pane: pane})).appendTo('body');
                    initialized = true;
                    loading = false;
                    // setup click handlers
                    $(selector + header + ' , .og-js-overflow-' + pane)
                        // handler for tabs (including the ones in the overflow pane)
                        .on('click', 'li[class^=og-tab-]', function (e) {
                            var id = extract_id($(this).attr('class')), index = get_index(id);
                            if ($(e.target).hasClass('og-delete')) container.del(gadgets[index]);
                            else if (!$(this).hasClass('og-active')) {
                                update_tabs(id || null);
                                if (id) gadgets[index].gadget.resize();
                            }
                            if (pane === 'south') toggle_pane(pane, true);
                        })
                        // handler for min/max toggle button
                        .on('click', '.og-js-toggle', function () {if (pane === 'south') toggle_pane(pane);});
                    if (!arr) update_tabs(null); else container.add(arr);
                    // implement drop
                    $('.OG-layout-analytics-' + pane).droppable({
                        hoverClass: 'og-drop',
                        accept: function (draggable) {
                            var pane = extract_pane($(this).attr('class')),
                                pane_class = 'OG-layout-analytics-' + pane,
                                overflow_class = 'og-js-overflow-' + pane,
                                has_ancestor = function (elm, sel) {
                                    return $(elm).parentsUntil('.' + sel).parent().hasClass(sel);
                                };
                            return $(draggable).is('li[class^=og-tab-]') // is it a tab...
                                && !has_ancestor(draggable, pane_class) // that isnt a child of the active pane...
                                && !has_ancestor(draggable, overflow_class);
                        },
                        over: function () {setTimeout(toggle_dropbox)}, // can't guarantee over and out fire in correct
                        out: function () {setTimeout(toggle_dropbox)},  // order, toggle function seems to solve issue
                        drop: function(e, ui) {
                            var data = ui.draggable.data(), gadget = data.gadget.config.options;
                            gadget.selector = gadget.selector.replace(/analytics\-(.*?)\s/, 'analytics-' + pane + ' ');
                            container.add([data.gadget.config]);
                            setTimeout(data.handler); // setTimeout to ensure handler is called after drag evt finishes
                        }
                    });
                    og.common.gadgets.manager.register(container);
                });
            };
            /**
             * @param {String|Array} data A String that defines what gadgets to load, or an Array of gadgets to load
             *
             * The data Array is a list of objects that describe the gadgets to load
             *     obj.gadget   Function
             *     obj.options  Object
             *     obj.name     String
             *     obj.margin   Boolean
             */
            container.add = function (data) {
                var panel_container = selector + ' .OG-gadget-container', new_gadgets, arr;
                if (!loading && !initialized)
                    return container.init(), setTimeout(container.add.partial(data), 10), container;
                if (!initialized) return setTimeout(container.add.partial(data), 10), container;
                if (!selector) throw new TypeError('GadgetsContainer has not been initialized');
                if (data === void 0) return; // no gadgets for this container
                var generate_arr = function (data) { // create gadgets object array from url args using default settings
                    var obj, type, gadgets = [], options = {};
                    obj = data.split(';').reduce(function (acc, val) {
                        var data = val.split(':');
                        acc[data[0]] = data[1].split(',');
                        return acc;
                    }, {});
                    // TODO: move default options to gadgets
                    options.t = function (id) {
                        return {
                            gadget: 'og.common.gadgets.timeseries',
                            options: {id: id, datapoints_link: false, child: true},
                            name: 'Timeseries ' + id,
                            margin: true
                        }
                    };
                    options.g = function (id) {
                        return {gadget: 'og.analytics.Grid', name: 'grid ' + id, options: {}}
                    };
                    for (type in obj) if (obj.hasOwnProperty(type))
                        obj[type].forEach(function (val) {gadgets.push(options[type](val));});
                    return gadgets;
                };
                arr = typeof data === 'string' ? generate_arr(data) : data;
                new_gadgets = arr.map(function (obj) {
                    var id, gadget_class = 'OG-gadget-' + (id = counter++), gadget,
                        gadget_selector = panel_container + ' .' + gadget_class,
                        options = $.extend(true, obj.options || {}, {selector: gadget_selector}),
                        constructor = obj.gadget.split('.').reduce(function (acc, val) {return acc[val];}, window),
                        type = obj.gadget.replace(/^[a-z0-9.-_]+\.([a-z0-9.-_]+?)$/, '$1').toLowerCase();
                    $(panel_container)
                        .append('<div class="' + gadget_class + '" />')
                        .find('.' + gadget_class)
                        .css({height: '100%', margin: obj.margin ? 10 : 0});
                    gadgets.push(gadget = {id: id, config: obj, type: type, gadget: new constructor(options)});
                    return gadget;
                });
                update_tabs(new_gadgets[new_gadgets.length - 1].id);
                return container;
            };
            container.del = function (obj) {
                var id;
                $(selector + ' .OG-gadget-container .OG-gadget-' + obj.id).remove();
                gadgets.splice(gadgets.indexOf(obj), 1);
                id = gadgets.length
                    ? live_id === obj.id ? gadgets[gadgets.length - 1].id : live_id
                    : null;
                if (id) gadgets[get_index(id)].gadget.resize();
                update_tabs(id); // new active tab or empty
            };
            container.alive = function () {return !!$(selector).length;};
            container.resize = function () {
                gadgets.forEach(function (obj) {
                    if (obj.id === live_id) {
                        $(selector + ' .ui-layout-content').show();
                        update_tabs();
                        obj.gadget.resize();
                    }
                });
            }
        };
    }
});