package com.temporal.roleGameDemo.shared;

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

    @SignalMethod
    public void checkWeather();

    @SignalMethod
    public void plantTrees();

    @SignalMethod
    public void treeHasGrown(int locationX, int locationY);

    @QueryMethod
    public int getMapWidth();

    @QueryMethod
    public int getMapHeight();

    @QueryMethod
    public View lookAround();
}
