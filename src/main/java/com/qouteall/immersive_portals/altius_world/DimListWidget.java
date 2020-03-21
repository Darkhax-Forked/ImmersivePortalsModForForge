package com.qouteall.immersive_portals.altius_world;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.list.AbstractList;
import java.util.ArrayList;
import java.util.List;

public class DimListWidget extends AbstractList<DimTermWidget> {
    public final List<DimTermWidget> terms = new ArrayList<>();
    public final Screen parent;
    
    public DimListWidget(
        int width,
        int height,
        int top,
        int bottom,
        int itemHeight,
        Screen parent
    ) {
        super(Minecraft.getInstance(), width, height, top, bottom, itemHeight);
        this.parent = parent;
    }
    
    public void update() {
        this.clearEntries();
        this.terms.forEach(this::addEntry);
    }
}
