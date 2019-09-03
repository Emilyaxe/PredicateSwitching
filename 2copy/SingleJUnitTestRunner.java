/**
 *  * Created by mirror on 11/20/16.
 *   */

import org.junit.runner.JUnitCore;
import org.junit.runner.Request;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

public class SingleJUnitTestRunner {
    public static void main(String... args) throws ClassNotFoundException {
        String[] classAndMethod = args[0].split("::");
        String project = args[1];
        String bugid = args[2];
        String path = args[3];
        Request request = Request.method(Class.forName(classAndMethod[0]), classAndMethod[1]);
        //String writeFilePath = "/Users/liangjingjing/WorkSpace/Tool/PredicateSwitching/traceIfResult/" + project + "/" + bugid + ".txt";
        String writeFilePath = path + "/traceIfResult/" + project + "/" + bugid + ".txt";
        Result result = new JUnitCore().run(request);

        if (result.wasSuccessful()) {
            System.out.println("Success.\n");

        }
        else {
            Failure failure = result.getFailures().get(0);

            System.out.println("\nTrace:\n"+failure.getTrace());
            System.out.println("\nMessage:\n"+failure.getMessage());
            System.out.println("\nTestHeader:\n"+failure.getTestHeader());
            System.out.println("\nRunTime: \n" + String.valueOf(result.getRunTime()) + " ms");
            System.out.println("\nFailure.\n");

            auxiliary.Dumper.write(writeFilePath, "\nTrace:\n"+failure.getTrace());
            auxiliary.Dumper.write(writeFilePath, "\nMessage:\n"+failure.getMessage());
            auxiliary.Dumper.write(writeFilePath, "\nTestHeader:\n"+failure.getTestHeader());
            auxiliary.Dumper.write(writeFilePath, "\nRunTime: \n" + String.valueOf(result.getRunTime()) + " ms");
            auxiliary.Dumper.write(writeFilePath, "\nFailure.\n");
            }

        System.exit(0);
    }

}


