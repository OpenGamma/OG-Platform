/*
 * Copyright 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.views.common.layout',
    dependencies: [],
    obj: function () {
        return {
            admin: function () {return {
                main: $('.OG-layout-admin-container').layout({
                    stateManagement: {enabled: true, cookie: {name: 'opengamma_admin_layout_main', path: '/'}},
                    defaults: {
                        enableCursorHotkey: false, onresize_end: 'og.common.gadgets.manager.resize',
                        togglerLength_open: 0
                    },
                    north:    {spacing_open: 0, size: 25, paneClass: 'OG-layout-masthead'},
                    west:     {spacing_open: 8, size: '33%', paneClass: 'OG-layout-admin-search'},
                    center:   {paneClass: 'ui-layout-details'}
                }),
                inner: $('.ui-layout-details').layout({
                    stateManagement: {enabled: true, cookie: {name: 'opengamma_admin_layout_inner', path: '/'}},
                    defaults: {
                        enableCursorHotkey: false, onresize_end: 'og.common.gadgets.manager.resize',
                        togglerLength_open: 0
                    },
                    north: { // used for deleted view message
                        paneSelector: '.OG-layout-admin-details-north', paneClass: 'OG-layout-admin-details-north',
                        size: 50, initClosed: true, spacing_closed: 0, spacing_open: 0
                    },
                    south: { // versions / sync etc
                        paneSelector: '.OG-layout-admin-details-south', paneClass: 'OG-layout-admin-details-south',
                        size: '50%', initClosed: true, spacing_closed: 0, spacing_open: 7,
                        onopen_start: function () {$('.OG-layout-admin-details-south').empty();}
                    },
                    center: {
                        paneSelector: '.OG-layout-admin-details-center', paneClass: 'OG-layout-admin-details-center',
                        contentSelector: '.ui-layout-content'
                    }
                })
            };},
            analytics_legacy: function () {return {
                main: $('.OG-layout-admin-container').layout({
                    defaults: {enableCursorHotkey: false},
                    north: {spacing_open: 0, paneClass: 'OG-layout-masthead', size: 25},
                    center: {paneClass: 'OG-layout-analytics-legacy', contentSelector: '.ui-layout-content'}
                })
            };},
            gadget: function () {return {
                main: $('.OG-layout-gadget-container').layout({
                    defaults: {enableCursorHotkey: false, onresize_end: 'og.common.gadgets.manager.resize'},
                    center: {paneClass: 'OG-gadgets-container'}
                })
            };},
            analytics: function () {return {
                main: $('.OG-layout-analytics-container').layout({
                    stateManagement: {enabled: true, cookie: {name: 'opengamma_layout_main', path: '/'}},
                    defaults: {
                        enableCursorHotkey: false, onresize_end: 'og.common.gadgets.manager.resize',
                        togglerLength_open: 0
                    },
                    north: {spacing_open: 0, paneClass: 'OG-layout-masthead', size: 67},
                    east: {spacing_open: 7, paneClass: 'OG-layout-analytics-dock', size: "25%"},
                    center: {paneClass: 'OG-layout-analytics2'}
                }),
                inner: $('.OG-layout-analytics2').layout({
                    stateManagement: {enabled: true, cookie: {name: 'opengamma_layout_inner', path: '/'}},
                    defaults: {
                        enableCursorHotkey: false, onresize_end: 'og.common.gadgets.manager.resize',
                        togglerLength_open: 0, paneClass: 'OG-gadgets-container'
                    },
                    south: {paneSelector: '.OG-layout-analytics-south', size: '50%', spacing_open: 7},
                    center: {
                        paneSelector: '.OG-layout-analytics-center',
                        contentSelector: '.ui-layout-content', paneClass: 'OG-layout-analytics-center'
                    }
                }),
                right: $('.OG-layout-analytics-dock').layout({
                    stateManagement: {enabled: true, cookie: {name: 'opengamma_layout_right', path: '/'}},
                    defaults: {
                        enableCursorHotkey: false, onresize_end: 'og.common.gadgets.manager.resize',
                        togglerLength_open: 0, spacing_open: 7, paneClass: 'OG-gadgets-container'
                    },
                    north: {paneSelector: '.OG-layout-analytics-dock-north', size: '33%'},
                    south: {paneSelector: '.OG-layout-analytics-dock-south' ,size: '33%'},
                    center: {paneSelector: '.OG-layout-analytics-dock-center', size: '34%'}
                })
            };},
            blotter: function () {
                return {
                    main: $('.OG-layout-blotter-container').layout({
                        defaults: {
                            enableCursorHotkey: false, onresize_end: 'og.common.gadgets.manager.resize',
                            togglerLength_open: 0
                        },
                        north: {spacing_open: 0, paneClass: 'OG-layout-blotter-masthead', size: 67},
                        center: {spacing_open: 0, paneClass: 'OG-layout-analytics-center'}
                    })
            };}
        };
    }
});