/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.myelasticsearch;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.InetAddress;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.tika.Tika;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.xml.sax.SAXException;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.pdf.PDFParser;
import org.apache.tika.sax.BodyContentHandler;
import org.elasticsearch.node.Node;
import static org.elasticsearch.node.NodeBuilder.nodeBuilder;
import org.json.JSONObject;

/**
 *
 * @author Neeraj
 */
public class MainClass {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        // TODO code application logic here
        Tika tika = new Tika();
        String fileEntry = "C:\\Contract\\Contract1.pdf";
        String filetype = tika.detect(fileEntry);
        System.out.println("FileType " + filetype);
        BodyContentHandler handler = new BodyContentHandler(-1);

        Metadata metadata = new Metadata();

        FileInputStream inputstream = null;

        try {
            inputstream = new FileInputStream(fileEntry);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(MainClass.class.getName()).log(Level.SEVERE, null, ex);
        }
        ParseContext pcontext = new ParseContext();

        //parsing the document using PDF parser
        PDFParser pdfparser = new PDFParser();
        try {
            pdfparser.parse(inputstream, handler, metadata, pcontext);
        } catch (IOException ex) {

            Logger.getLogger(MainClass.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SAXException ex) {
            Logger.getLogger(MainClass.class.getName()).log(Level.SEVERE, null, ex);
        } catch (TikaException ex) {
            Logger.getLogger(MainClass.class.getName()).log(Level.SEVERE, null, ex);
        }
        String docText = "";
        String outputArray[];
        String out[];
        //getting the content of the document
        docText = handler.toString().replaceAll("(/[^\\da-zA-Z.]/)", "");

        // PhraseDetection.getPhrases(docText);
        try {
            Node node = nodeBuilder().node();
            Client client = node.client();
            DocumentReader.parseString(docText, client);
            //"Borrowing should be replaced by the user input key"
            Elastic.getDefinedTerm(client, "definedterms", "term", "1", "Borrowing");
            node.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(MainClass.class.getName()).log(Level.SEVERE, null, ex);
        }
        Stanford.getSentence(docText);

        int definedTermsEnd = docText.indexOf("SCHEDULES");
        String toc = docText.substring(0, definedTermsEnd);
        String c = docText.substring(definedTermsEnd);

        System.out.println("Table of content" + toc);
        System.out.println("--------------------------------");
        System.out.println("content" + c);

        out = toc.split("Article|article|ARTICLE");
        int count = 0;
        String outputArrayString = "";
        int s = 0;
        StringBuffer tocOutput = new StringBuffer();

        for (String o : out) {
            if (count != 0) {
                s = Integer.parseInt(String.valueOf(o.charAt(1)));
                if (s == count) {
                    tocOutput.append(o);
                    tocOutput.append("JigarAnkitNeeraj");
                    System.out.println(s);
                }
            }
            outputArrayString += "Count" + count + o;
            count++;
            System.out.println();
        }
        System.out.println("---------------------------------------------------Content---------");
        count = 1;
        StringBuffer contentOutput = new StringBuffer();

        String splitContent[] = c.split("ARTICLE|Article");
        Node node = nodeBuilder().node();
        Client client = node.client();
        for (String o : splitContent) {
            o = o.replaceAll("[^a-zA-Z0-9.,\\/#!$%\\^&\\*;:{}=\\-_`~()“”\\s]+", "");
            o = o.replaceAll("\n", " ");
            char input = o.charAt(1);
            if (input >= '0' && input <= '9') {
                s = Integer.parseInt(String.valueOf(o.charAt(1)));
                if (s == count) {
                    //System.out.println(s);
                    JSONObject articleJSONObject = new JSONObject();
                    contentOutput.append(" \n MyArticlesSeparated \n ");
                    articleJSONObject.put("Article" + count, o.toString());
                    try {
                        try {
                            JSONObject articleJSONObject1 = new JSONObject();
                            articleJSONObject1.put("hi", "j");
                            client.prepareIndex("contract", "article", String.valueOf(count))
                                    .setSource(articleJSONObject.toString()).execute().actionGet();
                        } catch (Exception e) {
                            System.out.println(e.getMessage());
                        }
                        //"Borrowing should be replaced by the user input key"

                    } catch (Exception ex) {
                        Logger.getLogger(MainClass.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    System.out.println(s);
                    count++;
                }
                //outputArrayString += "Count" + count + o;

                contentOutput.append(o);
            }
        }
        Elastic.getDocument(client, "contract", "article", "1");
        Elastic.searchDocument(client, "contract", "article", "Lenders");
        Elastic.searchDocument(client, "contract", "article", "Negative Covenants");

        Elastic.searchDocument(client, "contract", "article", "Change in Law");
        String tableOfContent[];
        tableOfContent = tocOutput.toString().split("JigarAnkitNeeraj");

        String splitContectsAccordingToArticles[];
        splitContectsAccordingToArticles = contentOutput.toString().split("MyArticlesSeparated");
        int numberOfArticle = splitContectsAccordingToArticles.length;

        int countArticle = 1;
        Double toBeTruncated = new Double("" + countArticle + ".00");

        String section = "Section";
        toBeTruncated += 0.01;

        System.out.println(toBeTruncated);
        String sectionEnd;
        StringBuffer sectionOutput = new StringBuffer();
        int skipFirstArtcile = 0;
        JSONObject obj = new JSONObject();

        for (String article : splitContectsAccordingToArticles) {
            if (skipFirstArtcile != 0) {
                DecimalFormat f = new DecimalFormat("##.00");
                String sectionStart = section + " " + f.format(toBeTruncated);
                int start = article.indexOf(sectionStart);
                toBeTruncated += 0.01;

                System.out.println();
                sectionEnd = section + " " + f.format(toBeTruncated);

                int end = article.indexOf(sectionEnd);
                while (end != -1) {
                    sectionStart = section + " " + f.format(toBeTruncated - 0.01);
                    sectionOutput.append(" \n Key:" + sectionStart);
                    if (start < end) {
                        sectionOutput.append("\n Value:" + article.substring(start, end));
                        obj.put(sectionStart, article.substring(start, end).replaceAll("\\r\\n|\\r|\\n", " "));
                        try {
                            try {
                                JSONObject articleJSONObject1 = new JSONObject();
                                articleJSONObject1.put("hi", "j");
                                client.prepareIndex("contract", "section", String.valueOf(count))
                                        .setSource(obj.toString()).execute().actionGet();
                            } catch (Exception e) {
                                System.out.println(e.getMessage());
                            }
                            //"Borrowing should be replaced by the user input key"

                        } catch (Exception ex) {
                            Logger.getLogger(MainClass.class.getName()).log(Level.SEVERE, null, ex);
                        }

                    }

                    start = end;
                    toBeTruncated += 0.01;
                    sectionEnd = section + " " + f.format(toBeTruncated);
                    System.out.println("SectionEnd " + sectionEnd);
                    try {
                        end = article.indexOf(sectionEnd);
                    } catch (Exception e) {
                        System.out.print(e.getMessage());
                    }

                    System.out.println("End section index " + end);
                }
                end = article.length() - 1;
                sectionOutput.append(" \n Key:" + sectionStart);
                try {
                    sectionOutput.append(" \n Value:" + article.substring(start, end));
                    obj.put(sectionStart, article.substring(start, end).replaceAll("\\r\\n|\\r|\\n", " "));
                } catch (Exception e) {
                    //What if Article has No Sections
                    String numberOnly = article.replaceAll("[^0-9]", "").substring(0, 1);
                    String sectionArticle = "ARTICLE " + numberOnly;
                    sectionOutput.append(" \n Value:" + article);
                    obj.put(sectionArticle, article);

                    System.out.println(e.getMessage());
                }

                DecimalFormat ff = new DecimalFormat("##");
                toBeTruncated = Double.valueOf(ff.format(toBeTruncated)) + 1.01;
            }
            skipFirstArtcile++;
        }

        for (String article : splitContectsAccordingToArticles) {
            if (skipFirstArtcile != 0) {
                DecimalFormat f = new DecimalFormat("##.00");
                String sectionStart = section + " " + f.format(toBeTruncated);
                int start = article.indexOf(sectionStart);
                toBeTruncated += 0.01;
                System.out.println();
                sectionEnd = section + " " + f.format(toBeTruncated);

                int end = article.indexOf(sectionEnd);
                while (end != -1) {
                    sectionStart = section + " " + f.format(toBeTruncated - 0.01);
                    sectionOutput.append(" \n Key:" + sectionStart);
                    if (start < end) {
                        sectionOutput.append("\n Value:" + article.substring(start, end));
                        System.out.println(sectionOutput);
                        String patternStr = "\\n\\n+[(]";
                        String paragraphSubstringArray[] = article.substring(start, end).split(patternStr);

                        JSONObject paragraphObject = new JSONObject();
                        int counter = 0;
                        for (String paragraphSubstring : paragraphSubstringArray) {
                            counter++;
                            paragraphObject.put("Paragraph " + counter, paragraphSubstring);

                        }
                        obj.put(sectionStart, paragraphObject);

                    }

                    start = end;
                    toBeTruncated += 0.01;
                    sectionEnd = section + " " + f.format(toBeTruncated);
                    System.out.println("SectionEnd " + sectionEnd);
                    try {
                        end = article.indexOf(sectionEnd);
                    } catch (Exception e) {
                        System.out.print(e.getMessage());
                    }

                    System.out.println("End section index " + end);
                }
                end = article.length() - 1;
                sectionOutput.append(" \n Key:" + sectionStart);
                try {
                    sectionOutput.append(" \n Value:" + article.substring(start, end));
                    obj.put(sectionStart, article.substring(start, end));
                    PhraseDetection.getPhrases(docText);
                } catch (Exception e) {
                    //What if Article has No Sections
                    String sectionArticle = "ARTICLE";
                    System.out.println(e.getMessage());
                }
                DecimalFormat ff = new DecimalFormat("##");
                toBeTruncated = Double.valueOf(ff.format(toBeTruncated)) + 1.01;
            }
            skipFirstArtcile++;
        }

        Elastic.getDocument(client, "contract", "section", "1");
        Elastic.searchDocument(client, "contract", "section", "Lenders");
        Elastic.searchDocument(client, "contract", "section", "Negative Covenants");
        try {
            FileWriter file = new FileWriter("TableOfIndex.txt");
            file.write(tocOutput.toString());
            file.flush();
            file.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            FileWriter file = new FileWriter("Contract3_JSONFile.txt");
            file.write(obj.toString());
            file.flush();
            file.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            FileWriter file = new FileWriter("Contract1_KeyValueSections.txt");
            file.write(sectionOutput.toString());
            file.flush();
            file.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
