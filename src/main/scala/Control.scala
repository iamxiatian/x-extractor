/**
  * 控制流，目前实现了借贷模式(Loan Pattern)
  *
  * @author Tian Xia
  * @date Oct 22, 2016 10:40
  */
object Control {
  /**
    * Loan Pattern, 使用示例：
    * using(io.Source.fromFile("example.txt")) { source => source.getLines.foreach(rintln)  }
    * @return
    */
  def using[A <: {def close():Unit}, B](resource: A)(f: A=>B): B =
    try {
      f(resource)
    } finally {
      resource.close()
    }

}

