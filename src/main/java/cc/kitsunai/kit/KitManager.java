package cc.kitsunai.kit;

import cc.kitsunai.kit.api.Kit;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public final class KitManager {
    private final List<Class<?>> loaded;
    private final Map<String, Kit> kits;
    private final File kitsDir;
    private final KitDataBase kitDataBase;

    public KitManager() {
        this.kits = new HashMap<>();
        this.kitDataBase = new KitDataBase();
        kitsDir = new File(KitsunaiKit.getInstance().getDataFolder(), "kits");
        loaded = new ArrayList<>();
        if (!kitsDir.exists()) kitsDir.mkdirs();
    }

    public void loadAllKits() {
        File[] jarFiles = kitsDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".jar"));
        if (jarFiles == null) return;
        for (File jarFile : jarFiles) {
            try (JarFile jar = new JarFile(jarFile)) {
                JarEntry jarEntry = jar.getJarEntry("kit.properties");
                if (jarEntry == null) continue;
                Properties properties = new Properties();
                try (InputStream is = jar.getInputStream(jarEntry)) {
                    properties.load(is);
                }
                String mainClass = properties.getProperty("main-class");
                if (mainClass == null) {
                    KitsunaiKit.getInstance().getLogger().severe("kit.properties 缺少 main-class: " + jarFile.getName());
                    continue;
                }
                URL jarUrl = jarFile.toURI().toURL();
                try (URLClassLoader loader = new URLClassLoader(new URL[]{jarUrl}, KitsunaiKit.getInstance().getClass().getClassLoader())) {
                    Class<?> giftClass = loader.loadClass(mainClass);
                    if (Kit.class.isAssignableFrom(giftClass)) {
                        loaded.add(giftClass);
                        KitsunaiKit.getInstance().getLogger().info("成功加载礼包: " + mainClass);
                    }
                } catch (ClassNotFoundException e) {
                    KitsunaiKit.getInstance().getLogger().severe("加载礼包失败: " + jarFile.getName() + ", " + e);
                }
            } catch (IOException e) {
                KitsunaiKit.getInstance().getLogger().severe("加载礼包失败: " + jarFile.getName() + ", " + e);
            }
        }
        instantiateAllKits();
    }

    private void instantiateAllKits() {
        for (Class<?> clazz : loaded) {
            try {
                Kit kit = (Kit) clazz.getDeclaredConstructor().newInstance();
                kits.put(kit.getCdKey(), kit);
                kitDataBase.createGiftTable(kit.getDataBaseName().toLowerCase());
            } catch (Exception e) {
                KitsunaiKit.getInstance().getLogger().severe("实例化礼包失败: " + clazz.getName() + ", " + e);
            }
        }
    }

    public void reloadAllKits() {
        loaded.clear();
        kits.clear();
        loadAllKits();
    }

    public void newComerKit(Player player) {
        for (Map.Entry<String, Kit> entry : kits.entrySet()) {
            if (entry.getValue().isFirstJoin()) {
                Collection<ItemStack> itemStacks = new ArrayList<>();
                for (ItemStack itemStack : entry.getValue().getItems()) {
                    itemStacks.add(itemStack.clone());
                }
                player.give(itemStacks, true);
                kitDataBase.recordRedemption(entry.getValue().getDataBaseName(), player.getUniqueId());
            }
        }
    }

    public void getKit(Player player, String cdKey) {
        if (!kits.containsKey(cdKey)) {
            player.sendMessage(MiniMessage.miniMessage().deserialize("<red>CdKey不存在！</red>"));
            return;
        }
        Kit kit = kits.get(cdKey);
        if (kit.isFirstJoin()) {
            player.sendMessage(MiniMessage.miniMessage().deserialize("<red>新手礼包不能手动领取</red>"));
            return;
        }
        if (kitDataBase.getRedemptionCount(kit.getDataBaseName(), player.getUniqueId()) < kit.getMaximumCollect()) {
            Collection<ItemStack> itemStacks = new ArrayList<>();
            for (ItemStack itemStack : kit.getItems()) {
                itemStacks.add(itemStack.clone());
            }
            player.give(itemStacks, true);
            Component component = MiniMessage.miniMessage().deserialize("<green>成功领取礼包：</green>");
            player.sendMessage(component.append(kit.getName()));
            kitDataBase.recordRedemption(kit.getDataBaseName(), player.getUniqueId());
        }
    }
}
