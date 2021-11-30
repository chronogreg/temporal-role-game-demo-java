package com.temporal.roleGameDemo.shared;

public enum CellKinds
{
    Unknown,
    Empty,
    Wall,
    Home,
    Monster,
    Treasure;

    public static char getTextCharView(CellKinds cellKind)
    {
        return switch (cellKind)
        {
            case Unknown -> '?';
            case Empty -> ' ';
            case Wall -> '#';
            case Home -> 'H';
            case Monster -> 'M';
            case Treasure -> 'T';
        };
    }
}
