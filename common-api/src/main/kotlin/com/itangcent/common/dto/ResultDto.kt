package com.itangcent.common.dto

class ResultDto<T> {
    var msg: String? = null
    var code: Int? = null

    var date: T? = null
}