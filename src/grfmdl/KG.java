package grfmdl;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import utils.Pair;

public class KG {

	RI ri;
	ArrayList<String> theta = new ArrayList<String>();
	int[][] tp;
	double[][] ed;
	double [][] vwc;
	double DELTA = 0.0;
	double graph_total_weight = 0.0;
	double[] part_total_weight;
	int[] part_total_count;
	double minimum_bigram_weight_threshold;
	double max_partition_expansion_ratio;
	double function_word_threshold;
	int dimension;
	int last_topic_count = 0;
	int skipGramModel;
	int count_no_topic = 0;
	
	public KG(int dim, double in_delta, RI in_ri, double min_bgw, double max_PER, double fun_WT, int skip) {
		dimension = dim;
		DELTA = in_delta;
		ri = in_ri;
		ed = new double[dimension][dimension];
		tp = new int[dimension][dimension];
		minimum_bigram_weight_threshold = min_bgw;
		max_partition_expansion_ratio = max_PER;
		function_word_threshold = fun_WT;
		skipGramModel = skip;
		
		//ts = new int[dimension][dimension];
		vwc = new double[dimension][50000]; // vwc[0] = [vertex_total_weight, vertex_c_1_weight, vertex_c_2_weight, ...]
		part_total_weight = new double[50000];
		part_total_count = new int[50000];
	}

	/**
	 * @author kambiz ghoorchian
	 * Convert a document into subgraph using RI vectors. Appends the subgraph to the KG Matrix. Updates KG and document Topic.
	 * @param line
	 * @param time_stamp
	 */
	public void update(String line, int time_stamp) {
		String[] words = line.split(" ");

		HashMap<Integer, Double> doc_grf = create_document_skipgram_subgraph_2(words);
		
		int dominan_topic = -1;
		
		if(doc_grf.size() > 0){
			//Extract document topic weighted distribution
			double total_topic_overlap = 0.0;
			HashMap<Integer, Double> topic_weights = new HashMap<Integer, Double>();
			int count_zoro = doc_grf.size();
			for(int edge : doc_grf.keySet()){
				int[] srcDst = getSrcDst(edge);
				int topic = tp[srcDst[0]][srcDst[1]];
				if(topic > 0){
					count_zoro--;
					double weight = ed[srcDst[0]][srcDst[1]];
					total_topic_overlap += weight;
					
					if(topic_weights.containsKey(topic))
						weight += topic_weights.get(topic);
					topic_weights.put(topic, weight);
				}
			}
			
			// Find dominant topic - extract theta distribution
			if(topic_weights.size() > 0){
				double maxTopicWgt = -1.0;
				int maxTopicClr = -1;
				
				String doc_theta = "";
				
				for(int tp : topic_weights.keySet()){
					doc_theta += tp + ":" + topic_weights.get(tp)/total_topic_overlap + " ";
					
					if(topic_weights.get(tp) > maxTopicWgt){
						maxTopicWgt = topic_weights.get(tp);
						maxTopicClr = tp;
					}
				}
				if( maxTopicWgt/total_topic_overlap > 0.2){
					dominan_topic = maxTopicClr;
					theta.add(doc_theta);
				}else if(count_zoro / doc_grf.size() > 0.5){
					dominan_topic = last_topic_count + 1;
					theta.add(dominan_topic + ":1.0");
				}else{
					// New Topic Assignment 
					//dominan_topic = last_topic_count + 1;
					//theta.add(dominan_topic + ":1.0");
					dominan_topic = -1;
					theta.add("");		
				}
			}else{
				// New Topic Assignment 
				dominan_topic = last_topic_count + 1;
				theta.add(dominan_topic + ":1.0");
			}
		}else{
			theta.add("");
		}

		// Apply partitioning
		// ******************************************************************************************
		if(dominan_topic == last_topic_count + 1){
			last_topic_count = dominan_topic;	
		}
		
		if(dominan_topic == -1) 
			count_no_topic++;
		
		if(dominan_topic != -1 && (graph_total_weight == 0 || part_total_weight[dominan_topic] <= graph_total_weight * max_partition_expansion_ratio)){
		//if(dominan_topic != -1 && (graph_total_weight == 0 || part_total_weight[dominan_topic] <= graph_total_weight / (last_topic_count * 0.03) )){
		//if(dominan_topic != -1 && (graph_total_weight == 0 || r > 0.1)){
			if(dominan_topic == last_topic_count){
				for(int edge : doc_grf.keySet()){
					int[] srcDst = getSrcDst(edge);
					int src = srcDst[0];
					int dst = srcDst[1];
					double wgt = doc_grf.get(edge);
					int old_clr = tp[src][dst];
					if(old_clr == 0){
						tp[src][dst] = dominan_topic;
						
						ed[src][dst] += wgt;
						vwc[src][0] += wgt;
						vwc[dst][0] += wgt;
						
						vwc[src][dominan_topic] += ed[src][dst];
						vwc[dst][dominan_topic] += ed[src][dst];
						
						graph_total_weight += wgt;
						part_total_weight[dominan_topic] += wgt;
						part_total_count[dominan_topic]++;
					}else{
						if(ed[src][dst] < part_total_weight[old_clr] / part_total_count[old_clr]){
							tp[src][dst] = dominan_topic;
							
							part_total_weight[old_clr] -= ed[src][dst];
							part_total_count[old_clr]--;
							vwc[src][old_clr] -= ed[src][dst];
							vwc[dst][old_clr] -= ed[src][dst];
							
							ed[src][dst] += wgt;
							vwc[src][0] += wgt;
							vwc[dst][0] += wgt;
							
							vwc[src][dominan_topic] += ed[src][dst]; 
							vwc[dst][dominan_topic] += ed[src][dst];
							
							graph_total_weight += wgt;
							part_total_weight[dominan_topic] += ed[src][dst];
							part_total_count[dominan_topic]++;
						}
					}
				}
			}else{
				for(int edge : doc_grf.keySet()){
					int[] srcDst = getSrcDst(edge);
					int src = srcDst[0];
					int dst = srcDst[1];
					double wgt = doc_grf.get(edge);
					int old_clr = tp[src][dst];
					if(old_clr == 0){ //Internal edge
						if(vwc[src][dominan_topic] > 0 && vwc[dst][dominan_topic] > 0){
							tp[src][dst] = dominan_topic;
							
							ed[src][dst] += wgt;
							vwc[src][0] += wgt;
							vwc[dst][0] += wgt;
							
							vwc[src][dominan_topic] += ed[src][dst];
							vwc[dst][dominan_topic] += ed[src][dst];
							
							graph_total_weight += wgt;
							part_total_weight[dominan_topic] += wgt;
							part_total_count[dominan_topic]++;
						}else if(wgt > part_total_weight[dominan_topic] / part_total_count[dominan_topic]){  // external edge
							tp[src][dst] = dominan_topic;
							
							ed[src][dst] += wgt;
							vwc[src][0] += wgt;
							vwc[dst][0] += wgt;
							
							vwc[src][dominan_topic] += ed[src][dst];
							vwc[dst][dominan_topic] += ed[src][dst];
							
							graph_total_weight += wgt;
							part_total_weight[dominan_topic] += wgt;
							part_total_count[dominan_topic]++;
						}
					}else if(old_clr != dominan_topic){
						if(wgt > part_total_weight[dominan_topic] / part_total_count[dominan_topic] &&
								ed[src][dst] < part_total_weight[old_clr] / part_total_count[old_clr]){
							tp[src][dst] = dominan_topic;
							
							part_total_weight[old_clr] -= ed[src][dst];
							part_total_count[old_clr]--;
							vwc[src][old_clr] -= ed[src][dst];
							vwc[dst][old_clr] -= ed[src][dst];
							
							ed[src][dst] += wgt;
							vwc[src][0] += wgt;
							vwc[dst][0] += wgt;
							
							vwc[src][dominan_topic] += ed[src][dst]; 
							vwc[dst][dominan_topic] += ed[src][dst];
							
							graph_total_weight += wgt;
							part_total_weight[dominan_topic] += ed[src][dst];
							part_total_count[dominan_topic]++;
						}
					}else{
						ed[src][dst] += wgt;
						vwc[src][0] += wgt;
						vwc[dst][0] += wgt;
						
						vwc[src][dominan_topic] += ed[src][dst]; 
						vwc[dst][dominan_topic] += ed[src][dst];
						
						graph_total_weight += wgt;
						part_total_weight[dominan_topic] += ed[src][dst];
						part_total_count[dominan_topic]++;
					}
				}
			}
		}
	}

