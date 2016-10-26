# x-extractor
Automatic Chinese keyword extractor（page extractor and topic link extractor）

x-extractor是本人在研究过程中所实现的关键词抽取、网页正文抽取、主题链接抽取等信息抽取相关的工具包，此项目仅公开了关键词抽取部分的代码，尚未包含正文自动抽取和中心网页中主题链接的自动抽取代码。

由于多次收到关键词抽取研究人员希望获取基于TextRank的关键词抽取代码，以便能够进行对比分析，故在此公开此部分代码，希望能与大家一起，共同推进中文关键词抽取的研究。


# Run

1. 确保计算机上已经安装Java1.8环境和最新的Scala及SBT

2. 编译代码：
	```
	sbt package
	```

3. 测试：

    首先进入工程目录，执行：
    ```    
	sbt console
    ```

    然后，对Id为１的单篇文档进行测试，查看抽取结果：
    
    ```
    > Keyword test 1
	```
	
	对整个测试数据集进行测试，查看保留的关键词数量从１到１０时，准确率、召回率和Ｆ值的变化：
	
	```
    > val result = Keyword evaluate 5

    > result foreach println
	```

## Data

1. 训练词向量模型的维基百科文本数据集： [[https://pan.baidu.com/s/1kV6nB7L]]

由2015年6月发布的维基百科中文导出数据“zhwiki-20150602-pages-articles-multistream.xml.bz”加工生成 ,该数据集共包含516,695篇文章，已经进行分词处理；

2. 由上述维基百科文本数据集生成的word2vec模型文件：[[https://pan.baidu.com/s/1gfJPU3D]]

该模型采用Gensim的word2vec以默认参数生成。

3. 南方周末抓取生成的带关键词文章数据集：[[https://pan.baidu.com/s/1pKOMe6n]]

关键词通过原始文章中的tag得到。


## Reference

如在研究工作中使用了本部分代码并发表论文，请注明引用：

1. 夏天. 词向量聚类加权TextRank的关键词抽取研究
2. 夏天. 词语位置加权TextRank的关键词抽取研究. 现代图书情报技术, 2013, 29(9): 30-34.
3. 顾益军, 夏天. 融合LDA与TextRank的关键词抽取研究. 现代图书情报技术, 2014, 29(9): 30-34.


## Thanks

本工程的部分源代码摘自开源项目,为方便编译和调整,更改了原始代码的包名称, 对用到的所有开源代码致以敬意,如需要从本代码库中移除,请留言.

用到的源代码包括:

1. T-SNE-Java: [[https://github.com/lejon/T-SNE-Java]] , T-SNE用于词图的可视化

