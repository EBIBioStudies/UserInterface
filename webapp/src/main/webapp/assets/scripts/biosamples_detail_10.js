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

// the next funtions are used for paging the samples inside a group
// ######### PAGING SAMPLES INSIDE A GROUP OF SAMPLES ############
var query = new Object();
var pageName = /\/?([^\/]+)$/.exec(decodeURI(window.location.pathname))[1];
var anchor = decodeURI(window.location.hash);

// all the fileds are numbers excepts the first one that is "sampleaccession".
// If you need to change it here, be careful and change it also in the
// biosamplesgroup-html.xsl
var sortDefault = {
	"accession" : "ascending",
	"1" : "ascending",
	"2" : "ascending",
	"3" : "ascending",
	"4" : "ascending",
	"5" : "ascending",
	"6" : "ascending",
	"7" : "ascending",
	"8" : "ascending",
	"9" : "ascending",
	"10" : "ascending",
	"11" : "ascending",
	"12" : "ascending",
	"13" : "ascending",
	"14" : "ascending",
	"15" : "ascending",
	"16" : "ascending",
	"17" : "ascending",
	"18" : "ascending",
	"19" : "ascending",
	"20" : "ascending",
	"21" : "ascending",
	"22" : "ascending",
	"23" : "ascending",
	"24" : "ascending",
	"25" : "ascending",
	"26" : "ascending",
	"27" : "ascending",
	"28" : "ascending",
	"29" : "ascending",
	"30" : "ascending",
	"31" : "ascending",
	"32" : "ascending",
	"33" : "ascending",
	"34" : "ascending",
	"35" : "ascending",
	"36" : "ascending",
	"37" : "ascending",
	"38" : "ascending",
	"39" : "ascending",
	"40" : "ascending"
};

var sortTitle = {
	"1" : "name",
	"2" : "name",
	"3" : "name",
	"4" : "name",
	"5" : "name"
};

// these are used when i make a new query in samples
var sortByDefault = "accession";
var sortOrderDefault = sortDefault[sortByDefault];
var pageInitDefault = "1";

// these are used to mantain the current srt and order (they are reset when I
// make a new search)
var sortBy = "accession";
var sortOrder = sortDefault[sortBy];

var pageInit = pageInitDefault;
var pageSize = "25";
var sampleskeywords = $.query.get("sampleskeywords") ? $.query
		.get("sampleskeywords") : "";
var keywords = $.query.get("keywords") ? $.query.get("keywords") : "";

