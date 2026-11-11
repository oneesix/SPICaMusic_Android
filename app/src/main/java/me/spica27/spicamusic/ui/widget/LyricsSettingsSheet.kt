package me.spica27.spicamusic.ui.widget

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import me.spica27.spicamusic.R
import me.spica27.spicamusic.core.preferences.LineEffect
import me.spica27.spicamusic.core.preferences.LyricsStyleSettings
import me.spica27.spicamusic.core.preferences.TransliterationPosition

/**
 * Settings sheet of the lyrics window (opened from the lyrics tool button).
 *
 * Sections: Font, Text size, Transliteration, Lyrics blur and opacity, Jiggle.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LyricsSettingsSheet(
    settings: LyricsStyleSettings,
    onUpdate: ((LyricsStyleSettings) -> LyricsStyleSettings) -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp)
                    .navigationBarsPadding()
                    .padding(bottom = 24.dp),
        ) {
            FontSection(settings = settings, onUpdate = onUpdate)
            TextSizeSection(settings = settings, onUpdate = onUpdate)
            TransliterationSection(settings = settings, onUpdate = onUpdate)
            BlurAndOpacitySection(settings = settings, onUpdate = onUpdate)
            LineEffectSection(settings = settings, onUpdate = onUpdate)
            if (settings.lineEffect == LineEffect.Jiggle) {
                JiggleSection(settings = settings, onUpdate = onUpdate)
            }
            LightSweepSection(settings = settings, onUpdate = onUpdate)
            GradientLightSection(settings = settings, onUpdate = onUpdate)
            DynamicColorSection(settings = settings, onUpdate = onUpdate)
            LyricsProgressSection(settings = settings, onUpdate = onUpdate)
            LineAnimationSection(settings = settings, onUpdate = onUpdate)
            EnhanceHeaderSection(settings = settings, onUpdate = onUpdate)
        }
    }
}

@Composable
private fun FontSection(
    settings: LyricsStyleSettings,
    onUpdate: ((LyricsStyleSettings) -> LyricsStyleSettings) -> Unit,
) {
    LyricsSettingsSection(stringResource(R.string.lyr_set_section_font))

    LyricsSettingsChips(
        label = stringResource(R.string.lyr_set_font_style),
        description = stringResource(R.string.lyr_set_font_style_desc),
        options = LyricsFontOption.entries,
        selected = LyricsFontOption.fromId(settings.fontStyle),
        optionLabel = { stringResource(it.labelRes) },
        optionFont = { it.family },
        onSelect = { option -> onUpdate { it.copy(fontStyle = option.id) } },
    )

    // "Same as player" + every font
    val lyricsFontOptions = listOf<String>(LyricsFontOption.SAME_AS_PLAYER) + LyricsFontOption.entries.map { it.id }
    LyricsSettingsChips(
        label = stringResource(R.string.lyr_set_lyrics_font),
        description = stringResource(R.string.lyr_set_lyrics_font_desc),
        options = lyricsFontOptions,
        selected = settings.lyricsFont,
        optionLabel = { id ->
            if (id == LyricsFontOption.SAME_AS_PLAYER) {
                stringResource(R.string.lyr_set_font_same)
            } else {
                stringResource(LyricsFontOption.fromId(id).labelRes)
            }
        },
        optionFont = { id -> if (id == LyricsFontOption.SAME_AS_PLAYER) null else LyricsFontOption.fromId(id).family },
        onSelect = { id -> onUpdate { it.copy(lyricsFont = id) } },
    )

    val weightLabels =
        mapOf(
            400 to stringResource(R.string.lyr_set_weight_regular),
            500 to stringResource(R.string.lyr_set_weight_medium),
            600 to stringResource(R.string.lyr_set_weight_semibold),
            700 to stringResource(R.string.lyr_set_weight_bold),
            800 to stringResource(R.string.lyr_set_weight_extra_bold),
        )
    LyricsSettingsSlider(
        label = stringResource(R.string.lyr_set_weight),
        description = stringResource(R.string.lyr_set_weight_desc),
        value = settings.lyricsWeight,
        range = LyricsStyleSettings.WEIGHT_RANGE,
        step = 100,
        valueText = { weightLabels[it] ?: it.toString() },
        onValueChange = { v -> onUpdate { it.copy(lyricsWeight = v) } },
    )

    LyricsSettingsResetButton(
        text = stringResource(R.string.lyr_set_reset_fonts),
        onClick = {
            onUpdate {
                it.copy(
                    fontStyle = LyricsStyleSettings().fontStyle,
                    lyricsFont = LyricsStyleSettings().lyricsFont,
                    lyricsWeight = LyricsStyleSettings.DEFAULT_WEIGHT,
                )
            }
        },
    )
}

@Composable
private fun TextSizeSection(
    settings: LyricsStyleSettings,
    onUpdate: ((LyricsStyleSettings) -> LyricsStyleSettings) -> Unit,
) {
    LyricsSettingsSection(stringResource(R.string.lyr_set_section_text_size))
    val percent: (Int) -> String = { "$it%" }

    LyricsSettingsSlider(
        label = stringResource(R.string.lyr_set_lyrics_size),
        description = stringResource(R.string.lyr_set_lyrics_size_desc),
        value = settings.lyricsSize,
        range = LyricsStyleSettings.LYRICS_SIZE_RANGE,
        step = 5,
        valueText = percent,
        onValueChange = { v -> onUpdate { it.copy(lyricsSize = v) } },
    )
    LyricsSettingsSlider(
        label = stringResource(R.string.lyr_set_transliteration_size),
        description = stringResource(R.string.lyr_set_transliteration_size_desc),
        value = settings.transliterationSize,
        range = LyricsStyleSettings.SECONDARY_SIZE_RANGE,
        step = 5,
        valueText = percent,
        onValueChange = { v -> onUpdate { it.copy(transliterationSize = v) } },
    )
    LyricsSettingsSlider(
        label = stringResource(R.string.lyr_set_translation_size),
        description = stringResource(R.string.lyr_set_translation_size_desc),
        value = settings.translationSize,
        range = LyricsStyleSettings.SECONDARY_SIZE_RANGE,
        step = 5,
        valueText = percent,
        onValueChange = { v -> onUpdate { it.copy(translationSize = v) } },
    )
    LyricsSettingsSlider(
        label = stringResource(R.string.lyr_set_bg_translation_size),
        description = stringResource(R.string.lyr_set_bg_translation_size_desc),
        value = settings.backgroundTranslationSize,
        range = LyricsStyleSettings.SECONDARY_SIZE_RANGE,
        step = 5,
        valueText = percent,
        onValueChange = { v -> onUpdate { it.copy(backgroundTranslationSize = v) } },
    )

    LyricsSettingsResetButton(
        text = stringResource(R.string.lyr_set_reset_text_size),
        onClick = {
            onUpdate {
                it.copy(
                    lyricsSize = 100,
                    transliterationSize = 100,
                    translationSize = 100,
                    backgroundTranslationSize = 100,
                )
            }
        },
    )
}

@Composable
private fun TransliterationSection(
    settings: LyricsStyleSettings,
    onUpdate: ((LyricsStyleSettings) -> LyricsStyleSettings) -> Unit,
) {
    LyricsSettingsSection(stringResource(R.string.lyr_set_section_transliteration))

    LyricsSettingsToggle(
        label = stringResource(R.string.lyr_set_attach_to_words),
        description = stringResource(R.string.lyr_set_attach_to_words_desc),
        checked = settings.attachToWords,
        onCheckedChange = { v -> onUpdate { it.copy(attachToWords = v) } },
    )

    LyricsSettingsChips(
        label = stringResource(R.string.lyr_set_translit_position),
        description = stringResource(R.string.lyr_set_translit_position_desc),
        options = TransliterationPosition.entries,
        selected = settings.transliterationPosition,
        optionLabel = {
            stringResource(
                if (it == TransliterationPosition.Top) R.string.lyr_set_position_top else R.string.lyr_set_position_bottom,
            )
        },
        onSelect = { v -> onUpdate { it.copy(transliterationPosition = v) } },
    )
}

@Composable
private fun BlurAndOpacitySection(
    settings: LyricsStyleSettings,
    onUpdate: ((LyricsStyleSettings) -> LyricsStyleSettings) -> Unit,
) {
    LyricsSettingsSection(stringResource(R.string.lyr_set_section_blur_opacity))
    val off = stringResource(R.string.lyr_set_off)

    LyricsSettingsSlider(
        label = stringResource(R.string.lyr_set_unsung_opacity),
        description = stringResource(R.string.lyr_set_unsung_opacity_desc),
        value = settings.unsungOpacity,
        range = LyricsStyleSettings.UNSUNG_RANGE,
        step = 5,
        valueText = { "$it%" },
        onValueChange = { v -> onUpdate { it.copy(unsungOpacity = v) } },
    )
    LyricsSettingsSlider(
        label = stringResource(R.string.lyr_set_blur),
        description = stringResource(R.string.lyr_set_blur_desc),
        value = settings.blur,
        range = LyricsStyleSettings.BLUR_RANGE,
        step = 10,
        valueText = { if (it == 0) off else "$it%" },
        onValueChange = { v -> onUpdate { it.copy(blur = v) } },
    )
    LyricsSettingsSlider(
        label = stringResource(R.string.lyr_set_move_duration),
        description = stringResource(R.string.lyr_set_move_duration_desc),
        value = settings.moveDurationMs,
        range = LyricsStyleSettings.MOVE_RANGE,
        step = 50,
        valueText = { String.format(java.util.Locale.US, "%.2f s", it / 1000f) },
        onValueChange = { v -> onUpdate { it.copy(moveDurationMs = v) } },
    )
    LyricsSettingsSlider(
        label = stringResource(R.string.lyr_set_current_position),
        description = stringResource(R.string.lyr_set_current_position_desc),
        value = settings.currentLinePosition,
        range = LyricsStyleSettings.POSITION_RANGE,
        step = 5,
        valueText = { "$it%" },
        onValueChange = { v -> onUpdate { it.copy(currentLinePosition = v) } },
    )
}

@Composable
private fun LyricsProgressSection(
    settings: LyricsStyleSettings,
    onUpdate: ((LyricsStyleSettings) -> LyricsStyleSettings) -> Unit,
) {
    LyricsSettingsSection(stringResource(R.string.lyr_set_section_progress))

    LyricsSettingsToggle(
        label = stringResource(R.string.lyr_set_word_lift),
        description = stringResource(R.string.lyr_set_word_lift_desc),
        checked = settings.wordLiftEnabled,
        onCheckedChange = { v -> onUpdate { it.copy(wordLiftEnabled = v) } },
    )
    if (settings.wordLiftEnabled) {
        LyricsSettingsSlider(
            label = stringResource(R.string.lyr_set_word_lift_amount),
            description = stringResource(R.string.lyr_set_word_lift_amount_desc),
            value = settings.wordLift,
            range = LyricsStyleSettings.WORD_LIFT_RANGE,
            step = 5,
            valueText = { "${it / 10}.${it % 10}" },
            onValueChange = { v -> onUpdate { it.copy(wordLift = v) } },
        )
    }
    LyricsSettingsToggle(
        label = stringResource(R.string.lyr_set_merge_words),
        description = stringResource(R.string.lyr_set_merge_words_desc),
        checked = settings.mergeWords,
        onCheckedChange = { v -> onUpdate { it.copy(mergeWords = v) } },
    )
    LyricsSettingsToggle(
        label = stringResource(R.string.lyr_set_held_manual),
        description = stringResource(R.string.lyr_set_held_manual_desc),
        checked = settings.manualHeldWords,
        onCheckedChange = { v -> onUpdate { it.copy(manualHeldWords = v) } },
    )
    if (settings.manualHeldWords) {
        LyricsSettingsSlider(
            label = stringResource(R.string.lyr_set_held_min),
            description = stringResource(R.string.lyr_set_held_min_desc),
            value = settings.heldWordMinMs,
            range = LyricsStyleSettings.HELD_WORD_MIN_RANGE,
            step = 50,
            valueText = { "$it ms" },
            onValueChange = { v -> onUpdate { it.copy(heldWordMinMs = v) } },
        )
    }
    LyricsSettingsToggle(
        label = stringResource(R.string.lyr_set_held_optimize),
        description = stringResource(R.string.lyr_set_held_optimize_desc),
        checked = settings.optimizeHeldWords,
        onCheckedChange = { v -> onUpdate { it.copy(optimizeHeldWords = v) } },
    )
    if (settings.optimizeHeldWords) {
        LyricsSettingsToggle(
            label = stringResource(R.string.lyr_set_held_default_glow),
            description = stringResource(R.string.lyr_set_held_default_glow_desc),
            checked = settings.heldDefaultGlow,
            onCheckedChange = { v -> onUpdate { it.copy(heldDefaultGlow = v) } },
        )
    }
    LyricsSettingsToggle(
        label = stringResource(R.string.lyr_set_hold_scroll),
        description = stringResource(R.string.lyr_set_hold_scroll_desc),
        checked = settings.holdScrollEnabled,
        onCheckedChange = { v -> onUpdate { it.copy(holdScrollEnabled = v) } },
    )
    if (settings.holdScrollEnabled) {
        LyricsSettingsSlider(
            label = stringResource(R.string.lyr_set_hold_scroll_lines),
            description = stringResource(R.string.lyr_set_hold_scroll_lines_desc),
            value = settings.holdScrollLines,
            range = LyricsStyleSettings.HOLD_SCROLL_RANGE,
            step = 1,
            valueText = { "$it" },
            onValueChange = { v -> onUpdate { it.copy(holdScrollLines = v) } },
        )
    }
}

@Composable
private fun LightSweepSection(
    settings: LyricsStyleSettings,
    onUpdate: ((LyricsStyleSettings) -> LyricsStyleSettings) -> Unit,
) {
    LyricsSettingsSection(stringResource(R.string.lyr_set_section_light_sweep))

    LyricsSettingsToggle(
        label = stringResource(R.string.lyr_set_light_sweep),
        description = stringResource(R.string.lyr_set_light_sweep_desc),
        checked = settings.lightSweepEnabled,
        onCheckedChange = { v -> onUpdate { it.copy(lightSweepEnabled = v) } },
    )

    if (settings.lightSweepEnabled) {
        LyricsSettingsToggle(
            label = stringResource(R.string.lyr_set_sweep_continuous),
            description = stringResource(R.string.lyr_set_sweep_continuous_desc),
            checked = settings.lightSweepContinuous,
            onCheckedChange = { v -> onUpdate { it.copy(lightSweepContinuous = v) } },
        )
        LyricsSettingsToggle(
            label = stringResource(R.string.lyr_set_sweep_oklch),
            description = stringResource(R.string.lyr_set_sweep_oklch_desc),
            checked = settings.lightSweepOklch,
            onCheckedChange = { v -> onUpdate { it.copy(lightSweepOklch = v) } },
        )
        LyricsSettingsToggle(
            label = stringResource(R.string.lyr_set_sweep_ease),
            description = stringResource(R.string.lyr_set_sweep_ease_desc),
            checked = settings.lightSweepEase,
            onCheckedChange = { v -> onUpdate { it.copy(lightSweepEase = v) } },
        )
        LyricsSettingsToggle(
            label = stringResource(R.string.lyr_set_sweep_flash),
            description = stringResource(R.string.lyr_set_sweep_flash_desc),
            checked = settings.lightSweepFlash,
            onCheckedChange = { v -> onUpdate { it.copy(lightSweepFlash = v) } },
        )
        LyricsSettingsSlider(
            label = stringResource(R.string.lyr_set_sweep_softness),
            description = stringResource(R.string.lyr_set_sweep_softness_desc),
            value = settings.lightSweepSoftness,
            range = LyricsStyleSettings.LIGHT_SWEEP_SOFTNESS_RANGE,
            step = 5,
            valueText = { "$it%" },
            onValueChange = { v -> onUpdate { it.copy(lightSweepSoftness = v) } },
        )
    }
}

@Composable
private fun GradientLightSection(
    settings: LyricsStyleSettings,
    onUpdate: ((LyricsStyleSettings) -> LyricsStyleSettings) -> Unit,
) {
    LyricsSettingsSection(stringResource(R.string.lyr_set_section_fx))

    LyricsSettingsToggle(
        label = stringResource(R.string.lyr_set_gradient),
        description = stringResource(R.string.lyr_set_gradient_desc),
        checked = settings.gradientLyricsEnabled,
        onCheckedChange = { v -> onUpdate { it.copy(gradientLyricsEnabled = v) } },
    )
    LyricsSettingsToggle(
        label = stringResource(R.string.lyr_set_fx_light),
        description = stringResource(R.string.lyr_set_fx_light_desc),
        checked = settings.lyricsLightEnabled,
        onCheckedChange = { v -> onUpdate { it.copy(lyricsLightEnabled = v) } },
    )
    if (settings.lyricsLightEnabled) {
        LyricsSettingsSlider(
            label = stringResource(R.string.lyr_set_fx_intensity),
            description = stringResource(R.string.lyr_set_fx_intensity_desc),
            value = settings.lyricsLightIntensity,
            range = LyricsStyleSettings.LYRICS_LIGHT_RANGE,
            step = 5,
            valueText = { "$it%" },
            onValueChange = { v -> onUpdate { it.copy(lyricsLightIntensity = v) } },
        )
    }
}

@Composable
private fun DynamicColorSection(
    settings: LyricsStyleSettings,
    onUpdate: ((LyricsStyleSettings) -> LyricsStyleSettings) -> Unit,
) {
    LyricsSettingsSection(stringResource(R.string.lyr_set_section_dyn))

    LyricsSettingsToggle(
        label = stringResource(R.string.lyr_set_dyn),
        description = stringResource(R.string.lyr_set_dyn_desc),
        checked = settings.dynamicLyricsEnabled,
        onCheckedChange = { v -> onUpdate { it.copy(dynamicLyricsEnabled = v) } },
    )
    if (settings.dynamicLyricsEnabled) {
        LyricsSettingsToggle(
            label = stringResource(R.string.lyr_set_dyn_bg),
            description = stringResource(R.string.lyr_set_dyn_bg_desc),
            checked = settings.dynamicBackgroundEnabled,
            onCheckedChange = { v -> onUpdate { it.copy(dynamicBackgroundEnabled = v) } },
        )
        LyricsSettingsChips(
            label = stringResource(R.string.lyr_set_dyn_mode),
            description = stringResource(R.string.lyr_set_dyn_mode_desc),
            options = listOf("auto", "dark", "light"),
            selected = settings.dynamicLyricsMode,
            optionLabel = {
                when (it) {
                    "dark" -> stringResource(R.string.verse_mode_dark)
                    "light" -> stringResource(R.string.verse_mode_light)
                    else -> stringResource(R.string.verse_mode_auto)
                }
            },
            onSelect = { v -> onUpdate { it.copy(dynamicLyricsMode = v) } },
        )
        LyricsSettingsSlider(
            label = stringResource(R.string.lyr_set_dyn_opacity),
            description = stringResource(R.string.lyr_set_dyn_opacity_desc),
            value = settings.dynamicLyricsOpacity,
            range = LyricsStyleSettings.DYNAMIC_OPACITY_RANGE,
            step = 5,
            valueText = { "$it%" },
            onValueChange = { v -> onUpdate { it.copy(dynamicLyricsOpacity = v) } },
        )
        LyricsSettingsSlider(
            label = stringResource(R.string.lyr_set_dyn_saturation),
            description = stringResource(R.string.lyr_set_dyn_saturation_desc),
            value = settings.dynamicLyricsSaturation,
            range = LyricsStyleSettings.DYNAMIC_SATURATION_RANGE,
            step = 5,
            valueText = { "$it%" },
            onValueChange = { v -> onUpdate { it.copy(dynamicLyricsSaturation = v) } },
        )
        LyricsSettingsSlider(
            label = stringResource(R.string.lyr_set_dyn_contrast),
            description = stringResource(R.string.lyr_set_dyn_contrast_desc),
            value = settings.dynamicLyricsContrast,
            range = LyricsStyleSettings.DYNAMIC_CONTRAST_RANGE,
            step = 5,
            valueText = { "$it%" },
            onValueChange = { v -> onUpdate { it.copy(dynamicLyricsContrast = v) } },
        )
    }
}

private fun secondsText(ms: Int): String {
    val sign = if (ms < 0) "-" else ""
    val abs = kotlin.math.abs(ms)
    return "$sign${abs / 1000}.${(abs % 1000) / 100} s"
}

@Composable
private fun LineAnimationSection(
    settings: LyricsStyleSettings,
    onUpdate: ((LyricsStyleSettings) -> LyricsStyleSettings) -> Unit,
) {
    LyricsSettingsSection(stringResource(R.string.lyr_set_section_line_anim))

    LyricsSettingsToggle(
        label = stringResource(R.string.lyr_set_line_move),
        description = stringResource(R.string.lyr_set_line_move_desc),
        checked = settings.lineMoveEnabled,
        onCheckedChange = { v -> onUpdate { it.copy(lineMoveEnabled = v) } },
    )
    if (settings.lineMoveEnabled) {
        LyricsSettingsSlider(
            label = stringResource(R.string.lyr_set_line_move_duration),
            description = stringResource(R.string.lyr_set_line_move_duration_desc),
            value = settings.lineMoveMs,
            range = LyricsStyleSettings.LINE_MOVE_AFTER_RANGE,
            step = 100,
            valueText = ::secondsText,
            onValueChange = { v -> onUpdate { it.copy(lineMoveMs = v) } },
        )
    }
    LyricsSettingsToggle(
        label = stringResource(R.string.lyr_set_bg_reveal),
        description = stringResource(R.string.lyr_set_bg_reveal_desc),
        checked = settings.bgRevealEnabled,
        onCheckedChange = { v -> onUpdate { it.copy(bgRevealEnabled = v) } },
    )
    if (settings.bgRevealEnabled) {
        LyricsSettingsChips(
            label = stringResource(R.string.lyr_set_bg_reveal_style),
            description = stringResource(R.string.lyr_set_bg_reveal_style_desc),
            options = listOf(LyricsStyleSettings.BG_REVEAL_STYLE_CLASSIC, LyricsStyleSettings.BG_REVEAL_STYLE_FLIP),
            selected = settings.bgRevealStyle,
            optionLabel = { style ->
                stringResource(
                    if (style == LyricsStyleSettings.BG_REVEAL_STYLE_FLIP) {
                        R.string.lyr_set_bg_reveal_style_flip
                    } else {
                        R.string.lyr_set_bg_reveal_style_classic
                    },
                )
            },
            onSelect = { style -> onUpdate { it.copy(bgRevealStyle = style) } },
        )
        LyricsSettingsSlider(
            label = stringResource(R.string.lyr_set_bg_reveal_before),
            description = stringResource(R.string.lyr_set_bg_reveal_before_desc),
            value = settings.bgRevealStartOffsetMs,
            range = LyricsStyleSettings.BG_REVEAL_START_OFFSET_RANGE,
            step = 100,
            valueText = ::secondsText,
            onValueChange = { v -> onUpdate { it.copy(bgRevealStartOffsetMs = v) } },
        )
        LyricsSettingsSlider(
            label = stringResource(R.string.lyr_set_bg_reveal_after),
            description = stringResource(R.string.lyr_set_bg_reveal_after_desc),
            value = settings.bgRevealAfterMs,
            range = LyricsStyleSettings.BG_REVEAL_OFFSET_RANGE,
            step = 100,
            valueText = ::secondsText,
            onValueChange = { v -> onUpdate { it.copy(bgRevealAfterMs = v) } },
        )
        LyricsSettingsSlider(
            label = stringResource(R.string.lyr_set_bg_reveal_duration),
            description = stringResource(R.string.lyr_set_bg_reveal_duration_desc),
            value = settings.bgRevealDurationMs,
            range = LyricsStyleSettings.BG_REVEAL_DURATION_RANGE,
            step = 100,
            valueText = ::secondsText,
            onValueChange = { v -> onUpdate { it.copy(bgRevealDurationMs = v) } },
        )
        LyricsSettingsToggle(
            label = stringResource(R.string.lyr_set_bg_reveal_fade),
            description = stringResource(R.string.lyr_set_bg_reveal_fade_desc),
            checked = settings.bgRevealFadeEnabled,
            onCheckedChange = { v -> onUpdate { it.copy(bgRevealFadeEnabled = v) } },
        )
        if (settings.bgRevealFadeEnabled) {
            LyricsSettingsSlider(
                label = stringResource(R.string.lyr_set_bg_reveal_fade_duration),
                description = stringResource(R.string.lyr_set_bg_reveal_fade_duration_desc),
                value = settings.bgRevealFadeMs,
                range = LyricsStyleSettings.BG_REVEAL_FADE_RANGE,
                step = 100,
                valueText = ::secondsText,
                onValueChange = { v -> onUpdate { it.copy(bgRevealFadeMs = v) } },
            )
        }
        if (settings.bgRevealStyle == LyricsStyleSettings.BG_REVEAL_STYLE_FLIP) {
            LyricsSettingsSlider(
                label = stringResource(R.string.lyr_set_bg_reveal_flip_angle),
                description = stringResource(R.string.lyr_set_bg_reveal_flip_angle_desc),
                value = settings.bgRevealFlipAngle,
                range = LyricsStyleSettings.BG_REVEAL_FLIP_ANGLE_RANGE,
                step = 30,
                valueText = { "$it°" },
                onValueChange = { v -> onUpdate { it.copy(bgRevealFlipAngle = v) } },
            )
            LyricsSettingsSlider(
                label = stringResource(R.string.lyr_set_bg_reveal_flip_pivot),
                description = stringResource(R.string.lyr_set_bg_reveal_flip_pivot_desc),
                value = settings.bgRevealFlipPivot,
                range = LyricsStyleSettings.BG_REVEAL_FLIP_PIVOT_RANGE,
                step = 10,
                valueText = { "$it%" },
                onValueChange = { v -> onUpdate { it.copy(bgRevealFlipPivot = v) } },
            )
        } else {
            LyricsSettingsToggle(
                label = stringResource(R.string.lyr_set_bg_reveal_blur),
                description = stringResource(R.string.lyr_set_bg_reveal_blur_desc),
                checked = settings.bgRevealBlurEnabled,
                onCheckedChange = { v -> onUpdate { it.copy(bgRevealBlurEnabled = v) } },
            )
            if (settings.bgRevealBlurEnabled) {
                LyricsSettingsSlider(
                    label = stringResource(R.string.lyr_set_bg_reveal_blur_amount),
                    description = stringResource(R.string.lyr_set_bg_reveal_blur_amount_desc),
                    value = settings.bgRevealBlurAmount,
                    range = LyricsStyleSettings.BG_REVEAL_BLUR_RANGE,
                    step = 1,
                    valueText = { "$it dp" },
                    onValueChange = { v -> onUpdate { it.copy(bgRevealBlurAmount = v) } },
                )
            }
        }
    }
    LyricsSettingsToggle(
        label = stringResource(R.string.lyr_set_fix_anchor),
        description = stringResource(R.string.lyr_set_fix_anchor_desc),
        checked = settings.fixAnchorEnabled,
        onCheckedChange = { v -> onUpdate { it.copy(fixAnchorEnabled = v) } },
    )
}

@Composable
private fun LineEffectSection(
    settings: LyricsStyleSettings,
    onUpdate: ((LyricsStyleSettings) -> LyricsStyleSettings) -> Unit,
) {
    LyricsSettingsSection(stringResource(R.string.lyr_set_section_line_effect))

    LyricsSettingsChips(
        label = stringResource(R.string.lyr_set_line_effect),
        description = stringResource(R.string.lyr_set_line_effect_desc),
        options = LineEffect.entries,
        selected = settings.lineEffect,
        optionLabel = {
            stringResource(
                when (it) {
                    LineEffect.Stretch -> R.string.lyr_set_effect_stretch
                    LineEffect.Jiggle -> R.string.lyr_set_effect_jiggle
                    LineEffect.Off -> R.string.lyr_set_effect_off
                },
            )
        },
        onSelect = { v -> onUpdate { it.copy(lineEffect = v) } },
    )

    if (settings.lineEffect == LineEffect.Stretch) {
        LyricsSettingsSlider(
            label = stringResource(R.string.lyr_set_stretch_strength),
            description = stringResource(R.string.lyr_set_stretch_strength_desc),
            value = settings.stretchStrength,
            range = LyricsStyleSettings.STRETCH_STRENGTH_RANGE,
            step = 5,
            valueText = { "$it%" },
            onValueChange = { v -> onUpdate { it.copy(stretchStrength = v) } },
        )
        LyricsSettingsSlider(
            label = stringResource(R.string.lyr_set_stretch_delay),
            description = stringResource(R.string.lyr_set_stretch_delay_desc),
            value = settings.stretchDelayMs,
            range = LyricsStyleSettings.STRETCH_DELAY_RANGE,
            step = 5,
            valueText = { "$it ms" },
            onValueChange = { v -> onUpdate { it.copy(stretchDelayMs = v) } },
        )
        LyricsSettingsSlider(
            label = stringResource(R.string.lyr_set_stretch_duration),
            description = stringResource(R.string.lyr_set_stretch_duration_desc),
            value = settings.stretchDurationMs,
            range = LyricsStyleSettings.STRETCH_DURATION_RANGE,
            step = 50,
            valueText = { "$it ms" },
            onValueChange = { v -> onUpdate { it.copy(stretchDurationMs = v) } },
        )
    }
}

@Composable
private fun JiggleSection(
    settings: LyricsStyleSettings,
    onUpdate: ((LyricsStyleSettings) -> LyricsStyleSettings) -> Unit,
) {
    LyricsSettingsSection(stringResource(R.string.lyr_set_section_jiggle))

    LyricsSettingsToggle(
        label = stringResource(R.string.lyr_set_jiggle),
        description = stringResource(R.string.lyr_set_jiggle_desc),
        checked = settings.jiggleEnabled,
        onCheckedChange = { v -> onUpdate { it.copy(jiggleEnabled = v) } },
    )

    // The three jiggle sliders only matter while the effect is on.
    if (settings.jiggleEnabled) {
        LyricsSettingsSlider(
            label = stringResource(R.string.lyr_set_jiggle_strength),
            description = stringResource(R.string.lyr_set_jiggle_strength_desc),
            value = settings.jiggleStrength,
            range = LyricsStyleSettings.JIGGLE_STRENGTH_RANGE,
            step = 5,
            valueText = { "$it%" },
            onValueChange = { v -> onUpdate { it.copy(jiggleStrength = v) } },
        )
        LyricsSettingsSlider(
            label = stringResource(R.string.lyr_set_jiggle_duration),
            description = stringResource(R.string.lyr_set_jiggle_duration_desc),
            value = settings.jiggleDurationMs,
            range = LyricsStyleSettings.JIGGLE_DURATION_RANGE,
            step = 100,
            valueText = { String.format(java.util.Locale.US, "%.1f s", it / 1000f) },
            onValueChange = { v -> onUpdate { it.copy(jiggleDurationMs = v) } },
        )
        LyricsSettingsSlider(
            label = stringResource(R.string.lyr_set_jiggle_delay),
            description = stringResource(R.string.lyr_set_jiggle_delay_desc),
            value = settings.jiggleDelayMs,
            range = LyricsStyleSettings.JIGGLE_DELAY_RANGE,
            step = 5,
            valueText = { "$it ms" },
            onValueChange = { v -> onUpdate { it.copy(jiggleDelayMs = v) } },
        )
    }
}

@Composable
private fun EnhanceHeaderSection(
    settings: LyricsStyleSettings,
    onUpdate: ((LyricsStyleSettings) -> LyricsStyleSettings) -> Unit,
) {
    LyricsSettingsSection(stringResource(R.string.lyr_set_section_header))

    LyricsSettingsToggle(
        label = stringResource(R.string.lyr_set_header_enable),
        description = stringResource(R.string.lyr_set_header_enable_desc),
        checked = settings.enhanceHeader,
        onCheckedChange = { v -> onUpdate { it.copy(enhanceHeader = v) } },
    )
    // Off = the original header, so the options below only appear when it is on.
    if (!settings.enhanceHeader) return

    val dp: (Int) -> String = { "$it dp" }
    val sp: (Int) -> String = { "$it sp" }
    val percent: (Int) -> String = { "$it%" }

    LyricsSettingsSlider(
        label = stringResource(R.string.lyr_set_header_album),
        description = stringResource(R.string.lyr_set_header_album_desc),
        value = settings.headerAlbumSize,
        range = LyricsStyleSettings.HEADER_ALBUM_RANGE,
        step = 1,
        valueText = dp,
        onValueChange = { v -> onUpdate { it.copy(headerAlbumSize = v) } },
    )
    LyricsSettingsToggle(
        label = stringResource(R.string.lyr_set_header_outline),
        description = stringResource(R.string.lyr_set_header_outline_desc),
        checked = settings.headerOutlineEnabled,
        onCheckedChange = { v -> onUpdate { it.copy(headerOutlineEnabled = v) } },
    )
    if (settings.headerOutlineEnabled) {
        LyricsSettingsSlider(
            label = stringResource(R.string.lyr_set_header_outline_weight),
            description = stringResource(R.string.lyr_set_header_outline_weight_desc),
            value = settings.headerOutlineWeight,
            range = LyricsStyleSettings.HEADER_OUTLINE_WEIGHT_RANGE,
            step = 1,
            valueText = { String.format(java.util.Locale.US, "%.1f dp", it / 2f) },
            onValueChange = { v -> onUpdate { it.copy(headerOutlineWeight = v) } },
        )
        LyricsSettingsSlider(
            label = stringResource(R.string.lyr_set_header_outline_opacity),
            description = stringResource(R.string.lyr_set_header_outline_opacity_desc),
            value = settings.headerOutlineOpacity,
            range = LyricsStyleSettings.HEADER_OUTLINE_OPACITY_RANGE,
            step = 1,
            valueText = percent,
            onValueChange = { v -> onUpdate { it.copy(headerOutlineOpacity = v) } },
        )
    }
    LyricsSettingsSlider(
        label = stringResource(R.string.lyr_set_header_title),
        description = stringResource(R.string.lyr_set_header_title_desc),
        value = settings.headerTitleSize,
        range = LyricsStyleSettings.HEADER_TITLE_RANGE,
        step = 1,
        valueText = sp,
        onValueChange = { v -> onUpdate { it.copy(headerTitleSize = v) } },
    )
    LyricsSettingsSlider(
        label = stringResource(R.string.lyr_set_header_artist),
        description = stringResource(R.string.lyr_set_header_artist_desc),
        value = settings.headerArtistSize,
        range = LyricsStyleSettings.HEADER_ARTIST_RANGE,
        step = 1,
        valueText = sp,
        onValueChange = { v -> onUpdate { it.copy(headerArtistSize = v) } },
    )
    LyricsSettingsSlider(
        label = stringResource(R.string.lyr_set_header_buttons),
        description = stringResource(R.string.lyr_set_header_buttons_desc),
        value = settings.headerButtonSize,
        range = LyricsStyleSettings.HEADER_BUTTON_RANGE,
        step = 1,
        valueText = dp,
        onValueChange = { v -> onUpdate { it.copy(headerButtonSize = v) } },
    )
    LyricsSettingsSlider(
        label = stringResource(R.string.lyr_set_header_line_weight),
        description = stringResource(R.string.lyr_set_header_line_weight_desc),
        value = settings.headerLineWeight,
        range = LyricsStyleSettings.HEADER_LINE_WEIGHT_RANGE,
        step = 1,
        valueText = { String.format(java.util.Locale.US, "%.1f dp", it / 2f) },
        onValueChange = { v -> onUpdate { it.copy(headerLineWeight = v) } },
    )
    LyricsSettingsSlider(
        label = stringResource(R.string.lyr_set_header_line_opacity),
        description = stringResource(R.string.lyr_set_header_line_opacity_desc),
        value = settings.headerLineOpacity,
        range = LyricsStyleSettings.HEADER_LINE_OPACITY_RANGE,
        step = 1,
        valueText = percent,
        onValueChange = { v -> onUpdate { it.copy(headerLineOpacity = v) } },
    )
    LyricsSettingsSlider(
        label = stringResource(R.string.lyr_set_header_bottom_padding),
        description = stringResource(R.string.lyr_set_header_bottom_padding_desc),
        value = settings.headerBottomPadding,
        range = LyricsStyleSettings.HEADER_BOTTOM_PADDING_RANGE,
        step = 1,
        valueText = dp,
        onValueChange = { v -> onUpdate { it.copy(headerBottomPadding = v) } },
    )
    LyricsSettingsToggle(
        label = stringResource(R.string.lyr_set_header_blur),
        description = stringResource(R.string.lyr_set_header_blur_desc),
        checked = settings.headerBlurEnabled,
        onCheckedChange = { v -> onUpdate { it.copy(headerBlurEnabled = v) } },
    )
    if (settings.headerBlurEnabled) {
        LyricsSettingsSlider(
            label = stringResource(R.string.lyr_set_header_blur_amount),
            description = stringResource(R.string.lyr_set_header_blur_amount_desc),
            value = settings.headerBlurAmount,
            range = LyricsStyleSettings.HEADER_BLUR_RANGE,
            step = 1,
            valueText = dp,
            onValueChange = { v -> onUpdate { it.copy(headerBlurAmount = v) } },
        )
    }
    // Soft edge works with blur on (the blur fades out) and off (the lyrics fade in).
    LyricsSettingsToggle(
        label = stringResource(R.string.lyr_set_header_soft_edge),
        description = stringResource(R.string.lyr_set_header_soft_edge_desc),
        checked = settings.headerSoftEdge,
        onCheckedChange = { v -> onUpdate { it.copy(headerSoftEdge = v) } },
    )
    if (settings.headerSoftEdge) {
        LyricsSettingsSlider(
            label = stringResource(R.string.lyr_set_header_edge_fade),
            description = stringResource(R.string.lyr_set_header_edge_fade_desc),
            value = settings.headerEdgeFade,
            range = LyricsStyleSettings.HEADER_EDGE_FADE_RANGE,
            step = 1,
            valueText = dp,
            onValueChange = { v -> onUpdate { it.copy(headerEdgeFade = v) } },
        )
    }

    // ---- Bottom bar (translation / transliteration buttons + player pill) ----
    LyricsSettingsSection(stringResource(R.string.lyr_set_section_dock))
    LyricsSettingsToggle(
        label = stringResource(R.string.lyr_set_dock_blur),
        description = stringResource(R.string.lyr_set_dock_blur_desc),
        checked = settings.dockBlurEnabled,
        onCheckedChange = { v -> onUpdate { it.copy(dockBlurEnabled = v) } },
    )
    if (settings.dockBlurEnabled) {
        LyricsSettingsSlider(
            label = stringResource(R.string.lyr_set_dock_blur_amount),
            description = stringResource(R.string.lyr_set_dock_blur_amount_desc),
            value = settings.dockBlurAmount,
            range = LyricsStyleSettings.DOCK_BLUR_RANGE,
            step = 1,
            valueText = dp,
            onValueChange = { v -> onUpdate { it.copy(dockBlurAmount = v) } },
        )
        LyricsSettingsChips(
            label = stringResource(R.string.lyr_set_dock_style),
            description = stringResource(R.string.lyr_set_dock_style_desc),
            options = listOf(false, true),
            selected = settings.dockSpreadBlur,
            optionLabel = {
                if (it) {
                    stringResource(R.string.lyr_set_dock_style_spread)
                } else {
                    stringResource(R.string.lyr_set_dock_style_normal)
                }
            },
            onSelect = { v -> onUpdate { it.copy(dockSpreadBlur = v) } },
        )
        // Spread only: the blur fades out above the bar (same idea as the header's soft edge).
        if (settings.dockSpreadBlur) {
            LyricsSettingsToggle(
                label = stringResource(R.string.lyr_set_dock_soft_edge),
                description = stringResource(R.string.lyr_set_dock_soft_edge_desc),
                checked = settings.dockSoftEdge,
                onCheckedChange = { v -> onUpdate { it.copy(dockSoftEdge = v) } },
            )
            if (settings.dockSoftEdge) {
                LyricsSettingsSlider(
                    label = stringResource(R.string.lyr_set_dock_edge_fade),
                    description = stringResource(R.string.lyr_set_dock_edge_fade_desc),
                    value = settings.dockEdgeFade,
                    range = LyricsStyleSettings.DOCK_EDGE_FADE_RANGE,
                    step = 1,
                    valueText = dp,
                    onValueChange = { v -> onUpdate { it.copy(dockEdgeFade = v) } },
                )
            }
        }
    }
    LyricsSettingsSlider(
        label = stringResource(R.string.lyr_set_dock_size),
        description = stringResource(R.string.lyr_set_dock_size_desc),
        value = settings.dockSize,
        range = LyricsStyleSettings.DOCK_SIZE_RANGE,
        step = 1,
        valueText = dp,
        onValueChange = { v -> onUpdate { it.copy(dockSize = v) } },
    )
    LyricsSettingsSlider(
        label = stringResource(R.string.lyr_set_dock_bottom_padding),
        description = stringResource(R.string.lyr_set_dock_bottom_padding_desc),
        value = settings.dockBottomPadding,
        range = LyricsStyleSettings.DOCK_BOTTOM_PADDING_RANGE,
        step = 1,
        valueText = dp,
        onValueChange = { v -> onUpdate { it.copy(dockBottomPadding = v) } },
    )
    LyricsSettingsSlider(
        label = stringResource(R.string.lyr_set_dock_pill_padding),
        description = stringResource(R.string.lyr_set_dock_pill_padding_desc),
        value = settings.dockPillPadding,
        range = LyricsStyleSettings.DOCK_PILL_PADDING_RANGE,
        step = 1,
        valueText = dp,
        onValueChange = { v -> onUpdate { it.copy(dockPillPadding = v) } },
    )
    LyricsSettingsSlider(
        label = stringResource(R.string.lyr_set_dock_gap_left),
        description = stringResource(R.string.lyr_set_dock_gap_left_desc),
        value = settings.dockGapLeft,
        range = LyricsStyleSettings.DOCK_GAP_RANGE,
        step = 1,
        valueText = dp,
        onValueChange = { v -> onUpdate { it.copy(dockGapLeft = v) } },
    )
    LyricsSettingsSlider(
        label = stringResource(R.string.lyr_set_dock_gap_right),
        description = stringResource(R.string.lyr_set_dock_gap_right_desc),
        value = settings.dockGapRight,
        range = LyricsStyleSettings.DOCK_GAP_RANGE,
        step = 1,
        valueText = dp,
        onValueChange = { v -> onUpdate { it.copy(dockGapRight = v) } },
    )

    LyricsSettingsResetButton(
        text = stringResource(R.string.lyr_set_reset_header),
        onClick = {
            val d = LyricsStyleSettings()
            onUpdate {
                it.copy(
                    headerAlbumSize = d.headerAlbumSize,
                    headerTitleSize = d.headerTitleSize,
                    headerArtistSize = d.headerArtistSize,
                    headerButtonSize = d.headerButtonSize,
                    headerLineWeight = d.headerLineWeight,
                    headerLineOpacity = d.headerLineOpacity,
                    headerBottomPadding = d.headerBottomPadding,
                    headerBlurEnabled = d.headerBlurEnabled,
                    headerBlurAmount = d.headerBlurAmount,
                    headerSoftEdge = d.headerSoftEdge,
                    headerEdgeFade = d.headerEdgeFade,
                    headerOutlineEnabled = d.headerOutlineEnabled,
                    headerOutlineWeight = d.headerOutlineWeight,
                    headerOutlineOpacity = d.headerOutlineOpacity,
                    dockBlurEnabled = d.dockBlurEnabled,
                    dockBlurAmount = d.dockBlurAmount,
                    dockSize = d.dockSize,
                    dockBottomPadding = d.dockBottomPadding,
                    dockPillPadding = d.dockPillPadding,
                    dockGapLeft = d.dockGapLeft,
                    dockGapRight = d.dockGapRight,
                    dockSpreadBlur = d.dockSpreadBlur,
                    dockSoftEdge = d.dockSoftEdge,
                    dockEdgeFade = d.dockEdgeFade,
                )
            }
        },
    )
}
