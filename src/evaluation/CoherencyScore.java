package evaluation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import utils.Pair;

public class CoherencyScore {
	
	public CoherencyScore(String documents, String doc_topics, int num_top_wrds, int min_tp_cnt_trsh){
		try{
			// read document topics 
			HashMap<Integer, String> docTopics = readDocTopics(doc_topics); 
			
			//##########################################################################################
			// Extract word frequencies			=> {(w1,f1), (w2,f2), ...}
			// Extract tuple frequencies		=> {(w1w2,f1), (w1w3,f2), (w2w3,f3), ...}
			// Extract Topic word frequencies	=> {(t1, {(w1,f1), (w2,f2), ...}), (t2, {(w1,f1), (w2,f2), ...}), ...}
			//##########################################################################################
			HashMap<String, AtomicInteger> wordFreq = new HashMap<String, AtomicInteger>();
			HashMap<String, AtomicInteger> tupleFereq = new HashMap<String, AtomicInteger>();
			HashMap<String, HashMap<String, AtomicInteger>> topicWordFreq = new HashMap<String, HashMap<String,AtomicInteger>>();
			HashMap<String, AtomicInteger> topic_count = new HashMap<String, AtomicInteger>();
			
			BufferedReader br = new BufferedReader(new FileReader(new File(documents)));
			String line = "";
			int doc_number = 0;
			while((line = br.readLine()) != null){
				doc_number++;
				if(doc_number > docTopics.size())
					break;
				String[] wrds = line.trim().split("\\s");
				String topic = docTopics.get(doc_number);
				if(!topic.equals("-1")){
					for(int i = 0; i < wrds.length - 1; i++){
						String w1 = wrds[i];
						if(i==0){
							// Update word Frequency for first word
							if(wordFreq.containsKey(w1)) wordFreq.get(w1).incrementAndGet(); else wordFreq.put(w1, new AtomicInteger(1));

							// Update topic word Frequency for first word
							if(topicWordFreq.containsKey(topic)){
								if(topicWordFreq.get(topic).containsKey(w1))
									topicWordFreq.get(topic).get(w1).incrementAndGet();
								else
									topicWordFreq.get(topic).put(w1, new AtomicInteger(1));
							}else{
								HashMap<String, AtomicInteger> wf = new HashMap<String, AtomicInteger>();
								wf.put(w1, new AtomicInteger(1));
								topicWordFreq.put(topic, wf);
							}
						}
						for(int j = i+1; j< wrds.length; j++){
							String w2 = wrds[j];
							
							// Update word Frequency
							if(wordFreq.containsKey(w2)) wordFreq.get(w2).incrementAndGet(); else wordFreq.put(w2, new AtomicInteger(1));

							// Update Topic word Frequency
							if(topicWordFreq.containsKey(topic)){
								if(topicWordFreq.get(topic).containsKey(w1))
									topicWordFreq.get(topic).get(w1).incrementAndGet();
								else
									topicWordFreq.get(topic).put(w1, new AtomicInteger(1));
							}else{
								HashMap<String, AtomicInteger> wf = new HashMap<String, AtomicInteger>();
								wf.put(w1, new AtomicInteger(1));
								topicWordFreq.put(topic, wf);
							}
							
							// Update Tuple Frequency
							if(tupleFereq.containsKey(w1 + "," + w2))
								tupleFereq.get(w1 + "," + w2).incrementAndGet();
							else if(tupleFereq.containsKey(w2 + "," + w1))
								tupleFereq.get(w2 + "," + w1).incrementAndGet();
							else
								tupleFereq.put(w1 + "," + w2, new AtomicInteger(1));
						}
					}
					
					//Update Topic Count
					if(topic_count.containsKey(topic)){
						topic_count.get(topic).incrementAndGet();
					}else{
						topic_count.put(topic, new AtomicInteger(1));
					}
					
				}
			}

			br.close();
			calculate_topic_coherence(wordFreq, tupleFereq, topicWordFreq, topic_count, num_top_wrds, doc_number, min_tp_cnt_trsh);
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}

	private void calculate_topic_coherence(
			HashMap<String, AtomicInteger> wordFreq,
			HashMap<String, AtomicInteger> tupleFereq,
			HashMap<String, HashMap<String, AtomicInteger>> topicWordFreq,
			HashMap<String, AtomicInteger> topic_count,
			int num_top_wrds, int doc_number, int min_tp_cnt_trsh) {
		//##########################################################################################
		// Convert HashMap to Array for sorting purpose
		//##########################################################################################
		HashMap<String, ArrayList<Pair<String, Double>>> topicWords = new HashMap<String, ArrayList<Pair<String, Double>>>();
		int count_valid_topics = 0;
		for(String topic: topicWordFreq.keySet()){
			if(topic_count.get(topic).intValue() > min_tp_cnt_trsh){
				count_valid_topics++;
				ArrayList<Pair<String, Double>> wf = new ArrayList<Pair<String, Double>>();
				for(String w: topicWordFreq.get(topic).keySet()){
					wf.add(new Pair<String, Double>(w, topicWordFreq.get(topic).get(w).doubleValue()));
				}
				topicWords.put(topic, wf);
			}
		}
		
		//##########################################################################################
		// Calculate Topic_Coherence_Score, Mean_Coherence_Score and Median_Coherence_Score
		//##########################################################################################
		ArrayList<Double> topicCoh = new ArrayList<Double>();
		double sum_tp_coh = 0.0;
		for(String tp: topicWords.keySet()){
			if(!tp.startsWith("-1")){
				String wrds = "";
				sort(topicWords.get(tp));
				List<Pair<String, Double>> tpwds = topicWords.get(tp).subList(0, Math.min(num_top_wrds, topicWords.get(tp).size()));
				double tc = 0.0;
				for(int i = 0; i < tpwds.size() - 1 ; i++){
					String w1 = tpwds.get(i).key;
					if(i == 0) wrds += w1 + " ";
					for(int j = i+1; j < tpwds.size(); j++){
						String w2 = tpwds.get(j).key;

						if(i == 0) wrds += w2 + " ";
						
						String tuple = w1 + "," + w2;
						double tuple_freq = 0.0;
						if(!tupleFereq.containsKey(tuple))
							tuple = w2 + "," + w1;
				
						if(tupleFereq.containsKey(tuple)) tuple_freq = tupleFereq.get(tuple).doubleValue();
						tc += Math.log((tuple_freq + 1) / wordFreq.get(w1).doubleValue()); 
					}
				}
				topicCoh.add(tc);
				sum_tp_coh += tc;
				System.out.println(tp + "\t" + tc + "\t" + wrds);
			}
		}
					
		System.out.println(doc_number + "\t" + topicWordFreq.size() + "\t" + count_valid_topics + "\t" + sum_tp_coh/topicCoh.size());
		
	}

	private HashMap<Integer, String> readDocTopics(String doc_topics) throws Exception{
		HashMap<Integer, String> docTopics = new HashMap<Integer, String>();
		BufferedReader br = new BufferedReader(new FileReader(new File(doc_topics)));
		String line = "";
		int doc_number = 1;
		while((line = br.readLine()) != null){
			docTopics.put(doc_number, line.trim());
			doc_number++;
		}
		br.close();
		return docTopics;
	}

	private void sort(List<Pair<String, Double>> unsorted_list) {
		Collections.sort(unsorted_list , new Comparator<Pair<String, Double>>() {public int compare(Pair<String, Double> o1 , Pair<String, Double> o2) { if(o1.value > o2.value) return -1; else
		 	if(o1.value < o2.value) return 1; else return 0; }});
	}
	
	public static void main(String[] args){
		//String docs = "documents.txt";
		//String doc_topics = "doc_topics.txt";
		//CoherencyScore coh = new CoherencyScore(docs, doc_topics, 10, 5);
	}
}