/**
  * 关键词抽取的测试文章集合
  */
object Articles {
  import scala.xml.XML

  val doc = XML.loadFile("data/articles.xml")

  /** 文章总数量 */
  val articleCount = (doc \\ "article").length

  /** 所有tags标签下包含的标签数量: 3.565 */
  val averageTagCount = (doc \\ "tags").map(_.text.split(",").length).foldLeft(0)(_ + _) / articleCount.toDouble

  /**
    * 文章的平均字符数量： 2629.101
    */
  val averageContentLength = (doc \\ "content").map(_.text.length).foldLeft(0)(_ + _) /articleCount.toDouble

  /**
    * 文章的平均词语数量: 1631.814
    */
  val averageContentWords = (doc \\ "content").map(e=>Segment.segment(e.text).length).foldLeft(0)(_ + _) /articleCount.toDouble

  private val acceptPOS = (pos: String) => pos.take(1) == "n" || pos.take(3) == "adj" || pos.take(1) == "v"

  /**
    * 文章的平均词语数量： 过滤后只保留名词，动词和形容词: 757.284
    */
  val filteredAverageContentWords = (doc \\ "content").map(a=>Segment.segment(a.text).filter(b=>acceptPOS(b._2)).length).foldLeft(0)(_ + _) /articleCount.toDouble

}