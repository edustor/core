package ru.wutiarn.edustor.api

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.mongodb.gridfs.GridFsOperations
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

/**
 * Created by wutiarn on 22.02.16.
 */
@RestController
@RequestMapping("/api")
open class ApiController @Autowired constructor(val gfs: GridFsOperations) {
    @RequestMapping(value = "/upload", method = arrayOf(RequestMethod.POST))
    fun upload(@RequestParam file: MultipartFile) {
        gfs.store(file.inputStream, "upload_${file.originalFilename}")
    }
}