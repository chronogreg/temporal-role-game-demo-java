package com.temporal.roleGameDemo.shared;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MapCell
{
    private CellKinds kind;
    private int treeCount;

    public MapCell()
    {
        this(CellKinds.Unknown);
    }

    public MapCell(CellKinds kind)
    {
        this.kind = kind;
        treeCount = 0;
    }

    public CellKinds getKind()
    {
        return kind;
    }

    public void setKind(CellKinds cellKind)
    {
        kind = cellKind;
    }

    public int getTreeCount()
    {
        return treeCount;
    }

    public void setTreeCount(int count)
    {
        if (count < 0)
        {
            throw new IllegalArgumentException("count may not be < 0.");
        }

        if (count > 9)
        {
            throw new IllegalArgumentException("count may not be > 9.");
        }

        treeCount = count;
    }

    public char getTextCharView()
    {
        if (getKind() == CellKinds.Empty && getTreeCount() > 0)
        {
            return (char) ('0' + getTreeCount());
        }

        return  CellKinds.getTextCharView(getKind());
    }
}
