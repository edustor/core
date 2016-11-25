package db.migration

import org.apache.commons.codec.digest.DigestUtils
import org.flywaydb.core.api.migration.spring.SpringJdbcMigration
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.JdbcTemplate
import ru.edustor.core.pdf.storage.PdfStorage

@Suppress("unused")
class V4__Page_md5 : SpringJdbcMigration {

    val logger: Logger = LoggerFactory.getLogger(V4__Page_md5::class.java)

    override fun migrate(jdbcTemplate: JdbcTemplate) {
        val pdfStorage = PdfStorage()

        jdbcTemplate.execute("ALTER TABLE page ADD COLUMN file_md5 CHARACTER VARYING(32);")

        val uploadedPageIds = jdbcTemplate.queryForList("SELECT id FROM page WHERE is_uploaded = TRUE;")
                .map { it["id"] as String }

        val total = uploadedPageIds.count()
        logger.info("Calculating MD5 hash for $total pages")

        uploadedPageIds.forEachIndexed { i, pageId ->
            logger.info("[${i + 1}/$total] Processing $pageId")
            var md5: String = ""
            pdfStorage.get(pageId)!!.use { pageStream ->
                md5 = DigestUtils.md5Hex(pageStream)
            }

            logger.info("[${i + 1}/$total] MD5: $md5")

            jdbcTemplate.update("UPDATE page SET file_md5 = '$md5' WHERE id = '$pageId';")
        }
    }
}