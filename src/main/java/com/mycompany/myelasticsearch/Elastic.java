/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.myelasticsearch;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;

import org.elasticsearch.search.SearchHit;

/**
 *
 * @author neera
 */
public class Elastic {

    public static Map<String, Object> putDefinedTerms(Object obj) {
        Map<String, Object> jsonDocument = new HashMap<String, Object>();
        jsonDocument.put("Object", obj);
        return jsonDocument;
    }

    public static Map<String, Object> putJsonTerm(String term, String content
    ) {

        Map<String, Object> jsonDocument = new HashMap<String, Object>();
        jsonDocument.put("term", term);
        jsonDocument.put("definition", content);
        return jsonDocument;
    }

    public static Map<String, Object> putJsonDocument(String title, String content, Date postDate,
            String[] tags, String author) {

        Map<String, Object> jsonDocument = new HashMap<String, Object>();

        jsonDocument.put("title", title);
        jsonDocument.put("content", content);
        jsonDocument.put("postDate", postDate);
        jsonDocument.put("tags", tags);
        jsonDocument.put("author", author);

        return jsonDocument;
    }

    public static void getTerm(Client client, String index, String type, String id) {

        GetResponse getResponse = client.prepareGet(index, type, id)
                .execute()
                .actionGet();
        Map<String, Object> source = getResponse.getSource();

        System.out.println("------------------------------");
        System.out.println("Index: " + getResponse.getIndex());
        System.out.println("Type: " + getResponse.getType());
        System.out.println("Id: " + getResponse.getId());
        System.out.println("Version: " + getResponse.getVersion());
        System.out.println(source);
        System.out.println("------------------------------");

    }

    public static Map<String, Object> getDocument(Client client, String index, String type, String id) {

        GetResponse getResponse = client.prepareGet(index, type, id)
                .execute()
                .actionGet();
        Map<String, Object> source = getResponse.getSource();

        return source;

    }

    public static Map<String, Object> getDefinedTerm(Client client, String index, String type, String id, String key) {

        GetResponse getResponse = client.prepareGet(index, type, id)
                .execute()
                .actionGet();
        Map<String, Object> source = getResponse.getSource();
        Map<String, Object> mySource = new HashMap();

        for (String s : source.keySet()) {
            if (s.equalsIgnoreCase(key)) {
                mySource.put(s, source.get(s));
                System.out.println(mySource.keySet() + ":" + mySource.entrySet());
            }
        }
        return mySource;

    }

    public static ArrayList<String> searchDocumentColumn(Client client, String index, String type,
            String field, String value) {

        QueryBuilder qb = QueryBuilders.queryString(value);

        SearchResponse response2 = client.prepareSearch(index).setTypes(type).addField(field).execute().actionGet();
        SearchHit[] results2 = response2.getHits().getHits();

        System.out.println("Current results: " + results2.length);
        ArrayList<String> linkedColumns = new ArrayList();
        for (SearchHit hit : results2) {
            System.out.println("------------------------------");
            String columnValue = hit.field(field).getValue().toString();
            //Map<String,Object> result = hit.getSource();  
            linkedColumns.add(columnValue);
            System.out.println(columnValue);
        }
        return linkedColumns;
    }

    public static void searchDocumentLinking(Client client, String index, String type,
            String value) {

        QueryBuilder qb = QueryBuilders.queryString(value);

        SearchResponse response2 = client.prepareSearch(index).setTypes(type).setQuery(qb).execute().actionGet();
        SearchHit[] results2 = response2.getHits().getHits();
        for (SearchHit hit : results2) {
            System.out.println("------------------------------");
            Map<String, Object> result = hit.getSource();
            //Finding refered Links 
            Iterator it = result.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry) it.next();
                System.out.println(pair.getKey() + " = " + pair.getValue());
                String valueLinks = (String) pair.getValue();
                int valueIndex = valueLinks.indexOf("meaning specified in ");
                String sectionLink = valueLinks.substring(valueIndex + 21, valueIndex + 21 + 12);

                if (valueIndex != -1) {
                    searchDocumentColumn(client, index, type, sectionLink, value);

                }
                System.out.println("sectionLink " + sectionLink);
                // avoids a ConcurrentModificationException
            }

            System.out.println(result);
        }
    }

    public static void searchDocument(Client client, String index, String type,
            String value) {

        QueryBuilder qb = QueryBuilders.queryString(value);
        SearchResponse response2 = client.prepareSearch(index)
                .setTypes(type)
                .setQuery(qb)
                .setFrom(0)
                .setSize(60)
                .setExplain(true).setSearchType(SearchType.DEFAULT)
                .execute()
                .actionGet();
        SearchHit[] results2 = response2.getHits().getHits();
        for (SearchHit hit : results2) {
            System.out.println("------------------------------");
            Map<String, Object> result = hit.getSource();

            System.out.println(result);
        }
    }

    public static void deleteDocument(Client client, String index, String type, String id) {

        DeleteResponse response = client.prepareDelete(index, type, id).execute().actionGet();
        System.out.println("Information on the deleted document:");
        System.out.println("Index: " + response.getIndex());
        System.out.println("Type: " + response.getType());
        System.out.println("Id: " + response.getId());
        System.out.println("Version: " + response.getVersion());
    }
}
