import groovyjarjarcommonscli.*
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

import java.nio.file.Files
import java.nio.file.Paths
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

    void analysisFile(def filePath) {

        boolean b
        int countInLine
        int countInFile = 0
        int lineNumber = 0
        File f = new File(filePath.toString());

        if (!f.isDirectory()) {

            int firstNotAsciiChar
            int lastNotAsciiChar

            BufferedReader fin = new BufferedReader(new FileReader(f));
            String line;
            logger.info("Analysis of the file " + f.getName())
            while ((line = fin.readLine()) != null) {
                lineNumber++
                countInLine = 0
                for (int i = 1; i < line.length(); i++) {
                    b = Pattern.matches("[^\\p{ASCII}]", line[i])
                    if (b) {
                        countInLine++
                        countInFile++
                        if (countInLine == 1) {
                            firstNotAsciiChar = i
                        }
                        if (countInLine <= MAX_MATCH_IN_LINE) {
                            lastNotAsciiChar = i

                        }
                    }
                }
                if (countInLine > MAX_MATCH_IN_LINE) {
                    logger.info("in line №" + lineNumber + " more than 5 not ASCII of characters: " + line.substring(firstNotAsciiChar, lastNotAsciiChar + 1) + "...")
                }
                if (countInLine != 0 && countInLine <= MAX_MATCH_IN_LINE) {
                    logger.info("in line №" + lineNumber + " - " + countInLine + " not ASCII of characters: " + line.substring(firstNotAsciiChar, lastNotAsciiChar + 1))
                }
                if (countInFile > MAX_MATCH_IN_FILE) {
                    logger.info("in file " + f.getName() + " more than 100 not ASCII of characters. Last line: " + line.substring(firstNotAsciiChar, lastNotAsciiChar + 1))
                    break
                }
            }
        }
    }

    void fileTest() {
        analysisFile()
    }

    public static int work(String[] args) {

        Options options = new Options()
                .addOption(makeOptionWithArgument("expansion", "Expansion", false))
                .addOption(makeOptionWithArgument("path", "Path", true))

        CommandLine commandLine = null;
        try {
            commandLine = new GnuParser().parse(options, args)
        } catch (ParseException e) {
            printHelp(options);
            return 255;
        }

        Find unicode = new Find()
        def path
        path = commandLine.getOptionValue("path")

        if (unicode.isDirectory(path)) {
            if (commandLine.getOptionValue("expansion")) {
                Files.walk(Paths.get(path)).each {
                    ArrayList file
                    file = it.fileName.toString().split("\\.")

                    if((file.last() ==~ /${commandLine.getOptionValue("expansion")}/)){
                        println it
                        unicode.analysisFile(it.toString())
                    }
                }
            }
        }

        return 0
    }

    public static void main(String[] args) {
        System.exit(work(args))
    }
}
