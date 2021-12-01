package com.temporal.roleGameDemo.server.workflow;

import io.temporal.activity.Activity;
import io.temporal.activity.ActivityExecutionContext;

import java.util.Base64;
import java.util.concurrent.ForkJoinPool;

public class LumberjackImpl implements Lumberjack
{
    @Override
    public int cutTrees(int initialTreeCount)
    {
        if (initialTreeCount < 1)
        {
            return 0;
        }

        ActivityExecutionContext activity = Activity.getExecutionContext();
        byte[] activityTaskToken = activity.getTaskToken();
        String activityTaskTokenBase64 = Base64.getEncoder().encodeToString(activityTaskToken);

        String initialTreeCountStr = ((Integer) initialTreeCount).toString();

        ForkJoinPool.commonPool().execute(() -> LumberjackProcess.main(new String[] { initialTreeCountStr,
                                                                                      activityTaskTokenBase64}));

        activity.doNotCompleteOnReturn();
        return 0;
    }
}
