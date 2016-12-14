package db.migration

import org.flywaydb.core.api.migration.spring.SpringJdbcMigration
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.JdbcTemplate

@Suppress("unused")
class V5__Initialize_file_id : SpringJdbcMigration {

    val logger = LoggerFactory.getLogger(V5__Initialize_file_id::class.java)

    override fun migrate(jdbcTemplate: JdbcTemplate) {
        jdbcTemplate.execute("ALTER TABLE page ADD COLUMN file_id CHARACTER VARYING(255);")

        val builder = StringBuilder()
        jdbcTemplate.queryForList("SELECT id FROM page WHERE is_uploaded = TRUE AND file_id IS NULL")
                .forEach { p ->
                    val id = p["id"]
                    logger.info("Updating page $id")
                    builder.appendln("UPDATE page SET file_id = '$id' WHERE id = '$id';")
                }
        logger.info("Executing update")
        jdbcTemplate.execute(builder.toString())
        logger.info("Migration done")
    }
}