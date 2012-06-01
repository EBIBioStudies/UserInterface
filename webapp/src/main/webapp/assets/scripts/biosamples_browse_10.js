/*
 * Copyright 2009-2011 European Molecular Biology Laboratory
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

var query = new Object();
var pageName = /\/?([^\/]+)$/.exec(decodeURI(window.location.pathname))[1];
var anchor = decodeURI(window.location.hash);

var sortDefault = {
	relevance : "descending",
	id : "ascending",
	description : "ascending",
	samples : "descending"
};

var sortTitle = {
	id : "accession",
	description : "description",
	samples : "number of samples"
};

//var sortBy = "id";
//var sortOrder = sortDefault[sortBy];

//by default I will sort by lucene algorithm
var sortBy = "relevance";
var sortOrder = sortDefault[sortBy];


var pageInit = "1";
var pageSize = "50";
var keywords = "";

$(function() {
	$("#bs_contents").show();

	// this will be executed when DOM is ready
	if ($.query == undefined)
		throw "jQuery.query not loaded";
	
	pageInit = $.query.get("page") || pageInit;
	pageSize = $.query.get("pagesize") || pageSize;
	keywords = $.query.get("keywords") || keywords;
	sortBy = $.query.get("sortby") || sortBy;
	//alert(sortBy);
	sortOrder = $.query.get("sortorder") || sortDefault[sortBy];
	// I need to initialize the sorting on the defaultfield
	var thElt = $("#bs_results_header_" + sortBy);
	if (null != thElt) {
		// alert("#bs_results_header_" + sortBy);
		if ("" != sortOrder) {
			var divElt = thElt.find("div.table_header_inner");
			if (null != divElt) {
				divElt
						.addClass("descending" == sortOrder ? "table_header_sort_desc"
								: "table_header_sort_asc");
			}
		}
	}

	

	var newQuery = $.query.set("keywords", keywords).set("sortby", sortBy).set(
			"sortorder", sortOrder).set("page", pageInit).set("pagesize",
			pageSize).toString();

	var urlPage = "biosamplesgroup/browse-table.html" + newQuery;

	//initialize the keywords input with the search string from the homepage
	$("#bs_keywords").val(keywords);

	QuerySampleGroup(urlPage);
	// added autocompletion
	var basePath = decodeURI(window.location.pathname).replace(/\/\w+\.\w+$/,
			"/");
	// alert(basePath);
	$("#bs_keywords").autocomplete(basePath + "keywords.txt", {
		matchContains : false,
		selectFirst : false,
		scroll : true,
		max : 50,
		requestTreeUrl : basePath + "efotree.txt"
	});
});

function QuerySampleGroup(url) {
	// alert("QuerySampleGroup2->" + url);
	$
			.get(
					url,
					function(tableHtml) {
						//alert("Data->"+tableHtml);

						$("#bs_results_body_inner").removeClass(
								"bs_results_tbl_loading");

						// populate table with data
						$("#bs_results_tbody").html(tableHtml);

						// attach titles to highlight classes
						$("#bs_results_tbody")
								.find(".ae_text_hit")
								.attr("title",
										"This is exact string matched for input query terms");
						$("#bs_results_tbody")
								.find(".ae_text_syn")
								.attr(
										"title",
										"This is synonym matched from Experimental Factor Ontology e.g. neoplasia for cancer");
						$("#bs_results_tbody")
								.find(".ae_text_efo")
								.attr(
										"title",
										"This is matched child term from Experimental Factor Ontology e.g. brain and subparts of brain");

						// get stats from the first row
						var total = $("#bs_results_total").text();
						var from = $("#bs_results_from").text();
						var to = $("#bs_results_to").text();
						var curpage = $("#bs_results_page").text();
						var pagesize = $("#bs_results_pagesize").text();

						if (total > 0) {

							var totalPages = total > 0 ? Math.floor((total - 1)
									/ pagesize) + 1 : 0;
							$("#bs_results_stats_fromto")
									.html(
											total
													+ " group"
													+ (total != 1 ? "s" : "")
													+ " found"
													+ (totalPages > 1 ? (", displaying groups "
															+ from
															+ " to "
															+ to + ".")
															: ""));

							if (totalPages > 1) {
								var pagerHtml = "Pages: ";
								for ( var page = 1; page <= totalPages; page++) {
									if (curpage == page) {
										pagerHtml = pagerHtml
												+ "<span class=\"pager_current\">"
												+ page + "</span>";
									} else if (2 == page && curpage > 6
											&& totalPages > 11) {
										pagerHtml = pagerHtml + "..";
									} else if (totalPages - 1 == page
											&& totalPages - curpage > 5
											&& totalPages > 11) {
										pagerHtml = pagerHtml + "..";
									} else if (1 == page
											|| (curpage < 7 && page < 11)
											|| (Math.abs(page - curpage) < 5)
											|| (totalPages - curpage < 6 && totalPages
													- page < 10)
											|| totalPages == page
											|| totalPages <= 11) {
										pagerHtml = pagerHtml
												+ "<a href=\"javascript:goToPage("
												+ page + ")" + ";\">" + page
												+ "</a>";
									}
								}
								$("#bs_results_pager").html(pagerHtml);
							}

						}
						else{
							// Clean the paging information
							$("#bs_results_stats_fromto").html("No groups found.");
							$("#bs_results_pager").html("&nbsp;");	
						}
						
//						//ellipsis
//						//alert("ellipsis");
//						var the_obj = $('.ellipsis_class').ThreeDots({
//							max_rows : 3,
//							alt_text_t : true
//						});
//						//ellipsis

					});

}

function onQueryError() {
	// remove progress gif
	$("#bs_results_body_inner").removeClass("bs_results_tbl_loading");
	// report problem to the user
	$("#bs_results_tbody")
			.html(
					"<tr class=\"bs_results_tr_error\"><td colspan=\"9\">There was an error processing the query. Please try again later.</td></tr>");
}

function goToPage(pPage) {
	pageInit = pPage;
	var newQuery = $.query.set("keywords", keywords).set("sortby", sortBy).set(
			"sortorder", sortOrder).set("page", pageInit).set("pagesize",
			pageSize).toString();
	var urlPage = "biosamplesgroup/browse-table.html" + newQuery;
	QuerySampleGroup(urlPage);
}



function getQueryStringParam(paramName, defaultValue) {
	var param = $.query.get(paramName);
	if ("" !== param) {
		return param;
	} else {
		return defaultValue;
	}
}

function getQueryArrayParam(paramName) {
	var param = $.query.get(paramName);
	if (!jQuery.isArray(param)) {
		return new Array(param);
	} else {
		return param;
	}
}

function getQueryBooleanParam(paramName) {
	var param = $.query.get(paramName);
	return (true === param || "" != param) ? true : undefined;
}

function aeSort(psortby) {
	// alert("sortby->" + psortby);

	// im sorting the same attribute
	if (psortby == sortBy) {
		sortOrder = (sortOrder == "ascending" ? "descending" : "ascending");
	} else {
		sortBy = psortby;
		sortOrder = sortDefault[sortBy];
	}
	// alert("sortBy->" + sortBy + ";sortOrder->" + sortOrder);
	pageInit = $.query.get("page") || pageInit;
	pageSize = $.query.get("pagesize") || pageSize;

	for ( var key in sortDefault) {
		// do something with key and hmap[key]
		$("#bs_results_header_" + key).find("div.table_header_inner")
				.removeClass("table_header_sort_desc").removeClass(
						"table_header_sort_asc");

	}
	var thElt = $("#bs_results_header_" + sortBy);

	if (null != thElt) {
		// alert("#bs_results_header_" + sortBy);
		if ("" != sortOrder) {
			var divElt = thElt.find("div.table_header_inner");
			if (null != divElt) {
				// alert("div.table_header_inner not null");
				divElt
						.addClass("descending" == sortOrder ? "table_header_sort_desc"
								: "table_header_sort_asc");
			}
		}
	}

	var newQuery = $.query.set("keywords", keywords).set("sortby", sortBy).set(
			"sortorder", sortOrder).set("page", pageInit).set("pagesize",
			pageSize).toString();

	// var pageName =
	// /\/?([^\/]+)$/.exec(decodeURI(window.location.pathname))[1];
	var urlPage = "biosamplesgroup/browse-table.html" + newQuery;

	QuerySampleGroup(urlPage);
	// window.location.href = urlPage;

}
