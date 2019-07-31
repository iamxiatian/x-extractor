package ruc.irm.extractor.keyword

import ruc.irm.extractor.evaluation.EvalResult
import ruc.irm.extractor.keyword.{TextRankExtractor, Word2VecKMeansExtractor}
import ruc.irm.extractor.keyword.TextRankExtractor.GraphType._

/**
  * 利用Scala进行关键词抽取测试, 命令行下运行：sbt console
  * 然后执行：
  * ```Keyword test 1```, 表明是测试第一篇文档,
  *
  * 執行以下代碼，测试保留的关键词数量从１到１０时，准确率、召回率和Ｆ值的变化：
  * ```
  * val results = Keyword.evaluateAll(1, 10)
  *   Keyword.printEvaluationResult(results, 1)
  * ```
  *
  *
  */
object Keyword {
  val weightedExtractor: TextRankExtractor = new TextRankExtractor(PositionRank)
  val ningExtractor: TextRankExtractor = new TextRankExtractor(NingJianfei)
  val clusterExtractor: TextRankExtractor = new TextRankExtractor(ClusterRank)
  val kmeansExtractor = new Word2VecKMeansExtractor()

  val weightedDivExtractor: TextRankExtractor = new TextRankExtractor(PositionDivRank)
  val clusterDivExtractor: TextRankExtractor = new TextRankExtractor(ClusterDivRank)

  /**
    * 对比测试第id篇文档在不同抽取方法下的抽取结果
    *
    */
  val compare = (id: Int) => {
    val topN = 5
    val article = Articles.getArticle(id)
    val title = article("title")
    val content = article("content")
    println("原始关键词：" + article("tags"))
    println("-------------------")
    println("词语位置加权：\t" + weightedExtractor.extractAsList(title, content, topN))
    println("宁建飞方法：\t" + ningExtractor.extractAsList(title, content, topN))
    println("夏天新方法：\t" + clusterExtractor.extractAsList(title, content, topN))
    println("Word2Vec聚类：\t" + kmeansExtractor.extractAsList(title, content, topN))
    println("词语位置加权+DivRank：\t" + weightedDivExtractor.extractAsList(title, content, topN))
    println("夏天新方法+DivRank：\t" + clusterDivExtractor.extractAsList(title, content, topN))
    println()
  }

  val evaluate = (topN: Int) => ruc.irm.extractor.evaluation.ExtractKeywordEvaluation.evaluate(topN)

  /**
    * 选择不同数量的topN关键词，评估抽取结果， Array里面保存的是不同方法的抽取结果
    */
  def evaluateAll(fromTopN: Int = 3, toTopN: Int = 10): Seq[Array[EvalResult]] = (fromTopN to toTopN) map evaluate

  def printEvaluationResult(results: IndexedSeq[Array[EvalResult]], fromTopN: Int = 3) = {
    results.zipWithIndex.foreach {
      case (evalResultArray, idx) => {
        println(s"Keywords count:${idx + fromTopN}")
        println("-------------------")
        evalResultArray foreach println
        println()
      }
    }
    println("-------END---------\n")
    results
  }

  /**
    * 输出LaTex格式的表格，方便论文写作，格式为：
    * M1 & 0.304 & 0.259 & 0.277   & 0.000 & 0.000 & 0.000 \\
    */
  def outputLatexTable(x: Array[EvalResult]): String = {
    val lines = x.zipWithIndex.map {
      case (a, idx) =>
        f"$$M${idx + 1}$$ & ${a.precision}%.3f & ${a.recall}%.3f & ${a.f}%.3f \\\\"
    }.mkString("\n")

    s"""
       |\\begin{tabular}{| l | c| c | c |}
       |\\hline
       |Method & P & R & F \\
       |$lines
       |\\end{tabular}
    """.stripMargin
  }

  /**
    * 输出tikz绘制曲线图需要的数据格式
    *
    * @return
    */
  def outputPlotData(results: Seq[Array[EvalResult]], fromTopN: Int): String = {
    val precision = results.zipWithIndex
      .map {
        case (algorithms, idx) => f"${idx + fromTopN} " + algorithms.map(a => f"${a.precision}%.3f").mkString(" ")
      }.mkString("\n")

    val recall = results.zipWithIndex
      .map {
        case (algorithms, idx) => f"${idx + fromTopN} " + algorithms.map(a => f"${a.recall}%.3f").mkString(" ")
      }.mkString("\n")

    val fvalue = results.zipWithIndex
      .map {
        case (algorithms, idx) => f"${idx + fromTopN} " + algorithms.map(a => f"${a.f}%.3f").mkString(" ")
      }.mkString("\n")

    val header = "TopN M1 M2 M3 M4 M5"
    Seq("Precision:", header, precision, "\nRecall:", header, recall, "\nF-value:", header, fvalue).mkString("\n")
  }

  def main(args: Array[String]): Unit = {
    //    val topN = 5
    //    val article = Articles.getArticle(202)
    //    println("原始关键词：" + article("tags"))
    //    println("-------------------")
    //    println("词语位置加权：\t" + weightedExtractor.extractAsList(article("title"), article("content"), topN))
    //    println("宁建飞方法：\t" + ningExtractor.extractAsList(article("title"), article("content"), topN))
    //    println("夏天新方法：\t" + clusteringExtractor.extractAsList(article("title"), article("content"), topN))
    //    println("Word2Vec聚类：\t" + kmeansExtractor.extractAsList(article("title"), article("content"), topN))
    //    println()
    //    compare(202)
    val results = evaluateAll(1, 10)
    println(results)

    println("===================")
    results.zipWithIndex.foreach {
      case (r, idx) =>
        println(s"TopN = ${idx + 1}\n")
        println(outputLatexTable(r))
    }

    println("==================")
    println(outputPlotData(results, 3))
    println("I'm DONE!")
  }
}
