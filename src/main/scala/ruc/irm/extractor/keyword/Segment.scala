package ruc.irm.extractor.keyword

import com.hankcs.hanlp.HanLP
import scala.jdk.CollectionConverters._

object Segment {
  def segment(text: String): List[(String, String)] =
    HanLP.segment(text).asScala.toList.map {
      term =>
        (term.word, term.nature.toString)
    }

}