// if I;m showing the detail I will get all the samples using paging
$(function() {
	// $("#ae_results_body_inner").removeClass("ae_results_tbl_loading");

	// I need to initialize the sorting on the defaultfield
	// alert(sortBy);

	pageInit = $.query.get("samplepage") || pageInit;
	pageSize = $.query.get("samplepagesize") || pageSize;
	sortBy = $.query.get("samplesortby") || sortBy;
	// alert(sortBy);
	sortOrder = $.query.get("samplesortorder") || sortDefault[sortBy];

	var thElt = $("#bs_results_header_" + sortBy);
	if (null != thElt) {
		// alert("#bs_results_header_" + sortBy);
		if ("" != sortOrder) {

			var divElt = thElt.find("span.table_header_inner");
			if (null != divElt) {
				// alert("not null");
				// I'm using textContent because innerText doesnt work on
				// FireFox

				var hasInnerText = (divElt[0].innerText != undefined) ? true
						: false;
				if (hasInnerText) {
					if ("descending" == sortOrder) {
						divElt[0].innerHTML = divElt[0].innerText
								+ "<i class='aw-icon-angle-down'></i>";
					} else {
						divElt[0].innerHTML = divElt[0].innerText
								+ "<i class='aw-icon-angle-up'></i>";
					}
				} else {

					if ("descending" == sortOrder) {
						divElt[0].innerHTML = divElt[0].textContent
								+ "<i class='aw-icon-angle-down'></i>";
					} else {
						divElt[0].innerHTML = divElt[0].textContent
								+ "<i class='aw-icon-angle-up'></i>";
					}
				}

			}
		}
	}

	/* hint to the lucene highlight */
	$("#bs_results_box").find(".ae_text_hit").attr("title",
			"This is exact string matched for input query terms");
	$("#bs_results_box").find(".ae_text_syn").attr("title",
			"This is synonym matched from Experimental Factor Ontology");
	$("#bs_results_box").find(".ae_text_efo").attr("title",
			"This is matched child term from Experimental Factor Ontology");
	/* hint to the lucene highlight */
	// if (-1 == url.indexOf("browse")) {
	var keywordsFixed = $.query.get("sampleskeywords").toString();
	// If I was filtering the search for some field I will not apply a search to
	// the samples
	var aux = keywordsFixed.match(/\s*\w\s*:/g);
	if (aux != null) {
		keywordsFixed = "";
	}

	var newQuery = $.query.set("keywords", keywordsFixed).set("sortby", sortBy)
			.set("sortorder", sortOrder).set("page", pageInit).set("pagesize",
					pageSize).toString();
	var pageName = /\/?([^\/]+)$/.exec(decodeURI(window.location.pathname))[1];
	var urlPage = "../sample/browse/" + pageName + newQuery;
	// alert(urlPage);
	updateSamplesList(urlPage);

	var basePath = decodeURI(window.location.pathname).replace(/\/\w+\.\w+$/,
			"/");
	// alert(basePath);
	$("#bs_keywords_field").autocomplete(
			"../" + "keywords.txt?domain=biosamplessample", {
				matchContains : false,
				selectFirst : false,
				scroll : true,
				max : 2500,
				fields : getFields(),
				requestTreeUrl : "../" + "efotree.txt"
			});
	// }

	// manage 2 scrolbars
	$("#wrapper_top_scroll").scroll(
			function() {
				// alert("wrapper_top_scroll SCROLL");
				$(".attr_table_scroll").scrollLeft(
						$("#wrapper_top_scroll").scrollLeft());
			});
	$(".attr_table_scroll").scroll(
			function() {
				// alert("attr_table_scroll SCROLL");
				$("#wrapper_top_scroll").scrollLeft(
						$(".attr_table_scroll").scrollLeft());
			});

});

// function used to filter the fields on the autocomplete
function getFields() {
	var arrayFields = [];
	/*
	 * var field=""; // I'm using textContent because innerText doesnt work on
	 * FireFox for (i = 0; i < ($("span.table_header_inner").length); i++) { var
	 * hasInnerText = ($("span.table_header_inner")[i].innerText != undefined) ?
	 * true : false; if (hasInnerText) { field=
	 * $("span.table_header_inner")[i].innerText; } else { field=
	 * $("span.table_header_inner")[i].textContent; // + }
	 * arrayFields[i]="attribute<" +field.trim().toLowerCase() + ">";
	 *  } //alert("arrayFields->"+arrayFields);
	 */
	return arrayFields;
}

// this function is called by juqery.autocomplete-ebi.js
function isElementOnArray(arr, obj) {
	for ( var i = 0; i < arr.length; i++) {
		if (arr[i].toLowerCase() == obj.toLowerCase())
			return true;
	}
}

// var pageName = /\/?([^\/]+)$/.exec(decodeURI(window.location.pathname))[1];

// function responsible for managing the back history properly (this function
// uses the values added during the sorting + paging + pagingSize)
if (typeof (window.history.pushState) == 'function') {
	$(window)
			.bind(
					'popstate',
					function(event) {
						var state = event.originalEvent.state;
						if (state != null) {
							// alert("estado pro qual eu quero ir->" +state);
							removeAllSortingIndications();
							updateSamplesList(state.url);
							sortBy = state.sortby;
							sortOrder = state.sortorder;
							// alert(state.sortby + state.sortorder + "###" +
							// state.url);
							// put the keywords
							if (state.keywords != null) {
								document.forms['bs_query_form'].bs_keywords_field.value = state.keywords;
							}
							addSortingIndication(sortBy, sortOrder);
						}
					});
}

