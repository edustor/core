ALTER TABLE "subject"
  RENAME TO "tag";
ALTER TABLE "lesson"
  RENAME COLUMN "subject_id" TO "tag_id";