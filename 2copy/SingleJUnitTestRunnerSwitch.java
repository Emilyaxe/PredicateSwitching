/**
 *  * Created by mirror on 11/20/16.
 *   */

import org.junit.runner.JUnitCore;
import org.junit.runner.Request;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import java.lang.management.*;

public class SingleJUnitTestRunnerSwitch {
    public static void main(String... args) throws ClassNotFoundException {
// get name representing the running Java virtual machine.
// String name = ManagementFactory.getRuntimeMXBean().getName();
// System.out.println(name);
// // get pid
// String pid = name.split("@")[0];
// System.out.println(">>>>>>>> Pid is:" + pid);
        String[] classAndMethod = args[0].split("::");
        String project = args[1];
        String bugid = args[2];
        Request request = Request.method(Class.forName(classAndMethod[0]), classAndMethod[1]);
       // String writeFilePath = "/home/emily/WorkSpace/Data/test/testResult/" + project + "/" + bugid + ".txt";

        Result result = new JUnitCore().run(request);

        if (result.wasSuccessful()) {
            System.out.println("Success.\n");

        }
        else {
            Failure failure = result.getFailures().get(0);
/*
            System.out.println("\nTrace:\n"+failure.getTrace());
            System.out.println("\nMessage:\n"+failure.getMessage());
            System.out.println("\nTestHeader:\n"+failure.getTestHeader());
            System.out.println("\nRunTime: \n" + String.valueOf(result.getRunTime()) + " ms");*/
            System.out.println("Failure.\n");
/*
            auxiliary.Dumper.write(writeFilePath, "\nTrace:\n"+failure.getTrace());
            auxiliary.Dumper.write(writeFilePath, "\nMessage:\n"+failure.getMessage());
            auxiliary.Dumper.write(writeFilePath, "\nTestHeader:\n"+failure.getTestHeader());
            auxiliary.Dumper.write(writeFilePath, "\nRunTime: \n" + String.valueOf(result.getRunTime()) + " ms");
            auxiliary.Dumper.write(writeFilePath, "\nFailure.\n");
  */          }

        System.exit(0);
    }

}


