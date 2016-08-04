/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.myelasticsearch;

import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import opennlp.tools.cmdline.parser.ParserTool;
import opennlp.tools.parser.Parse;
import opennlp.tools.parser.Parser;
import opennlp.tools.parser.ParserFactory;
import opennlp.tools.parser.ParserModel;

/**
 *
 * @author neera
 */
public class PhraseDetection {

    static Set<String> nounPhrases = new HashSet<>();

    public static Set<String> getPhrases(String docText) {
        List<String> allSentence = Stanford.getSentence(docText);
        InputStream modelInParse = null;
        String tocOutput = "";
        for (String sentence : allSentence) {
            try {
                //load chunking model
                modelInParse = new FileInputStream("en-parser-chunking.bin"); //from http://opennlp.sourceforge.net/models-1.5/
                ParserModel model = new ParserModel(modelInParse);

                //create parse tree
                Parser parser = ParserFactory.create(model);
                Parse topParses[] = ParserTool.parseLine(sentence, parser, 1);

                //call subroutine to extract noun phrases
                for (Parse p : topParses) {
                    getNounPhrases(p);
                }

                //print noun phrases
                for (String s : nounPhrases) {
                    System.out.println(s);
                    tocOutput += s;
                }

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (modelInParse != null) {
                    try {
                        modelInParse.close();
                    } catch (IOException e) {
                    }
                }
            }

            try {
                FileWriter file = new FileWriter("NounPhrases.txt");
                file.write(tocOutput.toString());
                file.flush();
                file.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return nounPhrases;
    }

    //recursively loop through tree, extracting noun phrases
    public static void getNounPhrases(Parse p) {

        if (p.getType().equals("NP")) { //NP=noun phrase
            nounPhrases.add(p.getCoveredText());
        }
        for (Parse child : p.getChildren()) {
            getNounPhrases(child);
        }
    }
}
