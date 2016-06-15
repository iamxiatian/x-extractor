# x-extractor
Automatic Chinese keyword extractor（page extractor and topic link extractor）

x-extractor是本人在研究过程中所实现的基于TextRank的关键词抽取工具包，尚未包含正文自动抽取和中心网页中主题链接的自动抽取代码。

由于多次收到关键词抽取研究人员希望获取基于TextRank的关键词抽取代码，以便能够进行对比分析，故在此公开此部分代码，希望能与大家一起，共同推进中文关键词抽取的研究。


# Run

1. 确保计算机上已经安装Java1.8环境和gradle
2. 编译代码：
```
	gradle compileJava
	gradle copyJars
```
3. 测试：
	```
	./run.py Main -f test/article01.txt
	```

## Reference

如在研究工作中使用了本部分代码并发表论文，请注明引用：

夏天. 词语位置加权TextRank的关键词抽取研究. 现代图书情报技术, 2013, 29(9): 30-34.
顾益军, 夏天. 融合LDA与TextRank的关键词抽取研究. 现代图书情报技术, 2014, 29(9): 30-34.
