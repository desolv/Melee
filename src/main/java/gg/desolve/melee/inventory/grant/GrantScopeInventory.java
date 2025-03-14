package gg.desolve.melee.inventory.grant;

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
import gg.desolve.mithril.instance.Instance;
import gg.desolve.mithril.relevance.Converter;
import gg.desolve.mithril.relevance.Material;
import gg.desolve.mithril.relevance.Message;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class GrantScopeInventory implements InventoryProvider {

    private final Profile profile;
    private final Rank rank;
    private final String duration;

    public GrantScopeInventory(Profile profile, Rank rank, String duration) {
        this.profile = profile;
        this.rank = rank;
        this.duration = duration;
    }

    public static SmartInventory getInventory(Profile profile, Rank rank, String duration) {
        return SmartInventory.builder()
                .id("grantScopeInventory")
                .provider(new GrantScopeInventory(profile, rank, duration))
                .size(3, 9)
                .title(Message.translate("<dark_gray>Scope for " + profile.getUsernameColored() + "<dark_gray>..."))
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
        List<ClickableItem> instances = new ArrayList<>();

        Mithril.getInstance().getInstanceManager().retrieve().stream()
                .map(Instance::getName)
                .distinct()
                .forEach(instance -> {
                    XMaterial material = XMaterial.matchXMaterial(Material.getRandomWoolColor() + "_WOOL")
                            .orElse(XMaterial.WHITE_WOOL);

                    ItemStack stack = material.parseItem();
                    ItemMeta meta = stack.getItemMeta();

                    meta.setDisplayName(Message.translate("<white>" + instance));
                    stack.setItemMeta(meta);
                    instances.add(ClickableItem.of(stack, r -> GrantReasonInventory.getInventory(profile, rank, duration, instance).open(player)));
                });


        pagination.setItemsPerPage(18);
        pagination.setItems(instances.toArray(new ClickableItem[0]));
        pagination.addToIterator(contents.newIterator(SlotIterator.Type.HORIZONTAL, 1, 0));

        ItemStack scopeStack = XMaterial.MAP.parseItem();
        ItemMeta scopeMeta = scopeStack.getItemMeta();
        scopeMeta.setDisplayName(Message.translate("<green><bold>Custom Scope"));
        scopeMeta.setLore(Stream.of(
                "<gray>Input a custom scope for this",
                "<gray>grant"
        ).map(Message::translate).toList());
        scopeStack.setItemMeta(scopeMeta);
        contents.set(0, 3, ClickableItem.of(scopeStack, r -> {
            Profile executer = Melee.getInstance().getProfileManager().retrieve(player.getUniqueId());
            executer.setGrantProcess(player.getUniqueId(), profile.getUuid(), rank, duration, "","scope");
        }));

        ItemStack globalStack = XMaterial.COMPASS.parseItem();
        ItemMeta globalMeta = globalStack.getItemMeta();
        globalMeta.setDisplayName(Message.translate("<red><bold>Global"));
        globalMeta.setLore(Stream.of(
                "<gray>Use this to apply this rank",
                "<gray>globally"
        ).map(Message::translate).toList());
        globalStack.setItemMeta(globalMeta);
        contents.set(0, 5, ClickableItem.of(globalStack, r -> GrantReasonInventory.getInventory(profile, rank, duration, "global").open(player)));

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
                    e -> getInventory(profile, rank, duration).open(player, pagination.previous().getPage())
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
                    e -> getInventory(profile, rank, duration).open(player, pagination.previous().getPage())
            ));
        }
    }

    @Override
    public void update(Player player, InventoryContents contents) {}
}
