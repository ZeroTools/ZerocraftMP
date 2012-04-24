package net.minecraft.src;

import java.util.*;

public class ComponentNetherBridgeStartPiece extends ComponentNetherBridgeCrossing3
{
    public StructureNetherBridgePieceWeight field_40296_a;
    public List field_40294_b;
    public List field_40295_c;
    public ArrayList field_40293_d;

    public ComponentNetherBridgeStartPiece(Random par1Random, int par2, int par3)
    {
        super(par1Random, par2, par3);
        field_40293_d = new ArrayList();
        field_40294_b = new ArrayList();
        StructureNetherBridgePieceWeight astructurenetherbridgepieceweight[] = StructureNetherBridgePieces.getPrimaryComponents();
        int i = astructurenetherbridgepieceweight.length;

        for (int j = 0; j < i; j++)
        {
            StructureNetherBridgePieceWeight structurenetherbridgepieceweight = astructurenetherbridgepieceweight[j];
            structurenetherbridgepieceweight.field_40654_c = 0;
            field_40294_b.add(structurenetherbridgepieceweight);
        }

        field_40295_c = new ArrayList();
        astructurenetherbridgepieceweight = StructureNetherBridgePieces.getSecondaryComponents();
        i = astructurenetherbridgepieceweight.length;

        for (int k = 0; k < i; k++)
        {
            StructureNetherBridgePieceWeight structurenetherbridgepieceweight1 = astructurenetherbridgepieceweight[k];
            structurenetherbridgepieceweight1.field_40654_c = 0;
            field_40295_c.add(structurenetherbridgepieceweight1);
        }
    }
}
