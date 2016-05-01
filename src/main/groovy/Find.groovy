import groovyjarjarcommonscli.*

class Find {

    ArrayList<File> files

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

    void filesList(String path){
        File f = new File(path);
        for (File s : f.listFiles()) {
            if (s.isFile()) {
                files.add(s);
            } else if (s.isDirectory()) {
                filesList(s.getAbsolutePath());
            }
        }
    }

    void filesList(String path, ArrayList expansion){
        ArrayList file

        File f = new File(path);
        for (File s : f.listFiles()) {
            if (s.isFile()) {
                file = s.toString().split("\\.")
                for(int i = 0; i < expansion.size(); i++){
                    if (file.last().equals(expansion.get(i))){
                        files.add(s);
                    }
                }

            } else if (s.isDirectory()) {
                filesList(s.getAbsolutePath(), expansion);
            }
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
        ArrayList expansion
        // Find file in directory
        path = commandLine.getOptionValue("path")

        if(commandLine.getOptionValue("expansion")){
            expansion = commandLine.getOptionValue("expansion").toString().split("\\,")

            println(expansion)

            unicode.filesList(path, expansion)

            for (File file : unicode.files) {
                //println(fil.getName());
                println(file.path.toString())
            }

        } else {

            unicode.filesList(path)

            for (File file : unicode.files) {
                //println(fil.getName());
                println(file.path.toString())
            }
        }

        return 0


    }

    public static void main(String[] args) {
        System.exit(work(args))
    }
}
