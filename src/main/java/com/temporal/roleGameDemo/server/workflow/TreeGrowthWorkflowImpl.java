package com.temporal.roleGameDemo.server.workflow;

import com.temporal.roleGameDemo.shared.MapNavigationWorkflow;
import io.temporal.api.common.v1.WorkflowExecution;
import io.temporal.workflow.Workflow;
import io.temporal.workflow.WorkflowInfo;

import java.util.Optional;

public class TreeGrowthWorkflowImpl implements TreeGrowthWorkflow
{
    private static final int MaxNumOfTrees = 9;
    private static final int SingleTreeGrowthDurationSecs = 30;

    @Override
    public void growTrees(int locationX, int locationY)
    {
        System.out.println("growTrees(" + locationX + ", " + locationY + "): Started.");

        WorkflowInfo thisWorkflow = Workflow.getInfo();
        Optional<String> parentRunId = thisWorkflow.getParentRunId();
        Optional<String> parentWorkflowId = thisWorkflow.getParentWorkflowId();

        if (! (parentRunId.isPresent() && parentWorkflowId.isPresent()))
        {
            throw new IllegalStateException("Cannot grow a tree because"
                                          + " this growth workflow has no parent that represents a map.");
        }

        WorkflowExecution parentExecution = WorkflowExecution.newBuilder()
                                                             .setRunId(parentRunId.get())
                                                             .setWorkflowId(parentWorkflowId.get())
                                                             .build();

        MapNavigationWorkflow map = Workflow.newExternalWorkflowStub(MapNavigationWorkflow.class, parentExecution);

        for (int t = 1; t <= MaxNumOfTrees; t++)
        {
            {
                //Instant workflowTimestamp = Instant.ofEpochMilli(Workflow.currentTimeMillis());
                //Instant realTimestamp = Instant.ofEpochMilli(System.currentTimeMillis());

                //System.out.println("growTrees(" + locationX + ", " + locationY + "): Signaling that a tree has grown"
                //        + " WT: " + workflowTimestamp + ";"
                //        + " RT: " + realTimestamp + ";");
            }

            map.treeHasGrown(locationX, locationY);
            Workflow.sleep(SingleTreeGrowthDurationSecs * 1000);
        }
    }
}
