package grfmdl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import utils.Vocab;

public class RI {
	private int DIMENSION = 2000;
	private int NON_ZERO = 8;
	int tokens = 0; // total number of vocabs repeated.
	int types = 0; // total number of vocabs unique
	int WIN = 2;
	int PI = 1;
	double DELTA = 0.0;
	ArrayList<HashMap<Integer, Integer>> rivecs = new ArrayList<HashMap<Integer, Integer>>();
	ArrayList<HashMap<Integer, Double>> distvec = new ArrayList<HashMap<Integer, Double>>();
	//vocab = {Key = word, Value = [w_index, w_count]}
	HashMap<String, Vocab> vocabs = new HashMap<String, Vocab>();
	HashSet<String> bigrams = new HashSet<String>();
	
	public RI(int dim, int nonz, int win, int pi, double delta){
		this.DIMENSION = dim;
		this.NON_ZERO = nonz;
		this.WIN = win;
		this.PI = pi;
		this.DELTA = delta;
	}

	public void update_vecs(String line) {
		String[] words = line.split(" ");
		tokens += words.length;
		int ind = 0;
		int stop = words.length;
		for(String w: words){
			check_word(w);
			int wind = vocabs.get(w).index;
			HashMap<Integer, Double> wvec = distvec.get(wind);
			HashMap<Integer, Integer> wrivec = rivecs.get(wind);
			vocabs.get(w).count++;
			int lind = 1;
			while(lind < WIN + 1 && ind + lind < stop){
				String c = words[ind + lind];
				if(!bigrams.contains(w + "|" + c)){
					bigrams.add(w + "|" + c);
					check_word(c);
				
					int cind = vocabs.get(c).index;
					HashMap<Integer, Double> cvec = distvec.get(cind);
					HashMap<Integer, Integer> crivec = rivecs.get(cind);
				
					double cweight = weight_func(c, DELTA);
					for(int i : crivec.keySet()){
						Double value = crivec.get(i) * cweight;
						if(wvec.containsKey(i + PI))
							value += wvec.get(i + PI);
						wvec.put(i + PI, value);
					}
				
					double wweight = weight_func(w, DELTA);
					for(int i : wrivec.keySet()){
						Double value = wrivec.get(i) * wweight;
						if(cvec.containsKey(i - PI))
							value += cvec.get(i - PI);
						cvec.put(i - PI, value);
					}

				}
				lind++;
			}			
			ind++;
		}
	}

	private double weight_func(String c, double delta) {
		return 1.0;
		//return java.lang.Math.exp(-delta * (vocabs.get(c).count * 1.0/tokens));
	}

	private void check_word(String w) {
		if(!vocabs.containsKey(w)){
			types++;
			rivecs.add(make_index());
			distvec.add(new HashMap<Integer, Double>());
			vocabs.put(w, new Vocab(types, rivecs.size() - 1, 0));
		}
	}

	private HashMap<Integer, Integer> make_index() {
		HashMap<Integer, Integer> ret = new HashMap<Integer, Integer>();
		Random aux = new Random(System.currentTimeMillis());
		Random rind = new Random(System.currentTimeMillis() * aux.nextInt());
		//Random rsign = new Random(System.currentTimeMillis());
		for(int i = 0; i< NON_ZERO; i++){
			int ind = rind.nextInt(DIMENSION - 2);
			//int sign = rsign.nextInt(2) * 2 - 1;
			int sign = 1;
			ret.put(ind+1, sign);
		}
		return ret;
	}
	
}

