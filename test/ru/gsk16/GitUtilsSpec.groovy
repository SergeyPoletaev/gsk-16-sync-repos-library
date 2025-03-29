package ru.gsk16

import spock.lang.Specification
import spock.lang.TempDir
import java.nio.file.Path

class GitUtilsSpec extends Specification {

    @TempDir
    Path tempDir

    def "тест синхронизации репозиториев: зеркальная синхронизация всех веток и тегов"() {
        given: "Создаем локальный репозиторий и добавляем в него ветки и теги"
        def sourceRepoDir = tempDir.resolve("source-repo").toFile()
        def targetRepoDir = tempDir.resolve("target-repo").toFile()

        GitUtils.executeCommand("git init --bare ${sourceRepoDir}")

        def workDir = tempDir.resolve("work-dir").toFile()
        GitUtils.executeCommand("git clone ${sourceRepoDir} ${workDir}")

        new File(workDir, "file1.txt").text = "Hello, world!"
        GitUtils.executeCommand("cd ${workDir} && git add . && git commit -m 'Initial commit'")

        GitUtils.executeCommand("cd ${workDir} && git checkout -b feature-branch")
        new File(workDir, "file2.txt").text = "Feature branch"
        GitUtils.executeCommand("cd ${workDir} && git add . && git commit -m 'Feature commit'")

        GitUtils.executeCommand("cd ${workDir} && git tag v1.0")

        GitUtils.executeCommand("cd ${workDir} && git push origin --all")
        GitUtils.executeCommand("cd ${workDir} && git push origin --tags")

        GitUtils.executeCommand("git init --bare ${targetRepoDir}")

        when: "Синхронизируем репозитории как зеркало"
        GitUtils.syncReposAsMirror(
                "file://${sourceRepoDir.absolutePath}",
                "file://${targetRepoDir.absolutePath}"
        )

        then: "Проверяем, что все ветки и теги синхронизированы"
        def targetBranches = GitUtils.executeCommand("cd ${targetRepoDir} && git branch --list").trim()
        targetBranches.contains("* master")
        targetBranches.contains("feature-branch")

        def targetTags = GitUtils.executeCommand("cd ${targetRepoDir} && git tag").trim()
        targetTags.contains("v1.0")
    }

    def "тест синхронизации репозиториев: зеркальная синхронизация без потери данных"() {
        given: "Создаем локальный репозиторий с несколькими ветками и тегами"
        def sourceRepoDir = tempDir.resolve("source-repo").toFile()
        def targetRepoDir = tempDir.resolve("target-repo").toFile()

        GitUtils.executeCommand("git init --bare ${sourceRepoDir}")

        def workDir = tempDir.resolve("work-dir").toFile()
        GitUtils.executeCommand("git clone ${sourceRepoDir} ${workDir}")

        new File(workDir, "file1.txt").text = "Hello, world!"
        GitUtils.executeCommand("cd ${workDir} && git add . && git commit -m 'Initial commit'")

        GitUtils.executeCommand("cd ${workDir} && git checkout -b feature-branch")
        new File(workDir, "file2.txt").text = "Feature branch"
        GitUtils.executeCommand("cd ${workDir} && git add . && git commit -m 'Feature commit'")

        GitUtils.executeCommand("cd ${workDir} && git tag v1.0")
        GitUtils.executeCommand("cd ${workDir} && git tag v2.0")

        GitUtils.executeCommand("cd ${workDir} && git push origin --all")
        GitUtils.executeCommand("cd ${workDir} && git push origin --tags")

        GitUtils.executeCommand("git init --bare ${targetRepoDir}")

        when: "Синхронизируем репозитории как зеркало"
        GitUtils.syncReposAsMirror(
                "file://${sourceRepoDir.absolutePath}",
                "file://${targetRepoDir.absolutePath}"
        )

        then: "Целевой репозиторий должен содержать все данные из исходного"
        def targetBranches = GitUtils.executeCommand("cd ${targetRepoDir} && git branch --list")
                .trim()
                .split("\n")
                .collect { it.trim().replace("* ", "") } // Убираем символ "*" для текущей ветки
        targetBranches.contains("master")
        targetBranches.contains("feature-branch")

        def targetTags = GitUtils.executeCommand("cd ${targetRepoDir} && git tag --list")
                .trim()
                .split("\n")
                .collect { it.trim() }
        targetTags.contains("v1.0")
        targetTags.contains("v2.0")
    }
}