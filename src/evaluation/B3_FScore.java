package evaluation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.List;

public class B3_FScore {
	
	Hashtable<String, String> result = new Hashtable<String, String>();
	Hashtable<String, String> truth = new Hashtable<String, String>();
	
	Hashtable<String, ArrayList<String>> sortedTruth = new Hashtable<String, ArrayList<String>>();
	Hashtable<String, ArrayList<String>> sortedResult = new Hashtable<String, ArrayList<String>>();
	
	Hashtable<String, Integer> combinedCounts = new Hashtable<String, Integer>();
	int totalNumDocuments = 0;

	public B3_FScore(String groundTruth_File, String result_File, int min_top_cnt){	
		
		readResults(result_File);
		readGroundTruth(groundTruth_File);

		List<EvalObject> evals = new ArrayList<EvalObject>(); 
		
		float generalTotalPre = 0;
		float generalTotalRec = 0;
		
		for(String classLable : sortedResult.keySet()){
			ArrayList<String> keys = sortedResult.get(classLable);
			if(keys.size() > min_top_cnt){
				float totalPre = 0;
				float totalRec = 0;
				for(String key : keys){
					totalPre += precision(key, classLable);
					totalRec += recall(key, classLable);
				}
				
				float avPre = totalPre/keys.size();
				float avRec = totalRec/keys.size();
				float avFScore = 2 * avPre * avRec / (avPre+avRec);
				evals.add(new EvalObject(classLable, keys.size(), avPre, avRec, avFScore));
	
				generalTotalPre += totalPre;
				generalTotalRec += totalRec;
			}
		}	
		
		Collections.sort(evals, new Comparator<EvalObject>(){
			   public int compare(EvalObject n1, EvalObject n2){
				   if(n1.count < n2.count) return 1;
				   else if(n1.count > n2.count) return -1;
				   else return 0;
			   }
			});
		for(EvalObject eo : evals){
			String repo = eo.partition + "\t" + eo.count + "\t" + String.format("%.3f", eo.P) + "\t" + String.format("%.3f", eo.R) + "\t" + String.format("%.3f", eo.F);
			System.out.println(repo);
		}
		
		float genAvPre =  generalTotalPre/result.size();
		float genAvRec = generalTotalRec/result.size();
		float getAvF = 2 * genAvPre * genAvRec / (genAvPre+genAvRec);
		String repo = "tot:\t " + result.size() + "\t" + String.format("%.3f", genAvPre) + "\t" + String.format("%.3f", genAvRec) + "\t" + String.format("%.3f", getAvF);
		System.out.println(repo);
	}

	public float precision(String number, String classLable){
		String myTruth = truth.get(number);
		int selectedDocuments = sortedResult.get(classLable).size();
		
		int truePositive = 0;
		if(combinedCounts.containsKey(classLable+"-"+myTruth))
			truePositive = combinedCounts.get(classLable + "-" + myTruth);
		else{
			List<String> res = sortedResult.get(classLable);
			List<String> tur = sortedTruth.get(myTruth);
			for(String id : res)
				if(tur.contains(id))
					truePositive++;
			combinedCounts.put(classLable+"-"+myTruth, truePositive);
		}
		return 1.0f * truePositive/selectedDocuments;
	}
	
	public float recall(String number, String classLable){
		String myTruth = truth.get(number);
		
		int truePositive = 0;
		if(combinedCounts.containsKey(classLable+"-"+myTruth))
			truePositive = combinedCounts.get(classLable + "-" + myTruth);
		else{
			List<String> res = sortedResult.get(classLable);
			List<String> tur = sortedTruth.get(myTruth);
			for(String id : res)
				if(tur.contains(id))
					truePositive++;
			combinedCounts.put(classLable+"-"+myTruth, truePositive);
		}
		int countPositive = sortedTruth.get(myTruth).size();
		return 1.0f * truePositive/countPositive;
	}

	public void readResults(String resultFileName){
		try{
			BufferedReader br = new BufferedReader(new FileReader(new File(resultFileName)));
			String line = "";
			int doc_number = 1;
			while((line = br.readLine())!= null){
				String par_number = line.split("\t")[0];
				if(!par_number.equals("-1")){
					result.put(doc_number + "", par_number);

					if(sortedResult.containsKey(par_number))
						sortedResult.get(par_number).add(doc_number + "");
					else{
						ArrayList<String> docs = new ArrayList<String>();
						docs.add(doc_number + "");
						sortedResult.put(par_number, docs);
					}
				}
				doc_number++;
			}
			br.close();
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}

	public void readGroundTruth(String groundTruthFile){
		try{
			BufferedReader br = new BufferedReader(new FileReader(new File(groundTruthFile)));
			String line = "";
			int docNumber = 1;
			while((line = br.readLine())!= null){
				if(result.containsKey(docNumber + "")){
					String classLabel = line;
					truth.put(docNumber + "", classLabel);
	
					if(sortedTruth.containsKey(classLabel))
						sortedTruth.get(classLabel).add(docNumber + "");
					else{
						ArrayList<String> docNum = new ArrayList<String>();	
						docNum.add(docNumber + "");
						sortedTruth.put(classLabel, docNum);
					}
				}
				docNumber++;
			}
			br.close();
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
	
	public static void main(String[] args){
		
	}
}
class EvalObject{
	String partition;
	int count;
	double P;
	double R;
	double F;
	public EvalObject(String in_partition, int in_count, float in_P, float in_R, float in_F){
		this.partition = in_partition;
		this.count = in_count;
		this.P = in_P;
		this.R = in_R;
		this.F = in_F;
	}
}
