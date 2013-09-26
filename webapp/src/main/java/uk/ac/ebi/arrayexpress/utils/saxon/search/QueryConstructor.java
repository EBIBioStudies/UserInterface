package uk.ac.ebi.arrayexpress.utils.saxon.search;

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

import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class QueryConstructor implements IQueryConstructor {
	public Query construct(AbstractIndexEnvironment env,
			Map<String, String[]> querySource) throws ParseException {
		BooleanQuery result = new BooleanQuery();
		for (Map.Entry<String, String[]> queryItem : querySource.entrySet()) {
			if (env.fields.containsKey(queryItem.getKey())
					&& queryItem.getValue().length > 0) {
				QueryParser parser = new EnhancedQueryParser(env,
						queryItem.getKey(), env.indexAnalyzer);
				parser.setDefaultOperator(QueryParser.Operator.AND);
				for (String value : queryItem.getValue()) {
					if (!"".equals(value)) {
						if (env.fields.get(queryItem.getKey()).shouldEscape) {
							value = value.replaceAll(
									"([+\"!()\\[\\]{}^~*?:\\\\-]|&&|\\|\\|)",
									"\\\\$1");
						}
						//RPE: I need to clean the fields name becau of the using of {} [ ] on the description
						Query q = parser.parse(QueryConstructor.CleanFilterFieldsName(value));
						result.add(q, BooleanClause.Occur.MUST);
					}
				}
			}
		}
		return result;
	}

	public static String CleanFilterFieldsName(String query) {
		Pattern pattern = Pattern.compile("[^\\s]+?\\s*\\:");
		Matcher matcher = pattern.matcher(query);
		StringBuffer sb = new StringBuffer();
		while (matcher.find()) {

			String macthReplaced = QueryConstructor
					.CleanIndividualFilterFieldName(matcher.group());
			System.out.println("TT->"+macthReplaced);
			matcher.appendReplacement(sb, macthReplaced.replace("\\", "\\\\"));
			//System.out.println("��" + matcher.group() + "��");
			//System.out.println(matcher.group());
		}
		matcher.appendTail(sb);
		//System.out.println("FIBAL-" + sb);
	
		return sb.toString();
	}

	public static String CleanIndividualFilterFieldName(String field) {
		Pattern pattern = Pattern
				.compile("[\\+|\\-|\\||\\{|\\}|\\[|\\]|\\^|\\~]");
		// + - && || ! ( ) { } [ ] ^ " ~ * ? : \ (special lucene characters
		Matcher matcher = pattern.matcher(field);
		StringBuffer sb = new StringBuffer();
		while (matcher.find()) {
			String macthReplaced = "\\\\" + matcher.group();
			matcher.appendReplacement(sb, macthReplaced);
		}
		matcher.appendTail(sb);
		//System.out.println("PartiaL-" + sb);
		return sb.toString();
	}

	public static void main(String[] args) {
		QueryConstructor.CleanFilterFieldsName("rui[ze]:dfdfd AND pedro+ter : rerere");
	}

	public Query construct(AbstractIndexEnvironment env, String queryString)
			throws ParseException {
		QueryParser parser = new EnhancedQueryParser(env, env.defaultField,
				env.indexAnalyzer);
		parser.setDefaultOperator(QueryParser.Operator.AND);
		return parser.parse(queryString);
	}
}
