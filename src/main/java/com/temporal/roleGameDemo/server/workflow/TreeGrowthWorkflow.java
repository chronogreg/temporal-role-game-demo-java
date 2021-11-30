package com.temporal.roleGameDemo.server.workflow;

import com.temporal.roleGameDemo.shared.MapNavigationWorkflow;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

@WorkflowInterface
public interface TreeGrowthWorkflow
{
    @WorkflowMethod
    public void growTrees(int locationX, int locationY);
}

