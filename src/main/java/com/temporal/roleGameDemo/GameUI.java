package com.temporal.roleGameDemo;

import io.temporal.api.common.v1.WorkflowExecution;
import io.temporal.api.filter.v1.StartTimeFilter;
import io.temporal.api.filter.v1.WorkflowTypeFilter;
import io.temporal.api.workflow.v1.WorkflowExecutionInfo;
import io.temporal.api.workflowservice.v1.ListOpenWorkflowExecutionsRequest;
import io.temporal.api.workflowservice.v1.ListOpenWorkflowExecutionsResponse;
import io.temporal.api.workflowservice.v1.WorkflowServiceGrpc;
import io.temporal.api.workflowservice.v1.WorkflowServiceGrpc.WorkflowServiceBlockingStub;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowException;
import io.temporal.client.WorkflowOptions;
import io.temporal.client.WorkflowStub;
import io.temporal.serviceclient.WorkflowServiceStubs;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class GameUI
{
    private static final char PlayerChar = 'X';
    private static final char NotInViewChar = '.';
    private static final String MapIndentString = "    ";

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        (new GameUI()).run();
    }

    public void run() throws ExecutionException, InterruptedException {
        System.out.println();
        System.out.println();
        System.out.println("Hello player!");
        System.out.println();

        MapNavigationWorkflow mapNavigation = startOrOpenNavigationWorkflow();
        WorkflowStub mapNavigationWorkflow = WorkflowStub.fromTyped(mapNavigation);
        CompletableFuture<NavigationResults> mapNavigationResult = mapNavigationWorkflow.getResultAsync(NavigationResults.class);

        int mapWidth = mapNavigation.getMapWidth();
        int mapHeight = mapNavigation.getMapHeight();

        boolean keepRunning = true;
        while (!mapNavigationResult.isDone() && keepRunning)
        {
            System.out.println();
            System.out.println();
            System.out.println("This is what you can see:");
            System.out.println();
            View view = mapNavigation.lookAround();
            // System.out.println("Received new view:\n" + view.toString());

            printMap(mapWidth, mapHeight, view);
            printTreasureInfo(view);

            keepRunning = readAndProcessNextUserCommand(mapNavigation, mapNavigationWorkflow, mapNavigationResult);
        }

        System.out.println();
        System.out.println("Game over.");

        try
        {
            System.out.println("Game outcome: " + mapNavigationResult.get() + ".");
        }
        catch (Exception ex)
        {
            if (mapNavigationResult.isCancelled())
            {
                // Checking for Cancelled earlier is not safe due to the race it results in.
                System.out.println("Game outcome: CANCELLED.");
            }
            {
                System.out.println("Game outcome: ERROR (" + ex.getClass().getName() + ": " + ex.getMessage() + ").");
            }

            throw ex;
        }

        System.out.println();
        System.out.println("Good bye.");
    }

    private boolean readAndProcessNextUserCommand(MapNavigationWorkflow mapNavigation,
                                                  WorkflowStub mapNavigationWorkflow,
                                                  CompletableFuture<NavigationResults> mapNavigationResult)
    {
        try
        {
            Scanner inputScanner = new Scanner(System.in);

            while(!mapNavigationResult.isDone())
            {
                System.out.println("What do you do? (type \"help\" for help)");
                System.out.print("> ");

                String command = inputScanner.nextLine();
                if (command != null)
                {
                    command = command.toLowerCase();
                }

                // Reading command from stdin takes a long time. Let's check for completion again:
                if (mapNavigationResult.isDone())
                {
                    System.out.println("While you were thinking about it, the game has exited.");
                    return false;  // UI quits.
                }

                switch (command)
                {
                    case "up":
                        System.out.println("Navigating North...");
                        mapNavigation.tryMoveUp();
                        return true;  // UI keeps running.

                    case "down":
                        System.out.println("Navigating South...");
                        mapNavigation.tryMoveDown();
                        return true;  // UI keeps running.

                    case "left":
                        System.out.println("Navigating West...");
                        mapNavigation.tryMoveLeft();
                        return true;  // UI keeps running.

                    case "right":
                        System.out.println("Navigating East...");
                        mapNavigation.tryMoveRight();
                        return true;  // UI keeps running.

                    case "quit":
                        System.out.println("Giving up and ending the game...");
                        mapNavigationWorkflow.cancel();
                        return false;  // UI quits.

                    case "sleep":
                        System.out.println("You set up camp. Game will be saved...");
                        // We simply need to disconnect form the game, and it will be magically persisted by Temporal.
                        return false;  // UI quits.

                    case "help":
                        printCommandHelp();
                        break;

                    case "legend":
                        printMapLegend();
                        break;

                    default:
                        System.out.println("Invalid command!");
                        break;
                }  // switch(..)
            }  // while(..)
        }
        catch (WorkflowException ex)
        {
            System.out.println();
            System.out.println(ex.getClass().getName() + ": " + ex.getMessage());
        }

        return false;  // UI quits.
    }

    private MapNavigationWorkflow startOrOpenNavigationWorkflow()
    {
        WorkflowServiceStubs workflowServiceConnection = WorkflowServiceStubs.newInstance();
        WorkflowServiceBlockingStub workflowService = workflowServiceConnection.blockingStub();

        System.out.println("You can start a new game or you can connect to an existing game (listed below).");
        System.out.println("Remember: Any number of people can connect to a game.");
        System.out.println("          In such cases, they all will be controlling the same avatar.");
        System.out.println("          Don't we all have those days when multiple personalities seem to be inside our heads?");
        System.out.println();

        ListOpenWorkflowExecutionsRequest openGamesRequest = ListOpenWorkflowExecutionsRequest.newBuilder()
                .setNamespace("default")
                .setTypeFilter(WorkflowTypeFilter.newBuilder().setName("MapNavigationWorkflow"))
                .build();

        ListOpenWorkflowExecutionsResponse runningGamesResponse = workflowService. listOpenWorkflowExecutions(openGamesRequest);
        List<WorkflowExecutionInfo> runningGames = runningGamesResponse.getExecutionsList();

        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("'at' HH:mm:ss 'on' yyyy-MM-dd");

        System.out.println("[0] Start New Game.");
        for (int i = 0; i < runningGames.size(); i++)
        {
            WorkflowExecutionInfo runningGame = runningGames.get(i);
            LocalDateTime startTime = Instant.ofEpochSecond(runningGame.getStartTime().getSeconds(),
                                                            runningGame.getStartTime().getNanos())
                                             .atZone(ZoneId.systemDefault())
                                             .toLocalDateTime();

            System.out.println("[" + (i + 1) + "]"
                             + " \""+ runningGame.getExecution().getWorkflowId() + "\""
                             + " (started " + startTime.format(timeFormatter) + ").");
        }

        Scanner inputScanner = new Scanner(System.in);

        int loadChoice = -1;
        while (loadChoice < 0 || loadChoice > runningGames.size())
        {
            System.out.println("");
            System.out.println("Enter a number 0.." + runningGames.size() + ":");
            System.out.print("> ");

            String command = inputScanner.nextLine();
            try
            {
                loadChoice = Integer.parseInt(command);
            }
            catch (Exception ex)
            {
                loadChoice = -1;
            }
        }

        WorkflowClient workflowClient = WorkflowClient.newInstance(workflowServiceConnection);
        MapNavigationWorkflow game;

        if (loadChoice == 0)
        {
            System.out.println();
            System.out.println("Starting a new game.");

            String gameName = "";
            while(gameName.length() == 0)
            {
                System.out.println("Enter game name (entering an existing name will lead to error):");
                System.out.print("> ");
                gameName = inputScanner.nextLine();

                if (gameName == null)
                {
                    gameName = "";
                }
                else
                {
                    gameName = gameName.trim();
                }
            }
            System.out.println("Enter walkable field width:");
            System.out.print("> ");
            String command = inputScanner.nextLine();
            int fieldWidth = Integer.parseInt(command);

            System.out.println("Enter walkable field height:");
            System.out.print("> ");
            command = inputScanner.nextLine();
            int fieldHeight = Integer.parseInt(command);

            System.out.println("Starting new game \"" + gameName
                             + "\" on a field ox size " + fieldWidth + "x" + fieldHeight + "...");

            WorkflowOptions newGameWorkflowOptions = WorkflowOptions.newBuilder()
                    .setTaskQueue(Shared.ROLE_GAME_TASK_QUEUE)
                    .setWorkflowId(gameName)
                    .build();

            game = workflowClient.newWorkflowStub(MapNavigationWorkflow.class,
                                                  newGameWorkflowOptions);

            WorkflowClient.start(game::navigateMap, fieldWidth, fieldHeight);
        }
        else
        {
            WorkflowExecutionInfo gameToLoad = runningGames.get(loadChoice - 1);

            System.out.println("");
            System.out.println("Loading game \""+ gameToLoad.getExecution().getWorkflowId() + "\".");

            game = workflowClient.newWorkflowStub(MapNavigationWorkflow.class,
                                                  gameToLoad.getExecution().getWorkflowId());
        }

        return game;
    }

    private void printMap(int mapWidth, int mapHeight, View view)
    {
        for (int y = 0; y < mapHeight; y++)
        {
            System.out.print(MapIndentString);
            for (int x = 0; x < mapWidth; x++)
            {
                if (view.getPositionX() == x && view.getPositionY() == y)
                {
                    System.out.print(PlayerChar);
                }
                else if (view.isVisible(x, y))
                {
                    CellKinds viewCell = view.getCellKindAbsolute(x, y);
                    System.out.print(CellKinds.GetTextCharView(viewCell));
                }
                else
                {
                    System.out.print(NotInViewChar);
                }
            }

            System.out.println();
        }
    }

    private void printTreasureInfo(View view)
    {
        if (view.hasTreasure())
        {
            System.out.println("(You carry a TREASURE !)");
        }
        else
        {
            System.out.println("(You do not carry a treasure.)");
        }
    }

    private void printCommandHelp()
    {
        System.out.println("Navigate the map, find the Treasure, and bring it home while avoiding the Monsters.");
        System.out.println("The game ends when you enter your Home, when a Monster kills you or when you Quit.");
        System.out.println("Valid commands (case insensitive):");
        System.out.println("  - Up");
        System.out.println("  - Down");
        System.out.println("  - Left");
        System.out.println("  - Right");
        System.out.println("  - Quit");
        System.out.println("  - Sleep");
        System.out.println("  - Legend");
        System.out.println("  - Help");
    }

    private void printMapLegend()
    {
        System.out.println("Map Legend:");
        System.out.println("  - '" + PlayerChar
                         + "': This is YOU.");
        System.out.println("  - '" + NotInViewChar
                         + "': You CANNOT SEE that cell.");
        System.out.println("  - '" + CellKinds.GetTextCharView(CellKinds.Empty)
                         + "': You can see that cell and it is EMPTY.");
        System.out.println("  - '" + CellKinds.GetTextCharView(CellKinds.Wall)
                         + "': You can see that cell and it is a WALL.");
        System.out.println("  - '" + CellKinds.GetTextCharView(CellKinds.Home)
                         + "': You can see that cell and it is your HOME.");
        System.out.println("  - '" + CellKinds.GetTextCharView(CellKinds.Monster)
                         + "': You can see that cell and there is a MONSTER.");
        System.out.println("  - '" + CellKinds.GetTextCharView(CellKinds.Treasure)
                         + "': You can see that cell and there is a TREASURE.");
        System.out.println("  - '" + CellKinds.GetTextCharView(CellKinds.Unknown)
                         + "': It is not known what's in that cell.");
    }
}
