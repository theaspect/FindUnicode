import groovyjarjarcommonscli.*
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

import java.nio.file.Files
import java.nio.file.Paths
import java.util.regex.Matcher
import java.util.regex.Pattern

class Find {

    private static final int MAX_MATCH_IN_LINE = 5
    private static final int MAX_MATCH_IN_FILE = 100

    Logger logger = LogManager.getLogger(Find.class);

    ArrayList<File> files

    public boolean isDirectory(String path) {
        File f = new File(path)
        if (!f.exists()) {
            return false
        } else {
            return true
        }
    }

    Find() {
        this.files = new ArrayList()
    }

    private static Option makeOptionWithArgument(String shortName, String description, boolean isRequired) {
        Option result = new Option(shortName, true, description)
        result.setArgs(1)
        result.setRequired(isRequired)

        return result
    }

    static void printHelp(Options options) {
        final PrintWriter writer = new PrintWriter(System.out)
        final HelpFormatter helpFormatter = new HelpFormatter()

        helpFormatter.printHelp(
                writer,
                80,
                "[program]",
                "Options:",
                options,
                3,
                5,
                "-- HELP --",
                true)

        writer.flush()
    }

    ArrayList analysisFile(FileInputStream paths) {
        int countInLine
        int countInFile = 0
        int lineNumber = 0
        ArrayList indexNoAscii
        def result = new ArrayList<Result>()

        paths.eachLine {
            if(it.startsWith("\uFEFF")){
                it = it.substring(1)
            }
            if (countInFile > MAX_MATCH_IN_FILE) {
                return
            }
            lineNumber++
            Pattern pattern = Pattern.compile("[^\\p{ASCII}]")
            Matcher matcher = pattern.matcher(it)
            countInLine = 0
            indexNoAscii = new ArrayList()
            while (matcher.find()) {
                indexNoAscii.add(matcher.start())
                countInLine++
                countInFile++
            }

            if (lineNumber == 1){
                countInLine -= 1
            }

            if (countInLine > MAX_MATCH_IN_LINE) {
                result.add(new Result(lineNumber, it.substring(indexNoAscii.first(), indexNoAscii[MAX_MATCH_IN_LINE]), indexNoAscii.take(MAX_MATCH_IN_LINE), " more than 5 not ASCII of characters: "))
            }

            if (countInLine != 0 && countInLine <= MAX_MATCH_IN_LINE) {
                result.add(new Result(lineNumber, it.substring(indexNoAscii.first(), indexNoAscii.last() + 1), indexNoAscii, " - " + countInLine + " not ASCII of characters: "))

            }
        }
        paths.close()
        return result
    }

    public static int work(String path, String expansion) {

        Find unicode = new Find()

        if (unicode.isDirectory(path)) {
            Files.walk(Paths.get(path)).each {
                if (!Pattern.matches(/^(.)?[^.]*$/, it.fileName.toString() as CharSequence)){
                    if (expansion != null) {
                        if (Pattern.matches(/^(.*(${expansion}))[^.]*$/, it.fileName.toString() as CharSequence)) {
                            unicode.logger.info("Analysis of the file " + it.fileName)
                            unicode.analysisFile(new FileInputStream(it.toString())).each {
                                aResult -> println aResult
                            }
                        }
                    } else {
                        unicode.logger.info("Analysis of the file " + it.fileName)
                        unicode.analysisFile(new FileInputStream(it.toString())).each {
                            aResult -> println aResult
                        }
                    }
                }
            }
        } else {
            return 1
        }

        return 0
    }

    public static void main(String[] args) {

        Options options = new Options()
                .addOption(makeOptionWithArgument("expansion", "Expansion", false))
                .addOption(makeOptionWithArgument("path", "Path", true))

        CommandLine commandLine = null;
        try {
            commandLine = new GnuParser().parse(options, args)
        } catch (ParseException e) {
            printHelp(options);
            System.exit(255)
        }

        if(commandLine.getOptionValue("expansion")){
            System.exit(work(commandLine.getOptionValue("path"), commandLine.getOptionValue("expansion")))
        } else {
            System.exit(work(commandLine.getOptionValue("path"), null))
        }

    }

}
