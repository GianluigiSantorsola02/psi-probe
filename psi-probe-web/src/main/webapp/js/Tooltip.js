/*
 * Licensed under the GPL License. You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   https://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 *
 * THIS PACKAGE IS PROVIDED "AS IS" AND WITHOUT ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING,
 * WITHOUT LIMITATION, THE IMPLIED WARRANTIES OF MERCHANTIBILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE.
 */
/*
 * Tooltip.js
 *
 * Advanced Tooltip class
 *
 * WARNING: Due to an IE bug, Tooltips will NOT display on top of a <select> element.
 *
 * @copyright Davey Shafik (c) 2005 All Rights Reserved
 * @authors Davey Shafik <davey@php.net>
 * @version 0.6.0
 * @license MIT-style <http://tooltip.crtx.org/LICENSE>
 */

/**
 * Add an Array.contains() method, mimics PHPs in_array() function
 */



/**
 * Tooltip Object definition
 */
let Tooltip = {
	/**
	 * @const int Indicate that the current tooltip should be used
	 */
	CURRENT_TOOLTIP: 1,

	/**
	 * Attach show/hide/load events
	 *
	 * This method removes any existing events first
	 * in case show/hide are the same.
	 *
	 * @param element Element to which events should be attached
	 * @param event Event for which events are being registered. One of show/hide/load/click.
	 */

	_attachEvent: function (element, event) {
		const eventHandlers = {
			toggle: function (e) {
				Tooltip.toggle(element, e);
				return false;
			},
			load: function () {
				Tooltip.setup();
			},
			click: function (e) {
				Tooltip.toggle(element, e);
			},
			follow: function (e) {
				Tooltip._follow(element, e);
			},
			clickanywhere: function (e) {
				Tooltip.toggle(Tooltip.CURRENT_TOOLTIP, e);
			}
		};

		if (event in eventHandlers) {
			const handler = eventHandlers[event];
			if (element.addEventListener) {
				element.addEventListener(event, handler, false);
			} else {
				element.attachEvent('on' + event, handler);
			}
		} else {
			console.error('Invalid event:', event);
		}
	},
	/**
	 * @let object Currently shown Tooltip
	 */
	_current: false,

	_follow: function (activator, event) {
		if (activator.timer) {
			try {
				clearTimeout(activator.timer);
			} catch (e) {
			}
		}

		let winWidth;
		if (typeof window.innerWidth != 'undefined') {
			winWidth = window.innerWidth;
		} else if (d.documentElement && typeof d.documentElement.clientWidth != 'undefined' && d.documentElement.clientWidth !== 0) {
			winWidth = d.documentElement.clientWidth
		} else if (d.body && typeof d.body.clientWidth != 'undefined') {
			winWidth = d.body.clientWidth
		}


		let tooltipWidth;
		if (activator.Tooltip.currentStyle) {
			tooltipWidth = activator.Tooltip.currentStyle.width;
		} else if (window.getComputedStyle) {
			tooltipWidth = window.getComputedStyle(activator.Tooltip, null).width;
		}

		activator.Tooltip.style.position = "absolute";

		let left;
		if (event.pageY) {
			top = event.pageY + 15;
			left = event.pageX + 15;
		} else if (event.clientY) {
			top = event.clientY + 15;
		}

		if ((left + parseInt(tooltipWidth)) > winWidth) {
			left = winWidth - parseInt(tooltipWidth) * 1.2;
		}

		activator.Tooltip.style.top = parseInt(top) + "px";
		activator.Tooltip.style.left = left + "px";
	},

	/**
	 * Hide the Tooltip
	 *
	 * Hides the Tooltip and sets the show events up. You should never need to call this manually.
	 *
	 * @param activator Activator Element
	 * @param event
	 * @param ignore_event
	 * @private
	 * @return void
	 */
	_hide: function (activator, event, ignore_event) {
		if (!activator) {
			return;
		}

		event = event.type;
		let tooltip = activator.Tooltip;

		function hideTooltip() {
			activator.Tooltip.isVisible = false;
			try {
				Tooltip.hideMethod(tooltip, {duration: Tooltip.fade});
			} catch (e) {
				activator.Tooltip.style.visibility = "hidden";
			}

			if (Tooltip.autoFollowMouse) {
				Tooltip._removeEvent(activator, "follow");
			}
		}

		if (event === "mouseout" && Tooltip.autoFollowMouse) {
			activator.timer = setTimeout(function () {
				hideTooltip();
			}, Tooltip.autoHideTimeout * 1000);
		} else if (ignore_event === true || (Tooltip.hideEvent && (typeof Tooltip.hideEvent === "string" && Tooltip.hideEvent === event) || (Tooltip.hideEvent.constructor && Tooltip.hideEvent.constructor === Array && Tooltip.hideEvent.includes(event)))) {
			hideTooltip();
		}

		if (activator.timer) {
			clearTimeout(activator.timer);
		}
		window._currentTT = false;
	},
	_removeEvent: function (element, event) {
		try {
			if (event === "follow") {
				if (element.addEventListener) {
					element.removeEventListener("mousemove", function (e) {
						Tooltip._follow(element, e);
					}, false);
				} else {
					element.dettachEvent('onmousemove', function (e) {
						Tooltip._follow(element, e);
					});
				}
			}
		} catch (e) {
		}
	},
	/**
	 * Manually add a Tooltip
	 *
	 * When passed an Activator and Tooltip element or ID, it is setup as a Tooltip
	 *
	 * @param activator Activator Element or ID, this is the element that activates the Tooltip
	 * @param tooltip Tooltip Element or ID, this is the Tooltip element itself that is shown/hidden
	 */
	add: function (activator, tooltip) {
		if (typeof activator == 'string') {
			activator = document.getElementById(activator);
		}
		if (typeof tooltip == 'string') {
			tooltip = document.getElementById(tooltip);
		}

		activator.Tooltip = tooltip;
		Tooltip.init(activator);
	},

	/**
	 * @let boolean Whether the Tooltip should follow the mouse or not. Warning: Cheesy!
	 */
	autoFollowMouse: false,

	/**
	 * @let boolean Allow user to click anywhere to hide current tooltip
	 */
	autoHideClick: true,

	/**
	 * @let integer If set, the Tooltip will automatically hide after X seconds
	 *
	 * When followMouse is true, the mouseout event does not trigger the hide callback
	 * till X has passed. This is to allow the user to move a little off the element -
	 * which is especially useful when it's an inline element such as a link.
	 */
	autoHideTimeout: 40,

	/**
	 * @let boolean If set to true, the Tooltip will be displayed (static) at the current Mouse Cursor location.
	 */
	autoMoveToCursor: true,

	/**
	 * @let string Close Link Text
	 */
	closeText: "Close",


	/**
	 * @let float Duration of the fade events, in seconds
	 * @author Idea contributed by Richard Thomas <cyberlot@cyberlot.net>
	 */
	fade: 0.5,

	/**
	 * @let string|Array An event name or an array of event names on which to trigger hiding the Tooltip
	 */
	hideEvent: "click",

	/**
	 * @let function Set the method which will be called for hiding the tooltip
	 */
	hideMethod: Effect.Fade,

	/**
	 * Initiate an Activator/Tooltip for events and display
	 *
	 * @param activator DomElement The element to which the Tooltip show/hide events are attached
	 * @return void
	 */
	init: function (activator) {
		let tooltip = activator.Tooltip;
		activator.Tooltip.style.visibility = "hidden";

		Tooltip._attachEvent(activator, "toggle");


		// Remove Link Hrefs
		if (activator.tagName.toLowerCase() === "a") {
			try {
				activator.removeAttribute("href");
				activator.style.cursor = (document.links[0].style.cursor.length > 0) ? document.links[0].style.cursor : "pointer";
			} catch (e) {
				//DEBUG alert(e.message);
			}
		}

		// Make sure the Tooltip is on top, only works if the element has position: absolute; in the CSS
		tooltip.style.zIndex = "1000";

		if (!(Tooltip.autoFollowMouse !== true && Tooltip.closeText)) {
			return;
		}
		let p = document.createElement('p');
		p.style.textAlign = "right";
		p.style.padding.padding = "0";
		p.style.margin = "0";
		p.className = "close";
		let link = document.createElement('a');
		link.Tooltip = tooltip;
		link.style.cursor = "pointer";
		Tooltip._attachEvent(link, "click");
		let close = document.createTextNode(Tooltip.closeText);
		link.appendChild(close);
		p.appendChild(link);
		tooltip.appendChild(p, tooltip.firstChild);
	},

	/**
	 * Initial Setup
	 *
	 * Find all standard tooltips and auto-initialize them
	 *
	 * @return void
	 */
	setup: function () {
		let match_class = /^?tooltip?(.*)$/i;
		let match_for = /^.*?for_?.*$/i;
		let divs = document.getElementsByTagName('div');

		function processDiv(div) {
			let activator = div;
			if (!activator) {
				return;
			}

			activator.Tooltip = div;
			if (!activator.id) {
				activator.id = "tt" + i;
			}
			activator.Tooltip.activator = activator.id;
			Tooltip.init(activator);
		}

		if (Tooltip.autoFollowMouse && Tooltip.autoHideTimeout) {
			Tooltip.hideEvent.push("mouseout");
		}

		if (Tooltip.autoHideClick) {
			Tooltip._attachEvent(document.getElementsByTagName("body").item(0), "clickanywhere");
		}

		for (let i = 0; i < divs.length; i++) {
			let div = divs.item(i);
			if (!match_class.exec(div.className)) {
				continue;
			}

			let for_result = match_for.exec(div.className);
			if (for_result && for_result.length > 0) {
				let foundNext = false;
				let activator = div;
				while (foundNext === false && activator) {
					activator = activator.nextSibling;
					if (activator?.tagName) {
						foundNext = true;
					}
				}
			} else {
				let foundPrevious = false;
				let activator = div;
				while (foundPrevious === false && activator) {
					activator = activator.previousSibling;
					if (activator?.tagName) {
						foundPrevious = true;
					}
				}
			}

			processDiv(div);
		}
	},
	/**
	 * @let string|Array An event name or an array of event names on which to trigger showing the Tooltip
	 */
	showEvent: "click",

	/**
	 * @let function Set the method which will be called for showing the tooltip
	 */
	showMethod: Effect.Appear,

	/**
	 * Toggle the Tooltip
	 *
	 * Shows or Hides the Tooltip
	 *
	 * @param activator Activator Element
	 * @param event
	 * @return void
	 */

	toggle: function (activator, event) {
		event.fromElement = undefined;

		if (Tooltip.autoHideClick ?.event.type === "click") {
			let close_class = /^?close?(.*)$/i;
			let tooltip_class = /^?tooltip?(.*)$/i;

			function isWithinTooltip(node) {
				while (node.parentNode) {
					if (node.className?.match(close_class) || node.className?.match(tooltip_class)) {
						return true;
					}
					node = node.parentNode;
				}
				return false;
			}

			if (!isWithinTooltip(event.target)) {
				Tooltip._hide(activator, event, true);

			}
		}
	}};

// Start the Tooltips in motion
try {
	Tooltip._attachEvent(window, 'load');
}
catch (e) { }
