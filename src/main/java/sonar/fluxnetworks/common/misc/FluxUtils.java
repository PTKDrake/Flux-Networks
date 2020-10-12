package sonar.fluxnetworks.common.misc;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import sonar.fluxnetworks.api.misc.EnergyType;
import sonar.fluxnetworks.api.misc.FluxConfigurationType;
import sonar.fluxnetworks.api.network.FluxDeviceType;
import sonar.fluxnetworks.api.text.FluxTranslate;
import sonar.fluxnetworks.client.gui.button.FluxTextWidget;
import sonar.fluxnetworks.client.gui.button.SlidedSwitchButton;
import sonar.fluxnetworks.common.item.ItemFluxDevice;
import sonar.fluxnetworks.common.tileentity.TileFluxDevice;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.text.NumberFormat;
import java.util.Collection;
import java.util.Objects;

public class FluxUtils {

    public static final String FLUX_DATA = "FluxData";
    public static final String GUI_COLOR = "GuiColor";
    public static final String CONFIGS_TAG = "Configs";

    public static <E extends Enum<?>> E incrementEnum(E enumObj, E[] values) {
        int ordinal = enumObj.ordinal() + 1;
        if (ordinal < values.length) {
            return values[ordinal];
        } else {
            return values[0];
        }
    }

    @Nullable
    public static Direction getBlockDirection(BlockPos pos, BlockPos other) {
        for (Direction face : Direction.values()) {
            if (pos.offset(face).equals(other))
                return face;
        }
        return null;
    }

    public static String getTransferInfo(FluxDeviceType type, EnergyType energyType, long change) {
        if (type.isPlug()) {
            String b = FluxUtils.format(change, FluxUtils.TypeNumberFormat.COMMAS, energyType, true);
            if (change == 0) {
                return FluxTranslate.INPUT.t() + ": " + TextFormatting.GOLD + b;
            } else {
                return FluxTranslate.INPUT.t() + ": " + TextFormatting.GREEN + "+" + b;
            }
        }
        if (type.isPoint() || type.isController()) {
            String b = FluxUtils.format(change, FluxUtils.TypeNumberFormat.COMMAS, energyType, true);
            if (change == 0) {
                return FluxTranslate.OUTPUT.t() + ": " + TextFormatting.GOLD + b;
            } else {
                return FluxTranslate.OUTPUT.t() + ": " + TextFormatting.RED + b;
            }
        }
        if (type.isStorage()) {
            if (change == 0) {
                return FluxTranslate.CHANGE.t() + ": " + TextFormatting.GOLD + change + energyType.getUsageSuffix();
            } else if (change > 0) {
                return FluxTranslate.CHANGE.t() + ": " + TextFormatting.GREEN + "+" + FluxUtils.format(change, FluxUtils.TypeNumberFormat.COMMAS, energyType, true);
            } else {
                return FluxTranslate.CHANGE.t() + ": " + TextFormatting.RED + FluxUtils.format(change, FluxUtils.TypeNumberFormat.COMMAS, energyType, true);
            }
        }
        return "";
    }

    /*public static int getPlayerXP(EntityPlayer player) {
        return (int) (getExperienceForLevel(player.experienceLevel) + (player.experience * player.xpBarCap()));
    }

    public static void addPlayerXP(EntityPlayer player, int amount) {
        int experience = getPlayerXP(player) + amount;
        player.experienceTotal = experience;
        player.experienceLevel = getLevelForExperience(experience);
        int expForLevel = getExperienceForLevel(player.experienceLevel);
        player.experience = (float) (experience - expForLevel) / (float) player.xpBarCap();
    }

    public static boolean removePlayerXP(EntityPlayer player, int amount) {
        if(getPlayerXP(player) >= amount) {
            addPlayerXP(player, -amount);
            return true;
        }
        return false;
    }

    public static int xpBarCap(int level) {
        if (level >= 30)
            return 112 + (level - 30) * 9;

        if (level >= 15)
            return 37 + (level - 15) * 5;

        return 7 + level * 2;
    }

    private static int sum(int n, int a0, int d) {
        return n * (2 * a0 + (n - 1) * d) / 2;
    }

    public static int getExperienceForLevel(int level) {
        if (level == 0) return 0;
        if (level <= 15) return sum(level, 7, 2);
        if (level <= 30) return 315 + sum(level - 15, 37, 5);
        return 1395 + sum(level - 30, 112, 9);
    }

    public static int getLevelForExperience(int targetXp) {
        int level = 0;
        while (true) {
            final int xpToNextLevel = xpBarCap(level);
            if (targetXp < xpToNextLevel) return level;
            level++;
            targetXp -= xpToNextLevel;
        }
    }*/

