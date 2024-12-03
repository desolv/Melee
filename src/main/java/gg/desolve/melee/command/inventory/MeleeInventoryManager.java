package gg.desolve.melee.command.inventory;

import com.cryptomorin.xseries.XMaterial;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.InventoryManager;
import fr.minuskube.inv.content.InventoryContents;
import gg.desolve.melee.Melee;
import gg.desolve.melee.common.Material;
import gg.desolve.melee.common.Message;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public class MeleeInventoryManager  {

    public static void addLeftButton(InventoryContents contents, Runnable onClick) {
        addButton(
                contents,
                "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYmQ2OWUwNmU1ZGFkZmQ4NGU1ZjNkMWMyMTA2M2YyNTUzYjJmYTk0NWVlMWQ0ZDcxNTJmZGM1NDI1YmMxMmE5In19fQ==",
                "<yellow>Previous Page",
                new ArrayList<>(),
                0,
                0,
                onClick
        );
    }

    public static void addRightButton(InventoryContents contents, Runnable onClick) {
        addButton(
                contents,
                "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTliZjMyOTJlMTI2YTEwNWI1NGViYTcxM2FhMWIxNTJkNTQxYTFkODkzODgyOWM1NjM2NGQxNzhlZDIyYmYifX19",
                "<yellow>Next Page",
                new ArrayList<>(),
                0,
                8,
                onClick
        );
    }

    public static void addButton(InventoryContents contents, XMaterial material, String displayName, List<String> lore, int row, int column, Runnable onClick) {
        ItemStack stack = material.parseItem();
        ItemMeta meta = stack.getItemMeta();

        meta.setDisplayName(Message.translate(displayName));
        meta.setLore(lore);
        stack.setItemMeta(meta);

        contents.set(row, column, ClickableItem.of(stack, e -> onClick.run()));
    }

    public static void addButton(InventoryContents contents, String skull, String displayName, List<String> lore, int row, int column, Runnable onClick) {
        ItemStack stack = Material.getTypeSkull(skull);
        ItemMeta meta = stack.getItemMeta();

        meta.setDisplayName(Message.translate(displayName));
        meta.setLore(lore);
        stack.setItemMeta(meta);

        contents.set(row, column, ClickableItem.of(stack, e -> onClick.run()));
    }

}
