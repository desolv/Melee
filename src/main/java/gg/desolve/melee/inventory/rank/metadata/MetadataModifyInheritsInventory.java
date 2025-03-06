package gg.desolve.melee.inventory.rank.metadata;

import com.cryptomorin.xseries.XMaterial;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import fr.minuskube.inv.content.Pagination;
import fr.minuskube.inv.content.SlotIterator;
import gg.desolve.melee.Melee;
import gg.desolve.melee.profile.Profile;
import gg.desolve.melee.rank.Rank;
import gg.desolve.mithril.Mithril;
import gg.desolve.mithril.relevance.Material;
import gg.desolve.mithril.relevance.Message;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class MetadataModifyInheritsInventory implements InventoryProvider {

    private final Profile profile;
    private final Rank rank;

    public MetadataModifyInheritsInventory(Profile profile, Rank rank) {
        this.profile = profile;
        this.rank = rank;
    }

    public static SmartInventory getInventory(Profile profile, Rank rank) {
        return SmartInventory.builder()
                .id("rankModifyInheritsInventory")
                .provider(new MetadataModifyInheritsInventory(profile, rank))
                .size(3, 9)
                .title(Message.translate("Modifying Inherits"))
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
        List<ClickableItem> inherits = new ArrayList<>();

        rank.getInherits().forEach(inheritRank -> {
            Rank inherit = Melee.getInstance().getRankManager().retrieve(inheritRank);
            XMaterial material = XMaterial.matchXMaterial(Material.getWool(inherit.getColor()) + "_WOOL")
                    .orElse(XMaterial.WHITE_WOOL);
            ItemStack stack = material.parseItem();
            ItemMeta meta = stack.getItemMeta();
            meta.setDisplayName(Message.translate(inherit.getNameColored()));
            meta.setLore(Stream.of(
                    "<white>",
                    "<yellow>Click to remove inherit"
            ).map(Message::translate).toList());
            stack.setItemMeta(meta);
            inherits.add(ClickableItem.of(stack, r -> {
                rank.setInherits(rank.getInherits().stream()
                        .filter(i -> !i.equalsIgnoreCase(inheritRank))
                        .toList());
                rank.save();
                getInventory(profile, rank).open(player);
                Melee.getInstance().getRankManager().publish(rank, "none");
            }));
        });

        pagination.setItemsPerPage(18);
        pagination.setItems(inherits.toArray(new ClickableItem[0]));
        pagination.addToIterator(contents.newIterator(SlotIterator.Type.HORIZONTAL, 1, 0));


        ItemStack addStack = Material.getSkull("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvM2VkZDIwYmU5MzUyMDk0OWU2Y2U3ODlkYzRmNDNlZmFlYjI4YzcxN2VlNmJmY2JiZTAyNzgwMTQyZjcxNiJ9fX0=");
        ItemMeta addMeta = addStack.getItemMeta();
        addMeta.setDisplayName(Message.translate("<green>Add Inherit"));
        addMeta.setLore(Stream.of(
                "<gray>Ranks that this rank will inherit",
                "<gray>attributes from",
                "<white>",
                "<yellow>Click to add inherit"
        ).map(Message::translate).toList());
        addStack.setItemMeta(addMeta);
        contents.set(0, 4, ClickableItem.of(addStack, r -> profile.setRankProcess(rank, "inherit")));

        if (!pagination.isFirst()) {
            ItemStack previousStack = Material.getSkull("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYmQ2OWUwNmU1ZGFkZmQ4NGU1ZjNkMWMyMTA2M2YyNTUzYjJmYTk0NWVlMWQ0ZDcxNTJmZGM1NDI1YmMxMmE5In19fQ==");
            ItemMeta previousMeta = previousStack.getItemMeta();
            previousMeta.setDisplayName(Message.translate("<yellow>Previous Page"));
            previousMeta.setLore(Stream.of(
                    "<gray>Click to go to the previous page"
            ).map(Message::translate).toList());
            previousStack.setItemMeta(previousMeta);

            contents.set(0, 0, ClickableItem.of(
                    previousStack,
                    e -> getInventory(profile, rank).open(player, pagination.previous().getPage())
            ));
        }

        if (!pagination.isLast()) {
            ItemStack nextStack = Material.getSkull("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTliZjMyOTJlMTI2YTEwNWI1NGViYTcxM2FhMWIxNTJkNTQxYTFkODkzODgyOWM1NjM2NGQxNzhlZDIyYmYifX19");
            ItemMeta nextMeta = nextStack.getItemMeta();
            nextMeta.setDisplayName(Message.translate("<yellow>Next Page"));
            nextMeta.setLore(Stream.of(
                    "<gray>Click to go to the next page"
            ).map(Message::translate).toList());
            nextStack.setItemMeta(nextMeta);

            contents.set(0, 8, ClickableItem.of(
                    nextStack,
                    e -> getInventory(profile, rank).open(player, pagination.next().getPage())
            ));
        }
    }

    @Override
    public void update(Player player, InventoryContents contents) {}
}
