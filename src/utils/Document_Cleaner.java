package utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class Document_Cleaner {
	
	private ArrayList<String> sw = new ArrayList<String>();
	
	public Document_Cleaner(String stop_words_file){
		sw = loadStopWrods(stop_words_file);
	}
	
	/**
	 * Load Stop words from longstoplist.txt
	 * */
	private static ArrayList<String> loadStopWrods(String stplist) {
		ArrayList<String> sl = new ArrayList<String>();
		try{
			BufferedReader br = new BufferedReader(new FileReader(new File(stplist)));
			String line = "";
			while((line = br.readLine()) != null){
				sl.add(line);
			}
			br.close();
		}catch(Exception ex){
			ex.printStackTrace();
		}
		return sl;
	}
	
	/**
	 * Clean documents:
	 * URL, @ and punctuation {!\"$%&'*+,./:;<=>?[]^`{|}~()}
	 * */
	public String str_cln(String line) {
		String ret = "";
		String noURLnoMention = "";
		// clean URL and Mention
		for(String w : line.trim().split(" ")){
			if(!w.startsWith("http") && !w.startsWith("@") && !w.startsWith("#")){
				noURLnoMention += w.toLowerCase() + " ";
			}
		}
		// clean punctuation
		String noPunc = noURLnoMention.replaceAll("[!?\"$%&'*+,./:;<=>?\\[\\]^`{\\|}~()\\\\]", " ").replaceAll("\\s{2,}", " ").trim();
		for(String w : noPunc.trim().split(" ")){
			// eliminate noise and stop words
			if(w.trim().length() > 2 && w.trim().length() < 25 && !sw.contains(w.toLowerCase()))
				ret += w.toLowerCase() + " ";
		}
		return ret;
	}
	
	public static void main (String[] args){
		
		String dir = "supervised/data/";
		String corpus = dir + "corpus.txt";
		String documents = dir + "documents.txt";
		String dates = dir + "dates.txt";
		String stop_words = "longstoplist_en.txt";
		String ground_truth = dir + "ground_truth.txt";
		
		Document_Cleaner dt = new Document_Cleaner(stop_words);
		
		try{
			BufferedWriter rw_clena_doc = new BufferedWriter(new FileWriter(new File(documents)));
			BufferedWriter rw_dates = new BufferedWriter(new FileWriter(new File(dates)));
			BufferedWriter rw_groud_trt = new BufferedWriter(new FileWriter(new File(ground_truth)));
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(corpus), StandardCharsets.UTF_8));
			String line = "";
			while((line = br.readLine()) != null){
				String parts[] = line.split("\t", 4);
				String label = parts[0];
				String month = parts[1];
				String week = parts[2];
				String text = parts[3];
				text = dt.str_cln(text);
				
				if(text == "" || text.split(" ").length < 3) {
					continue;
				}
				rw_dates.write(month + "\t" + week + "\n");
				rw_clena_doc.write(text + "\n");
				rw_groud_trt.write(label + "\n");
			}
			br.close();
			rw_clena_doc.flush();
			rw_clena_doc.close();
			rw_groud_trt.flush();
			rw_groud_trt.close();
			rw_dates.flush();
			rw_dates.close();
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
}
