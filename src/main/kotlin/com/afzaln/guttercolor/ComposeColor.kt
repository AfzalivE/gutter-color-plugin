/*
 * Copyright 2019 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.afzaln.guttercolor

/**
 * The `Color` class contains color information to be used while painting
 * in [Canvas]. `Color` supports [ColorSpace]s with 3 [components][ColorSpace.componentCount],
 * plus one for [alpha].
 *
 * ### Creating
 *
 * `Color` can be created with one of these methods:
 *
 *     // from 4 separate [Float] components. Alpha and ColorSpace are optional
 *     val rgbaWhiteFloat = Color(red = 1f, green = 1f, blue = 1f, alpha = 1f,
 *         ColorSpace.get(ColorSpaces.Srgb))
 *
 *     // from a 32-bit SRGB color integer
 *     val fromIntWhite = Color(android.graphics.Color.WHITE)
 *     val fromLongBlue = Color(0xFF0000FF)
 *
 *     // from SRGB integer component values. Alpha is optional
 *     val rgbaWhiteInt = Color(red = 0xFF, green = 0xFF, blue = 0xFF, alpha = 0xFF)
 *
 * ### Representation
 *
 * A `Color` always defines a color using 4 components packed in a single
 * 64 bit long value. One of these components is always alpha while the other
 * three components depend on the color space's [color model][ColorModel].
 * The most common color model is the [RGB][ColorModel.Rgb] model in
 * which the components represent red, green, and blue values.
 *
 * **Component ranges:** the ranges defined in the tables
 * below indicate the ranges that can be encoded in a color long. They do not
 * represent the actual ranges as they may differ per color space. For instance,
 * the RGB components of a color in the [Display P3][ColorSpaces.DisplayP3]
 * color space use the `[0..1]` range. Please refer to the documentation of the
 * various [color spaces][ColorSpaces] to find their respective ranges.
 *
 * **Alpha range:** while alpha is encoded in a color long using
 * a 10 bit integer (thus using a range of `[0..1023]`), it is converted to and
 * from `[0..1]` float values when decoding and encoding color longs.
 *
 * **sRGB color space:** for compatibility reasons and ease of
 * use, `Color` encoded [sRGB][ColorSpaces.Srgb] colors do not
 * use the same encoding as other color longs.
 * ```
 * | Component | Name        | Size    | Range                 |
 * |-----------|-------------|---------|-----------------------|
 * | [RGB][ColorSpace.Model.Rgb] color model |
 * | R         | Red         | 16 bits | `[-65504.0, 65504.0]` |
 * | G         | Green       | 16 bits | `[-65504.0, 65504.0]` |
 * | B         | Blue        | 16 bits | `[-65504.0, 65504.0]` |
 * | A         | Alpha       | 10 bits | `[0..1023]`           |
 * |           | Color space | 6 bits  | `[0..63]`             |
 * | [SRGB][ColorSpaces.Srgb] color space |
 * | R         | Red         | 8 bits  | `[0..255]`            |
 * | G         | Green       | 8 bits  | `[0..255]`            |
 * | B         | Blue        | 8 bits  | `[0..255]`            |
 * | A         | Alpha       | 8 bits  | `[0..255]`            |
 * | X         | Unused      | 32 bits | `[0]`                 |
 * | [XYZ][ColorSpace.Model.Xyz] color model |
 * | X         | X           | 16 bits | `[-65504.0, 65504.0]` |
 * | Y         | Y           | 16 bits | `[-65504.0, 65504.0]` |
 * | Z         | Z           | 16 bits | `[-65504.0, 65504.0]` |
 * | A         | Alpha       | 10 bits | `[0..1023]`           |
 * |           | Color space | 6 bits  | `[0..63]`             |
 * | [Lab][ColorSpace.Model.Lab] color model |
 * | L         | L           | 16 bits | `[-65504.0, 65504.0]` |
 * | a         | a           | 16 bits | `[-65504.0, 65504.0]` |
 * | b         | b           | 16 bits | `[-65504.0, 65504.0]` |
 * | A         | Alpha       | 10 bits | `[0..1023]`           |
 * |           | Color space | 6 bits  | `[0..63]`             |
 * ```
 * The components in this table are listed in encoding order (see below),
 * which is why color longs in the RGB model are called RGBA colors (even if
 * this doesn't quite hold for the special case of sRGB colors).
 *
 * The color encoding relies on half-precision float values (fp16). If you
 * wish to know more about the limitations of half-precision float values, please
 * refer to the documentation of the [Float16] class.
 *
 * The values returned by these methods depend on the color space encoded
 * in the color long. The values are however typically in the `[0..1]` range
 * for RGB colors. Please refer to the documentation of the various
 * [color spaces][ColorSpaces] for the exact ranges.
 */
