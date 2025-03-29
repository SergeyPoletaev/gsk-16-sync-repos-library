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
     * Синхронизирует два репозитория как зеркало.
     * @param sourceRepoUrl URL исходного репозитория.
     * @param targetRepoUrl URL целевого репозитория.
     */
    static void syncReposAsMirror(String sourceRepoUrl, String targetRepoUrl) {
        def tempDir = "${System.getProperty('java.io.tmpdir')}/temp-repo-${UUID.randomUUID()}"
        def tempRepoDir = new File(tempDir)

        try {
            println "Используется временная директория: ${tempDir}"
            if (!tempRepoDir.exists()) {
                tempRepoDir.mkdirs()
            }
            executeCommand("git clone --mirror ${sourceRepoUrl} ${tempDir}")
            executeCommand("cd ${tempDir} && git remote add target ${targetRepoUrl}")
            executeCommand("cd ${tempDir} && git push target --mirror")
        } finally {
            if (tempRepoDir.exists()) {
                tempRepoDir.deleteDir()
            }
        }
    }
}