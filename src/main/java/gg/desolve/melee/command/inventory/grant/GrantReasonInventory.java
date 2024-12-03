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
import java.util.Random;

public class GrantReasonInventory implements InventoryProvider {

    private final Hunter hunter;
    private final Rank rank;
    private final String scope;
    private final String duration;


    public GrantReasonInventory(Hunter hunter, Rank rank, String scope, String duration) {
        this.hunter = hunter;
        this.rank = rank;
        this.scope = scope;
        this.duration = duration;
    }

    public static SmartInventory getInventory(Hunter hunter, Rank rank, String scope, String duration) {
        return SmartInventory.builder()
                .id("grantReasonInventory")
                .provider(new GrantReasonInventory(hunter, rank, scope, duration))
                .parent(GrantInventory.getInventory(hunter))
                .size(2, 9)
                .title(Message.translate("<dark_gray>Granting Reason " + hunter.getUsernameColored() + "<dark_gray>.."))
                .manager(Melee.getInstance().getInventoryManager())
                .build();
    }

    @Override
    public void init(Player player, InventoryContents contents) {
        Pagination pagination = contents.pagination();
        List<XMaterial> wools = new ArrayList<>(Material.getAestheticWools().values());
        List<ClickableItem> reasons = new ArrayList<>();
        Random random = new Random();

        ImmutableList.of("Store", "Whitelist", "Won", "Promoted", "Demoted", "Famous", "Other").forEach(reason -> {
            ItemStack stack = wools.get(random.nextInt(wools.size())).parseItem();
            ItemMeta meta = stack.getItemMeta();

            meta.setDisplayName(Message.translate("<white>" + reason));
            stack.setItemMeta(meta);

            reasons.add(ClickableItem.of(stack, e -> GrantConfirmInventory.getInventory(hunter, rank, scope, duration, reason).open(player)));
        });

        pagination.setItemsPerPage(9);
        pagination.setItems(reasons.toArray(new ClickableItem[0]));
        pagination.addToIterator(contents.newIterator(SlotIterator.Type.HORIZONTAL, 1, 0));

        List<String> infoLore = Arrays.asList(
                "",
                "<white>Selected Rank: <aqua>" + rank.getDisplayColored(),
                "<white>Selected Scope: <aqua>" + scope,
                "<white>Selected Duration: <aqua>" + duration,
                "");
        infoLore.replaceAll(Message::translate);

        MeleeInventoryManager.addButton(
                contents,
                XMaterial.PAPER,
                "<aqua>Reporting",
                infoLore,
                0,
                4,
                () -> {}
        );

        if (!pagination.isFirst()) {
            MeleeInventoryManager.addLeftButton(
                    contents,
                    () -> getInventory(hunter, rank, scope, duration).open(player, pagination.previous().getPage())
            );
        }

        if (!pagination.isLast()) {
            MeleeInventoryManager.addRightButton(
                    contents,
                    () -> getInventory(hunter, rank, scope, duration).open(player, pagination.next().getPage())
            );
        }
    }

    @Override
    public void update(Player player, InventoryContents contents) {

    }

}
