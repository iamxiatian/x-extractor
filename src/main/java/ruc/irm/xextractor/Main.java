package ruc.irm.xextractor;

import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.io.Files;
import org.apache.commons.cli.*;
import ruc.irm.xextractor.keyword.KeywordExtractor;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * @author Tian Xia
 * @date Jun 15, 2016 11:12
 */
public class Main {
    public static void main(String[] args) throws IOException, ParseException {
        String helpMsg = "usage: ./run.py " + Main.class.getSimpleName();

        HelpFormatter helpFormatter = new HelpFormatter();
        CommandLineParser parser = new PosixParser();
        Options options = new Options();
        options.addOption(new Option("f", true, "the article file to extract keywords."));

        KeywordExtractor extractor = new KeywordExtractor();

        CommandLine commandLine = parser.parse(options, args);
        if (!commandLine.hasOption("f")) {
            helpFormatter.printHelp("参数错误！", options);
            return;
        }
        File f = new File(commandLine.getOptionValue("f"));
        if(!f.exists()) {
            System.out.println("文件不存在！" + f.getAbsolutePath());
            return;
        }
        List<String> lines = Files.readLines(f, Charsets.UTF_8);
        String title = lines.get(0);
        StringBuilder text = new StringBuilder();
        for(int i=1; i<lines.size(); i++) {
            text.append(lines.get(i)).append("\n");
        }

        String keywords = extractor.extractAsString(title, text.toString(), 5);
        System.out.println("抽取的关键词为：" + keywords);
    }
}
