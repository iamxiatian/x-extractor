
name := "extractor"
organization := "ruc.nlp"
version := "0.2"

scalaVersion := "2.13.0"
sbtVersion := "1.2.8"
scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8")
javacOptions := Seq("-encoding", "UTF-8")

libraryDependencies += "org.scala-lang.modules" %% "scala-xml" % "1.2.0"
libraryDependencies += "com.github.scopt" %% "scopt" % "4.0.0-RC2" //command line parser

libraryDependencies += "com.github.haifengl" % "smile-scala_2.11" % "1.2.0" //simile machine learning

//add jars for old java code
libraryDependencies += "org.slf4j" % "slf4j-api" % "1.7.1"

libraryDependencies += "org.apache.commons" % "commons-lang3" % "3.3.1"
libraryDependencies += "com.google.guava" % "guava" % "23.5-jre"

//NLP libraries
libraryDependencies += "com.hankcs" % "hanlp" % "portable-1.7.4"
//libraryDependencies += "org.ansj" % "ansj_seg" % "5.0.1"

libraryDependencies += "net.sf.trove4j" % "trove4j" % "3.0.3"

//HTML Process
libraryDependencies += "org.jsoup" % "jsoup" % "1.9.2"
libraryDependencies += "com.squareup.okhttp3" % "okhttp" % "3.4.1"

scalacOptions in Test ++= Seq("-Yrangepos")

enablePlugins(JavaAppPackaging)

mainClass in Compile := Some("Start")

mappings in(Compile, packageDoc) := Seq()

//把运行时需要的配置文件拷贝到打包后的主目录下
// mappings in Universal ++= directory("conf")

javaOptions in Universal ++= Seq(
  // -J params will be added as jvm parameters
  "-J-Xms2G",
  "-J-Xmx8G"
)

//解决windows的line too long问题
scriptClasspath := Seq("*")