package com.wolfdrache.salormurder.items;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import com.wolfdrache.salormurder.helper.ItemHelper;

public class NavItems {
    public final static ItemStack leaveItem = ItemHelper.createNavItem(Material.MAGMA_CREAM, "§cRunde verlassen");
    public final static ItemStack spectatorTpItem = ItemHelper.createNavItem(Material.COMPASS, "§bTeleporter");
    public final static ItemStack knifeSelectorItem = ItemHelper.createNavItem(Material.IRON_SWORD, "§aMesser auswählen");

    public final static ItemStack bowItem = ItemHelper.createRoundBow();
    public final static ItemStack arrowItem = ItemHelper.createNavItem(Material.ARROW, "§eWaffe geladen");
}
