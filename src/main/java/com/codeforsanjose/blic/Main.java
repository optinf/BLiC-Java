package com.codeforsanjose.blic;

import com.google.gson.Gson;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Main {

    private static final Logger log = LogManager.getLogger(Main.class);
    private static final int minimumNumberArguments = 2;

    public static void main(String[] args) {
        log.info("\n"
                + "+-------------------------------------------+\n"
                + "|            Broken Link Checker            |\n"
                + "+-------------------------------------------+");

        startCli(args);
    }

    private static boolean hasMinimumNumberArgurments(String[] arguments) {
        return arguments.length < minimumNumberArguments;
    }

    private static void exit() {
        log.info(getUsage());
        System.exit(-1);
    }

    private static void startCli(String[] arguments) {


        if (hasMinimumNumberArgurments(arguments)) {
            exit();
        } else {
            String arg_url = arguments[0];
            String fileName = arguments[1];
            Integer arg_depth_limit = arguments.length >= 3 ? parseArgInt(arguments, 2, "Second argument (depth limit) must be a valid integer") : 1;
            Integer arg_fail_tolerance = arguments.length >= 4 ? parseArgInt(arguments, 3, "Third argument (fail tolerance) must be a valid integer") : 1;
            Integer max_thread_limit = arguments.length >= 5 ? parseArgInt(arguments, 4, "Third argument (max thread limit) must be a valid integer") : 1;
            String authentication = arguments.length >= 6 ? arguments[5] : null;
            List<String> exclusionPatterns = arguments.length >= 7 ? Arrays.asList(arguments[6].split(";")) : null;

            try {
                CrawlController crawlController = new CrawlController(arg_url, arg_depth_limit, arg_fail_tolerance, max_thread_limit, exclusionPatterns, authentication);
                log.debug("args : " + String.join(",", arguments));

                if (crawlController == null) {
                    exit();
                }

                crawlController.crawl();

                List<WebPage> results = crawlController.getResults();

                Collections.sort(results);

                String json = new Gson().toJson(results);

                FileWriter writer = new FileWriter(fileName);
                writer.write(json);
                writer.close();

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public static String getUsage() {
        return
                "usage: blic.jar [url] [fileName] [depth limit] [fail tolerance] [max thread limit] [basic auth] [exclusion]\n"
                        + "\turl:               the URL of a website to be checked for broken links.\n"
                        + "\tfileName:               the name where the result is write\n"
                        + "\tdepth limit:       Optional number defining how far links should be\n"
                        + "\t                    traversed before stopping\n\n"
                        + "\tfail tolerance:    Optional number defining how many retry attempts\n"
                        + "\t                    should be made for a URL that fails to respond in\n"
                        + "\t                    an expected manner.\n\n"
                        + "\tmax thread limit:  Optional number that disables the dynamic thread\n"
                        + "\t                    management and defines the max number of threads\n"
                        + "\tbasic auth:        Optional Basic auth credentials (username:password)"
                        + "\tExclusions         Optional URL patterns to avoid : (/login.*;/private)\n";
    }

    /**
     * Try to parse an integer from args at position argn
     *
     * @param args       the array of strings containing the number to be parsed
     * @param argn       array position in args that should be parsed
     * @param errmessage message to be written to the console if parsing the integer fails
     * @return null if unable to parse the int from the given String
     */
    static Integer parseArgInt(String[] args, int argn, String errmessage) {
        Integer res = null;
        try {
            res = Integer.parseInt(args[argn]);
        } catch (NumberFormatException nfe) {
            System.out.println(errmessage);
        }
        return res;
    }
}
