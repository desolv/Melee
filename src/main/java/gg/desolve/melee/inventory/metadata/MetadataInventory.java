package gg.desolve.melee.inventory.metadata;

import com.cryptomorin.xseries.XMaterial;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import fr.minuskube.inv.content.Pagination;
import fr.minuskube.inv.content.SlotIterator;
import gg.desolve.melee.Melee;
import gg.desolve.melee.inventory.grant.GrantInventory;
import gg.desolve.melee.profile.Profile;
import gg.desolve.mithril.Mithril;
import gg.desolve.mithril.relevance.Material;
import gg.desolve.mithril.relevance.Message;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class MetadataInventory implements InventoryProvider {

    private final Profile profile;

    public MetadataInventory(Profile profile) {
        this.profile = profile;
    }

    public static SmartInventory getInventory(Profile profile) {
        return SmartInventory.builder()
                .id("rankInventory")
                .provider(new MetadataInventory(profile))
                .size(3, 9)
                .title("Rank Metadata")
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

        Pagination pagination = contents.pagination();
        List<ClickableItem> ranks = new ArrayList<>();

        Melee.getInstance().getRankManager().sorted().forEach(rank -> {
            XMaterial material = XMaterial.matchXMaterial(Material.getWoolColor(rank.getColor()) + "_WOOL")
                    .orElse(XMaterial.WHITE_WOOL);

            ItemStack rankStack = material.parseItem();
            ItemMeta rankMeta = rankStack.getItemMeta();

            rankMeta.setDisplayName(Message.translate(rank.getNameColored()));
            rankMeta.setLore(Stream.of(
                    "<gray>Priority: <aqua>" + rank.getPriority(),
                    "<gray>Display Name: " + rank.getDisplayColored(),
                    "<gray>Permissions: <aqua>" + (rank.getPermissions().isEmpty() ? "&cNone" : rank.getPermissions().size()),
                    "<gray>Inherits: <aqua>" + (rank.getInherits().isEmpty() ? "&cNone" : rank.getInherits().size()),
                    "<gray>",
                    "<yellow>Click to modify metadata"
            ).map(Message::translate).toList());

            rankStack.setItemMeta(rankMeta);
            ranks.add(ClickableItem.of(rankStack, r -> MetadataModifyInventory.getInventory(profile, rank).open(player)));
        });

        pagination.setItemsPerPage(18);
        pagination.setItems(ranks.toArray(new ClickableItem[0]));
        pagination.addToIterator(contents.newIterator(SlotIterator.Type.HORIZONTAL, 1, 0));

        if (!pagination.isFirst()) {
            ItemStack previousStack = Material.getSkull("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYmQ2OWUwNmU1ZGFkZmQ4NGU1ZjNkMWMyMTA2M2YyNTUzYjJmYTk0NWVlMWQ0ZDcxNTJmZGM1NDI1YmMxMmE5In19fQ==");
            ItemMeta previousMeta = previousStack.getItemMeta();
            previousMeta.setDisplayName(Message.translate("<yellow><bold>Previous Page"));
            previousMeta.setLore(Stream.of(
                    "<gray>Click to go to the previous page"
            ).map(Message::translate).toList());
            previousStack.setItemMeta(previousMeta);

            contents.set(0, 0, ClickableItem.of(
                    previousStack,
                    e -> getInventory(profile).open(player, pagination.previous().getPage())
            ));
        }

        if (!pagination.isLast()) {
            ItemStack nextStack = Material.getSkull("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTliZjMyOTJlMTI2YTEwNWI1NGViYTcxM2FhMWIxNTJkNTQxYTFkODkzODgyOWM1NjM2NGQxNzhlZDIyYmYifX19");
            ItemMeta nextMeta = nextStack.getItemMeta();
            nextMeta.setDisplayName(Message.translate("<yellow><bold>Next Page"));
            nextMeta.setLore(Stream.of(
                    "<gray>Click to go to the next page"
            ).map(Message::translate).toList());
            nextStack.setItemMeta(nextMeta);

            contents.set(0, 8, ClickableItem.of(
                    nextStack,
                    e -> getInventory(profile).open(player, pagination.next().getPage())
            ));
        }
    }

    @Override
    public void update(Player player, InventoryContents contents) {}
}
