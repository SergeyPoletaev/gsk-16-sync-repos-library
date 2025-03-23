package ru.gsk16

import spock.lang.Specification
import spock.lang.TempDir
import java.nio.file.Path

class GitUtilsSpec extends Specification {

    @TempDir
    Path tempDir

    def "тест синхронизации репозиториев: синхронизация всех веток и тегов"() {
        given: "Создаем локальный репозиторий и добавляем в него ветки и теги"
        def sourceRepoDir = tempDir.resolve("source-repo").toFile()
        def targetRepoDir = tempDir.resolve("target-repo").toFile()

        // Инициализируем исходный репозиторий
        GitUtils.executeCommand("git init --bare ${sourceRepoDir}")

        // Клонируем исходный репозиторий в рабочую директорию
        def workDir = tempDir.resolve("work-dir").toFile()
        GitUtils.executeCommand("git clone ${sourceRepoDir} ${workDir}")

        // Добавляем файл и создаем коммит в ветке master
        new File(workDir, "file1.txt").text = "Hello, world!"
        GitUtils.executeCommand("cd ${workDir} && git add . && git commit -m 'Initial commit'")

        // Создаем ветку feature-branch
        GitUtils.executeCommand("cd ${workDir} && git checkout -b feature-branch")
        new File(workDir, "file2.txt").text = "Feature branch"
        GitUtils.executeCommand("cd ${workDir} && git add . && git commit -m 'Feature commit'")

        // Создаем тег v1.0
        GitUtils.executeCommand("cd ${workDir} && git tag v1.0")

        // Пушим все изменения в исходный репозиторий
        GitUtils.executeCommand("cd ${workDir} && git push origin --all")
        GitUtils.executeCommand("cd ${workDir} && git push origin --tags")

        // Инициализируем целевой репозиторий
        GitUtils.executeCommand("git init --bare ${targetRepoDir}")

        when: "Синхронизируем репозитории"
        GitUtils.syncRepos(
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

    def "тест синхронизации репозиториев: синхронизация только указанных веток"() {
        given: "Создаем локальный репозиторий и добавляем в него ветки и теги"
        def sourceRepoDir = tempDir.resolve("source-repo").toFile()
        def targetRepoDir = tempDir.resolve("target-repo").toFile()

        // Инициализируем исходный репозиторий
        GitUtils.executeCommand("git init --bare ${sourceRepoDir}")

        // Клонируем исходный репозиторий в рабочую директорию
        def workDir = tempDir.resolve("work-dir").toFile()
        GitUtils.executeCommand("git clone ${sourceRepoDir} ${workDir}")

        // Добавляем файл и создаем коммит в ветке master
        new File(workDir, "file1.txt").text = "Hello, world!"
        GitUtils.executeCommand("cd ${workDir} && git add . && git commit -m 'Initial commit'")

        // Создаем ветку feature-branch
        GitUtils.executeCommand("cd ${workDir} && git checkout -b feature-branch")
        new File(workDir, "file2.txt").text = "Feature branch"
        GitUtils.executeCommand("cd ${workDir} && git add . && git commit -m 'Feature commit'")

        // Создаем тег v1.0
        GitUtils.executeCommand("cd ${workDir} && git tag v1.0")

        // Пушим все изменения в исходный репозиторий
        GitUtils.executeCommand("cd ${workDir} && git push origin --all")
        GitUtils.executeCommand("cd ${workDir} && git push origin --tags")

        // Инициализируем целевой репозиторий
        GitUtils.executeCommand("git init --bare ${targetRepoDir}")

        when: "Синхронизируем только ветку master"
        GitUtils.syncRepos(
                "file://${sourceRepoDir.absolutePath}",
                "file://${targetRepoDir.absolutePath}",
                [branches: ["master"], tags: []] // Явно указываем, что теги не должны синхронизироваться
        )

        then: "Целевой репозиторий должен содержать только ветку master"
        def targetBranches = GitUtils.executeCommand("cd ${targetRepoDir} && git branch --list").split("\n")
        targetBranches.contains("* master")
        !targetBranches.contains("  feature-branch")

        and: "Теги не должны быть синхронизированы"
        def targetTags = GitUtils.executeCommand("cd ${targetRepoDir} && git tag --list")
                .split("\n") // Разделяем вывод на строки
                .findAll { it.trim() } // Убираем пустые строки
                .toList() // Преобразуем в список
        targetTags.isEmpty()
    }

    def "тест синхронизации репозиториев: синхронизация только указанных тегов"() {
        given: "Создаем локальный репозиторий и добавляем в него ветки и теги"
        def sourceRepoDir = tempDir.resolve("source-repo").toFile()
        def targetRepoDir = tempDir.resolve("target-repo").toFile()

        // Инициализируем исходный репозиторий
        GitUtils.executeCommand("git init --bare ${sourceRepoDir}")

        // Клонируем исходный репозиторий в рабочую директорию
        def workDir = tempDir.resolve("work-dir").toFile()
        GitUtils.executeCommand("git clone ${sourceRepoDir} ${workDir}")

        // Добавляем файл и создаем коммит в ветке master
        new File(workDir, "file1.txt").text = "Hello, world!"
        GitUtils.executeCommand("cd ${workDir} && git add . && git commit -m 'Initial commit'")

        // Создаем теги v1.0 и v2.0
        GitUtils.executeCommand("cd ${workDir} && git tag v1.0")
        GitUtils.executeCommand("cd ${workDir} && git tag v2.0")

        // Пушим все изменения в исходный репозиторий
        GitUtils.executeCommand("cd ${workDir} && git push origin --all")
        GitUtils.executeCommand("cd ${workDir} && git push origin --tags")

        // Инициализируем целевой репозиторий
        GitUtils.executeCommand("git init --bare ${targetRepoDir}")

        when: "Синхронизируем только тег v1.0"
        GitUtils.syncRepos(
                "file://${sourceRepoDir.absolutePath}",
                "file://${targetRepoDir.absolutePath}",
                [tags: ["v1.0"]]
        )

        then: "Целевой репозиторий должен содержать только тег v1.0"
        def targetTags = GitUtils.executeCommand("cd ${targetRepoDir} && git tag --list").split("\n")
        targetTags.contains("v1.0")
        !targetTags.contains("v2.0")
    }
}