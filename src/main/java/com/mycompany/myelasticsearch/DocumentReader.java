/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.myelasticsearch;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.pdf.PDFParser;
import org.apache.tika.sax.BodyContentHandler;
import org.elasticsearch.client.Client;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeBuilder;
import static org.elasticsearch.node.NodeBuilder.nodeBuilder;
import org.json.JSONObject;
import org.xml.sax.SAXException;

/**
 *
 * @author neera
 */
public class DocumentReader {

    public static String outputArray[];
    public static String docText;

    public static String readDocument(String filepath, String fileName) {
        Tika tika = new Tika();
        final File folder = new File(filepath);
        String fileEntry = filepath + fileName;
        String filetype = tika.detect(fileEntry);
        System.out.println("FileType " + filetype);
        BodyContentHandler handler = new BodyContentHandler(-1);

        Metadata metadata = new Metadata();

        FileInputStream inputstream = null;
        try {
            inputstream = new FileInputStream(fileEntry);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(DocumentReader.class.getName()).log(Level.SEVERE, null, ex);
        }
        ParseContext pcontext = new ParseContext();

        //parsing the document using PDF parser
        PDFParser pdfparser = new PDFParser();
        try {
            pdfparser.parse(inputstream, handler, metadata, pcontext);
        } catch (IOException ex) {
            Logger.getLogger(DocumentReader.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SAXException ex) {
            Logger.getLogger(DocumentReader.class.getName()).log(Level.SEVERE, null, ex);
        } catch (TikaException ex) {
            Logger.getLogger(DocumentReader.class.getName()).log(Level.SEVERE, null, ex);
        }

        //getting the content of the document
        docText = handler.toString().replaceAll("(/[^\\da-zA-Z.]/)", "");
        outputArray = docText.split("Article|Section|Borrower|Agents");
        int count = 0;
        String outputArrayString = "";
        for (String o : outputArray) {
            count++;
            outputArrayString += "Count" + count + o;
            System.out.println();

        }
        System.out.println(outputArrayString);
        return docText;
    }

    public static void getAllCapitalWords(String docText) {
        Set<String> allCapsWords = new HashSet<>();
        Pattern p = Pattern.compile("\\b[A-Z]{2,}\\b");
        Matcher m = p.matcher(docText);
        while (m.find()) {
            String word = m.group();
            // System.out.println(word);
            allCapsWords.add(word);
        }

        for (String allcaps : allCapsWords) {
             System.out.println(allcaps);
        }
        System.out.println("Caps word count" + allCapsWords.size());
        org.json.simple.JSONObject obj = new org.json.simple.JSONObject();
        int count = 0;
        for (String output : outputArray) {
            obj.put(String.valueOf(count), output.replaceAll("\\s+", " "));
            count++;
        }
        try {

            FileWriter file = new FileWriter("CapsWord.txt");
            file.write(obj.toJSONString());
            file.flush();
            file.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String parseString(String documentText, Client client) throws FileNotFoundException {
        System.out.println("----INDEXOF----");
        int definedTermsStart = documentText.indexOf("ARTICLE 1");
        
        
        
        int definedTermsEnd = documentText.indexOf("ARTICLE 2");

        int start = documentText.indexOf('“', definedTermsStart);
        int end = documentText.indexOf('”', start);
        int delimiter = -1;
        int count = 0;
        JSONObject obj = new JSONObject();

        while (start != -1 && end < definedTermsEnd) {

            System.out.println(documentText.substring(start + 1, end));

            delimiter = documentText.indexOf(".", end + 1);

            System.out.println(documentText.substring(end + 1, delimiter));

            System.out.println();

            obj.put(documentText.substring(start + 1, end), documentText.substring(end + 1, delimiter));
            
            count++;

            start = documentText.indexOf('“', end + 1);
            end = documentText.indexOf('”', start);

        }
        try {
            client.prepareIndex("definedterms", "term", "1")
                    .setSource(obj.toString()).execute().actionGet();

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        PrintWriter out = new PrintWriter("DefinedTerms.json");
        out.println(obj.toString());

        System.out.println("stop");
        // node.close();
        return obj.toString();
    }

}
