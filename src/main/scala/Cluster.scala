import smile.clustering._
import smile.math.distance.EuclideanDistance

object Cluster {
  /** 可以接受的词性，目前只有名词，形容词和动词会保留到聚类处理过程之中 */
  val acceptPOS = (pos: String) => pos.take(1) == "n" || pos.take(3) == "adj" || pos.take(1) == "v"

  /** 对文本分词并只保留可接受词性的词语，保存到List[String]之中 */
  val acceptWords: String => List[String] = (text: String) => Segment.segment(text).filter(x => acceptPOS(x._2)).map(_._1)

  val word2vec: Word2Vec = Word2Vec("/home/xiatian/data/wiki/word2vec.bin")

  /** 对文本分词后保留的词性，取出其对应的word2vec词向量 */
  val wordVectors = (text: String) => acceptWords(text).filter(word2vec.contains).map(word2vec.vectorInDouble).toArray

  /** 测试用文本 */
  val text = "9月30日晚间，北京加码楼市限贷，分别将购买首套和二套普通住宅的首付款比例提至35%、50%；天津几乎同步出台“分区限购”政策，对已有1套住房的非本市户籍家庭，暂停在市内六区和武清区再购房，同时提高其首套房商贷首付比例至40%；同日，苏州也宣布进一步加强商品住房价格监管。\n10月1日晚间，郑州、成都正式接棒。郑州宣布对市内部分区域实施住房限购，对在指定区域内拥有2套及以上住房的本市户籍居民家庭和拥有1套及以上住房的非本市户籍居民家庭限购180平方米以下的住房；成都限购略有不同，规定在高新区、锦江区、武侯区等区域内，一人只能新购买1套商品住房。\n10月2日，限购阵营再添济南、无锡、合肥三市。济南分别将购买首套和二套住房的商贷最低首付款提至30%、40%，且明确本市户籍家庭已有三套住房的，暂不得再购房；无锡则宣布将二套房商贷最低首付比例提至40%，非本市户籍家庭限购第二套房，同时对土地出让设置最高限价；合肥也于昨晚紧急发布区域性住房限购政策，在市区范围内，暂停向拥有2套房以上的本市户籍家庭出售新建商品房，并将首套房商贷最低首付比例调整为30%。\n\n点击图片进入下一页\n资料图。汤彦俊 摄\n“分区限购”成亮点 或现挤出效应\n除北京、苏州属于“加码”调控外，其余城市均祭出了最新的限购限贷举措，差异化的“分区限购”调控成为政策新亮点。\n这将产生一种挤出效应，缓解城市热点区域房价的过快上涨，同时将购房需求推向可能存在‘去库存’压力的周边区域。”易居研究院智库中心研究总监严跃进向中新网记者透露。\n以天津为例，严跃进具体分析了分区限购调控的作用原理，天津武清区由于毗邻北京，成为本轮房地产市场炒作的热点，前8月二手房交易价格迅速上涨。“而在限购政策实施后，北京购房者至天津购房的门槛抬高，在武清区认购首套住房首付需要40%，且无法购买二套，遂只能认购北辰和宝坻等区域，未来这些区域市场会呈现明显上涨，即市场需求转移。”\n限购阵营渐扩围 下一个会是谁？\n据记者统计，除北、上、广、深四个一线城市外，已有厦门、杭州、苏州、郑州、成都、南京、天津、济南、无锡、合肥共10个二、三线城市加入楼市限购阵营。\n与其他城市单纯收紧调控、“抑房价”的目的不同，北京此次加码限贷的举措，被认为调控信号意义大于政策本身调控的意义。在严跃进看来，“\n北京政策模板将成为第四季度一、二线城市调控楼市的模板。”\n在北京发布楼市新政后，确有六城紧跟步伐，而这股调控浪潮或许仍将继续。综合业内人士分析显示，未来，福州、东莞、珠海、石家庄、青岛等房价涨幅较快的城市，限购可能性仍非常大。\n\n点击图片进入下一页\n图为南京市民在一处楼盘售楼处内咨询。（资料照片） 中新社记者 泱波 摄\n房价会有实质性下跌？\n调控政策何时能真正“抑房价”？在严跃进看来，中国楼市政策的拐点已在今年第四季度正式开启，而市场拐点会在明年，2017年第二季度部分城市或有“量价齐跌”的现象出现。\n但需要注意的是，此类市场拐点是周期性的，而非房价有实质性下跌。”严跃进提醒。\n中原地产首席分析师张大伟也持有相同的观点，“此前全面宽松的趋势已发生改变”。他认为，在市场分化的基础下，调控政策同样趋于分化，房价涨幅过快的城市还将陆续出台限购限贷等举措。（完）"

  /** 对文本进行k均值聚类, 返回该每个聚类中心最近的词语 */
  val clustering = (text: String, k: Int) => {
    val words = acceptWords(text).filter(word2vec.contains) //可以接受的指定词性的词语，并且在word2vec中出现过
    val data = words.map(word2vec.vectorInDouble).toArray //转换为二维数组，每一行为一个词向量

    val clusters: KMeans = kmeans(data, k, runs = 20)
    val centroids = clusters.centroids()
    val clusterLabels: Array[Int] = clusters.getClusterLabel

    /**
      * 递归遍历clusterLabels列表，返回距离clusterIdx聚类中心最近的文档二元组(文档Id,距离)
      */
    def findNearDoc(labelIdx: Int, clusterIdx: Int, nearDoc: (Int, Double)): (Int, Double) =
      if ((labelIdx == clusterLabels.length) ) {
        nearDoc //到达列表末尾，返回
      } else if(clusterIdx != clusterLabels(labelIdx)){
        findNearDoc (labelIdx + 1, clusterIdx, nearDoc)
      } else {
        val d = new EuclideanDistance ().d (centroids(clusterIdx), data(labelIdx) )
        if (d < nearDoc._2)
          findNearDoc (labelIdx + 1, clusterIdx, (labelIdx, d) )
        else
          findNearDoc (labelIdx + 1, clusterIdx, nearDoc)
      }

    for (c <- (0 to k - 1).toArray) yield words(findNearDoc(0, c, (0, Double.MaxValue))._1)
    
//    for (c <- (0 to k - 1).toArray) yield
//    {
//      println(s"find near doc for cluster $c")
//      val doc = findNearDoc(0, c, (0, Double.MaxValue))
//      println("\t" + doc)
//      words(doc._1)
//    }
  }

}