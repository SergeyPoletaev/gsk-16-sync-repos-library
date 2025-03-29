#!/usr/bin/env groovy

import ru.gsk16.GitUtils

/**
 * Скрипт для синхронизации двух Git-репозиториев как зеркало.
 *
 * <p>Этот скрипт использует библиотеку GitUtils для полной синхронизации исходного репозитория с целевым.
 * Параметры синхронизации (URL репозиториев) должны передаваться динамически при запуске.
 * Если обязательные параметры отсутствуют, скрипт завершается с ошибкой.</p>
 *
 * <p>Обязательные параметры:</p>
 * <ul>
 *     <li><b>sourceRepoUrl</b> — URL исходного репозитория.</li>
 *     <li><b>targetRepoUrl</b> — URL целевого репозитория.</li>
 * </ul>
 *
 * <p>Пример использования:</p>
 * <pre>
 * \@Library('gsk-16-sync-repos-library') _ // Подключаем Shared Library
 *
 * stage('Sync Repositories') {
 *     syncRepos(
 *         sourceRepoUrl: 'https://github.com/username/source-repo.git',
 *         targetRepoUrl: 'https://github.com/username/target-repo.git'
 *     )
 * }
 * </pre>
 *
 * @param sourceRepoUrl URL исходного репозитория (обязательный).
 * @param targetRepoUrl URL целевого репозитория (обязательный).
 * @throws IllegalArgumentException Если обязательные параметры отсутствуют.
 *
 * @see ru.gsk16.GitUtils
 */
def call(Map params) {
    if (!params.sourceRepoUrl || !params.targetRepoUrl) {
        throw new IllegalArgumentException(
                "Обязательные параметры sourceRepoUrl и targetRepoUrl должны быть указаны.")
    }

    echo "Синхронизация репозиториев:"
    echo "  Исходный репозиторий: ${params.sourceRepoUrl}"
    echo "  Целевой репозиторий: ${params.targetRepoUrl}"

    def sourceRepoUrl = params.sourceRepoUrl
    def targetRepoUrl = params.targetRepoUrl

    GitUtils.syncReposAsMirror(sourceRepoUrl as String, targetRepoUrl as String)
    echo "Синхронизация завершена."
}