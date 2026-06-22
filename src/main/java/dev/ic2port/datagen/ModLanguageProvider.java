package dev.ic2port.datagen;

import dev.ic2port.Reference;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.LanguageProvider;

/**
 * Optional language datagen provider (not registered by default).
 * <p>
 * Full translations live in {@code src/main/resources/assets/ic2port/lang/}
 * ({@code en_us.json}, {@code ru_ru.json}). Registering this provider in
 * {@link ModDataGenerators} would write stub locale files into
 * {@code src/generated/resources/}, which Gradle merges after main resources and
 * would replace most in-game strings with only the creative-tab label.
 * <p>
 * To migrate to datagen-driven lang later, move all keys into
 * {@link #addEnglishTranslations()} / {@link #addRussianTranslations()} and
 * re-enable registration in {@link ModDataGenerators}.
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
