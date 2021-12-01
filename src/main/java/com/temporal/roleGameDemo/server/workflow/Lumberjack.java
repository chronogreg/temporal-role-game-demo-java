package com.temporal.roleGameDemo.server.workflow;

import io.temporal.activity.ActivityInterface;

@ActivityInterface
public interface Lumberjack
{
    int cutTrees(int initialTreeCount);
}
