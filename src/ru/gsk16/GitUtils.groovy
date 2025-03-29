package ru.gsk16

class GitUtils {

    /**
     * Выполняет shell-команду.
     * @param command Команда для выполнения.
     * @return Результат выполнения команды.
     */
    static String executeCommand(String command) {
        echo "--> Выполняю команду: ${command}"
        def process = new ProcessBuilder()
                .command("sh", "-c", command)
                .redirectErrorStream(true)
                .start()
        process.waitFor()

        def output = process.inputStream.text.trim()
        echo "--> Результат команды: ${output}"

        if (process.exitValue() != 0) {
            throw new RuntimeException("""
                Ошибка при выполнении команды: ${command}
                Output: ${output}
            """.stripIndent())
        }
        return output ?: ""
    }

    /**
     * Синхронизирует два репозитория как зеркало.
     * @param sourceRepoUrl URL исходного репозитория.
     * @param targetRepoUrl URL целевого репозитория.
     */
    static void syncReposAsMirror(String sourceRepoUrl, String targetRepoUrl) {
        def tempDir = "${System.getProperty('java.io.tmpdir')}/temp-repo-${UUID.randomUUID()}"
        def tempRepoDir = new File(tempDir)

        try {
            echo "Используется временная директория: ${tempDir}"
            if (!tempRepoDir.exists()) {
                tempRepoDir.mkdirs()
            }
            executeCommand("git clone --mirror ${sourceRepoUrl} ${tempDir}")
            echo "Репозиторий успешно клонирован."
            executeCommand("cd ${tempDir} && git remote add target ${targetRepoUrl}")
            echo "Удалённый репозиторий добавлен."
            executeCommand("cd ${tempDir} && git push target --mirror")
            echo "Данные успешно синхронизированы."
        } finally {
            if (tempRepoDir.exists()) {
                tempRepoDir.deleteDir()
                echo "Временная директория удалена."
            }
        }
    }
}