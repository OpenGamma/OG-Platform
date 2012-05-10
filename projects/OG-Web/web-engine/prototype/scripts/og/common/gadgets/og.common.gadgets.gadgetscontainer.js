/*
 * @copyright 2011 - present by OpenGamma Inc
 * @license See distribution for license
 */
$.register_module({
    name: 'og.common.gadgets.GadgetsContainer',
    dependencies: ['og.common.gadgets.manager'],
    obj: function () {
        var tabs_tmpl, header = ' .ui-layout-header', panels = {
            '.OG-layout-analytics-south': 'south',
            '.OG-layout-analytics-dock-north': 'dock-north',
            '.OG-layout-analytics-dock-center': 'dock-center',
            '.OG-layout-analytics-dock-south': 'dock-south'
        };
        /**
         * @param {String} selector Selector to initialize a GadgetsContainer in
         * @param {Array} arr Optional array of gadgets to initialize a GadgetsContainer with
         */
        return function (selector, arr) {
            var pane, initialized = false, live_id, counter = 1, gadgets = [], container = this;
            container.selector = selector;
            /**
             * @param {Number} id Active tab id
             */
            var update_tabs = function (id) {
                var template = Handlebars.compile(tabs_tmpl), $header = $(container.selector + header), tabs;
                if (!id) $header.html(template({'tabs': [{'name': 'empty'}]})); // empty tabs
                else {
                    tabs = gadgets.reduce(function (acc, val) {
                        return acc.push({
                            'name': val.name, 'active': (id === val.id), 'delete': true, 'id': val.id
                        }) && acc;
                    }, []);
                    $header.html(template({'tabs': tabs}));
                    show_gadget(id);
                }
            };
            /**
             * @param id Id of gadget to show, hide all others
             */
            var show_gadget = function (id) {
                $(container.selector).find('.OG-gadget-container [class*=OG-gadget-]')
                    .hide().filter('.OG-gadget-' + id).show();
                live_id = id;
            };
            /**
             * @param {String} pane Pane to toggle
             * @param {Boolean} open_only Don't close, only open
             */
            var toggle_pane = function (pane, open_only) {
                var max = '50%', min = $('.OG-layout-analytics-' + pane + ' .OG-gadget-tabs').height();
                og.views.common.layout.inner.state[pane].size === min
                        ? og.views.common.layout.inner.sizePane(pane, max)
                        : open_only ? null : og.views.common.layout.inner.sizePane(pane, min);
            };
            container.init = function (selector, arr) {
                container.selector = selector || container.selector;
                $.when(og.api.text({module: 'og.analytics.tabs_tash'})).then(function (tmpl) {
                    pane = panels[container.selector];
                    tabs_tmpl = tmpl;
                    initialized = true;
                    // setup click handlers
                    $(container.selector + header)
                        .on('click', 'li', function () {
                            if (!$(this).hasClass('og-active')) {
                                var id = +$(this).attr('class').replace(/og\-/, '');
                                update_tabs(id);
                                gadgets[id-1].gadget.resize();
                                toggle_pane(pane, true);
                            }
                        })
                        .on('click', '.og-minimize', function () {toggle_pane(pane);});
                    if (!arr) update_tabs(); else container.add(arr);
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
                if (!initialized) return setTimeout(container.add.partial(arr), 10);
                if (!container.selector) throw new TypeError('GadgetsContainer has not been initialized');
                var panel_container = container.selector + ' .OG-gadget-container', new_gadgets;
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
            };
            container.alive = function () {return !!$(container.selector).length;};
            container.resize = function () {
                gadgets.forEach(function (obj) {
                    if (obj.id === live_id) $(container.selector + ' .ui-layout-content').show(), obj.gadget.resize();
                });
            }
        };
    }
});