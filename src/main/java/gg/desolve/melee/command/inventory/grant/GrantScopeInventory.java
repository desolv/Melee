package gg.desolve.melee.command.inventory.grant;

import com.cryptomorin.xseries.XMaterial;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.InventoryListener;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.*;
import gg.desolve.melee.Melee;
import gg.desolve.melee.command.inventory.MeleeInventoryManager;
import gg.desolve.melee.common.Material;
import gg.desolve.melee.common.Message;
import gg.desolve.melee.player.profile.Hunter;
import gg.desolve.melee.player.rank.Rank;
import gg.desolve.melee.server.MeleeServerManager;
import gg.desolve.melee.server.Scope;
import gg.desolve.melee.server.Server;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GrantScopeInventory implements InventoryProvider {

    private final Hunter hunter;
    private final Rank rank;

    public GrantScopeInventory(Hunter hunter, Rank rank) {
        this.hunter = hunter;
        this.rank = rank;
    }

    public static SmartInventory getInventory(Hunter hunter, Rank rank) {
        return SmartInventory.builder()
                .id("grantScopeInventory")
                .provider(new GrantScopeInventory(hunter, rank))
                .parent(GrantInventory.getInventory(hunter))
                .size(2, 9)
                .title(Message.translate("<dark_gray>Granting Scope " + hunter.getUsernameColored() + "<dark_gray>.."))
                .manager(Melee.getInstance().getInventoryManager())
                .build();
    }

    @Override
    public void init(Player player, InventoryContents contents) {
        Pagination pagination = contents.pagination();
        List<XMaterial> wools = new ArrayList<>(Material.getAestheticWools().values());
        List<ClickableItem> scopes = new ArrayList<>();
        Random random = new Random();

        Stream.concat(
                Stream.of("global"),
                MeleeServerManager.getServers()
                        .stream()
                        .map(Server::getName)
        ).collect(Collectors.toList()).forEach(scope -> {
            ItemStack stack = wools.get(random.nextInt(wools.size())).parseItem();
            ItemMeta meta = stack.getItemMeta();

            meta.setDisplayName(Message.translate("<white>" + scope));
            stack.setItemMeta(meta);

            scopes.add(ClickableItem.of(stack, e -> GrantDurationInventory.getInventory(hunter, rank, scope).open(player)));
        });

        pagination.setItemsPerPage(9);
        pagination.setItems(scopes.toArray(new ClickableItem[0]));
        pagination.addToIterator(contents.newIterator(SlotIterator.Type.HORIZONTAL, 1, 0));

        List<String> infoLore = Arrays.asList(
                "",
                "<white>Selected Rank: <aqua>" + rank.getDisplayColored(),
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
                    () -> getInventory(hunter, rank).open(player, pagination.previous().getPage())
            );
        }

        if (!pagination.isLast()) {
            MeleeInventoryManager.addRightButton(
                    contents,
                    () -> getInventory(hunter, rank).open(player, pagination.next().getPage())
            );
        }
    }

    @Override
    public void update(Player player, InventoryContents contents) {

    }

}
