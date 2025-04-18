package tech.thatgravyboat.skyblockpv.config

import com.teamresourceful.resourcefulconfigkt.api.CategoryKt

object DevConfig : CategoryKt("Dev") {

    var devMode by boolean(false) {
        name = Translated("skyblockpv.dev.dev_mode")
        description = Translated("skyblockpv.dev.dev_mode.desc")
    }

    var hoppityParser by boolean(false) {
        name = Translated("skyblockpv.dev.hoppity_parser")
        description = Translated("skyblockpv.dev.hoppity_parser.desc")
    }

    var sacksParser by boolean(false) {
        name = Translated("skyblockpv.dev.sacks_parser")
        description = Translated("skyblockpv.dev.sacks_parser.desc")
    }

}