@UseExperimental(ExperimentalUnsignedTypes::class)
class Color(val value: ULong) {

    /**
     * Returns the value of the red component in the range defined by this
     * color's color space (see [ColorSpace.getMinValue] and
     * [ColorSpace.getMaxValue]).
     *
     * If this color's color model is not [RGB][ColorModel.Rgb],
     * calling this is the first component of the ColorSpace.
     *
     * @see alpha
     * @see blue
     * @see green
     */
    val red: Float
        get() {
            return if ((value and 0x3fUL) == 0UL) {
                ((value shr 48) and 0xffUL).toFloat() / 255.0f
            } else {
                Float16(((value shr 48) and 0xffffUL).toShort())
                    .toFloat()
            }
        }

    /**
     * Returns the value of the green component in the range defined by this
     * color's color space (see [ColorSpace.getMinValue] and
     * [ColorSpace.getMaxValue]).
     *
     * If this color's color model is not [RGB][ColorModel.Rgb],
     * calling this is the second component of the ColorSpace.
     *
     * @see alpha
     * @see red
     * @see blue
     */
    val green: Float
        get() {
            return if ((value and 0x3fUL) == 0UL) {
                ((value shr 40) and 0xffUL).toFloat() / 255.0f
            } else {
                Float16(((value shr 32) and 0xffffUL).toShort())
                    .toFloat()
            }
        }

    /**
     * Returns the value of the blue component in the range defined by this
     * color's color space (see [ColorSpace.getMinValue] and
     * [ColorSpace.getMaxValue]).
     *
     * If this color's color model is not [RGB][ColorModel.Rgb],
     * calling this is the third component of the ColorSpace.
     *
     * @see alpha
     * @see red
     * @see green
     */
    val blue: Float
        get() {
            return if ((value and 0x3fUL) == 0UL) {
                ((value shr 32) and 0xffUL).toFloat() / 255.0f
            } else {
                Float16(((value shr 16) and 0xffffUL).toShort())
                    .toFloat()
            }
        }

    /**
     * Returns the value of the alpha component in the range `[0..1]`.
     *
     * @see red
     * @see green
     * @see blue
     */
    val alpha: Float
        get() {
            return if ((value and 0x3fUL) == 0UL) {
                ((value shr 56) and 0xffUL).toFloat() / 255.0f
            } else {
                ((value shr 6) and 0x3ffUL).toFloat() / 1023.0f
            }
        }

    operator fun component1(): Float = red

    operator fun component2(): Float = green

    operator fun component3(): Float = blue

    operator fun component4(): Float = alpha

    /**
     * Copies the existing color, changing only the provided values. The [ColorSpace][colorSpace]
     * of the returned [Color] is the same as this [colorSpace].
     */
    fun copy(
        alpha: Float = this.alpha,
        red: Float = this.red,
        green: Float = this.green,
        blue: Float = this.blue
    ): Color = composeColor(
        red = red,
        green = green,
        blue = blue,
        alpha = alpha
    )

