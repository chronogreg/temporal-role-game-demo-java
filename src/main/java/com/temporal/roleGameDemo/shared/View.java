package com.temporal.roleGameDemo.shared;

import com.fasterxml.jackson.annotation.*;
import com.temporal.roleGameDemo.shared.CellKinds;

import java.lang.Math;

@JsonIgnoreProperties(ignoreUnknown = true)
public class View {

    @JsonProperty("cells")
    private CellKinds[][] cells;

    @JsonProperty("posX")
    private int posX;

    @JsonProperty("posY")
    private int posY;

    @JsonProperty("hasTreasure")
    private boolean hasTreasure;

    @JsonProperty("weatherInfo")
    private String weatherInfo;

    public View()
    {
        this(-1, -1,
             CellKinds.Unknown, CellKinds.Unknown, CellKinds.Unknown,
             CellKinds.Unknown, CellKinds.Unknown, CellKinds.Unknown,
             CellKinds.Unknown, CellKinds.Unknown, CellKinds.Unknown,
             false, null);
    }

    public View(int x, int y,
                CellKinds cellUL, CellKinds cellU, CellKinds cellUR,
                CellKinds cellL, CellKinds cell, CellKinds cellR,
                CellKinds cellDL, CellKinds cellD, CellKinds cellDR,
                boolean hasTreasure, String weatherInfo)
    {
        // Arras is indexed as [x][y], we an array of columns (not an array or rows):
        cells = new CellKinds[][] {{cellUL, cellL, cellDL},
                                   {cellU, cell, cellD},
                                   {cellUR, cellR, cellDR}};

        posX = x;
        posY = y;
        this.hasTreasure = hasTreasure;
        this.weatherInfo = weatherInfo;
    }

    public int getPositionX()
    {
        return posX;
    }

    public int getPositionY()
    {
        return posY;
    }

    public boolean hasTreasure()
    {
        return hasTreasure;
    }

    public String getWeatherInfo()
    {
        return weatherInfo;
    }

    public CellKinds getCellKindRelative(int dX, int dY)
    {
        if (dX < -1 || dX > 1)
        {
            throw new IllegalArgumentException("dX may not be " + dX);
        }

        if (dY < -1 || dY > 1)
        {
            throw new IllegalArgumentException("dX may not be " + dY);
        }

        return cells[1 + dX][1 + dY];
    }

    public boolean isVisible(int x, int y)
    {
        return Math.abs(x - posX) <= 1 && Math.abs(y - posY) <= 1;
    }

    public CellKinds getCellKindAbsolute(int x, int y)
    {
        return isVisible(x, y)
                    ? getCellKindRelative(x - posX, y - posY)
                    : CellKinds.Unknown;
    }

    public String toString()
    {
        StringBuffer str = new StringBuffer();

        str.append("Pos: (");
        str.append(posX);
        str.append(", ");
        str.append(posY);
        str.append("); Treasure: ");
        str.append(hasTreasure ? 'Y' : 'N');
        str.append("; Weather: ");
        str.append(weatherInfo == null ? "<unknown>" : weatherInfo);
        str.append("; Cells:");
        str.append("\n");

        str.append(CellKinds.GetTextCharView(getCellKindRelative(-1, -1)));
        str.append(CellKinds.GetTextCharView(getCellKindRelative(0, -1)));
        str.append(CellKinds.GetTextCharView(getCellKindRelative(1, -1)));
        str.append("\n");

        str.append(CellKinds.GetTextCharView(getCellKindRelative(-1, 0)));
        str.append(CellKinds.GetTextCharView(getCellKindRelative(0, 0)));
        str.append(CellKinds.GetTextCharView(getCellKindRelative(1, 0)));
        str.append("\n");

        str.append(CellKinds.GetTextCharView(getCellKindRelative(-1, 1)));
        str.append(CellKinds.GetTextCharView(getCellKindRelative(0, 1)));
        str.append(CellKinds.GetTextCharView(getCellKindRelative(1, 1)));
        str.append("\n");

        return str.toString();
    }
}