	/**
	 * @author kambiz ghoorchian
	 * @param words
	 * @return
	 */
	private HashMap<Integer, Double> create_document_skipgram_subgraph_2(String[] words) {		
		
		int bigram_normalization_factor = 0;
		HashMap<Integer, Double> doc_vec = new HashMap<Integer, Double>();
		for (int i = 0; i < words.length - skipGramModel; i++) {		
			String w_center = words[i];
			for(int j = i - skipGramModel; j <= i + skipGramModel; j++){
				if(j != i && j > 0 && j < words.length){
					String w_around = words[j];
					if(!w_center.equals(w_around)){
						//extract bi-gram vector {(elm_indx, elm_value), (elm_indx, elm_value), ...}
						
						HashMap<Integer, Double> v1 = ri.distvec.get(ri.vocabs.get(w_center).index);
						HashMap<Integer, Double> v2 = ri.distvec.get(ri.vocabs.get(w_around).index);
						
						double wgt1 = weight_func(ri, w_center, DELTA, function_word_threshold);
						if(wgt1 == 0) continue;
						double wgt2 = weight_func(ri, w_around, DELTA, function_word_threshold);
						if(wgt2 == 0) continue;

						if(v1.size() > v2.size()){		 // Join over smaller set Optimization
							HashMap<Integer, Double> temp = v1;
							v1 = v2; v2 = temp; temp = null;
						}
						
						boolean valid_bigram = false;
						for (int key : v1.keySet()) {
							if (v2.containsKey(key)) {
								valid_bigram = true;
								double mult_element_weight = v1.get(key).doubleValue() * v2.get(key).doubleValue() * wgt1 * wgt2;
								if(mult_element_weight > 0){
									if(doc_vec.containsKey(key)){
										mult_element_weight += doc_vec.get(key);
									}
									doc_vec.put(key, mult_element_weight);
								}
							}
						}
						if(valid_bigram) bigram_normalization_factor++;
					}
				}
			}
		}
		
		List<Pair<Integer, Double>> normlized_sorted_doc_vec = new ArrayList<Pair<Integer,Double>>();
		double sum_weights = 0.0;
		for (int key: doc_vec.keySet()) {
			sum_weights += doc_vec.get(key) / bigram_normalization_factor;
			normlized_sorted_doc_vec.add(new Pair<Integer, Double>(key, doc_vec.get(key)/bigram_normalization_factor));
		}
		
		if(minimum_bigram_weight_threshold < 1.0){
			sort(normlized_sorted_doc_vec);
			
			int min_w_inx = -1;
			double sumvalues = 0.0;
			for(Pair<Integer, Double> item : normlized_sorted_doc_vec){
				min_w_inx++;
				sumvalues += item.value;
				if(sumvalues >= sum_weights * minimum_bigram_weight_threshold)
					break;
			}
			if(min_w_inx > 0)
				normlized_sorted_doc_vec = normlized_sorted_doc_vec.subList(0, min_w_inx);
		}
		
		HashMap<Integer, Double> doc_grf = new HashMap<Integer, Double>();
		for (int k = 0; k < normlized_sorted_doc_vec.size() - 1 ; k++) {
			for (int l = k + 1; l < normlized_sorted_doc_vec.size(); l++) {
				int key1 = normlized_sorted_doc_vec.get(k).key.intValue();
				int key2 = normlized_sorted_doc_vec.get(l).key.intValue();
				double edge_weight = normlized_sorted_doc_vec.get(k).value.doubleValue() * normlized_sorted_doc_vec.get(l).value.doubleValue();
				
				int edge = (key1 < key2)? getEdge(key1, key2) : getEdge(key2, key1);
				
				if(doc_grf.containsKey(edge))
					edge_weight += doc_grf.get(edge);
				doc_grf.put(edge, edge_weight);
			}
		}
		return doc_grf;
	}
	
