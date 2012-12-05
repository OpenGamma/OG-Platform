/*
 * Copyright 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.common.gadgets.GadgetsContainer',
    dependencies: ['og.common.gadgets.manager', 'og.api.text'],
    obj: function () {
        var api = og.api, tabs_template, overflow_template, dropbox_template, typemenu_template,
        counter = 1, header = ' .ui-layout-header';
        var constructor = function (selector_prefix, pane) {
            var initialized = false, loading, gadgets = [], container = this, highlight_timer,
                selector = selector_prefix + pane, $selector = $(selector),
                class_prefix = selector_prefix.substring(1),
                live_id, // active tab id
                overflow = {}, // document offset of overflow panel
                $overflow_panel; // panel that houses non visible tabs
            var extract_id = function (str) {return +str.replace(/^og\-tab\-(\d+)\s(?:.*)$/, '$1');};
            var extract_index = function (id) {
                return gadgets.reduce(function (acc, val, idx) {return acc + (val.id === id ? idx : 0);}, 0);
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
                    $selector.find('.OG-gadget-container [class*=OG-gadget-]').hide()
                        .filter('.OG-gadget-' + id).show();
                    live_id = id;
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
                        num_tabs = $tabs.length,
                        num_inactive_tabs = num_tabs - 1,
                        new_tab_width, // new truncated width of a tab
                        compressed_tabs_width, // new full width of all tabs
                        space_needed, // number of pixles needed to reclaim
                        num_tabs_to_hide, // number of tabs to move to overflow
                        $tabs_to_move, // the actual tabs we are moving
                        // full available width
                        full_width = $tabs_container.width() - (overflow_buffer + (buttons_buffer[pane] || 0)),
                        // the full width of all the tabs
                        tabs_width = Array.prototype.reduce.apply($tabs
                            .map(function () {return $(this).outerWidth();}), [function (a, b) {return a + b;}, 0]),
                        new_window = function (i, dropped) {
                            var index = extract_index(extract_id($(this).attr('class')));
                            if (!dropped) {
                                container.fire('launch', gadgets[index].config);
                                setTimeout(container.del.partial(gadgets[i]));
                            }
                        };
                    // stage 1
                    if (tabs_width > full_width) {
                        new_tab_width = ~~((full_width - active_tab_width) / num_inactive_tabs);
                        new_tab_width = new_tab_width < min_tab_width ? min_tab_width : new_tab_width;
                        compressed_tabs_width = (num_inactive_tabs * new_tab_width) + active_tab_width;
                        // stage 2
                        if (compressed_tabs_width > full_width) {
                            if (num_tabs > 1) {
                                $overflow_button.show().on('click', function (e) {
                                    e.stopPropagation();
                                    $overflow_panel.toggle();
                                    var wins = [window].concat(Array.prototype.slice.call(window.frames));
                                    if ($overflow_panel.is(':visible'))
                                        $(wins).on('click.overflow_handler', function () {
                                            $overflow_panel.hide();
                                            $(wins).off('click.overflow_handler');
                                        });
                                    else $(wins).off('click.overflow_handler');
                                });
                            }
                            space_needed = full_width - compressed_tabs_width;
                            num_tabs_to_hide = Math.ceil(Math.abs(space_needed) / new_tab_width);
                            if (num_tabs_to_hide) $overflow_panel.find('ul').html(
                                $tabs_to_move = $tabs.filter(':not(.og-active):lt(' + num_tabs_to_hide + ')')
                            );
                            // stage 3
                            if (num_tabs_to_hide >= num_tabs) $active_tab.width(
                                active_tab_width
                                + (space_needed + ((num_inactive_tabs * min_tab_width) - overflow_buffer)) + 'px'
                            );
                        } else {
                            $overflow_panel.hide();
                        }
                        // set inactive tab widths to calculated value
                        $tabs.each(function () {if (!$(this).hasClass('og-active')) {
                            var original_width = $(this).outerWidth();
                            $(this).outerWidth(original_width < new_tab_width ? original_width : new_tab_width);
                        }});
                        // unset width of tabs in overflow panel
                        if ($tabs_to_move) $tabs_to_move.each(function () {$(this).attr('style', '');});
                        // set position of overflow panel
                        overflow.right = $(document).width() - ($overflow_button.offset().left + 25 - 5);
                        overflow.height = $overflow_button.height();
                        overflow.top = $overflow_button.offset().top + overflow.height + 1;
                        $overflow_panel.css({'right': overflow.right + 'px', 'top': overflow.top + 'px'});
                        $tabs.each(function () { // add tooltips to truncated tabs only
                            var $this = $(this);
                            if (!!$this.attr('style')) $this.find('.OG-gadget-tabs-label')
                                .attr('title', $this.text().replace(/\s+/g , ' ').trim());
                        });
                    }
                    // implement drag
                    $tabs.each(function (i) {
                        $(this).draggable({
                            cursor: 'move', zIndex: 3, cursorAt: {top: 25}, scroll: false,
                            iframeFix: true, appendTo: 'body', distance: 20,
                            revert: new_window.partial(i),
                            stop: function () {$(this).draggable('option','revert', new_window.partial(i));},
                            helper: function () {return dropbox_template({label: $(this).text().trim()});}
                        }).data({
                            gadget: function () {return gadgets[i];},
                            handler: function () {container.del(gadgets[i]);},
                            source: pane
                        });
                    });
                };
                if (id === null) $header.html(tabs_template({'tabs': [{'name': 'empty'}]})); // empty tabs
                else {
                    if (id === void 0) id = live_id;
                    tabs = gadgets.reduce(function (acc, val, idx) {
                        return acc.push({
                            'gadget_type': val.config.gadget_type, 'row_name': val.config.row_name, 'delete': true,
                            'col_name': val.config.col_name, 'active': gadgets[idx].active = id === val.id,
                            'id': val.id, 'data_type': val.config.data_type, 'gadget_name': val.config.gadget_name,
                            'gadget': val, 'gadget_index': idx
                        }) && acc;
                    }, []);
                    $header.html(tabs_template({'tabs': tabs}));
                    $.each(tabs, function (key, val) {
                        var menu_config, menu_template, menu, $icon,
                            depgraph = val.gadget.config.options.source.depgraph,
                            tmpl_data = og.common.gadgets.mapping.available_types(val.data_type, depgraph);
                        menu_template = typemenu_template(tmpl_data);
                        menu_config = {$cntr: $('.og-tab-' + val.id + ' .OG-multiselect'), tmpl: menu_template};
                        menu = new og.common.util.ui.DropMenu(menu_config);
                        menu.$dom.toggle.on('mousedown', function () {
                            menu.toggle_handler();
                            return false;
                        });
                        $icon = $('<div class="OG-icon og-icon-' + val.gadget_type + '"></div>')
                            .css({width: '13px', height: '14px'});
                        menu.$dom.menu.on('mousedown', '.og-js-icon', function () {
                            var gadget_type = $(this).attr('data-gadget_type'), $icon,
                                gadget_name = $(this).attr('data-gadget_name'), swap_config;
                            menu.close();
                            if (gadget_type === val.gadget_type) return false;
                            $icon = $('<div class="OG-icon og-icon-' + (val.gadget_type = gadget_type) + '"></div>')
                                .css({width: '13px', height: '14px'});
                            menu.$dom.toggle.html($icon);
                            swap_config = {
                                gadget: 'og.common.gadgets.' + gadget_type, gadget_name: gadget_name,
                                options: val.gadget.config.options, gadget_type: gadget_type,
                                col_name: val.gadget.config.col_name, data_type: val.gadget.config.data_type,
                                row_name: val.gadget.config.row_name
                            };
                            if (false !== container.fire('swap', swap_config, val.gadget_index))
                                container.add([swap_config], val.gadget_index, true);
                            return false;
                        });
                        menu.$dom.toggle.html($icon);
                    });
                    reflow();
                    show_gadget(id);
                }
            };
            /**
             * @param {String|Array} data A String that defines what gadgets to load, or an Array of gadgets to load
             * @param {Number} index location to add new gadgets
             *
             * The data Array is a list of objects that describe the gadgets to load
             *     obj.gadget   Function
             *     obj.options  Object
             *     obj.name     String
             *     obj.margin   Boolean
             */
            container.add = function (data, index, swap) {
                var panel_container = selector + ' .OG-gadget-container', new_gadgets;
                if (!loading && !initialized)
                    return container.init(), setTimeout(container.add.partial(data, index), 10), container;
                if (!initialized) return setTimeout(container.add.partial(data, index), 10), container;
                if (!data) return container; // no gadgets for this container
                if (!selector) throw new TypeError('GadgetsContainer has not been initialized');
                new_gadgets = data.map(function (obj, idx) {
                    var id, gadget_class = 'OG-gadget-' + (id = counter++), gadget,
                        options = $.extend(true, obj.options || {}, {selector: panel_container + ' .' + gadget_class}),
                        constructor = obj.gadget.split('.').reduce(function (acc, val) {return acc[val];}, window),
                        type = obj.gadget.replace(/^[a-z0-9.-_]+\.([a-z0-9.-_]+?)$/, '$1').toLowerCase();
                    $(panel_container).append('<div class="' + gadget_class + '" />').find('.' + gadget_class).css({
                        position: 'absolute', top: 0, bottom: 0, left: 0, right: 0,
                        display: idx === data.length - 1 ? 'block' : 'none'
                    });
                    gadget = {id: id, config: obj, type: type, gadget: new constructor(options)};
                    if (swap && gadgets[index])
                        $(selector + ' .OG-gadget-container .OG-gadget-' + gadgets[index].id).remove();
                    if (typeof index === 'number') {
                        if (gadgets[index]) gadgets[index].gadget.alive();
                        gadgets.splice(index, 1, gadget);
                    } else gadgets.push(gadget);
                    if (obj.fingerprint) gadget.fingerprint = obj.fingerprint;
                    return gadget;
                });
                if (!swap) update_tabs(new_gadgets[new_gadgets.length - 1].id);
                return container;
            };
            container.alive = function () {
                gadgets.forEach(function (obj) {obj.gadget.alive();});
                return $(selector).length ? true : !$('.og-js-overflow-' + pane).remove(); // clean up overflow panel
            };
            container.del = function (obj, silent) {
                var id, index = gadgets.indexOf(obj);
                $(selector + ' .OG-gadget-container .OG-gadget-' + obj.id).remove();
                gadgets[gadgets.length - 1].gadget.alive();
                gadgets.splice(index, 1);
                id = gadgets.length
                    ? live_id === obj.id ? gadgets[gadgets.length - 1].id : live_id
                    : null;
                if (!silent && id) gadgets[extract_index(id)].gadget.resize();
                update_tabs(id); // new active tab or empty
                if (!silent) container.fire('del', index);
            };
            container.gadgets = function () {return gadgets;};
            /**
             * Highlight gadget panel with number
             * @param show {Boolean} turn on / off
             * @param strong {Boolean} set to active panel
             */
            container.highlight = function (show, strong) {
                var number = ({'south': 1, 'dock-north': 2, 'dock-center': 3, 'dock-south': 4})[pane],
                    query = $selector.find('.og-highlight'), $html = query.length ? query
                        : $('<div />').text(number).addClass('og-highlight').appendTo($selector);
                clearTimeout(highlight_timer);
                if (show) (strong ? $html.addClass('strong') : $html.removeClass('strong')).show();
                else clearTimeout(highlight_timer), highlight_timer = setTimeout(function () {$html.hide();}, 250);
            };
            container.init = function (data) {
                var toggle_dropbox = function () {
                        var $db = $('.og-drop').length, $dbs_span = $('.OG-dropbox span');
                        if ($db) $dbs_span.removeClass('og-icon-new-window').addClass('og-icon-drop');
                        else $dbs_span.removeClass('og-icon-drop').addClass('og-icon-new-window');
                    };
                loading = true;
                $.when(
                    api.text({module: 'og.analytics.tabs_tash'}),
                    api.text({module: 'og.analytics.tabs_overflow_tash'}),
                    api.text({module: 'og.analytics.dropbox_tash'}),
                    api.text({module: 'og.analytics.typemenu_tash'})
                ).then(function (tabs_tmpl, overflow_tmpl, dropbox_tmpl, typemenu_tmpl) {
                    if (!tabs_template) tabs_template = Handlebars.compile(tabs_tmpl);
                    if (!overflow_template) overflow_template = Handlebars.compile(overflow_tmpl);
                    if (!dropbox_template) dropbox_template = Handlebars.compile(dropbox_tmpl);
                    if (!typemenu_template) typemenu_template = Handlebars.compile(typemenu_tmpl);
                    if (!$overflow_panel) $overflow_panel = $(overflow_template({pane: pane})).appendTo('body');
                    initialized = true;
                    loading = false;
                    // setup click handlers
                    $(selector + header + ' , .og-js-overflow-' + pane)
                        // handler for tabs (including the ones in the overflow pane)
                        .on('click', 'li[class^=og-tab-]', function (e) {
                            var id = extract_id($(this).attr('class')), menu, index = extract_index(id);
                            if ($(e.target).hasClass('og-delete')) container.del(gadgets[index]);
                            else if (!$(this).hasClass('og-active')) {
                                update_tabs(id || null);
                                if (id) gadgets[index].gadget.resize();
                            }
                        });
                    if (!data) update_tabs(null); else container.add(data);
                    // implement drop
                    $selector.droppable({
                        hoverClass: 'og-drop',
                        accept: function (draggable) {return $(draggable).is('li[class^=og-tab-]');}, // is it a tab...
                        tolerance: 'pointer',
                        over: function () {setTimeout(toggle_dropbox);}, // can't guarantee over and out fire in correct
                        out: function () {setTimeout(toggle_dropbox);},  // order, toggle function seems to solve issue
                        drop: function (e, ui) {
                            var has_ancestor = function (elm, sel) {return $(elm).closest('.' + sel).length;},
                                pane_class = class_prefix + pane,
                                overflow_class = 'og-js-overflow-' + pane,
                                data = ui.draggable.data(),
                                gadget = data.gadget().config.options,
                                re = new RegExp(selector_prefix + '(.*?)\\s');
                            if (has_ancestor(ui.draggable, pane_class) || has_ancestor(ui.draggable, overflow_class)) {
                                ui.draggable.draggable('option', 'revert', true);
                            } else {
                                ui.draggable.draggable('option', 'revert', false);
                                gadget.selector = gadget.selector.replace(re, selector_prefix + pane + ' ');
                                if (false !== container.fire('drop', data.gadget().config, data.source))
                                    container.add([data.gadget.config]);
                                setTimeout(data.handler); // setTimeout to ensure handler is called after drag evt ends
                            }
                        }
                    });
                    og.common.gadgets.manager.register(container);
                });
                return container;
            };
            container.resize = function () {
                gadgets.forEach(function (obj) {
                    if (obj.gadget.alive() && obj.active) {
                        $(selector + ' .ui-layout-content').show();
                        update_tabs();
                        obj.gadget.resize();
                    }
                });
            };
            container.verify = function (fingerprints) {
                if (!initialized) return setTimeout(container.verify.partial(fingerprints), 10), container;
                var gadget_prints = gadgets.pluck('fingerprint'), keep;
                keep = (fingerprints || []).map(function (fingerprint) {
                    var index;
                    if (!fingerprint) return null;
                    if (~(index = gadget_prints.indexOf(fingerprint))) return index; else return null;
                }).reduce(function (acc, val) {if (val !== null) acc[val] = null; return acc;}, {});
                gadgets.forEach(function (gadget, index) {if (!(index in keep)) container.del(gadgets[index], true);});
                return container;
            };
        };
        constructor.prototype.fire = og.common.events.fire;
        constructor.prototype.off = og.common.events.off;
        constructor.prototype.on = og.common.events.on;
        return constructor;
    }
});