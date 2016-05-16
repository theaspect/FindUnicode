/*
 * Copyright 2016 Valery Butuzov
 * <valery.butuzov@gmail.com>
 * Licensed under the Apache License, Version 2.0
 */
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

    Logger logger = LogManager.getLogger(Find.class)

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

    static ArrayList analysisFile(InputStream paths, String ignore) {
        int countInLine
        int countInFile = 0
        int lineNumber = 0
        ArrayList indexNoAscii
        def result = new ArrayList<Result>()
        ignore = (ignore == null) ? "" : ignore
        paths.eachLine {
            if (it.startsWith("\uFEFF")) {
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
                if (ignore.indexOf(matcher.group(0)) >= 0) {
                    continue
                }
                indexNoAscii.add(matcher.start())
                countInLine++
                countInFile++
            }

            if (countInLine > MAX_MATCH_IN_LINE) {
                result.add(new Result(lineNumber, it.substring(0, indexNoAscii[MAX_MATCH_IN_LINE] as int), indexNoAscii.take(MAX_MATCH_IN_LINE) as ArrayList, " more than 5 not ASCII of characters: "))
            }

            if (countInLine != 0 && countInLine <= MAX_MATCH_IN_LINE) {
                result.add(new Result(lineNumber, it.substring(0, indexNoAscii.last() + 1), indexNoAscii, " - " + countInLine + " not ASCII of characters: "))

            }
        }
        paths.close()
        return result
    }

    void arrayResult(String it, String fileName, String ignore, boolean v) {
        logger.info("Analysis of the file " + fileName)
        if(v){
            analysisFile(new FileInputStream(it), ignore).each {
                println it
            }
        }

    }
    /**
     * @param Way of the analysis of files
     * @param Extensions of files. If extension = null, there is an analysis of all files
     * @param Ignoring of characters. If ignore = null(""), look for all characters
     * @param If v = false, outputs only names of files in which there is unicode. 
     *        If v = true, outputs names of files in which there is unicode, with an output of the specific character in line
     */
    public static int work(String path, String extension = null, String ignore, boolean v) {

        Find unicode = new Find()

        if(!Files.isDirectory(Paths.get(path.toString()))){
            unicode.logger.info("Not found directory")
            return 1
        }
        Files.walk(Paths.get(path.toString())).each {
            if (Files.isDirectory(it)) {
                return
            }
            /* Choice on the given extensions */
            if (extension != null && !Pattern.matches(/^(.*(${extension}))[^.]*$/, it.fileName.toString() as CharSequence)) {
                return
            }
            unicode.arrayResult(it.toString(), it.toAbsolutePath().toString(), ignore, v)
        }
        return 0
    }

    public static void main(String[] args) {

        Options options = new Options()
                .addOption(makeOptionWithArgument("extension", "Extension", false))
                .addOption(makeOptionWithArgument("ignore", "Ignore", false))
                .addOption(new Option("v","V"))
                .addOption(makeOptionWithArgument("path", "Path", true))

        CommandLine commandLine = null;
        try {
            commandLine = new GnuParser().parse(options, args)
        } catch (ParseException e) {
            printHelp(options);
            System.exit(255)
        }

        System.exit(work(commandLine.getOptionValue("path"), commandLine.getOptionValue("extension"), commandLine.getOptionValue("ignore"), commandLine.hasOption("v")))
    }

}
