import org.zhinang.conf.Configuration
import ruc.irm.xextractor.nlp.SegmentFactory

import scala.collection.mutable.ListBuffer


object Segment {

  import scala.collection.JavaConverters._

  private val conf = new Configuration()
  private val ansjSeg: ruc.irm.xextractor.nlp.Segment = SegmentFactory.getSegment(conf)
  private val hanlpSeg: ruc.irm.xextractor.nlp.Segment = SegmentFactory.getHanSegment(conf)

  def segmentByAnsj(text: String): List[(String, String)] =
    ansjSeg.tag(text).asScala.toList.map(x => (x.word, x.pos))

  def segmentByHanLP(text: String): List[(String, String)] =
    hanlpSeg.tag(text).asScala.toList.map(x => (x.word, x.pos))

  val segment = segmentByAnsj _
}