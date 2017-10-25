/*-
 * #%L
 * io.earcam.utilitarian.site.search.offline
 * %%
 * Copyright (C) 2017 earcam
 * %%
 * SPDX-License-Identifier: (BSD-3-Clause OR EPL-1.0 OR Apache-2.0 OR MIT)
 * 
 * You <b>must</b> choose to accept, in full - any individual or combination of 
 * the following licenses:
 * <ul>
 * 	<li><a href="https://opensource.org/licenses/BSD-3-Clause">BSD-3-Clause</a></li>
 * 	<li><a href="https://www.eclipse.org/legal/epl-v10.html">EPL-1.0</a></li>
 * 	<li><a href="https://www.apache.org/licenses/LICENSE-2.0">Apache-2.0</a></li>
 * 	<li><a href="https://opensource.org/licenses/MIT">MIT</a></li>
 * </ul>
 * #L%
 */

var search = {};


function loadSearch(inputElement)
{
	loadSearchFrom(inputElement, '/search-data.json');
}


function loadSearchFrom(inputElement, jsonUri)
{
	var placeholder = inputElement.getAttribute('placeholder'),
		xhr = new XMLHttpRequest();		

	inputElement.setAttribute('disabled', 'true');
	inputElement.setAttribute('placeholder', 'Loading ...');

	xhr.open('get', jsonUri);
	xhr.addEventListener('load', function(e) {
		var data = JSON.parse(e.target.response);
		search.index = lunr.Index.load(data.index);
		search.autocomplete = data.autocomplete;
		search.titleMap = data.titleMap;
		
		wiring(inputElement);
		
		inputElement.removeAttribute('disabled');
		inputElement.setAttribute('placeholder', placeholder);
		
		window.setTimeout(function() {
			inputElement.dispatchEvent(new Event('searchIndexLoaded'));
		}, 500);
	});
	xhr.send();
}


function wiring(inputElement)
{
	var input = $(inputElement);
	$(inputElement.form).on('submit', function(e){ doSearch(e, input); });

	input.typeahead({
		
		classNames: {
			input: 'typeahead-dummy-input',
			hint: 'typeahead-dummy-hint',
			menu: 'dropdown-menu pull-right',
			dataset: 'typeahead-dummy-dataset',
			suggestion: 'dropdown-item',
			empty: 'typeahead-dummy-empty',
			open: 'dropdown-menu-autocomplete-open',
			cursor: 'dropdown-item-focus-hover',
			highlight: 'active',
		},
		hint: true,
		highlight: true,
		minLength: 1
	}, {
		name: 'search',
		source: multipleTermMatcher(search.autocomplete)
	});

	input
		.bind('typeahead:autocomplete', resizeAutoCompleteSuggestions)
		.bind('typeahead:render', resizeAutoCompleteSuggestions)
		.bind('typeahead:select', resizeAutoCompleteSuggestions)
		.bind('typeahead:cursorchange', resizeAutoCompleteSuggestions);
}


function doSearch(event, inputElement) {

	event.preventDefault();
	
	var query = $(inputElement).val().trim(),
		results = search.index.search(query),
		resultsBody = $('#search-results-body'),
		baseUri = location.protocol + '//' + location.host;
	
	// TODO pagination use Array.slice on results
	resultsBody.empty();
	
	results.forEach(function(result) {
		
// var fields = '';
// for(var t in result.matchData.metadata) {
// for(var f in result.matchData.metadata[t]) {
// fields += f + ' ';
// }
// }
		
		var card = 
				'<div class="card">' +
					'<div class="card-block">' +
						'<h4 class="card-title"><a href="' + result.ref + '">' + search.titleMap[result.ref] + '</a></h4>' +
						'<a href="' + result.ref + '" class="card-link small text-muted">' + baseUri + result.ref + '</a><br/>' +
						'<small class="muted">Relevance Score: ' + Math.round(result.score * 1000)/1000 + '</small>' +
					'</div>' +
				'</div><br/>';
		
		resultsBody.append(card);
// console.log(res);
	});


// var markInstance = new Mark(document.querySelector('#search-results-body')),
// lastChar = query.charAt(query.length -1),
// separate = true;
//
// //Handle highlighting for quoted query
// if(query.charAt(0) === lastChar && (lastChar == '\'' || lastChar == '"')) {
// query = query.substring(1, query.length -1);
// separate = false;
// }
//
// markInstance.unmark({
// done: function() {
// markInstance.mark(query, {separateWordSearch: separate});
// }
// });

	
	$('#search-results-title').text(query + " - " + results.length + " search result" + (results.length == 1 ? "" : "s"));
	$('#search-results-modal').modal('show');
	return false;
}


function multipleTermMatcher(strs)
{
	return function findMatches(query, cb) {
		var matches = [],
			lastSpace = query.lastIndexOf(' '),
			prefix = lastSpace == -1 ? "" : query.substring(0, lastSpace + 1);

		if(lastSpace != query.length -1) {
			query = query.substring(lastSpace + 1);
	
			$.each(strs, function(i, str) {
				if(str.startsWith(query)) {
					matches.push(prefix + str);
				}
			});
		}
		cb(matches);
	};
}


function resizeAutoCompleteSuggestions(ev, suggestion) {

	var menu = document.querySelector('#search-form .dropdown-menu');
	resizeForWidthOverflow(menu);
}


function resizeForWidthOverflow(el)
{
	var overflow = el.style.overflow;

	if(!overflow || overflow === 'visible') {
		el.style.overflow = 'hidden';
	}

	var amount = el.clientWidth - el.scrollWidth,
		left = parseInt(el.style.left);

	if(amount < 0) {
		el.style.left = (left + amount) + "px";
		el.style.right = "0px";
	} else if(el.parentNode.clientWidth < el.clientWidth) {
		el.style.left = "0px";
		resizeForWidthOverflow(el);
	}
	el.style.overflow = overflow;
}