    @Nonnull
    public static GlobalPos getGlobalPos(@Nonnull TileEntity tileEntity) {
        return GlobalPos.getPosition(Objects.requireNonNull(tileEntity.getWorld()).getDimensionKey(), tileEntity.getPos());
    }

    public static void writeGlobalPos(@Nonnull CompoundNBT nbt, @Nonnull GlobalPos pos) {
        BlockPos p = pos.getPos();
        nbt.putInt("x", p.getX());
        nbt.putInt("y", p.getY());
        nbt.putInt("z", p.getZ());
        nbt.putString("dimension", pos.getDimension().getLocation().toString());
    }

    @Nonnull
    public static GlobalPos readGlobalPos(@Nonnull CompoundNBT nbt) {
        return GlobalPos.getPosition(RegistryKey.getOrCreateKey(Registry.WORLD_KEY, new ResourceLocation(nbt.getString("dimension"))),
                new BlockPos(nbt.getInt("x"), nbt.getInt("y"), nbt.getInt("z")));
    }

    @Nonnull
    public static String getDisplayString(@Nonnull GlobalPos pos) {
        BlockPos p = pos.getPos();
        return "X: " + p.getX() + " Y: " + p.getY() + " Z: " + p.getZ() + " Dim: " + pos.getDimension().getLocation();
    }

    public static <T> boolean addWithCheck(@Nonnull Collection<T> list, @Nullable T toAdd) {
        if (toAdd != null && !list.contains(toAdd)) {
            list.add(toAdd);
            return true;
        }
        return false;
    }

    @Nonnull
    public static ItemStack createItemStackFromBlock(@Nonnull World world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        return new ItemStack(state.getBlock().asItem());
    }

    /*public static boolean addConnection(@Nonnull IFluxDevice fluxDevice) {
        if (fluxDevice.getNetworkID() != -1) {
            IFluxNetwork network = FluxNetworkCache.INSTANCE.getNetwork(fluxDevice.getNetworkID());
            if (network.isValid()) {
                if (fluxDevice.getDeviceType().isController() && network.getConnections(FluxLogicType.CONTROLLER).size() > 0) {
                    return false;
                }
                network.enqueueConnectionAddition(fluxDevice);
                return true;
            }
        }
        return false;
    }

    public static void removeConnection(@Nonnull IFluxDevice fluxDevice, boolean isChunkUnload) {
        if (fluxDevice.getNetworkID() != -1) {
            IFluxNetwork network = FluxNetworkCache.INSTANCE.getNetwork(fluxDevice.getNetworkID());
            if (network.isValid()) {
                network.enqueueConnectionRemoval(fluxDevice, isChunkUnload);
            }
        }
    }*/

    public static int getIntFromColor(int red, int green, int blue) {
        red = red << 16 & 0x00FF0000;
        green = green << 8 & 0x0000FF00;
        blue = blue & 0x000000FF;

        return 0xFF000000 | red | green | blue;
    }

