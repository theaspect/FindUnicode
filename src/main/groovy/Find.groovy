import groovyjarjarcommonscli.*
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.util.regex.Pattern

class Find {

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

    void filesList(String path) {
        File f = new File(path)
        for (File s : f.listFiles()) {
            if (s.isFile()) {
                files.add(s);
            } else if (s.isDirectory()) {
                filesList(s.getAbsolutePath());
            }
        }

    }

    void filesList(String path, ArrayList expansion) {
        ArrayList file
        File f = new File(path)
        for (File s : f.listFiles()) {
            if (s.isFile()) {
                file = s.toString().split("\\.")
                for (int i = 0; i < expansion.size(); i++) {
                    if (file.last().equals(expansion.get(i))) {
                        files.add(s);
                    }
                }
            } else if (s.isDirectory()) {
                filesList(s.getAbsolutePath(), expansion);
            }
        }
    }

    void analysisFile(String filePath) {
        boolean b
        int countInLine
        int countInFile = 0
        int lineNumber = 0
        File f = new File(filePath);

        BufferedReader fin = new BufferedReader(new FileReader(f));
        String line;
        logger.info("Analysis of the file " + f.getName())
        while ((line = fin.readLine()) != null) {
            lineNumber++
            countInLine = 0
            for (int i = 0; i < line.length(); i++) {
                b = Pattern.matches("[^\\p{ASCII}]", line[i]);
                if (b) {
                    countInLine++
                    countInFile++
                }
            }
            if (countInLine > 5) {
                logger.info("in line №" + lineNumber + " more than 5 not ASCII of characters")
            }
            if (countInLine != 0 && countInLine <= 5) {
                logger.info("in line №" + lineNumber + " - " + countInLine + " not ASCII of characters")
            }
            if (countInFile > 100) {
                logger.info("in file " + f.getName() + " more than 100 not ASCII of characters")
                logger.println()
                break
            }
        }
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
        ArrayList expansion

        // Find file in directory
        path = commandLine.getOptionValue("path")

        if (unicode.isDirectory(path)) {
            if (commandLine.getOptionValue("expansion")) {
                expansion = commandLine.getOptionValue("expansion").toString().split("\\,")
                unicode.filesList(path, expansion)
            } else {
                unicode.filesList(path)
            }
        } else {
            unicode.logger.error("Path not found")
            return 1
        }

        //Find not ASCII char
        for (File file : unicode.files) {
            unicode.analysisFile(file.path.toString())
        }

        return 0
    }

    public static void main(String[] args) {
        System.exit(work(args))
    }
}
