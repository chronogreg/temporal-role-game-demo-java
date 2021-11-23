package com.temporal.roleGameDemo.server.workflow;

import java.time.Duration;
import java.util.Random;

import com.temporal.roleGameDemo.server.shared.WeatherProvider;
import com.temporal.roleGameDemo.shared.*;
import io.temporal.activity.ActivityOptions;
import io.temporal.failure.CanceledFailure;
import io.temporal.workflow.*;

public class MapNavigationWorkflowImpl implements MapNavigationWorkflow {

    private int mapWidth;
    private int mapHeight;

    private CellKinds[][] map;

    private int currPosX;
    private int currPosY;
    private String currWeatherInfo;

    boolean hasFoundTreasure;

    private boolean hasProcessedSignal;
    private boolean isCancelled;
    private boolean isWeatherRequested;

    private WeatherProvider weatherProvider;

    public MapNavigationWorkflowImpl()
    {
        ActivityOptions weatherProviderOptions = ActivityOptions.newBuilder()
                .setTaskQueue(TaskQueueNames.ROLE_GAME_TASK_QUEUE)
                .setStartToCloseTimeout(Duration.ofMinutes(1))
                .build();

        weatherProvider = Workflow.newActivityStub(WeatherProvider.class, weatherProviderOptions);
    }

    @Override
    public NavigationResults navigateMap(int width, int height)
    {
        System.out.println("DEBUG: Running or re-running workflow: \""
                         + Workflow.getInfo().getWorkflowId() + "\" (run: \""
                         + Workflow.getInfo().getRunId() + "\"; attempt: "
                         + Workflow.getInfo().getAttempt() + ").");

        if (width < 1)
        {
            System.out.println("DEBUG: Exiting with " + NavigationResults.InvalidGameConfiguration + " (width < 1).");
            return NavigationResults.InvalidGameConfiguration;
        }

        if (height < 1)
        {
            System.out.println("DEBUG: Exiting with " + NavigationResults.InvalidGameConfiguration + " (height < 1).");
            return NavigationResults.InvalidGameConfiguration;
        }

        initMap(width, height);
        currPosX = 1;
        currPosY = 1;
        hasFoundTreasure = false;
        isCancelled = false;

        waitForNextSignal();
        while (true)
        {
            // Act on current position:

            CellKinds currentCell = map[currPosX][currPosY];
            System.out.println("DEBUG: Player position: (" + currPosX + "," + currPosY + "); Cell Kind: " + currentCell+ ".");

            if (currentCell == CellKinds.Home)
            {
                return hasFoundTreasure
                            ? NavigationResults.HomeWithTreasure
                            : NavigationResults.HomeWithoutTreasure;
            }
            else if (currentCell == CellKinds.Treasure)
            {
                hasFoundTreasure = true;
                map[currPosX][currPosY] = CellKinds.Empty;
            }
            else if (currentCell == CellKinds.Monster)
            {
                System.out.println("DEBUG: Exiting with " + NavigationResults.DeathByMonster + ".");
                return NavigationResults.DeathByMonster;
            }
            else if (currentCell == CellKinds.Empty)
            {
                ; // nothing to do.
            }
            else
            {
                throw new IllegalStateException("The current cell ("
                                              + currPosX
                                              + ", "
                                              + currPosY + ") has an unexpected kind ("
                                              + currentCell
                                              + ").");
            }

            // Act on possible requests received via signals:

            if (isCancelled)
            {
                System.out.println("DEBUG: Exiting with " + NavigationResults.GameAborted + ".");
                return NavigationResults.GameAborted;
            }

            if (isWeatherRequested)
            {
                try
                {
                    currWeatherInfo = weatherProvider.getCurrentRainierForecast();
                }
                catch (Exception ex)
                {
                    currWeatherInfo = ex.getClass().getName() + ": " + ex.getMessage();
                }
            }

            waitForNextSignal();
        }
    }

    @Override
    public void tryMoveUp()
    {
        tryMoveTo(currPosX, currPosY - 1);
    }

    @Override
    public void tryMoveDown()
    {
        tryMoveTo(currPosX, currPosY + 1);
    }

    @Override
    public void tryMoveLeft()
    {
        tryMoveTo(currPosX - 1, currPosY);
    }

    @Override
    public void tryMoveRight()
    {
        tryMoveTo(currPosX + 1, currPosY);
    }

    @Override
    public void quit()
    {
        isCancelled = true;
        hasProcessedSignal = true;
    }

    @Override
    public void checkWeather()
    {
        isWeatherRequested = true;
        hasProcessedSignal = true;
    }

    @Override
    public int getMapWidth()
    {
        return  mapWidth;
    }

    @Override
    public int getMapHeight()
    {
        return mapHeight;
    }

    @Override
    public View lookAround()
    {
        View view = new View(currPosX, currPosY,
                             map[currPosX-1][currPosY-1], map[currPosX][currPosY-1], map[currPosX+1][currPosY-1],
                             map[currPosX-1][currPosY],   map[currPosX][currPosY],   map[currPosX+1][currPosY],
                             map[currPosX-1][currPosY+1], map[currPosX][currPosY+1], map[currPosX+1][currPosY+1],
                             hasFoundTreasure, currWeatherInfo);

        // System.out.println("lookAround result:\n" + view.toString());
        return view;
    }

    private void tryMoveTo(int targetX, int targetY)
    {
        if (targetX >= 0
                && targetX < mapWidth
                && targetY >= 0
                && targetY < mapHeight
                && map[targetX][targetY] != CellKinds.Wall)
        {
            currPosX = targetX;
            currPosY = targetY;
            currWeatherInfo = null;
            hasProcessedSignal = true;
        }
    }

    private void waitForNextSignal()
    {
        try
        {
            hasProcessedSignal = false;
            Workflow.await(() -> hasProcessedSignal);
        }
        catch (CanceledFailure cf)
        {
            System.out.println("DEBUG: If we wanted to handle cancellation, it would be here,"
                             + " but for now there is noting to do for such handling.");
            throw cf;
        }
    }

    private void initMap(int width, int height)
    {
        // Create map:
        mapWidth = width + 2;
        mapHeight = height + 2;
        map = new CellKinds[mapWidth][mapHeight];

        // Init home cell:
        map[1][1] = CellKinds.Home;

        // Place the treasure:
        Random rnd = Workflow.newRandom();

        int tX = 1, tY = 1;
        while (tX == 1 && tY == 1)
        {
            tX = rnd.nextInt(width);
            tY = rnd.nextInt(height);
        }

        map[tX + 1][tY + 1] = CellKinds.Treasure;

        // Place walls and monsters:

        final double monsterProbability = 0.07;

        for (int y = 0; y < mapHeight; y++)
        {
            for (int x = 0; x < mapWidth; x++)
            {
                if (y == 0)
                {
                    map[x][y] = CellKinds.Wall;
                }
                else if (y == mapHeight - 1)
                {
                    map[x][y] = CellKinds.Wall;
                }
                else if (x == 0)
                {
                    map[x][y] = CellKinds.Wall;
                }
                else if (x == mapWidth - 1)
                {
                    map[x][y] = CellKinds.Wall;
                }
                else if (map[x][y] != CellKinds.Home && map[x][y] != CellKinds.Treasure)
                {
                    double dice = rnd.nextDouble();
                    map[x][y] = (dice < monsterProbability)
                                    ? CellKinds.Monster
                                    : CellKinds.Empty;
                }
            }
        }
    }
}
