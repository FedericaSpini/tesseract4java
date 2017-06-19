package de.vorb.tesseract.gui.work;

import de.vorb.tesseract.gui.controller.TesseractController;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by federica on 07/06/17.
 */
public class DictionaryWorker
{
    private TesseractController controller;
    private Path dictionaryPath;
    private ArrayList<String> wordsToAdd;
    private String transcriptedText;

    private static ArrayList<String> PUNCTUATION = new ArrayList<String>(Arrays.asList(",","»","«"));

    public DictionaryWorker(TesseractController controller, Path dictionaryPath, String transcriptedText){
        this.controller=controller;
        this.dictionaryPath=dictionaryPath;
        this.wordsToAdd= new ArrayList<String>();
        this.transcriptedText= transcriptedText;
        //System.out.println("Creato il DiictionaryWorker");
    }

    public void doInBackGround() throws IOException {
        //parole del file dizionario
        //System.out.println("Chiamato doInBackground nel dictionaryWorker");

        ArrayList<String> dictWords=getDictWords();

        //System.out.println("Le parole nel dizionario sono: "+dictWords.toString());

        //parole del testo trascritto


        ArrayList<String> traWords= getTraWords(transcriptedText);
        //System.out.println("Le parole del testo trascritto sono: "+traWords.toString());
        //System.out.println("il testo trascritto è: "+transcriptedText);

        //parole da aggiungere al dizionario
        ArrayList<String> toAddWords= new ArrayList<String>();

        for (String s: traWords){
                if(!toAddWords.contains(s)) toAddWords.add(s);
        }
        for (String s: dictWords){
            if(!toAddWords.contains(s)) toAddWords.add(s);
        }
        //System.out.println("Le parole da aggiungere nel dizionario sono: "+toAddWords);

        Collections.sort(toAddWords);

        Files.write(dictionaryPath, toAddWords, Charset.forName("UTF-8"));


    }

    public ArrayList<String> getDictWords()throws IOException{
        String everything="";
        BufferedReader br = new BufferedReader(new FileReader(dictionaryPath.toString()));
        try {
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();

            while (line != null) {
                sb.append(line);
                sb.append(System.lineSeparator());
                line = br.readLine();
            }
            everything = sb.toString();
        } finally {
            br.close();
        }

        List<String> res= Arrays.asList(everything.split("\n"));
        ArrayList<String> res2= new ArrayList<>(res);
        return res2;
    }

    public ArrayList<String> getTraWords(String text){
        String t = text.replaceAll("\n", " ");
        for(String c: PUNCTUATION){
            t=t.replaceAll(c, " ");
        }
        t = t.replaceAll("[\\-\\+\\.\\^:,]","");
        ArrayList<String> res= new ArrayList<>(Arrays.asList(t.split(" ")));
        return res;

    }

}