    public static int getBrighterColor(int color, float factor) {
        int red = (color >> 16) & 0x000000FF;
        int green = (color >> 8) & 0x000000FF;
        int blue = (color) & 0x000000FF;
        return getIntFromColor((int) Math.min(red * factor, 255), (int) Math.min(green * factor, 255), (int) Math.min(blue * factor, 255));
    }

    public enum TypeNumberFormat {
        FULL,                   // Full format
        COMPACT,                // Compact format (like 3.5M)
        COMMAS,                 // Language dependent comma separated format
        NONE                    // No output (empty string)
    }

    public static String format(long in, TypeNumberFormat style, String suffix) {
        switch (style) {
            case FULL:
                return in + suffix;
            case COMPACT: {
                int unit = 1000;
                if (in < unit) {
                    return in + " " + suffix;
                }
                int exp = (int) (Math.log(in) / Math.log(unit));
                char pre;
                if (suffix.startsWith("m")) {
                    suffix = suffix.substring(1);
                    if (exp - 2 >= 0) {
                        pre = "kMGTPE".charAt(exp - 2);
                        return String.format("%.1f%s", in / Math.pow(unit, exp), pre) + suffix;
                    } else {
                        return String.format("%.1f%s", in / Math.pow(unit, exp), suffix);
                    }
                } else {
                    pre = "kMGTPE".charAt(exp - 1);
                    return String.format("%.1f%s", in / Math.pow(unit, exp), pre) + suffix;
                }
            }
            case COMMAS:
                return NumberFormat.getInstance().format(in) + suffix;
            case NONE:
                return suffix;
        }
        return Long.toString(in);
    }

    public static String format(long in, TypeNumberFormat style, EnergyType energy, boolean usage) {
        if (energy == EnergyType.EU) {
            return format(in / 4, style, usage ? energy.getUsageSuffix() : energy.getStorageSuffix());
        }
        return format(in, style, usage ? energy.getUsageSuffix() : energy.getStorageSuffix());
    }

    public static boolean checkPassword(String str) {
        for (int i = 0; i < str.length(); i++) {
            if (!Character.isLetterOrDigit(str.charAt(i)))
                return false;
        }
        return true;
    }

    @Nullable
    public static <T> T getCap(@Nonnull PlayerEntity player, Capability<T> capability) {
        return getCap(player.getCapability(capability));
    }

    @Nullable
    public static <T> T getCap(@Nonnull LazyOptional<T> lazyOptional) {
        if (lazyOptional.isPresent()) {
            return lazyOptional.orElseThrow(IllegalStateException::new);
        }
        return null;
    }

    public static CompoundNBT copyConfiguration(TileFluxDevice flux, CompoundNBT config) {
        for (FluxConfigurationType type : FluxConfigurationType.VALUES) {
            type.copy.copyFromTile(config, type.getNBTName(), flux);
        }
        return config;
    }

    public static void pasteConfiguration(TileFluxDevice flux, CompoundNBT config) {
        for (FluxConfigurationType type : FluxConfigurationType.VALUES) {
            if (config.contains(type.getNBTName())) {
                type.paste.pasteToTile(config, type.getNBTName(), flux);
            }
        }
    }

    public static CompoundNBT getBatchEditingTag(FluxTextWidget a, FluxTextWidget b, FluxTextWidget c, SlidedSwitchButton d, SlidedSwitchButton e, SlidedSwitchButton f) {
        CompoundNBT tag = new CompoundNBT();
        tag.putString(ItemFluxDevice.CUSTOM_NAME, a.getText());
        tag.putInt(ItemFluxDevice.PRIORITY, b.getIntegerFromText(false));
        tag.putLong(ItemFluxDevice.LIMIT, c.getLongFromText(true));
        tag.putBoolean(ItemFluxDevice.SURGE_MODE, d != null && d.slideControl);
        tag.putBoolean(ItemFluxDevice.DISABLE_LIMIT, e != null && e.slideControl);
        tag.putBoolean("chunkLoad", f != null && f.slideControl);
        return tag;
    }

}