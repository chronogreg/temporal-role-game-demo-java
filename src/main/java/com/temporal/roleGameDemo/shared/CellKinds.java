package com.temporal.roleGameDemo.shared;

public enum CellKinds
{
    Unknown,
    Empty,
    Wall,
    Home,
    Monster,
    Treasure;

    public static char GetTextCharView(CellKinds cellKind)
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
