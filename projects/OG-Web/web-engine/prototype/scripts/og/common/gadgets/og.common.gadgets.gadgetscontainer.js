/*
 * @copyright 2011 - present by OpenGamma Inc
 * @license See distribution for license
 */
$.register_module({
    name: 'og.common.gadgets.GadgetsContainer',
    dependencies: ['og.common.gadgets.manager', 'og.api.text'],
    obj: function () {
        var api = og.api, tabs_template, overflow_template, header = ' .ui-layout-header', panels = {
                '.OG-layout-analytics-south': 'south',
                '.OG-layout-analytics-dock-north': 'dock-north',
                '.OG-layout-analytics-dock-center': 'dock-center',
                '.OG-layout-analytics-dock-south': 'dock-south'
            };
        /**
         * @param {String} selector Selector to initialize a GadgetsContainer in
         */
        return function (selector) {
            var initialized = false, loading, counter = 1, gadgets = [], container = this,
                pane, // layout pannel
                live_id, // active tab id
                overflow = {}, // document offet of overflow panel
                $overflow_panel; // panel that houses non visible tabs
            /**
             * @param {Number|Null} id
             *        if id is a Number set the active tab to that ID
             *        if id is undefined use the current Live ID and reflow tabs
             *        if id is null set tabs to a single empty tab
             */
            var update_tabs = function (id) {
                var $header = $(selector + header), tabs, reflow;
                /** Manages the widths of the tabs when the panel is resized, the following stages exist:
                 *  0 - all tabs are full size
                 *  1 - all but the active tab are truncated
                 *  2 - tabs are truncated plus some/all are moved to an overflow panel
                 *  3 - same as above with active tab being truncated
                 */
                reflow = function () {
                    var overflow_buffer = 25, // space for the overflow button
                        buttons_buffer = {'south': 23}, // add extra buffer space if the tabs area has buttons
                        min_tab_width = 30,
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
                                if ($overflow_panel.is(":visible")) $(window).on('click.overflow_handler', function () {
                                    $overflow_panel.hide();
                                    $(window).off('click.overflow_handler');
                                });
                                else $(window).off('click.overflow_handler');
                                // attach global click handler
                                // close overflow panel on body click
                                // remove global click handler
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
                        if ($tabs_to_move) $tabs_to_move.each(function () {$(this).width('auto');});
                        // set position of overflow panel
                        overflow.right = $(document).width() - ($overflow_button.offset().left + 25 - 5);
                        overflow.height = $overflow_button.height();
                        overflow.top = $overflow_button.offset().top + overflow.height + 4;
                        $overflow_panel.css({'right': overflow.right + 'px', 'top': overflow.top + 'px'});
                    }
                };
                if (id === null) $header.html(tabs_template({'tabs': [{'name': 'empty'}]})); // empty tabs
                else {
                    if (id === void 0) id = live_id;
                    tabs = gadgets.reduce(function (acc, val) {
                        return acc.push({
                            'name': val.name, 'active': (id === val.id), 'delete': true, 'id': val.id
                        }) && acc;
                    }, []);
                    if (pane === 'south') tabs.toggle = true; // add min/max toggle button
                    $header.html(tabs_template({'tabs': tabs}));
                    reflow();
                    show_gadget(id);
                }
            };
            /**
             * @param id Id of gadget to show, hide all others
             */
            var show_gadget = function (id) {
                $(selector).find('.OG-gadget-container [class*=OG-gadget-]')
                    .hide().filter('.OG-gadget-' + id).show();
                live_id = id;
            };
            /**
             * @param {String} pane Pane to toggle
             * @param {Boolean} open_only Don't close, only open
             */
            var toggle_pane = function (pane, open_only) {
                var max = '50%', min = $('.OG-layout-analytics-' + pane + ' .OG-gadget-tabs').height(),
                    minimize = function () {
                        og.views.common.layout.inner.sizePane(pane, min);
                        $('.OG-layout-analytics-' + pane + ' .og-js-toggle')
                            .toggleClass('og-icon-minimize og-icon-maximize');
                    },
                    maximize = function () {
                        og.views.common.layout.inner.sizePane(pane, max);
                        $('.OG-layout-analytics-' + pane + ' .og-js-toggle')
                            .toggleClass('og-icon-minimize og-icon-maximize');
                    };
                og.views.common.layout.inner.state[pane].size === min
                        ? maximize()
                        : open_only ? null : minimize();
            };
            container.init = function (arr) {
                loading = true;
                $.when(
                    api.text({module: 'og.analytics.tabs_tash'}),
                    api.text({module: 'og.analytics.tabs_overflow_tash'})
                ).then(function (tabs_tmpl, overflow_tmpl) {
                    pane = panels[selector];
                    if (!tabs_template) tabs_template = Handlebars.compile(tabs_tmpl);
                    if (!overflow_template) overflow_template = Handlebars.compile(overflow_tmpl);
                    if (!$overflow_panel) $overflow_panel = $(overflow_template({pane: pane})).appendTo('body');
                    initialized = true;
                    loading = false;
                    // setup click handlers
                    $(selector + header + ' , .og-js-overflow-' + pane)
                        // handler for tabs (including the ones in the overflow pane)
                        .on('click', 'li[class^=og-tab-]', function () {
                            if (!$(this).hasClass('og-active')) {
                                var id = +$(this).attr('class').replace(/og\-tab\-/, '');
                                update_tabs(id);
                                gadgets[id - 1].gadget.resize();
                            }
                            if (pane === 'south') toggle_pane(pane, true);
                        })
                        // handler for min/max toggle button
                        .on('click', '.og-js-toggle', function () {if (pane === 'south') toggle_pane(pane);});
                    if (!arr) update_tabs(null); else container.add(arr);
                    og.common.gadgets.manager.register(container);
                });
            };
            /**
             * @param {Array}          arr An array of gadget configuration objects
             * @param {Function}       arr[x].gadget
             * @param {Object}         arr[x].options
             * @param {String}         arr[x].name Tab name
             * @param {Boolean}        arr[x].margin Add margin to the container
             */
            container.add = function (arr) {
                if (!loading && !initialized)
                    return container.init(), setTimeout(container.add.partial(arr), 10), container;
                if (!initialized) return setTimeout(container.add.partial(arr), 10), container;
                if (!selector) throw new TypeError('GadgetsContainer has not been initialized');
                var panel_container = selector + ' .OG-gadget-container', new_gadgets;
                new_gadgets = arr.map(function (obj) {
                    var id, gadget_class = 'OG-gadget-' + (id = counter++), gadget,
                        gadget_selector = panel_container + ' .' + gadget_class;
                    $(panel_container)
                        .append('<div class="' + gadget_class + '" />')
                        .find('.' + gadget_class)
                        .css({height: '100%', margin: obj.margin ? 10 : 0});
                    gadgets.push(gadget = {
                        id: id,
                        name: obj.name,
                        gadget: new obj.gadget($.extend(true, obj.options, {selector: gadget_selector}))
                    });
                    return gadget;
                });
                update_tabs(new_gadgets[new_gadgets.length - 1].id);
                return container;
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