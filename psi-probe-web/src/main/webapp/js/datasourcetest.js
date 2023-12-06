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
	Java Script functions for datasourcetest.jsp

	Author: Andy Shapoval, Vlad Ilyushchenko
*/

let connectUrl = '';
let recordsetUrl = '';
let queryHistoryUrl = '';
let sqlOutputDivId = 'outputHolder';
let formId = 'sqlForm';
let ajaxActivityId = 'ajaxActivity';
let metaDataH3Id = 'metaDataH3';
let resultsH3Id = "resultsH3";
let historyContainerDivId = "queryHistoryContainer";
let historyOutputDivId = 'queryHistoryHolder';
let historyVisible = false;
let historyWrapped = true;
let historyHeight = 150;
let optionsDivId = 'optionsDL';
let optionsVisible = false;
let ajaxActivityTimer;

function setupAjaxActions(aConnectUrl, aRecordsetUrl, aQueryHistoryUrl) {
	connectUrl = aConnectUrl;
	recordsetUrl = aRecordsetUrl;
	queryHistoryUrl = aQueryHistoryUrl;

	let rules = {
		'li#connect': function(element) {
			element.onclick = function() {
				testConnction();
				$('sql').focus();
				return false;
			}
		},
		'li#executeSql': function(element) {
			element.onclick = function() {
				executeSql();
				$('sql').focus();
				return false;
			}
		},
		'li#showHistory': function(element) {
			element.onclick = function() {
				showQueryHistory();
				$('sql').focus();
				return false;
			}
		},
		'li#hideHistory': function(element) {
			element.onclick = function() {
				hideQueryHistory();
				$('sql').focus();
				return false;
			}
		},
		'li#showOptions': function(element) {
			element.onclick = function() {
				showOptions();
				$('sql').focus();
				return false;
			}
		},
		'li#hideOptions': function(element) {
			element.onclick = function() {
				hideOptions();
				$('sql').focus();
				return false;
			}
		},
		'li#wrap': function(element) {
			element.onclick = function() {
				wrapQueryHistory();
				$('sql').focus();
				return false;
			}
		},
		'li#nowrap': function(element) {
			element.onclick = function() {
				nowrapQueryHistory();
				$('sql').focus();
				return false;
			}
		}
	}

	Behaviour.register(rules);
}

function testConnction() {
	hideQueryHistory();
	Element.show(ajaxActivityId);
	Element.hide(resultsH3Id);
	Element.show(metaDataH3Id);
	$('rowsAffected').innerHTML = "";
	$('pagebanner').innerHTML = "";
	$('pagelinks').innerHTML = "";

}

function executeSql() {
	hideQueryHistory();
	Element.show(ajaxActivityId);
	Element.hide(metaDataH3Id);
	Element.show(resultsH3Id);
}

function setupPaginationLinks(req, obj) {
	if ($('rs_rowsAffected') && $('rs_pagebanner') && $('rs_pagelinks')) {
		$('rowsAffected').innerHTML = $('rs_rowsAffected').innerHTML;
		$('pagebanner').innerHTML = $('rs_pagebanner').innerHTML;
		$('pagelinks').innerHTML = $('rs_pagelinks').innerHTML;
	} else {
		$('rowsAffected').innerHTML = "";
		$('pagebanner').innerHTML = "";
		$('pagelinks').innerHTML = "";
	}

	let links = $$('#pagelinks a');

	links.each(function(lnk) {
		lnk.onclick = function() {
			Element.show(ajaxActivityId);
			Element.show(resultsH3Id);
			return false;
		}
	});

	if (ajaxActivityTimer) clearTimeout(ajaxActivityTimer);
	ajaxActivityTimer = setTimeout('Element.hide("' + ajaxActivityId + '")', 250);
}

/*
	event handlers to display a query history list
*/

function showQueryHistory() {
	Element.hide('hideHistory');
	Element.show('showHistory');
	Effect.Fade(historyContainerDivId, {
		duration:0.20
	});
	historyVisible = true;
}

function getQueryHistoryItem(lnk) {


hideQueryHistory();
$('sql').focus();
}

function hideQueryHistory() {
	Element.hide('hideHistory');
	Element.show('showHistory');
	Effect.Fade(historyContainerDivId, {
		duration:0.20
	});
	historyVisible = false;
}

function wrapQueryHistory() {
	Element.setStyle(historyOutputDivId, {
		"whiteSpace": "normal"
	});
	Element.hide('wrap');
	Element.show('nowrap');
	historyWrapped = true;
}

function nowrapQueryHistory() {
	Element.setStyle(historyOutputDivId, {
		"whiteSpace": "nowrap"
	});
	Element.hide('nowrap');
	Element.show('wrap');
	historyWrapped = false;
}

/*
	event handlers to display an option entry form
*/

function showOptions() {
	Element.hide('showOptions');
	Element.show('hideOptions');
	Effect.Appear(optionsDivId, {
		duration:0.20
	});
	optionsVisible = true;
}

function hideOptions() {
	Element.hide('hideOptions');
	Element.show('showOptions');
	Effect.Fade(optionsDivId, {
		duration:0.20
	});
	optionsVisible = false;
}

/*
	event handlers to provide keyboard shortcuts for major form actions
*/

function setupShortcuts() {
	let rules = {
		'body': function(element) {
			element.onkeydown = function(event) {
				let e = event || window.event;

				function isEnterKeyPressed() {
					return e.keyCode === 13 && e.ctrlKey && !e.altKey && !e.shiftKey;
				}

				function isDownKeyPressed() {
					return e.keyCode === 40 && e.ctrlKey && !e.altKey && !e.shiftKey;
				}

				function isUpKeyPressed() {
					return e.keyCode === 38 && e.ctrlKey && !e.altKey && !e.shiftKey;
				}

				if (isEnterKeyPressed()) {
					executeSql();
					$('sql').focus();
				} else if (isDownKeyPressed()) {
					if (historyVisible) {
						hideQueryHistory();
					} else {
						showQueryHistory();
					}
					$('sql').focus();
				} else if (isUpKeyPressed()) {
					if (optionsVisible) {
						hideOptions();
						$('sql').focus();
					} else {
						showOptions();
						$('sql').focus();
					}
				}
			};
		}
	};

	Behaviour.register(rules);
}

/*
   event handlers for sql textarea and query history div resizing
*/

function resizeTextArea(drag) {
	let deltaY = drag.currentDelta()[1];
	let h = (Element.getDimensions('sql').height + deltaY);
	h = Math.max(h, 100);
	Element.setStyle('sql', {
		height: h + 'px'
	});
}

function resizeQueryHistory(drag) {
	let deltaY = drag.currentDelta()[1];
	let h = (Element.getDimensions(historyOutputDivId).height + deltaY);
	h = Math.max(h, 20);
	historyHeight = h;
	Element.setStyle(historyOutputDivId, {
		height: h + 'px'
	});
}

function revertDragHandle(handle) {
	handle.style.top = 0;
	handle.style.position = 'relative';
	$('sql').focus();
}
