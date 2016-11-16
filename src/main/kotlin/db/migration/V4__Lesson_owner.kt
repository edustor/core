package db.migration

import org.flywaydb.core.api.migration.spring.SpringJdbcMigration
import org.springframework.jdbc.core.JdbcTemplate

@Suppress("unused")
class V4__Lesson_owner : SpringJdbcMigration {
    override fun migrate(jdbcTemplate: JdbcTemplate) {

        jdbcTemplate.execute("ALTER TABLE lesson ADD COLUMN owner_id character varying(255);")

        val folderOwners = jdbcTemplate.queryForList("SELECT id, owner_id FROM folder;")
                .associateBy({ it["id"] }, { it["owner_id"] })

        jdbcTemplate.queryForList("SELECT id, folder_id FROM lesson;").forEach { lessonMap ->
            val ownerId = folderOwners[lessonMap["folder_id"]]
            val id = lessonMap["id"]
            jdbcTemplate.update("UPDATE lesson SET owner_id = '$ownerId' WHERE id = '$id';")
        }

        jdbcTemplate.execute("ALTER TABLE lesson ALTER COLUMN owner_id SET NOT NULL;")
    }
}