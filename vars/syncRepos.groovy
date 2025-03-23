#!/usr/bin/env groovy

import ru.gsk16.GitUtils

/**
 * Скрипт для синхронизации двух Git-репозиториев.
 *
 * <p>Этот скрипт использует библиотеку GitUtils для синхронизации исходного репозитория с целевым.
 * Параметры синхронизации (URL репозиториев, ветки и теги) должны передаваться динамически при запуске.
 * Если обязательные параметры отсутствуют, скрипт завершается с ошибкой.</p>
 *
 * <p>Обязательные параметры:</p>
 * <ul>
 *     <li><b>sourceRepoUrl</b> — URL исходного репозитория.</li>
 *     <li><b>targetRepoUrl</b> — URL целевого репозитория.</li>
 * </ul>
 *
 * <p>Необязательные параметры:</p>
 * <ul>
 *     <li><b>branches</b> — список веток для синхронизации. Если не указан, синхронизируются все ветки.</li>
 *     <li><b>tags</b> — список тегов для синхронизации. Если не указан, синхронизируются все теги.</li>
 * </ul>
 *
 * <p>Примеры использования:</p>
 * <pre>
 * \@Library('gsk-16-sync-repos-library') _ // Подключаем Shared Library
 *
 * stage('Sync Repositories') {
 *     syncRepos(
 *         sourceRepoUrl: 'https://github.com/username/source-repo.git',
 *         targetRepoUrl: 'https://github.com/username/target-repo.git',
 *         branches: ["main"], // Необязательно
 *         tags: ["v1.0"]      // Необязательно
 *     )
 * }
 * </pre>
 *
 * <p>Дополнительные примеры:</p>
 * <ul>
 *     <li><b>Синхронизация всех веток и тегов:</b>
 *         <pre>
 *         \@Library('gsk-16-sync-repos-library') _
 *
 *         stage('Sync Repositories') {
 *             syncRepos(
 *                 sourceRepoUrl: 'https://github.com/username/source-repo.git',
 *                 targetRepoUrl: 'https://github.com/username/target-repo.git'
 *             )
 *         }
 *         </pre>
 *     </li>
 *     <li><b>Синхронизация только указанных веток (все теги):</b>
 *         <pre>
 *         \@Library('gsk-16-sync-repos-library') _
 *
 *         stage('Sync Repositories') {
 *             syncRepos(
 *                 sourceRepoUrl: 'https://github.com/username/source-repo.git',
 *                 targetRepoUrl: 'https://github.com/username/target-repo.git',
 *                 branches: ["main", "feature-branch"]
 *             )
 *         }
 *         </pre>
 *     </li>
 *     <li><b>Синхронизация только указанных тегов (все ветки):</b>
 *         <pre>
 *         \@Library('gsk-16-sync-repos-library') _
 *
 *         stage('Sync Repositories') {
 *             syncRepos(
 *                 sourceRepoUrl: 'https://github.com/username/source-repo.git',
 *                 targetRepoUrl: 'https://github.com/username/target-repo.git',
 *                 tags: ["v1.0", "v2.0"]
 *             )
 *         }
 *         </pre>
 *     </li>
 *     <li><b>Синхронизация без тегов (только ветки):</b>
 *         <pre>
 *         \@Library('gsk-16-sync-repos-library') _
 *
 *         stage('Sync Repositories') {
 *             syncRepos(
 *                 sourceRepoUrl: 'https://github.com/username/source-repo.git',
 *                 targetRepoUrl: 'https://github.com/username/target-repo.git',
 *                 branches: ["main"],
 *                 tags: [] // Пустой список, теги не синхронизируются
 *             )
 *         }
 *         </pre>
 *     </li>
 * </ul>
 *
 * @param sourceRepoUrl URL исходного репозитория (обязательный).
 * @param targetRepoUrl URL целевого репозитория (обязательный).
 * @param branches Список веток для синхронизации (необязательный).
 * @param tags Список тегов для синхронизации (необязательный).
 * @throws IllegalArgumentException Если обязательные параметры отсутствуют.
 *
 * @see ru.gsk16.GitUtils
 */
def call(Map params) {
    if (!params.sourceRepoUrl || !params.targetRepoUrl) {
        throw new IllegalArgumentException(
                "Обязательные параметры sourceRepoUrl и targetRepoUrl должны быть указаны.")
    }

    def sourceRepoUrl = params.sourceRepoUrl
    def targetRepoUrl = params.targetRepoUrl

    def branches = params.branches
    def tags = params.tags

    GitUtils.syncRepos(sourceRepoUrl, targetRepoUrl, [branches: branches, tags: tags])
}