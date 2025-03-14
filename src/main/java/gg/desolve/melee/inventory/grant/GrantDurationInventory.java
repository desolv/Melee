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

public class GrantDurationInventory implements InventoryProvider {

    private final Profile profile;
    private final Rank rank;

    public GrantDurationInventory(Profile profile, Rank rank) {
        this.profile = profile;
        this.rank = rank;
    }

    public static SmartInventory getInventory(Profile profile, Rank rank) {
        return SmartInventory.builder()
                .id("grantDurationInventory")
                .provider(new GrantDurationInventory(profile, rank))
                .size(3, 9)
                .title(Message.translate("<dark_gray>Duration for " + profile.getUsernameColored() + "<dark_gray>..."))
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
        List<ClickableItem> durations = new ArrayList<>();

        Arrays.asList(Melee.getInstance().getLanguageConfig().getString("server.timing")
                .split("\\|"))
                .forEach(duration -> {
                    XMaterial material = XMaterial.matchXMaterial(Material.getRandomWoolColor() + "_WOOL")
                            .orElse(XMaterial.WHITE_WOOL);

                    ItemStack stack = material.parseItem();
                    ItemMeta meta = stack.getItemMeta();

                    meta.setDisplayName(Message.translate("<white>" + duration));
                    stack.setItemMeta(meta);
                    durations.add(ClickableItem.of(stack, r -> GrantScopeInventory.getInventory(profile, rank, duration).open(player)));
                });

        pagination.setItemsPerPage(18);
        pagination.setItems(durations.toArray(new ClickableItem[0]));
        pagination.addToIterator(contents.newIterator(SlotIterator.Type.HORIZONTAL, 1, 0));

        ItemStack durationStack = XMaterial.MAP.parseItem();
        ItemMeta durationMeta = durationStack.getItemMeta();
        durationMeta.setDisplayName(Message.translate("<green><bold>Custom Duration"));
        durationMeta.setLore(Stream.of(
                "<gray>Input a custom duration for this",
                "<gray>grant"
        ).map(Message::translate).toList());
        durationStack.setItemMeta(durationMeta);
        contents.set(0, 3, ClickableItem.of(durationStack, r -> {
            Profile executer = Melee.getInstance().getProfileManager().retrieve(player.getUniqueId());
            executer.setGrantProcess(player.getUniqueId(), profile.getUuid(), rank, "", "", "duration");
        }));

        ItemStack permanentStack = XMaterial.COMPASS.parseItem();
        ItemMeta permanentMeta = permanentStack.getItemMeta();
        permanentMeta.setDisplayName(Message.translate("<red><bold>Permanent"));
        permanentMeta.setLore(Stream.of(
                "<gray>Use this to apply a rank",
                "<gray>permanently"
        ).map(Message::translate).toList());
        permanentStack.setItemMeta(permanentMeta);
        contents.set(0, 5, ClickableItem.of(permanentStack, r -> GrantScopeInventory.getInventory(profile, rank, "permanent").open(player)));

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
                    e -> getInventory(profile, rank).open(player, pagination.previous().getPage())
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
                    e -> getInventory(profile, rank).open(player, pagination.previous().getPage())
            ));
        }
    }

    @Override
    public void update(Player player, InventoryContents contents) {}
}
