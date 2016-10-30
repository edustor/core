ALTER TABLE "public"."subject"
  RENAME TO "folder";
ALTER TABLE "public"."lesson"
  RENAME COLUMN "subject_id" TO "folder_id";