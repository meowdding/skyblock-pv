package me.owdding.skyblockpv.api.data.shared.types

import me.owdding.ktcodecs.FieldName

data class Melody(
    @FieldName("hymn_to_the_joy") val hymnToTheJoy: Int,
    @FieldName("frere_jacques") val frereJacques: Int,
    @FieldName("amazing_grace") val amazingGrace: Int,
    @FieldName("brahams_lullaby") val brahamsLullaby: Int,
    @FieldName("happy_birthday_to_you") val happyBirthdayToYou: Int,
    val greensleeves: Int,
    val geothermy: Int,
    val minuet: Int,
    @FieldName("joy_to_the_world") val joyToTheWorld: Int,
    @FieldName("godly_imagination") val godlyImagination: Int,
    @FieldName("la_vie_en_rose") val laVieEnRose: Int,
    @FieldName("through_the_campfire") val throughTheCampfire: Int,
    val pachelbel: Int,
)
