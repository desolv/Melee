package gg.desolve.melee.command.inventory.grant;

import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import fr.minuskube.inv.content.Pagination;
import fr.minuskube.inv.content.SlotIterator;
import gg.desolve.melee.Melee;
import gg.desolve.melee.command.inventory.MeleeInventoryManager;
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

public class GrantInventory implements InventoryProvider {

    private final Hunter hunter;

    public GrantInventory(Hunter hunter) {
        this.hunter = hunter;
    }

    public static SmartInventory getInventory(Hunter hunter) {
        return SmartInventory.builder()
                .id("grantInventory")
                .provider(new GrantInventory(hunter))
                .size(3, 9)
                .title(Message.translate("<dark_gray>Granting " + hunter.getUsernameColored() + "<dark_gray>.."))
                .manager(Melee.getInstance().getInventoryManager())
                .build();
    }

    @Override
    public void init(Player player, InventoryContents contents) {
        Pagination pagination = contents.pagination();
        List<ClickableItem> ranks = new ArrayList<>();

        Rank.getSortedRanks().forEach(rank -> {
            ItemStack stack = Material.getMiniMessageWool(rank.getColor()).parseItem();
            ItemMeta meta = stack.getItemMeta();

            Hunter granter = Hunter.getHunter((player).getUniqueId());

            String message = (!granter.hasPermission("melee.*") && (!rank.isGrantable()
                    || Rank.rankIsHigherThanRank(rank, granter.getGrant().getRank()))) ?
                    "<red>You cannot grant this rank" :
                    "<yellow>Click to grant this rank";

            message = rank.isBaseline() ? "<red>You cannot grant this rank" : message;

            List<String> lore = Arrays.asList(
                    "",
                    "<white>Display Name: " + rank.getDisplayColored(),
                    "<white>Prefix: " + rank.getPrefix() + "You",
                    "<white>Color: " + rank.getColor() + "this",
                    "",
                    message);
            lore.replaceAll(Message::translate);

            meta.setDisplayName(Message.translate(rank.getNameColored()));
            meta.setLore(lore);
            stack.setItemMeta(meta);

            if (message.equalsIgnoreCase("<red>You cannot grant this rank")) {
                ranks.add(ClickableItem.empty(stack));
                return;
            }

            ranks.add(ClickableItem.of(stack, e -> GrantScopeInventory.getInventory(hunter, rank).open(player)));
        });

        pagination.setItemsPerPage(18);
        pagination.setItems(ranks.toArray(new ClickableItem[0]));
        pagination.addToIterator(contents.newIterator(SlotIterator.Type.HORIZONTAL, 1, 0));

        List<String> hunterLore = Arrays.asList(
                "",
                "<white>Display Rank: " + hunter.getGrant().getRank().getDisplayColored(),
                "<white>Priority: <aqua>" + hunter.getGrant().getRank().getPriority(),
                "");
        hunterLore.replaceAll(Message::translate);

        MeleeInventoryManager.addButton(
                contents,
                hunter.getUuid().toString(),
                hunter.getUsernameColored(),
                hunterLore,
                0,
                4,
                () -> {}
        );

        if (!pagination.isFirst()) {
            MeleeInventoryManager.addLeftButton(
                    contents,
                    () -> getInventory(hunter).open(player, pagination.previous().getPage())
            );
        }

        if (!pagination.isLast()) {
            MeleeInventoryManager.addRightButton(
                    contents,
                    () -> getInventory(hunter).open(player, pagination.next().getPage())
            );
        }
    }

    @Override
    public void update(Player player, InventoryContents contents) {

    }

}