function updateSamplesList(urlPage) {
	$
			.get(
					urlPage,
					function(data) {
						// alert('Load was performed.' + data);
						// TODO: find a different solution (this was done
						// because a IE problem)
						// var
						// dataParsed=data.replace(/<\?xml(.*?)<tr>/g,"<tr>");

						$("#bs_results_tbody").html(data);

						// get stats from the first row
						// I need to get the numbers before an afterwards I put
						// the html without the DIVs - To Solve IE problems
						var total = $("#bs_results_total").text();
						var from = $("#bs_results_from").text();
						var to = $("#bs_results_to").text();
						var curpage = $("#bs_results_page").text();
						var pagesize = $("#bs_results_pagesize").text();

						// split the tr from left middel and right

						var count = to - from;
						samplesleftstring = "";
						samplesmiddlestring = "";
						samplesrigthstring = "";
						commonsamplesstring = ""
							
						// for common attributes I just need to do the replace once (only one row)
							commonsamplesstring += "<tr>"
								+ $("#samplescommon").html() + "</tr>";

						for (i = 1; i <= (count + 1); i++) {
							// alert("Found bs_results_total element2: " +
							// pars3[i].innerHTML);
							countleft = "#samplesleft" + i;
							countmiddle = "#samplesmiddle" + i;
							countrigth = "#samplesright" + i;
							samplesleftstring += "<tr>" + $(countleft).html()
									+ "</tr>";
							samplesmiddlestring += "<tr>"
									+ $(countmiddle).html() + "</tr>";
							samplesrigthstring += "<tr>" + $(countrigth).html()
									+ "</tr>";
							// alert("fixed->" + $("#samplesmiddle1").html());
							// alert("fixed->" + $("#samplesmiddle1").text());
							// alert("left" + i + "->" + $(countleft).html());
						}
						// I need to use /i because in IE the <bs_value_att> is
						// transformed in <BS_VALUE_ATT>
						commonsamplesstring = commonsamplesstring.replace(
								/<bs_value_att>/gi, "<td>");
						commonsamplesstring = commonsamplesstring.replace(
								/<\/bs_value_att>/gi, "<\/td>");
						samplesleftstring = samplesleftstring.replace(
								/<bs_value_att>/gi, "<td>");
						samplesleftstring = samplesleftstring.replace(
								/<\/bs_value_att>/gi, "<\/td>");
						// alert("left table->" + samplesleftstring);
						samplesmiddlestring = samplesmiddlestring.replace(
								/<bs_value_att>/gi, "<td>");
						samplesmiddlestring = samplesmiddlestring.replace(
								/<\/bs_value_att>/gi, "<\/td>");
						// alert("middle table->" + samplesmiddlestring);
						samplesrigthstring = samplesrigthstring.replace(
								/<bs_value_att>/gi, "<td align='middle'>");
						samplesrigthstring = samplesrigthstring.replace(
								/<\/bs_value_att>/gi, "<\/td>");
						// alert("right table->" + samplesrigthstring);

						$("#bs_results_tbody").html("");
						$("#bs_results_tbody_common").html(commonsamplesstring);
						$("#bs_results_tbody_left").html(samplesleftstring);
						$("#bs_results_tbody_middle").html(samplesmiddlestring);
						$("#bs_results_tbody_right").html(samplesrigthstring);

						var totalPages = total > 0 ? Math.floor((total - 1)
								/ pagesize) + 1 : 0;

						// $("#bs_results_tbody").html(dataParsed);

						// alert('Load was performed.' + dataParsed);

						/*
						 * hint to the lucene highlight - the right side is the
						 * link, so I dont need
						 */
						$("#bs_results_tbody_common")
								.find(".ae_text_hit")
								.attr("title",
										"This is exact string matched for input query terms");
						$("#bs_results_tbody_common")
								.find(".ae_text_syn")
								.attr("title",
										"This is synonym matched from Experimental Factor Ontology");
						$("#bs_results_tbody_common")
								.find(".ae_text_efo")
								.attr("title",
										"This is matched child term from Experimental Factor Ontology");
						$("#bs_results_tbody_left")
								.find(".ae_text_hit")
								.attr("title",
										"This is exact string matched for input query terms");
						$("#bs_results_tbody_left")
								.find(".ae_text_syn")
								.attr("title",
										"This is synonym matched from Experimental Factor Ontology");
						$("#bs_results_tbody_left")
								.find(".ae_text_efo")
								.attr("title",
										"This is matched child term from Experimental Factor Ontology");

						$("#bs_results_tbody_middle")
								.find(".ae_text_hit")
								.attr("title",
										"This is exact string matched for input query terms");
						$("#bs_results_tbody_middle")
								.find(".ae_text_syn")
								.attr("title",
										"This is synonym matched from Experimental Factor Ontology");
						$("#bs_results_tbody_middle")
								.find(".ae_text_efo")
								.attr("title",
										"This is matched child term from Experimental Factor Ontology");
						/* hint to the lucene highlight */

						// $("#ae_results_status")
						// .html(
						// total
						// + " sample"
						// + (total != 1 ? "s" : "")
						// + " found "
						// + (totalPages > 1 ? (", displaying samples "
						// + from + " to " + to + ".")
						// : ""));
						$("#bs_results_pager").html("&nbsp;");
						if (total > 0) {

							var totalPages = total > 0 ? Math.floor((total - 1)
									/ pagesize) + 1 : 0;
							$(".bs-stats").html(
									" Showing <span>" + from + " - " + to
											+ "</span> of <span>" + total
											+ "</span> Samples");

							if (totalPages > 1) {
								var pagerHtml = "Page ";
								for ( var page = 1; page <= totalPages; page++) {
									if (curpage == page) {
										pagerHtml = pagerHtml + "<span>" + page
												+ "</span>";
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
								$(".bs-pager").html(pagerHtml);
							} else {
								$(".bs-pager").html("&nbsp;");
							}

							// pagesize
							var arrayPageSize = [ "10", "25", "50", "100",
									"250", "500" ];
							var pageSizeHtml = "Page size ";
							for ( var i = 0; i < arrayPageSize.length; i++) {
								if (pagesize == arrayPageSize[i]) {
									pageSizeHtml += "<span>" + pagesize
											+ "</span>";
								} else {
									pageSizeHtml += "<a href=\"javascript:goToPageSize("
											+ arrayPageSize[i]
											+ ")"
											+ ";\">"
											+ arrayPageSize[i] + "</a>";
									;

								}
								// Do something with element i.
							}
							$(".bs-page-size").html(pageSizeHtml);

						} else {
							// Clean the paging information
							$(".bs-stats").html("No Samples found.");
							$(".bs-page-size").html("&nbsp;");
							$(".bs-pager").html("&nbsp;");
						}
						// }

						// I need to put all the tds (from sampleaccesion tablle
						// and from the link table with the same size
						$('#src_name_table tbody tr').each(
								function(i, item) {
									// alert(i);
									var aux = "#attr_table tbody tr:eq(" + i
											+ ") td:first";
									// alert($(aux).innerHeight());
									// alert("tamanho da src_name_table->"
									// + $(this).find("td:first")
									// .innerHeight()
									// + "outer->"
									// + $(this).find("td:first")
									// .outerHeight()
									// + "tamanho da attr_table->"
									// + $(aux).innerHeight()
									// + "outer->"
									// + "tamanho da attr_table->"
									// + $(aux).outerHeight());
									$(this).find("td:first").innerHeight(
											$(aux).innerHeight());
									// alert("APOS #### tamanho da
									// src_name_table->"
									// + $(this).find("td:first")
									// .innerHeight()
									// + "outer->"
									// + $(this).find("td:first")
									// .outerHeight()
									// + "tamanho da attr_table->"
									// + $(aux).innerHeight()
									// + "outer->"
									// + "tamanho da attr_table->"
									// + $(aux).outerHeight());
								});

						$('#links_table tbody tr').each(
								function(i, item) {
									var aux = "#attr_table tbody tr:eq(" + i
											+ ") td:first";
									// alert($(aux).innerHeight());
									$(this).find("td:first").innerHeight(
											$(aux).innerHeight());

								});

						// must be updated here, after all content has been
						// loaded
						var tableWidth = $("#attr_table").width(); // 
						// alert("dynamic table width->" + tableWidth);
						$("#div_top_scroll").css('width', tableWidth);

					});

	// // manage 2 scrolbars
	// var tableWidth = $("#attr_table").width(); //
	// alert("dynamic table width->" + tableWidth);
	// jQuery("#div_top_scroll").css('width', tableWidth);//define the sizes of
	// fake top scrllbar bar div based on table size - has to be dynamic

}

function searchSamples(pKeywords) {
	// alert("par->" + pKeywords);
	sampleskeywords = pKeywords;
	// reset of status variables
	sortBy = sortByDefault;
	sortOrder = sortOrderDefault;
	pageInit = pageInitDefault;
	var newQuery = $.query.set("keywords", sampleskeywords).set("sortby",
			sortBy).set("sortorder", sortOrder).set("page", pageInit).set(
			"pagesize", pageSize).toString();
	var pageName = /\/?([^\/]+)$/.exec(decodeURI(window.location.pathname))[1];
	var urlPage = "../sample/browse/" + pageName + newQuery;
	removeAllSortingIndications();
	aeSort(""); // I'm passing "" and to mean that it is a new search
	// aeSort(sortBy);
	// updateSamplesList(urlPage);
}

function goToPage(page) {
	pageInit = page;
	var newQuery = $.query.set("keywords",
			document.forms['bs_query_form'].bs_keywords_field.value).set(
			"sortby", sortBy).set("sortorder", sortOrder).set("page", pageInit)
			.set("pagesize", pageSize).toString();
	var pageName = /\/?([^\/]+)$/.exec(decodeURI(window.location.pathname))[1];

	// I cannot use the same variable names otherwise the will be used on the
	// sampleGroup query
	var newQuerySampleGroup = $.query.set("keywords",
			document.forms['bs_query_form'].bs_keywords_field.value).set(
			"samplesortby", sortBy).set("samplesortorder", sortOrder).set(
			"samplepage", pageInit).set("samplepagesize", pageSize).toString();

	var urlPage = "../sample/browse/" + pageName + newQuery;
	if (typeof (window.history.pushState) == 'function') {
		// mantain the history on browser ans also change the URL for history
		// favorites purpose
		var urlState = "../group/" + pageName + newQuerySampleGroup;
		// I will use the urlPage to reload the data on poststate event
		window.history
				.pushState(
						{
							url : urlPage,
							sortby : sortBy,
							sortorder : sortOrder,
							keywords : keywords,
							sampleskeywords : document.forms['bs_query_form'].bs_keywords_field.value
						}, "", urlState);
	}
	updateSamplesList(urlPage);
}

function goToPageSize(pPageSize) {
	pageSize = pPageSize;
	pageInit = 1;
	var newQuery = $.query.set("sampleskeywords",
			document.forms['bs_query_form'].bs_keywords_field.value).set(
			"sortby", sortBy).set("sortorder", sortOrder).set("page", pageInit)
			.set("pagesize", pageSize).toString();
	var pageName = /\/?([^\/]+)$/.exec(decodeURI(window.location.pathname))[1];

	// I cannot use the same variable names otherwise the will be used on the
	// sampleGroup query
	var newQuerySampleGroup = $.query.set("sampleskeywords",
			document.forms['bs_query_form'].bs_keywords_field.value).set(
			"keywords", keywords).set("samplesortby", sortBy).set(
			"samplesortorder", sortOrder).set("samplepage", pageInit).set(
			"samplepagesize", pageSize).toString();

	var urlPage = "../sample/browse/" + pageName + newQuery;
	if (typeof (window.history.pushState) == 'function') {
		// mantain the history on browser ans also change the URL for history
		// favorites purpose
		var urlState = "../group/" + pageName + newQuerySampleGroup;
		// I will use the urlPage to reload the data on poststate event
		window.history
				.pushState(
						{
							url : urlPage,
							sortby : sortBy,
							sortorder : sortOrder,
							keywords : keywords,
							sampleskeywords : document.forms['bs_query_form'].bs_keywords_field.value
						}, "", urlState);
	}
	updateSamplesList(urlPage);
}

function aeSort(psortby) {
	// If i'm sorting for the actual field, I just change the sorting directions
	if (psortby != "") {
		if (psortby == sortBy) {
			sortOrder = (sortOrder == "ascending" ? "descending" : "ascending");
		} else {
			sortBy = psortby;
			sortOrder = "ascending";
		}
	}
	// else - it will use the defaults - it was done in the searchSamples
	// function but i will also do it here
	else {
		sortBy = sortByDefault;
		sortOrder = sortOrderDefault;
	}

	removeAllSortingIndications();

	var sortbyText = convertSortNumberInText(sortBy);

	var newQuery = $.query.set("keywords",
			document.forms['bs_query_form'].bs_keywords_field.value).set(
			"sortby", sortbyText).set("sortorder", sortOrder).set("page",
			pageInit).set("pagesize", pageSize).toString();

	var pageName = /\/?([^\/]+)$/.exec(decodeURI(window.location.pathname))[1];
	// alert(pageName);

	// I cannot use the same variable names otherwise the will be used on the
	// sampleGroup query
	var newQuerySampleGroup = $.query.set("sampleskeywords",
			document.forms['bs_query_form'].bs_keywords_field.value).set(
			"keywords", keywords).set("samplesortby", sortBy).set(
			"samplesortorder", sortOrder).set("samplepage", pageInit).set(
			"samplepagesize", pageSize).toString();

	var urlPage = "../sample/browse/" + pageName + newQuery;

	if (typeof (window.history.pushState) == 'function') {
		// mantain the history on browser ans also change the URL for history
		// favorites purpose
		var urlState = "../group/" + pageName + newQuerySampleGroup;
		// I will use the urlPage to reload the data on poststate event
		window.history
				.pushState(
						{
							url : urlPage,
							sortby : sortBy,
							sortorder : sortOrder,
							keywords : keywords,
							sampleskeywords : document.forms['bs_query_form'].bs_keywords_field.value
						}, "", urlState);
	}
	// alert(urlPage);
	updateSamplesList(urlPage);
	addSortingIndication(sortBy, sortOrder);

}

function convertSortNumberInText(sortBy) {

	var thElt = $("#bs_results_header_" + sortBy);
	var ret = "";

	if (null != thElt) {
		var divElt = thElt.find("span.table_header_inner");
		if (null != divElt) {
			// I'm using textContent because innerText doesnt work on
			// FireFox
			var hasInnerText = ($("span.table_header_inner")[0].innerText != undefined) ? true
					: false;
			if (hasInnerText) {
				ret = divElt[0].innerText;
			} else {
				ret = divElt[0].textContent;

			}

		}
		ret = ret.trim().toLowerCase();
		;
		// I cannot send fiels with spaces
	}
	// alert("retconvert->"+ ret);
	return ret;
}

function addSortingIndication() {
	// I just put the orientation after the query return the results (before I
	// clean all the asc and desc of all the columns, after I make the query and
	// at the end i Put the correct one)
	// var sortByValidString=sortBy.replace(/\[/,"\\[").replace(/\]/,"\\]");
	var thElt = $("#bs_results_header_" + sortBy);

	if (null != thElt) {
		// alert("#bs_results_header_" + sortBy);
		if ("" != sortOrder) {
			var divElt = thElt.find("span.table_header_inner");
			if (null != divElt) {
				// alert("div.table_header_inner not null");
				// alert(divElt[0]);
				// I'm using textContent because innerText doesnt work on
				// FireFox
				var hasInnerText = ($("span.table_header_inner")[0].innerText != undefined) ? true
						: false;
				if (hasInnerText) {
					if ("descending" == sortOrder) {
						// alert(divElt[0].innerText);
						divElt[0].innerHTML = divElt[0].innerText
								+ "<i class='aw-icon-angle-down'></i>";
					} else {
						// alert($("div.table_header_inner")[0].innerText);
						divElt[0].innerHTML = divElt[0].innerText
								+ "<i class='aw-icon-angle-up'></i>";
					}
				} else {
					if ("descending" == sortOrder) {
						divElt[0].innerHTML = divElt[0].textContent
								+ "<i class='aw-icon-angle-down'></i>";
					} else {
						divElt[0].innerHTML = divElt[0].textContent
								+ "<i class='aw-icon-angle-up'></i>";
					}
				}

			}
		}
	}
}

function removeAllSortingIndications() {

	// remove all the sort signs on the headers
	// I'm using textContent because innerText doesnt work on FireFox
	for (i = 0; i < ($("span.table_header_inner").length); i++) {
		// alert($("div.table_header_inner")[i].textContent);
		// browser implementation differences :(
		var hasInnerText = ($("span.table_header_inner")[i].innerText != undefined) ? true
				: false;
		if (hasInnerText) {
			$("span.table_header_inner")[i].innerHTML = $("span.table_header_inner")[i].innerText; // +
			// "&nbsp;";
		} else {
			// &nbsp is used to preserve the arrow space
			$("span.table_header_inner")[i].innerHTML = $("span.table_header_inner")[i].textContent; // +
			// "&nbsp;";
		}

	}

}

// ######### PAGING SAMPLES INSIDE A GROUP OF SAMPLES ############
