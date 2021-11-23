package com.temporal.roleGameDemo;

import io.temporal.workflow.*;

@WorkflowInterface
public interface MapNavigationWorkflow {

    @WorkflowMethod
    public NavigationResults navigateMap(int width, int height);

    @SignalMethod
    public void tryMoveUp();

    @SignalMethod
    public void tryMoveDown();

    @SignalMethod
    public void tryMoveLeft();

    @SignalMethod
    public void tryMoveRight();

    @SignalMethod
    public void quit();

    @QueryMethod
    public int getMapWidth();

    @QueryMethod
    public int getMapHeight();

    @QueryMethod
    public View lookAround();
}
