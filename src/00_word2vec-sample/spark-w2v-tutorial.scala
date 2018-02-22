Spark Word2Vec Docs:
https://spark.apache.org/docs/latest/mllib-feature-extraction.html#word2vec

import org.apache.spark.mllib.feature.{Word2Vec, Word2VecModel}

val stop = sc.textFile("longstoplist.txt").map(x => (x, 0))
val input = sc.textFile("sample.txt").map(line => line.toLowerCase.replaceAll("[!\"$%&'*+,./:;<=>?\\[\\]^`{\\|}~()]", " ").replaceAll("http", "").replaceAll("\\s+", " ").split(" ").toSeq)
val temp = input.zipWithIndex.map(x => (x._2, x._1)).flatMapValues(x => x).map(x => (x._2, x._1)).subtractByKey(stop).filter(x => !x._1.isEmpty)
val docs = temp.map(x => (x._2, x._1)).groupByKey().map(_._2)

val word2vec = new Word2Vec()
val model = word2vec.fit(docs)
val synonyms = model.findSynonyms("train", 5)

docs.flatMapValues(x => x).map(x => (x, 1)).reduceByKey(_+_).sortBy(_._2, false).take(10)

model.save(sc, "myModelPath")

val df = spark.read.parquet("myModelPath/data/part-00000-a60f313b-8344-4534-92fa-5e2d047c1557-c000.snappy.parquet").rdd
val vecs = df.map(x => (x.getString(0), x.getList[Double](1)))
//vecs.flatMapValues(x => x).reduceByKey((a,b) => a + "," + b).map(x => x._1 + "," + x._2).first