package gg.desolve.melee.inventory.grant;

import com.cryptomorin.xseries.XMaterial;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import fr.minuskube.inv.content.Pagination;
import fr.minuskube.inv.content.SlotIterator;
import gg.desolve.melee.Melee;
import gg.desolve.melee.grant.GrantType;
import gg.desolve.melee.profile.Profile;
import gg.desolve.melee.rank.Rank;
import gg.desolve.melee.rank.RankManager;
import gg.desolve.mithril.Mithril;
import gg.desolve.mithril.relevance.Material;
import gg.desolve.mithril.relevance.Message;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class GrantConfirmInventory implements InventoryProvider {

    private final Profile profile;
    private final Rank rank;
    private final String duration;
    private final String scope;
    private final String reason;
    private boolean acknowledged;

    public GrantConfirmInventory(Profile profile, Rank rank, String duration, String scope, String reason) {
        this.profile = profile;
        this.rank = rank;
        this.duration = duration;
        this.scope = scope;
        this.reason = reason;
    }

    public static SmartInventory getInventory(Profile profile, Rank rank, String duration, String scope, String reason) {
        return SmartInventory.builder()
                .id("grantConfirmInventory")
                .provider(new GrantConfirmInventory(profile, rank, duration, scope, reason))
                .size(3, 9)
                .title(Message.translate("<dark_gray>Confirming for " + profile.getUsernameColored() + "<dark_gray>..."))
                .manager(Mithril.getInstance().getInventoryManager())
                .build();
    }

    @Override
    public void init(Player player, InventoryContents contents) {
        ItemStack glassStack = XMaterial.GRAY_STAINED_GLASS_PANE.parseItem();
        ItemMeta glassMeta = glassStack.getItemMeta();
        glassMeta.setDisplayName(" ");
        glassStack.setItemMeta(glassMeta);
        contents.fillRow(0, ClickableItem.empty(glassStack));
        contents.fillColumn(1, ClickableItem.empty(glassStack));

        ItemStack confirmStack = Material.getSkull("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYTc5YTVjOTVlZTE3YWJmZWY0NWM4ZGMyMjQxODk5NjQ5NDRkNTYwZjE5YTQ0ZjE5ZjhhNDZhZWYzZmVlNDc1NiJ9fX0=");
        ItemMeta confirmMeta = confirmStack.getItemMeta();
        confirmMeta.setDisplayName(Message.translate("<bold><green>Confirm"));
        confirmMeta.setLore(Stream.of(
                "<gray>By pressing this you will grant",
                "<gray>the selected rank",
                "<gray>",
                acknowledged ? "<yellow>Click to confirm" : "<red>Grant is not acknowledged"
        ).map(Message::translate).toList());
        confirmStack.setItemMeta(confirmMeta);
        contents.set(1, 3, ClickableItem.of(confirmStack, r -> {
            if (acknowledged) {
                player.closeInventory();
                player.performCommand("grantmanual " + profile.getUsername() + " " + rank.getName() + " " + duration + " " + scope + " " + reason);
            }
        }));

        ItemStack cancelStack = Material.getSkull("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYmViNTg4YjIxYTZmOThhZDFmZjRlMDg1YzU1MmRjYjA1MGVmYzljYWI0MjdmNDYwNDhmMThmYzgwMzQ3NWY3In19fQ==");
        ItemMeta cancelMeta = cancelStack.getItemMeta();
        cancelMeta.setDisplayName(Message.translate("<bold><red>Cancel"));
        cancelMeta.setLore(Stream.of(
                "<gray>Stop the grant process",
                "<gray>",
                "<yellow>Click to terminate"
        ).map(Message::translate).toList());
        cancelStack.setItemMeta(cancelMeta);
        contents.set(1, 6, ClickableItem.of(cancelStack, r -> {
            player.closeInventory();
            Message.send(player, "<red>Cancelled grant process for " + profile.getUsernameColored() + ".");
        }));

        ItemStack profileStack = Material.getSkull(profile.getUuid().toString());
        ItemMeta profileMeta = profileStack.getItemMeta();
        profileMeta.setDisplayName(Message.translate(profile.getUsernameColored()));
        profileMeta.setLore(Stream.of(
                "<gray>Current Rank: " + profile.getGrant().getRank().getNameColored(),
                "<gray>Grants: <green>" + profile.getGrants().stream().filter(grant -> grant.getType() == GrantType.ACTIVE).toList().size(),
                "<gray>",
                Melee.getInstance().getRankManager().compare(profile.getGrant().getRank(), rank) ?
                        rank.getNameColored() + "<red> will not become highest" :
                        rank.getNameColored() + "<green> will become highest"
        ).map(Message::translate).toList());
        profileStack.setItemMeta(profileMeta);
        contents.set(1, 0, ClickableItem.empty(profileStack));


        ItemStack infoStack = XMaterial.MAP.parseItem();
        ItemMeta infoMeta = infoStack.getItemMeta();
        infoMeta.setDisplayName(Message.translate("<bold><green>Information"));
        infoMeta.setLore(Stream.of(
                "<gray>Rank: " + rank.getNameColored(),
                "<gray>Duration: <aqua>" + duration,
                "<gray>Scope: <aqua>" + scope,
                "<gray>Reason: <aqua>" + reason,
                "<gray>",
                acknowledged ? "<green>Grant is acknowledged" : "<yellow>Click to acknowledge grant"
        ).map(Message::translate).toList());
        infoStack.setItemMeta(infoMeta);
        contents.set(2, 0, ClickableItem.of(infoStack, r -> {
            if (!acknowledged) {
                acknowledged = true;
                init(player, contents);
            }
        }));
    }

    @Override
    public void update(Player player, InventoryContents contents) {}
}
