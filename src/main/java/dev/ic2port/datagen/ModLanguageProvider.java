package dev.ic2port.datagen;

import dev.ic2port.Reference;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.LanguageProvider;

/**
 * Generates language files for the specified locale.
 * <p>
 * Supported locales: {@code en_us}, {@code ru_ru}.
 */
public class ModLanguageProvider extends LanguageProvider {

    private final String locale;

    public ModLanguageProvider(final PackOutput output, final String locale) {
        super(output, Reference.MOD_ID, locale);
        this.locale = locale;
    }

    @Override
    protected void addTranslations() {
        switch (locale) {
            case "en_us" -> addEnglishTranslations();
            case "ru_ru" -> addRussianTranslations();
            default -> throw new IllegalArgumentException("Unsupported locale: " + locale);
        }
    }

    private void addEnglishTranslations() {
        add("itemGroup." + Reference.MOD_ID, "IC2 Port");
    }

    private void addRussianTranslations() {
        add("itemGroup." + Reference.MOD_ID, "IC2 Port");
    }
}
