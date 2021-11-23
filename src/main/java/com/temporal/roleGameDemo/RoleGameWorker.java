package com.temporal.roleGameDemo;

import io.temporal.client.WorkflowClient;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerFactory;

public class RoleGameWorker
{
    public static void main(String[] args)
    {
        WorkflowServiceStubs service = WorkflowServiceStubs.newInstance();
        WorkflowClient client = WorkflowClient.newInstance(service);

        WorkerFactory factory = WorkerFactory.newInstance(client);
        Worker worker = factory.newWorker(Shared.ROLE_GAME_TASK_QUEUE);

        worker.registerWorkflowImplementationTypes(MapNavigationWorkflowImpl.class);

        //worker.registerActivitiesImplementations(new FormatImpl());

        factory.start();
    }
}
