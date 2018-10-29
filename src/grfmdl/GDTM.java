package grfmdl;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import utils.Document_Cleaner;
import evaluation.B3_FScore;
import evaluation.CoherencyScore;

public class GDTM {
	
	RI ri;
	KG kg;
	int saving_step;
	String theta_file;
	String documents_file;
	int num_top_words;
	int min_tp_cnt_trsh;
	
	public static void main(String[] args) throws IOException{
		GDTM gdtm = new GDTM(args);
		gdtm.init(args);
		gdtm.run();
	}
	
	/**
	 * @author kambiz ghoorchian
	 * @throws IOException
	 */
	public GDTM(String[] args){
		//init(args);
		//extract_doc_topics_GDTM(theta_file, "sample/doc_topics.txt");
		//CoherencyScore coh = new CoherencyScore(documents_file, "sample/doc_topics.txt", num_top_words, min_tp_cnt_trsh);
		//System.out.println("#################################");
		//B3_FScore bf = new B3_FScore("sample/ground_truth.txt", "sample/doc_topics.txt", min_tp_cnt_trsh);
	}	

	/**
	 * @author kambiz ghoorchian
	 * @param args
	 */
	public void init(String[] args){
		
		double delta=60;
		double pet=1.0;
		double fwt=0.1;

		double mwt = 0.3;
		int dim=2000;
		int noz=8;
		int win=2;
		int pi=1;
		int skip=1;
		saving_step=2000;
		documents_file = "data/input/documents.txt";
		theta_file = "data/output/document_topic_distribution.txt";
		
		try {
			Properties props = new Properties();
			FileInputStream in = new FileInputStream("props.properties");
			props.load(in);
			in.close();
			delta = Double.parseDouble(props.getProperty("delta"));
			pet = Double.parseDouble(props.getProperty("alpha"));
			fwt = Double.parseDouble(props.getProperty("gamma"));
			dim = Integer.parseInt(props.getProperty("dim"));
			noz = Integer.parseInt(props.getProperty("noz"));
			win = Integer.parseInt(props.getProperty("win"));
			skip = Integer.parseInt(props.getProperty("skip"));
			saving_step = Integer.parseInt(props.getProperty("SN"));
			documents_file = props.getProperty("in");
			theta_file = props.getProperty("out");
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		File thetaFile = new File(theta_file);
		if(thetaFile.exists())
			thetaFile.delete();

		System.out.println("################# Initialize RI and KG #################");
		System.out.println("Initialize Random Index Generator.");
		ri = new RI(dim, noz, win, pi, delta);
		System.out.println("Initialize Knowledge Graph Manager.");
		kg = new KG(dim, delta, ri, mwt, pet, fwt, skip);
	}
	
	
	/**
	 * @author kambiz ghoorchian
	 */
	public void run(){
		try{
			System.out.println("################# Begin Streaming #################");
			BufferedReader fileReader = new BufferedReader(new InputStreamReader(new FileInputStream(documents_file), StandardCharsets.UTF_8));
			String line = "";
			
			int time_stamp = 0;
			long initial_ts = System.currentTimeMillis();
			long start = System.currentTimeMillis();
			
			Document_Cleaner dc = new Document_Cleaner("longstoplist_en.txt");
			
			while((line = fileReader.readLine()) != null){
				line = dc.str_cln(line);
				if(line.split("\\s").length > 2){
					ri.update_vecs(line);
					kg.update(line, time_stamp);
					time_stamp++;
					
					if(time_stamp % saving_step == 0){
						double ts = (System.currentTimeMillis() - start) * 1.0 / 1000.0;
						double tts = (System.currentTimeMillis() - initial_ts) * 1.0 / 1000.0;
						System.out.println(time_stamp + "\t#vocabs: " + ri.vocabs.size() + "\t#Skip_Grams: " + ri.bigrams.size() + "\t#distvec: " + ri.distvec.size() + "\tTS: " + ts + "\tTTS: " + tts);
						//System.out.println(time_stamp + "\t" + ts + "\tTTS:" + tts);
						kg.saveTheta(theta_file);
						start = System.currentTimeMillis();
					}
				}
			}
			fileReader.close();
		}catch(Exception ex){
			ex.printStackTrace();
		}
		
		System.out.println("################# Save Theta #################\n");
		kg.saveTheta(theta_file);
	}
	

	/**
	 * To extract a single topic per document as the topic with the maximum likelihood. 
	 * @param theta
	 * @param output
	 */
	private void extract_doc_topics_GDTM(String document_topic_distribution, String output) {
		try{
			BufferedReader br = new BufferedReader(new FileReader(new File(document_topic_distribution)));
			BufferedWriter rw = new BufferedWriter(new FileWriter(new File(output)));
			String line = "";
			while((line = br.readLine()) != null){
				if(!line.isEmpty()){
					String[] probs = line.trim().split("\\s");
					double maxPro = -1;
					String maxToic = "-1";
					for(int i = 0; i < probs.length; i++){
						String topic = probs[i].trim().split(":")[0];
						double pro = Double.parseDouble(probs[i].trim().split(":")[1]);
						if(pro > maxPro){
							maxPro = pro;
							maxToic = topic;
						}
					}
					rw.write(maxToic + "\n");
				}else{
					rw.write("-1\n");
				}
			}
			br.close();
			rw.flush();
			rw.close();
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
}
