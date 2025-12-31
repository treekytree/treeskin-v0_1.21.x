package net.treekytree.treeskin;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class TreeSkinConfigScreen {

    private final Screen parent;

    public TreeSkinConfigScreen(Screen parent) {
        this.parent = parent;
    }

    public Screen build() {
        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Text.literal("TreeSkin Settings"));

        builder.setSavingRunnable(TreeskinConfig::save);
        ConfigEntryBuilder entry = builder.entryBuilder();

        // GENERAL SETTINGS
        ConfigCategory general = builder.getOrCreateCategory(Text.literal("General Settings"));

        general.addEntry(entry.startEnumSelector(
                        Text.literal("Skin Type"),
                        SkinType.class,
                        TreeskinConfig.getSkinType()
                ).setEnumNameProvider(type -> Text.literal(type.name()))
                .setDefaultValue(SkinType.CLASSIC)
                .setSaveConsumer(TreeskinConfig::setSkinType)
                .build());

        general.addEntry(entry.startEnumSelector(
                        Text.literal("Trigger Mode"),
                        TriggerMode.class,
                        TreeskinConfig.getTriggerMode()
                ).setEnumNameProvider(mode -> Text.literal(mode.name()))
                .setDefaultValue(TriggerMode.ENTER)
                .setSaveConsumer(TreeskinConfig::setTriggerMode)
                .build());

        general.addEntry(entry.startIntSlider(
                        Text.literal("Stillness Threshold (ms)"),
                        (int) TreeskinConfig.getStillThresholdMs(),
                        1000,
                        60000
                ).setDefaultValue(5000)
                .setSaveConsumer(TreeskinConfig::setStillThresholdMs)
                .build());

        general.addEntry(entry.startBooleanToggle(
                        Text.literal("Show Notifications"),
                        TreeskinConfig.getShowNotifications()
                ).setDefaultValue(true)
                .setSaveConsumer(TreeskinConfig::setShowNotifications)
                .build());

        // SKIN URLS
        ConfigCategory urls = builder.getOrCreateCategory(Text.literal("Skin URLs"));
        addUrlField(urls, entry, "nether");
        addUrlField(urls, entry, "end");
        addUrlField(urls, entry, "overworld_cold");
        addUrlField(urls, entry, "overworld_hot");
        addUrlField(urls, entry, "overworld_moderate");

        return builder.build();
    }

    private void addUrlField(ConfigCategory cat, ConfigEntryBuilder entry, String key) {
        cat.addEntry(entry.startStrField(
                        Text.literal(key),
                        TreeskinConfig.getSkinUrl(key)
                ).setDefaultValue("")
                .setSaveConsumer(value -> TreeskinConfig.setSkinUrl(key, value))
                .build());
    }
}