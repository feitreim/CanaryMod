package net.canarymod.api.inventory;

import net.canarymod.Canary;
import net.canarymod.MathHelp;
import net.canarymod.api.nbt.CanaryCompoundTag;
import net.canarymod.api.nbt.CompoundTag;
import net.canarymod.api.packet.CanaryPacket;
import net.canarymod.api.world.CanaryWorld;
import net.canarymod.api.world.CanaryWorldManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S34PacketMaps;
import net.minecraft.world.World;

/**
 * MapData wrapper
 *
 * @author Jason Jones (darkdiplomat)
 */
public class CanaryMapData implements MapData {
    private final net.minecraft.world.storage.MapData nmsMapData;

    public CanaryMapData(net.minecraft.world.storage.MapData nmsMapData) {
        this.nmsMapData = nmsMapData;
    }

    @Override
    public String getMapName() {
        return getNative().a;
    }

    @Override
    public int getXCenter() {
        return getNative().b;
    }

    @Override
    public int getZCenter() {
        return getNative().c;
    }

    @Override
    public void setXCenter(int xCenter) {
        getNative().b = xCenter;
    }

    @Override
    public void setZCenter(int zCenter) {
        getNative().c = zCenter;
    }

    @Override
    public byte getScale() {
        return getNative().e;
    }

    @Override
    public void setScale(byte scale) {
        getNative().e = (byte)MathHelp.setInRange(scale, 1, 4);
    }

    @Override
    public byte[] getColors() {
        return getNative().f;
    }

    @Override
    public void setColors(byte[] bytes) {
        if (bytes.length != 16384) {
            return;
        }
        getNative().f = bytes;
        getNative().mapUpdating = false; // Probably don't want the new colors overridden do we
    }

    @Override
    public void setMapUpdating(boolean updating) {
        getNative().mapUpdating = updating;
    }

    @Override
    public boolean isMapUpdating() {
        return getNative().mapUpdating;
    }

    @Override
    public void update() {
        if (!getNative().isBroken && !getNative().i.isEmpty()) {
            EntityPlayer entityPlayer = (EntityPlayer)getNative().i.keySet().iterator().next(); // grab the first player

            if (entityPlayer != null) {
                Packet data = getNative().a(((CanaryItem)entityPlayer.getCanaryHuman().getItemHeld()).getHandle(), entityPlayer.o, entityPlayer);
                if (data != null) {
                    Canary.getServer().getConfigurationManager().sendPacketToAllInWorld(entityPlayer.getCanaryWorld().getFqName(), new CanaryPacket(data));
                }
            }
        }
    }

    @Override
    public void setColumnDirty(int x, int yLower, int yHigher) {
        if (MathHelp.isInRange(x, 0, 127) && MathHelp.isInRange(yLower, 0, 127) && MathHelp.isInRange(yHigher, 0, 127) && yLower <= yHigher) {
            getNative().a(x, yLower, yHigher);
        }
    }

    @Override
    public void setNBTData(CompoundTag compoundTag) {
        getNative().a(((CanaryCompoundTag)compoundTag).getHandle());
    }

    @Override
    public void getNBTData(CompoundTag compoundTag) {
        getNative().b(((CanaryCompoundTag)compoundTag).getHandle());
    }

    public net.minecraft.world.storage.MapData getNative() {
        return nmsMapData;
    }

    /* Putting this all here so i don't run over it during an update */
    private static String testItemStackForMapWorld(ItemStack itemStack, World defaultTo) {
        CanaryItem cItem = itemStack.getCanaryItem();
        if (cItem.getMetaTag().containsKey("mapWorldName")) {
            return cItem.getMetaTag().getString("mapWorldName");
        }
        cItem.getMetaTag().put("mapWorldName", defaultTo.getCanaryWorld().getFqName());
        return null;
    }

    public static World getMapWorld(ItemStack itemStack, World defaultTo) {
        String worldName = testItemStackForMapWorld(itemStack, defaultTo);
        if (worldName != null) {
            CanaryWorldManager cwm = (CanaryWorldManager)Canary.getServer().getWorldManager();
            if (cwm.worldExists(worldName) && cwm.worldIsLoaded(worldName)) {
                return ((CanaryWorld)cwm.getWorld(worldName, false)).getHandle();
            }
            return null;
        }
        return defaultTo;
    }
    //
}
