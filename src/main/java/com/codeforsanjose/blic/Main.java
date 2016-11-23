package com.codeforsanjose.blic;

import com.google.gson.Gson;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Main {

    private static final Logger log = LogManager.getLogger(Main.class);

    public static void main(String[] args) {
        log.info("\n"
            + "+-------------------------------------------+\n"
            + "|            Broken Link Checker            |\n"
            + "+-------------------------------------------+");

        startCli(args);
    }

    private static void startCli(String[] args) {
        CrawlController c = null;

        int requiredArgNumber = 6;

        if (args.length < requiredArgNumber) {
            System.out.println(getUsage());
            System.exit(-1);
        }
        else {
            String arg_url = args[0];
            Integer arg_depth_limit = parseArgInt(args, 1, "Second argument (depth limit) must be a valid integer");
            Integer arg_fail_tolerance = parseArgInt(args, 2, "Third argument (fail tolerance) must be a valid integer");
            Integer max_thread_limit = parseArgInt(args, 3, "Third argument (max thread limit) must be a valid integer");
            List<String> exclusionPatterns = Arrays.asList(args[4].split(";"));
            String authentication = args[5];

            try {
                c = new CrawlController(arg_url, arg_depth_limit, arg_fail_tolerance, max_thread_limit, exclusionPatterns, authentication);
            }
            catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }

        log.debug("args : " + String.join(",", args));

        if (c == null) {
            System.out.println(getUsage());
            System.exit(-1);
        }

        c.crawl();

        List<WebPage> results = c.getResults();
        Collections.sort(results);
        String json = new Gson().toJson(results);
        System.out.println(json);
        log.info(json);
    }

    public static String getUsage() {
        return
            "usage: blic.jar [url] [depth limit] [fail tolerance] [max thread limit] [exclusion] [basic auth]\n"
                + "\turl:               the URL of a website to be checked for broken links.\n"
                + "\tdepth limit:       number defining how far links should be\n"
                + "\t                    traversed before stopping\n\n"
                + "\tfail tolerance:    number defining how many retry attempts\n"
                + "\t                    should be made for a URL that fails to respond in\n"
                + "\t                    an expected manner.\n\n"
                + "\tmax thread limit:  number that disables the dynamic thread\n"
                + "\t                    management and defines the max number of threads\n"
                + "\tExclusions         URL patterns to avoid : (/login.*;/private)\n"
                + "\tbasic auth:  Basic auth credentials (username:password)";
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
        }
        catch (NumberFormatException nfe) {
            System.out.println(errmessage);
        }
        return res;
    }
}