    /**
     * Returns a string representation of the object. This method returns
     * a string equal to the value of:
     *
     *     "Color($r, $g, $b, $a, ${colorSpace.name})"
     *
     * For instance, the string representation of opaque black in the sRGB
     * color space is equal to the following value:
     *
     *     Color(0.0, 0.0, 0.0, 1.0, sRGB IEC61966-2.1)
     *
     * @return A non-null string representation of the object
     */
    override fun toString(): String {
        return "Color($red, $green, $blue, $alpha)"
    }

    companion object {
        val Black = composeColor(0xFF000000)
        val DarkGray = composeColor(0xFF444444)
        val Gray = composeColor(0xFF888888)
        val LightGray = composeColor(0xFFCCCCCC)
        val White = composeColor(0xFFFFFFFF)
        val Red = composeColor(0xFFFF0000)
        val Green = composeColor(0xFF00FF00)
        val Blue = composeColor(0xFF0000FF)
        val Yellow = composeColor(0xFFFFFF00)
        val Cyan = composeColor(0xFF00FFFF)
        val Magenta = composeColor(0xFFFF00FF)
        val Transparent = composeColor(0x00000000)
        /**
         * Because Color is an inline class, this represents an unset value
         * without having to box the Color. It will be treated as [Transparent]
         * when drawn. A Color can compare with [Unspecified] for equality or use
         * [isUnspecified] to check for the unset value or [isSpecified] for any color that isn't
         * [Unspecified].
         */
    }
}

/**
 * Create a [Color] by passing individual [red], [green], [blue], [alpha], and [colorSpace]
 * components. The default [color space][ColorSpace] is [SRGB][ColorSpaces.Srgb] and
 * the default [alpha] is `1.0` (opaque). [colorSpace] must have a [ColorSpace.componentCount] of
 * 3.
 */
@UseExperimental(ExperimentalUnsignedTypes::class)
fun composeColor(
    red: Float,
    green: Float,
    blue: Float,
    alpha: Float = 1f
): Color {
    val argb = (
            ((alpha * 255.0f + 0.5f).toInt() shl 24) or
                    ((red * 255.0f + 0.5f).toInt() shl 16) or
                    ((green * 255.0f + 0.5f).toInt() shl 8) or
                    (blue * 255.0f + 0.5f).toInt()
            )
    return Color(value = (argb.toULong() and 0xffffffffUL) shl 32)
}

/**
 * Creates a new [Color] instance from an ARGB color int.
 * The resulting color is in the [sRGB][ColorSpaces.Srgb]
 * color space.
 *
 * @param color The ARGB color int to create a <code>Color</code> from.
 * @return A non-null instance of {@link Color}
 */
@UseExperimental(ExperimentalUnsignedTypes::class)
fun composeColor(color: Int): Color {
    return Color(value = color.toULong() shl 32)
}

/**
 * Creates a new [Color] instance from an ARGB color int.
 * The resulting color is in the [sRGB][ColorSpaces.Srgb]
 * color space. This is useful for specifying colors with alpha
 * greater than 0x80 in numeric form without using [Long.toInt]:
 *
 *     val color = Color(0xFF000080)
 *
 * @param color The 32-bit ARGB color int to create a <code>Color</code>
 * from
 * @return A non-null instance of {@link Color}
 */
@UseExperimental(ExperimentalUnsignedTypes::class)
fun composeColor(color: Long): Color {
    return Color(value = (color.toULong() and 0xffffffffUL) shl 32)
}

/**
 * Creates a new [Color] instance from an ARGB color components.
 * The resulting color is in the [sRGB][ColorSpaces.Srgb]
 * color space. The default alpha value is `0xFF` (opaque).
 *
 * @return A non-null instance of {@link Color}
 */
fun composeColor(
    red: Int,
    green: Int,
    blue: Int,
    alpha: Int = 0xFF
): Color {
    val color = ((alpha and 0xFF) shl 24) or
            ((red and 0xFF) shl 16) or
            ((green and 0xFF) shl 8) or
            (blue and 0xFF)
    return composeColor(color)
}
