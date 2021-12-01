package com.temporal.roleGameDemo.server.workflow;

import io.temporal.client.ActivityCanceledException;
import io.temporal.client.ActivityCompletionClient;
import io.temporal.client.ActivityCompletionException;
import io.temporal.client.WorkflowClient;
import io.temporal.serviceclient.WorkflowServiceStubs;
import org.apache.commons.lang.NullArgumentException;

import java.time.Instant;
import java.util.Base64;
import java.util.Random;

public class LumberjackProcess
{
    private static final double StopAfterEachTreeProbability = 0.25;
    private static final int SleepAfterEachTreeMillis = 1000;

    public static void main(String[] args)
    {
        int initialTreeCount = 0;
        String activityTaskToken = null;

        if (args != null && args.length > 0 && args[0] != null)
        {
            try {
                initialTreeCount = Integer.parseInt(args[0]);
            } catch (Exception ex) {
                initialTreeCount = 0;
            }
        }

        if (args != null && args.length > 1)
        {
            activityTaskToken = args[1];
        }

        run(initialTreeCount, activityTaskToken);
    }

    public static void run(int initialTreeCount, String activityTaskTokenBase64)
    {
        (new LumberjackProcess()).exec(initialTreeCount, activityTaskTokenBase64);
    }

    public void exec(final int initialTreeCount, final String activityTaskTokenBase64)
    {
        // Decode and validate activityTaskToken:

        if (activityTaskTokenBase64 == null)
        {
            throw new NullArgumentException("activityTaskTokenBase64");
        }

        byte[] activityTaskToken;
        try
        {
            activityTaskToken = Base64.getDecoder().decode(activityTaskTokenBase64);
        }
        catch (Exception ex)
        {
            throw new IllegalArgumentException("activityTaskTokenBase64 is not well-formatted.", ex);
        }

        if (activityTaskToken.length == 0)
        {
            throw new IllegalArgumentException("activityTaskTokenBase64 encoded zero bytes.");
        }

        // Initialize ActivityCompletionClient:

        System.out.println("LumberjackProcess.exec(initialTreeCount=" + initialTreeCount + ", ...): Starting;"
                         + " realTimestamp: " + Instant.ofEpochMilli(System.currentTimeMillis()) + ";"
                         + " activityTaskTokenBase64: \"" + activityTaskTokenBase64 + "\".");

        ActivityCompletionClient activity = WorkflowClient.newInstance(WorkflowServiceStubs.newInstance())
                                                          .newActivityCompletionClient();

        // Start of business logic.

        Random rnd = new Random();

        int cutTrees = 0;
        while (cutTrees < initialTreeCount)
        {
            double dice = rnd.nextDouble();
            if (dice < StopAfterEachTreeProbability)
            {
                break;
            }

            cutTrees++;

            {
                Instant realTimestamp = Instant.ofEpochMilli(System.currentTimeMillis());
                System.out.println("LumberjackProcess.exec(initialTreeCount=" + initialTreeCount + ", ...): "
                                 + " cutTrees: " + cutTrees + ";"
                                 + " realTimestamp: " + realTimestamp + ".");
            }

            try
            {
                activity.heartbeat(activityTaskToken, cutTrees);
                Thread.sleep(SleepAfterEachTreeMillis);
            }
            catch (ActivityCanceledException aCnclEx)
            {
                activity.reportCancellation(activityTaskToken, cutTrees);
            }
            catch (ActivityCompletionException aCmplEx) {}
            catch (InterruptedException intEx) {}
        }

        System.out.println("LumberjackProcess.exec(initialTreeCount=" + initialTreeCount + ", ...): Completing;"
                         + " cutTrees: " + cutTrees + ";"
                         + " realTimestamp: " + Instant.ofEpochMilli(System.currentTimeMillis()) + ";"
                         + " activityTaskTokenBase64: \"" + activityTaskTokenBase64 + "\".");

        activity.complete(activityTaskToken, cutTrees);
    }
}
