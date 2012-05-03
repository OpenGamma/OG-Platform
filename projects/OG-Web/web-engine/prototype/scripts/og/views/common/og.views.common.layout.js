/*
 * @copyright 2009 - present by OpenGamma Inc
 * @license See distribution for license
 */
$.register_module({
    name: 'og.views.common.layout',
    dependencies: [],
    obj: function () {
        return {
            admin: function () {return {
                main: $('.OG-layout-admin-container').layout({
                    defaults: {
                        enableCursorHotkey: false, onresizeall_end: 'og.common.gadgets.manager.resize',
                        togglerLength_open: 0
                    },
                    north:    {spacing_open: 0, size: 43, paneClass: 'OG-layout-admin-masthead'},
                    south:    {spacing_open: 0, size: 36, paneClass: 'OG-layout-admin-footer'},
                    east:     {spacing_closed: 0, initClosed: true}, // Not used
                    west:     {spacing_open: 7, size: '33%', paneClass: 'OG-layout-admin-search'},
                    center:   {paneClass: 'ui-layout-details'}
                }),
                inner: $('.ui-layout-details').layout({
                    defaults: {
                        enableCursorHotkey: false, onresizeall_end: 'og.common.gadgets.manager.resize',
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
            analytics: function () {return {
                main: $('.OG-layout-admin-container').layout({
                    defaults: {enableCursorHotkey: false},
                    north: {spacing_open: 0, paneClass: 'OG-layout-admin-masthead', size: 43},
                    south: {spacing_open: 0, paneClass: 'OG-layout-admin-footer', size: 36},
                    center: {paneClass: 'OG-layout-analytics', contentSelector: '.ui-layout-content'}
                })
            };},
            analytics2: function () {return {
                main: $('.OG-layout-analytics-container').layout({
                    defaults: {
                        enableCursorHotkey: false, onresize_end: 'og.common.gadgets.manager.resize',
                        togglerLength_open: 0
                    },
                    north: {
                        spacing_open: 0, paneClass: 'OG-layout-analytics-masthead', size: 43
                    },
                    south: {
                        spacing_open: 0, paneClass: 'OG-layout-analytics-footer', size: 20
                    },
                    east: {
                        spacing_open: 7, spacing_closed: 0, initClosed: false,
                        paneClass: 'OG-layout-analytics-dock', size: 450
                    },
                    center: {
                        paneClass: 'OG-layout-analytics2'
                    }
                }),
                inner: $('.OG-layout-analytics2').layout({
                    defaults: {
                        enableCursorHotkey: false, onresize_end: 'og.common.gadgets.manager.resize',
                        togglerLength_open: 0
                    },
                    south: {
                        paneSelector: '.OG-layout-analytics-south', paneClass: 'OG-layout-analytics-south',
                        size: '50%', initClosed: false, spacing_closed: 0
                    },
                    center: { // main grid
                        paneSelector: '.OG-layout-analytics-center', paneClass: 'OG-layout-analytics-center',
                        contentSelector: '.ui-layout-content'
                    }
                }),
                right: $('.OG-layout-analytics-dock').layout({
                    defaults: {
                        enableCursorHotkey: false, onresize_end: 'og.common.gadgets.manager.resize',
                        togglerLength_open: 0, spacing_open: 7
                    },
                    north: {
                        paneSelector: '.OG-layout-analytics-dock-north', paneClass: 'OG-layout-analytics-dock-north',
                        size: '33%', initClosed: false, spacing_closed: 0
                    },
                    south: {
                        paneSelector: '.OG-layout-analytics-dock-south', paneClass: 'OG-layout-analytics-dock-south',
                        size: '33%', initClosed: false, spacing_closed: 0
                    },
                    center: {
                        paneSelector: '.OG-layout-analytics-dock-center', paneClass: 'OG-layout-analytics-dock-center',
                        size: '34%', contentSelector: '.ui-layout-content'
                    }
                })
            };}
        };
    }
});