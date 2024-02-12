package com.itangcent.utils

import org.eclipse.jgit.api.Git
import java.io.File

object GitUtils {
    fun  getGitBranchName(projectPath:String):String{
        val file = File(projectPath)

        try {
            return Git.open(file).use { git->{
                val repository = git.repository
                val branch = repository.branch
                branch
            } }.invoke()
        } catch (e: Exception) {
            throw e;
        }
    }
}