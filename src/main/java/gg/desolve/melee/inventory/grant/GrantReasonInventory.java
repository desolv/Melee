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
import gg.desolve.mithril.relevance.Material;
import gg.desolve.mithril.relevance.Message;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class GrantReasonInventory implements InventoryProvider {

    private final Profile profile;
    private final Rank rank;
    private final String duration;
    private final String scope;

    public GrantReasonInventory(Profile profile, Rank rank, String duration, String scope) {
        this.profile = profile;
        this.rank = rank;
        this.duration = duration;
        this.scope = scope;
    }

    public static SmartInventory getInventory(Profile profile, Rank rank, String duration, String scope) {
        return SmartInventory.builder()
                .id("grantReasonInventory")
                .provider(new GrantReasonInventory(profile, rank, duration, scope))
                .size(3, 9)
                .title(Message.translate("<dark_gray>Reason for " + profile.getUsernameColored() + "<dark_gray>..."))
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
        List<ClickableItem> reasons = new ArrayList<>();

        Arrays.asList(Melee.getInstance().getLanguageConfig().getString("server.reasons")
                .split("\\|"))
                .forEach(reason -> {
                    XMaterial material = XMaterial.matchXMaterial(Material.getRandomWoolColor() + "_WOOL")
                            .orElse(XMaterial.WHITE_WOOL);

                    ItemStack stack = material.parseItem();
                    ItemMeta meta = stack.getItemMeta();

                    meta.setDisplayName(Message.translate("<white>" + reason));
                    stack.setItemMeta(meta);
                    reasons.add(ClickableItem.of(stack, r -> GrantConfirmInventory.getInventory(profile, rank, duration, scope, reason).open(player)));
                });

        pagination.setItemsPerPage(18);
        pagination.setItems(reasons.toArray(new ClickableItem[0]));
        pagination.addToIterator(contents.newIterator(SlotIterator.Type.HORIZONTAL, 1, 0));

        ItemStack reasonStack = XMaterial.MAP.parseItem();
        ItemMeta reasonMeta = reasonStack.getItemMeta();
        reasonMeta.setDisplayName(Message.translate("<green><bold>Custom Reason"));
        reasonMeta.setLore(Stream.of(
                "<gray>Input a custom reason for this",
                "<gray>grant"
        ).map(Message::translate).toList());
        reasonStack.setItemMeta(reasonMeta);
        contents.set(0, 3, ClickableItem.of(reasonStack, r -> profile.setGrantProcess(rank, duration, scope, "","reason")));

        ItemStack otherStack = XMaterial.COMPASS.parseItem();
        ItemMeta otherMeta = otherStack.getItemMeta();
        otherMeta.setDisplayName(Message.translate("<red><bold>Other"));
        otherMeta.setLore(Stream.of(
                "<gray>Use this to apply this rank",
                "<gray>reason as Other"
        ).map(Message::translate).toList());
        otherStack.setItemMeta(otherMeta);
        contents.set(0, 5, ClickableItem.of(otherStack, r -> GrantConfirmInventory.getInventory(profile, rank, duration, scope, "Other").open(player)));

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
                    e -> getInventory(profile, rank, duration, scope).open(player, pagination.previous().getPage())
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
                    e -> getInventory(profile, rank, duration, scope).open(player, pagination.previous().getPage())
            ));
        }
    }

    @Override
    public void update(Player player, InventoryContents contents) {}
}
