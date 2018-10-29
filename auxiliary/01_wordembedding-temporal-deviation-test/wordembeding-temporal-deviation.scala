// Check the median deviation between multiple wordVectors created by w2v through one month of Twitter data.
// - Extract one month (May 2016) data from Twitter.
// - Group tweets daily and construct daily accumulative corpuses (e.g., C1 = {D1}, C2 = {D1,D2}, C3 = {D1, D2, D3}, ...)
// - Extract feature vecotrs for each corpus using Spark implementation of Wrod2Vec in MlLitb.
// - Calculate median deviation between feature vectors from every two consequent corpus (using cosine similarity).

import org.apache.spark.mllib.feature.{Word2Vec, Word2VecModel}
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import scala.collection.mutable.HashMap
import org.apache.spark.rdd.RDD
//##################################################################
def cosineSimilarity(v1: Array[Float], v2: Array[Float]): Double ={
var dotProduct = 0.0
var normA = 0.0
var normB = 0.0
for(i <- 0 to v1.size - 1){
dotProduct += v1(i) * v2(i)
normA += Math.pow(v1(i), 2);
normB += Math.pow(v2(i), 2);
}
val cosine = dotProduct / (Math.sqrt(normA) * Math.sqrt(normB))
return cosine
}

//##################################################################
def median(devs: List[Double]): Double = {
val (lower, upper) = devs.sortWith((a,b) => a < b).splitAt(devs.size / 2)
if (devs.size % 2 == 0) return (lower.last + upper.head) / 2.0 else return upper.head
}

//##################################################################
def Average_or_Median_Deviation(m1: Word2VecModel, m2: Word2VecModel): Double ={
val v1 = m1.getVectors
val v2 = m2.getVectors
var devs = List[Double]()
for((k,v) <- v1){
if(v2.keySet.contains(k)){
devs = devs :+ (1 - Math.abs(cosineSimilarity(v, v2.get(k).get)))
}
}
//Average
//return devs.sum/devs.size
//Median
return median(devs)
}

def expand(index: Int, max: Int): Array[Int] ={
val arr = Array.fill(max)(0)
for(i <- 0 to max - 1) arr(i) = index + i
return arr
}

//##################################################################
def getModel(docs: org.apache.spark.rdd.RDD[Seq[String]]): Word2VecModel ={
val vv = new Word2Vec()
val model = vv.fit(doc)
return model
}

//##################################################################
def getTimeAndDayOfYear(stringDate: String): (Long, Int) ={
val dateparser = new java.text.SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy")
val date = dateparser.parse(stringDate)
val cal = Calendar.getInstance()
cal.setTime(date)
return (date.getTime, cal.get(Calendar.DAY_OF_YEAR))
}
val dateparser = new java.text.SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy")
val min_time = dateparser.parse("Sun May 01 00:00:00 CEST 2013").getTime
val max_time = dateparser.parse("Wed Jun 01 00:00:00 CEST 2013").getTime
val input = sc.textFile("sample.txt").map(_.split(",")).map(x => (getTimeAndDayOfYear(x(2)), x(6))).filter(x => x._1._1 > min_time && x._1._1 < max_time).map(x => (x._1._2, x._2.split(" ").toSeq))
val expanded_input = input.groupByKey().sortBy(_._1).map(x => (x._2, expand(x._1, 31 - (x._1 - 121)))).flatMapValues(x => x).map(x => (x._2, x._1))
val commulative_input = expanded_input.reduceByKey((a,b) => a ++ b).sortBy(_._1)
val corps = commulative_input.collect
var models = HashMap[Int, Word2VecModel]()
corps.foreach(x => models.put(x._1,getModel(sc.parallelize(x._2.toSeq))))

val m1 = models.get(121).get
val m2 = models.get(122).get
val m3 = models.get(123).get
val m4 = models.get(124).get
val m5 = models.get(125).get
val m6 = models.get(126).get
val m7 = models.get(127).get
val m8 = models.get(128).get
val m9 = models.get(129).get
val m10 = models.get(130).get
val m11 = models.get(131).get
val m12 = models.get(132).get
val m13 = models.get(133).get
val m14 = models.get(134).get
val m15 = models.get(135).get
val m16 = models.get(136).get
val m17 = models.get(137).get
val m18 = models.get(138).get
val m19 = models.get(139).get
val m20 = models.get(140).get
val m21 = models.get(141).get
val m22 = models.get(142).get
val m23 = models.get(143).get
val m24 = models.get(144).get
val m25 = models.get(145).get
val m26 = models.get(146).get
val m27 = models.get(147).get
val m28 = models.get(148).get
val m29 = models.get(149).get
val m30 = models.get(150).get
val m31 = models.get(151).get

Average_or_Median_Deviation(m1, m2)
Average_or_Median_Deviation(m2, m3)
Average_or_Median_Deviation(m3, m4)
Average_or_Median_Deviation(m4, m5)
Average_or_Median_Deviation(m5, m6)
Average_or_Median_Deviation(m6, m7)
Average_or_Median_Deviation(m7, m8)
Average_or_Median_Deviation(m8, m9)
Average_or_Median_Deviation(m9, m10)
Average_or_Median_Deviation(m10, m11)
Average_or_Median_Deviation(m11, m12)
Average_or_Median_Deviation(m12, m13)
Average_or_Median_Deviation(m13, m14)
Average_or_Median_Deviation(m14, m15)
Average_or_Median_Deviation(m15, m16)
Average_or_Median_Deviation(m16, m17)
Average_or_Median_Deviation(m17, m18)
Average_or_Median_Deviation(m18, m19)
Average_or_Median_Deviation(m19, m20)
Average_or_Median_Deviation(m20, m21)
Average_or_Median_Deviation(m21, m22)
Average_or_Median_Deviation(m22, m23)
Average_or_Median_Deviation(m23, m24)
Average_or_Median_Deviation(m24, m25)
Average_or_Median_Deviation(m25, m26)
Average_or_Median_Deviation(m26, m27)
Average_or_Median_Deviation(m27, m28)
Average_or_Median_Deviation(m28, m29)
Average_or_Median_Deviation(m29, m30)
Average_or_Median_Deviation(m30, m31)



