package io.github.pavelshel1.delta.about

import io.github.pavelshel1.delta.BuildConfig

data class AppInfo(
    val appName: String,
    val description: String,
    val version: String,
    val buildLabel: String,
    val author: String,
    val githubRepoUrl: String,
    val githubProfileUrl: String,
    val license: String,
) {
    companion object {
        fun fromBuildConfig(appName: String) = AppInfo(
            appName           = appName,
            description       = BuildConfig.APP_DESCRIPTION,
            version           = BuildConfig.VERSION_NAME,
            buildLabel        = BuildConfig.BUILD_LABEL,
            author            = BuildConfig.AUTHOR,
            githubRepoUrl     = BuildConfig.GITHUB_REPO_URL,
            githubProfileUrl  = BuildConfig.GITHUB_PROFILE_URL,
            license           = BuildConfig.LICENSE,
        )
    }
}
