package ru.gsk16

class GitUtils {

    /**
     * Выполняет shell-команду.
     * @param command Команда для выполнения.
     * @return Результат выполнения команды.
     */
    static String executeCommand(String command) {
        println "--> Выполняю команду: ${command}"
        def process = new ProcessBuilder()
                .command("sh", "-c", command)
                .redirectErrorStream(true)
                .start()
        process.waitFor()

        def output = process.inputStream.text.trim()
        println "--> Результат команды: ${output}"

        if (process.exitValue() != 0) {
            throw new RuntimeException("""
                Ошибка при выполнении команды: ${command}
                Output: ${output}
            """.stripIndent())
        }
        return output ?: ""
    }

    /**
     * Синхронизирует указанные ветки и теги между двумя репозиториями.
     * @param sourceRepoUrl URL исходного репозитория.
     * @param targetRepoUrl URL целевого репозитория.
     * @param options Опции синхронизации (branches, tags).
     */
    static void syncRepos(String sourceRepoUrl, String targetRepoUrl, Map options = [:]) {
        def branches = options.branches
        def tags = options.tags

        def tempDir = "temp-repo"
        def tempRepoDir = new File(tempDir)

        try {
            executeCommand("git clone --mirror ${sourceRepoUrl} ${tempDir}")
            executeCommand("cd ${tempDir} && git remote add target ${targetRepoUrl}")

            if (options.containsKey("branches")) {
                println "--> Ключ 'branches' присутствует. Ветки: ${branches}"
                branches?.each { branch ->
                    executeCommand("cd ${tempDir} && git push target refs/heads/${branch}:refs/heads/${branch}")
                }
            } else {
                println "--> Ключ 'branches' отсутствует. Синхронизируем все ветки."
                executeCommand("cd ${tempDir} && git push target --all")
            }

            if (options.containsKey("tags")) {
                println "--> Ключ 'tags' присутствует. Теги: ${tags}"
                tags?.each { tag ->
                    executeCommand("cd ${tempDir} && git push target refs/tags/${tag}:refs/tags/${tag}")
                }
            } else {
                println "--> Ключ 'tags' отсутствует. Синхронизируем все теги."
                executeCommand("cd ${tempDir} && git push target --tags")
            }

        } finally {
            if (tempRepoDir.exists()) {
                tempRepoDir.deleteDir()
            }
        }
    }
}