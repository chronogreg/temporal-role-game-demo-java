package com.temporal.roleGameDemo.shared;

import io.temporal.workflow.*;

@WorkflowInterface
public interface MapNavigationWorkflow
{
    @WorkflowMethod
    NavigationResults navigateMap(int width, int height);

    @SignalMethod
    void tryMoveUp();

    @SignalMethod
    void tryMoveDown();

    @SignalMethod
    void tryMoveLeft();

    @SignalMethod
    void tryMoveRight();

    @SignalMethod
    void quit();

    @SignalMethod
    void checkWeather();

    @SignalMethod
    void plantTrees();

    @SignalMethod
    void lumberTrees();

    @SignalMethod
    void treeHasGrown(int locationX, int locationY);

    @QueryMethod
    int getMapWidth();

    @QueryMethod
    int getMapHeight();

    @QueryMethod
    View lookAround();
}
