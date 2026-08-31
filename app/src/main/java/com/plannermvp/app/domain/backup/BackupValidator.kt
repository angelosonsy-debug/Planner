package com.plannermvp.app.domain.backup

enum class BackupIssueLevel { WARNING, ERROR }

data class BackupIssue(val level: BackupIssueLevel, val message: String)

/**
 * Section 32: "validate before restoring" / "handle corrupted backups
 * safely". A malformed *file* never even reaches this point — that's
 * BackupSerializer.fromJson throwing BackupParseException. This validates
 * a file that parsed successfully but might still be internally
 * inconsistent (duplicate IDs, a check-in for a habit that isn't in the
 * file, etc.).
 *
 * Only an unsupported format version is a hard ERROR (we genuinely can't
 * trust the field mappings for a shape we don't recognize). Everything
 * else is a WARNING: BackupRepository.restoreData() sanitizes these
 * automatically (drops orphaned check-ins, clears a dangling project
 * reference) rather than failing the whole restore over a few bad rows —
 * restoring 99% of someone's data beats restoring none of it.
 */
object BackupValidator {

    fun validate(data: BackupData): List<BackupIssue> {
        val issues = mutableListOf<BackupIssue>()

        if (data.formatVersion != BACKUP_FORMAT_VERSION) {
            issues += BackupIssue(
                BackupIssueLevel.ERROR,
                "Unsupported backup format version (${data.formatVersion}); this app reads version $BACKUP_FORMAT_VERSION."
            )
            return issues // field mappings for a different version can't be trusted, so stop here
        }

        val projectIds = data.projects.map { it.id }.toSet()
        val habitIds = data.habits.map { it.id }.toSet()

        if (projectIds.size != data.projects.size) {
            issues += BackupIssue(BackupIssueLevel.WARNING, "This backup has duplicate project IDs; only the last of each will be kept.")
        }
        if (habitIds.size != data.habits.size) {
            issues += BackupIssue(BackupIssueLevel.WARNING, "This backup has duplicate habit IDs; only the last of each will be kept.")
        }
        if (data.tasks.map { it.id }.toSet().size != data.tasks.size) {
            issues += BackupIssue(BackupIssueLevel.WARNING, "This backup has duplicate task IDs; only the last of each will be kept.")
        }

        val orphanedCheckIns = data.habitCheckIns.count { it.habitId !in habitIds }
        if (orphanedCheckIns > 0) {
            issues += BackupIssue(
                BackupIssueLevel.WARNING,
                "$orphanedCheckIns habit check-in(s) reference a habit that isn't in this backup and will be skipped."
            )
        }

        val danglingProjectTasks = data.tasks.count { it.projectId != null && it.projectId !in projectIds }
        if (danglingProjectTasks > 0) {
            issues += BackupIssue(
                BackupIssueLevel.WARNING,
                "$danglingProjectTasks task(s) reference a project that isn't in this backup; they'll restore without that project link."
            )
        }

        return issues
    }

    fun hasBlockingError(issues: List<BackupIssue>): Boolean = issues.any { it.level == BackupIssueLevel.ERROR }
}
