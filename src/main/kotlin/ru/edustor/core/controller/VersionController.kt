package ru.edustor.core.controller

import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.edustor.core.util.EdustorVersionInfoHolder

@RestController
class VersionController(val versionInfoHolder: EdustorVersionInfoHolder) {
    @RequestMapping("version")
    fun version(): EdustorVersionInfoHolder {
        return versionInfoHolder
    }
}