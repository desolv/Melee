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
import gg.desolve.melee.server.MeleeServerManager;
import gg.desolve.melee.server.Scope;
import gg.desolve.melee.server.Server;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GrantDurationInventory implements InventoryProvider {

    private final Hunter hunter;
    private final Rank rank;
    private final String scope;


    public GrantDurationInventory(Hunter hunter, Rank rank, String scope) {
        this.hunter = hunter;
        this.rank = rank;
        this.scope = scope;
    }

    public static SmartInventory getInventory(Hunter hunter, Rank rank, String scope) {
        return SmartInventory.builder()
                .id("grantDurationInventory")
                .provider(new GrantDurationInventory(hunter, rank, scope))
                .parent(GrantInventory.getInventory(hunter))
                .size(2, 9)
                .title(Message.translate("<dark_gray>Granting Duration " + hunter.getUsernameColored() + "<dark_gray>.."))
                .manager(Melee.getInstance().getInventoryManager())
                .build();
    }

    @Override
    public void init(Player player, InventoryContents contents) {
        Pagination pagination = contents.pagination();
        List<XMaterial> wools = new ArrayList<>(Material.getAestheticWools().values());
        List<ClickableItem> durations = new ArrayList<>();
        Random random = new Random();

        ImmutableList.of("permanent", "10s", "5m", "1h", "2d", "1w", "1M", "1y").forEach(duration -> {
            ItemStack stack = wools.get(random.nextInt(wools.size())).parseItem();
            ItemMeta meta = stack.getItemMeta();

            meta.setDisplayName(Message.translate("<white>" + duration));
            stack.setItemMeta(meta);

            durations.add(ClickableItem.of(stack, e -> GrantReasonInventory.getInventory(hunter, rank, scope, duration).open(player)));
        });

        pagination.setItemsPerPage(9);
        pagination.setItems(durations.toArray(new ClickableItem[0]));
        pagination.addToIterator(contents.newIterator(SlotIterator.Type.HORIZONTAL, 1, 0));

        List<String> infoLore = Arrays.asList(
                "",
                "<white>Selected Rank: <aqua>" + rank.getDisplayColored(),
                "<white>Selected Scope: <aqua>" + scope,
                "");
        infoLore.replaceAll(Message::translate);

        Melee.getInstance().getInventoryManager().addButton(
                contents,
                XMaterial.PAPER,
                "<aqua>Reporting",
                infoLore,
                0,
                4,
                () -> {}
        );

        if (!pagination.isFirst()) {
            Melee.getInstance().getInventoryManager().addLeftButton(
                    contents,
                    () -> getInventory(hunter, rank, scope).open(player, pagination.previous().getPage())
            );
        }

        if (!pagination.isLast()) {
            Melee.getInstance().getInventoryManager().addRightButton(
                    contents,
                    () -> getInventory(hunter, rank, scope).open(player, pagination.next().getPage())
            );
        }
    }

    @Override
    public void update(Player player, InventoryContents contents) {

    }

}
