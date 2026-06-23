package dev.ic2port.api.events;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;
import org.jetbrains.annotations.Nullable;

/**
 * Fired before a scrap box roll resolves. Addons may replace the reward or cancel the open.
 */
@Cancelable
public class ScrapBoxEvent extends Event {

  private final Player player;
  private final ItemStack scrapBox;
  private ItemStack reward;

  public ScrapBoxEvent(final Player player, final ItemStack scrapBox, final ItemStack reward) {
    this.player = player;
    this.scrapBox = scrapBox;
    this.reward = reward;
  }

  public Player getPlayer() {
    return player;
  }

  public ItemStack getScrapBox() {
    return scrapBox;
  }

  public ItemStack getReward() {
    return reward;
  }

  public void setReward(final ItemStack reward) {
    this.reward = reward;
  }

  @Nullable
  public static ScrapBoxEvent onOpen(final Player player, final ItemStack scrapBox, final ItemStack reward) {
    ScrapBoxEvent event = new ScrapBoxEvent(player, scrapBox, reward);
    return net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(event) ? null : event;
  }
}
