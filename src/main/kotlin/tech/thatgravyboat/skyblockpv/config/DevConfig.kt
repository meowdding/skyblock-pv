package tech.thatgravyboat.skyblockpv.config

import com.teamresourceful.resourcefulconfigkt.api.CategoryKt

object DevConfig : CategoryKt("Dev") {

    var devMode by boolean(false) {
        name = Translated("sbpv.dev.devMode")
        description = Translated("sbpv.dev.devMode.desc")
    }

    var hoppityParser by boolean(false) {
        name = Translated("sbpv.dev.hoppityParser")
        description = Translated("sbpv.dev.hoppityParser.desc")
    }

}