	/**
	 * @author Kambiz Ghoorchian
	 * @param output
	 * @param count
	 */
	public void saveTheta(String output){
		try{
			BufferedWriter rw = new BufferedWriter(new FileWriter(new File(output), true));
			for(String item : theta){
				rw.write(item + "\n");
			}
			rw.flush();
			rw.close();
			theta.clear();
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
	
	/**
	 * @author Kambiz Ghoorchian
	 * @param key1
	 * @param key2
	 * @return
	 */
	private int getEdge(int key1, int key2) {
		if(key1 < key2)
			return key1 * 10000 + key2;
		else
			return key2 * 10000 + key1;
	}
	
	/**
	 * 
	 * @param edge
	 * @return
	 */
	private int[] getSrcDst(int edge){
		return new int[]{(edge - (edge % 10000)) / 10000, edge % 10000};
	}
	
	/**
	 * 
	 * @param ri
	 * @param c
	 * @param delta
	 * @param function_word_threshold
	 * @return
	 */
	private double weight_func(RI ri, String c, double delta, double function_word_threshold) {
		double wgt = java.lang.Math.exp(-delta * (ri.vocabs.get(c).count * 1.0 / ri.tokens));
		if(wgt < function_word_threshold) wgt = 0;
		//if(wgt == 0) System.out.println(c);
		return wgt;
	}
	
	/**
	 * 
	 * @param unsorted_list
	 */
	private void sort(List<Pair<Integer, Double>> unsorted_list) {
		Collections.sort(unsorted_list , new Comparator<Pair<Integer, Double>>() {public int compare(Pair<Integer, Double> o1 , Pair<Integer, Double> o2) { if(o1.value > o2.value) return -1; else
		 	if(o1.value < o2.value) return 1; else return 0; }});
	}

	/**
	 * 
	 * @param min_wei_trs
	 * @return
	 */
	public double minwt(double min_wei_trs) {
		double minWT = 0;
		if(min_wei_trs < 1.0){
			List<Double> weights = new ArrayList<Double>();
			double totalWeight = 0.0;
			for (int i = 0; i < ed[0].length - 2; i++) {
				for (int j = i+1; j < ed[1].length -1; j++) {
					weights.add(ed[i][j]);
					totalWeight += ed[i][j];
				}
			}
			Collections.sort(weights);
			Collections.reverse(weights);
			double sum = 0.0;
			for (double w : weights) {
				sum += w;
				if (sum >= totalWeight * min_wei_trs) {
					minWT = w;
					break;
				}
			}
		}
		return minWT;
	}
}