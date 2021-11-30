package com.temporal.roleGameDemo.server.workflow;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

@WorkflowInterface
public interface TreeGrowthWorkflow
{
    @WorkflowMethod
    void growTrees(int locationX, int locationY);
}

