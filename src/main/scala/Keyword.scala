import ruc.irm.xextractor.keyword.TextRankExtractor.GraphType._
import ruc.irm.xextractor.keyword.{TextRankExtractor, Word2VecKMeansExtractor}

/**
  * 利用Scala进行关键词抽取测试, 命令行下运行：sbt console
  * 然后执行： ```Keyword test 1```, 表明是测试第一篇文档
  */
object Keyword {
  lazy val weightedExtractor: TextRankExtractor = new TextRankExtractor(WeightedRank)
  lazy val ningExtractor: TextRankExtractor = new TextRankExtractor(NingJianfei)
  lazy val clusteringExtractor: TextRankExtractor = new TextRankExtractor(ClusterRank)
  lazy val kmeansExtractor = new Word2VecKMeansExtractor()

  /**
    * 对比测试第id篇文档在不同抽取方法下的抽取结果
    *
    */
  val compare = (id: Int) => {
    val topN = 5
    val article = Articles.getArticle(id)
    println("原始关键词：" + article("tags"))
    println("-------------------")
    println("词语位置加权：\t" + weightedExtractor.extractAsList(article("title"), article("content"), topN))
    println("宁建飞方法：\t" + ningExtractor.extractAsList(article("title"), article("content"), topN))
    println("夏天新方法：\t" + clusteringExtractor.extractAsList(article("title"), article("content"), topN))
    println("Word2Vec聚类：\t" + kmeansExtractor.extractAsList(article("title"), article("content"), topN))
    println()
  }

  val evaluate = (topN: Int) => ruc.irm.xextractor.evaluation.ExtractKeywordEvaluation.evaluate(topN)
}