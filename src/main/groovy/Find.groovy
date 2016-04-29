import groovyjarjarcommonscli.*

import java.nio.file.*

class Find {

    def files

    Find(ArrayList<String> files) {
        this.files = new ArrayList<String>()
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

    void filesList(def path){
        try {
            DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(path.toString()))
            for (Path file : stream) {
                if (!file.toFile().isDirectory()) {
                    files.add(file.getFileName())
                }
            }
        } catch (IOException | DirectoryIteratorException e) {
            println(e);
        }
    }

    public static int work(String[] args){

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

        unicode.filesList(path)

        println(unicode.files)

        return 0


    }

    public static void main(String[] args) {
        System.exit(work(args))
    }
}
