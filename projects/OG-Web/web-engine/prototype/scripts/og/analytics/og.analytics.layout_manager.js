/*
 * @copyright 2012 - present by OpenGamma Inc
 * @license See distribution for license
 */
$.register_module({
    name: 'og.analytics.layout_manager',
    dependencies: [],
    obj: function () {
        var tabs_tmpl,
            rand = function () {return Math.floor(Math.random() * 1000000);},
            has = 'hasOwnProperty', hdr = ' .ui-layout-header',
            panels = {
                'all_gadgets': {
                    'south': [],
                    'dock-north': [],
                    'dock-center': [],
                    'dock-south': []
                },
                'panel_selectors': {
                    'south': '.OG-layout-analytics-south',
                    'dock-north': '.OG-layout-analytics-dock-north',
                    'dock-center': '.OG-layout-analytics-dock-center',
                    'dock-south': '.OG-layout-analytics-dock-south'
                },
                'get_panel': function (elm) {
                    return $(elm).parentsUntil('[class*=ui-layout-analytics]', '[class*="OG-layout-analytics"]')
                                 .attr('class')
                                 .replace(/OG\-layout\-analytics\-([-a-z]+)?\s(?:.+)/, '$1');
                },
                /**
                 * @param {Object}         config
                 * @param {Function}       config.gadget
                 * @param {Object}         config.options
                 * @param {String}         config.name     Tab name
                 * @param {String|Number}  config.id       A unique id that can be used to identify a gadget
                 * @param {String}         config.panel    // bottom, top_right...
                 * @param {Boolean}        config.margin   // add margin to the container
                 */
                'add_gadget': function (config) {
                    var panel_container = panels.panel_selectors[config.panel] + ' .OG-gadget-container',
                        gadget_class = 'OG-gadget-' + config.id,
                        gadget_selector = panel_container + ' .' + gadget_class;
                    $(panel_container)
                        .append('<div class="' + gadget_class + '" />')
                        .find('.' + gadget_class)
                        .css({margin: config.margin ? 10 : 0}); // add margin if requested
                    config.gadget($.extend(true, config.options, {selector: gadget_selector}));
                    console.log('p', panels.all_gadgets[config.panel]);
                    panels.all_gadgets[config.panel].push(config);
                    panels.update_tabs(config.panel, config.id);
                },
                'remove_gadget': function (tab_id, panel) {},
                /**
                 * @param {String} panel Panel to update. If not supplied all panels will be initialized with empty tabs
                 * @param {Number} id Active tab id
                 */
                'update_tabs': function (panel, id) {
                    var template = Handlebars.compile(tabs_tmpl), tabs_arr;
                    // init empty tabs
                    if (!panel) return (function () {
                        var panel, ps = panels.all_gadgets;
                        for (panel in ps) if (ps[has](panel)) $(panels.panel_selectors[panel] + hdr)
                            .html(template({'tabs': [{'name': 'empty'}]}))
                            .on('click', 'li', function () {
                                var id;
                                if (!$(this).hasClass('og-active')) id = $(this).attr('class').replace(/og\-/, '');
                                panels.update_tabs(panels.get_panel(this), +id);
                            });
                    }());
                    // load tabs
                    tabs_arr = panels.all_gadgets[panel].reduce(function (acc, val) {
                        return acc.push({
                            'name': val.name, 'active': (id === val.id), 'delete': true, 'id': val.id
                        }) && acc;
                    }, []);
                    $(panels.panel_selectors[panel] + hdr).html(template({'tabs': tabs_arr}));
                    panels.show_gadget(panel, id);
                },
                'show_gadget': function (panel, id) {
                    console.log(arguments);
                    var $p_gadgets = $(panels.panel_selectors[panel]).find('.OG-gadget-container [class*=OG-gadget-]');
                    $p_gadgets.filter(function () {return $(this).hasClass('OG-gadget-' + id) ? false : true;}).hide();
                    $p_gadgets.filter('.OG-gadget-' + id).show();
                }
            };
        return function () {
            $.when(og.api.text({module: 'og.analytics.tabs_tash'})).then(function (tmpl) {
                tabs_tmpl = tmpl;
                panels.update_tabs();
                (function () { // init minimize button
                    var layout = og.views.common.layout.inner;
                    $('.OG-gadget-tabs-options .og-minimize').on('click', function () {
                        var max = '25%', min = $(this).parent().parent().height(), pane = panels.get_panel(this);
                        layout.state[pane].size === min ? layout.sizePane(pane, max) : layout.sizePane(pane, min);
                    });
                }());
                /**
                 * TEMP STUFF
                 */
                (function () {
                    var tmp_link = '\
                            <div class="tmp-links">\
                              <span class="og-open-ts-gadget-bot">bot</span>\
                              <span class="og-open-ts-gadget-bot2">bot2</span>\
                              <span class="og-open-ts-gadget-tr">tr</span>\
                            </div>\
                            ';
                    $('body').append(tmp_link).find('.tmp-links').css({
                        'position': 'absolute', 'top': '10px', 'left': '100px', 'z-index': '2'
                    });
                    $('.og-open-ts-gadget-bot').on('click', function () {
                        panels.add_gadget({
                            gadget: og.common.gadgets.timeseries,
                            options: {id: 'DbHts~1015', datapoints_link: false},
                            name: 'Timeseries 1015',
                            panel: 'south',
                            margin: true,
                            id: rand()
                        });
                    });
                    $('.og-open-ts-gadget-bot2').on('click', function () {
                        panels.add_gadget({
                            gadget: og.common.gadgets.timeseries,
                            options: {id: 'DbHts~3943', datapoints_link: false},
                            name: 'Timeseries 3943',
                            panel: 'south',
                            margin: true,
                            id: rand()
                        });
                    });
                    $('.og-open-ts-gadget-tr').on('click', function () {
                        panels.add_gadget({
                            gadget: og.common.gadgets.timeseries,
                            options: {id: 'DbHts~1001', datapoints_link: false},
                            name: 'Timeseries 1001',
                            panel: 'dock-north',
                            margin: true,
                            id: rand()
                        });
                    });
                }());
            });
        }
    }
});