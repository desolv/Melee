package gg.desolve.melee.command.inventory.grant;

import com.cryptomorin.xseries.XMaterial;
import com.google.common.collect.ImmutableList;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import fr.minuskube.inv.content.Pagination;
import fr.minuskube.inv.content.SlotIterator;
import gg.desolve.melee.Melee;
import gg.desolve.melee.common.Material;
import gg.desolve.melee.common.Message;
import gg.desolve.melee.player.profile.Hunter;
import gg.desolve.melee.player.rank.Rank;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class GrantConfirmInventory implements InventoryProvider {

    private final Hunter hunter;
    private final Rank rank;
    private final String scope;
    private final String duration;
    private final String reason;


    public GrantConfirmInventory(Hunter hunter, Rank rank, String scope, String duration, String reason) {
        this.hunter = hunter;
        this.rank = rank;
        this.scope = scope;
        this.duration = duration;
        this.reason = reason;
    }

    public static SmartInventory getInventory(Hunter hunter, Rank rank, String scope, String duration, String reason) {
        return SmartInventory.builder()
                .id("grantConfirmInventory")
                .provider(new GrantConfirmInventory(hunter, rank, scope, duration, reason))
                .parent(GrantInventory.getInventory(hunter))
                .size(3, 9)
                .title(Message.translate("<dark_gray>Granting Confirmation " + hunter.getUsernameColored() + "<dark_gray>.."))
                .manager(Melee.getInstance().getInventoryManager())
                .build();
    }

    @Override
    public void init(Player player, InventoryContents contents) {

        List<String> infoLore = Arrays.asList(
                "",
                "<white>Selected Rank: <aqua>" + rank.getDisplayColored(),
                "<white>Selected Scope: <aqua>" + scope,
                "<white>Selected Duration: <aqua>" + duration,
                "<white>Selected Reason: <aqua>" + reason,
                "");
        infoLore.replaceAll(Message::translate);

        Melee.getInstance().getInventoryManager().addButton(
                contents,
                XMaterial.PAPER,
                "<aqua>Reporting",
                infoLore,
                1,
                4,
                () -> {}
        );

        Melee.getInstance().getInventoryManager().addButton(
                contents,
                "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYTc5YTVjOTVlZTE3YWJmZWY0NWM4ZGMyMjQxODk5NjQ5NDRkNTYwZjE5YTQ0ZjE5ZjhhNDZhZWYzZmVlNDc1NiJ9fX0=",
                "<green>Confirm",
                new ArrayList<>(),
                1,
                2,
                () -> {
                    player.closeInventory();
                    player.performCommand(
                            "grantmanual username% rank% scope% duration% reason%"
                                    .replace("username%", hunter.getUsername())
                                    .replace("rank%", rank.getName())
                                    .replace("scope%", scope)
                                    .replace("duration%", duration)
                                    .replace("reason%", reason)
                    );
                }
        );

        Melee.getInstance().getInventoryManager().addButton(
                contents,
                "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMjc1NDgzNjJhMjRjMGZhODQ1M2U0ZDkzZTY4YzU5NjlkZGJkZTU3YmY2NjY2YzAzMTljMWVkMWU4NGQ4OTA2NSJ9fX0=",
                "<red>Cancel",
                new ArrayList<>(),
                1,
                6,
                () -> {
                    player.closeInventory();
                    Message.send(player, "<red>Cancelled granting for " + hunter.getUsernameColored() + ".");
                }
        );
    }

    @Override
    public void update(Player player, InventoryContents contents) {

    }

}
